package cpu.tms32010;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.tms32010.tms32010H.*;
import static mame.memory.*;
import static platform.libc_old.*;
import static mame.mame.*;

/**
 *
 * @author shadow
 */
public class tms32010 extends cpu_interface{

    public static int[] tms320c10_ICount = new int[1];
    
    public tms32010()
    {
          cpu_num = CPU_TMS320C10;
          num_irqs = 2;
          default_vector = 0;
          overclock = 1.0;
          no_int = TMS320C10_INT_NONE;
          irq_int = -1;
          nmi_int = -1;
          address_bits = 16;
          address_shift = -1;
          endianess = CPU_IS_BE;
          align_unit = 2;
          max_inst_len = 4;
          abits1 = ABITS1_16;
          abits2 = ABITS2_16;
          abitsmin = ABITS_MIN_16; 
          icount =tms320c10_ICount;
    }

    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public static class tms320c10_Regs {
        char/*UINT16*/	PREPC;		/* previous program counter */
	char/*UINT16*/  PC;
	int  ACC, Preg;
	int   ALU;
	char/*UINT16*/  Treg;
	char[]/*UINT16*/  AR=new char[2];
        char[]/*UINT16*/STACK=new char[4];
        char/*UINT16*/STR;
	int     pending_irq, BIO_pending_irq;
	int     irq_state;
	public irqcallbacksPtr irq_callback;
    }
    static /*UINT16*/char   opcode=0;
    static /*UINT8*/int opcode_major=0, opcode_minor, opcode_minr;	/* opcode split into MSB and LSB */
    static tms320c10_Regs R=new tms320c10_Regs();
    static int tmpacc;
    static /*UINT16*/char memaccess;
    
    public static abstract interface opcode_fn {

        public abstract void handler();
    }
    public static int M_RDROM(int A){	return ((cpu_readmem16((A<<1))<<8) | cpu_readmem16(((A<<1)+1))); }
    public static void M_WRTROM(int A,int V) { cpu_writemem16(((A<<1)+1),(V&0xff)); cpu_writemem16((A<<1),((V>>8)&0xff)); }
    public static int M_RDRAM(int A){ return ((cpu_readmem16((A<<1)|0x8000)<<8) | cpu_readmem16(((A<<1)|0x8001))); }
    public static void M_WRTRAM(int A,int V)	{ cpu_writemem16(((A<<1)|0x8001),(V&0x0ff)); cpu_writemem16(((A<<1)|0x8000),((V>>8)&0x0ff)); }
    public static char M_RDOP(char A)		
    { 
        return ((char)((cpu_readop((A<<1))<<8) | cpu_readop(((A<<1)+1))));
    }
    public static char M_RDOP_ARG(char A)	{ return (char)((cpu_readop_arg((A<<1))<<8) | cpu_readop_arg(((A<<1)+1))); }
    public static int M_IN(int Port)	{ return (cpu_readport(Port))&0xFFFF; }
    public static void M_OUT(int Port,int Value){ cpu_writeport(Port,Value);}

    public static final int ADDR_MASK		=TMS320C10_ADDR_MASK;
    
    /********  The following is the Status (Flag) register definition.  *********/
    /* 15 | 14  |  13  | 12 | 11 | 10 | 9 |  8  | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0  */
    /* OV | OVM | INTM |  1 |  1 |  1 | 1 | ARP | 1 | 1 | 1 | 1 | 1 | 1 | 1 | DP */
    public static final int OV_FLAG		=0x8000;	/* OV	(Overflow flag) 1 indicates an overflow */
    public static final int OVM_FLAG            =0x4000;	/* OVM	(Overflow Mode bit) 1 forces ACC overflow to greatest positive or negative saturation value */
    public static final int INTM_FLAG           =0x2000;	/* INTM	(Interrupt Mask flag) 0 enables maskable interrupts */
    public static final int ARP_REG		=0x0100;	/* ARP	(Auxiliary Register Pointer) */
    public static final int DP_REG		=0x0001;	/* DP	(Data memory Pointer (bank) bit) */

    public static int OV		 =( R.STR & OV_FLAG);			/* OV	(Overflow flag) */
    public static int OVM		 =( R.STR & OVM_FLAG);		/* OVM	(Overflow Mode bit) 1 indicates an overflow */
    public static int INTM               =( R.STR & INTM_FLAG);		/* INTM	(Interrupt enable flag) 0 enables maskable interrupts */
    public static int ARP		 =((R.STR & ARP_REG) >> 8 );	/* ARP	(Auxiliary Register Pointer) */
    public static int DP		 =((R.STR & DP_REG) << 7);	/* DP	(Data memory Pointer bit) */
    
    public static int dma		 =(DP | (opcode_minor & 0x07f));	/* address used in direct memory access operations */
    public static int dmapage1           =(0x80 | opcode_minor);			/* address used in direct memory access operations for sst instruction */
    public static int ind		 =(R.AR[ARP] & 0x00ff);			/* address used in indirect memory access operations */
    public static void memacc()	 { memaccess = (opcode_minor & 0x80)!=0 ? (char)ind : (char)dma;}
    
    
    public static void CLR(/*UINT16*/int flag) { R.STR &= ~flag; R.STR |= 0x1efe; }
    public static void SET(/*UINT16*/int flag) { R.STR |=  flag; R.STR |= 0x1efe; }
    
    static void getdata(/*UINT8*/int shift,/*UINT8*/int signext)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = (char)ind;
            else memaccess = (char)dma;
            R.ALU = M_RDRAM(memaccess);
            if ((signext!=0) && (R.ALU & 0x8000)!=0) R.ALU |= 0xffff0000;
            else R.ALU &= 0x0000ffff;
            R.ALU <<= shift;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/char tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = tmpAR-- ;
                            R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff));//check if it has to unsigned???(shadow)
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
    }
    static void getdata_lar()
    {
            if ((opcode_minor & 0x80)!=0) memaccess = (char)ind;
            else memaccess = (char)dma;
            R.ALU = M_RDRAM(memaccess);
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            if ((opcode_major & 1) != ARP) {
                                /*UINT16*/char tmpAR = R.AR[ARP];
                                if ((opcode_minor & 0x20)!=0) tmpAR = tmpAR++ ;
                                if ((opcode_minor & 0x10)!=0) tmpAR = tmpAR-- ;
                                 R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff));//check if it has to unsigned???(shadow)
                            }
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
    }
    static void putdata(/*UINT16*/char data)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = (char)ind;
            else memaccess = (char)dma;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/char tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = tmpAR-- ;
                            R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff));
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
            if ((opcode_major == 0x30) || (opcode_major == 0x31)) {
                    M_WRTRAM(memaccess,(R.AR[data])); }
            else M_WRTRAM(memaccess,data);
    }
    static void putdata_sst(/*UINT16*/char data)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = (char)ind;
            else memaccess = (char)dmapage1;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/char tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = tmpAR-- ;
                            R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff));
                    }
            }
            M_WRTRAM(memaccess,data);
    }
    static void M_ILLEGAL()
    {
            if (errorlog!=null) fprintf(errorlog, "TMS320C10:  PC = %04x,  Illegal opcode = %04x\n", (R.PC-1), opcode);
    }
    /* This following function is here to fill in the void for */
    /* the opcode call function. This function is never called. */
    public static opcode_fn other_7F_opcodes= new opcode_fn() {  public void handler()  { }};

    public static opcode_fn illegal= new opcode_fn() {  public void handler()	{ M_ILLEGAL(); }};
    public static opcode_fn abst= new opcode_fn() {  public void handler()
		{
			if (R.ACC >= 0x80000000) {
				R.ACC = ~R.ACC;
				R.ACC++ ;
				if (OVM!=0 && (R.ACC == 0x80000000)) R.ACC-- ;
			}
		}
    };
    public static opcode_fn add_sh= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(opcode_major,1);
			R.ACC += R.ALU;
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x7fffffff;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn addh= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.ACC += (R.ALU << 16);
			R.ACC &= 0xffff0000;
			R.ACC += (tmpacc & 0x0000ffff);
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) {
					R.ACC &= 0x0000ffff; R.ACC |= 0x7fff0000;
				}
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn adds= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.ACC += R.ALU;
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x7fffffff;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn and= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			R.ACC &= R.ALU;
			R.ACC &= 0x0000ffff;
		}
    };
    public static opcode_fn apac= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			R.ACC += R.Preg;
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x7fffffff;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn br= new opcode_fn() {  public void handler()		
    { 
        R.PC = M_RDOP_ARG(R.PC);
    }};
    public static opcode_fn banz= new opcode_fn() {  public void handler()
		{
			if ((R.AR[ARP] & 0x01ff) == 0) R.PC++ ;
			else R.PC = M_RDOP_ARG(R.PC);
			R.ALU = R.AR[ARP]; R.ALU-- ;
			R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (R.ALU & 0x01ff));//unsigned? (shadow)
		}
    };
    public static opcode_fn bgez= new opcode_fn() {  public void handler()
		{
			if (R.ACC >= 0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn bgz= new opcode_fn() {  public void handler()
		{
			if (R.ACC >  0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn bioz= new opcode_fn() {  public void handler()
		{
			if (R.BIO_pending_irq!=0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn blez= new opcode_fn() {  public void handler()
		{
			if (R.ACC <= 0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn blz= new opcode_fn() {  public void handler()
		{
			if (R.ACC <  0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn bnz= new opcode_fn() {  public void handler()
		{
			if (R.ACC != 0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn bv= new opcode_fn() {  public void handler()
		{
			if (OV!=0) {
				R.PC = M_RDOP_ARG(R.PC);
				CLR(OV_FLAG);
			}
			else R.PC++ ;
		}
    };
    public static opcode_fn bz= new opcode_fn() {  public void handler()
		{
			if (R.ACC == 0) R.PC = M_RDOP_ARG(R.PC);
			else R.PC++ ;
		}
    };
    public static opcode_fn cala= new opcode_fn() {  public void handler()
		{
			R.STACK[0] = R.STACK[1];
			R.STACK[1] = R.STACK[2];
			R.STACK[2] = R.STACK[3];
			R.STACK[3] = (char)(R.PC & ADDR_MASK);
			R.PC = (char)(R.ACC & ADDR_MASK);
		}
    };
    public static opcode_fn call= new opcode_fn() {  public void handler()
		{
			R.PC++ ;
			R.STACK[0] = R.STACK[1];
			R.STACK[1] = R.STACK[2];
			R.STACK[2] = R.STACK[3];
			R.STACK[3] = (char)(R.PC & ADDR_MASK);
			R.PC = (char)(M_RDOP_ARG((char)(R.PC-1)) & ADDR_MASK);
		}
    };
    public static opcode_fn dint= new opcode_fn() {  public void handler()		
    { SET(INTM_FLAG);
    }};
    public static opcode_fn dmov= new opcode_fn() {  public void handler()		{ getdata(0,0); M_WRTRAM((memaccess+1),R.ALU); }};
    public static opcode_fn eint= new opcode_fn() {  public void handler()		{ CLR(INTM_FLAG); }};
    public static opcode_fn in_p= new opcode_fn() {  public void handler()
		{
			R.ALU = M_IN((opcode_major & 7));
			putdata((char)(R.ALU & 0x0000ffff));
		}
    };
    public static opcode_fn lac_sh= new opcode_fn() {  public void handler()
		{
			getdata((opcode_major & 0x0f),1);
			R.ACC = R.ALU;
		}
    };
    public static opcode_fn lack= new opcode_fn() {  public void handler()		{ R.ACC = (opcode_minor & 0x000000ff); }};
    public static opcode_fn lar_ar0= new opcode_fn() {  public void handler()	{ getdata_lar(); R.AR[0] = (char)R.ALU; }};
    public static opcode_fn lar_ar1= new opcode_fn() {  public void handler()	{ getdata_lar(); R.AR[1] = (char)R.ALU; }};
    public static opcode_fn lark_ar0= new opcode_fn() {  public void handler()	{ R.AR[0] = (char)(opcode_minor & 0x00ff); }};
    public static opcode_fn lark_ar1= new opcode_fn() {  public void handler()	{ R.AR[1] = (char)(opcode_minor & 0x00ff); }};
    public static opcode_fn larp_mar= new opcode_fn() {  public void handler()
		{
			if ((opcode_minor & 0x80)!=0) {
				if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
					char/*UINT16*/ tmpAR = R.AR[ARP];
					if ((opcode_minor & 0x20)!=0) tmpAR = tmpAR++ ;
					if ((opcode_minor & 0x10)!=0) tmpAR = tmpAR--;
					R.AR[ARP] = (char)((R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff));
				}
				if ((~opcode_minor & 0x08)!=0) {
					if ((opcode_minor & 0x01)!=0) SET(ARP_REG) ;
					else CLR(ARP_REG);
				}
			}
		}
    };
    public static opcode_fn ldp= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			if ((R.ALU & 1)!=0) SET(DP_REG);
			else CLR(DP_REG);
		}
    };
    public static opcode_fn ldpk= new opcode_fn() {  public void handler()
		{
			if ((opcode_minor & 1)!=0) SET(DP_REG);
			else CLR(DP_REG);
		}
    };
    public static opcode_fn lst= new opcode_fn() {  public void handler()
		{
			tmpacc = R.STR;
			opcode_minor |= 0x08; /* This dont support next arp, so make sure it dont happen */
			getdata(0,0);
			R.STR = (char)R.ALU;
			tmpacc &= INTM_FLAG;
			R.STR |= tmpacc;
			R.STR |= 0x1efe;
		}
    };
    public static opcode_fn lt= new opcode_fn() {  public void handler()		{ getdata(0,0); R.Treg = (char)R.ALU; }};
    public static opcode_fn lta= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.Treg = (char)R.ALU;
			R.ACC += R.Preg;
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x7fffffff;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn ltd= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.Treg = (char)R.ALU;
			R.ACC += R.Preg;
			if (tmpacc > R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x7fffffff;
			}
			else CLR(OV_FLAG);
			M_WRTRAM((memaccess+1),R.ALU);
		}
    };
    public static opcode_fn mpy= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			if ((R.ALU == 0x00008000) && (R.Treg == 0x8000))
				R.Preg = 0xc0000000;
			else R.Preg = (R.ALU * R.Treg);
		}
    };
    public static opcode_fn mpyk= new opcode_fn() {  public void handler()
		{
			if ((opcode & 0x1000)!=0)
				R.Preg = R.Treg * ((opcode & 0x1fff) | 0xe000);
			else R.Preg = R.Treg * (opcode & 0x1fff);
		}
    };
    public static opcode_fn nop= new opcode_fn() {  public void handler()		{ }};
    public static opcode_fn or= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			R.ALU &= 0x0000ffff;
			R.ACC |= R.ALU;
		}
    };
    public static opcode_fn out_p= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			M_OUT((opcode_major & 7), (R.ALU & 0x0000ffff));
		}
    };
    public static opcode_fn pac= new opcode_fn() {  public void handler()		{ R.ACC = R.Preg; }};
    public static opcode_fn pop= new opcode_fn() {  public void handler()
		{
			R.ACC = R.STACK[3] & ADDR_MASK;
			R.STACK[3] = R.STACK[2];
			R.STACK[2] = R.STACK[1];
			R.STACK[1] = R.STACK[0];
		}
    };
    public static opcode_fn push= new opcode_fn() {  public void handler()
		{
			R.STACK[0] = R.STACK[1];
			R.STACK[1] = R.STACK[2];
			R.STACK[2] = R.STACK[3];
			R.STACK[3] =(char)(R.ACC & ADDR_MASK);
		}
    };
    public static opcode_fn ret= new opcode_fn() {  public void handler()
		{
			R.PC = (char)(R.STACK[3] & ADDR_MASK);
			R.STACK[3] = R.STACK[2];
			R.STACK[2] = R.STACK[1];
			R.STACK[1] = R.STACK[0];
		}
    };
    public static opcode_fn rovm= new opcode_fn() {  public void handler()		{ CLR(OVM_FLAG); }};
    public static opcode_fn sach_sh= new opcode_fn() {  public void handler()	{ putdata((char)(((R.ACC << (opcode_major & 7)) >> 16))); }};
    public static opcode_fn sacl= new opcode_fn() {  public void handler()		{ putdata((char)(R.ACC & 0x0000ffff)); }};
    public static opcode_fn sar_ar0= new opcode_fn() {  public void handler()	{ putdata((char)0); }};
    public static opcode_fn sar_ar1= new opcode_fn() {  public void handler()	{ putdata((char)1); }};
    public static opcode_fn sovm= new opcode_fn() {  public void handler()		{ SET(OVM_FLAG); }};
    public static opcode_fn spac= new opcode_fn() {  public void handler()
		{
			int tmpPreg = R.Preg;
			tmpacc = R.ACC ;
			/* if (tmpPreg & 0x8000) tmpPreg |= 0xffff0000; */
			R.ACC -= tmpPreg ;
			if (tmpacc < R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x80000000;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn sst= new opcode_fn() {  public void handler()		{ putdata_sst((char)R.STR); }};
    public static opcode_fn sub_sh= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata((opcode_major & 0x0f),1);
			R.ACC -= R.ALU;
			if (tmpacc < R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x80000000;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn subc= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(15,0);
			tmpacc -= R.ALU;
			if (tmpacc < 0) {
				R.ACC <<= 1;
				SET(OV_FLAG);
			}
			else R.ACC = ((tmpacc << 1) + 1);
		}
    };
    public static opcode_fn subh= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.ACC -= (R.ALU << 16);
			R.ACC &= 0xffff0000;
			R.ACC += (tmpacc & 0x0000ffff);
			if ((tmpacc & 0xffff0000) < (R.ACC & 0xffff0000)) {
				SET(OV_FLAG);
				if (OVM!=0) {
					R.ACC = (tmpacc & 0x0000ffff);
					R.ACC |= 0x80000000 ;
				}
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn subs= new opcode_fn() {  public void handler()
		{
			tmpacc = R.ACC;
			getdata(0,0);
			R.ACC -= R.ALU;
			if (tmpacc < R.ACC) {
				SET(OV_FLAG);
				if (OVM!=0) R.ACC = 0x80000000;
			}
			else CLR(OV_FLAG);
		}
    };
    public static opcode_fn tblr= new opcode_fn() {  public void handler()
		{
			R.ALU = M_RDROM((R.ACC & ADDR_MASK));
			putdata((char)R.ALU);
			R.STACK[0] = R.STACK[1];
		}
    };
    public static opcode_fn tblw= new opcode_fn() {  public void handler()
		{
			getdata(0,0);
			M_WRTROM(((R.ACC & ADDR_MASK)),R.ALU);
			R.STACK[0] = R.STACK[1];
		}
    };
    public static opcode_fn xor= new opcode_fn() {  public void handler()
		{
			tmpacc = (R.ACC & 0xffff0000);
			getdata(0,0);
			R.ACC ^= R.ALU;
			R.ACC &= 0x0000ffff;
			R.ACC |= tmpacc;
		}
    };
    public static opcode_fn zac= new opcode_fn() {  public void handler()		{ R.ACC = 0; }};
    public static opcode_fn zalh= new opcode_fn() {  public void handler()		{ getdata(16,0); R.ACC = R.ALU; }};
    public static opcode_fn zals= new opcode_fn() {  public void handler()		{ getdata(0 ,0); R.ACC = R.ALU; }};
    
    static int cycles_main[]=
    {
    /*00*/		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    /*10*/		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    /*20*/		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    /*30*/		1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
    /*40*/		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    /*50*/		1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1,
    /*60*/		1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1,
    /*70*/		1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 3, 1, 0,
    /*80*/		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    /*90*/		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    /*A0*/		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*B0*/		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*C0*/		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*D0*/		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*E0*/		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    /*F0*/		0, 0, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2
    };

    static int cycles_7F_other[]=
    {
    /*80*/		1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 1, 1,
    /*90*/		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 1,
    };
    static opcode_fn opcode_main[]=
    {
    /*00*/  add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh
    /*08*/ ,add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh		,add_sh
    /*10*/ ,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh
    /*18*/ ,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh		,sub_sh
    /*20*/ ,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh
    /*28*/ ,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh		,lac_sh
    /*30*/ ,sar_ar0		,sar_ar1	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*38*/ ,lar_ar0		,lar_ar1	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*40*/ ,in_p		,in_p		,in_p		,in_p		,in_p		,in_p		,in_p		,in_p
    /*48*/ ,out_p		,out_p		,out_p		,out_p		,out_p		,out_p		,out_p		,out_p
    /*50*/ ,sacl		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*58*/ ,sach_sh		,sach_sh	,sach_sh	,sach_sh	,sach_sh	,sach_sh	,sach_sh	,sach_sh
    /*60*/ ,addh		,adds		,subh		,subs		,subc		,zalh		,zals		,tblr
    /*68*/ ,larp_mar	,dmov		,lt			,ltd		,lta		,mpy		,ldpk		,ldp
    /*70*/ ,lark_ar0	,lark_ar1	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*78*/ ,xor			,and		,or			,lst		,sst		,tblw		,lack		,other_7F_opcodes
    /*80*/ ,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk
    /*88*/ ,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk
    /*90*/ ,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk
    /*98*/ ,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk		,mpyk
    /*A0*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*A8*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*B0*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*B8*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*C0*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*C8*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*D0*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*D8*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*E0*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*E8*/ ,illegal		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*F0*/ ,illegal		,illegal	,illegal	,illegal	,banz		,bv			,bioz		,illegal
    /*F8*/ ,call		,br			,blz		,blez		,bgz		,bgez		,bnz		,bz
    };

    static opcode_fn opcode_7F_other[]=
    {
    /*80*/  nop			,dint		,eint		,illegal	,illegal	,illegal	,illegal	,illegal
    /*88*/ ,abst		,zac		,rovm		,sovm		,cala		,ret		,pac		,apac
    /*90*/ ,spac		,illegal	,illegal	,illegal	,illegal	,illegal	,illegal	,illegal
    /*98*/ ,illegal		,illegal	,illegal	,illegal	,push		,pop		,illegal	,illegal
    };
    @Override
    public void reset(Object param) {
        R.PC  = 0;
	R.STR  = 0x0fefe;
	R.ACC = 0;
	R.pending_irq		= TMS320C10_NOT_PENDING;
	R.BIO_pending_irq	= TMS320C10_NOT_PENDING;
    }

    @Override
    public void exit() {
        
    }
    /****************************************************************************
    * Issue an interrupt if necessary
    ****************************************************************************/

   static int Ext_IRQ()
   {
           if (INTM == 0)
           {
                   if (errorlog!=null) fprintf(errorlog, "TMS320C10:  EXT INTERRUPT\n");
                   SET(INTM_FLAG);
                   R.STACK[0] = R.STACK[1];
                   R.STACK[1] = R.STACK[2];
                   R.STACK[2] = R.STACK[3];
                   R.STACK[3] = (char)(R.PC & ADDR_MASK);
                   R.PC = 0x0002;
                   R.pending_irq = TMS320C10_NOT_PENDING;
                   return 3;  /* 3 clock cycles used due to PUSH and DINT operation ? */
           }
           return 0;
   }
    @Override
    public int execute(int cycles) {
        	tms320c10_ICount[0] = cycles;

	do
	{
		if ((R.pending_irq & TMS320C10_PENDING)!=0)
		{
			int type = R.irq_callback.handler(0);
			R.pending_irq |= type;
		}

		if (R.pending_irq!=0) {
			/* Dont service INT if prev instruction was MPY, MPYK or EINT */
			if ((opcode_major != 0x6d) || ((opcode_major & 0xe0) != 0x80) || (opcode != 0x7f82))
				tms320c10_ICount[0] -= Ext_IRQ();
		}

		R.PREPC = R.PC;

		opcode=M_RDOP(R.PC);

		opcode_major = ((opcode & 0x0ff00) >> 8);
		opcode_minor = (opcode & 0x0ff);

		R.PC++;
		if (opcode_major != 0x07f) { /* Do all opcodes except the 7Fxx ones */
			tms320c10_ICount[0] -= cycles_main[opcode_major];
			opcode_main[opcode_major].handler();
		}
		else { /* Opcode major byte 7Fxx has many opcodes in its minor byte */
			opcode_minr = (opcode & 0x001f);
			tms320c10_ICount[0] -= cycles_7F_other[opcode_minr];
			opcode_7F_other[opcode_minr].handler();
		}
	}
	while (tms320c10_ICount[0]>0);

	return cycles - tms320c10_ICount[0];
    }

    @Override
    public Object init_context() {
        Object reg = new tms320c10_Regs();
        return reg;
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
    public int get_pc() {
        return R.PC & 0xFFFF;
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
        /* TMS320C10 does not have a NMI line */
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        if (irqline == TMS320C10_ACTIVE_INT)
	{
		R.irq_state = state;
		if (state == CLEAR_LINE) R.pending_irq &= ~TMS320C10_PENDING;
		if (state == ASSERT_LINE) R.pending_irq |= TMS320C10_PENDING;
	}
	if (irqline == TMS320C10_ACTIVE_BIO)
	{
		if (state == CLEAR_LINE) R.BIO_pending_irq &= ~TMS320C10_PENDING;
		if (state == ASSERT_LINE) R.BIO_pending_irq |= TMS320C10_PENDING;
	}
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        R.irq_callback = callback;
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
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "320C10";
            case CPU_INFO_FAMILY:
                return "Texas Instruments 320C10";
            case CPU_INFO_VERSION:
                return "1.02";
            case CPU_INFO_FILE:
                return "tms32010.c";
            case CPU_INFO_CREDITS:
                return "Copyright (C) 1999 by Quench";
        }
        throw new UnsupportedOperationException("Unsupported");
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
    
}
