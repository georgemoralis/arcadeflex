package gr.codebb.arcadeflex.v036.cpu.m6809;

import arcadeflex.v036.mame.cpuintrfH.cpu_interface;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_setOPbase16;

/**
 *
 * @author shadow
 */
public class m6809 extends cpu_interface
{
    static FILE errorlog=null;
    public static FILE m6809log=null;//fopen("m6809.log", "wa");  //for debug purposes
    public int[] m6809_ICount={50000};
    public m6809()
    {
        cpu_num = CPU_M6809;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6809_INT_NONE;
        irq_int = M6809_INT_IRQ;
        nmi_int = M6809_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6809_ICount;
    
    
    }
    /* 6809 Registers */
    public static class m6809_Regs
    {
        public /*PAIR*/ char pc; 		/* Program counter */
        public /*PAIR*/ char ppc;		/* Previous program counter */
        public char a;
        public char b;   //PAIR	d;		/* Accumulator a and b */
        public /*PAIR*/ char dp; 		/* Direct Page register (page in MSB) */
        public char u;
        public char s;//PAIR	u, s;		/* Stack pointers */
        public char x;
        public char y;//PAIR	x, y;		/* Index registers */
        public char /*UINT8*/   cc;
        public int /*UINT8*/   ireg;		/* First opcode */
        public int[] /*UINT8*/   irq_state=new int[2];
        public    int     extra_cycles; /* cycles used up by interrupts */
        public irqcallbacksPtr irq_callback;
        public int /*UINT8*/   int_state;  /* SYNC and CWAI flags */
        public int /*UINT8*/   nmi_state;
    }
    int getDreg()//compose dreg
    {
         return m6809.a << 8 | m6809.b; 
    }
    void setDreg(int reg) //write to dreg
    { 
        m6809.a = (char)(reg >>> 8 & 0xFF);
        m6809.b = (char)(reg & 0xFF);
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
    
    /* 6809 registers */
    private static m6809_Regs m6809=new m6809_Regs();
    /*TODO*///int m6809_slapstic = 0;
    /*TODO*///
    /*TODO*///#define pPPC    m6809.ppc
    /*TODO*///#define pPC 	m6809.pc
    /*TODO*///#define pU		m6809.u
    /*TODO*///#define pS		m6809.s
    /*TODO*///#define pX		m6809.x
    /*TODO*///#define pY		m6809.y
    /*TODO*///#define pD		m6809.d
    /*TODO*///
    /*TODO*///#define	PPC		m6809.ppc.w.l
    /*TODO*///#define PC  	m6809.pc.w.l
    /*TODO*///#define PCD 	m6809.pc.d
    /*TODO*///#define U		m6809.u.w.l
    /*TODO*///#define UD		m6809.u.d
    /*TODO*///#define S		m6809.s.w.l
    /*TODO*///#define SD		m6809.s.d
    /*TODO*///#define X		m6809.x.w.l
    /*TODO*///#define XD		m6809.x.d
    /*TODO*///#define Y		m6809.y.w.l
    /*TODO*///#define YD		m6809.y.d
    /*TODO*///#define D   	m6809.d.w.l
    /*TODO*///#define A   	m6809.d.b.h
    /*TODO*///#define B		m6809.d.b.l
    /*TODO*///#define DP		m6809.dp.b.h
    /*TODO*///#define DPD 	m6809.dp.d
    /*TODO*///#define CC  	m6809.cc
    /*TODO*///
    /*TODO*///static PAIR ea;         /* effective address */
    /*TODO*///#define EA	ea.w.l
    /*TODO*///#define EAD ea.d
    public int ea;
    /*TODO*///
    public void CHANGE_PC()
    {
        change_pc16(m6809.pc & 0xFFFF);//ensure it's 16bit just in case
    }

    public static final int M6809_CWAI		=8;	/* set when CWAI is waiting for an interrupt */
    public static final int M6809_SYNC		=16;	/* set when SYNC is waiting for an interrupt */
    public static final int M6809_LDS		=32;	/* set when LDS occured at least once */
    
    public void CHECK_IRQ_LINES()
    {
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d irq_linesb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d S1:%d S2:%d\n", cpu_getactivecpu(),m6809.pc,m6809.ppc,m6809.a,m6809.b,getDreg(),m6809.dp,m6809.u,m6809.s,m6809.x,m6809.y,m6809.cc,ea,m6809.irq_state[M6809_IRQ_LINE],m6809.irq_state[M6809_FIRQ_LINE]);
 
    	if( m6809.irq_state[M6809_IRQ_LINE] != CLEAR_LINE || m6809.irq_state[M6809_FIRQ_LINE] != CLEAR_LINE )	
        {
    		m6809.int_state &= ~M6809_SYNC; /* clear SYNC flag */			
        }
    	if( m6809.irq_state[M6809_FIRQ_LINE]!=CLEAR_LINE && ((m6809.cc & CC_IF)==0) ) 
    	{																	
    		/* fast IRQ */													
    		/* HJB 990225: state already saved by CWAI? */					
    		if(( m6809.int_state & M6809_CWAI )!=0)								
    		{																
    			m6809.int_state &= ~M6809_CWAI;  /* clear CWAI */			
    			m6809.extra_cycles += 7;		 /* subtract +7 cycles */	
            }                                                               
    		else															
    		{																
    			m6809.cc &= ~CC_E;				/* save 'short' state */        
    			PUSHWORD(m6809.pc);												
    			PUSHBYTE(m6809.cc);												
    			m6809.extra_cycles += 10;	/* subtract +10 cycles */		
    		}																
    		m6809.cc |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */		
    		m6809.pc=(char)RM16(0xfff6);
    		CHANGE_PC();														
    		m6809.irq_callback.handler(M6809_FIRQ_LINE);					
    	}																	
        else if( m6809.irq_state[M6809_IRQ_LINE]!=CLEAR_LINE && ((m6809.cc & CC_II)==0) )	
    	{																	
    		/* standard IRQ */												
    		/* HJB 990225: state already saved by CWAI? */					
    		if(( m6809.int_state & M6809_CWAI )!=0)								
    		{																
    			m6809.int_state &= ~M6809_CWAI;  /* clear CWAI flag */		
    			m6809.extra_cycles += 7;		 /* subtract +7 cycles */	
    		}																
    		else															
    		{																
    			m6809.cc |= CC_E; 				/* save entire state */ 		
    			PUSHWORD(m6809.pc);												
    			PUSHWORD(m6809.u);												
    			PUSHWORD(m6809.y);												
    			PUSHWORD(m6809.x);												
    			PUSHBYTE(m6809.dp);												
    			PUSHBYTE(m6809.b);												
    			PUSHBYTE(m6809.a);												
    			PUSHBYTE(m6809.cc);												
    			m6809.extra_cycles += 19;	 /* subtract +19 cycles */		
    		}																
    		m6809.cc |= CC_II;					/* inhibit IRQ */				
    		m6809.pc=(char)RM16(0xfff8);
    		CHANGE_PC();														
    		m6809.irq_callback.handler(M6809_IRQ_LINE);					
    	}
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d irq_lines :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    
    
    public char RM(int addr)
    {
        return (char)((cpu_readmem16(addr) & 0xFF));
    }
    public void WM(int addr,int value)
    {
        cpu_writemem16(addr&0xFFFF,value&0xFF);
    }
    public char ROP(int addr)
    {
    	return cpu_readop(addr);
    }
    public char ROP_ARG(int addr)
    {
    	return cpu_readop_arg(addr);
    }
    /*TODO*////* macros to access memory */
    /*TODO*///#define IMMBYTE(b)	b = ROP_ARG(PCD); PC++
    public char IMMBYTE()
    {
        int reg = ROP_ARG(m6809.pc); 
        m6809.pc = (char)(m6809.pc + 1);
        return (char)(reg & 0xFF);//insure it returns a 8bit value
    }
    /*TODO*///#define IMMWORD(w)	w.d = (ROP_ARG(PCD)<<8) | ROP_ARG((PCD+1)&0xffff); PC+=2
    public char IMMWORD()
    {
        int reg = (ROP_ARG(m6809.pc)<<8) | ROP_ARG((m6809.pc+1)&0xffff);
        m6809.pc = (char)(m6809.pc + 2);
        return (char)reg;
    }
    /*TODO*///
    /*TODO*///#define PUSHBYTE(b) --S; WM(SD,b)
    public void PUSHBYTE(int w)
    {
        m6809.s = (char)(m6809.s -1);
        WM(m6809.s,w);
    }
    /*TODO*///#define PUSHWORD(w) --S; WM(SD,w.b.l); --S; WM(SD,w.b.h)
    public void PUSHWORD(int w)
    {
        m6809.s = (char)(m6809.s -1);
        WM(m6809.s,w & 0xFF); 
        m6809.s = (char)(m6809.s -1);
        WM(m6809.s,w >>>8);
    }
    /*TODO*///#define PULLBYTE(b) b = RM(SD); S++
    public int PULLBYTE()
    {
        int b = RM(m6809.s);
        m6809.s = (char)(m6809.s +1);
        return b;
    }
    /*TODO*///#define PULLWORD(w) w = RM(SD)<<8; S++; w |= RM(SD); S++
    public int PULLWORD()//TODO recheck
    {
        int w = RM(m6809.s)<<8;
        m6809.s = (char)(m6809.s +1);
        w |= RM(m6809.s);
        m6809.s = (char)(m6809.s +1);
        return w;
    }
    
    /*TODO*///#define PSHUBYTE(b) --U; WM(UD,b);
    public void PSHUBYTE(int w)
    {
        m6809.u = (char)(m6809.u -1);
        WM(m6809.u,w);
    }
    public void PSHUWORD(int w)
    {
        m6809.u = (char)(m6809.u -1);
        WM(m6809.u,w & 0xFF); 
        m6809.u = (char)(m6809.u -1);
        WM(m6809.u,w >>>8);
    }
    /*TODO*///#define PSHUWORD(w) --U; WM(UD,w.b.l); --U; WM(UD,w.b.h)
    /*TODO*///#define PULUBYTE(b) b = RM(UD); U++
    public int PULUBYTE()
    {
        int b = RM(m6809.u);
        m6809.u = (char)(m6809.u +1);
        return b;
    }
    /*TODO*///#define PULUWORD(w) w = RM(UD)<<8; U++; w |= RM(UD); U++
    public int PULUWORD()//TODO recheck
    {
        int w = RM(m6809.u)<<8;
        m6809.u = (char)(m6809.u +1);
        w |= RM(m6809.u);
        m6809.u = (char)(m6809.u +1);
        return w;
    }
    
    public void CLR_HNZVC()    {m6809.cc&=~(CC_H|CC_N|CC_Z|CC_V|CC_C);}
    public void CLR_NZV() { 	m6809.cc&=~(CC_N|CC_Z|CC_V); }
    /*TODO*///#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
    public void CLR_NZVC()	{ m6809.cc &=~(CC_N|CC_Z|CC_V|CC_C); }
    public void CLR_Z()		{ m6809.cc &=~(CC_Z); }
    public void CLR_NZC() 	{ m6809.cc&=~(CC_N|CC_Z|CC_C);}
    public void CLR_ZC()	{ m6809.cc&=~(CC_Z|CC_C); }
    
    /* macros for CC -- CC bits affected should be reset before calling */
    public void SET_Z(int a)		{if(a==0) SEZ();}
    public void SET_Z8(int a)		{SET_Z(a&0xFF);}
    public void SET_Z16(int a)		{SET_Z(a&0xFFFF);}
    public void SET_N8(int a)		{m6809.cc |=((a&0x80)>>4);}
    public void SET_N16(int a)		{m6809.cc|=((a&0x8000)>>12);}
    public void SET_H(int a,int b,int r){m6809.cc|=(((a^b^r)&0x10)<<1);}
    public void SET_C8(int a)		{m6809.cc|=((a&0x100)>>8);}
    public void SET_C16(int a)		{m6809.cc|=((a&0x10000)>>16);}
    public void SET_V8(int a,int b,int r){m6809.cc|=(((a^b^r^(r>>1))&0x80)>>6);}
    public void SET_V16(int a,int b,int r){m6809.cc|=(((a^b^r^(r>>1))&0x8000)>>14);}

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
    public void SET_FLAGS8I(int a)	{m6809.cc|=flags8i[(a)&0xff];}
    public void SET_FLAGS8D(int a)	{m6809.cc |=flags8d[(a)&0xff];}
    
    /*TODO*////* combos */
    public void SET_NZ8(int a)			{SET_N8(a);SET_Z(a);}
    public void SET_NZ16(int a)			{SET_N16(a);SET_Z(a);}
    public void SET_FLAGS8(int a,int b,int r)	{SET_N8(r);SET_Z8(r);SET_V8(a,b,r);SET_C8(r);}
    public void SET_FLAGS16(int a,int b,int r)	{SET_N16(r);SET_Z16(r);SET_V16(a,b,r);SET_C16(r);}
    /*TODO*///
    /*TODO*////* for treating an unsigned byte as a signed word */
    /*TODO*///#define SIGNED(b) ((UINT16)(b&0x80?b|0xff00:b))
    /*TODO*///
    /*TODO*////* macros for addressing modes (postbytes have their own code) */
    /*TODO*///#define DIRECT	EAD = DPD; IMMBYTE(ea.b.l)
    public void DIRECT()//TODO rececheck!
    {
        ea=IMMBYTE();
        ea |= m6809.dp <<8;  
    }
    /*TODO*///#define IMM8	EAD = PCD; PC++
    /*TODO*///#define IMM16	EAD = PCD; PC+=2
    public void EXTENDED(){ ea=IMMWORD();}
    /*TODO*///
    /*TODO*////* macros to set status flags */
    public void  SEC() {m6809.cc|=CC_C;}
    /*TODO*///#define CLC CC&=~CC_C
    public void  SEZ() { m6809.cc |=CC_Z;  }
    /*TODO*///#define CLZ CC&=~CC_Z
    /*TODO*///#define SEN CC|=CC_N
    /*TODO*///#define CLN CC&=~CC_N
    /*TODO*///#define SEV CC|=CC_V
    /*TODO*///#define CLV CC&=~CC_V
    /*TODO*///#define SEH CC|=CC_H
    /*TODO*///#define CLH CC&=~CC_H
    /*TODO*///
    /*TODO*////* macros for convenience */
    /*TODO*///#define DIRBYTE(b) {DIRECT;b=RM(EAD);}
    public char DIRBYTE()
    {
        DIRECT();
        return RM(ea);
    }
    /*TODO*///#define DIRWORD(w) {DIRECT;w.d=RM16(EAD);}
    public char DIRWORD()
    {
        DIRECT();
        return RM16(ea);
    }
    /*TODO*///#define EXTBYTE(b) {EXTENDED;b=RM(EAD);}
    public char EXTBYTE()
    {
        EXTENDED();
        return RM(ea);
    }
    /*TODO*///#define EXTWORD(w) {EXTENDED;w.d=RM16(EAD);}
    public char EXTWORD()
    {
        EXTENDED();
        return RM16(ea);
    }
    /*TODO*///
    /*TODO*////* macros for branch instructions */
    /*TODO*///#define BRANCH(f) { 					\
    /*TODO*///	UINT8 t;							\
    /*TODO*///	IMMBYTE(t); 						\
    /*TODO*///	if( f ) 							\
    /*TODO*///	{									\
    /*TODO*///		PC += SIGNED(t);				\
    /*TODO*///		CHANGE_PC;						\
    /*TODO*///	}									\
    /*TODO*///}
    public void BRANCH(boolean f)
    {
        int t= IMMBYTE();
        if(f)
        {
            m6809.pc=(char)(m6809.pc+(byte)t);//TODO check if it has to be better...
            CHANGE_PC();
        }
    }
    /*TODO*///
    /*TODO*///#define LBRANCH(f) {                    \
    /*TODO*///	PAIR t; 							\
    /*TODO*///	IMMWORD(t); 						\
    /*TODO*///	if( f ) 							\
    /*TODO*///	{									\
    /*TODO*///		m6809_ICount -= 1;				\
    /*TODO*///		PC += t.w.l;					\
    /*TODO*///		CHANGE_PC;						\
    /*TODO*///	}									\
    /*TODO*///}
    /*TODO*///
    public void LBRANCH(boolean f)
    {
        int t= IMMWORD();
        if(f)
        {
            m6809_ICount[0] -= 1;
            m6809.pc = (char)(m6809.pc + t);
            CHANGE_PC();
        }
    }
    public int NXORV()  { return ((m6809.cc&CC_N)^((m6809.cc&CC_V)<<2)); }
    /*TODO*///
    /*TODO*////* macros for setting/getting registers in TFR/EXG instructions */
    /*TODO*///
    /*TODO*///#if (!BIG_SWITCH)
    /*TODO*////* timings for 1-byte opcodes */
    /*TODO*///static UINT8 cycles1[] =
    /*TODO*///{
    /*TODO*///	/*	 0	1  2  3  4	5  6  7  8	9  A  B  C	D  E  F */
    /*TODO*///  /*0*/  6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 3, 6,
    /*TODO*///  /*1*/  0, 0, 2, 4, 0, 0, 5, 9, 0, 2, 3, 0, 3, 2, 8, 6,
    /*TODO*///  /*2*/  3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    /*TODO*///  /*3*/  4, 4, 4, 4, 5, 5, 5, 5, 0, 5, 3, 6,20,11, 0,19,
    /*TODO*///  /*4*/  2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
    /*TODO*///  /*5*/  2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
    /*TODO*///  /*6*/  6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 3, 6,
    /*TODO*///  /*7*/  7, 0, 0, 7, 7, 0, 7, 7, 7, 7, 7, 0, 7, 7, 4, 7,
    /*TODO*///  /*8*/  2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 4, 7, 3, 0,
    /*TODO*///  /*9*/  4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 6, 7, 5, 5,
    /*TODO*///  /*A*/  4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 6, 7, 5, 5,
    /*TODO*///  /*B*/  5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 5, 7, 8, 6, 6,
    /*TODO*///  /*C*/  2, 2, 2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 3, 0, 3, 3,
    /*TODO*///  /*D*/  4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
    /*TODO*///  /*E*/  4, 4, 4, 6, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
    /*TODO*///  /*F*/  5, 5, 5, 7, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6
    /*TODO*///};
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///INLINE UINT32 RM16( UINT32 Addr )
    /*TODO*///{
    /*TODO*///	UINT32 result = RM(Addr) << 8;
    /*TODO*///	return result | RM((Addr+1)&0xffff);
    /*TODO*///}
    char RM16(int addr)
    {
        /*int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i;*/
        int temp = RM(addr & 0xffff) << 8;
        temp = temp | RM((addr & 0xffff) + 1);
        return (char)temp;
    }
    /*TODO*///
    /*TODO*///INLINE void WM16( UINT32 Addr, PAIR *p )
    /*TODO*///{
    /*TODO*///	WM( Addr, p->b.h );
    /*TODO*///	WM( (Addr+1)&0xffff, p->b.l );
    /*TODO*///}
    /*TODO*///
    void WM16(int addr,int reg)
    {
        /*WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);*/
        WM((addr + 1)&0xffff , reg & 0xff);
        WM(addr & 0xffff, (reg >>> 8) & 0xff);
    }
    @Override
    public Object init_context() {
       Object reg = new m6809_Regs();
       return reg;
    }
    /****************************************************************************
    * Get all registers in given buffer
     ****************************************************************************/
    @Override
    public Object get_context() {
        m6809_Regs regs=new m6809_Regs();
        regs.pc=m6809.pc;
        regs.ppc=m6809.ppc;
        regs.a=m6809.a;
        regs.b=m6809.b;
        regs.dp=m6809.dp;
        regs.u=m6809.u;
        regs.s=m6809.s;
        regs.x=m6809.x;
        regs.y=m6809.y;
        regs.cc=m6809.cc;
        regs.ireg=m6809.ireg;		
        regs.irq_state=m6809.irq_state;
        regs.extra_cycles=m6809.extra_cycles;
        regs.irq_callback=m6809.irq_callback;
        regs.int_state=m6809.int_state;
        regs.nmi_state=m6809.nmi_state;
        return regs;
    }
    /****************************************************************************
     * Set all registers to given values
    / ****************************************************************************/
    @Override
    public void set_context(Object reg) {
        m6809_Regs Regs = (m6809_Regs)reg;
        m6809.pc=Regs.pc;
        m6809.ppc=Regs.ppc;
        m6809.a=Regs.a;
        m6809.b=Regs.b;
        m6809.dp=Regs.dp;
        m6809.u=Regs.u;
        m6809.s= Regs.s;
        m6809.x=Regs.x;
        m6809.y=Regs.y;
        m6809.cc=Regs.cc;
        m6809.ireg=Regs.ireg;		
        m6809.irq_state=Regs.irq_state;
        m6809.extra_cycles=Regs.extra_cycles;
        m6809.irq_callback=Regs.irq_callback;
        m6809.int_state=Regs.int_state;
        m6809.nmi_state=Regs.nmi_state;
        
        CHANGE_PC();
       CHECK_IRQ_LINES();
    }

    /*TODO*///void m6809_set_context(void *src)
    /*TODO*///{
    /*TODO*///	if( src )
    /*TODO*///		m6809 = *(m6809_Regs*)src;
    /*TODO*///	
    /*TODO*///
    /*TODO*///    
    /*TODO*///}
    /*TODO*///
    /****************************************************************************
     * Return program counter
     ****************************************************************************/
    @Override
    public int get_pc() {
        return m6809.pc & 0xFFFF;
    }
    /*TODO*///
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set program counter
    /*TODO*/// ****************************************************************************/
    /*TODO*///void m6809_set_pc(unsigned val)
    /*TODO*///{
    /*TODO*///	PC = val;
    /*TODO*///	CHANGE_PC;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return stack pointer
    /*TODO*/// ****************************************************************************/
    /*TODO*///unsigned m6809_get_sp(void)
    /*TODO*///{
    /*TODO*///	return S;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Set stack pointer
    /*TODO*/// ****************************************************************************/
    /*TODO*///void m6809_set_sp(unsigned val)
    /*TODO*///{
    /*TODO*///	S = val;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Return a specific register                                               */
    /*TODO*////****************************************************************************/
    @Override
    public int get_reg(int regnum) {
    	switch( regnum )
    	{
    		case M6809_PC: return m6809.pc;
    		case M6809_S: return m6809.s;
    		case M6809_CC: return m6809.cc;
    		case M6809_U: return m6809.u;
    		case M6809_A: return m6809.a;
    		case M6809_B: return m6809.b;
    		case M6809_X: return m6809.x;
    		case M6809_Y: return m6809.y;
    		case M6809_DP: return m6809.dp;
    /*TODO*///		case M6809_NMI_STATE: return m6809.nmi_state;
    /*TODO*///		case M6809_IRQ_STATE: return m6809.irq_state[M6809_IRQ_LINE];
    /*TODO*///		case M6809_FIRQ_STATE: return m6809.irq_state[M6809_FIRQ_LINE];
    		case REG_PREVIOUSPC: return m6809.ppc;
    		default:
                throw new UnsupportedOperationException("Not supported");
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				if( offset < 0xffff )
    /*TODO*///					return ( RM( offset ) << 8 ) | RM( offset + 1 );
    /*TODO*///			}
    	}
    /*TODO*///	return 0;
    }


    /****************************************************************************/
    /* Set a specific register                                                  */
    /****************************************************************************/
    @Override
    public void set_reg(int regnum, int val) {
            	switch( regnum )
    	{
    /*TODO*///		case M6809_PC: PC = val; CHANGE_PC; break;
    /*TODO*///		case M6809_S: S = val; break;
    /*TODO*///		case M6809_CC: CC = val; CHECK_IRQ_LINES; break;
    /*TODO*///		case M6809_U: U = val; break;
    /*TODO*///		case M6809_A: A = val; break;
    /*TODO*///		case M6809_B: B = val; break;
    /*TODO*///		case M6809_X: X = val; break;
    		case M6809_Y: m6809.y = (char)((val&0xFFFF)); break;
    /*TODO*///		case M6809_DP: DP = val; break;
    /*TODO*///		case M6809_NMI_STATE: m6809.nmi_state = val; break;
    /*TODO*///		case M6809_IRQ_STATE: m6809.irq_state[M6809_IRQ_LINE] = val; break;
    /*TODO*///		case M6809_FIRQ_STATE: m6809.irq_state[M6809_FIRQ_LINE] = val; break;
    		default:
                throw new UnsupportedOperationException("Not supported");
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				if( offset < 0xffff )
    /*TODO*///				{
    /*TODO*///					WM( offset, (val >> 8) & 0xff );
    /*TODO*///					WM( offset+1, val & 0xff );
    /*TODO*///				}
    /*TODO*///			}
        }
    }

    /****************************************************************************/
    /* Reset registers to their initial values									*/
    /****************************************************************************/
    @Override
    public void reset(Object param) {
    	m6809.int_state = 0;
    	m6809.nmi_state = CLEAR_LINE;
    	m6809.irq_state[0] = CLEAR_LINE;
    	m6809.irq_state[0] = CLEAR_LINE;
    
    	m6809.dp = 0;			/* Reset direct page register */
    
        m6809.cc |= CC_II;        /* IRQ disabled */
        m6809.cc |= CC_IF;        /* FIRQ disabled */
    
        m6809.pc = (char)(RM16(0xfffe));
    	CHANGE_PC();
    }
    /*TODO*///
    /*TODO*///void m6809_exit(void)
    /*TODO*///{
    /*TODO*///	/* nothing to do ? */
    /*TODO*///}
    /* Generate interrupts */
    /****************************************************************************
     * Set NMI line state
     ****************************************************************************/
    @Override
    public void set_nmi_line(int state) {
    	if (m6809.nmi_state == state) return;
    	m6809.nmi_state = state;
    	if(errorlog!=null) fprintf(errorlog, "M6809#%d set_nmi_line %d\n", cpu_getactivecpu(), state);
    	if( state == CLEAR_LINE ) return;
    
    	/* if the stack was not yet initialized */
        if( (m6809.int_state & M6809_LDS)==0 ) return;
    
        m6809.int_state &= ~M6809_SYNC;
    	/* HJB 990225: state already saved by CWAI? */
    	if(( m6809.int_state & M6809_CWAI )!=0)
    	{
    		m6809.int_state &= ~M6809_CWAI;
    		m6809.extra_cycles += 7;	/* subtract +7 cycles next time */
        }
    	else
    	{
    		m6809.cc |= CC_E; 				/* save entire state */
    		PUSHWORD(m6809.pc);
    		PUSHWORD(m6809.u);
    		PUSHWORD(m6809.y);
    		PUSHWORD(m6809.x);
    		PUSHBYTE(m6809.dp);
    		PUSHBYTE(m6809.b);
    		PUSHBYTE(m6809.a);
    		PUSHBYTE(m6809.cc);
    		m6809.extra_cycles += 19;	/* subtract +19 cycles next time */
    	}
    	m6809.cc |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
    	m6809.pc = (char)(RM16(0xfffc));
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d nmi_line :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /****************************************************************************
     * Set IRQ line state
     ****************************************************************************/
    @Override
    public void set_irq_line(int irqline, int state) {
        if(errorlog!=null) fprintf(errorlog, "M6809#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, state);
    	m6809.irq_state[irqline] = state;
    	if (state == CLEAR_LINE) return;
    	CHECK_IRQ_LINES();
    }
    /****************************************************************************
     * Set IRQ vector callback
     ****************************************************************************/
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        m6809.irq_callback = callback;
    }
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Save CPU state
    /*TODO*/// ****************************************************************************/
    /*TODO*///static void state_save(void *file, const char *module)
    /*TODO*///{
    /*TODO*///	int cpu_old = cpu_getactivecpu();
    /*TODO*///	state_save_UINT16(file, module, cpu_old, "PC", &PC, 1);
    /*TODO*///	state_save_UINT16(file, module, cpu_old, "U", &U, 1);
    /*TODO*///	state_save_UINT16(file, module, cpu_old, "S", &S, 1);
    /*TODO*///	state_save_UINT16(file, module, cpu_old, "X", &X, 1);
    /*TODO*///	state_save_UINT16(file, module, cpu_old, "Y", &Y, 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "DP", &DP, 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "CC", &CC, 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "INT", &m6809.int_state, 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "NMI", &m6809.nmi_state, 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "IRQ", &m6809.irq_state[0], 1);
    /*TODO*///	state_save_UINT8(file, module, cpu_old, "FIRQ", &m6809.irq_state[1], 1);
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Load CPU state
    /*TODO*/// ****************************************************************************/
    /*TODO*///static void state_load(void *file, const char *module)
    /*TODO*///{
    /*TODO*///	int cpu_old = cpu_getactivecpu();
    /*TODO*///	state_load_UINT16(file, module, cpu_old, "PC", &PC, 1);
    /*TODO*///	state_load_UINT16(file, module, cpu_old, "U", &U, 1);
    /*TODO*///	state_load_UINT16(file, module, cpu_old, "S", &S, 1);
    /*TODO*///	state_load_UINT16(file, module, cpu_old, "X", &X, 1);
    /*TODO*///	state_load_UINT16(file, module, cpu_old, "Y", &Y, 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "DP", &DP, 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "CC", &CC, 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "INT", &m6809.int_state, 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "NMI", &m6809.nmi_state, 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "IRQ", &m6809.irq_state[0], 1);
    /*TODO*///	state_load_UINT8(file, module, cpu_old, "FIRQ", &m6809.irq_state[1], 1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void m6809_state_save(void *file) { state_save(file, "m6809"); }
    /*TODO*///void m6809_state_load(void *file) { state_load(file, "m6809"); }
    /*TODO*///
    /*TODO*////****************************************************************************
    /*TODO*/// * Return a formatted string for a register
    /*TODO*/// ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) {
    /*TODO*///const char *m6809_info(void *context, int regnum)
    /*TODO*///{
    /*TODO*///	static char buffer[16][47+1];
    /*TODO*///	static int which = 0;
    /*TODO*///	m6809_Regs *r = context;
    /*TODO*///
    /*TODO*///	which = ++which % 16;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///	if( !context )
    /*TODO*///		r = &m6809;
    /*TODO*///
        switch( regnum )
        {
    		case CPU_INFO_NAME: return "M6809";
    		case CPU_INFO_FAMILY: return "Motorola 6809";
    		case CPU_INFO_VERSION: return "1.1";
    		case CPU_INFO_FILE: return "m6809.java";
    		case CPU_INFO_CREDITS: return "Copyright (C) John Butler 1997";
    /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)m6809_reg_layout;
    /*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)m6809_win_layout;
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
    /*TODO*///		case CPU_INFO_REG+M6809_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
    /*TODO*///		case CPU_INFO_REG+M6809_U: sprintf(buffer[which], "U:%04X", r->u.w.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_A: sprintf(buffer[which], "A:%02X", r->d.b.h); break;
    /*TODO*///		case CPU_INFO_REG+M6809_B: sprintf(buffer[which], "B:%02X", r->d.b.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_Y: sprintf(buffer[which], "Y:%04X", r->y.w.l); break;
    /*TODO*///		case CPU_INFO_REG+M6809_DP: sprintf(buffer[which], "DP:%02X", r->dp.b.h); break;
    /*TODO*///		case CPU_INFO_REG+M6809_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///		case CPU_INFO_REG+M6809_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[M6809_IRQ_LINE]); break;
    /*TODO*///		case CPU_INFO_REG+M6809_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r->irq_state[M6809_FIRQ_LINE]); break;
    	}
    /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("unsupported m6809 cpu_info");
    }
    /* execute instructions on this CPU until icount expires */
    @Override
    public int execute(int cycles) {
        m6809_ICount[0] = cycles - m6809.extra_cycles;
    	m6809.extra_cycles = 0;
    
    	if ((m6809.int_state & (M6809_CWAI | M6809_SYNC))!=0)
    	{
    		m6809_ICount[0] = 0;
    	}
    	else
    	{
    		do
    		{
    			m6809.ppc = m6809.pc;
    
    			m6809.ireg = ROP(m6809.pc);
    			m6809.pc = (char)(m6809.pc + 1);
                switch( m6809.ireg )
    		{
    			case 0x00: neg_di();   m6809_ICount[0]-= 6; break;
    			case 0x01: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x02: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x03: com_di();   m6809_ICount[0]-= 6; break;
    			case 0x04: lsr_di();   m6809_ICount[0]-= 6; break;
    			case 0x05: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x06: ror_di();   m6809_ICount[0]-= 6; break;
    			case 0x07: asr_di();   m6809_ICount[0]-= 6; break;
    			case 0x08: asl_di();   m6809_ICount[0]-= 6; break;
    			case 0x09: rol_di();   m6809_ICount[0]-= 6; break;
    			case 0x0a: dec_di();   m6809_ICount[0]-= 6; break;
    			case 0x0b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x0c: inc_di();   m6809_ICount[0]-= 6; break;
    			case 0x0d: tst_di();   m6809_ICount[0]-= 6; break;
    			case 0x0e: jmp_di();   m6809_ICount[0]-= 3; break;
    			case 0x0f: clr_di();   m6809_ICount[0]-= 6; break;
    			case 0x10: pref10();					 break;
    			case 0x11: pref11();					 break;
    			case 0x12: nop();	   m6809_ICount[0]-= 2; break;
    			case 0x13: sync();	   m6809_ICount[0]-= 4; break;
    			case 0x14: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x15: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x16: lbra();	   m6809_ICount[0]-= 5; break;
    			case 0x17: lbsr();	   m6809_ICount[0]-= 9; break;
    			case 0x18: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x19: daa();	   m6809_ICount[0]-= 2; break;
    			case 0x1a: orcc();	   m6809_ICount[0]-= 3; break;
    			case 0x1b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x1c: andcc();    m6809_ICount[0]-= 3; break;
    			case 0x1d: sex();	   m6809_ICount[0]-= 2; break;
    			case 0x1e: exg();	   m6809_ICount[0]-= 8; break;
    			case 0x1f: tfr();	   m6809_ICount[0]-= 6; break;
    			case 0x20: bra();	   m6809_ICount[0]-= 3; break;
    			case 0x21: brn();	   m6809_ICount[0]-= 3; break;
    			case 0x22: bhi();	   m6809_ICount[0]-= 3; break;
    			case 0x23: bls();	   m6809_ICount[0]-= 3; break;
    			case 0x24: bcc();	   m6809_ICount[0]-= 3; break;
    			case 0x25: bcs();	   m6809_ICount[0]-= 3; break;
    			case 0x26: bne();	   m6809_ICount[0]-= 3; break;
    			case 0x27: beq();	   m6809_ICount[0]-= 3; break;
    			case 0x28: bvc();	   m6809_ICount[0]-= 3; break;
    			case 0x29: bvs();	   m6809_ICount[0]-= 3; break;
    			case 0x2a: bpl();	   m6809_ICount[0]-= 3; break;
    			case 0x2b: bmi();	   m6809_ICount[0]-= 3; break;
    			case 0x2c: bge();	   m6809_ICount[0]-= 3; break;
    			case 0x2d: blt();	   m6809_ICount[0]-= 3; break;
    			case 0x2e: bgt();	   m6809_ICount[0]-= 3; break;
    			case 0x2f: ble();	   m6809_ICount[0]-= 3; break;
    			case 0x30: leax();	   m6809_ICount[0]-= 4; break;
    			case 0x31: leay();	   m6809_ICount[0]-= 4; break;
    			case 0x32: leas();	   m6809_ICount[0]-= 4; break;
    			case 0x33: leau();	   m6809_ICount[0]-= 4; break;
    			case 0x34: pshs();	   m6809_ICount[0]-= 5; break;
    			case 0x35: puls();	   m6809_ICount[0]-= 5; break;
    			case 0x36: pshu();	   m6809_ICount[0]-= 5; break;
    			case 0x37: pulu();	   m6809_ICount[0]-= 5; break;
    			case 0x38: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x39: rts();	   m6809_ICount[0]-= 5; break;
    			case 0x3a: abx();	   m6809_ICount[0]-= 3; break;
    			case 0x3b: rti();	   m6809_ICount[0]-= 6; break;
    			case 0x3c: cwai();	   m6809_ICount[0]-=20; break;
    			case 0x3d: mul();	   m6809_ICount[0]-=11; break;
    			case 0x3e: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x3f: swi();	   m6809_ICount[0]-=19; break;
    			case 0x40: nega();	   m6809_ICount[0]-= 2; break;
    			case 0x41: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x42: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x43: coma();	   m6809_ICount[0]-= 2; break;
    			case 0x44: lsra();	   m6809_ICount[0]-= 2; break;
    			case 0x45: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x46: rora();	   m6809_ICount[0]-= 2; break;
    			case 0x47: asra();	   m6809_ICount[0]-= 2; break;
    			case 0x48: asla();	   m6809_ICount[0]-= 2; break;
                        case 0x49: rola();	   m6809_ICount[0]-= 2; break;
    			case 0x4a: deca();	   m6809_ICount[0]-= 2; break;
    			case 0x4b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x4c: inca();	   m6809_ICount[0]-= 2; break;
    			case 0x4d: tsta();	   m6809_ICount[0]-= 2; break;
    			case 0x4e: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x4f: clra();	   m6809_ICount[0]-= 2; break;
    			case 0x50: negb();	   m6809_ICount[0]-= 2; break;
    			case 0x51: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x52: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x53: comb();	   m6809_ICount[0]-= 2; break;
    			case 0x54: lsrb();	   m6809_ICount[0]-= 2; break;
    			case 0x55: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x56: rorb();	   m6809_ICount[0]-= 2; break;
    			case 0x57: asrb();	   m6809_ICount[0]-= 2; break;
    			case 0x58: aslb();	   m6809_ICount[0]-= 2; break;
    			case 0x59: rolb();	   m6809_ICount[0]-= 2; break;
    			case 0x5a: decb();	   m6809_ICount[0]-= 2; break;
    			case 0x5b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x5c: incb();	   m6809_ICount[0]-= 2; break;
    			case 0x5d: tstb();	   m6809_ICount[0]-= 2; break;
    			case 0x5e: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x5f: clrb();	   m6809_ICount[0]-= 2; break;
    			case 0x60: neg_ix();   m6809_ICount[0]-= 6; break;
    			case 0x61: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x62: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x63: com_ix();   m6809_ICount[0]-= 6; break;
    			case 0x64: lsr_ix();   m6809_ICount[0]-= 6; break;
    			case 0x65: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x66: ror_ix();   m6809_ICount[0]-= 6; break;
    			case 0x67: asr_ix();   m6809_ICount[0]-= 6; break;
    			case 0x68: asl_ix();   m6809_ICount[0]-= 6; break;
    			case 0x69: rol_ix();   m6809_ICount[0]-= 6; break;
    			case 0x6a: dec_ix();   m6809_ICount[0]-= 6; break;
    			case 0x6b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x6c: inc_ix();   m6809_ICount[0]-= 6; break;
    			case 0x6d: tst_ix();   m6809_ICount[0]-= 6; break;
    			case 0x6e: jmp_ix();   m6809_ICount[0]-= 3; break;
    			case 0x6f: clr_ix();   m6809_ICount[0]-= 6; break;
    			case 0x70: neg_ex();   m6809_ICount[0]-= 7; break;
    			case 0x71: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x72: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x73: com_ex();   m6809_ICount[0]-= 7; break;
    			case 0x74: lsr_ex();   m6809_ICount[0]-= 7; break;
    			case 0x75: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x76: ror_ex();   m6809_ICount[0]-= 7; break;
    			case 0x77: asr_ex();   m6809_ICount[0]-= 7; break;
    			case 0x78: asl_ex();   m6809_ICount[0]-= 7; break;
    			case 0x79: rol_ex();   m6809_ICount[0]-= 7; break;
    			case 0x7a: dec_ex();   m6809_ICount[0]-= 7; break;
    			case 0x7b: illegal();  m6809_ICount[0]-= 2; break;
    			case 0x7c: inc_ex();   m6809_ICount[0]-= 7; break;
    			case 0x7d: tst_ex();   m6809_ICount[0]-= 7; break;
    			case 0x7e: jmp_ex();   m6809_ICount[0]-= 4; break;
    			case 0x7f: clr_ex();   m6809_ICount[0]-= 7; break;
    			case 0x80: suba_im();  m6809_ICount[0]-= 2; break;
    			case 0x81: cmpa_im();  m6809_ICount[0]-= 2; break;
    			case 0x82: sbca_im();  m6809_ICount[0]-= 2; break;
    			case 0x83: subd_im();  m6809_ICount[0]-= 4; break;
    			case 0x84: anda_im();  m6809_ICount[0]-= 2; break;
    			case 0x85: bita_im();  m6809_ICount[0]-= 2; break;
    			case 0x86: lda_im();   m6809_ICount[0]-= 2; break;
    /*TODO*///			case 0x87: sta_im();   m6809_ICount[0]-= 2; break;
    			case 0x88: eora_im();  m6809_ICount[0]-= 2; break;
    			case 0x89: adca_im();  m6809_ICount[0]-= 2; break;
    			case 0x8a: ora_im();   m6809_ICount[0]-= 2; break;
    			case 0x8b: adda_im();  m6809_ICount[0]-= 2; break;
    			case 0x8c: cmpx_im();  m6809_ICount[0]-= 4; break;
    			case 0x8d: bsr();	   m6809_ICount[0]-= 7; break;
    			case 0x8e: ldx_im();   m6809_ICount[0]-= 3; break;
    /*TODO*///			case 0x8f: stx_im();   m6809_ICount[0]-= 2; break;
    			case 0x90: suba_di();  m6809_ICount[0]-= 4; break;
    			case 0x91: cmpa_di();  m6809_ICount[0]-= 4; break;
    			case 0x92: sbca_di();  m6809_ICount[0]-= 4; break;
    			case 0x93: subd_di();  m6809_ICount[0]-= 6; break;
    			case 0x94: anda_di();  m6809_ICount[0]-= 4; break;
    			case 0x95: bita_di();  m6809_ICount[0]-= 4; break;
   			    case 0x96: lda_di();   m6809_ICount[0]-= 4; break;
    			case 0x97: sta_di();   m6809_ICount[0]-= 4; break;
    			case 0x98: eora_di();  m6809_ICount[0]-= 4; break;
    			case 0x99: adca_di();  m6809_ICount[0]-= 4; break;
    			case 0x9a: ora_di();   m6809_ICount[0]-= 4; break;
    			case 0x9b: adda_di();  m6809_ICount[0]-= 4; break;
    			case 0x9c: cmpx_di();  m6809_ICount[0]-= 6; break;
    			case 0x9d: jsr_di();   m6809_ICount[0]-= 7; break;
    			case 0x9e: ldx_di();   m6809_ICount[0]-= 5; break;
    			case 0x9f: stx_di();   m6809_ICount[0]-= 5; break;
    			case 0xa0: suba_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa1: cmpa_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa2: sbca_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa3: subd_ix();  m6809_ICount[0]-= 6; break;
    			case 0xa4: anda_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa5: bita_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa6: lda_ix();   m6809_ICount[0]-= 4; break;
    			case 0xa7: sta_ix();   m6809_ICount[0]-= 4; break;
    			case 0xa8: eora_ix();  m6809_ICount[0]-= 4; break;
    			case 0xa9: adca_ix();  m6809_ICount[0]-= 4; break;
    			case 0xaa: ora_ix();   m6809_ICount[0]-= 4; break;
    			case 0xab: adda_ix();  m6809_ICount[0]-= 4; break;
    			case 0xac: cmpx_ix();  m6809_ICount[0]-= 6; break;
    			case 0xad: jsr_ix();   m6809_ICount[0]-= 7; break;
    			case 0xae: ldx_ix();   m6809_ICount[0]-= 5; break;
    			case 0xaf: stx_ix();   m6809_ICount[0]-= 5; break;
    			case 0xb0: suba_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb1: cmpa_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb2: sbca_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb3: subd_ex();  m6809_ICount[0]-= 7; break;
    			case 0xb4: anda_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb5: bita_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb6: lda_ex();   m6809_ICount[0]-= 5; break;
    			case 0xb7: sta_ex();   m6809_ICount[0]-= 5; break;
    			case 0xb8: eora_ex();  m6809_ICount[0]-= 5; break;
    			case 0xb9: adca_ex();  m6809_ICount[0]-= 5; break;
    			case 0xba: ora_ex();   m6809_ICount[0]-= 5; break;
    			case 0xbb: adda_ex();  m6809_ICount[0]-= 5; break;
    			case 0xbc: cmpx_ex();  m6809_ICount[0]-= 7; break;
    			case 0xbd: jsr_ex();   m6809_ICount[0]-= 8; break;
    			case 0xbe: ldx_ex();   m6809_ICount[0]-= 6; break;
    			case 0xbf: stx_ex();   m6809_ICount[0]-= 6; break;
    			case 0xc0: subb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc1: cmpb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc2: sbcb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc3: addd_im();  m6809_ICount[0]-= 4; break;
    			case 0xc4: andb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc5: bitb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc6: ldb_im();   m6809_ICount[0]-= 2; break;
    /*TODO*///			case 0xc7: stb_im();   m6809_ICount[0]-= 2; break;
    			case 0xc8: eorb_im();  m6809_ICount[0]-= 2; break;
    			case 0xc9: adcb_im();  m6809_ICount[0]-= 2; break;
    			case 0xca: orb_im();   m6809_ICount[0]-= 2; break;
    			case 0xcb: addb_im();  m6809_ICount[0]-= 2; break;
    			case 0xcc: ldd_im();   m6809_ICount[0]-= 3; break;
    /*TODO*///			case 0xcd: std_im();   m6809_ICount[0]-= 2; break;
    			case 0xce: ldu_im();   m6809_ICount[0]-= 3; break;
    /*TODO*///			case 0xcf: stu_im();   m6809_ICount[0]-= 3; break;
    			case 0xd0: subb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd1: cmpb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd2: sbcb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd3: addd_di();  m6809_ICount[0]-= 6; break;
    			case 0xd4: andb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd5: bitb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd6: ldb_di();   m6809_ICount[0]-= 4; break;
    			case 0xd7: stb_di();   m6809_ICount[0]-= 4; break;
    			case 0xd8: eorb_di();  m6809_ICount[0]-= 4; break;
    			case 0xd9: adcb_di();  m6809_ICount[0]-= 4; break;
    			case 0xda: orb_di();   m6809_ICount[0]-= 4; break;
    			case 0xdb: addb_di();  m6809_ICount[0]-= 4; break;
    			case 0xdc: ldd_di();   m6809_ICount[0]-= 5; break;
    			case 0xdd: std_di();   m6809_ICount[0]-= 5; break;
    			case 0xde: ldu_di();   m6809_ICount[0]-= 5; break;
    			case 0xdf: stu_di();   m6809_ICount[0]-= 5; break;
    			case 0xe0: subb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xe1: cmpb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xe2: sbcb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xe3: addd_ix();  m6809_ICount[0]-= 6; break;
    			case 0xe4: andb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xe5: bitb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xe6: ldb_ix();   m6809_ICount[0]-= 4; break;
    			case 0xe7: stb_ix();   m6809_ICount[0]-= 4; break;
    			case 0xe8: eorb_ix();  m6809_ICount[0]-= 4; break;
   			    case 0xe9: adcb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xea: orb_ix();   m6809_ICount[0]-= 4; break;
    			case 0xeb: addb_ix();  m6809_ICount[0]-= 4; break;
    			case 0xec: ldd_ix();   m6809_ICount[0]-= 5; break;
    			case 0xed: std_ix();   m6809_ICount[0]-= 5; break;
    			case 0xee: ldu_ix();   m6809_ICount[0]-= 5; break;
    			case 0xef: stu_ix();   m6809_ICount[0]-= 5; break;
    			case 0xf0: subb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf1: cmpb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf2: sbcb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf3: addd_ex();  m6809_ICount[0]-= 7; break;
    			case 0xf4: andb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf5: bitb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf6: ldb_ex();   m6809_ICount[0]-= 5; break;
    			case 0xf7: stb_ex();   m6809_ICount[0]-= 5; break;
    			case 0xf8: eorb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xf9: adcb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xfa: orb_ex();   m6809_ICount[0]-= 5; break;
    			case 0xfb: addb_ex();  m6809_ICount[0]-= 5; break;
    			case 0xfc: ldd_ex();   m6809_ICount[0]-= 6; break;
    			case 0xfd: std_ex();   m6809_ICount[0]-= 6; break;
    			case 0xfe: ldu_ex();   m6809_ICount[0]-= 6; break;
    			case 0xff: stu_ex();   m6809_ICount[0]-= 6; break;
                         default:
                            System.out.println("6809 opcode 0x" + Integer.toHexString(m6809.ireg));
                            //for debug
                            if(m6809log!=null){
                            fclose(m6809log);
                            System.exit(0);};
    			}
    
    		} while( m6809_ICount[0] > 0 );
    
            m6809_ICount[0] -= m6809.extra_cycles;
            m6809.extra_cycles = 0;
        }
    
        return cycles - m6809_ICount[0];   /* NS 970908 */
    }
    public void fetch_effective_address()
    {
       int postbyte = ROP_ARG(m6809.pc) & 0xFF;
       m6809.pc = (char)(m6809.pc +1);
    
    	switch(postbyte)
    	{
    	case 0x00: ea=m6809.x & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x01: ea=m6809.x+1 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x02: ea=m6809.x+2 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x03: ea=m6809.x+3 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x04: ea=m6809.x+4 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x05: ea=m6809.x+5 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x06: ea=m6809.x+6 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x07: ea=m6809.x+7 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x08: ea=m6809.x+8 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x09: ea=m6809.x+9 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x0a: ea=m6809.x+10 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x0b: ea=m6809.x+11 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x0c: ea=m6809.x+12 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x0d: ea=m6809.x+13 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x0e: ea=m6809.x+14 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x0f: ea=m6809.x+15 &0xFFFF; 											m6809_ICount[0]-=1;   break;
 
    	case 0x10: ea=m6809.x-16 &0xFFFF;											m6809_ICount[0]-=1;   break;
    	case 0x11: ea=m6809.x-15 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x12: ea=m6809.x-14 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x13: ea=m6809.x-13 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x14: ea=m6809.x-12 &0xFFFF;											m6809_ICount[0]-=1;   break;
    	case 0x15: ea=m6809.x-11 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x16: ea=m6809.x-10 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x17: ea=m6809.x-9 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x18: ea=m6809.x-8 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x19: ea=m6809.x-7 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1a: ea=m6809.x-6 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1b: ea=m6809.x-5 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1c: ea=m6809.x-4 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1d: ea=m6809.x-3 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1e: ea=m6809.x-2 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x1f: ea=m6809.x-1 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x20: ea=m6809.y & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x21: ea=m6809.y+1 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x22: ea=m6809.y+2 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x23: ea=m6809.y+3 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x24: ea=m6809.y+4 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x25: ea=m6809.y+5 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x26: ea=m6809.y+6 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x27: ea=m6809.y+7 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x28: ea=m6809.y+8 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x29: ea=m6809.y+9 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x2a: ea=m6809.y+10 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x2b: ea=m6809.y+11 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x2c: ea=m6809.y+12 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
        case 0x2d: ea=m6809.y+13 & 0xFFFF;											m6809_ICount[0]-=1;   break;
    	case 0x2e: ea=m6809.y+14 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x2f: ea=m6809.y+15 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    
    	case 0x30: ea=m6809.y-16 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x31: ea=m6809.y-15 & 0xFFFF;											m6809_ICount[0]-=1;   break;
    	case 0x32: ea=m6809.y-14 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
   	case 0x33: ea=m6809.y-13 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x34: ea=m6809.y-12 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x35: ea=m6809.y-11 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x36: ea=m6809.y-10 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x37: ea=m6809.y-9 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x38: ea=m6809.y-8 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x39: ea=m6809.y-7 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3a: ea=m6809.y-6 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3b: ea=m6809.y-5 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3c: ea=m6809.y-4 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3d: ea=m6809.y-3 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3e: ea=m6809.y-2&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x3f: ea=m6809.y-1&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x40: ea=m6809.u & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x41: ea=m6809.u+1 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x42: ea=m6809.u+2 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x43: ea=m6809.u+3 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x44: ea=m6809.u+4 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x45: ea=m6809.u+5 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x46: ea=m6809.u+6 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x47: ea=m6809.u+7 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x48: ea=m6809.u+8 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x49: ea=m6809.u+9 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x4a: ea=m6809.u+10 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x4b: ea=m6809.u+11 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x4c: ea=m6809.u+12 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x4d: ea=m6809.u+13 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x4e: ea=m6809.u+14 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x4f: ea=m6809.u+15 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    
    	case 0x50: ea=m6809.u-16 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x51: ea=m6809.u-15 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x52: ea=m6809.u-14 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x53: ea=m6809.u-13 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x54: ea=m6809.u-12 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x55: ea=m6809.u-11 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x56: ea=m6809.u-10 & 0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x57: ea=m6809.u-9 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x58: ea=m6809.u-8 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x59: ea=m6809.u-7 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5a: ea=m6809.u-6 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5b: ea=m6809.u-5 & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5c: ea=m6809.u-4 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5d: ea=m6809.u-3 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5e: ea=m6809.u-2 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x5f: ea=m6809.u-1 &0xFFFF;												m6809_ICount[0]-=1;   break;

    	case 0x60: ea=m6809.s & 0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x61: ea=m6809.s+1&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x62: ea=m6809.s+2&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x63: ea=m6809.s+3&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x64: ea=m6809.s+4&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x65: ea=m6809.s+5&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x66: ea=m6809.s+6&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x67: ea=m6809.s+7&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x68: ea=m6809.s+8&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x69: ea=m6809.s+9&0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x6a: ea=m6809.s+10&0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x6b: ea=m6809.s+11&0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x6c: ea=m6809.s+12&0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x6d: ea=m6809.s+13&0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x6e: ea=m6809.s+14&0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x6f: ea=m6809.s+15&0xFFFF;												m6809_ICount[0]-=1;   break;

    	case 0x70: ea=m6809.s-16 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x71: ea=m6809.s-15 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x72: ea=m6809.s-14 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x73: ea=m6809.s-13 &0xFFFF;											m6809_ICount[0]-=1;   break;
   	case 0x74: ea=m6809.s-12 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x75: ea=m6809.s-11 &0xFFFF; 											m6809_ICount[0]-=1;   break;
    	case 0x76: ea=m6809.s-10 &0xFFFF;											m6809_ICount[0]-=1;   break;
    	case 0x77: ea=m6809.s-9 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x78: ea=m6809.s-8 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x79: ea=m6809.s-7 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7a: ea=m6809.s-6 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7b: ea=m6809.s-5 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7c: ea=m6809.s-4 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7d: ea=m6809.s-3 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7e: ea=m6809.s-2 &0xFFFF;												m6809_ICount[0]-=1;   break;
    	case 0x7f: ea=m6809.s-1 &0xFFFF;												m6809_ICount[0]-=1;   break;
    
    	case 0x80: ea=m6809.x&0xFFFF;	m6809.x=(char)(m6809.x+1);										m6809_ICount[0]-=2;   break;
    	case 0x81: ea=m6809.x&0xFFFF;	m6809.x=(char)(m6809.x+2);										m6809_ICount[0]-=3;   break;
    	case 0x82: m6809.x=(char)(m6809.x-1); 	ea=m6809.x&0xFFFF;										m6809_ICount[0]-=2;   break;
    	case 0x83: m6809.x=(char)(m6809.x-2); 	ea=m6809.x&0xFFFF;										m6809_ICount[0]-=3;   break;
    	case 0x84: ea=m6809.x&0xFFFF;																   break;
    	case 0x85: ea = (m6809.x + (byte)m6809.b) & 0xFFFF;/*EA=X+SIGNED(B);*/										m6809_ICount[0]-=1;   break;
    	case 0x86: ea = (m6809.x + (byte)m6809.a) & 0xFFFF;/*EA=X+SIGNED(A);*/										m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0x87: EA=0;																   break; /*   ILLEGAL*/
    	case 0x88: ea=IMMBYTE(); 	ea=m6809.x+(byte)ea& 0xFFFF;					m6809_ICount[0]-=1;   break; /* this is a hack to make Vectrex work. It should be m6809_ICount[0]-=1. Dunno where the cycle was lost :( */
    	case 0x89: ea=IMMWORD(); 	ea = ea + m6809.x & 0xFFFF;								m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0x8a: EA=0;																   break; /*   ILLEGAL*/
    	case 0x8b: ea=m6809.x+getDreg() &0xFFFF;												m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0x8c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount[0]-=1;   break;
    	case 0x8d: ea=IMMWORD(); 	ea = ea + m6809.pc & 0xFFFF;				m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0x8e: EA=0;																   break; /*   ILLEGAL*/
    /*TODO*///	case 0x8f: IMMWORD(ea); 										m6809_ICount[0]-=5;   break;
    /*TODO*///
    	case 0x90: ea=m6809.x&0xFFFF;	m6809.x=(char)(m6809.x+1);						ea=RM16(ea);	m6809_ICount[0]-=5;   break; /* Indirect ,R+ not in my specs */
    	case 0x91: ea=m6809.x&0xFFFF;	m6809.x=(char)(m6809.x+2);						ea=RM16(ea);	m6809_ICount[0]-=6;   break;
    	case 0x92: m6809.x=(char)(m6809.x-1); 	ea=m6809.x;						ea=RM16(ea);	m6809_ICount[0]-=5;   break;
    	case 0x93: m6809.x=(char)(m6809.x-2);	ea=m6809.x;						ea=RM16(ea);	m6809_ICount[0]-=6;   break;
    	case 0x94: ea=m6809.x;								ea=RM16(ea);	m6809_ICount[0]-=3;   break;
    	case 0x95: ea = m6809.x + (byte)m6809.b & 0xFFFF;						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    	case 0x96: ea = m6809.x + (byte)m6809.a & 0xFFFF;						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0x97: EA=0;																   break; /*   ILLEGAL*/
    	case 0x98: ea=IMMBYTE(); 	ea=m6809.x+(byte)ea & 0xFFFF;	ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0x99: IMMWORD(ea); 	EA+=X;				EAD=RM16(EAD);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0x9a: EA=0;																   break; /*   ILLEGAL*/
    	case 0x9b: ea=m6809.x+getDreg();								ea=RM16(ea);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0x9c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount[0]-=4;   break;
    	case 0x9d: ea=IMMWORD(); 	ea = ea + m6809.pc & 0xFFFF; 			ea=RM16(ea);	m6809_ICount[0]-=8;   break;
    /*TODO*///	case 0x9e: EA=0;																   break; /*   ILLEGAL*/
    	case 0x9f: ea=IMMWORD(); 						ea=RM16(ea);	m6809_ICount[0]-=8;   break;
    
    	case 0xa0: ea=m6809.y&0xFFFF;	m6809.y=(char)(m6809.y+1);										m6809_ICount[0]-=2;   break;
    	case 0xa1: ea=m6809.y&0xFFFF;	m6809.y=(char)(m6809.y+2);										m6809_ICount[0]-=3;   break;
    	case 0xa2: m6809.y=(char)(m6809.y-1); 	ea=m6809.y&0xFFFF;										m6809_ICount[0]-=2;   break;
    	case 0xa3: m6809.y=(char)(m6809.y-2); 	ea=m6809.y&0xFFFF;										m6809_ICount[0]-=3;   break;
    	case 0xa4: ea=m6809.y&0xFFFF;																   break;
    	case 0xa5: ea=m6809.y + (byte)m6809.b & 0xFFFF;										m6809_ICount[0]-=1;   break;
    	case 0xa6: ea=m6809.y + (byte)m6809.a & 0xFFFF;/*EA=Y+SIGNED(A);*/									m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xa7: EA=0;																   break; /*   ILLEGAL*/
    	case 0xa8: ea=IMMBYTE(); 	ea=m6809.y + (byte)ea & 0xFFFF;					m6809_ICount[0]-=1;   break;
    	case 0xa9: ea=IMMWORD(); 	ea=ea+m6809.y & 0xFFFF;								m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xaa: EA=0;																   break; /*   ILLEGAL*/
    	case 0xab: ea=m6809.y+getDreg() & 0xFFFF;												m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xac: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xad: IMMWORD(ea); 	EA+=PC; 							m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xae: EA=0;																   break; /*   ILLEGAL*/
    	case 0xaf: ea=IMMWORD(); 										m6809_ICount[0]-=5;   break;
    /*TODO*///
    /*TODO*///	case 0xb0: EA=Y;	Y++;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
    	case 0xb1: ea=m6809.y&0xFFFF;	m6809.y=(char)(m6809.y+2);						ea=RM16(ea);	m6809_ICount[0]-=6;   break;
    /*TODO*///	case 0xb2: Y--; 	EA=Y;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xb3: Y-=2;	EA=Y;						EAD=RM16(EAD);	m6809_ICount[0]-=6;   break;
    	case 0xb4: ea=m6809.y;								ea=RM16(ea);	m6809_ICount[0]-=3;   break;
    	case 0xb5: ea=m6809.y + (byte)m6809.b & 0xFFFF;						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    	case 0xb6: ea=m6809.y + (byte)m6809.a & 0xFFFF; 						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xb7: EA=0;																   break; /*   ILLEGAL*/
    	case 0xb8: ea=IMMBYTE(); 	ea=m6809.y + (byte)ea & 0xFFFF;	ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xb9: IMMWORD(ea); 	EA+=Y;				EAD=RM16(EAD);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0xba: EA=0;																   break; /*   ILLEGAL*/
    	case 0xbb: ea=m6809.y+getDreg() & 0xFFFF;								ea=RM16(ea);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0xbc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xbd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount[0]-=8;   break;
    /*TODO*///	case 0xbe: EA=0;																   break; /*   ILLEGAL*/
    /*TODO*///	case 0xbf: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount[0]-=8;   break;
    /*TODO*///
    	case 0xc0: ea=m6809.u&0xFFFF;			m6809.u=(char)(m6809.u+1);								m6809_ICount[0]-=2;   break;
    	case 0xc1: ea=m6809.u&0xFFFF;			m6809.u=(char)(m6809.u+2);								m6809_ICount[0]-=3;   break;
    	case 0xc2: m6809.u=(char)(m6809.u-1); 			ea=m6809.u&0xFFFF;								m6809_ICount[0]-=2;   break;
    	case 0xc3: m6809.u=(char)(m6809.u-2); 			ea=m6809.u&0xFFFF;								m6809_ICount[0]-=3;   break;
    	case 0xc4: ea=m6809.u&0xFFFF;																   break;
    	case 0xc5: ea=m6809.u+(byte)m6809.b & 0xFFFF;										m6809_ICount[0]-=1;   break;
    	case 0xc6: ea=m6809.u+(byte)m6809.a & 0xFFFF;										m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xc7: EA=0;																   break; /*ILLEGAL*/
    	case 0xc8: ea=IMMBYTE(); 	ea=m6809.u+(byte)ea & 0xFFFF;					m6809_ICount[0]-=1;   break;
    	case 0xc9: ea=IMMWORD(); 	ea=ea+m6809.u &0xFFFF;					m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xca: EA=0;																   break; /*ILLEGAL*/
    	case 0xcb: ea=m6809.u+getDreg() &0xFFFF;												m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xcc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xcd: IMMWORD(ea); 	EA+=PC; 							m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xce: EA=0;																   break; /*ILLEGAL*/
    /*TODO*///	case 0xcf: IMMWORD(ea); 										m6809_ICount[0]-=5;   break;
    /*TODO*///
    /*TODO*///	case 0xd0: EA=U;	U++;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
        case 0xd1: ea=m6809.u;	m6809.u=(char)(m6809.u+2);						ea=RM16(ea);	m6809_ICount[0]-=6;   break;
    /*TODO*///	case 0xd2: U--; 	EA=U;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xd3: U-=2;	EA=U;						EAD=RM16(EAD);	m6809_ICount[0]-=6;   break;
    	case 0xd4: ea=m6809.u;								ea=RM16(ea);	m6809_ICount[0]-=3;   break;
    	case 0xd5: ea=m6809.u+(byte)m6809.b & 0xFFFF;						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    	case 0xd6: ea=m6809.u+(byte)m6809.a & 0xFFFF;						ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xd7: EA=0;																   break; /*ILLEGAL*/
    	case 0xd8: ea=IMMBYTE(); 	ea=m6809.u+(byte)ea&0xFFFF;	ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    	case 0xd9: ea=IMMWORD(); 	ea=(ea+m6809.u)&0xFFFF;				ea=RM16(ea);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0xda: EA=0;																   break; /*ILLEGAL*/
    	case 0xdb: ea=m6809.u+getDreg() & 0xFFFF;								ea=RM16(ea);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0xdc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xdd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount[0]-=8;   break;
    /*TODO*///	case 0xde: EA=0;																   break; /*ILLEGAL*/
    /*TODO*///	case 0xdf: IMMWORD(ea); 						EAD=RM16(EAD);	m6809_ICount[0]-=8;   break;
    /*TODO*///
    	case 0xe0: ea=m6809.s;	m6809.s=(char)(m6809.s+1);										m6809_ICount[0]-=2;   break;
    	case 0xe1: ea=m6809.s;	m6809.s=(char)(m6809.s+2);										m6809_ICount[0]-=3;   break;
    	case 0xe2: m6809.s=(char)(m6809.s-1); 	ea=m6809.s;										m6809_ICount[0]-=2;   break;
    	case 0xe3: m6809.s=(char)(m6809.s-2); 	ea=m6809.s;										m6809_ICount[0]-=3;   break;
    	case 0xe4: ea=m6809.s;																   break;
    	case 0xe5: ea=m6809.s+(byte)m6809.b&0xFFFF;										m6809_ICount[0]-=1;   break;
    	case 0xe6: ea=m6809.s+(byte)m6809.a&0xFFFF;										m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xe7: EA=0;																   break; /*ILLEGAL*/
    	case 0xe8: ea=IMMBYTE(); 	ea=m6809.s+(byte)ea & 0xFFFF;					m6809_ICount[0]-=1;   break;
    	case 0xe9: ea=IMMWORD(); 	ea=(ea+m6809.s)&0xFFFF;								m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xea: EA=0;																   break; /*ILLEGAL*/
    /*TODO*///	case 0xeb: EA=S+D;												m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xec: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					m6809_ICount[0]-=1;   break;
    /*TODO*///	case 0xed: IMMWORD(ea); 	EA+=PC; 							m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xee: EA=0;																   break;  /*ILLEGAL*/
    /*TODO*///	case 0xef: IMMWORD(ea); 										m6809_ICount[0]-=5;   break;
    /*TODO*///
    /*TODO*///	case 0xf0: EA=S;	S++;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
    	case 0xf1: ea=m6809.s&0xFFFF;	m6809.s = (char)(m6809.s+2);						ea=RM16(ea);	m6809_ICount[0]-=6;   break;
    /*TODO*///	case 0xf2: S--; 	EA=S;						EAD=RM16(EAD);	m6809_ICount[0]-=5;   break;
    /*TODO*///	case 0xf3: S-=2;	EA=S;						EAD=RM16(EAD);	m6809_ICount[0]-=6;   break;
    	case 0xf4: ea=m6809.s&0xFFFF;								ea=RM16(ea);	m6809_ICount[0]-=3;   break;
    /*TODO*///	case 0xf5: EA=S+SIGNED(B);						EAD=RM16(EAD);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xf6: EA=S+SIGNED(A);						EAD=RM16(EAD);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xf7: EA=0;																   break; /*ILLEGAL*/
    	case 0xf8: ea=IMMBYTE(); 	ea=m6809.s+(byte)ea & 0xFFFF;	ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xf9: IMMWORD(ea); 	EA+=S;				EAD=RM16(EAD);	m6809_ICount[0]-=7;   break;
    /*TODO*///	case 0xfa: EA=0;																   break; /*ILLEGAL*/
    /*TODO*///	case 0xfb: EA=S+D;								EAD=RM16(EAD);	m6809_ICount[0]-=7;   break;
    	case 0xfc: ea=IMMBYTE(); 	ea=m6809.pc+(byte)ea & 0xFFFF;	ea=RM16(ea);	m6809_ICount[0]-=4;   break;
    /*TODO*///	case 0xfd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	m6809_ICount[0]-=8;   break;
    /*TODO*///	case 0xfe: EA=0;																   break; /*ILLEGAL*/
    	case 0xff: ea=IMMWORD(); 						ea=RM16(ea);	m6809_ICount[0]-=8;   break;
                default:
                System.out.println("6809 effective address : 0x"+Integer.toHexString(postbyte));
        }
    }
    /*TODO*////****************************************************************************
    /*TODO*/// * M6309 section
    /*TODO*/// ****************************************************************************/
    /*TODO*///#if HAS_HD6309
    /*TODO*///static UINT8 hd6309_reg_layout[] = {
    /*TODO*///	M6309_PC, M6309_S, M6309_CC, M6309_A, M6309_B, M6309_X, -1,
    /*TODO*///	M6309_Y, M6309_U, M6309_DP, M6309_NMI_STATE, M6309_IRQ_STATE, M6309_FIRQ_STATE, 0
    /*TODO*///};
    /*TODO*///
    /*TODO*////* Layout of the debugger windows x,y,w,h */
    /*TODO*///static UINT8 hd6309_win_layout[] = {
    /*TODO*///	27, 0,53, 4,	/* register window (top, right rows) */
    /*TODO*///	 0, 0,26,22,	/* disassembler window (left colums) */
    /*TODO*///	27, 5,53, 8,	/* memory #1 window (right, upper middle) */
    /*TODO*///	27,14,53, 8,	/* memory #2 window (right, lower middle) */
    /*TODO*///     0,23,80, 1,    /* command line window (bottom rows) */
    /*TODO*///};
    /*TODO*///
    /*TODO*///void hd6309_reset(void *param) { m6809_reset(param); }
    /*TODO*///void hd6309_exit(void) { m6809_exit(); }
    /*TODO*///int hd6309_execute(int cycles) { return m6809_execute(cycles); }
    /*TODO*///unsigned hd6309_get_context(void *dst) { return m6809_get_context(dst); }
    /*TODO*///void hd6309_set_context(void *src) { m6809_set_context(src); }
    /*TODO*///unsigned hd6309_get_pc(void) { return m6809_get_pc(); }
    /*TODO*///void hd6309_set_pc(unsigned val) { m6809_set_pc(val); }
    /*TODO*///unsigned hd6309_get_sp(void) { return m6809_get_sp(); }
    /*TODO*///void hd6309_set_sp(unsigned val) { m6809_set_sp(val); }
    /*TODO*///unsigned hd6309_get_reg(int regnum) { return m6809_get_reg(regnum); }
    /*TODO*///void hd6309_set_reg(int regnum, unsigned val) { m6809_set_reg(regnum,val); }
    /*TODO*///void hd6309_set_nmi_line(int state) { m6809_set_nmi_line(state); }
    /*TODO*///void hd6309_set_irq_line(int irqline, int state) { m6809_set_irq_line(irqline,state); }
    /*TODO*///void hd6309_set_irq_callback(int (*callback)(int irqline)) { m6809_set_irq_callback(callback); }
    /*TODO*///void hd6309_state_save(void *file) { state_save(file, "hd6309"); }
    /*TODO*///void hd6309_state_load(void *file) { state_load(file, "hd6309"); }

    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
     throw new UnsupportedOperationException("Not supported yet.");
    }};

    @Override
    public void exit() {
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
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }
    public static void illegal()
    {
        if( errorlog!=null )
    		fprintf(errorlog, "M6809: illegal opcode at %04x\n",m6809.pc);
    }
    /* $00 NEG direct ?**** */
    public void neg_di()
    {
    	int r,t;
    	t=DIRBYTE();
    	r = -t & 0xFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d neg_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $03 COM direct -**01 */
    public void com_di()
    {
        int t=	DIRBYTE();
    	t = ~t & 0xFF;
    	CLR_NZV();
    	SET_NZ8(t);
    	SEC();
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d com_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $04 LSR direct -0*-* */
    public void lsr_di()
    {
        int t=DIRBYTE();
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	t =t>>> 1 & 0xFF;
   	SET_Z8(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lsr_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $06 ROR direct -**-* */
    public void ror_di()//suspicious recheck
    {
        int t,r;
        t=DIRBYTE();
    	r= ((m6809.cc & CC_C) << 7) & 0xFF;
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	r = (r | t>>>1)&0xFF;
    	SET_NZ8(r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ror_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $07 ASR direct ?**-* */
    public void asr_di()
    {
        int t=DIRBYTE();
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	t = ((t & 0x80) | (t >>> 1))&0xFF;
    	SET_NZ8(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asr_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $08 ASL direct ?**** */
    public void asl_di()
    {
        int t,r;
    	t=DIRBYTE();
    	r = t << 1 & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asl_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $09 ROL direct -**** */
    public void rol_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = (m6809.cc & CC_C) | (t << 1);
    	CLR_NZVC();
   	SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rol_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $0A DEC direct -***- */
    public void dec_di()
    {
        int t=DIRBYTE();
	t= t-1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8D(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d dec_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $OC INC direct -***- */
    public void inc_di()
    {
        int t=DIRBYTE();
    	t=t+1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8I(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d inc_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $OD TST direct -**0- */
    public void tst_di()
    {
        int t=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d tst_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $0E JMP direct ----- */
    public void jmp_di()
    {
    	DIRECT();
        m6809.pc = (char)(ea&0xFFFF);
        CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d jmp_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $0F CLR direct -0100 */
    public void clr_di()
    {
    	DIRECT();
    	WM(ea,0);
    	CLR_NZVC();
    	SEZ();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d clr_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $12 NOP inherent ----- */
    public void nop()
    {

    }
    /* $13 SYNC inherent ----- */
    public void sync()
    {
    	/* SYNC stops processing instructions until an interrupt request happens. */
    	/* This doesn't require the corresponding interrupt to be enabled: if it */
    	/* is disabled, execution continues with the next instruction. */
    	m6809.int_state |= M6809_SYNC;	 /* HJB 990227 */
    	CHECK_IRQ_LINES();
    	/* if M6809_SYNC has not been cleared by CHECK_IRQ_LINES,
    	 * stop execution until the interrupt lines change. */
    	if(( m6809.int_state & M6809_SYNC )!=0)
    		if (m6809_ICount[0] > 0) m6809_ICount[0] = 0;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sync :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
   /* $16 LBRA relative ----- */
    public void lbra()
    {
       ea=IMMWORD();
       m6809.pc = (char)((m6809.pc + ea) & 0xFFFF);
       CHANGE_PC();
    
    	if ( ea == 0xfffd )  /* EHC 980508 speed up busy loop */
    		if ( m6809_ICount[0] > 0)
   			m6809_ICount[0] = 0;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $17 LBSR relative ----- */
    public void lbsr()
    {
    	ea=IMMWORD();
    	PUSHWORD(m6809.pc);
    	m6809.pc=(char)((m6809.pc + ea) &0xFFFF);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbsr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $19 DAA inherent (A) -**0* */
    public void daa()//suapicious recheck
    {
    	int/*UINT8*/ msn, lsn;
    	int/*UINT16*/ t, cf = 0;
    	msn = m6809.a & 0xf0; 
        lsn = m6809.a & 0x0f;
    	if( lsn>0x09 || (m6809.cc & CC_H)!=0) cf |= 0x06;
    	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
    	if( msn>0x90 || (m6809.cc & CC_C)!=0) cf |= 0x60;
    	t = cf + m6809.a & 0xFFFF;//should be unsigned???
    	CLR_NZV(); /* keep carry from previous operation */
    	SET_NZ8(/*(UINT8)*/t & 0xFF); 
        SET_C8(t);
    	m6809.a = (char)(t & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d daa :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $1A ORCC immediate ##### */
    public void orcc()
    {
    	int t=	IMMBYTE();
    	m6809.cc |= t;
    	CHECK_IRQ_LINES();	/* HJB 990116 */
        if(m6809log!=null) fprintf(m6809log,"M6809#%d orcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $1C ANDCC immediate ##### */
    public void andcc()
    {
        int t= IMMBYTE();
    	m6809.cc &= t;
    	CHECK_IRQ_LINES();	/* HJB 990116 */
        if(m6809log!=null) fprintf(m6809log,"M6809#%d andcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $1D SEX inherent -**0- */
    public void sex()
    {
        int t = (byte)m6809.b & 0xFFFF;
        setDreg(t);
    	CLR_NZV();
    	SET_NZ16(t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1E EXG inherent ----- */
    public void exg()
    {
    	/*UINT16*/int t1,t2;
    	/*UINT8*/int tb;
    
    	tb=IMMBYTE();
    	if(( (tb^(tb>>>4)) & 0x08 )!=0)	/* HJB 990225: mixed 8/16 bit case? */
    	{
    		/* transfer $ff to both registers */
    		t1 = t2 = 0xff;
    	}
    	else
    	{
    		switch(tb>>>4) {
    			case  0: t1 = getDreg();  break;
    			case  1: t1 = m6809.x;  break;
    			case  2: t1 = m6809.y;  break;
    			case  3: t1 = m6809.u;  break;
    			case  4: t1 = m6809.s;  break;
    			case  5: t1 = m6809.pc; break;
    			case  8: t1 = m6809.a;  break;
    			case  9: t1 = m6809.b;  break;
    			case 10: t1 = m6809.cc; break;
    			case 11: t1 = m6809.dp; break;
    			default: t1 = 0xff;
    		}
    		switch(tb&15) {
    			case  0: t2 = getDreg();  break;
    			case  1: t2 = m6809.x;  break;
    			case  2: t2 = m6809.y;  break;
    			case  3: t2 = m6809.u;  break;
    			case  4: t2 = m6809.s;  break;
    			case  5: t2 = m6809.pc; break;
    			case  8: t2 = m6809.a;  break;
    			case  9: t2 = m6809.b;  break;
    			case 10: t2 = m6809.cc; break;
    			case 11: t2 = m6809.dp; break;
    			default: t2 = 0xff;
            }
    	}
    	switch(tb>>>4) {
    		case  0: setDreg(t2);  break;
    		case  1: m6809.x = (char)(t2);  break;
    		case  2: m6809.y = (char)(t2);  break;
    		case  3: m6809.u = (char)(t2);  break;
    		case  4: m6809.s = (char)(t2);  break;
    		case  5: m6809.pc = (char)(t2); CHANGE_PC(); break;
    		case  8: m6809.a = (char)(t2);  break;
    		case  9: m6809.b = (char)(t2);  break;
    		case 10: m6809.cc= (char)(t2); break;
    		case 11: m6809.dp = (char)(t2); break;
    	}
    	switch(tb&15) {
    		case  0: setDreg(t1);  break;
    		case  1: m6809.x = (char)(t1);  break;
    		case  2: m6809.y = (char)(t1);  break;
    		case  3: m6809.u = (char)(t1);  break;
    		case  4: m6809.s = (char)(t1);  break;
    		case  5: m6809.pc = (char)(t1); CHANGE_PC(); break;
    		case  8: m6809.a = (char)(t1&0xFF);  break;
    		case  9: m6809.b = (char)(t1&0xFF);  break;
    		case 10: m6809.cc = (char)(t1&0xFF); break;
    		case 11: m6809.dp = (char)(t1&0xFF); break;
    	}
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d exg :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1F TFR inherent ----- */
    public void tfr()
    {
    	/*UINT8*/int tb;
    	/*UINT16*/ int t;
    
    	tb=IMMBYTE();
    	if(( (tb^(tb>>>4)) & 0x08 )!=0)	/* HJB 990225: mixed 8/16 bit case? */
    	{
    		/* transfer $ff to register */
    		t = 0xff;
        }
    	else
    	{
    		switch(tb>>>4) {
    			case  0: t = getDreg();  break;
    			case  1: t = m6809.x;  break;
    			case  2: t = m6809.y;  break;
    			case  3: t = m6809.u;  break;
    			case  4: t = m6809.s;  break;
    			case  5: t = m6809.pc; break;
    			case  8: t = m6809.a;  break;
    			case  9: t = m6809.b;  break;
    			case 10: t = m6809.cc; break;
    			case 11: t = m6809.dp; break;
    			default: t = 0xff;
            }
    	}
    	switch(tb&15) {
    		case  0: setDreg(t);   break;
    		case  1: m6809.x = (char)(t);  break;
    		case  2: m6809.y = (char)(t);  break;
    		case  3: m6809.u = (char)(t);  break;
    		case  4: m6809.s = (char)(t);  break;
    		case  5: m6809.pc = (char)(t); CHANGE_PC(); break;
    		case  8: m6809.a = (char)(t&0xFF);  break;
    		case  9: m6809.b = (char)(t&0xFF);  break;
    		case 10: m6809.cc = (char)(t&0xFF); break;
    		case 11: m6809.dp = (char)(t&0xFF); break;
        }
        if(m6809log!=null) fprintf(m6809log,"M6809#%d tfr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $20 BRA relative ----- */
    public void bra()
    {
        int t;
        t=IMMBYTE();
        m6809.pc=(char)(m6809.pc+(byte)t);//TODO check if it has to be better...
        CHANGE_PC();
    	/* JB 970823 - speed up busy loops */
    	if( t == 0xfe )
    		if( m6809_ICount[0] > 0 ) m6809_ICount[0] = 0;
         //if(m6809log!=null) fprintf(m6809log,"M6809#%d bra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $21 BRN relative ----- */
    public void brn()
    {
        int t=	IMMBYTE();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d brn :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }

    /* $1021 LBRN relative ----- */
    public void lbrn()
    {
       ea=IMMWORD();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbrn :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $22 BHI relative ----- */
    public void bhi()
    {
    	BRANCH( (m6809.cc & (CC_Z|CC_C))==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bhi :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $1022 LBHI relative ----- */
    public void lbhi()
    {
    	LBRANCH( (m6809.cc & (CC_Z|CC_C))==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbhi :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $23 BLS relative ----- */
    public void bls()
    {
    	BRANCH( (m6809.cc & (CC_Z|CC_C))!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bls :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $1023 LBLS relative ----- */
    public void lbls()
    {
    	LBRANCH( (m6809.cc & (CC_Z|CC_C))!=0 );
       if(m6809log!=null) fprintf(m6809log,"M6809#%d lbls :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $24 BCC relative ----- */
    public void bcc()
    {
    	BRANCH( (m6809.cc&CC_C)==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $1024 LBCC relative ----- */
    public void lbcc()
    {
    	LBRANCH( (m6809.cc&CC_C) ==0);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $25 BCS relative ----- */
    public void bcs()
    {
    	BRANCH( (m6809.cc&CC_C)!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bcs :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $1025 LBCS relative ----- */
    public void lbcs()
    {
    	LBRANCH( (m6809.cc&CC_C)!=0 );
       if(m6809log!=null) fprintf(m6809log,"M6809#%d lbcs :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $26 BNE relative ----- */
    public void bne()
    {
    	BRANCH( (m6809.cc&CC_Z)==0 );
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d bne :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1026 LBNE relative ----- */
    public void lbne()
    {
    	LBRANCH( (m6809.cc&CC_Z)==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbne :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $27 BEQ relative ----- */
    public void beq()
    {
    	BRANCH( (m6809.cc&CC_Z)!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d beq :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1027 LBEQ relative ----- */
    public void lbeq()
    {
    	LBRANCH( (m6809.cc&CC_Z)!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbeq :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $28 BVC relative ----- */
    public void bvc()
    {
    	BRANCH( (m6809.cc&CC_V)==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bvc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1028 LBVC relative ----- */
    public void lbvc()
    {
    	LBRANCH( (m6809.cc&CC_V)==0 );
    }
    /* $29 BVS relative ----- */
    public void bvs()
    {
    	BRANCH( (m6809.cc&CC_V)!=0 );
    }
    /* $1029 LBVS relative ----- */
    public void lbvs()
    {
    	LBRANCH( (m6809.cc&CC_V)!=0 );
    }
    /* $2A BPL relative ----- */
    public void bpl()
    {
    	BRANCH( (m6809.cc&CC_N)==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bpl :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $102A LBPL relative ----- */
    public void lbpl()
    {
    	LBRANCH( (m6809.cc&CC_N)==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbpl :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $2B BMI relative ----- */
    public void bmi()
    {
    	BRANCH( (m6809.cc&CC_N)!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bmi :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $102B LBMI relative ----- */
    public void lbmi()
    {
    	LBRANCH( (m6809.cc&CC_N)!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbmi :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $2C BGE relative ----- */
    public void bge()
    {
    	BRANCH( NXORV()==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bge :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    //* $102C LBGE relative ----- */
    public void lbge()
    {
    	LBRANCH( NXORV()==0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbge :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $2D BLT relative ----- */
    public void blt()
    {
    	BRANCH( NXORV()!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d blt :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $102D LBLT relative ----- */
    public void lblt()
    {
    	LBRANCH( NXORV()!=0 );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lblt :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $2E BGT relative ----- */
    public void bgt()
    {
    	BRANCH( !((NXORV()!=0) || ((m6809.cc&CC_Z)!=0)) );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bgt :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $102E LBGT relative ----- */
    public void lbgt()
    {
    	LBRANCH( !((NXORV()!=0) || ((m6809.cc&CC_Z)!=0)) );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lbgt :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $2F BLE relative ----- */
    public void ble()
    {
    	BRANCH( (NXORV()!=0 || (m6809.cc&CC_Z)!=0) );
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ble :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $102F LBLE relative ----- */
    public void lble()
    {
    	LBRANCH( (NXORV()!=0 || (m6809.cc&CC_Z)!=0) );
    }
    /* $30 LEAX indexed --*-- */
    public void leax()
    {
    	fetch_effective_address();
        m6809.x = (char)(ea);
    	CLR_Z();
    	SET_Z(m6809.x);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d leax :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $31 LEAY indexed --*-- */
    public void leay()
    {
    	fetch_effective_address();
        m6809.y = (char)(ea);
    	CLR_Z();
    	SET_Z(m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d leay :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $32 LEAS indexed ----- */
    public void leas()
    {
    	fetch_effective_address();
        m6809.s = (char)(ea);
    	m6809.int_state |= M6809_LDS;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d leas :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $33 LEAU indexed ----- */
    public void leau()
    {
    	fetch_effective_address();
        m6809.u = (char)(ea);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d leau :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $34 PSHS inherent ----- */
    public void pshs()
    {
    	int t=IMMBYTE();
    	if((  t&0x80 )!=0) { PUSHWORD(m6809.pc); m6809_ICount[0]-= 2; }
    	if((  t&0x40 )!=0) { PUSHWORD(m6809.u);  m6809_ICount[0]-= 2; }
    	if((  t&0x20 )!=0) { PUSHWORD(m6809.y);  m6809_ICount[0]-= 2; }
    	if((  t&0x10 )!=0) { PUSHWORD(m6809.x);  m6809_ICount[0]-= 2; }
    	if((  t&0x08 )!=0) { PUSHBYTE(m6809.dp);  m6809_ICount[0]-= 1; }
    	if((  t&0x04 )!=0) { PUSHBYTE(m6809.b);   m6809_ICount[0]-= 1; }
    	if((  t&0x02 )!=0) { PUSHBYTE(m6809.a);   m6809_ICount[0]-= 1; }
    	if((  t&0x01 )!=0) { PUSHBYTE(m6809.cc);  m6809_ICount[0]-= 1; }
        if(m6809log!=null) fprintf(m6809log,"M6809#%d pshs :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
        
    }
    
    /* 35 PULS inherent ----- */
    public void puls()
    {
        int t=IMMBYTE();
    	if(( t&0x01 )!=0) { m6809.cc=(char)(PULLBYTE()); m6809_ICount[0] -= 1; }
    	if(( t&0x02 )!=0) { m6809.a=(char)(PULLBYTE());  m6809_ICount[0] -= 1; }
    	if(( t&0x04 )!=0) { m6809.b=(char)(PULLBYTE());  m6809_ICount[0] -= 1; }
    	if(( t&0x08 )!=0) { m6809.dp=(char)(PULLBYTE()); m6809_ICount[0] -= 1; }
    	if(( t&0x10 )!=0) { m6809.x=(char)(PULLWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x20 )!=0) { m6809.y=(char)(PULLWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x40 )!=0) { m6809.u=(char)(PULLWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x80 )!=0) { m6809.pc=(char)(PULLWORD()); CHANGE_PC(); m6809_ICount[0] -= 2; }
    
    	/* HJB 990225: moved check after all PULLs */
    	if(( t&0x01 )!=0) { CHECK_IRQ_LINES(); }
        if(m6809log!=null) fprintf(m6809log,"M6809#%d puls :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $36 PSHU inherent ----- */
    public void pshu()
    {
    	int t=IMMBYTE();
    	if(( t&0x80 )!=0) { PSHUWORD(m6809.pc); m6809_ICount[0] -= 2; }
    	if(( t&0x40 )!=0) { PSHUWORD(m6809.s);  m6809_ICount[0] -= 2; }
    	if(( t&0x20 )!=0) { PSHUWORD(m6809.y);  m6809_ICount[0] -= 2; }
    	if(( t&0x10 )!=0) { PSHUWORD(m6809.x);  m6809_ICount[0] -= 2; }
    	if(( t&0x08 )!=0) { PSHUBYTE(m6809.dp);  m6809_ICount[0] -= 1; }
    	if(( t&0x04 )!=0) { PSHUBYTE(m6809.b);   m6809_ICount[0] -= 1; }
    	if(( t&0x02 )!=0) { PSHUBYTE(m6809.a);   m6809_ICount[0] -= 1; }
    	if(( t&0x01 )!=0) { PSHUBYTE(m6809.cc);  m6809_ICount[0] -= 1; }
        
        if(m6809log!=null) fprintf(m6809log,"M6809#%d pshu :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    
    /* 37 PULU inherent ----- */
    public void pulu()
    {
    	int t=IMMBYTE();
    	if(( t&0x01 )!=0) { m6809.cc=(char)(PULUBYTE()); m6809_ICount[0] -= 1; }
    	if(( t&0x02 )!=0) { m6809.a=(char)(PULUBYTE());  m6809_ICount[0] -= 1; }
    	if(( t&0x04 )!=0) { m6809.b=(char)(PULUBYTE());  m6809_ICount[0] -= 1; }
    	if(( t&0x08 )!=0) { m6809.dp=(char)(PULUBYTE()); m6809_ICount[0] -= 1; }
    	if(( t&0x10 )!=0) { m6809.x=(char)(PULUWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x20 )!=0) { m6809.y=(char)(PULUWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x40 )!=0) { m6809.s=(char)(PULUWORD()); m6809_ICount[0] -= 2; }
    	if(( t&0x80 )!=0) { m6809.pc=(char)(PULUWORD()); CHANGE_PC(); m6809_ICount[0] -= 2; }
    
    	/* HJB 990225: moved check after all PULLs */
    	if(( t&0x01 )!=0) { CHECK_IRQ_LINES(); }
        if(m6809log!=null) fprintf(m6809log,"M6809#%d pulu :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $39 RTS inherent ----- */
    public void rts()
    {
    	m6809.pc=(char)(PULLWORD());
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rts :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $3A ABX inherent ----- */
    public void abx()
    {
        m6809.x=(char)(m6809.x+m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d abx :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $3B RTI inherent ##### */
    public void rti()
    {
    	int t;
    	m6809.cc=(char)(PULLBYTE());
    	t = m6809.cc & CC_E;		/* HJB 990225: entire state saved? */
    	if(t!=0)
    	{
            m6809_ICount[0] -= 9;
    		m6809.a=(char)(PULLBYTE());
    		m6809.b=(char)(PULLBYTE());
    		m6809.dp=(char)(PULLBYTE());
    		m6809.x=(char)(PULLWORD());
    		m6809.y=(char)(PULLWORD());
    		m6809.u=(char)(PULLWORD());
    	}
    	m6809.pc=(char)(PULLWORD());
    	CHANGE_PC();
    	CHECK_IRQ_LINES();	/* HJB 990116 */
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rti :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    
    /* $3C CWAI inherent ----1 */
    public void cwai()
    {
    	int t =IMMBYTE();
    	m6809.cc &= t;
    	/*
         * CWAI stacks the entire machine_old state on the hardware stack,
         * then waits for an interrupt; when the interrupt is taken
         * later, the state is *not* saved again after CWAI.
         */
    	m6809.cc |= CC_E; 		/* HJB 990225: save entire state */
    	PUSHWORD(m6809.pc);
    	PUSHWORD(m6809.u);
    	PUSHWORD(m6809.y);
    	PUSHWORD(m6809.x);
    	PUSHBYTE(m6809.dp);
    	PUSHBYTE(m6809.b);
    	PUSHBYTE(m6809.a);
    	PUSHBYTE(m6809.cc);
    	m6809.int_state |= M6809_CWAI;	 /* HJB 990228 */
        CHECK_IRQ_LINES();    /* HJB 990116 */
    	if(( m6809.int_state & M6809_CWAI )!=0)
    		if( m6809_ICount[0] > 0 )
    			m6809_ICount[0] = 0;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cwai :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $3D MUL inherent --*-@ */
    public void mul()
    {
        int t;
    	t = ((m6809.a&0xff) * (m6809.b&0xff)) & 0xFFFF;
    	CLR_ZC(); 
        SET_Z16(t); 
        if((t&0x80)!=0) SEC();
    	setDreg(t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d mul :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
       
    }

    /* $3F SWI (SWI2 SWI3) absolute indirect ----- */
    public void swi()
    {
        m6809.cc |= CC_E; 			/* HJB 980225: save entire state */
    	PUSHWORD(m6809.ppc);
    	PUSHWORD(m6809.u);
    	PUSHWORD(m6809.y);
    	PUSHWORD(m6809.x);
    	PUSHBYTE(m6809.dp);
    	PUSHBYTE(m6809.b);
    	PUSHBYTE(m6809.a);
    	PUSHBYTE(m6809.cc);
        m6809.cc |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
        m6809.pc=RM16(0xfffa);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d swi :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }

    /* $103F SWI2 absolute indirect ----- */
    public void swi2()
    {
        m6809.cc |= CC_E; 			/* HJB 980225: save entire state */
    	PUSHWORD(m6809.ppc);
    	PUSHWORD(m6809.u);
    	PUSHWORD(m6809.y);
    	PUSHWORD(m6809.x);
    	PUSHBYTE(m6809.dp);
    	PUSHBYTE(m6809.b);
    	PUSHBYTE(m6809.a);
        PUSHBYTE(m6809.cc);
    	m6809.pc = RM16(0xfff4);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d swi2 :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /*TODO*///
    /*TODO*////* $113F SWI3 absolute indirect ----- */
    public void swi3()
    {
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///    PUSHBYTE(CC);
    /*TODO*///	PCD = RM16(0xfff2);
    /*TODO*///	CHANGE_PC;
    }
    /* $40 NEGA inherent ?**** */
    public void nega()
    {
        int r;
    	r = -m6809.a & 0xFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,m6809.a,r);
    	m6809.a = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d nega :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $43 COMA inherent -**01 */
    public void coma()
    {
    	m6809.a =(char)( ~m6809.a & 0xFF);
    	CLR_NZV();
    	SET_NZ8(m6809.a);
    	SEC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d coma :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $44 LSRA inherent -0*-* */
    public void lsra()//suspicious recheck
    {
        CLR_NZC();
    	m6809.cc |= (m6809.a & CC_C);
    	m6809.a = (char)(m6809.a >>> 1 & 0xFF);
    	SET_Z8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lsra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $46 RORA inherent -**-* */
    public void rora()
    {
        int r;
    	r = ((m6809.cc & CC_C) << 7)&0xFF;
    	CLR_NZC();
    	m6809.cc |= (m6809.a & CC_C);
    	r = (r | m6809.a >>> 1)&0xFF;
    	SET_NZ8(r);
    	m6809.a = (char)(r&0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rora :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $47 ASRA inherent ?**-* */
    public void asra()//suspicious recheck
    {
    	CLR_NZC();
    	m6809.cc |= (m6809.a & CC_C);
    	m6809.a = (char)(((m6809.a & 0x80) | (m6809.a >>> 1)) &0xFF);
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $48 ASLA inherent ?**** */
    public void asla()
    {
        int r = (m6809.a << 1) & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,m6809.a,r);
    	m6809.a = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asla :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $49 ROLA inherent -**** */
    public void rola()//very suspicious to recheck
    {
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rola(before):PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
 //BUGGY have to figure it out!
        int t,r;
    	t = m6809.a;
   	r = ((m6809.cc & CC_C) | ((t<<1))) &0xFFFF;//is that correct???
    	CLR_NZVC(); 
        SET_FLAGS8(t,t,r);
    	m6809.a = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rola:PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $4A DECA inherent -***- */
    public void deca()
    {
        m6809.a = (char)(m6809.a -1 & 0xFF);
    	CLR_NZV();
    	SET_FLAGS8D(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d deca :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);    
    }
    /* $4C INCA inherent -***- */
    public void inca()
    {
        m6809.a = (char)(m6809.a +1 & 0xFF);
    	CLR_NZV();
    	SET_FLAGS8I(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d inca :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $4D TSTA inherent -**0- */
    public void tsta()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.a);
    }
    /* $4F CLRA inherent -0100 */
    public void clra()
    {
        m6809.a = 0;
    	CLR_NZVC(); 
        SEZ();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d clra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $50 NEGB inherent ?**** */
    public void negb()
    {
        int r;
    	r = -m6809.b & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,m6809.b,r);
    	m6809.b = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d negb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $53 COMB inherent -**01 */
    public void comb()
    {
        m6809.b = (char)((~m6809.b) & 0xFF);
    	CLR_NZV();
    	SET_NZ8(m6809.b);
    	SEC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d comb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }   
    /* $54 LSRB inherent -0*-* */
    public void lsrb()
    {
        CLR_NZC();
    	m6809.cc |= (m6809.b & CC_C);
    	m6809.b = (char)(m6809.b >>> 1 &0xFF);
    	SET_Z8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lsrb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);    
    }
    /* $56 RORB inherent -**-* */
    public void rorb()
    {
        int r;
    	r = ((m6809.cc & CC_C) << 7)&0xFF;
    	CLR_NZC();
    	m6809.cc |= (m6809.b & CC_C);
    	r = (r | m6809.b >>> 1)&0xFF;
    	SET_NZ8(r);
    	m6809.b = (char)(r&0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rorb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);    
    }
    /* $57 ASRB inherent ?**-* */
    public void asrb()//suspicious recheck
    {
        CLR_NZC();
    	m6809.cc |= (m6809.b & CC_C);
    	m6809.b = (char)(((m6809.b & 0x80) | (m6809.b >>> 1)) &0xFF);
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asrb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $58 ASLB inherent ?**** */
    public void aslb()
    {
        int r = (m6809.b << 1) & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,m6809.b,r);
    	m6809.b = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d aslb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $59 ROLB inherent -**** */
    public void rolb()
    {
        int t,r;
    	t = m6809.b;
    	r = m6809.cc & CC_C;
    	r = (r | t << 1) &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(t,t,r);
    	m6809.b = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rolb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $5A DECB inherent -***- */
    public void decb()
    {
       m6809.b = (char)(m6809.b-1&0xFF);
       CLR_NZV();
       SET_FLAGS8D(m6809.b);
       if(m6809log!=null) fprintf(m6809log,"M6809#%d decb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $5C INCB inherent -***- */
    public void incb()
    {
        m6809.b = (char)(m6809.b +1 & 0xFF);
    	CLR_NZV();
    	SET_FLAGS8I(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d incb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $5D TSTB inherent -**0- */
    public void tstb()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.b);
    }
    /* $5F CLRB inherent -0100 */
    public void clrb()
    {
    	m6809.b = 0;
    	CLR_NZVC(); SEZ();
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d clrb :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $60 NEG indexed ?**** */
    public void neg_ix()
    {
        int r,t;
    	fetch_effective_address();
    	t = RM(ea);
    	r=-t & 0xFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d neg_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $63 COM indexed -**01 */
    public void com_ix()
    {
        int t;
    	fetch_effective_address();
    	t = ~RM(ea) & 0xFF;
    	CLR_NZV();
    	SET_NZ8(t);
    	SEC();
    	WM(ea,t);
       if(m6809log!=null) fprintf(m6809log,"M6809#%d com_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $64 LSR indexed -0*-* */
    public void lsr_ix()
    {
        int t;
    	fetch_effective_address();
    	t=RM(ea);
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	t= t >>>1 & 0xFF;
        SET_Z8(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lsr_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $66 ROR indexed -**-* */
    public void ror_ix()//suspicious recheck
    {
        int t,r;
    	fetch_effective_address();
    	t=RM(ea);
    	r = (m6809.cc & CC_C) << 7 &0xFF;
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	r = r | t>>>1 &0xFF;//correct???//r |= t>>1;
        SET_NZ8(r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ror_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $67 ASR indexed ?**-* */
    public void asr_ix()
    {
        int t;
    	fetch_effective_address();
    	t=RM(ea);
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	t=((t&0x80)|(t>>>1))&0xFF;
    	SET_NZ8(t);
   	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asr_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $68 ASL indexed ?**** */
    public void asl_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t=RM(ea);
   	r = (t << 1) &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asl_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $69 ROL indexed -**** */
    public void rol_ix()//suspicious recheck
    {
        int t,r;
    	fetch_effective_address();
    	t=RM(ea);
    	r = m6809.cc & CC_C;
    	r = (r | t << 1) & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rol_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $6A DEC indexed -***- */
    public void dec_ix()
    {
        int t;
    	fetch_effective_address();
    	t = RM(ea) - 1 & 0xFF;
    	CLR_NZV();
        SET_FLAGS8D(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d dec_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    
    /*TODO*////* $6C INC indexed -***- */
    public void inc_ix()
    {
        int t;
    	fetch_effective_address();
    	t = RM(ea) + 1 &0xFF;
    	CLR_NZV(); 
        SET_FLAGS8I(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d inc_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $6D TST indexed -**0- */
    public void tst_ix()
    {
        int t;
   	fetch_effective_address();
    	t = RM(ea);
    	CLR_NZV();
    	SET_NZ8(t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d tst_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $6E JMP indexed ----- */
    public void jmp_ix()
    {
    	fetch_effective_address();
    	m6809.pc = (char)(ea);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d jmp_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $6F CLR indexed -0100 */
    public void clr_ix()
    {
    	fetch_effective_address();
        WM(ea,0);
    	CLR_NZVC(); 
        SEZ();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d clr_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $70 NEG extended ?**** */
    public void neg_ex()
    {
        int r,t;
    	t=EXTBYTE();
    	r=-t & 0xFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,t,r);
    	WM(ea,r);
    }
    /* $73 COM extended -**01 */
    public void com_ex()
    {
        int t= EXTBYTE(); 
        t = ~t & 0xFF;
    	CLR_NZV(); 
        SET_NZ8(t); 
        SEC();
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d com_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $74 LSR extended -0*-* */
    public void lsr_ex()
    {
        int t=EXTBYTE(); 
        CLR_NZC(); 
        m6809.cc |= (t & CC_C);
    	t=t>>>1 &0XFF;
        SET_Z8(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lsr_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $76 ROR extended -**-* */
    public void ror_ex()
    {
        int t,r;
    	t=EXTBYTE();
        r=((m6809.cc & CC_C) << 7)&0xFF;
    	CLR_NZC();
        m6809.cc |= (t & CC_C);
    	r = (r| t>>>1)&0xFF;
        SET_NZ8(r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ror_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $77 ASR extended ?**-* */
    public void asr_ex()
    {
        int t=EXTBYTE();
    	CLR_NZC();
    	m6809.cc |= (t & CC_C);
    	t = ((t & 0x80) | (t >>> 1))&0xFF;
    	SET_NZ8(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asr_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $78 ASL extended ?**** */
    public void asl_ex()
    {
        int t,r;
        t= EXTBYTE();
        r=t<<1 & 0xFFFF;
        CLR_NZVC(); 
        SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d asl_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $79 ROL extended -**** */
    public void rol_ex()
    {
        int t,r;
        t= EXTBYTE();
        r = ((m6809.cc & CC_C) | (t << 1))&0xFFFF;
    	CLR_NZVC(); 
        SET_FLAGS8(t,t,r);
    	WM(ea,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d rol_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $7A DEC extended -***- */
    public void dec_ex()
    {
       int t=EXTBYTE(); 
       t=t-1&0xFF;
       CLR_NZV(); 
       SET_FLAGS8D(t);
       WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d dec_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $7C INC extended -***- */
    public void inc_ex()
    {
        int t=EXTBYTE(); 
        t=t+1&0xFF;
    	CLR_NZV(); 
        SET_FLAGS8I(t);
    	WM(ea,t);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d inc_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $7D TST extended -**0- */
    public void tst_ex()
    {
    	int t=EXTBYTE(); 
        CLR_NZV(); 
        SET_NZ8(t);
    }
    /* $7E JMP extended ----- */
    public void jmp_ex()
    {
    	EXTENDED();
    	m6809.pc = (char)(ea);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d jmp_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $7F CLR extended -0100 */
    public void clr_ex()
    {
    	EXTENDED();
    	WM(ea,0);
    	CLR_NZVC(); 
        SEZ();
    }
    /* $80 SUBA immediate ?**** */
    public void suba_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = m6809.a - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d suba_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);  
    }
    /* $81 CMPA immediate ?**** */
    public void cmpa_im()
    {
       int t,r;
       t=IMMBYTE();
       r = (m6809.a - t) & 0xFFFF;
       CLR_NZVC();
       SET_FLAGS8(m6809.a,t,r);
       if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpa_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $82 SBCA immediate ?**** */
    public void sbca_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = (m6809.a - t - (m6809.cc & CC_C))& 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbca_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);  
    
    }
    /* $83 SUBD (CMPD CMPU) immediate -**** */
    public void subd_im()
    {
       int r,d;
       int b=IMMWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);  
    }
    /* $1083 CMPD immediate -**** */
    public void cmpd_im()
    {
        int r,d;
        int b=IMMWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $1183 CMPU immediate -**** */
    public void cmpu_im()
    {
        int r, d;
        int b=IMMWORD();
    	d = m6809.u;
    	r = (d - b); //& 0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpu_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $84 ANDA immediate -**0- */
    public void anda_im()//suspicious recheck
    {
        int t=IMMBYTE();
    	m6809.a &= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d anda_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $85 BITA immediate -**0- */
    public void bita_im()//suspicious recheck
    {
   	int t,r;
        t=IMMBYTE();
    	r = m6809.a & t;
       	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bita_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $86 LDA immediate -**0- */
    public void lda_im()
    {
    	m6809.a=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lda_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $87 STA immediate -**0- */
    public void sta_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,A);
    }
    //* $88 EORA immediate -**0- */
    public void eora_im()
    {
        int t=IMMBYTE();
    	m6809.a ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eora_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $89 ADCA immediate ***** */
    public void adca_im()
    {
        int t,r;
    	t=IMMBYTE();
    	r = m6809.a + t + (m6809.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r & 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adca_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $8A ORA immediate -**0- */
    public void ora_im()
    {
        int t=IMMBYTE();
    	m6809.a |= t; //TODO should unsigned it??
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ora_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
       
    }
    /* $8B ADDA immediate ***** */
    public void adda_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = m6809.a + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adda_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $8C CMPX (CMPY CMPS) immediate -**** */
    public void cmpx_im()//suspicious recheck it
    {
        int r,d;
        int b=IMMWORD();
	d = m6809.x;
    	r = (d - b); //&0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpx_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
       
    }
    /* $108C CMPY immediate -**** */
    public void cmpy_im()
    {
        int r,d;
        int b=IMMWORD();
	d = m6809.y;
    	r = (d - b); //&0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpy_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $118C CMPS immediate -**** */
    public void cmps_im()
    {
        int r,d;
        int b=IMMWORD();
	d = m6809.s;
    	r = (d - b); //&0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmps_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $8D BSR ----- */
    public void bsr()
    {
    	int t=IMMBYTE();
    	PUSHWORD(m6809.pc);
    	m6809.pc = (char)(m6809.pc + (byte)t);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bsr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $8E LDX (LDY) immediate -**0- */
    public void ldx_im()
    {
    	m6809.x=(char)(IMMWORD());
    	CLR_NZV();
    	SET_NZ16(m6809.x);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldx_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $108E LDY immediate -**0- */
    public void ldy_im()
    {
        m6809.y=(char)(IMMWORD());
    	CLR_NZV();
    	SET_NZ16(m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldy_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea); 
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $8F STX (STY) immediate -**0- */
    public void stx_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pX);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $108F STY immediate -**0- */
    public void sty_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pY);
    }
    /* $90 SUBA direct ?**** */
    public void suba_di()
    {
        int t,r;
        t= DIRBYTE();
    	r = m6809.a - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d suba_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $91 CMPA direct ?**** */
    public void cmpa_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.a - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpa_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $92 SBCA direct ?**** */
    public void sbca_di()//suspicious recheck
    {
        int t,r;
        t=DIRBYTE();
    	r = (m6809.a - t - (m6809.cc & CC_C)) &0xFFFF;//should be unsigned??
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbca_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $93 SUBD (CMPD CMPU) direct -**** */
    public void subd_di()
    {
        int r,d;
        int b=DIRWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
   	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    //* $1093 CMPD direct -**** */
    public void cmpd_di()
    {
        int r,d;
        int b;
    	b=DIRWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $1193 CMPU direct -**** */
    public void cmpu_di()
    {
        int r,d;
    	int b=DIRWORD();
    	d = m6809.u;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpu_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $94 ANDA direct -**0- */
    public void anda_di()
    {
    	int t=DIRBYTE();
    	m6809.a &= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d anda_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $95 BITA direct -**0- */
    public void bita_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.a & t;
    	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bita_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $96 LDA direct -**0- */
    public void lda_di()
    {
    	m6809.a=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lda_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $97 STA direct -**0- */
    public void sta_di()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.a);
    	DIRECT();
    	WM(ea,m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sta_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $98 EORA direct -**0- */
    public void eora_di()
    {
        int t=DIRBYTE();
    	m6809.a ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eora_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $99 ADCA direct ***** */
    public void adca_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a= (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adca_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /*TODO*///
    /*TODO*////* $9A ORA direct -**0- */
    public void ora_di()
    {
        int t= DIRBYTE();
    	m6809.a |= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ora_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $9B ADDA direct ***** */
    public void adda_di()
    {
   	int t,r;
        t=DIRBYTE();
    	r = m6809.a + t;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adda_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $9C CMPX (CMPY CMPS) direct -**** */
    public void cmpx_di()
    {
        int r,d;
    	int b=DIRWORD();
    	d = m6809.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpx_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $109C CMPY direct -**** */
    public void cmpy_di()
    {
        int r,d;
    	int b=DIRWORD();
    	d = m6809.y;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpy_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $119C CMPS direct -**** */
    public void cmps_di()
    {
        int r,d;
        int b=DIRWORD();
        d = m6809.s;
        r = d - b;
        CLR_NZVC();
        SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmps_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $9D JSR direct ----- */
    public void jsr_di()
    {
        DIRECT();
        PUSHWORD(m6809.pc);
        m6809.pc = (char)(ea);
        CHANGE_PC();
    }
    /* $9E LDX (LDY) direct -**0- */
    public void ldx_di()
    {
    	m6809.x=(char)(DIRWORD());
    	CLR_NZV();
    	SET_NZ16(m6809.x);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldx_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $109E LDY direct -**0- */
    public void ldy_di()
    {
        m6809.y=(char)(DIRWORD());
    	CLR_NZV();
    	SET_NZ16(m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldy_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $9F STX (STY) direct -**0- */
    public void stx_di()
    {
    	CLR_NZV();
    	SET_NZ16(m6809.x);
    	DIRECT();
    	WM16(ea,m6809.x);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d stx_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $109F STY direct -**0- */
    public void sty_di()
    {
        CLR_NZV();
    	SET_NZ16(m6809.y);
    	DIRECT();
    	WM16(ea,m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sty_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $a0 SUBA indexed ?**** */
    public void suba_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.a - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d suba_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $a1 CMPA indexed ?**** */
    public void cmpa_ix()
    {
    	/*UINT16*/int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = (m6809.a - t) &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpa_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $a2 SBCA indexed ?**** */
    public void sbca_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = (m6809.a - t - (m6809.cc & CC_C))&0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbca_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $a3 SUBD (CMPD CMPU) indexed -**** */
    public void subd_ix()
    {
        int r,d;
        int b;
    	fetch_effective_address();
        b=RM16(ea);
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $10a3 CMPD indexed -**** */
    public void cmpd_ix()
    {
        int r,d;
        int b;
    	fetch_effective_address();
        b=RM16(ea);
    	d = getDreg();
    	r = (d -  b); //& 0xFFFF; //should be unsinged?????
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);

        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $11a3 CMPU indexed -**** */
    public void cmpu_ix()
    {    
        int r;
        int b;
    	fetch_effective_address();
        b=RM16(ea);
    	r = m6809.u - b;
    	CLR_NZVC();
    	SET_FLAGS16(m6809.u,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpu_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $a4 ANDA indexed -**0- */
    public void anda_ix()
    {
        fetch_effective_address();
    	m6809.a &= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d anda_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $a5 BITA indexed -**0- */
    public void bita_ix()
    {
        int r;
    	fetch_effective_address();
    	r = m6809.a & RM(ea);
    	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bita_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $a6 LDA indexed -**0- */
    public void lda_ix()
    {
    	fetch_effective_address();
    	m6809.a = RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lda_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $a7 STA indexed -**0- */
    public void sta_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
    	SET_NZ8(m6809.a);
    	WM(ea,m6809.a);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d sta_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $a8 EORA indexed -**0- */
    public void eora_ix()
    {
    	fetch_effective_address();
    	m6809.a ^= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eora_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $a9 ADCA indexed ***** */
    public void adca_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.a + t + (m6809.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adca_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $aA ORA indexed -**0- */
    public void ora_ix()
    {
    	fetch_effective_address();
    	m6809.a |= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ora_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $aB ADDA indexed ***** */
    public void adda_ix()
    {
       
    	int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.a + t &0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adda_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $aC CMPX (CMPY CMPS) indexed -**** */
    public void cmpx_ix()
    {
    	int r,d;
    	int b;
    	fetch_effective_address();
        b=RM16(ea);
    	d = m6809.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpx_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $10aC CMPY indexed -**** */
    public void cmpy_ix()
    {
        int r,d;
    	int b;
    	fetch_effective_address();
        b=RM16(ea);
    	d = m6809.y;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpy_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);  
    }
    /* $11aC CMPS indexed -**** */
    public void cmps_ix()
    {
        int r,d;
        int b;
        fetch_effective_address();
        b=RM16(ea);
        d = m6809.s;
        r = d - b;
        CLR_NZVC();
        SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmps_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $aD JSR indexed ----- */
    public void jsr_ix()
    {
    	fetch_effective_address();
        PUSHWORD(m6809.pc);
    	m6809.pc = (char)(ea);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d jsr_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $aE LDX (LDY) indexed -**0- */
    public void ldx_ix()
    {
    	fetch_effective_address();
        m6809.x=(char)(RM16(ea));
    	CLR_NZV();
    	SET_NZ16(m6809.x);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldx_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $10aE LDY indexed -**0- */
    public void ldy_ix()
    {
    	fetch_effective_address();
        m6809.y=(char)(RM16(ea));
    	CLR_NZV();
    	SET_NZ16(m6809.y);
         //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldy_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $aF STX (STY) indexed -**0- */
    public void stx_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
    	SET_NZ16(m6809.x);
    	WM16(ea,m6809.x);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d stx_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $10aF STY indexed -**0- */
    public void sty_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
    	SET_NZ16(m6809.y);
    	WM16(ea,m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sty_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $b0 SUBA extended ?**** */
    public void suba_ex()
    {
        int t,r;
    	t=EXTBYTE();
    	r = m6809.a - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d suba_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $b1 CMPA extended ?**** */
    public void cmpa_ex()
    {
        int t,r;
    	t=EXTBYTE();
    	r = m6809.a - t;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.a,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpa_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $b2 SBCA extended ?**** */
    public void sbca_ex()
    {
        int  t,r;
        t=EXTBYTE();
        r = (m6809.a - t - (m6809.cc & CC_C))&0xFFFF;
        CLR_NZVC();
        SET_FLAGS8(m6809.a,t,r);
        m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbca_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $b3 SUBD (CMPD CMPU) extended -**** */
    public void subd_ex()
    {
        int r,d;
        int b=EXTWORD();
        d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $10b3 CMPD extended -**** */
    public void cmpd_ex()
    {
        int r,d;
        int b=EXTWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $11b3 CMPU extended -**** */
    public void cmpu_ex()
    {
        int r,d;
    	int b=EXTWORD();
    	d = m6809.u;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpu_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }  
    /* $b4 ANDA extended -**0- */
    public void anda_ex()
    {
        int t=EXTBYTE();
    	m6809.a &= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d anda_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $b5 BITA extended -**0- */
    public void bita_ex()
    {
        int t,r;
        t =EXTBYTE();
    	r = m6809.a & t;
    	CLR_NZV(); 
        SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bita_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $b6 LDA extended -**0- */
    public void lda_ex()
    {
    	m6809.a=EXTBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d lda_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }

    /* $b7 STA extended -**0- */
    public void sta_ex()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.a);
    	EXTENDED();
    	WM(ea,m6809.a);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d sta_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $b8 EORA extended -**0- */
    public void eora_ex()
    {
        int t=	EXTBYTE();
    	m6809.a ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eora_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $b9 ADCA extended ***** */
    public void adca_ex()
    {
        int  t,r;
    	t=EXTBYTE();
    	r = (m6809.a + t + (m6809.cc & CC_C)) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
    }
    /* $bA ORA extended -**0- */
    public void ora_ex()
    {
        int t=EXTBYTE();
    	m6809.a |= t;
    	CLR_NZV();
    	SET_NZ8(m6809.a);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ora_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $bB ADDA extended ***** */
    public void adda_ex()
    {
        int  t,r;
    	t=EXTBYTE();
    	r = m6809.a + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.a,t,r);
    	SET_H(m6809.a,t,r);
    	m6809.a = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adda_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $bC CMPX (CMPY CMPS) extended -**** */
    public void cmpx_ex()
    {
        int r,d;
    	int b=EXTWORD();
    	d = m6809.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpx_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $10bC CMPY extended -**** */
    public void cmpy_ex()
    {
    	int r,d;
    	int b=EXTWORD();
    	d = m6809.y;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpy_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $11bC CMPS extended -**** */
    public void cmps_ex()
    {
        int r,d;
        int b=EXTWORD();
        d = m6809.s;
        r = d - b;
        CLR_NZVC();
        SET_FLAGS16(d,b,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmps_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $bD JSR extended ----- */
    public void jsr_ex()
    {
    	EXTENDED();
    	PUSHWORD(m6809.pc);
    	m6809.pc = (char)(ea);
    	CHANGE_PC();
        if(m6809log!=null) fprintf(m6809log,"M6809#%d jsr_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $bE LDX (LDY) extended -**0- */
    public void ldx_ex()
    {
        m6809.x=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.x);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldx_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
     
    }
    /* $10bE LDY extended -**0- */
    public void ldy_ex()
    {
    	m6809.y=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldy_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
     
    }
    /* $bF STX (STY) extended -**0- */
    public void stx_ex()
    {
    	CLR_NZV();
    	SET_NZ16(m6809.x);
    	EXTENDED();
    	WM16(ea,m6809.x);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d stx_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
     
    }
    /* $10bF STY extended -**0- */
    public void sty_ex()
    {
    	CLR_NZV();
    	SET_NZ16(m6809.y);
    	EXTENDED();
    	WM16(ea,m6809.y);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sty_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $c0 SUBB immediate ?**** */
    public void subb_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = m6809.b - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
         if(m6809log!=null) fprintf(m6809log,"M6809#%d subb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $c1 CMPB immediate ?**** */
    public void cmpb_im()
    {
        int t,r;
       t=IMMBYTE();
       r = (m6809.b - t) & 0xFFFF;
       CLR_NZVC();
       SET_FLAGS8(m6809.b,t,r);
       if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $c2 SBCB immediate ?**** */
    public void sbcb_im()
    {
        int t,r;
        t=IMMBYTE();
        r = (m6809.b - t - (m6809.cc & CC_C))& 0xFFFF;
        CLR_NZVC();
        SET_FLAGS8(m6809.b,t,r);
        m6809.b = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbcb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $c3 ADDD immediate -**** */
    public void addd_im()
    {
        int r,d;
        int b=IMMWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $c4 ANDB immediate -**0- */
    public void andb_im()
    {
        int t=IMMBYTE();
    	m6809.b &= t;//should be unsigned?
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d andb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $c5 BITB immediate -**0- */
    public void bitb_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = m6809.b & t;
       	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bitb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $c6 LDB immediate -**0- */
    public void ldb_im()
    {
        m6809.b=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $c7 STB immediate -**0- */
    public void stb_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,B);
    }

    /* $c8 EORB immediate -**0- */
    public void eorb_im()
    {
        int t=IMMBYTE();
    	m6809.b ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eorb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $c9 ADCB immediate ***** */
    public void adcb_im()
    {
        int t,r;
    	t=IMMBYTE();
    	r = m6809.b + t + (m6809.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adcb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $cA ORB immediate -**0- */
    public void orb_im()
    {
        int t=IMMBYTE();
    	m6809.b |= t; //TODO should unsigned it??
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d orb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $cB ADDB immediate ***** */
    public void addb_im()
    {
        int t,r;
        t=IMMBYTE();
    	r = m6809.b + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
       if(m6809log!=null) fprintf(m6809log,"M6809#%d addb_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $cC LDD immediate -**0- */
    public void ldd_im()
    {
    	int temp=IMMWORD();
        setDreg(temp);
    	CLR_NZV();
    	SET_NZ16(temp);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cD STD immediate -**0- */
    public void std_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pD);
    }
    /* $cE LDU (LDS) immediate -**0- */
    public void ldu_im()
    {
    	m6809.u=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.u);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldu_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);  
    }
    /* $10cE LDS immediate -**0- */
    public void lds_im()
    {
    	m6809.s=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.s);
    	m6809.int_state |= M6809_LDS;
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d lds_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cF STU (STS) immediate -**0- */
    public void stu_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pU);
    }
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $10cF STS immediate -**0- */
    public void sts_im()
    {
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pS);
    }
    /* $d0 SUBB direct ?**** */
    public void subb_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.b - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $d1 CMPB direct ?**** */
    public void cmpb_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.b - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $d2 SBCB direct ?**** */
    public void sbcb_di()
    {
        int t,r;
        t=DIRBYTE();
        r = (m6809.b - t - (m6809.cc & CC_C)) &0xFFFF;//should be unsigned??
        CLR_NZVC();
        SET_FLAGS8(m6809.b,t,r);
        m6809.b = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbcb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $d3 ADDD direct -**** */
    public void addd_di()
    {
        int r,d;
        int b=DIRWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $d4 ANDB direct -**0- */
    public void andb_di()
    {
        int t=DIRBYTE();
    	m6809.b &= t; //TODO should be unsigned?
        CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d andb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $d5 BITB direct -**0- */
    public void bitb_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.b & t;
    	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bitb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $d6 LDB direct -**0- */
    public void ldb_di()
    {
    	m6809.b=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $d7 STB direct -**0- */
    public void stb_di()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.b);
    	DIRECT();
    	WM(ea,m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d stb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $d8 EORB direct -**0- */
    public void eorb_di()
    {
        int t=DIRBYTE();
    	m6809.b ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eorb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $d9 ADCB direct ***** */
    public void adcb_di()
    {
        int t,r;
        t=DIRBYTE();
        r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
        CLR_HNZVC();
        SET_FLAGS8(m6809.b,t,r);
        SET_H(m6809.b,t,r);
        m6809.b= (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adcb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $dA ORB direct -**0- */
    public void orb_di()
    {
        int t=	DIRBYTE();
    	m6809.b |= t;  //todo check if it should be unsigned
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d orb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $dB ADDB direct ***** */
    public void addb_di()
    {
        int t,r;
        t=DIRBYTE();
    	r = m6809.b + t &0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addb_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $dC LDD direct -**0- */
    public void ldd_di()
    {
      int temp=	DIRWORD();
      setDreg(temp);
      CLR_NZV();
      SET_NZ16(temp);
      if(m6809log!=null) fprintf(m6809log,"M6809#%d ldd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $dD STD direct -**0- */
    public void std_di()
    {
    	CLR_NZV();
        int temp = getDreg();
    	SET_NZ16(temp);
        DIRECT();
    	WM16(ea,temp);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d std_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $dE LDU (LDS) direct -**0- */
    public void ldu_di()
    {
        m6809.u=DIRWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.u);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldu_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
   
    }
    /* $10dE LDS direct -**0- */
    public void lds_di()
    {
    	m6809.s=DIRWORD();
        CLR_NZV();
    	SET_NZ16(m6809.s);
    	m6809.int_state |= M6809_LDS;
    }
    /* $dF STU (STS) direct -**0- */
    public void stu_di()
    {
    	CLR_NZV();
   	SET_NZ16(m6809.u);
    	DIRECT();
    	WM16(ea,m6809.u);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d stu_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $10dF STS direct -**0- */
    public void sts_di()
    {
    	CLR_NZV();
    	SET_NZ16(m6809.s);
    	DIRECT();
    	WM16(ea,m6809.s);
    }
    /* $e0 SUBB indexed ?**** */
    public void subb_ix()
    {
        int	  t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.b - t & 0xFFFF;
    	CLR_NZVC();
   	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $e1 CMPB indexed ?**** */
    public void cmpb_ix()
    {
        /*UINT16*/int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = (m6809.b - t) &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $e2 SBCB indexed ?**** */
    public void sbcb_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = (m6809.b - t - (m6809.cc & CC_C))&0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbcb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $e3 ADDD indexed -**** */
    public void addd_ix()
    {
    	int r,d;
        int b;
        fetch_effective_address();
    	b=RM16(ea);
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $e4 ANDB indexed -**0- */
    public void andb_ix()
    {
    	fetch_effective_address();
    	m6809.b &= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d andb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea); 
    }
    /* $e5 BITB indexed -**0- */
    public void bitb_ix()
    {
      	int r;
    	fetch_effective_address();
    	r = m6809.b & RM(ea);
    	CLR_NZV();
    	SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bitb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea); 
    }
    /* $e6 LDB indexed -**0- */
    public void ldb_ix()
    {
    	fetch_effective_address();
    	m6809.b = RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $e7 STB indexed -**0- */
    public void stb_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
    	SET_NZ8(m6809.b);
    	WM(ea,m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d stb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        

    }
    /* $e8 EORB indexed -**0- */
    public void eorb_ix()
    {
    	fetch_effective_address();
    	m6809.b ^= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d eorb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $e9 ADCB indexed ***** */
    public void adcb_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.b + t + (m6809.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d adcb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);      
    }
    /* $eA ORB indexed -**0- */
    public void orb_ix()
    {
    	fetch_effective_address();
    	m6809.b |= RM(ea);
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d orb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $eB ADDB indexed ***** */
    public void addb_ix()
    {
        int t,r;
    	fetch_effective_address();
    	t = RM(ea);
    	r = m6809.b + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addb_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $eC LDD indexed -**0- */
    public void ldd_ix()
    {
    	fetch_effective_address();
        int temp=RM16(ea);
        setDreg(temp);
    	CLR_NZV(); 
        SET_NZ16(temp);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d ldd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $eD STD indexed -**0- */
    public void std_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
        int temp=getDreg();
    	SET_NZ16(temp);
    	WM16(ea,temp);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d std_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $eE LDU (LDS) indexed -**0- */
    public void ldu_ix()
    {
    	fetch_effective_address();
        m6809.u=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(m6809.u);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldu_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $10eE LDS indexed -**0- */
    public void lds_ix()
    {
        fetch_effective_address();
        m6809.s=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(m6809.s);
        m6809.int_state |= M6809_LDS;
    }
    /* $eF STU (STS) indexed -**0- */
    public void stu_ix()
    {
    	fetch_effective_address();
        CLR_NZV();
    	SET_NZ16(m6809.u);
    	WM16(ea,m6809.u);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d stu_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    
    /* $10eF STS indexed -**0- */
    public void sts_ix()
    {
        fetch_effective_address();
        CLR_NZV();
    	SET_NZ16(m6809.s);
    	WM16(ea,m6809.s);
    }
    /* $f0 SUBB extended ?**** */
    public void subb_ex()
    {
        int  t,r;
        t=EXTBYTE();
    	r = m6809.b - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
        if(m6809log!=null) fprintf(m6809log,"M6809#%d subb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    
    }
    /* $f1 CMPB extended ?**** */
    public void cmpb_ex()
    {
        int t,r;
    	t=EXTBYTE();
    	r = m6809.b - t;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d cmpb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
  
    }
    /* $f2 SBCB extended ?**** */
    public void sbcb_ex()
    {
        int t,r;
    	t = EXTBYTE();
    	r = (m6809.b - t - (m6809.cc & CC_C))&0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d sbcb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $f3 ADDD extended -**** */
    public void addd_ex()
    {
        int r,d;
        int b=EXTWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $f4 ANDB extended -**0- */
    public void andb_ex()
    {
        int t = EXTBYTE();
        m6809.b &=t;
        CLR_NZV();
        SET_NZ8(m6809.b);
    }
    /* $f5 BITB extended -**0- */
    public void bitb_ex()
    {
        int t,r;
        t =EXTBYTE();
    	r = m6809.b & t;
    	CLR_NZV(); 
        SET_NZ8(r);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d bitb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
        
    }
    /* $f6 LDB extended -**0- */
    public void ldb_ex()
    {
        m6809.b=EXTBYTE();
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $f7 STB extended -**0- */
    public void stb_ex()
    {
    	CLR_NZV();
    	SET_NZ8(m6809.b);
    	EXTENDED();
    	WM(ea,m6809.b);
        //if(m6809log!=null) fprintf(m6809log,"M6809#%d stb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
    }
    /* $f8 EORB extended -**0- */
    public void eorb_ex()
    {
        int t=EXTBYTE();
    	m6809.b ^= t;
    	CLR_NZV();
    	SET_NZ8(m6809.b);
    }
    /* $f9 ADCB extended ***** */
    public void adcb_ex()
    {
        int  t,r;
    	t=EXTBYTE();
    	r = (m6809.b + t + (m6809.cc & CC_C)) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);;
    }
    /* $fA ORB extended -**0- */
    public void orb_ex()
    {
        int t=EXTBYTE();
    	m6809.b |= t;
    	CLR_NZV();
    	SET_NZ8(m6809.b);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d orb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);

    }
    /* $fB ADDB extended ***** */
    public void addb_ex()
    {
        int  t,r;
    	t=EXTBYTE();
    	r = m6809.b + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(m6809.b,t,r);
    	SET_H(m6809.b,t,r);
    	m6809.b = (char)(r& 0xFF);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d addb_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $fC LDD extended -**0- */
    public void ldd_ex()
    {
    	int temp=EXTWORD();
        setDreg(temp);
    	CLR_NZV();
    	SET_NZ16(temp);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d ldd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $fD STD extended -**0- */
    public void std_ex()
    {
    	CLR_NZV();
        int temp = getDreg();
    	SET_NZ16(temp);
        EXTENDED();
    	WM16(ea,temp);
        if(m6809log!=null) fprintf(m6809log,"M6809#%d std_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),(int)m6809.pc,(int)m6809.ppc,(int)m6809.a,(int)m6809.b,getDreg(),(int)m6809.dp,(int)m6809.u,(int)m6809.s,(int)m6809.x,(int)m6809.y,(int)m6809.cc,ea);
 
    }
    /* $fE LDU (LDS) extended -**0- */
    public void ldu_ex()
    {
    	m6809.u=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.u);
    }
    /* $10fE LDS extended -**0- */
    public void lds_ex()
    {
        m6809.s=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(m6809.s);
	m6809.int_state |= M6809_LDS;
    }
    /* $fF STU (STS) extended -**0- */
    public void stu_ex()
    {
    	CLR_NZV();
   	SET_NZ16(m6809.u);
    	EXTENDED();
    	WM16(ea,m6809.u);
    }
    /* $10fF STS extended -**0- */
    public void sts_ex()
    {
    	CLR_NZV();
    	SET_NZ16(m6809.s);
    	EXTENDED();
    	WM16(ea,m6809.s);
    }
    /* $10xx opcodes */
    public void pref10()
    {
    	int ireg2 = ROP(m6809.pc) &0xFF;
        m6809.pc = (char)(m6809.pc + 1);
    	switch( ireg2 )
    	{
    		case 0x21: lbrn();		m6809_ICount[0]-=5;	break;
    		case 0x22: lbhi();		m6809_ICount[0]-=5;	break;
    		case 0x23: lbls();		m6809_ICount[0]-=5;	break;
    		case 0x24: lbcc();		m6809_ICount[0]-=5;	break;
    		case 0x25: lbcs();		m6809_ICount[0]-=5;	break;
    		case 0x26: lbne();		m6809_ICount[0]-=5;	break;
    		case 0x27: lbeq();		m6809_ICount[0]-=5;	break;
    		case 0x28: lbvc();		m6809_ICount[0]-=5;	break;
    		case 0x29: lbvs();		m6809_ICount[0]-=5;	break;
    		case 0x2a: lbpl();		m6809_ICount[0]-=5;	break;
    		case 0x2b: lbmi();		m6809_ICount[0]-=5;	break;
    		case 0x2c: lbge();		m6809_ICount[0]-=5;	break;
    		case 0x2d: lblt();		m6809_ICount[0]-=5;	break;
    		case 0x2e: lbgt();		m6809_ICount[0]-=5;	break;
    		case 0x2f: lble();		m6809_ICount[0]-=5;	break;

    		case 0x3f: swi2();		m6809_ICount[0]-=20;	break;

    		case 0x83: cmpd_im();	m6809_ICount[0]-=5;	break;
    		case 0x8c: cmpy_im();	m6809_ICount[0]-=5;	break;
    		case 0x8e: ldy_im();	m6809_ICount[0]-=4;	break;
    /*TODO*///		case 0x8f: sty_im();	m6809_ICount[0]-=4;	break;
    /*TODO*///
    		case 0x93: cmpd_di();	m6809_ICount[0]-=7;	break;
    		case 0x9c: cmpy_di();	m6809_ICount[0]-=7;	break;
    		case 0x9e: ldy_di();	m6809_ICount[0]-=6;	break;
    		case 0x9f: sty_di();	m6809_ICount[0]-=6;	break;
    
    		case 0xa3: cmpd_ix();	m6809_ICount[0]-=7;	break;
    		case 0xac: cmpy_ix();	m6809_ICount[0]-=7;	break;
    		case 0xae: ldy_ix();	m6809_ICount[0]-=6;	break;
    		case 0xaf: sty_ix();	m6809_ICount[0]-=6;	break;
    
    		case 0xb3: cmpd_ex();	m6809_ICount[0]-=8;	break;
    		case 0xbc: cmpy_ex();	m6809_ICount[0]-=8;	break;
    		case 0xbe: ldy_ex();	m6809_ICount[0]-=7;	break;
    		case 0xbf: sty_ex();	m6809_ICount[0]-=7;	break;
    /*TODO*///
    		case 0xce: lds_im();	m6809_ICount[0]-=4;	break;
    /*TODO*///		case 0xcf: sts_im();	m6809_ICount[0]-=4;	break;
    /*TODO*///
    		case 0xde: lds_di();	m6809_ICount[0]-=4;	break;
    		case 0xdf: sts_di();	m6809_ICount[0]-=4;	break;
    /*TODO*///
    		case 0xee: lds_ix();	m6809_ICount[0]-=6;	break;
    
                case 0xef: sts_ix();	m6809_ICount[0]-=6;	break;
    /*TODO*///
    		case 0xfe: lds_ex();	m6809_ICount[0]-=7;	break;
    		case 0xff: sts_ex();	m6809_ICount[0]-=7;	break;
    /*TODO*///
    /*TODO*///		default:   illegal();						break;
            default:
                System.out.println("6809 prefix10 opcode 0x"+Integer.toHexString(ireg2));
        }
    }
    /* $11xx opcodes */
    public void pref11()
    {
        int ireg2 = ROP(m6809.pc) &0xFF;
        m6809.pc = (char)(m6809.pc + 1);
    	switch( ireg2 )
    	{
    /*TODO*///		case 0x3f: swi3();		m6809_ICount[0]-=20;	break;

    		case 0x83: cmpu_im();	m6809_ICount[0]-=5;	break;
    		case 0x8c: cmps_im();	m6809_ICount[0]-=5;	break;

    		case 0x93: cmpu_di();	m6809_ICount[0]-=7;	break;
    		case 0x9c: cmps_di();	m6809_ICount[0]-=7;	break;

    		case 0xa3: cmpu_ix();	m6809_ICount[0]-=7;	break;
    		case 0xac: cmps_ix();	m6809_ICount[0]-=7;	break;

    		case 0xb3: cmpu_ex();	m6809_ICount[0]-=8;	break;
    		case 0xbc: cmps_ex();	m6809_ICount[0]-=8;	break;

    /*TODO*///		default:   illegal();						break;
             default:
                System.out.println("6809 prefix11 opcode 0x"+Integer.toHexString(ireg2));
    	}
    }

}

