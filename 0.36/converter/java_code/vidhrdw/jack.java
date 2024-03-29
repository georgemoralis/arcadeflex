/***************************************************************************

  vidhrdw/jack.c

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

public class jack
{
	
	static int flipscreen = 0;
	
	public static WriteHandlerPtr jack_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* RGB output is inverted */
		paletteram_BBGGGRRR_w(offset,~data);
	} };
	
	
	public static ReadHandlerPtr jack_flipscreen_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset != flipscreen)
		{
			flipscreen = offset;
			memset(dirtybuffer,1,videoram_size);
		}
	
		return 0;
	} };
	
	public static WriteHandlerPtr jack_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		jack_flipscreen_r(offset);
	} };
	
	
	public static VhUpdatePtr jack_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (palette_recalc())
			memset(dirtybuffer,1,videoram_size);
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs / 32;
				sy = 31 - offs % 32;
	
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + ((colorram[offs] & 0x18) << 5),
						colorram[offs] & 0x07,
						flipscreen,flipscreen,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* draw sprites */
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,num, color,flipx,flipy;
	
			sx    = spriteram[offs + 1];
			sy    = spriteram[offs];
			num   = spriteram[offs + 2] + ((spriteram[offs + 3] & 0x08) << 5);
			color = spriteram[offs + 3] & 0x07;
			flipx = (spriteram[offs + 3] & 0x80);
			flipy = (spriteram[offs + 3] & 0x40);
	
			if (flipscreen != 0)
			{
				sx = 248 - sx;
				sy = 248 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					num,
					color,
					flipx,flipy,
					sx,sy,
					&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
