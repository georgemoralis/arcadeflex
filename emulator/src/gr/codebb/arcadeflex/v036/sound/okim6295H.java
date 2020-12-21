/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.sound;

/**
 *
 * @author shadow
 */
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
        int num;                /* total number of chips */

        int[] frequency;	/* playback frequency */

        int[] region;		/* memory region where the sample ROM lives */

        int[] mixing_level;	/* master volume */

    };

}
