#ifndef M68KCPU__HEADER
#define M68KCPU__HEADER


#include "m68k.h"
#include <limits.h>

/* ======================================================================== */
/* ==================== ARCHITECTURE-DEPENDANT DEFINES ==================== */
/* ======================================================================== */

/* Check if we have certain storage sizes */

#if UCHAR_MAX == 0xff
#define M68K_HAS_8_BIT_SIZE  1
#else
#define M68K_HAS_8_BIT_SIZE  0
#endif /* UCHAR_MAX == 0xff */

#if USHRT_MAX == 0xffff
#define M68K_HAS_16_BIT_SIZE 1
#else
#define M68K_HAS_16_BIT_SIZE 0
#endif /* USHRT_MAX == 0xffff */

#if ULONG_MAX == 0xffffffff
#define M68K_HAS_32_BIT_SIZE 1
#else
#define M68K_HAS_32_BIT_SIZE 0
#endif /* ULONG_MAX == 0xffffffff */

#if UINT_MAX > 0xffffffff
#define M68K_OVER_32_BIT     1
#else
#define M68K_OVER_32_BIT     0
#endif /* UINT_MAX > 0xffffffff */

/* Data types used in this emulation core */
#undef int8
#undef int16
#undef int32
#undef uint
#undef uint8
#undef uint16
#undef uint

#define int8   signed char			/* ASG: changed from char to signed char */
#define uint8  unsigned char
#define int16  short
#define uint16 unsigned short
#define int32  long

/* int and unsigned int must be at least 32 bits wide */
#define uint   unsigned int


/* Allow for architectures that don't have 8-bit sizes */
#if M68K_HAS_8_BIT_SIZE
#define MAKE_INT_8(A) (int8)((A)&0xff)
#else
#undef  int8
#define int8   int
#undef  uint8
#define uint8  unsigned int
INLINE int MAKE_INT_8(int value)
{
   /* Will this work for 1's complement machines? */
   return (value & 0x80) ? value | ~0xff : value & 0xff;
}
#endif /* M68K_HAS_8_BIT_SIZE */


/* Allow for architectures that don't have 16-bit sizes */
#if M68K_HAS_16_BIT_SIZE
#define MAKE_INT_16(A) (int16)((A)&0xffff)
#else
#undef  int16
#define int16  int
#undef  uint16
#define uint16 unsigned int
INLINE int MAKE_INT_16(int value)
{
   /* Will this work for 1's complement machines? */
   return (value & 0x8000) ? value | ~0xffff : value & 0xffff;
}
#endif /* M68K_HAS_16_BIT_SIZE */


/* Allow for architectures that don't have 32-bit sizes */
#if M68K_HAS_32_BIT_SIZE
#if M68K_OVER_32_BIT
#define MAKE_INT_32(A) (int32)((A)&0xffffffff)
#else
#define MAKE_INT_32(A) (int32)(A)
#endif /* M68K_OVER_32_BIT */
#else
#undef  int32
#define int32  int
INLINE int MAKE_INT_32(int value)
{
   /* Will this work for 1's complement machines? */
   return (value & 0x80000000) ? value | ~0xffffffff : value & 0xffffffff;
}
#endif /* M68K_HAS_32_BIT_SIZE */



/* ======================================================================== */
/* ============================ GENERAL DEFINES =========================== */
/* ======================================================================== */

/* Exception Vectors handled by emulation */
#define EXCEPTION_ILLEGAL_INSTRUCTION      4
#define EXCEPTION_ZERO_DIVIDE              5
#define EXCEPTION_CHK                      6
#define EXCEPTION_TRAPV                    7
#define EXCEPTION_PRIVILEGE_VIOLATION      8
#define EXCEPTION_TRACE                    9
#define EXCEPTION_1010                    10
#define EXCEPTION_1111                    11
#define EXCEPTION_FORMAT_ERROR            14
#define EXCEPTION_UNINITIALIZED_INTERRUPT 15
#define EXCEPTION_SPURIOUS_INTERRUPT      24
#define EXCEPTION_INTERRUPT_AUTOVECTOR    24
#define EXCEPTION_TRAP_BASE               32

/* Function codes set by CPU during data/address bus activity */
#define FUNCTION_CODE_USER_DATA          1
#define FUNCTION_CODE_USER_PROGRAM       2
#define FUNCTION_CODE_SUPERVISOR_DATA    5
#define FUNCTION_CODE_SUPERVISOR_PROGRAM 6
#define FUNCTION_CODE_CPU_SPACE          7

/* CPU modes for deciding what to emulate */
#define CPU_MODE_000   M68K_CPU_MODE_68000
#define CPU_MODE_010   M68K_CPU_MODE_68010
#define CPU_MODE_EC020 M68K_CPU_MODE_68EC020
#define CPU_MODE_020   M68K_CPU_MODE_68020

#define CPU_MODE_ALL       (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020)
#define CPU_MODE_010_PLUS  (CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020)
#define CPU_MODE_010_LESS  (CPU_MODE_000 | CPU_MODE_010)
#define CPU_MODE_EC020_PLUS  (CPU_MODE_EC020 | CPU_MODE_020)
#define CPU_MODE_EC020_LESS  (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020)
#define CPU_MODE_020_PLUS  CPU_MODE_020
#define CPU_MODE_020_LESS  (CPU_MODE_000 | CPU_MODE_010 | CPU_MODE_EC020 | CPU_MODE_020)


/* ======================================================================== */
/* ================================ MACROS ================================ */
/* ======================================================================== */

/* Bit Isolation Macros */
#define BIT_0(A)  ((A) & 0x00000001)
#define BIT_1(A)  ((A) & 0x00000002)
#define BIT_2(A)  ((A) & 0x00000004)
#define BIT_3(A)  ((A) & 0x00000008)
#define BIT_4(A)  ((A) & 0x00000010)
#define BIT_5(A)  ((A) & 0x00000020)
#define BIT_6(A)  ((A) & 0x00000040)
#define BIT_7(A)  ((A) & 0x00000080)
#define BIT_8(A)  ((A) & 0x00000100)
#define BIT_9(A)  ((A) & 0x00000200)
#define BIT_A(A)  ((A) & 0x00000400)
#define BIT_B(A)  ((A) & 0x00000800)
#define BIT_C(A)  ((A) & 0x00001000)
#define BIT_D(A)  ((A) & 0x00002000)
#define BIT_E(A)  ((A) & 0x00004000)
#define BIT_F(A)  ((A) & 0x00008000)
#define BIT_10(A) ((A) & 0x00010000)
#define BIT_11(A) ((A) & 0x00020000)
#define BIT_12(A) ((A) & 0x00040000)
#define BIT_13(A) ((A) & 0x00080000)
#define BIT_14(A) ((A) & 0x00100000)
#define BIT_15(A) ((A) & 0x00200000)
#define BIT_16(A) ((A) & 0x00400000)
#define BIT_17(A) ((A) & 0x00800000)
#define BIT_18(A) ((A) & 0x01000000)
#define BIT_19(A) ((A) & 0x02000000)
#define BIT_1A(A) ((A) & 0x04000000)
#define BIT_1B(A) ((A) & 0x08000000)
#define BIT_1C(A) ((A) & 0x10000000)
#define BIT_1D(A) ((A) & 0x20000000)
#define BIT_1E(A) ((A) & 0x40000000)
#define BIT_1F(A) ((A) & 0x80000000)

/* Get the most significant bit for specific sizes */
#define GET_MSB_8(A)  ((A) & 0x80)
#define GET_MSB_9(A)  ((A) & 0x100)
#define GET_MSB_16(A) ((A) & 0x8000)
#define GET_MSB_17(A) ((A) & 0x10000)
#define GET_MSB_32(A) ((A) & 0x80000000)

/* Isolate nibbles */
#define LOW_NIBBLE(A) ((A) & 0x0f)
#define HIGH_NIBBLE(A) ((A) & 0xf0)

/* These are used to isolate 8, 16, and 32 bit sizes */
#define MASK_OUT_ABOVE_2(A)  ((A) & 3)
#define MASK_OUT_ABOVE_8(A)  ((A) & 0xff)
#define MASK_OUT_ABOVE_16(A) ((A) & 0xffff)
#define MASK_OUT_BELOW_2(A)  ((A) & ~3)
#define MASK_OUT_BELOW_8(A)  ((A) & ~0xff)
#define MASK_OUT_BELOW_16(A) ((A) & ~0xffff)

/* No need for useless masking if we're 32-bit */
#if M68K_OVER_32_BIT
#define MASK_OUT_ABOVE_32(A) ((A) & 0xffffffff)
#define MASK_OUT_BELOW_32(A) ((A) & ~0xffffffff)
#else
#define MASK_OUT_ABOVE_32(A) (A)
#define MASK_OUT_BELOW_32(A) 0
#endif /* M68K_OVER_32_BIT */


/* Simulate address lines of 68k family */
#define ADDRESS_68K(A) (CPU_MODE & CPU_MODE_020_PLUS ? A : (A)&0xffffff)


/* Instruction extension word information for indexed addressing modes */
#define EXT_INDEX_LONG(A)         BIT_B(A)
#define EXT_INDEX_AR(A)           BIT_F(A)
#define EXT_INDEX_REGISTER(A)     (((A)>>12)&7)
#define EXT_INDEX_SCALE(A)        (((A)>>9)&3)
#define EXT_BRIEF_FORMAT(A)       !BIT_8(A)
#define EXT_IX_SUPPRESSED(A)      BIT_6(A)
#define EXT_BR_SUPPRESSED(A)      BIT_7(A)
#define EXT_BD_PRESENT(A)         BIT_5(A)
#define EXT_BD_LONG(A)            BIT_4(A)
#define EXT_NO_MEMORY_INDIRECT(A) !((A)&7)
#define EXT_OD_PRESENT(A)         BIT_1(A)
#define EXT_OD_LONG(A)            BIT_0(A)
#define EXT_POSTINDEX(A)          BIT_2(A)


/* Shift & Rotate Macros.
 * 32-bit shifts defined in architecture-dependant section.
 */

#define LSL(A, C) ((A) << (C))
#define LSR(A, C) ((A) >> (C))

/* Some > 32-bit optimizations */
#if M68K_OVER_32_BIT
/* Shift left and right */
#define LSR_32(A, C) ((A) >> (C))
#define LSL_32(A, C) ((A) << (C))
#else
/* We have to do this because the morons at ANSI decided that shifts
 * by >= data size are undefined.
 */
#define LSR_32(A, C) ((C) < 32 ? (A) >> (C) : 0)
#define LSL_32(A, C) ((C) < 32 ? (A) << (C) : 0)
#endif /* M68K_OVER_32_BIT */

#define ROL_8(A, C)      MASK_OUT_ABOVE_8(LSL(A, C) | LSR(A, 8-(C)))
#define ROL_9(A, C)                       LSL(A, C) | LSR(A, 9-(C))
#define ROL_16(A, C)    MASK_OUT_ABOVE_16(LSL(A, C) | LSR(A, 16-(C)))
#define ROL_17(A, C)                      LSL(A, C) | LSR(A, 17-(C))
#define ROL_32(A, C) MASK_OUT_ABOVE_32(LSL_32(A, C) | LSR_32(A, 32-(C)))
#define ROL_33(A, C)                  (LSL_32(A, C) | LSR_32(A, 33-(C)))

#define ROR_8(A, C)      MASK_OUT_ABOVE_8(LSR(A, C) | LSL(A, 8-(C)))
#define ROR_9(A, C)                       LSR(A, C) | LSL(A, 9-(C))
#define ROR_16(A, C)    MASK_OUT_ABOVE_16(LSR(A, C) | LSL(A, 16-(C)))
#define ROR_17(A, C)                      LSR(A, C) | LSL(A, 17-(C))
#define ROR_32(A, C) MASK_OUT_ABOVE_32(LSR_32(A, C) | LSL_32(A, 32-(C)))
#define ROR_33(A, C)                  (LSR_32(A, C) | LSL_32(A, 33-(C)))


/* Access the CPU registers */
#define CPU_MODE         m68k_cpu.mode
#define CPU_D            m68k_cpu.dr
#define CPU_A            m68k_cpu.ar
#define CPU_PPC 		 m68k_cpu.ppc
#define CPU_PC           m68k_cpu.pc
#define CPU_SP           m68k_cpu.sp
#define CPU_USP          m68k_cpu.sp[0]
#define CPU_ISP          m68k_cpu.sp[1]
#define CPU_MSP          m68k_cpu.sp[3]
#define CPU_VBR          m68k_cpu.vbr
#define CPU_SFC          m68k_cpu.sfc
#define CPU_DFC          m68k_cpu.dfc
#define CPU_CACR         m68k_cpu.cacr
#define CPU_CAAR         m68k_cpu.caar
#define CPU_IR           m68k_cpu.ir
#define CPU_T1           m68k_cpu.t1_flag
#define CPU_T0           m68k_cpu.t0_flag
#define CPU_S            m68k_cpu.s_flag
#define CPU_M            m68k_cpu.m_flag
#define CPU_X            m68k_cpu.x_flag
#define CPU_N            m68k_cpu.n_flag
#define CPU_NOT_Z        m68k_cpu.not_z_flag
#define CPU_V            m68k_cpu.v_flag
#define CPU_C            m68k_cpu.c_flag
#define CPU_INT_MASK     m68k_cpu.int_mask
#define CPU_INT_STATE    m68k_cpu.int_state /* ASG: changed from CPU_INTS_PENDING */
#define CPU_STOPPED      m68k_cpu.stopped
#define CPU_HALTED       m68k_cpu.halted
#define CPU_INT_CYCLES   m68k_cpu.int_cycles /* ASG */
#define CPU_PREF_ADDR    m68k_cpu.pref_addr
#define CPU_PREF_DATA    m68k_cpu.pref_data

#define CPU_INT_ACK_CALLBACK     m68k_cpu.int_ack_callback
#define CPU_BKPT_ACK_CALLBACK    m68k_cpu.bkpt_ack_callback
#define CPU_RESET_INSTR_CALLBACK m68k_cpu.reset_instr_callback
#define CPU_PC_CHANGED_CALLBACK  m68k_cpu.pc_changed_callback
#define CPU_SET_FC_CALLBACK      m68k_cpu.set_fc_callback
#define CPU_INSTR_HOOK_CALLBACK  m68k_cpu.instr_hook_callback


/*
 * The general instruction format follows this pattern:
 * .... XXX. .... .YYY
 * where XXX is register X and YYY is register Y
 */
/* Data Register Isolation */
#define DX (CPU_D[(CPU_IR >> 9) & 7])
#define DY (CPU_D[CPU_IR & 7])
/* Address Register Isolation */
#define AX (CPU_A[(CPU_IR >> 9) & 7])
#define AY (CPU_A[CPU_IR & 7])


/* Effective Address Calculations */
#define EA_AI    AY                                    /* address register indirect */
#define EA_PI_8  (AY++)                                /* postincrement (size = byte) */
#define EA_PI7_8 ((CPU_A[7]+=2)-2)                     /* postincrement (size = byte & AR = 7) */
#define EA_PI_16 ((AY+=2)-2)                           /* postincrement (size = word) */
#define EA_PI_32 ((AY+=4)-4)                           /* postincrement (size = long) */
#define EA_PD_8  (--AY)                                /* predecrement (size = byte) */
#define EA_PD7_8 (CPU_A[7]-=2)                         /* predecrement (size = byte & AR = 7) */
#define EA_PD_16 (AY-=2)                               /* predecrement (size = word) */
#define EA_PD_32 (AY-=4)                               /* predecrement (size = long) */
#define EA_DI    (AY+MAKE_INT_16(m68ki_read_imm_16())) /* displacement */
#define EA_IX    m68ki_get_ea_ix()                     /* indirect + index */
#define EA_AW    MAKE_INT_16(m68ki_read_imm_16())      /* absolute word */
#define EA_AL    m68ki_read_imm_32()                   /* absolute long */
#define EA_PCIX  m68ki_get_ea_pcix()                   /* pc indirect + index */


/* Add and Subtract Flag Calculation Macros */
#define VFLAG_ADD_8(S, D, R) GET_MSB_8((S & D & ~R) | (~S & ~D & R))
#define VFLAG_ADD_16(S, D, R) GET_MSB_16((S & D & ~R) | (~S & ~D & R))
#define VFLAG_ADD_32(S, D, R) GET_MSB_32((S & D & ~R) | (~S & ~D & R))

#define CFLAG_ADD_8(S, D, R) GET_MSB_8((S & D) | (~R & D) | (S & ~R))
#define CFLAG_ADD_16(S, D, R) GET_MSB_16((S & D) | (~R & D) | (S & ~R))
#define CFLAG_ADD_32(S, D, R) GET_MSB_32((S & D) | (~R & D) | (S & ~R))


#define VFLAG_SUB_8(S, D, R) GET_MSB_8((~S & D & ~R) | (S & ~D & R))
#define VFLAG_SUB_16(S, D, R) GET_MSB_16((~S & D & ~R) | (S & ~D & R))
#define VFLAG_SUB_32(S, D, R) GET_MSB_32((~S & D & ~R) | (S & ~D & R))

#define CFLAG_SUB_8(S, D, R) GET_MSB_8((S & ~D) | (R & ~D) | (S & R))
#define CFLAG_SUB_16(S, D, R) GET_MSB_16((S & ~D) | (R & ~D) | (S & R))
#define CFLAG_SUB_32(S, D, R) GET_MSB_32((S & ~D) | (R & ~D) | (S & R))


/* Conditions */
#define CONDITION_HI     (CPU_C == 0 && CPU_NOT_Z != 0)
#define CONDITION_NOT_HI (CPU_C != 0 || CPU_NOT_Z == 0)
#define CONDITION_LS     (CPU_C != 0 || CPU_NOT_Z == 0)
#define CONDITION_NOT_LS (CPU_C == 0 && CPU_NOT_Z != 0)
#define CONDITION_CC     (CPU_C == 0)
#define CONDITION_NOT_CC (CPU_C != 0)
#define CONDITION_CS     (CPU_C != 0)
#define CONDITION_NOT_CS (CPU_C == 0)
#define CONDITION_NE     (CPU_NOT_Z != 0)
#define CONDITION_NOT_NE (CPU_NOT_Z == 0)
#define CONDITION_EQ     (CPU_NOT_Z == 0)
#define CONDITION_NOT_EQ (CPU_NOT_Z != 0)
#define CONDITION_VC     (CPU_V == 0)
#define CONDITION_NOT_VC (CPU_V != 0)
#define CONDITION_VS     (CPU_V != 0)
#define CONDITION_NOT_VS (CPU_V == 0)
#define CONDITION_PL     (CPU_N == 0)
#define CONDITION_NOT_PL (CPU_N != 0)
#define CONDITION_MI     (CPU_N != 0)
#define CONDITION_NOT_MI (CPU_N == 0)
#define CONDITION_GE     ((CPU_N == 0) == (CPU_V == 0))
#define CONDITION_NOT_GE ((CPU_N == 0) != (CPU_V == 0))
#define CONDITION_LT     ((CPU_N == 0) != (CPU_V == 0))
#define CONDITION_NOT_LT ((CPU_N == 0) == (CPU_V == 0))
#define CONDITION_GT     (CPU_NOT_Z != 0 && (CPU_N == 0) == (CPU_V == 0))
#define CONDITION_NOT_GT (CPU_NOT_Z == 0 || (CPU_N == 0) != (CPU_V == 0))
#define CONDITION_LE     (CPU_NOT_Z == 0 || (CPU_N == 0) != (CPU_V == 0))
#define CONDITION_NOT_LE (CPU_NOT_Z != 0 && (CPU_N == 0) == (CPU_V == 0))


/* Use up clock cycles.
 * NOTE: clock cycles used in here are 99.9% correct for a 68000, not for the
 * higher processors.
 */
#define USE_CLKS(A) m68k_clks_left -= (A)


/* Push/pull data to/from the stack */
#define m68ki_push_16(A) m68ki_write_16(CPU_A[7]-=2, A)
#define m68ki_push_32(A) m68ki_write_32(CPU_A[7]-=4, A)
#define m68ki_pull_16()  m68ki_read_16((CPU_A[7]+=2) - 2)
#define m68ki_pull_32()  m68ki_read_32((CPU_A[7]+=4) - 4)


/* branch byte and word are for branches, while long is for jumps.
 * So far it's been safe to not call set_pc() for branch word.
 */
#define m68ki_branch_byte(A) CPU_PC += MAKE_INT_8(A)
#define m68ki_branch_word(A) CPU_PC += MAKE_INT_16(A)
#define m68ki_branch_dword(A) CPU_PC += (A)
#define m68ki_branch_long(A) m68ki_set_pc(A)


/* Get the condition code register */
#define m68ki_get_ccr() (((CPU_X != 0)     << 4) | \
                         ((CPU_N != 0)     << 3) | \
                         ((CPU_NOT_Z == 0) << 2) | \
                         ((CPU_V != 0)     << 1) | \
                          (CPU_C != 0))

/* Get the status register */
#define m68ki_get_sr() (((CPU_T1 != 0)    << 15) | \
                        ((CPU_T0 != 0)    << 14) | \
                        ((CPU_S != 0)     << 13) | \
                        ((CPU_M != 0)     << 12) | \
                         (CPU_INT_MASK    <<  8) | \
                        ((CPU_X != 0)     <<  4) | \
                        ((CPU_N != 0)     <<  3) | \
                        ((CPU_NOT_Z == 0) <<  2) | \
                        ((CPU_V != 0)     <<  1) | \
                         (CPU_C != 0))



/* ======================================================================== */
/* ========================= CONFIGURATION DEFINES ======================== */
/* ======================================================================== */

/* Act on values in m68kconf.h */
#if M68K_INT_ACK
#define m68ki_int_ack(A) CPU_INT_ACK_CALLBACK(A)
#else
/* Default action is to used autovector mode, which is most common */
#define m68ki_int_ack(A) M68K_INT_ACK_AUTOVECTOR
#endif /* M68K_INT_ACK */

#if M68K_BKPT_ACK
#define m68ki_bkpt_ack(A) CPU_BKPT_ACK_CALLBACK(A)
#else
#define m68ki_bkpt_ack(A)
#endif /* M68K_BKPT_ACK */

#if M68K_OUTPUT_RESET
#define m68ki_output_reset() CPU_RESET_INSTR_CALLBACK()
#else
#define m68ki_output_reset()
#endif /* M68K_OUTPUT_RESET */

#if M68K_PC_CHANGED
#define m68ki_pc_changed(A) CPU_PC_CHANGED_CALLBACK(A)
#else
#define m68ki_pc_changed(A)
#endif /* M68K_PC_CHANGED */

#if M68K_SET_FC
#define m68ki_set_fc(A) CPU_SET_FC_CALLBACK(A)
#else
#define m68ki_set_fc(A)
#endif /* M68K_SET_FC */

#if M68K_INSTR_HOOK
#define m68ki_instr_hook() CPU_INSTR_HOOK_CALLBACK()
#else
#define m68ki_instr_hook()
#endif /* M68K_INSTR_HOOK */

#if M68K_TRACE
/* Initiates trace checking before each instruction (t1) */
#define m68ki_set_trace() m68k_tracing = CPU_T1
/* adds t0 to trace checking if we encounter change of flow */
#define m68ki_add_trace() m68k_tracing |= CPU_T0
/* Clear all tracing */
#define m68ki_clear_trace() m68k_tracing = 0
/* Cause a trace exception if we are tracing */
#define m68ki_exception_if_trace() if(m68k_tracing) m68ki_exception(EXCEPTION_TRACE)
#else
#define m68ki_set_trace()
#define m68ki_add_trace()
#define m68ki_clear_trace()
#define m68ki_exception_if_trace()
#endif /* M68K_TRACE */


#ifdef M68K_LOG

extern char* m68k_disassemble_quick(uint pc);
extern uint  m68k_pc_offset;
extern char* m68k_cpu_names[];

#define M68K_DO_LOG(A) if(M68K_LOG) fprintf A
#if M68K_LOG_EMULATED_INSTRUCTIONS
#define M68K_DO_LOG_EMU(A) if(M68K_LOG) fprintf A
#else
#define M68K_DO_LOG_EMU(A)
#endif

#else
#define M68K_DO_LOG(A)
#define M68K_DO_LOG_EMU(A)

#endif


/* ======================================================================== */
/* =============================== PROTOTYPES ============================= */
/* ======================================================================== */

typedef struct
{
   uint mode;        /* CPU Operation Mode: 68000, 68010, or 68020 */
   uint dr[8];       /* Data Registers */
   uint ar[8];       /* Address Registers */
   uint ppc;		 /* Previous program counter */
   uint pc;          /* Program Counter */
   uint sp[4];       /* User, Interrupt, and Master Stack Pointers */
   uint vbr;         /* Vector Base Register (68010+) */
   uint sfc;         /* Source Function Code Register (m68010+) */
   uint dfc;         /* Destination Function Code Register (m68010+) */
   uint cacr;        /* Cache Control Register (m68020+) */
   uint caar;        /* Cacge Address Register (m68020+) */
   uint ir;          /* Instruction Register */
   uint t1_flag;     /* Trace 1 */
   uint t0_flag;     /* Trace 0 */
   uint s_flag;      /* Supervisor */
   uint m_flag;      /* Master/Interrupt state */
   uint x_flag;      /* Extend */
   uint n_flag;      /* Negative */
   uint not_z_flag;  /* Zero, inverted for speedups */
   uint v_flag;      /* Overflow */
   uint c_flag;      /* Carry */
   uint int_mask;    /* I0-I2 */
   uint int_state;   /* Current interrupt state -- ASG: changed from ints_pending */
   uint stopped;     /* Stopped state */
   uint halted;      /* Halted state */
   uint int_cycles;  /* ASG: extra cycles from generated interrupts */
   uint pref_addr;   /* Last prefetch address */
   uint pref_data;   /* Data in the prefetch queue */

   /* Callbacks to host */
   int  (*int_ack_callback)(int int_line);  /* Interrupt Acknowledge */
   void (*bkpt_ack_callback)(int data);     /* Breakpoint Acknowledge */
   void (*reset_instr_callback)(void);      /* Called when a RESET instruction is encountered */
   void (*pc_changed_callback)(int new_pc); /* Called when the PC changes by a large amount */
   void (*set_fc_callback)(int new_fc);     /* Called when the CPU function code changes */
   void (*instr_hook_callback)(void);       /* Called every instruction cycle prior to execution */

} m68k_cpu_core;



extern int           m68k_clks_left;
extern uint          m68k_tracing;
extern uint          m68k_sr_implemented_bits[];
extern m68k_cpu_core m68k_cpu;
extern uint*         m68k_cpu_dar[];
extern uint*         m68k_movem_pi_table[];
extern uint*         m68k_movem_pd_table[];
extern uint8         m68k_int_masks[];
extern uint8         m68k_shift_8_table[];
extern uint16        m68k_shift_16_table[];
extern uint          m68k_shift_32_table[];
extern uint8         m68k_exception_cycle_table[];


/* Read data from anywhere */
INLINE uint m68ki_read_8  (uint address);
INLINE uint m68ki_read_16 (uint address);
INLINE uint m68ki_read_32 (uint address);

/* Write to memory */
INLINE void m68ki_write_8 (uint address, uint value);
INLINE void m68ki_write_16(uint address, uint value);
INLINE void m68ki_write_32(uint address, uint value);

/* Read data immediately after the program counter */
INLINE uint m68ki_read_imm_8(void);
INLINE uint m68ki_read_imm_16(void);
INLINE uint m68ki_read_imm_32(void);

/* Reads the next word after the program counter */
INLINE uint m68ki_read_instruction(void);

/* Read data with specific function code */
INLINE uint m68ki_read_8_fc  (uint address, uint fc);
INLINE uint m68ki_read_16_fc (uint address, uint fc);
INLINE uint m68ki_read_32_fc (uint address, uint fc);

/* Write data with specific function code */
INLINE void m68ki_write_8_fc (uint address, uint fc, uint value);
INLINE void m68ki_write_16_fc(uint address, uint fc, uint value);
INLINE void m68ki_write_32_fc(uint address, uint fc, uint value);

INLINE uint m68ki_get_ea_ix(void);     /* Get ea for address register indirect + index */
INLINE uint m68ki_get_ea_pcix(void);   /* Get ea for program counter indirect + index */
INLINE uint m68ki_get_ea_ix_dst(void); /* Get ea ix for destination of move instruction */

INLINE void m68ki_set_s_flag(int value);                 /* Set the S flag */
INLINE void m68ki_set_m_flag(int value);                 /* Set the M flag */
INLINE void m68ki_set_sm_flag(int s_value, int m_value); /* Set the S and M flags */
INLINE void m68ki_set_ccr(uint value);                   /* set the condition code register */
INLINE void m68ki_set_sr(uint value);                    /* set the status register */
INLINE void m68ki_set_sr_no_int(uint value);             /* ASG: set the status register, but don't check interrupts */
INLINE void m68ki_set_pc(uint address);                  /* set the program counter */
INLINE void m68ki_service_interrupt(uint pending_mask);  /* service a pending interrupt -- ASG: added parameter */
INLINE void m68ki_exception(uint vector);                /* process an exception */
INLINE void m68ki_interrupt(uint vector);				 /* process an interrupt */
INLINE void m68ki_check_interrupts(void); 				 /* ASG: check for interrupts */

/* ======================================================================== */
/* =========================== UTILITY FUNCTIONS ========================== */
/* ======================================================================== */

/* Set the function code and read memory from anywhere. */
INLINE uint m68ki_read_8(uint address)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   return m68k_read_memory_8(ADDRESS_68K(address));
}
INLINE uint m68ki_read_16(uint address)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   return m68k_read_memory_16(ADDRESS_68K(address));
}
INLINE uint m68ki_read_32(uint address)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   return m68k_read_memory_32(ADDRESS_68K(address));
}


/* Set the function code and write memory to anywhere. */
INLINE void m68ki_write_8(uint address, uint value)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   m68k_write_memory_8(ADDRESS_68K(address), value);
}
INLINE void m68ki_write_16(uint address, uint value)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   m68k_write_memory_16(ADDRESS_68K(address), value);
}
INLINE void m68ki_write_32(uint address, uint value)
{
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_DATA : FUNCTION_CODE_USER_DATA);
   m68k_write_memory_32(ADDRESS_68K(address), value);
}


/* Set the function code and read memory immediately following the PC. */
INLINE uint m68ki_read_imm_8(void)
{
#if M68K_USE_PREFETCH
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   if(MASK_OUT_BELOW_2(CPU_PC) != CPU_PREF_ADDR)
   {
      CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
      CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
   }
   CPU_PC += 2;
   return MASK_OUT_ABOVE_8(CPU_PREF_DATA >> ((2-((CPU_PC-2)&2))<<3));
#else
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   CPU_PC += 2;
   return m68k_read_immediate_8(ADDRESS_68K(CPU_PC-1));
#endif /* M68K_USE_PREFETCH */
}
INLINE uint m68ki_read_imm_16(void)
{
#if M68K_USE_PREFETCH
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   if(MASK_OUT_BELOW_2(CPU_PC) != CPU_PREF_ADDR)
   {
      CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
      CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
   }
   CPU_PC += 2;
   return MASK_OUT_ABOVE_16(CPU_PREF_DATA >> ((2-((CPU_PC-2)&2))<<3));
#else
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   CPU_PC += 2;
   return m68k_read_immediate_16(ADDRESS_68K(CPU_PC-2));
#endif /* M68K_USE_PREFETCH */
}
INLINE uint m68ki_read_imm_32(void)
{
#if M68K_USE_PREFETCH
   uint temp_val;

   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   if(MASK_OUT_BELOW_2(CPU_PC) != CPU_PREF_ADDR)
   {
      CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
      CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
   }
   temp_val = CPU_PREF_DATA;
   CPU_PC += 2;
   if(MASK_OUT_BELOW_2(CPU_PC) != CPU_PREF_ADDR)
   {
      CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
      CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
      temp_val = MASK_OUT_ABOVE_32((temp_val << 16) | (CPU_PREF_DATA >> 16));
   }
   CPU_PC += 2;

   return temp_val;
#else
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   CPU_PC += 4;
   return m68k_read_immediate_32(ADDRESS_68K(CPU_PC-4));
#endif /* M68K_USE_PREFETCH */
}


/* Set the function code and read an instruction immediately following the PC. */
INLINE uint m68ki_read_instruction(void)
{
#if M68K_USE_PREFETCH
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   if(MASK_OUT_BELOW_2(CPU_PC) != CPU_PREF_ADDR)
   {
      CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
      CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
   }
   CPU_PC += 2;
   return MASK_OUT_ABOVE_16(CPU_PREF_DATA >> ((2-((CPU_PC-2)&2))<<3));
#else
   m68ki_set_fc(CPU_S ? FUNCTION_CODE_SUPERVISOR_PROGRAM : FUNCTION_CODE_USER_PROGRAM);
   CPU_PC += 2;
   return m68k_read_instruction(ADDRESS_68K(CPU_PC-2));
#endif /* M68k_USE_PREFETCH */
}


/* Read/Write data with a specific function code (used by MOVES) */
INLINE uint m68ki_read_8_fc(uint address, uint fc)
{
   m68ki_set_fc(fc&7);
   return m68k_read_memory_8(ADDRESS_68K(address));
}
INLINE uint m68ki_read_16_fc(uint address, uint fc)
{
   m68ki_set_fc(fc&7);
   return m68k_read_memory_16(ADDRESS_68K(address));
}
INLINE uint m68ki_read_32_fc(uint address, uint fc)
{
   m68ki_set_fc(fc&7);
   return m68k_read_memory_32(ADDRESS_68K(address));
}

INLINE void m68ki_write_8_fc(uint address, uint fc, uint value)
{
   m68ki_set_fc(fc&7);
   m68k_write_memory_8(ADDRESS_68K(address), value);
}
INLINE void m68ki_write_16_fc(uint address, uint fc, uint value)
{
   m68ki_set_fc(fc&7);
   m68k_write_memory_16(ADDRESS_68K(address), value);
}
INLINE void m68ki_write_32_fc(uint address, uint fc, uint value)
{
   m68ki_set_fc(fc&7);
   m68k_write_memory_32(ADDRESS_68K(address), value);
}


/* Decode address register indirect with index */
INLINE uint m68ki_get_ea_ix(void)
{
   uint extension = m68ki_read_imm_16();
   uint ea_index = m68k_cpu_dar[EXT_INDEX_AR(extension)!=0][EXT_INDEX_REGISTER(extension)];
   uint base = AY;
   uint outer = 0;

   /* Sign-extend the index value if needed */
   if(!EXT_INDEX_LONG(extension))
      ea_index = MAKE_INT_16(ea_index);

   /* If we're running 010 or less, there's no scale or full extension word mode */
   if(CPU_MODE & CPU_MODE_010_LESS)
      return base + ea_index + MAKE_INT_8(extension);

   /* Scale the index value */
   ea_index <<= EXT_INDEX_SCALE(extension);

   /* If we're using brief extension mode, we are done */
   if(EXT_BRIEF_FORMAT(extension))
      return base + ea_index + MAKE_INT_8(extension);

   /* Decode the long extension format */
   if(EXT_IX_SUPPRESSED(extension))
      ea_index = 0;
   if(EXT_BR_SUPPRESSED(extension))
      base = 0;
   if(EXT_BD_PRESENT(extension))
      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_NO_MEMORY_INDIRECT(extension))
      return base + ea_index;

   if(EXT_OD_PRESENT(extension))
      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_POSTINDEX(extension))
      return m68ki_read_32(base) + ea_index + outer;
   return m68ki_read_32(base + ea_index) + outer;
}

/* Decode address register indirect with index for MOVE destination */
INLINE uint m68ki_get_ea_ix_dst(void)
{
   uint extension = m68ki_read_imm_16();
   uint ea_index = m68k_cpu_dar[EXT_INDEX_AR(extension)!=0][EXT_INDEX_REGISTER(extension)];
   uint base = AX; /* This is the only thing different from m68ki_get_ea_ix() */
   uint outer = 0;

   /* Sign-extend the index value if needed */
   if(!EXT_INDEX_LONG(extension))
      ea_index = MAKE_INT_16(ea_index);

   /* If we're running 010 or less, there's no scale or full extension word mode */
   if(CPU_MODE & CPU_MODE_010_LESS)
      return base + ea_index + MAKE_INT_8(extension);

   /* Scale the index value */
   ea_index <<= EXT_INDEX_SCALE(extension);

   /* If we're using brief extension mode, we are done */
   if(EXT_BRIEF_FORMAT(extension))
      return base + ea_index + MAKE_INT_8(extension);

   /* Decode the long extension format */
   if(EXT_IX_SUPPRESSED(extension))
      ea_index = 0;
   if(EXT_BR_SUPPRESSED(extension))
      base = 0;
   if(EXT_BD_PRESENT(extension))
      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_NO_MEMORY_INDIRECT(extension))
      return base + ea_index;

   if(EXT_OD_PRESENT(extension))
      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_POSTINDEX(extension))
      return m68ki_read_32(base) + ea_index + outer;
   return m68ki_read_32(base + ea_index) + outer;
}

/* Decode program counter indirect with index */
INLINE uint m68ki_get_ea_pcix(void)
{
   uint base = (CPU_PC+=2) - 2;
   uint extension = m68ki_read_16(base);
   uint ea_index = m68k_cpu_dar[EXT_INDEX_AR(extension)!=0][EXT_INDEX_REGISTER(extension)];
   uint outer = 0;

   /* Sign-extend the index value if needed */
   if(!EXT_INDEX_LONG(extension))
      ea_index = MAKE_INT_16(ea_index);

   /* If we're running 010 or less, there's no scale or full extension word mode */
   if(CPU_MODE & CPU_MODE_010_LESS)
      return base + ea_index + MAKE_INT_8(extension);

   /* Scale the index value */
   ea_index <<= EXT_INDEX_SCALE(extension);

   /* If we're using brief extension mode, we are done */
   if(EXT_BRIEF_FORMAT(extension))
      return base + ea_index + MAKE_INT_8(extension);

   /* Decode the long extension format */
   if(EXT_IX_SUPPRESSED(extension))
      ea_index = 0;
   if(EXT_BR_SUPPRESSED(extension))
      base = 0;
   if(EXT_BD_PRESENT(extension))
      base += EXT_BD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_NO_MEMORY_INDIRECT(extension))
      return base + ea_index;

   if(EXT_OD_PRESENT(extension))
      outer = EXT_OD_LONG(extension) ? m68ki_read_imm_32() : MAKE_INT_16(m68ki_read_imm_16());
   if(EXT_POSTINDEX(extension))
      return m68ki_read_32(base) + ea_index + outer;
   return m68ki_read_32(base + ea_index) + outer;
}


/* Set the S flag and change the active stack pointer. */
INLINE void m68ki_set_s_flag(int value)
{
   /* ASG: Only do the rest if we're changing */
   value = (value != 0);
   if (CPU_S != value)
   {
      /* Backup the old stack pointer */
      CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))] = CPU_A[7];
      /* Set the S flag */
      CPU_S = value;
      /* Set the new stack pointer */
      CPU_A[7] = CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))];
   }
}

/* Set the M flag and change the active stack pointer. */
INLINE void m68ki_set_m_flag(int value)
{
   /* ASG: Only do the rest if we're changing */
   value = (value != 0 && CPU_MODE & CPU_MODE_EC020_PLUS)<<1;
   if (CPU_M != value)
   {
      /* Backup the old stack pointer */
      CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))] = CPU_A[7];
      /* Set the M flag */
      CPU_M = value;
      /* Set the new stack pointer */
      CPU_A[7] = CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))];
   }
}

/* Set the S and M flags and change the active stack pointer. */
INLINE void m68ki_set_sm_flag(int s_value, int m_value)
{
   /* ASG: Only do the rest if we're changing */
   s_value = (s_value != 0);
   m_value = (m_value != 0 && CPU_MODE & CPU_MODE_EC020_PLUS)<<1;
   if (CPU_S != s_value || CPU_M != m_value)
   {
      /* Backup the old stack pointer */
      CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))] = CPU_A[7];
      /* Set the S and M flags */
      CPU_S = s_value != 0;
      CPU_M = (m_value != 0 && CPU_MODE & CPU_MODE_EC020_PLUS)<<1;
      /* Set the new stack pointer */
      CPU_A[7] = CPU_SP[CPU_S | (CPU_M & (CPU_S<<1))];
   }
}


/* Set the condition code register */
INLINE void m68ki_set_ccr(uint value)
{
   CPU_X = BIT_4(value);
   CPU_N = BIT_3(value);
   CPU_NOT_Z = !BIT_2(value);
   CPU_V = BIT_1(value);
   CPU_C = BIT_0(value);
}

/* Set the status register */
INLINE void m68ki_set_sr(uint value)
{
   /* ASG: detect changes to the INT_MASK */
   int old_mask = CPU_INT_MASK;

   /* Mask out the "unimplemented" bits */
   value &= m68k_sr_implemented_bits[CPU_MODE];

   /* Now set the status register */
   CPU_T1 = BIT_F(value);
   CPU_T0 = BIT_E(value);
   CPU_INT_MASK = (value >> 8) & 7;
   CPU_X = BIT_4(value);
   CPU_N = BIT_3(value);
   CPU_NOT_Z = !BIT_2(value);
   CPU_V = BIT_1(value);
   CPU_C = BIT_0(value);
   m68ki_set_sm_flag(BIT_D(value), BIT_C(value));

   /* ASG: detect changes to the INT_MASK */
   if (CPU_INT_MASK != old_mask)
      m68ki_check_interrupts();
}


/* Set the status register */
INLINE void m68ki_set_sr_no_int(uint value)
{
   /* Mask out the "unimplemented" bits */
   value &= m68k_sr_implemented_bits[CPU_MODE];

   /* Now set the status register */
   CPU_T1 = BIT_F(value);
   CPU_T0 = BIT_E(value);
   CPU_INT_MASK = (value >> 8) & 7;
   CPU_X = BIT_4(value);
   CPU_N = BIT_3(value);
   CPU_NOT_Z = !BIT_2(value);
   CPU_V = BIT_1(value);
   CPU_C = BIT_0(value);
   m68ki_set_sm_flag(BIT_D(value), BIT_C(value));
}


/* I set the PC this way to let host programs be nicer.
 * This is mainly for programs running from separate ram banks.
 * If the host program knows where the PC is, it can offer faster
 * ram access times for data to be retrieved immediately following
 * the PC.
 */
INLINE void m68ki_set_pc(uint address)
{
   /* Set the program counter */
   CPU_PC = address;
   /* Inform the host program */
/* MAME */
   change_pc24(ADDRESS_68K(address));
/*
   m68ki_pc_changed(ADDRESS_68K(address));
*/
}


/* Process an exception */
INLINE void m68ki_exception(uint vector)
{
   /* Save the old status register */
   uint old_sr = m68ki_get_sr();

   /* Use up some clock cycles */
   USE_CLKS(m68k_exception_cycle_table[vector]);

   /* Turn off stopped state and trace flag, clear pending traces */
   CPU_STOPPED = 0;
   CPU_T1 = CPU_T0 = 0;
   m68ki_clear_trace();
   /* Enter supervisor mode */
   m68ki_set_s_flag(1);
   /* Push a stack frame */
   if(CPU_MODE & CPU_MODE_010_PLUS)
      m68ki_push_16(vector<<2); /* This is format 0 */
   m68ki_push_32(CPU_PPC);	/* save previous PC, ie. PC that contains an offending instruction */
   m68ki_push_16(old_sr);
   /* Generate a new program counter from the vector */
   m68ki_set_pc(m68ki_read_32((vector<<2)+CPU_VBR));
}


/* Process an interrupt (or trap) */
INLINE void m68ki_interrupt(uint vector)
{
   /* Save the old status register */
   uint old_sr = m68ki_get_sr();

   /* Use up some clock cycles */
   /* ASG: just keep them pending */
/* USE_CLKS(m68k_exception_cycle_table[vector]);*/
   CPU_INT_CYCLES += m68k_exception_cycle_table[vector];

   /* Turn off stopped state and trace flag, clear pending traces */
   CPU_STOPPED = 0;
   CPU_T1 = CPU_T0 = 0;
   m68ki_clear_trace();
   /* Enter supervisor mode */
   m68ki_set_s_flag(1);
   /* Push a stack frame */
   if(CPU_MODE & CPU_MODE_010_PLUS)
      m68ki_push_16(vector<<2); /* This is format 0 */
   m68ki_push_32(CPU_PC);
   m68ki_push_16(old_sr);
   /* Generate a new program counter from the vector */
   m68ki_set_pc(m68ki_read_32((vector<<2)+CPU_VBR));
}


/* Service an interrupt request */
INLINE void m68ki_service_interrupt(uint pending_mask)	/* ASG: added parameter here */
{
   uint int_level = 7;
   uint vector;

   /* Start at level 7 and then go down */
   for(;!(pending_mask & (1<<int_level));int_level--)	/* ASG: changed to use parameter instead of CPU_INTS_PENDING */
      ;

   /* Get the exception vector */
   switch(vector = m68ki_int_ack(int_level))
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
      case M68K_INT_ACK_AUTOVECTOR:
         /* Use the autovectors.  This is the most commonly used implementation */
         vector = EXCEPTION_INTERRUPT_AUTOVECTOR+int_level;
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
   if(m68ki_read_32(vector<<2) == 0)
      vector = EXCEPTION_UNINITIALIZED_INTERRUPT;

   /* Generate an interupt */
   m68ki_interrupt(vector);

   /* Set the interrupt mask to the level of the one being serviced */
   CPU_INT_MASK = int_level;
}


/* ASG: Check for interrupts */
INLINE void m68ki_check_interrupts(void)
{
   uint pending_mask = 1 << CPU_INT_STATE;
   if (pending_mask & m68k_int_masks[CPU_INT_MASK])
      m68ki_service_interrupt(pending_mask);
}


#endif /* M68KCPU__HEADER */
