package cpu.m68000;

import static cpu.m68000.m68kH.*;
import static mame.cpuintrfH.*;
import static cpu.m68000.m68kcpu.*;
import static cpu.m68000.m68kmameH.*;
import static mame.memoryH.*;
import static arcadeflex.libc_old.*;

public class m68kcpuH {
    /*TODO*////* Check if we have certain storage sizes */
/*TODO*///
/*TODO*///#if UCHAR_MAX == 0xff
/*TODO*///#define M68K_HAS_8_BIT_SIZE  1
/*TODO*///#else
/*TODO*///#define M68K_HAS_8_BIT_SIZE  0
/*TODO*///#endif /* UCHAR_MAX == 0xff */
/*TODO*///
/*TODO*///#if USHRT_MAX == 0xffff
/*TODO*///#define M68K_HAS_16_BIT_SIZE 1
/*TODO*///#else
/*TODO*///#define M68K_HAS_16_BIT_SIZE 0
/*TODO*///#endif /* USHRT_MAX == 0xffff */
/*TODO*///
/*TODO*///#if ULONG_MAX == 0xffffffff
/*TODO*///#define M68K_HAS_32_BIT_SIZE 1
/*TODO*///#else
/*TODO*///#define M68K_HAS_32_BIT_SIZE 0
/*TODO*///#endif /* ULONG_MAX == 0xffffffff */
/*TODO*///
/*TODO*///#if UINT_MAX > 0xffffffff
/*TODO*///#define M68K_OVER_32_BIT     1
/*TODO*///#else
/*TODO*///#define M68K_OVER_32_BIT     0
/*TODO*///#endif /* UINT_MAX > 0xffffffff */
/*TODO*///
/*TODO*////* Data types used in this emulation core */
/*TODO*///#undef int8
/*TODO*///#undef int16
/*TODO*///#undef int32
/*TODO*///#undef uint
/*TODO*///#undef uint8
/*TODO*///#undef uint16
/*TODO*///#undef uint
/*TODO*///
/*TODO*///#define int8   signed char			/* ASG: changed from char to signed char */
/*TODO*///#define uint8  unsigned char
/*TODO*///#define int16  short
/*TODO*///#define uint16 unsigned short
/*TODO*///#define int32  long
/*TODO*///
/*TODO*////* int and unsigned int must be at least 32 bits wide */
/*TODO*///#define uint   unsigned int
/*TODO*///
/*TODO*///
/*TODO*////* Allow for architectures that don't have 8-bit sizes */
/*TODO*///#if M68K_HAS_8_BIT_SIZE
/*TODO*///#define MAKE_INT_8(A) (int8)((A)&0xff)

    public static long MAKE_INT_8(long A) {
        return (byte) ((A) & 0xffL);
    }
    /*TODO*///#else
/*TODO*///#undef  int8
/*TODO*///#define int8   int
/*TODO*///#undef  uint8
/*TODO*///#define uint8  unsigned int
/*TODO*///INLINE int MAKE_INT_8(int value)
/*TODO*///{
/*TODO*///   /* Will this work for 1's complement machines? */
/*TODO*///   return (value & 0x80) ? value | ~0xff : value & 0xff;
/*TODO*///}
/*TODO*///#endif /* M68K_HAS_8_BIT_SIZE */
/*TODO*///
/*TODO*///
/*TODO*////* Allow for architectures that don't have 16-bit sizes */
/*TODO*///#if M68K_HAS_16_BIT_SIZE
/*TODO*///#define MAKE_INT_16(A) (int16)((A)&0xffff)

    public static long MAKE_INT_16(long A) {
        return (short) ((A) & 0xffffL);
    }
    /*TODO*///#else
/*TODO*///#undef  int16
/*TODO*///#define int16  int
/*TODO*///#undef  uint16
/*TODO*///#define uint16 unsigned int
/*TODO*///INLINE int MAKE_INT_16(int value)
/*TODO*///{
/*TODO*///   /* Will this work for 1's complement machines? */
/*TODO*///   return (value & 0x8000) ? value | ~0xffff : value & 0xffff;
/*TODO*///}
/*TODO*///#endif /* M68K_HAS_16_BIT_SIZE */
/*TODO*///
/*TODO*///
/*TODO*////* Allow for architectures that don't have 32-bit sizes */
/*TODO*///#if M68K_HAS_32_BIT_SIZE
/*TODO*///#if M68K_OVER_32_BIT
/*TODO*///#define MAKE_INT_32(A) (int32)((A)&0xffffffff)

    public static long MAKE_INT_32(long A) {
        return (int) ((A) & 0xffffffffL);
    }
    /*TODO*///#else
/*TODO*///#define MAKE_INT_32(A) (int32)(A)
/*TODO*///#endif /* M68K_OVER_32_BIT */
/*TODO*///#else
/*TODO*///#undef  int32
/*TODO*///#define int32  int
/*TODO*///INLINE int MAKE_INT_32(int value)
/*TODO*///{
/*TODO*///   /* Will this work for 1's complement machines? */
/*TODO*///   return (value & 0x80000000) ? value | ~0xffffffff : value & 0xffffffff;
/*TODO*///}
/*TODO*///#endif /* M68K_HAS_32_BIT_SIZE */
/*TODO*///
/*TODO*///
/*TODO*///
    /* ======================================================================== */
    /* ============================ GENERAL DEFINES =========================== */
    /* ======================================================================== */
    /* Exception Vectors handled by emulation */

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


    /* CPU modes for deciding what to emulate */
    public static int CPU_MODE_000 = M68K_CPU_MODE_68000;
    public static int CPU_MODE_010 = M68K_CPU_MODE_68010;
    public static int CPU_MODE_EC020 = M68K_CPU_MODE_68EC020;
    public static int CPU_MODE_020 = M68K_CPU_MODE_68020;

    public static int CPU_MODE_ALL = (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020);
    public static int CPU_MODE_010_PLUS = (CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020);
    public static int CPU_MODE_010_LESS = (CPU_MODE_000 | CPU_MODE_010);
    public static int CPU_MODE_EC020_PLUS = (CPU_MODE_EC020 | CPU_MODE_020);
    public static int CPU_MODE_EC020_LESS = (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020);
    public static int CPU_MODE_020_PLUS = CPU_MODE_020;
    public static int CPU_MODE_020_LESS = (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020);
    /*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ================================ MACROS ================================ */
/*TODO*////* ======================================================================== */
/*TODO*///
/* Bit Isolation Macros */

    public static long BIT_0(long A) {
        return ((A) & 0x00000001L);
    }

    public static long BIT_1(long A) {
        return ((A) & 0x00000002L);
    }

    public static long BIT_2(long A) {
        return ((A) & 0x00000004L);
    }

    public static long BIT_3(long A) {
        return ((A) & 0x00000008L);
    }

    public static long BIT_4(long A) {
        return ((A) & 0x00000010L);
    }

    public static long BIT_5(long A) {
        return ((A) & 0x00000020L);
    }

    public static long BIT_6(long A) {
        return ((A) & 0x00000040L);
    }

    public static long BIT_7(long A) {
        return ((A) & 0x00000080L);
    }

    public static long BIT_8(long A) {
        return ((A) & 0x00000100L);
    }

    public static long BIT_9(long A) {
        return ((A) & 0x00000200L);
    }

    public static long BIT_A(long A) {
        return ((A) & 0x00000400L);
    }

    public static long BIT_B(long A) {
        return ((A) & 0x00000800L);
    }

    public static long BIT_C(long A) {
        return ((A) & 0x00001000L);
    }

    public static long BIT_D(long A) {
        return ((A) & 0x00002000L);
    }

    public static long BIT_E(long A) {
        return ((A) & 0x00004000L);
    }

    public static long BIT_F(long A) {
        return ((A) & 0x00008000L);
    }
    /*TODO*///#define BIT_10(A) ((A) & 0x00010000)
/*TODO*///#define BIT_11(A) ((A) & 0x00020000)
/*TODO*///#define BIT_12(A) ((A) & 0x00040000)
/*TODO*///#define BIT_13(A) ((A) & 0x00080000)
/*TODO*///#define BIT_14(A) ((A) & 0x00100000)
/*TODO*///#define BIT_15(A) ((A) & 0x00200000)
/*TODO*///#define BIT_16(A) ((A) & 0x00400000)
/*TODO*///#define BIT_17(A) ((A) & 0x00800000)
/*TODO*///#define BIT_18(A) ((A) & 0x01000000)
/*TODO*///#define BIT_19(A) ((A) & 0x02000000)
/*TODO*///#define BIT_1A(A) ((A) & 0x04000000)
/*TODO*///#define BIT_1B(A) ((A) & 0x08000000)
/*TODO*///#define BIT_1C(A) ((A) & 0x10000000)
/*TODO*///#define BIT_1D(A) ((A) & 0x20000000)
/*TODO*///#define BIT_1E(A) ((A) & 0x40000000)
/*TODO*///#define BIT_1F(A) ((A) & 0x80000000)
/*TODO*///
/*TODO*////* Get the most significant bit for specific sizes */
/*TODO*///#define GET_MSB_8(A)  ((A) & 0x80)

    public static long GET_MSB_8(long A) {
        return ((A) & 0x80L);
    }
    /*TODO*///#define GET_MSB_9(A)  ((A) & 0x100)
/*TODO*///#define GET_MSB_16(A) ((A) & 0x8000)

    public static long GET_MSB_16(long A) {
        return ((A) & 0x8000L);
    }
    /*TODO*///#define GET_MSB_17(A) ((A) & 0x10000)
/*TODO*///#define GET_MSB_32(A) ((A) & 0x80000000)

    public static long GET_MSB_32(long A) {
        return ((A) & 0x80000000L);
    }
    /*TODO*///
/*TODO*////* Isolate nibbles */

    public static long LOW_NIBBLE(long A) {
        return ((A) & 0x0fL);
    }

    public static long HIGH_NIBBLE(long A) {
        return ((A) & 0xf0L);
    }
    /*TODO*///
/*TODO*////* These are used to isolate 8, 16, and 32 bit sizes */
/*TODO*///#define MASK_OUT_ABOVE_2(A)  ((A) & 3)  

    public static long MASK_OUT_ABOVE_8(long A) {
        return ((A) & 0xffL);
    }

    public static long MASK_OUT_ABOVE_16(long A) {
        return ((A) & 0xffffL);
    }
    /*TODO*///#define MASK_OUT_BELOW_2(A)  ((A) & ~3)

    public static long MASK_OUT_BELOW_2(long A) {
        return ((A) & ~3);
    }
    /*TODO*///#define MASK_OUT_BELOW_8(A)  ((A) & ~0xff)

    public static long MASK_OUT_BELOW_8(long A) {
        return ((A) & ~0xffL);
    }
    /*TODO*///#define MASK_OUT_BELOW_16(A) ((A) & ~0xffff)

    public static long MASK_OUT_BELOW_16(long A) {
        return ((A) & ~0xffffL);
    }
    /*TODO*///
/*TODO*////* No need for useless masking if we're 32-bit */
/*TODO*///#if M68K_OVER_32_BIT
/*TODO*///#define MASK_OUT_ABOVE_32(A) ((A) & 0xffffffff)

    public static long MASK_OUT_ABOVE_32(long A) {
        return ((A) & 0xffffffffL);
    }
    /*TODO*///#define MASK_OUT_BELOW_32(A) ((A) & ~0xffffffff)
/*TODO*///#else
/*TODO*///#define MASK_OUT_ABOVE_32(A) (A)
/*TODO*///#define MASK_OUT_BELOW_32(A) 0
/*TODO*///#endif /* M68K_OVER_32_BIT */
/*TODO*///
/*TODO*///
/*TODO*////* Simulate address lines of 68k family */
/*TODO*///#define ADDRESS_68K(A) (CPU_MODE & CPU_MODE_020_PLUS ? A : (A)&0xffffff)

    public static long ADDRESS_68K(long A) {
        if ((get_CPU_MODE() & CPU_MODE_020_PLUS) != 0) {
            return A;
        } else {
            return A & 0xffffffL;
        }
    }
    /* Instruction extension word information for indexed addressing modes */

    public static long EXT_INDEX_LONG(long A) {
        return BIT_B(A);
    }

    public static long EXT_INDEX_AR(long A) {
        return BIT_F(A);
    }

    public static long EXT_INDEX_REGISTER(long A) {
        return (((A) >>> 12) & 7);
    }
    /*TODO*///#define EXT_INDEX_SCALE(A)        (((A)>>9)&3)
/*TODO*///#define EXT_BRIEF_FORMAT(A)       !BIT_8(A)
/*TODO*///#define EXT_IX_SUPPRESSED(A)      BIT_6(A)
/*TODO*///#define EXT_BR_SUPPRESSED(A)      BIT_7(A)
/*TODO*///#define EXT_BD_PRESENT(A)         BIT_5(A)
/*TODO*///#define EXT_BD_LONG(A)            BIT_4(A)
/*TODO*///#define EXT_NO_MEMORY_INDIRECT(A) !((A)&7)
/*TODO*///#define EXT_OD_PRESENT(A)         BIT_1(A)
/*TODO*///#define EXT_OD_LONG(A)            BIT_0(A)
/*TODO*///#define EXT_POSTINDEX(A)          BIT_2(A)
/*TODO*///
/*TODO*///
/*TODO*////* Shift & Rotate Macros.
/*TODO*/// * 32-bit shifts defined in architecture-dependant section.
/*TODO*/// */
/*TODO*///

    public static long LSL(long A, long C) {
        return ((A) << (C));
    }

    public static long LSR(long A, long C) {
        return ((A) >>> (C));
    }
    /*TODO*///
/*TODO*////* Some > 32-bit optimizations */
/*TODO*///#if M68K_OVER_32_BIT
/*TODO*////* Shift left and right */

    public static long LSR_32(long A, long C) {
        return ((A) >>> (C));
    }

    public static long LSL_32(long A, long C) {
        return ((A) << (C));
    }
    /*TODO*///#else
/*TODO*////* We have to do this because the morons at ANSI decided that shifts
/*TODO*/// * by >= data size are undefined.
/*TODO*/// */
/*TODO*///#define LSR_32(A, C) ((C) < 32 ? (A) >> (C) : 0)
/*TODO*///#define LSL_32(A, C) ((C) < 32 ? (A) << (C) : 0)
/*TODO*///#endif /* M68K_OVER_32_BIT */
/*TODO*///

    public static long ROL_8(long A, long C) {
        return MASK_OUT_ABOVE_8(LSL(A, C) | LSR(A, 8 - (C)));
    }
    /*TODO*///#define ROL_9(A, C)                       LSL(A, C) | LSR(A, 9-(C))

    public static long ROL_16(long A, long C) {
        return MASK_OUT_ABOVE_16(LSL(A, C) | LSR(A, 16 - (C)));
    }
    /*TODO*///#define ROL_17(A, C)                      LSL(A, C) | LSR(A, 17-(C))

    public static long ROL_32(long A, long C) {
        return MASK_OUT_ABOVE_32(LSL_32(A, C) | LSR_32(A, 32 - (C)));
    }
    /*TODO*///#define ROL_33(A, C)                  (LSL_32(A, C) | LSR_32(A, 33-(C)))
/*TODO*///

    public static long ROR_8(long A, long C) {
        return MASK_OUT_ABOVE_8(LSR(A, C) | LSL(A, 8 - (C)));
    }
    /*TODO*///#define ROR_9(A, C)                       LSR(A, C) | LSL(A, 9-(C))

    public static long ROR_16(long A, long C) {
        return MASK_OUT_ABOVE_16(LSR(A, C) | LSL(A, 16 - (C)));
    }
    /*TODO*///#define ROR_17(A, C)                      LSR(A, C) | LSL(A, 17-(C))

    public static long ROR_32(long A, long C) {
        return MASK_OUT_ABOVE_32(LSR_32(A, C) | LSL_32(A, 32 - (C)));
    }
    /*TODO*///#define ROR_33(A, C)                  (LSR_32(A, C) | LSL_32(A, 33-(C)))

    /* Access the CPU registers */
    public static long get_CPU_MODE() {
        return m68k_cpu.mode;
    }

    public static void set_CPU_MODE(long mode) {
        m68k_cpu.mode = mode;
    }

    public static long[] get_CPU_D() {
        return m68k_cpu.dr;
    }

    public static void set_CPU_D(int reg_num, long value) {
        m68k_cpu.dr[reg_num] = value;
    }

    public static long[] get_CPU_A() {
        return m68k_cpu.ar;
    }

    public static void set_CPU_A(int reg_num, long value) {
        m68k_cpu.ar[reg_num] = value;
    }

    public static long get_CPU_PPC() {
        return m68k_cpu.ppc;
    }

    public static void set_CPU_PPC(long ppc) {
        m68k_cpu.ppc = ppc;
    }

    public static long get_CPU_PC() {
        return m68k_cpu.pc;
    }

    public static void set_CPU_PC(long pc) {
        m68k_cpu.pc = pc;
    }

    public static long[] get_CPU_SP() {
        return m68k_cpu.sp;
    }

    public static void set_CPU_SP(int reg_num, long value) {
        m68k_cpu.sp[reg_num] = value;
    }

    public static long get_CPU_USP() {
        return m68k_cpu.sp[0];
    }

    public static void set_CPU_USP(long usp) {
        m68k_cpu.sp[0] = usp;
    }

    public static long get_CPU_ISP() {
        return m68k_cpu.sp[1];
    }

    public static void set_CPU_ISP(long isp) {
        m68k_cpu.sp[1] = isp;
    }

    public static long get_CPU_MSP() {
        return m68k_cpu.sp[3];
    }

    public static void set_CPU_MSP(long msp) {
        m68k_cpu.sp[3] = msp;
    }

    public static long get_CPU_VBR() {
        return m68k_cpu.vbr;
    }

    public static void set_CPU_VBR(long vbr) {
        m68k_cpu.vbr = vbr;
    }

    public static long get_CPU_SFC() {
        return m68k_cpu.sfc;
    }

    public static void set_CPU_SFC(long sfc) {
        m68k_cpu.sfc = sfc;
    }

    public static long get_CPU_DFC() {
        return m68k_cpu.dfc;
    }

    public static void set_CPU_DFC(long dfc) {
        m68k_cpu.dfc = dfc;
    }

    public static long get_CPU_CACR() {
        return m68k_cpu.cacr;
    }

    public static void set_CPU_CACR(long cacr) {
        m68k_cpu.cacr = cacr;
    }

    public static long get_CPU_CAAR() {
        return m68k_cpu.caar;
    }

    public static void set_CPU_CAAR(long caar) {
        m68k_cpu.caar = caar;
    }

    public static long get_CPU_IR() {
        return m68k_cpu.ir;
    }

    public static void set_CPU_IR(long ir) {
        m68k_cpu.ir = ir;
    }

    public static long get_CPU_T1() {
        return m68k_cpu.t1_flag;
    }

    public static void set_CPU_T1(long t1) {
        m68k_cpu.t1_flag = t1;
    }

    public static long get_CPU_T0() {
        return m68k_cpu.t0_flag;
    }

    public static void set_CPU_T0(long t0) {
        m68k_cpu.t0_flag = t0;
    }

    public static long get_CPU_S() {
        return m68k_cpu.s_flag;
    }

    public static void set_CPU_S(long s) {
        m68k_cpu.s_flag = s;
    }

    public static long get_CPU_M() {
        return m68k_cpu.m_flag;
    }

    public static void set_CPU_M(long m) {
        m68k_cpu.m_flag = m;
    }

    public static long get_CPU_X() {
        return m68k_cpu.x_flag;
    }

    public static void set_CPU_X(long x) {
        m68k_cpu.x_flag = x;
    }

    public static long get_CPU_N() {
        return m68k_cpu.n_flag;
    }

    public static void set_CPU_N(long n) {
        m68k_cpu.n_flag = n;
    }

    public static long get_CPU_NOT_Z() {
        return m68k_cpu.not_z_flag;
    }

    public static void set_CPU_NOT_Z(long not_z) {
        m68k_cpu.not_z_flag = not_z;
    }

    public static long get_CPU_V() {
        return m68k_cpu.v_flag;
    }

    public static void set_CPU_V(long v) {
        m68k_cpu.v_flag = v;
    }

    public static long get_CPU_C() {
        return m68k_cpu.c_flag;
    }

    public static void set_CPU_C(long c) {
        m68k_cpu.c_flag = c;
    }

    public static long get_CPU_INT_MASK() {
        return m68k_cpu.int_mask;
    }

    public static void set_CPU_INT_MASK(long int_mask) {
        m68k_cpu.int_mask = int_mask;
    }

    public static long get_CPU_INT_STATE() {
        return m68k_cpu.int_state;
    }

    public static void set_CPU_INT_STATE(long int_state) {
        m68k_cpu.int_state = int_state;
    }

    public static long get_CPU_STOPPED() {
        return m68k_cpu.stopped;
    }

    public static void set_CPU_STOPPED(long stopped) {
        m68k_cpu.stopped = stopped;
    }

    public static long get_CPU_HALTED() {
        return m68k_cpu.halted;
    }

    public static void set_CPU_HALTED(long halted) {
        m68k_cpu.halted = halted;
    }

    public static long get_CPU_INT_CYCLES() {
        return m68k_cpu.int_cycles;
    }

    public static void set_CPU_INT_CYCLES(long int_cycles) {
        m68k_cpu.int_cycles = int_cycles;
    }

    public static long get_CPU_PREF_ADDR() {
        return m68k_cpu.pref_addr;
    }

    public static void set_CPU_PREF_ADDR(long pref_addr) {
        m68k_cpu.pref_addr = pref_addr;
    }

    public static long get_CPU_PREF_DATA() {
        return m68k_cpu.pref_data;
    }

    public static void set_CPU_PREF_DATA(long pref_data) {
        m68k_cpu.pref_data = pref_data;
    }

    public static irqcallbacksPtr get_CPU_INT_ACK_CALLBACK() {
        return m68k_cpu.int_ack_callback;
    }

    public static void set_CPU_INT_ACK_CALLBACK(irqcallbacksPtr int_ack_callback) {
        m68k_cpu.int_ack_callback = int_ack_callback;
    }

    public static bkpt_ack_callbackPtr get_CPU_BKPT_ACK_CALLBACK() {
        return m68k_cpu.bkpt_ack_callback;
    }

    public static void set_CPU_BKPT_ACK_CALLBACK(bkpt_ack_callbackPtr bkpt_ack_callback) {
        m68k_cpu.bkpt_ack_callback = bkpt_ack_callback;
    }

    public static reset_instr_callbackPtr get_CPU_RESET_INSTR_CALLBACK() {
        return m68k_cpu.reset_instr_callback;
    }

    public static void set_CPU_RESET_INSTR_CALLBACK(reset_instr_callbackPtr reset_instr_callback) {
        m68k_cpu.reset_instr_callback = reset_instr_callback;
    }

    public static pc_changed_callbackPtr get_CPU_PC_CHANGED_CALLBACK() {
        return m68k_cpu.pc_changed_callback;
    }

    public static void set_CPU_PC_CHANGED_CALLBACK(pc_changed_callbackPtr pc_changed_callback) {
        m68k_cpu.pc_changed_callback = pc_changed_callback;
    }

    public static set_fc_callbackPtr get_CPU_SET_FC_CALLBACK() {
        return m68k_cpu.set_fc_callback;
    }

    public static void set_CPU_SET_FC_CALLBACK(set_fc_callbackPtr set_fc_callback) {
        m68k_cpu.set_fc_callback = set_fc_callback;
    }

    public static instr_hook_callbackPtr get_CPU_INSTR_HOOK_CALLBACK() {
        return m68k_cpu.instr_hook_callback;
    }

    public static void set_CPU_INSTR_HOOK_CALLBACK(instr_hook_callbackPtr instr_hook_callback) {
        m68k_cpu.instr_hook_callback = instr_hook_callback;
    }

    /*TODO*////*
/*TODO*/// * The general instruction format follows this pattern:
/*TODO*/// * .... XXX. .... .YYY
/*TODO*/// * where XXX is register X and YYY is register Y
/*TODO*/// */
/*TODO*////* Data Register Isolation */
/*TODO*///#define DX (CPU_D[(CPU_IR >> 9) & 7])
    public static long get_DX() {
        return get_CPU_D()[(int) ((get_CPU_IR() >>> 9) & 7)];
    }

    public static void set_DX(long value) {
        set_CPU_D((int) ((get_CPU_IR() >>> 9) & 7), value);
    }
    /*TODO*///#define DY (CPU_D[CPU_IR & 7])

    public static long get_DY() {
        return get_CPU_D()[(int) (get_CPU_IR() & 7)];
    }

    public static void set_DY(long value) {
        set_CPU_D((int) (get_CPU_IR() & 7), value);
    }
    /*TODO*////* Address Register Isolation */

    public static long get_AX() {
        return get_CPU_A()[(int) ((get_CPU_IR() >>> 9) & 7)];
    }

    public static void set_AX(long value) {
        set_CPU_A((int) ((get_CPU_IR() >>> 9) & 7), value);
    }
    /*TODO*///#define AX (CPU_A[(CPU_IR >> 9) & 7])

    public static long get_AY() {
        return get_CPU_A()[(int) (get_CPU_IR() & 7)];
    }

    public static void set_AY(long value) {
        set_CPU_A((int) (get_CPU_IR() & 7), value);
    }
    /*TODO*///#define AY (CPU_A[CPU_IR & 7])
/*TODO*///
/*TODO*///
/*TODO*////* Effective Address Calculations */
/*TODO*///#define EA_AI    AY                                    /* address register indirect */

    public static long EA_AI() {
        return get_CPU_A()[(int) (get_CPU_IR() & 7)];
    }
    /*TODO*///#define EA_PI_8  (AY++)                                /* postincrement (size = byte) */

    public static long EA_PI_8() {
        long tmp = get_AY();
        set_CPU_A((int) (get_CPU_IR() & 7), (get_AY() + 1) & 0xFFFFFFFFL);
        return tmp;
    }
    /*TODO*///#define EA_PI7_8 ((CPU_A[7]+=2)-2)                     /* postincrement (size = byte & AR = 7) */
/*TODO*///#define EA_PI_16 ((AY+=2)-2)                           /* postincrement (size = word) */

    public static long EA_PI_16() {
        set_CPU_A((int) (get_CPU_IR() & 7), get_CPU_A()[(int) (get_CPU_IR() & 7)] + 2);
        return (get_CPU_A()[(int) (get_CPU_IR() & 7)] - 2) & 0xFFFFFFFFL;
    }
    /*TODO*///#define EA_PI_32 ((AY+=4)-4)                           /* postincrement (size = long) */

    public static long EA_PI_32() {
        set_CPU_A((int) (get_CPU_IR() & 7), get_CPU_A()[(int) (get_CPU_IR() & 7)] + 4);
        return (get_CPU_A()[(int) (get_CPU_IR() & 7)] - 4) & 0xFFFFFFFFL;
    }
    /*TODO*///#define EA_PD_8  (--AY)                                /* predecrement (size = byte) */

    public static long EA_PD_8() {
        set_CPU_A((int) (get_CPU_IR() & 7), (get_CPU_A()[(int) (get_CPU_IR() & 7)] - 1) & 0xFFFFFFFFL);
        return get_AY();
    }
    /*TODO*///#define EA_PD7_8 (CPU_A[7]-=2)                         /* predecrement (size = byte & AR = 7) */
/*TODO*///#define EA_PD_16 (AY-=2)                               /* predecrement (size = word) */

    public static long EA_PD_16() {
        set_CPU_A((int) (get_CPU_IR() & 7), (get_CPU_A()[(int) (get_CPU_IR() & 7)] - 2) & 0xFFFFFFFFL);
        return get_AY();
    }
    /*TODO*///#define EA_PD_32 (AY-=4)                               /* predecrement (size = long) */

    public static long EA_PD_32() {
        set_CPU_A((int) (get_CPU_IR() & 7), (get_CPU_A()[(int) (get_CPU_IR() & 7)] - 4) & 0xFFFFFFFFL);
        return get_AY();
    }
    /*TODO*///#define EA_DI    (AY+MAKE_INT_16(m68ki_read_imm_16())) /* displacement */

    public static long EA_DI() {
        return (get_AY() + MAKE_INT_16(m68ki_read_imm_16())) & 0xFFFFFFFFL;
    }

    public static long EA_IX() {
        return m68ki_get_ea_ix();                   /* indirect + index */

    }
    /*TODO*///#define EA_AW    MAKE_INT_16(m68ki_read_imm_16())      /* absolute word */

    public static long EA_AW() {
        return MAKE_INT_16(m68ki_read_imm_16());
    }

    public static long EA_AL() {
        return m68ki_read_imm_32();                   /* absolute long */

    }

    public static long EA_PCIX() {
        return m68ki_get_ea_pcix();                   /* pc indirect + index */

    }
    /* Add and Subtract Flag Calculation Macros */

    public static long VFLAG_ADD_8(long S, long D, long R) {
        return GET_MSB_8((S & D & ~R) | (~S & ~D & R));
    }

    public static long VFLAG_ADD_16(long S, long D, long R) {
        return GET_MSB_16((S & D & ~R) | (~S & ~D & R));
    }

    public static long VFLAG_ADD_32(long S, long D, long R) {
        return GET_MSB_32((S & D & ~R) | (~S & ~D & R));
    }

    public static long CFLAG_ADD_8(long S, long D, long R) {
        return GET_MSB_8((S & D) | (~R & D) | (S & ~R));
    }

    public static long CFLAG_ADD_16(long S, long D, long R) {
        return GET_MSB_16((S & D) | (~R & D) | (S & ~R));
    }

    public static long CFLAG_ADD_32(long S, long D, long R) {
        return GET_MSB_32((S & D) | (~R & D) | (S & ~R));
    }

    public static long VFLAG_SUB_8(long S, long D, long R) {
        return GET_MSB_8((~S & D & ~R) | (S & ~D & R));
    }

    public static long VFLAG_SUB_16(long S, long D, long R) {
        return GET_MSB_16((~S & D & ~R) | (S & ~D & R));
    }

    public static long VFLAG_SUB_32(long S, long D, long R) {
        return GET_MSB_32((~S & D & ~R) | (S & ~D & R));
    }

    public static long CFLAG_SUB_8(long S, long D, long R) {
        return GET_MSB_8((S & ~D) | (R & ~D) | (S & R));
    }

    public static long CFLAG_SUB_16(long S, long D, long R) {
        return GET_MSB_16((S & ~D) | (R & ~D) | (S & R));
    }

    public static long CFLAG_SUB_32(long S, long D, long R) {
        return GET_MSB_32((S & ~D) | (R & ~D) | (S & R));
    }
    /* Conditions */

    public static boolean CONDITION_HI() {
        return (get_CPU_C() == 0 && get_CPU_NOT_Z() != 0);
    }
    /*TODO*///#define CONDITION_NOT_HI (CPU_C != 0 || CPU_NOT_Z == 0)

    public static boolean CONDITION_LS() {
        return (get_CPU_C() != 0 || get_CPU_NOT_Z() == 0);
    }
    /*TODO*///#define CONDITION_NOT_LS (CPU_C == 0 && CPU_NOT_Z != 0)

    public static boolean CONDITION_CC() {
        return (get_CPU_C() == 0);
    }
    /*TODO*///#define CONDITION_NOT_CC (CPU_C != 0)

    public static boolean CONDITION_CS() {
        return (get_CPU_C() != 0);
    }

    public static boolean CONDITION_NOT_CS() {
        return (get_CPU_C() == 0);
    }

    public static boolean CONDITION_NE() {
        return (get_CPU_NOT_Z() != 0);
    }
    /*TODO*///#define CONDITION_NOT_NE (CPU_NOT_Z == 0)

    public static boolean CONDITION_EQ() {
        return (get_CPU_NOT_Z() == 0);
    }

    public static boolean CONDITION_NOT_EQ() {
        return (get_CPU_NOT_Z() != 0);
    }
    /*TODO*///#define CONDITION_VC     (CPU_V == 0)
/*TODO*///#define CONDITION_NOT_VC (CPU_V != 0)
/*TODO*///#define CONDITION_VS     (CPU_V != 0)
/*TODO*///#define CONDITION_NOT_VS (CPU_V == 0)

    public static boolean CONDITION_PL() {
        return (get_CPU_N() == 0);
    }

    public static boolean CONDITION_NOT_PL() {
        return (get_CPU_N() != 0);
    }

    public static boolean CONDITION_MI() {
        return (get_CPU_N() != 0);
    }
    /*TODO*///#define CONDITION_NOT_MI (CPU_N == 0)

    public static boolean CONDITION_GE() {
        return ((get_CPU_N() == 0) == (get_CPU_V() == 0));
    }
    /*TODO*///#define CONDITION_NOT_GE ((CPU_N == 0) != (CPU_V == 0))

    public static boolean CONDITION_LT() {
        return ((get_CPU_N() == 0) != (get_CPU_V() == 0));
    }
    /*TODO*///#define CONDITION_NOT_LT ((CPU_N == 0) == (CPU_V == 0))

    public static boolean CONDITION_GT() {
        return (get_CPU_NOT_Z() != 0 && (get_CPU_N() == 0) == (get_CPU_V() == 0));
    }
    /*TODO*///#define CONDITION_NOT_GT (CPU_NOT_Z == 0 || (CPU_N == 0) != (CPU_V == 0))

    public static boolean CONDITION_LE() {
        return (get_CPU_NOT_Z() == 0 || (get_CPU_N() == 0) != (get_CPU_V() == 0));
    }
    /*TODO*///#define CONDITION_NOT_LE (CPU_NOT_Z != 0 && (CPU_N == 0) == (CPU_V == 0))
/*TODO*///

    /* Use up clock cycles.
     * NOTE: clock cycles used in here are 99.9% correct for a 68000, not for the
     * higher processors.
     */
    public static void USE_CLKS(int A) {
        m68k_clks_left[0] -= (A);
    }


    /*TODO*////* Push/pull data to/from the stack */
/*TODO*///#define m68ki_push_16(A) m68ki_write_16(CPU_A[7]-=2, A)
    public static void m68ki_push_16(long A) {
        set_CPU_A(7, (get_CPU_A()[7] - 2) & 0xFFFFFFFFL);
        m68ki_write_16(get_CPU_A()[7], A);
    }
    /*TODO*///#define m68ki_push_32(A) m68ki_write_32(CPU_A[7]-=4, A)

    public static void m68ki_push_32(long A) {
        set_CPU_A(7, (get_CPU_A()[7] - 4) & 0xFFFFFFFFL);
        m68ki_write_32(get_CPU_A()[7], A);
    }
    /*TODO*///#define m68ki_pull_16()  m68ki_read_16((CPU_A[7]+=2) - 2)

    public static long m68ki_pull_16() {
        set_CPU_A(7, (get_CPU_A()[7] + 2) & 0xFFFFFFFFL);
        return m68ki_read_16(get_CPU_A()[7] - 2);
    }

    /*TODO*///#define m68ki_pull_32()  m68ki_read_32((CPU_A[7]+=4) - 4)
    public static long m68ki_pull_32() {
        set_CPU_A(7, (get_CPU_A()[7] + 4) & 0xFFFFFFFFL);
        return m68ki_read_32(get_CPU_A()[7] - 4);
    }
    /*TODO*///
/*TODO*////* branch byte and word are for branches, while long is for jumps.
/*TODO*/// * So far it's been safe to not call set_pc() for branch word.
/*TODO*/// */

    public static void m68ki_branch_byte(long A) {
        set_CPU_PC((get_CPU_PC() + MAKE_INT_8(A)) & 0xFFFFFFFFL);
    }

    public static void m68ki_branch_word(long A) {
        set_CPU_PC((get_CPU_PC() + MAKE_INT_16(A)) & 0xFFFFFFFFL);
    }
    /*TODO*///#define m68ki_branch_dword(A) CPU_PC += (A)

    public static void m68ki_branch_long(long A) {
        m68ki_set_pc(A);
    }
    /*TODO*///
/*TODO*///
/*TODO*////* Get the condition code register */
/*TODO*///#define m68ki_get_ccr() (((CPU_X != 0)     << 4) | \
/*TODO*///                         ((CPU_N != 0)     << 3) | \
/*TODO*///                         ((CPU_NOT_Z == 0) << 2) | \
/*TODO*///                         ((CPU_V != 0)     << 1) | \
/*TODO*///                          (CPU_C != 0))
/*TODO*///
/* Get the status register */

    public static long m68ki_get_sr() {
        return ((((m68k_cpu.t1_flag != 0) ? 1 : 0) << 15)
                | (((m68k_cpu.t0_flag != 0) ? 1 : 0) << 14)
                | (((m68k_cpu.s_flag != 0) ? 1 : 0) << 13)
                | (((m68k_cpu.m_flag != 0) ? 1 : 0) << 12)
                | (m68k_cpu.int_mask << 8)
                | (((m68k_cpu.x_flag != 0) ? 1 : 0) << 4)
                | (((m68k_cpu.n_flag != 0) ? 1 : 0) << 3)
                | (((m68k_cpu.not_z_flag == 0) ? 1 : 0) << 2)
                | (((m68k_cpu.v_flag != 0) ? 1 : 0) << 1)
                | ((m68k_cpu.c_flag != 0) ? 1 : 0));

    }
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* ======================================================================== */
/*TODO*////* ========================= CONFIGURATION DEFINES ======================== */
/*TODO*////* ======================================================================== */
/*TODO*///
/*TODO*////* Act on values in m68kconf.h */
/*TODO*///#if M68K_INT_ACK
/*TODO*///#define m68ki_int_ack(A) CPU_INT_ACK_CALLBACK(A)
/*TODO*///#else
/*TODO*////* Default action is to used autovector mode, which is most common */
/*TODO*///#define m68ki_int_ack(A) M68K_INT_ACK_AUTOVECTOR
/*TODO*///#endif /* M68K_INT_ACK */
/*TODO*///
/*TODO*///#if M68K_BKPT_ACK
/*TODO*///#define m68ki_bkpt_ack(A) CPU_BKPT_ACK_CALLBACK(A)
/*TODO*///#else
/*TODO*///#define m68ki_bkpt_ack(A)
/*TODO*///#endif /* M68K_BKPT_ACK */
/*TODO*///
/*TODO*///#if M68K_OUTPUT_RESET
/*TODO*///#define m68ki_output_reset() CPU_RESET_INSTR_CALLBACK()
/*TODO*///#else
/*TODO*///#define m68ki_output_reset()
/*TODO*///#endif /* M68K_OUTPUT_RESET */
/*TODO*///
/*TODO*///#if M68K_PC_CHANGED
/*TODO*///#define m68ki_pc_changed(A) CPU_PC_CHANGED_CALLBACK(A)
/*TODO*///#else
/*TODO*///#define m68ki_pc_changed(A)
/*TODO*///#endif /* M68K_PC_CHANGED */
/*TODO*///
/*TODO*///#if M68K_SET_FC
/*TODO*///#define m68ki_set_fc(A) CPU_SET_FC_CALLBACK(A)
/*TODO*///#else
/*TODO*///#define m68ki_set_fc(A)
/*TODO*///#endif /* M68K_SET_FC */
/*TODO*///
/*TODO*///#if M68K_INSTR_HOOK
/*TODO*///#define m68ki_instr_hook() CPU_INSTR_HOOK_CALLBACK()
/*TODO*///#else
/*TODO*///#define m68ki_instr_hook()
/*TODO*///#endif /* M68K_INSTR_HOOK */
/*TODO*///
/*TODO*///#if M68K_TRACE
/*TODO*////* Initiates trace checking before each instruction (t1) */
/*TODO*///#define m68ki_set_trace() m68k_tracing = CPU_T1
/*TODO*////* adds t0 to trace checking if we encounter change of flow */
/*TODO*///#define m68ki_add_trace() m68k_tracing |= CPU_T0
/*TODO*////* Clear all tracing */
/*TODO*///#define m68ki_clear_trace() m68k_tracing = 0
/*TODO*////* Cause a trace exception if we are tracing */
/*TODO*///#define m68ki_exception_if_trace() if(m68k_tracing) m68ki_exception(EXCEPTION_TRACE)
/*TODO*///#else
/*TODO*///#define m68ki_set_trace()
/*TODO*///#define m68ki_add_trace()
/*TODO*///#define m68ki_clear_trace()
/*TODO*///#define m68ki_exception_if_trace()
/*TODO*///#endif /* M68K_TRACE */
/*TODO*///

    /* ======================================================================== */
    /* =============================== PROTOTYPES ============================= */
    /* ======================================================================== */

    /* M68K CPU core class */
    public static class m68k_cpu_core {

        public long mode;                /* CPU Operation Mode: 68000, 68010, or 68020 */

        public long[] dr = new long[8];   /* Data Registers */

        public long[] ar = new long[8];   /* Address Registers */

        public long ppc;                 /* Previous program counter */

        public long pc;                  /* Program Counter */

        public long[] sp = new long[4];   /* User, Interrupt, and Master Stack Polongers */

        public long vbr;                 /* Vector Base Register (68010+) */

        public long sfc;                 /* Source Function Code Register (m68010+) */

        public long dfc;                 /* Destination Function Code Register (m68010+) */

        public long cacr;                /* Cache Control Register (m68020+) */

        public long caar;                /* Cacge Address Register (m68020+) */

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

        public long int_state;           /* Current longerrupt state -- ASG: changed from longs_pending */

        public long stopped;             /* Stopped state */

        public long halted;              /* Halted state */

        public long int_cycles;          /* ASG: extra cycles from generated interrupts */

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

    /* ======================================================================== */
    /* =========================== UTILITY FUNCTIONS ========================== */
    /* ======================================================================== */
    /* Set the function code and read memory from anywhere. */
    public static long m68ki_read_8(long address) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        return m68k_read_memory_8((int) ADDRESS_68K(address));
    }

    public static long m68ki_read_16(long address) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        return m68k_read_memory_16((int) ADDRESS_68K(address));
    }

    public static long m68ki_read_32(long address) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        return m68k_read_memory_32((int) ADDRESS_68K(address));
    }


    /* Set the function code and write memory to anywhere. */
    public static void m68ki_write_8(long address, long value) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        m68k_write_memory_8((int) ADDRESS_68K(address), (int) value);
    }

    public static void m68ki_write_16(long address, long value) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        m68k_write_memory_16((int) ADDRESS_68K(address), (int) value);
    }

    public static void m68ki_write_32(long address, long value) {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
        m68k_write_memory_32((int) ADDRESS_68K(address), (int) value);
    }


    /* Set the function code and read memory immediately following the PC. */
    public static long m68ki_read_imm_8() {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
        if (MASK_OUT_BELOW_2(get_CPU_PC()) != get_CPU_PREF_ADDR()) {
            set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
            set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));
        }
        //CPU_PC += 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);//unsingned?
        return MASK_OUT_ABOVE_8(get_CPU_PREF_DATA() >>> ((2 - ((get_CPU_PC() - 2) & 2)) << 3));
    }

    public static long m68ki_read_imm_16() {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
        if (MASK_OUT_BELOW_2(get_CPU_PC()) != get_CPU_PREF_ADDR()) {
            set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
            set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));
        }
        //CPU_PC += 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);//unsingned?
        return MASK_OUT_ABOVE_16(get_CPU_PREF_DATA() >>> ((2 - ((get_CPU_PC() - 2) & 2)) << 3));

    }

    public static long m68ki_read_imm_32() {

        long temp_val;

        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
        if (MASK_OUT_BELOW_2(get_CPU_PC()) != get_CPU_PREF_ADDR()) {
            set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
            set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));
        }
        temp_val = get_CPU_PREF_DATA() & 0xFFFFFFFFL;
        //CPU_PC += 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);//unsingned?
        if (MASK_OUT_BELOW_2(get_CPU_PC()) != get_CPU_PREF_ADDR()) {
            set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
            set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));
            temp_val = MASK_OUT_ABOVE_32(((temp_val << 16)) | ((get_CPU_PREF_DATA() >>> 16) & 0xFFFFL));
        }
        //CPU_PC += 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);//unsingned?

        return temp_val;
    }


    /* Set the function code and read an instruction immediately following the PC. */
    public static long m68ki_read_instruction() {
        //m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
        if (MASK_OUT_BELOW_2(get_CPU_PC()) != get_CPU_PREF_ADDR()) {
            set_CPU_PREF_ADDR(MASK_OUT_BELOW_2(get_CPU_PC()));
            set_CPU_PREF_DATA(m68k_read_immediate_32((int) ADDRESS_68K(get_CPU_PREF_ADDR())));
        }
        //CPU_PC += 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);//unsingned?
        return MASK_OUT_ABOVE_16(get_CPU_PREF_DATA() >>> ((2 - ((get_CPU_PC() - 2) & 2)) << 3));
    }

    /*TODO*///
/*TODO*////* Read/Write data with a specific function code (used by MOVES) */
/*TODO*///INLINE uint m68ki_read_8_fc(uint address, uint fc)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   return m68k_read_memory_8(ADDRESS_68K(address));
/*TODO*///}
/*TODO*///INLINE uint m68ki_read_16_fc(uint address, uint fc)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   return m68k_read_memory_16(ADDRESS_68K(address));
/*TODO*///}
/*TODO*///INLINE uint m68ki_read_32_fc(uint address, uint fc)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   return m68k_read_memory_32(ADDRESS_68K(address));
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void m68ki_write_8_fc(uint address, uint fc, uint value)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   m68k_write_memory_8(ADDRESS_68K(address), value);
/*TODO*///}
/*TODO*///INLINE void m68ki_write_16_fc(uint address, uint fc, uint value)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   m68k_write_memory_16(ADDRESS_68K(address), value);
/*TODO*///}
/*TODO*///INLINE void m68ki_write_32_fc(uint address, uint fc, uint value)
/*TODO*///{
/*TODO*///   m68ki_set_fc(fc&7);
/*TODO*///   m68k_write_memory_32(ADDRESS_68K(address), value);
/*TODO*///}
/*TODO*///
/*TODO*///
/* Decode address register indirect with index */
    public static long m68ki_get_ea_ix() {
        long extension = m68ki_read_imm_16();
        long ea_index = m68k_cpu_dar[(int) (EXT_INDEX_AR(extension) != 0 ? 1 : 0)][(int) EXT_INDEX_REGISTER(extension)];
        long base = get_AY();
        long outer = 0;

        /* Sign-extend the index value if needed */
        if (EXT_INDEX_LONG(extension) == 0) {
            ea_index = MAKE_INT_16(ea_index);
        }

        /* If we're running 010 or less, there's no scale or full extension word mode */
        if ((get_CPU_MODE() & CPU_MODE_010_LESS) != 0) {
            return (base + ea_index + MAKE_INT_8(extension)) & 0xFFFFFFFFL;
        }

        throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///   /* Scale the index value */
/*TODO*///   ea_index <<= EXT_INDEX_SCALE(extension);
/*TODO*///
/*TODO*///   /* If we're using brief extension mode, we are done */
/*TODO*///   if(EXT_BRIEF_FORMAT(extension))
/*TODO*///      return base + ea_index + MAKE_INT_8(extension);
/*TODO*///
/*TODO*///   /* Decode the long extension format */
/*TODO*///   if(EXT_IX_SUPPRESSED(extension))
/*TODO*///      ea_index = 0;
/*TODO*///   if(EXT_BR_SUPPRESSED(extension))
/*TODO*///      base = 0;
/*TODO*///   if(EXT_BD_PRESENT(extension))
/*TODO*///      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_NO_MEMORY_INDIRECT(extension))
/*TODO*///      return base + ea_index;
/*TODO*///
/*TODO*///   if(EXT_OD_PRESENT(extension))
/*TODO*///      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_POSTINDEX(extension))
/*TODO*///      return m68ki_read_32(base) + ea_index + outer;
/*TODO*///   return m68ki_read_32(base + ea_index) + outer;
    }

    /* Decode address register indirect with index for MOVE destination */
    public static long m68ki_get_ea_ix_dst() {
        long extension = m68ki_read_imm_16();
        long ea_index = m68k_cpu_dar[(int) (EXT_INDEX_AR(extension) != 0 ? 1 : 0)][(int) EXT_INDEX_REGISTER(extension)];
        long base = get_AX(); /* This is the only thing different from m68ki_get_ea_ix() */

        long outer = 0;

        /* Sign-extend the index value if needed */
        if (EXT_INDEX_LONG(extension) == 0) {
            ea_index = MAKE_INT_16(ea_index);
        }

        /* If we're running 010 or less, there's no scale or full extension word mode */
        if ((get_CPU_MODE() & CPU_MODE_010_LESS) != 0) {
            return (base + ea_index + MAKE_INT_8(extension)) & 0xFFFFFFFFL;
        }

        throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///   /* Scale the index value */
/*TODO*///   ea_index <<= EXT_INDEX_SCALE(extension);
/*TODO*///
/*TODO*///   /* If we're using brief extension mode, we are done */
/*TODO*///   if(EXT_BRIEF_FORMAT(extension))
/*TODO*///      return base + ea_index + MAKE_INT_8(extension);
/*TODO*///
/*TODO*///   /* Decode the long extension format */
/*TODO*///   if(EXT_IX_SUPPRESSED(extension))
/*TODO*///      ea_index = 0;
/*TODO*///   if(EXT_BR_SUPPRESSED(extension))
/*TODO*///      base = 0;
/*TODO*///   if(EXT_BD_PRESENT(extension))
/*TODO*///      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_NO_MEMORY_INDIRECT(extension))
/*TODO*///      return base + ea_index;
/*TODO*///
/*TODO*///   if(EXT_OD_PRESENT(extension))
/*TODO*///      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_POSTINDEX(extension))
/*TODO*///      return m68ki_read_32(base) + ea_index + outer;
/*TODO*///   return m68ki_read_32(base + ea_index) + outer;
    }

    /* Decode program counter indirect with index */
    public static long m68ki_get_ea_pcix() {

        //uint base = (CPU_PC += 2) - 2;
        set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);
        long base = get_CPU_PC() - 2;
        long extension = m68ki_read_16(base);
        long ea_index = m68k_cpu_dar[(int) (EXT_INDEX_AR(extension) != 0 ? 1 : 0)][(int) EXT_INDEX_REGISTER(extension)];
        long outer = 0;

        /* Sign-extend the index value if needed */
        if (EXT_INDEX_LONG(extension) == 0) {
            ea_index = MAKE_INT_16(ea_index);
        }

        /* If we're running 010 or less, there's no scale or full extension word mode */
        if ((get_CPU_MODE() & CPU_MODE_010_LESS) != 0) {
            return (base + ea_index + MAKE_INT_8(extension)) & 0xFFFFFFFFL;
        }
        throw new UnsupportedOperationException("Unimplemented");
        /*TODO*///   /* Scale the index value */
/*TODO*///   ea_index <<= EXT_INDEX_SCALE(extension);
/*TODO*///
/*TODO*///   /* If we're using brief extension mode, we are done */
/*TODO*///   if(EXT_BRIEF_FORMAT(extension))
/*TODO*///      return base + ea_index + MAKE_INT_8(extension);
/*TODO*///
/*TODO*///   /* Decode the long extension format */
/*TODO*///   if(EXT_IX_SUPPRESSED(extension))
/*TODO*///      ea_index = 0;
/*TODO*///   if(EXT_BR_SUPPRESSED(extension))
/*TODO*///      base = 0;
/*TODO*///   if(EXT_BD_PRESENT(extension))
/*TODO*///      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_NO_MEMORY_INDIRECT(extension))
/*TODO*///      return base + ea_index;
/*TODO*///
/*TODO*///   if(EXT_OD_PRESENT(extension))
/*TODO*///      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///   if(EXT_POSTINDEX(extension))
/*TODO*///      return m68ki_read_32(base) + ea_index + outer;
/*TODO*///   return m68ki_read_32(base + ea_index) + outer;
    }
    /* Set the S flag and change the active stack pointer. */

    public static void m68ki_set_s_flag(int value) {
        /* ASG: Only do the rest if we're changing */
        value = (value != 0) ? 1 : 0;
        if (get_CPU_S() != value) {
            /* Backup the old stack pointer */
            set_CPU_SP((int) (get_CPU_S() | (get_CPU_M() & (get_CPU_S() << 1))), get_CPU_A()[7]);
            /* Set the S flag */
            set_CPU_S(value);
            /* Set the new stack pointer */
            set_CPU_A(7, get_CPU_SP()[(int) (get_CPU_S() | (get_CPU_M() & (get_CPU_S() << 1)))]);
        }
    }

    /*TODO*////* Set the M flag and change the active stack pointer. */
/*TODO*///INLINE void m68ki_set_m_flag(int value)
/*TODO*///{
/*TODO*///   /* ASG: Only do the rest if we're changing */
/*TODO*///   value = (value != 0 && CPU_MODE & CPU_MODE_EC020_PLUS)<<1;
/*TODO*///   if (CPU_M != value)
/*TODO*///   {
/*TODO*///      /* Backup the old stack pointer */
/*TODO*///      CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))] = CPU_A[7];
/*TODO*///      /* Set the M flag */
/*TODO*///      CPU_M = value;
/*TODO*///      /* Set the new stack pointer */
/*TODO*///      CPU_A[7] = CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))];
/*TODO*///   }
/*TODO*///}
/*TODO*///
/*TODO*////* Set the S and M flags and change the active stack pointer. */
    public static void m68ki_set_sm_flag(long s_value, long m_value) {
        /* ASG: Only do the rest if we're changing */
        s_value = (s_value != 0) ? 1L : 0L;
        m_value = (m_value != 0 && (m68k_cpu.mode & CPU_MODE_EC020_PLUS) != 0) ? 1 : 0 << 1;
        if (get_CPU_S() != s_value || get_CPU_M() != m_value) {
            /* Backup the old stack pointer */
            m68k_cpu.sp[(int) (get_CPU_S() | (get_CPU_M() & (get_CPU_S() << 1)))] = get_CPU_A()[7];
            /* Set the S and M flags */
            set_CPU_S(s_value != 0 ? 1L : 0L);
            set_CPU_M((m_value != 0 && (m68k_cpu.mode & CPU_MODE_EC020_PLUS) != 0) ? 1 : 0 << 1);
            /* Set the new stack pointer */
            set_CPU_A(7, m68k_cpu.sp[(int) (get_CPU_S() | (get_CPU_M() & (get_CPU_S() << 1)))]);
        }
    }


    /* Set the condition code register */
    public static void m68ki_set_ccr(long value) {
        set_CPU_X(BIT_4(value));
        set_CPU_N(BIT_3(value));
        set_CPU_NOT_Z(BIT_2(value) == 0 ? 1 : 0);
        set_CPU_V(BIT_1(value));
        set_CPU_C(BIT_0(value));
    }

    /* Set the status register */
    public static void m68ki_set_sr(long value) {
        /* ASG: detect changes to the INT_MASK */
        long old_mask = get_CPU_INT_MASK();

        /* Mask out the "unimplemented" bits */
        value &= m68k_sr_implemented_bits[(int) get_CPU_MODE()];

        /* Now set the status register */
        set_CPU_T1(BIT_F(value));
        set_CPU_T0(BIT_E(value));
        set_CPU_INT_MASK((value >> 8) & 7);
        set_CPU_X(BIT_4(value));
        set_CPU_N(BIT_3(value));
        set_CPU_NOT_Z(BIT_2(value) == 0L ? 1L : 0L);
        set_CPU_V(BIT_1(value));
        set_CPU_C(BIT_0(value));
        m68ki_set_sm_flag(BIT_D(value), BIT_C(value));

        /* ASG: detect changes to the INT_MASK */
        if (get_CPU_INT_MASK() != old_mask) {
            m68ki_check_interrupts();
        }
    }
    /*TODO*///
/*TODO*///
/*TODO*////* Set the status register */
/*TODO*///INLINE void m68ki_set_sr_no_int(uint value)
/*TODO*///{
/*TODO*///   /* Mask out the "unimplemented" bits */
/*TODO*///   value &= m68k_sr_implemented_bits[CPU_MODE];
/*TODO*///
/*TODO*///   /* Now set the status register */
/*TODO*///   CPU_T1 = BIT_F(value);
/*TODO*///   CPU_T0 = BIT_E(value);
/*TODO*///   CPU_INT_MASK = (value >> 8) & 7;
/*TODO*///   CPU_X = BIT_4(value);
/*TODO*///   CPU_N = BIT_3(value);
/*TODO*///   CPU_NOT_Z = !BIT_2(value);
/*TODO*///   CPU_V = BIT_1(value);
/*TODO*///   CPU_C = BIT_0(value);
/*TODO*///   m68ki_set_sm_flag(BIT_D(value), BIT_C(value));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* I set the PC this way to let host programs be nicer.
/*TODO*/// * This is mainly for programs running from separate ram banks.
/*TODO*/// * If the host program knows where the PC is, it can offer faster
/*TODO*/// * ram access times for data to be retrieved immediately following
/*TODO*/// * the PC.
/*TODO*/// */

    public static void m68ki_set_pc(long address) {
        /* Set the program counter */
        set_CPU_PC(address & 0xFFFFFFFFL);
        /* Inform the host program */
        /* MAME */
        change_pc24((int) ADDRESS_68K(address));
        /*
         m68ki_pc_changed(ADDRESS_68K(address));
         */
    }
    /* Process an exception */

    public static void m68ki_exception(long vector) {
        /* Save the old status register */
        long old_sr = m68ki_get_sr();

        /* Use up some clock cycles */
        USE_CLKS(m68k_exception_cycle_table[(int) vector]);

        /* Turn off stopped state and trace flag, clear pending traces */
        set_CPU_STOPPED(0);
        set_CPU_T1(0);
        set_CPU_T0(0);

        /* Enter supervisor mode */
        m68ki_set_s_flag(1);
        /* Push a stack frame */
        if ((get_CPU_MODE() & CPU_MODE_010_PLUS) != 0) {
            m68ki_push_16(vector << 2); /* This is format 0 */
        }
        m68ki_push_32(get_CPU_PPC());	/* save previous PC, ie. PC that contains an offending instruction */

        m68ki_push_16(old_sr);
        /* Generate a new program counter from the vector */
        m68ki_set_pc(m68ki_read_32((vector << 2) + get_CPU_VBR()));
    }
    /* Process an interrupt (or trap) */

    public static void m68ki_interrupt(long vector) {
        /* Save the old status register */
        long old_sr = m68ki_get_sr();

        /* Use up some clock cycles */
        /* ASG: just keep them pending */
        /* USE_CLKS(m68k_exception_cycle_table[vector]);*/
        set_CPU_INT_CYCLES(get_CPU_INT_CYCLES() + m68k_exception_cycle_table[(int) vector]);

        /* Turn off stopped state and trace flag, clear pending traces */
        set_CPU_STOPPED(0);
        set_CPU_T1(0);
        set_CPU_T0(0);

        /* Enter supervisor mode */
        m68ki_set_s_flag(1);
        /* Push a stack frame */
        if ((get_CPU_MODE() & CPU_MODE_010_PLUS) != 0) {
            m68ki_push_16(vector << 2); /* This is format 0 */

        }
        m68ki_push_32(get_CPU_PC());
        m68ki_push_16(old_sr);
        /* Generate a new program counter from the vector */
        m68ki_set_pc(m68ki_read_32((vector << 2) + get_CPU_VBR()));
    }
    /* Service an interrupt request */

    public static void m68ki_service_interrupt(long pending_mask) /* ASG: added parameter here */ {
        int int_level = 7;
        int vector;

        /* Start at level 7 and then go down */
        for (; (pending_mask & (1 << int_level)) == 0; int_level--)	/* ASG: changed to use parameter instead of CPU_INTS_PENDING */
      ;

        /* Get the exception vector */
        switch (vector = get_CPU_INT_ACK_CALLBACK().handler(int_level)) {
            case 0x00:
            case 0x01:
                /* vectors 0 and 1 are ignored since they are for reset only */
                return;
            case 0x02:
            case 0x03:
            case 0x04:
            case 0x05:
            case 0x06:
            case 0x07:
            case 0x08:
            case 0x09:
            case 0x0a:
            case 0x0b:
            case 0x0c:
            case 0x0d:
            case 0x0e:
            case 0x0f:
            case 0x10:
            case 0x11:
            case 0x12:
            case 0x13:
            case 0x14:
            case 0x15:
            case 0x16:
            case 0x17:
            case 0x18:
            case 0x19:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x1e:
            case 0x1f:
            case 0x20:
            case 0x21:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0x2e:
            case 0x2f:
            case 0x30:
            case 0x31:
            case 0x32:
            case 0x33:
            case 0x34:
            case 0x35:
            case 0x36:
            case 0x37:
            case 0x38:
            case 0x39:
            case 0x3a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x3f:
            case 0x40:
            case 0x41:
            case 0x42:
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x46:
            case 0x47:
            case 0x48:
            case 0x49:
            case 0x4a:
            case 0x4b:
            case 0x4c:
            case 0x4d:
            case 0x4e:
            case 0x4f:
            case 0x50:
            case 0x51:
            case 0x52:
            case 0x53:
            case 0x54:
            case 0x55:
            case 0x56:
            case 0x57:
            case 0x58:
            case 0x59:
            case 0x5a:
            case 0x5b:
            case 0x5c:
            case 0x5d:
            case 0x5e:
            case 0x5f:
            case 0x60:
            case 0x61:
            case 0x62:
            case 0x63:
            case 0x64:
            case 0x65:
            case 0x66:
            case 0x67:
            case 0x68:
            case 0x69:
            case 0x6a:
            case 0x6b:
            case 0x6c:
            case 0x6d:
            case 0x6e:
            case 0x6f:
            case 0x70:
            case 0x71:
            case 0x72:
            case 0x73:
            case 0x74:
            case 0x75:
            case 0x76:
            case 0x77:
            case 0x78:
            case 0x79:
            case 0x7a:
            case 0x7b:
            case 0x7c:
            case 0x7d:
            case 0x7e:
            case 0x7f:
            case 0x80:
            case 0x81:
            case 0x82:
            case 0x83:
            case 0x84:
            case 0x85:
            case 0x86:
            case 0x87:
            case 0x88:
            case 0x89:
            case 0x8a:
            case 0x8b:
            case 0x8c:
            case 0x8d:
            case 0x8e:
            case 0x8f:
            case 0x90:
            case 0x91:
            case 0x92:
            case 0x93:
            case 0x94:
            case 0x95:
            case 0x96:
            case 0x97:
            case 0x98:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0x9f:
            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0xa5:
            case 0xa6:
            case 0xa7:
            case 0xa8:
            case 0xa9:
            case 0xaa:
            case 0xab:
            case 0xac:
            case 0xad:
            case 0xae:
            case 0xaf:
            case 0xb0:
            case 0xb1:
            case 0xb2:
            case 0xb3:
            case 0xb4:
            case 0xb5:
            case 0xb6:
            case 0xb7:
            case 0xb8:
            case 0xb9:
            case 0xba:
            case 0xbb:
            case 0xbc:
            case 0xbd:
            case 0xbe:
            case 0xbf:
            case 0xc0:
            case 0xc1:
            case 0xc2:
            case 0xc3:
            case 0xc4:
            case 0xc5:
            case 0xc6:
            case 0xc7:
            case 0xc8:
            case 0xc9:
            case 0xca:
            case 0xcb:
            case 0xcc:
            case 0xcd:
            case 0xce:
            case 0xcf:
            case 0xd0:
            case 0xd1:
            case 0xd2:
            case 0xd3:
            case 0xd4:
            case 0xd5:
            case 0xd6:
            case 0xd7:
            case 0xd8:
            case 0xd9:
            case 0xda:
            case 0xdb:
            case 0xdc:
            case 0xdd:
            case 0xde:
            case 0xdf:
            case 0xe0:
            case 0xe1:
            case 0xe2:
            case 0xe3:
            case 0xe4:
            case 0xe5:
            case 0xe6:
            case 0xe7:
            case 0xe8:
            case 0xe9:
            case 0xea:
            case 0xeb:
            case 0xec:
            case 0xed:
            case 0xee:
            case 0xef:
            case 0xf0:
            case 0xf1:
            case 0xf2:
            case 0xf3:
            case 0xf4:
            case 0xf5:
            case 0xf6:
            case 0xf7:
            case 0xf8:
            case 0xf9:
            case 0xfa:
            case 0xfb:
            case 0xfc:
            case 0xfd:
            case 0xfe:
            case 0xff:
                /* The external peripheral has provided the interrupt vector to take */
                break;
            case M68K_INT_ACK_AUTOVECTOR:
                /* Use the autovectors.  This is the most commonly used implementation */
                vector = EXCEPTION_INTERRUPT_AUTOVECTOR + int_level;
                break;
            case M68K_INT_ACK_SPURIOUS:
                /* Called if no devices respond to the interrupt acknowledge */
                vector = EXCEPTION_SPURIOUS_INTERRUPT;
                break;
            default:
                /* Everything else is ignored */
                return;
        }

        /* If vector is uninitialized, call the uninitialized interrupt vector */
        if (m68ki_read_32(vector << 2) == 0) {
            vector = EXCEPTION_UNINITIALIZED_INTERRUPT;
        }

        /* Generate an interupt */
        m68ki_interrupt(vector);

        /* Set the interrupt mask to the level of the one being serviced */
        set_CPU_INT_MASK(int_level);
        if (m68klog != null) {
            fprintf(m68klog, "service_interrupt :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
        }
    }


    /* ASG: Check for interrupts */
    public static void m68ki_check_interrupts() {

        long pending_mask = 1 << get_CPU_INT_STATE();
        if ((pending_mask & m68k_int_masks[(int) get_CPU_INT_MASK()]) != 0L) {
            m68ki_service_interrupt(pending_mask);
        }
    }
}
