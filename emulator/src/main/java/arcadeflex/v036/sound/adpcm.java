/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.adpcmH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;

public class adpcm extends snd_interface {

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
    /* global pointer to the current array of samples */
    static ADPCMsample[] sample_list;
    /* step size index shift table */
    static int[] index_shift = {-1, -1, -1, -1, 2, 4, 6, 8};
    /* lookup table for the precomputed difference */
    static int[] diff_lookup = new int[49 * 16];
    /* volume lookup table */
    static /*UINT32*/ int[] volume_table = new int[16];

    public adpcm() {
        this.sound_num = SOUND_ADPCM;
        this.name = "ADPCM";
        for (int i = 0; i < MAX_ADPCM; i++) {
            adpcm[i] = new ADPCMVoice();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((ADPCMinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;//NO functionality expected
    }

    @Override
    public void reset() {
        //NO functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * compute_tables -- compute the difference tables
     *
     **********************************************************************************************
     */
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
        /* generate the volume table (currently just a guess) */
        for (int step = 0; step < 16; step++) {
            double out = 256.0;
            int vol = step;

            /* assume 2dB per step (most likely wrong!) */
            while (vol-- > 0) {
                out /= 1.258925412;
                /* = 10 ^ (2/20) = 2dB */

            }
            volume_table[step] = (/*UINT32*/int) out;
        }
    }

    /**
     * ********************************************************************************************
     *
     * generate_adpcm -- general ADPCM decoding routine
     *
     **********************************************************************************************
     */
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

    /**
     * ********************************************************************************************
     *
     * adpcm_update -- update the sound chip so that it is in sync with CPU
     * execution
     *
     **********************************************************************************************
     */
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

    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_start -- start emulation of several ADPCM output streams
     *
     **********************************************************************************************
     */
    @Override
    public int start(MachineSound msound) {
        ADPCMinterface intf = (ADPCMinterface) msound.sound_interface;
        String stream_name;

        /* reset the ADPCM system */
        num_voices = intf.num;
        compute_tables();
        sample_list = null;

        /* generate the sample table, if one is needed */
        if (intf.init != null) {
            /* allocate memory for it */
            sample_list = new ADPCMsample[257];
            if (sample_list == null) {
                return 1;
            }
            for (int i = 0; i < 257; i++) {
                sample_list[i] = new ADPCMsample();
            }

            /* callback to initialize */
            intf.init.handler(intf, sample_list, 256);
        }

        /* initialize the voices */
        //memset(adpcm, 0, sizeof(adpcm));
        for (int i = 0; i < num_voices; i++) {
            /* generate the name and create the stream */
            stream_name = sprintf("%s #%d", sound_name(msound), i);
            adpcm[i].stream = stream_init(stream_name, intf.mixing_level[i], Machine.sample_rate, i, adpcm_update);
            if (adpcm[i].stream == -1) {
                return 1;
            }

            /* initialize the rest of the structure */
            adpcm[i].region_base = memory_region(intf.region);
            adpcm[i].volume = 255;
            adpcm[i].signal = -2;
            if (Machine.sample_rate != 0) {
                adpcm[i].source_step = (/*UINT32*/int) ((double) intf.frequency * (double) FRAC_ONE / (double) Machine.sample_rate);
            }
        }

        /* success */
        return 0;
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_stop -- stop emulation of several ADPCM output streams
     *
     **********************************************************************************************
     */
    @Override
    public void stop() {
        /* free the temporary table if we created it */
        if (sample_list != null) {
            sample_list = null;
        }
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_sh_update -- update ADPCM streams
     *
     **********************************************************************************************
     */
    @Override
    public void update() {
        //NO functionality expected
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_trigger -- handle a write to the ADPCM data stream
     *
     **********************************************************************************************
     */
    public static WriteHandlerPtr ADPCM_trigger = new WriteHandlerPtr() {
        public void handler(int num, int which) {
            ADPCMVoice voice = adpcm[num];//struct ADPCMVoice *voice = &adpcm[num];
            //struct ADPCMsample *sample;

            /* bail if we're not playing anything */
            if (Machine.sample_rate == 0) {
                return;
            }

            /* range check the numbers */
            if (num >= num_voices) {
                if (errorlog != null) {
                    fprintf(errorlog, "error: ADPCM_trigger() called with channel = %d, but only %d channels allocated\n", num, num_voices);
                }
                return;
            }

            /* find a match */
            //for (sample = sample_list; sample->length > 0; sample++)
            for (ADPCMsample sample : sample_list) {
                if (sample.length > 0) {
                    if (sample.num == which) {
                        /* update the ADPCM voice */
                        stream_update(voice.stream, 0);

                        /* set up the voice to play this sample */
                        voice.playing = 1;
                        voice._base = new UBytePtr(voice.region_base, sample.offset);//&voice->region_base[sample->offset];
                        voice.sample = 0;
                        voice.count = sample.length;

                        /* also reset the ADPCM parameters */
                        voice.signal = -2;
                        voice.step = 0;
                        return;
                    }
                }
            }

            if (errorlog != null) {
                fprintf(errorlog, "warning: ADPCM_trigger() called with unknown trigger = %08x\n", which);
            }
        }
    };

    /**
     * ********************************************************************************************
     *
     * ADPCM_play -- play data from a specific offset for a specific length
     *
     **********************************************************************************************
     */
    public static void ADPCM_play(int num, int offset, int length) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            if (errorlog != null) {
                fprintf(errorlog, "error: ADPCM_trigger() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            }
            return;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);

        /* set up the voice to play this sample */
        voice.playing = 1;
        voice._base = new UBytePtr(voice.region_base, offset);
        voice.sample = 0;
        voice.count = length;

        /* also reset the ADPCM parameters */
        voice.signal = -2;
        voice.step = 0;
    }

    /*TODO*///
    /*TODO*///
    /*TODO*///
    /*TODO*////**********************************************************************************************
    /*TODO*///
    /*TODO*///     ADPCM_stop -- stop playback on an ADPCM data channel
    /*TODO*///
    /*TODO*///***********************************************************************************************/
    /*TODO*///
    /*TODO*///void ADPCM_stop(int num)
    /*TODO*///{
    /*TODO*///	struct ADPCMVoice *voice = &adpcm[num];
    /*TODO*///
    /*TODO*///	/* bail if we're not playing anything */
    /*TODO*///	if (Machine->sample_rate == 0)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* range check the numbers */
    /*TODO*///	if (num >= num_voices)
    /*TODO*///	{
    /*TODO*///		if (errorlog) fprintf(errorlog,"error: ADPCM_stop() called with channel = %d, but only %d channels allocated\n", num, num_voices);
    /*TODO*///		return;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the ADPCM voice */
    /*TODO*///	stream_update(voice->stream, 0);
    /*TODO*///
    /*TODO*///	/* stop playback */
    /*TODO*///	voice->playing = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///

    /**
     * ********************************************************************************************
     *
     * ADPCM_setvol -- change volume on an ADPCM data channel
     *
     **********************************************************************************************
     */
    public static void ADPCM_setvol(int num, int vol) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            if (errorlog != null) {
                fprintf(errorlog, "error: ADPCM_setvol() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            }
            return;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);
        voice.volume = vol;
    }

    /**
     * ********************************************************************************************
     *
     * ADPCM_playing -- returns true if an ADPCM data channel is still playing
     *
     **********************************************************************************************
     */
    public static int ADPCM_playing(int num) {
        ADPCMVoice voice = adpcm[num];

        /* bail if we're not playing anything */
        if (Machine.sample_rate == 0) {
            return 0;
        }

        /* range check the numbers */
        if (num >= num_voices) {
            if (errorlog != null) {
                fprintf(errorlog, "error: ADPCM_playing() called with channel = %d, but only %d channels allocated\n", num, num_voices);
            }
            return 0;
        }

        /* update the ADPCM voice */
        stream_update(voice.stream, 0);
        return voice.playing;
    }

}
