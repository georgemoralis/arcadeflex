package mame;

public class timerH {

    public static double TIME_IN_HZ(double hz) { return 1.0 / hz; }
/*TODO*///#define TIME_IN_CYCLES(c,cpu) ((double)(c) * cycles_to_sec[cpu])
    public static double TIME_IN_SEC(double s)   { return s; }
    public static double TIME_IN_MSEC(double ms) { return ((double)(ms) * (1.0 / 1000.0)); }
    public static double TIME_IN_USEC(double us) { return ((double)(us) * (1.0 / 1000000.0)); }
    public static double TIME_IN_NSEC(double us) { return ((double)(us) * (1.0 / 1000000000.0)); }

    public static final double TIME_NOW = 0.0;
    public static final double TIME_NEVER = 1.0e30;

/*TODO*///#define TIME_TO_CYCLES(cpu,t) ((int)((t) * sec_to_cycles[cpu]))

    public static final int SUSPEND_REASON_HALT = 0x0001;
    public static final int SUSPEND_REASON_RESET = 0x0002;
    public static final int SUSPEND_REASON_SPIN = 0x0004;
    public static final int SUSPEND_REASON_TRIGGER = 0x0008;
    public static final int SUSPEND_REASON_DISABLE = 0x0010;
    public static final int SUSPEND_ANY_REASON = -1;

}
