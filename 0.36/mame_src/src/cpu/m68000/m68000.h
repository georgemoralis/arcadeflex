#ifndef M68000__HEADER
#define M68000__HEADER

#include "osd_cpu.h"
#include "mamedbg.h"

enum {
	M68K_PC=1, M68K_ISP, M68K_USP, M68K_SR, M68K_VBR, M68K_SFC, M68K_DFC,
	M68K_D0, M68K_D1, M68K_D2, M68K_D3, M68K_D4, M68K_D5, M68K_D6, M68K_D7,
	M68K_A0, M68K_A1, M68K_A2, M68K_A3, M68K_A4, M68K_A5, M68K_A6, M68K_A7 };

#ifdef A68KEM

extern int m68000_ICount;

#else

/* Use the C core */
#include "m68k.h"
#define m68000_ICount m68k_clks_left

#endif /* A68KEM */

/* The MAME API for MC68000 */

#define MC68000_INT_NONE 0
#define MC68000_IRQ_1    1
#define MC68000_IRQ_2    2
#define MC68000_IRQ_3    3
#define MC68000_IRQ_4    4
#define MC68000_IRQ_5    5
#define MC68000_IRQ_6    6
#define MC68000_IRQ_7    7

#define MC68000_INT_ACK_AUTOVECTOR    -1
#define MC68000_INT_ACK_SPURIOUS      -2

#define MC68000_CPU_MODE_68000 1
#define MC68000_CPU_MODE_68010 2
#define MC68000_CPU_MODE_68020 4


extern void m68000_reset(void *param);
extern void m68000_exit(void);
extern int	m68000_execute(int cycles);
extern unsigned m68000_get_context(void *dst);
extern void m68000_set_context(void *src);
extern unsigned m68000_get_pc(void);
extern void m68000_set_pc(unsigned val);
extern unsigned m68000_get_sp(void);
extern void m68000_set_sp(unsigned val);
extern unsigned m68000_get_reg(int regnum);
extern void m68000_set_reg(int regnum, unsigned val);
extern void m68000_set_nmi_line(int state);
extern void m68000_set_irq_line(int irqline, int state);
extern void m68000_set_irq_callback(int (*callback)(int irqline));
extern const char *m68000_info(void *context, int regnum);
extern unsigned m68000_dasm(char *buffer, unsigned pc);

/****************************************************************************
 * M68010 section
 ****************************************************************************/
#if HAS_M68010
#define MC68010_INT_NONE                MC68000_INT_NONE
#define MC68010_IRQ_1					MC68000_IRQ_1
#define MC68010_IRQ_2					MC68000_IRQ_2
#define MC68010_IRQ_3					MC68000_IRQ_3
#define MC68010_IRQ_4					MC68000_IRQ_4
#define MC68010_IRQ_5					MC68000_IRQ_5
#define MC68010_IRQ_6					MC68000_IRQ_6
#define MC68010_IRQ_7					MC68000_IRQ_7
#define MC68010_INT_ACK_AUTOVECTOR		MC68000_INT_ACK_AUTOVECTOR
#define MC68010_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS

#define m68010_ICount                   m68000_ICount
extern void m68010_reset(void *param);
extern void m68010_exit(void);
extern int	m68010_execute(int cycles);
extern unsigned m68010_get_context(void *dst);
extern void m68010_set_context(void *src);
extern unsigned m68010_get_pc(void);
extern void m68010_set_pc(unsigned val);
extern unsigned m68010_get_sp(void);
extern void m68010_set_sp(unsigned val);
extern unsigned m68010_get_reg(int regnum);
extern void m68010_set_reg(int regnum, unsigned val);
extern void m68010_set_nmi_line(int state);
extern void m68010_set_irq_line(int irqline, int state);
extern void m68010_set_irq_callback(int (*callback)(int irqline));
const char *m68010_info(void *context, int regnum);
extern unsigned m68010_dasm(char *buffer, unsigned pc);
#endif

/****************************************************************************
 * M68EC020 section
 ****************************************************************************/
#if HAS_M68EC020
#define MC68EC020_INT_NONE				MC68000_INT_NONE
#define MC68EC020_IRQ_1					MC68000_IRQ_1
#define MC68EC020_IRQ_2					MC68000_IRQ_2
#define MC68EC020_IRQ_3					MC68000_IRQ_3
#define MC68EC020_IRQ_4					MC68000_IRQ_4
#define MC68EC020_IRQ_5					MC68000_IRQ_5
#define MC68EC020_IRQ_6					MC68000_IRQ_6
#define MC68EC020_IRQ_7					MC68000_IRQ_7
#define MC68EC020_INT_ACK_AUTOVECTOR	MC68000_INT_ACK_AUTOVECTOR
#define MC68EC020_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS

#define m68ec020_ICount                   m68000_ICount
extern void m68ec020_reset(void *param);
extern void m68ec020_exit(void);
extern int	m68ec020_execute(int cycles);
extern unsigned m68ec020_get_context(void *dst);
extern void m68ec020_set_context(void *src);
extern unsigned m68ec020_get_pc(void);
extern void m68ec020_set_pc(unsigned val);
extern unsigned m68ec020_get_sp(void);
extern void m68ec020_set_sp(unsigned val);
extern unsigned m68ec020_get_reg(int regnum);
extern void m68ec020_set_reg(int regnum, unsigned val);
extern void m68ec020_set_nmi_line(int state);
extern void m68ec020_set_irq_line(int irqline, int state);
extern void m68ec020_set_irq_callback(int (*callback)(int irqline));
const char *m68ec020_info(void *context, int regnum);
extern unsigned m68ec020_dasm(char *buffer, unsigned pc);
#endif

/****************************************************************************
 * M68020 section
 ****************************************************************************/
#if HAS_M68020
#define MC68020_INT_NONE				MC68000_INT_NONE
#define MC68020_IRQ_1					MC68000_IRQ_1
#define MC68020_IRQ_2					MC68000_IRQ_2
#define MC68020_IRQ_3					MC68000_IRQ_3
#define MC68020_IRQ_4					MC68000_IRQ_4
#define MC68020_IRQ_5					MC68000_IRQ_5
#define MC68020_IRQ_6					MC68000_IRQ_6
#define MC68020_IRQ_7					MC68000_IRQ_7
#define MC68020_INT_ACK_AUTOVECTOR		MC68000_INT_ACK_AUTOVECTOR
#define MC68020_INT_ACK_SPURIOUS		MC68000_INT_ACK_SPURIOUS

#define m68020_ICount                   m68000_ICount
extern void m68020_reset(void *param);
extern void m68020_exit(void);
extern int	m68020_execute(int cycles);
extern unsigned m68020_get_context(void *dst);
extern void m68020_set_context(void *src);
extern unsigned m68020_get_pc(void);
extern void m68020_set_pc(unsigned val);
extern unsigned m68020_get_sp(void);
extern void m68020_set_sp(unsigned val);
extern unsigned m68020_get_reg(int regnum);
extern void m68020_set_reg(int regnum, unsigned val);
extern void m68020_set_nmi_line(int state);
extern void m68020_set_irq_line(int irqline, int state);
extern void m68020_set_irq_callback(int (*callback)(int irqline));
const char *m68020_info(void *context, int regnum);
extern unsigned m68020_dasm(char *buffer, unsigned pc);
#endif

#ifdef MAME_DEBUG
extern int Dasm68000(char *buffer, int pc);
#endif /* MAME_DEBUG */

#endif /* M68000__HEADER */
