/***************************************************************************

Minivader (Space Invaders's mini game)
(c)1990 Taito Corporation

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/12/19 -

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

public class minivadr
{
	
	
	
	/*******************************************************************
	
		Palette Setting.
	
	*******************************************************************/
	static unsigned char minivadr_palette[] =
	{
		0x00,0x00,0x00,			/* black */
		0xff,0xff,0xff			/* white */
	};
	
	void minivadr_init_palette(unsigned char *game_palette, unsigned short *game_colortable,const unsigned char *color_prom)
	{
		memcpy(game_palette, minivadr_palette, sizeof(minivadr_palette));
	}
	
	
	/*******************************************************************
	
		Draw Pixel.
	
	*******************************************************************/
	public static WriteHandlerPtr minivadr_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int i;
		int x, y;
		int color;
	
	
		videoram[offset] = data;
	
		x = (offset % 32) * 8;
		y = (offset / 32);
	
		if (x >= Machine.drv.visible_area.min_x &&
				x <= Machine.drv.visible_area.max_x &&
				y >= Machine.drv.visible_area.min_y &&
				y <= Machine.drv.visible_area.max_y)
		{
			for (i = 0; i < 8; i++)
			{
				color = Machine.pens[((data >> i) & 0x01)];
	
				plot_pixel(Machine.scrbitmap, x + (7 - i), y, color);
			}
		}
	} };
	
	
	void minivadr_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
	{
		if (full_refresh != 0)
		{
			int offs;
	
			/* redraw bitmap */
	
			for (offs = 0; offs < videoram_size; offs++)
				minivadr_videoram_w(offs,videoram[offs]);
		}
	}
}
