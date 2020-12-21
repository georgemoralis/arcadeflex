package gr.codebb.arcadeflex.v036.cpu.m6805;

import static gr.codebb.arcadeflex.v036.cpu.m6805.m6805.m6805_ICount;
import static gr.codebb.arcadeflex.v036.cpu.m6805.m6805H.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.ABITS1_16;
import static gr.codebb.arcadeflex.v036.mame.memoryH.ABITS2_16;
import static gr.codebb.arcadeflex.v036.mame.memoryH.ABITS_MIN_16;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.CPU_INFO_NAME;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.CPU_INFO_VERSION;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.CPU_IS_BE;
import static gr.codebb.arcadeflex.v036.mame.driverH.CPU_M68705;

public class m68705 extends m6805 {

    public m68705() {
        cpu_num = CPU_M68705;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M68705_INT_NONE;
        irq_int = M68705_INT_IRQ;
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
        m6805.subtype = SUBTYPE_M68705;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "M68705";
            case CPU_INFO_VERSION:
                return "1.1";
        }
        return super.cpu_info(context, regnum);
    }
}
