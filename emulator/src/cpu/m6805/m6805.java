package cpu.m6805;

import static mame.cpuintrfH.*;
import static mame.memoryH.*;
import static mame.driverH.CPU_M6805;
import static cpu.m6805.m6805H.*;
import static mame.memory.cpu_setOPbase16;
import static arcadeflex.osdepend.*;

public class m6805 extends cpu_interface {

    public static int[] m6805_ICount = new int[1];
    public static final int SUBTYPE_M6805 = 0;
    public static final int SUBTYPE_M68705 = 1;
    public static final int SUBTYPE_HD63705 = 2;

    public m6805() {
        cpu_num = CPU_M6805;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6805_INT_NONE;
        irq_int = M6805_INT_IRQ;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 11;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6805_ICount;
        icount[0] = 50000;
    }

    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    /* 6805 Registers */
    public static class m6805_Regs {

        int subtype;
        /* Which sub-type is being emulated */
        int/*UINT32*/ amask;
        /* Address bus width */
        int/*UINT32*/ sp_mask;
        /* Stack pointer address mask */
        int/*UINT32*/ sp_low;
        /* Stack pointer low water mark (or floor) */
        PAIR pc = new PAIR();
        /* Program counter */
        PAIR s = new PAIR();
        /* Stack pointer */
        char/*UINT8*/ u8_a;
        /* Accumulator */
        char/*UINT8*/ u8_x;
        /* Index register */
        char/*UINT8*/ u8_cc;
        /* Condition codes */

        char/*UINT8*/ u8_pending_interrupts;
        /* MB */
        irqcallbacksPtr irq_callback;
        int[] irq_state = new int[8];
        /* KW Additional lines for HD63705 */
        int nmi_state;
    }

    /* 6805 registers */
    public static m6805_Regs m6805=new m6805_Regs();
    public static PAIR ea = new PAIR();

    /* effective address */
    public static char RM(int Addr) {
        return M6805_RDMEM((Addr) & m6805.amask);
    }

    public static void WM(int Addr, int Value) {
        M6805_WRMEM((Addr) & m6805.amask, Value);
    }

    public static char M_RDOP(int Addr) {
        return M6805_RDOP(Addr);
    }

    public static char M_RDOP_ARG(int Addr) {
        return M6805_RDOP_ARG(Addr);
    }

    /* macros to tweak the PC and SP */
    public static void SP_INC() {
        m6805.s.AddD(1);
        if (m6805.s.D > m6805.sp_mask) {
            m6805.s.SetD(m6805.sp_low);
        }
    }

    public static void SP_DEC() {
        m6805.s.AddD(-1);
        if (m6805.s.D < m6805.sp_low) {
            m6805.s.SetD(m6805.sp_mask);
        }
    }

    /*TODO*///#define SP_ADJUST(s) ( ( (s) & SP_MASK ) | SP_LOW )

    /* macros to access memory */
    public static char IMMBYTE() {
        char b;
        b = M_RDOP_ARG(m6805.pc.D);
        m6805.pc.AddD(1);
        return (char) (b & 0xFF);
    }

    public static void IMMWORD(PAIR w) {
        w.SetD(0);
        w.SetH(M_RDOP_ARG(m6805.pc.D));
        m6805.pc.AddD(1);
        w.SetL(M_RDOP_ARG(m6805.pc.D));
        m6805.pc.AddD(1);
    }

    public static void PUSHBYTE(int b) {
        wr_s_handler_b(b);
    }

    public static void PUSHWORD(PAIR w) {
        wr_s_handler_w(w);
    }

    public static char PULLBYTE() {
        return rd_s_handler_b();
    }

    public static void PULLWORD(PAIR w) {
        rd_s_handler_w(w);
    }

    /* CC masks      H INZC
              7654 3210	*/
    public static final int CFLAG = 0x01;
    public static final int ZFLAG = 0x02;
    public static final int NFLAG = 0x04;
    public static final int IFLAG = 0x08;
    public static final int HFLAG = 0x10;

    public static void CLR_NZ() {
        m6805.u8_cc &= ~(NFLAG | ZFLAG);
    }

    public static void CLR_HNZC() {
        m6805.u8_cc &= ~(HFLAG | NFLAG | ZFLAG | CFLAG);
    }

    public static void CLR_Z() {
        m6805.u8_cc &= ~(ZFLAG);
    }

    public static void CLR_NZC() {
        m6805.u8_cc &= ~(NFLAG | ZFLAG | CFLAG);
    }

    public static void CLR_ZC() {
        m6805.u8_cc &= ~(ZFLAG | CFLAG);
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

    public static void SET_N8(int a) {
        m6805.u8_cc |= ((a & 0x80) >> 5);
    }

    public static void SET_H(int a, int b, int r) {
        m6805.u8_cc |= ((a ^ b ^ r) & 0x10);
    }

    public static void SET_C8(int a) {
        m6805.u8_cc |= ((a & 0x100) >> 8);
    }
    static int flags8i[]
            = /* increment */ {
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04
            };
    static int flags8d[]
            = /* decrement */ {
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04,
                0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04
            };

    public static void SET_FLAGS8I(int a) {
        m6805.u8_cc = (char) ((m6805.u8_cc | flags8i[(a) & 0xff]) & 0xFF);
    }

    public static void SET_FLAGS8D(int a) {
        m6805.u8_cc = (char) ((m6805.u8_cc | flags8d[(a) & 0xff]) & 0xFF);
    }

    /* combos */
    public static void SET_NZ8(int a) {
        SET_N8(a);
        SET_Z(a);
    }

    public static void SET_FLAGS8(int a, int b, int r) {
        SET_N8(r);
        SET_Z8(r);
        SET_C8(r);
    }

    /* for treating an unsigned UINT8 as a signed INT16 */
    public static short SIGNED(int b) {
        return ((short) ((b & 0x80) != 0 ? b | 0xff00 : b));
    }

    /* Macros for addressing modes */
    public static void DIRECT() {
        ea.SetD(0);
        ea.SetL(IMMBYTE());
    }

    public static void IMM8() {
        ea.SetD(m6805.pc.D);
        m6805.pc.AddD(1);
    }

    public static void EXTENDED() {
        IMMWORD(ea);
    }

    public static void INDEXED() {
        ea.SetD(m6805.u8_x);
    }

    public static void INDEXED1() {
        ea.SetD(0);
        ea.SetL(IMMBYTE());
        ea.AddD(m6805.u8_x);
    }

    public static void INDEXED2() {
        IMMWORD(ea);
        ea.AddD(m6805.u8_x);
    }

    /* macros to set status flags */
    public static void SEC() {
        m6805.u8_cc |= CFLAG;
    }

    public static void CLC() {
        m6805.u8_cc &= ~CFLAG;
    }

    public static void SEZ() {
        m6805.u8_cc |= ZFLAG;
    }

    public static void CLZ() {
        m6805.u8_cc &= ~ZFLAG;
    }

    public static void SEN() {
        m6805.u8_cc |= NFLAG;
    }

    public static void CLN() {
        m6805.u8_cc &= ~NFLAG;
    }

    public static void SEH() {
        m6805.u8_cc |= HFLAG;
    }

    public static void CLH() {
        m6805.u8_cc &= ~HFLAG;
    }

    public static void SEI() {
        m6805.u8_cc |= IFLAG;
    }

    public static void CLI() {
        m6805.u8_cc &= ~IFLAG;
    }

    /* macros for convenience */
    public static char DIRBYTE() {
        DIRECT();
        char b = RM(ea.D);
        return (char) (b & 0xFF);
    }

    public static char EXTBYTE() {
        EXTENDED();
        char b = RM(ea.D);
        return (char) (b & 0xFF);
    }

    public static char IDXBYTE() {
        INDEXED();
        char b = RM(ea.D);
        return (char) (b & 0xFF);
    }

    public static char IDX1BYTE() {
        INDEXED1();
        char b = RM(ea.D);
        return (char) (b & 0xFF);
    }

    public static char IDX2BYTE() {
        INDEXED2();
        char b = RM(ea.D);
        return (char) (b & 0xFF);
    }

    /* Macros for branch instructions */
    public static void BRANCH(boolean f) {
        char/*UINT8*/ t;
        t = IMMBYTE();
        if (f) {
            m6805.pc.AddD(SIGNED(t & 0xFF));
        }
    }

    /* what they say it is ... */
    static int cycles1[]
            = {
                /* 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F */
                /*0*/10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
                /*1*/ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
                /*2*/ 4, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                /*3*/ 6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 6, 0, 6, 6, 0,
                /*4*/ 4, 0, 0, 4, 4, 0, 4, 4, 4, 4, 4, 0, 4, 4, 0, 4,
                /*5*/ 4, 0, 0, 4, 4, 0, 4, 4, 4, 4, 4, 0, 4, 4, 0, 4,
                /*6*/ 7, 0, 0, 7, 7, 0, 7, 7, 7, 7, 7, 0, 7, 7, 0, 7,
                /*7*/ 6, 0, 0, 6, 6, 0, 6, 6, 6, 6, 6, 0, 6, 6, 0, 6,
                /*8*/ 9, 6, 0, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                /*9*/ 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 0, 2,
                /*A*/ 2, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 0, 8, 2, 0,
                /*B*/ 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 3, 7, 4, 5,
                /*C*/ 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 4, 8, 5, 6,
                /*D*/ 6, 6, 6, 6, 6, 6, 6, 7, 6, 6, 6, 6, 5, 9, 6, 7,
                /*E*/ 5, 5, 5, 5, 5, 5, 5, 6, 5, 5, 5, 5, 4, 8, 5, 6,
                /*F*/ 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 3, 7, 4, 5
            };

    public static char rd_s_handler_b() {
        char b = RM(m6805.s.D);
        SP_INC();
        return b;
    }

    public static void rd_s_handler_w(PAIR p) {
        p.SetD(0);
        p.SetH(RM(m6805.s.D));
        SP_INC();
        p.SetL(RM(m6805.s.D));
        SP_INC();
    }

    public static void wr_s_handler_b(int/*UINT8 **/ b) {
        SP_DEC();
        WM(m6805.s.D, b & 0xFF);
    }

    public static void wr_s_handler_w(PAIR p) {
        SP_DEC();
        WM(m6805.s.D, p.L);
        SP_DEC();
        WM(m6805.s.D, p.H);
    }

    public static void RM16( /*UINT32*/int Addr, PAIR p) {
        p.SetD(0);
        p.SetH(RM(Addr));
        if (++Addr > m6805.amask) {
            Addr = 0;
        }
        p.SetL(RM(Addr));
    }

    public static void WM16( /*UINT32*/int Addr, PAIR p) {
        WM(Addr, p.H);
        if (++Addr > m6805.amask) {
            Addr = 0;
        }
        WM(Addr, p.L);
    }

    /* Generate interrupts */
    static void Interrupt() {
        /* the 6805 latches interrupt requests internally, so we don't clear */
 /* pending_interrupts until the interrupt is taken, no matter what the */
 /* external IRQ pin does. */
        if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_NMI)) != 0) {
            PUSHWORD(m6805.pc);
            PUSHBYTE(m6805.u8_x);
            PUSHBYTE(m6805.u8_a);
            PUSHBYTE(m6805.u8_cc);
            SEI();
            /* no vectors supported, just do the callback to clear irq_state if needed */
            if (m6805.irq_callback != null) {
                (m6805.irq_callback).handler(0);
            }

            RM16(0x1ffc, m6805.pc);
            m6805.u8_pending_interrupts &= ~(1 << HD63705_INT_NMI);

            m6805_ICount[0] -= 11;

        } else if ((m6805.u8_pending_interrupts & (M6805_INT_IRQ | HD63705_INT_MASK)) != 0 && (m6805.u8_cc & IFLAG) == 0) {
            /* standard IRQ */
            if (m6805.subtype != SUBTYPE_HD63705) {
                m6805.pc.SetD(m6805.pc.D | 0xf800);
            }
            PUSHWORD(m6805.pc);
            PUSHBYTE(m6805.u8_x);
            PUSHBYTE(m6805.u8_a);
            PUSHBYTE(m6805.u8_cc);
            SEI();
            /* no vectors supported, just do the callback to clear irq_state if needed */
            if (m6805.irq_callback != null) {
                (m6805.irq_callback).handler(0);
            }

            if (m6805.subtype == SUBTYPE_HD63705) {
                /* Need to add emulation of other interrupt sources here KW-2/4/99 */
 /* This is just a quick patch for Namco System 2 operation         */

                if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_IRQ1)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_IRQ1)) & 0xFF);
                    RM16(0x1ff8, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_IRQ2)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_IRQ2)) & 0xFF);
                    RM16(0x1fec, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_ADCONV)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_ADCONV)) & 0xFF);
                    RM16(0x1fea, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_TIMER1)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_TIMER1)) & 0xFF);
                    RM16(0x1ff6, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_TIMER2)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_TIMER2)) & 0xFF);
                    RM16(0x1ff4, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_TIMER3)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_TIMER3)) & 0xFF);
                    RM16(0x1ff2, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_PCI)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_PCI)) & 0xFF);
                    RM16(0x1ff0, m6805.pc);
                } else if ((m6805.u8_pending_interrupts & (1 << HD63705_INT_SCI)) != 0) {
                    m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~(1 << HD63705_INT_SCI)) & 0xFF);
                    RM16(0x1fee, m6805.pc);
                }
            } else {
                m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts & ~M6805_INT_IRQ) & 0xFF);
                RM16(m6805.amask - 5, m6805.pc);
            }
            m6805_ICount[0] -= 11;
        }
    }

    @Override
    public void reset(Object param) {
        //memset(&m6805, 0, sizeof(m6805));
        /* Force CPU sub-type and relevant masks */
        m6805.subtype = SUBTYPE_M6805;
        m6805.amask = 0x7ff;
        m6805.sp_mask = 0x07f;
        m6805.sp_low = 0x060;
        /* Initial stack pointer */
        m6805.s.SetD(m6805.sp_mask);
        /* IRQ disabled */
        SEI();
        RM16(m6805.amask - 1, m6805.pc);
    }

    @Override
    public void exit() {
        /* nothing to do */
    }

    @Override
    public int execute(int cycles) {
        int/*UINT8*/ ireg;

        m6805_ICount[0] = cycles;

        do {
            if (m6805.u8_pending_interrupts != 0) {
                Interrupt();
            }

            ireg = M_RDOP(m6805.pc.D);
            m6805.pc.AddD(1);

            switch (ireg) {
                case 0x00:
                    brset(0x01);
                    break;
                case 0x01:
                    brclr(0x01);
                    break;
                case 0x02:
                    brset(0x02);
                    break;
                case 0x03:
                    brclr(0x02);
                    break;
                case 0x04:
                    brset(0x04);
                    break;
                case 0x05:
                    brclr(0x04);
                    break;
                case 0x06:
                    brset(0x08);
                    break;
                case 0x07:
                    brclr(0x08);
                    break;
                case 0x08:
                    brset(0x10);
                    break;
                case 0x09:
                    brclr(0x10);
                    break;
                case 0x0A:
                    brset(0x20);
                    break;
                case 0x0B:
                    brclr(0x20);
                    break;
                case 0x0C:
                    brset(0x40);
                    break;
                case 0x0D:
                    brclr(0x40);
                    break;
                case 0x0E:
                    brset(0x80);
                    break;
                case 0x0F:
                    brclr(0x80);
                    break;
                case 0x10:
                    bset(0x01);
                    break;
                case 0x11:
                    bclr(0x01);
                    break;
                case 0x12:
                    bset(0x02);
                    break;
                case 0x13:
                    bclr(0x02);
                    break;
                case 0x14:
                    bset(0x04);
                    break;
                case 0x15:
                    bclr(0x04);
                    break;
                case 0x16:
                    bset(0x08);
                    break;
                case 0x17:
                    bclr(0x08);
                    break;
                case 0x18:
                    bset(0x10);
                    break;
                case 0x19:
                    bclr(0x10);
                    break;
                case 0x1a:
                    bset(0x20);
                    break;
                case 0x1b:
                    bclr(0x20);
                    break;
                case 0x1c:
                    bset(0x40);
                    break;
                case 0x1d:
                    bclr(0x40);
                    break;
                case 0x1e:
                    bset(0x80);
                    break;
                case 0x1f:
                    bclr(0x80);
                    break;
                case 0x20:
                    bra();
                    break;
                case 0x21:
                    brn();
                    break;
                case 0x22:
                    bhi();
                    break;
                case 0x23:
                    bls();
                    break;
                case 0x24:
                    bcc();
                    break;
                case 0x25:
                    bcs();
                    break;
                case 0x26:
                    bne();
                    break;
                case 0x27:
                    beq();
                    break;
                case 0x28:
                    bhcc();
                    break;
                case 0x29:
                    bhcs();
                    break;
                case 0x2a:
                    bpl();
                    break;
                case 0x2b:
                    bmi();
                    break;
                case 0x2c:
                    bmc();
                    break;
                case 0x2d:
                    bms();
                    break;
                case 0x2e:
                    bil();
                    break;
                case 0x2f:
                    bih();
                    break;
                case 0x30:
                    neg_di();
                    break;
                case 0x31:
                    illegal();
                    break;
                case 0x32:
                    illegal();
                    break;
                case 0x33:
                    com_di();
                    break;
                case 0x34:
                    lsr_di();
                    break;
                case 0x35:
                    illegal();
                    break;
                case 0x36:
                    ror_di();
                    break;
                case 0x37:
                    asr_di();
                    break;
                case 0x38:
                    lsl_di();
                    break;
                case 0x39:
                    rol_di();
                    break;
                case 0x3a:
                    dec_di();
                    break;
                case 0x3b:
                    illegal();
                    break;
                case 0x3c:
                    inc_di();
                    break;
                case 0x3d:
                    tst_di();
                    break;
                case 0x3e:
                    illegal();
                    break;
                case 0x3f:
                    clr_di();
                    break;
                case 0x40:
                    nega();
                    break;
                case 0x41:
                    illegal();
                    break;
                case 0x42:
                    illegal();
                    break;
                case 0x43:
                    coma();
                    break;
                case 0x44:
                    lsra();
                    break;
                case 0x45:
                    illegal();
                    break;
                case 0x46:
                    rora();
                    break;
                case 0x47:
                    asra();
                    break;
                case 0x48:
                    lsla();
                    break;
                case 0x49:
                    rola();
                    break;
                case 0x4a:
                    deca();
                    break;
                case 0x4b:
                    illegal();
                    break;
                case 0x4c:
                    inca();
                    break;
                case 0x4d:
                    tsta();
                    break;
                case 0x4e:
                    illegal();
                    break;
                case 0x4f:
                    clra();
                    break;
                case 0x50:
                    negx();
                    break;
                case 0x51:
                    illegal();
                    break;
                case 0x52:
                    illegal();
                    break;
                case 0x53:
                    comx();
                    break;
                case 0x54:
                    lsrx();
                    break;
                case 0x55:
                    illegal();
                    break;
                case 0x56:
                    rorx();
                    break;
                case 0x57:
                    asrx();
                    break;
                case 0x58:
                    aslx();
                    break;
                case 0x59:
                    rolx();
                    break;
                case 0x5a:
                    decx();
                    break;
                case 0x5b:
                    illegal();
                    break;
                case 0x5c:
                    incx();
                    break;
                case 0x5d:
                    tstx();
                    break;
                case 0x5e:
                    illegal();
                    break;
                case 0x5f:
                    clrx();
                    break;
                case 0x60:
                    neg_ix1();
                    break;
                case 0x61:
                    illegal();
                    break;
                case 0x62:
                    illegal();
                    break;
                case 0x63:
                    com_ix1();
                    break;
                case 0x64:
                    lsr_ix1();
                    break;
                case 0x65:
                    illegal();
                    break;
                case 0x66:
                    ror_ix1();
                    break;
                case 0x67:
                    asr_ix1();
                    break;
                case 0x68:
                    lsl_ix1();
                    break;
                case 0x69:
                    rol_ix1();
                    break;
                case 0x6a:
                    dec_ix1();
                    break;
                case 0x6b:
                    illegal();
                    break;
                case 0x6c:
                    inc_ix1();
                    break;
                case 0x6d:
                    tst_ix1();
                    break;
                case 0x6e:
                    illegal();
                    break;
                case 0x6f:
                    clr_ix1();
                    break;
                case 0x70:
                    neg_ix();
                    break;
                case 0x71:
                    illegal();
                    break;
                case 0x72:
                    illegal();
                    break;
                case 0x73:
                    com_ix();
                    break;
                case 0x74:
                    lsr_ix();
                    break;
                case 0x75:
                    illegal();
                    break;
                case 0x76:
                    ror_ix();
                    break;
                case 0x77:
                    asr_ix();
                    break;
                case 0x78:
                    lsl_ix();
                    break;
                case 0x79:
                    rol_ix();
                    break;
                case 0x7a:
                    dec_ix();
                    break;
                case 0x7b:
                    illegal();
                    break;
                case 0x7c:
                    inc_ix();
                    break;
                case 0x7d:
                    tst_ix();
                    break;
                case 0x7e:
                    illegal();
                    break;
                case 0x7f:
                    clr_ix();
                    break;
                case 0x80:
                    rti();
                    break;
                case 0x81:
                    rts();
                    break;
                case 0x82:
                    illegal();
                    break;
                case 0x83:
                    swi();
                    break;
                case 0x84:
                    illegal();
                    break;
                case 0x85:
                    illegal();
                    break;
                case 0x86:
                    illegal();
                    break;
                case 0x87:
                    illegal();
                    break;
                case 0x88:
                    illegal();
                    break;
                case 0x89:
                    illegal();
                    break;
                case 0x8a:
                    illegal();
                    break;
                case 0x8b:
                    illegal();
                    break;
                case 0x8c:
                    illegal();
                    break;
                case 0x8d:
                    illegal();
                    break;
                case 0x8e:
                    illegal();
                    break;
                case 0x8f:
                    illegal();
                    break;
                case 0x90:
                    illegal();
                    break;
                case 0x91:
                    illegal();
                    break;
                case 0x92:
                    illegal();
                    break;
                case 0x93:
                    illegal();
                    break;
                case 0x94:
                    illegal();
                    break;
                case 0x95:
                    illegal();
                    break;
                case 0x96:
                    illegal();
                    break;
                case 0x97:
                    tax();
                    break;
                case 0x98:
                    CLC();
                    break;
                case 0x99:
                    SEC();
                    break;
                case 0x9a:
                    CLI();
                    break;
                case 0x9b:
                    SEI();
                    break;
                case 0x9c:
                    rsp();
                    break;
                case 0x9d:
                    nop();
                    break;
                case 0x9e:
                    illegal();
                    break;
                case 0x9f:
                    txa();
                    break;
                case 0xa0:
                    suba_im();
                    break;
                case 0xa1:
                    cmpa_im();
                    break;
                case 0xa2:
                    sbca_im();
                    break;
                case 0xa3:
                    cpx_im();
                    break;
                case 0xa4:
                    anda_im();
                    break;
                case 0xa5:
                    bita_im();
                    break;
                case 0xa6:
                    lda_im();
                    break;
                case 0xa7:
                    illegal();
                    break;
                case 0xa8:
                    eora_im();
                    break;
                case 0xa9:
                    adca_im();
                    break;
                case 0xaa:
                    ora_im();
                    break;
                case 0xab:
                    adda_im();
                    break;
                case 0xac:
                    illegal();
                    break;
                case 0xad:
                    bsr();
                    break;
                case 0xae:
                    ldx_im();
                    break;
                case 0xaf:
                    illegal();
                    break;
                case 0xb0:
                    suba_di();
                    break;
                case 0xb1:
                    cmpa_di();
                    break;
                case 0xb2:
                    sbca_di();
                    break;
                case 0xb3:
                    cpx_di();
                    break;
                case 0xb4:
                    anda_di();
                    break;
                case 0xb5:
                    bita_di();
                    break;
                case 0xb6:
                    lda_di();
                    break;
                case 0xb7:
                    sta_di();
                    break;
                case 0xb8:
                    eora_di();
                    break;
                case 0xb9:
                    adca_di();
                    break;
                case 0xba:
                    ora_di();
                    break;
                case 0xbb:
                    adda_di();
                    break;
                case 0xbc:
                    jmp_di();
                    break;
                case 0xbd:
                    jsr_di();
                    break;
                case 0xbe:
                    ldx_di();
                    break;
                case 0xbf:
                    stx_di();
                    break;
                case 0xc0:
                    suba_ex();
                    break;
                case 0xc1:
                    cmpa_ex();
                    break;
                case 0xc2:
                    sbca_ex();
                    break;
                case 0xc3:
                    cpx_ex();
                    break;
                case 0xc4:
                    anda_ex();
                    break;
                case 0xc5:
                    bita_ex();
                    break;
                case 0xc6:
                    lda_ex();
                    break;
                case 0xc7:
                    sta_ex();
                    break;
                case 0xc8:
                    eora_ex();
                    break;
                case 0xc9:
                    adca_ex();
                    break;
                case 0xca:
                    ora_ex();
                    break;
                case 0xcb:
                    adda_ex();
                    break;
                case 0xcc:
                    jmp_ex();
                    break;
                case 0xcd:
                    jsr_ex();
                    break;
                case 0xce:
                    ldx_ex();
                    break;
                case 0xcf:
                    stx_ex();
                    break;
                case 0xd0:
                    suba_ix2();
                    break;
                case 0xd1:
                    cmpa_ix2();
                    break;
                case 0xd2:
                    sbca_ix2();
                    break;
                case 0xd3:
                    cpx_ix2();
                    break;
                case 0xd4:
                    anda_ix2();
                    break;
                case 0xd5:
                    bita_ix2();
                    break;
                case 0xd6:
                    lda_ix2();
                    break;
                case 0xd7:
                    sta_ix2();
                    break;
                case 0xd8:
                    eora_ix2();
                    break;
                case 0xd9:
                    adca_ix2();
                    break;
                case 0xda:
                    ora_ix2();
                    break;
                case 0xdb:
                    adda_ix2();
                    break;
                case 0xdc:
                    jmp_ix2();
                    break;
                case 0xdd:
                    jsr_ix2();
                    break;
                case 0xde:
                    ldx_ix2();
                    break;
                case 0xdf:
                    stx_ix2();
                    break;
                case 0xe0:
                    suba_ix1();
                    break;
                case 0xe1:
                    cmpa_ix1();
                    break;
                case 0xe2:
                    sbca_ix1();
                    break;
                case 0xe3:
                    cpx_ix1();
                    break;
                case 0xe4:
                    anda_ix1();
                    break;
                case 0xe5:
                    bita_ix1();
                    break;
                case 0xe6:
                    lda_ix1();
                    break;
                case 0xe7:
                    sta_ix1();
                    break;
                case 0xe8:
                    eora_ix1();
                    break;
                case 0xe9:
                    adca_ix1();
                    break;
                case 0xea:
                    ora_ix1();
                    break;
                case 0xeb:
                    adda_ix1();
                    break;
                case 0xec:
                    jmp_ix1();
                    break;
                case 0xed:
                    jsr_ix1();
                    break;
                case 0xee:
                    ldx_ix1();
                    break;
                case 0xef:
                    stx_ix1();
                    break;
                case 0xf0:
                    suba_ix();
                    break;
                case 0xf1:
                    cmpa_ix();
                    break;
                case 0xf2:
                    sbca_ix();
                    break;
                case 0xf3:
                    cpx_ix();
                    break;
                case 0xf4:
                    anda_ix();
                    break;
                case 0xf5:
                    bita_ix();
                    break;
                case 0xf6:
                    lda_ix();
                    break;
                case 0xf7:
                    sta_ix();
                    break;
                case 0xf8:
                    eora_ix();
                    break;
                case 0xf9:
                    adca_ix();
                    break;
                case 0xfa:
                    ora_ix();
                    break;
                case 0xfb:
                    adda_ix();
                    break;
                case 0xfc:
                    jmp_ix();
                    break;
                case 0xfd:
                    jsr_ix();
                    break;
                case 0xfe:
                    ldx_ix();
                    break;
                case 0xff:
                    stx_ix();
                    break;
            }
            m6805_ICount[0] -= cycles1[ireg];
        } while (m6805_ICount[0] > 0);

        return cycles - m6805_ICount[0];
    }

    @Override
    public Object init_context() {
        Object reg = new m6805_Regs();
        return reg;
    }

    @Override
    public Object get_context() {
        /*TODO*///	if( dst )
/*TODO*///		*(m6805_Regs*)dst = m6805;
/*TODO*///    return sizeof(m6805_Regs);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_context(Object reg) {
        /*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		m6805 = *(m6805_Regs*)src;
/*TODO*///		S = SP_ADJUST( S );
/*TODO*///	}
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int get_pc() {
        return m6805.pc.D & m6805.amask;
    }

    @Override
    public void set_pc(int val) {
        /*TODO*///	PC = val & AMASK;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        /*TODO*///	return SP_ADJUST(S);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        /*TODO*///	S = SP_ADJUST(val);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6805_A: return A;
/*TODO*///		case M6805_PC: return PC;
/*TODO*///		case M6805_S: return SP_ADJUST(S);
/*TODO*///		case M6805_X: return X;
/*TODO*///		case M6805_CC: return CC;
/*TODO*///		case M6805_IRQ_STATE: return m6805.irq_state[0];
/*TODO*///		default:
/*TODO*///			if( regnum < REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < SP_MASK )
/*TODO*///					return (RM( offset ) << 8) | RM( offset+1 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        /*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case M6805_A: A = val; break;
/*TODO*///		case M6805_PC: PC = val & AMASK; break;
/*TODO*///		case M6805_S: S = SP_ADJUST(val); break;
/*TODO*///		case M6805_X: X = val; break;
/*TODO*///		case M6805_CC: CC = val; break;
/*TODO*///		case M6805_IRQ_STATE: m6805_set_irq_line(0,val); break;
/*TODO*///		default:
/*TODO*///			if( regnum < REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < SP_MASK )
/*TODO*///				{
/*TODO*///                    WM( offset, (val >> 8) & 0xff );
/*TODO*///					WM( offset+1, val & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int linestate) {
        /* 6805 has no NMI line... but the HD63705 does !! see specific version */
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        /* Basic 6805 only has one IRQ line */
 /* See HD63705 specific version     */
        if (m6805.irq_state[0] == state) {
            return;
        }

        m6805.irq_state[0] = state;
        if (state != CLEAR_LINE) {
            m6805.u8_pending_interrupts = (char) ((m6805.u8_pending_interrupts | M6805_INT_IRQ) & 0xFF);
        }
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        m6805.irq_callback = callback;
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_save(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT8(file,module,cpu,"A", &A, 1);
/*TODO*///	state_save_UINT16(file,module,cpu,"PC", &PC, 1);
/*TODO*///	state_save_UINT16(file,module,cpu,"S", &S, 1);
/*TODO*///	state_save_UINT8(file,module,cpu,"X", &X, 1);
/*TODO*///	state_save_UINT8(file,module,cpu,"CC", &CC, 1);
/*TODO*///	state_save_UINT8(file,module,cpu,"PENDING", &m6805.pending_interrupts, 1);
/*TODO*///	state_save_INT32(file,module,cpu,"IRQ_STATE", &m6805.irq_state[0], 1);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_load(Object file) {
        /*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT8(file,module,cpu,"A", &A, 1);
/*TODO*///	state_load_UINT16(file,module,cpu,"PC", &PC, 1);
/*TODO*///	state_load_UINT16(file,module,cpu,"S", &S, 1);
/*TODO*///	state_load_UINT8(file,module,cpu,"X", &X, 1);
/*TODO*///	state_load_UINT8(file,module,cpu,"CC", &CC, 1);
/*TODO*///	state_load_UINT8(file,module,cpu,"PENDING", &m6805.pending_interrupts, 1);
/*TODO*///	state_load_INT32(file,module,cpu,"IRQ_STATE", &m6805.irq_state[0], 1);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[8][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	m6805_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 8;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///
/*TODO*///    if( !context )
/*TODO*///		r = &m6805;

        switch (regnum) {
            case CPU_INFO_NAME:
                return "M6805";
            case CPU_INFO_FAMILY:
                return "Motorola 6805";
            case CPU_INFO_VERSION:
                return "1.0";
            case CPU_INFO_FILE:
                return "m6805.java";
            case CPU_INFO_CREDITS:
                return "The MAME team.";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)m6805_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)m6805_win_layout;
/*TODO*///
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->cc & 0x80 ? '?':'.',
/*TODO*///                r->cc & 0x40 ? '?':'.',
/*TODO*///                r->cc & 0x20 ? '?':'.',
/*TODO*///                r->cc & 0x10 ? 'H':'.',
/*TODO*///                r->cc & 0x08 ? 'I':'.',
/*TODO*///                r->cc & 0x04 ? 'N':'.',
/*TODO*///                r->cc & 0x02 ? 'Z':'.',
/*TODO*///                r->cc & 0x01 ? 'C':'.');
/*TODO*///            break;
/*TODO*///		case CPU_INFO_REG+M6805_A: sprintf(buffer[which], "A:%02X", r->a); break;
/*TODO*///		case CPU_INFO_REG+M6805_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6805_S: sprintf(buffer[which], "S:%02X", r->s.w.l); break;
/*TODO*///		case CPU_INFO_REG+M6805_X: sprintf(buffer[which], "X:%02X", r->x); break;
/*TODO*///		case CPU_INFO_REG+M6805_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
/*TODO*///		case CPU_INFO_REG+M6805_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[0]); break;
        }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        cpu_setOPbase16.handler(pc,0);
    }

    static void illegal() {
        //logerror("M6805: illegal opcode\n");
    }


    /* $00/$02/$04/$06/$08/$0A/$0C/$0E BRSET direct,relative ---- */
    public static void brset(/*UINT8*/int bit) {
        char /*UINT8*/ t, r;
        r = DIRBYTE();
        t = IMMBYTE();
        if ((r & bit) != 0) {
            m6805.pc.AddD(SIGNED(t & 0xFF));
        } else if (t == 0xfd) {
            /* speed up busy loops */
            if (m6805_ICount[0] > 0) {
                m6805_ICount[0] = 0;
            }
        }
    }

    /* $01/$03/$05/$07/$09/$0B/$0D/$0F BRCLR direct,relative ---- */
    public static void brclr(/*UINT8*/int bit) {
        char /*UINT8*/ t, r;
        r = DIRBYTE();
        t = IMMBYTE();
        if ((r & bit) == 0) {
            m6805.pc.AddD(SIGNED(t & 0xFF));
        } else {
            /* speed up busy loops */
            if (m6805_ICount[0] > 0) {
                m6805_ICount[0] = 0;
            }
        }
    }

    /* $10/$12/$14/$16/$18/$1A/$1C/$1E BSET direct ---- */
    public static void bset(/*UINT8*/int bit) {
        char/*UINT8*/ t, r;
        t = DIRBYTE();
        r = (char) ((t | bit) & 0xFF);
        WM(ea.D, r);
    }

    /* $11/$13/$15/$17/$19/$1B/$1D/$1F BCLR direct ---- */
    public static void bclr(/*UINT8*/int bit) {
        char/*UINT8*/ t, r;
        t = DIRBYTE();
        r = (char) ((t & (~bit)) & 0xFF);
        WM(ea.D, r);
    }

    /* $20 BRA relative ---- */
    public static void bra() {
        char /*UINT8*/ t;
        t = IMMBYTE();
        m6805.pc.AddD(SIGNED(t & 0xFF));
        if (t == 0xfe) {
            /* speed up busy loops */
            if (m6805_ICount[0] > 0) {
                m6805_ICount[0] = 0;
            }
        }
    }

    /* $21 BRN relative ---- */
    public static void brn() {
        char /*UINT8*/ t;
        t = IMMBYTE();
    }

    /* $22 BHI relative ---- */
    public static void bhi() {
        BRANCH((m6805.u8_cc & (CFLAG | ZFLAG)) == 0);
    }

    /* $23 BLS relative ---- */
    public static void bls() {
        BRANCH((m6805.u8_cc & (CFLAG | ZFLAG)) != 0);
    }

    /* $24 BCC relative ---- */
    public static void bcc() {
        BRANCH((m6805.u8_cc & CFLAG) == 0);
    }

    /* $25 BCS relative ---- */
    public static void bcs() {
        BRANCH((m6805.u8_cc & CFLAG) != 0);
    }

    /* $26 BNE relative ---- */
    public static void bne() {
        BRANCH((m6805.u8_cc & ZFLAG) == 0);
    }

    /* $27 BEQ relative ---- */
    public static void beq() {
        BRANCH((m6805.u8_cc & ZFLAG) != 0);
    }

    /* $28 BHCC relative ---- */
    public static void bhcc() {
        BRANCH((m6805.u8_cc & HFLAG) == 0);
    }

    /* $29 BHCS relative ---- */
    public static void bhcs() {
        BRANCH((m6805.u8_cc & HFLAG) != 0);
    }

    /* $2a BPL relative ---- */
    public static void bpl() {
        BRANCH((m6805.u8_cc & NFLAG) == 0);
    }

    /* $2b BMI relative ---- */
    public static void bmi() {
        BRANCH((m6805.u8_cc & NFLAG) != 0);
    }

    /* $2c BMC relative ---- */
    public static void bmc() {
        BRANCH((m6805.u8_cc & IFLAG) == 0);
    }

    /* $2d BMS relative ---- */
    public static void bms() {
        BRANCH((m6805.u8_cc & IFLAG) != 0);
    }

    /* $2e BIL relative ---- */
    public static void bil() {
        if (m6805.subtype == SUBTYPE_HD63705) {
            BRANCH(m6805.nmi_state != CLEAR_LINE);
        } else {
            BRANCH(m6805.irq_state[0] != CLEAR_LINE);
        }
    }

    /* $2f BIH relative ---- */
    public static void bih() {
        if (m6805.subtype == SUBTYPE_HD63705) {
            BRANCH(m6805.nmi_state == CLEAR_LINE);
        } else {
            BRANCH(m6805.irq_state[0] == CLEAR_LINE);
        }
    }

    /* $30 NEG direct -*** */
    public static void neg_di() {
        char/*UINT8*/ t;
        char r;
        t = DIRBYTE();
        r = (char) (-t);
        CLR_NZC();
        SET_FLAGS8(0, t, r);
        WM(ea.D, r);
    }

    /* $31 ILLEGAL */

 /* $32 ILLEGAL */

 /* $33 COM direct -**1 */
    public static void com_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        t = (char) ((~t) & 0xFF);
        CLR_NZ();
        SET_NZ8(t);
        SEC();
        WM(ea.D, t);
    }

    /* $34 LSR direct -0** */
    public static void lsr_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) ((t >>> 1) & 0xFF);
        SET_Z8(t);
        WM(ea.D, t);
    }

    /* $35 ILLEGAL */

 /* $36 ROR direct -*** */
    public static void ror_di() {
        char/*UINT8*/ t, r;
        t = DIRBYTE();
        r = (char) (((m6805.u8_cc & 0x01) << 7) & 0xFF);
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        r = (char) ((r | t >>> 1) & 0xFF);
        SET_NZ8(r);
        WM(ea.D, r);
    }

    /* $37 ASR direct ?*** */
    public static void asr_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) ((t >>> 1) & 0xFF);
        t = (char) ((t | ((t & 0x40) << 1)) & 0xFF);
        SET_NZ8(t);
        WM(ea.D, t);
    }

    /* $38 LSL direct ?*** */
    public static void lsl_di() {
        char/*UINT8*/ t;
        char r;
        t = DIRBYTE();
        r = (char) (t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $39 ROL direct -*** */
    public static void rol_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_cc & 0x01);
        r = (char) (r | t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $3a DEC direct -**- */
    public static void dec_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        t = (char) ((t - 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8D(t);
        WM(ea.D, t);
    }

    /* $3b ILLEGAL */

 /* $3c INC direct -**- */
    public static void inc_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        t = (char) ((t + 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8I(t);
        WM(ea.D, t);
    }

    /* $3d TST direct -**- */
    public static void tst_di() {
        char/*UINT8*/ t;
        t = DIRBYTE();
        CLR_NZ();
        SET_NZ8(t);
    }

    /* $3e ILLEGAL */

 /* $3f CLR direct -0100 */
    public static void clr_di() {
        DIRECT();
        CLR_NZC();
        SEZ();
        WM(ea.D, 0);
    }

    /* $40 NEGA inherent ?*** */
    public static void nega() {
        char r;
        r = (char) -m6805.u8_a;
        CLR_NZC();
        SET_FLAGS8(0, m6805.u8_a, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $41 ILLEGAL */

 /* $42 ILLEGAL */

 /* $43 COMA inherent -**1 */
    public static void coma() {
        m6805.u8_a = (char) ((~m6805.u8_a) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        SEC();
    }

    /* $44 LSRA inherent -0** */
    public static void lsra() {
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_a & 0x01)) & 0xFF);
        m6805.u8_a = (char) ((m6805.u8_a >>> 1) & 0xFF);
        SET_Z8(m6805.u8_a);
    }

    /* $45 ILLEGAL */

 /* $46 RORA inherent -*** */
    public static void rora() {
        char/*UINT8*/ r;
        r = (char) (((m6805.u8_cc & 0x01) << 7) & 0xFF);
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_a & 0x01)) & 0xFF);
        r = (char) ((r | m6805.u8_a >>> 1) & 0xFF);
        SET_NZ8(r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $47 ASRA inherent ?*** */
    public static void asra() {
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_a & 0x01)) & 0xFF);
        m6805.u8_a = (char) (((m6805.u8_a & 0x80) | (m6805.u8_a >>> 1)) & 0xFF);
        SET_NZ8(m6805.u8_a);
    }

    /* $48 LSLA inherent ?*** */
    public static void lsla() {
        char r;
        r = (char) (m6805.u8_a << 1);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, m6805.u8_a, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $49 ROLA inherent -*** */
    public static void rola() {
        char t, r;
        t = m6805.u8_a;
        r = (char) (m6805.u8_cc & 0x01);
        r = (char) (r | t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $4a DECA inherent -**- */
    public static void deca() {
        m6805.u8_a = (char) ((m6805.u8_a - 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8D(m6805.u8_a);
    }

    /* $4b ILLEGAL */

 /* $4c INCA inherent -**- */
    public static void inca() {
        m6805.u8_a = (char) ((m6805.u8_a + 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8I(m6805.u8_a);
    }

    /* $4d TSTA inherent -**- */
    public static void tsta() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $4e ILLEGAL */

 /* $4f CLRA inherent -010 */
    public static void clra() {
        m6805.u8_a = 0;
        CLR_NZC();
        SEZ();
    }

    /* $50 NEGX inherent ?*** */
    public static void negx() {
        char r;
        r = (char) -m6805.u8_x;
        CLR_NZC();
        SET_FLAGS8(0, m6805.u8_x, r);
        m6805.u8_x = (char) (r & 0xFF);
    }

    /* $51 ILLEGAL */

 /* $52 ILLEGAL */

 /* $53 COMX inherent -**1 */
    public static void comx() {
        m6805.u8_x = (char) ((~m6805.u8_x) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        SEC();
    }

    /* $54 LSRX inherent -0** */
    public static void lsrx() {
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_x & 0x01)) & 0xFF);
        m6805.u8_x = (char) ((m6805.u8_x >>> 1) & 0xFF);
        SET_Z8(m6805.u8_x);
    }

    /* $55 ILLEGAL */

 /* $56 RORX inherent -*** */
    public static void rorx() {
        char/*UINT8*/ r;
        r = (char) (((m6805.u8_cc & 0x01) << 7) & 0xFF);
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_x & 0x01)) & 0xFF);
        r = (char) ((r | m6805.u8_x >>> 1) & 0xFF);
        SET_NZ8(r);
        m6805.u8_x = (char) (r & 0xFF);
    }

    /* $57 ASRX inherent ?*** */
    public static void asrx() {
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (m6805.u8_x & 0x01)) & 0xFF);
        m6805.u8_x = (char) (((m6805.u8_x & 0x80) | (m6805.u8_x >>> 1)) & 0xFF);
        SET_NZ8(m6805.u8_x);
    }

    /* $58 ASLX inherent ?*** */
    public static void aslx() {
        char r;
        r = (char) (m6805.u8_x << 1);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, m6805.u8_x, r);
        m6805.u8_x = (char) (r & 0xFF);
    }

    /* $59 ROLX inherent -*** */
    public static void rolx() {
        char t, r;
        t = (char) m6805.u8_x;
        r = (char) (m6805.u8_cc & 0x01);
        r = (char) ((r | t << 1) & 0xFF);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        m6805.u8_x = (char) (r & 0xFF);
    }

    /* $5a DECX inherent -**- */
    public static void decx() {
        m6805.u8_x = (char) ((m6805.u8_x - 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8D(m6805.u8_x);
    }

    /* $5b ILLEGAL */

 /* $5c INCX inherent -**- */
    public static void incx() {
        m6805.u8_x = (char) ((m6805.u8_x + 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8I(m6805.u8_x);
    }

    /* $5d TSTX inherent -**- */
    public static void tstx() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $5e ILLEGAL */

 /* $5f CLRX inherent -010 */
    public static void clrx() {
        m6805.u8_x = 0;
        CLR_NZC();
        SEZ();
    }

    /* $60 NEG indexed, 1 byte offset -*** */
    public static void neg_ix1() {
        char/*UINT8*/ t;
        char r;
        t = IDX1BYTE();
        r = (char) -t;
        CLR_NZC();
        SET_FLAGS8(0, t, r);
        WM(ea.D, r);
    }

    /* $61 ILLEGAL */

 /* $62 ILLEGAL */

 /* $63 COM indexed, 1 byte offset -**1 */
    public static void com_ix1() {
        char/*UINT8*/ t;
        t = IDX1BYTE();
        t = (char) ((~t) & 0xFF);
        CLR_NZ();
        SET_NZ8(t);
        SEC();
        WM(ea.D, t);
    }

    /* $64 LSR indexed, 1 byte offset -0** */
    public static void lsr_ix1() {
        char/*UINT8*/ t;
        t = IDX1BYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) ((t >>> 1) & 0xFF);
        SET_Z8(t);
        WM(ea.D, t);
    }

    /* $65 ILLEGAL */

 /* $66 ROR indexed, 1 byte offset -*** */
    public static void ror_ix1() {
        char/*UINT8*/ t, r;
        t = IDX1BYTE();
        r = (char) (((m6805.u8_cc & 0x01) << 7) & 0xFF);
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        r = (char) ((r | t >>> 1) & 0xFF);
        SET_NZ8(r);
        WM(ea.D, r);
    }

    /* $67 ASR indexed, 1 byte offset ?*** */
    public static void asr_ix1() {
        char/*UINT8*/ t;
        t = IDX1BYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) ((t >>> 1) & 0xFF);
        t = (char) ((t | ((t & 0x40) << 1)) & 0xFF);
        SET_NZ8(t);
        WM(ea.D, t);
    }

    /* $68 LSL indexed, 1 byte offset ?*** */
    public static void lsl_ix1() {
        char /*UINT8*/ t;
        char r;
        t = IDX1BYTE();
        r = (char) (t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $69 ROL indexed, 1 byte offset -*** */
    public static void rol_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_cc & 0x01);
        r = (char) (r | t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $6a DEC indexed, 1 byte offset -**- */
    public static void dec_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        t = (char) ((t - 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8D(t);
        WM(ea.D, t);
    }

    /* $6b ILLEGAL */

 /* $6c INC indexed, 1 byte offset -**- */
    public static void inc_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        t = (char) ((t + 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8I(t);
        WM(ea.D, t);
    }

    /* $6d TST indexed, 1 byte offset -**- */
    public static void tst_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        CLR_NZ();
        SET_NZ8(t);
    }

    /* $6e ILLEGAL */

 /* $6f CLR indexed, 1 byte offset -0100 */
    public static void clr_ix1() {
        INDEXED1();
        CLR_NZC();
        SEZ();
        WM(ea.D, 0);
    }

    /* $70 NEG indexed -*** */
    public static void neg_ix() {
        char /*UINT8*/ t;
        char r;
        t = IDXBYTE();
        r = (char) ((-t) & 0xFF);
        CLR_NZC();
        SET_FLAGS8(0, t, r);
        WM(ea.D, r);
    }

    /* $71 ILLEGAL */

 /* $72 ILLEGAL */

 /* $73 COM indexed -**1 */
    public static void com_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        t = (char) ((~t) & 0xFF);
        CLR_NZ();
        SET_NZ8(t);
        SEC();
        WM(ea.D, t);
    }

    /* $74 LSR indexed -0** */
    public static void lsr_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) ((t >>> 1) & 0xFF);
        SET_Z8(t);
        WM(ea.D, t);
    }

    /* $75 ILLEGAL */

 /* $76 ROR indexed -*** */
    public static void ror_ix() {
        char /*UINT8*/ t, r;
        t = IDXBYTE();
        r = (char) (((m6805.u8_cc & 0x01) << 7) & 0xFF);
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        r = (char) ((r | t >>> 1) & 0xFF);
        SET_NZ8(r);
        WM(ea.D, r);
    }

    /* $77 ASR indexed ?*** */
    public static void asr_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        CLR_NZC();
        m6805.u8_cc = (char) ((m6805.u8_cc | (t & 0x01)) & 0xFF);
        t = (char) (((t & 0x80) | (t >>> 1)) & 0xFF);
        SET_NZ8(t);
        WM(ea.D, t);
    }

    /* $78 LSL indexed ?*** */
    public static void lsl_ix() {
        char /*UINT8*/ t;
        char r;
        t = IDXBYTE();
        r = (char) (t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $79 ROL indexed -*** */
    public static void rol_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_cc & 0x01);
        r = (char) (r | t << 1);
        CLR_NZC();
        SET_FLAGS8(t, t, r);
        WM(ea.D, r);
    }

    /* $7a DEC indexed -**- */
    public static void dec_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        t = (char) ((t - 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8D(t);
        WM(ea.D, t);
    }

    /* $7b ILLEGAL */

 /* $7c INC indexed -**- */
    public static void inc_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        t = (char) ((t + 1) & 0xFF);
        CLR_NZ();
        SET_FLAGS8I(t);
        WM(ea.D, t);
    }

    /* $7d TST indexed -**- */
    public static void tst_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        CLR_NZ();
        SET_NZ8(t);
    }

    /* $7e ILLEGAL */

 /* $7f CLR indexed -0100 */
    public static void clr_ix() {
        INDEXED();
        CLR_NZC();
        SEZ();
        WM(ea.D, 0);
    }

    /* $80 RTI inherent #### */
    public static void rti() {
        m6805.u8_cc = PULLBYTE();
        m6805.u8_a = PULLBYTE();
        m6805.u8_x = PULLBYTE();
        PULLWORD(m6805.pc);
        m6805.pc.SetD(m6805.pc.D & m6805.amask);
    }

    /* $81 RTS inherent ---- */
    public static void rts() {
        PULLWORD(m6805.pc);
        m6805.pc.SetD(m6805.pc.D & m6805.amask);
    }

    /* $82 ILLEGAL */

 /* $83 SWI absolute indirect ---- */
    public static void swi() {
        PUSHWORD(m6805.pc);
        PUSHBYTE(m6805.u8_x);
        PUSHBYTE(m6805.u8_a);
        PUSHBYTE(m6805.u8_cc);
        SEI();
        if (m6805.subtype == SUBTYPE_HD63705) {
            RM16(0x1ffa, m6805.pc);
        } else {
            RM16(m6805.amask - 3, m6805.pc);
        }
    }

    /* $84 ILLEGAL */

 /* $85 ILLEGAL */

 /* $86 ILLEGAL */

 /* $87 ILLEGAL */

 /* $88 ILLEGAL */

 /* $89 ILLEGAL */

 /* $8A ILLEGAL */

 /* $8B ILLEGAL */

 /* $8C ILLEGAL */

 /* $8D ILLEGAL */

 /* $8E ILLEGAL */

 /* $8F ILLEGAL */

 /* $90 ILLEGAL */

 /* $91 ILLEGAL */

 /* $92 ILLEGAL */

 /* $93 ILLEGAL */

 /* $94 ILLEGAL */

 /* $95 ILLEGAL */

 /* $96 ILLEGAL */

 /* $97 TAX inherent ---- */
    public static void tax() {
        m6805.u8_x = (char) (m6805.u8_a & 0xFF);
    }

    /* $98 CLC */

 /* $99 SEC */

 /* $9A CLI */

 /* $9B SEI */

 /* $9C RSP inherent ---- */
    public static void rsp() {
        m6805.s.SetD(m6805.sp_mask);
    }

    /* $9D NOP inherent ---- */
    public static void nop() {
    }

    /* $9E ILLEGAL */

 /* $9F TXA inherent ---- */
    public static void txa() {
        m6805.u8_a = (char) (m6805.u8_x & 0xFF);
    }


    /* $a0 SUBA immediate ?*** */
    public static void suba_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) ((m6805.u8_a - t));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $a1 CMPA immediate ?*** */
    public static void cmpa_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) ((m6805.u8_a - t));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $a2 SBCA immediate ?*** */
    public static void sbca_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $a3 CPX immediate -*** */
    public static void cpx_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $a4 ANDA immediate -**- */
    public static void anda_im() {
        char /*UINT8*/ t;
        t = IMMBYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $a5 BITA immediate -**- */
    public static void bita_im() {
        char /*UINT8*/ t, r;
        t = IMMBYTE();
        r = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $a6 LDA immediate -**- */
    public static void lda_im() {
        m6805.u8_a = IMMBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $a7 ILLEGAL */

 /* $a8 EORA immediate -**- */
    public static void eora_im() {
        char /*UINT8*/ t;
        t = IMMBYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $a9 ADCA immediate **** */
    public static void adca_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $aa ORA immediate -**- */
    public static void ora_im() {
        char /*UINT8*/ t;
        t = IMMBYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $ab ADDA immediate **** */
    public static void adda_im() {
        char t, r;
        t = IMMBYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $ac ILLEGAL */

 /* $ad BSR ---- */
    public static void bsr() {
        char /*UINT8*/ t;
        t = IMMBYTE();
        PUSHWORD(m6805.pc);
        m6805.pc.AddD(SIGNED(t & 0xFF));
    }

    /* $ae LDX immediate -**- */
    public static void ldx_im() {
        m6805.u8_x = IMMBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $af ILLEGAL */

 /* $b0 SUBA direct ?*** */
    public static void suba_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $b1 CMPA direct ?*** */
    public static void cmpa_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $b2 SBCA direct ?*** */
    public static void sbca_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $b3 CPX direct -*** */
    public static void cpx_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $b4 ANDA direct -**- */
    public static void anda_di() {
        char /*UINT8*/ t;
        t = DIRBYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $b5 BITA direct -**- */
    public static void bita_di() {
        char /*UINT8*/ t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a & t);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $b6 LDA direct -**- */
    public static void lda_di() {
        m6805.u8_a = DIRBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $b7 STA direct -**- */
    public static void sta_di() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        DIRECT();
        WM(ea.D, m6805.u8_a);
    }

    /* $b8 EORA direct -**- */
    public static void eora_di() {
        char /*UINT8*/ t;
        t = DIRBYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $b9 ADCA direct **** */
    public static void adca_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $ba ORA direct -**- */
    public static void ora_di() {
        char /*UINT8*/ t;
        t = DIRBYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $bb ADDA direct **** */
    public static void adda_di() {
        char t, r;
        t = DIRBYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $bc JMP direct -*** */
    public static void jmp_di() {
        DIRECT();
        m6805.pc.SetD(ea.D);
    }

    /* $bd JSR direct ---- */
    public static void jsr_di() {
        DIRECT();
        PUSHWORD(m6805.pc);
        m6805.pc.SetD(ea.D);
    }

    /* $be LDX direct -**- */
    public static void ldx_di() {
        m6805.u8_x = DIRBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $bf STX direct -**- */
    public static void stx_di() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        DIRECT();
        WM(ea.D, m6805.u8_x);
    }


    /* $c0 SUBA extended ?*** */
    public static void suba_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $c1 CMPA extended ?*** */
    public static void cmpa_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) ((m6805.u8_a - t) & 0xFF);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $c2 SBCA extended ?*** */
    public static void sbca_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $c3 CPX extended -*** */
    public static void cpx_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $c4 ANDA extended -**- */
    public static void anda_ex() {
        char /*UINT8*/ t;
        t = EXTBYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $c5 BITA extended -**- */
    public static void bita_ex() {
        char /*UINT8*/ t, r;
        t = EXTBYTE();
        r = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $c6 LDA extended -**- */
    public static void lda_ex() {
        m6805.u8_a = EXTBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $c7 STA extended -**- */
    public static void sta_ex() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        EXTENDED();
        WM(ea.D, m6805.u8_a);
    }

    /* $c8 EORA extended -**- */
    public static void eora_ex() {
        char /*UINT8*/ t;
        t = EXTBYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $c9 ADCA extended **** */
    public static void adca_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $ca ORA extended -**- */
    public static void ora_ex() {
        char /*UINT8*/ t;
        t = EXTBYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $cb ADDA extended **** */
    public static void adda_ex() {
        char t, r;
        t = EXTBYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $cc JMP extended -*** */
    public static void jmp_ex() {
        EXTENDED();
        m6805.pc.SetD(ea.D);
    }

    /* $cd JSR extended ---- */
    public static void jsr_ex() {
        EXTENDED();
        PUSHWORD(m6805.pc);
        m6805.pc.SetD(ea.D);
    }

    /* $ce LDX extended -**- */
    public static void ldx_ex() {
        m6805.u8_x = EXTBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $cf STX extended -**- */
    public static void stx_ex() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        EXTENDED();
        WM(ea.D, m6805.u8_x);
    }


    /* $d0 SUBA indexed, 2 byte offset ?*** */
    public static void suba_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $d1 CMPA indexed, 2 byte offset ?*** */
    public static void cmpa_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $d2 SBCA indexed, 2 byte offset ?*** */
    public static void sbca_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $d3 CPX indexed, 2 byte offset -*** */
    public static void cpx_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $d4 ANDA indexed, 2 byte offset -**- */
    public static void anda_ix2() {
        char /*UINT8*/ t;
        t = IDX2BYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $d5 BITA indexed, 2 byte offset -**- */
    public static void bita_ix2() {
        char /*UINT8*/ t, r;
        t = IDX2BYTE();
        r = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $d6 LDA indexed, 2 byte offset -**- */
    public static void lda_ix2() {
        m6805.u8_a = IDX2BYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $d7 STA indexed, 2 byte offset -**- */
    public static void sta_ix2() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        INDEXED2();
        WM(ea.D, m6805.u8_a);
    }

    /* $d8 EORA indexed, 2 byte offset -**- */
    public static void eora_ix2() {
        char /*UINT8*/ t;
        t = IDX2BYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $d9 ADCA indexed, 2 byte offset **** */
    public static void adca_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $da ORA indexed, 2 byte offset -**- */
    public static void ora_ix2() {
        char /*UINT8*/ t;
        t = IDX2BYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $db ADDA indexed, 2 byte offset **** */
    public static void adda_ix2() {
        char t, r;
        t = IDX2BYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $dc JMP indexed, 2 byte offset -*** */
    public static void jmp_ix2() {
        INDEXED2();
        m6805.pc.SetD(ea.D);
    }

    /* $dd JSR indexed, 2 byte offset ---- */
    public static void jsr_ix2() {
        INDEXED2();
        PUSHWORD(m6805.pc);
        m6805.pc.SetD(ea.D);
    }

    /* $de LDX indexed, 2 byte offset -**- */
    public static void ldx_ix2() {
        m6805.u8_x = IDX2BYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $df STX indexed, 2 byte offset -**- */
    public static void stx_ix2() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        INDEXED2();
        WM(ea.D, m6805.u8_x);
    }


    /* $e0 SUBA indexed, 1 byte offset ?*** */
    public static void suba_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $e1 CMPA indexed, 1 byte offset ?*** */
    public static void cmpa_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $e2 SBCA indexed, 1 byte offset ?*** */
    public static void sbca_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $e3 CPX indexed, 1 byte offset -*** */
    public static void cpx_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $e4 ANDA indexed, 1 byte offset -**- */
    public static void anda_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $e5 BITA indexed, 1 byte offset -**- */
    public static void bita_ix1() {
        char /*UINT8*/ t, r;
        t = IDX1BYTE();
        r = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $e6 LDA indexed, 1 byte offset -**- */
    public static void lda_ix1() {
        m6805.u8_a = IDX1BYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $e7 STA indexed, 1 byte offset -**- */
    public static void sta_ix1() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        INDEXED1();
        WM(ea.D, m6805.u8_a);
    }

    /* $e8 EORA indexed, 1 byte offset -**- */
    public static void eora_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $e9 ADCA indexed, 1 byte offset **** */
    public static void adca_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $ea ORA indexed, 1 byte offset -**- */
    public static void ora_ix1() {
        char /*UINT8*/ t;
        t = IDX1BYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $eb ADDA indexed, 1 byte offset **** */
    public static void adda_ix1() {
        char t, r;
        t = IDX1BYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $ec JMP indexed, 1 byte offset -*** */
    public static void jmp_ix1() {
        INDEXED1();
        m6805.pc.SetD(ea.D);
    }

    /* $ed JSR indexed, 1 byte offset ---- */
    public static void jsr_ix1() {
        INDEXED1();
        PUSHWORD(m6805.pc);
        m6805.pc.SetD(ea.D);
    }

    /* $ee LDX indexed, 1 byte offset -**- */
    public static void ldx_ix1() {
        m6805.u8_x = IDX1BYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $ef STX indexed, 1 byte offset -**- */
    public static void stx_ix1() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        INDEXED1();
        WM(ea.D, m6805.u8_x);
    }


    /* $f0 SUBA indexed ?*** */
    public static void suba_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $f1 CMPA indexed ?*** */
    public static void cmpa_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_a - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
    }

    /* $f2 SBCA indexed ?*** */
    public static void sbca_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_a - t - (m6805.u8_cc & 0x01));
        CLR_NZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $f3 CPX indexed -*** */
    public static void cpx_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_x - t);
        CLR_NZC();
        SET_FLAGS8(m6805.u8_x, t, r);
    }

    /* $f4 ANDA indexed -**- */
    public static void anda_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        m6805.u8_a = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $f5 BITA indexed -**- */
    public static void bita_ix() {
        char /*UINT8*/ t, r;
        t = IDXBYTE();
        r = (char) ((m6805.u8_a & t) & 0xFF);
        CLR_NZ();
        SET_NZ8(r);
    }

    /* $f6 LDA indexed -**- */
    public static void lda_ix() {
        m6805.u8_a = IDXBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $f7 STA indexed -**- */
    public static void sta_ix() {
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
        INDEXED();
        WM(ea.D, m6805.u8_a);
    }

    /* $f8 EORA indexed -**- */
    public static void eora_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        m6805.u8_a = (char) ((m6805.u8_a ^ t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $f9 ADCA indexed **** */
    public static void adca_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_a + t + (m6805.u8_cc & 0x01));
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $fa ORA indexed -**- */
    public static void ora_ix() {
        char /*UINT8*/ t;
        t = IDXBYTE();
        m6805.u8_a = (char) ((m6805.u8_a | t) & 0xFF);
        CLR_NZ();
        SET_NZ8(m6805.u8_a);
    }

    /* $fb ADDA indexed **** */
    public static void adda_ix() {
        char t, r;
        t = IDXBYTE();
        r = (char) (m6805.u8_a + t);
        CLR_HNZC();
        SET_FLAGS8(m6805.u8_a, t, r);
        SET_H(m6805.u8_a, t, r);
        m6805.u8_a = (char) (r & 0xFF);
    }

    /* $fc JMP indexed -*** */
    public static void jmp_ix() {
        INDEXED();
        m6805.pc.SetD(ea.D);
    }

    /* $fd JSR indexed ---- */
    public static void jsr_ix() {
        INDEXED();
        PUSHWORD(m6805.pc);
        m6805.pc.SetD(ea.D);
    }

    /* $fe LDX indexed -**- */
    public static void ldx_ix() {
        m6805.u8_x = IDXBYTE();
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
    }

    /* $ff STX indexed -**- */
    public static void stx_ix() {
        CLR_NZ();
        SET_NZ8(m6805.u8_x);
        INDEXED();
        WM(ea.D, m6805.u8_x);
    }

}
