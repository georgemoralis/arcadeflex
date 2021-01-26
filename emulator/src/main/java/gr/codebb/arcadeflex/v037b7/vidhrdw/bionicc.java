/***************************************************************************

Bionic Commando Video Hardware

This board handles tile/tile and tile/sprite priority with a PROM. Its
working is complicated and hardcoded in the driver.

The PROM is a 256x4 chip, with address inputs wired as follows:

A0 bg opaque
A1 \
A2 |  fg pen
A3 |
A4 /
A5 fg has priority over sprites (bit 5 of tile attribute)
A6 fg has not priority over bg (bits 6 & 7 of tile attribute both set)
A7 sprite opaque

The output selects the active layer, it can be:
0  bg
1  fg
2  sprite

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.SubArrays.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_CPU1;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class bionicc
{
	
	public static UBytePtr bionicc_fgvideoram = new UBytePtr();
	public static UBytePtr bionicc_bgvideoram = new UBytePtr();
	public static UBytePtr bionicc_txvideoram = new UBytePtr();
	
	static tilemap tx_tilemap, bg_tilemap, fg_tilemap;
	static int flipscreen;
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
                int offset = 2*(row*64+col);
                UShortPtr videoram1 = new UShortPtr(bionicc_bgvideoram);
		int attr = videoram1.read(offset+1);
		SET_TILE_INFO(1,(videoram1.read(offset) & 0xff) + ((attr & 0x07) << 8),(attr & 0x18) >> 3);
		tile_info.flags = (char) TILE_FLIPXY((attr & 0xc0) >> 6);
            }
        };
       
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
		UShortPtr videoram1 = new UShortPtr(bionicc_fgvideoram);
		
		int offset = 2*(row*64 + col);
                int attr = videoram1.read(offset+1);
                
                SET_TILE_INFO(2,(videoram1.read(offset) & 0xff) + ((attr & 0x07) << 8),(attr & 0x18) >> 3);
                if ((attr & 0xc0) == 0xc0)
                {
                        tile_info.priority = 2;
                        tile_info.flags = 0;
                }
                else
                {
                        tile_info.priority = (char) ((attr & 0x20) >> 5);
                        tile_info.flags = (char) TILE_FLIPXY((attr & 0xc0) >> 6);
                }
                
	} };
	
	public static WriteHandlerPtr get_tx_tile_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
		UShortPtr videoram1 = new UShortPtr(bionicc_txvideoram);
		
                int offset = row*32+col;
                int attr = new UBytePtr(bionicc_txvideoram, 0x800).read(offset);
                SET_TILE_INFO(0,(videoram1.read(offset) & 0xff) + ((attr & 0x00c0) << 2),attr & 0x3f);
                
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr bionicc_vh_start = new VhStartPtr() { public int handler() 
	{
		tx_tilemap = tilemap_create(
		get_tx_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		32,32
                );
                fg_tilemap = tilemap_create(
                        get_fg_tile_info,
                        TILEMAP_TRANSPARENT,
                        16,16,
                        64,64
                );
                bg_tilemap = tilemap_create(
                        get_bg_tile_info,
                        TILEMAP_TRANSPARENT,
                        8,8,
                        64,64
                );

                if (fg_tilemap!=null && bg_tilemap!=null && tx_tilemap!=null)
                {
                        tx_tilemap.transparent_pen = 3;
                        fg_tilemap.transparent_pen = 15;
/*TODO*///                        fg_tilemap.transmask[0] = 0xffff; /* split type 0 is completely transparent in front half */
/*TODO*///                        fg_tilemap.transmask[1] = 0xffc1; /* split type 1 has pens 1-5 opaque in front half */
                        bg_tilemap.transparent_pen = 15;

                        return 0;
                }

                return 1;

	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr bionicc_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = bionicc_bgvideoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			int tile_index = offset/4;
			bionicc_bgvideoram.WRITE_WORD(offset,newword);
			tilemap_mark_tile_dirty(bg_tilemap,tile_index,0);
		}
	} };
	
	public static WriteHandlerPtr bionicc_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = bionicc_fgvideoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			int tile_index = offset/4;
			bionicc_fgvideoram.WRITE_WORD(offset,newword);
			tilemap_mark_tile_dirty(fg_tilemap,tile_index,0);
		}
	} };
	
	public static WriteHandlerPtr bionicc_txvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = bionicc_txvideoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			int tile_index = (offset&0x7ff)/2;
			bionicc_txvideoram.WRITE_WORD(offset,newword);
			tilemap_mark_tile_dirty(tx_tilemap,tile_index,0);
		}
	} };
	
	public static ReadHandlerPtr bionicc_bgvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bionicc_bgvideoram.READ_WORD(offset);
	} };
	
	public static ReadHandlerPtr bionicc_fgvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bionicc_fgvideoram.READ_WORD(offset);
	} };
	
	public static ReadHandlerPtr bionicc_txvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bionicc_txvideoram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr bionicc_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		paletteram_RRRRGGGGBBBBIIII_word_w.handler(offset,(data & 0xfff1) | ((data & 0x0007) << 1));
	} };
	
	public static WriteHandlerPtr bionicc_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch( offset )
		{
			case 0:
				tilemap_set_scrollx(fg_tilemap,0,data);
				break;
			case 2:
				tilemap_set_scrolly(fg_tilemap,0,data);
				break;
			case 4:
				tilemap_set_scrollx(bg_tilemap,0,data);
				break;
			case 6:
				tilemap_set_scrolly(bg_tilemap,0,data);
				break;
		}
	} };
	
	public static WriteHandlerPtr bionicc_gfxctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data >>= 8;
	
		flipscreen = data & 1;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		tilemap_set_enable(bg_tilemap,data & 0x20);	/* guess */
		tilemap_set_enable(fg_tilemap,data & 0x10);	/* guess */
	
		coin_counter_w.handler(0,data & 0x80);
		coin_counter_w.handler(1,data & 0x40);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void bionicc_draw_sprites( osd_bitmap bitmap )
	{
		int offs;
		GfxElement gfx = Machine.gfx[3];
		rectangle clip = new rectangle(Machine.visible_area);
	
		for (offs = spriteram_size[0]-8;offs >= 0;offs -= 8)
		{
			int tile_number = buffered_spriteram.READ_WORD(offs)&0x7ff;
			if( tile_number!=0x7FF ){
				int attr = buffered_spriteram.READ_WORD(offs+2);
				int color = (attr&0x3C)>>2;
				int flipx = attr&0x02;
				int flipy = 0;
				int sx = buffered_spriteram.READ_WORD(offs+6);
				int sy = buffered_spriteram.READ_WORD(offs+4);
				if(sy>512-16) sy-=512;
				if (flipscreen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = flipx!=0?0:1;
					flipy = flipy!=0?0:1;
				}
	
				drawgfx( bitmap, gfx,
					tile_number,
					color,
					flipx,flipy,
					sx,sy,
					clip,TRANSPARENCY_PEN,15);
			}
		}
	}
	
	public static void mark_sprite_colors()
	{
		int offs, code, color, i, pal_base;
		int[] colmask=new int[16];
	
		pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
		for(i=0;i<16;i++) colmask[i] = 0;
	
		for (offs = 0; offs < 0x500;offs += 8)
		{
	
			code = buffered_spriteram.READ_WORD(offs) & 0x7ff;
			if( code != 0x7FF ) {
				color = (buffered_spriteram.READ_WORD(offs+2) & 0x3c) >> 2;
				colmask[color] |= Machine.gfx[3].pen_usage[code];
			}
		}
	
		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 15;i++)
			{
				if ((colmask[color] & (1 << i)) != 0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	}
	
	public static VhUpdatePtr bionicc_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		//fg_preupdate(); 
                tilemap_update(fg_tilemap);
                //bg_preupdate(); 
                tilemap_update(bg_tilemap);
                //tx_preupdate(); 
                tilemap_update(tx_tilemap);

                palette_init_used_colors();
                mark_sprite_colors();
                palette_used_colors.write(0, palette_used_colors.read(0) | PALETTE_COLOR_VISIBLE);

                if (palette_recalc() != null)
                        tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

                tilemap_render(ALL_TILEMAPS);

                fillbitmap(bitmap,Machine.pens[0],Machine.drv.visible_area);
                tilemap_draw(bitmap,fg_tilemap,2);
                tilemap_draw(bitmap,bg_tilemap,0);
                tilemap_draw(bitmap,fg_tilemap,0);
                bionicc_draw_sprites(bitmap);
                tilemap_draw(bitmap,fg_tilemap,1);
                tilemap_draw(bitmap,tx_tilemap,0);
	} };
	
	public static VhEofCallbackPtr bionicc_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram_w.handler(0,0);
	} };
}
