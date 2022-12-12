/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

public class okim6295H {

    /* an interface for the OKIM6295 and similar chips */
    public static final int MAX_OKIM6295 = 2;
    public static final int MAX_OKIM6295_VOICES = 4;
    public static final int ALL_VOICES = -1;

    public static class OKIM6295interface {

        public OKIM6295interface(int num, int[] frequency, int[] region, int[] mixing_level) {
            this.num = num;
            this.frequency = frequency;
            this.region = region;
            this.mixing_level = mixing_level;
        }
        public int num;/* total number of chips */
        public int[] frequency;/* playback frequency */
        public int[] region;/* memory region where the sample ROM lives */
        public int[] mixing_level;/* master volume */

    };

}
