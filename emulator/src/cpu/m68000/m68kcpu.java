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

public class m68kcpu {
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
        0xf71f, /* 68020:   T1 T0 S  M  -- I2 I1 I0 -- -- -- X  N  Z  V  C  */
    };

    /* The CPU core */
    private static m68k_cpu_core m68k_cpu = new m68k_cpu_core();

    /* Pointers to speed up address register indirect with index calculation */
    private int[][] m68k_cpu_dar = {m68k_cpu.get_CPU_D(), m68k_cpu.get_CPU_A()};
    
    /* Pointers to speed up movem instructions */
    private int[] m68k_movem_pi_table = {
        m68k_cpu.get_CPU_D()[0], m68k_cpu.get_CPU_D()[1], m68k_cpu.get_CPU_D()[2], m68k_cpu.get_CPU_D()[3],
        m68k_cpu.get_CPU_D()[4], m68k_cpu.get_CPU_D()[5], m68k_cpu.get_CPU_D()[6], m68k_cpu.get_CPU_D()[7],
        m68k_cpu.get_CPU_A()[0], m68k_cpu.get_CPU_A()[1], m68k_cpu.get_CPU_A()[2], m68k_cpu.get_CPU_A()[3],
        m68k_cpu.get_CPU_A()[4], m68k_cpu.get_CPU_A()[5], m68k_cpu.get_CPU_A()[6], m68k_cpu.get_CPU_A()[0],
    };

    private int[] m68k_movem_pd_table = {
        m68k_cpu.get_CPU_A()[7], m68k_cpu.get_CPU_A()[6], m68k_cpu.get_CPU_A()[5], m68k_cpu.get_CPU_A()[4],
        m68k_cpu.get_CPU_A()[3], m68k_cpu.get_CPU_A()[2], m68k_cpu.get_CPU_A()[1], m68k_cpu.get_CPU_A()[0],
        m68k_cpu.get_CPU_D()[7], m68k_cpu.get_CPU_D()[6], m68k_cpu.get_CPU_D()[5], m68k_cpu.get_CPU_D()[4],
        m68k_cpu.get_CPU_D()[3], m68k_cpu.get_CPU_D()[2], m68k_cpu.get_CPU_D()[1], m68k_cpu.get_CPU_D()[0],
    };

    /* Used when checking for pending interrupts */
    byte[] m68k_int_masks = {
        (byte)0xfe, (byte)0xfc, (byte)0xf8, (byte)0xf0,
        (byte)0xe0, (byte)0xc0, (byte)0x80, (byte)0x80
    };

    /* Used by shift & rotate instructions */
    byte[] m68k_shift_8_table = {
        (byte)0x00, (byte)0x80, (byte)0xc0, (byte)0xe0, (byte)0xf0, (byte)0xf8, (byte)0xfc, (byte)0xfe,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
        (byte)0xff
    };
    
    short[] m68k_shift_16_table = {
        (short)0x0000, (short)0x8000, (short)0xc000, (short)0xe000, (short)0xf000, (short)0xf800,
        (short)0xfc00, (short)0xfe00, (short)0xff00, (short)0xff80, (short)0xffc0, (short)0xffe0,
        (short)0xfff0, (short)0xfff8, (short)0xfffc, (short)0xfffe, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff,
        (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff, (short)0xffff
    };
    
    int[] m68k_shift_32_table = {
        0x00000000, 0x80000000, 0xc0000000, 0xe0000000, 0xf0000000, 0xf8000000, 0xfc000000, 0xfe000000,
        0xff000000, 0xff800000, 0xffc00000, 0xffe00000, 0xfff00000, 0xfff80000, 0xfffc0000, 0xfffe0000,
        0xffff0000, 0xffff8000, 0xffffc000, 0xffffe000, 0xfffff000, 0xfffff800, 0xfffffc00, 0xfffffe00,
        0xffffff00, 0xffffff80, 0xffffffc0, 0xffffffe0, 0xfffffff0, 0xfffffff8, 0xfffffffc, 0xfffffffe,
        0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
        0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
        0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
        0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
        0xffffffff
    };
    
    byte[] m68k_exception_cycle_table = {
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
         4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, /* 240-255: User Defined */
    };
    
    /* Peek at the internals of the M68K */
    public static int m68k_peek_dr(int reg_num) {
        return (reg_num < 8) ? m68k_cpu.get_CPU_D()[reg_num] : 0;
    }

    public static int m68k_peek_ar(int reg_num) {
        return (reg_num < 8) ? m68k_cpu.get_CPU_A()[reg_num] : 0;
    }

    public static int m68k_peek_pc() {
        return m68k_cpu.ADDRESS_68K(m68k_cpu.get_CPU_PC());
    }

    public static int m68k_peek_ppc() {
        return m68k_cpu.ADDRESS_68K(m68k_cpu.get_CPU_PPC());
    }

    public static int m68k_peek_sr() {
        return 0;
    }

    public static int m68k_peek_ir() {
        return m68k_cpu.get_CPU_IR();
    }

    public static boolean m68k_peek_t1_flag() {
        return m68k_cpu.get_CPU_T1() != 0;
    }

    public static boolean m68k_peek_t0_flag() {
        return m68k_cpu.get_CPU_T0() != 0;
    }

    public static boolean m68k_peek_s_flag() {
        return m68k_cpu.get_CPU_S() != 0;
    }

    public static boolean m68k_peek_m_flag() {
        return m68k_cpu.get_CPU_M() != 0;
    }

    public static int m68k_peek_int_mask() {
        return m68k_cpu.get_CPU_INT_MASK();
    }

    public static boolean m68k_peek_x_flag() {
        return m68k_cpu.get_CPU_X() != 0;
    }

    public static boolean m68k_peek_n_flag() {
        return m68k_cpu.get_CPU_N() != 0;
    }

    public static boolean m68k_peek_z_flag() {
        return m68k_cpu.get_CPU_NOT_Z() == 0;
    }

    public static boolean m68k_peek_v_flag() {
        return m68k_cpu.get_CPU_V() != 0;
    }

    public static boolean m68k_peek_c_flag() {
        return m68k_cpu.get_CPU_C() != 0;
    }

    public static int m68k_peek_usp() {
        return ((m68k_cpu.get_CPU_S() != 0) ? m68k_cpu.get_CPU_USP() : m68k_cpu.get_CPU_A()[7]);
    }

    public static int m68k_peek_isp() {
        return ((m68k_cpu.get_CPU_S() != 0) && (m68k_cpu.get_CPU_M() == 0) ? m68k_cpu.get_CPU_A()[7] : m68k_cpu.get_CPU_ISP());
    }

    public static int m68k_peek_msp() {
        return ((m68k_cpu.get_CPU_S() != 0) && (m68k_cpu.get_CPU_M() != 0) ? m68k_cpu.get_CPU_A()[7] : m68k_cpu.get_CPU_MSP());
    }

    /* Poke data into the M68K */
    public static void m68k_poke_dr(int reg_num, int value) {
        if (reg_num < 8) {
            m68k_cpu.set_CPU_D(8, m68k_cpu.MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_ar(int reg_num, int value) {
        if (reg_num < 8) {
            m68k_cpu.set_CPU_A(8, m68k_cpu.MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_pc(int value) {
        m68k_cpu.set_CPU_PC(m68k_cpu.ADDRESS_68K(value));
    }

    public static void m68k_poke_sr(int value) { /*m68ki_set_sr(MASK_OUT_ABOVE_16(value));*/ }

    public static void m68k_poke_ir(int value) {
        m68k_cpu.set_CPU_IR(m68k_cpu.MASK_OUT_ABOVE_16(value));
    }

    public static void m68k_poke_t1_flag(int value) {
        m68k_cpu.set_CPU_T1(value);
    }

    public static void m68k_poke_t0_flag(int value) {
        if ((m68k_cpu.get_CPU_MODE() & CPU_TYPE_020) == CPU_TYPE_020) {
            m68k_cpu.set_CPU_T0(value);
        }
    }

    public static void m68k_poke_s_flag(int value) {
        m68k_cpu.set_CPU_S(value);
    }

    public static void m68k_poke_m_flag(int value) {
        if ((m68k_cpu.get_CPU_MODE() & CPU_TYPE_020) == CPU_TYPE_020) {
            m68k_cpu.set_CPU_M(value);
        }
    }

    public static void m68k_poke_int_mask(int value) {
        m68k_cpu.set_CPU_INT_MASK(value & 7);
    }

    public static void m68k_poke_x_flag(int value) {
        m68k_cpu.set_CPU_X(value);
    }

    public static void m68k_poke_n_flag(int value) {
        m68k_cpu.set_CPU_N(value);
    }

    public static void m68k_poke_z_flag(int value) {
        m68k_cpu.set_CPU_NOT_Z(value);
    }

    public static void m68k_poke_v_flag(int value) {
        m68k_cpu.set_CPU_V(value);
    }

    public static void m68k_poke_c_flag(int value) {
        m68k_cpu.set_CPU_C(value);
    }

    public static void m68k_poke_usp(int value) {
        if (m68k_cpu.get_CPU_S() != 0) {
            m68k_cpu.set_CPU_USP(m68k_cpu.MASK_OUT_ABOVE_32(value));
        } else {
            m68k_cpu.set_CPU_A(7, m68k_cpu.MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_isp(int value) {
        if ((m68k_cpu.get_CPU_S() != 0) && (m68k_cpu.get_CPU_M() == 0)) {
            m68k_cpu.set_CPU_A(7, m68k_cpu.MASK_OUT_ABOVE_32(value));
        } else {
            m68k_cpu.set_CPU_ISP(m68k_cpu.MASK_OUT_ABOVE_32(value));
        }
    }

    public static void m68k_poke_msp(int value) {
        if ((m68k_cpu.get_CPU_MODE() & CPU_TYPE_020) == CPU_TYPE_020) {
            if ((m68k_cpu.get_CPU_S() != 0) && (m68k_cpu.get_CPU_M() != 0)) {
                m68k_cpu.set_CPU_A(7, m68k_cpu.MASK_OUT_ABOVE_32(value));
            } else {
                m68k_cpu.set_CPU_MSP(m68k_cpu.MASK_OUT_ABOVE_32(value));
            }
        }
    }
    
    /* Interrupt acknowledge */
    static int default_int_ack_callback_data;
    static int default_int_ack_callback(int int_level) {
        default_int_ack_callback_data = int_level;
        return M68K_INT_ACK_AUTOVECTOR;
    }

    /* Breakpoint acknowledge */
    static int default_bkpt_ack_callback_data;
    static void default_bkpt_ack_callback(int data) {
        default_bkpt_ack_callback_data = data;
    }

    /* Called when a reset instruction is executed */
    static void default_reset_instr_callback() {   
    }

    /* Called when the program counter changed by a large value */
    static int default_pc_changed_callback_data;
    static void default_pc_changed_callback(int new_pc) {
        default_pc_changed_callback_data = new_pc;
    }

    /* Called every time there's bus activity (read/write to/from memory */
    static int default_set_fc_callback_data;
    static void default_set_fc_callback(int new_fc) {
        default_set_fc_callback_data = new_fc;
    }

    /* Called every instruction cycle prior to execution */
    static void default_instr_hook_callback() {
    }
    
    /* Set the callbacks */
    public static void m68k_set_int_ack_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_INT_ACK_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_INT_ACK_CALLBACK(null);
    }
    
    public static void m68k_set_bkpt_ack_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_BKPT_ACK_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_BKPT_ACK_CALLBACK(null);
    }
    
    public static void m68k_set_reset_instr_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_RESET_INSTR_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_RESET_INSTR_CALLBACK(null);
    }
    
    public static void m68k_set_pc_changed_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_PC_CHANGED_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_PC_CHANGED_CALLBACK(null);
    }
    
    public static void m68k_set_fc_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_SET_FC_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_SET_FC_CALLBACK(null);
    }
    
    public static void m68k_set_instr_hook_callback(irqcallbacksPtr callback) {
        // TODO
        if (callback != null)
            m68k_cpu.set_CPU_INSTR_HOOK_CALLBACK(callback);
        else
            m68k_cpu.set_CPU_INSTR_HOOK_CALLBACK(null);
    }
    
    public static void m68k_set_cpu_mode(int cpu_mode)
    {
        switch(cpu_mode)
        {
            case M68K_CPU_MODE_68000:
            case M68K_CPU_MODE_68010:
            case M68K_CPU_MODE_68EC020:
            case M68K_CPU_MODE_68020:
                m68k_cpu.set_CPU_MODE(cpu_mode);
            return;
            default:
                m68k_cpu.set_CPU_MODE(M68K_CPU_MODE_68000);
        }
    }
    
    /* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */
    void m68k_assert_irq(int int_line)
    {
       /* OR in the bits of the interrupt */
       int old_state = m68k_cpu.get_CPU_INT_STATE();
       m68k_cpu.set_CPU_INT_STATE(0);	/* ASG: remove me to do proper mask setting */
       int new_state = m68k_cpu.get_CPU_INT_STATE();
       m68k_cpu.set_CPU_INT_STATE(new_state | (int_line & 7));

       /* if it's NMI, we're edge triggered */
       if (m68k_cpu.get_CPU_INT_STATE() == 7)
       {
          if (old_state != 7)
             m68k_cpu.service_interrupt(1 << 7);
       }

       /* other interrupts just reflect the current state */
       else
          m68k_cpu.check_interrupts();
    }

    /* ASG: rewrote so that the int_line is a mask of the IPL0/IPL1/IPL2 bits */
    void m68k_clear_irq(int int_line)
    {
       /* AND in the bits of the interrupt */
       int state = m68k_cpu.get_CPU_INT_STATE();
       m68k_cpu.set_CPU_INT_STATE(state & (~int_line & 7));
       m68k_cpu.set_CPU_INT_STATE(0); /* ASG: remove me to do proper mask setting */

       /* check for interrupts again */
       m68k_cpu.check_interrupts();
    }
    
    /* Execute some instructions until we use up num_clks clock cycles */
    /* ASG: removed per-instruction interrupt checks */
    int m68k_execute(int num_clks)
    {
        if (M68K_HALT != 0)
        {
          if(m68k_cpu.get_CPU_HALTED() != 0)
          {
          }
        }
        
        /* Make sure we're not stopped */
        if(m68k_cpu.get_CPU_HALTED() != 0)
        {
          /* Set our pool of clock cycles available */
          m68k_clks_left[0] = num_clks;

          /* ASG: update cycles */
          m68k_clks_left[0] -= m68k_cpu.get_CPU_INT_CYCLES();
          m68k_cpu.set_CPU_INT_CYCLES(0);

          /* Main loop.  Keep going until we run out of clock cycles */
          do
          {
             /* Set tracing accodring to T1. (T0 is done inside instruction) */
             //m68k_cpu.set_trace(); /* auto-disable (see m68kcpu.h) */

             /* Call external hook to peek at CPU */
             //m68k_cpu.instr_hook(); /* auto-disable (see m68kcpu.h) */

             /* MAME */
             m68k_cpu.set_CPU_PPC(m68k_cpu.get_CPU_PC());
             //CALL_MAME_DEBUG;

             /* Read an instruction and call its handler */
             m68k_cpu.set_CPU_IR(0/*read_instruction()*/);
             opcode i = m68k_instruction_jump_table[m68k_cpu.get_CPU_IR()];
             i.handler();

             /* Trace m68k_exception, if necessary */
             //exception_if_trace(); /* auto-disable (see m68kcpu.h) */
             continue;
          } while(m68k_clks_left[0] > 0);

          /* set previous PC to current PC for the next entry into the loop */
          m68k_cpu.set_CPU_PPC(m68k_cpu.get_CPU_PC());

          /* ASG: update cycles */
          m68k_clks_left[0] -= m68k_cpu.get_CPU_INT_CYCLES();;
          m68k_cpu.set_CPU_INT_CYCLES(0);
          
          /* return how many clocks we used */
          return num_clks - m68k_clks_left[0];
       }

       /* We get here if the CPU is stopped */
       m68k_clks_left[0] = 0;

       return num_clks;
    }

    public static void m68k_pulse_reset(Object param) {
        m68k_cpu.set_CPU_HALTED(0);
        m68k_cpu.set_CPU_STOPPED(0);
        m68k_cpu.set_CPU_INT_STATE(0);
        m68k_cpu.set_CPU_T1(0);
        m68k_cpu.set_CPU_T0(0);
        /*TOD0*///   m68ki_clear_trace();
        m68k_cpu.set_CPU_S(1);
        m68k_cpu.set_CPU_M(0);
        m68k_cpu.set_CPU_INT_MASK(7);
        m68k_cpu.set_CPU_VBR(0);
        /*TOD0*///   CPU_A[7] = m68ki_read_32(0);
        /*TOD0*///   m68ki_set_pc(m68ki_read_32(4));
        /*TOD0*///#if M68K_USE_PREFETCH
        /*TOD0*///   CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
        /*TOD0*///   CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
        /*TOD0*///#endif /* M68K_USE_PREFETCH */
        m68k_clks_left[0] = 0;

        if (m68k_cpu.get_CPU_MODE() == 0)
            m68k_cpu.set_CPU_MODE(MC68000_CPU_MODE_68000);	/* KW 990319 */
        
        /* The first call to this function initializes the opcode handler jump table */
        if (m68k_emulation_initialized == 0) {
            m68ki_build_opcode_table();
            /*
            m68k_set_int_ack_callback();
            m68k_set_bkpt_ack_callback();
            m68k_set_reset_instr_callback();
            m68k_set_pc_changed_callback();
            m68k_set_fc_callback();
            m68k_set_instr_hook_callback();
            */
            m68k_emulation_initialized = 1;
        }
    }

    /* Halt the CPU */
    void m68k_pulse_halt()
    {
       m68k_cpu.set_CPU_HALTED(1);
    }
}
