package cpu.h6280;

import static arcadeflex.libc_old.*;
import static mame.cpuintrfH.*;
import static arcadeflex.libc_v2.*;
import static cpu.h6280.h6280H.H6280_INT_NMI;
import static cpu.h6280.h6280H.H6280_INT_NONE;
import static cpu.h6280.h6280H.H6280_IRQ1_VEC;
import static cpu.h6280.h6280H.H6280_IRQ2_VEC;
import static cpu.h6280.h6280H.H6280_NMI_VEC;
import static cpu.h6280.h6280H.H6280_RESET_VEC;
import static cpu.h6280.h6280H.H6280_TIMER_VEC;
import static mame.memory.cpu_readmem21;
import static mame.memory.cpu_setOPbase21;
import static mame.memory.cpu_writemem21;
import static mame.driverH.CPU_H6280;
import static mame.memoryH.*;
//import static arcadeflex.osdepend.logerror;
import static mame.cpuintrf.cpu_get_pc;
import static mame.driverH.*;
import static mame.memory.cpu_readmem21;
import static mame.memory.cpu_setOPbase21;
import static mame.memory.cpu_writemem21;
import static mame.memory.cpu_writeport;

public class h6280 extends cpu_interface {

    public static FILE h6280log = null;//fopen("h6280.log", "wa");  //for debug purposes

    public static int[] h6280_ICount = new int[1];

    public h6280() {
        cpu_num = CPU_H6280;
        num_irqs = 3;
        default_vector = 0;
        overclock = 1.0;
        no_int = H6280_INT_NONE;
        irq_int = -1;
        nmi_int = H6280_INT_NMI;
        address_bits = 21;
        address_shift = 0;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_21;
        abits2 = ABITS2_21;
        abitsmin = ABITS_MIN_21;
        icount = h6280_ICount;
    }

    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*TODO*///static UINT8 reg_layout[] = {
/*TODO*///	H6280_PC, H6280_S, H6280_P, H6280_A, H6280_X, H6280_Y, -1,
/*TODO*///	H6280_IRQ_MASK, H6280_TIMER_STATE, H6280_NMI_STATE, H6280_IRQ1_STATE, H6280_IRQ2_STATE, H6280_IRQT_STATE,
/*TODO*///	0
/*TODO*///};
/*TODO*///
/*TODO*////* Layout of the debugger windows x,y,w,h */
/*TODO*///static UINT8 win_layout[] = {
/*TODO*///	25, 0,55, 4,	/* register window (top rows) */
/*TODO*///	 0, 0,24,22,	/* disassembler window (left colums) */
/*TODO*///	25, 5,55, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///	25,14,55, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///	 0,23,80, 1,	/* command line window (bottom rows) */
/*TODO*///};
/*TODO*///
    public static class PAIR {
        //L = low 8 bits
        //H = high 8 bits
        //D = whole 16 bits

        public int H, L, D;

        public void SetH(int val) {
            H = val & 0xFF;
            D = ((H << 8) | L) & 0xFFFF;
        }

        public void SetL(int val) {
            L = val & 0xFF;
            D = ((H << 8) | L) & 0xFFFF;
        }

        public void SetD(int val) {
            D = val & 0xFFFF;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }

        public void AddH(int val) {
            H = (H + val) & 0xFF;
            D = ((H << 8) | L)&0xFFFF;
        }

        public void AddL(int val) {
            L = (L + val) & 0xFF;
            D = ((H << 8) | L)&0xFFFF;
        }

        public void AddD(int val) {
            D = (D + val) & 0xFFFF;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }
    };

    /**
     * **************************************************************************
     * The 6280 registers.
     * **************************************************************************
     */
    public static class h6280_Regs {

        PAIR ppc = new PAIR();
        /* previous program counter */
        PAIR pc = new PAIR();
        /* program counter */
        PAIR sp = new PAIR();
        /* stack pointer (always 100 - 1FF) */
        PAIR zp = new PAIR();
        /* zero page address */
        PAIR ea = new PAIR();
        /* effective address */
        int u8_a;
        /* Accumulator */
        int u8_x;
        /* X index register */
        int u8_y;
        /* Y index register */
        int u8_p;
        /* Processor status */
        int[] u8_mmr = new int[8];
        /* Hu6280 memory mapper registers */
        int u8_irq_mask;
        /* interrupt enable/disable */
        int u8_timer_status;
        /* timer status */
        int u8_timer_ack;
        /* timer acknowledge */
        int timer_value;
        /* timer interrupt */
        int timer_load;
        /* reload value */
        int extra_cycles;
        /* cycles used taking an interrupt */
        int nmi_state;
        int[] irq_state = new int[3];
        public irqcallbacksPtr irq_callback;
    }
    static h6280_Regs h6280 = new h6280_Regs();

    @Override
    public void reset(Object param) {
        int i;

        /* wipe out the h6280 structure */
        h6280 = new h6280_Regs();//memset(&h6280, 0, sizeof(h6280_Regs));

        /* set I and Z flags */
        h6280.u8_p = _fI | _fZ;

        /* stack starts at 0x01ff */
        h6280.sp.SetD(0x1ff);

        /* read the reset vector into PC */
        h6280.pc.SetL(RDMEM(H6280_RESET_VEC));
        h6280.pc.SetH(RDMEM((H6280_RESET_VEC + 1)));

        /* timer off by default */
        h6280.u8_timer_status = 0;
        h6280.u8_timer_ack = 1;

        /* clear pending interrupts */
        for (i = 0; i < 3; i++) {
            h6280.irq_state[i] = CLEAR_LINE;
        }
        if (h6280log != null) {
            fprintf(h6280log, "reset :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
        }
    }

    @Override
    public void exit() {
        /*NOTHING*/
    }

    @Override
    public int execute(int cycles) {
        int in, lastcycle, deltacycle;
        h6280_ICount[0] = cycles;

        /* Subtract cycles used for taking an interrupt */
        h6280_ICount[0] -= h6280.extra_cycles;
        h6280.extra_cycles = 0;
        lastcycle = h6280_ICount[0];

        /* Execute instructions */
        do {
            h6280.ppc.SetD(h6280.pc.D);

            /* Execute 1 instruction */
            in = RDOP();
            h6280.pc.AddD(1);
            insnh6280[in].handler();

            /* Check internal timer */
            if (h6280.u8_timer_status != 0) {
                deltacycle = lastcycle - h6280_ICount[0];
                h6280.timer_value -= deltacycle;
                if (h6280.timer_value <= 0 && h6280.u8_timer_ack == 1) {
                    h6280.u8_timer_ack = h6280.u8_timer_status = 0;
                    set_irq_line(2, ASSERT_LINE);
                }
            }
            lastcycle = h6280_ICount[0];

            /* If PC has not changed we are stuck in a tight loop, may as well finish */
            if (h6280.pc.D == h6280.ppc.D) {
                if (h6280_ICount[0] > 0) {
                    h6280_ICount[0] = 0;
                }
                h6280.extra_cycles = 0;
                return cycles;
            }

        } while (h6280_ICount[0] > 0);

        /* Subtract cycles used for taking an interrupt */
        h6280_ICount[0] -= h6280.extra_cycles;
        h6280.extra_cycles = 0;

        return cycles - h6280_ICount[0];
    }

    @Override
    public Object init_context() {
        Object reg = new h6280_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        h6280_Regs regs = new h6280_Regs();
        regs.ppc.SetD(h6280.ppc.D);
        regs.pc.SetD(h6280.pc.D);
        regs.sp.SetD(h6280.sp.D);
        regs.zp.SetD(h6280.zp.D);
        regs.ea.SetD(h6280.ea.D);
        regs.u8_a = h6280.u8_a;
        regs.u8_x = h6280.u8_x;
        regs.u8_y = h6280.u8_y;
        regs.u8_p = h6280.u8_p;
        regs.u8_mmr[0] = h6280.u8_mmr[0];
        regs.u8_mmr[1] = h6280.u8_mmr[1];
        regs.u8_mmr[2] = h6280.u8_mmr[2];
        regs.u8_mmr[3] = h6280.u8_mmr[3];
        regs.u8_mmr[4] = h6280.u8_mmr[4];
        regs.u8_mmr[5] = h6280.u8_mmr[5];
        regs.u8_mmr[6] = h6280.u8_mmr[6];
        regs.u8_mmr[7] = h6280.u8_mmr[7];
        regs.u8_irq_mask = h6280.u8_irq_mask;
        regs.u8_timer_status = h6280.u8_timer_status;
        regs.u8_timer_ack = h6280.u8_timer_ack;
        regs.timer_value = h6280.timer_value;
        regs.timer_load = h6280.timer_load;
        regs.extra_cycles = h6280.extra_cycles;
        regs.nmi_state = h6280.nmi_state;
        regs.irq_state[0] = h6280.irq_state[0];
        regs.irq_state[1] = h6280.irq_state[1];
        regs.irq_state[2] = h6280.irq_state[2];
        regs.irq_callback = h6280.irq_callback;
        return regs;
    }

    @Override
    public void set_context(Object reg) {
        h6280_Regs regs = (h6280_Regs) reg;
        h6280.ppc.SetD(regs.ppc.D);
        h6280.pc.SetD(regs.pc.D);
        h6280.sp.SetD(regs.sp.D);
        h6280.zp.SetD(regs.zp.D);
        h6280.ea.SetD(regs.ea.D);
        h6280.u8_a = regs.u8_a;
        h6280.u8_x = regs.u8_x;
        h6280.u8_y = regs.u8_y;
        h6280.u8_p = regs.u8_p;
        h6280.u8_mmr[0] = regs.u8_mmr[0];
        h6280.u8_mmr[1] = regs.u8_mmr[1];
        h6280.u8_mmr[2] = regs.u8_mmr[2];
        h6280.u8_mmr[3] = regs.u8_mmr[3];
        h6280.u8_mmr[4] = regs.u8_mmr[4];
        h6280.u8_mmr[5] = regs.u8_mmr[5];
        h6280.u8_mmr[6] = regs.u8_mmr[6];
        h6280.u8_mmr[7] = regs.u8_mmr[7];
        h6280.u8_irq_mask = regs.u8_irq_mask;
        h6280.u8_timer_status = regs.u8_timer_status;
        h6280.u8_timer_ack = regs.u8_timer_ack;
        h6280.timer_value = regs.timer_value;
        h6280.timer_load = regs.timer_load;
        h6280.extra_cycles = regs.extra_cycles;
        h6280.nmi_state = regs.nmi_state;
        h6280.irq_state[0] = regs.irq_state[0];
        h6280.irq_state[1] = regs.irq_state[1];
        h6280.irq_state[2] = regs.irq_state[2];
        h6280.irq_callback = regs.irq_callback;
    }

/*    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    @Override
    public int get_pc() {
        return h6280.pc.D & 0xFFFF;
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int state) {
        if (h6280.nmi_state == state) {
            return;
        }
        h6280.nmi_state = state;
        if (state != CLEAR_LINE) {
            DO_INTERRUPT(H6280_NMI_VEC);
        }
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        h6280.irq_state[irqline] = state;

        /* If line is cleared, just exit */
        if (state == CLEAR_LINE) {
            return;
        }

        /* Check if interrupts are enabled and the IRQ mask is clear */
        CHECK_IRQ_LINES();

    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        h6280.irq_callback = callback;
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
        /*TODO*///	static char buffer[32][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	h6280_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 32;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &h6280;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+H6280_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///        case CPU_INFO_REG+H6280_S: sprintf(buffer[which], "S:%02X", r->sp.b.l); break;
/*TODO*///        case CPU_INFO_REG+H6280_P: sprintf(buffer[which], "P:%02X", r->p); break;
/*TODO*///        case CPU_INFO_REG+H6280_A: sprintf(buffer[which], "A:%02X", r->a); break;
/*TODO*///		case CPU_INFO_REG+H6280_X: sprintf(buffer[which], "X:%02X", r->x); break;
/*TODO*///		case CPU_INFO_REG+H6280_Y: sprintf(buffer[which], "Y:%02X", r->y); break;
/*TODO*///		case CPU_INFO_REG+H6280_IRQ_MASK: sprintf(buffer[which], "IM:%02X", r->irq_mask); break;
/*TODO*///		case CPU_INFO_REG+H6280_TIMER_STATE: sprintf(buffer[which], "TMR:%02X", r->timer_status); break;
/*TODO*///		case CPU_INFO_REG+H6280_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+H6280_IRQ1_STATE: sprintf(buffer[which], "IRQ1:%X", r->irq_state[0]); break;
/*TODO*///		case CPU_INFO_REG+H6280_IRQ2_STATE: sprintf(buffer[which], "IRQ2:%X", r->irq_state[1]); break;
/*TODO*///		case CPU_INFO_REG+H6280_IRQT_STATE: sprintf(buffer[which], "IRQT:%X", r->irq_state[2]); break;
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///		case CPU_INFO_REG+H6280_M1: sprintf(buffer[which], "M1:%02X", r->mmr[0]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M2: sprintf(buffer[which], "M2:%02X", r->mmr[1]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M3: sprintf(buffer[which], "M3:%02X", r->mmr[2]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M4: sprintf(buffer[which], "M4:%02X", r->mmr[3]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M5: sprintf(buffer[which], "M5:%02X", r->mmr[4]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M6: sprintf(buffer[which], "M6:%02X", r->mmr[5]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M7: sprintf(buffer[which], "M7:%02X", r->mmr[6]); break;
/*TODO*///		case CPU_INFO_REG+H6280_M8: sprintf(buffer[which], "M8:%02X", r->mmr[7]); break;
/*TODO*///#endif
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->p & 0x80 ? 'N':'.',
/*TODO*///				r->p & 0x40 ? 'V':'.',
/*TODO*///				r->p & 0x20 ? 'R':'.',
/*TODO*///				r->p & 0x10 ? 'B':'.',
/*TODO*///				r->p & 0x08 ? 'D':'.',
/*TODO*///				r->p & 0x04 ? 'I':'.',
/*TODO*///				r->p & 0x02 ? 'Z':'.',
/*TODO*///				r->p & 0x01 ? 'C':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "HuC6280";
            case CPU_INFO_FAMILY:
                return "Hudsonsoft 6280";
            case CPU_INFO_VERSION:
                return "1.07";
            case CPU_INFO_FILE:
                return "h6280.java";
            case CPU_INFO_CREDITS:
                return "Copyright (c) 1999, 2000 Bryan McPhail, mish@tendril.co.uk";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)win_layout;
        }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
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
        cpu_setOPbase21.handler(pc);
    }

    /*TODO*///unsigned h6280_get_context (void *dst)
/*TODO*///{
/*TODO*///	if( dst )
/*TODO*///		*(h6280_Regs*)dst = h6280;
/*TODO*///	return sizeof(h6280_Regs);
/*TODO*///}
/*TODO*///
/*TODO*///void h6280_set_context (void *src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///		h6280 = *(h6280_Regs*)src;
/*TODO*///}
/*TODO*///
/*TODO*///void h6280_set_pc (unsigned val)
/*TODO*///{
/*TODO*///	PCW = val;
/*TODO*///}
/*TODO*///
/*TODO*///unsigned h6280_get_sp (void)
/*TODO*///{
/*TODO*///	return S;
/*TODO*///}
/*TODO*///
/*TODO*///void h6280_set_sp (unsigned val)
/*TODO*///{
/*TODO*///	S = val;
/*TODO*///}
/*TODO*///
/*TODO*///unsigned h6280_get_reg (int regnum)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case H6280_PC: return PCD;
/*TODO*///		case H6280_S: return S;
/*TODO*///		case H6280_P: return P;
/*TODO*///		case H6280_A: return A;
/*TODO*///		case H6280_X: return X;
/*TODO*///		case H6280_Y: return Y;
/*TODO*///		case H6280_IRQ_MASK: return h6280.irq_mask;
/*TODO*///		case H6280_TIMER_STATE: return h6280.timer_status;
/*TODO*///		case H6280_NMI_STATE: return h6280.nmi_state;
/*TODO*///		case H6280_IRQ1_STATE: return h6280.irq_state[0];
/*TODO*///		case H6280_IRQ2_STATE: return h6280.irq_state[1];
/*TODO*///		case H6280_IRQT_STATE: return h6280.irq_state[2];
/*TODO*///		case REG_PREVIOUSPC: return h6280.ppc.d;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///					return RDMEM( offset ) | ( RDMEM( offset+1 ) << 8 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void h6280_set_reg (int regnum, unsigned val)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case H6280_PC: PCW = val; break;
/*TODO*///		case H6280_S: S = val; break;
/*TODO*///		case H6280_P: P = val; break;
/*TODO*///		case H6280_A: A = val; break;
/*TODO*///		case H6280_X: X = val; break;
/*TODO*///		case H6280_Y: Y = val; break;
/*TODO*///		case H6280_IRQ_MASK: h6280.irq_mask = val; CHECK_IRQ_LINES; break;
/*TODO*///		case H6280_TIMER_STATE: h6280.timer_status = val; break;
/*TODO*///		case H6280_NMI_STATE: h6280_set_nmi_line( val ); break;
/*TODO*///		case H6280_IRQ1_STATE: h6280_set_irq_line( 0, val ); break;
/*TODO*///		case H6280_IRQ2_STATE: h6280_set_irq_line( 1, val ); break;
/*TODO*///		case H6280_IRQT_STATE: h6280_set_irq_line( 2, val ); break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///				{
/*TODO*///					WRMEM( offset, val & 0xff );
/*TODO*///					WRMEM( offset+1, (val >> 8) & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///    }
/*TODO*///}
    /**
     * **************************************************************************
     */
    public static ReadHandlerPtr H6280_irq_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int status;

            switch (offset) {
                case 0:
                    /* Read irq mask */
                    return h6280.u8_irq_mask;

                case 1:
                    /* Read irq status */
                    status = 0;
                    if (h6280.irq_state[1] != CLEAR_LINE) {
                        status |= 1;
                        /* IRQ 2 */
                    }
                    if (h6280.irq_state[0] != CLEAR_LINE) {
                        status |= 2;
                        /* IRQ 1 */
                    }
                    if (h6280.irq_state[2] != CLEAR_LINE) {
                        status |= 4;
                        /* TIMER */
                    }
                    return status;
            }

            return 0;
        }
    };
    public static WriteHandlerPtr H6280_irq_status_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    /* Write irq mask */
                    h6280.u8_irq_mask = data & 0x7;
                    CHECK_IRQ_LINES();
                    break;

                case 1:
                    /* Timer irq ack - timer is reloaded here */
                    h6280.timer_value = h6280.timer_load;
                    h6280.u8_timer_ack = 1;
                    /* Timer can't refire until ack'd */
                    break;
            }
        }
    };
    public static ReadHandlerPtr H6280_timer_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0:
                    /* Counter value */
                    return (h6280.timer_value / 1024) & 127;

                case 1:
                    /* Read counter status */
                    return h6280.u8_timer_status;
            }

            return 0;
        }
    };
    public static WriteHandlerPtr H6280_timer_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    /* Counter preload */
                    h6280.timer_load = h6280.timer_value = ((data & 127) + 1) * 1024;
                    return;

                case 1:
                    /* Counter enable */
                    if ((data & 1) != 0) {
                        /* stop -> start causes reload */
                        if (h6280.u8_timer_status == 0) {
                            h6280.timer_value = h6280.timer_load;
                        }
                    }
                    h6280.u8_timer_status = data & 1;
                    return;
            }
        }
    };

    /**
     * **************************************************************************
     */

    /* 6280 flags */
    public static final int _fC = 0x01;
    public static final int _fZ = 0x02;
    public static final int _fI = 0x04;
    public static final int _fD = 0x08;
    public static final int _fB = 0x10;
    public static final int _fT = 0x20;
    public static final int _fV = 0x40;
    public static final int _fN = 0x80;

    public static void SET_NZ(int n) {
        h6280.u8_p = ((h6280.u8_p & ~(_fN | _fT | _fZ))
                | (n & _fN)
                | ((n == 0) ? _fZ : 0)) & 0xFF;
    }

    public static void DO_INTERRUPT(int vector) {
        h6280.extra_cycles += 7;
        /* 7 cycles for an int */
        PUSH(h6280.pc.H);
        PUSH(h6280.pc.L);
        COMPOSE_P(0, _fB);
        PUSH(h6280.u8_p);
        h6280.u8_p = ((h6280.u8_p & ~_fD) | _fI) & 0xFF;
        /* knock out D and set I flag */
        h6280.pc.SetL(RDMEM(vector));
        h6280.pc.SetH(RDMEM((vector + 1)));
    }

    public static void CHECK_IRQ_LINES() {
        if ((h6280.u8_p & _fI) == 0) {
            if (h6280.irq_state[0] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x2) == 0) {
                DO_INTERRUPT(H6280_IRQ1_VEC);
                (h6280.irq_callback).handler(0);
            } else if (h6280.irq_state[1] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x1) == 0) {
                DO_INTERRUPT(H6280_IRQ2_VEC);
                (h6280.irq_callback).handler(1);
            } else if (h6280.irq_state[2] != CLEAR_LINE
                    && (h6280.u8_irq_mask & 0x4) == 0) {
                h6280.irq_state[2] = CLEAR_LINE;
                DO_INTERRUPT(H6280_TIMER_VEC);
            }
        }
    }

    /**
     * *************************************************************
     * RDMEM read memory
     * *************************************************************
     */
    public static int RDMEM(int addr) {
        return cpu_readmem21((h6280.u8_mmr[(addr) >>> 13] << 13) | ((addr) & 0x1fff));
    }

    /**
     * *************************************************************
     * WRMEM write memory
     * *************************************************************
     */
    public static void WRMEM(int addr, int data) {
        cpu_writemem21((h6280.u8_mmr[(addr) >>> 13] << 13) | ((addr) & 0x1fff), data & 0xFF);
    }

    /**
     * *************************************************************
     *
     * RDMEMZ read memory - zero page
     * *************************************************************
     */
    public static int RDMEMZ(int addr) {
        return cpu_readmem21((h6280.u8_mmr[1] << 13) | ((addr) & 0x1fff));
    }

    /**
     * *************************************************************
     * WRMEMZ write memory - zero page
     * *************************************************************
     */
    public static void WRMEMZ(int addr, int data) {
        cpu_writemem21((h6280.u8_mmr[1] << 13) | ((addr) & 0x1fff), data & 0xFF);
    }

    /**
     * *************************************************************
     * RDMEMW read word from memory
     * *************************************************************
     */
    public static int RDMEMW(int addr) {
        return cpu_readmem21((h6280.u8_mmr[(addr) >>> 13] << 13) | ((addr) & 0x1fff))
                | (cpu_readmem21((h6280.u8_mmr[(addr + 1) >>> 13] << 13) | ((addr + 1) & 0x1fff)) << 8);
    }

    /**
     * *************************************************************
     * RDZPWORD read a word from a zero page address
     * *************************************************************
     */
    public static int RDZPWORD(int addr) {
        if ((addr & 0xff) == 0xff) {
            return cpu_readmem21((h6280.u8_mmr[1] << 13) | ((addr) & 0x1fff))
                    + (cpu_readmem21((h6280.u8_mmr[1] << 13) | ((addr - 0xff) & 0x1fff)) << 8);
        } else {
            return cpu_readmem21((h6280.u8_mmr[1] << 13) | ((addr) & 0x1fff))
                    + (cpu_readmem21((h6280.u8_mmr[1] << 13) | ((addr + 1) & 0x1fff)) << 8);
        }
    }

    /**
     * *************************************************************
     * push a register onto the stack
     * *************************************************************
     */
    public static void PUSH(int Rg) {
        cpu_writemem21((h6280.u8_mmr[1] << 13) | h6280.sp.D, Rg);
        h6280.sp.AddL(-1);//S--
    }

    /**
     * *************************************************************
     * RDOP read an opcode
     * *************************************************************
     */
    public static int RDOP() {
        return cpu_readop((h6280.u8_mmr[h6280.pc.D >>> 13] << 13) | (h6280.pc.D & 0x1fff));
    }

    /**
     * *************************************************************
     *
     * RDOPARG read an opcode argument
     * *************************************************************
     */
    public static int RDOPARG() {
        return cpu_readop_arg((h6280.u8_mmr[h6280.pc.D >>> 13] << 13) | (h6280.pc.D & 0x1fff));
    }

    /**
     * *************************************************************
     * BRA branch relative
     * *************************************************************
     */
    public static void BRA(boolean cond, int tmp) {
        if (cond) {
            h6280_ICount[0] -= 4;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            h6280.ea.SetD(h6280.pc.D + (byte) tmp);
            h6280.pc.SetD(h6280.ea.D);
        } else {
            h6280.pc.AddD(1);
            h6280_ICount[0] -= 2;
        }
    }

    /**
     * *************************************************************
     * EA = zero page address
     * *************************************************************
     */
    public static void EA_ZPG() {
        h6280.zp.SetL(RDOPARG());
        h6280.pc.AddD(1);
        h6280.ea.SetD(h6280.zp.D);
    }

    /**
     * *************************************************************
     * EA = zero page address + X
     * *************************************************************
     */
    public static void EA_ZPX() {
        h6280.zp.SetL(RDOPARG() + h6280.u8_x);
        h6280.pc.AddD(1);
        h6280.ea.SetD(h6280.zp.D);
    }

    /**
     * *************************************************************
     * EA = zero page address + Y
     * *************************************************************
     */
    public static void EA_ZPY() {
        h6280.zp.SetL(RDOPARG() + h6280.u8_y);
        h6280.pc.AddD(1);
        h6280.ea.SetD(h6280.zp.D);
    }

    /**
     * *************************************************************
     * EA = absolute address
     * *************************************************************
     */
    public static void EA_ABS() {
        h6280.ea.SetL(RDOPARG());
        h6280.pc.AddD(1);
        h6280.ea.SetH(RDOPARG());
        h6280.pc.AddD(1);
    }

    /**
     * *************************************************************
     * EA = absolute address + X
     * *************************************************************
     */
    public static void EA_ABX() {
        EA_ABS();
        h6280.ea.SetD(h6280.ea.D + h6280.u8_x);
    }

    /**
     * *************************************************************
     * EA = absolute address + Y
     * *************************************************************
     */
    public static void EA_ABY() {
        EA_ABS();
        h6280.ea.SetD(h6280.ea.D + h6280.u8_y);
    }

    /**
     * *************************************************************
     * EA = zero page indirect (65c02 pre indexed w/o X)
     * *************************************************************
     */
    public static void EA_ZPI() {
        h6280.zp.SetL(RDOPARG());
        h6280.pc.AddD(1);
        h6280.ea.SetD(RDZPWORD(h6280.zp.D));
    }

    /**
     * *************************************************************
     * EA = zero page + X indirect (pre indexed)
     * *************************************************************
     */
    public static void EA_IDX() {
        h6280.zp.SetL(RDOPARG() + h6280.u8_x);
        h6280.pc.AddD(1);
        h6280.ea.SetD(RDZPWORD(h6280.zp.D));
    }

    /**
     * *************************************************************
     * EA = zero page indirect + Y (post indexed)
     * *************************************************************
     */
    public static void EA_IDY() {
        h6280.zp.SetL(RDOPARG());
        h6280.pc.AddD(1);
        h6280.ea.SetD(RDZPWORD(h6280.zp.D));
        h6280.ea.SetD(h6280.ea.D + h6280.u8_y);
    }

    /**
     * *************************************************************
     * EA = indirect (only used by JMP)
     * *************************************************************
     */
    public static void EA_IND(int tmp) {
        EA_ABS();
        tmp = RDMEM(h6280.ea.D);
        h6280.ea.AddD(1);//EAD++;
        h6280.ea.SetH(RDMEM(h6280.ea.D));
        h6280.ea.SetL(tmp);
    }

    /**
     * *************************************************************
     * EA = indirect plus x (only used by JMP)
     * *************************************************************
     */
    public static void EA_IAX() {
        EA_ABS();
        h6280.ea.SetD(h6280.ea.D + h6280.u8_x);//EAD += X;
        int tmp = RDMEM(h6280.ea.D);
        h6280.ea.AddD(1);//EAD++;
        h6280.ea.SetH(RDMEM(h6280.ea.D));
        h6280.ea.SetL(tmp);
    }

    /* write a value from tmp */
    public static void WR_ZPG(int tmp) {
        EA_ZPG();
        WRMEMZ(h6280.ea.D, tmp);
    }

    public static void WR_ZPX(int tmp) {
        EA_ZPX();
        WRMEMZ(h6280.ea.D, tmp);
    }

    public static void WR_ZPY(int tmp) {
        EA_ZPY();
        WRMEMZ(h6280.ea.D, tmp);
    }

    public static void WR_ABS(int tmp) {
        EA_ABS();
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WR_ABX(int tmp) {
        EA_ABX();
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WR_ABY(int tmp) {
        EA_ABY();
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WR_ZPI(int tmp) {
        EA_ZPI();
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WR_IDX(int tmp) {
        EA_IDX();
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WR_IDY(int tmp) {
        EA_IDY();
        WRMEM(h6280.ea.D, tmp);
    }

    /* write back a value from tmp to the last EA */
    public static void WB_ACC(int tmp) {
        h6280.u8_a = tmp & 0xFF;
    }

    public static void WB_EA(int tmp) {
        WRMEM(h6280.ea.D, tmp);
    }

    public static void WB_EAZ(int tmp) {
        WRMEMZ(h6280.ea.D, tmp);
    }

    /**
     * *************************************************************
     * compose the real flag register by including N and Z and set any SET and
     * clear any CLR bits also
     */
    public static void COMPOSE_P(int SET, int CLR) {
        h6280.u8_p = ((h6280.u8_p & ~CLR) | SET) & 0xFF;
    }

    /* 6280 ********************************************************
 *	ADC Add with carry
 ***************************************************************/
    public static void ADC(int tmp) {
        if ((h6280.u8_p & _fD) != 0) {
            int c = (h6280.u8_p & _fC);
            int lo = (h6280.u8_a & 0x0f) + (tmp & 0x0f) + c;
            int hi = (h6280.u8_a & 0xf0) + (tmp & 0xf0);
            h6280.u8_p = (h6280.u8_p & ~(_fV | _fC)) & 0xFF;
            if (lo > 0x09) {
                hi += 0x10;
                lo += 0x06;
            }
            if ((~(h6280.u8_a ^ tmp) & (h6280.u8_a ^ hi) & _fN) != 0) {
                h6280.u8_p = (h6280.u8_p | _fV) & 0xFF;
            }
            if (hi > 0x90) {
                hi += 0x60;
            }
            if ((hi & 0xff00) != 0) {
                h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
            }
            h6280.u8_a = (lo & 0x0f) + (hi & 0xf0);
        } else {
            int c = (h6280.u8_p & _fC);
            int sum = h6280.u8_a + tmp + c;
            h6280.u8_p = (h6280.u8_p & ~(_fV | _fC)) & 0xFF;
            if ((~(h6280.u8_a ^ tmp) & (h6280.u8_a ^ sum) & _fN) != 0) {
                h6280.u8_p = (h6280.u8_p | _fV) & 0xFF;
            }
            if ((sum & 0xff00) != 0) {
                h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
            }
            h6280.u8_a = sum & 0xFF;
        }
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
     *	AND Logical and
     ***************************************************************/
    public static void AND(int tmp) {
        h6280.u8_a = (h6280.u8_a & tmp) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
     *  BBR Branch if bit is reset
     ***************************************************************/
    public static void BBR(int bit, int tmp) {
        BRA((tmp & (1 << bit)) == 0, tmp);
    }

    /* 6280 ********************************************************
    *  BBS Branch if bit is set
    ***************************************************************/
    public static void BBS(int tmp, int bit) {
        BRA((tmp & (1 << bit)) != 0, tmp);
    }

    /* 6280 ********************************************************
    *	BCC Branch if carry clear
    ***************************************************************/
    public static void BCC(int tmp) {
        BRA((h6280.u8_p & _fC) == 0, tmp);
    }

    /* 6280 ********************************************************
    *	BCS Branch if carry set
    ***************************************************************/
    public static void BCS(int tmp) {
        BRA((h6280.u8_p & _fC) != 0, tmp);
    }

    /* 6280 ********************************************************
     *	BEQ Branch if equal
     ***************************************************************/
    public static void BEQ(int tmp) {
        BRA((h6280.u8_p & _fZ) != 0, tmp);
    }

    /* 6280 ********************************************************
     *	BIT Bit test
     ***************************************************************/
    public static void BIT(int tmp) {
        h6280.u8_p = ((h6280.u8_p & ~(_fN | _fV | _fT | _fZ))
                | ((tmp & 0x80) != 0 ? _fN : 0)
                | ((tmp & 0x40) != 0 ? _fV : 0)
                | ((tmp & h6280.u8_a) != 0 ? 0 : _fZ)) & 0xff;
    }

    /* 6280 ********************************************************
     *	BMI Branch if minus
     ***************************************************************/
    public static void BMI(int tmp) {
        BRA((h6280.u8_p & _fN) != 0, tmp);
    }

    /* 6280 ********************************************************
     *	BNE Branch if not equal
     ***************************************************************/
    public static void BNE(int tmp) {
        BRA((h6280.u8_p & _fZ) == 0, tmp);
    }

    /* 6280 ********************************************************
    *	BPL Branch if plus
    ***************************************************************/
    public static void BPL(int tmp) {
        BRA((h6280.u8_p & _fN) == 0, tmp);
    }


    /*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	BRK Break
/*TODO*/// *	increment PC, push PC hi, PC lo, flags (with B bit set),
/*TODO*/// *	set I flag, reset D flag and jump via IRQ vector
/*TODO*/// ***************************************************************/
/*TODO*///#define BRK 													\
/*TODO*///	logerror("BRK %04x\n",cpu_get_pc());	\
/*TODO*///	h6280.pc.AddD(1);														\
/*TODO*///	PUSH(PCH);													\
/*TODO*///	PUSH(PCL);													\
/*TODO*///	PUSH(P | _fB);												\
/*TODO*///	P = (P & ~_fD) | _fI;										\
/*TODO*///	PCL = RDMEM(H6280_IRQ2_VEC); 								\
/*TODO*///	PCH = RDMEM(H6280_IRQ2_VEC+1)
/*TODO*///
    /* 6280 ********************************************************
     *	BSR Branch to subroutine
     ***************************************************************/
    public static void BSR(int tmp) {
        PUSH(h6280.pc.H);
        PUSH(h6280.pc.L);
        h6280_ICount[0] -= 4;
        /* 4 cycles here, 4 in BRA */
        BRA(true, tmp);
    }

    /* 6280 ********************************************************
    *	BVC Branch if overflow clear
    ***************************************************************/
    public static void BVC(int tmp) {
        BRA((h6280.u8_p & _fV) == 0, tmp);
    }

    /* 6280 ********************************************************
    *	BVS Branch if overflow set
    ***************************************************************/
    public static void BVS(int tmp) {
        BRA((h6280.u8_p & _fV) != 0, tmp);
    }

    /* 6280 ********************************************************
     *  CLA Clear accumulator
     ***************************************************************/
    public static void CLA() {
        h6280.u8_a = 0;
    }

    /* 6280 ********************************************************
    *	CLC Clear carry flag
    ***************************************************************/
    public static void CLC() {
        h6280.u8_p = (h6280.u8_p & ~_fC) & 0xFF;
    }

    /* 6280 ********************************************************
    *	CLD Clear decimal flag
    ***************************************************************/
    public static void CLD() {
        h6280.u8_p = (h6280.u8_p & ~_fD) & 0xFF;

    }

    /* 6280 ********************************************************
    *	CLI Clear interrupt flag
    ***************************************************************/
    public static void CLI() {
        if ((h6280.u8_p & _fI) != 0) {
            h6280.u8_p = (h6280.u8_p & ~_fI) & 0xFF;
            CHECK_IRQ_LINES();
        }
    }

    /* 6280 ********************************************************
    *	CLV Clear overflow flag
    ***************************************************************/
    public static void CLV() {
        h6280.u8_p = (h6280.u8_p & ~_fV) & 0xFF;
    }

    /* 6280 ********************************************************
    *  CLX Clear index X
    ***************************************************************/
    public static void CLX() {
        h6280.u8_x = 0;
    }

    /* 6280 ********************************************************
     *  CLY Clear index Y
     ***************************************************************/
    public static void CLY() {
        h6280.u8_y = 0;
    }

    /* 6280 ********************************************************
    *	CMP Compare accumulator
    ***************************************************************/
    public static void CMP(int tmp) {
        h6280.u8_p = (h6280.u8_p & ~_fC) & 0xFF;
        if (h6280.u8_a >= tmp) {
            h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
        }
        SET_NZ((h6280.u8_a - tmp) & 0xFF);
    }

    /* 6280 ********************************************************
    *	CPX Compare index X
    ***************************************************************/
    public static void CPX(int tmp) {
        h6280.u8_p = (h6280.u8_p & ~_fC) & 0xFF;
        if (h6280.u8_x >= tmp) {
            h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
        }
        SET_NZ((h6280.u8_x - tmp) & 0xFF);
    }

    /* 6280 ********************************************************
     *	CPY Compare index Y
     ***************************************************************/
    public static void CPY(int tmp) {
        h6280.u8_p = (h6280.u8_p & ~_fC) & 0xFF;
        if (h6280.u8_y >= tmp) {
            h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
        }
        SET_NZ((h6280.u8_y - tmp) & 0xFF);
    }

    /* 6280 ********************************************************
     *  DEA Decrement accumulator
     ***************************************************************/
    public static void DEA() {
        h6280.u8_a = (h6280.u8_a - 1) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
 *	DEX Decrement index X
 ***************************************************************/
    public static void DEX() {
        h6280.u8_x = (h6280.u8_x - 1) & 0xFF;
        SET_NZ(h6280.u8_x);
    }

    /* 6280 ********************************************************
    *	DEY Decrement index Y
    ***************************************************************/
    public static void DEY() {
        h6280.u8_y = (h6280.u8_y - 1) & 0xFF;
        SET_NZ(h6280.u8_y);
    }

    /* 6280 ********************************************************
     *	EOR Logical exclusive or
     ***************************************************************/
    public static void EOR(int tmp) {
        h6280.u8_a = (h6280.u8_a ^ tmp) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
 *	ILL Illegal opcode
 ***************************************************************/
    public static void ILL() {
        h6280_ICount[0] -= 2;
        /* (assumed) */
//        logerror("%04x: WARNING - h6280 illegal opcode\n", cpu_get_pc());
    }

    /* 6280 ********************************************************
    *  INA Increment accumulator
    ***************************************************************/
    public static void INA() {
        h6280.u8_a = (h6280.u8_a + 1) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
    *	INX Increment index X
    ***************************************************************/
    public static void INX() {
        h6280.u8_x = (h6280.u8_x + 1) & 0xFF;
        SET_NZ(h6280.u8_x);
    }

    /* 6280 ********************************************************
     *	INY Increment index Y
     ***************************************************************/
    public static void INY() {
        h6280.u8_y = (h6280.u8_y + 1) & 0xFF;
        SET_NZ(h6280.u8_y);
    }

    /* 6280 ********************************************************
     *	JMP Jump to address
     *	set PC to the effective address
     ***************************************************************/
    public static void JMP() {
        h6280.pc.SetD(h6280.ea.D);
    }

    /* 6280 ********************************************************
     *	JSR Jump to subroutine
     *	decrement PC (sic!) push PC hi, push PC lo and set
     *	PC to the effective address
     ***************************************************************/
    public static void JSR() {
        h6280.pc.AddD(-1);//PCW--;														
        PUSH(h6280.pc.H);
        PUSH(h6280.pc.L);
        h6280.pc.SetD(h6280.ea.D);
    }

    /* 6280 ********************************************************
    *	LDA Load accumulator
    ***************************************************************/
    public static void LDA(int tmp) {
        h6280.u8_a = tmp & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
    *	LDX Load index X
    ***************************************************************/
    public static void LDX(int tmp) {
        h6280.u8_x = tmp & 0xFF;
        SET_NZ(h6280.u8_x);
    }

    /* 6280 ********************************************************
    *	LDY Load index Y
    ***************************************************************/
    public static void LDY(int tmp) {
        h6280.u8_y = tmp & 0xFF;
        SET_NZ(h6280.u8_y);
    }

    /* 6280 ********************************************************
     *	NOP No operation
     ***************************************************************/
    public static void NOP() {

    }

    /* 6280 ********************************************************
    *	ORA Logical inclusive or
    ***************************************************************/
    public static void ORA(int tmp) {
        h6280.u8_a = (h6280.u8_a | tmp) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
    *	PHA Push accumulator
    ***************************************************************/
    public static void PHA() {
        PUSH(h6280.u8_a);
    }

    /* 6280 ********************************************************
    *	PHP Push processor status (flags)
    ***************************************************************/
    public static void PHP() {
        COMPOSE_P(0, 0);
        PUSH(h6280.u8_p);
    }

    /* 6280 ********************************************************
     *  PHX Push index X
     ***************************************************************/
    public static void PHX() {
        PUSH(h6280.u8_x);
    }

    /* 6280 ********************************************************
    *  PHY Push index Y
    ***************************************************************/
    public static void PHY() {
        PUSH(h6280.u8_y);
    }

    /* 6280 ********************************************************
    *	PLA Pull accumulator
    ***************************************************************/
    public static void PLA() {
        //PULL(A);
        h6280.sp.AddL(1);
        h6280.u8_a = cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D) & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    /* 6280 ********************************************************
    *	PLP Pull processor status (flags)
    ***************************************************************/
    public static void PLP() {
        //PULL(P);
        h6280.sp.AddL(1);
        h6280.u8_p = cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D) & 0xFF;
        CHECK_IRQ_LINES();
    }

    /* 6280 ********************************************************
     *  PLX Pull index X
     ***************************************************************/
    public static void PLX() {
        h6280.sp.AddL(1);
        h6280.u8_x = cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D) & 0xFF;
    }

    /* 6280 ********************************************************
     *  PLY Pull index Y
     ***************************************************************/
    public static void PLY() {
        h6280.sp.AddL(1);
        h6280.u8_y = cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D) & 0xFF;
    }

    /* 6280 ********************************************************
     *	RTI Return from interrupt
     *	pull flags, pull PC lo, pull PC hi and increment PC
     ***************************************************************/
    public static void RTI() {
        //PULL(P);
        h6280.sp.AddL(1);
        h6280.u8_p = cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D) & 0xFF;
        //PULL(PCL);
        h6280.sp.AddL(1);
        h6280.pc.SetL(cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D));
        //PULL(PCH);
        h6280.sp.AddL(1);
        h6280.pc.SetH(cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D));
        CHECK_IRQ_LINES();
    }

    /* 6280 ********************************************************
     *	RTS Return from subroutine
     *	pull PC lo, PC hi and increment PC
     ***************************************************************/
    public static void RTS() {
        //PULL(PCL);
        h6280.sp.AddL(1);
        h6280.pc.SetL(cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D));
        //PULL(PCH);
        h6280.sp.AddL(1);
        h6280.pc.SetH(cpu_readmem21((h6280.u8_mmr[1] << 13) | h6280.sp.D));
        h6280.pc.AddD(1);
    }

    /* 6280 ********************************************************
    *  SAX Swap accumulator and index X
    ***************************************************************/
    public static void SAX() {
        int tmp = h6280.u8_x;
        h6280.u8_x = h6280.u8_a & 0xFF;
        h6280.u8_a = tmp & 0xFF;
    }

    /*TODO*////* 6280 ********************************************************
/*TODO*/// *  SAY Swap accumulator and index Y
/*TODO*/// ***************************************************************/
/*TODO*///#define SAY                                                     
/*TODO*///    tmp = Y;                                                    
/*TODO*///    Y = A;                                                      
/*TODO*///    A = tmp
/*TODO*///
/* 6280 ********************************************************
 *	SBC Subtract with carry
 ***************************************************************/
    public static void SBC(int tmp) {
        if ((h6280.u8_p & _fD) != 0) {
            int c = (h6280.u8_p & _fC) ^ _fC;
            int sum = h6280.u8_a - tmp - c;
            int lo = (h6280.u8_a & 0x0f) - (tmp & 0x0f) - c;
            int hi = (h6280.u8_a & 0xf0) - (tmp & 0xf0);
            h6280.u8_p = (h6280.u8_p & ~(_fV | _fC)) & 0xFF;
            if (((h6280.u8_a ^ tmp) & (h6280.u8_a ^ sum) & _fN) != 0) {
                h6280.u8_p = (h6280.u8_p | _fV) & 0xFF;
            }
            if ((lo & 0xf0) != 0) {
                lo -= 6;
            }
            if ((lo & 0x80) != 0) {
                hi -= 0x10;
            }
            if ((hi & 0x0f00) != 0) {
                hi -= 0x60;
            }
            if ((sum & 0xff00) == 0) {
                h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
            }
            h6280.u8_a = (lo & 0x0f) + (hi & 0xf0);
        } else {
            int c = (h6280.u8_p & _fC) ^ _fC;
            int sum = h6280.u8_a - tmp - c;
            h6280.u8_p = (h6280.u8_p & ~(_fV | _fC)) & 0xFF;
            if (((h6280.u8_a ^ tmp) & (h6280.u8_a ^ sum) & _fN) != 0) {
                h6280.u8_p = (h6280.u8_p | _fV) & 0xFF;
            }
            if ((sum & 0xff00) == 0) {
                h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
            }
            h6280.u8_a = sum & 0xFF;
        }
        SET_NZ(h6280.u8_a);

    }

    /* 6280 ********************************************************
     *	SEC Set carry flag
     ***************************************************************/
    public static void SEC() {
        h6280.u8_p = (h6280.u8_p | _fC) & 0xFF;
    }

    /* 6280 ********************************************************
    *	SED Set decimal flag
    ***************************************************************/
    public static void SED() {
        h6280.u8_p = (h6280.u8_p | _fD) & 0xFF;
    }

    /* 6280 ********************************************************
    *	SEI Set interrupt flag
    ***************************************************************/
    public static void SEI() {
        h6280.u8_p = (h6280.u8_p | _fI) & 0xFF;
    }

    /* 6280 ********************************************************
    *	SET Set t flag
    ***************************************************************/
    public static void SET() {
        h6280.u8_p = (h6280.u8_p | _fT) & 0xFF;
//        logerror("%04x: WARNING H6280 SET\n", cpu_get_pc());
    }

    /* 6280 ********************************************************
    *  ST0 Store at hardware address 0
    ***************************************************************/
    public static void ST0(int tmp) {
        cpu_writeport(0x0000, tmp);
    }

    /* 6280 ********************************************************
    *  ST1 Store at hardware address 2
    ***************************************************************/
    public static void ST1(int tmp) {
        cpu_writeport(0x0002, tmp);
    }

    /* 6280 ********************************************************
    *  ST2 Store at hardware address 3
    ***************************************************************/
    public static void ST2(int tmp) {
        cpu_writeport(0x0003, tmp);
    }

    /* H6280 *******************************************************
     *  SXY Swap index X and index Y
     ***************************************************************/
    public static void SXY() {
        int tmp = h6280.u8_x & 0xFF;
        h6280.u8_x = h6280.u8_y & 0xFF;
        h6280.u8_y = tmp;
    }

    /* H6280 *******************************************************
     *  TAI
     ***************************************************************/
    public static void TAI() {
        int from = RDMEMW(h6280.pc.D);
        int to = RDMEMW(h6280.pc.D + 2);
        int length = RDMEMW(h6280.pc.D + 4);
        h6280.pc.SetD(h6280.pc.D + 6);//PCW+=6; 													
        int alternate = 0;
        while ((length--) != 0) {
            WRMEM(to, RDMEM(from + alternate));
            to++;
            alternate ^= 1;
        }
        h6280_ICount[0] -= (6 * length) + 17;
    }

    /* H6280 *******************************************************
     *  TAM Transfer accumulator to memory mapper register(s)
     ***************************************************************/
    public static void TAM(int tmp) {
        if ((tmp & 0x01) != 0) {
            h6280.u8_mmr[0] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x02) != 0) {
            h6280.u8_mmr[1] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x04) != 0) {
            h6280.u8_mmr[2] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x08) != 0) {
            h6280.u8_mmr[3] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x10) != 0) {
            h6280.u8_mmr[4] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x20) != 0) {
            h6280.u8_mmr[5] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x40) != 0) {
            h6280.u8_mmr[6] = h6280.u8_a & 0xFF;
        }
        if ((tmp & 0x80) != 0) {
            h6280.u8_mmr[7] = h6280.u8_a & 0xFF;
        }
    }

    /* 6280 ********************************************************
    *	TAX Transfer accumulator to index X
    ***************************************************************/
    public static void TAX() {
        h6280.u8_x = h6280.u8_a & 0xFF;
        SET_NZ(h6280.u8_x);
    }

    /* 6280 ********************************************************
     *	TAY Transfer accumulator to index Y
     ***************************************************************/
    public static void TAY() {
        h6280.u8_y = h6280.u8_a & 0xFF;//Y = A;														
        SET_NZ(h6280.u8_y);
    }

    /*TODO*////* 6280 ********************************************************
/*TODO*/// *  TDD
/*TODO*/// ***************************************************************/
/*TODO*///#define TDD 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from)); 									
/*TODO*///		to--; 													
/*TODO*///		from--;													
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *  TIA
/*TODO*/// ***************************************************************/
/*TODO*///#define TIA 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	alternate=0; 												
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to+alternate,RDMEM(from));						
/*TODO*///		from++; 												
/*TODO*///		alternate ^= 1; 										
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
    /* 6280 ********************************************************
     *  TII
     ***************************************************************/
    public static void TII(int from, int to, int length) {
        from = RDMEMW(h6280.pc.D);
        to = RDMEMW(h6280.pc.D + 2);
        length = RDMEMW(h6280.pc.D + 4);
        h6280.pc.SetD(h6280.pc.D + 6);//PCW+=6; 													
        while ((length--) != 0) {
            WRMEM(to, RDMEM(from));
            to++;
            from++;
        }
        h6280_ICount[0] -= (6 * length) + 17;
    }

    /*TODO*////* 6280 ********************************************************
/*TODO*/// *  TIN Transfer block, source increments every loop
/*TODO*/// ***************************************************************/
/*TODO*///#define TIN 													
/*TODO*///	from=RDMEMW(PCW);											
/*TODO*///	to  =RDMEMW(PCW+2);											
/*TODO*///	length=RDMEMW(PCW+4);										
/*TODO*///	PCW+=6; 													
/*TODO*///	while ((length--) != 0) { 									
/*TODO*///		WRMEM(to,RDMEM(from)); 									
/*TODO*///		from++;													
/*TODO*///	}		 													
/*TODO*///	h6280_ICount-=(6 * length) + 17;
/*TODO*///
/* 6280 ********************************************************
 *  TMA Transfer memory mapper register(s) to accumulator
 *  the highest bit set in tmp is the one that counts
 ***************************************************************/
    public static void TMA(int tmp) {
        if ((tmp & 0x01) != 0) {
            h6280.u8_a = h6280.u8_mmr[0] & 0xFF;
        }
        if ((tmp & 0x02) != 0) {
            h6280.u8_a = h6280.u8_mmr[1] & 0xFF;
        }
        if ((tmp & 0x04) != 0) {
            h6280.u8_a = h6280.u8_mmr[2] & 0xFF;
        }
        if ((tmp & 0x08) != 0) {
            h6280.u8_a = h6280.u8_mmr[3] & 0xFF;
        }
        if ((tmp & 0x10) != 0) {
            h6280.u8_a = h6280.u8_mmr[4] & 0xFF;
        }
        if ((tmp & 0x20) != 0) {
            h6280.u8_a = h6280.u8_mmr[5] & 0xFF;
        }
        if ((tmp & 0x40) != 0) {
            h6280.u8_a = h6280.u8_mmr[6] & 0xFF;
        }
        if ((tmp & 0x80) != 0) {
            h6280.u8_a = h6280.u8_mmr[7] & 0xFF;
        }
    }

    /*TODO*////* 6280 ********************************************************
/*TODO*/// * TRB  Test and reset bits
/*TODO*/// ***************************************************************/
/*TODO*///#define TRB                                                   	
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp&0x80) ? _fN:0)									
/*TODO*///		| ((tmp&0x40) ? _fV:0)									
/*TODO*///		| ((tmp&A)  ? 0:_fZ);									
/*TODO*///    tmp &= ~A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// * TSB  Test and set bits
/*TODO*/// ***************************************************************/
/*TODO*///#define TSB                                                     
/*TODO*///	P = (P & ~(_fN|_fV|_fT|_fZ))								
/*TODO*///		| ((tmp&0x80) ? _fN:0)									
/*TODO*///		| ((tmp&0x40) ? _fV:0)									
/*TODO*///		| ((tmp&A)  ? 0:_fZ);									
/*TODO*///    tmp |= A
/*TODO*///
/*TODO*////* 6280 ********************************************************
/*TODO*/// *	TSX Transfer stack LSB to index X
/*TODO*/// ***************************************************************/
/*TODO*///#define TSX 													
/*TODO*///	X = S;														
/*TODO*///	SET_NZ(X)
/*TODO*///
    /* 6280 ********************************************************
     *	TST
     ***************************************************************/
    public static void TST(int tmp, int tmp2) {
        h6280.u8_p = ((h6280.u8_p & ~(_fN | _fV | _fT | _fZ))
                | ((tmp2 & 0x80) != 0 ? _fN : 0)
                | ((tmp2 & 0x40) != 0 ? _fV : 0)
                | ((tmp2 & tmp) != 0 ? 0 : _fZ)) & 0xFF;
    }

    /* 6280 ********************************************************
     *	TXA Transfer index X to accumulator
     ***************************************************************/
    public static void TXA() {
        h6280.u8_a = h6280.u8_x & 0xFF;
        SET_NZ(h6280.u8_a);

    }

    /* 6280 ********************************************************
     *	TXS Transfer index X to stack LSB
     *	no flags changed (sic!)
     ***************************************************************/
    public static void TXS() {
        h6280.sp.SetL(h6280.u8_x);
    }

    /* 6280 ********************************************************
    *	TYA Transfer index Y to accumulator
    ***************************************************************/
    public static void TYA() {
        h6280.u8_a = h6280.u8_y & 0xFF;
        SET_NZ(h6280.u8_a);
    }

    public abstract interface opcode {

        public abstract void handler();
    }
    static opcode h6280_000 = new opcode() {
        public void handler() {
            /*TODO*///            h6280_ICount[0] -= 8;BRK;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 8 BRK
    static opcode h6280_020 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 7;
            EA_ABS();
            JSR();
            if (h6280log != null) {
                fprintf(h6280log, "020 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 7 JSR  ABS
    static opcode h6280_040 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 7;
            RTI();
            if (h6280log != null) {
                fprintf(h6280log, "040 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 7 RTI
    static opcode h6280_060 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 7;
            RTS();
            if (h6280log != null) {
                fprintf(h6280log, "060 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 7 RTS
    static opcode h6280_080 = new opcode() {
        public void handler() {
            int tmp = 0;
            BRA(true, tmp);
            if (h6280log != null) {
                fprintf(h6280log, "080 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 4 BRA  REL
    static opcode h6280_0a0 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            //RD_IMM
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            LDY(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0a0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 LDY  IMM
    static opcode h6280_0c0 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            CPY(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0c0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 CPY  IMM
    static opcode h6280_0e0 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            CPX(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0e0 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 CPX  IMM

    static opcode h6280_010 = new opcode() {
        public void handler() {
            int tmp = 0;
            BPL(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "010 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2/4 BPL  REL
    static opcode h6280_030 = new opcode() {
        public void handler() {
            int tmp = 0;
            BMI(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "030 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2/4 BMI  REL
    static opcode h6280_050 = new opcode() {
        public void handler() {
            int tmp = 0;
            BVC(tmp);
        }
    }; // 2/4 BVC  REL
    static opcode h6280_070 = new opcode() {
        public void handler() {
            int tmp = 0;
            BVS(tmp);
        }
    }; // 2/4 BVS  REL
    static opcode h6280_090 = new opcode() {
        public void handler() {
            int tmp = 0;
            BCC(tmp);
        }
    }; // 2/4 BCC  REL
    static opcode h6280_0b0 = new opcode() {
        public void handler() {
            int tmp = 0;
            BCS(tmp);
        }
    }; // 2/4 BCS  REL
    static opcode h6280_0d0 = new opcode() {
        public void handler() {
            int tmp = 0;
            BNE(tmp);
        }
    }; // 2/4 BNE  REL
    static opcode h6280_0f0 = new opcode() {
        public void handler() {
            int tmp = 0;
            BEQ(tmp);
        }
    }; // 2/4 BEQ  REL

    static opcode h6280_001 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 7 ORA  IDX
    static opcode h6280_021 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 7 AND  IDX
    static opcode h6280_041 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 7 EOR  IDX
    static opcode h6280_061 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 7 ADC  IDX
    static opcode h6280_081 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            tmp = h6280.u8_a;
            WR_IDX(tmp);
        }
    }; // 7 STA  IDX
    static opcode h6280_0a1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
        }
    }; // 7 LDA  IDX
    static opcode h6280_0c1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 7 CMP  IDX
    static opcode h6280_0e1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDX();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 7 SBC  IDX

    static opcode h6280_011 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 7 ORA  IDY
    static opcode h6280_031 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 7 AND  IDY
    static opcode h6280_051 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 7 EOR  IDY
    static opcode h6280_071 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 7 ADC  AZP
    static opcode h6280_091 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            tmp = h6280.u8_a;
            WR_IDY(tmp);
        }
    }; // 7 STA  IDY
    static opcode h6280_0b1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
        }
    }; // 7 LDA  IDY
    static opcode h6280_0d1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 7 CMP  IDY
    static opcode h6280_0f1 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_IDY();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 7 SBC  IDY

    static opcode h6280_002 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            SXY();
        }
    }; // 3 SXY
    static opcode h6280_022 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            SAX();
        }
    }; // 3 SAX
    static opcode h6280_042 = new opcode() {
        public void handler() {
            int tmp;
            /*TODO*///            h6280_ICount[0] -= 3;SAY;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 3 SAY
    static opcode h6280_062 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLA();
        }
    }; // 2 CLA
    static opcode h6280_082 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLX();
            if (h6280log != null) {
                fprintf(h6280log, "082 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 CLX
    static opcode h6280_0a2 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            LDX(tmp);
        }
    }; // 2 LDX  IMM
    static opcode h6280_0c2 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLY();
        }
    }; // 2 CLY
    static opcode h6280_0e2 = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???

    static opcode h6280_012 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 7 ORA  ZPI
    static opcode h6280_032 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 7 AND  ZPI
    static opcode h6280_052 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 7 EOR  ZPI
    static opcode h6280_072 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 7 ADC  ZPI
    static opcode h6280_092 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            tmp = h6280.u8_a;
            WR_ZPI(tmp);
        }
    }; // 7 STA  ZPI
    static opcode h6280_0b2 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
        }
    }; // 7 LDA  ZPI
    static opcode h6280_0d2 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 7 CMP  ZPI
    static opcode h6280_0f2 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPI();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 7 SBC  ZPI

    static opcode h6280_003 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            ST0(tmp);
        }
    }; // 4 ST0  IMM
    static opcode h6280_023 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            ST2(tmp);
        }
    }; // 4 ST2  IMM
    static opcode h6280_043 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            TMA(tmp);
        }
    }; // 4 TMA
    static opcode h6280_063 = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_083 = new opcode() {
        public void handler() {
            int tmp, tmp2;
            h6280_ICount[0] -= 7;
            tmp2 = RDOPARG();
            h6280.pc.AddD(1);;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            TST(tmp, tmp2);
        }
    }; // 7 TST  IMM,ZPG
    static opcode h6280_0a3 = new opcode() {
        public void handler() {
            int tmp, tmp2;
            h6280_ICount[0] -= 7;
            tmp2 = RDOPARG();
            h6280.pc.AddD(1);;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            TST(tmp, tmp2);
        }
    }; // 7 TST  IMM,ZPX
    static opcode h6280_0c3 = new opcode() {
        public void handler() {
            /*TODO*///           int to, from, length;TDD;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 6*l+17 TDD  XFER
    static opcode h6280_0e3 = new opcode() {
        public void handler() {
            /*TODO*///            int to, from, length, alternate;TIA;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 6*l+17 TIA  XFER

    static opcode h6280_013 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            ST1(tmp);
        }
    }; // 4 ST1
    static opcode h6280_033 = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_053 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            TAM(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "053 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 5 TAM  IMM
    static opcode h6280_073 = new opcode() {
        public void handler() {
            int to = 0, from = 0, length = 0;
            TII(to, from, length);
        }
    }; // 6*l+17 TII  XFER
    static opcode h6280_093 = new opcode() {
        public void handler() {
            int tmp, tmp2;
            h6280_ICount[0] -= 8;
            tmp2 = RDOPARG();
            h6280.pc.AddD(1);
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            TST(tmp, tmp2);
        }
    }; // 8 TST  IMM,ABS
    static opcode h6280_0b3 = new opcode() {
        public void handler() {
            int tmp, tmp2;
            h6280_ICount[0] -= 8;
            tmp2 = RDOPARG();
            h6280.pc.AddD(1);;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            TST(tmp, tmp2);
            if (h6280log != null) {
                fprintf(h6280log, "0b3 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 8 TST  IMM,ABX
    static opcode h6280_0d3 = new opcode() {
        public void handler() {
            /*TODO*///            int to, from, length;TIN;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 6*l+17 TIN  XFER
    static opcode h6280_0f3 = new opcode() {
        public void handler() {
            TAI();
            if (h6280log != null) {
                fprintf(h6280log, "0f3 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 6*l+17 TAI  XFER

    static opcode h6280_004 = new opcode() {
        public void handler() {
            int tmp;
            /*TODO*///            h6280_ICount[0] -= 6;EA_ZPG(); tmp = RDMEMZ(h6280.ea.D);TSB;WB_EAZ(tmp);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 6 TSB  ZPG
    static opcode h6280_024 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BIT(tmp);
        }
    }; // 4 BIT  ZPG
    static opcode h6280_044 = new opcode() {
        public void handler() {
            int tmp = 0;
            BSR(tmp);
        }
    }; // 8 BSR  REL
    static opcode h6280_064 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = 0;
            WR_ZPG(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "064 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 4 STZ  ZPG
    static opcode h6280_084 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_y & 0xFF;
            WR_ZPG(tmp);
        }
    }; // 4 STY  ZPG
    static opcode h6280_0a4 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            LDY(tmp);
        }
    }; // 4 LDY  ZPG
    static opcode h6280_0c4 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            CPY(tmp);
        }
    }; // 4 CPY  ZPG
    static opcode h6280_0e4 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            CPX(tmp);
        }
    }; // 4 CPX  ZPG

    static opcode h6280_014 = new opcode() {
        public void handler() {
            int tmp;
            /*TODO*///            h6280_ICount[0] -= 6;EA_ZPG(); tmp = RDMEMZ(h6280.ea.D);TRB;WB_EAZ(tmp);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 6 TRB  ZPG
    static opcode h6280_034 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            BIT(tmp);
        }
    }; // 4 BIT  ZPX
    static opcode h6280_054 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
        }
    }; // 2 CSL
    static opcode h6280_074 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = 0;
            WR_ZPX(tmp);
        }
    }; // 4 STZ  ZPX
    static opcode h6280_094 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_y & 0xFF;
            WR_ZPX(tmp);
        }
    }; // 4 STY  ZPX
    static opcode h6280_0b4 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            LDY(tmp);
        }
    }; // 4 LDY  ZPX
    static opcode h6280_0d4 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            if (h6280log != null) {
                fprintf(h6280log, "0d4 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 CSH
    static opcode h6280_0f4 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            SET();
        }
    }; // 2 SET

    static opcode h6280_005 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            ORA(tmp);
        }
    }; // 4 ORA  ZPG
    static opcode h6280_025 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            AND(tmp);
        }
    }; // 4 AND  ZPG
    static opcode h6280_045 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            EOR(tmp);
        }
    }; // 4 EOR  ZPG
    static opcode h6280_065 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            ADC(tmp);
        }
    }; // 4 ADC  ZPG
    static opcode h6280_085 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_a;
            WR_ZPG(tmp);
        }
    }; // 4 STA  ZPG
    static opcode h6280_0a5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            LDA(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0a5 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 4 LDA  ZPG
    static opcode h6280_0c5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            CMP(tmp);
        }
    }; // 4 CMP  ZPG
    static opcode h6280_0e5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            SBC(tmp);
        }
    }; // 4 SBC  ZPG

    static opcode h6280_015 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            ORA(tmp);
        }
    }; // 4 ORA  ZPX
    static opcode h6280_035 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            AND(tmp);
        }
    }; // 4 AND  ZPX
    static opcode h6280_055 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            EOR(tmp);
        }
    }; // 4 EOR  ZPX
    static opcode h6280_075 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            ADC(tmp);
        }
    }; // 4 ADC  ZPX
    static opcode h6280_095 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_a;
            WR_ZPX(tmp);
        }
    }; // 4 STA  ZPX
    static opcode h6280_0b5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            LDA(tmp);
        }
    }; // 4 LDA  ZPX
    static opcode h6280_0d5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            CMP(tmp);
        }
    }; // 4 CMP  ZPX
    static opcode h6280_0f5 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            SBC(tmp);
        }
    }; // 4 SBC  ZPX

    static opcode h6280_006 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //ASL;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 7) & _fC)) & 0xFF;
            tmp = (tmp << 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ASL  ZPG
    static opcode h6280_026 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //ROL;
            tmp = (tmp << 1) | (h6280.u8_p & _fC);
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 8) & _fC)) & 0xFF;
            tmp = tmp & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ROL  ZPG
    static opcode h6280_046 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //LSR;
            h6280.u8_p = (h6280.u8_p & ~_fC) | (tmp & _fC);
            tmp = (tmp & 0xFF) >>> 1;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 LSR  ZPG
    static opcode h6280_066 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //ROR;
            tmp |= (h6280.u8_p & _fC) << 8;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp >> 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ROR  ZPG
    static opcode h6280_086 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_x & 0xFF;
            WR_ZPG(tmp);
        }
    }; // 4 STX  ZPG
    static opcode h6280_0a6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            LDX(tmp);
        }
    }; // 4 LDX  ZPG
    static opcode h6280_0c6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            tmp = (tmp - 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 DEC  ZPG
    static opcode h6280_0e6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            tmp = (tmp + 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 INC  ZPG

    static opcode h6280_016 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            //ASL;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 7) & _fC)) & 0xFF;
            tmp = (tmp << 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ASL  ZPX
    static opcode h6280_036 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            //ROL;
            tmp = (tmp << 1) | (h6280.u8_p & _fC);
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 8) & _fC)) & 0xFF;
            tmp = tmp & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ROL  ZPX
    static opcode h6280_056 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            //LSR;
            h6280.u8_p = (h6280.u8_p & ~_fC) | (tmp & _fC);
            tmp = (tmp & 0xFF) >>> 1;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 LSR  ZPX
    static opcode h6280_076 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            //ROR;
            tmp |= (h6280.u8_p & _fC) << 8;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp >> 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 ROR  ZPX
    static opcode h6280_096 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            tmp = h6280.u8_x & 0xFF;
            WR_ZPY(tmp);
        }
    }; // 4 STX  ZPY
    static opcode h6280_0b6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPY();
            tmp = RDMEMZ(h6280.ea.D);
            LDX(tmp);
        }
    }; // 4 LDX  ZPY
    static opcode h6280_0d6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            tmp = (tmp - 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 DEC  ZPX
    static opcode h6280_0f6 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 6;
            EA_ZPX();
            tmp = RDMEMZ(h6280.ea.D);
            tmp = (tmp + 1) & 0xFF;
            SET_NZ(tmp);
            WB_EAZ(tmp);
        }
    }; // 6 INC  ZPX

    static opcode h6280_007 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(0);
            tmp &= ~(1 << 0);
            WB_EAZ(tmp);

        }
    }; // 7 RMB0 ZPG
    static opcode h6280_027 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(2);
            tmp &= ~(1 << 2);
            WB_EAZ(tmp);

        }
    }; // 7 RMB2 ZPG
    static opcode h6280_047 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(4);
            tmp &= ~(1 << 4);
            WB_EAZ(tmp);
        }
    }; // 7 RMB4 ZPG
    static opcode h6280_067 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(6);
            tmp &= ~(1 << 6);
            WB_EAZ(tmp);
        }
    }; // 7 RMB6 ZPG
    static opcode h6280_087 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(0);
            tmp |= (1 << 0);
            WB_EAZ(tmp);
        }
    }; // 7 SMB0 ZPG
    static opcode h6280_0a7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(2);
            tmp |= (1 << 2);
            WB_EAZ(tmp);
        }
    }; // 7 SMB2 ZPG
    static opcode h6280_0c7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(4);
            tmp |= (1 << 4);
            WB_EAZ(tmp);
        }
    }; // 7 SMB4 ZPG
    static opcode h6280_0e7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(6);
            tmp |= (1 << 6);
            WB_EAZ(tmp);
        }
    }; // 7 SMB6 ZPG

    static opcode h6280_017 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(1);
            tmp &= ~(1 << 1);
            WB_EAZ(tmp);
        }
    }; // 7 RMB1 ZPG
    static opcode h6280_037 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(3);
            tmp &= ~(1 << 3);
            WB_EAZ(tmp);
        }
    }; // 7 RMB3 ZPG
    static opcode h6280_057 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(5);
            tmp &= ~(1 << 5);
            WB_EAZ(tmp);
        }
    }; // 7 RMB5 ZPG
    static opcode h6280_077 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //RMB(7);
            tmp &= ~(1 << 7);
            WB_EAZ(tmp);
        }
    }; // 7 RMB7 ZPG
    static opcode h6280_097 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(1);
            tmp |= (1 << 1);
            WB_EAZ(tmp);
        }
    }; // 7 SMB1 ZPG
    static opcode h6280_0b7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(3);
            tmp |= (1 << 3);
            WB_EAZ(tmp);
        }
    }; // 7 SMB3 ZPG
    static opcode h6280_0d7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(5);
            tmp |= (1 << 5);
            WB_EAZ(tmp);
        }
    }; // 7 SMB5 ZPG
    static opcode h6280_0f7 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            //SMB(7);
            tmp |= (1 << 7);
            WB_EAZ(tmp);
        }
    }; // 7 SMB7 ZPG

    static opcode h6280_008 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            PHP();
        }
    }; // 3 PHP
    static opcode h6280_028 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 4;
            PLP();
        }
    }; // 4 PLP
    static opcode h6280_048 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            PHA();
        }
    }; // 3 PHA
    static opcode h6280_068 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 4;
            PLA();
        }
    }; // 4 PLA
    static opcode h6280_088 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            DEY();
        }
    }; // 2 DEY
    static opcode h6280_0a8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            TAY();
        }
    }; // 2 TAY
    static opcode h6280_0c8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            INY();
        }
    }; // 2 INY
    static opcode h6280_0e8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            INX();
        }
    }; // 2 INX

    static opcode h6280_018 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLC();
        }
    }; // 2 CLC
    static opcode h6280_038 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            SEC();
        }
    }; // 2 SEC
    static opcode h6280_058 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLI();
        }
    }; // 2 CLI
    static opcode h6280_078 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            SEI();
            if (h6280log != null) {
                fprintf(h6280log, "078 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 SEI
    static opcode h6280_098 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            TYA();
            if (h6280log != null) {
                fprintf(h6280log, "098 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 TYA
    static opcode h6280_0b8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLV();
        }
    }; // 2 CLV
    static opcode h6280_0d8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            CLD();
            if (h6280log != null) {
                fprintf(h6280log, "0d8 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 CLD
    static opcode h6280_0f8 = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            SED();
        }
    }; // 2 SED

    static opcode h6280_009 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            ORA(tmp);
        }
    }; // 2 ORA  IMM
    static opcode h6280_029 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            AND(tmp);
        }
    }; // 2 AND  IMM
    static opcode h6280_049 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            EOR(tmp);
        }
    }; // 2 EOR  IMM
    static opcode h6280_069 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            ADC(tmp);
        }
    }; // 2 ADC  IMM
    static opcode h6280_089 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            BIT(tmp);
        }
    }; // 2 BIT  IMM
    static opcode h6280_0a9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            LDA(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0a9 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 LDA  IMM
    static opcode h6280_0c9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            CMP(tmp);
        }
    }; // 2 CMP  IMM
    static opcode h6280_0e9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = RDOPARG();
            h6280.pc.AddD(1);
            SBC(tmp);
        }
    }; // 2 SBC  IMM

    static opcode h6280_019 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 5 ORA  ABY
    static opcode h6280_039 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 5 AND  ABY
    static opcode h6280_059 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 5 EOR  ABY
    static opcode h6280_079 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 5 ADC  ABY
    static opcode h6280_099 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = h6280.u8_a;
            WR_ABY(tmp);
        }
    }; // 5 STA  ABY
    static opcode h6280_0b9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
        }
    }; // 5 LDA  ABY
    static opcode h6280_0d9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 5 CMP  ABY
    static opcode h6280_0f9 = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 5 SBC  ABY

    static opcode h6280_00a = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = h6280.u8_a & 0xFF;
            //ASL;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 7) & _fC)) & 0xFF;
            tmp = (tmp << 1) & 0xFF;
            SET_NZ(tmp);
            WB_ACC(tmp);
        }
    }; // 2 ASL  A
    static opcode h6280_02a = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = h6280.u8_a & 0xFF;
            //ROL;
            tmp = (tmp << 1) | (h6280.u8_p & _fC);
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 8) & _fC)) & 0xFF;
            tmp = tmp & 0xFF;
            SET_NZ(tmp);
            WB_ACC(tmp);
        }
    }; // 2 ROL  A
    static opcode h6280_04a = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = h6280.u8_a & 0xFF;
            //LSR;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp & 0xFF) >>> 1;
            SET_NZ(tmp);
            WB_ACC(tmp);
        }
    }; // 2 LSR  A
    static opcode h6280_06a = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 2;
            tmp = h6280.u8_a & 0xFF;
            //ROR;
            tmp |= (h6280.u8_p & _fC) << 8;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp >> 1) & 0xFF;
            SET_NZ(tmp);
            WB_ACC(tmp);
        }
    }; // 2 ROR  A
    static opcode h6280_08a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            TXA();
        }
    }; // 2 TXA
    static opcode h6280_0aa = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            TAX();
        }
    }; // 2 TAX
    static opcode h6280_0ca = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            DEX();
        }
    }; // 2 DEX
    static opcode h6280_0ea = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            NOP();
        }
    }; // 2 NOP

    static opcode h6280_01a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            INA();
        }
    }; // 2 INC  A
    static opcode h6280_03a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            DEA();
        }
    }; // 2 DEC  A
    static opcode h6280_05a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            PHY();
        }
    }; // 3 PHY
    static opcode h6280_07a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 4;
            PLY();
        }
    }; // 4 PLY
    static opcode h6280_09a = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 2;
            TXS();
            if (h6280log != null) {
                fprintf(h6280log, "09a :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 2 TXS
    static opcode h6280_0ba = new opcode() {
        public void handler() {
            /*TODO*///            h6280_ICount[0] -= 2;TSX;
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 2 TSX
    static opcode h6280_0da = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 3;
            PHX();
        }
    }; // 3 PHX
    static opcode h6280_0fa = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 4;
            PLX();
        }
    }; // 4 PLX

    static opcode h6280_00b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_02b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_04b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_06b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_08b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0ab = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0cb = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0eb = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???

    static opcode h6280_01b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_03b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_05b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_07b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_09b = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0bb = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0db = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0fb = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???

    static opcode h6280_00c = new opcode() {
        public void handler() {
            int tmp;
            /*TODO*///            h6280_ICount[0] -= 7;EA_ABS(); tmp = RDMEM(h6280.ea.D);TSB;WB_EA(tmp);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 7 TSB  ABS
    static opcode h6280_02c = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            BIT(tmp);
        }
    }; // 5 BIT  ABS
    static opcode h6280_04c = new opcode() {
        public void handler() {
            h6280_ICount[0] -= 4;
            EA_ABS();
            JMP();
        }
    }; // 4 JMP  ABS
    static opcode h6280_06c = new opcode() {
        public void handler() {
            int tmp = 0;
            h6280_ICount[0] -= 7;
            EA_IND(tmp);
            JMP();
        }
    }; // 7 JMP  IND
    static opcode h6280_08c = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = h6280.u8_y & 0xFF;
            WR_ABS(tmp);
        }
    }; // 5 STY  ABS
    static opcode h6280_0ac = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            LDY(tmp);
        }
    }; // 5 LDY  ABS
    static opcode h6280_0cc = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            CPY(tmp);
        }
    }; // 5 CPY  ABS
    static opcode h6280_0ec = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            CPX(tmp);
        }
    }; // 5 CPX  ABS

    static opcode h6280_01c = new opcode() {
        public void handler() {
            int tmp;
            /*TODO*///            h6280_ICount[0] -= 7;EA_ABS(); tmp = RDMEM(h6280.ea.D);TRB;WB_EA(tmp);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }; // 7 TRB  ABS
    static opcode h6280_03c = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            BIT(tmp);
        }
    }; // 5 BIT  ABX
    static opcode h6280_05c = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_07c = new opcode() {
        public void handler() {
            int tmp = 0;
            h6280_ICount[0] -= 7;
            EA_IAX();
            JMP();
        }
    }; // 7 JMP  IAX
    static opcode h6280_09c = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = 0;
            WR_ABS(tmp);
        }
    }; // 5 STZ  ABS
    static opcode h6280_0bc = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            LDY(tmp);
        }
    }; // 5 LDY  ABX
    static opcode h6280_0dc = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???
    static opcode h6280_0fc = new opcode() {
        public void handler() {
            ILL();
        }
    }; // 2 ???

    static opcode h6280_00d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 5 ORA  ABS
    static opcode h6280_02d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 4 AND  ABS
    static opcode h6280_04d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 4 EOR  ABS
    static opcode h6280_06d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 4 ADC  ABS
    static opcode h6280_08d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = h6280.u8_a;
            WR_ABS(tmp);
        }
    }; // 4 STA  ABS
    static opcode h6280_0ad = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
            if (h6280log != null) {
                fprintf(h6280log, "0ad :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,MMR0:%d,MMR1:%d,MMR2:%d,MMR3:%d,MMR4:%d,MMR5:%d,MMR6:%d,MMR7:%d,IRQM:%d,TS:%d,TA:%d,TV:%d,TL:%d,EC:%d,NMIS:%d,IR1:%d,IR2:%d,IR3:%d\n", h6280.ppc.D, h6280.pc.D, h6280.sp.D, h6280.zp.D, h6280.ea.D, h6280.u8_a, h6280.u8_x, h6280.u8_y, h6280.u8_p, h6280.u8_mmr[0], h6280.u8_mmr[1], h6280.u8_mmr[2], h6280.u8_mmr[3], h6280.u8_mmr[4], h6280.u8_mmr[5], h6280.u8_mmr[6], h6280.u8_mmr[7], h6280.u8_irq_mask, h6280.u8_timer_status, h6280.u8_timer_ack, h6280.timer_value, h6280.timer_load, h6280.extra_cycles, h6280.nmi_state, h6280.irq_state[0], h6280.irq_state[1], h6280.irq_state[2]);
            }
        }
    }; // 4 LDA  ABS
    static opcode h6280_0cd = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 4 CMP  ABS
    static opcode h6280_0ed = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 4 SBC  ABS

    static opcode h6280_01d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            ORA(tmp);
        }
    }; // 5 ORA  ABX
    static opcode h6280_03d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            AND(tmp);
        }
    }; // 4 AND  ABX
    static opcode h6280_05d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            EOR(tmp);
        }
    }; // 4 EOR  ABX
    static opcode h6280_07d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            ADC(tmp);
        }
    }; // 4 ADC  ABX
    static opcode h6280_09d = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = h6280.u8_a;
            WR_ABX(tmp);
        }
    }; // 5 STA  ABX
    static opcode h6280_0bd = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            LDA(tmp);
        }
    }; // 5 LDA  ABX
    static opcode h6280_0dd = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            CMP(tmp);
        }
    }; // 4 CMP  ABX
    static opcode h6280_0fd = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            SBC(tmp);
        }
    }; // 4 SBC  ABX

    static opcode h6280_00e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            //ASL;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 7) & _fC)) & 0xFF;
            tmp = (tmp << 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 ASL  ABS
    static opcode h6280_02e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            //ROL;
            tmp = (tmp << 1) | (h6280.u8_p & _fC);
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 8) & _fC)) & 0xFF;
            tmp = tmp & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 ROL  ABS
    static opcode h6280_04e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            //LSR;
            h6280.u8_p = (h6280.u8_p & ~_fC) | (tmp & _fC);
            tmp = (tmp & 0xFF) >>> 1;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 LSR  ABS
    static opcode h6280_06e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            //ROR;
            tmp |= (h6280.u8_p & _fC) << 8;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp >> 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 ROR  ABS
    static opcode h6280_08e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = h6280.u8_x & 0xFF;
            WR_ABS(tmp);
        }
    }; // 4 STX  ABS
    static opcode h6280_0ae = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            LDX(tmp);
        }
    }; // 5 LDX  ABS
    static opcode h6280_0ce = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            tmp = (tmp - 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 DEC  ABS
    static opcode h6280_0ee = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABS();
            tmp = RDMEM(h6280.ea.D);
            tmp = (tmp + 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 6 INC  ABS

    static opcode h6280_01e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            //ASL;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 7) & _fC)) & 0xFF;
            tmp = (tmp << 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 ASL  ABX
    static opcode h6280_03e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            //ROL;
            tmp = (tmp << 1) | (h6280.u8_p & _fC);
            h6280.u8_p = ((h6280.u8_p & ~_fC) | ((tmp >> 8) & _fC)) & 0xFF;
            tmp = tmp & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 ROL  ABX
    static opcode h6280_05e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            //LSR;
            h6280.u8_p = (h6280.u8_p & ~_fC) | (tmp & _fC);
            tmp = (tmp & 0xFF) >>> 1;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 LSR  ABX
    static opcode h6280_07e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            //ROR;
            tmp |= (h6280.u8_p & _fC) << 8;
            h6280.u8_p = ((h6280.u8_p & ~_fC) | (tmp & _fC)) & 0xFF;
            tmp = (tmp >> 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 ROR  ABX
    static opcode h6280_09e = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            tmp = 0;
            WR_ABX(tmp);
        }
    }; // 5 STZ  ABX
    static opcode h6280_0be = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 5;
            EA_ABY();
            tmp = RDMEM(h6280.ea.D);
            LDX(tmp);
        }
    }; // 4 LDX  ABY
    static opcode h6280_0de = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            tmp = (tmp - 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 DEC  ABX
    static opcode h6280_0fe = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 7;
            EA_ABX();
            tmp = RDMEM(h6280.ea.D);
            tmp = (tmp + 1) & 0xFF;
            SET_NZ(tmp);
            WB_EA(tmp);
        }
    }; // 7 INC  ABX

    static opcode h6280_00f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(0, tmp);
        }
    }; // 6/8 BBR0 ZPG,REL
    static opcode h6280_02f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(2, tmp);
        }
    }; // 6/8 BBR2 ZPG,REL
    static opcode h6280_04f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(4, tmp);
        }
    }; // 6/8 BBR4 ZPG,REL
    static opcode h6280_06f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(6, tmp);
        }
    }; // 6/8 BBR6 ZPG,REL
    static opcode h6280_08f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(0, tmp);
        }
    }; // 6/8 BBS0 ZPG,REL
    static opcode h6280_0af = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(2, tmp);
        }
    }; // 6/8 BBS2 ZPG,REL
    static opcode h6280_0cf = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(4, tmp);
        }
    }; // 6/8 BBS4 ZPG,REL
    static opcode h6280_0ef = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(6, tmp);
        }
    }; // 6/8 BBS6 ZPG,REL

    static opcode h6280_01f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(1, tmp);
        }
    }; // 6/8 BBR1 ZPG,REL
    static opcode h6280_03f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(3, tmp);
        }
    }; // 6/8 BBR3 ZPG,REL
    static opcode h6280_05f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(5, tmp);
        }
    }; // 6/8 BBR5 ZPG,REL
    static opcode h6280_07f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBR(7, tmp);
        }
    }; // 6/8 BBR7 ZPG,REL
    static opcode h6280_09f = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(1, tmp);
        }
    }; // 6/8 BBS1 ZPG,REL
    static opcode h6280_0bf = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(3, tmp);
        }
    }; // 6/8 BBS3 ZPG,REL
    static opcode h6280_0df = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(5, tmp);
        }
    }; // 6/8 BBS5 ZPG,REL
    static opcode h6280_0ff = new opcode() {
        public void handler() {
            int tmp;
            h6280_ICount[0] -= 4;
            EA_ZPG();
            tmp = RDMEMZ(h6280.ea.D);
            BBS(7, tmp);
        }
    }; // 6/8 BBS7 ZPG,REL

    static opcode[] insnh6280 = {
        h6280_000, h6280_001, h6280_002, h6280_003, h6280_004, h6280_005, h6280_006, h6280_007,
        h6280_008, h6280_009, h6280_00a, h6280_00b, h6280_00c, h6280_00d, h6280_00e, h6280_00f,
        h6280_010, h6280_011, h6280_012, h6280_013, h6280_014, h6280_015, h6280_016, h6280_017,
        h6280_018, h6280_019, h6280_01a, h6280_01b, h6280_01c, h6280_01d, h6280_01e, h6280_01f,
        h6280_020, h6280_021, h6280_022, h6280_023, h6280_024, h6280_025, h6280_026, h6280_027,
        h6280_028, h6280_029, h6280_02a, h6280_02b, h6280_02c, h6280_02d, h6280_02e, h6280_02f,
        h6280_030, h6280_031, h6280_032, h6280_033, h6280_034, h6280_035, h6280_036, h6280_037,
        h6280_038, h6280_039, h6280_03a, h6280_03b, h6280_03c, h6280_03d, h6280_03e, h6280_03f,
        h6280_040, h6280_041, h6280_042, h6280_043, h6280_044, h6280_045, h6280_046, h6280_047,
        h6280_048, h6280_049, h6280_04a, h6280_04b, h6280_04c, h6280_04d, h6280_04e, h6280_04f,
        h6280_050, h6280_051, h6280_052, h6280_053, h6280_054, h6280_055, h6280_056, h6280_057,
        h6280_058, h6280_059, h6280_05a, h6280_05b, h6280_05c, h6280_05d, h6280_05e, h6280_05f,
        h6280_060, h6280_061, h6280_062, h6280_063, h6280_064, h6280_065, h6280_066, h6280_067,
        h6280_068, h6280_069, h6280_06a, h6280_06b, h6280_06c, h6280_06d, h6280_06e, h6280_06f,
        h6280_070, h6280_071, h6280_072, h6280_073, h6280_074, h6280_075, h6280_076, h6280_077,
        h6280_078, h6280_079, h6280_07a, h6280_07b, h6280_07c, h6280_07d, h6280_07e, h6280_07f,
        h6280_080, h6280_081, h6280_082, h6280_083, h6280_084, h6280_085, h6280_086, h6280_087,
        h6280_088, h6280_089, h6280_08a, h6280_08b, h6280_08c, h6280_08d, h6280_08e, h6280_08f,
        h6280_090, h6280_091, h6280_092, h6280_093, h6280_094, h6280_095, h6280_096, h6280_097,
        h6280_098, h6280_099, h6280_09a, h6280_09b, h6280_09c, h6280_09d, h6280_09e, h6280_09f,
        h6280_0a0, h6280_0a1, h6280_0a2, h6280_0a3, h6280_0a4, h6280_0a5, h6280_0a6, h6280_0a7,
        h6280_0a8, h6280_0a9, h6280_0aa, h6280_0ab, h6280_0ac, h6280_0ad, h6280_0ae, h6280_0af,
        h6280_0b0, h6280_0b1, h6280_0b2, h6280_0b3, h6280_0b4, h6280_0b5, h6280_0b6, h6280_0b7,
        h6280_0b8, h6280_0b9, h6280_0ba, h6280_0bb, h6280_0bc, h6280_0bd, h6280_0be, h6280_0bf,
        h6280_0c0, h6280_0c1, h6280_0c2, h6280_0c3, h6280_0c4, h6280_0c5, h6280_0c6, h6280_0c7,
        h6280_0c8, h6280_0c9, h6280_0ca, h6280_0cb, h6280_0cc, h6280_0cd, h6280_0ce, h6280_0cf,
        h6280_0d0, h6280_0d1, h6280_0d2, h6280_0d3, h6280_0d4, h6280_0d5, h6280_0d6, h6280_0d7,
        h6280_0d8, h6280_0d9, h6280_0da, h6280_0db, h6280_0dc, h6280_0dd, h6280_0de, h6280_0df,
        h6280_0e0, h6280_0e1, h6280_0e2, h6280_0e3, h6280_0e4, h6280_0e5, h6280_0e6, h6280_0e7,
        h6280_0e8, h6280_0e9, h6280_0ea, h6280_0eb, h6280_0ec, h6280_0ed, h6280_0ee, h6280_0ef,
        h6280_0f0, h6280_0f1, h6280_0f2, h6280_0f3, h6280_0f4, h6280_0f5, h6280_0f6, h6280_0f7,
        h6280_0f8, h6280_0f9, h6280_0fa, h6280_0fb, h6280_0fc, h6280_0fd, h6280_0fe, h6280_0ff
    };

}
