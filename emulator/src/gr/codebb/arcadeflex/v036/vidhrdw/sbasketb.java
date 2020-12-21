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

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class sbasketb
{
	
	
	
	public static UBytePtr sbasketb_scroll=new UBytePtr();
	public static UBytePtr sbasketb_palettebank=new UBytePtr();
	public static UBytePtr sbasketb_spriteram_select=new UBytePtr();
	
	static int flipscreen = 0;
	static rectangle scroll_area = new rectangle( 0*8, 32*8-1, 0*8, 32*8-1 );
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Super Basketball has three 256x4 palette PROMs (one per gun) and two 256x4
	  lookup table PROMs (one for characters, one for sprites).
	  I don't know for sure how the palette PROMs are connected to the RGB
	  output, but it's probably the usual:
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
        static int TOTAL_COLORS(int gfxn) {
            return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
        }
	public static VhConvertColorPromPtr sbasketb_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
                int p_inc = 0;
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0) >> 0) & 0x01;
			bit1 = (color_prom.read(0) >> 1) & 0x01;
			bit2 = (color_prom.read(0) >> 2) & 0x01;
			bit3 = (color_prom.read(0) >> 3) & 0x01;
			palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
			bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
			palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
			bit0 = (color_prom.read(2*Machine.drv.total_colors) >> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine.drv.total_colors) >> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine.drv.total_colors) >> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine.drv.total_colors) >> 3) & 0x01;
			palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
	
			color_prom.inc();
		}
	
		color_prom.inc(2*Machine.drv.total_colors);
		/* color_prom now points to the beginning of the character lookup table */
	
	
		/* characters use colors 240-255 */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 240);
	
		/* sprites use colors 0-256 (?) in 16 banks */
		for (i = 0;i < TOTAL_COLORS(1)/16;i++)
		{
			int j;
	
	
			for (j = 0;j < 16;j++)
                        {
                            colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i+ j * TOTAL_COLORS(1)/16]=(char)((color_prom.read() & 0x0f) + 16 * j);
                        }
			color_prom.inc();
		}
	} };
	
	
	public static WriteHandlerPtr sbasketb_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (flipscreen != data)
		{
			flipscreen = data;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr sbasketb_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,i;
		int sx,sy,code,color,flipx,flipy;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs]!=0)
			{
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				code  = videoram.read(offs) + ((colorram.read(offs) & 0x20) << 3);
				color = colorram.read(offs) & 0x0f;
				flipx = colorram.read(offs) & 0x40;
				flipy = colorram.read(offs) & 0x80;
	
				if (flipscreen != 0)
				{
					flipx = NOT(flipx);
					flipy = NOT(flipy);
	
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						code, color,
						flipx, flipy,
						8*sx,8*sy,
						scroll_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int[] scroll=new int[32];
	
			if (flipscreen==0)
			{
				for (i = 0;i < 6;i++)
					scroll[i] = 0;
	
				for (i = 6;i < 32;i++)
					scroll[i] = -sbasketb_scroll.read() - 1;
			}
			else
			{
				for (i = 26;i < 32;i++)
					scroll[i] = 0;
	
				for (i = 0;i < 26;i++)
					scroll[i] = sbasketb_scroll.read() + 1;
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,0,null,32,scroll,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
		/* Draw the sprites */
		offs = (sbasketb_spriteram_select.read() & 0x01) * 0x100;
	
		for (i = 0; i < 64; i++, offs += 4)
		{
			sx = spriteram.read(offs + 2);
			sy = spriteram.read(offs + 3);
	
			if (sx!=0 || sy!=0)
			{
				code  =  spriteram.read(offs + 0) | ((spriteram.read(offs + 1) & 0x20) << 3);
				color = (spriteram.read(offs + 1) & 0x0f) + 16 * sbasketb_palettebank.read();
				flipx =  spriteram.read(offs + 1) & 0x40;
				flipy =  spriteram.read(offs + 1) & 0x80;
	
				if (flipscreen != 0)
				{
					flipx = NOT(flipx);
					flipy = NOT(flipy);
	
					sx = 240 - sx;
					sy = 240 - sy;
				}
	
				drawgfx(bitmap,Machine.gfx[1],
						code, color,
						flipx, flipy,
						sx, sy,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	} };
}
