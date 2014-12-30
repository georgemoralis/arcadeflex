/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.m68000;

import mame.cpuintrfH.cpu_interface;
import static mame.cpuintrfH.*;
import static mame.cpuintrfH.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.memory.*;

public class m68000 extends cpu_interface {

    public m68000() {
        cpu_num = CPU_M68000;
        num_irqs = 8;
        default_vector = -1;
        overclock = 1.0;
        //        no_int = MC68000_INT_NONE;
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
        //        icount = m68k_clks_left;
        //       m68k_clks_left[0] = 0;
    }

    @Override
    public void reset(Object param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

}
