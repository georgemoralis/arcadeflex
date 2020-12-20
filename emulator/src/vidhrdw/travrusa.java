/***************************************************************************

  vidhrdw.c

  Traverse USA

L Taylor
J Clegg

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */ 
package vidhrdw;

import static platform.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static platform.libc_old.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.common.*;
import static mame.inputport.*;
import static platform.ptrlib.*;

public class travrusa
{
	
	public static UBytePtr travrusa_videoram=new UBytePtr();
	
	static tilemap bg_tilemap;
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Traverse USA has one 256x8 character palette PROM (some versions have two
	  256x4), one 32x8 sprite palette PROM, and one 256x4 sprite color lookup
	  table PROM.
	
	  I don't know for sure how the palette PROMs are connected to the RGB
	  output, but it's probably something like this; note that RED and BLUE
	  are swapped wrt the usual configuration.
	
	  bit 7 -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	  bit 0 -- 1  kohm resistor  -- BLUE
	
	***************************************************************************/
	static int TOTAL_COLORS(int gfxn) 
        {
            return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
        }
        public static VhConvertColorPromPtr travrusa_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
                int p_inc=0;
		/* character palette */
		for (i = 0;i < 128;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = 0;
			bit1 = (color_prom.read() >> 6) & 0x01;
			bit2 = (color_prom.read() >> 7) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
			/* green component */
			bit0 = (color_prom.read() >> 3) & 0x01;
			bit1 = (color_prom.read() >> 4) & 0x01;
			bit2 = (color_prom.read() >> 5) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
			/* blue component */
			bit0 = (color_prom.read() >> 0) & 0x01;
			bit1 = (color_prom.read() >> 1) & 0x01;
			bit2 = (color_prom.read() >> 2) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
	
			color_prom.inc();
		}
	
		/* skip bottom half - not used */
		color_prom.inc(128);
	
		/* sprite palette */
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = 0;
			bit1 = (color_prom.read() >> 6) & 0x01;
			bit2 = (color_prom.read() >> 7) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
			/* green component */
			bit0 = (color_prom.read() >> 3) & 0x01;
			bit1 = (color_prom.read() >> 4) & 0x01;
			bit2 = (color_prom.read() >> 5) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
			/* blue component */
			bit0 = (color_prom.read() >> 0) & 0x01;
			bit1 = (color_prom.read() >> 1) & 0x01;
			bit2 = (color_prom.read() >> 2) & 0x01;
			palette[p_inc++].set((char)(0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
	
			color_prom.inc();
		}
	
		/* color_prom now points to the beginning of the lookup table */
	
		/* character lookup table */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char)i;
	
		/* sprite lookup table */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char)((color_prom.readinc()) + 128);
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 2*(64*row+col);
		/*unsigned*/ char attr = travrusa_videoram.read(tile_index+1);
		SET_TILE_INFO(0,travrusa_videoram.read(tile_index) + ((attr & 0xc0) << 2),attr & 0x0f);
		tile_info.flags = 0;
		if ((attr & 0x20) != 0) tile_info.flags |= TILE_FLIPX;
		if ((attr & 0x10) != 0) tile_info.flags |= TILE_FLIPY;
		if ((attr & 0x0f) == 0x0f) tile_info.flags |= TILE_SPLIT(1);	/* hack */
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr travrusa_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(
			get_bg_tile_info,
			TILEMAP_SPLIT,
			8,8,
			64,32
		);
	
		if (bg_tilemap != null)
		{
			bg_tilemap.transmask[0] = 0xff; /* split type 0 is totally transparent in front half */
			bg_tilemap.transmask[1] = 0x3f; /* split type 1 has pens 6 and 7 opaque - hack! */
	
			tilemap_set_scroll_rows(bg_tilemap,32);
	
			return 0;
		}
	
		return 1;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr travrusa_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (travrusa_videoram.read(offset) != data)
		{
			travrusa_videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap,(offset/2)%64,(offset/2)/64);
		}
	} };
	
	
	static int[] scrollx=new int[2];
	
	static void set_scroll()
	{
		int i;
	
		for (i = 0;i < 24;i++)
			tilemap_set_scrollx(bg_tilemap,i,scrollx[0] + 256 * scrollx[1]);
		for (i = 24;i < 32;i++)
			tilemap_set_scrollx(bg_tilemap,i,0);
	}
	
	public static WriteHandlerPtr travrusa_scroll_x_low_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scrollx[0] = data;
		set_scroll();
	} };
	
	public static WriteHandlerPtr travrusa_scroll_x_high_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scrollx[1] = data;
		set_scroll();
	} };
	
	
	public static WriteHandlerPtr travrusa_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* screen flip is handled both by software and hardware */
		data ^= ~readinputport(4) & 1;
	
		flipscreen = data & 1;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		coin_counter_w.handler(0,data & 0x02);
		coin_counter_w.handler(1,data & 0x20);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
		static rectangle spritevisiblearea = new rectangle
		(
			1*8, 31*8-1,
			0*8, 24*8-1
                );
		static rectangle spritevisibleareaflip = new rectangle
		(
			1*8, 31*8-1,
			8*8, 32*8-1
                );
	static void draw_sprites(osd_bitmap bitmap)
	{
		int offs;

	
	
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
	
			sx = ((spriteram.read(offs + 3) + 8) & 0xff) - 8;
			sy = 240 - spriteram.read(offs);
			flipx = spriteram.read(offs + 1) & 0x40;
			flipy = spriteram.read(offs + 1) & 0x80;
			if (flipscreen != 0)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					spriteram.read(offs + 2),
					spriteram.read(offs + 1) & 0x0f,
					flipx,flipy,
					sx,sy,
					flipscreen!=0 ? spritevisibleareaflip : spritevisiblearea,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VhUpdatePtr travrusa_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_BACK);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT);
	} };
}
