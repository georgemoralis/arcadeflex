/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class cclimber
{
	
	
	/* macro to convert 4-bit unsigned samples to 8-bit signed samples */
	#define SAMPLE_CONV4(a) (0x11*((a&0x0f))-0x80)
	
	#define SND_CLOCK 3072000	/* 3.072 MHz */
	
	
	static signed char *samplebuf;	/* buffer to decode samples at run time */
	static int channel;
	
	
	
	public static ShStartPtr cclimber_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		channel = mixer_allocate_channel(50);
		mixer_set_name(channel,"Samples");
	
		samplebuf = 0;
		if (memory_region(REGION_SOUND1))
		{
			samplebuf = malloc(2*memory_region_length(REGION_SOUND1));
			if (!samplebuf)
				return 1;
		}
	
		return 0;
	} };
	
	public static ShStopPtr cclimber_sh_stop = new ShStopPtr() { public void handler() 
	{
		if (samplebuf != 0)
			free(samplebuf);
		samplebuf = NULL;
	} };
	
	
	
	static void cclimber_play_sample(int start,int freq,int volume)
	{
		int len;
		const UINT8 *rom = memory_region(REGION_SOUND1);
	
	
		if (!rom) return;
	
		/* decode the rom samples */
		len = 0;
		while (start + len < memory_region_length(REGION_SOUND1) && rom[start+len] != 0x70)
		{
			int sample;
	
			sample = (rom[start + len] & 0xf0) >> 4;
			samplebuf[2*len] = SAMPLE_CONV4(sample) * volume / 31;
	
			sample = rom[start + len] & 0x0f;
			samplebuf[2*len + 1] = SAMPLE_CONV4(sample) * volume / 31;
	
			len++;
		}
	
		mixer_play_sample(channel,samplebuf,2 * len,freq,0);
	}
	
	
	static int sample_num,sample_freq,sample_volume;
	
	public static WriteHandlerPtr cclimber_sample_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sample_num = data;
	} };
	
	public static WriteHandlerPtr cclimber_sample_rate_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* calculate the sampling frequency */
		sample_freq = SND_CLOCK / 4 / (256 - data);
	} };
	
	public static WriteHandlerPtr cclimber_sample_volume_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sample_volume = data & 0x1f;	/* range 0-31 */
	} };
	
	public static WriteHandlerPtr cclimber_sample_trigger_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data == 0 || Machine.sample_rate == 0)
			return;
	
		cclimber_play_sample(32 * sample_num,sample_freq,sample_volume);
	} };
}
