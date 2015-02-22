package cpu.m68000;

import static mame.cpuintrfH.*;

public class m68kcpuH {
    
    /* Exception Vectors handled by emulation */
    public static int EXCEPTION_BUS_ERROR = 2;
    public static int EXCEPTION_ADDRESS_ERROR = 3;
    public static int EXCEPTION_ILLEGAL_INSTRUCTION = 4;
    public static int EXCEPTION_ZERO_DIVIDE = 5;
    public static int EXCEPTION_CHK = 6;
    public static int EXCEPTION_TRAPV = 7;
    public static int EXCEPTION_PRIVILEGE_VIOLATION = 8;
    public static int EXCEPTION_TRACE = 9;
    public static int EXCEPTION_1010 = 10;
    public static int EXCEPTION_1111 = 11;
    public static int EXCEPTION_FORMAT_ERROR = 14;
    public static int EXCEPTION_UNINITIALIZED_INTERRUPT = 15;
    public static int EXCEPTION_SPURIOUS_INTERRUPT = 24;
    public static int EXCEPTION_INTERRUPT_AUTOVECTOR = 24;
    public static int EXCEPTION_TRAP_BASE = 32;

    /* Function codes set by CPU during data/address bus activity */
    public static int FUNCTION_CODE_USER_DATA = 1;
    public static int FUNCTION_CODE_USER_PROGRAM = 2;
    public static int FUNCTION_CODE_SUPERVISOR_DATA = 5;
    public static int FUNCTION_CODE_SUPERVISOR_PROGRAM = 6;
    public static int FUNCTION_CODE_CPU_SPACE = 7;

    /* CPU types for deciding what to emulate */
    public static int CPU_TYPE_000 = 1;
    public static int CPU_TYPE_010 = 2;
    public static int CPU_TYPE_EC020 = 4;
    public static int CPU_TYPE_020 = 8;

    /* Different ways to stop the CPU */
    public static int STOP_LEVEL_STOP = 1;
    public static int STOP_LEVEL_HALT = 2;
    
    /* Bit Isolation */
    public int BIT_0(int A) { return ((A) & 0x00000001); }
    public int BIT_1(int A) { return ((A) & 0x00000002); }
    public int BIT_2(int A) { return ((A) & 0x00000004); }
    public int BIT_3(int A) { return ((A) & 0x00000008); }
    public int BIT_4(int A) { return ((A) & 0x00000010); }
    public int BIT_5(int A) { return ((A) & 0x00000020); }
    public int BIT_6(int A) { return ((A) & 0x00000040); }
    public int BIT_7(int A) { return ((A) & 0x00000080); }
    public int BIT_8(int A) { return ((A) & 0x00000100); }
    public int BIT_9(int A) { return ((A) & 0x00000200); }
    public int BIT_A(int A) { return ((A) & 0x00000400); }
    public int BIT_B(int A) { return ((A) & 0x00000800); }
    public int BIT_C(int A) { return ((A) & 0x00001000); }
    public int BIT_D(int A) { return ((A) & 0x00002000); }
    public int BIT_E(int A) { return ((A) & 0x00004000); }
    public int BIT_F(int A) { return ((A) & 0x00008000); }
    public int BIT_10(int A) { return ((A) & 0x00010000); }
    public int BIT_11(int A) { return ((A) & 0x00020000); }
    public int BIT_12(int A) { return ((A) & 0x00040000); }
    public int BIT_13(int A) { return ((A) & 0x00080000); }
    public int BIT_14(int A) { return ((A) & 0x00100000); }
    public int BIT_15(int A) { return ((A) & 0x00200000); }
    public int BIT_16(int A) { return ((A) & 0x00400000); }
    public int BIT_17(int A) { return ((A) & 0x00800000); }
    public int BIT_18(int A) { return ((A) & 0x01000000); }
    public int BIT_19(int A) { return ((A) & 0x02000000); }
    public int BIT_1A(int A) { return ((A) & 0x04000000); }
    public int BIT_1B(int A) { return ((A) & 0x08000000); }
    public int BIT_1C(int A) { return ((A) & 0x10000000); }
    public int BIT_1D(int A) { return ((A) & 0x20000000); }
    public int BIT_1E(int A) { return ((A) & 0x40000000); }
    public int BIT_1F(int A) { return ((A) & 0x80000000); }
    
    /* Get the most significant bit for specific sizes */
    public int GET_MSB_8(int A) { return ((A) & 0x80); }
    public int GET_MSB_9(int A) { return ((A) & 0x100); }
    public int GET_MSB_16(int A) { return ((A) & 0x8000); }
    public int GET_MSB_17(int A) { return ((A) & 0x10000); }
    public int GET_MSB_32(int A) { return ((A) & 0x80000000); }
    public long GET_MSB_33(long A) { return ((A) & 0x100000000L); }
    
    /* Isolate nibbles */
    public int LOW_NIBBLE(int A) { return ((A) & 0x0f); }
    public int HIGH_NIBBLE(int A) { return ((A) & 0xf0); }

    /* These are used to isolate 8, 16, and 32 bit sizes */
    public int MASK_OUT_ABOVE_2(int A) { return ((A) & 3); }
    public int MASK_OUT_ABOVE_8(int A) { return ((A) & 0xff); }
    public int MASK_OUT_ABOVE_16(int A) { return ((A) & 0xffff); }
    public int MASK_OUT_BELOW_2(int A) { return ((A) & ~3); }
    public int MASK_OUT_BELOW_8(int A) { return ((A) & ~0xff); }
    public int MASK_OUT_BELOW_16(int A) { return ((A) & ~0xffff); }
    public int MASK_OUT_ABOVE_32(int A) { return ((A) & 0xffffffff); }
    public int MASK_OUT_BELOW_32(int A) { return ((A) & ~0xffffffff); }

    /* Shift & Rotate Macros. */
    public int LSL(int A, int C) { return ((A) << (C)); }
    public int LSR(int A, int C) { return ((A) >> (C)); }

    public int ROL_8(int A, int C) { return MASK_OUT_ABOVE_8(LSL(A, C) | LSR(A, 8-(C))); }
    public int ROL_9(int A, int C) { return (LSL(A, C) | LSR(A, 9-(C))); }
    public int ROL_16(int A, int C) { return MASK_OUT_ABOVE_16(LSL(A, C) | LSR(A, 16-(C))); }
    public int ROL_17(int A, int C) { return (LSL(A, C) | LSR(A, 17-(C))); }
    public int ROL_32(int A, int C) { return MASK_OUT_ABOVE_32(LSL(A, C) | LSR(A, 32-(C))); }
    public int ROL_33(int A, int C) { return (LSL(A, C) | LSR(A, 33-(C))); }

    public int ROR_8(int A, int C) { return MASK_OUT_ABOVE_8(LSR(A, C) | LSL(A, 8-(C))); }
    public int ROR_9(int A, int C) { return (LSR(A, C) | LSL(A, 9-(C))); }
    public int ROR_16(int A, int C) { return MASK_OUT_ABOVE_16(LSR(A, C) | LSL(A, 16-(C))); }
    public int ROR_17(int A, int C) { return (LSR(A, C) | LSL(A, 17-(C))); }
    public int ROR_32(int A, int C) { return MASK_OUT_ABOVE_32(LSR(A, C) | LSL(A, 32-(C))); }
    public int ROR_33(int A, int C) { return (LSR(A, C) | LSL(A, 33-(C))); }
    
    public static class m68k_cpu_core {
        public int mode;                /* CPU Operation Mode: 68000, 68010, or 68020 */
        public int[] dr = new int[8];   /* Data Registers */
        public int[] ar = new int[8];   /* Address Registers */
        public int ppc;                 /* Previous program counter */
        public int pc;                  /* Program Counter */
        public int[] sp = new int[4];   /* User, Interrupt, and Master Stack Pointers */
        public int vbr;                 /* Vector Base Register (68010+) */
        public int sfc;                 /* Source Function Code Register (m68010+) */
        public int dfc;                 /* Destination Function Code Register (m68010+) */
        public int cacr;                /* Cache Control Register (m68020+) */
        public int caar;                /* Cacge Address Register (m68020+) */
        public int ir;                  /* Instruction Register */
        public int t1_flag;             /* Trace 1 */
        public int t0_flag;             /* Trace 0 */
        public int s_flag;              /* Supervisor */
        public int m_flag;              /* Master/Interrupt state */
        public int x_flag;              /* Extend */
        public int n_flag;              /* Negative */
        public int not_z_flag;          /* Zero, inverted for speedups */
        public int v_flag;              /* Overflow */
        public int c_flag;              /* Carry */
        public int int_mask;            /* I0-I2 */
        public int int_state;           /* Current interrupt state -- ASG: changed from ints_pending */
        public int stopped;             /* Stopped state */
        public int halted;              /* Halted state */
        public int int_cycles;          /* ASG: extra cycles from generated interrupts */
        public int pref_addr;           /* Last prefetch address */
        public int pref_data;           /* Data in the prefetch queue */

        /* Callbacks to host */
        irqcallbacksPtr int_ack_callback;  /* Interrupt Acknowledge */
        irqcallbacksPtr bkpt_ack_callback;     /* Breakpoint Acknowledge */
        irqcallbacksPtr reset_instr_callback;   /* Called when a RESET instruction is encountered */
        irqcallbacksPtr pc_changed_callback; /* Called when the PC changes by a large amount */
        irqcallbacksPtr set_fc_callback;     /* Called when the CPU function code changes */
        irqcallbacksPtr instr_hook_callback;       /* Called every instruction cycle prior to execution */
        
        public m68k_cpu_core()
        {
            
        }
        
        /* Access the CPU registers */
        public int get_CPU_MODE() { return mode; }
        public int[] get_CPU_D() { return dr; }
        public int[] get_CPU_A() { return ar; }
        public int get_CPU_PPC() { return ppc; }
        public int get_CPU_PC() { return pc; }
        public int[] get_CPU_SP() { return sp; }
        public int get_CPU_USP() { return sp[0]; }
        public int get_CPU_ISP() { return sp[1]; }
        public int get_CPU_MSP() { return sp[3]; }
        public int get_CPU_VBR() { return vbr; }
        public int get_CPU_SFC() { return sfc; }
        public int get_CPU_DFC() { return dfc; }
        public int get_CPU_CACR() { return cacr; }
        public int get_CPU_CAAR() { return caar; }
        public int get_CPU_IR() { return ir; }
        public int get_CPU_T1() { return t1_flag; }
        public int get_CPU_T0() { return t0_flag; }
        public int get_CPU_S() { return s_flag; }
        public int get_CPU_M() { return m_flag; }
        public int get_CPU_X() { return x_flag; }
        public int get_CPU_N() { return n_flag; }
        public int get_CPU_NOT_Z() { return not_z_flag; }
        public int get_CPU_V() { return v_flag; }
        public int get_CPU_C() { return c_flag; }
        public int get_CPU_INT_MASK() { return int_mask; }
        public int get_CPU_INT_STATE() { return int_state; }
        public int get_CPU_STOPPED() { return stopped; }
        public int get_CPU_HALTED() { return halted; }
        public int get_CPU_INT_CYCLES() { return int_cycles; }
        public int get_CPU_PREF_ADDR() { return pref_addr; }
        public int get_CPU_PREF_DATA() { return pref_data; } 
        
        public irqcallbacksPtr get_CPU_INT_ACK_CALLBACK() { return int_ack_callback; }
        public irqcallbacksPtr get_CPU_BKPT_ACK_CALLBACK() { return bkpt_ack_callback; }
        public irqcallbacksPtr get_CPU_RESET_INSTR_CALLBACK() { return reset_instr_callback; }
        public irqcallbacksPtr get_CPU_PC_CHANGED_CALLBACK() { return pc_changed_callback; }
        public irqcallbacksPtr get_CPU_SET_FC_CALLBACK() { return set_fc_callback; }
        public irqcallbacksPtr get_CPU_INSTR_HOOK_CALLBACK() { return instr_hook_callback; }
        
        public void set_CPU_MODE(int mode) { this.mode = mode; }
        public void set_CPU_D(int[] dr) { this.dr = dr; }
        public void set_CPU_A(int[] ar) { this.ar = ar; }
        public void set_CPU_PPC(int ppc) { this.ppc = ppc; }
        public void set_CPU_PC(int pc) { this.pc = pc; }
        public void set_CPU_SP(int[] sp) { this.sp = sp; }
        public void set_CPU_USP(int usp) { this.sp[0] = usp; }
        public void set_CPU_ISP(int isp) { this.sp[1] = isp; }
        public void set_CPU_MSP(int msp) { this.sp[3] = msp; }
        public void set_CPU_VBR(int vbr) { this.vbr = vbr; }
        public void set_CPU_SFC(int sfc) { this.sfc = sfc; }
        public void set_CPU_DFC(int dfc) { this.dfc = dfc; }
        public void set_CPU_CACR(int cacr) { this.cacr = cacr; }
        public void set_CPU_CAAR(int caar) { this.caar = caar; }
        public void set_CPU_IR(int ir) { this.ir = ir; }
        public void set_CPU_T1(int t1) { this.t1_flag = t1; }
        public void set_CPU_T0(int t0) { this.t0_flag = t0; }
        public void set_CPU_S(int s) { this.s_flag = s; }
        public void set_CPU_M(int m) { this.m_flag = m; }
        public void set_CPU_X(int x) { this.x_flag = x; }
        public void set_CPU_N(int n) { this.n_flag = n; }
        public void set_CPU_NOT_Z(int not_z) { this.not_z_flag = not_z; }
        public void set_CPU_V(int v) { this.v_flag = v; }
        public void set_CPU_C(int c) { this.c_flag = c; }
        public void set_CPU_INT_MASK(int int_mask) { this.int_mask = int_mask; }
        public void set_CPU_INT_STATE(int int_state) { this.int_state = int_state; }
        public void set_CPU_STOPPED(int stopped) { this.stopped = stopped; }
        public void set_CPU_HALTED(int halted) { this.halted = halted; }
        public void set_CPU_INT_CYCLES(int int_cycles) { this.int_cycles = int_cycles; }
        public void set_CPU_PREF_ADDR(int pref_addr) { this.pref_addr = pref_addr; }
        public void set_CPU_PREF_DATA(int pref_data) { this.pref_data = pref_data; } 
        
        public void set_CPU_INT_ACK_CALLBACK(irqcallbacksPtr int_ack_callback) {this.int_ack_callback = int_ack_callback; }
        public void set_CPU_BKPT_ACK_CALLBACK(irqcallbacksPtr bkpt_ack_callback) { this.bkpt_ack_callback = bkpt_ack_callback; }
        public void set_CPU_RESET_INSTR_CALLBACK(irqcallbacksPtr reset_instr_callback) { this.reset_instr_callback = reset_instr_callback; }
        public void set_CPU_PC_CHANGED_CALLBACK(irqcallbacksPtr pc_changed_callback) { this.pc_changed_callback = pc_changed_callback; }
        public void set_CPU_SET_FC_CALLBACK(irqcallbacksPtr set_fc_callback) { this.set_fc_callback = set_fc_callback; }
        public void set_CPU_INSTR_HOOK_CALLBACK(irqcallbacksPtr instr_hook_callback) { this.instr_hook_callback = instr_hook_callback; }
    }
}
