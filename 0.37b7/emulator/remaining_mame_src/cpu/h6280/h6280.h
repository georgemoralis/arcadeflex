/*****************************************************************************

	h6280.h Portable Hu6280 emulator interface

	Copyright (c) 1999 Bryan McPhail, mish@tendril.co.uk

	This source code is based (with permission!) on the 6502 emulator by
	Juergen Buchmueller.  It is released as part of the Mame emulator project.
	Let me know if you intend to use this code in any other project.

******************************************************************************/

#ifndef _H6280_H
#define _H6280_H

#include "osd_cpu.h"

enum {
	H6280_PC=1, H6280_S, H6280_P, H6280_A, H6280_X, H6280_Y,
	H6280_IRQ_MASK, H6280_TIMER_STATE,
	H6280_NMI_STATE, H6280_IRQ1_STATE, H6280_IRQ2_STATE, H6280_IRQT_STATE
#ifdef MAME_DEBUG
    ,
	H6280_M1, H6280_M2, H6280_M3, H6280_M4,
	H6280_M5, H6280_M6, H6280_M7, H6280_M8
#endif
};

//#define LAZY_FLAGS  1

#define H6280_INT_NONE	0
#define H6280_INT_NMI	1
#define H6280_INT_TIMER	2
#define H6280_INT_IRQ1	3
#define H6280_INT_IRQ2	4

#define H6280_RESET_VEC	0xfffe
#define H6280_NMI_VEC	0xfffc
#define H6280_TIMER_VEC	0xfffa
#define H6280_IRQ1_VEC	0xfff8
#define H6280_IRQ2_VEC	0xfff6			/* Aka BRK vector */

extern int h6280_ICount;				/* cycle count */

extern void h6280_reset(void *param);			/* Reset registers to the initial values */
extern void h6280_exit(void);					/* Shut down CPU */
extern int h6280_execute(int cycles);			/* Execute cycles - returns number of cycles actually run */
extern unsigned h6280_get_context(void *dst);	/* Get registers, return context size */
extern void h6280_set_context(void *src);		/* Set registers */
extern unsigned h6280_get_pc(void); 			/* Get program counter */
extern void h6280_set_pc(unsigned val); 		/* Set program counter */
extern unsigned h6280_get_sp(void); 			/* Get stack pointer */
extern void h6280_set_sp(unsigned val); 		/* Set stack pointer */
extern unsigned h6280_get_reg (int regnum);
extern void h6280_set_reg (int regnum, unsigned val);
extern void h6280_set_nmi_line(int state);
extern void h6280_set_irq_line(int irqline, int state);
extern void h6280_set_irq_callback(int (*callback)(int irqline));
extern const char *h6280_info(void *context, int regnum);
extern unsigned h6280_dasm(char *buffer, unsigned pc);

READ_HANDLER( H6280_irq_status_r );
WRITE_HANDLER( H6280_irq_status_w );

READ_HANDLER( H6280_timer_r );
WRITE_HANDLER( H6280_timer_w );

#ifdef MAME_DEBUG
extern int Dasm6280(char *buffer, int pc);
#endif

#endif /* _H6280_H */
