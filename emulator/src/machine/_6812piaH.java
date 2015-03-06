package machine;

import static mame.driverH.*;

public class _6812piaH {

    public static final int MAX_PIA = 8;


    /* this is the standard ordering of the registers */
    /* alternate ordering swaps registers 1 and 2 */
    public static final int PIA_DDRA = 0;
    public static final int PIA_CTLA = 1;
    public static final int PIA_DDRB = 2;
    public static final int PIA_CTLB = 3;

    /* PIA addressing modes */
    public static final int PIA_STANDARD_ORDERING = 0;
    public static final int PIA_ALTERNATE_ORDERING = 1;

    public static final int PIA_8BIT = 0;
    public static final int PIA_16BIT = 2;

    public static final int PIA_LOWER = 0;
    public static final int PIA_UPPER = 4;
    public static final int PIA_AUTOSENSE = 8;

    public static final int PIA_16BIT_LOWER = (PIA_16BIT | PIA_LOWER);
    public static final int PIA_16BIT_UPPER = (PIA_16BIT | PIA_UPPER);
    public static final int PIA_16BIT_AUTO = (PIA_16BIT | PIA_AUTOSENSE);

    public static abstract interface irqfuncPtr {

        public abstract void handler(int state);
    }

    public static class pia6821_interface {

        public pia6821_interface(ReadHandlerPtr in_a_func, ReadHandlerPtr in_b_func, ReadHandlerPtr in_ca1_func,
                ReadHandlerPtr in_cb1_func,
                ReadHandlerPtr in_ca2_func,
                ReadHandlerPtr in_cb2_func,
                WriteHandlerPtr out_a_func,
                WriteHandlerPtr out_b_func,
                WriteHandlerPtr out_ca2_func,
                WriteHandlerPtr out_cb2_func,
                irqfuncPtr irq_a_func,
                irqfuncPtr irq_b_func) {
            this.in_a_func = in_a_func;
            this.in_b_func = in_b_func;
            this.in_ca1_func = in_ca1_func;
            this.in_cb1_func = in_cb1_func;
            this.in_ca2_func = in_ca2_func;
            this.in_cb2_func = in_cb2_func;
            this.out_b_func = out_b_func;
            this.out_ca2_func = out_ca2_func;
            this.out_cb2_func = out_cb2_func;
            this.irq_a_func = irq_a_func;
            this.irq_b_func = irq_b_func;
        }
        ReadHandlerPtr in_a_func;
        ReadHandlerPtr in_b_func;
        ReadHandlerPtr in_ca1_func;
        ReadHandlerPtr in_cb1_func;
        ReadHandlerPtr in_ca2_func;
        ReadHandlerPtr in_cb2_func;
        WriteHandlerPtr out_a_func;
        WriteHandlerPtr out_b_func;
        WriteHandlerPtr out_ca2_func;
        WriteHandlerPtr out_cb2_func;
        irqfuncPtr irq_a_func;
        irqfuncPtr irq_b_func;
    };
}
