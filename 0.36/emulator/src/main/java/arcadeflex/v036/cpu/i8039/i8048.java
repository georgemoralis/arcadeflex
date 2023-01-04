/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.i8039;

//cpu imports
import static arcadeflex.v036.cpu.i8039.i8039H.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.driverH.*;

public class i8048 extends i8039 {

    public i8048() {
        cpu_num = CPU_I8048;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = I8048_IGNORE_INT;
        irq_int = I8048_EXT_INT;
        nmi_int = -1;
        address_bits = 16;
        address_shift = 0;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 2;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = i8039_ICount;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "I8048";
            case CPU_INFO_VERSION:
                return "1.1";
        }
        return super.cpu_info(context, regnum);
    }
}
