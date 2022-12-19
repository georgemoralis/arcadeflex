/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class pkunwar
{
	
	
	static int flipscreen[2];
	
	
	public static WriteHandlerPtr pkunwar_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen[0] != (data & 1))
		{
			flipscreen[0] = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
		if (flipscreen[1] != (data & 2))
		{
			flipscreen[1] = data & 2;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	
	
	public static VhUpdatePtr pkunwar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				if (flipscreen[0]) sx = 31 - sx;
				if (flipscreen[1]) sy = 31 - sy;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ ((colorram.read(offs)& 0x07) << 8),
						(colorram.read(offs)& 0xf0) >> 4,
						flipscreen[0],flipscreen[1],
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size[0];offs += 32)
		{
			int sx,sy,flipx,flipy;
	
	
			sx = ((spriteram.read(offs + 1)+ 8) & 0xff) - 8;
			sy = spriteram.read(offs + 2);
			flipx = spriteram.read(offs)& 0x01;
			flipy = spriteram.read(offs)& 0x02;
			if (flipscreen[0])
			{
				sx = 240 - sx;
				flipx = !flipx;
			}
			if (flipscreen[1])
			{
				sy = 240 - sy;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					((spriteram.read(offs)& 0xfc) >> 2) + ((spriteram.read(offs + 3)& 0x07) << 6),
					(spriteram.read(offs + 3)& 0xf0) >> 4,
					flipx,flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* redraw characters which have priority over sprites */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (colorram.read(offs)& 0x08)
			{
				int sx,sy;
	
	
				sx = offs % 32;
				sy = offs / 32;
				if (flipscreen[0]) sx = 31 - sx;
				if (flipscreen[1]) sy = 31 - sy;
	
				drawgfx(bitmap,Machine.gfx[0],
						videoram.read(offs)+ ((colorram.read(offs)& 0x07) << 8),
						(colorram.read(offs)& 0xf0) >> 4,
						flipscreen[0],flipscreen[1],
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	} };
}
