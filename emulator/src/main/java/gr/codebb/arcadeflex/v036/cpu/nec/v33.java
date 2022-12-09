
package gr.codebb.arcadeflex.v036.cpu.nec;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.cpu.nec.necH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;

/**
 *
 * @author shadow
 */
public class v33 extends v30{
        public v33() {
        cpu_num = CPU_V33;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.05;
        no_int = NEC_INT_NONE;
        irq_int = -1000;
        nmi_int = NEC_NMI_INT;
        address_shift = 0;
        address_bits = 20;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 5;
        abits1 = ABITS1_20;
        abits2 = ABITS2_20;
        abitsmin = ABITS_MIN_20;
        icount = nec_ICount;
    }
    public String cpu_info(Object context, int regnum) {
    /*TODO*///    static char buffer[32][63+1];
    /*TODO*///    static int which = 0;
    /*TODO*///    nec_Regs *r = context;
    /*TODO*///
    /*TODO*///    which = ++which % 32;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///    if( !context )
    /*TODO*///        r = &I;
    /*TODO*///
       switch( regnum )
        {
    /*TODO*///        case CPU_INFO_REG+NEC_IP: sprintf(buffer[which], "IP:%04X", r->ip); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SP: sprintf(buffer[which], "SP:%04X", r->regs.w[SP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_FLAGS: sprintf(buffer[which], "F:%04X", r->flags); break;
    /*TODO*///        case CPU_INFO_REG+NEC_AW: sprintf(buffer[which], "AW:%04X", r->regs.w[AW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CW: sprintf(buffer[which], "CW:%04X", r->regs.w[CW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DW: sprintf(buffer[which], "DW:%04X", r->regs.w[DW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BW: sprintf(buffer[which], "BW:%04X", r->regs.w[BW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BP: sprintf(buffer[which], "BP:%04X", r->regs.w[BP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IX: sprintf(buffer[which], "IX:%04X", r->regs.w[IX]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IY: sprintf(buffer[which], "IY:%04X", r->regs.w[IY]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_ES: sprintf(buffer[which], "ES:%04X", r->sregs[ES]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CS: sprintf(buffer[which], "CS:%04X", r->sregs[CS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SS: sprintf(buffer[which], "SS:%04X", r->sregs[SS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DS: sprintf(buffer[which], "DS:%04X", r->sregs[DS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_VECTOR: sprintf(buffer[which], "V:%02X", r->int_vector); break;
    /*TODO*///        case CPU_INFO_REG+NEC_PENDING: sprintf(buffer[which], "P:%X", r->pending_irq); break;
    /*TODO*///        case CPU_INFO_REG+NEC_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///        case CPU_INFO_FLAGS:
    /*TODO*///            r->flags = CompressFlags();
    /*TODO*///            sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
    /*TODO*///                r->flags & 0x8000 ? 'M':'.',
    /*TODO*///                r->flags & 0x4000 ? '?':'.',
    /*TODO*///                r->flags & 0x2000 ? '?':'.',
    /*TODO*///                r->flags & 0x1000 ? '?':'.',
    /*TODO*///                r->flags & 0x0800 ? 'O':'.',
    /*TODO*///                r->flags & 0x0400 ? 'D':'.',
    /*TODO*///                r->flags & 0x0200 ? 'I':'.',
    /*TODO*///                r->flags & 0x0100 ? 'T':'.',
    /*TODO*///                r->flags & 0x0080 ? 'S':'.',
    /*TODO*///                r->flags & 0x0040 ? 'Z':'.',
    /*TODO*///                r->flags & 0x0020 ? '?':'.',
    /*TODO*///                r->flags & 0x0010 ? 'A':'.',
    /*TODO*///                r->flags & 0x0008 ? '?':'.',
    /*TODO*///                r->flags & 0x0004 ? 'P':'.',
    /*TODO*///                r->flags & 0x0002 ? 'N':'.',
    /*TODO*///                r->flags & 0x0001 ? 'C':'.');
    /*TODO*///            break;
            case CPU_INFO_NAME: return "V33";
            case CPU_INFO_FAMILY: return "NEC V-Series";
            case CPU_INFO_VERSION: return "1.6";
            case CPU_INFO_FILE: return "v33.java";
            case CPU_INFO_CREDITS: return "Real mode NEC emulator v1.3 by Oliver Bergmann\n(initial work based on Fabrice Fabian's i86 core)";
    /*TODO*///        case CPU_INFO_REG_LAYOUT: return (const char*)nec_reg_layout;
    /*TODO*///        case CPU_INFO_WIN_LAYOUT: return (const char*)nec_win_layout;
        }
    /*TODO*///    return buffer[which];
       throw new UnsupportedOperationException("unsupported v30 cpu_info");
    }
}
