package cpu.z80;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.z80.z80H.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static mame.memoryH.*;
import static mame.mame.*;
import static mame.cpuintrf.*;


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
        
        //setup opcode tables
        SetupTables();
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
    /****************************************************************************/
    /* The Z80 registers. HALT is set to 1 when the CPU is halted, the refresh  */
    /* register is calculated as follows: refresh=(Regs.R&127)|(Regs.R2&128)    */
    /****************************************************************************/
    public static class PAIR
    {
      //L = low 8 bits
      //H = high 8 bits
      //D = whole 16 bits
      public int H,L,D;
      public void SetH(int val) 
      {
        H = val;
        D = (H << 8) | L;
      }
      public void SetL(int val) 
      {
        L = val;
        D = (H << 8) | L;
      }
      public void SetD(int val)
      {
        D = val;
        H = D >> 8 & 0xFF;
        L = D & 0xFF;
      }
      public void AddH(int val) 
      {
         H = (H + val) & 0xFF;
         D = (H << 8) | L;
      }
      public void AddL(int val)
      {
         L = (L + val) & 0xFF;
         D = (H << 8) | L;
      }
      public void AddD(int val)
      {
         D = (D + val) & 0xFFFF;
         H = D >> 8 & 0xFF;
         L = D & 0xFF;
      } 
    };
    public static class Z80_Regs
    {
        public PAIR PREPC = new PAIR();
        public PAIR PC    = new PAIR();
        public PAIR SP    = new PAIR();
        public PAIR AF    = new PAIR();
        public PAIR BC    = new PAIR();
        public PAIR DE    = new PAIR();
        public PAIR HL    = new PAIR();
        public PAIR IX    = new PAIR();
        public PAIR IY    = new PAIR();
        public PAIR AF2   = new PAIR();
        public PAIR BC2   = new PAIR();
        public PAIR DE2   = new PAIR();
        public PAIR HL2   = new PAIR();
        public int /*UINT8*/ R,R2,IFF1,IFF2,HALT,IM,I;
        public int /*UINT8*/ irq_max;         /* number of daisy chain devices        */
        public int /*INT8*/  request_irq;	/* daisy chain next request device		*/
        public int /*INT8*/  service_irq;	/* daisy chain next reti handling device */
        public int /*UINT8*/ nmi_state;	/* nmi line state */
        public int /*UINT8*/ irq_state;	/* irq line state */
        public int /*UNIT8*/ int_state[] = new int[Z80_MAXDAISY];
        public Z80_DaisyChain[] irq = new Z80_DaisyChain[Z80_MAXDAISY];
        public irqcallbacksPtr irq_callback;
        public int     extra_cycles;       /* extra cycles for interrupts */
    };
    
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

    private static Z80_Regs Z80 = new Z80_Regs();
    private static int EA;
    private static int after_EI = 0;

	
    private static int  /*UINT8*/ SZ[]        = new int[256];/* zero and sign flags */
    private static int  /*UINT8*/ SZ_BIT[]    = new int[256];/* zero, sign and parity/overflow (=zero) flags for BIT opcode */
    private static int  /*UINT8*/ SZP[]       = new int[256];/* zero, sign and parity flags */
    private static int  /*UINT8*/ SZHV_inc[]  = new int[256];/* zero, sign, half carry and overflow flags INC r8 */
    private static int  /*UINT8*/ SZHV_dec[]  = new int[256];/* zero, sign, half carry and overflow flags DEC r8 */
    
    private static int SZHVC_add[]     = new int[2 * 256 * 256];//static UINT8 *SZHVC_add = 0;
    private static int SZHVC_sub[]     = new int[2 * 256 * 256];//static UINT8 *SZHVC_sub = 0;
    /*TODO*/
    /*TODO*/
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
    static int cc_op[] = {
     4,10, 7, 6, 4, 4, 7, 4, 4,11, 7, 6, 4, 4, 7, 4,
     8,10, 7, 6, 4, 4, 7, 4,12,11, 7, 6, 4, 4, 7, 4,
     7,10,16, 6, 4, 4, 7, 4, 7,11,16, 6, 4, 4, 7, 4,
     7,10,13, 6,11,11,10, 4, 7,11,13, 6, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     7, 7, 7, 7, 7, 7, 4, 7, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
     5,10,10,10,10,11, 7,11, 5, 4,10, 0,10,10, 7,11,
     5,10,10,11,10,11, 7,11, 5, 4,10,11,10, 0, 7,11,
     5,10,10,19,10,11, 7,11, 5, 4,10, 4,10, 0, 7,11,
     5,10,10, 4,10,11, 7,11, 5, 6,10, 4,10, 0, 7,11};
    
    
    static int cc_cb[] = {
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
     8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
     8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
     8, 8, 8, 8, 8, 8,12, 8, 8, 8, 8, 8, 8, 8,12, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8,
     8, 8, 8, 8, 8, 8,15, 8, 8, 8, 8, 8, 8, 8,15, 8};
    
    static int cc_dd[] = {
     4, 4, 4, 4, 4, 4, 4, 4, 4,15, 4, 4, 4, 4, 4, 4,
     4, 4, 4, 4, 4, 4, 4, 4, 4,15, 4, 4, 4, 4, 4, 4,
     4,14,20,10, 9, 9, 9, 4, 4,15,20,10, 9, 9, 9, 4,
     4, 4, 4, 4,23,23,19, 4, 4,15, 4, 4, 4, 4, 4, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     9, 9, 9, 9, 9, 9,19, 9, 9, 9, 9, 9, 9, 9,19, 9,
    19,19,19,19,19,19, 4,19, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 9, 9,19, 4, 4, 4, 4, 4, 9, 9,19, 4,
     4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4,
     4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
     4,14, 4,23, 4,15, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4,
     4, 4, 4, 4, 4, 4, 4, 4, 4,10, 4, 4, 4, 4, 4, 4};
    
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
    
    static int cc_ed[] = {
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    12,12,15,20, 8, 8, 8, 9,12,12,15,20, 8, 8, 8, 9,
    12,12,15,20, 8, 8, 8, 9,12,12,15,20, 8, 8, 8, 9,
    12,12,15,20, 8, 8, 8,18,12,12,15,20, 8, 8, 8,18,
    12,12,15,20, 8, 8, 8, 8,12,12,15,20, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    16,16,16,16, 8, 8, 8, 8,16,16,16,16, 8, 8, 8, 8,
    16,16,16,16, 8, 8, 8, 8,16,16,16,16, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
     8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};
    
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

    /****************************************************************************/
    /* Burn an odd amount of cycles, that is instructions taking something      */
    /* different from 4 T-states per opcode (and R increment)                   */
    /****************************************************************************/
    void BURNODD(int cycles, int opcodes, int cyclesum)
    {
       if (cycles > 0)
       {
           Z80.R = (Z80.R + ((cycles / cyclesum) * opcodes)) & 0xFF;//_R += (cycles / cyclesum) * opcodes;
           z80_ICount[0] -= (cycles / cyclesum) * cyclesum;
       }
    }

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
    opcode[] Z80op = new opcode[256];
    opcode[] Z80cb = new opcode[256];
    opcode[] Z80dd = new opcode[256];
    opcode[] Z80ed = new opcode[256];
    opcode[] Z80fd = new opcode[256];
    opcode[] Z80xxcb = new opcode[256];
    
    void SetupTables()
    {
        setup_op_table();
        setup_cb_table();
        setup_dd_table();
        setup_ed_table();
        setup_fd_table();
        setup_xxcb_table();
    }

    void setup_op_table()
    {
        Z80op[0x00] = op_00; Z80op[0x01] = op_01; Z80op[0x02] = op_02; Z80op[0x03] = op_03;
        Z80op[0x04] = op_04; Z80op[0x05] = op_05; Z80op[0x06] = op_06; Z80op[0x07] = op_07;
        Z80op[0x08] = op_08; Z80op[0x09] = op_09; Z80op[0x0a] = op_0a; Z80op[0x0b] = op_0b;
        Z80op[0x0c] = op_0c; Z80op[0x0d] = op_0d; Z80op[0x0e] = op_0e; Z80op[0x0f] = op_0f;

        Z80op[0x10] = op_10; Z80op[0x11] = op_11; Z80op[0x12] = op_12; Z80op[0x13] = op_13;
        Z80op[0x14] = op_14; Z80op[0x15] = op_15; Z80op[0x16] = op_16; Z80op[0x17] = op_17;
        Z80op[0x18] = op_18; Z80op[0x19] = op_19; Z80op[0x1a] = op_1a; Z80op[0x1b] = op_1b;
        Z80op[0x1c] = op_1c; Z80op[0x1d] = op_1d; Z80op[0x1e] = op_1e; Z80op[0x1f] = op_1f;

        Z80op[0x20] = op_20; Z80op[0x21] = op_21; Z80op[0x22] = op_22; Z80op[0x23] = op_23;
        Z80op[0x24] = op_24; Z80op[0x25] = op_25; Z80op[0x26] = op_26; Z80op[0x27] = op_27;
        Z80op[0x28] = op_28; Z80op[0x29] = op_29; Z80op[0x2a] = op_2a; Z80op[0x2b] = op_2b;
        Z80op[0x2c] = op_2c; Z80op[0x2d] = op_2d; Z80op[0x2e] = op_2e; Z80op[0x2f] = op_2f;

        Z80op[0x30] = op_30; Z80op[0x31] = op_31; Z80op[0x32] = op_32; Z80op[0x33] = op_33;
        Z80op[0x34] = op_34; Z80op[0x35] = op_35; Z80op[0x36] = op_36; Z80op[0x37] = op_37;
        Z80op[0x38] = op_38; Z80op[0x39] = op_39; Z80op[0x3a] = op_3a; Z80op[0x3b] = op_3b;
        Z80op[0x3c] = op_3c; Z80op[0x3d] = op_3d; Z80op[0x3e] = op_3e; Z80op[0x3f] = op_3f;

        Z80op[0x40] = op_40; Z80op[0x41] = op_41; Z80op[0x42] = op_42; Z80op[0x43] = op_43;
        Z80op[0x44] = op_44; Z80op[0x45] = op_45; Z80op[0x46] = op_46; Z80op[0x47] = op_47;
        Z80op[0x48] = op_48; Z80op[0x49] = op_49; Z80op[0x4a] = op_4a; Z80op[0x4b] = op_4b;
        Z80op[0x4c] = op_4c; Z80op[0x4d] = op_4d; Z80op[0x4e] = op_4e; Z80op[0x4f] = op_4f;

        Z80op[0x50] = op_50; Z80op[0x51] = op_51; Z80op[0x52] = op_52; Z80op[0x53] = op_53;
        Z80op[0x54] = op_54; Z80op[0x55] = op_55; Z80op[0x56] = op_56; Z80op[0x57] = op_57;
        Z80op[0x58] = op_58; Z80op[0x59] = op_59; Z80op[0x5a] = op_5a; Z80op[0x5b] = op_5b;
        Z80op[0x5c] = op_5c; Z80op[0x5d] = op_5d; Z80op[0x5e] = op_5e; Z80op[0x5f] = op_5f;

        Z80op[0x60] = op_60; Z80op[0x61] = op_61; Z80op[0x62] = op_62; Z80op[0x63] = op_63;
        Z80op[0x64] = op_64; Z80op[0x65] = op_65; Z80op[0x66] = op_66; Z80op[0x67] = op_67;
        Z80op[0x68] = op_68; Z80op[0x69] = op_69; Z80op[0x6a] = op_6a; Z80op[0x6b] = op_6b;
        Z80op[0x6c] = op_6c; Z80op[0x6d] = op_6d; Z80op[0x6e] = op_6e; Z80op[0x6f] = op_6f;

        Z80op[0x70] = op_70; Z80op[0x71] = op_71; Z80op[0x72] = op_72; Z80op[0x73] = op_73;
        Z80op[0x74] = op_74; Z80op[0x75] = op_75; Z80op[0x76] = op_76; Z80op[0x77] = op_77;
        Z80op[0x78] = op_78; Z80op[0x79] = op_79; Z80op[0x7a] = op_7a; Z80op[0x7b] = op_7b;
        Z80op[0x7c] = op_7c; Z80op[0x7d] = op_7d; Z80op[0x7e] = op_7e; Z80op[0x7f] = op_7f;

        Z80op[0x80] = op_80; Z80op[0x81] = op_81; Z80op[0x82] = op_82; Z80op[0x83] = op_83;
        Z80op[0x84] = op_84; Z80op[0x85] = op_85; Z80op[0x86] = op_86; Z80op[0x87] = op_87;
        Z80op[0x88] = op_88; Z80op[0x89] = op_89; Z80op[0x8a] = op_8a; Z80op[0x8b] = op_8b;
        Z80op[0x8c] = op_8c; Z80op[0x8d] = op_8d; Z80op[0x8e] = op_8e; Z80op[0x8f] = op_8f;

        Z80op[0x90] = op_90; Z80op[0x91] = op_91; Z80op[0x92] = op_92; Z80op[0x93] = op_93;
        Z80op[0x94] = op_94; Z80op[0x95] = op_95; Z80op[0x96] = op_96; Z80op[0x97] = op_97;
        Z80op[0x98] = op_98; Z80op[0x99] = op_99; Z80op[0x9a] = op_9a; Z80op[0x9b] = op_9b;
        Z80op[0x9c] = op_9c; Z80op[0x9d] = op_9d; Z80op[0x9e] = op_9e; Z80op[0x9f] = op_9f;

        Z80op[0xa0] = op_a0; Z80op[0xa1] = op_a1; Z80op[0xa2] = op_a2; Z80op[0xa3] = op_a3;
        Z80op[0xa4] = op_a4; Z80op[0xa5] = op_a5; Z80op[0xa6] = op_a6; Z80op[0xa7] = op_a7;
        Z80op[0xa8] = op_a8; Z80op[0xa9] = op_a9; Z80op[0xaa] = op_aa; Z80op[0xab] = op_ab;
        Z80op[0xac] = op_ac; Z80op[0xad] = op_ad; Z80op[0xae] = op_ae; Z80op[0xaf] = op_af;

        Z80op[0xb0] = op_b0; Z80op[0xb1] = op_b1; Z80op[0xb2] = op_b2; Z80op[0xb3] = op_b3;
        Z80op[0xb4] = op_b4; Z80op[0xb5] = op_b5; Z80op[0xb6] = op_b6; Z80op[0xb7] = op_b7;
        Z80op[0xb8] = op_b8; Z80op[0xb9] = op_b9; Z80op[0xba] = op_ba; Z80op[0xbb] = op_bb; 
        Z80op[0xbc] = op_bc; Z80op[0xbd] = op_bd; Z80op[0xbe] = op_be; Z80op[0xbf] = op_bf;

        Z80op[0xc0] = op_c0; Z80op[0xc1] = op_c1; Z80op[0xc2] = op_c2; Z80op[0xc3] = op_c3;
        Z80op[0xc4] = op_c4; Z80op[0xc5] = op_c5; Z80op[0xc6] = op_c6; Z80op[0xc7] = op_c7;
        Z80op[0xc8] = op_c8; Z80op[0xc9] = op_c9; Z80op[0xca] = op_ca; Z80op[0xcb] = op_cb; 
        Z80op[0xcc] = op_cc; Z80op[0xcd] = op_cd; Z80op[0xce] = op_ce; Z80op[0xcf] = op_cf;

        Z80op[0xd0] = op_d0; Z80op[0xd1] = op_d1; Z80op[0xd2] = op_d2; Z80op[0xd3] = op_d3;
        Z80op[0xd4] = op_d4; Z80op[0xd5] = op_d5; Z80op[0xd6] = op_d6; Z80op[0xd7] = op_d7;
        Z80op[0xd8] = op_d8; Z80op[0xd9] = op_d9; Z80op[0xda] = op_da; Z80op[0xdb] = op_db;
        Z80op[0xdc] = op_dc; Z80op[0xdd] = op_dd; Z80op[0xde] = op_de; Z80op[0xdf] = op_df;

        Z80op[0xe0] = op_e0; Z80op[0xe1] = op_e1; Z80op[0xe2] = op_e2; Z80op[0xe3] = op_e3;
        Z80op[0xe4] = op_e4; Z80op[0xe5] = op_e5; Z80op[0xe6] = op_e6; Z80op[0xe7] = op_e7;
        Z80op[0xe8] = op_e8; Z80op[0xe9] = op_e9; Z80op[0xea] = op_ea; Z80op[0xeb] = op_eb;
        Z80op[0xec] = op_ec; Z80op[0xed] = op_ed; Z80op[0xee] = op_ee; Z80op[0xef] = op_ef;

        Z80op[0xf0] = op_f0; Z80op[0xf1] = op_f1; Z80op[0xf2] = op_f2; Z80op[0xf3] = op_f3;
        Z80op[0xf4] = op_f4; Z80op[0xf5] = op_f5; Z80op[0xf6] = op_f6; Z80op[0xf7] = op_f7;
        Z80op[0xf8] = op_f8; Z80op[0xf9] = op_f9; Z80op[0xfa] = op_fa; Z80op[0xfb] = op_fb; 
        Z80op[0xfc] = op_fc; Z80op[0xfd] = op_fd; Z80op[0xfe] = op_fe; Z80op[0xff] = op_ff;
    }
    void setup_cb_table()
    {
        Z80cb[0x00] = cb_00; Z80cb[0x01] = cb_01; Z80cb[0x02] = cb_02; Z80cb[0x03] = cb_03;
        Z80cb[0x04] = cb_04; Z80cb[0x05] = cb_05; Z80cb[0x06] = cb_06; Z80cb[0x07] = cb_07;
        Z80cb[0x08] = cb_08; Z80cb[0x09] = cb_09; Z80cb[0x0a] = cb_0a; Z80cb[0x0b] = cb_0b;
        Z80cb[0x0c] = cb_0c; Z80cb[0x0d] = cb_0d; Z80cb[0x0e] = cb_0e; Z80cb[0x0f] = cb_0f;

        Z80cb[0x10] = cb_10; Z80cb[0x11] = cb_11; Z80cb[0x12] = cb_12; Z80cb[0x13] = cb_13;
        Z80cb[0x14] = cb_14; Z80cb[0x15] = cb_15; Z80cb[0x16] = cb_16; Z80cb[0x17] = cb_17;
        Z80cb[0x18] = cb_18; Z80cb[0x19] = cb_19; Z80cb[0x1a] = cb_1a; Z80cb[0x1b] = cb_1b;
        Z80cb[0x1c] = cb_1c; Z80cb[0x1d] = cb_1d; Z80cb[0x1e] = cb_1e; Z80cb[0x1f] = cb_1f;

        Z80cb[0x20] = cb_20; Z80cb[0x21] = cb_21; Z80cb[0x22] = cb_22; Z80cb[0x23] = cb_23;
        Z80cb[0x24] = cb_24; Z80cb[0x25] = cb_25; Z80cb[0x26] = cb_26; Z80cb[0x27] = cb_27;
        Z80cb[0x28] = cb_28; Z80cb[0x29] = cb_29; Z80cb[0x2a] = cb_2a; Z80cb[0x2b] = cb_2b;
        Z80cb[0x2c] = cb_2c; Z80cb[0x2d] = cb_2d; Z80cb[0x2e] = cb_2e; Z80cb[0x2f] = cb_2f;

        Z80cb[0x30] = cb_30; Z80cb[0x31] = cb_31; Z80cb[0x32] = cb_32; Z80cb[0x33] = cb_33;
        Z80cb[0x34] = cb_34; Z80cb[0x35] = cb_35; Z80cb[0x36] = cb_36; Z80cb[0x37] = cb_37;
        Z80cb[0x38] = cb_38; Z80cb[0x39] = cb_39; Z80cb[0x3a] = cb_3a; Z80cb[0x3b] = cb_3b;
        Z80cb[0x3c] = cb_3c; Z80cb[0x3d] = cb_3d; Z80cb[0x3e] = cb_3e; Z80cb[0x3f] = cb_3f;

        Z80cb[0x40] = cb_40; Z80cb[0x41] = cb_41; Z80cb[0x42] = cb_42; Z80cb[0x43] = cb_43;
        Z80cb[0x44] = cb_44; Z80cb[0x45] = cb_45; Z80cb[0x46] = cb_46; Z80cb[0x47] = cb_47;
        Z80cb[0x48] = cb_48; Z80cb[0x49] = cb_49; Z80cb[0x4a] = cb_4a; Z80cb[0x4b] = cb_4b;
        Z80cb[0x4c] = cb_4c; Z80cb[0x4d] = cb_4d; Z80cb[0x4e] = cb_4e; Z80cb[0x4f] = cb_4f;

        Z80cb[0x50] = cb_50; Z80cb[0x51] = cb_51; Z80cb[0x52] = cb_52; Z80cb[0x53] = cb_53;
        Z80cb[0x54] = cb_54; Z80cb[0x55] = cb_55; Z80cb[0x56] = cb_56; Z80cb[0x57] = cb_57;
        Z80cb[0x58] = cb_58; Z80cb[0x59] = cb_59; Z80cb[0x5a] = cb_5a; Z80cb[0x5b] = cb_5b;
        Z80cb[0x5c] = cb_5c; Z80cb[0x5d] = cb_5d; Z80cb[0x5e] = cb_5e; Z80cb[0x5f] = cb_5f;

        Z80cb[0x60] = cb_60; Z80cb[0x61] = cb_61; Z80cb[0x62] = cb_62; Z80cb[0x63] = cb_63;
        Z80cb[0x64] = cb_64; Z80cb[0x65] = cb_65; Z80cb[0x66] = cb_66; Z80cb[0x67] = cb_67;
        Z80cb[0x68] = cb_68; Z80cb[0x69] = cb_69; Z80cb[0x6a] = cb_6a; Z80cb[0x6b] = cb_6b;
        Z80cb[0x6c] = cb_6c; Z80cb[0x6d] = cb_6d; Z80cb[0x6e] = cb_6e; Z80cb[0x6f] = cb_6f;

        Z80cb[0x70] = cb_70; Z80cb[0x71] = cb_71; Z80cb[0x72] = cb_72; Z80cb[0x73] = cb_73;
        Z80cb[0x74] = cb_74; Z80cb[0x75] = cb_75; Z80cb[0x76] = cb_76; Z80cb[0x77] = cb_77;
        Z80cb[0x78] = cb_78; Z80cb[0x79] = cb_79; Z80cb[0x7a] = cb_7a; Z80cb[0x7b] = cb_7b;
        Z80cb[0x7c] = cb_7c; Z80cb[0x7d] = cb_7d; Z80cb[0x7e] = cb_7e; Z80cb[0x7f] = cb_7f;

        Z80cb[0x80] = cb_80; Z80cb[0x81] = cb_81; Z80cb[0x82] = cb_82; Z80cb[0x83] = cb_83;
        Z80cb[0x84] = cb_84; Z80cb[0x85] = cb_85; Z80cb[0x86] = cb_86; Z80cb[0x87] = cb_87;
        Z80cb[0x88] = cb_88; Z80cb[0x89] = cb_89; Z80cb[0x8a] = cb_8a; Z80cb[0x8b] = cb_8b;
        Z80cb[0x8c] = cb_8c; Z80cb[0x8d] = cb_8d; Z80cb[0x8e] = cb_8e; Z80cb[0x8f] = cb_8f;

        Z80cb[0x90] = cb_90; Z80cb[0x91] = cb_91; Z80cb[0x92] = cb_92; Z80cb[0x93] = cb_93;
        Z80cb[0x94] = cb_94; Z80cb[0x95] = cb_95; Z80cb[0x96] = cb_96; Z80cb[0x97] = cb_97;
        Z80cb[0x98] = cb_98; Z80cb[0x99] = cb_99; Z80cb[0x9a] = cb_9a; Z80cb[0x9b] = cb_9b;
        Z80cb[0x9c] = cb_9c; Z80cb[0x9d] = cb_9d; Z80cb[0x9e] = cb_9e; Z80cb[0x9f] = cb_9f;

        Z80cb[0xa0] = cb_a0; Z80cb[0xa1] = cb_a1; Z80cb[0xa2] = cb_a2; Z80cb[0xa3] = cb_a3;
        Z80cb[0xa4] = cb_a4; Z80cb[0xa5] = cb_a5; Z80cb[0xa6] = cb_a6; Z80cb[0xa7] = cb_a7;
        Z80cb[0xa8] = cb_a8; Z80cb[0xa9] = cb_a9; Z80cb[0xaa] = cb_aa; Z80cb[0xab] = cb_ab;
        Z80cb[0xac] = cb_ac; Z80cb[0xad] = cb_ad; Z80cb[0xae] = cb_ae; Z80cb[0xaf] = cb_af;

        Z80cb[0xb0] = cb_b0; Z80cb[0xb1] = cb_b1; Z80cb[0xb2] = cb_b2; Z80cb[0xb3] = cb_b3;
        Z80cb[0xb4] = cb_b4; Z80cb[0xb5] = cb_b5; Z80cb[0xb6] = cb_b6; Z80cb[0xb7] = cb_b7;
        Z80cb[0xb8] = cb_b8; Z80cb[0xb9] = cb_b9; Z80cb[0xba] = cb_ba; Z80cb[0xbb] = cb_bb; 
        Z80cb[0xbc] = cb_bc; Z80cb[0xbd] = cb_bd; Z80cb[0xbe] = cb_be; Z80cb[0xbf] = cb_bf;

        Z80cb[0xc0] = cb_c0; Z80cb[0xc1] = cb_c1; Z80cb[0xc2] = cb_c2; Z80cb[0xc3] = cb_c3;
        Z80cb[0xc4] = cb_c4; Z80cb[0xc5] = cb_c5; Z80cb[0xc6] = cb_c6; Z80cb[0xc7] = cb_c7;
        Z80cb[0xc8] = cb_c8; Z80cb[0xc9] = cb_c9; Z80cb[0xca] = cb_ca; Z80cb[0xcb] = cb_cb; 
        Z80cb[0xcc] = cb_cc; Z80cb[0xcd] = cb_cd; Z80cb[0xce] = cb_ce; Z80cb[0xcf] = cb_cf;

        Z80cb[0xd0] = cb_d0; Z80cb[0xd1] = cb_d1; Z80cb[0xd2] = cb_d2; Z80cb[0xd3] = cb_d3;
        Z80cb[0xd4] = cb_d4; Z80cb[0xd5] = cb_d5; Z80cb[0xd6] = cb_d6; Z80cb[0xd7] = cb_d7;
        Z80cb[0xd8] = cb_d8; Z80cb[0xd9] = cb_d9; Z80cb[0xda] = cb_da; Z80cb[0xdb] = cb_db;
        Z80cb[0xdc] = cb_dc; Z80cb[0xdd] = cb_dd; Z80cb[0xde] = cb_de; Z80cb[0xdf] = cb_df;

        Z80cb[0xe0] = cb_e0; Z80cb[0xe1] = cb_e1; Z80cb[0xe2] = cb_e2; Z80cb[0xe3] = cb_e3;
        Z80cb[0xe4] = cb_e4; Z80cb[0xe5] = cb_e5; Z80cb[0xe6] = cb_e6; Z80cb[0xe7] = cb_e7;
        Z80cb[0xe8] = cb_e8; Z80cb[0xe9] = cb_e9; Z80cb[0xea] = cb_ea; Z80cb[0xeb] = cb_eb;
        Z80cb[0xec] = cb_ec; Z80cb[0xed] = cb_ed; Z80cb[0xee] = cb_ee; Z80cb[0xef] = cb_ef;

        Z80cb[0xf0] = cb_f0; Z80cb[0xf1] = cb_f1; Z80cb[0xf2] = cb_f2; Z80cb[0xf3] = cb_f3;
        Z80cb[0xf4] = cb_f4; Z80cb[0xf5] = cb_f5; Z80cb[0xf6] = cb_f6; Z80cb[0xf7] = cb_f7;
        Z80cb[0xf8] = cb_f8; Z80cb[0xf9] = cb_f9; Z80cb[0xfa] = cb_fa; Z80cb[0xfb] = cb_fb; 
        Z80cb[0xfc] = cb_fc; Z80cb[0xfd] = cb_fd; Z80cb[0xfe] = cb_fe; Z80cb[0xff] = cb_ff;
    }
    void setup_dd_table()
    {
        Z80dd[0x00] = dd_00; Z80dd[0x01] = dd_01; Z80dd[0x02] = dd_02; Z80dd[0x03] = dd_03;
        Z80dd[0x04] = dd_04; Z80dd[0x05] = dd_05; Z80dd[0x06] = dd_06; Z80dd[0x07] = dd_07;
        Z80dd[0x08] = dd_08; Z80dd[0x09] = dd_09; Z80dd[0x0a] = dd_0a; Z80dd[0x0b] = dd_0b;
        Z80dd[0x0c] = dd_0c; Z80dd[0x0d] = dd_0d; Z80dd[0x0e] = dd_0e; Z80dd[0x0f] = dd_0f;

        Z80dd[0x10] = dd_10; Z80dd[0x11] = dd_11; Z80dd[0x12] = dd_12; Z80dd[0x13] = dd_13;
        Z80dd[0x14] = dd_14; Z80dd[0x15] = dd_15; Z80dd[0x16] = dd_16; Z80dd[0x17] = dd_17;
        Z80dd[0x18] = dd_18; Z80dd[0x19] = dd_19; Z80dd[0x1a] = dd_1a; Z80dd[0x1b] = dd_1b;
        Z80dd[0x1c] = dd_1c; Z80dd[0x1d] = dd_1d; Z80dd[0x1e] = dd_1e; Z80dd[0x1f] = dd_1f;

        Z80dd[0x20] = dd_20; Z80dd[0x21] = dd_21; Z80dd[0x22] = dd_22; Z80dd[0x23] = dd_23;
        Z80dd[0x24] = dd_24; Z80dd[0x25] = dd_25; Z80dd[0x26] = dd_26; Z80dd[0x27] = dd_27;
        Z80dd[0x28] = dd_28; Z80dd[0x29] = dd_29; Z80dd[0x2a] = dd_2a; Z80dd[0x2b] = dd_2b;
        Z80dd[0x2c] = dd_2c; Z80dd[0x2d] = dd_2d; Z80dd[0x2e] = dd_2e; Z80dd[0x2f] = dd_2f;

        Z80dd[0x30] = dd_30; Z80dd[0x31] = dd_31; Z80dd[0x32] = dd_32; Z80dd[0x33] = dd_33;
        Z80dd[0x34] = dd_34; Z80dd[0x35] = dd_35; Z80dd[0x36] = dd_36; Z80dd[0x37] = dd_37;
        Z80dd[0x38] = dd_38; Z80dd[0x39] = dd_39; Z80dd[0x3a] = dd_3a; Z80dd[0x3b] = dd_3b;
        Z80dd[0x3c] = dd_3c; Z80dd[0x3d] = dd_3d; Z80dd[0x3e] = dd_3e; Z80dd[0x3f] = dd_3f;

        Z80dd[0x40] = dd_40; Z80dd[0x41] = dd_41; Z80dd[0x42] = dd_42; Z80dd[0x43] = dd_43;
        Z80dd[0x44] = dd_44; Z80dd[0x45] = dd_45; Z80dd[0x46] = dd_46; Z80dd[0x47] = dd_47;
        Z80dd[0x48] = dd_48; Z80dd[0x49] = dd_49; Z80dd[0x4a] = dd_4a; Z80dd[0x4b] = dd_4b;
        Z80dd[0x4c] = dd_4c; Z80dd[0x4d] = dd_4d; Z80dd[0x4e] = dd_4e; Z80dd[0x4f] = dd_4f;

        Z80dd[0x50] = dd_50; Z80dd[0x51] = dd_51; Z80dd[0x52] = dd_52; Z80dd[0x53] = dd_53;
        Z80dd[0x54] = dd_54; Z80dd[0x55] = dd_55; Z80dd[0x56] = dd_56; Z80dd[0x57] = dd_57;
        Z80dd[0x58] = dd_58; Z80dd[0x59] = dd_59; Z80dd[0x5a] = dd_5a; Z80dd[0x5b] = dd_5b;
        Z80dd[0x5c] = dd_5c; Z80dd[0x5d] = dd_5d; Z80dd[0x5e] = dd_5e; Z80dd[0x5f] = dd_5f;

        Z80dd[0x60] = dd_60; Z80dd[0x61] = dd_61; Z80dd[0x62] = dd_62; Z80dd[0x63] = dd_63;
        Z80dd[0x64] = dd_64; Z80dd[0x65] = dd_65; Z80dd[0x66] = dd_66; Z80dd[0x67] = dd_67;
        Z80dd[0x68] = dd_68; Z80dd[0x69] = dd_69; Z80dd[0x6a] = dd_6a; Z80dd[0x6b] = dd_6b;
        Z80dd[0x6c] = dd_6c; Z80dd[0x6d] = dd_6d; Z80dd[0x6e] = dd_6e; Z80dd[0x6f] = dd_6f;

        Z80dd[0x70] = dd_70; Z80dd[0x71] = dd_71; Z80dd[0x72] = dd_72; Z80dd[0x73] = dd_73;
        Z80dd[0x74] = dd_74; Z80dd[0x75] = dd_75; Z80dd[0x76] = dd_76; Z80dd[0x77] = dd_77;
        Z80dd[0x78] = dd_78; Z80dd[0x79] = dd_79; Z80dd[0x7a] = dd_7a; Z80dd[0x7b] = dd_7b;
        Z80dd[0x7c] = dd_7c; Z80dd[0x7d] = dd_7d; Z80dd[0x7e] = dd_7e; Z80dd[0x7f] = dd_7f;

        Z80dd[0x80] = dd_80; Z80dd[0x81] = dd_81; Z80dd[0x82] = dd_82; Z80dd[0x83] = dd_83;
        Z80dd[0x84] = dd_84; Z80dd[0x85] = dd_85; Z80dd[0x86] = dd_86; Z80dd[0x87] = dd_87;
        Z80dd[0x88] = dd_88; Z80dd[0x89] = dd_89; Z80dd[0x8a] = dd_8a; Z80dd[0x8b] = dd_8b;
        Z80dd[0x8c] = dd_8c; Z80dd[0x8d] = dd_8d; Z80dd[0x8e] = dd_8e; Z80dd[0x8f] = dd_8f;

        Z80dd[0x90] = dd_90; Z80dd[0x91] = dd_91; Z80dd[0x92] = dd_92; Z80dd[0x93] = dd_93;
        Z80dd[0x94] = dd_94; Z80dd[0x95] = dd_95; Z80dd[0x96] = dd_96; Z80dd[0x97] = dd_97;
        Z80dd[0x98] = dd_98; Z80dd[0x99] = dd_99; Z80dd[0x9a] = dd_9a; Z80dd[0x9b] = dd_9b;
        Z80dd[0x9c] = dd_9c; Z80dd[0x9d] = dd_9d; Z80dd[0x9e] = dd_9e; Z80dd[0x9f] = dd_9f;

        Z80dd[0xa0] = dd_a0; Z80dd[0xa1] = dd_a1; Z80dd[0xa2] = dd_a2; Z80dd[0xa3] = dd_a3;
        Z80dd[0xa4] = dd_a4; Z80dd[0xa5] = dd_a5; Z80dd[0xa6] = dd_a6; Z80dd[0xa7] = dd_a7;
        Z80dd[0xa8] = dd_a8; Z80dd[0xa9] = dd_a9; Z80dd[0xaa] = dd_aa; Z80dd[0xab] = dd_ab;
        Z80dd[0xac] = dd_ac; Z80dd[0xad] = dd_ad; Z80dd[0xae] = dd_ae; Z80dd[0xaf] = dd_af;

        Z80dd[0xb0] = dd_b0; Z80dd[0xb1] = dd_b1; Z80dd[0xb2] = dd_b2; Z80dd[0xb3] = dd_b3;
        Z80dd[0xb4] = dd_b4; Z80dd[0xb5] = dd_b5; Z80dd[0xb6] = dd_b6; Z80dd[0xb7] = dd_b7;
        Z80dd[0xb8] = dd_b8; Z80dd[0xb9] = dd_b9; Z80dd[0xba] = dd_ba; Z80dd[0xbb] = dd_bb; 
        Z80dd[0xbc] = dd_bc; Z80dd[0xbd] = dd_bd; Z80dd[0xbe] = dd_be; Z80dd[0xbf] = dd_bf;

        Z80dd[0xc0] = dd_c0; Z80dd[0xc1] = dd_c1; Z80dd[0xc2] = dd_c2; Z80dd[0xc3] = dd_c3;
        Z80dd[0xc4] = dd_c4; Z80dd[0xc5] = dd_c5; Z80dd[0xc6] = dd_c6; Z80dd[0xc7] = dd_c7;
        Z80dd[0xc8] = dd_c8; Z80dd[0xc9] = dd_c9; Z80dd[0xca] = dd_ca; Z80dd[0xcb] = dd_cb; 
        Z80dd[0xcc] = dd_cc; Z80dd[0xcd] = dd_cd; Z80dd[0xce] = dd_ce; Z80dd[0xcf] = dd_cf;

        Z80dd[0xd0] = dd_d0; Z80dd[0xd1] = dd_d1; Z80dd[0xd2] = dd_d2; Z80dd[0xd3] = dd_d3;
        Z80dd[0xd4] = dd_d4; Z80dd[0xd5] = dd_d5; Z80dd[0xd6] = dd_d6; Z80dd[0xd7] = dd_d7;
        Z80dd[0xd8] = dd_d8; Z80dd[0xd9] = dd_d9; Z80dd[0xda] = dd_da; Z80dd[0xdb] = dd_db;
        Z80dd[0xdc] = dd_dc; Z80dd[0xdd] = dd_dd; Z80dd[0xde] = dd_de; Z80dd[0xdf] = dd_df;

        Z80dd[0xe0] = dd_e0; Z80dd[0xe1] = dd_e1; Z80dd[0xe2] = dd_e2; Z80dd[0xe3] = dd_e3;
        Z80dd[0xe4] = dd_e4; Z80dd[0xe5] = dd_e5; Z80dd[0xe6] = dd_e6; Z80dd[0xe7] = dd_e7;
        Z80dd[0xe8] = dd_e8; Z80dd[0xe9] = dd_e9; Z80dd[0xea] = dd_ea; Z80dd[0xeb] = dd_eb;
        Z80dd[0xec] = dd_ec; Z80dd[0xed] = dd_ed; Z80dd[0xee] = dd_ee; Z80dd[0xef] = dd_ef;

        Z80dd[0xf0] = dd_f0; Z80dd[0xf1] = dd_f1; Z80dd[0xf2] = dd_f2; Z80dd[0xf3] = dd_f3;
        Z80dd[0xf4] = dd_f4; Z80dd[0xf5] = dd_f5; Z80dd[0xf6] = dd_f6; Z80dd[0xf7] = dd_f7;
        Z80dd[0xf8] = dd_f8; Z80dd[0xf9] = dd_f9; Z80dd[0xfa] = dd_fa; Z80dd[0xfb] = dd_fb; 
        Z80dd[0xfc] = dd_fc; Z80dd[0xfd] = dd_fd; Z80dd[0xfe] = dd_fe; Z80dd[0xff] = dd_ff;
    }
    void setup_ed_table()
    {
        Z80ed[0x00] = ed_00; Z80ed[0x01] = ed_01; Z80ed[0x02] = ed_02; Z80ed[0x03] = ed_03;
        Z80ed[0x04] = ed_04; Z80ed[0x05] = ed_05; Z80ed[0x06] = ed_06; Z80ed[0x07] = ed_07;
        Z80ed[0x08] = ed_08; Z80ed[0x09] = ed_09; Z80ed[0x0a] = ed_0a; Z80ed[0x0b] = ed_0b;
        Z80ed[0x0c] = ed_0c; Z80ed[0x0d] = ed_0d; Z80ed[0x0e] = ed_0e; Z80ed[0x0f] = ed_0f;

        Z80ed[0x10] = ed_10; Z80ed[0x11] = ed_11; Z80ed[0x12] = ed_12; Z80ed[0x13] = ed_13;
        Z80ed[0x14] = ed_14; Z80ed[0x15] = ed_15; Z80ed[0x16] = ed_16; Z80ed[0x17] = ed_17;
        Z80ed[0x18] = ed_18; Z80ed[0x19] = ed_19; Z80ed[0x1a] = ed_1a; Z80ed[0x1b] = ed_1b;
        Z80ed[0x1c] = ed_1c; Z80ed[0x1d] = ed_1d; Z80ed[0x1e] = ed_1e; Z80ed[0x1f] = ed_1f;

        Z80ed[0x20] = ed_20; Z80ed[0x21] = ed_21; Z80ed[0x22] = ed_22; Z80ed[0x23] = ed_23;
        Z80ed[0x24] = ed_24; Z80ed[0x25] = ed_25; Z80ed[0x26] = ed_26; Z80ed[0x27] = ed_27;
        Z80ed[0x28] = ed_28; Z80ed[0x29] = ed_29; Z80ed[0x2a] = ed_2a; Z80ed[0x2b] = ed_2b;
        Z80ed[0x2c] = ed_2c; Z80ed[0x2d] = ed_2d; Z80ed[0x2e] = ed_2e; Z80ed[0x2f] = ed_2f;

        Z80ed[0x30] = ed_30; Z80ed[0x31] = ed_31; Z80ed[0x32] = ed_32; Z80ed[0x33] = ed_33;
        Z80ed[0x34] = ed_34; Z80ed[0x35] = ed_35; Z80ed[0x36] = ed_36; Z80ed[0x37] = ed_37;
        Z80ed[0x38] = ed_38; Z80ed[0x39] = ed_39; Z80ed[0x3a] = ed_3a; Z80ed[0x3b] = ed_3b;
        Z80ed[0x3c] = ed_3c; Z80ed[0x3d] = ed_3d; Z80ed[0x3e] = ed_3e; Z80ed[0x3f] = ed_3f;

        Z80ed[0x40] = ed_40; Z80ed[0x41] = ed_41; Z80ed[0x42] = ed_42; Z80ed[0x43] = ed_43;
        Z80ed[0x44] = ed_44; Z80ed[0x45] = ed_45; Z80ed[0x46] = ed_46; Z80ed[0x47] = ed_47;
        Z80ed[0x48] = ed_48; Z80ed[0x49] = ed_49; Z80ed[0x4a] = ed_4a; Z80ed[0x4b] = ed_4b;
        Z80ed[0x4c] = ed_4c; Z80ed[0x4d] = ed_4d; Z80ed[0x4e] = ed_4e; Z80ed[0x4f] = ed_4f;

        Z80ed[0x50] = ed_50; Z80ed[0x51] = ed_51; Z80ed[0x52] = ed_52; Z80ed[0x53] = ed_53;
        Z80ed[0x54] = ed_54; Z80ed[0x55] = ed_55; Z80ed[0x56] = ed_56; Z80ed[0x57] = ed_57;
        Z80ed[0x58] = ed_58; Z80ed[0x59] = ed_59; Z80ed[0x5a] = ed_5a; Z80ed[0x5b] = ed_5b;
        Z80ed[0x5c] = ed_5c; Z80ed[0x5d] = ed_5d; Z80ed[0x5e] = ed_5e; Z80ed[0x5f] = ed_5f;

        Z80ed[0x60] = ed_60; Z80ed[0x61] = ed_61; Z80ed[0x62] = ed_62; Z80ed[0x63] = ed_63;
        Z80ed[0x64] = ed_64; Z80ed[0x65] = ed_65; Z80ed[0x66] = ed_66; Z80ed[0x67] = ed_67;
        Z80ed[0x68] = ed_68; Z80ed[0x69] = ed_69; Z80ed[0x6a] = ed_6a; Z80ed[0x6b] = ed_6b;
        Z80ed[0x6c] = ed_6c; Z80ed[0x6d] = ed_6d; Z80ed[0x6e] = ed_6e; Z80ed[0x6f] = ed_6f;

        Z80ed[0x70] = ed_70; Z80ed[0x71] = ed_71; Z80ed[0x72] = ed_72; Z80ed[0x73] = ed_73;
        Z80ed[0x74] = ed_74; Z80ed[0x75] = ed_75; Z80ed[0x76] = ed_76; Z80ed[0x77] = ed_77;
        Z80ed[0x78] = ed_78; Z80ed[0x79] = ed_79; Z80ed[0x7a] = ed_7a; Z80ed[0x7b] = ed_7b;
        Z80ed[0x7c] = ed_7c; Z80ed[0x7d] = ed_7d; Z80ed[0x7e] = ed_7e; Z80ed[0x7f] = ed_7f;

        Z80ed[0x80] = ed_80; Z80ed[0x81] = ed_81; Z80ed[0x82] = ed_82; Z80ed[0x83] = ed_83;
        Z80ed[0x84] = ed_84; Z80ed[0x85] = ed_85; Z80ed[0x86] = ed_86; Z80ed[0x87] = ed_87;
        Z80ed[0x88] = ed_88; Z80ed[0x89] = ed_89; Z80ed[0x8a] = ed_8a; Z80ed[0x8b] = ed_8b;
        Z80ed[0x8c] = ed_8c; Z80ed[0x8d] = ed_8d; Z80ed[0x8e] = ed_8e; Z80ed[0x8f] = ed_8f;

        Z80ed[0x90] = ed_90; Z80ed[0x91] = ed_91; Z80ed[0x92] = ed_92; Z80ed[0x93] = ed_93;
        Z80ed[0x94] = ed_94; Z80ed[0x95] = ed_95; Z80ed[0x96] = ed_96; Z80ed[0x97] = ed_97;
        Z80ed[0x98] = ed_98; Z80ed[0x99] = ed_99; Z80ed[0x9a] = ed_9a; Z80ed[0x9b] = ed_9b;
        Z80ed[0x9c] = ed_9c; Z80ed[0x9d] = ed_9d; Z80ed[0x9e] = ed_9e; Z80ed[0x9f] = ed_9f;

        Z80ed[0xa0] = ed_a0; Z80ed[0xa1] = ed_a1; Z80ed[0xa2] = ed_a2; Z80ed[0xa3] = ed_a3;
        Z80ed[0xa4] = ed_a4; Z80ed[0xa5] = ed_a5; Z80ed[0xa6] = ed_a6; Z80ed[0xa7] = ed_a7;
        Z80ed[0xa8] = ed_a8; Z80ed[0xa9] = ed_a9; Z80ed[0xaa] = ed_aa; Z80ed[0xab] = ed_ab;
        Z80ed[0xac] = ed_ac; Z80ed[0xad] = ed_ad; Z80ed[0xae] = ed_ae; Z80ed[0xaf] = ed_af;

        Z80ed[0xb0] = ed_b0; Z80ed[0xb1] = ed_b1; Z80ed[0xb2] = ed_b2; Z80ed[0xb3] = ed_b3;
        Z80ed[0xb4] = ed_b4; Z80ed[0xb5] = ed_b5; Z80ed[0xb6] = ed_b6; Z80ed[0xb7] = ed_b7;
        Z80ed[0xb8] = ed_b8; Z80ed[0xb9] = ed_b9; Z80ed[0xba] = ed_ba; Z80ed[0xbb] = ed_bb; 
        Z80ed[0xbc] = ed_bc; Z80ed[0xbd] = ed_bd; Z80ed[0xbe] = ed_be; Z80ed[0xbf] = ed_bf;

        Z80ed[0xc0] = ed_c0; Z80ed[0xc1] = ed_c1; Z80ed[0xc2] = ed_c2; Z80ed[0xc3] = ed_c3;
        Z80ed[0xc4] = ed_c4; Z80ed[0xc5] = ed_c5; Z80ed[0xc6] = ed_c6; Z80ed[0xc7] = ed_c7;
        Z80ed[0xc8] = ed_c8; Z80ed[0xc9] = ed_c9; Z80ed[0xca] = ed_ca; Z80ed[0xcb] = ed_cb; 
        Z80ed[0xcc] = ed_cc; Z80ed[0xcd] = ed_cd; Z80ed[0xce] = ed_ce; Z80ed[0xcf] = ed_cf;

        Z80ed[0xd0] = ed_d0; Z80ed[0xd1] = ed_d1; Z80ed[0xd2] = ed_d2; Z80ed[0xd3] = ed_d3;
        Z80ed[0xd4] = ed_d4; Z80ed[0xd5] = ed_d5; Z80ed[0xd6] = ed_d6; Z80ed[0xd7] = ed_d7;
        Z80ed[0xd8] = ed_d8; Z80ed[0xd9] = ed_d9; Z80ed[0xda] = ed_da; Z80ed[0xdb] = ed_db;
        Z80ed[0xdc] = ed_dc; Z80ed[0xdd] = ed_dd; Z80ed[0xde] = ed_de; Z80ed[0xdf] = ed_df;

        Z80ed[0xe0] = ed_e0; Z80ed[0xe1] = ed_e1; Z80ed[0xe2] = ed_e2; Z80ed[0xe3] = ed_e3;
        Z80ed[0xe4] = ed_e4; Z80ed[0xe5] = ed_e5; Z80ed[0xe6] = ed_e6; Z80ed[0xe7] = ed_e7;
        Z80ed[0xe8] = ed_e8; Z80ed[0xe9] = ed_e9; Z80ed[0xea] = ed_ea; Z80ed[0xeb] = ed_eb;
        Z80ed[0xec] = ed_ec; Z80ed[0xed] = ed_ed; Z80ed[0xee] = ed_ee; Z80ed[0xef] = ed_ef;

        Z80ed[0xf0] = ed_f0; Z80ed[0xf1] = ed_f1; Z80ed[0xf2] = ed_f2; Z80ed[0xf3] = ed_f3;
        Z80ed[0xf4] = ed_f4; Z80ed[0xf5] = ed_f5; Z80ed[0xf6] = ed_f6; Z80ed[0xf7] = ed_f7;
        Z80ed[0xf8] = ed_f8; Z80ed[0xf9] = ed_f9; Z80ed[0xfa] = ed_fa; Z80ed[0xfb] = ed_fb; 
        Z80ed[0xfc] = ed_fc; Z80ed[0xfd] = ed_fd; Z80ed[0xfe] = ed_fe; Z80ed[0xff] = ed_ff;
    }
    void setup_fd_table()
    {
        Z80fd[0x00] = fd_00; Z80fd[0x01] = fd_01; Z80fd[0x02] = fd_02; Z80fd[0x03] = fd_03;
        Z80fd[0x04] = fd_04; Z80fd[0x05] = fd_05; Z80fd[0x06] = fd_06; Z80fd[0x07] = fd_07;
        Z80fd[0x08] = fd_08; Z80fd[0x09] = fd_09; Z80fd[0x0a] = fd_0a; Z80fd[0x0b] = fd_0b;
        Z80fd[0x0c] = fd_0c; Z80fd[0x0d] = fd_0d; Z80fd[0x0e] = fd_0e; Z80fd[0x0f] = fd_0f;

        Z80fd[0x10] = fd_10; Z80fd[0x11] = fd_11; Z80fd[0x12] = fd_12; Z80fd[0x13] = fd_13;
        Z80fd[0x14] = fd_14; Z80fd[0x15] = fd_15; Z80fd[0x16] = fd_16; Z80fd[0x17] = fd_17;
        Z80fd[0x18] = fd_18; Z80fd[0x19] = fd_19; Z80fd[0x1a] = fd_1a; Z80fd[0x1b] = fd_1b;
        Z80fd[0x1c] = fd_1c; Z80fd[0x1d] = fd_1d; Z80fd[0x1e] = fd_1e; Z80fd[0x1f] = fd_1f;

        Z80fd[0x20] = fd_20; Z80fd[0x21] = fd_21; Z80fd[0x22] = fd_22; Z80fd[0x23] = fd_23;
        Z80fd[0x24] = fd_24; Z80fd[0x25] = fd_25; Z80fd[0x26] = fd_26; Z80fd[0x27] = fd_27;
        Z80fd[0x28] = fd_28; Z80fd[0x29] = fd_29; Z80fd[0x2a] = fd_2a; Z80fd[0x2b] = fd_2b;
        Z80fd[0x2c] = fd_2c; Z80fd[0x2d] = fd_2d; Z80fd[0x2e] = fd_2e; Z80fd[0x2f] = fd_2f;

        Z80fd[0x30] = fd_30; Z80fd[0x31] = fd_31; Z80fd[0x32] = fd_32; Z80fd[0x33] = fd_33;
        Z80fd[0x34] = fd_34; Z80fd[0x35] = fd_35; Z80fd[0x36] = fd_36; Z80fd[0x37] = fd_37;
        Z80fd[0x38] = fd_38; Z80fd[0x39] = fd_39; Z80fd[0x3a] = fd_3a; Z80fd[0x3b] = fd_3b;
        Z80fd[0x3c] = fd_3c; Z80fd[0x3d] = fd_3d; Z80fd[0x3e] = fd_3e; Z80fd[0x3f] = fd_3f;

        Z80fd[0x40] = fd_40; Z80fd[0x41] = fd_41; Z80fd[0x42] = fd_42; Z80fd[0x43] = fd_43;
        Z80fd[0x44] = fd_44; Z80fd[0x45] = fd_45; Z80fd[0x46] = fd_46; Z80fd[0x47] = fd_47;
        Z80fd[0x48] = fd_48; Z80fd[0x49] = fd_49; Z80fd[0x4a] = fd_4a; Z80fd[0x4b] = fd_4b;
        Z80fd[0x4c] = fd_4c; Z80fd[0x4d] = fd_4d; Z80fd[0x4e] = fd_4e; Z80fd[0x4f] = fd_4f;

        Z80fd[0x50] = fd_50; Z80fd[0x51] = fd_51; Z80fd[0x52] = fd_52; Z80fd[0x53] = fd_53;
        Z80fd[0x54] = fd_54; Z80fd[0x55] = fd_55; Z80fd[0x56] = fd_56; Z80fd[0x57] = fd_57;
        Z80fd[0x58] = fd_58; Z80fd[0x59] = fd_59; Z80fd[0x5a] = fd_5a; Z80fd[0x5b] = fd_5b;
        Z80fd[0x5c] = fd_5c; Z80fd[0x5d] = fd_5d; Z80fd[0x5e] = fd_5e; Z80fd[0x5f] = fd_5f;

        Z80fd[0x60] = fd_60; Z80fd[0x61] = fd_61; Z80fd[0x62] = fd_62; Z80fd[0x63] = fd_63;
        Z80fd[0x64] = fd_64; Z80fd[0x65] = fd_65; Z80fd[0x66] = fd_66; Z80fd[0x67] = fd_67;
        Z80fd[0x68] = fd_68; Z80fd[0x69] = fd_69; Z80fd[0x6a] = fd_6a; Z80fd[0x6b] = fd_6b;
        Z80fd[0x6c] = fd_6c; Z80fd[0x6d] = fd_6d; Z80fd[0x6e] = fd_6e; Z80fd[0x6f] = fd_6f;

        Z80fd[0x70] = fd_70; Z80fd[0x71] = fd_71; Z80fd[0x72] = fd_72; Z80fd[0x73] = fd_73;
        Z80fd[0x74] = fd_74; Z80fd[0x75] = fd_75; Z80fd[0x76] = fd_76; Z80fd[0x77] = fd_77;
        Z80fd[0x78] = fd_78; Z80fd[0x79] = fd_79; Z80fd[0x7a] = fd_7a; Z80fd[0x7b] = fd_7b;
        Z80fd[0x7c] = fd_7c; Z80fd[0x7d] = fd_7d; Z80fd[0x7e] = fd_7e; Z80fd[0x7f] = fd_7f;

        Z80fd[0x80] = fd_80; Z80fd[0x81] = fd_81; Z80fd[0x82] = fd_82; Z80fd[0x83] = fd_83;
        Z80fd[0x84] = fd_84; Z80fd[0x85] = fd_85; Z80fd[0x86] = fd_86; Z80fd[0x87] = fd_87;
        Z80fd[0x88] = fd_88; Z80fd[0x89] = fd_89; Z80fd[0x8a] = fd_8a; Z80fd[0x8b] = fd_8b;
        Z80fd[0x8c] = fd_8c; Z80fd[0x8d] = fd_8d; Z80fd[0x8e] = fd_8e; Z80fd[0x8f] = fd_8f;

        Z80fd[0x90] = fd_90; Z80fd[0x91] = fd_91; Z80fd[0x92] = fd_92; Z80fd[0x93] = fd_93;
        Z80fd[0x94] = fd_94; Z80fd[0x95] = fd_95; Z80fd[0x96] = fd_96; Z80fd[0x97] = fd_97;
        Z80fd[0x98] = fd_98; Z80fd[0x99] = fd_99; Z80fd[0x9a] = fd_9a; Z80fd[0x9b] = fd_9b;
        Z80fd[0x9c] = fd_9c; Z80fd[0x9d] = fd_9d; Z80fd[0x9e] = fd_9e; Z80fd[0x9f] = fd_9f;

        Z80fd[0xa0] = fd_a0; Z80fd[0xa1] = fd_a1; Z80fd[0xa2] = fd_a2; Z80fd[0xa3] = fd_a3;
        Z80fd[0xa4] = fd_a4; Z80fd[0xa5] = fd_a5; Z80fd[0xa6] = fd_a6; Z80fd[0xa7] = fd_a7;
        Z80fd[0xa8] = fd_a8; Z80fd[0xa9] = fd_a9; Z80fd[0xaa] = fd_aa; Z80fd[0xab] = fd_ab;
        Z80fd[0xac] = fd_ac; Z80fd[0xad] = fd_ad; Z80fd[0xae] = fd_ae; Z80fd[0xaf] = fd_af;

        Z80fd[0xb0] = fd_b0; Z80fd[0xb1] = fd_b1; Z80fd[0xb2] = fd_b2; Z80fd[0xb3] = fd_b3;
        Z80fd[0xb4] = fd_b4; Z80fd[0xb5] = fd_b5; Z80fd[0xb6] = fd_b6; Z80fd[0xb7] = fd_b7;
        Z80fd[0xb8] = fd_b8; Z80fd[0xb9] = fd_b9; Z80fd[0xba] = fd_ba; Z80fd[0xbb] = fd_bb; 
        Z80fd[0xbc] = fd_bc; Z80fd[0xbd] = fd_bd; Z80fd[0xbe] = fd_be; Z80fd[0xbf] = fd_bf;

        Z80fd[0xc0] = fd_c0; Z80fd[0xc1] = fd_c1; Z80fd[0xc2] = fd_c2; Z80fd[0xc3] = fd_c3;
        Z80fd[0xc4] = fd_c4; Z80fd[0xc5] = fd_c5; Z80fd[0xc6] = fd_c6; Z80fd[0xc7] = fd_c7;
        Z80fd[0xc8] = fd_c8; Z80fd[0xc9] = fd_c9; Z80fd[0xca] = fd_ca; Z80fd[0xcb] = fd_cb; 
        Z80fd[0xcc] = fd_cc; Z80fd[0xcd] = fd_cd; Z80fd[0xce] = fd_ce; Z80fd[0xcf] = fd_cf;

        Z80fd[0xd0] = fd_d0; Z80fd[0xd1] = fd_d1; Z80fd[0xd2] = fd_d2; Z80fd[0xd3] = fd_d3;
        Z80fd[0xd4] = fd_d4; Z80fd[0xd5] = fd_d5; Z80fd[0xd6] = fd_d6; Z80fd[0xd7] = fd_d7;
        Z80fd[0xd8] = fd_d8; Z80fd[0xd9] = fd_d9; Z80fd[0xda] = fd_da; Z80fd[0xdb] = fd_db;
        Z80fd[0xdc] = fd_dc; Z80fd[0xdd] = fd_dd; Z80fd[0xde] = fd_de; Z80fd[0xdf] = fd_df;

        Z80fd[0xe0] = fd_e0; Z80fd[0xe1] = fd_e1; Z80fd[0xe2] = fd_e2; Z80fd[0xe3] = fd_e3;
        Z80fd[0xe4] = fd_e4; Z80fd[0xe5] = fd_e5; Z80fd[0xe6] = fd_e6; Z80fd[0xe7] = fd_e7;
        Z80fd[0xe8] = fd_e8; Z80fd[0xe9] = fd_e9; Z80fd[0xea] = fd_ea; Z80fd[0xeb] = fd_eb;
        Z80fd[0xec] = fd_ec; Z80fd[0xed] = fd_ed; Z80fd[0xee] = fd_ee; Z80fd[0xef] = fd_ef;

        Z80fd[0xf0] = fd_f0; Z80fd[0xf1] = fd_f1; Z80fd[0xf2] = fd_f2; Z80fd[0xf3] = fd_f3;
        Z80fd[0xf4] = fd_f4; Z80fd[0xf5] = fd_f5; Z80fd[0xf6] = fd_f6; Z80fd[0xf7] = fd_f7;
        Z80fd[0xf8] = fd_f8; Z80fd[0xf9] = fd_f9; Z80fd[0xfa] = fd_fa; Z80fd[0xfb] = fd_fb; 
        Z80fd[0xfc] = fd_fc; Z80fd[0xfd] = fd_fd; Z80fd[0xfe] = fd_fe; Z80fd[0xff] = fd_ff;
    }
        void setup_xxcb_table()
    {
        Z80xxcb[0x00] = xxcb_00; Z80xxcb[0x01] = xxcb_01; Z80xxcb[0x02] = xxcb_02; Z80xxcb[0x03] = xxcb_03;
        Z80xxcb[0x04] = xxcb_04; Z80xxcb[0x05] = xxcb_05; Z80xxcb[0x06] = xxcb_06; Z80xxcb[0x07] = xxcb_07;
        Z80xxcb[0x08] = xxcb_08; Z80xxcb[0x09] = xxcb_09; Z80xxcb[0x0a] = xxcb_0a; Z80xxcb[0x0b] = xxcb_0b;
        Z80xxcb[0x0c] = xxcb_0c; Z80xxcb[0x0d] = xxcb_0d; Z80xxcb[0x0e] = xxcb_0e; Z80xxcb[0x0f] = xxcb_0f;

        Z80xxcb[0x10] = xxcb_10; Z80xxcb[0x11] = xxcb_11; Z80xxcb[0x12] = xxcb_12; Z80xxcb[0x13] = xxcb_13;
        Z80xxcb[0x14] = xxcb_14; Z80xxcb[0x15] = xxcb_15; Z80xxcb[0x16] = xxcb_16; Z80xxcb[0x17] = xxcb_17;
        Z80xxcb[0x18] = xxcb_18; Z80xxcb[0x19] = xxcb_19; Z80xxcb[0x1a] = xxcb_1a; Z80xxcb[0x1b] = xxcb_1b;
        Z80xxcb[0x1c] = xxcb_1c; Z80xxcb[0x1d] = xxcb_1d; Z80xxcb[0x1e] = xxcb_1e; Z80xxcb[0x1f] = xxcb_1f;

        Z80xxcb[0x20] = xxcb_20; Z80xxcb[0x21] = xxcb_21; Z80xxcb[0x22] = xxcb_22; Z80xxcb[0x23] = xxcb_23;
        Z80xxcb[0x24] = xxcb_24; Z80xxcb[0x25] = xxcb_25; Z80xxcb[0x26] = xxcb_26; Z80xxcb[0x27] = xxcb_27;
        Z80xxcb[0x28] = xxcb_28; Z80xxcb[0x29] = xxcb_29; Z80xxcb[0x2a] = xxcb_2a; Z80xxcb[0x2b] = xxcb_2b;
        Z80xxcb[0x2c] = xxcb_2c; Z80xxcb[0x2d] = xxcb_2d; Z80xxcb[0x2e] = xxcb_2e; Z80xxcb[0x2f] = xxcb_2f;

        Z80xxcb[0x30] = xxcb_30; Z80xxcb[0x31] = xxcb_31; Z80xxcb[0x32] = xxcb_32; Z80xxcb[0x33] = xxcb_33;
        Z80xxcb[0x34] = xxcb_34; Z80xxcb[0x35] = xxcb_35; Z80xxcb[0x36] = xxcb_36; Z80xxcb[0x37] = xxcb_37;
        Z80xxcb[0x38] = xxcb_38; Z80xxcb[0x39] = xxcb_39; Z80xxcb[0x3a] = xxcb_3a; Z80xxcb[0x3b] = xxcb_3b;
        Z80xxcb[0x3c] = xxcb_3c; Z80xxcb[0x3d] = xxcb_3d; Z80xxcb[0x3e] = xxcb_3e; Z80xxcb[0x3f] = xxcb_3f;

        Z80xxcb[0x40] = xxcb_40; Z80xxcb[0x41] = xxcb_41; Z80xxcb[0x42] = xxcb_42; Z80xxcb[0x43] = xxcb_43;
        Z80xxcb[0x44] = xxcb_44; Z80xxcb[0x45] = xxcb_45; Z80xxcb[0x46] = xxcb_46; Z80xxcb[0x47] = xxcb_47;
        Z80xxcb[0x48] = xxcb_48; Z80xxcb[0x49] = xxcb_49; Z80xxcb[0x4a] = xxcb_4a; Z80xxcb[0x4b] = xxcb_4b;
        Z80xxcb[0x4c] = xxcb_4c; Z80xxcb[0x4d] = xxcb_4d; Z80xxcb[0x4e] = xxcb_4e; Z80xxcb[0x4f] = xxcb_4f;

        Z80xxcb[0x50] = xxcb_50; Z80xxcb[0x51] = xxcb_51; Z80xxcb[0x52] = xxcb_52; Z80xxcb[0x53] = xxcb_53;
        Z80xxcb[0x54] = xxcb_54; Z80xxcb[0x55] = xxcb_55; Z80xxcb[0x56] = xxcb_56; Z80xxcb[0x57] = xxcb_57;
        Z80xxcb[0x58] = xxcb_58; Z80xxcb[0x59] = xxcb_59; Z80xxcb[0x5a] = xxcb_5a; Z80xxcb[0x5b] = xxcb_5b;
        Z80xxcb[0x5c] = xxcb_5c; Z80xxcb[0x5d] = xxcb_5d; Z80xxcb[0x5e] = xxcb_5e; Z80xxcb[0x5f] = xxcb_5f;

        Z80xxcb[0x60] = xxcb_60; Z80xxcb[0x61] = xxcb_61; Z80xxcb[0x62] = xxcb_62; Z80xxcb[0x63] = xxcb_63;
        Z80xxcb[0x64] = xxcb_64; Z80xxcb[0x65] = xxcb_65; Z80xxcb[0x66] = xxcb_66; Z80xxcb[0x67] = xxcb_67;
        Z80xxcb[0x68] = xxcb_68; Z80xxcb[0x69] = xxcb_69; Z80xxcb[0x6a] = xxcb_6a; Z80xxcb[0x6b] = xxcb_6b;
        Z80xxcb[0x6c] = xxcb_6c; Z80xxcb[0x6d] = xxcb_6d; Z80xxcb[0x6e] = xxcb_6e; Z80xxcb[0x6f] = xxcb_6f;

        Z80xxcb[0x70] = xxcb_70; Z80xxcb[0x71] = xxcb_71; Z80xxcb[0x72] = xxcb_72; Z80xxcb[0x73] = xxcb_73;
        Z80xxcb[0x74] = xxcb_74; Z80xxcb[0x75] = xxcb_75; Z80xxcb[0x76] = xxcb_76; Z80xxcb[0x77] = xxcb_77;
        Z80xxcb[0x78] = xxcb_78; Z80xxcb[0x79] = xxcb_79; Z80xxcb[0x7a] = xxcb_7a; Z80xxcb[0x7b] = xxcb_7b;
        Z80xxcb[0x7c] = xxcb_7c; Z80xxcb[0x7d] = xxcb_7d; Z80xxcb[0x7e] = xxcb_7e; Z80xxcb[0x7f] = xxcb_7f;

        Z80xxcb[0x80] = xxcb_80; Z80xxcb[0x81] = xxcb_81; Z80xxcb[0x82] = xxcb_82; Z80xxcb[0x83] = xxcb_83;
        Z80xxcb[0x84] = xxcb_84; Z80xxcb[0x85] = xxcb_85; Z80xxcb[0x86] = xxcb_86; Z80xxcb[0x87] = xxcb_87;
        Z80xxcb[0x88] = xxcb_88; Z80xxcb[0x89] = xxcb_89; Z80xxcb[0x8a] = xxcb_8a; Z80xxcb[0x8b] = xxcb_8b;
        Z80xxcb[0x8c] = xxcb_8c; Z80xxcb[0x8d] = xxcb_8d; Z80xxcb[0x8e] = xxcb_8e; Z80xxcb[0x8f] = xxcb_8f;

        Z80xxcb[0x90] = xxcb_90; Z80xxcb[0x91] = xxcb_91; Z80xxcb[0x92] = xxcb_92; Z80xxcb[0x93] = xxcb_93;
        Z80xxcb[0x94] = xxcb_94; Z80xxcb[0x95] = xxcb_95; Z80xxcb[0x96] = xxcb_96; Z80xxcb[0x97] = xxcb_97;
        Z80xxcb[0x98] = xxcb_98; Z80xxcb[0x99] = xxcb_99; Z80xxcb[0x9a] = xxcb_9a; Z80xxcb[0x9b] = xxcb_9b;
        Z80xxcb[0x9c] = xxcb_9c; Z80xxcb[0x9d] = xxcb_9d; Z80xxcb[0x9e] = xxcb_9e; Z80xxcb[0x9f] = xxcb_9f;

        Z80xxcb[0xa0] = xxcb_a0; Z80xxcb[0xa1] = xxcb_a1; Z80xxcb[0xa2] = xxcb_a2; Z80xxcb[0xa3] = xxcb_a3;
        Z80xxcb[0xa4] = xxcb_a4; Z80xxcb[0xa5] = xxcb_a5; Z80xxcb[0xa6] = xxcb_a6; Z80xxcb[0xa7] = xxcb_a7;
        Z80xxcb[0xa8] = xxcb_a8; Z80xxcb[0xa9] = xxcb_a9; Z80xxcb[0xaa] = xxcb_aa; Z80xxcb[0xab] = xxcb_ab;
        Z80xxcb[0xac] = xxcb_ac; Z80xxcb[0xad] = xxcb_ad; Z80xxcb[0xae] = xxcb_ae; Z80xxcb[0xaf] = xxcb_af;

        Z80xxcb[0xb0] = xxcb_b0; Z80xxcb[0xb1] = xxcb_b1; Z80xxcb[0xb2] = xxcb_b2; Z80xxcb[0xb3] = xxcb_b3;
        Z80xxcb[0xb4] = xxcb_b4; Z80xxcb[0xb5] = xxcb_b5; Z80xxcb[0xb6] = xxcb_b6; Z80xxcb[0xb7] = xxcb_b7;
        Z80xxcb[0xb8] = xxcb_b8; Z80xxcb[0xb9] = xxcb_b9; Z80xxcb[0xba] = xxcb_ba; Z80xxcb[0xbb] = xxcb_bb; 
        Z80xxcb[0xbc] = xxcb_bc; Z80xxcb[0xbd] = xxcb_bd; Z80xxcb[0xbe] = xxcb_be; Z80xxcb[0xbf] = xxcb_bf;

        Z80xxcb[0xc0] = xxcb_c0; Z80xxcb[0xc1] = xxcb_c1; Z80xxcb[0xc2] = xxcb_c2; Z80xxcb[0xc3] = xxcb_c3;
        Z80xxcb[0xc4] = xxcb_c4; Z80xxcb[0xc5] = xxcb_c5; Z80xxcb[0xc6] = xxcb_c6; Z80xxcb[0xc7] = xxcb_c7;
        Z80xxcb[0xc8] = xxcb_c8; Z80xxcb[0xc9] = xxcb_c9; Z80xxcb[0xca] = xxcb_ca; Z80xxcb[0xcb] = xxcb_cb; 
        Z80xxcb[0xcc] = xxcb_cc; Z80xxcb[0xcd] = xxcb_cd; Z80xxcb[0xce] = xxcb_ce; Z80xxcb[0xcf] = xxcb_cf;

        Z80xxcb[0xd0] = xxcb_d0; Z80xxcb[0xd1] = xxcb_d1; Z80xxcb[0xd2] = xxcb_d2; Z80xxcb[0xd3] = xxcb_d3;
        Z80xxcb[0xd4] = xxcb_d4; Z80xxcb[0xd5] = xxcb_d5; Z80xxcb[0xd6] = xxcb_d6; Z80xxcb[0xd7] = xxcb_d7;
        Z80xxcb[0xd8] = xxcb_d8; Z80xxcb[0xd9] = xxcb_d9; Z80xxcb[0xda] = xxcb_da; Z80xxcb[0xdb] = xxcb_db;
        Z80xxcb[0xdc] = xxcb_dc; Z80xxcb[0xdd] = xxcb_dd; Z80xxcb[0xde] = xxcb_de; Z80xxcb[0xdf] = xxcb_df;

        Z80xxcb[0xe0] = xxcb_e0; Z80xxcb[0xe1] = xxcb_e1; Z80xxcb[0xe2] = xxcb_e2; Z80xxcb[0xe3] = xxcb_e3;
        Z80xxcb[0xe4] = xxcb_e4; Z80xxcb[0xe5] = xxcb_e5; Z80xxcb[0xe6] = xxcb_e6; Z80xxcb[0xe7] = xxcb_e7;
        Z80xxcb[0xe8] = xxcb_e8; Z80xxcb[0xe9] = xxcb_e9; Z80xxcb[0xea] = xxcb_ea; Z80xxcb[0xeb] = xxcb_eb;
        Z80xxcb[0xec] = xxcb_ec; Z80xxcb[0xed] = xxcb_ed; Z80xxcb[0xee] = xxcb_ee; Z80xxcb[0xef] = xxcb_ef;

        Z80xxcb[0xf0] = xxcb_f0; Z80xxcb[0xf1] = xxcb_f1; Z80xxcb[0xf2] = xxcb_f2; Z80xxcb[0xf3] = xxcb_f3;
        Z80xxcb[0xf4] = xxcb_f4; Z80xxcb[0xf5] = xxcb_f5; Z80xxcb[0xf6] = xxcb_f6; Z80xxcb[0xf7] = xxcb_f7;
        Z80xxcb[0xf8] = xxcb_f8; Z80xxcb[0xf9] = xxcb_f9; Z80xxcb[0xfa] = xxcb_fa; Z80xxcb[0xfb] = xxcb_fb; 
        Z80xxcb[0xfc] = xxcb_fc; Z80xxcb[0xfd] = xxcb_fd; Z80xxcb[0xfe] = xxcb_fe; Z80xxcb[0xff] = xxcb_ff;
    }
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
        
    
    /***************************************************************
     * ROP() is identical to RM() except it is used for
     * reading opcodes. In case of system with memory mapped I/O,
     * this function can be used to greatly speed up emulation
     ***************************************************************/
    public char ROP()
    {
    	int pc = Z80.PC.D;
    	Z80.PC.AddD(1); //_PC++
    	return cpu_readop(pc);
    }
    
    /****************************************************************
     * ARG() is identical to ROP() except it is used
     * for reading opcode arguments. This difference can be used to
     * support systems that use different encoding mechanisms for
     * opcodes and opcode arguments
     ***************************************************************/
    public char ARG()
    {
    	int pc = Z80.PC.D;
    	Z80.PC.AddD(1); //_PC++
    	return cpu_readop_arg(pc);
    }
    public char ARG16()
    {
        int pc = Z80.PC.D;
        Z80.PC.AddD(2);
        return (char)(cpu_readop_arg(pc) | (cpu_readop_arg((pc+1)&0xffff) << 8));
    }

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
    /*TODO*///

    /*TODO*////***************************************************************
    /*TODO*/// * RST
    /*TODO*/// ***************************************************************/
    /*TODO*///#define RST(addr)												\
    /*TODO*///	PUSH( PC ); 												\
    /*TODO*///	_PCD = addr;												\
    /*TODO*///	change_pc16(_PCD)
    
    /***************************************************************
     * INC	r8
     ***************************************************************/
    public int INC(int value) {
        value = (value + 1) & 0xff;
        Z80.AF.SetL(((Z80.AF.L & CF) | SZHV_inc[value]) & 0xFF);
        return value;
    }
    
    /***************************************************************
     * DEC	r8
     ***************************************************************/
    public int DEC(int value)
    {
        value = (value-1) & 0xFF;
        Z80.AF.SetL(((Z80.AF.L & CF) | SZHV_dec[value]) & 0xFF);
        return value;
    }
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
    
    /***************************************************************
     * ADD	A,n
    ***************************************************************/
    public void ADD(int value)
    {
        int ah = Z80.AF.D & 0xff00;
        if(ah <= -1)//check unsigness TODO crazy check to be removed
            throw new UnsupportedOperationException("that shouldn't happend");
        int res = ((ah>>8) + value) & 0xFF;
        Z80.AF.SetL(SZHVC_add[ah | res]);
        Z80.AF.SetH(res & 0xFF);
    }
    /***************************************************************
     * ADC	A,n
     ***************************************************************/
    public void ADC(int value)
    {
        int ah = Z80.AF.D & 0xff00;
        int c = Z80.AF.D & 1;
        if(ah <= -1 || c <=-1)//check unsigness TODO crazy check to be removed
            throw new UnsupportedOperationException("that shouldn't happend");
        int res = ((ah>>8) + value + c) & 0xFF;
        Z80.AF.SetL(SZHVC_add[(c << 16) | ah | res]);
        Z80.AF.SetH(res & 0xFF);
    }
    /*TODO*///#define ADC(value)												\
    /*TODO*///{																\
    /*TODO*///	UINT32 ah = _AFD & 0xff00, c = _AFD & 1;					\
    /*TODO*///	UINT32 res = (UINT8)((ah >> 8) + value + c);				\
    /*TODO*///	_F = SZHVC_add[(c << 16) | ah | res];						\
    /*TODO*///    _A = res;                                                   \
    /*TODO*///}

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
    public void SUB(int value)
    {
        int ah = Z80.AF.D & 0xff00;
        if(ah <= -1)//check unsigness TODO crazy check to be removed
            throw new UnsupportedOperationException("that shouldn't happend");
        int res = ((ah>>8) - value) & 0xFF;
        Z80.AF.SetL(SZHVC_sub[ah | res]);
        Z80.AF.SetH(res & 0xFF);
    }
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
    
    /***************************************************************
     * AND	n
     ***************************************************************/
    public void AND(int value)
    {
        Z80.AF.SetH(Z80.AF.H & value);
        Z80.AF.SetL((SZP[Z80.AF.H] | HF) & 0xFF);
    }
    /***************************************************************
     * OR	n
     ***************************************************************/
    public void OR(int value)
    {
        Z80.AF.SetH(Z80.AF.H | value);
        Z80.AF.SetL(SZP[Z80.AF.H]); 
    }

    /*TODO*////***************************************************************
    /*TODO*/// * XOR	n
    /*TODO*/// ***************************************************************/
    /*TODO*///#define XOR(value)												\
    /*TODO*///	_A ^= value;												\
    /*TODO*///	_F = SZP[_A]
    public void XOR(int value)
    {
        Z80.AF.SetH(Z80.AF.H ^ value);
        Z80.AF.SetL(SZP[Z80.AF.H]);
    }
    /***************************************************************
     * CP	n
     ***************************************************************/
    public void CP(int value)
    {
        int ah = Z80.AF.D & 0xff00;
        if(ah <= -1)//check unsigness TODO crazy check to be removed
            throw new UnsupportedOperationException("that shouldn't happend");
        int res = ((ah>>8) - value) & 0xFF;
        Z80.AF.SetL(SZHVC_sub[ah | res]);
    }
    /*TODO*////***************************************************************
    /*TODO*/// * EX   AF,AF'
    /*TODO*/// ***************************************************************/
    /*TODO*///#define EX_AF {                                                 \
    /*TODO*///	PAIR tmp;													\
    /*TODO*///    tmp = Z80.AF; Z80.AF = Z80.AF2; Z80.AF2 = tmp;              \
    /*TODO*///}
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
    public int SRL(int value)//TODO check it again sometime (shadow)
    {
        int res = value & 0xFF;
        int c = (res & 0x01) != 0 ? CF : 0;
        res = (res >>> 1) & 0xff;
        Z80.AF.SetL((SZP[res] | c) & 0xFF);
        return (res & 0xFF);
    }
    
    /***************************************************************
     * BIT  bit,r8
    / ***************************************************************/
    public void BIT(int bit,int reg)
    {
        //_F = (_F & CF) | HF | SZ_BIT[reg & (1<<bit)]
        Z80.AF.SetL(((Z80.AF.L & CF) | HF | SZ_BIT[reg & (1 << bit)]) & 0xFF);
    }
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

    /***************************************************************
    /* LDI
    / ***************************************************************/
    public void LDI()
    {
    	int io = cpu_readmem16(Z80.HL.D) & 0xFF; 										
    	cpu_writemem16(Z80.DE.D, io );												
    	Z80.AF.SetL(Z80.AF.L & (SF | ZF | CF)); 										
    	if(( (Z80.AF.H + io) & 0x02 )!=0)
            Z80.AF.SetL(Z80.AF.L | YF); /* bit 1 -> flag 5 */		
        if(( (Z80.AF.H + io) & 0x08 )!=0)
            Z80.AF.SetL(Z80.AF.L | XF); /* bit 3 -> flag 3 */      
        Z80.HL.AddD(1);
        Z80.DE.AddD(1);
        Z80.BC.AddD(-1);                                       
    	if( Z80.BC.D !=0 ) 
            Z80.AF.SetL(Z80.AF.L | VF);						
    }
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
    public void LDIR()
    {
        z80_ICount[0] -= 5; 
        Z80.PC.AddD(-2);
        do
        {
            LDI();
            if(Z80.BC.D !=0)
            {
                if(z80_ICount[0] >0)
                {
                    Z80.R = (Z80.R + 2) & 0xFF; /* increment R twice */
                    z80_ICount[0] -= 21;
                }
                else break;
            }
            else
            {
                    Z80.PC.AddD(2);//PC += 2;
                    z80_ICount[0] += 5;
                    break;
            }
        }while( z80_ICount[0] > 0 );
    }
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

    /*TODO*////**********************************************************
    /*TODO*/// * opcodes with CB prefix
    /*TODO*/// * rotate, shift and bit operations
    /*TODO*/// **********************************************************/
    opcode cb_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode cb_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_19 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_21 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* SRL  B 		  */opcode cb_38 = new opcode() { public void handler(){ Z80.BC.SetH(SRL(Z80.BC.H)); }};
    /* SRL  C 		  */opcode cb_39 = new opcode() { public void handler(){ Z80.BC.SetL(SRL(Z80.BC.L)); }};
    /* SRL  D 		  */opcode cb_3a = new opcode() { public void handler(){ Z80.DE.SetH(SRL(Z80.DE.H)); }};
    /* SRL  E 		  */opcode cb_3b = new opcode() { public void handler(){ Z80.DE.SetL(SRL(Z80.DE.L)); }};
    /* SRL  H 		  */opcode cb_3c = new opcode() { public void handler(){ Z80.HL.SetH(SRL(Z80.HL.H)); }};
    /* SRL  L 		  */opcode cb_3d = new opcode() { public void handler(){ Z80.HL.SetL(SRL(Z80.HL.L)); }};
    opcode cb_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_56 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_70 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_71 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_72 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_73 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_74 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_75 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_77 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_7e = new opcode() { public void handler()/* BIT  7,(HL)	  */
    {
        BIT(7,cpu_readmem16(Z80.HL.D) & 0xFF);
    }};
    opcode cb_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_86 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_be = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode cb_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
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
    opcode xxcb_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode xxcb_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_19 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_21 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_38 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_56 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_70 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_71 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_72 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_73 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_74 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_75 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_77 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_86 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_be = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode xxcb_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xxcb_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
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
    opcode dd_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode dd_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_19 = new opcode() { public void handler()/* ADD  IX,DE 	  */
    { 
//THIS APPEAR A BIT nasty should probably be rechecked sometime
         // ADD16(IX,DE);	
         int res = (Z80.IX.D + Z80.DE.D) & 0xFFFF; 
         Z80.AF.SetL(((Z80.AF.L & (SF | ZF | VF)) | (((Z80.IX.D ^ res ^ Z80.DE.D) >> 8) & HF) | ((res >> 16) & CF)) & 0xFF); 
         Z80.IX.SetD(res & 0xFFFF);
    }};
    opcode dd_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_21 = new opcode() { public void handler()/* LD   IX,w		  */
    {       
         Z80.IX.SetD(ARG16() & 0xFFFF);
    }};
    opcode dd_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_34 = new opcode() { public void handler(){ }};
    opcode dd_35 = new opcode() { public void handler()/* DEC  (IX+o)	  */
    { 
        //EAX; WM( EA, DEC(RM(EA))
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;//EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
        cpu_writemem16(EA, DEC(cpu_readmem16((int)EA) & 0xFF));
    }};
   
    opcode dd_36 = new opcode() { public void handler()/* LD   (IX+o),n	  */
    { 
        //EAX; WM( EA, ARG()
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;//EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
        cpu_writemem16(EA, ARG() & 0xFF);      
    }};
    opcode dd_37 = new opcode() { public void handler()
    { 
        throw new UnsupportedOperationException("unimplemented");
    }};
    
    opcode dd_38 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_56 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_70 = new opcode() { public void handler()/* LD   (IX+o),B	  */
    { 
        //EAX; WM( EA, _B );
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;//EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
        cpu_writemem16(EA, Z80.BC.H);  
    }};
    opcode dd_71 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_72 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_73 = new opcode() { public void handler()/* LD   (IX+o),E	  */
    { 
        //EAX; WM( EA, _E );	
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;//EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
        cpu_writemem16(EA, Z80.DE.L);
    }};
    opcode dd_74 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_75 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_77 = new opcode() { public void handler()/* LD   (IX+o),A	  */
    { 
        
        //EAX; WM( EA, _A );     
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;//EA = (UINT32)(UINT16)(_IX+(INT8)ARG())
        cpu_writemem16(EA, Z80.AF.H);
    }};
    opcode dd_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7e = new opcode() { public void handler()/* LD   A,(IX+o)	  */
    { 
       
        //EAX; _A = RM(EA);	
        EA = (Z80.IX.D + (byte)ARG()) & 0xFFFF;
        Z80.AF.SetH(cpu_readmem16(EA) &0xFF);     
    }};
    opcode dd_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_86 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_be = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode dd_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e1 = new opcode() { public void handler()/* POP  IX		  */
    { 
        //POP(IX);
        Z80.IX.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.IX.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2);  
    }};
    opcode dd_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e5 = new opcode() { public void handler()/* PUSH IX		  */
    { 
        Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); 
        cpu_writemem16(Z80.SP.D, Z80.IX.L);
        cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.IX.H);
    }};
    opcode dd_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
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
    /*TODO*///OP(dd,1a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1c) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1d) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1e) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,1f) { illegal_1();											} /* DB   DD		  */
    /*TODO*///
    /*TODO*///OP(dd,20) { illegal_1();											} /* DB   DD		  */
    
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
    
    /*TODO*///OP(dd,71) { EAX; WM( EA, _C );										} /* LD   (IX+o),C	  */
    /*TODO*///OP(dd,72) { EAX; WM( EA, _D );										} /* LD   (IX+o),D	  */
    
    /*TODO*///OP(dd,74) { EAX; WM( EA, _H );										} /* LD   (IX+o),H	  */
    /*TODO*///OP(dd,75) { EAX; WM( EA, _L );										} /* LD   (IX+o),L	  */
    /*TODO*///OP(dd,76) { illegal_1();											}		  /* DB   DD		  */
    
    /*TODO*///
    /*TODO*///OP(dd,78) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,79) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7a) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7b) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,7c) { _A = _HX;												} /* LD   A,HX		  */
    /*TODO*///OP(dd,7d) { _A = _LX;												} /* LD   A,LX		  */
    
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
    
    /*TODO*///OP(dd,e2) { illegal_1();											} /* DB   DD		  */
    /*TODO*///OP(dd,e3) { EXSP(IX);												} /* EX   (SP),IX	  */
    /*TODO*///OP(dd,e4) { illegal_1();											} /* DB   DD		  */
    
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

    /**********************************************************
     * IY register related opcodes (FD prefix)
     **********************************************************/
    opcode fd_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode fd_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_19 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_21 = new opcode() { public void handler()/* LD   IY,w		  */
    { 
        // _IY = ARG16();
        Z80.IY.SetD(ARG16());
    }};
    opcode fd_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_38 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_56 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_70 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_71 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_72 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_73 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_74 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_75 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_77 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_86 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_be = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode fd_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e1 = new opcode() { public void handler()/* POP  IY		  */
    { 
        //POP(IY); 
        Z80.IY.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.IY.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2);  
    }};
    opcode fd_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e5 = new opcode() { public void handler()/* PUSH IY		  */
    { 
        Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); 
        cpu_writemem16(Z80.SP.D, Z80.IY.L);
        cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.IY.H);
    }};
    opcode fd_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
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
    
    /*TODO*///OP(fd,e2) { illegal_1();											} /* DB   FD		  */
    /*TODO*///OP(fd,e3) { EXSP(IY);												} /* EX   (SP),IY	  */
    /*TODO*///OP(fd,e4) { illegal_1();											} /* DB   FD		  */
    
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

    /**********************************************************
     * special opcodes (ED prefix)
     **********************************************************/
    opcode ed_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode ed_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_19 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_21 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_38 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_47 = new opcode() { public void handler() /* LD   I,A */
    { 
        Z80.I = Z80.AF.H;
    }};
    opcode ed_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* IM   1 		  */opcode ed_56 = new opcode() { public void handler() { Z80.IM = 1; }};
    opcode ed_57 = new opcode() { public void handler()/* LD   A,I		  */
    { 
        Z80.AF.SetH(Z80.I);//	_A = _I;
	Z80.AF.SetL(((Z80.AF.L & CF) | SZ[Z80.AF.H] | (Z80.IFF2 <<2) ) & 0xFF);//_F = (_F & CF) | SZ[_A] | ( _IFF2 << 2 );    
    }};
    opcode ed_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_70 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_71 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_72 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_73 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_74 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_75 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_77 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_86 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b0 = new opcode() { public void handler()/* LDIR			  */
    { 
//TODO check it for sure (shadow)
       LDIR();
    }};
    opcode ed_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_be = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode ed_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
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

    /* NOP			  */opcode op_00 = new opcode() { public void handler(){ }};
    opcode op_01 = new opcode() { public void handler()/* LD   BC,w		  */
    { 	
        Z80.BC.SetD(ARG16());//_BC = ARG16();
    }};
    opcode op_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_04 = new opcode() { public void handler()/* INC  B 		  */
    {
        Z80.BC.SetH(INC(Z80.BC.H));
    }};
    opcode op_05 = new opcode() { public void handler()/* DEC  B 		  */
    { 
        Z80.BC.SetH(DEC(Z80.BC.H));	
    }};
    opcode op_06 = new opcode() { public void handler() /* LD   B,n  */
    { 
        Z80.BC.SetH(ARG());		
    }};
    opcode op_07 = new opcode() { public void handler()/* RLCA			  */
    { 
//TODO recheck it not sure if it's ok (shadow)     
          // A = (_A << 1) | (_A >> 7); 								
         // _F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF))
         Z80.AF.SetH(((Z80.AF.H << 1) | (Z80.AF.H >>> 7)) & 0xFF);
         Z80.AF.SetL(((Z80.AF.L & (SF | ZF | PF)) | (Z80.AF.H & (YF | XF | CF)))& 0xFF);
    }};
    opcode op_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_09 = new opcode() { public void handler()/* ADD  HL,BC 	  */
    { 
//TODO recheck it not sure if it's ok (shadow)           
         // ADD16(HL,BC);	
         int res = (Z80.HL.D + Z80.BC.D) & 0xFFFF; 
         Z80.AF.SetL(((Z80.AF.L & (SF | ZF | VF)) | (((Z80.HL.D ^ res ^ Z80.BC.D) >> 8) & HF) | ((res >> 16) & CF)) & 0xFF); 
         Z80.HL.SetD(res & 0xFFFF); 
    }};
    opcode op_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode op_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_0f = new opcode() { public void handler()/* RRCA			  */
    { 
//TODO recheck it not sure if it's ok (shadow)         
         //_F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF)); 		
         //_A = (_A >> 1) | (_A << 7)
         Z80.AF.SetL(((Z80.AF.L & (SF | ZF | PF)) | (Z80.AF.H & (YF | XF | CF))) & 0xFF);
         Z80.AF.SetH(((Z80.AF.H >>> 1) | (Z80.AF.H << 7)) & 0xFF);
    }};
    opcode op_10 = new opcode() { public void handler()  /* DJNZ o 		  */
    { 
        Z80.BC.AddH(-1);// _B--;
        if(Z80.BC.H !=0)
        {
            byte arg = (byte)ARG();/* ARG() also increments _PC */
            Z80.PC.SetD((Z80.PC.D + arg) & 0xFFFF);/* so don't do _PC += ARG() */
            z80_ICount[0] -= 5; 
            change_pc16(Z80.PC.D);
        }
        else
        {
            Z80.PC.AddD(1);//_PC++;
        }
    }};
    opcode op_11 = new opcode() { public void handler()/* LD   DE,w		  */
    { 
        Z80.DE.SetD(ARG16() & 0xFFFF);
    }};
    opcode op_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_13 = new opcode() { public void handler()/* INC  DE		  */
    { 
       Z80.DE.AddD(1);// _DE++;													} 
    }};
    opcode op_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_15 = new opcode() { public void handler()/* DEC  D 		  */
    { 
        Z80.DE.SetH(DEC(Z80.DE.H));
    }};
    opcode op_16 = new opcode() { public void handler()/* LD   D,n		  */
    { 
        Z80.DE.SetH(ARG() & 0xFF);
    }};
    opcode op_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_18 = new opcode() { public void handler()/* JR   o 		  */
    { 
 //TODO complext check it once more sometime (shadow)
        //JR();
    	int oldpc = Z80.PC.D -1; //_PCD-1;	
        byte arg = (byte)ARG();/* ARG() also increments _PC */
        Z80.PC.SetD((Z80.PC.D + arg) & 0xFFFF);/* so don't do _PC += ARG() */   
	change_pc16(Z80.PC.D);											
        /* speed up busy loop */                                    
	if( Z80.PC.D == oldpc ) 										
	{															
		if( after_EI==0 ) 										
			BURNODD( z80_ICount[0], 1, 12 );						
	}															
	else														
	{															
		int op = cpu_readop(Z80.PC.D) & 0xFF;							
		if( Z80.PC.D == oldpc-1 )									
		{														
			/* NOP - JR $-1 or EI - JR $-1 */					
			if ( op == 0x00 || op == 0xfb ) 					
			{													
				if( after_EI==0 ) 								
					BURNODD( z80_ICount[0]-4, 2, 4+12 );			
			}													
		}														
		else													
		/* LD SP,#xxxx - JR $-3 */								
		if( Z80.PC.D == oldpc-3 && op == 0x31 ) 					
		{														
			if( after_EI==0 ) 									
				BURNODD( z80_ICount[0]-12, 2, 10+12 ); 			
		}														
        }    
    }};
    opcode op_19 = new opcode() { public void handler()/* ADD  HL,DE 	  */
    { 
       /*TODO*///OP(op,19) { ADD16(HL,DE);
//THIS APPEAR A BIT nasty should probably be rechecked sometime
         // ADD16(HL,DE);	
         int res = (Z80.HL.D + Z80.DE.D) & 0xFFFF; 
         Z80.AF.SetL(((Z80.AF.L & (SF | ZF | VF)) | (((Z80.HL.D ^ res ^ Z80.DE.D) >> 8) & HF) | ((res >> 16) & CF)) & 0xFF); 
         Z80.HL.SetD(res & 0xFFFF); 
    }};
    opcode op_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_1e = new opcode() { public void handler()/* LD   E,n		  */
    { 
        //_E = ARG(); 
        Z80.DE.SetL(ARG() & 0xFF);
    }};
    opcode op_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_20 = new opcode() { public void handler(){  /* JR   NZ,o		  */							
    	if((Z80.AF.L & ZF) ==0)
        {
            byte arg = (byte)ARG();/* ARG() also increments _PC */
            Z80.PC.SetD((Z80.PC.D + arg) & 0xFFFF);/* so don't do _PC += ARG() */
            z80_ICount[0] -= 5; 
            change_pc16(Z80.PC.D);
        }
        else
        {
            Z80.PC.AddD(1);//_PC++;
        }
    }};
    opcode op_21 = new opcode() { public void handler() /* LD   HL,w */
    { 
        Z80.HL.SetD(ARG16());
    }};
    opcode op_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_23 = new opcode() { public void handler() /* INC  HL		  */
    { 
       Z80.HL.AddD(1);//_HL++;
    }};
    opcode op_24 = new opcode() { public void handler(){ /* INC  H 		  */										 
        Z80.HL.SetH(INC(Z80.HL.H));
    }};
    opcode op_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_26 = new opcode() { public void handler() /* LD   H,n		  */
    { 
         Z80.HL.SetH(ARG() & 0xFF);
    }};
    opcode op_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_28 = new opcode() { public void handler()/* JR   Z,o		  */
    { 
        if((Z80.AF.L & ZF) !=0)
        {
            byte arg = (byte)ARG();/* ARG() also increments _PC */
            Z80.PC.SetD((Z80.PC.D + arg) & 0xFFFF);/* so don't do _PC += ARG() */
            z80_ICount[0] -= 5; 
            change_pc16(Z80.PC.D);
        }
        else
        {
            Z80.PC.AddD(1);//_PC++;
        }
    }};
    opcode op_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_2c = new opcode() { public void handler() /* INC  L 		  */
    { 
        Z80.HL.SetL(INC(Z80.HL.L));
    }};
    opcode op_2d = new opcode() { public void handler()/* DEC  L 		  */
    { 
        Z80.HL.SetL(DEC(Z80.HL.L));    
    }};
    opcode op_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_2f = new opcode() { public void handler()/* CPL			  */
    { 
//TODO recheck it i am not sure (shadow)      
        // _A ^= 0xff; _F = (_F&(SF|ZF|PF|CF))|HF|NF|(_A&(YF|XF));  
        Z80.AF.SetH(Z80.AF.H ^ 0xff);
        Z80.AF.SetL(((Z80.AF.L & (SF|ZF|PF|CF)) | HF|NF | (Z80.AF.H & (YF|XF))) & 0xFF);
    }};
    opcode op_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_31 = new opcode() { public void handler()/* LD   SP,w		  */
    { 
        Z80.SP.SetD(ARG16());
    }};
    opcode op_32 = new opcode() { public void handler()/* LD   (w),A 	  */
    { 
        EA = ARG16(); 
        cpu_writemem16((int)EA, Z80.AF.H);    
    }};
    opcode op_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_34 = new opcode() { public void handler() /* INC  (HL)		  */
    { 
          //WM( _HL, INC(RM(_HL)) );
          cpu_writemem16(Z80.HL.D, INC(cpu_readmem16(Z80.HL.D) & 0xFF));
    }};
    opcode op_35 = new opcode() { public void handler()/* DEC  (HL)		  */
    { 
         //WM( _HL, DEC(RM(_HL)) );
        cpu_writemem16(Z80.HL.D, DEC(cpu_readmem16(Z80.HL.D) & 0xFF));
    }};
    opcode op_36 = new opcode() { public void handler()/* LD   (HL),n	  */
    { 
         cpu_writemem16(Z80.HL.D, ARG() &0xFF);//WM( _HL, ARG() );
    }};
    opcode op_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_38 = new opcode() { public void handler()/* JR   C,o		  */
    { 
        
        //JR_COND( _F & CF ); 								
        if((Z80.AF.L & CF) !=0)
        {
            byte arg = (byte)ARG();/* ARG() also increments _PC */
            Z80.PC.SetD((Z80.PC.D + arg) & 0xFFFF);/* so don't do _PC += ARG() */
            z80_ICount[0] -= 5; 
            change_pc16(Z80.PC.D);
        }
        else
        {
            Z80.PC.AddD(1);//_PC++;
        }
    }};
    opcode op_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_3a = new opcode() { public void handler()/* LD   A,(w) 	  */
    { 
        //EA = ARG16(); _A = RM( EA );		
        EA = ARG16(); 
        Z80.AF.SetH(cpu_readmem16((int)EA) & 0xFF); 
    }};
    opcode op_3b = new opcode() { public void handler()/* DEC  SP		  */
    { 
        Z80.SP.AddD(-1);// _SP--;
    }};
    opcode op_3c = new opcode() { public void handler()/* INC  A 		  */
    { 
         Z80.AF.SetH(INC(Z80.AF.H));
    }};
    opcode op_3d = new opcode() { public void handler()/* DEC  A 		  */
    { 
         Z80.AF.SetH(DEC(Z80.AF.H));
    }};
    opcode op_3e = new opcode() { public void handler()/* LD   A,n */
    { 
        Z80.AF.SetH(ARG());//_A = ARG(); 											
    }};
    opcode op_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_46 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* LD   B,A		  */opcode op_47 = new opcode() { public void handler(){ Z80.BC.SetH(Z80.AF.H);}};
    /* LD   C,B		  */opcode op_48 = new opcode() { public void handler(){ Z80.BC.SetL(Z80.BC.H);}};
    opcode op_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* LD   C,D		  */opcode op_4a = new opcode() { public void handler(){ Z80.BC.SetL(Z80.DE.H);}};
    /* LD   C,E		  */opcode op_4b = new opcode() { public void handler(){ Z80.BC.SetL(Z80.DE.L);}};
    /* LD   C,H		  */opcode op_4c = new opcode() { public void handler(){ Z80.BC.SetL(Z80.HL.H);}};
    /* LD   C,L		  */opcode op_4d = new opcode() { public void handler(){ Z80.BC.SetL(Z80.HL.L);}};
    opcode op_4e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* LD   C,A		  */opcode op_4f = new opcode() { public void handler(){ Z80.BC.SetL(Z80.AF.H); }};
    opcode op_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_56 = new opcode() { public void handler()/* LD   D,(HL)	  */
    { 
        Z80.DE.SetH(cpu_readmem16(Z80.HL.D) & 0xFF);// _D = RM(_HL);	
    }};
    opcode op_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_5e = new opcode() { public void handler()/* LD   E,(HL)	  */
    { 
         Z80.DE.SetL(cpu_readmem16(Z80.HL.D) & 0xFF);
    }};
    opcode op_5f = new opcode() { public void handler() /* LD   E,A		  */
    { 
        Z80.DE.SetL(Z80.AF.H);
    }};
    opcode op_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_66 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_67 = new opcode() { public void handler()/* LD   H,A		  */
    { 
            Z80.HL.SetH(Z80.AF.H);
    }};
    opcode op_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_6e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* LD   L,A		  */opcode op_6f = new opcode() { public void handler(){ Z80.HL.SetL(Z80.AF.H); }};
    /* LD   (HL),B	  */opcode op_70 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.BC.H); }};
    /* LD   (HL),C	  */opcode op_71 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.BC.L); }};
    /* LD   (HL),D	  */opcode op_72 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.DE.H); }};
    /* LD   (HL),E	  */opcode op_73 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.DE.L); }};
    /* LD   (HL),H	  */opcode op_74 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.HL.H); }};
    /* LD   (HL),L	  */opcode op_75 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.HL.L); }};
    opcode op_76 = new opcode() { public void handler(){ /* HALT			  */
        Z80.PC.AddD(-1);// _PC--;                                                      
        Z80.HALT = 1;                                                  
    	if( after_EI==0) 											
    		burn.handler(z80_ICount[0]); 								
    }};
    
    /* LD   (HL),A	  */opcode op_77 = new opcode() { public void handler(){ cpu_writemem16(Z80.HL.D, Z80.AF.H);}};   											
    /* LD   A,B		  */opcode op_78 = new opcode() { public void handler(){ Z80.AF.SetH(Z80.BC.H);}}; 
    /* LD   A,C		  */opcode op_79 = new opcode() { public void handler(){ Z80.AF.SetH(Z80.BC.L);}};
    /* LD   A,D		  */opcode op_7a = new opcode() { public void handler(){ Z80.AF.SetH(Z80.DE.H);}};
    /* LD   A,E		  */opcode op_7b = new opcode() { public void handler(){ Z80.AF.SetH(Z80.DE.L);}};
    /* LD   A,H		  */opcode op_7c = new opcode() { public void handler(){ Z80.AF.SetH(Z80.HL.H);}};
    /* LD   A,L		  */opcode op_7d = new opcode() { public void handler(){ Z80.AF.SetH(Z80.HL.L);}};
    
    opcode op_7e = new opcode() { public void handler()/* LD   A,(HL)	  */
    { 	
            Z80.AF.SetH(cpu_readmem16(Z80.HL.D) & 0xFF);//_A = RM(_HL);
    }};
    opcode op_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};

    /* ADD  A,B		  */opcode op_80 = new opcode() { public void handler(){  ADD(Z80.BC.H);}};
    /* ADD  A,C		  */opcode op_81 = new opcode() { public void handler(){  ADD(Z80.BC.L);}};
    /* ADD  A,D		  */opcode op_82 = new opcode() { public void handler(){  ADD(Z80.DE.H);}};
    /* ADD  A,E		  */opcode op_83 = new opcode() { public void handler(){  ADD(Z80.DE.L);}};
    /* ADD  A,H		  */opcode op_84 = new opcode() { public void handler(){  ADD(Z80.HL.H);}};
    /* ADD  A,L		  */opcode op_85 = new opcode() { public void handler(){  ADD(Z80.HL.L);}};
    opcode op_86 = new opcode() { public void handler()/* ADD  A,(HL)	  */
    { 
        ADD(cpu_readmem16(Z80.HL.D) & 0xFF);//OP(op,86) { ADD(RM(_HL));
    }};
    opcode op_87 = new opcode() { public void handler()/* ADD  A,A		  */
    { 
        ADD(Z80.AF.H);
    }};
    opcode op_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_8c = new opcode() { public void handler()/* ADC  A,H		  */
    { 
        ADC(Z80.HL.H);
    }};
    opcode op_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_8e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_96 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    /* AND  B 		  */opcode op_a0 = new opcode() { public void handler(){ AND(Z80.BC.H);}};
    /* AND  C 		  */opcode op_a1 = new opcode() { public void handler(){ AND(Z80.BC.L);}};
    /* AND  D 		  */opcode op_a2 = new opcode() { public void handler(){ AND(Z80.DE.H);}};
    /* AND  E 		  */opcode op_a3 = new opcode() { public void handler(){ AND(Z80.DE.L);}};
    /* AND  H 		  */opcode op_a4 = new opcode() { public void handler(){ AND(Z80.HL.H);}};
    /* AND  L 		  */opcode op_a5 = new opcode() { public void handler(){ AND(Z80.HL.L);}};
    opcode op_a6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_a7 = new opcode() { public void handler() /* AND  A 		  */
    { 
        AND(Z80.AF.H);	
    }};
    opcode op_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_ae = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_af = new opcode() { public void handler() /* XOR  A */
    { 
        Z80.AF.SetH(Z80.AF.H ^ Z80.AF.H);
        Z80.AF.SetL(SZP[Z80.AF.H]);
    }};
    /* OR   B 		  */opcode op_b0 = new opcode() { public void handler(){ OR(Z80.BC.H); }};
    /* OR   C 		  */opcode op_b1 = new opcode() { public void handler(){ OR(Z80.BC.L); }};
    /* OR   D 		  */opcode op_b2 = new opcode() { public void handler(){ OR(Z80.DE.H); }};
    /* OR   E 		  */opcode op_b3 = new opcode() { public void handler(){ OR(Z80.DE.L); }};
    /* OR   H 		  */opcode op_b4 = new opcode() { public void handler(){ OR(Z80.HL.H); }};
    /* OR   L 		  */opcode op_b5 = new opcode() { public void handler(){ OR(Z80.HL.L); }};
    opcode op_b6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_b7 = new opcode() { public void handler()/* OR   A 		  */
    { 
        OR(Z80.AF.H);
    }};
    /* CP   B 		  */opcode op_b8 = new opcode() { public void handler(){ CP(Z80.BC.H);}};
    /* CP   C 		  */opcode op_b9 = new opcode() { public void handler(){ CP(Z80.BC.L);}};
    /* CP   D 		  */opcode op_ba = new opcode() { public void handler(){ CP(Z80.DE.H);}};
    /* CP   E 		  */opcode op_bb = new opcode() { public void handler(){ CP(Z80.DE.L);}};
    /* CP   H 		  */opcode op_bc = new opcode() { public void handler(){ CP(Z80.HL.H);}};
    /* CP   L 		  */opcode op_bd = new opcode() { public void handler(){ CP(Z80.HL.L);}};
    opcode op_be = new opcode() { public void handler()/* CP   (HL)		  */
    { 
        // CP(RM(_HL));
        CP(cpu_readmem16(Z80.HL.D) & 0xFF);
        
    }};
    opcode op_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_c1 = new opcode() { public void handler()/* POP  BC		  */
    { 
        // POP(BC);
        Z80.BC.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.BC.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2); 
    }};
    opcode op_c2 = new opcode() { public void handler(){ /* JP   NZ,a		  */
        if((Z80.AF.L & ZF)==0 )													
   	{															
    		Z80.PC.SetD(ARG16());//_PCD = ARG16(); 										
    		change_pc16(Z80.PC.D);										
    	}															
    	else														
    	{															
    		Z80.PC.AddD(2);//_PC += 2;												
        }
        
    }};
    opcode op_c3 = new opcode() { public void handler() /* JP  a */ 
    { 
        Z80.PC.SetD(ARG16());											
    	change_pc16(Z80.PC.D);												
 //above is a speedup for the JR instruction first check if it work correctly and then we will see :D (shadow)      
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
    }};
    opcode op_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_c5 = new opcode() { public void handler()/* PUSH BC		  */
    { 
        Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); 
        cpu_writemem16(Z80.SP.D, Z80.BC.L);
        cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.BC.H);
    }};
    opcode op_c6 = new opcode() { public void handler()/* ADD  A,n		  */
    { 
        ADD(ARG() & 0xFF);
    }};
    opcode op_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_c8 = new opcode() { public void handler()/* RET  Z 		  */
    { 
         // RET( _F & ZF ); 	
        if((Z80.AF.L& ZF)!=0)
        {
            //POP(PC);
            Z80.PC.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
            Z80.PC.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
            Z80.SP.AddD(2);  
            change_pc16(Z80.PC.D);//change_pc16(_PCD);
            z80_ICount[0] -= 6;//CY(6);		
        }
    }};
    opcode op_c9 = new opcode() { public void handler()/* RET			  */
    {  
        if(true)
        {
            //POP(PC);
            Z80.PC.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
            Z80.PC.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
            Z80.SP.AddD(2);  
            change_pc16(Z80.PC.D);//change_pc16(_PCD);
            z80_ICount[0] -= 6;//CY(6);		
        }
    }};
    opcode op_ca = new opcode() { public void handler()/* JP   Z,a		  */
    { 
        //JP_COND( _F & ZF ); 
        if((Z80.AF.L & ZF)!=0 )													
   	{															
    		Z80.PC.SetD(ARG16());//_PCD = ARG16(); 										
    		change_pc16(Z80.PC.D);										
    	}															
    	else														
    	{															
    		Z80.PC.AddD(2);//_PC += 2;												
        }
    }};
    opcode op_cb = new opcode() { public void handler()/* **** CB xx 	  */
    { 
        Z80.R= (Z80.R +1) & 0xFF;//_R++;
        int op = ROP();
        z80_ICount[0] -= cc_cb[op];
        Z80cb[op].handler();//EXEC(cb,ROP());
    }};
    opcode op_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_cd = new opcode() { public void handler()/* CALL a 		  */
    { 
        if(true)
        {
            EA = ARG16() & 0xFFFF;
            Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); //PUSH( PC );
            cpu_writemem16(Z80.SP.D, Z80.PC.L);
            cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.PC.H);
            Z80.PC.SetD(EA); //_PCD = EA;
            z80_ICount[0] -= 7; //CY(7); 
            change_pc16(Z80.PC.D);
        }
        else
        {
            Z80.PC.AddD(2);//_PC+=2; 
        }
    }};
    opcode op_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_d1 = new opcode() { public void handler()/* POP  DE		  */
    { 
        Z80.DE.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.DE.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2);  
    }};
    opcode op_d2 = new opcode() { public void handler()/* JP   NC,a		  */
    { 
        //JP_COND( !(_F & CF) );	
        if((Z80.AF.L & CF)==0 )													
   	{															
    		Z80.PC.SetD(ARG16());//_PCD = ARG16(); 										
    		change_pc16(Z80.PC.D);										
    	}															
    	else														
    	{															
    		Z80.PC.AddD(2);//_PC += 2;												
        }
    }};
    opcode op_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_d5 = new opcode() { public void handler()/* PUSH DE		  */
    { 
        Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); 
        cpu_writemem16(Z80.SP.D, Z80.DE.L);
        cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.DE.H);
    }};
    opcode op_d6 = new opcode() { public void handler()/* SUB  n 		  */
    { 
        SUB(ARG() & 0xFF);
    }};
    opcode op_d7 = new opcode() { public void handler()/* RST  2 		  */
    { 
            //RST(0x10);
            Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); //PUSH( PC );
            cpu_writemem16(Z80.SP.D, Z80.PC.L);
            cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.PC.H);
            Z80.PC.SetD(0x10);
            change_pc16(Z80.PC.D);
    }};
    opcode op_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_d9 = new opcode() { public void handler()/* EXX			  */
    { 
        PAIR tmp = new PAIR();
        tmp = Z80.BC; Z80.BC = Z80.BC2; Z80.BC2 = tmp;
        tmp = null; //null it just in case
        tmp = new PAIR();
        tmp = Z80.DE; Z80.DE = Z80.DE2; Z80.DE2 = tmp;
        tmp = null;
        tmp = new PAIR();
        tmp = Z80.HL; Z80.HL = Z80.HL2; Z80.HL2 = tmp;
        tmp=null;
    }};
    opcode op_da = new opcode() { public void handler()/* JP   C,a		  */
    { 
        if((Z80.AF.L & CF )!=0 ) //JP_COND( _F & CF );											
   	{															
    		Z80.PC.SetD(ARG16());//_PCD = ARG16(); 										
    		change_pc16(Z80.PC.D);										
    	}															
    	else														
    	{															
    		Z80.PC.AddD(2);//_PC += 2;												
        }
    }};
    opcode op_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_dd = new opcode() { public void handler()/* **** DD xx 	  */
    { 

        Z80.R= (Z80.R +1) & 0xFF;//_R++;
        int op = ROP();
        z80_ICount[0] -= cc_dd[op];
        Z80dd[op].handler();//EXEC(dd,ROP());
    
    }}; 
    opcode op_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_df = new opcode() { public void handler()/* RST  3 		  */
    { 
            //RST(0x18);
            Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); //PUSH( PC );
            cpu_writemem16(Z80.SP.D, Z80.PC.L);
            cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.PC.H);
            Z80.PC.SetD(0x18);
            change_pc16(Z80.PC.D);
    }};
    opcode op_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_e1 = new opcode() { public void handler()/* POP  HL		  */
    { 
        Z80.HL.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.HL.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2);  
    }};
    opcode op_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_e5 = new opcode() { public void handler()/* PUSH HL		  */
    { 
        Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); 
        cpu_writemem16(Z80.SP.D, Z80.HL.L);
        cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.HL.H);

    }};
    opcode op_e6 = new opcode() { public void handler() /* AND  n 		  */
    { 
        AND(ARG() & 0xFF);
    }};
    opcode op_e7 = new opcode() { public void handler()/* RST  4 		  */
    { 
        //RST(0x20);												
            Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF); //PUSH( PC );
            cpu_writemem16(Z80.SP.D, Z80.PC.L);
            cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.PC.H);
            Z80.PC.SetD(0x20);
            change_pc16(Z80.PC.D);
    }};
    opcode op_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_e9 = new opcode() { public void handler()/* JP   (HL)		  */
    { 
    // _PC = _HL; change_pc16(_PCD);
        Z80.PC.SetD(Z80.HL.D);
        change_pc16(Z80.PC.D);
    }};
    opcode op_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_eb = new opcode() { public void handler()/* EX   DE,HL 	  */
    { 
       PAIR tmp = new PAIR();													
       tmp = Z80.DE; Z80.DE = Z80.HL; Z80.HL = tmp;             
       tmp=null;
    }};
    opcode op_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_ed = new opcode() { public void handler() /* **** ED xx 	  */
    { 
        Z80.R = (Z80.R +1) & 0xFF;
        int op = ROP(); 
        z80_ICount[0] -= cc_ed[op]; 
        Z80ed[op].handler();
    }};
    opcode op_ee = new opcode() { public void handler() /* XOR  n 		  */
    { 
        XOR(ARG() & 0xFF);
    }};
    opcode op_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f1 = new opcode() { public void handler() /* POP  AF		  */
    { 
	Z80.AF.SetL((cpu_readmem16(Z80.SP.D) & 0xFF)); //RM16
        Z80.AF.SetH((cpu_readmem16((Z80.SP.D + 1) & 0xffff)& 0xFF));
        Z80.SP.AddD(2);     
    }};
    opcode op_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f3 = new opcode() { public void handler()/* DI */
    { 
        Z80.IFF1 = Z80.IFF2 =0;										
    }};
    opcode op_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f5 = new opcode() { public void handler() /* PUSH AF		  */
    { 
       Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF);
       cpu_writemem16(Z80.SP.D, Z80.AF.L);
       cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.AF.H);
    
    }};
    opcode op_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_fa = new opcode() { public void handler()/* JP   M,a		  */
    { 
        if((Z80.AF.L & SF )!=0 ) //JP_COND(_F & SF);										
   	{															
    		Z80.PC.SetD(ARG16());//_PCD = ARG16(); 										
    		change_pc16(Z80.PC.D);										
    	}															
    	else														
    	{															
    		Z80.PC.AddD(2);//_PC += 2;												
        }
    }};
    opcode op_fb = new opcode() { public void handler() /* EI 			  */
    { 
         /* If interrupts were disabled, execute one more			
         * instruction and check the IRQ line.                      
         * If not, simply set interrupt flip-flop 2                 
         */                                                         
    	if(Z80.IFF1 == 0 )											
    	{															
            Z80.IFF1 = Z80.IFF2 = 1; 
            Z80.PREPC.SetD(Z80.PC.D); //_PPC = _PCD;                                                                            
    	    Z80.R= (Z80.R +1) & 0xFF;//_R++;													
    	    if( Z80.irq_state != CLEAR_LINE || Z80.request_irq >= 0 )								
    	    {														
    		after_EI = 1;	/* avoid cycle skip hacks */	
                int op = ROP();
                z80_ICount[0] -= cc_op[op];
                Z80op[op].handler();									
    		after_EI = 0;										
                if(errorlog!=null) fprintf(errorlog, "Z80#%d EI takes irq\n", cpu_getactivecpu()); 
                take_interrupt();                                  
            }                                                      
    	    else
            {
                int op = ROP();
                z80_ICount[0] -= cc_op[op];
                Z80op[op].handler();
            }
        } 
        else
        {
            Z80.IFF2 = 1;
        }                                           
    }};
    opcode op_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode op_fd = new opcode() { public void handler()/* **** FD xx 	  */
    { 
        Z80.R= (Z80.R +1) & 0xFF;//_R++;
        int op = ROP();
        z80_ICount[0] -= cc_dd[op];  //cc_fd and cc_dd are the same
        Z80fd[op].handler();//EXEC(fd,ROP());} 
    }};
    opcode op_fe = new opcode() { public void handler()/* CP   n 		  */
    { 
        CP(ARG() & 0xFF); //CP(ARG());
    }};
    opcode op_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
   
    
    /*TODO*///OP(op,02) { WM( _BC, _A );											} /* LD   (BC),A	  */
    /*TODO*///OP(op,03) { _BC++;													} /* INC  BC		  */

    /*TODO*///
    /*TODO*///OP(op,08) { EX_AF;													} /* EX   AF,AF'      */
    
    /*TODO*///OP(op,0a) { _A = RM(_BC);											} /* LD   A,(BC)	  */
    /*TODO*///OP(op,0b) { _BC--; CHECK_BC_LOOP;									} /* DEC  BC		  */
    /*TODO*///OP(op,0c) { _C = INC(_C);											} /* INC  C 		  */
    /*TODO*///OP(op,0d) { _C = DEC(_C);											} /* DEC  C 		  */
    /*TODO*///OP(op,0e) { _C = ARG(); 											} /* LD   C,n		  */
   
    /*TODO*///
    
    
    /*TODO*///OP(op,12) { WM( _DE, _A );											} /* LD   (DE),A	  */
    
    /*TODO*///OP(op,14) { _D = INC(_D);											} /* INC  D 		  */
    
   
    /*TODO*///OP(op,17) { RLA;													} /* RLA			  */
    /*TODO*///
    
    
    /*TODO*///OP(op,1a) { _A = RM(_DE);											} /* LD   A,(DE)	  */
    /*TODO*///OP(op,1b) { _DE--; CHECK_DE_LOOP;									} /* DEC  DE		  */
    /*TODO*///OP(op,1c) { _E = INC(_E);											} /* INC  E 		  */
    /*TODO*///OP(op,1d) { _E = DEC(_E);											} /* DEC  E 		  */
    
    /*TODO*///OP(op,1f) { RRA;													} /* RRA			  */
    /*TODO*///

    /*TODO*///OP(op,22) { EA = ARG16(); WM16( EA, &Z80.HL );						} /* LD   (w),HL	  */
    
   
    /*TODO*///OP(op,25) { _H = DEC(_H);											} /* DEC  H 		  */
   
    /*TODO*///OP(op,27) { DAA;													} /* DAA			  */
    /*TODO*///
    
    /*TODO*///OP(op,29) { ADD16(HL,HL);											} /* ADD  HL,HL 	  */
    /*TODO*///OP(op,2a) { EA = ARG16(); RM16( EA, &Z80.HL );						} /* LD   HL,(w)	  */
    /*TODO*///OP(op,2b) { _HL--; CHECK_HL_LOOP;									} /* DEC  HL		  */
    
    
    /*TODO*///OP(op,2e) { _L = ARG(); 											} /* LD   L,n		  */
    
    /*TODO*///
    /*TODO*///OP(op,30) { JR_COND( !(_F & CF) );									} /* JR   NC,o		  */
    
    
    /*TODO*///OP(op,33) { _SP++;													} /* INC  SP		  */
  
   
    
    /*TODO*///OP(op,37) { _F = (_F & (SF|ZF|PF)) | CF | (_A & (YF|XF));			} /* SCF			  */
    /*TODO*///
    
    /*TODO*///OP(op,39) { ADD16(HL,SP);											} /* ADD  HL,SP 	  */
    
    
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
    
    /*TODO*///OP(op,49) { 														} /* LD   C,C		  */
    
    /*TODO*///OP(op,4e) { _C = RM(_HL);											} /* LD   C,(HL)	  */
    
    /*TODO*///
    /*TODO*///OP(op,50) { _D = _B;												} /* LD   D,B		  */
    /*TODO*///OP(op,51) { _D = _C;												} /* LD   D,C		  */
    /*TODO*///OP(op,52) { 														} /* LD   D,D		  */
    /*TODO*///OP(op,53) { _D = _E;												} /* LD   D,E		  */
    /*TODO*///OP(op,54) { _D = _H;												} /* LD   D,H		  */
    /*TODO*///OP(op,55) { _D = _L;												} /* LD   D,L		  */
    
    /*TODO*///OP(op,57) { _D = _A;												} /* LD   D,A		  */
    /*TODO*///
    /*TODO*///OP(op,58) { _E = _B;												} /* LD   E,B		  */
    /*TODO*///OP(op,59) { _E = _C;												} /* LD   E,C		  */
    /*TODO*///OP(op,5a) { _E = _D;												} /* LD   E,D		  */
    /*TODO*///OP(op,5b) { 														} /* LD   E,E		  */
    /*TODO*///OP(op,5c) { _E = _H;												} /* LD   E,H		  */
    /*TODO*///OP(op,5d) { _E = _L;												} /* LD   E,L		  */
   
    
    /*TODO*///
    /*TODO*///OP(op,60) { _H = _B;												} /* LD   H,B		  */
    /*TODO*///OP(op,61) { _H = _C;												} /* LD   H,C		  */
    /*TODO*///OP(op,62) { _H = _D;												} /* LD   H,D		  */
    /*TODO*///OP(op,63) { _H = _E;												} /* LD   H,E		  */
    /*TODO*///OP(op,64) { 														} /* LD   H,H		  */
    /*TODO*///OP(op,65) { _H = _L;												} /* LD   H,L		  */
    /*TODO*///OP(op,66) { _H = RM(_HL);											} /* LD   H,(HL)	  */
    
    /*TODO*///
    /*TODO*///OP(op,68) { _L = _B;												} /* LD   L,B		  */
    /*TODO*///OP(op,69) { _L = _C;												} /* LD   L,C		  */
    /*TODO*///OP(op,6a) { _L = _D;												} /* LD   L,D		  */
    /*TODO*///OP(op,6b) { _L = _E;												} /* LD   L,E		  */
    /*TODO*///OP(op,6c) { _L = _H;												} /* LD   L,H		  */
    /*TODO*///OP(op,6d) { 														} /* LD   L,L		  */
    /*TODO*///OP(op,6e) { _L = RM(_HL);											} /* LD   L,(HL)	  */
    
    /*TODO*///
    /*TODO*///OP(op,7f) { 														} /* LD   A,A		  */

    /*TODO*///OP(op,88) { ADC(_B);												} /* ADC  A,B		  */
    /*TODO*///OP(op,89) { ADC(_C);												} /* ADC  A,C		  */
    /*TODO*///OP(op,8a) { ADC(_D);												} /* ADC  A,D		  */
    /*TODO*///OP(op,8b) { ADC(_E);												} /* ADC  A,E		  */
    
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
    
    /*TODO*///OP(op,a6) { AND(RM(_HL));											} /* AND  (HL)		  */
   
    /*TODO*///
    /*TODO*///OP(op,a8) { XOR(_B);												} /* XOR  B 		  */
    /*TODO*///OP(op,a9) { XOR(_C);												} /* XOR  C 		  */
    /*TODO*///OP(op,aa) { XOR(_D);												} /* XOR  D 		  */
    /*TODO*///OP(op,ab) { XOR(_E);												} /* XOR  E 		  */
    /*TODO*///OP(op,ac) { XOR(_H);												} /* XOR  H 		  */
    /*TODO*///OP(op,ad) { XOR(_L);												} /* XOR  L 		  */
    /*TODO*///OP(op,ae) { XOR(RM(_HL));											} /* XOR  (HL)		  */
    
    /*TODO*///
 
    /*TODO*///OP(op,b6) { OR(RM(_HL));											} /* OR   (HL)		  */
    
    
    /*TODO*///OP(op,bf) { CP(_A); 												} /* CP   A 		  */
    /*TODO*///
    /*TODO*///OP(op,c0) { RET( !(_F & ZF) );										} /* RET  NZ		  */
    
   

    /*TODO*///OP(op,c4) { CALL( !(_F & ZF) ); 									} /* CALL NZ,a		  */

    
    /*TODO*///OP(op,c7) { RST(0x00);												} /* RST  0 		  */
    /*TODO*///

    /*TODO*///OP(op,cc) { CALL( _F & ZF );										} /* CALL Z,a		  */
    
    /*TODO*///OP(op,ce) { ADC(ARG()); 											} /* ADC  A,n		  */
    /*TODO*///OP(op,cf) { RST(0x08);												} /* RST  1 		  */
    /*TODO*///
    /*TODO*///OP(op,d0) { RET( !(_F & CF) );										} /* RET  NC		  */
    
    
    /*TODO*///OP(op,d3) { unsigned n = ARG() | (_A << 8); OUT( n, _A );			} /* OUT  (n),A 	  */
    /*TODO*///OP(op,d4) { CALL( !(_F & CF) ); 									} /* CALL NC,a		  */
   
    
    
    /*TODO*///
    /*TODO*///OP(op,d8) { RET( _F & CF ); 										} /* RET  C 		  */
    

    /*TODO*///OP(op,db) { unsigned n = ARG() | (_A << 8); _A = IN( n );			} /* IN   A,(n) 	  */
    /*TODO*///OP(op,dc) { CALL( _F & CF );										} /* CALL C,a		  */
    
    /*TODO*///OP(op,de) { SBC(ARG()); 											} /* SBC  A,n		  */
    
    /*TODO*///
    /*TODO*///OP(op,e0) { RET( !(_F & PF) );										} /* RET  PO		  */
    
    /*TODO*///OP(op,e2) { JP_COND( !(_F & PF) );									} /* JP   PO,a		  */
    /*TODO*///OP(op,e3) { EXSP(HL);												} /* EX   HL,(SP)	  */
    /*TODO*///OP(op,e4) { CALL( !(_F & PF) ); 									} /* CALL PO,a		  */
    
    
   
    /*TODO*///
    /*TODO*///OP(op,e8) { RET( _F & PF ); 										} /* RET  PE		  */
    
    /*TODO*///OP(op,ea) { JP_COND( _F & PF ); 									} /* JP   PE,a		  */
    
    /*TODO*///OP(op,ec) { CALL( _F & PF );										} /* CALL PE,a		  */
    
    
    /*TODO*///OP(op,ef) { RST(0x28);												} /* RST  5 		  */
    /*TODO*///
    /*TODO*///OP(op,f0) { RET( !(_F & SF) );										} /* RET  P 		  */
    
    /*TODO*///OP(op,f2) { JP_COND( !(_F & SF) );									} /* JP   P,a		  */
    
    /*TODO*///OP(op,f4) { CALL( !(_F & SF) ); 									} /* CALL P,a		  */
    
    /*TODO*///OP(op,f6) { OR(ARG());												} /* OR   n 		  */
    /*TODO*///OP(op,f7) { RST(0x30);												} /* RST  6 		  */
    /*TODO*///
    /*TODO*///OP(op,f8) { RET(_F & SF);											} /* RET  M 		  */
    /*TODO*///OP(op,f9) { _SP = _HL;												} /* LD   SP,HL 	  */
    
    
    /*TODO*///OP(op,fc) { CALL(_F & SF);											} /* CALL M,a		  */
    
    
    /*TODO*///OP(op,ff) { RST(0x38);												} /* RST  7 		  */
    /*TODO*///
    /*TODO*///
    static void take_interrupt()
    {
        if( Z80.IFF1!=0)
        {
            int irq_vector;
    
           /* there isn't a valid previous program counter */
           Z80.PREPC.SetD(-1);//_PPC = -1;
    
            /* Check if processor was halted */
            if(Z80.HALT!=0) 												
            {															
    		Z80.HALT = 0;												
    		Z80.PC.AddD(1);//_PC++;
            }
            if( Z80.irq_max!=0)           /* daisy chain mode */
            {
                throw new UnsupportedOperationException("Daisy chain unimplemented");         
    /*TODO*///            if( Z80.request_irq >= 0 )
    /*TODO*///            {
    /*TODO*///                /* Clear both interrupt flip flops */
    /*TODO*///                _IFF1 = _IFF2 = 0;
    /*TODO*///                irq_vector = Z80.irq[Z80.request_irq].interrupt_entry(Z80.irq[Z80.request_irq].irq_param);
    /*TODO*///                LOG((errorlog, "Z80#%d daisy chain irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
    /*TODO*///                Z80.request_irq = -1;
    /*TODO*///            } else return;
            }
            else
            {
                
               /* Clear both interrupt flip flops */
                Z80.IFF1 = Z80.IFF2 = 0;
                /* call back the cpu interface to retrieve the vector */
                irq_vector = Z80.irq_callback.handler(0);
                if(errorlog!=null) fprintf(errorlog, "Z80#%d single int. irq_vector $%02x\n", cpu_getactivecpu(), System.identityHashCode(irq_vector));
            }
    
            /* Interrupt mode 2. Call [Z80.I:databyte] */
            if( Z80.IM == 2 )
            {
                throw new UnsupportedOperationException("unimplemented");  
    /*TODO*///			irq_vector = (irq_vector & 0xff) | (_I << 8);
    /*TODO*///            PUSH( PC );
    /*TODO*///			RM16( irq_vector, &Z80.PC );
    /*TODO*///            LOG((errorlog, "Z80#%d IM2 [$%04x] = $%04x\n",cpu_getactivecpu() , irq_vector, _PCD));
    /*TODO*///            Z80.extra_cycles += 19;
            }
            else
            {
                /* Interrupt mode 1. RST 38h */
                if( Z80.IM == 1 )
                {
                   if(errorlog!=null) fprintf(errorlog, "Z80#%d IM1 $0038\n",cpu_getactivecpu());
                   Z80.SP.SetD((Z80.SP.D - 2) & 0xFFFF);
                   cpu_writemem16(Z80.SP.D, Z80.PC.L);
                   cpu_writemem16((int)(Z80.SP.D + 1) & 0xffff, Z80.PC.H);
                   Z80.PC.SetD(0x0038);//_PCD = 0x0038;
                   Z80.extra_cycles += 11+2; /* RST $38 + 2 cycles */
                }
                else
                {
                    throw new UnsupportedOperationException("unimplemented");  
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
               }
             }
          
            change_pc(Z80.PC.D);
        }       
    }

    /****************************************************************************
     * Reset registers to their initial values
     ****************************************************************************/
    @Override
    public void reset(Object param) {
        Z80_DaisyChain[] daisy_chain = (Z80_DaisyChain[])param;
            
    	int i, p;
        
        int oldval, newval, val;
        int padd, padc, psub, psbc;
        padd = 0 * 256;
        padc = 256 * 256;
        psub = 0 * 256;
        psbc = 256 * 256;
        for (oldval = 0; oldval < 256; oldval++) 
        {
            for (newval = 0; newval < 256; newval++) 
            {
                /* add or adc w/o carry set */
                val = newval - oldval;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_add[padd] = SF;
                    } else {
                        SZHVC_add[padd] = 0;
                    }
                } else {
                    SZHVC_add[padd] = ZF;
                }
                SZHVC_add[padd] |= (newval & (YF | XF)); /* undocumented flag bits 5+3 */
                
                 if ((newval & 0x0f) < (oldval & 0x0f)) {
                    SZHVC_add[padd] |= HF;
                }
                if (newval < oldval) {
                    SZHVC_add[padd] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_add[padd] |= VF;
                }
                padd++;
                
                /* adc with carry set */
                val = newval - oldval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_add[padc] = SF;
                    } else {
                        SZHVC_add[padc] = 0;
                    }
                } else {
                    SZHVC_add[padc] = ZF;
                }
                
                SZHVC_add[padc] |= (newval & (YF | XF)); /* undocumented flag bits 5+3 */
                if ((newval & 0x0f) <= (oldval & 0x0f)) {
                    SZHVC_add[padc] |= HF;
                }
                if (newval <= oldval) {
                    SZHVC_add[padc] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_add[padc] |= VF;
                }
                padc++;
                
                /* cp, sub or sbc w/o carry set */
                val = oldval - newval;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psub] = NF | SF;
                    } else {
                        SZHVC_sub[psub] = NF;
                    }
                } else {
                    SZHVC_sub[psub] = NF | ZF;
                }

                SZHVC_sub[psub] |= (newval & (YF | XF)); /* undocumented flag bits 5+3 */
                if ((newval & 0x0f) > (oldval & 0x0f)) {
                    SZHVC_sub[psub] |= HF;
                }
                if (newval > oldval) {
                    SZHVC_sub[psub] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psub] |= VF;
                }
                psub++;
 
                /* sbc with carry set */
                val = oldval - newval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psbc] = NF | SF;
                    } else {
                        SZHVC_sub[psbc] = NF;
                    }
                } else {
                    SZHVC_sub[psbc] = NF | ZF;
                }

                SZHVC_sub[psbc] |= (newval & (YF | XF)); /* undocumented flag bits 5+3 */
                if ((newval & 0x0f) >= (oldval & 0x0f)) {
                    SZHVC_sub[psbc] |= HF;
                }
                if (newval >= oldval) {
                    SZHVC_sub[psbc] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psbc] |= VF;
                }
                psbc++;
            }
        }
    	for (i = 0; i < 256; i++)
    	{
            p = 0;
            if ((i & 0x01) != 0)  ++p;
            if ((i & 0x02) != 0)  ++p;
            if ((i & 0x04) != 0)  ++p;
            if ((i & 0x08) != 0)  ++p;
            if ((i & 0x10) != 0)  ++p;
            if ((i & 0x20) != 0)  ++p;
            if ((i & 0x40) != 0)  ++p;
            if ((i & 0x80) != 0)  ++p;
            
            SZ[i] = (i != 0) ? i & SF : ZF;
            SZ[i] |= (i & (YF | XF)); /* undocumented flag bits 5+3 */
            SZ_BIT[i] = (i != 0) ? i & SF : ZF | PF;
            SZ_BIT[i] |= (i & (YF | XF)); /* undocumented flag bits 5+3 */
            SZP[i] = SZ[i] | (((p & 1) != 0) ? 0 : PF);
            SZHV_inc[i] = SZ[i];
            if( i == 0x80 ) SZHV_inc[i] |= VF;
            if( (i & 0x0f) == 0x00 ) SZHV_inc[i] |= HF;
            SZHV_dec[i] = SZ[i] | NF;
            if( i == 0x7f ) SZHV_dec[i] |= VF;
            if( (i & 0x0f) == 0x0f ) SZHV_dec[i] |= HF;
        }
    	//memset(&Z80, 0, sizeof(Z80));
        Z80.PREPC.SetD(0);
        Z80.PC.SetD(0);    
        Z80.SP.SetD(0);    
        Z80.AF.SetD(0);    
        Z80.BC.SetD(0);    
        Z80.DE.SetD(0);    
        Z80.HL.SetD(0);    
        Z80.IX.SetD(0);    
        Z80.IY.SetD(0);    
        Z80.AF2.SetD(0);   
        Z80.BC2.SetD(0);   
        Z80.DE2.SetD(0);    
        Z80.HL2.SetD(0); 
        Z80.R=Z80.R2=Z80.IFF1=Z80.IFF2=Z80.HALT=Z80.IM=Z80.I=0;
        Z80.irq_max=0;         
        Z80.request_irq = -1;
        Z80.service_irq = -1;	
        Z80.nmi_state = CLEAR_LINE;	
        Z80.irq_state = CLEAR_LINE;	
        Z80.int_state = new int[Z80_MAXDAISY];
        Z80.irq = new Z80_DaisyChain[Z80_MAXDAISY];
        Z80.irq_callback=null;
        Z80.extra_cycles=0;
        Z80.IX.SetD(0xFFFF);//_IX = _IY = 0xffff;
        Z80.IY.SetD(0xFFFF); /* IX and IY are FFFF after a reset! */
        Z80.AF.AddL(ZF);//	_F = ZF;	/* Zero flag is set */

        if( daisy_chain!=null )
    	{
            throw new UnsupportedOperationException("Daisy Chain Not supported yet.");
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
        }
        
      change_pc(Z80.PC.D);           
    }
    
    /*TODO*///void z80_exit(void)
    /*TODO*///{
    /*TODO*///#if BIG_FLAGS_ARRAY
    /*TODO*///	if (SZHVC_add) free(SZHVC_add);
    /*TODO*///	SZHVC_add = NULL;
    /*TODO*///	if (SZHVC_sub) free(SZHVC_sub);
    /*TODO*///	SZHVC_sub = NULL;
    /*TODO*///#endif
    /*TODO*///}

    /****************************************************************************
     * Execute 'cycles' T-states. Return number of T-states really executed
    / ****************************************************************************/
    @Override
    public int execute(int cycles) {
  
    	z80_ICount[0] = cycles - Z80.extra_cycles;
   	Z80.extra_cycles = 0;
    
       do
    	{
           Z80.PREPC.SetD(Z80.PC.D); //_PPC = _PCD;
           Z80.R= (Z80.R +1) & 0xFF;//_R++;
           int op = ROP();
           z80_ICount[0] -= cc_op[op];
           Z80op[op].handler();//EXEC_INLINE(op,ROP());       
    	} while( z80_ICount[0] > 0 );

    	z80_ICount[0] -= Z80.extra_cycles;
        Z80.extra_cycles = 0;
    
       return cycles - z80_ICount[0];
    }  
    /****************************************************************************
     * Burn 'cycles' T-states. Adjust R register for the lost time
     ****************************************************************************/
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
           	if( cycles > 0 )
        	{
        		/* NOP takes 4 cycles per instruction */
        		int n = (cycles + 3) / 4;
        		Z80.R = (Z80.R + n) & 0xFF;//_R += n;
        		z80_ICount[0] -= 4 * n;
        	}
    }};

    /*TODO*////****************************************************************************
    /*TODO*/// * Get all registers in given buffer
    /*TODO*/// ****************************************************************************/
    @Override
    public Object init_context() {
        Object reg = new Z80_Regs();
        return reg;
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
 
    /****************************************************************************
     * Return program counter
     ****************************************************************************/
    @Override
    public int get_pc() {
       return Z80.PC.D;
    }
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
    @Override
    public void set_irq_line(int irqline, int state) {
        
        if(errorlog!=null) fprintf(errorlog, "Z80#%d set_irq_line %d\n",cpu_getactivecpu() , state);
        Z80.irq_state = state;
        if(state==CLEAR_LINE) return;
        
        
    	if( Z80.irq_max!=0 )
    	{
            throw new UnsupportedOperationException("Daisychain not supported yet."); 
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
    	}
    	take_interrupt();
    }
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set IRQ vector callback
    /*TODO*/// ****************************************************************************/
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        //if(errorlog!=null) fprintf(errorlog, "Z80#%d set_irq_callback $%08x\n",cpu_getactivecpu() , (int)callback));
        if(errorlog!=null) fprintf(errorlog, "Z80#%d set_irq_callback $%08x\n",cpu_getactivecpu() , System.identityHashCode(callback));
        Z80.irq_callback = callback;
    }
    /*TODO*///void z80_set_irq_callback(int (*callback)(int))
    /*TODO*///{
    
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





    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int linestate) {
         if (Z80.nmi_state == linestate) return;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int memory_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void set_op_base(int pc) 
    {
        cpu_setOPbase16.handler(pc,0);
    }

    public abstract interface opcode
    {
        public abstract void handler();
    }
}
