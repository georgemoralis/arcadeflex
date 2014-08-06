/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.m6502;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m6502.m6502H.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;


public class m6502 extends cpu_interface {
    public static FILE m6502log=fopen("m6502.log", "wa");  //for debug purposes
    int[] m6502_ICount = new int[1];

    public m6502() {
        cpu_num = CPU_M6502;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6502_INT_NONE;
        irq_int = M6502_INT_IRQ;
        nmi_int = M6502_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;

        //intialize interfaces
        burn = burn_function;

        m6502_ICount[0] = 0;
        setupTables();
    }
 
    opcode[] insn6502 = new opcode[256];

    public void setupTables()
    {
        insn6502[0x00] = m6502_00; insn6502[0x01] = m6502_01; insn6502[0x02] = m6502_02; insn6502[0x03] = m6502_03;
        insn6502[0x04] = m6502_04; insn6502[0x05] = m6502_05; insn6502[0x06] = m6502_06; insn6502[0x07] = m6502_07;
        insn6502[0x08] = m6502_08; insn6502[0x09] = m6502_09; insn6502[0x0a] = m6502_0a; insn6502[0x0b] = m6502_0b;
        insn6502[0x0c] = m6502_0c; insn6502[0x0d] = m6502_0d; insn6502[0x0e] = m6502_0e; insn6502[0x0f] = m6502_0f;

        insn6502[0x10] = m6502_10; insn6502[0x11] = m6502_11; insn6502[0x12] = m6502_12; insn6502[0x13] = m6502_13;
        insn6502[0x14] = m6502_14; insn6502[0x15] = m6502_15; insn6502[0x16] = m6502_16; insn6502[0x17] = m6502_17;
        insn6502[0x18] = m6502_18; insn6502[0x19] = m6502_19; insn6502[0x1a] = m6502_1a; insn6502[0x1b] = m6502_1b;
        insn6502[0x1c] = m6502_1c; insn6502[0x1d] = m6502_1d; insn6502[0x1e] = m6502_1e; insn6502[0x1f] = m6502_1f;

        insn6502[0x20] = m6502_20; insn6502[0x21] = m6502_21; insn6502[0x22] = m6502_22; insn6502[0x23] = m6502_23;
        insn6502[0x24] = m6502_24; insn6502[0x25] = m6502_25; insn6502[0x26] = m6502_26; insn6502[0x27] = m6502_27;
        insn6502[0x28] = m6502_28; insn6502[0x29] = m6502_29; insn6502[0x2a] = m6502_2a; insn6502[0x2b] = m6502_2b;
        insn6502[0x2c] = m6502_2c; insn6502[0x2d] = m6502_2d; insn6502[0x2e] = m6502_2e; insn6502[0x2f] = m6502_2f;

        insn6502[0x30] = m6502_30; insn6502[0x31] = m6502_31; insn6502[0x32] = m6502_32; insn6502[0x33] = m6502_33;
        insn6502[0x34] = m6502_34; insn6502[0x35] = m6502_35; insn6502[0x36] = m6502_36; insn6502[0x37] = m6502_37;
        insn6502[0x38] = m6502_38; insn6502[0x39] = m6502_39; insn6502[0x3a] = m6502_3a; insn6502[0x3b] = m6502_3b;
        insn6502[0x3c] = m6502_3c; insn6502[0x3d] = m6502_3d; insn6502[0x3e] = m6502_3e; insn6502[0x3f] = m6502_3f;

        insn6502[0x40] = m6502_40; insn6502[0x41] = m6502_41; insn6502[0x42] = m6502_42; insn6502[0x43] = m6502_43;
        insn6502[0x44] = m6502_44; insn6502[0x45] = m6502_45; insn6502[0x46] = m6502_46; insn6502[0x47] = m6502_47;
        insn6502[0x48] = m6502_48; insn6502[0x49] = m6502_49; insn6502[0x4a] = m6502_4a; insn6502[0x4b] = m6502_4b;
        insn6502[0x4c] = m6502_4c; insn6502[0x4d] = m6502_4d; insn6502[0x4e] = m6502_4e; insn6502[0x4f] = m6502_4f;

        insn6502[0x50] = m6502_50; insn6502[0x51] = m6502_51; insn6502[0x52] = m6502_52; insn6502[0x53] = m6502_53;
        insn6502[0x54] = m6502_54; insn6502[0x55] = m6502_55; insn6502[0x56] = m6502_56; insn6502[0x57] = m6502_57;
        insn6502[0x58] = m6502_58; insn6502[0x59] = m6502_59; insn6502[0x5a] = m6502_5a; insn6502[0x5b] = m6502_5b;
        insn6502[0x5c] = m6502_5c; insn6502[0x5d] = m6502_5d; insn6502[0x5e] = m6502_5e; insn6502[0x5f] = m6502_5f;

        insn6502[0x60] = m6502_60; insn6502[0x61] = m6502_61; insn6502[0x62] = m6502_62; insn6502[0x63] = m6502_63;
        insn6502[0x64] = m6502_64; insn6502[0x65] = m6502_65; insn6502[0x66] = m6502_66; insn6502[0x67] = m6502_67;
        insn6502[0x68] = m6502_68; insn6502[0x69] = m6502_69; insn6502[0x6a] = m6502_6a; insn6502[0x6b] = m6502_6b;
        insn6502[0x6c] = m6502_6c; insn6502[0x6d] = m6502_6d; insn6502[0x6e] = m6502_6e; insn6502[0x6f] = m6502_6f;

        insn6502[0x70] = m6502_70; insn6502[0x71] = m6502_71; insn6502[0x72] = m6502_72; insn6502[0x73] = m6502_73;
        insn6502[0x74] = m6502_74; insn6502[0x75] = m6502_75; insn6502[0x76] = m6502_76; insn6502[0x77] = m6502_77;
        insn6502[0x78] = m6502_78; insn6502[0x79] = m6502_79; insn6502[0x7a] = m6502_7a; insn6502[0x7b] = m6502_7b;
        insn6502[0x7c] = m6502_7c; insn6502[0x7d] = m6502_7d; insn6502[0x7e] = m6502_7e; insn6502[0x7f] = m6502_7f;

        insn6502[0x80] = m6502_80; insn6502[0x81] = m6502_81; insn6502[0x82] = m6502_82; insn6502[0x83] = m6502_83;
        insn6502[0x84] = m6502_84; insn6502[0x85] = m6502_85; insn6502[0x86] = m6502_86; insn6502[0x87] = m6502_87;
        insn6502[0x88] = m6502_88; insn6502[0x89] = m6502_89; insn6502[0x8a] = m6502_8a; insn6502[0x8b] = m6502_8b;
        insn6502[0x8c] = m6502_8c; insn6502[0x8d] = m6502_8d; insn6502[0x8e] = m6502_8e; insn6502[0x8f] = m6502_8f;

        insn6502[0x90] = m6502_90; insn6502[0x91] = m6502_91; insn6502[0x92] = m6502_92; insn6502[0x93] = m6502_93;
        insn6502[0x94] = m6502_94; insn6502[0x95] = m6502_95; insn6502[0x96] = m6502_96; insn6502[0x97] = m6502_97;
        insn6502[0x98] = m6502_98; insn6502[0x99] = m6502_99; insn6502[0x9a] = m6502_9a; insn6502[0x9b] = m6502_9b;
        insn6502[0x9c] = m6502_9c; insn6502[0x9d] = m6502_9d; insn6502[0x9e] = m6502_9e; insn6502[0x9f] = m6502_9f;

        insn6502[0xa0] = m6502_a0; insn6502[0xa1] = m6502_a1; insn6502[0xa2] = m6502_a2; insn6502[0xa3] = m6502_a3;
        insn6502[0xa4] = m6502_a4; insn6502[0xa5] = m6502_a5; insn6502[0xa6] = m6502_a6; insn6502[0xa7] = m6502_a7;
        insn6502[0xa8] = m6502_a8; insn6502[0xa9] = m6502_a9; insn6502[0xaa] = m6502_aa; insn6502[0xab] = m6502_ab;
        insn6502[0xac] = m6502_ac; insn6502[0xad] = m6502_ad; insn6502[0xae] = m6502_ae; insn6502[0xaf] = m6502_af;

        insn6502[0xb0] = m6502_b0; insn6502[0xb1] = m6502_b1; insn6502[0xb2] = m6502_b2; insn6502[0xb3] = m6502_b3;
        insn6502[0xb4] = m6502_b4; insn6502[0xb5] = m6502_b5; insn6502[0xb6] = m6502_b6; insn6502[0xb7] = m6502_b7;
        insn6502[0xb8] = m6502_b8; insn6502[0xb9] = m6502_b9; insn6502[0xba] = m6502_ba; insn6502[0xbb] = m6502_bb; 
        insn6502[0xbc] = m6502_bc; insn6502[0xbd] = m6502_bd; insn6502[0xbe] = m6502_be; insn6502[0xbf] = m6502_bf;

        insn6502[0xc0] = m6502_c0; insn6502[0xc1] = m6502_c1; insn6502[0xc2] = m6502_c2; insn6502[0xc3] = m6502_c3;
        insn6502[0xc4] = m6502_c4; insn6502[0xc5] = m6502_c5; insn6502[0xc6] = m6502_c6; insn6502[0xc7] = m6502_c7;
        insn6502[0xc8] = m6502_c8; insn6502[0xc9] = m6502_c9; insn6502[0xca] = m6502_ca; insn6502[0xcb] = m6502_cb; 
        insn6502[0xcc] = m6502_cc; insn6502[0xcd] = m6502_cd; insn6502[0xce] = m6502_ce; insn6502[0xcf] = m6502_cf;

        insn6502[0xd0] = m6502_d0; insn6502[0xd1] = m6502_d1; insn6502[0xd2] = m6502_d2; insn6502[0xd3] = m6502_d3;
        insn6502[0xd4] = m6502_d4; insn6502[0xd5] = m6502_d5; insn6502[0xd6] = m6502_d6; insn6502[0xd7] = m6502_d7;
        insn6502[0xd8] = m6502_d8; insn6502[0xd9] = m6502_d9; insn6502[0xda] = m6502_da; insn6502[0xdb] = m6502_db;
        insn6502[0xdc] = m6502_dc; insn6502[0xdd] = m6502_dd; insn6502[0xde] = m6502_de; insn6502[0xdf] = m6502_df;

        insn6502[0xe0] = m6502_e0; insn6502[0xe1] = m6502_e1; insn6502[0xe2] = m6502_e2; insn6502[0xe3] = m6502_e3;
        insn6502[0xe4] = m6502_e4; insn6502[0xe5] = m6502_e5; insn6502[0xe6] = m6502_e6; insn6502[0xe7] = m6502_e7;
        insn6502[0xe8] = m6502_e8; insn6502[0xe9] = m6502_e9; insn6502[0xea] = m6502_ea; insn6502[0xeb] = m6502_eb;
        insn6502[0xec] = m6502_ec; insn6502[0xed] = m6502_ed; insn6502[0xee] = m6502_ee; insn6502[0xef] = m6502_ef;

        insn6502[0xf0] = m6502_f0; insn6502[0xf1] = m6502_f1; insn6502[0xf2] = m6502_f2; insn6502[0xf3] = m6502_f3;
        insn6502[0xf4] = m6502_f4; insn6502[0xf5] = m6502_f5; insn6502[0xf6] = m6502_f6; insn6502[0xf7] = m6502_f7;
        insn6502[0xf8] = m6502_f8; insn6502[0xf9] = m6502_f9; insn6502[0xfa] = m6502_fa; insn6502[0xfb] = m6502_fb; 
        insn6502[0xfc] = m6502_fc; insn6502[0xfd] = m6502_fd; insn6502[0xfe] = m6502_fe; insn6502[0xff] = m6502_ff;
    }
    /****************************************************************************
    / * The 6502 registers.
    / ****************************************************************************/
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
    public static class m6502_Regs
    {
	public /*UINT8*/int	subtype;		/* currently selected cpu sub type */
        public opcode[] insn; /* pointer to the function pointer table */
        public PAIR ppc    = new PAIR();	/* previous program counter */
        public PAIR pc     = new PAIR(); 	/* program counter */
        public PAIR sp    = new PAIR(); 	/* stack pointer (always 100 - 1FF) */
        public PAIR zp    = new PAIR(); 	/* zero page address */
        public PAIR ea    = new PAIR();		/* effective address */
	public /*UINT8*/int	a;				/* Accumulator */
	public /*UINT8*/int	x;				/* X index register */
	public /*UINT8*/int	y;				/* Y index register */
	public /*UINT8*/int	p;				/* Processor status */
	public /*UINT8*/int	pending_irq;	/* nonzero if an IRQ is pending */
	public /*UINT8*/int	after_cli;		/* pending IRQ and last insn cleared I */
	public /*UINT8*/int	nmi_state;
	public /*UINT8*/int	irq_state;
	public /*UINT8*/int     so_state;
        public irqcallbacksPtr irq_callback;	/* IRQ callback */
    }

    private static m6502_Regs m6502=new m6502_Regs();
    
   /* 6502 flags */
    public static final int F_C =0x01;
    public static final int F_Z =0x02;
    public static final int F_I =0x04;
    public static final int F_D =0x08;
    public static final int F_B =0x10;
    public static final int F_T =0x20;
    public static final int F_V =0x40;
    public static final int F_N =0x80;

/*TODO*////* some shortcuts for improved readability */
/*TODO*///#define A	m6502.a
/*TODO*///#define X	m6502.x
/*TODO*///#define Y	m6502.y
/*TODO*///#define P	m6502.p
/*TODO*///#define S	m6502.sp.b.l
/*TODO*///#define SPD m6502.sp.d
/*TODO*///
/*TODO*///#define NZ	m6502.nz
/*TODO*///
    public void SET_NZ(int n)
    {
        if ((n) == 0) 
            m6502.p = ((m6502.p & ~F_N) | F_Z)&0xFF; 
        else 
            m6502.p = ((m6502.p & ~(F_N | F_Z)) | ((n) & F_N))&0xFF;
    }
/*TODO*///#define SET_Z(n)				\
/*TODO*///	if ((n) == 0) P |= F_Z; else P &= ~F_Z
/*TODO*///
/*TODO*///#define EAL m6502.ea.b.l
/*TODO*///#define EAH m6502.ea.b.h
/*TODO*///#define EAW m6502.ea.w.l
/*TODO*///#define EAD m6502.ea.d
/*TODO*///
/*TODO*///#define ZPL m6502.zp.b.l
/*TODO*///#define ZPH m6502.zp.b.h
/*TODO*///#define ZPW m6502.zp.w.l
/*TODO*///#define ZPD m6502.zp.d
/*TODO*///
/*TODO*///#define PCL m6502.pc.b.l
/*TODO*///#define PCH m6502.pc.b.h
/*TODO*///#define PCW m6502.pc.w.l
/*TODO*///#define PCD m6502.pc.d
/*TODO*///
/*TODO*///#define PPC m6502.ppc.d
/*TODO*///
/*TODO*///#if FAST_MEMORY
/*TODO*///extern	MHELE	*cur_mwhard;
/*TODO*///extern	MHELE	*cur_mrhard;
/*TODO*///extern	UINT8	*RAM;
/*TODO*///#endif

    /***************************************************************
     *	RDOP	read an opcode
     ***************************************************************/
    public int RDOP()
    {
        int tmp =cpu_readop(m6502.pc.D);
        m6502.pc.AddD(1);
        return tmp;
    }
    /***************************************************************
     *	RDOPARG read an opcode argument
     ***************************************************************/
    public int RDOPARG()
    {
        int tmp = cpu_readop_arg(m6502.pc.D);
        m6502.pc.AddD(1);
        return tmp;
    }
/*TODO*////***************************************************************
/*TODO*/// *	RDMEM	read memory
/*TODO*/// ***************************************************************/
/*TODO*///#if FAST_MEMORY
/*TODO*///#define RDMEM(addr) 											\
/*TODO*///	((cur_mrhard[(addr) >> (ABITS2_16 + ABITS_MIN_16)]) ?		\
/*TODO*///		cpu_readmem16(addr) : RAM[addr])
/*TODO*///#else
/*TODO*///#define RDMEM(addr) cpu_readmem16(addr)
/*TODO*///#endif
/*TODO*///
    public int RDMEM(int addr)
    {
        return cpu_readmem16(addr);
    }
/*TODO*////***************************************************************
/*TODO*/// *	WRMEM	write memory
/*TODO*/// ***************************************************************/
/*TODO*///#if FAST_MEMORY
/*TODO*///#define WRMEM(addr,data)										\
/*TODO*///	if (cur_mwhard[(addr) >> (ABITS2_16 + ABITS_MIN_16)])		\
/*TODO*///		cpu_writemem16(addr,data);								\
/*TODO*///	else														\
/*TODO*///		RAM[addr] = data
/*TODO*///#else
/*TODO*///#define WRMEM(addr,data) cpu_writemem16(addr,data)
/*TODO*///#endif
/*TODO*///
    public void WRMEM(int addr,int data)
    {
        cpu_writemem16(addr,data);
    }
/*TODO*////***************************************************************
/*TODO*/// *	BRA  branch relative
/*TODO*/// *	extra cycle if page boundary is crossed
/*TODO*/// ***************************************************************/
/*TODO*///#define BRA(cond)												\
/*TODO*///	if (cond)													\
/*TODO*///	{															\
/*TODO*///		tmp = RDOPARG();										\
/*TODO*///		EAW = PCW + (signed char)tmp;							\
/*TODO*///		m6502_ICount -= (PCH == EAH) ? 3 : 4;					\
/*TODO*///		PCD = EAD;												\
/*TODO*///		change_pc16(PCD);										\
/*TODO*///	}															\
/*TODO*///	else														\
/*TODO*///	{															\
/*TODO*///		PCW++;													\
/*TODO*///		m6502_ICount -= 2;										\
/*TODO*///	}
/*TODO*///
    public void BRA(boolean cond)
    {
        if(cond)
        {
            int tmp = RDOPARG();
            m6502.ea.SetD(m6502.pc.D + (byte)tmp);
            m6502_ICount[0] -= (m6502.pc.H == m6502.ea.H) ? 3 : 4;
            m6502.pc.SetD(m6502.ea.D);
            change_pc16(m6502.pc.D);
        }
        else
        {
            m6502.pc.AddD(1);
            m6502_ICount[0] -=2;
        }
    }
/*TODO*////***************************************************************
/*TODO*/// *
/*TODO*/// * Helper macros to build the effective address
/*TODO*/// *
/*TODO*/// ***************************************************************/
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page address
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPG													\
/*TODO*///	ZPL = RDOPARG();											\
/*TODO*///	EAD = ZPD
/*TODO*///
    public void EA_ZPG()
    {
        m6502.zp.SetL(RDOPARG());
        m6502.ea.SetD(m6502.zp.D);
    }
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page address + X
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPX													\
/*TODO*///	ZPL = RDOPARG() + X;										\
/*TODO*///	EAD = ZPD
/*TODO*///
    public void EA_ZPX()
    {
        m6502.zp.SetL(RDOPARG() + m6502.x);
        m6502.ea.SetD(m6502.zp.D);
    }
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page address + Y
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPY													\
/*TODO*///	ZPL = RDOPARG() + Y;										\
/*TODO*///	EAD = ZPD
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = absolute address
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABS													\
/*TODO*///	EAL = RDOPARG();											\
/*TODO*///	EAH = RDOPARG()
    public void EA_ABS()
    {
        m6502.ea.SetL(RDOPARG());
        m6502.ea.SetH(RDOPARG());
    }

/*TODO*////***************************************************************
/*TODO*/// *	EA = absolute address + X
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABX													\
/*TODO*///	EA_ABS; 													\
/*TODO*///	EAW += X
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = absolute address + Y
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ABY													\
/*TODO*///	EA_ABS; 													\
/*TODO*///	EAW += Y
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page indirect (65c02 pre indexed w/o X)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_ZPI													\
/*TODO*///	ZPL = RDOPARG();											\
/*TODO*///	EAL = RDMEM(ZPD);											\
/*TODO*///	ZPL++;														\
/*TODO*///	EAH = RDMEM(ZPD)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page + X indirect (pre indexed)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IDX													\
/*TODO*///	ZPL = RDOPARG() + X;										\
/*TODO*///	EAL = RDMEM(ZPD);											\
/*TODO*///	ZPL++;														\
/*TODO*///	EAH = RDMEM(ZPD)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = zero page indirect + Y (post indexed)
/*TODO*/// *	subtract 1 cycle if page boundary is crossed
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IDY													\
/*TODO*///	ZPL = RDOPARG();											\
/*TODO*///	EAL = RDMEM(ZPD);											\
/*TODO*///	ZPL++;														\
/*TODO*///	EAH = RDMEM(ZPD);											\
/*TODO*///	if (EAL + Y > 0xff) 										\
/*TODO*///		m6502_ICount--; 										\
/*TODO*///	EAW += Y
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = indirect (only used by JMP)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IND													\
/*TODO*///	EA_ABS; 													\
/*TODO*///	tmp = RDMEM(EAD);											\
/*TODO*///	EAL++;	/* booby trap: stay in same page! ;-) */			\
/*TODO*///	EAH = RDMEM(EAD);											\
/*TODO*///	EAL = tmp
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// *	EA = indirect plus x (only used by 65c02 JMP)
/*TODO*/// ***************************************************************/
/*TODO*///#define EA_IAX													\
/*TODO*///	EA_IND; 													\
/*TODO*///	if (EAL + X > 0xff) /* assumption; probably wrong ? */		\
/*TODO*///		m6502_ICount--; 										\
/*TODO*///	EAW += X
/*TODO*///
/*TODO*////* read a value into tmp */
/*TODO*///#define RD_IMM	tmp = RDOPARG()
    public int RD_IMM()
    {
        return RDOPARG();
    }
/*TODO*///#define RD_ACC	tmp = A
/*TODO*///#define RD_ZPG	EA_ZPG; tmp = RDMEM(EAD)
/*TODO*///#define RD_ZPX	EA_ZPX; tmp = RDMEM(EAD)
/*TODO*///#define RD_ZPY	EA_ZPY; tmp = RDMEM(EAD)
/*TODO*///#define RD_ABS	EA_ABS; tmp = RDMEM(EAD)
/*TODO*///#define RD_ABX	EA_ABX; tmp = RDMEM(EAD)
/*TODO*///#define RD_ABY	EA_ABY; tmp = RDMEM(EAD)
/*TODO*///#define RD_ZPI	EA_ZPI; tmp = RDMEM(EAD)
/*TODO*///#define RD_IDX	EA_IDX; tmp = RDMEM(EAD)
/*TODO*///#define RD_IDY	EA_IDY; tmp = RDMEM(EAD)
/*TODO*///
/*TODO*////* write a value from tmp */
/*TODO*///#define WR_ZPG	EA_ZPG; WRMEM(EAD, tmp)
    public void WR_ZPG(int tmp)
    {
        EA_ZPG();
        WRMEM(m6502.ea.D,tmp);
    }
/*TODO*///#define WR_ZPX	EA_ZPX; WRMEM(EAD, tmp)
    public void WR_ZPX(int tmp)
    {
        EA_ZPX();
        WRMEM(m6502.ea.D,tmp);
    }
/*TODO*///#define WR_ZPY	EA_ZPY; WRMEM(EAD, tmp)
/*TODO*///#define WR_ABS	EA_ABS; WRMEM(EAD, tmp)
/*TODO*///#define WR_ABX	EA_ABX; WRMEM(EAD, tmp)
/*TODO*///#define WR_ABY	EA_ABY; WRMEM(EAD, tmp)
/*TODO*///#define WR_ZPI	EA_ZPI; WRMEM(EAD, tmp)
/*TODO*///#define WR_IDX	EA_IDX; WRMEM(EAD, tmp)
/*TODO*///#define WR_IDY	EA_IDY; WRMEM(EAD, tmp)
/*TODO*///
/*TODO*////* write back a value from tmp to the last EA */
/*TODO*///#define WB_ACC	A = (UINT8)tmp;
/*TODO*///#define WB_EA	WRMEM(EAD, tmp)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// ***************************************************************
/*TODO*/// *			Macros to emulate the plain 6502 opcodes
/*TODO*/// ***************************************************************
/*TODO*/// ***************************************************************/
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * push a register onto the stack
/*TODO*/// ***************************************************************/
/*TODO*///#define PUSH(Rg) WRMEM(SPD, Rg); S--
/*TODO*///
    public void PUSH(int Rg)
    {
        WRMEM(m6502.sp.D,Rg);
        m6502.sp.AddL(-1);
    }
/*TODO*////***************************************************************
/*TODO*/// * pull a register from the stack
/*TODO*/// ***************************************************************/
/*TODO*///#define PULL(Rg) S++; Rg = RDMEM(SPD)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	ADC Add with carry
/*TODO*/// ***************************************************************/
/*TODO*///#define ADC 													\
/*TODO*///	if (P & F_D)												\
/*TODO*///	{															\
/*TODO*///	int c = (P & F_C);											\
/*TODO*///	int lo = (A & 0x0f) + (tmp & 0x0f) + c; 					\
/*TODO*///	int hi = (A & 0xf0) + (tmp & 0xf0); 						\
/*TODO*///		P &= ~(F_V | F_C);										\
/*TODO*///		if (lo > 0x09)											\
/*TODO*///		{														\
/*TODO*///			hi += 0x10; 										\
/*TODO*///			lo += 0x06; 										\
/*TODO*///		}														\
/*TODO*///		if (~(A^tmp) & (A^hi) & F_N)							\
/*TODO*///			P |= F_V;											\
/*TODO*///		if (hi > 0x90)											\
/*TODO*///			hi += 0x60; 										\
/*TODO*///		if (hi & 0xff00)										\
/*TODO*///			P |= F_C;											\
/*TODO*///		A = (lo & 0x0f) + (hi & 0xf0);							\
/*TODO*///	}															\
/*TODO*///	else														\
/*TODO*///	{															\
/*TODO*///	int c = (P & F_C);											\
/*TODO*///	int sum = A + tmp + c;										\
/*TODO*///		P &= ~(F_V | F_C);										\
/*TODO*///		if (~(A^tmp) & (A^sum) & F_N)							\
/*TODO*///			P |= F_V;											\
/*TODO*///		if (sum & 0xff00)										\
/*TODO*///			P |= F_C;											\
/*TODO*///		A = (UINT8) sum;										\
/*TODO*///	}															\
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	AND Logical and
/*TODO*/// ***************************************************************/
/*TODO*///#define AND 													\
/*TODO*///	A = (UINT8)(A & tmp);										\
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	ASL Arithmetic shift left
/*TODO*/// ***************************************************************/
/*TODO*///#define ASL 													\
/*TODO*///	P = (P & ~F_C) | ((tmp >> 7) & F_C);						\
/*TODO*///	tmp = (UINT8)(tmp << 1);									\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BCC Branch if carry clear
/*TODO*/// ***************************************************************/
/*TODO*///#define BCC BRA(!(P & F_C))
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BCS Branch if carry set
/*TODO*/// ***************************************************************/
/*TODO*///#define BCS BRA(P & F_C)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BEQ Branch if equal
/*TODO*/// ***************************************************************/
/*TODO*///#define BEQ BRA(P & F_Z)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BIT Bit test
/*TODO*/// ***************************************************************/
/*TODO*///#define BIT 													\
/*TODO*///	P &= ~(F_N|F_V|F_Z);										\
/*TODO*///	P |= tmp & (F_N|F_V);										\
/*TODO*///	if ((tmp & A) == 0) 										\
/*TODO*///		P |= F_Z
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BMI Branch if minus
/*TODO*/// ***************************************************************/
/*TODO*///#define BMI BRA(P & F_N)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BNE Branch if not equal
/*TODO*/// ***************************************************************/
/*TODO*///#define BNE BRA(!(P & F_Z))
/*TODO*///
    public void BNE()
    {
        BRA((m6502.p & F_Z)==0);
    }
            
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BPL Branch if plus
/*TODO*/// ***************************************************************/
/*TODO*///#define BPL BRA(!(P & F_N))
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	BRK Break
/*TODO*/// *	increment PC, push PC hi, PC lo, flags (with B bit set),
/*TODO*/// *	set I flag, reset D flag and jump via IRQ vector
/*TODO*/// ***************************************************************/
/*TODO*///#define BRK 													\
/*TODO*///	PCW++;														\
/*TODO*///	PUSH(PCH);													\
/*TODO*///	PUSH(PCL);													\
/*TODO*///	PUSH(P | F_B);												\
/*TODO*///	P = (P | F_I) & ~F_D;										\
/*TODO*///	PCL = RDMEM(M6502_IRQ_VEC); 								\
/*TODO*///	PCH = RDMEM(M6502_IRQ_VEC+1);								\
/*TODO*///	change_pc16(PCD)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * BVC	Branch if overflow clear
/*TODO*/// ***************************************************************/
/*TODO*///#define BVC BRA(!(P & F_V))
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * BVS	Branch if overflow set
/*TODO*/// ***************************************************************/
/*TODO*///#define BVS BRA(P & F_V)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * CLC	Clear carry flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLC 													\
/*TODO*///	P &= ~F_C

    /* 6502 ********************************************************
     * CLD	Clear decimal flag
     ***************************************************************/
    public void CLD()
    {
            //m6502.p &= ~F_D;
            m6502.p = (m6502.p & ~F_D) & 0xFF;
    }

/*TODO*////* 6502 ********************************************************
/*TODO*/// * CLI	Clear interrupt flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLI 													\
/*TODO*///	if ((m6502.irq_state != CLEAR_LINE) && (P & F_I)) { 		\
/*TODO*///		LOG((errorlog, "M6502#%d CLI sets after_cli\n",cpu_getactivecpu())); \
/*TODO*///		m6502.after_cli = 1;									\
/*TODO*///	}															\
/*TODO*///	P &= ~F_I
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * CLV	Clear overflow flag
/*TODO*/// ***************************************************************/
/*TODO*///#define CLV 													\
/*TODO*///	P &= ~F_V
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	CMP Compare accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define CMP 													\
/*TODO*///	P &= ~F_C;													\
/*TODO*///	if (A >= tmp)												\
/*TODO*///		P |= F_C;												\
/*TODO*///	SET_NZ((UINT8)(A - tmp))
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	CPX Compare index X
/*TODO*/// ***************************************************************/
/*TODO*///#define CPX 													\
/*TODO*///	P &= ~F_C;													\
/*TODO*///	if (X >= tmp)												\
/*TODO*///		P |= F_C;												\
/*TODO*///	SET_NZ((UINT8)(X - tmp))
/*TODO*///
    public void CPX(int tmp)
    {
        m6502.p = (m6502.p & ~F_C) & 0xFF;
        if(m6502.x >=tmp)
            m6502.p = (m6502.p | F_C) & 0xFF;
        SET_NZ((m6502.x-tmp)&0xFF);
    }
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	CPY Compare index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define CPY 													\
/*TODO*///	P &= ~F_C;													\
/*TODO*///	if (Y >= tmp)												\
/*TODO*///		P |= F_C;												\
/*TODO*///	SET_NZ((UINT8)(Y - tmp))
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	DEC Decrement memory
/*TODO*/// ***************************************************************/
/*TODO*///#define DEC 													\
/*TODO*///	tmp = (UINT8)--tmp; 										\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	DEX Decrement index X
/*TODO*/// ***************************************************************/
/*TODO*///#define DEX 													\
/*TODO*///	X = (UINT8)--X; 											\
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	DEY Decrement index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define DEY 													\
/*TODO*///	Y = (UINT8)--Y; 											\
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	EOR Logical exclusive or
/*TODO*/// ***************************************************************/
/*TODO*///#define EOR 													\
/*TODO*///	A = (UINT8)(A ^ tmp);										\
/*TODO*///	SET_NZ(A)
/*TODO*///
   /* 6502 ********************************************************
    *	ILL Illegal opcode
    ***************************************************************/
    public void ILL()
    {
        if (errorlog!=null)												
		fprintf(errorlog, "M6502 illegal opcode %04x: %02x\n",  
			(m6502.pc.D-1)&0xffff, cpu_readop((m6502.pc.D-1)&0xffff));
    }

/*TODO*////* 6502 ********************************************************
/*TODO*/// *	INC Increment memory
/*TODO*/// ***************************************************************/
/*TODO*///#define INC 													\
/*TODO*///	tmp = (UINT8)++tmp; 										\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	INX Increment index X
/*TODO*/// ***************************************************************/
/*TODO*///#define INX 													\
/*TODO*///	X = (UINT8)++X; 											\
/*TODO*///	SET_NZ(X)
/*TODO*///
    public void INX()
    {
        m6502.x = (m6502.x+1)&0xFF;
        SET_NZ(m6502.x);
    }
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	INY Increment index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define INY 													\
/*TODO*///	Y = (UINT8)++Y; 											\
/*TODO*///	SET_NZ(Y)
/*TODO*///
    /* 6502 ********************************************************
     *	JMP Jump to address
     *	set PC to the effective address
     ***************************************************************/
    public void JMP() 
    {
            if( m6502.ea.D == m6502.ppc.D && m6502.pending_irq==0 && m6502.after_cli==0 )	
                    if( m6502_ICount[0] > 0 ) m6502_ICount[0] = 0;
            m6502.pc.SetD(m6502.ea.D);												
            change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
     *	JSR Jump to subroutine
     *	decrement PC (sic!) push PC hi, push PC lo and set
     *	PC to the effective address
     ***************************************************************/
    public void JSR()
    {        
            m6502.ea.SetL(RDOPARG());											
            PUSH(m6502.pc.H);													
            PUSH(m6502.pc.L);													
            m6502.ea.SetH(RDOPARG());											
            m6502.pc.SetD(m6502.ea.D);//PCD = EAD;													
            change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
     *	LDA Load accumulator
     ***************************************************************/
    public void LDA(int tmp)
    {
        m6502.a = tmp & 0xFF;
        SET_NZ(m6502.a);
    }
    /* 6502 ********************************************************
     *	LDX Load index X
     ***************************************************************/
    public void LDX(int tmp)
    {
        m6502.x = tmp & 0xFF;
        SET_NZ(m6502.x);
    }
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	LDY Load index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define LDY 													\
/*TODO*///	Y = (UINT8)tmp; 											\
/*TODO*///	SET_NZ(Y)
/*TODO*///
    public void LDY(int tmp)
    {
        m6502.y = tmp & 0xFF;
        SET_NZ(m6502.y);
    }
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	LSR Logic shift right
/*TODO*/// *	0 -> [7][6][5][4][3][2][1][0] -> C
/*TODO*/// ***************************************************************/
/*TODO*///#define LSR 													\
/*TODO*///	P = (P & ~F_C) | (tmp & F_C);								\
/*TODO*///	tmp = (UINT8)tmp >> 1;										\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	NOP No operation
/*TODO*/// ***************************************************************/
/*TODO*///#define NOP
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	ORA Logical inclusive or
/*TODO*/// ***************************************************************/
/*TODO*///#define ORA 													\
/*TODO*///	A = (UINT8)(A | tmp);										\
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	PHA Push accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define PHA 													\
/*TODO*///	PUSH(A)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	PHP Push processor status (flags)
/*TODO*/// ***************************************************************/
/*TODO*///#define PHP 													\
/*TODO*///	PUSH(P)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	PLA Pull accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define PLA 													\
/*TODO*///	PULL(A);													\
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	PLP Pull processor status (flags)
/*TODO*/// ***************************************************************/
/*TODO*///#define PLP 													\
/*TODO*///	if ( P & F_I ) {											\
/*TODO*///		PULL(P);												\
/*TODO*///		if ((m6502.irq_state != CLEAR_LINE) && !(P & F_I)) {	\
/*TODO*///			LOG((errorlog, "M6502#%d PLP sets after_cli\n",cpu_getactivecpu())); \
/*TODO*///			m6502.after_cli = 1;								\
/*TODO*///		}														\
/*TODO*///	} else {													\
/*TODO*///		PULL(P);												\
/*TODO*///	}															\
/*TODO*///	P |= F_T
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * ROL	Rotate left
/*TODO*/// *	new C <- [7][6][5][4][3][2][1][0] <- C
/*TODO*/// ***************************************************************/
/*TODO*///#define ROL 													\
/*TODO*///	tmp = (tmp << 1) | (P & F_C);								\
/*TODO*///	P = (P & ~F_C) | ((tmp >> 8) & F_C);						\
/*TODO*///	tmp = (UINT8)tmp;											\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * ROR	Rotate right
/*TODO*/// *	C -> [7][6][5][4][3][2][1][0] -> new C
/*TODO*/// ***************************************************************/
/*TODO*///#define ROR 													\
/*TODO*///	tmp |= (P & F_C) << 8;										\
/*TODO*///	P = (P & ~F_C) | (tmp & F_C);								\
/*TODO*///	tmp = (UINT8)(tmp >> 1);									\
/*TODO*///	SET_NZ(tmp)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * RTI	Return from interrupt
/*TODO*/// * pull flags, pull PC lo, pull PC hi and increment PC
/*TODO*/// *	PCW++;
/*TODO*/// ***************************************************************/
/*TODO*///#define RTI 													\
/*TODO*///	PULL(P);													\
/*TODO*///	PULL(PCL);													\
/*TODO*///	PULL(PCH);													\
/*TODO*///	P |= F_T;													\
/*TODO*///	if( (m6502.irq_state != CLEAR_LINE) && !(P & F_I) ) 		\
/*TODO*///	{															\
/*TODO*///		LOG((errorlog, "M6502#%d RTI sets after_cli\n",cpu_getactivecpu())); \
/*TODO*///		m6502.after_cli = 1;									\
/*TODO*///	}															\
/*TODO*///	change_pc16(PCD)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	RTS Return from subroutine
/*TODO*/// *	pull PC lo, PC hi and increment PC
/*TODO*/// ***************************************************************/
/*TODO*///#define RTS 													\
/*TODO*///	PULL(PCL);													\
/*TODO*///	PULL(PCH);													\
/*TODO*///	PCW++;														\
/*TODO*///	change_pc16(PCD)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	SBC Subtract with carry
/*TODO*/// ***************************************************************/
/*TODO*///#define SBC 													\
/*TODO*///	if (P & F_D)												\
/*TODO*///	{															\
/*TODO*///	int c = (P & F_C) ^ F_C;									\
/*TODO*///	int sum = A - tmp - c;										\
/*TODO*///	int lo = (A & 0x0f) - (tmp & 0x0f) - c; 					\
/*TODO*///	int hi = (A & 0xf0) - (tmp & 0xf0); 						\
/*TODO*///		P &= ~(F_V | F_C);										\
/*TODO*///		if ((A^tmp) & (A^sum) & F_N)							\
/*TODO*///			P |= F_V;											\
/*TODO*///		if (lo & 0xf0)											\
/*TODO*///			lo -= 6;											\
/*TODO*///		if (lo & 0x80)											\
/*TODO*///			hi -= 0x10; 										\
/*TODO*///		if (hi & 0x0f00)										\
/*TODO*///			hi -= 0x60; 										\
/*TODO*///		if ((sum & 0xff00) == 0)								\
/*TODO*///			P |= F_C;											\
/*TODO*///		A = (lo & 0x0f) + (hi & 0xf0);							\
/*TODO*///	}															\
/*TODO*///	else														\
/*TODO*///	{															\
/*TODO*///	int c = (P & F_C) ^ F_C;									\
/*TODO*///	int sum = A - tmp - c;										\
/*TODO*///		P &= ~(F_V | F_C);										\
/*TODO*///		if ((A^tmp) & (A^sum) & F_N)							\
/*TODO*///			P |= F_V;											\
/*TODO*///		if ((sum & 0xff00) == 0)								\
/*TODO*///			P |= F_C;											\
/*TODO*///		A = (UINT8) sum;										\
/*TODO*///	}															\
/*TODO*///	SET_NZ(A)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	SEC Set carry flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SEC 													\
/*TODO*///	P |= F_C
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// *	SED Set decimal flag
/*TODO*/// ***************************************************************/
/*TODO*///#define SED 													\
/*TODO*///	P |= F_D

    /* 6502 ********************************************************
     *	SEI Set interrupt flag
     ***************************************************************/
    public void SEI()
    {
            //m6502.p |= F_I;
        m6502.p = (m6502.p | F_I) & 0xFF;
    }

/*TODO*////* 6502 ********************************************************
/*TODO*/// * STA	Store accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define STA 													\
/*TODO*///	tmp = A
/*TODO*///
    public int STA()
    {
        return m6502.a;
    }
/*TODO*////* 6502 ********************************************************
/*TODO*/// * STX	Store index X
/*TODO*/// ***************************************************************/
/*TODO*///#define STX 													\
/*TODO*///	tmp = X
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * STY	Store index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define STY 													\
/*TODO*///	tmp = Y
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * TAX	Transfer accumulator to index X
/*TODO*/// ***************************************************************/
/*TODO*///#define TAX 													\
/*TODO*///	X = A;														\
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * TAY	Transfer accumulator to index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define TAY 													\
/*TODO*///	Y = A;														\
/*TODO*///	SET_NZ(Y)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * TSX	Transfer stack LSB to index X
/*TODO*/// ***************************************************************/
/*TODO*///#define TSX 													\
/*TODO*///	X = S;														\
/*TODO*///	SET_NZ(X)
/*TODO*///
/*TODO*////* 6502 ********************************************************
/*TODO*/// * TXA	Transfer index X to accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define TXA 													\
/*TODO*///	A = X;														\
/*TODO*///	SET_NZ(A)
/*TODO*///
/* 6502 ********************************************************
 * TXS	Transfer index X to stack LSB
 * no flags changed (sic!)
 ***************************************************************/
    public void TXS ()
    {
	m6502.sp.SetL(m6502.x);//S = X
    }

/*TODO*////* 6502 ********************************************************
/*TODO*/// * TYA	Transfer index Y to accumulator
/*TODO*/// ***************************************************************/
/*TODO*///#define TYA 													\
/*TODO*///	A = Y;														\
/*TODO*///	SET_NZ(A)

    

    /*****************************************************************************
     *
     *	 plain vanilla 6502 opcodes
     *
     *****************************************************************************/
    opcode m6502_00 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* 		  m6502_ICount[0] -= 7;		 BRK;		  */ }}; /* 7 BRK */
    opcode m6502_20 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 6;		 
        JSR();		  
        if(m6502log!=null) fprintf(m6502log,"M6502#%d 20 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
        
    }}; /* 6 JSR */
    opcode m6502_40 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 6;		 RTI;		  */ }}; /* 6 RTI */
    opcode m6502_60 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 6;		 RTS;		  */ }}; /* 6 RTS */
    opcode m6502_a0 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 2; 
        int tmp=RD_IMM(); 
        LDY(tmp);
        if(m6502log!=null) fprintf(m6502log,"M6502#%d a0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 2 LDY IMM */
    opcode m6502_c0 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; CPY;		  */ }}; /* 2 CPY IMM */
    opcode m6502_e0 = new opcode() { public void handler()
    {  
        
        m6502_ICount[0] -= 2; 
        int tmp=RD_IMM(); 
        CPX(tmp);		 
        if(m6502log!=null) fprintf(m6502log,"M6502#%d e0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    }}; /* 2 CPX IMM */
    opcode m6502_10 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BPL;		  */ }}; /* 2 BPL REL */
    opcode m6502_30 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BMI;		  */ }}; /* 2 BMI REL */
    opcode m6502_50 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BVC;		  */ }}; /* 2 BVC REL */
    opcode m6502_70 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BVS;		  */ }}; /* 2 BVS REL */
    opcode m6502_90 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BCC;		  */ }}; /* 2 BCC REL */
    opcode m6502_b0 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BCS;		  */ }}; /* 2 BCS REL */
    opcode m6502_d0 = new opcode() { public void handler()
    {  
        BNE();
        if(m6502log!=null) fprintf(m6502log,"M6502#%d d0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 2 BNE REL */
    opcode m6502_f0 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp;							 BEQ;		  */ }}; /* 2 BEQ REL */
    opcode m6502_01 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; ORA;		  */ }}; /* 6 ORA IDX */
    opcode m6502_21 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; AND;		  */ }}; /* 6 AND IDX */
    opcode m6502_41 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; EOR;		  */ }}; /* 6 EOR IDX */
    opcode m6502_61 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; ADC;		  */ }}; /* 6 ADC IDX */
    opcode m6502_81 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6;		 STA; WR_IDX; */ }}; /* 6 STA IDX */
    opcode m6502_a1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; LDA;		  */ }}; /* 6 LDA IDX */
    opcode m6502_c1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; CMP;		  */ }}; /* 6 CMP IDX */
    opcode m6502_e1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_IDX; SBC;		  */ }}; /* 6 SBC IDX */

    opcode m6502_11 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; ORA;		  */ }}; /* 5 ORA IDY */
    opcode m6502_31 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; AND;		  */ }}; /* 5 AND IDY */
    opcode m6502_51 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; EOR;		  */ }}; /* 5 EOR IDY */
    opcode m6502_71 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; ADC;		  */ }}; /* 5 ADC IDY */
    opcode m6502_91 = new opcode() { public void handler()
    {  
        fclose(m6502log);
        throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6;		 STA; WR_IDY; */ 
    }}; /* 6 STA IDY */
    opcode m6502_b1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; LDA;		  */ }}; /* 5 LDA IDY */
    opcode m6502_d1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; CMP;		  */ }}; /* 5 CMP IDY */
    opcode m6502_f1 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_IDY; SBC;		  */ }}; /* 5 SBC IDY */


    opcode m6502_a2 = new opcode() { public void handler()
    {   
        m6502_ICount[0] -= 2; 
        int tmp=RD_IMM(); 
        LDX(tmp);		  
        if(m6502log!=null) fprintf(m6502log,"M6502#%d a2 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
   
    }}; /* 2 LDX IMM */

    opcode m6502_24 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; BIT;		  */ }}; /* 3 BIT ZPG */

    opcode m6502_84 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3;		 STY; WR_ZPG; */ }}; /* 3 STY ZPG */
    opcode m6502_a4 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; LDY;		  */ }}; /* 3 LDY ZPG */
    opcode m6502_c4 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; CPY;		  */ }}; /* 3 CPY ZPG */
    opcode m6502_e4 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; CPX;		  */ }}; /* 3 CPX ZPG */


    opcode m6502_94 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4;		 STY; WR_ZPX; */ }}; /* 4 STY ZPX */
    opcode m6502_b4 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; LDY;		  */ }}; /* 4 LDY ZPX */


    opcode m6502_05 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; ORA;		  */ }}; /* 3 ORA ZPG */
    opcode m6502_25 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; AND;		  */ }}; /* 3 AND ZPG */
    opcode m6502_45 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; EOR;		  */ }}; /* 3 EOR ZPG */
    opcode m6502_65 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; ADC;		  */ }}; /* 3 ADC ZPG */
    opcode m6502_85 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 3;		 
        int tmp=STA(); 
        WR_ZPG(tmp);
        if(m6502log!=null) fprintf(m6502log,"M6502#%d 85 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    }}; /* 3 STA ZPG */
    opcode m6502_a5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; LDA;		  */ }}; /* 3 LDA ZPG */
    opcode m6502_c5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; CMP;		  */ }}; /* 3 CMP ZPG */
    opcode m6502_e5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; SBC;		  */ }}; /* 3 SBC ZPG */

    opcode m6502_15 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; ORA;		  */ }}; /* 4 ORA ZPX */
    opcode m6502_35 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; AND;		  */ }}; /* 4 AND ZPX */
    opcode m6502_55 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; EOR;		  */ }}; /* 4 EOR ZPX */
    opcode m6502_75 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; ADC;		  */ }}; /* 4 ADC ZPX */
    opcode m6502_95 = new opcode() { public void handler()
    {  
         m6502_ICount[0] -= 4;		 
         int tmp=STA(); 
         WR_ZPX(tmp);
         if(m6502log!=null) fprintf(m6502log,"M6502#%d 95 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 4 STA ZPX */
    opcode m6502_b5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; LDA;		  */ }}; /* 4 LDA ZPX */
    opcode m6502_d5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; CMP;		  */ }}; /* 4 CMP ZPX */
    opcode m6502_f5 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPX; SBC;		  */ }}; /* 4 SBC ZPX */

    opcode m6502_06 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; ASL; WB_EA;  */ }}; /* 5 ASL ZPG */
    opcode m6502_26 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; ROL; WB_EA;  */ }}; /* 5 ROL ZPG */
    opcode m6502_46 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; LSR; WB_EA;  */ }}; /* 5 LSR ZPG */
    opcode m6502_66 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; ROR; WB_EA;  */ }}; /* 5 ROR ZPG */
    opcode m6502_86 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3;		 STX; WR_ZPG; */ }}; /* 3 STX ZPG */
    opcode m6502_a6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 3; RD_ZPG; LDX;		  */ }}; /* 3 LDX ZPG */
    opcode m6502_c6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; DEC; WB_EA;  */ }}; /* 5 DEC ZPG */
    opcode m6502_e6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; RD_ZPG; INC; WB_EA;  */ }}; /* 5 INC ZPG */

    opcode m6502_16 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; ASL; WB_EA;  */ }}; /* 6 ASL ZPX */
    opcode m6502_36 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; ROL; WB_EA;  */ }}; /* 6 ROL ZPX */
    opcode m6502_56 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; LSR; WB_EA;  */ }}; /* 6 LSR ZPX */
    opcode m6502_76 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; ROR; WB_EA;  */ }}; /* 6 ROR ZPX */
    opcode m6502_96 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4;		 STX; WR_ZPY; */ }}; /* 4 STX ZPY */
    opcode m6502_b6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ZPY; LDX;		  */ }}; /* 4 LDX ZPY */
    opcode m6502_d6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; DEC; WB_EA;  */ }}; /* 6 DEC ZPX */
    opcode m6502_f6 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ZPX; INC; WB_EA;  */ }}; /* 6 INC ZPX */



    opcode m6502_08 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 PHP;		  */ }}; /* 2 PHP */
    opcode m6502_28 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 PLP;		  */ }}; /* 2 PLP */
    opcode m6502_48 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 PHA;		  */ }}; /* 2 PHA */
    opcode m6502_68 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 PLA;		  */ }}; /* 2 PLA */
    opcode m6502_88 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 DEY;		  */ }}; /* 2 DEY */
    opcode m6502_a8 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 TAY;		  */ }}; /* 2 TAY */
    opcode m6502_c8 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 INY;		  */ }}; /* 2 INY */
    opcode m6502_e8 = new opcode() { public void handler()
    {  
	m6502_ICount[0] -= 2;		 
        INX();
        if(m6502log!=null) fprintf(m6502log,"M6502#%d e8 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    
    }}; /* 2 INX */

    opcode m6502_18 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 CLC;		  */ }}; /* 2 CLC */
    opcode m6502_38 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 SEC;		  */ }}; /* 2 SEC */
    opcode m6502_58 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 CLI;		  */ }}; /* 2 CLI */
    opcode m6502_78 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 2;		 
        SEI();		
        if(m6502log!=null) fprintf(m6502log,"M6502#%d 78 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    }}; /* 2 SEI */
    opcode m6502_98 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 TYA;		  */ }}; /* 2 TYA */
    opcode m6502_b8 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 CLV;		  */ }}; /* 2 CLV */
    opcode m6502_d8 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 2;		 
        CLD();
        if(m6502log!=null) fprintf(m6502log,"M6502#%d d8 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    }}; /* 2 CLD */
    opcode m6502_f8 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 SED;		  */ }}; /* 2 SED */

    opcode m6502_09 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; ORA;		  */ }}; /* 2 ORA IMM */
    opcode m6502_29 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; AND;		  */ }}; /* 2 AND IMM */
    opcode m6502_49 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; EOR;		  */ }}; /* 2 EOR IMM */
    opcode m6502_69 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; ADC;		  */ }}; /* 2 ADC IMM */

    opcode m6502_a9 = new opcode() { public void handler()
    {   
         m6502_ICount[0] -= 2; 
         int tmp =RD_IMM(); 
         LDA(tmp);	
         if(m6502log!=null) fprintf(m6502log,"M6502#%d a9 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 2 LDA IMM */
    opcode m6502_c9 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; CMP;		  */ }}; /* 2 CMP IMM */
    opcode m6502_e9 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_IMM; SBC;		  */ }}; /* 2 SBC IMM */

    opcode m6502_19 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; ORA;		  */ }}; /* 4 ORA ABY */
    opcode m6502_39 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; AND;		  */ }}; /* 4 AND ABY */
    opcode m6502_59 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; EOR;		  */ }}; /* 4 EOR ABY */
    opcode m6502_79 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; ADC;		  */ }}; /* 4 ADC ABY */
    opcode m6502_99 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5;		 STA; WR_ABY; */ }}; /* 5 STA ABY */
    opcode m6502_b9 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; LDA;		  */ }}; /* 4 LDA ABY */
    opcode m6502_d9 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; CMP;		  */ }}; /* 4 CMP ABY */
    opcode m6502_f9 = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; SBC;		  */ }}; /* 4 SBC ABY */

    opcode m6502_0a = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_ACC; ASL; WB_ACC; */ }}; /* 2 ASL A */
    opcode m6502_2a = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_ACC; ROL; WB_ACC; */ }}; /* 2 ROL A */
    opcode m6502_4a = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_ACC; LSR; WB_ACC; */ }}; /* 2 LSR A */
    opcode m6502_6a = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 2; RD_ACC; ROR; WB_ACC; */ }}; /* 2 ROR A */
    opcode m6502_8a = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 TXA;		  */ }}; /* 2 TXA */
    opcode m6502_aa = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 TAX;		  */ }}; /* 2 TAX */
    opcode m6502_ca = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 DEX;		  */ }}; /* 2 DEX */
    opcode m6502_ea = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 NOP;		  */ }}; /* 2 NOP */


    opcode m6502_9a = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 2;		 
        TXS();		  
        if(m6502log!=null) fprintf(m6502log,"M6502#%d 9a :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);      

    }}; /* 2 TXS */
    opcode m6502_ba = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /*		  m6502_ICount[0] -= 2;		 TSX;		  */ }}; /* 2 TSX */

 
    opcode m6502_2c = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; BIT;		  */ }}; /* 4 BIT ABS */
    opcode m6502_4c = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 3; 
        EA_ABS();
        JMP();
        if(m6502log!=null) fprintf(m6502log,"M6502#%d 4c :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);      
    }}; /* 3 JMP ABS */
    opcode m6502_6c = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5; EA_IND; JMP;		  */ }}; /* 5 JMP IND */
    opcode m6502_8c = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4;		 STY; WR_ABS; */ }}; /* 4 STY ABS */
    opcode m6502_ac = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; LDY;		  */ }}; /* 4 LDY ABS */
    opcode m6502_cc = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; CPY;		  */ }}; /* 4 CPY ABS */
    opcode m6502_ec = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; CPX;		  */ }}; /* 4 CPX ABS */


    opcode m6502_bc = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; LDY;		  */ }}; /* 4 LDY ABX */


    opcode m6502_0d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; ORA;		  */ }}; /* 4 ORA ABS */
    opcode m6502_2d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; AND;		  */ }}; /* 4 AND ABS */
    opcode m6502_4d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; EOR;		  */ }}; /* 4 EOR ABS */
    opcode m6502_6d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; ADC;		  */ }}; /* 4 ADC ABS */
    opcode m6502_8d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4;		 STA; WR_ABS; */ }}; /* 4 STA ABS */
    opcode m6502_ad = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; LDA;		  */ }}; /* 4 LDA ABS */
    opcode m6502_cd = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; CMP;		  */ }}; /* 4 CMP ABS */
    opcode m6502_ed = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; SBC;		  */ }}; /* 4 SBC ABS */

    opcode m6502_1d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; ORA;		  */ }}; /* 4 ORA ABX */
    opcode m6502_3d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; AND;		  */ }}; /* 4 AND ABX */
    opcode m6502_5d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; EOR;		  */ }}; /* 4 EOR ABX */
    opcode m6502_7d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; ADC;		  */ }}; /* 4 ADC ABX */
    opcode m6502_9d = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5;		 STA; WR_ABX; */ }}; /* 5 STA ABX */
    opcode m6502_bd = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; LDA;		  */ }}; /* 4 LDA ABX */
    opcode m6502_dd = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; CMP;		  */ }}; /* 4 CMP ABX */
    opcode m6502_fd = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABX; SBC;		  */ }}; /* 4 SBC ABX */

    opcode m6502_0e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; ASL; WB_EA;  */ }}; /* 6 ASL ABS */
    opcode m6502_2e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; ROL; WB_EA;  */ }}; /* 6 ROL ABS */
    opcode m6502_4e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; LSR; WB_EA;  */ }}; /* 6 LSR ABS */
    opcode m6502_6e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; ROR; WB_EA;  */ }}; /* 6 ROR ABS */
    opcode m6502_8e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 5;		 STX; WR_ABS; */ }}; /* 5 STX ABS */
    opcode m6502_ae = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABS; LDX;		  */ }}; /* 4 LDX ABS */
    opcode m6502_ce = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; DEC; WB_EA;  */ }}; /* 6 DEC ABS */
    opcode m6502_ee = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 6; RD_ABS; INC; WB_EA;  */ }}; /* 6 INC ABS */

    opcode m6502_1e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; ASL; WB_EA;  */ }}; /* 7 ASL ABX */
    opcode m6502_3e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; ROL; WB_EA;  */ }}; /* 7 ROL ABX */
    opcode m6502_5e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; LSR; WB_EA;  */ }}; /* 7 LSR ABX */
    opcode m6502_7e = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; ROR; WB_EA;  */ }}; /* 7 ROR ABX */

    opcode m6502_be = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 4; RD_ABY; LDX;		  */ }}; /* 4 LDX ABY */
    opcode m6502_de = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; DEC; WB_EA;  */ }}; /* 7 DEC ABX */
    opcode m6502_fe = new opcode() { public void handler(){  throw new UnsupportedOperationException("unimplemented"); /* int tmp; m6502_ICount[0] -= 7; RD_ABX; INC; WB_EA;  */ }}; /* 7 INC ABX */

    /*
    *  ILLEGAL Instructions
    */
    opcode m6502_80 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_14 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_34 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_54 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_74 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_02 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_22 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_42 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_62 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_82 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_c2 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_e2 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_12 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_32 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_52 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_72 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_92 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_b2 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_d2 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_f2 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_03 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_23 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_43 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_63 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_83 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_a3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_c3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_e3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_13 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_33 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_53 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_73 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_93 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_b3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_d3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_f3 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_04 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_d4 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_f4 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_44 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_64 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_07 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_27 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_47 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_67 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_87 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_a7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_c7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_e7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_17 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_37 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_57 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_77 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_97 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_b7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_d7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_f7 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_1a = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_3a = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_5a = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_7a = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_89 = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_da = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_fa = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_0b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_2b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_4b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_6b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_8b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_ab = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_cb = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_eb = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_1b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_3b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_5b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_7b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_9b = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_bb = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_db = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_fb = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_0c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_1c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_3c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_5c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_7c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_9c = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_dc = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_fc = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_9e = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		    }}; /* 2 ILL */
    opcode m6502_0f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_2f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_4f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_6f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_8f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_af = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_cf = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_ef = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_1f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_3f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_5f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_7f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_9f = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_bf = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_df = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */
    opcode m6502_ff = new opcode() { public void handler(){  		  m6502_ICount[0] -= 2;		 ILL();		   }}; /* 2 ILL */



    /*TODO*////*****************************************************************************
/*TODO*/// *
/*TODO*/// *		6502 CPU interface functions
/*TODO*/// *
/*TODO*/// *****************************************************************************/
/*TODO*///
    @Override
    public void reset(Object param) {
	m6502.subtype = SUBTYPE_6502;
	m6502.insn = insn6502;

	/* wipe out the rest of the m6502 structure */
	/* read the reset vector into PC */
	m6502.pc.SetL(RDMEM(M6502_RST_VEC));
	m6502.pc.SetH(RDMEM(M6502_RST_VEC+1));

	m6502.sp.SetD(0x01ff);	/* stack pointer starts at page 1 offset FF */
	m6502.p = F_T|F_I|F_Z;	/* set T, I and Z flags */
	m6502.pending_irq = 0;	/* nonzero if an IRQ is pending */
	m6502.after_cli = 0;	/* pending IRQ and last insn cleared I */
	m6502.irq_callback = null;

	change_pc16(m6502.pc.D);
    }
/*TODO*///
/*TODO*///void m6502_exit(void)
/*TODO*///{
/*TODO*///	/* nothing to do yet */
/*TODO*///}
/*TODO*///
/*TODO*///unsigned m6502_get_context (void *dst)
/*TODO*///{
/*TODO*///	if( dst )
/*TODO*///		*(m6502_Regs*)dst = m6502;
/*TODO*///	return sizeof(m6502_Regs);
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_context (void *src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		m6502 = *(m6502_Regs*)src;
/*TODO*///		change_pc(PCD);
/*TODO*///	}
/*TODO*///}
/*TODO*///
    @Override
    public int get_pc() {
       return m6502.pc.D;
    }
/*TODO*///
/*TODO*///void m6502_set_pc (unsigned val)
/*TODO*///{
/*TODO*///	PCW = val;
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*///unsigned m6502_get_sp (void)
/*TODO*///{
/*TODO*///	return S;
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_sp (unsigned val)
/*TODO*///{
/*TODO*///	S = val;
/*TODO*///}
/*TODO*///
    @Override
    public Object init_context() {
       Object reg = new m6502_Regs();
       return reg;
    }
/*TODO*///unsigned m6502_get_reg (int regnum)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6502_PC: return m6502.pc.w.l;
/*TODO*///		case M6502_S: return m6502.sp.b.l;
/*TODO*///		case M6502_P: return m6502.p;
/*TODO*///		case M6502_A: return m6502.a;
/*TODO*///		case M6502_X: return m6502.x;
/*TODO*///		case M6502_Y: return m6502.y;
/*TODO*///		case M6502_EA: return m6502.ea.w.l;
/*TODO*///		case M6502_ZP: return m6502.zp.w.l;
/*TODO*///		case M6502_NMI_STATE: return m6502.nmi_state;
/*TODO*///		case M6502_IRQ_STATE: return m6502.irq_state;
/*TODO*///		case M6502_SO_STATE: return m6502.so_state;
/*TODO*///		case M6502_SUBTYPE: return m6502.subtype;
/*TODO*///		case REG_PREVIOUSPC: return m6502.ppc.w.l;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///					return RDMEM( offset ) | ( RDMEM( offset + 1 ) << 8 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_reg (int regnum, unsigned val)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6502_PC: m6502.pc.w.l = val; break;
/*TODO*///		case M6502_S: m6502.sp.b.l = val; break;
/*TODO*///		case M6502_P: m6502.p = val; break;
/*TODO*///		case M6502_A: m6502.a = val; break;
/*TODO*///		case M6502_X: m6502.x = val; break;
/*TODO*///		case M6502_Y: m6502.y = val; break;
/*TODO*///		case M6502_EA: m6502.ea.w.l = val; break;
/*TODO*///		case M6502_ZP: m6502.zp.w.l = val; break;
/*TODO*///		case M6502_NMI_STATE: m6502_set_nmi_line( val ); break;
/*TODO*///		case M6502_IRQ_STATE: m6502_set_irq_line( 0, val ); break;
/*TODO*///		case M6502_SO_STATE: m6502_set_irq_line( M6502_SET_OVERFLOW, val ); break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///				{
/*TODO*///					WRMEM( offset, val & 0xfff );
/*TODO*///					WRMEM( offset + 1, (val >> 8) & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public void take_irq()
    {
        throw new UnsupportedOperationException("Not supported yet.");
/*TODO*///	if( !(P & F_I) )
/*TODO*///	{
/*TODO*///		EAD = M6502_IRQ_VEC;
/*TODO*///		m6502_ICount[0] -= 7;
/*TODO*///		PUSH(PCH);
/*TODO*///		PUSH(PCL);
/*TODO*///		PUSH(P & ~F_B);
/*TODO*///		P = (P & ~F_D) | F_I;		/* knock out D and set I flag */
/*TODO*///		PCL = RDMEM(EAD);
/*TODO*///		PCH = RDMEM(EAD+1);
/*TODO*///		LOG((errorlog,"M6502#%d takes IRQ ($%04x)\n", cpu_getactivecpu(), PCD));
/*TODO*///		/* call back the cpuintrf to let it clear the line */
/*TODO*///		if (m6502.irq_callback) (*m6502.irq_callback)(0);
/*TODO*///		change_pc16(PCD);
/*TODO*///	}
/*TODO*///	m6502.pending_irq = 0;
    }

    @Override
    public int execute(int cycles) {
      
    	m6502_ICount[0] = cycles;
    
    	change_pc16(m6502.pc.D);
    
    	do
    	{
    		int /*UINT8*/ op;
                m6502.ppc.SetD(m6502.pc.D);
    
    		//CALL_MAME_DEBUG;
    
    		op = RDOP();
    		/* if an irq is pending, take it now */
    		if( m6502.pending_irq!=0 && op == 0x78 )
    			take_irq();
    
    		m6502.insn[op].handler();
    
    		/* check if the I flag was just reset (interrupts enabled) */
    		if( m6502.after_cli!=0 )
    		{
    			if(errorlog!=null) fprintf(errorlog,"M6502#%d after_cli was >0", cpu_getactivecpu());
    			m6502.after_cli = 0;
    			if (m6502.irq_state != CLEAR_LINE)
    			{
    				if(errorlog!=null) fprintf(errorlog,": irq line is asserted: set pending IRQ\n");
    				m6502.pending_irq = 1;
    			}
    			else
    			{
    				if(errorlog!=null) fprintf(errorlog,": irq line is clear\n");
    			}
    		}
    		else
    		if( m6502.pending_irq!=0 )
    			take_irq();
    
    	} while (m6502_ICount[0] > 0);
    
    	return cycles - m6502_ICount[0];
    }
/*TODO*///
/*TODO*///void m6502_set_nmi_line(int state)
/*TODO*///{
/*TODO*///	if (m6502.nmi_state == state) return;
/*TODO*///	m6502.nmi_state = state;
/*TODO*///	if( state != CLEAR_LINE )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "M6502#%d set_nmi_line(ASSERT)\n", cpu_getactivecpu()));
/*TODO*///		EAD = M6502_NMI_VEC;
/*TODO*///		m6502_ICount[0] -= 7;
/*TODO*///		PUSH(PCH);
/*TODO*///		PUSH(PCL);
/*TODO*///		PUSH(P & ~F_B);
/*TODO*///		P = (P & ~F_D) | F_I;		/* knock out D and set I flag */
/*TODO*///		PCL = RDMEM(EAD);
/*TODO*///		PCH = RDMEM(EAD+1);
/*TODO*///		LOG((errorlog,"M6502#%d takes NMI ($%04x)\n", cpu_getactivecpu(), PCD));
/*TODO*///		change_pc16(PCD);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_irq_line(int irqline, int state)
/*TODO*///{
/*TODO*///	if( irqline == M6502_SET_OVERFLOW )
/*TODO*///	{
/*TODO*///		if( m6502.so_state && !state )
/*TODO*///		{
/*TODO*///			LOG((errorlog, "M6502#%d set overflow\n", cpu_getactivecpu()));
/*TODO*///			P|=F_V;
/*TODO*///		}
/*TODO*///		m6502.so_state=state;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m6502.irq_state = state;
/*TODO*///	if( state != CLEAR_LINE )
/*TODO*///	{
/*TODO*///		LOG((errorlog, "M6502#%d set_irq_line(ASSERT)\n", cpu_getactivecpu()));
/*TODO*///		m6502.pending_irq = 1;
/*TODO*///	}
/*TODO*///}
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
         m6502.irq_callback = callback;
    }
/*TODO*///
/*TODO*///void m6502_state_save(void *file)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"TYPE",&m6502.subtype,1);
/*TODO*///	/* insn is set at restore since it's a pointer */
/*TODO*///	state_save_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
/*TODO*///	state_save_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_state_load(void *file)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"TYPE",&m6502.subtype,1);
/*TODO*///	/* insn is set at restore since it's a pointer */
/*TODO*///	switch (m6502.subtype)
/*TODO*///	{
/*TODO*///#if HAS_M65C02
/*TODO*///		case SUBTYPE_65C02:
/*TODO*///			m6502.insn = insn65c02;
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///#if HAS_M65SC02
/*TODO*///		case SUBTYPE_65SC02:
/*TODO*///			m6502.insn = insn65sc02;
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///#if HAS_M6510
/*TODO*///		case SUBTYPE_6510:
/*TODO*///			m6502.insn = insn6510;
/*TODO*///			break;
/*TODO*///#endif
/*TODO*///		default:
/*TODO*///			m6502.insn = insn6502;
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	state_load_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
/*TODO*///	state_load_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return a formatted string for a register
/*TODO*/// ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) {

        
/*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	m6502_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &m6502;
/*TODO*///
	switch( regnum )
	{
/*TODO*///		case CPU_INFO_REG+M6502_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_S: sprintf(buffer[which], "S:%02X", r->sp.b.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_P: sprintf(buffer[which], "P:%02X", r->p); break;
/*TODO*///		case CPU_INFO_REG+M6502_A: sprintf(buffer[which], "A:%02X", r->a); break;
/*TODO*///		case CPU_INFO_REG+M6502_X: sprintf(buffer[which], "X:%02X", r->x); break;
/*TODO*///		case CPU_INFO_REG+M6502_Y: sprintf(buffer[which], "Y:%02X", r->y); break;
/*TODO*///		case CPU_INFO_REG+M6502_EA: sprintf(buffer[which], "EA:%04X", r->ea.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_ZP: sprintf(buffer[which], "ZP:%03X", r->zp.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+M6502_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+M6502_SO_STATE: sprintf(buffer[which], "SO:%X", r->so_state); break;
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->p & 0x80 ? 'N':'.',
/*TODO*///				r->p & 0x40 ? 'V':'.',
/*TODO*///				r->p & 0x20 ? 'R':'.',
/*TODO*///				r->p & 0x10 ? 'B':'.',
/*TODO*///				r->p & 0x08 ? 'D':'.',
/*TODO*///				r->p & 0x04 ? 'I':'.',
/*TODO*///				r->p & 0x02 ? 'Z':'.',
/*TODO*///				r->p & 0x01 ? 'C':'.');
/*TODO*///			break;
		case CPU_INFO_NAME: return "M6502";
		case CPU_INFO_FAMILY: return "Motorola 6502";
		case CPU_INFO_VERSION: return "1.2";
		case CPU_INFO_FILE: return "m6502.java";
		case CPU_INFO_CREDITS: return "Copyright (c) 1998 Juergen Buchmueller, all rights reserved.";
/*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)m6502_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)m6502_win_layout;
	}
/*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("unsupported m6502 cpu_info");
    }
/*TODO*///
/*TODO*///unsigned m6502_dasm(char *buffer, unsigned pc)
/*TODO*///{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	return Dasm6502( buffer, pc );
/*TODO*///#else
/*TODO*///	sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///	return 1;
/*TODO*///#endif
/*TODO*///}

    public burnPtr burn_function = new burnPtr() {
        public void handler(int cycles) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };




    @Override
    public void set_op_base(int pc) 
    {
        cpu_setOPbase16.handler(pc,0);
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
    public abstract interface opcode
    {
        public abstract void handler();
    }
}
