/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  (c) 12/2/1998 Lee Taylor

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

public class skychut
{
	
	
	
	static int flipscreen;
	
	
	public static WriteHandlerPtr skychut_vh_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	/*	if (flipscreen != (data & 0x8f))
		{
			flipscreen = (data & 0x8f);
			memset(dirtybuffer,1,videoram_size);
		}
	*/
	} };
	
	
	public static WriteHandlerPtr skychut_colorram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (colorram[offset] != data)
		{
			dirtybuffer[offset] = 1;
	
			colorram[offset] = data;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr skychut_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		if (full_refresh != 0)
			memset (dirtybuffer, 1, videoram_size);
	
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(bitmap,Machine.gfx[0],
						videoram[offs],
						 colorram[offs],
						flipscreen,flipscreen,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
			}
		}
	
	} };
}
