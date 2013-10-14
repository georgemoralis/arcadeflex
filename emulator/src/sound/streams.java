//TODO
package sound;

/**
 *
 * @author shadow
 */
import static arcadeflex.libc_old.*;
import static sound.mixerH.*;
import static sound.mixer.*;
import static mame.mame.*;
import static mame.sndintrf.*;

public class streams {
    
    public static abstract interface StreamInitPtr { public abstract void handler(int param,CharPtr buffer,int length); }

    public static final int BUFFER_LEN =16384;
    
    public static int SAMPLES_THIS_FRAME(int channel)
    {
       return mixer_need_samples_this_frame((channel),stream_sample_rate[(channel)]);
    }
    
    static int[] stream_joined_channels=new int[MIXER_MAX_CHANNELS];
    static CharPtr[] stream_buffer=new CharPtr[MIXER_MAX_CHANNELS];
    static int[] stream_sample_rate=new int[MIXER_MAX_CHANNELS];
    static int[] stream_buffer_pos=new int[MIXER_MAX_CHANNELS];
    static int[] stream_sample_length=new int[MIXER_MAX_CHANNELS];	/* in usec */
    static int[] stream_param=new int[MIXER_MAX_CHANNELS];
    
    static StreamInitPtr[] stream_callback = new StreamInitPtr[MIXER_MAX_CHANNELS];
    
    /*TODO*///static void (*stream_callback_multi[MIXER_MAX_CHANNELS])(int param,INT16 **buffer,int length);
    /*TODO*///
    static int[] memory=new int[MIXER_MAX_CHANNELS];
    static int[] r1=new int[MIXER_MAX_CHANNELS];
    static int[] r2=new int[MIXER_MAX_CHANNELS];
    static int[] r3=new int[MIXER_MAX_CHANNELS];
    static int[] c=new int[MIXER_MAX_CHANNELS];
    
    /*
    signal >--R1--+--R2--+
                  |      |
                  C      R3---> amp
                  |      |
                 GND    GND
    */
    
    /* R1, R2, R3 in Ohm; C in pF */
    /* set C = 0 to disable the filter */
    public static void set_RC_filter(int channel,int R1,int R2,int R3,int C)
    {
    	r1[channel] = R1;
    	r2[channel] = R2;
    	r3[channel] = R3;
    	c[channel] = C;
    }
    
    public static void apply_RC_filter(int channel,CharPtr buf,int len,int sample_rate)
    {
    	float R1,R2,R3,C;
    	float Req;
    	int K;
    	int i;
    
    
    	if (c[channel] == 0) return;	/* filter disabled */
    
    	R1 = r1[channel];
    	R2 = r2[channel];
    	R3 = r3[channel];
    	C = (float)(c[channel] * 1E-12);	/* convert pF to F */
    
    	/* Cut Frequency = 1/(2*Pi*Req*C) */
    
    	Req = (R1*(R2+R3))/(R1+R2+R3);
    
    	K = (int)(0x10000 * Math.exp(-1 / (Req * C) / sample_rate));
    
    	buf.write(0,buf.read(0) + (memory[channel] - buf.read(0)) * K / 0x10000);
    
    	for (i = 1;i < len;i++)
    		buf.write(i,buf.read(i) + (buf.read(i-1) - buf.read(i)) * K / 0x10000);
    
    	memory[channel] = buf.read(len-1);
    }
    
    
    
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
    	int i;
    
    
    	for (i = 0;i < MIXER_MAX_CHANNELS;i++)
    	{
    		stream_buffer[i] = null;
    	}
    }

    
    public static void streams_sh_update()
    {
    	int channel,i;
    
    
    	if (Machine.sample_rate == 0) return;
    
    	/* update all the output buffers */
    	for (channel = 0;channel < MIXER_MAX_CHANNELS;channel += stream_joined_channels[channel])
    	{
    		if (stream_buffer[channel]!=null)
    		{
    			int newpos;
    			int buflen;
    
    
    			newpos = SAMPLES_THIS_FRAME(channel);
    
    			buflen = newpos - stream_buffer_pos[channel];
    
    			if (stream_joined_channels[channel] > 1)
    			{
                            throw new UnsupportedOperationException("stream_sh_update channel >1 ");
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
    			}
    			else
    			{
    				if (buflen > 0)
    				{
  				  //INT16 *buf;
    				  //buf = stream_buffer[channel] + stream_buffer_pos[channel];
				  //(*stream_callback[channel])(stream_param[channel],buf,buflen);                                
                                  CharPtr buf= new CharPtr(stream_buffer[channel], stream_buffer_pos[channel]);
                                  stream_callback[channel].handler(stream_param[channel],buf,buflen);
    				}
    
    				stream_buffer_pos[channel] = 0;
    
    				apply_RC_filter(channel,stream_buffer[channel],buflen,stream_sample_rate[channel]);
    			}
    		}
    	}
    
    	for (channel = 0;channel < MIXER_MAX_CHANNELS;channel += stream_joined_channels[channel])
    	{
    		if (stream_buffer[channel]!=null)
    		{
    			for (i = 0;i < stream_joined_channels[channel];i++)
    				mixer_play_streamed_sample_16(channel+i,
    						stream_buffer[channel+i],SAMPLES_THIS_FRAME(channel+i),
    						stream_sample_rate[channel]);
    		}
    	}
    }

    public static int stream_init(String name,int default_mixing_level,
		int sample_rate,
		int param,StreamInitPtr callback)
    {
    	int channel;
    
    
    	channel = mixer_allocate_channel(default_mixing_level);
    
    	stream_joined_channels[channel] = 1;
    
    	mixer_set_name(channel,name);
        
        stream_buffer[channel]= new CharPtr(BUFFER_LEN);

    
    	stream_sample_rate[channel] = sample_rate;
    	stream_buffer_pos[channel] = 0;
    	if (sample_rate!=0)
    		stream_sample_length[channel] = 1000000 / sample_rate;
    	else
    		stream_sample_length[channel] = 0;
    	stream_param[channel] = param;
    	stream_callback[channel] = callback;
    	set_RC_filter(channel,0,0,0,0);
    
    	return channel;
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
    	int newpos;
    	int buflen;
    
    
    	if (Machine.sample_rate == 0 || stream_buffer[channel] == null)
    		return;
    
    	/* get current position based on the timer */
    	newpos = sound_scalebufferpos(SAMPLES_THIS_FRAME(channel));
    
    	buflen = newpos - stream_buffer_pos[channel];
    
    	if (buflen * stream_sample_length[channel] > min_interval)
    	{
    		if (stream_joined_channels[channel] > 1)
    		{
                    throw new UnsupportedOperationException("stream_update channel >1 ");
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
    		}
    		else
    		{
    			//INT16 *buf;
    			//buf = stream_buffer[channel] + stream_buffer_pos[channel];
    			//(*stream_callback[channel])(stream_param[channel],buf,buflen);
                        CharPtr buf = new CharPtr(stream_buffer[channel],stream_buffer_pos[channel]);
                        stream_callback[channel].handler(stream_param[channel],buf,buflen);
    			stream_buffer_pos[channel] += buflen;
    		}
    	}
    }
}
