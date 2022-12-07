/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
package arcadeflex.v036.sndhrdw;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.sound.samplesH.*;
import static gr.codebb.arcadeflex.v036.sound.samples.*;

public class astrof {

    /* Make sure that the sample name definitions in drivers/astrof.c matches these */
    public static final int SAMPLE_FIRE = 0;
    public static final int SAMPLE_EKILLED = 1;
    public static final int SAMPLE_WAVE = 2;
    public static final int SAMPLE_BOSSFIRE = 6;
    public static final int SAMPLE_FUEL = 7;
    public static final int SAMPLE_DEATH = 8;
    public static final int SAMPLE_BOSSHIT = 9;
    public static final int SAMPLE_BOSSKILL = 10;

    public static final int CHANNEL_FIRE = 0;
    public static final int CHANNEL_EXPLOSION = 1;
    public static final int CHANNEL_WAVE = 2;
    /* Background humm */

    public static final int CHANNEL_BOSSFIRE = 2;
    /* There is no background humm on the boss level */

    public static final int CHANNEL_FUEL = 3;

    /* Make sure that the public static final int's in sndhrdw/astrof.c matches these */
    static String astrof_sample_names[]
            = {
                "*astrof",
                "fire.wav",
                "ekilled.wav",
                "wave1.wav",
                "wave2.wav",
                "wave3.wav",
                "wave4.wav",
                "bossfire.wav",
                "fuel.wav",
                "death.wav",
                "bosshit.wav",
                "bosskill.wav",
                null /* end of array */};

    public static Samplesinterface astrof_samples_interface = new Samplesinterface(
            4, /* 4 channels */
            25, /* volume */
            astrof_sample_names
    );

    static String tomahawk_sample_names[]
            = {
                "*tomahawk",
                /* We don't have these yet */
                null /* end of array */};

    public static Samplesinterface tomahawk_samples_interface = new Samplesinterface(
            1, /* 1 channel for now */
            25, /* volume */
            tomahawk_sample_names
    );

    static int start_explosion = 0;
    static int death_playing = 0;
    static int bosskill_playing = 0;
    static int last_a = 0;
    public static WriteHandlerPtr astrof_sample1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (death_playing != 0) {
                death_playing = sample_playing(CHANNEL_EXPLOSION);
            }

            if (bosskill_playing != 0) {
                bosskill_playing = sample_playing(CHANNEL_EXPLOSION);
            }

            /* Bit 2 - Explosion trigger */
            if ((data & 0x04) != 0 && (last_a & 0x04) == 0) {
                /* I *know* that the effect select port will be written shortly
                 after this one, so this works */
                start_explosion = 1;
            }

            /* Bit 0/1/3 - Background noise */
            if ((data & 0x08) != (last_a & 0x08)) {
                if ((data & 0x08) != 0) {
                    int sample = SAMPLE_WAVE + (data & 3);
                    sample_start(CHANNEL_WAVE, sample, 1);
                } else {
                    sample_stop(CHANNEL_WAVE);
                }
            }

            /* Bit 4 - Boss Laser */
            if ((data & 0x10) != 0 && (last_a & 0x10) == 0) {
                if (bosskill_playing == 0) {
                    sample_start(CHANNEL_BOSSFIRE, SAMPLE_BOSSFIRE, 0);
                }
            }

            /* Bit 5 - Fire */
            if ((data & 0x20) != 0 && (last_a & 0x20) == 0) {
                if (bosskill_playing == 0) {
                    sample_start(CHANNEL_FIRE, SAMPLE_FIRE, 0);
                }
            }

            /* Bit 6 - Don't know. Probably something to do with the explosion sounds */
 /* Bit 7 - Don't know. Maybe a global sound enable bit? */
            last_a = data;
        }
    };

    static int last_b = 0;
    public static WriteHandlerPtr astrof_sample2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            /* Bit 0-2 Explosion select (triggered by Bit 2 of the other port */
            if (start_explosion != 0) {
                if ((data & 0x04) != 0) {
                    /* This is really a compound effect, made up of I believe 3 sound
                     effects, but since our sample contains them all, disable playing
                     the other effects while the explosion is playing */
                    if (bosskill_playing == 0) {
                        sample_start(CHANNEL_EXPLOSION, SAMPLE_BOSSKILL, 0);
                        bosskill_playing = 1;
                    }
                } else if ((data & 0x02) != 0) {
                    sample_start(CHANNEL_EXPLOSION, SAMPLE_BOSSHIT, 0);
                } else if ((data & 0x01) != 0) {
                    sample_start(CHANNEL_EXPLOSION, SAMPLE_EKILLED, 0);
                } else {
                    if (death_playing == 0) {
                        sample_start(CHANNEL_EXPLOSION, SAMPLE_DEATH, 0);
                        death_playing = 1;
                    }
                }

                start_explosion = 0;
            }

            /* Bit 3 - Low Fuel Warning */
            if ((data & 0x08) != 0 && (last_b & 0x08) == 0) {
                sample_start(CHANNEL_FUEL, SAMPLE_FUEL, 0);
            }

            last_b = data;
        }
    };

}
