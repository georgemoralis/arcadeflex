/*
 * ported to 0.36
 */
package arcadeflex.v036.cpu.s2650;

public class s2650H {

    public static final int S2650_PC = 1;
    public static final int S2650_PS = 2;
    public static final int S2650_R0 = 3;
    public static final int S2650_R1 = 4;
    public static final int S2650_R2 = 5;
    public static final int S2650_R3 = 6;
    public static final int S2650_R1A = 7;
    public static final int S2650_R2A = 8;
    public static final int S2650_R3A = 9;
    public static final int S2650_HALT = 10;
    public static final int S2650_IRQ_STATE = 11;
    public static final int S2650_SI = 12;
    public static final int S2650_FO = 13;

    public static final int S2650_INT_NONE = 0;
    public static final int S2650_INT_IRQ = 1;

    /* fake control port   M/~IO=0 D/~C=0 E/~NE=0 */
    public static final int S2650_CTRL_PORT = 0x100;

    /* fake data port      M/~IO=0 D/~C=1 E/~NE=0 */
    public static final int S2650_DATA_PORT = 0x101;

    /* extended i/o ports  M/~IO=0 D/~C=x E/~NE=1 */
    public static final int S2650_EXT_PORT = 0xff;
}
