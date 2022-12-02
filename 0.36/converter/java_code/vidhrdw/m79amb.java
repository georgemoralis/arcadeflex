/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;

public class m79amb
{
	
	
	
	/* palette colors (see drivers/8080bw.c) */
	enum { BLACK, WHITE };
	
	
	static unsigned char mask = 0;
	
	public static WriteHandlerPtr ramtek_mask_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		mask = data;
	} };
	
	public static WriteHandlerPtr ramtek_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		data = data & ~mask;
	
		if (videoram[offset] != data)
		{
			int i,x,y;
	
			videoram[offset] = data;
	
			y = offset / 32;
			x = 8 * (offset % 32);
	
			for (i = 0; i < 8; i++)
			{
				plot_pixel2(Machine.scrbitmap, tmpbitmap, x, y, Machine.pens[(data & 0x80) ? WHITE : BLACK]);
	
				x++;
				data <<= 1;
			}
		}
	} };
}
