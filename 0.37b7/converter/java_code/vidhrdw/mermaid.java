/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class mermaid
{
	
	
	
	UBytePtr  mermaid_background_videoram;
	UBytePtr  mermaid_foreground_videoram;
	UBytePtr  mermaid_foreground_colorram;
	UBytePtr  mermaid_background_scrollram;
	UBytePtr  mermaid_foreground_scrollram;
	
	
	static struct rectangle spritevisiblearea =
	{
		0*8, 26*8-1,
		2*8, 30*8-1
	};
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  I'm not sure about the resistor value, I'm using the Galaxian ones.
	
	***************************************************************************/
	public static VhConvertColorPromPtr mermaid_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		int i;
	
		/* first, the char acter/sprite palette */
		for (i = 0;i < TOTAL_COLORS(0); i++)
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
	
		/* blue background */
		*(palette++) = 0;
		*(palette++) = 0;
		*(palette++) = 0xff;
	
		/* set up background palette */
	    COLOR(2,0) = 32;
	    COLOR(2,1) = 33;
	
	    COLOR(2,2) = 64;
	    COLOR(2,3) = 33;
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr mermaid_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		/* for every character in the backround RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = 0; offs < videoram_size[0]; offs ++)
		{
			int code,sx,sy;
	
	
			sy = 8 * (offs / 32);
			sx = 8 * (offs % 32);
	
			code = mermaid_background_videoram[offs];
	
			drawgfx(tmpbitmap,Machine.gfx[2],
					code,
					(sx >= 26*8) ? 0 : 1,
					0,0,
					sx,sy,
					0,TRANSPARENCY_NONE,0);
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int i, scroll[32];
	
	
			for (i = 0;i < 32;i++)
			{
				scroll[i] = -mermaid_background_scrollram[i];
			}
	
	
			copyscrollbitmap(bitmap,tmpbitmap,0,0,32,scroll,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* draw the front layer. They are characters, but draw them as sprites */
		for (offs = 0; offs < videoram_size[0]; offs ++)
		{
			int code,sx,sy;
	
	
			sy = 8 * (offs / 32);
			sx =     (offs % 32);
	
			sy = (sy - mermaid_foreground_scrollram[sx]) & 0xff;
	
			code = mermaid_foreground_videoram[offs] | ((mermaid_foreground_colorram[offs] & 0x30) << 4);
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					mermaid_foreground_colorram[offs] & 0x0f,
					0,0,
					8*sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* draw the sprites */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
	#ifdef MAME_DEBUG
			extern int debug_key_pressed;
	#endif
			UINT8 flipx,flipy,sx,sy,code,bank = 0;
	
	
			sx = spriteram.read(offs + 3)+ 1;
			sy = 240 - spriteram.read(offs + 1);
			flipx = spriteram.read(offs + 0)& 0x40;
			flipy = spriteram.read(offs + 0)& 0x80;
	
			/* this doesn't look correct. Oh really? Maybe there is a PROM. */
			switch (spriteram.read(offs + 2)& 0xf0)
			{
			case 0x00:  bank = 2; break;
			case 0x10:  bank = 1; break;
			case 0x20:  bank = 2; break;
			case 0x30:  bank = 3; break;
			case 0x50:  bank = 1; break;
			case 0x60:  bank = 2; break;
			case 0x80:  bank = 0; break;
			case 0x90:  bank = 3; break;
			case 0xa0:  bank = 2; break;
			case 0xb0:  bank = 3; break;
	#ifdef MAME_DEBUG
			default:  debug_key_pressed = 1; break;
	#endif
			}
	
			code = (spriteram.read(offs + 0)& 0x3f) | (bank << 6);
	
			drawgfx(bitmap,Machine.gfx[1],
					code,
					spriteram.read(offs + 2)& 0x0f,
					flipx, flipy,
					sx, sy,
					&spritevisiblearea,TRANSPARENCY_PEN,0);
		}
	} };
}
