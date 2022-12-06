package gr.codebb.arcadeflex.v036.cpu.m68000;

import static arcadeflex.v036.mame.cpuintrfH.*;

public class m68kH {
    public static final int M68K_IRQ_1 = 1;
    public static final int M68K_IRQ_2 = 2;
    public static final int M68K_IRQ_3 = 3;
    public static final int M68K_IRQ_4 = 4;
    public static final int M68K_IRQ_5 = 5;
    public static final int M68K_IRQ_6 = 6;
    public static final int M68K_IRQ_7 = 7;
    
    public static final int M68K_INT_ACK_AUTOVECTOR = -1;
    public static final int M68K_INT_ACK_SPURIOUS = -2;
    
    public static final int M68K_CPU_MODE_68000 = 1;
    public static final int M68K_CPU_MODE_68010 = 2;
    public static final int M68K_CPU_MODE_68EC020 = 4;
    public static final int M68K_CPU_MODE_68020 = 8;
    
    public static class m68k_cpu_context {
        public int mode;                /* CPU Operation Mode: 68000, 68010, or 68020 */
        public long[] dr = new long[8];   /* Data Registers */
        public long[] ar = new long[8];   /* Address Registers */
        public long ppc;                 /* Previous program counter */
        public long pc;                  /* Program Counter */
        public long[] sp = new long[4];   /* User, Interrupt, and Master Stack Pointers */
        public long vbr;                 /* Vector Base Register (68010+) */
        public long sfc;                 /* Source Function Code Register (m68010+) */
        public long dfc;                 /* Destination Function Code Register (m68010+) */
        public long cacr;                /* Cache Control Register (m68020+) */
        public long caar;                /* Cache Address Register (m68020+) */
        public long ir;                  /* Instruction Register */
        public long t1_flag;             /* Trace 1 */
        public long t0_flag;             /* Trace 0 */
        public long s_flag;              /* Supervisor */
        public long m_flag;              /* Master/Interrupt state */
        public long x_flag;              /* Extend */
        public long n_flag;              /* Negative */
        public long not_z_flag;          /* Zero, inverted for speedups */
        public long v_flag;              /* Overflow */
        public long c_flag;              /* Carry */
        public long int_mask;            /* I0-I2 */
        public long int_state;           /* Current interrupt state -- ASG: changed from ints_pending */
        public long stopped;             /* Stopped state */
        public long halted;              /* Halted state */
        public int int_cycles;          /* ASG: extra cycles from generated interrupts */
        public long pref_addr;           /* Last prefetch address */
        public long pref_data;           /* Data in the prefetch queue */

        /* Callbacks to host */
        irqcallbacksPtr int_ack_callback;  /* Interrupt Acknowledge */
        bkpt_ack_callbackPtr bkpt_ack_callback;     /* Breakpoint Acknowledge */
        reset_instr_callbackPtr reset_instr_callback;   /* Called when a RESET instruction is encountered */
        pc_changed_callbackPtr pc_changed_callback; /* Called when the PC changes by a large amount */
        set_fc_callbackPtr set_fc_callback;     /* Called when the CPU function code changes */
        instr_hook_callbackPtr instr_hook_callback;       /* Called every instruction cycle prior to execution */
    }
    public static abstract interface bkpt_ack_callbackPtr {
        public abstract void handler(int data);
    }

    public static abstract interface reset_instr_callbackPtr {
        public abstract void handler();
    }

    public static abstract interface pc_changed_callbackPtr {
        public abstract void handler(int new_pc);
    }

    public static abstract interface set_fc_callbackPtr {
        public abstract void handler(int new_fc);
    }

    public static abstract interface instr_hook_callbackPtr {
        public abstract void handler();
    }
}
