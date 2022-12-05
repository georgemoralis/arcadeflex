/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package gr.codebb.arcadeflex.v037b7.sound;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static common.libc.cstdio.sprintf;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import gr.codebb.arcadeflex.v036.mame.driverH.ReadHandlerPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.sndintrf.snd_interface;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.sound_name;
import gr.codebb.arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_OKIM6295;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.sound.adpcmH.MAX_ADPCM;
import static gr.codebb.arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;



public class okim6295 extends snd_interface {

    public static final int MAX_SAMPLE_CHUNK = 10000;

    public static final int FRAC_BITS = 14;
    public static final int FRAC_ONE = (1 << FRAC_BITS);
    public static final int FRAC_MASK = (FRAC_ONE - 1);

    /* struct describing a single playing ADPCM voice */
    public static class ADPCMVoice {

        int stream;
        /* which stream are we playing on? */

        byte playing;
        /* 1 if we are actively playing */

        UBytePtr region_base;
        /* pointer to the base of the region */

        UBytePtr _base;
        /* pointer to the base memory location */

        int/*UINT32*/ sample;
        /* current sample number */

        int/*UINT32*/ count;
        /* total samples to play */

        int/*UINT32*/ signal;
        /* current ADPCM signal */

        int/*UINT32*/ step;
        /* current ADPCM step */

        int/*UINT32*/ volume;
        /* output volume */

        short last_sample;
        /* last sample output */

        short curr_sample;
        /* current sample target */

        int/*UINT32*/ source_step;
        /* step value for frequency conversion */

        int/*UINT32*/ source_pos;
        /* current fractional position */

    };
    /* array of ADPCM voices */
    static int/*UINT8*/ num_voices;
    static ADPCMVoice[] adpcm = new ADPCMVoice[MAX_ADPCM];
    /* step size index shift table */
    static int[] index_shift = {-1, -1, -1, -1, 2, 4, 6, 8};
    /* lookup table for the precomputed difference */
    static int[] diff_lookup = new int[49 * 16];
    /* volume lookup table */
    static /*UINT32*/ int[] volume_table = new int[16];

    static int[] okim6295_command = new int[MAX_OKIM6295];
    static int[][] okim6295_base = new int[MAX_OKIM6295][];

    public okim6295() {
        this.name = "OKI6295";
        this.sound_num = SOUND_OKIM6295;
        for (int i = 0; i < MAX_OKIM6295; i++) {
            okim6295_base[i] = new int[MAX_OKIM6295_VOICES];
        }
        for (int i = 0; i < MAX_ADPCM; i++) {
            adpcm[i] = new ADPCMVoice();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((OKIM6295interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((OKIM6295interface) msound.sound_interface).frequency[0];
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_sh_start -- start emulation of an OKIM6295-compatible chip
     *
     **********************************************************************************************
     */
    @Override
    public int start(MachineSound msound) {
        OKIM6295interface intf = (OKIM6295interface) msound.sound_interface;
        String stream_name;
        int i;

        /* reset the ADPCM system */
        num_voices = (intf.num * MAX_OKIM6295_VOICES);
        compute_tables();

        /* initialize the voices */
        //memset(adpcm, 0, sizeof(adpcm));
        for (i = 0; i < num_voices; i++) {
            int chip = i / MAX_OKIM6295_VOICES;
            int voice = i % MAX_OKIM6295_VOICES;

            /* reset the OKI-specific parameters */
            okim6295_command[chip] = -1;
            okim6295_base[chip][voice] = 0;
            /* generate the name and create the stream */
            stream_name = sprintf("%s #%d (voice %d)", sound_name(msound), chip, voice);
            adpcm[i].stream = stream_init(stream_name, intf.mixing_level[chip], Machine.sample_rate, i, adpcm_update);
            if (adpcm[i].stream == -1) {
                return 1;
            }

            /* initialize the rest of the structure */
            adpcm[i].region_base = memory_region(intf.region[chip]);
            adpcm[i].volume = 255;
            adpcm[i].signal = -2;
            if (Machine.sample_rate != 0) {
                adpcm[i].source_step = (/*UNIT32*/int) ((double) intf.frequency[chip] * (double) FRAC_ONE / (double) Machine.sample_rate);
            }
        }
        /* success */
        return 0;
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_sh_stop -- stop emulation of an OKIM6295-compatible chip
     *
     **********************************************************************************************
     */
    @Override
    public void stop() {
        //Not functionality neccesary
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_sh_update -- update emulation of an OKIM6295-compatible chip
     *
     **********************************************************************************************
     */
    @Override
    public void update() {
        //Not functionality neccesary
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_set_bank_base -- set the base of the bank for a given voice on a
     * given chip
     *
     **********************************************************************************************
     */
    public static void OKIM6295_set_bank_base(int which, int channel, int base) {
        /* handle the all voice case */
        if (channel == ALL_VOICES) {
            int i;

            for (i = 0; i < MAX_OKIM6295_VOICES; i++) {
                OKIM6295_set_bank_base(which, i, base);
            }
            return;
        }
        ADPCMVoice voice = adpcm[which * MAX_OKIM6295_VOICES + channel];
        /* update the stream and set the new base */
        stream_update(voice.stream, 0);
        okim6295_base[which][channel] = base;
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_set_frequency -- dynamically adjusts the frequency of a given
     * ADPCM voice
     *
     **********************************************************************************************
     */
    public static void OKIM6295_set_frequency(int which, int channel, int frequency) {

        /* handle the all voice case */
        if (channel == ALL_VOICES) {
            int i;

            for (i = 0; i < MAX_OKIM6295_VOICES; i++) {
                OKIM6295_set_frequency(which, i, frequency);
            }
            return;
        }

        /* update the stream and set the new base */
        ADPCMVoice voice = adpcm[which * MAX_OKIM6295_VOICES + channel];
        stream_update(voice.stream, 0);
        if (Machine.sample_rate != 0) {
            voice.source_step = (/*UNIT32*/int) ((double) frequency * (double) FRAC_ONE / (double) Machine.sample_rate);
        }
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_status_r -- read the status port of an OKIM6295-compatible chip
     *
     **********************************************************************************************
     */
    public static int OKIM6295_status_r(int num) {
        int i, result;

        /* range check the numbers */
        if (num >= num_voices / MAX_OKIM6295_VOICES) {
            logerror("error: OKIM6295_status_r() called with chip = %d, but only %d chips allocated\n", num, num_voices / MAX_OKIM6295_VOICES);
            return 0x0f;
        }

        /* set the bit to 1 if something is playing on a given channel */
        result = 0;
        for (i = 0; i < MAX_OKIM6295_VOICES; i++) {
            ADPCMVoice voice = adpcm[num * MAX_OKIM6295_VOICES + i];

            /* update the stream */
            stream_update(voice.stream, 0);

            /* set the bit if it's playing */
            if (voice.playing != 0) {
                result |= 1 << i;
            }
        }

        return result;
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_data_w -- write to the data port of an OKIM6295-compatible chip
     *
     **********************************************************************************************
     */
    static void OKIM6295_data_w(int num, int data) {
        /* range check the numbers */
        if (num >= num_voices / MAX_OKIM6295_VOICES) {
            logerror("error: OKIM6295_data_w() called with chip = %d, but only %d chips allocated\n", num, num_voices / MAX_OKIM6295_VOICES);
            return;
        }

        /* if a command is pending, process the second half */
        if (okim6295_command[num] != -1) {
            int temp = data >> 4, i, start, stop;
            UBytePtr _base;

            /* determine which voice(s) (voice is set by a 1 bit in the upper 4 bits of the second byte) */
            for (i = 0; i < MAX_OKIM6295_VOICES; i++, temp >>= 1) {
                if ((temp & 1) != 0) {
                    ADPCMVoice voice = adpcm[num * MAX_OKIM6295_VOICES + i];

                    /* update the stream */
                    stream_update(voice.stream, 0);
                    /* determine the start/stop positions */
                    _base = new UBytePtr(voice.region_base, okim6295_base[num][i] + okim6295_command[num] * 8);
                    start = (_base.read(0) << 16) + (_base.read(1) << 8) + _base.read(2);
                    stop = (_base.read(3) << 16) + (_base.read(4) << 8) + _base.read(5);
                    /* set up the voice to play this sample */
                    if (start < 0x40000 && stop < 0x40000) {
                        voice.playing = 1;
                        voice._base = new UBytePtr(voice.region_base, okim6295_base[num][i] + start);
                        voice.sample = 0;
                        voice.count = (int) (2 * (stop - start + 1));

                        /* also reset the ADPCM parameters */
                        voice.signal = -2;
                        voice.step = 0;
                        voice.volume = volume_table[data & 0x0f];
                    } /* invalid samples go here */ else {
                        logerror("OKIM6295: requested to play invalid sample %02x\n", okim6295_command[num]);
                        voice.playing = 0;
                    }
                }
            }

            /* reset the command */
            okim6295_command[num] = -1;
        } /* if this is the start of a command, remember the sample number for next time */ else if ((data & 0x80) != 0) {
            okim6295_command[num] = data & 0x7f;
        } /* otherwise, see if this is a silence command */ else {
            int temp = data >> 3, i;

            /* determine which voice(s) (voice is set by a 1 bit in bits 3-6 of the command */
            for (i = 0; i < 4; i++, temp >>= 1) {
                if ((temp & 1) != 0) {
                    ADPCMVoice voice = adpcm[num * MAX_OKIM6295_VOICES + i];

                    /* update the stream, then turn it off */
                    stream_update(voice.stream, 0);
                    voice.playing = 0;
                }
            }
        }
    }

    /**
     * ********************************************************************************************
     *
     * OKIM6295_status_0_r -- generic status read functions OKIM6295_status_1_r
     *
     **********************************************************************************************
     */
    public static ReadHandlerPtr OKIM6295_status_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OKIM6295_status_r(0);
        }
    };

    public static ReadHandlerPtr OKIM6295_status_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OKIM6295_status_r(0);
        }
    };
    /**
     * ********************************************************************************************
     *
     * OKIM6295_data_0_w -- generic data write functions OKIM6295_data_1_w
     *
     **********************************************************************************************
     */

    public static WriteHandlerPtr OKIM6295_data_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OKIM6295_data_w(0, data);
        }
    };

    public static WriteHandlerPtr OKIM6295_data_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            OKIM6295_data_w(1, data);
        }
    };

    //taken from adpcm.java
    static void compute_tables() {
        /* nibble to bit map */
        int[][] nbl2bit
                = {
                    new int[]{1, 0, 0, 0}, new int[]{1, 0, 0, 1}, new int[]{1, 0, 1, 0}, new int[]{1, 0, 1, 1},
                    new int[]{1, 1, 0, 0}, new int[]{1, 1, 0, 1}, new int[]{1, 1, 1, 0}, new int[]{1, 1, 1, 1},
                    new int[]{-1, 0, 0, 0}, new int[]{-1, 0, 0, 1}, new int[]{-1, 0, 1, 0}, new int[]{-1, 0, 1, 1},
                    new int[]{-1, 1, 0, 0}, new int[]{-1, 1, 0, 1}, new int[]{-1, 1, 1, 0}, new int[]{-1, 1, 1, 1}
                };
        /* loop over all possible steps */
        for (int step = 0; step <= 48; step++) {
            /* compute the step value */
            int stepval = (int) Math.floor(16.0 * Math.pow(11.0 / 10.0, (double) step));

            /* loop over all nibbles and compute the difference */
            for (int nib = 0; nib < 16; nib++) {
                diff_lookup[step * 16 + nib] = nbl2bit[nib][0]
                        * (stepval * nbl2bit[nib][1]
                        + stepval / 2 * nbl2bit[nib][2]
                        + stepval / 4 * nbl2bit[nib][3]
                        + stepval / 8);
            }
        }
        /* generate the OKI6295 volume table */
        for (int step = 0; step < 16; step++) {
            double out = 256.0;
            int vol = step;

            /* 3dB per step */
            while (vol-- > 0) {
                out /= 1.412537545;
                /* = 10 ^ (3/20) = 3dB */
            }
            volume_table[step] = (/*UINT32*/int) out;
        }
    }

    static void generate_adpcm(ADPCMVoice voice, ShortPtr buffer, int samples) {
        /* if this voice is active */
        if (voice.playing != 0) {
            UBytePtr _base = voice._base;
            int sample = (int) voice.sample;
            int signal = (int) voice.signal;
            int count = (int) voice.count;
            int step = (int) voice.step;
            int val;
            /* loop while we still have samples to generate */
            while (samples != 0) {
                /* compute the new amplitude and update the current step */
                val = _base.read(sample / 2) >> (((sample & 1) << 2) ^ 4);
                signal += diff_lookup[step * 16 + (val & 15)];

                /* clamp to the maximum */
                if (signal > 2047) {
                    signal = 2047;
                } else if (signal < -2048) {
                    signal = -2048;
                }

                /* adjust the step size and clamp */
                step += index_shift[val & 7];
                if (step > 48) {
                    step = 48;
                } else if (step < 0) {
                    step = 0;
                }
                /* output to the buffer, scaling by the volume */
                buffer.write(0, (short) (signal * voice.volume / 16));
                buffer.offset += 2;
                samples--;

                /* next! */
                if (++sample > count) {
                    voice.playing = 0;
                    break;
                }
            }
            /* update the parameters */
            voice.sample = sample;
            voice.signal = signal;
            voice.step = step;
        }

        /* fill the rest with silence */
        while (samples-- != 0) {
            buffer.write(0, (short) 0);
            buffer.offset += 2;
        }
    }
    public static StreamInitPtr adpcm_update = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            ADPCMVoice voice = adpcm[num];
            ShortPtr sample_data = new ShortPtr(MAX_SAMPLE_CHUNK * 2), curr_data = new ShortPtr(sample_data);
            short prev = voice.last_sample, curr = voice.curr_sample;
            int/*UINT32*/ final_pos;
            int/*UINT32*/ new_samples;
            /* finish off the current sample */
            if (voice.source_pos > 0) {
                /* interpolate */
                while (length > 0 && voice.source_pos < FRAC_ONE) {
                    buffer.write(0, (short) ((((int) prev * (FRAC_ONE - voice.source_pos)) + ((int) curr * voice.source_pos)) >> FRAC_BITS));
                    buffer.offset += 2;
                    voice.source_pos += voice.source_step;
                    length--;
                }

                /* if we're over, continue; otherwise, we're done */
                if (voice.source_pos >= FRAC_ONE) {
                    voice.source_pos -= FRAC_ONE;
                } else {
                    return;
                }
            }
            /* compute how many new samples we need */
            final_pos = (int) (voice.source_pos + length * voice.source_step);
            new_samples = (final_pos + FRAC_ONE - 1) >> FRAC_BITS;
            if (new_samples > MAX_SAMPLE_CHUNK) {
                new_samples = MAX_SAMPLE_CHUNK;
            }

            /* generate them into our buffer */
            generate_adpcm(voice, sample_data, (int) new_samples);
            prev = curr;
            curr = (short) curr_data.read(0);
            curr_data.offset += 2;

            /* then sample-rate convert with linear interpolation */
            while (length > 0) {
                /* interpolate */
                while (length > 0 && voice.source_pos < FRAC_ONE) {
                    buffer.write(0, (short) ((((int) prev * (FRAC_ONE - voice.source_pos)) + ((int) curr * voice.source_pos)) >> FRAC_BITS));
                    buffer.offset += 2;
                    voice.source_pos += voice.source_step;
                    length--;
                }

                /* if we're over, grab the next samples */
                if (voice.source_pos >= FRAC_ONE) {
                    voice.source_pos -= FRAC_ONE;
                    prev = curr;
                    curr = (short) curr_data.read(0);
                    curr_data.offset += 2;
                }
            }

            /* remember the last samples */
            voice.last_sample = prev;
            voice.curr_sample = curr;
        }
    };
}
