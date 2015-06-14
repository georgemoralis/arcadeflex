package cpu.nec;

import mame.cpuintrfH;
import static mame.cpuintrfH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.nec.necH.*;
import static arcadeflex.libc_old.*;
import static cpu.nec.necmodrmH.*;
import static cpu.nec.necinstrH.*;
import static cpu.nec.neceaH.*;

public class v30 extends cpuintrfH.cpu_interface {

    public static FILE neclog = null;//fopen("neclog.log", "wa");  //for debug purposes

    public v30() {
        cpu_num = CPU_V30;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = NEC_INT_NONE;
        irq_int = -1000;
        nmi_int = NEC_NMI_INT;
        address_shift = 0;
        address_bits = 20;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 5;
        abits1 = ABITS1_20;
        abits2 = ABITS2_20;
        abitsmin = ABITS_MIN_20;
        icount = nec_ICount;
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*TODO*///static UINT16 bytes[] = {
    /*TODO*///	1,2,4,8,16,32,64,128,256,
    /*TODO*///	512,1024,2048,4096,8192,16384,32768,65336
    /*TODO*///};
    /*TODO*///
    /*TODO*////* NEC registers */
    /*TODO*///typedef union
    /*TODO*///{                   /* eight general registers */
    /*TODO*///    UINT16 w[8];    /* viewed as 16 bits registers */
    /*TODO*///    UINT8  b[16];   /* or as 8 bit registers */
    /*TODO*///} necbasicregs;
    /*TODO*///
    public class nec_Regs {

        _regs regs = new _regs();//int[]   mainregs = new int[8];
        int ip;
        /*TODO*///	UINT16	flags;
        /*UINT32*/
        int[] base = new int[4];
        /*UINT16*/
        int[] sregs = new int[4];
        public irqcallbacksPtr irq_callback;
        int AuxVal, OverVal, SignVal, ZeroVal, CarryVal, ParityVal; /* 0 or non-0 valued flags */

        int/*UINT8*/ TF, IF, DF, MF; 	/* 0 or 1 valued flags */	/* OB[19.07.99] added Mode Flag V30 */


        int/*UINT8*/ int_vector;
        int/*UINT8*/ pending_irq;
        int nmi_state;
        int irq_state;
        int/*unsigned*/ prefix_base;	/* base address of the latest prefix segment */

        int /*char*/ seg_prefix;		/* prefix segment indicator */


        class _regs {

            public int[] w = new int[8];
            public int[] b = new int[16];

            public void SetB(int index, int val) {
                b[index] = val;
                w[(index >> 1)] = (b[((index & 0xFFFFFFFE) + 1)] << 8 | b[(index & 0xFFFFFFFE)]);
            }

            public void AddB(int index, int val) {
                b[index] = (b[index] + val & 0xFF);
                w[(index >> 1)] = (b[((index & 0xFFFFFFFE) + 1)] << 8 | b[(index & 0xFFFFFFFE)]);
            }

            public void SetW(int index, int val) {
                w[index] = val;
                index <<= 1;
                b[index] = (val & 0xFF);
                b[(index + 1)] = (val >> 8);
            }
        }
    }

    /**
     * ************************************************************************
     */
    /* cpu state                                                               */
    /**
     * ************************************************************************
     */
    static int[] nec_ICount = new int[1];
    static nec_Regs I;

    /* The interrupt number of a pending external interrupt pending NMI is 2.	*/
    /* For INTR interrupts, the level is caught on the bus during an INTA cycle */
    public static final int INT_IRQ = 0x01;
    public static final int NMI_IRQ = 0x02;

    static /*UINT8*/ int[] parity_table = new int[256];

    /**
     * ************************************************************************
     */
    @Override
    public void reset(Object param) {
        /*unsigned*/ int i, j, c;

        int[] reg_name = {AL, CL, DL, BL, AH, CH, DH, BH};

        I = new nec_Regs();
        I.sregs[CS] = 0xffff;
        I.base[CS] = I.sregs[CS] << 4;
        change_pc20((I.base[CS] + I.ip));

        for (i = 0; i < 256; i++) {
            for (j = i, c = 0; j > 0; j >>= 1) {
                if ((j & 1) != 0) {
                    c++;
                }
            }
            parity_table[i] = ((c & 1) == 0 ? 1 : 0);
        }
        I.ZeroVal = I.ParityVal = 1;
        SetMD(1);						/* set the mode-flag = native mode */

        for (i = 0; i < 256; i++) {
            Mod_RM.reg.b[i] = reg_name[(i & 0x38) >> 3];
            Mod_RM.reg.w[i] = ((i & 0x38) >> 3);
        }

        for (i = 0xc0; i < 0x100; i++) {
            Mod_RM.RM.w[i] = (i & 7);
            Mod_RM.RM.b[i] = reg_name[i & 7];
        }
    }

    @Override
    public void exit() {
        /* nothing to do ? */
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase20.handler(pc, 0);
    }
    /*TODO*///static void nec_interrupt(unsigned int_num,BOOLEAN md_flag)
    /*TODO*///{
    /*TODO*///    unsigned dest_seg, dest_off;
    /*TODO*///
    /*TODO*///#if 0
    /*TODO*///	if (errorlog)
    /*TODO*///		fprintf(errorlog,"PC=%06x : NEC Interrupt %02d",cpu_get_pc(),int_num);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///    i_pushf();
    /*TODO*///	I.TF = I.IF = 0;
    /*TODO*///	if (md_flag) SetMD(0);	/* clear Mode-flag = start 8080 emulation mode */
    /*TODO*///
    /*TODO*///	if (int_num == -1)
    /*TODO*///	{
    /*TODO*///		int_num = (*I.irq_callback)(0);
    /*TODO*/////		if (errorlog) fprintf(errorlog," (indirect ->%02d) ",int_num);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///    dest_off = ReadWord(int_num*4);
    /*TODO*///    dest_seg = ReadWord(int_num*4+2);
    /*TODO*///
    /*TODO*///	PUSH(I.sregs[CS]);
    /*TODO*///	PUSH(I.ip);
    /*TODO*///	I.ip = (WORD)dest_off;
    /*TODO*///	I.sregs[CS] = (WORD)dest_seg;
    /*TODO*///	I.base[CS] = SegBase(CS);
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*/////	if (errorlog)
    /*TODO*/////		fprintf(errorlog,"=%06x\n",cpu_get_pc());
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///void nec_trap(void)
    /*TODO*///{
    /*TODO*///	nec_instruction[FETCHOP]();
    /*TODO*///	nec_interrupt(1,0);
    /*TODO*///}
    /*TODO*///
    /*TODO*///

    public static void external_int() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /*TODO*///{
    /*TODO*///	if( I.pending_irq & NMI_IRQ )
    /*TODO*///	{
    /*TODO*///		nec_interrupt(NEC_NMI_INT,0);
    /*TODO*///		I.pending_irq &= ~NMI_IRQ;
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	if( I.pending_irq )
    /*TODO*///	{
    /*TODO*///		/* the actual vector is retrieved after pushing flags */
    /*TODO*///		/* and clearing the IF */
    /*TODO*///		nec_interrupt(-1,0);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////*                             OPCODES                                      */
    /*TODO*////****************************************************************************/
    /*TODO*///
    /*TODO*///static void i_add_br8(void)    /* Opcode 0x00 - ADD */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ADDB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_add_wr16(void)    /* Opcode 0x01 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ADDW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_add_r8b = new InstructionPtr() { /* Opcode 0x02 */

        public void handler() {
            //DEF_r8b(dst,src);
            int ModRM = FETCHOP();
            int dst = RegByte(ModRM);
            int src = GetRMByte(ModRM);
            nec_ICount[0] -= 3;
            //ADDB(dst,src);
            int res = dst + src;
            SetCFB(res);
            SetOFB_Add(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Byte(res);
            dst = res & 0xFF;
            SetRegByte(ModRM, dst);
            /*if (neclog != null) {
             fprintf(neclog, "i_add_r8b :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };

    static InstructionPtr i_add_r16w = new InstructionPtr() /* Opcode 0x03 */ {
        public void handler() {
            //DEF_r16w(dst, src);
            /*unsigned*/
            int ModRM = FETCHOP();
            /*unsigned*/
            int dst = RegWord(ModRM);
            /*unsigned*/
            int src = GetRMWord(ModRM);
            nec_ICount[0] -= 3;
            //ADDW(dst, src);
            /*unsigned*/
            int res = dst + src;
            SetCFW(res);
            SetOFW_Add(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            dst = res & 0xFFFF;
            SetRegWord(ModRM, dst);
            if (neclog != null) {
                fprintf(neclog, "i_add_r16w :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_add_ald8(void)    /* Opcode 0x04 */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///	nec_ICount-=4;
    /*TODO*///    ADDB(dst,src);
    /*TODO*///	I.regs.b[AL]=dst;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_add_axd16 = new InstructionPtr() {
        public void handler() {
            //DEF_axd16(dst,src);
            /*unsigned*/
            int src = FETCHOP();
            /*unsigned*/
            int dst = I.regs.w[AW];
            src += (FETCH() << 8);
            nec_ICount[0] -= 4;
            //ADDW(dst,src);
            /*unsigned*/
            int res = dst + src;
            SetCFW(res);
            SetOFW_Add(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            dst = res & 0xFFFF;
            I.regs.SetW(AW, dst);
            if (neclog != null) {
                fprintf(neclog, "i_add_axd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_push_es = new InstructionPtr() {
        public void handler() {
            nec_ICount[0] -= 3;
            PUSH(I.sregs[ES]);
            if (neclog != null) {
                fprintf(neclog, "i_push_es :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_pop_es = new InstructionPtr() {
        public void handler() {
            I.sregs[ES] = POP();
            I.base[ES] = SegBase(ES);
            nec_ICount[0] -= 2;
            if (neclog != null) {
                fprintf(neclog, "i_pop_es :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_or_br8(void)    /* Opcode 0x08 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ORB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_or_wr16(void)    /* Opcode 0x09 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ORW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_or_r8b(void)    /* Opcode 0x0a */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ORB(dst,src);
    /*TODO*///    RegByte(ModRM)=dst;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_or_r16w(void)    /* Opcode 0x0b */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    ORW(dst,src);
    /*TODO*///    RegWord(ModRM)=dst;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_or_ald8(void)    /* Opcode 0x0c */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///	nec_ICount-=4;
    /*TODO*///    ORB(dst,src);
    /*TODO*///	I.regs.b[AL]=dst;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_or_axd16(void)    /* Opcode 0x0d */
    /*TODO*///{
    /*TODO*///    DEF_axd16(dst,src);
    /*TODO*///	nec_ICount-=4;
    /*TODO*///    ORW(dst,src);
    /*TODO*///	I.regs.w[AW]=dst;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_push_cs = new InstructionPtr() {
        public void handler() {
            nec_ICount[0] -= 3;
            PUSH(I.sregs[CS]);
            if (neclog != null) {
                fprintf(neclog, "i_push_cs :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_pre_nec(void) /* Opcode 0x0f */
    /*TODO*///{
    /*TODO*///    unsigned Opcode = FETCH;
    /*TODO*///    unsigned ModRM;
    /*TODO*///    unsigned tmp;
    /*TODO*///    unsigned tmp2;
    /*TODO*///
    /*TODO*///	switch (Opcode) {
    /*TODO*///		case 0x10 : // 0F 10 47 30 - TEST1 [bx+30h],cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=3;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-12;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = I.regs.b[CL] & 0x7;
    /*TODO*///			I.ZeroVal = tmp & bytes[tmp2] ? 1 : 0;
    /*TODO*/////			SetZF(tmp & (1<<tmp2));
    /*TODO*///			break;
    /*TODO*///		case 0x11 : // 0F 11 47 30 - TEST1 [bx+30h],cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    //tmp = GetRMWord(ModRM);
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=3;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-12;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = I.regs.b[CL] & 0xF;
    /*TODO*///			I.ZeroVal = tmp & bytes[tmp2] ? 1 : 0;
    /*TODO*/////			SetZF(tmp & (1<<tmp2));
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///
    /*TODO*///		case 0x12 : // 0F 12 [mod:000:r/m] - CLR1 reg/m8,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    /* need the long if due to correct cycles OB[19.07.99] */
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=5;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte( EA);
    /*TODO*///   				nec_ICount=old-14;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = I.regs.b[CL] & 0x7;		/* hey its a Byte so &07 NOT &0f */
    /*TODO*///			tmp &= ~(bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x13 : // 0F 13 [mod:000:r/m] - CLR1 reg/m16,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    //tmp = GetRMWord(ModRM);
    /*TODO*///			if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=5;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-14;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = I.regs.b[CL] & 0xF;		/* this time its a word */
    /*TODO*///			tmp &= ~(bytes[tmp2]);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x14 : // 0F 14 47 30 - SET1 [bx+30h],cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=4;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	 (*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-13;
    /*TODO*///		    }
    /*TODO*///			tmp2 = I.regs.b[CL] & 0x7;
    /*TODO*///			tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x15 : // 0F 15 C6 - SET1 si,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   //tmp = GetRMWord(ModRM);
    /*TODO*///		   if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=4;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-13;
    /*TODO*///			}
    /*TODO*///			tmp2 = I.regs.b[CL] & 0xF;
    /*TODO*///			tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x16 : // 0F 16 C6 - NOT1 si,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    /* need the long if due to correct cycles OB[19.07.99] */
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=4;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-18;			/* my source says 18 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = I.regs.b[CL] & 0x7;	/* hey its a Byte so &07 NOT &0f */
    /*TODO*///			if (tmp & bytes[tmp2])
    /*TODO*///				tmp &= ~(bytes[tmp2]);
    /*TODO*///			else
    /*TODO*///				tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x17 : // 0F 17 C6 - NOT1 si,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    //tmp = GetRMWord(ModRM);
    /*TODO*///			if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=4;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-18;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = I.regs.b[CL] & 0xF;		/* this time its a word */
    /*TODO*///			if (tmp & bytes[tmp2])
    /*TODO*///				tmp &= ~(bytes[tmp2]);
    /*TODO*///			else
    /*TODO*///				tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x18 : // 0F 18 XX - TEST1 [bx+30h],07
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   //tmp = GetRMByte(ModRM);
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=4;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-13;			/* my source says 15 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0xF;
    /*TODO*///			I.ZeroVal = tmp & (bytes[tmp2]) ? 1 : 0;
    /*TODO*/////			SetZF(tmp & (1<<tmp2));
    /*TODO*///			break;
    /*TODO*///		case 0x19 : // 0F 19 XX - TEST1 [bx+30h],07
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMWord(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=4;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-13;			/* my source says 14 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0xf;
    /*TODO*///			I.ZeroVal = tmp & (bytes[tmp2]) ? 1 : 0;
    /*TODO*/////			SetZF(tmp & (1<<tmp2));
    /*TODO*///			break;
    /*TODO*///		case 0x1a : // 0F 1A 06 - CLR1 si,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMByte(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=6;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-15;			/* my source says 15 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0x7;
    /*TODO*///			tmp &= ~(bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x1B : // 0F 1B 06 - CLR1 si,cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMWord(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=6;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-15;			/* my source says 15 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0xF;
    /*TODO*///			tmp &= ~(bytes[tmp2]);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x1C : // 0F 1C 47 30 - SET1 [bx+30h],cl
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   //tmp = GetRMByte(ModRM);
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=5;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-14;			/* my source says 15 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///		    }
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0x7;
    /*TODO*///			tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x1D : // 0F 1D C6 - SET1 si,cl
    /*TODO*///			//if (errorlog) fprintf(errorlog,"PC=%06x : Set1 ",cpu_get_pc()-2);
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=5;
    /*TODO*///				//if (errorlog) fprintf(errorlog,"reg=%04x ->",tmp);
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();	// calculate EA
    /*TODO*///				tmp=ReadWord(EA);	// read from EA
    /*TODO*///				nec_ICount=old-14;
    /*TODO*///				//if (errorlog) fprintf(errorlog,"[%04x]=%04x ->",EA,tmp);
    /*TODO*///			}
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0xF;
    /*TODO*///			tmp |= (bytes[tmp2]);
    /*TODO*///			//if (errorlog) fprintf(errorlog,"%04x",tmp);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x1e : // 0F 1e C6 - NOT1 si,07
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMByte(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=5;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-19;
    /*TODO*///		    }
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0x7;
    /*TODO*///			if (tmp & bytes[tmp2])
    /*TODO*///				tmp &= ~(bytes[tmp2]);
    /*TODO*///			else
    /*TODO*///				tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x1f : // 0F 1f C6 - NOT1 si,07
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMWord(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=5;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-19;			/* my source says 15 cycles everytime and not
    /*TODO*///   											   ModRM-dependent like GetEA[] does..hmmm */
    /*TODO*///			}
    /*TODO*///			tmp2 = FETCH;
    /*TODO*///			tmp2 &= 0xF;
    /*TODO*///			if (tmp & bytes[tmp2])
    /*TODO*///				tmp &= ~(bytes[tmp2]);
    /*TODO*///			else
    /*TODO*///				tmp |= (bytes[tmp2]);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		case 0x20 : { // 0F 20 59 - add4s
    /*TODO*///			int count = (I.regs.b[CL]+1)/2;	// length in words !
    /*TODO*///			int i;
    /*TODO*///	      	unsigned di = I.regs.w[IY];
    /*TODO*///			unsigned si = I.regs.w[IX];
    /*TODO*///			I.ZeroVal = 0;
    /*TODO*///			I.CarryVal = 0; // NOT ADC
    /*TODO*///			for (i=0;i<count;i++) {
    /*TODO*///				int v1,v2;
    /*TODO*///				int result;
    /*TODO*///				tmp = GetMemB(DS, si);
    /*TODO*///				tmp2 = GetMemB(ES, di);
    /*TODO*///
    /*TODO*///				v1 = (tmp>>4)*10 + (tmp&0xf);
    /*TODO*///				v2 = (tmp2>>4)*10 + (tmp2&0xf);
    /*TODO*///				result = v1+v2+I.CarryVal;
    /*TODO*///				I.CarryVal = result > 99 ? 1 : 0;
    /*TODO*///				result = result % 100;
    /*TODO*///				v1 = ((result/10)<<4) | (result % 10);
    /*TODO*///				PutMemB(ES, di,v1)
    /*TODO*///				if (v1) I.ZeroVal = 1;
    /*TODO*///				si++;
    /*TODO*///				di++;
    /*TODO*///			}
    /*TODO*///			I.OverVal = I.CarryVal;
    /*TODO*///			nec_ICount-=7+19*count;	// 7+19n, n #operand words
    /*TODO*///			} break;
    /*TODO*///
    /*TODO*///		case 0x22 : { // 0F 22 59 - sub4s
    /*TODO*///			int count = (I.regs.b[CL]+1)/2;
    /*TODO*///			int i;
    /*TODO*///	      unsigned di = I.regs.w[IY];
    /*TODO*///			unsigned si = I.regs.w[IX];
    /*TODO*///			I.ZeroVal = 0;
    /*TODO*///			I.CarryVal = 0; // NOT ADC
    /*TODO*///			for (i=0;i<count;i++) {
    /*TODO*///				int v1,v2;
    /*TODO*///				int result;
    /*TODO*///				tmp = GetMemB(ES, di);
    /*TODO*///				tmp2 = GetMemB(DS, si);
    /*TODO*///
    /*TODO*///				v1 = (tmp>>4)*10 + (tmp&0xf);
    /*TODO*///				v2 = (tmp2>>4)*10 + (tmp2&0xf);
    /*TODO*///				if (v1 < (v2+I.CarryVal)) {
    /*TODO*///					v1+=100;
    /*TODO*///					result = v1-(v2+I.CarryVal);
    /*TODO*///					I.CarryVal = 1;
    /*TODO*///				} else {
    /*TODO*///					result = v1-(v2+I.CarryVal);
    /*TODO*///					I.CarryVal = 0;
    /*TODO*///				}
    /*TODO*///				v1 = ((result/10)<<4) | (result % 10);
    /*TODO*///				PutMemB(ES, di,v1)
    /*TODO*///				if (v1) I.ZeroVal = 1;
    /*TODO*///				si++;
    /*TODO*///				di++;
    /*TODO*///			}
    /*TODO*///			I.OverVal = I.CarryVal;
    /*TODO*///			nec_ICount-=7+19*count;
    /*TODO*///			} break;
    /*TODO*///
    /*TODO*///		case 0x25 :
    /*TODO*///			/*
    /*TODO*///			----------O-MOVSPA---------------------------------
    /*TODO*///			OPCODE MOVSPA	 -  Move Stack Pointer After Bank Switched
    /*TODO*///
    /*TODO*///			CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  MOVSPA
    /*TODO*///
    /*TODO*///			Description:  This instruction transfer	 both SS and SP	 of the old register
    /*TODO*///				      bank to new register bank after the bank has been switched by
    /*TODO*///				      interrupt or BRKCS instruction.
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	MOVSPA
    /*TODO*///			COP (Code of Operation)	 : 0Fh 25h
    /*TODO*///
    /*TODO*///			Clocks:	 16
    /*TODO*///			*/
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : MOVSPA\n",cpu_get_pc()-2);
    /*TODO*///			nec_ICount-=16;
    /*TODO*///			break;
    /*TODO*///		case 0x26 : { // 0F 22 59 - cmp4s
    /*TODO*///			int count = (I.regs.b[CL]+1)/2;
    /*TODO*///			int i;
    /*TODO*///	        unsigned di = I.regs.w[IY];
    /*TODO*///			unsigned si = I.regs.w[IX];
    /*TODO*///			I.ZeroVal = 0;
    /*TODO*///			I.CarryVal = 0; // NOT ADC
    /*TODO*///			for (i=0;i<count;i++) {
    /*TODO*///				int v1,v2;
    /*TODO*///				int result;
    /*TODO*///				tmp = GetMemB(ES, di);
    /*TODO*///				tmp2 = GetMemB(DS, si);
    /*TODO*///
    /*TODO*///				v1 = (tmp>>4)*10 + (tmp&0xf);
    /*TODO*///				v2 = (tmp2>>4)*10 + (tmp2&0xf);
    /*TODO*///				if (v1 < (v2+I.CarryVal)) {
    /*TODO*///					v1+=100;
    /*TODO*///					result = v1-(v2+I.CarryVal);
    /*TODO*///					I.CarryVal = 1;
    /*TODO*///				} else {
    /*TODO*///					result = v1-(v2+I.CarryVal);
    /*TODO*///					I.CarryVal = 0;
    /*TODO*///				}
    /*TODO*///				v1 = ((result/10)<<4) | (result % 10);
    /*TODO*/////				PutMemB(ES, di,v1)	/* no store, only compare */
    /*TODO*///				if (v1) I.ZeroVal = 1;
    /*TODO*///				si++;
    /*TODO*///				di++;
    /*TODO*///			}
    /*TODO*///			I.OverVal = I.CarryVal;
    /*TODO*///			nec_ICount-=7+19*count;	// 7+19n, n #operand bytes
    /*TODO*///			} break;
    /*TODO*///		case 0x28 : // 0F 28 C7 - ROL4 bh
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMByte(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=25;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-28;
    /*TODO*///   			}
    /*TODO*///			tmp <<= 4;
    /*TODO*///			tmp |= I.regs.b[AL] & 0xF;
    /*TODO*///			I.regs.b[AL] = (I.regs.b[AL] & 0xF0) | ((tmp>>8)&0xF);
    /*TODO*///			tmp &= 0xff;
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///		// Is this a REAL instruction??
    /*TODO*///		case 0x29 : // 0F 29 C7 - ROL4 bx
    /*TODO*///
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		    /*
    /*TODO*///		    if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=29;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-33;
    /*TODO*///			}
    /*TODO*///			tmp <<= 4;
    /*TODO*///			tmp |= I.regs.b[AL] & 0xF;
    /*TODO*///			I.regs.b[AL] = (I.regs.b[AL] & 0xF0) | ((tmp>>8)&0xF);
    /*TODO*///			tmp &= 0xffff;
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			*/
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : ROL4 %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x2A : // 0F 2a c2 - ROR4 bh
    /*TODO*///			ModRM = FETCH;
    /*TODO*///		   	//tmp = GetRMByte(ModRM);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		    	nec_ICount-=29;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///   				nec_ICount=old-33;
    /*TODO*///   			}
    /*TODO*///			tmp2 = (I.regs.b[AL] & 0xF)<<4;
    /*TODO*///			I.regs.b[AL] = (I.regs.b[AL] & 0xF0) | (tmp&0xF);
    /*TODO*///			tmp = tmp2 | (tmp>>4);
    /*TODO*///			PutbackRMByte(ModRM,tmp);
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x2B : // 0F 2b c2 - ROR4 bx
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			/*
    /*TODO*///			//tmp = GetRMWord(ModRM);
    /*TODO*///			if (ModRM >= 0xc0) {
    /*TODO*///				tmp=I.regs.w[Mod_RM.RM.w[ModRM]];
    /*TODO*///				nec_ICount-=29;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				int old=nec_ICount;
    /*TODO*///				(*GetEA[ModRM])();;
    /*TODO*///				tmp=ReadWord(EA);
    /*TODO*///				nec_ICount=old-33;
    /*TODO*///			}
    /*TODO*///			tmp2 = (I.regs.b[AL] & 0xF)<<4;
    /*TODO*///			I.regs.b[AL] = (I.regs.b[AL] & 0xF0) | (tmp&0xF);
    /*TODO*///
    /*TODO*///			tmp = tmp2 | (tmp>>4);
    /*TODO*///			PutbackRMWord(ModRM,tmp);
    /*TODO*///			*/
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : ROR4 %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			break;
    /*TODO*///		case 0x2D : // 0Fh 2Dh <1111 1RRR>
    /*TODO*///			/* OPCODE BRKCS  -	 Break with Contex Switch
    /*TODO*///			   CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			   Description:
    /*TODO*///
    /*TODO*///				Perform a High-Speed Software Interrupt with contex-switch to
    /*TODO*///				register bank indicated by the lower 3-bits of 'bank'.
    /*TODO*///
    /*TODO*///			Info:	NEC V25/V35/V25 Plus/V35 Plus Bank System
    /*TODO*///
    /*TODO*///				This Chips have	 8 32bytes register banks, which placed in
    /*TODO*///				Internal chip RAM by addresses:
    /*TODO*///				xxE00h..xxE1Fh Bank 0
    /*TODO*///				xxE20h..xxE3Fh Bank 1
    /*TODO*///				   .........
    /*TODO*///				xxEC0h..xxEDFh Bank 6
    /*TODO*///				xxEE0h..xxEFFh Bank 7
    /*TODO*///				xxF00h..xxFFFh Special Functions Register
    /*TODO*///				Where xx is Value of IDB register.
    /*TODO*///				IBD is Byte Register contained Internal data area base
    /*TODO*///				IBD addresses is FFFFFh and xxFFFh where xx is data in IBD.
    /*TODO*///
    /*TODO*///				Format of Bank:
    /*TODO*///				+0	Reserved
    /*TODO*///				+2	Vector PC
    /*TODO*///				+4	Save   PSW
    /*TODO*///				+6	Save   PC
    /*TODO*///				+8	DS0		;DS
    /*TODO*///				+A	SS		;SS
    /*TODO*///				+C	PS		;CS
    /*TODO*///				+E	DS1		;ES
    /*TODO*///				+10	IY		;IY
    /*TODO*///				+11	IX		;IX
    /*TODO*///				+14	BP		;BP
    /*TODO*///				+16	SP		;SP
    /*TODO*///				+18	BW		;BW
    /*TODO*///				+1A	DW		;DW
    /*TODO*///				+1C	CW		;CW
    /*TODO*///				+1E	AW		;AW
    /*TODO*///
    /*TODO*///				Format of V25 etc. PSW (FLAGS):
    /*TODO*///				Bit	Description
    /*TODO*///				15	1
    /*TODO*///				14	RB2 \
    /*TODO*///				13	RB1  >	Current Bank Number
    /*TODO*///				12	RB0 /
    /*TODO*///				11	V	;OF
    /*TODO*///				10	IYR	;DF
    /*TODO*///				9	IE	;IF
    /*TODO*///				8	BRK	;TF
    /*TODO*///				7	S	;SF
    /*TODO*///				6	Z	;ZF
    /*TODO*///				5	F1	General Purpose user flag #1
    /*TODO*///						(accessed by Flag Special Function Register)
    /*TODO*///				4	AC	;AF
    /*TODO*///				3	F0	General purpose user flag #0
    /*TODO*///						(accessed by Flag Special Function Register)
    /*TODO*///				2	P	;PF
    /*TODO*///				1	BRKI	I/O Trap Enable Flag
    /*TODO*///				0	CY	;CF
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///			*/
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : BRKCS %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_ICount-=15;// checked !
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x31: // 0F 31 [mod:reg:r/m] - INS reg8,reg8 or INS reg8,imm4
    /*TODO*///
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : INS ",cpu_get_pc()-2);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		        if (errorlog) fprintf(errorlog,"ModRM=%04x \n",ModRM);
    /*TODO*///		    	nec_ICount-=29;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///		    	if (errorlog) fprintf(errorlog,"ModRM=%04x  Byte=%04x\n",EA,tmp);
    /*TODO*///   				nec_ICount=old-33;
    /*TODO*///   			}
    /*TODO*///
    /*TODO*///			// more to come
    /*TODO*///			//bfl=tmp2 & 0xf;		// bit field length
    /*TODO*///			//bfs=tmp & 0xf;		// bit field start (bit offset in DS:IX)
    /*TODO*///			//I.regs.b[AH] =0;	// AH =0
    /*TODO*///
    /*TODO*///			/*2do: the rest is silence....yet
    /*TODO*///			----------O-INS------------------------------------
    /*TODO*///			OPCODE INS  -  Insert Bit String
    /*TODO*///
    /*TODO*///			CPU: NEC/Sony  all V-series
    /*TODO*///			Type of Instruction: User
    /*TODO*///
    /*TODO*///			Instruction:  INS  start,len
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///
    /*TODO*///				  BitField [	     BASE =  ES:IY
    /*TODO*///					 START BIT OFFSET =  start
    /*TODO*///						   LENGTH =  len
    /*TODO*///						 ]   <-	 AW [ bits= (len-1)..0]
    /*TODO*///
    /*TODO*///			Note:	di and start automatically UPDATE
    /*TODO*///			Note:	Alternative Name of this instruction is NECINS
    /*TODO*///
    /*TODO*///			Flags Affected: None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form		 : INS	reg8,reg8
    /*TODO*///			COP (Code of Operation)	 : 0FH 31H  PostByte
    /*TODO*///			*/
    /*TODO*///
    /*TODO*///			//nec_ICount-=31; /* 31 -117 clocks ....*/
    /*TODO*///			break;
    /*TODO*///		case 0x33: // 0F 33 [mod:reg:r/m] - EXT reg8,reg8 or EXT reg8,imm4
    /*TODO*///
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : EXT ",cpu_get_pc()-2);
    /*TODO*///		   	if (ModRM >= 0xc0) {
    /*TODO*///		    	tmp=I.regs.b[Mod_RM.RM.b[ModRM]];
    /*TODO*///		        if (errorlog) fprintf(errorlog,"ModRM=%04x \n",ModRM);
    /*TODO*///		    	nec_ICount-=29;
    /*TODO*///		    }
    /*TODO*///		    else {
    /*TODO*///		    	int old=nec_ICount;
    /*TODO*///		    	(*GetEA[ModRM])();;
    /*TODO*///		    	tmp=ReadByte(EA);
    /*TODO*///		    	if (errorlog) fprintf(errorlog,"ModRM=%04x  Byte=%04x\n",EA,tmp);
    /*TODO*///   				nec_ICount=old-33;
    /*TODO*///   			}
    /*TODO*///			/*2do: the rest is silence....yet */
    /*TODO*///			//bfl=tmp2 & 0xf;		// bit field length
    /*TODO*///			//bfs=tmp & 0xf;		// bit field start (bit offset in DS:IX)
    /*TODO*///			//I.regs.b[AH] =0;	// AH =0
    /*TODO*///
    /*TODO*///			/*
    /*TODO*///
    /*TODO*///			----------O-EXT------------------------------------
    /*TODO*///			OPCODE EXT  -  Extract Bit Field
    /*TODO*///
    /*TODO*///			CPU: NEC/Sony all  V-series
    /*TODO*///			Type of Instruction: User
    /*TODO*///
    /*TODO*///			Instruction:  EXT  start,len
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///
    /*TODO*///				  AW <- BitField [
    /*TODO*///						     BASE =  DS:IX
    /*TODO*///					 START BIT OFFSET =  start
    /*TODO*///						   LENGTH =  len
    /*TODO*///						 ];
    /*TODO*///
    /*TODO*///			Note:	si and start automatically UPDATE
    /*TODO*///
    /*TODO*///			Flags Affected: None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form		 : EXT	reg8,reg8
    /*TODO*///			COP (Code of Operation)	 : 0FH 33H  PostByte
    /*TODO*///
    /*TODO*///			Clocks:		EXT  reg8,reg8
    /*TODO*///			NEC V20:	26-55
    /*TODO*///			*/
    /*TODO*///
    /*TODO*///			//NEC_ICount-=26; /* 26 -55 clocks ....*/
    /*TODO*///			break;
    /*TODO*///		case 0x91:
    /*TODO*///			/*
    /*TODO*///			----------O-RETRBI---------------------------------
    /*TODO*///			OPCODE RETRBI	 -  Return from Register Bank Context
    /*TODO*///				     Switch  Interrupt.
    /*TODO*///
    /*TODO*///			CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  RETRBI
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///
    /*TODO*///				PC  <- Save PC;
    /*TODO*///				PSW <- Save PSW;
    /*TODO*///
    /*TODO*///			Flags Affected:	 All
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	RETRBI
    /*TODO*///			COP (Code of Operation)	 : 0Fh 91h
    /*TODO*///
    /*TODO*///			Clocks:	 12
    /*TODO*///			*/
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : RETRBI\n",cpu_get_pc()-2);
    /*TODO*///			nec_ICount-=12;
    /*TODO*///			break;
    /*TODO*///
    /*TODO*///		case 0x94:
    /*TODO*///			/*
    /*TODO*///			----------O-TSKSW----------------------------------
    /*TODO*///			OPCODE TSKSW  -	  Task Switch
    /*TODO*///
    /*TODO*///			CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  TSKSW   reg16
    /*TODO*///
    /*TODO*///			Description:  Perform a High-Speed task switch to the register bank indicated
    /*TODO*///				      by lower 3 bits of reg16. The PC and PSW are saved in the old
    /*TODO*///				      banks. PC and PSW save Registers and the new PC and PSW values
    /*TODO*///				      are retrived from the new register bank's save area.
    /*TODO*///
    /*TODO*///			Note:	     See BRKCS instruction for more Info about banks.
    /*TODO*///
    /*TODO*///			Flags Affected:	 All
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	TSCSW reg16
    /*TODO*///			COP (Code of Operation)	 : 0Fh 94h <1111 1RRR>
    /*TODO*///
    /*TODO*///			Clocks:	 11
    /*TODO*///			*/
    /*TODO*///			ModRM = FETCH;
    /*TODO*///
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : TSCSW %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_ICount-=11;
    /*TODO*///			break;
    /*TODO*///		case 0x95:
    /*TODO*///			/*
    /*TODO*///			----------O-MOVSPB---------------------------------
    /*TODO*///			OPCODE MOVSPB	 -  Move Stack Pointer Before Bamk Switching
    /*TODO*///
    /*TODO*///			CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  MOVSPB  Number_of_bank
    /*TODO*///
    /*TODO*///			Description:  The MOVSPB instruction transfers the current SP and SS before
    /*TODO*///				      the bank switching to new register bank.
    /*TODO*///
    /*TODO*///			Note:	      New Register Bank Number indicated by lower 3bit of Number_of_
    /*TODO*///				      _bank.
    /*TODO*///
    /*TODO*///			Note:	      See BRKCS instruction for more info about banks.
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	MOVSPB	  reg16
    /*TODO*///			COP (Code of Operation)	 : 0Fh 95h <1111 1RRR>
    /*TODO*///
    /*TODO*///			Clocks:	 11
    /*TODO*///			*/
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : MOVSPB %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_ICount-=11;
    /*TODO*///			break;
    /*TODO*///		case 0xbe:
    /*TODO*///			/*
    /*TODO*///			----------O-STOP-----------------------------------
    /*TODO*///			OPCODE STOP    -  Stop CPU
    /*TODO*///
    /*TODO*///			CPU:  NEC V25,V35,V25 Plus,V35 Plus,V25 Software Guard
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  STOP
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///					PowerDown instruction, Stop Oscillator,
    /*TODO*///					Halt CPU.
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	STOP
    /*TODO*///			COP (Code of Operation)	 : 0Fh BEh
    /*TODO*///
    /*TODO*///			Clocks:	 N/A
    /*TODO*///			*/
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : STOP\n",cpu_get_pc()-2);
    /*TODO*///			nec_ICount-=2; /* of course this is crap */
    /*TODO*///			break;
    /*TODO*///		case 0xe0:
    /*TODO*///			/*
    /*TODO*///			----------O-BRKXA----------------------------------
    /*TODO*///			OPCODE BRKXA   -  Break to Expansion Address
    /*TODO*///
    /*TODO*///			CPU:  NEC V33/V53  only
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  BRKXA int_vector
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///				     [sp-1,sp-2] <- PSW		; PSW EQU FLAGS
    /*TODO*///				     [sp-3,sp-4] <- PS		; PS  EQU CS
    /*TODO*///				     [sp-5,sp-6] <- PC		; PC  EQU IP
    /*TODO*///				     SP	 <-  SP -6
    /*TODO*///				     IE	 <-  0
    /*TODO*///				     BRK <-  0
    /*TODO*///				     MD	 <-  0
    /*TODO*///				     PC	 <- [int_vector*4 +0,+1]
    /*TODO*///				     PS	 <- [int_vector*4 +2,+3]
    /*TODO*///				     Enter Expansion Address Mode.
    /*TODO*///
    /*TODO*///			Note:	In NEC V53 Memory Space dividing into 1024 16K pages.
    /*TODO*///				The programming model is Same as in Normal mode.
    /*TODO*///
    /*TODO*///				Mechanism is:
    /*TODO*///				20 bit Logical Address:	 19..14 Page Num  13..0 Offset
    /*TODO*///
    /*TODO*///				page Num convertin by internal table to 23..14 Page Base
    /*TODO*///				tHE pHYIXCAL ADDRESS is both Base and Offset.
    /*TODO*///
    /*TODO*///				Address Expansion Registers:
    /*TODO*///				logical Address A19..A14	I/O Address
    /*TODO*///				0				FF00h
    /*TODO*///				1				FF02h
    /*TODO*///				...				...
    /*TODO*///				63				FF7Eh
    /*TODO*///
    /*TODO*///				Register XAM aliased with port # FF80h indicated current mode
    /*TODO*///				of operation.
    /*TODO*///				Format of XAM register (READ ONLY):
    /*TODO*///				15..1	reserved
    /*TODO*///				0	XA Flag, if=1 then in XA mode.
    /*TODO*///
    /*TODO*///			Format	of  V53 PSW:
    /*TODO*///				15..12	1
    /*TODO*///				11	V
    /*TODO*///				10	IYR
    /*TODO*///				9	IE
    /*TODO*///				8	BRK
    /*TODO*///				7	S
    /*TODO*///				6	Z
    /*TODO*///				5	0
    /*TODO*///				4	AC
    /*TODO*///				3	0
    /*TODO*///				2	P
    /*TODO*///				1	1
    /*TODO*///				0	CY
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	BRKXA  imm8
    /*TODO*///			COP (Code of Operation)	 : 0Fh E0h imm8
    /*TODO*///			*/
    /*TODO*///
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : BRKXA %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_ICount-=12;
    /*TODO*///			break;
    /*TODO*///		case 0xf0:
    /*TODO*///			/*
    /*TODO*///			----------O-RETXA----------------------------------
    /*TODO*///			OPCODE RETXA   -  Return from  Expansion Address
    /*TODO*///
    /*TODO*///			CPU:  NEC V33/V53 only
    /*TODO*///			Type of Instruction: System
    /*TODO*///
    /*TODO*///			Instruction:  RETXA int_vector
    /*TODO*///
    /*TODO*///			Description:
    /*TODO*///				     [sp-1,sp-2] <- PSW		; PSW EQU FLAGS
    /*TODO*///				     [sp-3,sp-4] <- PS		; PS  EQU CS
    /*TODO*///				     [sp-5,sp-6] <- PC		; PC  EQU IP
    /*TODO*///				     SP	 <-  SP -6
    /*TODO*///				     IE	 <-  0
    /*TODO*///				     BRK <-  0
    /*TODO*///				     MD	 <-  0
    /*TODO*///				     PC	 <- [int_vector*4 +0,+1]
    /*TODO*///				     PS	 <- [int_vector*4 +2,+3]
    /*TODO*///				     Disable EA mode.
    /*TODO*///
    /*TODO*///			Flags Affected:	 None
    /*TODO*///
    /*TODO*///			CPU mode: RM
    /*TODO*///
    /*TODO*///			+++++++++++++++++++++++
    /*TODO*///			Physical Form:	RETXA  imm8
    /*TODO*///			COP (Code of Operation)	 : 0Fh F0h imm8
    /*TODO*///
    /*TODO*///			Clocks:	 12
    /*TODO*///			*/
    /*TODO*///			ModRM = FETCH;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : RETXA %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_ICount-=12;
    /*TODO*///			break;
    /*TODO*///		case 0xff: /* 0F ff imm8 - BRKEM */
    /*TODO*///			/*
    /*TODO*///			OPCODE BRKEM  -	 Break for Emulation
    /*TODO*///
    /*TODO*///			CPU: NEC/Sony V20/V30/V40/V50
    /*TODO*///			Description:
    /*TODO*///
    /*TODO*///					PUSH	FLAGS
    /*TODO*///					PUSH	CS
    /*TODO*///					PUSH	IP
    /*TODO*///					MOV	CS,0:[intnum*4+2]
    /*TODO*///					MOV	IP,0:[intnum*4]
    /*TODO*///					MD <- 0;	// Enable 8080 emulation
    /*TODO*///
    /*TODO*///			Note:	BRKEM instruction do software interrupt and then New CS,IP loaded
    /*TODO*///				it switch to 8080 mode i.e. CPU will execute 8080 code.
    /*TODO*///				Mapping Table of Registers in 8080 Mode
    /*TODO*///				8080 Md.   A  B	 C  D  E  H  L	SP PC  F
    /*TODO*///				native.	   AL CH CL DH DL BH BL BP IP  FLAGS(low)
    /*TODO*///				For Return of 8080 mode use CALLN instruction.
    /*TODO*///			Note:	I.e. 8080 addressing only 64KB then "Real Address" is CS*16+PC
    /*TODO*///
    /*TODO*///			Flags Affected: MD
    /*TODO*///			*/
    /*TODO*///			ModRM=FETCH;
    /*TODO*///			nec_ICount-=38;
    /*TODO*///			if (errorlog) fprintf(errorlog,"PC=%06x : BRKEM %02x\n",cpu_get_pc()-3,ModRM);
    /*TODO*///			nec_interrupt(ModRM,1);
    /*TODO*///			break;
    /*TODO*///		default :
    /*TODO*///			break;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_adc_br8(void)    /* Opcode 0x10 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    ADDB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_adc_wr16(void)    /* Opcode 0x11 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    ADDW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_adc_r8b(void)    /* Opcode 0x12 */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    ADDB(dst,src);
    /*TODO*///    RegByte(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:11;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_adc_r16w(void)    /* Opcode 0x13 */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///    src+=CF;
    /*TODO*///    ADDW(dst,src);
    /*TODO*///    RegWord(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_adc_ald8(void)    /* Opcode 0x14 */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    ADDB(dst,src);
    /*TODO*///	I.regs.b[AL] = dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_adc_axd16(void)    /* Opcode 0x15 */
    /*TODO*///{
    /*TODO*///    DEF_axd16(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    ADDW(dst,src);
    /*TODO*///	I.regs.w[AW]=dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_push_ss(void)    	/* Opcode 0x16 */
    /*TODO*///{
    /*TODO*///	PUSH(I.sregs[SS]);
    /*TODO*///	nec_ICount-=10;			   	/* OPCODE.LST says 8-12...so 10 */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_pop_ss(void)    	/* Opcode 0x17 */
    /*TODO*///{
    /*TODO*///	POP(I.sregs[SS]);
    /*TODO*///	I.base[SS] = SegBase(SS);
    /*TODO*///	nec_instruction[FETCHOP](); 	/* no interrupt before next instruction */
    /*TODO*///	nec_ICount-=10;				/* OPCODE.LST says 8-12...so 10 */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_br8(void)    /* Opcode 0x18 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_wr16(void)    /* Opcode 0x19 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_r8b(void)    /* Opcode 0x1a */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBB(dst,src);
    /*TODO*///    RegByte(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:11;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_r16w(void)    /* Opcode 0x1b */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBW(dst,src);
    /*TODO*///    RegWord(ModRM)= dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_ald8(void)    /* Opcode 0x1c */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBB(dst,src);
    /*TODO*///	I.regs.b[AL] = dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sbb_axd16(void)    /* Opcode 0x1d */
    /*TODO*///{
    /*TODO*///    DEF_axd16(dst,src);
    /*TODO*///    src+=CF;
    /*TODO*///    SUBW(dst,src);
    /*TODO*///	I.regs.w[AW]=dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_push_ds = new InstructionPtr() {
        public void handler() {
            PUSH(I.sregs[DS]);
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_push_ds :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_pop_ds = new InstructionPtr() {
        public void handler() {
            I.sregs[DS] = POP();
            I.base[DS] = SegBase(DS);
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_pop_ds :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_and_br8(void)    /* Opcode 0x20 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///    ANDB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_and_wr16(void)    /* Opcode 0x21 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    ANDW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_and_r8b(void)    /* Opcode 0x22 */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///    ANDB(dst,src);
    /*TODO*///    RegByte(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:11;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_and_r16w(void)    /* Opcode 0x23 */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///    ANDW(dst,src);
    /*TODO*///    RegWord(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_and_ald8 = new InstructionPtr() {
        public void handler() {
            //DEF_ald8(dst,src);
            int src = FETCHOP();
            int dst = I.regs.b[AL];
            //ANDB(dst, src);
            dst &= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Byte(dst);
            I.regs.SetB(AL, dst & 0xFF);
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_and_ald8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_and_axd16 = new InstructionPtr() /* Opcode 0x25 */ {
        public void handler() {
            //DEF_axd16(dst, src);
            /*unsigned*/
            int src = FETCHOP();
            /*unsigned*/
            int dst = I.regs.w[AW];
            src += (FETCH() << 8);
            //ANDW(dst, src);
            dst &= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Word(dst);
            I.regs.SetW(AW, dst);
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_and_axd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_es = new InstructionPtr() {
        public void handler() {
            I.seg_prefix = 1;
            I.prefix_base = I.base[ES];
            nec_ICount[0] -= 2;
            if (neclog != null) {
                fprintf(neclog, "i_es :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
            nec_instruction[FETCHOP()].handler();
        }
    };

    /*TODO*///static void i_daa(void)    /* Opcode 0x27 */
    /*TODO*///{
    /*TODO*///	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    /*TODO*///	{
    /*TODO*///		int tmp;
    /*TODO*///		I.regs.b[AL] = tmp = I.regs.b[AL] + 6;
    /*TODO*///		I.AuxVal = 1;
    /*TODO*///		I.CarryVal |= tmp & 0x100;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (CF || (I.regs.b[AL] > 0x9f))
    /*TODO*///	{
    /*TODO*///		I.regs.b[AL] += 0x60;
    /*TODO*///		I.CarryVal = 1;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	SetSZPF_Byte(I.regs.b[AL]);
    /*TODO*///	nec_ICount-=3;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_sub_br8(void)    /* Opcode 0x28 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///	SUBB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sub_wr16(void)    /* Opcode 0x29 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    SUBW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sub_r8b(void)    /* Opcode 0x2a */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///    SUBB(dst,src);
    /*TODO*///    RegByte(ModRM)=dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:11;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_sub_r16w = new InstructionPtr() /* Opcode 0x2b */ {
        public void handler() {
            //    DEF_r16w(dst,src);
            /*unsigned*/
            int ModRM = FETCHOP();
            /*unsigned*/
            int dst = RegWord(ModRM);
            /*unsigned*/
            int src = GetRMWord(ModRM);
            //SUBW(dst,src);
            /*unsigned*/
            int res = dst - src;
            SetCFW(res);
            SetOFW_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            dst = res & 0xFFFF;
            SetRegWord(ModRM, dst);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_sub_r16w :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///
    /*TODO*///static void i_sub_ald8(void)    /* Opcode 0x2c */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    SUBB(dst,src);
    /*TODO*///	I.regs.b[AL] = dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_sub_axd16 = new InstructionPtr() {
        public void handler() {
            //DEF_axd16(dst,src);
            int src = FETCHOP();
            int dst = I.regs.w[AW];
            src += (FETCH() << 8);
            //SUBW(dst,src);
            int res = dst - src;
            SetCFW(res);
            SetOFW_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            dst = res & 0xFFFF;
            I.regs.SetW(AW, dst);
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_sub_axd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_sub_axd16(void)    /* Opcode 0x2d */
    /*TODO*///{
    /*TODO*///    
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cs(void)    /* Opcode 0x2e */
    /*TODO*///{
    /*TODO*///    I.seg_prefix=TRUE;
    /*TODO*///	I.prefix_base=I.base[CS];
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	nec_instruction[FETCHOP]();
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_das(void)    /* Opcode 0x2f */
    /*TODO*///{
    /*TODO*///	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    /*TODO*///	{
    /*TODO*///		int tmp;
    /*TODO*///		I.regs.b[AL] = tmp = I.regs.b[AL] - 6;
    /*TODO*///		I.AuxVal = 1;
    /*TODO*///		I.CarryVal |= tmp & 0x100;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if (CF || (I.regs.b[AL] > 0x9f))
    /*TODO*///	{
    /*TODO*///		I.regs.b[AL] -= 0x60;
    /*TODO*///		I.CarryVal = 1;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	SetSZPF_Byte(I.regs.b[AL]);
    /*TODO*///	nec_ICount-=7;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xor_br8(void)    /* Opcode 0x30 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///    XORB(dst,src);
    /*TODO*///    PutbackRMByte(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:15;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xor_wr16(void)    /* Opcode 0x31 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    XORW(dst,src);
    /*TODO*///    PutbackRMWord(ModRM,dst);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_xor_r8b = new InstructionPtr() /* Opcode 0x32 */ {
        public void handler() {
            //DEF_r8b(dst,src);
            /*unsigned*/
            int ModRM = FETCHOP();
            /*unsigned*/
            int dst = RegByte(ModRM);
            /*unsigned*/
            int src = GetRMByte(ModRM);
            //XORB(dst,src);
            dst ^= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Byte(dst);
            SetRegByte(ModRM, dst & 0xFF);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 11;
            if (neclog != null) {
                fprintf(neclog, "i_xor_r8b :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_xor_r16w = new InstructionPtr() /* Opcode 0x33 */ {
        public void handler() {
            //DEF_r16w(dst, src);
            /*unsigned*/
            int ModRM = FETCHOP();
            /*unsigned*/
            int dst = RegWord(ModRM);
            /*unsigned*/
            int src = GetRMWord(ModRM);
            //XORW(dst, src);
            dst ^= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Word(dst);
            SetRegWord(ModRM, dst);//RegWord(ModRM) = dst;
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_xor_r16w :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_xor_ald8(void)    /* Opcode 0x34 */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    XORB(dst,src);
    /*TODO*///	I.regs.b[AL] = dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xor_axd16(void)    /* Opcode 0x35 */
    /*TODO*///{
    /*TODO*///    DEF_axd16(dst,src);
    /*TODO*///    XORW(dst,src);
    /*TODO*///	I.regs.w[AW]=dst;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_ss = new InstructionPtr() {
        public void handler() {
            I.seg_prefix = 1;
            I.prefix_base = I.base[SS];
            nec_ICount[0] -= 2;
            if (neclog != null) {
                fprintf(neclog, "i_ss :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
            nec_instruction[FETCHOP()].handler();
        }
    };

    /*TODO*///static void i_ss(void)    /* Opcode 0x36 */
    /*TODO*///{
    /*TODO*///    
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_aaa(void)    /* Opcode 0x37 */
    /*TODO*///{
    /*TODO*///	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    /*TODO*///    {
    /*TODO*///		I.regs.b[AL] += 6;
    /*TODO*///		I.regs.b[AH] += 1;
    /*TODO*///		I.AuxVal = 1;
    /*TODO*///		I.CarryVal = 1;
    /*TODO*///    }
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		I.AuxVal = 0;
    /*TODO*///		I.CarryVal = 0;
    /*TODO*///    }
    /*TODO*///	I.regs.b[AL] &= 0x0F;
    /*TODO*///	nec_ICount-=3;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_cmp_br8 = new InstructionPtr() {
        public void handler() {
            //DEF_br8(dst,src);
            int ModRM = FETCHOP();
            int src = RegByte(ModRM);
            int dst = GetRMByte(ModRM);
            //SUBB(dst,src);
            int res = dst - src;
            SetCFB(res);
            SetOFB_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Byte(res);
            dst = res & 0xFF;
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 11;
            if (neclog != null) {
                fprintf(neclog, "i_cmp_br8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_cmp_wr16 = new InstructionPtr() {
        public void handler() {
            //DEF_wr16(dst,src);
            int ModRM = FETCHOP();
            int src = RegWord(ModRM);
            int dst = GetRMWord(ModRM);
            //SUBW(dst,src);
            int res = dst - src;
            SetCFW(res);
            SetOFW_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_cmp_wr16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///
    /*TODO*///static void i_cmp_wr16(void)    /* Opcode 0x39 */
    /*TODO*///{
    /*TODO*///    
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cmp_r8b(void)    /* Opcode 0x3a */
    /*TODO*///{
    /*TODO*///    DEF_r8b(dst,src);
    /*TODO*///    SUBB(dst,src);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:11;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_cmp_r16w = new InstructionPtr() {
        public void handler() {
            //DEF_r16w(dst,src);
            /*unsigned*/
            int ModRM = FETCHOP();
            /*unsigned*/
            int dst = RegWord(ModRM);
            /*unsigned*/
            int src = GetRMWord(ModRM);
            //SUBW(dst,src);
            int res = dst - src;
            SetCFW(res);
            SetOFW_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_cmp_r16w :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_cmp_ald8(void)    /* Opcode 0x3c */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    SUBB(dst,src);
    /*TODO*///    nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cmp_axd16(void)    /* Opcode 0x3d */
    /*TODO*///{
    /*TODO*///    DEF_axd16(dst,src);
    /*TODO*///    SUBW(dst,src);
    /*TODO*///    nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_ds(void)    /* Opcode 0x3e */
    /*TODO*///{
    /*TODO*///    I.seg_prefix=TRUE;
    /*TODO*///	I.prefix_base=I.base[DS];
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	nec_instruction[FETCHOP]();
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_aas(void)    /* Opcode 0x3f */
    /*TODO*///{
    /*TODO*///	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    /*TODO*///    {
    /*TODO*///		I.regs.b[AL] -= 6;
    /*TODO*///		I.regs.b[AH] -= 1;
    /*TODO*///		I.AuxVal = 1;
    /*TODO*///		I.CarryVal = 1;
    /*TODO*///    }
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		I.AuxVal = 0;
    /*TODO*///		I.CarryVal = 0;
    /*TODO*///    }
    /*TODO*///	I.regs.b[AL] &= 0x0F;
    /*TODO*///	nec_ICount-=3;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    static void IncWordReg(int Reg) {
        /*unsigned*/
        int tmp = /*(unsigned)*/ I.regs.w[Reg];
        /*unsigned*/
        int tmp1 = tmp + 1;
        /*SetOFW_Add(tmp1,tmp,1);*/
        I.OverVal = BOOL(tmp == 0x7fff); /* MISH */

        SetAF(tmp1, tmp, 1);
        SetSZPF_Word(tmp1);
        I.regs.SetW(Reg, tmp1 & 0xFFFF);
        nec_ICount[0] -= 2;
    }
    static InstructionPtr i_inc_ax = new InstructionPtr() /* Opcode 0x40 */ {
        public void handler() {
            IncWordReg(AW);
            if (neclog != null) {
                fprintf(neclog, "i_inc_ax :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_cx = new InstructionPtr() /* Opcode 0x41 */ {
        public void handler() {
            IncWordReg(CW);
            if (neclog != null) {
                fprintf(neclog, "i_inc_cx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_dx = new InstructionPtr() /* Opcode 0x42 */ {
        public void handler() {
            IncWordReg(DW);
            if (neclog != null) {
                fprintf(neclog, "i_inc_dx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_bx = new InstructionPtr() /* Opcode 0x43 */ {
        public void handler() {
            IncWordReg(BW);
            /*if (neclog != null) {
             fprintf(neclog, "i_inc_bx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_inc_sp = new InstructionPtr() /* Opcode 0x44 */ {
        public void handler() {
            IncWordReg(SP);
            if (neclog != null) {
                fprintf(neclog, "i_inc_sp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_bp = new InstructionPtr() /* Opcode 0x45 */ {
        public void handler() {
            IncWordReg(BP);
            if (neclog != null) {
                fprintf(neclog, "i_inc_bp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_si = new InstructionPtr() /* Opcode 0x46 */ {
        public void handler() {
            IncWordReg(IX);
            if (neclog != null) {
                fprintf(neclog, "i_inc_si :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_inc_di = new InstructionPtr() /* Opcode 0x47 */ {
        public void handler() {
            IncWordReg(IY);
            /*if (neclog != null) {
             fprintf(neclog, "i_inc_di :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };

    public static void DecWordReg(int Reg) {
        /*unsigned*/
        int tmp = /*(unsigned)*/ I.regs.w[Reg];
        /*unsigned*/
        int tmp1 = tmp - 1;
        /*SetOFW_Sub(tmp1,1,tmp);*/
        I.OverVal = BOOL(tmp == 0x8000); /* MISH */

        SetAF(tmp1, tmp, 1);
        SetSZPF_Word(tmp1);
        I.regs.SetW(Reg, tmp1 & 0xFFFF);
        nec_ICount[0] -= 2;
    }
    static InstructionPtr i_dec_ax = new InstructionPtr() /* Opcode 0x48 */ {
        public void handler() {
            DecWordReg(AW);
            if (neclog != null) {
                fprintf(neclog, "i_dec_ax :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_cx = new InstructionPtr() /* Opcode 0x49 */ {
        public void handler() {
            DecWordReg(CW);
            if (neclog != null) {
                fprintf(neclog, "i_dec_cx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_dx = new InstructionPtr() /* Opcode 0x4a */ {
        public void handler() {
            DecWordReg(DW);
            if (neclog != null) {
                fprintf(neclog, "i_dec_dx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_bx = new InstructionPtr() /* Opcode 0x4b */ {
        public void handler() {
            DecWordReg(BW);
            if (neclog != null) {
                fprintf(neclog, "i_dec_bx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_sp = new InstructionPtr() /* Opcode 0x4c */ {
        public void handler() {
            DecWordReg(SP);
            if (neclog != null) {
                fprintf(neclog, "i_dec_sp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_bp = new InstructionPtr() /* Opcode 0x4d */ {
        public void handler() {
            DecWordReg(BP);
            if (neclog != null) {
                fprintf(neclog, "i_dec_bp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_si = new InstructionPtr() /* Opcode 0x4e */ {
        public void handler() {
            DecWordReg(IX);
            if (neclog != null) {
                fprintf(neclog, "i_dec_si :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_dec_di = new InstructionPtr() /* Opcode 0x4f */ {
        public void handler() {
            DecWordReg(IY);
            if (neclog != null) {
                fprintf(neclog, "i_dec_di :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_push_ax(void)    /* Opcode 0x50 */
    /*TODO*///{
    /*TODO*///	PUSH(I.regs.w[AW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_push_cx(void)    /* Opcode 0x51 */
    /*TODO*///{
    /*TODO*///	PUSH(I.regs.w[CW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_push_dx(void)    /* Opcode 0x52 */
    /*TODO*///{
    /*TODO*///	PUSH(I.regs.w[DW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_push_bx = new InstructionPtr() {
        public void handler() {
            PUSH(I.regs.w[BW]);
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_push_bx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_push_sp(void)    /* Opcode 0x54 */
    /*TODO*///{
    /*TODO*///	PUSH(I.regs.w[SP]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_push_bp = new InstructionPtr() {
        public void handler() {
            PUSH(I.regs.w[BP]);
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_push_bp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_push_bp(void)    /* Opcode 0x55 */
    /*TODO*///{
    /*TODO*///	
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_push_si(void)    /* Opcode 0x56 */
    /*TODO*///{
    /*TODO*///	PUSH(I.regs.w[IX]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_push_di = new InstructionPtr() {
        public void handler() {
            PUSH(I.regs.w[IY]);
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_push_di :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_pop_ax(void)    /* Opcode 0x58 */
    /*TODO*///{
    /*TODO*///	POP(I.regs.w[AW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_pop_cx(void)    /* Opcode 0x59 */
    /*TODO*///{
    /*TODO*///	POP(I.regs.w[CW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_pop_dx(void)    /* Opcode 0x5a */
    /*TODO*///{
    /*TODO*///	POP(I.regs.w[DW]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_pop_bx = new InstructionPtr() {
        public void handler() {
            I.regs.SetW(BW, POP());
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_pop_bx :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_pop_sp(void)    /* Opcode 0x5c */
    /*TODO*///{
    /*TODO*///	POP(I.regs.w[SP]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_pop_bp = new InstructionPtr() {
        public void handler() {
            I.regs.SetW(BP, POP());
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_pop_bp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_pop_si(void)    /* Opcode 0x5e */
    /*TODO*///{
    /*TODO*///	POP(I.regs.w[IX]);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_pop_di = new InstructionPtr() {
        public void handler() {
            I.regs.SetW(IY, POP());
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_pop_di :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_pusha(void)    /* Opcode 0x60 */
    /*TODO*///{
    /*TODO*///	unsigned tmp=I.regs.w[SP];
    /*TODO*///	PUSH(I.regs.w[AW]);
    /*TODO*///	PUSH(I.regs.w[CW]);
    /*TODO*///	PUSH(I.regs.w[DW]);
    /*TODO*///	PUSH(I.regs.w[BW]);
    /*TODO*///    PUSH(tmp);
    /*TODO*///	PUSH(I.regs.w[BP]);
    /*TODO*///	PUSH(I.regs.w[IX]);
    /*TODO*///	PUSH(I.regs.w[IY]);
    /*TODO*///	nec_ICount-=51;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_popa(void)    /* Opcode 0x61 */
    /*TODO*///{
    /*TODO*///    unsigned tmp;
    /*TODO*///	POP(I.regs.w[IY]);
    /*TODO*///	POP(I.regs.w[IX]);
    /*TODO*///	POP(I.regs.w[BP]);
    /*TODO*///    POP(tmp);
    /*TODO*///	POP(I.regs.w[BW]);
    /*TODO*///	POP(I.regs.w[DW]);
    /*TODO*///	POP(I.regs.w[CW]);
    /*TODO*///	POP(I.regs.w[AW]);
    /*TODO*///	nec_ICount-=59;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_bound(void)    /* Opcode 0x62  BOUND or CHKIND (on NEC)*/
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    int low = (INT16)GetRMWord(ModRM);
    /*TODO*///    int high= (INT16)GetnextRMWord;
    /*TODO*///    int tmp= (INT16)RegWord(ModRM);
    /*TODO*///    if (tmp<low || tmp>high) {
    /*TODO*///		/* OB: on NECs CS:IP points to instruction
    /*TODO*///		       FOLLOWING the BOUND instruction ! */
    /*TODO*///		// I.ip-=2;
    /*TODO*///		nec_interrupt(5,0);
    /*TODO*///    }
    /*TODO*/// 	nec_ICount-=20;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_brkn(void)	/* Opcode 0x63 BRKN -  Break to Native Mode */
    /*TODO*///{
    /*TODO*///	/*
    /*TODO*///	CPU:  NEC (V25/V35) Software Guard only
    /*TODO*///	Instruction:  BRKN int_vector
    /*TODO*///
    /*TODO*///	Description:
    /*TODO*///		     [sp-1,sp-2] <- PSW		; PSW EQU FLAGS
    /*TODO*///		     [sp-3,sp-4] <- PS		; PS  EQU CS
    /*TODO*///		     [sp-5,sp-6] <- PC		; PC  EQU IP
    /*TODO*///		     SP	 <-  SP -6
    /*TODO*///		     IE	 <-  0
    /*TODO*///		     BRK <-  0
    /*TODO*///		     MD	 <-  1
    /*TODO*///		     PC	 <- [int_vector*4 +0,+1]
    /*TODO*///		     PS	 <- [int_vector*4 +2,+3]
    /*TODO*///
    /*TODO*///	Note:	The BRKN instruction switches operations in Native Mode
    /*TODO*///		from Security Mode via Interrupt call. In Normal Mode
    /*TODO*///		Instruction executed as	 mPD70320/70322 (V25) operation mode.
    /*TODO*///
    /*TODO*///	Flags Affected:	 None
    /*TODO*///
    /*TODO*///	CPU mode: RM
    /*TODO*///
    /*TODO*///	+++++++++++++++++++++++
    /*TODO*///	Physical Form:	BRKN  imm8
    /*TODO*///	COP (Code of Operation)	 : 63h imm8
    /*TODO*///
    /*TODO*///	Clocks:	 56+10T [44+10T]
    /*TODO*///	*/
    /*TODO*///	//nec_ICount-=56;
    /*TODO*///	unsigned int_vector;
    /*TODO*///	int_vector = FETCH;
    /*TODO*///	if (errorlog) fprintf(errorlog,"PC=%06x : BRKN %02x\n",cpu_get_pc()-2,int_vector);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*///static void repc(int flagval)
    /*TODO*///{
    /*TODO*///    /* Handles repc- and repnc- prefixes. flagval is the value of ZF for the
    /*TODO*///       loop  to continue for CMPS and SCAS instructions. */
    /*TODO*///
    /*TODO*///	unsigned next = FETCHOP;
    /*TODO*///	unsigned count = I.regs.w[CW];
    /*TODO*///
    /*TODO*///    switch(next)
    /*TODO*///    {
    /*TODO*///    case 0x26:  /* ES: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///		I.prefix_base=I.base[ES];
    /*TODO*///		nec_ICount-=2;
    /*TODO*///		repc(flagval);
    /*TODO*///		break;
    /*TODO*///    case 0x2e:  /* CS: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///		I.prefix_base=I.base[CS];
    /*TODO*///		nec_ICount-=2;
    /*TODO*///		repc(flagval);
    /*TODO*///		break;
    /*TODO*///    case 0x36:  /* SS: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///		I.prefix_base=I.base[SS];
    /*TODO*///		nec_ICount-=2;
    /*TODO*///		repc(flagval);
    /*TODO*///		break;
    /*TODO*///    case 0x3e:  /* DS: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///		I.prefix_base=I.base[DS];
    /*TODO*///		nec_ICount-=2;
    /*TODO*///		repc(flagval);
    /*TODO*///		break;
    /*TODO*///    case 0x6c:  /* REP INSB */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (; (CF==flagval)&&(count > 0); count--)
    /*TODO*///           i_insb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0x6d:  /* REP INSW */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///           i_insw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0x6e:  /* REP OUTSB */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///            i_outsb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0x6f:  /* REP OUTSW */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (; (CF==flagval)&&(count > 0); count--)
    /*TODO*///            i_outsw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xa4:  /* REP MOVSB */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_movsb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xa5:  /* REP MOVSW */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_movsw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xa6:  /* REP(N)E CMPSB */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (I.ZeroVal = !flagval; (ZF == flagval) && (CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_cmpsb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xa7:  /* REP(N)E CMPSW */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (I.ZeroVal = !flagval; (ZF == flagval) && (CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_cmpsw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xaa:  /* REP STOSB */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_stosb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xab:  /* REP STOSW */
    /*TODO*///		nec_ICount-=9-count;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_stosw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xac:  /* REP LODSB */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_lodsb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xad:  /* REP LODSW */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (;(CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_lodsw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xae:  /* REP(N)E SCASB */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (I.ZeroVal = !flagval; (ZF == flagval) && (CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_scasb();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    case 0xaf:  /* REP(N)E SCASW */
    /*TODO*///		nec_ICount-=9;
    /*TODO*///		for (I.ZeroVal = !flagval; (ZF == flagval) && (CF==flagval)&&(count > 0); count--)
    /*TODO*///			i_scasw();
    /*TODO*///		I.regs.w[CW]=count;
    /*TODO*///		break;
    /*TODO*///    default:
    /*TODO*///		nec_instruction[next]();
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_repnc(void)    /* Opcode 0x64 */
    /*TODO*///{
    /*TODO*///    repc(0);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_repc(void)    /* Opcode 0x65 */
    /*TODO*///{
    /*TODO*///    repc(1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_push_d16(void)    /* Opcode 0x68 */
    /*TODO*///{
    /*TODO*///    unsigned tmp = FETCH;
    /*TODO*///    tmp += FETCH << 8;
    /*TODO*///    PUSH(tmp);
    /*TODO*///    nec_ICount-=12;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_imul_d16(void)    /* Opcode 0x69 */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///    unsigned src2=FETCH;
    /*TODO*///    src2+=(FETCH<<8);
    /*TODO*///    dst = (INT32)((INT16)src)*(INT32)((INT16)src2);
    /*TODO*///	I.CarryVal = I.OverVal = (((INT32)dst) >> 15 != 0) && (((INT32)dst) >> 15 != -1);
    /*TODO*///    RegWord(ModRM)=(WORD)dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?38:47;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_push_d8(void)    /* Opcode 0x6a */
    /*TODO*///{
    /*TODO*///    unsigned tmp = (WORD)((INT16)((INT8)FETCH));
    /*TODO*///    PUSH(tmp);
    /*TODO*///    nec_ICount-=7;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_imul_d8(void)    /* Opcode 0x6b */
    /*TODO*///{
    /*TODO*///    DEF_r16w(dst,src);
    /*TODO*///    unsigned src2= (WORD)((INT16)((INT8)FETCH));
    /*TODO*///    dst = (INT32)((INT16)src)*(INT32)((INT16)src2);
    /*TODO*///	I.CarryVal = I.OverVal = (((INT32)dst) >> 15 != 0) && (((INT32)dst) >> 15 != -1);
    /*TODO*///    RegWord(ModRM)=(WORD)dst;
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?31:39;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_insb(void)    /* Opcode 0x6c */
    /*TODO*///{
    /*TODO*///	nec_ICount-=5;
    /*TODO*///	PutMemB(ES,I.regs.w[IY],read_port(I.regs.w[DW]));
    /*TODO*///	I.regs.w[IY]+= -2 * I.DF + 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_insw(void)    /* Opcode 0x6d */
    /*TODO*///{
    /*TODO*///	PutMemB(ES,I.regs.w[IY],read_port(I.regs.w[DW]));
    /*TODO*///	PutMemB(ES,I.regs.w[IY]+1,read_port(I.regs.w[DW]+1));
    /*TODO*/////if (errorlog) fprintf(errorlog,"%04x:  insw\n",cpu_get_pc());
    /*TODO*///	I.regs.w[IY]+= -4 * I.DF + 2;
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_outsb(void)    /* Opcode 0x6e */
    /*TODO*///{
    /*TODO*///	write_port(I.regs.w[DW],GetMemB(DS,I.regs.w[IX]));
    /*TODO*///	I.regs.w[IY]+= -2 * I.DF + 1;
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_outsw(void)    /* Opcode 0x6f */
    /*TODO*///{
    /*TODO*///	write_port(I.regs.w[DW],GetMemB(DS,I.regs.w[IX]));
    /*TODO*///	write_port(I.regs.w[DW]+1,GetMemB(DS,I.regs.w[IX]+1));
    /*TODO*///	I.regs.w[IY]+= -4 * I.DF + 2;
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jo(void)    /* Opcode 0x70 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///	if (OF)
    /*TODO*///	{
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jno(void)    /* Opcode 0x71 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///	if (!OF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jb(void)    /* Opcode 0x72 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///	if (CF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_jnb = new InstructionPtr() {
        public void handler() {
            int tmp = (int) ((byte) FETCH());
            if (CF() == 0) {
                I.ip = (I.ip + tmp) & 0xFFFF;
                nec_ICount[0] -= 14;
                change_pc20((I.base[CS] + I.ip));
            } else {
                nec_ICount[0] -= 4;
            }
            if (neclog != null) {
                fprintf(neclog, "i_jnb :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_jz = new InstructionPtr() /* Opcode 0x74 */ {
        public void handler() {
            int tmp = (int) ((byte) FETCH());
            if (ZF() != 0) {
                I.ip = (I.ip + tmp) & 0xFFFF;
                nec_ICount[0] -= 14;
                change_pc20((I.base[CS] + I.ip));
            } else {
                nec_ICount[0] -= 4;
            }
            if (neclog != null) {
                fprintf(neclog, "i_jz :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_jnz = new InstructionPtr() /* Opcode 0x75 */ {
        public void handler() {
            int tmp = (int) ((byte) FETCH());
            if (ZF() == 0) {
                I.ip = (I.ip + tmp) & 0xFFFF;
                nec_ICount[0] -= 14;
                change_pc20((I.base[CS] + I.ip));
            } else {
                nec_ICount[0] -= 4;
            }
            if (neclog != null) {
                fprintf(neclog, "i_jnz :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///
    /*TODO*///static void i_jbe(void)    /* Opcode 0x76 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (CF || ZF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jnbe(void)    /* Opcode 0x77 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (!(CF || ZF)) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_js(void)    /* Opcode 0x78 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (SF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jns(void)    /* Opcode 0x79 */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (!SF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jp(void)    /* Opcode 0x7a */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (PF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jnp(void)    /* Opcode 0x7b */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (!PF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jl(void)    /* Opcode 0x7c */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if ((SF!=OF)&&!ZF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jnl(void)    /* Opcode 0x7d */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (ZF||(SF==OF)) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jle(void)    /* Opcode 0x7e */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if (ZF||(SF!=OF)) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_jnle(void)    /* Opcode 0x7f */
    /*TODO*///{
    /*TODO*///	int tmp = (int)((INT8)FETCH);
    /*TODO*///    if ((SF==OF)&&!ZF) {
    /*TODO*///		I.ip = (WORD)(I.ip+tmp);
    /*TODO*///		nec_ICount-=14;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_80pre = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            int dst = GetRMByte(ModRM);
            int src = FETCH();
            nec_ICount[0] -= (ModRM >= 0xc0) ? 4 : 18;

            switch (ModRM & 0x38) {
                /*TODO*///    case 0x00:  /* ADD eb,d8 */
    /*TODO*///        ADDB(dst,src);
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x08:  /* OR eb,d8 */
    /*TODO*///        ORB(dst,src);
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
                case 0x10: /* ADC eb,d8 */ {
                    src += CF();
                    //ADDB(dst,src);
                    int res = dst + src;
                    SetCFB(res);
                    SetOFB_Add(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Byte(res);
                    dst = res & 0xFF;
                    PutbackRMByte(ModRM, dst);
                    if (neclog != null) {
                        fprintf(neclog, "i_80pre_0x10 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                /*TODO*///    case 0x18:  /* SBB eb,b8 */
    /*TODO*///        src+=CF;
    /*TODO*///        SUBB(dst,src);
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
                case 0x20: /* AND eb,d8 */ {
                    //ANDB(dst,src);
                    dst &= src;
                    I.CarryVal = I.OverVal = I.AuxVal = 0;
                    SetSZPF_Byte(dst);
                    PutbackRMByte(ModRM, dst & 0xFF);
                    if (neclog != null) {
                        fprintf(neclog, "i_80pre_0x20 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                /*TODO*///    case 0x28:  /* SUB eb,d8 */
    /*TODO*///        SUBB(dst,src);
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x30:  /* XOR eb,d8 */
    /*TODO*///        XORB(dst,src);
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
                case 0x38:  /* CMP eb,d8 */

                    //SUBB(dst,src);

                    int res = dst - src;
                    SetCFB(res);
                    SetOFB_Sub(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Byte(res);
                    dst = res & 0xFF;
                    if (neclog != null) {
                        fprintf(neclog, "i_80pre_0x38 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                    break;
                default:
                    System.out.println("i_80pre 0x" + Integer.toHexString(ModRM & 0x38));
                    break;
            }
        }
    };
    static InstructionPtr i_81pre = new InstructionPtr() /* Opcode 0x81 */ {
        public void handler() {
            /*unsigned*/
            int ModRM = FETCH();
            /*unsigned*/
            int dst = GetRMWord(ModRM) & 0xFFFF;
            /*unsigned*/
            int src = FETCH();
            src += (FETCH() << 8);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 4 : 26;

            switch (ModRM & 0x38) {
                case 0x00: /* ADD ew,d16 */ {
                    //ADDW(dst,src);
                        /*unsigned*/
                    int res = dst + src;
                    SetCFW(res);
                    SetOFW_Add(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Word(res);
                    dst = res & 0xFFFF;
                    PutbackRMWord(ModRM, dst);
                    if (neclog != null) {
                        fprintf(neclog, "i_81pre_0x00 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                /*TODO*///    case 0x08:  /* OR ew,d16 */
    /*TODO*///        ORW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x10:  /* ADC ew,d16 */
    /*TODO*///        src+=CF;
    /*TODO*///		ADDW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x18:  /* SBB ew,d16 */
    /*TODO*///        src+=CF;
    /*TODO*///        SUBW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
                case 0x20: /* AND ew,d16 */ {
                    //ANDW(dst,src);
                    dst &= src;
                    I.CarryVal = I.OverVal = I.AuxVal = 0;
                    SetSZPF_Word(dst);
                    PutbackRMWord(ModRM, dst);
                    if (neclog != null) {
                        fprintf(neclog, "i_81pre_0x20 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                case 0x28: /* SUB ew,d16 */ {
                    //SUBW(dst,src);
                        /*unsigned*/
                    int res = dst - src;
                    SetCFW(res);
                    SetOFW_Sub(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Word(res);
                    dst = res & 0xFFFF;
                    PutbackRMWord(ModRM, dst);
                    if (neclog != null) {
                        fprintf(neclog, "i_81pre_0x28 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                /*TODO*///    case 0x30:  /* XOR ew,d16 */
    /*TODO*///        XORW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
                case 0x38: /* CMP ew,d16 */ {
                    //SUBW(dst,src);
                    /*unsigned*/
                    int res = dst - src;
                    SetCFW(res);
                    SetOFW_Sub(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Word(res);
                    dst = res & 0xFFFF;
                    if (neclog != null) {
                        fprintf(neclog, "i_81pre_0x38 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                default:
                    System.out.println("i_81pre 0x" + Integer.toHexString(ModRM & 0x38));
                    break;
            }
        }
    };
    /*TODO*///static void i_82pre(void)	 /* Opcode 0x82 */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///	unsigned dst = GetRMByte(ModRM);
    /*TODO*///	unsigned src = FETCH;
    /*TODO*///	nec_ICount-=(ModRM >=0xc0 )?4:18;
    /*TODO*///
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///	case 0x00:	/* ADD eb,d8 */
    /*TODO*///		ADDB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x08:	/* OR eb,d8 */
    /*TODO*///		ORB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x10:	/* ADC eb,d8 */
    /*TODO*///        src+=CF;
    /*TODO*///		ADDB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x18:	/* SBB eb,d8 */
    /*TODO*///        src+=CF;
    /*TODO*///		SUBB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x20:	/* AND eb,d8 */
    /*TODO*///		ANDB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x28:	/* SUB eb,d8 */
    /*TODO*///		SUBB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x30:	/* XOR eb,d8 */
    /*TODO*///		XORB(dst,src);
    /*TODO*///		PutbackRMByte(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///	case 0x38:	/* CMP eb,d8 */
    /*TODO*///		SUBB(dst,src);
    /*TODO*///	break;
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///

    static InstructionPtr i_83pre = new InstructionPtr() /* Opcode 0x83 */ {
        public void handler() {
            /*unsigned*/
            int ModRM = FETCH();
            /*unsigned*/
            int dst = GetRMWord(ModRM) & 0xFFFF;
            /*unsigned*/
            int src = ((short) ((byte) FETCH())) & 0xFFFF;
            nec_ICount[0] -= (ModRM >= 0xc0) ? 4 : 26;

            switch (ModRM & 0x38) {
                case 0x00: /* ADD ew,d16 */ {
                    //ADDW(dst,src);
                        /*unsigned*/
                    int res = dst + src;
                    SetCFW(res);
                    SetOFW_Add(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Word(res);
                    dst = res & 0xFFFF;
                    PutbackRMWord(ModRM, dst);
                    if (neclog != null) {
                        fprintf(neclog, "i_83pre_0x00 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                /*TODO*///    case 0x08:  /* OR ew,d16 */
    /*TODO*///        ORW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x10:  /* ADC ew,d16 */
    /*TODO*///        src+=CF;
    /*TODO*///        ADDW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x18:  /* SBB ew,d16 */
    /*TODO*///        src+=CF;
    /*TODO*///        SUBW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x20:  /* AND ew,d16 */
    /*TODO*///        ANDW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x28:  /* SUB ew,d16 */
    /*TODO*///        SUBW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    case 0x30:  /* XOR ew,d16 */
    /*TODO*///        XORW(dst,src);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
                case 0x38: /* CMP ew,d16 */ {
                    //SUBW(dst,src);
                    int res = dst - src;
                    SetCFW(res);
                    SetOFW_Sub(res, src, dst);
                    SetAF(res, src, dst);
                    SetSZPF_Word(res);
                    //dst=(WORD)res;
                    if (neclog != null) {
                        fprintf(neclog, "i_83pre_0x38 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                }
                break;
                default:
                    System.out.println("i_83pre 0x" + Integer.toHexString(ModRM & 0x38));
                    break;
            }
        }
    };
    /*TODO*///static void i_test_br8(void)    /* Opcode 0x84 */
    /*TODO*///{
    /*TODO*///    DEF_br8(dst,src);
    /*TODO*///    ANDB(dst,src);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_test_wr16 = new InstructionPtr() {
        public void handler() {
            //DEF_wr16(dst,src);
            int ModRM = FETCHOP();
            int src = RegWord(ModRM);
            int dst = GetRMWord(ModRM);
            //ANDW(dst,src);
            dst &= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Word(dst);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 14;
            if (neclog != null) {
                fprintf(neclog, "i_test_wr16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_xchg_br8 = new InstructionPtr() {
        public void handler() {
            //DEF_br8(dst,src);
            int ModRM = FETCHOP();
            int src = RegByte(ModRM);
            int dst = GetRMByte(ModRM);
            SetRegByte(ModRM, dst);
            PutbackRMByte(ModRM, src);
            // V30
            if (ModRM >= 0xc0) {
                nec_ICount[0] -= 3;
            } else {
                nec_ICount[0] -= (EO & 1) != 0 ? 24 : 16;
            }
            if (neclog != null) {
                fprintf(neclog, "i_xchg_br8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_xchg_wr16(void)    /* Opcode 0x87 */
    /*TODO*///{
    /*TODO*///    DEF_wr16(dst,src);
    /*TODO*///    RegWord(ModRM)=dst;
    /*TODO*///    PutbackRMWord(ModRM,src);
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?3:24;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_mov_br8 = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            int src = RegByte(ModRM) & 0xFF;
            PutRMByte(ModRM, src);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 9;
            /*if (neclog != null) {
             fprintf(neclog, "i_mov_br8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_mov_wr16 = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            int src = RegWord(ModRM) & 0xFFFF;
            PutRMWord(ModRM, src);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 13;
            if (neclog != null) {
                fprintf(neclog, "i_mov_wr16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_r8b = new InstructionPtr() /* Opcode 0x8a */ {
        public void handler() {
            /*unsigned*/
            int ModRM = FETCH();
            /*BYTE*/
            int src = GetRMByte(ModRM) & 0xFF;
            SetRegByte(ModRM, src);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 11;
            /*if (neclog != null) {
             fprintf(neclog, "i_mov_r8b :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_mov_r16w = new InstructionPtr() /* Opcode 0x8b */ {
        public void handler() {
            /*unsigned*/
            int ModRM = FETCH();
            /*WORD*/
            int src = GetRMWord(ModRM) & 0xFFFF;
            SetRegWord(ModRM, src);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_movr16w :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_mov_wsreg(void)    /* Opcode 0x8c */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///	if (ModRM & 0x20) return;	/* HJB 12/13/98 1xx is invalid */
    /*TODO*///	PutRMWord(ModRM,I.sregs[(ModRM & 0x38) >> 3]);
    /*TODO*///	nec_ICount-=(ModRM >=0xc0 )?2:12;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_lea(void)    /* Opcode 0x8d */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///	(void)(*GetEA[ModRM])();
    /*TODO*///	RegWord(ModRM)=EO;	/* HJB 12/13/98 effective offset (no segment part) */
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_mov_sregw = new InstructionPtr() /* Opcode 0x8e */ {
        public void handler() {

            /*unsigned*/ int ModRM = FETCH();
            /*WORD*/
            int src = GetRMWord(ModRM) & 0xFFFF;
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 13;
            switch (ModRM & 0x38) {
                case 0x00:  /* mov es,ew */

                    I.sregs[ES] = src;
                    I.base[ES] = SegBase(ES);
                    if (neclog != null) {
                        fprintf(neclog, "i_mov_sregw_0x00 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                    break;
                case 0x18:  /* mov ds,ew */

                    I.sregs[DS] = src;
                    I.base[DS] = SegBase(DS);
                    if (neclog != null) {
                        fprintf(neclog, "i_mov_sregw_0x18 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                    break;
                case 0x10:  /* mov ss,ew */

                    I.sregs[SS] = src;
                    I.base[SS] = SegBase(SS); /* no interrupt allowed before next instr */

                    if (neclog != null) {
                        fprintf(neclog, "i_mov_sregw_0x10 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                    nec_instruction[FETCHOP()].handler();
                    break;
                case 0x08:  /* mov cs,ew */

                    break;  /* doesn't do a jump far */

            }
        }
    };
    /*TODO*///static void i_popw(void)    /* Opcode 0x8f */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    WORD tmp;
    /*TODO*///    POP(tmp);
    /*TODO*///    PutRMWord(ModRM,tmp);
    /*TODO*///    nec_ICount-=21;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///#define XchgAWReg(Reg) \
    /*TODO*///{ \
    /*TODO*///    WORD tmp; \
    /*TODO*///	tmp = I.regs.w[Reg]; \
    /*TODO*///	I.regs.w[Reg] = I.regs.w[AW]; \
    /*TODO*///	I.regs.w[AW] = tmp; \
    /*TODO*///	nec_ICount-=3; \
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_nop(void)    /* Opcode 0x90 */
    /*TODO*///{
    /*TODO*///    /* this is XchgAWReg(AW); */
    /*TODO*///	nec_ICount-=2;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axcx(void)    /* Opcode 0x91 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(CW);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axdx(void)    /* Opcode 0x92 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(DW);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axbx(void)    /* Opcode 0x93 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(BW);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axsp(void)    /* Opcode 0x94 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(SP);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axbp(void)    /* Opcode 0x95 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(BP);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axsi(void)    /* Opcode 0x96 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(IX);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xchg_axdi(void)    /* Opcode 0x97 */
    /*TODO*///{
    /*TODO*///    XchgAWReg(IY);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cbw(void)    /* Opcode 0x98 */
    /*TODO*///{
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	I.regs.b[AH] = (I.regs.b[AL] & 0x80) ? 0xff : 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cwd(void)    /* Opcode 0x99 */
    /*TODO*///{
    /*TODO*///	nec_ICount-=5;
    /*TODO*///	I.regs.w[DW] = (I.regs.b[AH] & 0x80) ? 0xffff : 0;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_call_far = new InstructionPtr() {
        public void handler() {

            int tmp, tmp2;

            tmp = FETCH();
            tmp += FETCH() << 8;

            tmp2 = FETCH();
            tmp2 += FETCH() << 8;

            PUSH(I.sregs[CS]);
            PUSH(I.ip);

            I.ip = tmp & 0xFFFF;
            I.sregs[CS] = tmp2 & 0xFFFF;
            I.base[CS] = SegBase(CS);
            change_pc20((I.base[CS] + I.ip));
            nec_ICount[0] -= 39;
            if (neclog != null) {
                fprintf(neclog, "i_call_far :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_wait(void)    /* Opcode 0x9b */
    /*TODO*///{
    /*TODO*///	nec_ICount-=7;   /* 2+5n (n = number of times POLL pin sampled) */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_pushf(void)    /* Opcode 0x9c */
    /*TODO*///{
    /*TODO*///    PUSH( CompressFlags() | 0xf000 );
    /*TODO*///    nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_popf(void)    /* Opcode 0x9d */
    /*TODO*///{
    /*TODO*///    unsigned tmp;
    /*TODO*///    POP(tmp);
    /*TODO*///    ExpandFlags(tmp);
    /*TODO*///	nec_ICount-=10;
    /*TODO*///	if (I.TF) nec_trap();
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_sahf(void)    /* Opcode 0x9e */
    /*TODO*///{
    /*TODO*///	unsigned tmp = (CompressFlags() & 0xff00) | (I.regs.b[AH] & 0xd5);
    /*TODO*///    ExpandFlags(tmp);
    /*TODO*///    nec_ICount-=3;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_lahf(void)    /* Opcode 0x9f */
    /*TODO*///{
    /*TODO*///	I.regs.b[AH] = CompressFlags() & 0xff;
    /*TODO*///	nec_ICount-=2;
    /*TODO*///}
    /*TODO*///
    public static InstructionPtr i_mov_aldisp = new InstructionPtr() /* Opcode 0xa0 */ {
        public void handler() {
            /*unsigned*/
            int addr;

            addr = FETCH();
            addr += FETCH() << 8;
            I.regs.SetB(AL, GetMemB(DS, addr));
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_mov_aldisp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    public static InstructionPtr i_mov_axdisp = new InstructionPtr() /* Opcode 0xa1 */ {
        public void handler() {
            /*unsigned*/
            int addr;

            addr = FETCH();
            addr += FETCH() << 8;
            I.regs.SetB(AL, GetMemB(DS, addr));
            I.regs.SetB(AH, GetMemB(DS, addr + 1));
            nec_ICount[0] -= 14;
            if (neclog != null) {
                fprintf(neclog, "i_mov_axdisp :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_dispal = new InstructionPtr() {
        public void handler() {
            int addr;
            addr = FETCH();
            addr += FETCH() << 8;
            PutMemB(DS, addr, I.regs.b[AL]);
            nec_ICount[0] -= 9;
            if (neclog != null) {
                fprintf(neclog, "i_mov_dispal :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_dispax = new InstructionPtr() {
        public void handler() {
            int addr;
            addr = FETCH();
            addr += FETCH() << 8;
            PutMemB(DS, addr, I.regs.b[AL]);
            PutMemB(DS, addr + 1, I.regs.b[AH]);
            nec_ICount[0] -= 13;
            if (neclog != null) {
                fprintf(neclog, "i_mov_dispax :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_movsb = new InstructionPtr() {
        public void handler() {
            int ix = I.regs.w[IX];
            int iy = I.regs.w[IY];
            int tmp = GetMemB(DS, ix) & 0xFF;
            PutMemB(ES, iy, tmp);
            ix += -2 * I.DF + 1;
            iy += -2 * I.DF + 1;
            I.regs.SetW(IY, iy & 0xFFFF);
            I.regs.SetW(IX, ix & 0xFFFF);
            nec_ICount[0] -= 19;	// 11+8n
            if (neclog != null) {
                fprintf(neclog, "i_movsb :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_movsw(void)    /* Opcode 0xa5 */
    /*TODO*///{
    /*TODO*///	WORD tmp = GetMemW(DS,I.regs.w[IX]);
    /*TODO*///	PutMemW(ES,I.regs.w[IY], tmp);
    /*TODO*///	I.regs.w[IY] += -4 * I.DF + 2;
    /*TODO*///	I.regs.w[IX] += -4 * I.DF + 2;
    /*TODO*///	nec_ICount-=19; // 11+8n
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cmpsb(void)    /* Opcode 0xa6 */
    /*TODO*///{
    /*TODO*///	unsigned dst = GetMemB(ES, I.regs.w[IY]);
    /*TODO*///	unsigned src = GetMemB(DS, I.regs.w[IX]);
    /*TODO*///    SUBB(src,dst); /* opposite of the usual convention */
    /*TODO*///	I.regs.w[IY] += -2 * I.DF + 1;
    /*TODO*///	I.regs.w[IX] += -2 * I.DF + 1;
    /*TODO*///	nec_ICount-=14;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cmpsw(void)    /* Opcode 0xa7 */
    /*TODO*///{
    /*TODO*///	unsigned dst = GetMemW(ES, I.regs.w[IY]);
    /*TODO*///	unsigned src = GetMemW(DS, I.regs.w[IX]);
    /*TODO*///    SUBW(src,dst); /* opposite of the usual convention */
    /*TODO*///	I.regs.w[IY] += -4 * I.DF + 2;
    /*TODO*///	I.regs.w[IX] += -4 * I.DF + 2;
    /*TODO*///	nec_ICount-=14;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_test_ald8(void)    /* Opcode 0xa8 */
    /*TODO*///{
    /*TODO*///    DEF_ald8(dst,src);
    /*TODO*///    ANDB(dst,src);
    /*TODO*///    nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_test_axd16 = new InstructionPtr() /* Opcode 0xa9 */ {
        public void handler() {
            //DEF_axd16(dst,src);
            int src = FETCHOP();
            int dst = I.regs.w[AW];
            src += (FETCH() << 8);
            //ANDW(dst,src);
            dst &= src;
            I.CarryVal = I.OverVal = I.AuxVal = 0;
            SetSZPF_Word(dst);
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_test_axd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_stosb = new InstructionPtr() {/* Opcode 0xaa */

        public void handler() {
            int tmp = I.regs.w[IY];
            PutMemB(ES, tmp, I.regs.b[AL]);
            tmp += -2 * I.DF + 1;
            I.regs.SetW(IY, tmp & 0xFFFF);
            nec_ICount[0] -= 5;
            if (neclog != null) {
                fprintf(neclog, "i_stosb :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_stosw = new InstructionPtr() /* Opcode 0xab */ {
        public void handler() {
            int tmp = I.regs.w[IY];
            PutMemW(ES, tmp, I.regs.w[AW]);
            //	PutMemB(ES,I.regs.w[IY],I.regs.b[AL]); /* MISH */
            //	PutMemB(ES,I.regs.w[IY]+1,I.regs.b[AH]);
            tmp += -4 * I.DF + 2;
            I.regs.SetW(IY, tmp & 0xFFFF);
            nec_ICount[0] -= 5;
            if (neclog != null) {
                fprintf(neclog, "i_stosw :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_lodsb = new InstructionPtr() /* Opcode 0xac */ {
        public void handler() {
            int tmp = I.regs.w[IX];
            I.regs.SetB(AL, GetMemB(DS, tmp));//I.regs.b[AL] = GetMemB(DS,I.regs.w[IX]);
            tmp += -2 * I.DF + 1;
            I.regs.SetW(IX, tmp & 0xFFFF);//I.regs.w[IX] += -2 * I.DF + 1;
            nec_ICount[0] -= 10;
            if (neclog != null) {
                fprintf(neclog, "i_lodsb :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_lodsw(void)    /* Opcode 0xad */
    /*TODO*///{
    /*TODO*///	I.regs.w[AW] = GetMemW(DS,I.regs.w[IX]);
    /*TODO*///	I.regs.w[IX] +=  -4 * I.DF + 2;
    /*TODO*///	nec_ICount-=10;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_scasb = new InstructionPtr() {
        public void handler() {
            int tmp = I.regs.w[IY];
            int src = GetMemB(ES, tmp);
            int dst = I.regs.b[AL];
            //SUBB(dst,src);
            int res = dst - src;
            SetCFB(res);
            SetOFB_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Byte(res);
            dst = res & 0xFF;
            tmp += -2 * I.DF + 1;
            I.regs.SetW(IY, tmp & 0xFFFF);
            nec_ICount[0] -= 12;
            if (neclog != null) {
                fprintf(neclog, "i_scasb :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_scasw = new InstructionPtr() {
        public void handler() {
            int tmp = I.regs.w[IY];
            int src = GetMemW(ES, tmp);
            int dst = I.regs.w[AW];
            //SUBW(dst, src);
            /*unsigned*/
            int res = dst - src;
            SetCFW(res);
            SetOFW_Sub(res, src, dst);
            SetAF(res, src, dst);
            SetSZPF_Word(res);
            dst = res & 0xFFFF;
            tmp += -4 * I.DF + 2;
            I.regs.SetW(IY, tmp & 0xFFFF);
            nec_ICount[0] -= 12;
            if (neclog != null) {
                fprintf(neclog, "i_scasw :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_mov_ald8 = new InstructionPtr() {
        public void handler() {
            I.regs.SetB(AL, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_ald8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_cld8 = new InstructionPtr() {
        public void handler() {
            I.regs.SetB(CL, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_cld8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_mov_dld8(void)    /* Opcode 0xb2 */
    /*TODO*///{
    /*TODO*///	I.regs.b[DL] = FETCH;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_mov_bld8(void)    /* Opcode 0xb3 */
    /*TODO*///{
    /*TODO*///	I.regs.b[BL] = FETCH;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_mov_ahd8(void)    /* Opcode 0xb4 */
    /*TODO*///{
    /*TODO*///	I.regs.b[AH] = FETCH;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_mov_chd8 = new InstructionPtr() {
        public void handler() {
            I.regs.SetB(CH, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_chd8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_mov_dhd8(void)    /* Opcode 0xb6 */
    /*TODO*///{
    /*TODO*///	I.regs.b[DH] = FETCH;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_mov_bhd8(void)    /* Opcode 0xb7 */
    /*TODO*///{
    /*TODO*///	I.regs.b[BH] = FETCH;
    /*TODO*///	nec_ICount-=4;
    /*TODO*///}
    /*TODO*///
    /*OK*/    static InstructionPtr i_mov_axd16 = new InstructionPtr() /* Opcode 0xb8 */ {
        public void handler() {
            I.regs.SetB(AL, FETCH());//I.regs.b[AL] = FETCH;
            I.regs.SetB(AH, FETCH());//I.regs.b[AH] = FETCH;
            nec_ICount[0] -= 4;
            /*if (neclog != null) {
             fprintf(neclog, "i_mov_axd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    /*OK*/
    static InstructionPtr i_mov_cxd16 = new InstructionPtr() /* Opcode 0xb9 */ {
        public void handler() {

            I.regs.SetB(CL, FETCH());//I.regs.b[CL] = FETCH;
            I.regs.SetB(CH, FETCH());//I.regs.b[CH] = FETCH;
            nec_ICount[0] -= 4;

            /*if (neclog != null) {
             fprintf(neclog, "i_mov_cxd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_mov_dxd16 = new InstructionPtr() /* Opcode 0xba */ {
        public void handler() {
            I.regs.SetB(DL, FETCH());
            I.regs.SetB(DH, FETCH());
            nec_ICount[0] -= 4;
            /*if (neclog != null) {
             fprintf(neclog, "i_mov_dxd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_mov_bxd16 = new InstructionPtr() /* Opcode 0xbb */ {
        public void handler() {
            I.regs.SetB(BL, FETCH());
            I.regs.SetB(BH, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_bxd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*OK*/
    static InstructionPtr i_mov_spd16 = new InstructionPtr() /* Opcode 0xbc */ {
        public void handler() {
            I.regs.SetB(SPL, FETCH());
            I.regs.SetB(SPH, FETCH());
            nec_ICount[0] -= 4;
            /*if (neclog != null) {
             fprintf(neclog, "i_mov_spd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
             }*/
        }
    };
    static InstructionPtr i_mov_bpd16 = new InstructionPtr() /* Opcode 0xbd */ {
        public void handler() {
            I.regs.SetB(BPL, FETCH());
            I.regs.SetB(BPH, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_bpd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_sid16 = new InstructionPtr() /* Opcode 0xbe */ {
        public void handler() {
            I.regs.SetB(IXL, FETCH());
            I.regs.SetB(IXH, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_sid16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_mov_did16 = new InstructionPtr() /* Opcode 0xbf */ {
        public void handler() {
            I.regs.SetB(IYL, FETCH());
            I.regs.SetB(IYH, FETCH());
            nec_ICount[0] -= 4;
            if (neclog != null) {
                fprintf(neclog, "i_mov_did16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///void nec_rotate_shift_Byte(unsigned ModRM, int count)
    /*TODO*///{
    /*TODO*///  unsigned src = (unsigned)GetRMByte(ModRM);
    /*TODO*///  unsigned dst=src;
    /*TODO*///
    /*TODO*///	if (count < 0) /* FETCH must come _after_ GetRMWord */
    /*TODO*///		count = FETCH;
    /*TODO*///
    /*TODO*///  if (count==0)
    /*TODO*///  {
    /*TODO*///	nec_ICount-=8; /* or 7 if dest is in memory */
    /*TODO*///  }
    /*TODO*///  else if (count==1)
    /*TODO*///  {
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:16;
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///      case 0x00:  /* ROL eb,1 */
    /*TODO*///	I.CarryVal = src & 0x80;
    /*TODO*///        dst=(src<<1)+CF;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	I.OverVal = (src^dst)&0x80;
    /*TODO*///	break;
    /*TODO*///      case 0x08:  /* ROR eb,1 */
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///        dst = ((CF<<8)+src) >> 1;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	I.OverVal = (src^dst)&0x80;
    /*TODO*///	break;
    /*TODO*///      case 0x10:  /* RCL eb,1 */
    /*TODO*///        dst=(src<<1)+CF;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///        SetCFB(dst);
    /*TODO*///	I.OverVal = (src^dst)&0x80;
    /*TODO*///	break;
    /*TODO*///      case 0x18:  /* RCR eb,1 */
    /*TODO*///        dst = ((CF<<8)+src) >> 1;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = (src^dst)&0x80;
    /*TODO*///	break;
    /*TODO*///      case 0x20:  /* SHL eb,1 */
    /*TODO*///      case 0x30:
    /*TODO*///        dst = src << 1;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///        SetCFB(dst);
    /*TODO*///	I.OverVal = (src^dst)&0x80;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///	break;
    /*TODO*///      case 0x28:  /* SHR eb,1 */
    /*TODO*///        dst = src >> 1;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = src & 0x80;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///	break;
    /*TODO*///      case 0x38:  /* SAR eb,1 */
    /*TODO*///        dst = ((INT8)src) >> 1;
    /*TODO*///        PutbackRMByte(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = 0;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///	break;
    /*TODO*///    }
    /*TODO*///  }
    /*TODO*///  else
    /*TODO*///  {
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?7+4*count:19+4*count;
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///      case 0x00:  /* ROL eb,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///	  I.CarryVal = dst & 0x80;
    /*TODO*///          dst = (dst << 1) + CF;
    /*TODO*///	}
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///     case 0x08:  /* ROR eb,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///	  I.CarryVal = dst & 0x01;
    /*TODO*///          dst = (dst >> 1) + (CF << 7);
    /*TODO*///	}
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///      case 0x10:  /* RCL eb,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///          dst = (dst << 1) + CF;
    /*TODO*///          SetCFB(dst);
    /*TODO*///	}
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///      case 0x18:  /* RCR eb,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///          dst = (CF<<8)+dst;
    /*TODO*///	  I.CarryVal = dst & 0x01;
    /*TODO*///          dst >>= 1;
    /*TODO*///	}
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///      case 0x20:
    /*TODO*///      case 0x30:  /* SHL eb,count */
    /*TODO*///        dst <<= count;
    /*TODO*///        SetCFB(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///      case 0x28:  /* SHR eb,count */
    /*TODO*///        dst >>= count-1;
    /*TODO*///	I.CarryVal = dst & 0x1;
    /*TODO*///        dst >>= 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///      case 0x38:  /* SAR eb,count */
    /*TODO*///        dst = ((INT8)dst) >> (count-1);
    /*TODO*///	I.CarryVal = dst & 0x1;
    /*TODO*///        dst = ((INT8)((BYTE)dst)) >> 1;
    /*TODO*///        SetSZPF_Byte(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        PutbackRMByte(ModRM,(BYTE)dst);
    /*TODO*///	break;
    /*TODO*///    }
    /*TODO*///  }
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///void nec_rotate_shift_Word(unsigned ModRM, int count)
    /*TODO*///{
    /*TODO*///	unsigned src = GetRMWord(ModRM);
    /*TODO*///	unsigned dst=src;
    /*TODO*///
    /*TODO*///	if (count < 0) /* FETCH must come _after_ GetRMWord */
    /*TODO*///		count = FETCH;
    /*TODO*///
    /*TODO*///  if (count==0)
    /*TODO*///  {
    /*TODO*///	nec_ICount-=8; /* or 7 if dest is in memory */
    /*TODO*///  }
    /*TODO*///  else if (count==1)
    /*TODO*///  {
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///#if 0
    /*TODO*///      case 0x00:  /* ROL ew,1 */
    /*TODO*///        tmp2 = (tmp << 1) + CF;
    /*TODO*///	SetCFW(tmp2);
    /*TODO*///	I.OverVal = !(!(tmp & 0x4000)) != CF;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///      case 0x08:  /* ROR ew,1 */
    /*TODO*///	I.CarryVal = tmp & 0x01;
    /*TODO*///	tmp2 = (tmp >> 1) + ((unsigned)CF << 15);
    /*TODO*///	I.OverVal = !(!(tmp & 0x8000)) != CF;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///      case 0x10:  /* RCL ew,1 */
    /*TODO*///	tmp2 = (tmp << 1) + CF;
    /*TODO*///	SetCFW(tmp2);
    /*TODO*///	I.OverVal = (tmp ^ (tmp << 1)) & 0x8000;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///      case 0x18:  /* RCR ew,1 */
    /*TODO*///	tmp2 = (tmp >> 1) + ((unsigned)CF << 15);
    /*TODO*///	I.OverVal = !(!(tmp & 0x8000)) != CF;
    /*TODO*///	I.CarryVal = tmp & 0x01;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///      case 0x20:  /* SHL ew,1 */
    /*TODO*///      case 0x30:
    /*TODO*///	tmp <<= 1;
    /*TODO*///	SetCFW(tmp);
    /*TODO*///	SetOFW_Add(tmp,tmp2,tmp2);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///	SetSZPF_Word(tmp);
    /*TODO*///	PutbackRMWord(ModRM,tmp);
    /*TODO*///	break;
    /*TODO*///      case 0x28:  /* SHR ew,1 */
    /*TODO*///	I.CarryVal = tmp & 0x01;
    /*TODO*///	I.OverVal = tmp & 0x8000;
    /*TODO*///	tmp2 = tmp >> 1;
    /*TODO*///	SetSZPF_Word(tmp2);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///      case 0x38:  /* SAR ew,1 */
    /*TODO*///	I.CarryVal = tmp & 0x01;
    /*TODO*///	I.OverVal = 0;
    /*TODO*///	tmp2 = (tmp >> 1) | (tmp & 0x8000);
    /*TODO*///	SetSZPF_Word(tmp2);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///	PutbackRMWord(ModRM,tmp2);
    /*TODO*///	break;
    /*TODO*///#else
    /*TODO*///      case 0x00:  /* ROL ew,1 */
    /*TODO*///	I.CarryVal = src & 0x8000;
    /*TODO*///        dst=(src<<1)+CF;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	I.OverVal = (src^dst)&0x8000;
    /*TODO*///	break;
    /*TODO*///      case 0x08:  /* ROR ew,1 */
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///        dst = ((CF<<16)+src) >> 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	I.OverVal = (src^dst)&0x8000;
    /*TODO*///	break;
    /*TODO*///      case 0x10:  /* RCL ew,1 */
    /*TODO*///        dst=(src<<1)+CF;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///        SetCFW(dst);
    /*TODO*///	I.OverVal = (src^dst)&0x8000;
    /*TODO*///	break;
    /*TODO*///      case 0x18:  /* RCR ew,1 */
    /*TODO*///        dst = ((CF<<16)+src) >> 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = (src^dst)&0x8000;
    /*TODO*///	break;
    /*TODO*///      case 0x20:  /* SHL ew,1 */
    /*TODO*///      case 0x30:
    /*TODO*///        dst = src << 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///        SetCFW(dst);
    /*TODO*///	I.OverVal = (src^dst)&0x8000;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///	break;
    /*TODO*///      case 0x28:  /* SHR ew,1 */
    /*TODO*///        dst = src >> 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = src & 0x8000;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///	break;
    /*TODO*///      case 0x38:  /* SAR ew,1 */
    /*TODO*///        dst = ((INT16)src) >> 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	I.CarryVal = src & 0x01;
    /*TODO*///	I.OverVal = 0;
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///	break;
    /*TODO*///#endif
    /*TODO*///    }
    /*TODO*///  }
    /*TODO*///  else
    /*TODO*///  {
    /*TODO*///
    /*TODO*///    nec_ICount-=(ModRM >=0xc0 )?7+count*4:27+count*4;
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///      case 0x00:  /* ROL ew,count */
    /*TODO*///     	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///	  I.CarryVal = dst & 0x8000;
    /*TODO*///          dst = (dst << 1) + CF;
    /*TODO*///	}
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x08:  /* ROR ew,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///	  I.CarryVal = dst & 0x01;
    /*TODO*///          dst = (dst >> 1) + (CF << 15);
    /*TODO*///	}
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x10:  /* RCL ew,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///          dst = (dst << 1) + CF;
    /*TODO*///          SetCFW(dst);
    /*TODO*///	}
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x18:  /* RCR ew,count */
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///	{
    /*TODO*///          dst = dst + (CF << 16);
    /*TODO*///	  I.CarryVal = dst & 0x01;
    /*TODO*///           dst >>= 1;
    /*TODO*///	}
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x20:
    /*TODO*///      case 0x30:  /* SHL ew,count */
    /*TODO*///        dst <<= count;
    /*TODO*///        SetCFW(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x28:  /* SHR ew,count */
    /*TODO*///        dst >>= count-1;
    /*TODO*///	I.CarryVal = dst & 0x1;
    /*TODO*///        dst >>= 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///      case 0x38:  /* SAR ew,count */
    /*TODO*///        dst = ((INT16)dst) >> (count-1);
    /*TODO*///	I.CarryVal = dst & 0x01;
    /*TODO*///        dst = ((INT16)((WORD)dst)) >> 1;
    /*TODO*///        SetSZPF_Word(dst);
    /*TODO*///	I.AuxVal = 1;
    /*TODO*///        PutbackRMWord(ModRM,dst);
    /*TODO*///	break;
    /*TODO*///    }
    /*TODO*///  }
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_rotshft_bd8(void)    /* Opcode 0xc0 */
    /*TODO*///{
    /*TODO*///    unsigned ModRM = FETCH;
    /*TODO*///    nec_rotate_shift_Byte(ModRM,-1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_rotshft_wd8(void)    /* Opcode 0xc1 */
    /*TODO*///{
    /*TODO*///    unsigned ModRM = FETCH;
    /*TODO*///    nec_rotate_shift_Word(ModRM,-1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_ret_d16(void)    /* Opcode 0xc2 */
    /*TODO*///{
    /*TODO*///	unsigned count = FETCH;
    /*TODO*///	count += FETCH << 8;
    /*TODO*///	POP(I.ip);
    /*TODO*///	I.regs.w[SP]+=count;
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///	nec_ICount-=22;	// near 20-24
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_ret = new InstructionPtr() {
        public void handler() {
            I.ip = POP();
            change_pc20((I.base[CS] + I.ip));
            nec_ICount[0] -= 17; // near 15-19
            if (neclog != null) {
                fprintf(neclog, "i_ret :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_les_dw(void)    /* Opcode 0xc4 */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    WORD tmp = GetRMWord(ModRM);
    /*TODO*///
    /*TODO*///    RegWord(ModRM)= tmp;
    /*TODO*///    I.sregs[ES] = GetnextRMWord;
    /*TODO*///    I.base[ES] = SegBase(ES);
    /*TODO*///    nec_ICount-=22;   /* 18-26 */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_lds_dw(void)    /* Opcode 0xc5 */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    WORD tmp = GetRMWord(ModRM);
    /*TODO*///
    /*TODO*///    RegWord(ModRM)=tmp;
    /*TODO*///    I.sregs[DS] = GetnextRMWord;
    /*TODO*///    I.base[DS] = SegBase(DS);
    /*TODO*///    nec_ICount-=22;   /* 18-26 */
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_mov_bd8 = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            PutImmRMByte(ModRM);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 4 : 11;
            if (neclog != null) {
                fprintf(neclog, "i_mov_bd8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_mov_wd16 = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            PutImmRMWord(ModRM);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 4 : 15;
            if (neclog != null) {
                fprintf(neclog, "i_mov_wd16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    /*TODO*///static void i_enter(void)    /* Opcode 0xc8 */
    /*TODO*///{
    /*TODO*///    unsigned nb = FETCH;
    /*TODO*///    unsigned i,level;
    /*TODO*///
    /*TODO*///    nec_ICount-=23;
    /*TODO*///    nb += FETCH << 8;
    /*TODO*///    level = FETCH;
    /*TODO*///    PUSH(I.regs.w[BP]);
    /*TODO*///    I.regs.w[BP]=I.regs.w[SP];
    /*TODO*///    I.regs.w[SP] -= nb;
    /*TODO*///    for (i=1;i<level;i++) {
    /*TODO*///	PUSH(GetMemW(SS,I.regs.w[BP]-i*2));
    /*TODO*///	nec_ICount-=16;
    /*TODO*///    }
    /*TODO*///    if (level) PUSH(I.regs.w[BP]);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_leave(void)    /* Opcode 0xc9 */
    /*TODO*///{
    /*TODO*///	I.regs.w[SP]=I.regs.w[BP];
    /*TODO*///	POP(I.regs.w[BP]);
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_retf_d16(void)    /* Opcode 0xca */
    /*TODO*///{
    /*TODO*///	unsigned count = FETCH;
    /*TODO*///	count += FETCH << 8;
    /*TODO*///	POP(I.ip);
    /*TODO*///	POP(I.sregs[CS]);
    /*TODO*///	I.base[CS] = SegBase(CS);
    /*TODO*///	I.regs.w[SP]+=count;
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///	nec_ICount-=25; // 21-29
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_retf(void)    /* Opcode 0xcb */
    /*TODO*///{
    /*TODO*///	POP(I.ip);
    /*TODO*///	POP(I.sregs[CS]);
    /*TODO*///	I.base[CS] = SegBase(CS);
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///	nec_ICount-=28;	// 24-32
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_int3(void)    /* Opcode 0xcc */
    /*TODO*///{
    /*TODO*///	nec_ICount-=38;	// 38-50
    /*TODO*///	nec_interrupt(3,0);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_int(void)    /* Opcode 0xcd */
    /*TODO*///{
    /*TODO*///	unsigned int_num = FETCH;
    /*TODO*///	nec_ICount-=38;	// 38-50
    /*TODO*///	nec_interrupt(int_num,0);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_into(void)    /* Opcode 0xce */
    /*TODO*///{
    /*TODO*///    if (OF) {
    /*TODO*///	nec_ICount-=52;
    /*TODO*///	nec_interrupt(4,0);
    /*TODO*///    } else nec_ICount-=3;   /* 3 or 52! */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_iret(void)    /* Opcode 0xcf */
    /*TODO*///{
    /*TODO*///	POP(I.ip);
    /*TODO*///	POP(I.sregs[CS]);
    /*TODO*///	I.base[CS] = SegBase(CS);
    /*TODO*///    	i_popf();
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///	nec_ICount-=32;	// 27-39
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_rotshft_b(void)    /* Opcode 0xd0 */
    /*TODO*///{
    /*TODO*///	nec_rotate_shift_Byte(FETCH,1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_rotshft_w(void)    /* Opcode 0xd1 */
    /*TODO*///{
    /*TODO*///	nec_rotate_shift_Word(FETCH,1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_rotshft_bcl(void)    /* Opcode 0xd2 */
    /*TODO*///{
    /*TODO*///	nec_rotate_shift_Byte(FETCH,I.regs.b[CL]);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_rotshft_wcl(void)    /* Opcode 0xd3 */
    /*TODO*///{
    /*TODO*///	nec_rotate_shift_Word(FETCH,I.regs.b[CL]);
    /*TODO*///}
    /*TODO*///
    /*TODO*////* OB: Opcode works on NEC V-Series but not the Variants 		*/
    /*TODO*////*     one could specify any byte value as operand but the NECs */
    /*TODO*////*     always substitute 0x0a.									*/
    /*TODO*///static void i_aam(void)   	/* Opcode 0xd4 */
    /*TODO*///{
    /*TODO*///	unsigned mult=FETCH;
    /*TODO*///
    /*TODO*///	if (mult == 0)
    /*TODO*///		nec_interrupt(0,0);
    /*TODO*///    else
    /*TODO*///    {
    /*TODO*///		I.regs.b[AH] = I.regs.b[AL] / 10;
    /*TODO*///		I.regs.b[AL] %= 10;
    /*TODO*///		SetSZPF_Word(I.regs.w[AW]);
    /*TODO*///		nec_ICount-=15;
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*////* OB: Opcode works on NEC V-Series but not the Variants 	*/
    /*TODO*////*     one could specify any byte value as operand but the NECs */
    /*TODO*////*     always substitute 0x0a.					*/
    /*TODO*///static void i_aad(void)    	/* Opcode 0xd5 */
    /*TODO*///{
    /*TODO*///	unsigned mult=FETCH;				/* eat operand = ignore ! */
    /*TODO*///
    /*TODO*///	I.regs.b[AL] = I.regs.b[AH] * 10 + I.regs.b[AL];
    /*TODO*///	I.regs.b[AH] = 0;
    /*TODO*///
    /*TODO*///	SetZF(I.regs.b[AL]);
    /*TODO*///	SetPF(I.regs.b[AL]);
    /*TODO*///	I.SignVal = 0;
    /*TODO*///	nec_ICount-=7;
    /*TODO*///	mult=0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_setalc(void)  /* Opcode 0xd6 */
    /*TODO*///{
    /*TODO*///	/*
    /*TODO*///	----------O-SETALC---------------------------------
    /*TODO*///	OPCODE SETALC  - Set AL to Carry Flag
    /*TODO*///
    /*TODO*///	CPU:  Intel 8086 and all its clones and upward
    /*TODO*///	    compatibility chips.
    /*TODO*///	Type of Instruction: User
    /*TODO*///
    /*TODO*///	Instruction: SETALC
    /*TODO*///
    /*TODO*///	Description:
    /*TODO*///
    /*TODO*///		IF (CF=0) THEN AL:=0 ELSE AL:=FFH;
    /*TODO*///
    /*TODO*///	Flags Affected: None
    /*TODO*///
    /*TODO*///	CPU mode: RM,PM,VM,SMM
    /*TODO*///
    /*TODO*///	Physical Form:		 SETALC
    /*TODO*///	COP (Code of Operation): D6H
    /*TODO*///	Clocks:	      80286    : n/a   [3]
    /*TODO*///		      80386    : n/a   [3]
    /*TODO*///		     Cx486SLC  : n/a   [2]
    /*TODO*///		      i486     : n/a   [3]
    /*TODO*///		      Pentium  : n/a   [3]
    /*TODO*///	Note: n/a is Time that Intel etc not say.
    /*TODO*///	      [3] is real time it executed.
    /*TODO*///
    /*TODO*///	*/
    /*TODO*///	I.regs.b[AL] = (CF)?0xff:0x00;
    /*TODO*///	nec_ICount-=3;	// V30
    /*TODO*///	if (errorlog) fprintf(errorlog,"PC=%06x : SETALC\n",cpu_get_pc()-1);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_xlat(void)    /* Opcode 0xd7 */
    /*TODO*///{
    /*TODO*///	unsigned dest = I.regs.w[BW]+I.regs.b[AL];
    /*TODO*///	I.regs.b[AL] = GetMemB(DS, dest);
    /*TODO*///	nec_ICount-=9;	// V30
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_escape(void)    /* Opcodes 0xd8, 0xd9, 0xda, 0xdb, 0xdc, 0xdd, 0xde and 0xdf */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///	nec_ICount-=2;	// dont found any info :-(, set same as hlt
    /*TODO*///    	GetRMByte(ModRM);
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_loopne(void)    /* Opcode 0xe0 */
    /*TODO*///{
    /*TODO*///    int disp = (int)((INT8)FETCH);
    /*TODO*///    unsigned tmp = I.regs.w[CW]-1;
    /*TODO*///
    /*TODO*///    I.regs.w[CW]=tmp;
    /*TODO*///
    /*TODO*///    if (!ZF && tmp) {
    /*TODO*///	nec_ICount-=14;
    /*TODO*///	I.ip = (WORD)(I.ip+disp);
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///    } else nec_ICount-=5;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_loope = new InstructionPtr() {
        public void handler() {
            int disp = (int) ((byte) FETCH());
            /*unsigned*/ int tmp = (I.regs.w[CW] - 1) & 0xFFFF;
            I.regs.SetW(CW, tmp);

            if (ZF() != 0 && tmp != 0) {
                nec_ICount[0] -= 14;
                I.ip = (I.ip + disp) & 0xFFFF;
                change_pc20((I.base[CS] + I.ip));
            } else {
                nec_ICount[0] -= 5;
            }
            if (neclog != null) {
                fprintf(neclog, "i_loope :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };


    /*ok*/    static InstructionPtr i_loop = new InstructionPtr()/* Opcode 0xe2 */ {
        public void handler() {
            int disp = (int) ((byte) FETCH());
            /*unsigned*/ int tmp = (I.regs.w[CW] - 1) & 0xFFFF;
            I.regs.SetW(CW, tmp);
            if (tmp != 0) {
                nec_ICount[0] -= 13;
                I.ip = (I.ip + disp) & 0xFFFF;
                change_pc20((I.base[CS] + I.ip));
            } else {
                nec_ICount[0] -= 5;
            }
            /*if (neclog != null) {
             fprintf(neclog, "i_loop :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip,I.regs.w[AW],I.regs.w[CW],I.regs.w[DW],I.regs.w[BW],I.regs.w[SP],I.regs.w[BP],I.regs.w[IX],I.regs.w[IY],I.base[0],I.base[1],I.base[2],I.base[3],I.sregs[0],I.sregs[1],I.sregs[2],I.sregs[3],I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal,I.TF, I.IF, I.DF, I.MF,I.int_vector,I.pending_irq,I.nmi_state,I.irq_state,I.prefix_base,I.seg_prefix,EA);
             }*/
        }
    };
    /*TODO*///
    /*TODO*///static void i_jcxz(void)    /* Opcode 0xe3 */
    /*TODO*///{
    /*TODO*///	int disp = (int)((INT8)FETCH);
    /*TODO*///	if (I.regs.w[CW] == 0) {
    /*TODO*///	nec_ICount-=13;
    /*TODO*///	I.ip = (WORD)(I.ip+disp);
    /*TODO*///	change_pc20((I.base[CS]+I.ip));
    /*TODO*///	} else nec_ICount-=5;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_inal(void)    /* Opcode 0xe4 */
    /*TODO*///{
    /*TODO*///	unsigned port = FETCH;
    /*TODO*///	I.regs.b[AL] = read_port(port);
    /*TODO*///	nec_ICount-=9;
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_inax = new InstructionPtr() /* Opcode 0xe5 */ {
        public void handler() {
            /*unsigned*/
            int port = FETCH();
            I.regs.SetB(AL, read_port(port));
            I.regs.SetB(AH, read_port(port + 1));
            nec_ICount[0] -= 13;
            if (neclog != null) {
                fprintf(neclog, "i_inax :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_outal = new InstructionPtr() {
        public void handler() {
            int port = FETCH();
            write_port(port, I.regs.b[AL]);
            nec_ICount[0] -= 8;
            if (neclog != null) {
                fprintf(neclog, "i_outal :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_outax = new InstructionPtr() /* Opcode 0xe7 */ {
        public void handler() {
            /*unsigned*/
            int port = FETCH();
            write_port(port, I.regs.b[AL]);
            write_port(port + 1, I.regs.b[AH]);
            nec_ICount[0] -= 12;
            if (neclog != null) {
                fprintf(neclog, "i_outax :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_call_d16 = new InstructionPtr() {
        public void handler() {
            int tmp = FETCH();
            tmp += FETCH() << 8;

            PUSH(I.ip);
            I.ip = (I.ip + (short) tmp) & 0xFFFF;
            change_pc20((I.base[CS] + I.ip));
            nec_ICount[0] -= 24; // 21-29
            if (neclog != null) {
                fprintf(neclog, "i_call_d16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };

    static InstructionPtr i_jmp_d16 = new InstructionPtr() /* Opcode 0xe9 */ {
        public void handler() {

            int tmp = FETCH();
            tmp += FETCH() << 8;
            I.ip = (I.ip + (short) tmp) & 0xFFFF;
            change_pc20((I.base[CS] + I.ip));
            nec_ICount[0] -= 15;
            if (neclog != null) {
                fprintf(neclog, "i_jmp_d16 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_jmp_far = new InstructionPtr() /* Opcode 0xea */ {
        public void handler() {
            /*unsigned*/
            int tmp, tmp1;

            tmp = FETCH();
            tmp += FETCH() << 8;

            tmp1 = FETCH();
            tmp1 += FETCH() << 8;

            I.sregs[CS] = tmp1 & 0xFFFF;
            I.base[CS] = SegBase(CS);
            I.ip = tmp & 0xFFFF;
            change_pc20((I.base[CS] + I.ip));
            nec_ICount[0] -= 27; // 27-35
            if (neclog != null) {
                fprintf(neclog, "i_jmp_far :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    static InstructionPtr i_jmp_d8 = new InstructionPtr() {
        public void handler() {
            int tmp = (int) ((byte) FETCH());
            I.ip = (I.ip + tmp) & 0xFFFF;
            nec_ICount[0] -= 12;
            if (neclog != null) {
                fprintf(neclog, "i_jmp_d8 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };


    /*TODO*///static void i_inaldx(void)    /* Opcode 0xec */
    /*TODO*///{
    /*TODO*///	I.regs.b[AL] = read_port(I.regs.w[DW]);
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_inaxdx(void)    /* Opcode 0xed */
    /*TODO*///{
    /*TODO*///	unsigned port = I.regs.w[DW];
    /*TODO*///
    /*TODO*///	I.regs.b[AL] = read_port(port);
    /*TODO*///	I.regs.b[AH] = read_port(port+1);
    /*TODO*///	nec_ICount-=12;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_outdxal(void)    /* Opcode 0xee */
    /*TODO*///{
    /*TODO*///	write_port(I.regs.w[DW], I.regs.b[AL]);
    /*TODO*///	nec_ICount-=8;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_outdxax(void)    /* Opcode 0xef */
    /*TODO*///{
    /*TODO*///	unsigned port = I.regs.w[DW];
    /*TODO*///	write_port(port, I.regs.b[AL]);
    /*TODO*///	write_port(port+1, I.regs.b[AH]);
    /*TODO*///	nec_ICount-=12;
    /*TODO*///}
    /*TODO*///
    /*TODO*////* I think thats not a V20 instruction...*/
    /*TODO*///static void i_lock(void)    /* Opcode 0xf0 */
    /*TODO*///{
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	nec_instruction[FETCHOP]();  /* un-interruptible */
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_brks(void) 	/* Opcode 0xf1 - Break to Security Mode */
    /*TODO*///{
    /*TODO*///	/*
    /*TODO*///	CPU:  NEC (V25/V35) Software Guard  only
    /*TODO*///	Instruction:  BRKS int_vector
    /*TODO*///
    /*TODO*///	Description:
    /*TODO*///		     [sp-1,sp-2] <- PSW		; PSW EQU FLAGS
    /*TODO*///		     [sp-3,sp-4] <- PS		; PS  EQU CS
    /*TODO*///		     [sp-5,sp-6] <- PC		; PC  EQU IP
    /*TODO*///		     SP	 <-  SP -6
    /*TODO*///		     IE	 <-  0
    /*TODO*///		     BRK <-  0
    /*TODO*///		     MD	 <-  0
    /*TODO*///		     PC	 <- [int_vector*4 +0,+1]
    /*TODO*///		     PS	 <- [int_vector*4 +2,+3]
    /*TODO*///
    /*TODO*///	Note:	The BRKS instruction switches operations in Security Mode
    /*TODO*///		via Interrupt call. In Security Mode the fetched operation
    /*TODO*///		code is executed after conversion in accordance with build-in
    /*TODO*///		translation table
    /*TODO*///
    /*TODO*///	Flags Affected:	 None
    /*TODO*///
    /*TODO*///	CPU mode: RM
    /*TODO*///
    /*TODO*///	+++++++++++++++++++++++
    /*TODO*///	Physical Form:	BRKS  imm8
    /*TODO*///	Clocks:	 56+10T [44+10T]
    /*TODO*///*/
    /*TODO*///	unsigned int_vector;
    /*TODO*///	int_vector=FETCH;
    /*TODO*///	if (errorlog) fprintf(errorlog,"PC=%06x : BRKS %02x\n",cpu_get_pc()-2,int_vector);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    static void rep(int flagval) {
        /* Handles rep- and repnz- prefixes. flagval is the value of ZF for the
         loop  to continue for CMPS and SCAS instructions. */

        /*unsigned*/ int next = FETCHOP();
        /*unsigned*/
        int count = I.regs.w[CW];

        switch (next) {
            /*TODO*///    	case 0x26:  /* ES: */
    /*TODO*///			I.seg_prefix=TRUE;
    /*TODO*///			I.prefix_base=I.base[ES];
    /*TODO*///			nec_ICount-=2;
    /*TODO*///			rep(flagval);
    /*TODO*///			break;
    /*TODO*///	    case 0x2e:  /* CS: */
    /*TODO*///			I.seg_prefix=TRUE;
    /*TODO*///			I.prefix_base=I.base[CS];
    /*TODO*///			nec_ICount-=2;
    /*TODO*///			rep(flagval);
    /*TODO*///			break;
    /*TODO*///    case 0x36:  /* SS: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///	I.prefix_base=I.base[SS];
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	rep(flagval);
    /*TODO*///	break;
    /*TODO*///    case 0x3e:  /* DS: */
    /*TODO*///        I.seg_prefix=TRUE;
    /*TODO*///	I.prefix_base=I.base[DS];
    /*TODO*///	nec_ICount-=2;
    /*TODO*///	rep(flagval);
    /*TODO*///	break;
    /*TODO*///    case 0x6c:  /* REP INSB */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///        	i_insb();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0x6d:  /* REP INSW */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///        	i_insw();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0x6e:  /* REP OUTSB */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///           i_outsb();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0x6f:  /* REP OUTSW */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///            i_outsw();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0xa4:  /* REP MOVSB */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///		i_movsb();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0xa5:  /* REP MOVSW */
    /*TODO*///	nec_ICount-=9-count;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///		i_movsw();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0xa6:  /* REP(N)E CMPSB */
    /*TODO*///	nec_ICount-=9;
    /*TODO*///	for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
    /*TODO*///		i_cmpsb();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0xa7:  /* REP(N)E CMPSW */
    /*TODO*///	nec_ICount-=9;
    /*TODO*///	for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
    /*TODO*///		i_cmpsw();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
            case 0xaa:  /* REP STOSB */

                nec_ICount[0] -= 9 - count;
                for (; count > 0; count--) {
                    i_stosb.handler();
                }
                I.regs.SetW(CW, count);
                if (neclog != null) {
                    fprintf(neclog, "i_rep_0xaa :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                }
                break;
            case 0xab:  /* REP STOSW */

                nec_ICount[0] -= 9 - count;
                for (; count > 0; count--) {
                    i_stosw.handler();
                }
                I.regs.SetW(CW, count);
                if (neclog != null) {
                    fprintf(neclog, "i_rep_0xab :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                }
                break;
            /*TODO*///    case 0xac:  /* REP LODSB */
    /*TODO*///	nec_ICount-=9;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///		i_lodsb();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
    /*TODO*///    case 0xad:  /* REP LODSW */
    /*TODO*///	nec_ICount-=9;
    /*TODO*///	for (; count > 0; count--)
    /*TODO*///		i_lodsw();
    /*TODO*///	I.regs.w[CW]=count;
    /*TODO*///	break;
            case 0xae:  /* REP(N)E SCASB */

                nec_ICount[0] -= 9;
                for (I.ZeroVal = NOT(flagval); (ZF() == flagval) && (count > 0); count--) {
                    i_scasb.handler();
                }
                I.regs.SetW(CW, count);
                if (neclog != null) {
                    fprintf(neclog, "i_rep_0xae :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                }
                break;
            case 0xaf:  /* REP(N)E SCASW */

                nec_ICount[0] -= 9;
                for (I.ZeroVal = NOT(flagval); (ZF() == flagval) && (count > 0); count--) {
                    i_scasw.handler();
                }
                I.regs.SetW(CW, count);
                if (neclog != null) {
                    fprintf(neclog, "i_rep_0xaf :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                }
                break;
            default:
                System.out.println("(neednext) rep 0x" + Integer.toHexString(next));
            //nec_instruction[next].handler();
        }
    }

    /*TODO*///static void i_repne(void)    /* Opcode 0xf2 */
    /*TODO*///{
    /*TODO*///    rep(0);
    /*TODO*///}
    /*TODO*///
    static InstructionPtr i_repe = new InstructionPtr() /* Opcode 0xf3 */ {
        public void handler() {
            rep(1);
        }
    };
    /*TODO*///static void i_hlt(void)    /* Opcode 0xf4 */
    /*TODO*///{
    /*TODO*///	nec_ICount=0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_cmc(void)    /* Opcode 0xf5 */
    /*TODO*///{
    /*TODO*///	I.CarryVal = !CF;
    /*TODO*///	nec_ICount-=2;
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void i_f6pre(void)
    /*TODO*///{
    /*TODO*///	/* Opcode 0xf6 */
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    unsigned tmp = (unsigned)GetRMByte(ModRM);
    /*TODO*///    unsigned tmp2;
    /*TODO*///
    /*TODO*///
    /*TODO*///    switch (ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///    case 0x00:  /* TEST Eb, data8 */
    /*TODO*///    case 0x08:  /* ??? */
    /*TODO*///		tmp &= FETCH;
    /*TODO*///		I.CarryVal = I.OverVal = I.AuxVal = 0;
    /*TODO*///		SetSZPF_Byte(tmp);
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?4:11;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x10:  /* NOT Eb */
    /*TODO*///		PutbackRMByte(ModRM,~tmp);
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?2:16;
    /*TODO*///		break;
    /*TODO*///    case 0x18:  /* NEG Eb */
    /*TODO*///        	tmp2=0;
    /*TODO*///        	SUBB(tmp2,tmp);
    /*TODO*///        	PutbackRMByte(ModRM,tmp2);
    /*TODO*///        	nec_ICount-=(ModRM >=0xc0 )?2:16;
    /*TODO*///		break;
    /*TODO*///    case 0x20:  /* MUL AL, Eb */
    /*TODO*///		{
    /*TODO*///			UINT16 result;
    /*TODO*///			tmp2 = I.regs.b[AL];
    /*TODO*///
    /*TODO*///			SetSF((INT8)tmp2);
    /*TODO*///			SetPF(tmp2);
    /*TODO*///
    /*TODO*///			result = (UINT16)tmp2*tmp;
    /*TODO*///			I.regs.w[AW]=(WORD)result;
    /*TODO*///
    /*TODO*///			SetZF(I.regs.w[AW]);
    /*TODO*///			I.CarryVal = I.OverVal = (I.regs.b[AH] != 0);
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?30:36;
    /*TODO*///		break;
    /*TODO*///    case 0x28:  /* IMUL AL, Eb */
    /*TODO*///		{
    /*TODO*///			INT16 result;
    /*TODO*///
    /*TODO*///			tmp2 = (unsigned)I.regs.b[AL];
    /*TODO*///
    /*TODO*///			SetSF((INT8)tmp2);
    /*TODO*///			SetPF(tmp2);
    /*TODO*///
    /*TODO*///			result = (INT16)((INT8)tmp2)*(INT16)((INT8)tmp);
    /*TODO*///			I.regs.w[AW]=(WORD)result;
    /*TODO*///
    /*TODO*///			SetZF(I.regs.w[AW]);
    /*TODO*///
    /*TODO*///			I.CarryVal = I.OverVal = (result >> 7 != 0) && (result >> 7 != -1);
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?30:39;
    /*TODO*///		break;
    /*TODO*///    case 0x30:  /* IYV AL, Ew */
    /*TODO*///		{
    /*TODO*///			UINT16 result;
    /*TODO*///
    /*TODO*///			result = I.regs.w[AW];
    /*TODO*///
    /*TODO*///			if (tmp)
    /*TODO*///			{
    /*TODO*///				tmp2 = result % tmp;
    /*TODO*///
    /*TODO*///				if ((result /= tmp) > 0xff)
    /*TODO*///				{
    /*TODO*///					nec_interrupt(0,0);
    /*TODO*///					break;
    /*TODO*///				}
    /*TODO*///				else
    /*TODO*///				{
    /*TODO*///					I.regs.b[AL] = result;
    /*TODO*///					I.regs.b[AH] = tmp2;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				nec_interrupt(0,0);
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?25:35;
    /*TODO*///		break;
    /*TODO*///    case 0x38:  /* IIYV AL, Ew */
    /*TODO*///		{
    /*TODO*///
    /*TODO*///			INT16 result;
    /*TODO*///
    /*TODO*///			result = (INT16)I.regs.w[AW];
    /*TODO*///
    /*TODO*///			if (tmp)
    /*TODO*///			{
    /*TODO*///				tmp2 = result % (INT16)((INT8)tmp);
    /*TODO*///
    /*TODO*///				if ((result /= (INT16)((INT8)tmp)) > 0xff)
    /*TODO*///				{
    /*TODO*///					nec_interrupt(0,0);
    /*TODO*///					break;
    /*TODO*///				}
    /*TODO*///				else
    /*TODO*///				{
    /*TODO*///					I.regs.b[AL] = result;
    /*TODO*///					I.regs.b[AH] = tmp2;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				nec_interrupt(0,0);
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?43:53;
    /*TODO*///		break;
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    static InstructionPtr i_f7pre = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            int tmp = GetRMWord(ModRM);
            int tmp2;

            switch (ModRM & 0x38) {
                /*TODO*///    case 0x00:  /* TEST Ew, data16 */
    /*TODO*///    case 0x08:  /* ??? */
    /*TODO*///		tmp2 = FETCH;
    /*TODO*///		tmp2 += FETCH << 8;
    /*TODO*///
    /*TODO*///		tmp &= tmp2;
    /*TODO*///
    /*TODO*///		I.CarryVal = I.OverVal = I.AuxVal = 0;
    /*TODO*///		SetSZPF_Word(tmp);
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?4:15;
    /*TODO*///		break;
                case 0x10:  /* NOT Ew */

                    tmp = ~tmp;
                    PutbackRMWord(ModRM, tmp & 0xFFFF);
                    nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 24;
                    if (neclog != null) {
                        fprintf(neclog, "i_f7pre 0x10 :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
                    }
                    break;
                /*TODO*///
    /*TODO*///    case 0x18:  /* NEG Ew */
    /*TODO*///        tmp2 = 0;
    /*TODO*///        SUBW(tmp2,tmp);
    /*TODO*///        PutbackRMWord(ModRM,tmp2);
    /*TODO*///	nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///	break;
    /*TODO*///    case 0x20:  /* MUL AW, Ew */
    /*TODO*///		{
    /*TODO*///			UINT32 result;
    /*TODO*///			tmp2 = I.regs.w[AW];
    /*TODO*///
    /*TODO*///			SetSF((INT16)tmp2);
    /*TODO*///			SetPF(tmp2);
    /*TODO*///
    /*TODO*///			result = (UINT32)tmp2*tmp;
    /*TODO*///			I.regs.w[AW]=(WORD)result;
    /*TODO*///            result >>= 16;
    /*TODO*///			I.regs.w[DW]=result;
    /*TODO*///
    /*TODO*///			SetZF(I.regs.w[AW] | I.regs.w[DW]);
    /*TODO*///			I.CarryVal = I.OverVal = (I.regs.w[DW] != 0);
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?30:36;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x28:  /* IMUL AW, Ew */
    /*TODO*///		nec_ICount-=150;
    /*TODO*///		{
    /*TODO*///			INT32 result;
    /*TODO*///
    /*TODO*///			tmp2 = I.regs.w[AW];
    /*TODO*///
    /*TODO*///			SetSF((INT16)tmp2);
    /*TODO*///			SetPF(tmp2);
    /*TODO*///
    /*TODO*///			result = (INT32)((INT16)tmp2)*(INT32)((INT16)tmp);
    /*TODO*///			I.CarryVal = I.OverVal = (result >> 15 != 0) && (result >> 15 != -1);
    /*TODO*///
    /*TODO*///			I.regs.w[AW]=(WORD)result;
    /*TODO*///			result = (WORD)(result >> 16);
    /*TODO*///			I.regs.w[DW]=result;
    /*TODO*///
    /*TODO*///			SetZF(I.regs.w[AW] | I.regs.w[DW]);
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?34:44;
    /*TODO*///		break;
    /*TODO*///    case 0x30:  /* IYV AW, Ew */
    /*TODO*///		{
    /*TODO*///			UINT32 result;
    /*TODO*///
    /*TODO*///            result = (((UINT32)I.regs.w[DW]) << 16) | I.regs.w[AW];
    /*TODO*///
    /*TODO*///			if (tmp)
    /*TODO*///			{
    /*TODO*///				tmp2 = result % tmp;
    /*TODO*///				if ((result /= tmp) > 0xffff)
    /*TODO*///				{
    /*TODO*///					nec_interrupt(0,0);
    /*TODO*///					break;
    /*TODO*///				}
    /*TODO*///				else
    /*TODO*///				{
    /*TODO*///                    I.regs.w[AW]=result;
    /*TODO*///                    I.regs.w[DW]=tmp2;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				nec_interrupt(0,0);
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?25:35;
    /*TODO*///		break;
    /*TODO*///    case 0x38:  /* IIYV AW, Ew */
    /*TODO*///		{
    /*TODO*///			INT32 result;
    /*TODO*///
    /*TODO*///			result = ((UINT32)I.regs.w[DW] << 16) + I.regs.w[AW];
    /*TODO*///
    /*TODO*///			if (tmp)
    /*TODO*///			{
    /*TODO*///				tmp2 = result % (INT32)((INT16)tmp);
    /*TODO*///				if ((result /= (INT32)((INT16)tmp)) > 0xffff)
    /*TODO*///				{
    /*TODO*///					nec_interrupt(0,0);
    /*TODO*///					break;
    /*TODO*///				}
    /*TODO*///				else
    /*TODO*///				{
    /*TODO*///					I.regs.w[AW]=result;
    /*TODO*///					I.regs.w[DW]=tmp2;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				nec_interrupt(0,0);
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?43:53;
    /*TODO*///		break;
                default:
                    System.out.println("i_f7pre 0x" + Integer.toHexString(ModRM & 0x38));
                    break;
            }
        }
    };
    static InstructionPtr i_clc = new InstructionPtr() /* Opcode 0xf8 */ {
        public void handler() {
            I.CarryVal = 0;
            nec_ICount[0] -= 2;
        }
    };
    static InstructionPtr i_stc = new InstructionPtr() /* Opcode 0xf9 */ {
        public void handler() {
            I.CarryVal = 1;
            nec_ICount[0] -= 2;
        }
    };

    static InstructionPtr i_di = new InstructionPtr() /* Opcode 0xfa */ {
        public void handler() {
            I.IF = 0;
            nec_ICount[0] -= 2;
        }
    };
    static InstructionPtr i_ei = new InstructionPtr() /* Opcode 0xfb */ {
        public void handler() {
            SetIF(1);
            nec_ICount[0] -= 2;
        }
    };
    static InstructionPtr i_cld = new InstructionPtr() /* Opcode 0xfc */ {
        public void handler() {
            SetDF(0);
            nec_ICount[0] -= 2;
        }
    };
    static InstructionPtr i_std = new InstructionPtr() /* Opcode 0xfd */ {
        public void handler() {
            SetDF(1);
            nec_ICount[0] -= 2;
        }
    };
    static InstructionPtr i_fepre = new InstructionPtr() {
        public void handler() {
            int ModRM = FETCH();
            int tmp = GetRMByte(ModRM);
            int tmp1;
            if ((ModRM & 0x38) == 0) /* INC eb */ {
                tmp1 = tmp + 1;
                SetOFB_Add(tmp1, tmp, 1);
            } else /* DEC eb */ {
                tmp1 = tmp - 1;
                SetOFB_Sub(tmp1, 1, tmp);
            }

            SetAF(tmp1, tmp, 1);
            SetSZPF_Byte(tmp1);

            PutbackRMByte(ModRM, tmp1 & 0xFF);
            nec_ICount[0] -= (ModRM >= 0xc0) ? 2 : 16;
            if (neclog != null) {
                fprintf(neclog, "i_fepre :PC:%d,I.ip:%d,AW:%d,CW:%d,DW:%d,BW:%d,SP:%d,BP:%d,IX:%d,IY:%d,b1:%d,b2:%d,b3:%d,b4:%d,s1:%d,s2:%d,s3:%d,s4:%d,A:%d,O:%d,S:%d,Z:%d,C:%d,P:%d,T:%d,I:%d,D:%d,M:%d,v:%d,irq:%d,ns:%d,is:%d,pb:%d,pre:%d,EA:%d\n", cpu_get_pc(), I.ip, I.regs.w[AW], I.regs.w[CW], I.regs.w[DW], I.regs.w[BW], I.regs.w[SP], I.regs.w[BP], I.regs.w[IX], I.regs.w[IY], I.base[0], I.base[1], I.base[2], I.base[3], I.sregs[0], I.sregs[1], I.sregs[2], I.sregs[3], I.AuxVal, I.OverVal, I.SignVal, I.ZeroVal, I.CarryVal, I.ParityVal, I.TF, I.IF, I.DF, I.MF, I.int_vector, I.pending_irq, I.nmi_state, I.irq_state, I.prefix_base, I.seg_prefix, EA);
            }
        }
    };
    /*TODO*///static void i_ffpre(void)    /* Opcode 0xff */
    /*TODO*///{
    /*TODO*///	unsigned ModRM = FETCH;
    /*TODO*///    unsigned tmp;
    /*TODO*///    unsigned tmp1;
    /*TODO*///
    /*TODO*///    switch(ModRM & 0x38)
    /*TODO*///    {
    /*TODO*///    case 0x00:  /* INC ew */
    /*TODO*///		tmp = GetRMWord(ModRM);
    /*TODO*///		tmp1 = tmp+1;
    /*TODO*///
    /*TODO*///		/*SetOFW_Add(tmp1,tmp,1);*/
    /*TODO*///        I.OverVal = (tmp==0x7fff); /* Mish */
    /*TODO*///		SetAF(tmp1,tmp,1);
    /*TODO*///		SetSZPF_Word(tmp1);
    /*TODO*///
    /*TODO*///		PutbackRMWord(ModRM,(WORD)tmp1);
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x08:  /* DEC ew */
    /*TODO*///		tmp = GetRMWord(ModRM);
    /*TODO*///		tmp1 = tmp-1;
    /*TODO*///
    /*TODO*///		/*SetOFW_Sub(tmp1,1,tmp);*/
    /*TODO*///		I.OverVal = (tmp==0x8000);	 /* Mish */
    /*TODO*///		SetAF(tmp1,tmp,1);
    /*TODO*///		SetSZPF_Word(tmp1);
    /*TODO*///
    /*TODO*///		PutbackRMWord(ModRM,(WORD)tmp1);
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?2:24;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x10:  /* CALL ew */
    /*TODO*///		tmp = GetRMWord(ModRM);
    /*TODO*///		PUSH(I.ip);
    /*TODO*///		I.ip = (WORD)tmp;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?16:20;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///	case 0x18:  /* CALL FAR ea */
    /*TODO*///		tmp = I.sregs[CS];	/* HJB 12/13/98 need to skip displacements of EA */
    /*TODO*///		tmp1 = GetRMWord(ModRM);
    /*TODO*///		I.sregs[CS] = GetnextRMWord;
    /*TODO*///		I.base[CS] = SegBase(CS);
    /*TODO*///		PUSH(tmp);
    /*TODO*///		PUSH(I.ip);
    /*TODO*///		I.ip = tmp1;
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///		nec_ICount-=(ModRM >=0xc0 )?16:26;
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x20:  /* JMP ea */
    /*TODO*///		nec_ICount-=13;
    /*TODO*///		I.ip = GetRMWord(ModRM);
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x28:  /* JMP FAR ea */
    /*TODO*///		nec_ICount-=15;
    /*TODO*///		I.ip = GetRMWord(ModRM);
    /*TODO*///		I.sregs[CS] = GetnextRMWord;
    /*TODO*///		I.base[CS] = SegBase(CS);
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///		break;
    /*TODO*///
    /*TODO*///    case 0x30:  /* PUSH ea */
    /*TODO*///		nec_ICount-=4;
    /*TODO*///		tmp = GetRMWord(ModRM);
    /*TODO*///		PUSH(tmp);
    /*TODO*///		break;
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void i_invalid(void)
    /*TODO*///{
    /*TODO*///    /* makes the cpu loops forever until user resets it */
    /*TODO*///	/*	{ extern int debug_key_pressed; debug_key_pressed = 1; } */
    /*TODO*///	I.ip--;
    /*TODO*///	nec_ICount-=10;
    /*TODO*///	if (errorlog)
    /*TODO*///		fprintf(errorlog,"PC=%06x : Invalid Opcode %02x\n",cpu_get_pc(),(BYTE)cpu_readop((I.base[CS]+I.ip)));
    /*TODO*///}
    /*TODO*///

    @Override
    public Object init_context() {
        Object reg = new nec_Regs();
        return reg;
    }
    /*TODO*////* ASG 971222 -- added these interface functions */
    /*TODO*///
    /*TODO*///unsigned nec_get_context(void *dst)
    /*TODO*///{
    /*TODO*///	if( dst )
    /*TODO*///		*(nec_Regs*)dst = I;
    /*TODO*///    return sizeof(nec_Regs);
    /*TODO*///}
    /*TODO*///
    /*TODO*///void nec_set_context(void *src)
    /*TODO*///{
    /*TODO*///	if( src )
    /*TODO*///	{
    /*TODO*///		I = *(nec_Regs*)src;
    /*TODO*///		I.base[CS] = SegBase(CS);
    /*TODO*///		I.base[DS] = SegBase(DS);
    /*TODO*///		I.base[ES] = SegBase(ES);
    /*TODO*///		I.base[SS] = SegBase(SS);
    /*TODO*///		change_pc20((I.base[CS]+I.ip));
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///

    @Override
    public int get_pc() {
        return (I.base[CS] + (I.ip & 0xFFFF));//return (I.base[CS] + (WORD)I.ip);
    }
    /*TODO*///void nec_set_pc(unsigned val)
    /*TODO*///{
    /*TODO*///	if( val - I.base[CS] < 0x10000 )
    /*TODO*///	{
    /*TODO*///		I.ip = val - I.base[CS];
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		I.base[CS] = val & 0xffff0;
    /*TODO*///		I.sregs[CS] = I.base[CS] >> 4;
    /*TODO*///		I.ip = val & 0x0000f;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///unsigned nec_get_sp(void)
    /*TODO*///{
    /*TODO*///	return I.base[SS] + I.regs.w[SP];
    /*TODO*///}
    /*TODO*///
    /*TODO*///void nec_set_sp(unsigned val)
    /*TODO*///{
    /*TODO*///	if( val - I.base[SS] < 0x10000 )
    /*TODO*///	{
    /*TODO*///		I.regs.w[SP] = val - I.base[SS];
    /*TODO*///	}
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		I.base[SS] = val & 0xffff0;
    /*TODO*///		I.sregs[SS] = I.base[SS] >> 4;
    /*TODO*///		I.regs.w[SP] = val & 0x0000f;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///unsigned nec_get_reg(int regnum)
    /*TODO*///{
    /*TODO*///	switch( regnum )
    /*TODO*///	{
    /*TODO*///		case NEC_IP: return I.ip;
    /*TODO*///		case NEC_SP: return I.regs.w[SP];
    /*TODO*///		case NEC_FLAGS: CompressFlags(); return I.flags;
    /*TODO*///        case NEC_AW: return I.regs.w[AW];
    /*TODO*///		case NEC_CW: return I.regs.w[CW];
    /*TODO*///		case NEC_DW: return I.regs.w[DW];
    /*TODO*///		case NEC_BW: return I.regs.w[BW];
    /*TODO*///		case NEC_BP: return I.regs.w[BP];
    /*TODO*///		case NEC_IX: return I.regs.w[IX];
    /*TODO*///		case NEC_IY: return I.regs.w[IY];
    /*TODO*///		case NEC_ES: return I.sregs[ES];
    /*TODO*///		case NEC_CS: return I.sregs[CS];
    /*TODO*///		case NEC_SS: return I.sregs[SS];
    /*TODO*///		case NEC_DS: return I.sregs[DS];
    /*TODO*///		case NEC_VECTOR: return I.int_vector;
    /*TODO*///		case NEC_PENDING: return I.pending_irq;
    /*TODO*///		case NEC_NMI_STATE: return I.nmi_state;
    /*TODO*///		case NEC_IRQ_STATE: return I.irq_state;
    /*TODO*///		case REG_PREVIOUSPC: return 0;	/* not supported */
    /*TODO*///		default:
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = ((I.base[SS] + I.regs.w[SP])) + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				return cpu_readmem20( offset ) | ( cpu_readmem20( offset + 1) << 8 );
    /*TODO*///			}
    /*TODO*///	}
    /*TODO*///	return 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void nec_set_nmi_line(int state);
    /*TODO*///void nec_set_irq_line(int irqline, int state);
    /*TODO*///
    /*TODO*///void nec_set_reg(int regnum, unsigned val)
    /*TODO*///{
    /*TODO*///	switch( regnum )
    /*TODO*///	{
    /*TODO*///		case NEC_IP: I.ip = val; break;
    /*TODO*///		case NEC_SP: I.regs.w[SP] = val; break;
    /*TODO*///		case NEC_FLAGS: I.flags = val; ExpandFlags(val); break;
    /*TODO*///        case NEC_AW: I.regs.w[AW] = val; break;
    /*TODO*///		case NEC_CW: I.regs.w[CW] = val; break;
    /*TODO*///		case NEC_DW: I.regs.w[DW] = val; break;
    /*TODO*///		case NEC_BW: I.regs.w[BW] = val; break;
    /*TODO*///		case NEC_BP: I.regs.w[BP] = val; break;
    /*TODO*///		case NEC_IX: I.regs.w[IX] = val; break;
    /*TODO*///		case NEC_IY: I.regs.w[IY] = val; break;
    /*TODO*///		case NEC_ES: I.sregs[ES] = val; break;
    /*TODO*///		case NEC_CS: I.sregs[CS] = val; break;
    /*TODO*///		case NEC_SS: I.sregs[SS] = val; break;
    /*TODO*///		case NEC_DS: I.sregs[DS] = val; break;
    /*TODO*///		case NEC_VECTOR: I.int_vector = val; break;
    /*TODO*///		case NEC_PENDING: I.pending_irq = val; break;
    /*TODO*///		case NEC_NMI_STATE: nec_set_nmi_line(val); break;
    /*TODO*///		case NEC_IRQ_STATE: nec_set_irq_line(0,val); break;
    /*TODO*///		default:
    /*TODO*///			if( regnum <= REG_SP_CONTENTS )
    /*TODO*///			{
    /*TODO*///				unsigned offset = ((I.base[SS] + I.regs.w[SP])) + 2 * (REG_SP_CONTENTS - regnum);
    /*TODO*///				cpu_writemem20( offset, val & 0xff );
    /*TODO*///				cpu_writemem20( offset+1, (val >> 8) & 0xff );
    /*TODO*///			}
    /*TODO*///    }
    /*TODO*///}
    /*TODO*///

    @Override
    public void set_nmi_line(int state) {
        if (I.nmi_state == state) {
            return;
        }
        I.nmi_state = state;
        if (state != CLEAR_LINE) {
            I.pending_irq |= NMI_IRQ;
        }
    }

    @Override
    public void set_irq_line(int irqline, int state) {
        I.irq_state = state;
        if (state == CLEAR_LINE) {
            if (I.IF == 0) {
                I.pending_irq &= ~INT_IRQ;
            }
        } else {
            if (I.IF != 0) {
                I.pending_irq |= INT_IRQ;
            }
        }
    }

    @Override
    public void set_irq_callback(cpuintrfH.irqcallbacksPtr callback) {
        I.irq_callback = callback;
    }

    @Override
    public int execute(int cycles) {
        nec_ICount[0] = cycles;	/* ASG 971222 cycles_per_run;*/

        while (nec_ICount[0] > 0) {
            /*TODO*///
    /*TODO*///#ifdef VERBOSE_DEBUG
    /*TODO*///printf("[%04x:%04x]=%02x\tAW=%04x\tBW=%04x\tCW=%04x\tDW=%04x\n",sregs[CS],I.ip,GetMemB(CS,I.ip),I.regs.w[AW],I.regs.w[BW],I.regs.w[CW],I.regs.w[DW]);
    /*TODO*///#endif
    /*TODO*///
            if ((I.pending_irq != 0 && I.IF != 0) || (I.pending_irq & NMI_IRQ) != 0) {
                external_int(); 	 /* HJB 12/15/98 */

            }

            I.seg_prefix = 0;//FALSE
            int FETCHOP = cpu_readop((I.base[CS] + I.ip++)) & 0xFF;
            switch (FETCHOP) {
                /*TODO*///	case 0x00:    i_add_br8(); break;
    /*TODO*///	case 0x01:    i_add_wr16(); break;
                case 0x02:
                    i_add_r8b.handler();
                    break;
                case 0x03:
                    i_add_r16w.handler();
                    break;
                /*TODO*///	case 0x04:    i_add_ald8(); break;
                case 0x05:
                    i_add_axd16.handler();
                    break;
                case 0x06:
                    i_push_es.handler();
                    break;
                case 0x07:
                    i_pop_es.handler();
                    break;
                /*TODO*///	case 0x08:    i_or_br8(); break;
    /*TODO*///	case 0x09:    i_or_wr16(); break;
    /*TODO*///	case 0x0a:    i_or_r8b(); break;
    /*TODO*///	case 0x0b:    i_or_r16w(); break;
    /*TODO*///	case 0x0c:    i_or_ald8(); break;
    /*TODO*///	case 0x0d:    i_or_axd16(); break;
                case 0x0e:
                    i_push_cs.handler();
                    break;
                /*TODO*///	case 0x0f:    i_pre_nec(); break;
    /*TODO*///	case 0x10:    i_adc_br8(); break;
    /*TODO*///	case 0x11:    i_adc_wr16(); break;
    /*TODO*///	case 0x12:    i_adc_r8b(); break;
    /*TODO*///	case 0x13:    i_adc_r16w(); break;
    /*TODO*///	case 0x14:    i_adc_ald8(); break;
    /*TODO*///	case 0x15:    i_adc_axd16(); break;
    /*TODO*///	case 0x16:    i_push_ss(); break;
    /*TODO*///	case 0x17:    i_pop_ss(); break;
    /*TODO*///	case 0x18:    i_sbb_br8(); break;
    /*TODO*///	case 0x19:    i_sbb_wr16(); break;
    /*TODO*///	case 0x1a:    i_sbb_r8b(); break;
    /*TODO*///	case 0x1b:    i_sbb_r16w(); break;
    /*TODO*///	case 0x1c:    i_sbb_ald8(); break;
    /*TODO*///	case 0x1d:    i_sbb_axd16(); break;
                case 0x1e:
                    i_push_ds.handler();
                    break;
                case 0x1f:
                    i_pop_ds.handler();
                    break;
                /*TODO*///	case 0x20:    i_and_br8(); break;
    /*TODO*///	case 0x21:    i_and_wr16(); break;
    /*TODO*///	case 0x22:    i_and_r8b(); break;
    /*TODO*///	case 0x23:    i_and_r16w(); break;
                case 0x24:
                    i_and_ald8.handler();
                    break;
                case 0x25:
                    i_and_axd16.handler();
                    break;
                case 0x26:
                    i_es.handler();
                    break;
                /*TODO*///	case 0x27:    i_daa(); break;
    /*TODO*///	case 0x28:    i_sub_br8(); break;
    /*TODO*///	case 0x29:    i_sub_wr16(); break;
    /*TODO*///	case 0x2a:    i_sub_r8b(); break;
                case 0x2b:
                    i_sub_r16w.handler();
                    break;
                /*TODO*///	case 0x2c:    i_sub_ald8(); break;
                case 0x2d:
                    i_sub_axd16.handler();
                    break;
                /*TODO*///	case 0x2e:    i_cs(); break;
    /*TODO*///	case 0x2f:    i_das(); break;
    /*TODO*///	case 0x30:    i_xor_br8(); break;
    /*TODO*///	case 0x31:    i_xor_wr16(); break;
                case 0x32:
                    i_xor_r8b.handler();
                    break;
                case 0x33:
                    i_xor_r16w.handler();
                    break;
                /*TODO*///	case 0x34:    i_xor_ald8(); break;
    /*TODO*///	case 0x35:    i_xor_axd16(); break;
                case 0x36:
                    i_ss.handler();
                    break;
                /*TODO*///	case 0x37:    i_aaa(); break;
                case 0x38:
                    i_cmp_br8.handler();
                    break;
                case 0x39:
                    i_cmp_wr16.handler();
                    break;
                /*TODO*///	case 0x3a:    i_cmp_r8b(); break;
                case 0x3b:
                    i_cmp_r16w.handler();
                    break;
                /*TODO*///	case 0x3c:    i_cmp_ald8(); break;
    /*TODO*///	case 0x3d:    i_cmp_axd16(); break;
    /*TODO*///	case 0x3e:    i_ds(); break;
    /*TODO*///	case 0x3f:    i_aas(); break;
                case 0x40:
                    i_inc_ax.handler();
                    break;
                case 0x41:
                    i_inc_cx.handler();
                    break;
                case 0x42:
                    i_inc_dx.handler();
                    break;
                case 0x43:
                    i_inc_bx.handler();
                    break;
                case 0x44:
                    i_inc_sp.handler();
                    break;
                case 0x45:
                    i_inc_bp.handler();
                    break;
                case 0x46:
                    i_inc_si.handler();
                    break;
                case 0x47:
                    i_inc_di.handler();
                    break;
                case 0x48:
                    i_dec_ax.handler();
                    break;
                case 0x49:
                    i_dec_cx.handler();
                    break;
                case 0x4a:
                    i_dec_dx.handler();
                    break;
                case 0x4b:
                    i_dec_bx.handler();
                    break;
                case 0x4c:
                    i_dec_sp.handler();
                    break;
                case 0x4d:
                    i_dec_bp.handler();
                    break;
                case 0x4e:
                    i_dec_si.handler();
                    break;
                case 0x4f:
                    i_dec_di.handler();
                    break;
                /*TODO*///	case 0x50:    i_push_ax(); break;
    /*TODO*///	case 0x51:    i_push_cx(); break;
    /*TODO*///	case 0x52:    i_push_dx(); break;
                case 0x53:
                    i_push_bx.handler();
                    break;
                /*TODO*///	case 0x54:    i_push_sp(); break;
                case 0x55:
                    i_push_bp.handler();
                    break;
                /*TODO*///	case 0x56:    i_push_si(); break;
                case 0x57:
                    i_push_di.handler();
                    break;
                /*TODO*///	case 0x58:    i_pop_ax(); break;
    /*TODO*///	case 0x59:    i_pop_cx(); break;
    /*TODO*///	case 0x5a:    i_pop_dx(); break;
                case 0x5b:
                    i_pop_bx.handler();
                    break;
                /*TODO*///	case 0x5c:    i_pop_sp(); break;
                case 0x5d:
                    i_pop_bp.handler();
                    break;
                /*TODO*///	case 0x5e:    i_pop_si(); break;
                case 0x5f:
                    i_pop_di.handler();
                    break;
                /*TODO*///        case 0x60:    i_pusha(); break;
    /*TODO*///        case 0x61:    i_popa(); break;
    /*TODO*///        case 0x62:    i_bound(); break;
    /*TODO*///	case 0x63:    i_invalid(); break;
    /*TODO*///	case 0x64:    i_repnc(); break;
    /*TODO*///	case 0x65:	  i_repc(); break;
    /*TODO*///	case 0x66:    i_invalid(); break;
    /*TODO*///	case 0x67:    i_invalid(); break;
    /*TODO*///        case 0x68:    i_push_d16(); break;
    /*TODO*///        case 0x69:    i_imul_d16(); break;
    /*TODO*///        case 0x6a:    i_push_d8(); break;
    /*TODO*///        case 0x6b:    i_imul_d8(); break;
    /*TODO*///        case 0x6c:    i_insb(); break;
    /*TODO*///        case 0x6d:    i_insw(); break;
    /*TODO*///        case 0x6e:    i_outsb(); break;
    /*TODO*///        case 0x6f:    i_outsw(); break;
    /*TODO*///	case 0x70:    i_jo(); break;
    /*TODO*///	case 0x71:    i_jno(); break;
    /*TODO*///	case 0x72:    i_jb(); break;
                case 0x73:
                    i_jnb.handler();
                    break;
                case 0x74:
                    i_jz.handler();
                    break;
                case 0x75:
                    i_jnz.handler();
                    break;
                /*TODO*///	case 0x76:    i_jbe(); break;
    /*TODO*///	case 0x77:    i_jnbe(); break;
    /*TODO*///	case 0x78:    i_js(); break;
    /*TODO*///	case 0x79:    i_jns(); break;
    /*TODO*///	case 0x7a:    i_jp(); break;
    /*TODO*///	case 0x7b:    i_jnp(); break;
    /*TODO*///	case 0x7c:    i_jl(); break;
    /*TODO*///	case 0x7d:    i_jnl(); break;
    /*TODO*///	case 0x7e:    i_jle(); break;
    /*TODO*///	case 0x7f:    i_jnle(); break;
                case 0x80:
                    i_80pre.handler();
                    break;
                case 0x81:
                    i_81pre.handler();
                    break;
                /*TODO*///	case 0x82:	  i_82pre(); break;
                case 0x83:
                    i_83pre.handler();
                    break;
                /*TODO*///	case 0x84:    i_test_br8(); break;
                case 0x85:
                    i_test_wr16.handler();
                    break;
                case 0x86:
                    i_xchg_br8.handler();
                    break;
                /*TODO*///	case 0x87:    i_xchg_wr16(); break;
                case 0x88:
                    i_mov_br8.handler();
                    break;
                case 0x89:
                    i_mov_wr16.handler();
                    break;
                case 0x8a:
                    i_mov_r8b.handler();
                    break;
                case 0x8b:
                    i_mov_r16w.handler();
                    break;
                /*TODO*///	case 0x8c:    i_mov_wsreg(); break;
    /*TODO*///	case 0x8d:    i_lea(); break;
                case 0x8e:
                    i_mov_sregw.handler();
                    break;
                /*TODO*///	case 0x8f:    i_popw(); break;
    /*TODO*///	case 0x90:    i_nop(); break;
    /*TODO*///	case 0x91:    i_xchg_axcx(); break;
    /*TODO*///	case 0x92:    i_xchg_axdx(); break;
    /*TODO*///	case 0x93:    i_xchg_axbx(); break;
    /*TODO*///	case 0x94:    i_xchg_axsp(); break;
    /*TODO*///	case 0x95:    i_xchg_axbp(); break;
    /*TODO*///	case 0x96:    i_xchg_axsi(); break;
    /*TODO*///	case 0x97:    i_xchg_axdi(); break;
    /*TODO*///	case 0x98:    i_cbw(); break;
    /*TODO*///	case 0x99:    i_cwd(); break;
                case 0x9a:
                    i_call_far.handler();
                    break;
                /*TODO*///	case 0x9b:    i_wait(); break;
    /*TODO*///	case 0x9c:    i_pushf(); break;
    /*TODO*///	case 0x9d:    i_popf(); break;
    /*TODO*///	case 0x9e:    i_sahf(); break;
    /*TODO*///	case 0x9f:    i_lahf(); break;
                case 0xa0:
                    i_mov_aldisp.handler();
                    break;
                case 0xa1:
                    i_mov_axdisp.handler();
                    break;
                case 0xa2:
                    i_mov_dispal.handler();
                    break;
                case 0xa3:
                    i_mov_dispax.handler();
                    break;
                case 0xa4:
                    i_movsb.handler();
                    break;
                /*TODO*///	case 0xa5:    i_movsw(); break;
    /*TODO*///	case 0xa6:    i_cmpsb(); break;
    /*TODO*///	case 0xa7:    i_cmpsw(); break;
    /*TODO*///	case 0xa8:    i_test_ald8(); break;
                case 0xa9:
                    i_test_axd16.handler();
                    break;
                case 0xaa:
                    i_stosb.handler();
                    break;
                case 0xab:
                    i_stosw.handler();
                    break;
                case 0xac:
                    i_lodsb.handler();
                    break;
                /*TODO*///	case 0xad:    i_lodsw(); break;
                case 0xae:
                    i_scasb.handler();
                    break;
                case 0xaf:
                    i_scasw.handler();
                    break;
                case 0xb0:
                    i_mov_ald8.handler();
                    break;
                case 0xb1:
                    i_mov_cld8.handler();
                    break;
                /*TODO*///	case 0xb2:    i_mov_dld8(); break;
    /*TODO*///	case 0xb3:    i_mov_bld8(); break;
    /*TODO*///	case 0xb4:    i_mov_ahd8(); break;
                case 0xb5:
                    i_mov_chd8.handler();
                    break;
                /*TODO*///	case 0xb6:    i_mov_dhd8(); break;
    /*TODO*///	case 0xb7:    i_mov_bhd8(); break;
                case 0xb8:
                    i_mov_axd16.handler();
                    break;
                case 0xb9:
                    i_mov_cxd16.handler();
                    break;
                case 0xba:
                    i_mov_dxd16.handler();
                    break;
                case 0xbb:
                    i_mov_bxd16.handler();
                    break;
                case 0xbc:
                    i_mov_spd16.handler();
                    break;
                case 0xbd:
                    i_mov_bpd16.handler();
                    break;
                case 0xbe:
                    i_mov_sid16.handler();
                    break;
                case 0xbf:
                    i_mov_did16.handler();
                    break;
                /*TODO*///        case 0xc0:    i_rotshft_bd8(); break;
    /*TODO*///        case 0xc1:    i_rotshft_wd8(); break;
    /*TODO*///	case 0xc2:    i_ret_d16(); break;
                case 0xc3:
                    i_ret.handler();
                    break;
                /*TODO*///	case 0xc4:    i_les_dw(); break;
    /*TODO*///	case 0xc5:    i_lds_dw(); break;
                case 0xc6:
                    i_mov_bd8.handler();
                    break;
                case 0xc7:
                    i_mov_wd16.handler();
                    break;
                /*TODO*///        case 0xc8:    i_enter(); break;
    /*TODO*///        case 0xc9:    i_leave(); break;
    /*TODO*///	case 0xca:    i_retf_d16(); break;
    /*TODO*///	case 0xcb:    i_retf(); break;
    /*TODO*///	case 0xcc:    i_int3(); break;
    /*TODO*///	case 0xcd:    i_int(); break;
    /*TODO*///	case 0xce:    i_into(); break;
    /*TODO*///	case 0xcf:    i_iret(); break;
    /*TODO*///        case 0xd0:    i_rotshft_b(); break;
    /*TODO*///        case 0xd1:    i_rotshft_w(); break;
    /*TODO*///        case 0xd2:    i_rotshft_bcl(); break;
    /*TODO*///        case 0xd3:    i_rotshft_wcl(); break;
    /*TODO*///	case 0xd4:    i_aam(); break;
    /*TODO*///	case 0xd5:    i_aad(); break;
    /*TODO*///	case 0xd6:    i_setalc(); break;
    /*TODO*///	case 0xd7:    i_xlat(); break;
    /*TODO*///	case 0xd8:    i_escape(); break;
    /*TODO*///	case 0xd9:    i_escape(); break;
    /*TODO*///	case 0xda:    i_escape(); break;
    /*TODO*///	case 0xdb:    i_escape(); break;
    /*TODO*///	case 0xdc:    i_escape(); break;
    /*TODO*///	case 0xdd:    i_escape(); break;
    /*TODO*///	case 0xde:    i_escape(); break;
    /*TODO*///	case 0xdf:    i_escape(); break;
    /*TODO*///	case 0xe0:    i_loopne(); break;
                case 0xe1:
                    i_loope.handler();
                    break;
                case 0xe2:
                    i_loop.handler();
                    break;
                /*TODO*///	case 0xe3:    i_jcxz(); break;
    /*TODO*///	case 0xe4:    i_inal(); break;
                case 0xe5:
                    i_inax.handler();
                    break;
                case 0xe6:
                    i_outal.handler();
                    break;
                case 0xe7:
                    i_outax.handler();
                    break;
                case 0xe8:
                    i_call_d16.handler();
                    break;
                case 0xe9:
                    i_jmp_d16.handler();
                    break;
                case 0xea:
                    i_jmp_far.handler();
                    break;
                case 0xeb:
                    i_jmp_d8.handler();
                    break;
                /*TODO*///	case 0xec:    i_inaldx(); break;
    /*TODO*///	case 0xed:    i_inaxdx(); break;
    /*TODO*///	case 0xee:    i_outdxal(); break;
    /*TODO*///	case 0xef:    i_outdxax(); break;
    /*TODO*///	case 0xf0:    i_lock(); break;
    /*TODO*///	case 0xf1:    i_invalid(); break;
    /*TODO*///	case 0xf2:    i_repne(); break;
                case 0xf3:
                    i_repe.handler();
                    break;
                /*TODO*///	case 0xf4:    i_hlt(); break;
    /*TODO*///	case 0xf5:    i_cmc(); break;
    /*TODO*///	case 0xf6:    i_f6pre(); break;
                case 0xf7:
                    i_f7pre.handler();
                    break;
                case 0xf8:
                    i_clc.handler();
                    break;
                case 0xf9:
                    i_stc.handler();
                    break;
                case 0xfa:
                    i_di.handler();
                    break;
                case 0xfb:
                    i_ei.handler();
                    break;
                case 0xfc:
                    i_cld.handler();
                    break;
                case 0xfd:
                    i_std.handler();
                    break;
                case 0xfe:
                    i_fepre.handler();
                    break;
                /*TODO*///	case 0xff:    i_ffpre(); break;
                default:
                    System.out.println("Unsupported opcode 0x" + Integer.toHexString(FETCHOP));
                    if (neclog != null) {
                        fclose(neclog);
                    }
                    break;
            }
            //if (errorlog && cpu_get_pc()>0xc0000) fprintf(errorlog,"CPU %05x\n",cpu_get_pc());
        }
        return cycles - nec_ICount[0];
    }
    /*TODO*///
    /*TODO*///
    /*TODO*///unsigned nec_dasm(char *buffer, unsigned pc)
    /*TODO*///{
    /*TODO*///#ifdef MAME_DEBUG
    /*TODO*///    return Dasmnec(buffer,pc);
    /*TODO*///#else
    /*TODO*///	sprintf( buffer, "$%02X", cpu_readop(pc) );
    /*TODO*///	return 1;
    /*TODO*///#endif
    /*TODO*///}
    /*TODO*///
    /*TODO*////* Wrappers for the different CPU types */
    /*TODO*///void v20_reset(void *param) { nec_reset(param); }
    /*TODO*///void v20_exit(void) { nec_exit(); }
    /*TODO*///int v20_execute(int cycles) { return nec_execute(cycles); }
    /*TODO*///unsigned v20_get_context(void *dst) { return nec_get_context(dst); }
    /*TODO*///void v20_set_context(void *src) { nec_set_context(src); }
    /*TODO*///unsigned v20_get_pc(void) { return nec_get_pc(); }
    /*TODO*///void v20_set_pc(unsigned val) { nec_set_pc(val); }
    /*TODO*///unsigned v20_get_sp(void) { return nec_get_sp(); }
    /*TODO*///void v20_set_sp(unsigned val) { nec_set_sp(val); }
    /*TODO*///unsigned v20_get_reg(int regnum) { return nec_get_reg(regnum); }
    /*TODO*///void v20_set_reg(int regnum, unsigned val)	{ nec_set_reg(regnum,val); }
    /*TODO*///void v20_set_nmi_line(int state) { nec_set_nmi_line(state); }
    /*TODO*///void v20_set_irq_line(int irqline, int state) { nec_set_irq_line(irqline,state); }
    /*TODO*///void v20_set_irq_callback(int (*callback)(int irqline)) { nec_set_irq_callback(callback); }
    /*TODO*///const char *v20_info(void *context, int regnum)
    /*TODO*///{
    /*TODO*///    static char buffer[32][63+1];
    /*TODO*///    static int which = 0;
    /*TODO*///    nec_Regs *r = context;
    /*TODO*///
    /*TODO*///    which = ++which % 32;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///    if( !context )
    /*TODO*///        r = &I;
    /*TODO*///
    /*TODO*///    switch( regnum )
    /*TODO*///    {
    /*TODO*///        case CPU_INFO_REG+NEC_IP: sprintf(buffer[which], "IP:%04X", r->ip); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SP: sprintf(buffer[which], "SP:%04X", r->regs.w[SP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_FLAGS: sprintf(buffer[which], "F:%04X", r->flags); break;
    /*TODO*///        case CPU_INFO_REG+NEC_AW: sprintf(buffer[which], "AW:%04X", r->regs.w[AW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CW: sprintf(buffer[which], "CW:%04X", r->regs.w[CW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DW: sprintf(buffer[which], "DW:%04X", r->regs.w[DW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BW: sprintf(buffer[which], "BW:%04X", r->regs.w[BW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BP: sprintf(buffer[which], "BP:%04X", r->regs.w[BP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IX: sprintf(buffer[which], "IX:%04X", r->regs.w[IX]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IY: sprintf(buffer[which], "IY:%04X", r->regs.w[IY]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_ES: sprintf(buffer[which], "ES:%04X", r->sregs[ES]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CS: sprintf(buffer[which], "CS:%04X", r->sregs[CS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SS: sprintf(buffer[which], "SS:%04X", r->sregs[SS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DS: sprintf(buffer[which], "DS:%04X", r->sregs[DS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_VECTOR: sprintf(buffer[which], "V:%02X", r->int_vector); break;
    /*TODO*///        case CPU_INFO_REG+NEC_PENDING: sprintf(buffer[which], "P:%X", r->pending_irq); break;
    /*TODO*///        case CPU_INFO_REG+NEC_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///        case CPU_INFO_FLAGS:
    /*TODO*///            r->flags = CompressFlags();
    /*TODO*///            sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
    /*TODO*///                r->flags & 0x8000 ? 'M':'.',
    /*TODO*///                r->flags & 0x4000 ? '?':'.',
    /*TODO*///                r->flags & 0x2000 ? '?':'.',
    /*TODO*///                r->flags & 0x1000 ? '?':'.',
    /*TODO*///                r->flags & 0x0800 ? 'O':'.',
    /*TODO*///                r->flags & 0x0400 ? 'D':'.',
    /*TODO*///                r->flags & 0x0200 ? 'I':'.',
    /*TODO*///                r->flags & 0x0100 ? 'T':'.',
    /*TODO*///                r->flags & 0x0080 ? 'S':'.',
    /*TODO*///                r->flags & 0x0040 ? 'Z':'.',
    /*TODO*///                r->flags & 0x0020 ? '?':'.',
    /*TODO*///                r->flags & 0x0010 ? 'A':'.',
    /*TODO*///                r->flags & 0x0008 ? '?':'.',
    /*TODO*///                r->flags & 0x0004 ? 'P':'.',
    /*TODO*///                r->flags & 0x0002 ? 'N':'.',
    /*TODO*///                r->flags & 0x0001 ? 'C':'.');
    /*TODO*///            break;
    /*TODO*///        case CPU_INFO_NAME: return "V20";
    /*TODO*///        case CPU_INFO_FAMILY: return "NEC V-Series";
    /*TODO*///        case CPU_INFO_VERSION: return "1.6";
    /*TODO*///        case CPU_INFO_FILE: return __FILE__;
    /*TODO*///        case CPU_INFO_CREDITS: return "Real mode NEC emulator v1.3 by Oliver Bergmann\n(initial work based on Fabrice Fabian's i86 core)";
    /*TODO*///        case CPU_INFO_REG_LAYOUT: return (const char*)nec_reg_layout;
    /*TODO*///        case CPU_INFO_WIN_LAYOUT: return (const char*)nec_win_layout;
    /*TODO*///    }
    /*TODO*///    return buffer[which];
    /*TODO*///}
    /*TODO*///unsigned v20_dasm(char *buffer, unsigned pc) { return nec_dasm(buffer,pc); }
    /*TODO*///
    /*TODO*///void v30_reset(void *param) { nec_reset(param); }
    /*TODO*///void v30_exit(void) { nec_exit(); }
    /*TODO*///int v30_execute(int cycles) { return nec_execute(cycles); }
    /*TODO*///unsigned v30_get_context(void *dst) { return nec_get_context(dst); }
    /*TODO*///void v30_set_context(void *src) { nec_set_context(src); }
    /*TODO*///unsigned v30_get_pc(void) { return nec_get_pc(); }
    /*TODO*///void v30_set_pc(unsigned val) { nec_set_pc(val); }
    /*TODO*///unsigned v30_get_sp(void) { return nec_get_sp(); }
    /*TODO*///void v30_set_sp(unsigned val) { nec_set_sp(val); }
    /*TODO*///unsigned v30_get_reg(int regnum) { return nec_get_reg(regnum); }
    /*TODO*///void v30_set_reg(int regnum, unsigned val)	{ nec_set_reg(regnum,val); }
    /*TODO*///void v30_set_nmi_line(int state) { nec_set_nmi_line(state); }
    /*TODO*///void v30_set_irq_line(int irqline, int state) { nec_set_irq_line(irqline,state); }
    /*TODO*///void v30_set_irq_callback(int (*callback)(int irqline)) { nec_set_irq_callback(callback); }

    @Override
    public String cpu_info(Object context, int regnum) {
        /*TODO*///    static char buffer[32][63+1];
    /*TODO*///    static int which = 0;
    /*TODO*///    nec_Regs *r = context;
    /*TODO*///
    /*TODO*///    which = ++which % 32;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///    if( !context )
    /*TODO*///        r = &I;
    /*TODO*///
        switch (regnum) {
            /*TODO*///        case CPU_INFO_REG+NEC_IP: sprintf(buffer[which], "IP:%04X", r->ip); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SP: sprintf(buffer[which], "SP:%04X", r->regs.w[SP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_FLAGS: sprintf(buffer[which], "F:%04X", r->flags); break;
    /*TODO*///        case CPU_INFO_REG+NEC_AW: sprintf(buffer[which], "AW:%04X", r->regs.w[AW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CW: sprintf(buffer[which], "CW:%04X", r->regs.w[CW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DW: sprintf(buffer[which], "DW:%04X", r->regs.w[DW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BW: sprintf(buffer[which], "BW:%04X", r->regs.w[BW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BP: sprintf(buffer[which], "BP:%04X", r->regs.w[BP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IX: sprintf(buffer[which], "IX:%04X", r->regs.w[IX]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IY: sprintf(buffer[which], "IY:%04X", r->regs.w[IY]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_ES: sprintf(buffer[which], "ES:%04X", r->sregs[ES]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CS: sprintf(buffer[which], "CS:%04X", r->sregs[CS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SS: sprintf(buffer[which], "SS:%04X", r->sregs[SS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DS: sprintf(buffer[which], "DS:%04X", r->sregs[DS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_VECTOR: sprintf(buffer[which], "V:%02X", r->int_vector); break;
    /*TODO*///        case CPU_INFO_REG+NEC_PENDING: sprintf(buffer[which], "P:%X", r->pending_irq); break;
    /*TODO*///        case CPU_INFO_REG+NEC_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///        case CPU_INFO_FLAGS:
    /*TODO*///            r->flags = CompressFlags();
    /*TODO*///            sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
    /*TODO*///                r->flags & 0x8000 ? 'M':'.',
    /*TODO*///                r->flags & 0x4000 ? '?':'.',
    /*TODO*///                r->flags & 0x2000 ? '?':'.',
    /*TODO*///                r->flags & 0x1000 ? '?':'.',
    /*TODO*///                r->flags & 0x0800 ? 'O':'.',
    /*TODO*///                r->flags & 0x0400 ? 'D':'.',
    /*TODO*///                r->flags & 0x0200 ? 'I':'.',
    /*TODO*///                r->flags & 0x0100 ? 'T':'.',
    /*TODO*///                r->flags & 0x0080 ? 'S':'.',
    /*TODO*///                r->flags & 0x0040 ? 'Z':'.',
    /*TODO*///                r->flags & 0x0020 ? '?':'.',
    /*TODO*///                r->flags & 0x0010 ? 'A':'.',
    /*TODO*///                r->flags & 0x0008 ? '?':'.',
    /*TODO*///                r->flags & 0x0004 ? 'P':'.',
    /*TODO*///                r->flags & 0x0002 ? 'N':'.',
    /*TODO*///                r->flags & 0x0001 ? 'C':'.');
    /*TODO*///            break;
            case CPU_INFO_NAME:
                return "V30";
            case CPU_INFO_FAMILY:
                return "NEC V-Series";
            case CPU_INFO_VERSION:
                return "1.6";
            case CPU_INFO_FILE:
                return "v30.java";
            case CPU_INFO_CREDITS:
                return "Real mode NEC emulator v1.3 by Oliver Bergmann\n(initial work based on Fabrice Fabian's i86 core)";
            /*TODO*///        case CPU_INFO_REG_LAYOUT: return (const char*)nec_reg_layout;
    /*TODO*///        case CPU_INFO_WIN_LAYOUT: return (const char*)nec_win_layout;
            }
        throw new UnsupportedOperationException("unsupported v30 cpu_info");
        /*TODO*///    return buffer[which];
    }
    /*TODO*///unsigned v30_dasm(char *buffer, unsigned pc) { return nec_dasm(buffer,pc); }
    /*TODO*///
    /*TODO*///void v33_reset(void *param) { nec_reset(param); }
    /*TODO*///void v33_exit(void) { nec_exit(); }
    /*TODO*///int v33_execute(int cycles) { return nec_execute(cycles); }
    /*TODO*///unsigned v33_get_context(void *dst) { return nec_get_context(dst); }
    /*TODO*///void v33_set_context(void *src) { nec_set_context(src); }
    /*TODO*///unsigned v33_get_pc(void) { return nec_get_pc(); }
    /*TODO*///void v33_set_pc(unsigned val) { nec_set_pc(val); }
    /*TODO*///unsigned v33_get_sp(void) { return nec_get_sp(); }
    /*TODO*///void v33_set_sp(unsigned val) { nec_set_sp(val); }
    /*TODO*///unsigned v33_get_reg(int regnum) { return nec_get_reg(regnum); }
    /*TODO*///void v33_set_reg(int regnum, unsigned val)	{ nec_set_reg(regnum,val); }
    /*TODO*///void v33_set_nmi_line(int state) { nec_set_nmi_line(state); }
    /*TODO*///void v33_set_irq_line(int irqline, int state) { nec_set_irq_line(irqline,state); }
    /*TODO*///void v33_set_irq_callback(int (*callback)(int irqline)) { nec_set_irq_callback(callback); }
    /*TODO*///const char *v33_info(void *context, int regnum)
    /*TODO*///{
    /*TODO*///    static char buffer[32][63+1];
    /*TODO*///    static int which = 0;
    /*TODO*///    nec_Regs *r = context;
    /*TODO*///
    /*TODO*///    which = ++which % 32;
    /*TODO*///    buffer[which][0] = '\0';
    /*TODO*///    if( !context )
    /*TODO*///        r = &I;
    /*TODO*///
    /*TODO*///    switch( regnum )
    /*TODO*///    {
    /*TODO*///        case CPU_INFO_REG+NEC_IP: sprintf(buffer[which], "IP:%04X", r->ip); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SP: sprintf(buffer[which], "SP:%04X", r->regs.w[SP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_FLAGS: sprintf(buffer[which], "F:%04X", r->flags); break;
    /*TODO*///        case CPU_INFO_REG+NEC_AW: sprintf(buffer[which], "AW:%04X", r->regs.w[AW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CW: sprintf(buffer[which], "CW:%04X", r->regs.w[CW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DW: sprintf(buffer[which], "DW:%04X", r->regs.w[DW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BW: sprintf(buffer[which], "BW:%04X", r->regs.w[BW]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_BP: sprintf(buffer[which], "BP:%04X", r->regs.w[BP]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IX: sprintf(buffer[which], "IX:%04X", r->regs.w[IX]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IY: sprintf(buffer[which], "IY:%04X", r->regs.w[IY]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_ES: sprintf(buffer[which], "ES:%04X", r->sregs[ES]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_CS: sprintf(buffer[which], "CS:%04X", r->sregs[CS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_SS: sprintf(buffer[which], "SS:%04X", r->sregs[SS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_DS: sprintf(buffer[which], "DS:%04X", r->sregs[DS]); break;
    /*TODO*///        case CPU_INFO_REG+NEC_VECTOR: sprintf(buffer[which], "V:%02X", r->int_vector); break;
    /*TODO*///        case CPU_INFO_REG+NEC_PENDING: sprintf(buffer[which], "P:%X", r->pending_irq); break;
    /*TODO*///        case CPU_INFO_REG+NEC_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
    /*TODO*///        case CPU_INFO_REG+NEC_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
    /*TODO*///        case CPU_INFO_FLAGS:
    /*TODO*///            r->flags = CompressFlags();
    /*TODO*///            sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
    /*TODO*///                r->flags & 0x8000 ? 'M':'.',
    /*TODO*///                r->flags & 0x4000 ? '?':'.',
    /*TODO*///                r->flags & 0x2000 ? '?':'.',
    /*TODO*///                r->flags & 0x1000 ? '?':'.',
    /*TODO*///                r->flags & 0x0800 ? 'O':'.',
    /*TODO*///                r->flags & 0x0400 ? 'D':'.',
    /*TODO*///                r->flags & 0x0200 ? 'I':'.',
    /*TODO*///                r->flags & 0x0100 ? 'T':'.',
    /*TODO*///                r->flags & 0x0080 ? 'S':'.',
    /*TODO*///                r->flags & 0x0040 ? 'Z':'.',
    /*TODO*///                r->flags & 0x0020 ? '?':'.',
    /*TODO*///                r->flags & 0x0010 ? 'A':'.',
    /*TODO*///                r->flags & 0x0008 ? '?':'.',
    /*TODO*///                r->flags & 0x0004 ? 'P':'.',
    /*TODO*///                r->flags & 0x0002 ? 'N':'.',
    /*TODO*///                r->flags & 0x0001 ? 'C':'.');
    /*TODO*///            break;
    /*TODO*///        case CPU_INFO_NAME: return "V33";
    /*TODO*///        case CPU_INFO_FAMILY: return "NEC V-Series";
    /*TODO*///        case CPU_INFO_VERSION: return "1.6";
    /*TODO*///        case CPU_INFO_FILE: return __FILE__;
    /*TODO*///        case CPU_INFO_CREDITS: return "Real mode NEC emulator v1.3 by Oliver Bergmann\n(initial work based on Fabrice Fabian's i86 core)";
    /*TODO*///        case CPU_INFO_REG_LAYOUT: return (const char*)nec_reg_layout;
    /*TODO*///        case CPU_INFO_WIN_LAYOUT: return (const char*)nec_win_layout;
    /*TODO*///    }
    /*TODO*///    return buffer[which];
    /*TODO*///}
    /*TODO*///unsigned v33_dasm(char *buffer, unsigned pc) { return nec_dasm(buffer,pc); }
    /*TODO*///
    /*TODO*///
    /*TODO*///

}
