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

public class spacefb
{
	
	
	static int video_control;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Space FB has one 32 bytes palette PROM, connected to the RGB output this
	  way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static VhConvertColorPromPtr spacefb_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
			bit0 = (color_prom[i] >> 0) & 0x01;
			bit1 = (color_prom[i] >> 1) & 0x01;
			bit2 = (color_prom[i] >> 2) & 0x01;
			palette[3*i + 0] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = (color_prom[i] >> 3) & 0x01;
			bit1 = (color_prom[i] >> 4) & 0x01;
			bit2 = (color_prom[i] >> 5) & 0x01;
			palette[3*i + 1] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			bit0 = 0;
			bit1 = (color_prom[i] >> 6) & 0x01;
			bit2 = (color_prom[i] >> 7) & 0x01;
			if (bit1 | bit2)
				bit0 = 1;
			palette[3*i + 2] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		}
	
		for (i = 0;i < 4 * 8;i++)
		{
			if ((i & 3) != 0) colortable[i] = i;
			else colortable[i] = 0;
		}
	} };
	
	
	public static WriteHandlerPtr spacefb_video_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		video_control = data;
	} };
	
	
	public static WriteHandlerPtr spacefb_port_2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	if (errorlog != 0) fprintf(errorlog,"Port #2 = %02d\n",data);
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhUpdatePtr spacefb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int spriteno, col_bit2, flipscreen;
	
	
	
		/* Clear the bitmap */
		fillbitmap(bitmap,Machine.pens[0],&Machine.drv.visible_area);
	
		/* Draw the sprite/chars */
		spriteno = (video_control & 0x20) ? 0x80 : 0x00;
		flipscreen = video_control & 0x01;
	
		/* A4 of the color PROM, depending on a jumper setting, can either come
		   from the CREF line or from sprite memory. CREF is the default
		   according to the schematics */
		col_bit2 = (video_control & 0x40) ? 0x04 : 0x00;
	
		for (offs = 0; offs < 128; offs++, spriteno++)
		{
			int sx,sy,code,cnt,col;
	
	
			sx = 255 - videoram[spriteno];
			sy = videoram[spriteno+0x100];
	
			code = videoram[spriteno+0x200];
			cnt  = videoram[spriteno+0x300];
	
			col = (~cnt & 0x03) | col_bit2;
	
			if (cnt != 0)
			{
				if ((cnt & 0x20) != 0)
				{
					/* Draw bullets */
	
					if (flipscreen != 0)
					{
						sx = 260 - sx;
						sy = 252 - sy;
					}
	
					drawgfx(bitmap,Machine.gfx[1],
							code & 0x3f,
							col,
							flipscreen,flipscreen,
							sx,sy,
							&Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
				}
				else if ((cnt & 0x40) != 0)
				{
					sy -= 5;	/* aligns the spaceship and the bullet */
	
					if (flipscreen != 0)
					{
						sx = 256 - sx;
						sy = 248 - sy;
					}
	
					drawgfx(bitmap,Machine.gfx[0],
							255 - code,
							col,
							flipscreen,flipscreen,
							sx,sy,
							&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
				}
			}
		}
	
	} };
}
