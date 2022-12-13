/*
 * ported to v0.36
 */
package arcadeflex.v036.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;

public class cpuintrf {

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  cpuintrf.c
/*TODO*///
/*TODO*///  Don't you love MS-DOS 8+3 names? That stands for CPU interface.
/*TODO*///  Functions needed to interface the CPU emulator with the other parts of
/*TODO*///  the emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///#include <signal.h>
/*TODO*///#include "driver.h"
/*TODO*///#include "timer.h"
/*TODO*///#include "state.h"
/*TODO*///#include "mamedbg.h"
/*TODO*///#include "hiscore.h"
/*TODO*///
/*TODO*///#if (HAS_Z80)
/*TODO*///#include "cpu/z80/z80.h"
/*TODO*///#endif
/*TODO*///#if (HAS_Z80GB)
/*TODO*///#include "cpu/z80gb/z80gb.h"
/*TODO*///#endif
/*TODO*///#if (HAS_8080 || HAS_8085A)
/*TODO*///#include "cpu/i8085/i8085.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M6502 || HAS_M65C02 || HAS_M65SC02 || HAS_M6510 || HAS_N2A03)
/*TODO*///#include "cpu/m6502/m6502.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///#include "cpu/m6502/m65ce02.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///#include "cpu/m6502/m6509.h"
/*TODO*///#endif
/*TODO*///#if (HAS_H6280)
/*TODO*///#include "cpu/h6280/h6280.h"
/*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///#include "cpu/i86/i86intrf.h"
/*TODO*///#endif
/*TODO*///#if (HAS_V20 || HAS_V30 || HAS_V33)
/*TODO*///#include "cpu/nec/necintrf.h"
/*TODO*///#endif
/*TODO*///#if (HAS_I8035 || HAS_I8039 || HAS_I8048 || HAS_N7751)
/*TODO*///#include "cpu/i8039/i8039.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M6800 || HAS_M6801 || HAS_M6802 || HAS_M6803 || HAS_M6808 || HAS_HD63701)
/*TODO*///#include "cpu/m6800/m6800.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M6805 || HAS_M68705 || HAS_HD63705)
/*TODO*///#include "cpu/m6805/m6805.h"
/*TODO*///#endif
/*TODO*///#if (HAS_HD6309 || HAS_M6809)
/*TODO*///#include "cpu/m6809/m6809.h"
/*TODO*///#endif
/*TODO*///#if (HAS_KONAMI)
/*TODO*///#include "cpu/konami/konami.h"
/*TODO*///#endif
/*TODO*///#if (HAS_M68000 || defined HAS_M68010 || HAS_M68020 || HAS_M68EC020)
/*TODO*///#include "cpu/m68000/m68000.h"
/*TODO*///#endif
/*TODO*///#if (HAS_T11)
/*TODO*///#include "cpu/t11/t11.h"
/*TODO*///#endif
/*TODO*///#if (HAS_S2650)
/*TODO*///#include "cpu/s2650/s2650.h"
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34010)
/*TODO*///#include "cpu/tms34010/tms34010.h"
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9900) || (HAS_TMS9940) || (HAS_TMS9980) || (HAS_TMS9985) \
/*TODO*///	|| (HAS_TMS9989) || (HAS_TMS9995) || (HAS_TMS99105A) || (HAS_TMS99110A)
/*TODO*///#include "cpu/tms9900/tms9900.h"
/*TODO*///#endif
/*TODO*///#if (HAS_Z8000)
/*TODO*///#include "cpu/z8000/z8000.h"
/*TODO*///#endif
/*TODO*///#if (HAS_TMS320C10)
/*TODO*///#include "cpu/tms32010/tms32010.h"
/*TODO*///#endif
/*TODO*///#if (HAS_CCPU)
/*TODO*///#include "cpu/ccpu/ccpu.h"
/*TODO*///#endif
/*TODO*///#if (HAS_PDP1)
/*TODO*///#include "cpu/pdp1/pdp1.h"
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2100)
/*TODO*///#include "cpu/adsp2100/adsp2100.h"
/*TODO*///#endif
/*TODO*///
/*TODO*////* these are triggers sent to the timer system for various interrupt events */
/*TODO*///#define TRIGGER_TIMESLICE		-1000
/*TODO*///#define TRIGGER_INT 			-2000
/*TODO*///#define TRIGGER_YIELDTIME		-3000
/*TODO*///#define TRIGGER_SUSPENDTIME 	-4000
/*TODO*///
/*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#define SAVE_STATE_TEST 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	if( errorlog ) fprintf x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
/*TODO*///#define CPUINFO_SIZE	(5*sizeof(int)+4*sizeof(void*)+2*sizeof(double))
/*TODO*////* How do I calculate the next power of two from CPUINFO_SIZE using a macro? */
/*TODO*///#ifdef __LP64__
/*TODO*///#define CPUINFO_ALIGN	(128-CPUINFO_SIZE)
/*TODO*///#else
/*TODO*///#define CPUINFO_ALIGN	(64-CPUINFO_SIZE)
/*TODO*///#endif
/*TODO*///
/*TODO*///struct cpuinfo
/*TODO*///{
/*TODO*///	struct cpu_interface *intf; 	/* pointer to the interface functions */
/*TODO*///	int iloops; 					/* number of interrupts remaining this frame */
/*TODO*///	int totalcycles;				/* total CPU cycles executed */
/*TODO*///	int vblankint_countdown;		/* number of vblank callbacks left until we interrupt */
/*TODO*///	int vblankint_multiplier;		/* number of vblank callbacks per interrupt */
/*TODO*///	void *vblankint_timer;			/* reference to elapsed time counter */
/*TODO*///	double vblankint_period;		/* timing period of the VBLANK interrupt */
/*TODO*///	void *timedint_timer;			/* reference to this CPU's timer */
/*TODO*///	double timedint_period; 		/* timing period of the timed interrupt */
/*TODO*///	void *context;					/* dynamically allocated context buffer */
/*TODO*///	int save_context;				/* need to context switch this CPU? yes or no */
/*TODO*///	UINT8 filler[CPUINFO_ALIGN];	/* make the array aligned to next power of 2 */
/*TODO*///};
/*TODO*///
/*TODO*///static struct cpuinfo cpu[MAX_CPU];
/*TODO*///
/*TODO*///static int activecpu,totalcpu;
/*TODO*///static int cycles_running;	/* number of cycles that the CPU emulation was requested to run */
/*TODO*///					/* (needed by cpu_getfcount) */
/*TODO*///static int have_to_reset;
/*TODO*///
/*TODO*///static int interrupt_enable[MAX_CPU];
/*TODO*///static int interrupt_vector[MAX_CPU];
/*TODO*///
/*TODO*///static int irq_line_state[MAX_CPU * MAX_IRQ_LINES];
/*TODO*///static int irq_line_vector[MAX_CPU * MAX_IRQ_LINES];
/*TODO*///
/*TODO*///static int watchdog_counter;
/*TODO*///
/*TODO*///static void *vblank_timer;
/*TODO*///static int vblank_countdown;
/*TODO*///static int vblank_multiplier;
/*TODO*///static double vblank_period;
/*TODO*///
/*TODO*///static void *refresh_timer;
/*TODO*///static double refresh_period;
/*TODO*///static double refresh_period_inv;
/*TODO*///
/*TODO*///static void *timeslice_timer;
/*TODO*///static double timeslice_period;
/*TODO*///
/*TODO*///static double scanline_period;
/*TODO*///static double scanline_period_inv;
/*TODO*///
/*TODO*///static int usres; /* removed from cpu_run and made global */
/*TODO*///static int vblank;
/*TODO*///static int current_frame;
/*TODO*///
/*TODO*///static void cpu_generate_interrupt(int cpunum, int (*func)(void), int num);
/*TODO*///static void cpu_vblankintcallback(int param);
/*TODO*///static void cpu_timedintcallback(int param);
/*TODO*///static void cpu_internal_interrupt(int cpunum, int type);
/*TODO*///static void cpu_manualnmicallback(int param);
/*TODO*///static void cpu_manualirqcallback(int param);
/*TODO*///static void cpu_internalintcallback(int param);
/*TODO*///static void cpu_manualintcallback(int param);
/*TODO*///static void cpu_clearintcallback(int param);
/*TODO*///static void cpu_resetcallback(int param);
/*TODO*///static void cpu_haltcallback(int param);
/*TODO*///static void cpu_timeslicecallback(int param);
/*TODO*///static void cpu_vblankreset(void);
/*TODO*///static void cpu_vblankcallback(int param);
/*TODO*///static void cpu_updatecallback(int param);
/*TODO*///static double cpu_computerate(int value);
/*TODO*///static void cpu_inittimers(void);
/*TODO*///
/*TODO*///
/*TODO*////* default irq callback handlers */
/*TODO*///static int cpu_0_irq_callback(int irqline);
/*TODO*///static int cpu_1_irq_callback(int irqline);
/*TODO*///static int cpu_2_irq_callback(int irqline);
/*TODO*///static int cpu_3_irq_callback(int irqline);
/*TODO*///
/*TODO*////* and a list of them for indexed access */
/*TODO*///static int (*cpu_irq_callbacks[MAX_CPU])(int) = {
/*TODO*///	cpu_0_irq_callback,
/*TODO*///	cpu_1_irq_callback,
/*TODO*///	cpu_2_irq_callback,
/*TODO*///	cpu_3_irq_callback
/*TODO*///};
/*TODO*///
/*TODO*////* Default window layout for the debugger */
/*TODO*///UINT8 default_win_layout[] = {
/*TODO*///	 0, 0,80, 5,	/* register window (top rows) */
/*TODO*///	 0, 5,24,17,	/* disassembler window (left, middle columns) */
/*TODO*///	25, 5,55, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///	25,14,55, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///	 0,23,80, 1 	/* command line window (bottom row) */
/*TODO*///};
/*TODO*///
/*TODO*////* Dummy interfaces for non-CPUs */
/*TODO*///static void Dummy_reset(void *param);
/*TODO*///static void Dummy_exit(void);
/*TODO*///static int Dummy_execute(int cycles);
/*TODO*///static void Dummy_burn(int cycles);
/*TODO*///static unsigned Dummy_get_context(void *regs);
/*TODO*///static void Dummy_set_context(void *regs);
/*TODO*///static unsigned Dummy_get_pc(void);
/*TODO*///static void Dummy_set_pc(unsigned val);
/*TODO*///static unsigned Dummy_get_sp(void);
/*TODO*///static void Dummy_set_sp(unsigned val);
/*TODO*///static unsigned Dummy_get_reg(int regnum);
/*TODO*///static void Dummy_set_reg(int regnum, unsigned val);
/*TODO*///static void Dummy_set_nmi_line(int state);
/*TODO*///static void Dummy_set_irq_line(int irqline, int state);
/*TODO*///static void Dummy_set_irq_callback(int (*callback)(int irqline));
/*TODO*///static int Dummy_ICount;
/*TODO*///static const char *Dummy_info(void *context, int regnum);
/*TODO*///static unsigned Dummy_dasm(char *buffer, unsigned pc);
/*TODO*///
/*TODO*////* Convenience macros - not in cpuintrf.h because they shouldn't be used by everyone */
/*TODO*///#define RESET(index)					((*cpu[index].intf->reset)(Machine->drv->cpu[index].reset_param))
/*TODO*///#define EXECUTE(index,cycles)			((*cpu[index].intf->execute)(cycles))
/*TODO*///#define GETCONTEXT(index,context)		((*cpu[index].intf->get_context)(context))
/*TODO*///#define SETCONTEXT(index,context)		((*cpu[index].intf->set_context)(context))
/*TODO*///#define GETPC(index)					((*cpu[index].intf->get_pc)())
/*TODO*///#define SETPC(index,val)				((*cpu[index].intf->set_pc)(val))
/*TODO*///#define GETSP(index)					((*cpu[index].intf->get_sp)())
/*TODO*///#define SETSP(index,val)				((*cpu[index].intf->set_sp)(val))
/*TODO*///#define GETREG(index,regnum)			((*cpu[index].intf->get_reg)(regnum))
/*TODO*///#define SETREG(index,regnum,value)		((*cpu[index].intf->set_reg)(regnum,value))
/*TODO*///#define SETNMILINE(index,state) 		((*cpu[index].intf->set_nmi_line)(state))
/*TODO*///#define SETIRQLINE(index,line,state)	((*cpu[index].intf->set_irq_line)(line,state))
/*TODO*///#define SETIRQCALLBACK(index,callback)	((*cpu[index].intf->set_irq_callback)(callback))
/*TODO*///#define INTERNAL_INTERRUPT(index,type)	if( cpu[index].intf->internal_interrupt ) ((*cpu[index].intf->internal_interrupt)(type))
/*TODO*///#define CPUINFO(index,context,regnum)	((*cpu[index].intf->cpu_info)(context,regnum))
/*TODO*///#define CPUDASM(index,buffer,pc)		((*cpu[index].intf->cpu_dasm)(buffer,pc))
/*TODO*///#define ICOUNT(index)					(*cpu[index].intf->icount)
/*TODO*///#define INT_TYPE_NONE(index)			(cpu[index].intf->no_int)
/*TODO*///#define INT_TYPE_IRQ(index) 			(cpu[index].intf->irq_int)
/*TODO*///#define INT_TYPE_NMI(index) 			(cpu[index].intf->nmi_int)
/*TODO*///#define READMEM(index,offset)			((*cpu[index].intf->memory_read)(offset))
/*TODO*///#define WRITEMEM(index,offset,data) 	((*cpu[index].intf->memory_write)(offset,data))
/*TODO*///#define SET_OP_BASE(index,pc)			((*cpu[index].intf->set_op_base)(pc))
/*TODO*///
/*TODO*///#define CPU_TYPE(index) 				(Machine->drv->cpu[index].cpu_type & ~CPU_FLAGS_MASK)
/*TODO*///#define CPU_AUDIO(index)				(Machine->drv->cpu[index].cpu_type & CPU_AUDIO_CPU)
/*TODO*///
/*TODO*///#define IFC_INFO(cpu,context,regnum)	((cpuintf[cpu].cpu_info)(context,regnum))
/*TODO*///
/*TODO*////* most CPUs use this macro */
/*TODO*///#define CPU0(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM) \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute, NULL,						   \
/*TODO*///		name##_get_context, name##_set_context, name##_get_pc, name##_set_pc,	   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,NULL,NULL, name##_info, name##_dasm,								   \
/*TODO*///		nirq, dirq, &##name##_ICount, oc, i0, i1, i2,							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, cpu_setOPbase##mem,				   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}
/*TODO*///
/*TODO*////* CPUs which have _burn, _state_save and _state_load functions */
/*TODO*///#define CPU1(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM)   \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute,								   \
/*TODO*///		name##_burn,															   \
/*TODO*///		name##_get_context, name##_set_context, name##_get_pc, name##_set_pc,	   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,name##_state_save,name##_state_load, name##_info, name##_dasm, 	   \
/*TODO*///		nirq, dirq, &##name##_ICount, oc, i0, i1, i2,							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, cpu_setOPbase##mem,				   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}
/*TODO*///
/*TODO*////* CPUs which have the _internal_interrupt function */
/*TODO*///#define CPU2(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM)   \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute,								   \
/*TODO*///		NULL,																	   \
/*TODO*///		name##_get_context, name##_set_context, name##_get_pc, name##_set_pc,	   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		name##_internal_interrupt,NULL,NULL, name##_info, name##_dasm,			   \
/*TODO*///		nirq, dirq, &##name##_ICount, oc, i0, i1, i2,							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, cpu_setOPbase##mem,				   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}																			   \
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* warning the ordering must match the one of the enum in driver.h! */
/*TODO*///struct cpu_interface cpuintf[] =
/*TODO*///{
/*TODO*///	CPU0(DUMMY,    Dummy,	 1,  0,1.00,0,				   -1,			   -1,			   16,	  0,16,LE,1, 1,16	),
/*TODO*///#if (HAS_Z80)
/*TODO*///	CPU1(Z80,	   z80, 	 1,255,1.00,Z80_IGNORE_INT,    Z80_IRQ_INT,    Z80_NMI_INT,    16,	  0,16,LE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_Z80GB)
/*TODO*///	CPU0(Z80GB,    z80gb,	 5,255,1.00,Z80GB_IGNORE_INT,  0,			   1,			   16,	  0,16,LE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_8080)
/*TODO*///	CPU0(8080,	   i8080,	 4,255,1.00,I8080_NONE, 	   I8080_INTR,	   I8080_TRAP,	   16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_8085A)
/*TODO*///	CPU0(8085A,    i8085,	 4,255,1.00,I8085_NONE, 	   I8085_INTR,	   I8085_TRAP,	   16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6502)
/*TODO*///	CPU0(M6502,    m6502,	 1,  0,1.00,M6502_INT_NONE,    M6502_INT_IRQ,  M6502_INT_NMI,  16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M65C02)
/*TODO*///	CPU0(M65C02,   m65c02,	 1,  0,1.00,M65C02_INT_NONE,   M65C02_INT_IRQ, M65C02_INT_NMI, 16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M65SC02)
/*TODO*///	CPU0(M65SC02,  m65sc02,  1,  0,1.00,M65SC02_INT_NONE,  M65SC02_INT_IRQ,M65SC02_INT_NMI,16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///	CPU0(M65CE02,  m65ce02,  1,  0,1.00,M65CE02_INT_NONE,  M65CE02_INT_IRQ,M65CE02_INT_NMI,16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///	CPU0(M6509,    m6509,	 1,  0,1.00,M6509_INT_NONE,    M6509_INT_IRQ,  M6509_INT_NMI,  20,	  0,20,LE,1, 3,20	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6510)
/*TODO*///	CPU0(M6510,    m6510,	 1,  0,1.00,M6510_INT_NONE,    M6510_INT_IRQ,  M6510_INT_NMI,  16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_N2A03)
/*TODO*///	CPU0(N2A03,    n2a03,	 1,  0,1.00,N2A03_INT_NONE,    N2A03_INT_IRQ,  N2A03_INT_NMI,  16,	  0,16,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_H6280)
/*TODO*///	CPU0(H6280,    h6280,	 3,  0,1.00,H6280_INT_NONE,    -1,			   H6280_INT_NMI,  21,	  0,21,LE,1, 3,21	),
/*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///	CPU0(I86,	   i86, 	 1,  0,1.00,I86_INT_NONE,	   -1000,		   I86_NMI_INT,    20,	  0,20,LE,1, 5,20	),
/*TODO*///#endif
/*TODO*///#if (HAS_V20)
/*TODO*///	CPU0(V20,	   v20, 	 1,  0,1.00,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
/*TODO*///#endif
/*TODO*///#if (HAS_V30)
/*TODO*///	CPU0(V30,	   v30, 	 1,  0,1.00,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
/*TODO*///#endif
/*TODO*///#if (HAS_V33)
/*TODO*///	CPU0(V33,	   v33, 	 1,  0,1.05,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
/*TODO*///#endif
/*TODO*///#if (HAS_I8035)
/*TODO*///	CPU0(I8035,    i8035,	 1,  0,1.00,I8035_IGNORE_INT,  I8035_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_I8039)
/*TODO*///	CPU0(I8039,    i8039,	 1,  0,1.00,I8039_IGNORE_INT,  I8039_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_I8048)
/*TODO*///	CPU0(I8048,    i8048,	 1,  0,1.00,I8048_IGNORE_INT,  I8048_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_N7751)
/*TODO*///	CPU0(N7751,    n7751,	 1,  0,1.00,N7751_IGNORE_INT,  N7751_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6800)
/*TODO*///	CPU0(M6800,    m6800,	 1,  0,1.00,M6800_INT_NONE,    M6800_INT_IRQ,  M6800_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6801)
/*TODO*///	CPU0(M6801,    m6801,	 1,  0,1.00,M6801_INT_NONE,    M6801_INT_IRQ,  M6801_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6802)
/*TODO*///	CPU0(M6802,    m6802,	 1,  0,1.00,M6802_INT_NONE,    M6802_INT_IRQ,  M6802_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6803)
/*TODO*///	CPU0(M6803,    m6803,	 1,  0,1.00,M6803_INT_NONE,    M6803_INT_IRQ,  M6803_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6808)
/*TODO*///	CPU0(M6808,    m6808,	 1,  0,1.00,M6808_INT_NONE,    M6808_INT_IRQ,  M6808_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_HD63701)
/*TODO*///	CPU0(HD63701,  hd63701,  1,  0,1.00,HD63701_INT_NONE,  HD63701_INT_IRQ,HD63701_INT_NMI,16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_NSC8105)
/*TODO*///	CPU0(NSC8105,  nsc8105,  1,  0,1.00,NSC8105_INT_NONE,  NSC8105_INT_IRQ,NSC8105_INT_NMI,16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6805)
/*TODO*///	CPU0(M6805,    m6805,	 1,  0,1.00,M6805_INT_NONE,    M6805_INT_IRQ,  -1,			   16,	  0,11,BE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M68705)
/*TODO*///	CPU0(M68705,   m68705,	 1,  0,1.00,M68705_INT_NONE,   M68705_INT_IRQ, -1,			   16,	  0,11,BE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_HD63705)
/*TODO*///	CPU0(HD63705,  hd63705,  8,  0,1.00,HD63705_INT_NONE,  HD63705_INT_IRQ,-1,			   16,	  0,16,BE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_HD6309)
/*TODO*///	CPU0(HD6309,   hd6309,	 2,  0,1.00,HD6309_INT_NONE,   HD6309_INT_IRQ, HD6309_INT_NMI, 16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M6809)
/*TODO*///	CPU0(M6809,    m6809,	 2,  0,1.00,M6809_INT_NONE,    M6809_INT_IRQ,  M6809_INT_NMI,  16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_KONAMI)
/*TODO*///	CPU0(KONAMI,   konami,	 2,  0,1.00,KONAMI_INT_NONE,   KONAMI_INT_IRQ, KONAMI_INT_NMI, 16,	  0,16,BE,1, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_M68000)
/*TODO*///	CPU0(M68000,   m68000,	 8, -1,1.00,MC68000_INT_NONE,  -1,			   -1,			   24,	  0,24,BE,2,10,24	),
/*TODO*///#endif
/*TODO*///#if (HAS_M68010)
/*TODO*///	CPU0(M68010,   m68010,	 8, -1,1.00,MC68010_INT_NONE,  -1,			   -1,			   24,	  0,24,BE,2,10,24	),
/*TODO*///#endif
/*TODO*///#if (HAS_M68EC020)
/*TODO*///	CPU0(M68EC020, m68ec020, 8, -1,1.00,MC68EC020_INT_NONE,-1,			   -1,			   24,	  0,24,BE,2,10,24	),
/*TODO*///#endif
/*TODO*///#if (HAS_M68020)
/*TODO*///	CPU0(M68020,   m68020,	 8, -1,1.00,MC68020_INT_NONE,  -1,			   -1,			   24,	  0,24,BE,2,10,24	),
/*TODO*///#endif
/*TODO*///#if (HAS_T11)
/*TODO*///	CPU0(T11,	   t11, 	 4,  0,1.00,T11_INT_NONE,	   -1,			   -1,			   16lew, 0,16,LE,2, 6,16LEW),
/*TODO*///#endif
/*TODO*///#if (HAS_S2650)
/*TODO*///	CPU0(S2650,    s2650,	 2,  0,1.00,S2650_INT_NONE,    -1,			   -1,			   16,	  0,15,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34010)
/*TODO*///	CPU2(TMS34010, tms34010, 2,  0,1.00,TMS34010_INT_NONE, TMS34010_INT1,  -1,			   29,	  3,29,LE,2,10,29	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9900)
/*TODO*///	CPU0(TMS9900,  tms9900,  1,  0,1.00,TMS9900_NONE,	   -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9940)
/*TODO*///	CPU0(TMS9940,  tms9940,  1,  0,1.00,TMS9940_NONE,	   -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9980)
/*TODO*///	CPU0(TMS9980,  tms9980a, 1,  0,1.00,TMS9980A_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9985)
/*TODO*///	CPU0(TMS9985,  tms9985,  1,  0,1.00,TMS9985_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9989)
/*TODO*///	CPU0(TMS9989,  tms9989,  1,  0,1.00,TMS9989_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS9995)
/*TODO*///	CPU0(TMS9995,  tms9995,  1,  0,1.00,TMS9995_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99105A)
/*TODO*///	CPU0(TMS99105A,tms99105a,1,  0,1.00,TMS99105A_NONE,    -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS99110A)
/*TODO*///	CPU0(TMS99110A,tms99110a,1,  0,1.00,TMS99110A_NONE,    -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
/*TODO*///#endif
/*TODO*///#if (HAS_Z8000)
/*TODO*///	CPU0(Z8000,    z8000,	 2,  0,1.00,Z8000_INT_NONE,    Z8000_NVI,	   Z8000_NMI,	   16bew, 0,16,BE,2, 6,16BEW),
/*TODO*///#endif
/*TODO*///#if (HAS_TMS320C10)
/*TODO*///	CPU0(TMS320C10,tms320c10,2,  0,1.00,TMS320C10_INT_NONE,-1,			   -1,			   16,	 -1,16,BE,2, 4,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_CCPU)
/*TODO*///	CPU0(CCPU,	   ccpu,	 2,  0,1.00,0,				   -1,			   -1,			   16,	  0,15,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_PDP1)
/*TODO*///	CPU0(PDP1,	   pdp1,	 0,  0,1.00,0,				   -1,			   -1,			   16,	  0,18,LE,1, 3,16	),
/*TODO*///#endif
/*TODO*///#if (HAS_ADSP2100)
/*TODO*////* IMO we should rename all *_ICount to *_icount - ie. no mixed case */
/*TODO*///#define adsp2100_ICount adsp2100_icount
/*TODO*///	CPU0(ADSP2100, adsp2100, 4,  0,1.00,ADSP2100_INT_NONE, -1,			   -1,			   16lew,-1,14,LE,2, 4,16LEW),
/*TODO*///#endif
/*TODO*///};
/*TODO*///
/*TODO*///void cpu_init(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* Verify the order of entries in the cpuintf[] array */
/*TODO*///	for( i = 0; i < CPU_COUNT; i++ )
/*TODO*///	{
/*TODO*///		if( cpuintf[i].cpu_num != i )
/*TODO*///		{
/*TODO*///if (errorlog) fprintf( errorlog, "CPU #%d [%s] wrong ID %d: check enum CPU_... in src/driver.h!\n", i, cputype_name(i), cpuintf[i].cpu_num);
/*TODO*///			exit(1);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* count how many CPUs we have to emulate */
/*TODO*///	totalcpu = 0;
/*TODO*///
/*TODO*///	while (totalcpu < MAX_CPU)
/*TODO*///	{
/*TODO*///		if( CPU_TYPE(totalcpu) == CPU_DUMMY ) break;
/*TODO*///		totalcpu++;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* zap the CPU data structure */
/*TODO*///	memset(cpu, 0, sizeof(cpu));
/*TODO*///
/*TODO*///	/* Set up the interface functions */
/*TODO*///	for (i = 0; i < MAX_CPU; i++)
/*TODO*///		cpu[i].intf = &cpuintf[CPU_TYPE(i)];
/*TODO*///
/*TODO*///	/* reset the timer system */
/*TODO*///	timer_init();
/*TODO*///	timeslice_timer = refresh_timer = vblank_timer = NULL;
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_run(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* determine which CPUs need a context switch */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		int j, size;
/*TODO*///
/*TODO*///		/* allocate a context buffer for the CPU */
/*TODO*///		size = GETCONTEXT(i,NULL);
/*TODO*///		if( size == 0 )
/*TODO*///		{
/*TODO*///			/* That can't really be true */
/*TODO*///if (errorlog) fprintf( errorlog, "CPU #%d claims to need no context buffer!\n", i);
/*TODO*///			raise( SIGABRT );
/*TODO*///		}
/*TODO*///
/*TODO*///		cpu[i].context = malloc( size );
/*TODO*///		if( cpu[i].context == NULL )
/*TODO*///		{
/*TODO*///			/* That's really bad :( */
/*TODO*///if (errorlog) fprintf( errorlog, "CPU #%d failed to allocate context buffer (%d bytes)!\n", i, size);
/*TODO*///			raise( SIGABRT );
/*TODO*///		}
/*TODO*///
/*TODO*///		/* Zap the context buffer */
/*TODO*///		memset(cpu[i].context, 0, size );
/*TODO*///
/*TODO*///
/*TODO*///		/* Save if there is another CPU of the same type */
/*TODO*///		cpu[i].save_context = 0;
/*TODO*///
/*TODO*///		for (j = 0; j < totalcpu; j++)
/*TODO*///			if ( i != j && !strcmp(cpunum_core_file(i),cpunum_core_file(j)) )
/*TODO*///				cpu[i].save_context = 1;
/*TODO*///
/*TODO*///		#ifdef MAME_DEBUG
/*TODO*///
/*TODO*///		/* or if we're running with the debugger */
/*TODO*///		{
/*TODO*///			extern int mame_debug;
/*TODO*///			cpu[i].save_context |= mame_debug;
/*TODO*///		}
/*TODO*///
/*TODO*///		#endif
/*TODO*///
/*TODO*///		for( j = 0; j < MAX_IRQ_LINES; j++ )
/*TODO*///		{
/*TODO*///			irq_line_state[i * MAX_IRQ_LINES + j] = CLEAR_LINE;
/*TODO*///			irq_line_vector[i * MAX_IRQ_LINES + j] = cpuintf[CPU_TYPE(i)].default_vector;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///#ifdef	MAME_DEBUG
/*TODO*///	/* Initialize the debugger */
/*TODO*///	if( mame_debug )
/*TODO*///		mame_debug_init();
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///reset:
/*TODO*///	/* read hi scores information from hiscore.dat */
/*TODO*///	hs_open(Machine->gamedrv->name);
/*TODO*///	hs_init();
/*TODO*///
/*TODO*///	/* initialize the various timers (suspends all CPUs at startup) */
/*TODO*///	cpu_inittimers();
/*TODO*///	watchdog_counter = -1;
/*TODO*///
/*TODO*///	/* reset sound chips */
/*TODO*///	sound_reset();
/*TODO*///
/*TODO*///	/* enable all CPUs (except for audio CPUs if the sound is off) */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		if (!CPU_AUDIO(i) || Machine->sample_rate != 0)
/*TODO*///		{
/*TODO*///			timer_suspendcpu(i, 0, SUSPEND_REASON_RESET);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			timer_suspendcpu(i, 1, SUSPEND_REASON_DISABLE);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	have_to_reset = 0;
/*TODO*///	vblank = 0;
/*TODO*///
/*TODO*///if (errorlog) fprintf(errorlog,"Machine reset\n");
/*TODO*///
/*TODO*///	/* start with interrupts enabled, so the generic routine will work even if */
/*TODO*///	/* the machine doesn't have an interrupt enable port */
/*TODO*///	for (i = 0;i < MAX_CPU;i++)
/*TODO*///	{
/*TODO*///		interrupt_enable[i] = 1;
/*TODO*///		interrupt_vector[i] = 0xff;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* do this AFTER the above so init_machine() can use cpu_halt() to hold the */
/*TODO*///	/* execution of some CPUs, or disable interrupts */
/*TODO*///	if (Machine->drv->init_machine) (*Machine->drv->init_machine)();
/*TODO*///
/*TODO*///	/* reset each CPU */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		/* swap memory contexts and reset */
/*TODO*///		memorycontextswap(i);
/*TODO*///		if (cpu[i].save_context) SETCONTEXT(i, cpu[i].context);
/*TODO*///		activecpu = i;
/*TODO*///		RESET(i);
/*TODO*///
/*TODO*///		/* Set the irq callback for the cpu */
/*TODO*///		SETIRQCALLBACK(i,cpu_irq_callbacks[i]);
/*TODO*///
/*TODO*///		/* save the CPU context if necessary */
/*TODO*///		if (cpu[i].save_context) GETCONTEXT (i, cpu[i].context);
/*TODO*///
/*TODO*///		/* reset the total number of cycles */
/*TODO*///		cpu[i].totalcycles = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* reset the globals */
/*TODO*///	cpu_vblankreset();
/*TODO*///	current_frame = 0;
/*TODO*///
/*TODO*///	/* loop until the user quits */
/*TODO*///	usres = 0;
/*TODO*///	while (usres == 0)
/*TODO*///	{
/*TODO*///		int cpunum;
/*TODO*///
/*TODO*///		/* was machine_reset() called? */
/*TODO*///		if (have_to_reset)
/*TODO*///		{
/*TODO*///#ifdef MESS
/*TODO*///			if (Machine->drv->stop_machine) (*Machine->drv->stop_machine)();
/*TODO*///#endif
/*TODO*///			goto reset;
/*TODO*///		}
/*TODO*///		profiler_mark(PROFILER_EXTRA);
/*TODO*///
/*TODO*///#if SAVE_STATE_TEST
/*TODO*///		{
/*TODO*///			if( keyboard_pressed_memory(KEYCODE_S) )
/*TODO*///			{
/*TODO*///				void *s = state_create(Machine->gamedrv->name);
/*TODO*///				if( s )
/*TODO*///				{
/*TODO*///					for( cpunum = 0; cpunum < totalcpu; cpunum++ )
/*TODO*///					{
/*TODO*///						activecpu = cpunum;
/*TODO*///						memorycontextswap(activecpu);
/*TODO*///						if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///						/* make sure any bank switching is reset */
/*TODO*///						SET_OP_BASE(activecpu, GETPC(activecpu));
/*TODO*///						if( cpu[activecpu].intf->cpu_state_save )
/*TODO*///							(*cpu[activecpu].intf->cpu_state_save)(s);
/*TODO*///					}
/*TODO*///					state_close(s);
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( keyboard_pressed_memory(KEYCODE_L) )
/*TODO*///			{
/*TODO*///				void *s = state_open(Machine->gamedrv->name);
/*TODO*///				if( s )
/*TODO*///				{
/*TODO*///					for( cpunum = 0; cpunum < totalcpu; cpunum++ )
/*TODO*///					{
/*TODO*///						activecpu = cpunum;
/*TODO*///						memorycontextswap(activecpu);
/*TODO*///						if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///						/* make sure any bank switching is reset */
/*TODO*///						SET_OP_BASE(activecpu, GETPC(activecpu));
/*TODO*///						if( cpu[activecpu].intf->cpu_state_load )
/*TODO*///							(*cpu[activecpu].intf->cpu_state_load)(s);
/*TODO*///						/* update the contexts */
/*TODO*///						if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///					}
/*TODO*///					state_close(s);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///#endif
/*TODO*///		/* ask the timer system to schedule */
/*TODO*///		if (timer_schedule_cpu(&cpunum, &cycles_running))
/*TODO*///		{
/*TODO*///			int ran;
/*TODO*///
/*TODO*///
/*TODO*///			/* switch memory and CPU contexts */
/*TODO*///			activecpu = cpunum;
/*TODO*///			memorycontextswap(activecpu);
/*TODO*///			if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///			/* make sure any bank switching is reset */
/*TODO*///			SET_OP_BASE(activecpu, GETPC(activecpu));
/*TODO*///
/*TODO*///			/* run for the requested number of cycles */
/*TODO*///			profiler_mark(PROFILER_CPU1 + cpunum);
/*TODO*///			ran = EXECUTE(activecpu, cycles_running);
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///			/* update based on how many cycles we really ran */
/*TODO*///			cpu[activecpu].totalcycles += ran;
/*TODO*///
/*TODO*///			/* update the contexts */
/*TODO*///			if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///			activecpu = -1;
/*TODO*///
/*TODO*///			/* update the timer with how long we actually ran */
/*TODO*///			timer_update_cpu(cpunum, ran);
/*TODO*///		}
/*TODO*///
/*TODO*///		profiler_mark(PROFILER_END);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* write hi scores to disk - No scores saving if cheat */
/*TODO*///	hs_close();
/*TODO*///
/*TODO*///#ifdef MESS
/*TODO*///	if (Machine->drv->stop_machine) (*Machine->drv->stop_machine)();
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef	MAME_DEBUG
/*TODO*///	/* Shut down the debugger */
/*TODO*///	if( mame_debug )
/*TODO*///		mame_debug_exit();
/*TODO*///#endif
/*TODO*///
/*TODO*///	/* shut down the CPU cores */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		/* if the CPU core defines an exit function, call it now */
/*TODO*///		if( cpu[i].intf->exit )
/*TODO*///			(*cpu[i].intf->exit)();
/*TODO*///
/*TODO*///		/* free the context buffer for that CPU */
/*TODO*///		if( cpu[i].context )
/*TODO*///		{
/*TODO*///			free( cpu[i].context );
/*TODO*///			cpu[i].context = NULL;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	totalcpu = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use this function to initialize, and later maintain, the watchdog. For
/*TODO*///  convenience, when the machine is reset, the watchdog is disabled. If you
/*TODO*///  call this function, the watchdog is initialized, and from that point
/*TODO*///  onwards, if you don't call it at least once every 10 video frames, the
/*TODO*///  machine will be reset.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void watchdog_reset_w(int offset,int data)
/*TODO*///{
/*TODO*///	watchdog_counter = Machine->drv->frames_per_second;
/*TODO*///}
/*TODO*///
/*TODO*///int watchdog_reset_r(int offset)
/*TODO*///{
/*TODO*///	watchdog_counter = Machine->drv->frames_per_second;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  This function resets the machine (the reset will not take place
/*TODO*///  immediately, it will be performed at the end of the active CPU's time
/*TODO*///  slice)
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void machine_reset(void)
/*TODO*///{
/*TODO*///	/* write hi scores to disk - No scores saving if cheat */
/*TODO*///	hs_close();
/*TODO*///
/*TODO*///	have_to_reset = 1;
/*TODO*///}
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     *
     * Use this function to reset a specified CPU immediately
     *
     **************************************************************************
     */
    public static void cpu_set_reset_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 7) | (state << 3), cpu_resetcallback);
    }

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use this function to control the HALT line on a CPU
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void cpu_set_halt_line(int cpunum,int state)
/*TODO*///{
/*TODO*///	timer_set(TIME_NOW, (cpunum & 7) | (state << 3), cpu_haltcallback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  This function returns CPUNUM current status  (running or halted)
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///int cpu_getstatus(int cpunum)
/*TODO*///{
/*TODO*///	if (cpunum >= MAX_CPU) return 0;
/*TODO*///
/*TODO*///	return !timer_iscpususpended(cpunum,
/*TODO*///			SUSPEND_REASON_HALT | SUSPEND_REASON_RESET | SUSPEND_REASON_DISABLE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int cpu_getactivecpu(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpunum;
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_setactivecpu(int cpunum)
/*TODO*///{
/*TODO*///	activecpu = cpunum;
/*TODO*///}
/*TODO*///
/*TODO*///int cpu_gettotalcpu(void)
/*TODO*///{
/*TODO*///	return totalcpu;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///unsigned cpu_get_pc(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return GETPC(cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_set_pc(unsigned val)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	SETPC(cpunum,val);
/*TODO*///}
/*TODO*///
/*TODO*///unsigned cpu_get_sp(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return GETSP(cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_set_sp(unsigned val)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	SETSP(cpunum,val);
/*TODO*///}
/*TODO*///
    /* these are available externally, for the timer system */
    public static int cycles_currently_ran() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return cycles_running - ICOUNT(cpunum);
    }

    public static int cycles_left_to_run() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return ICOUNT(cpunum);
    }

    /**
     * *************************************************************************
     * Returns the number of CPU cycles since the last reset of the CPU
     * IMPORTANT: this value wraps around in a relatively short time. For
     * example, for a 6Mhz CPU, it will wrap around in 2^32/6000000 = 716
     * seconds = 12 minutes. Make sure you don't do comparisons between values
     * returned by this function, but only use the difference (which will be
     * correct regardless of wraparound).
     * *************************************************************************
     */
    public static int cpu_gettotalcycles() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return cpu.get(cpunum).totalcycles + cycles_currently_ran();
    }

    /**
     * *************************************************************************
     * Returns the number of CPU cycles before the next interrupt handler call
     * *************************************************************************
     */
    public static int cpu_geticount() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        int result = TIME_TO_CYCLES(cpunum, cpu.get(cpunum).vblankint_period - timer_timeelapsed(cpu.get(cpunum).vblankint_timer));
        return (result < 0) ? 0 : result;
    }

    /**
     * *************************************************************************
     * Returns the number of CPU cycles before the end of the current video
     * frame
     * *************************************************************************
     */
    public static int cpu_getfcount() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        int result = TIME_TO_CYCLES(cpunum, refresh_period - timer_timeelapsed(refresh_timer));
        return (result < 0) ? 0 : result;
    }

    /**
     * *************************************************************************
     *
     * Returns the number of CPU cycles in one video frame
     *
     **************************************************************************
     */
    public static int cpu_getfperiod() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return TIME_TO_CYCLES(cpunum, refresh_period);
    }

    /**
     * *************************************************************************
     *
     * Scales a given value by the ratio of fcount / fperiod
     *
     **************************************************************************
     */
    public static int cpu_scalebyfcount(int value) {
        int result = (int) ((double) value * timer_timeelapsed(refresh_timer) * refresh_period_inv);
        if (value >= 0) {
            return (result < value) ? result : value;
        } else {
            return (result > value) ? result : value;
        }
    }

    /**
     * *************************************************************************
     * Returns the current scanline, or the time until a specific scanline Note:
     * cpu_getscanline() counts from 0, 0 being the first visible line. You
     * might have to adjust this value to match the hardware, since in many
     * cases the first visible line is >0.
     * *************************************************************************
     */
    public static int cpu_getscanline() {
        return (int) (timer_timeelapsed(refresh_timer) * scanline_period_inv);
    }

    public static double cpu_getscanlinetime(int scanline) {
        double ret;
        double scantime = timer_starttime(refresh_timer) + (double) scanline * scanline_period;
        double abstime = timer_get_time();
        if (abstime >= scantime) {
            scantime += TIME_IN_HZ(Machine.drv.frames_per_second);
        }
        ret = scantime - abstime;
        if (ret < TIME_IN_NSEC(1)) {
            ret = TIME_IN_HZ(Machine.drv.frames_per_second);
        }

        return ret;
    }

    public static double cpu_getscanlineperiod() {
        return scanline_period;
    }

    /**
     * *************************************************************************
     *
     * Returns the number of cycles in a scanline
     *
     **************************************************************************
     */
    public static int cpu_getscanlinecycles() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return TIME_TO_CYCLES(cpunum, scanline_period);
    }

    /**
     * *************************************************************************
     *
     * Returns the number of cycles since the beginning of this frame
     *
     **************************************************************************
     */
    public static int cpu_getcurrentcycles() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return TIME_TO_CYCLES(cpunum, timer_timeelapsed(refresh_timer));
    }

    /**
     * *************************************************************************
     *
     * Returns the current horizontal beam position in pixels
     *
     **************************************************************************
     */
    public static int cpu_gethorzbeampos() {
        double elapsed_time = timer_timeelapsed(refresh_timer);
        int scanline = (int) (elapsed_time * scanline_period_inv);
        double time_since_scanline = elapsed_time - (double) scanline * scanline_period;
        return (int) (time_since_scanline * scanline_period_inv * (double) Machine.drv.screen_width);
    }

    /**
     * *************************************************************************
     * Returns the number of times the interrupt handler will be called before
     * the end of the current video frame. This can be useful to interrupt
     * handlers to synchronize their operation. If you call this from outside an
     * interrupt handler, add 1 to the result, i.e. if it returns 0, it means
     * that the interrupt handler will be called once.
     * *************************************************************************
     */
    public static int cpu_getiloops() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return cpu.get(cpunum).iloops;
    }

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Interrupt handling
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  These functions are called when a cpu calls the callback sent to it's
/*TODO*///  set_irq_callback function. It clears the irq line if the current state
/*TODO*///  is HOLD_LINE and returns the interrupt vector for that line.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static int cpu_0_irq_callback(int irqline)
/*TODO*///{
/*TODO*///	if( irq_line_state[0 * MAX_IRQ_LINES + irqline] == HOLD_LINE )
/*TODO*///	{
/*TODO*///		SETIRQLINE(0, irqline, CLEAR_LINE);
/*TODO*///		irq_line_state[0 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
/*TODO*///	}
/*TODO*///	LOG((errorlog, "cpu_0_irq_callback(%d) $%04x\n", irqline, irq_line_vector[0 * MAX_IRQ_LINES + irqline]));
/*TODO*///	return irq_line_vector[0 * MAX_IRQ_LINES + irqline];
/*TODO*///}
/*TODO*///
/*TODO*///static int cpu_1_irq_callback(int irqline)
/*TODO*///{
/*TODO*///	if( irq_line_state[1 * MAX_IRQ_LINES + irqline] == HOLD_LINE )
/*TODO*///	{
/*TODO*///		SETIRQLINE(1, irqline, CLEAR_LINE);
/*TODO*///		irq_line_state[1 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
/*TODO*///	}
/*TODO*///	LOG((errorlog, "cpu_1_irq_callback(%d) $%04x\n", irqline, irq_line_vector[1 * MAX_IRQ_LINES + irqline]));
/*TODO*///	return irq_line_vector[1 * MAX_IRQ_LINES + irqline];
/*TODO*///}
/*TODO*///
/*TODO*///static int cpu_2_irq_callback(int irqline)
/*TODO*///{
/*TODO*///	if( irq_line_state[2 * MAX_IRQ_LINES + irqline] == HOLD_LINE )
/*TODO*///	{
/*TODO*///		SETIRQLINE(2, irqline, CLEAR_LINE);
/*TODO*///		irq_line_state[2 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
/*TODO*///	}
/*TODO*///	LOG((errorlog, "cpu_2_irq_callback(%d) $%04x\n", irqline, irq_line_vector[2 * MAX_IRQ_LINES + irqline]));
/*TODO*///	return irq_line_vector[2 * MAX_IRQ_LINES + irqline];
/*TODO*///}
/*TODO*///
/*TODO*///static int cpu_3_irq_callback(int irqline)
/*TODO*///{
/*TODO*///	if( irq_line_state[3 * MAX_IRQ_LINES + irqline] == HOLD_LINE )
/*TODO*///	{
/*TODO*///		SETIRQLINE(3, irqline, CLEAR_LINE);
/*TODO*///		irq_line_state[3 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
/*TODO*///	}
/*TODO*///	LOG((errorlog, "cpu_3_irq_callback(%d) $%04x\n", irqline, irq_line_vector[2 * MAX_IRQ_LINES + irqline]));
/*TODO*///	return irq_line_vector[3 * MAX_IRQ_LINES + irqline];
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  This function is used to generate internal interrupts (TMS34010)
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void cpu_generate_internal_interrupt(int cpunum, int type)
/*TODO*///{
/*TODO*///	timer_set(TIME_NOW, (cpunum & 7) | (type << 3), cpu_internalintcallback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use this functions to set the vector for a irq line of a CPU
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void cpu_irq_line_vector_w(int cpunum, int irqline, int vector)
/*TODO*///{
/*TODO*///	cpunum &= (MAX_CPU - 1);
/*TODO*///	irqline &= (MAX_IRQ_LINES - 1);
/*TODO*///	if( irqline < cpu[cpunum].intf->num_irqs )
/*TODO*///	{
/*TODO*///		LOG((errorlog,"cpu_irq_line_vector_w(%d,%d,$%04x)\n",cpunum,irqline,vector));
/*TODO*///		irq_line_vector[cpunum * MAX_IRQ_LINES + irqline] = vector;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	LOG((errorlog, "cpu_irq_line_vector_w CPU#%d irqline %d > max irq lines\n", cpunum, irqline));
/*TODO*///}
/*TODO*///
    /**
     * *************************************************************************
     * Use these functions to set the vector (data) for a irq line (offset) of
     * CPU #0 to #3
     * *************************************************************************
     */
    public static WriteHandlerPtr cpu_0_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(0, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_1_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(1, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_2_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(2, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_3_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(3, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_4_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(4, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_5_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(5, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_6_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(6, offset, data);
        }
    };
    public static WriteHandlerPtr cpu_7_irq_line_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_irq_line_vector_w(7, offset, data);
        }
    };

    /**
     * *************************************************************************
     * Use this function to set the state the NMI line of a CPU
     * *************************************************************************
     */
    public static void cpu_set_nmi_line(int cpunum, int state) {
        /* don't trigger interrupts on suspended CPUs */
        if (cpu_getstatus(cpunum) == 0) {
            return;
        }

        //LOG((errorlog,"cpu_set_nmi_line(%d,%d)\n",cpunum,state));
        timer_set(TIME_NOW, (cpunum & 7) | (state << 3), cpu_manualnmicallback);
    }

    /**
     * *************************************************************************
     * Use this function to set the state of an IRQ line of a CPU The meaning of
     * irqline varies between the different CPU types
     * *************************************************************************
     */
    public static void cpu_set_irq_line(int cpunum, int irqline, int state) {
        /* don't trigger interrupts on suspended CPUs */
        if (cpu_getstatus(cpunum) == 0) {
            return;
        }

        //LOG((errorlog,"cpu_set_irq_line(%d,%d,%d)\n",cpunum,irqline,state));
        timer_set(TIME_NOW, (irqline & 7) | ((cpunum & 7) << 3) | (state << 6), cpu_manualirqcallback);
    }

    /**
     * *************************************************************************
     * Use this function to cause an interrupt immediately (don't have to wait
     * until the next call to the interrupt handler)
     * *************************************************************************
     */
    public static void cpu_cause_interrupt(int cpunum, int type) {
        /* don't trigger interrupts on suspended CPUs */
        if (cpu_getstatus(cpunum) == 0) {
            return;
        }

        timer_set(TIME_NOW, (cpunum & 7) | (type << 3), cpu_manualintcallback);
    }

    public static void cpu_clear_pending_interrupts(int cpunum) {
        timer_set(TIME_NOW, cpunum, cpu_clearintcallback);
    }

    public static WriteHandlerPtr interrupt_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            interrupt_enable[cpunum] = data;

            /* make sure there are no queued interrupts */
            if (data == 0) {
                cpu_clear_pending_interrupts(cpunum);
            }
        }
    };

    public static WriteHandlerPtr interrupt_vector_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_vector[cpunum] != data) {
                //LOG((errorlog,"CPU#%d interrupt_vector_w $%02x\n", cpunum, data));
                interrupt_vector[cpunum] = data;

                /* make sure there are no queued interrupts */
                cpu_clear_pending_interrupts(cpunum);
            }
        }
    };

    public static InterruptHandlerPtr interrupt = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            int val;

            if (interrupt_enable[cpunum] == 0) {
                return INT_TYPE_NONE(cpunum);
            }

            val = INT_TYPE_IRQ(cpunum);
            if (val == -1000) {
                val = interrupt_vector[cpunum];
            }

            return val;
        }
    };

    public static InterruptHandlerPtr nmi_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;

            if (interrupt_enable[cpunum] == 0) {
                return INT_TYPE_NONE(cpunum);
            }
            return INT_TYPE_NMI(cpunum);
        }
    };

    /*TODO*///
/*TODO*///
/*TODO*///int m68_level1_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_1;
/*TODO*///}
/*TODO*///int m68_level2_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_2;
/*TODO*///}
/*TODO*///int m68_level3_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_3;
/*TODO*///}
/*TODO*///int m68_level4_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_4;
/*TODO*///}
/*TODO*///int m68_level5_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_5;
/*TODO*///}
/*TODO*///int m68_level6_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_6;
/*TODO*///}
/*TODO*///int m68_level7_irq(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	if (interrupt_enable[cpunum] == 0) return MC68000_INT_NONE;
/*TODO*///	return MC68000_IRQ_7;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int ignore_interrupt(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return INT_TYPE_NONE(cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  CPU timing and synchronization functions.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////* generate a trigger */
/*TODO*///void cpu_trigger(int trigger)
/*TODO*///{
/*TODO*///	timer_trigger(trigger);
/*TODO*///}
/*TODO*///
/*TODO*////* generate a trigger after a specific period of time */
/*TODO*///void cpu_triggertime(double duration, int trigger)
/*TODO*///{
/*TODO*///	timer_set(duration, trigger, cpu_trigger);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* burn CPU cycles until a timer trigger */
/*TODO*///void cpu_spinuntil_trigger(int trigger)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	timer_suspendcpu_trigger(cpunum, trigger);
/*TODO*///}
/*TODO*///
/*TODO*////* burn CPU cycles until the next interrupt */
/*TODO*///void cpu_spinuntil_int(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*////* burn CPU cycles until our timeslice is up */
/*TODO*///void cpu_spin(void)
/*TODO*///{
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*////* burn CPU cycles for a specific period of time */
/*TODO*///void cpu_spinuntil_time(double duration)
/*TODO*///{
/*TODO*///	static int timetrig = 0;
/*TODO*///
/*TODO*///	cpu_spinuntil_trigger(TRIGGER_SUSPENDTIME + timetrig);
/*TODO*///	cpu_triggertime(duration, TRIGGER_SUSPENDTIME + timetrig);
/*TODO*///	timetrig = (timetrig + 1) & 255;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////* yield our timeslice for a specific period of time */
/*TODO*///void cpu_yielduntil_trigger(int trigger)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	timer_holdcpu_trigger(cpunum, trigger);
/*TODO*///}
/*TODO*///
/*TODO*////* yield our timeslice until the next interrupt */
/*TODO*///void cpu_yielduntil_int(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*////* yield our current timeslice */
/*TODO*///void cpu_yield(void)
/*TODO*///{
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*////* yield our timeslice for a specific period of time */
/*TODO*///void cpu_yielduntil_time(double duration)
/*TODO*///{
/*TODO*///	static int timetrig = 0;
/*TODO*///
/*TODO*///	cpu_yielduntil_trigger(TRIGGER_YIELDTIME + timetrig);
/*TODO*///	cpu_triggertime(duration, TRIGGER_YIELDTIME + timetrig);
/*TODO*///	timetrig = (timetrig + 1) & 255;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int cpu_getvblank(void)
/*TODO*///{
/*TODO*///	return vblank;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int cpu_getcurrentframe(void)
/*TODO*///{
/*TODO*///	return current_frame;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Internal CPU event processors.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///static void cpu_manualnmicallback(int param)
/*TODO*///{
/*TODO*///	int cpunum, state, oldactive;
/*TODO*///	cpunum = param & 7;
/*TODO*///	state = param >> 3;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	LOG((errorlog,"cpu_manualnmicallback %d,%d\n",cpunum,state));
/*TODO*///
/*TODO*///	switch (state)
/*TODO*///	{
/*TODO*///		case PULSE_LINE:
/*TODO*///			SETNMILINE(cpunum,ASSERT_LINE);
/*TODO*///			SETNMILINE(cpunum,CLEAR_LINE);
/*TODO*///			break;
/*TODO*///		case HOLD_LINE:
/*TODO*///		case ASSERT_LINE:
/*TODO*///			SETNMILINE(cpunum,ASSERT_LINE);
/*TODO*///			break;
/*TODO*///		case CLEAR_LINE:
/*TODO*///			SETNMILINE(cpunum,CLEAR_LINE);
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			if( errorlog ) fprintf( errorlog, "cpu_manualnmicallback cpu #%d unknown state %d\n", cpunum, state);
/*TODO*///	}
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	/* generate a trigger to unsuspend any CPUs waiting on the interrupt */
/*TODO*///	if (state != CLEAR_LINE)
/*TODO*///		timer_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///static void cpu_manualirqcallback(int param)
/*TODO*///{
/*TODO*///	int cpunum, irqline, state, oldactive;
/*TODO*///
/*TODO*///	irqline = param & 7;
/*TODO*///	cpunum = (param >> 3) & 7;
/*TODO*///	state = param >> 6;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	LOG((errorlog,"cpu_manualirqcallback %d,%d,%d\n",cpunum,irqline,state));
/*TODO*///
/*TODO*///	irq_line_state[cpunum * MAX_IRQ_LINES + irqline] = state;
/*TODO*///	switch (state)
/*TODO*///	{
/*TODO*///		case PULSE_LINE:
/*TODO*///			SETIRQLINE(cpunum,irqline,ASSERT_LINE);
/*TODO*///			SETIRQLINE(cpunum,irqline,CLEAR_LINE);
/*TODO*///			break;
/*TODO*///		case HOLD_LINE:
/*TODO*///		case ASSERT_LINE:
/*TODO*///			SETIRQLINE(cpunum,irqline,ASSERT_LINE);
/*TODO*///			break;
/*TODO*///		case CLEAR_LINE:
/*TODO*///			SETIRQLINE(cpunum,irqline,CLEAR_LINE);
/*TODO*///			break;
/*TODO*///		default:
/*TODO*///			if( errorlog ) fprintf( errorlog, "cpu_manualirqcallback cpu #%d, line %d, unknown state %d\n", cpunum, irqline, state);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	/* generate a trigger to unsuspend any CPUs waiting on the interrupt */
/*TODO*///	if (state != CLEAR_LINE)
/*TODO*///		timer_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///static void cpu_internal_interrupt(int cpunum, int type)
/*TODO*///{
/*TODO*///	int oldactive = activecpu;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	INTERNAL_INTERRUPT(cpunum, type);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	/* generate a trigger to unsuspend any CPUs waiting on the interrupt */
/*TODO*///	timer_trigger(TRIGGER_INT + cpunum);
/*TODO*///}
/*TODO*///
/*TODO*///static void cpu_internalintcallback(int param)
/*TODO*///{
/*TODO*///	int type = param >> 3;
/*TODO*///	int cpunum = param & 7;
/*TODO*///
/*TODO*///	LOG((errorlog,"CPU#%d internal interrupt type $%04x\n", cpunum, type));
/*TODO*///	/* generate the interrupt */
/*TODO*///	cpu_internal_interrupt(cpunum, type);
/*TODO*///}
/*TODO*///
/*TODO*///static void cpu_generate_interrupt(int cpunum, int (*func)(void), int num)
/*TODO*///{
/*TODO*///	int oldactive = activecpu;
/*TODO*///
/*TODO*///	/* don't trigger interrupts on suspended CPUs */
/*TODO*///	if (cpu_getstatus(cpunum) == 0) return;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	/* cause the interrupt, calling the function if it exists */
/*TODO*///	if (func) num = (*func)();
/*TODO*///
/*TODO*///	/* wrapper for the new interrupt system */
/*TODO*///	if (num != INT_TYPE_NONE(cpunum))
/*TODO*///	{
/*TODO*///		LOG((errorlog,"CPU#%d interrupt type $%04x: ", cpunum, num));
/*TODO*///		/* is it the NMI type interrupt of that CPU? */
/*TODO*///		if (num == INT_TYPE_NMI(cpunum))
/*TODO*///		{
/*TODO*///
/*TODO*///			LOG((errorlog,"NMI\n"));
/*TODO*///			cpu_manualnmicallback(cpunum | (PULSE_LINE << 3) );
/*TODO*///
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int irq_line;
/*TODO*///
/*TODO*///			switch (CPU_TYPE(cpunum))
/*TODO*///			{
/*TODO*///#if (HAS_Z80)
/*TODO*///			case CPU_Z80:				irq_line = 0; LOG((errorlog,"Z80 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_8080)
/*TODO*///			case CPU_8080:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case I8080_INTR:		irq_line = 0; LOG((errorlog,"I8080 INTR\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"I8080 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_8085A)
/*TODO*///			case CPU_8085A:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case I8085_INTR:		irq_line = 0; LOG((errorlog,"I8085 INTR\n")); break;
/*TODO*///				case I8085_RST55:		irq_line = 1; LOG((errorlog,"I8085 RST55\n")); break;
/*TODO*///				case I8085_RST65:		irq_line = 2; LOG((errorlog,"I8085 RST65\n")); break;
/*TODO*///				case I8085_RST75:		irq_line = 3; LOG((errorlog,"I8085 RST75\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"I8085 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6502)
/*TODO*///			case CPU_M6502: 			irq_line = 0; LOG((errorlog,"M6502 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M65C02)
/*TODO*///			case CPU_M65C02:			irq_line = 0; LOG((errorlog,"M65C02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M65SC02)
/*TODO*///			case CPU_M65SC02:			irq_line = 0; LOG((errorlog,"M65SC02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///			case CPU_M65CE02:			irq_line = 0; LOG((errorlog,"M65CE02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///			case CPU_M6509: 			irq_line = 0; LOG((errorlog,"M6509 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6510)
/*TODO*///			case CPU_M6510: 			irq_line = 0; LOG((errorlog,"M6510 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_N2A03)
/*TODO*///			case CPU_N2A03: 			irq_line = 0; LOG((errorlog,"N2A03 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_H6280)
/*TODO*///			case CPU_H6280:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case H6280_INT_IRQ1:	irq_line = 0; LOG((errorlog,"H6280 INT 1\n")); break;
/*TODO*///				case H6280_INT_IRQ2:	irq_line = 1; LOG((errorlog,"H6280 INT 2\n")); break;
/*TODO*///				case H6280_INT_TIMER:	irq_line = 2; LOG((errorlog,"H6280 TIMER INT\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"H6280 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///			case CPU_I86:				irq_line = 0; LOG((errorlog,"I86 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_V20)
/*TODO*///			case CPU_V20:				irq_line = 0; LOG((errorlog,"V20 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_V30)
/*TODO*///			case CPU_V30:				irq_line = 0; LOG((errorlog,"V30 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_V33)
/*TODO*///			case CPU_V33:				irq_line = 0; LOG((errorlog,"V33 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I8035)
/*TODO*///			case CPU_I8035: 			irq_line = 0; LOG((errorlog,"I8035 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I8039)
/*TODO*///			case CPU_I8039: 			irq_line = 0; LOG((errorlog,"I8039 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I8048)
/*TODO*///			case CPU_I8048: 			irq_line = 0; LOG((errorlog,"I8048 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_N7751)
/*TODO*///			case CPU_N7751: 			irq_line = 0; LOG((errorlog,"N7751 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6800)
/*TODO*///			case CPU_M6800: 			irq_line = 0; LOG((errorlog,"M6800 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6801)
/*TODO*///			case CPU_M6801: 			irq_line = 0; LOG((errorlog,"M6801 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6802)
/*TODO*///			case CPU_M6802: 			irq_line = 0; LOG((errorlog,"M6802 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6803)
/*TODO*///			case CPU_M6803: 			irq_line = 0; LOG((errorlog,"M6803 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6808)
/*TODO*///			case CPU_M6808: 			irq_line = 0; LOG((errorlog,"M6808 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_HD63701)
/*TODO*///			case CPU_HD63701:			irq_line = 0; LOG((errorlog,"HD63701 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6805)
/*TODO*///			case CPU_M6805: 			irq_line = 0; LOG((errorlog,"M6805 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68705)
/*TODO*///			case CPU_M68705:			irq_line = 0; LOG((errorlog,"M68705 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_HD63705)
/*TODO*///			case CPU_HD63705:			irq_line = 0; LOG((errorlog,"HD68705 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_HD6309)
/*TODO*///			case CPU_HD6309:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case HD6309_INT_IRQ:	irq_line = 0; LOG((errorlog,"M6309 IRQ\n")); break;
/*TODO*///				case HD6309_INT_FIRQ:	irq_line = 1; LOG((errorlog,"M6309 FIRQ\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M6309 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6809)
/*TODO*///			case CPU_M6809:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case M6809_INT_IRQ: 	irq_line = 0; LOG((errorlog,"M6809 IRQ\n")); break;
/*TODO*///				case M6809_INT_FIRQ:	irq_line = 1; LOG((errorlog,"M6809 FIRQ\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M6809 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_KONAMI)
/*TODO*///				case CPU_KONAMI:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case KONAMI_INT_IRQ:	irq_line = 0; LOG((errorlog,"KONAMI IRQ\n")); break;
/*TODO*///				case KONAMI_INT_FIRQ:	irq_line = 1; LOG((errorlog,"KONAMI FIRQ\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"KONAMI unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68000)
/*TODO*///			case CPU_M68000:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68000_IRQ_1: 	irq_line = 1; LOG((errorlog,"M68K IRQ1\n")); break;
/*TODO*///				case MC68000_IRQ_2: 	irq_line = 2; LOG((errorlog,"M68K IRQ2\n")); break;
/*TODO*///				case MC68000_IRQ_3: 	irq_line = 3; LOG((errorlog,"M68K IRQ3\n")); break;
/*TODO*///				case MC68000_IRQ_4: 	irq_line = 4; LOG((errorlog,"M68K IRQ4\n")); break;
/*TODO*///				case MC68000_IRQ_5: 	irq_line = 5; LOG((errorlog,"M68K IRQ5\n")); break;
/*TODO*///				case MC68000_IRQ_6: 	irq_line = 6; LOG((errorlog,"M68K IRQ6\n")); break;
/*TODO*///				case MC68000_IRQ_7: 	irq_line = 7; LOG((errorlog,"M68K IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M68K unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68010)
/*TODO*///			case CPU_M68010:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68010_IRQ_1: 	irq_line = 1; LOG((errorlog,"M68010 IRQ1\n")); break;
/*TODO*///				case MC68010_IRQ_2: 	irq_line = 2; LOG((errorlog,"M68010 IRQ2\n")); break;
/*TODO*///				case MC68010_IRQ_3: 	irq_line = 3; LOG((errorlog,"M68010 IRQ3\n")); break;
/*TODO*///				case MC68010_IRQ_4: 	irq_line = 4; LOG((errorlog,"M68010 IRQ4\n")); break;
/*TODO*///				case MC68010_IRQ_5: 	irq_line = 5; LOG((errorlog,"M68010 IRQ5\n")); break;
/*TODO*///				case MC68010_IRQ_6: 	irq_line = 6; LOG((errorlog,"M68010 IRQ6\n")); break;
/*TODO*///				case MC68010_IRQ_7: 	irq_line = 7; LOG((errorlog,"M68010 IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M68010 unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68020)
/*TODO*///			case CPU_M68020:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68020_IRQ_1: 	irq_line = 1; LOG((errorlog,"M68020 IRQ1\n")); break;
/*TODO*///				case MC68020_IRQ_2: 	irq_line = 2; LOG((errorlog,"M68020 IRQ2\n")); break;
/*TODO*///				case MC68020_IRQ_3: 	irq_line = 3; LOG((errorlog,"M68020 IRQ3\n")); break;
/*TODO*///				case MC68020_IRQ_4: 	irq_line = 4; LOG((errorlog,"M68020 IRQ4\n")); break;
/*TODO*///				case MC68020_IRQ_5: 	irq_line = 5; LOG((errorlog,"M68020 IRQ5\n")); break;
/*TODO*///				case MC68020_IRQ_6: 	irq_line = 6; LOG((errorlog,"M68020 IRQ6\n")); break;
/*TODO*///				case MC68020_IRQ_7: 	irq_line = 7; LOG((errorlog,"M68020 IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M68020 unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68EC020)
/*TODO*///			case CPU_M68EC020:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68EC020_IRQ_1:	irq_line = 1; LOG((errorlog,"M68EC020 IRQ1\n")); break;
/*TODO*///				case MC68EC020_IRQ_2:	irq_line = 2; LOG((errorlog,"M68EC020 IRQ2\n")); break;
/*TODO*///				case MC68EC020_IRQ_3:	irq_line = 3; LOG((errorlog,"M68EC020 IRQ3\n")); break;
/*TODO*///				case MC68EC020_IRQ_4:	irq_line = 4; LOG((errorlog,"M68EC020 IRQ4\n")); break;
/*TODO*///				case MC68EC020_IRQ_5:	irq_line = 5; LOG((errorlog,"M68EC020 IRQ5\n")); break;
/*TODO*///				case MC68EC020_IRQ_6:	irq_line = 6; LOG((errorlog,"M68EC020 IRQ6\n")); break;
/*TODO*///				case MC68EC020_IRQ_7:	irq_line = 7; LOG((errorlog,"M68EC020 IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"M68EC020 unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if HAS_T11
/*TODO*///			case CPU_T11:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case T11_IRQ0:			irq_line = 0; LOG((errorlog,"T11 IRQ0\n")); break;
/*TODO*///				case T11_IRQ1:			irq_line = 1; LOG((errorlog,"T11 IRQ1\n")); break;
/*TODO*///				case T11_IRQ2:			irq_line = 2; LOG((errorlog,"T11 IRQ2\n")); break;
/*TODO*///				case T11_IRQ3:			irq_line = 3; LOG((errorlog,"T11 IRQ3\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"T11 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if HAS_S2650
/*TODO*///			case CPU_S2650: 			irq_line = 0; LOG((errorlog,"S2650 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if HAS_TMS34010
/*TODO*///			case CPU_TMS34010:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case TMS34010_INT1: 	irq_line = 0; LOG((errorlog,"TMS34010 INT1\n")); break;
/*TODO*///				case TMS34010_INT2: 	irq_line = 1; LOG((errorlog,"TMS34010 INT2\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"TMS34010 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*////*#if HAS_TMS9900
/*TODO*///			case CPU_TMS9900:	irq_line = 0; LOG((errorlog,"TMS9900 IRQ\n")); break;
/*TODO*///#endif*/
/*TODO*///#if (HAS_TMS9900) || (HAS_TMS9940) || (HAS_TMS9980) || (HAS_TMS9985) \
/*TODO*///	|| (HAS_TMS9989) || (HAS_TMS9995) || (HAS_TMS99105A) || (HAS_TMS99110A)
/*TODO*///	#if (HAS_TMS9900)
/*TODO*///			case CPU_TMS9900:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS9940)
/*TODO*///			case CPU_TMS9940:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS9980)
/*TODO*///			case CPU_TMS9980:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS9985)
/*TODO*///			case CPU_TMS9985:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS9989)
/*TODO*///			case CPU_TMS9989:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS9995)
/*TODO*///			case CPU_TMS9995:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS99105A)
/*TODO*///			case CPU_TMS99105A:
/*TODO*///	#endif
/*TODO*///	#if (HAS_TMS99110A)
/*TODO*///			case CPU_TMS99110A:
/*TODO*///	#endif
/*TODO*///				LOG((errorlog,"Please use the new interrupt scheme for your new developments !\n"));
/*TODO*///				irq_line = 0;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if HAS_Z8000
/*TODO*///			case CPU_Z8000:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case Z8000_NVI: 		irq_line = 0; LOG((errorlog,"Z8000 NVI\n")); break;
/*TODO*///				case Z8000_VI:			irq_line = 1; LOG((errorlog,"Z8000 VI\n")); break;
/*TODO*///				default:				irq_line = 0; LOG((errorlog,"Z8000 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if HAS_TMS320C10
/*TODO*///			case CPU_TMS320C10:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case TMS320C10_ACTIVE_INT:	irq_line = 0; LOG((errorlog,"TMS32010 INT\n")); break;
/*TODO*///				case TMS320C10_ACTIVE_BIO:	irq_line = 1; LOG((errorlog,"TMS32010 BIO\n")); break;
/*TODO*///				default:					irq_line = 0; LOG((errorlog,"TMS32010 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if HAS_ADSP2100
/*TODO*///			case CPU_ADSP2100:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case ADSP2100_IRQ0: 		irq_line = 0; LOG((errorlog,"ADSP2100 IRQ0\n")); break;
/*TODO*///				case ADSP2100_IRQ1: 		irq_line = 1; LOG((errorlog,"ADSP2100 IRQ1\n")); break;
/*TODO*///				case ADSP2100_IRQ2: 		irq_line = 2; LOG((errorlog,"ADSP2100 IRQ1\n")); break;
/*TODO*///				case ADSP2100_IRQ3: 		irq_line = 3; LOG((errorlog,"ADSP2100 IRQ1\n")); break;
/*TODO*///				default:					irq_line = 0; LOG((errorlog,"ADSP2100 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///			default:
/*TODO*///				irq_line = 0;
/*TODO*///				/* else it should be an IRQ type; assume line 0 and store vector */
/*TODO*///				LOG((errorlog,"unknown IRQ\n"));
/*TODO*///			}
/*TODO*///			cpu_irq_line_vector_w(cpunum, irq_line, num);
/*TODO*///			cpu_manualirqcallback(irq_line | (cpunum << 3) | (HOLD_LINE << 6) );
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	/* trigger already generated by cpu_manualirqcallback or cpu_manualnmicallback */
/*TODO*///}
/*TODO*///
/*TODO*///static void cpu_clear_interrupts(int cpunum)
/*TODO*///{
/*TODO*///	int oldactive = activecpu;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	/* clear NMI line */
/*TODO*///	SETNMILINE(activecpu,CLEAR_LINE);
/*TODO*///
/*TODO*///	/* clear all IRQ lines */
/*TODO*///	for (i = 0; i < cpu[activecpu].intf->num_irqs; i++)
/*TODO*///		SETIRQLINE(activecpu,i,CLEAR_LINE);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void cpu_reset_cpu(int cpunum)
/*TODO*///{
/*TODO*///	int oldactive = activecpu;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	/* reset the CPU */
/*TODO*///	RESET(cpunum);
/*TODO*///
/*TODO*///	/* Set the irq callback for the cpu */
/*TODO*///	SETIRQCALLBACK(cpunum,cpu_irq_callbacks[cpunum]);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///}
/*TODO*///
    /**
     * *************************************************************************
     * Interrupt callback. This is called once per CPU interrupt by either the
     * VBLANK handler or by the CPU's own timer directly, depending on whether
     * or not the CPU's interrupts are synced to VBLANK.
     * *************************************************************************
     */
    public static void cpu_vblankintcallback(int param) {
        if (Machine.drv.cpu[param].vblank_interrupt != null) {
            cpu_generate_interrupt(param, Machine.drv.cpu[param].vblank_interrupt, 0);
        }

        /* update the counters */
        cpu.get(param).iloops--;
    }

    public static TimerCallbackHandlerPtr cpu_timedintcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            /* bail if there is no routine */
            if (Machine.drv.cpu[param].timed_interrupt == null) {
                return;
            }

            /* generate the interrupt */
            cpu_generate_interrupt(param, Machine.drv.cpu[param].timed_interrupt, 0);
        }
    };

    public static TimerCallbackHandlerPtr cpu_manualintcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int intnum = param >> 3;
            int cpunum = param & 7;

            /* generate the interrupt */
            cpu_generate_interrupt(cpunum, null, intnum);
        }
    };

    public static TimerCallbackHandlerPtr cpu_clearintcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            /* clear the interrupts */
            cpu_clear_interrupts(param);
        }
    };

    public static TimerCallbackHandlerPtr cpu_resetcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int state = param >> 3;
            int cpunum = param & 7;

            /* reset the CPU */
            if (state == PULSE_LINE) {
                cpu_reset_cpu(cpunum);
            } else if (state == ASSERT_LINE) {
                /* ASG - do we need this?		cpu_reset_cpu(cpunum);*/
                timer_suspendcpu(cpunum, 1, SUSPEND_REASON_RESET);
                /* halt cpu */
            } else if (state == CLEAR_LINE) {
                if (timer_iscpususpended(cpunum, SUSPEND_REASON_RESET) != 0) {
                    cpu_reset_cpu(cpunum);
                }
                timer_suspendcpu(cpunum, 0, SUSPEND_REASON_RESET);/* restart cpu */
            }
        }
    };


    /*TODO*///static void cpu_haltcallback(int param)
/*TODO*///{
/*TODO*///	int state = param >> 3;
/*TODO*///	int cpunum = param & 7;
/*TODO*///
/*TODO*///	/* reset the CPU */
/*TODO*///	if (state == ASSERT_LINE)
/*TODO*///		timer_suspendcpu(cpunum, 1, SUSPEND_REASON_HALT);	/* halt cpu */
/*TODO*///	else if (state == CLEAR_LINE)
/*TODO*///		timer_suspendcpu(cpunum, 0, SUSPEND_REASON_HALT);	/* restart cpu */
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  VBLANK reset. Called at the start of emulation and once per VBLANK in
/*TODO*///  order to update the input ports and reset the interrupt counter.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static void cpu_vblankreset(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* read hi scores from disk */
/*TODO*///profiler_mark(PROFILER_HISCORE);
/*TODO*///	hs_update();
/*TODO*///profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///	/* read keyboard & update the status of the input ports */
/*TODO*///	update_input_ports();
/*TODO*///
/*TODO*///	/* reset the cycle counters */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		if (!timer_iscpususpended(i, SUSPEND_ANY_REASON))
/*TODO*///			cpu[i].iloops = Machine->drv->cpu[i].vblank_interrupts_per_frame - 1;
/*TODO*///		else
/*TODO*///			cpu[i].iloops = -1;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  VBLANK callback. This is called 'vblank_multipler' times per frame to
/*TODO*///  service VBLANK-synced interrupts and to begin the screen update process.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static void cpu_firstvblankcallback(int param)
/*TODO*///{
/*TODO*///	/* now that we're synced up, pulse from here on out */
/*TODO*///	vblank_timer = timer_pulse(vblank_period, param, cpu_vblankcallback);
/*TODO*///
/*TODO*///	/* but we need to call the standard routine as well */
/*TODO*///	cpu_vblankcallback(param);
/*TODO*///}
/*TODO*///
/*TODO*////* note that calling this with param == -1 means count everything, but call no subroutines */
/*TODO*///static void cpu_vblankcallback(int param)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	/* loop over CPUs */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		/* if the interrupt multiplier is valid */
/*TODO*///		if (cpu[i].vblankint_multiplier != -1)
/*TODO*///		{
/*TODO*///			/* decrement; if we hit zero, generate the interrupt and reset the countdown */
/*TODO*///			if (!--cpu[i].vblankint_countdown)
/*TODO*///			{
/*TODO*///				if (param != -1)
/*TODO*///					cpu_vblankintcallback(i);
/*TODO*///				cpu[i].vblankint_countdown = cpu[i].vblankint_multiplier;
/*TODO*///				timer_reset(cpu[i].vblankint_timer, TIME_NEVER);
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		/* else reset the VBLANK timer if this is going to be a real VBLANK */
/*TODO*///		else if (vblank_countdown == 1)
/*TODO*///			timer_reset(cpu[i].vblankint_timer, TIME_NEVER);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* is it a real VBLANK? */
/*TODO*///	if (!--vblank_countdown)
/*TODO*///	{
/*TODO*///		/* do we update the screen now? */
/*TODO*///		if (!(Machine->drv->video_attributes & VIDEO_UPDATE_AFTER_VBLANK))
/*TODO*///			usres = updatescreen();
/*TODO*///
/*TODO*///		/* Set the timer to update the screen */
/*TODO*///		timer_set(TIME_IN_USEC(Machine->drv->vblank_duration), 0, cpu_updatecallback);
/*TODO*///		vblank = 1;
/*TODO*///
/*TODO*///		/* reset the globals */
/*TODO*///		cpu_vblankreset();
/*TODO*///
/*TODO*///		/* reset the counter */
/*TODO*///		vblank_countdown = vblank_multiplier;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Video update callback. This is called a game-dependent amount of time
/*TODO*///  after the VBLANK in order to trigger a video update.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static void cpu_updatecallback(int param)
/*TODO*///{
/*TODO*///	/* update the screen if we didn't before */
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_UPDATE_AFTER_VBLANK)
/*TODO*///		usres = updatescreen();
/*TODO*///	vblank = 0;
/*TODO*///
/*TODO*///	/* update IPT_VBLANK input ports */
/*TODO*///	inputport_vblank_end();
/*TODO*///
/*TODO*///	/* check the watchdog */
/*TODO*///	if (watchdog_counter > 0)
/*TODO*///	{
/*TODO*///		if (--watchdog_counter == 0)
/*TODO*///		{
/*TODO*///if (errorlog) fprintf(errorlog,"reset caused by the watchdog\n");
/*TODO*///			machine_reset();
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	current_frame++;
/*TODO*///
/*TODO*///	/* reset the refresh timer */
/*TODO*///	timer_reset(refresh_timer, TIME_NEVER);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Converts an integral timing rate into a period. Rates can be specified
/*TODO*///  as follows:
/*TODO*///
/*TODO*///		rate > 0	   -> 'rate' cycles per frame
/*TODO*///		rate == 0	   -> 0
/*TODO*///		rate >= -10000 -> 'rate' cycles per second
/*TODO*///		rate < -10000  -> 'rate' nanoseconds
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static double cpu_computerate(int value)
/*TODO*///{
/*TODO*///	/* values equal to zero are zero */
/*TODO*///	if (value <= 0)
/*TODO*///		return 0.0;
/*TODO*///
/*TODO*///	/* values above between 0 and 50000 are in Hz */
/*TODO*///	if (value < 50000)
/*TODO*///		return TIME_IN_HZ(value);
/*TODO*///
/*TODO*///	/* values greater than 50000 are in nanoseconds */
/*TODO*///	else
/*TODO*///		return TIME_IN_NSEC(value);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void cpu_timeslicecallback(int param)
/*TODO*///{
/*TODO*///	timer_trigger(TRIGGER_TIMESLICE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Initializes all the timers used by the CPU system.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static void cpu_inittimers(void)
/*TODO*///{
/*TODO*///	double first_time;
/*TODO*///	int i, max, ipf;
/*TODO*///
/*TODO*///	/* remove old timers */
/*TODO*///	if (timeslice_timer)
/*TODO*///		timer_remove(timeslice_timer);
/*TODO*///	if (refresh_timer)
/*TODO*///		timer_remove(refresh_timer);
/*TODO*///	if (vblank_timer)
/*TODO*///		timer_remove(vblank_timer);
/*TODO*///
/*TODO*///	/* allocate a dummy timer at the minimum frequency to break things up */
/*TODO*///	ipf = Machine->drv->cpu_slices_per_frame;
/*TODO*///	if (ipf <= 0)
/*TODO*///		ipf = 1;
/*TODO*///	timeslice_period = TIME_IN_HZ(Machine->drv->frames_per_second * ipf);
/*TODO*///	timeslice_timer = timer_pulse(timeslice_period, 0, cpu_timeslicecallback);
/*TODO*///
/*TODO*///	/* allocate an infinite timer to track elapsed time since the last refresh */
/*TODO*///	refresh_period = TIME_IN_HZ(Machine->drv->frames_per_second);
/*TODO*///	refresh_period_inv = 1.0 / refresh_period;
/*TODO*///	refresh_timer = timer_set(TIME_NEVER, 0, NULL);
/*TODO*///
/*TODO*///	/* while we're at it, compute the scanline times */
/*TODO*///	if (Machine->drv->vblank_duration)
/*TODO*///		scanline_period = (refresh_period - TIME_IN_USEC(Machine->drv->vblank_duration)) /
/*TODO*///				(double)(Machine->drv->visible_area.max_y - Machine->drv->visible_area.min_y + 1);
/*TODO*///	else
/*TODO*///		scanline_period = refresh_period / (double)Machine->drv->screen_height;
/*TODO*///	scanline_period_inv = 1.0 / scanline_period;
/*TODO*///
/*TODO*///	/*
/*TODO*///	 *		The following code finds all the CPUs that are interrupting in sync with the VBLANK
/*TODO*///	 *		and sets up the VBLANK timer to run at the minimum number of cycles per frame in
/*TODO*///	 *		order to service all the synced interrupts
/*TODO*///	 */
/*TODO*///
/*TODO*///	/* find the CPU with the maximum interrupts per frame */
/*TODO*///	max = 1;
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[i].vblank_interrupts_per_frame;
/*TODO*///		if (ipf > max)
/*TODO*///			max = ipf;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now find the LCD with the rest of the CPUs (brute force - these numbers aren't huge) */
/*TODO*///	vblank_multiplier = max;
/*TODO*///	while (1)
/*TODO*///	{
/*TODO*///		for (i = 0; i < totalcpu; i++)
/*TODO*///		{
/*TODO*///			ipf = Machine->drv->cpu[i].vblank_interrupts_per_frame;
/*TODO*///			if (ipf > 0 && (vblank_multiplier % ipf) != 0)
/*TODO*///				break;
/*TODO*///		}
/*TODO*///		if (i == totalcpu)
/*TODO*///			break;
/*TODO*///		vblank_multiplier += max;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* initialize the countdown timers and intervals */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[i].vblank_interrupts_per_frame;
/*TODO*///		if (ipf > 0)
/*TODO*///			cpu[i].vblankint_countdown = cpu[i].vblankint_multiplier = vblank_multiplier / ipf;
/*TODO*///		else
/*TODO*///			cpu[i].vblankint_countdown = cpu[i].vblankint_multiplier = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* allocate a vblank timer at the frame rate * the LCD number of interrupts per frame */
/*TODO*///	vblank_period = TIME_IN_HZ(Machine->drv->frames_per_second * vblank_multiplier);
/*TODO*///	vblank_timer = timer_pulse(vblank_period, 0, cpu_vblankcallback);
/*TODO*///	vblank_countdown = vblank_multiplier;
/*TODO*///
/*TODO*///	/*
/*TODO*///	 *		The following code creates individual timers for each CPU whose interrupts are not
/*TODO*///	 *		synced to the VBLANK, and computes the typical number of cycles per interrupt
/*TODO*///	 */
/*TODO*///
/*TODO*///	/* start the CPU interrupt timers */
/*TODO*///	for (i = 0; i < totalcpu; i++)
/*TODO*///	{
/*TODO*///		ipf = Machine->drv->cpu[i].vblank_interrupts_per_frame;
/*TODO*///
/*TODO*///		/* remove old timers */
/*TODO*///		if (cpu[i].vblankint_timer)
/*TODO*///			timer_remove(cpu[i].vblankint_timer);
/*TODO*///		if (cpu[i].timedint_timer)
/*TODO*///			timer_remove(cpu[i].timedint_timer);
/*TODO*///
/*TODO*///		/* compute the average number of cycles per interrupt */
/*TODO*///		if (ipf <= 0)
/*TODO*///			ipf = 1;
/*TODO*///		cpu[i].vblankint_period = TIME_IN_HZ(Machine->drv->frames_per_second * ipf);
/*TODO*///		cpu[i].vblankint_timer = timer_set(TIME_NEVER, 0, NULL);
/*TODO*///
/*TODO*///		/* see if we need to allocate a CPU timer */
/*TODO*///		ipf = Machine->drv->cpu[i].timed_interrupts_per_second;
/*TODO*///		if (ipf)
/*TODO*///		{
/*TODO*///			cpu[i].timedint_period = cpu_computerate(ipf);
/*TODO*///			cpu[i].timedint_timer = timer_pulse(cpu[i].timedint_period, i, cpu_timedintcallback);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* note that since we start the first frame on the refresh, we can't pulse starting
/*TODO*///	   immediately; instead, we back up one VBLANK period, and inch forward until we hit
/*TODO*///	   positive time. That time will be the time of the first VBLANK timer callback */
/*TODO*///	timer_remove(vblank_timer);
/*TODO*///
/*TODO*///	first_time = -TIME_IN_USEC(Machine->drv->vblank_duration) + vblank_period;
/*TODO*///	while (first_time < 0)
/*TODO*///	{
/*TODO*///		cpu_vblankcallback(-1);
/*TODO*///		first_time += vblank_period;
/*TODO*///	}
/*TODO*///	vblank_timer = timer_set(first_time, 0, cpu_firstvblankcallback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* AJP 981016 */
/*TODO*///int cpu_is_saving_context(int _activecpu)
/*TODO*///{
/*TODO*///	return (cpu[_activecpu].save_context);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* JB 971019 */
/*TODO*///void* cpu_getcontext(int _activecpu)
/*TODO*///{
/*TODO*///	return cpu[_activecpu].context;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Retrieve or set the entire context of the active CPU
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///unsigned cpu_get_context(void *context)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return GETCONTEXT(cpunum,context);
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_set_context(void *context)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	SETCONTEXT(cpunum,context);
/*TODO*///}
/*TODO*///
    /**
     * *************************************************************************
     * Retrieve or set the value of a specific register of the active CPU
     * *************************************************************************
     */
    public static int cpu_get_reg(int regnum) {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return GETREG(cpunum, regnum);
    }
    /*TODO*///
/*TODO*///void cpu_set_reg(int regnum, unsigned val)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	SETREG(cpunum,regnum,val);
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Get various CPU information
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the number of address bits for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_address_bits(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpuintf[CPU_TYPE(cpunum)].address_bits;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the address bit mask for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_address_mask(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return (1 << cpuintf[CPU_TYPE(cpunum)].address_bits) - 1;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the address shift factor for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///int cpu_address_shift(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpuintf[CPU_TYPE(cpunum)].address_shift;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the endianess for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_endianess(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpuintf[CPU_TYPE(cpunum)].endianess;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the code align unit for the active CPU (1 byte, 2 word, ...)
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_align_unit(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpuintf[CPU_TYPE(cpunum)].align_unit;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the max. instruction length for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_max_inst_len(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return cpuintf[CPU_TYPE(cpunum)].max_inst_len;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the name for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_name(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_NAME);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the family name for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_core_family(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_FAMILY);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the version number for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_core_version(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_VERSION);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the core filename for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_core_file(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_FILE);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the credits for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_core_credits(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_CREDITS);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the register layout for the active CPU (debugger)
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_reg_layout(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_REG_LAYOUT);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the window layout for the active CPU (debugger)
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_win_layout(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_WIN_LAYOUT);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns a dissassembled instruction for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpu_dasm(char *buffer, unsigned pc)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUDASM(activecpu,buffer,pc);
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns a flags (state, condition codes) string for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_flags(void)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_FLAGS);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns a specific register string for the currently active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_dump_reg(int regnum)
/*TODO*///{
/*TODO*///	if( activecpu >= 0 )
/*TODO*///		return CPUINFO(activecpu,NULL,CPU_INFO_REG+regnum);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns a state dump for the currently active CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpu_dump_state(void)
/*TODO*///{
/*TODO*///	static char buffer[1024+1];
/*TODO*///	unsigned addr_width = (cpu_address_bits() + 3) / 4;
/*TODO*///	char *dst = buffer;
/*TODO*///	const char *src;
/*TODO*///	const INT8 *regs;
/*TODO*///	int width;
/*TODO*///
/*TODO*///	dst += sprintf(dst, "CPU #%d [%s]\n", activecpu, cputype_name(CPU_TYPE(activecpu)));
/*TODO*///	width = 0;
/*TODO*///	regs = (INT8 *)cpu_reg_layout();
/*TODO*///	while( *regs )
/*TODO*///	{
/*TODO*///		if( *regs == -1 )
/*TODO*///		{
/*TODO*///			dst += sprintf(dst, "\n");
/*TODO*///			width = 0;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			src = cpu_dump_reg( *regs );
/*TODO*///			if( *src )
/*TODO*///			{
/*TODO*///				if( width + strlen(src) + 1 >= 80 )
/*TODO*///				{
/*TODO*///					dst += sprintf(dst, "\n");
/*TODO*///					width = 0;
/*TODO*///				}
/*TODO*///				dst += sprintf(dst, "%s ", src);
/*TODO*///				width += strlen(src) + 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		regs++;
/*TODO*///	}
/*TODO*///	dst += sprintf(dst, "\n%0*X: ", addr_width, cpu_get_pc());
/*TODO*///	cpu_dasm( dst, cpu_get_pc() );
/*TODO*///	strcat(dst, "\n\n");
/*TODO*///
/*TODO*///	return buffer;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the number of address bits for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///unsigned cputype_address_bits(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return cpuintf[cpu_type].address_bits;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the address bit mask for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///unsigned cputype_address_mask(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return (1 << cpuintf[cpu_type].address_bits) - 1;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the address shift factor for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///int cputype_address_shift(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return cpuintf[cpu_type].address_shift;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the endianess for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///unsigned cputype_endianess(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return cpuintf[cpu_type].endianess;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the code align unit for a speciific CPU type (1 byte, 2 word, ...)
/*TODO*///***************************************************************************/
/*TODO*///unsigned cputype_align_unit(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return cpuintf[cpu_type].align_unit;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the max. instruction length for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///unsigned cputype_max_inst_len(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return cpuintf[cpu_type].max_inst_len;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the name for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_name(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_NAME);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the family name for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_core_family(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_FAMILY);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the version number for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_core_version(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_VERSION);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the core filename for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_core_file(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_FILE);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the credits for a specific CPU type
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_core_credits(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_CREDITS);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the register layout for a specific CPU type (debugger)
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_reg_layout(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_REG_LAYOUT);
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the window layout for a specific CPU type (debugger)
/*TODO*///***************************************************************************/
/*TODO*///const char *cputype_win_layout(int cpu_type)
/*TODO*///{
/*TODO*///	cpu_type &= ~CPU_FLAGS_MASK;
/*TODO*///	if( cpu_type < CPU_COUNT )
/*TODO*///		return IFC_INFO(cpu_type,NULL,CPU_INFO_WIN_LAYOUT);
/*TODO*///
/*TODO*///	/* just in case... */
/*TODO*///	return (const char *)default_win_layout;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the number of address bits for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_address_bits(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_address_bits(CPU_TYPE(cpunum));
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the address bit mask for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_address_mask(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_address_mask(CPU_TYPE(cpunum));
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the endianess for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_endianess(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_endianess(CPU_TYPE(cpunum));
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the code align unit for the active CPU (1 byte, 2 word, ...)
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_align_unit(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_align_unit(CPU_TYPE(cpunum));
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the max. instruction length for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_max_inst_len(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_max_inst_len(CPU_TYPE(cpunum));
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the name for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_name(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_name(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the family name for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_core_family(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_core_family(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the core version for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_core_version(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_core_version(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the core filename for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_core_file(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_core_file(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns the credits for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_core_credits(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_core_credits(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns (debugger) register layout for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_reg_layout(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_reg_layout(CPU_TYPE(cpunum));
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Returns (debugger) window layout for a specific CPU number
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_win_layout(int cpunum)
/*TODO*///{
/*TODO*///	if( cpunum < totalcpu )
/*TODO*///		return cputype_win_layout(CPU_TYPE(cpunum));
/*TODO*///	return (const char *)default_win_layout;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Return a register value for a specific CPU number of the running machine
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_get_reg(int cpunum, int regnum)
/*TODO*///{
/*TODO*///	int oldactive;
/*TODO*///	unsigned val = 0;
/*TODO*///
/*TODO*///	if( cpunum == activecpu )
/*TODO*///		return cpu_get_reg( regnum );
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	val = GETREG(activecpu,regnum);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	return val;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Set a register value for a specific CPU number of the running machine
/*TODO*///***************************************************************************/
/*TODO*///void cpunum_set_reg(int cpunum, int regnum, unsigned val)
/*TODO*///{
/*TODO*///	int oldactive;
/*TODO*///
/*TODO*///	if( cpunum == activecpu )
/*TODO*///	{
/*TODO*///		cpu_set_reg( regnum, val );
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	SETREG(activecpu,regnum,val);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Return a dissassembled instruction for a specific CPU
/*TODO*///***************************************************************************/
/*TODO*///unsigned cpunum_dasm(int cpunum,char *buffer,unsigned pc)
/*TODO*///{
/*TODO*///	unsigned result;
/*TODO*///	int oldactive;
/*TODO*///
/*TODO*///	if( cpunum == activecpu )
/*TODO*///		return cpu_dasm(buffer,pc);
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	result = CPUDASM(activecpu,buffer,pc);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Return a flags (state, condition codes) string for a specific CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_flags(int cpunum)
/*TODO*///{
/*TODO*///	const char *result;
/*TODO*///	int oldactive;
/*TODO*///
/*TODO*///	if( cpunum == activecpu )
/*TODO*///		return cpu_flags();
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	result = CPUINFO(activecpu,NULL,CPU_INFO_FLAGS);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Return a specific register string for a specific CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_dump_reg(int cpunum, int regnum)
/*TODO*///{
/*TODO*///	const char *result;
/*TODO*///	int oldactive;
/*TODO*///
/*TODO*///	if( cpunum == activecpu )
/*TODO*///		return cpu_dump_reg(regnum);
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	result = CPUINFO(activecpu,NULL,CPU_INFO_REG+regnum);
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	return result;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Return a state dump for a specific CPU
/*TODO*///***************************************************************************/
/*TODO*///const char *cpunum_dump_state(int cpunum)
/*TODO*///{
/*TODO*///	static char buffer[1024+1];
/*TODO*///	int oldactive;
/*TODO*///
/*TODO*///	/* swap to the CPU's context */
/*TODO*///	oldactive = activecpu;
/*TODO*///	activecpu = cpunum;
/*TODO*///	memorycontextswap(activecpu);
/*TODO*///	if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///
/*TODO*///	strcpy( buffer, cpu_dump_state() );
/*TODO*///
/*TODO*///	/* update the CPU's context */
/*TODO*///	if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	activecpu = oldactive;
/*TODO*///	if (activecpu >= 0) memorycontextswap(activecpu);
/*TODO*///
/*TODO*///	return buffer;
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///  Dump all CPU's state to stdout
/*TODO*///***************************************************************************/
/*TODO*///void cpu_dump_states(void)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	for( i = 0; i < totalcpu; i++ )
/*TODO*///	{
/*TODO*///		puts( cpunum_dump_state(i) );
/*TODO*///	}
/*TODO*///	fflush(stdout);
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Dummy interfaces for non-CPUs
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///static void Dummy_reset(void *param) { }
/*TODO*///static void Dummy_exit(void) { }
/*TODO*///static int Dummy_execute(int cycles) { return cycles; }
/*TODO*///static void Dummy_burn(int cycles) { }
/*TODO*///static unsigned Dummy_get_context(void *regs) { return 0; }
/*TODO*///static void Dummy_set_context(void *regs) { }
/*TODO*///static unsigned Dummy_get_pc(void) { return 0; }
/*TODO*///static void Dummy_set_pc(unsigned val) { }
/*TODO*///static unsigned Dummy_get_sp(void) { return 0; }
/*TODO*///static void Dummy_set_sp(unsigned val) { }
/*TODO*///static unsigned Dummy_get_reg(int regnum) { return 0; }
/*TODO*///static void Dummy_set_reg(int regnum, unsigned val) { }
/*TODO*///static void Dummy_set_nmi_line(int state) { }
/*TODO*///static void Dummy_set_irq_line(int irqline, int state) { }
/*TODO*///static void Dummy_set_irq_callback(int (*callback)(int irqline)) { }
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * Return a formatted string for a register
/*TODO*/// ****************************************************************************/
/*TODO*///static const char *Dummy_info(void *context, int regnum)
/*TODO*///{
/*TODO*///	if( !context && regnum )
/*TODO*///		return "";
/*TODO*///
/*TODO*///	switch (regnum)
/*TODO*///	{
/*TODO*///		case CPU_INFO_NAME: return "Dummy";
/*TODO*///		case CPU_INFO_FAMILY: return "no CPU";
/*TODO*///		case CPU_INFO_VERSION: return "0.0";
/*TODO*///		case CPU_INFO_FILE: return __FILE__;
/*TODO*///		case CPU_INFO_CREDITS: return "The MAME team.";
/*TODO*///	}
/*TODO*///	return "";
/*TODO*///}
/*TODO*///
/*TODO*///static unsigned Dummy_dasm(char *buffer, unsigned pc)
/*TODO*///{
/*TODO*///	strcpy(buffer, "???");
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///    
}
