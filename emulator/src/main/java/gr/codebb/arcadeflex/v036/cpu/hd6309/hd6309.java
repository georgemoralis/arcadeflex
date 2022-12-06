

package gr.codebb.arcadeflex.v036.cpu.hd6309;

//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
//TODO
import gr.codebb.arcadeflex.v036.cpu.m6809.m6809;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;

public class hd6309 extends m6809
{
    public hd6309()
    {
        cpu_num = CPU_HD6309;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = HD6309_INT_NONE;
        irq_int = HD6309_INT_IRQ;
        nmi_int = HD6309_INT_NMI;
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
    public String cpu_info(Object context, int regnum) {
        switch( regnum )
        {
            case CPU_INFO_NAME: return "HD6309";
        }
        return super.cpu_info(context, regnum);
    }
}
