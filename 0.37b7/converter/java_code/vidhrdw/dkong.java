/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class dkong
{
	
	
	static data_t gfx_bank,palette_bank;
	static int grid_on;
	static const UBytePtr color_codes;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Donkey Kong has two 256x4 palette PROMs and one 256x4 PROM which contains
	  the color codes to use for characters on a per row/column basis (groups of
	  of 4 characters in the same column - actually row, since the display is
	  rotated)
	  The palette PROMs are connected to the RGB output this way:
	
	  bit 3 -- 220 ohm resistor -- inverter  -- RED
	        -- 470 ohm resistor -- inverter  -- RED
	        -- 1  kohm resistor -- inverter  -- RED
	  bit 0 -- 220 ohm resistor -- inverter  -- GREEN
	  bit 3 -- 470 ohm resistor -- inverter  -- GREEN
	        -- 1  kohm resistor -- inverter  -- GREEN
	        -- 220 ohm resistor -- inverter  -- BLUE
	  bit 0 -- 470 ohm resistor -- inverter  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr dkong_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (color_prom.read(256)>> 1) & 1;
			bit1 = (color_prom.read(256)>> 2) & 1;
			bit2 = (color_prom.read(256)>> 3) & 1;
			*(palette++) = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* green component */
			bit0 = (color_prom.read(0)>> 2) & 1;
			bit1 = (color_prom.read(0)>> 3) & 1;
			bit2 = (color_prom.read(256)>> 0) & 1;
			*(palette++) = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* blue component */
			bit0 = (color_prom.read(0)>> 0) & 1;
			bit1 = (color_prom.read(0)>> 1) & 1;
			*(palette++) = 255 - (0x55 * bit0 + 0xaa * bit1);
	
			color_prom++;
		}
	
		color_prom += 256;
		/* color_prom now points to the beginning of the character color codes */
		color_codes = color_prom;	/* we'll need it later */
	} };
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Donkey Kong 3 has two 512x8 palette PROMs and one 256x4 PROM which contains
	  the color codes to use for characters on a per row/column basis (groups of
	  of 4 characters in the same column - actually row, since the display is
	  rotated)
	  Interstingly, bytes 0-255 of the palette PROMs contain an inverted palette,
	  as other Nintendo games like Donkey Kong, while bytes 256-511 contain a non
	  inverted palette. This was probably done to allow connection to both the
	  special Nintendo and a standard monitor.
	  I don't know the exact values of the resistors between the PROMs and the
	  RGB output, but they are probably the usual:
	
	  bit 7 -- 220 ohm resistor -- inverter  -- RED
	        -- 470 ohm resistor -- inverter  -- RED
	        -- 1  kohm resistor -- inverter  -- RED
	        -- 2.2kohm resistor -- inverter  -- RED
	        -- 220 ohm resistor -- inverter  -- GREEN
	        -- 470 ohm resistor -- inverter  -- GREEN
	        -- 1  kohm resistor -- inverter  -- GREEN
	  bit 0 -- 2.2kohm resistor -- inverter  -- GREEN
	
	  bit 3 -- 220 ohm resistor -- inverter  -- BLUE
	        -- 470 ohm resistor -- inverter  -- BLUE
	        -- 1  kohm resistor -- inverter  -- BLUE
	  bit 0 -- 2.2kohm resistor -- inverter  -- BLUE
	
	***************************************************************************/
	public static VhConvertColorPromPtr dkong3_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 4) & 0x01;
			bit1 = (color_prom.read(0)>> 5) & 0x01;
			bit2 = (color_prom.read(0)>> 6) & 0x01;
			bit3 = (color_prom.read(0)>> 7) & 0x01;
			*(palette++) = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			/* green component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			/* blue component */
			bit0 = (color_prom.read(256)>> 0) & 0x01;
			bit1 = (color_prom.read(256)>> 1) & 0x01;
			bit2 = (color_prom.read(256)>> 2) & 0x01;
			bit3 = (color_prom.read(256)>> 3) & 0x01;
			*(palette++) = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			color_prom++;
		}
	
		color_prom += 256;
		/* color_prom now points to the beginning of the character color codes */
		color_codes = color_prom;	/* we'll need it later */
	} };
	
	
	
	public static VhStartPtr dkong_vh_start = new VhStartPtr() { public int handler() 
	{
		gfx_bank = 0;
		palette_bank = 0;
	
		return generic_vh_start();
	} };
	
	
	
	public static WriteHandlerPtr dkongjr_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		set_vh_global_attribute(&gfx_bank, data & 1);
	} };
	
	public static WriteHandlerPtr dkong3_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		set_vh_global_attribute(&gfx_bank, ~data & 1);
	} };
	
	
	
	public static WriteHandlerPtr dkong_palettebank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int newbank;
	
	
		newbank = palette_bank;
		if ((data & 1) != 0)
			newbank |= 1 << offset;
		else
			newbank &= ~(1 << offset);
	
		set_vh_global_attribute(&palette_bank, newbank);
	} };
	
	public static WriteHandlerPtr radarscp_grid_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		grid_on = data & 1;
	} };
	
	public static WriteHandlerPtr radarscp_grid_color_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r,g,b;
	
		r = ((~data >> 0) & 0x01) * 0xff;
		g = ((~data >> 1) & 0x01) * 0xff;
		b = ((~data >> 2) & 0x01) * 0xff;
	//	palette_change_color(257,r,g,b);
		palette_change_color(257,0x00,0x00,0xff);
	} };
	
	public static WriteHandlerPtr dkong_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_w(0,~data & 1);
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void draw_tiles(struct osd_bitmap *bitmap)
	{
		int offs;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
				int charcode,color;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				charcode = videoram.read(offs)+ 256 * gfx_bank;
				/* retrieve the character color from the PROM */
				color = (color_codes[offs % 32 + 32 * (offs / 32 / 4)] & 0x0f) + 0x10 * palette_bank;
	
				if (flip_screen != 0)
				{
					sx = 31 - sx;
					sy = 31 - sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						charcode,color,
						flip_screen,flip_screen,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	}
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			if (spriteram.read(offs))
			{
				/* spriteram.read(offs + 2)& 0x40 is used by Donkey Kong 3 only */
				/* spriteram.read(offs + 2)& 0x30 don't seem to be used (they are */
				/* probably not part of the color code, since Mario Bros, which */
				/* has similar hardware, uses a memory mapped port to change */
				/* palette bank, so it's limited to 16 color codes) */
	
				int x,y;
	
				x = spriteram.read(offs + 3)- 8;
				y = 240 - spriteram.read(offs)+ 7;
	
				if (flip_screen != 0)
				{
					x = 240 - x;
					y = 240 - y;
	
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + 2 * (spriteram.read(offs + 2)& 0x40),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							!(spriteram.read(offs + 2)& 0x80),!(spriteram.read(offs + 1)& 0x80),
							x,y,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
	
					/* draw with wrap around - this fixes the 'beheading' bug */
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + 2 * (spriteram.read(offs + 2)& 0x40),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x-256,y,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				else
				{
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + 2 * (spriteram.read(offs + 2)& 0x40),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x,y,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
	
					/* draw with wrap around - this fixes the 'beheading' bug */
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + 2 * (spriteram.read(offs + 2)& 0x40),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x+256,y,
							&Machine.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	static void draw_grid(struct osd_bitmap *bitmap)
	{
		const UBytePtr table = memory_region(REGION_GFX3);
		int x,y,counter;
	
		counter = flip_screen ? 0x000 : 0x400;
	
		x = Machine.visible_area.min_x;
		y = Machine.visible_area.min_y;
		while (y <= Machine.visible_area.max_y)
		{
			x = 4 * (table[counter] & 0x7f);
			if (x >= Machine.visible_area.min_x &&
					x <= Machine.visible_area.max_x)
			{
				if (table[counter] & 0x80)	/* star */
				{
					if (rand() & 1)	/* noise coming from sound board */
						plot_pixel(bitmap,x,y,Machine.pens[256]);
				}
				else if (grid_on != 0)			/* radar */
					plot_pixel(bitmap,x,y,Machine.pens[257]);
			}
	
			counter++;
	
			if (x >= 4 * (table[counter] & 0x7f))
				y++;
		}
	}
	
	public static VhUpdatePtr radarscp_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_change_color(256,0xff,0x00,0x00);	/* stars */
	
		if (palette_recalc() || full_refresh)
			memset(dirtybuffer,1,videoram_size[0]);
	
		draw_tiles(bitmap);
		draw_grid(bitmap);
		draw_sprites(bitmap);
	} };
	
	public static VhUpdatePtr dkong_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
			memset(dirtybuffer,1,videoram_size[0]);
	
		draw_tiles(bitmap);
		draw_sprites(bitmap);
	} };
}
