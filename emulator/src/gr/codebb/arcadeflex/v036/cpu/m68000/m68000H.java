package gr.codebb.arcadeflex.v036.cpu.m68000;

public class m68000H {
    public static final int MC68000_INT_NONE = 0;
    public static final int MC68000_IRQ_1 = 1;
    public static final int MC68000_IRQ_2 = 2;
    public static final int MC68000_IRQ_3 = 3;
    public static final int MC68000_IRQ_4 = 4;
    public static final int MC68000_IRQ_5 = 5;
    public static final int MC68000_IRQ_6 = 6;
    public static final int MC68000_IRQ_7 = 7;

    public static final int MC68000_INT_ACK_AUTOVECTOR = -1;
    public static final int MC68000_INT_ACK_SPURIOUS = -2;

    public static final int MC68000_CPU_MODE_68000 = 1;
    public static final int MC68000_CPU_MODE_68010 = 2;
    public static final int MC68000_CPU_MODE_68020 = 4;
}
