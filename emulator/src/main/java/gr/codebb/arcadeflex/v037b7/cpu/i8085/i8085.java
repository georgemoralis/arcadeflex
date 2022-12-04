package gr.codebb.arcadeflex.v037b7.cpu.i8085;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085H.*;
import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085cpuH.*;
import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085daaH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;


public class i8085 extends cpu_interface {

    public static int[] i8085_ICount = new int[1];

    public i8085() {
        cpu_num = CPU_8085A;
        num_irqs = 4;
        default_vector = 255;
        overclock = 1.0;
        no_int = I8085_NONE;
        irq_int = I8085_INTR;
        nmi_int = I8085_TRAP;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = i8085_ICount;

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

    public static abstract interface sod_callbackPtr {

        public abstract void handler(int state);
    }

    public static class i8085_Regs {

        int cputype;
        /* 0 8080, 1 8085A */
        public PAIR PC = new PAIR();
        public PAIR SP = new PAIR();
        public PAIR AF = new PAIR();
        public PAIR BC = new PAIR();
        public PAIR DE = new PAIR();
        public PAIR HL = new PAIR();
        public PAIR XX = new PAIR();
        char/*UINT8*/ u8_HALT;
        char/*UINT8*/ u8_IM;
        /* interrupt mask */
        char/*UINT8*/ u8_IREQ;
        /* requested interrupts */
        char/*UINT8*/ u8_ISRV;
        /* serviced interrupt */
        int/*UINT32*/ INTR;
        /* vector for INTR */
        int/*UINT32*/ IRQ2;
        /* scheduled interrupt address */
        int/*UINT32*/ IRQ1;
        /* executed interrupt address */
        int/*INT8*/ nmi_state;
        int[]/*INT8*/ irq_state = new int[4];
        /*TODO*///	INT8	filler; /* align on dword boundary */
        irqcallbacksPtr irq_callback;
        sod_callbackPtr sod_callback;
    }

    public static i8085_Regs I = new i8085_Regs();

    static int[]/*UINT8*/ ZS = new int[256];
    static int[]/*UINT8*/ ZSP = new int[256];

    static char ROP() {
        //return cpu_readop(I.PC.w.l++);
        int tmp = I.PC.D;
        I.PC.AddD(1);
        return cpu_readop(tmp);
    }

    static char ARG() {
        //return cpu_readop_arg(I.PC.w.l++);
        int tmp = I.PC.D;
        I.PC.AddD(1);
        return cpu_readop_arg(tmp);
    }

    static char ARG16() {
        char w;
        w = cpu_readop_arg(I.PC.D);
        I.PC.AddD(1);
        w += cpu_readop_arg(I.PC.D) << 8;
        I.PC.AddD(1);
        return w;
    }

    static char RM(char a) {
        return (char) ((cpu_readmem16(a)) & 0xFF);
    }

    static void WM(int a, int v) {
        cpu_writemem16(a & 0xFFFF, v & 0xFF);
    }

    static void illegal() {
        /*#if VERBOSE
	UINT16 pc = I.PC.w.l - 1;
	LOG(("i8085 illegal instruction %04X $%02X\n", pc, cpu_readop(pc)));
#endif*/
    }

    public static void execute_one(int opcode) {
        switch (opcode) {
            case 0x00:
                i8085_ICount[0] -= 4;
                /* NOP	*/
 /* no op */
                break;
            case 0x01:
                i8085_ICount[0] -= 10;
                /* LXI	B,nnnn */
                I.BC.SetD(ARG16());
                break;
            case 0x02:
                i8085_ICount[0] -= 7;
                /* STAX B */
                WM(I.BC.D, I.AF.H);
                break;
            case 0x03:
                i8085_ICount[0] -= 5;
                /* INX	B */
                I.BC.AddD(1);
                break;
            case 0x04:
                i8085_ICount[0] -= 5;
                /* INR	B */
                //M_INR(I.BC.H);
                I.BC.AddH(1);
                I.AF.SetL((I.AF.L & CF) | ZS[I.BC.H] | ((I.BC.H == 0x80) ? VF : 0) | ((I.BC.H & 0x0F) != 0 ? 0 : HF));
                break;
            case 0x05:
                i8085_ICount[0] -= 5;
                /* DCR	B */
                //M_DCR(I.BC.H);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.BC.H == 0x80) ? VF : 0) | ((I.BC.H & 0x0F) != 0 ? 0 : HF));
                I.BC.AddH(-1);
                I.AF.SetL(I.AF.L | ZS[I.BC.H]);
                break;
            case 0x06:
                i8085_ICount[0] -= 7;
                /* MVI	B,nn */
                //M_MVI(I.BC.H);
                I.BC.SetH(ARG());
                break;
            case 0x07:
                i8085_ICount[0] -= 4;
                /* RLC	*/
                M_RLC();
                break;

            case 0x08:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0x09:
                i8085_ICount[0] -= 10;
                /* DAD	B */
                M_DAD(I.BC.D);
                break;
            case 0x0a:
                i8085_ICount[0] -= 7;
                /* LDAX B */
                I.AF.SetH(RM((char) I.BC.D));
                break;
            case 0x0b:
                i8085_ICount[0] -= 5;
                /* DCX	B */
                I.BC.AddD(-1);
                break;
            case 0x0c:
                i8085_ICount[0] -= 5;
                /* INR	C */
                //M_INR(I.BC.L);
                I.BC.AddL(1);
                I.AF.SetL((I.AF.L & CF) | ZS[I.BC.L] | ((I.BC.L == 0x80) ? VF : 0) | ((I.BC.L & 0x0F) != 0 ? 0 : HF));
                break;
            case 0x0d:
                i8085_ICount[0] -= 5;
                /* DCR	C */
                //M_DCR(I.BC.L);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.BC.L == 0x80) ? VF : 0) | ((I.BC.L & 0x0F) != 0 ? 0 : HF));
                I.BC.AddL(-1);
                I.AF.SetL(I.AF.L | ZS[I.BC.L]);
                break;
            case 0x0e:
                i8085_ICount[0] -= 7;
                /* MVI	C,nn */
                //M_MVI(I.BC.L);
                I.BC.SetL(ARG());
                break;
            case 0x0f:
                i8085_ICount[0] -= 4;
                /* RRC	*/
                M_RRC();
                break;

            case 0x10:
                i8085_ICount[0] -= 8;
                /* ????  */
                illegal();
                break;
            case 0x11:
                i8085_ICount[0] -= 10;
                /* LXI	D,nnnn */
                I.DE.SetD(ARG16());
                break;
            case 0x12:
                i8085_ICount[0] -= 7;
                /* STAX D */
                WM(I.DE.D, I.AF.H);
                break;
            case 0x13:
                i8085_ICount[0] -= 5;
                /* INX	D */
                I.DE.AddD(1);
                break;
            case 0x14:
                i8085_ICount[0] -= 5;
                /* INR	D */
                //M_INR(I.DE.H);
                I.DE.AddH(1);
                I.AF.SetL((I.AF.L & CF) | ZS[I.DE.H] | ((I.DE.H == 0x80) ? VF : 0) | ((I.DE.H & 0x0F) != 0 ? 0 : HF));
                break;
            case 0x15:
                i8085_ICount[0] -= 5;
                /* DCR	D */
                //M_DCR(I.DE.H);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.DE.H == 0x80) ? VF : 0) | ((I.DE.H & 0x0F) != 0 ? 0 : HF));
                I.DE.AddH(-1);
                I.AF.SetL(I.AF.L | ZS[I.DE.H]);
                break;
            case 0x16:
                i8085_ICount[0] -= 7;
                /* MVI	D,nn */
                //M_MVI(I.DE.b.h);
                I.DE.SetH(ARG());
                break;
            case 0x17:
                i8085_ICount[0] -= 4;
                /* RAL	*/
                M_RAL();
                break;

            case 0x18:
                i8085_ICount[0] -= 7;
                /* ????? */
                illegal();
                break;
            case 0x19:
                i8085_ICount[0] -= 10;
                /* DAD	D */
                M_DAD(I.DE.D);
                break;
            case 0x1a:
                i8085_ICount[0] -= 7;
                /* LDAX D */
                I.AF.SetH(RM((char) I.DE.D));
                break;
            case 0x1b:
                i8085_ICount[0] -= 5;
                /* DCX	D */
                I.DE.AddD(-1);
                break;
            case 0x1c:
                i8085_ICount[0] -= 5;
                /* INR	E */
                //M_INR(I.DE.L);
                I.DE.AddL(1);
                I.AF.SetL((I.AF.L & CF) | ZS[I.DE.L] | ((I.DE.L == 0x80) ? VF : 0) | ((I.DE.L & 0x0F) != 0 ? 0 : HF));
                break;
            case 0x1d:
                i8085_ICount[0] -= 5;
                /* DCR	E */
                //M_DCR(I.DE.L);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.DE.L == 0x80) ? VF : 0) | ((I.DE.L & 0x0F) != 0 ? 0 : HF));
                I.DE.AddL(-1);
                I.AF.SetL(I.AF.L | ZS[I.DE.L]);
                break;
            case 0x1e:
                i8085_ICount[0] -= 7;
                /* MVI	E,nn */
                //M_MVI(I.DE.b.l);
                I.DE.SetL(ARG());
                break;
            case 0x1f:
                i8085_ICount[0] -= 4;
                /* RAR	*/
                M_RAR();
                break;

            case 0x20:
                if (I.cputype != 0) {
                    i8085_ICount[0] -= 7;
                    /* RIM	*/
                    I.AF.SetH(I.u8_IM);
                } else {
                    i8085_ICount[0] -= 7;
                    /* ???	*/
                }
                break;
            case 0x21:
                i8085_ICount[0] -= 10;
                /* LXI	H,nnnn */
                I.HL.SetD(ARG16());
                break;
            case 0x22:
                i8085_ICount[0] -= 16;
                /* SHLD nnnn */
                I.XX.SetD(ARG16());
                WM(I.XX.D, I.HL.L);
                I.XX.AddD(1);
                WM(I.XX.D, I.HL.H);
                break;
            case 0x23:
                i8085_ICount[0] -= 5;
                /* INX	H */
                I.HL.AddD(1);
                break;
            case 0x24:
                i8085_ICount[0] -= 5;
                /* INR	H */
                //M_INR(I.HL.H);
                I.HL.AddH(1);
                I.AF.SetL((I.AF.L & CF) | ZS[I.HL.H] | ((I.HL.H == 0x80) ? VF : 0) | ((I.HL.H & 0x0F) != 0 ? 0 : HF));
                break;
            case 0x25:
                i8085_ICount[0] -= 5;
                /* DCR	H */
                //M_DCR(I.HL.H);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.HL.H == 0x80) ? VF : 0) | ((I.HL.H & 0x0F) != 0 ? 0 : HF));
                I.HL.AddH(-1);
                I.AF.SetL(I.AF.L | ZS[I.HL.H]);
                break;
            case 0x26:
                i8085_ICount[0] -= 7;
                /* MVI	H,nn */
                //M_MVI(I.HL.b.h);
                I.HL.SetH(ARG());
                break;
            case 0x27:
                i8085_ICount[0] -= 4;
                /* DAA	*/
                I.XX.SetD(I.AF.H);
                if ((I.AF.L & CF) != 0) {
                    I.XX.SetD(I.XX.D | 0x100);
                }
                if ((I.AF.L & HF) != 0) {
                    I.XX.SetD(I.XX.D | 0x200);
                }
                if ((I.AF.L & NF) != 0) {
                    I.XX.SetD(I.XX.D | 0x400);
                }
                I.AF.SetD(DAA[I.XX.D]);
                break;

            case 0x28:
                i8085_ICount[0] -= 7;
                /* ???? */
                illegal();
                break;
            case 0x29:
                i8085_ICount[0] -= 10;
                /* DAD	H */
                M_DAD(I.HL.D);
                break;
            case 0x2a:
                i8085_ICount[0] -= 16;
                /* LHLD nnnn */
                I.XX.SetD(ARG16());
                I.HL.SetL(RM((char) I.XX.D));
                I.XX.AddD(1);
                I.HL.SetH(RM((char) I.XX.D));
                break;
            case 0x2b:
                i8085_ICount[0] -= 5;
                /* DCX	H */
                I.HL.AddD(-1);
                break;
            case 0x2c:
                i8085_ICount[0] -= 5;
                /* INR	L */

                 {//M_INR(I.HL.b.l);
                    I.HL.AddL(1);
                    I.AF.SetL((I.AF.L & CF) | ZS[I.HL.L] | ((I.HL.L == 0x80) ? VF : 0) | ((I.HL.L & 0x0F) != 0 ? 0 : HF));
                }
                break;
            case 0x2d:
                i8085_ICount[0] -= 5;
                /* DCR	L */
                //M_DCR(I.HL.L);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.HL.L == 0x80) ? VF : 0) | ((I.HL.L & 0x0F) != 0 ? 0 : HF));
                I.HL.AddL(-1);
                I.AF.SetL(I.AF.L | ZS[I.HL.L]);
                break;
            case 0x2e:
                i8085_ICount[0] -= 7;
                /* MVI	L,nn */
                //M_MVI(I.HL.b.l);
                I.HL.SetL(ARG());
                break;
            case 0x2f:
                i8085_ICount[0] -= 4;
                /* CMA	*/
                I.AF.SetH(I.AF.H ^ 0xff);
                I.AF.SetL(I.AF.L | HF + NF);
                break;

            case 0x30:
                if (I.cputype != 0) {
                    i8085_ICount[0] -= 7;
                    /* SIM	*/
                    if (((I.u8_IM ^ I.AF.H) & 0x80) != 0) {
                        if (I.sod_callback != null) {
                            (I.sod_callback).handler(I.AF.H >>> 7);
                        }
                    }
                    I.u8_IM = (char) ((I.u8_IM & (IM_SID + IM_IEN + IM_TRAP)) & 0xFF);
                    I.u8_IM = (char) ((I.u8_IM | (I.AF.H & ~(IM_SID + IM_SOD + IM_IEN + IM_TRAP))) & 0xFF);
                    if ((I.AF.H & 0x80) != 0) {
                        I.u8_IM = (char) ((I.u8_IM | IM_SOD) & 0xFF);
                    }
                } else {
                    i8085_ICount[0] -= 4;
                    /* ???	*/
                }
                break;
            case 0x31:
                i8085_ICount[0] -= 10;
                /* LXI SP,nnnn */
                I.SP.SetD(ARG16());
                break;
            case 0x32:
                i8085_ICount[0] -= 13;
                /* STAX nnnn */
                I.XX.SetD(ARG16());
                WM(I.XX.D, I.AF.H);
                break;
            case 0x33:
                i8085_ICount[0] -= 5;
                /* INX	SP */
                I.SP.AddD(1);
                break;
            case 0x34:
                i8085_ICount[0] -= 10;
                /* INR	M */
                I.XX.SetL(RM((char) I.HL.D));
                 {//M_INR(I.XX.b.l);
                    I.XX.AddL(1);
                    I.AF.SetL((I.AF.L & CF) | ZS[I.XX.L] | ((I.XX.L == 0x80) ? VF : 0) | ((I.XX.L & 0x0F) != 0 ? 0 : HF));
                }
                WM(I.HL.D, I.XX.L);
                break;
            case 0x35:
                i8085_ICount[0] -= 10;
                /* DCR	M */
                I.XX.SetL(RM((char) I.HL.D));
                //M_DCR(I.XX.L);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.XX.L == 0x80) ? VF : 0) | ((I.XX.L & 0x0F) != 0 ? 0 : HF));
                I.XX.AddL(-1);
                I.AF.SetL(I.AF.L | ZS[I.XX.L]);
                WM(I.HL.D, I.XX.L);
                break;
            case 0x36:
                i8085_ICount[0] -= 10;
                /* MVI	M,nn */
                I.XX.SetL(ARG());
                WM(I.HL.D, I.XX.L);
                break;
            case 0x37:
                i8085_ICount[0] -= 4;
                /* STC	*/
                I.AF.SetL((I.AF.L & ~(HF + NF)) | CF);
                break;

            case 0x38:
                i8085_ICount[0] -= 7;
                /* ???? */
                illegal();
                break;
            case 0x39:
                i8085_ICount[0] -= 10;
                /* DAD SP */
                M_DAD(I.SP.D);
                break;
            case 0x3a:
                i8085_ICount[0] -= 13;
                /* LDAX nnnn */
                I.XX.SetD(ARG16());
                I.AF.SetH(RM((char) I.XX.D));
                break;
            case 0x3b:
                i8085_ICount[0] -= 5;
                /* DCX	SP */
                I.SP.AddD(-1);
                break;
            case 0x3c:
                i8085_ICount[0] -= 5;
                /* INR	A */
                 {//M_INR(I..AF.H);
                    I.AF.AddH(1);
                    I.AF.SetL((I.AF.L & CF) | ZS[I.AF.H] | ((I.AF.H == 0x80) ? VF : 0) | ((I.AF.H & 0x0F) != 0 ? 0 : HF));
                }
                break;
            case 0x3d:
                i8085_ICount[0] -= 5;
                /* DCR	A */
                //M_DCR(I.AF.H);
                I.AF.SetL((I.AF.L & CF) | NF | ((I.AF.H == 0x80) ? VF : 0) | ((I.AF.H & 0x0F) != 0 ? 0 : HF));
                I.AF.AddH(-1);
                I.AF.SetL(I.AF.L | ZS[I.AF.H]);
                break;
            case 0x3e:
                i8085_ICount[0] -= 7;
                /* MVI	A,nn */
                //M_MVI(I.AF.H);
                I.AF.SetH(ARG());
                break;
            case 0x3f:
                i8085_ICount[0] -= 4;
                /* CMF	*/
                I.AF.SetL(((I.AF.L & ~(HF + NF))
                        | ((I.AF.L & CF) << 4)) ^ CF);
                break;

            case 0x40:
                i8085_ICount[0] -= 5;
                /* MOV	B,B */
 /* no op */
                break;
            case 0x41:
                i8085_ICount[0] -= 5;
                /* MOV	B,C */
                I.BC.SetH(I.BC.L);
                break;
            case 0x42:
                i8085_ICount[0] -= 5;
                /* MOV	B,D */
                I.BC.SetH(I.DE.H);
                break;
            case 0x43:
                i8085_ICount[0] -= 5;
                /* MOV	B,E */
                I.BC.SetH(I.DE.L);
                break;
            case 0x44:
                i8085_ICount[0] -= 5;
                /* MOV	B,H */
                I.BC.SetH(I.HL.H);
                break;
            case 0x45:
                i8085_ICount[0] -= 5;
                /* MOV	B,L */
                I.BC.SetH(I.HL.L);
                break;
            case 0x46:
                i8085_ICount[0] -= 7;
                /* MOV	B,M */
                I.BC.SetH(RM((char) I.HL.D));
                break;
            case 0x47:
                i8085_ICount[0] -= 5;
                /* MOV	B,A */
                I.BC.SetH(I.AF.H);
                break;

            case 0x48:
                i8085_ICount[0] -= 5;
                /* MOV	C,B */
                I.BC.SetL(I.BC.H);
                break;
            case 0x49:
                i8085_ICount[0] -= 5;
                /* MOV	C,C */
 /* no op */
                break;
            case 0x4a:
                i8085_ICount[0] -= 5;
                /* MOV	C,D */
                I.BC.SetL(I.DE.H);
                break;
            case 0x4b:
                i8085_ICount[0] -= 5;
                /* MOV	C,E */
                I.BC.SetL(I.DE.L);
                break;
            case 0x4c:
                i8085_ICount[0] -= 5;
                /* MOV	C,H */
                I.BC.SetL(I.HL.H);
                break;
            case 0x4d:
                i8085_ICount[0] -= 5;
                /* MOV	C,L */
                I.BC.SetL(I.HL.L);
                break;
            case 0x4e:
                i8085_ICount[0] -= 7;
                /* MOV	C,M */
                I.BC.SetL(RM((char) I.HL.D));
                break;
            case 0x4f:
                i8085_ICount[0] -= 5;
                /* MOV	C,A */
                I.BC.SetL(I.AF.H);
                break;

            case 0x50:
                i8085_ICount[0] -= 5;
                /* MOV	D,B */
                I.DE.SetH(I.BC.H);
                break;
            case 0x51:
                i8085_ICount[0] -= 5;
                /* MOV	D,C */
                I.DE.SetH(I.BC.L);
                break;
            case 0x52:
                i8085_ICount[0] -= 5;
                /* MOV	D,D */
 /* no op */
                break;
            case 0x53:
                i8085_ICount[0] -= 5;
                /* MOV	D,E */
                I.DE.SetH(I.DE.L);
                break;
            case 0x54:
                i8085_ICount[0] -= 5;
                /* MOV	D,H */
                I.DE.SetH(I.HL.H);
                break;
            case 0x55:
                i8085_ICount[0] -= 5;
                /* MOV	D,L */
                I.DE.SetH(I.HL.L);
                break;
            case 0x56:
                i8085_ICount[0] -= 7;
                /* MOV	D,M */
                I.DE.SetH(RM((char) I.HL.D));
                break;
            case 0x57:
                i8085_ICount[0] -= 5;
                /* MOV	D,A */
                I.DE.SetH(I.AF.H);
                break;

            case 0x58:
                i8085_ICount[0] -= 5;
                /* MOV	E,B */
                I.DE.SetL(I.BC.H);
                break;
            case 0x59:
                i8085_ICount[0] -= 5;
                /* MOV	E,C */
                I.DE.SetL(I.BC.L);
                break;
            case 0x5a:
                i8085_ICount[0] -= 5;
                /* MOV	E,D */
                I.DE.SetL(I.DE.H);
                break;
            case 0x5b:
                i8085_ICount[0] -= 5;
                /* MOV	E,E */
 /* no op */
                break;
            case 0x5c:
                i8085_ICount[0] -= 5;
                /* MOV	E,H */
                I.DE.SetL(I.HL.H);
                break;
            case 0x5d:
                i8085_ICount[0] -= 5;
                /* MOV	E,L */
                I.DE.SetL(I.HL.L);
                break;
            case 0x5e:
                i8085_ICount[0] -= 7;
                /* MOV	E,M */
                I.DE.SetL(RM((char) I.HL.D));
                break;
            case 0x5f:
                i8085_ICount[0] -= 5;
                /* MOV	E,A */
                I.DE.SetL(I.AF.H);
                break;

            case 0x60:
                i8085_ICount[0] -= 5;
                /* MOV	H,B */
                I.HL.SetH(I.BC.H);
                break;
            case 0x61:
                i8085_ICount[0] -= 5;
                /* MOV	H,C */
                I.HL.SetH(I.BC.L);
                break;
            case 0x62:
                i8085_ICount[0] -= 5;
                /* MOV	H,D */
                I.HL.SetH(I.DE.H);
                break;
            case 0x63:
                i8085_ICount[0] -= 5;
                /* MOV	H,E */
                I.HL.SetH(I.DE.L);
                break;
            case 0x64:
                i8085_ICount[0] -= 5;
                /* MOV	H,H */
 /* no op */
                break;
            case 0x65:
                i8085_ICount[0] -= 5;
                /* MOV	H,L */
                I.HL.SetH(I.HL.L);
                break;
            case 0x66:
                i8085_ICount[0] -= 7;
                /* MOV	H,M */
                I.HL.SetH(RM((char) I.HL.D));
                break;
            case 0x67:
                i8085_ICount[0] -= 5;
                /* MOV	H,A */
                I.HL.SetH(I.AF.H);
                break;

            case 0x68:
                i8085_ICount[0] -= 5;
                /* MOV	L,B */
                I.HL.SetL(I.BC.H);
                break;
            case 0x69:
                i8085_ICount[0] -= 5;
                /* MOV	L,C */
                I.HL.SetL(I.BC.L);
                break;
            case 0x6a:
                i8085_ICount[0] -= 5;
                /* MOV	L,D */
                I.HL.SetL(I.DE.H);
                break;
            case 0x6b:
                i8085_ICount[0] -= 5;
                /* MOV	L,E */
                I.HL.SetL(I.DE.L);
                break;
            case 0x6c:
                i8085_ICount[0] -= 5;
                /* MOV	L,H */
                I.HL.SetL(I.HL.H);
                break;
            case 0x6d:
                i8085_ICount[0] -= 5;
                /* MOV	L,L */
 /* no op */
                break;
            case 0x6e:
                i8085_ICount[0] -= 7;
                /* MOV	L,M */
                I.HL.SetL(RM((char) I.HL.D));
                break;
            case 0x6f:
                i8085_ICount[0] -= 5;
                /* MOV	L,A */
                I.HL.SetL(I.AF.H);
                break;

            case 0x70:
                i8085_ICount[0] -= 7;
                /* MOV	M,B */
                WM(I.HL.D, I.BC.H);
                break;
            case 0x71:
                i8085_ICount[0] -= 7;
                /* MOV	M,C */
                WM(I.HL.D, I.BC.L);
                break;
            case 0x72:
                i8085_ICount[0] -= 7;
                /* MOV	M,D */
                WM(I.HL.D, I.DE.H);
                break;
            case 0x73:
                i8085_ICount[0] -= 7;
                /* MOV	M,E */
                WM(I.HL.D, I.DE.L);
                break;
            case 0x74:
                i8085_ICount[0] -= 7;
                /* MOV	M,H */
                WM(I.HL.D, I.HL.H);
                break;
            case 0x75:
                i8085_ICount[0] -= 7;
                /* MOV	M,L */
                WM(I.HL.D, I.HL.L);
                break;
            case 0x76:
                i8085_ICount[0] -= 4;
                /* HALT */
                I.PC.AddD(-1);
                I.u8_HALT = 1;
                if (i8085_ICount[0] > 0) {
                    i8085_ICount[0] = 0;
                }
                break;
            case 0x77:
                i8085_ICount[0] -= 7;
                /* MOV	M,A */
                WM(I.HL.D, I.AF.H);
                break;

            case 0x78:
                i8085_ICount[0] -= 5;
                /* MOV	A,B */
                I.AF.SetH(I.BC.H);
                break;
            case 0x79:
                i8085_ICount[0] -= 5;
                /* MOV	A,C */
                I.AF.SetH(I.BC.L);
                break;
            case 0x7a:
                i8085_ICount[0] -= 5;
                /* MOV	A,D */
                I.AF.SetH(I.DE.H);
                break;
            case 0x7b:
                i8085_ICount[0] -= 5;
                /* MOV	A,E */
                I.AF.SetH(I.DE.L);
                break;
            case 0x7c:
                i8085_ICount[0] -= 5;
                /* MOV	A,H */
                I.AF.SetH(I.HL.H);
                break;
            case 0x7d:
                i8085_ICount[0] -= 5;
                /* MOV	A,L */
                I.AF.SetH(I.HL.L);
                break;
            case 0x7e:
                i8085_ICount[0] -= 7;
                /* MOV	A,M */
                I.AF.SetH(RM((char) I.HL.D));
                break;
            case 0x7f:
                i8085_ICount[0] -= 5;
                /* MOV	A,A */
 /* no op */
                break;

            case 0x80:
                i8085_ICount[0] -= 4;
                /* ADD	B */
                M_ADD(I.BC.H);
                break;
            case 0x81:
                i8085_ICount[0] -= 4;
                /* ADD	C */
                M_ADD(I.BC.L);
                break;
            case 0x82:
                i8085_ICount[0] -= 4;
                /* ADD	D */
                M_ADD(I.DE.H);
                break;
            case 0x83:
                i8085_ICount[0] -= 4;
                /* ADD	E */
                M_ADD(I.DE.L);
                break;
            case 0x84:
                i8085_ICount[0] -= 4;
                /* ADD	H */
                M_ADD(I.HL.H);
                break;
            case 0x85:
                i8085_ICount[0] -= 4;
                /* ADD	L */
                M_ADD(I.HL.L);
                break;
            case 0x86:
                i8085_ICount[0] -= 7;
                /* ADD	M */
                M_ADD(RM((char) I.HL.D));
                break;
            case 0x87:
                i8085_ICount[0] -= 4;
                /* ADD	A */
                M_ADD(I.AF.H);
                break;

            case 0x88:
                i8085_ICount[0] -= 4;
                /* ADC	B */
                M_ADC(I.BC.H);
                break;
            case 0x89:
                i8085_ICount[0] -= 4;
                /* ADC	C */
                M_ADC(I.BC.L);
                break;
            case 0x8a:
                i8085_ICount[0] -= 4;
                /* ADC	D */
                M_ADC(I.DE.H);
                break;
            case 0x8b:
                i8085_ICount[0] -= 4;
                /* ADC	E */
                M_ADC(I.DE.L);
                break;
            case 0x8c:
                i8085_ICount[0] -= 4;
                /* ADC	H */
                M_ADC(I.HL.H);
                break;
            case 0x8d:
                i8085_ICount[0] -= 4;
                /* ADC	L */
                M_ADC(I.HL.L);
                break;
            case 0x8e:
                i8085_ICount[0] -= 7;
                /* ADC	M */
                M_ADC(RM((char) I.HL.D));
                break;
            case 0x8f:
                i8085_ICount[0] -= 4;
                /* ADC	A */
                M_ADC(I.AF.H);
                break;
            case 0x90:
                i8085_ICount[0] -= 4;
                /* SUB	B */
                M_SUB(I.BC.H);
                break;
            case 0x91:
                i8085_ICount[0] -= 4;
                /* SUB	C */
                M_SUB(I.BC.L);
                break;
            case 0x92:
                i8085_ICount[0] -= 4;
                /* SUB	D */
                M_SUB(I.DE.H);
                break;
            case 0x93:
                i8085_ICount[0] -= 4;
                /* SUB	E */
                M_SUB(I.DE.L);
                break;
            case 0x94:
                i8085_ICount[0] -= 4;
                /* SUB	H */
                M_SUB(I.HL.H);
                break;
            case 0x95:
                i8085_ICount[0] -= 4;
                /* SUB	L */
                M_SUB(I.HL.L);
                break;
            case 0x96:
                i8085_ICount[0] -= 7;
                /* SUB	M */
                M_SUB(RM((char) I.HL.D));
                break;
            case 0x97:
                i8085_ICount[0] -= 4;
                /* SUB	A */
                M_SUB(I.AF.H);
                break;

            case 0x98:
                i8085_ICount[0] -= 4;
                /* SBB	B */
                M_SBB(I.BC.H);
                break;
            case 0x99:
                i8085_ICount[0] -= 4;
                /* SBB	C */
                M_SBB(I.BC.L);
                break;
            case 0x9a:
                i8085_ICount[0] -= 4;
                /* SBB	D */
                M_SBB(I.DE.H);
                break;
            case 0x9b:
                i8085_ICount[0] -= 4;
                /* SBB	E */
                M_SBB(I.DE.L);
                break;
            case 0x9c:
                i8085_ICount[0] -= 4;
                /* SBB	H */
                M_SBB(I.HL.H);
                break;
            case 0x9d:
                i8085_ICount[0] -= 4;
                /* SBB	L */
                M_SBB(I.HL.L);
                break;
            case 0x9e:
                i8085_ICount[0] -= 7;
                /* SBB	M */
                M_SBB(RM((char) I.HL.D));
                break;
            case 0x9f:
                i8085_ICount[0] -= 4;
                /* SBB	A */
                M_SBB(I.AF.H);
                break;

            case 0xa0:
                i8085_ICount[0] -= 4;
                /* ANA	B */
                M_ANA(I.BC.H);
                break;
            case 0xa1:
                i8085_ICount[0] -= 4;
                /* ANA	C */
                M_ANA(I.BC.L);
                break;
            case 0xa2:
                i8085_ICount[0] -= 4;
                /* ANA	D */
                M_ANA(I.DE.H);
                break;
            case 0xa3:
                i8085_ICount[0] -= 4;
                /* ANA	E */
                M_ANA(I.DE.L);
                break;
            case 0xa4:
                i8085_ICount[0] -= 4;
                /* ANA	H */
                M_ANA(I.HL.H);
                break;
            case 0xa5:
                i8085_ICount[0] -= 4;
                /* ANA	L */
                M_ANA(I.HL.L);
                break;
            case 0xa6:
                i8085_ICount[0] -= 7;
                /* ANA	M */
                M_ANA(RM((char) I.HL.D));
                break;
            case 0xa7:
                i8085_ICount[0] -= 4;
                /* ANA	A */
                M_ANA(I.AF.H);
                break;

            case 0xa8:
                i8085_ICount[0] -= 4;
                /* XRA	B */
                M_XRA(I.BC.H);
                break;
            case 0xa9:
                i8085_ICount[0] -= 4;
                /* XRA	C */
                M_XRA(I.BC.L);
                break;
            case 0xaa:
                i8085_ICount[0] -= 4;
                /* XRA	D */
                M_XRA(I.DE.H);
                break;
            case 0xab:
                i8085_ICount[0] -= 4;
                /* XRA	E */
                M_XRA(I.DE.L);
                break;
            case 0xac:
                i8085_ICount[0] -= 4;
                /* XRA	H */
                M_XRA(I.HL.H);
                break;
            case 0xad:
                i8085_ICount[0] -= 4;
                /* XRA	L */
                M_XRA(I.HL.L);
                break;
            case 0xae:
                i8085_ICount[0] -= 7;
                /* XRA	M */
                M_XRA(RM((char) I.HL.D));
                break;
            case 0xaf:
                i8085_ICount[0] -= 4;
                /* XRA	A */
                M_XRA(I.AF.H);
                break;

            case 0xb0:
                i8085_ICount[0] -= 4;
                /* ORA	B */
                M_ORA(I.BC.H);
                break;
            case 0xb1:
                i8085_ICount[0] -= 4;
                /* ORA	C */
                M_ORA(I.BC.L);
                break;
            case 0xb2:
                i8085_ICount[0] -= 4;
                /* ORA	D */
                M_ORA(I.DE.H);
                break;
            case 0xb3:
                i8085_ICount[0] -= 4;
                /* ORA	E */
                M_ORA(I.DE.L);
                break;
            case 0xb4:
                i8085_ICount[0] -= 4;
                /* ORA	H */
                M_ORA(I.HL.H);
                break;
            case 0xb5:
                i8085_ICount[0] -= 4;
                /* ORA	L */
                M_ORA(I.HL.L);
                break;
            case 0xb6:
                i8085_ICount[0] -= 7;
                /* ORA	M */
                M_ORA(RM((char) I.HL.D));
                break;
            case 0xb7:
                i8085_ICount[0] -= 4;
                /* ORA	A */
                M_ORA(I.AF.H);
                break;

            case 0xb8:
                i8085_ICount[0] -= 4;
                /* CMP	B */
                M_CMP(I.BC.H);
                break;
            case 0xb9:
                i8085_ICount[0] -= 4;
                /* CMP	C */
                M_CMP(I.BC.L);
                break;
            case 0xba:
                i8085_ICount[0] -= 4;
                /* CMP	D */
                M_CMP(I.DE.H);
                break;
            case 0xbb:
                i8085_ICount[0] -= 4;
                /* CMP	E */
                M_CMP(I.DE.L);
                break;
            case 0xbc:
                i8085_ICount[0] -= 4;
                /* CMP	H */
                M_CMP(I.HL.H);
                break;
            case 0xbd:
                i8085_ICount[0] -= 4;
                /* CMP	L */
                M_CMP(I.HL.L);
                break;
            case 0xbe:
                i8085_ICount[0] -= 7;
                /* CMP	M */
                M_CMP(RM((char) I.HL.D));
                break;
            case 0xbf:
                i8085_ICount[0] -= 4;
                /* CMP	A */
                M_CMP(I.AF.H);
                break;

            case 0xc0:
                i8085_ICount[0] -= 5;
                /* RNZ	*/
                M_RET((I.AF.L & ZF) == 0);
                break;
            case 0xc1:
                i8085_ICount[0] -= 10;
                /* POP	B */
                //M_POP(BC);
                I.BC.SetL(RM((char) I.SP.D));
                I.SP.AddD(1);
                I.BC.SetH(RM((char) I.SP.D));
                I.SP.AddD(1);
                break;
            case 0xc2:
                i8085_ICount[0] -= 10;
                /* JNZ	nnnn */
                M_JMP((I.AF.L & ZF) == 0);
                break;
            case 0xc3:
                i8085_ICount[0] -= 10;
                /* JMP	nnnn */
                M_JMP(true);
                break;
            case 0xc4:
                i8085_ICount[0] -= 11;
                /* CNZ	nnnn */
                M_CALL((I.AF.L & ZF) == 0);
                break;
            case 0xc5:
                i8085_ICount[0] -= 11;
                /* PUSH B */
                //M_PUSH(BC);
                I.SP.AddD(-1);
                WM(I.SP.D, I.BC.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.BC.L);
                break;
            case 0xc6:
                i8085_ICount[0] -= 7;
                /* ADI	nn */
                I.XX.SetL(ARG());
                M_ADD(I.XX.L);
                break;
            case 0xc7:
                i8085_ICount[0] -= 11;
                /* RST	0 */
                M_RST(0);
                break;

            case 0xc8:
                i8085_ICount[0] -= 5;
                /* RZ	*/
                M_RET((I.AF.L & ZF) != 0);
                break;
            case 0xc9:
                i8085_ICount[0] -= 4;
                /* RET	*/
                M_RET(true);
                break;
            case 0xca:
                i8085_ICount[0] -= 10;
                /* JZ	nnnn */
                M_JMP((I.AF.L & ZF) != 0);
                break;
            case 0xcb:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0xcc:
                i8085_ICount[0] -= 11;
                /* CZ	nnnn */
                M_CALL((I.AF.L & ZF) != 0);
                break;
            case 0xcd:
                i8085_ICount[0] -= 11;
                /* CALL nnnn */
                M_CALL(true);
                break;
            case 0xce:
                i8085_ICount[0] -= 7;
                /* ACI	nn */
                I.XX.SetL(ARG());
                M_ADC(I.XX.L);
                break;
            case 0xcf:
                i8085_ICount[0] -= 11;
                /* RST	1 */
                M_RST(1);
                break;

            case 0xd0:
                i8085_ICount[0] -= 5;
                /* RNC	*/
                M_RET((I.AF.L & CF) == 0);
                break;
            case 0xd1:
                i8085_ICount[0] -= 10;
                /* POP	D */
                //M_POP(DE);
                I.DE.SetL(RM((char) I.SP.D));
                I.SP.AddD(1);
                I.DE.SetH(RM((char) I.SP.D));
                I.SP.AddD(1);
                break;
            case 0xd2:
                i8085_ICount[0] -= 10;
                /* JNC	nnnn */
                M_JMP((I.AF.L & CF) == 0);
                break;
            case 0xd3:
                i8085_ICount[0] -= 10;
                /* OUT	nn */
                M_OUT();
                break;
            case 0xd4:
                i8085_ICount[0] -= 11;
                /* CNC	nnnn */
                M_CALL((I.AF.L & CF) == 0);
                break;
            case 0xd5:
                i8085_ICount[0] -= 11;
                /* PUSH D */
                //M_PUSH(DE);
                I.SP.AddD(-1);
                WM(I.SP.D, I.DE.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.DE.L);
                break;
            case 0xd6:
                i8085_ICount[0] -= 7;
                /* SUI	nn */
                I.XX.SetL(ARG());
                M_SUB(I.XX.L);
                break;
            case 0xd7:
                i8085_ICount[0] -= 11;
                /* RST	2 */
                M_RST(2);
                break;

            case 0xd8:
                i8085_ICount[0] -= 5;
                /* RC	*/
                M_RET((I.AF.L & CF) != 0);
                break;
            case 0xd9:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0xda:
                i8085_ICount[0] -= 10;
                /* JC	nnnn */
                M_JMP((I.AF.L & CF) != 0);
                break;
            case 0xdb:
                i8085_ICount[0] -= 10;
                /* IN	nn */
                M_IN();
                break;
            case 0xdc:
                i8085_ICount[0] -= 11;
                /* CC	nnnn */
                M_CALL((I.AF.L & CF) != 0);
                break;
            case 0xdd:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0xde:
                i8085_ICount[0] -= 7;
                /* SBI	nn */
                I.XX.SetL(ARG());
                M_SBB(I.XX.L);
                break;
            case 0xdf:
                i8085_ICount[0] -= 11;
                /* RST	3 */
                M_RST(3);
                break;

            case 0xe0:
                i8085_ICount[0] -= 5;
                /* RPE	  */
                M_RET((I.AF.L & VF) == 0);
                break;
            case 0xe1:
                i8085_ICount[0] -= 10;
                /* POP	H */
                //M_POP(HL);
                I.HL.SetL(RM((char) I.SP.D));
                I.SP.AddD(1);
                I.HL.SetH(RM((char) I.SP.D));
                I.SP.AddD(1);
                break;
            case 0xe2:
                i8085_ICount[0] -= 10;
                /* JPE	nnnn */
                M_JMP((I.AF.L & VF) == 0);
                break;
            case 0xe3:
                i8085_ICount[0] -= 18;
                /* XTHL */
                //M_POP(XX);
                I.XX.SetL(RM((char) I.SP.D));
                I.SP.AddD(1);
                I.XX.SetH(RM((char) I.SP.D));
                I.SP.AddD(1);
                //M_PUSH(HL);
                I.SP.AddD(-1);
                WM(I.SP.D, I.HL.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.HL.L);
                I.HL.SetD(I.XX.D);
                break;
            case 0xe4:
                i8085_ICount[0] -= 11;
                /* CPE	nnnn */
                M_CALL((I.AF.L & VF) == 0);
                break;
            case 0xe5:
                i8085_ICount[0] -= 11;
                /* PUSH H */
                //M_PUSH(HL);
                I.SP.AddD(-1);
                WM(I.SP.D, I.HL.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.HL.L);
                break;
            case 0xe6:
                i8085_ICount[0] -= 7;
                /* ANI	nn */
                I.XX.SetL(ARG());
                M_ANA(I.XX.L);
                break;
            case 0xe7:
                i8085_ICount[0] -= 11;
                /* RST	4 */
                M_RST(4);
                break;

            case 0xe8:
                i8085_ICount[0] -= 5;
                /* RPO	*/
                M_RET((I.AF.L & VF) != 0);
                break;
            case 0xe9:
                i8085_ICount[0] -= 5;
                /* PCHL */
                I.PC.SetD(I.HL.D);
                change_pc16(I.PC.D);
                break;
            case 0xea:
                i8085_ICount[0] -= 10;
                /* JPO	nnnn */
                M_JMP((I.AF.L & VF) != 0);
                break;
            case 0xeb:
                i8085_ICount[0] -= 4;
                /* XCHG */
                I.XX.SetD(I.DE.D);
                I.DE.SetD(I.HL.D);
                I.HL.SetD(I.XX.D);
                break;
            case 0xec:
                i8085_ICount[0] -= 11;
                /* CPO	nnnn */
                M_CALL((I.AF.L & VF) != 0);
                break;
            case 0xed:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0xee:
                i8085_ICount[0] -= 7;
                /* XRI	nn */
                I.XX.SetL(ARG());
                M_XRA(I.XX.L);
                break;
            case 0xef:
                i8085_ICount[0] -= 11;
                /* RST	5 */
                M_RST(5);
                break;

            case 0xf0:
                i8085_ICount[0] -= 5;
                /* RP	*/
                M_RET((I.AF.L & SF) == 0);
                break;
            case 0xf1:
                i8085_ICount[0] -= 10;
                /* POP	A */
                //M_POP(AF);
                I.AF.SetL(RM((char) I.SP.D));
                I.SP.AddD(1);
                I.AF.SetH(RM((char) I.SP.D));
                I.SP.AddD(1);
                break;
            case 0xf2:
                i8085_ICount[0] -= 10;
                /* JP	nnnn */
                M_JMP((I.AF.L & SF) == 0);
                break;
            case 0xf3:
                i8085_ICount[0] -= 4;
                /* DI	*/
 /* remove interrupt enable */
                I.u8_IM = (char) ((I.u8_IM & ~IM_IEN) & 0xFF);
                break;
            case 0xf4:
                i8085_ICount[0] -= 11;
                /* CP	nnnn */
                M_CALL((I.AF.L & SF) == 0);
                break;
            case 0xf5:
                i8085_ICount[0] -= 11;
                /* PUSH A */
                //M_PUSH(AF);
                I.SP.AddD(-1);
                WM(I.SP.D, I.AF.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.AF.L);
                break;
            case 0xf6:
                i8085_ICount[0] -= 7;
                /* ORI	nn */
                I.XX.SetL(ARG());
                M_ORA(I.XX.L);
                break;
            case 0xf7:
                i8085_ICount[0] -= 11;
                /* RST	6 */
                M_RST(6);
                break;

            case 0xf8:
                i8085_ICount[0] -= 5;
                /* RM	*/
                M_RET((I.AF.L & SF) != 0);
                break;
            case 0xf9:
                i8085_ICount[0] -= 5;
                /* SPHL */
                I.SP.SetD(I.HL.D);
                break;
            case 0xfa:
                i8085_ICount[0] -= 10;
                /* JM	nnnn */
                M_JMP((I.AF.L & SF) != 0);
                break;
            case 0xfb:
                i8085_ICount[0] -= 4;
                /* EI */
 /* set interrupt enable */
                I.u8_IM = (char) ((I.u8_IM | IM_IEN) & 0xFF);
                /* remove serviced IRQ flag */
                I.u8_IREQ = (char) ((I.u8_IREQ & ~I.u8_ISRV) & 0xFF);
                /* reset serviced IRQ */
                I.u8_ISRV = 0;
                if (I.irq_state[0] != CLEAR_LINE) {
                    //LOG(("i8085 EI sets INTR\n"));
                    I.u8_IREQ = (char) ((I.u8_IREQ | IM_INTR) & 0xFF);
                    I.INTR = I8085_INTR;
                }
                if (I.cputype != 0) {
                    if (I.irq_state[1] != CLEAR_LINE) {
                        //LOG(("i8085 EI sets RST5.5\n"));
                        I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST55) & 0xFF);
                    }
                    if (I.irq_state[2] != CLEAR_LINE) {
                        //LOG(("i8085 EI sets RST6.5\n"));
                        I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST65) & 0xFF);
                    }
                    if (I.irq_state[3] != CLEAR_LINE) {
                        //LOG(("i8085 EI sets RST7.5\n"));
                        I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST75) & 0xFF);
                    }
                    /* find highest priority IREQ flag with
				   IM enabled and schedule for execution */
                    if ((I.u8_IM & IM_RST75) == 0 && (I.u8_IREQ & IM_RST75) != 0) {
                        I.u8_ISRV = IM_RST75;
                        I.IRQ2 = ADDR_RST75;
                    } else if ((I.u8_IM & IM_RST65) == 0 && (I.u8_IREQ & IM_RST65) != 0) {
                        I.u8_ISRV = IM_RST65;
                        I.IRQ2 = ADDR_RST65;
                    } else if ((I.u8_IM & IM_RST55) == 0 && (I.u8_IREQ & IM_RST55) != 0) {
                        I.u8_ISRV = IM_RST55;
                        I.IRQ2 = ADDR_RST55;
                    } else if ((I.u8_IM & IM_INTR) == 0 && (I.u8_IREQ & IM_INTR) != 0) {
                        I.u8_ISRV = IM_INTR;
                        I.IRQ2 = I.INTR;
                    }
                } else if ((I.u8_IM & IM_INTR) == 0 && (I.u8_IREQ & IM_INTR) != 0) {
                    I.u8_ISRV = IM_INTR;
                    I.IRQ2 = I.INTR;
                }
                break;
            case 0xfc:
                i8085_ICount[0] -= 11;
                /* CM	nnnn */
                M_CALL((I.AF.L & SF) != 0);
                break;
            case 0xfd:
                i8085_ICount[0] -= 4;
                /* ???? */
                illegal();
                break;
            case 0xfe:
                i8085_ICount[0] -= 7;
                /* CPI	nn */
                I.XX.SetL(ARG());
                M_CMP(I.XX.L);
                break;
            case 0xff:
                i8085_ICount[0] -= 11;
                /* RST	7 */
                M_RST(7);
                break;
            default://debug only
                System.out.println(Integer.toHexString(opcode));
        }
    }

    static void Interrupt() {

        if (I.u8_HALT != 0) /* if the CPU was halted */ {
            I.PC.AddD(1);
            /* skip HALT instr */
            I.u8_HALT = 0;
        }
        I.u8_IM = (char) ((I.u8_IM & ~IM_IEN) & 0xFF);
        /* remove general interrupt enable bit */

        if (I.u8_ISRV == IM_INTR) {
            //LOG(("Interrupt get INTR vector\n"));
            I.IRQ1 = (I.irq_callback).handler(0);
        }

        if (I.cputype != 0) {
            if (I.u8_ISRV == IM_RST55) {
                //LOG(("Interrupt get RST5.5 vector\n"));
                I.IRQ1 = (I.irq_callback).handler(1);
            }

            if (I.u8_ISRV == IM_RST65) {
                //LOG(("Interrupt get RST6.5 vector\n"));
                I.IRQ1 = (I.irq_callback).handler(2);
            }

            if (I.u8_ISRV == IM_RST75) {
                //LOG(("Interrupt get RST7.5 vector\n"));
                I.IRQ1 = (I.irq_callback).handler(3);
            }
        }

        switch (I.IRQ1 & 0xff0000) {
            case 0xcd0000:
                /* CALL nnnn */
                i8085_ICount[0] -= 7;
                 {
                    //M_PUSH(PC);
                    I.SP.AddD(-1);
                    WM(I.SP.D, I.PC.H);
                    I.SP.AddD(-1);
                    WM(I.SP.D, I.PC.L);
                }
            case 0xc30000:
                /* JMP	nnnn */
                i8085_ICount[0] -= 10;
                I.PC.SetD(I.IRQ1 & 0xffff);
                change_pc16(I.PC.D);
                break;
            default:
                switch (I.IRQ1) {
                    case I8085_TRAP:
                    case I8085_RST75:
                    case I8085_RST65:
                    case I8085_RST55: {
                        //M_PUSH(PC);
                        I.SP.AddD(-1);
                        WM(I.SP.D, I.PC.H);
                        I.SP.AddD(-1);
                        WM(I.SP.D, I.PC.L);
                    }
                    if (I.IRQ1 != I8085_RST75) {
                        I.PC.SetD(I.IRQ1);
                    } else {
                        I.PC.SetD(0x3c);
                    }
                    change_pc16(I.PC.D);
                    break;
                    default:
                        //LOG(("i8085 take int $%02x\n", I.IRQ1));
                        execute_one(I.IRQ1 & 0xff);
                }
        }
    }

    @Override
    public int execute(int cycles) {
        i8085_ICount[0] = cycles;
        do {
            /* interrupts enabled or TRAP pending ? */
            if ((I.u8_IM & IM_IEN) != 0 || (I.u8_IREQ & IM_TRAP) != 0) {
                /* copy scheduled to executed interrupt request */
                I.IRQ1 = I.IRQ2;
                /* reset scheduled interrupt request */
                I.IRQ2 = 0;
                /* interrupt now ? */
                if (I.IRQ1 != 0) {
                    Interrupt();
                }
            }

            /* here we go... */
            execute_one(ROP());

        } while (i8085_ICount[0] > 0);

        return cycles - i8085_ICount[0];
    }

    /**
     * **************************************************************************
     * Initialise the various lookup tables used by the emulation code
     * **************************************************************************
     */
    static void init_tables() {
        int/*UINT8*/ zs;
        int i, p;
        for (i = 0; i < 256; i++) {
            zs = 0;
            if (i == 0) {
                zs |= ZF;
            }
            if ((i & 128) != 0) {
                zs |= SF;
            }
            p = 0;
            if ((i & 1) != 0) {
                ++p;
            }
            if ((i & 2) != 0) {
                ++p;
            }
            if ((i & 4) != 0) {
                ++p;
            }
            if ((i & 8) != 0) {
                ++p;
            }
            if ((i & 16) != 0) {
                ++p;
            }
            if ((i & 32) != 0) {
                ++p;
            }
            if ((i & 64) != 0) {
                ++p;
            }
            if ((i & 128) != 0) {
                ++p;
            }
            ZS[i] = zs & 0xFF;
            ZSP[i] = (zs | ((p & 1) != 0 ? 0 : VF)) & 0xFF;
        }
    }

    /**
     * **************************************************************************
     * Reset the 8085 emulation
     * **************************************************************************
     */
    @Override
    public void reset(Object param) {

        init_tables();
        //memset(&I, 0, sizeof(i8085_Regs));
        I.cputype = 1;
        change_pc16(I.PC.D);
    }

    /**
     * **************************************************************************
     * Shut down the CPU emulation
     * **************************************************************************
     */
    @Override
    public void exit() {
        /* nothing to do */
    }

    /**
     * **************************************************************************
     * Get the current 8085 context
     * **************************************************************************
     */
    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        /*TODO*///	if( dst )
/*TODO*///		*(i8085_Regs*)dst = I;
/*TODO*///	return sizeof(i8085_Regs);
    }

    /**
     * **************************************************************************
     * Set the current 8085 context
     * **************************************************************************
     */
    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        /*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		I = *(i8085_Regs*)src;
/*TODO*///		change_pc(I.PC.d);
/*TODO*///	}
    }

    /**
     * **************************************************************************
     * Get the current 8085 PC
     * **************************************************************************
     */
    @Override
    public int get_pc() {
        return I.PC.D;
    }

    /**
     * **************************************************************************
     * Set the current 8085 PC
     * **************************************************************************
     */
    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	I.PC.w.l = val;
/*TODO*///	change_pc(I.PC.d);
    }

    /**
     * **************************************************************************
     * Get the current 8085 SP
     * **************************************************************************
     */
    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	return I.SP.d;
    }

    /**
     * **************************************************************************
     * Set the current 8085 SP
     * **************************************************************************
     */
    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	I.SP.w.l = val;
    }

    /**
     * **************************************************************************
     * Get a specific register
     * **************************************************************************
     */
    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case I8085_PC: return I.PC.w.l;
/*TODO*///		case I8085_SP: return I.SP.w.l;
/*TODO*///		case I8085_AF: return I.AF.w.l;
/*TODO*///		case I8085_BC: return I.BC.w.l;
/*TODO*///		case I8085_DE: return I.DE.w.l;
/*TODO*///		case I8085_HL: return I.HL.w.l;
/*TODO*///		case I8085_IM: return I.IM;
/*TODO*///		case I8085_HALT: return I.HALT;
/*TODO*///		case I8085_IREQ: return I.IREQ;
/*TODO*///		case I8085_ISRV: return I.ISRV;
/*TODO*///		case I8085_VECTOR: return I.INTR;
/*TODO*///		case I8085_TRAP_STATE: return I.nmi_state;
/*TODO*///		case I8085_INTR_STATE: return I.irq_state[I8085_INTR_LINE];
/*TODO*///		case I8085_RST55_STATE: return I.irq_state[I8085_RST55_LINE];
/*TODO*///		case I8085_RST65_STATE: return I.irq_state[I8085_RST65_LINE];
/*TODO*///		case I8085_RST75_STATE: return I.irq_state[I8085_RST75_LINE];
/*TODO*///		case REG_PREVIOUSPC: return 0; /* previous pc not supported */
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = I.SP.w.l + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///					return RM( offset ) + ( RM( offset+1 ) << 8 );
/*TODO*///			}
/*TODO*///	}
/*TODO*///	return 0;
    }

    /**
     * **************************************************************************
     * Set a specific register
     * **************************************************************************
     */
    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case I8085_PC: I.PC.w.l = val; break;
/*TODO*///		case I8085_SP: I.SP.w.l = val; break;
/*TODO*///		case I8085_AF: I.AF.w.l = val; break;
/*TODO*///		case I8085_BC: I.BC.w.l = val; break;
/*TODO*///		case I8085_DE: I.DE.w.l = val; break;
/*TODO*///		case I8085_HL: I.HL.w.l = val; break;
/*TODO*///		case I8085_IM: I.IM = val; break;
/*TODO*///		case I8085_HALT: I.HALT = val; break;
/*TODO*///		case I8085_IREQ: I.IREQ = val; break;
/*TODO*///		case I8085_ISRV: I.ISRV = val; break;
/*TODO*///		case I8085_VECTOR: I.INTR = val; break;
/*TODO*///		case I8085_TRAP_STATE: I.nmi_state = val; break;
/*TODO*///		case I8085_INTR_STATE: I.irq_state[I8085_INTR_LINE] = val; break;
/*TODO*///		case I8085_RST55_STATE: I.irq_state[I8085_RST55_LINE] = val; break;
/*TODO*///		case I8085_RST65_STATE: I.irq_state[I8085_RST65_LINE] = val; break;
/*TODO*///		case I8085_RST75_STATE: I.irq_state[I8085_RST75_LINE] = val; break;
/*TODO*///		default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = I.SP.w.l + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///				{
/*TODO*///					WM( offset, val&0xff );
/*TODO*///					WM( offset+1, (val>>8)&0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///	}
    }

    /*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Set the 8085 SID input signal state										*/
/*TODO*////****************************************************************************/
/*TODO*///void i8085_set_SID(int state)
/*TODO*///{
/*TODO*///	LOG(("i8085: SID %d\n", state));
/*TODO*///	if (state)
/*TODO*///		I.IM |= IM_SID;
/*TODO*///	else
/*TODO*///		I.IM &= ~IM_SID;
/*TODO*///}
    /**
     * *************************************************************************
     */
    /* Set a callback to be called at SOD output change 						
    /****************************************************************************/
    public static void i8085_set_sod_callback(sod_callbackPtr callback) {
        I.sod_callback = callback;
    }

    /**
     * *************************************************************************
     */
    /* Set TRAP signal state													
    /**
     * *************************************************************************
     */
    static void i8085_set_TRAP(int state) {
        //LOG(("i8085: TRAP %d\n", state));
        if (state != 0) {
            I.u8_IREQ = (char) ((I.u8_IREQ | IM_TRAP) & 0xFF);
            if ((I.u8_ISRV & IM_TRAP) != 0) {
                return;
                /* already servicing TRAP ? */
            }
            I.u8_ISRV = (char) (IM_TRAP & 0xFF);
            /* service TRAP */
            I.IRQ2 = ADDR_TRAP;
        } else {
            I.u8_IREQ = (char) ((I.u8_IREQ & ~IM_TRAP) & 0xFF);
            /* remove request for TRAP */
        }
    }

    /**
     * *************************************************************************
     */
    /* Set RST7.5 signal state													
    /**
     * *************************************************************************
     */
    static void i8085_set_RST75(int state) {
        //LOG(("i8085: RST7.5 %d\n", state));
        if (state != 0) {

            I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST75) & 0xFF);
            /* request RST7.5 */
            if ((I.u8_IM & IM_RST75) != 0) {
                return;
                /* if masked, ignore it for now */
            }
            if (I.u8_ISRV == 0) /* if no higher priority IREQ is serviced */ {
                I.u8_ISRV = (char) (IM_RST75 & 0xFF);
                /* service RST7.5 */
                I.IRQ2 = ADDR_RST75;
            }
        }
        /* RST7.5 is reset only by SIM or end of service routine ! */
    }

    /**
     * *************************************************************************
     */
    /* Set RST6.5 signal state													
/****************************************************************************/
    public static void i8085_set_RST65(int state) {
        //LOG(("i8085: RST6.5 %d\n", state));
        if (state != 0) {
            I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST65) & 0xFF);
            /* request RST6.5 */
            if ((I.u8_IM & IM_RST65) != 0) {
                return;
                /* if masked, ignore it for now */
            }
            if (I.u8_ISRV == 0) /* if no higher priority IREQ is serviced */ {
                I.u8_ISRV = (char) (IM_RST65 & 0xFF);
                /* service RST6.5 */
                I.IRQ2 = ADDR_RST65;
            }
        } else {
            I.u8_IREQ = (char) ((I.u8_IREQ & ~IM_RST65) & 0xFF);
            /* remove request for RST6.5 */
        }
    }

    /**
     * *************************************************************************
     */
    /* Set RST5.5 signal state													
/****************************************************************************/
    public static void i8085_set_RST55(int state) {
        //LOG(("i8085: RST5.5 %d\n", state));
        if (state != 0) {
            I.u8_IREQ = (char) ((I.u8_IREQ | IM_RST55) & 0xFF);
            /* request RST5.5 */
            if ((I.u8_IM & IM_RST55) != 0) {
                return;
                /* if masked, ignore it for now */
            }
            if (I.u8_ISRV == 0) /* if no higher priority IREQ is serviced */ {
                I.u8_ISRV = (char) (IM_RST55 & 0xFF);
                /* service RST5.5 */
                I.IRQ2 = ADDR_RST55;
            }
        } else {
            I.u8_IREQ = (char) ((I.u8_IREQ & ~IM_RST55) & 0xFF);
            /* remove request for RST5.5 */
        }
    }

    /**
     * *************************************************************************
     */
    /* Set INTR signal															*/
    /**
     * *************************************************************************
     */
    public static void i8085_set_INTR(int state) {
        //LOG(("i8085: INTR %d\n", state));
        if (state != 0) {
            I.u8_IREQ = (char) ((I.u8_IREQ | IM_INTR) & 0xFF);
            /* request INTR */
            I.INTR = state;
            if ((I.u8_IM & IM_INTR) != 0) {
                return;
                /* if masked, ignore it for now */
            }
            if (I.u8_ISRV == 0) /* if no higher priority IREQ is serviced */ {
                I.u8_ISRV = (char) (IM_INTR & 0xFF);
                /* service INTR */
                I.IRQ2 = I.INTR;
            }
        } else {
            I.u8_IREQ = (char) ((I.u8_IREQ & ~IM_INTR) & 0xFF);
            /* remove request for INTR */
        }
    }

    @Override
    public void set_nmi_line(int state) {
        I.nmi_state = state;
        if (state != CLEAR_LINE) {
            i8085_set_TRAP(1);
        }
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        I.irq_state[irqline] = state;
        if (state == CLEAR_LINE) {
            if ((I.u8_IM & IM_IEN) == 0) {
                switch (irqline) {
                    case I8085_INTR_LINE:
                        i8085_set_INTR(0);
                        break;
                    case I8085_RST55_LINE:
                        i8085_set_RST55(0);
                        break;
                    case I8085_RST65_LINE:
                        i8085_set_RST65(0);
                        break;
                    case I8085_RST75_LINE:
                        i8085_set_RST75(0);
                        break;
                }
            }
        } else if ((I.u8_IM & IM_IEN) != 0) {
            switch (irqline) {
                case I8085_INTR_LINE:
                    i8085_set_INTR(1);
                    break;
                case I8085_RST55_LINE:
                    i8085_set_RST55(1);
                    break;
                case I8085_RST65_LINE:
                    i8085_set_RST65(1);
                    break;
                case I8085_RST75_LINE:
                    i8085_set_RST75(1);
                    break;
            }
        }
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        I.irq_callback = callback;
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "AF", &I.AF.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "BC", &I.BC.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "DE", &I.DE.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "HL", &I.HL.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "SP", &I.SP.w.l, 1);
/*TODO*///	state_save_UINT16(file, "i8085", cpu, "PC", &I.PC.w.l, 1);
/*TODO*///	state_save_UINT8(file, "i8085", cpu, "HALT", &I.HALT, 1);
/*TODO*///	state_save_UINT8(file, "i8085", cpu, "IM", &I.IM, 1);
/*TODO*///	state_save_UINT8(file, "i8085", cpu, "IREQ", &I.IREQ, 1);
/*TODO*///	state_save_UINT8(file, "i8085", cpu, "ISRV", &I.ISRV, 1);
/*TODO*///	state_save_UINT32(file, "i8085", cpu, "INTR", &I.INTR, 1);
/*TODO*///	state_save_UINT32(file, "i8085", cpu, "IRQ2", &I.IRQ2, 1);
/*TODO*///	state_save_UINT32(file, "i8085", cpu, "IRQ1", &I.IRQ1, 1);
/*TODO*///	state_save_INT8(file, "i8085", cpu, "NMI_STATE", &I.nmi_state, 1);
/*TODO*///	state_save_INT8(file, "i8085", cpu, "IRQ_STATE", I.irq_state, 4);
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "AF", &I.AF.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "BC", &I.BC.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "DE", &I.DE.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "HL", &I.HL.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "SP", &I.SP.w.l, 1);
/*TODO*///	state_load_UINT16(file, "i8085", cpu, "PC", &I.PC.w.l, 1);
/*TODO*///	state_load_UINT8(file, "i8085", cpu, "HALT", &I.HALT, 1);
/*TODO*///	state_load_UINT8(file, "i8085", cpu, "IM", &I.IM, 1);
/*TODO*///	state_load_UINT8(file, "i8085", cpu, "IREQ", &I.IREQ, 1);
/*TODO*///	state_load_UINT8(file, "i8085", cpu, "ISRV", &I.ISRV, 1);
/*TODO*///	state_load_UINT32(file, "i8085", cpu, "INTR", &I.INTR, 1);
/*TODO*///	state_load_UINT32(file, "i8085", cpu, "IRQ2", &I.IRQ2, 1);
/*TODO*///	state_load_UINT32(file, "i8085", cpu, "IRQ1", &I.IRQ1, 1);
/*TODO*///	state_load_INT8(file, "i8085", cpu, "NMI_STATE", &I.nmi_state, 1);
/*TODO*///	state_load_INT8(file, "i8085", cpu, "IRQ_STATE", I.irq_state, 4);
    }

    /**
     * **************************************************************************
     * Return a formatted string for a register
     * **************************************************************************
     */
    @Override
    public String cpu_info(Object context, int regnum) {

        /*TODO*///	static char buffer[16][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	i8085_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 16;
/*TODO*///	buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &I;
        switch (regnum) {
            /*TODO*///		case CPU_INFO_REG+I8085_AF: sprintf(buffer[which], "AF:%04X", r->AF.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_BC: sprintf(buffer[which], "BC:%04X", r->BC.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_DE: sprintf(buffer[which], "DE:%04X", r->DE.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_HL: sprintf(buffer[which], "HL:%04X", r->HL.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_SP: sprintf(buffer[which], "SP:%04X", r->SP.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
/*TODO*///		case CPU_INFO_REG+I8085_IM: sprintf(buffer[which], "IM:%02X", r->IM); break;
/*TODO*///		case CPU_INFO_REG+I8085_HALT: sprintf(buffer[which], "HALT:%d", r->HALT); break;
/*TODO*///		case CPU_INFO_REG+I8085_IREQ: sprintf(buffer[which], "IREQ:%02X", I.IREQ); break;
/*TODO*///		case CPU_INFO_REG+I8085_ISRV: sprintf(buffer[which], "ISRV:%02X", I.ISRV); break;
/*TODO*///		case CPU_INFO_REG+I8085_VECTOR: sprintf(buffer[which], "VEC:%02X", I.INTR); break;
/*TODO*///		case CPU_INFO_REG+I8085_TRAP_STATE: sprintf(buffer[which], "TRAP:%X", I.nmi_state); break;
/*TODO*///		case CPU_INFO_REG+I8085_INTR_STATE: sprintf(buffer[which], "INTR:%X", I.irq_state[I8085_INTR_LINE]); break;
/*TODO*///		case CPU_INFO_REG+I8085_RST55_STATE: sprintf(buffer[which], "RST55:%X", I.irq_state[I8085_RST55_LINE]); break;
/*TODO*///		case CPU_INFO_REG+I8085_RST65_STATE: sprintf(buffer[which], "RST65:%X", I.irq_state[I8085_RST65_LINE]); break;
/*TODO*///		case CPU_INFO_REG+I8085_RST75_STATE: sprintf(buffer[which], "RST75:%X", I.irq_state[I8085_RST75_LINE]); break;
/*TODO*///		case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->AF.b.l & 0x80 ? 'S':'.',
/*TODO*///				r->AF.b.l & 0x40 ? 'Z':'.',
/*TODO*///				r->AF.b.l & 0x20 ? '?':'.',
/*TODO*///				r->AF.b.l & 0x10 ? 'H':'.',
/*TODO*///				r->AF.b.l & 0x08 ? '?':'.',
/*TODO*///				r->AF.b.l & 0x04 ? 'P':'.',
/*TODO*///				r->AF.b.l & 0x02 ? 'N':'.',
/*TODO*///				r->AF.b.l & 0x01 ? 'C':'.');
/*TODO*///			break;
            case CPU_INFO_NAME:
                return "8085A";
            case CPU_INFO_FAMILY:
                return "Intel 8080";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "i8085.java";
            case CPU_INFO_CREDITS:
                return "Copyright (c) 1999 Juergen Buchmueller, all rights reserved.";
            /*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)i8085_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)i8085_win_layout;
        }
        /*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object init_context() {
        Object reg = new i8085_Regs();
        return reg;
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }

    /**
     *
     * Not used in this cpu
     */
    @Override
    public void internal_interrupt(int type) {
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
}
