#include <stdio.h>
#include "m68000.h"
#include "memory.h"
#include "mamedbg.h"

static UINT8 m68k_reg_layout[] = {
	M68K_PC, M68K_ISP, -1,
	M68K_SR, M68K_USP, -1,
	M68K_D0, M68K_A0, -1,
	M68K_D1, M68K_A1, -1,
	M68K_D2, M68K_A2, -1,
	M68K_D3, M68K_A3, -1,
	M68K_D4, M68K_A4, -1,
	M68K_D5, M68K_A5, -1,
	M68K_D6, M68K_A6, -1,
	M68K_D7, M68K_A7, 0
};

static UINT8 m68k_win_layout[] = {
	48, 0,32,13,	/* register window (top right) */
	 0, 0,47,13,	/* disassembler window (top left) */
	 0,14,47, 8,	/* memory #1 window (left, middle) */
	48,14,32, 8,	/* memory #2 window (right, middle) */
	 0,23,80, 1 	/* command line window (bottom rows) */
};

void m68000_reset(void *param)
{
	m68k_pulse_reset(param);
}

void m68000_exit(void)
{
	/* nothing to do ? */
}

int m68000_execute(int cycles)
{
	return m68k_execute(cycles);
}

unsigned m68000_get_context(void *dst)
{
	if( dst )
		m68k_get_context(dst);
	return sizeof(m68k_cpu_context);
}

void m68000_set_context(void *src)
{
	if( src )
		m68k_set_context(src);
}

unsigned m68000_get_pc(void)
{
	return m68k_peek_pc();
}

void m68000_set_pc(unsigned val)
{
	m68k_poke_pc(val);
}

unsigned m68000_get_sp(void)
{
	return m68k_peek_isp();
}

void m68000_set_sp(unsigned val)
{
	m68k_poke_isp(val);
}

unsigned m68000_get_reg(int regnum)
{
    switch( regnum )
    {
		case M68K_PC: return m68k_peek_pc();
        case M68K_ISP: return m68k_peek_isp();
        case M68K_USP: return m68k_peek_usp();
		case M68K_SR: return m68k_peek_sr();
        case M68K_VBR: return 0; /* missing m68k_peek_vbr(); */
		case M68K_SFC: return 0; /* missing m68k_peek_sfc(); */
		case M68K_DFC: return 0; /* missing m68k_peek_dfc(); */
		case M68K_D0: return m68k_peek_dr(0);
		case M68K_D1: return m68k_peek_dr(1);
		case M68K_D2: return m68k_peek_dr(2);
		case M68K_D3: return m68k_peek_dr(3);
		case M68K_D4: return m68k_peek_dr(4);
		case M68K_D5: return m68k_peek_dr(5);
		case M68K_D6: return m68k_peek_dr(6);
		case M68K_D7: return m68k_peek_dr(7);
		case M68K_A0: return m68k_peek_ar(0);
		case M68K_A1: return m68k_peek_ar(1);
		case M68K_A2: return m68k_peek_ar(2);
		case M68K_A3: return m68k_peek_ar(3);
		case M68K_A4: return m68k_peek_ar(4);
		case M68K_A5: return m68k_peek_ar(5);
		case M68K_A6: return m68k_peek_ar(6);
		case M68K_A7: return m68k_peek_ar(7);
		case REG_PREVIOUSPC: return m68k_peek_ppc();
/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
		default:
			if( regnum < REG_SP_CONTENTS )
			{
				unsigned offset = m68k_peek_isp() + 4 * (REG_SP_CONTENTS - regnum);
				if( offset < 0xfffffd )
					return cpu_readmem24_dword( offset );
			}
    }
    return 0;
}

void m68000_set_reg(int regnum, unsigned val)
{
    switch( regnum )
    {
		case M68K_PC: m68k_poke_pc(val); break;
		case M68K_ISP: m68k_poke_isp(val); break;
		case M68K_USP: m68k_poke_usp(val); break;
		case M68K_SR: m68k_poke_sr(val); break;
		case M68K_VBR: /* missing m68k_poke_vbr(val); */ break;
		case M68K_SFC: /* missing m68k_poke_sfc(val); */ break;
		case M68K_DFC: /* missing m68k_poke_dfc(val); */ break;
		case M68K_D0: m68k_poke_dr(0,val); break;
		case M68K_D1: m68k_poke_dr(1,val); break;
		case M68K_D2: m68k_poke_dr(2,val); break;
		case M68K_D3: m68k_poke_dr(3,val); break;
		case M68K_D4: m68k_poke_dr(4,val); break;
		case M68K_D5: m68k_poke_dr(5,val); break;
		case M68K_D6: m68k_poke_dr(6,val); break;
		case M68K_D7: m68k_poke_dr(7,val); break;
		case M68K_A0: m68k_poke_ar(0,val); break;
		case M68K_A1: m68k_poke_ar(1,val); break;
		case M68K_A2: m68k_poke_ar(2,val); break;
		case M68K_A3: m68k_poke_ar(3,val); break;
		case M68K_A4: m68k_poke_ar(4,val); break;
		case M68K_A5: m68k_poke_ar(5,val); break;
		case M68K_A6: m68k_poke_ar(6,val); break;
		case M68K_A7: m68k_poke_ar(7,val); break;
/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
		default:
			if( regnum < REG_SP_CONTENTS )
			{
				unsigned offset = m68k_peek_isp() + 4 * (REG_SP_CONTENTS - regnum);
				if( offset < 0xfffffd )
					cpu_writemem24_dword( offset, val );
			}
    }
}

void m68000_set_nmi_line(int state)
{
	switch(state)
	{
		case CLEAR_LINE:
			m68k_clear_irq(7);
			return;
		case ASSERT_LINE:
			m68k_assert_irq(7);
			return;
		default:
			m68k_assert_irq(7);
			return;
	}
}

void m68000_set_irq_line(int irqline, int state)
{
	switch(state)
	{
		case CLEAR_LINE:
			m68k_clear_irq(irqline);
			return;
		case ASSERT_LINE:
			m68k_assert_irq(irqline);
			return;
		default:
			m68k_assert_irq(irqline);
			return;
	}
}

void m68000_set_irq_callback(int (*callback)(int irqline))
{
	m68k_set_int_ack_callback(callback);
}

/****************************************************************************
 * Return a formatted string for a register
 ****************************************************************************/

const char *m68000_info(void *context, int regnum)
{
	static char buffer[32][47+1];
	static int which = 0;
	m68k_cpu_context *r = context;

	which = ++which % 32;
	buffer[which][0] = '\0';
	if( !context )
	{
		static m68k_cpu_context tmp;
		m68k_get_context(&tmp);
		r = &tmp;
	}

	switch( regnum )
	{
		case CPU_INFO_REG+M68K_PC:	sprintf(buffer[which], "PC :%06X", r->pc); break;
		case CPU_INFO_REG+M68K_ISP: sprintf(buffer[which], "ISP:%08X", r->isp); break;
		case CPU_INFO_REG+M68K_SR:  sprintf(buffer[which], "SR :%08X", r->sr); break;
		case CPU_INFO_REG+M68K_USP: sprintf(buffer[which], "USP:%08X", r->usp); break;
		case CPU_INFO_REG+M68K_VBR: sprintf(buffer[which], "VBR:%08X", r->vbr); break;
		case CPU_INFO_REG+M68K_SFC: sprintf(buffer[which], "SFC:%08X", r->sfc); break;
		case CPU_INFO_REG+M68K_DFC: sprintf(buffer[which], "DFC:%08X", r->dfc); break;
		case CPU_INFO_REG+M68K_D0:	sprintf(buffer[which], "D0 :%08X", r->d[0]); break;
		case CPU_INFO_REG+M68K_D1:	sprintf(buffer[which], "D1 :%08X", r->d[1]); break;
		case CPU_INFO_REG+M68K_D2:	sprintf(buffer[which], "D2 :%08X", r->d[2]); break;
		case CPU_INFO_REG+M68K_D3:	sprintf(buffer[which], "D3 :%08X", r->d[3]); break;
		case CPU_INFO_REG+M68K_D4:	sprintf(buffer[which], "D4 :%08X", r->d[4]); break;
		case CPU_INFO_REG+M68K_D5:	sprintf(buffer[which], "D5 :%08X", r->d[5]); break;
		case CPU_INFO_REG+M68K_D6:	sprintf(buffer[which], "D6 :%08X", r->d[6]); break;
		case CPU_INFO_REG+M68K_D7:	sprintf(buffer[which], "D7 :%08X", r->d[7]); break;
		case CPU_INFO_REG+M68K_A0:	sprintf(buffer[which], "A0 :%08X", r->a[0]); break;
		case CPU_INFO_REG+M68K_A1:	sprintf(buffer[which], "A1 :%08X", r->a[1]); break;
		case CPU_INFO_REG+M68K_A2:	sprintf(buffer[which], "A2 :%08X", r->a[2]); break;
		case CPU_INFO_REG+M68K_A3:	sprintf(buffer[which], "A3 :%08X", r->a[3]); break;
		case CPU_INFO_REG+M68K_A4:	sprintf(buffer[which], "A4 :%08X", r->a[4]); break;
		case CPU_INFO_REG+M68K_A5:	sprintf(buffer[which], "A5 :%08X", r->a[5]); break;
		case CPU_INFO_REG+M68K_A6:	sprintf(buffer[which], "A6 :%08X", r->a[6]); break;
		case CPU_INFO_REG+M68K_A7:	sprintf(buffer[which], "A7 :%08X", r->a[7]); break;
        case CPU_INFO_FLAGS:
			sprintf(buffer[which], "%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c%c",
				r->sr & 0x8000 ? 'T':'.',
				r->sr & 0x4000 ? '?':'.',
				r->sr & 0x2000 ? 'S':'.',
				r->sr & 0x1000 ? '?':'.',
				r->sr & 0x0800 ? '?':'.',
				r->sr & 0x0400 ? 'I':'.',
				r->sr & 0x0200 ? 'I':'.',
				r->sr & 0x0100 ? 'I':'.',
				r->sr & 0x0080 ? '?':'.',
				r->sr & 0x0040 ? '?':'.',
				r->sr & 0x0020 ? '?':'.',
				r->sr & 0x0010 ? 'X':'.',
				r->sr & 0x0008 ? 'N':'.',
				r->sr & 0x0004 ? 'Z':'.',
				r->sr & 0x0002 ? 'V':'.',
				r->sr & 0x0001 ? 'C':'.');
            break;
		case CPU_INFO_NAME: return "68000";
		case CPU_INFO_FAMILY: return "Motorola 68K";
		case CPU_INFO_VERSION: return "2.1";
		case CPU_INFO_FILE: return __FILE__;
		case CPU_INFO_CREDITS: return "Copyright 1999 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
		case CPU_INFO_REG_LAYOUT: return (const char*)m68k_reg_layout;
        case CPU_INFO_WIN_LAYOUT: return (const char*)m68k_win_layout;
    }
	return buffer[which];
}

unsigned m68000_dasm(char *buffer, unsigned pc)
{
	change_pc24(pc);
#ifdef MAME_DEBUG
	return m68k_disassemble( buffer, pc );
#else
	sprintf( buffer, "$%04X", cpu_readop16(pc) );
	return 2;
#endif
}

/****************************************************************************
 * M68010 section
 ****************************************************************************/
#if HAS_M68010
void m68010_reset(void *param) { m68000_reset(param); }
void m68010_exit(void) { m68000_exit(); }
int  m68010_execute(int cycles) { return m68000_execute(cycles); }
unsigned m68010_get_context(void *dst) { return m68000_get_context(dst); }
void m68010_set_context(void *src) { m68000_set_context(src); }
unsigned m68010_get_pc(void) { return m68000_get_pc(); }
void m68010_set_pc(unsigned val) { m68000_set_pc(val); }
unsigned m68010_get_sp(void) { return m68000_get_sp(); }
void m68010_set_sp(unsigned val) { m68000_set_sp(val); }
unsigned m68010_get_reg(int regnum) { return m68000_get_reg(regnum); }
void m68010_set_reg(int regnum, unsigned val) { m68000_set_reg(regnum,val); }
void m68010_set_nmi_line(int state) { m68000_set_nmi_line(state); }
void m68010_set_irq_line(int irqline, int state)  { m68000_set_irq_line(irqline,state); }
void m68010_set_irq_callback(int (*callback)(int irqline))	{ m68000_set_irq_callback(callback); }
const char *m68010_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "68010";
	}
	return m68000_info(context,regnum);
}

unsigned m68010_dasm(char *buffer, unsigned pc)
{
	change_pc24(pc);
#ifdef MAME_DEBUG
    return m68k_disassemble(buffer, pc);
#else
	sprintf( buffer, "$%04X", cpu_readop16(pc) );
	return 2;
#endif
}
#endif

/****************************************************************************
 * M680EC20 section
 ****************************************************************************/
#if HAS_M68EC020
void m68ec020_reset(void *param) { m68k_set_cpu_mode(M68K_CPU_MODE_68EC020); m68000_reset(param); }
void m68ec020_exit(void) { m68000_exit(); }
int  m68ec020_execute(int cycles) { return m68000_execute(cycles); }
unsigned m68ec020_get_context(void *dst) { return m68000_get_context(dst); }
void m68ec020_set_context(void *src) { m68000_set_context(src); }
unsigned m68ec020_get_pc(void) { return m68000_get_pc(); }
void m68ec020_set_pc(unsigned val) { m68000_set_pc(val); }
unsigned m68ec020_get_sp(void) { return m68000_get_sp(); }
void m68ec020_set_sp(unsigned val) { m68000_set_sp(val); }
unsigned m68ec020_get_reg(int regnum) { return m68000_get_reg(regnum); }
void m68ec020_set_reg(int regnum, unsigned val) { m68000_set_reg(regnum,val); }
void m68ec020_set_nmi_line(int state) { m68000_set_nmi_line(state); }
void m68ec020_set_irq_line(int irqline, int state)  { m68000_set_irq_line(irqline,state); }
void m68ec020_set_irq_callback(int (*callback)(int irqline))	{ m68000_set_irq_callback(callback); }
const char *m68ec020_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "680EC20";
	}
	return m68000_info(context,regnum);
}

unsigned m68ec020_dasm(char *buffer, unsigned pc)
{
    change_pc24(pc);
#ifdef MAME_DEBUG
    return m68k_disassemble(buffer, pc);
#else
	sprintf( buffer, "$%04X", cpu_readop16(pc) );
	return 2;
#endif
}
#endif

/****************************************************************************
 * M68020 section
 ****************************************************************************/
#if HAS_M68020
void m68020_reset(void *param) { m68k_set_cpu_mode(M68K_CPU_MODE_68020); m68000_reset(param); }
void m68020_exit(void) { m68000_exit(); }
int  m68020_execute(int cycles) { return m68000_execute(cycles); }
unsigned m68020_get_context(void *dst) { return m68000_get_context(dst); }
void m68020_set_context(void *src) { m68000_set_context(src); }
unsigned m68020_get_pc(void) { return m68000_get_pc(); }
void m68020_set_pc(unsigned val) { m68000_set_pc(val); }
unsigned m68020_get_sp(void) { return m68000_get_sp(); }
void m68020_set_sp(unsigned val) { m68000_set_sp(val); }
unsigned m68020_get_reg(int regnum) { return m68000_get_reg(regnum); }
void m68020_set_reg(int regnum, unsigned val) { m68000_set_reg(regnum,val); }
void m68020_set_nmi_line(int state) { m68000_set_nmi_line(state); }
void m68020_set_irq_line(int irqline, int state)  { m68000_set_irq_line(irqline,state); }
void m68020_set_irq_callback(int (*callback)(int irqline))	{ m68000_set_irq_callback(callback); }
const char *m68020_info(void *context, int regnum)
{
	switch( regnum )
	{
		case CPU_INFO_NAME: return "68020";
	}
	return m68000_info(context,regnum);
}

unsigned m68020_dasm(char *buffer, unsigned pc)
{
    change_pc32(pc);
#ifdef MAME_DEBUG
    return m68k_disassemble(buffer, pc);
#else
	sprintf( buffer, "$%04X", cpu_readop16(pc) );
	return 2;
#endif
}
#endif
