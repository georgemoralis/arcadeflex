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

public class pbaction
{
	
	
	unsigned char *pbaction_videoram2,*pbaction_colorram2;
	public static WriteHandlerPtr pbaction_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	public static WriteHandlerPtr pbaction_colorram2_w = new WriteHandlerPtr() { public void handler(int offset, int data);
	static unsigned char *dirtybuffer2;
	static struct osd_bitmap *tmpbitmap2;
	static int scroll;
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr pbaction_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
	
	
		if (generic_vh_start() != 0)
			return 1;
	
		if ((dirtybuffer2 = malloc(videoram_size)) == 0)
		{
			generic_vh_stop();
			return 1;
		}
		memset(dirtybuffer2,1,videoram_size);
	
		if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
		{
			free(dirtybuffer2);
			generic_vh_stop();
			return 1;
		}
	
		/* leave everything at the default, but map all foreground 0 pens as transparent */
		for (i = 0;i < 16;i++) palette_used_colors[8 * i] = PALETTE_COLOR_TRANSPARENT;
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr pbaction_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap2);
		free(dirtybuffer2);
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr pbaction_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (pbaction_videoram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			pbaction_videoram2[offset] = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr pbaction_colorram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (pbaction_colorram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			pbaction_colorram2[offset] = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr pbaction_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll = -(data-3);
	} };
	
	
	
	public static WriteHandlerPtr pbaction_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size);
			memset(dirtybuffer2,1,videoram_size);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr pbaction_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* recalc the palette if necessary */
		if (palette_recalc())
		{
			memset(dirtybuffer,1,videoram_size);
			memset(dirtybuffer2,1,videoram_size);
		}
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy,flipx,flipy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				flipx = colorram[offs] & 0x40;
				flipy = colorram[offs] & 0x80;
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs] + 0x10 * (colorram[offs] & 0x30),
						colorram[offs] & 0x0f,
						flipx,flipy,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
	
			if (dirtybuffer2[offs])
			{
				int sx,sy,flipy;
	
	
				dirtybuffer2[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				flipy = pbaction_colorram2[offs] & 0x80;
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipy = !flipy;
				}
	
				drawgfx(tmpbitmap2,Machine.gfx[1],
						pbaction_videoram2[offs] + 0x10 * (pbaction_colorram2[offs] & 0x70),
						pbaction_colorram2[offs] & 0x0f,
						flipscreen,flipy,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the background */
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scroll,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. */
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
	
			/* if next sprite is double size, skip this one */
			if (offs > 0 && spriteram[offs - 4] & 0x80) continue;
	
			sx = spriteram[offs+3];
			if (spriteram[offs] & 0x80)
				sy = 225-spriteram[offs+2];
			else
				sy = 241-spriteram[offs+2];
			flipx = spriteram[offs+1] & 0x40;
			flipy =	spriteram[offs+1] & 0x80;
			if (flipscreen != 0)
			{
				if (spriteram[offs] & 0x80)
				{
					sx = 224 - sx;
					sy = 225 - sy;
				}
				else
				{
					sx = 240 - sx;
					sy = 241 - sy;
				}
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[(spriteram[offs] & 0x80) ? 3 : 2],	/* normal or double size */
					spriteram[offs],
					spriteram[offs + 1] & 0x0f,
					flipx,flipy,
					sx+scroll,sy,
					&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* copy the foreground */
		copyscrollbitmap(bitmap,tmpbitmap,1,&scroll,0,0,&Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	} };
}
