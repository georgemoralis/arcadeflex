/*************************************************************************

  Food Fight machine hardware

*************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static arcadeflex.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static arcadeflex.ptrlib.*;
import static mame.timer.*;
import static mame.timerH.*;

public class foodf
{
	
	
	
	
	/*
	 *		Statics
	 */
	
	static int whichport = 0;
	
	
	/*
	 *		Interrupt handlers.
	 */
	public static timer_callback foodf_delayed_interrupt = new timer_callback(){ public void handler(int trigger)
        {

		cpu_cause_interrupt (0, 2);
	}};
	
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
	
	static UBytePtr nvram=new UBytePtr(128);
	
	public static ReadHandlerPtr foodf_nvram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ((nvram.read((offset / 4) ^ 0x03) >> 2*(offset % 4))) & 0x0f;
	} };
	
	
	public static WriteHandlerPtr foodf_nvram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		nvram.write((offset / 4) ^ 0x03,nvram.read((offset / 4) ^ 0x03) & ~(0x0f << 2*(offset % 4)));
		nvram.write((offset / 4) ^ 0x03, nvram.read((offset / 4) ^ 0x03) | (data & 0x0f) << 2*(offset % 4));
	} };
	public static nvramPtr foodf_nvram_handler = new nvramPtr(){ public void handler(Object file,int read_or_write)
        {
		if (read_or_write != 0)
                {
/*TODO*///			osd_fwrite(file,nvram,128);
                }
		else
		{
			if (file != null)
                        {
/*TODO*///				osd_fread(file,nvram,128);
                        }
			else
                        {
				memset(nvram,0xff,128);
                        }
		}
	}};
	
	
	/*
	 *		Analog controller read dispatch.
	 */
	
	public static ReadHandlerPtr foodf_analog_r = new ReadHandlerPtr() { public int handler(int offset)
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
	
	public static ReadHandlerPtr foodf_digital_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (offset)
		{
			case 0:
				return input_port_4_r.handler(offset);
		}
		return 0;
	} };
	
	
	/*
	 *		Analog write dispatch.
	 */
	
	public static WriteHandlerPtr foodf_analog_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		whichport = 3 - ((offset/2) & 3);
	} };
	
	
	/*
	 *		Digital write dispatch.
	 */
	
	public static WriteHandlerPtr foodf_digital_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	} };
}
