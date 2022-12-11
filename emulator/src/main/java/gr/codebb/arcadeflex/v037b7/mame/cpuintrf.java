/**
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.mame;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import arcadeflex.v036.cpu.dummyCPU;
import gr.codebb.arcadeflex.v036.cpu.hd6309.hd6309;
import arcadeflex.v036.cpu.z80.z80;
import gr.codebb.arcadeflex.v036.cpu.i8039.i8039;
import gr.codebb.arcadeflex.v036.cpu.i8039.i8035;
import gr.codebb.arcadeflex.v036.cpu.i8039.i8048;
import gr.codebb.arcadeflex.v036.cpu.i8039.n7751;
import gr.codebb.arcadeflex.v036.cpu.m6809.m6809;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import gr.codebb.arcadeflex.v037b7.cpu.m6502.m6502;
import gr.codebb.arcadeflex.v036.cpu.h6280.h6280;
import static gr.codebb.arcadeflex.v036.cpu.h6280.h6280H.*;
import java.util.ArrayList;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static arcadeflex.v036.mame.inptport.*;
import gr.codebb.arcadeflex.v036.cpu.konami.konami;
import static gr.codebb.arcadeflex.v036.cpu.konami.konamiH.*;
import gr.codebb.arcadeflex.v036.cpu.m68000.m68kmame;
import gr.codebb.arcadeflex.v036.cpu.m68000.m68010mame;
import gr.codebb.arcadeflex.v036.cpu.m6800.m6800;
import gr.codebb.arcadeflex.v036.cpu.m6800.m6801;
import gr.codebb.arcadeflex.v036.cpu.m6800.m6802;
import gr.codebb.arcadeflex.v036.cpu.m6800.m6803;
import gr.codebb.arcadeflex.v036.cpu.m6800.m6808;
import gr.codebb.arcadeflex.v036.cpu.m6800.hd63701;
import gr.codebb.arcadeflex.v036.cpu.tms32010.tms32010;
import static gr.codebb.arcadeflex.v036.cpu.tms32010.tms32010H.*;
import static gr.codebb.arcadeflex.v036.cpu.m68000.m68000H.*;
import gr.codebb.arcadeflex.v037b7.cpu.m6805.m6805;
import gr.codebb.arcadeflex.v037b7.cpu.m6805.m68705;
import gr.codebb.arcadeflex.v036.cpu.nec.v30;
import gr.codebb.arcadeflex.v036.cpu.nec.v33;
import gr.codebb.arcadeflex.v037b7.cpu.m6502.n2a03;
import gr.codebb.arcadeflex.v037b7.cpu.m6805.HD63705;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memorycontextswap;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import gr.codebb.arcadeflex.v037b7.cpu.i8085.i8080;
import gr.codebb.arcadeflex.v037b7.cpu.i8085.i8085;
import arcadeflex.v036.cpu.s2650.s2650;
import static arcadeflex.v036.mame.cpuintrf.cpu_timedintcallback;
import static arcadeflex.v036.mame.cpuintrf.cpu_vblankintcallback;

public class cpuintrf {

    /* these are triggers sent to the timer system for various interrupt events */
    public static final int TRIGGER_TIMESLICE = -1000;
    public static final int TRIGGER_INT = -2000;
    public static final int TRIGGER_YIELDTIME = -3000;
    public static final int TRIGGER_SUSPENDTIME = -4000;

    public static class cpuinfo {

        public cpuinfo(cpu_interface intf) {
            this.intf = intf;
        }

        public cpu_interface intf;/* pointer to the interface functions */
        public int iloops;/* number of interrupts remaining this frame */
        public int totalcycles;/* total CPU cycles executed */
        public int vblankint_countdown;/* number of vblank callbacks left until we interrupt */
        public int vblankint_multiplier;/* number of vblank callbacks per interrupt */
        public Object vblankint_timer;/* reference to elapsed time counter */
        public double vblankint_period;/* timing period of the VBLANK interrupt */
        public Object timedint_timer;/* reference to this CPU's timer */
        public double timedint_period;/* timing period of the timed interrupt */
        public Object context;/* dynamically allocated context buffer */
        public int save_context;/* need to context switch this CPU? yes or no */
        public int[] filler;//UINT8 filler[CPUINFO_ALIGN];	/* make the array aligned to next power of 2 */
    }

    public static ArrayList<cpuinfo> cpu = new ArrayList<cpuinfo>();

    public static int activecpu, totalcpu;
    static int cycles_running;/* number of cycles that the CPU emulation was requested to run (needed by cpu_getfcount) */

    static int have_to_reset;

    public static int[] interrupt_enable = new int[MAX_CPU];
    public static int[] interrupt_vector = new int[MAX_CPU];

    static int[] irq_line_state = new int[MAX_CPU * MAX_IRQ_LINES];
    static int[] irq_line_vector = new int[MAX_CPU * MAX_IRQ_LINES];

    static int watchdog_counter;

    static Object vblank_timer;
    static int vblank_countdown;
    static int vblank_multiplier;
    static double vblank_period;

    static Object refresh_timer;
    static double refresh_period;
    static double refresh_period_inv;

    static Object timeslice_timer;
    static double timeslice_period;

    static double scanline_period;
    static double scanline_period_inv;

    static int usres;
    /* removed from cpu_run and made global */
    static int vblank;
    static int current_frame;

    /* and a list of driver interception hooks */
    public static final IrqCallbackHandlerPtr[] drv_irq_callbacks = {
        null, null, null, null, null, null, null, null
    };

    /* Convenience macros - not in cpuintrf.h because they shouldn't be used by everyone */
    static void RESET(int index) {
        cpu.get(index).intf.reset(Machine.drv.cpu[index].reset_param);
    }

    static int EXECUTE(int index, int cycles) {
        return cpu.get(index).intf.execute(cycles);
    }

    static Object GETCONTEXT(int index) {
        return cpu.get(index).intf.get_context();
    }

    static void SETCONTEXT(int index, Object context) {
        cpu.get(index).intf.set_context(context);
    }

    /*TODO*///#define GETCYCLETBL(index,which)		((*cpu[index].intf->get_cycle_table)(which))
/*TODO*///#define SETCYCLETBL(index,which,cnts)	((*cpu[index].intf->set_cycle_table)(which,cnts))
    static int GETPC(int index) {
        return cpu.get(index).intf.get_pc();
    }

    /*TODO*///#define SETPC(index,val)				((*cpu[index].intf->set_pc)(val))
/*TODO*///#define GETSP(index)					((*cpu[index].intf->get_sp)())
/*TODO*///#define SETSP(index,val)				((*cpu[index].intf->set_sp)(val))
    static int GETREG(int index, int regnum) {
        return cpu.get(index).intf.get_reg(regnum);
    }

    static void SETREG(int index, int regnum, int value) {
        cpu.get(index).intf.set_reg(regnum, value);
    }

    static void SETNMILINE(int index, int state) {
        cpu.get(index).intf.set_nmi_line(state);
    }

    static void SETIRQLINE(int index, int line, int state) {
        cpu.get(index).intf.set_irq_line(line, state);
    }

    static void SETIRQCALLBACK(int index, IrqCallbackHandlerPtr callback) {
        cpu.get(index).intf.set_irq_callback(callback);
    }

    /*TODO*///#define INTERNAL_INTERRUPT(index,type)	if( cpu[index].intf->internal_interrupt ) ((*cpu[index].intf->internal_interrupt)(type))
/*TODO*///#define CPUINFO(index,context,regnum)	((*cpu[index].intf->cpu_info)(context,regnum))
/*TODO*///#define CPUDASM(index,buffer,pc)		((*cpu[index].intf->cpu_dasm)(buffer,pc))
    static int ICOUNT(int index) {
        return cpu.get(index).intf.icount[0];
    }

    public static int INT_TYPE_NONE(int index) {
        return cpu.get(index).intf.no_int;
    }

    public static int INT_TYPE_IRQ(int index) {
        return cpu.get(index).intf.irq_int;
    }

    public static int INT_TYPE_NMI(int index) {
        return cpu.get(index).intf.nmi_int;
    }

    /*TODO*///#define READMEM(index,offset)			((*cpu[index].intf->memory_read)(offset))
/*TODO*///#define WRITEMEM(index,offset,data) 	((*cpu[index].intf->memory_write)(offset,data))
    static void SET_OP_BASE(int index, int pc) {
        cpu.get(index).intf.set_op_base(pc);
    }

    public static int CPU_TYPE(int index) {
        return Machine.drv.cpu[index].cpu_type & ~CPU_FLAGS_MASK;
    }

    public static int CPU_AUDIO(int index) {
        return Machine.drv.cpu[index].cpu_type & CPU_AUDIO_CPU;
    }

    public static String IFC_INFO(int cpu, Object context, int regnum) {
        return cpuintf[cpu].cpu_info(context, regnum);
    }

    /*TODO*////* most CPUs use this macro */
/*TODO*///#define CPU0(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM) \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute, NULL,						   \
/*TODO*///		name##_get_context, name##_set_context, NULL, NULL, 					   \
/*TODO*///		name##_get_pc, name##_set_pc,											   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,NULL,NULL, name##_info, name##_dasm,								   \
/*TODO*///		nirq, dirq, &name##_ICount, oc, i0, i1, i2, 							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, NULL, NULL,						   \
/*TODO*///		0, cpu_setOPbase##mem,													   \
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
/*TODO*///		name##_get_context, name##_set_context, 								   \
/*TODO*///		name##_get_cycle_table, name##_set_cycle_table, 						   \
/*TODO*///		name##_get_pc, name##_set_pc,											   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,name##_state_save,name##_state_load, name##_info, name##_dasm, 	   \
/*TODO*///		nirq, dirq, &name##_ICount, oc, i0, i1, i2, 							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, NULL, NULL,						   \
/*TODO*///		0, cpu_setOPbase##mem,													   \
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
/*TODO*///		name##_get_context, name##_set_context, NULL, NULL, 					   \
/*TODO*///		name##_get_pc, name##_set_pc,											   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		name##_internal_interrupt,NULL,NULL, name##_info, name##_dasm,			   \
/*TODO*///		nirq, dirq, &name##_ICount, oc, i0, i1, i2, 							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, NULL, NULL,						   \
/*TODO*///		0, cpu_setOPbase##mem,													   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}																			   \
/*TODO*///
/*TODO*////* like CPU0, but CPU has Harvard-architecture like program/data memory */
/*TODO*///#define CPU3(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM) \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute, NULL,						   \
/*TODO*///		name##_get_context, name##_set_context, NULL, NULL, 					   \
/*TODO*///		name##_get_pc, name##_set_pc,											   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,NULL,NULL, name##_info, name##_dasm,								   \
/*TODO*///		nirq, dirq, &name##_icount, oc, i0, i1, i2, 							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, NULL, NULL,						   \
/*TODO*///		cpu##_PGM_OFFSET, cpu_setOPbase##mem,									   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}
/*TODO*///
/*TODO*////* like CPU0, but CPU has internal memory (or I/O ports, timers or similiar) */
/*TODO*///#define CPU4(cpu,name,nirq,dirq,oc,i0,i1,i2,mem,shift,bits,endian,align,maxinst,MEM) \
/*TODO*///	{																			   \
/*TODO*///		CPU_##cpu,																   \
/*TODO*///		name##_reset, name##_exit, name##_execute, NULL,						   \
/*TODO*///		name##_get_context, name##_set_context, NULL, NULL, 					   \
/*TODO*///		name##_get_pc, name##_set_pc,											   \
/*TODO*///		name##_get_sp, name##_set_sp, name##_get_reg, name##_set_reg,			   \
/*TODO*///		name##_set_nmi_line, name##_set_irq_line, name##_set_irq_callback,		   \
/*TODO*///		NULL,NULL,NULL, name##_info, name##_dasm,								   \
/*TODO*///		nirq, dirq, &name##_icount, oc, i0, i1, i2, 							   \
/*TODO*///		cpu_readmem##mem, cpu_writemem##mem, name##_internal_r, name##_internal_w, \
/*TODO*///		0, cpu_setOPbase##mem,													   \
/*TODO*///		shift, bits, CPU_IS_##endian, align, maxinst,							   \
/*TODO*///		ABITS1_##MEM, ABITS2_##MEM, ABITS_MIN_##MEM 							   \
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///
/* warning the ordering must match the one of the enum in driver.h! */
    public static cpu_interface cpuintf[]
            = {
                new dummyCPU(),
                new z80(),
                new dummyCPU(),/*TODO*///	CPU0(Z80GB,    z80gb,	 5,255,1.00,Z80GB_IGNORE_INT,  0,			   1,			   16,	  0,16,LE,1, 4,16	),
                new i8080(),/*TODO*///	CPU0(8080,	   i8080,	 4,255,1.00,I8080_NONE, 	   I8080_INTR,	   I8080_TRAP,	   16,	  0,16,LE,1, 3,16	),
                new i8085(),/*TODO*///	CPU0(8085A,    i8085,	 4,255,1.00,I8085_NONE, 	   I8085_INTR,	   I8085_TRAP,	   16,	  0,16,LE,1, 3,16	),
                new m6502(),/*TODO*///	CPU0(M6502,    m6502,	 1,  0,1.00,M6502_INT_NONE,    M6502_INT_IRQ,  M6502_INT_NMI,  16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M65C02,   m65c02,	 1,  0,1.00,M65C02_INT_NONE,   M65C02_INT_IRQ, M65C02_INT_NMI, 16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M65SC02,  m65sc02,  1,  0,1.00,M65SC02_INT_NONE,  M65SC02_INT_IRQ,M65SC02_INT_NMI,16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M65CE02,  m65ce02,  1,  0,1.00,M65CE02_INT_NONE,  M65CE02_INT_IRQ,M65CE02_INT_NMI,16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M6509,    m6509,	 1,  0,1.00,M6509_INT_NONE,    M6509_INT_IRQ,  M6509_INT_NMI,  20,	  0,20,LE,1, 3,20	),
                new dummyCPU(),/*TODO*///	CPU0(M6510,    m6510,	 1,  0,1.00,M6510_INT_NONE,    M6510_INT_IRQ,  M6510_INT_NMI,  16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M6510T,   m6510t,	 1,  0,1.00,M6510T_INT_NONE,   M6510T_INT_IRQ, M6510T_INT_NMI, 16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M7501,    m7501,	 1,  0,1.00,M7501_INT_NONE,    M7501_INT_IRQ,  M7501_INT_NMI,  16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M8502,    m8502,	 1,  0,1.00,M8502_INT_NONE,    M8502_INT_IRQ,  M8502_INT_NMI,  16,	  0,16,LE,1, 3,16	),
                new n2a03(),/*TODO*///	CPU0(N2A03,    n2a03,	 1,  0,1.00,N2A03_INT_NONE,    N2A03_INT_IRQ,  N2A03_INT_NMI,  16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(M4510,    m4510,	 1,  0,1.00,M4510_INT_NONE,    M4510_INT_IRQ,  M4510_INT_NMI,  20,	  0,20,LE,1, 3,20	),
                new h6280(),/*TODO*///	CPU0(H6280,    h6280,	 3,  0,1.00,H6280_INT_NONE,    -1,			   H6280_INT_NMI,  21,	  0,21,LE,1, 3,21	),
                new dummyCPU(),/*TODO*///	CPU0(I86,	   i86, 	 1,  0,1.00,I86_INT_NONE,	   -1000,		   I86_NMI_INT,    20,	  0,20,LE,1, 5,20	),
                new dummyCPU(),/*TODO*///	CPU0(I88,	   i88, 	 1,  0,1.00,I88_INT_NONE,	   -1000,		   I88_NMI_INT,    20,	  0,20,LE,1, 5,20	),
                new dummyCPU(),/*TODO*///	CPU0(I186,	   i186,	 1,  0,1.00,I186_INT_NONE,	   -1000,		   I186_NMI_INT,   20,	  0,20,LE,1, 5,20	),
                new dummyCPU(),/*TODO*///	CPU0(I188,	   i188,	 1,  0,1.00,I188_INT_NONE,	   -1000,		   I188_NMI_INT,   20,	  0,20,LE,1, 5,20	),
                new dummyCPU(),/*TODO*///	CPU0(I286,	   i286,	 1,  0,1.00,I286_INT_NONE,	   -1000,		   I286_NMI_INT,   24,	  0,24,LE,1, 5,24	),
                new dummyCPU(),/*TODO*///	CPU0(V20,	   v20, 	 1,  0,1.00,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
                new v30(),/*TODO*///	CPU0(V30,	   v30, 	 1,  0,1.00,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
                new v33(),/*TODO*///	CPU0(V33,	   v33, 	 1,  0,1.20,NEC_INT_NONE,	   -1000,		   NEC_NMI_INT,    20,	  0,20,LE,1, 5,20	),
                new i8035(),/*TODO*///	CPU0(I8035,    i8035,	 1,  0,1.00,I8035_IGNORE_INT,  I8035_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
                new i8039(),/*TODO*///	CPU0(I8039,    i8039,	 1,  0,1.00,I8039_IGNORE_INT,  I8039_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
                new i8048(),/*TODO*///	CPU0(I8048,    i8048,	 1,  0,1.00,I8048_IGNORE_INT,  I8048_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
                new n7751(),/*TODO*///	CPU0(N7751,    n7751,	 1,  0,1.00,N7751_IGNORE_INT,  N7751_EXT_INT,  -1,			   16,	  0,16,LE,1, 2,16	),
                new m6800(),/*TODO*///	CPU0(M6800,    m6800,	 1,  0,1.00,M6800_INT_NONE,    M6800_INT_IRQ,  M6800_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new m6801(),/*TODO*///	CPU0(M6801,    m6801,	 1,  0,1.00,M6801_INT_NONE,    M6801_INT_IRQ,  M6801_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new m6802(),/*TODO*///	CPU0(M6802,    m6802,	 1,  0,1.00,M6802_INT_NONE,    M6802_INT_IRQ,  M6802_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new m6803(),/*TODO*///	CPU0(M6803,    m6803,	 1,  0,1.00,M6803_INT_NONE,    M6803_INT_IRQ,  M6803_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new m6808(),/*TODO*///	CPU0(M6808,    m6808,	 1,  0,1.00,M6808_INT_NONE,    M6808_INT_IRQ,  M6808_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new hd63701(),/*TODO*///	CPU0(HD63701,  hd63701,  1,  0,1.00,HD63701_INT_NONE,  HD63701_INT_IRQ,HD63701_INT_NMI,16,	  0,16,BE,1, 4,16	),
                new dummyCPU(),/*TODO*///	CPU0(NSC8105,  nsc8105,  1,  0,1.00,NSC8105_INT_NONE,  NSC8105_INT_IRQ,NSC8105_INT_NMI,16,	  0,16,BE,1, 4,16	),
                new m6805(),/*TODO*///	CPU0(M6805,    m6805,	 1,  0,1.00,M6805_INT_NONE,    M6805_INT_IRQ,  -1,			   16,	  0,11,BE,1, 3,16	),
                new m68705(),/*TODO*///	CPU0(M68705,   m68705,	 1,  0,1.00,M68705_INT_NONE,   M68705_INT_IRQ, -1,			   16,	  0,11,BE,1, 3,16	),
                new HD63705(),/*TODO*///	CPU0(HD63705,  hd63705,  8,  0,1.00,HD63705_INT_NONE,  HD63705_INT_IRQ,-1,			   16,	  0,16,BE,1, 3,16	),
                new hd6309(),/*TODO*///	CPU0(HD6309,   hd6309,	 2,  0,1.00,HD6309_INT_NONE,   HD6309_INT_IRQ, HD6309_INT_NMI, 16,	  0,16,BE,1, 4,16	),
                new m6809(),/*TODO*///	CPU0(M6809,    m6809,	 2,  0,1.00,M6809_INT_NONE,    M6809_INT_IRQ,  M6809_INT_NMI,  16,	  0,16,BE,1, 4,16	),
                new konami(),/*TODO*///	CPU0(KONAMI,   konami,	 2,  0,1.00,KONAMI_INT_NONE,   KONAMI_INT_IRQ, KONAMI_INT_NMI, 16,	  0,16,BE,1, 4,16	),
                new m68kmame(),/*TODO*///	CPU0(M68000,   m68000,	 8, -1,1.00,MC68000_INT_NONE,  -1,			   -1,			   24bew, 0,24,BE,2,10,24BEW),
                new m68010mame(),/*TODO*///	CPU0(M68010,   m68010,	 8, -1,1.00,MC68010_INT_NONE,  -1,			   -1,			   24bew, 0,24,BE,2,10,24BEW),
                new dummyCPU(),/*TODO*///	CPU0(M68EC020, m68ec020, 8, -1,1.00,MC68EC020_INT_NONE,-1,			   -1,			   24bew, 0,24,BE,2,10,24BEW),
                new dummyCPU(),/*TODO*///	CPU0(M68020,   m68020,	 8, -1,1.00,MC68020_INT_NONE,  -1,			   -1,			   24bew, 0,24,BE,2,10,24BEW),
                new dummyCPU(),/*TODO*///	CPU0(T11,	   t11, 	 4,  0,1.00,T11_INT_NONE,	   -1,			   -1,			   16lew, 0,16,LE,2, 6,16LEW),
                new s2650(),/*TODO*///	CPU0(S2650,    s2650,	 2,  0,1.00,S2650_INT_NONE,    -1,			   -1,			   16,	  0,15,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU4(F8,	   f8,		 1,  0,1.00,F8_INT_NONE,	   F8_INT_INTR,    -1,			   16,	  0,16,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU2(TMS34010, tms34010, 2,  0,1.00,TMS34010_INT_NONE, TMS34010_INT1,  -1,			   29,	  3,29,LE,2,10,29	),
                new dummyCPU(),/*TODO*///	CPU0(TMS9900,  tms9900,  1,  0,1.00,TMS9900_NONE,	   -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
                new dummyCPU(),/*TODO*///	CPU0(TMS9940,  tms9940,  1,  0,1.00,TMS9940_NONE,	   -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
                new dummyCPU(),/*TODO*///	CPU0(TMS9980,  tms9980a, 1,  0,1.00,TMS9980A_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
                new dummyCPU(),/*TODO*///	CPU0(TMS9985,  tms9985,  1,  0,1.00,TMS9985_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
                new dummyCPU(),/*TODO*///	CPU0(TMS9989,  tms9989,  1,  0,1.00,TMS9989_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
                new dummyCPU(),/*TODO*///	CPU0(TMS9995,  tms9995,  1,  0,1.00,TMS9995_NONE,	   -1,			   -1,			   16,	  0,16,BE,1, 6,16	),
                new dummyCPU(),/*TODO*///	CPU0(TMS99105A,tms99105a,1,  0,1.00,TMS99105A_NONE,    -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
                new dummyCPU(),/*TODO*///	CPU0(TMS99110A,tms99110a,1,  0,1.00,TMS99110A_NONE,    -1,			   -1,			   16bew, 0,16,BE,2, 6,16BEW),
                new dummyCPU(),/*TODO*///	CPU0(Z8000,    z8000,	 2,  0,1.00,Z8000_INT_NONE,    Z8000_NVI,	   Z8000_NMI,	   16bew, 0,16,BE,2, 6,16BEW),
                new tms32010(),/*TODO*///	CPU3(TMS320C10,tms320c10,2,  0,1.00,TMS320C10_INT_NONE,-1,			   -1,			   16,	 -1,16,BE,2, 4,16	),
                new dummyCPU(),/*TODO*///	CPU3(CCPU,	   ccpu,	 2,  0,1.00,0,				   -1,			   -1,			   16,	  0,15,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU0(PDP1,	   pdp1,	 0,  0,1.00,0,				   -1,			   -1,			   16,	  0,18,LE,1, 3,16	),
                new dummyCPU(),/*TODO*///	CPU3(ADSP2100, adsp2100, 4,  0,1.00,ADSP2100_INT_NONE, -1,			   -1,			   16lew,-1,14,LE,2, 4,16LEW),
                new dummyCPU(),/*TODO*///	CPU3(ADSP2105, adsp2105, 4,  0,1.00,ADSP2105_INT_NONE, -1,			   -1,			   16lew,-1,14,LE,2, 4,16LEW),
                new dummyCPU(),/*TODO*///	CPU0(PSX,	   mips,	 8, -1,1.00,MIPS_INT_NONE,	   MIPS_INT_NONE,  MIPS_INT_NONE,  32lew, 0,32,LE,4, 4,32LEW),
                new dummyCPU(),/*TODO*///	CPU0(SC61860,  sc61860,  1,  0,1.00,-1, 			   -1,			   -1,			   16,	  0,16,BE,1, 4,16	),
                new dummyCPU(),/*TODO*///	CPU0(ARM,	   arm, 	 2,  0,1.00,ARM_INT_NONE,	   ARM_FIRQ,	   ARM_IRQ, 	   26lew, 0,26,LE,4, 4,26LEW),
                new dummyCPU(),/*TODO*///	CPU0(G65C816,  g65816,	 1,  0,1.00,G65816_INT_NONE,   G65816_INT_IRQ, G65816_INT_NMI, 24,	  0,24,BE,1, 3,24	),
                new dummyCPU()/*TODO*///	CPU0(SPC700,   spc700,	 0,  0,1.00,0,				   -1,			   -1,			   16,	  0,16,LE,1, 3,16	),
            };

    public static void cpu_init() {
        int i;
        /*TODO*///	/* Verify the order of entries in the cpuintf[] array */
/*TODO*///	for( i = 0; i < CPU_COUNT; i++ )
/*TODO*///	{
/*TODO*///		if( cpuintf[i].cpu_num != i )
/*TODO*///		{
/*TODO*///logerror("CPU #%d [%s] wrong ID %d: check enum CPU_... in src/driver.h!\n", i, cputype_name(i), cpuintf[i].cpu_num);
/*TODO*///			exit(1);
/*TODO*///		}
/*TODO*///	}
        /* count how many CPUs we have to emulate */
        totalcpu = 0;

        while (totalcpu < MAX_CPU) {
            if (CPU_TYPE(totalcpu) == CPU_DUMMY) {
                break;
            }
            totalcpu++;
        }
        /* zap the CPU data structure */
        cpu.clear();
        /* Set up the interface functions */
        for (i = 0; i < MAX_CPU; i++) {
            cpu.add(new cpuinfo(cpuintf[CPU_TYPE(i)]));
        }
        /* reset the timer system */
        timer_init();
        timeslice_timer = refresh_timer = vblank_timer = null;
    }

    public static void cpu_run() {
        /* determine which CPUs need a context switch */
        for (int i = 0; i < totalcpu; i++) {
            cpu.get(i).context = cpu.get(i).intf.init_context();
            /* Save if there is another CPU of the same type */
            cpu.get(i).save_context = 0;

            for (int j = 0; j < totalcpu; j++) {
                if (i != j && cpunum_core_file(i).compareTo(cpunum_core_file(j)) == 0) {
                    cpu.get(i).save_context = 1;
                }
            }

            for (int j = 0; j < MAX_IRQ_LINES; j++) {
                irq_line_state[i * MAX_IRQ_LINES + j] = CLEAR_LINE;
                irq_line_vector[i * MAX_IRQ_LINES + j] = cpuintf[CPU_TYPE(i)].default_vector;
            }
        }
        reset:
        for (;;) {
            /* read hi scores information from hiscore.dat */
 /*TODO*///            hs_open(Machine.gamedrv.name);
/*TODO*///            hs_init();

            /* initialize the various timers (suspends all CPUs at startup) */
            cpu_inittimers();
            watchdog_counter = -1;

            /* reset sound_old chips */
            sound_reset();
            /* enable all CPUs (except for audio CPUs if the sound_old is off) */
            for (int i = 0; i < totalcpu; i++) {
                if (CPU_AUDIO(i) == 0 || Machine.sample_rate != 0) {
                    timer_suspendcpu(i, 0, SUSPEND_REASON_RESET);
                } else {
                    timer_suspendcpu(i, 1, SUSPEND_REASON_DISABLE);
                }
            }
            have_to_reset = 0;
            vblank = 0;

            logerror("Machine reset\n");
            /* start with interrupts enabled, so the generic routine will work even if */
 /* the machine doesn't have an interrupt enable port */
            for (int i = 0; i < MAX_CPU; i++) {
                interrupt_enable[i] = 1;
                interrupt_vector[i] = 0xff;
                /* Reset any driver hooks into the IRQ acknowledge callbacks */
                drv_irq_callbacks[i] = null;
            }

            /* do this AFTER the above so init_machine() can use cpu_halt() to hold the */
 /* execution of some CPUs, or disable interrupts */
            if (Machine.drv.init_machine != null) {
                Machine.drv.init_machine.handler();
            }
            /* reset each CPU */
            for (int i = 0; i < totalcpu; i++) {
                /* swap memory contexts and reset */
                memorycontextswap(i);
                if (cpu.get(i).save_context != 0) {
                    SETCONTEXT(i, cpu.get(i).context);
                }
                activecpu = i;
                RESET(i);

                /* Set the irq callback for the cpu_old */
                SETIRQCALLBACK(i, cpu_irq_callbacks[i]);

                /* save the CPU context if necessary */
                if (cpu.get(i).save_context != 0) {
                    cpu.get(i).context = GETCONTEXT(i);
                }

                /* reset the total number of cycles */
                cpu.get(i).totalcycles = 0;
            }
            /* reset the globals */
            cpu_vblankreset();
            current_frame = 0;
            /* loop until the user quits */
            usres = 0;
            int cpunum_table[] = new int[1];
            int cycles_table[] = new int[1];
            while (usres == 0) {
                int cpunum;

                /* was machine_reset() called? */
                if (have_to_reset != 0) {
                    /*TODO*///#ifdef MESS
/*TODO*///			if (Machine->drv->stop_machine) (*Machine->drv->stop_machine)();
/*TODO*///#endif
                    continue reset;
                }
                /* ask the timer system to schedule */
                if (timer_schedule_cpu(cpunum_table, cycles_table) != 0) {
                    cpunum = cpunum_table[0];
                    cycles_running = cycles_table[0];
                    int ran = 1;

                    /* switch memory and CPU contexts */
                    activecpu = cpunum;
                    memorycontextswap(activecpu);

                    if (cpu.get(activecpu).save_context != 0) {
                        SETCONTEXT(activecpu, cpu.get(activecpu).context);
                    }

                    /* make sure any bank switching is reset */
                    SET_OP_BASE(activecpu, GETPC(activecpu));

                    /* run for the requested number of cycles */
                    ran = EXECUTE(activecpu, cycles_running);

                    /* update based on how many cycles we really ran */
                    cpu.get(activecpu).totalcycles += ran;

                    /* update the contexts */
                    if (cpu.get(activecpu).save_context != 0) {
                        cpu.get(activecpu).context = GETCONTEXT(activecpu);

                    }
                    activecpu = -1;

                    /* update the timer with how long we actually ran */
                    timer_update_cpu(cpunum, ran);
                }

            }
            /* write hi scores to disk - No scores saving if cheat */
 /*TODO*///            hs_close();
/*TODO*///#ifdef MESS
/*TODO*///	if (Machine->drv->stop_machine) (*Machine->drv->stop_machine)();
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
            totalcpu = 0;
            break;
        }
    }
    /**
     * *************************************************************************
     * Use this function to initialize, and later maintain, the watchdog. For
     * convenience, when the machine_old is reset, the watchdog is disabled. If
     * you call this function, the watchdog is initialized, and from that point
     * onwards, if you don't call it at least once every 2 seconds, the
     * machine_old will be reset.
     * <p>
     * The 2 seconds delay is targeted at dondokod, which during boot stays more
     * than 1 second without resetting the watchdog.
     * *************************************************************************
     */
    public static WriteHandlerPtr watchdog_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (watchdog_counter == -1) {
                logerror("watchdog armed\n");
            }
            watchdog_counter = (int) (2 * Machine.drv.frames_per_second);
        }
    };

    public static ReadHandlerPtr watchdog_reset_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (watchdog_counter == -1) {
                logerror("watchdog armed\n");
            }
            watchdog_counter = (int) (2 * Machine.drv.frames_per_second);
            return 0;
        }
    };

    /**
     * *************************************************************************
     * This function resets the machine_old (the reset will not take place
     * immediately, it will be performed at the end of the active CPU's time
     * slice)
     * *************************************************************************
     */
    public static void machine_reset() {
        /* write hi scores to disk - No scores saving if cheat */
 /*TODO*///        hs_close();

        have_to_reset = 1;
    }



    /**
     * *************************************************************************
     * Use this function to control the HALT line on a CPU
     * *************************************************************************
     */
    public static void cpu_set_halt_line(int cpunum, int state) {
        timer_set(TIME_NOW, (cpunum & 7) | (state << 3), cpu_haltcallback);
    }

    /*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Use this function to install a callback for IRQ acknowledge
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///void cpu_set_irq_callback(int cpunum, int (*callback)(int))
/*TODO*///{
/*TODO*///	drv_irq_callbacks[cpunum] = callback;
/*TODO*///}
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     * This function returns CPUNUM current status (running or halted)
     * *************************************************************************
     */
    public static int cpu_getstatus(int cpunum) {
        if (cpunum >= MAX_CPU) {
            return 0;
        }

        return timer_iscpususpended(cpunum,
                SUSPEND_REASON_HALT | SUSPEND_REASON_RESET | SUSPEND_REASON_DISABLE) == 0 ? 1 : 0;
    }

    public static int cpu_getactivecpu() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return cpunum;
    }

    public static void cpu_setactivecpu(int cpunum) {
        activecpu = cpunum;
    }

    public static int cpu_gettotalcpu() {
        return totalcpu;
    }

    public static int /*unsigned*/ cpu_get_pc() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        return GETPC(cpunum);
    }

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

    public static void cpu_set_op_base(int/*unsigned*/ val) {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        SET_OP_BASE(cpunum, val);
    }

    /**
     * *************************************************************************
     * Returns the number of CPU cycles since the last reset of the CPU
     * <p>
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

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Returns the number of CPU cycles in one video frame
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///int cpu_getfperiod(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return TIME_TO_CYCLES(cpunum, refresh_period);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     * Scales a given value by the ratio of fcount / fperiod
     * *************************************************************************
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
     * Returns the current scanline, or the time until a specific scanline
     * <p>
     * Note: cpu_getscanline() counts from 0, 0 being the first visible line.
     * You might have to adjust this value to match the hardware, since in many
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

    /*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Returns the number of cycles in a scanline
/*TODO*///
/*TODO*/// ***************************************************************************/
/*TODO*///int cpu_getscanlinecycles(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return TIME_TO_CYCLES(cpunum, scanline_period);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Returns the number of cycles since the beginning of this frame
/*TODO*///
/*TODO*/// ***************************************************************************/
/*TODO*///int cpu_getcurrentcycles(void)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return TIME_TO_CYCLES(cpunum, timer_timeelapsed(refresh_timer));
/*TODO*///}
/*TODO*///
    /**
     * *************************************************************************
     * Returns the current horizontal beam position in pixels
     * *************************************************************************
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

    /**
     * *************************************************************************
     *
     * Interrupt handling
     *
     **************************************************************************
     */
    /**
     * *************************************************************************
     * These functions are called when a cpu_old calls the callback sent to it's
     * set_irq_callback function. It clears the irq line if the current state is
     * HOLD_LINE and returns the interrupt vector for that line.
     * *************************************************************************
     */
    public static IrqCallbackHandlerPtr cpu_0_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[0 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[0 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(0, irqline, CLEAR_LINE);
                irq_line_state[0 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            logerror("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector);
            if (drv_irq_callbacks[0] != null) {
                return (drv_irq_callbacks[0]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_1_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[1 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[1 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(1, irqline, CLEAR_LINE);
                irq_line_state[1 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            logerror("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector);
            if (drv_irq_callbacks[1] != null) {
                return (drv_irq_callbacks[1]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_2_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[2 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[2 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(2, irqline, CLEAR_LINE);
                irq_line_state[2 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            logerror("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector);
            if (drv_irq_callbacks[2] != null) {
                return (drv_irq_callbacks[2]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_3_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[3 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[3 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(3, irqline, CLEAR_LINE);
                irq_line_state[3 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            //LOG(("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector));
            if (drv_irq_callbacks[3] != null) {
                return (drv_irq_callbacks[3]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_4_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[4 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[4 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(4, irqline, CLEAR_LINE);
                irq_line_state[4 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            //LOG(("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector));
            if (drv_irq_callbacks[4] != null) {
                return (drv_irq_callbacks[4]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_5_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[5 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[5 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(5, irqline, CLEAR_LINE);
                irq_line_state[5 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            //LOG(("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector));
            if (drv_irq_callbacks[5] != null) {
                return (drv_irq_callbacks[5]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_6_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[6 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[6 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(6, irqline, CLEAR_LINE);
                irq_line_state[6 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            //LOG(("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector));
            if (drv_irq_callbacks[6] != null) {
                return (drv_irq_callbacks[6]).handler(vector);
            }
            return vector;
        }
    };
    public static IrqCallbackHandlerPtr cpu_7_irq_callback = new IrqCallbackHandlerPtr() {
        public int handler(int irqline) {
            int vector = irq_line_vector[7 * MAX_IRQ_LINES + irqline];
            if (irq_line_state[7 * MAX_IRQ_LINES + irqline] == HOLD_LINE) {
                SETIRQLINE(7, irqline, CLEAR_LINE);
                irq_line_state[7 * MAX_IRQ_LINES + irqline] = CLEAR_LINE;
            }
            //LOG(("cpu_##num##_irq_callback(%d) $%04x\n", irqline, vector));
            if (drv_irq_callbacks[7] != null) {
                return (drv_irq_callbacks[7]).handler(vector);
            }
            return vector;
        }
    };
    /* and a list of them for indexed access */
    public static final IrqCallbackHandlerPtr[] cpu_irq_callbacks = {
        cpu_0_irq_callback,
        cpu_1_irq_callback,
        cpu_2_irq_callback,
        cpu_3_irq_callback,
        cpu_4_irq_callback,
        cpu_5_irq_callback,
        cpu_6_irq_callback,
        cpu_7_irq_callback
    };

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
    /**
     * *************************************************************************
     * Use this functions to set the vector for a irq line of a CPU
     * *************************************************************************
     */
    public static void cpu_irq_line_vector_w(int cpunum, int irqline, int vector) {
        cpunum &= (MAX_CPU - 1);
        irqline &= (MAX_IRQ_LINES - 1);
        if (irqline < cpu.get(cpunum).intf.num_irqs) {
            logerror("cpu_irq_line_vector_w(%d,%d,$%04x)\n", cpunum, irqline, vector);
            irq_line_vector[cpunum * MAX_IRQ_LINES + irqline] = vector;
            return;
        }
        //LOG(("cpu_irq_line_vector_w CPU#%d irqline %d > max irq lines\n", cpunum, irqline));
    }

    public static InterruptHandlerPtr m68_level1_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_1;
        }
    };
    public static InterruptHandlerPtr m68_level2_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_2;
        }
    };
    public static InterruptHandlerPtr m68_level3_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_3;
        }
    };
    public static InterruptHandlerPtr m68_level4_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_4;
        }
    };
    public static InterruptHandlerPtr m68_level5_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_5;
        }
    };
    public static InterruptHandlerPtr m68_level6_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_6;
        }
    };
    public static InterruptHandlerPtr m68_level7_irq = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            if (interrupt_enable[cpunum] == 0) {
                return MC68000_INT_NONE;
            }
            return MC68000_IRQ_7;
        }
    };

    public static InterruptHandlerPtr ignore_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            int cpunum = (activecpu < 0) ? 0 : activecpu;
            return INT_TYPE_NONE(cpunum);
        }
    };

    /**
     * *************************************************************************
     * CPU timing and synchronization functions.
     * *************************************************************************
     */

    /* generate a trigger */
    public static TimerCallbackHandlerPtr cpu_trigger = new TimerCallbackHandlerPtr() {
        public void handler(int trigger) {
            timer_trigger(trigger);
        }
    };

    /* generate a trigger after a specific period of time */
    public static void cpu_triggertime(double duration, int trigger) {
        timer_set(duration, trigger, cpu_trigger);
    }

    /* burn CPU cycles until a timer trigger */
    public static void cpu_spinuntil_trigger(int trigger) {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        timer_suspendcpu_trigger(cpunum, trigger);
    }

    /* burn CPU cycles until the next interrupt */
    public static void cpu_spinuntil_int() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        cpu_spinuntil_trigger(TRIGGER_INT + cpunum);
    }

    /* burn CPU cycles until our timeslice is up */
    public static void cpu_spin() {
        cpu_spinuntil_trigger(TRIGGER_TIMESLICE);
    }

    static int timetrig = 0;

    /* burn CPU cycles for a specific period of time */
    public static void cpu_spinuntil_time(double duration) {

        cpu_spinuntil_trigger(TRIGGER_SUSPENDTIME + timetrig);
        cpu_triggertime(duration, TRIGGER_SUSPENDTIME + timetrig);
        timetrig = (timetrig + 1) & 255;
    }

    /* yield our timeslice for a specific period of time */
    public static void cpu_yielduntil_trigger(int trigger) {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        timer_holdcpu_trigger(cpunum, trigger);
    }

    /* yield our timeslice until the next interrupt */
    public static void cpu_yielduntil_int() {
        int cpunum = (activecpu < 0) ? 0 : activecpu;
        cpu_yielduntil_trigger(TRIGGER_INT + cpunum);
    }

    /* yield our current timeslice */
    public static void cpu_yield() {
        cpu_yielduntil_trigger(TRIGGER_TIMESLICE);
    }

    static int timetrig_yield = 0;

    /* yield our timeslice for a specific period of time */
    public static void cpu_yielduntil_time(double duration) {
        cpu_yielduntil_trigger(TRIGGER_YIELDTIME + timetrig_yield);
        cpu_triggertime(duration, TRIGGER_YIELDTIME + timetrig_yield);
        timetrig_yield = (timetrig_yield + 1) & 255;
    }

    public static int cpu_getvblank() {
        return vblank;
    }

    public static int cpu_getcurrentframe() {
        return current_frame;
    }

    /**
     * *************************************************************************
     * Internal CPU event processors.
     * *************************************************************************
     */
    public static TimerCallbackHandlerPtr cpu_manualnmicallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int cpunum, state, oldactive;
            cpunum = param & 7;
            state = param >> 3;

            /* swap to the CPU's context */
            oldactive = activecpu;
            activecpu = cpunum;
            memorycontextswap(activecpu);
            if (cpu.get(activecpu).save_context != 0) {
                SETCONTEXT(activecpu, cpu.get(activecpu).context);
            }
            logerror("cpu_manualnmicallback %d,%d\n", cpunum, state);
            switch (state) {
                case PULSE_LINE:
                    SETNMILINE(cpunum, ASSERT_LINE);
                    SETNMILINE(cpunum, CLEAR_LINE);
                    break;
                case HOLD_LINE:
                case ASSERT_LINE:
                    SETNMILINE(cpunum, ASSERT_LINE);
                    break;
                case CLEAR_LINE:
                    SETNMILINE(cpunum, CLEAR_LINE);
                    break;
                default:
                    logerror("cpu_manualnmicallback cpu_old #%d unknown state %d\n", cpunum, state);
            }
            /* update the CPU's context */
            if (cpu.get(activecpu).save_context != 0) {
                cpu.get(activecpu).context = GETCONTEXT(activecpu);

            }
            activecpu = oldactive;
            if (activecpu >= 0) {
                memorycontextswap(activecpu);
            }

            /* generate a trigger to unsuspend any CPUs waiting on the interrupt */
            if (state != CLEAR_LINE) {
                timer_trigger(TRIGGER_INT + cpunum);
            }
        }
    };

    public static TimerCallbackHandlerPtr cpu_manualirqcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int cpunum, irqline, state, oldactive;

            irqline = param & 7;
            cpunum = (param >> 3) & 7;
            state = param >> 6;

            /* swap to the CPU's context */
            oldactive = activecpu;
            activecpu = cpunum;
            memorycontextswap(activecpu);
            if (cpu.get(activecpu).save_context != 0) {
                SETCONTEXT(activecpu, cpu.get(activecpu).context);
            }
            logerror("cpu_manualirqcallback %d,%d,%d\n", cpunum, irqline, state);

            irq_line_state[cpunum * MAX_IRQ_LINES + irqline] = state;
            switch (state) {
                case PULSE_LINE:
                    SETIRQLINE(cpunum, irqline, ASSERT_LINE);
                    SETIRQLINE(cpunum, irqline, CLEAR_LINE);
                    break;
                case HOLD_LINE:
                case ASSERT_LINE:
                    SETIRQLINE(cpunum, irqline, ASSERT_LINE);
                    break;
                case CLEAR_LINE:
                    SETIRQLINE(cpunum, irqline, CLEAR_LINE);
                    break;
                default:
                    logerror("cpu_manualirqcallback cpu_old #%d, line %d, unknown state %d\n", cpunum, irqline, state);
            }
            /* update the CPU's context */
            if (cpu.get(activecpu).save_context != 0) {
                cpu.get(activecpu).context = GETCONTEXT(activecpu);

            }
            activecpu = oldactive;
            if (activecpu >= 0) {
                memorycontextswap(activecpu);
            }

            /* generate a trigger to unsuspend any CPUs waiting on the interrupt */
            if (state != CLEAR_LINE) {
                timer_trigger(TRIGGER_INT + cpunum);
            }
        }
    };

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
/*TODO*///	LOG(("CPU#%d internal interrupt type $%04x\n", cpunum, type));
/*TODO*///	/* generate the interrupt */
/*TODO*///	cpu_internal_interrupt(cpunum, type);
/*TODO*///}
/*TODO*///
    public static void cpu_generate_interrupt(int cpunum, InterruptHandlerPtr func, int num) {
        int oldactive = activecpu;

        /* don't trigger interrupts on suspended CPUs */
        if (cpu_getstatus(cpunum) == 0) {
            return;
        }

        /* swap to the CPU's context */
        activecpu = cpunum;
        memorycontextswap(activecpu);
        if (cpu.get(activecpu).save_context != 0) {
            SETCONTEXT(activecpu, cpu.get(activecpu).context);
        }
        /* cause the interrupt, calling the function if it exists */
        if (func != null) {
            num = func.handler();
        }

        /* wrapper for the new interrupt system */
        if (num != INT_TYPE_NONE(cpunum)) {
            //logerror("CPU#%d interrupt type $%04x: ", cpunum, num);
            /* is it the NMI type interrupt of that CPU? */
            if (num == INT_TYPE_NMI(cpunum)) {

                //logerror("NMI\n");
                cpu_manualnmicallback.handler(cpunum | (PULSE_LINE << 3));

            } else {
                int irq_line;
                switch (CPU_TYPE(cpunum)) {
                    case CPU_Z80:
                        irq_line = 0;
                        break;
                    /*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case I8080_INTR:		irq_line = 0; LOG(("I8080 INTR\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("I8080 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_8085A)
/*TODO*///			case CPU_8085A:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case I8085_INTR:		irq_line = 0; LOG(("I8085 INTR\n")); break;
/*TODO*///				case I8085_RST55:		irq_line = 1; LOG(("I8085 RST55\n")); break;
/*TODO*///				case I8085_RST65:		irq_line = 2; LOG(("I8085 RST65\n")); break;
/*TODO*///				case I8085_RST75:		irq_line = 3; LOG(("I8085 RST75\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("I8085 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
                    case CPU_M6502:
                        irq_line = 0;
                        break;
                    /*TODO*///#if (HAS_M65C02)
/*TODO*///			case CPU_M65C02:			irq_line = 0; LOG(("M65C02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M65SC02)
/*TODO*///			case CPU_M65SC02:			irq_line = 0; LOG(("M65SC02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M65CE02)
/*TODO*///			case CPU_M65CE02:			irq_line = 0; LOG(("M65CE02 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6509)
/*TODO*///			case CPU_M6509: 			irq_line = 0; LOG(("M6509 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6510)
/*TODO*///			case CPU_M6510: 			irq_line = 0; LOG(("M6510 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M6510T)
/*TODO*///			case CPU_M6510T:			irq_line = 0; LOG(("M6510T IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M7501)
/*TODO*///			case CPU_M7501: 			irq_line = 0; LOG(("M7501 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_M8502)
/*TODO*///			case CPU_M8502: 			irq_line = 0; LOG(("M8502 IRQ\n")); break;
/*TODO*///#endif
                    case CPU_N2A03:
                        irq_line = 0;
                        break;
                    /*TODO*///			case CPU_M4510: 			irq_line = 0; LOG(("M4510 IRQ\n")); break;
                    case CPU_H6280:
                        switch (num) {
                            case H6280_INT_IRQ1:
                                irq_line = 0;
                                break;
                            case H6280_INT_IRQ2:
                                irq_line = 1;
                                break;
                            case H6280_INT_TIMER:
                                irq_line = 2;
                                break;
                            default:
                                irq_line = 0;
                        }
                        break;
                    /*TODO*///#endif
/*TODO*///#if (HAS_I86)
/*TODO*///			case CPU_I86:				irq_line = 0; LOG(("I86 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I88)
/*TODO*///			case CPU_I88:				irq_line = 0; LOG(("I88 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I186)
/*TODO*///			case CPU_I186:				irq_line = 0; LOG(("I186 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I188)
/*TODO*///			case CPU_I188:				irq_line = 0; LOG(("I188 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_I286)
/*TODO*///			case CPU_I286:				irq_line = 0; LOG(("I286 IRQ\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_V20)
/*TODO*///			case CPU_V20:				irq_line = 0; LOG(("V20 IRQ\n")); break;
                    case CPU_V30:
                        irq_line = 0;
                        break;

                    case CPU_V33:
                        irq_line = 0;
                        break;

                    case CPU_I8035:
                        irq_line = 0;
                        break;

                    case CPU_I8039:
                        irq_line = 0;
                        break;

                    case CPU_I8048:
                        irq_line = 0;
                        break;

                    case CPU_N7751:
                        irq_line = 0;
                        break;

                    case CPU_M6800:
                        irq_line = 0;
                        break;

                    case CPU_M6801:
                        irq_line = 0;
                        break;

                    case CPU_M6802:
                        irq_line = 0;
                        break;

                    case CPU_M6803:
                        irq_line = 0;
                        break;

                    case CPU_M6808:
                        irq_line = 0;
                        break;

                    case CPU_HD63701:
                        irq_line = 0;
                        break;

                    case CPU_M6805:
                        irq_line = 0;
                        break;

                    case CPU_M68705:
                        irq_line = 0;
                        break;

                    case CPU_HD63705:
                        irq_line = 0;
                        break;

                    case CPU_HD6309:
                        switch (num) {
                            case HD6309_INT_IRQ:
                                irq_line = 0;
                                break;
                            case HD6309_INT_FIRQ:
                                irq_line = 1;
                                break;
                            default:
                                irq_line = 0;
                        }
                        break;

                    case CPU_M6809:
                        switch (num) {
                            case M6809_INT_IRQ:
                                irq_line = 0;
                                break;
                            case M6809_INT_FIRQ:
                                irq_line = 1;
                                break;
                            default:
                                irq_line = 0;
                        }
                        break;

                    case CPU_KONAMI:
                        switch (num) {
                            case KONAMI_INT_IRQ:
                                irq_line = 0;
                                break;
                            case KONAMI_INT_FIRQ:
                                irq_line = 1;
                                break;
                            default:
                                irq_line = 0;
                        }
                        break;

                    case CPU_M68000:
                        switch (num) {
                            case MC68000_IRQ_1:
                                irq_line = 1;
                                break;
                            case MC68000_IRQ_2:
                                irq_line = 2;
                                break;
                            case MC68000_IRQ_3:
                                irq_line = 3;
                                break;
                            case MC68000_IRQ_4:
                                irq_line = 4;
                                break;
                            case MC68000_IRQ_5:
                                irq_line = 5;
                                break;
                            case MC68000_IRQ_6:
                                irq_line = 6;
                                break;
                            case MC68000_IRQ_7:
                                irq_line = 7;
                                break;
                            default:
                                irq_line = 0;
                        }
                        /* until now only auto vector interrupts supported */
                        num = MC68000_INT_ACK_AUTOVECTOR;
                        break;
                    /*TODO*///#if (HAS_M68010)
			case CPU_M68010:
				switch (num)
				{
				case MC68000_IRQ_1: 	irq_line = 1; break;
				case MC68000_IRQ_2: 	irq_line = 2; break;
				case MC68000_IRQ_3: 	irq_line = 3; break;
				case MC68000_IRQ_4: 	irq_line = 4; break;
				case MC68000_IRQ_5: 	irq_line = 5; break;
				case MC68000_IRQ_6: 	irq_line = 6; break;
				case MC68000_IRQ_7: 	irq_line = 7; break;
				default:				irq_line = 0;
				}
				/* until now only auto vector interrupts supported */
				num = MC68000_INT_ACK_AUTOVECTOR;
				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68020)
/*TODO*///			case CPU_M68020:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68020_IRQ_1: 	irq_line = 1; LOG(("M68020 IRQ1\n")); break;
/*TODO*///				case MC68020_IRQ_2: 	irq_line = 2; LOG(("M68020 IRQ2\n")); break;
/*TODO*///				case MC68020_IRQ_3: 	irq_line = 3; LOG(("M68020 IRQ3\n")); break;
/*TODO*///				case MC68020_IRQ_4: 	irq_line = 4; LOG(("M68020 IRQ4\n")); break;
/*TODO*///				case MC68020_IRQ_5: 	irq_line = 5; LOG(("M68020 IRQ5\n")); break;
/*TODO*///				case MC68020_IRQ_6: 	irq_line = 6; LOG(("M68020 IRQ6\n")); break;
/*TODO*///				case MC68020_IRQ_7: 	irq_line = 7; LOG(("M68020 IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("M68020 unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_M68EC020)
/*TODO*///			case CPU_M68EC020:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MC68EC020_IRQ_1:	irq_line = 1; LOG(("M68EC020 IRQ1\n")); break;
/*TODO*///				case MC68EC020_IRQ_2:	irq_line = 2; LOG(("M68EC020 IRQ2\n")); break;
/*TODO*///				case MC68EC020_IRQ_3:	irq_line = 3; LOG(("M68EC020 IRQ3\n")); break;
/*TODO*///				case MC68EC020_IRQ_4:	irq_line = 4; LOG(("M68EC020 IRQ4\n")); break;
/*TODO*///				case MC68EC020_IRQ_5:	irq_line = 5; LOG(("M68EC020 IRQ5\n")); break;
/*TODO*///				case MC68EC020_IRQ_6:	irq_line = 6; LOG(("M68EC020 IRQ6\n")); break;
/*TODO*///				case MC68EC020_IRQ_7:	irq_line = 7; LOG(("M68EC020 IRQ7\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("M68EC020 unknown\n"));
/*TODO*///				}
/*TODO*///				/* until now only auto vector interrupts supported */
/*TODO*///				num = MC68000_INT_ACK_AUTOVECTOR;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_T11)
/*TODO*///			case CPU_T11:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case T11_IRQ0:			irq_line = 0; LOG(("T11 IRQ0\n")); break;
/*TODO*///				case T11_IRQ1:			irq_line = 1; LOG(("T11 IRQ1\n")); break;
/*TODO*///				case T11_IRQ2:			irq_line = 2; LOG(("T11 IRQ2\n")); break;
/*TODO*///				case T11_IRQ3:			irq_line = 3; LOG(("T11 IRQ3\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("T11 unknown\n"));
/*TODO*///				}
/*TODO*///				break;

                    case CPU_S2650:
                        irq_line = 0;
                        break;
                    /*TODO*///#endif
/*TODO*///#if (HAS_F8)
/*TODO*///			case CPU_F8:				irq_line = 0; LOG(("F8 INTR\n")); break;
/*TODO*///#endif
/*TODO*///#if (HAS_TMS34010)
/*TODO*///			case CPU_TMS34010:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case TMS34010_INT1: 	irq_line = 0; LOG(("TMS34010 INT1\n")); break;
/*TODO*///				case TMS34010_INT2: 	irq_line = 1; LOG(("TMS34010 INT2\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("TMS34010 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*////*#if (HAS_TMS9900)
/*TODO*///			case CPU_TMS9900:	irq_line = 0; LOG(("TMS9900 IRQ\n")); break;
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
/*TODO*///				LOG(("Please use the new interrupt scheme for your new developments !\n"));
/*TODO*///				irq_line = 0;
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_Z8000)
/*TODO*///			case CPU_Z8000:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case Z8000_NVI: 		irq_line = 0; LOG(("Z8000 NVI\n")); break;
/*TODO*///				case Z8000_VI:			irq_line = 1; LOG(("Z8000 VI\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("Z8000 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
                    case CPU_TMS320C10:
                        switch (num) {
                            case TMS320C10_ACTIVE_INT:
                                irq_line = 0;
                                break;
                            case TMS320C10_ACTIVE_BIO:
                                irq_line = 1;
                                break;
                            default:
                                irq_line = 0;
                        }
                        break;
                    /*TODO*///#endif
/*TODO*///#if (HAS_ADSP2100)
/*TODO*///			case CPU_ADSP2100:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case ADSP2100_IRQ0: 		irq_line = 0; LOG(("ADSP2100 IRQ0\n")); break;
/*TODO*///				case ADSP2100_IRQ1: 		irq_line = 1; LOG(("ADSP2100 IRQ1\n")); break;
/*TODO*///				case ADSP2100_IRQ2: 		irq_line = 2; LOG(("ADSP2100 IRQ1\n")); break;
/*TODO*///				case ADSP2100_IRQ3: 		irq_line = 3; LOG(("ADSP2100 IRQ1\n")); break;
/*TODO*///				default:					irq_line = 0; LOG(("ADSP2100 unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
/*TODO*///#if (HAS_PSXCPU)
/*TODO*///			case CPU_PSX:
/*TODO*///				switch (num)
/*TODO*///				{
/*TODO*///				case MIPS_IRQ0: 		irq_line = 0; LOG(("MIPS IRQ0\n")); break;
/*TODO*///				case MIPS_IRQ1: 		irq_line = 1; LOG(("MIPS IRQ1\n")); break;
/*TODO*///				case MIPS_IRQ2: 		irq_line = 2; LOG(("MIPS IRQ2\n")); break;
/*TODO*///				case MIPS_IRQ3: 		irq_line = 3; LOG(("MIPS IRQ3\n")); break;
/*TODO*///				case MIPS_IRQ4: 		irq_line = 4; LOG(("MIPS IRQ4\n")); break;
/*TODO*///				case MIPS_IRQ5: 		irq_line = 5; LOG(("MIPS IRQ5\n")); break;
/*TODO*///				default:				irq_line = 0; LOG(("MIPS unknown\n"));
/*TODO*///				}
/*TODO*///				break;
/*TODO*///#endif
                    default:
                        irq_line = 0;
                    /* else it should be an IRQ type; assume line 0 and store vector */
                }
                cpu_irq_line_vector_w(cpunum, irq_line, num);
                cpu_manualirqcallback.handler(irq_line | (cpunum << 3) | (HOLD_LINE << 6));
            }
        }

        /* update the CPU's context */
        if (cpu.get(activecpu).save_context != 0) {
            cpu.get(activecpu).context = GETCONTEXT(activecpu);
        }
        activecpu = oldactive;
        if (activecpu >= 0) {
            memorycontextswap(activecpu);
        }

    }

    public static void cpu_clear_interrupts(int cpunum) {
        int oldactive = activecpu;
        int i;

        /* swap to the CPU's context */
        activecpu = cpunum;
        memorycontextswap(activecpu);
        if (cpu.get(activecpu).save_context != 0) {
            SETCONTEXT(activecpu, cpu.get(activecpu).context);
        }

        /* clear NMI line */
        SETNMILINE(activecpu, CLEAR_LINE);

        /* clear all IRQ lines */
        for (i = 0; i < cpu.get(activecpu).intf.num_irqs; i++) {
            SETIRQLINE(activecpu, i, CLEAR_LINE);
        }

        /* update the CPU's context */
        if (cpu.get(activecpu).save_context != 0) {
            cpu.get(activecpu).context = GETCONTEXT(activecpu);
        }
        activecpu = oldactive;
        if (activecpu >= 0) {
            memorycontextswap(activecpu);
        }
    }

    public static void cpu_reset_cpu(int cpunum) {
        int oldactive = activecpu;

        /* swap to the CPU's context */
        activecpu = cpunum;
        memorycontextswap(activecpu);
        if (cpu.get(activecpu).save_context != 0) {
            SETCONTEXT(activecpu, cpu.get(activecpu).context);
        }
        /* reset the CPU */
        RESET(cpunum);

        /* Set the irq callback for the cpu_old */
        SETIRQCALLBACK(cpunum, cpu_irq_callbacks[cpunum]);

        /* update the CPU's context */
        if (cpu.get(activecpu).save_context != 0) {
            cpu.get(activecpu).context = GETCONTEXT(activecpu);
        }
        activecpu = oldactive;
        if (activecpu >= 0) {
            memorycontextswap(activecpu);
        }
    }

    public static TimerCallbackHandlerPtr cpu_haltcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int state = param >> 3;
            int cpunum = param & 7;

            /* reset the CPU */
            if (state == ASSERT_LINE) {
                timer_suspendcpu(cpunum, 1, SUSPEND_REASON_HALT);/* halt cpu */
            } else if (state == CLEAR_LINE) {
                timer_suspendcpu(cpunum, 0, SUSPEND_REASON_HALT);/* restart cpu */
            }
        }
    };

    /**
     * *************************************************************************
     * VBLANK reset. Called at the start of emulation and once per VBLANK in
     * order to update the input ports and reset the interrupt counter.
     * *************************************************************************
     */
    public static void cpu_vblankreset() {
        int i;

        /* read hi scores from disk */
 /*TODO*///       hs_update();

        /* read keyboard & update the status of the input ports */
        update_input_ports();

        /* reset the cycle counters */
        for (i = 0; i < totalcpu; i++) {
            if (timer_iscpususpended(i, SUSPEND_ANY_REASON) == 0) {
                cpu.get(i).iloops = Machine.drv.cpu[i].vblank_interrupts_per_frame - 1;
            } else {
                cpu.get(i).iloops = -1;
            }
        }
    }

    /**
     * *************************************************************************
     * VBLANK callback. This is called 'vblank_multipler' times per frame to
     * service VBLANK-synced interrupts and to begin the screen update process.
     * *************************************************************************
     */
    public static TimerCallbackHandlerPtr cpu_firstvblankcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {

            /* now that we're synced up, pulse from here on out */
            vblank_timer = timer_pulse(vblank_period, param, cpu_vblankcallback);

            /* but we need to call the standard routine as well */
            cpu_vblankcallback.handler(param);
        }
    };
    /* note that calling this with param == -1 means count everything, but call no subroutines */
    public static TimerCallbackHandlerPtr cpu_vblankcallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            int i;

            /* loop over CPUs */
            for (i = 0; i < totalcpu; i++) {
                /* if the interrupt multiplier is valid */
                if (cpu.get(i).vblankint_multiplier != -1) {
                    /* decrement; if we hit zero, generate the interrupt and reset the countdown */
                    if (--cpu.get(i).vblankint_countdown == 0) {
                        if (param != -1) {
                            cpu_vblankintcallback(i);
                        }
                        cpu.get(i).vblankint_countdown = cpu.get(i).vblankint_multiplier;
                        timer_reset(cpu.get(i).vblankint_timer, TIME_NEVER);
                    }
                } /* else reset the VBLANK timer if this is going to be a real VBLANK */ else if (vblank_countdown == 1) {
                    timer_reset(cpu.get(i).vblankint_timer, TIME_NEVER);
                }
            }
            /* is it a real VBLANK? */
            if (--vblank_countdown == 0) {
                /* do we update the screen now? */
                if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) == 0) {
                    usres = updatescreen();
                }

                /* Set the timer to update the screen */
                timer_set(TIME_IN_USEC(Machine.drv.vblank_duration), 0, cpu_updatecallback);
                vblank = 1;

                /* reset the globals */
                cpu_vblankreset();

                /* reset the counter */
                vblank_countdown = vblank_multiplier;
            }
        }
    };
    /**
     * *************************************************************************
     * Video update callback. This is called a game-dependent amount of time
     * after the VBLANK in order to trigger a video update.
     * *************************************************************************
     */
    public static TimerCallbackHandlerPtr cpu_updatecallback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            /* update the screen if we didn't before */
            if ((Machine.drv.video_attributes & VIDEO_UPDATE_AFTER_VBLANK) != 0) {
                usres = updatescreen();
            }
            vblank = 0;

            /* update IPT_VBLANK input ports */
            inputport_vblank_end();

            /* check the watchdog */
            if (watchdog_counter > 0) {
                if (--watchdog_counter == 0) {
                    logerror("reset caused by the watchdog\n");
                    machine_reset();
                }
            }

            current_frame++;

            /* reset the refresh timer */
            timer_reset(refresh_timer, TIME_NEVER);
        }
    };

    /**
     * *************************************************************************
     * Converts an integral timing rate into a period. Rates can be specified as
     * follows:
     * <p>
     * rate > 0	. 'rate' cycles per frame rate == 0	. 0 rate >= -10000 . 'rate'
     * cycles per second rate < -10000 . 'rate' nanoseconds
     * *************************************************************************
     */
    public static double cpu_computerate(int value) {
        /* values equal to zero are zero */
        if (value <= 0) {
            return 0.0;
        }

        /* values above between 0 and 50000 are in Hz */
        if (value < 50000) {
            return TIME_IN_HZ(value);
        } /* values greater than 50000 are in nanoseconds */ else {
            return TIME_IN_NSEC(value);
        }
    }

    public static TimerCallbackHandlerPtr cpu_timeslicecallback = new TimerCallbackHandlerPtr() {
        public void handler(int i) {
            timer_trigger(TRIGGER_TIMESLICE);
        }
    };

    /**
     * *************************************************************************
     * Initializes all the timers used by the CPU system.
     * *************************************************************************
     */
    static void cpu_inittimers() {
        double first_time;
        int i, max, ipf;

        /* remove old timers */
        if (timeslice_timer != null) {
            timer_remove(timeslice_timer);
        }
        if (refresh_timer != null) {
            timer_remove(refresh_timer);
        }
        if (vblank_timer != null) {
            timer_remove(vblank_timer);
        }
        /* allocate a dummy timer at the minimum frequency to break things up */
        ipf = Machine.drv.cpu_slices_per_frame;
        if (ipf <= 0) {
            ipf = 1;
        }
        timeslice_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
        timeslice_timer = timer_pulse(timeslice_period, 0, cpu_timeslicecallback);

        /* allocate an infinite timer to track elapsed time since the last refresh */
        refresh_period = TIME_IN_HZ(Machine.drv.frames_per_second);
        refresh_period_inv = 1.0 / refresh_period;
        refresh_timer = timer_set(TIME_NEVER, 0, null);

        /* while we're at it, compute the scanline times */
        if (Machine.drv.vblank_duration != 0) {
            scanline_period = (refresh_period - TIME_IN_USEC(Machine.drv.vblank_duration))
                    / (double) (Machine.visible_area.max_y - Machine.visible_area.min_y + 1);
        } else {
            scanline_period = refresh_period / (double) Machine.drv.screen_height;
        }
        scanline_period_inv = 1.0 / scanline_period;
        /*
		 *		The following code finds all the CPUs that are interrupting in sync with the VBLANK
		 *		and sets up the VBLANK timer to run at the minimum number of cycles per frame in
		 *		order to service all the synced interrupts
         */

 /* find the CPU with the maximum interrupts per frame */
        max = 1;
        for (i = 0; i < totalcpu; i++) {
            ipf = Machine.drv.cpu[i].vblank_interrupts_per_frame;
            if (ipf > max) {
                max = ipf;
            }
        }

        /* now find the LCD with the rest of the CPUs (brute force - these numbers aren't huge) */
        vblank_multiplier = max;
        while (true) {
            for (i = 0; i < totalcpu; i++) {
                ipf = Machine.drv.cpu[i].vblank_interrupts_per_frame;
                if (ipf > 0 && (vblank_multiplier % ipf) != 0) {
                    break;
                }
            }
            if (i == totalcpu) {
                break;
            }
            vblank_multiplier += max;
        }

        /* initialize the countdown timers and intervals */
        for (i = 0; i < totalcpu; i++) {
            ipf = Machine.drv.cpu[i].vblank_interrupts_per_frame;
            if (ipf > 0) {
                cpu.get(i).vblankint_countdown = cpu.get(i).vblankint_multiplier = vblank_multiplier / ipf;
            } else {
                cpu.get(i).vblankint_countdown = cpu.get(i).vblankint_multiplier = -1;
            }
        }

        /* allocate a vblank timer at the frame rate * the LCD number of interrupts per frame */
        vblank_period = TIME_IN_HZ(Machine.drv.frames_per_second * vblank_multiplier);
        vblank_timer = timer_pulse(vblank_period, 0, cpu_vblankcallback);
        vblank_countdown = vblank_multiplier;

        /*
		 *		The following code creates individual timers for each CPU whose interrupts are not
		 *		synced to the VBLANK, and computes the typical number of cycles per interrupt
         */

 /* start the CPU interrupt timers */
        for (i = 0; i < totalcpu; i++) {
            ipf = Machine.drv.cpu[i].vblank_interrupts_per_frame;

            /* remove old timers */
            if (cpu.get(i).vblankint_timer != null) {
                timer_remove(cpu.get(i).vblankint_timer);
            }
            if (cpu.get(i).timedint_timer != null) {
                timer_remove(cpu.get(i).timedint_timer);
            }

            /* compute the average number of cycles per interrupt */
            if (ipf <= 0) {
                ipf = 1;
            }
            cpu.get(i).vblankint_period = TIME_IN_HZ(Machine.drv.frames_per_second * ipf);
            cpu.get(i).vblankint_timer = timer_set(TIME_NEVER, 0, null);

            /* see if we need to allocate a CPU timer */
            ipf = Machine.drv.cpu[i].timed_interrupts_per_second;
            if (ipf != 0) {
                cpu.get(i).timedint_period = cpu_computerate(ipf);
                cpu.get(i).timedint_timer = timer_pulse(cpu.get(i).timedint_period, i, cpu_timedintcallback);
            }
        }
        /* note that since we start the first frame on the refresh, we can't pulse starting
		   immediately; instead, we back up one VBLANK period, and inch forward until we hit
		   positive time. That time will be the time of the first VBLANK timer callback */
        timer_remove(vblank_timer);

        first_time = -TIME_IN_USEC(Machine.drv.vblank_duration) + vblank_period;
        while (first_time < 0) {
            cpu_vblankcallback.handler(-1);
            first_time += vblank_period;
        }
        vblank_timer = timer_set(first_time, 0, cpu_firstvblankcallback);
    }

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
/*TODO*////***************************************************************************
/*TODO*///  Retrieve or set a cycle counts lookup table for the active CPU
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///void *cpu_get_cycle_table(int which)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	return GETCYCLETBL(cpunum,which);
/*TODO*///}
/*TODO*///
/*TODO*///void cpu_set_cycle_tbl(int which, void *new_table)
/*TODO*///{
/*TODO*///	int cpunum = (activecpu < 0) ? 0 : activecpu;
/*TODO*///	SETCYCLETBL(cpunum,which,new_table);
/*TODO*///}
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
/*TODO*///	return MHMASK(cpuintf[CPU_TYPE(cpunum)].address_bits);
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
/*TODO*///		return MHMASK(cpuintf[cpu_type].address_bits);
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
    /**
     * *************************************************************************
     * Returns the name for a specific CPU type
     * *************************************************************************
     */
    public static String cputype_name(int cpu_type) {
        cpu_type &= ~CPU_FLAGS_MASK;
        if (cpu_type < CPU_COUNT) {
            return IFC_INFO(cpu_type, null, CPU_INFO_NAME);
        }
        return "";
    }

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
    /**
     * *************************************************************************
     * Returns the core filename for a specific CPU type
     * *************************************************************************
     */
    public static String cputype_core_file(int cpu_type) {
        cpu_type &= ~CPU_FLAGS_MASK;
        if (cpu_type < CPU_COUNT) {
            return IFC_INFO(cpu_type, null, CPU_INFO_FILE);
        }
        return "";
    }

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
    /**
     * *************************************************************************
     * Returns the core filename for a specific CPU number
     * *************************************************************************
     */
    public static String cpunum_core_file(int cpunum) {
        if (cpunum < totalcpu) {
            return cputype_core_file(CPU_TYPE(cpunum));
        }
        return "";
    }
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///		if (cpu[activecpu].save_context) GETCONTEXT(activecpu, cpu[activecpu].context);
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
/*TODO*///	if (activecpu >= 0)
/*TODO*///	{
/*TODO*///		memorycontextswap(activecpu);
/*TODO*///		if (cpu[activecpu].save_context) SETCONTEXT(activecpu, cpu[activecpu].context);
/*TODO*///	}
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
}
