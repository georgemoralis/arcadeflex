package gr.codebb.arcadeflex.v036.cpu.h6280;

public class h6280H {

    /*TODO*///enum {
/*TODO*///	H6280_PC=1, H6280_S, H6280_P, H6280_A, H6280_X, H6280_Y,
/*TODO*///	H6280_IRQ_MASK, H6280_TIMER_STATE,
/*TODO*///	H6280_NMI_STATE, H6280_IRQ1_STATE, H6280_IRQ2_STATE, H6280_IRQT_STATE
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///    ,
/*TODO*///	H6280_M1, H6280_M2, H6280_M3, H6280_M4,
/*TODO*///	H6280_M5, H6280_M6, H6280_M7, H6280_M8
/*TODO*///#endif
/*TODO*///};
/*TODO*///
/*TODO*/////#define LAZY_FLAGS  1
/*TODO*///
    public static final int H6280_INT_NONE = 0;
    public static final int H6280_INT_NMI = 1;
    public static final int H6280_INT_TIMER = 2;
    public static final int H6280_INT_IRQ1 = 3;
    public static final int H6280_INT_IRQ2 = 4;

    public static final int H6280_RESET_VEC = 0xfffe;
    public static final int H6280_NMI_VEC = 0xfffc;
    public static final int H6280_TIMER_VEC = 0xfffa;
    public static final int H6280_IRQ1_VEC = 0xfff8;
    public static final int H6280_IRQ2_VEC = 0xfff6;
    /* Aka BRK vector */
}
