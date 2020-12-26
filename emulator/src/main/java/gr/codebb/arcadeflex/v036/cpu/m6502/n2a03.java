package gr.codebb.arcadeflex.v036.cpu.m6502;

import gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.cpu_interface;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.m6502H.*;
import static gr.codebb.arcadeflex.v036.cpu.m6502.m6502.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;

public class n2a03 extends m6502 {

    public static double N2A03_DEFAULTCLOCK = (21477272.724 / 12);
    public static int N2A03_INT_NONE = M6502_INT_NONE;
    public static int N2A03_INT_IRQ = M6502_INT_IRQ;
    public static int N2A03_INT_NMI = M6502_INT_NMI;

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
        icount=m6502_ICount;
        
        m6502_ICount[0] = 0;
        setupTables1();


    }

    @Override
    public void reset(Object param) {
        super.reset(param);
        m6502.subtype = SUBTYPE_2A03;
        m6502.insn = insn2a03;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "N2A03";
            case CPU_INFO_VERSION:
                return "1.0";
        }
        return super.cpu_info(context, regnum);
    }

    public static void n2a03_irq() {
        take_irq();
    }
    opcode[] insn2a03 = new opcode[256];

    public void setupTables1()
    {
        insn2a03[0x00] = m6502_00; insn2a03[0x01] = m6502_01; insn2a03[0x02] = m6502_02; insn2a03[0x03] = m6502_03;
        insn2a03[0x04] = m6502_04; insn2a03[0x05] = m6502_05; insn2a03[0x06] = m6502_06; insn2a03[0x07] = m6502_07;
        insn2a03[0x08] = m6502_08; insn2a03[0x09] = m6502_09; insn2a03[0x0a] = m6502_0a; insn2a03[0x0b] = m6502_0b;
        insn2a03[0x0c] = m6502_0c; insn2a03[0x0d] = m6502_0d; insn2a03[0x0e] = m6502_0e; insn2a03[0x0f] = m6502_0f;

        insn2a03[0x10] = m6502_10; insn2a03[0x11] = m6502_11; insn2a03[0x12] = m6502_12; insn2a03[0x13] = m6502_13;
        insn2a03[0x14] = m6502_14; insn2a03[0x15] = m6502_15; insn2a03[0x16] = m6502_16; insn2a03[0x17] = m6502_17;
        insn2a03[0x18] = m6502_18; insn2a03[0x19] = m6502_19; insn2a03[0x1a] = m6502_1a; insn2a03[0x1b] = m6502_1b;
        insn2a03[0x1c] = m6502_1c; insn2a03[0x1d] = m6502_1d; insn2a03[0x1e] = m6502_1e; insn2a03[0x1f] = m6502_1f;

        insn2a03[0x20] = m6502_20; insn2a03[0x21] = m6502_21; insn2a03[0x22] = m6502_22; insn2a03[0x23] = m6502_23;
        insn2a03[0x24] = m6502_24; insn2a03[0x25] = m6502_25; insn2a03[0x26] = m6502_26; insn2a03[0x27] = m6502_27;
        insn2a03[0x28] = m6502_28; insn2a03[0x29] = m6502_29; insn2a03[0x2a] = m6502_2a; insn2a03[0x2b] = m6502_2b;
        insn2a03[0x2c] = m6502_2c; insn2a03[0x2d] = m6502_2d; insn2a03[0x2e] = m6502_2e; insn2a03[0x2f] = m6502_2f;

        insn2a03[0x30] = m6502_30; insn2a03[0x31] = m6502_31; insn2a03[0x32] = m6502_32; insn2a03[0x33] = m6502_33;
        insn2a03[0x34] = m6502_34; insn2a03[0x35] = m6502_35; insn2a03[0x36] = m6502_36; insn2a03[0x37] = m6502_37;
        insn2a03[0x38] = m6502_38; insn2a03[0x39] = m6502_39; insn2a03[0x3a] = m6502_3a; insn2a03[0x3b] = m6502_3b;
        insn2a03[0x3c] = m6502_3c; insn2a03[0x3d] = m6502_3d; insn2a03[0x3e] = m6502_3e; insn2a03[0x3f] = m6502_3f;

        insn2a03[0x40] = m6502_40; insn2a03[0x41] = m6502_41; insn2a03[0x42] = m6502_42; insn2a03[0x43] = m6502_43;
        insn2a03[0x44] = m6502_44; insn2a03[0x45] = m6502_45; insn2a03[0x46] = m6502_46; insn2a03[0x47] = m6502_47;
        insn2a03[0x48] = m6502_48; insn2a03[0x49] = m6502_49; insn2a03[0x4a] = m6502_4a; insn2a03[0x4b] = m6502_4b;
        insn2a03[0x4c] = m6502_4c; insn2a03[0x4d] = m6502_4d; insn2a03[0x4e] = m6502_4e; insn2a03[0x4f] = m6502_4f;

        insn2a03[0x50] = m6502_50; insn2a03[0x51] = m6502_51; insn2a03[0x52] = m6502_52; insn2a03[0x53] = m6502_53;
        insn2a03[0x54] = m6502_54; insn2a03[0x55] = m6502_55; insn2a03[0x56] = m6502_56; insn2a03[0x57] = m6502_57;
        insn2a03[0x58] = m6502_58; insn2a03[0x59] = m6502_59; insn2a03[0x5a] = m6502_5a; insn2a03[0x5b] = m6502_5b;
        insn2a03[0x5c] = m6502_5c; insn2a03[0x5d] = m6502_5d; insn2a03[0x5e] = m6502_5e; insn2a03[0x5f] = m6502_5f;

        insn2a03[0x60] = m6502_60; insn2a03[0x61] = n2a03_61; insn2a03[0x62] = m6502_62; insn2a03[0x63] = m6502_63;
        insn2a03[0x64] = m6502_64; insn2a03[0x65] = n2a03_65; insn2a03[0x66] = m6502_66; insn2a03[0x67] = m6502_67;
        insn2a03[0x68] = m6502_68; insn2a03[0x69] = n2a03_69; insn2a03[0x6a] = m6502_6a; insn2a03[0x6b] = m6502_6b;
        insn2a03[0x6c] = m6502_6c; insn2a03[0x6d] = n2a03_6d; insn2a03[0x6e] = m6502_6e; insn2a03[0x6f] = m6502_6f;

        insn2a03[0x70] = m6502_70; insn2a03[0x71] = n2a03_71; insn2a03[0x72] = m6502_72; insn2a03[0x73] = m6502_73;
        insn2a03[0x74] = m6502_74; insn2a03[0x75] = n2a03_75; insn2a03[0x76] = m6502_76; insn2a03[0x77] = m6502_77;
        insn2a03[0x78] = m6502_78; insn2a03[0x79] = n2a03_79; insn2a03[0x7a] = m6502_7a; insn2a03[0x7b] = m6502_7b;
        insn2a03[0x7c] = m6502_7c; insn2a03[0x7d] = n2a03_7d; insn2a03[0x7e] = m6502_7e; insn2a03[0x7f] = m6502_7f;

        insn2a03[0x80] = m6502_80; insn2a03[0x81] = m6502_81; insn2a03[0x82] = m6502_82; insn2a03[0x83] = m6502_83;
        insn2a03[0x84] = m6502_84; insn2a03[0x85] = m6502_85; insn2a03[0x86] = m6502_86; insn2a03[0x87] = m6502_87;
        insn2a03[0x88] = m6502_88; insn2a03[0x89] = m6502_89; insn2a03[0x8a] = m6502_8a; insn2a03[0x8b] = m6502_8b;
        insn2a03[0x8c] = m6502_8c; insn2a03[0x8d] = m6502_8d; insn2a03[0x8e] = m6502_8e; insn2a03[0x8f] = m6502_8f;

        insn2a03[0x90] = m6502_90; insn2a03[0x91] = m6502_91; insn2a03[0x92] = m6502_92; insn2a03[0x93] = m6502_93;
        insn2a03[0x94] = m6502_94; insn2a03[0x95] = m6502_95; insn2a03[0x96] = m6502_96; insn2a03[0x97] = m6502_97;
        insn2a03[0x98] = m6502_98; insn2a03[0x99] = m6502_99; insn2a03[0x9a] = m6502_9a; insn2a03[0x9b] = m6502_9b;
        insn2a03[0x9c] = m6502_9c; insn2a03[0x9d] = m6502_9d; insn2a03[0x9e] = m6502_9e; insn2a03[0x9f] = m6502_9f;

        insn2a03[0xa0] = m6502_a0; insn2a03[0xa1] = m6502_a1; insn2a03[0xa2] = m6502_a2; insn2a03[0xa3] = m6502_a3;
        insn2a03[0xa4] = m6502_a4; insn2a03[0xa5] = m6502_a5; insn2a03[0xa6] = m6502_a6; insn2a03[0xa7] = m6502_a7;
        insn2a03[0xa8] = m6502_a8; insn2a03[0xa9] = m6502_a9; insn2a03[0xaa] = m6502_aa; insn2a03[0xab] = m6502_ab;
        insn2a03[0xac] = m6502_ac; insn2a03[0xad] = m6502_ad; insn2a03[0xae] = m6502_ae; insn2a03[0xaf] = m6502_af;

        insn2a03[0xb0] = m6502_b0; insn2a03[0xb1] = m6502_b1; insn2a03[0xb2] = m6502_b2; insn2a03[0xb3] = m6502_b3;
        insn2a03[0xb4] = m6502_b4; insn2a03[0xb5] = m6502_b5; insn2a03[0xb6] = m6502_b6; insn2a03[0xb7] = m6502_b7;
        insn2a03[0xb8] = m6502_b8; insn2a03[0xb9] = m6502_b9; insn2a03[0xba] = m6502_ba; insn2a03[0xbb] = m6502_bb; 
        insn2a03[0xbc] = m6502_bc; insn2a03[0xbd] = m6502_bd; insn2a03[0xbe] = m6502_be; insn2a03[0xbf] = m6502_bf;

        insn2a03[0xc0] = m6502_c0; insn2a03[0xc1] = m6502_c1; insn2a03[0xc2] = m6502_c2; insn2a03[0xc3] = m6502_c3;
        insn2a03[0xc4] = m6502_c4; insn2a03[0xc5] = m6502_c5; insn2a03[0xc6] = m6502_c6; insn2a03[0xc7] = m6502_c7;
        insn2a03[0xc8] = m6502_c8; insn2a03[0xc9] = m6502_c9; insn2a03[0xca] = m6502_ca; insn2a03[0xcb] = m6502_cb; 
        insn2a03[0xcc] = m6502_cc; insn2a03[0xcd] = m6502_cd; insn2a03[0xce] = m6502_ce; insn2a03[0xcf] = m6502_cf;

        insn2a03[0xd0] = m6502_d0; insn2a03[0xd1] = m6502_d1; insn2a03[0xd2] = m6502_d2; insn2a03[0xd3] = m6502_d3;
        insn2a03[0xd4] = m6502_d4; insn2a03[0xd5] = m6502_d5; insn2a03[0xd6] = m6502_d6; insn2a03[0xd7] = m6502_d7;
        insn2a03[0xd8] = m6502_d8; insn2a03[0xd9] = m6502_d9; insn2a03[0xda] = m6502_da; insn2a03[0xdb] = m6502_db;
        insn2a03[0xdc] = m6502_dc; insn2a03[0xdd] = m6502_dd; insn2a03[0xde] = m6502_de; insn2a03[0xdf] = m6502_df;

        insn2a03[0xe0] = m6502_e0; insn2a03[0xe1] = n2a03_e1; insn2a03[0xe2] = m6502_e2; insn2a03[0xe3] = m6502_e3;
        insn2a03[0xe4] = m6502_e4; insn2a03[0xe5] = n2a03_e5; insn2a03[0xe6] = m6502_e6; insn2a03[0xe7] = m6502_e7;
        insn2a03[0xe8] = m6502_e8; insn2a03[0xe9] = n2a03_e9; insn2a03[0xea] = m6502_ea; insn2a03[0xeb] = m6502_eb;
        insn2a03[0xec] = m6502_ec; insn2a03[0xed] = n2a03_ed; insn2a03[0xee] = m6502_ee; insn2a03[0xef] = m6502_ef;

        insn2a03[0xf0] = m6502_f0; insn2a03[0xf1] = n2a03_f1; insn2a03[0xf2] = m6502_f2; insn2a03[0xf3] = m6502_f3;
        insn2a03[0xf4] = m6502_f4; insn2a03[0xf5] = n2a03_f5; insn2a03[0xf6] = m6502_f6; insn2a03[0xf7] = m6502_f7;
        insn2a03[0xf8] = m6502_f8; insn2a03[0xf9] = n2a03_f9; insn2a03[0xfa] = m6502_fa; insn2a03[0xfb] = m6502_fb; 
        insn2a03[0xfc] = m6502_fc; insn2a03[0xfd] = n2a03_fd; insn2a03[0xfe] = m6502_fe; insn2a03[0xff] = m6502_ff;
    }
    opcode n2a03_61 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 6; int tmp=RD_IDX(); ADC_NES(tmp);		   
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d 61 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
     
    }}; /* 6 ADC IDX */
    opcode n2a03_e1 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 6; int tmp=RD_IDX(); SBC_NES(tmp);		   
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d e1 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 6 SBC IDX */
    opcode n2a03_71 = new opcode() { public void handler()
    {  m6502_ICount[0] -= 5; int tmp=RD_IDY(); ADC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d 71 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 5 ADC IDY */
    opcode n2a03_f1 = new opcode() { public void handler()
    {  m6502_ICount[0] -= 5; int tmp=RD_IDY(); SBC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d f1 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
        
    }}; /* 5 SBC IDY */
    opcode n2a03_65 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 3; 
        int tmp=RD_ZPG(); 
        ADC_NES(tmp);
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d m6502_65 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
   
    }}; /* 3 ADC ZPG */
    opcode n2a03_e5 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 3; 
        int tmp=RD_ZPG(); 
        SBC_NES(tmp);
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d m6502_e5 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 3 SBC ZPG */
    opcode n2a03_75 = new opcode() { public void handler()
    {  m6502_ICount[0] -= 4; int tmp=RD_ZPX(); ADC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d 75 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
        
    }}; /* 4 ADC ZPX */
    opcode n2a03_f5 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 4; 
        int tmp=RD_ZPX(); 
        SBC_NES(tmp);		  
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d m6502_f5 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 4 SBC ZPX */
    opcode n2a03_69 = new opcode() { public void handler(){  
         m6502_ICount[0] -= 2; 
         int tmp=RD_IMM(); 
         ADC_NES(tmp);
         //if(m6502log!=null) fprintf(m6502log,"M6502#%d 69 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 2 ADC IMM */
    opcode n2a03_e9 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 2; 
        int tmp=RD_IMM(); 
        SBC_NES(tmp);		  
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d m6502_e9 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);          
    
    }}; /* 2 SBC IMM */
    opcode n2a03_79 = new opcode() { public void handler()
    {  
        m6502_ICount[0] -= 4; 
        int tmp=RD_ABY(); 
        ADC_NES(tmp);		
        //if(m6502log!=null) fprintf(m6502log,"M6502#%d 79 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 ADC ABY */
    opcode n2a03_f9 = new opcode() { public void handler()
    {  m6502_ICount[0] -= 4; 
       int tmp=RD_ABY(); 
       SBC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d f9 :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 SBC ABY */
    opcode n2a03_6d = new opcode() { public void handler()
    {  
       m6502_ICount[0] -= 4; 
       int tmp=RD_ABS(); 
       ADC_NES(tmp);		  
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d 6d :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 ADC ABS */
    opcode n2a03_ed = new opcode() { public void handler()
    {  
       m6502_ICount[0] -= 4; 
       int tmp=RD_ABS(); 
       SBC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d ed :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 SBC ABS */
    opcode n2a03_7d = new opcode() { public void handler()
    {  
       m6502_ICount[0] -= 4; 
       int tmp =RD_ABX(); 
       ADC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d 7d :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 ADC ABX */
    opcode n2a03_fd = new opcode() { public void handler()
    {  
       m6502_ICount[0] -= 4; 
       int tmp =RD_ABX(); 
       SBC_NES(tmp);		   
       //if(m6502log!=null) fprintf(m6502log,"M6502#%d fd :PC:%d,PPC:%d,SP:%d,ZP:%d,EA:%d,A:%d,X:%d,Y:%d,P:%d,p_irq:%d,a_c:%d,nmi:%d,irq:%d,so:%d\n", cpu_getactivecpu(),m6502.pc.D,m6502.ppc.D,m6502.sp.D,m6502.zp.D,m6502.ea.D,m6502.a,m6502.x,m6502.y,m6502.p,m6502.pending_irq,m6502.after_cli,m6502.nmi_state,m6502.irq_state,m6502.so_state);               
    
    }}; /* 4 SBC ABX */
}
