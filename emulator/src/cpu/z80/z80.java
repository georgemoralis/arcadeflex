package cpu.z80;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.z80.z80H.*;


public class z80 extends cpu_interface {
    
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
    @Override
    public void init_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
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

}
