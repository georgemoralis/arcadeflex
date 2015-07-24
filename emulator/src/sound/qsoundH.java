package sound;

public class qsoundH {

    public static final int QSOUND_CLOCK = 4000000;   /* default 4MHz clock */


    public static class QSound_interface {

        public QSound_interface(int clock, int region, int[] mixing_level) {
            this.clock = clock;
            this.region = region;
            this.mixing_level = mixing_level;
        }
        int clock;					/* clock */

        int region;					/* memory region of sample ROM(s) */

        int[] mixing_level;		/* volume */

    };

}
