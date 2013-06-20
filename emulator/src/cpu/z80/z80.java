/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.z80;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.z80.z80H.*;

/**
 *
 * @author george
 */
public class z80 extends cpu_interface {

    public z80()
    {    
        cpu_num = CPU_Z80;
        num_irqs = 1;
        default_vector = 255;
        overclock = 1.0;
        no_int = Z80_IGNORE_INT;
        irq_int = Z80_IRQ_INT;
        nmi_int = Z80_NMI_INT;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        
        //intialize interfaces
        burn = burn_function;
    }
    @Override
    public String cpu_info(Object context, int regnum) {
    /*TODO*/ // 	static char buffer[32][47+1];
    /*TODO*/ // 	static int which = 0;
    /*TODO*/ // 	Z80_Regs *r = context;

    /*TODO*/ // 	which = ++which % 32;
    /*TODO*/ //     buffer[which][0] = '\0';
    /*TODO*/ // 	if( !context )
    /*TODO*/ // 		r = &Z80;

            switch( regnum )
            {
/*TODO*/ //                     case CPU_INFO_REG+Z80_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_SP: sprintf(buffer[which], "SP:%04X", r->SP.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_AF: sprintf(buffer[which], "AF:%04X", r->AF.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_BC: sprintf(buffer[which], "BC:%04X", r->BC.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_DE: sprintf(buffer[which], "DE:%04X", r->DE.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_HL: sprintf(buffer[which], "HL:%04X", r->HL.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IX: sprintf(buffer[which], "IX:%04X", r->IX.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IY: sprintf(buffer[which], "IY:%04X", r->IY.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_R: sprintf(buffer[which], "R:%02X", (r->R & 0x7f) | (r->R2 & 0x80)); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_I: sprintf(buffer[which], "I:%02X", r->I); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_AF2: sprintf(buffer[which], "AF'%04X", r->AF2.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_BC2: sprintf(buffer[which], "BC'%04X", r->BC2.w.l); break;
 /*TODO*/ //                    case CPU_INFO_REG+Z80_DE2: sprintf(buffer[which], "DE'%04X", r->DE2.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_HL2: sprintf(buffer[which], "HL'%04X", r->HL2.w.l); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IM: sprintf(buffer[which], "IM:%X", r->IM); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IFF1: sprintf(buffer[which], "IFF1:%X", r->IFF1); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IFF2: sprintf(buffer[which], "IFF2:%X", r->IFF2); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_HALT: sprintf(buffer[which], "HALT:%X", r->HALT); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_DC0: if(Z80.irq_max >= 1) sprintf(buffer[which], "DC0:%X", r->int_state[0]); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_DC1: if(Z80.irq_max >= 2) sprintf(buffer[which], "DC1:%X", r->int_state[1]); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_DC2: if(Z80.irq_max >= 3) sprintf(buffer[which], "DC2:%X", r->int_state[2]); break;
/*TODO*/ //                     case CPU_INFO_REG+Z80_DC3: if(Z80.irq_max >= 4) sprintf(buffer[which], "DC3:%X", r->int_state[3]); break;
/*TODO*/ //             case CPU_INFO_FLAGS:
/*TODO*/ //                             sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*/ //                                     r->AF.b.l & 0x80 ? 'S':'.',
/*TODO*/ //                                     r->AF.b.l & 0x40 ? 'Z':'.',
/*TODO*/ //                                     r->AF.b.l & 0x20 ? '5':'.',
/*TODO*/ //                                     r->AF.b.l & 0x10 ? 'H':'.',
/*TODO*/ //                                     r->AF.b.l & 0x08 ? '3':'.',
/*TODO*/ //                                     r->AF.b.l & 0x04 ? 'P':'.',
/*TODO*/ //                                     r->AF.b.l & 0x02 ? 'N':'.',
/*TODO*/ //                                     r->AF.b.l & 0x01 ? 'C':'.');
/*TODO*/ //                             break;
                    case CPU_INFO_NAME: return "Z80";
                    case CPU_INFO_FAMILY: return "Zilog Z80";
                    case CPU_INFO_VERSION: return "2.7";
/*TODO*/ //                     case CPU_INFO_FILE: return __FILE__;
                    case CPU_INFO_CREDITS: return "Copyright (C) 1998,1999 Juergen Buchmueller, all rights reserved.";
/*TODO*/ //                     case CPU_INFO_REG_LAYOUT: return (const char *)z80_reg_layout;
/*TODO*/ //                     case CPU_INFO_WIN_LAYOUT: return (const char *)z80_win_layout;
            }
/*TODO*/ //             return buffer[which];
            throw new UnsupportedOperationException("unsupported z80 cpu_info");
    }
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
     throw new UnsupportedOperationException("Not supported yet.");
    }};
}
