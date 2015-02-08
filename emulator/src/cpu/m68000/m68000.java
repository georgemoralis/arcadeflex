package cpu.m68000;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;
import static cpu.m68000.m68000H.*;
import static cpu.m68000.m68kopsH.*;

public class m68000 extends cpu_interface {

    static int m68k_emulation_initialized = 0;                /* flag if emulation has been initialized */
    public static opcode[] m68k_instruction_jump_table = new opcode[0x10000]; /* opcode handler jump table */

    static int[] m68k_clks_left = new int[1];                            /* Number of clocks remaining */
    /*TODO*///uint         m68k_tracing = 0;


    public m68000() {
        cpu_num = CPU_M68000;
        num_irqs = 8;
        default_vector = -1;
        overclock = 1.0;
        no_int = MC68000_INT_NONE;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 24;
        endianess = CPU_IS_BE;
        align_unit = 2;
        max_inst_len = 10;
        abits1 = ABITS1_24;
        abits2 = ABITS2_24;
        abitsmin = ABITS_MIN_24;
        icount = m68k_clks_left;
        m68k_clks_left[0] = 0;
    }

    static m68k_cpu_core m68k_cpu = new m68k_cpu_core();

    @Override
    public void reset(Object param) {
        m68k_pulse_reset(param);
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int execute(int cycles) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object init_context() {
        Object reg = new m68k_cpu_core();
        return reg;
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_pc() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "68000";
            case CPU_INFO_FAMILY:
                return "Motorola 68K";
            case CPU_INFO_VERSION:
                return "2.1";
            case CPU_INFO_FILE:
                return "m68000.cs";
            case CPU_INFO_CREDITS:
                return "Copyright 1999 Karl Stenerud. All rights reserved. (2.1 fixes HJB)";
        }
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public int memory_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set_op_base(int pc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void m68k_pulse_reset(Object param) {
        /*TOD0*///   CPU_HALTED = 0;
/*TOD0*///   CPU_STOPPED = 0;
/*TOD0*///   CPU_INT_STATE = 0;	/* ASG: changed from CPU_INTS_PENDING */
/*TOD0*///   CPU_T1 = CPU_T0 = 0;
/*TOD0*///   m68ki_clear_trace();
/*TOD0*///   CPU_S = 1;
/*TOD0*///   CPU_M = 0;
/*TOD0*///   CPU_INT_MASK = 7;
/*TOD0*///   CPU_VBR = 0;
/*TOD0*///   CPU_A[7] = m68ki_read_32(0);
/*TOD0*///   m68ki_set_pc(m68ki_read_32(4));
/*TOD0*///#if M68K_USE_PREFETCH
/*TOD0*///   CPU_PREF_ADDR = MASK_OUT_BELOW_2(CPU_PC);
/*TOD0*///   CPU_PREF_DATA = m68k_read_immediate_32(ADDRESS_68K(CPU_PREF_ADDR));
/*TOD0*///#endif /* M68K_USE_PREFETCH */
/*TOD0*///   m68k_clks_left = 0;

        /*TOD0*///   if (CPU_MODE == 0) CPU_MODE = M68K_DEFAULT_CPU_MODE;	/* KW 990319 */
   /* The first call to this function initializes the opcode handler jump table */
        if (m68k_emulation_initialized != 0) {
            return;
        } else {
            /*TOD0*///     m68ki_build_opcode_table();
 /*TOD0*///      m68k_set_int_ack_callback(NULL);
 /*TOD0*///      m68k_set_bkpt_ack_callback(NULL);
 /*TOD0*///      m68k_set_reset_instr_callback(NULL);
 /*TOD0*///      m68k_set_pc_changed_callback(NULL);
 /*TOD0*///      m68k_set_fc_callback(NULL);
 /*TOD0*///      m68k_set_instr_hook_callback(NULL);

            m68k_emulation_initialized = 1;
        }
    }
}
