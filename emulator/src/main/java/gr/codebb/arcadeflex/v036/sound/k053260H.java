package gr.codebb.arcadeflex.v036.sound;

import gr.codebb.arcadeflex.v037b7.mame.timer;

/**
 * *******************************************************
 *
 * Konami 053260 PCM/ADPCM Sound Chip
 *
 ********************************************************
 */

public class k053260H {


    public static class K053260_interface {

        public K053260_interface(int clock, int region, int[] mixing_level, timer.timer_callback irq) {
            this.clock = clock;
            this.region = region;
            this.mixing_level = mixing_level;
            this.irq = irq;
        }
        int clock;					/* clock */

        int region;					/* memory region of sample ROM(s) */

        int[] mixing_level;		/* volume */

        timer.timer_callback irq;	/* called on SH1 complete cycle ( clock / 32 ) */

    };
}
