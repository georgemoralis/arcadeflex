package cpu.m68000;

import cpu.m68000.m68kH.*;
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
    
    /* M68K CPU core class */
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
        bkpt_ack_callbackPtr bkpt_ack_callback;     /* Breakpoint Acknowledge */
        reset_instr_callbackPtr reset_instr_callback;   /* Called when a RESET instruction is encountered */
        pc_changed_callbackPtr pc_changed_callback; /* Called when the PC changes by a large amount */
        set_fc_callbackPtr set_fc_callback;     /* Called when the CPU function code changes */
        instr_hook_callbackPtr instr_hook_callback;       /* Called every instruction cycle prior to execution */

        
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
        public bkpt_ack_callbackPtr get_CPU_BKPT_ACK_CALLBACK() { return bkpt_ack_callback; }
        public reset_instr_callbackPtr get_CPU_RESET_INSTR_CALLBACK() { return reset_instr_callback; }
        public pc_changed_callbackPtr get_CPU_PC_CHANGED_CALLBACK() { return pc_changed_callback; }
        public set_fc_callbackPtr get_CPU_SET_FC_CALLBACK() { return set_fc_callback; }
        public instr_hook_callbackPtr get_CPU_INSTR_HOOK_CALLBACK() { return instr_hook_callback; }
        
        public void set_CPU_MODE(int mode) { this.mode = mode; }
        public void set_CPU_D(int reg_num, int value) { this.dr[reg_num] = value; }
        public void set_CPU_A(int reg_num, int value) { this.ar[reg_num] = value; }
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
        public void set_CPU_BKPT_ACK_CALLBACK(bkpt_ack_callbackPtr bkpt_ack_callback) { this.bkpt_ack_callback = bkpt_ack_callback; }
        public void set_CPU_RESET_INSTR_CALLBACK(reset_instr_callbackPtr reset_instr_callback) { this.reset_instr_callback = reset_instr_callback; }
        public void set_CPU_PC_CHANGED_CALLBACK(pc_changed_callbackPtr pc_changed_callback) { this.pc_changed_callback = pc_changed_callback; }
        public void set_CPU_SET_FC_CALLBACK(set_fc_callbackPtr set_fc_callback) { this.set_fc_callback = set_fc_callback; }
        public void set_CPU_INSTR_HOOK_CALLBACK(instr_hook_callbackPtr instr_hook_callback) { this.instr_hook_callback = instr_hook_callback; }
    
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

        /* Simulate address lines of 68k family */
        public int ADDRESS_68K(int A) { 
            if ((this.get_CPU_MODE() & CPU_TYPE_020) == 0) return A; else return ((A) & 0xffffff);
        }

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
        
        /* Service an interrupt request */
        public void service_interrupt(int pending_mask)	/* ASG: added parameter here */
        {
            int int_level = 7;
            int vector = 0;

            /* Start at level 7 and then go down */
            for(;(pending_mask & (1<<int_level)) == 0;int_level--)	/* ASG: changed to use parameter instead of CPU_INTS_PENDING */
            ;

            /* Get the exception vector */
            switch(vector /*= int_ack(int_level)*/)
            {
               case 0x00: case 0x01:
               /* vectors 0 and 1 are ignored since they are for reset only */
                  return;
               case 0x02: case 0x03: case 0x04: case 0x05: case 0x06: case 0x07:
               case 0x08: case 0x09: case 0x0a: case 0x0b: case 0x0c: case 0x0d: case 0x0e: case 0x0f:
               case 0x10: case 0x11: case 0x12: case 0x13: case 0x14: case 0x15: case 0x16: case 0x17:
               case 0x18: case 0x19: case 0x1a: case 0x1b: case 0x1c: case 0x1d: case 0x1e: case 0x1f:
               case 0x20: case 0x21: case 0x22: case 0x23: case 0x24: case 0x25: case 0x26: case 0x27:
               case 0x28: case 0x29: case 0x2a: case 0x2b: case 0x2c: case 0x2d: case 0x2e: case 0x2f:
               case 0x30: case 0x31: case 0x32: case 0x33: case 0x34: case 0x35: case 0x36: case 0x37:
               case 0x38: case 0x39: case 0x3a: case 0x3b: case 0x3c: case 0x3d: case 0x3e: case 0x3f:
               case 0x40: case 0x41: case 0x42: case 0x43: case 0x44: case 0x45: case 0x46: case 0x47:
               case 0x48: case 0x49: case 0x4a: case 0x4b: case 0x4c: case 0x4d: case 0x4e: case 0x4f:
               case 0x50: case 0x51: case 0x52: case 0x53: case 0x54: case 0x55: case 0x56: case 0x57:
               case 0x58: case 0x59: case 0x5a: case 0x5b: case 0x5c: case 0x5d: case 0x5e: case 0x5f:
               case 0x60: case 0x61: case 0x62: case 0x63: case 0x64: case 0x65: case 0x66: case 0x67:
               case 0x68: case 0x69: case 0x6a: case 0x6b: case 0x6c: case 0x6d: case 0x6e: case 0x6f:
               case 0x70: case 0x71: case 0x72: case 0x73: case 0x74: case 0x75: case 0x76: case 0x77:
               case 0x78: case 0x79: case 0x7a: case 0x7b: case 0x7c: case 0x7d: case 0x7e: case 0x7f:
               case 0x80: case 0x81: case 0x82: case 0x83: case 0x84: case 0x85: case 0x86: case 0x87:
               case 0x88: case 0x89: case 0x8a: case 0x8b: case 0x8c: case 0x8d: case 0x8e: case 0x8f:
               case 0x90: case 0x91: case 0x92: case 0x93: case 0x94: case 0x95: case 0x96: case 0x97:
               case 0x98: case 0x99: case 0x9a: case 0x9b: case 0x9c: case 0x9d: case 0x9e: case 0x9f:
               case 0xa0: case 0xa1: case 0xa2: case 0xa3: case 0xa4: case 0xa5: case 0xa6: case 0xa7:
               case 0xa8: case 0xa9: case 0xaa: case 0xab: case 0xac: case 0xad: case 0xae: case 0xaf:
               case 0xb0: case 0xb1: case 0xb2: case 0xb3: case 0xb4: case 0xb5: case 0xb6: case 0xb7:
               case 0xb8: case 0xb9: case 0xba: case 0xbb: case 0xbc: case 0xbd: case 0xbe: case 0xbf:
               case 0xc0: case 0xc1: case 0xc2: case 0xc3: case 0xc4: case 0xc5: case 0xc6: case 0xc7:
               case 0xc8: case 0xc9: case 0xca: case 0xcb: case 0xcc: case 0xcd: case 0xce: case 0xcf:
               case 0xd0: case 0xd1: case 0xd2: case 0xd3: case 0xd4: case 0xd5: case 0xd6: case 0xd7:
               case 0xd8: case 0xd9: case 0xda: case 0xdb: case 0xdc: case 0xdd: case 0xde: case 0xdf:
               case 0xe0: case 0xe1: case 0xe2: case 0xe3: case 0xe4: case 0xe5: case 0xe6: case 0xe7:
               case 0xe8: case 0xe9: case 0xea: case 0xeb: case 0xec: case 0xed: case 0xee: case 0xef:
               case 0xf0: case 0xf1: case 0xf2: case 0xf3: case 0xf4: case 0xf5: case 0xf6: case 0xf7:
               case 0xf8: case 0xf9: case 0xfa: case 0xfb: case 0xfc: case 0xfd: case 0xfe: case 0xff:
                  /* The external peripheral has provided the interrupt vector to take */
                  break;
               //case M68K_INT_ACK_AUTOVECTOR:
                  /* Use the autovectors.  This is the most commonly used implementation */
                  //vector = EXCEPTION_INTERRUPT_AUTOVECTOR+int_level;
                 // break;
               //case M68K_INT_ACK_SPURIOUS:
                  /* Called if no devices respond to the interrupt acknowledge */
                  //vector = EXCEPTION_SPURIOUS_INTERRUPT;
                  //break;
               default:
                  /* Everything else is ignored */
                  return;
            }

            /* If vector is uninitialized, call the uninitialized interrupt vector */
            //if(read_32(vector<<2) == 0)
              // vector = EXCEPTION_UNINITIALIZED_INTERRUPT;

            /* Generate an interupt */
            //interrupt(vector);

            /* Set the interrupt mask to the level of the one being serviced */
            this.set_CPU_INT_MASK(int_level);
        }


        /* ASG: Check for interrupts */
        public void check_interrupts()
        {
           int pending_mask = 1 << this.get_CPU_INT_STATE();
           //if (pending_mask & int_masks[this.get_CPU_INT_MASK()])
              service_interrupt(pending_mask);
        }
    }
}
