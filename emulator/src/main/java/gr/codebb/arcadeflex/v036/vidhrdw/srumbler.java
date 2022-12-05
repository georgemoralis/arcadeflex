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

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;


public class srumbler
{
		
	public static UBytePtr srumbler_backgroundram=new UBytePtr();
        public static UBytePtr srumbler_foregroundram=new UBytePtr();
	static tilemap bg_tilemap,fg_tilemap;
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 2*(row+col*32);
		/*unsigned*/ char attr = srumbler_foregroundram.read(tile_index);
		SET_TILE_INFO(0,srumbler_foregroundram.read(tile_index + 1) + ((attr & 0x03) << 8),(attr & 0x3c) >> 2);
		tile_info.flags = (attr & 0x40)!=0 ? (char)TILE_IGNORE_TRANSPARENCY : 0;
	} };
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = 2*(row+col*64);
		/*unsigned*/ char attr = srumbler_backgroundram.read(tile_index);
		SET_TILE_INFO(1,srumbler_backgroundram.read(tile_index + 1) + ((attr & 0x07) << 8),(attr & 0xe0) >> 5);
		tile_info.flags = (char)TILE_SPLIT((attr & 0x10) >> 4);
		if ((attr & 0x08) != 0) tile_info.flags |= TILE_FLIPY;
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr srumbler_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap = tilemap_create(get_fg_tile_info,TILEMAP_TRANSPARENT, 8, 8,64,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,TILEMAP_SPLIT,      16,16,64,64);
	
		if (fg_tilemap==null || bg_tilemap==null)
			return 1;
	
		fg_tilemap.transparent_pen = 3;
	
		bg_tilemap.transmask[0] = 0xffff; /* split type 0 is totally transparent in front half */
		bg_tilemap.transmask[1] = 0x07ff; /* split type 1 has pens 0-10 transparent in front half */
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr srumbler_foreground_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (srumbler_foregroundram.read(offset) != data)
		{
			srumbler_foregroundram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap,(offset/2)/32,(offset/2)%32);
		}
	} };
	
	public static WriteHandlerPtr srumbler_background_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (srumbler_backgroundram.read(offset) != data)
		{
			srumbler_backgroundram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap,(offset/2)/64,(offset/2)%64);
		}
	} };
	
	
	public static WriteHandlerPtr srumbler_4009_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 0 flips screen */
		flipscreen = data & 1;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		/* bits 4-5 used during attract mode, unknown */
	
		/* bits 6-7 coin counters */
		coin_counter_w.handler(0,data & 0x40);
		coin_counter_w.handler(1,data & 0x80);
	} };
	
	static int[] scroll_w=new int[4];
	public static WriteHandlerPtr srumbler_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		
	
		scroll_w[offset] = data;
	
		tilemap_set_scrollx(bg_tilemap,0,scroll_w[0] | (scroll_w[1] << 8));
		tilemap_set_scrolly(bg_tilemap,0,scroll_w[2] | (scroll_w[3] << 8));
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		int offs;
	
		/* Draw the sprites. */
		for (offs = spriteram_size[0]-4; offs>=0;offs -= 4)
		{
			/* SPRITES
			=====
			Attribute
			0x80 Code MSB
			0x40 Code MSB
			0x20 Code MSB
			0x10 Colour
			0x08 Colour
			0x04 Colour
			0x02 y Flip
			0x01 X MSB
			*/
	
	
			int code,colour,sx,sy,flipy;
			int attr=spriteram.read(offs+1);
			code = spriteram.read(offs);
			code += ( (attr&0xe0) << 3 );
			colour = (attr & 0x1c)>>2;
			sy = spriteram.read(offs + 2);
			sx = spriteram.read(offs + 3) + 0x100 * ( attr & 0x01);
			flipy = attr & 0x02;
	
			if (flipscreen != 0)
			{
				sx = 496 - sx;
				sy = 240 - sy;
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine.gfx[2],
					code,
					colour,
					flipscreen,flipy,
					sx, sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,15);
		}
	}
	
	
	public static VhUpdatePtr srumbler_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_BACK);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,bg_tilemap,TILEMAP_FRONT);
		tilemap_draw(bitmap,fg_tilemap,0);
	} };
}
