/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class ladybug
{
	
	
	
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Lady Bug has a 32 bytes palette PROM and a 32 bytes sprite color lookup
	  table PROM.
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- inverter -- 220 ohm resistor  -- BLUE
	        -- inverter -- 220 ohm resistor  -- GREEN
	        -- inverter -- 220 ohm resistor  -- RED
	        -- inverter -- 470 ohm resistor  -- BLUE
	        -- unused
	        -- inverter -- 470 ohm resistor  -- GREEN
	        -- unused
	  bit 0 -- inverter -- 470 ohm resistor  -- RED
	
	***************************************************************************/
	public static VhConvertColorPromPtr ladybug_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
	
		for (i = 0;i < 32;i++)
		{
			int bit1,bit2;
	
	
			bit1 = (~color_prom.read(i)>> 0) & 0x01;
			bit2 = (~color_prom.read(i)>> 5) & 0x01;
			palette[3*i] = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (~color_prom.read(i)>> 2) & 0x01;
			bit2 = (~color_prom.read(i)>> 6) & 0x01;
			palette[3*i + 1] = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (~color_prom.read(i)>> 4) & 0x01;
			bit2 = (~color_prom.read(i)>> 7) & 0x01;
			palette[3*i + 2] = 0x47 * bit1 + 0x97 * bit2;
		}
	
		/* characters */
		for (i = 0;i < 8;i++)
		{
			colortable[4 * i] = 0;
			colortable[4 * i + 1] = i + 0x08;
			colortable[4 * i + 2] = i + 0x10;
			colortable[4 * i + 3] = i + 0x18;
		}
	
		/* sprites */
		for (i = 0;i < 4 * 8;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* low 4 bits are for sprite n */
			bit0 = (color_prom.read(i + 32)>> 3) & 0x01;
			bit1 = (color_prom.read(i + 32)>> 2) & 0x01;
			bit2 = (color_prom.read(i + 32)>> 1) & 0x01;
			bit3 = (color_prom.read(i + 32)>> 0) & 0x01;
			colortable[i + 4 * 8] = 1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3;
	
			/* high 4 bits are for sprite n + 8 */
			bit0 = (color_prom.read(i + 32)>> 7) & 0x01;
			bit1 = (color_prom.read(i + 32)>> 6) & 0x01;
			bit2 = (color_prom.read(i + 32)>> 5) & 0x01;
			bit3 = (color_prom.read(i + 32)>> 4) & 0x01;
			colortable[i + 4 * 16] = 1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3;
		}
	} };
	
	
	
	public static WriteHandlerPtr ladybug_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr ladybug_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i,offs;
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				if (flipscreen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ 32 * (colorram.read(offs)& 8),
						colorram.read(offs),
						flipscreen,flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int scroll[32];
			int sx,sy;
	
	
			for (offs = 0;offs < 32;offs++)
			{
				sx = offs % 4;
				sy = offs / 4;
	
				if (flipscreen != 0)
					scroll[31-offs] = -videoram.read(32 * sx + sy);
				else
					scroll[offs] = -videoram.read(32 * sx + sy);
			}
	
			copyscrollbitmap(bitmap,tmpbitmap,32,scroll,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* Draw the sprites. Note that it is important to draw them exactly in this */
		/* order, to have the correct priorities. */
		/* sprites in the columns 15, 1 and 0 are outside of the visible area */
		for (offs = spriteram_size[0] - 2*0x40;offs >= 2*0x40;offs -= 0x40)
		{
			i = 0;
			while (i < 0x40 && spriteram.read(offs + i)!= 0)
				i += 4;
	
			while (i > 0)
			{
	/*
	 abccdddd eeeeeeee fffghhhh iiiiiiii
	
	 a enable?
	 b size (0 = 8x8, 1 = 16x16)
	 cc flip
	 dddd y offset
	 eeeeeeee sprite code (shift right 2 bits for 16x16 sprites)
	 fff unknown
	 g sprite bank
	 hhhh color
	 iiiiiiii x position
	*/
				i -= 4;
	
				if (spriteram.read(offs + i)& 0x80)
				{
					if (spriteram.read(offs + i)& 0x40)	/* 16x16 */
						drawgfx(bitmap,Machine.gfx[1],
								(spriteram.read(offs + i + 1)>> 2) + 4 * (spriteram.read(offs + i + 2)& 0x10),
								spriteram.read(offs + i + 2)& 0x0f,
								spriteram.read(offs + i)& 0x20,spriteram.read(offs + i)& 0x10,
								spriteram.read(offs + i + 3),
								offs / 4 - 8 + (spriteram.read(offs + i)& 0x0f),
								&Machine.visible_area,TRANSPARENCY_PEN,0);
					else	/* 8x8 */
						drawgfx(bitmap,Machine.gfx[2],
								spriteram.read(offs + i + 1)+ 4 * (spriteram.read(offs + i + 2)& 0x10),
								spriteram.read(offs + i + 2)& 0x0f,
								spriteram.read(offs + i)& 0x20,spriteram.read(offs + i)& 0x10,
								spriteram.read(offs + i + 3),
								offs / 4 + (spriteram.read(offs + i)& 0x0f),
								&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	} };
}
