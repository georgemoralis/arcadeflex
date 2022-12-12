package gr.codebb.arcadeflex.v037b7.cpu.i8085;

import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085H.*;
import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085cpuH.*;

public class i8080 extends i8085 {
    
    public i8080() {
        cpu_num = CPU_8080;
        num_irqs = 4;
        default_vector = 255;
        overclock = 1.0;
        no_int = I8080_NONE;
        irq_int = I8080_INTR;
        nmi_int = I8080_TRAP;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = i8085_ICount;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "8080";
            case CPU_INFO_VERSION:
                return "1.2";
        }
        return super.cpu_info(context, regnum);
    }

    @Override
    public void reset(Object param) {
        super.reset(param);
        I.cputype = 0;
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        I.irq_state[irqline] = state;
        if (state == CLEAR_LINE) {
            if ((I.u8_IM & IM_IEN) == 0) {
                i8085_set_INTR(0);
            }
        } else {
            if ((I.u8_IM & IM_IEN) != 0) {
                i8085_set_INTR(1);
            }
        }
    }

    @Override
    public void cpu_state_save(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "AF", &I.AF.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "BC", &I.BC.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "DE", &I.DE.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "HL", &I.HL.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "SP", &I.SP.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8080", cpu, "PC", &I.PC.w.l, 1);
/*TODO*///	state_save_UINT8(file, "i8080", cpu, "HALT", &I.HALT, 1);
/*TODO*///	state_save_UINT8(file, "i8080", cpu, "IREQ", &I.IREQ, 1);
/*TODO*///	state_save_UINT8(file, "i8080", cpu, "ISRV", &I.ISRV, 1);
/*TODO*///	state_save_UINT32(file, "i8080", cpu, "INTR", &I.INTR, 1);
/*TODO*///	state_save_UINT32(file, "i8080", cpu, "IRQ2", &I.IRQ2, 1);
/*TODO*///	state_save_UINT32(file, "i8080", cpu, "IRQ1", &I.IRQ1, 1);
/*TODO*///	state_save_INT8(file, "i8080", cpu, "NMI_STATE", &I.nmi_state, 1);
/*TODO*///	state_save_INT8(file, "i8080", cpu, "IRQ_STATE", I.irq_state, 1);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_load(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "AF", &I.AF.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "BC", &I.BC.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "DE", &I.DE.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "HL", &I.HL.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "SP", &I.SP.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8080", cpu, "PC", &I.PC.w.l, 1);
/*TODO*///	state_load_UINT8(file, "i8080", cpu, "HALT", &I.HALT, 1);
/*TODO*///	state_load_UINT8(file, "i8080", cpu, "IREQ", &I.IREQ, 1);
/*TODO*///	state_load_UINT8(file, "i8080", cpu, "ISRV", &I.ISRV, 1);
/*TODO*///	state_load_UINT32(file, "i8080", cpu, "INTR", &I.INTR, 1);
/*TODO*///	state_load_UINT32(file, "i8080", cpu, "IRQ2", &I.IRQ2, 1);
/*TODO*///	state_load_UINT32(file, "i8080", cpu, "IRQ1", &I.IRQ1, 1);
/*TODO*///	state_load_INT8(file, "i8080", cpu, "NMI_STATE", &I.nmi_state, 1);
/*TODO*///	state_load_INT8(file, "i8080", cpu, "IRQ_STATE", I.irq_state, 1);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
