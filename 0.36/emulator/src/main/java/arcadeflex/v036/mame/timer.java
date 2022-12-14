/*
 * ported to 0.36 
 */
package arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.timerH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.cpuintrf.*;

public class timer {

    /*TODO*/
    //    #define VERBOSE 0
    public static final int MAX_TIMERS = 256;

    /*
   *		internal timer structures
     */
    public static class timer_entry {

        public timer_entry next;
        public timer_entry prev;
        public TimerCallbackHandlerPtr callback;
        public int callback_param;
        public int enabled;
        public double period;
        public double start;
        public double expire;
    }

    public static class cpu_entry {

        public int[] icount;
        public BurnHandlerPtr burn;
        public int index;
        public int suspended;
        public int trigger;
        public int nocount;
        public int lost;
        public double time;
        public double sec_to_cycles;
        public double cycles_to_sec;
        public double overclock;
    }

    /* conversion constants */
    static double[] cycles_to_sec = new double[MAX_CPU];
    static double[] sec_to_cycles = new double[MAX_CPU];

    /* list of per-CPU timer data */
    static cpu_entry[] cpudata = new cpu_entry[MAX_CPU + 1];
    static int /*cpu_entry*/ lastcpu_ptr;
    static int /*cpu_entry*/ active_cpu_ptr;
    static int /*cpu_entry*/ last_active_cpu_ptr;

    /* list of active timers */
    static timer_entry[] timers = new timer_entry[MAX_TIMERS];
    static timer_entry timer_head;
    static timer_entry timer_free_head;

    /* other internal states */
    static double base_time;
    static double global_offset;
    static timer_entry callback_timer;
    static int callback_timer_modified;

    /**
     * return the current absolute time
     */
    public static double getabsolutetime() {
        if (active_cpu_ptr != -1
                && (cpudata[active_cpu_ptr].icount[0] + cpudata[active_cpu_ptr].lost) > 0) {
            return base_time
                    - ((double) (cpudata[active_cpu_ptr].icount[0] + cpudata[active_cpu_ptr].lost)
                    * cpudata[active_cpu_ptr].cycles_to_sec);
        } else {
            return base_time;
        }
    }

    /**
     * adjust the current CPU's timer so that a new event will fire at the right
     * time
     */
    public static void timer_adjust(timer_entry timer, double time, double period) {
        int newicount, diff;

        /* compute a new icount for the current CPU */
        if (period == TIME_NOW) {
            newicount = 0;
        } else {
            newicount = (int) ((timer.expire - time) * cpudata[active_cpu_ptr].sec_to_cycles) + 1;
        }

        /* determine if we're scheduled to run more cycles */
        diff = cpudata[active_cpu_ptr].icount[0] - newicount;

        /* if so, set the new icount and compute the amount of "lost" time */
        if (diff > 0) {
            cpudata[active_cpu_ptr].lost += diff;
            if (cpudata[active_cpu_ptr].burn != null) {
                (cpudata[active_cpu_ptr].burn).handler(diff);
                /* let the CPU burn the cycles */
            } else {
                cpudata[active_cpu_ptr].icount[0] = newicount;
                /* CPU doesn't care */
            }
        }
    }

    /**
     * allocate a new timer
     */
    public static timer_entry timer_new() {
        timer_entry timer;

        /* remove an empty entry */
        if (timer_free_head == null) {
            return null;
        }
        timer = timer_free_head;
        timer_free_head = timer.next;

        return timer;
    }

    /**
     * insert a new timer into the list at the appropriate location
     */
    public static void timer_list_insert(timer_entry timer) {
        double expire = timer.enabled != 0 ? timer.expire : TIME_NEVER;
        timer_entry t = null;
        timer_entry lt = null;

        /* loop over the timer list */
        for (t = timer_head; t != null; lt = t, t = t.next) {
            /* if the current list entry expires after us, we should be inserted before it */
 /* note that due to floating point rounding, we need to allow a bit of slop here */
 /* because two equal entries -- within rounding precision -- need to sort in */
 /* the order they were inserted into the list */
            if ((t.expire - expire) > TIME_IN_NSEC(1)) {
                /* link the new guy in before the current list entry */
                timer.prev = t.prev;
                timer.next = t;

                if (t.prev != null) {
                    t.prev.next = timer;
                } else {
                    timer_head = timer;
                }
                t.prev = timer;
                return;
            }
        }

        /* need to insert after the last one */
        if (lt != null) {
            lt.next = timer;
        } else {
            timer_head = timer;
        }
        timer.prev = lt;
        timer.next = null;
    }

    /**
     * remove a timer from the linked list
     */
    public static void timer_list_remove(timer_entry timer) {
        /* remove it from the list */
        if (timer.prev != null) {
            timer.prev.next = timer.next;
        } else {
            timer_head = timer.next;
        }
        if (timer.next != null) {
            timer.next.prev = timer.prev;
        }
    }

    /**
     * initialize the timer system
     */
    public static void timer_init() {
        /* keep a local copy of how many total CPU's */
        lastcpu_ptr = cpu_gettotalcpu() - 1;

        /* we need to wait until the first call to timer_cyclestorun before using real CPU times */
        base_time = 0.0;
        global_offset = 0.0;
        callback_timer = null;
        callback_timer_modified = 0;

        /* reset the timers */
        for (int x = 0; x < timers.length; x++) {
            timers[x] = new timer_entry();
        }

        /* initialize the lists */
        timer_head = null;
        timer_free_head = timers[0];
        for (int i = 0; i < MAX_TIMERS - 1; i++) {
            timers[i].next = timers[i + 1];
        }

        /* reset the CPU timers */
        for (int i = 0; i < cpudata.length; i++) {
            cpudata[i] = new cpu_entry();
        }
        active_cpu_ptr = -1;
        last_active_cpu_ptr = lastcpu_ptr;

        /* compute the cycle times */
        for (int i = 0; i <= lastcpu_ptr; i++) // for (cpu = cpudata, i = 0; cpu <= lastcpu; cpu++, i++)
        {
            /* make a pointer to this CPU's interface functions */
            cpudata[i].icount = cpuintf[Machine.drv.cpu[i].cpu_type & ~CPU_FLAGS_MASK].icount;
            cpudata[i].burn = cpuintf[Machine.drv.cpu[i].cpu_type & ~CPU_FLAGS_MASK].burn;

            /* get the CPU's overclocking factor */
            cpudata[i].overclock = cpuintf[Machine.drv.cpu[i].cpu_type & ~CPU_FLAGS_MASK].overclock;

            /* everyone is active but suspended by the reset line until further notice */
            cpudata[i].suspended = SUSPEND_REASON_RESET;

            /* set the CPU index */
            cpudata[i].index = i;

            /* compute the cycle times */
            cpudata[i].sec_to_cycles
                    = sec_to_cycles[i] = cpudata[i].overclock * Machine.drv.cpu[i].cpu_clock;
            cpudata[i].cycles_to_sec = cycles_to_sec[i] = 1.0 / sec_to_cycles[i];
        }
    }

    /**
     * get overclocking factor for a CPU
     */
    public static double timer_get_overclock(int cpunum) {
        cpu_entry cpu = cpudata[cpunum];
        return cpu.overclock;
    }

    /**
     * set overclocking factor for a CPU
     */
    public static void timer_set_overclock(int cpunum, double overclock) {
        cpu_entry cpu = cpudata[cpunum];
        cpu.overclock = overclock;
        cpu.sec_to_cycles = sec_to_cycles[cpunum] = cpu.overclock * Machine.drv.cpu[cpunum].cpu_clock;
        cpu.cycles_to_sec = cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];
    }

    /**
     * allocate a pulse timer, which repeatedly calls the callback using the
     * given period
     */
    public static timer_entry timer_pulse(double period, int param, TimerCallbackHandlerPtr callback) {
        double time = getabsolutetime();
        timer_entry timer;

        /* allocate a new entry */
        timer = timer_new();
        if (timer == null) {
            return null;
        }

        /* fill in the record */
        timer.callback = callback;
        timer.callback_param = param;
        timer.enabled = 1;
        timer.period = period;

        /* compute the time of the next firing and insert into the list */
        timer.start = time;
        timer.expire = time + period;
        timer_list_insert(timer);

        /* if we're supposed to fire before the end of this cycle, adjust the counter */
        if (active_cpu_ptr != -1 && timer.expire < base_time) {
            timer_adjust(timer, time, period);
        }
        /*TODO*/
        //
        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: New pulse=%08X, period=%.6g\n", time + global_offset, timer, period);
        /*TODO*/
        //	#endif
        /*TODO*/
        //
        /* return a handle */
        return timer;
    }

    /**
     * allocate a one-shot timer, which calls the callback after the given
     * duration
     */
    public static timer_entry timer_set(double duration, int param, TimerCallbackHandlerPtr callback) {
        double time = getabsolutetime();
        timer_entry timer;

        /* allocate a new entry */
        timer = timer_new();
        if (timer == null) {
            return null;
        }

        /* fill in the record */
        timer.callback = callback;
        timer.callback_param = param;
        timer.enabled = 1;
        timer.period = 0;

        /* compute the time of the next firing and insert into the list */
        timer.start = time;
        timer.expire = time + duration;
        timer_list_insert(timer);

        /* if we're supposed to fire before the end of this cycle, adjust the counter */
        if (active_cpu_ptr != -1 && timer.expire < base_time) {
            timer_adjust(timer, time, duration);
        }

        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: New oneshot=%08X, duration=%.6g\n", time + global_offset, timer,
        // duration);
        /*TODO*/
        //	#endif

        /* return a handle */
        return timer;
    }

    /**
     * reset the timing on a timer
     */
    public static void timer_reset(Object which, double duration) {
        double time = getabsolutetime();
        timer_entry timer = (timer_entry) which;

        /* compute the time of the next firing */
        timer.start = time;
        timer.expire = time + duration;

        /* remove the timer and insert back into the list */
        timer_list_remove(timer);
        timer_list_insert(timer);

        /* if we're supposed to fire before the end of this cycle, adjust the counter */
        if (active_cpu_ptr != -1 && timer.expire < base_time) {
            timer_adjust(timer, time, duration);
        }

        /* if this is the callback timer, mark it modified */
        if (timer == callback_timer) {
            callback_timer_modified = 1;
        }

        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: Reset %08X, duration=%.6g\n", time + global_offset, timer, duration);
        /*TODO*/
        //	#endif
    }

    /**
     * remove a timer from the system
     */
    public static void timer_remove(Object which) {
        try {
            timer_entry timer = (timer_entry) which;

            /* remove it from the list */
            timer_list_remove(timer);

            /* free it up by adding it back to the free list */
            timer.next = timer_free_head;
            timer_free_head = timer;

            /*TODO*/
            //	#if VERBOSE
            /*TODO*/
            //		verbose_print("T=%.6g: Removed %08X\n", getabsolutetime() + global_offset, timer);
            /*TODO*/
            //	#endif
        } catch (Exception e) {

        }
    }

    /**
     * enable/disable a timer
     */
    public static int timer_enable(Object which, int enable) {
        timer_entry timer = (timer_entry) which;
        int old;
        /*TODO*/
        //
        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		if (enable) verbose_print("T=%.6g: Enabled %08X\n", getabsolutetime() + global_offset,
        // timer);
        /*TODO*/
        //		else verbose_print("T=%.6g: Disabled %08X\n", getabsolutetime() + global_offset, timer);
        /*TODO*/
        //	#endif

        /* set the enable flag */
        old = timer.enabled;
        timer.enabled = enable;

        /* remove the timer and insert back into the list */
        timer_list_remove(timer);
        timer_list_insert(timer);

        return old;
    }

    /**
     * return the time since the last trigger
     */
    public static double timer_timeelapsed(Object which) {
        double time = getabsolutetime();
        timer_entry timer = (timer_entry) which;

        return time - timer.start;
    }

    /**
     * return the time until the next trigger
     */
    public static double timer_timeleft(Object which) {
        double time = getabsolutetime();
        timer_entry timer = (timer_entry) which;

        return timer.expire - time;
    }

    /**
     * return the current time
     */
    public static double timer_get_time() {
        return global_offset + getabsolutetime();
    }

    /**
     * return the time when this timer started counting
     */
    public static double timer_starttime(Object which) {
        timer_entry timer = (timer_entry) which;
        return global_offset + timer.start;
    }

    /**
     * return the time when this timer will fire next
     */
    public static double timer_firetime(Object which) {
        timer_entry timer = (timer_entry) which;
        return global_offset + timer.expire;
    }

    /**
     * begin CPU execution by determining how many cycles the CPU should run
     */
    public static int timer_schedule_cpu(int[] cpu, int[] cycles) {
        double end;

        /* then see if there are any CPUs that aren't suspended and haven't yet been updated */
        if (pick_cpu(cpu, cycles, timer_head.expire) != 0) {
            return 1;
        }

        /* everyone is up-to-date; expire any timers now */
        end = timer_head.expire;
        while (timer_head.expire <= end) {
            timer_entry timer = timer_head;

            /* the base time is now the time of the timer */
            base_time = timer.expire;

            /*TODO*/
            //		#if VERBOSE
            /*TODO*/
            //			verbose_print("T=%.6g: %08X fired (exp time=%.6g)\n", getabsolutetime() + global_offset,
            // timer, timer->expire + global_offset);
            /*TODO*/
            //		#endif

            /* set the global state of which callback we're in */
            callback_timer_modified = 0;
            callback_timer = timer;

            /* call the callback */
            if (timer.callback != null) {
                (timer.callback).handler(timer.callback_param);
            }

            /* clear the callback timer global */
            callback_timer = null;

            /* reset or remove the timer, but only if it wasn't modified during the callback */
            if (callback_timer_modified == 0) {
                if (timer.period != 0.0) {
                    timer.start = timer.expire;
                    timer.expire += timer.period;

                    timer_list_remove(timer);
                    timer_list_insert(timer);
                } else {
                    timer_remove(timer);
                }
            }
        }

        /* reset scheduling so it starts with CPU 0 */
        last_active_cpu_ptr = lastcpu_ptr;

        /* go back to scheduling */
        return pick_cpu(cpu, cycles, timer_head.expire);
    }

    /**
     * end CPU execution by updating the number of cycles the CPU actually ran
     */
    public static void timer_update_cpu(int cpunum, int ran) {
        cpu_entry cpu = cpudata[cpunum];

        /* update the time if we haven't been suspended */
        if (cpu.suspended == 0) {
            cpu.time += (double) (ran - cpu.lost) * cpu.cycles_to_sec;
            cpu.lost = 0;
        }

        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: CPU %d finished (net=%d)\n", cpu->time + global_offset, cpunum, ran -
        // cpu->lost);
        /*TODO*/
        //	#endif

        /* time to renormalize? */
        if (cpu.time >= 1.0) {
            timer_entry timer;
            double one = 1.0;
            int c; // cpu_entry c;
            /*TODO*/
            //
            /*TODO*/
            //		#if VERBOSE
            /*TODO*/
            //			verbose_print("T=%.6g: Renormalizing\n", cpu->time + global_offset);
            /*TODO*/
            //		#endif

            /* renormalize all the CPU timers */
            for (c = 0; c <= lastcpu_ptr; c++) // for (c = cpudata; c <= lastcpu; c++)
            {
                cpudata[c].time -= one;
            }

            /* renormalize all the timers' times */
            for (timer = timer_head; timer != null; timer = timer.next) {
                timer.start -= one;
                timer.expire -= one;
            }

            /* renormalize the global timers */
            global_offset += one;
        }

        /* now stop counting cycles */
        base_time = cpu.time;
        active_cpu_ptr = -1;
    }

    /**
     * suspend a CPU but continue to count time for it
     */
    public static void timer_suspendcpu(int cpunum, int suspend, int reason) {
        cpu_entry cpu = cpudata[cpunum];
        int nocount = cpu.nocount;
        int old = cpu.suspended;

        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		if (suspend) verbose_print("T=%.6g: Suspending CPU %d\n", getabsolutetime() + global_offset,
        // cpunum);
        /*TODO*/
        //		else verbose_print("T=%.6g: Resuming CPU %d\n", getabsolutetime() + global_offset, cpunum);
        /*TODO*/
        //	#endif

        /* mark the CPU */
        if (suspend != 0) {
            cpu.suspended |= reason;
        } else {
            cpu.suspended &= ~reason;
        }
        cpu.nocount = 0;

        /* if this is the active CPU and we're halting, stop immediately */
        if (active_cpu_ptr != -1
                && cpunum == active_cpu_ptr
                && old == 0
                && cpu.suspended != 0) // if (active_cpu && cpu == active_cpu && !old && cpu->suspended)
        {
            /*TODO*/
            //		#if VERBOSE
            /*TODO*/
            //			verbose_print("T=%.6g: Reset ICount\n", getabsolutetime() + global_offset);
            /*TODO*/
            //		#endif

            /* set the CPU's time to the current time */
            cpu.time = base_time = getabsolutetime();
            /* ASG 990225 - also set base_time */
            cpu.lost = 0;

            /* no more instructions */
            if (cpu.burn != null) {
                (cpu.burn).handler(cpu.icount[0]);
                /* let the CPU burn the cycles */
            } else {
                cpu.icount[0] = 0;
                /* CPU doesn't care */
            }
        } /* else if we're unsuspending a CPU, reset its time */ else if (old != 0
                && cpu.suspended == 0
                && nocount == 0) {
            double time = getabsolutetime();

            /* only update the time if it's later than the CPU's time */
            if (time > cpu.time) {
                cpu.time = time;
            }
            cpu.lost = 0;
            /*TODO*/
            //
            /*TODO*/
            //		#if VERBOSE
            /*TODO*/
            //			verbose_print("T=%.6g: Resume time\n", cpu->time + global_offset);
            /*TODO*/
            //		#endif
        }
    }

    /**
     * hold a CPU and don't count time for it
     */
    public static void timer_holdcpu(int cpunum, int hold, int reason) {
        cpu_entry cpu = cpudata[cpunum];

        /* same as suspend */
        timer_suspendcpu(cpunum, hold, reason);

        /* except that we don't count time */
        if (hold != 0) {
            cpu.nocount = 1;
        }
    }

    /**
     * query if a CPU is suspended or not
     */
    public static int timer_iscpususpended(int cpunum, int reason) {
        cpu_entry cpu = cpudata[cpunum];
        return ((cpu.suspended & reason) != 0 && cpu.nocount == 0) ? 1 : 0;
    }

    /**
     * query if a CPU is held or not
     */
    public static int timer_iscpuheld(int cpunum, int reason) {
        cpu_entry cpu = cpudata[cpunum];
        return ((cpu.suspended & reason) != 0 && cpu.nocount != 0) ? 1 : 0;
    }

    /**
     * suspend a CPU until a specified trigger condition is met
     */
    public static void timer_suspendcpu_trigger(int cpunum, int trigger) {
        cpu_entry cpu = cpudata[cpunum];

        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: CPU %d suspended until %d\n", getabsolutetime() + global_offset,
        // cpunum, trigger);
        /*TODO*/
        //	#endif
        /*TODO*/
        //
        /* suspend the CPU immediately if it's not already */
        timer_suspendcpu(cpunum, 1, SUSPEND_REASON_TRIGGER);

        /* set the trigger */
        cpu.trigger = trigger;
    }

    /**
     * hold a CPU and don't count time for it
     */
    public static void timer_holdcpu_trigger(int cpunum, int trigger) {
        cpu_entry cpu = cpudata[cpunum];
        /*TODO*/
        //
        /*TODO*/
        //	#if VERBOSE
        /*TODO*/
        //		verbose_print("T=%.6g: CPU %d held until %d\n", getabsolutetime() + global_offset, cpunum,
        // trigger);
        /*TODO*/
        //	#endif

        /* suspend the CPU immediately if it's not already */
        timer_holdcpu(cpunum, 1, SUSPEND_REASON_TRIGGER);

        /* set the trigger */
        cpu.trigger = trigger;
    }

    /**
     * generates a trigger to unsuspend any CPUs waiting for it
     */
    public static void timer_trigger(int trigger) {
        int cpu; // cpu_entry *cpu;

        /* cause an immediate resynchronization */
        if (active_cpu_ptr != -1) {
            int left = cpudata[active_cpu_ptr].icount[0];
            if (left > 0) {
                cpudata[active_cpu_ptr].lost += left;
                if (cpudata[active_cpu_ptr].burn != null) {
                    (cpudata[active_cpu_ptr].burn).handler(left);
                    /* let the CPU burn the cycles */
                } else {
                    cpudata[active_cpu_ptr].icount[0] = 0;
                    /* CPU doesn't care */
                }
            }
        }

        /* look for suspended CPUs waiting for this trigger and unsuspend them */
        for (cpu = 0; cpu <= lastcpu_ptr; cpu++) // for (cpu = cpudata; cpu <= lastcpu; cpu++)
        {
            if (cpudata[cpu].suspended != 0 && cpudata[cpu].trigger == trigger) {
                /*TODO*/
                //			#if VERBOSE
                /*TODO*/
                //				verbose_print("T=%.6g: CPU %d triggered\n", getabsolutetime() + global_offset,
                // cpu->index);
                /*TODO*/
                //			#endif

                timer_suspendcpu(cpudata[cpu].index, 0, SUSPEND_REASON_TRIGGER);
                cpudata[cpu].trigger = 0;
            }
        }
    }

    /**
     * pick the next CPU to run
     */
    public static int pick_cpu(int[] cpunum, int[] cycles, double end) {
        int cpu = last_active_cpu_ptr; // cpu_entry *cpu = last_active_cpu;

        /* look for a CPU that isn't suspended and hasn't run its full timeslice yet */
        do {
            /* wrap around */
            cpu += 1;
            if (cpu > lastcpu_ptr) {
                cpu = 0;
            }

            /* if this CPU is suspended, just bump its time */
            if (cpudata[cpu].suspended != 0) {
                /* ASG 990225 - defer this update until the slice has finished */
 /*			if (!cpu->nocount)
        {
        	cpu->time = end;
        	cpu->lost = 0;
        }*/
            } /* if this CPU isn't suspended and has time left.... */ else if (cpudata[cpu].time < end) {
                /* mark the CPU active, and remember the CPU number locally */
                active_cpu_ptr = last_active_cpu_ptr = cpu;

                /* return the number of cycles to execute and the CPU number */
                cpunum[0] = cpudata[cpu].index;
                cycles[0] = (int) ((double) (end - cpudata[cpu].time) * cpudata[cpu].sec_to_cycles);

                if (cycles[0] > 0) {
                    /*TODO*/
                    //				#if VERBOSE
                    /*TODO*/
                    //					verbose_print("T=%.6g: CPU %d runs %d cycles\n", cpu->time + global_offset,
                    // *cpunum, *cycles);
                    /*TODO*/
                    //				#endif

                    /* remember the base time for this CPU */
                    base_time = cpudata[cpu].time + ((double) cycles[0] * cpudata[cpu].cycles_to_sec);

                    /* success */
                    return 1;
                }
            }
        } while (cpu != last_active_cpu_ptr);

        /* ASG 990225 - bump all suspended CPU times after the slice has finished */
        for (cpu = 0; cpu <= lastcpu_ptr; cpu++) // for (cpu = cpudata; cpu <= lastcpu; cpu++)
        {
            if (cpudata[cpu].suspended != 0 && cpudata[cpu].nocount == 0) {
                cpudata[cpu].time = end;
                cpudata[cpu].lost = 0;
            }
        }

        /* failure */
        return 0;
    }
}
