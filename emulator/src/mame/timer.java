package mame;

import static mame.driverH.*;
import static mame.timerH.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.mame.*;

public class timer {
    public static final int MAX_TIMERS =256;
    
    
    /*
     *		internal timer structures
     */
    public static class timer_entry
    {
        public timer_entry next;
        public timer_entry prev;
    /*TODO*///	void (*callback)(int);
        public int callback_param;
        public int enabled;
        public double period;
        public double start;
        public double expire;
    } 
    
    public static class cpu_entry
    {
    	public int[] icount;
        public burnPtr burn;
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

    static int lastcpu;//static cpu_entry *lastcpu;
    static int activecpu;//static cpu_entry *activecpu;
    static int last_activecpu;//static cpu_entry *last_activecpu;
    
    /* list of active timers */
    static timer_entry[] timers = new timer_entry[MAX_TIMERS];
    static timer_entry timer_head;
    static timer_entry timer_free_head;

    /* other internal states */
    static double base_time;
    static double global_offset;
    static timer_entry callback_timer;
    static int callback_timer_modified;

    /*TODO*////* prototypes */
    /*TODO*///static int pick_cpu(int *cpu, int *cycles, double expire);
    /*TODO*///
    /*TODO*///#if VERBOSE
    /*TODO*///static void verbose_print(char *s, ...);
    /*TODO*///#endif
    /*
    *	return the current absolute time
    */
    public static double getabsolutetime()
    {
        if (activecpu > 0 && (cpudata[activecpu].icount[0] + cpudata[activecpu].lost) > 0)
            return base_time - ((double)(cpudata[activecpu].icount[0] + cpudata[activecpu].lost) * cpudata[activecpu].cycles_to_sec);
       else
            return base_time;
   }
    
    
    /*
     *		adjust the current CPU's timer so that a new event will fire at the right time
     */
    public static void timer_adjust(timer_entry timer, double time, double period)
    {
    	int newicount, diff;
    
    	/* compute a new icount for the current CPU */
    	if (period == TIME_NOW)
    		newicount = 0;
    	else
            newicount = (int)((timer.expire - time) * cpudata[activecpu].sec_to_cycles) + 1;
    
    	/* determine if we're scheduled to run more cycles */
        diff = cpudata[activecpu].icount[0] - newicount;
    
    	/* if so, set the new icount and compute the amount of "lost" time */
        if (diff > 0)
        {
               cpudata[activecpu].lost += diff;
               if (cpudata[activecpu].burn != null)
                   cpudata[activecpu].burn.handler(diff);  /* let the CPU burn the cycles */
               else
                   cpudata[activecpu].icount[0] = newicount;  /* CPU doesn't care */
        }
    }
    
    
    /*
     *		allocate a new timer
     */
    public static timer_entry timer_new()
    {
    	timer_entry timer;
    
    	/* remove an empty entry */
    	if (timer_free_head==null)
    		return null;
    	timer = timer_free_head;
    	timer_free_head = timer.next;
    
    	return timer;
    }
    
    
    /*TODO*////*
    /*TODO*/// *		insert a new timer into the list at the appropriate location
    /*TODO*/// */
    /*TODO*///INLINE void timer_list_insert(timer_entry *timer)
    /*TODO*///{
    /*TODO*///	double expire = timer->enabled ? timer->expire : TIME_NEVER;
    /*TODO*///	timer_entry *t, *lt = NULL;
    /*TODO*///
    /*TODO*///	/* loop over the timer list */
    /*TODO*///	for (t = timer_head; t; lt = t, t = t->next)
    /*TODO*///	{
    /*TODO*///		/* if the current list entry expires after us, we should be inserted before it */
    /*TODO*///		/* note that due to floating point rounding, we need to allow a bit of slop here */
    /*TODO*///		/* because two equal entries -- within rounding precision -- need to sort in */
    /*TODO*///		/* the order they were inserted into the list */
    /*TODO*///		if ((t->expire - expire) > TIME_IN_NSEC(1))
    /*TODO*///		{
    /*TODO*///			/* link the new guy in before the current list entry */
    /*TODO*///			timer->prev = t->prev;
    /*TODO*///			timer->next = t;
    /*TODO*///
    /*TODO*///			if (t->prev)
    /*TODO*///				t->prev->next = timer;
    /*TODO*///			else
    /*TODO*///				timer_head = timer;
    /*TODO*///			t->prev = timer;
    /*TODO*///			return;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* need to insert after the last one */
    /*TODO*///	if (lt)
    /*TODO*///		lt->next = timer;
    /*TODO*///	else
    /*TODO*///		timer_head = timer;
    /*TODO*///	timer->prev = lt;
    /*TODO*///	timer->next = NULL;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		remove a timer from the linked list
    /*TODO*/// */
    /*TODO*///INLINE void timer_list_remove(timer_entry *timer)
    /*TODO*///{
    /*TODO*///	/* remove it from the list */
    /*TODO*///	if (timer->prev)
    /*TODO*///		timer->prev->next = timer->next;
    /*TODO*///	else
    /*TODO*///		timer_head = timer->next;
    /*TODO*///	if (timer->next)
    /*TODO*///		timer->next->prev = timer->prev;
    /*TODO*///}

    /*
     *		initialize the timer system
     */
    public static void timer_init()
    {
    	/* keep a local copy of how many total CPU's */
    	lastcpu = cpu_gettotalcpu() - 1;
    
    	/* we need to wait until the first call to timer_cyclestorun before using real CPU times */
    	base_time = 0.0;
    	global_offset = 0.0;
    	callback_timer = null;
    	callback_timer_modified = 0;
    
    	/* reset the timers */
    	//memset(timers, 0, sizeof(timers));
        for(int x=0; x<timers.length; x++)
        {
            timers[x]=new timer_entry();
        }
    
    	/* initialize the lists */
        timer_head = null;
        timer_free_head = timers[0];
        for (int i = 0; i < MAX_TIMERS - 1; i++)
               timers[i].next = timers[i + 1];

    	/* reset the CPU timers */
        for (int i = 0; i < cpudata.length; i++)
             cpudata[i] = new cpu_entry();
        
        activecpu = -1; //activecpu = NULL;
        last_activecpu = lastcpu;

    	/* compute the cycle times */
    	for (int i = 0; i <= lastcpu; i++)//for (cpu = cpudata, i = 0; cpu <= lastcpu; cpu++, i++)
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
                cpudata[i].sec_to_cycles = sec_to_cycles[i] = cpudata[i].overclock * Machine.drv.cpu[i].cpu_clock;
                cpudata[i].cycles_to_sec = cycles_to_sec[i] = 1.0 / sec_to_cycles[i];

    	}    
    }
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		get overclocking factor for a CPU
    /*TODO*/// */
    /*TODO*///double timer_get_overclock(int cpunum)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = &cpudata[cpunum];
    /*TODO*///	return cpu->overclock;
    /*TODO*///}
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		set overclocking factor for a CPU
    /*TODO*/// */
    /*TODO*///void timer_set_overclock(int cpunum, double overclock)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = &cpudata[cpunum];
    /*TODO*///	cpu->overclock = overclock;
    /*TODO*///	cpu->sec_to_cycles = sec_to_cycles[cpunum] = cpu->overclock * Machine->drv->cpu[cpunum].cpu_clock;
    /*TODO*///	cpu->cycles_to_sec = cycles_to_sec[cpunum] = 1.0 / sec_to_cycles[cpunum];
    /*TODO*///}
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		allocate a pulse timer, which repeatedly calls the callback using the given period
    /*TODO*/// */
    /*TODO*///void *timer_pulse(double period, int param, void (*callback)(int))
    /*TODO*///{
    /*TODO*///	double time = getabsolutetime();
    /*TODO*///	timer_entry *timer;
    /*TODO*///
    /*TODO*///	/* allocate a new entry */
    /*TODO*///	timer = timer_new();
    /*TODO*///	if (!timer)
    /*TODO*///		return NULL;
    /*TODO*///
    /*TODO*///	/* fill in the record */
    /*TODO*///	timer->callback = callback;
    /*TODO*///	timer->callback_param = param;
    /*TODO*///	timer->enabled = 1;
    /*TODO*///	timer->period = period;
    /*TODO*///
    /*TODO*///	/* compute the time of the next firing and insert into the list */
    /*TODO*///	timer->start = time;
    /*TODO*///	timer->expire = time + period;
    /*TODO*///	timer_list_insert(timer);
    /*TODO*///
    /*TODO*///	/* if we're supposed to fire before the end of this cycle, adjust the counter */
    /*TODO*///	if (activecpu && timer->expire < base_time)
    /*TODO*///		timer_adjust(timer, time, period);
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: New pulse=%08X, period=%.6g\n", time + global_offset, timer, period);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* return a handle */
    /*TODO*///	return timer;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		allocate a one-shot timer, which calls the callback after the given duration
    /*TODO*/// */
    /*TODO*///void *timer_set(double duration, int param, void (*callback)(int))
    /*TODO*///{
    /*TODO*///	double time = getabsolutetime();
    /*TODO*///	timer_entry *timer;
    /*TODO*///
    /*TODO*///	/* allocate a new entry */
    /*TODO*///	timer = timer_new();
    /*TODO*///	if (!timer)
    /*TODO*///		return NULL;
    /*TODO*///
    /*TODO*///	/* fill in the record */
    /*TODO*///	timer->callback = callback;
    /*TODO*///	timer->callback_param = param;
    /*TODO*///	timer->enabled = 1;
    /*TODO*///	timer->period = 0;
    /*TODO*///
    /*TODO*///	/* compute the time of the next firing and insert into the list */
    /*TODO*///	timer->start = time;
    /*TODO*///	timer->expire = time + duration;
    /*TODO*///	timer_list_insert(timer);
    /*TODO*///
    /*TODO*///	/* if we're supposed to fire before the end of this cycle, adjust the counter */
    /*TODO*///	if (activecpu && timer->expire < base_time)
    /*TODO*///		timer_adjust(timer, time, duration);
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: New oneshot=%08X, duration=%.6g\n", time + global_offset, timer, duration);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* return a handle */
    /*TODO*///	return timer;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		reset the timing on a timer
    /*TODO*/// */
    /*TODO*///void timer_reset(void *which, double duration)
    /*TODO*///{
    /*TODO*///	double time = getabsolutetime();
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///
    /*TODO*///	/* compute the time of the next firing */
    /*TODO*///	timer->start = time;
    /*TODO*///	timer->expire = time + duration;
    /*TODO*///
    /*TODO*///	/* remove the timer and insert back into the list */
    /*TODO*///	timer_list_remove(timer);
    /*TODO*///	timer_list_insert(timer);
    /*TODO*///
    /*TODO*///	/* if we're supposed to fire before the end of this cycle, adjust the counter */
    /*TODO*///	if (activecpu && timer->expire < base_time)
    /*TODO*///		timer_adjust(timer, time, duration);
    /*TODO*///
    /*TODO*///	/* if this is the callback timer, mark it modified */
    /*TODO*///	if (timer == callback_timer)
    /*TODO*///		callback_timer_modified = 1;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: Reset %08X, duration=%.6g\n", time + global_offset, timer, duration);
    /*TODO*///	#endif
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		remove a timer from the system
    /*TODO*/// */
    /*TODO*///void timer_remove(void *which)
    /*TODO*///{
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///
    /*TODO*///	/* remove it from the list */
    /*TODO*///	timer_list_remove(timer);
    /*TODO*///
    /*TODO*///	/* free it up by adding it back to the free list */
    /*TODO*///	timer->next = timer_free_head;
    /*TODO*///	timer_free_head = timer;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: Removed %08X\n", getabsolutetime() + global_offset, timer);
    /*TODO*///	#endif
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		enable/disable a timer
    /*TODO*/// */
    /*TODO*///int timer_enable(void *which, int enable)
    /*TODO*///{
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///	int old;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		if (enable) verbose_print("T=%.6g: Enabled %08X\n", getabsolutetime() + global_offset, timer);
    /*TODO*///		else verbose_print("T=%.6g: Disabled %08X\n", getabsolutetime() + global_offset, timer);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* set the enable flag */
    /*TODO*///	old = timer->enabled;
    /*TODO*///	timer->enabled = enable;
    /*TODO*///
    /*TODO*///	/* remove the timer and insert back into the list */
    /*TODO*///	timer_list_remove(timer);
    /*TODO*///	timer_list_insert(timer);
    /*TODO*///
    /*TODO*///	return old;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		return the time since the last trigger
    /*TODO*/// */
    /*TODO*///double timer_timeelapsed(void *which)
    /*TODO*///{
    /*TODO*///	double time = getabsolutetime();
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///
    /*TODO*///	return time - timer->start;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		return the time until the next trigger
    /*TODO*/// */
    /*TODO*///double timer_timeleft(void *which)
    /*TODO*///{
    /*TODO*///	double time = getabsolutetime();
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///
    /*TODO*///	return timer->expire - time;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		return the current time
    /*TODO*/// */
    /*TODO*///double timer_get_time(void)
    /*TODO*///{
    /*TODO*///	return global_offset + getabsolutetime();
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		return the time when this timer started counting
    /*TODO*/// */
    /*TODO*///double timer_starttime(void *which)
    /*TODO*///{
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///	return global_offset + timer->start;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		return the time when this timer will fire next
    /*TODO*/// */
    /*TODO*///double timer_firetime(void *which)
    /*TODO*///{
    /*TODO*///	timer_entry *timer = which;
    /*TODO*///	return global_offset + timer->expire;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		begin CPU execution by determining how many cycles the CPU should run
    /*TODO*/// */
    /*TODO*///int timer_schedule_cpu(int *cpu, int *cycles)
    /*TODO*///{
    /*TODO*///	double end;
    /*TODO*///
    /*TODO*///	/* then see if there are any CPUs that aren't suspended and haven't yet been updated */
    /*TODO*///	if (pick_cpu(cpu, cycles, timer_head->expire))
    /*TODO*///		return 1;
    /*TODO*///
    /*TODO*///	/* everyone is up-to-date; expire any timers now */
    /*TODO*///	end = timer_head->expire;
    /*TODO*///	while (timer_head->expire <= end)
    /*TODO*///	{
    /*TODO*///		timer_entry *timer = timer_head;
    /*TODO*///
    /*TODO*///		/* the base time is now the time of the timer */
    /*TODO*///		base_time = timer->expire;
    /*TODO*///
    /*TODO*///		#if VERBOSE
    /*TODO*///			verbose_print("T=%.6g: %08X fired (exp time=%.6g)\n", getabsolutetime() + global_offset, timer, timer->expire + global_offset);
    /*TODO*///		#endif
    /*TODO*///
    /*TODO*///		/* set the global state of which callback we're in */
    /*TODO*///		callback_timer_modified = 0;
    /*TODO*///		callback_timer = timer;
    /*TODO*///
    /*TODO*///		/* call the callback */
    /*TODO*///		if (timer->callback)
    /*TODO*///		{
    /*TODO*///			profiler_mark(PROFILER_TIMER_CALLBACK);
    /*TODO*///			(*timer->callback)(timer->callback_param);
    /*TODO*///			profiler_mark(PROFILER_END);
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* clear the callback timer global */
    /*TODO*///		callback_timer = NULL;
    /*TODO*///
    /*TODO*///		/* reset or remove the timer, but only if it wasn't modified during the callback */
    /*TODO*///		if (!callback_timer_modified)
    /*TODO*///		{
    /*TODO*///			if (timer->period)
    /*TODO*///			{
    /*TODO*///				timer->start = timer->expire;
    /*TODO*///				timer->expire += timer->period;
    /*TODO*///
    /*TODO*///				timer_list_remove(timer);
    /*TODO*///				timer_list_insert(timer);
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///				timer_remove(timer);
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* reset scheduling so it starts with CPU 0 */
    /*TODO*///	last_activecpu = lastcpu;
    /*TODO*///
    /*TODO*///#ifdef MAME_DEBUG
    /*TODO*///{
    /*TODO*///	extern int debug_key_delay;
    /*TODO*///	debug_key_delay = 0x7ffe;
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///	/* go back to scheduling */
    /*TODO*///	return pick_cpu(cpu, cycles, timer_head->expire);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		end CPU execution by updating the number of cycles the CPU actually ran
    /*TODO*/// */
    /*TODO*///void timer_update_cpu(int cpunum, int ran)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///
    /*TODO*///	/* update the time if we haven't been suspended */
    /*TODO*///	if (!cpu->suspended)
    /*TODO*///	{
    /*TODO*///		cpu->time += (double)(ran - cpu->lost) * cpu->cycles_to_sec;
    /*TODO*///		cpu->lost = 0;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: CPU %d finished (net=%d)\n", cpu->time + global_offset, cpunum, ran - cpu->lost);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* time to renormalize? */
    /*TODO*///	if (cpu->time >= 1.0)
    /*TODO*///	{
    /*TODO*///		timer_entry *timer;
    /*TODO*///		double one = 1.0;
    /*TODO*///		cpu_entry *c;
    /*TODO*///
    /*TODO*///		#if VERBOSE
    /*TODO*///			verbose_print("T=%.6g: Renormalizing\n", cpu->time + global_offset);
    /*TODO*///		#endif
    /*TODO*///
    /*TODO*///		/* renormalize all the CPU timers */
    /*TODO*///		for (c = cpudata; c <= lastcpu; c++)
    /*TODO*///			c->time -= one;
    /*TODO*///
    /*TODO*///		/* renormalize all the timers' times */
    /*TODO*///		for (timer = timer_head; timer; timer = timer->next)
    /*TODO*///		{
    /*TODO*///			timer->start -= one;
    /*TODO*///			timer->expire -= one;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* renormalize the global timers */
    /*TODO*///		global_offset += one;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* now stop counting cycles */
    /*TODO*///	base_time = cpu->time;
    /*TODO*///	activecpu = NULL;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		suspend a CPU but continue to count time for it
    /*TODO*/// */
    /*TODO*///void timer_suspendcpu(int cpunum, int suspend, int reason)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///	int nocount = cpu->nocount;
    /*TODO*///	int old = cpu->suspended;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		if (suspend) verbose_print("T=%.6g: Suspending CPU %d\n", getabsolutetime() + global_offset, cpunum);
    /*TODO*///		else verbose_print("T=%.6g: Resuming CPU %d\n", getabsolutetime() + global_offset, cpunum);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* mark the CPU */
    /*TODO*///	if (suspend)
    /*TODO*///		cpu->suspended |= reason;
    /*TODO*///	else
    /*TODO*///		cpu->suspended &= ~reason;
    /*TODO*///	cpu->nocount = 0;
    /*TODO*///
    /*TODO*///	/* if this is the active CPU and we're halting, stop immediately */
    /*TODO*///	if (activecpu && cpu == activecpu && !old && cpu->suspended)
    /*TODO*///	{
    /*TODO*///		#if VERBOSE
    /*TODO*///			verbose_print("T=%.6g: Reset ICount\n", getabsolutetime() + global_offset);
    /*TODO*///		#endif
    /*TODO*///
    /*TODO*///		/* set the CPU's time to the current time */
    /*TODO*///		cpu->time = base_time = getabsolutetime();	/* ASG 990225 - also set base_time */
    /*TODO*///		cpu->lost = 0;
    /*TODO*///
    /*TODO*///		/* no more instructions */
    /*TODO*///		if (cpu->burn)
    /*TODO*///			(*cpu->burn)(*cpu->icount); /* let the CPU burn the cycles */
    /*TODO*///		else
    /*TODO*///			*cpu->icount = 0;	/* CPU doesn't care */
    /*TODO*///    }
    /*TODO*///
    /*TODO*///	/* else if we're unsuspending a CPU, reset its time */
    /*TODO*///	else if (old && !cpu->suspended && !nocount)
    /*TODO*///	{
    /*TODO*///		double time = getabsolutetime();
    /*TODO*///
    /*TODO*///		/* only update the time if it's later than the CPU's time */
    /*TODO*///		if (time > cpu->time)
    /*TODO*///			cpu->time = time;
    /*TODO*///		cpu->lost = 0;
    /*TODO*///
    /*TODO*///		#if VERBOSE
    /*TODO*///			verbose_print("T=%.6g: Resume time\n", cpu->time + global_offset);
    /*TODO*///		#endif
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		hold a CPU and don't count time for it
    /*TODO*/// */
    /*TODO*///void timer_holdcpu(int cpunum, int hold, int reason)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///
    /*TODO*///	/* same as suspend */
    /*TODO*///	timer_suspendcpu(cpunum, hold, reason);
    /*TODO*///
    /*TODO*///	/* except that we don't count time */
    /*TODO*///	if (hold)
    /*TODO*///		cpu->nocount = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		query if a CPU is suspended or not
    /*TODO*/// */
    /*TODO*///int timer_iscpususpended(int cpunum, int reason)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///	return (cpu->suspended & reason) && !cpu->nocount;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		query if a CPU is held or not
    /*TODO*/// */
    /*TODO*///int timer_iscpuheld(int cpunum, int reason)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///	return (cpu->suspended & reason) && cpu->nocount;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		suspend a CPU until a specified trigger condition is met
    /*TODO*/// */
    /*TODO*///void timer_suspendcpu_trigger(int cpunum, int trigger)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: CPU %d suspended until %d\n", getabsolutetime() + global_offset, cpunum, trigger);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* suspend the CPU immediately if it's not already */
    /*TODO*///	timer_suspendcpu(cpunum, 1, SUSPEND_REASON_TRIGGER);
    /*TODO*///
    /*TODO*///	/* set the trigger */
    /*TODO*///	cpu->trigger = trigger;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		hold a CPU and don't count time for it
    /*TODO*/// */
    /*TODO*///void timer_holdcpu_trigger(int cpunum, int trigger)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = cpudata + cpunum;
    /*TODO*///
    /*TODO*///	#if VERBOSE
    /*TODO*///		verbose_print("T=%.6g: CPU %d held until %d\n", getabsolutetime() + global_offset, cpunum, trigger);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	/* suspend the CPU immediately if it's not already */
    /*TODO*///	timer_holdcpu(cpunum, 1, SUSPEND_REASON_TRIGGER);
    /*TODO*///
    /*TODO*///	/* set the trigger */
    /*TODO*///	cpu->trigger = trigger;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		generates a trigger to unsuspend any CPUs waiting for it
    /*TODO*/// */
    /*TODO*///void timer_trigger(int trigger)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu;
    /*TODO*///
    /*TODO*///	/* cause an immediate resynchronization */
    /*TODO*///	if (activecpu)
    /*TODO*///	{
    /*TODO*///		int left = *activecpu->icount;
    /*TODO*///		if (left > 0)
    /*TODO*///		{
    /*TODO*///			activecpu->lost += left;
    /*TODO*///			if (activecpu->burn)
    /*TODO*///				(*activecpu->burn)(left); /* let the CPU burn the cycles */
    /*TODO*///			else
    /*TODO*///				*activecpu->icount = 0; /* CPU doesn't care */
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* look for suspended CPUs waiting for this trigger and unsuspend them */
    /*TODO*///	for (cpu = cpudata; cpu <= lastcpu; cpu++)
    /*TODO*///	{
    /*TODO*///		if (cpu->suspended && cpu->trigger == trigger)
    /*TODO*///		{
    /*TODO*///			#if VERBOSE
    /*TODO*///				verbose_print("T=%.6g: CPU %d triggered\n", getabsolutetime() + global_offset, cpu->index);
    /*TODO*///			#endif
    /*TODO*///
    /*TODO*///			timer_suspendcpu(cpu->index, 0, SUSPEND_REASON_TRIGGER);
    /*TODO*///			cpu->trigger = 0;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		pick the next CPU to run
    /*TODO*/// */
    /*TODO*///static int pick_cpu(int *cpunum, int *cycles, double end)
    /*TODO*///{
    /*TODO*///	cpu_entry *cpu = last_activecpu;
    /*TODO*///
    /*TODO*///	/* look for a CPU that isn't suspended and hasn't run its full timeslice yet */
    /*TODO*///	do
    /*TODO*///	{
    /*TODO*///		/* wrap around */
    /*TODO*///		cpu += 1;
    /*TODO*///		if (cpu > lastcpu)
    /*TODO*///			cpu = cpudata;
    /*TODO*///
    /*TODO*///		/* if this CPU is suspended, just bump its time */
    /*TODO*///		if (cpu->suspended)
    /*TODO*///		{
    /*TODO*///			/* ASG 990225 - defer this update until the slice has finished */
    /*TODO*////*			if (!cpu->nocount)
    /*TODO*///			{
    /*TODO*///				cpu->time = end;
    /*TODO*///				cpu->lost = 0;
    /*TODO*///			}*/
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* if this CPU isn't suspended and has time left.... */
    /*TODO*///		else if (cpu->time < end)
    /*TODO*///		{
    /*TODO*///			/* mark the CPU active, and remember the CPU number locally */
    /*TODO*///			activecpu = last_activecpu = cpu;
    /*TODO*///
    /*TODO*///			/* return the number of cycles to execute and the CPU number */
    /*TODO*///			*cpunum = cpu->index;
    /*TODO*///			*cycles = (int)((double)(end - cpu->time) * cpu->sec_to_cycles);
    /*TODO*///
    /*TODO*///			if (*cycles > 0)
    /*TODO*///			{
    /*TODO*///				#if VERBOSE
    /*TODO*///					verbose_print("T=%.6g: CPU %d runs %d cycles\n", cpu->time + global_offset, *cpunum, *cycles);
    /*TODO*///				#endif
    /*TODO*///
    /*TODO*///				/* remember the base time for this CPU */
    /*TODO*///				base_time = cpu->time + ((double)*cycles * cpu->cycles_to_sec);
    /*TODO*///
    /*TODO*///				/* success */
    /*TODO*///				return 1;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	while (cpu != last_activecpu);
    /*TODO*///
    /*TODO*///	/* ASG 990225 - bump all suspended CPU times after the slice has finished */
    /*TODO*///	for (cpu = cpudata; cpu <= lastcpu; cpu++)
    /*TODO*///		if (cpu->suspended && !cpu->nocount)
    /*TODO*///		{
    /*TODO*///			cpu->time = end;
    /*TODO*///			cpu->lost = 0;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///	/* failure */
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////*
    /*TODO*/// *		debugging
    /*TODO*/// */
    /*TODO*///#if VERBOSE
    /*TODO*///
    /*TODO*///#ifdef macintosh
    /*TODO*///#undef printf
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///static void verbose_print(char *s, ...)
    /*TODO*///{
    /*TODO*///	va_list ap;
    /*TODO*///
    /*TODO*///	va_start(ap, s);
    /*TODO*///
    /*TODO*///	#if (VERBOSE == 1)
    /*TODO*///		if (errorlog) vfprintf(errorlog, s, ap);
    /*TODO*///	#else
    /*TODO*///		vprintf(s, ap);
    /*TODO*///		fflush(NULL);
    /*TODO*///	#endif
    /*TODO*///
    /*TODO*///	va_end(ap);
    /*TODO*///}
    /*TODO*///
    /*TODO*///#endif
    /*TODO*///    
}
