
package sound;

import static mame.mame.*;
import static sound.mixerH.*;
import static mame.driverH.*;
import static arcadeflex.sound.*;

public class mixer {
    /* enable this to turn off clipping (helpful to find cases where we max out */
    public static final int DISABLE_CLIPPING		=0;
    
    /* accumulators have ACCUMULATOR_SAMPLES samples (must be a power of 2) */
    public static final int ACCUMULATOR_SAMPLES		=8192;
    public static final int ACCUMULATOR_MASK		=(ACCUMULATOR_SAMPLES - 1);
    
    /* fractional numbers have FRACTION_BITS bits of resolution */
    public static final int FRACTION_BITS			=16;
    public static final int FRACTION_MASK			=((1 << FRACTION_BITS) - 1);
    
    
    static int mixer_sound_enabled;
    
    
    /* holds all the data for the a mixer channel */
    public static class mixer_channel_data
    {
    /*TODO*///	char		name[40];
    /*TODO*///
    /*TODO*///	/* current volume, gain and pan */
    /*TODO*///	INT32		volume;
    /*TODO*///	INT32		gain;
    /*TODO*///	INT32		pan;
    /*TODO*///
    /*TODO*///	/* mixing levels */
    	char /*UINT8*/		mixing_level;
    	char /*UINT8*/		default_mixing_level;
    	char /*UINT8*/		config_mixing_level;
    	char /*UINT8*/		config_default_mixing_level;
    /*TODO*///
    /*TODO*///	/* current playback positions */
    /*TODO*///	UINT32		input_frac;
    /*TODO*///	UINT32		samples_available;
    /*TODO*///	UINT32		frequency;
    /*TODO*///	UINT32		step_size;
    /*TODO*///
    /*TODO*///	/* state of non-streamed playback */
    /*TODO*///	UINT8		is_stream;
    /*TODO*///	UINT8		is_playing;
    /*TODO*///	UINT8		is_looping;
    /*TODO*///	UINT8		is_16bit;
    /*TODO*///	void *		data_start;
    /*TODO*///	void *		data_end;
    /*TODO*///	void *		data_current;
    };
    
    
    
    /* channel data */
    static mixer_channel_data[] mixer_channel = new mixer_channel_data[MIXER_MAX_CHANNELS];
    static /*UINT8*/char[] config_mixing_level = new char[MIXER_MAX_CHANNELS];
    static /*UINT8*/char[] config_default_mixing_level = new char[MIXER_MAX_CHANNELS];

    static /*UINT8*/char first_free_channel = 0;
    static /*UINT8*/char config_invalid;
    static boolean is_stereo;
    
    /* 32-bit accumulators */
    static int/*UINT32*/ accum_base;
    static int[] left_accum = new int[ACCUMULATOR_SAMPLES];
    static int[] right_accum = new int[ACCUMULATOR_SAMPLES];
        
    
    /* 16-bit mix buffers */
    static short[] mix_buffer = new short[ACCUMULATOR_SAMPLES * 2]; /* *2 for stereo */

    /* global sample tracking */
    static int/*UINT32*/ samples_this_frame;
        
    /***************************************************************************
    	mixer_sh_start
    ***************************************************************************/
    
    public static int mixer_sh_start()
    {    
    	/* reset all channels to their defaults */
        for (int i = 0; i < mixer_channel.length; i++)
        {
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
        for (int i = 0; i < ACCUMULATOR_SAMPLES; i++)
        {
            left_accum[i] = 0;
            right_accum[i] = 0;
        }
    
    	samples_this_frame = osd_start_audio_stream(is_stereo);
    
    	mixer_sound_enabled = 1;
    
    	return 0;
    }
    
    
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_sh_stop
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_sh_stop(void)
    /*TODO*///{
    /*TODO*///	osd_stop_audio_stream();
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_update_channel
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_update_channel(struct mixer_channel_data *channel, int total_sample_count)
    /*TODO*///{
    /*TODO*///	int samples_to_generate = total_sample_count - channel->samples_available;
    /*TODO*///
    /*TODO*///	/* don't do anything for streaming channels */
    /*TODO*///	if (channel->is_stream)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* if we're all caught up, just return */
    /*TODO*///	if (samples_to_generate <= 0)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* if we're playing, mix in the data */
    /*TODO*///	if (channel->is_playing)
    /*TODO*///	{
    /*TODO*///		if (channel->is_16bit)
    /*TODO*///			mix_sample_16(channel, samples_to_generate);
    /*TODO*///		else
    /*TODO*///			mix_sample_8(channel, samples_to_generate);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* just eat the rest */
    /*TODO*///	channel->samples_available += samples_to_generate;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_sh_update
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_sh_update(void)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *	channel;
    /*TODO*///	UINT32 accum_pos = accum_base;
    /*TODO*///	INT16 *mix;
    /*TODO*///	INT32 sample;
    /*TODO*///	int	i;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_MIXER);
    /*TODO*///
    /*TODO*///	/* update all channels (for streams this is a no-op) */
    /*TODO*///	for (i = 0, channel = mixer_channel; i < first_free_channel; i++, channel++)
    /*TODO*///	{
    /*TODO*///		mixer_update_channel(channel, samples_this_frame);
    /*TODO*///
    /*TODO*///		/* if we needed more than they could give, adjust their pointers */
    /*TODO*///		if (samples_this_frame > channel->samples_available)
    /*TODO*///			channel->samples_available = 0;
    /*TODO*///		else
    /*TODO*///			channel->samples_available -= samples_this_frame;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* copy the mono 32-bit data to a 16-bit buffer, clipping along the way */
    /*TODO*///	if (!is_stereo)
    /*TODO*///	{
    /*TODO*///		mix = mix_buffer;
    /*TODO*///		for (i = 0; i < samples_this_frame; i++)
    /*TODO*///		{
    /*TODO*///			/* fetch and clip the sample */
    /*TODO*///			sample = left_accum[accum_pos];
    /*TODO*///#if !DISABLE_CLIPPING
    /*TODO*///			if (sample < -32768)
    /*TODO*///				sample = -32768;
    /*TODO*///			else if (sample > 32767)
    /*TODO*///				sample = 32767;
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///			/* store and zero out behind us */
    /*TODO*///			*mix++ = sample;
    /*TODO*///			left_accum[accum_pos] = 0;
    /*TODO*///
    /*TODO*///			/* advance to the next sample */
    /*TODO*///			accum_pos = (accum_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* copy the stereo 32-bit data to a 16-bit buffer, clipping along the way */
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		mix = mix_buffer;
    /*TODO*///		for (i = 0; i < samples_this_frame; i++)
    /*TODO*///		{
    /*TODO*///			/* fetch and clip the left sample */
    /*TODO*///			sample = left_accum[accum_pos];
    /*TODO*///#if !DISABLE_CLIPPING
    /*TODO*///			if (sample < -32768)
    /*TODO*///				sample = -32768;
    /*TODO*///			else if (sample > 32767)
    /*TODO*///				sample = 32767;
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///			/* store and zero out behind us */
    /*TODO*///			*mix++ = sample;
    /*TODO*///			left_accum[accum_pos] = 0;
    /*TODO*///
    /*TODO*///			/* fetch and clip the right sample */
    /*TODO*///			sample = right_accum[accum_pos];
    /*TODO*///#if !DISABLE_CLIPPING
    /*TODO*///			if (sample < -32768)
    /*TODO*///				sample = -32768;
    /*TODO*///			else if (sample > 32767)
    /*TODO*///				sample = 32767;
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///			/* store and zero out behind us */
    /*TODO*///			*mix++ = sample;
    /*TODO*///			right_accum[accum_pos] = 0;
    /*TODO*///
    /*TODO*///			/* advance to the next sample */
    /*TODO*///			accum_pos = (accum_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* play the result */
    /*TODO*///	samples_this_frame = osd_update_audio_stream(mix_buffer);
    /*TODO*///
    /*TODO*///	accum_base = accum_pos;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_END);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_allocate_channel
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_allocate_channel(int default_mixing_level)
    /*TODO*///{
    /*TODO*///	/* this is just a degenerate case of the multi-channel mixer allocate */
    /*TODO*///	return mixer_allocate_channels(1, &default_mixing_level);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_allocate_channels
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_allocate_channels(int channels, const int *default_mixing_levels)
    /*TODO*///{
    /*TODO*///	int i, j;
    /*TODO*///
    /*TODO*///	/* make sure we didn't overrun the number of available channels */
    /*TODO*///	if (first_free_channel + channels > MIXER_MAX_CHANNELS)
    /*TODO*///	{
    /*TODO*///		if (errorlog)
    /*TODO*///			fprintf(errorlog, "Too many mixer channels (requested %d, available %d)\n", first_free_channel + channels, MIXER_MAX_CHANNELS);
    /*TODO*///		exit(1);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* loop over channels requested */
    /*TODO*///	for (i = 0; i < channels; i++)
    /*TODO*///	{
    /*TODO*///		struct mixer_channel_data *channel = &mixer_channel[first_free_channel + i];
    /*TODO*///
    /*TODO*///		/* extract the basic data */
    /*TODO*///		channel->default_mixing_level 	= MIXER_GET_LEVEL(default_mixing_levels[i]);
    /*TODO*///		channel->pan 					= MIXER_GET_PAN(default_mixing_levels[i]);
    /*TODO*///		channel->gain 					= MIXER_GET_GAIN(default_mixing_levels[i]);
    /*TODO*///		channel->volume 				= 100;
    /*TODO*///
    /*TODO*///		/* backwards compatibility with old 0-255 volume range */
    /*TODO*///		if (channel->default_mixing_level > 100)
    /*TODO*///			channel->default_mixing_level = channel->default_mixing_level * 25 / 255;
    /*TODO*///
    /*TODO*///		/* attempt to load in the configuration data for this channel */
    /*TODO*///		channel->mixing_level = channel->default_mixing_level;
    /*TODO*///		if (!config_invalid)
    /*TODO*///		{
    /*TODO*///			/* if the defaults match, set the mixing level from the config */
    /*TODO*///			if (channel->default_mixing_level == channel->config_default_mixing_level)
    /*TODO*///				channel->mixing_level = channel->config_mixing_level;
    /*TODO*///
    /*TODO*///			/* otherwise, invalidate all channels that have been created so far */
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				config_invalid = 1;
    /*TODO*///				for (j = 0; j < first_free_channel + i; j++)
    /*TODO*///					mixer_set_mixing_level(j, mixer_channel[j].default_mixing_level);
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* set the default name */
    /*TODO*///		mixer_set_name(first_free_channel + i, 0);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* increment the counter and return the first one */
    /*TODO*///	first_free_channel += channels;
    /*TODO*///	return first_free_channel - channels;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_set_name
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_set_name(int ch, const char *name)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	/* either copy the name or create a default one */
    /*TODO*///	if (name != NULL)
    /*TODO*///		strcpy(channel->name, name);
    /*TODO*///	else
    /*TODO*///		sprintf(channel->name, "<channel #%d>", ch);
    /*TODO*///
    /*TODO*///	/* append left/right onto the channel as appropriate */
    /*TODO*///	if (channel->pan == MIXER_PAN_LEFT)
    /*TODO*///		strcat(channel->name, " (Lt)");
    /*TODO*///	else if (channel->pan == MIXER_PAN_RIGHT)
    /*TODO*///		strcat(channel->name, " (Rt)");
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_get_name
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///const char *mixer_get_name(int ch)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	/* return a pointer to the name or a NULL for an unused channel */
    /*TODO*///	if (channel->name[0] != 0)
    /*TODO*///		return channel->name;
    /*TODO*///	else
    /*TODO*///		return NULL;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_set_volume
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_set_volume(int ch, int volume)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///	channel->volume = volume;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_set_mixing_level
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_set_mixing_level(int ch, int level)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///	channel->mixing_level = level;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
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
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_play_streamed_sample_16
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_play_streamed_sample_16(int ch, INT16 *data, int len, int freq)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///	UINT32 step_size, input_pos, output_pos, samples_mixed;
    /*TODO*///	INT32 mixing_volume;
    /*TODO*///
    /*TODO*///	/* skip if sound is off */
    /*TODO*///	if (Machine->sample_rate == 0)
    /*TODO*///		return;
    /*TODO*///	channel->is_stream = 1;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_MIXER);
    /*TODO*///
    /*TODO*///	/* compute the overall mixing volume */
    /*TODO*///	if (mixer_sound_enabled)
    /*TODO*///		mixing_volume = ((channel->volume * channel->mixing_level * 256) << channel->gain) / (100*100);
    /*TODO*///	else
    /*TODO*///		mixing_volume = 0;
    /*TODO*///
    /*TODO*///	/* compute the step size for sample rate conversion */
    /*TODO*///	if (freq != channel->frequency)
    /*TODO*///	{
    /*TODO*///		channel->frequency = freq;
    /*TODO*///		channel->step_size = (UINT32)((double)freq * (double)(1 << FRACTION_BITS) / (double)Machine->sample_rate);
    /*TODO*///	}
    /*TODO*///	step_size = channel->step_size;
    /*TODO*///
    /*TODO*///	/* now determine where to mix it */
    /*TODO*///	input_pos = channel->input_frac;
    /*TODO*///	output_pos = (accum_base + channel->samples_available) & ACCUMULATOR_MASK;
    /*TODO*///
    /*TODO*///	/* compute the length in fractional form */
    /*TODO*///	len = (len / 2) << FRACTION_BITS;
    /*TODO*///	samples_mixed = 0;
    /*TODO*///
    /*TODO*///	/* if we're mono or left panning, just mix to the left channel */
    /*TODO*///	if (!is_stereo || channel->pan == MIXER_PAN_LEFT)
    /*TODO*///	{
    /*TODO*///		while (input_pos < len)
    /*TODO*///		{
    /*TODO*///			left_accum[output_pos] += (data[input_pos >> FRACTION_BITS] * mixing_volume) >> 8;
    /*TODO*///			input_pos += step_size;
    /*TODO*///			output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///			samples_mixed++;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* if we're right panning, just mix to the right channel */
    /*TODO*///	else if (channel->pan == MIXER_PAN_RIGHT)
    /*TODO*///	{
    /*TODO*///		while (input_pos < len)
    /*TODO*///		{
    /*TODO*///			right_accum[output_pos] += (data[input_pos >> FRACTION_BITS] * mixing_volume) >> 8;
    /*TODO*///			input_pos += step_size;
    /*TODO*///			output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///			samples_mixed++;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* if we're stereo center, mix to both channels */
    /*TODO*///	else
    /*TODO*///	{
    /*TODO*///		while (input_pos < len)
    /*TODO*///		{
    /*TODO*///			INT32 mixing_value = (data[input_pos >> FRACTION_BITS] * mixing_volume) >> 8;
    /*TODO*///			left_accum[output_pos] += mixing_value;
    /*TODO*///			right_accum[output_pos] += mixing_value;
    /*TODO*///			input_pos += step_size;
    /*TODO*///			output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///			samples_mixed++;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the final positions */
    /*TODO*///	channel->input_frac = input_pos & FRACTION_MASK;
    /*TODO*///	channel->samples_available += samples_mixed;
    /*TODO*///
    /*TODO*///	profiler_mark(PROFILER_END);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_samples_this_frame
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_samples_this_frame(void)
    /*TODO*///{
    /*TODO*///	return samples_this_frame;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_need_samples_this_frame
    /*TODO*///***************************************************************************/
    /*TODO*///#define EXTRA_SAMPLES 1    // safety margin for sampling rate conversion
    /*TODO*///int mixer_need_samples_this_frame(int channel,int freq)
    /*TODO*///{
    /*TODO*///	return (samples_this_frame - mixer_channel[channel].samples_available + EXTRA_SAMPLES)
    /*TODO*///			* freq / Machine->sample_rate;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_play_sample
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_play_sample(int ch, INT8 *data, int len, int freq, int loop)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	/* skip if sound is off, or if this channel is a stream */
    /*TODO*///	if (Machine->sample_rate == 0 || channel->is_stream)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* update the state of this channel */
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///
    /*TODO*///	/* compute the step size for sample rate conversion */
    /*TODO*///	if (freq != channel->frequency)
    /*TODO*///	{
    /*TODO*///		channel->frequency = freq;
    /*TODO*///		channel->step_size = (UINT32)((double)freq * (double)(1 << FRACTION_BITS) / (double)Machine->sample_rate);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* now determine where to mix it */
    /*TODO*///	channel->input_frac = 0;
    /*TODO*///	channel->data_start = data;
    /*TODO*///	channel->data_current = data;
    /*TODO*///	channel->data_end = (UINT8 *)data + len;
    /*TODO*///	channel->is_playing = 1;
    /*TODO*///	channel->is_looping = loop;
    /*TODO*///	channel->is_16bit = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_play_sample_16
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_play_sample_16(int ch, INT16 *data, int len, int freq, int loop)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	/* skip if sound is off, or if this channel is a stream */
    /*TODO*///	if (Machine->sample_rate == 0 || channel->is_stream)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* update the state of this channel */
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///
    /*TODO*///	/* compute the step size for sample rate conversion */
    /*TODO*///	if (freq != channel->frequency)
    /*TODO*///	{
    /*TODO*///		channel->frequency = freq;
    /*TODO*///		channel->step_size = (UINT32)((double)freq * (double)(1 << FRACTION_BITS) / (double)Machine->sample_rate);
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* now determine where to mix it */
    /*TODO*///	channel->input_frac = 0;
    /*TODO*///	channel->data_start = data;
    /*TODO*///	channel->data_current = data;
    /*TODO*///	channel->data_end = (UINT8 *)data + len;
    /*TODO*///	channel->is_playing = 1;
    /*TODO*///	channel->is_looping = loop;
    /*TODO*///	channel->is_16bit = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_stop_sample
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_stop_sample(int ch)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///	channel->is_playing = 0;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_is_sample_playing
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///int mixer_is_sample_playing(int ch)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///	return channel->is_playing;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_set_sample_frequency
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_set_sample_frequency(int ch, int freq)
    /*TODO*///{
    /*TODO*///	struct mixer_channel_data *channel = &mixer_channel[ch];
    /*TODO*///
    /*TODO*///	mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///
    /*TODO*///	/* compute the step size for sample rate conversion */
    /*TODO*///	if (freq != channel->frequency)
    /*TODO*///	{
    /*TODO*///		channel->frequency = freq;
    /*TODO*///		channel->step_size = (UINT32)((double)freq * (double)(1 << FRACTION_BITS) / (double)Machine->sample_rate);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mixer_sound_enable_global_w
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mixer_sound_enable_global_w(int enable)
    /*TODO*///{
    /*TODO*///	int i;
    /*TODO*///	struct mixer_channel_data *channel;
    /*TODO*///
    /*TODO*///	/* update all channels (for streams this is a no-op) */
    /*TODO*///	for (i = 0, channel = mixer_channel; i < first_free_channel; i++, channel++)
    /*TODO*///	{
    /*TODO*///		mixer_update_channel(channel, sound_scalebufferpos(samples_this_frame));
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	mixer_sound_enabled = enable;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mix_sample_8
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mix_sample_8(struct mixer_channel_data *channel, int samples_to_generate)
    /*TODO*///{
    /*TODO*///	UINT32 step_size, input_frac, output_pos;
    /*TODO*///	INT8 *source, *source_end;
    /*TODO*///	INT32 mixing_volume;
    /*TODO*///
    /*TODO*///	/* compute the overall mixing volume */
    /*TODO*///	if (mixer_sound_enabled)
    /*TODO*///		mixing_volume = ((channel->volume * channel->mixing_level * 256) << channel->gain) / (100*100);
    /*TODO*///	else
    /*TODO*///		mixing_volume = 0;
    /*TODO*///
    /*TODO*///	/* get the initial state */
    /*TODO*///	step_size = channel->step_size;
    /*TODO*///	source = channel->data_current;
    /*TODO*///	source_end = channel->data_end;
    /*TODO*///	input_frac = channel->input_frac;
    /*TODO*///	output_pos = (accum_base + channel->samples_available) & ACCUMULATOR_MASK;
    /*TODO*///
    /*TODO*///	/* an outer loop to handle looping samples */
    /*TODO*///	while (samples_to_generate > 0)
    /*TODO*///	{
    /*TODO*///		/* if we're mono or left panning, just mix to the left channel */
    /*TODO*///		if (!is_stereo || channel->pan == MIXER_PAN_LEFT)
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				left_accum[output_pos] += *source * mixing_volume;
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* if we're right panning, just mix to the right channel */
    /*TODO*///		else if (channel->pan == MIXER_PAN_RIGHT)
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				right_accum[output_pos] += *source * mixing_volume;
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* if we're stereo center, mix to both channels */
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				INT32 mixing_value = *source * mixing_volume;
    /*TODO*///				left_accum[output_pos] += mixing_value;
    /*TODO*///				right_accum[output_pos] += mixing_value;
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* handle the end case */
    /*TODO*///		if (source >= source_end)
    /*TODO*///		{
    /*TODO*///			/* if we're done, stop playing */
    /*TODO*///			if (!channel->is_looping)
    /*TODO*///			{
    /*TODO*///				channel->is_playing = 0;
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			/* if we're looping, wrap to the beginning */
    /*TODO*///			else
    /*TODO*///				source -= (INT8 *)source_end - (INT8 *)channel->data_start;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the final positions */
    /*TODO*///	channel->input_frac = input_frac;
    /*TODO*///	channel->data_current = source;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///	mix_sample_16
    /*TODO*///***************************************************************************/
    /*TODO*///
    /*TODO*///void mix_sample_16(struct mixer_channel_data *channel, int samples_to_generate)
    /*TODO*///{
    /*TODO*///	UINT32 step_size, input_frac, output_pos;
    /*TODO*///	INT16 *source, *source_end;
    /*TODO*///	INT32 mixing_volume;
    /*TODO*///
    /*TODO*///	/* compute the overall mixing volume */
    /*TODO*///	if (mixer_sound_enabled)
    /*TODO*///		mixing_volume = ((channel->volume * channel->mixing_level * 256) << channel->gain) / (100*100);
    /*TODO*///	else
    /*TODO*///		mixing_volume = 0;
    /*TODO*///
    /*TODO*///	/* get the initial state */
    /*TODO*///	step_size = channel->step_size;
    /*TODO*///	source = channel->data_current;
    /*TODO*///	source_end = channel->data_end;
    /*TODO*///	input_frac = channel->input_frac;
    /*TODO*///	output_pos = (accum_base + channel->samples_available) & ACCUMULATOR_MASK;
    /*TODO*///
    /*TODO*///	/* an outer loop to handle looping samples */
    /*TODO*///	while (samples_to_generate > 0)
    /*TODO*///	{
    /*TODO*///		/* if we're mono or left panning, just mix to the left channel */
    /*TODO*///		if (!is_stereo || channel->pan == MIXER_PAN_LEFT)
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				left_accum[output_pos] += (*source * mixing_volume) >> 8;
    /*TODO*///
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* if we're right panning, just mix to the right channel */
    /*TODO*///		else if (channel->pan == MIXER_PAN_RIGHT)
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				right_accum[output_pos] += (*source * mixing_volume) >> 8;
    /*TODO*///
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* if we're stereo center, mix to both channels */
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			while (source < source_end && samples_to_generate > 0)
    /*TODO*///			{
    /*TODO*///				INT32 mixing_value = (*source * mixing_volume) >> 8;
    /*TODO*///				left_accum[output_pos] += mixing_value;
    /*TODO*///				right_accum[output_pos] += mixing_value;
    /*TODO*///
    /*TODO*///				input_frac += step_size;
    /*TODO*///				source += input_frac >> FRACTION_BITS;
    /*TODO*///				input_frac &= FRACTION_MASK;
    /*TODO*///
    /*TODO*///				output_pos = (output_pos + 1) & ACCUMULATOR_MASK;
    /*TODO*///				samples_to_generate--;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		/* handle the end case */
    /*TODO*///		if (source >= source_end)
    /*TODO*///		{
    /*TODO*///			/* if we're done, stop playing */
    /*TODO*///			if (!channel->is_looping)
    /*TODO*///			{
    /*TODO*///				channel->is_playing = 0;
    /*TODO*///				break;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			/* if we're looping, wrap to the beginning */
    /*TODO*///			else
    /*TODO*///				source -= (INT16 *)source_end - (INT16 *)channel->data_start;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	/* update the final positions */
    /*TODO*///	channel->input_frac = input_frac;
    /*TODO*///	channel->data_current = source;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///    
}
