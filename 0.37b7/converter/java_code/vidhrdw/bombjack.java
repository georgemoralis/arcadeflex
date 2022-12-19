/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class bombjack
{
	
	
	
	static int background_image;
	static int flipscreen;
	
	
	
	public static WriteHandlerPtr bombjack_background_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (background_image != data)
		{
			memset(dirtybuffer,1,videoram_size[0]);
			background_image = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr bombjack_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr bombjack_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,base;
	
	
		if (palette_recalc())
			memset(dirtybuffer,1,videoram_size[0]);
	
		base = 0x200 * (background_image & 0x07);
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy;
			int tilecode,tileattribute;
	
	
			sx = offs % 32;
			sy = offs / 32;
	
			if ((background_image & 0x10) != 0)
			{
				int bgoffs;
	
	
				bgoffs = base+16*(sy/2)+sx/2;
	
				tilecode = memory_region(REGION_GFX4)[bgoffs],
				tileattribute = memory_region(REGION_GFX4)[bgoffs + 0x100];
			}
			else
			{
				tilecode = 0xff;
				tileattribute = 0;	/* avoid compiler warning */
			}
	
			if (dirtybuffer[offs])
			{
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				/* draw the background (this can be handled better) */
				if (tilecode != 0xff)
				{
					struct rectangle clip;
					int flipy;
	
	
					clip.min_x = 8*sx;
					clip.max_x = 8*sx+7;
					clip.min_y = 8*sy;
					clip.max_y = 8*sy+7;
	
					flipy = tileattribute & 0x80;
					if (flipscreen != 0) flipy = !flipy;
	
					drawgfx(tmpbitmap,Machine.gfx[1],
							tilecode,
							tileattribute & 0x0f,
							flipscreen,flipy,
							16*(sx/2),16*(sy/2),
							&clip,TRANSPARENCY_NONE,0);
	
					drawgfx(tmpbitmap,Machine.gfx[0],
							videoram.read(offs)+ 16 * (colorram.read(offs)& 0x10),
							colorram.read(offs)& 0x0f,
							flipscreen,flipscreen,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				else
					drawgfx(tmpbitmap,Machine.gfx[0],
							videoram.read(offs)+ 16 * (colorram.read(offs)& 0x10),
							colorram.read(offs)& 0x0f,
							flipscreen,flipscreen,
							8*sx,8*sy,
							&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
				dirtybuffer[offs] = 0;
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
	
	/*
	 abbbbbbb cdefgggg hhhhhhhh iiiiiiii
	
	 a        use big sprites (32x32 instead of 16x16)
	 bbbbbbb  sprite code
	 c        x flip
	 d        y flip (used only in death sequence?)
	 e        ? (set when big sprites are selected)
	 f        ? (set only when the bonus (B) materializes?)
	 gggg     color
	 hhhhhhhh x position
	 iiiiiiii y position
	*/
			int sx,sy,flipx,flipy;
	
	
			sx = spriteram.read(offs+3);
			if (spriteram.read(offs)& 0x80)
				sy = 225-spriteram.read(offs+2);
			else
				sy = 241-spriteram.read(offs+2);
			flipx = spriteram.read(offs+1)& 0x40;
			flipy =	spriteram.read(offs+1)& 0x80;
			if (flipscreen != 0)
			{
				if (spriteram.read(offs+1)& 0x20)
				{
					sx = 224 - sx;
					sy = 224 - sy;
				}
				else
				{
					sx = 240 - sx;
					sy = 240 - sy;
				}
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[(spriteram.read(offs)& 0x80) ? 3 : 2],
					spriteram.read(offs)& 0x7f,
					spriteram.read(offs+1)& 0x0f,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
