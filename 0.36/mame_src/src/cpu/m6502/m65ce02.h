/*****************************************************************************
 *
 *	 m65ce02.c
 *	 Portable 65ce02 emulator V1.0beta
 *
 *	 Copyright (c) 2000 Peter Trauner, all rights reserved.
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

/*
mos metal oxid semiconductor
bought by cbm

licence to produce chips
 rockwell

6500 / 6501
mask programable microcontroller
32 io ports (2 interruptable)
timer
64 byte ram
8 kbyte rom

6502 (used in many designs)

6508
8 io pins (p0 bis p7)

6509
4 io pins (p0 bis p3)
1megabyte memory management???

6510/8500 (used in some designs)
6 io pins (p0 bis p5)

7501/8501 (c16, c116, c232, c264, plus4, c364)
7 io pins (no p5)
no nmi

8502 (c128)
7 io pins (no p7)

the above series is opcode compatible (including illegal opcodes)

65c02 (used in some designs)
no illegal opcodes from the above series
so not full compatible to 6502 series
additional commands

n2a03 (some arcades)
(nintendo variant without decimal mode?)
m65c02 compatible (except decimal mode)?

65sc02 (where used?)
65c02 compatible
additional commands

gte65816 (nintendo snes)
65802 upgrade cpu (c64 and c128 upgrade cpu)
16 bit wide registers
65c02? compatible mode
additional commands

huc6280 (nec pcengine)
65sc02 compatible?
8 memory registers
(highest 3 bits select memory register, these build a22..a13)
(so 2 Megabyte address room!)
additional commands?

65ce02 (c65 prototype)
(cpu core to be used in asics)
65sc02 compatible
z register
(65c02 zeropage indexed addressing is now (zeropage),z)
b bank register, highbyte of all zerozape addressing
register for stack high byte
additional command (some from the 65816)
*/

#ifndef _M65CE02_H
#define _M65CE02_H

#include "cpuintrf.h"
#include "osd_cpu.h"
#include "m6502.h"

enum {
	M65CE02_PC=1, M65CE02_S, M65CE02_P, M65CE02_A, M65CE02_X, M65CE02_Y,
	M65CE02_Z, M65CE02_B, M65CE02_EA, M65CE02_ZP,
	M65CE02_NMI_STATE, M65CE02_IRQ_STATE, M65CE02_SUBTYPE
};

#define M65CE02_INT_NONE	M6502_INT_NONE
#define M65CE02_INT_IRQ 	M6502_INT_IRQ
#define M65CE02_INT_NMI 	M6502_INT_NMI

#define M65CE02_NMI_VEC 	M6502_NMI_VEC
#define M65CE02_RST_VEC 	M6502_RST_VEC
#define M65CE02_IRQ_VEC 	M6502_IRQ_VEC

extern int m65ce02_ICount;				/* cycle count */

extern void m65ce02_reset(void *param);
extern void m65ce02_exit(void);
extern int	m65ce02_execute(int cycles);
extern unsigned m65ce02_get_context (void *dst);
extern void m65ce02_set_context (void *src);
extern unsigned m65ce02_get_pc (void);
extern void m65ce02_set_pc (unsigned val);
extern unsigned m65ce02_get_sp (void);
extern void m65ce02_set_sp (unsigned val);
extern unsigned m65ce02_get_reg (int regnum);
extern void m65ce02_set_reg (int regnum, unsigned val);
extern void m65ce02_set_nmi_line(int state);
extern void m65ce02_set_irq_line(int irqline, int state);
extern void m65ce02_set_irq_callback(int (*callback)(int irqline));
extern void m65ce02_state_save(void *file);
extern void m65ce02_state_load(void *file);
extern const char *m65ce02_info(void *context, int regnum);
extern unsigned m65ce02_dasm(char *buffer, unsigned pc);

#ifdef MAME_DEBUG
extern unsigned Dasm65ce02( char *dst, unsigned pc );
#endif

#endif /* _M65CE02_H */


