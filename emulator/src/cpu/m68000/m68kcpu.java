package cpu.m68000;

import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m68000.m68000H.*;
import static cpu.m68000.m68kH.*;
import static cpu.m68000.m68kmameH.*;
import static cpu.m68000.m68kmame.*;
import static cpu.m68000.m68kcpuH.*;
import static cpu.m68000.m68kopsH.*;
import static cpu.m68000.m68kops.*;
import static arcadeflex.libc_old.*;

public class m68kcpu {

    public static FILE m68klog = fopen("m68k.log", "wa");  //for debug purposes

    public static int m68k_emulation_initialized = 0;                /* flag if emulation has been initialized */

    public static opcode[] m68k_instruction_jump_table = new opcode[0x10000]; /* opcode handler jump table */

    public static int[] m68k_clks_left = new int[1];                          /* Number of clocks remaining */

    public static int m68k_tracing = 0;

    public static int m68k_sr_implemented_bits[] = {
        0x0000, /* invalid */
        0xa71f, /* 68000:   T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
        0xa71f, /* 68010:   T1 -- S  -- -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
        0x0000, /* invalid */
        0xf71f, /* 68EC020: T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
        0x0000, /* invalid */
        0x0000, /* invalid */
        0x0000, /* invalid */
        0xf71f, /* 68020:   T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */};

    /* The CPU core */
    public static m68k_cpu_core m68k_cpu = new m68k_cpu_core();

    /* Pointers to speed up address register indirect with index calculation */
    private long[][] m68k_cpu_dar = {get_CPU_D(), get_CPU_A()};

    /* Pointers to speed up movem instructions */
    private long[] m68k_movem_pi_table = {
        get_CPU_D()[0], get_CPU_D()[1], get_CPU_D()[2], get_CPU_D()[3],
        get_CPU_D()[4], get_CPU_D()[5], get_CPU_D()[6], get_CPU_D()[7],
        get_CPU_A()[0], get_CPU_A()[1], get_CPU_A()[2], get_CPU_A()[3],
        get_CPU_A()[4], get_CPU_A()[5], get_CPU_A()[6], get_CPU_A()[7],};

    private long[] m68k_movem_pd_table = {
        get_CPU_A()[7], get_CPU_A()[6], get_CPU_A()[5], get_CPU_A()[4],
        get_CPU_A()[3], get_CPU_A()[2], get_CPU_A()[1], get_CPU_A()[0],
        get_CPU_D()[7], get_CPU_D()[6], get_CPU_D()[5], get_CPU_D()[4],
        get_CPU_D()[3], get_CPU_D()[2], get_CPU_D()[1], get_CPU_D()[0],};

    /* Used when checking for pending interrupts */
    public static int m68k_int_masks[] = {0xfe, 0xfc, 0xf8, 0xf0, 0xe0, 0xc0, 0x80, 0x80};

    /*TODO*////* Used by shift & rotate instructions */
/*TODO*///uint8 m68k_shift_8_table[65] =
/*TODO*///{
/*TODO*///    0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
/*TODO*///    0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff
/*TODO*///};
/*TODO*///uint16 m68k_shift_16_table[65] =
/*TODO*///{
/*TODO*///    0x0000, 0x8000, 0xc000, 0xe000, 0xf000, 0xf800, 0xfc00, 0xfe00, 0xff00, 0xff80, 0xffc0, 0xffe0,
/*TODO*///    0xfff0, 0xfff8, 0xfffc, 0xfffe, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///    0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
/*TODO*///    0xffff, 0xffff, 0xffff, 0xffff, 0xffff
/*TODO*///};
/*TODO*///uint m68k_shift_32_table[65] =
/*TODO*///{
/*TODO*///    0x00000000, 0x80000000, 0xc0000000, 0xe0000000, 0xf0000000, 0xf8000000, 0xfc000000, 0xfe000000,
/*TODO*///    0xff000000, 0xff800000, 0xffc00000, 0xffe00000, 0xfff00000, 0xfff80000, 0xfffc0000, 0xfffe0000,
/*TODO*///    0xffff0000, 0xffff8000, 0xffffc000, 0xffffe000, 0xfffff000, 0xfffff800, 0xfffffc00, 0xfffffe00,
/*TODO*///    0xffffff00, 0xffffff80, 0xffffffc0, 0xffffffe0, 0xfffffff0, 0xfffffff8, 0xfffffffc, 0xfffffffe,
/*TODO*///    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
/*TODO*///    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff
/*TODO*///};
/*TODO*///

    /* Number of clock cycles to use for exception processing.
     * I used 4 for any vectors that are undocumented for processing times.
     */
    int m68k_exception_cycle_table[]
            = {
                40, /*  0: Reset - should never be called                                 */
                40, /*  1: Reset - should never be called                                 */
                50, /*  2: Bus Error                                (unused in emulation) */
                50, /*  3: Address Error                            (unused in emulation) */
                34, /*  4: Illegal Instruction                                            */
                38, /*  5: Divide by Zero -- ASG: changed from 42                         */
                40, /*  6: CHK -- ASG: chanaged from 44                                   */
                34, /*  7: TRAPV                                                          */
                34, /*  8: Privilege Violation                                            */
                34, /*  9: Trace                                                          */
                4, /* 10: 1010                                                           */
                4, /* 11: 1111                                                           */
                4, /* 12: RESERVED                                                       */
                4, /* 13: Coprocessor Protocol Violation           (unused in emulation) */
                4, /* 14: Format Error                             (unused in emulation) */
                44, /* 15: Uninitialized Interrupt                                        */
                4, /* 16: RESERVED                                                       */
                4, /* 17: RESERVED                                                       */
                4, /* 18: RESERVED                                                       */
                4, /* 19: RESERVED                                                       */
                4, /* 20: RESERVED                                                       */
                4, /* 21: RESERVED                                                       */
                4, /* 22: RESERVED                                                       */
                4, /* 23: RESERVED                                                       */
                44, /* 24: Spurious Interrupt                                             */
                44, /* 25: Level 1 Interrupt Autovector                                   */
                44, /* 26: Level 2 Interrupt Autovector                                   */
                44, /* 27: Level 3 Interrupt Autovector                                   */
                44, /* 28: Level 4 Interrupt Autovector                                   */
                44, /* 29: Level 5 Interrupt Autovector                                   */
                44, /* 30: Level 6 Interrupt Autovector                                   */
                44, /* 31: Level 7 Interrupt Autovector                                   */
                34, /* 32: TRAP #0 -- ASG: chanaged from 38                               */
                34, /* 33: TRAP #1                                                        */
                34, /* 34: TRAP #2                                                        */
                34, /* 35: TRAP #3                                                        */
                34, /* 36: TRAP #4                                                        */
                34, /* 37: TRAP #5                                                        */
                34, /* 38: TRAP #6                                                        */
                34, /* 39: TRAP #7                                                        */
                34, /* 40: TRAP #8                                                        */
                34, /* 41: TRAP #9                                                        */
                34, /* 42: TRAP #10                                                       */
                34, /* 43: TRAP #11                                                       */
                34, /* 44: TRAP #12                                                       */
                34, /* 45: TRAP #13                                                       */
                34, /* 46: TRAP #14                                                       */
                34, /* 47: TRAP #15                                                       */
                4, /* 48: FP Branch or Set on Unknown Condition    (unused in emulation) */
                4, /* 49: FP Inexact Result                        (unused in emulation) */
                4, /* 50: FP Divide by Zero                        (unused in emulation) */
                4, /* 51: FP Underflow                             (unused in emulation) */
                4, /* 52: FP Operand Error                         (unused in emulation) */
                4, /* 53: FP Overflow                              (unused in emulation) */
                4, /* 54: FP Signaling NAN                         (unused in emulation) */
                4, /* 55: FP Unimplemented Data Type               (unused in emulation) */
                4, /* 56: MMU Configuration Error                  (unused in emulation) */
                4, /* 57: MMU Illegal Operation Error              (unused in emulation) */
                4, /* 58: MMU Access Level Violation Error         (unused in emulation) */
                4, /* 59: RESERVED                                                       */
                4, /* 60: RESERVED                                                       */
                4, /* 61: RESERVED                                                       */
                4, /* 62: RESERVED                                                       */
                4, /* 63: RESERVED                                                       */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  64- 79: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  80- 95: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /*  96-111: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 112-127: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 128-143: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 144-159: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 160-175: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 176-191: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 192-207: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 208-223: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 224-239: User Defined */
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 240-255: User Defined */};

    /* ======================================================================== */
    /* =============================== CALLBACKS ============================== */
    /* ======================================================================== */

    /* Default callbacks used if the callback hasn't been set yet, or if the
     * callback is set to NULL
     */

    /* Interrupt acknowledge */
    static int default_int_ack_callback_data;

    static irqcallbacksPtr default_int_ack_callback = new irqcallbacksPtr() {
        public int handler(int int_level) {
            default_int_ack_callback_data = int_level;
            return M68K_INT_ACK_AUTOVECTOR;
        }
    };

    /* Breakpoint acknowledge */
    static int default_bkpt_ack_callback_data;
    static bkpt_ack_callbackPtr default_bkpt_ack_callback = new bkpt_ack_callbackPtr() {
        public void handler(int data) {
            default_bkpt_ack_callback_data = data;
        }
    };

    /* Called when a reset instruction is executed */
    static reset_instr_callbackPtr default_reset_instr_callback = new reset_instr_callbackPtr() {
        public void handler() {

        }
    };


    /* Called when the program counter changed by a large value */
    static int default_pc_changed_callback_data;
    static pc_changed_callbackPtr default_pc_changed_callback = new pc_changed_callbackPtr() {
        public void handler(int new_pc) {
            default_pc_changed_callback_data = new_pc;
        }
    };

    /* Called every time there's bus activity (read/write to/from memory */
    static int default_set_fc_callback_data;
    static set_fc_callbackPtr default_set_fc_callback = new set_fc_callbackPtr() {
        public void handler(int new_fc) {
            default_set_fc_callback_data = new_fc;
        }
    };


    /* Called every instruction cycle prior to execution */
    static instr_hook_callbackPtr default_instr_hook_callback = new instr_hook_callbackPtr() {
        public void handler() {

        }
    };

    /* ======================================================================== */
    /* ================================= API ================================== */
    /* ======================================================================== */

    /* Peek at the internals of the M68K */
    public static long m68k_peek_dr(int reg_num) {
        return (reg_num < 8) ? get_CPU_D()[reg_num] : 0L;
    }

    public static long m68k_peek_ar(int reg_num) {
        return (reg_num < 8) ? get_CPU_A()[reg_num] : 0L;
    }

    public static long m68k_peek_pc() {
        return ADDRESS_68K(get_CPU_PC());
    }

    public static long m68k_peek_ppc() {
        return ADDRESS_68K(get_CPU_PPC());
    }

    /*TODO*///int m68k_peek_sr(void)          { return m68ki_get_sr(); }
    public static long m68k_peek_ir() {
        return get_CPU_IR();
    }

    public static boolean m68k_peek_t1_flag() {
        return get_CPU_T1() != 0;
    }

    public static boolean m68k_peek_t0_flag() {
        return get_CPU_T0() != 0;
    }

    public static boolean m68k_peek_s_flag() {
        return get_CPU_S() != 0;
    }

    public static boolean m68k_peek_m_flag() {
        return get_CPU_M() != 0;
    }

    public static long m68k_peek_int_mask() {
        return get_CPU_INT_MASK();
    }

    public static boolean m68k_peek_x_flag() {
        return get_CPU_X() != 0;
    }

    public static boolean m68k_peek_n_flag() {
        return get_CPU_N() != 0;
    }

    public static boolean m68k_peek_z_flag() {
        return get_CPU_NOT_Z() == 0;
    }

    public static boolean m68k_peek_v_flag() {
        return get_CPU_V() != 0;
    }

    public static boolean m68k_peek_c_flag() {
        return get_CPU_C() != 0;
    }

    public static long m68k_peek_usp() {
        return ((get_CPU_S() != 0) ? get_CPU_USP() : get_CPU_A()[7]);
    }

    public static long m68k_peek_isp() {
        return ((get_CPU_S() != 0) && (get_CPU_M() == 0) ? get_CPU_A()[7] : get_CPU_ISP());
    }

    public static long m68k_peek_msp() {
        return ((get_CPU_S() != 0) && (get_CPU_M() != 0) ? get_CPU_A()[7] : get_CPU_MSP());
    }
    /* Poke data into the M68K */

    public static void m68k_poke_dr(int reg_num, long value) {
        if (reg_num < 8) {
            set_CPU_D(reg_num, MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_ar(int reg_num, long value) {
        if (reg_num < 8) {
            set_CPU_A(reg_num, MASK_OUT_ABOVE_32(value));
        }
    }
    /*TODO*///void m68k_poke_pc(unsigned int value)     { m68ki_set_pc(ADDRESS_68K(value)); }
/*TODO*///void m68k_poke_sr(int value)              { m68ki_set_sr(MASK_OUT_ABOVE_16(value)); }

    public static void m68k_poke_ir(long value) {
        set_CPU_IR(MASK_OUT_ABOVE_16(value));
    }

    public static void m68k_poke_t1_flag(int value) {
        set_CPU_T1(value != 0 ? 1 : 0); //CPU_T1 = (value != 0);
    }

    public static void m68k_poke_t0_flag(int value) {
        if ((get_CPU_MODE() & CPU_MODE_EC020_PLUS) != 0) {
            set_CPU_T0(value != 0 ? 1 : 0);//CPU_T0 = (value != 0);
        }
    }
    /*TODO*///void m68k_poke_s_flag(int value)          { m68ki_set_s_flag(value); }
/*TODO*///void m68k_poke_m_flag(int value)          { if(CPU_MODE & CPU_MODE_EC020_PLUS) m68ki_set_m_flag(value); }

    public static void m68k_poke_int_mask(int value) {
        set_CPU_INT_MASK(value & 7);
    }

    public static void m68k_poke_x_flag(int value) {
        set_CPU_X(value != 0 ? 1 : 0);//CPU_X = (value != 0);
    }

    public static void m68k_poke_n_flag(int value) {
        set_CPU_N(value != 0 ? 1 : 0);//CPU_N = (value != 0);
    }

    public static void m68k_poke_z_flag(int value) {
        set_CPU_NOT_Z(value == 0 ? 1 : 0);//CPU_NOT_Z = (value == 0);
    }

    public static void m68k_poke_v_flag(int value) {
        set_CPU_V(value != 0 ? 1 : 0);//CPU_V = (value != 0);
    }

    public static void m68k_poke_c_flag(int value) {
        set_CPU_C(value != 0 ? 1 : 0);//CPU_C = (value != 0);
    }

    public static void m68k_poke_usp(long value) {
        if (get_CPU_S() != 0) {
            set_CPU_USP(MASK_OUT_ABOVE_32(value));
        } else {
            set_CPU_A(7, MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_isp(long value) {
        if ((get_CPU_S() != 0) && (get_CPU_M() == 0)) {
            set_CPU_A(7, MASK_OUT_ABOVE_32(value));
        } else {
            set_CPU_ISP(MASK_OUT_ABOVE_32(value));
        }
    }
    /*TODO*///void m68k_poke_msp(int value)
/*TODO*///{
/*TODO*///   if(CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///   {
/*TODO*///      if(CPU_S && CPU_M)
/*TODO*///         CPU_A[7] = MASK_OUT_ABOVE_32(value);
/*TODO*///      else
/*TODO*///         CPU_MSP = MASK_OUT_ABOVE_32(value);
/*TODO*///   }
/*TODO*///}
/*TODO*///
    /* Set the callbacks */

    public static void m68k_set_int_ack_callback(irqcallbacksPtr callback) {
        if (callback != null) {
            set_CPU_INT_ACK_CALLBACK(callback);
        } else {
            set_CPU_INT_ACK_CALLBACK(default_int_ack_callback);
        }
    }

    public static void m68k_set_bkpt_ack_callback(bkpt_ack_callbackPtr callback) {
        if (callback != null) {
            set_CPU_BKPT_ACK_CALLBACK(callback);
        } else {
            set_CPU_BKPT_ACK_CALLBACK(default_bkpt_ack_callback);
        }
    }

    public static void m68k_set_reset_instr_callback(reset_instr_callbackPtr callback) {
        if (callback != null) {
            set_CPU_RESET_INSTR_CALLBACK(callback);
        } else {
            set_CPU_RESET_INSTR_CALLBACK(default_reset_instr_callback);
        }
    }

    public static void m68k_set_pc_changed_callback(pc_changed_callbackPtr callback) {
        if (callback != null) {
            set_CPU_PC_CHANGED_CALLBACK(callback);
        } else {
            set_CPU_PC_CHANGED_CALLBACK(default_pc_changed_callback);
        }
    }

    public static void m68k_set_fc_callback(set_fc_callbackPtr callback) {
        if (callback != null) {
            set_CPU_SET_FC_CALLBACK(callback);
        } else {
            set_CPU_SET_FC_CALLBACK(default_set_fc_callback);
        }
    }

    public static void m68k_set_instr_hook_callback(instr_hook_callbackPtr callback) {
        // TODO
        if (callback != null) {
            set_CPU_INSTR_HOOK_CALLBACK(callback);
        } else {
            set_CPU_INSTR_HOOK_CALLBACK(default_instr_hook_callback);
        }
    }

    public static void m68k_set_cpu_mode(int cpu_mode) {
        switch (cpu_mode) {
            case M68K_CPU_MODE_68000:
            case M68K_CPU_MODE_68010:
            case M68K_CPU_MODE_68EC020:
            case M68K_CPU_MODE_68020:
                set_CPU_MODE(cpu_mode);
                return;
            default:
                set_CPU_MODE(M68K_CPU_MODE_68000);
        }
    }

    /* Execute some instructions until we use up num_clks clock cycles */
    /* ASG: removed per-instruction interrupt checks */
    public static int m68k_execute(int num_clks) {
        /* Make sure we're not stopped */
        if (get_CPU_STOPPED() == 0)//if(!CPU_STOPPED)
        {
            /* Set our pool of clock cycles available */
            m68k_clks_left[0] = num_clks;

            /* ASG: update cycles */
            m68k_clks_left[0] -= get_CPU_INT_CYCLES();
            set_CPU_INT_CYCLES(0);


            /* Main loop.  Keep going until we run out of clock cycles */
            do {
                set_CPU_PPC(get_CPU_PC());

                /* Read an instruction and call its handler */
                set_CPU_IR(m68ki_read_instruction());
                opcode i = m68k_instruction_jump_table[(int) get_CPU_IR()];
                i.handler();

                continue;
            } while (m68k_clks_left[0] > 0);

            /* set previous PC to current PC for the next entry into the loop */
            set_CPU_PPC(get_CPU_PC());

            /* ASG: update cycles */
            m68k_clks_left[0] -= get_CPU_INT_CYCLES();;
            set_CPU_INT_CYCLES(0);

            /* return how many clocks we used */
            return num_clks - m68k_clks_left[0];
        }
        /* We get here if the CPU is stopped */
        m68k_clks_left[0] = 0;

        return num_clks;
    }


    /* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */
    public static void m68k_assert_irq(int int_line) {
        /* OR in the bits of the interrupt */
   
        long old_state = get_CPU_INT_STATE();
        set_CPU_INT_STATE(0);	/* ASG: remove me to do proper mask setting */

        set_CPU_INT_STATE(get_CPU_INT_STATE() | (int_line & 7));

        /* if it's NMI, we're edge triggered */
        if (get_CPU_INT_STATE() == 7L) {
            if (old_state != 7L) {
                if (m68klog != null) {
                    fprintf(m68klog, "m68k_assert_irq1 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
                }
                m68ki_service_interrupt(1 << 7L);
            }
        } /* other interrupts just reflect the current state */ else {
            if (m68klog != null) {
                fprintf(m68klog, "m68k_assert_irq2 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
            }
            m68ki_check_interrupts();
        }
    }
    /* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */

    public static void m68k_clear_irq(int int_line) {
        /* AND in the bits of the interrupt */
        long state = get_CPU_INT_STATE();
        set_CPU_INT_STATE(state & (~int_line & 7));
        set_CPU_INT_STATE(0); /* ASG: remove me to do proper mask setting */

        if (m68klog != null) {
            fprintf(m68klog, "m68k_clear_irq :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
        }
        /* check for interrupts again */
        m68ki_check_interrupts();
    }

    /* Reset the M68K */
    public static void m68k_pulse_reset(Object param) {
        set_CPU_HALTED(0);
        set_CPU_STOPPED(0);
        set_CPU_INT_STATE(0);	/* ASG: changed from CPU_INTS_PENDING */

        set_CPU_T1(0);
        set_CPU_T0(0);
        set_CPU_S(1);
        set_CPU_M(0);
        set_CPU_INT_MASK(7);
        set_CPU_VBR(0);
        set_CPU_A(7, m68ki_read_32(0));//CPU_A[7] = m68ki_read_32(0);
        m68ki_set_pc(m68ki_read_32(4));
        set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
        set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));

        m68k_clks_left[0] = 0;
        if (get_CPU_MODE() == 0) {
            set_CPU_MODE(MC68000_CPU_MODE_68000);	/* KW 990319 */

        }

        /* The first call to this function initializes the opcode handler jump table */
        if (m68k_emulation_initialized != 0) {
            return;
        } else {
            m68ki_build_opcode_table();
            m68k_set_int_ack_callback(null);
            m68k_set_bkpt_ack_callback(null);
            m68k_set_reset_instr_callback(null);
            m68k_set_pc_changed_callback(null);
            m68k_set_fc_callback(null);
            m68k_set_instr_hook_callback(null);

            m68k_emulation_initialized = 1;
        }
        if (m68klog != null) {
            fprintf(m68klog, "m68k_reset :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
        }
    }


    /* Halt the CPU */
    public static void m68k_pulse_halt() {
        set_CPU_HALTED(1);
    }
    /*TODO*///
/*TODO*////* Get and set the current CPU context */
/*TODO*////* This is to allow for multiple CPUs */
/*TODO*///unsigned m68k_get_context(void* dst)
/*TODO*///{
/*TODO*///	if( dst )
/*TODO*///	{
/*TODO*///		m68k_cpu_context *cpu = dst;
/*TODO*///
/*TODO*///        cpu->mode                 = CPU_MODE;
/*TODO*///		cpu->sr 				  = m68ki_get_sr();
/*TODO*///		cpu->pc 				  = CPU_PC;
/*TODO*///		memcpy(cpu->d, CPU_D, sizeof(CPU_D));
/*TODO*///		memcpy(cpu->a, CPU_A, sizeof(CPU_A));
/*TODO*///		cpu->usp				  = CPU_USP;
/*TODO*///		cpu->isp				  = CPU_ISP;
/*TODO*///		cpu->msp				  = CPU_MSP;
/*TODO*///		cpu->vbr				  = CPU_VBR;
/*TODO*///		cpu->sfc				  = CPU_SFC;
/*TODO*///		cpu->dfc				  = CPU_DFC;
/*TODO*///		cpu->stopped			  = CPU_STOPPED;
/*TODO*///		cpu->halted 			  = CPU_HALTED;
/*TODO*///		cpu->int_state			  = CPU_INT_STATE;	/* ASG: changed from CPU_INTS_PENDING */
/*TODO*///		cpu->int_cycles           = CPU_INT_CYCLES;	/* ASG */
/*TODO*///		cpu->int_ack_callback	  = CPU_INT_ACK_CALLBACK;
/*TODO*///		cpu->bkpt_ack_callback	  = CPU_BKPT_ACK_CALLBACK;
/*TODO*///		cpu->reset_instr_callback = CPU_RESET_INSTR_CALLBACK;
/*TODO*///		cpu->pc_changed_callback  = CPU_PC_CHANGED_CALLBACK;
/*TODO*///		cpu->set_fc_callback	  = CPU_SET_FC_CALLBACK;
/*TODO*///		cpu->instr_hook_callback  = CPU_INSTR_HOOK_CALLBACK;
/*TODO*///		cpu->pref_addr			  = CPU_PREF_ADDR;
/*TODO*///		cpu->pref_data			  = CPU_PREF_DATA;
/*TODO*///	}
/*TODO*///	return sizeof(m68k_cpu_context);
/*TODO*///}
/*TODO*///
/*TODO*///void m68k_set_context(void* src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		m68k_cpu_context *cpu = src;
/*TODO*///
/*TODO*///        CPU_MODE                 = cpu->mode;
/*TODO*///		m68ki_set_sr_no_int(cpu->sr); /* This stays on top to prevent side-effects */
/*TODO*///		m68ki_set_pc(cpu->pc);
/*TODO*///		memcpy(CPU_D, cpu->d, sizeof(CPU_D));
/*TODO*///		memcpy(CPU_A, cpu->a, sizeof(CPU_D));
/*TODO*///		CPU_USP 				 = cpu->usp;
/*TODO*///		CPU_ISP 				 = cpu->isp;
/*TODO*///		CPU_MSP 				 = cpu->msp;
/*TODO*///		CPU_VBR 				 = cpu->vbr;
/*TODO*///		CPU_SFC 				 = cpu->sfc;
/*TODO*///		CPU_DFC 				 = cpu->dfc;
/*TODO*///		CPU_STOPPED 			 = cpu->stopped;
/*TODO*///		CPU_HALTED				 = cpu->halted;
/*TODO*///		CPU_INT_STATE			 = cpu->int_state;	/* ASG: changed from CPU_INTS_PENDING */
/*TODO*///		CPU_INT_CYCLES           = cpu->int_cycles;	/* ASG */
/*TODO*///		CPU_INT_ACK_CALLBACK	 = cpu->int_ack_callback;
/*TODO*///		CPU_BKPT_ACK_CALLBACK	 = cpu->bkpt_ack_callback;
/*TODO*///		CPU_RESET_INSTR_CALLBACK = cpu->reset_instr_callback;
/*TODO*///		CPU_PC_CHANGED_CALLBACK  = cpu->pc_changed_callback;
/*TODO*///		CPU_SET_FC_CALLBACK 	 = cpu->set_fc_callback;
/*TODO*///		CPU_INSTR_HOOK_CALLBACK  = cpu->instr_hook_callback;
/*TODO*///		CPU_PREF_ADDR 			 = cpu->pref_addr;
/*TODO*///		CPU_PREF_DATA 			 = cpu->pref_data;
/*TODO*///
/*TODO*///		/* ASG: check for interrupts */
/*TODO*///		m68ki_check_interrupts();
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* Check if the instruction is a valid one */
/*TODO*///int m68k_is_valid_instruction(int instruction, int cpu_mode)
/*TODO*///{
/*TODO*///   if(m68k_instruction_jump_table[MASK_OUT_ABOVE_16(instruction)] == m68000_illegal)
/*TODO*///      return 0;
/*TODO*///   if(!(cpu_mode & CPU_MODE_010_PLUS))
/*TODO*///   {
/*TODO*///      if((instruction & 0xfff8) == 0x4848) /* bkpt */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xffc0) == 0x42c0) /* move from ccr */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xfffe) == 0x4e7a) /* movec */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff00) == 0x0e00) /* moves */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xffff) == 0x4e74) /* rtd */
/*TODO*///         return 0;
/*TODO*///   }
/*TODO*///   if(!(cpu_mode & CPU_MODE_EC020_PLUS))
/*TODO*///   {
/*TODO*///      if((instruction & 0xf0ff) == 0x60ff) /* bcc.l */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf8c0) == 0xe8c0) /* bfxxx */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xffc0) == 0x06c0) /* callm */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf9c0) == 0x08c0) /* cas */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf9ff) == 0x08fc) /* cas2 */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf1c0) == 0x4100) /* chk.l */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf9c0) == 0x00c0) /* chk2, cmp2 */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff3f) == 0x0c3a) /* cmpi (pcdi) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff3f) == 0x0c3b) /* cmpi (pcix) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xffc0) == 0x4c40) /* divl */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xfff8) == 0x49c0) /* extb */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xfff8) == 0x4808) /* link.l */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xffc0) == 0x4c00) /* mull */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf1f0) == 0x8140) /* pack */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xfff0) == 0x06c0) /* rtm */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf0f8) == 0x50f8) /* trapcc */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff38) == 0x4a08) /* tst (a) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff3f) == 0x4a3a) /* tst (pcdi) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff3f) == 0x4a3b) /* tst (pcix) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xff3f) == 0x4a3c) /* tst (imm) */
/*TODO*///         return 0;
/*TODO*///      if((instruction & 0xf1f0) == 0x8180) /* unpk */
/*TODO*///         return 0;
/*TODO*///   }
/*TODO*///   return 1;
/*TODO*///}
}
