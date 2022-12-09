/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.SubArrays.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import static gr.codebb.arcadeflex.v036.platform.video.osd_get_pen;
import static gr.codebb.arcadeflex.v036.drivers.system16.*;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_GFX2;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_GFX3;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.spriteC.*;
import static gr.codebb.arcadeflex.v036.mame.spriteH.*;
import static gr.codebb.arcadeflex.v036.drivers.system16.gr_bitmap_width;

public class system16
{
	public static final int MAXCOLOURS =8192;
	
	public static final int ShadowColorsShift =8;
	public static char[] shade_table=new char[MAXCOLOURS];
	public static int sys16_sh_shadowpal;
	public static int sys16_MaxShadowColors;
	public static int sys16_MaxShadowColors_Shift;
	
	public static final int NUM_SPRITES =128;
		
	static sprite_list sprite_list;
	
	/* video driver constants (potentially different for each game) */
	public static int sys16_spritesystem;
	public static int sys16_sprxoffset;
	public static int sys16_bgxoffset;
	public static int sys16_fgxoffset;
	public static int[] sys16_obj_bank;
	public static int sys16_textmode;
	public static int sys16_textlayer_lo_min;
	public static int sys16_textlayer_lo_max;
	public static int sys16_textlayer_hi_min;
	public static int sys16_textlayer_hi_max;
	public static int sys16_dactype;
	public static int sys16_bg1_trans;						// alien syn + sys18
	public static int sys16_bg_priority_mode;
	public static int sys16_fg_priority_mode;
	public static int sys16_bg_priority_value;
	public static int sys16_fg_priority_value;
	public static int sys16_18_mode;
	public static int sys16_spritelist_end;
	public static int sys16_tilebank_switch;
	public static int sys16_rowscroll_scroll;
	public static int sys16_quartet_title_kludge;
	
	/* video registers */
	public static int sys16_tile_bank1;
	public static int sys16_tile_bank0;
	public static int sys16_refreshenable;
	public static int sys16_clear_screen;
	public static int sys16_bg_scrollx, sys16_bg_scrolly;
	public static int[] sys16_bg_page=new int[4];
	public static int sys16_fg_scrollx, sys16_fg_scrolly;
	public static int[] sys16_fg_page=new int[4];
	
	public static int sys16_bg2_scrollx, sys16_bg2_scrolly;
	public static int[] sys16_bg2_page=new int[4];
	public static int sys16_fg2_scrollx, sys16_fg2_scrolly;
	public static int[] sys16_fg2_page=new int[4];
	
	public static int sys18_bg2_active;
	public static int sys18_fg2_active;
	public static UBytePtr sys18_splittab_bg_x=new UBytePtr();
	public static UBytePtr sys18_splittab_bg_y=new UBytePtr();
	public static UBytePtr sys18_splittab_fg_x=new UBytePtr();
	public static UBytePtr sys18_splittab_fg_y=new UBytePtr();
	
	static int sys16_freezepalette;
	static int[] sys16_palettedirty=new int[MAXCOLOURS];
	
	public static UBytePtr gr_ver=new UBytePtr();
	public static UBytePtr gr_hor=new UBytePtr();
	public static UBytePtr gr_pal=new UBytePtr();
	public static UBytePtr gr_flip=new UBytePtr();
	public static int gr_palette;
	public static int gr_palette_default;
	public static char[][] gr_colorflip=new char[2][4];
	public static UBytePtr gr_second_road=new UBytePtr();
	
	
	static tilemap background, foreground, text_layer;
	static tilemap background2, foreground2;
	static int[] old_bg_page=new int[4];
        static int[] old_fg_page=new int[4];
        static int old_tile_bank1, old_tile_bank0;
	static int[] old_bg2_page=new int[4];
        static int[] old_fg2_page=new int[4];
	
	public static WriteHandlerPtr sys16_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		char oldword = (char)paletteram.READ_WORD(offset);
		char newword = (char)COMBINE_WORD (oldword, data);
		if( oldword!=newword ){
			/* we can do this, because we initialize palette RAM to all black in vh_start */
	
			/*	   byte 0    byte 1 */
			/*	GBGR BBBB GGGG RRRR */
			/*	5444 3210 3210 3210 */
	
			char/*UINT8*/ r = (char)(((newword & 0x00f)<<1)&0xFF);
			char/*UINT8*/ g = (char)(((newword & 0x0f0)>>2)&0xFF);
			char/*UINT8*/ b = (char)(((newword & 0xf00)>>7)&0xFF);
	
			if(sys16_dactype == 0)
			{
				/* dac_type == 0 (from GCS file) */
				if ((newword & 0x1000) != 0) r|=1;
				if ((newword & 0x2000) != 0) g|=2;
				if ((newword & 0x8000) != 0) g|=1;
				if ((newword & 0x4000) != 0) b|=1;
			}
			else if(sys16_dactype == 1)
			{
				/* dac_type == 1 (from GCS file) Shinobi Only*/
				if ((newword & 0x1000) != 0) r|=1;
				if ((newword & 0x4000) != 0) g|=2;
				if ((newword & 0x8000) != 0) g|=1;
				if ((newword & 0x2000) != 0) b|=1;
			}
			if (Machine.scrbitmap.depth == 8) /* 8 bit shadows */
			{
				if(sys16_freezepalette==0)
				{
					palette_change_color( offset/2,
						(r << 3) | (r >> 3), /* 5 bits red */
						(g << 2) | (g >> 4), /* 6 bits green */
						(b << 3) | (b >> 3) /* 5 bits blue */
					);
				}
				else
				{
					r=(char)(((r << 3) | (r >> 3))&0xFF); /* 5 bits red */
					g=(char)(((g << 2) | (g >> 4))&0xFF); /* 6 bits green */
					b=(char)(((b << 3) | (b >> 3))&0xFF); /* 5 bits blue */
					sys16_palettedirty[offset/2]=0xff000000+(r<<16)+(g<<8)+b;
				}
			}
			else
			{
                            throw new UnsupportedOperationException("Unimplemented"); 
/*TODO*///				if(!sys16_freezepalette)
/*TODO*///				{
/*TODO*///					r=(r << 3) | (r >> 2); /* 5 bits red */
/*TODO*///					g=(g << 2) | (g >> 4); /* 6 bits green */
/*TODO*///					b=(b << 3) | (b >> 2); /* 5 bits blue */
/*TODO*///	
/*TODO*///					palette_change_color( offset/2,r,g,b);
/*TODO*///	
/*TODO*///					/* shadow color */
/*TODO*///	
/*TODO*///					r= r * 160 / 256;
/*TODO*///					g= g * 160 / 256;
/*TODO*///					b= b * 160 / 256;
/*TODO*///	
/*TODO*///					palette_change_color( offset/2+Machine.drv.total_colors/2,r,g,b);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					r=(r << 3) | (r >> 3); /* 5 bits red */
/*TODO*///					g=(g << 2) | (g >> 4); /* 6 bits green */
/*TODO*///					b=(b << 3) | (b >> 3); /* 5 bits blue */
/*TODO*///					sys16_palettedirty[offset/2]=0xff000000+(r<<16)+(g<<8)+b;
/*TODO*///	
/*TODO*///					r= r * 160 / 256;
/*TODO*///					g= g * 160 / 256;
/*TODO*///					b= b * 160 / 256;
/*TODO*///					sys16_palettedirty[offset/2+Machine.drv.total_colors/2]=0xff000000+(r<<16)+(g<<8)+b;
/*TODO*///				}
			}
			paletteram.WRITE_WORD(offset, newword);
		}
	} };
	
	
	static void sys16_refresh_palette()
	{
		int i;
		int/*UINT8*/ r,g,b;
	
		for(i=0;i<Machine.drv.total_colors;i++)
		{
			if(sys16_palettedirty[i]!=0)
			{
				r=((sys16_palettedirty[i]&0x00ff0000) >> 16)&0xFF;
				g=((sys16_palettedirty[i]&0x0000ff00) >> 8)&0xFF;
				b=((sys16_palettedirty[i]&0x000000ff))&0xFF;
				palette_change_color(i,r,g,b);
				sys16_palettedirty[i]=0;
			}
		}
	}
	static void update_page(){
		int i,r,c,ro,co, all_dirty = 0;
		int offsets[][]={{0,0},{64,0},{0,32},{64,32}};
	
		if( old_tile_bank1 != sys16_tile_bank1 ){
			all_dirty = 1;
			old_tile_bank1 = sys16_tile_bank1;
		}
		if( old_tile_bank0 != sys16_tile_bank0 ){
			all_dirty = 1;
			old_tile_bank0 = sys16_tile_bank0;
			tilemap_mark_all_tiles_dirty( text_layer );
		}
		if (all_dirty != 0)
		{
			tilemap_mark_all_tiles_dirty( background );
			tilemap_mark_all_tiles_dirty( foreground );
			if (sys16_18_mode != 0)
			{
				tilemap_mark_all_tiles_dirty( background2 );
				tilemap_mark_all_tiles_dirty( foreground2 );
			}
		}
		else
		{
			for(i=0;i<4;i++)
			{
				co=offsets[i][0];
				ro=offsets[i][1];
				if( old_bg_page[i]!=sys16_bg_page[i] )
				{
					old_bg_page[i] = sys16_bg_page[i];
					for(r=0;r<32;r++)
						for(c=0;c<64;c++)
							tilemap_mark_tile_dirty( background, c+co, r+ro );
				}
				if( old_fg_page[i]!=sys16_fg_page[i] )
				{
					old_fg_page[i] = sys16_fg_page[i];
					for(r=0;r<32;r++)
						for(c=0;c<64;c++)
							tilemap_mark_tile_dirty( foreground, c+co, r+ro );
				}
				if (sys16_18_mode != 0)
				{
					if( old_bg2_page[i]!=sys16_bg2_page[i] )
					{
						old_bg2_page[i] = sys16_bg2_page[i];
						for(r=0;r<32;r++)
							for(c=0;c<64;c++)
								tilemap_mark_tile_dirty( background2, c+co, r+ro );
					}
					if( old_fg2_page[i]!=sys16_fg2_page[i] )
					{
						old_fg2_page[i] = sys16_fg2_page[i];
						for(r=0;r<32;r++)
							for(c=0;c<64;c++)
								tilemap_mark_tile_dirty( foreground2, c+co, r+ro );
					}
				}
			}
		}
	}
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		UShortPtr source = new UShortPtr(sys16_tileram);
	
		if( row<32 ){
			if( col<64 ){
				source.offset += (64*32*sys16_bg_page[0])*2;
			}
			else {
				source.offset += (64*32*sys16_bg_page[1])*2;
			}
		}
		else {
			if( col<64 ){
				source.offset += (64*32*sys16_bg_page[2])*2;
			}
			else {
				source.offset += (64*32*sys16_bg_page[3])*2;
			}
		}
		row = row%32;
		col = col%64;
	
		{
			int data = source.read(row*64+col);
			int tile_number = (data&0xfff) +
					0x1000*((data&sys16_tilebank_switch)!=0?sys16_tile_bank1:sys16_tile_bank0);
	
			if(sys16_textmode==0)
			{
				SET_TILE_INFO( 0, tile_number, (data>>6)&0x7f );
			}
			else
			{
				SET_TILE_INFO( 0, tile_number, (data>>5)&0x7f );
			}
			switch(sys16_bg_priority_mode) {
				case 1:		// Alien Syndrome
					tile_info.priority = (data&0x8000)!=0?(char)1:(char)0;
					break;
				case 2:		// Body Slam / wrestwar
					if((data&0xff00) >= sys16_bg_priority_value)
						tile_info.priority = 1;
					else
						tile_info.priority = 0;
					break;
				case 3:		// sys18 games
					if ((data & 0x8000) != 0)
						tile_info.priority = 2;
					else if((data&0xff00) >= sys16_bg_priority_value)
						tile_info.priority = 1;
					else
						tile_info.priority = 0;
					break;
			}
		}
	} };
	
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		UShortPtr source = new UShortPtr(sys16_tileram);
	
		if( row<32 ){
			if( col<64 ){
				source.offset += (64*32*sys16_fg_page[0])*2;
			}
			else {
				source.offset += (64*32*sys16_fg_page[1])*2;
			}
		}
		else {
			if( col<64 ){
				source.offset += (64*32*sys16_fg_page[2])*2;
			}
			else {
				source.offset += (64*32*sys16_fg_page[3])*2;
			}
		}
		row = row%32;
		col = col%64;
	
		{
			int data = source.read(row*64+col);
			int tile_number = (data&0xfff) +
					0x1000*((data&sys16_tilebank_switch)!=0?sys16_tile_bank1:sys16_tile_bank0);
	
			if(sys16_textmode==0)
			{
				SET_TILE_INFO( 0, tile_number, (data>>6)&0x7f );
			}
			else
			{
				SET_TILE_INFO( 0, tile_number, (data>>5)&0x7f );
			}
			switch(sys16_fg_priority_mode)
			{
				case 1:		// alien syndrome
					tile_info.priority = (data&0x8000)!=0?(char)1:(char)0;
	//				if(READ_WORD(&paletteram[((data>>6)&0x7f)*16]) !=0 && tile_info.priority==1)
	//					tile_info.flags=TILE_IGNORE_TRANSPARENCY;
					break;
	
				case 3:
					if((data&0xff00) >= sys16_fg_priority_value)
						tile_info.priority = 1;
					else
						tile_info.priority = 0;
					break;
	
				default:
					if(sys16_fg_priority_mode>=0)
						tile_info.priority = (data&0x8000)!=0?(char)1:(char)0;
					break;
			}
		}
	} };
	
	public static WriteHandlerPtr get_bg2_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		UShortPtr source = new UShortPtr(sys16_tileram);
	
		if( row<32 ){
			if( col<64 ){
				source.offset += 64*32*sys16_bg2_page[0];
			}
			else {
				source.offset += 64*32*sys16_bg2_page[1];
			}
		}
		else {
			if( col<64 ){
				source.offset += 64*32*sys16_bg2_page[2];
			}
			else {
				source.offset += 64*32*sys16_bg2_page[3];
			}
		}
		row = row%32;
		col = col%64;
	
		{
			int data = source.read(row*64+col);
			int tile_number = (data&0xfff) +
					0x1000*((data&0x1000)!=0?sys16_tile_bank1:sys16_tile_bank0);
			if(sys16_textmode==0)
			{
				SET_TILE_INFO( 0, tile_number, (data>>6)&0x7f );
			}
			else
			{
				SET_TILE_INFO( 0, tile_number, (data>>5)&0x7f );
			}
			tile_info.priority = 0;
		}
	} };
	
	public static WriteHandlerPtr get_fg2_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		UShortPtr source = new UShortPtr(sys16_tileram);
	
		if( row<32 ){
			if( col<64 ){
				source.offset += 64*32*sys16_fg2_page[0];
			}
			else {
				source.offset += 64*32*sys16_fg2_page[1];
			}
		}
		else {
			if( col<64 ){
				source.offset += 64*32*sys16_fg2_page[2];
			}
			else {
				source.offset += 64*32*sys16_fg2_page[3];
			}
		}
		row = row%32;
		col = col%64;
	
		{
			int data = source.read(row*64+col);
			int tile_number = (data&0xfff) +
					0x1000*((data&0x1000)!=0?sys16_tile_bank1:sys16_tile_bank0);
	
			if(sys16_textmode==0)
			{
				SET_TILE_INFO( 0, tile_number, (data>>6)&0x7f );
			}
			else
			{
				SET_TILE_INFO( 0, tile_number, (data>>5)&0x7f );
			}
			if((data&0xff00) >= sys16_fg_priority_value)
				tile_info.priority = 1;
			else
				tile_info.priority = 0;
		}
	} };
	
	
	public static WriteHandlerPtr sys16_tileram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = sys16_tileram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		if( oldword != newword ){
			int row,col,page;
			sys16_tileram.WRITE_WORD(offset,newword);
			offset = offset/2;
			col = offset%64;
			row = (offset/64)%32;
			page = offset/(64*32);
	
			if( sys16_bg_page[0]==page ) tilemap_mark_tile_dirty( background, col, row );
			if( sys16_bg_page[1]==page ) tilemap_mark_tile_dirty( background, col+64, row );
			if( sys16_bg_page[2]==page ) tilemap_mark_tile_dirty( background, col, row+32 );
			if( sys16_bg_page[3]==page ) tilemap_mark_tile_dirty( background, col+64, row+32 );
	
			if( sys16_fg_page[0]==page ) tilemap_mark_tile_dirty( foreground, col, row );
			if( sys16_fg_page[1]==page ) tilemap_mark_tile_dirty( foreground, col+64, row );
			if( sys16_fg_page[2]==page ) tilemap_mark_tile_dirty( foreground, col, row+32 );
			if( sys16_fg_page[3]==page ) tilemap_mark_tile_dirty( foreground, col+64, row+32 );
	
			if (sys16_18_mode != 0)
			{
				if( sys16_bg2_page[0]==page ) tilemap_mark_tile_dirty( background2, col, row );
				if( sys16_bg2_page[1]==page ) tilemap_mark_tile_dirty( background2, col+64, row );
				if( sys16_bg2_page[2]==page ) tilemap_mark_tile_dirty( background2, col, row+32 );
				if( sys16_bg2_page[3]==page ) tilemap_mark_tile_dirty( background2, col+64, row+32 );
	
				if( sys16_fg2_page[0]==page ) tilemap_mark_tile_dirty( foreground2, col, row );
				if( sys16_fg2_page[1]==page ) tilemap_mark_tile_dirty( foreground2, col+64, row );
				if( sys16_fg2_page[2]==page ) tilemap_mark_tile_dirty( foreground2, col, row+32 );
				if( sys16_fg2_page[3]==page ) tilemap_mark_tile_dirty( foreground2, col+64, row+32 );
			}
		}
	} };
	
	public static ReadHandlerPtr sys16_tileram_r = new ReadHandlerPtr() { public int handler(int offset){
		return sys16_tileram.READ_WORD(offset);
	} };
	
	/***************************************************************************/
	
	public static WriteHandlerPtr get_text_tile_info = new WriteHandlerPtr() { public void handler(int col, int row){
		UShortPtr source = new UShortPtr(sys16_textram);
		int tile_number = source.read(row*64+col + (64-40));
		int pri = tile_number >> 8;
		if(sys16_textmode==0)
		{
			SET_TILE_INFO( 0, (tile_number&0x1ff) + sys16_tile_bank0 * 0x1000, (tile_number>>9)%8 );
		}
		else
		{
			SET_TILE_INFO( 0, (tile_number&0xff)  + sys16_tile_bank0 * 0x1000, (tile_number>>8)%8 );
		}
		if(pri>=sys16_textlayer_lo_min && pri<=sys16_textlayer_lo_max)
			tile_info.priority = 1;
		if(pri>=sys16_textlayer_hi_min && pri<=sys16_textlayer_hi_max)
			tile_info.priority = 0;
	} };
	
	public static WriteHandlerPtr sys16_textram_w = new WriteHandlerPtr() { public void handler(int offset, int data){
		int oldword = sys16_textram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
		if( oldword != newword ){
			int row,col;
			sys16_textram.WRITE_WORD(offset,newword);
			offset = (offset/2);
			col = (offset%64);
			row = offset/64;
			col -= (64-40);
			if( col>=0 && col<40 && row<28 ){
				tilemap_mark_tile_dirty( text_layer, col, row );
			}
		}
	} };
	
	public static ReadHandlerPtr sys16_textram_r = new ReadHandlerPtr() { public int handler(int offset){
		return sys16_textram.READ_WORD(offset);
	} };
	
	/***************************************************************************/
	
	public static VhStopPtr sys16_vh_stop = new VhStopPtr() { public void handler() {
	
	} };
	
	
	public static VhStartPtr sys16_vh_start = new VhStartPtr() { public int handler() {
		if(sys16_bg1_trans==0)
			background = tilemap_create(
				get_bg_tile_info,
				TILEMAP_OPAQUE,
				8,8,
				64*2,32*2 );
		else
			background = tilemap_create(
				get_bg_tile_info,
				TILEMAP_TRANSPARENT,
				8,8,
				64*2,32*2 );
	
		foreground = tilemap_create(
			get_fg_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			64*2,32*2 );
	
		text_layer = tilemap_create(
			get_text_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			40,28 );
	
		sprite_list = sprite_list_create( NUM_SPRITES, SPRITE_LIST_BACK_TO_FRONT | SPRITE_LIST_RAW_DATA );
	
		sprite_set_shade_table(shade_table);
	
		if( background!=null && foreground!=null && text_layer!=null && sprite_list!=null ){
			/* initialize all entries to black - needed for Golden Axe*/
			int i;
			for( i=0; i<Machine.drv.total_colors; i++ ){
				palette_change_color( i, 0,0,0 );
			}
			//memset(palette_used_colors[0], PALETTE_COLOR_UNUSED, Machine.drv.total_colors);
                        for(int k=0; k<Machine.drv.total_colors; k++)
                        {
                            palette_used_colors.write(k, PALETTE_COLOR_UNUSED);
                        }
			if (Machine.scrbitmap.depth == 8) /* 8 bit shadows */
			{
				int j,color;
				for(j = 0, i = Machine.drv.total_colors/2;j<sys16_MaxShadowColors;i++,j++)
				{
					color=j * 160 / (sys16_MaxShadowColors-1);
	//				color=j * 128 / (sys16_MaxShadowColors-1);
					color=color | 0x04;
					palette_change_color(i, color, color, color);
	//				palette_change_color(i, j * 128 / (sys16_MaxShadowColors-1), j * 128 / (sys16_MaxShadowColors-1), j * 128 / (sys16_MaxShadowColors-1));
				}
			}
			if(sys16_MaxShadowColors==32)
				sys16_MaxShadowColors_Shift = ShadowColorsShift;
			else if(sys16_MaxShadowColors==16)
				sys16_MaxShadowColors_Shift = ShadowColorsShift+1;
				
			for(i=0;i<MAXCOLOURS;i++)
			{
				sys16_palettedirty[i]=0;
			}
			sys16_freezepalette=0;
	
			sprite_list.max_priority = 3;
			sprite_list.sprite_type = SPRITE_TYPE_ZOOM;
	
			if (sys16_bg1_trans != 0) background.transparent_pen = 0;
			foreground.transparent_pen = 0;
			text_layer.transparent_pen = 0;
	
			sys16_tile_bank0 = 0;
			sys16_tile_bank1 = 1;
	
			sys16_fg_scrollx = 0;
			sys16_fg_scrolly = 0;
	
			sys16_bg_scrollx = 0;
			sys16_bg_scrolly = 0;
	
			sys16_refreshenable = 1;
			sys16_clear_screen = 0;
	
			/* common defaults */
			sys16_update_proc = null;
			sys16_spritesystem = 1;
			sys16_sprxoffset = -0xb8;
			sys16_textmode = 0;
			sys16_bgxoffset = 0;
			sys16_dactype = 0;
			sys16_bg_priority_mode=0;
			sys16_fg_priority_mode=0;
			sys16_spritelist_end=0xffff;
			sys16_tilebank_switch=0x1000;
	
			// Defaults for sys16 games
			sys16_textlayer_lo_min=0;
			sys16_textlayer_lo_max=0x7f;
			sys16_textlayer_hi_min=0x80;
			sys16_textlayer_hi_max=0xff;
	
			sys16_18_mode=0;
	
			return 0;
		}
		return 1;
	} };
	
	public static VhStartPtr sys16_ho_vh_start = new VhStartPtr() { public int handler() {
		int ret;
		sys16_bg1_trans=1;
	
		ret = sys16_vh_start.handler();
		if (ret != 0) return 1;
	
		sys16_textlayer_lo_min=0;
		sys16_textlayer_lo_max=0;
		sys16_textlayer_hi_min=0;
		sys16_textlayer_hi_max=0xff;
	
		sys16_bg_priority_mode=-1;
		sys16_bg_priority_value=0x1800;
		sys16_fg_priority_value=0x2000;
		return 0;
	} };
	
	public static VhStartPtr sys16_or_vh_start = new VhStartPtr() { public int handler() {
		int ret;
		sys16_bg1_trans=1;
	
		ret = sys16_vh_start.handler();
		if (ret != 0) return 1;
	
		sys16_textlayer_lo_min=0;
		sys16_textlayer_lo_max=0;
		sys16_textlayer_hi_min=0;
		sys16_textlayer_hi_max=0xff;
	
		sys16_bg_priority_mode=-1;
		sys16_bg_priority_value=0x1800;
		sys16_fg_priority_value=0x2000;
		return 0;
	} };
	
	
	public static VhStartPtr sys18_vh_start = new VhStartPtr() { public int handler() {
		int ret;
		sys16_bg1_trans=1;
	
		background2 = tilemap_create(
			get_bg2_tile_info,
			TILEMAP_OPAQUE,
			8,8,
			64*2,32*2 );
	
		foreground2 = tilemap_create(
			get_fg2_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			64*2,32*2 );
	
		if( background2!=null && foreground2!=null)
		{
			ret = sys16_vh_start.handler();
			if (ret != 0) return 1;
	
			foreground2.transparent_pen = 0;
	
			if (sys18_splittab_fg_x != null)
			{
				tilemap_set_scroll_rows( foreground , 64 );
				tilemap_set_scroll_rows( foreground2 , 64 );
			}
			if (sys18_splittab_bg_x != null)
			{
				tilemap_set_scroll_rows( background , 64 );
				tilemap_set_scroll_rows( background2 , 64 );
			}
	
			sys16_textlayer_lo_min=0;
			sys16_textlayer_lo_max=0x1f;
			sys16_textlayer_hi_min=0x20;
			sys16_textlayer_hi_max=0xff;
	
			sys16_18_mode=1;
			sys16_bg_priority_mode=3;
			sys16_fg_priority_mode=3;
			sys16_bg_priority_value=0x1800;
			sys16_fg_priority_value=0x2000;
			return 0;
		}
		return 1;
	} };
	
	
	/***************************************************************************/
	
	static void get_sprite_info(  ){
	//	const struct rectangle *clip = &Machine.drv.visible_area;
		UShortArray base_pal = new UShortArray(Machine.gfx[0].colortable, 1024);
		UBytePtr base_gfx = new UBytePtr(memory_region(REGION_GFX2));
	
		UShortPtr source = new UShortPtr(sys16_spriteram);
		sprite sprite[] = sprite_list.sprite;
                int sprite_ptr=0;
		int finish = sprite_ptr + NUM_SPRITES;//const struct sprite *finish = sprite + NUM_SPRITES;
	
		int passshot_y=0;
		int passshot_width=0;

		switch( sys16_spritesystem  ){
			case 1: /* standard sprite hardware (Shinobi, Altered Beast, Golden Axe, ...) */
	/*
		0	bottom--	top-----	(screen coordinates)
		1	???????X	XXXXXXXX	(screen coordinate)
		2	???????F	FWWWWWWW	(flipx, flipy, logical width)
		3	TTTTTTTT	TTTTTTTT	(pen data)
		4	????BBBB	PPCCCCCC	(attributes: bank, priority, color)
		5	??????ZZ	ZZZZZZZZ	zoomx
		6	??????ZZ	ZZZZZZZZ	zoomy (defaults to zoomx)
		7	?						"sprite offset"
	*/
			while( sprite_ptr<finish ){
				char ypos = source.read(0);
				char width = source.read(2);
				int top = ypos&0xff;
				int bottom = ypos>>8;
	
				if( bottom == 0xff || width ==sys16_spritelist_end){ /* end of spritelist marker */
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
	
				if(bottom !=0 && bottom > top)
				{
					char attributes = source.read(4);
					char zoomx = (char)(source.read(5)&0x3ff);
					char zoomy = (char)((source.read(6)&0x3ff));
					int gfx = source.read(3)*4;
	
					if( zoomy==0 || source.read(6)==0xffff ) zoomy = zoomx; /* if zoomy is 0, use zoomx instead */
	
					sprite[sprite_ptr].x = source.read(1) + sys16_sprxoffset;
					sprite[sprite_ptr].y = top;
					sprite[sprite_ptr].priority = 3-((attributes>>6)&0x3);
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, ((attributes&0x3f)<<4));
	
					sprite[sprite_ptr].total_height = bottom-top;
					sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height*(0x400+zoomy)/0x400;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;
	
					if ((attributes&0x3f)==0x3f)	// shadow sprite
						sprite[sprite_ptr].flags|= SPRITE_SHADOW;
						
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0){
						sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
							gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1);
						}
						else {
							gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
						}
					}
					else {
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
							gfx += 4;
						}
						else {
							gfx += sprite[sprite_ptr].line_offset;
						}
					}
	
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*(0x800-zoomx)/0x800;
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx , (gfx &0x3ffff) + (sys16_obj_bank[(attributes>>8)&0xf] << 17));
	
				}
	
				sprite_ptr++;
				source.offset += 8*2;
			}
			break;
	
			case 8: /* Passing shot 4p */
				passshot_y=-0x23;
				passshot_width=1;
			case 0: /* Passing shot */
	/*
		0	???????X	XXXXXXXX	(screen coordinate)
		1	bottom--	top-----	(screen coordinates)
		2	XTTTTTTT	YTTTTTTT	(pen data, flipx, flipy)
		3	????????	?WWWWWWW	(logical width)
		4	??????ZZ	ZZZZZZZZ	zoom
		5	PP???CCC	BBBB????	(attributes: bank, priority, color)
		6,7	(unused)
	*/
			while( sprite_ptr<finish ){
				int attributes = source.read(5);
				int ypos = source.read(1);
				int bottom = (ypos>>8)+passshot_y;
				int top = (ypos&0xff)+passshot_y;
				sprite[sprite_ptr].flags = 0;
	
				if( bottom>top && ypos!=0xffff ){
					int bank = (attributes>>4)&0xf;
					int number = source.read(2);
					int width = source.read(3);
	
					int zoom = source.read(4)&0x3ff;
					int xpos = source.read(0) + sys16_sprxoffset;
	
					sprite[sprite_ptr].priority = 3-((attributes>>14)&0x3);
					if (passshot_width != 0) /* 4 player bootleg version */
					{
						width=-width;
						number-=width*(bottom-top-1)-1;
					}
	
					if ((number & 0x8000) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x0080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;
					sprite[sprite_ptr].flags |= SPRITE_VISIBLE;
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, ((attributes>>4)&0x3f0));
					sprite[sprite_ptr].total_height = bottom - top;
					sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height*(0x400+zoom)/0x400;
	
					if (((attributes>>8)&0x3f)==0x3f)	// shadow sprite
						sprite[sprite_ptr].flags|= SPRITE_SHADOW;
						
					width &= 0x7f;
	
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0) width = 0x80-width;
	
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset = width*4;
	
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
						bank = (bank-1) & 0xf;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0){
							xpos += 4;
						}
						else {
							number += 1-width;
						}
					}
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, number*4 + (sys16_obj_bank[bank] << 17));
	
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0){
						sprite[sprite_ptr].pen_data.offset -= sprite[sprite_ptr].tile_height*sprite[sprite_ptr].tile_width;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0) sprite[sprite_ptr].pen_data.inc( 2 );
					}
	
					sprite[sprite_ptr].x = xpos;
					sprite[sprite_ptr].y = top+2;
	
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0){
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
							sprite[sprite_ptr].tile_width-=4;
							sprite[sprite_ptr].pen_data.inc(4);
						}
						else {
							sprite[sprite_ptr].pen_data.inc( sprite[sprite_ptr].line_offset );
						}
					}
	
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*(0x800-zoom)/0x800;
				}
				sprite_ptr++;
				source.inc( 8 );
			}
			break;
	
			case 4: // Aurail
	/*
		0	bottom--	top-----	(screen coordinates)
		1	???????X	XXXXXXXX	(screen coordinate)
		2	???????F	FWWWWWWW	(flipx, flipy, logical width)
		3	TTTTTTTT	TTTTTTTT	(pen data)
		4	????BBBB	PPCCCCCC	(attributes: bank, priority, color)
		5	??????ZZ	ZZZZZZZZ	zoomx
		6	??????ZZ	ZZZZZZZZ	zoomy (defaults to zoomx)
		7	?						"sprite offset"
	*/
			while( sprite_ptr<finish ){
				int ypos = source.read(0);
				int width = source.read(2);
				int attributes = source.read(4);
				int top = ypos&0xff;
				int bottom = ypos>>8;
	
				if( width == sys16_spritelist_end) {
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
				if(bottom !=0 && bottom > top)
				{
					int zoomx = source.read(5)&0x3ff;
					int zoomy = (source.read(6)&0x3ff);
					int gfx = source.read(3)*4;
		
					if( zoomy==0 ) zoomy = zoomx; /* if zoomy is 0, use zoomx instead */
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, ((attributes&0x3f)<<4));
	
					sprite[sprite_ptr].x = source.read(1) + sys16_sprxoffset;;
					sprite[sprite_ptr].y = top;
					sprite[sprite_ptr].priority = 3-((attributes>>6)&0x3);
	
					sprite[sprite_ptr].total_height = bottom-top;
					sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height*(0x400+zoomy)/0x400;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;

					if ((attributes&0x3f)==0x3f)	// shadow sprite
						sprite[sprite_ptr].flags|= SPRITE_SHADOW;

					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY ) != 0){
						sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1);
						}
						else {
							gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
						}
					}
					else {
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4;
						}
						else {
							gfx += sprite[sprite_ptr].line_offset;
						}
					}
	
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*(0x800-zoomx)/0x800;
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, (gfx &0x3ffff) + (sys16_obj_bank[(attributes>>8)&0xf] << 17));
	
				}
				sprite_ptr++;
				source.inc( 8 );
			}
			break;
			case 3:	// Fantzone
				{
					int spr_no=0;
					while( sprite_ptr<finish ){
						int ypos = source.read(0);
						int pal=(source.read(4)>>8)&0x3f;
						int top = ypos&0xff;
						int bottom = ypos>>8;
	
						if( bottom == 0xff ){ /* end of spritelist marker */
							do {
								sprite[sprite_ptr].flags = 0;
								sprite_ptr++;
							} while( sprite_ptr<finish );
							break;
						}
						sprite[sprite_ptr].flags = 0;

						if(bottom !=0 && bottom > top)
						{
							int spr_pri=(source.read(4))&0xf;
							int bank=(source.read(4)>>4) &0xf;
							int[] tsource=new int[4];
							int width;
							int gfx;
	
							if (spr_no==5 && (source.read(4)&0x00ff) == 0x0021 &&
								((source.read(3)&0xff00) == 0x5200 || (source.read(3)&0xff00) == 0x5300)) spr_pri=2; // tears fix for ending boss
	
							tsource[2]=source.read(2);
							tsource[3]=source.read(3);
	
							if((tsource[3] & 0x7f80) == 0x7f80)
							{
								bank=(bank-1)&0xf;
								tsource[3]^=0x8000;
							}
	
							tsource[2] &= 0x00ff;
							if ((tsource[3]&0x8000) != 0)
							{ // reverse
								tsource[2] |= 0x0100;
								tsource[3] &= 0x7fff;
							}
	
							gfx = tsource[3]*4;
							width = tsource[2];
							top++;
							bottom++;
	
							sprite[sprite_ptr].x = source.read(1) + sys16_sprxoffset;
							if(sprite[sprite_ptr].x > 0x140) sprite[sprite_ptr].x-=0x200;
							sprite[sprite_ptr].y = top;
							sprite[sprite_ptr].priority = 3-spr_pri;
							sprite[sprite_ptr].pal_data = new UShortArray(base_pal, (pal<<4));
	
							sprite[sprite_ptr].total_height = bottom-top;
							sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height;
	
							sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
							sprite[sprite_ptr].flags = SPRITE_VISIBLE;
							if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
							if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;

							if (pal==0x3f)	// shadow sprite
								sprite[sprite_ptr].flags|= SPRITE_SHADOW;
	
							if(( sprite[sprite_ptr].flags&SPRITE_FLIPY ) != 0){
								sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
								if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
									gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1);
								}
								else {
									gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
								}
							}
							else {
								if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
									gfx += 4;
								}
								else {
									gfx += sprite[sprite_ptr].line_offset;
								}
							}
	
							sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
							if(width==0) sprite[sprite_ptr].tile_width=320;			// fixes laser
							sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width;
							sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, (gfx &0x3ffff) + (sys16_obj_bank[bank] << 17));
	
						}
	
						source.inc( 8 );
						sprite_ptr++;
						spr_no++;
					}
				}
				break;
			case 2: // Quartet2 /Alexkidd + others
			while( sprite_ptr<finish ){
				char ypos = source.read(0);
				int top = ypos&0xff;
				int bottom = ypos>>8;
	
				if( bottom == 0xff ){ /* end of spritelist marker */
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
	
				if(bottom !=0 && bottom > top)
				{
					char spr_pri=(char)((source.read(4))&0xf);
					char bank=(char)((source.read(4)>>4) &0xf);
					char pal=(char)((source.read(4)>>8)&0x3f);
					char[] tsource=new char[4];
					char width;
					int gfx;
	
					tsource[2]=source.read(2);
					tsource[3]=source.read(3);
	
					if((tsource[3] & 0x7f80) == 0x7f80)
					{
						bank=(char)((bank-1)&0xf);
						tsource[3]^=0x8000;
					}
	
					tsource[2] &= 0x00ff;
					if ((tsource[3]&0x8000)!=0)
					{ // reverse
						tsource[2] |= 0x0100;
						tsource[3] &= 0x7fff;
					}
	
					gfx = tsource[3]*4;
					width = tsource[2];
					top++;
					bottom++;
	
					sprite[sprite_ptr].x = source.read(1) + sys16_sprxoffset;
					if(sprite[sprite_ptr].x > 0x140) sprite[sprite_ptr].x-=0x200;
					sprite[sprite_ptr].y = top;
					sprite[sprite_ptr].priority = 3 - spr_pri;
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal , (pal<<4));
	
					sprite[sprite_ptr].total_height = bottom-top;
					sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;

					if (pal==0x3f)	// shadow sprite
						sprite[sprite_ptr].flags|= SPRITE_SHADOW;

					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY )!=0){
						sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
							gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1);
						}
						else {
							gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
						}
					}
					else {
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX )!=0){
							gfx += 4;
						}
						else {
							gfx += sprite[sprite_ptr].line_offset;
						}
					}
	
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width;
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx , (gfx &0x3ffff) + (sys16_obj_bank[bank] << 17));
				}
	
				source.offset+=8*2;
				sprite_ptr++;
			}
			break;
			case 5: // Hang-On
			while( sprite_ptr<finish ){
				int ypos = source.read(0);
				int top = ypos&0xff;
				int bottom = ypos>>8;
	
				if( bottom == 0xff ){ /* end of spritelist marker */
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
	
				if(bottom !=0 && bottom > top)
				{
					int bank=(source.read(1)>>12);
					int pal=(source.read(4)>>8)&0x3f;
					int[] tsource=new int[4];
					int width;
					int gfx;
					int zoomx,zoomy;
	
					tsource[2]=source.read(2);
					tsource[3]=source.read(3);
	
					zoomx=((source.read(4)>>2) & 0x3f) *(1024/64);
			        zoomy = (1060*zoomx)/(2048-zoomx);
	
	//				if (pal==0x3f)	// ????????????
	//					pal=(bank<<1);
	
					if((tsource[3] & 0x7f80) == 0x7f80)
					{
						bank=(bank-1)&0xf;
						tsource[3]^=0x8000;
					}
	
					if ((tsource[3]&0x8000) != 0)
					{ // reverse
						tsource[2] |= 0x0100;
						tsource[3] &= 0x7fff;
					}
	
					gfx = tsource[3]*4;
					width = tsource[2];
	
					sprite[sprite_ptr].x = ((source.read(1) & 0x3ff) + sys16_sprxoffset);
					if(sprite[sprite_ptr].x >= 0x200) sprite[sprite_ptr].x-=0x200;
					sprite[sprite_ptr].y = top;
					sprite[sprite_ptr].priority = 0;
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, (pal<<4));
	
					sprite[sprite_ptr].total_height = bottom-top;
					sprite[sprite_ptr].tile_height = sprite[sprite_ptr].total_height*(0x400+zoomy)/0x400;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;
	
	
	//				sprite.flags|= SPRITE_PARTIAL_SHADOW;
	//				sprite.shadow_pen=10;
	
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY ) != 0){
						sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1);
						}
						else {
							gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
						}
					}
					else {
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4;
						}
						else {
							gfx += sprite[sprite_ptr].line_offset;
						}
					}
	
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*(0x0800 - zoomx)/0x800;
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, (gfx &0x3ffff) + (sys16_obj_bank[bank] << 17));
	
				}
	
				source.inc( 8 );
				sprite_ptr++;
			}
			break;
			case 6: // Space Harrier
			while( sprite_ptr<finish ){
				int ypos = source.read(0);
				int top = ypos&0xff;
				int bottom = ypos>>8;
	
				if( bottom == 0xff ){ /* end of spritelist marker */
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
	
				if(bottom !=0 && bottom > top)
				{
					int bank=(source.read(1)>>12);
					int pal=(source.read(2)>>8)&0x3f;
					int[] tsource=new int[4];
					int width;
					int gfx;
					int zoomx,zoomy;
	
					tsource[2]=source.read(2)&0xff;
					tsource[3]=source.read(3);
	
					zoomx=(source.read(4) & 0x3f) *(1024/64);
			        zoomy = (1024*zoomx)/(2048-zoomx);
	
					if((tsource[3] & 0x7f80) == 0x7f80)
					{
						bank=(bank-1)&0xf;
						tsource[3]^=0x8000;
					}
	
					if ((tsource[3]&0x8000) != 0)
					{ // reverse
						tsource[2] |= 0x0100;
						tsource[3] &= 0x7fff;
					}
	
					gfx = tsource[3]*4;
					width = tsource[2];
	
					sprite[sprite_ptr].x = ((source.read(1) & 0x3ff) + sys16_sprxoffset);
					if(sprite[sprite_ptr].x >= 0x200) sprite[sprite_ptr].x-=0x200;
					sprite[sprite_ptr].y = top+1;
					sprite[sprite_ptr].priority = 0;
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, (pal<<4));
	
					sprite[sprite_ptr].total_height = bottom-top;
	//				sprite.tile_height = sprite.total_height*(0x400+zoomy)/0x400;
					sprite[sprite_ptr].tile_height = ((sprite[sprite_ptr].total_height)<<4|0xf)*(0x400+zoomy)/0x4000;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if ((width & 0x100) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPX;
					if ((width & 0x080) != 0) sprite[sprite_ptr].flags |= SPRITE_FLIPY;
	
					if (sys16_sh_shadowpal == 0)		// space harrier
					{
						if (pal==sys16_sh_shadowpal)	// shadow sprite
							sprite[sprite_ptr].flags|= SPRITE_SHADOW;
						// I think this looks better, but I'm sure it's wrong.
	//					else if(READ_WORD(&paletteram[2048+pal*2+20]) == 0)
	//					{
	//						sprite.flags|= SPRITE_PARTIAL_SHADOW;
	//						sprite.shadow_pen=10;
	//					}
					}
					else								// enduro
					{
						sprite[sprite_ptr].flags|= SPRITE_PARTIAL_SHADOW;
						sprite[sprite_ptr].shadow_pen=10;
					}
					if(( sprite[sprite_ptr].flags&SPRITE_FLIPY ) != 0){
						sprite[sprite_ptr].line_offset = 512-sprite[sprite_ptr].line_offset;
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4 - sprite[sprite_ptr].line_offset*(sprite[sprite_ptr].tile_height+1) /*+4*/;
						}
						else {
							gfx -= sprite[sprite_ptr].line_offset*sprite[sprite_ptr].tile_height;
						}
					}
					else {
						if(( sprite[sprite_ptr].flags&SPRITE_FLIPX ) != 0){
							gfx += 4 /*+ 4*/;		// +2 ???
						}
						else {
							gfx += sprite[sprite_ptr].line_offset;
						}
					}
	
					sprite[sprite_ptr].line_offset<<=1;
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*(0x0800 - zoomx)/0x800;
	
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, (((gfx &0x3ffff) + (sys16_obj_bank[bank] << 17)) << 1));
				}
	
				source.inc( 8 );
				sprite_ptr++;
			}
			break;
			case 7: // Out Run
			while( sprite_ptr<finish ){
	
				if( source.read(0) == 0xffff ){ /* end of spritelist marker */
					do {
						sprite[sprite_ptr].flags = 0;
						sprite_ptr++;
					} while( sprite_ptr<finish );
					break;
				}
				sprite[sprite_ptr].flags = 0;
	
				if ((source.read(0)&0x4000)==0)
				{
					int bank=(source.read(0)>>8)&7;
					int pal=(source.read(5))&0x7f;
					int width;
					int gfx;
					int zoom;
					int x;
	
					zoom=source.read(4)&0xfff;
	//				if (zoom==0x32c) zoom=0x3cf;	//???
	
					if(zoom==0) zoom=1;
	
					if ((source.read(1)&0x8000)!=0) bank=(bank+1)&0x7;
	
					gfx = (source.read(1)&0x7fff)*4;
					width = source.read(2)>>9;
	
					x = (source.read(2) & 0x1ff);
	
					// patch misplaced sprite on map
	//				if(zoom == 0x3f0 && source[1]==0x142e && (source[0]&0xff)==0x19)
	//					x-=2;
	
					sprite[sprite_ptr].y = source.read(0)&0xff;
					sprite[sprite_ptr].priority = 0;
					sprite[sprite_ptr].pal_data = new UShortArray(base_pal, (pal<<4) + 1024);
	
					sprite[sprite_ptr].total_height = (source.read(5)>>8)+1;
					sprite[sprite_ptr].tile_height = ((sprite[sprite_ptr].total_height<<4)| 0xf)*(zoom)/0x2000;
	
					sprite[sprite_ptr].line_offset = (width&0x7f)*4;
	
					sprite[sprite_ptr].flags = SPRITE_VISIBLE;
					if(pal==0)
						sprite[sprite_ptr].flags|= SPRITE_SHADOW;
					else if((source.read(3)&0x4000) != 0)
	//				if(pal==0 || source[3]&0x4000)
					{
						sprite[sprite_ptr].flags|= SPRITE_PARTIAL_SHADOW;
						sprite[sprite_ptr].shadow_pen=10;
					}
					if((source.read(4)&0x2000)==0)
					{
						if((source.read(4)&0x4000)==0)
						{
							// Should be drawn right to left, but this should be ok.
							x-=(sprite[sprite_ptr].line_offset*2)*0x200/zoom;
							gfx+=4-sprite[sprite_ptr].line_offset;
						}
						else
						{
							int ofs=(sprite[sprite_ptr].line_offset*2)*0x200/zoom;
							x-=ofs;
							sprite[sprite_ptr].flags |= SPRITE_FLIPX;
	
							// x position compensation for rocks in round2R and round4RRR
	/*						if((source[0]&0xff00)==0x0300)
							{
								if (source[1]==0xc027)
								{
									if ((source[2]>>8)==0x59) x += ofs/2;
									else if ((source[2]>>8)>0x59) x += ofs;
								}
								else if (source[1]==0xcf73)
								{
									if ((source[2]>>8)==0x2d) x += ofs/2;
									else if ((source[2]>>8)>0x2d) x += ofs;
								}
								else if (source[1]==0xd3046)
								{
									if ((source[2]>>8)==0x19) x += ofs/2;
									else if ((source[2]>>8)>0x19) x += ofs;
								}
								else if (source[1]==0xd44e)
								{
									if ((source[2]>>8)==0x0d) x += ofs/2;
									else if ((source[2]>>8)>0x0d) x += ofs;
								}
								else if (source[1]==0xd490)
								{
									if ((source[2]>>8)==0x09) x += ofs/2;
									else if ((source[2]>>8)>0x09) x += ofs;
								}
							}
	*/
						}
					}
					else
					{
						if((source.read(4)&0x4000)==0)
						{
							gfx-=sprite[sprite_ptr].line_offset-4;
							sprite[sprite_ptr].flags |= SPRITE_FLIPX;
						}
						else
						{
							if ((source.read(4)&0x8000) != 0)
							{ // patch for car shadow position
								if (source.read(4)==0xe1a9 && source.read(1)==0x5817)
									x-=2;
							}
						}
					}
	
					sprite[sprite_ptr].x = x + sys16_sprxoffset;
	
					sprite[sprite_ptr].line_offset<<=1;
					sprite[sprite_ptr].tile_width = sprite[sprite_ptr].line_offset;
					sprite[sprite_ptr].total_width = sprite[sprite_ptr].tile_width*0x200/zoom;
					sprite[sprite_ptr].pen_data = new UBytePtr(base_gfx, (((gfx &0x3ffff) + (sys16_obj_bank[bank] << 17)) << 1));
				}
				source.inc( 8 );
				sprite_ptr++;
			}
			break;
		}
	}
	
	/***************************************************************************/
	
	static void mark_sprite_colors(){
		UShortPtr source = new UShortPtr(sys16_spriteram);
		UShortPtr finish = new UShortPtr(source,NUM_SPRITES*8);
		int pal_start=1024,pal_size=64;
	
		char[] used=new char[128];
		memset( used, 0, 128 );
	
		switch( sys16_spritesystem ){
			case 1: /* standard sprite hardware */
				do{
					if( source.read(0)>>8 == 0xff || source.read(2) == sys16_spritelist_end) break;
					used[source.read(4)&0x3f] = 1;
					source.offset+=8*2;
				}while( source.offset<finish.offset );
				break;
			case 4: /* Aurail */
				do{
					if( (source.read(2)) == sys16_spritelist_end) break;
					used[source.read(4)&0x3f] = 1;
					source.offset+=8;
				}while( source.offset<finish.offset );
				break;
	
			case 3:	/* Fantzone */
			case 5:	/* Hang-On */
			case 2: /* Quartet2 / alex kidd + others */
				do{
					if( (source.read(0)>>8) == 0xff ) break;;
					used[(source.read(4)>>8)&0x3f] = 1;
					source.offset+=8;
				}while( source.offset<finish.offset );
				break;
			case 6:	/* Space Harrier */
				do{
					if( (source.read(0)>>8) == 0xff ) break;;
					used[(source.read(2)>>8)&0x3f] = 1;
					source.offset+=8;
				}while( source.offset<finish.offset );
				break;
			case 7:	/* Out Run */
				do{
					if( (source.read(0)) == 0xffff ) break;;
					used[(source.read(5))&0x7f] = 1;
					source.offset+=8;
				}while( source.offset<finish.offset );
				pal_start=2048;
				pal_size=128;
				break;
			case 0: /* passing shot */
			case 8: /* passing shot 4p */
				do{
					if( source.read(1)!=0xffff ) used[(source.read(5)>>8)&0x3f] = 1;
					source.offset+=8;
				}while( source.offset<finish.offset );
				break;
		}
	
		{
			UBytePtr pal = new UBytePtr(palette_used_colors,pal_start);
			int i;
			for (i = 0; i < pal_size; i++){
				if ( used[i]!=0 ){
					pal.write(0,  PALETTE_COLOR_UNUSED);
					memset( pal,1,PALETTE_COLOR_USED,14 );
					pal.write(15, PALETTE_COLOR_UNUSED);
				}
				else {
					memset( pal, PALETTE_COLOR_UNUSED, 16 );
				}
				pal.offset += 16;
			}
		}
		if (Machine.scrbitmap.depth == 8) /* 8 bit shadows */
		{
			memset(palette_used_colors,Machine.drv.total_colors/2, PALETTE_COLOR_USED, sys16_MaxShadowColors);
		}
/*TODO*///		else if(sys16_MaxShadowColors != 0) /* 16 bit shadows */
/*TODO*///		{
/*TODO*///			/* Mark the shadowed versions of the used pens */
/*TODO*///			memcpy(&palette_used_colors[Machine.drv.total_colors/2], &palette_used_colors[0], Machine.drv.total_colors/2);
/*TODO*///		}
	}
	
	static void build_shadow_table()
	{
		int i,size;
		int color_start=Machine.drv.total_colors/2;
		/* build the shading lookup table */
		if (Machine.scrbitmap.depth == 8) /* 8 bit shadows */
		{
			if(sys16_MaxShadowColors == 0) return;
			for (i = 0; i < 256; i++)
			{
				//unsigned char r, g, b;
                                char[] r= new char[1];
                                char[] g= new char[1];
                                char[] b= new char[1];
				int y;
				osd_get_pen(i, r, g, b);
				y = (r[0] * 10 + g[0] * 18 + b[0] * 4) >> sys16_MaxShadowColors_Shift;
				shade_table[i] = Machine.pens[color_start + y];
			}
			for(i=0;i<sys16_MaxShadowColors;i++)
			{
				shade_table[Machine.pens[color_start + i]]=Machine.pens[color_start + i];
			}
		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if(sys16_MaxShadowColors != 0)
/*TODO*///			{
/*TODO*///				size=Machine.drv.total_colors/2;
/*TODO*///				for(i=0;i<size;i++)
/*TODO*///				{
/*TODO*///					shade_table[Machine.pens[i]]=Machine.pens[size + i];
/*TODO*///					shade_table[Machine.pens[size+i]]=Machine.pens[size + i];
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				size=Machine.drv.total_colors;
/*TODO*///				for(i=0;i<size;i++)
/*TODO*///				{
/*TODO*///					shade_table[Machine.pens[i]]=Machine.pens[i];
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
	}

        static int freeze_counter_sys16=0;
	public static VhUpdatePtr sys16_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		if (sys16_update_proc != null) sys16_update_proc.handler();
		update_page();
	
	
		// from sys16 emu (Not sure if this is the best place for this?)
		{
			if (sys16_refreshenable==0)
			{
				freeze_counter_sys16=4;
				sys16_freezepalette=1;
			}
			if (freeze_counter_sys16 != 0)
			{
				if (sys16_clear_screen != 0)
					fillbitmap(bitmap,palette_transparent_color,Machine.drv.visible_area);
				freeze_counter_sys16--;
				return;
			}
			else if (sys16_freezepalette != 0)
			{
				sys16_refresh_palette();
				sys16_freezepalette=0;
			}
		}
	
		if (sys16_refreshenable != 0){
	
			if (sys18_splittab_bg_x != null)
			{
				if((sys16_bg_scrollx&0xff00)  != sys16_rowscroll_scroll)
				{
					tilemap_set_scroll_rows( background , 1 );
					tilemap_set_scrollx( background, 0, -320-sys16_bg_scrollx+sys16_bgxoffset );
				}
				else
				{
					int offset, scroll,i;
	
					tilemap_set_scroll_rows( background , 64 );
					offset = 32+((sys16_bg_scrolly&0x1f8) >> 3);
	
					for(i=0;i<29;i++)
					{
						scroll = sys18_splittab_bg_x.READ_WORD(i*2);
						tilemap_set_scrollx( background , (i+offset)&0x3f, -320-(scroll&0x3ff)+sys16_bgxoffset );
					}
				}
			}
			else
			{
				tilemap_set_scrollx( background, 0, -320-sys16_bg_scrollx+sys16_bgxoffset );
			}
	
			if (sys18_splittab_bg_y != null)
			{
				if((sys16_bg_scrolly&0xff00)  != sys16_rowscroll_scroll)
				{
					tilemap_set_scroll_cols( background , 1 );
					tilemap_set_scrolly( background, 0, -256+sys16_bg_scrolly );
				}
				else
				{
					int offset, scroll,i;
	
					tilemap_set_scroll_cols( background , 128 );
					offset = 127-((sys16_bg_scrollx&0x3f8) >> 3)-40+2;
	
					for(i=0;i<41;i++)
					{
						scroll = sys18_splittab_bg_y.READ_WORD((i+24)&0xfffe);
						tilemap_set_scrolly( background , (i+offset)&0x7f, -256+(scroll&0x3ff) );
					}
				}
			}
			else
			{
				tilemap_set_scrolly( background, 0, -256+sys16_bg_scrolly );
			}
	
			if (sys18_splittab_fg_x != null)
			{
				if((sys16_fg_scrollx&0xff00)  != sys16_rowscroll_scroll)
				{
					tilemap_set_scroll_rows( foreground , 1 );
					tilemap_set_scrollx( foreground, 0, -320-sys16_fg_scrollx+sys16_fgxoffset );
				}
				else
				{
					int offset, scroll,i;
	
					tilemap_set_scroll_rows( foreground , 64 );
					offset = 32+((sys16_fg_scrolly&0x1f8) >> 3);
	
					for(i=0;i<29;i++)
					{
						scroll = sys18_splittab_fg_x.READ_WORD(i*2);
	
	
						tilemap_set_scrollx( foreground , (i+offset)&0x3f, -320-(scroll&0x3ff)+sys16_fgxoffset );
					}
				}
			}
			else
			{
				tilemap_set_scrollx( foreground, 0, -320-sys16_fg_scrollx+sys16_fgxoffset );
			}
	
			if (sys18_splittab_fg_y != null)
			{
				if((sys16_fg_scrolly&0xff00)  != sys16_rowscroll_scroll)
				{
					tilemap_set_scroll_cols( foreground , 1 );
					tilemap_set_scrolly( foreground, 0, -256+sys16_fg_scrolly );
				}
				else
				{
					int offset, scroll,i;
	
					tilemap_set_scroll_cols( foreground , 128 );
					offset = 127-((sys16_fg_scrollx&0x3f8) >> 3)-40+2;
	
					for(i=0;i<41;i++)
					{
						scroll = sys18_splittab_fg_y.READ_WORD((i+24)&0xfffe);
						tilemap_set_scrolly( foreground , (i+offset)&0x7f, -256+(scroll&0x3ff) );
					}
				}
			}
			else
			{
				tilemap_set_scrolly( foreground, 0, -256+sys16_fg_scrolly );
			}
	
			if (sys16_quartet_title_kludge != 0)
			{
				int top,bottom,left,right;
				int top2,bottom2,left2,right2;
				rectangle clip=new rectangle();
	
				left = background.clip_left;
				right = background.clip_right;
				top = background.clip_top;
				bottom = background.clip_bottom;
	
				left2 = foreground.clip_left;
				right2 = foreground.clip_right;
				top2 = foreground.clip_top;
				bottom2 = foreground.clip_bottom;
	
				clip.min_x=0;
				clip.min_y=0;
				clip.max_x=1024;
				clip.max_y=512;
	
				tilemap_set_clip( background, clip );
				tilemap_set_clip( foreground, clip );
	
				tilemap_update(  ALL_TILEMAPS  );
	
				background.clip_left = left;
				background.clip_right = right;
				background.clip_top = top;
				background.clip_bottom = bottom;
	
				foreground.clip_left = left2;
				foreground.clip_right = right2;
				foreground.clip_top = top2;
				foreground.clip_bottom = bottom2;
	
			}
			else
				tilemap_update(  ALL_TILEMAPS  );
	
			get_sprite_info();
	
			palette_init_used_colors();
			mark_sprite_colors(); // custom; normally this would be handled by the sprite manager
			sprite_update();
	
			if( palette_recalc()!=null ) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );
			build_shadow_table();
			tilemap_render(  ALL_TILEMAPS  );
	
			if(sys16_quartet_title_kludge==0)
			{
				tilemap_draw( bitmap, background, TILEMAP_IGNORE_TRANSPARENCY );
				if (sys16_bg_priority_mode != 0) tilemap_draw( bitmap, background, TILEMAP_IGNORE_TRANSPARENCY | 1 );
			}
			else
				draw_quartet_title_screen( bitmap, 0 );
	
			sprite_draw(sprite_list,3); // needed for Aurail
			if(sys16_bg_priority_mode==2) tilemap_draw( bitmap, background, 1 );		// body slam (& wrestwar??)
			sprite_draw(sprite_list,2);
			if(sys16_bg_priority_mode==1) tilemap_draw( bitmap, background, 1 );		// alien syndrome / aurail
	
			if(sys16_quartet_title_kludge==0)
			{
				tilemap_draw( bitmap, foreground, 0 );
				sprite_draw(sprite_list,1);
				tilemap_draw( bitmap, foreground, 1 );
			}
			else
			{
				draw_quartet_title_screen( bitmap, 1 );
				sprite_draw(sprite_list,1);
			}
	
			if(sys16_textlayer_lo_max!=0) tilemap_draw( bitmap, text_layer, 1 ); // needed for Body Slam
			sprite_draw(sprite_list,0);
			tilemap_draw( bitmap, text_layer, 0 );
		}
	} };
        
        static int freeze_counter=0;
	
	public static VhUpdatePtr sys18_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		int i;
		if (sys16_update_proc != null) sys16_update_proc.handler();
		update_page();
	
		// from sys16 emu (Not sure if this is the best place for this?)
		{
			
			if (sys16_refreshenable==0)
			{
				freeze_counter=4;
				sys16_freezepalette=1;
			}
			if (freeze_counter != 0)
			{
	//			if (sys16_clear_screen != 0)
	//				fillbitmap(bitmap,palette_transparent_color,&Machine.drv.visible_area);
				freeze_counter--;
				return;
			}
			else if (sys16_freezepalette != 0)
			{
				sys16_refresh_palette();
				sys16_freezepalette=0;
			}
		}
	
		if (sys16_refreshenable != 0){
	
			if (sys18_splittab_bg_x != null)
			{
				int offset,offset2, scroll,scroll2,orig_scroll;
	
				offset = 32+((sys16_bg_scrolly&0x1f8) >> 3);
				offset2 = 32+((sys16_bg2_scrolly&0x1f8) >> 3);
	
				for(i=0;i<29;i++)
				{
					orig_scroll = scroll2 = scroll = sys18_splittab_bg_x.READ_WORD(i*2);
	
					if((sys16_bg_scrollx &0xff00) != 0x8000)
						scroll = sys16_bg_scrollx;
	
					if((sys16_bg2_scrollx &0xff00) != 0x8000)
						scroll2 = sys16_bg2_scrollx;
	
					if ((orig_scroll & 0x8000) != 0)
					{
						tilemap_set_scrollx( background , (i+offset)&0x3f, TILE_LINE_DISABLED );
						tilemap_set_scrollx( background2, (i+offset2)&0x3f, -320-(scroll2&0x3ff)+sys16_bgxoffset );
					}
					else
					{
						tilemap_set_scrollx( background , (i+offset)&0x3f, -320-(scroll&0x3ff)+sys16_bgxoffset );
						tilemap_set_scrollx( background2 , (i+offset2)&0x3f, TILE_LINE_DISABLED );
					}
				}
			}
			else
			{
				tilemap_set_scrollx( background , 0, -320-(sys16_bg_scrollx&0x3ff)+sys16_bgxoffset );
				tilemap_set_scrollx( background2, 0, -320-(sys16_bg2_scrollx&0x3ff)+sys16_bgxoffset );
			}
	
			tilemap_set_scrolly( background , 0, -256+sys16_bg_scrolly );
			tilemap_set_scrolly( background2, 0, -256+sys16_bg2_scrolly );
	
			if (sys18_splittab_fg_x != null)
			{
				int offset,offset2, scroll,scroll2,orig_scroll;
	
				offset = 32+((sys16_fg_scrolly&0x1f8) >> 3);
				offset2 = 32+((sys16_fg2_scrolly&0x1f8) >> 3);
	
				for(i=0;i<29;i++)
				{
					orig_scroll = scroll2 = scroll = sys18_splittab_fg_x.READ_WORD(i*2);
	
					if((sys16_fg_scrollx &0xff00) != 0x8000)
						scroll = sys16_fg_scrollx;
	
					if((sys16_fg2_scrollx &0xff00) != 0x8000)
						scroll2 = sys16_fg2_scrollx;
	
					if ((orig_scroll & 0x8000) != 0)
					{
						tilemap_set_scrollx( foreground , (i+offset)&0x3f, TILE_LINE_DISABLED );
						tilemap_set_scrollx( foreground2, (i+offset2)&0x3f, -320-(scroll2&0x3ff)+sys16_fgxoffset );
					}
					else
					{
						tilemap_set_scrollx( foreground , (i+offset)&0x3f, -320-(scroll&0x3ff)+sys16_fgxoffset );
						tilemap_set_scrollx( foreground2 , (i+offset2)&0x3f, TILE_LINE_DISABLED );
					}
				}
			}
			else
			{
				tilemap_set_scrollx( foreground , 0, -320-(sys16_fg_scrollx&0x3ff)+sys16_fgxoffset );
				tilemap_set_scrollx( foreground2, 0, -320-(sys16_fg2_scrollx&0x3ff)+sys16_fgxoffset );
			}
	
	
			tilemap_set_scrolly( foreground , 0, -256+sys16_fg_scrolly );
			tilemap_set_scrolly( foreground2, 0, -256+sys16_fg2_scrolly );
	
			tilemap_set_enable( background2, sys18_bg2_active );
			tilemap_set_enable( foreground2, sys18_fg2_active );
	
			tilemap_update(  ALL_TILEMAPS  );
			get_sprite_info();
	
			palette_init_used_colors();
			mark_sprite_colors(); // custom; normally this would be handled by the sprite manager
			sprite_update();
	
			if( palette_recalc() != null ) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );
			build_shadow_table();
			tilemap_render(  ALL_TILEMAPS  );
	
			if (sys18_bg2_active != 0)
				tilemap_draw( bitmap, background2, 0 );
			else
				fillbitmap(bitmap,palette_transparent_color,Machine.drv.visible_area);
	
			tilemap_draw( bitmap, background, TILEMAP_IGNORE_TRANSPARENCY );
			tilemap_draw( bitmap, background, TILEMAP_IGNORE_TRANSPARENCY | 1 );	//??
			tilemap_draw( bitmap, background, TILEMAP_IGNORE_TRANSPARENCY | 2 );	//??
	
			sprite_draw(sprite_list,3);
			tilemap_draw( bitmap, background, 1 );
			sprite_draw(sprite_list,2);
			tilemap_draw( bitmap, background, 2 );
	
			if (sys18_fg2_active != 0) tilemap_draw( bitmap, foreground2, 0 );
			tilemap_draw( bitmap, foreground, 0 );
			sprite_draw(sprite_list,1);
			if (sys18_fg2_active != 0) tilemap_draw( bitmap, foreground2, 1 );
			tilemap_draw( bitmap, foreground, 1 );
	
			tilemap_draw( bitmap, text_layer, 1 );
			sprite_draw(sprite_list,0);
			tilemap_draw( bitmap, text_layer, 0 );
		}
	} };
	
/*TODO*///	extern int gr_bitmap_width;
	
	static void gr_colors()
	{
		int i;
		int ver_data;
		int colorflip;
		UBytePtr data_ver=new UBytePtr(gr_ver);
	
		for(i=0;i<224;i++)
		{
			ver_data=data_ver.READ_WORD(0);
			palette_used_colors.write((gr_pal.READ_WORD((ver_data<<1)&0x1fe)&0xff) + gr_palette, PALETTE_COLOR_USED);
	
			if(!((ver_data & 0x500) == 0x100 || (ver_data & 0x300) == 0x200))
			{
				ver_data=ver_data & 0x00ff;
				colorflip = (gr_flip.READ_WORD(ver_data<<1) >> 3) & 1;
	
				palette_used_colors.write( gr_colorflip[colorflip][0] + gr_palette_default , PALETTE_COLOR_USED);
				palette_used_colors.write( gr_colorflip[colorflip][1] + gr_palette_default , PALETTE_COLOR_USED);
				palette_used_colors.write( gr_colorflip[colorflip][2] + gr_palette_default , PALETTE_COLOR_USED);
				palette_used_colors.write( gr_colorflip[colorflip][3] + gr_palette_default , PALETTE_COLOR_USED);
			}
			data_ver.inc(2);
		}
	}
	
	static void render_gr(osd_bitmap bitmap,int priority)
	{
		int i,j;
		UBytePtr data = new UBytePtr(memory_region(REGION_GFX3));
		UBytePtr source=new UBytePtr();
		UBytePtr line=new UBytePtr();
		UShortPtr line16;
		UShortPtr line32;
		UBytePtr data_ver=new UBytePtr(gr_ver);
		int ver_data,hor_pos;
		int[] colors=new int[5];
	//	UINT8 colors[5];
		int fastfill;
		int colorflip;
		int yflip=0,ypos;
		int dx=1,xoff=0;
	
		UShortArray paldata1 = new UShortArray(Machine.gfx[0].colortable, gr_palette);
		UShortArray paldata2 = new UShortArray(Machine.gfx[0].colortable, gr_palette_default);
	
		priority=priority << 10;
	
		if (Machine.scrbitmap.depth == 16) /* 16 bit */
		{
			if(( Machine.orientation & ORIENTATION_SWAP_XY ) != 0)
			{
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x400) == priority)
					{
						colors[0] = paldata1.read( gr_pal.READ_WORD((ver_data<<1)&0x1fe)&0xff );
	
						if((ver_data & 0x500) == 0x100 || (ver_data & 0x300) == 0x200)
						{
							// fill line
							for(j=0;j<320;j++)
							{
								line16=new UShortPtr(bitmap.line[j], ypos);
								line16.write(0, (char) colors[0]);
							}
						}
						else
						{
							// copy line
							ver_data=ver_data & 0x00ff;
							colorflip = (gr_flip.READ_WORD(ver_data<<1) >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
							colors[4] = paldata2.read( gr_colorflip[colorflip][3] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							ver_data = ver_data << gr_bitmap_width;
	
							if ((hor_pos & 0xf000) != 0)
							{
								// reverse
								hor_pos=((0-((hor_pos&0x7ff)^7))+0x9f8)&0x3ff;
							}
							else
							{
								// normal
								hor_pos=(hor_pos+0x200) & 0x3ff;
							}
	
							source = new UBytePtr(data, hor_pos + ver_data + 18 + 8);
	
							for(j=0;j<320;j++)
							{
								line16=new UShortPtr(bitmap.line[xoff+j*dx], ypos);
								line16.write(0, (char) colors[source.readinc()]);
							}
						}
					}
					data_ver.inc(2);
				}
			}
			else
			{
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x400) == priority)
					{
						colors[0] = paldata1.read( gr_pal.READ_WORD((ver_data<<1)&0x1fe)&0xff );
	
						if((ver_data & 0x500) == 0x100 || (ver_data & 0x300) == 0x200)
						{
							line16=new UShortPtr(bitmap.line[ypos]);
							for(j=0;j<320;j++)
							{
								line16.write(0, (char) colors[0]);
                                                                line16.inc(1);
							}
						}
						else
						{
							// copy line
							line16 = new UShortPtr(bitmap.line[ypos], xoff);
							ver_data=ver_data & 0x00ff;
							colorflip = (gr_flip.READ_WORD(ver_data<<1) >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
							colors[4] = paldata2.read( gr_colorflip[colorflip][3] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							ver_data = ver_data << gr_bitmap_width;
	
							if ((hor_pos & 0xf000) != 0)
							{
								// reverse
								hor_pos=((0-((hor_pos&0x7ff)^7))+0x9f8)&0x3ff;
							}
							else
							{
								// normal
								hor_pos=(hor_pos+0x200) & 0x3ff;
							}
	
							source = new UBytePtr(data, hor_pos + ver_data + 18 + 8);
	
							for(j=0;j<320;j++)
							{
								line16.write(0, (char) colors[source.readinc()]);
								line16.inc(dx);
							}
						}
					}
					data_ver.inc(2);
				}
			}
		}
		else /* 8 bit */
		{
			if(( Machine.orientation & ORIENTATION_SWAP_XY ) != 0)
			{
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x400) == priority)
					{
						colors[0] = paldata1.read( gr_pal.READ_WORD((ver_data<<1)&0x1fe)&0xff );
	
						if((ver_data & 0x500) == 0x100 || (ver_data & 0x300) == 0x200)
						{
							// fill line
							for(j=0;j<320;j++)
							{
								new UBytePtr(bitmap.line[j]).write(ypos, colors[0]);
							}
						}
						else
						{
							// copy line
							ver_data=ver_data & 0x00ff;
							colorflip = (gr_flip.READ_WORD(ver_data<<1) >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
							colors[4] = paldata2.read( gr_colorflip[colorflip][3] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							ver_data = ver_data << gr_bitmap_width;
	
							if ((hor_pos & 0xf000) != 0)
							{
								// reverse
								hor_pos=((0-((hor_pos&0x7ff)^7))+0x9f8)&0x3ff;
							}
							else
							{
								// normal
								hor_pos=(hor_pos+0x200) & 0x3ff;
							}
	
							source = new UBytePtr(data, hor_pos + ver_data + 18 + 8);
	
							for(j=0;j<320;j++)
							{
								new UBytePtr(bitmap.line[xoff+j*dx]).write(ypos, colors[source.readinc()]);
							}
						}
					}
					data_ver.inc(2);
				}
			}
			else
			{
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x400) == priority)
					{
						colors[0] = paldata1.read( gr_pal.READ_WORD((ver_data<<1)&0x1fe)&0xff );
	
						if((ver_data & 0x500) == 0x100 || (ver_data & 0x300) == 0x200)
						{
							// fill line
							line32 = new UShortPtr(bitmap.line[ypos]);
							fastfill = colors[0] + (colors[0] << 8) + (colors[0] << 16) + (colors[0] << 24);
							for(j=0;j<320;j+=4)
							{
								line32.write(0, (char) fastfill);
                                                                line32.inc(1);
							}
						}
						else
						{
							// copy line
							line = new UBytePtr(bitmap.line[ypos], xoff);
							ver_data=ver_data & 0x00ff;
							colorflip = (gr_flip.READ_WORD(ver_data<<1) >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
							colors[4] = paldata2.read( gr_colorflip[colorflip][3] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							ver_data = ver_data << gr_bitmap_width;
	
							if ((hor_pos & 0xf000) != 0)
							{
								// reverse
								hor_pos=((0-((hor_pos&0x7ff)^7))+0x9f8)&0x3ff;
							}
							else
							{
								// normal
								hor_pos=(hor_pos+0x200) & 0x3ff;
							}
	
							source = new UBytePtr(data, hor_pos + ver_data + 18 + 8);
	
							for(j=0;j<320;j++)
							{
								line.write(0, colors[source.readinc()]);
								line.inc(dx);
							}
						}
					}
					data_ver.inc(2);
				}
			}
		}
	}
	
	
	
	//static int freeze_counter=0;
        
	// Refresh for hang-on, etc.
	public static VhUpdatePtr sys16_ho_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		if (sys16_update_proc != null) sys16_update_proc.handler();
		update_page();
	
		// from sys16 emu (Not sure if this is the best place for this?)
		{
			
			if (sys16_refreshenable==0)
			{
				freeze_counter=4;
				sys16_freezepalette=1;
			}
			if (freeze_counter != 0)
			{
				freeze_counter--;
				return;
			}
			else if (sys16_freezepalette != 0)
			{
				sys16_refresh_palette();
				sys16_freezepalette=0;
			}
		}
	
		if (sys16_refreshenable != 0){
	
			tilemap_set_scrollx( background, 0, -320-sys16_bg_scrollx+sys16_bgxoffset );
			tilemap_set_scrollx( foreground, 0, -320-sys16_fg_scrollx+sys16_fgxoffset );
	
			tilemap_set_scrolly( background, 0, -256+sys16_bg_scrolly );
			tilemap_set_scrolly( foreground, 0, -256+sys16_fg_scrolly );
	
			tilemap_update(  ALL_TILEMAPS  );
			get_sprite_info();
	
			palette_init_used_colors();
			mark_sprite_colors(); // custom; normally this would be handled by the sprite manager
			sprite_update();
			gr_colors();
	
			if( palette_recalc() != null) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );
			build_shadow_table();
			tilemap_render(  ALL_TILEMAPS  );
	
			render_gr(bitmap,0);
	
			tilemap_draw( bitmap, background, 0 );
			tilemap_draw( bitmap, foreground, 0 );
	
			render_gr(bitmap,1);
	
			sprite_draw(sprite_list,0);
			tilemap_draw( bitmap, text_layer, 0 );
	
		}
	} };
	
	
	static void grv2_colors()
	{
		int i;
		int ver_data;
		int colorflip,colorflip_info;
		UBytePtr data_ver=new UBytePtr(gr_ver);
	
		for(i=0;i<224;i++)
		{
			ver_data=data_ver.READ_WORD(0);
	
			if((ver_data & 0x800)==0)
			{
				ver_data=ver_data & 0x01ff;
				colorflip_info = gr_flip.READ_WORD(ver_data<<1);
	
				palette_used_colors.write( (((colorflip_info >> 8) & 0x1f) + 0x20) + gr_palette_default, PALETTE_COLOR_USED);
	
				colorflip = (colorflip_info >> 3) & 1;
	
				palette_used_colors.write( gr_colorflip[colorflip][0] + gr_palette_default , PALETTE_COLOR_USED);
				palette_used_colors.write( gr_colorflip[colorflip][1] + gr_palette_default , PALETTE_COLOR_USED);
				palette_used_colors.write( gr_colorflip[colorflip][2] + gr_palette_default , PALETTE_COLOR_USED);
			}
			else
			{
				palette_used_colors.write((ver_data&0x3f) + gr_palette, PALETTE_COLOR_USED);
			}
			data_ver.inc(2);
		}
	}
	
	static void render_grv2(osd_bitmap bitmap,int priority)
	{
		int i,j;
		UBytePtr data = new UBytePtr(memory_region(REGION_GFX3));
		UBytePtr source = new UBytePtr(), source2 = new UBytePtr(), temp = new UBytePtr();
		UBytePtr line = new UBytePtr();
		UShortPtr line16;
		/*UINT32*/ /*IntSubArray*/ UShortArray line32;
		UBytePtr data_ver = new UBytePtr(gr_ver);
		int ver_data,hor_pos,hor_pos2;
		int[] colors=new int[5];
		int fastfill;
		int colorflip,colorflip_info;
		int yflip=0,ypos;
		int dx=1,xoff=0;
	
		int second_road = gr_second_road.READ_WORD(0);
	
		UShortArray paldata1 = new UShortArray(Machine.gfx[0].colortable, gr_palette);
		UShortArray paldata2 = new UShortArray(Machine.gfx[0].colortable, gr_palette_default);
	
		priority=priority << 11;
	
		if (Machine.scrbitmap.depth == 16) /* 16 bit */
		{
			if(( Machine.orientation & ORIENTATION_SWAP_XY ) != 0)
			{
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x800) == priority)
					{
	
						if ((ver_data & 0x800) != 0)
						{
							colors[0] = paldata1.read( ver_data&0x3f );
							// fill line
							for(j=0;j<320;j++)
							{
								line16=new UShortPtr(bitmap.line[j], ypos);
								line16.write(0, (char) colors[0]);
							}
						}
						else
						{
							// copy line
							ver_data=ver_data & 0x01ff;		//???
							colorflip_info = gr_flip.READ_WORD(ver_data<<1);
	
							colors[0] = paldata2.read( ((colorflip_info >> 8) & 0x1f) + 0x20 );		//??
	
							colorflip = (colorflip_info >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							hor_pos2= (gr_hor.READ_WORD((ver_data<<1)+0x400) );
	
							ver_data=ver_data>>1;
							if( ver_data != 0 )
							{
								ver_data = (ver_data-1) << gr_bitmap_width;
							}
	
							source  = new UBytePtr(data, ((hor_pos +0x200) & 0x7ff) + 768 + ver_data + 8);
							source2 = new UBytePtr(data, ((hor_pos2+0x200) & 0x7ff) + 768 + ver_data + 8);
	
							switch(second_road)
							{
								case 0:	source2=source;	break;
								case 2:	temp=source;source=source2;source2=temp; break;
								case 3:	source=source2;	break;
							}
	
							source2.inc();
	
							for(j=0;j<320;j++)
							{
								line16=new UShortPtr(bitmap.line[xoff+j*dx], ypos);
								if(source2.read() <= source.read())
									line16.write(0, (char) colors[source.read()]);
								else
									line16.write(0, (char) colors[source2.read()]);
								source.inc();
								source2.inc();
							}
						}
					}
					data_ver.inc(2);
				}
			}
			else
			{
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x800) == priority)
					{
	
						if ((ver_data & 0x800) != 0)
						{
							colors[0] = paldata1.read( ver_data&0x3f );
							// fill line
							line16 = new UShortPtr(bitmap.line[ypos], 0);
							for(j=0;j<320;j++)
							{
								line16.write(0, (char) colors[0]);
                                                                line16.inc(1);
							}
						}
						else
						{
							// copy line
							line16 = new UShortPtr(bitmap.line[ypos], xoff);
							ver_data=ver_data & 0x01ff;		//???
							colorflip_info = gr_flip.READ_WORD(ver_data<<1);
	
							colors[0] = paldata2.read( ((colorflip_info >> 8) & 0x1f) + 0x20 );		//??
	
							colorflip = (colorflip_info >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							hor_pos2= (gr_hor.READ_WORD((ver_data<<1)+0x400) );
	
							ver_data=ver_data>>1;
							if( ver_data != 0 )
							{
								ver_data = (ver_data-1) << gr_bitmap_width;
							}
	
							source  = new UBytePtr(data, ((hor_pos +0x200) & 0x7ff) + 768 + ver_data + 8);
							source2 = new UBytePtr(data, ((hor_pos2+0x200) & 0x7ff) + 768 + ver_data + 8);
	
							switch(second_road)
							{
								case 0:	source2=source;	break;
								case 2:	temp=source;source=source2;source2=temp; break;
								case 3:	source=source2;	break;
							}
	
							source2.inc();
	
							for(j=0;j<320;j++)
							{
								if(source2.read() <= source.read())
									line16 = new UShortPtr(colors[source.read()]);
								else
									line16 = new UShortPtr(colors[source2.read()]);
								source.inc();
								source2.inc();
								line16.inc(dx);
							}
						}
					}
					data_ver.inc(2);
				}
			}
		}
		else
		{
			if(( Machine.orientation & ORIENTATION_SWAP_XY ) != 0)
			{
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){ 
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x800) == priority)
					{
	
						if ((ver_data & 0x800) != 0)
						{
							colors[0] = paldata1.read( ver_data&0x3f );
							// fill line
							for(j=0;j<320;j++)
							{
								new UBytePtr(bitmap.line[j]).write(ypos, colors[0]);
							}
						}
						else
						{
							// copy line
							ver_data=ver_data & 0x01ff;		//???
							colorflip_info = gr_flip.READ_WORD(ver_data<<1);
	
							colors[0] = paldata2.read( ((colorflip_info >> 8) & 0x1f) + 0x20 );		//??
	
							colorflip = (colorflip_info >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							hor_pos2= (gr_hor.READ_WORD((ver_data<<1)+0x400) );
	
							ver_data=ver_data>>1;
							if( ver_data != 0 )
							{
								ver_data = (ver_data-1) << gr_bitmap_width;
							}
	
							source  = new UBytePtr(data, ((hor_pos +0x200) & 0x7ff) + 768 + ver_data + 8);
							source2 = new UBytePtr(data, ((hor_pos2+0x200) & 0x7ff) + 768 + ver_data + 8);
	
							switch(second_road)
							{
								case 0:	source2=source;	break;
								case 2:	temp=source;source=source2;source2=temp; break;
								case 3:	source=source2;	break;
							}
	
							source2.inc();
	
							for(j=0;j<320;j++)
							{
								if(source2.read() <= source.read())
									new UBytePtr(bitmap.line[xoff+j*dx]).write(ypos, colors[source.read()]);
								else
									new UBytePtr(bitmap.line[xoff+j*dx]).write(ypos, colors[source2.read()]);
								source.inc();
								source2.inc();
							}
						}
					}
					data_ver.inc(2);
				}
			}
			else
			{
				if(( Machine.orientation & ORIENTATION_FLIP_X ) != 0){
					dx=-1;
					xoff=319;
				}
				if(( Machine.orientation & ORIENTATION_FLIP_Y ) != 0){
					yflip=1;
				}
	
				for(i=0;i<224;i++)
				{
					if (yflip != 0) ypos=223-i;
					else ypos=i;
					ver_data=data_ver.READ_WORD(0);
					if((ver_data & 0x800) == priority)
					{
	
						if ((ver_data & 0x800) != 0)
						{
							colors[0] = paldata1.read( ver_data&0x3f );
							// fill line
							line32 = new UShortArray(bitmap.line[ypos], 0);
							fastfill = colors[0] + (colors[0] << 8) + (colors[0] << 16) + (colors[0] << 24);
							for(j=0;j<320;j+=4)
							{
								line32.write( 0, fastfill );
                                                                line32.inc(1);
							}
						}
						else
						{
							// copy line
							line = new UBytePtr(bitmap.line[ypos], xoff);
							ver_data=ver_data & 0x01ff;		//???
							colorflip_info = gr_flip.READ_WORD(ver_data<<1);
	
							colors[0] = paldata2.read( ((colorflip_info >> 8) & 0x1f) + 0x20 );		//??
	
							colorflip = (colorflip_info >> 3) & 1;
	
							colors[1] = paldata2.read( gr_colorflip[colorflip][0] );
							colors[2] = paldata2.read( gr_colorflip[colorflip][1] );
							colors[3] = paldata2.read( gr_colorflip[colorflip][2] );
	
							hor_pos = (gr_hor.READ_WORD(ver_data<<1) );
							hor_pos2= (gr_hor.READ_WORD((ver_data<<1)+0x400) );
	
							ver_data=ver_data>>1;
							if( ver_data != 0 )
							{
								ver_data = (ver_data-1) << gr_bitmap_width;
							}
	
							source  = new UBytePtr(data, ((hor_pos +0x200) & 0x7ff) + 768 + ver_data + 8);
							source2 = new UBytePtr(data, ((hor_pos2+0x200) & 0x7ff) + 768 + ver_data + 8);
	
							switch(second_road)
							{
								case 0:	source2=source;	break;
								case 2:	temp=source;source=source2;source2=temp; break;
								case 3:	source=source2;	break;
							}
	
							source2.inc();
	
							for(j=0;j<320;j++)
							{
								if(source2.read() <= source.read())
									line.write( colors[source.read()] );
								else
									line.write( colors[source2.read()] );
								source.inc();
								source2.inc();
								line.inc(dx);
							}
						}
					}
					data_ver.inc(2);
				}
			}
		}
	}
	
        // Refresh for Outrun
	public static VhUpdatePtr sys16_or_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
		if (sys16_update_proc != null) sys16_update_proc.handler();
		update_page();
	
		// from sys16 emu (Not sure if this is the best place for this?)
		{
			
			if (sys16_refreshenable==0)
			{
				freeze_counter=4;
				sys16_freezepalette=1;
			}
			if (freeze_counter != 0)
			{
				freeze_counter--;
				return;
			}
			else if (sys16_freezepalette != 0)
			{
				sys16_refresh_palette();
				sys16_freezepalette=0;
			}
		}
	
		if (sys16_refreshenable != 0){
	
			tilemap_set_scrollx( background, 0, -320-sys16_bg_scrollx+sys16_bgxoffset );
			tilemap_set_scrollx( foreground, 0, -320-sys16_fg_scrollx+sys16_fgxoffset );
	
			tilemap_set_scrolly( background, 0, -256+sys16_bg_scrolly );
			tilemap_set_scrolly( foreground, 0, -256+sys16_fg_scrolly );
	
			tilemap_update(  ALL_TILEMAPS  );
			get_sprite_info();
	
			palette_init_used_colors();
			mark_sprite_colors(); // custom; normally this would be handled by the sprite manager
			sprite_update();
			grv2_colors();
	
			if( palette_recalc() != null ) tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );
			build_shadow_table();
			tilemap_render(  ALL_TILEMAPS  );
	
			render_grv2(bitmap,1);
	
			tilemap_draw( bitmap, background, 0 );
			tilemap_draw( bitmap, foreground, 0 );
	
			render_grv2(bitmap,0);
	
			sprite_draw(sprite_list,0);
	
			tilemap_draw( bitmap, text_layer, 0 );
	
		}
	} };
	
	
	// hideous kludge to display quartet title screen correctly
	static void draw_quartet_title_screen( osd_bitmap bitmap,int playfield )
	{
		UBytePtr xscroll=new UBytePtr(),yscroll=new UBytePtr();
		int r,c,scroll;
		tilemap _tilemap;
		rectangle clip=new rectangle();
	
		int top,bottom,left,right;
	
	
		if(playfield==0) // background
		{
			xscroll=new UBytePtr(sys16_textram, 0x0fc0);
			yscroll=new UBytePtr(sys16_textram, 0x0f58);
			_tilemap=background;
		}
		else
		{
			xscroll=new UBytePtr(sys16_textram, 0x0f80);
			yscroll=new UBytePtr(sys16_textram, 0x0f30);
			_tilemap=foreground;
		}
	
		left = _tilemap.clip_left;
		right = _tilemap.clip_right;
		top = _tilemap.clip_top;
		bottom = _tilemap.clip_bottom;
	
		for(r=0;r<14;r++)
		{
			clip.min_y=r*16;
			clip.max_y=r*16+15;
			for(c=0;c<10;c++)
			{
				clip.min_x=c*32;
				clip.max_x=c*32+31;
				tilemap_set_clip( _tilemap, clip );
				scroll = xscroll.READ_WORD(r*4)&0x3ff;
				tilemap_set_scrollx( _tilemap, 0, (-320-scroll+sys16_bgxoffset)&0x3ff );
				scroll = yscroll.READ_WORD(c*4)&0x1ff;
				tilemap_set_scrolly( _tilemap, 0, (-256+scroll)&0x1ff );
				tilemap_draw( bitmap, _tilemap, 0 );
			}
		}
	/*
		for(r=0;r<28;r++)
		{
			clip.min_y=r*8;
			clip.max_y=r*8+7;
			for(c=0;c<20;c++)
			{
				clip.min_x=c*16;
				clip.max_x=c*16+15;
				tilemap_set_clip( tilemap, &clip );
				scroll = READ_WORD(&xscroll[r*2])&0x3ff;
				tilemap_set_scrollx( tilemap, 0, (-320-scroll+sys16_bgxoffset)&0x3ff );
				scroll = READ_WORD(&yscroll[c*2])&0x1ff;
				tilemap_set_scrolly( tilemap, 0, (-256+scroll)&0x1ff );
				tilemap_draw( bitmap, tilemap, 0 );
			}
		}
	*/
		_tilemap.clip_left = left;
		_tilemap.clip_right = right;
		_tilemap.clip_top = top;
		_tilemap.clip_bottom = bottom;
	}	
}
