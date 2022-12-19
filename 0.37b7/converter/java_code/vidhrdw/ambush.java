/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class ambush
{
	
	
	UBytePtr ambush_scrollram;
	UBytePtr ambush_colorbank;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  I'm not sure about the resistor value, I'm using the Galaxian ones.
	
	***************************************************************************/
	public static VhConvertColorPromPtr ambush_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		/* first, the char acter/sprite palette */
		for (i = 0;i < Machine.drv.total_colors; i++)
		{
			int bit0,bit1,bit2;
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			color_prom++;
		}
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void draw_chars(struct osd_bitmap *bitmap, int priority)
	{
		int offs, transparency;
	
	
		transparency = (priority == 0) ? TRANSPARENCY_NONE : TRANSPARENCY_PEN;
	
		for (offs = 0; offs < videoram_size; offs++)
		{
			int code,sx,sy,col;
			UINT8 scroll;
	
	
			sy = (offs / 32);
			sx = (offs % 32);
	
			col = colorram.read(((sy & 0x1c) << 3) + sx);
	
			if ((col & 0x10) != priority)  continue;
	
			scroll = ~ambush_scrollram[sx];
	
			code = videoram.read(offs)| ((col & 0x60) << 3);
	
			if (flip_screen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
				scroll = ~scroll - 1;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					(col & 0x0f) | ((*ambush_colorbank & 0x03) << 4),
					flip_screen,flip_screen,
					8*sx, (8*sy + scroll) & 0xff,
					&Machine.visible_area,transparency,0);
		}
	}
	
	
	public static VhUpdatePtr ambush_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
	
		/* Draw the background priority characters */
		draw_chars(bitmap, 0x00);
	
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int code,col,sx,sy,flipx,flipy,gfx;
	
	
			sy = spriteram.read(offs + 0);
			sx = spriteram.read(offs + 3);
	
			if ( (sy == 0) ||
				 (sy == 0xff) ||
				((sx <  0x40) && (  spriteram.read(offs + 2)& 0x10)) ||
				((sx >= 0xc0) && (!(spriteram.read(offs + 2)& 0x10))))  continue;  /* prevent wraparound */
	
	
			code = (spriteram.read(offs + 1)& 0x3f) | ((spriteram.read(offs + 2)& 0x60) << 1);
	
			if (spriteram.read(offs + 2)& 0x80)
			{
				/* 16x16 sprites */
				gfx = 1;
	
				if (!flip_screen)
				{
					sy = 240 - sy;
				}
				else
				{
					sx = 240 - sx;
				}
			}
			else
			{
				/* 8x8 sprites */
				gfx = 0;
				code <<= 2;
	
				if (!flip_screen)
				{
					sy = 248 - sy;
				}
				else
				{
					sx = 248 - sx;
				}
			}
	
			col   = spriteram.read(offs + 2)& 0x0f;
			flipx = spriteram.read(offs + 1)& 0x40;
			flipy = spriteram.read(offs + 1)& 0x80;
	
			if (flip_screen != 0)
			{
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine.gfx[gfx],
					code, col | ((*ambush_colorbank & 0x03) << 4),
					flipx, flipy,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* Draw the foreground priority characters */
		draw_chars(bitmap, 0x10);
	} };
}
