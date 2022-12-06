package gr.codebb.arcadeflex.v037b7.cpu.m6502;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.m6502.m6502H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class n2a03 extends m6502 {

    public static int[] m6502_ICount = new int[1];

    public n2a03() {
        cpu_num = CPU_N2A03;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = N2A03_INT_NONE;
        irq_int = N2A03_INT_IRQ;
        nmi_int = N2A03_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6502_ICount;

        m6502_ICount[0] = 0;
    }

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
            D = ((H << 8) | L) & 0xFFFF;
        }

        public void AddL(int val) {
            L = (L + val) & 0xFF;
            D = ((H << 8) | L) & 0xFFFF;
        }

        public void AddD(int val) {
            D = (D + val) & 0xFFFF;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }
    };

    public static class m6502_Regs {

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
        int u8_pending_irq;
        /* nonzero if an IRQ is pending */
        int u8_after_cli;
        /* pending IRQ and last insn cleared I */
        int u8_nmi_state;
        int u8_irq_state;
        int u8_so_state;
        public IrqCallbackHandlerPtr irq_callback;/* IRQ callback */
    }

    static m6502_Regs m6502 = new m6502_Regs();

    /**
     * ***************************************************************************
     *
     * 6502 CPU interface functions
     *
     ****************************************************************************
     */
    @Override
    public void reset(Object param) {
        /* wipe out the rest of the m6502 structure */
 /* read the reset vector into PC */
        m6502.pc.SetL(RDMEM(M6502_RST_VEC));
        m6502.pc.SetH(RDMEM(M6502_RST_VEC + 1));

        m6502.sp.SetD(0x01ff);
        /* stack pointer starts at page 1 offset FF */
        m6502.u8_p = (F_T | F_I | F_Z | F_B | (m6502.u8_p & F_D)) & 0xFF;
        /* set T, I and Z flags */
        m6502.u8_pending_irq = 0;
        /* nonzero if an IRQ is pending */
        m6502.u8_after_cli = 0;
        /* pending IRQ and last insn cleared I */
        m6502.irq_callback = null;

        change_pc16(m6502.pc.D);
    }

    @Override
    public void exit() {
        /* nothing to do yet */
    }

    public static void m65c02_take_irq() {
        if ((m6502.u8_p & F_I) == 0) {
            m6502.ea.SetD(M6502_IRQ_VEC);
            m6502_ICount[0] -= 7;
            PUSH(m6502.pc.H);
            PUSH(m6502.pc.L);
            PUSH(m6502.u8_p & ~F_B);
            m6502.u8_p = ((m6502.u8_p & ~F_D) | F_I) & 0xFF;
            /* knock out D and set I flag */
 /* set I flag */
            m6502.pc.SetL(RDMEM(m6502.ea.D));
            m6502.pc.SetH(RDMEM(m6502.ea.D + 1));
            //LOG(("M6502#%d takes IRQ ($%04x)\n", cpu_getactivecpu(), PCD));
            /* call back the cpuintrf to let it clear the line */
            if (m6502.irq_callback != null) {
                (m6502.irq_callback).handler(0);
            }
            change_pc16(m6502.pc.D);
        }
        m6502.u8_pending_irq = 0;
    }

    @Override
    public int execute(int cycles) {
        m6502_ICount[0] = cycles;

        change_pc16(m6502.pc.D);

        do {
            int op;
            m6502.ppc.SetD(m6502.pc.D);//PPC = PCD;

            /* if an irq is pending, take it now */
            if (m6502.u8_pending_irq != 0) {
                m65c02_take_irq();
            }

            op = RDOP();
            insn6502[op].handler();

            /* check if the I flag was just reset (interrupts enabled) */
            if (m6502.u8_after_cli != 0) {
                //LOG(("M6502#%d after_cli was >0", cpu_getactivecpu()));
                m6502.u8_after_cli = 0;
                if (m6502.u8_irq_state != CLEAR_LINE) {
                    //LOG((": irq line is asserted: set pending IRQ\n"));
                    m6502.u8_pending_irq = 1;
                } else {
                    //LOG((": irq line is clear\n"));
                }
            } else if (m6502.u8_pending_irq != 0) {
                m65c02_take_irq();
            }

        } while (m6502_ICount[0] > 0);

        return cycles - m6502_ICount[0];
    }

    @Override
    public Object init_context() {
        Object reg = new m6502_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        m6502_Regs regs = new m6502_Regs();
        regs.ppc.SetD(m6502.ppc.D);
        regs.pc.SetD(m6502.pc.D);
        regs.zp.SetD(m6502.zp.D);
        regs.sp.SetD(m6502.sp.D);
        regs.ea.SetD(m6502.ea.D);
        regs.u8_a = m6502.u8_a;
        regs.u8_x = m6502.u8_x;
        regs.u8_y = m6502.u8_y;
        regs.u8_p = m6502.u8_p;
        regs.u8_pending_irq = m6502.u8_pending_irq;
        regs.u8_after_cli = m6502.u8_after_cli;
        regs.u8_nmi_state = m6502.u8_nmi_state;
        regs.u8_irq_state = m6502.u8_irq_state;
        regs.u8_so_state = m6502.u8_so_state;
        regs.irq_callback = m6502.irq_callback;
        return regs;
    }

    @Override
    public void set_context(Object reg) {
        m6502_Regs regs = (m6502_Regs) reg;
        m6502.ppc.SetD(regs.ppc.D);
        m6502.pc.SetD(regs.pc.D);
        m6502.zp.SetD(regs.zp.D);
        m6502.sp.SetD(regs.sp.D);
        m6502.ea.SetD(regs.ea.D);
        m6502.u8_a = regs.u8_a;
        m6502.u8_x = regs.u8_x;
        m6502.u8_y = regs.u8_y;
        m6502.u8_p = regs.u8_p;
        m6502.u8_pending_irq = regs.u8_pending_irq;
        m6502.u8_after_cli = regs.u8_after_cli;
        m6502.u8_nmi_state = regs.u8_nmi_state;
        m6502.u8_irq_state = regs.u8_irq_state;
        m6502.u8_so_state = regs.u8_so_state;
        m6502.irq_callback = regs.irq_callback;

        change_pc(m6502.pc.D);
    }

    @Override
    public int get_pc() {
        return m6502.pc.D & 0xFFFF;
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
        if (m6502.u8_nmi_state == state) {
            return;
        }
        m6502.u8_nmi_state = state;
        if (state != CLEAR_LINE) {
            //LOG(( "M6502#%d set_nmi_line(ASSERT)\n", cpu_getactivecpu()));
            m6502.ea.SetD(M6502_NMI_VEC);
            m6502_ICount[0] -= 7;
            PUSH(m6502.pc.H);
            PUSH(m6502.pc.L);
            PUSH(m6502.u8_p & ~F_B);
            m6502.u8_p = (m6502.u8_p | F_I);
            /* set I flag */
            m6502.pc.SetL(RDMEM(m6502.ea.D));
            m6502.pc.SetH(RDMEM(m6502.ea.D + 1));
            //LOG(("M6502#%d takes NMI ($%04x)\n", cpu_getactivecpu(), PCD));
            change_pc16(m6502.pc.D);
        }
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        if (irqline == M6502_SET_OVERFLOW) {
            if (m6502.u8_so_state != 0 && state == 0) {
                //LOG(( "M6502#%d set overflow\n", cpu_getactivecpu()));
                m6502.u8_p = (m6502.u8_p | F_V) & 0xFF;
            }
            m6502.u8_so_state = state;
            return;
        }
        m6502.u8_irq_state = state;
        if (state != CLEAR_LINE) {
            //LOG(( "M6502#%d set_irq_line(ASSERT)\n", cpu_getactivecpu()));
            m6502.u8_pending_irq = 1;
        }
    }

    @Override
    public void set_irq_callback(IrqCallbackHandlerPtr callback) {
        m6502.irq_callback = callback;
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
        /*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	m6502_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &m6502;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+M6502_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_S: sprintf(buffer[which], "S:%02X", r->sp.b.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_P: sprintf(buffer[which], "P:%02X", r->p); break;
/*TODO*///		case CPU_INFO_REG+M6502_A: sprintf(buffer[which], "A:%02X", r->a); break;
/*TODO*///		case CPU_INFO_REG+M6502_X: sprintf(buffer[which], "X:%02X", r->x); break;
/*TODO*///		case CPU_INFO_REG+M6502_Y: sprintf(buffer[which], "Y:%02X", r->y); break;
/*TODO*///		case CPU_INFO_REG+M6502_EA: sprintf(buffer[which], "EA:%04X", r->ea.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_ZP: sprintf(buffer[which], "ZP:%03X", r->zp.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6502_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+M6502_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+M6502_SO_STATE: sprintf(buffer[which], "SO:%X", r->so_state); break;
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
                return "N2A03";
            case CPU_INFO_FAMILY:
                return "Motorola 6502";
            case CPU_INFO_VERSION:
                return "1.0";
            case CPU_INFO_FILE:
                return "n2a03.java";
            case CPU_INFO_CREDITS:
                return "Copyright (c) 1998 Juergen Buchmueller, all rights reserved.";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)m6502_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)m6502_win_layout;
        }
        throw new UnsupportedOperationException("Not supported yet.");
        /*TODO*///	return buffer[which];
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
        cpu_setOPbase16.handler(pc);
    }

    /* The N2A03 is integrally tied to its PSG (they're on the same die).
   Bit 7 of address $4011 (the PSG's DPCM control register), when set,
   causes an IRQ to be generated.  This function allows the IRQ to be called
   from the PSG core when such an occasion arises. */
    public static void n2a03_irq() {
        m65c02_take_irq();
    }

    /*TODO*///
/*TODO*///
/*TODO*///void m6502_set_pc (unsigned val)
/*TODO*///{
/*TODO*///	PCW = val;
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*///unsigned m6502_get_sp (void)
/*TODO*///{
/*TODO*///	return S;
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_sp (unsigned val)
/*TODO*///{
/*TODO*///	S = val;
/*TODO*///}
/*TODO*///
/*TODO*///unsigned m6502_get_reg (int regnum)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6502_PC: return m6502.pc.w.l;
/*TODO*///		case M6502_S: return m6502.sp.b.l;
/*TODO*///		case M6502_P: return m6502.p;
/*TODO*///		case M6502_A: return m6502.a;
/*TODO*///		case M6502_X: return m6502.x;
/*TODO*///		case M6502_Y: return m6502.y;
/*TODO*///		case M6502_EA: return m6502.ea.w.l;
/*TODO*///		case M6502_ZP: return m6502.zp.w.l;
/*TODO*///		case M6502_NMI_STATE: return m6502.nmi_state;
/*TODO*///		case M6502_IRQ_STATE: return m6502.irq_state;
/*TODO*///		case M6502_SO_STATE: return m6502.so_state;
/*TODO*///		case REG_PREVIOUSPC: return m6502.ppc.w.l;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///					return RDMEM( offset ) | ( RDMEM( offset + 1 ) << 8 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_set_reg (int regnum, unsigned val)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6502_PC: m6502.pc.w.l = val; break;
/*TODO*///		case M6502_S: m6502.sp.b.l = val; break;
/*TODO*///		case M6502_P: m6502.p = val; break;
/*TODO*///		case M6502_A: m6502.a = val; break;
/*TODO*///		case M6502_X: m6502.x = val; break;
/*TODO*///		case M6502_Y: m6502.y = val; break;
/*TODO*///		case M6502_EA: m6502.ea.w.l = val; break;
/*TODO*///		case M6502_ZP: m6502.zp.w.l = val; break;
/*TODO*///		case M6502_NMI_STATE: m6502_set_nmi_line( val ); break;
/*TODO*///		case M6502_IRQ_STATE: m6502_set_irq_line( 0, val ); break;
/*TODO*///		case M6502_SO_STATE: m6502_set_irq_line( M6502_SET_OVERFLOW, val ); break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0x1ff )
/*TODO*///				{
/*TODO*///					WRMEM( offset, val & 0xfff );
/*TODO*///					WRMEM( offset + 1, (val >> 8) & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
/*TODO*///}
/*TODO*///

    /*TODO*///void m6502_state_save(void *file)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
/*TODO*///	state_save_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
/*TODO*///	state_save_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
/*TODO*///}
/*TODO*///
/*TODO*///void m6502_state_load(void *file)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
/*TODO*///	state_load_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
/*TODO*///	state_load_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
/*TODO*///}
    /* 6502 flags */
    public static final int F_C = 0x01;
    public static final int F_Z = 0x02;
    public static final int F_I = 0x04;
    public static final int F_D = 0x08;
    public static final int F_B = 0x10;
    public static final int F_T = 0x20;
    public static final int F_V = 0x40;
    public static final int F_N = 0x80;

    public static void SET_NZ(int n) {
        if ((n) == 0) {
            m6502.u8_p = ((m6502.u8_p & ~F_N) | F_Z) & 0xFF;
        } else {
            m6502.u8_p = ((m6502.u8_p & ~(F_N | F_Z)) | ((n) & F_N)) & 0xFF;
        }
    }

    public static void SET_Z(int n) {
        if ((n) == 0) {
            m6502.u8_p = (m6502.u8_p | F_Z) & 0xFF;
        } else {
            m6502.u8_p = (m6502.u8_p & ~F_Z) & 0xFF;
        }
    }

    /**
     * *************************************************************
     * RDOP	read an opcode
     * *************************************************************
     */
    public static int RDOP() {
        int tmp = cpu_readop(m6502.pc.D);
        m6502.pc.AddD(1);
        return tmp & 0xFF;
    }

    /**
     * *************************************************************
     * RDOPARG read an opcode argument
     * *************************************************************
     */
    public static int RDOPARG() {
        int tmp = cpu_readop_arg(m6502.pc.D);
        m6502.pc.AddD(1);
        return tmp & 0xFF;
    }

    /**
     * *************************************************************
     * RDMEM	read memory
     * *************************************************************
     */
    public static int RDMEM(int addr) {
        return cpu_readmem16(addr) & 0xFF;
    }

    /**
     * *************************************************************
     * WRMEM	write memory
     * *************************************************************
     */
    public static void WRMEM(int addr, int data) {
        cpu_writemem16(addr, data & 0xFF);
    }

    /**
     * *************************************************************
     * BRA branch relative extra cycle if page boundary is crossed
     * *************************************************************
     */
    public static void BRA(boolean cond) {
        if (cond) {
            int tmp = RDOPARG();
            m6502.ea.SetD(m6502.pc.D + (byte) tmp);
            m6502_ICount[0] -= (m6502.pc.H == m6502.ea.H) ? 3 : 4;
            m6502.pc.SetD(m6502.ea.D);
            change_pc16(m6502.pc.D);
        } else {
            m6502.pc.AddD(1);//PCW++;
            m6502_ICount[0] -= 2;
        }
    }

    /**
     * *************************************************************
     *
     * Helper macros to build the effective address
     *
     **************************************************************
     */
    /**
     * *************************************************************
     * EA = zero page address
     * *************************************************************
     */
    public static void EA_ZPG() {
        m6502.zp.SetL(RDOPARG());
        m6502.ea.SetD(m6502.zp.D);
    }

    /**
     * *************************************************************
     * EA = zero page address + X
     * *************************************************************
     */
    public static void EA_ZPX() {
        m6502.zp.SetL(RDOPARG() + m6502.u8_x);
        m6502.ea.SetD(m6502.zp.D);
    }

    /**
     * *************************************************************
     * EA = zero page address + Y
     * *************************************************************
     */
    public static void EA_ZPY() {
        m6502.zp.SetL(RDOPARG() + m6502.u8_y);
        m6502.ea.SetD(m6502.zp.D);
    }

    /**
     * *************************************************************
     * EA = absolute address
     * *************************************************************
     */
    public static void EA_ABS() {
        m6502.ea.SetL(RDOPARG());
        m6502.ea.SetH(RDOPARG());
    }

    /**
     * *************************************************************
     * EA = absolute address + X
     * *************************************************************
     */
    public static void EA_ABX() {
        EA_ABS();
        m6502.ea.SetD(m6502.ea.D + m6502.u8_x);
    }

    /**
     * *************************************************************
     * EA = absolute address + Y
     * *************************************************************
     */
    public static void EA_ABY() {
        EA_ABS();
        m6502.ea.SetD(m6502.ea.D + m6502.u8_y);
    }

    /**
     * *************************************************************
     * EA = zero page + X indirect (pre indexed)
     * *************************************************************
     */
    public static void EA_IDX() {
        m6502.zp.SetL(RDOPARG() + m6502.u8_x);
        m6502.ea.SetL(RDMEM(m6502.zp.D));
        m6502.zp.AddL(1);
        m6502.ea.SetH(RDMEM(m6502.zp.D));
    }

    /**
     * *************************************************************
     * EA = zero page indirect + Y (post indexed) subtract 1 cycle if page
     * boundary is crossed
     * *************************************************************
     */
    public static void EA_IDY() {
        m6502.zp.SetL(RDOPARG());
        m6502.ea.SetL(RDMEM(m6502.zp.D));
        m6502.zp.AddL(1);
        m6502.ea.SetH(RDMEM(m6502.zp.D));
        if (m6502.ea.L + m6502.u8_y > 0xff) {
            m6502_ICount[0]--;
        }
        m6502.ea.SetD(m6502.ea.D + m6502.u8_y);

    }

    /**
     * *************************************************************
     * EA = indirect (only used by JMP)
     * *************************************************************
     */
    public static void EA_IND() {
        EA_ABS();
        int tmp = RDMEM(m6502.ea.D);
        m6502.ea.AddL(1);
        m6502.ea.SetH(RDMEM(m6502.ea.D));
        m6502.ea.SetL(tmp);
    }

    /* read a value into tmp */
    public static int RD_IMM() {
        return RDOPARG();
    }

    public static int RD_ACC() {
        return m6502.u8_a & 0xFF;
    }

    public static int RD_ZPG() {
        EA_ZPG();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_ZPX() {
        EA_ZPX();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_ZPY() {
        EA_ZPY();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_ABS() {
        EA_ABS();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_ABX() {
        EA_ABX();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_ABY() {
        EA_ABY();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_IDX() {
        EA_IDX();
        return RDMEM(m6502.ea.D);
    }

    public static int RD_IDY() {
        EA_IDY();
        return RDMEM(m6502.ea.D);
    }

    /* write a value from tmp */
    public static void WR_ZPG(int tmp) {
        EA_ZPG();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_ZPX(int tmp) {
        EA_ZPX();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_ZPY(int tmp) {
        EA_ZPY();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_ABS(int tmp) {
        EA_ABS();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_ABX(int tmp) {
        EA_ABX();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_ABY(int tmp) {
        EA_ABY();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_IDX(int tmp) {
        EA_IDX();
        WRMEM(m6502.ea.D, tmp);
    }

    public static void WR_IDY(int tmp) {
        EA_IDY();
        WRMEM(m6502.ea.D, tmp);
    }

    /* write back a value from tmp to the last EA */
    public static void WB_ACC(int tmp) {
        m6502.u8_a = tmp & 0xFF;
    }

    public static void WB_EA(int tmp) {
        WRMEM(m6502.ea.D, tmp);
    }

    /**
     * *************************************************************
     ***************************************************************
     * Macros to emulate the plain 6502 opcodes
     * **************************************************************
     * *************************************************************
     */
    /**
     * *************************************************************
     * push a register onto the stack
     * *************************************************************
     */
    public static void PUSH(int Rg) {
        WRMEM(m6502.sp.D, Rg);
        m6502.sp.AddL(-1);
    }

    /**
     * *************************************************************
     * pull a register from the stack
     * *************************************************************
     */
    public static int PULL() {
        m6502.sp.AddL(1);
        return RDMEM(m6502.sp.D);
    }

    /* N2A03 *******************************************************
 *	ADC Add with carry - no decimal mode
 ***************************************************************/
    public static void ADC_NES(int tmp) {
        {
            int c = (m6502.u8_p & F_C);
            int sum = m6502.u8_a + tmp + c;
            m6502.u8_p = (m6502.u8_p & ~(F_V | F_C)) & 0xFF;
            if ((~(m6502.u8_a ^ tmp) & (m6502.u8_a ^ sum) & F_N) != 0) {
                m6502.u8_p = (m6502.u8_p | F_V) & 0xFF;
            }
            if ((sum & 0xff00) != 0) {
                m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
            }
            m6502.u8_a = sum & 0xFF;
        }
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
 *	AND Logical and
 ***************************************************************/
    public static void AND(int tmp) {
        m6502.u8_a = (m6502.u8_a & tmp) & 0xFF;
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
 *	ASL Arithmetic shift left
 ***************************************************************/
    public static int ASL(int tmp) {
        m6502.u8_p = ((m6502.u8_p & ~F_C) | ((tmp >> 7) & F_C)) & 0xFF;
        tmp = (tmp << 1) & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
     *	BCC Branch if carry clear
     ***************************************************************/
    public static void BCC() {
        BRA((m6502.u8_p & F_C) == 0);
    }

    /* 6502 ********************************************************
     *	BCS Branch if carry set
     ***************************************************************/
    public static void BCS() {
        BRA((m6502.u8_p & F_C) != 0);
    }

    /* 6502 ********************************************************
     *	BEQ Branch if equal
     ***************************************************************/
    public static void BEQ() {
        BRA((m6502.u8_p & F_Z) != 0);
    }

    /* 6502 ********************************************************
    *	BIT Bit test
    ***************************************************************/
    public static void BIT(int tmp) {
        m6502.u8_p = (m6502.u8_p & ~(F_N | F_V | F_Z)) & 0xFF;
        m6502.u8_p = (m6502.u8_p | tmp & (F_N | F_V)) & 0xFF;
        if ((tmp & m6502.u8_a) == 0) {
            m6502.u8_p = (m6502.u8_p | F_Z) & 0xFF;
        }
    }

    /* 6502 ********************************************************
    *	BMI Branch if minus
    ***************************************************************/
    public static void BMI() {
        BRA((m6502.u8_p & F_N) != 0);
    }

    /* 6502 ********************************************************
    *	BNE Branch if not equal
    ***************************************************************/
    public static void BNE() {
        BRA((m6502.u8_p & F_Z) == 0);
    }

    /* 6502 ********************************************************
    *	BPL Branch if plus
    ***************************************************************/
    public static void BPL() {
        BRA((m6502.u8_p & F_N) == 0);
    }

    /* 6502 ********************************************************
    *	BRK Break
    *	increment PC, push PC hi, PC lo, flags (with B bit set),
    *	set I flag, jump via IRQ vector
    ***************************************************************/
    public static void BRK() {
        m6502.pc.AddD(1);
        PUSH(m6502.pc.H);
        PUSH(m6502.pc.L);
        PUSH(m6502.u8_p | F_B);
        m6502.u8_p = (m6502.u8_p | F_I) & 0xFF;
        m6502.pc.SetL(RDMEM(M6502_IRQ_VEC));
        m6502.pc.SetH(RDMEM(M6502_IRQ_VEC + 1));
        change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
    * BVC	Branch if overflow clear
    ***************************************************************/
    public static void BVC() {
        BRA((m6502.u8_p & F_V) == 0);
    }

    /* 6502 ********************************************************
    * BVS	Branch if overflow set
    ***************************************************************/
    public static void BVS() {
        BRA((m6502.u8_p & F_V) != 0);
    }

    /* 6502 ********************************************************
    * CLC	Clear carry flag
    ***************************************************************/
    public static void CLC() {
        m6502.u8_p = (m6502.u8_p & ~F_C) & 0xFF;
    }

    /* 6502 ********************************************************
    * CLD	Clear decimal flag
    ***************************************************************/
    public static void CLD() {
        m6502.u8_p = (m6502.u8_p & ~F_D) & 0xFF;
    }

    /* 6502 ********************************************************
    * CLI	Clear interrupt flag
    ***************************************************************/
    public static void CLI() {
        if ((m6502.u8_irq_state != CLEAR_LINE) && (m6502.u8_p & F_I) != 0) {
            logerror("M6502#%d CLI sets after_cli\n", cpu_getactivecpu());
            m6502.u8_after_cli = 1;
        }
        m6502.u8_p = (m6502.u8_p & ~F_I) & 0xFF;
    }

    /* 6502 ********************************************************
    * CLV	Clear overflow flag
    ***************************************************************/
    public static void CLV() {
        m6502.u8_p = (m6502.u8_p & ~F_V) & 0xFF;
    }

    /* 6502 ********************************************************
    *	CMP Compare accumulator
    ***************************************************************/
    public static void CMP(int tmp) {
        m6502.u8_p = (m6502.u8_p & ~F_C) & 0xFF;
        if (m6502.u8_a >= tmp) {
            m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
        }
        SET_NZ((m6502.u8_a - tmp) & 0xFF);
    }

    /* 6502 ********************************************************
    *	CPX Compare index X
    ***************************************************************/
    public static void CPX(int tmp) {
        m6502.u8_p = (m6502.u8_p & ~F_C) & 0xFF;
        if (m6502.u8_x >= tmp) {
            m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
        }
        SET_NZ((m6502.u8_x - tmp) & 0xFF);
    }

    /* 6502 ********************************************************
    *	CPY Compare index Y
    ***************************************************************/
    public static void CPY(int tmp) {
        m6502.u8_p = (m6502.u8_p & ~F_C) & 0xFF;
        if (m6502.u8_y >= tmp) {
            m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
        }
        SET_NZ((m6502.u8_y - tmp) & 0xFF);
    }

    /* 6502 ********************************************************
    *	DEC Decrement memory
    ***************************************************************/
    public static int DEC(int tmp) {
        tmp = (tmp - 1) & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
    *	DEX Decrement index X
    ***************************************************************/
    public static void DEX() {
        m6502.u8_x = (m6502.u8_x - 1) & 0xFF;
        SET_NZ(m6502.u8_x);
    }

    /* 6502 ********************************************************
    *	DEY Decrement index Y
    ***************************************************************/
    public static void DEY() {
        m6502.u8_y = (m6502.u8_y - 1) & 0xFF;
        SET_NZ(m6502.u8_y);
    }

    /* 6502 ********************************************************
    *	EOR Logical exclusive or
    ***************************************************************/
    public static void EOR(int tmp) {
        m6502.u8_a = (m6502.u8_a ^ tmp) & 0xFF;
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
    *	ILL Illegal opcode
    ***************************************************************/
    public static void ILL() {
        logerror("M6502 illegal opcode %04x: %02x\n", (m6502.pc.D - 1) & 0xffff, cpu_readop((m6502.pc.D - 1) & 0xffff));
    }

    /* 6502 ********************************************************
    *	INC Increment memory
    ***************************************************************/
    public static int INC(int tmp) {
        tmp = (tmp + 1) & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
    *	INX Increment index X
    ***************************************************************/
    public static void INX() {
        m6502.u8_x = (m6502.u8_x + 1) & 0xFF;
        SET_NZ(m6502.u8_x);
    }

    /* 6502 ********************************************************
    *	INY Increment index Y
    ***************************************************************/
    public static void INY() {
        m6502.u8_y = (m6502.u8_y + 1) & 0xFF;
        SET_NZ(m6502.u8_y);
    }

    /* 6502 ********************************************************
    *	JMP Jump to address
    *	set PC to the effective address
    ***************************************************************/
    public static void JMP() {
        if (m6502.ea.D == m6502.ppc.D && m6502.u8_pending_irq == 0 && m6502.u8_after_cli == 0) {
            if (m6502_ICount[0] > 0) {
                m6502_ICount[0] = 0;
            }
        }
        m6502.pc.SetD(m6502.ea.D);
        change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
    *	JSR Jump to subroutine
    *	decrement PC (sic!) push PC hi, push PC lo and set
    *	PC to the effective address
    ***************************************************************/
    public static void JSR() {
        m6502.ea.SetL(RDOPARG());
        PUSH(m6502.pc.H);
        PUSH(m6502.pc.L);
        m6502.ea.SetH(RDOPARG());
        m6502.pc.SetD(m6502.ea.D);
        change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
    *	LDA Load accumulator
    ***************************************************************/
    public static void LDA(int tmp) {
        m6502.u8_a = tmp & 0xFF;
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
    *	LDX Load index X
    ***************************************************************/
    public static void LDX(int tmp) {
        m6502.u8_x = tmp & 0xFF;
        SET_NZ(m6502.u8_x);
    }

    /* 6502 ********************************************************
    *	LDY Load index Y
    ***************************************************************/
    public static void LDY(int tmp) {
        m6502.u8_y = tmp & 0xFF;
        SET_NZ(m6502.u8_y);
    }

    /* 6502 ********************************************************
     *	LSR Logic shift right
     *	0 -> [7][6][5][4][3][2][1][0] -> C
     ***************************************************************/
    public static int LSR(int tmp) {
        m6502.u8_p = ((m6502.u8_p & ~F_C) | (tmp & F_C)) & 0xFF;
        tmp = (tmp >> 1) & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
     *	NOP No operation
     ***************************************************************/
    public static void NOP() {
    }

    /* 6502 ********************************************************
     *	ORA Logical inclusive or
     ***************************************************************/
    public static void ORA(int tmp) {
        m6502.u8_a = (m6502.u8_a | tmp) & 0xFF;
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
     *	PHA Push accumulator
     ***************************************************************/
    public static void PHA() {
        PUSH(m6502.u8_a);
    }

    /* 6502 ********************************************************
     *	PHP Push processor status (flags)
     ***************************************************************/
    public static void PHP() {
        PUSH(m6502.u8_p);
    }

    /* 6502 ********************************************************
     *	PLA Pull accumulator
     ***************************************************************/
    public static void PLA() {
        m6502.u8_a = PULL();
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
    *	PLP Pull processor status (flags)
    ***************************************************************/
    public static void PLP() {
        if ((m6502.u8_p & F_I) != 0) {
            m6502.u8_p = PULL();
            if ((m6502.u8_irq_state != CLEAR_LINE) && (m6502.u8_p & F_I) == 0) {
                //LOG(("M6502#%d PLP sets after_cli\n",cpu_getactivecpu())); 
                m6502.u8_after_cli = 1;
            }
        } else {
            m6502.u8_p = PULL();
        }
        m6502.u8_p = (m6502.u8_p | (F_T | F_B)) & 0xFF;
    }

    /* 6502 ********************************************************
    * ROL	Rotate left
    *	new C <- [7][6][5][4][3][2][1][0] <- C
    ***************************************************************/
    public static int ROL(int tmp) {
        tmp = (tmp << 1) | (m6502.u8_p & F_C);
        m6502.u8_p = ((m6502.u8_p & ~F_C) | ((tmp >> 8) & F_C)) & 0xFF;
        tmp = tmp & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
    * ROR	Rotate right
    *	C -> [7][6][5][4][3][2][1][0] -> new C
    ***************************************************************/
    public static int ROR(int tmp) {
        tmp |= (m6502.u8_p & F_C) << 8;
        m6502.u8_p = ((m6502.u8_p & ~F_C) | (tmp & F_C)) & 0xFF;
        tmp = (tmp >> 1) & 0xFF;
        SET_NZ(tmp);
        return tmp;
    }

    /* 6502 ********************************************************
    * RTI	Return from interrupt
    * pull flags, pull PC lo, pull PC hi and increment PC
    *	PCW++;
    ***************************************************************/
    public static void RTI() {
        m6502.u8_p = PULL();
        m6502.pc.SetL(PULL());
        m6502.pc.SetH(PULL());
        m6502.u8_p = (m6502.u8_p | (F_T | F_B)) & 0xFF;
        if ((m6502.u8_irq_state != CLEAR_LINE) && (m6502.u8_p & F_I) == 0) {
            //LOG(("M6502#%d RTI sets after_cli\n", cpu_getactivecpu())); 
            m6502.u8_after_cli = 1;
        }
        change_pc16(m6502.pc.D);
    }

    /* 6502 ********************************************************
    *	RTS Return from subroutine
    *	pull PC lo, PC hi and increment PC
    ***************************************************************/
    public static void RTS() {
        m6502.pc.SetL(PULL());
        m6502.pc.SetH(PULL());
        m6502.pc.AddD(1);
        change_pc16(m6502.pc.D);
    }

    /* N2A03 *******************************************************
     *	SBC Subtract with carry - no decimal mode
     ***************************************************************/
    public static void SBC_NES(int tmp) {
        {
            int c = (m6502.u8_p & F_C) ^ F_C;
            int sum = m6502.u8_a - tmp - c;
            m6502.u8_p = (m6502.u8_p & ~(F_V | F_C)) & 0xFF;
            if (((m6502.u8_a ^ tmp) & (m6502.u8_a ^ sum) & F_N) != 0) {
                m6502.u8_p = (m6502.u8_p | F_V) & 0xFF;
            }
            if ((sum & 0xff00) == 0) {
                m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
            }
            m6502.u8_a = sum & 0xFF;
        }
        SET_NZ(m6502.u8_a);

    }

    /* 6502 ********************************************************
    *	SEC Set carry flag
    ***************************************************************/
    public static void SEC() {
        m6502.u8_p = (m6502.u8_p | F_C) & 0xFF;
    }

    /* 6502 ********************************************************
    *	SED Set decimal flag
    ***************************************************************/
    public static void SED() {
        m6502.u8_p = (m6502.u8_p | F_D) & 0xFF;
    }

    /* 6502 ********************************************************
    *	SEI Set interrupt flag
    ***************************************************************/
    public static void SEI() {
        m6502.u8_p = (m6502.u8_p | F_I) & 0xFF;
    }


    /* 6502 ********************************************************
     * STA	Store accumulator
     ***************************************************************/
    public static int STA() {
        return m6502.u8_a & 0xFF;
    }

    /* 6502 ********************************************************
     * STX	Store index X
     ***************************************************************/
    public static int STX() {
        return m6502.u8_x & 0xFF;
    }

    /* 6502 ********************************************************
     * STY	Store index Y
     ***************************************************************/
    public static int STY() {
        return m6502.u8_y & 0xFF;
    }

    /* 6502 ********************************************************
     * TAX	Transfer accumulator to index X
     ***************************************************************/
    public static void TAX() {
        m6502.u8_x = m6502.u8_a & 0xFF;
        SET_NZ(m6502.u8_x);
    }

    /* 6502 ********************************************************
     * TAY	Transfer accumulator to index Y
     ***************************************************************/
    public static void TAY() {
        m6502.u8_y = m6502.u8_a & 0xFF;
        SET_NZ(m6502.u8_y);
    }

    /* 6502 ********************************************************
     * TSX	Transfer stack LSB to index X
     ***************************************************************/
    public static void TSX() {
        m6502.u8_x = m6502.sp.L & 0xFF;
        SET_NZ(m6502.u8_x);
    }

    /* 6502 ********************************************************
     * TXA	Transfer index X to accumulator
     ***************************************************************/
    public static void TXA() {
        m6502.u8_a = m6502.u8_x & 0xFF;
        SET_NZ(m6502.u8_a);
    }

    /* 6502 ********************************************************
     * TXS	Transfer index X to stack LSB
     * no flags changed (sic!)
     ***************************************************************/
    public static void TXS() {
        m6502.sp.SetL(m6502.u8_x);
    }

    /* 6502 ********************************************************
     * TYA	Transfer index Y to accumulator
     ***************************************************************/
    public static void TYA() {
        m6502.u8_a = m6502.u8_y;
        SET_NZ(m6502.u8_a);
    }
    /**
     * ***************************************************************************
     *****************************************************************************
     *
     * plain vanilla 6502 opcodes
     *
     *****************************************************************************
     */
    static opcode m6502_00 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            BRK();
        }
    };
    /* 7 BRK */
    static opcode m6502_20 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            JSR();
        }
    };
    /* 6 JSR */
    static opcode m6502_40 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            RTI();
        }
    };
    /* 6 RTI */
    static opcode m6502_60 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            RTS();
        }
    };
    /* 6 RTS */
    static opcode m6502_80 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_a0 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            LDY(tmp);
        }
    };
    /* 2 LDY IMM */
    static opcode m6502_c0 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            CPY(tmp);
        }
    };
    /* 2 CPY IMM */
    static opcode m6502_e0 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            CPX(tmp);
        }
    };
    /* 2 CPX IMM */

    static opcode m6502_10 = new opcode() {
        public void handler() {
            BPL();
        }
    };
    /* 2 BPL REL */
    static opcode m6502_30 = new opcode() {
        public void handler() {
            BMI();
        }
    };
    /* 2 BMI REL */
    static opcode m6502_50 = new opcode() {
        public void handler() {
            BVC();
        }
    };
    /* 2 BVC REL */
    static opcode m6502_70 = new opcode() {
        public void handler() {
            BVS();
        }
    };
    /* 2 BVS REL */
    static opcode m6502_90 = new opcode() {
        public void handler() {
            BCC();
        }
    };
    /* 2 BCC REL */
    static opcode m6502_b0 = new opcode() {
        public void handler() {
            BCS();
        }
    };
    /* 2 BCS REL */
    static opcode m6502_d0 = new opcode() {
        public void handler() {
            BNE();
        }
    };
    /* 2 BNE REL */
    static opcode m6502_f0 = new opcode() {
        public void handler() {
            BEQ();
        }
    };
    /* 2 BEQ REL */

    static opcode m6502_01 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            ORA(tmp);
        }
    };
    /* 6 ORA IDX */
    static opcode m6502_21 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            AND(tmp);
        }
    };
    /* 6 AND IDX */
    static opcode m6502_41 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            EOR(tmp);
        }
    };
    /* 6 EOR IDX */
    static opcode m6502_61 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            ADC_NES(tmp);
        }
    };
    /* 6 ADC IDX */
    static opcode m6502_81 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = STA();
            WR_IDX(tmp);
        }
    };
    /* 6 STA IDX */
    static opcode m6502_a1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            LDA(tmp);
        }
    };
    /* 6 LDA IDX */
    static opcode m6502_c1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            CMP(tmp);
        }
    };
    /* 6 CMP IDX */
    static opcode m6502_e1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_IDX();
            SBC_NES(tmp);
        }
    };
    /* 6 SBC IDX */

    static opcode m6502_11 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            ORA(tmp);
        }
    };
    /* 5 ORA IDY */
    static opcode m6502_31 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            AND(tmp);
        }
    };
    /* 5 AND IDY */
    static opcode m6502_51 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            EOR(tmp);
        }
    };
    /* 5 EOR IDY */
    static opcode m6502_71 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            ADC_NES(tmp);
        }
    };
    /* 5 ADC IDY */
    static opcode m6502_91 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = STA();
            WR_IDY(tmp);
        }
    };
    /* 6 STA IDY */
    static opcode m6502_b1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            LDA(tmp);
        }
    };
    /* 5 LDA IDY */
    static opcode m6502_d1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            CMP(tmp);
        }
    };
    /* 5 CMP IDY */
    static opcode m6502_f1 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_IDY();
            SBC_NES(tmp);
        }
    };
    /* 5 SBC IDY */

    static opcode m6502_02 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_22 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_42 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_62 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_82 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_a2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            LDX(tmp);
        }
    };
    /* 2 LDX IMM */
    static opcode m6502_c2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_e2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_12 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_32 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_52 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_72 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_92 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_b2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_d2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_f2 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_03 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_23 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_43 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_63 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_83 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_a3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_c3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_e3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_13 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_33 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_53 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_73 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_93 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_b3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_d3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_f3 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_04 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_24 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            BIT(tmp);
        }
    };
    /* 3 BIT ZPG */
    static opcode m6502_44 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_64 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_84 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = STY();
            WR_ZPG(tmp);
        }
    };
    /* 3 STY ZPG */
    static opcode m6502_a4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            LDY(tmp);
        }
    };
    /* 3 LDY ZPG */
    static opcode m6502_c4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            CPY(tmp);
        }
    };
    /* 3 CPY ZPG */
    static opcode m6502_e4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            CPX(tmp);
        }
    };
    /* 3 CPX ZPG */

    static opcode m6502_14 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_34 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_54 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_74 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_94 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = STY();
            WR_ZPX(tmp);
        }
    };
    /* 4 STY ZPX */
    static opcode m6502_b4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            LDY(tmp);
        }
    };
    /* 4 LDY ZPX */
    static opcode m6502_d4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_f4 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_05 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            ORA(tmp);
        }
    };
    /* 3 ORA ZPG */
    static opcode m6502_25 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            AND(tmp);
        }
    };
    /* 3 AND ZPG */
    static opcode m6502_45 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            EOR(tmp);
        }
    };
    /* 3 EOR ZPG */
    static opcode m6502_65 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            ADC_NES(tmp);
        }
    };
    /* 3 ADC ZPG */
    static opcode m6502_85 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = STA();
            WR_ZPG(tmp);
        }
    };
    /* 3 STA ZPG */
    static opcode m6502_a5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            LDA(tmp);
        }
    };
    /* 3 LDA ZPG */
    static opcode m6502_c5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            CMP(tmp);
        }
    };
    /* 3 CMP ZPG */
    static opcode m6502_e5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            SBC_NES(tmp);
        }
    };
    /* 3 SBC ZPG */

    static opcode m6502_15 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            ORA(tmp);
        }
    };
    /* 4 ORA ZPX */
    static opcode m6502_35 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            AND(tmp);
        }
    };
    /* 4 AND ZPX */
    static opcode m6502_55 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            EOR(tmp);
        }
    };
    /* 4 EOR ZPX */
    static opcode m6502_75 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            ADC_NES(tmp);
        }
    };
    /* 4 ADC ZPX */
    static opcode m6502_95 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = STA();
            WR_ZPX(tmp);
        }
    };
    /* 4 STA ZPX */
    static opcode m6502_b5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            LDA(tmp);
        }
    };
    /* 4 LDA ZPX */
    static opcode m6502_d5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            CMP(tmp);
        }
    };
    /* 4 CMP ZPX */
    static opcode m6502_f5 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPX();
            SBC_NES(tmp);
        }
    };
    /* 4 SBC ZPX */

    static opcode m6502_06 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = ASL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 ASL ZPG */
    static opcode m6502_26 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = ROL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 ROL ZPG */
    static opcode m6502_46 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = LSR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 int tmp2=LSR(tmp); ZPG */
    static opcode m6502_66 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = ROR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 int tmp2=ROR(tmp); ZPG */
    static opcode m6502_86 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = STX();
            WR_ZPG(tmp);
        }
    };
    /* 3 STX ZPG */
    static opcode m6502_a6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            int tmp = RD_ZPG();
            LDX(tmp);
        }
    };
    /* 3 LDX ZPG */
    static opcode m6502_c6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = DEC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 DEC ZPG */
    static opcode m6502_e6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = RD_ZPG();
            int tmp2 = INC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 5 INC ZPG */

    static opcode m6502_16 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = ASL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 ASL ZPX */
    static opcode m6502_36 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = ROL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 ROL ZPX */
    static opcode m6502_56 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = LSR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 int tmp2=LSR(tmp); ZPX */
    static opcode m6502_76 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = ROR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 int tmp2=ROR(tmp); ZPX */
    static opcode m6502_96 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = STX();
            WR_ZPY(tmp);
        }
    };
    /* 4 STX ZPY */
    static opcode m6502_b6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ZPY();
            LDX(tmp);
        }
    };
    /* 4 LDX ZPY */
    static opcode m6502_d6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = DEC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 DEC ZPX */
    static opcode m6502_f6 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ZPX();
            int tmp2 = INC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 INC ZPX */

    static opcode m6502_07 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_27 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_47 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_67 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_87 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_a7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_c7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_e7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_17 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_37 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_57 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_77 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_97 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_b7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_d7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_f7 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_08 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            PHP();
        }
    };
    /* 2 PHP */
    static opcode m6502_28 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            PLP();
        }
    };
    /* 2 PLP */
    static opcode m6502_48 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            PHA();
        }
    };
    /* 2 PHA */
    static opcode m6502_68 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            PLA();
        }
    };
    /* 2 PLA */
    static opcode m6502_88 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            DEY();
        }
    };
    /* 2 DEY */
    static opcode m6502_a8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TAY();
        }
    };
    /* 2 TAY */
    static opcode m6502_c8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            INY();
        }
    };
    /* 2 INY */
    static opcode m6502_e8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            INX();
        }
    };
    /* 2 INX */

    static opcode m6502_18 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            CLC();
        }
    };
    /* 2 CLC */
    static opcode m6502_38 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            SEC();
        }
    };
    /* 2 SEC */
    static opcode m6502_58 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            CLI();
        }
    };
    /* 2 CLI */
    static opcode m6502_78 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            SEI();
        }
    };
    /* 2 SEI */
    static opcode m6502_98 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TYA();
        }
    };
    /* 2 TYA */
    static opcode m6502_b8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            CLV();
        }
    };
    /* 2 CLV */
    static opcode m6502_d8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            CLD();
        }
    };
    /* 2 CLD */
    static opcode m6502_f8 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            SED();
        }
    };
    /* 2 SED */

    static opcode m6502_09 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            ORA(tmp);
        }
    };
    /* 2 ORA IMM */
    static opcode m6502_29 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            AND(tmp);
        }
    };
    /* 2 AND IMM */
    static opcode m6502_49 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            EOR(tmp);
        }
    };
    /* 2 EOR IMM */
    static opcode m6502_69 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            ADC_NES(tmp);
        }
    };
    /* 2 ADC IMM */
    static opcode m6502_89 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_a9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            LDA(tmp);
        }
    };
    /* 2 LDA IMM */
    static opcode m6502_c9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            CMP(tmp);
        }
    };
    /* 2 CMP IMM */
    static opcode m6502_e9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_IMM();
            SBC_NES(tmp);
        }
    };
    /* 2 SBC IMM */

    static opcode m6502_19 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            ORA(tmp);
        }
    };
    /* 4 ORA ABY */
    static opcode m6502_39 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            AND(tmp);
        }
    };
    /* 4 AND ABY */
    static opcode m6502_59 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            EOR(tmp);
        }
    };
    /* 4 EOR ABY */
    static opcode m6502_79 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            ADC_NES(tmp);
        }
    };
    /* 4 ADC ABY */
    static opcode m6502_99 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = STA();
            WR_ABY(tmp);
        }
    };
    /* 5 STA ABY */
    static opcode m6502_b9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            LDA(tmp);
        }
    };
    /* 4 LDA ABY */
    static opcode m6502_d9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            CMP(tmp);
        }
    };
    /* 4 CMP ABY */
    static opcode m6502_f9 = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            SBC_NES(tmp);
        }
    };
    /* 4 SBC ABY */

    static opcode m6502_0a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_ACC();
            int tmp2 = ASL(tmp);
            WB_ACC(tmp2);
        }
    };
    /* 2 ASL A */
    static opcode m6502_2a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_ACC();
            int tmp2 = ROL(tmp);
            WB_ACC(tmp2);
        }
    };
    /* 2 ROL A */
    static opcode m6502_4a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_ACC();
            int tmp2 = LSR(tmp);
            WB_ACC(tmp2);
        }
    };
    /* 2 int tmp2=LSR(tmp); A */
    static opcode m6502_6a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            int tmp = RD_ACC();
            int tmp2 = ROR(tmp);
            WB_ACC(tmp2);
        }
    };
    /* 2 int tmp2=ROR(tmp); A */
    static opcode m6502_8a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TXA();
        }
    };
    /* 2 TXA */
    static opcode m6502_aa = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TAX();
        }
    };
    /* 2 TAX */
    static opcode m6502_ca = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            DEX();
        }
    };
    /* 2 DEX */
    static opcode m6502_ea = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            NOP();
        }
    };
    /* 2 NOP */

    static opcode m6502_1a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_3a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_5a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_7a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_9a = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TXS();
        }
    };
    /* 2 TXS */
    static opcode m6502_ba = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            TSX();
        }
    };
    /* 2 TSX */
    static opcode m6502_da = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_fa = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_0b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_2b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_4b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_6b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_8b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_ab = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_cb = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_eb = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_1b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_3b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_5b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_7b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_9b = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_bb = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_db = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_fb = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_0c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_2c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            BIT(tmp);
        }
    };
    /* 4 BIT ABS */
    static opcode m6502_4c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 3;
            EA_ABS();
            JMP();
        }
    };
    /* 3 JMP ABS */
    static opcode m6502_6c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            EA_IND();
            JMP();
        }
    };
    /* 5 JMP IND */
    static opcode m6502_8c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = STY();
            WR_ABS(tmp);
        }
    };
    /* 4 STY ABS */
    static opcode m6502_ac = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            LDY(tmp);
        }
    };
    /* 4 LDY ABS */
    static opcode m6502_cc = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            CPY(tmp);
        }
    };
    /* 4 CPY ABS */
    static opcode m6502_ec = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            CPX(tmp);
        }
    };
    /* 4 CPX ABS */

    static opcode m6502_1c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_3c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_5c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_7c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_9c = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_bc = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            LDY(tmp);
        }
    };
    /* 4 LDY ABX */
    static opcode m6502_dc = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_fc = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_0d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            ORA(tmp);
        }
    };
    /* 4 ORA ABS */
    static opcode m6502_2d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            AND(tmp);
        }
    };
    /* 4 AND ABS */
    static opcode m6502_4d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            EOR(tmp);
        }
    };
    /* 4 EOR ABS */
    static opcode m6502_6d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            ADC_NES(tmp);
        }
    };
    /* 4 ADC ABS */
    static opcode m6502_8d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = STA();
            WR_ABS(tmp);
        }
    };
    /* 4 STA ABS */
    static opcode m6502_ad = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            LDA(tmp);
        }
    };
    /* 4 LDA ABS */
    static opcode m6502_cd = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            CMP(tmp);
        }
    };
    /* 4 CMP ABS */
    static opcode m6502_ed = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            SBC_NES(tmp);
        }
    };
    /* 4 SBC ABS */

    static opcode m6502_1d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            ORA(tmp);
        }
    };
    /* 4 ORA ABX */
    static opcode m6502_3d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            AND(tmp);
        }
    };
    /* 4 AND ABX */
    static opcode m6502_5d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            EOR(tmp);
        }
    };
    /* 4 EOR ABX */
    static opcode m6502_7d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            ADC_NES(tmp);
        }
    };
    /* 4 ADC ABX */
    static opcode m6502_9d = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = STA();
            WR_ABX(tmp);
        }
    };
    /* 5 STA ABX */
    static opcode m6502_bd = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            LDA(tmp);
        }
    };
    /* 4 LDA ABX */
    static opcode m6502_dd = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            CMP(tmp);
        }
    };
    /* 4 CMP ABX */
    static opcode m6502_fd = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABX();
            SBC_NES(tmp);
        }
    };
    /* 4 SBC ABX */

    static opcode m6502_0e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = ASL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 ASL ABS */
    static opcode m6502_2e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = ROL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 ROL ABS */
    static opcode m6502_4e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = LSR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 int tmp2=LSR(tmp); ABS */
    static opcode m6502_6e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = ROR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 int tmp2=ROR(tmp); ABS */
    static opcode m6502_8e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 5;
            int tmp = STX();
            WR_ABS(tmp);
        }
    };
    /* 5 STX ABS */
    static opcode m6502_ae = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABS();
            LDX(tmp);
        }
    };
    /* 4 LDX ABS */
    static opcode m6502_ce = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = DEC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 DEC ABS */
    static opcode m6502_ee = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 6;
            int tmp = RD_ABS();
            int tmp2 = INC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 6 INC ABS */

    static opcode m6502_1e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = ASL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 ASL ABX */
    static opcode m6502_3e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = ROL(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 ROL ABX */
    static opcode m6502_5e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = LSR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 int tmp2=LSR(tmp); ABX */
    static opcode m6502_7e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = ROR(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 int tmp2=ROR(tmp); ABX */
    static opcode m6502_9e = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_be = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 4;
            int tmp = RD_ABY();
            LDX(tmp);
        }
    };
    /* 4 LDX ABY */
    static opcode m6502_de = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = DEC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 DEC ABX */
    static opcode m6502_fe = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 7;
            int tmp = RD_ABX();
            int tmp2 = INC(tmp);
            WB_EA(tmp2);
        }
    };
    /* 7 INC ABX */

    static opcode m6502_0f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_2f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_4f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_6f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_8f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_af = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_cf = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_ef = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

    static opcode m6502_1f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_3f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_5f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_7f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_9f = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_bf = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_df = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */
    static opcode m6502_ff = new opcode() {
        public void handler() {
            m6502_ICount[0] -= 2;
            ILL();
        }
    };
    /* 2 ILL */

 /* and here's the array of function pointers */
    static opcode[] insn6502 = {
        m6502_00, m6502_01, m6502_02, m6502_03, m6502_04, m6502_05, m6502_06, m6502_07,
        m6502_08, m6502_09, m6502_0a, m6502_0b, m6502_0c, m6502_0d, m6502_0e, m6502_0f,
        m6502_10, m6502_11, m6502_12, m6502_13, m6502_14, m6502_15, m6502_16, m6502_17,
        m6502_18, m6502_19, m6502_1a, m6502_1b, m6502_1c, m6502_1d, m6502_1e, m6502_1f,
        m6502_20, m6502_21, m6502_22, m6502_23, m6502_24, m6502_25, m6502_26, m6502_27,
        m6502_28, m6502_29, m6502_2a, m6502_2b, m6502_2c, m6502_2d, m6502_2e, m6502_2f,
        m6502_30, m6502_31, m6502_32, m6502_33, m6502_34, m6502_35, m6502_36, m6502_37,
        m6502_38, m6502_39, m6502_3a, m6502_3b, m6502_3c, m6502_3d, m6502_3e, m6502_3f,
        m6502_40, m6502_41, m6502_42, m6502_43, m6502_44, m6502_45, m6502_46, m6502_47,
        m6502_48, m6502_49, m6502_4a, m6502_4b, m6502_4c, m6502_4d, m6502_4e, m6502_4f,
        m6502_50, m6502_51, m6502_52, m6502_53, m6502_54, m6502_55, m6502_56, m6502_57,
        m6502_58, m6502_59, m6502_5a, m6502_5b, m6502_5c, m6502_5d, m6502_5e, m6502_5f,
        m6502_60, m6502_61, m6502_62, m6502_63, m6502_64, m6502_65, m6502_66, m6502_67,
        m6502_68, m6502_69, m6502_6a, m6502_6b, m6502_6c, m6502_6d, m6502_6e, m6502_6f,
        m6502_70, m6502_71, m6502_72, m6502_73, m6502_74, m6502_75, m6502_76, m6502_77,
        m6502_78, m6502_79, m6502_7a, m6502_7b, m6502_7c, m6502_7d, m6502_7e, m6502_7f,
        m6502_80, m6502_81, m6502_82, m6502_83, m6502_84, m6502_85, m6502_86, m6502_87,
        m6502_88, m6502_89, m6502_8a, m6502_8b, m6502_8c, m6502_8d, m6502_8e, m6502_8f,
        m6502_90, m6502_91, m6502_92, m6502_93, m6502_94, m6502_95, m6502_96, m6502_97,
        m6502_98, m6502_99, m6502_9a, m6502_9b, m6502_9c, m6502_9d, m6502_9e, m6502_9f,
        m6502_a0, m6502_a1, m6502_a2, m6502_a3, m6502_a4, m6502_a5, m6502_a6, m6502_a7,
        m6502_a8, m6502_a9, m6502_aa, m6502_ab, m6502_ac, m6502_ad, m6502_ae, m6502_af,
        m6502_b0, m6502_b1, m6502_b2, m6502_b3, m6502_b4, m6502_b5, m6502_b6, m6502_b7,
        m6502_b8, m6502_b9, m6502_ba, m6502_bb, m6502_bc, m6502_bd, m6502_be, m6502_bf,
        m6502_c0, m6502_c1, m6502_c2, m6502_c3, m6502_c4, m6502_c5, m6502_c6, m6502_c7,
        m6502_c8, m6502_c9, m6502_ca, m6502_cb, m6502_cc, m6502_cd, m6502_ce, m6502_cf,
        m6502_d0, m6502_d1, m6502_d2, m6502_d3, m6502_d4, m6502_d5, m6502_d6, m6502_d7,
        m6502_d8, m6502_d9, m6502_da, m6502_db, m6502_dc, m6502_dd, m6502_de, m6502_df,
        m6502_e0, m6502_e1, m6502_e2, m6502_e3, m6502_e4, m6502_e5, m6502_e6, m6502_e7,
        m6502_e8, m6502_e9, m6502_ea, m6502_eb, m6502_ec, m6502_ed, m6502_ee, m6502_ef,
        m6502_f0, m6502_f1, m6502_f2, m6502_f3, m6502_f4, m6502_f5, m6502_f6, m6502_f7,
        m6502_f8, m6502_f9, m6502_fa, m6502_fb, m6502_fc, m6502_fd, m6502_fe, m6502_ff
    };

    public abstract interface opcode {

        public abstract void handler();
    }

}
