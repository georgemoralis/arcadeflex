package gr.codebb.arcadeflex.v036.machine;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

public class tait8741H {

    public static final int MAX_TAITO8741 = 4;

    /* NEC 8741 program mode */
    public static final int TAITO8741_MASTER = 0;
    public static final int TAITO8741_SLAVE = 1;
    public static final int TAITO8741_PORT = 2;

    public static class TAITO8741interface {

        public TAITO8741interface(int num, int[] mode, int[] serial_connect, ReadHandlerPtr[] portHandler_r) {
            this.num = num;
            this.mode = mode;
            this.serial_connect = serial_connect;
            this.portHandler_r = portHandler_r;
        }
        int num;
        int[] mode;//[MAX_TAITO8741];            /* program select */
        int[] serial_connect;//[MAX_TAITO8741];	/* serial port connection */
        ReadHandlerPtr[] portHandler_r; //int (*portHandler_r[MAX_TAITO8741])(int offset); /* parallel port handler */
    };
}
