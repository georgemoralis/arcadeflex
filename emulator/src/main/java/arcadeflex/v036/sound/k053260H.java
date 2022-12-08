/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

public class k053260H {

    public static class K053260_interface {

        public K053260_interface(int clock, int region, int[] mixing_level, TimerCallbackHandlerPtr irq) {
            this.clock = clock;
            this.region = region;
            this.mixing_level = mixing_level;
            this.irq = irq;
        }
        public int clock;/* clock */
        public int region;/* memory region of sample ROM(s) */
        public int[] mixing_level;/* volume */
        public TimerCallbackHandlerPtr irq;/* called on SH1 complete cycle ( clock / 32 ) */
    }
}
