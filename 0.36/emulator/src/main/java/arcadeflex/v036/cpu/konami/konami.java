/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.konami;

//cpu imports
import static arcadeflex.v036.cpu.konami.konamiH.*;
import static arcadeflex.v036.cpu.konami.konamtbl.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.driverH.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class konami extends cpu_interface {

    public static FILE konamilog = null;//fopen("konami.log", "wa");  //for debug purposes

    public static abstract interface konami_cpu_setlines_callbackPtr {

        public abstract void handler(int lines);
    }
    public static int[] konami_ICount = new int[1];
    public static konami_cpu_setlines_callbackPtr konami_cpu_setlines_callback;

    public konami() {
        cpu_num = CPU_KONAMI;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = KONAMI_INT_NONE;
        irq_int = KONAMI_INT_IRQ;
        nmi_int = KONAMI_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;

        icount = konami_ICount;
        konami_ICount[0] = 50000;

        //intialize interfaces
        burn = burn_function;

    }

    /* Konami Registers */
    public static class konami_Regs {

        public /*PAIR*/ int pc;
        /* Program counter */

        public /*PAIR*/ int ppc;
        /* Previous program counter */

        public int a;
        public int b;   //PAIR	d;		/* Accumulator a and b */

        public /*PAIR*/ int dp;
        /* Direct Page register (page in MSB) */

        public int u;
        public int s;//PAIR	u, s;		/* Stack pointers */
        public int x;
        public int y;//PAIR	x, y;		/* Index registers */
        public int /*UINT8*/ cc;
        public int /*UINT8*/ ireg;
        /* First static opcode */

        public int[] /*UINT8*/ irq_state = new int[2];
        public int extra_cycles;
        /* cycles used up by interrupts */

        public IrqCallbackHandlerPtr irq_callback;
        public int /*UINT8*/ int_state;
        /* SYNC and CWAI flags */

        public int /*UINT8*/ nmi_state;
    }

    static int getDreg()//compose dreg
    {
        return (konami.a << 8 | konami.b) & 0xFFFF;
    }

    static void setDreg(int reg) //write to dreg
    {
        konami.a = reg >> 8 & 0xFF;
        konami.b = reg & 0xFF;
    }

    /* flag bits in the cc register */
    public static final int CC_C = 0x01;
    /* Carry */

    public static final int CC_V = 0x02;
    /* Overflow */

    public static final int CC_Z = 0x04;
    /* Zero */

    public static final int CC_N = 0x08;
    /* Negative */

    public static final int CC_II = 0x10;
    /* Inhibit IRQ */

    public static final int CC_H = 0x20;
    /* Half (auxiliary) carry */

    public static final int CC_IF = 0x40;
    /* Inhibit FIRQ */

    public static final int CC_E = 0x80;
    /* entire state pushed */

 /* Konami registers */
    public static konami_Regs konami = new konami_Regs();

    /*TODO*///#define	pPPC    konami.ppc
/*TODO*///#define pPC 	konami.pc
/*TODO*///#define pU		konami.u
/*TODO*///#define pS		konami.s
/*TODO*///#define pX		konami.x
/*TODO*///#define pY		konami.y
/*TODO*///#define pD		konami.d
/*TODO*///
/*TODO*///#define	PPC		konami.ppc.w.l
/*TODO*///#define PC  	konami.pc.w.l
/*TODO*///#define PCD 	konami.pc.d
/*TODO*///#define U		konami.u.w.l
/*TODO*///#define UD		konami.u.d
/*TODO*///#define S		konami.s.w.l
/*TODO*///#define SD		konami.s.d
/*TODO*///#define X		konami.x.w.l
/*TODO*///#define XD		konami.x.d
/*TODO*///#define Y		konami.y.w.l
/*TODO*///#define YD		konami.y.d
/*TODO*///#define D   	konami.d.w.l
/*TODO*///#define A   	konami.d.b.h
/*TODO*///#define B		konami.d.b.l
/*TODO*///#define DP		konami.dp.b.h
/*TODO*///#define DPD 	konami.dp.d
/*TODO*///#define CC  	konami.cc
/*TODO*///
/*TODO*///static PAIR ea;         /* effective address */
/*TODO*///#define EA	ea.w.l
/*TODO*///#define EAD ea.d
    public static int ea;

    public static final int KONAMI_CWAI = 8;
    /* set when CWAI is waiting for an interrupt */

    public static final int KONAMI_SYNC = 16;
    /* set when SYNC is waiting for an interrupt */

    public static final int KONAMI_LDS = 32;

    /* set when LDS occured at least once */
    public static void CHECK_IRQ_LINES() {
        if (konamilog != null) {
            fprintf(konamilog, "konami#%d irq_lines_en :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
        }
        if (konami.irq_state[KONAMI_IRQ_LINE] != CLEAR_LINE || konami.irq_state[KONAMI_FIRQ_LINE] != CLEAR_LINE) {
            konami.int_state &= ~KONAMI_SYNC;
            /* clear SYNC flag */

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d irq_lines_0 :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
        if (konami.irq_state[KONAMI_FIRQ_LINE] != CLEAR_LINE && ((konami.cc & CC_IF) == 0)) {
            /* fast IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((konami.int_state & KONAMI_CWAI) != 0) {
                konami.int_state &= ~KONAMI_CWAI;
                /* clear CWAI */

                konami.extra_cycles += 7;
                /* subtract +7 cycles */

            } else {
                konami.cc &= ~CC_E;
                /* save 'short' state */

                PUSHWORD(konami.pc);
                PUSHBYTE(konami.cc);
                konami.extra_cycles += 10;
                /* subtract +10 cycles */

            }
            konami.cc |= CC_IF | CC_II;
            /* inhibit FIRQ and IRQ */

            konami.pc = RM16(0xfff6);
            change_pc(konami.pc);
            konami.irq_callback.handler(KONAMI_FIRQ_LINE);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d irq_lines_a :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        } else if (konami.irq_state[KONAMI_IRQ_LINE] != CLEAR_LINE && ((konami.cc & CC_II) == 0)) {
            /* standard IRQ */
 /* HJB 990225: state already saved by CWAI? */
            if ((konami.int_state & KONAMI_CWAI) != 0) {
                konami.int_state &= ~KONAMI_CWAI;
                /* clear CWAI flag */

                konami.extra_cycles += 7;
                /* subtract +7 cycles */

            } else {
                konami.cc |= CC_E;
                /* save entire state */

                PUSHWORD(konami.pc);
                PUSHWORD(konami.u);
                PUSHWORD(konami.y);
                PUSHWORD(konami.x);
                PUSHBYTE(konami.dp);
                PUSHBYTE(konami.b);
                PUSHBYTE(konami.a);
                PUSHBYTE(konami.cc);
                konami.extra_cycles += 19;
                /* subtract +19 cycles */

            }
            konami.cc |= CC_II;
            /* inhibit IRQ */

            konami.pc = RM16(0xfff8);
            change_pc(konami.pc);
            konami.irq_callback.handler(KONAMI_IRQ_LINE);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d irq_lines_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }

    }


    /*TODO*///int konami_Flags;	/* flags for speed optimization (obsolete!!) */
/*TODO*///void (*konami_cpu_setlines_callback)( int lines ) = 0; /* callback called when A16-A23 are set */
/*TODO*///
    /* these are re-defined in konami.h TO RAM, ROM or functions in memory.c */
    public static int RM(int addr) {
        return (cpu_readmem16(addr) & 0xFF);
    }

    public static void WM(int addr, int value) {
        cpu_writemem16(addr, value);
    }

    public static char ROP(int addr) {
        return cpu_readop(addr);
    }

    public static char ROP_ARG(int addr) {
        return cpu_readop_arg(addr);
    }

    /*TODO*///
/*TODO*///#define SIGNED(a)	(UINT16)(INT16)(INT8)(a)
/*TODO*///
    //* macros to access memory */
    public static int IMMBYTE() {
        int reg = ROP_ARG(konami.pc);
        konami.pc = konami.pc + 1 & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
    }

    public static int IMMWORD() {
        int reg = (ROP_ARG(konami.pc) << 8) | ROP_ARG((konami.pc + 1) & 0xffff);
        konami.pc = konami.pc + 2 & 0xFFFF;
        return reg;
    }

    public static void PUSHBYTE(int w) {
        konami.s = konami.s - 1 & 0xFFFF;
        WM(konami.s, w);
    }

    public static void PUSHWORD(int w) {
        konami.s = konami.s - 1 & 0xFFFF;
        WM(konami.s, w & 0xFF);
        konami.s = konami.s - 1 & 0xFFFF;
        WM(konami.s, w >> 8);
    }

    public static int PULLBYTE() {
        int b = RM(konami.s);
        konami.s = konami.s + 1 & 0xFFFF;
        return b;
    }

    public static int PULLWORD()//TODO recheck
    {
        int w = (RM(konami.s) << 8);
        konami.s = (konami.s + 1) & 0xFFFF;
        w |= RM(konami.s);
        konami.s = (konami.s + 1) & 0xFFFF;
        return w;
    }

    public static void PSHUBYTE(int w) {
        konami.u = konami.u - 1 & 0xFFFF;
        WM(konami.u, w);
    }

    public static void PSHUWORD(int w) {
        konami.u = konami.u - 1 & 0xFFFF;
        WM(konami.u, w & 0xFF);
        konami.u = konami.u - 1 & 0xFFFF;
        WM(konami.u, w >> 8);
    }

    public static int PULUBYTE() {
        int b = RM(konami.u);
        konami.u = konami.u + 1 & 0xFFFF;
        return b;
    }

    public static int PULUWORD()//TODO recheck
    {
        int w = RM(konami.u) << 8;
        konami.u = konami.u + 1 & 0xFFFF;
        w |= RM(konami.u);
        konami.u = konami.u + 1 & 0xFFFF;
        return w;
    }

    public static void CLR_HNZVC() {
        konami.cc &= ~(CC_H | CC_N | CC_Z | CC_V | CC_C);
    }

    public static void CLR_NZV() {
        konami.cc &= ~(CC_N | CC_Z | CC_V);
    }

    /*TODO*///#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
    public static void CLR_NZVC() {
        konami.cc &= ~(CC_N | CC_Z | CC_V | CC_C);
    }

    public static void CLR_Z() {
        konami.cc &= ~(CC_Z);
    }

    public static void CLR_NZC() {
        konami.cc &= ~(CC_N | CC_Z | CC_C);
    }

    public static void CLR_ZC() {
        konami.cc &= ~(CC_Z | CC_C);
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
        konami.cc |= ((a & 0x80) >> 4);
    }

    public static void SET_N16(int a) {
        konami.cc |= ((a & 0x8000) >> 12);
    }

    public static void SET_H(int a, int b, int r) {
        konami.cc |= (((a ^ b ^ r) & 0x10) << 1);
    }

    public static void SET_C8(int a) {
        konami.cc |= ((a & 0x100) >> 8);
    }

    public static void SET_C16(int a) {
        konami.cc |= ((a & 0x10000) >> 16);
    }

    public static void SET_V8(int a, int b, int r) {
        konami.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x80) >> 6);
    }

    public static void SET_V16(int a, int b, int r) {
        konami.cc |= (((a ^ b ^ r ^ (r >> 1)) & 0x8000) >> 14);
    }

    static int flags8i[]
            = /* increment */ {
                CC_Z, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                CC_N | CC_V, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N
            };
    static int flags8d[]
            = /* decrement */ {
                CC_Z, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, CC_V,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N,
                CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N, CC_N
            };

    public static void SET_FLAGS8I(int a) {
        konami.cc |= flags8i[(a) & 0xff];
    }

    public static void SET_FLAGS8D(int a) {
        konami.cc |= flags8d[(a) & 0xff];
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

    /* macros for addressing modes (postbytes have their own code) */
    public static void DIRECT()//TODO rececheck!
    {
        ea = IMMBYTE();
        ea |= konami.dp << 8;
    }

    /*TODO*///#define IMM8	EAD = PCD; PC++
/*TODO*///#define IMM16	EAD = PCD; PC+=2
    public static void EXTENDED() {
        ea = IMMWORD();
    }

    /*TODO*///
    /*TODO*////* macros to set status flags */
    public static void SEC() {
        konami.cc |= CC_C;
    }

    /*TODO*///#define CLC CC&=~CC_C
    public static void SEZ() {
        konami.cc |= CC_Z;
    }

    /*TODO*///#define CLZ CC&=~CC_Z
    /*TODO*///#define SEN CC|=CC_N
    /*TODO*///#define CLN CC&=~CC_N
    /*TODO*///#define SEV CC|=CC_V
    /*TODO*///#define CLV CC&=~CC_V
    /*TODO*///#define SEH CC|=CC_H
    /*TODO*///#define CLH CC&=~CC_H

    /* macros for convenience */
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

    /* macros for branch instructions */
    public static void BRANCH(boolean f) {
        int t = IMMBYTE();
        if (f) {
            konami.pc = (konami.pc + ((byte) t & 0xFFFF)) & 0xFFFF;
            change_pc(konami.pc);
        }
    }

    public static void LBRANCH(boolean f) {
        int t = IMMWORD();
        if (f) {
            konami_ICount[0] -= 1;
            konami.pc = (konami.pc + t) & 0xFFFF;
            change_pc(konami.pc);
        }
    }

    public static int NXORV() {
        return ((konami.cc & CC_N) ^ ((konami.cc & CC_V) << 2));
    }

    /*TODO*///
/*TODO*////* macros for setting/getting registers in TFR/EXG instructions */
/*TODO*///#define GETREG(val,reg) 				\
/*TODO*///	switch(reg) {						\
/*TODO*///	case 0: val = A;	break;			\
/*TODO*///	case 1: val = B; 	break; 			\
/*TODO*///	case 2: val = X; 	break;			\
/*TODO*///	case 3: val = Y;	break; 			\
/*TODO*///	case 4: val = S; 	break; /* ? */	\
/*TODO*///	case 5: val = U;	break;			\
/*TODO*///	default: val = 0xff; if ( errorlog ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", PC ); break; \
/*TODO*///}
/*TODO*///
/*TODO*///#define SETREG(val,reg) 				\
/*TODO*///	switch(reg) {						\
/*TODO*///	case 0: A = val;	break;			\
/*TODO*///	case 1: B = val;	break;			\
/*TODO*///	case 2: X = val; 	break;			\
/*TODO*///	case 3: Y = val;	break;			\
/*TODO*///	case 4: S = val;	break; /* ? */	\
/*TODO*///	case 5: U = val; 	break;			\
/*TODO*///	default: if ( errorlog ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", PC ); break; \
/*TODO*///}
/*TODO*///
    /* static opcode timings */
    static int cycles1[]
            = {
                /*	 0	1  2  3  4	5  6  7  8	9  A  B  C	D  E  F */
                /*0*/1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 5, 5, 5, 5,
                /*1*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                /*2*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                /*3*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 7, 6,
                /*4*/ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 4, 4,
                /*5*/ 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 1, 1, 1,
                /*6*/ 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
                /*7*/ 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
                /*8*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5,
                /*9*/ 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 6,
                /*A*/ 2, 2, 2, 4, 4, 4, 4, 4, 2, 2, 2, 2, 3, 3, 2, 1,
                /*B*/ 3, 2, 2, 11, 22, 11, 2, 4, 3, 3, 3, 3, 3, 3, 3, 3,
                /*C*/ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3, 2,
                /*D*/ 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                /*E*/ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                /*F*/ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
            };

    static int RM16(int addr) {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i;
    }

    static void WM16(int addr, int reg) {
        WM(addr + 1 & 0xFFFF, reg & 0xFF);
        WM(addr, reg >> 8);
    }

    @Override
    public Object init_context() {
        Object reg = new konami_Regs();
        return reg;
    }

    /*TODO*////****************************************************************************
/*TODO*/// * Get all registers in given buffer
/*TODO*/// ****************************************************************************/
/*TODO*///unsigned konami_get_context(void *dst)
/*TODO*///{
/*TODO*///	if( dst )
/*TODO*///		*(konami_Regs*)dst = konami;
/*TODO*///	return sizeof(konami_Regs);
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set all registers to given values
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_context(void *src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///		konami = *(konami_Regs*)src;
/*TODO*///    change_pc(PC);    /* TS 971002 */
/*TODO*///
/*TODO*///    CHECK_IRQ_LINES;
/*TODO*///}
    /**
     * **************************************************************************
     * Return program counter
     * **************************************************************************
     */
    @Override
    public int get_pc() {
        return konami.pc & 0xFFFF;
    }


    /*TODO*////****************************************************************************
/*TODO*/// * Set program counter
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_pc(unsigned val)
/*TODO*///{
/*TODO*///	PC = val;
/*TODO*///	change_pc(PC);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return stack pointer
/*TODO*/// ****************************************************************************/
/*TODO*///unsigned konami_get_sp(void)
/*TODO*///{
/*TODO*///	return S;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set stack pointer
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_sp(unsigned val)
/*TODO*///{
/*TODO*///	S = val;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Return a specific register                                               */
/*TODO*////****************************************************************************/
/*TODO*///unsigned konami_get_reg(int regnum)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case KONAMI_PC: return PC;
/*TODO*///		case KONAMI_S: return S;
/*TODO*///		case KONAMI_CC: return CC;
/*TODO*///		case KONAMI_U: return U;
/*TODO*///		case KONAMI_A: return A;
/*TODO*///		case KONAMI_B: return B;
/*TODO*///		case KONAMI_X: return X;
/*TODO*///		case KONAMI_Y: return Y;
/*TODO*///		case KONAMI_DP: return DP;
/*TODO*///		case KONAMI_NMI_STATE: return konami.nmi_state;
/*TODO*///		case KONAMI_IRQ_STATE: return konami.irq_state[KONAMI_IRQ_LINE];
/*TODO*///		case KONAMI_FIRQ_STATE: return konami.irq_state[KONAMI_FIRQ_LINE];
/*TODO*///		case REG_PREVIOUSPC: return PPC;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///					return ( RM( offset ) << 8 ) | RM( offset + 1 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Set a specific register                                                  */
/*TODO*////****************************************************************************/
/*TODO*///void konami_set_reg(int regnum, unsigned val)
/*TODO*///{
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case KONAMI_PC: PC = val; change_pc(PC); break;
/*TODO*///		case KONAMI_S: S = val; break;
/*TODO*///		case KONAMI_CC: CC = val; CHECK_IRQ_LINES; break;
/*TODO*///		case KONAMI_U: U = val; break;
/*TODO*///		case KONAMI_A: A = val; break;
/*TODO*///		case KONAMI_B: B = val; break;
/*TODO*///		case KONAMI_X: X = val; break;
/*TODO*///		case KONAMI_Y: Y = val; break;
/*TODO*///		case KONAMI_DP: DP = val; break;
/*TODO*///		case KONAMI_NMI_STATE: konami.nmi_state = val; break;
/*TODO*///		case KONAMI_IRQ_STATE: konami.irq_state[KONAMI_IRQ_LINE] = val; break;
/*TODO*///		case KONAMI_FIRQ_STATE: konami.irq_state[KONAMI_FIRQ_LINE] = val; break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///				{
/*TODO*///					WM( offset, (val >> 8) & 0xff );
/*TODO*///					WM( offset+1, val & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     */
    /* Reset registers to their initial values									*/
    /**
     * *************************************************************************
     */
    @Override
    public void reset(Object param) {
        konami.int_state = 0;
        konami.nmi_state = CLEAR_LINE;
        konami.irq_state[0] = CLEAR_LINE;
        konami.irq_state[0] = CLEAR_LINE;

        konami.dp = 0;
        /* Reset direct page register */

        konami.cc |= CC_II;
        /* IRQ disabled */

        konami.cc |= CC_IF;
        /* FIRQ disabled */

        konami.pc = RM16(0xfffe);
        change_pc(konami.pc);
    }

    /*TODO*///void konami_exit(void)
/*TODO*///{
/*TODO*///	/* just make sure we deinit this, so the next game set its own */
/*TODO*///	konami_cpu_setlines_callback = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* Generate interrupts */
/*TODO*////****************************************************************************
/*TODO*/// * Set NMI line state
/*TODO*/// ****************************************************************************/
/*TODO*///void konami_set_nmi_line(int state)
/*TODO*///{
/*TODO*///	if (konami.nmi_state == state) return;
/*TODO*///	konami.nmi_state = state;
/*TODO*///	LOG((errorlog, "KONAMI#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
/*TODO*///	if( state == CLEAR_LINE ) return;
/*TODO*///
/*TODO*///	/* if the stack was not yet initialized */
/*TODO*///    if( !(konami.int_state & KONAMI_LDS) ) return;
/*TODO*///
/*TODO*///    konami.int_state &= ~KONAMI_SYNC;
/*TODO*///	/* state already saved by CWAI? */
/*TODO*///	if( konami.int_state & KONAMI_CWAI )
/*TODO*///	{
/*TODO*///		konami.int_state &= ~KONAMI_CWAI;
/*TODO*///		konami.extra_cycles += 7;	/* subtract +7 cycles next time */
/*TODO*///    }
/*TODO*///	else
/*TODO*///	{
/*TODO*///		CC |= CC_E; 				/* save entire state */
/*TODO*///		PUSHWORD(pPC);
/*TODO*///		PUSHWORD(pU);
/*TODO*///		PUSHWORD(pY);
/*TODO*///		PUSHWORD(pX);
/*TODO*///		PUSHBYTE(DP);
/*TODO*///		PUSHBYTE(B);
/*TODO*///		PUSHBYTE(A);
/*TODO*///		PUSHBYTE(CC);
/*TODO*///		konami.extra_cycles += 19;	/* subtract +19 cycles next time */
/*TODO*///	}
/*TODO*///	CC |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
/*TODO*///	PCD = RM16(0xfffc);
/*TODO*///	change_pc(PC);					/* TS 971002 */
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Set IRQ line state
/*TODO*/// ****************************************************************************/
    @Override
    public void set_irq_line(int irqline, int linestate) {
        if (konamilog != null) {
            fprintf(konamilog, "KONAMI#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, linestate);
        }
        konami.irq_state[irqline] = linestate;
        if (linestate == CLEAR_LINE) {
            return;
        }
        CHECK_IRQ_LINES();
    }

    /**
     * **************************************************************************
     * Set IRQ vector callback
     * **************************************************************************
     */
    @Override
    public void set_irq_callback(IrqCallbackHandlerPtr callback) {
        konami.irq_callback = callback;
    }

    /*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Save CPU state
/*TODO*/// ****************************************************************************/
/*TODO*///static void state_save(void *file, const char *module)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file, module, cpu, "PC", &PC, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "U", &U, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "S", &S, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "X", &X, 1);
/*TODO*///	state_save_UINT16(file, module, cpu, "Y", &Y, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "DP", &DP, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "CC", &CC, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "INT", &konami.int_state, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "NMI", &konami.nmi_state, 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "IRQ", &konami.irq_state[0], 1);
/*TODO*///	state_save_UINT8(file, module, cpu, "FIRQ", &konami.irq_state[1], 1);
/*TODO*///}
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Load CPU state
/*TODO*/// ****************************************************************************/
/*TODO*///static void state_load(void *file, const char *module)
/*TODO*///{
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file, module, cpu, "PC", &PC, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "U", &U, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "S", &S, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "X", &X, 1);
/*TODO*///	state_load_UINT16(file, module, cpu, "Y", &Y, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "DP", &DP, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "CC", &CC, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "INT", &konami.int_state, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "NMI", &konami.nmi_state, 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "IRQ", &konami.irq_state[0], 1);
/*TODO*///	state_load_UINT8(file, module, cpu, "FIRQ", &konami.irq_state[1], 1);
/*TODO*///}
/*TODO*///
/*TODO*///void konami_state_save(void *file) { state_save(file, "konami"); }
/*TODO*///void konami_state_load(void *file) { state_load(file, "konami"); }
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return a formatted string for a register
/*TODO*/// ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	konami_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &konami;
/*TODO*///
        switch (regnum) {
            case CPU_INFO_NAME:
                return "KONAMI";
            case CPU_INFO_FAMILY:
                return "KONAMI 5000x";
            case CPU_INFO_VERSION:
                return "1.0";
            case CPU_INFO_FILE:
                return "konami.java";
            case CPU_INFO_CREDITS:
                return "Copyright (C) The MAME Team 1999";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char*)konami_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char*)konami_win_layout;
/*TODO*///
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->cc & 0x80 ? 'E':'.',
/*TODO*///				r->cc & 0x40 ? 'F':'.',
/*TODO*///                r->cc & 0x20 ? 'H':'.',
/*TODO*///                r->cc & 0x10 ? 'I':'.',
/*TODO*///                r->cc & 0x08 ? 'N':'.',
/*TODO*///                r->cc & 0x04 ? 'Z':'.',
/*TODO*///                r->cc & 0x02 ? 'V':'.',
/*TODO*///                r->cc & 0x01 ? 'C':'.');
/*TODO*///            break;
/*TODO*///		case CPU_INFO_REG+KONAMI_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_U: sprintf(buffer[which], "U:%04X", r->u.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_A: sprintf(buffer[which], "A:%02X", r->d.b.h); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_B: sprintf(buffer[which], "B:%02X", r->d.b.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_Y: sprintf(buffer[which], "Y:%04X", r->y.w.l); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_DP: sprintf(buffer[which], "DP:%02X", r->dp.b.h); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[KONAMI_IRQ_LINE]); break;
/*TODO*///		case CPU_INFO_REG+KONAMI_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r->irq_state[KONAMI_FIRQ_LINE]); break;
        }
        throw new UnsupportedOperationException("unsupported konami cpu_info");
        /*TODO*///	return buffer[which];
    }

    /*TODO*///
/*TODO*///unsigned konami_dasm(char *buffer, unsigned pc)
/*TODO*///{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///    return Dasmknmi(buffer,pc);
/*TODO*///#else
/*TODO*///	sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///	return 1;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*////* includes the static function prototypes and the master static opcode table */
/*TODO*///#include "konamtbl.c"
/*TODO*///
/*TODO*////* includes the actual static opcode implementations */
/*TODO*///#include "konamops.c"
/*TODO*///
    /* execute instructions on this CPU until icount expires */
    @Override
    public int execute(int cycles) {
        konami_ICount[0] = cycles - konami.extra_cycles;
        konami.extra_cycles = 0;

        if ((konami.int_state & (KONAMI_CWAI | KONAMI_SYNC)) != 0) {
            konami_ICount[0] = 0;
        } else {
            do {
                konami.ppc = konami.pc;

                konami.ireg = ROP(konami.pc);
                konami.pc = konami.pc + 1 & 0xFFFF;

                if (konami_main[konami.ireg] != null) {
                    konami_main[konami.ireg].handler();
                } else {
                    System.out.println("Unsupported konami_main instruction 0x" + Integer.toHexString(konami.ireg));
                }

                konami_ICount[0] -= cycles1[konami.ireg];
            } while (konami_ICount[0] > 0);

            konami_ICount[0] -= konami.extra_cycles;
            konami.extra_cycles = 0;
        }

        return cycles - konami_ICount[0];
    }

    public BurnHandlerPtr burn_function = new BurnHandlerPtr() {
        public void handler(int cycles) {
            /*TODO*///            throw new UnsupportedOperationException("Not supported yet.");
            konami_ICount[0] -= cycles;
        }
    };

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }

    static opcode illegal = new opcode() {
        public void handler() {
            if (konamilog != null) {
                fprintf(konamilog, "KONAMI: illegal static opcode at %04x\n", konami.pc);
            }
            printf("KONAMI: illegal static opcode at %04x\n", konami.pc);
        }
    };

    static opcode adca_ex = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = EXTBYTE();
            r = konami.a + t + (konami.cc & CC_C);
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d adca_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode adcb_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode adcb_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode addd_di = new opcode() {
        public void handler() {
            int r, d;
            int b = DIRWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d addd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode addd_ex = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = EXTWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d addd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode addd_im = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d addd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode addd_ix = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b;
            b = RM16(ea);
            d = getDreg();
            r = d + b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d addd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode andcc = new opcode() {
        public void handler() {
            /*UINT8*/
            int t = IMMBYTE();
            konami.cc &= t;
            CHECK_IRQ_LINES();
            /* HJB 990116 */

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d andcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode bita_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode bra = new opcode() {
        public void handler() {
            /*UINT8*/
            int t;
            t = IMMBYTE();
            konami.pc = konami.pc + (byte) t & 0xFFFF;//TODO check if it has to be better...
            change_pc(konami.pc);
            /* JB 970823 - speed up busy loops */
            if (t == 0xfe && konami_ICount[0] > 0) {
                konami_ICount[0] = 0;
            }

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d bra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode brn = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode bsr = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            PUSHWORD(konami.pc);
            konami.pc = konami.pc + (byte) t & 0xFFFF;
            change_pc(konami.pc);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d bsr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode bvc = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode bvs = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode cmpd_di = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b;
            b = DIRWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpd_ex = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = EXTWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpd_im = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpd_ix = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b;
            b = RM16(ea);
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            /*if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }*/

        }
    };
    static opcode cmps_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode cmps_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode cmps_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode cmps_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode cmpu_di = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = DIRWORD();
            d = konami.u;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpu_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpu_ex = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = EXTWORD();
            d = konami.u;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpu_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpu_im = new opcode() {
        public void handler()//probably ok
        {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = konami.u;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            /*if (konamilog != null) {
             fprintf(konamilog, "konami#%d cmpu_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
             }*/

        }
    };
    static opcode cmpu_ix = new opcode() {
        public void handler() {
            /*UINT32*/
            int r;
            int b;
            b = RM16(ea);
            r = konami.u - b;
            CLR_NZVC();
            SET_FLAGS16(konami.u, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpu_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpx_di = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = DIRWORD();
            d = konami.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpx_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpx_ex = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = EXTWORD();
            d = konami.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            /*if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpx_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }*/

        }
    };
    static opcode cmpx_im = new opcode() {
        public void handler()//todo recheck
        {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = konami.x;
            r = (d - b); //&0xFFFF;//should be unsigned?
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            /*if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpx_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }*/

        }
    };
    static opcode cmpx_ix = new opcode() {
        public void handler()//probably ok
        {
            /*UINT32*/
            int r, d;
            int b;
            b = RM16(ea);
            d = konami.x;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            /*if (konamilog != null) {
             fprintf(konamilog, "konami#%d cmpx_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
             }*/
        }
    };
    static opcode cmpy_di = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = DIRWORD();
            d = konami.y;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpy_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode cmpy_ex = new opcode() {
        public void handler() {
            int r, d;
            int b = EXTWORD();
            d = konami.y;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpy_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode cmpy_im = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = konami.y;
            r = (d - b);
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpy_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode cmpy_ix = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b;
            b = RM16(ea);
            d = konami.y;
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d cmpy_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode cwai = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode daa = new opcode() { //todo recheck
        public void handler() {
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d daa_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

            int/*UINT8*/ msn, lsn;
            int/*UINT16*/ t, cf = 0;
            msn = konami.a & 0xf0;
            lsn = konami.a & 0x0f;
            if (lsn > 0x09 || (konami.cc & CC_H) != 0) {
                cf |= 0x06;
            }
            if (msn > 0x80 && lsn > 0x09) {
                cf |= 0x60;
            }
            if (msn > 0x90 || (konami.cc & CC_C) != 0) {
                cf |= 0x60;
            }
            t = cf + konami.a;
            CLR_NZV();
            /* keep carry from previous operation */

            SET_NZ8(/*(UINT8)*/t & 0xFF);
            SET_C8(t);
            konami.a = (t) & 0xFF;

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d daa_a :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode exg = new opcode() { //todo recheck
        public void handler() {
            /*UINT16*/
            int t1, t2;
            /*UINT8*/
            int tb;
            tb = IMMBYTE();
            //GETREG( t1, tb >> 4 );
            switch (tb >> 4) {
                case 0:
                    t1 = konami.a;
                    break;
                case 1:
                    t1 = konami.b;
                    break;
                case 2:
                    t1 = konami.x;
                    break;
                case 3:
                    t1 = konami.y;
                    break;
                case 4:
                    t1 = konami.s;
                    break;
                /* ? */

                case 5:
                    t1 = konami.u;
                    break;
                default:
                    t1 = 0xff;
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            //GETREG( t2, tb & 0x0f );
            switch (tb & 0x0f) {
                case 0:
                    t2 = konami.a;
                    break;
                case 1:
                    t2 = konami.b;
                    break;
                case 2:
                    t2 = konami.x;
                    break;
                case 3:
                    t2 = konami.y;
                    break;
                case 4:
                    t2 = konami.s;
                    break;
                /* ? */

                case 5:
                    t2 = konami.u;
                    break;
                default:
                    t2 = 0xff;
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            //SETREG( t2, tb >> 4 );
            switch (tb >> 4) {
                case 0:
                    konami.a = (t2) & 0xFF;
                    break;
                case 1:
                    konami.b = (t2) & 0xFF;
                    break;
                case 2:
                    konami.x = t2;
                    break;
                case 3:
                    konami.y = t2;
                    break;
                case 4:
                    konami.s = t2;
                    break;
                /* ? */

                case 5:
                    konami.u = t2;
                    break;
                default:
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            //SETREG( t1, tb & 0x0f );
            switch (tb & 0x0f) {
                case 0:
                    konami.a = (t1) & 0xFF;
                    break;
                case 1:
                    konami.b = (t1) & 0xFF;
                    break;
                case 2:
                    konami.x = t1;
                    break;
                case 3:
                    konami.y = t1;
                    break;
                case 4:
                    konami.s = t1;
                    break;
                /* ? */

                case 5:
                    konami.u = t1;
                    break;
                default:
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d exg :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode jmp_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode lbra = new opcode() {
        public void handler() {
            ea = IMMWORD();
            konami.pc = konami.pc + ea & 0xFFFF;
            change_pc(konami.pc);

            if (ea == 0xfffd && konami_ICount[0] > 0) {
                konami_ICount[0] = 0;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lbra :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lbrn = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode lbsr = new opcode() {
        public void handler() {
            ea = IMMWORD();
            PUSHWORD(konami.pc);
            konami.pc = konami.pc + ea & 0xFFFF;
            change_pc(konami.pc);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lbsr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lbvc = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode lbvs = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode mul = new opcode() {
        public void handler() {
            /*UINT16*/
            int t;
            t = (konami.a * konami.b) & 0xFFFF;
            CLR_ZC();
            SET_Z16(t);
            if ((t & 0x80) != 0) {
                SEC();
            }
            setDreg(t & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d mul :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode orcc = new opcode() {
        public void handler() {
            /*UINT8*/
            int t = IMMBYTE();
            konami.cc |= t;
            CHECK_IRQ_LINES();
            /* HJB 990116 */

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d orcc :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode pshs = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x80) != 0) {
                PUSHWORD(konami.pc);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                PUSHWORD(konami.u);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                PUSHWORD(konami.y);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x10) != 0) {
                PUSHWORD(konami.x);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x08) != 0) {
                PUSHBYTE(konami.dp);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                PUSHBYTE(konami.b);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                PUSHBYTE(konami.a);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x01) != 0) {
                PUSHBYTE(konami.cc);
                konami_ICount[0] -= 1;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d pshs :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode pshu = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x80) != 0) {
                PSHUWORD(konami.pc);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                PSHUWORD(konami.s);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                PSHUWORD(konami.y);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x10) != 0) {
                PSHUWORD(konami.x);
                konami_ICount[0] -= 2;
            }
            if ((t & 0x08) != 0) {
                PSHUBYTE(konami.dp);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                PSHUBYTE(konami.b);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                PSHUBYTE(konami.a);
                konami_ICount[0] -= 1;
            }
            if ((t & 0x01) != 0) {
                PSHUBYTE(konami.cc);
                konami_ICount[0] -= 1;
            }

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d pshu :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode puls = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x01) != 0) {
                konami.cc = PULLBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                konami.a = (PULLBYTE());
                konami_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                konami.b = (PULLBYTE());
                konami_ICount[0] -= 1;
            }
            if ((t & 0x08) != 0) {
                konami.dp = PULLBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x10) != 0) {
                konami.x = PULLWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                konami.y = PULLWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                konami.u = PULLWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x80) != 0) {
                konami.pc = PULLWORD();
                change_pc(konami.pc);
                konami_ICount[0] -= 2;
            }

            /* check after all PULLs */
            if ((t & 0x01) != 0) {
                CHECK_IRQ_LINES();
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d puls :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode pulu = new opcode() {
        public void handler() {
            int t = IMMBYTE();
            if ((t & 0x01) != 0) {
                konami.cc = PULUBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x02) != 0) {
                konami.a = PULUBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x04) != 0) {
                konami.b = PULUBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x08) != 0) {
                konami.dp = PULUBYTE();
                konami_ICount[0] -= 1;
            }
            if ((t & 0x10) != 0) {
                konami.x = PULUWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x20) != 0) {
                konami.y = PULUWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x40) != 0) {
                konami.s = PULUWORD();
                konami_ICount[0] -= 2;
            }
            if ((t & 0x80) != 0) {
                konami.pc = PULUWORD();
                change_pc(konami.pc);
                konami_ICount[0] -= 2;
            }

            /* check after all PULLs */
            if ((t & 0x01) != 0) {
                CHECK_IRQ_LINES();
            }
        }
    };

    static opcode ror_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode rti = new opcode() {
        public void handler() {
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d rti_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
            int t;
            konami.cc = PULLBYTE();
            t = konami.cc & CC_E;
            /* HJB 990225: entire state saved? */

            if (t != 0) {
                konami_ICount[0] -= 9;
                konami.a = (PULLBYTE());
                konami.b = (PULLBYTE());
                konami.dp = PULLBYTE();
                konami.x = PULLWORD();
                konami.y = PULLWORD();
                konami.u = PULLWORD();
            }
            konami.pc = PULLWORD();
            change_pc(konami.pc);
            CHECK_IRQ_LINES();
            /* HJB 990116 */

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d rti :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode rts = new opcode() {
        public void handler() {
            konami.pc = PULLWORD();
            change_pc(konami.pc);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d rts :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode sbca_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode sbcb_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode sbcb_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode sbcb_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode sex = new opcode() {
        public void handler() {
            int t = (byte) konami.b & 0xFFFF;
            setDreg(t);
            CLR_NZV();
            SET_NZ16(t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d sex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode sta_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode stb_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode std_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode sts_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode stu_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode stx_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode sty_im = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };

    static opcode subd_di = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = DIRWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d subd_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode subd_ex = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b = EXTWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d subd_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode subd_im = new opcode() {
        public void handler()//probably ok
        {
            /*UINT32*/
            int r, d;
            int b = IMMWORD();
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            /*if (konamilog != null) {
                fprintf(konamilog, "konami#%d subd_im :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }*/

        }
    };
    static opcode subd_ix = new opcode() {
        public void handler() {
            /*UINT32*/
            int r, d;
            int b;
            b = RM16(ea);
            d = getDreg();
            r = d - b;
            CLR_NZVC();
            SET_FLAGS16(d, b, r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d subd_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode swi = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode swi2 = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode swi3 = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode sync = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode tfr = new opcode() {
        public void handler() {
            /*UINT8*/
            int tb;
            /*UINT16*/ int t = 0;
            tb = IMMBYTE();
            //GETREG( t, tb & 0x0f );
            switch (tb & 0x0f) {
                case 0:
                    t = konami.a;
                    break;
                case 1:
                    t = konami.b;
                    break;
                case 2:
                    t = konami.x;
                    break;
                case 3:
                    t = konami.y;
                    break;
                case 4:
                    t = konami.s;
                    break;
                /* ? */

                case 5:
                    t = konami.u;
                    break;
                default:
                    t = 0xff;
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            //SETREG( t, ( tb >> 4 ) & 0x07 );
            switch ((tb >> 4) & 0x07) {
                case 0:
                    konami.a = (t) & 0xFF;
                    break;
                case 1:
                    konami.b = (t) & 0xFF;
                    break;
                case 2:
                    konami.x = t;
                    break;
                case 3:
                    konami.y = t;
                    break;
                case 4:
                    konami.s = t;
                    break;
                /* ? */

                case 5:
                    konami.u = t;
                    break;
                default:
                    if (konamilog != null) {
                        fprintf(konamilog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc);
                    }
                    break;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d tfr :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode negd = new opcode() {
        public void handler() {

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d negd_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
            int r = -getDreg();
            CLR_NZVC();
            SET_FLAGS16(0, getDreg(), r);
            setDreg(r & 0xFFFF);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d negd_a :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode negw_ix = new opcode() {
        public void handler() {
            int t = RM16(ea);
            int r = -t;
            CLR_NZVC();
            SET_FLAGS16(0, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d negw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    /* 6309 ? */

    static opcode negw_di = new opcode() {
        public void handler() {
            int t = DIRWORD();
            int r = -t;
            CLR_NZVC();
            SET_FLAGS16(0, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d negw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    /* 6309 ? */

    static opcode negw_ex = new opcode() {
        public void handler() {
            int t = EXTWORD();
            int r = -t;
            CLR_NZVC();
            SET_FLAGS16(0, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d negw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    /* 6309 ? */

    static opcode lsrd = new opcode() {
        public void handler() {
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lsrd_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
            //UINT8 t;
            int t = IMMBYTE();

            while (t-- != 0) {
                CLR_NZC();
                konami.cc |= getDreg() & CC_C;
                setDreg(getDreg() >> 1);
                SET_Z16(getDreg());
            }

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lsrd :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    /* 6309 */

    static opcode lsrd_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode lsrd_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode lsrd_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode rord = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 ? */

    static opcode rord_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode rord_ix = new opcode() {
        public void handler() {
            throw new UnsupportedOperationException("unsupported opcode");
            /*
             UINT16 r;
             UINT8  t;
             if(konamilog!=null) fprintf(konamilog,"konami#%d rord_ix_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),konami.pc,konami.ppc,konami.a,konami.b,getDreg(),konami.dp,konami.u,konami.s,konami.x,konami.y,konami.cc,ea);

             t=RM(EA);

             while ( t-- ) {
             r = (CC & CC_C) << 15;
             CLR_NZC;
             CC |= (D & CC_C);
             r |= D >> 1;
             SET_NZ16(r);
             D = r;
             }
             if(konamilog!=null) fprintf(konamilog,"konami#%d rord_ix_a :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(),konami.pc,konami.ppc,konami.a,konami.b,getDreg(),konami.dp,konami.u,konami.s,konami.x,konami.y,konami.cc,ea);
             */
        }
    };
    /* 6309 */

    static opcode rord_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asrd = new opcode() { //to check
        public void handler() {
            int t = IMMBYTE();

            while (t-- != 0) {
                CLR_NZC();
                konami.cc |= (getDreg() & CC_C);
                setDreg((getDreg() & 0x8000) | (getDreg() >> 1));
                SET_NZ16(getDreg());
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d asrd :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    /* 6309 ? */

    static opcode asrd_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asrd_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asrd_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asld = new opcode() {
        public void handler() {

            int t = IMMBYTE();
            while (t-- != 0) {
                int r = getDreg() << 1;
                CLR_NZVC();
                SET_FLAGS16(getDreg(), getDreg(), r);
                setDreg(r & 0xFFFF);
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d asld :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    /* 6309 */

    static opcode asld_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asld_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode asld_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode rold = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 ? */

    static opcode rold_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode rold_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode rold_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    /* 6309 */

    static opcode tstw_di = new opcode() {
        public void handler() {
            int t;//PAIR t;
            CLR_NZV();
            t = DIRWORD();
            SET_NZ16(t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d tstw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode tstw_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode tstw_ex = new opcode() {
        public void handler() {
            int t;//PAIR t;
            CLR_NZV();
            t = EXTWORD();
            SET_NZ16(t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d tstw_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    /* Custom opcodes */
    static opcode bset = new opcode() {
        public void handler() {
            //UINT8	t;
            int t;
            while (konami.u != 0) {
                t = konami.a;
                WM(konami.x, t);
                konami.x = konami.x + 1 & 0xFFFF;//X++;
                konami.u = konami.u - 1 & 0xFFFF;//U--;
                konami_ICount[0] -= 2;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d bset :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode bset2 = new opcode() {
        public void handler() {
            while (konami.u != 0) {
                WM16(konami.x, getDreg());
                konami.x = konami.x + 2 & 0xFFFF;//X += 2;
                konami.u = konami.u - 1 & 0xFFFF;//U--;
                konami_ICount[0] -= 3;
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d bset2 :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lmul = new opcode() {
        public void handler() {
            /*UINT32*/
            int t;
            t = konami.x * konami.y;
            konami.x = (t >> 16) & 0xffff;
            konami.y = (t & 0xffff);
            CLR_ZC();
            SET_Z(t);
            if ((t & 0x8000) != 0) {
                SEC();
            }
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lmul :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode divx = new opcode() {
        public void handler() {
            /*UINT16*/
            int t;
            /*UINT8*/
            int r;
            if (konami.b != 0) {
                t = (konami.x / konami.b);
                r = (konami.x % konami.b);
            } else {
                /* ?? */
                t = 0;
                r = 0;
            }
            CLR_ZC();
            SET_Z16(t);
            if ((t & 0x80) != 0) {
                SEC();
            }
            konami.x = t & 0xFFFF;
            konami.b = r & 0xFF;
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d divx :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode incd = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode incw_di = new opcode() {
        public void handler() {
            int/*PAIR*/ t, r;
            t = DIRWORD();
            r = t;
            ++r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d incw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode incw_ix = new opcode() {
        public void handler() {
            int/*PAIR*/ t, r;
            t = RM16(ea);
            r = t;
            ++r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d incw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode incw_ex = new opcode() {
        public void handler() {
            int/*PAIR*/ t, r;
            t = EXTWORD();
            r = t;
            ++r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d incw_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode decd = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode decw_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRWORD();
            r = t;
            --r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d decw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode decw_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM16(ea);
            r = t;
            --r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d decw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };

    static opcode decw_ex = new opcode() {
        public void handler() {
            /*PAIR*/
            int t, r;
            t = EXTWORD();
            r = t;
            --r;
            CLR_NZV();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d decw_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lsrw_di = new opcode() {
        public void handler() {
            //PAIR t;
            int t = DIRWORD();
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t >>= 1;
            SET_Z16(t);
            WM16(ea, t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lsrw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lsrw_ix = new opcode() {
        public void handler() {
            int t;//PAIR t;
            t = RM16(ea);
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t >>= 1;
            SET_Z16(t);
            WM16(ea, t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d lsrw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode lsrw_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rorw_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rorw_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rorw_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode asrw_di = new opcode() {
        public void handler() {
            int t = DIRWORD();
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = (t & 0x8000) | (t >> 1);
            SET_NZ16(t);
            WM16(ea, t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d asrw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode asrw_ix = new opcode() {
        public void handler() {
            int t = RM16(ea);
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = (t & 0x8000) | (t >> 1);
            SET_NZ16(t);
            WM16(ea, t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d asrw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode asrw_ex = new opcode() {
        public void handler() {
            int t = EXTWORD();
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = (t & 0x8000) | (t >> 1);
            SET_NZ16(t);
            WM16(ea, t);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d asrw_ex :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode aslw_di = new opcode() {
        public void handler() {
            /*PAIR*/
            int t, r;
            t = DIRWORD();
            r = t << 1;
            CLR_NZVC();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d aslw_di :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode aslw_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM16(ea);
            r = (t << 1);
            CLR_NZVC();
            SET_FLAGS16(t, t, r);
            WM16(ea, r);
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d aslw_ix :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };
    static opcode aslw_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rolw_di = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rolw_ix = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode rolw_ex = new opcode() {
        public void handler() {
            fclose(konamilog);
            throw new UnsupportedOperationException("unsupported opcode");
        }
    };
    static opcode absa = new opcode() {
        public void handler() {
            int r;
            if ((konami.a & 0x80) != 0) {
                r = -konami.a;
            } else {
                r = konami.a;
            }
            CLR_NZVC();
            SET_FLAGS8(0, konami.a, r);
            konami.a = r & 0xFF;
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d absa :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode absb = new opcode() {
        public void handler() {
            /*UINT16*/
            int r;
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d absb_b :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
            if ((konami.b & 0x80) != 0) {
                r = -konami.b;
            } else {
                r = konami.b;
            }
            CLR_NZVC();
            SET_FLAGS8(0, konami.b, r);
            konami.b = r & 0xFF;
            if (konamilog != null) {
                fprintf(konamilog, "konami#%d absb_a :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }
        }
    };
    static opcode absd = new opcode() {
        public void handler() {
            int r;
            if ((getDreg() & 0x8000) != 0) {
                r = -getDreg();
            } else {
                r = getDreg();
            }
            CLR_NZVC();
            SET_FLAGS16(0, getDreg(), r);
            setDreg(r & 0xFFFF);

            if (konamilog != null) {
                fprintf(konamilog, "konami#%d absd :PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
            }

        }
    };

    static opcode opcode2 = new opcode() {
        public void handler() {
            /*UINT8*/
            int ireg2 = ROP_ARG(konami.pc);
            konami.pc = konami.pc + 1 & 0xFFFF;

            switch (ireg2) {
                //	case 0x00: EA=0; break; /* auto increment */
                //	case 0x01: EA=0; break; /* double auto increment */
                //	case 0x02: EA=0; break; /* auto decrement */
                //	case 0x03: EA=0; break; /* double auto decrement */
                //	case 0x04: EA=0; break; /* postbyte offs */
                //	case 0x05: EA=0; break; /* postword offs */
                //	case 0x06: EA=0; break; /* normal */
                case 0x07:
                    ea = 0;
                    konami_extended[konami.ireg].handler();
                    konami_ICount[0] -= 2;
                    return;
                //	case 0x08: EA=0; break; /* indirect - auto increment */
                //	case 0x09: EA=0; break; /* indirect - double auto increment */
                //	case 0x0a: EA=0; break; /* indirect - auto decrement */
                //	case 0x0b: EA=0; break; /* indirect - double auto decrement */
                //	case 0x0c: EA=0; break; /* indirect - postbyte offs */
                //	case 0x0d: EA=0; break; /* indirect - postword offs */
                //	case 0x0e: EA=0; break; /* indirect - normal */
                case 0x0f:
                    /* indirect - extended */

                    ea = IMMWORD();
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0x10: EA=0; break; /* auto increment */
                //	case 0x11: EA=0; break; /* double auto increment */
                //	case 0x12: EA=0; break; /* auto decrement */
                //	case 0x13: EA=0; break; /* double auto decrement */
                //	case 0x14: EA=0; break; /* postbyte offs */
                //	case 0x15: EA=0; break; /* postword offs */
                //	case 0x16: EA=0; break; /* normal */
                //	case 0x17: EA=0; break; /* extended */
                //	case 0x18: EA=0; break; /* indirect - auto increment */
                //	case 0x19: EA=0; break; /* indirect - double auto increment */
                //	case 0x1a: EA=0; break; /* indirect - auto decrement */
                //	case 0x1b: EA=0; break; /* indirect - double auto decrement */
                //	case 0x1c: EA=0; break; /* indirect - postbyte offs */
                //	case 0x1d: EA=0; break; /* indirect - postword offs */
                //	case 0x1e: EA=0; break; /* indirect - normal */
                //	case 0x1f: EA=0; break; /* indirect - extended */

                /* base X */
                case 0x20:
                    /* auto increment */

                    ea = konami.x & 0xFFFF;
                    konami.x = (konami.x + 1) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x21:
                    /* double auto increment */

                    ea = konami.x & 0xFFFF;
                    konami.x = (konami.x + 2) & 0xFFFF;
                    konami_ICount[0] -= 3;
                    break;
                case 0x22:
                    /* auto decrement */

                    konami.x = konami.x - 1 & 0xFFFF;
                    ea = konami.x;
                    konami_ICount[0] -= 2;
                    break;
                case 0x23:
                    /* double auto decrement */

                    konami.x = konami.x - 2 & 0xFFFF;
                    ea = konami.x;
                    konami_ICount[0] -= 3;
                    break;
                case 0x24:
                    /* postbyte offs */

                    ea = IMMBYTE();
                    ea = (konami.x + ((byte) ea & 0xFFFF)) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x25:
                    /* postword offs */

                    ea = IMMWORD();
                    ea = (ea + konami.x) & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0x26:
                    /* normal */

                    ea = konami.x & 0xFFFF;
                    break;
                //	case 0x27: EA=0; break; /* extended */
                case 0x28:
                    /* indirect - auto increment */

                    ea = konami.x;
                    konami.x = konami.x + 1 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x29:
                    /* indirect - double auto increment */

                    ea = konami.x;
                    konami.x = konami.x + 2 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x2a:
                    /* indirect - auto decrement */

                    konami.x = konami.x - 1 & 0xFFFF;
                    ea = konami.x;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x2b:
                    /* indirect - double auto decrement */

                    konami.x = konami.x - 2 & 0xFFFF;
                    ea = konami.x;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x2c:
                    /* indirect - postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.x + (byte) ea & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0x2d:
                    /* indirect - postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.x & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0x2e:
                    /* indirect - normal */

                    ea = konami.x;
                    ea = RM16(ea);
                    konami_ICount[0] -= 3;
                    break;
                //	case 0x2f: EA=0; break; /* indirect - extended */

                /* base Y */
                case 0x30:
                    /* auto increment */

                    ea = konami.y & 0xFFFF;
                    konami.y = (konami.y + 1) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x31:
                    /* double auto increment */

                    ea = konami.y;
                    konami.y = konami.y + 2 & 0xFFFF;
                    konami_ICount[0] -= 3;
                    break;
                case 0x32:
                    /* auto decrement */

                    konami.y = konami.y - 1 & 0xFFFF;
                    ea = konami.y;
                    konami_ICount[0] -= 2;
                    break;
                case 0x33:
                    /* double auto decrement */

                    konami.y = konami.y - 2 & 0xFFFF;
                    ea = konami.y;
                    konami_ICount[0] -= 3;
                    break;
                case 0x34:
                    /* postbyte offs */

                    ea = IMMBYTE();
                    ea = (konami.y + ((byte) ea & 0xFFFF)) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x35:
                    /* postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.y & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0x36:
                    /* normal */

                    ea = konami.y;
                    break;
                //	case 0x37: EA=0; break; /* extended */
                case 0x38:
                    /* indirect - auto increment */

                    ea = konami.y;
                    konami.y = konami.y + 1 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x39:
                    /* indirect - double auto increment */

                    ea = konami.y;
                    konami.y = konami.y + 2 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x3a:
                    /* indirect - auto decrement */

                    konami.y = konami.y - 1 & 0xFFFF;
                    ea = konami.y;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x3b:
                    /* indirect - double auto decrement */

                    konami.y = konami.y - 2 & 0xFFFF;
                    ea = konami.y;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x3c:
                    /* indirect - postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.y + (byte) ea & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0x3d:
                    /* indirect - postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.y & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0x3e:
                    /* indirect - normal */

                    ea = konami.y;
                    ea = RM16(ea);
                    konami_ICount[0] -= 3;
                    break;
                //	case 0x3f: EA=0; break; /* indirect - extended */

                //  case 0x40: EA=0; break; /* auto increment */
                //	case 0x41: EA=0; break; /* double auto increment */
                //	case 0x42: EA=0; break; /* auto decrement */
                //	case 0x43: EA=0; break; /* double auto decrement */
                //	case 0x44: EA=0; break; /* postbyte offs */
                //	case 0x45: EA=0; break; /* postword offs */
                //	case 0x46: EA=0; break; /* normal */
                //	case 0x47: EA=0; break; /* extended */
                //	case 0x48: EA=0; break; /* indirect - auto increment */
                //	case 0x49: EA=0; break; /* indirect - double auto increment */
                //	case 0x4a: EA=0; break; /* indirect - auto decrement */
                //	case 0x4b: EA=0; break; /* indirect - double auto decrement */
                //	case 0x4c: EA=0; break; /* indirect - postbyte offs */
                //	case 0x4d: EA=0; break; /* indirect - postword offs */
                //	case 0x4e: EA=0; break; /* indirect - normal */
                //	case 0x4f: EA=0; break; /* indirect - extended */

                /* base U */
                case 0x50:
                    /* auto increment */

                    ea = konami.u & 0xFFFF;
                    konami.u = (konami.u + 1) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x51:
                    /* double auto increment */

                    ea = konami.u & 0xFFFF;
                    konami.u = (konami.u + 2) & 0xFFFF;
                    konami_ICount[0] -= 3;
                    break;
                case 0x52:
                    /* auto decrement */

                    konami.u = konami.u - 1 & 0xFFFF;
                    ea = konami.u;
                    konami_ICount[0] -= 2;
                    break;
                case 0x53:
                    /* double auto decrement */

                    konami.u = konami.u - 2 & 0xFFFF;
                    ea = konami.u;
                    konami_ICount[0] -= 3;
                    break;
                case 0x54:
                    /* postbyte offs */

                    ea = IMMBYTE();
                    ea = (konami.u + ((byte) ea & 0xFFFF)) & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x55:
                    /* postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.u & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0x56:
                    /* normal */

                    ea = konami.u & 0xFFFF;
                    break;
                //	case 0x57: EA=0; break; /* extended */
                case 0x58:
                    /* indirect - auto increment */

                    ea = konami.u;
                    konami.u = konami.u + 1 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x59:
                    /* indirect - double auto increment */

                    ea = konami.u;
                    konami.u = konami.u + 2 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x5a:
                    /* indirect - auto decrement */

                    konami.u = konami.u - 1 & 0xFFFF;
                    ea = konami.u;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x5b:
                    /* indirect - double auto decrement */

                    konami.u = konami.u - 2 & 0xFFFF;
                    ea = konami.u;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x5c:
                    /* indirect - postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.u + (byte) ea & 0xFFFF;
                    ;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0x5d:
                    /* indirect - postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.u & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0x5e:
                    /* indirect - normal */

                    ea = konami.u;
                    ea = RM16(ea);
                    konami_ICount[0] -= 3;
                    break;
                //	case 0x5f: EA=0; break; /* indirect - extended */

                /* base S */
                case 0x60:
                    /* auto increment */

                    ea = konami.s;
                    konami.s = konami.s + 1 & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x61:
                    /* double auto increment */

                    ea = konami.s;
                    konami.s = konami.s + 2 & 0xFFFF;
                    konami_ICount[0] -= 3;
                    break;
                case 0x62:
                    /* auto decrement */

                    konami.s = konami.s - 1 & 0xFFFF;
                    ea = konami.s;
                    konami_ICount[0] -= 2;
                    break;
                case 0x63:
                    /* double auto decrement */

                    konami.s = konami.s - 2 & 0xFFFF;
                    ea = konami.s;
                    konami_ICount[0] -= 3;
                    break;
                case 0x64:
                    /* postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.s + (byte) ea & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x65:
                    /* postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.s & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0x66:
                    /* normal */

                    ea = konami.s;
                    break;
                //	case 0x67: EA=0; break; /* extended */
                case 0x68:
                    /* indirect - auto increment */

                    ea = konami.s;
                    konami.s = konami.s + 1 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x69:
                    /* indirect - double auto increment */

                    ea = konami.s;
                    konami.s = konami.s + 2 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x6a:
                    /* indirect - auto decrement */

                    konami.s = konami.s - 1 & 0xFFFF;
                    ea = konami.s;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x6b:
                    /* indirect - double auto decrement */

                    konami.s = konami.s - 2 & 0xFFFF;
                    ea = konami.s;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x6c:
                    /* indirect - postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.s + (byte) ea & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0x6d:
                    /* indirect - postword offs */

                    ea = IMMWORD();
                    ea = ea + konami.s & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0x6e:
                    /* indirect - normal */

                    ea = konami.s;
                    ea = RM16(ea);
                    konami_ICount[0] -= 3;
                    break;
                //	case 0x6f: EA=0; break; /* indirect - extended */

                /* base PC */
                case 0x70:
                    /* auto increment */

                    ea = konami.pc;
                    konami.pc = konami.pc + 1 & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x71:
                    /* double auto increment */

                    ea = konami.pc;
                    konami.pc = konami.pc + 2 & 0xFFFF;
                    konami_ICount[0] -= 3;
                    break;
                case 0x72:
                    /* auto decrement */

                    konami.pc = konami.pc - 1 & 0xFFFF;
                    ea = konami.pc;
                    konami_ICount[0] -= 2;
                    break;
                case 0x73:
                    /* double auto decrement */

                    konami.pc = konami.pc - 2 & 0xFFFF;
                    ea = konami.pc;
                    konami_ICount[0] -= 3;
                    break;
                case 0x74:
                    /* postbyte offs */

                    ea = IMMBYTE();
                    ea = konami.pc - 1 + (byte) ea & 0xFFFF;
                    konami_ICount[0] -= 2;
                    break;
                case 0x75:
                    /* postword offs */

                    ea = IMMWORD();
                    ea = ea + (konami.pc - 2) & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0x76:
                    /* normal */

                    ea = konami.pc;
                    break;
                //	case 0x77: EA=0; break; /* extended */
                case 0x78:
                    /* indirect - auto increment */

                    ea = konami.pc;
                    konami.pc = konami.pc + 1 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x79:
                    /* indirect - double auto increment */

                    ea = konami.pc;
                    konami.pc = konami.pc + 2 & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x7a:
                    /* indirect - auto decrement */

                    konami.pc = konami.pc - 1 & 0xFFFF;
                    ea = konami.pc;
                    ea = RM16(ea);
                    konami_ICount[0] -= 5;
                    break;
                case 0x7b:
                    /* indirect - double auto decrement */

                    konami.pc = konami.pc - 2 & 0xFFFF;
                    ea = konami.pc;
                    ea = RM16(ea);
                    konami_ICount[0] -= 6;
                    break;
                case 0x7c:
                    /* indirect - postbyte offs */

                    ea = IMMBYTE();
                    ea = (konami.pc - 1) + (byte) ea & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0x7d:
                    /* indirect - postword offs */

                    ea = IMMWORD();
                    ea = ea + (konami.pc - 2) & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0x7e:
                    /* indirect - normal */

                    ea = konami.pc;
                    ea = RM16(ea);
                    konami_ICount[0] -= 3;
                    break;
                //	case 0x7f: EA=0; break; /* indirect - extended */

                //  case 0x80: EA=0; break; /* register a */
                //	case 0x81: EA=0; break; /* register b */
                //	case 0x82: EA=0; break; /* ???? */
                //	case 0x83: EA=0; break; /* ???? */
                //	case 0x84: EA=0; break; /* ???? */
                //	case 0x85: EA=0; break; /* ???? */
                //	case 0x86: EA=0; break; /* ???? */
                //	case 0x87: EA=0; break; /* register d */
                //	case 0x88: EA=0; break; /* indirect - register a */
                //	case 0x89: EA=0; break; /* indirect - register b */
                //	case 0x8a: EA=0; break; /* indirect - ???? */
                //	case 0x8b: EA=0; break; /* indirect - ???? */
                //	case 0x8c: EA=0; break; /* indirect - ???? */
                //	case 0x8d: EA=0; break; /* indirect - ???? */
                //	case 0x8e: EA=0; break; /* indirect - register d */
                //	case 0x8f: EA=0; break; /* indirect - ???? */
                //	case 0x90: EA=0; break; /* register a */
                //	case 0x91: EA=0; break; /* register b */
                //	case 0x92: EA=0; break; /* ???? */
                //	case 0x93: EA=0; break; /* ???? */
                //	case 0x94: EA=0; break; /* ???? */
                //	case 0x95: EA=0; break; /* ???? */
                //	case 0x96: EA=0; break; /* ???? */
                //	case 0x97: EA=0; break; /* register d */
                //	case 0x98: EA=0; break; /* indirect - register a */
                //	case 0x99: EA=0; break; /* indirect - register b */
                //	case 0x9a: EA=0; break; /* indirect - ???? */
                //	case 0x9b: EA=0; break; /* indirect - ???? */
                //	case 0x9c: EA=0; break; /* indirect - ???? */
                //	case 0x9d: EA=0; break; /* indirect - ???? */
                //	case 0x9e: EA=0; break; /* indirect - register d */
                //	case 0x9f: EA=0; break; /* indirect - ???? */
                case 0xa0:
                    /* register a */

                    ea = konami.x + (byte) konami.a & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                case 0xa1:
                    /* register b */

                    ea = konami.x + (byte) konami.b & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                //	case 0xa2: EA=0; break; /* ???? */
                //	case 0xa3: EA=0; break; /* ???? */
                //	case 0xa4: EA=0; break; /* ???? */
                //	case 0xa5: EA=0; break; /* ???? */
                //	case 0xa6: EA=0; break; /* ???? */
                case 0xa7:
                    /* register d */

                    ea = konami.x + getDreg() & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0xa8:
                    /* indirect - register a */

                    ea = konami.x + (byte) konami.a & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0xa9:
                    /* indirect - register b */

                    ea = konami.x + (byte) konami.b & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xaa: EA=0; break; /* indirect - ???? */
                //	case 0xab: EA=0; break; /* indirect - ???? */
                //	case 0xac: EA=0; break; /* indirect - ???? */
                //	case 0xad: EA=0; break; /* indirect - ???? */
                //	case 0xae: EA=0; break; /* indirect - ???? */
                case 0xaf:
                    /* indirect - register d */

                    ea = konami.x + getDreg() & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0xb0:
                    /* register a */

                    ea = konami.y + (byte) konami.a & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                case 0xb1:
                    /* register b */

                    ea = konami.y + (byte) konami.b & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                //	case 0xb2: EA=0; break; /* ???? */
                //	case 0xb3: EA=0; break; /* ???? */
                //	case 0xb4: EA=0; break; /* ???? */
                //	case 0xb5: EA=0; break; /* ???? */
                //	case 0xb6: EA=0; break; /* ???? */
                case 0xb7:
                    /* register d */

                    ea = konami.y + getDreg() & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0xb8:
                    /* indirect - register a */

                    ea = konami.y + (byte) konami.a & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0xb9:
                    /* indirect - register b */

                    ea = konami.y + (byte) konami.b & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xba: EA=0; break; /* indirect - ???? */
                //	case 0xbb: EA=0; break; /* indirect - ???? */
                //	case 0xbc: EA=0; break; /* indirect - ???? */
                //	case 0xbd: EA=0; break; /* indirect - ???? */
                //	case 0xbe: EA=0; break; /* indirect - ???? */
                case 0xbf:
                    /* indirect - register d */

                    ea = konami.y + getDreg() & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                //	case 0xc0: EA=0; break; /* register a */
                //	case 0xc1: EA=0; break; /* register b */
                //	case 0xc2: EA=0; break; /* ???? */
                //	case 0xc3: EA=0; break; /* ???? */
                case 0xc4:
                    ea = 0;
                    konami_direct[konami.ireg].handler();
                    konami_ICount[0] -= 1;
                    return;
                //	case 0xc5: EA=0; break; /* ???? */
                //	case 0xc6: EA=0; break; /* ???? */
                //	case 0xc7: EA=0; break; /* register d */
                //	case 0xc8: EA=0; break; /* indirect - register a */
                //	case 0xc9: EA=0; break; /* indirect - register b */
                //	case 0xca: EA=0; break; /* indirect - ???? */
                //	case 0xcb: EA=0; break; /* indirect - ???? */
                case 0xcc:
                    /* indirect - direct */

                    ea = DIRWORD();
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xcd: EA=0; break; /* indirect - ???? */
                //	case 0xce: EA=0; break; /* indirect - register d */
                //	case 0xcf: EA=0; break; /* indirect - ???? */
                case 0xd0:
                    /* register a */

                    ea = konami.u + (byte) konami.a & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                case 0xd1:
                    /* register b */

                    ea = konami.u + (byte) konami.b & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                //	case 0xd2: EA=0; break; /* ???? */
                //	case 0xd3: EA=0; break; /* ???? */
                //	case 0xd4: EA=0; break; /* ???? */
                //	case 0xd5: EA=0; break; /* ???? */
                //	case 0xd6: EA=0; break; /* ???? */
                case 0xd7:
                    /* register d */

                    ea = konami.u + getDreg() & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0xd8:
                    /* indirect - register a */

                    ea = konami.u + (byte) konami.a & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0xd9:
                    /* indirect - register b */

                    ea = konami.u + (byte) konami.b & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xda: EA=0; break; /* indirect - ???? */
                //	case 0xdb: EA=0; break; /* indirect - ???? */
                //	case 0xdc: EA=0; break; /* indirect - ???? */
                //	case 0xdd: EA=0; break; /* indirect - ???? */
                //	case 0xde: EA=0; break; /* indirect - ???? */
                case 0xdf:
                    /* indirect - register d */

                    ea = konami.u + getDreg() & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0xe0:
                    /* register a */

                    ea = konami.s + (byte) konami.a & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                case 0xe1:
                    /* register b */

                    ea = konami.s + (byte) konami.b & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                //	case 0xe2: EA=0; break; /* ???? */
                //	case 0xe3: EA=0; break; /* ???? */
                //	case 0xe4: EA=0; break; /* ???? */
                //	case 0xe5: EA=0; break; /* ???? */
                //	case 0xe6: EA=0; break; /* ???? */
                case 0xe7:
                    /* register d */

                    ea = konami.s + getDreg() & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0xe8:
                    /* indirect - register a */

                    ea = konami.s + (byte) konami.a & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0xe9:
                    /* indirect - register b */

                    ea = konami.s + (byte) konami.b & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xea: EA=0; break; /* indirect - ???? */
                //	case 0xeb: EA=0; break; /* indirect - ???? */
                //	case 0xec: EA=0; break; /* indirect - ???? */
                //	case 0xed: EA=0; break; /* indirect - ???? */
                //	case 0xee: EA=0; break; /* indirect - ???? */
                case 0xef:
                    /* indirect - register d */

                    ea = konami.s + getDreg() & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                case 0xf0:
                    /* register a */

                    ea = konami.pc + (byte) konami.a & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                case 0xf1:
                    /* register b */

                    ea = konami.pc + (byte) konami.b & 0xFFFF;
                    konami_ICount[0] -= 1;
                    break;
                //	case 0xf2: EA=0; break; /* ???? */
                //	case 0xf3: EA=0; break; /* ???? */
                //	case 0xf4: EA=0; break; /* ???? */
                //	case 0xf5: EA=0; break; /* ???? */
                //	case 0xf6: EA=0; break; /* ???? */
                case 0xf7:
                    /* register d */

                    ea = konami.pc + getDreg() & 0xFFFF;
                    konami_ICount[0] -= 4;
                    break;
                case 0xf8:
                    /* indirect - register a */

                    ea = konami.pc + (byte) konami.a & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                case 0xf9:
                    /* indirect - register b */

                    ea = konami.pc + (byte) konami.b & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 4;
                    break;
                //	case 0xfa: EA=0; break; /* indirect - ???? */
                //	case 0xfb: EA=0; break; /* indirect - ???? */
                //	case 0xfc: EA=0; break; /* indirect - ???? */
                //	case 0xfd: EA=0; break; /* indirect - ???? */
                //	case 0xfe: EA=0; break; /* indirect - ???? */
                case 0xff:
                    /* indirect - register d */

                    ea = konami.pc + getDreg() & 0xFFFF;
                    ea = RM16(ea);
                    konami_ICount[0] -= 7;
                    break;
                default:
                    if (konamilog != null) {
                        fprintf(konamilog, "KONAMI: Unknown/Invalid postbyte at PC = %04x\n", konami.pc - 1);
                    }
                    ea = 0;
            }
            if (konamilog != null) {
                switch (ireg2) {
                    case 0x20:
                    case 0x21:
                    case 0x24:
                    case 0x25:
                    case 0x26:
                    case 0x30:
                    case 0x34:
                    case 0x50:
                    case 0x51:
                    case 0x54:
                    case 0x56:
                        break;
                    default:
                        fprintf(konamilog, "konami#%d opcode2 : ireg2:%d,PC:%d,PPC:%d,A:%d,B:%d,D:%d,DP:%d,U:%d,S:%d,X:%d,Y:%d,CC:%d,EA:%d\n", cpu_getactivecpu(), ireg2, konami.pc, konami.ppc, konami.a, konami.b, getDreg(), konami.dp, konami.u, konami.s, konami.x, konami.y, konami.cc, ea);
                }

            }
            konami_indexed[konami.ireg].handler();
        }
    };

    @Override
    public void exit() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }
}
