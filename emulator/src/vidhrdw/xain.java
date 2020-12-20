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

import static platform.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static platform.ptrlib.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.memoryH.*;
import static mame.sndintrf.*;
import static mame.cpuintrf.*;
import static mame.inputport.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.common.*;
import static mame.paletteH.*;
import static platform.libc_old.*;

public class xain
{
	
	public static UBytePtr xain_charram=new UBytePtr();
        public static UBytePtr xain_bgram0=new UBytePtr();
        public static UBytePtr xain_bgram1=new UBytePtr();
	
	static tilemap char_tilemap, bgram0_tilemap, bgram1_tilemap;
	public static int flipscreen;
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_bgram0_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = (col & 0xf)|((col & 0x10)<<4)|((row & 0xf)<<4)|((row & 0x10)<<5);
		int attr = xain_bgram0.read(addr | 0x400);
		SET_TILE_INFO(2,xain_bgram0.read(addr) | ((attr & 7) << 8),(attr & 0x70) >> 4);
		tile_info.flags = (attr & 0x80)!=0 ? (char)TILE_FLIPX : 0;
	} };
	
	public static WriteHandlerPtr get_bgram1_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = (col & 0xf)|((col & 0x10)<<4)|((row & 0xf)<<4)|((row & 0x10)<<5);
		int attr = xain_bgram1.read(addr | 0x400);
		SET_TILE_INFO(1,xain_bgram1.read(addr) | ((attr & 7) << 8),(attr & 0x70) >> 4);
		tile_info.flags = (attr & 0x80)!=0 ? (char)TILE_FLIPX : 0;
	} };
	
	public static WriteHandlerPtr get_char_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int addr = col + row*32;
		int attr = xain_charram.read(addr | 0x400);
		SET_TILE_INFO(0,xain_charram.read(addr) | ((attr & 3) << 8),(attr & 0xe0) >> 5);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr xain_vh_start = new VhStartPtr() { public int handler() 
	{
		bgram0_tilemap = tilemap_create(
				get_bgram0_tile_info,
				TILEMAP_OPAQUE,
				16,16,
				32,32);
		bgram1_tilemap = tilemap_create(
				get_bgram1_tile_info,
				TILEMAP_TRANSPARENT,
				16,16,
				32,32);
		char_tilemap = tilemap_create(
				get_char_tile_info,
				TILEMAP_TRANSPARENT,
				8,8,
				32,32);
	
		if (bgram0_tilemap==null || bgram1_tilemap==null || char_tilemap==null)
			return 1;
	
		bgram1_tilemap.transparent_pen = 0;
		char_tilemap.transparent_pen = 0;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr xain_bgram0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (xain_bgram0.read(offset) != data)
		{
			xain_bgram0.write(offset,data);
			tilemap_mark_tile_dirty (bgram0_tilemap,
					 ((offset>>4)&0x10)|(offset&0xf),
					 ((offset>>5)&0x10)|((offset>>4)&0xf));
		}
	} };
	
	public static WriteHandlerPtr xain_bgram1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (xain_bgram1.read(offset) != data)
		{
			xain_bgram1.write(offset,data);
			tilemap_mark_tile_dirty (bgram1_tilemap,
					 ((offset>>4)&0x10)|(offset&0xf),
					 ((offset>>5)&0x10)|((offset>>4)&0xf));
		}
	} };
	
	public static WriteHandlerPtr xain_charram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (xain_charram.read(offset) != data)
		{
			xain_charram.write(offset,data);
			tilemap_mark_tile_dirty (char_tilemap, offset & 0x1f, (offset & 0x3e0) >> 5);
		}
	} };
	static char[] xain_scrollxP0=new char[2];
	public static WriteHandlerPtr xain_scrollxP0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		xain_scrollxP0[offset] = (char)(data & 0xFF);
		tilemap_set_scrollx(bgram0_tilemap, 0, xain_scrollxP0[0]|(xain_scrollxP0[1]<<8));
	} };
	static char[] xain_scrollyP0=new char[2];
	public static WriteHandlerPtr xain_scrollyP0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{

		xain_scrollyP0[offset] = (char)(data & 0xFF);
		tilemap_set_scrolly(bgram0_tilemap, 0, xain_scrollyP0[0]|(xain_scrollyP0[1]<<8));
	} };
	static char[] xain_scrollxP1=new char[2];
	public static WriteHandlerPtr xain_scrollxP1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{

	
		xain_scrollxP1[offset] = (char)(data & 0xFF);
		tilemap_set_scrollx(bgram1_tilemap, 0, xain_scrollxP1[0]|(xain_scrollxP1[1]<<8));
	} };
	static char[] xain_scrollyP1=new char[2];
	public static WriteHandlerPtr xain_scrollyP1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		xain_scrollyP1[offset] = (char)data;
		tilemap_set_scrolly(bgram1_tilemap, 0, xain_scrollyP1[0]|(xain_scrollyP1[1]<<8));
	} };
	
	
	public static WriteHandlerPtr xain_flipscreen_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		flipscreen = data & 1;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		int offs;
	
		for (offs = 0; offs < spriteram_size[0];offs += 4)
		{
			int sx,sy,flipx;
			int attr = spriteram.read(offs+1);
			int numtile = spriteram.read(offs+2) | ((attr & 7) << 8);
			int color = (attr & 0x38) >> 3;
	
			sx = 239 - spriteram.read(offs+3);
			if (sx <= -7) sx += 256;
			sy = 240 - spriteram.read(offs);
			if (sy <= -7) sy += 256;
			flipx = attr & 0x40;
			if (flipscreen != 0)
			{
				sx = 239 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
			}
	
			if ((attr & 0x80) != 0)	/* double height */
			{
				drawgfx(bitmap,Machine.gfx[3],
						numtile,
						color,
						flipx,flipscreen,
						sx-1,flipscreen!=0?sy+16:sy-16,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				drawgfx(bitmap,Machine.gfx[3],
						numtile+1,
						color,
						flipx,flipscreen,
						sx-1,sy,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
			else
			{
				drawgfx(bitmap,Machine.gfx[3],
						numtile,
						color,
						flipx,flipscreen,
						sx,sy,
						Machine.drv.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VhUpdatePtr xain_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		//memset(palette_used_colors+128,PALETTE_COLOR_USED,128);	/* sprites */
                for(int i=0; i<128; i++)
                {
                    palette_used_colors.write(i+128,PALETTE_COLOR_USED);
                }
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bgram0_tilemap,0);
		tilemap_draw(bitmap,bgram1_tilemap,0);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,char_tilemap,0);
	} };
}
