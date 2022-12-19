/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class sauro
{
	
	extern UBytePtr sauro_videoram2;
	extern UBytePtr sauro_colorram2;
	
	static int scroll1;
	static int scroll2;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	public static VhConvertColorPromPtr sauro_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	
	
	public static WriteHandlerPtr sauro_scroll1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		scroll1 = data;
	} };
	
	
	static int scroll2_map     [8] = {2, 1, 4, 3, 6, 5, 0, 7};
	static int scroll2_map_flip[8] = {0, 7, 2, 1, 4, 3, 6, 5};
	
	public static WriteHandlerPtr sauro_scroll2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int* map = (flip_screen ? scroll2_map_flip : scroll2_map);
	
		scroll2 = (data & 0xf8) | map[data & 7];
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr sauro_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,code,sx,sy,color,flipx;
	
	
		if (full_refresh != 0)
		{
			memset(dirtybuffer,1,videoram_size[0]);
		}
	
	
		/* for every character in the backround RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = 0; offs < videoram_size[0]; offs ++)
		{
			if (!dirtybuffer[offs]) continue;
	
			dirtybuffer[offs] = 0;
	
			code = videoram.read(offs)+ ((colorram.read(offs)& 0x07) << 8);
			sx = 8 * (offs / 32);
			sy = 8 * (offs % 32);
			color = (colorram.read(offs)>> 4) & 0x0f;
	
			flipx = colorram.read(offs)& 0x08;
	
			if (flip_screen != 0)
			{
				flipx = !flipx;
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			drawgfx(tmpbitmap,Machine.gfx[1],
					code,
					color,
					flipx,flip_screen,
					sx,sy,
					0,TRANSPARENCY_NONE,0);
		}
	
		if (!flip_screen)
		{
			int scroll = -scroll1;
			copyscrollbitmap(bitmap,tmpbitmap,1,&scroll ,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
		else
		{
			copyscrollbitmap(bitmap,tmpbitmap,1,&scroll1,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* draw the frontmost playfield. They are characters, but draw them as sprites */
		for (offs = 0; offs < videoram_size[0]; offs++)
		{
			code = sauro_videoram2[offs] + ((sauro_colorram2[offs] & 0x07) << 8);
	
			/* Skip spaces */
			if (code == 0x19) continue;
	
			sx = 8 * (offs / 32);
			sy = 8 * (offs % 32);
			color = (sauro_colorram2[offs] >> 4) & 0x0f;
	
			flipx = sauro_colorram2[offs] & 0x08;
	
			sx = (sx - scroll2) & 0xff;
	
			if (flip_screen != 0)
			{
				flipx = !flipx;
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					color,
					flipx,flip_screen,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		};
	
		/* Draw the sprites. The order is important for correct priorities */
	
		/* Weird, sprites entries don't start on DWORD boundary */
		for (offs = 3;offs < spriteram_size[0] - 1;offs += 4)
		{
			sy = spriteram.read(offs);
			if (sy == 0xf8) continue;
	
			code = spriteram.read(offs+1)+ ((spriteram.read(offs+3)& 0x03) << 8);
			sx = spriteram.read(offs+2);
			sy = 236 - sy;
			color = (spriteram.read(offs+3)>> 4) & 0x0f;
	
			/* I'm not really sure how this bit works */
			if (spriteram.read(offs+3)& 0x08)
			{
				if (sx > 0xc0)
				{
					/* Sign extend */
					sx = (signed int)(signed char)sx;
				}
			}
			else
			{
				if (sx < 0x40) continue;
			}
	
			flipx = spriteram.read(offs+3)& 0x04;
	
			if (flip_screen != 0)
			{
				flipx = !flipx;
				sx = (235 - sx) & 0xff;  /* The &0xff is not 100% percent correct */
				sy = 240 - sy;
			}
	
			drawgfx(bitmap, Machine.gfx[2],
					code,
					color,
					flipx,flip_screen,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
