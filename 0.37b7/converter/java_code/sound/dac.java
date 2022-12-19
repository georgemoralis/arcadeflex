/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sound;

public class dac
{
	
	
	static int channel[MAX_DAC];
	static int output[MAX_DAC];
	static int UnsignedVolTable[256];
	static int SignedVolTable[256];
	
	
	
	static void DAC_update(int num,INT16 *buffer,int length)
	{
		int out = output[num];
	
		while (length--) *(buffer++) = out;
	}
	
	
	void DAC_data_w(int num,int data)
	{
		int out = UnsignedVolTable[data];
	
		if (output[num] != out)
		{
			/* update the output buffer before changing the registers */
			stream_update(channel[num],0);
			output[num] = out;
		}
	}
	
	
	void DAC_signed_data_w(int num,int data)
	{
		int out = SignedVolTable[data];
	
		if (output[num] != out)
		{
			/* update the output buffer before changing the registers */
			stream_update(channel[num],0);
			output[num] = out;
		}
	}
	
	
	void DAC_data_16_w(int num,int data)
	{
		int out = data >> 1;		/* range      0..32767 */
	
		if (output[num] != out)
		{
			/* update the output buffer before changing the registers */
			stream_update(channel[num],0);
			output[num] = out;
		}
	}
	
	
	void DAC_signed_data_16_w(int num,int data)
	{
		int out = data - 0x8000;	/* range -32768..32767 */
	
		if (output[num] != out)
		{
			/* update the output buffer before changing the registers */
			stream_update(channel[num],0);
			output[num] = out;
		}
	}
	
	
	static void DAC_build_voltable(void)
	{
		int i;
	
	
		/* build volume table (linear) */
		for (i = 0;i < 256;i++)
		{
			UnsignedVolTable[i] = i * 0x101 / 2;	/* range      0..32767 */
			SignedVolTable[i] = i * 0x101 - 0x8000;	/* range -32768..32767 */
		}
	}
	
	
	public static ShStartPtr DAC_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		int i;
		const struct DACinterface *intf = msound.sound_interface;
	
	
		DAC_build_voltable();
	
		for (i = 0;i < intf.num;i++)
		{
			char name[40];
	
	
			sprintf(name,"DAC #%d",i);
			channel[i] = stream_init(name,intf.mixing_level[i],Machine.sample_rate,
					i,DAC_update);
	
			if (channel[i] == -1)
				return 1;
	
			output[i] = 0;
		}
	
		return 0;
	} };
	
	
	public static WriteHandlerPtr DAC_0_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_data_w(0,data);
	} };
	
	public static WriteHandlerPtr DAC_1_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_data_w(1,data);
	} };
	
	public static WriteHandlerPtr DAC_0_signed_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_signed_data_w(0,data);
	} };
	
	public static WriteHandlerPtr DAC_1_signed_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		DAC_signed_data_w(1,data);
	} };
}
