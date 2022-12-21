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

public class sharkatt
{
	
	static int color_plane = 0;
	
	/***************************************************************************
	 sharkatt_vtcsel_w
	
	 TODO:  This writes to a TMS9927 VTAC.  Do we care?
	 **************************************************************************/
	public static WriteHandlerPtr sharkatt_vtcsel_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	} };
	
	/***************************************************************************
	 sharkatt_color_plane_w
	 **************************************************************************/
	public static WriteHandlerPtr sharkatt_color_plane_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* D0-D3 = WS0-WS3, D4-D5 = RS0-RS1 */
		/* RS = CPU Memory Plane Read Multiplex Select */
		color_plane = (data & 0x3F);
	} };
	
	/***************************************************************************
	 sharkatt_color_map_w
	 **************************************************************************/
	public static WriteHandlerPtr sharkatt_color_map_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    int vals[4] = {0x00,0x55,0xAA,0xFF};
	    int r,g,b;
	
	    r = vals[(data & 0x03) >> 0];
	    g = vals[(data & 0x0C) >> 2];
	    b = vals[(data & 0x30) >> 4];
		palette_change_color (offset,r,g,b);
	} };
	
	/***************************************************************************
	 sharkatt_videoram_w
	 **************************************************************************/
	public static WriteHandlerPtr sharkatt_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i,x,y;
	
	
		videoram[offset] = data;
	
		x = offset / 32;
		y = 8 * (offset % 32);
	
		for (i = 0;i < 8;i++)
		{
			int col;
	
			col = Machine.pens[color_plane & 0x0F];
	
			if ((data & 0x80) != 0)
			{
				plot_pixel2(tmpbitmap, Machine.scrbitmap, x, y, col);
			}
			else
			{
				plot_pixel2(tmpbitmap, Machine.scrbitmap, x, y, Machine.pens[0]);
			}
	
			y++;
			data <<= 1;
		}
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr sharkatt_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (palette_recalc())
		{
			int offs;
	
			for (offs = 0;offs < videoram_size;offs++)
				sharkatt_videoram_w(offs,videoram[offs]);
		}
	
		if (full_refresh != 0)
			/* copy the character mapped graphics */
			copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	} };
}
