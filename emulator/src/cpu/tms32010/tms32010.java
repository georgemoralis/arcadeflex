package cpu.tms32010;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.tms32010.tms32010H.*;
import static mame.memory.*;
import static arcadeflex.libc_old.*;
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
    public static class tms320c10_Regs {
        int/*UINT16*/	PREPC;		/* previous program counter */
	int/*UINT16*/  PC;
	int  ACC, Preg;
	int   ALU;
	int/*UINT16*/  Treg;
	int[]/*UINT16*/  AR=new int[2];
        int[]/*UINT16*/STACK=new int[4];
        int/*UINT16*/STR;
	int     pending_irq, BIO_pending_irq;
	int     irq_state;
	public irqcallbacksPtr irq_callback;
    }
    static /*UINT16*/int   opcode=0;
    static /*UINT8*/int opcode_major=0, opcode_minor, opcode_minr;	/* opcode split into MSB and LSB */
    static tms320c10_Regs R=new tms320c10_Regs();
    static int tmpacc;
    static /*UINT16*/int memaccess;
    
    public static abstract interface opcode_fn {

        public abstract void handler();
    }
    public static int M_RDROM(int A){	return ((cpu_readmem16((A<<1))<<8) | cpu_readmem16(((A<<1)+1)))&0xFFFF; }
    public static void M_WRTROM(int A,int V) { cpu_writemem16(((A<<1)+1),(V&0xff)); cpu_writemem16((A<<1),((V>>8)&0xff)); }
    public static int M_RDRAM(int A){ return ((cpu_readmem16((A<<1)|0x8000)<<8) | cpu_readmem16(((A<<1)|0x8001)))&0xFFFF; }
    public static void M_WRTRAM(int A,int V)	{ cpu_writemem16(((A<<1)|0x8001),(V&0x0ff)); cpu_writemem16(((A<<1)|0x8000),((V>>8)&0x0ff)); }
    public static int M_RDOP(int A)		{ return ((cpu_readop((A<<1))<<8) | cpu_readop(((A<<1)+1)))&0xFFFF; }
    public static int M_RDOP_ARG(int A)	{ return ((cpu_readop_arg((A<<1))<<8) | cpu_readop_arg(((A<<1)+1)))&0xFFFF; }
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
    public static void memacc()	 { memaccess = (opcode_minor & 0x80)!=0 ? ind : dma;}
    
    
    public static void CLR(/*UINT16*/int flag) { R.STR &= ~flag; R.STR |= 0x1efe; }
    public static void SET(/*UINT16*/int flag) { R.STR |=  flag; R.STR |= 0x1efe; }
    
    static void getdata(/*UINT8*/int shift,/*UINT8*/int signext)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = ind&0xFFFF;
            else memaccess = dma&0xFFFF;
            R.ALU = M_RDRAM(memaccess);
            if ((signext!=0) && (R.ALU & 0x8000)!=0) R.ALU |= 0xffff0000;
            else R.ALU &= 0x0000ffff;
            R.ALU <<= shift;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/int tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = (tmpAR+1)&0xFFFF;//tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = (tmpAR-1)&0xFFFF;//tmpAR-- ;
                            R.AR[ARP] = (R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff);//check if it has to unsigned???(shadow)
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
    }
    static void getdata_lar()
    {
            if ((opcode_minor & 0x80)!=0) memaccess = ind&0xFFFF;
            else memaccess = dma&0xFFFF;
            R.ALU = M_RDRAM(memaccess);
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            if ((opcode_major & 1) != ARP) {
                                /*UINT16*/int tmpAR = R.AR[ARP];
                                if ((opcode_minor & 0x20)!=0) tmpAR = (tmpAR+1)&0xFFFF;//tmpAR++ ;
                                if ((opcode_minor & 0x10)!=0) tmpAR = (tmpAR-1)&0xFFFF;//tmpAR-- ;
                                 R.AR[ARP] = (R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff);//check if it has to unsigned???(shadow)
                            }
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
    }
    static void putdata(/*UINT16*/int data)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = ind&0xFFFF;
            else memaccess = dma&0xFFFF;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/int tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = (tmpAR+1)&0xFFFF;//tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = (tmpAR-1)&0xFFFF;//tmpAR-- ;
                            R.AR[ARP] = (R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff);
                    }
                    if ((~opcode_minor & 0x08)!=0) {
                            if ((opcode_minor & 1)!=0) SET(ARP_REG);
                            else CLR(ARP_REG);
                    }
            }
            if ((opcode_major == 0x30) || (opcode_major == 0x31)) {
                    M_WRTRAM(memaccess,(R.AR[data])); }
            else M_WRTRAM(memaccess,(data&0xffff));
    }
    static void putdata_sst(/*UINT16*/int data)
    {
            if ((opcode_minor & 0x80)!=0) memaccess = ind&0xFFFF;
            else memaccess = dmapage1&0xFFFF;
            if ((opcode_minor & 0x80)!=0) {
                    if ((opcode_minor & 0x20)!=0 || (opcode_minor & 0x10)!=0) {
                            /*UINT16*/int tmpAR = R.AR[ARP];
                            if ((opcode_minor & 0x20)!=0) tmpAR = (tmpAR+1)&0xFFFF;//tmpAR++ ;
                            if ((opcode_minor & 0x10)!=0) tmpAR = (tmpAR-1)&0xFFFF;//tmpAR-- ;
                            R.AR[ARP] = (R.AR[ARP] & 0xfe00) | (tmpAR & 0x01ff);
                    }
            }
            M_WRTRAM(memaccess,(data&0xffff));
    }
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
                   R.STACK[3] = (R.PC & ADDR_MASK)&0xFFFF;
                   R.PC = 0x0002;
                   R.pending_irq = TMS320C10_NOT_PENDING;
                   return 3;  /* 3 clock cycles used due to PUSH and DINT operation ? */
           }
           return 0;
   }
    @Override
    public int execute(int cycles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        cpu_setOPbase16.handler(pc, 0);
    }
    
}
