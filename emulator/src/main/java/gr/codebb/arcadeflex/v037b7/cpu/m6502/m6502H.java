
package gr.codebb.arcadeflex.v037b7.cpu.m6502;


/**
 *
 * @author george
 */
public class m6502H {

    public static final int M6502_PC = 1;
    public static final int M6502_S = 2;
    public static final int M6502_P = 3;
    public static final int M6502_A = 4;
    public static final int M6502_X = 5;
    public static final int M6502_Y = 6;
    public static final int M6502_EA = 7;
    public static final int M6502_ZP = 8;
    public static final int M6502_NMI_STATE = 9;
    public static final int M6502_IRQ_STATE = 10;
    public static final int M6502_SO_STATE = 11;

    public static final int M6502_INT_NONE = 0;
    public static final int M6502_INT_IRQ = 1;
    public static final int M6502_INT_NMI = 2;
    /* use cpu_set_irq_line(cpu, M6502_SET_OVERFLOW, level)
   to change level of the so input line
   positiv edge sets overflow flag */
    public static final int M6502_SET_OVERFLOW = 3;

    public static final int M6502_NMI_VEC = 0xfffa;
    public static final int M6502_RST_VEC = 0xfffc;
    public static final int M6502_IRQ_VEC = 0xfffe;

    public static double N2A03_DEFAULTCLOCK = (21477272.724 / 12);
    public static int N2A03_INT_NONE = M6502_INT_NONE;
    public static int N2A03_INT_IRQ = M6502_INT_IRQ;
    public static int N2A03_INT_NMI = M6502_INT_NMI;
}
