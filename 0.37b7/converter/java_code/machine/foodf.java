/*************************************************************************

  Food Fight machine hardware

*************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class foodf
{
	
	
	
	
	/*
	 *		Statics
	 */
	
	static int whichport = 0;
	
	
	/*
	 *		Interrupt handlers.
	 */
	
	void foodf_delayed_interrupt (int param)
	{
		cpu_cause_interrupt (0, 2);
	}
	
	public static InterruptPtr foodf_interrupt = new InterruptPtr() { public int handler() 
	{
		/* INT 2 once per frame in addition to... */
		if (cpu_getiloops () == 0)
			timer_set (TIME_IN_USEC (100), 0, foodf_delayed_interrupt);
	
		/* INT 1 on the 32V signal */
		return 1;
	} };
	
	
	/*
	 *		NVRAM read/write.
	 *      also used by Quantum
	 */
	
	static unsigned char nvram[128];
	
	public static ReadHandlerPtr foodf_nvram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ((nvram[(offset / 4) ^ 0x03] >> 2*(offset % 4))) & 0x0f;
	} };
	
	
	public static WriteHandlerPtr foodf_nvram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		nvram[(offset / 4) ^ 0x03] &= ~(0x0f << 2*(offset % 4));
		nvram[(offset / 4) ^ 0x03] |= (data & 0x0f) << 2*(offset % 4);
	} };
	
	public static nvramPtr foodf_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file,nvram,128);
		else
		{
			if (file != 0)
				osd_fread(file,nvram,128);
			else
				memset(nvram,0xff,128);
		}
	} };
	
	
	/*
	 *		Analog controller read dispatch.
	 */
	
	public static ReadHandlerPtr foodf_analog_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
			case 2:
			case 4:
			case 6:
				return readinputport (whichport);
		}
		return 0;
	} };
	
	
	/*
	 *		Digital controller read dispatch.
	 */
	
	public static ReadHandlerPtr foodf_digital_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return input_port_4_r.handler (offset);
		}
		return 0;
	} };
	
	
	/*
	 *		Analog write dispatch.
	 */
	
	public static WriteHandlerPtr foodf_analog_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		whichport = 3 - ((offset/2) & 3);
	} };
	
	
	/*
	 *		Digital write dispatch.
	 */
	
	public static WriteHandlerPtr foodf_digital_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	} };
}
