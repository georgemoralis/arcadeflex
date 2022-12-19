/*** hd6309: Portable 6309 emulator ******************************************

	Copyright (C) John Butler 1997
	Copyright (C) Tim Lindner 2000

	References:

		HD63B09EP Technical Refrence Guide, by Chet Simpson with addition
							by Alan Dekok
		6809 Simulator V09, By L.C. Benschop, Eidnhoven The Netherlands.

		m6809: Portable 6809 emulator, DS (6809 code in MAME, derived from
			the 6809 Simulator V09)

		6809 Microcomputer Programming & Interfacing with Experiments"
			by Andrew C. Staugaard, Jr.; Howard W. Sams & Co., Inc.

	System dependencies:	UINT16 must be 16 bit unsigned int
							UINT8 must be 8 bit unsigned int
							UINT32 must be more than 16 bits
							arrays up to 65536 bytes must be supported
							machine must be twos complement

	History:
991026 HJB:
	Fixed missing calls to cpu_changepc() for the TFR and EXG ocpodes.
	Replaced m6809_slapstic checks by a macro (CHANGE_PC). ESB still
	needs the tweaks.

991024 HJB:
	Tried to improve speed: Using bit7 of cycles1/2 as flag for multi
	byte opcodes is gone, those opcodes now call fetch_effective_address().
	Got rid of the slow/fast flags for stack (S and U) memory accesses.
	Minor changes to use 32 bit values as arguments to memory functions
	and added defines for that purpose (e.g. X = 16bit XD = 32bit).

990312 HJB:
	Added bugfixes according to Aaron's findings.
	Reset only sets CC_II and CC_IF, DP to zero and PC from reset vector.
990311 HJB:
	Added _info functions. Now uses static m6808_Regs struct instead
	of single statics. Changed the 16 bit registers to use the generic
	PAIR union. Registers defined using macros. Split the core into
	four execution loops for M6802, M6803, M6808 and HD63701.
	TST, TSTA and TSTB opcodes reset carry flag.
	Modified the read/write stack handlers to push LSB first then MSB
	and pull MSB first then LSB.

990228 HJB:
	Changed the interrupt handling again. Now interrupts are taken
	either right at the moment the lines are asserted or whenever
	an interrupt is enabled and the corresponding line is still
	asserted. That way the pending_interrupts checks are not
	needed anymore. However, the CWAI and SYNC flags still need
	some flags, so I changed the name to 'int_state'.
	This core also has the code for the old interrupt system removed.

990225 HJB:
	Cleaned up the code here and there, added some comments.
	Slightly changed the SAR opcodes (similiar to other CPU cores).
	Added symbolic names for the flag bits.
	Changed the way CWAI/Interrupt() handle CPU state saving.
	A new flag M6809_STATE in pending_interrupts is used to determine
	if a state save is needed on interrupt entry or already done by CWAI.
	Added M6809_IRQ_LINE and M6809_FIRQ_LINE defines to m6809.h
	Moved the internal interrupt_pending flags from m6809.h to m6809.c
	Changed CWAI cycles2[0x3c] to be 2 (plus all or at least 19 if
	CWAI actually pushes the entire state).
	Implemented undocumented TFR/EXG for undefined source and mixed 8/16
	bit transfers (they should transfer/exchange the constant $ff).
	Removed unused jmp/jsr _slap functions from 6809ops.c,
	m6809_slapstick check moved into the opcode functions.

00809 TJL:
	Started converting m6809 into hd6309

*****************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include "cpuintrf.h"
#include "state.h"
#include "mamedbg.h"
#include "hd6309.h"

#define VERBOSE 0

#if VERBOSE
#define LOG(x)	logerror x
#else
#define LOG(x)
#endif

#undef INLINE
#define INLINE

void CHECK_IRQ_LINES( void );

static UINT8 hd6309_reg_layout[] = {
	HD6309_PC, HD6309_S, HD6309_CC, HD6309_A, HD6309_B, HD6309_E, HD6309_F, HD6309_X, -1, HD6309_V, HD6309_MD,
	HD6309_Y, HD6309_U, HD6309_DP, HD6309_NMI_STATE, HD6309_IRQ_STATE, HD6309_FIRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 hd6309_win_layout[] = {
	27, 0,53, 4,	/* register window (top, right rows) */
	 0, 0,26,22,	/* disassembler window (left colums) */
	27, 5,53, 8,	/* memory #1 window (right, upper middle) */
	27,14,53, 8,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};

INLINE void fetch_effective_address( void );

/* 6309 Registers */
typedef struct
{
	PAIR	pc; 		/* Program counter */
	PAIR	ppc;		/* Previous program counter */
	PAIR	q;			/* Accumulator a, b, e, f (ab = d, ef = w, abef = q) */
	PAIR	dp; 		/* Direct Page register (page in MSB) */
	PAIR	u, s;		/* Stack pointers */
	PAIR	x, y;		/* Index registers */
	PAIR	v;			/* New 6309 register */
	UINT8	cc;
	UINT8	md; 		/* Special mode register */
	UINT8	ireg;		/* First opcode */
	UINT8	irq_state[2];
	int 	extra_cycles; /* cycles used up by interrupts */
	int 	(*irq_callback)(int irqline);
	UINT8	int_state;	/* SYNC and CWAI flags */
	UINT8	nmi_state;
} hd6309_Regs;

/* flag bits in the cc register */
#define CC_C	0x01		/* Carry */
#define CC_V	0x02		/* Overflow */
#define CC_Z	0x04		/* Zero */
#define CC_N	0x08		/* Negative */
#define CC_II	0x10		/* Inhibit IRQ */
#define CC_H	0x20		/* Half (auxiliary) carry */
#define CC_IF	0x40		/* Inhibit FIRQ */
#define CC_E	0x80		/* entire state pushed */

/* flag bits in the md register */
#define MD_EM	0x01		/* Execution mode */
#define MD_FM	0x02		/* FIRQ mode */
#define MD_II	0x40		/* Illegal instruction */
#define MD_DZ	0x80		/* Division by zero */

/* 6309 registers */
static hd6309_Regs hd6309;
int hd6309_slapstic = 0;

#define pPPC	hd6309.ppc
#define pPC 	hd6309.pc
#define pU		hd6309.u
#define pS		hd6309.s
#define pX		hd6309.x
#define pY		hd6309.y
#define pV		hd6309.v
#define pQ		hd6309.q

#define Q		hd6309.q.d

#define pD		hd6309.q		/* Not that these seem to be pointing to the same struct. They are. */
#define pW		hd6309.q		/* There are two different macros needed to access these registers	*/

#define PPC 	hd6309.ppc.w.l
#define PC		hd6309.pc.w.l
#define PCD 	hd6309.pc.d
#define U		hd6309.u.w.l
#define UD		hd6309.u.d
#define S		hd6309.s.w.l
#define SD		hd6309.s.d
#define X		hd6309.x.w.l
#define XD		hd6309.x.d
#define Y		hd6309.y.w.l
#define YD		hd6309.y.d
#define V		hd6309.v.w.l
#define VD		hd6309.v.d
#define D		hd6309.q.w.h
#define A		hd6309.q.b.h3
#define B		hd6309.q.b.h2
#define W		hd6309.q.w.l
#define E		hd6309.q.b.h
#define F		hd6309.q.b.l
#define DP		hd6309.dp.b.h
#define DPD 	hd6309.dp.d
#define CC		hd6309.cc
#define MD		hd6309.md

static PAIR ea; 		/* effective address */
#define EA	ea.w.l
#define EAD ea.d

#define CHANGE_PC change_pc16(PCD)
#if 0
#define CHANGE_PC	{			\
	if( hd6309_slapstic )		\
		cpu_setOPbase16(PCD);	\
	else						\
		change_pc16(PCD);		\
	}
#endif

#define HD6309_CWAI 	8	/* set when CWAI is waiting for an interrupt */
#define HD6309_SYNC 	16	/* set when SYNC is waiting for an interrupt */
#define HD6309_LDS		32	/* set when LDS occured at least once */

/* public globals */
int hd6309_ICount=50000;

/* these are re-defined in hd6309.h TO RAM, ROM or functions in cpuintrf.c */
#define RM(mAddr)		HD6309_RDMEM(mAddr)
#define WM(mAddr,Value) HD6309_WRMEM(mAddr,Value)
#define ROP(mAddr)		HD6309_RDOP(mAddr)
#define ROP_ARG(mAddr)	HD6309_RDOP_ARG(mAddr)

/* macros to access memory */
#define IMMBYTE(b)	b = ROP_ARG(PCD); PC++
#define IMMWORD(w)	w.d = (ROP_ARG(PCD)<<8) | ROP_ARG((PCD+1)&0xffff); PC+=2
#define IMMLONG(w)	w.d = (ROP_ARG(PCD)<<24) + (ROP_ARG(PCD+1)<<16) + (ROP_ARG(PCD+2)<<8) + (ROP_ARG(PCD+3)); PC+=4

#define PUSHBYTE(b) --S; WM(SD,b)
#define PUSHWORD(w) --S; WM(SD,w.b.l); --S; WM(SD,w.b.h)
#define PUSHWORD_D(w) --S; WM(SD,w.b.h2); --S; WM(SD,w.b.h3) /*special PUSHWORD for the d register */
#define PULLBYTE(b) b = RM(SD); S++
#define PULLWORD(w) w = RM(SD)<<8; S++; w |= RM(SD); S++

#define PSHUBYTE(b) --U; WM(UD,b);
#define PSHUWORD(w) --U; WM(UD,w.b.l); --U; WM(UD,w.b.h)
#define PSHUWORD_D(w) --U; WM(UD,w.b.h2); --U; WM(UD,w.b.h3) /*special PUSHWORD for the d register */
#define PULUBYTE(b) b = RM(UD); U++
#define PULUWORD(w) w = RM(UD)<<8; U++; w |= RM(UD); U++

#define CLR_HNZVC	CC&=~(CC_H|CC_N|CC_Z|CC_V|CC_C)
#define CLR_NZV 	CC&=~(CC_N|CC_Z|CC_V)
#define CLR_HNZC	CC&=~(CC_H|CC_N|CC_Z|CC_C)
#define CLR_NZVC	CC&=~(CC_N|CC_Z|CC_V|CC_C)
#define CLR_Z		CC&=~(CC_Z)
#define CLR_NZC 	CC&=~(CC_N|CC_Z|CC_C)
#define CLR_ZC		CC&=~(CC_Z|CC_C)

/* macros for CC -- CC bits affected should be reset before calling */
#define SET_Z(a)		if(!a)SEZ
#define SET_Z8(a)		SET_Z((UINT8)a)
#define SET_Z16(a)		SET_Z((UINT16)a)
#define SET_N8(a)		CC|=((a&0x80)>>4)
#define SET_N16(a)		CC|=((a&0x8000)>>12)
#define SET_N32(a)		CC|=((a&0x8000)>>20)
#define SET_H(a,b,r)	CC|=(((a^b^r)&0x10)<<1)
#define SET_C8(a)		CC|=((a&0x100)>>8)
#define SET_C16(a)		CC|=((a&0x10000)>>16)
#define SET_V8(a,b,r)	CC|=(((a^b^r^(r>>1))&0x80)>>6)
#define SET_V16(a,b,r)	CC|=(((a^b^r^(r>>1))&0x8000)>>14)

static UINT8 flags8i[256]=	 /* increment */
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
static UINT8 flags8d[256]= /* decrement */
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
#define SET_FLAGS8I(a)		{CC|=flags8i[(a)&0xff];}
#define SET_FLAGS8D(a)		{CC|=flags8d[(a)&0xff];}

/* combos */
#define SET_NZ8(a)			{SET_N8(a);SET_Z(a);}
#define SET_NZ16(a) 		{SET_N16(a);SET_Z(a);}
#define SET_NZ32(a) 		{SET_N32(a);SET_Z(a);}
#define SET_FLAGS8(a,b,r)	{SET_N8(r);SET_Z8(r);SET_V8(a,b,r);SET_C8(r);}
#define SET_FLAGS16(a,b,r)	{SET_N16(r);SET_Z16(r);SET_V16(a,b,r);SET_C16(r);}

#define NXORV				((CC&CC_N)^((CC&CC_V)<<2))

/* for treating an unsigned byte as a signed word */
#define SIGNED(b) ((UINT16)(b&0x80?b|0xff00:b))
/* for treating an unsigned short as a signed long */
#define SIGNED_16(b) ((UINT32)(b&0x8000?b|0xffff0000:b))

/* macros for addressing modes (postbytes have their own code) */
#define DIRECT	EAD = DPD; IMMBYTE(ea.b.l)
#define IMM8	EAD = PCD; PC++
#define IMM16	EAD = PCD; PC+=2
#define EXTENDED IMMWORD(ea)

/* macros to set status flags */
#define SEC CC|=CC_C
#define CLC CC&=~CC_C
#define SEZ CC|=CC_Z
#define CLZ CC&=~CC_Z
#define SEN CC|=CC_N
#define CLN CC&=~CC_N
#define SEV CC|=CC_V
#define CLV CC&=~CC_V
#define SEH CC|=CC_H
#define CLH CC&=~CC_H

/* Macros to set mode flags */
#define SEDZ MD|=MD_DZ
#define CLDZ MD&=~MD_DZ
#define SEII MD|=MD_II
#define CLII MD&=~MD_II
#define SEFM MD|=MD_FM
#define CLFM MD&=~MD_FM
#define SEEM MD|=MD_EM
#define CLEM MD&=~MD_EM

/* macros for convenience */
#define DIRBYTE(b) {DIRECT;b=RM(EAD);}
#define DIRWORD(w) {DIRECT;w.d=RM16(EAD);}
#define DIRLONG(lng) {DIRECT;lng.w.h=RM16(EAD);lng.w.l=RM16(EAD+2);}
#define EXTBYTE(b) {EXTENDED;b=RM(EAD);}
#define EXTWORD(wd) {EXTENDED;wd.w.l=RM16(EAD);}
#define EXTWORD_D(wd) {EXTENDED;wd.w.h=RM16(EAD);}
#define EXTLONG(lng) {EXTENDED;lng.w.h=RM16(EAD);lng.w.l=RM16(EAD+2);}

/* macros for branch instructions */
#define BRANCH(f) { 					\
	UINT8 t;							\
	IMMBYTE(t); 						\
	if( f ) 							\
	{									\
		PC += SIGNED(t);				\
		CHANGE_PC;						\
	}									\
}

#define LBRANCH(f) {					\
	PAIR t; 							\
	IMMWORD(t); 						\
	if( f ) 							\
	{									\
		hd6309_ICount -= 1; 			\
		PC += t.w.l;					\
		CHANGE_PC;						\
	}									\
}

INLINE UINT32 RM16( UINT32 mAddr );
INLINE UINT32 RM16( UINT32 mAddr )
{
	UINT32 result = RM(mAddr) << 8;
	return result | RM((mAddr+1)&0xffff);
}

INLINE UINT32 RM32( UINT32 mAddr );
INLINE UINT32 RM32( UINT32 mAddr )
{
	UINT32 result = RM(mAddr) << 24;
	result += RM(mAddr+1) << 16;
	result += RM(mAddr+2) << 8;
	result += RM(mAddr+3);
	return result;
}

INLINE void WM16( UINT32 mAddr, PAIR *p );
INLINE void WM16( UINT32 mAddr, PAIR *p )
{
	WM( mAddr, p->b.h );
	WM( (mAddr+1)&0xffff, p->b.l );
}

INLINE void WM16_D( UINT32 mAddr, PAIR *p );
INLINE void WM16_D( UINT32 mAddr, PAIR *p )
{
	WM( mAddr, p->b.h3 );
	WM( (mAddr+1)&0xffff, p->b.h2 );
}

INLINE void WM32( UINT32 mAddr, PAIR *p );
INLINE void WM32( UINT32 mAddr, PAIR *p )
{
	WM( mAddr, p->b.h3 );
	WM( (mAddr+1)&0xffff, p->b.h2 );
	WM( (mAddr+2)&0xffff, p->b.h );
	WM( (mAddr+3)&0xffff, p->b.l );
}

void CHECK_IRQ_LINES( void )
{
	if( hd6309.irq_state[HD6309_IRQ_LINE] != CLEAR_LINE ||
		hd6309.irq_state[HD6309_FIRQ_LINE] != CLEAR_LINE )
		hd6309.int_state &= ~HD6309_SYNC; /* clear SYNC flag */
	if( hd6309.irq_state[HD6309_FIRQ_LINE]!=CLEAR_LINE && !(CC & CC_IF))
	{
		/* fast IRQ */
		/* HJB 990225: state already saved by CWAI? */
		if( hd6309.int_state & HD6309_CWAI )
		{
			hd6309.int_state &= ~HD6309_CWAI;
			hd6309.extra_cycles += 7;		 /* subtract +7 cycles */
		}
		else
		{
			if ( hd6309.md & MD_FM )
			{
				CC |= CC_E; 				/* save entire state */
				PUSHWORD(pPC);
				PUSHWORD(pU);
				PUSHWORD(pY);
				PUSHWORD(pX);
				PUSHBYTE(DP);
				if ( hd6309.md & MD_EM )
				{
					PUSHBYTE(F);
					PUSHBYTE(E);
					hd6309.extra_cycles += 2; /* subtract +2 cycles */
				}
				PUSHBYTE(B);
				PUSHBYTE(A);
				PUSHBYTE(CC);
				hd6309.extra_cycles += 19;	 /* subtract +19 cycles */
			}
			else
			{
				CC &= ~CC_E;				/* save 'short' state */
				PUSHWORD(pPC);
				PUSHBYTE(CC);
				hd6309.extra_cycles += 10;	/* subtract +10 cycles */
			}
		}
		CC |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
		PCD=RM16(0xfff6);
		CHANGE_PC;
		(void)(*hd6309.irq_callback)(HD6309_FIRQ_LINE);
	}
	else
	if( hd6309.irq_state[HD6309_IRQ_LINE]!=CLEAR_LINE && !(CC & CC_II) )
	{
		/* standard IRQ */
		/* HJB 990225: state already saved by CWAI? */
		if( hd6309.int_state & HD6309_CWAI )
		{
			hd6309.int_state &= ~HD6309_CWAI;  /* clear CWAI flag */
			hd6309.extra_cycles += 7;		 /* subtract +7 cycles */
		}
		else
		{
			CC |= CC_E; 				/* save entire state */
			PUSHWORD(pPC);
			PUSHWORD(pU);
			PUSHWORD(pY);
			PUSHWORD(pX);
			PUSHBYTE(DP);
			if ( MD & MD_EM )
			{
				PUSHBYTE(F);
				PUSHBYTE(E);
				hd6309.extra_cycles += 2; /* subtract +2 cycles */
			}
			PUSHBYTE(B);
			PUSHBYTE(A);
			PUSHBYTE(CC);
			hd6309.extra_cycles += 19;	 /* subtract +19 cycles */
		}
		CC |= CC_II;					/* inhibit IRQ */
		PCD=RM16(0xfff8);
		CHANGE_PC;
		(void)(*hd6309.irq_callback)(HD6309_IRQ_LINE);
	}
}

/****************************************************************************
 * Get all registers in given buffer
 ****************************************************************************/
unsigned hd6309_get_context(void *dst)
{
	if( dst )
		*(hd6309_Regs*)dst = hd6309;
	return sizeof(hd6309_Regs);
}

/****************************************************************************
 * Set all registers to given values
 ****************************************************************************/
void hd6309_set_context(void *src)
{
	if( src )
		hd6309 = *(hd6309_Regs*)src;
	CHANGE_PC;

	CHECK_IRQ_LINES();
}

/****************************************************************************
 * Return program counter
 ****************************************************************************/
unsigned hd6309_get_pc(void)
{
	return PC;
}


/****************************************************************************
 * Set program counter
 ****************************************************************************/
void hd6309_set_pc(unsigned val)
{
	PC = val;
	CHANGE_PC;
}


/****************************************************************************
 * Return stack pointer
 ****************************************************************************/
unsigned hd6309_get_sp(void)
{
	return S;
}


/****************************************************************************
 * Set stack pointer
 ****************************************************************************/
void hd6309_set_sp(unsigned val)
{
	S = val;
}


/****************************************************************************/
/* Return a specific register												*/
/****************************************************************************/
unsigned hd6309_get_reg(int regnum)
{
	switch( regnum )
	{
		case HD6309_PC: return PC;
		case HD6309_S: return S;
		case HD6309_CC: return CC;
		case HD6309_MD: return MD;
		case HD6309_U: return U;
		case HD6309_A: return A;
		case HD6309_B: return B;
		case HD6309_E: return E;
		case HD6309_F: return F;
		case HD6309_X: return X;
		case HD6309_Y: return Y;
		case HD6309_V: return V;
		case HD6309_DP: return DP;
		case HD6309_NMI_STATE: return hd6309.nmi_state;
		case HD6309_IRQ_STATE: return hd6309.irq_state[HD6309_IRQ_LINE];
		case HD6309_FIRQ_STATE: return hd6309.irq_state[HD6309_FIRQ_LINE];
		case REG_PREVIOUSPC: return PPC;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < 0xffff )
					return ( RM( offset ) << 8 ) | RM( offset + 1 );
			}
	}
	return 0;
}


/****************************************************************************/
/* Set a specific register													*/
/****************************************************************************/
void hd6309_set_reg(int regnum, unsigned val)
{
	switch( regnum )
	{
		case HD6309_PC: PC = val; CHANGE_PC; break;
		case HD6309_S: S = val; break;
		case HD6309_CC: CC = val; CHECK_IRQ_LINES(); break;
		case HD6309_MD: MD = val; break;
		case HD6309_U: U = val; break;
		case HD6309_A: A = val; break;
		case HD6309_B: B = val; break;
		case HD6309_E: E = val; break;
		case HD6309_F: F = val; break;
		case HD6309_X: X = val; break;
		case HD6309_Y: Y = val; break;
		case HD6309_V: V = val; break;
		case HD6309_DP: DP = val; break;
		case HD6309_NMI_STATE: hd6309.nmi_state = val; break;
		case HD6309_IRQ_STATE: hd6309.irq_state[HD6309_IRQ_LINE] = val; break;
		case HD6309_FIRQ_STATE: hd6309.irq_state[HD6309_FIRQ_LINE] = val; break;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < 0xffff )
				{
					WM( offset, (val >> 8) & 0xff );
					WM( offset+1, val & 0xff );
				}
			}
	}
}


/****************************************************************************/
/* Reset registers to their initial values									*/
/****************************************************************************/
void hd6309_reset(void *param)
{
	hd6309.int_state = 0;
	hd6309.nmi_state = CLEAR_LINE;
	hd6309.irq_state[0] = CLEAR_LINE;
	hd6309.irq_state[0] = CLEAR_LINE;

	DPD = 0;			/* Reset direct page register */

	MD = 0; 			/* Mode register get reset */
	CC |= CC_II;		/* IRQ disabled */
	CC |= CC_IF;		/* FIRQ disabled */

	PCD = RM16(0xfffe);
	CHANGE_PC;
}

void hd6309_exit(void)
{
	/* nothing to do ? */
}

/* Generate interrupts */
/****************************************************************************
 * Set NMI line state
 ****************************************************************************/
void hd6309_set_nmi_line(int state)
{
	if (hd6309.nmi_state == state) return;
	hd6309.nmi_state = state;
	LOG(("HD6309#%d set_nmi_line %d\n", cpu_getactivecpu(), state));
	if( state == CLEAR_LINE ) return;

	/* if the stack was not yet initialized */
	if( !(hd6309.int_state & HD6309_LDS) ) return;

	hd6309.int_state &= ~HD6309_SYNC;
	/* HJB 990225: state already saved by CWAI? */
	if( hd6309.int_state & HD6309_CWAI )
	{
		hd6309.int_state &= ~HD6309_CWAI;
		hd6309.extra_cycles += 7;	/* subtract +7 cycles next time */
	}
	else
	{
		CC |= CC_E; 				/* save entire state */
		PUSHWORD(pPC);
		PUSHWORD(pU);
		PUSHWORD(pY);
		PUSHWORD(pX);
		PUSHBYTE(DP);

		/* I am not sure is this really happens on a 6309.
		   I have this here just in case					*/

		if ( MD & MD_EM )
		{
			PUSHBYTE(F);
			PUSHBYTE(E);
			hd6309.extra_cycles += 2; /* subtract +2 cycles */
		}

		PUSHBYTE(B);
		PUSHBYTE(A);
		PUSHBYTE(CC);
		hd6309.extra_cycles += 19;	/* subtract +19 cycles next time */
	}
	CC |= CC_IF | CC_II;			/* inhibit FIRQ and IRQ */
	PCD = RM16(0xfffc);
	CHANGE_PC;
}

/****************************************************************************
 * Set IRQ line state
 ****************************************************************************/
void hd6309_set_irq_line(int irqline, int state)
{
	LOG(("HD6309#%d set_irq_line %d, %d\n", cpu_getactivecpu(), irqline, state));
	hd6309.irq_state[irqline] = state;
	if (state == CLEAR_LINE) return;
	CHECK_IRQ_LINES();
}

/****************************************************************************
 * Set IRQ vector callback
 ****************************************************************************/
void hd6309_set_irq_callback(int (*callback)(int irqline))
{
	hd6309.irq_callback = callback;
}

/****************************************************************************
 * Save CPU state
 ****************************************************************************/
static void state_save(void *file, const char *module)
{
	int cpu = cpu_getactivecpu();
	state_save_UINT16(file, module, cpu, "PC", &PC, 1);
	state_save_UINT16(file, module, cpu, "U", &U, 1);
	state_save_UINT16(file, module, cpu, "S", &S, 1);
	state_save_UINT16(file, module, cpu, "X", &X, 1);
	state_save_UINT16(file, module, cpu, "Y", &Y, 1);
	state_save_UINT16(file, module, cpu, "V", &V, 1);
	state_save_UINT8(file, module, cpu, "DP", &DP, 1);
	state_save_UINT8(file, module, cpu, "CC", &CC, 1);
	state_save_UINT8(file, module, cpu, "MD", &MD, 1);
	state_save_UINT8(file, module, cpu, "INT", &hd6309.int_state, 1);
	state_save_UINT8(file, module, cpu, "NMI", &hd6309.nmi_state, 1);
	state_save_UINT8(file, module, cpu, "IRQ", &hd6309.irq_state[0], 1);
	state_save_UINT8(file, module, cpu, "FIRQ", &hd6309.irq_state[1], 1);
}

/****************************************************************************
 * Load CPU state
 ****************************************************************************/
static void state_load(void *file, const char *module)
{
	int cpu = cpu_getactivecpu();
	state_load_UINT16(file, module, cpu, "PC", &PC, 1);
	state_load_UINT16(file, module, cpu, "U", &U, 1);
	state_load_UINT16(file, module, cpu, "S", &S, 1);
	state_load_UINT16(file, module, cpu, "X", &X, 1);
	state_load_UINT16(file, module, cpu, "Y", &Y, 1);
	state_load_UINT16(file, module, cpu, "V", &V, 1);
	state_load_UINT8(file, module, cpu, "DP", &DP, 1);
	state_load_UINT8(file, module, cpu, "CC", &CC, 1);
	state_load_UINT8(file, module, cpu, "MD", &MD, 1);
	state_load_UINT8(file, module, cpu, "INT", &hd6309.int_state, 1);
	state_load_UINT8(file, module, cpu, "NMI", &hd6309.nmi_state, 1);
	state_load_UINT8(file, module, cpu, "IRQ", &hd6309.irq_state[0], 1);
	state_load_UINT8(file, module, cpu, "FIRQ", &hd6309.irq_state[1], 1);
}

void hd6309_state_save(void *file) { state_save(file, "hd6309"); }
void hd6309_state_load(void *file) { state_load(file, "hd6309"); }

/****************************************************************************
 * Return a formatted string for a register
 ****************************************************************************/
const char *hd6309_info(void *context, int regnum)
{
	static char buffer[16][47+1];
	static int which = 0;
	hd6309_Regs *r = context;

	which = ++which % 16;
	buffer[which][0] = '\0';
	if( !context )
		r = &hd6309;

	switch( regnum )
	{
		case CPU_INFO_NAME: return "HD6309";
		case CPU_INFO_FAMILY: return "Hitachi 6309";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Copyright (C) John Butler 1997 and Tim Lindner 2000";
		case CPU_INFO_REG_LAYOUT: return (const char*)hd6309_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)hd6309_win_layout;

		case CPU_INFO_FLAGS:
			sprintf(buffer[which], "%c%c%c%c%c%c%c%c (MD:%c%c%c%c%c%c%c%c)",
				r->cc & 0x80 ? 'E':'.',
				r->cc & 0x40 ? 'F':'.',
				r->cc & 0x20 ? 'H':'.',
				r->cc & 0x10 ? 'I':'.',
				r->cc & 0x08 ? 'N':'.',
				r->cc & 0x04 ? 'Z':'.',
				r->cc & 0x02 ? 'V':'.',
				r->cc & 0x01 ? 'C':'.',

				r->md & 0x80 ? 'E':'e',
				r->md & 0x40 ? 'F':'f',
				r->md & 0x20 ? '.':'.',
				r->md & 0x10 ? '.':'.',
				r->md & 0x08 ? '.':'.',
				r->md & 0x04 ? '.':'.',
				r->md & 0x02 ? 'I':'i',
				r->md & 0x01 ? 'Z':'z');
			break;
		case CPU_INFO_REG+HD6309_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
		case CPU_INFO_REG+HD6309_S: sprintf(buffer[which], "S:%04X", r->s.w.l); break;
		case CPU_INFO_REG+HD6309_CC: sprintf(buffer[which], "CC:%02X", r->cc); break;
		case CPU_INFO_REG+HD6309_MD: sprintf(buffer[which], "MD:%02X", r->md); break;
		case CPU_INFO_REG+HD6309_U: sprintf(buffer[which], "U:%04X", r->u.w.l); break;
		case CPU_INFO_REG+HD6309_A: sprintf(buffer[which], "A:%02X", r->q.b.h3); break;
		case CPU_INFO_REG+HD6309_B: sprintf(buffer[which], "B:%02X", r->q.b.h2); break;
		case CPU_INFO_REG+HD6309_E: sprintf(buffer[which], "E:%02X", r->q.b.h); break;
		case CPU_INFO_REG+HD6309_F: sprintf(buffer[which], "F:%02X", r->q.b.l); break;
		case CPU_INFO_REG+HD6309_X: sprintf(buffer[which], "X:%04X", r->x.w.l); break;
		case CPU_INFO_REG+HD6309_Y: sprintf(buffer[which], "Y:%04X", r->y.w.l); break;
		case CPU_INFO_REG+HD6309_V: sprintf(buffer[which], "V:%04X", r->v.w.l); break;
		case CPU_INFO_REG+HD6309_DP: sprintf(buffer[which], "DP:%02X", r->dp.b.h); break;
		case CPU_INFO_REG+HD6309_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
		case CPU_INFO_REG+HD6309_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state[HD6309_IRQ_LINE]); break;
		case CPU_INFO_REG+HD6309_FIRQ_STATE: sprintf(buffer[which], "FIRQ:%X", r->irq_state[HD6309_FIRQ_LINE]); break;
	}
	return buffer[which];
}

unsigned hd6309_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6309(buffer,pc);
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

/* includes the static function prototypes and the master opcode table */
#include "6309tbl.c"

#define IlegalInstructionError	{SEII;illegal();}
#define DivisionByZeroError 	{SEDZ;illegal();}

/* includes the actual opcode implementations */
#include "6309ops.c"

/* execute instructions on this CPU until icount expires */
int hd6309_execute(int cycles)	/* NS 970908 */
{
	hd6309_ICount = cycles - hd6309.extra_cycles;
	hd6309.extra_cycles = 0;

	if (hd6309.int_state & (HD6309_CWAI | HD6309_SYNC))
	{
		hd6309_ICount = 0;
	}
	else
	{
		do
		{
			pPPC = pPC;

			CALL_MAME_DEBUG;

			hd6309.ireg = ROP(PCD);
			PC++;

			switch( hd6309.ireg )
			{
			case 0x00: neg_di();   hd6309_ICount-= 6;				break;
			case 0x01: oim_di();   hd6309_ICount-= 6;				break;
			case 0x02: aim_di();   hd6309_ICount-= 6;				break;
			case 0x03: com_di();   hd6309_ICount-= 6;				break;
			case 0x04: lsr_di();   hd6309_ICount-= 6;				break;
			case 0x05: eim_di();   hd6309_ICount-= 6;				break;
			case 0x06: ror_di();   hd6309_ICount-= 6;				break;
			case 0x07: asr_di();   hd6309_ICount-= 6;				break;
			case 0x08: asl_di();   hd6309_ICount-= 6;				break;
			case 0x09: rol_di();   hd6309_ICount-= 6;				break;
			case 0x0a: dec_di();   hd6309_ICount-= 6;				break;
			case 0x0b: tim_di();   hd6309_ICount-= 6;				break;
			case 0x0c: inc_di();   hd6309_ICount-= 6;				break;
			case 0x0d: tst_di();   hd6309_ICount-= 6;				break;
			case 0x0e: jmp_di();   hd6309_ICount-= 3;				break;
			case 0x0f: clr_di();   hd6309_ICount-= 6;				break;
			case 0x10: pref10();									break;
			case 0x11: pref11();									break;
			case 0x12: nop();	   hd6309_ICount-= 2;				break;
			case 0x13: sync();	   hd6309_ICount-= 4;				break;
			case 0x14: sexw();	   hd6309_ICount-= 4;				break;
			case 0x15: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x16: lbra();	   hd6309_ICount-= 5;				break;
			case 0x17: lbsr();	   hd6309_ICount-= 9;				break;
			case 0x18: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x19: daa();	   hd6309_ICount-= 2;				break;
			case 0x1a: orcc();	   hd6309_ICount-= 3;				break;
			case 0x1b: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x1c: andcc();    hd6309_ICount-= 3;				break;
			case 0x1d: sex();	   hd6309_ICount-= 2;				break;
			case 0x1e: exg();	   hd6309_ICount-= 8;				break;
			case 0x1f: tfr();	   hd6309_ICount-= 6;				break;
			case 0x20: bra();	   hd6309_ICount-= 3;				break;
			case 0x21: brn();	   hd6309_ICount-= 3;				break;
			case 0x22: bhi();	   hd6309_ICount-= 3;				break;
			case 0x23: bls();	   hd6309_ICount-= 3;				break;
			case 0x24: bcc();	   hd6309_ICount-= 3;				break;
			case 0x25: bcs();	   hd6309_ICount-= 3;				break;
			case 0x26: bne();	   hd6309_ICount-= 3;				break;
			case 0x27: beq();	   hd6309_ICount-= 3;				break;
			case 0x28: bvc();	   hd6309_ICount-= 3;				break;
			case 0x29: bvs();	   hd6309_ICount-= 3;				break;
			case 0x2a: bpl();	   hd6309_ICount-= 3;				break;
			case 0x2b: bmi();	   hd6309_ICount-= 3;				break;
			case 0x2c: bge();	   hd6309_ICount-= 3;				break;
			case 0x2d: blt();	   hd6309_ICount-= 3;				break;
			case 0x2e: bgt();	   hd6309_ICount-= 3;				break;
			case 0x2f: ble();	   hd6309_ICount-= 3;				break;
			case 0x30: leax();	   hd6309_ICount-= 4;				break;
			case 0x31: leay();	   hd6309_ICount-= 4;				break;
			case 0x32: leas();	   hd6309_ICount-= 4;				break;
			case 0x33: leau();	   hd6309_ICount-= 4;				break;
			case 0x34: pshs();	   hd6309_ICount-= 5;				break;
			case 0x35: puls();	   hd6309_ICount-= 5;				break;
			case 0x36: pshu();	   hd6309_ICount-= 5;				break;
			case 0x37: pulu();	   hd6309_ICount-= 5;				break;
			case 0x38: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x39: rts();	   hd6309_ICount-= 5;				break;
			case 0x3a: abx();	   hd6309_ICount-= 3;				break;
			case 0x3b: rti();	   hd6309_ICount-= 6;				break;
			case 0x3c: cwai();	   hd6309_ICount-=20;				break;
			case 0x3d: mul();	   hd6309_ICount-=11;				break;
			case 0x3e: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x3f: swi();	   hd6309_ICount-=19;				break;
			case 0x40: nega();	   hd6309_ICount-= 2;				break;
			case 0x41: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x42: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x43: coma();	   hd6309_ICount-= 2;				break;
			case 0x44: lsra();	   hd6309_ICount-= 2;				break;
			case 0x45: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x46: rora();	   hd6309_ICount-= 2;				break;
			case 0x47: asra();	   hd6309_ICount-= 2;				break;
			case 0x48: asla();	   hd6309_ICount-= 2;				break;
			case 0x49: rola();	   hd6309_ICount-= 2;				break;
			case 0x4a: deca();	   hd6309_ICount-= 2;				break;
			case 0x4b: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x4c: inca();	   hd6309_ICount-= 2;				break;
			case 0x4d: tsta();	   hd6309_ICount-= 2;				break;
			case 0x4e: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x4f: clra();	   hd6309_ICount-= 2;				break;
			case 0x50: negb();	   hd6309_ICount-= 2;				break;
			case 0x51: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x52: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x53: comb();	   hd6309_ICount-= 2;				break;
			case 0x54: lsrb();	   hd6309_ICount-= 2;				break;
			case 0x55: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x56: rorb();	   hd6309_ICount-= 2;				break;
			case 0x57: asrb();	   hd6309_ICount-= 2;				break;
			case 0x58: aslb();	   hd6309_ICount-= 2;				break;
			case 0x59: rolb();	   hd6309_ICount-= 2;				break;
			case 0x5a: decb();	   hd6309_ICount-= 2;				break;
			case 0x5b: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x5c: incb();	   hd6309_ICount-= 2;				break;
			case 0x5d: tstb();	   hd6309_ICount-= 2;				break;
			case 0x5e: IlegalInstructionError;	hd6309_ICount-= 2;	break;
			case 0x5f: clrb();	   hd6309_ICount-= 2;				break;
			case 0x60: neg_ix();   hd6309_ICount-= 6;				break;
			case 0x61: oim_ix();   hd6309_ICount-= 7;				break;
			case 0x62: aim_ix();   hd6309_ICount-= 7;				break;
			case 0x63: com_ix();   hd6309_ICount-= 6;				break;
			case 0x64: lsr_ix();   hd6309_ICount-= 6;				break;
			case 0x65: eim_ix();   hd6309_ICount-= 7;				break;
			case 0x66: ror_ix();   hd6309_ICount-= 6;				break;
			case 0x67: asr_ix();   hd6309_ICount-= 6;				break;
			case 0x68: asl_ix();   hd6309_ICount-= 6;				break;
			case 0x69: rol_ix();   hd6309_ICount-= 6;				break;
			case 0x6a: dec_ix();   hd6309_ICount-= 6;				break;
			case 0x6b: tim_ix();   hd6309_ICount-= 7;				break;
			case 0x6c: inc_ix();   hd6309_ICount-= 6;				break;
			case 0x6d: tst_ix();   hd6309_ICount-= 6;				break;
			case 0x6e: jmp_ix();   hd6309_ICount-= 3;				break;
			case 0x6f: clr_ix();   hd6309_ICount-= 6;				break;
			case 0x70: neg_ex();   hd6309_ICount-= 7;				break;
			case 0x71: oim_ex();   hd6309_ICount-= 7;				break;
			case 0x72: aim_ex();   hd6309_ICount-= 7;				break;
			case 0x73: com_ex();   hd6309_ICount-= 7;				break;
			case 0x74: lsr_ex();   hd6309_ICount-= 7;				break;
			case 0x75: eim_ex();   hd6309_ICount-= 7;				break;
			case 0x76: ror_ex();   hd6309_ICount-= 7;				break;
			case 0x77: asr_ex();   hd6309_ICount-= 7;				break;
			case 0x78: asl_ex();   hd6309_ICount-= 7;				break;
			case 0x79: rol_ex();   hd6309_ICount-= 7;				break;
			case 0x7a: dec_ex();   hd6309_ICount-= 7;				break;
			case 0x7b: tim_ex();   hd6309_ICount-= 5;				break;
			case 0x7c: inc_ex();   hd6309_ICount-= 7;				break;
			case 0x7d: tst_ex();   hd6309_ICount-= 7;				break;
			case 0x7e: jmp_ex();   hd6309_ICount-= 4;				break;
			case 0x7f: clr_ex();   hd6309_ICount-= 7;				break;
			case 0x80: suba_im();  hd6309_ICount-= 2;				break;
			case 0x81: cmpa_im();  hd6309_ICount-= 2;				break;
			case 0x82: sbca_im();  hd6309_ICount-= 2;				break;
			case 0x83: subd_im();  hd6309_ICount-= 4;				break;
			case 0x84: anda_im();  hd6309_ICount-= 2;				break;
			case 0x85: bita_im();  hd6309_ICount-= 2;				break;
			case 0x86: lda_im();   hd6309_ICount-= 2;				break;
			case 0x87: IlegalInstructionError;	 hd6309_ICount-= 2; break;
			case 0x88: eora_im();  hd6309_ICount-= 2;				break;
			case 0x89: adca_im();  hd6309_ICount-= 2;				break;
			case 0x8a: ora_im();   hd6309_ICount-= 2;				break;
			case 0x8b: adda_im();  hd6309_ICount-= 2;				break;
			case 0x8c: cmpx_im();  hd6309_ICount-= 4;				break;
			case 0x8d: bsr();	   hd6309_ICount-= 7;				break;
			case 0x8e: ldx_im();   hd6309_ICount-= 3;				break;
			case 0x8f: IlegalInstructionError;	 hd6309_ICount-= 2; break;
			case 0x90: suba_di();  hd6309_ICount-= 4;				break;
			case 0x91: cmpa_di();  hd6309_ICount-= 4;				break;
			case 0x92: sbca_di();  hd6309_ICount-= 4;				break;
			case 0x93: subd_di();  hd6309_ICount-= 6;				break;
			case 0x94: anda_di();  hd6309_ICount-= 4;				break;
			case 0x95: bita_di();  hd6309_ICount-= 4;				break;
			case 0x96: lda_di();   hd6309_ICount-= 4;				break;
			case 0x97: sta_di();   hd6309_ICount-= 4;				break;
			case 0x98: eora_di();  hd6309_ICount-= 4;				break;
			case 0x99: adca_di();  hd6309_ICount-= 4;				break;
			case 0x9a: ora_di();   hd6309_ICount-= 4;				break;
			case 0x9b: adda_di();  hd6309_ICount-= 4;				break;
			case 0x9c: cmpx_di();  hd6309_ICount-= 6;				break;
			case 0x9d: jsr_di();   hd6309_ICount-= 7;				break;
			case 0x9e: ldx_di();   hd6309_ICount-= 5;				break;
			case 0x9f: stx_di();   hd6309_ICount-= 5;				break;
			case 0xa0: suba_ix();  hd6309_ICount-= 4;				break;
			case 0xa1: cmpa_ix();  hd6309_ICount-= 4;				break;
			case 0xa2: sbca_ix();  hd6309_ICount-= 4;				break;
			case 0xa3: subd_ix();  hd6309_ICount-= 6;				break;
			case 0xa4: anda_ix();  hd6309_ICount-= 4;				break;
			case 0xa5: bita_ix();  hd6309_ICount-= 4;				break;
			case 0xa6: lda_ix();   hd6309_ICount-= 4;				break;
			case 0xa7: sta_ix();   hd6309_ICount-= 4;				break;
			case 0xa8: eora_ix();  hd6309_ICount-= 4;				break;
			case 0xa9: adca_ix();  hd6309_ICount-= 4;				break;
			case 0xaa: ora_ix();   hd6309_ICount-= 4;				break;
			case 0xab: adda_ix();  hd6309_ICount-= 4;				break;
			case 0xac: cmpx_ix();  hd6309_ICount-= 6;				break;
			case 0xad: jsr_ix();   hd6309_ICount-= 7;				break;
			case 0xae: ldx_ix();   hd6309_ICount-= 5;				break;
			case 0xaf: stx_ix();   hd6309_ICount-= 5;				break;
			case 0xb0: suba_ex();  hd6309_ICount-= 5;				break;
			case 0xb1: cmpa_ex();  hd6309_ICount-= 5;				break;
			case 0xb2: sbca_ex();  hd6309_ICount-= 5;				break;
			case 0xb3: subd_ex();  hd6309_ICount-= 7;				break;
			case 0xb4: anda_ex();  hd6309_ICount-= 5;				break;
			case 0xb5: bita_ex();  hd6309_ICount-= 5;				break;
			case 0xb6: lda_ex();   hd6309_ICount-= 5;				break;
			case 0xb7: sta_ex();   hd6309_ICount-= 5;				break;
			case 0xb8: eora_ex();  hd6309_ICount-= 5;				break;
			case 0xb9: adca_ex();  hd6309_ICount-= 5;				break;
			case 0xba: ora_ex();   hd6309_ICount-= 5;				break;
			case 0xbb: adda_ex();  hd6309_ICount-= 5;				break;
			case 0xbc: cmpx_ex();  hd6309_ICount-= 7;				break;
			case 0xbd: jsr_ex();   hd6309_ICount-= 8;				break;
			case 0xbe: ldx_ex();   hd6309_ICount-= 6;				break;
			case 0xbf: stx_ex();   hd6309_ICount-= 6;				break;
			case 0xc0: subb_im();  hd6309_ICount-= 2;				break;
			case 0xc1: cmpb_im();  hd6309_ICount-= 2;				break;
			case 0xc2: sbcb_im();  hd6309_ICount-= 2;				break;
			case 0xc3: addd_im();  hd6309_ICount-= 4;				break;
			case 0xc4: andb_im();  hd6309_ICount-= 2;				break;
			case 0xc5: bitb_im();  hd6309_ICount-= 2;				break;
			case 0xc6: ldb_im();   hd6309_ICount-= 2;				break;
			case 0xc7: IlegalInstructionError;	 hd6309_ICount-= 2; break;
			case 0xc8: eorb_im();  hd6309_ICount-= 2;				break;
			case 0xc9: adcb_im();  hd6309_ICount-= 2;				break;
			case 0xca: orb_im();   hd6309_ICount-= 2;				break;
			case 0xcb: addb_im();  hd6309_ICount-= 2;				break;
			case 0xcc: ldd_im();   hd6309_ICount-= 3;				break;
			case 0xcd: ldq_im();   hd6309_ICount-= 5;				break; /* in m6809 was std_im */
			case 0xce: ldu_im();   hd6309_ICount-= 3;				break;
			case 0xcf: IlegalInstructionError;	 hd6309_ICount-= 3; break;
			case 0xd0: subb_di();  hd6309_ICount-= 4;				break;
			case 0xd1: cmpb_di();  hd6309_ICount-= 4;				break;
			case 0xd2: sbcb_di();  hd6309_ICount-= 4;				break;
			case 0xd3: addd_di();  hd6309_ICount-= 6;				break;
			case 0xd4: andb_di();  hd6309_ICount-= 4;				break;
			case 0xd5: bitb_di();  hd6309_ICount-= 4;				break;
			case 0xd6: ldb_di();   hd6309_ICount-= 4;				break;
			case 0xd7: stb_di();   hd6309_ICount-= 4;				break;
			case 0xd8: eorb_di();  hd6309_ICount-= 4;				break;
			case 0xd9: adcb_di();  hd6309_ICount-= 4;				break;
			case 0xda: orb_di();   hd6309_ICount-= 4;				break;
			case 0xdb: addb_di();  hd6309_ICount-= 4;				break;
			case 0xdc: ldd_di();   hd6309_ICount-= 5;				break;
			case 0xdd: std_di();   hd6309_ICount-= 5;				break;
			case 0xde: ldu_di();   hd6309_ICount-= 5;				break;
			case 0xdf: stu_di();   hd6309_ICount-= 5;				break;
			case 0xe0: subb_ix();  hd6309_ICount-= 4;				break;
			case 0xe1: cmpb_ix();  hd6309_ICount-= 4;				break;
			case 0xe2: sbcb_ix();  hd6309_ICount-= 4;				break;
			case 0xe3: addd_ix();  hd6309_ICount-= 6;				break;
			case 0xe4: andb_ix();  hd6309_ICount-= 4;				break;
			case 0xe5: bitb_ix();  hd6309_ICount-= 4;				break;
			case 0xe6: ldb_ix();   hd6309_ICount-= 4;				break;
			case 0xe7: stb_ix();   hd6309_ICount-= 4;				break;
			case 0xe8: eorb_ix();  hd6309_ICount-= 4;				break;
			case 0xe9: adcb_ix();  hd6309_ICount-= 4;				break;
			case 0xea: orb_ix();   hd6309_ICount-= 4;				break;
			case 0xeb: addb_ix();  hd6309_ICount-= 4;				break;
			case 0xec: ldd_ix();   hd6309_ICount-= 5;				break;
			case 0xed: std_ix();   hd6309_ICount-= 5;				break;
			case 0xee: ldu_ix();   hd6309_ICount-= 5;				break;
			case 0xef: stu_ix();   hd6309_ICount-= 5;				break;
			case 0xf0: subb_ex();  hd6309_ICount-= 5;				break;
			case 0xf1: cmpb_ex();  hd6309_ICount-= 5;				break;
			case 0xf2: sbcb_ex();  hd6309_ICount-= 5;				break;
			case 0xf3: addd_ex();  hd6309_ICount-= 7;				break;
			case 0xf4: andb_ex();  hd6309_ICount-= 5;				break;
			case 0xf5: bitb_ex();  hd6309_ICount-= 5;				break;
			case 0xf6: ldb_ex();   hd6309_ICount-= 5;				break;
			case 0xf7: stb_ex();   hd6309_ICount-= 5;				break;
			case 0xf8: eorb_ex();  hd6309_ICount-= 5;				break;
			case 0xf9: adcb_ex();  hd6309_ICount-= 5;				break;
			case 0xfa: orb_ex();   hd6309_ICount-= 5;				break;
			case 0xfb: addb_ex();  hd6309_ICount-= 5;				break;
			case 0xfc: ldd_ex();   hd6309_ICount-= 6;				break;
			case 0xfd: std_ex();   hd6309_ICount-= 6;				break;
			case 0xfe: ldu_ex();   hd6309_ICount-= 6;				break;
			case 0xff: stu_ex();   hd6309_ICount-= 6;				break;
			}
		} while( hd6309_ICount > 0 );

		hd6309_ICount -= hd6309.extra_cycles;
		hd6309.extra_cycles = 0;
	}

	return cycles - hd6309_ICount;	 /* NS 970908 */
}

INLINE void fetch_effective_address( void )
{
	UINT8 postbyte = ROP_ARG(PCD);
	PC++;

	switch(postbyte)
	{
	case 0x00: EA=X;												hd6309_ICount-=1;	break;
	case 0x01: EA=X+1;												hd6309_ICount-=1;	break;
	case 0x02: EA=X+2;												hd6309_ICount-=1;	break;
	case 0x03: EA=X+3;												hd6309_ICount-=1;	break;
	case 0x04: EA=X+4;												hd6309_ICount-=1;	break;
	case 0x05: EA=X+5;												hd6309_ICount-=1;	break;
	case 0x06: EA=X+6;												hd6309_ICount-=1;	break;
	case 0x07: EA=X+7;												hd6309_ICount-=1;	break;
	case 0x08: EA=X+8;												hd6309_ICount-=1;	break;
	case 0x09: EA=X+9;												hd6309_ICount-=1;	break;
	case 0x0a: EA=X+10; 											hd6309_ICount-=1;	break;
	case 0x0b: EA=X+11; 											hd6309_ICount-=1;	break;
	case 0x0c: EA=X+12; 											hd6309_ICount-=1;	break;
	case 0x0d: EA=X+13; 											hd6309_ICount-=1;	break;
	case 0x0e: EA=X+14; 											hd6309_ICount-=1;	break;
	case 0x0f: EA=X+15; 											hd6309_ICount-=1;	break;

	case 0x10: EA=X-16; 											hd6309_ICount-=1;	break;
	case 0x11: EA=X-15; 											hd6309_ICount-=1;	break;
	case 0x12: EA=X-14; 											hd6309_ICount-=1;	break;
	case 0x13: EA=X-13; 											hd6309_ICount-=1;	break;
	case 0x14: EA=X-12; 											hd6309_ICount-=1;	break;
	case 0x15: EA=X-11; 											hd6309_ICount-=1;	break;
	case 0x16: EA=X-10; 											hd6309_ICount-=1;	break;
	case 0x17: EA=X-9;												hd6309_ICount-=1;	break;
	case 0x18: EA=X-8;												hd6309_ICount-=1;	break;
	case 0x19: EA=X-7;												hd6309_ICount-=1;	break;
	case 0x1a: EA=X-6;												hd6309_ICount-=1;	break;
	case 0x1b: EA=X-5;												hd6309_ICount-=1;	break;
	case 0x1c: EA=X-4;												hd6309_ICount-=1;	break;
	case 0x1d: EA=X-3;												hd6309_ICount-=1;	break;
	case 0x1e: EA=X-2;												hd6309_ICount-=1;	break;
	case 0x1f: EA=X-1;												hd6309_ICount-=1;	break;

	case 0x20: EA=Y;												hd6309_ICount-=1;	break;
	case 0x21: EA=Y+1;												hd6309_ICount-=1;	break;
	case 0x22: EA=Y+2;												hd6309_ICount-=1;	break;
	case 0x23: EA=Y+3;												hd6309_ICount-=1;	break;
	case 0x24: EA=Y+4;												hd6309_ICount-=1;	break;
	case 0x25: EA=Y+5;												hd6309_ICount-=1;	break;
	case 0x26: EA=Y+6;												hd6309_ICount-=1;	break;
	case 0x27: EA=Y+7;												hd6309_ICount-=1;	break;
	case 0x28: EA=Y+8;												hd6309_ICount-=1;	break;
	case 0x29: EA=Y+9;												hd6309_ICount-=1;	break;
	case 0x2a: EA=Y+10; 											hd6309_ICount-=1;	break;
	case 0x2b: EA=Y+11; 											hd6309_ICount-=1;	break;
	case 0x2c: EA=Y+12; 											hd6309_ICount-=1;	break;
	case 0x2d: EA=Y+13; 											hd6309_ICount-=1;	break;
	case 0x2e: EA=Y+14; 											hd6309_ICount-=1;	break;
	case 0x2f: EA=Y+15; 											hd6309_ICount-=1;	break;

	case 0x30: EA=Y-16; 											hd6309_ICount-=1;	break;
	case 0x31: EA=Y-15; 											hd6309_ICount-=1;	break;
	case 0x32: EA=Y-14; 											hd6309_ICount-=1;	break;
	case 0x33: EA=Y-13; 											hd6309_ICount-=1;	break;
	case 0x34: EA=Y-12; 											hd6309_ICount-=1;	break;
	case 0x35: EA=Y-11; 											hd6309_ICount-=1;	break;
	case 0x36: EA=Y-10; 											hd6309_ICount-=1;	break;
	case 0x37: EA=Y-9;												hd6309_ICount-=1;	break;
	case 0x38: EA=Y-8;												hd6309_ICount-=1;	break;
	case 0x39: EA=Y-7;												hd6309_ICount-=1;	break;
	case 0x3a: EA=Y-6;												hd6309_ICount-=1;	break;
	case 0x3b: EA=Y-5;												hd6309_ICount-=1;	break;
	case 0x3c: EA=Y-4;												hd6309_ICount-=1;	break;
	case 0x3d: EA=Y-3;												hd6309_ICount-=1;	break;
	case 0x3e: EA=Y-2;												hd6309_ICount-=1;	break;
	case 0x3f: EA=Y-1;												hd6309_ICount-=1;	break;

	case 0x40: EA=U;												hd6309_ICount-=1;	break;
	case 0x41: EA=U+1;												hd6309_ICount-=1;	break;
	case 0x42: EA=U+2;												hd6309_ICount-=1;	break;
	case 0x43: EA=U+3;												hd6309_ICount-=1;	break;
	case 0x44: EA=U+4;												hd6309_ICount-=1;	break;
	case 0x45: EA=U+5;												hd6309_ICount-=1;	break;
	case 0x46: EA=U+6;												hd6309_ICount-=1;	break;
	case 0x47: EA=U+7;												hd6309_ICount-=1;	break;
	case 0x48: EA=U+8;												hd6309_ICount-=1;	break;
	case 0x49: EA=U+9;												hd6309_ICount-=1;	break;
	case 0x4a: EA=U+10; 											hd6309_ICount-=1;	break;
	case 0x4b: EA=U+11; 											hd6309_ICount-=1;	break;
	case 0x4c: EA=U+12; 											hd6309_ICount-=1;	break;
	case 0x4d: EA=U+13; 											hd6309_ICount-=1;	break;
	case 0x4e: EA=U+14; 											hd6309_ICount-=1;	break;
	case 0x4f: EA=U+15; 											hd6309_ICount-=1;	break;

	case 0x50: EA=U-16; 											hd6309_ICount-=1;	break;
	case 0x51: EA=U-15; 											hd6309_ICount-=1;	break;
	case 0x52: EA=U-14; 											hd6309_ICount-=1;	break;
	case 0x53: EA=U-13; 											hd6309_ICount-=1;	break;
	case 0x54: EA=U-12; 											hd6309_ICount-=1;	break;
	case 0x55: EA=U-11; 											hd6309_ICount-=1;	break;
	case 0x56: EA=U-10; 											hd6309_ICount-=1;	break;
	case 0x57: EA=U-9;												hd6309_ICount-=1;	break;
	case 0x58: EA=U-8;												hd6309_ICount-=1;	break;
	case 0x59: EA=U-7;												hd6309_ICount-=1;	break;
	case 0x5a: EA=U-6;												hd6309_ICount-=1;	break;
	case 0x5b: EA=U-5;												hd6309_ICount-=1;	break;
	case 0x5c: EA=U-4;												hd6309_ICount-=1;	break;
	case 0x5d: EA=U-3;												hd6309_ICount-=1;	break;
	case 0x5e: EA=U-2;												hd6309_ICount-=1;	break;
	case 0x5f: EA=U-1;												hd6309_ICount-=1;	break;

	case 0x60: EA=S;												hd6309_ICount-=1;	break;
	case 0x61: EA=S+1;												hd6309_ICount-=1;	break;
	case 0x62: EA=S+2;												hd6309_ICount-=1;	break;
	case 0x63: EA=S+3;												hd6309_ICount-=1;	break;
	case 0x64: EA=S+4;												hd6309_ICount-=1;	break;
	case 0x65: EA=S+5;												hd6309_ICount-=1;	break;
	case 0x66: EA=S+6;												hd6309_ICount-=1;	break;
	case 0x67: EA=S+7;												hd6309_ICount-=1;	break;
	case 0x68: EA=S+8;												hd6309_ICount-=1;	break;
	case 0x69: EA=S+9;												hd6309_ICount-=1;	break;
	case 0x6a: EA=S+10; 											hd6309_ICount-=1;	break;
	case 0x6b: EA=S+11; 											hd6309_ICount-=1;	break;
	case 0x6c: EA=S+12; 											hd6309_ICount-=1;	break;
	case 0x6d: EA=S+13; 											hd6309_ICount-=1;	break;
	case 0x6e: EA=S+14; 											hd6309_ICount-=1;	break;
	case 0x6f: EA=S+15; 											hd6309_ICount-=1;	break;

	case 0x70: EA=S-16; 											hd6309_ICount-=1;	break;
	case 0x71: EA=S-15; 											hd6309_ICount-=1;	break;
	case 0x72: EA=S-14; 											hd6309_ICount-=1;	break;
	case 0x73: EA=S-13; 											hd6309_ICount-=1;	break;
	case 0x74: EA=S-12; 											hd6309_ICount-=1;	break;
	case 0x75: EA=S-11; 											hd6309_ICount-=1;	break;
	case 0x76: EA=S-10; 											hd6309_ICount-=1;	break;
	case 0x77: EA=S-9;												hd6309_ICount-=1;	break;
	case 0x78: EA=S-8;												hd6309_ICount-=1;	break;
	case 0x79: EA=S-7;												hd6309_ICount-=1;	break;
	case 0x7a: EA=S-6;												hd6309_ICount-=1;	break;
	case 0x7b: EA=S-5;												hd6309_ICount-=1;	break;
	case 0x7c: EA=S-4;												hd6309_ICount-=1;	break;
	case 0x7d: EA=S-3;												hd6309_ICount-=1;	break;
	case 0x7e: EA=S-2;												hd6309_ICount-=1;	break;
	case 0x7f: EA=S-1;												hd6309_ICount-=1;	break;

	case 0x80: EA=X;	X++;										hd6309_ICount-=2;	break;
	case 0x81: EA=X;	X+=2;										hd6309_ICount-=3;	break;
	case 0x82: X--; 	EA=X;										hd6309_ICount-=2;	break;
	case 0x83: X-=2;	EA=X;										hd6309_ICount-=3;	break;
	case 0x84: EA=X;																	break;
	case 0x85: EA=X+SIGNED(B);										hd6309_ICount-=1;	break;
	case 0x86: EA=X+SIGNED(A);										hd6309_ICount-=1;	break;
	case 0x87: EA=X+SIGNED(E);										hd6309_ICount-=1;	break;
	case 0x88: IMMBYTE(EA); 	EA=X+SIGNED(EA);					hd6309_ICount-=1;	break; /* this is a hack to make Vectrex work. It should be hd6309_ICount-=1. Dunno where the cycle was lost :( */
	case 0x89: IMMWORD(ea); 	EA+=X;								hd6309_ICount-=4;	break;
	case 0x8a: EA=X+SIGNED(F);										hd6309_ICount-=1;	break;
	case 0x8b: EA=X+D;												hd6309_ICount-=4;	break;
	case 0x8c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0x8d: IMMWORD(ea); 	EA+=PC; 							hd6309_ICount-=5;	break;
	case 0x8e: EA=X+W;												hd6309_ICount-=4;	break;
	case 0x8f: IMMWORD(ea); 										hd6309_ICount-=5;	break;

	case 0x90: EA=W;								EAD=RM16(EAD);	hd6309_ICount-=3;	break;
	case 0x91: EA=X;	X+=2;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0x92: X--; 	EA=X;						EAD=RM16(EAD);	hd6309_ICount-=5;	break;
	case 0x93: X-=2;	EA=X;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0x94: EA=X;								EAD=RM16(EAD);	hd6309_ICount-=3;	break;
	case 0x95: EA=X+SIGNED(B);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x96: EA=X+SIGNED(A);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x97: EA=X+SIGNED(E);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x98: IMMBYTE(EA); 	EA=X+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x99: IMMWORD(ea); 	EA+=X;				EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0x9a: EA=X+SIGNED(F);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x9b: EA=X+D;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0x9c: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0x9d: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	hd6309_ICount-=8;	break;
	case 0x9e: EA=X+W;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0x9f: IMMWORD(ea); 						EAD=RM16(EAD);	hd6309_ICount-=8;	break;

	case 0xa0: EA=Y;	Y++;										hd6309_ICount-=2;	break;
	case 0xa1: EA=Y;	Y+=2;										hd6309_ICount-=3;	break;
	case 0xa2: Y--; 	EA=Y;										hd6309_ICount-=2;	break;
	case 0xa3: Y-=2;	EA=Y;										hd6309_ICount-=3;	break;
	case 0xa4: EA=Y;																	break;
	case 0xa5: EA=Y+SIGNED(B);										hd6309_ICount-=1;	break;
	case 0xa6: EA=Y+SIGNED(A);										hd6309_ICount-=1;	break;
	case 0xa7: EA=Y+SIGNED(E);										hd6309_ICount-=1;	break;
	case 0xa8: IMMBYTE(EA); 	EA=Y+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xa9: IMMWORD(ea); 	EA+=Y;								hd6309_ICount-=4;	break;
	case 0xaa: EA=Y+SIGNED(F);										hd6309_ICount-=1;	break;
	case 0xab: EA=Y+D;												hd6309_ICount-=4;	break;
	case 0xac: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xad: IMMWORD(ea); 	EA+=PC; 							hd6309_ICount-=5;	break;
	case 0xae: EA=Y+W;												hd6309_ICount-=4;	break;
	case 0xaf: IMMWORD(ea); 										hd6309_ICount-=5;	break;

	case 0xb0: IMMWORD(ea); 	EA+=W;				EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xb1: EA=Y;	Y+=2;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xb2: Y--; 	EA=Y;						EAD=RM16(EAD);	hd6309_ICount-=5;	break;
	case 0xb3: Y-=2;	EA=Y;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xb4: EA=Y;								EAD=RM16(EAD);	hd6309_ICount-=3;	break;
	case 0xb5: EA=Y+SIGNED(B);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xb6: EA=Y+SIGNED(A);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xb7: EA=Y+SIGNED(E);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xb8: IMMBYTE(EA); 	EA=Y+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xb9: IMMWORD(ea); 	EA+=Y;				EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xba: EA=Y+SIGNED(F);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xbb: EA=Y+D;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xbc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xbd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	hd6309_ICount-=8;	break;
	case 0xbe: EA=Y+W;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xbf: IMMWORD(ea); 						EAD=RM16(EAD);	hd6309_ICount-=8;	break;

	case 0xc0: EA=U;			U++;								hd6309_ICount-=2;	break;
	case 0xc1: EA=U;			U+=2;								hd6309_ICount-=3;	break;
	case 0xc2: U--; 			EA=U;								hd6309_ICount-=2;	break;
	case 0xc3: U-=2;			EA=U;								hd6309_ICount-=3;	break;
	case 0xc4: EA=U;																	break;
	case 0xc5: EA=U+SIGNED(B);										hd6309_ICount-=1;	break;
	case 0xc6: EA=U+SIGNED(A);										hd6309_ICount-=1;	break;
	case 0xc7: EA=U+SIGNED(E);										hd6309_ICount-=1;	break;
	case 0xc8: IMMBYTE(EA); 	EA=U+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xc9: IMMWORD(ea); 	EA+=U;								hd6309_ICount-=4;	break;
	case 0xca: EA=U+SIGNED(F);										hd6309_ICount-=1;	break;
	case 0xcb: EA=U+D;												hd6309_ICount-=4;	break;
	case 0xcc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xcd: IMMWORD(ea); 	EA+=PC; 							hd6309_ICount-=5;	break;
	case 0xce: EA=U+W;												hd6309_ICount-=4;	break;
	case 0xcf: IMMWORD(ea); 										hd6309_ICount-=5;	break;

	case 0xd0: EA=W;	W+=2;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xd1: EA=U;	U+=2;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xd2: U--; 	EA=U;						EAD=RM16(EAD);	hd6309_ICount-=5;	break;
	case 0xd3: U-=2;	EA=U;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xd4: EA=U;								EAD=RM16(EAD);	hd6309_ICount-=3;	break;
	case 0xd5: EA=U+SIGNED(B);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xd6: EA=U+SIGNED(A);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xd7: EA=U+SIGNED(E);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xd8: IMMBYTE(EA); 	EA=U+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xd9: IMMWORD(ea); 	EA+=U;				EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xda: EA=U+SIGNED(F);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xdb: EA=U+D;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xdc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xdd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	hd6309_ICount-=8;	break;
	case 0xde: EA=U+W;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xdf: IMMWORD(ea); 						EAD=RM16(EAD);	hd6309_ICount-=8;	break;

	case 0xe0: EA=S;	S++;										hd6309_ICount-=2;	break;
	case 0xe1: EA=S;	S+=2;										hd6309_ICount-=3;	break;
	case 0xe2: S--; 	EA=S;										hd6309_ICount-=2;	break;
	case 0xe3: S-=2;	EA=S;										hd6309_ICount-=3;	break;
	case 0xe4: EA=S;																	break;
	case 0xe5: EA=S+SIGNED(B);										hd6309_ICount-=1;	break;
	case 0xe6: EA=S+SIGNED(A);										hd6309_ICount-=1;	break;
	case 0xe7: EA=S+SIGNED(E);										hd6309_ICount-=1;	break;
	case 0xe8: IMMBYTE(EA); 	EA=S+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xe9: IMMWORD(ea); 	EA+=S;								hd6309_ICount-=4;	break;
	case 0xea: EA=S+SIGNED(F);										hd6309_ICount-=1;	break;
	case 0xeb: EA=S+D;												hd6309_ICount-=4;	break;
	case 0xec: IMMBYTE(EA); 	EA=PC+SIGNED(EA);					hd6309_ICount-=1;	break;
	case 0xed: IMMWORD(ea); 	EA+=PC; 							hd6309_ICount-=5;	break;
	case 0xee: EA=S+W;												hd6309_ICount-=4;	break;
	case 0xef: IMMWORD(ea); 										hd6309_ICount-=5;	break;

	case 0xf0: W-=2;	EA=W;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xf1: EA=S;	S+=2;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xf2: S--; 	EA=S;						EAD=RM16(EAD);	hd6309_ICount-=5;	break;
	case 0xf3: S-=2;	EA=S;						EAD=RM16(EAD);	hd6309_ICount-=6;	break;
	case 0xf4: EA=S;								EAD=RM16(EAD);	hd6309_ICount-=3;	break;
	case 0xf5: EA=S+SIGNED(B);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xf6: EA=S+SIGNED(A);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xf7: EA=S+SIGNED(E);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xf8: IMMBYTE(EA); 	EA=S+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xf9: IMMWORD(ea); 	EA+=S;				EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xfa: EA=S+SIGNED(F);						EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xfb: EA=S+D;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xfc: IMMBYTE(EA); 	EA=PC+SIGNED(EA);	EAD=RM16(EAD);	hd6309_ICount-=4;	break;
	case 0xfd: IMMWORD(ea); 	EA+=PC; 			EAD=RM16(EAD);	hd6309_ICount-=8;	break;
	case 0xfe: EA=S+W;								EAD=RM16(EAD);	hd6309_ICount-=7;	break;
	case 0xff: IMMWORD(ea); 						EAD=RM16(EAD);	hd6309_ICount-=8;	break;
	}
}

