/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class pbaction
{
	
	
	UBytePtr pbaction_videoram2,*pbaction_colorram2;
	static UBytePtr dirtybuffer2;
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
	
		if ((dirtybuffer2 = malloc(videoram_size[0])) == 0)
		{
			generic_vh_stop();
			return 1;
		}
		memset(dirtybuffer2,1,videoram_size[0]);
	
		if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
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
		bitmap_free(tmpbitmap2);
		free(dirtybuffer2);
		generic_vh_stop();
	} };
	
	
	
	public static WriteHandlerPtr pbaction_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (pbaction_videoram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			pbaction_videoram2[offset] = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr pbaction_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (pbaction_colorram2[offset] != data)
		{
			dirtybuffer2[offset] = 1;
	
			pbaction_colorram2[offset] = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr pbaction_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		scroll = -(data-3);
	} };
	
	
	
	public static WriteHandlerPtr pbaction_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
			memset(dirtybuffer2,1,videoram_size[0]);
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
			memset(dirtybuffer,1,videoram_size[0]);
			memset(dirtybuffer2,1,videoram_size[0]);
		}
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy,flipx,flipy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				flipx = colorram.read(offs)& 0x40;
				flipy = colorram.read(offs)& 0x80;
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ 0x10 * (colorram.read(offs)& 0x30),
						colorram.read(offs)& 0x0f,
						flipx,flipy,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
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
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the background */
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scroll,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
	
			/* if next sprite is double size, skip this one */
			if (offs > 0 && spriteram.read(offs - 4)& 0x80) continue;
	
			sx = spriteram.read(offs+3);
			if (spriteram.read(offs)& 0x80)
				sy = 225-spriteram.read(offs+2);
			else
				sy = 241-spriteram.read(offs+2);
			flipx = spriteram.read(offs+1)& 0x40;
			flipy =	spriteram.read(offs+1)& 0x80;
			if (flipscreen != 0)
			{
				if (spriteram.read(offs)& 0x80)
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
	
			drawgfx(bitmap,Machine.gfx[(spriteram.read(offs)& 0x80) ? 3 : 2],	/* normal or double size */
					spriteram.read(offs),
					spriteram.read(offs + 1)& 0x0f,
					flipx,flipy,
					sx+scroll,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* copy the foreground */
		copyscrollbitmap(bitmap,tmpbitmap,1,&scroll,0,0,&Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	} };
}
