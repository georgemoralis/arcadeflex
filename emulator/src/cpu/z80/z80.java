package cpu.z80;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.z80.z80H.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;


public class z80 extends cpu_interface {
    int[] z80_ICount = new int[1];
    public z80()
    {    
        cpu_num = CPU_Z80;
        num_irqs = 1;
        default_vector = 255;
        overclock = 1.0;
        no_int = Z80_IGNORE_INT;
        irq_int = Z80_IRQ_INT;
        nmi_int = Z80_NMI_INT;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = z80_ICount;
        //intialize interfaces
        burn = burn_function;
    }
    /*TODO*////* execute main opcodes inside a big switch statement */
    /*TODO*///#ifndef BIG_SWITCH
    /*TODO*///#define BIG_SWITCH          1
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* big flags array for ADD/ADC/SUB/SBC/CP results */
    /*TODO*///#define BIG_FLAGS_ARRAY     1
    /*TODO*///
    /*TODO*////* Set to 1 for a more exact (but somewhat slower) Z80 emulation */
    /*TODO*///#define Z80_EXACT			1
    /*TODO*///
    /*TODO*////* repetitive commands (ldir,cpdr etc.) repeat at
    /*TODO*///   once until cycles used up or B(C) counted down. */
    /*TODO*///#define REPEAT_AT_ONCE		1
    /*TODO*///
    /*TODO*////* on JP and JR opcodes check for tight loops */
    /*TODO*///#define BUSY_LOOP_HACKS 	1
    /*TODO*///
    /*TODO*////* check for delay loops counting down BC */
    /*TODO*///#define TIME_LOOP_HACKS 	1
    /*TODO*///
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#undef	BIG_FLAGS_ARRAY
    /*TODO*///#define BIG_FLAGS_ARRAY 	0
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///static UINT8 z80_reg_layout[] = {
    /*TODO*///    Z80_PC, Z80_SP, Z80_AF, Z80_BC, Z80_DE, Z80_HL, -1,
    /*TODO*///    Z80_IX, Z80_IY, Z80_AF2,Z80_BC2,Z80_DE2,Z80_HL2,-1,
    /*TODO*///    Z80_R,  Z80_I,  Z80_IM, Z80_IFF1,Z80_IFF2, -1,
    /*TODO*///	Z80_NMI_STATE,Z80_IRQ_STATE,Z80_DC0,Z80_DC1,Z80_DC2,Z80_DC3, 0
    /*TODO*///};
    /*TODO*///
    /*TODO*///static UINT8 z80_win_layout[] = {
    /*TODO*///	27, 0,53, 4,	/* register window (top rows) */
    /*TODO*///	 0, 0,26,22,	/* disassembler window (left colums) */
    /*TODO*///	27, 5,53, 8,	/* memory #1 window (right, upper middle) */
    /*TODO*///	27,14,53, 8,	/* memory #2 window (right, lower middle) */
    /*TODO*///	 0,23,80, 1,	/* command line window (bottom rows) */
    /*TODO*///};
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* The Z80 registers. HALT is set to 1 when the CPU is halted, the refresh  */
    /*TODO*////* register is calculated as follows: refresh=(Regs.R&127)|(Regs.R2&128)    */
    /*TODO*////****************************************************************************/
    /*TODO*///typedef struct {
    /*TODO*////* 00 */    PAIR    PREPC,PC,SP,AF,BC,DE,HL,IX,IY;
    /*TODO*////* 24 */    PAIR    AF2,BC2,DE2,HL2;
    /*TODO*////* 34 */    UINT8   R,R2,IFF1,IFF2,HALT,IM,I;
    /*TODO*////* 3B */    UINT8   irq_max;            /* number of daisy chain devices        */
    /*TODO*////* 3C */	INT8	request_irq;		/* daisy chain next request device		*/
    /*TODO*////* 3D */	INT8	service_irq;		/* daisy chain next reti handling device */
    /*TODO*////* 3E */	UINT8	nmi_state;			/* nmi line state */
    /*TODO*////* 3F */	UINT8	irq_state;			/* irq line state */
    /*TODO*////* 40 */    UINT8   int_state[Z80_MAXDAISY];
    /*TODO*////* 44 */    Z80_DaisyChain irq[Z80_MAXDAISY];
    /*TODO*////* 84 */    int     (*irq_callback)(int irqline);
    /*TODO*////* 88 */    int     extra_cycles;       /* extra cycles for interrupts */
    /*TODO*///}   Z80_Regs;
    /*TODO*///
    public static final int CF  =0x01;
    public static final int NF	=0x02;
    public static final int PF	=0x04;
    public static final int VF	=PF;
    public static final int XF	=0x08;
    public static final int HF	=0x10;
    public static final int YF	=0x20;
    public static final int ZF	=0x40;
    public static final int SF	=0x80;
    /*TODO*///
    /*TODO*///#define INT_IRQ 0x01
    /*TODO*///#define NMI_IRQ 0x02
    /*TODO*///
    /*TODO*///#define	_PPC	Z80.PREPC.d		/* previous program counter */
    /*TODO*///
    /*TODO*///#define _PCD	Z80.PC.d
    /*TODO*///#define _PC 	Z80.PC.w.l
    /*TODO*///
    /*TODO*///#define _SPD	Z80.SP.d
    /*TODO*///#define _SP 	Z80.SP.w.l
    /*TODO*///
    /*TODO*///#define _AFD	Z80.AF.d
    /*TODO*///#define _AF 	Z80.AF.w.l
    /*TODO*///#define _A		Z80.AF.b.h
    /*TODO*///#define _F		Z80.AF.b.l
    /*TODO*///
    /*TODO*///#define _BCD	Z80.BC.d
    /*TODO*///#define _BC 	Z80.BC.w.l
    /*TODO*///#define _B		Z80.BC.b.h
    /*TODO*///#define _C		Z80.BC.b.l
    /*TODO*///
    /*TODO*///#define _DED	Z80.DE.d
    /*TODO*///#define _DE 	Z80.DE.w.l
    /*TODO*///#define _D		Z80.DE.b.h
    /*TODO*///#define _E		Z80.DE.b.l
    /*TODO*///
    /*TODO*///#define _HLD	Z80.HL.d
    /*TODO*///#define _HL 	Z80.HL.w.l
    /*TODO*///#define _H		Z80.HL.b.h
    /*TODO*///#define _L		Z80.HL.b.l
    /*TODO*///
    /*TODO*///#define _IXD	Z80.IX.d
    /*TODO*///#define _IX 	Z80.IX.w.l
    /*TODO*///#define _HX 	Z80.IX.b.h
    /*TODO*///#define _LX 	Z80.IX.b.l
    /*TODO*///
    /*TODO*///#define _IYD	Z80.IY.d
    /*TODO*///#define _IY 	Z80.IY.w.l
    /*TODO*///#define _HY 	Z80.IY.b.h
    /*TODO*///#define _LY 	Z80.IY.b.l
    /*TODO*///
    /*TODO*///#define _I      Z80.I
    /*TODO*///#define _R      Z80.R
    /*TODO*///#define _R2     Z80.R2
    /*TODO*///#define _IM     Z80.IM
    /*TODO*///#define _IFF1	Z80.IFF1
    /*TODO*///#define _IFF2	Z80.IFF2
    /*TODO*///#define _HALT	Z80.HALT
    /*TODO*///
    /*TODO*///int z80_ICount;
    /*TODO*///static Z80_Regs Z80;
    /*TODO*///static UINT32 EA;
    /*TODO*///static int after_EI = 0;
    /*TODO*///
    /*TODO*///static UINT8 SZ[256];		/* zero and sign flags */
    /*TODO*///static UINT8 SZ_BIT[256];	/* zero, sign and parity/overflow (=zero) flags for BIT opcode */
    /*TODO*///static UINT8 SZP[256];		/* zero, sign and parity flags */
    /*TODO*///static UINT8 SZHV_inc[256]; /* zero, sign, half carry and overflow flags INC r8 */
    /*TODO*///static UINT8 SZHV_dec[256]; /* zero, sign, half carry and overflow flags DEC r8 */
    /*TODO*///#include "z80daa.h"
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#include <signal.h>
    /*TODO*///static UINT8 *SZHVC_add = 0;
    /*TODO*///static UINT8 *SZHVC_sub = 0;
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///#if Z80_EXACT
    /*TODO*////* tmp1 value for ini/inir/outi/otir for [C.1-0][io.1-0] */
    /*TODO*///static UINT8 irep_tmp1[4][4] = {
    /*TODO*///	{0,0,1,0},{0,1,0,1},{1,0,1,1},{0,1,1,0}
    /*TODO*///};
    /*TODO*///
    /*TODO*////* tmp1 value for ind/indr/outd/otdr for [C.1-0][io.1-0] */
    /*TODO*///static UINT8 drep_tmp1[4][4] = {
    /*TODO*///	{0,1,0,0},{1,0,0,1},{0,0,1,0},{0,1,0,1}
    /*TODO*///};
    /*TODO*///
    /*TODO*////* tmp2 value for all in/out repeated opcodes for B.7-0 */
    /*TODO*///static UINT8 breg_tmp2[256] = {
    /*TODO*///	0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
    /*TODO*///	0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
    /*TODO*///	1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1
    /*TODO*///};
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///static UINT8 cc_op[0x100] = {
    /*TODO*/// 4,10, 7, 6, 4, 4, 7, 4, 4,11, 7, 6, 4, 4, 7, 4,
    /*TODO*/// 8,10, 7, 6, 4, 4, 7, 4,12,11, 7, 6, 4, 4, 7, 4,
    /*TODO*/// 7,10,16, 6, 4, 4, 7, 4, 7,11,16, 6, 4, 4, 7, 4,
    /*TODO*/// 7,10,13, 6,11,11,10, 4, 7,11,13, 6, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 7, 7, 7, 7, 7, 7, 4, 7, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
    /*TODO*/// 5,10,10,10,10,11, 7,11, 5, 4,10, 0,10,10, 7,11,
    /*TODO*/// 5,10,10,11,10,11, 7,11, 5, 4,10,11,10, 0, 7,11,
    /*TODO*/// 5,10,10,19,10,11, 7,11, 5, 4,10, 4,10, 0, 7,11,
    /*TODO*/// 5,10,10, 4,10,11, 7,11, 5, 6,10, 4,10, 0, 7,11};
    /*TODO*///
    /*TODO*///
    /*TODO*///static UINT8 cc_cb[0x100] = {
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8};
    /*TODO*///
    /*TODO*///static UINT8 cc_dd[0x100] = {
    /*TODO*/// 4, 4, 4, 4, 4, 4, 4, 4, 4,15, 4, 4, 4, 4, 4, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 4, 4, 4,15, 4, 4, 4, 4, 4, 4,
    /*TODO*/// 4,14,20,10, 9, 9, 9, 4, 4,15,20,10, 9, 9, 9, 4,
    /*TODO*/// 4, 4, 4, 4,23,23,19, 4, 4,15, 4, 4, 4, 4, 4, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 9, 9, 9, 9, 9, 9,19, 9, 9, 9, 9, 9, 9, 9,19, 9,
    /*TODO*///19,19,19,19,19,19, 4,19, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    /*TODO*/// 4,14, 4,23, 4,15, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4,
    /*TODO*/// 4, 4, 4, 4, 4, 4, 4, 4, 4,10, 4, 4, 4, 4, 4, 4};
    /*TODO*///
    /*TODO*///// dd/fd cycles are identical
    /*TODO*///#define cc_fd cc_dd
    /*TODO*///
    /*TODO*///static UINT8 cc_xxcb[0x100] = {
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,
    /*TODO*///20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,
    /*TODO*///20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,
    /*TODO*///20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,20,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,
    /*TODO*///23,23,23,23,23,23,23,23,23,23,23,23,23,23,23,23};
    /*TODO*///
    /*TODO*///static UINT8 cc_ed[0x100] = {
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*///12,12,15,20, 8, 8, 8, 9,12,12,15,20, 8, 8, 8, 9,
    /*TODO*///12,12,15,20, 8, 8, 8, 9,12,12,15,20, 8, 8, 8, 9,
    /*TODO*///12,12,15,20, 8, 8, 8,18,12,12,15,20, 8, 8, 8,18,
    /*TODO*///12,12,15,20, 8, 8, 8, 8,12,12,15,20, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*///16,16,16,16, 8, 8, 8, 8,16,16,16,16, 8, 8, 8, 8,
    /*TODO*///16,16,16,16, 8, 8, 8, 8,16,16,16,16, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    /*TODO*/// 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};
    /*TODO*///
    /*TODO*///static void take_interrupt(void);
    /*TODO*///
    /*TODO*///#define PROTOTYPES(tablename,prefix) \
    /*TODO*///	INLINE void prefix##_00(void); INLINE void prefix##_01(void); INLINE void prefix##_02(void); INLINE void prefix##_03(void); \
    /*TODO*///	INLINE void prefix##_04(void); INLINE void prefix##_05(void); INLINE void prefix##_06(void); INLINE void prefix##_07(void); \
    /*TODO*///	INLINE void prefix##_08(void); INLINE void prefix##_09(void); INLINE void prefix##_0a(void); INLINE void prefix##_0b(void); \
    /*TODO*///	INLINE void prefix##_0c(void); INLINE void prefix##_0d(void); INLINE void prefix##_0e(void); INLINE void prefix##_0f(void); \
    /*TODO*///	INLINE void prefix##_10(void); INLINE void prefix##_11(void); INLINE void prefix##_12(void); INLINE void prefix##_13(void); \
    /*TODO*///	INLINE void prefix##_14(void); INLINE void prefix##_15(void); INLINE void prefix##_16(void); INLINE void prefix##_17(void); \
    /*TODO*///	INLINE void prefix##_18(void); INLINE void prefix##_19(void); INLINE void prefix##_1a(void); INLINE void prefix##_1b(void); \
    /*TODO*///	INLINE void prefix##_1c(void); INLINE void prefix##_1d(void); INLINE void prefix##_1e(void); INLINE void prefix##_1f(void); \
    /*TODO*///	INLINE void prefix##_20(void); INLINE void prefix##_21(void); INLINE void prefix##_22(void); INLINE void prefix##_23(void); \
    /*TODO*///	INLINE void prefix##_24(void); INLINE void prefix##_25(void); INLINE void prefix##_26(void); INLINE void prefix##_27(void); \
    /*TODO*///	INLINE void prefix##_28(void); INLINE void prefix##_29(void); INLINE void prefix##_2a(void); INLINE void prefix##_2b(void); \
    /*TODO*///	INLINE void prefix##_2c(void); INLINE void prefix##_2d(void); INLINE void prefix##_2e(void); INLINE void prefix##_2f(void); \
    /*TODO*///	INLINE void prefix##_30(void); INLINE void prefix##_31(void); INLINE void prefix##_32(void); INLINE void prefix##_33(void); \
    /*TODO*///	INLINE void prefix##_34(void); INLINE void prefix##_35(void); INLINE void prefix##_36(void); INLINE void prefix##_37(void); \
    /*TODO*///	INLINE void prefix##_38(void); INLINE void prefix##_39(void); INLINE void prefix##_3a(void); INLINE void prefix##_3b(void); \
    /*TODO*///	INLINE void prefix##_3c(void); INLINE void prefix##_3d(void); INLINE void prefix##_3e(void); INLINE void prefix##_3f(void); \
    /*TODO*///	INLINE void prefix##_40(void); INLINE void prefix##_41(void); INLINE void prefix##_42(void); INLINE void prefix##_43(void); \
    /*TODO*///	INLINE void prefix##_44(void); INLINE void prefix##_45(void); INLINE void prefix##_46(void); INLINE void prefix##_47(void); \
    /*TODO*///	INLINE void prefix##_48(void); INLINE void prefix##_49(void); INLINE void prefix##_4a(void); INLINE void prefix##_4b(void); \
    /*TODO*///	INLINE void prefix##_4c(void); INLINE void prefix##_4d(void); INLINE void prefix##_4e(void); INLINE void prefix##_4f(void); \
    /*TODO*///	INLINE void prefix##_50(void); INLINE void prefix##_51(void); INLINE void prefix##_52(void); INLINE void prefix##_53(void); \
    /*TODO*///	INLINE void prefix##_54(void); INLINE void prefix##_55(void); INLINE void prefix##_56(void); INLINE void prefix##_57(void); \
    /*TODO*///	INLINE void prefix##_58(void); INLINE void prefix##_59(void); INLINE void prefix##_5a(void); INLINE void prefix##_5b(void); \
    /*TODO*///	INLINE void prefix##_5c(void); INLINE void prefix##_5d(void); INLINE void prefix##_5e(void); INLINE void prefix##_5f(void); \
    /*TODO*///	INLINE void prefix##_60(void); INLINE void prefix##_61(void); INLINE void prefix##_62(void); INLINE void prefix##_63(void); \
    /*TODO*///	INLINE void prefix##_64(void); INLINE void prefix##_65(void); INLINE void prefix##_66(void); INLINE void prefix##_67(void); \
    /*TODO*///	INLINE void prefix##_68(void); INLINE void prefix##_69(void); INLINE void prefix##_6a(void); INLINE void prefix##_6b(void); \
    /*TODO*///	INLINE void prefix##_6c(void); INLINE void prefix##_6d(void); INLINE void prefix##_6e(void); INLINE void prefix##_6f(void); \
    /*TODO*///	INLINE void prefix##_70(void); INLINE void prefix##_71(void); INLINE void prefix##_72(void); INLINE void prefix##_73(void); \
    /*TODO*///	INLINE void prefix##_74(void); INLINE void prefix##_75(void); INLINE void prefix##_76(void); INLINE void prefix##_77(void); \
    /*TODO*///	INLINE void prefix##_78(void); INLINE void prefix##_79(void); INLINE void prefix##_7a(void); INLINE void prefix##_7b(void); \
    /*TODO*///	INLINE void prefix##_7c(void); INLINE void prefix##_7d(void); INLINE void prefix##_7e(void); INLINE void prefix##_7f(void); \
    /*TODO*///	INLINE void prefix##_80(void); INLINE void prefix##_81(void); INLINE void prefix##_82(void); INLINE void prefix##_83(void); \
    /*TODO*///	INLINE void prefix##_84(void); INLINE void prefix##_85(void); INLINE void prefix##_86(void); INLINE void prefix##_87(void); \
    /*TODO*///	INLINE void prefix##_88(void); INLINE void prefix##_89(void); INLINE void prefix##_8a(void); INLINE void prefix##_8b(void); \
    /*TODO*///	INLINE void prefix##_8c(void); INLINE void prefix##_8d(void); INLINE void prefix##_8e(void); INLINE void prefix##_8f(void); \
    /*TODO*///	INLINE void prefix##_90(void); INLINE void prefix##_91(void); INLINE void prefix##_92(void); INLINE void prefix##_93(void); \
    /*TODO*///	INLINE void prefix##_94(void); INLINE void prefix##_95(void); INLINE void prefix##_96(void); INLINE void prefix##_97(void); \
    /*TODO*///	INLINE void prefix##_98(void); INLINE void prefix##_99(void); INLINE void prefix##_9a(void); INLINE void prefix##_9b(void); \
    /*TODO*///	INLINE void prefix##_9c(void); INLINE void prefix##_9d(void); INLINE void prefix##_9e(void); INLINE void prefix##_9f(void); \
    /*TODO*///	INLINE void prefix##_a0(void); INLINE void prefix##_a1(void); INLINE void prefix##_a2(void); INLINE void prefix##_a3(void); \
    /*TODO*///	INLINE void prefix##_a4(void); INLINE void prefix##_a5(void); INLINE void prefix##_a6(void); INLINE void prefix##_a7(void); \
    /*TODO*///	INLINE void prefix##_a8(void); INLINE void prefix##_a9(void); INLINE void prefix##_aa(void); INLINE void prefix##_ab(void); \
    /*TODO*///	INLINE void prefix##_ac(void); INLINE void prefix##_ad(void); INLINE void prefix##_ae(void); INLINE void prefix##_af(void); \
    /*TODO*///	INLINE void prefix##_b0(void); INLINE void prefix##_b1(void); INLINE void prefix##_b2(void); INLINE void prefix##_b3(void); \
    /*TODO*///	INLINE void prefix##_b4(void); INLINE void prefix##_b5(void); INLINE void prefix##_b6(void); INLINE void prefix##_b7(void); \
    /*TODO*///	INLINE void prefix##_b8(void); INLINE void prefix##_b9(void); INLINE void prefix##_ba(void); INLINE void prefix##_bb(void); \
    /*TODO*///	INLINE void prefix##_bc(void); INLINE void prefix##_bd(void); INLINE void prefix##_be(void); INLINE void prefix##_bf(void); \
    /*TODO*///	INLINE void prefix##_c0(void); INLINE void prefix##_c1(void); INLINE void prefix##_c2(void); INLINE void prefix##_c3(void); \
    /*TODO*///	INLINE void prefix##_c4(void); INLINE void prefix##_c5(void); INLINE void prefix##_c6(void); INLINE void prefix##_c7(void); \
    /*TODO*///	INLINE void prefix##_c8(void); INLINE void prefix##_c9(void); INLINE void prefix##_ca(void); INLINE void prefix##_cb(void); \
    /*TODO*///	INLINE void prefix##_cc(void); INLINE void prefix##_cd(void); INLINE void prefix##_ce(void); INLINE void prefix##_cf(void); \
    /*TODO*///	INLINE void prefix##_d0(void); INLINE void prefix##_d1(void); INLINE void prefix##_d2(void); INLINE void prefix##_d3(void); \
    /*TODO*///	INLINE void prefix##_d4(void); INLINE void prefix##_d5(void); INLINE void prefix##_d6(void); INLINE void prefix##_d7(void); \
    /*TODO*///	INLINE void prefix##_d8(void); INLINE void prefix##_d9(void); INLINE void prefix##_da(void); INLINE void prefix##_db(void); \
    /*TODO*///	INLINE void prefix##_dc(void); INLINE void prefix##_dd(void); INLINE void prefix##_de(void); INLINE void prefix##_df(void); \
    /*TODO*///	INLINE void prefix##_e0(void); INLINE void prefix##_e1(void); INLINE void prefix##_e2(void); INLINE void prefix##_e3(void); \
    /*TODO*///	INLINE void prefix##_e4(void); INLINE void prefix##_e5(void); INLINE void prefix##_e6(void); INLINE void prefix##_e7(void); \
    /*TODO*///	INLINE void prefix##_e8(void); INLINE void prefix##_e9(void); INLINE void prefix##_ea(void); INLINE void prefix##_eb(void); \
    /*TODO*///	INLINE void prefix##_ec(void); INLINE void prefix##_ed(void); INLINE void prefix##_ee(void); INLINE void prefix##_ef(void); \
    /*TODO*///	INLINE void prefix##_f0(void); INLINE void prefix##_f1(void); INLINE void prefix##_f2(void); INLINE void prefix##_f3(void); \
    /*TODO*///	INLINE void prefix##_f4(void); INLINE void prefix##_f5(void); INLINE void prefix##_f6(void); INLINE void prefix##_f7(void); \
    /*TODO*///	INLINE void prefix##_f8(void); INLINE void prefix##_f9(void); INLINE void prefix##_fa(void); INLINE void prefix##_fb(void); \
    /*TODO*///	INLINE void prefix##_fc(void); INLINE void prefix##_fd(void); INLINE void prefix##_fe(void); INLINE void prefix##_ff(void); \
    /*TODO*///static void (*tablename[0x100])(void) = {	\
    /*TODO*///    prefix##_00,prefix##_01,prefix##_02,prefix##_03,prefix##_04,prefix##_05,prefix##_06,prefix##_07, \
    /*TODO*///    prefix##_08,prefix##_09,prefix##_0a,prefix##_0b,prefix##_0c,prefix##_0d,prefix##_0e,prefix##_0f, \
    /*TODO*///    prefix##_10,prefix##_11,prefix##_12,prefix##_13,prefix##_14,prefix##_15,prefix##_16,prefix##_17, \
    /*TODO*///    prefix##_18,prefix##_19,prefix##_1a,prefix##_1b,prefix##_1c,prefix##_1d,prefix##_1e,prefix##_1f, \
    /*TODO*///    prefix##_20,prefix##_21,prefix##_22,prefix##_23,prefix##_24,prefix##_25,prefix##_26,prefix##_27, \
    /*TODO*///    prefix##_28,prefix##_29,prefix##_2a,prefix##_2b,prefix##_2c,prefix##_2d,prefix##_2e,prefix##_2f, \
    /*TODO*///    prefix##_30,prefix##_31,prefix##_32,prefix##_33,prefix##_34,prefix##_35,prefix##_36,prefix##_37, \
    /*TODO*///    prefix##_38,prefix##_39,prefix##_3a,prefix##_3b,prefix##_3c,prefix##_3d,prefix##_3e,prefix##_3f, \
    /*TODO*///    prefix##_40,prefix##_41,prefix##_42,prefix##_43,prefix##_44,prefix##_45,prefix##_46,prefix##_47, \
    /*TODO*///    prefix##_48,prefix##_49,prefix##_4a,prefix##_4b,prefix##_4c,prefix##_4d,prefix##_4e,prefix##_4f, \
    /*TODO*///    prefix##_50,prefix##_51,prefix##_52,prefix##_53,prefix##_54,prefix##_55,prefix##_56,prefix##_57, \
    /*TODO*///    prefix##_58,prefix##_59,prefix##_5a,prefix##_5b,prefix##_5c,prefix##_5d,prefix##_5e,prefix##_5f, \
    /*TODO*///    prefix##_60,prefix##_61,prefix##_62,prefix##_63,prefix##_64,prefix##_65,prefix##_66,prefix##_67, \
    /*TODO*///    prefix##_68,prefix##_69,prefix##_6a,prefix##_6b,prefix##_6c,prefix##_6d,prefix##_6e,prefix##_6f, \
    /*TODO*///    prefix##_70,prefix##_71,prefix##_72,prefix##_73,prefix##_74,prefix##_75,prefix##_76,prefix##_77, \
    /*TODO*///    prefix##_78,prefix##_79,prefix##_7a,prefix##_7b,prefix##_7c,prefix##_7d,prefix##_7e,prefix##_7f, \
    /*TODO*///    prefix##_80,prefix##_81,prefix##_82,prefix##_83,prefix##_84,prefix##_85,prefix##_86,prefix##_87, \
    /*TODO*///    prefix##_88,prefix##_89,prefix##_8a,prefix##_8b,prefix##_8c,prefix##_8d,prefix##_8e,prefix##_8f, \
    /*TODO*///    prefix##_90,prefix##_91,prefix##_92,prefix##_93,prefix##_94,prefix##_95,prefix##_96,prefix##_97, \
    /*TODO*///    prefix##_98,prefix##_99,prefix##_9a,prefix##_9b,prefix##_9c,prefix##_9d,prefix##_9e,prefix##_9f, \
    /*TODO*///    prefix##_a0,prefix##_a1,prefix##_a2,prefix##_a3,prefix##_a4,prefix##_a5,prefix##_a6,prefix##_a7, \
    /*TODO*///    prefix##_a8,prefix##_a9,prefix##_aa,prefix##_ab,prefix##_ac,prefix##_ad,prefix##_ae,prefix##_af, \
    /*TODO*///    prefix##_b0,prefix##_b1,prefix##_b2,prefix##_b3,prefix##_b4,prefix##_b5,prefix##_b6,prefix##_b7, \
    /*TODO*///    prefix##_b8,prefix##_b9,prefix##_ba,prefix##_bb,prefix##_bc,prefix##_bd,prefix##_be,prefix##_bf, \
    /*TODO*///    prefix##_c0,prefix##_c1,prefix##_c2,prefix##_c3,prefix##_c4,prefix##_c5,prefix##_c6,prefix##_c7, \
    /*TODO*///    prefix##_c8,prefix##_c9,prefix##_ca,prefix##_cb,prefix##_cc,prefix##_cd,prefix##_ce,prefix##_cf, \
    /*TODO*///    prefix##_d0,prefix##_d1,prefix##_d2,prefix##_d3,prefix##_d4,prefix##_d5,prefix##_d6,prefix##_d7, \
    /*TODO*///    prefix##_d8,prefix##_d9,prefix##_da,prefix##_db,prefix##_dc,prefix##_dd,prefix##_de,prefix##_df, \
    /*TODO*///    prefix##_e0,prefix##_e1,prefix##_e2,prefix##_e3,prefix##_e4,prefix##_e5,prefix##_e6,prefix##_e7, \
    /*TODO*///    prefix##_e8,prefix##_e9,prefix##_ea,prefix##_eb,prefix##_ec,prefix##_ed,prefix##_ee,prefix##_ef, \
    /*TODO*///    prefix##_f0,prefix##_f1,prefix##_f2,prefix##_f3,prefix##_f4,prefix##_f5,prefix##_f6,prefix##_f7, \
    /*TODO*///	prefix##_f8,prefix##_f9,prefix##_fa,prefix##_fb,prefix##_fc,prefix##_fd,prefix##_fe,prefix##_ff  \
    /*TODO*///}
    /*TODO*///
    /*TODO*///PROTOTYPES(Z80op,op);
    /*TODO*///PROTOTYPES(Z80cb,cb);
    /*TODO*///PROTOTYPES(Z80dd,dd);
    /*TODO*///PROTOTYPES(Z80ed,ed);
    /*TODO*///PROTOTYPES(Z80fd,fd);
    /*TODO*///PROTOTYPES(Z80xxcb,xxcb);
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Burn an odd amount of cycles, that is instructions taking something      */
    /*TODO*////* different from 4 T-states per opcode (and R increment)                   */
    /*TODO*////****************************************************************************/
    /*TODO*///INLINE void BURNODD(int cycles, int opcodes, int cyclesum)
    /*TODO*///{
    /*TODO*///    if( cycles > 0 )
    /*TODO*///    {
    /*TODO*///		_R += (cycles / cyclesum) * opcodes;
    /*TODO*///		z80_ICount -= (cycles / cyclesum) * cyclesum;
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * define an opcode function
    /*TODO*/// ***************************************************************/
    /*TODO*///#define OP(prefix,opcode)  INLINE void prefix##_##opcode(void)
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * adjust cycle count by n T-states
    /*TODO*/// ***************************************************************/
    /*TODO*///#define CY(cycles) z80_ICount -= cycles
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * execute an opcode
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EXEC(prefix,opcode) 									\
    /*TODO*///{																\
    /*TODO*///	unsigned op = opcode;										\
    /*TODO*///	CY(cc_##prefix[op]);										\
    /*TODO*///	(*Z80##prefix[op])();										\
    /*TODO*///}
    /*TODO*///
    /*TODO*///#if BIG_SWITCH
    /*TODO*///#define EXEC_INLINE(prefix,opcode)								\
    /*TODO*///{																\
    /*TODO*///	unsigned op = opcode;										\
    /*TODO*///	CY(cc_##prefix[op]);										\
    /*TODO*///	switch(op)													\
    /*TODO*///	{															\
    /*TODO*///	case 0x00:prefix##_##00();break; case 0x01:prefix##_##01();break; case 0x02:prefix##_##02();break; case 0x03:prefix##_##03();break; \
    /*TODO*///	case 0x04:prefix##_##04();break; case 0x05:prefix##_##05();break; case 0x06:prefix##_##06();break; case 0x07:prefix##_##07();break; \
    /*TODO*///	case 0x08:prefix##_##08();break; case 0x09:prefix##_##09();break; case 0x0a:prefix##_##0a();break; case 0x0b:prefix##_##0b();break; \
    /*TODO*///	case 0x0c:prefix##_##0c();break; case 0x0d:prefix##_##0d();break; case 0x0e:prefix##_##0e();break; case 0x0f:prefix##_##0f();break; \
    /*TODO*///	case 0x10:prefix##_##10();break; case 0x11:prefix##_##11();break; case 0x12:prefix##_##12();break; case 0x13:prefix##_##13();break; \
    /*TODO*///	case 0x14:prefix##_##14();break; case 0x15:prefix##_##15();break; case 0x16:prefix##_##16();break; case 0x17:prefix##_##17();break; \
    /*TODO*///	case 0x18:prefix##_##18();break; case 0x19:prefix##_##19();break; case 0x1a:prefix##_##1a();break; case 0x1b:prefix##_##1b();break; \
    /*TODO*///	case 0x1c:prefix##_##1c();break; case 0x1d:prefix##_##1d();break; case 0x1e:prefix##_##1e();break; case 0x1f:prefix##_##1f();break; \
    /*TODO*///	case 0x20:prefix##_##20();break; case 0x21:prefix##_##21();break; case 0x22:prefix##_##22();break; case 0x23:prefix##_##23();break; \
    /*TODO*///	case 0x24:prefix##_##24();break; case 0x25:prefix##_##25();break; case 0x26:prefix##_##26();break; case 0x27:prefix##_##27();break; \
    /*TODO*///	case 0x28:prefix##_##28();break; case 0x29:prefix##_##29();break; case 0x2a:prefix##_##2a();break; case 0x2b:prefix##_##2b();break; \
    /*TODO*///	case 0x2c:prefix##_##2c();break; case 0x2d:prefix##_##2d();break; case 0x2e:prefix##_##2e();break; case 0x2f:prefix##_##2f();break; \
    /*TODO*///	case 0x30:prefix##_##30();break; case 0x31:prefix##_##31();break; case 0x32:prefix##_##32();break; case 0x33:prefix##_##33();break; \
    /*TODO*///	case 0x34:prefix##_##34();break; case 0x35:prefix##_##35();break; case 0x36:prefix##_##36();break; case 0x37:prefix##_##37();break; \
    /*TODO*///	case 0x38:prefix##_##38();break; case 0x39:prefix##_##39();break; case 0x3a:prefix##_##3a();break; case 0x3b:prefix##_##3b();break; \
    /*TODO*///	case 0x3c:prefix##_##3c();break; case 0x3d:prefix##_##3d();break; case 0x3e:prefix##_##3e();break; case 0x3f:prefix##_##3f();break; \
    /*TODO*///	case 0x40:prefix##_##40();break; case 0x41:prefix##_##41();break; case 0x42:prefix##_##42();break; case 0x43:prefix##_##43();break; \
    /*TODO*///	case 0x44:prefix##_##44();break; case 0x45:prefix##_##45();break; case 0x46:prefix##_##46();break; case 0x47:prefix##_##47();break; \
    /*TODO*///	case 0x48:prefix##_##48();break; case 0x49:prefix##_##49();break; case 0x4a:prefix##_##4a();break; case 0x4b:prefix##_##4b();break; \
    /*TODO*///	case 0x4c:prefix##_##4c();break; case 0x4d:prefix##_##4d();break; case 0x4e:prefix##_##4e();break; case 0x4f:prefix##_##4f();break; \
    /*TODO*///	case 0x50:prefix##_##50();break; case 0x51:prefix##_##51();break; case 0x52:prefix##_##52();break; case 0x53:prefix##_##53();break; \
    /*TODO*///	case 0x54:prefix##_##54();break; case 0x55:prefix##_##55();break; case 0x56:prefix##_##56();break; case 0x57:prefix##_##57();break; \
    /*TODO*///	case 0x58:prefix##_##58();break; case 0x59:prefix##_##59();break; case 0x5a:prefix##_##5a();break; case 0x5b:prefix##_##5b();break; \
    /*TODO*///	case 0x5c:prefix##_##5c();break; case 0x5d:prefix##_##5d();break; case 0x5e:prefix##_##5e();break; case 0x5f:prefix##_##5f();break; \
    /*TODO*///	case 0x60:prefix##_##60();break; case 0x61:prefix##_##61();break; case 0x62:prefix##_##62();break; case 0x63:prefix##_##63();break; \
    /*TODO*///	case 0x64:prefix##_##64();break; case 0x65:prefix##_##65();break; case 0x66:prefix##_##66();break; case 0x67:prefix##_##67();break; \
    /*TODO*///	case 0x68:prefix##_##68();break; case 0x69:prefix##_##69();break; case 0x6a:prefix##_##6a();break; case 0x6b:prefix##_##6b();break; \
    /*TODO*///	case 0x6c:prefix##_##6c();break; case 0x6d:prefix##_##6d();break; case 0x6e:prefix##_##6e();break; case 0x6f:prefix##_##6f();break; \
    /*TODO*///	case 0x70:prefix##_##70();break; case 0x71:prefix##_##71();break; case 0x72:prefix##_##72();break; case 0x73:prefix##_##73();break; \
    /*TODO*///	case 0x74:prefix##_##74();break; case 0x75:prefix##_##75();break; case 0x76:prefix##_##76();break; case 0x77:prefix##_##77();break; \
    /*TODO*///	case 0x78:prefix##_##78();break; case 0x79:prefix##_##79();break; case 0x7a:prefix##_##7a();break; case 0x7b:prefix##_##7b();break; \
    /*TODO*///	case 0x7c:prefix##_##7c();break; case 0x7d:prefix##_##7d();break; case 0x7e:prefix##_##7e();break; case 0x7f:prefix##_##7f();break; \
    /*TODO*///	case 0x80:prefix##_##80();break; case 0x81:prefix##_##81();break; case 0x82:prefix##_##82();break; case 0x83:prefix##_##83();break; \
    /*TODO*///	case 0x84:prefix##_##84();break; case 0x85:prefix##_##85();break; case 0x86:prefix##_##86();break; case 0x87:prefix##_##87();break; \
    /*TODO*///	case 0x88:prefix##_##88();break; case 0x89:prefix##_##89();break; case 0x8a:prefix##_##8a();break; case 0x8b:prefix##_##8b();break; \
    /*TODO*///	case 0x8c:prefix##_##8c();break; case 0x8d:prefix##_##8d();break; case 0x8e:prefix##_##8e();break; case 0x8f:prefix##_##8f();break; \
    /*TODO*///	case 0x90:prefix##_##90();break; case 0x91:prefix##_##91();break; case 0x92:prefix##_##92();break; case 0x93:prefix##_##93();break; \
    /*TODO*///	case 0x94:prefix##_##94();break; case 0x95:prefix##_##95();break; case 0x96:prefix##_##96();break; case 0x97:prefix##_##97();break; \
    /*TODO*///	case 0x98:prefix##_##98();break; case 0x99:prefix##_##99();break; case 0x9a:prefix##_##9a();break; case 0x9b:prefix##_##9b();break; \
    /*TODO*///	case 0x9c:prefix##_##9c();break; case 0x9d:prefix##_##9d();break; case 0x9e:prefix##_##9e();break; case 0x9f:prefix##_##9f();break; \
    /*TODO*///	case 0xa0:prefix##_##a0();break; case 0xa1:prefix##_##a1();break; case 0xa2:prefix##_##a2();break; case 0xa3:prefix##_##a3();break; \
    /*TODO*///	case 0xa4:prefix##_##a4();break; case 0xa5:prefix##_##a5();break; case 0xa6:prefix##_##a6();break; case 0xa7:prefix##_##a7();break; \
    /*TODO*///	case 0xa8:prefix##_##a8();break; case 0xa9:prefix##_##a9();break; case 0xaa:prefix##_##aa();break; case 0xab:prefix##_##ab();break; \
    /*TODO*///	case 0xac:prefix##_##ac();break; case 0xad:prefix##_##ad();break; case 0xae:prefix##_##ae();break; case 0xaf:prefix##_##af();break; \
    /*TODO*///	case 0xb0:prefix##_##b0();break; case 0xb1:prefix##_##b1();break; case 0xb2:prefix##_##b2();break; case 0xb3:prefix##_##b3();break; \
    /*TODO*///	case 0xb4:prefix##_##b4();break; case 0xb5:prefix##_##b5();break; case 0xb6:prefix##_##b6();break; case 0xb7:prefix##_##b7();break; \
    /*TODO*///	case 0xb8:prefix##_##b8();break; case 0xb9:prefix##_##b9();break; case 0xba:prefix##_##ba();break; case 0xbb:prefix##_##bb();break; \
    /*TODO*///	case 0xbc:prefix##_##bc();break; case 0xbd:prefix##_##bd();break; case 0xbe:prefix##_##be();break; case 0xbf:prefix##_##bf();break; \
    /*TODO*///	case 0xc0:prefix##_##c0();break; case 0xc1:prefix##_##c1();break; case 0xc2:prefix##_##c2();break; case 0xc3:prefix##_##c3();break; \
    /*TODO*///	case 0xc4:prefix##_##c4();break; case 0xc5:prefix##_##c5();break; case 0xc6:prefix##_##c6();break; case 0xc7:prefix##_##c7();break; \
    /*TODO*///	case 0xc8:prefix##_##c8();break; case 0xc9:prefix##_##c9();break; case 0xca:prefix##_##ca();break; case 0xcb:prefix##_##cb();break; \
    /*TODO*///	case 0xcc:prefix##_##cc();break; case 0xcd:prefix##_##cd();break; case 0xce:prefix##_##ce();break; case 0xcf:prefix##_##cf();break; \
    /*TODO*///	case 0xd0:prefix##_##d0();break; case 0xd1:prefix##_##d1();break; case 0xd2:prefix##_##d2();break; case 0xd3:prefix##_##d3();break; \
    /*TODO*///	case 0xd4:prefix##_##d4();break; case 0xd5:prefix##_##d5();break; case 0xd6:prefix##_##d6();break; case 0xd7:prefix##_##d7();break; \
    /*TODO*///	case 0xd8:prefix##_##d8();break; case 0xd9:prefix##_##d9();break; case 0xda:prefix##_##da();break; case 0xdb:prefix##_##db();break; \
    /*TODO*///	case 0xdc:prefix##_##dc();break; case 0xdd:prefix##_##dd();break; case 0xde:prefix##_##de();break; case 0xdf:prefix##_##df();break; \
    /*TODO*///	case 0xe0:prefix##_##e0();break; case 0xe1:prefix##_##e1();break; case 0xe2:prefix##_##e2();break; case 0xe3:prefix##_##e3();break; \
    /*TODO*///	case 0xe4:prefix##_##e4();break; case 0xe5:prefix##_##e5();break; case 0xe6:prefix##_##e6();break; case 0xe7:prefix##_##e7();break; \
    /*TODO*///	case 0xe8:prefix##_##e8();break; case 0xe9:prefix##_##e9();break; case 0xea:prefix##_##ea();break; case 0xeb:prefix##_##eb();break; \
    /*TODO*///	case 0xec:prefix##_##ec();break; case 0xed:prefix##_##ed();break; case 0xee:prefix##_##ee();break; case 0xef:prefix##_##ef();break; \
    /*TODO*///	case 0xf0:prefix##_##f0();break; case 0xf1:prefix##_##f1();break; case 0xf2:prefix##_##f2();break; case 0xf3:prefix##_##f3();break; \
    /*TODO*///	case 0xf4:prefix##_##f4();break; case 0xf5:prefix##_##f5();break; case 0xf6:prefix##_##f6();break; case 0xf7:prefix##_##f7();break; \
    /*TODO*///	case 0xf8:prefix##_##f8();break; case 0xf9:prefix##_##f9();break; case 0xfa:prefix##_##fa();break; case 0xfb:prefix##_##fb();break; \
    /*TODO*///	case 0xfc:prefix##_##fc();break; case 0xfd:prefix##_##fd();break; case 0xfe:prefix##_##fe();break; case 0xff:prefix##_##ff();break; \
    /*TODO*///	}																																	\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define EXEC_INLINE EXEC
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Enter HALT state; write 1 to fake port on first execution
    /*TODO*/// ***************************************************************/
    /*TODO*///#define ENTER_HALT {											\
    /*TODO*///    _PC--;                                                      \
    /*TODO*///    _HALT = 1;                                                  \
    /*TODO*///	if( !after_EI ) 											\
    /*TODO*///		z80_burn( z80_ICount ); 								\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Leave HALT state; write 0 to fake port
    /*TODO*/// ***************************************************************/
    /*TODO*///#define LEAVE_HALT {                                            \
    /*TODO*///	if( _HALT ) 												\
    /*TODO*///	{															\
    /*TODO*///		_HALT = 0;												\
    /*TODO*///		_PC++;													\
    /*TODO*///	}															\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Input a byte from given I/O port
    /*TODO*/// ***************************************************************/
    /*TODO*///#define IN(port)   ((UINT8)cpu_readport(port))
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Output a byte to given I/O port
    /*TODO*/// ***************************************************************/
    /*TODO*///#define OUT(port,value) cpu_writeport(port,value)
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Read a byte from given memory location
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RM(addr) (UINT8)cpu_readmem16(addr)
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Read a word from given memory location
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE void RM16( UINT32 addr, PAIR *r )
    /*TODO*///{
    /*TODO*///	r->b.l = RM(addr);
    /*TODO*///	r->b.h = RM((addr+1)&0xffff);
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Write a byte to given memory location
    /*TODO*/// ***************************************************************/
    /*TODO*///#define WM(addr,value) cpu_writemem16(addr,value)
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Write a word to given memory location
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE void WM16( UINT32 addr, PAIR *r )
    /*TODO*///{
    /*TODO*///	WM(addr,r->b.l);
    /*TODO*///	WM((addr+1)&0xffff,r->b.h);
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * ROP() is identical to RM() except it is used for
    /*TODO*/// * reading opcodes. In case of system with memory mapped I/O,
    /*TODO*/// * this function can be used to greatly speed up emulation
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 ROP(void)
    /*TODO*///{
    /*TODO*///	unsigned pc = _PCD;
    /*TODO*///	_PC++;
    /*TODO*///	return cpu_readop(pc);
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************
    /*TODO*/// * ARG() is identical to ROP() except it is used
    /*TODO*/// * for reading opcode arguments. This difference can be used to
    /*TODO*/// * support systems that use different encoding mechanisms for
    /*TODO*/// * opcodes and opcode arguments
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 ARG(void)
    /*TODO*///{
    /*TODO*///	unsigned pc = _PCD;
    /*TODO*///    _PC++;
    /*TODO*///	return cpu_readop_arg(pc);
    /*TODO*///}
    /*TODO*///
    /*TODO*///INLINE UINT32 ARG16(void)
    /*TODO*///{
    /*TODO*///	unsigned pc = _PCD;
    /*TODO*///    _PC += 2;
    /*TODO*///	return cpu_readop_arg(pc) | (cpu_readop_arg((pc+1)&0xffff) << 8);
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * Calculate the effective address EA of an opcode using
    /*TODO*/// * IX+offset resp. IY+offset addressing.
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EAX EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
    /*TODO*///#define EAY EA = (UINT32)(UINT16)(_IY+(INT8)ARG())
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * POP
    /*TODO*/// ***************************************************************/
    /*TODO*///#define POP(DR) { RM16( _SPD, &Z80.DR ); _SP += 2; }
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * PUSH
    /*TODO*/// ***************************************************************/
    /*TODO*///#define PUSH(SR) { _SP -= 2; WM16( _SPD, &Z80.SR ); }
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * JP
    /*TODO*/// ***************************************************************/
    /*TODO*///#if BUSY_LOOP_HACKS
    /*TODO*///#define JP {													\
    /*TODO*///	unsigned oldpc = _PCD-1;									\
    /*TODO*///	_PCD = ARG16(); 											\
    /*TODO*///	change_pc16(_PCD);											\
    /*TODO*///    /* speed up busy loop */                                    \
    /*TODO*///	if( _PCD == oldpc ) 										\
    /*TODO*///	{															\
    /*TODO*///		if( !after_EI ) 										\
    /*TODO*///			BURNODD( z80_ICount, 1, 10 );						\
    /*TODO*///	}															\
    /*TODO*///	else														\
    /*TODO*///	{															\
    /*TODO*///		UINT8 op = cpu_readop(_PCD);							\
    /*TODO*///		if( _PCD == oldpc-1 )									\
    /*TODO*///		{														\
    /*TODO*///			/* NOP - JP $-1 or EI - JP $-1 */					\
    /*TODO*///			if ( op == 0x00 || op == 0xfb ) 					\
    /*TODO*///			{													\
    /*TODO*///				if( !after_EI ) 								\
    /*TODO*///					BURNODD( z80_ICount-4, 2, 4+10 );			\
    /*TODO*///			}													\
    /*TODO*///		}														\
    /*TODO*///		else													\
    /*TODO*///		/* LD SP,#xxxx - JP $-3 (Galaga) */ 					\
    /*TODO*///		if( _PCD == oldpc-3 && op == 0x31 ) 					\
    /*TODO*///		{														\
    /*TODO*///			if( !after_EI ) 									\
    /*TODO*///				BURNODD( z80_ICount-10, 2, 10+10 ); 			\
    /*TODO*///		}														\
    /*TODO*///	}															\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define JP {													\
    /*TODO*///	_PCD = ARG16(); 											\
    /*TODO*///	change_pc16(_PCD);											\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * JP_COND
    /*TODO*/// ***************************************************************/
    /*TODO*///
    /*TODO*///#define JP_COND(cond)											\
    /*TODO*///	if( cond )													\
    /*TODO*///	{															\
    /*TODO*///		_PCD = ARG16(); 										\
    /*TODO*///		change_pc16(_PCD);										\
    /*TODO*///	}															\
    /*TODO*///	else														\
    /*TODO*///	{															\
    /*TODO*///		_PC += 2;												\
    /*TODO*///    }
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * JR
    /*TODO*/// ***************************************************************/
    /*TODO*///#define JR()													\
    /*TODO*///{																\
    /*TODO*///	unsigned oldpc = _PCD-1;									\
    /*TODO*///	INT8 arg = (INT8)ARG(); /* ARG() also increments _PC */ 	\
    /*TODO*///	_PC += arg; 			/* so don't do _PC += ARG() */      \
    /*TODO*///	change_pc16(_PCD);											\
    /*TODO*///    /* speed up busy loop */                                    \
    /*TODO*///	if( _PCD == oldpc ) 										\
    /*TODO*///	{															\
    /*TODO*///		if( !after_EI ) 										\
    /*TODO*///			BURNODD( z80_ICount, 1, 12 );						\
    /*TODO*///	}															\
    /*TODO*///	else														\
    /*TODO*///	{															\
    /*TODO*///		UINT8 op = cpu_readop(_PCD);							\
    /*TODO*///		if( _PCD == oldpc-1 )									\
    /*TODO*///		{														\
    /*TODO*///			/* NOP - JR $-1 or EI - JR $-1 */					\
    /*TODO*///			if ( op == 0x00 || op == 0xfb ) 					\
    /*TODO*///			{													\
    /*TODO*///				if( !after_EI ) 								\
    /*TODO*///					BURNODD( z80_ICount-4, 2, 4+12 );			\
    /*TODO*///			}													\
    /*TODO*///		}														\
    /*TODO*///		else													\
    /*TODO*///		/* LD SP,#xxxx - JR $-3 */								\
    /*TODO*///		if( _PCD == oldpc-3 && op == 0x31 ) 					\
    /*TODO*///		{														\
    /*TODO*///			if( !after_EI ) 									\
    /*TODO*///				BURNODD( z80_ICount-12, 2, 10+12 ); 			\
    /*TODO*///		}														\
    /*TODO*///    }                                                           \
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * JR_COND
    /*TODO*/// ***************************************************************/
    /*TODO*///#define JR_COND(cond)											\
    /*TODO*///	if( cond )													\
    /*TODO*///	{															\
    /*TODO*///		INT8 arg = (INT8)ARG(); /* ARG() also increments _PC */ \
    /*TODO*///		_PC += arg; 			/* so don't do _PC += ARG() */  \
    /*TODO*///        CY(5);                                                  \
    /*TODO*///		change_pc16(_PCD);										\
    /*TODO*///	}															\
    /*TODO*///	else _PC++; 												\
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CALL
    /*TODO*/// ***************************************************************/
    /*TODO*///#define CALL(cond)												\
    /*TODO*///	if( cond )													\
    /*TODO*///	{															\
    /*TODO*///		EA = ARG16();											\
    /*TODO*///		PUSH( PC ); 											\
    /*TODO*///		_PCD = EA;												\
    /*TODO*///        CY(7);                                                  \
    /*TODO*///		change_pc16(_PCD);										\
    /*TODO*///	}															\
    /*TODO*///	else														\
    /*TODO*///	{															\
    /*TODO*///		_PC+=2; 												\
    /*TODO*///	}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RET
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RET(cond)												\
    /*TODO*///	if( cond )													\
    /*TODO*///	{															\
    /*TODO*///		POP(PC);												\
    /*TODO*///		change_pc16(_PCD);										\
    /*TODO*///		CY(6);													\
    /*TODO*///	}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RETN
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RETN	{												\
    /*TODO*///	LOG((errorlog,"Z80#%d RETN IFF1:%d IFF2:%d\n", cpu_getactivecpu(), _IFF1, _IFF2)); \
    /*TODO*///    RET(1);                                                     \
    /*TODO*///	if( _IFF1 == 0 && _IFF2 == 1 )								\
    /*TODO*///	{															\
    /*TODO*///		_IFF1 = 1;												\
    /*TODO*///		if( Z80.irq_state != CLEAR_LINE ||						\
    /*TODO*///			Z80.request_irq >= 0 )								\
    /*TODO*///		{														\
    /*TODO*///			LOG((errorlog, "Z80#%d RETN takes IRQ\n",           \
    /*TODO*///				cpu_getactivecpu()));							\
    /*TODO*///			take_interrupt();									\
    /*TODO*///        }                                                       \
    /*TODO*///	}															\
    /*TODO*///	else _IFF1 = _IFF2; 										\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RETI
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RETI	{												\
    /*TODO*///	int device = Z80.service_irq;								\
    /*TODO*///    RET(1);                                                     \
    /*TODO*////* according to http://www.msxnet.org/tech/Z80/z80undoc.txt */	\
    /*TODO*////*	_IFF1 = _IFF2;	*/											\
    /*TODO*///	if( device >= 0 )											\
    /*TODO*///	{															\
    /*TODO*///		LOG((errorlog,"Z80#%d RETI device %d: $%02x\n",         \
    /*TODO*///			cpu_getactivecpu(), device, Z80.irq[device].irq_param)); \
    /*TODO*///		Z80.irq[device].interrupt_reti(Z80.irq[device].irq_param); \
    /*TODO*///	}															\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LD	R,A
    /*TODO*/// ***************************************************************/
    /*TODO*///#define LD_R_A {												\
    /*TODO*///	_R = _A;													\
    /*TODO*///	_R2 = _A & 0x80;				/* keep bit 7 of R */		\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LD	A,R
    /*TODO*/// ***************************************************************/
    /*TODO*///#define LD_A_R {												\
    /*TODO*///	_A = (_R & 0x7f) | _R2; 									\
    /*TODO*///	_F = (_F & CF) | SZ[_A] | ( _IFF2 << 2 );					\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LD	I,A
    /*TODO*/// ***************************************************************/
    /*TODO*///#define LD_I_A {												\
    /*TODO*///	_I = _A;													\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LD	A,I
    /*TODO*/// ***************************************************************/
    /*TODO*///#define LD_A_I {												\
    /*TODO*///	_A = _I;													\
    /*TODO*///	_F = (_F & CF) | SZ[_A] | ( _IFF2 << 2 );					\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RST
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RST(addr)												\
    /*TODO*///	PUSH( PC ); 												\
    /*TODO*///	_PCD = addr;												\
    /*TODO*///	change_pc16(_PCD)
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * INC	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 INC(UINT8 value)
    /*TODO*///{
    /*TODO*///	UINT8 res = value + 1;
    /*TODO*///	_F = (_F & CF) | SZHV_inc[res];
    /*TODO*///	return (UINT8)res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * DEC	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 DEC(UINT8 value)
    /*TODO*///{
    /*TODO*///	UINT8 res = value - 1;
    /*TODO*///	_F = (_F & CF) | SZHV_dec[res];
    /*TODO*///    return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RLCA
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define RLCA													\
    /*TODO*///	_A = (_A << 1) | (_A >> 7); 								\
    /*TODO*///	_F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF))
    /*TODO*///#else
    /*TODO*///#define RLCA                                                    \
    /*TODO*///	_A = (_A << 1) | (_A >> 7); 								\
    /*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | (_A & CF)
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RRCA
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define RRCA                                                    \
    /*TODO*///	_F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF)); 		\
    /*TODO*///    _A = (_A >> 1) | (_A << 7)
    /*TODO*///#else
    /*TODO*///#define RRCA                                                    \
    /*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | (_A & CF);			\
    /*TODO*///	_A = (_A >> 1) | (_A << 7)
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RLA
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define RLA {													\
    /*TODO*///	UINT8 res = (_A << 1) | (_F & CF);							\
    /*TODO*///	UINT8 c = (_A & 0x80) ? CF : 0; 							\
    /*TODO*///	_F = (_F & (SF | ZF | PF)) | c | (res & (YF | XF)); 		\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define RLA {                                                   \
    /*TODO*///	UINT8 res = (_A << 1) | (_F & CF);							\
    /*TODO*///	UINT8 c = (_A & 0x80) ? CF : 0; 							\
    /*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | c;					\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RRA
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define RRA {                                                   \
    /*TODO*///	UINT8 res = (_A >> 1) | (_F << 7);							\
    /*TODO*///	UINT8 c = (_A & 0x01) ? CF : 0; 							\
    /*TODO*///	_F = (_F & (SF | ZF | PF)) | c | (res & (YF | XF)); 		\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define RRA {                                                   \
    /*TODO*///	UINT8 res = (_A >> 1) | (_F << 7);							\
    /*TODO*///	UINT8 c = (_A & 0x01) ? CF : 0; 							\
    /*TODO*///    _F = (_F & (SF | ZF | YF | XF | PF)) | c;                   \
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RRD
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RRD {													\
    /*TODO*///	UINT8 n = RM(_HL);											\
    /*TODO*///	WM( _HL, (n >> 4) | (_A << 4) );							\
    /*TODO*///	_A = (_A & 0xf0) | (n & 0x0f);								\
    /*TODO*///	_F = (_F & CF) | SZP[_A];									\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RLD
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RLD {                                                   \
    /*TODO*///    UINT8 n = RM(_HL);                                          \
    /*TODO*///	WM( _HL, (n << 4) | (_A & 0x0f) );							\
    /*TODO*///    _A = (_A & 0xf0) | (n >> 4);                                \
    /*TODO*///	_F = (_F & CF) | SZP[_A];									\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * ADD	A,n
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define ADD(value)												\
    /*TODO*/// asm (															\
    /*TODO*/// " addb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n"                                     \
    /*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define ADD(value)                                              \
    /*TODO*/// asm (															\
    /*TODO*/// " addb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n"                                     \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#define ADD(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00;									\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) + value);					\
    /*TODO*///	_F = SZHVC_add[ah | res];									\
    /*TODO*///    _A = res;                                                   \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define ADD(value)												\
    /*TODO*///{																\
    /*TODO*///	unsigned val = value;										\
    /*TODO*///    unsigned res = _A + val;                                    \
    /*TODO*///    _F = SZ[(UINT8)res] | ((res >> 8) & CF) |                   \
    /*TODO*///        ((_A ^ res ^ val) & HF) |                               \
    /*TODO*///        (((val ^ _A ^ 0x80) & (val ^ res) & 0x80) >> 5);        \
    /*TODO*///    _A = (UINT8)res;                                            \
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * ADC	A,n
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define ADC(value)												\
    /*TODO*/// asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " adcb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define ADC(value)                                              \
    /*TODO*/// asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " adcb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#define ADC(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00, c = _AFD & 1;					\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) + value + c);				\
    /*TODO*///	_F = SZHVC_add[(c << 16) | ah | res];						\
    /*TODO*///    _A = res;                                                   \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define ADC(value)												\
    /*TODO*///{																\
    /*TODO*///	unsigned val = value;										\
    /*TODO*///	unsigned res = _A + val + (_F & CF);						\
    /*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) |					\
    /*TODO*///		((_A ^ res ^ val) & HF) |								\
    /*TODO*///		(((val ^ _A ^ 0x80) & (val ^ res) & 0x80) >> 5);		\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SUB	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define SUB(value)												\
    /*TODO*/// asm (															\
    /*TODO*/// " subb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define SUB(value)                                              \
    /*TODO*/// asm (															\
    /*TODO*/// " subb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#define SUB(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00;									\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) - value);					\
    /*TODO*///	_F = SZHVC_sub[ah | res];									\
    /*TODO*///    _A = res;                                                   \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define SUB(value)												\
    /*TODO*///{																\
    /*TODO*///	unsigned val = value;										\
    /*TODO*///	unsigned res = _A - val;									\
    /*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) | NF |				\
    /*TODO*///		((_A ^ res ^ val) & HF) |								\
    /*TODO*///		(((val ^ _A) & (_A ^ res) & 0x80) >> 5);				\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SBC	A,n
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define SBC(value)												\
    /*TODO*/// asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " sbbb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define SBC(value)                                              \
    /*TODO*/// asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " sbbb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#define SBC(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00, c = _AFD & 1;					\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) - value - c);				\
    /*TODO*///	_F = SZHVC_sub[(c<<16) | ah | res]; 						\
    /*TODO*///    _A = res;                                                   \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define SBC(value)												\
    /*TODO*///{																\
    /*TODO*///	unsigned val = value;										\
    /*TODO*///	unsigned res = _A - val - (_F & CF);						\
    /*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) | NF |				\
    /*TODO*///		((_A ^ res ^ val) & HF) |								\
    /*TODO*///		(((val ^ _A) & (_A ^ res) & 0x80) >> 5);				\
    /*TODO*///	_A = res;													\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * NEG
    /*TODO*/// ***************************************************************/
    /*TODO*///#define NEG {                                                   \
    /*TODO*///	UINT8 value = _A;											\
    /*TODO*///	_A = 0; 													\
    /*TODO*///	SUB(value); 												\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * DAA
    /*TODO*/// ***************************************************************/
    /*TODO*///#define DAA {													\
    /*TODO*///	int idx = _A;												\
    /*TODO*///	if( _F & CF ) idx |= 0x100; 								\
    /*TODO*///	if( _F & HF ) idx |= 0x200; 								\
    /*TODO*///	if( _F & NF ) idx |= 0x400; 								\
    /*TODO*///	_AF = DAATable[idx];										\
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * AND	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#define AND(value)												\
    /*TODO*///	_A &= value;												\
    /*TODO*///	_F = SZP[_A] | HF
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * OR	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#define OR(value)												\
    /*TODO*///	_A |= value;												\
    /*TODO*///	_F = SZP[_A]
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * XOR	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#define XOR(value)												\
    /*TODO*///	_A ^= value;												\
    /*TODO*///	_F = SZP[_A]
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CP	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define CP(value)												\
    /*TODO*/// asm (															\
    /*TODO*/// " cmpb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// " movb %2,%%ah         \n" /* get result */                    \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define CP(value)                                               \
    /*TODO*/// asm (															\
    /*TODO*/// " cmpb %2,%0           \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
    /*TODO*/// " stc                  \n" /* prepare to set N flag */         \
    /*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
    /*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
    /*TODO*/// :"=r" (_A), "=r" (_F)                                          \
    /*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///#define CP(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00;									\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) - value);					\
    /*TODO*///	_F = SZHVC_sub[ah | res];									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define CP(value)												\
    /*TODO*///{																\
    /*TODO*///	unsigned val = value;										\
    /*TODO*///	unsigned res = _A - val;									\
    /*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) | NF |				\
    /*TODO*///		((_A ^ res ^ val) & HF) |								\
    /*TODO*///		((((val ^ _A) & (_A ^ res)) >> 5) & VF);				\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * EX   AF,AF'
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EX_AF {                                                 \
    /*TODO*///	PAIR tmp;													\
    /*TODO*///    tmp = Z80.AF; Z80.AF = Z80.AF2; Z80.AF2 = tmp;              \
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * EX   DE,HL
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EX_DE_HL {                                              \
    /*TODO*///	PAIR tmp;													\
    /*TODO*///    tmp = Z80.DE; Z80.DE = Z80.HL; Z80.HL = tmp;                \
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * EXX
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EXX {                                                   \
    /*TODO*///	PAIR tmp;													\
    /*TODO*///    tmp = Z80.BC; Z80.BC = Z80.BC2; Z80.BC2 = tmp;              \
    /*TODO*///    tmp = Z80.DE; Z80.DE = Z80.DE2; Z80.DE2 = tmp;              \
    /*TODO*///    tmp = Z80.HL; Z80.HL = Z80.HL2; Z80.HL2 = tmp;              \
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * EX   (SP),r16
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EXSP(DR)												\
    /*TODO*///{																\
    /*TODO*///	PAIR tmp = { { 0, 0, 0, 0 } };								\
    /*TODO*///	RM16( _SPD, &tmp ); 										\
    /*TODO*///	WM16( _SPD, &Z80.DR );										\
    /*TODO*///	Z80.DR = tmp;												\
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * ADD16
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef	X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define ADD16(DR,SR)											\
    /*TODO*/// asm (															\
    /*TODO*/// " andb $0xc4,%1        \n"                                     \
    /*TODO*/// " addb %%dl,%%cl       \n"                                     \
    /*TODO*/// " adcb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " andb $0x11,%%ah      \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n"                                     \
    /*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=c" (Z80.DR.d), "=r" (_F)                                    \
    /*TODO*/// :"0" (Z80.DR.d), "1" (_F), "d" (Z80.SR.d)                      \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define ADD16(DR,SR)                                            \
    /*TODO*/// asm (															\
    /*TODO*/// " andb $0xc4,%1        \n"                                     \
    /*TODO*/// " addb %%dl,%%cl       \n"                                     \
    /*TODO*/// " adcb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " andb $0x11,%%ah      \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n"                                     \
    /*TODO*/// :"=c" (Z80.DR.d), "=r" (_F)                                    \
    /*TODO*/// :"0" (Z80.DR.d), "1" (_F), "d" (Z80.SR.d)                      \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#define ADD16(DR,SR)											\
    /*TODO*///{																\
    /*TODO*///	UINT32 res = Z80.DR.d + Z80.SR.d;							\
    /*TODO*///	_F = (_F & (SF | ZF | VF)) |								\
    /*TODO*///		(((Z80.DR.d ^ res ^ Z80.SR.d) >> 8) & HF) | 			\
    /*TODO*///		((res >> 16) & CF); 									\
    /*TODO*///	Z80.DR.w.l = (UINT16)res;									\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * ADC	r16,r16
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef	X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define ADC16(Reg)												\
    /*TODO*/// asm (                                                          \
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " adcb %%dl,%%cl       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " movb %%ah,%%dl       \n"                                     \
    /*TODO*/// " adcb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n"                                     \
    /*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
    /*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
    /*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
    /*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define ADC16(Reg)                                              \
    /*TODO*/// asm (                                                          \
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " adcb %%dl,%%cl       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " movb %%ah,%%dl       \n"                                     \
    /*TODO*/// " adcb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n"                                     \
    /*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
    /*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
    /*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
    /*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#define ADC16(Reg)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 res = _HLD + Z80.Reg.d + (_F & CF);					\
    /*TODO*///	_F = (((_HLD ^ res ^ Z80.Reg.d) >> 8) & HF) |				\
    /*TODO*///		((res >> 16) & CF) |									\
    /*TODO*///		((res >> 8) & SF) | 									\
    /*TODO*///		((res & 0xffff) ? 0 : ZF) | 							\
    /*TODO*///		(((Z80.Reg.d ^ _HLD ^ 0x8000) & (Z80.Reg.d ^ res) & 0x8000) >> 13); \
    /*TODO*///	_HL = (UINT16)res;											\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SBC	r16,r16
    /*TODO*/// ***************************************************************/
    /*TODO*///#ifdef	X86_ASM
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define SBC16(Reg)												\
    /*TODO*///asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " sbbb %%dl,%%cl       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " movb %%ah,%%dl       \n"                                     \
    /*TODO*/// " sbbb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n"                                     \
    /*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
    /*TODO*/// " stc                  \n"                                     \
    /*TODO*/// " adcb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
    /*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
    /*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
    /*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
    /*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
    /*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
    /*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
    /*TODO*/// )
    /*TODO*///#else
    /*TODO*///#define SBC16(Reg)                                              \
    /*TODO*///asm (															\
    /*TODO*/// " shrb $1,%1           \n"                                     \
    /*TODO*/// " sbbb %%dl,%%cl       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " movb %%ah,%%dl       \n"                                     \
    /*TODO*/// " sbbb %%dh,%%ch       \n"                                     \
    /*TODO*/// " lahf                 \n"                                     \
    /*TODO*/// " setob %1             \n"                                     \
    /*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
    /*TODO*/// " stc                  \n"                                     \
    /*TODO*/// " adcb %1,%1           \n"                                     \
    /*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
    /*TODO*/// " addb %1,%1           \n"                                     \
    /*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
    /*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
    /*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
    /*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
    /*TODO*/// )
    /*TODO*///#endif
    /*TODO*///#else
    /*TODO*///#define SBC16(Reg)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 res = _HLD - Z80.Reg.d - (_F & CF);					\
    /*TODO*///	_F = (((_HLD ^ res ^ Z80.Reg.d) >> 8) & HF) | NF |			\
    /*TODO*///		((res >> 16) & CF) |									\
    /*TODO*///		((res >> 8) & SF) | 									\
    /*TODO*///		((res & 0xffff) ? 0 : ZF) | 							\
    /*TODO*///		(((Z80.Reg.d ^ _HLD) & (_HLD ^ res) &0x8000) >> 13);	\
    /*TODO*///	_HL = (UINT16)res;											\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RLC	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 RLC(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x80) ? CF : 0;
    /*TODO*///	res = ((res << 1) | (res >> 7)) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RRC	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 RRC(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x01) ? CF : 0;
    /*TODO*///	res = ((res >> 1) | (res << 7)) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RL	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 RL(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x80) ? CF : 0;
    /*TODO*///	res = ((res << 1) | (_F & CF)) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RR	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 RR(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x01) ? CF : 0;
    /*TODO*///	res = ((res >> 1) | (_F << 7)) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SLA	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 SLA(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x80) ? CF : 0;
    /*TODO*///	res = (res << 1) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SRA	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 SRA(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x01) ? CF : 0;
    /*TODO*///	res = ((res >> 1) | (res & 0x80)) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SLL	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 SLL(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x80) ? CF : 0;
    /*TODO*///	res = ((res << 1) | 0x01) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SRL	r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 SRL(UINT8 value)
    /*TODO*///{
    /*TODO*///	unsigned res = value;
    /*TODO*///	unsigned c = (res & 0x01) ? CF : 0;
    /*TODO*///	res = (res >> 1) & 0xff;
    /*TODO*///	_F = SZP[res] | c;
    /*TODO*///	return res;
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * BIT  bit,r8
    /*TODO*/// ***************************************************************/
    /*TODO*///#define BIT(bit,reg)                                            \
    /*TODO*///	_F = (_F & CF) | HF | SZ_BIT[reg & (1<<bit)]
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * BIT	bit,(IX/Y+o)
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define BIT_XY(bit,reg)                                         \
    /*TODO*///    _F = (_F & CF) | HF | (SZ_BIT[reg & (1<<bit)] & ~(YF|XF)) | ((EA>>8) & (YF|XF))
    /*TODO*///#else
    /*TODO*///#define BIT_XY	BIT
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * RES	bit,r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 RES(UINT8 bit, UINT8 value)
    /*TODO*///{
    /*TODO*///	return value & ~(1<<bit);
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * SET  bit,r8
    /*TODO*/// ***************************************************************/
    /*TODO*///INLINE UINT8 SET(UINT8 bit, UINT8 value)
    /*TODO*///{
    /*TODO*///	return value | (1<<bit);
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LDI
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define LDI {													\
    /*TODO*///	UINT8 io = RM(_HL); 										\
    /*TODO*///	WM( _DE, io );												\
    /*TODO*///	_F &= SF | ZF | CF; 										\
    /*TODO*///	if( (_A + io) & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */		\
    /*TODO*///    if( (_A + io) & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */      \
    /*TODO*///    _HL++; _DE++; _BC--;                                        \
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define LDI {                                                   \
    /*TODO*///	WM( _DE, RM(_HL) ); 										\
    /*TODO*///    _F &= SF | ZF | YF | XF | CF;                               \
    /*TODO*///	_HL++; _DE++; _BC--;										\
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CPI
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define CPI {													\
    /*TODO*///	UINT8 val = RM(_HL);										\
    /*TODO*///	UINT8 res = _A - val;										\
    /*TODO*///	_HL++; _BC--;												\
    /*TODO*///	_F = (_F & CF) | (SZ[res] & ~(YF|XF)) | ((_A ^ val ^ res) & HF) | NF;  \
    /*TODO*///	if( _F & HF ) res -= 1; 									\
    /*TODO*///	if( res & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */			\
    /*TODO*///	if( res & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */			\
    /*TODO*///    if( _BC ) _F |= VF;                                         \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define CPI {                                                   \
    /*TODO*///	UINT8 val = RM(_HL);										\
    /*TODO*///	UINT8 res = _A - val;										\
    /*TODO*///	_HL++; _BC--;												\
    /*TODO*///	_F = (_F & CF) | SZ[res] | ((_A ^ val ^ res) & HF) | NF;	\
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * INI
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define INI {													\
    /*TODO*///	UINT8 io = IN(_BC); 										\
    /*TODO*///	_B--;														\
    /*TODO*///	WM( _HL, io );												\
    /*TODO*///	_HL++;														\
    /*TODO*///	_F = SZ[_B];												\
    /*TODO*///	if( io & SF ) _F |= NF; 									\
    /*TODO*///	if( (_C + io + 1) & 0x100 ) _F |= HF | CF;					\
    /*TODO*///    if( (irep_tmp1[_C & 3][io & 3] ^                            \
    /*TODO*///		 breg_tmp2[_B] ^										\
    /*TODO*///		 (_C >> 2) ^											\
    /*TODO*///		 (io >> 2)) & 1 )										\
    /*TODO*///		_F |= PF;												\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define INI {													\
    /*TODO*///	_B--;														\
    /*TODO*///	WM( _HL, IN(_BC) ); 										\
    /*TODO*///	_HL++;														\
    /*TODO*///	_F = (_B) ? NF : NF | ZF;									\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * OUTI
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define OUTI {													\
    /*TODO*///	UINT8 io = RM(_HL); 										\
    /*TODO*///	OUT( _BC, io ); 											\
    /*TODO*///    _B--;                                                       \
    /*TODO*///	_HL++;														\
    /*TODO*///	_F = SZ[_B];												\
    /*TODO*///	if( io & SF ) _F |= NF; 									\
    /*TODO*///	if( (_C + io + 1) & 0x100 ) _F |= HF | CF;					\
    /*TODO*///    if( (irep_tmp1[_C & 3][io & 3] ^                            \
    /*TODO*///		 breg_tmp2[_B] ^										\
    /*TODO*///		 (_C >> 2) ^											\
    /*TODO*///		 (io >> 2)) & 1 )										\
    /*TODO*///        _F |= PF;                                               \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define OUTI {													\
    /*TODO*///    OUT( _BC, RM(_HL) );                                        \
    /*TODO*///	_B--;														\
    /*TODO*///    _HL++;                                                      \
    /*TODO*///    _F = (_B) ? NF : NF | ZF;                                   \
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LDD
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define LDD {													\
    /*TODO*///	UINT8 io = RM(_HL); 										\
    /*TODO*///	WM( _DE, io );												\
    /*TODO*///	_F &= SF | ZF | CF; 										\
    /*TODO*///	if( (_A + io) & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */		\
    /*TODO*///	if( (_A + io) & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */		\
    /*TODO*///	_HL--; _DE--; _BC--;										\
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define LDD {                                                   \
    /*TODO*///	WM( _DE, RM(_HL) ); 										\
    /*TODO*///    _F &= SF | ZF | YF | XF | CF;                               \
    /*TODO*///	_HL--; _DE--; _BC--;										\
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CPD
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define CPD {													\
    /*TODO*///	UINT8 val = RM(_HL);										\
    /*TODO*///	UINT8 res = _A - val;										\
    /*TODO*///	_HL--; _BC--;												\
    /*TODO*///	_F = (_F & CF) | (SZ[res] & ~(YF|XF)) | ((_A ^ val ^ res) & HF) | NF;  \
    /*TODO*///	if( _F & HF ) res -= 1; 									\
    /*TODO*///	if( res & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */			\
    /*TODO*///	if( res & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */			\
    /*TODO*///    if( _BC ) _F |= VF;                                         \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define CPD {                                                   \
    /*TODO*///	UINT8 val = RM(_HL);										\
    /*TODO*///	UINT8 res = _A - val;										\
    /*TODO*///	_HL--; _BC--;												\
    /*TODO*///	_F = (_F & CF) | SZ[res] | ((_A ^ val ^ res) & HF) | NF;	\
    /*TODO*///	if( _BC ) _F |= VF; 										\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * IND
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define IND {													\
    /*TODO*///    UINT8 io = IN(_BC);                                         \
    /*TODO*///	_B--;														\
    /*TODO*///	WM( _HL, io );												\
    /*TODO*///	_HL--;														\
    /*TODO*///	_F = SZ[_B];												\
    /*TODO*///    if( io & SF ) _F |= NF;                                     \
    /*TODO*///	if( (_C + io - 1) & 0x100 ) _F |= HF | CF;					\
    /*TODO*///	if( (drep_tmp1[_C & 3][io & 3] ^							\
    /*TODO*///		 breg_tmp2[_B] ^										\
    /*TODO*///		 (_C >> 2) ^											\
    /*TODO*///		 (io >> 2)) & 1 )										\
    /*TODO*///        _F |= PF;                                               \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define IND {                                                   \
    /*TODO*///	_B--;														\
    /*TODO*///	WM( _HL, IN(_BC) ); 										\
    /*TODO*///	_HL--;														\
    /*TODO*///	_F = (_B) ? NF : NF | ZF;									\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * OUTD
    /*TODO*/// ***************************************************************/
    /*TODO*///#if Z80_EXACT
    /*TODO*///#define OUTD {													\
    /*TODO*///	UINT8 io = RM(_HL); 										\
    /*TODO*///	OUT( _BC, io ); 											\
    /*TODO*///	_B--;														\
    /*TODO*///	_HL--;														\
    /*TODO*///	_F = SZ[_B];												\
    /*TODO*///    if( io & SF ) _F |= NF;                                     \
    /*TODO*///	if( (_C + io - 1) & 0x100 ) _F |= HF | CF;					\
    /*TODO*///	if( (drep_tmp1[_C & 3][io & 3] ^							\
    /*TODO*///		 breg_tmp2[_B] ^										\
    /*TODO*///		 (_C >> 2) ^											\
    /*TODO*///		 (io >> 2)) & 1 )										\
    /*TODO*///        _F |= PF;                                               \
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define OUTD {                                                  \
    /*TODO*///    OUT( _BC, RM(_HL) );                                        \
    /*TODO*///	_B--;														\
    /*TODO*///	_HL--;														\
    /*TODO*///	_F = (_B) ? NF : NF | ZF;									\
    /*TODO*///}
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LDIR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define LDIR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///	do															\
    /*TODO*///	{															\
    /*TODO*///		LDI;													\
    /*TODO*///		if( _BC )												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///			{													\
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///			else break; 										\
    /*TODO*///		}														\
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define LDIR LDI; if( _BC ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CPIR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define CPIR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		CPI;													\
    /*TODO*///		if( _BC && !(_F & ZF) ) 								\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define CPIR CPI; if( _BC && !(_F & ZF) ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * INIR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define INIR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		INI;													\
    /*TODO*///		if( _B )												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define INIR INI; if( _B ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * OTIR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define OTIR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		OUTI;													\
    /*TODO*///		if( _B	)												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define OTIR OUTI; if( _B ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * LDDR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define LDDR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		LDD;													\
    /*TODO*///		if( _BC )												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define LDDR LDD; if( _BC ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * CPDR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define CPDR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		CPD;													\
    /*TODO*///		if( _BC && !(_F & ZF) ) 								\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define CPDR CPD; if( _BC && !(_F & ZF) ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * INDR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define INDR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		IND;													\
    /*TODO*///		if( _B )												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define INDR IND; if( _B ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * OTDR
    /*TODO*/// ***************************************************************/
    /*TODO*///#if REPEAT_AT_ONCE
    /*TODO*///#define OTDR {                                                  \
    /*TODO*///	CY(5);														\
    /*TODO*///	_PC -= 2;													\
    /*TODO*///    do                                                          \
    /*TODO*///	{															\
    /*TODO*///		OUTD;													\
    /*TODO*///		if( _B )												\
    /*TODO*///		{														\
    /*TODO*///			if( z80_ICount > 0 )								\
    /*TODO*///            {                                                   \
    /*TODO*///				_R += 2;  /* increment R twice */				\
    /*TODO*///				CY(21); 										\
    /*TODO*///			}													\
    /*TODO*///            else break;                                         \
    /*TODO*///        }                                                       \
    /*TODO*///		else													\
    /*TODO*///		{														\
    /*TODO*///			_PC += 2;											\
    /*TODO*///            z80_ICount += 5;                                    \
    /*TODO*///            break;                                              \
    /*TODO*///		}														\
    /*TODO*///	} while( z80_ICount > 0 );									\
    /*TODO*///}
    /*TODO*///#else
    /*TODO*///#define OTDR OUTD; if( _B ) { _PC -= 2; CY(5); }
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////***************************************************************
    /*TODO*/// * EI
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EI {													\
    /*TODO*///	/* If interrupts were disabled, execute one more			\
    /*TODO*///     * instruction and check the IRQ line.                      \
    /*TODO*///     * If not, simply set interrupt flip-flop 2                 \
    /*TODO*///     */                                                         \
    /*TODO*///	if( _IFF1 == 0 )											\
    /*TODO*///	{															\
    /*TODO*///        _IFF1 = _IFF2 = 1;                                      \
    /*TODO*///        _PPC = _PCD;                                            \
    /*TODO*///        CALL_MAME_DEBUG;                                        \
    /*TODO*///		_R++;													\
    /*TODO*///		if( Z80.irq_state != CLEAR_LINE ||						\
    /*TODO*///			Z80.request_irq >= 0 )								\
    /*TODO*///		{														\
    /*TODO*///			after_EI = 1;	/* avoid cycle skip hacks */		\
    /*TODO*///			EXEC(op,ROP()); 									\
    /*TODO*///			after_EI = 0;										\
    /*TODO*///            LOG((errorlog, "Z80#%d EI takes irq\n", cpu_getactivecpu())); \
    /*TODO*///            take_interrupt();                                   \
    /*TODO*///        }                                                       \
    /*TODO*///		else EXEC(op,ROP()); 									\
    /*TODO*///    } else _IFF2 = 1;                                           \
    /*TODO*///}
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*/// * opcodes with CB prefix
    /*TODO*/// * rotate, shift and bit operations
    /*TODO*/// **********************************************************/
    /*TODO*///OP(cb,00) { _B = RLC(_B);											} /* RLC  B 		  */
    /*TODO*///OP(cb,01) { _C = RLC(_C);											} /* RLC  C 		  */
    /*TODO*///OP(cb,02) { _D = RLC(_D);											} /* RLC  D 		  */
    /*TODO*///OP(cb,03) { _E = RLC(_E);											} /* RLC  E 		  */
    /*TODO*///OP(cb,04) { _H = RLC(_H);											} /* RLC  H 		  */
    /*TODO*///OP(cb,05) { _L = RLC(_L);											} /* RLC  L 		  */
    /*TODO*///OP(cb,06) { WM( _HL, RLC(RM(_HL)) );								} /* RLC  (HL)		  */
    /*TODO*///OP(cb,07) { _A = RLC(_A);											} /* RLC  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,08) { _B = RRC(_B);											} /* RRC  B 		  */
    /*TODO*///OP(cb,09) { _C = RRC(_C);											} /* RRC  C 		  */
    /*TODO*///OP(cb,0a) { _D = RRC(_D);											} /* RRC  D 		  */
    /*TODO*///OP(cb,0b) { _E = RRC(_E);											} /* RRC  E 		  */
    /*TODO*///OP(cb,0c) { _H = RRC(_H);											} /* RRC  H 		  */
    /*TODO*///OP(cb,0d) { _L = RRC(_L);											} /* RRC  L 		  */
    /*TODO*///OP(cb,0e) { WM( _HL, RRC(RM(_HL)) );								} /* RRC  (HL)		  */
    /*TODO*///OP(cb,0f) { _A = RRC(_A);											} /* RRC  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,10) { _B = RL(_B);											} /* RL   B 		  */
    /*TODO*///OP(cb,11) { _C = RL(_C);											} /* RL   C 		  */
    /*TODO*///OP(cb,12) { _D = RL(_D);											} /* RL   D 		  */
    /*TODO*///OP(cb,13) { _E = RL(_E);											} /* RL   E 		  */
    /*TODO*///OP(cb,14) { _H = RL(_H);											} /* RL   H 		  */
    /*TODO*///OP(cb,15) { _L = RL(_L);											} /* RL   L 		  */
    /*TODO*///OP(cb,16) { WM( _HL, RL(RM(_HL)) ); 								} /* RL   (HL)		  */
    /*TODO*///OP(cb,17) { _A = RL(_A);											} /* RL   A 		  */
    /*TODO*///
    /*TODO*///OP(cb,18) { _B = RR(_B);											} /* RR   B 		  */
    /*TODO*///OP(cb,19) { _C = RR(_C);											} /* RR   C 		  */
    /*TODO*///OP(cb,1a) { _D = RR(_D);											} /* RR   D 		  */
    /*TODO*///OP(cb,1b) { _E = RR(_E);											} /* RR   E 		  */
    /*TODO*///OP(cb,1c) { _H = RR(_H);											} /* RR   H 		  */
    /*TODO*///OP(cb,1d) { _L = RR(_L);											} /* RR   L 		  */
    /*TODO*///OP(cb,1e) { WM( _HL, RR(RM(_HL)) ); 								} /* RR   (HL)		  */
    /*TODO*///OP(cb,1f) { _A = RR(_A);											} /* RR   A 		  */
    /*TODO*///
    /*TODO*///OP(cb,20) { _B = SLA(_B);											} /* SLA  B 		  */
    /*TODO*///OP(cb,21) { _C = SLA(_C);											} /* SLA  C 		  */
    /*TODO*///OP(cb,22) { _D = SLA(_D);											} /* SLA  D 		  */
    /*TODO*///OP(cb,23) { _E = SLA(_E);											} /* SLA  E 		  */
    /*TODO*///OP(cb,24) { _H = SLA(_H);											} /* SLA  H 		  */
    /*TODO*///OP(cb,25) { _L = SLA(_L);											} /* SLA  L 		  */
    /*TODO*///OP(cb,26) { WM( _HL, SLA(RM(_HL)) );								} /* SLA  (HL)		  */
    /*TODO*///OP(cb,27) { _A = SLA(_A);											} /* SLA  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,28) { _B = SRA(_B);											} /* SRA  B 		  */
    /*TODO*///OP(cb,29) { _C = SRA(_C);											} /* SRA  C 		  */
    /*TODO*///OP(cb,2a) { _D = SRA(_D);											} /* SRA  D 		  */
    /*TODO*///OP(cb,2b) { _E = SRA(_E);											} /* SRA  E 		  */
    /*TODO*///OP(cb,2c) { _H = SRA(_H);											} /* SRA  H 		  */
    /*TODO*///OP(cb,2d) { _L = SRA(_L);											} /* SRA  L 		  */
    /*TODO*///OP(cb,2e) { WM( _HL, SRA(RM(_HL)) );								} /* SRA  (HL)		  */
    /*TODO*///OP(cb,2f) { _A = SRA(_A);											} /* SRA  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,30) { _B = SLL(_B);											} /* SLL  B 		  */
    /*TODO*///OP(cb,31) { _C = SLL(_C);											} /* SLL  C 		  */
    /*TODO*///OP(cb,32) { _D = SLL(_D);											} /* SLL  D 		  */
    /*TODO*///OP(cb,33) { _E = SLL(_E);											} /* SLL  E 		  */
    /*TODO*///OP(cb,34) { _H = SLL(_H);											} /* SLL  H 		  */
    /*TODO*///OP(cb,35) { _L = SLL(_L);											} /* SLL  L 		  */
    /*TODO*///OP(cb,36) { WM( _HL, SLL(RM(_HL)) );								} /* SLL  (HL)		  */
    /*TODO*///OP(cb,37) { _A = SLL(_A);											} /* SLL  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,38) { _B = SRL(_B);											} /* SRL  B 		  */
    /*TODO*///OP(cb,39) { _C = SRL(_C);											} /* SRL  C 		  */
    /*TODO*///OP(cb,3a) { _D = SRL(_D);											} /* SRL  D 		  */
    /*TODO*///OP(cb,3b) { _E = SRL(_E);											} /* SRL  E 		  */
    /*TODO*///OP(cb,3c) { _H = SRL(_H);											} /* SRL  H 		  */
    /*TODO*///OP(cb,3d) { _L = SRL(_L);											} /* SRL  L 		  */
    /*TODO*///OP(cb,3e) { WM( _HL, SRL(RM(_HL)) );								} /* SRL  (HL)		  */
    /*TODO*///OP(cb,3f) { _A = SRL(_A);											} /* SRL  A 		  */
    /*TODO*///
    /*TODO*///OP(cb,40) { BIT(0,_B);												} /* BIT  0,B		  */
    /*TODO*///OP(cb,41) { BIT(0,_C);												} /* BIT  0,C		  */
    /*TODO*///OP(cb,42) { BIT(0,_D);												} /* BIT  0,D		  */
    /*TODO*///OP(cb,43) { BIT(0,_E);												} /* BIT  0,E		  */
    /*TODO*///OP(cb,44) { BIT(0,_H);												} /* BIT  0,H		  */
    /*TODO*///OP(cb,45) { BIT(0,_L);												} /* BIT  0,L		  */
    /*TODO*///OP(cb,46) { BIT(0,RM(_HL)); 										} /* BIT  0,(HL)	  */
    /*TODO*///OP(cb,47) { BIT(0,_A);												} /* BIT  0,A		  */
    /*TODO*///
    /*TODO*///OP(cb,48) { BIT(1,_B);												} /* BIT  1,B		  */
    /*TODO*///OP(cb,49) { BIT(1,_C);												} /* BIT  1,C		  */
    /*TODO*///OP(cb,4a) { BIT(1,_D);												} /* BIT  1,D		  */
    /*TODO*///OP(cb,4b) { BIT(1,_E);												} /* BIT  1,E		  */
    /*TODO*///OP(cb,4c) { BIT(1,_H);												} /* BIT  1,H		  */
    /*TODO*///OP(cb,4d) { BIT(1,_L);												} /* BIT  1,L		  */
    /*TODO*///OP(cb,4e) { BIT(1,RM(_HL)); 										} /* BIT  1,(HL)	  */
    /*TODO*///OP(cb,4f) { BIT(1,_A);												} /* BIT  1,A		  */
    /*TODO*///
    /*TODO*///OP(cb,50) { BIT(2,_B);												} /* BIT  2,B		  */
    /*TODO*///OP(cb,51) { BIT(2,_C);												} /* BIT  2,C		  */
    /*TODO*///OP(cb,52) { BIT(2,_D);												} /* BIT  2,D		  */
    /*TODO*///OP(cb,53) { BIT(2,_E);												} /* BIT  2,E		  */
    /*TODO*///OP(cb,54) { BIT(2,_H);												} /* BIT  2,H		  */
    /*TODO*///OP(cb,55) { BIT(2,_L);												} /* BIT  2,L		  */
    /*TODO*///OP(cb,56) { BIT(2,RM(_HL)); 										} /* BIT  2,(HL)	  */
    /*TODO*///OP(cb,57) { BIT(2,_A);												} /* BIT  2,A		  */
    /*TODO*///
    /*TODO*///OP(cb,58) { BIT(3,_B);												} /* BIT  3,B		  */
    /*TODO*///OP(cb,59) { BIT(3,_C);												} /* BIT  3,C		  */
    /*TODO*///OP(cb,5a) { BIT(3,_D);												} /* BIT  3,D		  */
    /*TODO*///OP(cb,5b) { BIT(3,_E);												} /* BIT  3,E		  */
    /*TODO*///OP(cb,5c) { BIT(3,_H);												} /* BIT  3,H		  */
    /*TODO*///OP(cb,5d) { BIT(3,_L);												} /* BIT  3,L		  */
    /*TODO*///OP(cb,5e) { BIT(3,RM(_HL)); 										} /* BIT  3,(HL)	  */
    /*TODO*///OP(cb,5f) { BIT(3,_A);												} /* BIT  3,A		  */
    /*TODO*///
    /*TODO*///OP(cb,60) { BIT(4,_B);												} /* BIT  4,B		  */
    /*TODO*///OP(cb,61) { BIT(4,_C);												} /* BIT  4,C		  */
    /*TODO*///OP(cb,62) { BIT(4,_D);												} /* BIT  4,D		  */
    /*TODO*///OP(cb,63) { BIT(4,_E);												} /* BIT  4,E		  */
    /*TODO*///OP(cb,64) { BIT(4,_H);												} /* BIT  4,H		  */
    /*TODO*///OP(cb,65) { BIT(4,_L);												} /* BIT  4,L		  */
    /*TODO*///OP(cb,66) { BIT(4,RM(_HL)); 										} /* BIT  4,(HL)	  */
    /*TODO*///OP(cb,67) { BIT(4,_A);												} /* BIT  4,A		  */
    /*TODO*///
    /*TODO*///OP(cb,68) { BIT(5,_B);												} /* BIT  5,B		  */
    /*TODO*///OP(cb,69) { BIT(5,_C);												} /* BIT  5,C		  */
    /*TODO*///OP(cb,6a) { BIT(5,_D);												} /* BIT  5,D		  */
    /*TODO*///OP(cb,6b) { BIT(5,_E);												} /* BIT  5,E		  */
    /*TODO*///OP(cb,6c) { BIT(5,_H);												} /* BIT  5,H		  */
    /*TODO*///OP(cb,6d) { BIT(5,_L);												} /* BIT  5,L		  */
    /*TODO*///OP(cb,6e) { BIT(5,RM(_HL)); 										} /* BIT  5,(HL)	  */
    /*TODO*///OP(cb,6f) { BIT(5,_A);												} /* BIT  5,A		  */
    /*TODO*///
    /*TODO*///OP(cb,70) { BIT(6,_B);												} /* BIT  6,B		  */
    /*TODO*///OP(cb,71) { BIT(6,_C);												} /* BIT  6,C		  */
    /*TODO*///OP(cb,72) { BIT(6,_D);												} /* BIT  6,D		  */
    /*TODO*///OP(cb,73) { BIT(6,_E);												} /* BIT  6,E		  */
    /*TODO*///OP(cb,74) { BIT(6,_H);												} /* BIT  6,H		  */
    /*TODO*///OP(cb,75) { BIT(6,_L);												} /* BIT  6,L		  */
    /*TODO*///OP(cb,76) { BIT(6,RM(_HL)); 										} /* BIT  6,(HL)	  */
    /*TODO*///OP(cb,77) { BIT(6,_A);												} /* BIT  6,A		  */
    /*TODO*///
    /*TODO*///OP(cb,78) { BIT(7,_B);												} /* BIT  7,B		  */
    /*TODO*///OP(cb,79) { BIT(7,_C);												} /* BIT  7,C		  */
    /*TODO*///OP(cb,7a) { BIT(7,_D);												} /* BIT  7,D		  */
    /*TODO*///OP(cb,7b) { BIT(7,_E);												} /* BIT  7,E		  */
    /*TODO*///OP(cb,7c) { BIT(7,_H);												} /* BIT  7,H		  */
    /*TODO*///OP(cb,7d) { BIT(7,_L);												} /* BIT  7,L		  */
    /*TODO*///OP(cb,7e) { BIT(7,RM(_HL)); 										} /* BIT  7,(HL)	  */
    /*TODO*///OP(cb,7f) { BIT(7,_A);												} /* BIT  7,A		  */
    /*TODO*///
    /*TODO*///OP(cb,80) { _B = RES(0,_B); 										} /* RES  0,B		  */
    /*TODO*///OP(cb,81) { _C = RES(0,_C); 										} /* RES  0,C		  */
    /*TODO*///OP(cb,82) { _D = RES(0,_D); 										} /* RES  0,D		  */
    /*TODO*///OP(cb,83) { _E = RES(0,_E); 										} /* RES  0,E		  */
    /*TODO*///OP(cb,84) { _H = RES(0,_H); 										} /* RES  0,H		  */
    /*TODO*///OP(cb,85) { _L = RES(0,_L); 										} /* RES  0,L		  */
    /*TODO*///OP(cb,86) { WM( _HL, RES(0,RM(_HL)) );								} /* RES  0,(HL)	  */
    /*TODO*///OP(cb,87) { _A = RES(0,_A); 										} /* RES  0,A		  */
    /*TODO*///
    /*TODO*///OP(cb,88) { _B = RES(1,_B); 										} /* RES  1,B		  */
    /*TODO*///OP(cb,89) { _C = RES(1,_C); 										} /* RES  1,C		  */
    /*TODO*///OP(cb,8a) { _D = RES(1,_D); 										} /* RES  1,D		  */
    /*TODO*///OP(cb,8b) { _E = RES(1,_E); 										} /* RES  1,E		  */
    /*TODO*///OP(cb,8c) { _H = RES(1,_H); 										} /* RES  1,H		  */
    /*TODO*///OP(cb,8d) { _L = RES(1,_L); 										} /* RES  1,L		  */
    /*TODO*///OP(cb,8e) { WM( _HL, RES(1,RM(_HL)) );								} /* RES  1,(HL)	  */
    /*TODO*///OP(cb,8f) { _A = RES(1,_A); 										} /* RES  1,A		  */
    /*TODO*///
    /*TODO*///OP(cb,90) { _B = RES(2,_B); 										} /* RES  2,B		  */
    /*TODO*///OP(cb,91) { _C = RES(2,_C); 										} /* RES  2,C		  */
    /*TODO*///OP(cb,92) { _D = RES(2,_D); 										} /* RES  2,D		  */
    /*TODO*///OP(cb,93) { _E = RES(2,_E); 										} /* RES  2,E		  */
    /*TODO*///OP(cb,94) { _H = RES(2,_H); 										} /* RES  2,H		  */
    /*TODO*///OP(cb,95) { _L = RES(2,_L); 										} /* RES  2,L		  */
    /*TODO*///OP(cb,96) { WM( _HL, RES(2,RM(_HL)) );								} /* RES  2,(HL)	  */
    /*TODO*///OP(cb,97) { _A = RES(2,_A); 										} /* RES  2,A		  */
    /*TODO*///
    /*TODO*///OP(cb,98) { _B = RES(3,_B); 										} /* RES  3,B		  */
    /*TODO*///OP(cb,99) { _C = RES(3,_C); 										} /* RES  3,C		  */
    /*TODO*///OP(cb,9a) { _D = RES(3,_D); 										} /* RES  3,D		  */
    /*TODO*///OP(cb,9b) { _E = RES(3,_E); 										} /* RES  3,E		  */
    /*TODO*///OP(cb,9c) { _H = RES(3,_H); 										} /* RES  3,H		  */
    /*TODO*///OP(cb,9d) { _L = RES(3,_L); 										} /* RES  3,L		  */
    /*TODO*///OP(cb,9e) { WM( _HL, RES(3,RM(_HL)) );								} /* RES  3,(HL)	  */
    /*TODO*///OP(cb,9f) { _A = RES(3,_A); 										} /* RES  3,A		  */
    /*TODO*///
    /*TODO*///OP(cb,a0) { _B = RES(4,_B); 										} /* RES  4,B		  */
    /*TODO*///OP(cb,a1) { _C = RES(4,_C); 										} /* RES  4,C		  */
    /*TODO*///OP(cb,a2) { _D = RES(4,_D); 										} /* RES  4,D		  */
    /*TODO*///OP(cb,a3) { _E = RES(4,_E); 										} /* RES  4,E		  */
    /*TODO*///OP(cb,a4) { _H = RES(4,_H); 										} /* RES  4,H		  */
    /*TODO*///OP(cb,a5) { _L = RES(4,_L); 										} /* RES  4,L		  */
    /*TODO*///OP(cb,a6) { WM( _HL, RES(4,RM(_HL)) );								} /* RES  4,(HL)	  */
    /*TODO*///OP(cb,a7) { _A = RES(4,_A); 										} /* RES  4,A		  */
    /*TODO*///
    /*TODO*///OP(cb,a8) { _B = RES(5,_B); 										} /* RES  5,B		  */
    /*TODO*///OP(cb,a9) { _C = RES(5,_C); 										} /* RES  5,C		  */
    /*TODO*///OP(cb,aa) { _D = RES(5,_D); 										} /* RES  5,D		  */
    /*TODO*///OP(cb,ab) { _E = RES(5,_E); 										} /* RES  5,E		  */
    /*TODO*///OP(cb,ac) { _H = RES(5,_H); 										} /* RES  5,H		  */
    /*TODO*///OP(cb,ad) { _L = RES(5,_L); 										} /* RES  5,L		  */
    /*TODO*///OP(cb,ae) { WM( _HL, RES(5,RM(_HL)) );								} /* RES  5,(HL)	  */
    /*TODO*///OP(cb,af) { _A = RES(5,_A); 										} /* RES  5,A		  */
    /*TODO*///
    /*TODO*///OP(cb,b0) { _B = RES(6,_B); 										} /* RES  6,B		  */
    /*TODO*///OP(cb,b1) { _C = RES(6,_C); 										} /* RES  6,C		  */
    /*TODO*///OP(cb,b2) { _D = RES(6,_D); 										} /* RES  6,D		  */
    /*TODO*///OP(cb,b3) { _E = RES(6,_E); 										} /* RES  6,E		  */
    /*TODO*///OP(cb,b4) { _H = RES(6,_H); 										} /* RES  6,H		  */
    /*TODO*///OP(cb,b5) { _L = RES(6,_L); 										} /* RES  6,L		  */
    /*TODO*///OP(cb,b6) { WM( _HL, RES(6,RM(_HL)) );								} /* RES  6,(HL)	  */
    /*TODO*///OP(cb,b7) { _A = RES(6,_A); 										} /* RES  6,A		  */
    /*TODO*///
    /*TODO*///OP(cb,b8) { _B = RES(7,_B); 										} /* RES  7,B		  */
    /*TODO*///OP(cb,b9) { _C = RES(7,_C); 										} /* RES  7,C		  */
    /*TODO*///OP(cb,ba) { _D = RES(7,_D); 										} /* RES  7,D		  */
    /*TODO*///OP(cb,bb) { _E = RES(7,_E); 										} /* RES  7,E		  */
    /*TODO*///OP(cb,bc) { _H = RES(7,_H); 										} /* RES  7,H		  */
    /*TODO*///OP(cb,bd) { _L = RES(7,_L); 										} /* RES  7,L		  */
    /*TODO*///OP(cb,be) { WM( _HL, RES(7,RM(_HL)) );								} /* RES  7,(HL)	  */
    /*TODO*///OP(cb,bf) { _A = RES(7,_A); 										} /* RES  7,A		  */
    /*TODO*///
    /*TODO*///OP(cb,c0) { _B = SET(0,_B); 										} /* SET  0,B		  */
    /*TODO*///OP(cb,c1) { _C = SET(0,_C); 										} /* SET  0,C		  */
    /*TODO*///OP(cb,c2) { _D = SET(0,_D); 										} /* SET  0,D		  */
    /*TODO*///OP(cb,c3) { _E = SET(0,_E); 										} /* SET  0,E		  */
    /*TODO*///OP(cb,c4) { _H = SET(0,_H); 										} /* SET  0,H		  */
    /*TODO*///OP(cb,c5) { _L = SET(0,_L); 										} /* SET  0,L		  */
    /*TODO*///OP(cb,c6) { WM( _HL, SET(0,RM(_HL)) );								} /* SET  0,(HL)	  */
    /*TODO*///OP(cb,c7) { _A = SET(0,_A); 										} /* SET  0,A		  */
    /*TODO*///
    /*TODO*///OP(cb,c8) { _B = SET(1,_B); 										} /* SET  1,B		  */
    /*TODO*///OP(cb,c9) { _C = SET(1,_C); 										} /* SET  1,C		  */
    /*TODO*///OP(cb,ca) { _D = SET(1,_D); 										} /* SET  1,D		  */
    /*TODO*///OP(cb,cb) { _E = SET(1,_E); 										} /* SET  1,E		  */
    /*TODO*///OP(cb,cc) { _H = SET(1,_H); 										} /* SET  1,H		  */
    /*TODO*///OP(cb,cd) { _L = SET(1,_L); 										} /* SET  1,L		  */
    /*TODO*///OP(cb,ce) { WM( _HL, SET(1,RM(_HL)) );								} /* SET  1,(HL)	  */
    /*TODO*///OP(cb,cf) { _A = SET(1,_A); 										} /* SET  1,A		  */
    /*TODO*///
    /*TODO*///OP(cb,d0) { _B = SET(2,_B); 										} /* SET  2,B		  */
    /*TODO*///OP(cb,d1) { _C = SET(2,_C); 										} /* SET  2,C		  */
    /*TODO*///OP(cb,d2) { _D = SET(2,_D); 										} /* SET  2,D		  */
    /*TODO*///OP(cb,d3) { _E = SET(2,_E); 										} /* SET  2,E		  */
    /*TODO*///OP(cb,d4) { _H = SET(2,_H); 										} /* SET  2,H		  */
    /*TODO*///OP(cb,d5) { _L = SET(2,_L); 										} /* SET  2,L		  */
    /*TODO*///OP(cb,d6) { WM( _HL, SET(2,RM(_HL)) );								}/* SET  2,(HL) 	 */
    /*TODO*///OP(cb,d7) { _A = SET(2,_A); 										} /* SET  2,A		  */
    /*TODO*///
    /*TODO*///OP(cb,d8) { _B = SET(3,_B); 										} /* SET  3,B		  */
    /*TODO*///OP(cb,d9) { _C = SET(3,_C); 										} /* SET  3,C		  */
    /*TODO*///OP(cb,da) { _D = SET(3,_D); 										} /* SET  3,D		  */
    /*TODO*///OP(cb,db) { _E = SET(3,_E); 										} /* SET  3,E		  */
    /*TODO*///OP(cb,dc) { _H = SET(3,_H); 										} /* SET  3,H		  */
    /*TODO*///OP(cb,dd) { _L = SET(3,_L); 										} /* SET  3,L		  */
    /*TODO*///OP(cb,de) { WM( _HL, SET(3,RM(_HL)) );								} /* SET  3,(HL)	  */
    /*TODO*///OP(cb,df) { _A = SET(3,_A); 										} /* SET  3,A		  */
    /*TODO*///
    /*TODO*///OP(cb,e0) { _B = SET(4,_B); 										} /* SET  4,B		  */
    /*TODO*///OP(cb,e1) { _C = SET(4,_C); 										} /* SET  4,C		  */
    /*TODO*///OP(cb,e2) { _D = SET(4,_D); 										} /* SET  4,D		  */
    /*TODO*///OP(cb,e3) { _E = SET(4,_E); 										} /* SET  4,E		  */
    /*TODO*///OP(cb,e4) { _H = SET(4,_H); 										} /* SET  4,H		  */
    /*TODO*///OP(cb,e5) { _L = SET(4,_L); 										} /* SET  4,L		  */
    /*TODO*///OP(cb,e6) { WM( _HL, SET(4,RM(_HL)) );								} /* SET  4,(HL)	  */
    /*TODO*///OP(cb,e7) { _A = SET(4,_A); 										} /* SET  4,A		  */
    /*TODO*///
    /*TODO*///OP(cb,e8) { _B = SET(5,_B); 										} /* SET  5,B		  */
    /*TODO*///OP(cb,e9) { _C = SET(5,_C); 										} /* SET  5,C		  */
    /*TODO*///OP(cb,ea) { _D = SET(5,_D); 										} /* SET  5,D		  */
    /*TODO*///OP(cb,eb) { _E = SET(5,_E); 										} /* SET  5,E		  */
    /*TODO*///OP(cb,ec) { _H = SET(5,_H); 										} /* SET  5,H		  */
    /*TODO*///OP(cb,ed) { _L = SET(5,_L); 										} /* SET  5,L		  */
    /*TODO*///OP(cb,ee) { WM( _HL, SET(5,RM(_HL)) );								} /* SET  5,(HL)	  */
    /*TODO*///OP(cb,ef) { _A = SET(5,_A); 										} /* SET  5,A		  */
    /*TODO*///
    /*TODO*///OP(cb,f0) { _B = SET(6,_B); 										} /* SET  6,B		  */
    /*TODO*///OP(cb,f1) { _C = SET(6,_C); 										} /* SET  6,C		  */
    /*TODO*///OP(cb,f2) { _D = SET(6,_D); 										} /* SET  6,D		  */
    /*TODO*///OP(cb,f3) { _E = SET(6,_E); 										} /* SET  6,E		  */
    /*TODO*///OP(cb,f4) { _H = SET(6,_H); 										} /* SET  6,H		  */
    /*TODO*///OP(cb,f5) { _L = SET(6,_L); 										} /* SET  6,L		  */
    /*TODO*///OP(cb,f6) { WM( _HL, SET(6,RM(_HL)) );								} /* SET  6,(HL)	  */
    /*TODO*///OP(cb,f7) { _A = SET(6,_A); 										} /* SET  6,A		  */
    /*TODO*///
    /*TODO*///OP(cb,f8) { _B = SET(7,_B); 										} /* SET  7,B		  */
    /*TODO*///OP(cb,f9) { _C = SET(7,_C); 										} /* SET  7,C		  */
    /*TODO*///OP(cb,fa) { _D = SET(7,_D); 										} /* SET  7,D		  */
    /*TODO*///OP(cb,fb) { _E = SET(7,_E); 										} /* SET  7,E		  */
    /*TODO*///OP(cb,fc) { _H = SET(7,_H); 										} /* SET  7,H		  */
    /*TODO*///OP(cb,fd) { _L = SET(7,_L); 										} /* SET  7,L		  */
    /*TODO*///OP(cb,fe) { WM( _HL, SET(7,RM(_HL)) );								} /* SET  7,(HL)	  */
    /*TODO*///OP(cb,ff) { _A = SET(7,_A); 										} /* SET  7,A		  */
    /*TODO*///
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*///* opcodes with DD/FD CB prefix
    /*TODO*///* rotate, shift and bit operations with (IX+o)
    /*TODO*///**********************************************************/
    /*TODO*///OP(xxcb,00) { _B = RLC( RM(EA) ); WM( EA,_B );						} /* RLC  B=(XY+o)	  */
    /*TODO*///OP(xxcb,01) { _C = RLC( RM(EA) ); WM( EA,_C );						} /* RLC  C=(XY+o)	  */
    /*TODO*///OP(xxcb,02) { _D = RLC( RM(EA) ); WM( EA,_D );						} /* RLC  D=(XY+o)	  */
    /*TODO*///OP(xxcb,03) { _E = RLC( RM(EA) ); WM( EA,_E );						} /* RLC  E=(XY+o)	  */
    /*TODO*///OP(xxcb,04) { _H = RLC( RM(EA) ); WM( EA,_H );						} /* RLC  H=(XY+o)	  */
    /*TODO*///OP(xxcb,05) { _L = RLC( RM(EA) ); WM( EA,_L );						} /* RLC  L=(XY+o)	  */
    /*TODO*///OP(xxcb,06) { WM( EA, RLC( RM(EA) ) );								} /* RLC  (XY+o)	  */
    /*TODO*///OP(xxcb,07) { _A = RLC( RM(EA) ); WM( EA,_A );						} /* RLC  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,08) { _B = RRC( RM(EA) ); WM( EA,_B );						} /* RRC  B=(XY+o)	  */
    /*TODO*///OP(xxcb,09) { _C = RRC( RM(EA) ); WM( EA,_C );						} /* RRC  C=(XY+o)	  */
    /*TODO*///OP(xxcb,0a) { _D = RRC( RM(EA) ); WM( EA,_D );						} /* RRC  D=(XY+o)	  */
    /*TODO*///OP(xxcb,0b) { _E = RRC( RM(EA) ); WM( EA,_E );						} /* RRC  E=(XY+o)	  */
    /*TODO*///OP(xxcb,0c) { _H = RRC( RM(EA) ); WM( EA,_H );						} /* RRC  H=(XY+o)	  */
    /*TODO*///OP(xxcb,0d) { _L = RRC( RM(EA) ); WM( EA,_L );						} /* RRC  L=(XY+o)	  */
    /*TODO*///OP(xxcb,0e) { WM( EA,RRC( RM(EA) ) );								} /* RRC  (XY+o)	  */
    /*TODO*///OP(xxcb,0f) { _A = RRC( RM(EA) ); WM( EA,_A );						} /* RRC  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,10) { _B = RL( RM(EA) ); WM( EA,_B );						} /* RL   B=(XY+o)	  */
    /*TODO*///OP(xxcb,11) { _C = RL( RM(EA) ); WM( EA,_C );						} /* RL   C=(XY+o)	  */
    /*TODO*///OP(xxcb,12) { _D = RL( RM(EA) ); WM( EA,_D );						} /* RL   D=(XY+o)	  */
    /*TODO*///OP(xxcb,13) { _E = RL( RM(EA) ); WM( EA,_E );						} /* RL   E=(XY+o)	  */
    /*TODO*///OP(xxcb,14) { _H = RL( RM(EA) ); WM( EA,_H );						} /* RL   H=(XY+o)	  */
    /*TODO*///OP(xxcb,15) { _L = RL( RM(EA) ); WM( EA,_L );						} /* RL   L=(XY+o)	  */
    /*TODO*///OP(xxcb,16) { WM( EA,RL( RM(EA) ) );								} /* RL   (XY+o)	  */
    /*TODO*///OP(xxcb,17) { _A = RL( RM(EA) ); WM( EA,_A );						} /* RL   A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,18) { _B = RR( RM(EA) ); WM( EA,_B );						} /* RR   B=(XY+o)	  */
    /*TODO*///OP(xxcb,19) { _C = RR( RM(EA) ); WM( EA,_C );						} /* RR   C=(XY+o)	  */
    /*TODO*///OP(xxcb,1a) { _D = RR( RM(EA) ); WM( EA,_D );						} /* RR   D=(XY+o)	  */
    /*TODO*///OP(xxcb,1b) { _E = RR( RM(EA) ); WM( EA,_E );						} /* RR   E=(XY+o)	  */
    /*TODO*///OP(xxcb,1c) { _H = RR( RM(EA) ); WM( EA,_H );						} /* RR   H=(XY+o)	  */
    /*TODO*///OP(xxcb,1d) { _L = RR( RM(EA) ); WM( EA,_L );						} /* RR   L=(XY+o)	  */
    /*TODO*///OP(xxcb,1e) { WM( EA,RR( RM(EA) ) );								} /* RR   (XY+o)	  */
    /*TODO*///OP(xxcb,1f) { _A = RR( RM(EA) ); WM( EA,_A );						} /* RR   A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,20) { _B = SLA( RM(EA) ); WM( EA,_B );						} /* SLA  B=(XY+o)	  */
    /*TODO*///OP(xxcb,21) { _C = SLA( RM(EA) ); WM( EA,_C );						} /* SLA  C=(XY+o)	  */
    /*TODO*///OP(xxcb,22) { _D = SLA( RM(EA) ); WM( EA,_D );						} /* SLA  D=(XY+o)	  */
    /*TODO*///OP(xxcb,23) { _E = SLA( RM(EA) ); WM( EA,_E );						} /* SLA  E=(XY+o)	  */
    /*TODO*///OP(xxcb,24) { _H = SLA( RM(EA) ); WM( EA,_H );						} /* SLA  H=(XY+o)	  */
    /*TODO*///OP(xxcb,25) { _L = SLA( RM(EA) ); WM( EA,_L );						} /* SLA  L=(XY+o)	  */
    /*TODO*///OP(xxcb,26) { WM( EA,SLA( RM(EA) ) );								} /* SLA  (XY+o)	  */
    /*TODO*///OP(xxcb,27) { _A = SLA( RM(EA) ); WM( EA,_A );						} /* SLA  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,28) { _B = SRA( RM(EA) ); WM( EA,_B );						} /* SRA  B=(XY+o)	  */
    /*TODO*///OP(xxcb,29) { _C = SRA( RM(EA) ); WM( EA,_C );						} /* SRA  C=(XY+o)	  */
    /*TODO*///OP(xxcb,2a) { _D = SRA( RM(EA) ); WM( EA,_D );						} /* SRA  D=(XY+o)	  */
    /*TODO*///OP(xxcb,2b) { _E = SRA( RM(EA) ); WM( EA,_E );						} /* SRA  E=(XY+o)	  */
    /*TODO*///OP(xxcb,2c) { _H = SRA( RM(EA) ); WM( EA,_H );						} /* SRA  H=(XY+o)	  */
    /*TODO*///OP(xxcb,2d) { _L = SRA( RM(EA) ); WM( EA,_L );						} /* SRA  L=(XY+o)	  */
    /*TODO*///OP(xxcb,2e) { WM( EA,SRA( RM(EA) ) );								} /* SRA  (XY+o)	  */
    /*TODO*///OP(xxcb,2f) { _A = SRA( RM(EA) ); WM( EA,_A );						} /* SRA  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,30) { _B = SLL( RM(EA) ); WM( EA,_B );						} /* SLL  B=(XY+o)	  */
    /*TODO*///OP(xxcb,31) { _C = SLL( RM(EA) ); WM( EA,_C );						} /* SLL  C=(XY+o)	  */
    /*TODO*///OP(xxcb,32) { _D = SLL( RM(EA) ); WM( EA,_D );						} /* SLL  D=(XY+o)	  */
    /*TODO*///OP(xxcb,33) { _E = SLL( RM(EA) ); WM( EA,_E );						} /* SLL  E=(XY+o)	  */
    /*TODO*///OP(xxcb,34) { _H = SLL( RM(EA) ); WM( EA,_H );						} /* SLL  H=(XY+o)	  */
    /*TODO*///OP(xxcb,35) { _L = SLL( RM(EA) ); WM( EA,_L );						} /* SLL  L=(XY+o)	  */
    /*TODO*///OP(xxcb,36) { WM( EA,SLL( RM(EA) ) );								} /* SLL  (XY+o)	  */
    /*TODO*///OP(xxcb,37) { _A = SLL( RM(EA) ); WM( EA,_A );						} /* SLL  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,38) { _B = SRL( RM(EA) ); WM( EA,_B );						} /* SRL  B=(XY+o)	  */
    /*TODO*///OP(xxcb,39) { _C = SRL( RM(EA) ); WM( EA,_C );						} /* SRL  C=(XY+o)	  */
    /*TODO*///OP(xxcb,3a) { _D = SRL( RM(EA) ); WM( EA,_D );						} /* SRL  D=(XY+o)	  */
    /*TODO*///OP(xxcb,3b) { _E = SRL( RM(EA) ); WM( EA,_E );						} /* SRL  E=(XY+o)	  */
    /*TODO*///OP(xxcb,3c) { _H = SRL( RM(EA) ); WM( EA,_H );						} /* SRL  H=(XY+o)	  */
    /*TODO*///OP(xxcb,3d) { _L = SRL( RM(EA) ); WM( EA,_L );						} /* SRL  L=(XY+o)	  */
    /*TODO*///OP(xxcb,3e) { WM( EA,SRL( RM(EA) ) );								} /* SRL  (XY+o)	  */
    /*TODO*///OP(xxcb,3f) { _A = SRL( RM(EA) ); WM( EA,_A );						} /* SRL  A=(XY+o)	  */
    /*TODO*///
    /*TODO*///OP(xxcb,40) { xxcb_46();											} /* BIT  0,B=(XY+o)  */
    /*TODO*///OP(xxcb,41) { xxcb_46();													  } /* BIT	0,C=(XY+o)	*/
    /*TODO*///OP(xxcb,42) { xxcb_46();											} /* BIT  0,D=(XY+o)  */
    /*TODO*///OP(xxcb,43) { xxcb_46();											} /* BIT  0,E=(XY+o)  */
    /*TODO*///OP(xxcb,44) { xxcb_46();											} /* BIT  0,H=(XY+o)  */
    /*TODO*///OP(xxcb,45) { xxcb_46();											} /* BIT  0,L=(XY+o)  */
    /*TODO*///OP(xxcb,46) { BIT_XY(0,RM(EA)); 									} /* BIT  0,(XY+o)	  */
    /*TODO*///OP(xxcb,47) { xxcb_46();											} /* BIT  0,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,48) { xxcb_4e();											} /* BIT  1,B=(XY+o)  */
    /*TODO*///OP(xxcb,49) { xxcb_4e();													  } /* BIT	1,C=(XY+o)	*/
    /*TODO*///OP(xxcb,4a) { xxcb_4e();											} /* BIT  1,D=(XY+o)  */
    /*TODO*///OP(xxcb,4b) { xxcb_4e();											} /* BIT  1,E=(XY+o)  */
    /*TODO*///OP(xxcb,4c) { xxcb_4e();											} /* BIT  1,H=(XY+o)  */
    /*TODO*///OP(xxcb,4d) { xxcb_4e();											} /* BIT  1,L=(XY+o)  */
    /*TODO*///OP(xxcb,4e) { BIT_XY(1,RM(EA)); 									} /* BIT  1,(XY+o)	  */
    /*TODO*///OP(xxcb,4f) { xxcb_4e();											} /* BIT  1,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,50) { xxcb_56();											} /* BIT  2,B=(XY+o)  */
    /*TODO*///OP(xxcb,51) { xxcb_56();													  } /* BIT	2,C=(XY+o)	*/
    /*TODO*///OP(xxcb,52) { xxcb_56();											} /* BIT  2,D=(XY+o)  */
    /*TODO*///OP(xxcb,53) { xxcb_56();											} /* BIT  2,E=(XY+o)  */
    /*TODO*///OP(xxcb,54) { xxcb_56();											} /* BIT  2,H=(XY+o)  */
    /*TODO*///OP(xxcb,55) { xxcb_56();											} /* BIT  2,L=(XY+o)  */
    /*TODO*///OP(xxcb,56) { BIT_XY(2,RM(EA)); 									} /* BIT  2,(XY+o)	  */
    /*TODO*///OP(xxcb,57) { xxcb_56();											} /* BIT  2,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,58) { xxcb_5e();											} /* BIT  3,B=(XY+o)  */
    /*TODO*///OP(xxcb,59) { xxcb_5e();													  } /* BIT	3,C=(XY+o)	*/
    /*TODO*///OP(xxcb,5a) { xxcb_5e();											} /* BIT  3,D=(XY+o)  */
    /*TODO*///OP(xxcb,5b) { xxcb_5e();											} /* BIT  3,E=(XY+o)  */
    /*TODO*///OP(xxcb,5c) { xxcb_5e();											} /* BIT  3,H=(XY+o)  */
    /*TODO*///OP(xxcb,5d) { xxcb_5e();											} /* BIT  3,L=(XY+o)  */
    /*TODO*///OP(xxcb,5e) { BIT_XY(3,RM(EA)); 									} /* BIT  3,(XY+o)	  */
    /*TODO*///OP(xxcb,5f) { xxcb_5e();											} /* BIT  3,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,60) { xxcb_66();											} /* BIT  4,B=(XY+o)  */
    /*TODO*///OP(xxcb,61) { xxcb_66();													  } /* BIT	4,C=(XY+o)	*/
    /*TODO*///OP(xxcb,62) { xxcb_66();											} /* BIT  4,D=(XY+o)  */
    /*TODO*///OP(xxcb,63) { xxcb_66();											} /* BIT  4,E=(XY+o)  */
    /*TODO*///OP(xxcb,64) { xxcb_66();											} /* BIT  4,H=(XY+o)  */
    /*TODO*///OP(xxcb,65) { xxcb_66();											} /* BIT  4,L=(XY+o)  */
    /*TODO*///OP(xxcb,66) { BIT_XY(4,RM(EA)); 									} /* BIT  4,(XY+o)	  */
    /*TODO*///OP(xxcb,67) { xxcb_66();											} /* BIT  4,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,68) { xxcb_6e();											} /* BIT  5,B=(XY+o)  */
    /*TODO*///OP(xxcb,69) { xxcb_6e();													  } /* BIT	5,C=(XY+o)	*/
    /*TODO*///OP(xxcb,6a) { xxcb_6e();											} /* BIT  5,D=(XY+o)  */
    /*TODO*///OP(xxcb,6b) { xxcb_6e();											} /* BIT  5,E=(XY+o)  */
    /*TODO*///OP(xxcb,6c) { xxcb_6e();											} /* BIT  5,H=(XY+o)  */
    /*TODO*///OP(xxcb,6d) { xxcb_6e();											} /* BIT  5,L=(XY+o)  */
    /*TODO*///OP(xxcb,6e) { BIT_XY(5,RM(EA)); 									} /* BIT  5,(XY+o)	  */
    /*TODO*///OP(xxcb,6f) { xxcb_6e();											} /* BIT  5,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,70) { xxcb_76();											} /* BIT  6,B=(XY+o)  */
    /*TODO*///OP(xxcb,71) { xxcb_76();													  } /* BIT	6,C=(XY+o)	*/
    /*TODO*///OP(xxcb,72) { xxcb_76();											} /* BIT  6,D=(XY+o)  */
    /*TODO*///OP(xxcb,73) { xxcb_76();											} /* BIT  6,E=(XY+o)  */
    /*TODO*///OP(xxcb,74) { xxcb_76();											} /* BIT  6,H=(XY+o)  */
    /*TODO*///OP(xxcb,75) { xxcb_76();											} /* BIT  6,L=(XY+o)  */
    /*TODO*///OP(xxcb,76) { BIT_XY(6,RM(EA)); 									} /* BIT  6,(XY+o)	  */
    /*TODO*///OP(xxcb,77) { xxcb_76();											} /* BIT  6,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,78) { xxcb_7e();											} /* BIT  7,B=(XY+o)  */
    /*TODO*///OP(xxcb,79) { xxcb_7e();													  } /* BIT	7,C=(XY+o)	*/
    /*TODO*///OP(xxcb,7a) { xxcb_7e();											} /* BIT  7,D=(XY+o)  */
    /*TODO*///OP(xxcb,7b) { xxcb_7e();											} /* BIT  7,E=(XY+o)  */
    /*TODO*///OP(xxcb,7c) { xxcb_7e();											} /* BIT  7,H=(XY+o)  */
    /*TODO*///OP(xxcb,7d) { xxcb_7e();											} /* BIT  7,L=(XY+o)  */
    /*TODO*///OP(xxcb,7e) { BIT_XY(7,RM(EA)); 									} /* BIT  7,(XY+o)	  */
    /*TODO*///OP(xxcb,7f) { xxcb_7e();											} /* BIT  7,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,80) { _B = RES(0, RM(EA) ); WM( EA,_B );					} /* RES  0,B=(XY+o)  */
    /*TODO*///OP(xxcb,81) { _C = RES(0, RM(EA) ); WM( EA,_C );					} /* RES  0,C=(XY+o)  */
    /*TODO*///OP(xxcb,82) { _D = RES(0, RM(EA) ); WM( EA,_D );					} /* RES  0,D=(XY+o)  */
    /*TODO*///OP(xxcb,83) { _E = RES(0, RM(EA) ); WM( EA,_E );					} /* RES  0,E=(XY+o)  */
    /*TODO*///OP(xxcb,84) { _H = RES(0, RM(EA) ); WM( EA,_H );					} /* RES  0,H=(XY+o)  */
    /*TODO*///OP(xxcb,85) { _L = RES(0, RM(EA) ); WM( EA,_L );					} /* RES  0,L=(XY+o)  */
    /*TODO*///OP(xxcb,86) { WM( EA, RES(0,RM(EA)) );								} /* RES  0,(XY+o)	  */
    /*TODO*///OP(xxcb,87) { _A = RES(0, RM(EA) ); WM( EA,_A );					} /* RES  0,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,88) { _B = RES(1, RM(EA) ); WM( EA,_B );					} /* RES  1,B=(XY+o)  */
    /*TODO*///OP(xxcb,89) { _C = RES(1, RM(EA) ); WM( EA,_C );					} /* RES  1,C=(XY+o)  */
    /*TODO*///OP(xxcb,8a) { _D = RES(1, RM(EA) ); WM( EA,_D );					} /* RES  1,D=(XY+o)  */
    /*TODO*///OP(xxcb,8b) { _E = RES(1, RM(EA) ); WM( EA,_E );					} /* RES  1,E=(XY+o)  */
    /*TODO*///OP(xxcb,8c) { _H = RES(1, RM(EA) ); WM( EA,_H );					} /* RES  1,H=(XY+o)  */
    /*TODO*///OP(xxcb,8d) { _L = RES(1, RM(EA) ); WM( EA,_L );					} /* RES  1,L=(XY+o)  */
    /*TODO*///OP(xxcb,8e) { WM( EA, RES(1,RM(EA)) );								} /* RES  1,(XY+o)	  */
    /*TODO*///OP(xxcb,8f) { _A = RES(1, RM(EA) ); WM( EA,_A );					} /* RES  1,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,90) { _B = RES(2, RM(EA) ); WM( EA,_B );					} /* RES  2,B=(XY+o)  */
    /*TODO*///OP(xxcb,91) { _C = RES(2, RM(EA) ); WM( EA,_C );					} /* RES  2,C=(XY+o)  */
    /*TODO*///OP(xxcb,92) { _D = RES(2, RM(EA) ); WM( EA,_D );					} /* RES  2,D=(XY+o)  */
    /*TODO*///OP(xxcb,93) { _E = RES(2, RM(EA) ); WM( EA,_E );					} /* RES  2,E=(XY+o)  */
    /*TODO*///OP(xxcb,94) { _H = RES(2, RM(EA) ); WM( EA,_H );					} /* RES  2,H=(XY+o)  */
    /*TODO*///OP(xxcb,95) { _L = RES(2, RM(EA) ); WM( EA,_L );					} /* RES  2,L=(XY+o)  */
    /*TODO*///OP(xxcb,96) { WM( EA, RES(2,RM(EA)) );								} /* RES  2,(XY+o)	  */
    /*TODO*///OP(xxcb,97) { _A = RES(2, RM(EA) ); WM( EA,_A );					} /* RES  2,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,98) { _B = RES(3, RM(EA) ); WM( EA,_B );					} /* RES  3,B=(XY+o)  */
    /*TODO*///OP(xxcb,99) { _C = RES(3, RM(EA) ); WM( EA,_C );					} /* RES  3,C=(XY+o)  */
    /*TODO*///OP(xxcb,9a) { _D = RES(3, RM(EA) ); WM( EA,_D );					} /* RES  3,D=(XY+o)  */
    /*TODO*///OP(xxcb,9b) { _E = RES(3, RM(EA) ); WM( EA,_E );					} /* RES  3,E=(XY+o)  */
    /*TODO*///OP(xxcb,9c) { _H = RES(3, RM(EA) ); WM( EA,_H );					} /* RES  3,H=(XY+o)  */
    /*TODO*///OP(xxcb,9d) { _L = RES(3, RM(EA) ); WM( EA,_L );					} /* RES  3,L=(XY+o)  */
    /*TODO*///OP(xxcb,9e) { WM( EA, RES(3,RM(EA)) );								} /* RES  3,(XY+o)	  */
    /*TODO*///OP(xxcb,9f) { _A = RES(3, RM(EA) ); WM( EA,_A );					} /* RES  3,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,a0) { _B = RES(4, RM(EA) ); WM( EA,_B );					} /* RES  4,B=(XY+o)  */
    /*TODO*///OP(xxcb,a1) { _C = RES(4, RM(EA) ); WM( EA,_C );					} /* RES  4,C=(XY+o)  */
    /*TODO*///OP(xxcb,a2) { _D = RES(4, RM(EA) ); WM( EA,_D );					} /* RES  4,D=(XY+o)  */
    /*TODO*///OP(xxcb,a3) { _E = RES(4, RM(EA) ); WM( EA,_E );					} /* RES  4,E=(XY+o)  */
    /*TODO*///OP(xxcb,a4) { _H = RES(4, RM(EA) ); WM( EA,_H );					} /* RES  4,H=(XY+o)  */
    /*TODO*///OP(xxcb,a5) { _L = RES(4, RM(EA) ); WM( EA,_L );					} /* RES  4,L=(XY+o)  */
    /*TODO*///OP(xxcb,a6) { WM( EA, RES(4,RM(EA)) );								} /* RES  4,(XY+o)	  */
    /*TODO*///OP(xxcb,a7) { _A = RES(4, RM(EA) ); WM( EA,_A );					} /* RES  4,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,a8) { _B = RES(5, RM(EA) ); WM( EA,_B );					} /* RES  5,B=(XY+o)  */
    /*TODO*///OP(xxcb,a9) { _C = RES(5, RM(EA) ); WM( EA,_C );					} /* RES  5,C=(XY+o)  */
    /*TODO*///OP(xxcb,aa) { _D = RES(5, RM(EA) ); WM( EA,_D );					} /* RES  5,D=(XY+o)  */
    /*TODO*///OP(xxcb,ab) { _E = RES(5, RM(EA) ); WM( EA,_E );					} /* RES  5,E=(XY+o)  */
    /*TODO*///OP(xxcb,ac) { _H = RES(5, RM(EA) ); WM( EA,_H );					} /* RES  5,H=(XY+o)  */
    /*TODO*///OP(xxcb,ad) { _L = RES(5, RM(EA) ); WM( EA,_L );					} /* RES  5,L=(XY+o)  */
    /*TODO*///OP(xxcb,ae) { WM( EA, RES(5,RM(EA)) );								} /* RES  5,(XY+o)	  */
    /*TODO*///OP(xxcb,af) { _A = RES(5, RM(EA) ); WM( EA,_A );					} /* RES  5,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,b0) { _B = RES(6, RM(EA) ); WM( EA,_B );					} /* RES  6,B=(XY+o)  */
    /*TODO*///OP(xxcb,b1) { _C = RES(6, RM(EA) ); WM( EA,_C );					} /* RES  6,C=(XY+o)  */
    /*TODO*///OP(xxcb,b2) { _D = RES(6, RM(EA) ); WM( EA,_D );					} /* RES  6,D=(XY+o)  */
    /*TODO*///OP(xxcb,b3) { _E = RES(6, RM(EA) ); WM( EA,_E );					} /* RES  6,E=(XY+o)  */
    /*TODO*///OP(xxcb,b4) { _H = RES(6, RM(EA) ); WM( EA,_H );					} /* RES  6,H=(XY+o)  */
    /*TODO*///OP(xxcb,b5) { _L = RES(6, RM(EA) ); WM( EA,_L );					} /* RES  6,L=(XY+o)  */
    /*TODO*///OP(xxcb,b6) { WM( EA, RES(6,RM(EA)) );								} /* RES  6,(XY+o)	  */
    /*TODO*///OP(xxcb,b7) { _A = RES(6, RM(EA) ); WM( EA,_A );					} /* RES  6,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,b8) { _B = RES(7, RM(EA) ); WM( EA,_B );					} /* RES  7,B=(XY+o)  */
    /*TODO*///OP(xxcb,b9) { _C = RES(7, RM(EA) ); WM( EA,_C );					} /* RES  7,C=(XY+o)  */
    /*TODO*///OP(xxcb,ba) { _D = RES(7, RM(EA) ); WM( EA,_D );					} /* RES  7,D=(XY+o)  */
    /*TODO*///OP(xxcb,bb) { _E = RES(7, RM(EA) ); WM( EA,_E );					} /* RES  7,E=(XY+o)  */
    /*TODO*///OP(xxcb,bc) { _H = RES(7, RM(EA) ); WM( EA,_H );					} /* RES  7,H=(XY+o)  */
    /*TODO*///OP(xxcb,bd) { _L = RES(7, RM(EA) ); WM( EA,_L );					} /* RES  7,L=(XY+o)  */
    /*TODO*///OP(xxcb,be) { WM( EA, RES(7,RM(EA)) );								} /* RES  7,(XY+o)	  */
    /*TODO*///OP(xxcb,bf) { _A = RES(7, RM(EA) ); WM( EA,_A );					} /* RES  7,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,c0) { _B = SET(0, RM(EA) ); WM( EA,_B );					} /* SET  0,B=(XY+o)  */
    /*TODO*///OP(xxcb,c1) { _C = SET(0, RM(EA) ); WM( EA,_C );					} /* SET  0,C=(XY+o)  */
    /*TODO*///OP(xxcb,c2) { _D = SET(0, RM(EA) ); WM( EA,_D );					} /* SET  0,D=(XY+o)  */
    /*TODO*///OP(xxcb,c3) { _E = SET(0, RM(EA) ); WM( EA,_E );					} /* SET  0,E=(XY+o)  */
    /*TODO*///OP(xxcb,c4) { _H = SET(0, RM(EA) ); WM( EA,_H );					} /* SET  0,H=(XY+o)  */
    /*TODO*///OP(xxcb,c5) { _L = SET(0, RM(EA) ); WM( EA,_L );					} /* SET  0,L=(XY+o)  */
    /*TODO*///OP(xxcb,c6) { WM( EA, SET(0,RM(EA)) );								} /* SET  0,(XY+o)	  */
    /*TODO*///OP(xxcb,c7) { _A = SET(0, RM(EA) ); WM( EA,_A );					} /* SET  0,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,c8) { _B = SET(1, RM(EA) ); WM( EA,_B );					} /* SET  1,B=(XY+o)  */
    /*TODO*///OP(xxcb,c9) { _C = SET(1, RM(EA) ); WM( EA,_C );					} /* SET  1,C=(XY+o)  */
    /*TODO*///OP(xxcb,ca) { _D = SET(1, RM(EA) ); WM( EA,_D );					} /* SET  1,D=(XY+o)  */
    /*TODO*///OP(xxcb,cb) { _E = SET(1, RM(EA) ); WM( EA,_E );					} /* SET  1,E=(XY+o)  */
    /*TODO*///OP(xxcb,cc) { _H = SET(1, RM(EA) ); WM( EA,_H );					} /* SET  1,H=(XY+o)  */
    /*TODO*///OP(xxcb,cd) { _L = SET(1, RM(EA) ); WM( EA,_L );					} /* SET  1,L=(XY+o)  */
    /*TODO*///OP(xxcb,ce) { WM( EA, SET(1,RM(EA)) );								} /* SET  1,(XY+o)	  */
    /*TODO*///OP(xxcb,cf) { _A = SET(1, RM(EA) ); WM( EA,_A );					} /* SET  1,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,d0) { _B = SET(2, RM(EA) ); WM( EA,_B );					} /* SET  2,B=(XY+o)  */
    /*TODO*///OP(xxcb,d1) { _C = SET(2, RM(EA) ); WM( EA,_C );					} /* SET  2,C=(XY+o)  */
    /*TODO*///OP(xxcb,d2) { _D = SET(2, RM(EA) ); WM( EA,_D );					} /* SET  2,D=(XY+o)  */
    /*TODO*///OP(xxcb,d3) { _E = SET(2, RM(EA) ); WM( EA,_E );					} /* SET  2,E=(XY+o)  */
    /*TODO*///OP(xxcb,d4) { _H = SET(2, RM(EA) ); WM( EA,_H );					} /* SET  2,H=(XY+o)  */
    /*TODO*///OP(xxcb,d5) { _L = SET(2, RM(EA) ); WM( EA,_L );					} /* SET  2,L=(XY+o)  */
    /*TODO*///OP(xxcb,d6) { WM( EA, SET(2,RM(EA)) );								} /* SET  2,(XY+o)	  */
    /*TODO*///OP(xxcb,d7) { _A = SET(2, RM(EA) ); WM( EA,_A );					} /* SET  2,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,d8) { _B = SET(3, RM(EA) ); WM( EA,_B );					} /* SET  3,B=(XY+o)  */
    /*TODO*///OP(xxcb,d9) { _C = SET(3, RM(EA) ); WM( EA,_C );					} /* SET  3,C=(XY+o)  */
    /*TODO*///OP(xxcb,da) { _D = SET(3, RM(EA) ); WM( EA,_D );					} /* SET  3,D=(XY+o)  */
    /*TODO*///OP(xxcb,db) { _E = SET(3, RM(EA) ); WM( EA,_E );					} /* SET  3,E=(XY+o)  */
    /*TODO*///OP(xxcb,dc) { _H = SET(3, RM(EA) ); WM( EA,_H );					} /* SET  3,H=(XY+o)  */
    /*TODO*///OP(xxcb,dd) { _L = SET(3, RM(EA) ); WM( EA,_L );					} /* SET  3,L=(XY+o)  */
    /*TODO*///OP(xxcb,de) { WM( EA, SET(3,RM(EA)) );								} /* SET  3,(XY+o)	  */
    /*TODO*///OP(xxcb,df) { _A = SET(3, RM(EA) ); WM( EA,_A );					} /* SET  3,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,e0) { _B = SET(4, RM(EA) ); WM( EA,_B );					} /* SET  4,B=(XY+o)  */
    /*TODO*///OP(xxcb,e1) { _C = SET(4, RM(EA) ); WM( EA,_C );					} /* SET  4,C=(XY+o)  */
    /*TODO*///OP(xxcb,e2) { _D = SET(4, RM(EA) ); WM( EA,_D );					} /* SET  4,D=(XY+o)  */
    /*TODO*///OP(xxcb,e3) { _E = SET(4, RM(EA) ); WM( EA,_E );					} /* SET  4,E=(XY+o)  */
    /*TODO*///OP(xxcb,e4) { _H = SET(4, RM(EA) ); WM( EA,_H );					} /* SET  4,H=(XY+o)  */
    /*TODO*///OP(xxcb,e5) { _L = SET(4, RM(EA) ); WM( EA,_L );					} /* SET  4,L=(XY+o)  */
    /*TODO*///OP(xxcb,e6) { WM( EA, SET(4,RM(EA)) );								} /* SET  4,(XY+o)	  */
    /*TODO*///OP(xxcb,e7) { _A = SET(4, RM(EA) ); WM( EA,_A );					} /* SET  4,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,e8) { _B = SET(5, RM(EA) ); WM( EA,_B );					} /* SET  5,B=(XY+o)  */
    /*TODO*///OP(xxcb,e9) { _C = SET(5, RM(EA) ); WM( EA,_C );					} /* SET  5,C=(XY+o)  */
    /*TODO*///OP(xxcb,ea) { _D = SET(5, RM(EA) ); WM( EA,_D );					} /* SET  5,D=(XY+o)  */
    /*TODO*///OP(xxcb,eb) { _E = SET(5, RM(EA) ); WM( EA,_E );					} /* SET  5,E=(XY+o)  */
    /*TODO*///OP(xxcb,ec) { _H = SET(5, RM(EA) ); WM( EA,_H );					} /* SET  5,H=(XY+o)  */
    /*TODO*///OP(xxcb,ed) { _L = SET(5, RM(EA) ); WM( EA,_L );					} /* SET  5,L=(XY+o)  */
    /*TODO*///OP(xxcb,ee) { WM( EA, SET(5,RM(EA)) );								} /* SET  5,(XY+o)	  */
    /*TODO*///OP(xxcb,ef) { _A = SET(5, RM(EA) ); WM( EA,_A );					} /* SET  5,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,f0) { _B = SET(6, RM(EA) ); WM( EA,_B );					} /* SET  6,B=(XY+o)  */
    /*TODO*///OP(xxcb,f1) { _C = SET(6, RM(EA) ); WM( EA,_C );					} /* SET  6,C=(XY+o)  */
    /*TODO*///OP(xxcb,f2) { _D = SET(6, RM(EA) ); WM( EA,_D );					} /* SET  6,D=(XY+o)  */
    /*TODO*///OP(xxcb,f3) { _E = SET(6, RM(EA) ); WM( EA,_E );					} /* SET  6,E=(XY+o)  */
    /*TODO*///OP(xxcb,f4) { _H = SET(6, RM(EA) ); WM( EA,_H );					} /* SET  6,H=(XY+o)  */
    /*TODO*///OP(xxcb,f5) { _L = SET(6, RM(EA) ); WM( EA,_L );					} /* SET  6,L=(XY+o)  */
    /*TODO*///OP(xxcb,f6) { WM( EA, SET(6,RM(EA)) );								} /* SET  6,(XY+o)	  */
    /*TODO*///OP(xxcb,f7) { _A = SET(6, RM(EA) ); WM( EA,_A );					} /* SET  6,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(xxcb,f8) { _B = SET(7, RM(EA) ); WM( EA,_B );					} /* SET  7,B=(XY+o)  */
    /*TODO*///OP(xxcb,f9) { _C = SET(7, RM(EA) ); WM( EA,_C );					} /* SET  7,C=(XY+o)  */
    /*TODO*///OP(xxcb,fa) { _D = SET(7, RM(EA) ); WM( EA,_D );					} /* SET  7,D=(XY+o)  */
    /*TODO*///OP(xxcb,fb) { _E = SET(7, RM(EA) ); WM( EA,_E );					} /* SET  7,E=(XY+o)  */
    /*TODO*///OP(xxcb,fc) { _H = SET(7, RM(EA) ); WM( EA,_H );					} /* SET  7,H=(XY+o)  */
    /*TODO*///OP(xxcb,fd) { _L = SET(7, RM(EA) ); WM( EA,_L );					} /* SET  7,L=(XY+o)  */
    /*TODO*///OP(xxcb,fe) { WM( EA, SET(7,RM(EA)) );								} /* SET  7,(XY+o)	  */
    /*TODO*///OP(xxcb,ff) { _A = SET(7, RM(EA) ); WM( EA,_A );					} /* SET  7,A=(XY+o)  */
    /*TODO*///
    /*TODO*///OP(illegal,1) {
    /*TODO*///	_PC--;
    /*TODO*///	if( errorlog )
    /*TODO*///		fprintf(errorlog, "Z80#%d ill. opcode $%02x $%02x\n",
    /*TODO*///			cpu_getactivecpu(), cpu_readop((_PCD-1)&0xffff), cpu_readop(_PCD));
    /*TODO*///}
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*/// * IX register related opcodes (DD prefix)
    /*TODO*/// **********************************************************/
    /*TODO*///OP(dd,00) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,01) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,02) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,03) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,04) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,05) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,06) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,07) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,08) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,09) { ADD16(IX,BC);											} /* ADD  IX,BC 	  */
    /*TODO*///OP(dd,0a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,0b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,0c) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,0d) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,0e) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,0f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,10) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,11) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,12) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,13) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,14) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,15) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,16) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,17) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,18) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,19) { ADD16(IX,DE);											} /* ADD  IX,DE 	  */
    /*TODO*///OP(dd,1a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1c) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1d) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1e) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,20) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,21) { _IX = ARG16();											} /* LD   IX,w		  */
    /*TODO*///OP(dd,22) { EA = ARG16(); WM16( EA, &Z80.IX );						} /* LD   (w),IX	  */
    /*TODO*///OP(dd,23) { _IX++;													} /* INC  IX		  */
    /*TODO*///OP(dd,24) { _HX = INC(_HX); 										} /* INC  HX		  */
    /*TODO*///OP(dd,25) { _HX = DEC(_HX); 										} /* DEC  HX		  */
    /*TODO*///OP(dd,26) { _HX = ARG();											} /* LD   HX,n		  */
    /*TODO*///OP(dd,27) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,28) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,29) { ADD16(IX,IX);											} /* ADD  IX,IX 	  */
    /*TODO*///OP(dd,2a) { EA = ARG16(); RM16( EA, &Z80.IX );						} /* LD   IX,(w)	  */
    /*TODO*///OP(dd,2b) { _IX--;													} /* DEC  IX		  */
    /*TODO*///OP(dd,2c) { _LX = INC(_LX); 										} /* INC  LX		  */
    /*TODO*///OP(dd,2d) { _LX = DEC(_LX); 										} /* DEC  LX		  */
    /*TODO*///OP(dd,2e) { _LX = ARG();											} /* LD   LX,n		  */
    /*TODO*///OP(dd,2f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,30) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,31) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,32) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,33) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,34) { EAX; WM( EA, INC(RM(EA)) ); 							} /* INC  (IX+o)	  */
    /*TODO*///OP(dd,35) { EAX; WM( EA, DEC(RM(EA)) ); 							} /* DEC  (IX+o)	  */
    /*TODO*///OP(dd,36) { EAX; WM( EA, ARG() );									} /* LD   (IX+o),n	  */
    /*TODO*///OP(dd,37) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,38) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,39) { ADD16(IX,SP);											} /* ADD  IX,SP 	  */
    /*TODO*///OP(dd,3a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,3b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,3c) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,3d) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,3e) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,3f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,40) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,41) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,42) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,43) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,44) { _B = _HX;												} /* LD   B,HX		  */
    /*TODO*///OP(dd,45) { _B = _LX;												} /* LD   B,LX		  */
    /*TODO*///OP(dd,46) { EAX; _B = RM(EA);										} /* LD   B,(IX+o)	  */
    /*TODO*///OP(dd,47) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,48) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,49) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,4a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,4b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,4c) { _C = _HX;												} /* LD   C,HX		  */
    /*TODO*///OP(dd,4d) { _C = _LX;												} /* LD   C,LX		  */
    /*TODO*///OP(dd,4e) { EAX; _C = RM(EA);										} /* LD   C,(IX+o)	  */
    /*TODO*///OP(dd,4f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,50) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,51) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,52) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,53) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,54) { _D = _HX;												} /* LD   D,HX		  */
    /*TODO*///OP(dd,55) { _D = _LX;												} /* LD   D,LX		  */
    /*TODO*///OP(dd,56) { EAX; _D = RM(EA);										} /* LD   D,(IX+o)	  */
    /*TODO*///OP(dd,57) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,58) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,59) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,5a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,5b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,5c) { _E = _HX;												} /* LD   E,HX		  */
    /*TODO*///OP(dd,5d) { _E = _LX;												} /* LD   E,LX		  */
    /*TODO*///OP(dd,5e) { EAX; _E = RM(EA);										} /* LD   E,(IX+o)	  */
    /*TODO*///OP(dd,5f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,60) { _HX = _B;												} /* LD   HX,B		  */
    /*TODO*///OP(dd,61) { _HX = _C;												} /* LD   HX,C		  */
    /*TODO*///OP(dd,62) { _HX = _D;												} /* LD   HX,D		  */
    /*TODO*///OP(dd,63) { _HX = _E;												} /* LD   HX,E		  */
    /*TODO*///OP(dd,64) { 														} /* LD   HX,HX 	  */
    /*TODO*///OP(dd,65) { _HX = _LX;												} /* LD   HX,LX 	  */
    /*TODO*///OP(dd,66) { EAX; _H = RM(EA);										} /* LD   H,(IX+o)	  */
    /*TODO*///OP(dd,67) { _HX = _A;												} /* LD   HX,A		  */
    /*TODO*///
    /*TODO*///OP(dd,68) { _LX = _B;												} /* LD   LX,B		  */
    /*TODO*///OP(dd,69) { _LX = _C;												} /* LD   LX,C		  */
    /*TODO*///OP(dd,6a) { _LX = _D;												} /* LD   LX,D		  */
    /*TODO*///OP(dd,6b) { _LX = _E;												} /* LD   LX,E		  */
    /*TODO*///OP(dd,6c) { _LX = _HX;												} /* LD   LX,HX 	  */
    /*TODO*///OP(dd,6d) { 														} /* LD   LX,LX 	  */
    /*TODO*///OP(dd,6e) { EAX; _L = RM(EA);										} /* LD   L,(IX+o)	  */
    /*TODO*///OP(dd,6f) { _LX = _A;												} /* LD   LX,A		  */
    /*TODO*///
    /*TODO*///OP(dd,70) { EAX; WM( EA, _B );										} /* LD   (IX+o),B	  */
    /*TODO*///OP(dd,71) { EAX; WM( EA, _C );										} /* LD   (IX+o),C	  */
    /*TODO*///OP(dd,72) { EAX; WM( EA, _D );										} /* LD   (IX+o),D	  */
    /*TODO*///OP(dd,73) { EAX; WM( EA, _E );										} /* LD   (IX+o),E	  */
    /*TODO*///OP(dd,74) { EAX; WM( EA, _H );										} /* LD   (IX+o),H	  */
    /*TODO*///OP(dd,75) { EAX; WM( EA, _L );										} /* LD   (IX+o),L	  */
    /*TODO*///OP(dd,76) { illegal_1();											}		  /* DB   DD		  */
    /*TODO*///OP(dd,77) { EAX; WM( EA, _A );										} /* LD   (IX+o),A	  */
    /*TODO*///
    /*TODO*///OP(dd,78) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,79) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7c) { _A = _HX;												} /* LD   A,HX		  */
    /*TODO*///OP(dd,7d) { _A = _LX;												} /* LD   A,LX		  */
    /*TODO*///OP(dd,7e) { EAX; _A = RM(EA);										} /* LD   A,(IX+o)	  */
    /*TODO*///OP(dd,7f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,80) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,81) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,82) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,83) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,84) { ADD(_HX);												} /* ADD  A,HX		  */
    /*TODO*///OP(dd,85) { ADD(_LX);												} /* ADD  A,LX		  */
    /*TODO*///OP(dd,86) { EAX; ADD(RM(EA));										} /* ADD  A,(IX+o)	  */
    /*TODO*///OP(dd,87) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,88) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,89) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,8a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,8b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,8c) { ADC(_HX);												} /* ADC  A,HX		  */
    /*TODO*///OP(dd,8d) { ADC(_LX);												} /* ADC  A,LX		  */
    /*TODO*///OP(dd,8e) { EAX; ADC(RM(EA));										} /* ADC  A,(IX+o)	  */
    /*TODO*///OP(dd,8f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,90) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,91) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,92) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,93) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,94) { SUB(_HX);												} /* SUB  HX		  */
    /*TODO*///OP(dd,95) { SUB(_LX);												} /* SUB  LX		  */
    /*TODO*///OP(dd,96) { EAX; SUB(RM(EA));										} /* SUB  (IX+o)	  */
    /*TODO*///OP(dd,97) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,98) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,99) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,9a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,9b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,9c) { SBC(_HX);												} /* SBC  A,HX		  */
    /*TODO*///OP(dd,9d) { SBC(_LX);												} /* SBC  A,LX		  */
    /*TODO*///OP(dd,9e) { EAX; SBC(RM(EA));										} /* SBC  A,(IX+o)	  */
    /*TODO*///OP(dd,9f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,a0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,a1) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,a2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,a3) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,a4) { AND(_HX);												} /* AND  HX		  */
    /*TODO*///OP(dd,a5) { AND(_LX);												} /* AND  LX		  */
    /*TODO*///OP(dd,a6) { EAX; AND(RM(EA));										} /* AND  (IX+o)	  */
    /*TODO*///OP(dd,a7) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,a8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,a9) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,aa) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ab) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ac) { XOR(_HX);												} /* XOR  HX		  */
    /*TODO*///OP(dd,ad) { XOR(_LX);												} /* XOR  LX		  */
    /*TODO*///OP(dd,ae) { EAX; XOR(RM(EA));										} /* XOR  (IX+o)	  */
    /*TODO*///OP(dd,af) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,b0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,b1) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,b2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,b3) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,b4) { OR(_HX);												} /* OR   HX		  */
    /*TODO*///OP(dd,b5) { OR(_LX);												} /* OR   LX		  */
    /*TODO*///OP(dd,b6) { EAX; OR(RM(EA));										} /* OR   (IX+o)	  */
    /*TODO*///OP(dd,b7) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,b8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,b9) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ba) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,bb) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,bc) { CP(_HX);												} /* CP   HX		  */
    /*TODO*///OP(dd,bd) { CP(_LX);												} /* CP   LX		  */
    /*TODO*///OP(dd,be) { EAX; CP(RM(EA));										} /* CP   (IX+o)	  */
    /*TODO*///OP(dd,bf) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,c0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c1) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c3) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c4) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c5) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c6) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c7) { illegal_1();											}		  /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,c8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,c9) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ca) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,cb) { EAX; EXEC(xxcb,ARG());									} /* **   DD CB xx	  */
    /*TODO*///OP(dd,cc) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,cd) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ce) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,cf) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,d0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d1) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d3) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d4) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d5) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d6) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d7) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,d8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,d9) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,da) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,db) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,dc) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,dd) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,de) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,df) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,e0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e1) { POP(IX);												} /* POP  IX		  */
    /*TODO*///OP(dd,e2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e3) { EXSP(IX);												} /* EX   (SP),IX	  */
    /*TODO*///OP(dd,e4) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e5) { PUSH( IX ); 											} /* PUSH IX		  */
    /*TODO*///OP(dd,e6) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e7) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,e8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e9) { _PC = _IX; change_pc16(_PCD);							} /* JP   (IX)		  */
    /*TODO*///OP(dd,ea) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,eb) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ec) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ed) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ee) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ef) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,f0) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f1) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f3) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f4) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f5) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f6) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f7) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,f8) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,f9) { _SP = _IX;												} /* LD   SP,IX 	  */
    /*TODO*///OP(dd,fa) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,fb) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,fc) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,fd) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,fe) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,ff) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*/// * IY register related opcodes (FD prefix)
    /*TODO*/// **********************************************************/
    /*TODO*///OP(fd,00) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,01) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,02) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,03) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,04) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,05) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,06) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,07) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,08) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,09) { ADD16(IY,BC);											} /* ADD  IY,BC 	  */
    /*TODO*///OP(fd,0a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,0b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,0c) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,0d) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,0e) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,0f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,10) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,11) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,12) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,13) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,14) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,15) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,16) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,17) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,18) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,19) { ADD16(IY,DE);											} /* ADD  IY,DE 	  */
    /*TODO*///OP(fd,1a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,1b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,1c) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,1d) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,1e) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,1f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,20) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,21) { _IY = ARG16();											} /* LD   IY,w		  */
    /*TODO*///OP(fd,22) { EA = ARG16(); WM16( EA, &Z80.IY );						} /* LD   (w),IY	  */
    /*TODO*///OP(fd,23) { _IY++;													} /* INC  IY		  */
    /*TODO*///OP(fd,24) { _HY = INC(_HY); 										} /* INC  HY		  */
    /*TODO*///OP(fd,25) { _HY = DEC(_HY); 										} /* DEC  HY		  */
    /*TODO*///OP(fd,26) { _HY = ARG();											} /* LD   HY,n		  */
    /*TODO*///OP(fd,27) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,28) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,29) { ADD16(IY,IY);											} /* ADD  IY,IY 	  */
    /*TODO*///OP(fd,2a) { EA = ARG16(); RM16( EA, &Z80.IY );						} /* LD   IY,(w)	  */
    /*TODO*///OP(fd,2b) { _IY--;													} /* DEC  IY		  */
    /*TODO*///OP(fd,2c) { _LY = INC(_LY); 										} /* INC  LY		  */
    /*TODO*///OP(fd,2d) { _LY = DEC(_LY); 										} /* DEC  LY		  */
    /*TODO*///OP(fd,2e) { _LY = ARG();											} /* LD   LY,n		  */
    /*TODO*///OP(fd,2f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,30) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,31) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,32) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,33) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,34) { EAY; WM( EA, INC(RM(EA)) ); 							} /* INC  (IY+o)	  */
    /*TODO*///OP(fd,35) { EAY; WM( EA, DEC(RM(EA)) ); 							} /* DEC  (IY+o)	  */
    /*TODO*///OP(fd,36) { EAY; WM( EA, ARG() );									} /* LD   (IY+o),n	  */
    /*TODO*///OP(fd,37) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,38) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,39) { ADD16(IY,SP);											} /* ADD  IY,SP 	  */
    /*TODO*///OP(fd,3a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,3b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,3c) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,3d) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,3e) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,3f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,40) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,41) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,42) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,43) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,44) { _B = _HY;												} /* LD   B,HY		  */
    /*TODO*///OP(fd,45) { _B = _LY;												} /* LD   B,LY		  */
    /*TODO*///OP(fd,46) { EAY; _B = RM(EA);										} /* LD   B,(IY+o)	  */
    /*TODO*///OP(fd,47) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,48) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,49) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,4a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,4b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,4c) { _C = _HY;												} /* LD   C,HY		  */
    /*TODO*///OP(fd,4d) { _C = _LY;												} /* LD   C,LY		  */
    /*TODO*///OP(fd,4e) { EAY; _C = RM(EA);										} /* LD   C,(IY+o)	  */
    /*TODO*///OP(fd,4f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,50) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,51) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,52) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,53) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,54) { _D = _HY;												} /* LD   D,HY		  */
    /*TODO*///OP(fd,55) { _D = _LY;												} /* LD   D,LY		  */
    /*TODO*///OP(fd,56) { EAY; _D = RM(EA);										} /* LD   D,(IY+o)	  */
    /*TODO*///OP(fd,57) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,58) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,59) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,5a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,5b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,5c) { _E = _HY;												} /* LD   E,HY		  */
    /*TODO*///OP(fd,5d) { _E = _LY;												} /* LD   E,LY		  */
    /*TODO*///OP(fd,5e) { EAY; _E = RM(EA);										} /* LD   E,(IY+o)	  */
    /*TODO*///OP(fd,5f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,60) { _HY = _B;												} /* LD   HY,B		  */
    /*TODO*///OP(fd,61) { _HY = _C;												} /* LD   HY,C		  */
    /*TODO*///OP(fd,62) { _HY = _D;												} /* LD   HY,D		  */
    /*TODO*///OP(fd,63) { _HY = _E;												} /* LD   HY,E		  */
    /*TODO*///OP(fd,64) { 														} /* LD   HY,HY 	  */
    /*TODO*///OP(fd,65) { _HY = _LY;												} /* LD   HY,LY 	  */
    /*TODO*///OP(fd,66) { EAY; _H = RM(EA);										} /* LD   H,(IY+o)	  */
    /*TODO*///OP(fd,67) { _HY = _A;												} /* LD   HY,A		  */
    /*TODO*///
    /*TODO*///OP(fd,68) { _LY = _B;												} /* LD   LY,B		  */
    /*TODO*///OP(fd,69) { _LY = _C;												} /* LD   LY,C		  */
    /*TODO*///OP(fd,6a) { _LY = _D;												} /* LD   LY,D		  */
    /*TODO*///OP(fd,6b) { _LY = _E;												} /* LD   LY,E		  */
    /*TODO*///OP(fd,6c) { _LY = _HY;												} /* LD   LY,HY 	  */
    /*TODO*///OP(fd,6d) { 														} /* LD   LY,LY 	  */
    /*TODO*///OP(fd,6e) { EAY; _L = RM(EA);										} /* LD   L,(IY+o)	  */
    /*TODO*///OP(fd,6f) { _LY = _A;												} /* LD   LY,A		  */
    /*TODO*///
    /*TODO*///OP(fd,70) { EAY; WM( EA, _B );										} /* LD   (IY+o),B	  */
    /*TODO*///OP(fd,71) { EAY; WM( EA, _C );										} /* LD   (IY+o),C	  */
    /*TODO*///OP(fd,72) { EAY; WM( EA, _D );										} /* LD   (IY+o),D	  */
    /*TODO*///OP(fd,73) { EAY; WM( EA, _E );										} /* LD   (IY+o),E	  */
    /*TODO*///OP(fd,74) { EAY; WM( EA, _H );										} /* LD   (IY+o),H	  */
    /*TODO*///OP(fd,75) { EAY; WM( EA, _L );										} /* LD   (IY+o),L	  */
    /*TODO*///OP(fd,76) { illegal_1();											}		  /* DB   FD		  */
    /*TODO*///OP(fd,77) { EAY; WM( EA, _A );										} /* LD   (IY+o),A	  */
    /*TODO*///
    /*TODO*///OP(fd,78) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,79) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,7a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,7b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,7c) { _A = _HY;												} /* LD   A,HY		  */
    /*TODO*///OP(fd,7d) { _A = _LY;												} /* LD   A,LY		  */
    /*TODO*///OP(fd,7e) { EAY; _A = RM(EA);										} /* LD   A,(IY+o)	  */
    /*TODO*///OP(fd,7f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,80) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,81) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,82) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,83) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,84) { ADD(_HY);												} /* ADD  A,HY		  */
    /*TODO*///OP(fd,85) { ADD(_LY);												} /* ADD  A,LY		  */
    /*TODO*///OP(fd,86) { EAY; ADD(RM(EA));										} /* ADD  A,(IY+o)	  */
    /*TODO*///OP(fd,87) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,88) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,89) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,8a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,8b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,8c) { ADC(_HY);												} /* ADC  A,HY		  */
    /*TODO*///OP(fd,8d) { ADC(_LY);												} /* ADC  A,LY		  */
    /*TODO*///OP(fd,8e) { EAY; ADC(RM(EA));										} /* ADC  A,(IY+o)	  */
    /*TODO*///OP(fd,8f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,90) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,91) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,92) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,93) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,94) { SUB(_HY);												} /* SUB  HY		  */
    /*TODO*///OP(fd,95) { SUB(_LY);												} /* SUB  LY		  */
    /*TODO*///OP(fd,96) { EAY; SUB(RM(EA));										} /* SUB  (IY+o)	  */
    /*TODO*///OP(fd,97) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,98) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,99) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,9a) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,9b) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,9c) { SBC(_HY);												} /* SBC  A,HY		  */
    /*TODO*///OP(fd,9d) { SBC(_LY);												} /* SBC  A,LY		  */
    /*TODO*///OP(fd,9e) { EAY; SBC(RM(EA));										} /* SBC  A,(IY+o)	  */
    /*TODO*///OP(fd,9f) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,a0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,a1) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,a2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,a3) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,a4) { AND(_HY);												} /* AND  HY		  */
    /*TODO*///OP(fd,a5) { AND(_LY);												} /* AND  LY		  */
    /*TODO*///OP(fd,a6) { EAY; AND(RM(EA));										} /* AND  (IY+o)	  */
    /*TODO*///OP(fd,a7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,a8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,a9) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,aa) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ab) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ac) { XOR(_HY);												} /* XOR  HY		  */
    /*TODO*///OP(fd,ad) { XOR(_LY);												} /* XOR  LY		  */
    /*TODO*///OP(fd,ae) { EAY; XOR(RM(EA));										} /* XOR  (IY+o)	  */
    /*TODO*///OP(fd,af) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,b0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,b1) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,b2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,b3) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,b4) { OR(_HY);												} /* OR   HY		  */
    /*TODO*///OP(fd,b5) { OR(_LY);												} /* OR   LY		  */
    /*TODO*///OP(fd,b6) { EAY; OR(RM(EA));										} /* OR   (IY+o)	  */
    /*TODO*///OP(fd,b7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,b8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,b9) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ba) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,bb) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,bc) { CP(_HY);												} /* CP   HY		  */
    /*TODO*///OP(fd,bd) { CP(_LY);												} /* CP   LY		  */
    /*TODO*///OP(fd,be) { EAY; CP(RM(EA));										} /* CP   (IY+o)	  */
    /*TODO*///OP(fd,bf) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,c0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c1) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c3) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c4) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c5) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c6) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,c8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,c9) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ca) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,cb) { EAY; EXEC(xxcb,ARG());									} /* **   FD CB xx	  */
    /*TODO*///OP(fd,cc) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,cd) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ce) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,cf) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,d0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d1) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d3) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d4) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d5) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d6) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,d8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,d9) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,da) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,db) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,dc) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,dd) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,de) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,df) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,e0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e1) { POP(IY);												} /* POP  IY		  */
    /*TODO*///OP(fd,e2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e3) { EXSP(IY);												} /* EX   (SP),IY	  */
    /*TODO*///OP(fd,e4) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e5) { PUSH( IY ); 											} /* PUSH IY		  */
    /*TODO*///OP(fd,e6) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,e8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e9) { _PC = _IY; change_pc16(_PCD);							} /* JP   (IY)		  */
    /*TODO*///OP(fd,ea) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,eb) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ec) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ed) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ee) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ef) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,f0) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f1) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f3) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f4) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f5) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f6) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f7) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(fd,f8) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,f9) { _SP = _IY;												} /* LD   SP,IY 	  */
    /*TODO*///OP(fd,fa) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,fb) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,fc) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,fd) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,fe) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,ff) { illegal_1();											} /* DB   FD		  */
    /*TODO*///
    /*TODO*///OP(illegal,2)
    /*TODO*///{
    /*TODO*///	if( errorlog )
    /*TODO*///		fprintf(errorlog, "Z80#%d ill. opcode $ed $%02x\n",
    /*TODO*///			cpu_getactivecpu(), cpu_readop((_PCD-1)&0xffff));
    /*TODO*///}
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*/// * special opcodes (ED prefix)
    /*TODO*/// **********************************************************/
    /*TODO*///OP(ed,00) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,01) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,02) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,03) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,04) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,05) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,06) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,07) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,08) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,09) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,0f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,10) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,11) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,12) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,13) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,14) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,15) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,16) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,17) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,18) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,19) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,1f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,20) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,21) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,22) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,23) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,24) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,25) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,26) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,27) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,28) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,29) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,2f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,30) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,31) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,32) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,33) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,34) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,35) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,36) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,37) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,38) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,39) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,3f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,40) { _B = IN(_BC); _F = (_F & CF) | SZP[_B]; 				} /* IN   B,(C) 	  */
    /*TODO*///OP(ed,41) { OUT(_BC,_B);											} /* OUT  (C),B 	  */
    /*TODO*///OP(ed,42) { SBC16( BC );											} /* SBC  HL,BC 	  */
    /*TODO*///OP(ed,43) { EA = ARG16(); WM16( EA, &Z80.BC );						} /* LD   (w),BC	  */
    /*TODO*///OP(ed,44) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,45) { RETN;													} /* RETN;			  */
    /*TODO*///OP(ed,46) { _IM = 0;												} /* IM   0 		  */
    /*TODO*///OP(ed,47) { LD_I_A; 												} /* LD   I,A		  */
    /*TODO*///
    /*TODO*///OP(ed,48) { _C = IN(_BC); _F = (_F & CF) | SZP[_C]; 				} /* IN   C,(C) 	  */
    /*TODO*///OP(ed,49) { OUT(_BC,_C);											} /* OUT  (C),C 	  */
    /*TODO*///OP(ed,4a) { ADC16( BC );											} /* ADC  HL,BC 	  */
    /*TODO*///OP(ed,4b) { EA = ARG16(); RM16( EA, &Z80.BC );						} /* LD   BC,(w)	  */
    /*TODO*///OP(ed,4c) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,4d) { RETI;													} /* RETI			  */
    /*TODO*///OP(ed,4e) { _IM = 0;												} /* IM   0 		  */
    /*TODO*///OP(ed,4f) { LD_R_A; 												} /* LD   R,A		  */
    /*TODO*///
    /*TODO*///OP(ed,50) { _D = IN(_BC); _F = (_F & CF) | SZP[_D]; 				} /* IN   D,(C) 	  */
    /*TODO*///OP(ed,51) { OUT(_BC,_D);											} /* OUT  (C),D 	  */
    /*TODO*///OP(ed,52) { SBC16( DE );											} /* SBC  HL,DE 	  */
    /*TODO*///OP(ed,53) { EA = ARG16(); WM16( EA, &Z80.DE );						} /* LD   (w),DE	  */
    /*TODO*///OP(ed,54) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,55) { RETN;													} /* RETN;			  */
    /*TODO*///OP(ed,56) { _IM = 1;												} /* IM   1 		  */
    /*TODO*///OP(ed,57) { LD_A_I; 												} /* LD   A,I		  */
    /*TODO*///
    /*TODO*///OP(ed,58) { _E = IN(_BC); _F = (_F & CF) | SZP[_E]; 				} /* IN   E,(C) 	  */
    /*TODO*///OP(ed,59) { OUT(_BC,_E);											} /* OUT  (C),E 	  */
    /*TODO*///OP(ed,5a) { ADC16( DE );											} /* ADC  HL,DE 	  */
    /*TODO*///OP(ed,5b) { EA = ARG16(); RM16( EA, &Z80.DE );						} /* LD   DE,(w)	  */
    /*TODO*///OP(ed,5c) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,5d) { RETI;													} /* RETI			  */
    /*TODO*///OP(ed,5e) { _IM = 2;												} /* IM   2 		  */
    /*TODO*///OP(ed,5f) { LD_A_R; 												} /* LD   A,R		  */
    /*TODO*///
    /*TODO*///OP(ed,60) { _H = IN(_BC); _F = (_F & CF) | SZP[_H]; 				} /* IN   H,(C) 	  */
    /*TODO*///OP(ed,61) { OUT(_BC,_H);											} /* OUT  (C),H 	  */
    /*TODO*///OP(ed,62) { SBC16( HL );											} /* SBC  HL,HL 	  */
    /*TODO*///OP(ed,63) { EA = ARG16(); WM16( EA, &Z80.HL );						} /* LD   (w),HL	  */
    /*TODO*///OP(ed,64) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,65) { RETN;													} /* RETN;			  */
    /*TODO*///OP(ed,66) { _IM = 0;												} /* IM   0 		  */
    /*TODO*///OP(ed,67) { RRD;													} /* RRD  (HL)		  */
    /*TODO*///
    /*TODO*///OP(ed,68) { _L = IN(_BC); _F = (_F & CF) | SZP[_L]; 				} /* IN   L,(C) 	  */
    /*TODO*///OP(ed,69) { OUT(_BC,_L);											} /* OUT  (C),L 	  */
    /*TODO*///OP(ed,6a) { ADC16( HL );											} /* ADC  HL,HL 	  */
    /*TODO*///OP(ed,6b) { EA = ARG16(); RM16( EA, &Z80.HL );						} /* LD   HL,(w)	  */
    /*TODO*///OP(ed,6c) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,6d) { RETI;													} /* RETI			  */
    /*TODO*///OP(ed,6e) { _IM = 0;												} /* IM   0 		  */
    /*TODO*///OP(ed,6f) { RLD;													} /* RLD  (HL)		  */
    /*TODO*///
    /*TODO*///OP(ed,70) { UINT8 res = IN(_BC); _F = (_F & CF) | SZP[res]; 		} /* IN   0,(C) 	  */
    /*TODO*///OP(ed,71) { OUT(_BC,0); 											} /* OUT  (C),0 	  */
    /*TODO*///OP(ed,72) { SBC16( SP );											} /* SBC  HL,SP 	  */
    /*TODO*///OP(ed,73) { EA = ARG16(); WM16( EA, &Z80.SP );						} /* LD   (w),SP	  */
    /*TODO*///OP(ed,74) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,75) { RETN;													} /* RETN;			  */
    /*TODO*///OP(ed,76) { _IM = 1;												} /* IM   1 		  */
    /*TODO*///OP(ed,77) { illegal_2();											} /* DB   ED,77 	  */
    /*TODO*///
    /*TODO*///OP(ed,78) { _A = IN(_BC); _F = (_F & CF) | SZP[_A]; 				} /* IN   E,(C) 	  */
    /*TODO*///OP(ed,79) { OUT(_BC,_A);											} /* OUT  (C),E 	  */
    /*TODO*///OP(ed,7a) { ADC16( SP );											} /* ADC  HL,SP 	  */
    /*TODO*///OP(ed,7b) { EA = ARG16(); RM16( EA, &Z80.SP );						} /* LD   SP,(w)	  */
    /*TODO*///OP(ed,7c) { NEG;													} /* NEG			  */
    /*TODO*///OP(ed,7d) { RETI;													} /* RETI			  */
    /*TODO*///OP(ed,7e) { _IM = 2;												} /* IM   2 		  */
    /*TODO*///OP(ed,7f) { illegal_2();											} /* DB   ED,7F 	  */
    /*TODO*///
    /*TODO*///OP(ed,80) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,81) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,82) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,83) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,84) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,85) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,86) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,87) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,88) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,89) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,8f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,90) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,91) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,92) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,93) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,94) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,95) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,96) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,97) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,98) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,99) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9a) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9b) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9c) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9d) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9e) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,9f) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,a0) { LDI;													} /* LDI			  */
    /*TODO*///OP(ed,a1) { CPI;													} /* CPI			  */
    /*TODO*///OP(ed,a2) { INI;													} /* INI			  */
    /*TODO*///OP(ed,a3) { OUTI;													} /* OUTI			  */
    /*TODO*///OP(ed,a4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,a5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,a6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,a7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,a8) { LDD;													} /* LDD			  */
    /*TODO*///OP(ed,a9) { CPD;													} /* CPD			  */
    /*TODO*///OP(ed,aa) { IND;													} /* IND			  */
    /*TODO*///OP(ed,ab) { OUTD;													} /* OUTD			  */
    /*TODO*///OP(ed,ac) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ad) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ae) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,af) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,b0) { LDIR;													} /* LDIR			  */
    /*TODO*///OP(ed,b1) { CPIR;													} /* CPIR			  */
    /*TODO*///OP(ed,b2) { INIR;													} /* INIR			  */
    /*TODO*///OP(ed,b3) { OTIR;													} /* OTIR			  */
    /*TODO*///OP(ed,b4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,b5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,b6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,b7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,b8) { LDDR;													} /* LDDR			  */
    /*TODO*///OP(ed,b9) { CPDR;													} /* CPDR			  */
    /*TODO*///OP(ed,ba) { INDR;													} /* INDR			  */
    /*TODO*///OP(ed,bb) { OTDR;													} /* OTDR			  */
    /*TODO*///OP(ed,bc) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,bd) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,be) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,bf) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,c0) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c1) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c2) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c3) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,c8) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,c9) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ca) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,cb) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,cc) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,cd) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ce) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,cf) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,d0) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d1) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d2) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d3) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,d8) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,d9) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,da) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,db) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,dc) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,dd) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,de) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,df) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,e0) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e1) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e2) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e3) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,e8) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,e9) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ea) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,eb) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ec) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ed) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ee) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ef) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,f0) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f1) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f2) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f3) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f4) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f5) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f6) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f7) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///OP(ed,f8) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,f9) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,fa) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,fb) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,fc) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,fd) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,fe) { illegal_2();											} /* DB   ED		  */
    /*TODO*///OP(ed,ff) { illegal_2();											} /* DB   ED		  */
    /*TODO*///
    /*TODO*///#if TIME_LOOP_HACKS
    /*TODO*///
    /*TODO*///#define CHECK_BC_LOOP                                               \
    /*TODO*///if( _BC > 1 && _PCD < 0xfffc ) {									\
    /*TODO*///	UINT8 op1 = cpu_readop(_PCD);									\
    /*TODO*///	UINT8 op2 = cpu_readop(_PCD+1); 								\
    /*TODO*///	if( (op1==0x78 && op2==0xb1) || (op1==0x79 && op2==0xb0) )		\
    /*TODO*///	{																\
    /*TODO*///		UINT8 op3 = cpu_readop(_PCD+2); 							\
    /*TODO*///		UINT8 op4 = cpu_readop(_PCD+3); 							\
    /*TODO*///		if( op3==0x20 && op4==0xfb )								\
    /*TODO*///		{															\
    /*TODO*///			while( _BC > 0 && z80_ICount > 4+4+12+6 )				\
    /*TODO*///			{														\
    /*TODO*///				BURNODD( 4+4+12+6, 4, 4+4+12+6 );					\
    /*TODO*///				_BC--;												\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///		else														\
    /*TODO*///		if( op3 == 0xc2 )											\
    /*TODO*///		{															\
    /*TODO*///			UINT8 ad1 = cpu_readop_arg(_PCD+3); 					\
    /*TODO*///			UINT8 ad2 = cpu_readop_arg(_PCD+4); 					\
    /*TODO*///			if( (ad1 + 256 * ad2) == (_PCD - 1) )					\
    /*TODO*///			{														\
    /*TODO*///				while( _BC > 0 && z80_ICount > 4+4+10+6 )			\
    /*TODO*///				{													\
    /*TODO*///					BURNODD( 4+4+10+6, 4, 4+4+10+6 );				\
    /*TODO*///					_BC--;											\
    /*TODO*///				}													\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///	}																\
    /*TODO*///}
    /*TODO*///
    /*TODO*///#define CHECK_DE_LOOP                                               \
    /*TODO*///if( _DE > 1 && _PCD < 0xfffc ) {                                    \
    /*TODO*///	UINT8 op1 = cpu_readop(_PCD);									\
    /*TODO*///	UINT8 op2 = cpu_readop(_PCD+1); 								\
    /*TODO*///	if( (op1==0x7a && op2==0xb3) || (op1==0x7b && op2==0xb2) )		\
    /*TODO*///	{																\
    /*TODO*///		UINT8 op3 = cpu_readop(_PCD+2); 							\
    /*TODO*///		UINT8 op4 = cpu_readop(_PCD+3); 							\
    /*TODO*///		if( op3==0x20 && op4==0xfb )								\
    /*TODO*///		{															\
    /*TODO*///			while( _DE > 0 && z80_ICount > 4+4+12+6 )				\
    /*TODO*///			{														\
    /*TODO*///				BURNODD( 4+4+12+6, 4, 4+4+12+6 );					\
    /*TODO*///				_DE--;												\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///		else														\
    /*TODO*///		if( op3==0xc2 ) 											\
    /*TODO*///		{															\
    /*TODO*///			UINT8 ad1 = cpu_readop_arg(_PCD+3); 					\
    /*TODO*///			UINT8 ad2 = cpu_readop_arg(_PCD+4); 					\
    /*TODO*///			if( (ad1 + 256 * ad2) == (_PCD - 1) )					\
    /*TODO*///			{														\
    /*TODO*///				while( _DE > 0 && z80_ICount > 4+4+10+6 )			\
    /*TODO*///				{													\
    /*TODO*///					BURNODD( 4+4+10+6, 4, 4+4+10+6 );				\
    /*TODO*///					_DE--;											\
    /*TODO*///				}													\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///	}																\
    /*TODO*///}
    /*TODO*///
    /*TODO*///#define CHECK_HL_LOOP                                               \
    /*TODO*///if( _HL > 1 && _PCD < 0xfffc ) {                                    \
    /*TODO*///	UINT8 op1 = cpu_readop(_PCD);									\
    /*TODO*///	UINT8 op2 = cpu_readop(_PCD+1); 								\
    /*TODO*///	if( (op1==0x7c && op2==0xb5) || (op1==0x7d && op2==0xb4) )		\
    /*TODO*///	{																\
    /*TODO*///		UINT8 op3 = cpu_readop(_PCD+2); 							\
    /*TODO*///		UINT8 op4 = cpu_readop(_PCD+3); 							\
    /*TODO*///		if( op3==0x20 && op4==0xfb )								\
    /*TODO*///		{															\
    /*TODO*///			while( _HL > 0 && z80_ICount > 4+4+12+6 )				\
    /*TODO*///			{														\
    /*TODO*///				BURNODD( 4+4+12+6, 4, 4+4+12+6 );					\
    /*TODO*///				_HL--;												\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///		else														\
    /*TODO*///		if( op3==0xc2 ) 											\
    /*TODO*///		{															\
    /*TODO*///			UINT8 ad1 = cpu_readop_arg(_PCD+3); 					\
    /*TODO*///			UINT8 ad2 = cpu_readop_arg(_PCD+4); 					\
    /*TODO*///			if( (ad1 + 256 * ad2) == (_PCD - 1) )					\
    /*TODO*///			{														\
    /*TODO*///				while( _HL > 0 && z80_ICount > 4+4+10+6 )			\
    /*TODO*///				{													\
    /*TODO*///					BURNODD( 4+4+10+6, 4, 4+4+10+6 );				\
    /*TODO*///					_HL--;											\
    /*TODO*///				}													\
    /*TODO*///			}														\
    /*TODO*///		}															\
    /*TODO*///	}																\
    /*TODO*///}
    /*TODO*///
    /*TODO*///#else
    /*TODO*///
    /*TODO*///#define CHECK_BC_LOOP
    /*TODO*///#define CHECK_DE_LOOP
    /*TODO*///#define CHECK_HL_LOOP
    /*TODO*///
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////**********************************************************
    /*TODO*/// * main opcodes
    /*TODO*/// **********************************************************/
    /*TODO*///OP(op,00) { 														} /* NOP			  */
    /*TODO*///OP(op,01) { _BC = ARG16();											} /* LD   BC,w		  */
    /*TODO*///OP(op,02) { WM( _BC, _A );											} /* LD   (BC),A	  */
    /*TODO*///OP(op,03) { _BC++;													} /* INC  BC		  */
    /*TODO*///OP(op,04) { _B = INC(_B);											} /* INC  B 		  */
    /*TODO*///OP(op,05) { _B = DEC(_B);											} /* DEC  B 		  */
    /*TODO*///OP(op,06) { _B = ARG(); 											} /* LD   B,n		  */
    /*TODO*///OP(op,07) { RLCA;													} /* RLCA			  */
    /*TODO*///
    /*TODO*///OP(op,08) { EX_AF;													} /* EX   AF,AF'      */
    /*TODO*///OP(op,09) { ADD16(HL,BC);											} /* ADD  HL,BC 	  */
    /*TODO*///OP(op,0a) { _A = RM(_BC);											} /* LD   A,(BC)	  */
    /*TODO*///OP(op,0b) { _BC--; CHECK_BC_LOOP;									} /* DEC  BC		  */
    /*TODO*///OP(op,0c) { _C = INC(_C);											} /* INC  C 		  */
    /*TODO*///OP(op,0d) { _C = DEC(_C);											} /* DEC  C 		  */
    /*TODO*///OP(op,0e) { _C = ARG(); 											} /* LD   C,n		  */
    /*TODO*///OP(op,0f) { RRCA;													} /* RRCA			  */
    /*TODO*///
    /*TODO*///OP(op,10) { _B--; JR_COND(_B);										} /* DJNZ o 		  */
    /*TODO*///OP(op,11) { _DE = ARG16();											} /* LD   DE,w		  */
    /*TODO*///OP(op,12) { WM( _DE, _A );											} /* LD   (DE),A	  */
    /*TODO*///OP(op,13) { _DE++;													} /* INC  DE		  */
    /*TODO*///OP(op,14) { _D = INC(_D);											} /* INC  D 		  */
    /*TODO*///OP(op,15) { _D = DEC(_D);											} /* DEC  D 		  */
    /*TODO*///OP(op,16) { _D = ARG(); 											} /* LD   D,n		  */
    /*TODO*///OP(op,17) { RLA;													} /* RLA			  */
    /*TODO*///
    /*TODO*///OP(op,18) { JR();													} /* JR   o 		  */
    /*TODO*///OP(op,19) { ADD16(HL,DE);											} /* ADD  HL,DE 	  */
    /*TODO*///OP(op,1a) { _A = RM(_DE);											} /* LD   A,(DE)	  */
    /*TODO*///OP(op,1b) { _DE--; CHECK_DE_LOOP;									} /* DEC  DE		  */
    /*TODO*///OP(op,1c) { _E = INC(_E);											} /* INC  E 		  */
    /*TODO*///OP(op,1d) { _E = DEC(_E);											} /* DEC  E 		  */
    /*TODO*///OP(op,1e) { _E = ARG(); 											} /* LD   E,n		  */
    /*TODO*///OP(op,1f) { RRA;													} /* RRA			  */
    /*TODO*///
    /*TODO*///OP(op,20) { JR_COND( !(_F & ZF) );									} /* JR   NZ,o		  */
    /*TODO*///OP(op,21) { _HL = ARG16();											} /* LD   HL,w		  */
    /*TODO*///OP(op,22) { EA = ARG16(); WM16( EA, &Z80.HL );						} /* LD   (w),HL	  */
    /*TODO*///OP(op,23) { _HL++;													} /* INC  HL		  */
    /*TODO*///OP(op,24) { _H = INC(_H);											} /* INC  H 		  */
    /*TODO*///OP(op,25) { _H = DEC(_H);											} /* DEC  H 		  */
    /*TODO*///OP(op,26) { _H = ARG(); 											} /* LD   H,n		  */
    /*TODO*///OP(op,27) { DAA;													} /* DAA			  */
    /*TODO*///
    /*TODO*///OP(op,28) { JR_COND( _F & ZF ); 									} /* JR   Z,o		  */
    /*TODO*///OP(op,29) { ADD16(HL,HL);											} /* ADD  HL,HL 	  */
    /*TODO*///OP(op,2a) { EA = ARG16(); RM16( EA, &Z80.HL );						} /* LD   HL,(w)	  */
    /*TODO*///OP(op,2b) { _HL--; CHECK_HL_LOOP;									} /* DEC  HL		  */
    /*TODO*///OP(op,2c) { _L = INC(_L);											} /* INC  L 		  */
    /*TODO*///OP(op,2d) { _L = DEC(_L);											} /* DEC  L 		  */
    /*TODO*///OP(op,2e) { _L = ARG(); 											} /* LD   L,n		  */
    /*TODO*///OP(op,2f) { _A ^= 0xff; _F = (_F&(SF|ZF|PF|CF))|HF|NF|(_A&(YF|XF)); } /* CPL			  */
    /*TODO*///
    /*TODO*///OP(op,30) { JR_COND( !(_F & CF) );									} /* JR   NC,o		  */
    /*TODO*///OP(op,31) { _SP = ARG16();											} /* LD   SP,w		  */
    /*TODO*///OP(op,32) { EA = ARG16(); WM( EA, _A ); 							} /* LD   (w),A 	  */
    /*TODO*///OP(op,33) { _SP++;													} /* INC  SP		  */
    /*TODO*///OP(op,34) { WM( _HL, INC(RM(_HL)) );								} /* INC  (HL)		  */
    /*TODO*///OP(op,35) { WM( _HL, DEC(RM(_HL)) );								} /* DEC  (HL)		  */
    /*TODO*///OP(op,36) { WM( _HL, ARG() );										} /* LD   (HL),n	  */
    /*TODO*///OP(op,37) { _F = (_F & (SF|ZF|PF)) | CF | (_A & (YF|XF));			} /* SCF			  */
    /*TODO*///
    /*TODO*///OP(op,38) { JR_COND( _F & CF ); 									} /* JR   C,o		  */
    /*TODO*///OP(op,39) { ADD16(HL,SP);											} /* ADD  HL,SP 	  */
    /*TODO*///OP(op,3a) { EA = ARG16(); _A = RM( EA );							} /* LD   A,(w) 	  */
    /*TODO*///OP(op,3b) { _SP--;													} /* DEC  SP		  */
    /*TODO*///OP(op,3c) { _A = INC(_A);											} /* INC  A 		  */
    /*TODO*///OP(op,3d) { _A = DEC(_A);											} /* DEC  A 		  */
    /*TODO*///OP(op,3e) { _A = ARG(); 											} /* LD   A,n		  */
    /*TODO*///OP(op,3f) { _F = ((_F&(SF|ZF|PF|CF))|((_F&CF)<<4)|(_A&(YF|XF)))^CF; } /* CCF			  */
    /*TODO*/////OP(op,3f) { _F = ((_F & ~(HF|NF)) | ((_F & CF)<<4)) ^ CF; 		  } /* CCF				*/
    /*TODO*///
    /*TODO*///OP(op,40) { 														} /* LD   B,B		  */
    /*TODO*///OP(op,41) { _B = _C;												} /* LD   B,C		  */
    /*TODO*///OP(op,42) { _B = _D;												} /* LD   B,D		  */
    /*TODO*///OP(op,43) { _B = _E;												} /* LD   B,E		  */
    /*TODO*///OP(op,44) { _B = _H;												} /* LD   B,H		  */
    /*TODO*///OP(op,45) { _B = _L;												} /* LD   B,L		  */
    /*TODO*///OP(op,46) { _B = RM(_HL);											} /* LD   B,(HL)	  */
    /*TODO*///OP(op,47) { _B = _A;												} /* LD   B,A		  */
    /*TODO*///
    /*TODO*///OP(op,48) { _C = _B;												} /* LD   C,B		  */
    /*TODO*///OP(op,49) { 														} /* LD   C,C		  */
    /*TODO*///OP(op,4a) { _C = _D;												} /* LD   C,D		  */
    /*TODO*///OP(op,4b) { _C = _E;												} /* LD   C,E		  */
    /*TODO*///OP(op,4c) { _C = _H;												} /* LD   C,H		  */
    /*TODO*///OP(op,4d) { _C = _L;												} /* LD   C,L		  */
    /*TODO*///OP(op,4e) { _C = RM(_HL);											} /* LD   C,(HL)	  */
    /*TODO*///OP(op,4f) { _C = _A;												} /* LD   C,A		  */
    /*TODO*///
    /*TODO*///OP(op,50) { _D = _B;												} /* LD   D,B		  */
    /*TODO*///OP(op,51) { _D = _C;												} /* LD   D,C		  */
    /*TODO*///OP(op,52) { 														} /* LD   D,D		  */
    /*TODO*///OP(op,53) { _D = _E;												} /* LD   D,E		  */
    /*TODO*///OP(op,54) { _D = _H;												} /* LD   D,H		  */
    /*TODO*///OP(op,55) { _D = _L;												} /* LD   D,L		  */
    /*TODO*///OP(op,56) { _D = RM(_HL);											} /* LD   D,(HL)	  */
    /*TODO*///OP(op,57) { _D = _A;												} /* LD   D,A		  */
    /*TODO*///
    /*TODO*///OP(op,58) { _E = _B;												} /* LD   E,B		  */
    /*TODO*///OP(op,59) { _E = _C;												} /* LD   E,C		  */
    /*TODO*///OP(op,5a) { _E = _D;												} /* LD   E,D		  */
    /*TODO*///OP(op,5b) { 														} /* LD   E,E		  */
    /*TODO*///OP(op,5c) { _E = _H;												} /* LD   E,H		  */
    /*TODO*///OP(op,5d) { _E = _L;												} /* LD   E,L		  */
    /*TODO*///OP(op,5e) { _E = RM(_HL);											} /* LD   E,(HL)	  */
    /*TODO*///OP(op,5f) { _E = _A;												} /* LD   E,A		  */
    /*TODO*///
    /*TODO*///OP(op,60) { _H = _B;												} /* LD   H,B		  */
    /*TODO*///OP(op,61) { _H = _C;												} /* LD   H,C		  */
    /*TODO*///OP(op,62) { _H = _D;												} /* LD   H,D		  */
    /*TODO*///OP(op,63) { _H = _E;												} /* LD   H,E		  */
    /*TODO*///OP(op,64) { 														} /* LD   H,H		  */
    /*TODO*///OP(op,65) { _H = _L;												} /* LD   H,L		  */
    /*TODO*///OP(op,66) { _H = RM(_HL);											} /* LD   H,(HL)	  */
    /*TODO*///OP(op,67) { _H = _A;												} /* LD   H,A		  */
    /*TODO*///
    /*TODO*///OP(op,68) { _L = _B;												} /* LD   L,B		  */
    /*TODO*///OP(op,69) { _L = _C;												} /* LD   L,C		  */
    /*TODO*///OP(op,6a) { _L = _D;												} /* LD   L,D		  */
    /*TODO*///OP(op,6b) { _L = _E;												} /* LD   L,E		  */
    /*TODO*///OP(op,6c) { _L = _H;												} /* LD   L,H		  */
    /*TODO*///OP(op,6d) { 														} /* LD   L,L		  */
    /*TODO*///OP(op,6e) { _L = RM(_HL);											} /* LD   L,(HL)	  */
    /*TODO*///OP(op,6f) { _L = _A;												} /* LD   L,A		  */
    /*TODO*///
    /*TODO*///OP(op,70) { WM( _HL, _B );											} /* LD   (HL),B	  */
    /*TODO*///OP(op,71) { WM( _HL, _C );											} /* LD   (HL),C	  */
    /*TODO*///OP(op,72) { WM( _HL, _D );											} /* LD   (HL),D	  */
    /*TODO*///OP(op,73) { WM( _HL, _E );											} /* LD   (HL),E	  */
    /*TODO*///OP(op,74) { WM( _HL, _H );											} /* LD   (HL),H	  */
    /*TODO*///OP(op,75) { WM( _HL, _L );											} /* LD   (HL),L	  */
    /*TODO*///OP(op,76) { ENTER_HALT; 											} /* HALT			  */
    /*TODO*///OP(op,77) { WM( _HL, _A );											} /* LD   (HL),A	  */
    /*TODO*///
    /*TODO*///OP(op,78) { _A = _B;												} /* LD   A,B		  */
    /*TODO*///OP(op,79) { _A = _C;												} /* LD   A,C		  */
    /*TODO*///OP(op,7a) { _A = _D;												} /* LD   A,D		  */
    /*TODO*///OP(op,7b) { _A = _E;												} /* LD   A,E		  */
    /*TODO*///OP(op,7c) { _A = _H;												} /* LD   A,H		  */
    /*TODO*///OP(op,7d) { _A = _L;												} /* LD   A,L		  */
    /*TODO*///OP(op,7e) { _A = RM(_HL);											} /* LD   A,(HL)	  */
    /*TODO*///OP(op,7f) { 														} /* LD   A,A		  */
    /*TODO*///
    /*TODO*///OP(op,80) { ADD(_B);												} /* ADD  A,B		  */
    /*TODO*///OP(op,81) { ADD(_C);												} /* ADD  A,C		  */
    /*TODO*///OP(op,82) { ADD(_D);												} /* ADD  A,D		  */
    /*TODO*///OP(op,83) { ADD(_E);												} /* ADD  A,E		  */
    /*TODO*///OP(op,84) { ADD(_H);												} /* ADD  A,H		  */
    /*TODO*///OP(op,85) { ADD(_L);												} /* ADD  A,L		  */
    /*TODO*///OP(op,86) { ADD(RM(_HL));											} /* ADD  A,(HL)	  */
    /*TODO*///OP(op,87) { ADD(_A);												} /* ADD  A,A		  */
    /*TODO*///
    /*TODO*///OP(op,88) { ADC(_B);												} /* ADC  A,B		  */
    /*TODO*///OP(op,89) { ADC(_C);												} /* ADC  A,C		  */
    /*TODO*///OP(op,8a) { ADC(_D);												} /* ADC  A,D		  */
    /*TODO*///OP(op,8b) { ADC(_E);												} /* ADC  A,E		  */
    /*TODO*///OP(op,8c) { ADC(_H);												} /* ADC  A,H		  */
    /*TODO*///OP(op,8d) { ADC(_L);												} /* ADC  A,L		  */
    /*TODO*///OP(op,8e) { ADC(RM(_HL));											} /* ADC  A,(HL)	  */
    /*TODO*///OP(op,8f) { ADC(_A);												} /* ADC  A,A		  */
    /*TODO*///
    /*TODO*///OP(op,90) { SUB(_B);												} /* SUB  B 		  */
    /*TODO*///OP(op,91) { SUB(_C);												} /* SUB  C 		  */
    /*TODO*///OP(op,92) { SUB(_D);												} /* SUB  D 		  */
    /*TODO*///OP(op,93) { SUB(_E);												} /* SUB  E 		  */
    /*TODO*///OP(op,94) { SUB(_H);												} /* SUB  H 		  */
    /*TODO*///OP(op,95) { SUB(_L);												} /* SUB  L 		  */
    /*TODO*///OP(op,96) { SUB(RM(_HL));											} /* SUB  (HL)		  */
    /*TODO*///OP(op,97) { SUB(_A);												} /* SUB  A 		  */
    /*TODO*///
    /*TODO*///OP(op,98) { SBC(_B);												} /* SBC  A,B		  */
    /*TODO*///OP(op,99) { SBC(_C);												} /* SBC  A,C		  */
    /*TODO*///OP(op,9a) { SBC(_D);												} /* SBC  A,D		  */
    /*TODO*///OP(op,9b) { SBC(_E);												} /* SBC  A,E		  */
    /*TODO*///OP(op,9c) { SBC(_H);												} /* SBC  A,H		  */
    /*TODO*///OP(op,9d) { SBC(_L);												} /* SBC  A,L		  */
    /*TODO*///OP(op,9e) { SBC(RM(_HL));											} /* SBC  A,(HL)	  */
    /*TODO*///OP(op,9f) { SBC(_A);												} /* SBC  A,A		  */
    /*TODO*///
    /*TODO*///OP(op,a0) { AND(_B);												} /* AND  B 		  */
    /*TODO*///OP(op,a1) { AND(_C);												} /* AND  C 		  */
    /*TODO*///OP(op,a2) { AND(_D);												} /* AND  D 		  */
    /*TODO*///OP(op,a3) { AND(_E);												} /* AND  E 		  */
    /*TODO*///OP(op,a4) { AND(_H);												} /* AND  H 		  */
    /*TODO*///OP(op,a5) { AND(_L);												} /* AND  L 		  */
    /*TODO*///OP(op,a6) { AND(RM(_HL));											} /* AND  (HL)		  */
    /*TODO*///OP(op,a7) { AND(_A);												} /* AND  A 		  */
    /*TODO*///
    /*TODO*///OP(op,a8) { XOR(_B);												} /* XOR  B 		  */
    /*TODO*///OP(op,a9) { XOR(_C);												} /* XOR  C 		  */
    /*TODO*///OP(op,aa) { XOR(_D);												} /* XOR  D 		  */
    /*TODO*///OP(op,ab) { XOR(_E);												} /* XOR  E 		  */
    /*TODO*///OP(op,ac) { XOR(_H);												} /* XOR  H 		  */
    /*TODO*///OP(op,ad) { XOR(_L);												} /* XOR  L 		  */
    /*TODO*///OP(op,ae) { XOR(RM(_HL));											} /* XOR  (HL)		  */
    /*TODO*///OP(op,af) { XOR(_A);												} /* XOR  A 		  */
    /*TODO*///
    /*TODO*///OP(op,b0) { OR(_B); 												} /* OR   B 		  */
    /*TODO*///OP(op,b1) { OR(_C); 												} /* OR   C 		  */
    /*TODO*///OP(op,b2) { OR(_D); 												} /* OR   D 		  */
    /*TODO*///OP(op,b3) { OR(_E); 												} /* OR   E 		  */
    /*TODO*///OP(op,b4) { OR(_H); 												} /* OR   H 		  */
    /*TODO*///OP(op,b5) { OR(_L); 												} /* OR   L 		  */
    /*TODO*///OP(op,b6) { OR(RM(_HL));											} /* OR   (HL)		  */
    /*TODO*///OP(op,b7) { OR(_A); 												} /* OR   A 		  */
    /*TODO*///
    /*TODO*///OP(op,b8) { CP(_B); 												} /* CP   B 		  */
    /*TODO*///OP(op,b9) { CP(_C); 												} /* CP   C 		  */
    /*TODO*///OP(op,ba) { CP(_D); 												} /* CP   D 		  */
    /*TODO*///OP(op,bb) { CP(_E); 												} /* CP   E 		  */
    /*TODO*///OP(op,bc) { CP(_H); 												} /* CP   H 		  */
    /*TODO*///OP(op,bd) { CP(_L); 												} /* CP   L 		  */
    /*TODO*///OP(op,be) { CP(RM(_HL));											} /* CP   (HL)		  */
    /*TODO*///OP(op,bf) { CP(_A); 												} /* CP   A 		  */
    /*TODO*///
    /*TODO*///OP(op,c0) { RET( !(_F & ZF) );										} /* RET  NZ		  */
    /*TODO*///OP(op,c1) { POP(BC);												} /* POP  BC		  */
    /*TODO*///OP(op,c2) { JP_COND( !(_F & ZF) );									} /* JP   NZ,a		  */
    /*TODO*///OP(op,c3) { JP; 													} /* JP   a 		  */
    /*TODO*///OP(op,c4) { CALL( !(_F & ZF) ); 									} /* CALL NZ,a		  */
    /*TODO*///OP(op,c5) { PUSH( BC ); 											} /* PUSH BC		  */
    /*TODO*///OP(op,c6) { ADD(ARG()); 											} /* ADD  A,n		  */
    /*TODO*///OP(op,c7) { RST(0x00);												} /* RST  0 		  */
    /*TODO*///
    /*TODO*///OP(op,c8) { RET( _F & ZF ); 										} /* RET  Z 		  */
    /*TODO*///OP(op,c9) { RET(1); 												} /* RET			  */
    /*TODO*///OP(op,ca) { JP_COND( _F & ZF ); 									} /* JP   Z,a		  */
    /*TODO*///OP(op,cb) { _R++; EXEC(cb,ROP());									} /* **** CB xx 	  */
    /*TODO*///OP(op,cc) { CALL( _F & ZF );										} /* CALL Z,a		  */
    /*TODO*///OP(op,cd) { CALL(1);												} /* CALL a 		  */
    /*TODO*///OP(op,ce) { ADC(ARG()); 											} /* ADC  A,n		  */
    /*TODO*///OP(op,cf) { RST(0x08);												} /* RST  1 		  */
    /*TODO*///
    /*TODO*///OP(op,d0) { RET( !(_F & CF) );										} /* RET  NC		  */
    /*TODO*///OP(op,d1) { POP(DE);												} /* POP  DE		  */
    /*TODO*///OP(op,d2) { JP_COND( !(_F & CF) );									} /* JP   NC,a		  */
    /*TODO*///OP(op,d3) { unsigned n = ARG() | (_A << 8); OUT( n, _A );			} /* OUT  (n),A 	  */
    /*TODO*///OP(op,d4) { CALL( !(_F & CF) ); 									} /* CALL NC,a		  */
    /*TODO*///OP(op,d5) { PUSH( DE ); 											} /* PUSH DE		  */
    /*TODO*///OP(op,d6) { SUB(ARG()); 											} /* SUB  n 		  */
    /*TODO*///OP(op,d7) { RST(0x10);												} /* RST  2 		  */
    /*TODO*///
    /*TODO*///OP(op,d8) { RET( _F & CF ); 										} /* RET  C 		  */
    /*TODO*///OP(op,d9) { EXX;													} /* EXX			  */
    /*TODO*///OP(op,da) { JP_COND( _F & CF ); 									} /* JP   C,a		  */
    /*TODO*///OP(op,db) { unsigned n = ARG() | (_A << 8); _A = IN( n );			} /* IN   A,(n) 	  */
    /*TODO*///OP(op,dc) { CALL( _F & CF );										} /* CALL C,a		  */
    /*TODO*///OP(op,dd) { _R++; EXEC(dd,ROP());									} /* **** DD xx 	  */
    /*TODO*///OP(op,de) { SBC(ARG()); 											} /* SBC  A,n		  */
    /*TODO*///OP(op,df) { RST(0x18);												} /* RST  3 		  */
    /*TODO*///
    /*TODO*///OP(op,e0) { RET( !(_F & PF) );										} /* RET  PO		  */
    /*TODO*///OP(op,e1) { POP(HL);												} /* POP  HL		  */
    /*TODO*///OP(op,e2) { JP_COND( !(_F & PF) );									} /* JP   PO,a		  */
    /*TODO*///OP(op,e3) { EXSP(HL);												} /* EX   HL,(SP)	  */
    /*TODO*///OP(op,e4) { CALL( !(_F & PF) ); 									} /* CALL PO,a		  */
    /*TODO*///OP(op,e5) { PUSH( HL ); 											} /* PUSH HL		  */
    /*TODO*///OP(op,e6) { AND(ARG()); 											} /* AND  n 		  */
    /*TODO*///OP(op,e7) { RST(0x20);												} /* RST  4 		  */
    /*TODO*///
    /*TODO*///OP(op,e8) { RET( _F & PF ); 										} /* RET  PE		  */
    /*TODO*///OP(op,e9) { _PC = _HL; change_pc16(_PCD);							} /* JP   (HL)		  */
    /*TODO*///OP(op,ea) { JP_COND( _F & PF ); 									} /* JP   PE,a		  */
    /*TODO*///OP(op,eb) { EX_DE_HL;												} /* EX   DE,HL 	  */
    /*TODO*///OP(op,ec) { CALL( _F & PF );										} /* CALL PE,a		  */
    /*TODO*///OP(op,ed) { _R++; EXEC(ed,ROP());									} /* **** ED xx 	  */
    /*TODO*///OP(op,ee) { XOR(ARG()); 											} /* XOR  n 		  */
    /*TODO*///OP(op,ef) { RST(0x28);												} /* RST  5 		  */
    /*TODO*///
    /*TODO*///OP(op,f0) { RET( !(_F & SF) );										} /* RET  P 		  */
    /*TODO*///OP(op,f1) { POP(AF);												} /* POP  AF		  */
    /*TODO*///OP(op,f2) { JP_COND( !(_F & SF) );									} /* JP   P,a		  */
    /*TODO*///OP(op,f3) { _IFF1 = _IFF2 = 0;										} /* DI 			  */
    /*TODO*///OP(op,f4) { CALL( !(_F & SF) ); 									} /* CALL P,a		  */
    /*TODO*///OP(op,f5) { PUSH( AF ); 											} /* PUSH AF		  */
    /*TODO*///OP(op,f6) { OR(ARG());												} /* OR   n 		  */
    /*TODO*///OP(op,f7) { RST(0x30);												} /* RST  6 		  */
    /*TODO*///
    /*TODO*///OP(op,f8) { RET(_F & SF);											} /* RET  M 		  */
    /*TODO*///OP(op,f9) { _SP = _HL;												} /* LD   SP,HL 	  */
    /*TODO*///OP(op,fa) { JP_COND(_F & SF);										} /* JP   M,a		  */
    /*TODO*///OP(op,fb) { EI; 													} /* EI 			  */
    /*TODO*///OP(op,fc) { CALL(_F & SF);											} /* CALL M,a		  */
    /*TODO*///OP(op,fd) { _R++; EXEC(fd,ROP());									} /* **** FD xx 	  */
    /*TODO*///OP(op,fe) { CP(ARG());												} /* CP   n 		  */
    /*TODO*///OP(op,ff) { RST(0x38);												} /* RST  7 		  */
    /*TODO*///
    /*TODO*///
    /*TODO*///static void take_interrupt(void)
    /*TODO*///{
    /*TODO*///    if( _IFF1 )
    /*TODO*///    {
    /*TODO*///        int irq_vector;
    /*TODO*///
    /*TODO*///        /* there isn't a valid previous program counter */
    /*TODO*///        _PPC = -1;
    /*TODO*///
    /*TODO*///        /* Check if processor was halted */
    /*TODO*///		LEAVE_HALT;
    /*TODO*///
    /*TODO*///        if( Z80.irq_max )           /* daisy chain mode */
    /*TODO*///        {
    /*TODO*///            if( Z80.request_irq >= 0 )
    /*TODO*///            {
    /*TODO*///                /* Clear both interrupt flip flops */
    /*TODO*///                _IFF1 = _IFF2 = 0;
    /*TODO*///                irq_vector = Z80.irq[Z80.request_irq].interrupt_entry(Z80.irq[Z80.request_irq].irq_param);
    /*TODO*///                LOG((errorlog, "Z80#%d daisy chain irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
    /*TODO*///                Z80.request_irq = -1;
    /*TODO*///            } else return;
    /*TODO*///        }
    /*TODO*///        else
    /*TODO*///        {
    /*TODO*///            /* Clear both interrupt flip flops */
    /*TODO*///            _IFF1 = _IFF2 = 0;
    /*TODO*///            /* call back the cpu interface to retrieve the vector */
    /*TODO*///            irq_vector = (*Z80.irq_callback)(0);
    /*TODO*///            LOG((errorlog, "Z80#%d single int. irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
    /*TODO*///        }
    /*TODO*///
    /*TODO*///        /* Interrupt mode 2. Call [Z80.I:databyte] */
    /*TODO*///        if( _IM == 2 )
    /*TODO*///        {
    /*TODO*///			irq_vector = (irq_vector & 0xff) | (_I << 8);
    /*TODO*///            PUSH( PC );
    /*TODO*///			RM16( irq_vector, &Z80.PC );
    /*TODO*///            LOG((errorlog, "Z80#%d IM2 [$%04x] = $%04x\n",cpu_getactivecpu() , irq_vector, _PCD));
    /*TODO*///            Z80.extra_cycles += 19;
    /*TODO*///        }
    /*TODO*///        else
    /*TODO*///        /* Interrupt mode 1. RST 38h */
    /*TODO*///        if( _IM == 1 )
    /*TODO*///        {
    /*TODO*///            LOG((errorlog, "Z80#%d IM1 $0038\n",cpu_getactivecpu() ));
    /*TODO*///            PUSH( PC );
    /*TODO*///            _PCD = 0x0038;
    /*TODO*///            Z80.extra_cycles += 11+2; /* RST $38 + 2 cycles */
    /*TODO*///        }
    /*TODO*///        else
    /*TODO*///        {
    /*TODO*///            /* Interrupt mode 0. We check for CALL and JP instructions, */
    /*TODO*///            /* if neither of these were found we assume a 1 byte opcode */
    /*TODO*///            /* was placed on the databus                                */
    /*TODO*///            LOG((errorlog, "Z80#%d IM0 $%04x\n",cpu_getactivecpu() , irq_vector));
    /*TODO*///            switch (irq_vector & 0xff0000)
    /*TODO*///            {
    /*TODO*///                case 0xcd0000:  /* call */
    /*TODO*///                    PUSH( PC );
    /*TODO*///                    Z80.extra_cycles += 5;  /* CALL $xxxx cycles (JP $xxxx follows)*/
    /*TODO*///                case 0xc30000:  /* jump */
    /*TODO*///                    _PCD = irq_vector & 0xffff;
    /*TODO*///                    Z80.extra_cycles += 10 + 2; /* JP $xxxx + 2 cycles */
    /*TODO*///                    break;
    /*TODO*///                default:        /* rst */
    /*TODO*///                    PUSH( PC );
    /*TODO*///                    _PCD = irq_vector & 0x0038;
    /*TODO*///                    Z80.extra_cycles += 11 + 2; /* RST $xx + 2 cycles */
    /*TODO*///                    break;
    /*TODO*///            }
    /*TODO*///        }
    /*TODO*///        change_pc(_PCD);
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Reset registers to their initial values
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_reset(void *param)
    /*TODO*///{
    /*TODO*///	Z80_DaisyChain *daisy_chain = (Z80_DaisyChain *)param;
    /*TODO*///	int i, p;
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///	if( !SZHVC_add || !SZHVC_sub )
    /*TODO*///    {
    /*TODO*///		int oldval, newval, val;
    /*TODO*///		UINT8 *padd, *padc, *psub, *psbc;
    /*TODO*///        /* allocate big flag arrays once */
    /*TODO*///		SZHVC_add = (UINT8 *)malloc(2*256*256);
    /*TODO*///		SZHVC_sub = (UINT8 *)malloc(2*256*256);
    /*TODO*///		if( !SZHVC_add || !SZHVC_sub )
    /*TODO*///		{
    /*TODO*///			LOG((errorlog, "Z80: failed to allocate 2 * 128K flags arrays!!!\n"));
    /*TODO*///			raise(SIGABRT);
    /*TODO*///		}
    /*TODO*///		padd = &SZHVC_add[	0*256];
    /*TODO*///		padc = &SZHVC_add[256*256];
    /*TODO*///		psub = &SZHVC_sub[	0*256];
    /*TODO*///		psbc = &SZHVC_sub[256*256];
    /*TODO*///		for (oldval = 0; oldval < 256; oldval++)
    /*TODO*///		{
    /*TODO*///			for (newval = 0; newval < 256; newval++)
    /*TODO*///			{
    /*TODO*///				/* add or adc w/o carry set */
    /*TODO*///				val = newval - oldval;
    /*TODO*///				*padd = (newval) ? ((newval & 0x80) ? SF : 0) : ZF;
    /*TODO*///#if Z80_EXACT
    /*TODO*///				*padd |= (newval & (YF | XF));	/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///                if( (newval & 0x0f) < (oldval & 0x0f) ) *padd |= HF;
    /*TODO*///				if( newval < oldval ) *padd |= CF;
    /*TODO*///				if( (val^oldval^0x80) & (val^newval) & 0x80 ) *padd |= VF;
    /*TODO*///				padd++;
    /*TODO*///
    /*TODO*///				/* adc with carry set */
    /*TODO*///				val = newval - oldval - 1;
    /*TODO*///				*padc = (newval) ? ((newval & 0x80) ? SF : 0) : ZF;
    /*TODO*///#if Z80_EXACT
    /*TODO*///				*padc |= (newval & (YF | XF));	/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///                if( (newval & 0x0f) <= (oldval & 0x0f) ) *padc |= HF;
    /*TODO*///				if( newval <= oldval ) *padc |= CF;
    /*TODO*///				if( (val^oldval^0x80) & (val^newval) & 0x80 ) *padc |= VF;
    /*TODO*///				padc++;
    /*TODO*///
    /*TODO*///				/* cp, sub or sbc w/o carry set */
    /*TODO*///				val = oldval - newval;
    /*TODO*///				*psub = NF | ((newval) ? ((newval & 0x80) ? SF : 0) : ZF);
    /*TODO*///#if Z80_EXACT
    /*TODO*///				*psub |= (newval & (YF | XF));	/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///                if( (newval & 0x0f) > (oldval & 0x0f) ) *psub |= HF;
    /*TODO*///				if( newval > oldval ) *psub |= CF;
    /*TODO*///				if( (val^oldval) & (oldval^newval) & 0x80 ) *psub |= VF;
    /*TODO*///				psub++;
    /*TODO*///
    /*TODO*///				/* sbc with carry set */
    /*TODO*///				val = oldval - newval - 1;
    /*TODO*///				*psbc = NF | ((newval) ? ((newval & 0x80) ? SF : 0) : ZF);
    /*TODO*///#if Z80_EXACT
    /*TODO*///				*psbc |= (newval & (YF | XF));	/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///                if( (newval & 0x0f) >= (oldval & 0x0f) ) *psbc |= HF;
    /*TODO*///				if( newval >= oldval ) *psbc |= CF;
    /*TODO*///				if( (val^oldval) & (oldval^newval) & 0x80 ) *psbc |= VF;
    /*TODO*///				psbc++;
    /*TODO*///			}
    /*TODO*///        }
    /*TODO*///    }
    /*TODO*///#endif
    /*TODO*///	for (i = 0; i < 256; i++)
    /*TODO*///	{
    /*TODO*///		p = 0;
    /*TODO*///		if( i&0x01 ) ++p;
    /*TODO*///		if( i&0x02 ) ++p;
    /*TODO*///		if( i&0x04 ) ++p;
    /*TODO*///		if( i&0x08 ) ++p;
    /*TODO*///		if( i&0x10 ) ++p;
    /*TODO*///		if( i&0x20 ) ++p;
    /*TODO*///		if( i&0x40 ) ++p;
    /*TODO*///		if( i&0x80 ) ++p;
    /*TODO*///		SZ[i] = i ? i & SF : ZF;
    /*TODO*///#if Z80_EXACT
    /*TODO*///		SZ[i] |= (i & (YF | XF));		/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///		SZ_BIT[i] = i ? i & SF : ZF | PF;
    /*TODO*///#if Z80_EXACT
    /*TODO*///		SZ_BIT[i] |= (i & (YF | XF));	/* undocumented flag bits 5+3 */
    /*TODO*///#endif
    /*TODO*///        SZP[i] = SZ[i] | ((p & 1) ? 0 : PF);
    /*TODO*///		SZHV_inc[i] = SZ[i];
    /*TODO*///		if( i == 0x80 ) SZHV_inc[i] |= VF;
    /*TODO*///		if( (i & 0x0f) == 0x00 ) SZHV_inc[i] |= HF;
    /*TODO*///		SZHV_dec[i] = SZ[i] | NF;
    /*TODO*///		if( i == 0x7f ) SZHV_dec[i] |= VF;
    /*TODO*///		if( (i & 0x0f) == 0x0f ) SZHV_dec[i] |= HF;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	memset(&Z80, 0, sizeof(Z80));
    /*TODO*///	_IX = _IY = 0xffff; /* IX and IY are FFFF after a reset! */
    /*TODO*///	_F = ZF;			/* Zero flag is set */
    /*TODO*///	Z80.request_irq = -1;
    /*TODO*///	Z80.service_irq = -1;
    /*TODO*///    Z80.nmi_state = CLEAR_LINE;
    /*TODO*///	Z80.irq_state = CLEAR_LINE;
    /*TODO*///
    /*TODO*///    if( daisy_chain )
    /*TODO*///	{
    /*TODO*///		while( daisy_chain->irq_param != -1 && Z80.irq_max < Z80_MAXDAISY )
    /*TODO*///		{
    /*TODO*///            /* set callbackhandler after reti */
    /*TODO*///			Z80.irq[Z80.irq_max] = *daisy_chain;
    /*TODO*///            /* device reset */
    /*TODO*///			if( Z80.irq[Z80.irq_max].reset )
    /*TODO*///				Z80.irq[Z80.irq_max].reset(Z80.irq[Z80.irq_max].irq_param);
    /*TODO*///			Z80.irq_max++;
    /*TODO*///            daisy_chain++;
    /*TODO*///        }
    /*TODO*///    }
    /*TODO*///
    /*TODO*///    change_pc(_PCD);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void z80_exit(void)
    /*TODO*///{
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///	if (SZHVC_add) free(SZHVC_add);
    /*TODO*///	SZHVC_add = NULL;
    /*TODO*///	if (SZHVC_sub) free(SZHVC_sub);
    /*TODO*///	SZHVC_sub = NULL;
    /*TODO*///#endif
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Execute 'cycles' T-states. Return number of T-states really executed
    /*TODO*/// ****************************************************************************/
    /*TODO*///int z80_execute(int cycles)
    /*TODO*///{
    /*TODO*///	z80_ICount = cycles - Z80.extra_cycles;
    /*TODO*///	Z80.extra_cycles = 0;
    /*TODO*///
    /*TODO*///    do
    /*TODO*///	{
    /*TODO*///        _PPC = _PCD;
    /*TODO*///        CALL_MAME_DEBUG;
    /*TODO*///		_R++;
    /*TODO*///        EXEC_INLINE(op,ROP());
    /*TODO*///	} while( z80_ICount > 0 );
    /*TODO*///
    /*TODO*///	z80_ICount -= Z80.extra_cycles;
    /*TODO*///    Z80.extra_cycles = 0;
    /*TODO*///
    /*TODO*///    return cycles - z80_ICount;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Burn 'cycles' T-states. Adjust R register for the lost time
    /*TODO*/// ****************************************************************************/
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
           /*TODO*///	if( cycles > 0 )
        /*TODO*///	{
        /*TODO*///		/* NOP takes 4 cycles per instruction */
        /*TODO*///		int n = (cycles + 3) / 4;
        /*TODO*///		_R += n;
        /*TODO*///		z80_ICount -= 4 * n;
        /*TODO*///	}
     throw new UnsupportedOperationException("Not supported yet.");
    }};

    /*TODO*////****************************************************************************
    /*TODO*/// * Get all registers in given buffer
    /*TODO*/// ****************************************************************************/
  /*  @Override
    public void init_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    /*TODO*///unsigned z80_get_context (void *dst)
    /*TODO*///{
    /*TODO*///	if( dst )
    /*TODO*///	    *(Z80_Regs*)dst = Z80;
    /*TODO*///	return sizeof(Z80_Regs);
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set all registers to given values
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_context (void *src)
    /*TODO*///{
    /*TODO*///	if( src )
    /*TODO*///		Z80 = *(Z80_Regs*)src;
    /*TODO*///    change_pc(_PCD);
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return program counter
    /*TODO*/// ****************************************************************************/
    /*TODO*///unsigned z80_get_pc (void)
    /*TODO*///{
    /*TODO*///    return _PCD;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set program counter
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_pc (unsigned val)
    /*TODO*///{
    /*TODO*///	_PC = val;
    /*TODO*///	change_pc(_PCD);
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return stack pointer
    /*TODO*/// ****************************************************************************/
    /*TODO*///unsigned z80_get_sp (void)
    /*TODO*///{
    /*TODO*///	return _SPD;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set stack pointer
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_sp (unsigned val)
    /*TODO*///{
    /*TODO*///	_SP = val;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return a specific register
    /*TODO*/// ****************************************************************************/
    /*TODO*///unsigned z80_get_reg (int regnum)
    /*TODO*///{
    /*TODO*///	switch( regnum )
    /*TODO*///	{
    /*TODO*///		case Z80_PC: return Z80.PC.w.l;
    /*TODO*///		case Z80_SP: return Z80.SP.w.l;
    /*TODO*///		case Z80_AF: return Z80.AF.w.l;
    /*TODO*///		case Z80_BC: return Z80.BC.w.l;
    /*TODO*///		case Z80_DE: return Z80.DE.w.l;
    /*TODO*///		case Z80_HL: return Z80.HL.w.l;
    /*TODO*///		case Z80_IX: return Z80.IX.w.l;
    /*TODO*///		case Z80_IY: return Z80.IY.w.l;
    /*TODO*///        case Z80_R: return (Z80.R & 0x7f) | (Z80.R2 & 0x80);
    /*TODO*///		case Z80_I: return Z80.I;
    /*TODO*///		case Z80_AF2: return Z80.AF2.w.l;
    /*TODO*///		case Z80_BC2: return Z80.BC2.w.l;
    /*TODO*///		case Z80_DE2: return Z80.DE2.w.l;
    /*TODO*///		case Z80_HL2: return Z80.HL2.w.l;
    /*TODO*///		case Z80_IM: return Z80.IM;
    /*TODO*///		case Z80_IFF1: return Z80.IFF1;
    /*TODO*///		case Z80_IFF2: return Z80.IFF2;
    /*TODO*///		case Z80_HALT: return Z80.HALT;
    /*TODO*///		case Z80_NMI_STATE: return Z80.nmi_state;
    /*TODO*///		case Z80_IRQ_STATE: return Z80.irq_state;
    /*TODO*///		case Z80_DC0: return Z80.int_state[0];
    /*TODO*///		case Z80_DC1: return Z80.int_state[1];
    /*TODO*///		case Z80_DC2: return Z80.int_state[2];
    /*TODO*///		case Z80_DC3: return Z80.int_state[3];
    /*TODO*///        case REG_PREVIOUSPC: return Z80.PREPC.w.l;
    /*TODO*///		default:
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = _SPD + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				if( offset < 0xffff )
    /*TODO*///					return RM( offset ) | ( RM( offset + 1) << 8 );
    /*TODO*///			}
    /*TODO*///	}
    /*TODO*///    return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set a specific register
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_reg (int regnum, unsigned val)
    /*TODO*///{
    /*TODO*///	switch( regnum )
    /*TODO*///	{
    /*TODO*///		case Z80_PC: Z80.PC.w.l = val; break;
    /*TODO*///		case Z80_SP: Z80.SP.w.l = val; break;
    /*TODO*///		case Z80_AF: Z80.AF.w.l = val; break;
    /*TODO*///		case Z80_BC: Z80.BC.w.l = val; break;
    /*TODO*///		case Z80_DE: Z80.DE.w.l = val; break;
    /*TODO*///		case Z80_HL: Z80.HL.w.l = val; break;
    /*TODO*///		case Z80_IX: Z80.IX.w.l = val; break;
    /*TODO*///		case Z80_IY: Z80.IY.w.l = val; break;
    /*TODO*///        case Z80_R: Z80.R = val; Z80.R2 = val & 0x80; break;
    /*TODO*///		case Z80_I: Z80.I = val; break;
    /*TODO*///		case Z80_AF2: Z80.AF2.w.l = val; break;
    /*TODO*///		case Z80_BC2: Z80.BC2.w.l = val; break;
    /*TODO*///		case Z80_DE2: Z80.DE2.w.l = val; break;
    /*TODO*///		case Z80_HL2: Z80.HL2.w.l = val; break;
    /*TODO*///		case Z80_IM: Z80.IM = val; break;
    /*TODO*///		case Z80_IFF1: Z80.IFF1 = val; break;
    /*TODO*///		case Z80_IFF2: Z80.IFF2 = val; break;
    /*TODO*///		case Z80_HALT: Z80.HALT = val; break;
    /*TODO*///		case Z80_NMI_STATE: z80_set_nmi_line(val); break;
    /*TODO*///		case Z80_IRQ_STATE: z80_set_irq_line(0,val); break;
    /*TODO*///		case Z80_DC0: Z80.int_state[0] = val; break;
    /*TODO*///		case Z80_DC1: Z80.int_state[1] = val; break;
    /*TODO*///		case Z80_DC2: Z80.int_state[2] = val; break;
    /*TODO*///		case Z80_DC3: Z80.int_state[3] = val; break;
    /*TODO*///        default:
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = _SPD + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				if( offset < 0xffff )
    /*TODO*///				{
    /*TODO*///					WM( offset, val & 0xff );
    /*TODO*///					WM( offset+1, (val >> 8) & 0xff );
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set NMI line state
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_nmi_line(int state)
    /*TODO*///{
    /*TODO*///	if( Z80.nmi_state == state ) return;
    /*TODO*///
    /*TODO*///    LOG((errorlog, "Z80#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
    /*TODO*///    Z80.nmi_state = state;
    /*TODO*///	if( state == CLEAR_LINE ) return;
    /*TODO*///
    /*TODO*///    LOG((errorlog, "Z80#%d take NMI\n", cpu_getactivecpu()));
    /*TODO*///	_PPC = -1;			/* there isn't a valid previous program counter */
    /*TODO*///	LEAVE_HALT; 		/* Check if processor was halted */
    /*TODO*///
    /*TODO*///	_IFF1 = 0;
    /*TODO*///    PUSH( PC );
    /*TODO*///	_PCD = 0x0066;
    /*TODO*///	Z80.extra_cycles += 11;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set IRQ line state
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_irq_line(int irqline, int state)
    /*TODO*///{
    /*TODO*///	LOG((errorlog, "Z80#%d set_irq_line %d\n",cpu_getactivecpu() , state));
    /*TODO*///    Z80.irq_state = state;
    /*TODO*///	if( state == CLEAR_LINE ) return;
    /*TODO*///
    /*TODO*///	if( Z80.irq_max )
    /*TODO*///	{
    /*TODO*///		int daisychain, device, int_state;
    /*TODO*///		daisychain = (*Z80.irq_callback)(irqline);
    /*TODO*///		device = daisychain >> 8;
    /*TODO*///		int_state = daisychain & 0xff;
    /*TODO*///		LOG((errorlog, "Z80#%d daisy chain $%04x -> device %d, state $%02x",cpu_getactivecpu(), daisychain, device, int_state));
    /*TODO*///
    /*TODO*///		if( Z80.int_state[device] != int_state )
    /*TODO*///		{
    /*TODO*///			LOG((errorlog, " change\n"));
    /*TODO*///			/* set new interrupt status */
    /*TODO*///            Z80.int_state[device] = int_state;
    /*TODO*///			/* check interrupt status */
    /*TODO*///			Z80.request_irq = Z80.service_irq = -1;
    /*TODO*///
    /*TODO*///            /* search higher IRQ or IEO */
    /*TODO*///			for( device = 0 ; device < Z80.irq_max ; device ++ )
    /*TODO*///			{
    /*TODO*///				/* IEO = disable ? */
    /*TODO*///				if( Z80.int_state[device] & Z80_INT_IEO )
    /*TODO*///				{
    /*TODO*///					Z80.request_irq = -1;		/* if IEO is disable , masking lower IRQ */
    /*TODO*///					Z80.service_irq = device;	/* set highest interrupt service device */
    /*TODO*///				}
    /*TODO*///				/* IRQ = request ? */
    /*TODO*///				if( Z80.int_state[device] & Z80_INT_REQ )
    /*TODO*///					Z80.request_irq = device;
    /*TODO*///			}
    /*TODO*///            LOG((errorlog, "Z80#%d daisy chain service_irq $%02x, request_irq $%02x\n", cpu_getactivecpu(), Z80.service_irq, Z80.request_irq));
    /*TODO*///			if( Z80.request_irq < 0 ) return;
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			LOG((errorlog, " no change\n"));
    /*TODO*///			return;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	take_interrupt();
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set IRQ vector callback
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_set_irq_callback(int (*callback)(int))
    /*TODO*///{
    /*TODO*///	LOG((errorlog, "Z80#%d set_irq_callback $%08x\n",cpu_getactivecpu() , (int)callback));
    /*TODO*///    Z80.irq_callback = callback;
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Save CPU state
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_state_save(void *file)
    /*TODO*///{
    /*TODO*///	int cpu = cpu_getactivecpu();
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "AF", &Z80.AF.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "BC", &Z80.BC.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "DE", &Z80.DE.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "HL", &Z80.HL.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "IX", &Z80.IX.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "IY", &Z80.IY.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "PC", &Z80.PC.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "SP", &Z80.SP.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "AF2", &Z80.AF2.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "BC2", &Z80.BC2.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "DE2", &Z80.DE2.w.l, 1);
    /*TODO*///	state_save_UINT16(file, "z80", cpu, "HL2", &Z80.HL2.w.l, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "R", &Z80.R, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "R2", &Z80.R2, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "IFF1", &Z80.IFF1, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "IFF2", &Z80.IFF2, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "HALT", &Z80.HALT, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "IM", &Z80.IM, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "I", &Z80.I, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "irq_max", &Z80.irq_max, 1);
    /*TODO*///	state_save_INT8(file, "z80", cpu, "request_irq", &Z80.request_irq, 1);
    /*TODO*///	state_save_INT8(file, "z80", cpu, "service_irq", &Z80.service_irq, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "int_state", Z80.int_state, 4);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "nmi_state", &Z80.nmi_state, 1);
    /*TODO*///	state_save_UINT8(file, "z80", cpu, "irq_state", &Z80.irq_state, 1);
    /*TODO*///	/* daisy chain needs to be saved by z80ctc.c somehow */
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Load CPU state
    /*TODO*/// ****************************************************************************/
    /*TODO*///void z80_state_load(void *file)
    /*TODO*///{
    /*TODO*///	int cpu = cpu_getactivecpu();
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "AF", &Z80.AF.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "BC", &Z80.BC.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "DE", &Z80.DE.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "HL", &Z80.HL.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "IX", &Z80.IX.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "IY", &Z80.IY.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "PC", &Z80.PC.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "SP", &Z80.SP.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "AF2", &Z80.AF2.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "BC2", &Z80.BC2.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "DE2", &Z80.DE2.w.l, 1);
    /*TODO*///	state_load_UINT16(file, "z80", cpu, "HL2", &Z80.HL2.w.l, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "R", &Z80.R, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "R2", &Z80.R2, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "IFF1", &Z80.IFF1, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "IFF2", &Z80.IFF2, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "HALT", &Z80.HALT, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "IM", &Z80.IM, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "I", &Z80.I, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "irq_max", &Z80.irq_max, 1);
    /*TODO*///	state_load_INT8(file, "z80", cpu, "request_irq", &Z80.request_irq, 1);
    /*TODO*///	state_load_INT8(file, "z80", cpu, "service_irq", &Z80.service_irq, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "int_state", Z80.int_state, 4);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "nmi_state", &Z80.nmi_state, 1);
    /*TODO*///	state_load_UINT8(file, "z80", cpu, "irq_state", &Z80.irq_state, 1);
    /*TODO*///    /* daisy chain needs to be restored by z80ctc.c somehow */
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return a formatted string for a register
    /*TODO*/// ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) 
    {
    /*TODO*///	static char buffer[32][47+1];
    /*TODO*///	static int which = 0;
    /*TODO*///	Z80_Regs *r = context;
    /*TODO*///
    /*TODO*///	which = ++which % 32;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///	if( !context )
    /*TODO*///		r = &Z80;
    /*TODO*///
            switch( regnum )
            {
    /*TODO*///		case CPU_INFO_REG+Z80_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_SP: sprintf(buffer[which], "SP:%04X", r->SP.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_AF: sprintf(buffer[which], "AF:%04X", r->AF.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_BC: sprintf(buffer[which], "BC:%04X", r->BC.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DE: sprintf(buffer[which], "DE:%04X", r->DE.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_HL: sprintf(buffer[which], "HL:%04X", r->HL.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IX: sprintf(buffer[which], "IX:%04X", r->IX.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IY: sprintf(buffer[which], "IY:%04X", r->IY.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_R: sprintf(buffer[which], "R:%02X", (r->R & 0x7f) | (r->R2 & 0x80)); break;
    /*TODO*///		case CPU_INFO_REG+Z80_I: sprintf(buffer[which], "I:%02X", r->I); break;
    /*TODO*///		case CPU_INFO_REG+Z80_AF2: sprintf(buffer[which], "AF'%04X", r->AF2.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_BC2: sprintf(buffer[which], "BC'%04X", r->BC2.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DE2: sprintf(buffer[which], "DE'%04X", r->DE2.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_HL2: sprintf(buffer[which], "HL'%04X", r->HL2.w.l); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IM: sprintf(buffer[which], "IM:%X", r->IM); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IFF1: sprintf(buffer[which], "IFF1:%X", r->IFF1); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IFF2: sprintf(buffer[which], "IFF2:%X", r->IFF2); break;
    /*TODO*///		case CPU_INFO_REG+Z80_HALT: sprintf(buffer[which], "HALT:%X", r->HALT); break;
    /*TODO*///		case CPU_INFO_REG+Z80_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///		case CPU_INFO_REG+Z80_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DC0: if(Z80.irq_max >= 1) sprintf(buffer[which], "DC0:%X", r->int_state[0]); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DC1: if(Z80.irq_max >= 2) sprintf(buffer[which], "DC1:%X", r->int_state[1]); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DC2: if(Z80.irq_max >= 3) sprintf(buffer[which], "DC2:%X", r->int_state[2]); break;
    /*TODO*///		case CPU_INFO_REG+Z80_DC3: if(Z80.irq_max >= 4) sprintf(buffer[which], "DC3:%X", r->int_state[3]); break;
    /*TODO*///        case CPU_INFO_FLAGS:
    /*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
    /*TODO*///				r->AF.b.l & 0x80 ? 'S':'.',
    /*TODO*///				r->AF.b.l & 0x40 ? 'Z':'.',
    /*TODO*///				r->AF.b.l & 0x20 ? '5':'.',
    /*TODO*///				r->AF.b.l & 0x10 ? 'H':'.',
    /*TODO*///				r->AF.b.l & 0x08 ? '3':'.',
    /*TODO*///				r->AF.b.l & 0x04 ? 'P':'.',
    /*TODO*///				r->AF.b.l & 0x02 ? 'N':'.',
    /*TODO*///				r->AF.b.l & 0x01 ? 'C':'.');
    /*TODO*///			break;
                case CPU_INFO_NAME: return "Z80";
                case CPU_INFO_FAMILY: return "Zilog Z80";
                case CPU_INFO_VERSION: return "2.7";
    
    /*TODO*///		case CPU_INFO_FILE: return __FILE__;
                case CPU_INFO_CREDITS: return "Copyright (C) 1998,1999 Juergen Buchmueller, all rights reserved.";
    /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)z80_reg_layout;
    /*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)z80_win_layout;
    	}
    /*TODO*///	return buffer[which];
            throw new UnsupportedOperationException("unsupported z80 cpu_info");
    }
    /*TODO*///
    /*TODO*///unsigned z80_dasm( char *buffer, unsigned pc )
    /*TODO*///{
    /*TODO*///#ifdef MAME_DEBUG
    /*TODO*///    return DasmZ80( buffer, pc );
    /*TODO*///#else
    /*TODO*///	sprintf( buffer, "$%02X", cpu_readop(pc) );
    /*TODO*///	return 1;
    /*TODO*///#endif
    /*TODO*///}

   /* @Override
    public void reset(Object param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

   /* @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    /*@Override
    public int execute(int cycles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    /*@Override
    public int get_pc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

   /* @Override
    public void set_op_base(int pc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

  

    
    /*
     * 
     *   OLD Z80 cpu core for reference TOBE REPLACED!!!
     * 
     */
      @Override
    public void set_irq_line(int irqline, int linestate) {
        Interrupt ();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
     	public static class z80_pair
	{
                public int H,L,W;
		public void SetH(int val) 
                {
                    H = val;
                    W = (H << 8) | L;
                }
		public void SetL(int val) 
                {
                    L = val;
                    W = (H << 8) | L;
                }
		public void SetW(int val)
                {
                    W = val;
                    H = W >> 8;
                    L = W & 0xFF;
                }
		public void AddH(int val) 
                {
                    H = (H + val) & 0xFF;
                    W = (H << 8) | L;
                }

		public void AddW(int val)
                {
                    W = (W + val) & 0xFFFF;
                    H = W >> 8;
                    L = W & 0xFF;
                }
              public void AddL(int val)
              {
                  L = (L + val) & 0xFF;
                  W = (H << 8) | L;
              }

	};

	/****************************************************************************/
	/* The Z80 registers. HALT is set to 1 when the CPU is halted, the refresh  */
	/* register is calculated as follows: refresh=(Regs.R&127)|(Regs.R2&128)    */
	/****************************************************************************/
	public static class Z80_Regs
	{
	  	public int AF2, BC2, DE2, HL2;
	  	public int IFF1, IFF2, HALT, IM, I, R, R2;
                public int AF, PC, SP;
                public int A, F;
                public z80_pair BC = new z80_pair();
                public z80_pair DE = new z80_pair();
                public z80_pair HL = new z80_pair();
                public z80_pair IX = new z80_pair();
                public z80_pair IY = new z80_pair();
                int pending_irq,pending_nmi;
	};
        /****************************************************************************/
	/* Reset registers to their initial values                                  */
	/****************************************************************************/
    @Override
	public void reset(Object param) 
	{
		R.AF = R.PC = R.SP = 0;
		R.A = R.F = 0;
		R.BC.SetW(0); R.DE.SetW(0); R.HL.SetW(0); R.IX.SetW(0); R.IY.SetW(0);
		R.AF2 = R.BC2 = R.DE2 = R.HL2 = 0;
		R.IFF1 = R.IFF2 = R.HALT = R.IM = R.I = R.R = R.R2 = 0;
		R.SP = 0xF000;
 		R.R = rand();
		Z80_Clear_Pending_Interrupts();
	}
        public void Z80_Clear_Pending_Interrupts()	/* NS 970904 */
        {
                R.pending_irq = Z80_IGNORE_INT;
                R.pending_nmi = 0;
        }
        public Z80_Regs R = new Z80_Regs();
        public int Z80_Running = 1;
	//public int Z80_IPeriod = 50000;
	//public int z80_ICount[0] = 50000;
	int PTable[] = new int[512];
	int ZSTable[] = new int[512];
	int ZSPTable[] = new int[512];
        
        @Override
        public Object init_context() {
            Object reg = new Z80_Regs();
            return reg;
        }
        @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
            System.out.println("irq callback");
            //ignore for now...
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public int get_pc() {
        return R.PC;
    }
    @Override
    public int execute(int cycles) {
        		Z80_Running=1;
 		InitTables();
                z80_ICount[0]=cycles;	/* NS 970904 */
 		do
 		{
                       if (R.pending_nmi != 0 || R.pending_irq != Z80_IGNORE_INT) Interrupt();	/* NS 970901 */
  			//++R.R;
                        R.R += 1;
			oldPC=R.PC;
  			//opcode=M_RDOP(R.PC);
                         int i = M_RDOP(R.PC);
			R.PC = (R.PC + 1) & 0xFFFF;
			z80_ICount[0] -= cycles_main[i];
  			if (opcode_main[i] != null)
                        {
  				opcode_main[i].handler();
                              //Z80_RegisterDump();
                        }
  			else
  			{
	  			System.out.println("MAIN PC = " + Integer.toHexString(oldPC) + " OPCODE = " + Integer.toHexString(i));
	  			
	  		}
 		}
 		while (z80_ICount[0] > 0);
 		return cycles - z80_ICount[0];	/* NS 970904 */
    }
    	final int S_FLAG = 0x80;
	final int Z_FLAG = 0x40;
	final int H_FLAG = 0x10;
	final int V_FLAG = 0x04;
	final int N_FLAG = 0x02;
	final int C_FLAG = 0x01;
    	void InitTables()//TODO checked it since i modified it for 0.27
	{
		int zs;
		int i, p;
		if (InitTables_virgin == 0) return;
		InitTables_virgin = 0;
		for (i = 0; i < 256; i++)
		{
			zs = 0;
			if (i == 0)
				zs |= Z_FLAG;
			if ((i&0x80) != 0)
				zs |= S_FLAG;
			p = 0;
			if ((i&1) != 0) p++;
			if ((i&2) != 0) p++;
			if ((i&4) != 0) p++;
			if ((i&8) != 0) p++;
			if ((i&16) != 0) p++;
			if ((i&32) != 0) p++;
			if ((i&64) != 0) p++;
			if ((i&128) != 0) p++;
			PTable[i] = ((p&1) != 0) ? 0:V_FLAG;
			ZSTable[i] = zs;
			ZSPTable[i] = zs | PTable[i];
		}
		for (i = 0; i < 256; i++)
		{
			ZSTable[i + 256] = ZSTable[i] | C_FLAG;
			ZSPTable[i + 256] = ZSPTable[i] | C_FLAG;
			PTable[i + 256] = PTable[i] | C_FLAG;
		}
	}
    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16(pc,0);
    }
    /****************************************************************************/
	/* Input a byte from given I/O port                                         */
	/****************************************************************************/
	static final char Z80_In(int Port) { return (char) cpu_readport(Port); }

	/****************************************************************************/
	/* Output a byte to given I/O port                                          */
	/****************************************************************************/
	static final void Z80_Out(int Port, int Value) { cpu_writeport(Port, Value); }

	/****************************************************************************/
	/* Read a byte from given memory location                                   */
	/****************************************************************************/
	public static final char Z80_RDMEM(int A) { return (char) cpu_readmem16(A); }
	
	/****************************************************************************/
	/* Write a byte to given memory location                                    */
	/****************************************************************************/
	public static final void Z80_WRMEM(int A, int V) { cpu_writemem16(A, (char) V); }
	
	/****************************************************************************/
	/* Z80_RDOP() is identical to Z80_RDMEM() except it is used for reading     */
	/* opcodes. In case of system with memory mapped I/O, this function can be  */
	/* used to greatly speed up emulation                                       */
	/****************************************************************************/
	public static final char Z80_RDOP(int A) { return OP_ROM.read(A); }
	
	/****************************************************************************/
	/* Z80_RDOP_ARG() is identical to Z80_RDOP() except it is used for reading  */
	/* opcode arguments. This difference can be used to support systems that    */
	/* use different encoding mechanisms for opcodes and opcode arguments       */
	/****************************************************************************/
	public static final char Z80_RDOP_ARG(int A) { return OP_RAM.read(A); }
	
	/****************************************************************************/
	/* Z80_RDSTACK() is identical to Z80_RDMEM() except it is used for reading  */
	/* stack variables. In case of system with memory mapped I/O, this function */
	/* can be used to slightly speed up emulation                               */
	/****************************************************************************/
	public static final char Z80_RDSTACK(int A) 
        {
            //return RAM[A];/* Galaga doesn't work with this */
            return (char)cpu_readmem16(A);
        }
	
	/****************************************************************************/
	/* Z80_WRSTACK() is identical to Z80_WRMEM() except it is used for writing  */
	/* stack variables. In case of system with memory mapped I/O, this function */
	/* can be used to slightly speed up emulation                               */
	/****************************************************************************/
	public static final void Z80_WRSTACK(int A, int V) 
        {
            //RAM[A] = (char) V; /* Galaga doesn't work with this */
            cpu_writemem16(A, (char)V);
        }
    	public  final int M_POP()
	{	int i = M_RDSTACK(R.SP) + (M_RDSTACK((R.SP + 1) & 65535) << 8);
		R.SP = (R.SP + 2) & 0xFFFF; return i;
        }
	public  final void M_PUSH(int Rg)
	{	R.SP = (R.SP - 2) & 0xFFFF;
		M_WRSTACK(R.SP, Rg & 0xFF);
		M_WRSTACK((R.SP + 1) & 65535, Rg >> 8);
        }
	public  final void M_CALL()
	{
		int q = M_RDMEM_OPCODE_WORD();
		M_PUSH(R.PC);
		R.PC = q;
		z80_ICount[0] -= 7;
	}
	public  final void M_JP()
	{	R.PC = M_RDOP_ARG(R.PC) + ((M_RDOP_ARG((R.PC + 1) & 65535)) << 8);

        }
	public  final void M_JR()
	{	R.PC = (R.PC + (byte) M_RDOP_ARG(R.PC) + 1) & 0xFFFF; z80_ICount[0]-=5;

        }
	public  final void M_RET()
        {
            R.PC = M_POP(); z80_ICount[0] -= 6;
        }
	public  final void M_RST(int Addr)
        {
            M_PUSH(R.PC); R.PC = Addr;
        }
	public  final int M_SET(int Bit, int Reg)
        {
            return Reg | (1 << Bit);
        }
	public  final int M_RES(int Bit, int Reg) //NEW!
        {
           // return Reg & ~(1 << Bit);
            return Reg & (1 << Bit ^ 0xFFFFFFFF);
        }
	public  final void M_BIT(int Bit, int Reg)
	{	R.F = (R.F & C_FLAG) | H_FLAG |
		(((Reg & (1 << Bit)) != 0) ? ((Bit == 7) ? S_FLAG : 0) : Z_FLAG);
          //TODO buggy check it..
         //R.F = (R.F & C_FLAG | H_FLAG | ((Reg & 1 << Bit) != 0 ? 0 : Bit == 7 ? S_FLAG : Z_FLAG));
        }
	public  final void M_AND(int Reg)
        {
            R.A &= Reg;
            R.F = ZSPTable[R.A] | H_FLAG;
        }
	public  final void M_OR(int Reg) 
        {
            R.A |= Reg;
            R.F = ZSPTable[R.A];
        }
	public  final void M_XOR(int Reg) 
        {
            R.A ^= Reg;
            R.F = ZSPTable[R.A];
        }
	public  final int M_IN()
	{	int Reg = Z80_In(R.BC.L); 
                R.F = (R.F & C_FLAG) | ZSPTable[Reg]; return Reg;
        }
	public  final void M_RLCA()
	{	R.A = ((R.A << 1) | ((R.A & 0x80) >> 7)) & 0xFF;
		R.F = (R.F & 0xEC) | (R.A & C_FLAG);
        }
	public  final void M_RRCA()
	{	R.F = (R.F & 0xEC) | (R.A & 0x01);
		R.A = ((R.A >> 1) | (R.A << 7)) & 0xFF;	}

	public  final void M_RLA()
	{
		int i;
		i = R.F & C_FLAG;
		R.F = (R.F & 0xEC) | ((R.A & 0x80) >> 7);
		R.A = ((R.A << 1) | i) & 0xFF;
	};
	public  final void M_RRA()
	{
		int i;
		i = R.F & C_FLAG;
		R.F = (R.F & 0xEC) | (R.A & 0x01);
		R.A = ((R.A >> 1) | (i << 7)) & 0xFF;
	};
	public  final int M_RLC(int Reg)
	{
		int q = Reg >> 7;
		Reg = ((Reg << 1) | q) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_RRC(int Reg)
	{
		int q= Reg & 1;
		Reg = ((Reg >> 1) | (q << 7)) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_RL(int Reg)
	{
		int q = Reg >> 7;
		Reg = ((Reg << 1) | (R.F & 1)) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_RR(int Reg)
	{
		int q = Reg & 1;
		Reg = ((Reg >> 1) | (R.F << 7)) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_SLL(int Reg)
	{
		int q = Reg >> 7;
		Reg = ((Reg << 1) | 1) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_SLA(int Reg)
	{
		int q = Reg >> 7;
		Reg = (Reg << 1) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_SRL(int Reg)
	{
		int q = Reg & 1;
		Reg = (Reg >> 1) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_SRA(int Reg)
	{
		int q = Reg & 1;
		Reg = ((Reg >> 1) | (Reg & 0x80)) & 0xFF;
		R.F = ZSPTable[Reg] | q;
		return Reg;
	}
	public  final int M_INC(int Reg)
	{
		Reg = (Reg + 1) & 0xFF;
		R.F = (R.F & C_FLAG) | ZSTable[Reg] |
			((Reg == 0x80) ? V_FLAG : 0) | ((Reg &0x0F) != 0 ? 0 : H_FLAG);
		return Reg;
	}
	public  final int M_DEC(int Reg)
	{
		R.F = (R.F & C_FLAG) | N_FLAG |
			((Reg == 0x80) ? V_FLAG : 0) | ((Reg & 0x0F) != 0 ? 0 : H_FLAG);
		Reg = (Reg - 1) & 0xFF;
 		R.F |= ZSTable[Reg];
		return Reg;
	}
	public  final void M_ADD(int Reg)
	{
		int q = R.A + Reg;
		R.F = ZSTable[q & 255] | ((q & 256) >> 8) |
		      ((R.A ^ q ^ Reg) & H_FLAG) |
		      (((Reg ^ R.A ^ 0x80) & (Reg ^ q) & 0x80) >> 5);
		R.A = q & 0xFF;
	}

	public  final void M_ADC(int Reg)
	{
		int q = R.A + Reg + (R.F & 1);
		R.F = ZSTable[q & 255] | ((q & 256) >> 8) |
		      ((R.A ^ q ^ Reg) & H_FLAG) |
		      (((Reg ^ R.A ^ 0x80) & (Reg ^ q) & 0x80) >> 5);
		R.A = q & 0xFF;
	}
	public  final void M_SUB(int Reg)
	{
		int q = R.A - Reg;
		R.F = ZSTable[q & 255] | ((q & 256) >> 8) | N_FLAG |
			((R.A ^ q ^ Reg) & H_FLAG) |
			(((Reg ^ R.A) & (Reg ^ q) & 0x80) >> 5);
		R.A = q & 0xFF;
	}
	public  final void M_SBC(int Reg)
	{
		int q;
		q = R.A - Reg - (R.F & 1);
		R.F = ZSTable[q & 255] | ((q & 256) >> 8) | N_FLAG |
			((R.A ^ q ^ Reg) & H_FLAG) |
			(((Reg ^ R.A) & (Reg ^ q) & 0x80) >> 5);
		R.A = q & 0xFF;
	}
	public  final void M_CP(int Reg)
	{
		int q = R.A - Reg;
	 	R.F = ZSTable[q & 255] | ((q & 256) >> 8) | N_FLAG |
	          ((R.A ^ q ^ Reg) & H_FLAG) |
	          (((Reg ^ R.A) & (Reg ^ q) & 0x80) >> 5);
	}
	public  final int M_ADDW(int Reg1, int Reg2)
	{
		int q = Reg1 + Reg2;
		R.F = (R.F & (S_FLAG | Z_FLAG | V_FLAG)) |
				(((Reg1 ^ q ^ Reg2) & 0x1000) >> 8) |
				((q >> 16) & 1);
		return q & 0xFFFF;
	}
	public  final void M_ADCW(int Reg)
	{
		int q = R.HL.W + Reg + (R.F & 1);
		R.F = (((R.HL.W ^ q ^ Reg) & 0x1000) >> 8) |
			((q >> 16) & 1) |
			((q & 0x8000) >> 8) |
			(((q & 65535) != 0) ? 0 : Z_FLAG) |
			(((Reg ^ R.HL.W ^ 0x8000) & (Reg ^ q) & 0x8000) >> 13);
		R.HL.SetW(q & 0xFFFF);
	}
	public  final void M_SBCW(int Reg)
	{
		int q = R.HL.W - Reg - (R.F & 1);
		R.F = (((R.HL.W ^ q ^ Reg) & 0x1000) >> 8) |
			  ((q >> 16) & 1) |
			  ((q & 0x8000) >> 8) |
			  (((q & 65535) != 0) ? 0 : Z_FLAG) |
			  (((Reg ^ R.HL.W) & (Reg ^ q) & 0x8000) >> 13) |
			  N_FLAG;
		R.HL.SetW(q & 0xFFFF);
	}
    	opcode_fn adc_a_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_ADC(i); } };
	opcode_fn adc_a_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_ADC(i); } };
	opcode_fn adc_a_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_ADC(i); } };
	opcode_fn adc_a_a = new opcode_fn() { public void handler() { M_ADC(R.A); } };
	opcode_fn adc_a_b = new opcode_fn() { public void handler() { M_ADC(R.BC.H); } };
	opcode_fn adc_a_c = new opcode_fn() { public void handler() { M_ADC(R.BC.L); } };
	opcode_fn adc_a_d = new opcode_fn() { public void handler() { M_ADC(R.DE.H); } };
	opcode_fn adc_a_e = new opcode_fn() { public void handler() { M_ADC(R.DE.L); } };
	opcode_fn adc_a_h = new opcode_fn() { public void handler() { M_ADC(R.HL.H); } };
	opcode_fn adc_a_l = new opcode_fn() { public void handler() { M_ADC(R.HL.L); } };
	opcode_fn adc_a_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_ADC(i); } };

	opcode_fn adc_hl_bc = new opcode_fn() { public void handler() { M_ADCW(R.BC.W); } };
	opcode_fn adc_hl_de = new opcode_fn() { public void handler() { M_ADCW(R.DE.W); } };
	opcode_fn adc_hl_hl = new opcode_fn() { public void handler() { M_ADCW(R.HL.W); } };
	opcode_fn adc_hl_sp = new opcode_fn() { public void handler() { M_ADCW(R.SP); } };

	opcode_fn add_a_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_ADD(i); } };
	opcode_fn add_a_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_ADD(i); } };
	opcode_fn add_a_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_ADD(i); } };
	opcode_fn add_a_a = new opcode_fn() { public void handler() { M_ADD(R.A); } };
	opcode_fn add_a_b = new opcode_fn() { public void handler() { M_ADD(R.BC.H); } };
	opcode_fn add_a_c = new opcode_fn() { public void handler() { M_ADD(R.BC.L); } };
	opcode_fn add_a_d = new opcode_fn() { public void handler() { M_ADD(R.DE.H); } };
	opcode_fn add_a_e = new opcode_fn() { public void handler() { M_ADD(R.DE.L); } };
	opcode_fn add_a_h = new opcode_fn() { public void handler() { M_ADD(R.HL.H); } };
	opcode_fn add_a_l = new opcode_fn() { public void handler() { M_ADD(R.HL.L); } };
	opcode_fn add_a_ixh = new opcode_fn() { public void handler() { M_ADD(R.IX.H); } };
        opcode_fn add_a_ixl = new opcode_fn() { public void handler() { M_ADD(R.IX.L); } } ;
	opcode_fn add_a_iyh = new opcode_fn() { public void handler() { M_ADD(R.IY.H); } };
	opcode_fn add_a_iyl = new opcode_fn() { public void handler() { M_ADD(R.IY.L); } };
	opcode_fn add_a_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_ADD(i); } };

	opcode_fn add_hl_bc = new opcode_fn() { public void handler() { R.HL.SetW(M_ADDW(R.HL.W, R.BC.W)); } };
	opcode_fn add_hl_de = new opcode_fn() { public void handler() { R.HL.SetW(M_ADDW(R.HL.W, R.DE.W)); } };
	opcode_fn add_hl_hl = new opcode_fn() { public void handler() { R.HL.SetW(M_ADDW(R.HL.W, R.HL.W)); } };
	opcode_fn add_hl_sp = new opcode_fn() { public void handler() { R.HL.SetW(M_ADDW(R.HL.W, R.SP)); } };
	opcode_fn add_ix_bc = new opcode_fn() { public void handler() { R.IX.SetW(M_ADDW(R.IX.W, R.BC.W)); } };
	opcode_fn add_ix_de = new opcode_fn() { public void handler() { R.IX.SetW(M_ADDW(R.IX.W, R.DE.W)); } };
	opcode_fn add_ix_ix = new opcode_fn() { public void handler() { R.IX.SetW(M_ADDW(R.IX.W, R.IX.W)); } };
	opcode_fn add_ix_sp = new opcode_fn() { public void handler() { R.IX.SetW(M_ADDW(R.IX.W, R.SP)); } };
	opcode_fn add_iy_bc = new opcode_fn() { public void handler() { R.IY.SetW(M_ADDW(R.IY.W, R.BC.W)); } };
	opcode_fn add_iy_de = new opcode_fn() { public void handler() { R.IY.SetW(M_ADDW(R.IY.W, R.DE.W)); } };
	opcode_fn add_iy_iy = new opcode_fn() { public void handler() { R.IY.SetW(M_ADDW(R.IY.W, R.IY.W)); } };
	opcode_fn add_iy_sp = new opcode_fn() { public void handler() { R.IY.SetW(M_ADDW(R.IY.W, R.SP)); } };

	opcode_fn and_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_AND(i); } };
	opcode_fn and_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_AND(i); } };
	opcode_fn and_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_AND(i); } };
	opcode_fn and_a = new opcode_fn() { public void handler() { R.F = ZSPTable[R.A] | H_FLAG; } };
	opcode_fn and_b = new opcode_fn() { public void handler() { M_AND(R.BC.H); } };
	opcode_fn and_c = new opcode_fn() { public void handler() { M_AND(R.BC.L); } };
	opcode_fn and_d = new opcode_fn() { public void handler() { M_AND(R.DE.H); } };
	opcode_fn and_e = new opcode_fn() { public void handler() { M_AND(R.DE.L); } };
	opcode_fn and_h = new opcode_fn() { public void handler() { M_AND(R.HL.H); } };
	opcode_fn and_l = new opcode_fn() { public void handler() { M_AND(R.HL.L); } };
	opcode_fn and_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_AND(i); } };

	opcode_fn bit_0_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(0, i); } };
	opcode_fn bit_0_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(0, i); } };
	opcode_fn bit_0_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(0, i); } };
	opcode_fn bit_0_a = new opcode_fn() { public void handler() { M_BIT(0, R.A); } };
	opcode_fn bit_0_b = new opcode_fn() { public void handler() { M_BIT(0, R.BC.H); } };
	opcode_fn bit_0_c = new opcode_fn() { public void handler() { M_BIT(0, R.BC.L); } };
	opcode_fn bit_0_d = new opcode_fn() { public void handler() { M_BIT(0, R.DE.H); } };
	opcode_fn bit_0_e = new opcode_fn() { public void handler() { M_BIT(0, R.DE.L); } };
	opcode_fn bit_0_h = new opcode_fn() { public void handler() { M_BIT(0, R.HL.H); } };
	opcode_fn bit_0_l = new opcode_fn() { public void handler() { M_BIT(0, R.HL.L); } };

	opcode_fn bit_1_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(1, i); } };
	opcode_fn bit_1_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(1, i); } };
	opcode_fn bit_1_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(1, i); } };
	opcode_fn bit_1_a = new opcode_fn() { public void handler() { M_BIT(1, R.A); } };
	opcode_fn bit_1_b = new opcode_fn() { public void handler() { M_BIT(1, R.BC.H); } };
	opcode_fn bit_1_c = new opcode_fn() { public void handler() { M_BIT(1, R.BC.L); } };
	opcode_fn bit_1_d = new opcode_fn() { public void handler() { M_BIT(1, R.DE.H); } };
	opcode_fn bit_1_e = new opcode_fn() { public void handler() { M_BIT(1, R.DE.L); } };
	opcode_fn bit_1_h = new opcode_fn() { public void handler() { M_BIT(1, R.HL.H); } };
	opcode_fn bit_1_l = new opcode_fn() { public void handler() { M_BIT(1, R.HL.L); } };

	opcode_fn bit_2_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(2, i); } };
	opcode_fn bit_2_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(2, i); } };
	opcode_fn bit_2_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(2, i); } };
	opcode_fn bit_2_a = new opcode_fn() { public void handler() { M_BIT(2, R.A); } };
	opcode_fn bit_2_b = new opcode_fn() { public void handler() { M_BIT(2, R.BC.H); } };
	opcode_fn bit_2_c = new opcode_fn() { public void handler() { M_BIT(2, R.BC.L); } };
	opcode_fn bit_2_d = new opcode_fn() { public void handler() { M_BIT(2, R.DE.H); } };
	opcode_fn bit_2_e = new opcode_fn() { public void handler() { M_BIT(2, R.DE.L); } };
	opcode_fn bit_2_h = new opcode_fn() { public void handler() { M_BIT(2, R.HL.H); } };
	opcode_fn bit_2_l = new opcode_fn() { public void handler() { M_BIT(2, R.HL.L); } };

	opcode_fn bit_3_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(3, i); } };
	opcode_fn bit_3_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(3, i); } };
	opcode_fn bit_3_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(3, i); } };
	opcode_fn bit_3_a = new opcode_fn() { public void handler() { M_BIT(3, R.A); } };
	opcode_fn bit_3_b = new opcode_fn() { public void handler() { M_BIT(3, R.BC.H); } };
	opcode_fn bit_3_c = new opcode_fn() { public void handler() { M_BIT(3, R.BC.L); } };
	opcode_fn bit_3_d = new opcode_fn() { public void handler() { M_BIT(3, R.DE.H); } };
	opcode_fn bit_3_e = new opcode_fn() { public void handler() { M_BIT(3, R.DE.L); } };
	opcode_fn bit_3_h = new opcode_fn() { public void handler() { M_BIT(3, R.HL.H); } };
	opcode_fn bit_3_l = new opcode_fn() { public void handler() { M_BIT(3, R.HL.L); } };

	opcode_fn bit_4_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(4, i); } };
	opcode_fn bit_4_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(4, i); } };
	opcode_fn bit_4_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(4, i); } };
	opcode_fn bit_4_a = new opcode_fn() { public void handler() { M_BIT(4, R.A); } };
	opcode_fn bit_4_b = new opcode_fn() { public void handler() { M_BIT(4, R.BC.H); } };
	opcode_fn bit_4_c = new opcode_fn() { public void handler() { M_BIT(4, R.BC.L); } };
	opcode_fn bit_4_d = new opcode_fn() { public void handler() { M_BIT(4, R.DE.H); } };
	opcode_fn bit_4_e = new opcode_fn() { public void handler() { M_BIT(4, R.DE.L); } };
	opcode_fn bit_4_h = new opcode_fn() { public void handler() { M_BIT(4, R.HL.H); } };
	opcode_fn bit_4_l = new opcode_fn() { public void handler() { M_BIT(4, R.HL.L); } };

	opcode_fn bit_5_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(5, i); } };
	opcode_fn bit_5_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(5, i); } };
	opcode_fn bit_5_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(5, i); } };
	opcode_fn bit_5_a = new opcode_fn() { public void handler() { M_BIT(5, R.A); } };
	opcode_fn bit_5_b = new opcode_fn() { public void handler() { M_BIT(5, R.BC.H); } };
	opcode_fn bit_5_c = new opcode_fn() { public void handler() { M_BIT(5, R.BC.L); } };
	opcode_fn bit_5_d = new opcode_fn() { public void handler() { M_BIT(5, R.DE.H); } };
	opcode_fn bit_5_e = new opcode_fn() { public void handler() { M_BIT(5, R.DE.L); } };
	opcode_fn bit_5_h = new opcode_fn() { public void handler() { M_BIT(5, R.HL.H); } };
	opcode_fn bit_5_l = new opcode_fn() { public void handler() { M_BIT(5, R.HL.L); } };

	opcode_fn bit_6_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(6, i); } };
	opcode_fn bit_6_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(6, i); } };
	opcode_fn bit_6_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(6, i); } };
	opcode_fn bit_6_a = new opcode_fn() { public void handler() { M_BIT(6, R.A); } };
	opcode_fn bit_6_b = new opcode_fn() { public void handler() { M_BIT(6, R.BC.H); } };
	opcode_fn bit_6_c = new opcode_fn() { public void handler() { M_BIT(6, R.BC.L); } };
	opcode_fn bit_6_d = new opcode_fn() { public void handler() { M_BIT(6, R.DE.H); } };
	opcode_fn bit_6_e = new opcode_fn() { public void handler() { M_BIT(6, R.DE.L); } };
	opcode_fn bit_6_h = new opcode_fn() { public void handler() { M_BIT(6, R.HL.H); } };
	opcode_fn bit_6_l = new opcode_fn() { public void handler() { M_BIT(6, R.HL.L); } };

	opcode_fn bit_7_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_BIT(7, i); } };
	opcode_fn bit_7_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_BIT(7, i); } };
	opcode_fn bit_7_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_BIT(7, i); } };
	opcode_fn bit_7_a = new opcode_fn() { public void handler() { M_BIT(7, R.A); } };
	opcode_fn bit_7_b = new opcode_fn() { public void handler() { M_BIT(7, R.BC.H); } };
	opcode_fn bit_7_c = new opcode_fn() { public void handler() { M_BIT(7, R.BC.L); } };
	opcode_fn bit_7_d = new opcode_fn() { public void handler() { M_BIT(7, R.DE.H); } };
	opcode_fn bit_7_e = new opcode_fn() { public void handler() { M_BIT(7, R.DE.L); } };
	opcode_fn bit_7_h = new opcode_fn() { public void handler() { M_BIT(7, R.HL.H); } };
	opcode_fn bit_7_l = new opcode_fn() { public void handler() { M_BIT(7, R.HL.L); } };

	opcode_fn call_c = new opcode_fn() { public void handler() { if (M_C()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call_m = new opcode_fn() { public void handler() { if (M_M()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call_nc = new opcode_fn() { public void handler() { if (M_NC()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call_nz = new opcode_fn() { public void handler() { if (M_NZ()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call_p = new opcode_fn() { public void handler() { if (M_P()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call_z = new opcode_fn() { public void handler() { if (M_Z()) { M_CALL(); } else { M_SKIP_CALL(); } } };
	opcode_fn call = new opcode_fn() { public void handler() { M_CALL(); } };

	opcode_fn ccf = new opcode_fn() { public void handler() { R.F = ((R.F & 0xED) | ((R.F & 1) << 4)) ^ 1; } };

	opcode_fn cp_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_CP(i); } };
	opcode_fn cp_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_CP(i); } };
	opcode_fn cp_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_CP(i); } };
	opcode_fn cp_a = new opcode_fn() { public void handler() { M_CP(R.A); } };
	opcode_fn cp_b = new opcode_fn() { public void handler() { M_CP(R.BC.H); } };
	opcode_fn cp_c = new opcode_fn() { public void handler() { M_CP(R.BC.L); } };
	opcode_fn cp_d = new opcode_fn() { public void handler() { M_CP(R.DE.H); } };
	opcode_fn cp_e = new opcode_fn() { public void handler() { M_CP(R.DE.L); } };
	opcode_fn cp_h = new opcode_fn() { public void handler() { M_CP(R.HL.H); } };
	opcode_fn cp_l = new opcode_fn() { public void handler() { M_CP(R.HL.L); } };
	opcode_fn cp_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_CP(i); } };

	opcode_fn cpdr = new opcode_fn() { public void handler()
	{
		int i, j;
	 	R.R -= 2;
	 	do
	 	{
	  		R.R += 2;
	  		i = M_RDMEM(R.HL.W);
	  		j = (R.A - i) & 0xFF;
			R.HL.AddW(-1);
			R.BC.AddW(-1);
	 		z80_ICount[0] -= 21;
	 	}
	 	while (R.BC.W != 0 && j != 0 && z80_ICount[0] > 0);
	 	R.F = (R.F & C_FLAG) | ZSTable[j] |
	          ((R.A ^ i ^ j) & H_FLAG) | (R.BC.W != 0 ? V_FLAG : 0) | N_FLAG;
	 	if (R.BC.W != 0 && j != 0) R.PC = (R.PC - 2) & 0xFFFF;
		else z80_ICount[0] += 5;
	} };

	opcode_fn cpi = new opcode_fn() { public void handler()
	{
		int i, j;
		i = M_RDMEM(R.HL.W);
		j = (R.A - i) & 0xFF;
		R.HL.AddW(1);
		R.BC.AddW(-1);
		R.F = (R.F & C_FLAG) | ZSTable[j] |
			  ((R.A ^ i ^ j) & H_FLAG) | (R.BC.W != 0 ? V_FLAG : 0) | N_FLAG;
	} };

	opcode_fn cpir = new opcode_fn() { public void handler()
	{
		int i, j;
		R.R -= 2;
		do
		{
			R.R += 2;
			i = M_RDMEM(R.HL.W);
			j = (R.A - i) & 0xFF;
			R.HL.AddW(1);
			R.BC.AddW(-1);
			z80_ICount[0] -= 21;
		}
		while (R.BC.W != 0 && j != 0 && z80_ICount[0] > 0);
		R.F = (R.F & C_FLAG) | ZSTable[j] |
			  ((R.A ^ i ^ j) & H_FLAG) | (R.BC.W != 0 ? V_FLAG : 0) | N_FLAG;
		if (R.BC.W != 0 && j != 0) R.PC = (R.PC - 2) & 0xFFFF;
		else z80_ICount[0] += 5;
	} };

	opcode_fn cpl = new opcode_fn() { public void handler() { R.A ^= 0xFF; R.F |= (H_FLAG | N_FLAG); } };

	opcode_fn daa = new opcode_fn() { public void handler()
	{
		int i;
		i = R.A;
		if ((R.F & C_FLAG) != 0) i |= 256;
		if ((R.F & H_FLAG) != 0) i |= 512;
		if ((R.F & N_FLAG) != 0) i |= 1024;
		R.A = ((char) DAATable[i]) >> 8;
		R.F = ((char) DAATable[i]) & 0xFF;
	} };

	opcode_fn dec_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_DEC(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn dec_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_DEC(i);
		M_WRMEM(j, i);
	} };
	opcode_fn dec_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_DEC(i);
		M_WRMEM(j, i);
	} };
	opcode_fn dec_a = new opcode_fn() { public void handler() { R.A = M_DEC(R.A); } };
	opcode_fn dec_b = new opcode_fn() { public void handler() { R.BC.SetH(M_DEC(R.BC.H)); } };
	opcode_fn dec_c = new opcode_fn() { public void handler() { R.BC.SetL(M_DEC(R.BC.L)); } };
	opcode_fn dec_d = new opcode_fn() { public void handler() { R.DE.SetH(M_DEC(R.DE.H)); } };
	opcode_fn dec_e = new opcode_fn() { public void handler() { R.DE.SetL(M_DEC(R.DE.L)); } };
	opcode_fn dec_h = new opcode_fn() { public void handler() { R.HL.SetH(M_DEC(R.HL.H)); } };
	opcode_fn dec_l = new opcode_fn() { public void handler() { R.HL.SetL(M_DEC(R.HL.L)); } };
	opcode_fn dec_ixl = new opcode_fn() { public void handler() { R.IX.SetL(M_DEC(R.IX.L)); } } ;
        opcode_fn dec_iyh = new opcode_fn() { public void handler() { R.IY.SetH(M_DEC(R.IY.H)); } };
	opcode_fn dec_iyl = new opcode_fn() { public void handler() { R.IY.SetL(M_DEC(R.IY.L)); } };

	opcode_fn dec_bc = new opcode_fn() { public void handler() { R.BC.AddW(-1); } };
	opcode_fn dec_de = new opcode_fn() { public void handler() { R.DE.AddW(-1); } };
	opcode_fn dec_hl = new opcode_fn() { public void handler() { R.HL.AddW(-1); } };
	opcode_fn dec_ix = new opcode_fn() { public void handler() { R.IX.AddW(-1); } };
	opcode_fn dec_iy = new opcode_fn() { public void handler() { R.IY.AddW(-1); } };
	opcode_fn dec_sp = new opcode_fn() { public void handler() { R.SP = (R.SP - 1) & 0xFFFF; } };

	opcode_fn di = new opcode_fn() {	public void handler() { R.IFF1 = R.IFF2 = 0; } };

	opcode_fn djnz = new opcode_fn() { public void handler() { R.BC.AddH(-1); if (R.BC.H != 0) { M_JR(); } else { M_SKIP_JR(); } } };

	opcode_fn ex_xsp_hl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM_WORD(R.SP);
		M_WRMEM_WORD(R.SP, R.HL.W);
		R.HL.SetW(i);
	} };

	opcode_fn ex_xsp_ix = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM_WORD(R.SP);
		M_WRMEM_WORD(R.SP, R.IX.W);
		R.IX.SetW(i);
	} };

	opcode_fn ex_xsp_iy = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM_WORD(R.SP);
		M_WRMEM_WORD(R.SP, R.IY.W);
		R.IY.SetW(i);
	} };

	opcode_fn ex_af_af = new opcode_fn() { public void handler()
	{
		int i;
		i = (R.A << 8) | R.F;
		R.A = (R.AF2 >> 8);
		R.F = (R.AF2 & 0xFF);
		R.AF2 = i;
	} };

	opcode_fn ex_de_hl = new opcode_fn() { public void handler()
	{
		int i;
		i = R.DE.W;
		R.DE.SetW(R.HL.W);
		R.HL.SetW(i);
	} };

	opcode_fn exx = new opcode_fn() { public void handler()
	{
		int i;
		i = R.BC.W;
		R.BC.SetW(R.BC2);
		R.BC2 = i;
		i = R.DE.W;
		R.DE.SetW(R.DE2);
		R.DE2 = i;
		i = R.HL.W;
		R.HL.SetW(R.HL2);
		R.HL2 = i;
	} };

	opcode_fn halt = new opcode_fn() { public void handler()
	{
		R.PC = (R.PC - 1) & 0xFFFF;
		R.HALT = 1;
		if (z80_ICount[0] > 0) z80_ICount[0] = 0;
	} };

	opcode_fn im_0 = new opcode_fn() { public void handler() { R.IM = 0; } };
	opcode_fn im_1 = new opcode_fn() { public void handler() { R.IM = 1; } };
	opcode_fn im_2 = new opcode_fn() { public void handler() { R.IM = 2; } };

	opcode_fn in_a_c = new opcode_fn() { public void handler() { R.A = M_IN(); } };
	opcode_fn in_c_c = new opcode_fn() { public void handler() { R.BC.SetL(M_IN()); } };
        opcode_fn in_b_c = new opcode_fn() { public void handler() { R.BC.SetH(M_IN()); } } ;
        opcode_fn in_e_c = new opcode_fn() { public void handler() { R.DE.SetL(M_IN()); } } ;
        opcode_fn in_l_c = new opcode_fn() { public void handler() { R.HL.SetL(M_IN()); } } ;

        opcode_fn in_a_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); R.A = Z80_In(i); } };

	opcode_fn inc_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_INC(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn inc_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_INC(i);
		M_WRMEM(j, i);
	} };
	opcode_fn inc_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_INC(i);
		M_WRMEM(j, i);
	} };
	opcode_fn inc_a = new opcode_fn() { public void handler() { R.A = M_INC(R.A); } };
	opcode_fn inc_b = new opcode_fn() { public void handler() { R.BC.SetH(M_INC(R.BC.H)); } };
	opcode_fn inc_c = new opcode_fn() { public void handler() { R.BC.SetL(M_INC(R.BC.L)); } };
	opcode_fn inc_d = new opcode_fn() { public void handler() { R.DE.SetH(M_INC(R.DE.H)); } };
	opcode_fn inc_e = new opcode_fn() { public void handler() { R.DE.SetL(M_INC(R.DE.L)); } };
	opcode_fn inc_h = new opcode_fn() { public void handler() { R.HL.SetH(M_INC(R.HL.H)); } };
	opcode_fn inc_l = new opcode_fn() { public void handler() { R.HL.SetL(M_INC(R.HL.L)); } };
	opcode_fn inc_ixl = new opcode_fn() { public void handler() { R.IX.SetL(M_INC(R.IX.L)); } };

	opcode_fn inc_bc = new opcode_fn() {	public void handler() { R.BC.AddW(1); } };
	opcode_fn inc_de = new opcode_fn() {	public void handler() { R.DE.AddW(1); } };
	opcode_fn inc_hl = new opcode_fn() {	public void handler() { R.HL.AddW(1); } };
	opcode_fn inc_ix = new opcode_fn() {	public void handler() { R.IX.AddW(1); } };
	opcode_fn inc_iy = new opcode_fn() {	public void handler() { R.IY.AddW(1); } };
	opcode_fn inc_sp = new opcode_fn() {	public void handler() { R.SP = (R.SP + 1) & 0xFFFF; } };

	opcode_fn jp = new opcode_fn() //TODO changed for v0.27 to be checked
        {
            public void handler()
            {
               // M_JP();
                 int i = R.PC - 1;
                 M_JP();
                 int j = R.PC;
                if (j == i)
                {
                    if (z80_ICount[0] > 0) z80_ICount[0] = 0;/* speed up busy loop */
                }
                else if ((j == i - 3) && (M_RDOP(j) == 0x31))/* LD SP,#xxxx - Galaga */
                {
                    if (z80_ICount[0] > 10) z80_ICount[0] = 10;
                 }

            }
        };
	opcode_fn jp_hl = new opcode_fn() { public void handler() { R.PC = R.HL.W; } };
	opcode_fn jp_ix = new opcode_fn() { public void handler() { R.PC = R.IX.W; } };
	opcode_fn jp_iy = new opcode_fn() { public void handler() { R.PC = R.IY.W; } };
	opcode_fn jp_c = new opcode_fn() { public void handler() { if (M_C()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_m = new opcode_fn() { public void handler() { if (M_M()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_nc = new opcode_fn() { public void handler() { if (M_NC()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_nz = new opcode_fn() { public void handler() { if (M_NZ()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_p = new opcode_fn() { public void handler() { if (M_P()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_pe = new opcode_fn() { public void handler() { if (M_PE()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_po = new opcode_fn() { public void handler() { if (M_PO()) { M_JP(); } else { M_SKIP_JP(); } } };
	opcode_fn jp_z = new opcode_fn() { public void handler() { if (M_Z()) { M_JP(); } else { M_SKIP_JP(); } } };

	opcode_fn jr = new opcode_fn() //TODO changed for v0.27 to be checked
        {
            public void handler()
            {
               // M_JR();
               int i = R.PC - 1;
               M_JR();
               int j = R.PC;
               if (j == i)
               {
                  if (z80_ICount[0] > 0) z80_ICount[0] = 0;/* speed up busy loop */
               }
               else if ((j == i - 1) && (M_RDOP(j) == 0xfb))/* EI - 1942 */
               {
                   if (z80_ICount[0] > 4) z80_ICount[0] = 4;
               }
            }
        };
	opcode_fn jr_c = new opcode_fn() { public void handler() { if (M_C()) { M_JR(); } else { M_SKIP_JR(); } } };
	opcode_fn jr_nc = new opcode_fn() { public void handler() { if (M_NC()) { M_JR(); } else { M_SKIP_JR(); } } };
	opcode_fn jr_nz = new opcode_fn() { public void handler() { if (M_NZ()) { M_JR(); } else { M_SKIP_JR(); } } };
	opcode_fn jr_z = new opcode_fn() { public void handler() { if (M_Z()) { M_JR(); } else { M_SKIP_JR(); } } };

	opcode_fn ld_xbc_a = new opcode_fn() { public void handler() { M_WRMEM(R.BC.W, R.A); } };
	opcode_fn ld_xde_a = new opcode_fn() { public void handler() { M_WRMEM(R.DE.W, R.A); } };
	opcode_fn ld_xhl_a = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.A); } };
	opcode_fn ld_xhl_b = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.BC.H); } };
	opcode_fn ld_xhl_c = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.BC.L); } };
	opcode_fn ld_xhl_d = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.DE.H); } };
	opcode_fn ld_xhl_e = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.DE.L); } };
	opcode_fn ld_xhl_h = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.HL.H); } };
	opcode_fn ld_xhl_l = new opcode_fn() { public void handler() { M_WRMEM(R.HL.W, R.HL.L); } };
	opcode_fn ld_xhl_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_WRMEM(R.HL.W, i); } };
	opcode_fn ld_xix_a = new opcode_fn() { public void handler() { M_WR_XIX(R.A); } };
	opcode_fn ld_xix_b = new opcode_fn() { public void handler() { M_WR_XIX(R.BC.H); } };
	opcode_fn ld_xix_c = new opcode_fn() { public void handler() { M_WR_XIX(R.BC.L); } };
	opcode_fn ld_xix_d = new opcode_fn() { public void handler() { M_WR_XIX(R.DE.H); } };
	opcode_fn ld_xix_e = new opcode_fn() { public void handler() { M_WR_XIX(R.DE.L); } };
	opcode_fn ld_xix_h = new opcode_fn() { public void handler() { M_WR_XIX(R.HL.H); } };
	opcode_fn ld_xix_l = new opcode_fn() { public void handler() { M_WR_XIX(R.HL.L); } };
	opcode_fn ld_xix_byte = new opcode_fn() { public void handler()
	{
		int i, j;
		i = M_XIX();
		j = M_RDMEM_OPCODE();
		M_WRMEM(i, j);
	} };
	opcode_fn ld_xiy_a = new opcode_fn() { public void handler() { M_WR_XIY(R.A); } };
	opcode_fn ld_xiy_b = new opcode_fn() { public void handler() { M_WR_XIY(R.BC.H); } };
	opcode_fn ld_xiy_c = new opcode_fn() { public void handler() { M_WR_XIY(R.BC.L); } };
	opcode_fn ld_xiy_d = new opcode_fn() { public void handler() { M_WR_XIY(R.DE.H); } };
	opcode_fn ld_xiy_e = new opcode_fn() { public void handler() { M_WR_XIY(R.DE.L); } };
	opcode_fn ld_xiy_h = new opcode_fn() { public void handler() { M_WR_XIY(R.HL.H); } };
	opcode_fn ld_xiy_l = new opcode_fn() { public void handler() { M_WR_XIY(R.HL.L); } };
	opcode_fn ld_xiy_byte = new opcode_fn() { public void handler()
	{
		int i, j;
		i = M_XIY();
		j = M_RDMEM_OPCODE();
		M_WRMEM(i, j);
	} };
	opcode_fn ld_xbyte_a = new opcode_fn() { public void handler()
	{ int i = M_RDMEM_OPCODE_WORD(); M_WRMEM(i, R.A); } };
	opcode_fn ld_xword_bc = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.BC.W); } };
	opcode_fn ld_xword_de = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.DE.W); } };
	opcode_fn ld_xword_hl = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.HL.W); } };
	opcode_fn ld_xword_ix = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.IX.W); } };
	opcode_fn ld_xword_iy = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.IY.W); } };
	opcode_fn ld_xword_sp = new opcode_fn() { public void handler() { M_WRMEM_WORD(M_RDMEM_OPCODE_WORD(), R.SP); } };
	opcode_fn ld_a_xbc = new opcode_fn() { public void handler() { R.A = M_RDMEM(R.BC.W); } };
	opcode_fn ld_a_xde = new opcode_fn() { public void handler() { R.A = M_RDMEM(R.DE.W); } };
	opcode_fn ld_a_xhl = new opcode_fn() { public void handler() { R.A = M_RD_XHL(); } };
	opcode_fn ld_a_xix = new opcode_fn() { public void handler() { R.A = M_RD_XIX(); } };
	opcode_fn ld_a_xiy = new opcode_fn() { public void handler() { R.A = M_RD_XIY(); } };
	opcode_fn ld_a_xbyte = new opcode_fn() { public void handler()
	{ int i = M_RDMEM_OPCODE_WORD(); R.A = M_RDMEM(i); } };

	opcode_fn ld_a_byte = new opcode_fn() { public void handler() { R.A = M_RDMEM_OPCODE(); } };
	opcode_fn ld_b_byte = new opcode_fn() { public void handler() { R.BC.SetH(M_RDMEM_OPCODE()); } };
	opcode_fn ld_c_byte = new opcode_fn() { public void handler() { R.BC.SetL(M_RDMEM_OPCODE()); } };
	opcode_fn ld_d_byte = new opcode_fn() { public void handler() { R.DE.SetH(M_RDMEM_OPCODE()); } };
	opcode_fn ld_e_byte = new opcode_fn() { public void handler() { R.DE.SetL(M_RDMEM_OPCODE()); } };
	opcode_fn ld_h_byte = new opcode_fn() { public void handler() { R.HL.SetH(M_RDMEM_OPCODE()); } };
	opcode_fn ld_l_byte = new opcode_fn() { public void handler() { R.HL.SetL(M_RDMEM_OPCODE()); } };
	opcode_fn ld_ixh_byte = new opcode_fn() { public void handler() { R.IX.SetH(M_RDMEM_OPCODE()); } } ;
        opcode_fn ld_ixl_byte = new opcode_fn() { public void handler() { R.IX.SetL(M_RDMEM_OPCODE()); } } ;
        opcode_fn ld_iyh_byte = new opcode_fn() { public void handler() { R.IY.SetH(M_RDMEM_OPCODE()); } };
	opcode_fn ld_iyl_byte = new opcode_fn() { public void handler() { R.IY.SetL(M_RDMEM_OPCODE()); } };

	opcode_fn ld_b_xhl = new opcode_fn() { public void handler() { R.BC.SetH(M_RD_XHL()); } };
	opcode_fn ld_c_xhl = new opcode_fn() { public void handler() { R.BC.SetL(M_RD_XHL()); } };
	opcode_fn ld_d_xhl = new opcode_fn() { public void handler() { R.DE.SetH(M_RD_XHL()); } };
	opcode_fn ld_e_xhl = new opcode_fn() { public void handler() { R.DE.SetL(M_RD_XHL()); } };
	opcode_fn ld_h_xhl = new opcode_fn() { public void handler() { R.HL.SetH(M_RD_XHL()); } };
	opcode_fn ld_l_xhl = new opcode_fn() { public void handler() { R.HL.SetL(M_RD_XHL()); } };
	opcode_fn ld_b_xix = new opcode_fn() { public void handler() { R.BC.SetH(M_RD_XIX()); } };
	opcode_fn ld_c_xix = new opcode_fn() { public void handler() { R.BC.SetL(M_RD_XIX()); } };
	opcode_fn ld_d_xix = new opcode_fn() { public void handler() { R.DE.SetH(M_RD_XIX()); } };
	opcode_fn ld_e_xix = new opcode_fn() { public void handler() { R.DE.SetL(M_RD_XIX()); } };
	opcode_fn ld_h_xix = new opcode_fn() { public void handler() { R.HL.SetH(M_RD_XIX()); } };
	opcode_fn ld_l_xix = new opcode_fn() { public void handler() { R.HL.SetL(M_RD_XIX()); } };
	opcode_fn ld_b_xiy = new opcode_fn() { public void handler() { R.BC.SetH(M_RD_XIY()); } };
	opcode_fn ld_c_xiy = new opcode_fn() { public void handler() { R.BC.SetL(M_RD_XIY()); } };
	opcode_fn ld_d_xiy = new opcode_fn() { public void handler() { R.DE.SetH(M_RD_XIY()); } };
	opcode_fn ld_e_xiy = new opcode_fn() { public void handler() { R.DE.SetL(M_RD_XIY()); } };
	opcode_fn ld_h_xiy = new opcode_fn() { public void handler() { R.HL.SetH(M_RD_XIY()); } };
	opcode_fn ld_l_xiy = new opcode_fn() { public void handler() { R.HL.SetL(M_RD_XIY()); } };
	opcode_fn ld_a_a = new opcode_fn() { public void handler() 
        {
           System.out.println("Z80: Unsupported ld_a_a instruction!");
        } };
	opcode_fn ld_a_b = new opcode_fn() { public void handler() { R.A = R.BC.H; } };
	opcode_fn ld_a_c = new opcode_fn() { public void handler() { R.A = R.BC.L; } };
	opcode_fn ld_a_d = new opcode_fn() { public void handler() { R.A = R.DE.H; } };
	opcode_fn ld_a_e = new opcode_fn() { public void handler() { R.A = R.DE.L; } };
	opcode_fn ld_a_h = new opcode_fn() { public void handler() { R.A = R.HL.H; } };
	opcode_fn ld_a_l = new opcode_fn() { public void handler() { R.A = R.HL.L; } };
        opcode_fn ld_a_ixh = new opcode_fn() { public void handler() { R.A = R.IX.H; } } ;
	opcode_fn ld_a_ixl = new opcode_fn() { public void handler() { R.A = R.IX.L; } };
	opcode_fn ld_a_iyh = new opcode_fn() { public void handler() { R.A = R.IY.H; } };
	opcode_fn ld_a_iyl = new opcode_fn() { public void handler() { R.A = R.IY.L; } };
	opcode_fn ld_b_b = new opcode_fn() {	public void handler()
        {
            System.out.println("Z80: Unsupported ld_b_b instruction!");
        } };

	opcode_fn ld_b_a = new opcode_fn() {	public void handler() { R.BC.SetH(R.A); } };
	opcode_fn ld_b_c = new opcode_fn() {	public void handler() { R.BC.SetH(R.BC.L); } };
	opcode_fn ld_b_d = new opcode_fn() {	public void handler() { R.BC.SetH(R.DE.H); } };
	opcode_fn ld_b_e = new opcode_fn() {	public void handler() { R.BC.SetH(R.DE.L); } };
	opcode_fn ld_b_h = new opcode_fn() {	public void handler() { R.BC.SetH(R.HL.H); } };
	opcode_fn ld_b_l = new opcode_fn() {	public void handler() { R.BC.SetH(R.HL.L); } };
	opcode_fn ld_c_c = new opcode_fn() {	public void handler() 
        {
           System.out.println("Z80: Unsupported ld_c_c instruction!");
        } };

	opcode_fn ld_c_a = new opcode_fn() {	public void handler() { R.BC.SetL(R.A); } };
	opcode_fn ld_c_b = new opcode_fn() {	public void handler() { R.BC.SetL(R.BC.H); } };
	opcode_fn ld_c_d = new opcode_fn() {	public void handler() { R.BC.SetL(R.DE.H); } };
	opcode_fn ld_c_e = new opcode_fn() {	public void handler() { R.BC.SetL(R.DE.L); } };
	opcode_fn ld_c_h = new opcode_fn() {	public void handler() { R.BC.SetL(R.HL.H); } };
        opcode_fn ld_c_l = new opcode_fn() {	public void handler() { R.BC.SetL(R.HL.L); } };
	opcode_fn ld_c_ixh = new opcode_fn() { public void handler() { R.BC.SetL(R.IX.H); } } ;
        opcode_fn ld_d_d = new opcode_fn() {	public void handler()
        {
           System.out.println("Z80: Unsupported ld_d_d instruction!");
        } };

	opcode_fn ld_d_a = new opcode_fn() {	public void handler() { R.DE.SetH(R.A); } };
	opcode_fn ld_d_b = new opcode_fn() {	public void handler() { R.DE.SetH(R.BC.H);} };
	opcode_fn ld_d_c = new opcode_fn() {	public void handler() { R.DE.SetH(R.BC.L); } };
	opcode_fn ld_d_e = new opcode_fn() {	public void handler() { R.DE.SetH(R.DE.L); } };
	opcode_fn ld_d_h = new opcode_fn() {	public void handler() { R.DE.SetH(R.HL.H); } };
	opcode_fn ld_d_l = new opcode_fn() {	public void handler() { R.DE.SetH(R.HL.L); } };
	opcode_fn ld_d_iyh = new opcode_fn() { public void handler() { R.DE.SetH(R.IY.H); } };
	opcode_fn ld_d_iyl = new opcode_fn() { public void handler() { R.DE.SetH(R.IY.L); } };
	opcode_fn ld_e_e = new opcode_fn() {	public void handler() 
        {
          System.out.println("Z80: Unsupported ld_e_e instruction!");
        } };
	opcode_fn ld_e_a = new opcode_fn() {	public void handler() { R.DE.SetL(R.A); } };
	opcode_fn ld_e_b = new opcode_fn() {	public void handler() { R.DE.SetL(R.BC.H); } };
	opcode_fn ld_e_c = new opcode_fn() {	public void handler() { R.DE.SetL(R.BC.L); } };
	opcode_fn ld_e_d = new opcode_fn() {	public void handler() { R.DE.SetL(R.DE.H); } };
	opcode_fn ld_e_h = new opcode_fn() {	public void handler() { R.DE.SetL(R.HL.H); } };
	opcode_fn ld_e_l = new opcode_fn() {	public void handler() { R.DE.SetL(R.HL.L); } };
	opcode_fn ld_e_ixl = new opcode_fn() { public void handler() { R.DE.SetL(R.IX.L); } } ;
        opcode_fn ld_e_iyh = new opcode_fn() { public void handler() { R.DE.SetL(R.IY.H); } };
	opcode_fn ld_e_iyl = new opcode_fn() { public void handler() { R.DE.SetL(R.IY.L); } };
	opcode_fn ld_h_h = new opcode_fn() {	public void handler() 
        {
           System.out.println("Z80: Unsupported ld_h_h instruction!");
        } };

	opcode_fn ld_h_a = new opcode_fn() {	public void handler() { R.HL.SetH(R.A); } };
	opcode_fn ld_h_b = new opcode_fn() {	public void handler() { R.HL.SetH(R.BC.H); } };
	opcode_fn ld_h_c = new opcode_fn() {	public void handler() { R.HL.SetH(R.BC.L); } };
	opcode_fn ld_h_d = new opcode_fn() {	public void handler() { R.HL.SetH(R.DE.H); } };
	opcode_fn ld_h_e = new opcode_fn() {	public void handler() { R.HL.SetH(R.DE.L); } };
	opcode_fn ld_h_l = new opcode_fn() {	public void handler() { R.HL.SetH(R.HL.L); } };
	opcode_fn ld_l_l = new opcode_fn() {	public void handler() 
        {
            System.out.println("Z80: Unsupported ld_l_l instruction!");
        } };

        opcode_fn ld_l_a = new opcode_fn() {	public void handler() { R.HL.SetL(R.A); } };
	opcode_fn ld_l_b = new opcode_fn() {	public void handler() { R.HL.SetL(R.BC.H); } };
	opcode_fn ld_l_c = new opcode_fn() {	public void handler() { R.HL.SetL(R.BC.L); } };
	opcode_fn ld_l_d = new opcode_fn() {	public void handler() { R.HL.SetL(R.DE.H); } };
	opcode_fn ld_l_e = new opcode_fn() {	public void handler() { R.HL.SetL(R.DE.L); } };
	opcode_fn ld_l_h = new opcode_fn() {	public void handler() { R.HL.SetL(R.HL.H); } };
	opcode_fn ld_ixl_a = new opcode_fn() { public void handler() { R.IX.SetL(R.A); } };
	opcode_fn ld_iyh_a = new opcode_fn() { public void handler() { R.IY.SetH(R.A); } };
	opcode_fn ld_iyl_a = new opcode_fn() { public void handler() { R.IY.SetL(R.A); } };
        opcode_fn ld_ixh_a = new opcode_fn() { public void handler() { R.IX.SetH(R.A); } } ;
        opcode_fn ld_ixh_b = new opcode_fn() { public void handler() { R.IX.SetH(R.BC.H); } } ;
        opcode_fn ld_ixh_c = new opcode_fn() { public void handler() { R.IX.SetH(R.BC.L); } } ;
        opcode_fn ld_ixh_d = new opcode_fn() { public void handler() { R.IX.SetH(R.DE.H); } } ;
        opcode_fn ld_ixh_e = new opcode_fn() { public void handler() { R.IX.SetH(R.DE.L); } } ;

	opcode_fn ld_bc_xword = new opcode_fn() { public void handler() { R.BC.SetW(M_RDMEM_WORD(M_RDMEM_OPCODE_WORD())); } };
	opcode_fn ld_bc_word = new opcode_fn() { public void handler() { R.BC.SetW(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_de_xword = new opcode_fn() { public void handler() { R.DE.SetW(M_RDMEM_WORD(M_RDMEM_OPCODE_WORD())); } };
	opcode_fn ld_de_word = new opcode_fn() { public void handler() { R.DE.SetW(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_hl_xword = new opcode_fn() { public void handler() { R.HL.SetW(M_RDMEM_WORD(M_RDMEM_OPCODE_WORD())); } };
	opcode_fn ld_hl_word = new opcode_fn() { public void handler() { R.HL.SetW(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_ix_xword = new opcode_fn() { public void handler() { R.IX.SetW(M_RDMEM_WORD(M_RDMEM_OPCODE_WORD())); } };
	opcode_fn ld_ix_word = new opcode_fn() { public void handler() { R.IX.SetW(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_iy_xword = new opcode_fn() { public void handler() { R.IY.SetW(M_RDMEM_WORD(M_RDMEM_OPCODE_WORD())); } };
	opcode_fn ld_iy_word = new opcode_fn() { public void handler() { R.IY.SetW(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_sp_xword = new opcode_fn() { public void handler() { R.SP = M_RDMEM_WORD(M_RDMEM_OPCODE_WORD()); } };
	opcode_fn ld_sp_word = new opcode_fn() { public void handler() { R.SP = M_RDMEM_OPCODE_WORD(); } };
	opcode_fn ld_sp_hl = new opcode_fn() {	public void handler() { R.SP = R.HL.W; } };
	opcode_fn ld_sp_ix = new opcode_fn() {	public void handler() { R.SP = R.IX.W; } };
	opcode_fn ld_sp_iy = new opcode_fn() {	public void handler() { R.SP = R.IY.W; } };
	opcode_fn ld_a_i = new opcode_fn() {	public void handler()
	{
		R.A = R.I;
		R.F = (R.F & C_FLAG) | ZSTable[R.I] | (R.IFF2 << 2);
	} };
	opcode_fn ld_i_a = new opcode_fn() {	public void handler() { R.I = R.A; } };
	opcode_fn ld_a_r = new opcode_fn() {	public void handler()
	{
		 R.A = (R.R & 127) | (R.R2 & 128);
		 R.F = (R.F & C_FLAG) | ZSTable[R.A] | (R.IFF2 << 2);
	} };
	opcode_fn ld_r_a = new opcode_fn() {	public void handler() { R.R = R.R2 = R.A; } };

	opcode_fn ldd = new opcode_fn() { public void handler()
	{
		M_WRMEM(R.DE.W, M_RDMEM(R.HL.W));
		R.DE.AddW(-1);
		R.HL.AddW(-1);
		R.BC.AddW(-1);
		R.F = (R.F & 0xE9) | (R.BC.W != 0 ? V_FLAG : 0);
	} };

	opcode_fn lddr = new opcode_fn() { public void handler()
	{
		R.R -= 2;
		do
		{
			R.R += 2;
			M_WRMEM(R.DE.W, M_RDMEM(R.HL.W));
			R.DE.AddW(-1);
			R.HL.AddW(-1);
			R.BC.AddW(-1);
			z80_ICount[0] -= 21;
		}
		while (R.BC.W != 0 && z80_ICount[0] > 0);
		R.F = (R.F & 0xE9) | (R.BC.W != 0 ? V_FLAG : 0);
		if (R.BC.W != 0) R.PC = (R.PC - 2) & 0xFFFF;
		else z80_ICount[0] += 5;
	 
	} };
	opcode_fn ldi = new opcode_fn() { public void handler()
	{
		M_WRMEM(R.DE.W, M_RDMEM(R.HL.W));
		R.DE.AddW(1);
		R.HL.AddW(1);
		R.BC.AddW(-1);
		R.F = (R.F & 0xE9) | (R.BC.W != 0 ? V_FLAG : 0);
	} };
	opcode_fn ldir = new opcode_fn() { public void handler()
	{
		R.R -= 2;
		do
		{
			R.R += 2;
			M_WRMEM(R.DE.W, M_RDMEM(R.HL.W));
			R.DE.AddW(1);
			R.HL.AddW(1);
			R.BC.AddW(-1);
			z80_ICount[0] -= 21;
		}
		while (R.BC.W != 0 && z80_ICount[0] > 0);
		R.F = (R.F & 0xE9) | (R.BC.W != 0 ? V_FLAG : 0);
		if (R.BC.W != 0) R.PC = (R.PC - 2) & 0xFFFF;
		else z80_ICount[0] += 5;
	} };
	opcode_fn neg = new opcode_fn() { public void handler()
	{
		int i;
		i = R.A;
		R.A = 0;
		M_SUB(i);
	} };

	opcode_fn nop = new opcode_fn() { public void handler() { } };

	opcode_fn or_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_OR(i); } };
	opcode_fn or_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_OR(i); } };
	opcode_fn or_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_OR(i); } };
	opcode_fn or_a = new opcode_fn() { public void handler() { R.F = ZSPTable[R.A]; } };
	opcode_fn or_b = new opcode_fn() { public void handler() { M_OR(R.BC.H); } };
	opcode_fn or_c = new opcode_fn() { public void handler() { M_OR(R.BC.L); } };
	opcode_fn or_d = new opcode_fn() { public void handler() { M_OR(R.DE.H); } };
	opcode_fn or_e = new opcode_fn() { public void handler() { M_OR(R.DE.L); } };
	opcode_fn or_h = new opcode_fn() { public void handler() { M_OR(R.HL.H); } };
	opcode_fn or_l = new opcode_fn() { public void handler() { M_OR(R.HL.L); } };
	opcode_fn or_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_OR(i); } };

	opcode_fn outi = new opcode_fn() { public void handler()
	{
		Z80_Out (R.BC.L, M_RDMEM(R.HL.W));
		R.HL.AddW(1);
		R.BC.AddH(-1);
		R.F = (R.BC.H != 0) ? N_FLAG : (Z_FLAG | N_FLAG);
	} };
	opcode_fn otir = new opcode_fn() { public void handler()
	{
		R.R -= 2;
		do
		{
			R.R += 2;
			Z80_Out(R.BC.L, M_RDMEM(R.HL.W));
			R.HL.AddW(1);
			R.BC.AddH(-1);
			z80_ICount[0] -= 21;
		}
		while (R.BC.H != 0 && z80_ICount[0] > 0);
		R.F = (R.BC.H != 0) ? N_FLAG : (Z_FLAG | N_FLAG);
		if (R.BC.H != 0) R.PC = (R.PC - 2) & 0xFFFF;
		else z80_ICount[0] += 5;
	} };

	opcode_fn out_c_a = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.A); } };
	opcode_fn out_c_b = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.BC.H); } };
	opcode_fn out_c_d = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.DE.H); } };
	opcode_fn out_c_e = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.DE.L); } };
	opcode_fn out_c_h = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.HL.H); } };
	opcode_fn out_c_l = new opcode_fn() { public void handler() { Z80_Out(R.BC.L, R.HL.L); } };
	opcode_fn out_byte_a = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); Z80_Out(i, R.A); } };

	opcode_fn pop_af = new opcode_fn() { public void handler() { R.AF = M_POP(); R.A = R.AF >> 8; R.F = R.AF & 0xFF; } };
	opcode_fn pop_bc = new opcode_fn() { public void handler() { R.BC.SetW(M_POP()); } };
	opcode_fn pop_de = new opcode_fn() { public void handler() { R.DE.SetW(M_POP()); } };
	opcode_fn pop_hl = new opcode_fn() { public void handler() { R.HL.SetW(M_POP()); } };
	opcode_fn pop_ix = new opcode_fn() { public void handler() { R.IX.SetW(M_POP()); } };
	opcode_fn pop_iy = new opcode_fn() { public void handler() { R.IY.SetW(M_POP()); } };

	opcode_fn push_af = new opcode_fn() { public void handler() { M_PUSH((R.A << 8) | R.F); } };
	opcode_fn push_bc = new opcode_fn() { public void handler() { M_PUSH(R.BC.W); } };
	opcode_fn push_de = new opcode_fn() { public void handler() { M_PUSH(R.DE.W); } };
	opcode_fn push_hl = new opcode_fn() { public void handler() { M_PUSH(R.HL.W); } };
	opcode_fn push_ix = new opcode_fn() { public void handler() { M_PUSH(R.IX.W); } };
	opcode_fn push_iy = new opcode_fn() { public void handler() { M_PUSH(R.IY.W); } };

	opcode_fn res_0_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(0, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_0_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(0, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_0_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(0, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_0_a = new opcode_fn() { public void handler() { R.A = M_RES(0, R.A); } };
	opcode_fn res_0_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(0, R.BC.H)); } };
	opcode_fn res_0_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(0, R.BC.L)); } };
	opcode_fn res_0_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(0, R.DE.H)); } };
	opcode_fn res_0_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(0, R.DE.L)); } };
	opcode_fn res_0_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(0, R.HL.H)); } };
	opcode_fn res_0_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(0, R.HL.L)); } };

	opcode_fn res_1_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(1, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_1_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(1, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_1_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(1, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_1_a = new opcode_fn() { public void handler() { R.A = M_RES(1, R.A); } };
	opcode_fn res_1_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(1, R.BC.H)); } };
	opcode_fn res_1_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(1, R.BC.L)); } };
	opcode_fn res_1_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(1, R.DE.H)); } };
	opcode_fn res_1_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(1, R.DE.L)); } };
	opcode_fn res_1_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(1, R.HL.H)); } };
	opcode_fn res_1_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(1, R.HL.L)); } };

	opcode_fn res_2_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(2, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_2_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(2, i);
		M_WRMEM(j, i);
	} };
        opcode_fn res_2_xiy = new opcode_fn()
        {
            public void handler()
            {
                int j = M_XIY();
                int i = M_RDMEM(j);
                i = M_RES(2, i);
                M_WRMEM(j, i);
            }
        };

	opcode_fn res_2_a = new opcode_fn() { public void handler() { R.A = M_RES(2, R.A); } };
	opcode_fn res_2_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(2, R.BC.H)); } };
	opcode_fn res_2_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(2, R.BC.L)); } };
	opcode_fn res_2_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(2, R.DE.H)); } };
	opcode_fn res_2_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(2, R.DE.L)); } };
	opcode_fn res_2_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(2, R.HL.H)); } };
	opcode_fn res_2_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(2, R.HL.L)); } };

	opcode_fn res_3_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(3, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_3_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(3, i);
		M_WRMEM(j, i);
	} };
        opcode_fn res_3_xiy = new opcode_fn()
        {
            public void handler()
            {
                int j = M_XIY();
                int i = M_RDMEM(j);
                i = M_RES(3, i);
                M_WRMEM(j, i);
            }
        };
	opcode_fn res_3_a = new opcode_fn() { public void handler() { R.A = M_RES(3, R.A); } };
	opcode_fn res_3_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(3, R.BC.H)); } };
	opcode_fn res_3_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(3, R.BC.L)); } };
	opcode_fn res_3_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(3, R.DE.H)); } };
	opcode_fn res_3_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(3, R.DE.L)); } };
	opcode_fn res_3_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(3, R.HL.H)); } };
	opcode_fn res_3_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(3, R.HL.L)); } };

	opcode_fn res_4_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(4, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_4_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(4, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_4_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(4, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_4_a = new opcode_fn() { public void handler() { R.A = M_RES(4, R.A); } };
	opcode_fn res_4_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(4, R.BC.H)); } };
	opcode_fn res_4_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(4, R.BC.L)); } };
	opcode_fn res_4_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(4, R.DE.H)); } };
	opcode_fn res_4_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(4, R.DE.L)); } };
	opcode_fn res_4_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(4, R.HL.H)); } };
	opcode_fn res_4_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(4, R.HL.L)); } };

	opcode_fn res_5_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(5, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_5_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(5, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_5_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(5, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_5_a = new opcode_fn() { public void handler() { R.A = M_RES(5, R.A); } };
	opcode_fn res_5_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(5, R.BC.H)); } };
	opcode_fn res_5_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(5, R.BC.L)); } };
	opcode_fn res_5_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(5, R.DE.H)); } };
	opcode_fn res_5_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(5, R.DE.L)); } };
	opcode_fn res_5_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(5, R.HL.H)); } };
	opcode_fn res_5_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(5, R.HL.L)); } };

	opcode_fn res_6_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(6, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_6_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(6, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_6_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(6, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_6_a = new opcode_fn() { public void handler() { R.A = M_RES(6, R.A); } };
	opcode_fn res_6_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(6, R.BC.H)); } };
	opcode_fn res_6_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(6, R.BC.L)); } };
	opcode_fn res_6_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(6, R.DE.H)); } };
	opcode_fn res_6_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(6, R.DE.L)); } };
	opcode_fn res_6_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(6, R.HL.H)); } };
	opcode_fn res_6_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(6, R.HL.L)); } };

	opcode_fn res_7_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RES(7, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn res_7_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RES(7, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_7_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RES(7, i);
		M_WRMEM(j, i);
	} };
	opcode_fn res_7_a = new opcode_fn() { public void handler() { R.A = M_RES(7, R.A); } };
	opcode_fn res_7_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RES(7, R.BC.H)); } };
	opcode_fn res_7_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RES(7, R.BC.L)); } };
	opcode_fn res_7_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RES(7, R.DE.H)); } };
	opcode_fn res_7_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RES(7, R.DE.L)); } };
	opcode_fn res_7_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RES(7, R.HL.H)); } };
	opcode_fn res_7_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RES(7, R.HL.L)); } };

	opcode_fn ret = new opcode_fn() { public void handler() { M_RET(); } };
	opcode_fn ret_c = new opcode_fn() { public void handler() { if (M_C()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_m = new opcode_fn() { public void handler() { if (M_M()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_nc = new opcode_fn() { public void handler() { if (M_NC()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_nz = new opcode_fn() { public void handler() { if (M_NZ()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_p = new opcode_fn() { public void handler() { if (M_P()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_pe = new opcode_fn() { public void handler() { if (M_PE()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_po = new opcode_fn() { public void handler() { if (M_PO()) { M_RET(); } else { M_SKIP_RET(); } } };
	opcode_fn ret_z = new opcode_fn() { public void handler() { if (M_Z()) { M_RET(); } else { M_SKIP_RET(); } } };

	opcode_fn reti = new opcode_fn() { public void handler() { /*Z80_Reti();*/ M_RET(); } };
	opcode_fn retn = new opcode_fn() { public void handler() { R.IFF1 = R.IFF2; /*Z80_Retn();*/ M_RET(); } };

	opcode_fn rl_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RL(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn rl_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RL(i);
		M_WRMEM(j, i);
	} };
	opcode_fn rl_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_RL(i);
		M_WRMEM(j, i);
	} };
	opcode_fn rl_a = new opcode_fn() { public void handler() { R.A = M_RL(R.A); } };
	opcode_fn rl_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RL(R.BC.H)); } };
	opcode_fn rl_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RL(R.BC.L)); } };
	opcode_fn rl_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RL(R.DE.H)); } };
	opcode_fn rl_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RL(R.DE.L)); } };
	opcode_fn rl_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RL(R.HL.H)); } };
	opcode_fn rl_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RL(R.HL.L)); } };
	opcode_fn rla = new opcode_fn() { public void handler() { M_RLA(); } };

	opcode_fn rlc_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RLC(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn rlc_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RLC(i);
		M_WRMEM(j, i);
	} };
	opcode_fn rlc_a = new opcode_fn() { public void handler() { R.A = M_RLC(R.A); } };
	opcode_fn rlc_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RLC(R.BC.H)); } };
	opcode_fn rlc_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RLC(R.BC.L)); } };
	opcode_fn rlc_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RLC(R.DE.H)); } };
	opcode_fn rlc_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RLC(R.DE.L)); } };
	opcode_fn rlc_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RLC(R.HL.H)); } };
	opcode_fn rlc_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RLC(R.HL.L)); } };
	opcode_fn rlca = new opcode_fn() { public void handler() { M_RLCA(); } };

	opcode_fn rld = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		M_WRMEM(R.HL.W, ((i << 4) | (R.A & 0x0F)) & 0xFF);
		R.A = ((R.A & 0xF0) | (i >> 4)) & 0xFF;
		R.F = (R.F & C_FLAG) | ZSPTable[R.A];
	} };

	opcode_fn rr_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_RR(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn rr_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_RR(i);
		M_WRMEM(j, i);
	} };
	opcode_fn rr_a = new opcode_fn() { public void handler() { R.A = M_RR(R.A); } };
	opcode_fn rr_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RR(R.BC.H)); } };
	opcode_fn rr_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RR(R.BC.L)); } };
	opcode_fn rr_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RR(R.DE.H)); } };
	opcode_fn rr_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RR(R.DE.L)); } };
	opcode_fn rr_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RR(R.HL.H)); } };
	opcode_fn rr_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RR(R.HL.L)); } };
	opcode_fn rra = new opcode_fn() { public void handler() { M_RRA(); } };

	opcode_fn rrc_xhl = new opcode_fn() { public void handler()
	{
		int i;
	 	i = M_RDMEM(R.HL.W);
	 	i = M_RRC(i);
	 	M_WRMEM(R.HL.W, i);
	} };
	opcode_fn rrc_xix = new opcode_fn() { public void handler()
	{
		int i;
	 	int j;
	 	j = M_XIX();
	 	i = M_RDMEM(j);
	 	i = M_RRC(i);
	 	M_WRMEM(j, i);
	} };
	opcode_fn rrc_a = new opcode_fn() { public void handler() { R.A = M_RRC(R.A); } };
	opcode_fn rrc_b = new opcode_fn() { public void handler() { R.BC.SetH(M_RRC(R.BC.H)); } };
	opcode_fn rrc_c = new opcode_fn() { public void handler() { R.BC.SetL(M_RRC(R.BC.L)); } };
	opcode_fn rrc_d = new opcode_fn() { public void handler() { R.DE.SetH(M_RRC(R.DE.H)); } };
	opcode_fn rrc_e = new opcode_fn() { public void handler() { R.DE.SetL(M_RRC(R.DE.L)); } };
	opcode_fn rrc_h = new opcode_fn() { public void handler() { R.HL.SetH(M_RRC(R.HL.H)); } };
	opcode_fn rrc_l = new opcode_fn() { public void handler() { R.HL.SetL(M_RRC(R.HL.L)); } };
	opcode_fn rrca = new opcode_fn() { public void handler() { M_RRCA(); } };

	opcode_fn rrd = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		M_WRMEM(R.HL.W, ((i >> 4) | (R.A << 4)) & 0xFF); 
		R.A = ((R.A & 0xF0) | (i & 0x0F)) & 0xFF;
		R.F = (R.F & C_FLAG) | ZSPTable[R.A];
	} };
	opcode_fn rst_00 = new opcode_fn() { public void handler() { M_RST(0x00); } };
	opcode_fn rst_08 = new opcode_fn() { public void handler() { M_RST(0x08); } };
	opcode_fn rst_10 = new opcode_fn() { public void handler() { M_RST(0x10); } };
	opcode_fn rst_18 = new opcode_fn() { public void handler() { M_RST(0x18); } };
	opcode_fn rst_20 = new opcode_fn() { public void handler() { M_RST(0x20); } };
	opcode_fn rst_28 = new opcode_fn() { public void handler() { M_RST(0x28); } };
	opcode_fn rst_30 = new opcode_fn() { public void handler() { M_RST(0x30); } };
	opcode_fn rst_38 = new opcode_fn() { public void handler() { M_RST(0x38); } };

	opcode_fn sbc_a_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_SBC(i); } };
	opcode_fn sbc_a_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_SBC(i); } };
	opcode_fn sbc_a_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_SBC(i); } };
	opcode_fn sbc_a_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_SBC(i); } };
	
	opcode_fn sbc_a_a = new opcode_fn() { public void handler() { M_SBC(R.A); } };
	opcode_fn sbc_a_b = new opcode_fn() { public void handler() { M_SBC(R.BC.H); } };
	opcode_fn sbc_a_c = new opcode_fn() { public void handler() { M_SBC(R.BC.L); } };
	opcode_fn sbc_a_d = new opcode_fn() { public void handler() { M_SBC(R.DE.H); } };
	opcode_fn sbc_a_e = new opcode_fn() { public void handler() { M_SBC(R.DE.L); } };
	opcode_fn sbc_a_h = new opcode_fn() { public void handler() { M_SBC(R.HL.H); } };
	opcode_fn sbc_a_l = new opcode_fn() { public void handler() { M_SBC(R.HL.L); } };

	opcode_fn sbc_hl_bc = new opcode_fn() { public void handler() { M_SBCW(R.BC.W); } };
	opcode_fn sbc_hl_de = new opcode_fn() { public void handler() { M_SBCW(R.DE.W); } };
	opcode_fn sbc_hl_hl = new opcode_fn() { public void handler() { M_SBCW(R.HL.W); } };
	opcode_fn sbc_hl_sp = new opcode_fn() { public void handler() { M_SBCW(R.SP); } };

	opcode_fn scf = new opcode_fn() { public void handler() { R.F = (R.F & 0xEC) | C_FLAG; } };

	opcode_fn set_0_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(0, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_0_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(0, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_0_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(0, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_0_a = new opcode_fn() { public void handler() { R.A = M_SET(0, R.A); } };
	opcode_fn set_0_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(0, R.BC.H)); } };
	opcode_fn set_0_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(0, R.BC.L)); } };
	opcode_fn set_0_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(0, R.DE.H)); } };
	opcode_fn set_0_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(0, R.DE.L)); } };
	opcode_fn set_0_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(0, R.HL.H)); } };
	opcode_fn set_0_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(0, R.HL.L)); } };

	opcode_fn set_1_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(1, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_1_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(1, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_1_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(1, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_1_a = new opcode_fn() { public void handler() { R.A = M_SET(1, R.A); } };
	opcode_fn set_1_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(1, R.BC.H)); } };
	opcode_fn set_1_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(1, R.BC.L)); } };
	opcode_fn set_1_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(1, R.DE.H)); } };
	opcode_fn set_1_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(1, R.DE.L)); } };
	opcode_fn set_1_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(1, R.HL.H)); } };
	opcode_fn set_1_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(1, R.HL.L)); } };

	opcode_fn set_2_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(2, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_2_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(2, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_2_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(2, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_2_a = new opcode_fn() { public void handler() { R.A = M_SET(2, R.A); } };
	opcode_fn set_2_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(2, R.BC.H)); } };
	opcode_fn set_2_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(2, R.BC.L)); } };
	opcode_fn set_2_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(2, R.DE.H)); } };
	opcode_fn set_2_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(2, R.DE.L)); } };
	opcode_fn set_2_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(2, R.HL.H)); } };
	opcode_fn set_2_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(2, R.HL.L)); } };

	opcode_fn set_3_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(3, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_3_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(3, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_3_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(3, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_3_a = new opcode_fn() { public void handler() { R.A = M_SET(3, R.A); } };
	opcode_fn set_3_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(3, R.BC.H)); } };
	opcode_fn set_3_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(3, R.BC.L)); } };
	opcode_fn set_3_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(3, R.DE.H)); } };
	opcode_fn set_3_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(3, R.DE.L)); } };
	opcode_fn set_3_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(3, R.HL.H)); } };
	opcode_fn set_3_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(3, R.HL.L)); } };

	opcode_fn set_4_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(4, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_4_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(4, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_4_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(4, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_4_a = new opcode_fn() { public void handler() { R.A = M_SET(4, R.A); } };
	opcode_fn set_4_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(4, R.BC.H)); } };
	opcode_fn set_4_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(4, R.BC.L)); } };
	opcode_fn set_4_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(4, R.DE.H)); } };
	opcode_fn set_4_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(4, R.DE.L)); } };
	opcode_fn set_4_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(4, R.HL.H)); } };
	opcode_fn set_4_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(4, R.HL.L)); } };

	opcode_fn set_5_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(5, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_5_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(5, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_5_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(5, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_5_a = new opcode_fn() { public void handler() { R.A = M_SET(5, R.A); } };
	opcode_fn set_5_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(5, R.BC.H)); } };
	opcode_fn set_5_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(5, R.BC.L)); } };
	opcode_fn set_5_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(5, R.DE.H)); } };
	opcode_fn set_5_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(5, R.DE.L)); } };
	opcode_fn set_5_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(5, R.HL.H)); } };
	opcode_fn set_5_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(5, R.HL.L)); } };

	opcode_fn set_6_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(6, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_6_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(6, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_6_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(6, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_6_a = new opcode_fn() { public void handler() { R.A = M_SET(6, R.A); } };
	opcode_fn set_6_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(6, R.BC.H)); } };
	opcode_fn set_6_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(6, R.BC.L)); } };
	opcode_fn set_6_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(6, R.DE.H)); } };
	opcode_fn set_6_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(6, R.DE.L)); } };
	opcode_fn set_6_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(6, R.HL.H)); } };
	opcode_fn set_6_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(6, R.HL.L)); } };

	opcode_fn set_7_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SET(7, i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn set_7_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SET(7, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_7_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SET(7, i);
		M_WRMEM(j, i);
	} };
	opcode_fn set_7_a = new opcode_fn() { public void handler() { R.A = M_SET(7, R.A); } };
	opcode_fn set_7_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SET(7, R.BC.H)); } };
	opcode_fn set_7_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SET(7, R.BC.L)); } };
	opcode_fn set_7_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SET(7, R.DE.H)); } };
	opcode_fn set_7_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SET(7, R.DE.L)); } };
	opcode_fn set_7_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SET(7, R.HL.H)); } };
	opcode_fn set_7_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SET(7, R.HL.L)); } };

	opcode_fn sla_xhl = new opcode_fn() { public void handler()
	{
		int i;
		i = M_RDMEM(R.HL.W);
		i = M_SLA(i);
		M_WRMEM(R.HL.W, i);
	} };
	opcode_fn sla_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SLA(i);
		M_WRMEM(j, i);
	} };
	opcode_fn sla_xiy = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIY();
		i = M_RDMEM(j);
		i = M_SLA(i);
		M_WRMEM(j, i);
	} };

	opcode_fn sla_a = new opcode_fn() { public void handler() { R.A = M_SLA(R.A); } };
	opcode_fn sla_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SLA(R.BC.H)); } };
	opcode_fn sla_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SLA(R.BC.L)); } };
	opcode_fn sla_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SLA(R.DE.H)); } };
	opcode_fn sla_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SLA(R.DE.L)); } };
	opcode_fn sla_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SLA(R.HL.H)); } };
	opcode_fn sla_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SLA(R.HL.L)); } };

	opcode_fn sra_a = new opcode_fn() { public void handler() { R.A = M_SRA(R.A); } };
	opcode_fn sra_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SRA(R.BC.H)); } };
	opcode_fn sra_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SRA(R.BC.L)); } };
	opcode_fn sra_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SRA(R.DE.H)); } };
	opcode_fn sra_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SRA(R.DE.L)); } };
	opcode_fn sra_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SRA(R.HL.H)); } };
	opcode_fn sra_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SRA(R.HL.L)); } };

	opcode_fn srl_xix = new opcode_fn() { public void handler()
	{
		int i;
		int j;
		j = M_XIX();
		i = M_RDMEM(j);
		i = M_SRL(i);
		M_WRMEM(j, i);
	} };
	opcode_fn srl_a = new opcode_fn() { public void handler() { R.A = M_SRL(R.A); } };
	opcode_fn srl_b = new opcode_fn() { public void handler() { R.BC.SetH(M_SRL(R.BC.H)); } };
	opcode_fn srl_c = new opcode_fn() { public void handler() { R.BC.SetL(M_SRL(R.BC.L)); } };
	opcode_fn srl_d = new opcode_fn() { public void handler() { R.DE.SetH(M_SRL(R.DE.H)); } };
	opcode_fn srl_e = new opcode_fn() { public void handler() { R.DE.SetL(M_SRL(R.DE.L)); } };
	opcode_fn srl_h = new opcode_fn() { public void handler() { R.HL.SetH(M_SRL(R.HL.H)); } };
	opcode_fn srl_l = new opcode_fn() { public void handler() { R.HL.SetL(M_SRL(R.HL.L)); } };

	opcode_fn sub_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_SUB(i); } };
	opcode_fn sub_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_SUB(i); } };
	opcode_fn sub_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_SUB(i); } };
	opcode_fn sub_a = new opcode_fn() { public void handler() { R.A = 0; R.F = Z_FLAG | N_FLAG; } };
	opcode_fn sub_b = new opcode_fn() { public void handler() { M_SUB(R.BC.H); } };
	opcode_fn sub_c = new opcode_fn() { public void handler() { M_SUB(R.BC.L); } };
	opcode_fn sub_d = new opcode_fn() { public void handler() { M_SUB(R.DE.H); } };
	opcode_fn sub_e = new opcode_fn() { public void handler() { M_SUB(R.DE.L); } };
	opcode_fn sub_h = new opcode_fn() { public void handler() { M_SUB(R.HL.H); } };
	opcode_fn sub_l = new opcode_fn() { public void handler() { M_SUB(R.HL.L); } };
        opcode_fn sub_ixh = new opcode_fn() { public void handler() { M_SUB(R.IX.H); } } ;
        opcode_fn sub_ixl = new opcode_fn() { public void handler() { M_SUB(R.IX.L); } } ;
	opcode_fn sub_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_SUB(i); } };

	opcode_fn xor_xhl = new opcode_fn() { public void handler() { int i = M_RD_XHL(); M_XOR(i); } };
	opcode_fn xor_xix = new opcode_fn() { public void handler() { int i = M_RD_XIX(); M_XOR(i); } };
	opcode_fn xor_xiy = new opcode_fn() { public void handler() { int i = M_RD_XIY(); M_XOR(i); } } ;
        opcode_fn xor_a = new opcode_fn() { public void handler() { R.A = 0; R.F = Z_FLAG | V_FLAG; } };
	opcode_fn xor_b = new opcode_fn() { public void handler() { M_XOR(R.BC.H); } };
	opcode_fn xor_c = new opcode_fn() { public void handler() { M_XOR(R.BC.L); } };
	opcode_fn xor_d = new opcode_fn() { public void handler() { M_XOR(R.DE.H); } };
	opcode_fn xor_e = new opcode_fn() { public void handler() { M_XOR(R.DE.L); } };
	opcode_fn xor_h = new opcode_fn() { public void handler() { M_XOR(R.HL.H); } };
	opcode_fn xor_l = new opcode_fn() { public void handler() { M_XOR(R.HL.L); } };
	opcode_fn xor_byte = new opcode_fn() { public void handler() { int i = M_RDMEM_OPCODE(); M_XOR(i); } };
        opcode_fn xor_ixh = new opcode_fn() { public void handler() { M_XOR(R.IX.H); } } ;
	opcode_fn no_op = new opcode_fn() { public void handler()
	{
		R.PC = (R.PC - 1) & 0xFFFF;
	} };

  int[] cycles_main = { 4, 10, 7, 6, 4, 4, 7, 4, 4, 11, 7, 6, 4, 4, 7, 4, 8, 10, 7, 6, 4, 4, 7, 4, 7, 11, 7, 6, 4, 4, 7, 4, 7, 10, 16, 6, 4, 4, 7, 4, 7, 11, 16, 6, 4, 4, 7, 4, 7, 10, 13, 6, 11, 11, 10, 4, 7, 11, 13, 6, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 7, 7, 7, 7, 7, 7, 4, 7, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4, 5, 10, 10, 10, 10, 11, 7, 11, 5, 4, 10, 0, 10, 10, 7, 11, 5, 10, 10, 11, 10, 11, 7, 11, 5, 4, 10, 11, 10, 0, 7, 11, 5, 10, 10, 19, 10, 11, 7, 11, 5, 4, 10, 4, 10, 0, 7, 11, 5, 10, 10, 4, 10, 11, 7, 11, 5, 6, 10, 4, 10, 0, 7, 11 };

  int[] cycles_cb = { 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8 };

  int[] cycles_xx_cb = { 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 23, 0 };

  int[] cycles_xx = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 14, 20, 10, 9, 9, 9, 0, 0, 15, 20, 10, 9, 9, 9, 0, 0, 0, 0, 0, 23, 23, 19, 0, 0, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 19, 19, 19, 19, 19, 19, 19, 19, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 9, 9, 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 0, 23, 0, 15, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0 };

  int[] cycles_ed = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 18, 12, 12, 15, 20, 8, 8, 8, 18, 12, 12, 15, 20, 8, 8, 8, 0, 12, 12, 15, 20, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 16, 0, 0, 0, 0, 16, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };


	opcode_fn no_op_xx = new opcode_fn() { public void handler() {
		R.PC = (R.PC + 1) & 0xFFFF;
	} };
        opcode_fn[] opcode_dd_cb = { no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, rlc_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, rrc_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, rl_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, rr_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, sla_xix, no_op_xx, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, srl_xix, no_op_xx, bit_0_xix, bit_0_xix, bit_0_xix, bit_0_xix, bit_0_xix, bit_0_xix, bit_0_xix, bit_0_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_1_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_2_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_3_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_4_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_5_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_6_xix, bit_7_xix, bit_7_xix, bit_7_xix, bit_7_xix, bit_7_xix, bit_7_xix, bit_7_xix, bit_7_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_0_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_1_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_2_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_3_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_4_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_5_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_6_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_7_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_0_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_1_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_2_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_3_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_4_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_5_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_6_xix, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_7_xix, no_op_xx };

        opcode_fn[] opcode_fd_cb = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, rl_xiy, no_op_xx, null, null, null, null, null, null, null, null, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, sla_xiy, no_op_xx, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_0_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_1_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_2_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_3_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_4_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_5_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_6_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, bit_7_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_0_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_1_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_2_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_3_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_4_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_5_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_6_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, res_7_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_0_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_1_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_2_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_3_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_4_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_5_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_6_xiy, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, no_op_xx, set_7_xiy, no_op_xx };


	opcode_fn dd_cb = new opcode_fn() { public void handler()
	{
	 	int opcode=M_RDOP_ARG((R.PC+1)&0xFFFF);
	 	z80_ICount[0] -= cycles_xx_cb[opcode];
  		if (opcode_dd_cb[opcode] != null)
  			opcode_dd_cb[opcode].handler();
  		else
  		{
	  		System.out.println("DD CB PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));
	  	}
		R.PC = (R.PC + 1) & 0xFFFF;
	} };
	opcode_fn fd_cb = new opcode_fn() { public void handler()
	{

		int opcode=M_RDOP_ARG((R.PC+1)&0xFFFF);
	 	z80_ICount[0] -= cycles_xx_cb[opcode];
  		if (opcode_fd_cb[opcode] != null)
  			opcode_fd_cb[opcode].handler();
  		else
  		{
	  		System.out.println("FD CB PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));
	  	}
		R.PC = (R.PC + 1) & 0xFFFF;
	} };

        opcode_fn[] opcode_cb = { rlc_b, rlc_c, rlc_d, rlc_e, rlc_h, rlc_l, rlc_xhl, rlc_a, rrc_b, rrc_c, rrc_d, rrc_e, rrc_h, rrc_l, rrc_xhl, rrc_a, rl_b, rl_c, rl_d, rl_e, rl_h, rl_l, rl_xhl, rl_a, rr_b, rr_c, rr_d, rr_e, rr_h, rr_l, rr_xhl, rr_a, sla_b, sla_c, sla_d, sla_e, sla_h, sla_l, sla_xhl, sla_a, sra_b, sra_c, sra_d, sra_e, sra_h, sra_l, null, sra_a, null, null, null, null, null, null, null, null, srl_b, srl_c, srl_d, srl_e, srl_h, srl_l, null, srl_a, bit_0_b, bit_0_c, bit_0_d, bit_0_e, bit_0_h, bit_0_l, bit_0_xhl, bit_0_a, bit_1_b, bit_1_c, bit_1_d, bit_1_e, bit_1_h, bit_1_l, bit_1_xhl, bit_1_a, bit_2_b, bit_2_c, bit_2_d, bit_2_e, bit_2_h, bit_2_l, bit_2_xhl, bit_2_a, bit_3_b, bit_3_c, bit_3_d, bit_3_e, bit_3_h, bit_3_l, bit_3_xhl, bit_3_a, bit_4_b, bit_4_c, bit_4_d, bit_4_e, bit_4_h, bit_4_l, bit_4_xhl, bit_4_a, bit_5_b, bit_5_c, bit_5_d, bit_5_e, bit_5_h, bit_5_l, bit_5_xhl, bit_5_a, bit_6_b, bit_6_c, bit_6_d, bit_6_e, bit_6_h, bit_6_l, bit_6_xhl, bit_6_a, bit_7_b, bit_7_c, bit_7_d, bit_7_e, bit_7_h, bit_7_l, bit_7_xhl, bit_7_a, res_0_b, res_0_c, res_0_d, res_0_e, res_0_h, res_0_l, res_0_xhl, res_0_a, res_1_b, res_1_c, res_1_d, res_1_e, res_1_h, res_1_l, res_1_xhl, res_1_a, res_2_b, res_2_c, res_2_d, res_2_e, res_2_h, res_2_l, res_2_xhl, res_2_a, res_3_b, res_3_c, res_3_d, res_3_e, res_3_h, res_3_l, res_3_xhl, res_3_a, res_4_b, res_4_c, res_4_d, res_4_e, res_4_h, res_4_l, res_4_xhl, res_4_a, res_5_b, res_5_c, res_5_d, res_5_e, res_5_h, res_5_l, res_5_xhl, res_5_a, res_6_b, res_6_c, res_6_d, res_6_e, res_6_h, res_6_l, res_6_xhl, res_6_a, res_7_b, res_7_c, res_7_d, res_7_e, res_7_h, res_7_l, res_7_xhl, res_7_a, set_0_b, set_0_c, set_0_d, set_0_e, set_0_h, set_0_l, set_0_xhl, set_0_a, set_1_b, set_1_c, set_1_d, set_1_e, set_1_h, set_1_l, set_1_xhl, set_1_a, set_2_b, set_2_c, set_2_d, set_2_e, set_2_h, set_2_l, set_2_xhl, set_2_a, set_3_b, set_3_c, set_3_d, set_3_e, set_3_h, set_3_l, set_3_xhl, set_3_a, set_4_b, set_4_c, set_4_d, set_4_e, set_4_h, set_4_l, set_4_xhl, set_4_a, set_5_b, set_5_c, set_5_d, set_5_e, set_5_h, set_5_l, set_5_xhl, set_5_a, set_6_b, set_6_c, set_6_d, set_6_e, set_6_h, set_6_l, set_6_xhl, set_6_a, set_7_b, set_7_c, set_7_d, set_7_e, set_7_h, set_7_l, set_7_xhl, set_7_a };

        opcode_fn[] opcode_dd = { no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, add_ix_bc, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, add_ix_de, no_op, no_op, no_op, no_op, no_op, no_op, null, ld_ix_word, ld_xword_ix, inc_ix, null, null, ld_ixh_byte, null, no_op, add_ix_ix, ld_ix_xword, dec_ix, inc_ixl, dec_ixl, ld_ixl_byte, no_op, no_op, no_op, no_op, no_op, inc_xix, dec_xix, ld_xix_byte, no_op, no_op, add_ix_sp, no_op, no_op, no_op, no_op, no_op, no_op, null, null, null, null, null, null, ld_b_xix, null, null, null, null, null, ld_c_ixh, null, ld_c_xix, null, null, null, null, null, null, null, ld_d_xix, null, null, null, null, null, null, ld_e_ixl, ld_e_xix, null, ld_ixh_b, ld_ixh_c, ld_ixh_d, ld_ixh_e, null, null, ld_h_xix, ld_ixh_a, null, null, null, null, null, null, ld_l_xix, ld_ixl_a, ld_xix_b, ld_xix_c, ld_xix_d, ld_xix_e, ld_xix_h, ld_xix_l, no_op, ld_xix_a, no_op, no_op, no_op, no_op, ld_a_ixh, ld_a_ixl, ld_a_xix, no_op, no_op, no_op, no_op, no_op, add_a_ixh, add_a_ixl, add_a_xix, no_op, null, null, null, null, null, null, adc_a_xix, null, no_op, no_op, no_op, no_op, sub_ixh, sub_ixl, sub_xix, no_op, null, null, null, null, null, null, sbc_a_xix, null, null, null, null, null, null, null, and_xix, null, null, null, null, null, xor_ixh, null, xor_xix, null, null, null, null, null, null, null, or_xix, null, null, null, null, null, null, null, cp_xix, null, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, dd_cb, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, pop_ix, no_op, ex_xsp_ix, no_op, push_ix, no_op, no_op, no_op, jp_ix, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, null, null, null, null, null, no_op, no_op, null };

        opcode_fn[] opcode_ed = { nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, in_b_c, out_c_b, sbc_hl_bc, ld_xword_bc, neg, retn, im_0, ld_i_a, in_c_c, null, adc_hl_bc, ld_bc_xword, neg, reti, im_0, ld_r_a, null, out_c_d, sbc_hl_de, ld_xword_de, neg, retn, im_1, ld_a_i, in_e_c, out_c_e, adc_hl_de, ld_de_xword, neg, reti, im_2, ld_a_r, null, out_c_h, null, null, neg, retn, im_0, rrd, in_l_c, out_c_l, adc_hl_hl, null, neg, reti, im_0, rld, null, null, null, ld_xword_sp, neg, retn, im_1, null, in_a_c, out_c_a, null, ld_sp_xword, neg, reti, im_2, null, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, ldi, cpi, null, outi, null, null, null, null, ldd, null, null, null, null, null, null, null, ldir, cpir, null, otir, null, null, null, null, lddr, cpdr, null, null, null, null, null, null, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, nop, null, null, null, null, null, null, null, null };

        opcode_fn[] opcode_fd = { no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, add_iy_bc, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, add_iy_de, no_op, no_op, no_op, no_op, no_op, no_op, null, ld_iy_word, ld_xword_iy, inc_iy, null, dec_iyh, ld_iyh_byte, null, null, null, ld_iy_xword, dec_iy, null, dec_iyl, ld_iyl_byte, null, no_op, no_op, no_op, no_op, inc_xiy, dec_xiy, ld_xiy_byte, no_op, no_op, add_iy_sp, no_op, no_op, no_op, no_op, no_op, no_op, null, null, null, null, null, null, ld_b_xiy, null, null, null, null, null, null, null, ld_c_xiy, null, null, null, null, null, ld_d_iyh, null, ld_d_xiy, null, null, null, no_op, null, null, ld_e_iyl, ld_e_xiy, null, null, null, null, null, null, null, ld_h_xiy, ld_iyh_a, null, null, null, null, null, null, ld_l_xiy, ld_iyl_a, ld_xiy_b, ld_xiy_c, ld_xiy_d, ld_xiy_e, ld_xiy_h, ld_xiy_l, no_op, ld_xiy_a, null, null, null, null, ld_a_iyh, ld_a_iyl, ld_a_xiy, null, null, null, null, null, add_a_iyh, null, add_a_xiy, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, sub_xiy, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, and_xiy, null, null, null, null, null, null, null, xor_xiy, null, null, null, null, null, null, null, or_xiy, null, null, null, null, null, null, null, cp_xiy, null, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, fd_cb, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, pop_iy, no_op, ex_xsp_iy, no_op, push_iy, no_op, no_op, no_op, jp_iy, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, no_op, ld_sp_iy, no_op, no_op, no_op, no_op, no_op, no_op };


	opcode_fn cb = new opcode_fn() //TODO checked since it changed!
        {
            public void handler()
	    {
	 	
	 	R.R += 1;
		int opcode = M_RDOP(R.PC);
		R.PC = (R.PC + 1) & 0xFFFF;
	 	z80_ICount[0] -= cycles_cb[opcode];
  		if (opcode_cb[opcode] != null)
  			opcode_cb[opcode].handler();
  		else
  		{
	  		System.out.println("CB PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));
	  	}
	    }
        };

	opcode_fn dd = new opcode_fn()//TODO checked since it changed!
        {
            public void handler()
	    {
	 	
	 	R.R += 1;
		int opcode = M_RDOP(R.PC);
		R.PC = (R.PC + 1) & 0xFFFF;
	 	z80_ICount[0] -= cycles_xx[opcode];
  		if (opcode_dd[opcode] != null)
  			opcode_dd[opcode].handler();
  		else
  		{
	  		System.out.println("DD PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));
	  	}
	    }
        };
	opcode_fn ed = new opcode_fn()//TODO checked since it changed!
        {
            public void handler()
	    {
	 	
	 	R.R += 1;
		int opcode = M_RDOP(R.PC);
		R.PC = (R.PC + 1) & 0xFFFF;
	 	z80_ICount[0] -= cycles_ed[opcode];
  		if (opcode_ed[opcode] != null)
  			opcode_ed[opcode].handler();
  		else
  		{
	  		System.out.println("ED PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));	
	  	}
	    }
        };
	opcode_fn fd = new opcode_fn() //TODO checked since it changed!
        {
            public void handler()
	    {
	 	
	 	R.R += 1;
		int opcode = M_RDOP(R.PC);
		R.PC = (R.PC + 1) & 0xFFFF;
	 	z80_ICount[0] -= cycles_xx[opcode];
  		if (opcode_fd[opcode] != null)
  			opcode_fd[opcode].handler();
  		else
  		{
	  		System.out.println("FD PC = " + Integer.toHexString(R.PC) + " OPCODE = " + Integer.toHexString(opcode));
	        }
	    }
        };
	opcode_fn ei = new opcode_fn()//TODO checked since it changed!
        {
            public void handler()
	    {
		int opcode;
		/* If interrupts were disabled, execute one more instruction and check the */
		/* IRQ line. If not, simply set interrupt flip/flop 2                      */
		if (R.IFF1 == 0)
		{
			R.IFF1 = R.IFF2 = 1;
			R.R += 1;
			opcode=M_RDOP(R.PC);
			R.PC = (R.PC + 1) & 0xFFFF;
			z80_ICount[0] -= cycles_main[opcode];
			//System.out.println(Integer.toHexString(opcode));
			opcode_main[opcode].handler();
			Interrupt();
		}
		else
			R.IFF2 = 1;
	   }
        };

        opcode_fn[] opcode_main =
        {
            nop, ld_bc_word, ld_xbc_a, inc_bc, inc_b, dec_b, ld_b_byte, rlca,
            ex_af_af, add_hl_bc, ld_a_xbc, dec_bc, inc_c, dec_c, ld_c_byte, rrca,
            djnz, ld_de_word, ld_xde_a, inc_de, inc_d, dec_d, ld_d_byte, rla, jr,
            add_hl_de, ld_a_xde, dec_de, inc_e, dec_e, ld_e_byte, rra, jr_nz,
            ld_hl_word, ld_xword_hl, inc_hl, inc_h, dec_h, ld_h_byte, daa, jr_z,
            add_hl_hl, ld_hl_xword, dec_hl, inc_l, dec_l, ld_l_byte, cpl, jr_nc,
            ld_sp_word, ld_xbyte_a, inc_sp, inc_xhl, dec_xhl, ld_xhl_byte, scf,
            jr_c, add_hl_sp, ld_a_xbyte, dec_sp, inc_a, dec_a, ld_a_byte, ccf,
            ld_b_b, ld_b_c, ld_b_d, ld_b_e, ld_b_h, ld_b_l, ld_b_xhl, ld_b_a,
            ld_c_b, ld_c_c, ld_c_d, ld_c_e, ld_c_h, ld_c_l, ld_c_xhl, ld_c_a,
            ld_d_b, ld_d_c, ld_d_d, ld_d_e, ld_d_h, ld_d_l, ld_d_xhl, ld_d_a,
            ld_e_b, ld_e_c, ld_e_d, ld_e_e, ld_e_h, ld_e_l, ld_e_xhl, ld_e_a,
            ld_h_b, ld_h_c, ld_h_d, ld_h_e, ld_h_h, ld_h_l, ld_h_xhl, ld_h_a,
            ld_l_b, ld_l_c, ld_l_d, ld_l_e, ld_l_h, ld_l_l, ld_l_xhl, ld_l_a,
            ld_xhl_b, ld_xhl_c, ld_xhl_d, ld_xhl_e, ld_xhl_h, ld_xhl_l, halt,
            ld_xhl_a, ld_a_b, ld_a_c, ld_a_d, ld_a_e, ld_a_h, ld_a_l, ld_a_xhl,
            ld_a_a, add_a_b, add_a_c, add_a_d, add_a_e, add_a_h, add_a_l, add_a_xhl,
            add_a_a, adc_a_b, adc_a_c, adc_a_d, adc_a_e, adc_a_h, adc_a_l, adc_a_xhl,
            adc_a_a, sub_b, sub_c, sub_d, sub_e, sub_h, sub_l, sub_xhl, sub_a, sbc_a_b,
            sbc_a_c, sbc_a_d, sbc_a_e, sbc_a_h, sbc_a_l, sbc_a_xhl, sbc_a_a, and_b,
            and_c, and_d, and_e, and_h, and_l, and_xhl, and_a, xor_b, xor_c, xor_d,
            xor_e, xor_h, xor_l, xor_xhl, xor_a, or_b, or_c, or_d, or_e, or_h, or_l,
            or_xhl, or_a, cp_b, cp_c, cp_d, cp_e, cp_h, cp_l, cp_xhl, cp_a, ret_nz,
            pop_bc, jp_nz, jp, call_nz, push_bc, add_a_byte, rst_00, ret_z, ret, jp_z,
            cb, call_z, call, adc_a_byte, rst_08, ret_nc, pop_de, jp_nc, out_byte_a,
            call_nc, push_de, sub_byte, rst_10, ret_c, exx, jp_c, in_a_byte, call_c, dd,
            sbc_a_byte, rst_18, ret_po, pop_hl, jp_po, ex_xsp_hl, null, push_hl, and_byte,
            rst_20, ret_pe, jp_hl, jp_pe, ex_de_hl, null, ed, xor_byte, rst_28, ret_p,
            pop_af, jp_p, di, call_p, push_af, or_byte, rst_30, ret_m, ld_sp_hl, jp_m,
            ei, call_m, fd, cp_byte, rst_38 };

        int InitTables_virgin = 1;
        boolean addresses[] = new boolean[0x10000];
        boolean debug = false;
	int oldPC;

        public final int M_RDMEM(int A) { return Z80_RDMEM(A); }
	public final void M_WRMEM(int A, int V) { Z80_WRMEM(A, V); }
	public final char M_RDOP(int A) { return Z80_RDOP(A); }
	public final char M_RDOP_ARG(int A) { return Z80_RDOP_ARG(A); }
	public final char M_RDSTACK(int A) { return Z80_RDSTACK(A); }
	public final void M_WRSTACK(int A, int V) { Z80_WRSTACK(A, V); }

       public final void M_SKIP_CALL()
       {
           R.PC = (R.PC + 2) & 0xFFFF;
       }
	public final void M_SKIP_JP()
        {
            R.PC = (R.PC + 2) & 0xFFFF;
        }
	public final void M_SKIP_JR()
        {
            R.PC = (R.PC + 1) & 0xFFFF;
        }
	public final void M_SKIP_RET()////TODO check if i need to do something
        {
          //  System.out.println("Z80 M_SKIP_RET CALLED! (dunno if i must support that)");
        }



        final boolean M_C() { return (R.F & C_FLAG) != 0; }
	final boolean M_NC() { return (!M_C()); }
	final boolean M_Z() { return (R.F & Z_FLAG) != 0; }
	final boolean M_NZ() { return (!M_Z()); }
	final boolean M_M() { return (R.F & S_FLAG) != 0; }
	final boolean M_P() { return (!M_M()); }
	final boolean M_PE() { return (R.F & V_FLAG) != 0; }
	final boolean M_PO() { return (!M_PE()); }

	/* Get next opcode and increment program counter */
	char M_RDMEM_OPCODE() //TODO check it i modified for 0.27
	{
		int retval = M_RDOP_ARG(R.PC);
	 	R.PC = (R.PC + 1) & 0xFFFF;
	 	return (char)retval;
	}
	char M_RDMEM_WORD(int A)
	{
		int i=M_RDMEM(A);
		i+=M_RDMEM((A+1)&0xFFFF)<<8;
		return (char) i;
	}
	void M_WRMEM_WORD(int A, int V)
	{
		M_WRMEM(A, V & 255);
		M_WRMEM((A+1)&0xFFFF,V>>8);
	}
	char M_RDMEM_OPCODE_WORD()
	{
		int i=M_RDMEM_OPCODE();
		i+=M_RDMEM_OPCODE()<<8;
		return (char) i;
	}
	final int M_XIX()
        {
            return (R.IX.W + (byte) M_RDMEM_OPCODE()) & 0xFFFF;
        }
	final int M_XIY()
        {
            return (R.IY.W + (byte) M_RDMEM_OPCODE()) & 0xFFFF;
        }
	final int M_RD_XHL()
        {
            return M_RDMEM(R.HL.W);
        }
	final int M_RD_XIX()
	{
		int i = M_XIX();
	 	return M_RDMEM(i);
	}
	final int M_RD_XIY()
	{
		int i = M_XIY();
	 	return M_RDMEM(i);
	}
	final void M_WR_XIX(int a)
	{
		int i = M_XIX();
	 	M_WRMEM(i, a);
	}
	final void M_WR_XIY(int a)
	{
		int i = M_XIY();
	 	M_WRMEM(i, a);
	}



	/****************************************************************************/
	/* Issue an interrupt if necessary                                          */
	/****************************************************************************/
        void Interrupt ()//rewrote for v0.29
        {
        /* Z80_IRQ = j;	* -NS- sticky interrupts *	* NS 970901 */

        /* if (j==Z80_IGNORE_INT) return; */ /* NS 970904*/
        /* if (j==Z80_NMI_INT || R.IFF1) */

                if (R.pending_irq == Z80_IGNORE_INT && R.pending_nmi == 0) return;	/* NS 970904 */
                if (R.pending_nmi != 0 || R.IFF1!=0)	/* NS 970904 */
         {
        /*Z80_IRQ = Z80_IGNORE_INT;*/	/* NS 970904 */
          /* Clear interrupt flip-flop 1 */
          R.IFF1=0;
          /* Check if processor was halted */
          if (R.HALT!=0)
          {
           R.PC = (R.PC + 1) & 0xFFFF;// ++R.PC.W.l;
           R.HALT=0;
          }
        /*  if (j==Z80_NMI_INT)*/
                if (R.pending_nmi != 0)	/* NS 970904 */
          {
                R.pending_nmi = 0;	/* NS 970904 */
           M_PUSH (R.PC);
           R.PC=0x0066;
          }
          else
          {
                  int j;

                  j = R.pending_irq;	/* NS 970904 */
                R.pending_irq = Z80_IGNORE_INT;	/* NS 970904 */

           /* Interrupt mode 2. Call [R.I:databyte] */
           if (R.IM==2)
           {
            M_PUSH (R.PC);
            R.PC=M_RDMEM_WORD((j&255)|(R.I<<8));
           }
           else
            /* Interrupt mode 1. RST 38h */
            if (R.IM==1)
            {
             z80_ICount[0]-=cycles_main[0xFF];
             opcode_main[0xFF].handler();
            }
            else
            /* Interrupt mode 0. We check for CALL and JP instructions, if neither  */
            /* of these were found we assume a 1 byte opcode was placed on the      */
            /* databus                                                              */
            {
             switch (j&0xFF0000)
             {
              case 0xCD0000:	/* bugfix NS 970904 */
               M_PUSH(R.PC);
              case 0xC30000:	/* bugfix NS 970904 */
               R.PC=j&0xFFFF;
               break;
              default:
               j&=255;
               z80_ICount[0]-=cycles_main[j];
               opcode_main[j].handler();
               break;
             }
            }
          }
         }
        }
	
	public void Debug(String toto)
	{
		System.out.println(toto + " PC " + Integer.toHexString(oldPC) + " SP " + Integer.toHexString(R.SP) + " AF " + Integer.toHexString((R.A << 8) | R.F) + " BC " + Integer.toHexString(R.BC.W) + " DE " + Integer.toHexString(R.DE.W) + " HL " + Integer.toHexString(R.HL.W) + " IX " + Integer.toHexString(R.IX.W));

        }

	/****************************************************************************/
	/* Set all registers to given values                                        */
	/****************************************************************************/
	public void Z80_SetRegs(Z80_Regs Regs)
	{
		R.AF = Regs.AF; R.PC = Regs.PC; R.SP = Regs.SP;
		R.A = Regs.A; R.F = Regs.F;
		R.BC.SetW(Regs.BC.W); R.DE.SetW(Regs.DE.W); R.HL.SetW(Regs.HL.W); R.IX.SetW(Regs.IX.W); R.IY.SetW(Regs.IY.W);
		R.AF2 = Regs.AF2; R.BC2 = Regs.BC2; R.DE2 = Regs.DE2; R.HL2 = Regs.HL2;
		R.IFF1 = Regs.IFF1; R.IFF2 = Regs.IFF2; R.HALT = Regs.HALT; R.IM = Regs.IM; R.I = Regs.I; R.R = Regs.R; R.R2 = Regs.R2;
                R.pending_irq=Regs.pending_irq;
                R.pending_nmi=Regs.pending_nmi;
	}	
	/****************************************************************************/
	/* Get all registers in given buffer                                        */
	/****************************************************************************/
	public void Z80_GetRegs(Z80_Regs Regs)
	{
		Regs.AF = R.AF; Regs.PC = R.PC; Regs.SP = R.SP;
		Regs.A = R.A; Regs.F = R.F;
		Regs.BC.SetW(R.BC.W); Regs.DE.SetW(R.DE.W); Regs.HL.SetW(R.HL.W); Regs.IX.SetW(R.IX.W); Regs.IY.SetW(R.IY.W);
		Regs.AF2 = R.AF2; Regs.BC2 = R.BC2; Regs.DE2 = R.DE2; Regs.HL2 = R.HL2;
		Regs.IFF1 = R.IFF1; Regs.IFF2 = R.IFF2; Regs.HALT = R.HALT; Regs.IM = R.IM; Regs.I = R.I; Regs.R = R.R; Regs.R2 = R.R2;
                Regs.pending_irq=R.pending_irq;
                Regs.pending_nmi=R.pending_nmi;
	}


	/****************************************************************************/
	/* Return program counter                                                   */
	/****************************************************************************/
	public int Z80_GetPC()
	{
		return R.PC;
	}

	public void Z80_Cause_Interrupt(int type)	/* NS 970904 */
        {
                if (type == Z80_NMI_INT)
                        R.pending_nmi = 1;
                else if (type != Z80_IGNORE_INT)
                        R.pending_irq = type;
        }

	/****************************************************************************/
	/* Execute IPeriod T-States. Return 0 if emulation should be stopped        */
	/****************************************************************************/



  public abstract interface opcode_fn
  {
    public abstract void handler();
  }
  public short[] DAATable=
    {
    68,
    256,
    512,
    772,
    1024,
    1284,
    1540,
    1792,
    2056,
    2316,
    4112,
    4372,
    4628,
    4880,
    5140,
    5392,
    4096,
    4356,
    4612,
    4864,
    5124,
    5376,
    5632,
    5892,
    6156,
    6408,
    8240,
    8500,
    8756,
    9008,
    9268,
    9520,
    8224,
    8484,
    8740,
    8992,
    9252,
    9504,
    9760,
    10020,
    10284,
    10536,
    12340,
    12592,
    12848,
    13108,
    13360,
    13620,
    12324,
    12576,
    12832,
    13092,
    13344,
    13604,
    13860,
    14112,
    14376,
    14636,
    16400,
    16660,
    16916,
    17168,
    17428,
    17680,
    16384,
    16644,
    16900,
    17152,
    17412,
    17664,
    17920,
    18180,
    18444,
    18696,
    20500,
    20752,
    21008,
    21268,
    21520,
    21780,
    20484,
    20736,
    20992,
    21252,
    21504,
    21764,
    22020,
    22272,
    22536,
    22796,
    24628,
    24880,
    25136,
    25396,
    25648,
    25908,
    24612,
    24864,
    25120,
    25380,
    25632,
    25892,
    26148,
    26400,
    26664,
    26924,
    28720,
    28980,
    29236,
    29488,
    29748,
    30000,
    28704,
    28964,
    29220,
    29472,
    29732,
    29984,
    30240,
    30500,
    30764,
    31016,
    -32624,
    -32364,
    -32108,
    -31856,
    -31596,
    -31344,
    -32640,
    -32380,
    -32124,
    -31872,
    -31612,
    -31360,
    -31104,
    -30844,
    -30580,
    -30328,
    -28524,
    -28272,
    -28016,
    -27756,
    -27504,
    -27244,
    -28540,
    -28288,
    -28032,
    -27772,
    -27520,
    -27260,
    -27004,
    -26752,
    -26488,
    -26228,
    85,
    273,
    529,
    789,
    1041,
    1301,
    69,
    257,
    513,
    773,
    1025,
    1285,
    1541,
    1793,
    2057,
    2317,
    4113,
    4373,
    4629,
    4881,
    5141,
    5393,
    4097,
    4357,
    4613,
    4865,
    5125,
    5377,
    5633,
    5893,
    6157,
    6409,
    8241,
    8501,
    8757,
    9009,
    9269,
    9521,
    8225,
    8485,
    8741,
    8993,
    9253,
    9505,
    9761,
    10021,
    10285,
    10537,
    12341,
    12593,
    12849,
    13109,
    13361,
    13621,
    12325,
    12577,
    12833,
    13093,
    13345,
    13605,
    13861,
    14113,
    14377,
    14637,
    16401,
    16661,
    16917,
    17169,
    17429,
    17681,
    16385,
    16645,
    16901,
    17153,
    17413,
    17665,
    17921,
    18181,
    18445,
    18697,
    20501,
    20753,
    21009,
    21269,
    21521,
    21781,
    20485,
    20737,
    20993,
    21253,
    21505,
    21765,
    22021,
    22273,
    22537,
    22797,
    24629,
    24881,
    25137,
    25397,
    25649,
    25909,
    24613,
    24865,
    25121,
    25381,
    25633,
    25893,
    26149,
    26401,
    26665,
    26925,
    28721,
    28981,
    29237,
    29489,
    29749,
    30001,
    28705,
    28965,
    29221,
    29473,
    29733,
    29985,
    30241,
    30501,
    30765,
    31017,
    -32623,
    -32363,
    -32107,
    -31855,
    -31595,
    -31343,
    -32639,
    -32379,
    -32123,
    -31871,
    -31611,
    -31359,
    -31103,
    -30843,
    -30579,
    -30327,
    -28523,
    -28271,
    -28015,
    -27755,
    -27503,
    -27243,
    -28539,
    -28287,
    -28031,
    -27771,
    -27519,
    -27259,
    -27003,
    -26751,
    -26487,
    -26227,
    -24395,
    -24143,
    -23887,
    -23627,
    -23375,
    -23115,
    -24411,
    -24159,
    -23903,
    -23643,
    -23391,
    -23131,
    -22875,
    -22623,
    -22359,
    -22099,
    -20303,
    -20043,
    -19787,
    -19535,
    -19275,
    -19023,
    -20319,
    -20059,
    -19803,
    -19551,
    -19291,
    -19039,
    -18783,
    -18523,
    -18259,
    -18007,
    -16235,
    -15983,
    -15727,
    -15467,
    -15215,
    -14955,
    -16251,
    -15999,
    -15743,
    -15483,
    -15231,
    -14971,
    -14715,
    -14463,
    -14199,
    -13939,
    -12143,
    -11883,
    -11627,
    -11375,
    -11115,
    -10863,
    -12159,
    -11899,
    -11643,
    -11391,
    -11131,
    -10879,
    -10623,
    -10363,
    -10099,
    -9847,
    -8015,
    -7755,
    -7499,
    -7247,
    -6987,
    -6735,
    -8031,
    -7771,
    -7515,
    -7263,
    -7003,
    -6751,
    -6495,
    -6235,
    -5971,
    -5719,
    -3915,
    -3663,
    -3407,
    -3147,
    -2895,
    -2635,
    -3931,
    -3679,
    -3423,
    -3163,
    -2911,
    -2651,
    -2395,
    -2143,
    -1879,
    -1619,
    85,
    273,
    529,
    789,
    1041,
    1301,
    69,
    257,
    513,
    773,
    1025,
    1285,
    1541,
    1793,
    2057,
    2317,
    4113,
    4373,
    4629,
    4881,
    5141,
    5393,
    4097,
    4357,
    4613,
    4865,
    5125,
    5377,
    5633,
    5893,
    6157,
    6409,
    8241,
    8501,
    8757,
    9009,
    9269,
    9521,
    8225,
    8485,
    8741,
    8993,
    9253,
    9505,
    9761,
    10021,
    10285,
    10537,
    12341,
    12593,
    12849,
    13109,
    13361,
    13621,
    12325,
    12577,
    12833,
    13093,
    13345,
    13605,
    13861,
    14113,
    14377,
    14637,
    16401,
    16661,
    16917,
    17169,
    17429,
    17681,
    16385,
    16645,
    16901,
    17153,
    17413,
    17665,
    17921,
    18181,
    18445,
    18697,
    20501,
    20753,
    21009,
    21269,
    21521,
    21781,
    20485,
    20737,
    20993,
    21253,
    21505,
    21765,
    22021,
    22273,
    22537,
    22797,
    24629,
    24881,
    25137,
    25397,
    25649,
    25909,
    1540,
    1792,
    2056,
    2316,
    2572,
    2824,
    3084,
    3336,
    3592,
    3852,
    4112,
    4372,
    4628,
    4880,
    5140,
    5392,
    5632,
    5892,
    6156,
    6408,
    6664,
    6924,
    7176,
    7436,
    7692,
    7944,
    8240,
    8500,
    8756,
    9008,
    9268,
    9520,
    9760,
    10020,
    10284,
    10536,
    10792,
    11052,
    11304,
    11564,
    11820,
    12072,
    12340,
    12592,
    12848,
    13108,
    13360,
    13620,
    13860,
    14112,
    14376,
    14636,
    14892,
    15144,
    15404,
    15656,
    15912,
    16172,
    16400,
    16660,
    16916,
    17168,
    17428,
    17680,
    17920,
    18180,
    18444,
    18696,
    18952,
    19212,
    19464,
    19724,
    19980,
    20232,
    20500,
    20752,
    21008,
    21268,
    21520,
    21780,
    22020,
    22272,
    22536,
    22796,
    23052,
    23304,
    23564,
    23816,
    24072,
    24332,
    24628,
    24880,
    25136,
    25396,
    25648,
    25908,
    26148,
    26400,
    26664,
    26924,
    27180,
    27432,
    27692,
    27944,
    28200,
    28460,
    28720,
    28980,
    29236,
    29488,
    29748,
    30000,
    30240,
    30500,
    30764,
    31016,
    31272,
    31532,
    31784,
    32044,
    32300,
    32552,
    -32624,
    -32364,
    -32108,
    -31856,
    -31596,
    -31344,
    -31104,
    -30844,
    -30580,
    -30328,
    -30072,
    -29812,
    -29560,
    -29300,
    -29044,
    -28792,
    -28524,
    -28272,
    -28016,
    -27756,
    -27504,
    -27244,
    -27004,
    -26752,
    -26488,
    -26228,
    -25972,
    -25720,
    -25460,
    -25208,
    -24952,
    -24692,
    85,
    273,
    529,
    789,
    1041,
    1301,
    1541,
    1793,
    2057,
    2317,
    2573,
    2825,
    3085,
    3337,
    3593,
    3853,
    4113,
    4373,
    4629,
    4881,
    5141,
    5393,
    5633,
    5893,
    6157,
    6409,
    6665,
    6925,
    7177,
    7437,
    7693,
    7945,
    8241,
    8501,
    8757,
    9009,
    9269,
    9521,
    9761,
    10021,
    10285,
    10537,
    10793,
    11053,
    11305,
    11565,
    11821,
    12073,
    12341,
    12593,
    12849,
    13109,
    13361,
    13621,
    13861,
    14113,
    14377,
    14637,
    14893,
    15145,
    15405,
    15657,
    15913,
    16173,
    16401,
    16661,
    16917,
    17169,
    17429,
    17681,
    17921,
    18181,
    18445,
    18697,
    18953,
    19213,
    19465,
    19725,
    19981,
    20233,
    20501,
    20753,
    21009,
    21269,
    21521,
    21781,
    22021,
    22273,
    22537,
    22797,
    23053,
    23305,
    23565,
    23817,
    24073,
    24333,
    24629,
    24881,
    25137,
    25397,
    25649,
    25909,
    26149,
    26401,
    26665,
    26925,
    27181,
    27433,
    27693,
    27945,
    28201,
    28461,
    28721,
    28981,
    29237,
    29489,
    29749,
    30001,
    30241,
    30501,
    30765,
    31017,
    31273,
    31533,
    31785,
    32045,
    32301,
    32553,
    -32623,
    -32363,
    -32107,
    -31855,
    -31595,
    -31343,
    -31103,
    -30843,
    -30579,
    -30327,
    -30071,
    -29811,
    -29559,
    -29299,
    -29043,
    -28791,
    -28523,
    -28271,
    -28015,
    -27755,
    -27503,
    -27243,
    -27003,
    -26751,
    -26487,
    -26227,
    -25971,
    -25719,
    -25459,
    -25207,
    -24951,
    -24691,
    -24395,
    -24143,
    -23887,
    -23627,
    -23375,
    -23115,
    -22875,
    -22623,
    -22359,
    -22099,
    -21843,
    -21591,
    -21331,
    -21079,
    -20823,
    -20563,
    -20303,
    -20043,
    -19787,
    -19535,
    -19275,
    -19023,
    -18783,
    -18523,
    -18259,
    -18007,
    -17751,
    -17491,
    -17239,
    -16979,
    -16723,
    -16471,
    -16235,
    -15983,
    -15727,
    -15467,
    -15215,
    -14955,
    -14715,
    -14463,
    -14199,
    -13939,
    -13683,
    -13431,
    -13171,
    -12919,
    -12663,
    -12403,
    -12143,
    -11883,
    -11627,
    -11375,
    -11115,
    -10863,
    -10623,
    -10363,
    -10099,
    -9847,
    -9591,
    -9331,
    -9079,
    -8819,
    -8563,
    -8311,
    -8015,
    -7755,
    -7499,
    -7247,
    -6987,
    -6735,
    -6495,
    -6235,
    -5971,
    -5719,
    -5463,
    -5203,
    -4951,
    -4691,
    -4435,
    -4183,
    -3915,
    -3663,
    -3407,
    -3147,
    -2895,
    -2635,
    -2395,
    -2143,
    -1879,
    -1619,
    -1363,
    -1111,
    -851,
    -599,
    -343,
    -83,
    85,
    273,
    529,
    789,
    1041,
    1301,
    1541,
    1793,
    2057,
    2317,
    2573,
    2825,
    3085,
    3337,
    3593,
    3853,
    4113,
    4373,
    4629,
    4881,
    5141,
    5393,
    5633,
    5893,
    6157,
    6409,
    6665,
    6925,
    7177,
    7437,
    7693,
    7945,
    8241,
    8501,
    8757,
    9009,
    9269,
    9521,
    9761,
    10021,
    10285,
    10537,
    10793,
    11053,
    11305,
    11565,
    11821,
    12073,
    12341,
    12593,
    12849,
    13109,
    13361,
    13621,
    13861,
    14113,
    14377,
    14637,
    14893,
    15145,
    15405,
    15657,
    15913,
    16173,
    16401,
    16661,
    16917,
    17169,
    17429,
    17681,
    17921,
    18181,
    18445,
    18697,
    18953,
    19213,
    19465,
    19725,
    19981,
    20233,
    20501,
    20753,
    21009,
    21269,
    21521,
    21781,
    22021,
    22273,
    22537,
    22797,
    23053,
    23305,
    23565,
    23817,
    24073,
    24333,
    24629,
    24881,
    25137,
    25397,
    25649,
    25909,
    70,
    258,
    514,
    774,
    1026,
    1286,
    1542,
    1794,
    2058,
    2318,
    1026,
    1286,
    1542,
    1794,
    2058,
    2318,
    4098,
    4358,
    4614,
    4866,
    5126,
    5378,
    5634,
    5894,
    6158,
    6410,
    5126,
    5378,
    5634,
    5894,
    6158,
    6410,
    8226,
    8486,
    8742,
    8994,
    9254,
    9506,
    9762,
    10022,
    10286,
    10538,
    9254,
    9506,
    9762,
    10022,
    10286,
    10538,
    12326,
    12578,
    12834,
    13094,
    13346,
    13606,
    13862,
    14114,
    14378,
    14638,
    13346,
    13606,
    13862,
    14114,
    14378,
    14638,
    16386,
    16646,
    16902,
    17154,
    17414,
    17666,
    17922,
    18182,
    18446,
    18698,
    17414,
    17666,
    17922,
    18182,
    18446,
    18698,
    20486,
    20738,
    20994,
    21254,
    21506,
    21766,
    22022,
    22274,
    22538,
    22798,
    21506,
    21766,
    22022,
    22274,
    22538,
    22798,
    24614,
    24866,
    25122,
    25382,
    25634,
    25894,
    26150,
    26402,
    26666,
    26926,
    25634,
    25894,
    26150,
    26402,
    26666,
    26926,
    28706,
    28966,
    29222,
    29474,
    29734,
    29986,
    30242,
    30502,
    30766,
    31018,
    29734,
    29986,
    30242,
    30502,
    30766,
    31018,
    -32638,
    -32378,
    -32122,
    -31870,
    -31610,
    -31358,
    -31102,
    -30842,
    -30578,
    -30326,
    -31610,
    -31358,
    -31102,
    -30842,
    -30578,
    -30326,
    -28538,
    -28286,
    -28030,
    -27770,
    -27518,
    -27258,
    -27002,
    -26750,
    -26486,
    -26226,
    13347,
    13607,
    13863,
    14115,
    14379,
    14639,
    16387,
    16647,
    16903,
    17155,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    20487,
    20739,
    20995,
    21255,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    24615,
    24867,
    25123,
    25383,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    28707,
    28967,
    29223,
    29475,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    -32637,
    -32377,
    -32121,
    -31869,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -28537,
    -28285,
    -28029,
    -27769,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225,
    -24409,
    -24157,
    -23901,
    -23641,
    -23389,
    -23129,
    -22873,
    -22621,
    -22357,
    -22097,
    -23389,
    -23129,
    -22873,
    -22621,
    -22357,
    -22097,
    -20317,
    -20057,
    -19801,
    -19549,
    -19289,
    -19037,
    -18781,
    -18521,
    -18257,
    -18005,
    -19289,
    -19037,
    -18781,
    -18521,
    -18257,
    -18005,
    -16249,
    -15997,
    -15741,
    -15481,
    -15229,
    -14969,
    -14713,
    -14461,
    -14197,
    -13937,
    -15229,
    -14969,
    -14713,
    -14461,
    -14197,
    -13937,
    -12157,
    -11897,
    -11641,
    -11389,
    -11129,
    -10877,
    -10621,
    -10361,
    -10097,
    -9845,
    -11129,
    -10877,
    -10621,
    -10361,
    -10097,
    -9845,
    -8029,
    -7769,
    -7513,
    -7261,
    -7001,
    -6749,
    -6493,
    -6233,
    -5969,
    -5717,
    -7001,
    -6749,
    -6493,
    -6233,
    -5969,
    -5717,
    -3929,
    -3677,
    -3421,
    -3161,
    -2909,
    -2649,
    -2393,
    -2141,
    -1877,
    -1617,
    -2909,
    -2649,
    -2393,
    -2141,
    -1877,
    -1617,
    71,
    259,
    515,
    775,
    1027,
    1287,
    1543,
    1795,
    2059,
    2319,
    1027,
    1287,
    1543,
    1795,
    2059,
    2319,
    4099,
    4359,
    4615,
    4867,
    5127,
    5379,
    5635,
    5895,
    6159,
    6411,
    5127,
    5379,
    5635,
    5895,
    6159,
    6411,
    8227,
    8487,
    8743,
    8995,
    9255,
    9507,
    9763,
    10023,
    10287,
    10539,
    9255,
    9507,
    9763,
    10023,
    10287,
    10539,
    12327,
    12579,
    12835,
    13095,
    13347,
    13607,
    13863,
    14115,
    14379,
    14639,
    13347,
    13607,
    13863,
    14115,
    14379,
    14639,
    16387,
    16647,
    16903,
    17155,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    20487,
    20739,
    20995,
    21255,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    24615,
    24867,
    25123,
    25383,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    28707,
    28967,
    29223,
    29475,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    -32637,
    -32377,
    -32121,
    -31869,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -28537,
    -28285,
    -28029,
    -27769,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225,
    -1346,
    -1094,
    -834,
    -582,
    -326,
    -66,
    70,
    258,
    514,
    774,
    1026,
    1286,
    1542,
    1794,
    2058,
    2318,
    2590,
    2842,
    3102,
    3354,
    3610,
    3870,
    4098,
    4358,
    4614,
    4866,
    5126,
    5378,
    5634,
    5894,
    6158,
    6410,
    6682,
    6942,
    7194,
    7454,
    7710,
    7962,
    8226,
    8486,
    8742,
    8994,
    9254,
    9506,
    9762,
    10022,
    10286,
    10538,
    10810,
    11070,
    11322,
    11582,
    11838,
    12090,
    12326,
    12578,
    12834,
    13094,
    13346,
    13606,
    13862,
    14114,
    14378,
    14638,
    14910,
    15162,
    15422,
    15674,
    15930,
    16190,
    16386,
    16646,
    16902,
    17154,
    17414,
    17666,
    17922,
    18182,
    18446,
    18698,
    18970,
    19230,
    19482,
    19742,
    19998,
    20250,
    20486,
    20738,
    20994,
    21254,
    21506,
    21766,
    22022,
    22274,
    22538,
    22798,
    23070,
    23322,
    23582,
    23834,
    24090,
    24350,
    24614,
    24866,
    25122,
    25382,
    25634,
    25894,
    26150,
    26402,
    26666,
    26926,
    27198,
    27450,
    27710,
    27962,
    28218,
    28478,
    28706,
    28966,
    29222,
    29474,
    29734,
    29986,
    30242,
    30502,
    30766,
    31018,
    31290,
    31550,
    31802,
    32062,
    32318,
    32570,
    -32638,
    -32378,
    -32122,
    -31870,
    -31610,
    -31358,
    -31102,
    -30842,
    -30578,
    -30326,
    -30054,
    -29794,
    -29542,
    -29282,
    -29026,
    -28774,
    -28538,
    -28286,
    -28030,
    -27770,
    13347,
    13607,
    13863,
    14115,
    14379,
    14639,
    14911,
    15163,
    15423,
    15675,
    15931,
    16191,
    16387,
    16647,
    16903,
    17155,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    18971,
    19231,
    19483,
    19743,
    19999,
    20251,
    20487,
    20739,
    20995,
    21255,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    23071,
    23323,
    23583,
    23835,
    24091,
    24351,
    24615,
    24867,
    25123,
    25383,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    27199,
    27451,
    27711,
    27963,
    28219,
    28479,
    28707,
    28967,
    29223,
    29475,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    31291,
    31551,
    31803,
    32063,
    32319,
    32571,
    -32637,
    -32377,
    -32121,
    -31869,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -30053,
    -29793,
    -29541,
    -29281,
    -29025,
    -28773,
    -28537,
    -28285,
    -28029,
    -27769,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225,
    -25953,
    -25701,
    -25441,
    -25189,
    -24933,
    -24673,
    -24409,
    -24157,
    -23901,
    -23641,
    -23389,
    -23129,
    -22873,
    -22621,
    -22357,
    -22097,
    -21825,
    -21573,
    -21313,
    -21061,
    -20805,
    -20545,
    -20317,
    -20057,
    -19801,
    -19549,
    -19289,
    -19037,
    -18781,
    -18521,
    -18257,
    -18005,
    -17733,
    -17473,
    -17221,
    -16961,
    -16705,
    -16453,
    -16249,
    -15997,
    -15741,
    -15481,
    -15229,
    -14969,
    -14713,
    -14461,
    -14197,
    -13937,
    -13665,
    -13413,
    -13153,
    -12901,
    -12645,
    -12385,
    -12157,
    -11897,
    -11641,
    -11389,
    -11129,
    -10877,
    -10621,
    -10361,
    -10097,
    -9845,
    -9573,
    -9313,
    -9061,
    -8801,
    -8545,
    -8293,
    -8029,
    -7769,
    -7513,
    -7261,
    -7001,
    -6749,
    -6493,
    -6233,
    -5969,
    -5717,
    -5445,
    -5185,
    -4933,
    -4673,
    -4417,
    -4165,
    -3929,
    -3677,
    -3421,
    -3161,
    -2909,
    -2649,
    -2393,
    -2141,
    -1877,
    -1617,
    -1345,
    -1093,
    -833,
    -581,
    -325,
    -65,
    71,
    259,
    515,
    775,
    1027,
    1287,
    1543,
    1795,
    2059,
    2319,
    2591,
    2843,
    3103,
    3355,
    3611,
    3871,
    4099,
    4359,
    4615,
    4867,
    5127,
    5379,
    5635,
    5895,
    6159,
    6411,
    6683,
    6943,
    7195,
    7455,
    7711,
    7963,
    8227,
    8487,
    8743,
    8995,
    9255,
    9507,
    9763,
    10023,
    10287,
    10539,
    10811,
    11071,
    11323,
    11583,
    11839,
    12091,
    12327,
    12579,
    12835,
    13095,
    13347,
    13607,
    13863,
    14115,
    14379,
    14639,
    14911,
    15163,
    15423,
    15675,
    15931,
    16191,
    16387,
    16647,
    16903,
    17155,
    17415,
    17667,
    17923,
    18183,
    18447,
    18699,
    18971,
    19231,
    19483,
    19743,
    19999,
    20251,
    20487,
    20739,
    20995,
    21255,
    21507,
    21767,
    22023,
    22275,
    22539,
    22799,
    23071,
    23323,
    23583,
    23835,
    24091,
    24351,
    24615,
    24867,
    25123,
    25383,
    25635,
    25895,
    26151,
    26403,
    26667,
    26927,
    27199,
    27451,
    27711,
    27963,
    28219,
    28479,
    28707,
    28967,
    29223,
    29475,
    29735,
    29987,
    30243,
    30503,
    30767,
    31019,
    31291,
    31551,
    31803,
    32063,
    32319,
    32571,
    -32637,
    -32377,
    -32121,
    -31869,
    -31609,
    -31357,
    -31101,
    -30841,
    -30577,
    -30325,
    -30053,
    -29793,
    -29541,
    -29281,
    -29025,
    -28773,
    -28537,
    -28285,
    -28029,
    -27769,
    -27517,
    -27257,
    -27001,
    -26749,
    -26485,
    -26225
    };
}
