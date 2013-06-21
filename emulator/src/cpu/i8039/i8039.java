/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.i8039;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.i8039.i8039H.*;

public class i8039 extends cpu_interface{
    public i8039()
    {
          cpu_num = CPU_I8039;
          num_irqs = 1;
          default_vector = 0;
          overclock = 1.0;
          no_int = I8039_IGNORE_INT;
          irq_int = I8039_EXT_INT;
          nmi_int = -1;
          address_bits = 16;
          address_shift = 0;
          endianess = CPU_IS_LE;
          align_unit = 1;
          max_inst_len = 2;
          abits1 = ABITS1_16;
          abits2 = ABITS2_16;
          abitsmin = ABITS_MIN_16;
          
        //intialize interfaces
        burn = burn_function;
    }
    @Override
    public String cpu_info(Object context, int regnum) {
    /*TODO*///	static char buffer[8][47+1];
    /*TODO*///	static int which = 0;
    /*TODO*///    I8039_Regs *r = context;
    /*TODO*///
    /*TODO*///	which = ++which % 8;
    /*TODO*///	buffer[which][0] = '\0';
    /*TODO*///	if( !context )
    /*TODO*///		r = &R;
    /*TODO*///
        switch( regnum )
        {
    /*TODO*///		case CPU_INFO_REG+I8039_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
    /*TODO*///		case CPU_INFO_REG+I8039_SP: sprintf(buffer[which], "SP:%02X", r->SP); break;
    /*TODO*///		case CPU_INFO_REG+I8039_PSW: sprintf(buffer[which], "PSW:%02X", r->PSW); break;
    /*TODO*///        case CPU_INFO_REG+I8039_A: sprintf(buffer[which], "A:%02X", r->A); break;
    /*TODO*///		case CPU_INFO_REG+I8039_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R0: sprintf(buffer[which], "R0:%02X", r->RAM[r->regPtr+0]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R1: sprintf(buffer[which], "R1:%02X", r->RAM[r->regPtr+1]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R2: sprintf(buffer[which], "R2:%02X", r->RAM[r->regPtr+2]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R3: sprintf(buffer[which], "R3:%02X", r->RAM[r->regPtr+3]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R4: sprintf(buffer[which], "R4:%02X", r->RAM[r->regPtr+4]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R5: sprintf(buffer[which], "R5:%02X", r->RAM[r->regPtr+5]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R6: sprintf(buffer[which], "R6:%02X", r->RAM[r->regPtr+6]); break;
    /*TODO*///		case CPU_INFO_REG+I8039_R7: sprintf(buffer[which], "R7:%02X", r->RAM[r->regPtr+7]); break;
    /*TODO*///		case CPU_INFO_FLAGS:
    /*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
    /*TODO*///				r->PSW & 0x80 ? 'C':'.',
    /*TODO*///				r->PSW & 0x40 ? 'A':'.',
    /*TODO*///				r->PSW & 0x20 ? 'F':'.',
    /*TODO*///				r->PSW & 0x10 ? 'B':'.',
    /*TODO*///				r->PSW & 0x08 ? '?':'.',
    /*TODO*///				r->PSW & 0x04 ? '4':'.',
    /*TODO*///				r->PSW & 0x02 ? '2':'.',
    /*TODO*///				r->PSW & 0x01 ? '1':'.');
    /*TODO*///			break;
                    case CPU_INFO_NAME: return "I8039";
                    case CPU_INFO_FAMILY: return "Intel 8039";
                    case CPU_INFO_VERSION: return "1.1";
    /*TODO*///		case CPU_INFO_FILE: return __FILE__;
                    case CPU_INFO_CREDITS: return "Copyright (C) 1997 by Mirko Buffoni\nBased on the original work (C) 1997 by Dan Boris";
    /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)i8039_reg_layout;
    /*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)i8039_win_layout;
        }
        throw new UnsupportedOperationException("unsupported i8039 cpu_info");
    /*TODO*///    return buffer[which];
    }    
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
     throw new UnsupportedOperationException("Not supported yet.");
    }};

    @Override
    public void init_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
