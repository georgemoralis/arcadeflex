package gr.codebb.arcadeflex.v036.cpu.m68000;

public class m68000H {


    /* NOTE: M68K_SP fetches the current SP, be it USP, ISP, or MSP */
    public static final int M68K_PC = 1;
    public static final int M68K_SP = 2;
    public static final int M68K_ISP = 3;
    public static final int M68K_USP = 4;
    public static final int M68K_MSP = 5;
    public static final int M68K_SR = 6;
    public static final int M68K_VBR = 7;
    public static final int M68K_SFC = 8;
    public static final int M68K_DFC = 9;
    public static final int M68K_CACR = 10;
    public static final int M68K_CAAR = 11;
    public static final int M68K_PREF_ADDR = 12;
    public static final int M68K_PREF_DATA = 13;
    public static final int M68K_D0 = 14;
    public static final int M68K_D1 = 15;
    public static final int M68K_D2 = 16;
    public static final int M68K_D3 = 17;
    public static final int M68K_D4 = 18;
    public static final int M68K_D5 = 19;
    public static final int M68K_D6 = 20;
    public static final int M68K_D7 = 21;
    public static final int M68K_A0 = 22;
    public static final int M68K_A1 = 23;
    public static final int M68K_A2 = 24;
    public static final int M68K_A3 = 25;
    public static final int M68K_A4 = 26;
    public static final int M68K_A5 = 27;
    public static final int M68K_A6 = 28;
    public static final int M68K_A7 = 29;

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
