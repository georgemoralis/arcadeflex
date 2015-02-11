package sound;

public class upd7759H {

    public static final int MAX_UPD7759 = 2;

    /* There are two modes for the uPD7759, selected through the !MD pin.
     This is the mode select input.  High is stand alone, low is slave.
     We're making the assumption that nobody switches modes through
     software. */
    public static final int UPD7759_STANDALONE_MODE = 1;
    public static final int UPD7759_SLAVE_MODE = 0;

    public static final int UPD7759_STANDARD_CLOCK = 640000;

    public static abstract interface irqcallbackPtr {

        public abstract void handler(int param);
    }

    public static class UPD7759_interface {

        public UPD7759_interface(int num, int clock_rate, int[] volume, int[] region, int mode, irqcallbackPtr[] irqcallback) {
            this.num = num;
            this.clock_rate = clock_rate;
            this.volume = volume;
            this.region = region;
            this.mode = mode;
            this.irqcallback = irqcallback;
        }
        int num;		/* num of upd chips */
        int clock_rate;
        int[] volume;
        int[] region; 	/* memory region from which the samples came */
        int mode;		/* standalone or slave mode */
        irqcallbackPtr[] irqcallback;	/* for slave mode only */

    };
}
