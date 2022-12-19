/***************************************************************************

Minivader (Space Invaders's mini game)
(c)1990 Taito Corporation

Driver by Takahiro Nogi (nogi@kt.rim.or.jp) 1999/12/19 -

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

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
	
	void minivadr_init_palette(UBytePtr game_palette, unsigned short *game_colortable,const UBytePtr color_prom)
	{
		memcpy(game_palette, minivadr_palette, sizeof(minivadr_palette));
	}
	
	
	/*******************************************************************
	
		Draw Pixel.
	
	*******************************************************************/
	public static WriteHandlerPtr minivadr_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
		int x, y;
		int color;
	
	
		videoram.write(offset,data);

		x = (offset % 32) * 8;
		y = (offset / 32);
	
		if (x >= Machine.visible_area.min_x &&
				x <= Machine.visible_area.max_x &&
				y >= Machine.visible_area.min_y &&
				y <= Machine.visible_area.max_y)
		{
			for (i = 0; i < 8; i++)
			{
				color = Machine.pens[((data >> i) & 0x01)];
	
				plot_pixel(Machine.scrbitmap, x + (7 - i), y, color);
			}
		}
	} };
	
	
	public static VhUpdatePtr minivadr_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
		{
			int offs;
	
			/* redraw bitmap */
	
			for (offs = 0; offs < videoram_size[0]; offs++)
				minivadr_videoram_w(offs,videoram.read(offs));
		}
	} };
}
