package cpu.m68000;

public class m68000H {

    public static abstract interface int_ack_callbackPtr {

        public abstract int handler(int int_line);
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

    public static class m68k_cpu_core {

        public int/*public int*/ mode;        /* CPU Operation Mode: 68000, 68010, or 68020 */

        public int[]/*uint*/ dr = new int[8];       /* Data Registers */

        public int[]/*uint*/ ar = new int[8];       /* Address Registers */

        public int/*uint*/ ppc;		 /* Previous program counter */

        public int/*uint*/ pc;          /* Program Counter */

        public int[]/*uint*/ sp = new int[4];       /* User, Interrupt, and Master Stack Pointers */

        public int/*uint*/ vbr;         /* Vector Base Register (68010+) */

        public int/*uint*/ sfc;         /* Source Function Code Register (m68010+) */

        public int/*uint*/ dfc;         /* Destination Function Code Register (m68010+) */

        public int/*uint*/ cacr;        /* Cache Control Register (m68020+) */

        public int/*uint*/ caar;        /* Cacge Address Register (m68020+) */

        public int/*uint*/ ir;          /* Instruction Register */

        public int/*uint*/ t1_flag;     /* Trace 1 */

        public int/*uint*/ t0_flag;     /* Trace 0 */

        public int/*uint*/ s_flag;      /* Supervisor */

        public int/*uint*/ m_flag;      /* Master/Interrupt state */

        public int/*uint*/ x_flag;      /* Extend */

        public int/*uint*/ n_flag;      /* Negative */

        public int/*uint*/ not_z_flag;  /* Zero, inverted for speedups */

        public int/*uint*/ v_flag;      /* Overflow */

        public int/*uint*/ c_flag;      /* Carry */

        public int/*uint*/ int_mask;    /* I0-I2 */

        public int/*uint*/ int_state;   /* Current interrupt state -- ASG: changed from ints_pending */

        public int/*uint*/ stopped;     /* Stopped state */

        public int/*uint*/ halted;      /* Halted state */

        public int/*uint*/ int_cycles;  /* ASG: extra cycles from generated interrupts */

        public int/*uint*/ pref_addr;   /* Last prefetch address */

        public int/*uint*/ pref_data;   /* Data in the prefetch queue */

        /* Callbacks to host */
        int_ack_callbackPtr int_ack_callback;  /* Interrupt Acknowledge */

        bkpt_ack_callbackPtr bkpt_ack_callback;     /* Breakpoint Acknowledge */

        reset_instr_callbackPtr reset_instr_callback;   /* Called when a RESET instruction is encountered */

        pc_changed_callbackPtr pc_changed_callback; /* Called when the PC changes by a large amount */

        set_fc_callbackPtr set_fc_callback;     /* Called when the CPU function code changes */

        instr_hook_callbackPtr instr_hook_callback;       /* Called every instruction cycle prior to execution */

    }
    public static final int MC68000_INT_NONE = 0;
    public static final int MC68000_IRQ_1 = 1;
    public static final int MC68000_IRQ_2 = 2;
    public static final int MC68000_IRQ_3 = 3;
    public static final int MC68000_IRQ_4 = 4;
    public static final int MC68000_IRQ_5 = 5;
    public static final int MC68000_IRQ_6 = 6;
    public static final int MC68000_IRQ_7 = 7;

    public static final int MC68000_INT_ACK_AUTOVECTOR = -1;
    public static final int MC68000_INT_ACK_SPURIOUS = -2;

    public static final int MC68000_CPU_MODE_68000 = 1;
    public static final int MC68000_CPU_MODE_68010 = 2;
    public static final int MC68000_CPU_MODE_68020 = 4;
}
