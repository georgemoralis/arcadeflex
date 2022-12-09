/*
 * ported to v0.36
 */
package arcadeflex.v036.sndhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//common imports
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.sound.samples.sample_playing;
import static arcadeflex.v036.sound.samples.sample_set_freq;
import static arcadeflex.v036.sound.samples.sample_start;
import static arcadeflex.v036.machine.berzerk.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class berzerk {

    /* Note: We have the ability to replace the real death sound (33) and the
   * real voices with sample (34) as no good death sample could be obtained.
     */
    // #define FAKE_DEATH_SOUND

    /* volume and channel controls */
    public static final int VOICE_VOLUME = 100;
    public static final int SOUND_VOLUME = 80;
    public static final int SOUND_CHANNEL = 1;
    /* channel for sound effects */
    public static final int VOICE_CHANNEL = 5;
    /* special channel for voice sounds only */
    public static final int DEATH_CHANNEL = 6;
    /* special channel for fake death sound only */

 /* berzerk sound */
    static int lastfreq = 0;
    static int lastnoise = 0;
    static int lastvoice = 0;
    static int lastdata = 0;

    /* sound controls */
    static int berzerknoisemulate = 0;
    static int samplereset = 0;
    static int voicevolume = 100;
    static int samplefrequency = 22320;
    static int deathsound = 0;
    /* trigger for playing collision sound */
    static int nextdata5 = -1;

    public static ShStartHandlerPtr berzerk_sh_start = new ShStartHandlerPtr() {
        public int handler(MachineSound msound) {
            int i;

            berzerknoisemulate = 1;

            if (Machine.samples != null) {
                for (i = 0; i < 5; i++) {
                    if (Machine.samples.sample[i] != null) {
                        berzerknoisemulate = 0;
                    }
                }
            }
            return 0;
        }
    };

    public static WriteHandlerPtr berzerk_sound_control_a_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int noise = 0;
            int voice = 0;
            int voicefrequency = 0;

            /* Throw out all the non-need sound info. (for the moment) */
            if (offset < 3) {
                return;
            }

            /* Decode message for voice samples */
            if (offset == 4) {
                if ((data & 0x40) == 0) {
                    voice = data;
                    berzerkplayvoice = 0;
                } else {
                    /* We should use the volume and frequency on the voice samples */
                    voicevolume = ((data & 0x38) >> 3);
                    if (voicevolume > 0) {
                        voicevolume = 255;
                    }

                    voicefrequency = (data & 0x07);
                    switch (voicefrequency) {
                        case 0:
                            samplefrequency = 17640;
                            break;
                        case 1:
                            samplefrequency = 19404;
                            break;
                        case 2:
                            samplefrequency = 20947;
                            break;
                        case 3:
                            samplefrequency = 22050;
                            break;
                        case 4:
                            samplefrequency = 26019;
                            break;
                        case 5:
                            samplefrequency = 27783;
                            break;
                        case 6:
                            samplefrequency = 31250;
                            break;
                        case 7:
                            samplefrequency = 34700;
                            break;
                        default:
                            samplefrequency = 22050;
                            break;
                    }
                    return;
                }
            }

            /* Check for player fire sample reset */
            if ((offset == 3) || (offset == 5)) {
                if (lastnoise == 70) {
                    if ((offset == 3) && (data == 172)) {
                        nextdata5 = 25;
                        return;
                    }

                    if (offset == 5) {
                        if (data == nextdata5) {
                            deathsound = 2;
                            /* trigger death sound */
                            lastnoise = 64;
                            /* reset death sound */
                        }

                        nextdata5 = -1;
                    }

                    return;
                }

                if (lastnoise == 69) {
                    if ((offset == 3) && (data == 50)) {
                        nextdata5 = 50;
                        return;
                    }

                    if (offset == 5) {
                        if (data == nextdata5) {
                            lastnoise = 64;
                            /* reset laser sound */
                        }

                        nextdata5 = -1;
                    }
                }

                return;
            }

            /* Check to see what game sound should be playing */
            if ((data > 60) && (data < 72) && (offset == 6)) {
                noise = data;
            } else {
                noise = lastnoise;
            }

            /* One day I'll do this... */
            if (berzerknoisemulate != 0) {
                return;
                /*if (voicefrequency != 1750*(4-noise))
            {
            	voicefrequency = 1750*(4-noise);
            	voicevolume = 85*noise;
            }

            if (noise != 0)
            {
            	sample_set_freq(2,voicefrequency);
            	sample_set_volume(2,voicevolume);
            }
            else
            {
            	sample_set_volume(2,0);
            	voicevolume = 0;
            }*/
            } else {
                if ((offset == 6) && (lastnoise != noise)) /* Game sound effects */ {
                    switch (noise) {
                        case 69:
                            /* shot sample */
                            sample_start(1, 30, 0);
                            break;
                        case 70:
                            /* baddie laser */
                            if (errorlog != null) {
                                fprintf(errorlog, "Trying death sound");
                            }
                            switch (deathsound) {
                                case 1:
                                    sample_start(2, 33, 0);
                                    deathsound = 0;
                                    break;
                                case 2:
                                    sample_start(DEATH_CHANNEL, 34, 0);
                                    deathsound = 3;
                                    break;
                                case 0:
                                    sample_start(2, 31, 0);
                                    break;
                            }
                            break;
                        case 71:
                            /* kill baddie */
                            sample_start(3, 32, 0);
                            break;
                        default:
                            break;
                    }
                }
                lastnoise = noise;
                /* make sure we only play the sound once */

                if (offset == 4) /* Game voice sounds */ {
                    if (deathsound < 2) /* no voices for real death sound */ {
                        if (lastvoice == 24 && voice == 27) {
                            lastvoice = voice;
                            /* catch for the 'chicken ayye' problem */
                            return;
                        }
                        sample_start(VOICE_CHANNEL, voice, 0);
                        sample_set_freq(VOICE_CHANNEL, samplefrequency);
                        lastvoice = voice;
                    }
                }
            }
            /* End of berzerknoisemulate */
        }
    };

    public static WriteHandlerPtr berzerk_sound_control_b_w
            = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("B Data value %d and offset %d at %d\n", data, offset, lastfreq);
        }
    };

    public static ShUpdateHandlerPtr berzerk_sh_update = new ShUpdateHandlerPtr() {
        public void handler() {
            berzerkplayvoice = NOT(sample_playing(VOICE_CHANNEL));
            if (deathsound == 3 && sample_playing(DEATH_CHANNEL) == 0 && lastnoise != 70) {
                deathsound = 0;
                /* reset death sound */
            }
        }
    };
}
