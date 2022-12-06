/***************************************************************************

  Video Hardware for Armed Formation and Terra Force and Kodure Ookami

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.SubArrays.*;
import static common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_CPU1;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class armedf
{
	
	public static int scroll_type;
	
	public static int armedf_vreg;
	
	public static UBytePtr armedf_bg_videoram=new UBytePtr();
	public static int armedf_bg_scrollx;
	public static int armedf_bg_scrolly;
	
	public static UBytePtr armedf_fg_videoram=new UBytePtr();
	public static int armedf_fg_scrollx;
	public static int armedf_fg_scrolly;
	
	public static int terraf_scroll_msb;
	
	public static tilemap background, foreground, text_layer;
	
	/******************************************************************/
	
	
	public static WriteHandlerPtr get_text_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		UShortPtr source = new UShortPtr(videoram, 0x80);
                int offset = col*32+row;
                int attributes = source.read(offset+0x800)&0xff;
                int tile_number = (source.read(offset)&0xff) + 256*(attributes&3);
                int color = attributes>>4;
                SET_TILE_INFO( 0, tile_number, color );
	} };
	
	public static WriteHandlerPtr armedf_text_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = videoram.READ_WORD(offset);
                int newword = COMBINE_WORD(oldword,data);
                if( oldword != newword ){
                        videoram.WRITE_WORD(offset, newword);
                        offset = (offset-0x80)/2;
                        if( offset>=0x800 ) offset -= 0x800;
                        if( offset>=0 && offset<38*32 ){
                                tilemap_mark_tile_dirty( text_layer, offset/32, offset%32 );
                        }
                }
	} };
	
	public static ReadHandlerPtr armedf_text_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return videoram.READ_WORD (offset);
	} };
	
	public static ReadHandlerPtr terraf_text_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return videoram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr terraf_text_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
                // hack by Chuso
		if (videoram.offset + offset >=(videoram.memory.length-1))
                    return;
                
                int oldword = videoram.READ_WORD(offset&0x1fff);
                int newword = COMBINE_WORD(oldword,data);
                if( oldword != newword ){
                        int row,col;
                        videoram.WRITE_WORD(offset, newword);
                        offset = offset/2;
                        row = 31-(offset&0x3ff)/32;

                        if( offset>=0x800 ){
                                col = offset%32;
                                if( col<3 )
                                        tilemap_mark_tile_dirty( text_layer,col+35,row );
                                else if( col>=29 )
                                        tilemap_mark_tile_dirty( text_layer,col-29,row );
                        }
                        else {
                                offset = offset&0x3ff;
                                col = (offset%32)+3;
                                tilemap_mark_tile_dirty( text_layer,col,row );
                        }
                }
	} };
	
	
	public static WriteHandlerPtr terraf_get_text_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int tile_index = 32*(31-row);
                UShortPtr source = new UShortPtr(videoram);

                if( col<3 ){
                        tile_index += 0x800+col+29;
                }
                else if( col<35 ){
                        tile_index += (col-3);
                }
                else {
                        tile_index += 0x800+col-35;
                }

                {
                        int attributes = source.read(tile_index+0x400)&0xff;
                        int tile_number = source.read(tile_index)&0xff;

                        SET_TILE_INFO( 0, /* gfx bank */
                                tile_number+ 256*(attributes&0x3),
                                attributes>>4 /* color */
                        );
                }
	} };
	
	/******************************************************************/
	
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row) 
	{
		int data = (new UShortPtr(armedf_fg_videoram)).read(col*32+row);
                SET_TILE_INFO( 1, data&0x7ff, data>>11 );
	} };
	
	public static WriteHandlerPtr armedf_fg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = armedf_fg_videoram.READ_WORD(offset);
                int newword = COMBINE_WORD(oldword,data);
                if( oldword != newword ){
                        armedf_fg_videoram.WRITE_WORD(offset, newword);
                        offset = offset/2;
                        tilemap_mark_tile_dirty( foreground, offset/32, offset%32 );
                }
	} };
	
	public static ReadHandlerPtr armedf_fg_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return armedf_fg_videoram.READ_WORD (offset);
	} };
	
	/******************************************************************/
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {public void handler(int col, int row)
	{
		int data = (new UShortPtr(armedf_bg_videoram)).read(col*32+row);
                SET_TILE_INFO( 2, data&0x3ff, data>>11 );
	} };
	
	public static WriteHandlerPtr armedf_bg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = armedf_bg_videoram.READ_WORD(offset);
                int newword = COMBINE_WORD(oldword,data);
                if( oldword != newword ){
                        armedf_bg_videoram.WRITE_WORD(offset, newword);
                        offset = offset/2;
                        tilemap_mark_tile_dirty( background, offset/32, offset%32 );
                }
	} };
	
	public static ReadHandlerPtr armedf_bg_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return armedf_bg_videoram.READ_WORD( offset );
	} };
	
	/******************************************************************/
	
	public static VhStartPtr terraf_vh_start = new VhStartPtr() { public int handler() 
	{
		scroll_type = 0;

                text_layer = tilemap_create(
                        terraf_get_text_tile_info,
                        TILEMAP_TRANSPARENT,
                        8,8,	/* tile width, tile height */
                        38,32	/* number of columns, number of rows */
                );

                background = tilemap_create(
                        get_bg_tile_info,
                        0,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                foreground = tilemap_create(
                        get_fg_tile_info,
                        TILEMAP_TRANSPARENT,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                if( background!=null && foreground!=null && text_layer!=null ){
                        foreground.transparent_pen = 0xf;
                        text_layer.transparent_pen = 0xf;

                        return 0;
                }
                return 1;
	} };
	
	public static VhStartPtr armedf_vh_start = new VhStartPtr() { public int handler() 
	{
		scroll_type = 1;

                text_layer = tilemap_create(
                        get_text_tile_info,
                        TILEMAP_TRANSPARENT,
                        8,8,	/* tile width, tile height */
                        38,32	/* number of columns, number of rows */
                );

                background = tilemap_create(
                        get_bg_tile_info,
                        0,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                foreground = tilemap_create(
                        get_fg_tile_info,
                        TILEMAP_TRANSPARENT,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                if( background!=null && foreground!=null && text_layer!=null ){
                        foreground.transparent_pen = 0xf;
                        text_layer.transparent_pen = 0xf;

                        return 0;
                }
                return 1;
	} };
	
	public static VhStartPtr kodure_vh_start = new VhStartPtr() { public int handler() 
	{
		scroll_type = 2;

                text_layer = tilemap_create(
                        terraf_get_text_tile_info,
                        TILEMAP_TRANSPARENT,
                        8,8,	/* tile width, tile height */
                        38,32	/* number of columns, number of rows */
                );

                background = tilemap_create(
                        get_bg_tile_info,
                        0,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                foreground = tilemap_create(
                        get_fg_tile_info,
                        TILEMAP_TRANSPARENT,
                        16,16,	/* tile width, tile height */
                        64,32	/* number of columns, number of rows */
                );

                if( background!=null && foreground!=null && text_layer!=null ){
                        foreground.transparent_pen = 0xf;
                        text_layer.transparent_pen = 0xf;

                        return 0;
                }
                return 1;
	} };
	
	public static VhStopPtr armedf_vh_stop = new VhStopPtr() { public void handler() 
	{
	} };
	
	static void draw_sprites( osd_bitmap bitmap, int priority )
	{
		rectangle clip = new rectangle(Machine.drv.visible_area);
                GfxElement gfx = Machine.gfx[3];

                UShortPtr source = new UShortPtr(spriteram);
                UShortPtr finish = new UShortPtr(source, 512);

                while( source.offset<finish.offset ){
                        int sy = 128+240-(source.read(0)&0x1ff);
                        int tile_number = source.read(1); /* ??YX?TTTTTTTTTTT */

                        int color = (source.read(2)>>8)&0x1f;
                        int sx = source.read(3) - 0x60;

                        if( ((source.read(0)&0x2000)!=0?0:1) == priority ){
                                drawgfx(bitmap,gfx,
                                        tile_number,
                                        color,
                                        tile_number&0x2000,tile_number&0x1000, /* flip */
                                        sx,sy,
                                        clip,TRANSPARENCY_PEN,0xf);
                        }

                        source.inc(4);
                }
	}
	
	static void mark_sprite_colors()
	{
		UShortPtr source = new UShortPtr(spriteram);
                UShortPtr finish = new UShortPtr(source, 512);
                int i;
                int[] flag=new int[32];

                for( i=0; i<32; i++ ) flag[i] = 0;

                while( source.offset<finish.offset ){
                        int color = (source.read(2)>>8)&0x1f;
                        flag[color] = 1;
                        source.inc(4);
                }

                {
                        UBytePtr pen_ptr = new UBytePtr(palette_used_colors, Machine.drv.gfxdecodeinfo[3].color_codes_start);
                        int pen;
                        for( i = 0; i<32; i++ ){
                                if( flag[i] != 0 ){
                                        for( pen = 0; pen<0xf; pen++ ) pen_ptr.write(pen, PALETTE_COLOR_USED);
                                }
                                pen_ptr.inc(16);
                        }
                }
	}
	
	public static VhUpdatePtr armedf_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int sprite_enable = armedf_vreg & 0x200;

                tilemap_set_enable( background, armedf_vreg&0x800 );
                tilemap_set_enable( foreground, armedf_vreg&0x400 );
                tilemap_set_enable( text_layer, armedf_vreg&0x100 );

                tilemap_set_scrollx( background, 0, armedf_bg_scrollx+96 );
                tilemap_set_scrolly( background, 0, armedf_bg_scrolly );

                switch (scroll_type)
                {
                        case	0:		/* terra force */
                                tilemap_set_scrollx( foreground, 0, (armedf_fg_scrolly>>8) + ((terraf_scroll_msb>>12)&3)*256 - 160-256*3);
                                tilemap_set_scrolly( foreground, 0, (armedf_fg_scrollx>>8) + ((terraf_scroll_msb>>8)&3)*256 );
                                break;
                        case	1:		/* armed formation */
                        case	2:		/* kodure ookami */
                                tilemap_set_scrollx( foreground, 0, armedf_fg_scrollx+96 );
                                tilemap_set_scrolly( foreground, 0, armedf_fg_scrolly );
                }

                if (scroll_type == 2)		/* kodure ookami */
                {
                        tilemap_set_scrollx( text_layer, 0, -8 );
                        tilemap_set_scrolly( text_layer, 0, 0 );
                }

                tilemap_update(  ALL_TILEMAPS  );

                palette_init_used_colors();
                mark_sprite_colors();
                palette_used_colors.write(0, PALETTE_COLOR_USED);	/* background */

                if( palette_recalc() != null ) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );

                tilemap_render(  ALL_TILEMAPS  );

                if(( armedf_vreg & 0x0800 ) != 0 )
                        tilemap_draw( bitmap, background, 0 );
                else
                        fillbitmap( bitmap, Machine.pens[0], null ); /* disabled background - all black? */

                if( sprite_enable != 0 ) draw_sprites( bitmap, 0 );
                tilemap_draw( bitmap, foreground, 0 );
                if( sprite_enable != 0 ) draw_sprites( bitmap, 1 );
                tilemap_draw( bitmap, text_layer, 0 );
	} };
	
	
	static void cclimbr2_draw_sprites( osd_bitmap bitmap, int priority )
	{
		rectangle clip = new rectangle(Machine.drv.visible_area);
                GfxElement gfx = Machine.gfx[3];

                UShortArray source = new UShortArray(spriteram);
                UShortArray finish = new UShortArray(source, 1024);

                while( source.offset<finish.offset ){
                        int sy = 240-(source.read(0)&0x1ff);				// ???
                        int tile_number = source.read(1); /* ??YX?TTTTTTTTTTT */

                        int color = (source.read(2)>>8)&0x1f;
                        int sx = source.read(3) - 0x68;

                        if (((source.read(0) & 0x3000) >> 12) == priority)
                        {
                                drawgfx(bitmap,gfx,
                                        tile_number,
                                        color,
                                        tile_number&0x2000,tile_number&0x1000, /* flip */
                                        sx,sy,
                                        clip,TRANSPARENCY_PEN,0xf);
                        }

                        source.inc(4);
                }
	}
	
	public static VhUpdatePtr cclimbr2_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		UBytePtr RAM=new UBytePtr();
		int sprite_enable = armedf_vreg & 0x200;

                tilemap_set_enable( background, armedf_vreg&0x800 );
                tilemap_set_enable( foreground, armedf_vreg&0x400 );
                tilemap_set_enable( text_layer, armedf_vreg&0x100 );

                tilemap_set_scrollx( text_layer, 0, 0 );
                tilemap_set_scrolly( text_layer, 0, 0 );

                tilemap_set_scrollx( background, 0, armedf_bg_scrollx+104);
                tilemap_set_scrolly( background, 0, armedf_bg_scrolly );

                RAM = new UBytePtr(memory_region(REGION_CPU1));
                tilemap_set_scrollx( foreground, 0, RAM.READ_WORD(0x6123c) - (160 + 256 * 3)+8);	// ???
                tilemap_set_scrolly( foreground, 0, RAM.READ_WORD(0x6123e) - 1);			// ???

                tilemap_update(  ALL_TILEMAPS  );

                palette_init_used_colors();
                mark_sprite_colors();
                palette_used_colors.write(0, PALETTE_COLOR_USED);	/* background */

                if( palette_recalc() != null ) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );

                tilemap_render(  ALL_TILEMAPS  );

                if(( armedf_vreg & 0x0800 ) != 0 )
                        tilemap_draw( bitmap, background, 0 );
                else
                        fillbitmap( bitmap, Machine.pens[0], null ); /* disabled background - all black? */

                if( sprite_enable != 0 ) cclimbr2_draw_sprites( bitmap, 2 );
                tilemap_draw( bitmap, foreground, 0 );
                if( sprite_enable != 0 ) cclimbr2_draw_sprites( bitmap, 1 );
                tilemap_draw( bitmap, text_layer, 0 );
                if( sprite_enable != 0 ) cclimbr2_draw_sprites( bitmap, 0 );
	} };
}
