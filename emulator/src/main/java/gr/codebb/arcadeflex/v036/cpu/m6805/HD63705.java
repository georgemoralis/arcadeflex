package gr.codebb.arcadeflex.v036.cpu.m6805;

import static gr.codebb.arcadeflex.v036.cpu.m6805.m6805.m6805_ICount;
import static gr.codebb.arcadeflex.v036.cpu.m6805.m6805H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.ABITS1_16;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.ABITS2_16;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.ABITS_MIN_16;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.CLEAR_LINE;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.CPU_INFO_CREDITS;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.CPU_INFO_NAME;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.CPU_INFO_VERSION;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.CPU_IS_BE;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class HD63705 extends m6805 {

    public HD63705() {
        cpu_num = CPU_HD63705;
        num_irqs = 8;
        default_vector = 0;
        overclock = 1.0;
        no_int = HD63705_INT_NONE;
        irq_int = HD63705_INT_IRQ;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 11;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6805_ICount;
        icount[0] = 50000;
    }

    @Override
    public void reset(Object param) {
        super.reset(param);
        /* Overide default 6805 type */
        m6805.subtype = SUBTYPE_HD63705;
        m6805.amask = 0xffff;
        m6805.sp_mask = 0x17f;
        m6805.sp_low = 0x100;
        RM16(0x1ffe, m6805.pc);
        m6805.s.SetD(0x17f);
    }

    @Override
    public void set_nmi_line(int state) {
        if (m6805.nmi_state == state) {
            return;
        }

        m6805.nmi_state = state;
        if (state != CLEAR_LINE) {
            m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts | HD63705_INT_NMI) & 0xFF);
        }
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        if (irqline > HD63705_INT_ADCONV) {
            return;
        }

        if (m6805.irq_state[irqline] == state) {
            return;
        }
        m6805.irq_state[irqline] = state;
        if (state != CLEAR_LINE) {
            m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts | 1 << irqline) & 0xFF);
        }
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[8][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	m6805_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 8;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///
/*TODO*///    if( !context )
/*TODO*///		r = &m6805;
/*TODO*///
        switch (regnum) {
            case CPU_INFO_NAME:
                return "HD63705";
            case CPU_INFO_VERSION:
                return "1.0";
            case CPU_INFO_CREDITS:
                return "Keith Wilkins, Juergen Buchmueller";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)hd63705_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)hd63705_win_layout;
/*TODO*///		case CPU_INFO_REG+HD63705_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); return buffer[which];
/*TODO*///		case CPU_INFO_REG+HD63705_IRQ1_STATE: sprintf(buffer[which], "IRQ1:%X", r->irq_state[HD63705_INT_IRQ1]); return buffer[which];
/*TODO*///		case CPU_INFO_REG+HD63705_IRQ2_STATE: sprintf(buffer[which], "IRQ2:%X", r->irq_state[HD63705_INT_IRQ2]); return buffer[which];
/*TODO*///		case CPU_INFO_REG+HD63705_ADCONV_STATE: sprintf(buffer[which], "ADCONV:%X", r->irq_state[HD63705_INT_ADCONV]); return buffer[which];
        }
        return super.cpu_info(context, regnum);
    }

    @Override
    public void cpu_state_save(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	char module[8]="hd63705";
/*TODO*///	state_save(file,module);
/*TODO*///	state_save_INT32(file,module,cpu,"IRQ1_STATE", &m6805.irq_state[0], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"IRQ2_STATE", &m6805.irq_state[1], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"TIMER1_STATE", &m6805.irq_state[2], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"TIMER2_STATE", &m6805.irq_state[3], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"TIMER3_STATE", &m6805.irq_state[4], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"PCI_STATE", &m6805.irq_state[5], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"SCI_STATE", &m6805.irq_state[6], 1);
/*TODO*///	state_save_INT32(file,module,cpu,"ADCONV_STATE", &m6805.irq_state[7], 1);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cpu_state_load(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	char module[8]="hd63705";
/*TODO*///	state_load(file,module);
/*TODO*///	state_load_INT32(file,module,cpu,"IRQ1_STATE", &m6805.irq_state[0], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"IRQ2_STATE", &m6805.irq_state[1], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"TIMER1_STATE", &m6805.irq_state[2], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"TIMER2_STATE", &m6805.irq_state[3], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"TIMER3_STATE", &m6805.irq_state[4], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"PCI_STATE", &m6805.irq_state[5], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"SCI_STATE", &m6805.irq_state[6], 1);
/*TODO*///	state_load_INT32(file,module,cpu,"ADCONV_STATE", &m6805.irq_state[7], 1);  
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
