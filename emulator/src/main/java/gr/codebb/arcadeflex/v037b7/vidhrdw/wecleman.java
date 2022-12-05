/***************************************************************************
						WEC Le Mans 24  &   Hot Chase

					      (C)   1986 & 1988 Konami

					driver by	Luca Elia (eliavit@unina.it)


Note:	if MAME_DEBUG is defined, pressing Z with:

		Q		shows background layer
		W		shows foreground layer
		E		shows text layer
		R		shows road layer
		A		shows sprites
		B		toggles the custom gfx browser on/off

		Keys can be used togheter!

							[WEC Le Mans 24]

[ 2 Scrolling Layers ]
	[Background]
	[Foreground]
		Tile Size:				8x8

		Tile Format:
										Colour?
				---- ba98 7654 3210		Code

		Layer Size:				4 Pages -	Page0 Page1
											Page2 Page3
								each page is 512 x 256 (64 x 32 tiles)

		Page Selection Reg.:	108efe	[Bg]
								108efc	[Fg]
								4 pages to choose from

		Scrolling Columns:		1
		Scrolling Columns Reg.:	108f26	[Bg]
								108f24	[Fg]

		Scrolling Rows:			224 / 8 (Screen-wise scrolling)
		Scrolling Rows Reg.:	108f82/4/6..	[Bg]
								108f80/2/4..	[Fg]

[ 1 Text Layer ]
		Tile Size:				8x8

		Tile Format:
				fedc ba9- ---- ----		Colour: ba9 fedc
				---- ba98 7654 3210		Code

		Layer Size:				1 Page: 512 x 256 (64 x 32 tiles)

		Scrolling:				-

[ 1 Road Layer ]

[ 256 Sprites ]
	Zooming Sprites, see below



								[Hot Chase]

[ 3 Zooming Layers ]
	[Background]
	[Foreground (text)]
	[Road]

[ 256 Sprites ]
	Zooming Sprites, see below


**************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.spriteC.*;
import static gr.codebb.arcadeflex.v036.mame.spriteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import gr.codebb.arcadeflex.v036.vidhrdw.konamiic;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b7.drivers.wecleman.wecleman_irqctrl;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;


public class wecleman
{
	
	/* Variables only used here: */
	
	static tilemap bg_tilemap, fg_tilemap, txt_tilemap;
	static sprite_list _sprite_list;
	
	
	/* Variables that driver has acces to: */
	
	public static UBytePtr wecleman_pageram=new UBytePtr(), wecleman_txtram=new UBytePtr(), wecleman_roadram=new UBytePtr(), wecleman_unknown=new UBytePtr();
        public static int[] wecleman_roadram_size = new int[1];
        static int[] wecleman_bgpage=new int[4], wecleman_fgpage=new int[4], wecleman_gfx_bank;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* Variables defined in driver: */
/*TODO*///	
/*TODO*///	extern int wecleman_selected_ip, wecleman_irqctrl;
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///								Common routines
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/* Useful defines - for debug */
/*TODO*///	#define KEY(_k_,_action_) \
/*TODO*///		if (keyboard_pressed(KEYCODE_##_k_))	{ while (keyboard_pressed(KEYCODE_##_k_)); _action_ }
/*TODO*///	#define KEY_SHIFT(_k_,_action_) \
/*TODO*///		if ( (keyboard_pressed(KEYCODE_LSHIFT)||keyboard_pressed(KEYCODE_RSHIFT)) && \
/*TODO*///		      keyboard_pressed(KEYCODE_##_k_) )	{ while (keyboard_pressed(KEYCODE_##_k_)); _action_ }
/*TODO*///	#define KEY_FAST(_k_,_action_) \
/*TODO*///		if (keyboard_pressed(KEYCODE_##_k_))	{ _action_ }
/*TODO*///	
/*TODO*///	
	/* WEC Le Mans 24 and Hot Chase share the same sprite hardware */
        static int NUM_SPRITES = 256;
	
	
	public static WriteHandlerPtr paletteram_SBGRBBBBGGGGRRRR_word_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*	byte 0    	byte 1		*/
		/*	SBGR BBBB 	GGGG RRRR	*/
		/*	S000 4321 	4321 4321	*/
		/*  S = Shade				*/
	
		int oldword = paletteram.READ_WORD (offset);
		int newword = COMBINE_WORD (oldword, data);
	
		int r = ((newword << 1) & 0x1E ) | ((newword >> 12) & 0x01);
		int g = ((newword >> 3) & 0x1E ) | ((newword >> 13) & 0x01);
		int b = ((newword >> 7) & 0x1E ) | ((newword >> 14) & 0x01);
	
		/* This effect can be turned on/off actually ... */
		if ((newword & 0x8000) != 0)	{ r /= 2;	 g /= 2;	 b /= 2; }
	
		palette_change_color( offset/2,	 (r * 0xFF) / 0x1F,
										 (g * 0xFF) / 0x1F,
										 (b * 0xFF) / 0x1F	 );
	
		paletteram.WRITE_WORD (offset, newword);
	} };
	
	
	
	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///						  Callbacks for the TileMap code
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///									WEC Le Mans 24
/*TODO*///	***************************************************************************/
/*TODO*///	
	public static final int PAGE_NX  		= (0x40);
        public static final int PAGE_NY  		= (0x20);
        public static final int PAGE_GFX		= (0);
        public static final int TILEMAP_DIMY            = (PAGE_NY * 2 * 8);
	
	/*------------------------------------------------------------------------
					[ Frontmost (text) layer + video registers ]
	------------------------------------------------------------------------*/
	
	
	
	static WriteHandlerPtr wecleman_get_txt_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index	=	col + row * PAGE_NX;
                int code 		=	wecleman_txtram.READ_WORD(tile_index*2);
                SET_TILE_INFO(PAGE_GFX, code & 0xfff, (code >> 12) + ((code >> 5) & 0x70) );
	}};
	
	
	
	
	public static ReadHandlerPtr wecleman_txtram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return wecleman_txtram.READ_WORD(offset);
	} };
	
	
	
	
	public static WriteHandlerPtr wecleman_txtram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int old_data = wecleman_txtram.READ_WORD(offset);
		int new_data = COMBINE_WORD(old_data,data);
	
		if ( old_data != new_data )
		{
			wecleman_txtram.WRITE_WORD(offset, new_data);
	
			if (offset >= 0xE00 )	/* Video registers */
			{
				/* pages selector for the background */
				if (offset == 0xEFE)
				{
					wecleman_bgpage[0] = (new_data >> 0x4) & 3;
					wecleman_bgpage[1] = (new_data >> 0x0) & 3;
					wecleman_bgpage[2] = (new_data >> 0xc) & 3;
					wecleman_bgpage[3] = (new_data >> 0x8) & 3;
					tilemap_mark_all_tiles_dirty(bg_tilemap);
				}
	
				/* pages selector for the foreground */
				if (offset == 0xEFC)
				{
					wecleman_fgpage[0] = (new_data >> 0x4) & 3;
					wecleman_fgpage[1] = (new_data >> 0x0) & 3;
					wecleman_fgpage[2] = (new_data >> 0xc) & 3;
					wecleman_fgpage[3] = (new_data >> 0x8) & 3;
					tilemap_mark_all_tiles_dirty(fg_tilemap);
				}
	
				/* Parallactic horizontal scroll registers follow */
	
			}
			else
				tilemap_mark_tile_dirty(txt_tilemap, offset / 2, 0);
		}
	} };
	
	
	
	
	/*------------------------------------------------------------------------
								[ Background ]
	------------------------------------------------------------------------*/
	
	
	
	static WriteHandlerPtr wecleman_get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index	=	(col % PAGE_NX) + (row % PAGE_NY) * PAGE_NX;
                int page		=	wecleman_bgpage[(col / PAGE_NX) + (row / PAGE_NY) * 2];
                int code 		=	wecleman_pageram.READ_WORD(tile_index*2 + page*(PAGE_NX*PAGE_NY*2));
                SET_TILE_INFO(PAGE_GFX, code & 0xfff, (code >> 12) + ((code >> 5) & 0x70) );
	}};
	
	
	
	/*------------------------------------------------------------------------
								[ Foreground ]
	------------------------------------------------------------------------*/
	
	
	
	static WriteHandlerPtr wecleman_get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index	=	(col % PAGE_NX) + (row % PAGE_NY) * PAGE_NX;
                int page		=	wecleman_fgpage[(col / PAGE_NX) + (row / PAGE_NY) * 2];
                int code 		=	wecleman_pageram.READ_WORD(tile_index*2 + page*(PAGE_NX*PAGE_NY*2));
                SET_TILE_INFO(PAGE_GFX, code & 0xfff, (code >> 12) + ((code >> 5) & 0x70) );
	}};
	
	
	
	
	
	
	/*------------------------------------------------------------------------
						[ Pages (Background & Foreground) ]
	------------------------------------------------------------------------*/
	
	
	
	/* Pages that compose both the background and the foreground */
	public static ReadHandlerPtr wecleman_pageram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return wecleman_pageram.READ_WORD(offset);
	} };
	
	
	public static WriteHandlerPtr wecleman_pageram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int old_data = wecleman_pageram.READ_WORD(offset);
		int new_data = COMBINE_WORD(old_data,data);
	
		if ( old_data != new_data )
		{
			int page,col,row;
	
			wecleman_pageram.WRITE_WORD(offset, new_data);
	
			page	=	( offset / 2 ) / (PAGE_NX * PAGE_NY);
	
			col		=	( offset / 2 )           % PAGE_NX;
			row		=	( offset / 2 / PAGE_NX ) % PAGE_NY;
	
			/* background */
			if (wecleman_bgpage[0] == page)	tilemap_mark_tile_dirty(bg_tilemap, (col+PAGE_NX*0) + (row+PAGE_NY*0)*PAGE_NX*2, 0 );
			if (wecleman_bgpage[1] == page)	tilemap_mark_tile_dirty(bg_tilemap, (col+PAGE_NX*1) + (row+PAGE_NY*0)*PAGE_NX*2, 0 );
			if (wecleman_bgpage[2] == page)	tilemap_mark_tile_dirty(bg_tilemap, (col+PAGE_NX*0) + (row+PAGE_NY*1)*PAGE_NX*2, 0 );
			if (wecleman_bgpage[3] == page)	tilemap_mark_tile_dirty(bg_tilemap, (col+PAGE_NX*1) + (row+PAGE_NY*1)*PAGE_NX*2, 0 );
	
			/* foreground */
			if (wecleman_fgpage[0] == page)	tilemap_mark_tile_dirty(fg_tilemap, (col+PAGE_NX*0) + (row+PAGE_NY*0)*PAGE_NX*2, 0 );
			if (wecleman_fgpage[1] == page)	tilemap_mark_tile_dirty(fg_tilemap, (col+PAGE_NX*1) + (row+PAGE_NY*0)*PAGE_NX*2, 0 );
			if (wecleman_fgpage[2] == page)	tilemap_mark_tile_dirty(fg_tilemap, (col+PAGE_NX*0) + (row+PAGE_NY*1)*PAGE_NX*2, 0 );
			if (wecleman_fgpage[3] == page)	tilemap_mark_tile_dirty(fg_tilemap, (col+PAGE_NX*1) + (row+PAGE_NY*1)*PAGE_NX*2, 0 );
		}
	} };
	
	
	
	/*------------------------------------------------------------------------
							[ Video Hardware Start ]
	------------------------------------------------------------------------*/
	
	public static VhStartPtr wecleman_vh_start = new VhStartPtr() { public int handler() 
	{
	
	/*
	 Sprite banking - each bank is 0x20000 bytes (we support 0x40 bank codes)
	 This game has ROMs for 16 banks
	*/
	
		int bank[] = {	0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,
									8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,
									0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,
									8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15	};
	
		wecleman_gfx_bank = bank;
	
	
		bg_tilemap = tilemap_create(wecleman_get_bg_tile_info,
								TILEMAP_TRANSPARENT,	/* We draw part of the road below */
								8,8,
								PAGE_NX * 2, PAGE_NY * 2 );
	
		fg_tilemap = tilemap_create(wecleman_get_fg_tile_info,
									//tilemap_scan_rows,
									TILEMAP_TRANSPARENT,
									8,8,
									PAGE_NX * 2, PAGE_NY * 2);
	
		txt_tilemap = tilemap_create(wecleman_get_txt_tile_info,
									 //tilemap_scan_rows,
									 TILEMAP_TRANSPARENT,
									 8,8,
									 PAGE_NX * 1, PAGE_NY * 1);
	
	
		_sprite_list = sprite_list_create( NUM_SPRITES, SPRITE_LIST_BACK_TO_FRONT | SPRITE_LIST_RAW_DATA );
	
	
		if (bg_tilemap!=null && fg_tilemap!=null && txt_tilemap!=null && _sprite_list!=null)
		{
			tilemap_set_scroll_rows(bg_tilemap, TILEMAP_DIMY);	/* Screen-wise scrolling */
			tilemap_set_scroll_cols(bg_tilemap, 1);
			bg_tilemap.transparent_pen = 0;
	
			tilemap_set_scroll_rows(fg_tilemap, TILEMAP_DIMY);	/* Screen-wise scrolling */
			tilemap_set_scroll_cols(fg_tilemap, 1);
			fg_tilemap.transparent_pen = 0;
	
			tilemap_set_scroll_rows(txt_tilemap, 1);
			tilemap_set_scroll_cols(txt_tilemap, 1);
			txt_tilemap.transparent_pen = 0;
			tilemap_set_scrollx(txt_tilemap,0, 512-320-16 );	/* fixed scrolling? */
			tilemap_set_scrolly(txt_tilemap,0, 0 );
	
			_sprite_list.max_priority = 0;
			_sprite_list.sprite_type = SPRITE_TYPE_ZOOM;
	
			return 0;
		}
		else return 1;
	} };
	
	
	
	
	
	
	
	
	
	
	
	
	
/*TODO*///	/***************************************************************************
/*TODO*///									Hot Chase
/*TODO*///	***************************************************************************/
	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	public static int ZOOMROM0_MEM_REGION = REGION_GFX2;
	public static int ZOOMROM1_MEM_REGION = REGION_GFX3;
	
	static K051316_callbackProcPtr zoom_callback_0 = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
                code[0] |= (color[0] & 0x03) << 8;
		color[0] = (color[0] & 0xfc) >> 2;
            }
        };
        
	static K051316_callbackProcPtr zoom_callback_1 = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
		code[0] |= (color[0] & 0x01) << 8;
		color[0] = ((color[0] & 0x3f) << 1) | ((code[0] & 0x80) >> 7);
            }
        };
	
	
	
	/*------------------------------------------------------------------------
							[ Video Hardware Start ]
	------------------------------------------------------------------------*/
	
	/* for the zoomed layers we support: road and fg */
	static osd_bitmap temp_bitmap, temp_bitmap2;
	
	public static VhStartPtr hotchase_vh_start = new VhStartPtr() { public int handler() 
	{
	/*
	 Sprite banking - each bank is 0x20000 bytes (we support 0x40 bank codes)
	 This game has ROMs for 0x30 banks
	*/
		int bank[] = {	0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,
									16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,
									32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,
									0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15	};
		wecleman_gfx_bank = bank;
	
	
		if (konamiic.K051316_vh_start_0(ZOOMROM0_MEM_REGION,4,zoom_callback_0) != 0)
			return 1;
	
		if (K051316_vh_start_1(ZOOMROM1_MEM_REGION,4,zoom_callback_1) != 0)
		{
			K051316_vh_stop_0();
			return 1;
		}
	
		temp_bitmap  = bitmap_alloc(512,512);
		temp_bitmap2 = bitmap_alloc(512,256);
	
		_sprite_list = sprite_list_create( NUM_SPRITES, SPRITE_LIST_BACK_TO_FRONT | SPRITE_LIST_RAW_DATA );
	
		if (temp_bitmap!=null && temp_bitmap2!=null && _sprite_list!=null)
		{
			K051316_wraparound_enable(0,1);
	//		K051316_wraparound_enable(1,1);
			K051316_set_offset(0,-96,-16);
			K051316_set_offset(1,-96,-16);
	
			_sprite_list.max_priority = 0;
			_sprite_list.sprite_type = SPRITE_TYPE_ZOOM;
	
			return 0;
		}
		else return 1;
	} };
	
	public static VhStopPtr hotchase_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (temp_bitmap != null)	bitmap_free(temp_bitmap);
		if (temp_bitmap2 != null)	bitmap_free(temp_bitmap2);
		K051316_vh_stop_0();
		K051316_vh_stop_1();
	} };
	
	
	
	
	
	
	
	/***************************************************************************
	
									Road Drawing
	
	***************************************************************************/
	
	
	/***************************************************************************
									WEC Le Mans 24
	***************************************************************************/
	
	static int ROAD_COLOR(int x){	return (0x00 + ((x)&0xff)); }

	static void wecleman_mark_road_colors()
	{
		int y					=	Machine.visible_area.min_y;
		int ymax				=	Machine.visible_area.max_y;
		int color_codes_start	=	Machine.drv.gfxdecodeinfo[1].color_codes_start;
	
		for (; y <= ymax; y++)
		{
			int color = ROAD_COLOR( wecleman_roadram.READ_WORD(0x400 + y * 2) );
			memset(new UBytePtr(palette_used_colors, color_codes_start + color*8),PALETTE_COLOR_USED,8);	// no transparency
		}
	}
	
	
	/*
	
		This layer is composed of horizontal lines gfx elements
		There are 256 lines in ROM, each is 512 pixels wide
	
		Offset:		Elements:	Data:
	
		0000-01ff	100 Words	Code
	
			fedcba98--------	Priority?
			--------76543210	Line Number
	
		0200-03ff	100 Words	Horizontal Scroll
		0400-05ff	100 Words	Color
		0600-07ff	100 Words	??
	
		We draw each line using a bunch of 64x1 tiles
	
	*/
	
	static void wecleman_draw_road(osd_bitmap bitmap,int priority)
	{
		rectangle rect = new rectangle(Machine.visible_area);
		int curr_code, sx,sy;
	
                /* Referred to what's in the ROMs */
                int XSIZE = 512;
                int YSIZE = 256;
	
		/* Let's draw from the top to the bottom of the visible screen */
		for (sy = rect.min_y ; sy <= rect.max_y ; sy ++)
		{
			int code		=	wecleman_roadram.READ_WORD((YSIZE*0+sy)*2);
			int scrollx 	=	wecleman_roadram.READ_WORD((YSIZE*1+sy)*2) + 24;	// fudge factor :)
			int attr		=	wecleman_roadram.READ_WORD((YSIZE*2+sy)*2);
	
			/* high byte is a priority information? */
			if ((code>>8) != priority)	continue;
	
			/* line number converted to tile number (each tile is 64x1) */
			code		=	(code % YSIZE) * (XSIZE/64);
	
			/* Scroll value applies to a "picture" twice as wide as the gfx
			   in ROM: left half is color 15, right half is the gfx */
			scrollx %= XSIZE * 2;
	
			if (scrollx >= XSIZE)	{curr_code = code+(scrollx-XSIZE)/64;	code = 0;}
			else					{curr_code = 0   + scrollx/64;}
	
			for (sx = -(scrollx % 64) ; sx <= rect.max_x ; sx += 64)
			{
				drawgfx(bitmap,Machine.gfx[1],
					curr_code++,
					ROAD_COLOR(attr),
					0,0,
					sx,sy,
					rect,
					TRANSPARENCY_NONE,0);
	
				if ( (curr_code % (XSIZE/64)) == 0)	curr_code = code;
			}
		}
	
	
	}
	
	
	
	
	
	
	/***************************************************************************
								Hot Chase
	***************************************************************************/
	
	
	
	
	static void hotchase_mark_road_colors()
	{
		int y					=	Machine.visible_area.min_y;
		int ymax				=	Machine.visible_area.max_y;
		int color_codes_start	=	Machine.drv.gfxdecodeinfo[0].color_codes_start;
	
		for (; y <= ymax; y++)
		{
			int color = (wecleman_roadram.READ_WORD(y * 4) >> 4 ) & 0xf;
			palette_used_colors.write(color_codes_start + color*16 + 0, PALETTE_COLOR_TRANSPARENT);	// pen 0 transparent
			memset(new UBytePtr(palette_used_colors, color_codes_start + color*16 + 1),PALETTE_COLOR_USED,16-1);
		}
	
	}
	
	
	
	
	/*
		This layer is composed of horizontal lines gfx elements
		There are 512 lines in ROM, each is 512 pixels wide
	
		Offset:		Elements:	Data:
	
		0000-03ff	00-FF		Code (4 bytes)
	
		Code:
	
			00.w
					fedc ba98 ---- ----		Unused?
					---- ---- 7654 ----		color
					---- ---- ---- 3210		scroll x
	
			02.w	fedc ba-- ---- ----		scroll x
					---- --9- ---- ----		?
					---- ---8 7654 3210		code
	
		We draw each line using a bunch of 64x1 tiles
	
	*/
	
	static void hotchase_draw_road(osd_bitmap bitmap,int priority, rectangle clip)
	{
            rectangle rect = new rectangle(clip);
            int sy;
	
	
            /* Referred to what's in the ROMs */
            int XSIZE = 512;
            int YSIZE = 512;
	
	
		/* Let's draw from the top to the bottom of the visible screen */
		for (sy = rect.min_y ; sy <= rect.max_y ; sy ++)
		{
			int curr_code,sx;
			int code	=	  wecleman_roadram.READ_WORD(0x0000+sy*4+2) +
							( wecleman_roadram.READ_WORD(0x0000+sy*4+0) << 16 );
			int color	=	(( code & 0x00f00000 ) >> 20 ) + 0x70;
			int scrollx =	 ( code & 0x000ffc00 ) >> 10;
			code		=	 ( code & 0x000003ff ) >>  0;
	
			/* convert line number in gfx element number: */
			/* code is the tile code of the start of this line */
			code	= ( code % YSIZE ) * ( XSIZE / 64 );
	
	//		if (scrollx < 0) scrollx += XSIZE * 2 ;
			scrollx %= XSIZE * 2;
	
			if (scrollx < XSIZE)	{curr_code = code + scrollx/64;	code = 0;}
			else					{curr_code = 0    + scrollx/64;}
	
			for (sx = -(scrollx % 64) ; sx <= rect.max_x ; sx += 64)
			{
				drawgfx(bitmap,Machine.gfx[0],
					curr_code++,
					color,
					0,0,
					sx,sy,
					rect,
					TRANSPARENCY_PEN,0);
	
				/* Maybe after the end of a line of gfx we shouldn't
				   wrap around, but pad with a value */
				if ((curr_code % (XSIZE/64)) == 0)	curr_code = code;
			}
		}
	
	}
	
	
	
	
	/***************************************************************************
	
								Sprites Drawing
	
	***************************************************************************/
	
	/* Hot Chase: shadow of trees is pen 0x0a - Should it be black like it is now */
	
	static void mark_sprites_colors()
	{
		int offs;
	
		for (offs = 0; offs < (NUM_SPRITES * 0x10); offs += 0x10)
		{
			int dest_y, dest_h, color;
	
			dest_y = spriteram.READ_WORD(offs + 0x00);
			if (dest_y == 0xFFFF)	break;
	
			dest_h = (dest_y >> 8) - (dest_y & 0x00FF);
			if (dest_h < 1) continue;
	
			color = (spriteram.READ_WORD(offs + 0x04) >> 8) & 0x7f;
			memset(new UBytePtr(palette_used_colors, color*16 + 1), PALETTE_COLOR_USED, 16 - 2 );
	
			// pens 0 & 15 are transparent
			palette_used_colors.write(color*16 + 0, PALETTE_COLOR_TRANSPARENT);
                        palette_used_colors.write(color*16 + 15, PALETTE_COLOR_TRANSPARENT);
		}
	}
	
	
	
	
/*TODO*///	/*
/*TODO*///	
/*TODO*///		Sprites: 256 entries, 16 bytes each, first ten bytes used (and tested)
/*TODO*///	
/*TODO*///		Offset	Bits					Meaning
/*TODO*///	
/*TODO*///		00.w	fedc ba98 ---- ----		Screen Y start
/*TODO*///				---- ---- 7654 3210		Screen Y stop
/*TODO*///	
/*TODO*///		02.w	fedc ba-- ---- ----		High bits of sprite "address"
/*TODO*///				---- --9- ---- ----		Flip Y ?
/*TODO*///				---- ---8 7654 3210		Screen X start
/*TODO*///	
/*TODO*///		04.w	fedc ba98 ---- ----		Color
/*TODO*///				---- ---- 7654 3210		Source Width / 8
/*TODO*///	
/*TODO*///		06.w	f--- ---- ---- ----		Flip X
/*TODO*///				-edc ba98 7654 3210		Low bits of sprite "address"
/*TODO*///	
/*TODO*///		08.w	--dc ba98 ---- ----		Y? Shrink Factor
/*TODO*///				---- ---- --54 3210		X? Shrink Factor
/*TODO*///	
/*TODO*///	Sprite "address" is the index of the pixel the hardware has to start
/*TODO*///	fetching data from, divided by 8. Only on screen height and source data
/*TODO*///	width are provided, along with two shrinking factors. So on screen width
/*TODO*///	and source height are calculated by the hardware using the shrink factors.
/*TODO*///	The factors are in the range 0 (no shrinking) - 3F (half size).
/*TODO*///	
/*TODO*///	*/
/*TODO*///	
/*TODO*///	static void get_sprite_info()
/*TODO*///	{
/*TODO*///		const unsigned short *base_pal	= Machine.remapped_colortable;
/*TODO*///		const UBytePtr base_gfx	= memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		const int gfx_max = memory_region_length(REGION_GFX1);
/*TODO*///	
/*TODO*///		UBytePtr source		=	spriteram;
/*TODO*///		struct sprite *sprite		=	sprite_list.sprite;
/*TODO*///		const struct sprite *finish	=	sprite + NUM_SPRITES;
/*TODO*///	
/*TODO*///		int visibility = SPRITE_VISIBLE;
/*TODO*///	
/*TODO*///	
/*TODO*///	#define SHRINK_FACTOR(x) \
/*TODO*///		(1.0 - ( ( (x) & 0x3F ) / 63.0) * 0.5)
/*TODO*///	
/*TODO*///		for (; sprite < finish; sprite++,source+=0x10)
/*TODO*///		{
/*TODO*///			int code, gfx, zoom;
/*TODO*///	
/*TODO*///			sprite.priority = 0;
/*TODO*///	
/*TODO*///			sprite.y	= READ_WORD(&source[0x00]);
/*TODO*///			if (sprite.y == 0xFFFF)	{ visibility = 0; }
/*TODO*///	
/*TODO*///			sprite.flags = visibility;
/*TODO*///			if (visibility==0) continue;
/*TODO*///	
/*TODO*///			sprite.total_height = (sprite.y >> 8) - (sprite.y & 0xFF);
/*TODO*///			if (sprite.total_height < 1) {sprite.flags = 0;	continue;}
/*TODO*///	
/*TODO*///			sprite.x	 		=	READ_WORD(&source[0x02]);
/*TODO*///			sprite.tile_width	=	READ_WORD(&source[0x04]);
/*TODO*///			code				=	READ_WORD(&source[0x06]);
/*TODO*///			zoom				=	READ_WORD(&source[0x08]);
/*TODO*///	
/*TODO*///			gfx	= (wecleman_gfx_bank[(sprite.x >> 10) & 0x3f] << 15) +  (code & 0x7fff);
/*TODO*///			sprite.pal_data = base_pal + ( (sprite.tile_width >> 4) & 0x7f0 );	// 16 colors = 16 shorts
/*TODO*///	
/*TODO*///			if ((code & 0x8000) != 0)
/*TODO*///			{	sprite.flags |= SPRITE_FLIPX;	gfx += 1 - (sprite.tile_width & 0xFF);	};
/*TODO*///	
/*TODO*///			if (sprite.x & 0x0200)		/* ?flip y? */
/*TODO*///			{	sprite.flags |= SPRITE_FLIPY; }
/*TODO*///	
/*TODO*///			gfx *= 8;
/*TODO*///	
/*TODO*///			sprite.pen_data = base_gfx + gfx;
/*TODO*///	
/*TODO*///			sprite.tile_width	= (sprite.tile_width & 0xFF) * 8;
/*TODO*///			if (sprite.tile_width < 1) {sprite.flags = 0;	continue;}
/*TODO*///	
/*TODO*///			sprite.tile_height = sprite.total_height * ( 1.0 / SHRINK_FACTOR(zoom>>8) );
/*TODO*///			sprite.x   = (sprite.x & 0x1ff) - 0xc0;
/*TODO*///			sprite.y   = (sprite.y & 0xff);
/*TODO*///			sprite.total_width = sprite.tile_width * SHRINK_FACTOR(zoom & 0xFF);
/*TODO*///	
/*TODO*///			sprite.line_offset = sprite.tile_width;
/*TODO*///	
/*TODO*///			/* Bound checking */
/*TODO*///			if ((gfx + sprite.tile_width * sprite.tile_height - 1) >= gfx_max )
/*TODO*///				{sprite.flags = 0;	continue;}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///								Browse the graphics
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
/*TODO*///	/*
/*TODO*///		Browse the sprites
/*TODO*///	
/*TODO*///		Use:
/*TODO*///		* LEFT, RIGHT, UP, DOWN and PGUP/PGDN to move around
/*TODO*///		* SHIFT + PGUP/PGDN to move around faster
/*TODO*///		* SHIFT + LEFT/RIGHT to change the width of the graphics
/*TODO*///		* SHIFT + RCTRL to go back to the start of the gfx
/*TODO*///	
/*TODO*///	*/
/*TODO*///	void browser(struct osd_bitmap *bitmap)
/*TODO*///	{
/*TODO*///		const unsigned short *base_pal	=	Machine.gfx[0].colortable + 0;
/*TODO*///		const UBytePtr base_gfx	=	memory_region(REGION_GFX1);
/*TODO*///	
/*TODO*///		const int gfx_max				=	memory_region_length(REGION_GFX1);
/*TODO*///	
/*TODO*///		struct sprite *sprite			=	sprite_list.sprite;
/*TODO*///		const struct sprite *finish		=	sprite + NUM_SPRITES;
/*TODO*///	
/*TODO*///		static int w = 32, gfx;
/*TODO*///		char buf[80];
/*TODO*///	
/*TODO*///		for ( ; sprite < finish ; sprite++)	sprite.flags = 0;
/*TODO*///	
/*TODO*///		sprite = sprite_list.sprite;
/*TODO*///	
/*TODO*///		sprite.flags = SPRITE_VISIBLE;
/*TODO*///		sprite.x = 0;
/*TODO*///		sprite.y = 0;
/*TODO*///		sprite.tile_height = sprite.total_height = 224;
/*TODO*///		sprite.pal_data = base_pal;
/*TODO*///	
/*TODO*///		KEY_FAST(LEFT,	gfx-=8;)
/*TODO*///		KEY_FAST(RIGHT,	gfx+=8;)
/*TODO*///		KEY_FAST(UP,	gfx-=w;)
/*TODO*///		KEY_FAST(DOWN,	gfx+=w;)
/*TODO*///	
/*TODO*///		KEY_SHIFT(PGDN,	gfx -= 0x100000;)
/*TODO*///		KEY_SHIFT(PGUP,	gfx += 0x100000;)
/*TODO*///	
/*TODO*///		KEY(PGDN,gfx+=w*sprite.tile_height;)
/*TODO*///		KEY(PGUP,gfx-=w*sprite.tile_height;)
/*TODO*///	
/*TODO*///		KEY_SHIFT(RCONTROL,	gfx = 0;)
/*TODO*///	
/*TODO*///		gfx %= gfx_max;
/*TODO*///		if (gfx < 0)	gfx += gfx_max;
/*TODO*///	
/*TODO*///		KEY_SHIFT(LEFT,		w-=8;)
/*TODO*///		KEY_SHIFT(RIGHT,	w+=8;)
/*TODO*///		w &= 0x1ff;
/*TODO*///	
/*TODO*///		sprite.pen_data = base_gfx + gfx;
/*TODO*///		sprite.tile_width = sprite.total_width = sprite.line_offset = w;
/*TODO*///	
/*TODO*///		/* Bound checking */
/*TODO*///		if ((gfx + sprite.tile_width * sprite.tile_height - 1) >= gfx_max )
/*TODO*///			sprite.flags = 0;
/*TODO*///	
/*TODO*///		sprite_draw( sprite_list, 0 );
/*TODO*///	
/*TODO*///		sprintf(buf,"W:%02X GFX/8: %X",w,gfx / 8);
/*TODO*///		usrintf_showmessage(buf);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/***************************************************************************
/*TODO*///	
/*TODO*///								Screen Drawing
/*TODO*///	
/*TODO*///	***************************************************************************/
/*TODO*///	
    static void WECLEMAN_TVKILL() {
		if ((wecleman_irqctrl & 0x40)==0) layers_ctrl = 0;	// TV-KILL
    }
/*TODO*///	#define WECLEMAN_LAMPS \
/*TODO*///		set_led_status(0,wecleman_selected_ip & 0x04); 		// Start lamp
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* You can activate each single layer of gfx */
/*TODO*///	#define WECLEMAN_LAYERSCTRL \
/*TODO*///	{ \
/*TODO*///		static int browse = 0; \
/*TODO*///		KEY(B, browse ^= 1;) \
/*TODO*///		if (browse != 0) \
/*TODO*///		{ \
/*TODO*///			osd_clearbitmap(Machine.scrbitmap); \
/*TODO*///			browser(bitmap); \
/*TODO*///			return; \
/*TODO*///		} \
/*TODO*///		if (keyboard_pressed(KEYCODE_Z)) \
/*TODO*///		{ \
/*TODO*///		int msk = 0; \
/*TODO*///		 \
/*TODO*///			if (keyboard_pressed(KEYCODE_Q))	{ msk |= 0xffe1;} \
/*TODO*///			if (keyboard_pressed(KEYCODE_W))	{ msk |= 0xffe2;} \
/*TODO*///			if (keyboard_pressed(KEYCODE_E))	{ msk |= 0xffe4;} \
/*TODO*///			if (keyboard_pressed(KEYCODE_A))	{ msk |= 0xffe8;} \
/*TODO*///			if (keyboard_pressed(KEYCODE_R))	{ msk |= 0xfff0;} \
/*TODO*///			if (msk != 0) layers_ctrl &= msk; \
/*TODO*///		} \
/*TODO*///	}
	
	
	/***************************************************************************
								WEC Le Mans 24
	***************************************************************************/
	static int layers_ctrl = 0xFFFF;
        
	public static VhUpdatePtr wecleman_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
/*TODO*///		WECLEMAN_LAMPS
	
		WECLEMAN_TVKILL();
	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		WECLEMAN_LAYERSCTRL
/*TODO*///	#endif
	
	{
	/* Set the scroll values for the scrolling layers */
	
		/* y values */
		int bg_y = wecleman_txtram.READ_WORD(0x0F24+2) & (TILEMAP_DIMY - 1);
		int fg_y = wecleman_txtram.READ_WORD(0x0F24+0) & (TILEMAP_DIMY - 1);
	
		tilemap_set_scrolly(bg_tilemap, 0, bg_y );
		tilemap_set_scrolly(fg_tilemap, 0, fg_y );
	
		/* x values */
		for ( i = 0 ; i < 28; i++ )
		{
			int j;
			int bg_x = 0xB0 + wecleman_txtram.READ_WORD(0xF80+i*4+2);
			int fg_x = 0xB0 + wecleman_txtram.READ_WORD(0xF80+i*4+0);
	
			for ( j = 0 ; j < 8; j++ )
			{
				tilemap_set_scrollx(bg_tilemap, (bg_y + i*8 + j) & (TILEMAP_DIMY - 1), bg_x );
				tilemap_set_scrollx(fg_tilemap, (fg_y + i*8 + j) & (TILEMAP_DIMY - 1), fg_x );
			}
		}
	}
	
	
		tilemap_update(ALL_TILEMAPS);
/*TODO*///		get_sprite_info();
                System.out.println("get_sprite_info() NOT IMPLEMENTED!!!!");
	
		palette_init_used_colors();
	
		wecleman_mark_road_colors();
		mark_sprites_colors();
	
		sprite_update();
	
		if (palette_recalc()!=null)	tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
	
		osd_clearbitmap(Machine.scrbitmap);
	
		/* Draw the road (lines which have "priority" 0x02) */
		if ((layers_ctrl & 16) != 0)	wecleman_draw_road(bitmap,0x02);
	
		/* Draw the background */
		if ((layers_ctrl & 1) != 0)
		{
			tilemap_render(bg_tilemap);
			tilemap_draw(bitmap, bg_tilemap,  0);
		}
	
		/* Draw the foreground */
		if ((layers_ctrl & 2) != 0)
		{
			tilemap_render(fg_tilemap);
			tilemap_draw(bitmap, fg_tilemap,  0);
		}
	
		/* Draw the road (lines which have "priority" 0x04) */
		if ((layers_ctrl & 16) != 0)	wecleman_draw_road(bitmap,0x04);
	
		/* Draw the sprites */
		if ((layers_ctrl & 8) != 0)	sprite_draw(_sprite_list,0);
	
		/* Draw the text layer */
		if ((layers_ctrl & 4) != 0)
		{
			tilemap_render(txt_tilemap);
			tilemap_draw(bitmap, txt_tilemap,  0);
		}
	
	} };
	
	
	
	
	
	
	
	
	/***************************************************************************
									Hot Chase
	***************************************************************************/
	
	public static VhUpdatePtr hotchase_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
		layers_ctrl = 0xFFFF;
	
/*TODO*///		WECLEMAN_LAMPS
	
		WECLEMAN_TVKILL();
	
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		WECLEMAN_LAYERSCTRL
/*TODO*///	#endif
	
		K051316_tilemap_update_0();
		K051316_tilemap_update_1();
/*TODO*///		get_sprite_info();
                System.out.println("get_sprite_info() NOT IMPLEMENTED!!!!");
	
		palette_init_used_colors();
	
		hotchase_mark_road_colors();
		mark_sprites_colors();
		sprite_update();
	
		/* set transparent pens for the K051316 */
		for (i = 0;i < 128;i++)
		{
			palette_used_colors.write(i * 16, PALETTE_COLOR_TRANSPARENT);
			palette_used_colors.write(i * 16, PALETTE_COLOR_TRANSPARENT);
			palette_used_colors.write(i * 16, PALETTE_COLOR_TRANSPARENT);
		}
	
		if (palette_recalc() != null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(bitmap,palette_transparent_pen,Machine.visible_area);
	
		/* Draw the background */
		if ((layers_ctrl & 1) != 0)
			K051316_zoom_draw_0(bitmap);
	
		/* Draw the road */
		if ((layers_ctrl & 16) != 0)
		{
			rectangle clip = new rectangle(0, 512-1, 0, 256-1);
	
			fillbitmap(temp_bitmap2,palette_transparent_pen,null);
			hotchase_draw_road(temp_bitmap2,0,clip);
                        
                        System.out.println("copyrozbitmap NOT IMPLEMENTED!!!!");
	
/*TODO*///			copyrozbitmap( bitmap, temp_bitmap2,
/*TODO*///					11*16*0x10000,0,	/* start coordinates */
/*TODO*///					0x08000,0,0,0x10000,	/* double horizontally */
/*TODO*///					0,	/* no wraparound */
/*TODO*///					Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen,0);
		}
	
		/* Draw the sprites */
		if ((layers_ctrl & 8) != 0)	sprite_draw(_sprite_list,0);
	
		/* Draw the foreground (text) */
		if ((layers_ctrl & 4) != 0)
			K051316_zoom_draw_1(bitmap);
	} };
}
