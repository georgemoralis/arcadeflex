/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package sndhrdw;

public class bosco
{
	
	/* macro to convert 4-bit unsigned samples to 8-bit signed samples */
	#define SAMPLE_CONV4(a) (0x11*((a&0x0f))-0x80)
	
	
	static signed char *speech;	/* 24k for speech */
	static int channel;
	
	
	
	int bosco_sh_start(const struct MachineSound *msound)
	{
		int i;
		unsigned char bits;
	
		channel = mixer_allocate_channel(25);
		mixer_set_name(channel,"Samples");
	
		speech = malloc(2*memory_region_length(REGION_SOUND2));
		if (!speech)
			return 1;
	
		/* decode the rom samples */
		for (i = 0;i < memory_region_length(REGION_SOUND2);i++)
		{
			bits = memory_region(REGION_SOUND2)[i] & 0x0f;
			speech[2*i] = SAMPLE_CONV4(bits);
	
			bits = (memory_region(REGION_SOUND2)[i] & 0xf0) >> 4;
			speech[2*i + 1] = SAMPLE_CONV4(bits);
		}
	
		return 0;
	}
	
	
	public static ShStopPtr bosco_sh_stop = new ShStopPtr() { public void handler() 
	{
		if (speech != 0)
			free(speech);
		speech = NULL;
	} };
	
	
	public static WriteHandlerPtr bosco_sample_play = new WriteHandlerPtr() { public void handler(int offset, int length)
	{
		if (Machine.sample_rate == 0)
			return;
	
		mixer_play_sample(channel,speech + offset,length,4000,0);
	} };
}
