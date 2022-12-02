/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

public class atari_vg
{
	
	#define EAROM_SIZE	0x40
	
	static int earom_offset;
	static int earom_data;
	static char earom[EAROM_SIZE];
	
	public static ReadHandlerPtr atari_vg_earom_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (errorlog != 0)
			fprintf (errorlog, "read earom: %02x(%02x):%02x\n", earom_offset, offset, earom_data);
		return (earom_data);
	} };
	
	public static WriteHandlerPtr atari_vg_earom_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (errorlog != 0)
			fprintf (errorlog, "write earom: %02x:%02x\n", offset, data);
		earom_offset = offset;
		earom_data = data;
	} };
	
	/* 0,8 and 14 get written to this location, too.
	 * Don't know what they do exactly
	 */
	public static WriteHandlerPtr atari_vg_earom_ctrl = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (errorlog != 0)
			fprintf (errorlog, "earom ctrl: %02x:%02x\n",offset, data);
		/*
			0x01 = clock
			0x02 = set data latch? - writes only (not always)
			0x04 = write mode? - writes only
			0x08 = set addr latch?
		*/
		if ((data & 0x01) != 0)
			earom_data = earom[earom_offset];
		if ((data & 0x0c) == 0x0c)
		{
			earom[earom_offset]=earom_data;
			if (errorlog != 0)
				fprintf (errorlog, "    written %02x:%02x\n", earom_offset, earom_data);
		}
	} };
	
	
	void atari_vg_earom_handler(void *file,int read_or_write)
	{
		if (read_or_write != 0)
			osd_fwrite(file,earom,EAROM_SIZE);
		else
		{
			if (file != 0)
				osd_fread(file,earom,EAROM_SIZE);
			else
				memset(earom,0,EAROM_SIZE);
		}
	}
}
