package cpu.nec;

import static cpu.nec.v30.*;
import static mame.memoryH.*;

public class necH {

    public static final int NEC_INT_NONE = 0;
    public static final int NEC_NMI_INT = 2;

    public static final int ES = 0, CS = 1, SS = 2, DS = 3;//typedef enum { ES, CS, SS, DS } SREGS;
    public static final int AW = 0, CW = 1, DW = 2, BW = 3, SP = 4, BP = 5, IX = 6, IY = 8;//typedef enum { AW, CW, DW, BW, SP, BP, IX, IY } WREGS;

    public static final int AL = 0, AH = 1, CL = 2, CH = 3, DL = 4, DH = 5, BL = 6, BH = 7, SPL = 8, SPH = 9, BPL = 10, BPH = 11, IXL = 12, IXH = 13, IYL = 14, IYH = 15;

    /*TODO*////* parameter x = result, y = source 1, z = source 2 */
    /*TODO*///
    /*TODO*///#define SetTF(x)		(I.TF = (x))
    /*TODO*///#define SetIF(x)		(I.IF = (x))
    /*TODO*///#define SetDF(x)		(I.DF = (x))
    /*TODO*///#define SetMD(x)		(I.MF = (x))	/* OB [19.07.99] Mode Flag V30 */
    /*TODO*///
    /*TODO*///#define SetOFW_Add(x,y,z)	(I.OverVal = ((x) /*TODO*/// (y)) & ((x) /*TODO*/// (z)) & 0x8000)
    /*TODO*///#define SetOFB_Add(x,y,z)	(I.OverVal = ((x) /*TODO*/// (y)) & ((x) /*TODO*/// (z)) & 0x80)
    /*TODO*///#define SetOFW_Sub(x,y,z)	(I.OverVal = ((z) /*TODO*/// (y)) & ((z) /*TODO*/// (x)) & 0x8000)
    /*TODO*///#define SetOFB_Sub(x,y,z)	(I.OverVal = ((z) /*TODO*/// (y)) & ((z) /*TODO*/// (x)) & 0x80)
    /*TODO*///
    /*TODO*///#define SetCFB(x)		(I.CarryVal = (x) & 0x100)
    /*TODO*///#define SetCFW(x)		(I.CarryVal = (x) & 0x10000)
    /*TODO*///#define SetAF(x,y,z)	(I.AuxVal = ((x) /*TODO*/// ((y) /*TODO*/// (z))) & 0x10)
    /*TODO*///#define SetSF(x)		(I.SignVal = (x))
    /*TODO*///#define SetZF(x)		(I.ZeroVal = (x))
    /*TODO*///#define SetPF(x)		(I.ParityVal = (x))
    /*TODO*///
    /*TODO*///#define SetSZPF_Byte(x) (I.SignVal=I.ZeroVal=I.ParityVal=(INT8)(x))
    /*TODO*///#define SetSZPF_Word(x) (I.SignVal=I.ZeroVal=I.ParityVal=(INT16)(x))
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
    /*TODO*///#define XORB(dst,src) dst/*TODO*///=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Byte(dst)
    /*TODO*///#define XORW(dst,src) dst/*TODO*///=src; I.CarryVal=I.OverVal=I.AuxVal=0; SetSZPF_Word(dst)
    /*TODO*///
    /*TODO*///#define CF		(I.CarryVal!=0)
    /*TODO*///#define SF		(I.SignVal<0)
    /*TODO*///#define ZF		(I.ZeroVal==0)
    /*TODO*///#define PF		parity_table[(BYTE)I.ParityVal]
    /*TODO*///#define AF		(I.AuxVal!=0)
    /*TODO*///#define OF		(I.OverVal!=0)
    /*TODO*///#define MD		(I.MF!=0)
    /*TODO*////************************************************************************/
    /*TODO*///
    /*TODO*////* drop lines A16-A19 for a 64KB memory (yes, I know this should be done after adding the offset 8-) */
    /*TODO*////* HJB 12/13/98 instead mask address lines with a driver supplied value */
    static final int SegBase(int Seg) {
        return I.sregs[Seg] << 4 & 0xFFFF;
    }
    /*TODO*///
    /*TODO*///#define DefaultBase(Seg) ((I.seg_prefix && (Seg==DS || Seg==SS)) ? I.prefix_base : I.base[Seg])
    /*TODO*///
    /*TODO*////* ASG 971005 -- changed to cpu_readmem20/cpu_writemem20 */
    /*TODO*///#define GetMemB(Seg,Off) (nec_ICount-=6,(BYTE)cpu_readmem20((DefaultBase(Seg)+(Off))))
    /*TODO*///#define GetMemW(Seg,Off) (nec_ICount-=10,(WORD)GetMemB(Seg,Off)+(WORD)(GetMemB(Seg,(Off)+1)<<8))
    /*TODO*///#define PutMemB(Seg,Off,x) { nec_ICount-=7; cpu_writemem20((DefaultBase(Seg)+(Off)),(x)); }
    /*TODO*///#define PutMemW(Seg,Off,x) { nec_ICount-=11; PutMemB(Seg,Off,(BYTE)(x)); PutMemB(Seg,(Off)+1,(BYTE)((x)>>8)); }
    /*TODO*///
    /*TODO*///#define ReadByte(ea) (nec_ICount-=6,(BYTE)cpu_readmem20((ea)))
    /*TODO*///#define ReadWord(ea) (nec_ICount-=10,cpu_readmem20((ea))+(cpu_readmem20(((ea)+1))<<8))
    /*TODO*///#define WriteByte(ea,val) { nec_ICount-=7; cpu_writemem20((ea),val); }
    /*TODO*///#define WriteWord(ea,val) { nec_ICount-=11; cpu_writemem20((ea),(BYTE)(val)); cpu_writemem20(((ea)+1),(val)>>8); }
    /*TODO*///
    /*TODO*///#define read_port(port) cpu_readport(port)
    /*TODO*///#define write_port(port,val) cpu_writeport(port,val)
    /*TODO*///
    /*TODO*////* no need to go through cpu_readmem for these ones... */
    /*TODO*////* ASG 971222 -- PUSH/POP now use the standard mechanisms; opcode reading is the same */

    public static final int FETCH() {
        int i = cpu_readop_arg((I.base[CS] + I.ip)) & 0xFF;//((BYTE)cpu_readop_arg((I.base[CS]+I.ip++)))
        I.ip = I.ip + 1 & 0xFFFF;//neccesary???
        return i;
    }
    /*TODO*///#define FETCHOP ((BYTE)cpu_readop((I.base[CS]+I.ip++)))
    /*TODO*///#define FETCHWORD(var) { var=cpu_readop_arg(((I.base[CS]+I.ip)))+(cpu_readop_arg(((I.base[CS]+I.ip+1)))<<8); I.ip+=2; }
    /*TODO*///#define PUSH(val) { I.regs.w[SP]-=2; WriteWord(((I.base[SS]+I.regs.w[SP])),val); }
    /*TODO*///#define POP(var) { var = ReadWord(((I.base[SS]+I.regs.w[SP]))); I.regs.w[SP]+=2; }
    /*TODO*////************************************************************************/
    /*TODO*///#define CompressFlags() (WORD)(CF | (PF << 2) | (AF << 4) | (ZF << 6) \
    /*TODO*///				| (SF << 7) | (I.TF << 8) | (I.IF << 9) \
    /*TODO*///				| (I.DF << 10) | (OF << 11)| (MD << 15))
    /*TODO*///
    /*TODO*///#define ExpandFlags(f) \
    /*TODO*///{ \
    /*TODO*///	  I.CarryVal = (f) & 1; \
    /*TODO*///	  I.ParityVal = !((f) & 4); \
    /*TODO*///	  I.AuxVal = (f) & 16; \
    /*TODO*///	  I.ZeroVal = !((f) & 64); \
    /*TODO*///	  I.SignVal = (f) & 128 ? -1 : 0; \
    /*TODO*///	  I.TF = ((f) & 256) == 256; \
    /*TODO*///	  I.IF = ((f) & 512) == 512; \
    /*TODO*///	  I.DF = ((f) & 1024) == 1024; \
    /*TODO*///	  I.OverVal = (f) & 2048; \
    /*TODO*///	  I.MF = ((f) & 0x8000) == 0x8000; \
    /*TODO*///}
    /*TODO*/// 
}
