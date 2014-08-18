/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.konami;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.konami.konamiH.*;
import static mame.memory.*;


public class konami extends cpu_interface{
    public int[] konami_ICount = new int[1];
    public konami()
    {
        cpu_num = CPU_KONAMI;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = KONAMI_INT_NONE;
        irq_int = KONAMI_INT_IRQ;
        nmi_int = KONAMI_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;

        icount = konami_ICount;
        konami_ICount[0] = 50000;
          
        //intialize interfaces
        burn = burn_function;
    }
    opcode[] konami_main = new opcode[256];
    
    /* Konami Registers */
    public static class konami_Regs
    {
        public /*PAIR*/ int pc; 		/* Program counter */
        public /*PAIR*/ int ppc;		/* Previous program counter */
        public int a;
        public int b;   //PAIR	d;		/* Accumulator a and b */
        public /*PAIR*/ int dp; 		/* Direct Page register (page in MSB) */
        public int u;
        public int s;//PAIR	u, s;		/* Stack pointers */
        public int x;
        public int y;//PAIR	x, y;		/* Index registers */
        public int /*UINT8*/   cc;
        public int /*UINT8*/   ireg;		/* First opcode */
        public int[] /*UINT8*/   irq_state=new int[2];
        public    int     extra_cycles; /* cycles used up by interrupts */
        public irqcallbacksPtr irq_callback;
        public int /*UINT8*/   int_state;  /* SYNC and CWAI flags */
        public int /*UINT8*/   nmi_state;
    }

    /* flag bits in the cc register */
    public static final int CC_C  =  0x01;        /* Carry */
    public static final int CC_V  =  0x02;        /* Overflow */
    public static final int CC_Z  =  0x04;        /* Zero */
    public static final int CC_N  =  0x08;        /* Negative */
    public static final int CC_II =  0x10;        /* Inhibit IRQ */
    public static final int CC_H  =  0x20;        /* Half (auxiliary) carry */
    public static final int CC_IF =  0x40;        /* Inhibit FIRQ */
    public static final int CC_E  =  0x80;        /* entire state pushed */
    
    /* Konami registers */
    private static konami_Regs konami=new konami_Regs();

/*TODO*///#define	pPPC    konami.ppc
/*TODO*///#define pPC 	konami.pc
/*TODO*///#define pU		konami.u
/*TODO*///#define pS		konami.s
/*TODO*///#define pX		konami.x
/*TODO*///#define pY		konami.y
/*TODO*///#define pD		konami.d
/*TODO*///
/*TODO*///#define	PPC		konami.ppc.w.l
/*TODO*///#define PC  	konami.pc.w.l
/*TODO*///#define PCD 	konami.pc.d
/*TODO*///#define U		konami.u.w.l
/*TODO*///#define UD		konami.u.d
/*TODO*///#define S		konami.s.w.l
/*TODO*///#define SD		konami.s.d
/*TODO*///#define X		konami.x.w.l
/*TODO*///#define XD		konami.x.d
/*TODO*///#define Y		konami.y.w.l
/*TODO*///#define YD		konami.y.d
/*TODO*///#define D   	konami.d.w.l
/*TODO*///#define A   	konami.d.b.h
/*TODO*///#define B		konami.d.b.l
/*TODO*///#define DP		konami.dp.b.h
/*TODO*///#define DPD 	konami.dp.d
/*TODO*///#define CC  	konami.cc
/*TODO*///
/*TODO*///static PAIR ea;         /* effective address */
/*TODO*///#define EA	ea.w.l
/*TODO*///#define EAD ea.d
/*TODO*///
/*TODO*///#define KONAMI_CWAI		8	/* set when CWAI is waiting for an interrupt */
/*TODO*///#define KONAMI_SYNC		16	/* set when SYNC is waiting for an interrupt */
/*TODO*///#define KONAMI_LDS		32	/* set when LDS occured at least once */
/*TODO*///
    public static final int KONAMI_CWAI		=8;	/* set when CWAI is waiting for an interrupt */
    public static final int KONAMI_SYNC		=16;	/* set when SYNC is waiting for an interrupt */
    public static final int KONAMI_LDS		=32;	/* set when LDS occured at least once */
/*TODO*///#define CHECK_IRQ_LINES 												\
/*TODO*///	if( konami.irq_state[KONAMI_IRQ_LINE] != CLEAR_LINE ||				\
/*TODO*///		konami.irq_state[KONAMI_FIRQ_LINE] != CLEAR_LINE )				\
/*TODO*///		konami.int_state &= ~KONAMI_SYNC; /* clear SYNC flag */			\
/*TODO*///	if( konami.irq_state[KONAMI_FIRQ_LINE]!=CLEAR_LINE && !(CC & CC_IF) ) \
/*TODO*///	{																	\
/*TODO*///		/* fast IRQ */													\
/*TODO*///		/* state already saved by CWAI? */								\
/*TODO*///		if( konami.int_state & KONAMI_CWAI )							\
/*TODO*///		{																\
/*TODO*///			konami.int_state &= ~KONAMI_CWAI;  /* clear CWAI */			\
/*TODO*///			konami.extra_cycles += 7;		 /* subtract +7 cycles */	\
/*TODO*///        }                                                               \
/*TODO*///		else															\
/*TODO*///		{																\
/*TODO*///			CC &= ~CC_E;				/* save 'short' state */        \
/*TODO*///			PUSHWORD(pPC);												\
/*TODO*///			PUSHBYTE(CC);												\
/*TODO*///			konami.extra_cycles += 10;	/* subtract +10 cycles */		\
/*TODO*///		}																\
/*TODO*///		CC |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */		\
/*TODO*///		PCD = RM16(0xfff6); 											\
/*TODO*///		change_pc(PC);					/* TS 971002 */ 				\
/*TODO*///		(void)(*konami.irq_callback)(KONAMI_FIRQ_LINE);					\
/*TODO*///	}																	\
/*TODO*///	else																\
/*TODO*///	if( konami.irq_state[KONAMI_IRQ_LINE]!=CLEAR_LINE && !(CC & CC_II) )\
/*TODO*///	{																	\
/*TODO*///		/* standard IRQ */												\
/*TODO*///		/* state already saved by CWAI? */								\
/*TODO*///		if( konami.int_state & KONAMI_CWAI )							\
/*TODO*///		{																\
/*TODO*///			konami.int_state &= ~KONAMI_CWAI;  /* clear CWAI flag */	\
/*TODO*///			konami.extra_cycles += 7;		 /* subtract +7 cycles */	\
/*TODO*///		}																\
/*TODO*///		else															\
/*TODO*///		{																\
/*TODO*///			CC |= CC_E; 				/* save entire state */ 		\
/*TODO*///			PUSHWORD(pPC);												\
/*TODO*///			PUSHWORD(pU);												\
/*TODO*///			PUSHWORD(pY);												\
/*TODO*///			PUSHWORD(pX);												\
/*TODO*///			PUSHBYTE(DP);												\
/*TODO*///			PUSHBYTE(B);												\
/*TODO*///			PUSHBYTE(A);												\
/*TODO*///			PUSHBYTE(CC);												\
/*TODO*///			konami.extra_cycles += 19;	 /* subtract +19 cycles */		\
/*TODO*///		}																\
/*TODO*///		CC |= CC_II;					/* inhibit IRQ */				\
/*TODO*///		PCD = RM16(0xfff8); 											\
/*TODO*///		change_pc(PC);					/* TS 971002 */ 				\
/*TODO*///		(void)(*konami.irq_callback)(KONAMI_IRQ_LINE);					\
/*TODO*///	}
/*TODO*///

/*TODO*///int konami_Flags;	/* flags for speed optimization (obsolete!!) */
/*TODO*///void (*konami_cpu_setlines_callback)( int lines ) = 0; /* callback called when A16-A23 are set */
/*TODO*///
    /* these are re-defined in konami.h TO RAM, ROM or functions in memory.c */
    public int RM(int addr)
    {
        return (cpu_readmem16(addr) & 0xFF);
    }
    public void WM(int addr,int value)
    {
        cpu_writemem16(addr,value);
    }
    public char ROP(int addr)
    {
    	return cpu_readop(addr);
    }
    public char ROP_ARG(int addr)
    {
    	return cpu_readop_arg(addr);
    }
/*TODO*///
/*TODO*///#define SIGNED(a)	(UINT16)(INT16)(INT8)(a)
/*TODO*///
    //* macros to access memory */
    public int IMMBYTE()
    {
        int reg = ROP_ARG(konami.pc); 
        konami.pc = konami.pc + 1 & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
    }
    public int IMMWORD()
    {
        int reg = (ROP_ARG(konami.pc)<<8) | ROP_ARG((konami.pc+1)&0xffff);
        konami.pc = konami.pc + 2 & 0xFFFF;
        return reg;
    }
    public void PUSHBYTE(int w)
    {
        konami.s = konami.s -1 & 0xFFFF; 
        WM(konami.s,w);
    }
    public void PUSHWORD(int w)
    {
        konami.s = konami.s -1 & 0xFFFF; 
        WM(konami.s,w & 0xFF); 
        konami.s = konami.s -1 & 0xFFFF;
        WM(konami.s,w >>8);
    }
    public int PULLBYTE()
    {
        int b = RM(konami.s);
        konami.s = konami.s +1 & 0xFFFF;
        return b;
    }
    public int PULLWORD()//TODO recheck
    {
        int w = RM(konami.s)<<8;
        konami.s = konami.s +1 & 0xFFFF; 
        w |= RM(konami.s);
        konami.s = konami.s +1 & 0xFFFF; 
        return w;
    }
    public void PSHUBYTE(int w)
    {
        konami.u = konami.u -1 & 0xFFFF; 
        WM(konami.u,w);
    }
    public void PSHUWORD(int w)
    {
        konami.u = konami.u -1 & 0xFFFF; 
        WM(konami.u,w & 0xFF); 
        konami.u = konami.u -1 & 0xFFFF;
        WM(konami.u,w >>8);
    }
    public int PULUBYTE()
    {
        int b = RM(konami.u);
        konami.u = konami.u +1 & 0xFFFF;
        return b;
    }
    public int PULUWORD()//TODO recheck
    {
        int w = RM(konami.u)<<8;
        konami.u = konami.u +1 & 0xFFFF; 
        w |= RM(konami.u);
        konami.u = konami.u +1 & 0xFFFF; 
        return w;
    }
    
    public void CLR_HNZVC()    {konami.cc&=~(CC_H|CC_N|CC_Z|CC_V|CC_C);}
    public void CLR_NZV() { 	konami.cc&=~(CC_N|CC_Z|CC_V); }
    /*TODO*///#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
    public void CLR_NZVC()	{ konami.cc &=~(CC_N|CC_Z|CC_V|CC_C); }
    public void CLR_Z()		{ konami.cc &=~(CC_Z); }
    public void CLR_NZC() 	{ konami.cc&=~(CC_N|CC_Z|CC_C);}
    public void CLR_ZC()	{ konami.cc&=~(CC_Z|CC_C); }

    /* macros for CC -- CC bits affected should be reset before calling */
    public void SET_Z(int a)		{if(a==0) SEZ();}
    public void SET_Z8(int a)		{SET_Z(a&0xFF);}
    public void SET_Z16(int a)		{SET_Z(a&0xFFFF);}
    public void SET_N8(int a)		{konami.cc |=((a&0x80)>>4);}
    public void SET_N16(int a)		{konami.cc|=((a&0x8000)>>12);}
    public void SET_H(int a,int b,int r){konami.cc|=(((a^b^r)&0x10)<<1);}
    public void SET_C8(int a)		{konami.cc|=((a&0x100)>>8);}
    public void SET_C16(int a)		{konami.cc|=((a&0x10000)>>16);}
    public void SET_V8(int a,int b,int r){konami.cc|=(((a^b^r^(r>>1))&0x80)>>6);}
    public void SET_V16(int a,int b,int r){konami.cc|=(((a^b^r^(r>>1))&0x8000)>>14);}
    
    static int flags8i[]=	 /* increment */
    {
    CC_Z,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    CC_N|CC_V,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N
    };
    static int flags8d[]= /* decrement */
    {
    CC_Z,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,CC_V,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N
    };
    public void SET_FLAGS8I(int a)	{konami.cc|=flags8i[(a)&0xff];}
    public void SET_FLAGS8D(int a)	{konami.cc |=flags8d[(a)&0xff];}
    
    /* combos */
    public void SET_NZ8(int a)			{SET_N8(a);SET_Z(a);}
    public void SET_NZ16(int a)			{SET_N16(a);SET_Z(a);}
    public void SET_FLAGS8(int a,int b,int r)	{SET_N8(r);SET_Z8(r);SET_V8(a,b,r);SET_C8(r);}
    public void SET_FLAGS16(int a,int b,int r)	{SET_N16(r);SET_Z16(r);SET_V16(a,b,r);SET_C16(r);}

/*TODO*////* macros for addressing modes (postbytes have their own code) */
/*TODO*///#define DIRECT	EAD = DPD; IMMBYTE(ea.b.l)
/*TODO*///#define IMM8	EAD = PCD; PC++
/*TODO*///#define IMM16	EAD = PCD; PC+=2
/*TODO*///#define EXTENDED IMMWORD(ea)
/*TODO*///
    /*TODO*////* macros to set status flags */
    public void  SEC() {konami.cc|=CC_C;}
    /*TODO*///#define CLC CC&=~CC_C
    public void  SEZ() { konami.cc |=CC_Z;  }
    /*TODO*///#define CLZ CC&=~CC_Z
    /*TODO*///#define SEN CC|=CC_N
    /*TODO*///#define CLN CC&=~CC_N
    /*TODO*///#define SEV CC|=CC_V
    /*TODO*///#define CLV CC&=~CC_V
    /*TODO*///#define SEH CC|=CC_H
    /*TODO*///#define CLH CC&=~CC_H
/*TODO*///
/*TODO*////* macros for convenience */
/*TODO*///#define DIRBYTE(b) DIRECT; b=RM(EAD)
/*TODO*///#define DIRWORD(w) DIRECT; w.d=RM16(EAD)
/*TODO*///#define EXTBYTE(b) EXTENDED; b=RM(EAD)
/*TODO*///#define EXTWORD(w) EXTENDED; w.d=RM16(EAD)
/*TODO*///
/*TODO*////* macros for branch instructions */
/*TODO*///#define BRANCH(f) { 					\
/*TODO*///	UINT8 t;							\
/*TODO*///	IMMBYTE(t); 						\
/*TODO*///	if( f ) 							\
/*TODO*///	{									\
/*TODO*///		PC += SIGNED(t);				\
/*TODO*///		change_pc(PC);	/* TS 971002 */ \
/*TODO*///	}									\
/*TODO*///}
/*TODO*///
/*TODO*///#define LBRANCH(f) {                    \
/*TODO*///	PAIR t; 							\
/*TODO*///	IMMWORD(t); 						\
/*TODO*///	if( f ) 							\
/*TODO*///	{									\
/*TODO*///		konami_ICount -= 1;				\
/*TODO*///		PC += t.w.l;					\
/*TODO*///		change_pc(PC);	/* TS 971002 */ \
/*TODO*///	}									\
/*TODO*///}
/*TODO*///
/*TODO*///#define NXORV  ((CC&CC_N)/*TODO*///((CC&CC_V)<<2))
/*TODO*///
/*TODO*////* macros for setting/getting registers in TFR/EXG instructions */
/*TODO*///#define GETREG(val,reg) 				\
/*TODO*///	switch(reg) {						\
/*TODO*///	case 0: val = A;	break;			\
/*TODO*///	case 1: val = B; 	break; 			\
/*TODO*///	case 2: val = X; 	break;			\
/*TODO*///	case 3: val = Y;	break; 			\
/*TODO*///	case 4: val = S; 	break; /* ? */	\
/*TODO*///	case 5: val = U;	break;			\
/*TODO*///	default: val = 0xff; if ( errorlog ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", PC ); break; \
/*TODO*///}
/*TODO*///
/*TODO*///#define SETREG(val,reg) 				\
/*TODO*///	switch(reg) {						\
/*TODO*///	case 0: A = val;	break;			\
/*TODO*///	case 1: B = val;	break;			\
/*TODO*///	case 2: X = val; 	break;			\
/*TODO*///	case 3: Y = val;	break;			\
/*TODO*///	case 4: S = val;	break; /* ? */	\
/*TODO*///	case 5: U = val; 	break;			\
/*TODO*///	default: if ( errorlog ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", PC ); break; \
/*TODO*///}
/*TODO*///
    /* opcode timings */
    static int cycles1[] =
    {
            /*	 0	1  2  3  4	5  6  7  8	9  A  B  C	D  E  F */
      /*0*/  1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 5, 5, 5, 5,
      /*1*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      /*2*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      /*3*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 7, 6,
      /*4*/  3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 4, 4,
      /*5*/  4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 1, 1, 1,
      /*6*/  3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
      /*7*/  3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
      /*8*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5,
      /*9*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 6,
      /*A*/  2, 2, 2, 4, 4, 4, 4, 4, 2, 2, 2, 2, 3, 3, 2, 1,
      /*B*/  3, 2, 2,11,22,11, 2, 4, 3, 3, 3, 3, 3, 3, 3, 3,
      /*C*/  3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3, 2,
      /*D*/  2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      /*E*/  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      /*F*/  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    int RM16(int addr)
    {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i;
    }
    void WM16(int addr,int reg)
    {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }
    @Override
    public Object init_context() {
       Object reg = new konami_Regs();
       return reg;
    }
/*TODO*////****************************************************************************
/*TODO*/// * Get all registers in given buffer
/*TODO*/// ****************************************************************************/
/*TODO*///unsigned konami_get_context(void *dst)
/*TODO*///{
/*TODO*///	if( dst )
/*TODO*///		*(konami_Regs*)dst = konami;
/*TODO*///	return sizeof(konami_Regs);
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set all registers to given values
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_context(void *src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///		konami = *(konami_Regs*)src;
/*TODO*///    change_pc(PC);    /* TS 971002 */
/*TODO*///
/*TODO*///    CHECK_IRQ_LINES;
/*TODO*///}

    /****************************************************************************
     * Return program counter
     ****************************************************************************/
    @Override
    public int get_pc() {
        return konami.pc & 0xFFFF;
    }


/*TODO*////****************************************************************************
/*TODO*/// * Set program counter
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_pc(unsigned val)
/*TODO*///{
/*TODO*///	PC = val;
/*TODO*///	change_pc(PC);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return stack pointer
/*TODO*/// ****************************************************************************/
/*TODO*///unsigned konami_get_sp(void)
/*TODO*///{
/*TODO*///	return S;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set stack pointer
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_sp(unsigned val)
/*TODO*///{
/*TODO*///	S = val;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Return a specific register                                               */
/*TODO*////****************************************************************************/
/*TODO*///unsigned konami_get_reg(int regnum)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case KONAMI_PC: return PC;
/*TODO*///		case KONAMI_S: return S;
/*TODO*///		case KONAMI_CC: return CC;
/*TODO*///		case KONAMI_U: return U;
/*TODO*///		case KONAMI_A: return A;
/*TODO*///		case KONAMI_B: return B;
/*TODO*///		case KONAMI_X: return X;
/*TODO*///		case KONAMI_Y: return Y;
/*TODO*///		case KONAMI_DP: return DP;
/*TODO*///		case KONAMI_NMI_STATE: return konami.nmi_state;
/*TODO*///		case KONAMI_IRQ_STATE: return konami.irq_state[KONAMI_IRQ_LINE];
/*TODO*///		case KONAMI_FIRQ_STATE: return konami.irq_state[KONAMI_FIRQ_LINE];
/*TODO*///		case REG_PREVIOUSPC: return PPC;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///					return ( RM( offset ) << 8 ) | RM( offset + 1 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Set a specific register                                                  */
/*TODO*////****************************************************************************/
/*TODO*///void konami_set_reg(int regnum, unsigned val)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case KONAMI_PC: PC = val; change_pc(PC); break;
/*TODO*///		case KONAMI_S: S = val; break;
/*TODO*///		case KONAMI_CC: CC = val; CHECK_IRQ_LINES; break;
/*TODO*///		case KONAMI_U: U = val; break;
/*TODO*///		case KONAMI_A: A = val; break;
/*TODO*///		case KONAMI_B: B = val; break;
/*TODO*///		case KONAMI_X: X = val; break;
/*TODO*///		case KONAMI_Y: Y = val; break;
/*TODO*///		case KONAMI_DP: DP = val; break;
/*TODO*///		case KONAMI_NMI_STATE: konami.nmi_state = val; break;
/*TODO*///		case KONAMI_IRQ_STATE: konami.irq_state[KONAMI_IRQ_LINE] = val; break;
/*TODO*///		case KONAMI_FIRQ_STATE: konami.irq_state[KONAMI_FIRQ_LINE] = val; break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///				{
/*TODO*///					WM( offset, (val >> 8) & 0xff );
/*TODO*///					WM( offset+1, val & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
    /****************************************************************************/
    /* Reset registers to their initial values									*/
    /****************************************************************************/
    @Override
    public void reset(Object param) {
    	konami.int_state = 0;
    	konami.nmi_state = CLEAR_LINE;
    	konami.irq_state[0] = CLEAR_LINE;
    	konami.irq_state[0] = CLEAR_LINE;
    
    	konami.dp = 0;			/* Reset direct page register */
    
        konami.cc |= CC_II;        /* IRQ disabled */
        konami.cc |= CC_IF;        /* FIRQ disabled */
    
        konami.pc = RM16(0xfffe);
    	change_pc(konami.pc);
    }
/*TODO*///void konami_exit(void)
/*TODO*///{
/*TODO*///	/* just make sure we deinit this, so the next game set its own */
/*TODO*///	konami_cpu_setlines_callback = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Generate interrupts */
/*TODO*////****************************************************************************
/*TODO*/// * Set NMI line state
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_nmi_line(int state)
/*TODO*///{
/*TODO*///	if (konami.nmi_state == state) return;
/*TODO*///	konami.nmi_state = state;
/*TODO*///	LOG((errorlog, "KONAMI#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
/*TODO*///	if( state == CLEAR_LINE ) return;
/*TODO*///
/*TODO*///	/* if the stack was not yet initialized */
/*TODO*///    if( !(konami.int_state & KONAMI_LDS) ) return;
/*TODO*///
/*TODO*///    konami.int_state &= ~KONAMI_SYNC;
/*TODO*///	/* state already saved by CWAI? */
/*TODO*///	if( konami.int_state & KONAMI_CWAI )
/*TODO*///	{
/*TODO*///		konami.int_state &= ~KONAMI_CWAI;
/*TODO*///		konami.extra_cycles += 7;	/* subtract +7 cycles next time */
/*TODO*///    }
/*TODO*///	else
/*TODO*///	{
/*TODO*///		CC |= CC_E; 				/* save entire state */
/*TODO*///		PUSHWORD(pPC);
/*TODO*///		PUSHWORD(pU);
/*TODO*///		PUSHWORD(pY);
/*TODO*///		PUSHWORD(pX);
/*TODO*///		PUSHBYTE(DP);
/*TODO*///		PUSHBYTE(B);
/*TODO*///		PUSHBYTE(A);
/*TODO*///		PUSHBYTE(CC);
/*TODO*///		konami.extra_cycles += 19;	/* subtract +19 cycles next time */
/*TODO*///	}
/*TODO*///	CC |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
/*TODO*///	PCD = RM16(0xfffc);
/*TODO*///	change_pc(PC);					/* TS 971002 */
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set IRQ line state
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_irq_line(int irqline, int state)
/*TODO*///{
/*TODO*///    LOG((errorlog, "KONAMI#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, state));
/*TODO*///	konami.irq_state[irqline] = state;
/*TODO*///	if (state == CLEAR_LINE) return;
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///}

    /****************************************************************************
     * Set IRQ vector callback
     ****************************************************************************/
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        konami.irq_callback = callback;
    }
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Save CPU state
/*TODO*/// ****************************************************************************/
/*TODO*///static void state_save(void *file, const char *module)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file, module, cpu, "PC", &PC, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "U", &U, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "S", &S, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "X", &X, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "Y", &Y, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "DP", &DP, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "CC", &CC, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "INT", &konami.int_state, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "NMI", &konami.nmi_state, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "IRQ", &konami.irq_state[0], 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "FIRQ", &konami.irq_state[1], 1);
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Load CPU state
/*TODO*/// ****************************************************************************/
/*TODO*///static void state_load(void *file, const char *module)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file, module, cpu, "PC", &PC, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "U", &U, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "S", &S, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "X", &X, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "Y", &Y, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "DP", &DP, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "CC", &CC, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "INT", &konami.int_state, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "NMI", &konami.nmi_state, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "IRQ", &konami.irq_state[0], 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "FIRQ", &konami.irq_state[1], 1);
/*TODO*///}
/*TODO*///
/*TODO*///void konami_state_save(void *file) { state_save(file, "konami"); }
/*TODO*///void konami_state_load(void *file) { state_load(file, "konami"); }
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return a formatted string for a register
/*TODO*/// ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) {
/*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	konami_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &konami;
/*TODO*///
	switch( regnum )
	{
		case CPU_INFO_NAME: return "KONAMI";
		case CPU_INFO_FAMILY: return "KONAMI 5000x";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_FILE: return "konami.java";
		case CPU_INFO_CREDITS: return "Copyright (C) The MAME Team 1999";
/*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)konami_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)konami_win_layout;
/*TODO*///
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->cc & 0x80 ? 'E':'.',
/*TODO*///				r->cc & 0x40 ? 'F':'.',
/*TODO*///                r->cc & 0x20 ? 'H':'.',
/*TODO*///                r->cc & 0x10 ? 'I':'.',
/*TODO*///                r->cc & 0x08 ? 'N':'.',
/*TODO*///                r->cc & 0x04 ? 'Z':'.',
/*TODO*///                r->cc & 0x02 ? 'V':'.',
/*TODO*///                r->cc & 0x01 ? 'C':'.');
/*TODO*///            break;
/*TODO*///		case CPU_INFO_REG+KONAMI_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_U: sprintf(buffer[which], "U:%04X", r->u.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_A: sprintf(buffer[which], "A:%02X", r->d.b.h); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_B: sprintf(buffer[which], "B:%02X", r->d.b.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_Y: sprintf(buffer[which], "Y:%04X", r->y.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_DP: sprintf(buffer[which], "DP:%02X", r->dp.b.h); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[KONAMI_IRQ_LINE]); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r->irq_state[KONAMI_FIRQ_LINE]); break;
	}
                throw new UnsupportedOperationException("unsupported konami cpu_info");
/*TODO*///	return buffer[which];
    }
/*TODO*///
/*TODO*///unsigned konami_dasm(char *buffer, unsigned pc)
/*TODO*///{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///    return Dasmknmi(buffer,pc);
/*TODO*///#else
/*TODO*///	sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///	return 1;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* includes the static function prototypes and the master opcode table */
/*TODO*///#include "konamtbl.c"
/*TODO*///
/*TODO*////* includes the actual opcode implementations */
/*TODO*///#include "konamops.c"
/*TODO*///
    /* execute instructions on this CPU until icount expires */
    @Override
    public int execute(int cycles) {
        konami_ICount[0] = cycles - konami.extra_cycles;
    	konami.extra_cycles = 0;
    
    	if ((konami.int_state & (KONAMI_CWAI | KONAMI_SYNC))!=0)
    	{
    		konami_ICount[0] = 0;
    	}
    	else
    	{
    		do
    		{
    			konami.ppc = konami.pc;
    
    			konami.ireg = ROP(konami.pc);
    			konami.pc = konami.pc + 1 & 0xFFFF;
                        
                        if(konami_main[konami.ireg]!=null)
                        {
                            konami_main[konami.ireg].handler();
                        }
                        else
                        {
                            System.out.println("Unsupported konami instruction " + Integer.toHexString(konami.ireg));
                        }

                    konami_ICount[0] -= cycles1[konami.ireg];                       
                } while( konami_ICount[0] > 0 );
    
            konami_ICount[0] -= konami.extra_cycles;
            konami.extra_cycles = 0;
        }
    
        return cycles - konami_ICount[0];
    }
 
    
       
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
     throw new UnsupportedOperationException("Not supported yet.");
    }};





    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc, 0);
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
