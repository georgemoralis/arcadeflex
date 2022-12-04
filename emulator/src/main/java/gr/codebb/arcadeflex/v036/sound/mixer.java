package gr.codebb.arcadeflex.v036.sound;

import gr.codebb.arcadeflex.common.PtrLib.BytePtr;
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;
import static gr.codebb.arcadeflex.v036.platform.sound.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;

public class mixer {

    /* enable this to turn off clipping (helpful to find cases where we max out */
    public static final boolean DISABLE_CLIPPING = false;

    /* accumulators have ACCUMULATOR_SAMPLES samples (must be a power of 2) */
    public static final int ACCUMULATOR_SAMPLES = 8192;
    public static final int ACCUMULATOR_MASK = (ACCUMULATOR_SAMPLES - 1);

    /* fractional numbers have FRACTION_BITS bits of resolution */
    public static final int FRACTION_BITS = 16;
    public static final int FRACTION_MASK = ((1 << FRACTION_BITS) - 1);

    static int mixer_sound_enabled;

    public static class mixer_channel_data {

        String name;

        /* current volume, gain and pan */
        int volume;
        int gain;
        int pan;

        /* mixing levels */
        char /*UINT8*/ mixing_level;
        char /*UINT8*/ default_mixing_level;
        char /*UINT8*/ config_mixing_level;
        char /*UINT8*/ config_default_mixing_level;

        /* current playback positions */
        int/*UINT32*/ input_frac;
        int/*UINT32*/ samples_available;
        int /*UINT32*/ frequency;
        int/*UINT32*/ step_size;

        /* state of non-streamed playback */
        boolean/*UINT8*/ is_stream;
        boolean/*UINT8*/ is_playing;
        boolean/*UINT8*/ is_looping;
        boolean/*UINT8*/ is_16bit;

        BytePtr data_start_b;
        ShortPtr data_start_s;
        int data_end;
        int data_current;
    };

    /* channel data */
    static mixer_channel_data[] mixer_channel = new mixer_channel_data[MIXER_MAX_CHANNELS];
    static /*UINT8*/ char[] config_mixing_level = new char[MIXER_MAX_CHANNELS];
    static /*UINT8*/ char[] config_default_mixing_level = new char[MIXER_MAX_CHANNELS];

    static /*UINT8*/ char first_free_channel = 0;
    static /*UINT8*/ char config_invalid;
    static boolean is_stereo;

    /* 32-bit accumulators */
    static int/*UINT32*/ accum_base;
    static int[] left_accum = new int[ACCUMULATOR_SAMPLES];
    static int[] right_accum = new int[ACCUMULATOR_SAMPLES];

    /* 16-bit mix buffers */
    static short[] mix_buffer = new short[ACCUMULATOR_SAMPLES * 2]; /* *2 for stereo */

    /* global sample tracking */
    public static int/*UINT32*/ samples_this_frame;

    /**
     * *************************************************************************
     * mixer_sh_start
     * *************************************************************************
     */
    public static int mixer_sh_start() {
    	//struct mixer_channel_data *channel;

        /* reset all channels to their defaults */
        //memset(&mixer_channel, 0, sizeof(mixer_channel));
        for (int i = 0; i < mixer_channel.length; i++) {
            mixer_channel[i] = new mixer_channel_data();
            mixer_channel[i].mixing_level = 0xff;
            mixer_channel[i].default_mixing_level = 0xff;
            mixer_channel[i].config_mixing_level = config_mixing_level[i];
            mixer_channel[i].config_default_mixing_level = config_default_mixing_level[i];
        }

        /* determine if we're playing in stereo or not */
        first_free_channel = 0;
        is_stereo = ((Machine.drv.sound_attributes & SOUND_SUPPORTS_STEREO) != 0);

        /* clear the accumulators */
        accum_base = 0;
        for (int i = 0; i < ACCUMULATOR_SAMPLES; i++) {
            left_accum[i] = 0;
            right_accum[i] = 0;
        }
        samples_this_frame = osd_start_audio_stream(is_stereo?1:0);

        int sound_enabled = 1;
        //disable sound in MainStream case for now..
        //if(MainStream.inst != null){
        //    sound_enabled = 0;
        //}
        mixer_sound_enabled = sound_enabled;

        return 0;
    }

    /**
     * *************************************************************************
     * mixer_sh_stop
     * *************************************************************************
     */
    public static void mixer_sh_stop() {
        osd_stop_audio_stream();
    }

    /**
     * *************************************************************************
     * mixer_update_channel
     * *************************************************************************
     */
    public static void mixer_update_channel(mixer_channel_data channel, int total_sample_count) {
        int samples_to_generate = (int) (total_sample_count - channel.samples_available);

        /* don't do anything for streaming channels */
        if (channel.is_stream) {
            return;
        }

        /* if we're all caught up, just return */
        if (samples_to_generate <= 0) {
            return;
        }

        /* if we're playing, mix in the data */
        if (channel.is_playing) {
            if (channel.is_16bit) {
                mix_sample_16(channel, samples_to_generate);
            } else {
                mix_sample_8(channel, samples_to_generate);
            }
        }

        /* just eat the rest */
        channel.samples_available += (int) samples_to_generate;
    }

    /**
     * *************************************************************************
     * mixer_sh_update
     * *************************************************************************
     */
    public static void mixer_sh_update() {
        int/*UINT32*/ accum_pos = accum_base;

        int sample;

        /* update all channels (for streams this is a no-op) */
        for (int i = 0; i < first_free_channel; i++) {
            mixer_update_channel(mixer_channel[i], (int) samples_this_frame);

            /* if we needed more than they could give, adjust their pointers */
            if (samples_this_frame > mixer_channel[i].samples_available) {
                mixer_channel[i].samples_available = 0;
            } else {
                mixer_channel[i].samples_available -= samples_this_frame;
            }
        }
        /* copy the mono 32-bit data to a 16-bit buffer, clipping along the way */
        if (!is_stereo) {
            int mix = 0;
            for (int i = 0; i < samples_this_frame; i++) {
                /* fetch and clip the sample */
                sample = left_accum[accum_pos];
//#if !DISABLE_CLIPPING
                if (sample < -32768) {
                    sample = -32768;
                } else if (sample > 32767) {
                    sample = 32767;
                }
//#endif

                /* store and zero out behind us */
                mix_buffer[mix++] = (short) sample;
                left_accum[accum_pos] = 0;

                /* advance to the next sample */
                accum_pos = (accum_pos + 1) & ACCUMULATOR_MASK;
            }
        } /* copy the stereo 32-bit data to a 16-bit buffer, clipping along the way */ else {
            int mix = 0;// mix_buffer;
            for (int i = 0; i < samples_this_frame; i++) {
                /* fetch and clip the left sample */
                sample = left_accum[accum_pos];
                //#if !DISABLE_CLIPPING
                if (sample < -32768) {
                    sample = -32768;
                } else if (sample > 32767) {
                    sample = 32767;
                }
    //#endif

                /* store and zero out behind us */
                mix_buffer[mix++] = (short) sample;
                left_accum[accum_pos] = 0;

                /* fetch and clip the right sample */
                sample = right_accum[accum_pos];
                //if !DISABLE_CLIPPING
                if (sample < -32768) {
                    sample = -32768;
                } else if (sample > 32767) {
                    sample = 32767;
                }
    //#endif

                /* store and zero out behind us */
                mix_buffer[mix++] = (short) sample;
                right_accum[accum_pos] = 0;

                /* advance to the next sample */
                accum_pos = (accum_pos + 1) & ACCUMULATOR_MASK;
            }
        }

        /* play the result */
        samples_this_frame = osd_update_audio_stream(mix_buffer);

        accum_base = accum_pos;

    }

    /**
     * *************************************************************************
     * mixer_allocate_channel
     * *************************************************************************
     */
    public static int mixer_allocate_channel(int default_mixing_level) {
        /* this is just a degenerate case of the multi-channel mixer allocate */
        return mixer_allocate_channels(1, new int[]{default_mixing_level});
    }

    /**
     * *************************************************************************
     * mixer_allocate_channels
     * *************************************************************************
     */
    public static int mixer_allocate_channels(int channels, int[] default_mixing_levels) {
        /* make sure we didn't overrun the number of available channels */
        if (first_free_channel + channels > MIXER_MAX_CHANNELS) {
            if (errorlog != null) {
                fprintf(errorlog, "Too many mixer channels (requested %d, available %d)\n", first_free_channel + channels, MIXER_MAX_CHANNELS);
            }
            throw new UnsupportedOperationException("Too many mixer channels");
        }
        /* loop over channels requested */
        for (int i = 0; i < channels; i++) {
            /* extract the basic data */
            mixer_channel[first_free_channel + i].default_mixing_level = (char) MIXER_GET_LEVEL(default_mixing_levels[i]);
            mixer_channel[first_free_channel + i].pan = MIXER_GET_PAN(default_mixing_levels[i]);
            mixer_channel[first_free_channel + i].gain = MIXER_GET_GAIN(default_mixing_levels[i]);
            mixer_channel[first_free_channel + i].volume = 100;

            /* backwards compatibility with old 0-255 volume range */
            if (mixer_channel[first_free_channel + i].default_mixing_level > 100) {
                mixer_channel[first_free_channel + i].default_mixing_level = (char) (mixer_channel[first_free_channel + i].default_mixing_level * 25 / 255);
            }

            /* attempt to load in the configuration data for this channel */
            mixer_channel[first_free_channel + i].mixing_level = mixer_channel[first_free_channel + i].default_mixing_level;
            if (config_invalid == 0) {
                /* if the defaults match, set the mixing level from the config */
                if (mixer_channel[first_free_channel + i].default_mixing_level == mixer_channel[first_free_channel + i].config_default_mixing_level) {
                    mixer_channel[first_free_channel + i].mixing_level = mixer_channel[first_free_channel + i].config_mixing_level;
                } /* otherwise, invalidate all channels that have been created so far */ else {
                    config_invalid = 1;
                    for (int j = 0; j < first_free_channel + i; j++) {
                        mixer_set_mixing_level(j, mixer_channel[j].default_mixing_level);
                    }
                }
            }

            /* set the default name */
            mixer_set_name(first_free_channel + i, null);
        }

        first_free_channel += (char) channels;
        return first_free_channel - channels;
    }

    /**
     * *************************************************************************
     * mixer_set_name
     * *************************************************************************
     */
    public static void mixer_set_name(int ch, String name) {
        /* either copy the name or create a default one */
        if (name != null) {
            mixer_channel[ch].name = name;
        } else {
            mixer_channel[ch].name = sprintf("<channel #%d>", ch);
        }

        /* append left/right onto the channel as appropriate */
        if (mixer_channel[ch].pan == MIXER_PAN_LEFT) {
            mixer_channel[ch].name += " (Lt)";
        } else if (mixer_channel[ch].pan == MIXER_PAN_RIGHT) {
            mixer_channel[ch].name += " (Rt)";
        }
    }

    /**
     * *************************************************************************
     * mixer_get_name
     * *************************************************************************
     */
    public static String mixer_get_name(int ch) {
        //struct mixer_channel_data *channel = &mixer_channel[ch];

        /* return a pointer to the name or a NULL for an unused channel */
        if (mixer_channel[ch].name != null) {
            return mixer_channel[ch].name;
        } else {
            return null;
        }
    }

    /**
     * *************************************************************************
     * mixer_set_volume
     * *************************************************************************
     */
    public static void mixer_set_volume(int ch, int volume) {
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));
        mixer_channel[ch].volume = volume;
    }

    /**
     * *************************************************************************
     * mixer_set_mixing_level
     * *************************************************************************
     */
    public static void mixer_set_mixing_level(int ch, int level) {
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));
        mixer_channel[ch].mixing_level = (char) level;
    }

    /*TODO*////***************************************************************************
    /*TODO*///	mixer_get_mixing_level
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_get_mixing_level(int ch)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///	return channel->mixing_level;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_get_default_mixing_level
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_get_default_mixing_level(int ch)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///	return channel->default_mixing_level;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_read_config
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_read_config(void *f)
    /*TODO*///{
    /*TODO*///	UINT8 default_levels[MIXER_MAX_CHANNELS];
    /*TODO*///	UINT8 mixing_levels[MIXER_MAX_CHANNELS];
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	memset(default_levels, 0xff, sizeof(default_levels));
    /*TODO*///	memset(mixing_levels, 0xff, sizeof(mixing_levels));
    /*TODO*///	osd_fread(f, default_levels, MIXER_MAX_CHANNELS);
    /*TODO*///	osd_fread(f, mixing_levels, MIXER_MAX_CHANNELS);
    /*TODO*///	for (i = 0; i < MIXER_MAX_CHANNELS; i++)
    /*TODO*///	{
    /*TODO*///		config_default_mixing_level[i] = default_levels[i];
    /*TODO*///		config_mixing_level[i] = mixing_levels[i];
    /*TODO*///	}
    /*TODO*///	config_invalid = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_write_config
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_write_config(void *f)
    /*TODO*///{
    /*TODO*///	UINT8 default_levels[MIXER_MAX_CHANNELS];
    /*TODO*///	UINT8 mixing_levels[MIXER_MAX_CHANNELS];
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///	for (i = 0; i < MIXER_MAX_CHANNELS; i++)
    /*TODO*///	{
    /*TODO*///		default_levels[i] = mixer_channel[i].default_mixing_level;
    /*TODO*///		mixing_levels[i] = mixer_channel[i].mixing_level;
    /*TODO*///	}
    /*TODO*///	osd_fwrite(f, default_levels, MIXER_MAX_CHANNELS);
    /*TODO*///	osd_fwrite(f, mixing_levels, MIXER_MAX_CHANNELS);
    /*TODO*///}
    /*TODO*///
    /**
     * *************************************************************************
     * mixer_play_streamed_sample_16
     * *************************************************************************
     */
    public static void mixer_play_streamed_sample_16(int ch, ShortPtr data, int len, int freq) {

        int/*UINT32*/ step_size, input_pos, output_pos, samples_mixed;
        int mixing_volume;

        /* skip if sound is off */
        if (Machine.sample_rate == 0) {
            return;
        }
        mixer_channel[ch].is_stream = true;

        /* compute the overall mixing volume */
        if (mixer_sound_enabled != 0) {
            mixing_volume = ((mixer_channel[ch].volume * mixer_channel[ch].mixing_level * 256) << mixer_channel[ch].gain) / (100 * 100);
        } else {
            mixing_volume = 0;
        }
        /* compute the step size for sample rate conversion */
        if (freq != mixer_channel[ch].frequency) {
            /*RECHECK*/ mixer_channel[ch].frequency = /*uint32)*/ (int) freq;
            /*RECHECK*/ mixer_channel[ch].step_size = (/*UINT32*/int) ((double) freq * (double) (1 << FRACTION_BITS) / (double) Machine.sample_rate);
        }
        step_size = mixer_channel[ch].step_size;

        /* now determine where to mix it */
        input_pos = mixer_channel[ch].input_frac;
        output_pos = (accum_base + mixer_channel[ch].samples_available) & ACCUMULATOR_MASK;

        /* compute the length in fractional form */
        len = (len / 2) << FRACTION_BITS;
        samples_mixed = 0;

        /* if we're mono or left panning, just mix to the left channel */
        if (!is_stereo || mixer_channel[ch].pan == MIXER_PAN_LEFT) {
            while (input_pos < len) {
                left_accum[output_pos] += ((short) data.read((int) (input_pos >> FRACTION_BITS)) * mixing_volume) >> 8;
                input_pos += step_size;
                output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                samples_mixed++;
            }
        } /* if we're right panning, just mix to the right channel */ else if (mixer_channel[ch].pan == MIXER_PAN_RIGHT) {
            while (input_pos < len) {
                right_accum[output_pos] += ((short) data.read((int) (input_pos >> FRACTION_BITS)) * mixing_volume) >> 8;
                input_pos += step_size;
                output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                samples_mixed++;
            }
        } /* if we're stereo center, mix to both channels */ else {
            while (input_pos < len) {
                int mixing_value = ((short) data.read((int) (input_pos >> FRACTION_BITS)) * mixing_volume) >> 8;
                left_accum[output_pos] += mixing_value;
                right_accum[output_pos] += mixing_value;
                input_pos += step_size;
                output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                samples_mixed++;
            }
        }
        /* update the final positions */
        mixer_channel[ch].input_frac = input_pos & FRACTION_MASK;
        mixer_channel[ch].samples_available += samples_mixed;
    }

    /**
     * *************************************************************************
     * mixer_samples_this_frame
    **************************************************************************
     */
    public static int mixer_samples_this_frame() {
        return samples_this_frame;
    }

    /**
     * *************************************************************************
     * mixer_need_samples_this_frame
     * *************************************************************************
     */
    public static final int EXTRA_SAMPLES = 1;// safety margin for sampling rate conversion

    public static int mixer_need_samples_this_frame(int channel, int freq) {
        return (int) (samples_this_frame - mixer_channel[channel].samples_available + EXTRA_SAMPLES) * freq / Machine.sample_rate;
    }

    /**
     * *************************************************************************
     * mixer_play_sample
    **************************************************************************
     */
    public static void mixer_play_sample(int ch, BytePtr data, int len, int freq, int loop) {
        if(loop==1)
        {
            mixer_play_sample(ch, data, len, freq, true);
        }
        else
        {
            mixer_play_sample(ch, data, len, freq, false);
        }
    }
    public static void mixer_play_sample(int ch, BytePtr data, int len, int freq, boolean loop) {
    	//struct mixer_channel_data *channel = &mixer_channel[ch];

        /* skip if sound is off, or if this channel is a stream */
        if (Machine.sample_rate == 0 || mixer_channel[ch].is_stream) {
            return;
        }

        /* update the state of this channel */
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));

        /* compute the step size for sample rate conversion */
        if (freq != mixer_channel[ch].frequency) {
            mixer_channel[ch].frequency = freq;
            mixer_channel[ch].step_size = (/*UINT32*/int) ((double) freq * (double) (1 << FRACTION_BITS) / (double) Machine.sample_rate);
        }

        /* now determine where to mix it */
        mixer_channel[ch].input_frac = 0;
        mixer_channel[ch].data_start_b = data;
        mixer_channel[ch].data_current = 0;
        mixer_channel[ch].data_end = /*(UINT8 *)data +*/ len;
        mixer_channel[ch].is_playing = true;
        mixer_channel[ch].is_looping = loop;
        mixer_channel[ch].is_16bit = false;
    }

    /**
     * ************************************************************************
     * mixer_play_sample_16
     * *************************************************************************
     */
    public static void mixer_play_sample_16(int ch, ShortPtr data, int len, int freq, boolean loop) {
        /* skip if sound is off, or if this channel is a stream */
        if (Machine.sample_rate == 0 || mixer_channel[ch].is_stream) {
            return;
        }

        /* update the state of this channel */
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));

        /* compute the step size for sample rate conversion */
        if (freq != mixer_channel[ch].frequency) {
            mixer_channel[ch].frequency = (int) freq;
            mixer_channel[ch].step_size = (int) ((double) freq * (double) (1 << FRACTION_BITS) / (double) Machine.sample_rate);
        }

        /* now determine where to mix it */
        mixer_channel[ch].input_frac = 0;
        mixer_channel[ch].data_start_s = data;
        mixer_channel[ch].data_current = 0;//data;
        mixer_channel[ch].data_end = /*(UINT8 *)data*/ len;
        mixer_channel[ch].is_playing = true;
        mixer_channel[ch].is_looping = loop;
        mixer_channel[ch].is_16bit = true;
    }

    /**
     * ************************************************************************
     * mixer_stop_sample
     * *************************************************************************
     */

    public static void mixer_stop_sample(int ch) {
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));
        mixer_channel[ch].is_playing = false;
    }

    /**
     * ************************************************************************
     * mixer_is_sample_playing
     * *************************************************************************
     */
    public static boolean mixer_is_sample_playing(int ch) {
        mixer_update_channel(mixer_channel[ch], sound_scalebufferpos((int) samples_this_frame));
        return mixer_channel[ch].is_playing;
    }
    
    
    /***************************************************************************
    	mixer_set_sample_frequency
    ***************************************************************************/
    
    public static void mixer_set_sample_frequency(int ch, int freq)
    {
    	//struct mixer_channel_data *channel = &mixer_channel[ch];
    
    	mixer_update_channel(mixer_channel[ch], sound_scalebufferpos(samples_this_frame));
    
    	/* compute the step size for sample rate conversion */
    	if (freq != mixer_channel[ch].frequency)
    	{
    		mixer_channel[ch].frequency = freq;
    		mixer_channel[ch].step_size = (int)((double)freq * (double)(1 << FRACTION_BITS) / (double)Machine.sample_rate);
    	}
    }
    
    
    /***************************************************************************
    	mixer_sound_enable_global_w
    ***************************************************************************/
    
    public static void mixer_sound_enable_global_w(int enable)
    {
    	int i;
        //struct mixer_channel_data *channel;

        /* update all channels (for streams this is a no-op) */
        for (i = 0; i < first_free_channel; i++) {
            mixer_update_channel(mixer_channel[i], sound_scalebufferpos(samples_this_frame));
        }

        mixer_sound_enabled = enable;
    }
    

    /**
     * *************************************************************************
     * mix_sample_8
    **************************************************************************
     */
    public static void mix_sample_8(mixer_channel_data channel, int samples_to_generate) {
        int/*UINT32*/ step_size, input_frac, output_pos;
        BytePtr source;
        int source_end;
        int mixing_volume;

        /* compute the overall mixing volume */
        if (mixer_sound_enabled != 0) {
            mixing_volume = ((channel.volume * channel.mixing_level * 256) << channel.gain) / (100 * 100);
        } else {
            mixing_volume = 0;
        }

        /* get the initial state */
        step_size = channel.step_size;
        source = new BytePtr(channel.data_start_b, channel.data_current);//source = channel->data_current;
        source_end = channel.data_end;
        input_frac = channel.input_frac;
        output_pos = (accum_base + channel.samples_available) & ACCUMULATOR_MASK;

        /* an outer loop to handle looping samples */
        while (samples_to_generate > 0) {
            /* if we're mono or left panning, just mix to the left channel */
            if (!is_stereo || channel.pan == MIXER_PAN_LEFT) {
                while (source.offset < source_end && samples_to_generate > 0) {
                    left_accum[output_pos] += source.read(0) * mixing_volume;
                    input_frac += step_size;
                    source.offset += (int) (input_frac >> FRACTION_BITS);
                    input_frac &= FRACTION_MASK;
                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            } /* if we're right panning, just mix to the right channel */ else if (channel.pan == MIXER_PAN_RIGHT) {
                while (source.offset < source_end && samples_to_generate > 0) {
                    right_accum[output_pos] += source.read(0) * mixing_volume;
                    input_frac += step_size;
                    source.offset += (int) (input_frac >> FRACTION_BITS);
                    input_frac &= FRACTION_MASK;
                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            } /* if we're stereo center, mix to both channels */ else {
                while (source.offset < source_end && samples_to_generate > 0) {
                    int mixing_value = source.read(0) * mixing_volume;
                    left_accum[output_pos] += mixing_value;
                    right_accum[output_pos] += mixing_value;
                    input_frac += step_size;
                    source.offset += (int) (input_frac >> FRACTION_BITS);
                    input_frac &= FRACTION_MASK;
                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            }

            /* handle the end case */
            if (source.offset >= source_end) {
                /* if we're done, stop playing */
                if (!channel.is_looping) {
                    channel.is_playing = false;
                    break;
                } /* if we're looping, wrap to the beginning */ else {
                    source.offset -= source_end;// -(INT8*)channel.data_start;
                }
            }
        }

        /* update the final positions */
        channel.input_frac = input_frac;
        channel.data_current = source.offset;
    }

    /**
     * ************************************************************************
     * mix_sample_16
     * *************************************************************************
     */
    static void mix_sample_16(mixer_channel_data channel, int samples_to_generate) {
        int step_size, input_frac, output_pos;
        ShortPtr source;
        int source_end;
        int mixing_volume;

        if (mixer_sound_enabled != 0) {
            mixing_volume = ((channel.volume * channel.mixing_level * 256) << channel.gain) / (100 * 100);
        } else {
            mixing_volume = 0;
        }

        /* get the initial state */
        step_size = channel.step_size;
        source = new ShortPtr(channel.data_start_s, channel.data_current);
        source_end = channel.data_end;
        input_frac = channel.input_frac;
        output_pos = (accum_base + channel.samples_available) & ACCUMULATOR_MASK;

        /* an outer loop to handle looping samples */
        while (samples_to_generate > 0) {
            /* if we're mono or left panning, just mix to the left channel */
            if (!is_stereo || channel.pan == MIXER_PAN_LEFT) {
                while (source.offset < source_end && samples_to_generate > 0) {
                    left_accum[output_pos] += (source.read() * mixing_volume) >> 8;

                    input_frac += step_size;
                    source.inc((int) (input_frac >> FRACTION_BITS));
                    input_frac &= FRACTION_MASK;

                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            } /* if we're right panning, just mix to the right channel */ else if (channel.pan == MIXER_PAN_RIGHT) {
                while (source.offset < source_end && samples_to_generate > 0) {
                    right_accum[output_pos] += (source.read() * mixing_volume) >> 8;

                    input_frac += step_size;
                    source.inc((int) (input_frac >> FRACTION_BITS));
                    input_frac &= FRACTION_MASK;

                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            } /* if we're stereo center, mix to both channels */ else {
                while (source.offset < source_end && samples_to_generate > 0) {
                    int mixing_value = (source.read() * mixing_volume) >> 8;
                    left_accum[output_pos] += mixing_value;
                    right_accum[output_pos] += mixing_value;

                    input_frac += step_size;
                    source.inc((int) (input_frac >> FRACTION_BITS));
                    input_frac &= FRACTION_MASK;

                    output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
                    samples_to_generate--;
                }
            }
            /* handle the end case */
            if (source.offset >= source_end) {
                /* if we're done, stop playing */
                if (!channel.is_looping) {
                    channel.is_playing = false;
                    break;
                } /* if we're looping, wrap to the beginning */ else {
                    source.offset -= source_end;// source.offset -= (INT16*)source_end - (INT16*)channel.data_start;
                }
            }
        }

        /* update the final positions */
        channel.input_frac = input_frac;
        channel.data_current = source.offset;
    }

}
