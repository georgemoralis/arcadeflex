package gr.codebb.arcadeflex.v037b7.cpu.i8085;

import static gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;

public class i8085cpuH {

    public static final int SF = 0x80;
    public static final int ZF = 0x40;
    public static final int YF = 0x20;
    public static final int HF = 0x10;
    public static final int XF = 0x08;
    public static final int VF = 0x04;
    public static final int NF = 0x02;
    public static final int CF = 0x01;

    public static final int IM_SID = 0x80;
    public static final int IM_SOD = 0x40;
    public static final int IM_IEN = 0x20;
    public static final int IM_TRAP = 0x10;
    public static final int IM_INTR = 0x08;
    public static final int IM_RST75 = 0x04;
    public static final int IM_RST65 = 0x02;
    public static final int IM_RST55 = 0x01;

    public static final int ADDR_TRAP = 0x0024;
    public static final int ADDR_RST55 = 0x002c;
    public static final int ADDR_RST65 = 0x0034;
    public static final int ADDR_RST75 = 0x003c;
    public static final int ADDR_INTR = 0x0038;

    public static void M_ANA(int R) {
        I.AF.SetH((I.AF.H & R) & 0xFF);
        I.AF.SetL(ZSP[I.AF.H] | HF);
    }

    public static void M_ORA(int R) {
        I.AF.SetH((I.AF.H | R) & 0xFF);
        I.AF.SetL(ZSP[I.AF.H]);
    }

    public static void M_XRA(int R) {
        I.AF.SetH((I.AF.H ^ R) & 0xFF);
        I.AF.SetL(ZSP[I.AF.H]);
    }

    public static void M_RLC() {
        I.AF.SetH((I.AF.H << 1) | (I.AF.H >>> 7));
        I.AF.SetL((I.AF.L & ~(HF + NF + CF)) | (I.AF.H & CF));
    }

    public static void M_RRC() {
        I.AF.SetL((I.AF.L & ~(HF + NF + CF)) | (I.AF.H & CF));
        I.AF.SetH((I.AF.H >>> 1) | (I.AF.H << 7));
    }

    public static void M_RAL() {
        int c = I.AF.L & CF;
        I.AF.SetL((I.AF.L & ~(HF + NF + CF)) | (I.AF.H >>> 7));
        I.AF.SetH((I.AF.H << 1) | c);
    }

    public static void M_RAR() {
        int c = (I.AF.L & CF) << 7;
        I.AF.SetL((I.AF.L & ~(HF + NF + CF)) | (I.AF.H & CF));
        I.AF.SetH((I.AF.H >>> 1) | c);
    }

    public static void M_ADD(int R) {
        int q = I.AF.H + R;
        I.AF.SetL(ZS[q & 255] | ((q >> 8) & CF)
                | ((I.AF.H ^ q ^ R) & HF)
                | (((R ^ I.AF.H ^ SF) & (R ^ q) & SF) >> 5));
        I.AF.SetH(q);
    }

    public static void M_ADC(int R) {
        int q = I.AF.H + R + (I.AF.L & CF);
        I.AF.SetL(ZS[q & 255] | ((q >> 8) & CF)
                | ((I.AF.H ^ q ^ R) & HF)
                | (((R ^ I.AF.H ^ SF) & (R ^ q) & SF) >> 5));
        I.AF.SetH(q);
    }

    public static void M_SUB(int R) {
        int q = I.AF.H - R;
        I.AF.SetL(ZS[q & 255] | ((q >> 8) & CF) | NF
                | ((I.AF.H ^ q ^ R) & HF)
                | (((R ^ I.AF.H) & (I.AF.H ^ q) & SF) >> 5));
        I.AF.SetH(q);
    }

    public static void M_SBB(int R) {
        int q = I.AF.H - R - (I.AF.L & CF);
        I.AF.SetL(ZS[q & 255] | ((q >> 8) & CF) | NF
                | ((I.AF.H ^ q ^ R) & HF)
                | (((R ^ I.AF.H) & (I.AF.H ^ q) & SF) >> 5));
        I.AF.SetH(q);
    }

    public static void M_CMP(int R) {
        int q = I.AF.H - R;
        I.AF.SetL(ZS[q & 255] | ((q >> 8) & CF) | NF
                | ((I.AF.H ^ q ^ R) & HF)
                | (((R ^ I.AF.H) & (I.AF.H ^ q) & SF) >> 5));
    }

    public static void M_IN() {
        I.XX.SetD(ARG());
        I.AF.SetH(cpu_readport(I.XX.D));
    }

    public static void M_OUT() {
        I.XX.SetD(ARG());
        cpu_writeport(I.XX.D, I.AF.H);
    }

    public static void M_DAD(int R) {
        int q = I.HL.D + R;
        I.AF.SetL((I.AF.L & ~(HF + CF))
                | (((I.HL.D ^ q ^ R) >> 8) & HF)
                | ((q >> 16) & CF));
        I.HL.SetD(q);
    }

    public static void M_RET(boolean cc) {
        if (cc) {
            i8085_ICount[0] -= 6;
            //M_POP(PC);
            I.PC.SetL(RM((char) I.SP.D));
            I.SP.AddD(1);
            I.PC.SetH(RM((char) I.SP.D));
            I.SP.AddD(1);
            change_pc16(I.PC.D);
        }
    }

    public static void M_JMP(boolean cc) {
        if (cc) {
            I.PC.SetD(ARG16());
            change_pc16(I.PC.D);
        } else {
            I.PC.AddD(2);
        }
    }

    public static void M_CALL(boolean cc) {
        if (cc) {
            char a = ARG16();
            i8085_ICount[0] -= 6;
            //M_PUSH(PC); 
            {
                I.SP.AddD(-1);
                WM(I.SP.D, I.PC.H);
                I.SP.AddD(-1);
                WM(I.SP.D, I.PC.L);
            }
            I.PC.SetD(a);
            change_pc16(I.PC.D);
        } else {
            I.PC.AddD(2);
        }
    }

    public static void M_RST(int nn) {
        //M_PUSH(PC); 
        {
            I.SP.AddD(-1);
            WM(I.SP.D, I.PC.H);
            I.SP.AddD(-1);
            WM(I.SP.D, I.PC.L);
        }
        I.PC.SetD(8 * nn);
        change_pc16(I.PC.D);
    }
}
