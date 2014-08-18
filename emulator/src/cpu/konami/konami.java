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
        Setup_konamiMain_Tables();
        Setup_konamiIndexed_Tables();
        Setup_konamiDirect_Tables();
        Setup_konamiExtended_Tables();
        
    }
    opcode[] konami_main    = new opcode[256];
    opcode[] konami_indexed = new opcode[256];
    opcode[] konami_direct  = new opcode[256];
    opcode[] konami_extended= new opcode[256];
    
    public void Setup_konamiMain_Tables()
    {
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
	konami_main[0x08]=opcode2;  konami_main[0x09]=opcode2;  konami_main[0x0a]=opcode2;  konami_main[0x0b]=opcode2;  konami_main[0x0c]=pshs;  /*,pshu   ,*/konami_main[0x0e]=puls;   /*,pulu   ,*/
        konami_main[0x10]=lda_im;   konami_main[0x11]=ldb_im;/* ,opcode2,opcode2,adda_im,addb_im,opcode2,opcode2,	/* 10 */
/*TODO*///	adca_im,adcb_im,opcode2,opcode2,suba_im,subb_im,opcode2,opcode2,
/*TODO*///	sbca_im,sbcb_im,opcode2,opcode2,anda_im,andb_im,opcode2,opcode2,	/* 20 */
/*TODO*///	bita_im,bitb_im,opcode2,opcode2,eora_im,eorb_im,opcode2,opcode2,
/*TODO*///	ora_im ,orb_im ,opcode2,opcode2,cmpa_im,cmpb_im,opcode2,opcode2,	/* 30 */
	/*setline_im,opcode2,*/konami_main[0x3a]=opcode2;/*,opcode2,andcc,orcc  ,exg    ,tfr    ,
        /*	ldd_im ,opcode2,*/konami_main[0x42]=ldx_im; konami_main[0x43]=opcode2;/*ldy_im ,opcode2,ldu_im ,opcode2,	/* 40 */
	konami_main[0x48]=lds_im; konami_main[0x49]=opcode2;//,cmpd_im,opcode2,cmpx_im,opcode2,cmpy_im,opcode2,
/*TODO*///	cmpu_im,opcode2,cmps_im,opcode2,addd_im,opcode2,subd_im,opcode2,	/* 50 */
/*TODO*///	opcode2,opcode2,opcode2,opcode2,opcode2,illegal,illegal,illegal,
	/*bra    ,bhi    ,bcc    ,*/konami_main[0x63]=bne;   /* ,bvc    ,bpl    ,bge    ,bgt    ,	/* 60 */
/*TODO*///	lbra   ,lbhi   ,lbcc   ,lbne   ,lbvc   ,lbpl   ,lbge   ,lbgt   ,
/*TODO*///	brn    ,bls    ,bcs    ,beq    ,bvs    ,bmi    ,blt    ,ble    ,	/* 70 */
/*TODO*///	lbrn   ,lbls   ,lbcs   ,lbeq   ,lbvs   ,lbmi   ,lblt   ,lble   ,
	konami_main[0x80]=clra;   /*,clrb   ,opcode2,coma   ,comb   ,opcode2,nega   ,negb   ,	/* 80 */
/*TODO*///	opcode2,inca   ,incb   ,opcode2,deca   ,decb   ,opcode2,rts    ,
/*TODO*///	tsta   ,tstb   ,opcode2,lsra   ,lsrb   ,opcode2,rora   ,rorb   ,	/* 90 */
/*TODO*///	opcode2,asra   ,asrb   ,opcode2,asla   ,aslb   ,opcode2,rti    ,
/*TODO*///	rola   ,rolb   ,opcode2,opcode2,opcode2,opcode2,opcode2,opcode2,	/* a0 */
/*TODO*///	opcode2,opcode2,bsr    ,lbsr   ,decbjnz,decxjnz,nop    ,illegal,
/*TODO*///	abx    ,daa	   ,sex    ,mul    ,lmul   ,divx   ,bmove  ,move   ,	/* b0 */
/*TODO*///	lsrd   ,opcode2,rord   ,opcode2,asrd   ,opcode2,asld   ,opcode2,
/*TODO*///	rold   ,opcode2,clrd   ,opcode2,negd   ,opcode2,incd   ,opcode2,	/* c0 */
/*TODO*///	decd   ,opcode2,tstd   ,opcode2,absa   ,absb   ,absd   ,bset   ,
/*TODO*///	bset2  ,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal
    }
    public void Setup_konamiIndexed_Tables()
    {
 /*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
    	konami_indexed[0x08]=leax;   /*,leay   ,leau   ,leas   ,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,lda_ix ,ldb_ix ,illegal,illegal,adda_ix,addb_ix,	/* 10 */
/*TODO*///	illegal,illegal,adca_ix,adcb_ix,illegal,illegal,suba_ix,subb_ix,
/*TODO*///	illegal,illegal,sbca_ix,sbcb_ix,illegal,illegal,anda_ix,andb_ix,	/* 20 */
/*TODO*///	illegal,illegal,bita_ix,bitb_ix,illegal,illegal,eora_ix,eorb_ix,
/*TODO*///	illegal,illegal,ora_ix ,orb_ix ,illegal,illegal,cmpa_ix,cmpb_ix,	/* 30 */
/*TODO*///	illegal,setline_ix,sta_ix,stb_ix,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,ldd_ix ,illegal,ldx_ix ,illegal,ldy_ix ,illegal,ldu_ix ,	/* 40 */
/*TODO*///	illegal,lds_ix ,illegal,cmpd_ix,illegal,cmpx_ix,illegal,cmpy_ix,
/*TODO*///	illegal,cmpu_ix,illegal,cmps_ix,illegal,addd_ix,illegal,subd_ix,	/* 50 */
/*TODO*///	std_ix ,stx_ix ,sty_ix ,stu_ix ,sts_ix ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,clr_ix ,illegal,illegal,com_ix ,illegal,illegal,	/* 80 */
/*TODO*///	neg_ix ,illegal,illegal,inc_ix ,illegal,illegal,dec_ix ,illegal,
/*TODO*///	illegal,illegal,tst_ix ,illegal,illegal,lsr_ix ,illegal,illegal,	/* 90 */
/*TODO*///	ror_ix ,illegal,illegal,asr_ix ,illegal,illegal,asl_ix ,illegal,
/*TODO*///	illegal,illegal,rol_ix ,lsrw_ix,rorw_ix,asrw_ix,aslw_ix,rolw_ix,	/* a0 */
/*TODO*///	jmp_ix ,jsr_ix ,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_ix,illegal,rord_ix,illegal,asrd_ix,illegal,asld_ix,
/*TODO*///	illegal,rold_ix,illegal,clrw_ix,illegal,negw_ix,illegal,incw_ix,	/* c0 */
/*TODO*///	illegal,decw_ix,illegal,tstw_ix,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal       
    }
    public void Setup_konamiDirect_Tables()
    {
 /*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,lda_di ,ldb_di ,illegal,illegal,adda_di,addb_di,	/* 10 */
/*TODO*///	illegal,illegal,adca_di,adcb_di,illegal,illegal,suba_di,subb_di,
/*TODO*///	illegal,illegal,sbca_di,sbcb_di,illegal,illegal,anda_di,andb_di,	/* 20 */
/*TODO*///	illegal,illegal,bita_di,bitb_di,illegal,illegal,eora_di,eorb_di,
/*TODO*///	illegal,illegal,ora_di ,orb_di ,illegal,illegal,cmpa_di,cmpb_di,	/* 30 */
/*TODO*///	illegal,setline_di,sta_di,stb_di,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,ldd_di ,illegal,ldx_di ,illegal,ldy_di ,illegal,ldu_di ,	/* 40 */
/*TODO*///	illegal,lds_di ,illegal,cmpd_di,illegal,cmpx_di,illegal,cmpy_di,
/*TODO*///	illegal,cmpu_di,illegal,cmps_di,illegal,addd_di,illegal,subd_di,	/* 50 */
/*TODO*///	std_di ,stx_di ,sty_di ,stu_di ,sts_di ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,clr_di ,illegal,illegal,com_di ,illegal,illegal,	/* 80 */
/*TODO*///	neg_di ,illegal,illegal,inc_di ,illegal,illegal,dec_di ,illegal,
/*TODO*///	illegal,illegal,tst_di ,illegal,illegal,lsr_di ,illegal,illegal,	/* 90 */
/*TODO*///	ror_di ,illegal,illegal,asr_di ,illegal,illegal,asl_di ,illegal,
/*TODO*///	illegal,illegal,rol_di ,lsrw_di,rorw_di,asrw_di,aslw_di,rolw_di,	/* a0 */
/*TODO*///	jmp_di ,jsr_di ,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_di,illegal,rord_di,illegal,asrd_di,illegal,asld_di,
/*TODO*///	illegal,rold_di,illegal,clrw_di,illegal,negw_di,illegal,incw_di,	/* c0 */
/*TODO*///	illegal,decw_di,illegal,tstw_di,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal       
    }
    public void Setup_konamiExtended_Tables()
    {
        /*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,lda_ex ,ldb_ex ,illegal,illegal,adda_ex,addb_ex,	/* 10 */
/*TODO*///	illegal,illegal,adca_ex,adcb_ex,illegal,illegal,suba_ex,subb_ex,
/*TODO*///	illegal,illegal,sbca_ex,sbcb_ex,illegal,illegal,anda_ex,andb_ex,	/* 20 */
/*TODO*///	illegal,illegal,bita_ex,bitb_ex,illegal,illegal,eora_ex,eorb_ex,
/*TODO*///	illegal,illegal,ora_ex ,orb_ex ,illegal,illegal,cmpa_ex,cmpb_ex,	/* 30 */
	/*illegal,setline_ex,*/konami_extended[0x3a]=sta_ex;/*,stb_ex,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,ldd_ex ,illegal,ldx_ex ,illegal,ldy_ex ,illegal,ldu_ex ,	/* 40 */
/*TODO*///	illegal,lds_ex ,illegal,cmpd_ex,illegal,cmpx_ex,illegal,cmpy_ex,
/*TODO*///	illegal,cmpu_ex,illegal,cmps_ex,illegal,addd_ex,illegal,subd_ex,	/* 50 */
/*TODO*///	std_ex ,stx_ex ,sty_ex ,stu_ex ,sts_ex ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,clr_ex ,illegal,illegal,com_ex ,illegal,illegal,	/* 80 */
/*TODO*///	neg_ex ,illegal,illegal,inc_ex ,illegal,illegal,dec_ex ,illegal,
/*TODO*///	illegal,illegal,tst_ex ,illegal,illegal,lsr_ex ,illegal,illegal,	/* 90 */
/*TODO*///	ror_ex ,illegal,illegal,asr_ex ,illegal,illegal,asl_ex ,illegal,
/*TODO*///	illegal,illegal,rol_ex ,lsrw_ex,rorw_ex,asrw_ex,aslw_ex,rolw_ex,	/* a0 */
/*TODO*///	jmp_ex ,jsr_ex ,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_ex,illegal,rord_ex,illegal,asrd_ex,illegal,asld_ex,
/*TODO*///	illegal,rold_ex,illegal,clrw_ex,illegal,negw_ex,illegal,incw_ex,	/* c0 */
/*TODO*///	illegal,decw_ex,illegal,tstw_ex,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal
    }
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
    public int ea;

    public static final int KONAMI_CWAI		=8;	/* set when CWAI is waiting for an interrupt */
    public static final int KONAMI_SYNC		=16;	/* set when SYNC is waiting for an interrupt */
    public static final int KONAMI_LDS		=32;	/* set when LDS occured at least once */
    
    public void CHECK_IRQ_LINES()
    {
        System.out.println("TODO check IRQ lines");
    }
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
public void EXTENDED(){ ea=IMMWORD();}
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
    /* macros for branch instructions */
    public void BRANCH(boolean f)
    {
        int t= IMMBYTE();
        if(f)
        {
            konami.pc=konami.pc+(byte)t & 0xFFFF;
            change_pc(konami.pc);  
        }
    }
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
                            System.out.println("Unsupported konami_main instruction 0x" + Integer.toHexString(konami.ireg));
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

    /*TODO*///#ifdef NEW
    /*TODO*///static void illegal= new opcode() { public void handler()
    /*TODO*///#else
    /*TODO*///opcode illegal= new opcode() { public void handler()
    /*TODO*///#endif
    /*TODO*///{
    /*TODO*///	if( errorlog )
    /*TODO*///		fprintf(errorlog, "KONAMI: illegal opcode at %04x\n",PC);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____0x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $00 NEG direct ?**** */
    /*TODO*///opcode neg_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = -t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $01 ILLEGAL */
    /*TODO*///
    /*TODO*////* $02 ILLEGAL */
    /*TODO*///
    /*TODO*////* $03 COM direct -**01 */
    /*TODO*///opcode com_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	t = ~t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $04 LSR direct -0*-* */
    /*TODO*///opcode lsr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t >>= 1;
    /*TODO*///	SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $05 ILLEGAL */
    /*TODO*///
    /*TODO*////* $06 ROR direct -**-* */
    /*TODO*///opcode ror_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r= (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	r |= t>>1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $07 ASR direct ?**-* */
    /*TODO*///opcode asr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t = (t & 0x80) | (t >> 1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $08 ASL direct ?**** */
    /*TODO*///opcode asl_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $09 ROL direct -**** */
    /*TODO*///opcode rol_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $0A DEC direct -***- */
    /*TODO*///opcode dec_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	--t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $0B ILLEGAL */
    /*TODO*///
    /*TODO*////* $OC INC direct -***- */
    /*TODO*///opcode inc_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	++t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $OD TST direct -**0- */
    /*TODO*///opcode tst_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $0E JMP direct ----- */
    /*TODO*///opcode jmp_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///    DIRECT;
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $0F CLR direct -0100 */
    /*TODO*///opcode clr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____1x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $10 FLAG */
    /*TODO*///
    /*TODO*////* $11 FLAG */
    /*TODO*///
    /*TODO*////* $12 NOP inherent ----- */
    /*TODO*///opcode nop= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $13 SYNC inherent ----- */
    /*TODO*///opcode sync= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
    /*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
    /*TODO*///	/* is disabled, execution continues with the next instruction. */
    /*TODO*///	konami.int_state |= KONAMI_SYNC;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///	/* if KONAMI_SYNC has not been cleared by CHECK_IRQ_LINES,
    /*TODO*///	 * stop execution until the interrupt lines change. */
    /*TODO*///	if( (konami.int_state & KONAMI_SYNC) && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $14 ILLEGAL */
    /*TODO*///
    /*TODO*////* $15 ILLEGAL */
    /*TODO*///
    /*TODO*////* $16 LBRA relative ----- */
    /*TODO*///opcode lbra= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///	PC += EA;
    /*TODO*///	change_pc(PCD);
    /*TODO*///
    /*TODO*///	/* EHC 980508 speed up busy loop */
    /*TODO*///	if( EA == 0xfffd && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $17 LBSR relative ----- */
    /*TODO*///opcode lbsr= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PC += EA;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $18 ILLEGAL */
    /*TODO*///
    /*TODO*///#if 1
    /*TODO*////* $19 DAA inherent (A) -**0* */
    /*TODO*///opcode daa= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 msn, lsn;
    /*TODO*///	UINT16 t, cf = 0;
    /*TODO*///	msn = A & 0xf0; lsn = A & 0x0f;
    /*TODO*///	if( lsn>0x09 || CC & CC_H) cf |= 0x06;
    /*TODO*///	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
    /*TODO*///	if( msn>0x90 || CC & CC_C) cf |= 0x60;
    /*TODO*///	t = cf + A;
    /*TODO*///	CLR_NZV; /* keep carry from previous operation */
    /*TODO*///	SET_NZ8((UINT8)t); SET_C8(t);
    /*TODO*///	A = t;
    /*TODO*///}};
    /*TODO*///#else
    /*TODO*////* $19 DAA inherent (A) -**0* */
    /*TODO*///opcode daa= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	t = A;
    /*TODO*///	if (CC & CC_H) t+=0x06;
    /*TODO*///	if ((t&0x0f)>9) t+=0x06;		/* ASG -- this code is broken! $66+$99=$FF -> DAA should = $65, we get $05! */
    /*TODO*///	if (CC & CC_C) t+=0x60;
    /*TODO*///	if ((t&0xf0)>0x90) t+=0x60;
    /*TODO*///	if (t&0x100) SEC;
    /*TODO*///	A = t;
    /*TODO*///}};
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $1A ORCC immediate ##### */
    /*TODO*///opcode orcc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC |= t;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1B ILLEGAL */
    /*TODO*///
    /*TODO*////* $1C ANDCC immediate ##### */
    /*TODO*///opcode andcc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC &= t;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1D SEX inherent -**0- */
    /*TODO*///opcode sex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	t = SIGNED(B);
    /*TODO*///	D = t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1E EXG inherent ----- */
    /*TODO*///opcode exg= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t1 = 0, t2 = 0;
    /*TODO*///	UINT8 tb;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	GETREG( t1, tb >> 4 );
    /*TODO*///	GETREG( t2, tb & 0x0f );
    /*TODO*///
    /*TODO*///	SETREG( t2, tb >> 4 );
    /*TODO*///	SETREG( t1, tb & 0x0f );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1F TFR inherent ----- */
    /*TODO*///opcode tfr= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 tb;
    /*TODO*///	UINT16 t = 0;
    /*TODO*///
    /*TODO*///	IMMBYTE(tb);
    /*TODO*///
    /*TODO*///	GETREG( t, tb & 0x0f );
    /*TODO*///	SETREG( t, ( tb >> 4 ) & 0x07 );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____2x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $20 BRA relative ----- */
    /*TODO*///opcode bra= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	PC += SIGNED(t);
    /*TODO*///	change_pc(PCD);
    /*TODO*///	/* JB 970823 - speed up busy loops */
    /*TODO*///	if( t == 0xfe && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $21 BRN relative ----- */
    /*TODO*///opcode brn= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1021 LBRN relative ----- */
    /*TODO*///opcode lbrn= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $22 BHI relative ----- */
    /*TODO*///opcode bhi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC & (CC_Z|CC_C)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1022 LBHI relative ----- */
    /*TODO*///opcode lbhi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC & (CC_Z|CC_C)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $23 BLS relative ----- */
    /*TODO*///opcode bls= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC & (CC_Z|CC_C)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1023 LBLS relative ----- */
    /*TODO*///opcode lbls= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&(CC_Z|CC_C)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $24 BCC relative ----- */
    /*TODO*///opcode bcc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_C) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1024 LBCC relative ----- */
    /*TODO*///opcode lbcc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_C) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $25 BCS relative ----- */
    /*TODO*///opcode bcs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_C) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1025 LBCS relative ----- */
    /*TODO*///opcode lbcs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_C) );
    /*TODO*///}};
    /*TODO*///
    /* $26 BNE relative ----- */
    opcode bne= new opcode() { public void handler()
    {
        BRANCH( (konami.cc&CC_Z)==0 );
    }};
    
    /*TODO*////* $1026 LBNE relative ----- */
    /*TODO*///opcode lbne= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_Z) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $27 BEQ relative ----- */
    /*TODO*///opcode beq= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_Z) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1027 LBEQ relative ----- */
    /*TODO*///opcode lbeq= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_Z) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $28 BVC relative ----- */
    /*TODO*///opcode bvc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1028 LBVC relative ----- */
    /*TODO*///opcode lbvc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $29 BVS relative ----- */
    /*TODO*///opcode bvs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1029 LBVS relative ----- */
    /*TODO*///opcode lbvs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2A BPL relative ----- */
    /*TODO*///opcode bpl= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(CC&CC_N) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102A LBPL relative ----- */
    /*TODO*///opcode lbpl= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_N) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2B BMI relative ----- */
    /*TODO*///opcode bmi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_N) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102B LBMI relative ----- */
    /*TODO*///opcode lbmi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_N) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2C BGE relative ----- */
    /*TODO*///opcode bge= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102C LBGE relative ----- */
    /*TODO*///opcode lbge= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2D BLT relative ----- */
    /*TODO*///opcode blt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102D LBLT relative ----- */
    /*TODO*///opcode lblt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2E BGT relative ----- */
    /*TODO*///opcode bgt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102E LBGT relative ----- */
    /*TODO*///opcode lbgt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2F BLE relative ----- */
    /*TODO*///opcode ble= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102F LBLE relative ----- */
    /*TODO*///opcode lble= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____3x____
    /*TODO*///#endif
    /*TODO*///
    /* $30 LEAX indexed --*-- */
    opcode leax= new opcode() { public void handler()
    {
        konami.x = ea;
    	CLR_Z();
    	SET_Z(konami.x);       
    }};
    
    /*TODO*////* $31 LEAY indexed --*-- */
    /*TODO*///opcode leay= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	Y = EA;
    /*TODO*///	CLR_Z;
    /*TODO*///	SET_Z(Y);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $32 LEAS indexed ----- */
    /*TODO*///opcode leas= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	S = EA;
    /*TODO*///	konami.int_state |= KONAMI_LDS;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $33 LEAU indexed ----- */
    /*TODO*///opcode leau= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	U = EA;
    /*TODO*///}};
    /*TODO*///
    /* $34 PSHS inherent ----- */
    opcode pshs= new opcode() { public void handler()
    {
         
        int t=IMMBYTE();
    	if((  t&0x80 )!=0) { PUSHWORD(konami.pc); konami_ICount[0]-= 2; }
    	if((  t&0x40 )!=0) { PUSHWORD(konami.u);  konami_ICount[0]-= 2; }
    	if((  t&0x20 )!=0) { PUSHWORD(konami.y);  konami_ICount[0]-= 2; }
    	if((  t&0x10 )!=0) { PUSHWORD(konami.x);  konami_ICount[0]-= 2; }
    	if((  t&0x08 )!=0) { PUSHBYTE(konami.dp);  konami_ICount[0]-= 1; }
    	if((  t&0x04 )!=0) { PUSHBYTE(konami.b);   konami_ICount[0]-= 1; }
    	if((  t&0x02 )!=0) { PUSHBYTE(konami.a);   konami_ICount[0]-= 1; }
    	if((  t&0x01 )!=0) { PUSHBYTE(konami.cc);  konami_ICount[0]-= 1; }
    }};
    
    /* 35 PULS inherent ----- */
    opcode puls= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	if(( t&0x01 )!=0) { konami.cc=PULLBYTE(); konami_ICount[0] -= 1; }
    	if(( t&0x02 )!=0) { konami.a=PULLBYTE();  konami_ICount[0] -= 1; }
    	if(( t&0x04 )!=0) { konami.b=PULLBYTE();  konami_ICount[0] -= 1; }
    	if(( t&0x08 )!=0) { konami.dp=PULLBYTE(); konami_ICount[0] -= 1; }
    	if(( t&0x10 )!=0) { konami.x=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x20 )!=0) { konami.y=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x40 )!=0) { konami.u=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x80 )!=0) { konami.pc=PULLWORD(); change_pc(konami.pc); konami_ICount[0] -= 2; }
    
    	/* check after all PULLs */
    	if(( t&0x01 )!=0) { CHECK_IRQ_LINES(); }
        
    }};
    
    /* $36 PSHU inherent ----- */
    /*TODO*///opcode pshu= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if( t&0x80 ) { PSHUWORD(pPC); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x40 ) { PSHUWORD(pS);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x20 ) { PSHUWORD(pY);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x10 ) { PSHUWORD(pX);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x08 ) { PSHUBYTE(DP);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x04 ) { PSHUBYTE(B);   konami_ICount -= 1; }};
    /*TODO*///	if( t&0x02 ) { PSHUBYTE(A);   konami_ICount -= 1; }};
    /*TODO*///	if( t&0x01 ) { PSHUBYTE(CC);  konami_ICount -= 1; }};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* 37 PULU inherent ----- */
    /*TODO*///opcode pulu= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if( t&0x01 ) { PULUBYTE(CC); konami_ICount -= 1; }};
    /*TODO*///	if( t&0x02 ) { PULUBYTE(A);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x04 ) { PULUBYTE(B);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x08 ) { PULUBYTE(DP); konami_ICount -= 1; }};
    /*TODO*///	if( t&0x10 ) { PULUWORD(XD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x20 ) { PULUWORD(YD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x40 ) { PULUWORD(SD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x80 ) { PULUWORD(PCD); change_pc(PCD); konami_ICount -= 2; }};
    /*TODO*///
    /*TODO*///	/* check after all PULLs */
    /*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $38 ILLEGAL */
    /*TODO*///
    /*TODO*////* $39 RTS inherent ----- */
    /*TODO*///opcode rts= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PULLWORD(PCD);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $3A ABX inherent ----- */
    /*TODO*///opcode abx= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	X += B;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $3B RTI inherent ##### */
    /*TODO*///opcode rti= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PULLBYTE(CC);
    /*TODO*///	if( CC & CC_E ) /* entire state saved? */
    /*TODO*///	{
    /*TODO*///        konami_ICount -= 9;
    /*TODO*///		PULLBYTE(A);
    /*TODO*///		PULLBYTE(B);
    /*TODO*///		PULLBYTE(DP);
    /*TODO*///		PULLWORD(XD);
    /*TODO*///		PULLWORD(YD);
    /*TODO*///		PULLWORD(UD);
    /*TODO*///	}};
    /*TODO*///	PULLWORD(PCD);
    /*TODO*///	change_pc(PCD);
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $3C CWAI inherent ----1 */
    /*TODO*///opcode cwai= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC &= t;
    /*TODO*///	/*
    /*TODO*///     * CWAI stacks the entire machine state on the hardware stack,
    /*TODO*///     * then waits for an interrupt; when the interrupt is taken
    /*TODO*///     * later, the state is *not* saved again after CWAI.
    /*TODO*///     */
    /*TODO*///	CC |= CC_E; 		/* HJB 990225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	konami.int_state |= KONAMI_CWAI;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///	if( (konami.int_state & KONAMI_CWAI) && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $3D MUL inherent --*-@ */
    /*TODO*///opcode mul= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	t = A * B;
    /*TODO*///	CLR_ZC; SET_Z16(t); if(t&0x80) SEC;
    /*TODO*///	D = t;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $3E ILLEGAL */
    /*TODO*///
    /*TODO*////* $3F SWI (SWI2 SWI3) absolute indirect ----- */
    /*TODO*///opcode swi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
    /*TODO*///	PCD=RM16(0xfffa);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $103F SWI2 absolute indirect ----- */
    /*TODO*///opcode swi2= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///    PUSHBYTE(CC);
    /*TODO*///	PCD=RM16(0xfff4);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $113F SWI3 absolute indirect ----- */
    /*TODO*///opcode swi3= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///    PUSHBYTE(CC);
    /*TODO*///	PCD=RM16(0xfff2);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____4x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $40 NEGA inherent ?**** */
    /*TODO*///opcode nega= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = -A;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,A,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $41 ILLEGAL */
    /*TODO*///
    /*TODO*////* $42 ILLEGAL */
    /*TODO*///
    /*TODO*////* $43 COMA inherent -**01 */
    /*TODO*///opcode coma= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A = ~A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	SEC;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $44 LSRA inherent -0*-* */
    /*TODO*///opcode lsra= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	A >>= 1;
    /*TODO*///	SET_Z8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $45 ILLEGAL */
    /*TODO*///
    /*TODO*////* $46 RORA inherent -**-* */
    /*TODO*///opcode rora= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	r |= A >> 1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $47 ASRA inherent ?**-* */
    /*TODO*///opcode asra= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	A = (A & 0x80) | (A >> 1);
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $48 ASLA inherent ?**** */
    /*TODO*///opcode asla= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = A << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,A,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $49 ROLA inherent -**** */
    /*TODO*///opcode rola= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = A;
    /*TODO*///	r = (CC & CC_C) | (t<<1);
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $4A DECA inherent -***- */
    /*TODO*///opcode deca= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	--A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $4B ILLEGAL */
    /*TODO*///
    /*TODO*////* $4C INCA inherent -***- */
    /*TODO*///opcode inca= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	++A;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $4D TSTA inherent -**0- */
    /*TODO*///opcode tsta= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $4E ILLEGAL */
    /*TODO*///
    /* $4F CLRA inherent -0100 */
    opcode clra= new opcode() { public void handler()
    {
        konami.a = 0;
    	CLR_NZVC(); 
        SEZ();
    }};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____5x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $50 NEGB inherent ?**** */
    /*TODO*///opcode negb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = -B;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,B,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $51 ILLEGAL */
    /*TODO*///
    /*TODO*////* $52 ILLEGAL */
    /*TODO*///
    /*TODO*////* $53 COMB inherent -**01 */
    /*TODO*///opcode comb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B = ~B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	SEC;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $54 LSRB inherent -0*-* */
    /*TODO*///opcode lsrb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	B >>= 1;
    /*TODO*///	SET_Z8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $55 ILLEGAL */
    /*TODO*///
    /*TODO*////* $56 RORB inherent -**-* */
    /*TODO*///opcode rorb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	r |= B >> 1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $57 ASRB inherent ?**-* */
    /*TODO*///opcode asrb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	B= (B & 0x80) | (B >> 1);
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $58 ASLB inherent ?**** */
    /*TODO*///opcode aslb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	r = B << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,B,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $59 ROLB inherent -**** */
    /*TODO*///opcode rolb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = B;
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $5A DECB inherent -***- */
    /*TODO*///opcode decb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	--B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $5B ILLEGAL */
    /*TODO*///
    /*TODO*////* $5C INCB inherent -***- */
    /*TODO*///opcode incb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	++B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8I(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $5D TSTB inherent -**0- */
    /*TODO*///opcode tstb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $5E ILLEGAL */
    /*TODO*///
    /*TODO*////* $5F CLRB inherent -0100 */
    /*TODO*///opcode clrb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____6x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $60 NEG indexed ?**** */
    /*TODO*///opcode neg_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = -t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $61 ILLEGAL */
    /*TODO*///
    /*TODO*////* $62 ILLEGAL */
    /*TODO*///
    /*TODO*////* $63 COM indexed -**01 */
    /*TODO*///opcode com_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = ~RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $64 LSR indexed -0*-* */
    /*TODO*///opcode lsr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $65 ILLEGAL */
    /*TODO*///
    /*TODO*////* $66 ROR indexed -**-* */
    /*TODO*///opcode ror_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	r |= t>>1; SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $67 ASR indexed ?**-* */
    /*TODO*///opcode asr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>=1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $68 ASL indexed ?**** */
    /*TODO*///opcode asl_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $69 ROL indexed -**** */
    /*TODO*///opcode rol_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $6A DEC indexed -***- */
    /*TODO*///opcode dec_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD) - 1;
    /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $6B ILLEGAL */
    /*TODO*///
    /*TODO*////* $6C INC indexed -***- */
    /*TODO*///opcode inc_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD) + 1;
    /*TODO*///	CLR_NZV; SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $6D TST indexed -**0- */
    /*TODO*///opcode tst_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $6E JMP indexed ----- */
    /*TODO*///opcode jmp_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $6F CLR indexed -0100 */
    /*TODO*///opcode clr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____7x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $70 NEG extended ?**** */
    /*TODO*///opcode neg_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	EXTBYTE(t); r=-t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $71 ILLEGAL */
    /*TODO*///
    /*TODO*////* $72 ILLEGAL */
    /*TODO*///
    /*TODO*////* $73 COM extended -**01 */
    /*TODO*///opcode com_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); t = ~t;
    /*TODO*///	CLR_NZV; SET_NZ8(t); SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $74 LSR extended -0*-* */
    /*TODO*///opcode lsr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $75 ILLEGAL */
    /*TODO*///
    /*TODO*////* $76 ROR extended -**-* */
    /*TODO*///opcode ror_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t); r=(CC & CC_C) << 7;
    /*TODO*///	CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	r |= t>>1; SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $77 ASR extended ?**-* */
    /*TODO*///opcode asr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $78 ASL extended ?**** */
    /*TODO*///opcode asl_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r=t<<1;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $79 ROL extended -**** */
    /*TODO*///opcode rol_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $7A DEC extended -***- */
    /*TODO*///opcode dec_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); --t;
    /*TODO*///	CLR_NZV; SET_FLAGS8D(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $7B ILLEGAL */
    /*TODO*///
    /*TODO*////* $7C INC extended -***- */
    /*TODO*///opcode inc_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); ++t;
    /*TODO*///	CLR_NZV; SET_FLAGS8I(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $7D TST extended -**0- */
    /*TODO*///opcode tst_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZV; SET_NZ8(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $7E JMP extended ----- */
    /*TODO*///opcode jmp_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $7F CLR extended -0100 */
    /*TODO*///opcode clr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,0);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____8x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $80 SUBA immediate ?**** */
    /*TODO*///opcode suba_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $81 CMPA immediate ?**** */
    /*TODO*///opcode cmpa_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $82 SBCA immediate ?**** */
    /*TODO*///opcode sbca_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $83 SUBD (CMPD CMPU) immediate -**** */
    /*TODO*///opcode subd_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1083 CMPD immediate -**** */
    /*TODO*///opcode cmpd_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1183 CMPU immediate -**** */
    /*TODO*///opcode cmpu_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r, d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $84 ANDA immediate -**0- */
    /*TODO*///opcode anda_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $85 BITA immediate -**0- */
    /*TODO*///opcode bita_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /* $86 LDA immediate -**0- */
    opcode lda_im= new opcode() { public void handler()
    {
    	konami.a=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $87 STA immediate -**0- */
    /*TODO*///opcode sta_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $88 EORA immediate -**0- */
    /*TODO*///opcode eora_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $89 ADCA immediate ***** */
    /*TODO*///opcode adca_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $8A ORA immediate -**0- */
    /*TODO*///opcode ora_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $8B ADDA immediate ***** */
    /*TODO*///opcode adda_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $8C CMPX (CMPY CMPS) immediate -**** */
    /*TODO*///opcode cmpx_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $108C CMPY immediate -**** */
    /*TODO*///opcode cmpy_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $118C CMPS immediate -**** */
    /*TODO*///opcode cmps_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $8D BSR ----- */
    /*TODO*///opcode bsr= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PC += SIGNED(t);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /* $8E LDX (LDY) immediate -**0- */
    opcode ldx_im= new opcode() { public void handler()
    {
        konami.x=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.x);
    }};
    
    /*TODO*////* $108E LDY immediate -**0- */
    /*TODO*///opcode ldy_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $8F STX (STY) immediate -**0- */
    /*TODO*///opcode stx_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $108F STY immediate -**0- */
    /*TODO*///opcode sty_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____9x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $90 SUBA direct ?**** */
    /*TODO*///opcode suba_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $91 CMPA direct ?**** */
    /*TODO*///opcode cmpa_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $92 SBCA direct ?**** */
    /*TODO*///opcode sbca_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $93 SUBD (CMPD CMPU) direct -**** */
    /*TODO*///opcode subd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1093 CMPD direct -**** */
    /*TODO*///opcode cmpd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1193 CMPU direct -**** */
    /*TODO*///opcode cmpu_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $94 ANDA direct -**0- */
    /*TODO*///opcode anda_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $95 BITA direct -**0- */
    /*TODO*///opcode bita_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $96 LDA direct -**0- */
    /*TODO*///opcode lda_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRBYTE(A);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $97 STA direct -**0- */
    /*TODO*///opcode sta_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $98 EORA direct -**0- */
    /*TODO*///opcode eora_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $99 ADCA direct ***** */
    /*TODO*///opcode adca_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9A ORA direct -**0- */
    /*TODO*///opcode ora_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9B ADDA direct ***** */
    /*TODO*///opcode adda_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9C CMPX (CMPY CMPS) direct -**** */
    /*TODO*///opcode cmpx_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $109C CMPY direct -**** */
    /*TODO*///opcode cmpy_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $119C CMPS direct -**** */
    /*TODO*///opcode cmps_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9D JSR direct ----- */
    /*TODO*///opcode jsr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9E LDX (LDY) direct -**0- */
    /*TODO*///opcode ldx_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRWORD(pX);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $109E LDY direct -**0- */
    /*TODO*///opcode ldy_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9F STX (STY) direct -**0- */
    /*TODO*///opcode stx_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $109F STY direct -**0- */
    /*TODO*///opcode sty_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Ax____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////* $a0 SUBA indexed ?**** */
    /*TODO*///opcode suba_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a1 CMPA indexed ?**** */
    /*TODO*///opcode cmpa_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a2 SBCA indexed ?**** */
    /*TODO*///opcode sbca_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a3 SUBD (CMPD CMPU) indexed -**** */
    /*TODO*///opcode subd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10a3 CMPD indexed -**** */
    /*TODO*///opcode cmpd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11a3 CMPU indexed -**** */
    /*TODO*///opcode cmpu_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	r = U - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a4 ANDA indexed -**0- */
    /*TODO*///opcode anda_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A &= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a5 BITA indexed -**0- */
    /*TODO*///opcode bita_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = A & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a6 LDA indexed -**0- */
    /*TODO*///opcode lda_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a7 STA indexed -**0- */
    /*TODO*///opcode sta_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	WM(EAD,A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a8 EORA indexed -**0- */
    /*TODO*///opcode eora_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $a9 ADCA indexed ***** */
    /*TODO*///opcode adca_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aA ORA indexed -**0- */
    /*TODO*///opcode ora_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A |= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aB ADDA indexed ***** */
    /*TODO*///opcode adda_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aC CMPX (CMPY CMPS) indexed -**** */
    /*TODO*///opcode cmpx_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10aC CMPY indexed -**** */
    /*TODO*///opcode cmpy_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11aC CMPS indexed -**** */
    /*TODO*///opcode cmps_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aD JSR indexed ----- */
    /*TODO*///opcode jsr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aE LDX (LDY) indexed -**0- */
    /*TODO*///opcode ldx_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	X=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10aE LDY indexed -**0- */
    /*TODO*///opcode ldy_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	Y=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $aF STX (STY) indexed -**0- */
    /*TODO*///opcode stx_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10aF STY indexed -**0- */
    /*TODO*///opcode sty_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Bx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $b0 SUBA extended ?**** */
    /*TODO*///opcode suba_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b1 CMPA extended ?**** */
    /*TODO*///opcode cmpa_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b2 SBCA extended ?**** */
    /*TODO*///opcode sbca_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b3 SUBD (CMPD CMPU) extended -**** */
    /*TODO*///opcode subd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10b3 CMPD extended -**** */
    /*TODO*///opcode cmpd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11b3 CMPU extended -**** */
    /*TODO*///opcode cmpu_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b4 ANDA extended -**0- */
    /*TODO*///opcode anda_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b5 BITA extended -**0- */
    /*TODO*///opcode bita_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV; SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b6 LDA extended -**0- */
    /*TODO*///opcode lda_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTBYTE(A);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /* $b7 STA extended -**0- */
    opcode sta_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.a);
    	EXTENDED();
    	WM(ea,konami.a);
    }};
    /*TODO*///
    /*TODO*////* $b8 EORA extended -**0- */
    /*TODO*///opcode eora_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b9 ADCA extended ***** */
    /*TODO*///opcode adca_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bA ORA extended -**0- */
    /*TODO*///opcode ora_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bB ADDA extended ***** */
    /*TODO*///opcode adda_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bC CMPX (CMPY CMPS) extended -**** */
    /*TODO*///opcode cmpx_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = X;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10bC CMPY extended -**** */
    /*TODO*///opcode cmpy_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11bC CMPS extended -**** */
    /*TODO*///opcode cmps_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bD JSR extended ----- */
    /*TODO*///opcode jsr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTENDED;
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bE LDX (LDY) extended -**0- */
    /*TODO*///opcode ldx_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTWORD(pX);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10bE LDY extended -**0- */
    /*TODO*///opcode ldy_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTWORD(pY);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bF STX (STY) extended -**0- */
    /*TODO*///opcode stx_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10bF STY extended -**0- */
    /*TODO*///opcode sty_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Cx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $c0 SUBB immediate ?**** */
    /*TODO*///opcode subb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c1 CMPB immediate ?**** */
    /*TODO*///opcode cmpb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(B,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c2 SBCB immediate ?**** */
    /*TODO*///opcode sbcb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c3 ADDD immediate -**** */
    /*TODO*///opcode addd_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c4 ANDB immediate -**0- */
    /*TODO*///opcode andb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c5 BITB immediate -**0- */
    /*TODO*///opcode bitb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /* $c6 LDB immediate -**0- */
    opcode ldb_im= new opcode() { public void handler()
    {
        konami.b=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $c7 STB immediate -**0- */
    /*TODO*///opcode stb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c8 EORB immediate -**0- */
    /*TODO*///opcode eorb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $c9 ADCB immediate ***** */
    /*TODO*///opcode adcb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $cA ORB immediate -**0- */
    /*TODO*///opcode orb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $cB ADDB immediate ***** */
    /*TODO*///opcode addb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $cC LDD immediate -**0- */
    /*TODO*///opcode ldd_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(pD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cD STD immediate -**0- */
    /*TODO*///opcode std_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $cE LDU (LDS) immediate -**0- */
    /*TODO*///opcode ldu_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}};
    /*TODO*///
    /* $10cE LDS immediate -**0- */
    opcode lds_im= new opcode() { public void handler()
    {
        konami.s=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.s);
    	konami.int_state |= KONAMI_LDS;        

    }};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cF STU (STS) immediate -**0- */
    /*TODO*///opcode stu_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $10cF STS immediate -**0- */
    /*TODO*///opcode sts_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Dx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $d0 SUBB direct ?**** */
    /*TODO*///opcode subb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d1 CMPB direct ?**** */
    /*TODO*///opcode cmpb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d2 SBCB direct ?**** */
    /*TODO*///opcode sbcb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d3 ADDD direct -**** */
    /*TODO*///opcode addd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d4 ANDB direct -**0- */
    /*TODO*///opcode andb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d5 BITB direct -**0- */
    /*TODO*///opcode bitb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d6 LDB direct -**0- */
    /*TODO*///opcode ldb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRBYTE(B);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d7 STB direct -**0- */
    /*TODO*///opcode stb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	DIRECT;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d8 EORB direct -**0- */
    /*TODO*///opcode eorb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $d9 ADCB direct ***** */
    /*TODO*///opcode adcb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dA ORB direct -**0- */
    /*TODO*///opcode orb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dB ADDB direct ***** */
    /*TODO*///opcode addb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dC LDD direct -**0- */
    /*TODO*///opcode ldd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRWORD(pD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dD STD direct -**0- */
    /*TODO*///opcode std_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///    DIRECT;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dE LDU (LDS) direct -**0- */
    /*TODO*///opcode ldu_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10dE LDS direct -**0- */
    /*TODO*///opcode lds_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRWORD(pS);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	konami.int_state |= KONAMI_LDS;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $dF STU (STS) direct -**0- */
    /*TODO*///opcode stu_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10dF STS direct -**0- */
    /*TODO*///opcode sts_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Ex____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///
    /*TODO*////* $e0 SUBB indexed ?**** */
    /*TODO*///opcode subb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e1 CMPB indexed ?**** */
    /*TODO*///opcode cmpb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e2 SBCB indexed ?**** */
    /*TODO*///opcode sbcb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e3 ADDD indexed -**** */
    /*TODO*///opcode addd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e4 ANDB indexed -**0- */
    /*TODO*///opcode andb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B &= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e5 BITB indexed -**0- */
    /*TODO*///opcode bitb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = B & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e6 LDB indexed -**0- */
    /*TODO*///opcode ldb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B = RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e7 STB indexed -**0- */
    /*TODO*///opcode stb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	WM(EAD,B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e8 EORB indexed -**0- */
    /*TODO*///opcode eorb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e9 ADCB indexed ***** */
    /*TODO*///opcode adcb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $eA ORB indexed -**0- */
    /*TODO*///opcode orb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B |= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $eb ADDB indexed ***** */
    /*TODO*///opcode addb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $ec LDD indexed -**0- */
    /*TODO*///opcode ldd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	D=RM16(EAD);
    /*TODO*///	CLR_NZV; SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $eD STD indexed -**0- */
    /*TODO*///opcode std_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $eE LDU (LDS) indexed -**0- */
    /*TODO*///opcode ldu_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	U=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10eE LDS indexed -**0- */
    /*TODO*///opcode lds_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	S=RM16(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	konami.int_state |= KONAMI_LDS;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $eF STU (STS) indexed -**0- */
    /*TODO*///opcode stu_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10eF STS indexed -**0- */
    /*TODO*///opcode sts_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Fx____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $f0 SUBB extended ?**** */
    /*TODO*///opcode subb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f1 CMPB extended ?**** */
    /*TODO*///opcode cmpb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f2 SBCB extended ?**** */
    /*TODO*///opcode sbcb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f3 ADDD extended -**** */
    /*TODO*///opcode addd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d + b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f4 ANDB extended -**0- */
    /*TODO*///opcode andb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f5 BITB extended -**0- */
    /*TODO*///opcode bitb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f6 LDB extended -**0- */
    /*TODO*///opcode ldb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTBYTE(B);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f7 STB extended -**0- */
    /*TODO*///opcode stb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f8 EORB extended -**0- */
    /*TODO*///opcode eorb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f9 ADCB extended ***** */
    /*TODO*///opcode adcb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fA ORB extended -**0- */
    /*TODO*///opcode orb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fB ADDB extended ***** */
    /*TODO*///opcode addb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fC LDD extended -**0- */
    /*TODO*///opcode ldd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTWORD(pD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fD STD extended -**0- */
    /*TODO*///opcode std_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///    EXTENDED;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fE LDU (LDS) extended -**0- */
    /*TODO*///opcode ldu_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTWORD(pU);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10fE LDS extended -**0- */
    /*TODO*///opcode lds_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	EXTWORD(pS);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	konami.int_state |= KONAMI_LDS;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $fF STU (STS) extended -**0- */
    /*TODO*///opcode stu_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $10fF STS extended -**0- */
    /*TODO*///opcode sts_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode setline_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode setline_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EA);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode setline_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode setline_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode bmove= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	while( U != 0 ) {
    /*TODO*///		t = RM(Y);
    /*TODO*///		WM(X,t);
    /*TODO*///		Y++;
    /*TODO*///		X++;
    /*TODO*///		U--;
    /*TODO*///		konami_ICount -= 2;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode move= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	t = RM(Y);
    /*TODO*///	WM(X,t);
    /*TODO*///	Y++;
    /*TODO*///	X++;
    /*TODO*///	U--;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* CLRD inherent -0100 */
    /*TODO*///opcode clrd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	D = 0;
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* CLRW indexed -0100 */
    /*TODO*///opcode clrw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d = 0;
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* CLRW direct -0100 */
    /*TODO*///opcode clrw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d = 0;
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* CLRW extended -0100 */
    /*TODO*///opcode clrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d = 0;
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD immediate -0*-* */
    /*TODO*///opcode lsrd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	IMMBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD immediate -**-* */
    /*TODO*///opcode rord= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD immediate ?**-* */
    /*TODO*///opcode asrd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD immediate ?**** */
    /*TODO*///opcode asld= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	IMMBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD immediate -**-* */
    /*TODO*///opcode rold= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECB,JNZ relative ----- */
    /*TODO*///opcode decbjnz= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	--B;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS8D(B);
    /*TODO*///	BRANCH( !(CC&CC_Z) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECX,JNZ relative ----- */
    /*TODO*///opcode decxjnz= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	--X;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);	/* should affect V as well? */
    /*TODO*///	BRANCH( !(CC&CC_Z) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode bset= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	while( U != 0 ) {
    /*TODO*///		t = A;
    /*TODO*///		WM(XD,t);
    /*TODO*///		X++;
    /*TODO*///		U--;
    /*TODO*///		konami_ICount -= 2;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*///opcode bset2= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	while( U != 0 ) {
    /*TODO*///		WM16(XD,&pD);
    /*TODO*///		X += 2;
    /*TODO*///		U--;
    /*TODO*///		konami_ICount -= 3;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LMUL inherent --*-@ */
    /*TODO*///opcode lmul= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 t;
    /*TODO*///	t = X * Y;
    /*TODO*///	X = (t >> 16);
    /*TODO*///	Y = (t & 0xffff);
    /*TODO*///	CLR_ZC; SET_Z(t); if( t & 0x8000 ) SEC;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DIVX inherent --*-@ */
    /*TODO*///opcode divx= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	UINT8 r;
    /*TODO*///	if ( B != 0 )
    /*TODO*///	{
    /*TODO*///		t = X / B;
    /*TODO*///		r = X % B;
    /*TODO*///	}};
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		/* ?? */
    /*TODO*///		t = 0;
    /*TODO*///		r = 0;
    /*TODO*///	}};
    /*TODO*///	CLR_ZC; SET_Z16(t); if ( t & 0x80 ) SEC;
    /*TODO*///	X = t;
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCD inherent -***- */
    /*TODO*///opcode incd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D + 1;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(D,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW direct -***- */
    /*TODO*///opcode incw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW indexed -***- */
    /*TODO*///opcode incw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW extended -***- */
    /*TODO*///opcode incw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECD inherent -***- */
    /*TODO*///opcode decd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D - 1;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(D,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW direct -***- */
    /*TODO*///opcode decw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW indexed -***- */
    /*TODO*///opcode decw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d, t.d, r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW extended -***- */
    /*TODO*///opcode decw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTD inherent -**0- */
    /*TODO*///opcode tstd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW direct -**0- */
    /*TODO*///opcode tstw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW indexed -**0- */
    /*TODO*///opcode tstw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW extended -**0- */
    /*TODO*///opcode tstw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW direct -0*-* */
    /*TODO*///opcode lsrw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW indexed -0*-* */
    /*TODO*///opcode lsrw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW extended -0*-* */
    /*TODO*///opcode lsrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW direct -**-* */
    /*TODO*///opcode rorw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW indexed -**-* */
    /*TODO*///opcode rorw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW extended -**-* */
    /*TODO*///opcode rorw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW direct ?**-* */
    /*TODO*///opcode asrw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW indexed ?**-* */
    /*TODO*///opcode asrw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW extended ?**-* */
    /*TODO*///opcode asrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW direct ?**** */
    /*TODO*///opcode aslw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW indexed ?**** */
    /*TODO*///opcode aslw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW extended ?**** */
    /*TODO*///opcode aslw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW direct -**** */
    /*TODO*///opcode rolw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW indexed -**** */
    /*TODO*///opcode rolw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW extended -**** */
    /*TODO*///opcode rolw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGD inherent ?**** */
    /*TODO*///opcode negd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = -D;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW direct ?**** */
    /*TODO*///opcode negw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW indexed ?**** */
    /*TODO*///opcode negw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW extended ?**** */
    /*TODO*///opcode negw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ABSA inherent ?**** */
    /*TODO*///opcode absa= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	if (A & 0x80)
    /*TODO*///		r = -A;
    /*TODO*///	else
    /*TODO*///		r = A;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,A,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ABSB inherent ?**** */
    /*TODO*///opcode absb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	if (B & 0x80)
    /*TODO*///		r = -B;
    /*TODO*///	else
    /*TODO*///		r = B;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,B,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ABSD inherent ?**** */
    /*TODO*///opcode absd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	if (D & 0x8000)
    /*TODO*///		r = -D;
    /*TODO*///	else
    /*TODO*///		r = D;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD direct -0*-* */
    /*TODO*///opcode lsrd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	DIRBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD direct -**-* */
    /*TODO*///opcode rord_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD direct ?**-* */
    /*TODO*///opcode asrd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD direct ?**** */
    /*TODO*///opcode asld_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	DIRBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD direct -**-* */
    /*TODO*///opcode rold_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD indexed -0*-* */
    /*TODO*///opcode lsrd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD indexed -**-* */
    /*TODO*///opcode rord_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD indexed ?**-* */
    /*TODO*///opcode asrd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD indexed ?**** */
    /*TODO*///opcode asld_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD indexed -**-* */
    /*TODO*///opcode rold_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD extended -0*-* */
    /*TODO*///opcode lsrd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD extended -**-* */
    /*TODO*///opcode rord_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD extended ?**-* */
    /*TODO*///opcode asrd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD extended ?**** */
    /*TODO*///opcode asld_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD extended -**-* */
    /*TODO*///opcode rold_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    
    opcode opcode2= new opcode() { public void handler()
    {
        int ireg2 = ROP_ARG(konami.pc) &0xFF;
        konami.pc = konami.pc + 1 & 0xFFFF;
        
        
    	switch ( ireg2 ) {
    /*TODO*/////	case 0x00: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x01: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x02: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x03: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x04: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x05: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x06: EA=0; break; /* normal */
    	case 0x07:
        {
            ea=0;
            if(konami_extended[konami.ireg]!=null)
            {
                konami_extended[konami.ireg].handler();//(*konami_extended[konami.ireg])();
            }
            else
            {
                System.out.println("Unsupported konami_extended instruction 0x" + Integer.toHexString(konami.ireg));
            }

            konami_ICount[0] -= 2;
            return;
        }
    /*TODO*/////	case 0x08: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x09: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x0a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x0b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x0c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x0d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x0e: EA=0; break; /* indirect - normal */
    /*TODO*///	case 0x0f:				/* indirect - extended */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0x10: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x11: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x12: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x13: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x14: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x15: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x16: EA=0; break; /* normal */
    /*TODO*/////	case 0x17: EA=0; break; /* extended */
    /*TODO*/////	case 0x18: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x19: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x1a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x1b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x1c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x1d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x1e: EA=0; break; /* indirect - normal */
    /*TODO*/////	case 0x1f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base X */
    /*TODO*///    case 0x20:              /* auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x21:				/* double auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///        break;
    /*TODO*///	case 0x22:				/* auto decrement */
    /*TODO*///		X--;
    /*TODO*///		EA=X;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///        break;
    /*TODO*///	case 0x23:				/* double auto decrement */
    /*TODO*///		X-=2;
    /*TODO*///		EA=X;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    	case 0x24:				/* postbyte offs */
    		ea=IMMBYTE();
    		ea = konami.x + (byte)ea & 0xFFFF;//EA=X+SIGNED(EA);
                konami_ICount[0]-=2;
    		break;
    /*TODO*///	case 0x25:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=X;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x26:				/* normal */
    /*TODO*///		EA=X;
    /*TODO*///		break;
    /*TODO*/////	case 0x27: EA=0; break; /* extended */
    /*TODO*///	case 0x28:				/* indirect - auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x29:				/* indirect - double auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x2a:				/* indirect - auto decrement */
    /*TODO*///		X--;
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x2b:				/* indirect - double auto decrement */
    /*TODO*///		X-=2;
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x2c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=X+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x2d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x2e:				/* indirect - normal */
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x2f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base Y */
    /*TODO*///    case 0x30:              /* auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x31:				/* double auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x32:				/* auto decrement */
    /*TODO*///		Y--;
    /*TODO*///		EA=Y;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x33:				/* double auto decrement */
    /*TODO*///		Y-=2;
    /*TODO*///		EA=Y;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x34:				/* postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=Y+SIGNED(EA);
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x35:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=Y;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x36:				/* normal */
    /*TODO*///		EA=Y;
    /*TODO*///		break;
    /*TODO*/////	case 0x37: EA=0; break; /* extended */
    /*TODO*///	case 0x38:				/* indirect - auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x39:				/* indirect - double auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x3a:				/* indirect - auto decrement */
    /*TODO*///		Y--;
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x3b:				/* indirect - double auto decrement */
    /*TODO*///		Y-=2;
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x3c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=Y+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x3d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x3e:				/* indirect - normal */
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x3f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*/////  case 0x40: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x41: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x42: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x43: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x44: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x45: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x46: EA=0; break; /* normal */
    /*TODO*/////	case 0x47: EA=0; break; /* extended */
    /*TODO*/////	case 0x48: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x49: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x4a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x4b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x4c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x4d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x4e: EA=0; break; /* indirect - normal */
    /*TODO*/////	case 0x4f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base U */
    /*TODO*///    case 0x50:              /* auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x51:				/* double auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x52:				/* auto decrement */
    /*TODO*///		U--;
    /*TODO*///		EA=U;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x53:				/* double auto decrement */
    /*TODO*///		U-=2;
    /*TODO*///		EA=U;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x54:				/* postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=U+SIGNED(EA);
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x55:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=U;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x56:				/* normal */
    /*TODO*///		EA=U;
    /*TODO*///		break;
    /*TODO*/////	case 0x57: EA=0; break; /* extended */
    /*TODO*///	case 0x58:				/* indirect - auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x59:				/* indirect - double auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x5a:				/* indirect - auto decrement */
    /*TODO*///		U--;
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x5b:				/* indirect - double auto decrement */
    /*TODO*///		U-=2;
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x5c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=U+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x5d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x5e:				/* indirect - normal */
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x5f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base S */
    /*TODO*///    case 0x60:              /* auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x61:				/* double auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x62:				/* auto decrement */
    /*TODO*///		S--;
    /*TODO*///		EAD=SD;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x63:				/* double auto decrement */
    /*TODO*///		S-=2;
    /*TODO*///		EAD=SD;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x64:				/* postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=S+SIGNED(EA);
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x65:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=S;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x66:				/* normal */
    /*TODO*///		EAD=SD;
    /*TODO*///		break;
    /*TODO*/////	case 0x67: EA=0; break; /* extended */
    /*TODO*///	case 0x68:				/* indirect - auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x69:				/* indirect - double auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x6a:				/* indirect - auto decrement */
    /*TODO*///		S--;
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x6b:				/* indirect - double auto decrement */
    /*TODO*///		S-=2;
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x6c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=S+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x6d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=S;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x6e:				/* indirect - normal */
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x6f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base PC */
    /*TODO*///    case 0x70:              /* auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x71:				/* double auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x72:				/* auto decrement */
    /*TODO*///		PC--;
    /*TODO*///		EAD=PCD;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x73:				/* double auto decrement */
    /*TODO*///		PC-=2;
    /*TODO*///		EAD=PCD;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x74:				/* postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=PC-1+SIGNED(EA);
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x75:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=PC-2;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x76:				/* normal */
    /*TODO*///		EAD=PCD;
    /*TODO*///		break;
    /*TODO*/////	case 0x77: EA=0; break; /* extended */
    /*TODO*///	case 0x78:				/* indirect - auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x79:				/* indirect - double auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x7a:				/* indirect - auto decrement */
    /*TODO*///		PC--;
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x7b:				/* indirect - double auto decrement */
    /*TODO*///		PC-=2;
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x7c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=PC-1+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x7d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=PC-2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x7e:				/* indirect - normal */
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x7f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*/////  case 0x80: EA=0; break; /* register a */
    /*TODO*/////	case 0x81: EA=0; break; /* register b */
    /*TODO*/////	case 0x82: EA=0; break; /* ???? */
    /*TODO*/////	case 0x83: EA=0; break; /* ???? */
    /*TODO*/////	case 0x84: EA=0; break; /* ???? */
    /*TODO*/////	case 0x85: EA=0; break; /* ???? */
    /*TODO*/////	case 0x86: EA=0; break; /* ???? */
    /*TODO*/////	case 0x87: EA=0; break; /* register d */
    /*TODO*/////	case 0x88: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0x89: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0x8a: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8b: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8c: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8d: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8e: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0x8f: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x90: EA=0; break; /* register a */
    /*TODO*/////	case 0x91: EA=0; break; /* register b */
    /*TODO*/////	case 0x92: EA=0; break; /* ???? */
    /*TODO*/////	case 0x93: EA=0; break; /* ???? */
    /*TODO*/////	case 0x94: EA=0; break; /* ???? */
    /*TODO*/////	case 0x95: EA=0; break; /* ???? */
    /*TODO*/////	case 0x96: EA=0; break; /* ???? */
    /*TODO*/////	case 0x97: EA=0; break; /* register d */
    /*TODO*/////	case 0x98: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0x99: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0x9a: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9b: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9c: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9d: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9e: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0x9f: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xa0:				/* register a */
    /*TODO*///		EA=X+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xa1:				/* register b */
    /*TODO*///		EA=X+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xa2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa6: EA=0; break; /* ???? */
    /*TODO*///	case 0xa7:				/* register d */
    /*TODO*///		EA=X+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xa8:				/* indirect - register a */
    /*TODO*///		EA=X+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xa9:				/* indirect - register b */
    /*TODO*///		EA=X+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xaa: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xab: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xac: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xad: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xae: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xaf:				/* indirect - register d */
    /*TODO*///		EA=X+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0xb0:				/* register a */
    /*TODO*///		EA=Y+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xb1:				/* register b */
    /*TODO*///		EA=Y+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xb2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb6: EA=0; break; /* ???? */
    /*TODO*///	case 0xb7:				/* register d */
    /*TODO*///		EA=Y+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xb8:				/* indirect - register a */
    /*TODO*///		EA=Y+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xb9:				/* indirect - register b */
    /*TODO*///		EA=Y+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xba: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbe: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xbf:				/* indirect - register d */
    /*TODO*///		EA=Y+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*/////	case 0xc0: EA=0; break; /* register a */
    /*TODO*/////	case 0xc1: EA=0; break; /* register b */
    /*TODO*/////	case 0xc2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc3: EA=0; break; /* ???? */
    /*TODO*///	case 0xc4:
    /*TODO*///		EAD=0;
    /*TODO*///		(*konami_direct[konami.ireg])();
    /*TODO*///        konami_ICount -= 1;
    /*TODO*///		return;
    /*TODO*/////	case 0xc5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc6: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc7: EA=0; break; /* register d */
    /*TODO*/////	case 0xc8: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0xc9: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0xca: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xcb: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xcc:				/* indirect - direct */
    /*TODO*///		DIRWORD(ea);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xcd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xce: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0xcf: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xd0:				/* register a */
    /*TODO*///		EA=U+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xd1:				/* register b */
    /*TODO*///		EA=U+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xd2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd6: EA=0; break; /* ???? */
    /*TODO*///	case 0xd7:				/* register d */
    /*TODO*///		EA=U+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xd8:				/* indirect - register a */
    /*TODO*///		EA=U+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xd9:				/* indirect - register b */
    /*TODO*///		EA=U+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xda: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xde: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xdf:				/* indirect - register d */
    /*TODO*///		EA=U+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///        break;
    /*TODO*///	case 0xe0:				/* register a */
    /*TODO*///		EA=S+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xe1:				/* register b */
    /*TODO*///		EA=S+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xe2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe6: EA=0; break; /* ???? */
    /*TODO*///	case 0xe7:				/* register d */
    /*TODO*///		EA=S+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xe8:				/* indirect - register a */
    /*TODO*///		EA=S+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xe9:				/* indirect - register b */
    /*TODO*///		EA=S+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xea: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xeb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xec: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xed: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xee: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xef:				/* indirect - register d */
    /*TODO*///		EA=S+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0xf0:				/* register a */
    /*TODO*///		EA=PC+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xf1:				/* register b */
    /*TODO*///		EA=PC+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xf2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf6: EA=0; break; /* ???? */
    /*TODO*///	case 0xf7:				/* register d */
    /*TODO*///		EA=PC+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xf8:				/* indirect - register a */
    /*TODO*///		EA=PC+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xf9:				/* indirect - register b */
    /*TODO*///		EA=PC+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xfa: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfe: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xff:				/* indirect - register d */
    /*TODO*///		EA=PC+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
	default:
            System.out.println("opcode2 ireg2 = 0x"+Integer.toHexString(ireg2)+ " ireg=0x"+Integer.toHexString(konami.ireg));
    /*TODO*///		if ( errorlog )
    /*TODO*///			fprintf( errorlog, "KONAMI: Unknown/Invalid postbyte at PC = %04x\n", PC -1 );
    /*TODO*///        EAD = 0;
    	}
        if(konami_indexed[konami.ireg]!=null)
        {
            konami_indexed[konami.ireg].handler();//(*konami_indexed[konami.ireg])();
        }
        else
        {
            System.out.println("Unsupported konami_indexed instruction 0x" + Integer.toHexString(konami.ireg));
        }	
    }};

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
