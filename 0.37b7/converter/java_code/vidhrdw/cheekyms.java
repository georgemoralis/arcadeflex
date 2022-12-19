/*************************************************************************
 Universal Cheeky Mouse Driver
 (c)Lee Taylor May 1998, All rights reserved.

 For use only in offical Mame releases.
 Not to be distrabuted as part of any commerical work.
***************************************************************************
Functions to emulate the video hardware of the machine.
***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class cheekyms
{
	
	
	static int redraw_man = 0;
	static int man_scroll = -1;
	static int sprites[0x20];
	static data_t char_palette = 0;
	
	
	public static VhConvertColorPromPtr cheekyms_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i,j,bit;
	
		for (i = 0; i < 3; i++)
		{
			const UBytePtr  color_prom_save = color_prom;
	
			/* lower nibble */
			for (j = 0;j < Machine.drv.total_colors/6;j++)
			{
				/* red component */
				bit = (color_prom.read(0)>> 0) & 0x01;
				*(palette++) = 0xff * bit;
				/* green component */
				bit = (color_prom.read(0)>> 1) & 0x01;
				*(palette++) = 0xff * bit;
				/* blue component */
				bit = (color_prom.read(0)>> 2) & 0x01;
				*(palette++) = 0xff * bit;
	
				color_prom++;
			}
	
			color_prom = color_prom_save;
	
			/* upper nibble */
			for (j = 0;j < Machine.drv.total_colors/6;j++)
			{
				/* red component */
				bit = (color_prom.read(0)>> 4) & 0x01;
				*(palette++) = 0xff * bit;
				/* green component */
				bit = (color_prom.read(0)>> 5) & 0x01;
				*(palette++) = 0xff * bit;
				/* blue component */
				bit = (color_prom.read(0)>> 6) & 0x01;
				*(palette++) = 0xff * bit;
	
				color_prom++;
			}
		}
	} };
	
	
	public static WriteHandlerPtr cheekyms_sprite_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		sprites[offset] = data;
	} };
	
	
	public static WriteHandlerPtr cheekyms_port_40_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int last_dac = -1;
	
		/* The lower bits probably trigger sound samples */
	
		if (last_dac != (data & 0x80))
		{
			last_dac = data & 0x80;
	
			DAC_data_w(0, last_dac ? 0x80 : 0);
		}
	} };
	
	
	public static WriteHandlerPtr cheekyms_port_80_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int new_man_scroll;
	
		/* Bits 0-1 Sound enables, not sure which bit is which */
	
		/* Bit 2 is interrupt enable */
		interrupt_enable_w(offset, data & 0x04);
	
		/* Bit 3-5 Man scroll amount */
	    new_man_scroll = (data >> 3) & 0x07;
		if (man_scroll != new_man_scroll)
		{
			man_scroll = new_man_scroll;
			redraw_man = 1;
		}
	
		/* Bit 6 is palette select (Selects either 0 = PROM M8, 1 = PROM M9) */
		set_vh_global_attribute(&char_palette, (data >> 2) & 0x10);
	
		/* Bit 7 is screen flip */
		flip_screen_w(offset, data & 0x80);
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr cheekyms_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (full_refresh != 0)
		{
			memset(dirtybuffer, 1, videoram_size[0]);
		}
	
	
		fillbitmap(bitmap,Machine.pens[0],&Machine.visible_area);
	
		/* Draw the sprites first, because they're supposed to appear below
		   the characters */
		for (offs = 0; offs < sizeof(sprites); offs += 4)
		{
			int v1, sx, sy, col, code;
	
			v1  = sprites[offs + 0];
			sy  = sprites[offs + 1];
			sx  = 256 - sprites[offs + 2];
			col = (sprites[offs + 3] & 0x07);
	
			if (!(sprites[offs + 3] & 0x08)) continue;
	
			code = (~v1 << 1) & 0x1f;
	
			if ((v1 & 0x80) != 0)
			{
				if (!flip_screen)
				{
					code++;
				}
	
				drawgfx(bitmap,Machine.gfx[1],
						code,col,
						0,0,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
			else
			{
				drawgfx(bitmap,Machine.gfx[1],
						code + 0x20,
						col,
						0,0,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
	
				drawgfx(bitmap,Machine.gfx[1],
						code + 0x21,
						col,
						0,0,
						sx + 8*(v1 & 2),sy + 8*(~v1 & 2),
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy,man_area;
	
			sx = offs % 32;
			sy = offs / 32;
	
			if (flip_screen != 0)
			{
				man_area = ((sy >=  5) && (sy <= 25) && (sx >=  8) && (sx <= 12));
			}
			else
			{
				man_area = ((sy >=  6) && (sy <= 26) && (sx >=  8) && (sx <= 12));
			}
	
			if (dirtybuffer[offs] ||
				(redraw_man && man_area))
			{
				dirtybuffer[offs] = 0;
	
				if (flip_screen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs),
						0 + char_palette,
						flip_screen,flip_screen,
						8*sx, 8*sy - (man_area ? man_scroll : 0),
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		redraw_man = 0;
	
		/* copy the temporary bitmap to the screen over the sprites */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_PEN,Machine.pens[4*char_palette]);
	} };
}
