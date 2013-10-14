//TODO
package sound;

/**
 *
 * @author shadow
 */
import static arcadeflex.libc_old.*;
import static sound.mixerH.*;
import static sound.mixer.*;

public class streams {
    
    public static abstract interface StreamInitPtr { public abstract void handler(int param,CharPtr buffer,int length); }

    /*TODO*///#define BUFFER_LEN 16384
    /*TODO*///
    /*TODO*///#define SAMPLES_THIS_FRAME(channel) \
    /*TODO*///	mixer_need_samples_this_frame((channel),stream_sample_rate[(channel)])
    /*TODO*///
    static int[] stream_joined_channels=new int[MIXER_MAX_CHANNELS];
    static CharPtr[] stream_buffer=new CharPtr[MIXER_MAX_CHANNELS];
    /*TODO*///static int stream_sample_rate[MIXER_MAX_CHANNELS];
    /*TODO*///static int stream_buffer_pos[MIXER_MAX_CHANNELS];
    /*TODO*///static int stream_sample_length[MIXER_MAX_CHANNELS];	/* in usec */
    /*TODO*///static int stream_param[MIXER_MAX_CHANNELS];
    /*TODO*///static void (*stream_callback[MIXER_MAX_CHANNELS])(int param,INT16 *buffer,int length);
    /*TODO*///static void (*stream_callback_multi[MIXER_MAX_CHANNELS])(int param,INT16 **buffer,int length);
    /*TODO*///
    /*TODO*///static int memory[MIXER_MAX_CHANNELS];
    /*TODO*///static int r1[MIXER_MAX_CHANNELS];
    /*TODO*///static int r2[MIXER_MAX_CHANNELS];
    /*TODO*///static int r3[MIXER_MAX_CHANNELS];
    /*TODO*///static int c[MIXER_MAX_CHANNELS];
    /*TODO*///
    /*TODO*////*
    /*TODO*///signal >--R1--+--R2--+
    /*TODO*///              |      |
    /*TODO*///              C      R3---> amp
    /*TODO*///              |      |
    /*TODO*///             GND    GND
    /*TODO*///*/
    /*TODO*///
    /*TODO*////* R1, R2, R3 in Ohm; C in pF */
    /*TODO*////* set C = 0 to disable the filter */
    public static void set_RC_filter(int channel,int R1,int R2,int R3,int C)
    {
    /*TODO*///	r1[channel] = R1;
    /*TODO*///	r2[channel] = R2;
    /*TODO*///	r3[channel] = R3;
    /*TODO*///	c[channel] = C;
    }
    /*TODO*///
    /*TODO*///void apply_RC_filter(int channel,INT16 *buf,int len,int sample_rate)
    /*TODO*///{
    /*TODO*///	float R1,R2,R3,C;
    /*TODO*///	float Req;
    /*TODO*///	int K;
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (c[channel] == 0) return;	/* filter disabled */
    /*TODO*///
    /*TODO*///	R1 = r1[channel];
    /*TODO*///	R2 = r2[channel];
    /*TODO*///	R3 = r3[channel];
    /*TODO*///	C = (float)c[channel] * 1E-12;	/* convert pF to F */
    /*TODO*///
    /*TODO*///	/* Cut Frequency = 1/(2*Pi*Req*C) */
    /*TODO*///
    /*TODO*///	Req = (R1*(R2+R3))/(R1+R2+R3);
    /*TODO*///
    /*TODO*///	K = 0x10000 * exp(-1 / (Req * C) / sample_rate);
    /*TODO*///
    /*TODO*///	buf[0] = buf[0] + (memory[channel] - buf[0]) * K / 0x10000;
    /*TODO*///
    /*TODO*///	for (i = 1;i < len;i++)
    /*TODO*///		buf[i] = buf[i] + (buf[i-1] - buf[i]) * K / 0x10000;
    /*TODO*///
    /*TODO*///	memory[channel] = buf[len-1];
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    
    public static int streams_sh_start()
    {
    	int i;
    
    
    	for (i = 0;i < MIXER_MAX_CHANNELS;i++)
    	{
    		stream_joined_channels[i] = 1;
    		stream_buffer[i] = null;
    	}
    
    	return 0;
    }
    
    
    public static void streams_sh_stop()
    {
    /*TODO*///	int i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	for (i = 0;i < MIXER_MAX_CHANNELS;i++)
    /*TODO*///	{
    /*TODO*///		free(stream_buffer[i]);
    /*TODO*///		stream_buffer[i] = 0;
    /*TODO*///	}
    }

    
    public static void streams_sh_update()
    {
    /*TODO*///	int channel,i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (Machine->sample_rate == 0) return;
    /*TODO*///
    /*TODO*///	/* update all the output buffers */
    /*TODO*///	for (channel = 0;channel < MIXER_MAX_CHANNELS;channel += stream_joined_channels[channel])
    /*TODO*///	{
    /*TODO*///		if (stream_buffer[channel])
    /*TODO*///		{
    /*TODO*///			int newpos;
    /*TODO*///			int buflen;
    /*TODO*///
    /*TODO*///
    /*TODO*///			newpos = SAMPLES_THIS_FRAME(channel);
    /*TODO*///
    /*TODO*///			buflen = newpos - stream_buffer_pos[channel];
    /*TODO*///
    /*TODO*///			if (stream_joined_channels[channel] > 1)
    /*TODO*///			{
    /*TODO*///				INT16 *buf[MIXER_MAX_CHANNELS];
    /*TODO*///
    /*TODO*///
    /*TODO*///				if (buflen > 0)
    /*TODO*///				{
    /*TODO*///					for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///						buf[i] = stream_buffer[channel+i] + stream_buffer_pos[channel+i];
    /*TODO*///
    /*TODO*///					(*stream_callback_multi[channel])(stream_param[channel],buf,buflen);
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///					stream_buffer_pos[channel+i] = 0;
    /*TODO*///
    /*TODO*///				for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///					apply_RC_filter(channel+i,stream_buffer[channel+i],buflen,stream_sample_rate[channel+i]);
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				if (buflen > 0)
    /*TODO*///				{
    /*TODO*///					INT16 *buf;
    /*TODO*///
    /*TODO*///
    /*TODO*///					buf = stream_buffer[channel] + stream_buffer_pos[channel];
    /*TODO*///
    /*TODO*///					(*stream_callback[channel])(stream_param[channel],buf,buflen);
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				stream_buffer_pos[channel] = 0;
    /*TODO*///
    /*TODO*///				apply_RC_filter(channel,stream_buffer[channel],buflen,stream_sample_rate[channel]);
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	for (channel = 0;channel < MIXER_MAX_CHANNELS;channel += stream_joined_channels[channel])
    /*TODO*///	{
    /*TODO*///		if (stream_buffer[channel])
    /*TODO*///		{
    /*TODO*///			for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///				mixer_play_streamed_sample_16(channel+i,
    /*TODO*///						stream_buffer[channel+i],sizeof(INT16)*SAMPLES_THIS_FRAME(channel+i),
    /*TODO*///						stream_sample_rate[channel]);
    /*TODO*///		}
    /*TODO*///	}
    }

    public static int stream_init(String name,int default_mixing_level,
		int sample_rate,
		int param,StreamInitPtr callback)
    {
    	int channel;
    
    
    	channel = mixer_allocate_channel(default_mixing_level);
    
    	stream_joined_channels[channel] = 1;
    
    	mixer_set_name(channel,name);
    /*TODO*///
    /*TODO*///	if ((stream_buffer[channel] = malloc(sizeof(INT16)*BUFFER_LEN)) == 0)
    /*TODO*///		return -1;
    /*TODO*///
    /*TODO*///	stream_sample_rate[channel] = sample_rate;
    /*TODO*///	stream_buffer_pos[channel] = 0;
    /*TODO*///	if (sample_rate)
    /*TODO*///		stream_sample_length[channel] = 1000000 / sample_rate;
    /*TODO*///	else
    /*TODO*///		stream_sample_length[channel] = 0;
    /*TODO*///	stream_param[channel] = param;
    /*TODO*///	stream_callback[channel] = callback;
    /*TODO*///	set_RC_filter(channel,0,0,0,0);
    /*TODO*///
    /*TODO*///	return channel;
        /*temphack*/ return 0;
    }
    /*TODO*///
    /*TODO*///
    /*TODO*///int stream_init_multi(int channels,const char **names,const int *default_mixing_levels,
    /*TODO*///		int sample_rate,
    /*TODO*///		int param,void (*callback)(int param,INT16 **buffer,int length))
    /*TODO*///{
    /*TODO*///	int channel,i;
    /*TODO*///
    /*TODO*///
    /*TODO*///	channel = mixer_allocate_channels(channels,default_mixing_levels);
    /*TODO*///
    /*TODO*///	stream_joined_channels[channel] = channels;
    /*TODO*///
    /*TODO*///	for (i = 0;i < channels;i++)
    /*TODO*///	{
    /*TODO*///		mixer_set_name(channel+i,names[i]);
    /*TODO*///
    /*TODO*///		if ((stream_buffer[channel+i] = malloc(sizeof(INT16)*BUFFER_LEN)) == 0)
    /*TODO*///			return -1;
    /*TODO*///
    /*TODO*///		stream_sample_rate[channel+i] = sample_rate;
    /*TODO*///		stream_buffer_pos[channel+i] = 0;
    /*TODO*///		if (sample_rate)
    /*TODO*///			stream_sample_length[channel+i] = 1000000 / sample_rate;
    /*TODO*///		else
    /*TODO*///			stream_sample_length[channel+i] = 0;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	stream_param[channel] = param;
    /*TODO*///	stream_callback_multi[channel] = callback;
    /*TODO*///	set_RC_filter(channel,0,0,0,0);
    /*TODO*///
    /*TODO*///	return channel;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* min_interval is in usec */
    public static void stream_update(int channel,int min_interval)
    {
    /*TODO*///	int newpos;
    /*TODO*///	int buflen;
    /*TODO*///
    /*TODO*///
    /*TODO*///	if (Machine->sample_rate == 0 || stream_buffer[channel] == 0)
    /*TODO*///		return;
    /*TODO*///
    /*TODO*///	/* get current position based on the timer */
    /*TODO*///	newpos = sound_scalebufferpos(SAMPLES_THIS_FRAME(channel));
    /*TODO*///
    /*TODO*///	buflen = newpos - stream_buffer_pos[channel];
    /*TODO*///
    /*TODO*///	if (buflen * stream_sample_length[channel] > min_interval)
    /*TODO*///	{
    /*TODO*///		if (stream_joined_channels[channel] > 1)
    /*TODO*///		{
    /*TODO*///			INT16 *buf[MIXER_MAX_CHANNELS];
    /*TODO*///			int i;
    /*TODO*///
    /*TODO*///
    /*TODO*///			for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///				buf[i] = stream_buffer[channel+i] + stream_buffer_pos[channel+i];
    /*TODO*///
    /*TODO*///			profiler_mark(PROFILER_SOUND);
    /*TODO*///			(*stream_callback_multi[channel])(stream_param[channel],buf,buflen);
    /*TODO*///			profiler_mark(PROFILER_END);
    /*TODO*///
    /*TODO*///			for (i = 0;i < stream_joined_channels[channel];i++)
    /*TODO*///				stream_buffer_pos[channel+i] += buflen;
    /*TODO*///		}
    /*TODO*///		else
    /*TODO*///		{
    /*TODO*///			INT16 *buf;
    /*TODO*///
    /*TODO*///
    /*TODO*///			buf = stream_buffer[channel] + stream_buffer_pos[channel];
    /*TODO*///
    /*TODO*///			profiler_mark(PROFILER_SOUND);
    /*TODO*///			(*stream_callback[channel])(stream_param[channel],buf,buflen);
    /*TODO*///			profiler_mark(PROFILER_END);
    /*TODO*///
    /*TODO*///			stream_buffer_pos[channel] += buflen;
    /*TODO*///		}
    /*TODO*///	}
    }
}
