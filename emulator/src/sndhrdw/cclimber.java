/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package sndhrdw;

import static mame.driverH.*;
import static mame.sndintrfH.*;
import static mame.mame.*;

public class cclimber
{
	
	
	/* macro to convert 4-bit unsigned samples to 8-bit signed samples */
/*TODO*///	#define SAMPLE_CONV4(a) (0x11*((a&0x0f))-0x80)
	
        public static final int SND_CLOCK =3072000;	/* 3.072 Mhz */
	
	
/*TODO*///	static signed char *samplebuf;	/* buffer to decode samples at run time */
/*TODO*///	static int channel;
	
	
	public static ShStartPtr cclimber_sh_start = new ShStartPtr() { public int handler(MachineSound msound)
	{
/*TODO*///		channel = mixer_allocate_channel(50);
/*TODO*///		mixer_set_name(channel,"Samples");
	
/*TODO*///		samplebuf = 0;
/*TODO*///		if (memory_region(REGION_SOUND1))
/*TODO*///		{
/*TODO*///			samplebuf = malloc(2*memory_region_length(REGION_SOUND1));
/*TODO*///			if (!samplebuf)
/*TODO*///				return 1;
/*TODO*///		}
	
		return 0;
	}};
	
	public static ShStopPtr cclimber_sh_stop = new ShStopPtr() { public void handler() 
	{
/*TODO*///		if (samplebuf != 0)
/*TODO*///			free(samplebuf);
/*TODO*///		samplebuf = NULL;
	} };
	
	
	
	static void cclimber_play_sample(int start,int freq,int volume)
	{
/*TODO*///		int len;
/*TODO*///		const UINT8 *rom = memory_region(REGION_SOUND1);
	
	
/*TODO*///		if (!rom) return;
	
		/* decode the rom samples */
/*TODO*///		len = 0;
/*TODO*///		while (start + len < memory_region_length(REGION_SOUND1) && rom[start+len] != 0x70)
/*TODO*///		{
/*TODO*///			int sample;
	
/*TODO*///			sample = (rom[start + len] & 0xf0) >> 4;
/*TODO*///			samplebuf[2*len] = SAMPLE_CONV4(sample) * volume / 31;
	
/*TODO*///			sample = rom[start + len] & 0x0f;
/*TODO*///			samplebuf[2*len + 1] = SAMPLE_CONV4(sample) * volume / 31;
	
/*TODO*///			len++;
/*TODO*///		}
	
/*TODO*///		mixer_play_sample(channel,samplebuf,2 * len,freq,0);
	}
	
	
	static int sample_num,sample_freq,sample_volume;
	
	public static WriteHandlerPtr cclimber_sample_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		sample_num = data;
	} };
	
	public static WriteHandlerPtr cclimber_sample_rate_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* calculate the sampling frequency */
		sample_freq = SND_CLOCK / 4 / (256 - data);
	} };
	
	public static WriteHandlerPtr cclimber_sample_volume_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		sample_volume = data & 0x1f;	/* range 0-31 */
	} };
	
	public static WriteHandlerPtr cclimber_sample_trigger_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (data == 0 || Machine.sample_rate == 0)
			return;
	
		cclimber_play_sample(32 * sample_num,sample_freq,sample_volume);
	} };
}
