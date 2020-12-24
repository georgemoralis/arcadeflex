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
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.galaxian.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.drivers.zodiack.*;



public class zodiack
{
	public static UBytePtr zodiack_videoram2=new UBytePtr();
	

	static int flipscreen;
	
	public static VhConvertColorPromPtr zodiack_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
                int p_inc=0;
		/* first, the character/sprite palette */
		for (i = 0;i < Machine.drv.total_colors-1; i++)
		{
			int bit0,bit1,bit2;
	
			/* red component */
			bit0 = (color_prom.read() >> 0) & 0x01;
			bit1 = (color_prom.read() >> 1) & 0x01;
			bit2 = (color_prom.read() >> 2) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* green component */
			bit0 = (color_prom.read() >> 3) & 0x01;
			bit1 = (color_prom.read() >> 4) & 0x01;
			bit2 = (color_prom.read() >> 5) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read() >> 6) & 0x01;
			bit2 = (color_prom.read() >> 7) & 0x01;
			palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
	
			color_prom.inc();
		}
	
		/* white for bullets */
		palette[p_inc++]=(char) (0xff);
		palette[p_inc++]=(char) (0xff);
		palette[p_inc++]=(char) (0xff);
	
	
		for (i = 0;i < TOTAL_COLORS(0);i+=2)
		{
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char)(32 + (i / 2));
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i+1] = (char)(40 + (i / 2));
		}
	
		for (i = 0;i < TOTAL_COLORS(3);i++)
		{
			if ((i & 3) == 0)  colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char)0;
		}
	
		/* bullet */
		colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 0] = (char)0;
		colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 1] = (char)48;
	} };
	
	
	public static WriteHandlerPtr zodiac_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (flipscreen != (NOT(data)))
		{
			flipscreen = NOT(data);
	
			memset(dirtybuffer, 1, videoram_size[0]);
		}
	} };
	
	
	public static WriteHandlerPtr zodiac_control_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* Bit 0-1 - coin counters */
		coin_counter_w.handler(0, data & 0x02);
		coin_counter_w.handler(1, data & 0x01);
	
		/* Bit 2 - ???? */
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VhUpdatePtr zodiack_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* draw the background characters */
		for (offs = 0; offs < videoram_size[0]; offs++)
		{
			int code,sx,sy,col;
	
	
			if (dirtybuffer[offs]==0)  continue;
	
			dirtybuffer[offs] = 0;
	
	
			sy = offs / 32;
			sx = offs % 32;
	
			col = galaxian_attributesram.read(2 * sx + 1) & 0x07;
	
			if (flipscreen != 0)
			{
				sx = 31 - sx;
				sy = 31 - sy;
			}
	
			code = videoram.read(offs);
	
			drawgfx(tmpbitmap,Machine.gfx[3],
					code,
					col,
					flipscreen, flipscreen,
					8*sx, 8*sy,
					null,TRANSPARENCY_NONE,0);
		}
	
	
		/* draw the foreground characters */
		for (offs = 0; offs < videoram_size[0]; offs++)
		{
			int code,sx,sy,col;
	
	
			sy = offs / 32;
			sx = offs % 32;
	
			col = (galaxian_attributesram.read(2 * sx + 1) >> 4) & 0x07;
	
			if (flipscreen != 0)
			{
				sy = 31 - sy;
				sx = 31 - sx;
			}
	
			code = zodiack_videoram2.read(offs);
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					col,
					flipscreen, flipscreen,
					8*sx, 8*sy,
					Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int i;
                        int[] scroll=new int[32];
	
	
			if (flipscreen != 0)
			{
				for (i = 0;i < 32;i++)
				{
					scroll[31-i] = galaxian_attributesram.read(2 * i);
				}
			}
			else
			{
				for (i = 0;i < 32;i++)
				{
					scroll[i] = -galaxian_attributesram.read(2 * i);
				}
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,0,null,32,scroll,Machine.drv.visible_area,TRANSPARENCY_COLOR,0);
		}
	
	
		/* draw the bullets */
		for (offs = 0;offs < galaxian_bulletsram_size[0];offs += 4)
		{
			int x,y;
	
	
			x = galaxian_bulletsram.read(offs + 3) + Machine.drv.gfxdecodeinfo[2].gfxlayout.width;
			y = galaxian_bulletsram.read(offs + 1);
	
			if (percuss_hardware==0)
			{
				y = 255 - y;
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					0,	/* this is just a dot, generated by the hardware */
					0,
					0,0,
					x,y,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* draw the sprites */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int flipx,flipy,sx,sy,spritecode;
	
	
			sx = 240 - spriteram.read(offs + 3);
			sy = 240 - spriteram.read(offs);
			flipx = NOT((spriteram.read(offs + 1) & 0x40));
			flipy =   spriteram.read(offs + 1) & 0x80;
			spritecode = spriteram.read(offs + 1) & 0x3f;
	
			if (percuss_hardware != 0)
			{
				sy = 240 - sy;
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					spritecode,
					spriteram.read(offs + 2) & 0x07,
					flipx,flipy,
					sx,sy,
					//flipscreen[0] ? &spritevisibleareaflipx : &spritevisiblearea,TRANSPARENCY_PEN,0);
					//&spritevisiblearea,TRANSPARENCY_PEN,0);
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
