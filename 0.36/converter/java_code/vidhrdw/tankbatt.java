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

public class tankbatt
{
	
	unsigned char *tankbatt_bulletsram;
	int tankbatt_bulletsram_size;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	public static VhConvertColorPromPtr tankbatt_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		#define RES_1	0xc0 /* this is a guess */
		#define RES_2	0x3f /* this is a guess */
	
		/* Stick black in there */
		*(palette++) = 0;
		*(palette++) = 0;
		*(palette++) = 0;
	
		/* ? Skip the first byte ? */
		color_prom++;
	
		for (i = 1;i < Machine.drv.total_colors;i++)
		{
			int bit0, bit1, bit2, bit3;
	
			bit0 = (*color_prom >> 0) & 0x01; /* intensity */
			bit1 = (*color_prom >> 1) & 0x01; /* red */
			bit2 = (*color_prom >> 2) & 0x01; /* green */
			bit3 = (*color_prom >> 3) & 0x01; /* blue */
	
			/* red component */
			*(palette) = RES_1 * bit1;
			if (bit1 != 0) *(palette) += RES_2 * bit0;
			palette++;
			/* green component */
			*(palette) = RES_1 * bit2;
			if (bit2 != 0) *(palette) += RES_2 * bit0;
			palette++;
			/* blue component */
			*(palette) = RES_1 * bit3;
			if (bit3 != 0) *(palette) += RES_2 * bit0;
			palette++;
	
			color_prom += 4;
		}
	
		for (i = 0;i < 128;i++)
		{
			colortable[i++] = 0;
			colortable[i] = (i/2) + 1;
		}
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr tankbatt_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 1; offs >= 0; offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram[offs],
						(videoram[offs]) >> 2,
						0,0,
						8*sx,8*sy,
						&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* Draw the bullets */
		for (offs = 0;offs < tankbatt_bulletsram_size;offs += 2)
		{
			int x,y;
			int color;
	
	
			color = 63;	/* cyan, same color as the tanks */
	
			x = tankbatt_bulletsram[offs + 1];
			y = 255 - tankbatt_bulletsram[offs] - 2;
	
			drawgfx(bitmap,Machine.gfx[1],
					0,	/* this is just a square, generated by the hardware */
					color,
					0,0,
					x,y,
					&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
	} };
	
}
