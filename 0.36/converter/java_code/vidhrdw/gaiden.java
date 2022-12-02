/***************************************************************************

	Ninja Gaiden / Tecmo Knights Video Hardware

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

public class gaiden
{
	
	unsigned char *gaiden_videoram;
	unsigned char *gaiden_videoram2;
	unsigned char *gaiden_videoram3;
	
	public static VhStopPtr gaiden_vh_stop = new VhStopPtr() { public void handler() ;
	
	static struct tilemap *text_layer,*foreground,*background;
	static struct sprite_list *sprite_list;
	
	static const UINT16 *videoram1, *videoram2;
	static int gfxbank;
	
	static public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		int tile_index = row*32+col;
		SET_TILE_INFO(gfxbank,videoram1[tile_index] & 0x7ff,(videoram2[tile_index] & 0xf0) >> 4)
	} };
	
	static public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		int tile_index = row*64+col;
		SET_TILE_INFO(
			gfxbank,
			videoram1[tile_index] & 0xfff,
			(videoram2[tile_index] & 0xf0) >> 4
		)
	} };
	
	/********************************************************************************/
	
	#define NUMSPRITES 128
	
	/* sprite format:
	 *
	 *	word		bit					usage
	 * --------+-fedcba9876543210-+----------------
	 *    0    | ---------------x | flip x
	 *         | --------------x- | flip y
	 *         | -------------x-- | enable
	 *         | ----------x----- | flicker
	 *         | --------xx------ | sprite-tile priority
	 *    1    | xxxxxxxxxxxxxxxx | number
	 *    2    | --------xxxx---- | palette
	 *         | --------------xx | size: 8x8, 16x16, 32x32, 64x64
	 *    3    | xxxxxxxxxxxxxxxx | y position
	 *    4    | xxxxxxxxxxxxxxxx | x position
	 *    5,6,7|                  | unused
	 */
	
	/* get_sprite_info is the only routine to inspect spriteram */
	
	static void get_sprite_info( void ){
		const struct GfxElement *gfx = Machine.gfx[3];
		const unsigned short *source = (const UINT16 *)spriteram;
		struct sprite *sprite = sprite_list.sprite;
		int count = NUMSPRITES;
	
		int attributes, flags, number, color, span;
	
		while( count-- ){
			attributes = source[0];
			flags = 0;
	
			if ((attributes & 0x04) != 0){ /* visible */
				number = source[1]&0x7fff;
				color = source[2];
				flags |= SPRITE_VISIBLE;
				if ((attributes & 0x20) != 0) flags |= SPRITE_FLICKER;
	
				sprite.priority = (attributes>>6)&3;
				if( sprite.priority==3 ){
					flags |= SPRITE_TRANSPARENCY_THROUGH;
				//	sprite.priority = 2;
				}
	
				span = 8 << (color & 0x03);
	
				color = (color>>4)&0xf;
	
				sprite.y = source[3] & 0x1ff;
				sprite.x = source[4] & 0x1ff;
				/* wraparound - could be handled by Sprite Manager?*/
				if( sprite.x >= 256) sprite.x -= 512;
				if( sprite.y >= 256) sprite.y -= 512;
	
				sprite.total_width = sprite.total_height = span;
				sprite.tile_width = sprite.tile_height = 64;
				if ((attributes & 1) != 0) flags |= SPRITE_FLIPX;
				if ((attributes & 2) != 0) flags |= SPRITE_FLIPY;
	
				sprite.pal_data = &gfx.colortable[gfx.color_granularity * color];
				sprite.pen_usage = gfx.pen_usage[number/64];
	
				sprite.pen_data = gfx.gfxdata + (number/64) * gfx.char_modulo;
				sprite.x_offset = 0;
				sprite.y_offset = 0;
				if ((number & 0x01) != 0) sprite.x_offset += 8;
				if ((number & 0x02) != 0) sprite.y_offset += 8;
				if ((number & 0x04) != 0) sprite.x_offset += 16;
				if ((number & 0x08) != 0) sprite.y_offset += 16;
				if ((number & 0x10) != 0) sprite.x_offset += 32;
				if ((number & 0x20) != 0) sprite.y_offset += 32;
	
	//			if ((number & 0x01) != 0) sprite.pen_data += 8;
	//			if ((number & 0x02) != 0) sprite.pen_data += 8*64;
	//			if ((number & 0x04) != 0) sprite.pen_data += 16;
	//			if ((number & 0x08) != 0) sprite.pen_data += 16*64;
	//			if ((number & 0x10) != 0) sprite.pen_data += 32;
	//			if ((number & 0x20) != 0) sprite.pen_data += 32*64;
				sprite.line_offset = 64;
			}
			sprite.flags = flags;
			sprite++;
			source += 8;
		}
	}
	
	/********************************************************************************/
	
	public static VhStartPtr gaiden_vh_start = new VhStartPtr() { public int handler() 
	{
		sprite_list = sprite_list_create( NUMSPRITES, SPRITE_LIST_BACK_TO_FRONT );
	
		text_layer = tilemap_create(
			get_fg_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,	/* tile width, tile height */
			32,32	/* number of columns, number of rows */
		);
	
		foreground = tilemap_create(
			get_bg_tile_info,
			TILEMAP_TRANSPARENT,
			16,16,
			64,32
		);
	
		background = tilemap_create(
			get_bg_tile_info,
			0,
			16,16,
			64,32
		);
	
		if( sprite_list && text_layer && foreground && background ){
			sprite_list.sprite_type = SPRITE_TYPE_UNPACK;
			sprite_list.max_priority = 3;
			text_layer.transparent_pen = 0;
			foreground.transparent_pen = 0;
			palette_transparent_color = 0x200; /* background color */
			return 0;
		}
	
		return 1;
	} };
	
	public static VhStopPtr gaiden_vh_stop = new VhStopPtr() { public void handler() {
	} };
	
	/* scroll write handlers */
	
	public static WriteHandlerPtr gaiden_txscrollx_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrollx( text_layer,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_txscrolly_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrolly( text_layer,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_fgscrollx_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrollx( foreground,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_fgscrolly_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrolly( foreground,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_bgscrollx_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrollx( background,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_bgscrolly_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		static int oldword;
		oldword = COMBINE_WORD(oldword,data);
		tilemap_set_scrolly( background,0, oldword );
	} };
	
	public static WriteHandlerPtr gaiden_videoram3_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = READ_WORD(&gaiden_videoram3[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword){
			int tile_index = (offset/2)&0x7ff;
			WRITE_WORD(&gaiden_videoram3[offset],newword);
			tilemap_mark_tile_dirty( background,tile_index%64,tile_index/64 );
		}
	} };
	
	public static ReadHandlerPtr gaiden_videoram3_r = new ReadHandlerPtr() { public int handler(int offset){
	   return READ_WORD (&gaiden_videoram3[offset]);
	} };
	
	public static WriteHandlerPtr gaiden_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = READ_WORD(&gaiden_videoram2[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword){
			int tile_index = (offset/2)&0x7ff;
			WRITE_WORD(&gaiden_videoram2[offset],newword);
			tilemap_mark_tile_dirty(foreground,tile_index%64,tile_index/64 );
		}
	} };
	
	public static ReadHandlerPtr gaiden_videoram2_r = new ReadHandlerPtr() { public int handler(int offset){
	   return READ_WORD (&gaiden_videoram2[offset]);
	} };
	
	public static WriteHandlerPtr gaiden_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = READ_WORD(&gaiden_videoram[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword){
			int tile_index = (offset/2)&0x3ff;
			WRITE_WORD(&gaiden_videoram[offset],newword);
			tilemap_mark_tile_dirty(text_layer,tile_index%32,tile_index/32 );
		}
	} };
	
	public static ReadHandlerPtr gaiden_videoram_r = new ReadHandlerPtr() { public int handler(int offset){
		return READ_WORD (&gaiden_videoram[offset]);
	} };
	
	public static WriteHandlerPtr gaiden_spriteram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		COMBINE_WORD_MEM (&spriteram[offset],data);
	} };
	
	public static ReadHandlerPtr gaiden_spriteram_r = new ReadHandlerPtr() { public int handler(int offset){
		return READ_WORD (&spriteram[offset]);
	} };
	
	public static VhUpdatePtr gaiden_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		gfxbank = 1;
		videoram1 = (const unsigned short *)&gaiden_videoram3[0x1000];
		videoram2 = (const unsigned short *)gaiden_videoram3;
		tilemap_update(background);
	
		gfxbank = 2;
		videoram1 = (const unsigned short *)&gaiden_videoram2[0x1000];
		videoram2 = (const unsigned short *)gaiden_videoram2;
		tilemap_update(foreground);
	
		gfxbank = 0;
		videoram1 = (const unsigned short *)&gaiden_videoram[0x0800];
		videoram2 = (const unsigned short *)gaiden_videoram;
		tilemap_update(text_layer);
	
		get_sprite_info();
	
		palette_init_used_colors();
		sprite_update();
		{
			/* the following is required to make the colored background work */
			int i;
			for (i = 0;i < Machine.drv.total_colors;i += 16)
				palette_used_colors[i] = PALETTE_COLOR_TRANSPARENT;
		}
	
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
		tilemap_draw(bitmap,background,0);
		sprite_draw( sprite_list, 3); /* behind background (drawn with transparency_through) */
		sprite_draw( sprite_list, 2); /* between background and foreground */
		tilemap_draw(bitmap,foreground,0);
		sprite_draw( sprite_list, 1); /* ? */
		tilemap_draw(bitmap,text_layer,0);
		sprite_draw( sprite_list, 0); /* ? */
	} };
}
