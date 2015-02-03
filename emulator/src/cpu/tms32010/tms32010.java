package cpu.tms32010;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.tms32010.tms32010H.*;
import static mame.memory.*;
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
    
    public static abstract interface opcode_fn {

        public abstract void handler();
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
