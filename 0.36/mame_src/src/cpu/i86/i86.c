/****************************************************************************
*			  real mode i286 emulator v1.4 by Fabrice Frances				*
*				(initial work based on David Hedley's pcemu)                *
****************************************************************************/

#include <stdio.h>
#include <string.h>
#include "host.h"
#include "cpuintrf.h"
#include "memory.h"
#include "mamedbg.h"
#include "i86.h"
#include "i86intrf.h"


static UINT8 i86_reg_layout[] = {
	I86_IP,I86_SP,I86_FLAGS,I86_AX,I86_CX,I86_DX,I86_BX,I86_BP,I86_SI,I86_DI, -1,
	I86_ES,I86_CS,I86_SS,I86_DS,I86_VECTOR,I86_NMI_STATE,I86_IRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 i86_win_layout[] = {
     0, 0,80, 2,    /* register window (top rows) */
	 0, 3,34,19,	/* disassembler window (left colums) */
	35, 3,45, 9,	/* memory #1 window (right, upper middle) */
	35,13,45, 9,	/* memory #2 window (right, lower middle) */
     0,23,80, 1,    /* command line window (bottom rows) */
};

/* I86 registers */
typedef union
{                   /* eight general registers */
    UINT16 w[8];    /* viewed as 16 bits registers */
    UINT8  b[16];   /* or as 8 bit registers */
} i86basicregs;

typedef struct
{
    i86basicregs regs;
	int 	amask;			/* address mask */
    int     ip;
	UINT16	flags;
	UINT32	base[4];
	UINT16	sregs[4];
    int     (*irq_callback)(int irqline);
    int     AuxVal, OverVal, SignVal, ZeroVal, CarryVal, ParityVal; /* 0 or non-0 valued flags */
	UINT8	TF, IF, DF; 	/* 0 or 1 valued flags */
	UINT8	int_vector;
	UINT8	pending_irq;
	INT8	nmi_state;
	INT8	irq_state;
} i86_Regs;


/***************************************************************************/
/* cpu state                                                               */
/***************************************************************************/

int i86_ICount;

static i86_Regs I;
static unsigned prefix_base;	/* base address of the latest prefix segment */
static char seg_prefix;         /* prefix segment indicator */


/* The interrupt number of a pending external interrupt pending NMI is 2.	*/
/* For INTR interrupts, the level is caught on the bus during an INTA cycle */

#define INT_IRQ 0x01
#define NMI_IRQ 0x02

#include "instr.h"
#include "ea.h"
#include "modrm.h"

static UINT8 parity_table[256];
/***************************************************************************/

void i86_reset (void *param)
{
    unsigned int i,j,c;
    BREGS reg_name[8]={ AL, CL, DL, BL, AH, CH, DH, BH };

	memset( &I, 0, sizeof(I) );

	/* If a reset parameter is given, take it as pointer to an address mask */
    if( param )
		I.amask = *(unsigned*)param;
	else
		I.amask = 0x00ffff;
    I.sregs[CS] = 0xffff;
	I.base[CS] = I.sregs[CS] << 4;

	change_pc20( (I.base[CS] + I.ip) & I.amask);

    for (i = 0;i < 256; i++)
    {
		for (j = i, c = 0; j > 0; j >>= 1)
			if (j & 1) c++;

		parity_table[i] = !(c & 1);
    }

	I.ZeroVal = I.ParityVal = 1;

    for (i = 0; i < 256; i++)
    {
		Mod_RM.reg.b[i] = reg_name[(i & 0x38) >> 3];
		Mod_RM.reg.w[i] = (WREGS) ( (i & 0x38) >> 3) ;
    }

    for (i = 0xc0; i < 0x100; i++)
    {
		Mod_RM.RM.w[i] = (WREGS)( i & 7 );
		Mod_RM.RM.b[i] = (BREGS)reg_name[i & 7];
    }
}

void i86_exit (void)
{
	/* nothing to do ? */
}

static void i86_interrupt(unsigned int_num)
{
    unsigned dest_seg, dest_off;

    i_pushf();
	I.TF = I.IF = 0;

	if (int_num == -1)
		int_num = (*I.irq_callback)(0);

    dest_off = ReadWord(int_num*4);
    dest_seg = ReadWord(int_num*4+2);

	PUSH(I.sregs[CS]);
	PUSH(I.ip);
	I.ip = (WORD)dest_off;
	I.sregs[CS] = (WORD)dest_seg;
	I.base[CS] = SegBase(CS);
	change_pc20((I.base[CS]+I.ip) & I.amask);

}

void trap(void)
{
	instruction[FETCHOP]();
	i86_interrupt(1);
}

static void external_int(void)
{
	if( I.pending_irq & NMI_IRQ )
	{
		i86_interrupt(I86_NMI_INT);
		I.pending_irq &= ~NMI_IRQ;
	}
	else
	if( I.pending_irq )
	{
		/* the actual vector is retrieved after pushing flags */
		/* and clearing the IF */
		i86_interrupt(-1);
	}
}

/****************************************************************************/

static void i_add_br8(void)    /* Opcode 0x00 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    ADDB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_add_wr16(void)    /* Opcode 0x01 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    ADDW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_add_r8b(void)    /* Opcode 0x02 */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    ADDB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_add_r16w(void)    /* Opcode 0x03 */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    ADDW(dst,src);
    RegWord(ModRM)=dst;
}


static void i_add_ald8(void)    /* Opcode 0x04 */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    ADDB(dst,src);
	I.regs.b[AL]=dst;
}


static void i_add_axd16(void)    /* Opcode 0x05 */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    ADDW(dst,src);
	I.regs.w[AX]=dst;
}


static void i_push_es(void)    /* Opcode 0x06 */
{
	i86_ICount-=3;
	PUSH(I.sregs[ES]);
}


static void i_pop_es(void)    /* Opcode 0x07 */
{
	POP(I.sregs[ES]);
	I.base[ES] = SegBase(ES);
	i86_ICount-=2;
}

static void i_or_br8(void)    /* Opcode 0x08 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    ORB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_or_wr16(void)    /* Opcode 0x09 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    ORW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_or_r8b(void)    /* Opcode 0x0a */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    ORB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_or_r16w(void)    /* Opcode 0x0b */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    ORW(dst,src);
    RegWord(ModRM)=dst;
}

static void i_or_ald8(void)    /* Opcode 0x0c */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    ORB(dst,src);
	I.regs.b[AL]=dst;
}

static void i_or_axd16(void)    /* Opcode 0x0d */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    ORW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_push_cs(void)    /* Opcode 0x0e */
{
	i86_ICount-=3;
	PUSH(I.sregs[CS]);
}

/* Opcode 0x0f invalid */

static void i_adc_br8(void)    /* Opcode 0x10 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    src+=CF;
    ADDB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_adc_wr16(void)    /* Opcode 0x11 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    src+=CF;
    ADDW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_adc_r8b(void)    /* Opcode 0x12 */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    src+=CF;
    ADDB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_adc_r16w(void)    /* Opcode 0x13 */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    src+=CF;
    ADDW(dst,src);
    RegWord(ModRM)=dst;
}

static void i_adc_ald8(void)    /* Opcode 0x14 */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    src+=CF;
    ADDB(dst,src);
	I.regs.b[AL] = dst;
}

static void i_adc_axd16(void)    /* Opcode 0x15 */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    src+=CF;
    ADDW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_push_ss(void)    /* Opcode 0x16 */
{
	i86_ICount-=3;
	PUSH(I.sregs[SS]);
}

static void i_pop_ss(void)    /* Opcode 0x17 */
{
	i86_ICount-=2;
	POP(I.sregs[SS]);
	I.base[SS] = SegBase(SS);
	instruction[FETCHOP](); /* no interrupt before next instruction */
}

static void i_sbb_br8(void)    /* Opcode 0x18 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    src+=CF;
    SUBB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_sbb_wr16(void)    /* Opcode 0x19 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    src+=CF;
    SUBW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_sbb_r8b(void)    /* Opcode 0x1a */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    src+=CF;
    SUBB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_sbb_r16w(void)    /* Opcode 0x1b */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    src+=CF;
    SUBW(dst,src);
    RegWord(ModRM)= dst;
}

static void i_sbb_ald8(void)    /* Opcode 0x1c */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    src+=CF;
    SUBB(dst,src);
	I.regs.b[AL] = dst;
}

static void i_sbb_axd16(void)    /* Opcode 0x1d */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    src+=CF;
    SUBW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_push_ds(void)    /* Opcode 0x1e */
{
	i86_ICount-=3;
	PUSH(I.sregs[DS]);
}

static void i_pop_ds(void)    /* Opcode 0x1f */
{
	POP(I.sregs[DS]);
	I.base[DS] = SegBase(DS);
	i86_ICount-=2;
}

static void i_and_br8(void)    /* Opcode 0x20 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    ANDB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_and_wr16(void)    /* Opcode 0x21 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    ANDW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_and_r8b(void)    /* Opcode 0x22 */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    ANDB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_and_r16w(void)    /* Opcode 0x23 */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    ANDW(dst,src);
    RegWord(ModRM)=dst;
}

static void i_and_ald8(void)    /* Opcode 0x24 */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    ANDB(dst,src);
	I.regs.b[AL] = dst;
}

static void i_and_axd16(void)    /* Opcode 0x25 */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    ANDW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_es(void)    /* Opcode 0x26 */
{
    seg_prefix=TRUE;
	prefix_base=I.base[ES];
	i86_ICount-=2;
	instruction[FETCHOP]();
}

static void i_daa(void)    /* Opcode 0x27 */
{
	if (AF || ((I.regs.b[AL] & 0xf) > 9))
	{
		int tmp;
		I.regs.b[AL] = tmp = I.regs.b[AL] + 6;
		I.AuxVal = 1;
		I.CarryVal |= tmp & 0x100;
	}

	if (CF || (I.regs.b[AL] > 0x9f))
	{
		I.regs.b[AL] += 0x60;
		I.CarryVal = 1;
	}

	SetSZPF_Byte(I.regs.b[AL]);
	i86_ICount-=4;
}

static void i_sub_br8(void)    /* Opcode 0x28 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    SUBB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_sub_wr16(void)    /* Opcode 0x29 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    SUBW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_sub_r8b(void)    /* Opcode 0x2a */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    SUBB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_sub_r16w(void)    /* Opcode 0x2b */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    SUBW(dst,src);
    RegWord(ModRM)=dst;
}

static void i_sub_ald8(void)    /* Opcode 0x2c */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    SUBB(dst,src);
	I.regs.b[AL] = dst;
}

static void i_sub_axd16(void)    /* Opcode 0x2d */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    SUBW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_cs(void)    /* Opcode 0x2e */
{
    seg_prefix=TRUE;
	prefix_base=I.base[CS];
	i86_ICount-=2;
	instruction[FETCHOP]();
}

static void i_das(void)    /* Opcode 0x2f */
{
	if (AF || ((I.regs.b[AL] & 0xf) > 9))
	{
		int tmp;
		I.regs.b[AL] = tmp = I.regs.b[AL] - 6;
		I.AuxVal = 1;
		I.CarryVal |= tmp & 0x100;
	}

	if (CF || (I.regs.b[AL] > 0x9f))
	{
		I.regs.b[AL] -= 0x60;
		I.CarryVal = 1;
	}

	SetSZPF_Byte(I.regs.b[AL]);
	i86_ICount-=4;
}

static void i_xor_br8(void)    /* Opcode 0x30 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    XORB(dst,src);
    PutbackRMByte(ModRM,dst);
}

static void i_xor_wr16(void)    /* Opcode 0x31 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    XORW(dst,src);
    PutbackRMWord(ModRM,dst);
}

static void i_xor_r8b(void)    /* Opcode 0x32 */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    XORB(dst,src);
    RegByte(ModRM)=dst;
}

static void i_xor_r16w(void)    /* Opcode 0x33 */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    XORW(dst,src);
    RegWord(ModRM)=dst;
}

static void i_xor_ald8(void)    /* Opcode 0x34 */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    XORB(dst,src);
	I.regs.b[AL] = dst;
}

static void i_xor_axd16(void)    /* Opcode 0x35 */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    XORW(dst,src);
	I.regs.w[AX]=dst;
}

static void i_ss(void)    /* Opcode 0x36 */
{
    seg_prefix=TRUE;
	prefix_base=I.base[SS];
	i86_ICount-=2;
	instruction[FETCHOP]();
}

static void i_aaa(void)    /* Opcode 0x37 */
{
	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    {
		I.regs.b[AL] += 6;
		I.regs.b[AH] += 1;
		I.AuxVal = 1;
		I.CarryVal = 1;
    }
	else
	{
		I.AuxVal = 0;
		I.CarryVal = 0;
    }
	I.regs.b[AL] &= 0x0F;
	i86_ICount-=8;
}

static void i_cmp_br8(void)    /* Opcode 0x38 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    SUBB(dst,src);
}

static void i_cmp_wr16(void)    /* Opcode 0x39 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    SUBW(dst,src);
}

static void i_cmp_r8b(void)    /* Opcode 0x3a */
{
    DEF_r8b(dst,src);
	i86_ICount-=3;
    SUBB(dst,src);
}

static void i_cmp_r16w(void)    /* Opcode 0x3b */
{
    DEF_r16w(dst,src);
	i86_ICount-=3;
    SUBW(dst,src);
}

static void i_cmp_ald8(void)    /* Opcode 0x3c */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    SUBB(dst,src);
}

static void i_cmp_axd16(void)    /* Opcode 0x3d */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    SUBW(dst,src);
}

static void i_ds(void)    /* Opcode 0x3e */
{
    seg_prefix=TRUE;
	prefix_base=I.base[DS];
	i86_ICount-=2;
	instruction[FETCHOP]();
}

static void i_aas(void)    /* Opcode 0x3f */
{
	if (AF || ((I.regs.b[AL] & 0xf) > 9))
    {
		I.regs.b[AL] -= 6;
		I.regs.b[AH] -= 1;
		I.AuxVal = 1;
		I.CarryVal = 1;
    }
	else
	{
		I.AuxVal = 0;
		I.CarryVal = 0;
    }
	I.regs.b[AL] &= 0x0F;
	i86_ICount-=8;
}

#define IncWordReg(Reg) 					\
{											\
	unsigned tmp = (unsigned)I.regs.w[Reg]; \
	unsigned tmp1 = tmp+1;					\
	SetOFW_Add(tmp1,tmp,1); 				\
	SetAF(tmp1,tmp,1);						\
	SetSZPF_Word(tmp1); 					\
	I.regs.w[Reg]=tmp1; 					\
	i86_ICount-=3;							\
}

static void i_inc_ax(void)    /* Opcode 0x40 */
{
    IncWordReg(AX);
}

static void i_inc_cx(void)    /* Opcode 0x41 */
{
    IncWordReg(CX);
}

static void i_inc_dx(void)    /* Opcode 0x42 */
{
    IncWordReg(DX);
}

static void i_inc_bx(void)    /* Opcode 0x43 */
{
    IncWordReg(BX);
}

static void i_inc_sp(void)    /* Opcode 0x44 */
{
    IncWordReg(SP);
}

static void i_inc_bp(void)    /* Opcode 0x45 */
{
    IncWordReg(BP);
}

static void i_inc_si(void)    /* Opcode 0x46 */
{
    IncWordReg(SI);
}

static void i_inc_di(void)    /* Opcode 0x47 */
{
    IncWordReg(DI);
}

#define DecWordReg(Reg) \
{ \
	unsigned tmp = (unsigned)I.regs.w[Reg]; \
    unsigned tmp1 = tmp-1; \
    SetOFW_Sub(tmp1,1,tmp); \
    SetAF(tmp1,tmp,1); \
    SetSZPF_Word(tmp1); \
	I.regs.w[Reg]=tmp1; \
	i86_ICount-=3; \
}

static void i_dec_ax(void)    /* Opcode 0x48 */
{
    DecWordReg(AX);
}

static void i_dec_cx(void)    /* Opcode 0x49 */
{
    DecWordReg(CX);
}

static void i_dec_dx(void)    /* Opcode 0x4a */
{
    DecWordReg(DX);
}

static void i_dec_bx(void)    /* Opcode 0x4b */
{
    DecWordReg(BX);
}

static void i_dec_sp(void)    /* Opcode 0x4c */
{
    DecWordReg(SP);
}

static void i_dec_bp(void)    /* Opcode 0x4d */
{
    DecWordReg(BP);
}

static void i_dec_si(void)    /* Opcode 0x4e */
{
    DecWordReg(SI);
}

static void i_dec_di(void)    /* Opcode 0x4f */
{
    DecWordReg(DI);
}

static void i_push_ax(void)    /* Opcode 0x50 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[AX]);
}

static void i_push_cx(void)    /* Opcode 0x51 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[CX]);
}

static void i_push_dx(void)    /* Opcode 0x52 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[DX]);
}

static void i_push_bx(void)    /* Opcode 0x53 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[BX]);
}

static void i_push_sp(void)    /* Opcode 0x54 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[SP]);
}

static void i_push_bp(void)    /* Opcode 0x55 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[BP]);
}


static void i_push_si(void)    /* Opcode 0x56 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[SI]);
}

static void i_push_di(void)    /* Opcode 0x57 */
{
	i86_ICount-=4;
	PUSH(I.regs.w[DI]);
}

static void i_pop_ax(void)    /* Opcode 0x58 */
{
	i86_ICount-=2;
	POP(I.regs.w[AX]);
}

static void i_pop_cx(void)    /* Opcode 0x59 */
{
	i86_ICount-=2;
	POP(I.regs.w[CX]);
}

static void i_pop_dx(void)    /* Opcode 0x5a */
{
	i86_ICount-=2;
	POP(I.regs.w[DX]);
}

static void i_pop_bx(void)    /* Opcode 0x5b */
{
	i86_ICount-=2;
	POP(I.regs.w[BX]);
}

static void i_pop_sp(void)    /* Opcode 0x5c */
{
	i86_ICount-=2;
	POP(I.regs.w[SP]);
}

static void i_pop_bp(void)    /* Opcode 0x5d */
{
	i86_ICount-=2;
	POP(I.regs.w[BP]);
}

static void i_pop_si(void)    /* Opcode 0x5e */
{
	i86_ICount-=2;
	POP(I.regs.w[SI]);
}

static void i_pop_di(void)    /* Opcode 0x5f */
{
	i86_ICount-=2;
	POP(I.regs.w[DI]);
}

static void i_pusha(void)    /* Opcode 0x60 */
{
	unsigned tmp=I.regs.w[SP];
	i86_ICount-=17;
	PUSH(I.regs.w[AX]);
	PUSH(I.regs.w[CX]);
	PUSH(I.regs.w[DX]);
	PUSH(I.regs.w[BX]);
    PUSH(tmp);
	PUSH(I.regs.w[BP]);
	PUSH(I.regs.w[SI]);
	PUSH(I.regs.w[DI]);
}

static void i_popa(void)    /* Opcode 0x61 */
{
    unsigned tmp;
	i86_ICount-=19;
	POP(I.regs.w[DI]);
	POP(I.regs.w[SI]);
	POP(I.regs.w[BP]);
    POP(tmp);
	POP(I.regs.w[BX]);
	POP(I.regs.w[DX]);
	POP(I.regs.w[CX]);
	POP(I.regs.w[AX]);
}

static void i_bound(void)    /* Opcode 0x62 */
{
	unsigned ModRM = FETCHOP;
    int low = (INT16)GetRMWord(ModRM);
    int high= (INT16)GetnextRMWord;
    int tmp= (INT16)RegWord(ModRM);
    if (tmp<low || tmp>high) {
		I.ip-=2;
		i86_interrupt(5);
    }
}

static void i_push_d16(void)    /* Opcode 0x68 */
{
    unsigned tmp = FETCH;
	i86_ICount-=3;
    tmp += FETCH << 8;
    PUSH(tmp);
}

static void i_imul_d16(void)    /* Opcode 0x69 */
{
    DEF_r16w(dst,src);
    unsigned src2=FETCH;
    src+=(FETCH<<8);

	i86_ICount-=150;
    dst = (INT32)((INT16)src)*(INT32)((INT16)src2);
	I.CarryVal = I.OverVal = (((INT32)dst) >> 15 != 0) && (((INT32)dst) >> 15 != -1);
    RegWord(ModRM)=(WORD)dst;
}


static void i_push_d8(void)    /* Opcode 0x6a */
{
    unsigned tmp = (WORD)((INT16)((INT8)FETCH));
	i86_ICount-=3;
    PUSH(tmp);
}

static void i_imul_d8(void)    /* Opcode 0x6b */
{
    DEF_r16w(dst,src);
    unsigned src2= (WORD)((INT16)((INT8)FETCH));

	i86_ICount-=150;
    dst = (INT32)((INT16)src)*(INT32)((INT16)src2);
	I.CarryVal = I.OverVal = (((INT32)dst) >> 15 != 0) && (((INT32)dst) >> 15 != -1);
    RegWord(ModRM)=(WORD)dst;
}

static void i_insb(void)    /* Opcode 0x6c */
{
	i86_ICount-=5;
	PutMemB(ES,I.regs.w[DI],read_port(I.regs.w[DX]));
	I.regs.w[DI]+= -2 * I.DF + 1;
}

static void i_insw(void)    /* Opcode 0x6d */
{
	i86_ICount-=5;
	PutMemB(ES,I.regs.w[DI],read_port(I.regs.w[DX]));
	PutMemB(ES,I.regs.w[DI]+1,read_port(I.regs.w[DX]+1));
	I.regs.w[DI]+= -4 * I.DF + 2;
}

static void i_outsb(void)    /* Opcode 0x6e */
{
	i86_ICount-=5;
	write_port(I.regs.w[DX],GetMemB(DS,I.regs.w[SI]));
	I.regs.w[DI]+= -2 * I.DF + 1;
}

static void i_outsw(void)    /* Opcode 0x6f */
{
	i86_ICount-=5;
	write_port(I.regs.w[DX],GetMemB(DS,I.regs.w[SI]));
	write_port(I.regs.w[DX]+1,GetMemB(DS,I.regs.w[SI]+1));
	I.regs.w[DI]+= -4 * I.DF + 2;
}

static void i_jo(void)    /* Opcode 0x70 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (OF)
	{
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jno(void)    /* Opcode 0x71 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (!OF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jb(void)    /* Opcode 0x72 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (CF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnb(void)    /* Opcode 0x73 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (!CF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jz(void)    /* Opcode 0x74 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (ZF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnz(void)    /* Opcode 0x75 */
{
	int tmp = (int)((INT8)FETCHOP);
	if (!ZF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jbe(void)    /* Opcode 0x76 */
{
	int tmp = (int)((INT8)FETCHOP);
    if (CF || ZF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnbe(void)    /* Opcode 0x77 */
{
	int tmp = (int)((INT8)FETCHOP);
    if (!(CF || ZF)) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_js(void)    /* Opcode 0x78 */
{
	int tmp = (int)((INT8)FETCHOP);
    if (SF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jns(void)    /* Opcode 0x79 */
{
	int tmp = (int)((INT8)FETCHOP);
    if (!SF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jp(void)    /* Opcode 0x7a */
{
	int tmp = (int)((INT8)FETCHOP);
    if (PF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnp(void)    /* Opcode 0x7b */
{
	int tmp = (int)((INT8)FETCHOP);
    if (!PF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jl(void)    /* Opcode 0x7c */
{
	int tmp = (int)((INT8)FETCHOP);
    if ((SF!=OF)&&!ZF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnl(void)    /* Opcode 0x7d */
{
	int tmp = (int)((INT8)FETCHOP);
    if (ZF||(SF==OF)) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jle(void)    /* Opcode 0x7e */
{
	int tmp = (int)((INT8)FETCHOP);
    if (ZF||(SF!=OF)) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_jnle(void)    /* Opcode 0x7f */
{
	int tmp = (int)((INT8)FETCHOP);
    if ((SF==OF)&&!ZF) {
		I.ip = (WORD)(I.ip+tmp);
		i86_ICount-=16;
		change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=4;
}

static void i_80pre(void)    /* Opcode 0x80 */
{
	unsigned ModRM = FETCHOP;
    unsigned dst = GetRMByte(ModRM);
    unsigned src = FETCH;
	i86_ICount-=4;

    switch (ModRM & 0x38)
    {
    case 0x00:  /* ADD eb,d8 */
        ADDB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x08:  /* OR eb,d8 */
        ORB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x10:  /* ADC eb,d8 */
        src+=CF;
        ADDB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x18:  /* SBB eb,b8 */
        src+=CF;
        SUBB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x20:  /* AND eb,d8 */
        ANDB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x28:  /* SUB eb,d8 */
        SUBB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x30:  /* XOR eb,d8 */
        XORB(dst,src);
        PutbackRMByte(ModRM,dst);
	break;
    case 0x38:  /* CMP eb,d8 */
        SUBB(dst,src);
	break;
    }
}


static void i_81pre(void)    /* Opcode 0x81 */
{
	unsigned ModRM = FETCHOP;
    unsigned dst = GetRMWord(ModRM);
    unsigned src = FETCH;
    src+= (FETCH << 8);
	i86_ICount-=2;

    switch (ModRM & 0x38)
    {
    case 0x00:  /* ADD ew,d16 */
        ADDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x08:  /* OR ew,d16 */
        ORW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x10:  /* ADC ew,d16 */
        src+=CF;
		ADDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x18:  /* SBB ew,d16 */
        src+=CF;
        SUBW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x20:  /* AND ew,d16 */
        ANDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x28:  /* SUB ew,d16 */
        SUBW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x30:  /* XOR ew,d16 */
        XORW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x38:  /* CMP ew,d16 */
        SUBW(dst,src);
	break;
    }
}

static void i_82pre(void)	 /* Opcode 0x82 */
{
	unsigned ModRM = FETCHOP;
	unsigned dst = GetRMByte(ModRM);
	unsigned src = FETCH;
	i86_ICount-=2;

    switch (ModRM & 0x38)
    {
	case 0x00:	/* ADD eb,d8 */
		ADDB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x08:	/* OR eb,d8 */
		ORB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x10:	/* ADC eb,d8 */
        src+=CF;
		ADDB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x18:	/* SBB eb,d8 */
        src+=CF;
		SUBB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x20:	/* AND eb,d8 */
		ANDB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x28:	/* SUB eb,d8 */
		SUBB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x30:	/* XOR eb,d8 */
		XORB(dst,src);
		PutbackRMByte(ModRM,dst);
	break;
	case 0x38:	/* CMP eb,d8 */
		SUBB(dst,src);
	break;
    }
}

static void i_83pre(void)    /* Opcode 0x83 */
{
	unsigned ModRM = FETCHOP;
    unsigned dst = GetRMWord(ModRM);
    unsigned src = (WORD)((INT16)((INT8)FETCH));
	i86_ICount-=2;

    switch (ModRM & 0x38)
    {
    case 0x00:  /* ADD ew,d8 */
        ADDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x08:  /* OR ew,d8 */
        ORW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x10:  /* ADC ew,d8 */
        src+=CF;
        ADDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x18:  /* SBB ew,d8 */
        src+=CF;
        SUBW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x20:  /* AND ew,d8 */
        ANDW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x28:  /* SUB ew,d8 */
        SUBW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x30:  /* XOR ew,d8 */
        XORW(dst,src);
        PutbackRMWord(ModRM,dst);
	break;
    case 0x38:  /* CMP ew,d8 */
        SUBW(dst,src);
	break;
    }
}

static void i_test_br8(void)    /* Opcode 0x84 */
{
    DEF_br8(dst,src);
	i86_ICount-=3;
    ANDB(dst,src);
}

static void i_test_wr16(void)    /* Opcode 0x85 */
{
    DEF_wr16(dst,src);
	i86_ICount-=3;
    ANDW(dst,src);
}

static void i_xchg_br8(void)    /* Opcode 0x86 */
{
    DEF_br8(dst,src);
	i86_ICount-=4;
    RegByte(ModRM)=dst;
    PutbackRMByte(ModRM,src);
}

static void i_xchg_wr16(void)    /* Opcode 0x87 */
{
    DEF_wr16(dst,src);
	i86_ICount-=4;
    RegWord(ModRM)=dst;
    PutbackRMWord(ModRM,src);
}

static void i_mov_br8(void)    /* Opcode 0x88 */
{
	unsigned ModRM = FETCHOP;
    BYTE src = RegByte(ModRM);
	i86_ICount-=2;
    PutRMByte(ModRM,src);
}

static void i_mov_wr16(void)    /* Opcode 0x89 */
{
	unsigned ModRM = FETCHOP;
    WORD src = RegWord(ModRM);
	i86_ICount-=2;
    PutRMWord(ModRM,src);
}

static void i_mov_r8b(void)    /* Opcode 0x8a */
{
	unsigned ModRM = FETCHOP;
    BYTE src = GetRMByte(ModRM);
	i86_ICount-=2;
    RegByte(ModRM)=src;
}

static void i_mov_r16w(void)    /* Opcode 0x8b */
{
	unsigned ModRM = FETCHOP;
    WORD src = GetRMWord(ModRM);
	i86_ICount-=2;
    RegWord(ModRM)=src;
}

static void i_mov_wsreg(void)    /* Opcode 0x8c */
{
	unsigned ModRM = FETCHOP;
	i86_ICount-=2;
	if (ModRM & 0x20) return;	/* HJB 12/13/98 1xx is invalid */
	PutRMWord(ModRM,I.sregs[(ModRM & 0x38) >> 3]);
}

static void i_lea(void)    /* Opcode 0x8d */
{
	unsigned ModRM = FETCHOP;
	i86_ICount-=2;
	(void)(*GetEA[ModRM])();
	RegWord(ModRM)=EO;	/* HJB 12/13/98 effective offset (no segment part) */
}

static void i_mov_sregw(void)    /* Opcode 0x8e */
{
	unsigned ModRM = FETCHOP;
    WORD src = GetRMWord(ModRM);

	i86_ICount-=2;
    switch (ModRM & 0x38)
    {
    case 0x00:  /* mov es,ew */
	I.sregs[ES] = src;
	I.base[ES] = SegBase(ES);
	break;
    case 0x18:  /* mov ds,ew */
	I.sregs[DS] = src;
	I.base[DS] = SegBase(DS);
	break;
    case 0x10:  /* mov ss,ew */
	I.sregs[SS] = src;
	I.base[SS] = SegBase(SS); /* no interrupt allowed before next instr */
	instruction[FETCHOP]();
	break;
    case 0x08:  /* mov cs,ew */
	break;  /* doesn't do a jump far */
    }
}

static void i_popw(void)    /* Opcode 0x8f */
{
	unsigned ModRM = FETCHOP;
    WORD tmp;
    POP(tmp);
	i86_ICount-=4;
    PutRMWord(ModRM,tmp);
}


#define XchgAXReg(Reg) \
{ \
    WORD tmp; \
	tmp = I.regs.w[Reg]; \
	I.regs.w[Reg] = I.regs.w[AX]; \
	I.regs.w[AX] = tmp; \
	i86_ICount-=3; \
}


static void i_nop(void)    /* Opcode 0x90 */
{
    /* this is XchgAXReg(AX); */
	i86_ICount-=3;
}

static void i_xchg_axcx(void)    /* Opcode 0x91 */
{
    XchgAXReg(CX);
}

static void i_xchg_axdx(void)    /* Opcode 0x92 */
{
    XchgAXReg(DX);
}

static void i_xchg_axbx(void)    /* Opcode 0x93 */
{
    XchgAXReg(BX);
}

static void i_xchg_axsp(void)    /* Opcode 0x94 */
{
    XchgAXReg(SP);
}

static void i_xchg_axbp(void)    /* Opcode 0x95 */
{
    XchgAXReg(BP);
}

static void i_xchg_axsi(void)    /* Opcode 0x96 */
{
    XchgAXReg(SI);
}

static void i_xchg_axdi(void)    /* Opcode 0x97 */
{
    XchgAXReg(DI);
}

static void i_cbw(void)    /* Opcode 0x98 */
{
	i86_ICount-=2;
	I.regs.b[AH] = (I.regs.b[AL] & 0x80) ? 0xff : 0;
}

static void i_cwd(void)    /* Opcode 0x99 */
{
	i86_ICount-=5;
	I.regs.w[DX] = (I.regs.b[AH] & 0x80) ? 0xffff : 0;
}

static void i_call_far(void)
{
    unsigned tmp, tmp2;

	tmp = FETCHOP;
	tmp += FETCHOP << 8;

	tmp2 = FETCHOP;
	tmp2 += FETCHOP << 8;

	PUSH(I.sregs[CS]);
	PUSH(I.ip);

	I.ip = (WORD)tmp;
	I.sregs[CS] = (WORD)tmp2;
	I.base[CS] = SegBase(CS);
	i86_ICount-=14;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_wait(void)    /* Opcode 0x9b */
{
	i86_ICount-=4;
}

static void i_pushf(void)    /* Opcode 0x9c */
{
	i86_ICount-=3;
    PUSH( CompressFlags() | 0xf000 );
}

static void i_popf(void)    /* Opcode 0x9d */
{
    unsigned tmp;
    POP(tmp);
	i86_ICount-=2;
    ExpandFlags(tmp);

	if (I.TF) trap();
}

static void i_sahf(void)    /* Opcode 0x9e */
{
	unsigned tmp = (CompressFlags() & 0xff00) | (I.regs.b[AH] & 0xd5);

    ExpandFlags(tmp);
}

static void i_lahf(void)    /* Opcode 0x9f */
{
	I.regs.b[AH] = CompressFlags() & 0xff;
	i86_ICount-=4;
}

static void i_mov_aldisp(void)    /* Opcode 0xa0 */
{
    unsigned addr;

	addr = FETCHOP;
	addr += FETCHOP << 8;

	i86_ICount-=4;
	I.regs.b[AL] = GetMemB(DS, addr);
}

static void i_mov_axdisp(void)    /* Opcode 0xa1 */
{
    unsigned addr;

	addr = FETCHOP;
	addr += FETCHOP << 8;

	i86_ICount-=4;
	I.regs.b[AL] = GetMemB(DS, addr);
	I.regs.b[AH] = GetMemB(DS, addr+1);
}

static void i_mov_dispal(void)    /* Opcode 0xa2 */
{
    unsigned addr;

	addr = FETCHOP;
	addr += FETCHOP << 8;

	i86_ICount-=3;
	PutMemB(DS, addr, I.regs.b[AL]);
}

static void i_mov_dispax(void)    /* Opcode 0xa3 */
{
    unsigned addr;

	addr = FETCHOP;
	addr += FETCHOP << 8;

	i86_ICount-=3;
	PutMemB(DS, addr, I.regs.b[AL]);
	PutMemB(DS, addr+1, I.regs.b[AH]);
}

static void i_movsb(void)    /* Opcode 0xa4 */
{
	BYTE tmp = GetMemB(DS,I.regs.w[SI]);
	PutMemB(ES,I.regs.w[DI], tmp);
	I.regs.w[DI] += -2 * I.DF + 1;
	I.regs.w[SI] += -2 * I.DF + 1;
	i86_ICount-=5;
}

static void i_movsw(void)    /* Opcode 0xa5 */
{
	WORD tmp = GetMemW(DS,I.regs.w[SI]);
	PutMemW(ES,I.regs.w[DI], tmp);
	I.regs.w[DI] += -4 * I.DF + 2;
	I.regs.w[SI] += -4 * I.DF + 2;
	i86_ICount-=5;
}

static void i_cmpsb(void)    /* Opcode 0xa6 */
{
	unsigned dst = GetMemB(ES, I.regs.w[DI]);
	unsigned src = GetMemB(DS, I.regs.w[SI]);
    SUBB(src,dst); /* opposite of the usual convention */
	I.regs.w[DI] += -2 * I.DF + 1;
	I.regs.w[SI] += -2 * I.DF + 1;
	i86_ICount-=10;
}

static void i_cmpsw(void)    /* Opcode 0xa7 */
{
	unsigned dst = GetMemW(ES, I.regs.w[DI]);
	unsigned src = GetMemW(DS, I.regs.w[SI]);
    SUBW(src,dst); /* opposite of the usual convention */
	I.regs.w[DI] += -4 * I.DF + 2;
	I.regs.w[SI] += -4 * I.DF + 2;
	i86_ICount-=10;
}

static void i_test_ald8(void)    /* Opcode 0xa8 */
{
    DEF_ald8(dst,src);
	i86_ICount-=4;
    ANDB(dst,src);
}

static void i_test_axd16(void)    /* Opcode 0xa9 */
{
    DEF_axd16(dst,src);
	i86_ICount-=4;
    ANDW(dst,src);
}

static void i_stosb(void)    /* Opcode 0xaa */
{
	PutMemB(ES,I.regs.w[DI],I.regs.b[AL]);
	I.regs.w[DI] += -2 * I.DF + 1;
	i86_ICount-=4;
}

static void i_stosw(void)    /* Opcode 0xab */
{
	PutMemB(ES,I.regs.w[DI],I.regs.b[AL]);
	PutMemB(ES,I.regs.w[DI]+1,I.regs.b[AH]);
	I.regs.w[DI] += -4 * I.DF + 2;
	i86_ICount-=4;
}

static void i_lodsb(void)    /* Opcode 0xac */
{
	I.regs.b[AL] = GetMemB(DS,I.regs.w[SI]);
	I.regs.w[SI] += -2 * I.DF + 1;
	i86_ICount-=6;
}

static void i_lodsw(void)    /* Opcode 0xad */
{
	I.regs.w[AX] = GetMemW(DS,I.regs.w[SI]);
	I.regs.w[SI] +=  -4 * I.DF + 2;
	i86_ICount-=6;
}

static void i_scasb(void)    /* Opcode 0xae */
{
	unsigned src = GetMemB(ES, I.regs.w[DI]);
	unsigned dst = I.regs.b[AL];
    SUBB(dst,src);
	I.regs.w[DI] += -2 * I.DF + 1;
	i86_ICount-=9;
}

static void i_scasw(void)    /* Opcode 0xaf */
{
	unsigned src = GetMemW(ES, I.regs.w[DI]);
	unsigned dst = I.regs.w[AX];
    SUBW(dst,src);
	I.regs.w[DI] += -4 * I.DF + 2;
	i86_ICount-=9;
}

static void i_mov_ald8(void)    /* Opcode 0xb0 */
{
	I.regs.b[AL] = FETCH;
	i86_ICount-=4;
}

static void i_mov_cld8(void)    /* Opcode 0xb1 */
{
	I.regs.b[CL] = FETCH;
	i86_ICount-=4;
}

static void i_mov_dld8(void)    /* Opcode 0xb2 */
{
	I.regs.b[DL] = FETCH;
	i86_ICount-=4;
}

static void i_mov_bld8(void)    /* Opcode 0xb3 */
{
	I.regs.b[BL] = FETCH;
	i86_ICount-=4;
}

static void i_mov_ahd8(void)    /* Opcode 0xb4 */
{
	I.regs.b[AH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_chd8(void)    /* Opcode 0xb5 */
{
	I.regs.b[CH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_dhd8(void)    /* Opcode 0xb6 */
{
	I.regs.b[DH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_bhd8(void)    /* Opcode 0xb7 */
{
	I.regs.b[BH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_axd16(void)    /* Opcode 0xb8 */
{
	I.regs.b[AL] = FETCH;
	I.regs.b[AH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_cxd16(void)    /* Opcode 0xb9 */
{
	I.regs.b[CL] = FETCH;
	I.regs.b[CH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_dxd16(void)    /* Opcode 0xba */
{
	I.regs.b[DL] = FETCH;
	I.regs.b[DH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_bxd16(void)    /* Opcode 0xbb */
{
	I.regs.b[BL] = FETCH;
	I.regs.b[BH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_spd16(void)    /* Opcode 0xbc */
{
	I.regs.b[SPL] = FETCH;
	I.regs.b[SPH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_bpd16(void)    /* Opcode 0xbd */
{
	I.regs.b[BPL] = FETCH;
	I.regs.b[BPH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_sid16(void)    /* Opcode 0xbe */
{
	I.regs.b[SIL] = FETCH;
	I.regs.b[SIH] = FETCH;
	i86_ICount-=4;
}

static void i_mov_did16(void)    /* Opcode 0xbf */
{
	I.regs.b[DIL] = FETCH;
	I.regs.b[DIH] = FETCH;
	i86_ICount-=4;
}

void rotate_shift_Byte(unsigned ModRM, unsigned count)
{
  unsigned src = (unsigned)GetRMByte(ModRM);
  unsigned dst=src;

  if (count==0)
  {
	i86_ICount-=8; /* or 7 if dest is in memory */
  }
  else if (count==1)
  {
	i86_ICount-=2;
    switch (ModRM & 0x38)
    {
      case 0x00:  /* ROL eb,1 */
		I.CarryVal = src & 0x80;
        dst=(src<<1)+CF;
        PutbackRMByte(ModRM,dst);
		I.OverVal = (src^dst)&0x80;
		break;
      case 0x08:  /* ROR eb,1 */
		I.CarryVal = src & 0x01;
        dst = ((CF<<8)+src) >> 1;
        PutbackRMByte(ModRM,dst);
		I.OverVal = (src^dst)&0x80;
		break;
      case 0x10:  /* RCL eb,1 */
        dst=(src<<1)+CF;
        PutbackRMByte(ModRM,dst);
        SetCFB(dst);
		I.OverVal = (src^dst)&0x80;
		break;
      case 0x18:  /* RCR eb,1 */
        dst = ((CF<<8)+src) >> 1;
        PutbackRMByte(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = (src^dst)&0x80;
		break;
      case 0x20:  /* SHL eb,1 */
      case 0x30:
        dst = src << 1;
        PutbackRMByte(ModRM,dst);
        SetCFB(dst);
		I.OverVal = (src^dst)&0x80;
		I.AuxVal = 1;
        SetSZPF_Byte(dst);
		break;
      case 0x28:  /* SHR eb,1 */
        dst = src >> 1;
        PutbackRMByte(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = src & 0x80;
		I.AuxVal = 1;
        SetSZPF_Byte(dst);
		break;
      case 0x38:  /* SAR eb,1 */
        dst = ((INT8)src) >> 1;
        PutbackRMByte(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = 0;
		I.AuxVal = 1;
        SetSZPF_Byte(dst);
		break;
    }
  }
  else
  {
	i86_ICount-=8+4*count; /* or 7+4*count if dest is in memory */
    switch (ModRM & 0x38)
    {
      case 0x00:  /* ROL eb,count */
		for (; count > 0; count--)
		{
			I.CarryVal = dst & 0x80;
            dst = (dst << 1) + CF;
		}
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
     case 0x08:  /* ROR eb,count */
		for (; count > 0; count--)
		{
			I.CarryVal = dst & 0x01;
            dst = (dst >> 1) + (CF << 7);
		}
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
      case 0x10:  /* RCL eb,count */
		for (; count > 0; count--)
		{
            dst = (dst << 1) + CF;
            SetCFB(dst);
		}
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
      case 0x18:  /* RCR eb,count */
		for (; count > 0; count--)
		{
            dst = (CF<<8)+dst;
			I.CarryVal = dst & 0x01;
            dst >>= 1;
		}
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
      case 0x20:
      case 0x30:  /* SHL eb,count */
        dst <<= count;
        SetCFB(dst);
		I.AuxVal = 1;
        SetSZPF_Byte(dst);
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
      case 0x28:  /* SHR eb,count */
        dst >>= count-1;
		I.CarryVal = dst & 0x1;
        dst >>= 1;
        SetSZPF_Byte(dst);
		I.AuxVal = 1;
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
      case 0x38:  /* SAR eb,count */
        dst = ((INT8)dst) >> (count-1);
		I.CarryVal = dst & 0x1;
        dst = ((INT8)((BYTE)dst)) >> 1;
        SetSZPF_Byte(dst);
		I.AuxVal = 1;
        PutbackRMByte(ModRM,(BYTE)dst);
		break;
    }
  }
}

void rotate_shift_Word(unsigned ModRM, unsigned count)
{
  unsigned src = GetRMWord(ModRM);
  unsigned dst=src;

  if (count==0)
  {
	i86_ICount-=8; /* or 7 if dest is in memory */
  }
  else if (count==1)
  {
	i86_ICount-=2;
    switch (ModRM & 0x38)
    {
#if 0
      case 0x00:  /* ROL ew,1 */
        tmp2 = (tmp << 1) + CF;
		SetCFW(tmp2);
		I.OverVal = !(!(tmp & 0x4000)) != CF;
		PutbackRMWord(ModRM,tmp2);
		break;
      case 0x08:  /* ROR ew,1 */
		I.CarryVal = tmp & 0x01;
		tmp2 = (tmp >> 1) + ((unsigned)CF << 15);
		I.OverVal = !(!(tmp & 0x8000)) != CF;
		PutbackRMWord(ModRM,tmp2);
		break;
      case 0x10:  /* RCL ew,1 */
		tmp2 = (tmp << 1) + CF;
		SetCFW(tmp2);
		I.OverVal = (tmp ^ (tmp << 1)) & 0x8000;
		PutbackRMWord(ModRM,tmp2);
		break;
	  case 0x18:  /* RCR ew,1 */
		tmp2 = (tmp >> 1) + ((unsigned)CF << 15);
		I.OverVal = !(!(tmp & 0x8000)) != CF;
		I.CarryVal = tmp & 0x01;
		PutbackRMWord(ModRM,tmp2);
		break;
      case 0x20:  /* SHL ew,1 */
      case 0x30:
		tmp <<= 1;

		SetCFW(tmp);
		SetOFW_Add(tmp,tmp2,tmp2);
		I.AuxVal = 1;
		SetSZPF_Word(tmp);

		PutbackRMWord(ModRM,tmp);
		break;
      case 0x28:  /* SHR ew,1 */
		I.CarryVal = tmp & 0x01;
		I.OverVal = tmp & 0x8000;

		tmp2 = tmp >> 1;

		SetSZPF_Word(tmp2);
		I.AuxVal = 1;
		PutbackRMWord(ModRM,tmp2);
		break;
      case 0x38:  /* SAR ew,1 */
		I.CarryVal = tmp & 0x01;
		I.OverVal = 0;

		tmp2 = (tmp >> 1) | (tmp & 0x8000);

		SetSZPF_Word(tmp2);
		I.AuxVal = 1;
		PutbackRMWord(ModRM,tmp2);
		break;
#else
      case 0x00:  /* ROL ew,1 */
		I.CarryVal = src & 0x8000;
        dst=(src<<1)+CF;
        PutbackRMWord(ModRM,dst);
		I.OverVal = (src^dst)&0x8000;
		break;
      case 0x08:  /* ROR ew,1 */
		I.CarryVal = src & 0x01;
        dst = ((CF<<16)+src) >> 1;
        PutbackRMWord(ModRM,dst);
		I.OverVal = (src^dst)&0x8000;
		break;
      case 0x10:  /* RCL ew,1 */
        dst=(src<<1)+CF;
        PutbackRMWord(ModRM,dst);
        SetCFW(dst);
		I.OverVal = (src^dst)&0x8000;
		break;
      case 0x18:  /* RCR ew,1 */
        dst = ((CF<<16)+src) >> 1;
        PutbackRMWord(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = (src^dst)&0x8000;
		break;
      case 0x20:  /* SHL ew,1 */
      case 0x30:
        dst = src << 1;
        PutbackRMWord(ModRM,dst);
        SetCFW(dst);
		I.OverVal = (src^dst)&0x8000;
		I.AuxVal = 1;
        SetSZPF_Word(dst);
		break;
      case 0x28:  /* SHR ew,1 */
        dst = src >> 1;
        PutbackRMWord(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = src & 0x8000;
		I.AuxVal = 1;
        SetSZPF_Word(dst);
		break;
      case 0x38:  /* SAR ew,1 */
        dst = ((INT16)src) >> 1;
        PutbackRMWord(ModRM,dst);
		I.CarryVal = src & 0x01;
		I.OverVal = 0;
		I.AuxVal = 1;
        SetSZPF_Word(dst);
	break;
#endif
    }
  }
  else
  {
	i86_ICount-=8+4*count; /* or 7+4*count if dest is in memory */

    switch (ModRM & 0x38)
    {
      case 0x00:  /* ROL ew,count */
		for (; count > 0; count--)
		{
			I.CarryVal = dst & 0x8000;
            dst = (dst << 1) + CF;
		}
        PutbackRMWord(ModRM,dst);
		break;
      case 0x08:  /* ROR ew,count */
		for (; count > 0; count--)
		{
			I.CarryVal = dst & 0x01;
            dst = (dst >> 1) + (CF << 15);
		}
        PutbackRMWord(ModRM,dst);
		break;
      case 0x10:  /* RCL ew,count */
		for (; count > 0; count--)
		{
            dst = (dst << 1) + CF;
            SetCFW(dst);
		}
        PutbackRMWord(ModRM,dst);
	break;
      case 0x18:  /* RCR ew,count */
		for (; count > 0; count--)
		{
            dst = dst + (CF << 16);
			I.CarryVal = dst & 0x01;
            dst >>= 1;
		}
        PutbackRMWord(ModRM,dst);
		break;
      case 0x20:
      case 0x30:  /* SHL ew,count */
        dst <<= count;
        SetCFW(dst);
		I.AuxVal = 1;
        SetSZPF_Word(dst);
        PutbackRMWord(ModRM,dst);
		break;
      case 0x28:  /* SHR ew,count */
        dst >>= count-1;
		I.CarryVal = dst & 0x1;
        dst >>= 1;
        SetSZPF_Word(dst);
		I.AuxVal = 1;
        PutbackRMWord(ModRM,dst);
		break;
      case 0x38:  /* SAR ew,count */
        dst = ((INT16)dst) >> (count-1);
		I.CarryVal = dst & 0x01;
        dst = ((INT16)((WORD)dst)) >> 1;
        SetSZPF_Word(dst);
		I.AuxVal = 1;
        PutbackRMWord(ModRM,dst);
		break;
    }
  }
}


static void i_rotshft_bd8(void)    /* Opcode 0xc0 */
{
	unsigned ModRM = FETCHOP;
	unsigned count = FETCHOP;

    rotate_shift_Byte(ModRM,count);
}

static void i_rotshft_wd8(void)    /* Opcode 0xc1 */
{
	unsigned ModRM = FETCHOP;
	unsigned count = FETCHOP;

    rotate_shift_Word(ModRM,count);
}


static void i_ret_d16(void)    /* Opcode 0xc2 */
{
	unsigned count = FETCHOP;
	count += FETCHOP << 8;
	POP(I.ip);
	I.regs.w[SP]+=count;
	i86_ICount-=14;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_ret(void)    /* Opcode 0xc3 */
{
	POP(I.ip);
	i86_ICount-=10;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_les_dw(void)    /* Opcode 0xc4 */
{
	unsigned ModRM = FETCHOP;
    WORD tmp = GetRMWord(ModRM);

    RegWord(ModRM)= tmp;
	I.sregs[ES] = GetnextRMWord;
	I.base[ES] = SegBase(ES);
	i86_ICount-=4;
}

static void i_lds_dw(void)    /* Opcode 0xc5 */
{
	unsigned ModRM = FETCHOP;
    WORD tmp = GetRMWord(ModRM);

    RegWord(ModRM)=tmp;
	I.sregs[DS] = GetnextRMWord;
	I.base[DS] = SegBase(DS);
	i86_ICount-=4;
}

static void i_mov_bd8(void)    /* Opcode 0xc6 */
{
	unsigned ModRM = FETCHOP;
	i86_ICount-=4;
    PutImmRMByte(ModRM);
}

static void i_mov_wd16(void)    /* Opcode 0xc7 */
{
	unsigned ModRM = FETCHOP;
	i86_ICount-=4;
    PutImmRMWord(ModRM);
}

static void i_enter(void)    /* Opcode 0xc8 */
{
	unsigned nb = FETCHOP;
    unsigned i,level;

	i86_ICount-=11;
	nb += FETCHOP << 8;
	level = FETCHOP;
	PUSH(I.regs.w[BP]);
	I.regs.w[BP]=I.regs.w[SP];
	I.regs.w[SP] -= nb;
    for (i=1;i<level;i++) {
		PUSH(GetMemW(SS,I.regs.w[BP]-i*2));
		i86_ICount-=4;
    }
	if (level) PUSH(I.regs.w[BP]);
}

static void i_leave(void)    /* Opcode 0xc9 */
{
	i86_ICount-=5;
	I.regs.w[SP]=I.regs.w[BP];
	POP(I.regs.w[BP]);
}

static void i_retf_d16(void)    /* Opcode 0xca */
{
	unsigned count = FETCHOP;
	count += FETCHOP << 8;
	POP(I.ip);
	POP(I.sregs[CS]);
	I.base[CS] = SegBase(CS);
	I.regs.w[SP]+=count;
	i86_ICount-=13;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_retf(void)    /* Opcode 0xcb */
{
	POP(I.ip);
	POP(I.sregs[CS]);
	I.base[CS] = SegBase(CS);
	i86_ICount-=14;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_int3(void)    /* Opcode 0xcc */
{
	i86_ICount-=16;
	i86_interrupt(3);
}

static void i_int(void)    /* Opcode 0xcd */
{
	unsigned int_num = FETCHOP;
	i86_ICount-=15;
	i86_interrupt(int_num);
}

static void i_into(void)    /* Opcode 0xce */
{
    if (OF) {
		i86_ICount-=17;
		i86_interrupt(4);
	} else i86_ICount-=4;
}

static void i_iret(void)    /* Opcode 0xcf */
{
	i86_ICount-=12;
	POP(I.ip);
	POP(I.sregs[CS]);
	I.base[CS] = SegBase(CS);
    i_popf();
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_rotshft_b(void)    /* Opcode 0xd0 */
{
	rotate_shift_Byte(FETCHOP,1);
}


static void i_rotshft_w(void)    /* Opcode 0xd1 */
{
	rotate_shift_Word(FETCHOP,1);
}


static void i_rotshft_bcl(void)    /* Opcode 0xd2 */
{
	rotate_shift_Byte(FETCHOP,I.regs.b[CL]);
}

static void i_rotshft_wcl(void)    /* Opcode 0xd3 */
{
	rotate_shift_Word(FETCHOP,I.regs.b[CL]);
}

static void i_aam(void)    /* Opcode 0xd4 */
{
	unsigned mult = FETCHOP;

	i86_ICount-=83;
    if (mult == 0)
		i86_interrupt(0);
    else
    {
		I.regs.b[AH] = I.regs.b[AL] / mult;
		I.regs.b[AL] %= mult;

		SetSZPF_Word(I.regs.w[AX]);
    }
}


static void i_aad(void)    /* Opcode 0xd5 */
{
	unsigned mult = FETCHOP;

	i86_ICount-=60;
	I.regs.b[AL] = I.regs.b[AH] * mult + I.regs.b[AL];
	I.regs.b[AH] = 0;

	SetZF(I.regs.b[AL]);
	SetPF(I.regs.b[AL]);
	I.SignVal = 0;
}

static void i_xlat(void)    /* Opcode 0xd7 */
{
	unsigned dest = I.regs.w[BX]+I.regs.b[AL];

	i86_ICount-=5;
	I.regs.b[AL] = GetMemB(DS, dest);
}

static void i_escape(void)    /* Opcodes 0xd8, 0xd9, 0xda, 0xdb, 0xdc, 0xdd, 0xde and 0xdf */
{
	unsigned ModRM = FETCHOP;
	i86_ICount-=2;
    GetRMByte(ModRM);
}

static void i_loopne(void)    /* Opcode 0xe0 */
{
	int disp = (int)((INT8)FETCHOP);
	unsigned tmp = I.regs.w[CX]-1;

	I.regs.w[CX]=tmp;

    if (!ZF && tmp) {
	i86_ICount-=19;
	I.ip = (WORD)(I.ip+disp);
	change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=5;
}

static void i_loope(void)    /* Opcode 0xe1 */
{
	int disp = (int)((INT8)FETCHOP);
	unsigned tmp = I.regs.w[CX]-1;

	I.regs.w[CX]=tmp;

    if (ZF && tmp) {
	i86_ICount-=18;
	I.ip = (WORD)(I.ip+disp);
	change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=6;
}

static void i_loop(void)    /* Opcode 0xe2 */
{
	int disp = (int)((INT8)FETCHOP);
	unsigned tmp = I.regs.w[CX]-1;

	I.regs.w[CX]=tmp;

    if (tmp) {
	i86_ICount-=17;
	I.ip = (WORD)(I.ip+disp);
	change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=5;
}

static void i_jcxz(void)    /* Opcode 0xe3 */
{
	int disp = (int)((INT8)FETCHOP);

	if (I.regs.w[CX] == 0) {
	i86_ICount-=18;
	I.ip = (WORD)(I.ip+disp);
	change_pc20((I.base[CS]+I.ip) & I.amask);
	} else i86_ICount-=6;
}

static void i_inal(void)    /* Opcode 0xe4 */
{
	unsigned port = FETCHOP;

	i86_ICount-=10;
	I.regs.b[AL] = read_port(port);
}

static void i_inax(void)    /* Opcode 0xe5 */
{
	unsigned port = FETCHOP;

	i86_ICount-=14;
	I.regs.b[AL] = read_port(port);
	I.regs.b[AH] = read_port(port+1);
}

static void i_outal(void)    /* Opcode 0xe6 */
{
	unsigned port = FETCHOP;

	i86_ICount-=10;
	write_port(port, I.regs.b[AL]);
}

static void i_outax(void)    /* Opcode 0xe7 */
{
	unsigned port = FETCHOP;

	i86_ICount-=14;
	write_port(port, I.regs.b[AL]);
	write_port(port+1, I.regs.b[AH]);
}

static void i_call_d16(void)    /* Opcode 0xe8 */
{
	unsigned tmp = FETCHOP;
	tmp += FETCHOP << 8;

	PUSH(I.ip);
	I.ip = (WORD)(I.ip+(INT16)tmp);
	i86_ICount-=12;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}


static void i_jmp_d16(void)    /* Opcode 0xe9 */
{
	int tmp = FETCHOP;
	tmp += FETCHOP << 8;

	I.ip = (WORD)(I.ip+(INT16)tmp);
	i86_ICount-=15;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_jmp_far(void)    /* Opcode 0xea */
{
    unsigned tmp,tmp1;

	tmp = FETCHOP;
	tmp += FETCHOP << 8;

	tmp1 = FETCHOP;
	tmp1 += FETCHOP << 8;

	I.sregs[CS] = (WORD)tmp1;
	I.base[CS] = SegBase(CS);
	I.ip = (WORD)tmp;
	i86_ICount-=15;
	change_pc20((I.base[CS]+I.ip) & I.amask);
}

static void i_jmp_d8(void)    /* Opcode 0xeb */
{
	int tmp = (int)((INT8)FETCHOP);
	I.ip = (WORD)(I.ip+tmp);
	i86_ICount-=15;
}

static void i_inaldx(void)    /* Opcode 0xec */
{
	i86_ICount-=8;
	I.regs.b[AL] = read_port(I.regs.w[DX]);
}

static void i_inaxdx(void)    /* Opcode 0xed */
{
	unsigned port = I.regs.w[DX];

	i86_ICount-=12;
	I.regs.b[AL] = read_port(port);
	I.regs.b[AH] = read_port(port+1);
}

static void i_outdxal(void)    /* Opcode 0xee */
{
	i86_ICount-=8;
	write_port(I.regs.w[DX], I.regs.b[AL]);
}

static void i_outdxax(void)    /* Opcode 0xef */
{
	unsigned port = I.regs.w[DX];

	i86_ICount-=12;
	write_port(port, I.regs.b[AL]);
	write_port(port+1, I.regs.b[AH]);
}

static void i_lock(void)    /* Opcode 0xf0 */
{
	i86_ICount-=2;
	instruction[FETCHOP]();  /* un-interruptible */
}

static void rep(int flagval)
{
    /* Handles rep- and repnz- prefixes. flagval is the value of ZF for the
       loop  to continue for CMPS and SCAS instructions. */

	unsigned next = FETCHOP;
	unsigned count = I.regs.w[CX];

    switch(next)
    {
    case 0x26:  /* ES: */
        seg_prefix=TRUE;
		prefix_base=I.base[ES];
		i86_ICount-=2;
		rep(flagval);
		break;
    case 0x2e:  /* CS: */
        seg_prefix=TRUE;
		prefix_base=I.base[CS];
		i86_ICount-=2;
		rep(flagval);
		break;
    case 0x36:  /* SS: */
        seg_prefix=TRUE;
		prefix_base=I.base[SS];
		i86_ICount-=2;
		rep(flagval);
		break;
    case 0x3e:  /* DS: */
        seg_prefix=TRUE;
		prefix_base=I.base[DS];
		i86_ICount-=2;
		rep(flagval);
		break;
    case 0x6c:  /* REP INSB */
		i86_ICount-=9-count;
		for (; count > 0; count--)
            i_insb();
		I.regs.w[CX]=count;
		break;
    case 0x6d:  /* REP INSW */
		i86_ICount-=9-count;
		for (; count > 0; count--)
            i_insw();
		I.regs.w[CX]=count;
		break;
    case 0x6e:  /* REP OUTSB */
		i86_ICount-=9-count;
		for (; count > 0; count--)
            i_outsb();
		I.regs.w[CX]=count;
		break;
    case 0x6f:  /* REP OUTSW */
		i86_ICount-=9-count;
		for (; count > 0; count--)
            i_outsw();
		I.regs.w[CX]=count;
		break;
    case 0xa4:  /* REP MOVSB */
		i86_ICount-=9-count;
		for (; count > 0; count--)
			i_movsb();
		I.regs.w[CX]=count;
		break;
    case 0xa5:  /* REP MOVSW */
		i86_ICount-=9-count;
		for (; count > 0; count--)
			i_movsw();
		I.regs.w[CX]=count;
		break;
    case 0xa6:  /* REP(N)E CMPSB */
		i86_ICount-=9;
		for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
			i_cmpsb();
		I.regs.w[CX]=count;
		break;
    case 0xa7:  /* REP(N)E CMPSW */
		i86_ICount-=9;
		for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
			i_cmpsw();
		I.regs.w[CX]=count;
		break;
    case 0xaa:  /* REP STOSB */
		i86_ICount-=9-count;
		for (; count > 0; count--)
			i_stosb();
		I.regs.w[CX]=count;
		break;
    case 0xab:  /* REP STOSW */
		i86_ICount-=9-count;
		for (; count > 0; count--)
			i_stosw();
		I.regs.w[CX]=count;
		break;
    case 0xac:  /* REP LODSB */
		i86_ICount-=9;
		for (; count > 0; count--)
			i_lodsb();
		I.regs.w[CX]=count;
		break;
    case 0xad:  /* REP LODSW */
		i86_ICount-=9;
		for (; count > 0; count--)
			i_lodsw();
		I.regs.w[CX]=count;
		break;
    case 0xae:  /* REP(N)E SCASB */
		i86_ICount-=9;
		for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
			i_scasb();
		I.regs.w[CX]=count;
		break;
    case 0xaf:  /* REP(N)E SCASW */
		i86_ICount-=9;
		for (I.ZeroVal = !flagval; (ZF == flagval) && (count > 0); count--)
			i_scasw();
		I.regs.w[CX]=count;
		break;
    default:
		instruction[next]();
    }
}

static void i_repne(void)    /* Opcode 0xf2 */
{
    rep(0);
}

static void i_repe(void)    /* Opcode 0xf3 */
{
    rep(1);
}

static void i_hlt(void)    /* Opcode 0xf4 */
{
	i86_ICount=0;
}

static void i_cmc(void)    /* Opcode 0xf5 */
{
	i86_ICount-=2;
	I.CarryVal = !CF;
}

static void i_f6pre(void)
{
	/* Opcode 0xf6 */
	unsigned ModRM = FETCHOP;
    unsigned tmp = (unsigned)GetRMByte(ModRM);
    unsigned tmp2;


    switch (ModRM & 0x38)
    {
    case 0x00:  /* TEST Eb, data8 */
    case 0x08:  /* ??? */
		i86_ICount-=5;
		tmp &= FETCH;

		I.CarryVal = I.OverVal = I.AuxVal = 0;
		SetSZPF_Byte(tmp);
		break;

    case 0x10:  /* NOT Eb */
		i86_ICount-=3;
		PutbackRMByte(ModRM,~tmp);
		break;

    case 0x18:  /* NEG Eb */
		i86_ICount-=3;
        tmp2=0;
        SUBB(tmp2,tmp);
        PutbackRMByte(ModRM,tmp2);
		break;
    case 0x20:  /* MUL AL, Eb */
		i86_ICount-=77;
		{
			UINT16 result;
			tmp2 = I.regs.b[AL];

			SetSF((INT8)tmp2);
			SetPF(tmp2);

			result = (UINT16)tmp2*tmp;
			I.regs.w[AX]=(WORD)result;

			SetZF(I.regs.w[AX]);
			I.CarryVal = I.OverVal = (I.regs.b[AH] != 0);
		}
		break;
    case 0x28:  /* IMUL AL, Eb */
		i86_ICount-=80;
		{
			INT16 result;

			tmp2 = (unsigned)I.regs.b[AL];

			SetSF((INT8)tmp2);
			SetPF(tmp2);

			result = (INT16)((INT8)tmp2)*(INT16)((INT8)tmp);
			I.regs.w[AX]=(WORD)result;

			SetZF(I.regs.w[AX]);

			I.CarryVal = I.OverVal = (result >> 7 != 0) && (result >> 7 != -1);
		}
		break;
    case 0x30:  /* DIV AL, Ew */
		i86_ICount-=90;
		{
			UINT16 result;

			result = I.regs.w[AX];

			if (tmp)
			{
				if ((result / tmp) > 0xff)
				{
					i86_interrupt(0);
					break;
				}
				else
				{
					I.regs.b[AH] = result % tmp;
					I.regs.b[AL] = result / tmp;
				}
			}
			else
			{
				i86_interrupt(0);
				break;
			}
		}
		break;
    case 0x38:  /* IDIV AL, Ew */
		i86_ICount-=106;
		{

			INT16 result;

			result = I.regs.w[AX];

			if (tmp)
			{
				tmp2 = result % (INT16)((INT8)tmp);

				if ((result /= (INT16)((INT8)tmp)) > 0xff)
				{
					i86_interrupt(0);
					break;
				}
				else
				{
					I.regs.b[AL] = result;
					I.regs.b[AH] = tmp2;
				}
			}
			else
			{
				i86_interrupt(0);
				break;
			}
		}
		break;
    }
}


static void i_f7pre(void)
{
	/* Opcode 0xf7 */
	unsigned ModRM = FETCHOP;
    unsigned tmp = GetRMWord(ModRM);
    unsigned tmp2;


    switch (ModRM & 0x38)
    {
    case 0x00:  /* TEST Ew, data16 */
    case 0x08:  /* ??? */
		i86_ICount-=3;
		tmp2 = FETCH;
		tmp2 += FETCH << 8;

		tmp &= tmp2;

		I.CarryVal = I.OverVal = I.AuxVal = 0;
		SetSZPF_Word(tmp);
		break;

    case 0x10:  /* NOT Ew */
		i86_ICount-=3;
		tmp = ~tmp;
		PutbackRMWord(ModRM,tmp);
		break;

    case 0x18:  /* NEG Ew */
		i86_ICount-=3;
        tmp2 = 0;
        SUBW(tmp2,tmp);
        PutbackRMWord(ModRM,tmp2);
		break;
    case 0x20:  /* MUL AX, Ew */
		i86_ICount-=129;
		{
			UINT32 result;
			tmp2 = I.regs.w[AX];

			SetSF((INT16)tmp2);
			SetPF(tmp2);

			result = (UINT32)tmp2*tmp;
			I.regs.w[AX]=(WORD)result;
            result >>= 16;
			I.regs.w[DX]=result;

			SetZF(I.regs.w[AX] | I.regs.w[DX]);
			I.CarryVal = I.OverVal = (I.regs.w[DX] != 0);
		}
		break;

    case 0x28:  /* IMUL AX, Ew */
		i86_ICount-=150;
		{
			INT32 result;

			tmp2 = I.regs.w[AX];

			SetSF((INT16)tmp2);
			SetPF(tmp2);

			result = (INT32)((INT16)tmp2)*(INT32)((INT16)tmp);
			I.CarryVal = I.OverVal = (result >> 15 != 0) && (result >> 15 != -1);

			I.regs.w[AX]=(WORD)result;
			result = (WORD)(result >> 16);
			I.regs.w[DX]=result;

			SetZF(I.regs.w[AX] | I.regs.w[DX]);
		}
		break;
    case 0x30:  /* DIV AX, Ew */
		i86_ICount-=158;
		{
			UINT32 result;

			result = (I.regs.w[DX] << 16) + I.regs.w[AX];

			if (tmp)
			{
				tmp2 = result % tmp;
				if ((result / tmp) > 0xffff)
				{
					i86_interrupt(0);
					break;
				}
				else
				{
					I.regs.w[DX]=tmp2;
					result /= tmp;
					I.regs.w[AX]=result;
				}
			}
			else
			{
				i86_interrupt(0);
				break;
			}
		}
		break;
    case 0x38:  /* IDIV AX, Ew */
		i86_ICount-=180;
		{
			INT32 result;

			result = (I.regs.w[DX] << 16) + I.regs.w[AX];

			if (tmp)
			{
				tmp2 = result % (INT32)((INT16)tmp);
				if ((result /= (INT32)((INT16)tmp)) > 0xffff)
				{
					i86_interrupt(0);
					break;
				}
				else
				{
					I.regs.w[AX]=result;
					I.regs.w[DX]=tmp2;
				}
			}
			else
			{
				i86_interrupt(0);
				break;
			}
		}
		break;
    }
}


static void i_clc(void)    /* Opcode 0xf8 */
{
	i86_ICount-=2;
	I.CarryVal = 0;
}

static void i_stc(void)    /* Opcode 0xf9 */
{
	i86_ICount-=2;
	I.CarryVal = 1;
}

static void i_cli(void)    /* Opcode 0xfa */
{
	i86_ICount-=2;
	I.IF = 0;
}

static void i_sti(void)    /* Opcode 0xfb */
{
	i86_ICount-=2;
	I.IF = 1;
}

static void i_cld(void)    /* Opcode 0xfc */
{
	i86_ICount-=2;
	I.DF = 0;
}

static void i_std(void)    /* Opcode 0xfd */
{
	i86_ICount-=2;
	I.DF = 1;
}

static void i_fepre(void)    /* Opcode 0xfe */
{
	unsigned ModRM = FETCHOP;
    unsigned tmp = GetRMByte(ModRM);
    unsigned tmp1;

	i86_ICount-=3; /* 2 if dest is in memory */
    if ((ModRM & 0x38) == 0)  /* INC eb */
    {
		tmp1 = tmp+1;
		SetOFB_Add(tmp1,tmp,1);
    }
    else  /* DEC eb */
    {
		tmp1 = tmp-1;
		SetOFB_Sub(tmp1,1,tmp);
    }

    SetAF(tmp1,tmp,1);
    SetSZPF_Byte(tmp1);

    PutbackRMByte(ModRM,(BYTE)tmp1);
}


static void i_ffpre(void)    /* Opcode 0xff */
{
	unsigned ModRM = FETCHOP;
    unsigned tmp;
    unsigned tmp1;

    switch(ModRM & 0x38)
    {
    case 0x00:  /* INC ew */
		i86_ICount-=3; /* 2 if dest is in memory */
		tmp = GetRMWord(ModRM);
		tmp1 = tmp+1;

		SetOFW_Add(tmp1,tmp,1);
		SetAF(tmp1,tmp,1);
		SetSZPF_Word(tmp1);

		PutbackRMWord(ModRM,(WORD)tmp1);
		break;

    case 0x08:  /* DEC ew */
		i86_ICount-=3; /* 2 if dest is in memory */
		tmp = GetRMWord(ModRM);
		tmp1 = tmp-1;

		SetOFW_Sub(tmp1,1,tmp);
		SetAF(tmp1,tmp,1);
		SetSZPF_Word(tmp1);

		PutbackRMWord(ModRM,(WORD)tmp1);
		break;

    case 0x10:  /* CALL ew */
		i86_ICount-=9; /* 8 if dest is in memory */
		tmp = GetRMWord(ModRM);
		PUSH(I.ip);
		I.ip = (WORD)tmp;
		change_pc20((I.base[CS]+I.ip) & I.amask);
		break;

	case 0x18:  /* CALL FAR ea */
		i86_ICount-=11;
		tmp = I.sregs[CS];	/* HJB 12/13/98 need to skip displacements of EA */
		tmp1 = GetRMWord(ModRM);
		I.sregs[CS] = GetnextRMWord;
		I.base[CS] = SegBase(CS);
		PUSH(tmp);
		PUSH(I.ip);
		I.ip = tmp1;
		change_pc20((I.base[CS]+I.ip) & I.amask);
		break;

    case 0x20:  /* JMP ea */
		i86_ICount-=11; /* 8 if address in memory */
		I.ip = GetRMWord(ModRM);
		change_pc20((I.base[CS]+I.ip) & I.amask);
		break;

    case 0x28:  /* JMP FAR ea */
		i86_ICount-=4;
		I.ip = GetRMWord(ModRM);
		I.sregs[CS] = GetnextRMWord;
		I.base[CS] = SegBase(CS);
		change_pc20((I.base[CS]+I.ip) & I.amask);
		break;

    case 0x30:  /* PUSH ea */
		i86_ICount-=3;
		tmp = GetRMWord(ModRM);
		PUSH(tmp);
		break;
    }
}


static void i_invalid(void)
{
    /* makes the cpu loops forever until user resets it */
/*	{ extern int debug_key_pressed; debug_key_pressed = 1; } */
	I.ip--;
	i86_ICount-=10;
}

/* ASG 971222 -- added these interface functions */

unsigned i86_get_context(void *dst)
{
	if( dst )
		*(i86_Regs*)dst = I;
    return sizeof(i86_Regs);
}

void i86_set_context(void *src)
{
	if( src )
	{
		I = *(i86_Regs*)src;
		I.base[CS] = SegBase(CS);
		I.base[DS] = SegBase(DS);
		I.base[ES] = SegBase(ES);
		I.base[SS] = SegBase(SS);
		change_pc20((I.base[CS]+I.ip) & I.amask);
	}
}

unsigned i86_get_pc(void)
{
	return (I.base[CS] + (WORD)I.ip) & I.amask;
}

void i86_set_pc(unsigned val)
{
	if( val - I.base[CS] < 0x10000 )
	{
		I.ip = val - I.base[CS];
	}
	else
	{
		I.base[CS] = val & 0xffff0;
		I.sregs[CS] = I.base[CS] >> 4;
		I.ip = val & 0x0000f;
	}
}

unsigned i86_get_sp(void)
{
	return I.base[SS] + I.regs.w[SP];
}

void i86_set_sp(unsigned val)
{
	if( val - I.base[SS] < 0x10000 )
	{
		I.regs.w[SP] = val - I.base[SS];
	}
	else
	{
		I.base[SS] = val & 0xffff0;
		I.sregs[SS] = I.base[SS] >> 4;
		I.regs.w[SP] = val & 0x0000f;
	}
}

unsigned i86_get_reg(int regnum)
{
	switch( regnum )
	{
		case I86_IP: return I.ip;
		case I86_SP: return I.regs.w[SP];
		case I86_FLAGS: CompressFlags(); return I.flags;
        case I86_AX: return I.regs.w[AX];
		case I86_CX: return I.regs.w[CX];
		case I86_DX: return I.regs.w[DX];
		case I86_BX: return I.regs.w[BX];
		case I86_BP: return I.regs.w[BP];
		case I86_SI: return I.regs.w[SI];
		case I86_DI: return I.regs.w[DI];
		case I86_ES: return I.sregs[ES];
		case I86_CS: return I.sregs[CS];
		case I86_SS: return I.sregs[SS];
		case I86_DS: return I.sregs[DS];
		case I86_VECTOR: return I.int_vector;
		case I86_PENDING: return I.pending_irq;
		case I86_NMI_STATE: return I.nmi_state;
		case I86_IRQ_STATE: return I.irq_state;
		case REG_PREVIOUSPC: return 0;	/* not supported */
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = ((I.base[SS] + I.regs.w[SP]) & I.amask) + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < I.amask )
					return cpu_readmem20( offset ) | ( cpu_readmem20( offset + 1) << 8 );
			}
	}
	return 0;
}

void i86_set_reg(int regnum, unsigned val)
{
	switch( regnum )
	{
		case I86_IP: I.ip = val; break;
		case I86_SP: I.regs.w[SP] = val; break;
		case I86_FLAGS: I.flags = val; ExpandFlags(val); break;
        case I86_AX: I.regs.w[AX] = val; break;
		case I86_CX: I.regs.w[CX] = val; break;
		case I86_DX: I.regs.w[DX] = val; break;
		case I86_BX: I.regs.w[BX] = val; break;
		case I86_BP: I.regs.w[BP] = val; break;
		case I86_SI: I.regs.w[SI] = val; break;
		case I86_DI: I.regs.w[DI] = val; break;
		case I86_ES: I.sregs[ES] = val; break;
		case I86_CS: I.sregs[CS] = val; break;
		case I86_SS: I.sregs[SS] = val; break;
		case I86_DS: I.sregs[DS] = val; break;
		case I86_VECTOR: I.int_vector = val; break;
		case I86_PENDING: I.pending_irq = val; break;
		case I86_NMI_STATE: i86_set_nmi_line(val); break;
		case I86_IRQ_STATE: i86_set_irq_line(0,val); break;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = ((I.base[SS] + I.regs.w[SP]) & I.amask) + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < I.amask - 1 )
				{
					cpu_writemem20( offset, val & 0xff );
					cpu_writemem20( offset+1, (val >> 8) & 0xff );
				}
			}
    }
}

void i86_set_nmi_line(int state)
{
	if( I.nmi_state == state ) return;
    I.nmi_state = state;
	if (state != CLEAR_LINE)
	{
		I.pending_irq |= NMI_IRQ;
	}
}

void i86_set_irq_line(int irqline, int state)
{
	I.irq_state = state;
	if (state == CLEAR_LINE)
	{
		if (!I.IF)
			I.pending_irq &= ~INT_IRQ;
	}
	else
	{
		if (I.IF)
			I.pending_irq |= INT_IRQ;
	}
}

void i86_set_irq_callback(int (*callback)(int))
{
	I.irq_callback = callback;
}

int i86_execute(int cycles)
{
	i86_ICount=cycles;	/* ASG 971222 cycles_per_run;*/
	while(i86_ICount>0)
    {

#ifdef VERBOSE_DEBUG
printf("[%04x:%04x]=%02x\tAX=%04x\tBX=%04x\tCX=%04x\tDX=%04x\n",sregs[CS],I.ip,GetMemB(CS,I.ip),I.regs.w[AX],I.regs.w[BX],I.regs.w[CX],I.regs.w[DX]);
#endif

	if ((I.pending_irq && I.IF) || (I.pending_irq & NMI_IRQ))
		external_int(); 	 /* HJB 12/15/98 */

	CALL_MAME_DEBUG;

	seg_prefix=FALSE;
#if defined(BIGCASE) && !defined(RS6000)
  /* Some compilers cannot handle large case statements */
	switch(FETCHOP)
	{
	case 0x00:    i_add_br8(); break;
	case 0x01:    i_add_wr16(); break;
	case 0x02:    i_add_r8b(); break;
	case 0x03:    i_add_r16w(); break;
	case 0x04:    i_add_ald8(); break;
	case 0x05:    i_add_axd16(); break;
	case 0x06:    i_push_es(); break;
	case 0x07:    i_pop_es(); break;
	case 0x08:    i_or_br8(); break;
	case 0x09:    i_or_wr16(); break;
	case 0x0a:    i_or_r8b(); break;
	case 0x0b:    i_or_r16w(); break;
	case 0x0c:    i_or_ald8(); break;
	case 0x0d:    i_or_axd16(); break;
	case 0x0e:    i_push_cs(); break;
	case 0x0f:    i_invalid(); break;
	case 0x10:    i_adc_br8(); break;
	case 0x11:    i_adc_wr16(); break;
	case 0x12:    i_adc_r8b(); break;
	case 0x13:    i_adc_r16w(); break;
	case 0x14:    i_adc_ald8(); break;
	case 0x15:    i_adc_axd16(); break;
	case 0x16:    i_push_ss(); break;
	case 0x17:    i_pop_ss(); break;
	case 0x18:    i_sbb_br8(); break;
	case 0x19:    i_sbb_wr16(); break;
	case 0x1a:    i_sbb_r8b(); break;
	case 0x1b:    i_sbb_r16w(); break;
	case 0x1c:    i_sbb_ald8(); break;
	case 0x1d:    i_sbb_axd16(); break;
	case 0x1e:    i_push_ds(); break;
	case 0x1f:    i_pop_ds(); break;
	case 0x20:    i_and_br8(); break;
	case 0x21:    i_and_wr16(); break;
	case 0x22:    i_and_r8b(); break;
	case 0x23:    i_and_r16w(); break;
	case 0x24:    i_and_ald8(); break;
	case 0x25:    i_and_axd16(); break;
	case 0x26:    i_es(); break;
	case 0x27:    i_daa(); break;
	case 0x28:    i_sub_br8(); break;
	case 0x29:    i_sub_wr16(); break;
	case 0x2a:    i_sub_r8b(); break;
	case 0x2b:    i_sub_r16w(); break;
	case 0x2c:    i_sub_ald8(); break;
	case 0x2d:    i_sub_axd16(); break;
	case 0x2e:    i_cs(); break;
	case 0x2f:    i_das(); break;
	case 0x30:    i_xor_br8(); break;
	case 0x31:    i_xor_wr16(); break;
	case 0x32:    i_xor_r8b(); break;
	case 0x33:    i_xor_r16w(); break;
	case 0x34:    i_xor_ald8(); break;
	case 0x35:    i_xor_axd16(); break;
	case 0x36:    i_ss(); break;
	case 0x37:    i_aaa(); break;
	case 0x38:    i_cmp_br8(); break;
	case 0x39:    i_cmp_wr16(); break;
	case 0x3a:    i_cmp_r8b(); break;
	case 0x3b:    i_cmp_r16w(); break;
	case 0x3c:    i_cmp_ald8(); break;
	case 0x3d:    i_cmp_axd16(); break;
	case 0x3e:    i_ds(); break;
	case 0x3f:    i_aas(); break;
	case 0x40:    i_inc_ax(); break;
	case 0x41:    i_inc_cx(); break;
	case 0x42:    i_inc_dx(); break;
	case 0x43:    i_inc_bx(); break;
	case 0x44:    i_inc_sp(); break;
	case 0x45:    i_inc_bp(); break;
	case 0x46:    i_inc_si(); break;
	case 0x47:    i_inc_di(); break;
	case 0x48:    i_dec_ax(); break;
	case 0x49:    i_dec_cx(); break;
	case 0x4a:    i_dec_dx(); break;
	case 0x4b:    i_dec_bx(); break;
	case 0x4c:    i_dec_sp(); break;
	case 0x4d:    i_dec_bp(); break;
	case 0x4e:    i_dec_si(); break;
	case 0x4f:    i_dec_di(); break;
	case 0x50:    i_push_ax(); break;
	case 0x51:    i_push_cx(); break;
	case 0x52:    i_push_dx(); break;
	case 0x53:    i_push_bx(); break;
	case 0x54:    i_push_sp(); break;
	case 0x55:    i_push_bp(); break;
	case 0x56:    i_push_si(); break;
	case 0x57:    i_push_di(); break;
	case 0x58:    i_pop_ax(); break;
	case 0x59:    i_pop_cx(); break;
	case 0x5a:    i_pop_dx(); break;
	case 0x5b:    i_pop_bx(); break;
	case 0x5c:    i_pop_sp(); break;
	case 0x5d:    i_pop_bp(); break;
	case 0x5e:    i_pop_si(); break;
	case 0x5f:    i_pop_di(); break;
        case 0x60:    i_pusha(); break;
        case 0x61:    i_popa(); break;
        case 0x62:    i_bound(); break;
	case 0x63:    i_invalid(); break;
	case 0x64:    i_invalid(); break;
	case 0x65:	  i_invalid(); break;
	case 0x66:    i_invalid(); break;
	case 0x67:    i_invalid(); break;
        case 0x68:    i_push_d16(); break;
        case 0x69:    i_imul_d16(); break;
        case 0x6a:    i_push_d8(); break;
        case 0x6b:    i_imul_d8(); break;
        case 0x6c:    i_insb(); break;
        case 0x6d:    i_insw(); break;
        case 0x6e:    i_outsb(); break;
        case 0x6f:    i_outsw(); break;
	case 0x70:    i_jo(); break;
	case 0x71:    i_jno(); break;
	case 0x72:    i_jb(); break;
	case 0x73:    i_jnb(); break;
	case 0x74:    i_jz(); break;
	case 0x75:    i_jnz(); break;
	case 0x76:    i_jbe(); break;
	case 0x77:    i_jnbe(); break;
	case 0x78:    i_js(); break;
	case 0x79:    i_jns(); break;
	case 0x7a:    i_jp(); break;
	case 0x7b:    i_jnp(); break;
	case 0x7c:    i_jl(); break;
	case 0x7d:    i_jnl(); break;
	case 0x7e:    i_jle(); break;
	case 0x7f:    i_jnle(); break;
	case 0x80:    i_80pre(); break;
	case 0x81:    i_81pre(); break;
	case 0x82:	  i_82pre(); break;
	case 0x83:    i_83pre(); break;
	case 0x84:    i_test_br8(); break;
	case 0x85:    i_test_wr16(); break;
	case 0x86:    i_xchg_br8(); break;
	case 0x87:    i_xchg_wr16(); break;
	case 0x88:    i_mov_br8(); break;
	case 0x89:    i_mov_wr16(); break;
	case 0x8a:    i_mov_r8b(); break;
	case 0x8b:    i_mov_r16w(); break;
	case 0x8c:    i_mov_wsreg(); break;
	case 0x8d:    i_lea(); break;
	case 0x8e:    i_mov_sregw(); break;
	case 0x8f:    i_popw(); break;
	case 0x90:    i_nop(); break;
	case 0x91:    i_xchg_axcx(); break;
	case 0x92:    i_xchg_axdx(); break;
	case 0x93:    i_xchg_axbx(); break;
	case 0x94:    i_xchg_axsp(); break;
	case 0x95:    i_xchg_axbp(); break;
	case 0x96:    i_xchg_axsi(); break;
	case 0x97:    i_xchg_axdi(); break;
	case 0x98:    i_cbw(); break;
	case 0x99:    i_cwd(); break;
	case 0x9a:    i_call_far(); break;
	case 0x9b:    i_wait(); break;
	case 0x9c:    i_pushf(); break;
	case 0x9d:    i_popf(); break;
	case 0x9e:    i_sahf(); break;
	case 0x9f:    i_lahf(); break;
	case 0xa0:    i_mov_aldisp(); break;
	case 0xa1:    i_mov_axdisp(); break;
	case 0xa2:    i_mov_dispal(); break;
	case 0xa3:    i_mov_dispax(); break;
	case 0xa4:    i_movsb(); break;
	case 0xa5:    i_movsw(); break;
	case 0xa6:    i_cmpsb(); break;
	case 0xa7:    i_cmpsw(); break;
	case 0xa8:    i_test_ald8(); break;
	case 0xa9:    i_test_axd16(); break;
	case 0xaa:    i_stosb(); break;
	case 0xab:    i_stosw(); break;
	case 0xac:    i_lodsb(); break;
	case 0xad:    i_lodsw(); break;
	case 0xae:    i_scasb(); break;
	case 0xaf:    i_scasw(); break;
	case 0xb0:    i_mov_ald8(); break;
	case 0xb1:    i_mov_cld8(); break;
	case 0xb2:    i_mov_dld8(); break;
	case 0xb3:    i_mov_bld8(); break;
	case 0xb4:    i_mov_ahd8(); break;
	case 0xb5:    i_mov_chd8(); break;
	case 0xb6:    i_mov_dhd8(); break;
	case 0xb7:    i_mov_bhd8(); break;
	case 0xb8:    i_mov_axd16(); break;
	case 0xb9:    i_mov_cxd16(); break;
	case 0xba:    i_mov_dxd16(); break;
	case 0xbb:    i_mov_bxd16(); break;
	case 0xbc:    i_mov_spd16(); break;
	case 0xbd:    i_mov_bpd16(); break;
	case 0xbe:    i_mov_sid16(); break;
	case 0xbf:    i_mov_did16(); break;
        case 0xc0:    i_rotshft_bd8(); break;
        case 0xc1:    i_rotshft_wd8(); break;
	case 0xc2:    i_ret_d16(); break;
	case 0xc3:    i_ret(); break;
	case 0xc4:    i_les_dw(); break;
	case 0xc5:    i_lds_dw(); break;
	case 0xc6:    i_mov_bd8(); break;
	case 0xc7:    i_mov_wd16(); break;
        case 0xc8:    i_enter(); break;
        case 0xc9:    i_leave(); break;
	case 0xca:    i_retf_d16(); break;
	case 0xcb:    i_retf(); break;
	case 0xcc:    i_int3(); break;
	case 0xcd:    i_int(); break;
	case 0xce:    i_into(); break;
	case 0xcf:    i_iret(); break;
        case 0xd0:    i_rotshft_b(); break;
        case 0xd1:    i_rotshft_w(); break;
        case 0xd2:    i_rotshft_bcl(); break;
        case 0xd3:    i_rotshft_wcl(); break;
	case 0xd4:    i_aam(); break;
	case 0xd5:    i_aad(); break;
	case 0xd6:    i_invalid(); break;
	case 0xd7:    i_xlat(); break;
	case 0xd8:    i_escape(); break;
	case 0xd9:    i_escape(); break;
	case 0xda:    i_escape(); break;
	case 0xdb:    i_escape(); break;
	case 0xdc:    i_escape(); break;
	case 0xdd:    i_escape(); break;
	case 0xde:    i_escape(); break;
	case 0xdf:    i_escape(); break;
	case 0xe0:    i_loopne(); break;
	case 0xe1:    i_loope(); break;
	case 0xe2:    i_loop(); break;
	case 0xe3:    i_jcxz(); break;
	case 0xe4:    i_inal(); break;
	case 0xe5:    i_inax(); break;
	case 0xe6:    i_outal(); break;
	case 0xe7:    i_outax(); break;
	case 0xe8:    i_call_d16(); break;
	case 0xe9:    i_jmp_d16(); break;
	case 0xea:    i_jmp_far(); break;
	case 0xeb:    i_jmp_d8(); break;
	case 0xec:    i_inaldx(); break;
	case 0xed:    i_inaxdx(); break;
	case 0xee:    i_outdxal(); break;
	case 0xef:    i_outdxax(); break;
	case 0xf0:    i_lock(); break;
	case 0xf1:    i_invalid(); break;
	case 0xf2:    i_repne(); break;
	case 0xf3:    i_repe(); break;
	case 0xf4:    i_hlt(); break;
	case 0xf5:    i_cmc(); break;
	case 0xf6:    i_f6pre(); break;
	case 0xf7:    i_f7pre(); break;
	case 0xf8:    i_clc(); break;
	case 0xf9:    i_stc(); break;
	case 0xfa:    i_cli(); break;
	case 0xfb:    i_sti(); break;
	case 0xfc:    i_cld(); break;
	case 0xfd:    i_std(); break;
	case 0xfe:    i_fepre(); break;
	case 0xff:    i_ffpre(); break;
	};
#else
	instruction[FETCHOP]();
#endif

    }
	return cycles - i86_ICount;
}

/****************************************************************************
 * Return a formatted string for a register
 ****************************************************************************/
const char *i86_info(void *context, int regnum)
{
	static char buffer[32][63+1];
	static int which = 0;
	i86_Regs *r = context;

	which = ++which % 32;
	buffer[which][0] = '\0';
	if( !context )
		r = &I;

	switch( regnum )
	{
		case CPU_INFO_REG+I86_IP: sprintf(buffer[which], "IP:%04X", r->ip); break;
		case CPU_INFO_REG+I86_SP: sprintf(buffer[which], "SP:%04X", r->regs.w[SP]); break;
		case CPU_INFO_REG+I86_FLAGS: sprintf(buffer[which], "F:%04X", r->flags); break;
		case CPU_INFO_REG+I86_AX: sprintf(buffer[which], "AX:%04X", r->regs.w[AX]); break;
		case CPU_INFO_REG+I86_CX: sprintf(buffer[which], "CX:%04X", r->regs.w[CX]); break;
		case CPU_INFO_REG+I86_DX: sprintf(buffer[which], "DX:%04X", r->regs.w[DX]); break;
		case CPU_INFO_REG+I86_BX: sprintf(buffer[which], "BX:%04X", r->regs.w[BX]); break;
		case CPU_INFO_REG+I86_BP: sprintf(buffer[which], "BP:%04X", r->regs.w[BP]); break;
		case CPU_INFO_REG+I86_SI: sprintf(buffer[which], "SI:%04X", r->regs.w[SI]); break;
		case CPU_INFO_REG+I86_DI: sprintf(buffer[which], "DI:%04X", r->regs.w[DI]); break;
        case CPU_INFO_REG+I86_ES: sprintf(buffer[which], "ES:%04X", r->sregs[ES]); break;
        case CPU_INFO_REG+I86_CS: sprintf(buffer[which], "CS:%04X", r->sregs[CS]); break;
        case CPU_INFO_REG+I86_SS: sprintf(buffer[which], "SS:%04X", r->sregs[SS]); break;
        case CPU_INFO_REG+I86_DS: sprintf(buffer[which], "DS:%04X", r->sregs[DS]); break;
        case CPU_INFO_REG+I86_VECTOR: sprintf(buffer[which], "V:%02X", r->int_vector); break;
		case CPU_INFO_REG+I86_PENDING: sprintf(buffer[which], "P:%X", r->pending_irq); break;
		case CPU_INFO_REG+I86_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
		case CPU_INFO_REG+I86_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
		case CPU_INFO_FLAGS:
			r->flags = CompressFlags();
			sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
				r->flags & 0x8000 ? '?':'.',
				r->flags & 0x4000 ? '?':'.',
				r->flags & 0x2000 ? '?':'.',
				r->flags & 0x1000 ? '?':'.',
				r->flags & 0x0800 ? 'O':'.',
				r->flags & 0x0400 ? 'D':'.',
				r->flags & 0x0200 ? 'I':'.',
				r->flags & 0x0100 ? 'T':'.',
				r->flags & 0x0080 ? 'S':'.',
				r->flags & 0x0040 ? 'Z':'.',
				r->flags & 0x0020 ? '?':'.',
				r->flags & 0x0010 ? 'A':'.',
				r->flags & 0x0008 ? '?':'.',
				r->flags & 0x0004 ? 'P':'.',
				r->flags & 0x0002 ? 'N':'.',
				r->flags & 0x0001 ? 'C':'.');
			break;
		case CPU_INFO_NAME: return "I86";
		case CPU_INFO_FAMILY: return "Intel 80x86";
		case CPU_INFO_VERSION: return "1.4";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Real mode i286 emulator v1.4 by Fabrice Frances\n(initial work I.based on David Hedley's pcemu)";
		case CPU_INFO_REG_LAYOUT: return (const char*)i86_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)i86_win_layout;
	}
	return buffer[which];
}

unsigned i86_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
    return DasmI86(buffer,pc);
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}
