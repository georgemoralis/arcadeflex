package cpu.m68000;

import static arcadeflex.libc_old.fclose;
import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m68000.m68000H.*;
import static cpu.m68000.m68kH.*;
import static cpu.m68000.m68kopsH.*;
import static cpu.m68000.m68kops.*;
import static cpu.m68000.m68kcpu.*;

public class m68kmame extends cpu_interface {

    public m68kmame() {
        cpu_num = CPU_M68000;
        num_irqs = 8;
        default_vector = -1;
        overclock = 1.0;
        no_int = MC68000_INT_NONE;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 24;
        endianess = CPU_IS_BE;
        align_unit = 2;
        max_inst_len = 10;
        abits1 = ABITS1_24;
        abits2 = ABITS2_24;
        abitsmin = ABITS_MIN_24;
        icount = m68k_clks_left;
        m68k_clks_left[0] = 0;
    }

    @Override
    public void reset(Object param) {
        m68k_pulse_reset(param);
    }

    @Override
    public void exit() {
        /* nothing to do ? */
    }

    @Override
    public int execute(int cycles) {
        return m68k_execute(cycles);
    }

    @Override
    public Object init_context() {
        Object reg = new m68k_cpu_context();
        return reg;
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	if( dst )
/*TODO*///		m68k_get_context(dst);
/*TODO*///	return sizeof(m68k_cpu_context);
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	if( src )
        /*TODO*///		m68k_set_context(src);
    }

    @Override
    public int get_pc() {
        return (int) m68k_peek_pc();
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	m68k_poke_pc(val);
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	return m68k_peek_isp();
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	m68k_poke_isp(val);
    }

    @Override
    public int get_reg(int regnum) {
        
        
            switch( regnum )
    {
/*TODO*///		case M68K_PC: return m68k_peek_pc();
/*TODO*///        case M68K_ISP: return m68k_peek_isp();
/*TODO*///        case M68K_USP: return m68k_peek_usp();
		case /*M68K_SR*/4: return (int)m68k_peek_sr();
/*TODO*///        case M68K_VBR: return 0; /* missing m68k_peek_vbr(); */
/*TODO*///		case M68K_SFC: return 0; /* missing m68k_peek_sfc(); */
/*TODO*///		case M68K_DFC: return 0; /* missing m68k_peek_dfc(); */
/*TODO*///		case M68K_D0: return m68k_peek_dr(0);
/*TODO*///		case M68K_D1: return m68k_peek_dr(1);
/*TODO*///		case M68K_D2: return m68k_peek_dr(2);
/*TODO*///		case M68K_D3: return m68k_peek_dr(3);
/*TODO*///		case M68K_D4: return m68k_peek_dr(4);
/*TODO*///		case M68K_D5: return m68k_peek_dr(5);
/*TODO*///		case M68K_D6: return m68k_peek_dr(6);
/*TODO*///		case M68K_D7: return m68k_peek_dr(7);
/*TODO*///		case M68K_A0: return m68k_peek_ar(0);
/*TODO*///		case M68K_A1: return m68k_peek_ar(1);
/*TODO*///		case M68K_A2: return m68k_peek_ar(2);
/*TODO*///		case M68K_A3: return m68k_peek_ar(3);
/*TODO*///		case M68K_A4: return m68k_peek_ar(4);
/*TODO*///		case M68K_A5: return m68k_peek_ar(5);
/*TODO*///		case M68K_A6: return m68k_peek_ar(6);
/*TODO*///		case M68K_A7: return m68k_peek_ar(7);
/*TODO*///		case REG_PREVIOUSPC: return m68k_peek_ppc();
/*TODO*////* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
		default:
                    throw new UnsupportedOperationException("Not supported yet.");
/*TODO*///			if( regnum < REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = m68k_peek_isp() + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xfffffd )
/*TODO*///					return cpu_readmem24_dword( offset );
/*TODO*///			}
    }
/*TODO*///    return 0;
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///    switch( regnum )
/*TODO*///    {
/*TODO*///		case M68K_PC: m68k_poke_pc(val); break;
/*TODO*///		case M68K_ISP: m68k_poke_isp(val); break;
/*TODO*///		case M68K_USP: m68k_poke_usp(val); break;
/*TODO*///		case M68K_SR: m68k_poke_sr(val); break;
/*TODO*///		case M68K_VBR: /* missing m68k_poke_vbr(val); */ break;
/*TODO*///		case M68K_SFC: /* missing m68k_poke_sfc(val); */ break;
/*TODO*///		case M68K_DFC: /* missing m68k_poke_dfc(val); */ break;
/*TODO*///		case M68K_D0: m68k_poke_dr(0,val); break;
/*TODO*///		case M68K_D1: m68k_poke_dr(1,val); break;
/*TODO*///		case M68K_D2: m68k_poke_dr(2,val); break;
/*TODO*///		case M68K_D3: m68k_poke_dr(3,val); break;
/*TODO*///		case M68K_D4: m68k_poke_dr(4,val); break;
/*TODO*///		case M68K_D5: m68k_poke_dr(5,val); break;
/*TODO*///		case M68K_D6: m68k_poke_dr(6,val); break;
/*TODO*///		case M68K_D7: m68k_poke_dr(7,val); break;
/*TODO*///		case M68K_A0: m68k_poke_ar(0,val); break;
/*TODO*///		case M68K_A1: m68k_poke_ar(1,val); break;
/*TODO*///		case M68K_A2: m68k_poke_ar(2,val); break;
/*TODO*///		case M68K_A3: m68k_poke_ar(3,val); break;
/*TODO*///		case M68K_A4: m68k_poke_ar(4,val); break;
/*TODO*///		case M68K_A5: m68k_poke_ar(5,val); break;
/*TODO*///		case M68K_A6: m68k_poke_ar(6,val); break;
/*TODO*///		case M68K_A7: m68k_poke_ar(7,val); break;
/*TODO*////* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///		default:
/*TODO*///			if( regnum < REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = m68k_peek_isp() + 4 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xfffffd )
/*TODO*///					cpu_writemem24_dword( offset, val );
/*TODO*///			}
/*TODO*///    }
    }

    @Override
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	switch(state)
/*TODO*///	{
/*TODO*///		case CLEAR_LINE:
/*TODO*///			m68k_clear_irq(7);
/*TODO*///			return;
/*TODO*///		case ASSERT_LINE:
/*TODO*///			m68k_assert_irq(7);
/*TODO*///			return;
/*TODO*///		default:
/*TODO*///			m68k_assert_irq(7);
/*TODO*///			return;
/*TODO*///	}
    }

    @Override
    public void set_irq_line(int irqline, int state) {

        switch (state) {
            case CLEAR_LINE:
                m68k_clear_irq(irqline);
                return;
            case ASSERT_LINE:
                m68k_assert_irq(irqline);
                return;
            default:
                m68k_assert_irq(irqline);
                return;
        }
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        m68k_set_int_ack_callback(callback);
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
                return "68000";
            case CPU_INFO_FAMILY:
                return "Motorola 68K";
            case CPU_INFO_VERSION:
                return "2.1";
            case CPU_INFO_FILE:
                return "m68000.java";
            case CPU_INFO_CREDITS:
                return "Copyright 1999 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
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
        cpu_setOPbase24.handler(pc, 0);
    }
}
