#ifndef Z8K_H

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package cpu.z8000;

public class z8000H
{
	
	enum {
		Z8000_PC=1, Z8000_NSP, Z8000_FCW, Z8000_PSAP, Z8000_REFRESH,
		Z8000_IRQ_REQ, Z8000_IRQ_SRV, Z8000_IRQ_VEC,
		Z8000_R0, Z8000_R1, Z8000_R2, Z8000_R3,
		Z8000_R4, Z8000_R5, Z8000_R6, Z8000_R7,
		Z8000_R8, Z8000_R9, Z8000_R10, Z8000_R11,
		Z8000_R12, Z8000_R13, Z8000_R14, Z8000_R15,
		Z8000_NMI_STATE, Z8000_NVI_STATE, Z8000_VI_STATE };
	
	/* Interrupt Types that can be generated by outside sources */
	#define Z8000_TRAP		0x4000	/* internal trap */
	#define Z8000_NMI		0x2000	/* non maskable interrupt */
	#define Z8000_SEGTRAP	0x1000	/* segment trap (Z8001) */
	#define Z8000_NVI		0x0800	/* non vectored interrupt */
	#define Z8000_VI		0x0400	/* vectored interrupt (LSB is vector)  */
	#define Z8000_SYSCALL	0x0200	/* system call (lsb is vector) */
	#define Z8000_HALT		0x0100	/* halted flag	*/
	#define Z8000_INT_NONE  0x0000
	
	/* PUBLIC FUNCTIONS */
	extern unsigned z8000_get_context(void *dst);
	extern void z8000_set_context(void *src);
	extern unsigned z8000_get_pc(void);
	extern void z8000_set_pc(unsigned val);
	extern unsigned z8000_get_sp(void);
	extern void z8000_set_sp(unsigned val);
	extern unsigned z8000_get_reg(int regnum);
	extern void z8000_set_reg(int regnum, unsigned val);
	extern void z8000_reset(void *param);
	extern extern int	z8000_execute(int cycles);
	extern void z8000_set_nmi_line(int state);
	extern void z8000_set_irq_line(int irqline, int state);
	extern void z8000_set_irq_callback(int (*callback)(int irqline));
	extern const char *z8000_info(void *context, int regnum);
	extern unsigned z8000_dasm(char *buffer, unsigned pc);
	
	extern void z8000_State_Save(int cpunum, void *f);
	extern void z8000_State_Load(int cpunum, void *f);
	
	/* PUBLIC GLOBALS */
	extern int z8000_ICount;
	
	#ifdef MAME_DEBUG
	extern int DasmZ8000(char *buff, int pc);
	#endif
	
	#endif /* Z8K_H */
}
