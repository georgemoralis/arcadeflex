package gr.codebb.arcadeflex.v037b7.cpu.i8085;

public class i8085H {

    /*TODO*///enum {
/*TODO*///	I8085_PC=1, I8085_SP, I8085_AF ,I8085_BC, I8085_DE, I8085_HL,
/*TODO*///	I8085_HALT, I8085_IM, I8085_IREQ, I8085_ISRV, I8085_VECTOR,
/*TODO*///	I8085_TRAP_STATE, I8085_INTR_STATE,
/*TODO*///	I8085_RST55_STATE, I8085_RST65_STATE, I8085_RST75_STATE};
/*TODO*///
    public static final int I8085_INTR_LINE = 0;
    public static final int I8085_RST55_LINE = 1;
    public static final int I8085_RST65_LINE = 2;
    public static final int I8085_RST75_LINE = 3;

    public static final int I8085_NONE = 0;
    public static final int I8085_TRAP = 0x01;
    public static final int I8085_RST55 = 0x02;
    public static final int I8085_RST65 = 0x04;
    public static final int I8085_RST75 = 0x08;
    public static final int I8085_SID = 0x10;
    public static final int I8085_INTR = 0xff;

    /**
     * ************************************************************************
     * I8080 section
 *************************************************************************
     */
    public static final int I8080_INTR_LINE = I8085_INTR_LINE;
    public static final int I8080_TRAP = I8085_TRAP;
    public static final int I8080_INTR = I8085_INTR;
    public static final int I8080_NONE = I8085_NONE;

}
