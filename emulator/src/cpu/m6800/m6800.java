package cpu.m6800;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m6800.m6800H.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;

public class m6800 extends cpu_interface {

    public static FILE m6800log = null;//fopen("m6800.log", "wa");  //for debug purposes

    public static int[] m6800_ICount = new int[1];

    public m6800() {
        cpu_num = CPU_M6800;
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

    public static class PAIR {
        //L = low 8 bits
        //H = high 8 bits
        //D = whole 16 bits

        public int H, L, D;

        public void SetH(int val) {
            H = val;
            D = (H << 8) | L;
        }

        public void SetL(int val) {
            L = val;
            D = (H << 8) | L;
        }

        public void SetD(int val) {
            D = val;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }

        public void AddH(int val) {
            H = (H + val) & 0xFF;
            D = (H << 8) | L;
        }

        public void AddL(int val) {
            L = (L + val) & 0xFF;
            D = (H << 8) | L;
        }

        public void AddD(int val) {
            D = (D + val) & 0xFFFF;
            H = D >> 8 & 0xFF;
            L = D & 0xFF;
        }
    };

    public static class PAIRD {
        //L = low 16 bits
        //H = high 16 bits
        //D = whole 32 bits

        public long H, L, D;

        public void SetH(long val) {
            H = val;
            D = (H << 16) | L;
        }

        public void SetL(long val) {
            L = val;
            D = (H << 16) | L;
        }
        public void SetLL(long val)//insure recheck
        {
            L = ((L << 8)&0xFF) | (val&0xFF);
            D = (H << 16) | L;
        }
        public void SetLH(long val)//insure recheck
        {
            L = ((val << 8)&0xFF) | (L&0xFF);
            D = (H << 16) | L;
        }

        public void SetD(long val) {
            D = val;
            H = D >> 16 & 0xFFFF;
            L = D & 0xFFFF;
        }

        public void AddH(long val) {
            H = (H + val) & 0xFFFF;
            D = (H << 16) | L;
        }

        public void AddL(long val) {
            L = (L + val) & 0xFFFF;
            D = (H << 16) | L;
        }

        public void AddD(long val) {
            D = (D + val) & 0xFFFFFFFFL;
            H = D >> 16 & 0xFFFF;
            L = D & 0xFFFF;
        }
    };
    /* 6800 Registers */

    public static class m6800_Regs {

        //	int 	subtype;		/* CPU subtype */
        public /*PAIR*/ int ppc;			/* Previous program counter */

        public /*PAIR*/ int pc; 			/* Program counter */

        public int /*PAIR*/ s;				/* Stack pointer */

        public int/*PAIR*/ x;				/* Index register */

        public int a;
        public int b;//public PAIR d;				/* Accumulators */

        public int /*UINT8*/ cc; 			/* Condition codes */

        public int /*UINT8*/ wai_state;		/* WAI opcode state ,(or sleep opcode state) */

        public int /*UINT8*/ nmi_state;		/* NMI line state */

        public int[] /*UINT8*/ irq_state = new int[2];	/* IRQ line state [IRQ1,TIN] */

        public int /*UINT8*/ ic_eddge;		/* InputCapture eddge , b.0=fall,b.1=raise */

        public irqcallbacksPtr irq_callback;
        int extra_cycles;	/* cycles used for interrupts */

        public opcode[] insn;	/* instruction table */
        /*const UINT8*/ public int[] cycles;			/* clock cycle of instruction table */
        /* internal registers */

        public int /*UINT8*/ port1_ddr;
        public int /*UINT8*/ port2_ddr;
        public int /*UINT8*/ port1_data;
        public int /*UINT8*/ port2_data;
        public int /*UINT8*/ tcsr;			/* Timer Control and Status Register */

        public int /*UINT8*/ pending_tcsr;	/* pending IRQ flag for clear IRQflag process */

        public int /*UINT8*/ irq2;			/* IRQ2 flags */

        public int /*UINT8*/ ram_ctrl;
        public PAIRD counter=new PAIRD();		/* free running counter */

        public PAIRD output_compare=new PAIRD();	/* output compare       */

        public int /*UINT16*/ input_capture;	/* input capture        */

        public PAIRD timer_over=new PAIRD();
    }
    public static m6800_Regs m6800 = new m6800_Regs();
    /* point of next timer event */
    static /*UINT32*/ long timer_next;
    public int ea;
    static int cycles_6800[]
            = {
                /* 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F */
                /*0*/0, 2, 0, 0, 0, 0, 2, 2, 4, 4, 2, 2, 2, 2, 2, 2,
                /*1*/ 2, 2, 0, 0, 0, 0, 2, 2, 0, 2, 0, 2, 0, 0, 0, 0,
                /*2*/ 4, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                /*3*/ 4, 4, 4, 4, 4, 4, 4, 4, 0, 5, 0, 10, 0, 0, 9, 12,
                /*4*/ 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
                /*5*/ 2, 0, 0, 2, 2, 0, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2,
                /*6*/ 7, 0, 0, 7, 7, 0, 7, 7, 7, 7, 7, 0, 7, 7, 4, 7,
                /*7*/ 6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 3, 6,
                /*8*/ 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 3, 8, 3, 0,
                /*9*/ 3, 3, 3, 0, 3, 3, 3, 4, 3, 3, 3, 3, 4, 0, 4, 5,
                /*A*/ 5, 5, 5, 0, 5, 5, 5, 6, 5, 5, 5, 5, 6, 8, 6, 7,
                /*B*/ 4, 4, 4, 0, 4, 4, 4, 5, 4, 4, 4, 4, 5, 9, 5, 6,
                /*C*/ 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 0, 3, 0,
                /*D*/ 3, 3, 3, 0, 3, 3, 3, 4, 3, 3, 3, 3, 0, 0, 4, 5,
                /*E*/ 5, 5, 5, 0, 5, 5, 5, 6, 5, 5, 5, 5, 0, 0, 6, 7,
                /*F*/ 4, 4, 4, 0, 4, 4, 4, 5, 4, 4, 4, 4, 0, 0, 5, 6
            };
    /* CC masks                       HI NZVC
     7654 3210	*/

    public void CLR_HNZVC() {
        m6800.cc &= 0xd0;
    }

    public void CLR_NZV() {
        m6800.cc &= 0xf1;
    }

    public void CLR_HNZC() {
        m6800.cc &= 0xd2;
    }

    public void CLR_NZVC() {
        m6800.cc &= 0xf0;
    }

    public void CLR_Z() {
        m6800.cc &= 0xfb;
    }

    public void CLR_NZC() {
        m6800.cc &= 0xf2;
    }

    public void CLR_ZC() {
        m6800.cc &= 0xfa;
    }

    public void CLR_C() {
        m6800.cc &= 0xfe;
    }

    int flags8i[]
            = /* increment */ {
                0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x0a, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08
            };
    int flags8d[]
            = /* decrement */ {
                0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
                0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08
            };

    public void SET_FLAGS8I(int a) {
        m6800.cc |= flags8i[(a) & 0xff];
    }

    public void SET_FLAGS8D(int a) {
        m6800.cc |= flags8d[(a) & 0xff];
    }

    /* combos */
    public void SET_NZ8(int a) {
        SET_N8(a);
        SET_Z(a);
    }

    public void SET_NZ16(int a) {
        SET_N16(a);
        SET_Z(a);
    }

    public void SET_FLAGS8(int a, int b, int r) {
        SET_N8(r);
        SET_Z8(r);
        SET_V8(a, b, r);
        SET_C8(r);
    }

    public void SET_FLAGS16(int a, int b, int r) {
        SET_N16(r);
        SET_Z16(r);
        SET_V16(a, b, r);
        SET_C16(r);
    }
    /* macros for CC -- CC bits affected should be reset before calling */

    public void SET_Z(int a) {
        if (a == 0) {
            SEZ();
        }
    }

    public void SET_Z8(int a) {
        SET_Z(a & 0xFF);
    }

    public void SET_Z16(int a) {
        SET_Z(a & 0xFFFF);
    }

    public void SET_N8(int a) {
        m6800.cc |= ((a & 0x80) >> 4);
    }

    public void SET_N16(int a) {
        m6800.cc |= ((a & 0x8000) >> 12);
    }

    public void SET_H(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r) & 0x10) << 1);
    }

    public void SET_C8(int a) {
        m6800.cc |= ((a & 0x100) >> 8);
    }

    public void SET_C16(int a) {
        m6800.cc |= ((a & 0x10000) >> 16);
    }

    public void SET_V8(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x80) >> 6);
    }

    public void SET_V16(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x8000) >> 14);
    }

    public static int RM(int addr) {
        return (cpu_readmem16(addr) & 0xFF);
    }

    public static void WM(int addr, int value) {
        cpu_writemem16(addr, value);
    }

    public char ROP(int addr) {
        return cpu_readop(addr);
    }

    public char ROP_ARG(int addr) {
        return cpu_readop_arg(addr);
    }

    static int RM16(int addr) {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i;
    }

    static void WM16(int addr, int reg) {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }

    public char M_RDOP(int addr) {
        return cpu_readop(addr);
    }

    public char M_RDOP_ARG(int addr) {
        return cpu_readop_arg(addr);
    }

    public int IMMBYTE() {
        int reg = M_RDOP_ARG(m6800.pc);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
    }

    public int IMMWORD() {
        int reg = (M_RDOP_ARG(m6800.pc) << 8) | M_RDOP_ARG((m6800.pc + 1) & 0xffff);
        m6800.pc = m6800.pc + 2 & 0xFFFF;
        return reg;
    }

    public static void PUSHBYTE(int w)//WM(SD,b); --S
    {
        WM(m6800.s, w);
        m6800.s = m6800.s - 1 & 0xFFFF;

    }

    public static void PUSHWORD(int w)//WM(SD,w.b.l); --S; WM(SD,w.b.h); --S
    {
        WM(m6800.s, w & 0xFF);
        m6800.s = m6800.s - 1 & 0xFFFF;
        WM(m6800.s, w >> 8);
        m6800.s = m6800.s - 1 & 0xFFFF;
    }
    public static int PULLBYTE()//S++; b = RM(SD)
    {
        m6800.s = m6800.s +1 & 0xFFFF;
        int b = RM(m6800.s);      
        return b;
    }
    public static int PULLWORD()//S++; w.d = RM(SD)<<8; S++; w.d |= RM(SD)
    {
        m6800.s = m6800.s +1 & 0xFFFF; 
        int w = RM(m6800.s)<<8;
        m6800.s = m6800.s +1 & 0xFFFF; 
        w |= RM(m6800.s);
       
        return w;
    }

    public static void CHANGE_PC() {
        change_pc16(m6800.pc & 0xFFFF);//ensure it's 16bit just in case
    }
    /* macros to set status flags */

    public void SEC() {
        m6800.cc |= 0x01l;
    }

    public void CLC() {
        m6800.cc &= 0xfe;
    }

    public void SEZ() {
        m6800.cc |= 0x04;
    }

    public void CLZ() {
        m6800.cc &= 0xfb;
    }

    public void SEN() {
        m6800.cc |= 0x08;
    }

    public void CLN() {
        m6800.cc &= 0xf7;
    }

    public void SEV() {
        m6800.cc |= 0x02;
    }

    public void CLV() {
        m6800.cc &= 0xfd;
    }

    public void SEH() {
        m6800.cc |= 0x20;
    }

    public void CLH() {
        m6800.cc &= 0xdf;
    }

    public static void SEI() {
        m6800.cc |= 0x10;
    }

    public void CLI() {
        m6800.cc &= ~0x10;
    }

    public void DIRECT() {
        ea = IMMBYTE();
    }

    public void EXTENDED() {
        ea = IMMWORD();
    }

    public void INDEXED() {
        ea = m6800.x + (M_RDOP_ARG(m6800.pc) & 0xFF);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
    }

    public int DIRBYTE() {
        DIRECT();
        return RM(ea);
    }

    public int DIRWORD() {
        DIRECT();
        return RM16(ea);
    }

    public int EXTBYTE() {
        EXTENDED();
        return RM(ea);
    }

    public int EXTWORD() {
        EXTENDED();
        return RM16(ea);
    }

    public int IDXBYTE() {
        INDEXED();
        return RM(ea);
    }

    public int IDXWORD() {
        INDEXED();
        return RM16(ea);
    }

    int getDreg()//compose dreg
    {
        return m6800.a << 8 | m6800.b;
    }

    void setDreg(int reg) //write to dreg
    {
        m6800.a = reg >> 8 & 0xFF;
        m6800.b = reg & 0xFF;
    }
    public void BRANCH(boolean f)
    {
        int t= IMMBYTE();
        if(f)
        {
            m6800.pc=(m6800.pc+(byte)t) & 0xFFFF;//TODO check if it has to be better...
            CHANGE_PC();
        }
    }
    public int NXORV()  { return ((m6800.cc&0x08)^((m6800.cc&0x02)<<2)); }
    /* mnemonicos for the Timer Control and Status Register bits */
    public static final int TCSR_OLVL = 0x01;
    public static final int TCSR_IEDG = 0x02;
    public static final int TCSR_ETOI = 0x04;
    public static final int TCSR_EOCI = 0x08;
    public static final int TCSR_EICI = 0x10;
    public static final int TCSR_TOF = 0x20;
    public static final int TCSR_OCF = 0x40;
    public static final int TCSR_ICF = 0x80;

    public static void MODIFIED_tcsr() {
        m6800.irq2 = (m6800.tcsr & (m6800.tcsr << 3)) & (TCSR_ICF | TCSR_OCF | TCSR_TOF);
    }

    public static void SET_TIMRE_EVENT() {
        timer_next = (m6800.output_compare.D < m6800.timer_over.D) ? m6800.output_compare.D : m6800.timer_over.D;
    }
    public static void MODIFIED_counters() {						
	m6800.output_compare.SetH((m6800.output_compare.L >= m6800.counter.L) ? m6800.counter.H : m6800.counter.H+1);				
	SET_TIMRE_EVENT();							
    }
    /* cleanup high-word of counters */

    public void CLEANUP_conters() {
        m6800.output_compare.SetH(m6800.output_compare.H - m6800.counter.H);//OCH -= CTH;
        m6800.timer_over.SetL(m6800.timer_over.H - m6800.counter.H);//TOH -= CTH;
        m6800.counter.SetH(0);//CTH = 0;								
        SET_TIMRE_EVENT();
    }

    public void INCREMENT_COUNTER(int amount) {
        m6800_ICount[0] -= amount;
        m6800.timer_over.SetD(m6800.timer_over.D + amount);//CTD += amount;					
        if (m6800.timer_over.D >= timer_next) {
            check_timer_event();
        }
    }

    public void EAT_CYCLES() {
        int cycles_to_eat;

        cycles_to_eat = (int) (timer_next - m6800.counter.D);
        if (cycles_to_eat > m6800_ICount[0]) {
            cycles_to_eat = m6800_ICount[0];
        }
        if (cycles_to_eat > 0) {
            INCREMENT_COUNTER(cycles_to_eat);
        }
    }
    /* check OCI or TOI */

    public void check_timer_event() {
        /* OCI */
        if (m6800.timer_over.D >= m6800.output_compare.D) {
            m6800.output_compare.AddH(1);//OCH++;	// next IRQ point
            m6800.tcsr |= TCSR_OCF;
            m6800.pending_tcsr |= TCSR_OCF;
            MODIFIED_tcsr();
            if ((m6800.cc & 0x10) == 0 && (m6800.tcsr & TCSR_EOCI) != 0) {
                TAKE_OCI();
            }
        }
        /* TOI */
        if (m6800.counter.D >= m6800.timer_over.D) {
            m6800.timer_over.AddL(1);	// next IRQ point

            m6800.tcsr |= TCSR_TOF;
            m6800.pending_tcsr |= TCSR_TOF;
            MODIFIED_tcsr();
            if ((m6800.cc & 0x10) == 0 && (m6800.tcsr & TCSR_ETOI) != 0) {
                TAKE_TOI();
            }
        }
        /* set next event */
        SET_TIMRE_EVENT();
    }
    /* take interrupt */

    public static void TAKE_ICI() {
        ENTER_INTERRUPT("M6800#%d take ICI\n", 0xfff6);
    }

    public static void TAKE_OCI() {
        ENTER_INTERRUPT("M6800#%d take OCI\n", 0xfff4);
    }

    public static void TAKE_TOI() {
        ENTER_INTERRUPT("M6800#%d take TOI\n", 0xfff2);
    }

    public static void TAKE_SCI() {
        ENTER_INTERRUPT("M6800#%d take SCI\n", 0xfff0);
    }

    public static void TAKE_TRAP() {
        ENTER_INTERRUPT("M6800#%d take TRAP\n", 0xffee);
    }
    /* IRQ enter */

    public static void ENTER_INTERRUPT(String message, int irq_vector) {
        //LOG((errorlog, message, cpu_getactivecpu()));
        if ((m6800.wai_state & (M6800_WAI | M6800_SLP)) != 0) {
            if ((m6800.wai_state & M6800_WAI) != 0) {
                m6800.extra_cycles += 4;
            }
            m6800.wai_state &= ~(M6800_WAI | M6800_SLP);
        } else {
            PUSHWORD(m6800.pc);
            PUSHWORD(m6800.x);
            PUSHBYTE(m6800.a);
            PUSHBYTE(m6800.b);
            PUSHBYTE(m6800.cc);
            m6800.extra_cycles += 12;
        }
        SEI();
        m6800.pc = RM16(irq_vector);
        CHANGE_PC();
    }
    /* operate one instruction for */

    public void ONE_MORE_INSN() {
        int ireg;
        m6800.ppc = m6800.pc;
        //CALL_MAME_DEBUG;						
        ireg = M_RDOP(m6800.pc);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
        m6800.insn[ireg].handler();
        INCREMENT_COUNTER(m6800.cycles[ireg]);
    }
    /* check the IRQ lines for pending interrupts */

    public void CHECK_IRQ_LINES() {
        if ((m6800.cc & 0x10) == 0) {
            if (m6800.irq_state[M6800_IRQ_LINE] != CLEAR_LINE) {	/* stanadrd IRQ */

                ENTER_INTERRUPT("M6800#%d take IRQ1\n", 0xfff8);
                if (m6800.irq_callback != null) {
                    m6800.irq_callback.handler(M6800_IRQ_LINE);
                }
            } else {
                CHECK_IRQ2();
            }
        }
    }
    /* check IRQ2 (internal irq) */

    public static void CHECK_IRQ2() {
        if ((m6800.irq2 & (TCSR_ICF | TCSR_OCF | TCSR_TOF)) != 0) {
            if ((m6800.irq2 & TCSR_ICF) != 0) {
                TAKE_ICI();
                if (m6800.irq_callback != null) {
                    m6800.irq_callback.handler(M6800_TIN_LINE);
                }
            } else if ((m6800.irq2 & TCSR_OCF) != 0) {
                TAKE_OCI();
            } else if ((m6800.irq2 & TCSR_TOF) != 0) {
                TAKE_TOI();
            }
        }
    }

    @Override
    public void reset(Object param) {
        SEI();				/* IRQ disabled */

        m6800.pc = RM16(0xfffe);
        CHANGE_PC();

        /* HJB 990417 set CPU subtype (other reset functions override this) */
//	m6800.subtype   = SUBTYPE_M6800;
        m6800.insn = m6800_insn;
        m6800.cycles = cycles_6800;

        m6800.wai_state = 0;
        m6800.nmi_state = 0;
        m6800.irq_state[M6800_IRQ_LINE] = 0;
        m6800.irq_state[M6800_TIN_LINE] = 0;
        m6800.ic_eddge = 0;

        m6800.port1_ddr = 0x00;
        m6800.port2_ddr = 0x00;
        /* TODO: on reset port 2 should be read to determine the operating mode (bits 0-2) */
        m6800.tcsr = 0x00;
        m6800.pending_tcsr = 0x00;
        m6800.irq2 = 0;
        m6800.counter.SetD(0x0000);
        m6800.output_compare.SetD(0xffff);
        m6800.timer_over.SetD(0xffff);
        m6800.ram_ctrl |= 0x40;
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(int cycles) {
        /*UINT8*/
        int ireg;
        m6800_ICount[0] = cycles;

        CLEANUP_conters();
        INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        if ((m6800.wai_state & M6800_WAI) != 0) {
            EAT_CYCLES();
            //goto getout;
            INCREMENT_COUNTER(m6800.extra_cycles);
            m6800.extra_cycles = 0;

            return cycles - m6800_ICount[0];
        }

        do {
            m6800.ppc = m6800.pc;//pPPC = pPC;
            //CALL_MAME_DEBUG;
            ireg = M_RDOP(m6800.pc);
            m6800.pc = (m6800.pc + 1) & 0xFFFF;

            switch (ireg) {
                case 0x00:
                    illegal.handler();
                    break;
                case 0x01:
                    nop.handler();
                    break;
                case 0x02:
                    illegal.handler();
                    break;
                case 0x03:
                    illegal.handler();
                    break;
                case 0x04:
                    illegal.handler();
                    break;
                case 0x05:
                    illegal.handler();
                    break;
                case 0x06:
                    tap.handler();
                    break;
                case 0x07:
                    tpa.handler();
                    break;
                case 0x08:
                    inx.handler();
                    break;
                case 0x09:
                    dex.handler();
                    break;
                case 0x0A:
                    CLV();
                    break;
                case 0x0B:
                    SEV();
                    break;
                case 0x0C:
                    CLC();
                    break;
                case 0x0D:
                    SEC();
                    break;
                case 0x0E:
                    cli.handler();
                    break;
                case 0x0F:
                    sei.handler();
                    break;
                case 0x10:
                    sba.handler();
                    break;
                case 0x11:
                    cba.handler();
                    break;
                case 0x12:
                    illegal.handler();
                    break;
                case 0x13:
                    illegal.handler();
                    break;
                case 0x14:
                    illegal.handler();
                    break;
                case 0x15:
                    illegal.handler();
                    break;
                case 0x16:
                    tab.handler();
                    break;
                case 0x17:
                    tba.handler();
                    break;
                case 0x18:
                    illegal.handler();
                    break;
                case 0x19:
                    daa.handler();
                    break;
                case 0x1a:
                    illegal.handler();
                    break;
                case 0x1b:
                    aba.handler();
                    break;
                case 0x1c:
                    illegal.handler();
                    break;
                case 0x1d:
                    illegal.handler();
                    break;
                case 0x1e:
                    illegal.handler();
                    break;
                case 0x1f:
                    illegal.handler();
                    break;
                case 0x20:
                    bra.handler();
                    break;
                case 0x21:
                    brn.handler();
                    break;
                case 0x22:
                    bhi.handler();
                    break;
                case 0x23:
                    bls.handler();
                    break;
                case 0x24:
                    bcc.handler();
                    break;
                case 0x25:
                    bcs.handler();
                    break;
                case 0x26:
                    bne.handler();
                    break;
                case 0x27:
                    beq.handler();
                    break;
                case 0x28:
                    bvc.handler();
                    break;
                case 0x29:
                    bvs.handler();
                    break;
                case 0x2a:
                    bpl.handler();
                    break;
                case 0x2b:
                    bmi.handler();
                    break;
                case 0x2c:
                    bge.handler();
                    break;
                case 0x2d:
                    blt.handler();
                    break;
                case 0x2e:
                    bgt.handler();
                    break;
                case 0x2f:
                    ble.handler();
                    break;
                case 0x30:
                    tsx.handler();
                    break;
                case 0x31:
                    ins.handler();
                    break;
                case 0x32:
                    pula.handler();
                    break;
                case 0x33:
                    pulb.handler();
                    break;
                case 0x34:
                    des.handler();
                    break;
                case 0x35:
                    txs.handler();
                    break;
                case 0x36:
                    psha.handler();
                    break;
                case 0x37:
                    pshb.handler();
                    break;
                case 0x38:
                    illegal.handler();
                    break;
                case 0x39:
                    rts.handler();
                    break;
                case 0x3a:
                    illegal.handler();
                    break;
                case 0x3b:
                    rti.handler();
                    break;
                case 0x3c:
                    illegal.handler();
                    break;
                case 0x3d:
                    illegal.handler();
                    break;
                case 0x3e:
                    wai.handler();
                    break;
                case 0x3f:
                    swi.handler();
                    break;
                case 0x40:
                    nega.handler();
                    break;
                case 0x41:
                    illegal.handler();
                    break;
                case 0x42:
                    illegal.handler();
                    break;
                case 0x43:
                    coma.handler();
                    break;
                case 0x44:
                    lsra.handler();
                    break;
                case 0x45:
                    illegal.handler();
                    break;
                case 0x46:
                    rora.handler();
                    break;
                case 0x47:
                    asra.handler();
                    break;
                case 0x48:
                    asla.handler();
                    break;
                case 0x49:
                    rola.handler();
                    break;
                case 0x4a:
                    deca.handler();
                    break;
                case 0x4b:
                    illegal.handler();
                    break;
                case 0x4c:
                    inca.handler();
                    break;
                case 0x4d:
                    tsta.handler();
                    break;
                case 0x4e:
                    illegal.handler();
                    break;
                case 0x4f:
                    clra.handler();
                    break;
                case 0x50:
                    negb.handler();
                    break;
                case 0x51:
                    illegal.handler();
                    break;
                case 0x52:
                    illegal.handler();
                    break;
                case 0x53:
                    comb.handler();
                    break;
                case 0x54:
                    lsrb.handler();
                    break;
                case 0x55:
                    illegal.handler();
                    break;
                case 0x56:
                    rorb.handler();
                    break;
                case 0x57:
                    asrb.handler();
                    break;
                case 0x58:
                    aslb.handler();
                    break;
                case 0x59:
                    rolb.handler();
                    break;
                case 0x5a:
                    decb.handler();
                    break;
                case 0x5b:
                    illegal.handler();
                    break;
                case 0x5c:
                    incb.handler();
                    break;
                case 0x5d:
                    tstb.handler();
                    break;
                case 0x5e:
                    illegal.handler();
                    break;
                case 0x5f:
                    clrb.handler();
                    break;
                case 0x60:
                    neg_ix.handler();
                    break;
                case 0x61:
                    illegal.handler();
                    break;
                case 0x62:
                    illegal.handler();
                    break;
                case 0x63:
                    com_ix.handler();
                    break;
                case 0x64:
                    lsr_ix.handler();
                    break;
                case 0x65:
                    illegal.handler();
                    break;
                case 0x66:
                    ror_ix.handler();
                    break;
                case 0x67:
                    asr_ix.handler();
                    break;
                case 0x68:
                    asl_ix.handler();
                    break;
                case 0x69:
                    rol_ix.handler();
                    break;
                case 0x6a:
                    dec_ix.handler();
                    break;
                case 0x6b:
                    illegal.handler();
                    break;
                case 0x6c:
                    inc_ix.handler();
                    break;
                case 0x6d:
                    tst_ix.handler();
                    break;
                case 0x6e:
                    jmp_ix.handler();
                    break;
                case 0x6f:
                    clr_ix.handler();
                    break;
                case 0x70:
                    neg_ex.handler();
                    break;
                case 0x71:
                    illegal.handler();
                    break;
                case 0x72:
                    illegal.handler();
                    break;
                case 0x73:
                    com_ex.handler();
                    break;
                case 0x74:
                    lsr_ex.handler();
                    break;
                case 0x75:
                    illegal.handler();
                    break;
                case 0x76:
                    ror_ex.handler();
                    break;
                case 0x77:
                    asr_ex.handler();
                    break;
                case 0x78:
                    asl_ex.handler();
                    break;
                case 0x79:
                    rol_ex.handler();
                    break;
                case 0x7a:
                    dec_ex.handler();
                    break;
                case 0x7b:
                    illegal.handler();
                    break;
                case 0x7c:
                    inc_ex.handler();
                    break;
                case 0x7d:
                    tst_ex.handler();
                    break;
                case 0x7e:
                    jmp_ex.handler();
                    break;
                case 0x7f:
                    clr_ex.handler();
                    break;
                case 0x80:
                    suba_im.handler();
                    break;
                case 0x81:
                    cmpa_im.handler();
                    break;
                case 0x82:
                    sbca_im.handler();
                    break;
                case 0x83:
                    illegal.handler();
                    break;
                case 0x84:
                    anda_im.handler();
                    break;
                case 0x85:
                    bita_im.handler();
                    break;
                case 0x86:
                    lda_im.handler();
                    break;
                case 0x87:
                    sta_im.handler();
                    break;
                case 0x88:
                    eora_im.handler();
                    break;
                case 0x89:
                    adca_im.handler();
                    break;
                case 0x8a:
                    ora_im.handler();
                    break;
                case 0x8b:
                    adda_im.handler();
                    break;
                case 0x8c:
                    cmpx_im.handler();
                    break;
                case 0x8d:
                    bsr.handler();
                    break;
                case 0x8e:
                    lds_im.handler();
                    break;
                case 0x8f:
                    sts_im.handler(); /* orthogonality */ break;
                case 0x90:
                    suba_di.handler();
                    break;
                case 0x91:
                    cmpa_di.handler();
                    break;
                case 0x92:
                    sbca_di.handler();
                    break;
                case 0x93:
                    illegal.handler();
                    break;
                case 0x94:
                    anda_di.handler();
                    break;
                case 0x95:
                    bita_di.handler();
                    break;
                case 0x96:
                    lda_di.handler();
                    break;
                case 0x97:
                    sta_di.handler();
                    break;
                case 0x98:
                    eora_di.handler();
                    break;
                case 0x99:
                    adca_di.handler();
                    break;
                case 0x9a:
                    ora_di.handler();
                    break;
                case 0x9b:
                    adda_di.handler();
                    break;
                case 0x9c:
                    cmpx_di.handler();
                    break;
                case 0x9d:
                    jsr_di.handler();
                    break;
                case 0x9e:
                    lds_di.handler();
                    break;
                case 0x9f:
                    sts_di.handler();
                    break;
                case 0xa0:
                    suba_ix.handler();
                    break;
                case 0xa1:
                    cmpa_ix.handler();
                    break;
                case 0xa2:
                    sbca_ix.handler();
                    break;
                case 0xa3:
                    illegal.handler();
                    break;
                case 0xa4:
                    anda_ix.handler();
                    break;
                case 0xa5:
                    bita_ix.handler();
                    break;
                case 0xa6:
                    lda_ix.handler();
                    break;
                case 0xa7:
                    sta_ix.handler();
                    break;
                case 0xa8:
                    eora_ix.handler();
                    break;
                case 0xa9:
                    adca_ix.handler();
                    break;
                case 0xaa:
                    ora_ix.handler();
                    break;
                case 0xab:
                    adda_ix.handler();
                    break;
                case 0xac:
                    cmpx_ix.handler();
                    break;
                case 0xad:
                    jsr_ix.handler();
                    break;
                case 0xae:
                    lds_ix.handler();
                    break;
                case 0xaf:
                    sts_ix.handler();
                    break;
                case 0xb0:
                    suba_ex.handler();
                    break;
                case 0xb1:
                    cmpa_ex.handler();
                    break;
                case 0xb2:
                    sbca_ex.handler();
                    break;
                case 0xb3:
                    illegal.handler();
                    break;
                case 0xb4:
                    anda_ex.handler();
                    break;
                case 0xb5:
                    bita_ex.handler();
                    break;
                case 0xb6:
                    lda_ex.handler();
                    break;
                case 0xb7:
                    sta_ex.handler();
                    break;
                case 0xb8:
                    eora_ex.handler();
                    break;
                case 0xb9:
                    adca_ex.handler();
                    break;
                case 0xba:
                    ora_ex.handler();
                    break;
                case 0xbb:
                    adda_ex.handler();
                    break;
                case 0xbc:
                    cmpx_ex.handler();
                    break;
                case 0xbd:
                    jsr_ex.handler();
                    break;
                case 0xbe:
                    lds_ex.handler();
                    break;
                case 0xbf:
                    sts_ex.handler();
                    break;
                case 0xc0:
                    subb_im.handler();
                    break;
                case 0xc1:
                    cmpb_im.handler();
                    break;
                case 0xc2:
                    sbcb_im.handler();
                    break;
                case 0xc3:
                    illegal.handler();
                    break;
                case 0xc4:
                    andb_im.handler();
                    break;
                case 0xc5:
                    bitb_im.handler();
                    break;
                case 0xc6:
                    ldb_im.handler();
                    break;
                case 0xc7:
                    stb_im.handler();
                    break;
                case 0xc8:
                    eorb_im.handler();
                    break;
                case 0xc9:
                    adcb_im.handler();
                    break;
                case 0xca:
                    orb_im.handler();
                    break;
                case 0xcb:
                    addb_im.handler();
                    break;
                case 0xcc:
                    illegal.handler();
                    break;
                case 0xcd:
                    illegal.handler();
                    break;
                case 0xce:
                    ldx_im.handler();
                    break;
                case 0xcf:
                    stx_im.handler();
                    break;
                case 0xd0:
                    subb_di.handler();
                    break;
                case 0xd1:
                    cmpb_di.handler();
                    break;
                case 0xd2:
                    sbcb_di.handler();
                    break;
                case 0xd3:
                    illegal.handler();
                    break;
                case 0xd4:
                    andb_di.handler();
                    break;
                case 0xd5:
                    bitb_di.handler();
                    break;
                case 0xd6:
                    ldb_di.handler();
                    break;
                case 0xd7:
                    stb_di.handler();
                    break;
                case 0xd8:
                    eorb_di.handler();
                    break;
                case 0xd9:
                    adcb_di.handler();
                    break;
                case 0xda:
                    orb_di.handler();
                    break;
                case 0xdb:
                    addb_di.handler();
                    break;
                case 0xdc:
                    illegal.handler();
                    break;
                case 0xdd:
                    illegal.handler();
                    break;
                case 0xde:
                    ldx_di.handler();
                    break;
                case 0xdf:
                    stx_di.handler();
                    break;
                case 0xe0:
                    subb_ix.handler();
                    break;
                case 0xe1:
                    cmpb_ix.handler();
                    break;
                case 0xe2:
                    sbcb_ix.handler();
                    break;
                case 0xe3:
                    illegal.handler();
                    break;
                case 0xe4:
                    andb_ix.handler();
                    break;
                case 0xe5:
                    bitb_ix.handler();
                    break;
                case 0xe6:
                    ldb_ix.handler();
                    break;
                case 0xe7:
                    stb_ix.handler();
                    break;
                case 0xe8:
                    eorb_ix.handler();
                    break;
                case 0xe9:
                    adcb_ix.handler();
                    break;
                case 0xea:
                    orb_ix.handler();
                    break;
                case 0xeb:
                    addb_ix.handler();
                    break;
                case 0xec:
                    illegal.handler();
                    break;
                case 0xed:
                    illegal.handler();
                    break;
                case 0xee:
                    ldx_ix.handler();
                    break;
                case 0xef:
                    stx_ix.handler();
                    break;
                case 0xf0:
                    subb_ex.handler();
                    break;
                case 0xf1:
                    cmpb_ex.handler();
                    break;
                case 0xf2:
                    sbcb_ex.handler();
                    break;
                case 0xf3:
                    illegal.handler();
                    break;
                case 0xf4:
                    andb_ex.handler();
                    break;
                case 0xf5:
                    bitb_ex.handler();
                    break;
                case 0xf6:
                    ldb_ex.handler();
                    break;
                case 0xf7:
                    stb_ex.handler();
                    break;
                case 0xf8:
                    eorb_ex.handler();
                    break;
                case 0xf9:
                    adcb_ex.handler();
                    break;
                case 0xfa:
                    orb_ex.handler();
                    break;
                case 0xfb:
                    addb_ex.handler();
                    break;
                case 0xfc:
                    addx_ex.handler();
                    break;
                case 0xfd:
                    illegal.handler();
                    break;
                case 0xfe:
                    ldx_ex.handler();
                    break;
                case 0xff:
                    stx_ex.handler();
                    break;
            }
            INCREMENT_COUNTER(cycles_6800[ireg]);
        } while (m6800_ICount[0] > 0);

//getout:
        INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        return cycles - m6800_ICount[0];
    }

    @Override
    public Object init_context() {
        Object reg = new m6800_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_pc() {
        return m6800.pc & 0xFFFF;
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
        if (m6800.nmi_state == state) return;
	//LOG((errorlog, "M6800#%d set_nmi_line %d ", cpu_getactivecpu(), state));
	m6800.nmi_state = state;
	if (state == CLEAR_LINE) return;

	/* NMI */
	ENTER_INTERRUPT("M6800#%d take NMI\n",0xfffc);
    }

    @Override
    public void set_irq_line(int irqline, int state) {
	int eddge;

	if (m6800.irq_state[irqline] == state) return;
	//LOG((errorlog, "M6800#%d set_irq_line %d,%d\n", cpu_getactivecpu(), irqline, state));
	m6800.irq_state[irqline] = state;

	switch(irqline)
	{
	case M6800_IRQ_LINE:
		if (state == CLEAR_LINE) return;
		break;
	case M6800_TIN_LINE:
		eddge = (state == CLEAR_LINE ) ? 2 : 0;
		if( ((m6800.tcsr&TCSR_IEDG) ^ (state==CLEAR_LINE ? TCSR_IEDG : 0))==0 )
			return;
		/* active eddge in */
		m6800.tcsr |= TCSR_ICF;
		m6800.pending_tcsr |= TCSR_ICF;
		m6800.input_capture = (int)m6800.counter.L;
		MODIFIED_tcsr();
		if( (m6800.cc & 0x10)==0 )
			CHECK_IRQ2();
		break;
	default:
		return;
	}
	CHECK_IRQ_LINES(); /* HJB 990417 */
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        m6800.irq_callback = callback;
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
        switch (regnum) {
            case CPU_INFO_NAME:
                return "M6800";
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
        cpu_setOPbase16.handler(pc, 0);
    }

//public opcode illegal = new opcode() { public void handler() 
    public opcode illegal = new opcode() {
        public void handler() {
            if (errorlog != null) {
                fprintf(errorlog, "M6808: illegal opcode: address %04X, op %02X\n", m6800.pc, (int) M_RDOP_ARG(m6800.pc) & 0xFF);
            }
        }
    };

    /* HD63701 only */
    public opcode trap = new opcode() {
        public void handler() {
            if (errorlog != null) {
                fprintf(errorlog, "M6808: illegal opcode: address %04X, op %02X\n", m6800.pc, (int) M_RDOP_ARG(m6800.pc) & 0xFF);
            }
            TAKE_TRAP();
        }
    };


    /* $01 NOP */
    public opcode nop = new opcode() {
        public void handler() {
        }
    };

    /* $02 ILLEGAL */

    /* $03 ILLEGAL */

    /* $04 LSRD inherent -0*-* */
    public opcode lsrd = new opcode() {
        public void handler() {
            /*UINT16*/
            int t;
            CLR_NZC();
            t = getDreg();
            m6800.cc |= (t & 0x0001);
            t >>= 1;
            SET_Z16(t);
            setDreg(t);
        }
    };

    /* $05 ASLD inherent ?**** */
    public opcode asld = new opcode() {
        public void handler() {
            int r;
            int t;
            t = getDreg();
            r = t << 1;
            CLR_NZVC();
            SET_FLAGS16(t, t, r);
            setDreg(r);
        }
    };

    /* $06 TAP inherent ##### */
    public opcode tap = new opcode() {
        public void handler() {
            m6800.cc = m6800.a;
            ONE_MORE_INSN();
            CHECK_IRQ_LINES(); /* HJB 990417 */

        }
    };

    /* $07 TPA inherent ----- */
    public opcode tpa = new opcode() {
        public void handler() {
            m6800.a = m6800.cc & 0xFF;//A = CC;
        }
    };

    /* $08 INX inherent --*-- */
    public opcode inx = new opcode() {
        public void handler() {
            m6800.x = (m6800.x + 1) & 0xFFFF;//++X;
            CLR_Z();
            SET_Z16(m6800.x);
        }
    };

    /* $09 DEX inherent --*-- */
    public opcode dex = new opcode() {
        public void handler() {
            m6800.x = (m6800.x - 1) & 0xFFFF;//--X;
            CLR_Z();
            SET_Z16(m6800.x);
        }
    };

    /* $0a CLV */
    public opcode clv = new opcode() {
        public void handler() {
            CLV();
        }
    };

    /* $0b SEV */
    public opcode sev = new opcode() {
        public void handler() {
            SEV();
        }
    };

    /* $0c CLC */
    public opcode clc = new opcode() {
        public void handler() {
            CLC();
        }
    };

    /* $0d SEC */
    public opcode sec = new opcode() {
        public void handler() {
            SEC();
        }
    };

    /* $0e CLI */
    public opcode cli = new opcode() {
        public void handler() {
            CLI();
            ONE_MORE_INSN();
            CHECK_IRQ_LINES(); /* HJB 990417 */

        }
    };

    /* $0f SEI */
    public opcode sei = new opcode() {
        public void handler() {
            SEI();
            ONE_MORE_INSN();
            CHECK_IRQ_LINES(); /* HJB 990417 */

        }
    };

    /* $10 SBA inherent -**** */
    public opcode sba = new opcode() {
        public void handler() {
            /*UINT16*/
            int t;
            t = (m6800.a - m6800.b) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, m6800.b, t);
            m6800.a = t & 0xFF;
        }
    };

    /* $11 CBA inherent -**** */
    public opcode cba = new opcode() {
        public void handler() {
            /*UINT16*/
            int t;
            t = (m6800.a - m6800.b) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, m6800.b, t);
        }
    };

    /* $12 ILLEGAL */
    public opcode undoc1 = new opcode() {
        public void handler() {
            m6800.x = (m6800.x + RM(m6800.s + 1)) & 0xFFFF;
        }
    };

    /* $13 ILLEGAL */
    public opcode undoc2 = new opcode() {
        public void handler() {
            m6800.x = (m6800.x + RM(m6800.s + 1)) & 0xFFFF;
        }
    };


    /* $14 ILLEGAL */

    /* $15 ILLEGAL */

    /* $16 TAB inherent -**0- */
    public opcode tab = new opcode() {
        public void handler() {
            m6800.b = m6800.a;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $17 TBA inherent -**0- */
    public opcode tba = new opcode() {
        public void handler() {
            m6800.a = m6800.b;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $18 XGDX inherent ----- */ /* HD63701YO only */
    public opcode xgdx = new opcode() {
        public void handler() {
            /*UINT16*/
            int t = m6800.x & 0xFFFF;
            m6800.x = getDreg();
            setDreg(t);
        }
    };

    /* $19 DAA inherent (A) -**0* */
    public opcode daa = new opcode() {
        public void handler() {
            int/*UINT8*/ msn, lsn;
            int/*UINT16*/ t, cf = 0;
            msn = m6800.a & 0xf0;
            lsn = m6800.a & 0x0f;
            if (lsn > 0x09 || (m6800.cc & 0x20) != 0) {
                cf |= 0x06;
            }
            if (msn > 0x80 && lsn > 0x09) {
                cf |= 0x60;
            }
            if (msn > 0x90 || (m6800.cc & 0x01) != 0) {
                cf |= 0x60;
            }
            t = cf + m6800.a;
            CLR_NZV(); /* keep carry from previous operation */

            SET_NZ8(/*(UINT8)*/t & 0xFF);
            SET_C8(t);
            m6800.a = t & 0xFF;
        }
    };

    /* HD63701YO only */
    public opcode slp = new opcode() {
        public void handler() {
            /* wait for next IRQ (same as waiting of wai) */
    /*TODO*///        m6808.wai_state |= HD63701_SLP;
    /*TODO*///       EAT_CYCLES;
            throw new UnsupportedOperationException("Unsupported");
        }
    };


/* $1b ABA inherent ***** */
public opcode aba = new opcode() {
        public void handler() {
            /*UINT16*/int t;
            t = (m6800.a + m6800.b)&0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a, m6800.b, t);
            SET_H(m6800.a, m6800.a, t);
            m6800.a = t&0xFF;
            
        }
    };
    /* $20 BRA relative ----- */
    public opcode bra = new opcode() {
        public void handler() {
            int t;
            t=IMMBYTE();
            m6800.pc=(m6800.pc+(byte)t) & 0xFFFF;//TODO check if it has to be better...
            CHANGE_PC();
            /* speed up busy loops */
            if (t == 0xfe) {
                EAT_CYCLES();
            }
        }
    };

    /* $21 BRN relative ----- */
    public opcode brn = new opcode() {
        public void handler() {
            int t=IMMBYTE();
                    
        }
    };

    /* $22 BHI relative ----- */
    public opcode bhi = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc & (0x05))==0 );
        }
    };

    /* $23 BLS relative ----- */
    public opcode bls = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc & (0x05))!=0 );

        }
    };

    /* $24 BCC relative ----- */
    public opcode bcc = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc&0x01)==0 );
        }
    };

    /* $25 BCS relative ----- */
    public opcode bcs = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc&0x01)!=0 );
        }
    };

    /* $26 BNE relative ----- */
    public opcode bne = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc&0x04)==0 );
        }
    };

    /* $27 BEQ relative ----- */
    public opcode beq = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc&0x04)!=0 );
        }
    };

    /* $28 BVC relative ----- */
    public opcode bvc = new opcode() {
        public void handler() {
            BRANCH( (m6800.cc&0x02)==0 );
        }
    };

    /* $29 BVS relative ----- */
    public opcode bvs = new opcode() {
        public void handler() {
            BRANCH((m6800.cc & 0x02)!=0);
        }
    };

    /* $2a BPL relative ----- */
    public opcode bpl = new opcode() {
        public void handler() {
            BRANCH((m6800.cc & 0x08)==0);
        }
    };

    /* $2b BMI relative ----- */
    public opcode bmi = new opcode() {
        public void handler() {
           BRANCH( (m6800.cc&0x08)!=0 );
        }
    };

    /* $2c BGE relative ----- */
    public opcode bge = new opcode() {
        public void handler() {
            BRANCH( NXORV()==0 );
        }
    };

    /* $2d BLT relative ----- */
    public opcode blt = new opcode() {
        public void handler() {
           BRANCH( NXORV()!=0 );
        }
    };

    /* $2e BGT relative ----- */
    public opcode bgt = new opcode() {
        public void handler() {
            BRANCH( !((NXORV()!=0) || ((m6800.cc&0x04)!=0)) );
            
        }
    };

    /* $2f BLE relative ----- */
    public opcode ble = new opcode() {
        public void handler() {
            BRANCH( ((NXORV()!=0) || ((m6800.cc&0x04)!=0)) );

        }
    };

    /* $30 TSX inherent ----- */
    public opcode tsx = new opcode() {
        public void handler() {
            m6800.x = (m6800.s + 1) & 0xFF;
        }
    };

    /* $31 INS inherent ----- */
    public opcode ins = new opcode() {
        public void handler() {
            m6800.s = (m6800.s + 1) & 0xFFFF; //++S;
        }
    };

    /* $32 PULA inherent ----- */
    public opcode pula = new opcode() {
        public void handler() {
            m6800.a = PULLBYTE();//PULLBYTE(m6808.d.b.h);
            
        }
    };

    /* $33 PULB inherent ----- */
    public opcode pulb = new opcode() {
        public void handler() {
           m6800.b = PULLBYTE();////PULLBYTE(m6808.d.b.l);          
        }
    };

    /* $34 DES inherent ----- */
    public opcode des = new opcode() {
        public void handler() {
            m6800.s = (m6800.s - 1) &0xFFFF;//--S;
        }
    };

    /* $35 TXS inherent ----- */
    public opcode txs = new opcode() {
        public void handler() {
            m6800.s = (m6800.x - 1 ) & 0xFFFF;//S = (X - 1);
        }
    };

    /* $36 PSHA inherent ----- */
    public opcode psha = new opcode() {
        public void handler() {
        PUSHBYTE(m6800.a);//PUSHBYTE(m6808.d.b.h);
        }
    };

    /* $37 PSHB inherent ----- */
    public opcode pshb = new opcode() {
        public void handler() {
        PUSHBYTE(m6800.b);//PUSHBYTE(m6808.d.b.l);

        }
    };

    /* $38 PULX inherent ----- */
    public opcode pulx = new opcode() {
        public void handler() {
           m6800.x = PULLWORD();
        }
    };

    /* $39 RTS inherent ----- */
    public opcode rts = new opcode() {
        public void handler() {
            m6800.pc = PULLWORD();
            CHANGE_PC();
        }
    };

    /* $3a ABX inherent ----- */
    public opcode abx = new opcode() {
        public void handler() {
            m6800.x = (m6800.x + m6800.b)&0xFFFF;//X += B;
        }
    };

    /* $3b RTI inherent ##### */
    public opcode rti = new opcode() {
        public void handler() {
            m6800.cc=PULLBYTE();
            m6800.b=PULLBYTE();
            m6800.a=PULLBYTE();
            m6800.x=PULLWORD();
            m6800.pc=PULLWORD();
            CHANGE_PC();
            CHECK_IRQ_LINES(); /* HJB 990417 */

        }
    };

    /* $3c PSHX inherent ----- */
    public opcode pshx = new opcode() {
        public void handler() {
            PUSHWORD(m6800.x);
        }
    };

    /* $3d MUL inherent --*-@ */
    public opcode mul = new opcode() {
        public void handler() {
            int t;
            t = (m6800.a * m6800.b) & 0xFFFF;
            CLR_C();
            if((t&0x80)!=0) SEC();
            setDreg(t);
        }
    };

    /* $3e WAI inherent ----- */
    public opcode wai = new opcode() {
        public void handler() {
            /*
             * WAI stacks the entire machine state on the
             * hardware stack, then waits for an interrupt.
             */
            m6800.wai_state |= M6800_WAI;
            PUSHWORD(m6800.pc);
            PUSHWORD(m6800.x);
            PUSHBYTE(m6800.a);
            PUSHBYTE(m6800.b);
            PUSHBYTE(m6800.cc);
            CHECK_IRQ_LINES();
            if ((m6800.wai_state & M6800_WAI)!=0) {
                EAT_CYCLES();
            }
        }
    };

    /* $3f SWI absolute indirect ----- */
    public opcode swi = new opcode() {
        public void handler() {
            PUSHWORD(m6800.pc);
            PUSHWORD(m6800.x);
            PUSHBYTE(m6800.a);
            PUSHBYTE(m6800.b);
            PUSHBYTE(m6800.cc);
            SEI();
            m6800.pc = RM16(0xfffa) & 0xFFFF;
            CHANGE_PC();
        }
    };

    /* $40 NEGA inherent ?**** */
    public opcode nega = new opcode() {
        public void handler() {
            int r;
            r = -m6800.a & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, m6800.a, r);
            m6800.a = r & 0xFF;
        }
    };

    /* $41 ILLEGAL */

    /* $42 ILLEGAL */

    /* $43 COMA inherent -**01 */
    public opcode coma = new opcode() {
        public void handler() {
            m6800.a = ~m6800.a;
            CLR_NZV();
            SET_NZ8(m6800.a);
            SEC();
        }
    };

    /* $44 LSRA inherent -0*-* */
    public opcode lsra = new opcode() {
        public void handler() {
            CLR_NZC();
            m6800.cc |= (m6800.a & 0x01);
            m6800.a = (m6800.a >> 1) & 0xFF;
            SET_Z8(m6800.a);
        }
    };

    /* $45 ILLEGAL */

    /* $46 RORA inherent -**-* */
    public opcode rora = new opcode() {
        public void handler() {
            int r;
            r = ((m6800.cc & 0x01) << 7)&0xFF;
            CLR_NZC();
            m6800.cc |= (m6800.a & 0x01);
            r = (r | m6800.a >> 1)&0xFF;
            SET_NZ8(r);
            m6800.a = r&0xFF;
        }
    };

    /* $47 ASRA inherent ?**-* */
    public opcode asra = new opcode() {
        public void handler() {
            CLR_NZC();
            m6800.cc |= (m6800.a & 0x01);
            m6800.a >>= 1;
            m6800.a |= ((m6800.a & 0x40) << 1);
            SET_NZ8(m6800.a);
            
        }
    };

    /* $48 ASLA inherent ?**** */
    public opcode asla = new opcode() {
        public void handler() {
            int r = (m6800.a << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a,m6800.a,r);
            m6800.a = r & 0xFF;
        }
    };

    /* $49 ROLA inherent -**** */
    public opcode rola = new opcode() {
        public void handler() {
            int t,r;
            t = m6800.a & 0xFFFF;
            r = ((m6800.cc & 0x01));
            r |= t << 1; 
            CLR_NZVC(); 
            SET_FLAGS8(t,t,r);
            m6800.a = r & 0xFF;
        }
    };

    /* $4a DECA inherent -***- */
    public opcode deca = new opcode() {
        public void handler() {
            m6800.a = (m6800.a - 1) & 0xFF;//--A;
            CLR_NZV();
            SET_FLAGS8D(m6800.a);
        }
    };

    /* $4b ILLEGAL */

    /* $4c INCA inherent -***- */
    public opcode inca = new opcode() {
        public void handler() {
            m6800.a = (m6800.a + 1) & 0xFF;//++A;
            CLR_NZV();
            SET_FLAGS8I(m6800.a);
        }
    };

    /* $4d TSTA inherent -**0- */
    public opcode tsta = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(m6800.a);
        }
    };

    /* $4e ILLEGAL */

    /* $4f CLRA inherent -0100 */
    public opcode clra = new opcode() {
        public void handler() {
            m6800.a = 0;
            CLR_NZVC();
            SEZ();
        }
    };

    /* $50 NEGB inherent ?**** */
    public opcode negb = new opcode() {
        public void handler() {
            int r;
            r = -m6800.b & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0,m6800.b,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $53 COMB inherent -**01 */
    public opcode comb = new opcode() {
        public void handler() {
            m6800.b = (m6800.b ^ 0xFFFFFFFF) & 0xFF;//B = ~B;
            CLR_NZV();
            SET_NZ8(m6800.b);
            SEC();
        }
    };

    /* $54 LSRB inherent -0*-* */
    public opcode lsrb = new opcode() {
        public void handler() {
            CLR_NZC();
            m6800.cc |= (m6800.b & 0x01);
            m6800.b = m6800.b >> 1 &0xFF;
            SET_Z8(m6800.b);
        }
    };

    /* $55 ILLEGAL */

    /* $56 RORB inherent -**-* */
    public opcode rorb = new opcode() {
        public void handler() {
            int r;
            r = ((m6800.cc & 0x01) << 7)&0xFF;
            CLR_NZC();
            m6800.cc |= (m6800.b & 0x01);
            r = (r | m6800.b >> 1)&0xFF;
            SET_NZ8(r);
            m6800.b = r&0xFF;
        }
    };

    /* $57 ASRB inherent ?**-* */
    public opcode asrb = new opcode() {
        public void handler() {
            CLR_NZC();
            m6800.cc |= (m6800.b & 0x01);
            m6800.b >>= 1;
            m6800.b |= ((m6800.b & 0x40) << 1);
            SET_NZ8(m6800.b);           
        }
    };

    /* $58 ASLB inherent ?**** */
    public opcode aslb = new opcode() {
        public void handler() {
            int r = (m6800.b << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,m6800.b,r);
            m6800.b = r & 0xFF;
            
        }
    };

    /* $59 ROLB inherent -**** */
    public opcode rolb = new opcode() {
        public void handler() {
             int t,r;
            t = m6800.b;
            r = m6800.cc & 0x01;
            r = (r | t << 1) &0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t,t,r);
            m6800.b = r & 0xFF;
            
        }
    };

    /* $5a DECB inherent -***- */
    public opcode decb = new opcode() {
        public void handler() {
            m6800.b = (m6800.b-1)&0xFF;	
            CLR_NZV();
            SET_FLAGS8D(m6800.b);
        }
    };

    /* $5b ILLEGAL */

    /* $5c INCB inherent -***- */
    public opcode incb = new opcode() {
        public void handler() {
            m6800.b = (m6800.b + 1) & 0xFF;  //++B;
            CLR_NZV();
            SET_FLAGS8I(m6800.b);
        }
    };

    /* $5d TSTB inherent -**0- */
    public opcode tstb = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(m6800.b);
        }
    };

    /* $5e ILLEGAL */

    /* $5f CLRB inherent -0100 */
    public opcode clrb = new opcode() {
        public void handler() {
            m6800.b = 0;
            CLR_NZVC();
            SEZ();
        }
    };

    /* $60 NEG indexed ?**** */
    public opcode neg_ix = new opcode() {
        public void handler() {
            int r, t;
            t = IDXBYTE();
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r);
        }
    };

    /* $61 AIM --**0- */ /* HD63701YO only */
    public opcode aim_ix = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = IDXBYTE();
            r &= t;
            CLR_NZV();
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /* $62 OIM --**0- */ /* HD63701YO only */
    public opcode oim_ix = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = IDXBYTE();
            r |= t;
            CLR_NZV();
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /* $63 COM indexed -**01 */
    public opcode com_ix = new opcode() {
        public void handler() {
            int t;
            t = IDXBYTE();
            t = ~t;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    /* $64 LSR indexed -0*-* */
    public opcode lsr_ix = new opcode() {
        public void handler() {
            int t;
            t = IDXBYTE();
            CLR_NZC();
            m6800.cc |= (t & 0x01);
            t = (t >> 1) & 0xFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    /* $65 EIM --**0- */ /* HD63701YO only */
    public opcode eim_ix = new opcode() {
        public void handler() {
            /*UINT8*/
            int t, r;
            t = IMMBYTE();
            r = IDXBYTE();
            r ^= t;
            CLR_NZV();
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /* $66 ROR indexed -**-* */
    public opcode ror_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = (CC & 0x01) << 7;
            /*TODO*///CLR_NZC;
            /*TODO*///CC |= (t & 0x01);
            /*TODO*///r |= t >> 1;
            /*TODO*///SET_NZ8(r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $67 ASR indexed ?**-* */
    public opcode asr_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t;
            /*TODO*///IDXBYTE(t);CLR_NZC;
            /*TODO*///CC |= (t & 0x01);
            /*TODO*///t >>= 1;
            /*TODO*///t |= ((t & 0x40) << 1);
            /*TODO*///SET_NZ8(t);
            /*TODO*///WM(EAD, t);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $68 ASL indexed ?**** */
    public opcode asl_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = t << 1;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(t, t, r);
           /*TODO*/// WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $69 ROL indexed -**** */
    public opcode rol_ix = new opcode() {
        public void handler() {
            int t,r;
            t=IDXBYTE();
            r = m6800.cc & 0x01;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t,t,r);
            WM(ea,r);
        }
    };

    /* $6a DEC indexed -***- */
    public opcode dec_ix = new opcode() {
        public void handler() {
            //UINT8 t;
            int t=IDXBYTE();
            t= (t-1 )&0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);
            
        }
    };

    /* $6b TIM --**0- */ /* HD63701YO only */
    public opcode tim_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///IMMBYTE(t);
            /*TODO*///IDXBYTE(r);
            /*TODO*///r &= t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $6c INC indexed -***- */
    public opcode inc_ix = new opcode() {
        public void handler() {
            int t=IDXBYTE();
            t = (t + 1) &0xFF;
            CLR_NZV(); 
            SET_FLAGS8I(t);
            WM(ea,t);
        }
    };

    /* $6d TST indexed -**0- */
    public opcode tst_ix = new opcode() {
        public void handler() {
            int t=IDXBYTE();
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    /* $6e JMP indexed ----- */
    public opcode jmp_ix = new opcode() {
        public void handler() {
            INDEXED();
            m6800.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    /* $6f CLR indexed -0100 */
    public opcode clr_ix = new opcode() {
        public void handler() {
            INDEXED();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    /* $70 NEG extended ?**** */
    public opcode neg_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 r, t;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = -t;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(0, t, r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $71 AIM --**0- */ /* HD63701YO only */
    public opcode aim_di = new opcode() {
        public void handler() {
            int t, r;
            t=IMMBYTE();
            r=DIRBYTE();
            r &= t;
            CLR_NZV();
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /* $72 OIM --**0- */ /* HD63701YO only */
    public opcode oim_di = new opcode() {
        public void handler() {
            int t, r;
            t=IMMBYTE();
            r=DIRBYTE();
            r |= t;
            CLR_NZV();
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    /* $73 COM extended -**01 */
    public opcode com_ex = new opcode() {
        public void handler() {
            int t= EXTBYTE(); 
            t = (t^ 0xFFFFFFFF) & 0xFF;
            CLR_NZV(); 
            SET_NZ8(t); 
            SEC();
            WM(ea,t);
        }
    };

    /* $74 LSR extended -0*-* */
    public opcode lsr_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE(); 
            CLR_NZC(); 
            m6800.cc |= (t & 0x01);
            t=(t>>1) &0XFF; 
            SET_Z8(t);
            WM(ea,t);
        }
    };

    /* $75 EIM --**0- */ /* HD63701YO only */
    public opcode eim_di = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///IMMBYTE(t);
            /*TODO*///DIRBYTE(r);
            /*TODO*///r ^= t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $76 ROR extended -**-* */
    public opcode ror_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = (CC & 0x01) << 7;
            /*TODO*///CLR_NZC;
            /*TODO*///CC |= (t & 0x01);
            /*TODO*///r |= t >> 1;
            /*TODO*///SET_NZ8(r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
            
        }
    };

    /* $77 ASR extended ?**-* */
    public opcode asr_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t;
            /*TODO*///EXTBYTE(t);CLR_NZC;
            /*TODO*///CC |= (t & 0x01);
            /*TODO*///t >>= 1;
            /*TODO*///t |= ((t & 0x40) << 1);
            /*TODO*///SET_NZ8(t);
            /*TODO*///WM(EAD, t);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $78 ASL extended ?**** */
    public opcode asl_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = t << 1;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(t, t, r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $79 ROL extended -**** */
    public opcode rol_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = CC & 0x01;
            /*TODO*///r |= t << 1;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(t, t, r);
            /*TODO*///WM(EAD, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $7a DEC extended -***- */
    public opcode dec_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE(); 
            t=(t-1)&0xFF;
            CLR_NZV(); 
            SET_FLAGS8D(t);
            WM(ea,t);
        }
    };

    /* $7b TIM --**0- */ /* HD63701YO only */
    public opcode tim_di = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///IMMBYTE(t);
            /*TODO*///DIRBYTE(r);
            /*TODO*///r &= t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $7c INC extended -***- */
    public opcode inc_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE(); 
            t=t+1&0xFF;
            CLR_NZV(); 
            SET_FLAGS8I(t);
            WM(ea,t);
        }
    };

    /* $7d TST extended -**0- */
    public opcode tst_ex = new opcode() {
        public void handler() {
            int t;
            t=EXTBYTE();
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    /* $7e JMP extended ----- */
    public opcode jmp_ex = new opcode() {
        public void handler() {
            EXTENDED();
            m6800.pc = ea & 0xFFFF;
            CHANGE_PC(); /* TS 971002 */

        }
    };

    /* $7f CLR extended -0100 */
    public opcode clr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    /* $80 SUBA immediate ?**** */
    public opcode suba_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6800.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, t, r);
            m6800.a = r & 0xFF;
        }
    };

    /* $81 CMPA immediate ?**** */
    public opcode cmpa_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6800.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, t, r);
        }
    };

    /* $82 SBCA immediate ?**** */
    public opcode sbca_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (m6800.a - t - (m6800.cc & 0x01)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, t, r);
            m6800.a = r & 0xFF;

        }
    };

    /* $83 SUBD immediate -**** */
    public opcode subd_im = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b=IMMWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    /* $84 ANDA immediate -**0- */
    public opcode anda_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.a &= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $85 BITA immediate -**0- */
    public opcode bita_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = m6800.a & t;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    /* $86 LDA immediate -**0- */
    public opcode lda_im = new opcode() {
        public void handler() {
            m6800.a=IMMBYTE();
            CLR_NZV();
            SET_NZ8(m6800.a);
            
        }
    };

    /* is this a legal instruction? */
    /* $87 STA immediate -**0- */
    public opcode sta_im = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(A);
            /*TODO*///IMM8;
            /*TODO*///WM(EAD, A);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $88 EORA immediate -**0- */
    public opcode eora_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.a ^= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $89 ADCA immediate ***** */
    public opcode adca_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.a + t + (m6800.cc & 0x01)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a,t,r);
            SET_H(m6800.a,t,r);
            m6800.a = r & 0xFF;
        }
    };

    /* $8a ORA immediate -**0- */
    public opcode ora_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.a |= t; //TODO should unsigned it??
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $8b ADDA immediate ***** */
    public opcode adda_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a,t,r);
            SET_H(m6800.a,t,r);
            m6800.a = r & 0xFF;
        }
    };

    /* $8c CMPX immediate -***- */
    public opcode cmpx_im = new opcode() {
        public void handler() {
            int r,d;
            int b=IMMWORD();
            d = m6800.x;
            r = (d - b); //&0xFFFF;//should be unsigned?
            CLR_NZV();
            SET_NZ16(r);
            SET_V16(d, b, r);
        }
    };

    /* $8c CPX immediate -**** (6803) */
    public opcode cpx_im = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b=IMMWORD();
            d = m6800.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            
        }
    };


    /* $8d BSR ----- */
    public opcode bsr = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            PUSHWORD(m6800.pc);
            m6800.pc = m6800.pc + (byte)t & 0xFFFF; 
            CHANGE_PC();
        }
    };

    /* $8e LDS immediate -**0- */
    public opcode lds_im = new opcode() {
        public void handler() {
            m6800.s=IMMWORD();
            CLR_NZV();
            SET_NZ16(m6800.s);
            
        }
    };

    /* $8f STS immediate -**0- */
    public opcode sts_im = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(S);
            /*TODO*///IMM16;
            /*TODO*///WM16(EAD,  & m6808.s);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $90 SUBA direct ?**** */
    public opcode suba_di = new opcode() {
        public void handler() {
            /*UINT16*/int t, r;
            t=DIRBYTE();
            r = (m6800.a - t)&0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a, t, r);
            m6800.a = r &0xFF;
            
        }
    };

    /* $91 CMPA direct ?**** */
    public opcode cmpa_di = new opcode() {
        public void handler() {
            int t,r;
            t=DIRBYTE();
            r = (m6800.a - t) &0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a,t,r);
        }
    };

    /* $92 SBCA direct ?**** */
    public opcode sbca_di = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///DIRBYTE(t);
            /*TODO*///r = A - t - (CC & 0x01);
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $93 SUBD direct -**** */
    public opcode subd_di = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///DIRWORD(b);
            /*TODO*///d = D;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS16(d, b.d, r);
            /*TODO*///D = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $94 ANDA direct -**0- */
    public opcode anda_di = new opcode() {
        public void handler() {
            int t = DIRBYTE();
            m6800.a &= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $95 BITA direct -**0- */
    public opcode bita_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE();
            r = m6800.a & t;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    /* $96 LDA direct -**0- */
    public opcode lda_di = new opcode() {
        public void handler() {
            m6800.a = DIRBYTE();
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $97 STA direct -**0- */
    public opcode sta_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.a);
            DIRECT();
            WM(ea, m6800.a);
        }
    };

    /* $98 EORA direct -**0- */
    public opcode eora_di = new opcode() {
        public void handler() {
            int t=DIRBYTE();
            m6800.a ^= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $99 ADCA direct ***** */
    public opcode adca_di = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///DIRBYTE(t);
            /*TODO*///r = A + t + (CC & 0x01);
            /*TODO*///CLR_HNZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///SET_H(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $9a ORA direct -**0- */
    public opcode ora_di = new opcode() {
        public void handler() {
            int t= DIRBYTE();
            m6800.a |= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $9b ADDA direct ***** */
    public opcode adda_di = new opcode() {
        public void handler() {
            /*UINT16*/int t, r;
            t=DIRBYTE();
            r = (m6800.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a, t, r);
            SET_H(m6800.a, t, r);
            m6800.a = r & 0xFF;
            
        }
    };

    /* $9c CMPX direct -***- */
    public opcode cmpx_di = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///DIRWORD(b);
            /*TODO*///d = X;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(r);
            /*TODO*///SET_V16(d, b.d, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $9c CPX direct -**** (6803) */
    public opcode cpx_di = new opcode() {
        public void handler() {
            /*UINT32*/int r, d;
            int b;
            b=DIRWORD();
            d = m6800.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
        }
    };

    /* $9d JSR direct ----- */
    public opcode jsr_di = new opcode() {
        public void handler() {
            DIRECT();
            PUSHWORD(m6800.pc);
            m6800.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    /* $9e LDS direct -**0- */
    public opcode lds_di = new opcode() {
        public void handler() {
            /*TODO*///DIRWORD(m6808.s);
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(S);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $9f STS direct -**0- */
    public opcode sts_di = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(S);
            /*TODO*///DIRECT;
            /*TODO*///WM16(EAD,  & m6808.s);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $a0 SUBA indexed ?**** */
    public opcode suba_ix = new opcode() {
        public void handler() {
            int t,r;
            t = IDXBYTE();
            r = (m6800.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a,t,r);
            m6800.a = r & 0xFF;
        }
    };

    /* $a1 CMPA indexed ?**** */
    public opcode cmpa_ix = new opcode() {
        public void handler() {
            /*UINT16*/int t,r;
            t = IDXBYTE();
            r = (m6800.a - t) &0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.a,t,r);
        }
    };

    /* $a2 SBCA indexed ?**** */
    public opcode sbca_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = A - t - (CC & 0x01);
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $a3 SUBD indexed -**** */
    public opcode subd_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///IDXWORD(b);
            /*TODO*///d = D;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS16(d, b.d, r);
            /*TODO*///D = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $a4 ANDA indexed -**0- */
    public opcode anda_ix = new opcode() {
        public void handler() {
            /*UINT8*/int t;
            t=IDXBYTE();
            m6800.a &= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
            
        }
    };

    /* $a5 BITA indexed -**0- */
    public opcode bita_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = A & t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $a6 LDA indexed -**0- */
    public opcode lda_ix = new opcode() {
        public void handler() {
            m6800.a=IDXBYTE();
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $a7 STA indexed -**0- */
    public opcode sta_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.a);
            INDEXED();
            WM(ea, m6800.a);
        }
    };

    /* $a8 EORA indexed -**0- */
    public opcode eora_ix = new opcode() {
        public void handler() {
            /*UINT8*/int t;
            t=IDXBYTE();
            m6800.a ^= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $a9 ADCA indexed ***** */
    public opcode adca_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = A + t + (CC & 0x01);
            /*TODO*///CLR_HNZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///SET_H(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $aa ORA indexed -**0- */
    public opcode ora_ix = new opcode() {
        public void handler() {
            int t;
            t=IDXBYTE();
            m6800.a |= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
        }
    };

    /* $ab ADDA indexed ***** */
    public opcode adda_ix = new opcode() {
        public void handler() {
            int t,r;
            t = IDXBYTE();
            r = (m6800.a + t) &0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a,t,r);
            SET_H(m6800.a,t,r);
            m6800.a = r &0xFF;
        }
    };

    /* $ac CMPX indexed -***- */
    public opcode cmpx_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///IDXWORD(b);
            /*TODO*///d = X;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(r);
            /*TODO*///SET_V16(d, b.d, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $ac CPX indexed -**** (6803)*/
    public opcode cpx_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///IDXWORD(b);
            /*TODO*///d = X;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS16(d, b.d, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $ad JSR indexed ----- */
    public opcode jsr_ix = new opcode() {
        public void handler() {
            INDEXED();
            PUSHWORD(m6800.pc);
            m6800.pc = ea;
            CHANGE_PC();
        }
    };

    /* $ae LDS indexed -**0- */
    public opcode lds_ix = new opcode() {
        public void handler() {
           m6800.s= IDXWORD();
            CLR_NZV();
            SET_NZ16(m6800.s);
        }
    };

    /* $af STS indexed -**0- */
    public opcode sts_ix = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(S);
            /*TODO*///INDEXED;
            /*TODO*///WM16(EAD,  & m6808.s);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b0 SUBA extended ?**** */
    public opcode suba_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = A - t;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b1 CMPA extended ?**** */
    public opcode cmpa_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = A - t;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b2 SBCA extended ?**** */
    public opcode sbca_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = A - t - (CC & 0x01);
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(A, t, r);
            /*TODO*///A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b3 SUBD extended -**** */
    public opcode subd_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///EXTWORD(b);
            /*TODO*///d = D;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS16(d, b.d, r);
            /*TODO*///D = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b4 ANDA extended -**0- */
    public opcode anda_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t;
            /*TODO*///EXTBYTE(t);
            /*TODO*///A &= t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(A);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b5 BITA extended -**0- */
    public opcode bita_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///EXTBYTE(t);
            /*TODO*///r = A & t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b6 LDA extended -**0- */
    public opcode lda_ex = new opcode() {
        public void handler() {
            m6800.a=EXTBYTE();
            CLR_NZV();
            SET_NZ8(m6800.a);
 
        }
    };

    /* $b7 STA extended -**0- */
    public opcode sta_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.a);
            EXTENDED();
            WM(ea, m6800.a);
            
        }
    };

    /* $b8 EORA extended -**0- */
    public opcode eora_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t;
            /*TODO*///EXTBYTE(t);
            /*TODO*///A ^= t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(A);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $b9 ADCA extended ***** */
    public opcode adca_ex = new opcode() {
        public void handler() {
           /*TODO*/// UINT16 t, r;
           /*TODO*/// EXTBYTE(t);
           /*TODO*/// r = A + t + (CC & 0x01);
           /*TODO*/// CLR_HNZVC;
           /*TODO*/// SET_FLAGS8(A, t, r);
           /*TODO*/// SET_H(A, t, r);
           /*TODO*/// A = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $ba ORA extended -**0- */
    public opcode ora_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE();
            m6800.a |= t;
            CLR_NZV();
            SET_NZ8(m6800.a);
            
        }
    };

    /* $bb ADDA extended ***** */
    public opcode adda_ex = new opcode() {
        public void handler() {
            int t, r;
            t=EXTBYTE();
            r = (m6800.a + t)&0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.a, t, r);
            SET_H(m6800.a, t, r);
            m6800.a = r & 0xFF;
        }
    };

    /* $bc CMPX extended -***- */
    public opcode cmpx_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///EXTWORD(b);
            /*TODO*///d = X;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(r);
            /*TODO*///SET_V16(d, b.d, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $bc CPX extended -**** (6803) */
    public opcode cpx_ex = new opcode() {
        public void handler() {
            /*TODO*///UINT32 r, d;
            /*TODO*///PAIR b;
            /*TODO*///EXTWORD(b);
            /*TODO*///d = X;
            /*TODO*///r = d - b.d;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS16(d, b.d, r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $bd JSR extended ----- */
    public opcode jsr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            PUSHWORD(m6800.pc);
            m6800.pc = ea & 0xFFFF;
            CHANGE_PC();
        }
    };

    /* $be LDS extended -**0- */
    public opcode lds_ex = new opcode() {
        public void handler() {
            /*TODO*///EXTWORD(m6808.s);
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(S);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $bf STS extended -**0- */
    public opcode sts_ex = new opcode() {
        public void handler() {
           /*TODO*/// CLR_NZV;
           /*TODO*/// SET_NZ16(S);
            /*TODO*///EXTENDED;
            /*TODO*///WM16(EAD,  & m6808.s);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $c0 SUBB immediate ?**** */
    public opcode subb_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $c1 CMPB immediate ?**** */
    public opcode cmpb_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
        }
    };

    /* $c2 SBCB immediate ?**** */
    public opcode sbcb_im = new opcode() {
        public void handler() {
            /*UINT16*/int t, r;
            t=IMMBYTE();
            r = (m6800.b - t - (m6800.cc & 0x01))&0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b, t, r);
            m6800.b = r & 0xFF;
        }
    };

    /* $c3 ADDD immediate -**** */
    public opcode addd_im = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b=IMMWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
        }
    };

    /* $c4 ANDB immediate -**0- */
    public opcode andb_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.b &= t;//should be unsigned?
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $c5 BITB immediate -**0- */
    public opcode bitb_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = m6800.b & t;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    /* $c6 LDB immediate -**0- */
    public opcode ldb_im = new opcode() {
        public void handler() {
            m6800.b=IMMBYTE();
            CLR_NZV();
            SET_NZ8(m6800.b);
            
        }
    };

    /* is this a legal instruction? */
    /* $c7 STB immediate -**0- */
    public opcode stb_im = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(B);
            /*TODO*///IMM8;
            /*TODO*///WM(EAD, B);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $c8 EORB immediate -**0- */
    public opcode eorb_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.b ^= t;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $c9 ADCB immediate ***** */
    public opcode adcb_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.b + t + (m6800.cc & 0x01)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $ca ORB immediate -**0- */
    public opcode orb_im = new opcode() {
        public void handler() {
            int t=IMMBYTE();
            m6800.b |= t; //TODO should unsigned it??
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $cb ADDB immediate ***** */
    public opcode addb_im = new opcode() {
        public void handler() {
            int t,r;
            t=IMMBYTE();
            r = (m6800.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $CC LDD immediate -**0- */
    public opcode ldd_im = new opcode() {
        public void handler() {
            int tmp =IMMWORD();
            setDreg(tmp);
            CLR_NZV();
            SET_NZ16(tmp);
        }
    };

    /* is this a legal instruction? */
    /* $cd STD immediate -**0- */
    public opcode std_im = new opcode() {
        public void handler() {
            /*TODO*///IMM16;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(D);
            /*TODO*///WM16(EAD,  & m6808.d);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $ce LDX immediate -**0- */
    public opcode ldx_im = new opcode() {
        public void handler() {
            m6800.x=IMMWORD();
            CLR_NZV();
            SET_NZ16(m6800.x);
            
        }
    };

    /* $cf STX immediate -**0- */
    public opcode stx_im = new opcode() {
        public void handler() {
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ16(X);
            /*TODO*///IMM16;
            /*TODO*///WM16(EAD,  & m6808.x);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $d0 SUBB direct ?**** */
    public opcode subb_di = new opcode() {
        public void handler() {
            int t,r;
            t=DIRBYTE();
            r = (m6800.b - t) &0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $d1 CMPB direct ?**** */
    public opcode cmpb_di = new opcode() {
        public void handler() {
            int t,r;
            t=DIRBYTE();
            r = (m6800.b - t) &0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
        }
    };

    /* $d2 SBCB direct ?**** */
    public opcode sbcb_di = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///DIRBYTE(t);
            /*TODO*///r = B - t - (CC & 0x01);
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(B, t, r);
            /*TODO*///B = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $d3 ADDD direct -**** */
    public opcode addd_di = new opcode() {
        public void handler() {
            /*UINT32*/int r, d;
            int b;
            b=DIRWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);   
        }
    };

    /* $d4 ANDB direct -**0- */
    public opcode andb_di = new opcode() {
        public void handler() {
            int t=DIRBYTE();
            m6800.b &= t; //TODO should be unsigned?
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $d5 BITB direct -**0- */
    public opcode bitb_di = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t, r;
            /*TODO*///DIRBYTE(t);
            /*TODO*///r = B & t;
            /*TODO*///CLR_NZV;
            /*TODO*///SET_NZ8(r);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $d6 LDB direct -**0- */
    public opcode ldb_di = new opcode() {
        public void handler() {
            m6800.b=DIRBYTE();
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $d7 STB direct -**0- */
    public opcode stb_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.b);
            DIRECT();
            WM(ea, m6800.b);
        }
    };

    /* $d8 EORB direct -**0- */
    public opcode eorb_di = new opcode() {
        public void handler() {
            /*TODO*///UINT8 t;
            /*TODO*///DIRBYTE(t);
            /*TODO*///B ^= t;
            /*TODO*///;
            /*TODO*///SET_NZ8(B);
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $d9 ADCB direct ***** */
    public opcode adcb_di = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///DIRBYTE(t);
           /*TODO*/// r = B + t + (CC & 0x01);
            /*TODO*///CLR_HNZVC;
            /*TODO*///SET_FLAGS8(B, t, r);
            /*TODO*///SET_H(B, t, r);
            /*TODO*///B = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $da ORB direct -**0- */
    public opcode orb_di = new opcode() {
        public void handler() {
            int t=	DIRBYTE();
            m6800.b |= t;  //todo check if it should be unsigned
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $db ADDB direct ***** */
    public opcode addb_di = new opcode() {
        public void handler() {
            int t,r;
            t=DIRBYTE();
            r = (m6800.b + t) &0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $dc LDD direct -**0- */
    public opcode ldd_di = new opcode() {
        public void handler() {
            int temp=	DIRWORD();
            setDreg(temp);
            CLR_NZV();
            SET_NZ16(temp);
        }
    };

    /* $dd STD direct -**0- */
    public opcode std_di = new opcode() {
        public void handler() {
            DIRECT();
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);   
            WM16(ea,temp);
        }
    };

    /* $de LDX direct -**0- */
    public opcode ldx_di = new opcode() {
        public void handler() {
            m6800.x=DIRWORD();
            CLR_NZV();
            SET_NZ16(m6800.x);
        }
    };

    /* $dF STX direct -**0- */
    public opcode stx_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6800.x);
            DIRECT();
            WM16(ea,m6800.x);
        }
    };

    /* $e0 SUBB indexed ?**** */
    public opcode subb_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = B - t;
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(B, t, r);
            /*TODO*///B = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $e1 CMPB indexed ?**** */
    public opcode cmpb_ix = new opcode() {
        public void handler() {
            /*UINT16*/int t, r;
            t=IDXBYTE();
            r = (m6800.b - t)&0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b, t, r);
            
        }
    };

    /* $e2 SBCB indexed ?**** */
    public opcode sbcb_ix = new opcode() {
        public void handler() {
            /*TODO*///UINT16 t, r;
            /*TODO*///IDXBYTE(t);
            /*TODO*///r = B - t - (CC & 0x01);
            /*TODO*///CLR_NZVC;
            /*TODO*///SET_FLAGS8(B, t, r);
            /*TODO*///B = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $e3 ADDD indexed -**** */
    public opcode addd_ix = new opcode() {
        public void handler() {
            int r, d;
            int b;
            b=IDXWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r);
            
        }
    };

    /* $e4 ANDB indexed -**0- */
    public opcode andb_ix = new opcode() {
        public void handler() {
            int t =IDXBYTE();
            m6800.b &= t;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $e5 BITB indexed -**0- */
    public opcode bitb_ix = new opcode() {
        public void handler() {
            int t, r;
            t=IDXBYTE();
            r = m6800.b & t;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    /* $e6 LDB indexed -**0- */
    public opcode ldb_ix = new opcode() {
        public void handler() {
            m6800.b=IDXBYTE();
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $e7 STB indexed -**0- */
    public opcode stb_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.b);
            INDEXED();
            WM(ea, m6800.b);
        }
    };

    /* $e8 EORB indexed -**0- */
    public opcode eorb_ix = new opcode() {
        public void handler() {
            int t =IDXBYTE();
            m6800.b ^= t;
            CLR_NZV();
            SET_NZ8(m6800.b);

        }
    };

    /* $e9 ADCB indexed ***** */
    public opcode adcb_ix = new opcode() {
        public void handler() {
            int t,r;
            t = IDXBYTE();
            r = (m6800.b + t + (m6800.cc & 0x01)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $ea ORB indexed -**0- */
    public opcode orb_ix = new opcode() {
        public void handler() {
            int t;
            t=IDXBYTE();
            m6800.b |= t;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $eb ADDB indexed ***** */
    public opcode addb_ix = new opcode() {
        public void handler() {
            int t,r;
            t = IDXBYTE();
            r = (m6800.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $ec LDD indexed -**0- */
    public opcode ldd_ix = new opcode() {
        public void handler() {
            int temp=IDXWORD();
            setDreg(temp);
            CLR_NZV(); 
            SET_NZ16(temp);
        }
    };

    /* $ed STD indexed -**0- */
    public opcode std_ix = new opcode() {
        public void handler() {
            INDEXED();
            CLR_NZV();
            int temp=getDreg();
            SET_NZ16(temp);
            WM16(ea,temp);
        }
    };

    /* $ee LDX indexed -**0- */
    public opcode ldx_ix = new opcode() {
        public void handler() {
            m6800.x=IDXWORD();
            CLR_NZV();
            SET_NZ16(m6800.x);
        }
    };

    /* $ef STX indexed -**0- */
    public opcode stx_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6800.x);
            INDEXED();
            WM16(ea,m6800.x);
        }
    };

    /* $f0 SUBB extended ?**** */
    public opcode subb_ex = new opcode() {
        public void handler() {
            int  t,r;
            t=EXTBYTE();
            r = (m6800.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $f1 CMPB extended ?**** */
    public opcode cmpb_ex = new opcode() {
        public void handler() {
            int t,r;
            t=EXTBYTE();
            r = m6800.b - t;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
        }
    };

    /* $f2 SBCB extended ?**** */
    public opcode sbcb_ex = new opcode() {
        public void handler() {
            int t,r;
            t = EXTBYTE();
            r = (m6800.b - t - (m6800.cc & 0x01))&0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $f3 ADDD extended -**** */
    public opcode addd_ex = new opcode() {
        public void handler() {
            int r,d;
            int b=EXTWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d,b,r);
            setDreg(r);
        }
    };

    /* $f4 ANDB extended -**0- */
    public opcode andb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            m6800.b &=t;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $f5 BITB extended -**0- */
    public opcode bitb_ex = new opcode() {
        public void handler() {
            int t,r;
            t =EXTBYTE();
            r = m6800.b & t;
            CLR_NZV(); 
            SET_NZ8(r);
        }
    };

    /* $f6 LDB extended -**0- */
    public opcode ldb_ex = new opcode() {
        public void handler() {
            m6800.b=EXTBYTE();
            CLR_NZV();
            SET_NZ8(m6800.b);    
        }
    };

    /* $f7 STB extended -**0- */
    public opcode stb_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(m6800.b);
            EXTENDED();
            WM(ea,m6800.b);
        }
    };

    /* $f8 EORB extended -**0- */
    public opcode eorb_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE();
            m6800.b ^= t;
            CLR_NZV();
            SET_NZ8(m6800.b);
        }
    };

    /* $f9 ADCB extended ***** */
    public opcode adcb_ex = new opcode() {
        public void handler() {
        /*TODO*///    UINT16 t, r;
        /*TODO*///    EXTBYTE(t);
        /*TODO*///    r = B + t + (CC & 0x01);
        /*TODO*///    CLR_HNZVC;
        /*TODO*///    SET_FLAGS8(B, t, r);
        /*TODO*///    SET_H(B, t, r);
        /*TODO*///    B = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $fa ORB extended -**0- */
    public opcode orb_ex = new opcode() {
        public void handler() {
            int t=EXTBYTE();
            m6800.b |= t;
            CLR_NZV();
            SET_NZ8(m6800.b);
            
        }
    };

    /* $fb ADDB extended ***** */
    public opcode addb_ex = new opcode() {
        public void handler() {
            int  t,r;
            t=EXTBYTE();
            r = (m6800.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(m6800.b,t,r);
            SET_H(m6800.b,t,r);
            m6800.b = r & 0xFF;
        }
    };

    /* $fc LDD extended -**0- */
    public opcode ldd_ex = new opcode() {
        public void handler() {
            int temp=EXTWORD();
            setDreg(temp);
            CLR_NZV();
            SET_NZ16(temp);
        }
    };

    /* $fc ADDX extended -****    NSC8105 only.  Flags are a guess */
    public opcode addx_ex = new opcode() {
        public void handler() {
        /*TODO*///    UINT32 r, d;
        /*TODO*///    PAIR b;
        /*TODO*///    EXTWORD(b);
        /*TODO*///    d = X;
        /*TODO*///    r = d + b.d;
        /*TODO*///    CLR_NZVC;
        /*TODO*///    SET_FLAGS16(d, b.d, r);
        /*TODO*///    X = r;
            throw new UnsupportedOperationException("Unsupported");
        }
    };

    /* $fd STD extended -**0- */
    public opcode std_ex = new opcode() {
        public void handler() {
            EXTENDED();
            CLR_NZV();
            int temp = getDreg();
            SET_NZ16(temp);
            WM16(ea,temp);
        }
    };

    /* $fe LDX extended -**0- */
    public opcode ldx_ex = new opcode() {
        public void handler() {
            m6800.x=EXTWORD();
            CLR_NZV();
            SET_NZ16(m6800.x);
        }
    };

    /* $ff STX extended -**0- */
    public opcode stx_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(m6800.x);
            EXTENDED();
            WM16(ea,m6800.x);
        }
    };
    opcode[] m6800_insn = {
        illegal, nop, illegal, illegal, illegal, illegal, tap, tpa,
        inx, dex, clv, sev, clc, sec, cli, sei,
        sba, cba, illegal, illegal, illegal, illegal, tab, tba,
        illegal, daa, illegal, aba, illegal, illegal, illegal, illegal,
        bra, brn, bhi, bls, bcc, bcs, bne, beq,
        bvc, bvs, bpl, bmi, bge, blt, bgt, ble,
        tsx, ins, pula, pulb, des, txs, psha, pshb,
        illegal, rts, illegal, rti, illegal, illegal, wai, swi,
        nega, illegal, illegal, coma, lsra, illegal, rora, asra,
        asla, rola, deca, illegal, inca, tsta, illegal, clra,
        negb, illegal, illegal, comb, lsrb, illegal, rorb, asrb,
        aslb, rolb, decb, illegal, incb, tstb, illegal, clrb,
        neg_ix, illegal, illegal, com_ix, lsr_ix, illegal, ror_ix, asr_ix,
        asl_ix, rol_ix, dec_ix, illegal, inc_ix, tst_ix, jmp_ix, clr_ix,
        neg_ex, illegal, illegal, com_ex, lsr_ex, illegal, ror_ex, asr_ex,
        asl_ex, rol_ex, dec_ex, illegal, inc_ex, tst_ex, jmp_ex, clr_ex,
        suba_im, cmpa_im, sbca_im, illegal, anda_im, bita_im, lda_im, sta_im,
        eora_im, adca_im, ora_im, adda_im, cmpx_im, bsr, lds_im, sts_im,
        suba_di, cmpa_di, sbca_di, illegal, anda_di, bita_di, lda_di, sta_di,
        eora_di, adca_di, ora_di, adda_di, cmpx_di, jsr_di, lds_di, sts_di,
        suba_ix, cmpa_ix, sbca_ix, illegal, anda_ix, bita_ix, lda_ix, sta_ix,
        eora_ix, adca_ix, ora_ix, adda_ix, cmpx_ix, jsr_ix, lds_ix, sts_ix,
        suba_ex, cmpa_ex, sbca_ex, illegal, anda_ex, bita_ex, lda_ex, sta_ex,
        eora_ex, adca_ex, ora_ex, adda_ex, cmpx_ex, jsr_ex, lds_ex, sts_ex,
        subb_im, cmpb_im, sbcb_im, illegal, andb_im, bitb_im, ldb_im, stb_im,
        eorb_im, adcb_im, orb_im, addb_im, illegal, illegal, ldx_im, stx_im,
        subb_di, cmpb_di, sbcb_di, illegal, andb_di, bitb_di, ldb_di, stb_di,
        eorb_di, adcb_di, orb_di, addb_di, illegal, illegal, ldx_di, stx_di,
        subb_ix, cmpb_ix, sbcb_ix, illegal, andb_ix, bitb_ix, ldb_ix, stb_ix,
        eorb_ix, adcb_ix, orb_ix, addb_ix, illegal, illegal, ldx_ix, stx_ix,
        subb_ex, cmpb_ex, sbcb_ex, illegal, andb_ex, bitb_ex, ldb_ex, stb_ex,
        eorb_ex, adcb_ex, orb_ex, addb_ex, illegal, illegal, ldx_ex, stx_ex
    };

    public static abstract interface opcode {

        public abstract void handler();
    }
}
