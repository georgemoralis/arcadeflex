/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.konami;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static cpu.konami.konamiH.*;
import static mame.memory.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;


public class konami extends cpu_interface{
    public static abstract interface konami_cpu_setlines_callbackPtr { public abstract void handler(int lines); }
    public int[] konami_ICount = new int[1];
    public static konami_cpu_setlines_callbackPtr konami_cpu_setlines_callback;
    public konami()
    {
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
        Setup_konamiMain_Tables();
        Setup_konamiIndexed_Tables();
        Setup_konamiDirect_Tables();
        Setup_konamiExtended_Tables();
        
    }
    opcode[] konami_main    = new opcode[256];
    opcode[] konami_indexed = new opcode[256];
    opcode[] konami_direct  = new opcode[256];
    opcode[] konami_extended= new opcode[256];
    
    public void Setup_konamiMain_Tables()
    {
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
	konami_main[0x08]=opcode2;      konami_main[0x09]=opcode2;  konami_main[0x0a]=opcode2;  konami_main[0x0b]=opcode2;  
        konami_main[0x0c]=pshs;  /*,pshu   ,*/konami_main[0x0e]=puls;   /*,pulu   ,*/
        konami_main[0x10]=lda_im;       konami_main[0x11]=ldb_im;   konami_main[0x12]=opcode2;  konami_main[0x13]=opcode2;  
        konami_main[0x14]=adda_im;  /*addb_im,*/                konami_main[0x16]=opcode2;  konami_main[0x17]=opcode2;	/* 10 */
	konami_main[0x18]=adca_im;      /*,adcb_im,*/               konami_main[0x1a]=opcode2;  konami_main[0x1b]=opcode2;
        konami_main[0x1c]=suba_im;      konami_main[0x1d]=subb_im;  konami_main[0x1e]=opcode2;  konami_main[0x1f]=opcode2;
/*	sbca_im,sbcb_im,opcode2,opcode2,*/
        konami_main[0x24]=anda_im;      konami_main[0x25]=andb_im;  konami_main[0x26]=opcode2;  konami_main[0x27]=opcode2;	/* 20 */
        konami_main[0x28]=bita_im;      konami_main[0x29]=bitb_im;  konami_main[0x2a]=opcode2;  konami_main[0x2b]=opcode2;
        konami_main[0x2c]=eora_im;      konami_main[0x2d]=eorb_im;  konami_main[0x2e]=opcode2;  konami_main[0x2f]=opcode2;
	konami_main[0x30]=ora_im;       konami_main[0x31]=orb_im;   konami_main[0x32]=opcode2;  konami_main[0x33]=opcode2; 
        konami_main[0x34]=cmpa_im;      konami_main[0x35]=cmpb_im;  konami_main[0x36]=opcode2;  konami_main[0x37]=opcode2;	/* 30 */
	konami_main[0x38]=setline_im;   konami_main[0x39]=opcode2;  konami_main[0x3a]=opcode2;  konami_main[0x3b]=opcode2;
        konami_main[0x3c]=andcc;        konami_main[0x3d]=orcc;     konami_main[0x3e]=exg;      konami_main[0x3f]=tfr;
        konami_main[0x40]=ldd_im;       konami_main[0x41]=opcode2;  konami_main[0x42]=ldx_im;   konami_main[0x43]=opcode2;
        konami_main[0x44]=ldy_im;       konami_main[0x45]=opcode2;  konami_main[0x46]=ldu_im;   konami_main[0x47]=opcode2;	/* 40 */
	konami_main[0x48]=lds_im;       konami_main[0x49]=opcode2;  konami_main[0x4a]=cmpd_im;  konami_main[0x4b]=opcode2;
        konami_main[0x4c]=cmpx_im;      konami_main[0x4d]=opcode2;  konami_main[0x4e]=cmpy_im;  konami_main[0x4f]=opcode2;
	konami_main[0x50]=cmpu_im;      konami_main[0x51]=opcode2;/*cmps_im,opcode2,*/
        konami_main[0x54]=addd_im;      konami_main[0x55]=opcode2;  konami_main[0x56]=subd_im;  konami_main[0x57]=opcode2;	/* 50 */
	konami_main[0x58]=opcode2;      konami_main[0x59]=opcode2;  konami_main[0x5a]=opcode2;  konami_main[0x5b]=opcode2;
        konami_main[0x5c]=opcode2;      konami_main[0x5d]=illegal;  konami_main[0x5e]=illegal;  konami_main[0x5f]=illegal;
	konami_main[0x60]=bra;          konami_main[0x61]=bhi;      konami_main[0x62]=bcc;      konami_main[0x63]=bne;
        konami_main[0x64]=bvc;          konami_main[0x65]=bpl;   /* ,bge    ,bgt    ,	/* 60 */
        konami_main[0x68]=lbra;         /*,lbhi*/                   konami_main[0x6a]=lbcc;     konami_main[0x6b]=lbne;   
        /*,lbvc   ,lbpl   ,lbge   ,lbgt   ,
/*	brn*/ konami_main[0x71]=bls;konami_main[0x72]=bcs;    konami_main[0x73]=beq;    /*,bvs    ,*/konami_main[0x75]=bmi;    /*,blt    ,*/konami_main[0x77]=ble;	/* 70 */
/*	lbrn   ,lbls   ,*/konami_main[0x7a]=lbcs;  konami_main[0x7b]=lbeq;   /*,lbvs   ,lbmi   ,lblt   ,lble   ,*/
	konami_main[0x80]=clra;         konami_main[0x81]=clrb;     konami_main[0x82]=opcode2;  konami_main[0x83]=coma;
        konami_main[0x84]=comb;         konami_main[0x85]=opcode2;  konami_main[0x86]=nega;     konami_main[0x87]=negb;	/* 80 */
	konami_main[0x88]=opcode2;      konami_main[0x89]=inca;     konami_main[0x8a]=incb;     konami_main[0x8b]=opcode2;
        konami_main[0x8c]=deca;         konami_main[0x8d]=decb;     konami_main[0x8e]=opcode2;  konami_main[0x8f]=rts;
	konami_main[0x90]=tsta;         konami_main[0x91]=tstb;     konami_main[0x92]=opcode2;  konami_main[0x93]=lsra;   
        konami_main[0x94]=lsrb;         konami_main[0x95]=opcode2;  konami_main[0x96]=rora;     konami_main[0x97]=rorb;	/* 90 */
	konami_main[0x98]=opcode2;/*,asra   ,asrb   ,opcode2,*/
        konami_main[0x9c]=asla;     konami_main[0x9d]=aslb;     konami_main[0x9e]=opcode2;  konami_main[0x9f]=rti;
	konami_main[0xa0]=rola;   /*,rolb   ,opcode2,opcode2,opcode2,opcode2,opcode2,opcode2,	/* a0 */
	konami_main[0xa8]=opcode2;  konami_main[0xa9]=opcode2;  konami_main[0xaa]=bsr;      konami_main[0xab]=lbsr;  
        konami_main[0xac]=decbjnz;/*,decxjnz,*/konami_main[0xae]=nop;    konami_main[0xaf]=illegal;
        konami_main[0xb0]=abx;          konami_main[0xb1]=daa;	   konami_main[0xb2]=sex;       konami_main[0xb3]=mul;   
        /* ,lmul   ,divx   ,*/konami_main[0xb6]=bmove;  /*,move   ,	/* b0 */
/*TODO*///	lsrd   ,opcode2,rord   ,opcode2,asrd   ,opcode2,asld   ,opcode2,
/*	rold   ,opcode2,*/konami_main[0xc2]=clrd;   konami_main[0xc3]=opcode2; /*negd   ,opcode2,incd   ,opcode2,	/* c0 */
/*	decd   ,opcode2,tstd   ,opcode2,absa   ,*/konami_main[0xcd]=absb;   /*,absd   ,*/konami_main[0xcf]=bset;
	konami_main[0xd0]=bset2;    konami_main[0xd1]=illegal;     konami_main[0xd2]=illegal;   konami_main[0xd3]=illegal;
        konami_main[0xd4]=illegal;  konami_main[0xd5]=illegal;     konami_main[0xd6]=illegal;   konami_main[0xd7]=illegal;	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal
    }
    public void Setup_konamiIndexed_Tables()
    {
 	konami_indexed[0x00]=illegal;   konami_indexed[0x01]=illegal;   konami_indexed[0x02]=illegal;   konami_indexed[0x03]=illegal;
        konami_indexed[0x04]=illegal;   konami_indexed[0x05]=illegal;   konami_indexed[0x06]=illegal;   konami_indexed[0x07]=illegal;	/* 00 */
    	konami_indexed[0x08]=leax;      konami_indexed[0x09]=leay;      konami_indexed[0x0a]=leau;      konami_indexed[0x0b]=leas;
        konami_indexed[0x0c]=illegal;   konami_indexed[0x0d]=illegal;   konami_indexed[0x0e]=illegal;   konami_indexed[0x0f]=illegal;
	konami_indexed[0x10]=illegal;   konami_indexed[0x11]=illegal;   konami_indexed[0x12]=lda_ix;    konami_indexed[0x13]=ldb_ix;
        konami_indexed[0x14]=illegal;   konami_indexed[0x15]=illegal;   konami_indexed[0x16]=adda_ix;   konami_indexed[0x17]=addb_ix;	/* 10 */
/*	illegal,illegal,*/konami_indexed[0x1a]=adca_ix;/*adcb_ix,illegal,illegal,suba_ix,*/konami_indexed[0x1f]=subb_ix;
/*	illegal,illegal,sbca_ix,sbcb_ix,illegal,illegal,*/konami_indexed[0x26]=anda_ix;/*andb_ix,	/* 20 */
/*TODO*///	illegal,illegal,bita_ix,bitb_ix,illegal,illegal,eora_ix,eorb_ix,
	konami_indexed[0x30]=illegal;   konami_indexed[0x31]=illegal;   konami_indexed[0x32]=ora_ix;    konami_indexed[0x33]=orb_ix; 
        konami_indexed[0x34]=illegal;   konami_indexed[0x35]=illegal;   konami_indexed[0x36]=cmpa_ix; /*cmpb_ix,	/* 30 */
/*	illegal,setline_ix,*/konami_indexed[0x3a]=sta_ix; konami_indexed[0x3b]=stb_ix;//,illegal,illegal,illegal,illegal,
	konami_indexed[0x40]=illegal;   konami_indexed[0x41]=ldd_ix;    konami_indexed[0x42]=illegal;   konami_indexed[0x43]=ldx_ix;
        konami_indexed[0x44]=illegal;   konami_indexed[0x45]=ldy_ix;    konami_indexed[0x46]=illegal;   konami_indexed[0x47]=ldu_ix;	/* 40 */
/*	illegal,lds_ix ,illegal,*/konami_indexed[0x4b]=cmpd_ix; 
        konami_indexed[0x4c]=illegal;   konami_indexed[0x4d]=cmpx_ix;   konami_indexed[0x4e]=illegal; //cmpy_ix,
/*	illegal,cmpu_ix,illegal,cmps_ix,illegal,*/konami_indexed[0x55]=addd_ix; konami_indexed[0x56]=illegal; konami_indexed[0x57]=subd_ix;	/* 50 */
	konami_indexed[0x58]=std_ix;    konami_indexed[0x59]=stx_ix;    konami_indexed[0x5a]=sty_ix;    konami_indexed[0x5b]=stu_ix;
        /*,sts_ix ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*	illegal,illegal,*/konami_indexed[0x82]=clr_ix; /*,illegal,illegal,com_ix ,illegal,illegal,	/* 80 */
/*	neg_ix ,illegal,illegal,*/konami_indexed[0x8b]=inc_ix; /*,illegal,illegal,*/konami_indexed[0x8e]=dec_ix; konami_indexed[0x8f]=illegal;
/*	illegal,illegal,*/konami_indexed[0x92]=tst_ix; /*,illegal,illegal,lsr_ix ,illegal,illegal,	/* 90 */
	konami_indexed[0x98]=ror_ix;    konami_indexed[0x99]=illegal;   konami_indexed[0x9a]=illegal; //asr_ix ,illegal,illegal,asl_ix ,illegal,
/*TODO*///	illegal,illegal,rol_ix ,lsrw_ix,rorw_ix,asrw_ix,aslw_ix,rolw_ix,	/* a0 */
	konami_indexed[0xa8]=jmp_ix;    konami_indexed[0xa9]=jsr_ix; /*,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_ix,illegal,rord_ix,illegal,asrd_ix,illegal,asld_ix,
/*	illegal,rold_ix,illegal,*/konami_indexed[0xc3]=clrw_ix; /*illegal,negw_ix,illegal,incw_ix,	/* c0 */
/*TODO*///	illegal,decw_ix,illegal,tstw_ix,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal       
    }
    public void Setup_konamiDirect_Tables()
    {
 /*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*	illegal,illegal,*/konami_direct[0x12]=lda_di; konami_direct[0x13]=ldb_di; /*,illegal,illegal,*/konami_direct[0x16]=adda_di;/*,addb_di,	/* 10 */
/*	illegal,illegal,adca_di,adcb_di,illegal,illegal,suba_di,*/konami_direct[0x1f]=subb_di;
/*	illegal,illegal,sbca_di,sbcb_di,illegal,illegal,anda_di,*/konami_direct[0x27]=andb_di;	/* 20 */
/*	illegal,illegal,bita_di,bitb_di,illegal,illegal,eora_di,*/konami_direct[0x2f]=eorb_di;
	konami_direct[0x30]=illegal;    konami_direct[0x31]=illegal;    konami_direct[0x32]=ora_di;     konami_direct[0x33]=orb_di; 
        konami_direct[0x34]=illegal;    konami_direct[0x35]=illegal;    konami_direct[0x36]=cmpa_di;    konami_direct[0x37]=cmpb_di;	/* 30 */
	konami_direct[0x38]=illegal;    konami_direct[0x39]=setline_di; konami_direct[0x3a]=sta_di;     konami_direct[0x3b]=stb_di;
        konami_direct[0x3c]=illegal;    konami_direct[0x3d]=illegal;    konami_direct[0x3e]=illegal;    konami_direct[0x3f]=illegal;
	konami_direct[0x40]=illegal;    konami_direct[0x41]=ldd_di;     konami_direct[0x42]=illegal;    konami_direct[0x43]=ldx_di;
        konami_direct[0x44]=illegal;    konami_direct[0x45]=ldy_di;     konami_direct[0x46]=illegal;    konami_direct[0x47]=ldu_di;	/* 40 */
/*	illegal,lds_di ,illegal,*/ konami_direct[0x4b]=cmpd_di;/*illegal,*/konami_direct[0x4d]=cmpx_di;/*illegal,cmpy_di,*/
/*	illegal,cmpu_di,illegal,cmps_di,illegal,*/konami_direct[0x55]=addd_di;/*,illegal,subd_di,	/* 50 */
	konami_direct[0x58]=std_di; konami_direct[0x59]=stx_di;  konami_direct[0x5a]=sty_di; konami_direct[0x5b]=stu_di; //,sts_di ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*	illegal,illegal,*/konami_direct[0x82]=clr_di;/* ,illegal,illegal,com_di ,illegal,illegal,	/* 80 */
/*	neg_di ,illegal,illegal,*/konami_direct[0x8b]=inc_di; /*,illegal,illegal,*/konami_direct[0x8e]=dec_di; /*,illegal,*/
	konami_direct[0x90]=illegal;    konami_direct[0x91]=illegal;    konami_direct[0x92]=tst_di;     konami_direct[0x93]=illegal; 
        konami_direct[0x94]=illegal;/*lsr_di ,illegal,illegal,	/* 90 */
/*TODO*///	ror_di ,illegal,illegal,asr_di ,illegal,illegal,asl_di ,illegal,
/*TODO*///	illegal,illegal,rol_di ,lsrw_di,rorw_di,asrw_di,aslw_di,rolw_di,	/* a0 */
/*TODO*///	jmp_di ,jsr_di ,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_di,illegal,rord_di,illegal,asrd_di,illegal,asld_di,
/*	illegal,rold_di,illegal,*/konami_direct[0xc3]=clrw_di;/*,illegal,negw_di,illegal,incw_di,	/* c0 */
/*TODO*///	illegal,decw_di,illegal,tstw_di,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal       
    }
    public void Setup_konamiExtended_Tables()
    {
        /*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 00 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*	illegal,illegal,*/konami_extended[0x12]=lda_ex; konami_extended[0x13]=ldb_ex; /*,illegal,illegal,*/konami_extended[0x16]=adda_ex;/*,addb_ex,	/* 10 */
/*	illegal,illegal,adca_ex,adcb_ex,illegal,illegal,suba_ex,*/konami_extended[0x1f]=subb_ex;
/*TODO*///	illegal,illegal,sbca_ex,sbcb_ex,illegal,illegal,anda_ex,andb_ex,	/* 20 */
/*TODO*///	illegal,illegal,bita_ex,bitb_ex,illegal,illegal,eora_ex,eorb_ex,
/*	illegal,illegal,ora_ex ,*/konami_extended[0x33]=orb_ex; /*,illegal,illegal,cmpa_ex,*/konami_extended[0x37]=cmpb_ex;	/* 30 */
	/*illegal,setline_ex,*/konami_extended[0x3a]=sta_ex; konami_extended[0x3b]=stb_ex;/*,illegal,illegal,illegal,illegal,
/*	illegal,*/konami_extended[0x41]=ldd_ex; /*,illegal,*/konami_extended[0x43]=ldx_ex;
                konami_extended[0x44]=illegal;      konami_extended[0x45]=ldy_ex;   konami_extended[0x46]=illegal;  konami_extended[0x47]=ldu_ex;	/* 40 */
/*	illegal,lds_ex ,illegal,*/konami_extended[0x4b]=cmpd_ex;    konami_extended[0x4c]=illegal; konami_extended[0x4d]=cmpx_ex;//illegal,cmpy_ex,
/*	illegal,cmpu_ex,illegal,cmps_ex,illegal,*/konami_extended[0x55]=addd_ex; konami_extended[0x56]=illegal;konami_extended[0x57]=subd_ex;	/* 50 */
                konami_extended[0x58]=std_ex;       konami_extended[0x59]=stx_ex;       konami_extended[0x5a]=sty_ex;   konami_extended[0x5b]=stu_ex;
                /* ,sts_ex ,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 60 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* 70 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*	illegal,illegal,*/konami_extended[0x82]=clr_ex; /*,illegal,illegal,com_ex ,illegal,illegal,	/* 80 */
/*	neg_ex ,illegal,illegal,*/konami_extended[0x8b]=inc_ex; 
                konami_extended[0x8c]=illegal; konami_extended[0x8d]=illegal; konami_extended[0x8e]=dec_ex; konami_extended[0x8f]=illegal;
/*	illegal,illegal,*/konami_extended[0x92]=tst_ex; /*,illegal,illegal,lsr_ex ,illegal,illegal,	/* 90 */
/*TODO*///	ror_ex ,illegal,illegal,asr_ex ,illegal,illegal,asl_ex ,illegal,
/*TODO*///	illegal,illegal,rol_ex ,lsrw_ex,rorw_ex,asrw_ex,aslw_ex,rolw_ex,	/* a0 */
                konami_extended[0xa8]=jmp_ex; konami_extended[0xa9]=jsr_ex; //,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* b0 */
/*TODO*///	illegal,lsrd_ex,illegal,rord_ex,illegal,asrd_ex,illegal,asld_ex,
/*TODO*///	illegal,rold_ex,illegal,clrw_ex,illegal,negw_ex,illegal,incw_ex,	/* c0 */
/*TODO*///	illegal,decw_ex,illegal,tstw_ex,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* d0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* e0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal,	/* f0 */
/*TODO*///	illegal,illegal,illegal,illegal,illegal,illegal,illegal,illegal
    }
    /* Konami Registers */
    public static class konami_Regs
    {
        public /*PAIR*/ int pc; 		/* Program counter */
        public /*PAIR*/ int ppc;		/* Previous program counter */
        public int a;
        public int b;   //PAIR	d;		/* Accumulator a and b */
        public /*PAIR*/ int dp; 		/* Direct Page register (page in MSB) */
        public int u;
        public int s;//PAIR	u, s;		/* Stack pointers */
        public int x;
        public int y;//PAIR	x, y;		/* Index registers */
        public int /*UINT8*/   cc;
        public int /*UINT8*/   ireg;		/* First opcode */
        public int[] /*UINT8*/   irq_state=new int[2];
        public    int     extra_cycles; /* cycles used up by interrupts */
        public irqcallbacksPtr irq_callback;
        public int /*UINT8*/   int_state;  /* SYNC and CWAI flags */
        public int /*UINT8*/   nmi_state;
    }
    int getDreg()//compose dreg
    {
         return konami.a << 8 | konami.b; 
    }
    void setDreg(int reg) //write to dreg
    { 
        konami.a = reg >> 8 & 0xFF; 
        konami.b = reg & 0xFF; 
    }
    
    /* flag bits in the cc register */
    public static final int CC_C  =  0x01;        /* Carry */
    public static final int CC_V  =  0x02;        /* Overflow */
    public static final int CC_Z  =  0x04;        /* Zero */
    public static final int CC_N  =  0x08;        /* Negative */
    public static final int CC_II =  0x10;        /* Inhibit IRQ */
    public static final int CC_H  =  0x20;        /* Half (auxiliary) carry */
    public static final int CC_IF =  0x40;        /* Inhibit FIRQ */
    public static final int CC_E  =  0x80;        /* entire state pushed */
    
    /* Konami registers */
    private static konami_Regs konami=new konami_Regs();

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
    public int ea;

    public static final int KONAMI_CWAI		=8;	/* set when CWAI is waiting for an interrupt */
    public static final int KONAMI_SYNC		=16;	/* set when SYNC is waiting for an interrupt */
    public static final int KONAMI_LDS		=32;	/* set when LDS occured at least once */
    
    public void CHECK_IRQ_LINES()
    {
        if( konami.irq_state[KONAMI_IRQ_LINE] != CLEAR_LINE || konami.irq_state[KONAMI_FIRQ_LINE] != CLEAR_LINE )	
        {
    		konami.int_state &= ~KONAMI_SYNC; /* clear SYNC flag */			
        }
        if( konami.irq_state[KONAMI_FIRQ_LINE]!=CLEAR_LINE && ((konami.cc & CC_IF)==0) ) 
    	{																	
    		/* fast IRQ */													
    		/* HJB 990225: state already saved by CWAI? */					
    		if(( konami.int_state & KONAMI_CWAI )!=0)								
    		{																
    			konami.int_state &= ~KONAMI_CWAI;  /* clear CWAI */			
    			konami.extra_cycles += 7;		 /* subtract +7 cycles */	
                } 
                else															
    		{																
    			konami.cc &= ~CC_E;				/* save 'short' state */        
    			PUSHWORD(konami.pc);												
    			PUSHBYTE(konami.cc);												
    			konami.extra_cycles += 10;	/* subtract +10 cycles */		
    		}																
    		konami.cc |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */		
    		konami.pc=RM16(0xfff6);												
    		change_pc(konami.pc);														
    		konami.irq_callback.handler(KONAMI_FIRQ_LINE);	
        }																	
        else if( konami.irq_state[KONAMI_IRQ_LINE]!=CLEAR_LINE && ((konami.cc & CC_II)==0) )	
    	{																	
    		/* standard IRQ */												
    		/* HJB 990225: state already saved by CWAI? */					
    		if(( konami.int_state & KONAMI_CWAI )!=0)								
    		{																
    			konami.int_state &= ~KONAMI_CWAI;  /* clear CWAI flag */		
    			konami.extra_cycles += 7;		 /* subtract +7 cycles */	
    		}  
                else															
    		{																
    			konami.cc |= CC_E; 				/* save entire state */ 		
    			PUSHWORD(konami.pc);												
    			PUSHWORD(konami.u);												
    			PUSHWORD(konami.y);												
    			PUSHWORD(konami.x);												
    			PUSHBYTE(konami.dp);												
    			PUSHBYTE(konami.b);												
    			PUSHBYTE(konami.a);												
    			PUSHBYTE(konami.cc);												
    			konami.extra_cycles += 19;	 /* subtract +19 cycles */		
    		}																
    		konami.cc |= CC_II;					/* inhibit IRQ */				
    		konami.pc=RM16(0xfff8);												
    		change_pc(konami.pc);														
    		konami.irq_callback.handler(KONAMI_IRQ_LINE);					
    	}
    }
  

/*TODO*///int konami_Flags;	/* flags for speed optimization (obsolete!!) */
/*TODO*///void (*konami_cpu_setlines_callback)( int lines ) = 0; /* callback called when A16-A23 are set */
/*TODO*///
    /* these are re-defined in konami.h TO RAM, ROM or functions in memory.c */
    public int RM(int addr)
    {
        return (cpu_readmem16(addr) & 0xFF);
    }
    public void WM(int addr,int value)
    {
        cpu_writemem16(addr,value);
    }
    public char ROP(int addr)
    {
    	return cpu_readop(addr);
    }
    public char ROP_ARG(int addr)
    {
    	return cpu_readop_arg(addr);
    }
/*TODO*///
/*TODO*///#define SIGNED(a)	(UINT16)(INT16)(INT8)(a)
/*TODO*///
    //* macros to access memory */
    public int IMMBYTE()
    {
        int reg = ROP_ARG(konami.pc); 
        konami.pc = konami.pc + 1 & 0xFFFF;
        return reg & 0xFF;//insure it returns a 8bit value
    }
    public int IMMWORD()
    {
        int reg = (ROP_ARG(konami.pc)<<8) | ROP_ARG((konami.pc+1)&0xffff);
        konami.pc = konami.pc + 2 & 0xFFFF;
        return reg;
    }
    public void PUSHBYTE(int w)
    {
        konami.s = konami.s -1 & 0xFFFF; 
        WM(konami.s,w);
    }
    public void PUSHWORD(int w)
    {
        konami.s = konami.s -1 & 0xFFFF; 
        WM(konami.s,w & 0xFF); 
        konami.s = konami.s -1 & 0xFFFF;
        WM(konami.s,w >>8);
    }
    public int PULLBYTE()
    {
        int b = RM(konami.s);
        konami.s = konami.s +1 & 0xFFFF;
        return b;
    }
    public int PULLWORD()//TODO recheck
    {
        int w = RM(konami.s)<<8;
        konami.s = konami.s +1 & 0xFFFF; 
        w |= RM(konami.s);
        konami.s = konami.s +1 & 0xFFFF; 
        return w;
    }
    public void PSHUBYTE(int w)
    {
        konami.u = konami.u -1 & 0xFFFF; 
        WM(konami.u,w);
    }
    public void PSHUWORD(int w)
    {
        konami.u = konami.u -1 & 0xFFFF; 
        WM(konami.u,w & 0xFF); 
        konami.u = konami.u -1 & 0xFFFF;
        WM(konami.u,w >>8);
    }
    public int PULUBYTE()
    {
        int b = RM(konami.u);
        konami.u = konami.u +1 & 0xFFFF;
        return b;
    }
    public int PULUWORD()//TODO recheck
    {
        int w = RM(konami.u)<<8;
        konami.u = konami.u +1 & 0xFFFF; 
        w |= RM(konami.u);
        konami.u = konami.u +1 & 0xFFFF; 
        return w;
    }
    
    public void CLR_HNZVC()    {konami.cc&=~(CC_H|CC_N|CC_Z|CC_V|CC_C);}
    public void CLR_NZV() { 	konami.cc&=~(CC_N|CC_Z|CC_V); }
    /*TODO*///#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
    public void CLR_NZVC()	{ konami.cc &=~(CC_N|CC_Z|CC_V|CC_C); }
    public void CLR_Z()		{ konami.cc &=~(CC_Z); }
    public void CLR_NZC() 	{ konami.cc&=~(CC_N|CC_Z|CC_C);}
    public void CLR_ZC()	{ konami.cc&=~(CC_Z|CC_C); }

    /* macros for CC -- CC bits affected should be reset before calling */
    public void SET_Z(int a)		{if(a==0) SEZ();}
    public void SET_Z8(int a)		{SET_Z(a&0xFF);}
    public void SET_Z16(int a)		{SET_Z(a&0xFFFF);}
    public void SET_N8(int a)		{konami.cc |=((a&0x80)>>4);}
    public void SET_N16(int a)		{konami.cc|=((a&0x8000)>>12);}
    public void SET_H(int a,int b,int r){konami.cc|=(((a^b^r)&0x10)<<1);}
    public void SET_C8(int a)		{konami.cc|=((a&0x100)>>8);}
    public void SET_C16(int a)		{konami.cc|=((a&0x10000)>>16);}
    public void SET_V8(int a,int b,int r){konami.cc|=(((a^b^r^(r>>1))&0x80)>>6);}
    public void SET_V16(int a,int b,int r){konami.cc|=(((a^b^r^(r>>1))&0x8000)>>14);}
    
    static int flags8i[]=	 /* increment */
    {
    CC_Z,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    CC_N|CC_V,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N
    };
    static int flags8d[]= /* decrement */
    {
    CC_Z,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
    0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,CC_V,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,
    CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N,CC_N
    };
    public void SET_FLAGS8I(int a)	{konami.cc|=flags8i[(a)&0xff];}
    public void SET_FLAGS8D(int a)	{konami.cc |=flags8d[(a)&0xff];}
    
    /* combos */
    public void SET_NZ8(int a)			{SET_N8(a);SET_Z(a);}
    public void SET_NZ16(int a)			{SET_N16(a);SET_Z(a);}
    public void SET_FLAGS8(int a,int b,int r)	{SET_N8(r);SET_Z8(r);SET_V8(a,b,r);SET_C8(r);}
    public void SET_FLAGS16(int a,int b,int r)	{SET_N16(r);SET_Z16(r);SET_V16(a,b,r);SET_C16(r);}

    /* macros for addressing modes (postbytes have their own code) */
    public void DIRECT()//TODO rececheck!
    {
        ea=IMMBYTE();
        ea |= konami.dp <<8;  
    }
/*TODO*///#define IMM8	EAD = PCD; PC++
/*TODO*///#define IMM16	EAD = PCD; PC+=2
public void EXTENDED(){ ea=IMMWORD();}
/*TODO*///
    /*TODO*////* macros to set status flags */
    public void  SEC() {konami.cc|=CC_C;}
    /*TODO*///#define CLC CC&=~CC_C
    public void  SEZ() { konami.cc |=CC_Z;  }
    /*TODO*///#define CLZ CC&=~CC_Z
    /*TODO*///#define SEN CC|=CC_N
    /*TODO*///#define CLN CC&=~CC_N
    /*TODO*///#define SEV CC|=CC_V
    /*TODO*///#define CLV CC&=~CC_V
    /*TODO*///#define SEH CC|=CC_H
    /*TODO*///#define CLH CC&=~CC_H
    
    /* macros for convenience */
    public int DIRBYTE()
    {
        DIRECT();
        return RM(ea);
    }

    public int DIRWORD()
    {
        DIRECT();
        return RM16(ea);
    }
    public int EXTBYTE()
    {
        EXTENDED();
        return RM(ea);
    }
    public int EXTWORD()
    {
        EXTENDED();
        return RM16(ea);
    }
    /* macros for branch instructions */
    public void BRANCH(boolean f)
    {
        int t= IMMBYTE();
        if(f)
        {
            konami.pc=konami.pc+(byte)t & 0xFFFF;
            change_pc(konami.pc);  
        }
    }
    public void LBRANCH(boolean f)
    {
        int t= IMMWORD();
        if(f)
        {
            konami_ICount[0] -= 1;
            konami.pc = konami.pc + t & 0xFFFF;
            change_pc(konami.pc); 
        }
    }

    public int NXORV()  { return ((konami.cc&CC_N)^((konami.cc&CC_V)<<2)); }
    
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
    /* opcode timings */
    static int cycles1[] =
    {
            /*	 0	1  2  3  4	5  6  7  8	9  A  B  C	D  E  F */
      /*0*/  1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 5, 5, 5, 5,
      /*1*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      /*2*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      /*3*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 7, 6,
      /*4*/  3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 4, 4,
      /*5*/  4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 1, 1, 1,
      /*6*/  3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
      /*7*/  3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5,
      /*8*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 5,
      /*9*/  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 6,
      /*A*/  2, 2, 2, 4, 4, 4, 4, 4, 2, 2, 2, 2, 3, 3, 2, 1,
      /*B*/  3, 2, 2,11,22,11, 2, 4, 3, 3, 3, 3, 3, 3, 3, 3,
      /*C*/  3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 3, 2,
      /*D*/  2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      /*E*/  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      /*F*/  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    int RM16(int addr)
    {
        int i = RM(addr + 1 & 0xFFFF);
        i |= RM(addr) << 8;
        return i;
    }
    void WM16(int addr,int reg)
    {
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

    /****************************************************************************
     * Return program counter
     ****************************************************************************/
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
    /****************************************************************************/
    /* Reset registers to their initial values									*/
    /****************************************************************************/
    @Override
    public void reset(Object param) {
    	konami.int_state = 0;
    	konami.nmi_state = CLEAR_LINE;
    	konami.irq_state[0] = CLEAR_LINE;
    	konami.irq_state[0] = CLEAR_LINE;
    
    	konami.dp = 0;			/* Reset direct page register */
    
        konami.cc |= CC_II;        /* IRQ disabled */
        konami.cc |= CC_IF;        /* FIRQ disabled */
    
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
        if(errorlog!=null)fprintf(errorlog, "KONAMI#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, linestate);
	konami.irq_state[irqline] = linestate;
	if (linestate == CLEAR_LINE) return;
	CHECK_IRQ_LINES();
    }
    /****************************************************************************
     * Set IRQ vector callback
     ****************************************************************************/
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
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
	switch( regnum )
	{
		case CPU_INFO_NAME: return "KONAMI";
		case CPU_INFO_FAMILY: return "KONAMI 5000x";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_FILE: return "konami.java";
		case CPU_INFO_CREDITS: return "Copyright (C) The MAME Team 1999";
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
/*TODO*////* includes the static function prototypes and the master opcode table */
/*TODO*///#include "konamtbl.c"
/*TODO*///
/*TODO*////* includes the actual opcode implementations */
/*TODO*///#include "konamops.c"
/*TODO*///
    /* execute instructions on this CPU until icount expires */
    @Override
    public int execute(int cycles) {
        konami_ICount[0] = cycles - konami.extra_cycles;
    	konami.extra_cycles = 0;
    
    	if ((konami.int_state & (KONAMI_CWAI | KONAMI_SYNC))!=0)
    	{
    		konami_ICount[0] = 0;
    	}
    	else
    	{
    		do
    		{
    			konami.ppc = konami.pc;
    
    			konami.ireg = ROP(konami.pc);
    			konami.pc = konami.pc + 1 & 0xFFFF;
                        
                        if(konami_main[konami.ireg]!=null)
                        {
                            konami_main[konami.ireg].handler();
                        }
                        else
                        {
                            System.out.println("Unsupported konami_main instruction 0x" + Integer.toHexString(konami.ireg));
                        }

                    konami_ICount[0] -= cycles1[konami.ireg];                       
                } while( konami_ICount[0] > 0 );
    
            konami_ICount[0] -= konami.extra_cycles;
            konami.extra_cycles = 0;
        }
    
        return cycles - konami_ICount[0];
    }
 
    
       
    public burnPtr burn_function = new burnPtr() { public void handler(int cycles)
    {
     throw new UnsupportedOperationException("Not supported yet.");
    }};

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc, 0);
    }


   opcode illegal= new opcode() { public void handler()
   {
   	if( errorlog!=null )
   		fprintf(errorlog, "KONAMI: illegal opcode at %04x\n",konami.pc);
   }};
   
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____0x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $00 NEG direct ?**** */
    /*TODO*///opcode neg_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = -t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $01 ILLEGAL */
    /*TODO*///
    /*TODO*////* $02 ILLEGAL */
    /*TODO*///
    /*TODO*////* $03 COM direct -**01 */
    /*TODO*///opcode com_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	t = ~t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $04 LSR direct -0*-* */
    /*TODO*///opcode lsr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t >>= 1;
    /*TODO*///	SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $05 ILLEGAL */
    /*TODO*///
    /*TODO*////* $06 ROR direct -**-* */
    /*TODO*///opcode ror_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r= (CC & CC_C) << 7;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	r |= t>>1;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $07 ASR direct ?**-* */
    /*TODO*///opcode asr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t = (t & 0x80) | (t >> 1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $08 ASL direct ?**** */
    /*TODO*///opcode asl_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $09 ROL direct -**** */
    /*TODO*///opcode rol_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    
    /* $0A DEC direct -***- */
    opcode dec_di= new opcode() { public void handler()
    {
        int t=DIRBYTE();
	t= t-1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8D(t);
    	WM(ea,t);
    }};

    /* $OC INC direct -***- */
    opcode inc_di= new opcode() { public void handler()
    {
        int t=DIRBYTE();
    	t=t+1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8I(t);
    	WM(ea,t);    
    }};
    
    /* $OD TST direct -**0- */
    opcode tst_di= new opcode() { public void handler()
    {
        int t=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(t);
    }};
    
    /* $0E JMP direct ----- */
    /*TODO*///opcode jmp_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///    DIRECT;
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    
    /* $0F CLR direct -0100 */
    opcode clr_di= new opcode() { public void handler()
    {
        DIRECT();
    	WM(ea,0);
    	CLR_NZVC();
    	SEZ();
    }};
    

    /* $12 NOP inherent ----- */
    opcode nop= new opcode() { public void handler()
    {
    	;
    }};
    
    /*TODO*////* $13 SYNC inherent ----- */
    /*TODO*///opcode sync= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
    /*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
    /*TODO*///	/* is disabled, execution continues with the next instruction. */
    /*TODO*///	konami.int_state |= KONAMI_SYNC;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///	/* if KONAMI_SYNC has not been cleared by CHECK_IRQ_LINES,
    /*TODO*///	 * stop execution until the interrupt lines change. */
    /*TODO*///	if( (konami.int_state & KONAMI_SYNC) && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $14 ILLEGAL */
    /*TODO*///
    /*TODO*////* $15 ILLEGAL */
    
    /* $16 LBRA relative ----- */
    opcode lbra= new opcode() { public void handler()
    {
       ea=IMMWORD();
       konami.pc = konami.pc + ea & 0xFFFF;
       change_pc(konami.pc);
    
    	/* EHC 980508 speed up busy loop */
    	if( ea == 0xfffd && konami_ICount[0] > 0 )
    		konami_ICount[0] = 0;
    }};
    
    /* $17 LBSR relative ----- */
    opcode lbsr= new opcode() { public void handler()
    {
        ea=IMMWORD();
    	PUSHWORD(konami.pc);
    	konami.pc=konami.pc + ea &0xFFFF;
    	change_pc(konami.pc);
    }};
   
    /* $19 DAA inherent (A) -**0* */
    opcode daa= new opcode() { public void handler()
    {
        int/*UINT8*/ msn, lsn;
    	int/*UINT16*/ t, cf = 0;
    	msn = konami.a & 0xf0; 
        lsn = konami.a & 0x0f;
    	if( lsn>0x09 || (konami.cc & CC_H)!=0) cf |= 0x06;
    	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
    	if( msn>0x90 || (konami.cc & CC_C)!=0) cf |= 0x60;
    	t = cf + konami.a & 0xFFFF;//should be unsigned???
    	CLR_NZV(); /* keep carry from previous operation */
    	SET_NZ8(/*(UINT8)*/t & 0xFF); 
        SET_C8(t);
    	konami.a = t & 0xFF;
    }};

    
    /* $1A ORCC immediate ##### */
    opcode orcc= new opcode() { public void handler()
    {
        int t=	IMMBYTE();
    	konami.cc |= t;
    	CHECK_IRQ_LINES();
    }};
    
    /* $1C ANDCC immediate ##### */
    opcode andcc= new opcode() { public void handler()
    {
        int t= IMMBYTE();
    	konami.cc &= t;
    	CHECK_IRQ_LINES();
    }};
    
    /* $1D SEX inherent -**0- */
    opcode sex= new opcode() { public void handler()
    {
        int t = (byte)konami.b & 0xFFFF;
        setDreg(t);
    	CLR_NZV();
    	SET_NZ16(t);
    }};
    
    /* $1E EXG inherent ----- */
    opcode exg= new opcode() { public void handler()//different from 6809
    {
    	/*UINT16*/int t1,t2;
    	/*UINT8*/int tb;
    
    	tb=IMMBYTE();
    
    	//GETREG( t1, tb >> 4 );
        switch(tb >> 4) {						
            case 0: t1 = konami.a;	break;			
            case 1: t1 = konami.b; 	break; 			
            case 2: t1 = konami.x; 	break;			
            case 3: t1 = konami.y;	break; 			
            case 4: t1 = konami.s; 	break; /* ? */	
            case 5: t1 = konami.u;	break;			
            default: t1 = 0xff; if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        }
        //GETREG( t2, tb & 0x0f );
        switch(tb & 0x0f) {						
            case 0: t2 = konami.a;	break;			
            case 1: t2 = konami.b; 	break; 			
            case 2: t2 = konami.x; 	break;			
            case 3: t2 = konami.y;	break; 			
            case 4: t2 = konami.s; 	break; /* ? */	
            case 5: t2 = konami.u;	break;			
            default: t2 = 0xff; if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        } 
    	//SETREG( t2, tb >> 4 );
        switch(tb >> 4) {						
            case 0: konami.a = t2;	break;			
            case 1: konami.b = t2;	break;			
            case 2: konami.x = t2; 	break;			
            case 3: konami.y = t2;	break;			
            case 4: konami.s = t2;	break; /* ? */	
            case 5: konami.u = t2; 	break;			
            default: if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        }
    	//SETREG( t1, tb & 0x0f );
        switch(tb & 0x0f) {						
            case 0: konami.a = t1;	break;			
            case 1: konami.b = t1;	break;			
            case 2: konami.x = t1; 	break;			
            case 3: konami.y = t1;	break;			
            case 4: konami.s = t1;	break; /* ? */	
            case 5: konami.u = t1; 	break;			
            default: if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        }
    }};
    
    /* $1F TFR inherent ----- */
    opcode tfr= new opcode() { public void handler()
    {
        /*UINT8*/int tb;
    	/*UINT16*/ int t=0;
    
    	tb=IMMBYTE();
    
    	//GETREG( t, tb & 0x0f );
        switch(tb & 0x0f) {						
            case 0: t = konami.a;	break;			
            case 1: t = konami.b; 	break; 			
            case 2: t = konami.x; 	break;			
            case 3: t = konami.y;	break; 			
            case 4: t = konami.s; 	break; /* ? */	
            case 5: t = konami.u;	break;			
            default: t = 0xff; if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        } 
    	//SETREG( t, ( tb >> 4 ) & 0x07 );
        switch(( tb >> 4 ) & 0x07) {						
            case 0: konami.a = t;	break;			
            case 1: konami.b = t;	break;			
            case 2: konami.x = t; 	break;			
            case 3: konami.y = t;	break;			
            case 4: konami.s = t;	break; /* ? */	
            case 5: konami.u = t; 	break;			
            default: if ( errorlog!=null ) fprintf( errorlog, "Unknown TFR/EXG idx at PC:%04x\n", konami.pc ); break; 
        }
    }};

    
    /* $20 BRA relative ----- */
    opcode bra= new opcode() { public void handler()
    {
        int t;
        t=IMMBYTE();
        konami.pc=konami.pc+(byte)t & 0xFFFF;
        change_pc(konami.pc);
    	/* JB 970823 - speed up busy loops */
    	if( t == 0xfe && konami_ICount[0] > 0 )
    		 konami_ICount[0] = 0;

    }};
    
    /*TODO*////* $21 BRN relative ----- */
    /*TODO*///opcode brn= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1021 LBRN relative ----- */
    /*TODO*///opcode lbrn= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	IMMWORD(ea);
    /*TODO*///}};
    
    /* $22 BHI relative ----- */
    opcode bhi= new opcode() { public void handler()
    {
        BRANCH( (konami.cc & (CC_Z|CC_C))==0 );
    }};
    
    /* $1022 LBHI relative ----- */
    opcode lbhi= new opcode() { public void handler()
    {
        LBRANCH( (konami.cc & (CC_Z|CC_C))==0 );
    }};
    
    /* $23 BLS relative ----- */
    opcode bls= new opcode() { public void handler()
    {
        BRANCH( (konami.cc & (CC_Z|CC_C))!=0 );
    }};
    
    /* $1023 LBLS relative ----- */
    opcode lbls= new opcode() { public void handler()
    {
    	LBRANCH( (konami.cc & (CC_Z|CC_C))!=0 );
    }};
    
    /* $24 BCC relative ----- */
    opcode bcc= new opcode() { public void handler()
    {
    	BRANCH( (konami.cc&CC_C)==0 );
    }};
    
    /* $1024 LBCC relative ----- */
    opcode lbcc= new opcode() { public void handler()
    {
        LBRANCH( (konami.cc&CC_C) ==0);
    }};
    
    /* $25 BCS relative ----- */
    opcode bcs= new opcode() { public void handler()
    {
    	BRANCH( (konami.cc&CC_C)!=0 );
    }};
    
    /* $1025 LBCS relative ----- */
    opcode lbcs= new opcode() { public void handler()
    {
    	LBRANCH( (konami.cc&CC_C)!=0 );
    }};
    
    /* $26 BNE relative ----- */
    opcode bne= new opcode() { public void handler()
    {
        BRANCH( (konami.cc&CC_Z)==0 );
    }};
    
    /* $1026 LBNE relative ----- */
    opcode lbne= new opcode() { public void handler()
    {
    	LBRANCH( (konami.cc&CC_Z)==0 );
    }};
    
    /* $27 BEQ relative ----- */
    opcode beq= new opcode() { public void handler()
    {
        BRANCH( (konami.cc&CC_Z)!=0 );
    }};
    
    /* $1027 LBEQ relative ----- */
    opcode lbeq= new opcode() { public void handler()
    {
        LBRANCH( (konami.cc&CC_Z)!=0 );
    }};
    
    /* $28 BVC relative ----- */
    opcode bvc= new opcode() { public void handler()
    {
        BRANCH( (konami.cc&CC_V)==0 );
    }};
    
    /* $1028 LBVC relative ----- */
    /*TODO*///opcode lbvc= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $29 BVS relative ----- */
    /*TODO*///opcode bvs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( (CC&CC_V) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $1029 LBVS relative ----- */
    /*TODO*///opcode lbvs= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (CC&CC_V) );
    /*TODO*///}};
    
    /* $2A BPL relative ----- */
    opcode bpl= new opcode() { public void handler()
    {
        BRANCH( (konami.cc&CC_N)==0 );
    }};
    
    /* $102A LBPL relative ----- */
    opcode lbpl= new opcode() { public void handler()
    {
    	LBRANCH( (konami.cc&CC_N)==0 );
    }};
    
    /* $2B BMI relative ----- */
    opcode bmi= new opcode() { public void handler()
    {
    	BRANCH( (konami.cc&CC_N)!=0 );
    }};
    
    /* $102B LBMI relative ----- */
    opcode lbmi= new opcode() { public void handler()
    {
    	LBRANCH( (konami.cc&CC_N)!=0 );
    }};
    /*TODO*///
    /*TODO*////* $2C BGE relative ----- */
    /*TODO*///opcode bge= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102C LBGE relative ----- */
    /*TODO*///opcode lbge= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2D BLT relative ----- */
    /*TODO*///opcode blt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102D LBLT relative ----- */
    /*TODO*///opcode lblt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( NXORV );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $2E BGT relative ----- */
    /*TODO*///opcode bgt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	BRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $102E LBGT relative ----- */
    /*TODO*///opcode lbgt= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( !(NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /* $2F BLE relative ----- */
    opcode ble= new opcode() { public void handler()
    {
        BRANCH( (NXORV()!=0 || (konami.cc&CC_Z)!=0) );
    }};
    /*TODO*///
    /*TODO*////* $102F LBLE relative ----- */
    /*TODO*///opcode lble= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	LBRANCH( (NXORV || (CC&CC_Z)) );
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____3x____
    /*TODO*///#endif
    /*TODO*///
    /* $30 LEAX indexed --*-- */
    opcode leax= new opcode() { public void handler()
    {
        konami.x = ea;
    	CLR_Z();
    	SET_Z(konami.x);       
    }};
    
    /* $31 LEAY indexed --*-- */
    opcode leay= new opcode() { public void handler()
    {
        konami.y = ea;
    	CLR_Z();
    	SET_Z(konami.y);
    }};
    
    /* $32 LEAS indexed ----- */
    opcode leas= new opcode() { public void handler()
    {
    	konami.s = ea;
    	konami.int_state |= KONAMI_LDS;
    }};
    
    /* $33 LEAU indexed ----- */
    opcode leau= new opcode() { public void handler()
    {
    	konami.u = ea;
    }};
    /* $34 PSHS inherent ----- */
    opcode pshs= new opcode() { public void handler()
    {
         
        int t=IMMBYTE();
    	if((  t&0x80 )!=0) { PUSHWORD(konami.pc); konami_ICount[0]-= 2; }
    	if((  t&0x40 )!=0) { PUSHWORD(konami.u);  konami_ICount[0]-= 2; }
    	if((  t&0x20 )!=0) { PUSHWORD(konami.y);  konami_ICount[0]-= 2; }
    	if((  t&0x10 )!=0) { PUSHWORD(konami.x);  konami_ICount[0]-= 2; }
    	if((  t&0x08 )!=0) { PUSHBYTE(konami.dp);  konami_ICount[0]-= 1; }
    	if((  t&0x04 )!=0) { PUSHBYTE(konami.b);   konami_ICount[0]-= 1; }
    	if((  t&0x02 )!=0) { PUSHBYTE(konami.a);   konami_ICount[0]-= 1; }
    	if((  t&0x01 )!=0) { PUSHBYTE(konami.cc);  konami_ICount[0]-= 1; }
    }};
    
    /* 35 PULS inherent ----- */
    opcode puls= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	if(( t&0x01 )!=0) { konami.cc=PULLBYTE(); konami_ICount[0] -= 1; }
    	if(( t&0x02 )!=0) { konami.a=PULLBYTE();  konami_ICount[0] -= 1; }
    	if(( t&0x04 )!=0) { konami.b=PULLBYTE();  konami_ICount[0] -= 1; }
    	if(( t&0x08 )!=0) { konami.dp=PULLBYTE(); konami_ICount[0] -= 1; }
    	if(( t&0x10 )!=0) { konami.x=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x20 )!=0) { konami.y=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x40 )!=0) { konami.u=PULLWORD(); konami_ICount[0] -= 2; }
    	if(( t&0x80 )!=0) { konami.pc=PULLWORD(); change_pc(konami.pc); konami_ICount[0] -= 2; }
    
    	/* check after all PULLs */
    	if(( t&0x01 )!=0) { CHECK_IRQ_LINES(); }
        
    }};
    
    /* $36 PSHU inherent ----- */
    /*TODO*///opcode pshu= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if( t&0x80 ) { PSHUWORD(pPC); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x40 ) { PSHUWORD(pS);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x20 ) { PSHUWORD(pY);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x10 ) { PSHUWORD(pX);  konami_ICount -= 2; }};
    /*TODO*///	if( t&0x08 ) { PSHUBYTE(DP);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x04 ) { PSHUBYTE(B);   konami_ICount -= 1; }};
    /*TODO*///	if( t&0x02 ) { PSHUBYTE(A);   konami_ICount -= 1; }};
    /*TODO*///	if( t&0x01 ) { PSHUBYTE(CC);  konami_ICount -= 1; }};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* 37 PULU inherent ----- */
    /*TODO*///opcode pulu= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	if( t&0x01 ) { PULUBYTE(CC); konami_ICount -= 1; }};
    /*TODO*///	if( t&0x02 ) { PULUBYTE(A);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x04 ) { PULUBYTE(B);  konami_ICount -= 1; }};
    /*TODO*///	if( t&0x08 ) { PULUBYTE(DP); konami_ICount -= 1; }};
    /*TODO*///	if( t&0x10 ) { PULUWORD(XD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x20 ) { PULUWORD(YD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x40 ) { PULUWORD(SD); konami_ICount -= 2; }};
    /*TODO*///	if( t&0x80 ) { PULUWORD(PCD); change_pc(PCD); konami_ICount -= 2; }};
    /*TODO*///
    /*TODO*///	/* check after all PULLs */
    /*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }};
    /*TODO*///}};
    
    /* $38 ILLEGAL */
    
    /* $39 RTS inherent ----- */
    opcode rts= new opcode() { public void handler()
    {
        konami.pc=PULLWORD();
    	change_pc(konami.pc);
    }};
    
    /* $3A ABX inherent ----- */
    opcode abx= new opcode() { public void handler()
    {
    	konami.x=konami.x+konami.b & 0xFFFF;
    }};
    
    /* $3B RTI inherent ##### */
    opcode rti= new opcode() { public void handler()
    {
        int t;
    	konami.cc=PULLBYTE();
    	t = konami.cc & CC_E;		/* HJB 990225: entire state saved? */
    	if(t!=0)
    	{
            konami_ICount[0] -= 9;
    		konami.a=PULLBYTE();
    		konami.b=PULLBYTE();
    		konami.dp=PULLBYTE();
    		konami.x=PULLWORD();
    		konami.y=PULLWORD();
    		konami.u=PULLWORD();
    	}
    	konami.pc=PULLWORD();
    	change_pc(konami.pc);
    	CHECK_IRQ_LINES();
        
    }};
    
    /*TODO*////* $3C CWAI inherent ----1 */
    /*TODO*///opcode cwai= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	CC &= t;
    /*TODO*///	/*
    /*TODO*///     * CWAI stacks the entire machine state on the hardware stack,
    /*TODO*///     * then waits for an interrupt; when the interrupt is taken
    /*TODO*///     * later, the state is *not* saved again after CWAI.
    /*TODO*///     */
    /*TODO*///	CC |= CC_E; 		/* HJB 990225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	konami.int_state |= KONAMI_CWAI;
    /*TODO*///	CHECK_IRQ_LINES;
    /*TODO*///	if( (konami.int_state & KONAMI_CWAI) && konami_ICount > 0 )
    /*TODO*///		konami_ICount = 0;
    /*TODO*///}};
    
    /* $3D MUL inherent --*-@ */
    opcode mul= new opcode() { public void handler()
    {
        int t;
    	t = konami.a * konami.b & 0xFFFF;
    	CLR_ZC(); 
        SET_Z16(t); 
        if((t&0x80)!=0) SEC();
    	setDreg(t);
        
    }};
    
    /*TODO*////* $3E ILLEGAL */
    /*TODO*///
    /*TODO*////* $3F SWI (SWI2 SWI3) absolute indirect ----- */
    /*TODO*///opcode swi= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///	PUSHBYTE(CC);
    /*TODO*///	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
    /*TODO*///	PCD=RM16(0xfffa);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $103F SWI2 absolute indirect ----- */
    /*TODO*///opcode swi2= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///    PUSHBYTE(CC);
    /*TODO*///	PCD=RM16(0xfff4);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $113F SWI3 absolute indirect ----- */
    /*TODO*///opcode swi3= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PUSHWORD(pU);
    /*TODO*///	PUSHWORD(pY);
    /*TODO*///	PUSHWORD(pX);
    /*TODO*///	PUSHBYTE(DP);
    /*TODO*///	PUSHBYTE(B);
    /*TODO*///	PUSHBYTE(A);
    /*TODO*///    PUSHBYTE(CC);
    /*TODO*///	PCD=RM16(0xfff2);
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};

    /* $40 NEGA inherent ?**** */
    opcode nega= new opcode() { public void handler()
    {
        int r;
    	r = -konami.a & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,konami.a,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $43 COMA inherent -**01 */
    opcode coma= new opcode() { public void handler()
    {
        konami.a = (konami.a ^ 0xFFFFFFFF) & 0xFF;
    	CLR_NZV();
    	SET_NZ8(konami.a);
    	SEC();
    }};
    
    /* $44 LSRA inherent -0*-* */
    opcode lsra= new opcode() { public void handler()//todo recheck
    {
        CLR_NZC();
    	konami.cc |= (konami.a & CC_C);
    	konami.a = konami.a >> 1 & 0xFF;
    	SET_Z8(konami.a);
    }};
    
    /* $46 RORA inherent -**-* */
    opcode rora= new opcode() { public void handler()
    {
        int r;
    	r = ((konami.cc & CC_C) << 7)&0xFF;
    	CLR_NZC();
    	konami.cc |= (konami.a & CC_C);
    	r = (r | konami.a >> 1)&0xFF;
    	SET_NZ8(r);
    	konami.a = r&0xFF;
    }};
    
    /*TODO*////* $47 ASRA inherent ?**-* */
    /*TODO*///opcode asra= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (A & CC_C);
    /*TODO*///	A = (A & 0x80) | (A >> 1);
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /* $48 ASLA inherent ?**** */
    opcode asla= new opcode() { public void handler()
    {
        int r = (konami.a << 1) & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.a,konami.a,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $49 ROLA inherent -**** */
    opcode rola= new opcode() { public void handler()//todo recheck
    {
         //BUGGY have to figure it out!
        int t,r;
    	t = konami.a;
   	r = ((konami.cc & CC_C) | ((t<<1))) &0xFFFF;//is that correct???
    	CLR_NZVC(); 
        SET_FLAGS8(t,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $4A DECA inherent -***- */
    opcode deca= new opcode() { public void handler()
    {
        konami.a = konami.a -1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8D(konami.a);
    }};
    
    /* $4C INCA inherent -***- */
    opcode inca= new opcode() { public void handler()
    {
        konami.a = konami.a +1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8I(konami.a);
    }};
    
    /* $4D TSTA inherent -**0- */
    opcode tsta= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.a);
    }};

    /* $4F CLRA inherent -0100 */
    opcode clra= new opcode() { public void handler()
    {
        konami.a = 0;
    	CLR_NZVC(); 
        SEZ();
    }};

    /* $50 NEGB inherent ?**** */
    opcode negb= new opcode() { public void handler()
    {
        int r;
    	r = -konami.b & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(0,konami.b,r);
    	konami.b = r & 0xFF;
    }};
    
    /* $53 COMB inherent -**01 */
    opcode comb= new opcode() { public void handler()
    {
        konami.b = (konami.b ^ 0xFFFFFFFF) & 0xFF;
    	CLR_NZV();
    	SET_NZ8(konami.b);
    	SEC();
    }};
    
    /* $54 LSRB inherent -0*-* */
    opcode lsrb= new opcode() { public void handler()
    {
        CLR_NZC();
    	konami.cc |= (konami.b & CC_C);
    	konami.b = konami.b >> 1 &0xFF;
    	SET_Z8(konami.b);
    }};
    

    /* $56 RORB inherent -**-* */
    opcode rorb= new opcode() { public void handler()
    {
        int r;
    	r = ((konami.cc & CC_C) << 7)&0xFF;
    	CLR_NZC();
    	konami.cc |= (konami.b & CC_C);
    	r = (r | konami.b >> 1)&0xFF;
    	SET_NZ8(r);
    	konami.b = r&0xFF;
    }};
    
    /*TODO*////* $57 ASRB inherent ?**-* */
    /*TODO*///opcode asrb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (B & CC_C);
    /*TODO*///	B= (B & 0x80) | (B >> 1);
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    
    /* $58 ASLB inherent ?**** */
    opcode aslb= new opcode() { public void handler()
    {
        int r = (konami.b << 1) & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,konami.b,r);
    	konami.b = r & 0xFF;
    }};
    
    /* $59 ROLB inherent -**** */
    /*TODO*///opcode rolb= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = B;
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /* $5A DECB inherent -***- */
    opcode decb= new opcode() { public void handler()
    {
       konami.b = konami.b-1&0xFF;	
       CLR_NZV();
       SET_FLAGS8D(konami.b);
       
    }};
    
    /* $5B ILLEGAL */
    
    /* $5C INCB inherent -***- */
    opcode incb= new opcode() { public void handler()
    {
        konami.b = konami.b +1 & 0xFF;
    	CLR_NZV();
    	SET_FLAGS8I(konami.b);
    }};
    
    /* $5D TSTB inherent -**0- */
    opcode tstb= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    /* $5F CLRB inherent -0100 */
    opcode clrb= new opcode() { public void handler()
    {
        konami.b = 0;
    	CLR_NZVC(); SEZ();
    }};
    
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____6x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $60 NEG indexed ?**** */
    /*TODO*///opcode neg_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = -t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $61 ILLEGAL */
    /*TODO*///
    /*TODO*////* $62 ILLEGAL */
    /*TODO*///
    /*TODO*////* $63 COM indexed -**01 */
    /*TODO*///opcode com_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = ~RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $64 LSR indexed -0*-* */
    /*TODO*///opcode lsr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $65 ILLEGAL */
    /*TODO*///
    /*TODO*////* $66 ROR indexed -**-* */
    opcode ror_ix= new opcode() { public void handler()
    {
        int t,r;
    	t=RM(ea);
    	r = (konami.cc & CC_C) << 7 &0xFF;
    	CLR_NZC();
    	konami.cc |= (t & CC_C);
    	r = r | t>>1 &0xFF;//correct???//r |= t>>1; 
        SET_NZ8(r);
    	WM(ea,r);
    }};
    
    /*TODO*////* $67 ASR indexed ?**-* */
    /*TODO*///opcode asr_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>=1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $68 ASL indexed ?**** */
    /*TODO*///opcode asl_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $69 ROL indexed -**** */
    /*TODO*///opcode rol_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = CC & CC_C;
    /*TODO*///	r |= t << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    
    /* $6A DEC indexed -***- */
    opcode dec_ix= new opcode() { public void handler()
    {
        int t;
    	t = RM(ea) - 1 & 0xFF;
    	CLR_NZV();
        SET_FLAGS8D(t);
    }};
    
    /* $6C INC indexed -***- */
    opcode inc_ix= new opcode() { public void handler()
    {
        int t;
    	t = RM(ea) + 1 &0xFF;
    	CLR_NZV(); 
        SET_FLAGS8I(t);
    	WM(ea,t);
    }};
    
    /* $6D TST indexed -**0- */
    opcode tst_ix= new opcode() { public void handler()
    {
        int t;
    	t = RM(ea);
    	CLR_NZV();
    	SET_NZ8(t);
    }};
    
   /* $6E JMP indexed ----- */
   opcode jmp_ix= new opcode() { public void handler()
   {
   	konami.pc = ea;
    	change_pc(konami.pc);	
   }};
   
    /* $6F CLR indexed -0100 */
    opcode clr_ix= new opcode() { public void handler()
    {
        WM(ea,0);
    	CLR_NZVC(); 
        SEZ();
    }};
    
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____7x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $70 NEG extended ?**** */
    /*TODO*///opcode neg_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r,t;
    /*TODO*///	EXTBYTE(t); r=-t;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(0,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $71 ILLEGAL */
    /*TODO*///
    /*TODO*////* $72 ILLEGAL */
    /*TODO*///
    /*TODO*////* $73 COM extended -**01 */
    /*TODO*///opcode com_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); t = ~t;
    /*TODO*///	CLR_NZV; SET_NZ8(t); SEC;
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $74 LSR extended -0*-* */
    /*TODO*///opcode lsr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t>>=1; SET_Z8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $75 ILLEGAL */
    /*TODO*///
    /*TODO*////* $76 ROR extended -**-* */
    /*TODO*///opcode ror_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t); r=(CC & CC_C) << 7;
    /*TODO*///	CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	r |= t>>1; SET_NZ8(r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $77 ASR extended ?**-* */
    /*TODO*///opcode asr_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
    /*TODO*///	t=(t&0x80)|(t>>1);
    /*TODO*///	SET_NZ8(t);
    /*TODO*///	WM(EAD,t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $78 ASL extended ?**** */
    /*TODO*///opcode asl_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r=t<<1;
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $79 ROL extended -**** */
    /*TODO*///opcode rol_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t); r = (CC & CC_C) | (t << 1);
    /*TODO*///	CLR_NZVC; SET_FLAGS8(t,t,r);
    /*TODO*///	WM(EAD,r);
    /*TODO*///}};
    
    /* $7A DEC extended -***- */
    opcode dec_ex= new opcode() { public void handler()
    {
        int t=EXTBYTE(); 
        t=t-1&0xFF;
        CLR_NZV(); 
        SET_FLAGS8D(t);
        WM(ea,t);
    }};
    
    /* $7B ILLEGAL */
    
    /* $7C INC extended -***- */
    opcode inc_ex= new opcode() { public void handler()
    {
        int t=EXTBYTE(); 
        t=t+1&0xFF;
    	CLR_NZV(); 
        SET_FLAGS8I(t);
    	WM(ea,t);
    }};
    
    /* $7D TST extended -**0- */
    opcode tst_ex= new opcode() { public void handler()
    {
        int t=EXTBYTE(); 
        CLR_NZV(); 
        SET_NZ8(t);
    }};
    
    /* $7E JMP extended ----- */
    opcode jmp_ex= new opcode() { public void handler()
    {
    	EXTENDED();
    	konami.pc = ea;
    	change_pc(konami.pc);
    }};
    
    /* $7F CLR extended -0100 */
    opcode clr_ex= new opcode() { public void handler()
    {
        EXTENDED();
    	WM(ea,0);
    	CLR_NZVC(); 
        SEZ();
    }};
    
    /* $80 SUBA immediate ?**** */
    opcode suba_im= new opcode() { public void handler()
    {
        int t,r;
        t=IMMBYTE();
    	r = konami.a - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $81 CMPA immediate ?**** */
    opcode cmpa_im= new opcode() { public void handler()
    {
       int t,r;
       t=IMMBYTE();
       r = (konami.a - t) & 0xFFFF;
       CLR_NZVC();
       SET_FLAGS8(konami.a,t,r);
    }};
    
    /*TODO*////* $82 SBCA immediate ?**** */
    /*TODO*///opcode sbca_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /* $83 SUBD (CMPD CMPU) immediate -**** */
    opcode subd_im= new opcode() { public void handler()
    {
        int r,d;
        int b=IMMWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    
    /* $1083 CMPD immediate -**** */
    opcode cmpd_im= new opcode() { public void handler()
    {
        int r,d;
        int b=IMMWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /* $1183 CMPU immediate -**** */
    opcode cmpu_im= new opcode() { public void handler()
    {
        int r, d;
        int b=IMMWORD();
    	d = konami.u;
    	r = (d - b); //& 0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /* $84 ANDA immediate -**0- */
    opcode anda_im= new opcode() { public void handler()//todo recheck
    {
        int t=IMMBYTE();
    	konami.a &= t;
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $85 BITA immediate -**0- */
    opcode bita_im= new opcode() { public void handler()
    {
        int t,r;
        t=IMMBYTE();
    	r = konami.a & t;
       	CLR_NZV();
    	SET_NZ8(r);
    }};
    
    /* $86 LDA immediate -**0- */
    opcode lda_im= new opcode() { public void handler()
    {
    	konami.a=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* is this a legal instruction? */
    /* $87 STA immediate -**0- */
    /*TODO*///opcode sta_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,A);
    /*TODO*///}};
    /*TODO*///
    /* $88 EORA immediate -**0- */
    opcode eora_im= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	konami.a ^= t;
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $89 ADCA immediate ***** */
    opcode adca_im= new opcode() { public void handler()
    {
        int t,r;
    	t=IMMBYTE();
    	r = konami.a + t + (konami.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $8A ORA immediate -**0- */
    opcode ora_im= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	konami.a |= t; //TODO should unsigned it??
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $8B ADDA immediate ***** */
    opcode adda_im= new opcode() { public void handler()
    {
        int t,r;
        t=IMMBYTE();
    	r = konami.a + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $8C CMPX (CMPY CMPS) immediate -**** */
    opcode cmpx_im= new opcode() { public void handler()//todo recheck
    {
        int r,d;
        int b=IMMWORD();
	d = konami.x;
    	r = (d - b); //&0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /* $108C CMPY immediate -**** */
    opcode cmpy_im= new opcode() { public void handler()
    {
        int r,d;
        int b=IMMWORD();
	d = konami.y;
    	r = (d - b); //&0xFFFF;//should be unsigned?
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /*TODO*////* $118C CMPS immediate -**** */
    /*TODO*///opcode cmps_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	IMMWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    
    /* $8D BSR ----- */
    opcode bsr= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	PUSHWORD(konami.pc);
    	konami.pc = konami.pc + (byte)t & 0xFFFF; 
    	change_pc(konami.pc);
    }};
    
    /* $8E LDX (LDY) immediate -**0- */
    opcode ldx_im= new opcode() { public void handler()
    {
        konami.x=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.x);
    }};
    
    /* $108E LDY immediate -**0- */
    opcode ldy_im= new opcode() { public void handler()
    {
        konami.y=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.y);
    }};
    
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $8F STX (STY) immediate -**0- */
    /*TODO*///opcode stx_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pX);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $108F STY immediate -**0- */
    /*TODO*///opcode sty_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(Y);
    /*TODO*///	IMM16;
    /*TODO*///	WM16(EAD,&pY);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____9x____
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////* $90 SUBA direct ?**** */
    /*TODO*///opcode suba_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    
    /* $91 CMPA direct ?**** */
    opcode cmpa_di= new opcode() { public void handler()
    {
        int t,r;
        t=DIRBYTE();
    	r = konami.a - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.a,t,r);
    }};
    
    /*TODO*////* $92 SBCA direct ?**** */
    /*TODO*///opcode sbca_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $93 SUBD (CMPD CMPU) direct -**** */
    /*TODO*///opcode subd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = D;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    
    /* $1093 CMPD direct -**** */
    opcode cmpd_di= new opcode() { public void handler()
    {
        int r,d;
        int b;
    	b=DIRWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /*TODO*////* $1193 CMPU direct -**** */
    /*TODO*///opcode cmpu_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $94 ANDA direct -**0- */
    /*TODO*///opcode anda_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $95 BITA direct -**0- */
    /*TODO*///opcode bita_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    
    /* $96 LDA direct -**0- */
    opcode lda_di= new opcode() { public void handler()
    {
        konami.a=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $97 STA direct -**0- */
    opcode sta_di= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.a);
    	DIRECT();
    	WM(ea,konami.a);
    }};
    /*TODO*///
    /*TODO*////* $98 EORA direct -**0- */
    /*TODO*///opcode eora_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $99 ADCA direct ***** */
    /*TODO*///opcode adca_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    
    /* $9A ORA direct -**0- */
    opcode ora_di= new opcode() { public void handler()
    {
        int t= DIRBYTE();
    	konami.a |= t;
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $9B ADDA direct ***** */
    opcode adda_di= new opcode() { public void handler()
    {
        int t,r;
        t=DIRBYTE();
    	r = konami.a + t;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $9C CMPX (CMPY CMPS) direct -**** */
    opcode cmpx_di= new opcode() { public void handler()
    {
        int r,d;
    	int b=DIRWORD();
    	d = konami.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    /*TODO*///
    /*TODO*////* $109C CMPY direct -**** */
    /*TODO*///opcode cmpy_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $119C CMPS direct -**** */
    /*TODO*///opcode cmps_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	DIRWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $9D JSR direct ----- */
    /*TODO*///opcode jsr_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	DIRECT;
    /*TODO*///	PUSHWORD(pPC);
    /*TODO*///	PCD=EAD;
    /*TODO*///	change_pc(PCD);
    /*TODO*///}};
    
    /* $9E LDX (LDY) direct -**0- */
    opcode ldx_di= new opcode() { public void handler()
    {
        konami.x=DIRWORD();
    	CLR_NZV();
    	SET_NZ16(konami.x);
    }};
    
    /* $109E LDY direct -**0- */
    opcode ldy_di= new opcode() { public void handler()
    {
        konami.y=DIRWORD();
    	CLR_NZV();
    	SET_NZ16(konami.y);
    }};
    
    /* $9F STX (STY) direct -**0- */
    opcode stx_di= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.x);
    	DIRECT();
    	WM16(ea,konami.x);
    }};
    
    /* $109F STY direct -**0- */
    opcode sty_di= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.y);
    	DIRECT();
    	WM16(ea,konami.y);
    }};

    /*TODO*////* $a0 SUBA indexed ?**** */
    /*TODO*///opcode suba_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    
    /* $a1 CMPA indexed ?**** */
    opcode cmpa_ix= new opcode() { public void handler()
    {
        /*UINT16*/int t,r;
    	t = RM(ea);
    	r = (konami.a - t) &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.a,t,r);
        
    }};
    
    /*TODO*////* $a2 SBCA indexed ?**** */
    /*TODO*///opcode sbca_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    
    /* $a3 SUBD (CMPD CMPU) indexed -**** */
    opcode subd_ix= new opcode() { public void handler()
    {
        int r,d;
        int b;
        b=RM16(ea);
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
        
    }};
    
    /* $10a3 CMPD indexed -**** */
    opcode cmpd_ix= new opcode() { public void handler()
    {
        int r,d;
        int b;
        b=RM16(ea);
    	d = getDreg();
    	r = (d -  b); //& 0xFFFF; //should be unsinged?????
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /* $11a3 CMPU indexed -**** */
    /*TODO*///opcode cmpu_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	r = U - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(U,b.d,r);
    /*TODO*///}};
    
    /* $a4 ANDA indexed -**0- */
    opcode anda_ix= new opcode() { public void handler()
    {
        konami.a &= RM(ea);
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /*TODO*////* $a5 BITA indexed -**0- */
    /*TODO*///opcode bita_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 r;
    /*TODO*///	r = A & RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    
    /* $a6 LDA indexed -**0- */
    opcode lda_ix= new opcode() { public void handler()
    {
        konami.a = RM(ea);
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $a7 STA indexed -**0- */
    opcode sta_ix= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.a);
    	WM(ea,konami.a);
    }};
    
    /*TODO*////* $a8 EORA indexed -**0- */
    /*TODO*///opcode eora_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	A ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    
    /* $a9 ADCA indexed ***** */
    opcode adca_ix= new opcode() { public void handler()
    {
        int t,r;
    	t = RM(ea);
    	r = konami.a + t + (konami.cc & CC_C) & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $aA ORA indexed -**0- */
    opcode ora_ix= new opcode() { public void handler()
    {
        konami.a |= RM(ea);
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $aB ADDA indexed ***** */
    opcode adda_ix= new opcode() { public void handler()
    {
        int t,r;
    	t = RM(ea);
    	r = konami.a + t &0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r &0xFF;
    }};
    
    /* $aC CMPX (CMPY CMPS) indexed -**** */
    opcode cmpx_ix= new opcode() { public void handler()
    {
        int r,d;
    	int b;
        b=RM16(ea);
    	d = konami.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    /*TODO*///
    /*TODO*////* $10aC CMPY indexed -**** */
    /*TODO*///opcode cmpy_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11aC CMPS indexed -**** */
    /*TODO*///opcode cmps_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	b.d=RM16(EAD);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    
    /* $aD JSR indexed ----- */
    opcode jsr_ix= new opcode() { public void handler()
    {
        PUSHWORD(konami.pc);
    	konami.pc = ea;
    	change_pc(konami.pc);
    }};
    
    /* $aE LDX (LDY) indexed -**0- */
    opcode ldx_ix= new opcode() { public void handler()
    {
        konami.x=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(konami.x);
    }};
    
    /* $10aE LDY indexed -**0- */
    opcode ldy_ix= new opcode() { public void handler()
    {
        konami.y=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(konami.y);    
    }};
    
    /* $aF STX (STY) indexed -**0- */
    opcode stx_ix= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.x);
    	WM16(ea,konami.x);
    }};
    
    /* $10aF STY indexed -**0- */
    opcode sty_ix= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.y);
    	WM16(ea,konami.y);  
    }};

    /*TODO*////* $b0 SUBA extended ?**** */
    /*TODO*///opcode suba_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b1 CMPA extended ?**** */
    /*TODO*///opcode cmpa_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b2 SBCA extended ?**** */
    /*TODO*///opcode sbca_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    
    /* $b3 SUBD (CMPD CMPU) extended -**** */
    opcode subd_ex= new opcode() { public void handler()
    {
        int r,d;
        int b=EXTWORD();
        d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    
    /* $10b3 CMPD extended -**** */
    opcode cmpd_ex= new opcode() { public void handler()
    {
        int r,d;
        int b=EXTWORD();
    	d = getDreg();
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    /*TODO*///
    /*TODO*////* $11b3 CMPU extended -**** */
    /*TODO*///opcode cmpu_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = U;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b4 ANDA extended -**0- */
    /*TODO*///opcode anda_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b5 BITA extended -**0- */
    /*TODO*///opcode bita_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A & t;
    /*TODO*///	CLR_NZV; SET_NZ8(r);
    /*TODO*///}};
    /*TODO*///
    /* $b6 LDA extended -**0- */
    opcode lda_ex= new opcode() { public void handler()
    {
        konami.a=EXTBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.a);
    }};
    
    /* $b7 STA extended -**0- */
    opcode sta_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.a);
    	EXTENDED();
    	WM(ea,konami.a);
    }};
    /*TODO*///
    /*TODO*////* $b8 EORA extended -**0- */
    /*TODO*///opcode eora_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $b9 ADCA extended ***** */
    /*TODO*///opcode adca_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = A + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(A,t,r);
    /*TODO*///	SET_H(A,t,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $bA ORA extended -**0- */
    /*TODO*///opcode ora_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	A |= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(A);
    /*TODO*///}};
    
    /* $bB ADDA extended ***** */
    opcode adda_ex= new opcode() { public void handler()
    {
        int  t,r;
    	t=EXTBYTE();
    	r = konami.a + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.a,t,r);
    	SET_H(konami.a,t,r);
    	konami.a = r & 0xFF;
    }};
    
    /* $bC CMPX (CMPY CMPS) extended -**** */
    opcode cmpx_ex= new opcode() { public void handler()
    {
        int r,d;
    	int b=EXTWORD();
    	d = konami.x;
    	r = d - b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    }};
    
    /*TODO*////* $10bC CMPY extended -**** */
    /*TODO*///opcode cmpy_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = Y;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $11bC CMPS extended -**** */
    /*TODO*///opcode cmps_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r,d;
    /*TODO*///	PAIR b;
    /*TODO*///	EXTWORD(b);
    /*TODO*///	d = S;
    /*TODO*///	r = d - b.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(d,b.d,r);
    /*TODO*///}};
    /*TODO*///
    /* $bD JSR extended ----- */
    opcode jsr_ex= new opcode() { public void handler()
    {
        EXTENDED();
    	PUSHWORD(konami.pc);
    	konami.pc = ea;
    	change_pc(konami.pc);
    }};
    
    /* $bE LDX (LDY) extended -**0- */
    opcode ldx_ex= new opcode() { public void handler()
    {
        konami.x=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(konami.x);
    }};
    
    /* $10bE LDY extended -**0- */
    opcode ldy_ex= new opcode() { public void handler()
    {
        konami.y=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(konami.y);
    }};
    
    /* $bF STX (STY) extended -**0- */
    opcode stx_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.x);
    	EXTENDED();
    	WM16(ea,konami.x);
    }};
    
    /* $10bF STY extended -**0- */
    opcode sty_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.y);
    	EXTENDED();
    	WM16(ea,konami.y);
    }};

    
    /* $c0 SUBB immediate ?**** */
    opcode subb_im= new opcode() { public void handler()
    {
        int t,r;
        t=IMMBYTE();
    	r = konami.b - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,t,r);
    	konami.b = r & 0xFF;

    }};
    
    /* $c1 CMPB immediate ?**** */
    opcode cmpb_im= new opcode() { public void handler()
    {
       int t,r;
       t=IMMBYTE();
       r = (konami.b - t) & 0xFFFF;
       CLR_NZVC();
       SET_FLAGS8(konami.b,t,r);
    }};
    
    /*TODO*////* $c2 SBCB immediate ?**** */
    /*TODO*///opcode sbcb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $c3 ADDD immediate -**** */
    opcode addd_im= new opcode() { public void handler()
    {
        int r,d;
        int b=IMMWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    
    /* $c4 ANDB immediate -**0- */
    opcode andb_im= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	konami.b &= t;//should be unsigned?
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /* $c5 BITB immediate -**0- */
    opcode bitb_im= new opcode() { public void handler()
    {
        int t,r;
        t=IMMBYTE();
    	r = konami.b & t;
       	CLR_NZV();
    	SET_NZ8(r);
    }};
    
    /* $c6 LDB immediate -**0- */
    opcode ldb_im= new opcode() { public void handler()
    {
        konami.b=IMMBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $c7 STB immediate -**0- */
    /*TODO*///opcode stb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///	IMM8;
    /*TODO*///	WM(EAD,B);
    /*TODO*///}};
    
    /* $c8 EORB immediate -**0- */
    opcode eorb_im= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	konami.b ^= t;
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /*TODO*////* $c9 ADCB immediate ***** */
    /*TODO*///opcode adcb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $cA ORB immediate -**0- */
    opcode orb_im= new opcode() { public void handler()
    {
        int t=IMMBYTE();
    	konami.b |= t; //TODO should unsigned it??
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /*TODO*////* $cB ADDB immediate ***** */
    /*TODO*///opcode addb_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	IMMBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /* $cC LDD immediate -**0- */
    opcode ldd_im= new opcode() { public void handler()
    {
        int temp=IMMWORD();
        setDreg(temp);
    	CLR_NZV();
    	SET_NZ16(temp);
    }};
    
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cD STD immediate -**0- */
    /*TODO*///opcode std_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pD);
    /*TODO*///}};
    
    /* $cE LDU (LDS) immediate -**0- */
    opcode ldu_im= new opcode() { public void handler()
    {
        konami.u=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.u);
    }};
    
    /* $10cE LDS immediate -**0- */
    opcode lds_im= new opcode() { public void handler()
    {
        konami.s=IMMWORD();
    	CLR_NZV();
    	SET_NZ16(konami.s);
    	konami.int_state |= KONAMI_LDS;        

    }};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $cF STU (STS) immediate -**0- */
    /*TODO*///opcode stu_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(U);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pU);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* is this a legal instruction? */
    /*TODO*////* $10cF STS immediate -**0- */
    /*TODO*///opcode sts_im= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///    IMM16;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};

    /* $d0 SUBB direct ?**** */
    opcode subb_di= new opcode() { public void handler()
    {
        int t,r;
        t=DIRBYTE();
    	r = konami.b - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,t,r);
    	konami.b = r & 0xFF;
    }};
    
    /* $d1 CMPB direct ?**** */
    opcode cmpb_di= new opcode() { public void handler()
    {
        int t,r;
        t=DIRBYTE();
    	r = konami.b - t &0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,t,r);
    }};
    /*TODO*///
    /*TODO*////* $d2 SBCB direct ?**** */
    /*TODO*///opcode sbcb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $d3 ADDD direct -**** */
    opcode addd_di= new opcode() { public void handler()
    {
        int r,d;
        int b=DIRWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    
    /* $d4 ANDB direct -**0- */
    opcode andb_di= new opcode() { public void handler()
    {
        int t=DIRBYTE();
    	konami.b &= t; //TODO should be unsigned?
        CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /*TODO*////* $d5 BITB direct -**0- */
    /*TODO*///opcode bitb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    
    /* $d6 LDB direct -**0- */
    opcode ldb_di= new opcode() { public void handler()
    {
        konami.b=DIRBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /* $d7 STB direct -**0- */
    opcode stb_di= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.b);
    	DIRECT();
    	WM(ea,konami.b);
    }};
    
    /* $d8 EORB direct -**0- */
    opcode eorb_di= new opcode() { public void handler()
    {
        int t=DIRBYTE();
    	konami.b ^= t;
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /*TODO*////* $d9 ADCB direct ***** */
    /*TODO*///opcode adcb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /* $dA ORB direct -**0- */
    opcode orb_di= new opcode() { public void handler()
    {
        int t=	DIRBYTE();
    	konami.b |= t;  //todo check if it should be unsigned
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /*TODO*////* $dB ADDB direct ***** */
    /*TODO*///opcode addb_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	DIRBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $dC LDD direct -**0- */
    opcode ldd_di= new opcode() { public void handler()
    {
        int temp=DIRWORD();
        setDreg(temp);
        CLR_NZV();
        SET_NZ16(temp);
    }};
    
    /* $dD STD direct -**0- */
    opcode std_di= new opcode() { public void handler()
    {
        CLR_NZV();
        int temp = getDreg();
    	SET_NZ16(temp);
        DIRECT();
    	WM16(ea,temp);
        
    }};
    
    /* $dE LDU (LDS) direct -**0- */
    opcode ldu_di= new opcode() { public void handler()
    {
        konami.u=DIRWORD();
    	CLR_NZV();
    	SET_NZ16(konami.u);
    }};
    
    /* $10dE LDS direct -**0- */
    opcode lds_di= new opcode() { public void handler()
    {
        konami.s=DIRWORD();
        CLR_NZV();
    	SET_NZ16(konami.s);
    	konami.int_state |= KONAMI_LDS;
    }};
    
    /* $dF STU (STS) direct -**0- */
    opcode stu_di= new opcode() { public void handler()
    {
        CLR_NZV();
   	SET_NZ16(konami.u);
    	DIRECT();
    	WM16(ea,konami.u);
    }};
    
    /*TODO*////* $10dF STS direct -**0- */
    /*TODO*///opcode sts_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	DIRECT;
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    
 
    /* $e0 SUBB indexed ?**** */
    opcode subb_ix= new opcode() { public void handler()
    {
        int	  t,r;
    	t = RM(ea);
    	r = konami.b - t & 0xFFFF;
    	CLR_NZVC();
   	SET_FLAGS8(konami.b,t,r);
    	konami.b = r & 0xFF;      
    }};
    /*TODO*///
    /*TODO*////* $e1 CMPB indexed ?**** */
    /*TODO*///opcode cmpb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e2 SBCB indexed ?**** */
    /*TODO*///opcode sbcb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $e3 ADDD indexed -**** */
    opcode addd_ix= new opcode() { public void handler()
    {
        int r,d;
        int b;
    	b=RM16(ea);
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    
    /*TODO*////* $e4 ANDB indexed -**0- */
    /*TODO*///opcode andb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B &= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    
    /* $e5 BITB indexed -**0- */
    opcode bitb_ix= new opcode() { public void handler()
    {
        int r;
        r = konami.b & RM(ea);
    	CLR_NZV();
    	SET_NZ8(r);
    }};
    
    /* $e6 LDB indexed -**0- */
    opcode ldb_ix= new opcode() { public void handler()
    {
        konami.b = RM(ea);
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /* $e7 STB indexed -**0- */
    opcode stb_ix= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.b);
    	WM(ea,konami.b);
    }};
    
    /*TODO*////* $e8 EORB indexed -**0- */
    /*TODO*///opcode eorb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	B ^= RM(EAD);
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $e9 ADCB indexed ***** */
    /*TODO*///opcode adcb_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	t = RM(EAD);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $eA ORB indexed -**0- */
    opcode orb_ix= new opcode() { public void handler()
    {
        konami.b |= RM(ea);
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /* $eb ADDB indexed ***** */
    opcode addb_ix= new opcode() { public void handler()
    {
        int t,r;
    	t = RM(ea);
    	r = konami.b + t & 0xFFFF;
    	CLR_HNZVC();
    	SET_FLAGS8(konami.b,t,r);
    	SET_H(konami.b,t,r);
    	konami.b = r & 0xFF;
    }};
    
    /* $ec LDD indexed -**0- */
    opcode ldd_ix= new opcode() { public void handler()
    {
        int temp=RM16(ea);
        setDreg(temp);
    	CLR_NZV(); 
        SET_NZ16(temp);
    }};
   
   /* $eD STD indexed -**0- */
   opcode std_ix= new opcode() { public void handler()
   {
        CLR_NZV();
        int temp=getDreg();
    	SET_NZ16(temp);
    	WM16(ea,temp);
   }};
    
    /* $eE LDU (LDS) indexed -**0- */
    opcode ldu_ix= new opcode() { public void handler()
    {
        konami.u=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(konami.u);
    }};
    
    /* $10eE LDS indexed -**0- */
    opcode lds_ix= new opcode() { public void handler()
    {
        konami.s=RM16(ea);
    	CLR_NZV();
    	SET_NZ16(konami.s);
        konami.int_state |= KONAMI_LDS;
    }};
    
    /* $eF STU (STS) indexed -**0- */
    opcode stu_ix= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.u);
    	WM16(ea,konami.u);
    }};
    
    /*TODO*////* $10eF STS indexed -**0- */
    /*TODO*///opcode sts_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(S);
    /*TODO*///	WM16(EAD,&pS);
    /*TODO*///}};
    /*TODO*///
    /*TODO*///#if macintosh
    /*TODO*///#pragma mark ____Fx____
    /*TODO*///#endif
    
    /* $f0 SUBB extended ?**** */
    opcode subb_ex= new opcode() { public void handler()
    {
        int  t,r;
        t=EXTBYTE();
    	r = konami.b - t & 0xFFFF;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,t,r);
    	konami.b = r & 0xFF;
    }};
    
    /* $f1 CMPB extended ?**** */
    opcode cmpb_ex= new opcode() { public void handler()
    {
        int t,r;
    	t=EXTBYTE();
    	r = konami.b - t;
    	CLR_NZVC();
    	SET_FLAGS8(konami.b,t,r);
    }};
    
    /*TODO*////* $f2 SBCB extended ?**** */
    /*TODO*///opcode sbcb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16	  t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B - t - (CC & CC_C);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $f3 ADDD extended -**** */
    opcode addd_ex= new opcode() { public void handler()
    {
        int r,d;
        int b=EXTWORD();
    	d = getDreg();
    	r = d + b;
    	CLR_NZVC();
    	SET_FLAGS16(d,b,r);
    	setDreg(r);
    }};
    /*TODO*///
    /*TODO*////* $f4 ANDB extended -**0- */
    /*TODO*///opcode andb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B &= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f5 BITB extended -**0- */
    /*TODO*///opcode bitb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B & t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(r);
    /*TODO*///}};
    
    /* $f6 LDB extended -**0- */
    opcode ldb_ex= new opcode() { public void handler()
    {
        konami.b=EXTBYTE();
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    
    /* $f7 STB extended -**0- */
    opcode stb_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ8(konami.b);
    	EXTENDED();
    	WM(ea,konami.b);
    }};
    
    /*TODO*////* $f8 EORB extended -**0- */
    /*TODO*///opcode eorb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	B ^= t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ8(B);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* $f9 ADCB extended ***** */
    /*TODO*///opcode adcb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t + (CC & CC_C);
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $fA ORB extended -**0- */
    opcode orb_ex= new opcode() { public void handler()
    {
        int t=EXTBYTE();
    	konami.b |= t;
    	CLR_NZV();
    	SET_NZ8(konami.b);
    }};
    /*TODO*///
    /*TODO*////* $fB ADDB extended ***** */
    /*TODO*///opcode addb_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t,r;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///	r = B + t;
    /*TODO*///	CLR_HNZVC;
    /*TODO*///	SET_FLAGS8(B,t,r);
    /*TODO*///	SET_H(B,t,r);
    /*TODO*///	B = r;
    /*TODO*///}};
    
    /* $fC LDD extended -**0- */
    opcode ldd_ex= new opcode() { public void handler()
    {
        int temp=EXTWORD();
        setDreg(temp);
    	CLR_NZV();
    	SET_NZ16(temp);
    }};
    
    /* $fD STD extended -**0- */
    opcode std_ex= new opcode() { public void handler()
    {
        CLR_NZV();
        int temp = getDreg();
    	SET_NZ16(temp);
        EXTENDED();
    	WM16(ea,temp);
    }};
    
    /* $fE LDU (LDS) extended -**0- */
    opcode ldu_ex= new opcode() { public void handler()
    {
        konami.u=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(konami.u);
    }};
    
    /* $10fE LDS extended -**0- */
    opcode lds_ex= new opcode() { public void handler()
    {
        konami.s=EXTWORD();
    	CLR_NZV();
    	SET_NZ16(konami.s);
    	konami.int_state |= KONAMI_LDS;
    }};
    
    /* $fF STU (STS) extended -**0- */
    opcode stu_ex= new opcode() { public void handler()
    {
        CLR_NZV();
   	SET_NZ16(konami.u);
    	EXTENDED();
    	WM16(ea,konami.u);
    }};
    
    /* $10fF STS extended -**0- */
    opcode sts_ex= new opcode() { public void handler()
    {
        CLR_NZV();
    	SET_NZ16(konami.s);
    	EXTENDED();
    	WM16(ea,konami.s);
    }};
    
    opcode setline_im= new opcode() { public void handler()//konami specific
    {
    	int t=IMMBYTE();
    
    	if ( konami_cpu_setlines_callback!=null )
    		konami_cpu_setlines_callback.handler(t);
    }};
    /*TODO*///
    /*TODO*///opcode setline_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	t = RM(EA);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    /*TODO*///
    opcode setline_di= new opcode() { public void handler()
    {
    	/*UINT8 t;*/
    	int t=DIRBYTE() & 0xFF;//just for sure
    
    	if ( konami_cpu_setlines_callback!=null )
    		konami_cpu_setlines_callback.handler(t);
    }};
    /*TODO*///
    /*TODO*///opcode setline_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	if ( konami_cpu_setlines_callback )
    /*TODO*///		(*konami_cpu_setlines_callback)( t );
    /*TODO*///}};
    
    opcode bmove= new opcode() { public void handler()//konami specific
    {
    	/*UINT8*/int	t;
    
    	while( konami.u != 0 ) {
    		t = RM(konami.y)&0xFF;
    		WM(konami.x,t);
    		konami.y = konami.y +1 & 0xFFFF;//Y++;
    		konami.x= konami.x+1 & 0xFFFF;//X++;
    		konami.u = konami.u-1 & 0xFFFF;//U--;
    		konami_ICount[0] -= 2;
    	}
    }};
    /*TODO*///
    /*TODO*///opcode move= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	t = RM(Y);
    /*TODO*///	WM(X,t);
    /*TODO*///	Y++;
    /*TODO*///	X++;
    /*TODO*///	U--;
    /*TODO*///}};
    
    /* CLRD inherent -0100 */
    opcode clrd= new opcode() { public void handler()//konami specific
    {
    	setDreg(0);
    	CLR_NZVC(); 
        SEZ();
    }};
    
    /* CLRW indexed -0100 */
    opcode clrw_ix= new opcode() { public void handler()//konami specific
    {
    	//PAIR t;
    	//t.d = 0;
        int t=0;
    	WM16(ea,t);
    	CLR_NZVC(); 
        SEZ();
    }};
 
    /* CLRW direct -0100 */
    opcode clrw_di= new opcode() { public void handler()//konami specific
    {
    	//PAIR t;
    	//t.d = 0;
        int t=0;
    	DIRECT();
    	WM16(ea,t);
    	CLR_NZVC();
    	SEZ();
    }};
    /*TODO*///
    /*TODO*////* CLRW extended -0100 */
    /*TODO*///opcode clrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d = 0;
    /*TODO*///	EXTENDED;
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///	CLR_NZVC; SEZ;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD immediate -0*-* */
    /*TODO*///opcode lsrd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	IMMBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD immediate -**-* */
    /*TODO*///opcode rord= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD immediate ?**-* */
    /*TODO*///opcode asrd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD immediate ?**** */
    /*TODO*///opcode asld= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	IMMBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD immediate -**-* */
    /*TODO*///opcode rold= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	IMMBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    
    /* DECB,JNZ relative ----- */
    opcode decbjnz= new opcode() { public void handler()//konami specific
    {
    	konami.b = konami.b -1 & 0xFF;//--B;
    	CLR_NZV();
    	SET_FLAGS8D(konami.b);
    	BRANCH( (konami.cc&CC_Z)==0 );
    }};
    /*TODO*///
    /*TODO*////* DECX,JNZ relative ----- */
    /*TODO*///opcode decxjnz= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	--X;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(X);	/* should affect V as well? */
    /*TODO*///	BRANCH( !(CC&CC_Z) );
    /*TODO*///}};
    
    opcode bset= new opcode() { public void handler()
    {
    	/*UINT8*/int t;
    
    	while( konami.u != 0 ) {
    		t = konami.a & 0xFF;
    		WM(konami.x,t);
    		konami.x = konami.x + 1 & 0xFFFF;//X++;
    		konami.u=konami.u-1 & 0xFFFF;//U--;
    		konami_ICount[0] -= 2;
        }
    }};
    
    opcode bset2= new opcode() { public void handler()//konami specific
    {
    	while( konami.u != 0 ) {
    		WM16(konami.x,getDreg());
    		konami.x = konami.x + 2 & 0xFFFF;//X += 2;
    		konami.u=konami.u-1 & 0xFFFF;//U--;
    		konami_ICount[0] -= 3;
    	}
    }};
    /*TODO*///
    /*TODO*////* LMUL inherent --*-@ */
    /*TODO*///opcode lmul= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 t;
    /*TODO*///	t = X * Y;
    /*TODO*///	X = (t >> 16);
    /*TODO*///	Y = (t & 0xffff);
    /*TODO*///	CLR_ZC; SET_Z(t); if( t & 0x8000 ) SEC;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DIVX inherent --*-@ */
    /*TODO*///opcode divx= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 t;
    /*TODO*///	UINT8 r;
    /*TODO*///	if ( B != 0 )
    /*TODO*///	{
    /*TODO*///		t = X / B;
    /*TODO*///		r = X % B;
    /*TODO*///	}};
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		/* ?? */
    /*TODO*///		t = 0;
    /*TODO*///		r = 0;
    /*TODO*///	}};
    /*TODO*///	CLR_ZC; SET_Z16(t); if ( t & 0x80 ) SEC;
    /*TODO*///	X = t;
    /*TODO*///	B = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCD inherent -***- */
    /*TODO*///opcode incd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D + 1;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(D,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW direct -***- */
    /*TODO*///opcode incw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW indexed -***- */
    /*TODO*///opcode incw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* INCW extended -***- */
    /*TODO*///opcode incw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	++r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECD inherent -***- */
    /*TODO*///opcode decd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = D - 1;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(D,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW direct -***- */
    /*TODO*///opcode decw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW indexed -***- */
    /*TODO*///opcode decw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d, t.d, r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* DECW extended -***- */
    /*TODO*///opcode decw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t, r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r = t;
    /*TODO*///	--r.d;
    /*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTD inherent -**0- */
    /*TODO*///opcode tstd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	CLR_NZV;
    /*TODO*///	SET_NZ16(D);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW direct -**0- */
    /*TODO*///opcode tstw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW indexed -**0- */
    /*TODO*///opcode tstw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* TSTW extended -**0- */
    /*TODO*///opcode tstw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	CLR_NZV;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW direct -0*-* */
    /*TODO*///opcode lsrw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW indexed -0*-* */
    /*TODO*///opcode lsrw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRW extended -0*-* */
    /*TODO*///opcode lsrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d >>= 1;
    /*TODO*///	SET_Z16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW direct -**-* */
    /*TODO*///opcode rorw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW indexed -**-* */
    /*TODO*///opcode rorw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORW extended -**-* */
    /*TODO*///opcode rorw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = (CC & CC_C) << 15;
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	r.d |= t.d>>1;
    /*TODO*///	SET_NZ16(r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW direct ?**-* */
    /*TODO*///opcode asrw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW indexed ?**-* */
    /*TODO*///opcode asrw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRW extended ?**-* */
    /*TODO*///opcode asrw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	CLR_NZC;
    /*TODO*///	CC |= (t.d & CC_C);
    /*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
    /*TODO*///	SET_NZ16(t.d);
    /*TODO*///	WM16(EAD,&t);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW direct ?**** */
    /*TODO*///opcode aslw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW indexed ?**** */
    /*TODO*///opcode aslw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLW extended ?**** */
    /*TODO*///opcode aslw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = t.d << 1;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW direct -**** */
    /*TODO*///opcode rolw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW indexed -**** */
    /*TODO*///opcode rolw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLW extended -**** */
    /*TODO*///opcode rolw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR t,r;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGD inherent ?**** */
    /*TODO*///opcode negd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	r = -D;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW direct ?**** */
    /*TODO*///opcode negw_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	DIRWORD(t);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW indexed ?**** */
    /*TODO*///opcode negw_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	t.d=RM16(EAD);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* NEGW extended ?**** */
    /*TODO*///opcode negw_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	PAIR r,t;
    /*TODO*///	EXTWORD(t);
    /*TODO*///	r.d = -t.d;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,t.d,r.d);
    /*TODO*///	WM16(EAD,&r);
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ABSA inherent ?**** */
    /*TODO*///opcode absa= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	if (A & 0x80)
    /*TODO*///		r = -A;
    /*TODO*///	else
    /*TODO*///		r = A;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS8(0,A,r);
    /*TODO*///	A = r;
    /*TODO*///}};
    /*TODO*///
    /* ABSB inherent ?**** */
    opcode absb= new opcode() { public void handler()//konami specific (to be rechecked)
    {
    	/*UINT16*/ int r;
    	if ((konami.b & 0x80)!=0)
    		r = -konami.b;
    	else
    		r = konami.b;
    	CLR_NZVC();
    	SET_FLAGS8(0,konami.b,r);
    	konami.b = r & 0xFF;
    }};
    /*TODO*///
    /*TODO*////* ABSD inherent ?**** */
    /*TODO*///opcode absd= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32 r;
    /*TODO*///	if (D & 0x8000)
    /*TODO*///		r = -D;
    /*TODO*///	else
    /*TODO*///		r = D;
    /*TODO*///	CLR_NZVC;
    /*TODO*///	SET_FLAGS16(0,D,r);
    /*TODO*///	D = r;
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD direct -0*-* */
    /*TODO*///opcode lsrd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	DIRBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD direct -**-* */
    /*TODO*///opcode rord_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD direct ?**-* */
    /*TODO*///opcode asrd_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD direct ?**** */
    /*TODO*///opcode asld_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	DIRBYTE( t );
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD direct -**-* */
    /*TODO*///opcode rold_di= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	DIRBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD indexed -0*-* */
    /*TODO*///opcode lsrd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD indexed -**-* */
    /*TODO*///opcode rord_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD indexed ?**-* */
    /*TODO*///opcode asrd_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD indexed ?**** */
    /*TODO*///opcode asld_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD indexed -**-* */
    /*TODO*///opcode rold_ix= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	t=RM(EA);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* LSRD extended -0*-* */
    /*TODO*///opcode lsrd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D >>= 1;
    /*TODO*///		SET_Z16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* RORD extended -**-* */
    /*TODO*///opcode rord_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = (CC & CC_C) << 15;
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		r |= D >> 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASRD extended ?**-* */
    /*TODO*///opcode asrd_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT8 t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		CC |= (D & CC_C);
    /*TODO*///		D = (D & 0x8000) | (D >> 1);
    /*TODO*///		SET_NZ16(D);
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ASLD extended ?**** */
    /*TODO*///opcode asld_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT32	r;
    /*TODO*///	UINT8	t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		r = D << 1;
    /*TODO*///		CLR_NZVC;
    /*TODO*///		SET_FLAGS16(D,D,r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    /*TODO*///
    /*TODO*////* ROLD extended -**-* */
    /*TODO*///opcode rold_ex= new opcode() { public void handler()
    /*TODO*///{
    /*TODO*///	UINT16 r;
    /*TODO*///	UINT8  t;
    /*TODO*///
    /*TODO*///	EXTBYTE(t);
    /*TODO*///
    /*TODO*///	while ( t-- ) {
    /*TODO*///		CLR_NZC;
    /*TODO*///		if ( D & 0x8000 ) SEC;
    /*TODO*///		r = CC & CC_C;
    /*TODO*///		r |= D << 1;
    /*TODO*///		SET_NZ16(r);
    /*TODO*///		D = r;
    /*TODO*///	}};
    /*TODO*///}};
    
    opcode opcode2= new opcode() { public void handler()
    {
        int ireg2 = ROP_ARG(konami.pc) &0xFF;
        konami.pc = konami.pc + 1 & 0xFFFF;
        
        
    	switch ( ireg2 ) {
    /*TODO*/////	case 0x00: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x01: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x02: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x03: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x04: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x05: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x06: EA=0; break; /* normal */
    	case 0x07:
        {
            ea=0;
            if(konami_extended[konami.ireg]!=null)
            {
                konami_extended[konami.ireg].handler();//(*konami_extended[konami.ireg])();
            }
            else
            {
                System.out.println("Unsupported konami_extended instruction 0x" + Integer.toHexString(konami.ireg));
            }

            konami_ICount[0] -= 2;
            return;
        }
    /*TODO*/////	case 0x08: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x09: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x0a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x0b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x0c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x0d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x0e: EA=0; break; /* indirect - normal */
    	case 0x0f:				/* indirect - extended */
    		ea=IMMWORD();
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*/////	case 0x10: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x11: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x12: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x13: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x14: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x15: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x16: EA=0; break; /* normal */
    /*TODO*/////	case 0x17: EA=0; break; /* extended */
    /*TODO*/////	case 0x18: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x19: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x1a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x1b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x1c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x1d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x1e: EA=0; break; /* indirect - normal */
    /*TODO*/////	case 0x1f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /* base X */
        case 0x20:              /* auto increment */
    		ea=konami.x;
    		konami.x=konami.x + 1 & 0xFFFF;//X++;
                konami_ICount[0]-=2;
    		break;
    	case 0x21:				/* double auto increment */
    		ea=konami.x;
    		konami.x=konami.x + 2 & 0xFFFF;
                konami_ICount[0]-=3;
                break;
    /*TODO*///	case 0x22:				/* auto decrement */
    /*TODO*///		X--;
    /*TODO*///		EA=X;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///        break;
    /*TODO*///	case 0x23:				/* double auto decrement */
    /*TODO*///		X-=2;
    /*TODO*///		EA=X;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    	case 0x24:				/* postbyte offs */
    		ea=IMMBYTE();
    		ea = konami.x + (byte)ea & 0xFFFF;//EA=X+SIGNED(EA);
                konami_ICount[0]-=2;
    		break;
    	case 0x25:				/* postword offs */
    		ea=IMMWORD();
    		ea= ea + konami.x & 0xFFFF;//EA+=X;
                konami_ICount[0]-=4;
    		break;
   	case 0x26:				/* normal */
   		ea = konami.x;
   		break;
    /*TODO*/////	case 0x27: EA=0; break; /* extended */
    /*TODO*///	case 0x28:				/* indirect - auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x29:				/* indirect - double auto increment */
    /*TODO*///		EA=X;
    /*TODO*///		X+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x2a:				/* indirect - auto decrement */
    /*TODO*///		X--;
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x2b:				/* indirect - double auto decrement */
    /*TODO*///		X-=2;
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x2c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=X+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x2d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x2e:				/* indirect - normal */
    /*TODO*///		EA=X;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x2f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /* base Y */
        case 0x30:              /* auto increment */
                ea=konami.y;
    		konami.y=konami.y+1 & 0xFFFF;//Y++;
                konami_ICount[0]-=2;
    		break;
   	case 0x31:				/* double auto increment */
   		ea=konami.y;
    		konami.y=konami.y+2 & 0xFFFF;
                konami_ICount[0]-=3;
   		break;
    /*TODO*///	case 0x32:				/* auto decrement */
    /*TODO*///		Y--;
    /*TODO*///		EA=Y;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x33:				/* double auto decrement */
    /*TODO*///		Y-=2;
    /*TODO*///		EA=Y;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    	case 0x34:				/* postbyte offs */
    		ea=IMMBYTE();
    		ea = konami.y + (byte)ea & 0xFFFF;//EA=Y+SIGNED(EA);
                konami_ICount[0]-=2;
    		break;
    /*TODO*///	case 0x35:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=Y;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    	case 0x36:				/* normal */
    		ea=konami.y;
    		break;
    /*TODO*/////	case 0x37: EA=0; break; /* extended */
    /*TODO*///	case 0x38:				/* indirect - auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x39:				/* indirect - double auto increment */
    /*TODO*///		EA=Y;
    /*TODO*///		Y+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x3a:				/* indirect - auto decrement */
    /*TODO*///		Y--;
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x3b:				/* indirect - double auto decrement */
    /*TODO*///		Y-=2;
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x3c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=Y+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x3d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x3e:				/* indirect - normal */
    /*TODO*///		EA=Y;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x3f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*/////  case 0x40: EA=0; break; /* auto increment */
    /*TODO*/////	case 0x41: EA=0; break; /* double auto increment */
    /*TODO*/////	case 0x42: EA=0; break; /* auto decrement */
    /*TODO*/////	case 0x43: EA=0; break; /* double auto decrement */
    /*TODO*/////	case 0x44: EA=0; break; /* postbyte offs */
    /*TODO*/////	case 0x45: EA=0; break; /* postword offs */
    /*TODO*/////	case 0x46: EA=0; break; /* normal */
    /*TODO*/////	case 0x47: EA=0; break; /* extended */
    /*TODO*/////	case 0x48: EA=0; break; /* indirect - auto increment */
    /*TODO*/////	case 0x49: EA=0; break; /* indirect - double auto increment */
    /*TODO*/////	case 0x4a: EA=0; break; /* indirect - auto decrement */
    /*TODO*/////	case 0x4b: EA=0; break; /* indirect - double auto decrement */
    /*TODO*/////	case 0x4c: EA=0; break; /* indirect - postbyte offs */
    /*TODO*/////	case 0x4d: EA=0; break; /* indirect - postword offs */
    /*TODO*/////	case 0x4e: EA=0; break; /* indirect - normal */
    /*TODO*/////	case 0x4f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /* base U */
        case 0x50:              /* auto increment */
    		ea=konami.u;
    		konami.u=konami.u+1 & 0xFFFF;//U++;
                konami_ICount[0]-=2;
    		break;
    	case 0x51:				/* double auto increment */
    		ea=konami.u;
    		konami.u=konami.u+2 & 0xFFFF;
                konami_ICount[0]-=3;
    		break;
    /*TODO*///	case 0x52:				/* auto decrement */
    /*TODO*///		U--;
    /*TODO*///		EA=U;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x53:				/* double auto decrement */
    /*TODO*///		U-=2;
    /*TODO*///		EA=U;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    	case 0x54:				/* postbyte offs */
                ea=IMMBYTE();
    		ea = konami.u + (byte)ea & 0xFFFF;//EA=U+SIGNED(EA);
                konami_ICount[0]-=2;
    		break;
    	case 0x55:				/* postword offs */
    		ea=IMMWORD();
    		ea= ea + konami.u & 0xFFFF;//EA+=U;
                konami_ICount[0]-=4;
    		break;
    	case 0x56:				/* normal */
    		ea=konami.u;
    		break;
    /*TODO*/////	case 0x57: EA=0; break; /* extended */
    /*TODO*///	case 0x58:				/* indirect - auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x59:				/* indirect - double auto increment */
    /*TODO*///		EA=U;
    /*TODO*///		U+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x5a:				/* indirect - auto decrement */
    /*TODO*///		U--;
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x5b:				/* indirect - double auto decrement */
    /*TODO*///		U-=2;
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    	case 0x5c:				/* indirect - postbyte offs */
    		ea=IMMBYTE();
    		ea = konami.u + (byte)ea & 0xFFFF;//EA=U+SIGNED(EA);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*///	case 0x5d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x5e:				/* indirect - normal */
    /*TODO*///		EA=U;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x5f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /* base S */
        case 0x60:              /* auto increment */
    		ea = konami.s;
    		konami.s = konami.s + 1 & 0xFFFF;
                konami_ICount[0]-=2;
    		break;
    	case 0x61:				/* double auto increment */
    		ea = konami.s;
    		konami.s = konami.s + 2 & 0xFFFF;
                konami_ICount[0]-=3;
    		break;
    	case 0x62:				/* auto decrement */
    		konami.s = konami.s - 1 & 0xFFFF;
    		ea=konami.s;
                konami_ICount[0]-=2;
    		break;
    	case 0x63:				/* double auto decrement */
    		konami.s = konami.s - 2 & 0xFFFF;
    		ea=konami.s;
                konami_ICount[0]-=3;
    		break;
    	case 0x64:				/* postbyte offs */
    		ea=IMMBYTE();
    		ea = konami.s + (byte)ea & 0xFFFF;//EA=S+SIGNED(EA);
                konami_ICount[0]-=2;
    		break;
    /*TODO*///	case 0x65:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=S;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    	case 0x66:				/* normal */
    		ea=konami.s;
    		break;
    /*TODO*/////	case 0x67: EA=0; break; /* extended */
    /*TODO*///	case 0x68:				/* indirect - auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x69:				/* indirect - double auto increment */
    /*TODO*///		EAD=SD;
    /*TODO*///		S+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x6a:				/* indirect - auto decrement */
    /*TODO*///		S--;
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x6b:				/* indirect - double auto decrement */
    /*TODO*///		S-=2;
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x6c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=S+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x6d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=S;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x6e:				/* indirect - normal */
    /*TODO*///		EAD=SD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x6f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*////* base PC */
    /*TODO*///    case 0x70:              /* auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC++;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x71:				/* double auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC+=2;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x72:				/* auto decrement */
    /*TODO*///		PC--;
    /*TODO*///		EAD=PCD;
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x73:				/* double auto decrement */
    /*TODO*///		PC-=2;
    /*TODO*///		EAD=PCD;
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*///	case 0x74:				/* postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=PC-1+SIGNED(EA);
    /*TODO*///        konami_ICount-=2;
    /*TODO*///		break;
    /*TODO*///	case 0x75:				/* postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=PC-2;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x76:				/* normal */
    /*TODO*///		EAD=PCD;
    /*TODO*///		break;
    /*TODO*/////	case 0x77: EA=0; break; /* extended */
    /*TODO*///	case 0x78:				/* indirect - auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC++;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x79:				/* indirect - double auto increment */
    /*TODO*///		EAD=PCD;
    /*TODO*///		PC+=2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x7a:				/* indirect - auto decrement */
    /*TODO*///		PC--;
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=5;
    /*TODO*///		break;
    /*TODO*///	case 0x7b:				/* indirect - double auto decrement */
    /*TODO*///		PC-=2;
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=6;
    /*TODO*///		break;
    /*TODO*///	case 0x7c:				/* indirect - postbyte offs */
    /*TODO*///		IMMBYTE(EA);
    /*TODO*///		EA=PC-1+SIGNED(EA);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0x7d:				/* indirect - postword offs */
    /*TODO*///		IMMWORD(ea);
    /*TODO*///		EA+=PC-2;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0x7e:				/* indirect - normal */
    /*TODO*///		EAD=PCD;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=3;
    /*TODO*///		break;
    /*TODO*/////	case 0x7f: EA=0; break; /* indirect - extended */
    /*TODO*///
    /*TODO*/////  case 0x80: EA=0; break; /* register a */
    /*TODO*/////	case 0x81: EA=0; break; /* register b */
    /*TODO*/////	case 0x82: EA=0; break; /* ???? */
    /*TODO*/////	case 0x83: EA=0; break; /* ???? */
    /*TODO*/////	case 0x84: EA=0; break; /* ???? */
    /*TODO*/////	case 0x85: EA=0; break; /* ???? */
    /*TODO*/////	case 0x86: EA=0; break; /* ???? */
    /*TODO*/////	case 0x87: EA=0; break; /* register d */
    /*TODO*/////	case 0x88: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0x89: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0x8a: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8b: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8c: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8d: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x8e: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0x8f: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x90: EA=0; break; /* register a */
    /*TODO*/////	case 0x91: EA=0; break; /* register b */
    /*TODO*/////	case 0x92: EA=0; break; /* ???? */
    /*TODO*/////	case 0x93: EA=0; break; /* ???? */
    /*TODO*/////	case 0x94: EA=0; break; /* ???? */
    /*TODO*/////	case 0x95: EA=0; break; /* ???? */
    /*TODO*/////	case 0x96: EA=0; break; /* ???? */
    /*TODO*/////	case 0x97: EA=0; break; /* register d */
    /*TODO*/////	case 0x98: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0x99: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0x9a: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9b: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9c: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9d: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0x9e: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0x9f: EA=0; break; /* indirect - ???? */
    	case 0xa0:				/* register a */
    		ea = konami.x + (byte)konami.a & 0xFFFF;//EA=X+SIGNED(A);
                konami_ICount[0]-=1;
    		break;
    	case 0xa1:				/* register b */
    		ea = konami.x + (byte)konami.b & 0xFFFF;//EA=X+SIGNED(B);
                konami_ICount[0]-=1;
    		break;
    /*TODO*/////	case 0xa2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xa6: EA=0; break; /* ???? */
    	case 0xa7:				/* register d */
    		ea=konami.x+ getDreg() & 0xFFFF;//EA=X+D;
                konami_ICount[0]-=4;
    		break;
    	case 0xa8:				/* indirect - register a */
    		ea = konami.x + (byte)konami.a & 0xFFFF;//EA=X+SIGNED(A);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*///	case 0xa9:				/* indirect - register b */
    /*TODO*///		EA=X+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xaa: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xab: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xac: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xad: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xae: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xaf:				/* indirect - register d */
    /*TODO*///		EA=X+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    	case 0xb0:				/* register a */
    		ea = konami.y + (byte)konami.a & 0xFFFF;//EA=Y+SIGNED(A);
                konami_ICount[0]-=1;
    		break;
    /*TODO*///	case 0xb1:				/* register b */
    /*TODO*///		EA=Y+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xb2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xb6: EA=0; break; /* ???? */
    	case 0xb7:				/* register d */
    		ea=konami.y+getDreg() & 0xFFFF;//EA=Y+D;
                konami_ICount[0]-=4;
    		break;
    	case 0xb8:				/* indirect - register a */
    		ea = konami.y + (byte)konami.a & 0xFFFF;//EA=Y+SIGNED(A);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*///	case 0xb9:				/* indirect - register b */
    /*TODO*///		EA=Y+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xba: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xbe: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xbf:				/* indirect - register d */
    /*TODO*///		EA=Y+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*/////	case 0xc0: EA=0; break; /* register a */
    /*TODO*/////	case 0xc1: EA=0; break; /* register b */
    /*TODO*/////	case 0xc2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc3: EA=0; break; /* ???? */
    	case 0xc4:
        {
    		ea=0;
                if(konami_direct[konami.ireg]!=null)
                {
                    konami_direct[konami.ireg].handler();//(*konami_direct[konami.ireg])();
                }
                else
                {
                    System.out.println("Unsupported konami_direct instruction 0x" + Integer.toHexString(konami.ireg));
                }  		
                konami_ICount[0] -= 1;
    		return;
        }
    /*TODO*/////	case 0xc5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc6: EA=0; break; /* ???? */
    /*TODO*/////	case 0xc7: EA=0; break; /* register d */
    /*TODO*/////	case 0xc8: EA=0; break; /* indirect - register a */
    /*TODO*/////	case 0xc9: EA=0; break; /* indirect - register b */
    /*TODO*/////	case 0xca: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xcb: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xcc:				/* indirect - direct */
    /*TODO*///		DIRWORD(ea);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xcd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xce: EA=0; break; /* indirect - register d */
    /*TODO*/////	case 0xcf: EA=0; break; /* indirect - ???? */
    	case 0xd0:				/* register a */
    		ea = konami.u + (byte)konami.a & 0xFFFF;//EA=U+SIGNED(A);
                konami_ICount[0]-=1;
    		break;
    	case 0xd1:				/* register b */
    		ea = konami.u + (byte)konami.b & 0xFFFF;//EA=U+SIGNED(B);
                konami_ICount[0]-=1;
    		break;
    /*TODO*/////	case 0xd2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xd6: EA=0; break; /* ???? */
    	case 0xd7:				/* register d */
    		ea=konami.u + getDreg() & 0xFFFF;//EA=U+D;
                konami_ICount[0]-=4;
    		break;
    	case 0xd8:				/* indirect - register a */
    		ea = konami.u + (byte)konami.a & 0xFFFF;//EA=U+SIGNED(A);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    	case 0xd9:				/* indirect - register b */
    		ea = konami.u + (byte)konami.b & 0xFFFF;;//EA=U+SIGNED(B);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*/////	case 0xda: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xdd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xde: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xdf:				/* indirect - register d */
    /*TODO*///		EA=U+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///        break;
    /*TODO*///	case 0xe0:				/* register a */
    /*TODO*///		EA=S+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xe1:				/* register b */
    /*TODO*///		EA=S+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xe2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xe6: EA=0; break; /* ???? */
    /*TODO*///	case 0xe7:				/* register d */
    /*TODO*///		EA=S+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    	case 0xe8:				/* indirect - register a */
    		ea = konami.s + (byte)konami.a & 0xFFFF;//EA=S+SIGNED(A);
    		ea=RM16(ea);
                konami_ICount[0]-=4;
    		break;
    /*TODO*///	case 0xe9:				/* indirect - register b */
    /*TODO*///		EA=S+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xea: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xeb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xec: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xed: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xee: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xef:				/* indirect - register d */
    /*TODO*///		EA=S+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
    /*TODO*///	case 0xf0:				/* register a */
    /*TODO*///		EA=PC+SIGNED(A);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*///	case 0xf1:				/* register b */
    /*TODO*///		EA=PC+SIGNED(B);
    /*TODO*///        konami_ICount-=1;
    /*TODO*///		break;
    /*TODO*/////	case 0xf2: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf3: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf4: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf5: EA=0; break; /* ???? */
    /*TODO*/////	case 0xf6: EA=0; break; /* ???? */
    /*TODO*///	case 0xf7:				/* register d */
    /*TODO*///		EA=PC+D;
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xf8:				/* indirect - register a */
    /*TODO*///		EA=PC+SIGNED(A);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*///	case 0xf9:				/* indirect - register b */
    /*TODO*///		EA=PC+SIGNED(B);
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=4;
    /*TODO*///		break;
    /*TODO*/////	case 0xfa: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfb: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfc: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfd: EA=0; break; /* indirect - ???? */
    /*TODO*/////	case 0xfe: EA=0; break; /* indirect - ???? */
    /*TODO*///	case 0xff:				/* indirect - register d */
    /*TODO*///		EA=PC+D;
    /*TODO*///		EA=RM16(EAD);
    /*TODO*///        konami_ICount-=7;
    /*TODO*///		break;
	default:
            System.out.println("opcode2 ireg2 = 0x"+Integer.toHexString(ireg2)+ " ireg=0x"+Integer.toHexString(konami.ireg));
    /*TODO*///		if ( errorlog )
    /*TODO*///			fprintf( errorlog, "KONAMI: Unknown/Invalid postbyte at PC = %04x\n", PC -1 );
    /*TODO*///        EAD = 0;
    	}
        if(konami_indexed[konami.ireg]!=null)
        {
            konami_indexed[konami.ireg].handler();//(*konami_indexed[konami.ireg])();
        }
        else
        {
            System.out.println("Unsupported konami_indexed instruction 0x" + Integer.toHexString(konami.ireg));
        }	
    }};



    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public abstract interface opcode
    {
        public abstract void handler();
    }
}
