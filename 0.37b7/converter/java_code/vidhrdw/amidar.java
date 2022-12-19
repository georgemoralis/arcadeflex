/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class amidar
{
	
	
	
	UBytePtr amidar_attributesram;
	
	
	static struct rectangle spritevisiblearea =
	{
		2*8+1, 32*8-1,
		2*8, 30*8-1
	};
	static struct rectangle spritevisibleareaflipx =
	{
		0*8, 30*8-2,
		2*8, 30*8-1
	};
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Amidar has one 32 bytes palette PROM, connected to the RGB output this
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
	public static VhConvertColorPromPtr amidar_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
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
			bit0 = (*color_prom >> 6) & 0x01;
			bit1 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x4f * bit0 + 0xa8 * bit1;
	
			color_prom++;
		}
	
	
		/* characters and sprites use the same palette */
		for (i = 0;i < TOTAL_COLORS(0);i++)
		{
			if ((i & 3) != 0) COLOR(0,i) = i;
			else COLOR(0,i) = 0;	/* 00 is always black, regardless of the contents of the PROM */
		}
	} };
	
	
	
	public static WriteHandlerPtr amidar_attributes_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 1) && amidar_attributesram[offset] != data)
		{
			int i;
	
	
			for (i = offset / 2;i < videoram_size[0];i += 32)
				dirtybuffer[i] = 1;
		}
	
		amidar_attributesram[offset] = data;
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr amidar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
	
		if (full_refresh != 0)
		{
			memset(dirtybuffer, 1, videoram_size[0]);
		}
	
	
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
	
				if (flip_screen_x != 0) sx = 31 - sx;
				if (flip_screen_y != 0) sy = 31 - sy;
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs),
						amidar_attributesram[2 * (offs % 32) + 1] & 0x07,
						flip_screen_x,flip_screen_y,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the temporary bitmap to the screen */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		/* Draw the sprites. Note that it is important to draw them exactly in this */
		/* order, to have the correct priorities. */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int flipx,flipy,sx,sy;
	
	
			sx = (spriteram.read(offs + 3)+ 1) & 0xff;	/* ??? */
			sy = 240 - spriteram.read(offs);
			flipx = spriteram.read(offs + 1)& 0x40;
			flipy = spriteram.read(offs + 1)& 0x80;
	
			if (flip_screen_x != 0)
			{
				sx = 241 - sx;	/* note: 241, not 240 */
				flipx = !flipx;
			}
			if (flip_screen_y != 0)
			{
				sy = 240 - sy;
				flipy = !flipy;
			}
	
			/* Sprites #0, #1 and #2 need to be offset one pixel to be correctly */
			/* centered on the ladders in Turtles (we move them down, but since this */
			/* is a rotated game, we actually move them left). */
			/* Note that the adjustement must be done AFTER handling flipscreen, thus */
			/* proving that this is a hardware related "feature" */
			if (offs <= 2*4) sy++;
	
			drawgfx(bitmap,Machine.gfx[1],
					spriteram.read(offs + 1)& 0x3f,
					spriteram.read(offs + 2)& 0x07,
					flipx,flipy,
					sx,sy,
					flip_screen_x ? &spritevisibleareaflipx : &spritevisiblearea,TRANSPARENCY_PEN,0);
		}
	} };
}
