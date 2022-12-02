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

public class lastduel
{
	
	unsigned char *lastduel_vram,*lastduel_scroll2,*lastduel_scroll1;
	static int scroll[16];
	
	static struct tilemap *background_tilemap,*foreground_tilemap,*fix_tilemap;
	static unsigned char *gfx_base;
	static int gfx_bank,flipscreen;
	
	public static WriteHandlerPtr lastduel_flip_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		flipscreen=data&1;
		coin_lockout_w(0,~data & 0x10);
		coin_lockout_w(1,~data & 0x20);
	} };
	
	public static WriteHandlerPtr lastduel_scroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll[offset]=data&0xffff;  /* Scroll data */
	} };
	
	public static ReadHandlerPtr lastduel_scroll1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&lastduel_scroll1[offset]);
	} };
	
	public static ReadHandlerPtr lastduel_scroll2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&lastduel_scroll2[offset]);
	} };
	
	public static WriteHandlerPtr lastduel_scroll1_w = new WriteHandlerPtr() { public void handler(int offset, int value)
	{
		COMBINE_WORD_MEM(&lastduel_scroll1[offset],value);
		tilemap_mark_tile_dirty(foreground_tilemap,(offset/4)%64,(offset/4)/64);
	} };
	
	public static WriteHandlerPtr lastduel_scroll2_w = new WriteHandlerPtr() { public void handler(int offset, int value)
	{
		COMBINE_WORD_MEM(&lastduel_scroll2[offset],value);
		tilemap_mark_tile_dirty(background_tilemap,(offset/4)%64,(offset/4)/64);
	} };
	
	public static ReadHandlerPtr lastduel_vram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&lastduel_vram[offset]);
	} };
	
	public static WriteHandlerPtr lastduel_vram_w = new WriteHandlerPtr() { public void handler(int offset, int value)
	{
	 	COMBINE_WORD_MEM(&lastduel_vram[offset],value);
		tilemap_mark_tile_dirty(fix_tilemap,(offset/2)%64,(offset/2)/64);
	} };
	
	public static WriteHandlerPtr madgear_scroll1_w = new WriteHandlerPtr() { public void handler(int offset, int value)
	{
		COMBINE_WORD_MEM(&lastduel_scroll1[offset],value);
	
		offset&=0xfff;
		tilemap_mark_tile_dirty(foreground_tilemap,(offset/2)/32,(offset/2)%32);
	} };
	
	public static WriteHandlerPtr madgear_scroll2_w = new WriteHandlerPtr() { public void handler(int offset, int value)
	{
		COMBINE_WORD_MEM(&lastduel_scroll2[offset],value);
	
		offset&=0xfff;
		tilemap_mark_tile_dirty(background_tilemap,(offset/2)/32,(offset/2)%32);
	} };
	
	static public static WriteHandlerPtr get_ld_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs=(col*4) + (row*256);
		int tile=READ_WORD(&gfx_base[offs])&0x1fff;
		int color=READ_WORD(&gfx_base[offs+2]);
	
		SET_TILE_INFO(gfx_bank,tile,color&0xf)
		tile_info.flags = TILE_FLIPYX((color & 0x60)>>5);
		tile_info.priority = (color&0x80)>>7;
	} };
	
	static public static WriteHandlerPtr get_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs=(row*2) + (col*64);
		int tile=READ_WORD(&gfx_base[offs])&0x1fff;
		int color=READ_WORD(&gfx_base[offs+0x1000]);
	
		SET_TILE_INFO(gfx_bank,tile,color&0xf)
		tile_info.flags = TILE_FLIPYX((color & 0x60)>>5);
		tile_info.priority = (color&0x10)>>4;
	} };
	
	static public static WriteHandlerPtr get_fix_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int offs=(col*2) + (row*128);
		int tile=READ_WORD(&lastduel_vram[offs]);
	
		SET_TILE_INFO(1,tile&0xfff,tile>>12)
	} };
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr lastduel_vh_start = new VhStartPtr() { public int handler() 
	{
		background_tilemap = tilemap_create(
			get_ld_tile_info,
			0,
			16,16,
			64,64 /* 1024 by 1024 */
		);
	
		foreground_tilemap = tilemap_create(
			get_ld_tile_info,
			TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
			16,16,
			64,64
		);
	
		fix_tilemap = tilemap_create(
			get_fix_info,
			TILEMAP_TRANSPARENT,
			8,8,
			64,32
		);
	
		tilemap_set_scroll_rows(background_tilemap,1);
		tilemap_set_scroll_cols(background_tilemap,1);
		tilemap_set_scroll_rows(foreground_tilemap,1);
		tilemap_set_scroll_cols(foreground_tilemap,1);
		tilemap_set_scroll_rows(fix_tilemap,0);
		tilemap_set_scroll_cols(fix_tilemap,0);
		foreground_tilemap.transparent_pen = 0;
		foreground_tilemap.transmask[0] = 0x007f;
		foreground_tilemap.transmask[1] = 0xff10;
		fix_tilemap.transparent_pen = 3;
	
		return 0;
	} };
	
	public static VhStartPtr madgear_vh_start = new VhStartPtr() { public int handler() 
	{
		background_tilemap = tilemap_create(
			get_tile_info,
			0,
			16,16,
			64,32 /* 1024 by 512 */
		);
	
		foreground_tilemap = tilemap_create(
			get_tile_info,
			TILEMAP_TRANSPARENT | TILEMAP_SPLIT,
			16,16,
			64,32
		);
	
		fix_tilemap = tilemap_create(
			get_fix_info,
			TILEMAP_TRANSPARENT,
			8,8,
			64,32
		);
	
		tilemap_set_scroll_rows(background_tilemap,1);
		tilemap_set_scroll_cols(background_tilemap,1);
		tilemap_set_scroll_rows(foreground_tilemap,1);
		tilemap_set_scroll_cols(foreground_tilemap,1);
		tilemap_set_scroll_rows(fix_tilemap,0);
		tilemap_set_scroll_cols(fix_tilemap,0);
		foreground_tilemap.transparent_pen = 15;
		foreground_tilemap.transmask[0] = 0x80ff;
		foreground_tilemap.transmask[1] = 0x7f00;
		fix_tilemap.transparent_pen = 3;
	
		return 0;
	} };
	
	/***************************************************************************/
	
	public static VhUpdatePtr lastduel_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i,offs,color,code;
		int colmask[16];
		unsigned int *pen_usage; /* Save some struct derefs */
		static int last_flip;
	
		if (flipscreen!=last_flip)
			tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		last_flip=flipscreen;
	
		/* Update tilemaps */
		tilemap_set_scrollx( background_tilemap,0, scroll[6] );
		tilemap_set_scrolly( background_tilemap,0, scroll[4] );
		tilemap_set_scrollx( foreground_tilemap,0, scroll[2] );
		tilemap_set_scrolly( foreground_tilemap,0, scroll[0] );
	
		gfx_bank=2;
		gfx_base=lastduel_scroll2;
		tilemap_update(background_tilemap);
	
		gfx_bank=3;
		gfx_base=lastduel_scroll1;
		tilemap_update(foreground_tilemap);
		tilemap_update(fix_tilemap);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
	
		pen_usage= Machine.gfx[0].pen_usage;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for(offs=0x500-8;offs>-1;offs-=8)
		{
			int attributes = READ_WORD(&buffered_spriteram[offs+2]);
			code=READ_WORD(&buffered_spriteram[offs]);
			color = attributes&0xf;
	
			colmask[color] |= pen_usage[code];
		}
		for (color = 0;color < 16;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[32*16 + 16 * color +15] = PALETTE_COLOR_TRANSPARENT;
			for (i = 0;i < 15;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[32*16 + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		/* Check for complete remap and redirty if needed */
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		/* Draw playfields */
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,background_tilemap,0);
	
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_BACK | 0);
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_BACK | 1);
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_FRONT | 0);
	
		/* Sprites */
		for(offs=0x500-8;offs>=0;offs-=8)
		{
			int attributes,sy,sx,flipx,flipy;
			code=READ_WORD(&buffered_spriteram[offs]);
			if (!code) continue;
	
			attributes = READ_WORD(&buffered_spriteram[offs+2]);
			sy = READ_WORD(&buffered_spriteram[offs+4]) & 0x1ff;
			sx = READ_WORD(&buffered_spriteram[offs+6]) & 0x1ff;
	
			flipx = attributes&0x20;
			flipy = attributes&0x40;
			color = attributes&0xf;
	
			if( sy>0x100 )
				sy -= 0x200;
	
			if (flipscreen != 0) {
				sx=384+128-16-sx;
				sy=240-sy;
				if (flipx != 0) flipx=0; else flipx=1;
				if (flipy != 0) flipy=0; else flipy=1;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
				code,
				color,
				flipx,flipy,
				sx,sy,
				&Machine.drv.visible_area,
				TRANSPARENCY_PEN,15);
		}
	
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_FRONT | 1);
	
		tilemap_draw(bitmap,fix_tilemap,0);
	} };
	
	public static VhUpdatePtr ledstorm_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i,offs,color,code;
		int colmask[16];
		unsigned int *pen_usage; /* Save some struct derefs */
		static int last_flip;
	
		if (flipscreen!=last_flip)
			tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		last_flip=flipscreen;
	
		/* Update tilemaps */
		tilemap_set_scrollx( background_tilemap,0, scroll[6] );
		tilemap_set_scrolly( background_tilemap,0, scroll[4] );
		tilemap_set_scrollx( foreground_tilemap,0, scroll[2] );
		tilemap_set_scrolly( foreground_tilemap,0, scroll[0] );
	
		gfx_bank=2;
		gfx_base=lastduel_scroll2;
		tilemap_update(background_tilemap);
	
		gfx_bank=3;
		gfx_base=lastduel_scroll1;
		tilemap_update(foreground_tilemap);
		tilemap_update(fix_tilemap);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
	
		pen_usage= Machine.gfx[0].pen_usage;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for(offs=0x800-8;offs>-1;offs-=8)
		{
			int attributes = READ_WORD(&buffered_spriteram[offs+2]);
			code=READ_WORD(&buffered_spriteram[offs]);
			color = attributes&0xf;
	
			colmask[color] |= pen_usage[code];
		}
		for (color = 0;color < 16;color++)
		{
			if (colmask[color] & (1 << 0))
				palette_used_colors[32*16 + 16 * color +15] = PALETTE_COLOR_TRANSPARENT;
			for (i = 0;i < 15;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[32*16 + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		/* Check for complete remap and redirty if needed */
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		/* Draw playfields */
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,background_tilemap,0);
	
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_BACK | 0);
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_BACK | 1);
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_FRONT | 0);
	
		/* Sprites */
		for(offs=0x800-8;offs>=0;offs-=8)
		{
			int attributes,sy,sx,flipx,flipy;
			sy = READ_WORD(&buffered_spriteram[offs+4]) & 0x1ff;
			if (sy==0x180) continue;
	
			code=READ_WORD(&buffered_spriteram[offs]);
			attributes = READ_WORD(&buffered_spriteram[offs+2]);
			sx = READ_WORD(&buffered_spriteram[offs+6]) & 0x1ff;
	
			flipx = attributes&0x20;
			flipy = attributes&0x80; /* Different from Last Duel */
			color = attributes&0xf;
	
			if( sy>0x100 )
				sy -= 0x200;
	
			if (flipscreen != 0) {
				sx=384+128-16-sx;
				sy=240-sy;
				if (flipx != 0) flipx=0; else flipx=1;
				if (flipy != 0) flipy=0; else flipy=1;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
				code,
				color,
				flipx,flipy,
				sx,sy,
				&Machine.drv.visible_area,
				TRANSPARENCY_PEN,15);
		}
	
		tilemap_draw(bitmap,foreground_tilemap,TILEMAP_FRONT | 1);
	
		tilemap_draw(bitmap,fix_tilemap,0);
	} };
	
	
	void lastduel_eof_callback(void)
	{
		/* Spriteram is always 1 frame ahead, suggesting buffering.  I can't find
			a register to control this so I assume it happens automatically
			every frame at the end of vblank */
		buffer_spriteram_w(0,0);
	}
}
