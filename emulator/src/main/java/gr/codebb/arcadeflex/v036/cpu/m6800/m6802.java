package gr.codebb.arcadeflex.v036.cpu.m6800;

import arcadeflex.v036.mame.cpuintrfH.cpu_interface;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.m6800H.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;

public class m6802 extends m6800 {

    public m6802() {
        cpu_num = CPU_M6802;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6800_INT_NONE;
        irq_int = M6800_INT_IRQ;
        nmi_int = M6800_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6800_ICount;
        icount[0] = 50000;
    }

    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "M6802";
            case CPU_INFO_FAMILY:
                return "Motorola 6800";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "m6800.c";
            case CPU_INFO_CREDITS:
                return "The MAME team.";
        }
        throw new UnsupportedOperationException("Unsupported");
    }
}
