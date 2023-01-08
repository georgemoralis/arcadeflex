/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.m6800;

//cpu imports
import static arcadeflex.v036.cpu.m6800.m6800Η.*;
import static arcadeflex.v036.cpu.m6800.m6800ops.*;
import static arcadeflex.v036.cpu.m6800.m6800tbl.*;
import arcadeflex.v036.generic.funcPtr.IrqCallbackHandlerPtr;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.memoryH.*;

public class m6800 extends cpu_interface {

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
        m6800_ICount[0] = 50000;
    }

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
            L = ((L << 8) & 0xFF) | (val & 0xFF);
            D = (H << 16) | L;
        }

        public void SetLH(long val)//insure recheck
        {
            L = ((val << 8) & 0xFF) | (L & 0xFF);
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
        public /*PAIR*/ int ppc;
        /* Previous program counter */

        public /*PAIR*/ int pc;
        /* Program counter */

        public int /*PAIR*/ s;
        /* Stack pointer */

        public int/*PAIR*/ x;
        /* Index register */

        public int a;
        public int b;//public PAIR d;				/* Accumulators */

        public int /*UINT8*/ cc;
        /* Condition codes */

        public int /*UINT8*/ wai_state;
        /* WAI opcode state ,(or sleep opcode state) */

        public int /*UINT8*/ nmi_state;
        /* NMI line state */

        public int[] /*UINT8*/ irq_state = new int[2];
        /* IRQ line state [IRQ1,TIN] */

        public int /*UINT8*/ ic_eddge;
        /* InputCapture eddge , b.0=fall,b.1=raise */

        public IrqCallbackHandlerPtr irq_callback;
        public int extra_cycles;
        /* cycles used for interrupts */

        public opcode[] insn;
        /* instruction table */
 /*const UINT8*/ public int[] cycles;
        /* clock cycle of instruction table */
 /* internal registers */

        public int /*UINT8*/ port1_ddr;
        public int /*UINT8*/ port2_ddr;
        public int /*UINT8*/ port1_data;
        public int /*UINT8*/ port2_data;
        public int /*UINT8*/ tcsr;
        /* Timer Control and Status Register */

        public int /*UINT8*/ pending_tcsr;
        /* pending IRQ flag for clear IRQflag process */

        public int /*UINT8*/ irq2;
        /* IRQ2 flags */

        public int /*UINT8*/ ram_ctrl;
        public int counterL;
        public int counterH;/* free running counter */

        public int output_compareL;
        public int output_compareH;/* output compare       */

        public int u16_input_capture;/* input capture        */

        public int timer_overL;
        public int timer_overH;
    }
    public static m6800_Regs m6800 = new m6800_Regs();

    public static long getCounterReg() {
        return (m6800.counterH << 16 | m6800.counterL) & 0xFFFFFFFFL;
    }

    public static void setCounterReg(long reg) {
        m6800.counterH = (int) (reg >>> 16 & 0xFFFF);
        m6800.counterL = (int) (reg & 0xFFFF);
    }

    public static long getOutputReg() {
        return (m6800.output_compareH << 16 | m6800.output_compareL) & 0xFFFFFFFFL;
    }

    public static void setOutputReg(int reg) {
        m6800.output_compareH = (int) (reg >>> 16 & 0xFFFF);
        m6800.output_compareL = (int) (reg & 0xFFFF);
    }

    public static long getTimeOverReg() {
        return (m6800.timer_overH << 16 | m6800.timer_overL) & 0xFFFFFFFFL;
    }

    public static void setTimeOverReg(int reg) {
        m6800.timer_overH = (int) (reg >>> 16 & 0xFFFF);
        m6800.timer_overL = (int) (reg & 0xFFFF);
    }

    /* point of next timer event */
    static /*UINT32*/ long u32_timer_next;
    public static int ea;
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
    public static void CLR_HNZVC() {
        m6800.cc &= 0xd0;
    }

    public static void CLR_NZV() {
        m6800.cc &= 0xf1;
    }

    public static void CLR_HNZC() {
        m6800.cc &= 0xd2;
    }

    public static void CLR_NZVC() {
        m6800.cc &= 0xf0;
    }

    public static void CLR_Z() {
        m6800.cc &= 0xfb;
    }

    public static void CLR_NZC() {
        m6800.cc &= 0xf2;
    }

    public static void CLR_ZC() {
        m6800.cc &= 0xfa;
    }

    public static void CLR_C() {
        m6800.cc &= 0xfe;
    }

    static int flags8i[]
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
    static int flags8d[]
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

    public static void SET_FLAGS8I(int a) {
        m6800.cc |= flags8i[(a) & 0xff];
    }

    public static void SET_FLAGS8D(int a) {
        m6800.cc |= flags8d[(a) & 0xff];
    }

    /* combos */
    public static void SET_NZ8(int a) {
        SET_N8(a);
        SET_Z(a);
    }

    public static void SET_NZ16(int a) {
        SET_N16(a);
        SET_Z(a);
    }

    public static void SET_FLAGS8(int a, int b, int r) {
        SET_N8(r);
        SET_Z8(r);
        SET_V8(a, b, r);
        SET_C8(r);
    }

    public static void SET_FLAGS16(int a, int b, int r) {
        SET_N16(r);
        SET_Z16(r);
        SET_V16(a, b, r);
        SET_C16(r);
    }

    /* macros for CC -- CC bits affected should be reset before calling */
    public static void SET_Z(int a) {
        if (a == 0) {
            SEZ();
        }
    }

    public static void SET_Z8(int a) {
        SET_Z(a & 0xFF);
    }

    public static void SET_Z16(int a) {
        SET_Z(a & 0xFFFF);
    }

    public static void SET_N8(int a) {
        m6800.cc |= ((a & 0x80) >> 4);
    }

    public static void SET_N16(int a) {
        m6800.cc |= ((a & 0x8000) >> 12);
    }

    public static void SET_H(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r) & 0x10) << 1);
    }

    public static void SET_C8(int a) {
        m6800.cc |= ((a & 0x100) >> 8);
    }

    public static void SET_C16(int a) {
        m6800.cc |= ((a & 0x10000) >> 16);
    }

    public static void SET_V8(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x80) >> 6);
    }

    public static void SET_V16(int a, int b, int r) {
        m6800.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x8000) >> 14);
    }

    public static int RM(int addr) {
        return (cpu_readmem16(addr) & 0xFF);
    }

    public static void WM(int addr, int value) {
        cpu_writemem16(addr, value & 0xFF);
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
        return i & 0xFFFF;
    }

    static void WM16(int addr, int reg) {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }

    public static char M_RDOP(int addr) {
        return cpu_readop(addr);
    }

    public static char M_RDOP_ARG(int addr) {
        return cpu_readop_arg(addr);
    }

    public static int IMMBYTE() {
        int reg = M_RDOP_ARG(m6800.pc);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
    }

    public static int IMMWORD() {
        int reg = ((M_RDOP_ARG(m6800.pc) << 8) | M_RDOP_ARG((m6800.pc + 1)) & 0xffff);
        m6800.pc = (m6800.pc) + 2 & 0xFFFF;
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
        m6800.s = m6800.s + 1 & 0xFFFF;
        int b = RM(m6800.s);
        return b & 0xFF;
    }

    public static int PULLWORD()//S++; w.d = RM(SD)<<8; S++; w.d |= RM(SD)
    {
        m6800.s = m6800.s + 1 & 0xFFFF;
        int w = RM(m6800.s) << 8;
        m6800.s = m6800.s + 1 & 0xFFFF;
        w |= RM(m6800.s);

        return w & 0xFFFF;
    }

    public static void CHANGE_PC() {
        change_pc16(m6800.pc & 0xFFFF);//ensure it's 16bit just in case
    }

    /* macros to set status flags */
    public static void SEC() {
        m6800.cc |= 0x01l;
    }

    public static void CLC() {
        m6800.cc &= 0xfe;
    }

    public static void SEZ() {
        m6800.cc |= 0x04;
    }

    public static void CLZ() {
        m6800.cc &= 0xfb;
    }

    public static void SEN() {
        m6800.cc |= 0x08;
    }

    public static void CLN() {
        m6800.cc &= 0xf7;
    }

    public static void SEV() {
        m6800.cc |= 0x02;
    }

    public static void CLV() {
        m6800.cc &= 0xfd;
    }

    public static void SEH() {
        m6800.cc |= 0x20;
    }

    public static void CLH() {
        m6800.cc &= 0xdf;
    }

    public static void SEI() {
        m6800.cc |= 0x10;
    }

    public static void CLI() {
        m6800.cc &= ~0x10;
    }

    public static void DIRECT() {
        ea = IMMBYTE();
    }

    public static void EXTENDED() {
        ea = IMMWORD();
    }

    public static void INDEXED() {
        ea = m6800.x + (M_RDOP_ARG(m6800.pc) & 0xFF);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
    }

    public static int DIRBYTE() {
        DIRECT();
        return RM(ea);
    }

    public static int DIRWORD() {
        DIRECT();
        return RM16(ea);
    }

    public static int EXTBYTE() {
        EXTENDED();
        return RM(ea);
    }

    public static int EXTWORD() {
        EXTENDED();
        return RM16(ea);
    }

    public static int IDXBYTE() {
        INDEXED();
        return RM(ea);
    }

    public static int IDXWORD() {
        INDEXED();
        return RM16(ea);
    }

    static int getDreg()//compose dreg
    {
        return (m6800.a << 8 | m6800.b) & 0xFFFF;
    }

    static void setDreg(int reg) //write to dreg
    {
        m6800.a = reg >> 8 & 0xFF;
        m6800.b = reg & 0xFF;
    }

    public static void BRANCH(boolean f) {
        int t = IMMBYTE();
        if (f) {
            m6800.pc = (m6800.pc + (byte) t) & 0xFFFF;//TODO check if it has to be better...
            CHANGE_PC();
        }
    }

    public static int NXORV() {
        return ((m6800.cc & 0x08) ^ ((m6800.cc & 0x02) << 2));
    }
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
        u32_timer_next = (getOutputReg() < getTimeOverReg()) ? getOutputReg() : getTimeOverReg();
    }

    public static void MODIFIED_counters() {
        m6800.output_compareH = (m6800.output_compareL >= m6800.counterL) ? m6800.counterH : ((m6800.counterH + 1) & 0xFFFF);
        SET_TIMRE_EVENT();
    }

    /* cleanup high-word of counters */
    public void CLEANUP_conters() {
        m6800.output_compareH = (m6800.output_compareH - m6800.counterH) & 0xFFFF;//OCH -= CTH;									
        m6800.timer_overH = (m6800.timer_overH - m6800.counterH) & 0xFFFF;//TOH -= CTH;									
        m6800.counterH = 0;
        SET_TIMRE_EVENT();
    }

    public static void INCREMENT_COUNTER(int amount) {
        m6800_ICount[0] -= amount;
        setCounterReg(getCounterReg() - amount);//CTD += amount;					
        if (getCounterReg() >= u32_timer_next) {
            check_timer_event();
        }
    }

    public static void EAT_CYCLES() {
        int cycles_to_eat;

        cycles_to_eat = (int) (u32_timer_next - getCounterReg());
        if (cycles_to_eat > m6800_ICount[0]) {
            cycles_to_eat = m6800_ICount[0];
        }
        if (cycles_to_eat > 0) {
            INCREMENT_COUNTER(cycles_to_eat);
        }
    }

    /* check OCI or TOI */
    public static void check_timer_event() {
        /* OCI */
        if (getCounterReg() >= getOutputReg()) {
            m6800.output_compareH = (m6800.output_compareH + 1) & 0xFFFF;//OCH++;	// next IRQ point
            m6800.tcsr |= TCSR_OCF;
            m6800.pending_tcsr |= TCSR_OCF;
            MODIFIED_tcsr();
            if ((m6800.cc & 0x10) == 0 && (m6800.tcsr & TCSR_EOCI) != 0) {
                TAKE_OCI();
            }
        }
        /* TOI */
        if (getCounterReg() >= getTimeOverReg()) {
            m6800.timer_overL = (m6800.timer_overL & 0xFFFF);	// next IRQ point

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
    public static void ONE_MORE_INSN() {
        int ireg;
        m6800.ppc = m6800.pc;
        //CALL_MAME_DEBUG;						
        ireg = M_RDOP(m6800.pc);
        m6800.pc = (m6800.pc + 1) & 0xFFFF;
        m6800.insn[ireg].handler();
        INCREMENT_COUNTER(m6800.cycles[ireg]);
    }

    /* check the IRQ lines for pending interrupts */
    public static void CHECK_IRQ_LINES() {
        if ((m6800.cc & 0x10) == 0) {
            if (m6800.irq_state[M6800_IRQ_LINE] != CLEAR_LINE) {
                /* stanadrd IRQ */

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
        SEI();
        /* IRQ disabled */

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
        setCounterReg(0x0000);
        setOutputReg(0xffff);
        setTimeOverReg(0xffff);
        m6800.ram_ctrl |= 0x40;
    }

    @Override
    public void exit() {
        /* nothing to do */
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
                    sts_im.handler();
                    /* orthogonality */ break;
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
        m6800_Regs regs = new m6800_Regs();
        regs.ppc = m6800.ppc;
        regs.pc = m6800.pc;
        regs.s = m6800.s;
        regs.x = m6800.x;
        regs.a = m6800.a;
        regs.b = m6800.b;
        regs.cc = m6800.cc;
        regs.wai_state = m6800.wai_state;
        regs.nmi_state = m6800.nmi_state;
        regs.irq_state[0] = m6800.irq_state[0];
        regs.irq_state[1] = m6800.irq_state[1];
        regs.ic_eddge = m6800.ic_eddge;
        regs.irq_callback = m6800.irq_callback;
        regs.extra_cycles = m6800.extra_cycles;
        regs.insn = m6800.insn;
        regs.cycles = m6800.cycles;
        regs.port1_ddr = m6800.port1_ddr;
        regs.port2_ddr = m6800.port2_ddr;
        regs.port1_data = m6800.port1_data;
        regs.port2_data = m6800.port2_data;
        regs.tcsr = m6800.tcsr;
        regs.pending_tcsr = m6800.pending_tcsr;
        regs.irq2 = m6800.irq2;
        regs.ram_ctrl = m6800.ram_ctrl;
        regs.counterH = m6800.counterH;
        regs.counterL = m6800.counterL;
        regs.output_compareH = m6800.output_compareH;
        regs.output_compareL = m6800.output_compareL;
        regs.u16_input_capture = m6800.u16_input_capture;
        regs.timer_overH = m6800.timer_overH;
        regs.timer_overL = m6800.timer_overL;
        return regs;
    }

    @Override
    public void set_context(Object reg) {
        m6800_Regs Regs = (m6800_Regs) reg;
        m6800.ppc = Regs.ppc;
        m6800.pc = Regs.pc;
        m6800.s = Regs.s;
        m6800.x = Regs.x;
        m6800.a = Regs.a;
        m6800.b = Regs.b;
        m6800.cc = Regs.cc;
        m6800.wai_state = Regs.wai_state;
        m6800.nmi_state = Regs.nmi_state;
        m6800.irq_state[0] = Regs.irq_state[0];
        m6800.irq_state[1] = Regs.irq_state[1];
        m6800.ic_eddge = Regs.ic_eddge;
        m6800.irq_callback = Regs.irq_callback;
        m6800.extra_cycles = Regs.extra_cycles;
        m6800.insn = Regs.insn;
        m6800.cycles = Regs.cycles;
        m6800.port1_ddr = Regs.port1_ddr;
        m6800.port2_ddr = Regs.port2_ddr;
        m6800.port1_data = Regs.port1_data;
        m6800.port2_data = Regs.port2_data;
        m6800.tcsr = Regs.tcsr;
        m6800.pending_tcsr = Regs.pending_tcsr;
        m6800.irq2 = Regs.irq2;
        m6800.ram_ctrl = Regs.ram_ctrl;
        m6800.counterH = Regs.counterH;
        m6800.counterL = Regs.counterL;
        m6800.output_compareH = Regs.output_compareH;
        m6800.output_compareL = Regs.output_compareL;
        m6800.u16_input_capture = Regs.u16_input_capture;
        m6800.timer_overH = Regs.timer_overH;
        m6800.timer_overL = Regs.timer_overL;
        CHANGE_PC();
        CHECK_IRQ_LINES();
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
        if (m6800.nmi_state == state) {
            return;
        }
        //LOG((errorlog, "M6800#%d set_nmi_line %d ", cpu_getactivecpu(), state));
        m6800.nmi_state = state;
        if (state == CLEAR_LINE) {
            return;
        }

        /* NMI */
        ENTER_INTERRUPT("M6800#%d take NMI\n", 0xfffc);
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        int eddge;

        if (m6800.irq_state[irqline] == state) {
            return;
        }
        //LOG((errorlog, "M6800#%d set_irq_line %d,%d\n", cpu_getactivecpu(), irqline, state));
        m6800.irq_state[irqline] = state;

        switch (irqline) {
            case M6800_IRQ_LINE:
                if (state == CLEAR_LINE) {
                    return;
                }
                break;
            case M6800_TIN_LINE:
                eddge = (state == CLEAR_LINE) ? 2 : 0;
                if (((m6800.tcsr & TCSR_IEDG) ^ (state == CLEAR_LINE ? TCSR_IEDG : 0)) == 0) {
                    return;
                }
                /* active eddge in */
                m6800.tcsr |= TCSR_ICF;
                m6800.pending_tcsr |= TCSR_ICF;
                m6800.u16_input_capture = (int) m6800.counterL;
                MODIFIED_tcsr();
                if ((m6800.cc & 0x10) == 0) {
                    CHECK_IRQ2();
                }
                break;
            default:
                return;
        }
        CHECK_IRQ_LINES();
        /* HJB 990417 */
    }

    @Override
    public void set_irq_callback(IrqCallbackHandlerPtr callback) {
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
        /*TODO*///	/* Layout of the registers in the debugger */
/*TODO*///	static UINT8 m6800_reg_layout[] = {
/*TODO*///		M6800_PC, M6800_S, M6800_CC, M6800_A, M6800_B, M6800_X, -1,
/*TODO*///		M6800_WAI_STATE, M6800_NMI_STATE, M6800_IRQ_STATE, 0
/*TODO*///	};
/*TODO*///
/*TODO*///	/* Layout of the debugger windows x,y,w,h */
/*TODO*///	static UINT8 m6800_win_layout[] = {
/*TODO*///		27, 0,53, 4,	/* register window (top rows) */
/*TODO*///		 0, 0,26,22,	/* disassembler window (left colums) */
/*TODO*///		27, 5,53, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///		27,14,53, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///		 0,23,80, 1,	/* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///
/*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	m6800_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &m6800;
/*TODO*///
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+M6800_A: sprintf(buffer[which], "A:%02X", r->d.b.h); break;
/*TODO*///		case CPU_INFO_REG+M6800_B: sprintf(buffer[which], "B:%02X", r->d.b.l); break;
/*TODO*///		case CPU_INFO_REG+M6800_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6800_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6800_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6800_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
/*TODO*///		case CPU_INFO_REG+M6800_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+M6800_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[M6800_IRQ_LINE]); break;
/*TODO*/////		case CPU_INFO_REG+M6800_TIN_STATE: sprintf(buffer[which], "TIN:%X", r->irq_state[M6800_TIN_LINE]); break;
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->cc & 0x80 ? '?':'.',
/*TODO*///				r->cc & 0x40 ? '?':'.',
/*TODO*///				r->cc & 0x20 ? 'H':'.',
/*TODO*///				r->cc & 0x10 ? 'I':'.',
/*TODO*///				r->cc & 0x08 ? 'N':'.',
/*TODO*///				r->cc & 0x04 ? 'Z':'.',
/*TODO*///				r->cc & 0x02 ? 'V':'.',
/*TODO*///				r->cc & 0x01 ? 'C':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "M6800";
            case CPU_INFO_FAMILY:
                return "Motorola 6800";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "m6800.java";
            case CPU_INFO_CREDITS:
                return "The MAME team.";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)m6800_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: case 6800: return (const char *)m6800_win_layout;
        }
        throw new UnsupportedOperationException("Unsupported");
        /*TODO*///	return buffer[which];
    }

    @Override
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }
}
