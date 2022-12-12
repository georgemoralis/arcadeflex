package arcadeflex.v036.cpu;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;

public class dummyCPU extends cpu_interface {

    public dummyCPU() {
        cpu_num = CPU_DUMMY;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = 0;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 1;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;

        //intialize interfaces
        burn = burn_function;
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        if (context == null && regnum != 0) {
            return "";
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public BurnHandlerPtr burn_function = new BurnHandlerPtr() {
        public void handler(int cycles) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    @Override
    public Object init_context() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void reset(Object param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_irq_callback(IrqCallbackHandlerPtr callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int execute(int cycles) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int get_pc() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_op_base(int pc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int memory_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void memory_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
