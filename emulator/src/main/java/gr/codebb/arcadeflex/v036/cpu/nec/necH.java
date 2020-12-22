package gr.codebb.arcadeflex.v036.cpu.nec;

import static gr.codebb.arcadeflex.v036.cpu.nec.v30.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_readport;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_writeport;

public class necH {

    public static final int NEC_INT_NONE = 0;
    public static final int NEC_NMI_INT = 2;

    public static final int ES = 0, CS = 1, SS = 2, DS = 3;//typedef enum { ES, CS, SS, DS } SREGS;
    public static final int AW = 0, CW = 1, DW = 2, BW = 3, SP = 4, BP = 5, IX = 6, IY = 7;//typedef enum { AW, CW, DW, BW, SP, BP, IX, IY } WREGS;

    public static final int AL = 0, AH = 1, CL = 2, CH = 3, DL = 4, DH = 5, BL = 6, BH = 7, SPL = 8, SPH = 9, BPL = 10, BPH = 11, IXL = 12, IXH = 13, IYL = 14, IYH = 15;

    /* parameter x = result, y = source 1, z = source 2 */
    public static void SetTF(int x) {
        I.TF = x;
    }

    public static void SetIF(int x) {
        I.IF = x;
    }

    public static void SetDF(int x) {
        I.DF = x;
    }

    public static void SetMD(int x) {
        I.MF = x;
    }	/* OB [19.07.99] Mode Flag V30 */


    public static void SetOFW_Add(int x, int y, int z) {
        I.OverVal = ((x) ^ (y)) & ((x) ^ (z)) & 0x8000;

    }

    public static void SetOFB_Add(int x, int y, int z) {
        I.OverVal = ((x) ^ (y)) & ((x) ^ (z)) & 0x80;

    }

    public static void SetOFW_Sub(int x, int y, int z) {
        I.OverVal = ((z) ^ (y)) & ((z) ^ (x)) & 0x8000;

    }

    public static void SetOFB_Sub(int x, int y, int z) {
        I.OverVal = ((z) ^ (y)) & ((z) ^ (x)) & 0x80;
    }

    public static void SetCFB(int x) {
        I.CarryVal = (x) & 0x100;
    }

    public static void SetCFW(int x) {
        I.CarryVal = (x) & 0x10000;
    }

    public static void SetAF(int x, int y, int z) {
        I.AuxVal = ((x) ^ ((y) ^ (z))) & 0x10;

    }

    public static void SetSF(int x) {
        I.SignVal = (x);
    }

    public static void SetZF(int x) {
        I.ZeroVal = (x);
    }

    public static void SetPF(int x) {
        I.ParityVal = (x);
    }

    public static void SetSZPF_Byte(int x) {
        I.SignVal = (byte) (x);
        I.ZeroVal = (byte) (x);
        I.ParityVal = (byte) (x);

    }

    public static void SetSZPF_Word(int x) {
        I.SignVal = (short) (x);
        I.ZeroVal = (short) (x);
        I.ParityVal = (short) (x);
    }
    /*TODO*///
    /*TODO*///#define ADDB(dst,src) { unsigned res=dst+src; SetCFB(res); SetOFB_Add(res,src,dst); SetAF(res,src,dst); SetSZPF_Byte(res); dst=(BYTE)res; }
    /*TODO*///#define ADDW(dst,src) { unsigned res=dst+src; SetCFW(res); SetOFW_Add(res,src,dst); SetAF(res,src,dst); SetSZPF_Word(res); dst=(WORD)res; }
    /*TODO*///
    /*TODO*///#define SUBB(dst,src) { unsigned res=dst-src; SetCFB(res); SetOFB_Sub(res,src,dst); SetAF(res,src,dst); SetSZPF_Byte(res); dst=(BYTE)res; }
    /*TODO*///#define SUBW(dst,src) { unsigned res=dst-src; SetCFW(res); SetOFW_Sub(res,src,dst); SetAF(res,src,dst); SetSZPF_Word(res); dst=(WORD)res; }
    /*TODO*///
    /*TODO*///#define ORB(dst,src) dst|=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Byte(dst)
    /*TODO*///#define ORW(dst,src) dst|=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Word(dst)
    /*TODO*///
    /*TODO*///#define ANDB(dst,src) dst&=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Byte(dst)
    /*TODO*///#define ANDW(dst,src) dst&=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Word(dst)
    /*TODO*///
    /*TODO*///#define XORB(dst,src) dst^=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Byte(dst)
    /*TODO*///#define XORW(dst,src) dst^=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Word(dst)
    /*TODO*///

    static final int CF() {
        return BOOL(I.CarryVal != 0);
    }

    static final int SF() {
        return BOOL(I.SignVal < 0);
    }

    static final int ZF() {
        return BOOL(I.ZeroVal == 0);
    }

    static final int PF() {
        return BOOL(parity_table[(I.ParityVal & 0xFF)]);
    }

    static final int AF() {
        return BOOL(I.AuxVal != 0);
    }

    static final int OF() {
        return BOOL(I.OverVal != 0);
    }

    static final int MD() {
        return BOOL(I.MF != 0);
    }

    /**
     * *********************************************************************
     */

    /* drop lines A16-A19 for a 64KB memory (yes, I know this should be done after adding the offset 8-) */
    /* HJB 12/13/98 instead mask address lines with a driver supplied value */
    static final int SegBase(int Seg) {
        return I.sregs[Seg] << 4;
    }

    static final int DefaultBase(int Seg) {
        return ((I.seg_prefix != 0 && (Seg == DS || Seg == SS)) ? I.prefix_base : I.base[Seg]);
    }
    /* ASG 971005 -- changed to cpu_readmem20/cpu_writemem20 */

    public static int GetMemB(int Seg, int Off) {
        nec_ICount[0] -= 6;
        return cpu_readmem20((DefaultBase(Seg) + (Off))) & 0xFF;
    }

    public static int GetMemW(int Seg, int Off) {
        nec_ICount[0] -= 10;
        return (GetMemB(Seg, Off) & 0xFFFF) + ((GetMemB(Seg, (Off) + 1) << 8) & 0xFFFF);
    }

    public static void PutMemB(int Seg, int Off, int x) {
        nec_ICount[0] -= 7;
        cpu_writemem20((DefaultBase(Seg) + (Off)), (x));
    }

    public static void PutMemW(int Seg, int Off, int x) {
        nec_ICount[0] -= 11;
        PutMemB(Seg, Off, (x) & 0xFF);
        PutMemB(Seg, (Off) + 1, ((x) >> 8) & 0xFF);
    }

    public static int ReadByte(int ea) {
        nec_ICount[0] -= 6;
        return cpu_readmem20((ea)) & 0xFF;
    }

    public static int ReadWord(int ea) {
        nec_ICount[0] -= 10;
        return cpu_readmem20((ea)) + (cpu_readmem20(((ea) + 1)) << 8);
    }

    public static void WriteByte(int ea, int val) {
        nec_ICount[0] -= 7;
        cpu_writemem20((ea), val);
    }

    public static void WriteWord(int ea, int val) {
        nec_ICount[0] -= 11;
        cpu_writemem20((ea), (val & 0xFF));
        cpu_writemem20(((ea) + 1), (val) >> 8);
    }

    public static int read_port(int port) {
        return cpu_readport(port);
    }

    public static void write_port(int port, int val) {
        cpu_writeport(port, val);
    }
    /* no need to go through cpu_readmem for these ones... */
    /* ASG 971222 -- PUSH/POP now use the standard mechanisms; opcode reading is the same */

    public static final int FETCH() {
        int i = cpu_readop_arg((I.base[CS] + I.ip)) & 0xFF;//((BYTE)cpu_readop_arg((I.base[CS]+I.ip++)))
        I.ip = I.ip + 1 & 0xFFFF;//neccesary???
        return i;
    }

    public static final int FETCHOP() {
        int i = cpu_readop((I.base[CS] + I.ip)) & 0xFF;//((BYTE)cpu_readop((I.base[CS]+I.ip++)))
        I.ip = I.ip + 1 & 0xFFFF;//neccesary???
        return i;
    }

    public static final int FETCHWORD() {
        int var = cpu_readop_arg(((I.base[CS] + I.ip))) + (cpu_readop_arg(((I.base[CS] + I.ip + 1))) << 8);
        I.ip = I.ip + 2 & 0xFFFF;
        return var;
    }

    public static final void PUSH(int val) {
        I.regs.SetW(SP, (I.regs.w[SP] - 2) & 0xFFFF);
        WriteWord(((I.base[SS] + I.regs.w[SP])), val);
    }

    public static final int POP() {
        int tmp = ReadWord(((I.base[SS] + I.regs.w[SP])));
        I.regs.SetW(SP, (I.regs.w[SP] + 2) & 0xFFFF);
        return tmp;
    }

    /**
     * *********************************************************************
     */
    public static int CompressFlags() {
        return ((CF() | (PF() << 2) | (AF() << 4) | (ZF() << 6)
                | (SF() << 7) | (I.TF << 8) | (I.IF << 9)
                | (I.DF << 10) | (OF() << 11) | (MD() << 15)) & 0xFFFF);
    }

    public static void ExpandFlags(int f) {
        I.CarryVal = (f) & 1;
        I.ParityVal = NOT((f) & 4);
        I.AuxVal = (f) & 16;
        I.ZeroVal = NOT((f) & 64);
        I.SignVal = ((f) & 128) != 0 ? -1 : 0;
        I.TF = BOOL(((f) & 256) == 256);
        I.IF = BOOL(((f) & 512) == 512);
        I.DF = BOOL(((f) & 1024) == 1024);
        I.OverVal = (f) & 2048;
        I.MF = BOOL(((f) & 0x8000) == 0x8000);
    }

}
