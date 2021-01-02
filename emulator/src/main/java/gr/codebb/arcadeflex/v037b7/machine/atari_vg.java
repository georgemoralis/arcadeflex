/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.machine;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.common.libc.cstring.memset;

public class atari_vg
{
	
	public static final int EAROM_SIZE	= 0x40;
	
	static int earom_offset;
	static int earom_data;
	static UBytePtr earom = new UBytePtr(EAROM_SIZE);
	
	public static ReadHandlerPtr atari_vg_earom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		logerror("read earom: %02x(%02x):%02x\n", earom_offset, offset, earom_data);
		return (earom_data);
	} };
	
	public static WriteHandlerPtr atari_vg_earom_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("write earom: %02x:%02x\n", offset, data);
		earom_offset = offset;
		earom_data = data;
	} };
	
	/* 0,8 and 14 get written to this location, too.
	 * Don't know what they do exactly
	 */
	public static WriteHandlerPtr atari_vg_earom_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("earom ctrl: %02x:%02x\n",offset, data);
		/*
			0x01 = clock
			0x02 = set data latch? - writes only (not always)
			0x04 = write mode? - writes only
			0x08 = set addr latch?
		*/
		if ((data & 0x01) != 0)
			earom_data = earom.read(earom_offset);
		if ((data & 0x0c) == 0x0c)
		{
			earom.write(earom_offset, earom_data);
			logerror("    written %02x:%02x\n", earom_offset, earom_data);
		}
	} };
	
	
	public static nvramPtr atari_vg_earom_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			osd_fwrite(file,earom,EAROM_SIZE);
		else
		{
			if (file != null)
				osd_fread(file,earom,EAROM_SIZE);
			else
				memset(earom,0,EAROM_SIZE);
		}
	} };
}
