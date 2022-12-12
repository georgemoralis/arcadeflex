/*****************************************************************************
 *
 *	 m6502.c
 *	 Portable 6502/65c02/65sc02/6510/n2a03 emulator V1.2
 *
 *	 Copyright (c) 1998,1999,2000 Juergen Buchmueller, all rights reserved.
 *	 65sc02 core Copyright (c) 2000 Peter Trauner.
 *
 *	 - This source code is released as freeware for non-commercial purposes.
 *	 - You are free to use and redistribute this code in modified or
 *	   unmodified form, provided you list me in the credits.
 *	 - If you modify this source code, you must add a notice to each modified
 *	   source file that it has been changed.  If you're a nice person, you
 *	   will clearly mark each change too.  :)
 *	 - If you wish to use this for commercial purposes, please contact me at
 *	   pullmoll@t-online.de
 *	 - The author of this copywritten work reserves the right to change the
 *	   terms of its usage and license at any time, including retroactively
 *	 - This entire notice must remain in the source code.
 *
 *****************************************************************************/
/* 2.February 2000 PeT added 65sc02 subtype */
/* 10.March   2000 PeT added 6502 set overflow input line */

#include <stdio.h>
#include "driver.h"
#include "state.h"
#include "mamedbg.h"
#include "m6502.h"
#include "ops02.h"

extern FILE * errorlog;

#define VERBOSE 0

#if VERBOSE
#define LOG(x)	if( errorlog ) fprintf x
#else
#define LOG(x)
#endif

/* Layout of the registers in the debugger */
static UINT8 m6502_reg_layout[] = {
	M6502_PC, M6502_S, M6502_P, M6502_A, M6502_X, M6502_Y, -1,
	M6502_EA, M6502_ZP, M6502_NMI_STATE, M6502_IRQ_STATE, M6502_SO_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 m6502_win_layout[] = {
	25, 0,55, 2,	/* register window (top, right rows) */
	 0, 0,24,22,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};

/****************************************************************************
 * The 6502 registers.
 ****************************************************************************/
typedef struct
{
	UINT8	subtype;		/* currently selected cpu sub type */
	void	(**insn)(void); /* pointer to the function pointer table */
	PAIR	ppc;			/* previous program counter */
	PAIR	pc; 			/* program counter */
	PAIR	sp; 			/* stack pointer (always 100 - 1FF) */
	PAIR	zp; 			/* zero page address */
	PAIR	ea; 			/* effective address */
	UINT8	a;				/* Accumulator */
	UINT8	x;				/* X index register */
	UINT8	y;				/* Y index register */
	UINT8	p;				/* Processor status */
	UINT8	pending_irq;	/* nonzero if an IRQ is pending */
	UINT8	after_cli;		/* pending IRQ and last insn cleared I */
	UINT8	nmi_state;
	UINT8	irq_state;
	UINT8   so_state;
	int 	(*irq_callback)(int irqline);	/* IRQ callback */
}	m6502_Regs;

int m6502_ICount = 0;

static m6502_Regs m6502;

/***************************************************************
 * include the opcode macros, functions and tables
 ***************************************************************/
#include "t6502.c"

#if HAS_M65C02
#include "t65c02.c"
#endif

#if HAS_M65SC02
#include "t65sc02.c"
#endif

#if HAS_M6510
#include "t6510.c"
#endif

#if HAS_N2A03
#include "tn2a03.c"
#endif

/*****************************************************************************
 *
 *		6502 CPU interface functions
 *
 *****************************************************************************/

void m6502_reset(void *param)
{
	m6502.subtype = SUBTYPE_6502;
	m6502.insn = insn6502;

	/* wipe out the rest of the m6502 structure */
	/* read the reset vector into PC */
	PCL = RDMEM(M6502_RST_VEC);
	PCH = RDMEM(M6502_RST_VEC+1);

	m6502.sp.d = 0x01ff;	/* stack pointer starts at page 1 offset FF */
	m6502.p = F_T|F_I|F_Z;	/* set T, I and Z flags */
	m6502.pending_irq = 0;	/* nonzero if an IRQ is pending */
	m6502.after_cli = 0;	/* pending IRQ and last insn cleared I */
	m6502.irq_callback = NULL;

	change_pc16(PCD);
}

void m6502_exit(void)
{
	/* nothing to do yet */
}

unsigned m6502_get_context (void *dst)
{
	if( dst )
		*(m6502_Regs*)dst = m6502;
	return sizeof(m6502_Regs);
}

void m6502_set_context (void *src)
{
	if( src )
	{
		m6502 = *(m6502_Regs*)src;
		change_pc(PCD);
	}
}

unsigned m6502_get_pc (void)
{
	return PCD;
}

void m6502_set_pc (unsigned val)
{
	PCW = val;
	change_pc(PCD);
}

unsigned m6502_get_sp (void)
{
	return S;
}

void m6502_set_sp (unsigned val)
{
	S = val;
}

unsigned m6502_get_reg (int regnum)
{
	switch( regnum )
	{
		case M6502_PC: return m6502.pc.w.l;
		case M6502_S: return m6502.sp.b.l;
		case M6502_P: return m6502.p;
		case M6502_A: return m6502.a;
		case M6502_X: return m6502.x;
		case M6502_Y: return m6502.y;
		case M6502_EA: return m6502.ea.w.l;
		case M6502_ZP: return m6502.zp.w.l;
		case M6502_NMI_STATE: return m6502.nmi_state;
		case M6502_IRQ_STATE: return m6502.irq_state;
		case M6502_SO_STATE: return m6502.so_state;
		case M6502_SUBTYPE: return m6502.subtype;
		case REG_PREVIOUSPC: return m6502.ppc.w.l;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < 0x1ff )
					return RDMEM( offset ) | ( RDMEM( offset + 1 ) << 8 );
			}
	}
	return 0;
}

void m6502_set_reg (int regnum, unsigned val)
{
	switch( regnum )
	{
		case M6502_PC: m6502.pc.w.l = val; break;
		case M6502_S: m6502.sp.b.l = val; break;
		case M6502_P: m6502.p = val; break;
		case M6502_A: m6502.a = val; break;
		case M6502_X: m6502.x = val; break;
		case M6502_Y: m6502.y = val; break;
		case M6502_EA: m6502.ea.w.l = val; break;
		case M6502_ZP: m6502.zp.w.l = val; break;
		case M6502_NMI_STATE: m6502_set_nmi_line( val ); break;
		case M6502_IRQ_STATE: m6502_set_irq_line( 0, val ); break;
		case M6502_SO_STATE: m6502_set_irq_line( M6502_SET_OVERFLOW, val ); break;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				unsigned offset = S + 2 * (REG_SP_CONTENTS - regnum);
				if( offset < 0x1ff )
				{
					WRMEM( offset, val & 0xfff );
					WRMEM( offset + 1, (val >> 8) & 0xff );
				}
			}
	}
}

INLINE void take_irq(void)
{
	if( !(P & F_I) )
	{
		EAD = M6502_IRQ_VEC;
		m6502_ICount -= 7;
		PUSH(PCH);
		PUSH(PCL);
		PUSH(P & ~F_B);
		P = (P & ~F_D) | F_I;		/* knock out D and set I flag */
		PCL = RDMEM(EAD);
		PCH = RDMEM(EAD+1);
		LOG((errorlog,"M6502#%d takes IRQ ($%04x)\n", cpu_getactivecpu(), PCD));
		/* call back the cpuintrf to let it clear the line */
		if (m6502.irq_callback) (*m6502.irq_callback)(0);
		change_pc16(PCD);
	}
	m6502.pending_irq = 0;
}

int m6502_execute(int cycles)
{
	m6502_ICount = cycles;

	change_pc16(PCD);

	do
	{
		UINT8 op;
		PPC = PCD;

		CALL_MAME_DEBUG;

		op = RDOP();
		/* if an irq is pending, take it now */
		if( m6502.pending_irq && op == 0x78 )
			take_irq();

		(*m6502.insn[op])();

		/* check if the I flag was just reset (interrupts enabled) */
		if( m6502.after_cli )
		{
			LOG((errorlog,"M6502#%d after_cli was >0", cpu_getactivecpu()));
			m6502.after_cli = 0;
			if (m6502.irq_state != CLEAR_LINE)
			{
				LOG((errorlog,": irq line is asserted: set pending IRQ\n"));
				m6502.pending_irq = 1;
			}
			else
			{
				LOG((errorlog,": irq line is clear\n"));
			}
		}
		else
		if( m6502.pending_irq )
			take_irq();

	} while (m6502_ICount > 0);

	return cycles - m6502_ICount;
}

void m6502_set_nmi_line(int state)
{
	if (m6502.nmi_state == state) return;
	m6502.nmi_state = state;
	if( state != CLEAR_LINE )
	{
		LOG((errorlog, "M6502#%d set_nmi_line(ASSERT)\n", cpu_getactivecpu()));
		EAD = M6502_NMI_VEC;
		m6502_ICount -= 7;
		PUSH(PCH);
		PUSH(PCL);
		PUSH(P & ~F_B);
		P = (P & ~F_D) | F_I;		/* knock out D and set I flag */
		PCL = RDMEM(EAD);
		PCH = RDMEM(EAD+1);
		LOG((errorlog,"M6502#%d takes NMI ($%04x)\n", cpu_getactivecpu(), PCD));
		change_pc16(PCD);
	}
}

void m6502_set_irq_line(int irqline, int state)
{
	if( irqline == M6502_SET_OVERFLOW )
	{
		if( m6502.so_state && !state )
		{
			LOG((errorlog, "M6502#%d set overflow\n", cpu_getactivecpu()));
			P|=F_V;
		}
		m6502.so_state=state;
		return;
	}
	m6502.irq_state = state;
	if( state != CLEAR_LINE )
	{
		LOG((errorlog, "M6502#%d set_irq_line(ASSERT)\n", cpu_getactivecpu()));
		m6502.pending_irq = 1;
	}
}

void m6502_set_irq_callback(int (*callback)(int))
{
	m6502.irq_callback = callback;
}

void m6502_state_save(void *file)
{
	int cpu = cpu_getactivecpu();
	state_save_UINT8(file,"m6502",cpu,"TYPE",&m6502.subtype,1);
	/* insn is set at restore since it's a pointer */
	state_save_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
	state_save_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
	state_save_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
	state_save_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
	state_save_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
	state_save_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
	state_save_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
	state_save_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
	state_save_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
	state_save_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
	state_save_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
}

void m6502_state_load(void *file)
{
	int cpu = cpu_getactivecpu();
	state_load_UINT8(file,"m6502",cpu,"TYPE",&m6502.subtype,1);
	/* insn is set at restore since it's a pointer */
	switch (m6502.subtype)
	{
#if HAS_M65C02
		case SUBTYPE_65C02:
			m6502.insn = insn65c02;
			break;
#endif
#if HAS_M65SC02
		case SUBTYPE_65SC02:
			m6502.insn = insn65sc02;
			break;
#endif
#if HAS_M6510
		case SUBTYPE_6510:
			m6502.insn = insn6510;
			break;
#endif
		default:
			m6502.insn = insn6502;
			break;
	}
	state_load_UINT16(file,"m6502",cpu,"PC",&m6502.pc.w.l,2);
	state_load_UINT16(file,"m6502",cpu,"SP",&m6502.sp.w.l,2);
	state_load_UINT8(file,"m6502",cpu,"P",&m6502.p,1);
	state_load_UINT8(file,"m6502",cpu,"A",&m6502.a,1);
	state_load_UINT8(file,"m6502",cpu,"X",&m6502.x,1);
	state_load_UINT8(file,"m6502",cpu,"Y",&m6502.y,1);
	state_load_UINT8(file,"m6502",cpu,"PENDING",&m6502.pending_irq,1);
	state_load_UINT8(file,"m6502",cpu,"AFTER_CLI",&m6502.after_cli,1);
	state_load_UINT8(file,"m6502",cpu,"NMI_STATE",&m6502.nmi_state,1);
	state_load_UINT8(file,"m6502",cpu,"IRQ_STATE",&m6502.irq_state,1);
	state_load_UINT8(file,"m6502",cpu,"SO_STATE",&m6502.so_state,1);
}

/****************************************************************************
 * Return a formatted string for a register
 ****************************************************************************/
const char *m6502_info(void *context, int regnum)
{
	static char buffer[16][47+1];
	static int which = 0;
	m6502_Regs *r = context;

	which = ++which % 16;
	buffer[which][0] = '\0';
	if( !context )
		r = &m6502;

	switch( regnum )
	{
		case CPU_INFO_REG+M6502_PC: sprintf(buffer[which], "PC:%04X", r->pc.w.l); break;
		case CPU_INFO_REG+M6502_S: sprintf(buffer[which], "S:%02X", r->sp.b.l); break;
		case CPU_INFO_REG+M6502_P: sprintf(buffer[which], "P:%02X", r->p); break;
		case CPU_INFO_REG+M6502_A: sprintf(buffer[which], "A:%02X", r->a); break;
		case CPU_INFO_REG+M6502_X: sprintf(buffer[which], "X:%02X", r->x); break;
		case CPU_INFO_REG+M6502_Y: sprintf(buffer[which], "Y:%02X", r->y); break;
		case CPU_INFO_REG+M6502_EA: sprintf(buffer[which], "EA:%04X", r->ea.w.l); break;
		case CPU_INFO_REG+M6502_ZP: sprintf(buffer[which], "ZP:%03X", r->zp.w.l); break;
		case CPU_INFO_REG+M6502_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
		case CPU_INFO_REG+M6502_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
		case CPU_INFO_REG+M6502_SO_STATE: sprintf(buffer[which], "SO:%X", r->so_state); break;
		case CPU_INFO_FLAGS:
			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
				r->p & 0x80 ? 'N':'.',
				r->p & 0x40 ? 'V':'.',
				r->p & 0x20 ? 'R':'.',
				r->p & 0x10 ? 'B':'.',
				r->p & 0x08 ? 'D':'.',
				r->p & 0x04 ? 'I':'.',
				r->p & 0x02 ? 'Z':'.',
				r->p & 0x01 ? 'C':'.');
			break;
		case CPU_INFO_NAME: return "M6502";
		case CPU_INFO_FAMILY: return "Motorola 6502";
		case CPU_INFO_VERSION: return "1.2";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Copyright (c) 1998 Juergen Buchmueller, all rights reserved.";
		case CPU_INFO_REG_LAYOUT: return (const char*)m6502_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)m6502_win_layout;
	}
	return buffer[which];
}

unsigned m6502_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6502( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

/****************************************************************************
 * 65C02 section
 ****************************************************************************/
#if HAS_M65C02
/* Layout of the registers in the debugger */
static UINT8 m65c02_reg_layout[] = {
	M65C02_A,M65C02_X,M65C02_Y,M65C02_S,M65C02_PC,M65C02_P, -1,
	M65C02_EA,M65C02_ZP,M65C02_NMI_STATE,M65C02_IRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 m65c02_win_layout[] = {
	25, 0,55, 2,	/* register window (top, right rows) */
	 0, 0,24,22,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};


void m65c02_reset (void *param)
{
	m6502_reset(param);
	m6502.subtype = SUBTYPE_65C02;
	m6502.insn = insn65c02;
}
void m65c02_exit  (void) { m6502_exit(); }
int  m65c02_execute(int cycles) { return m6502_execute(cycles); }
unsigned m65c02_get_context (void *dst) { return m6502_get_context(dst); }
void m65c02_set_context (void *src) { m6502_set_context(src); }
unsigned m65c02_get_pc (void) { return m6502_get_pc(); }
void m65c02_set_pc (unsigned val) { m6502_set_pc(val); }
unsigned m65c02_get_sp (void) { return m6502_get_sp(); }
void m65c02_set_sp (unsigned val) { m6502_set_sp(val); }
unsigned m65c02_get_reg (int regnum) { return m6502_get_reg(regnum); }
void m65c02_set_reg (int regnum, unsigned val) { m6502_set_reg(regnum,val); }
void m65c02_set_nmi_line(int state) { m6502_set_nmi_line(state); }
void m65c02_set_irq_line(int irqline, int state) { m6502_set_irq_line(irqline,state); }
void m65c02_set_irq_callback(int (*callback)(int irqline)) { m6502_set_irq_callback(callback); }
void m65c02_state_save(void *file) { m6502_state_save(file); }
void m65c02_state_load(void *file) { m6502_state_load(file); }
const char *m65c02_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "M65C02";
		case CPU_INFO_VERSION: return "1.2";
		case CPU_INFO_REG_LAYOUT: return (const char*)m65c02_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)m65c02_win_layout;
	}
	return m6502_info(context,regnum);
}
unsigned m65c02_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6502( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

#endif

/****************************************************************************
 * 65SC02 section
 ****************************************************************************/
#if HAS_M65SC02
/* Layout of the registers in the debugger */
static UINT8 m65sc02_reg_layout[] = {
	M65SC02_A,M65SC02_X,M65SC02_Y,M65SC02_S,M65SC02_PC,M65SC02_P, -1,
	M65SC02_EA,M65SC02_ZP,M65SC02_NMI_STATE,M65SC02_IRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 m65sc02_win_layout[] = {
	25, 0,55, 2,	/* register window (top, right rows) */
	 0, 0,24,22,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};


void m65sc02_reset (void *param)
{
	m6502_reset(param);
	m6502.subtype = SUBTYPE_65SC02;
	m6502.insn = insn65sc02;
}
void m65sc02_exit  (void) { m6502_exit(); }
int  m65sc02_execute(int cycles) { return m6502_execute(cycles); }
unsigned m65sc02_get_context (void *dst) { return m6502_get_context(dst); }
void m65sc02_set_context (void *src) { m6502_set_context(src); }
unsigned m65sc02_get_pc (void) { return m6502_get_pc(); }
void m65sc02_set_pc (unsigned val) { m6502_set_pc(val); }
unsigned m65sc02_get_sp (void) { return m6502_get_sp(); }
void m65sc02_set_sp (unsigned val) { m6502_set_sp(val); }
unsigned m65sc02_get_reg (int regnum) { return m6502_get_reg(regnum); }
void m65sc02_set_reg (int regnum, unsigned val) { m6502_set_reg(regnum,val); }
void m65sc02_set_nmi_line(int state) { m6502_set_nmi_line(state); }
void m65sc02_set_irq_line(int irqline, int state) { m6502_set_irq_line(irqline,state); }
void m65sc02_set_irq_callback(int (*callback)(int irqline)) { m6502_set_irq_callback(callback); }
void m65sc02_state_save(void *file) { m6502_state_save(file); }
void m65sc02_state_load(void *file) { m6502_state_load(file); }
const char *m65sc02_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "M65SC02";
		case CPU_INFO_FAMILY: return "Metal Oxid Semiconductor MOS 6502";
		case CPU_INFO_VERSION: return "1.0beta";
		case CPU_INFO_CREDITS:
			return "Copyright (c) 1998 Juergen Buchmueller\n"
				"Copyright (c) 2000 Peter Trauner\n"
				"all rights reserved.";
		case CPU_INFO_REG_LAYOUT: return (const char*)m65sc02_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)m65sc02_win_layout;
	}
	return m6502_info(context,regnum);
}
unsigned m65sc02_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6502( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}

#endif

/****************************************************************************
 * 6510 section
 ****************************************************************************/
#if HAS_M6510
/* Layout of the registers in the debugger */
static UINT8 m6510_reg_layout[] = {
	M6510_A,M6510_X,M6510_Y,M6510_S,M6510_PC,M6510_P, -1,
	M6510_EA,M6510_ZP,M6510_NMI_STATE,M6510_IRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 m6510_win_layout[] = {
	25, 0,55, 2,	/* register window (top, right rows) */
	 0, 0,24,22,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};

void m6510_reset (void *param)
{
	m6502_reset(param);
	m6502.subtype = SUBTYPE_6510;
	m6502.insn = insn6510;
}
void m6510_exit  (void) { m6502_exit(); }
int  m6510_execute(int cycles) { return m6502_execute(cycles); }
unsigned m6510_get_context (void *dst) { return m6502_get_context(dst); }
void m6510_set_context (void *src) { m6502_set_context(src); }
unsigned m6510_get_pc (void) { return m6502_get_pc(); }
void m6510_set_pc (unsigned val) { m6502_set_pc(val); }
unsigned m6510_get_sp (void) { return m6502_get_sp(); }
void m6510_set_sp (unsigned val) { m6502_set_sp(val); }
unsigned m6510_get_reg (int regnum) { return m6502_get_reg(regnum); }
void m6510_set_reg (int regnum, unsigned val) { m6502_set_reg(regnum,val); }
void m6510_set_nmi_line(int state) { m6502_set_nmi_line(state); }
void m6510_set_irq_line(int irqline, int state) { m6502_set_irq_line(irqline,state); }
void m6510_set_irq_callback(int (*callback)(int irqline)) { m6502_set_irq_callback(callback); }
void m6510_state_save(void *file) { m6502_state_save(file); }
void m6510_state_load(void *file) { m6502_state_load(file); }
const char *m6510_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "M6510";
		case CPU_INFO_VERSION: return "1.2";
		case CPU_INFO_REG_LAYOUT: return (const char*)m6510_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)m6510_win_layout;
	}
	return m6502_info(context,regnum);
}

unsigned m6510_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6502( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}
#endif

/****************************************************************************
 * 2A03 section
 ****************************************************************************/
#if HAS_N2A03
/* Layout of the registers in the debugger */
static UINT8 n2a03_reg_layout[] = {
	N2A03_A,N2A03_X,N2A03_Y,N2A03_S,N2A03_PC,N2A03_P, -1,
	N2A03_EA,N2A03_ZP,N2A03_NMI_STATE,N2A03_IRQ_STATE, 0
};

/* Layout of the debugger windows x,y,w,h */
static UINT8 n2a03_win_layout[] = {
	25, 0,55, 2,	/* register window (top, right rows) */
	 0, 0,24,22,	/* disassembler window (left colums) */
	25, 3,55, 9,	/* memory #1 window (right, upper middle) */
	25,13,55, 9,	/* memory #2 window (right, lower middle) */
	 0,23,80, 1,	/* command line window (bottom rows) */
};

void n2a03_reset (void *param)
{
	m6502_reset(param);
	m6502.subtype = SUBTYPE_2A03;
	m6502.insn = insn2a03;
}
void n2a03_exit  (void) { m6502_exit(); }
int  n2a03_execute(int cycles) { return m6502_execute(cycles); }
unsigned n2a03_get_context (void *dst) { return m6502_get_context(dst); }
void n2a03_set_context (void *src) { m6502_set_context(src); }
unsigned n2a03_get_pc (void) { return m6502_get_pc(); }
void n2a03_set_pc (unsigned val) { m6502_set_pc(val); }
unsigned n2a03_get_sp (void) { return m6502_get_sp(); }
void n2a03_set_sp (unsigned val) { m6502_set_sp(val); }
unsigned n2a03_get_reg (int regnum) { return m6502_get_reg(regnum); }
void n2a03_set_reg (int regnum, unsigned val) { m6502_set_reg(regnum,val); }
void n2a03_set_nmi_line(int state) { m6502_set_nmi_line(state); }
void n2a03_set_irq_line(int irqline, int state) { m6502_set_irq_line(irqline,state); }
void n2a03_set_irq_callback(int (*callback)(int irqline)) { m6502_set_irq_callback(callback); }
void n2a03_state_save(void *file) { m6502_state_save(file); }
void n2a03_state_load(void *file) { m6502_state_load(file); }
const char *n2a03_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "N2A03";
		case CPU_INFO_VERSION: return "1.0";
		case CPU_INFO_REG_LAYOUT: return (const char*)n2a03_reg_layout;
		case CPU_INFO_WIN_LAYOUT: return (const char*)n2a03_win_layout;
	}
	return m6502_info(context,regnum);
}

/* The N2A03 is integrally tied to its PSG (they're on the same die).
   Bit 7 of address $4011 (the PSG's DPCM control register), when set,
   causes an IRQ to be generated.  This function allows the IRQ to be called
   from the PSG core when such an occasion arises. */
void n2a03_irq(void)
{
  take_irq();
}

unsigned n2a03_dasm(char *buffer, unsigned pc)
{
#ifdef MAME_DEBUG
	return Dasm6502( buffer, pc );
#else
	sprintf( buffer, "$%02X", cpu_readop(pc) );
	return 1;
#endif
}
#endif

