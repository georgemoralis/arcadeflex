/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.gfxobj.*;
import static gr.codebb.arcadeflex.v037b7.mame.gfxobjH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;

public class namcos1
{
	
    static UBytePtr get_gfx_pointer(GfxElement gfxelement, int c, int line){ return new UBytePtr(gfxelement.gfxdata, (c*gfxelement.height+line) * gfxelement.line_modulo); }

    public static final int SPRITECOLORS = 2048;
    public static final int TILECOLORS = 1536;
    public static final int BACKGROUNDCOLOR = (SPRITECOLORS+2*TILECOLORS);

/*TODO*///	/* support non use tilemap system draw routine */
/*TODO*///	#define NAMCOS1_DIRECT_DRAW 1
	
	
	/*
	  video ram map
	  0000-1fff : scroll playfield (0) : 64*64*2
	  2000-3fff : scroll playfield (1) : 64*64*2
	  4000-5fff : scroll playfield (2) : 64*64*2
	  6000-6fff : scroll playfield (3) : 64*32*2
	  7000-700f : ?
	  7010-77ef : fixed playfield (4)  : 36*28*2
	  77f0-77ff : ?
	  7800-780f : ?
	  7810-7fef : fixed playfield (5)  : 36*28*2
	  7ff0-7fff : ?
	*/
	static UBytePtr namcos1_videoram;
	/*
	  paletteram map (s1ram  0x0000-0x7fff)
	  0000-17ff : palette page0 : sprite
	  2000-37ff : palette page1 : playfield
	  4000-57ff : palette page2 : playfield (shadow)
	  6000-7fff : work ram ?
	*/
	public static UBytePtr namcos1_paletteram=new UBytePtr();
	/*
	  controlram map (s1ram 0x8000-0x9fff)
	  0000-07ff : work ram
	  0800-0fef : sprite ram	: 0x10 * 127
	  0ff0-0fff : display control register
	  1000-1fff : playfield control register
	*/
	static UBytePtr namcos1_controlram=new UBytePtr();

	public static final int FG_OFFSET = 0x7000;

	public static final int MAX_PLAYFIELDS = 6;
	public static final int MAX_SPRITES    = 127;

	static class playfield {
		UBytePtr base;
		int 	scroll_x;
		int 	scroll_y;
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		int 	width;
		int 	height;
/*TODO*///	#endif
		tilemap _tilemap;
		int 	color;
	};

	static playfield[] playfields=new playfield[MAX_PLAYFIELDS];

/*TODO*///	#if NAMCOS1_DIRECT_DRAW
	static int namcos1_tilemap_need = 0;
	static int namcos1_tilemap_used;

	static UBytePtr char_state=new UBytePtr();
        static final int CHAR_BLANK	= 0;
        static final int CHAR_FULL	= 1;
/*TODO*///	#endif
	
	/* playfields maskdata for tilemap */
	static UBytePtr[] mask_ptr;
	static UBytePtr mask_data=new UBytePtr();

	/* graphic object */
	static gfx_object_list objectlist;
	static gfx_object[] objects;
	
	/* palette dirty information */
	static int[] sprite_palette_state = new int[MAX_SPRITES+1];
	static int[] tilemap_palette_state = new int[MAX_PLAYFIELDS];

	/* per game scroll adjustment */
	static int[] scrolloffsX=new int[4];
	static int[] scrolloffsY=new int[4];

	static int sprite_fixed_sx;
	static int sprite_fixed_sy;
	static int flipscreen;

	public static VhConvertColorPromPtr namcos1_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		for (i = 0; i < Machine.drv.total_colors; i++) {
			palette[i*3+0] = 0;
			palette[i*3+1] = 0;
			palette[i*3+2] = 0;
		}
	} };
	
	static void namcos1_set_flipscreen(int flip)
	{
		int i;
	
		int pos_x[] = {0x0b0,0x0b2,0x0b3,0x0b4};
		int pos_y[] = {0x108,0x108,0x108,0x008};
		int neg_x[] = {0x1d0,0x1d2,0x1d3,0x1d4};
		int neg_y[] = {0x1e8,0x1e8,0x1e8,0x0e8};
	
		flipscreen = flip;
		if(flip==0)
		{
			for ( i = 0; i < 4; i++ ) {
				scrolloffsX[i] = pos_x[i];
				scrolloffsY[i] = pos_y[i];
			}
		}
		else
		{
			for ( i = 0; i < 4; i++ ) {
				scrolloffsX[i] = neg_x[i];
				scrolloffsY[i] = neg_y[i];
			}
		}
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
/*TODO*///	#endif
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? TILEMAP_FLIPX|TILEMAP_FLIPY : 0);
	}
	
	public static WriteHandlerPtr namcos1_playfield_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* 0-15 : scrolling */
		if ( offset < 16 )
		{
			int whichone = offset / 4;
			int xy = offset & 2;
			if ( xy == 0 ) { /* scroll x */
				if ((offset & 1) != 0)
					playfields[whichone].scroll_x = ( playfields[whichone].scroll_x & 0xff00 ) | data;
				else
					playfields[whichone].scroll_x = ( playfields[whichone].scroll_x & 0xff ) | ( data << 8 );
			} else { /* scroll y */
				if ((offset & 1) != 0)
					playfields[whichone].scroll_y = ( playfields[whichone].scroll_y & 0xff00 ) | data;
				else
					playfields[whichone].scroll_y = ( playfields[whichone].scroll_y & 0xff ) | ( data << 8 );
			}
		}
		/* 16-21 : priority */
		else if ( offset < 22 )
		{
			/* bit 0-2 priority */
			/* bit 3   disable	*/
			int whichone = offset - 16;
			objects[whichone].priority = data & 7;
			objects[whichone].visible = (data&0xf8)!=0 ? 0 : 1;
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			if (namcos1_tilemap_used != 0)
/*TODO*///	#endif
			playfields[whichone]._tilemap.enable = objects[whichone].visible;
		}
		/* 22,23 unused */
		else if (offset < 24)
		{
		}
		/* 24-29 palette */
		else if ( offset < 30 )
		{
			int whichone = offset - 24;
			if (playfields[whichone].color != (data & 7))
			{
				playfields[whichone].color = data & 7;
				tilemap_palette_state[whichone] = 1;
			}
		}
	} };
	
	public static ReadHandlerPtr namcos1_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return namcos1_videoram.read(offset);
	} };
	
	public static WriteHandlerPtr namcos1_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (namcos1_videoram.read(offset) != data)
		{
			namcos1_videoram.write(offset, data);
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			if (namcos1_tilemap_used != 0)
			{
/*TODO*///	#endif
			if(offset < FG_OFFSET)
			{	/* background 0-3 */
				int layer = offset/0x2000;
				int num = (offset &= 0x1fff)/2;
				tilemap_mark_tile_dirty(playfields[layer]._tilemap,num%64,num/64);
			}
			else
			{	/* foreground 4-5 */
				int layer = (offset&0x800)!=0 ? 5 : 4;
				int num = ((offset&0x7ff)-0x10)/2;
				if (num >= 0 && num < 0x3f0)
					tilemap_mark_tile_dirty(playfields[layer]._tilemap,num%36,num/36);
			}
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			}
/*TODO*///	#endif
		}
	} };
	
	public static ReadHandlerPtr namcos1_paletteram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return namcos1_paletteram.read(offset);
	} };
	
	public static WriteHandlerPtr namcos1_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if(namcos1_paletteram.read(offset) != data)
		{
			namcos1_paletteram.write(offset, data);
			if ((offset&0x1fff) < 0x1800)
			{
				if (offset < 0x2000)
				{
					sprite_palette_state[(offset&0x7f0)/16] = 1;
				}
				else
				{
					int i,color;
	
					color = (offset&0x700)/256;
					for(i=0;i<MAX_PLAYFIELDS;i++)
					{
						if (playfields[i].color == color)
							tilemap_palette_state[i] = 1;
					}
				}
			}
		}
	} };
	
/*TODO*///	static void namcos1_palette_refresh(int start,int offset,int num)
/*TODO*///	{
/*TODO*///		int color;
/*TODO*///	
/*TODO*///		offset = (offset/0x800)*0x2000 + (offset&0x7ff);
/*TODO*///	
/*TODO*///		for (color = start; color < start + num; color++)
/*TODO*///		{
/*TODO*///			int r,g,b;
/*TODO*///			r = namcos1_paletteram[offset];
/*TODO*///			g = namcos1_paletteram[offset + 0x0800];
/*TODO*///			b = namcos1_paletteram[offset + 0x1000];
/*TODO*///			palette_change_color(color,r,g,b);
/*TODO*///	
/*TODO*///			if (offset >= 0x2000)
/*TODO*///			{
/*TODO*///				r = namcos1_paletteram[offset + 0x2000];
/*TODO*///				g = namcos1_paletteram[offset + 0x2800];
/*TODO*///				b = namcos1_paletteram[offset + 0x3000];
/*TODO*///				palette_change_color(color+TILECOLORS,r,g,b);
/*TODO*///			}
/*TODO*///			offset++;
/*TODO*///		}
/*TODO*///	}
	
	public static WriteHandlerPtr namcos1_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int sprite_sizemap[] = {16,8,32,4};
		int num = offset / 0x10;
		gfx_object object = objectlist.objects[num+MAX_PLAYFIELDS];
		UBytePtr base = new UBytePtr(namcos1_controlram, 0x0800 + num*0x10);
		int sx, sy;
		int resize_x=0,resize_y=0;
	
		switch(offset&0x0f)
		{
		case 0x04:
			/* bit.6-7 : x size (16/8/32/4) */
			/* bit.5   : flipx */
			/* bit.3-4 : x offset */
			/* bit.0-2 : code.8-10 */
			object.width = sprite_sizemap[(data>>6)&3];
			object.flipx = ((data>>5)&1) ^ flipscreen;
			object.left = (data&0x18) & (~(object.width-1));
			object.code = (base.read(4)&7)*256 + base.read(5);
			resize_x=1;
			break;
		case 0x05:
			/* bit.0-7 : code.0-7 */
			object.code = (base.read(4)&7)*256 + base.read(5);
			break;
		case 0x06:
			/* bit.1-7 : color */
			/* bit.0   : x draw position.8 */
			object.color = data>>1;
			object.transparency = object.color==0x7f ? TRANSPARENCY_PEN_TABLE : TRANSPARENCY_PEN;
/*TODO*///	#if 0
/*TODO*///			if(object.color==0x7f && !(Machine.gamedrv.flags & GAME_REQUIRES_16BIT))
/*TODO*///				usrintf_showmessage("This driver requires GAME_REQUIRES_16BIT flag");
/*TODO*///	#endif
		case 0x07:
			/* bit.0-7 : x draw position.0-7 */
			resize_x=1;
			break;
		case 0x08:
			/* bit.5-7 : priority */
			/* bit.3-4 : y offset */
			/* bit.1-2 : y size (16/8/32/4) */
			/* bit.0   : flipy */
			object.priority = (data>>5)&7;
			object.height = sprite_sizemap[(data>>1)&3];
			object.flipy = (data&1) ^ flipscreen;
			object.top = (data&0x18) & (~(object.height-1));
		case 0x09:
			/* bit.0-7 : y draw position */
			resize_y=1;
			break;
		default:
			return;
		}
		if (resize_x != 0)
		{
			/* sx */
			sx = (base.read(6)&1)*256 + base.read(7);
			sx += sprite_fixed_sx;
	
			if (flipscreen != 0) sx = 210 - sx - object.width;
	
			if( sx > 480  ) sx -= 512;
			if( sx < -32  ) sx += 512;
			if( sx < -224 ) sx += 512;
			object.sx = sx;
		}
		if (resize_y != 0)
		{
			/* sy */
			sy = sprite_fixed_sy - base.read(9);
	
			if (flipscreen != 0) sy = 222 - sy;
			else sy = sy - object.height;
	
			if( sy > 224 ) sy -= 256;
			if( sy < -32 ) sy += 256;
			object.sy = sy;
		}
		object.dirty_flag = GFXOBJ_DIRTY_ALL;
	} };
	
	/* display control block write */
	/*
	0-3  unknown
	4-5  sprite offset x
	6	 flip screen
	7	 sprite offset y
	8-15 unknown
	*/
	public static WriteHandlerPtr namcos1_displaycontrol_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr disp_reg = new UBytePtr(namcos1_controlram, 0xff0);
		int newflip;
	
		switch(offset)
		{
		case 0x02: /* ?? */
			break;
		case 0x04: /* sprite offset X */
		case 0x05:
			sprite_fixed_sx = disp_reg.read(4)*256+disp_reg.read(5) - 151;
			if( sprite_fixed_sx > 480 ) sprite_fixed_sx -= 512;
			if( sprite_fixed_sx < -32 ) sprite_fixed_sx += 512;
			break;
		case 0x06: /* flip screen */
			newflip = (disp_reg.read(6)&1)^0x01;
			if(flipscreen != newflip)
			{
				namcos1_set_flipscreen(newflip);
			}
			break;
		case 0x07: /* sprite offset Y */
			sprite_fixed_sy = 239 - disp_reg.read(7);
			break;
		case 0x0a: /* ?? */
			/* 00 : blazer,dspirit,quester */
			/* 40 : others */
			break;
		case 0x0e: /* ?? */
			/* 00 : blazer,dangseed,dspirit,pacmania,quester */
			/* 06 : others */
		case 0x0f: /* ?? */
			/* 00 : dangseed,dspirit,pacmania */
			/* f1 : blazer */
			/* f8 : galaga88,quester */
			/* e7 : others */
			break;
		}
/*TODO*///	#if 0
/*TODO*///		{
/*TODO*///			char buf[80];
/*TODO*///			sprintf(buf,"%02x:%02x:%02x:%02x:%02x%02x,%02x,%02x,%02x:%02x:%02x:%02x:%02x:%02x:%02x:%02x",
/*TODO*///			disp_reg[0],disp_reg[1],disp_reg[2],disp_reg[3],
/*TODO*///			disp_reg[4],disp_reg[5],disp_reg[6],disp_reg[7],
/*TODO*///			disp_reg[8],disp_reg[9],disp_reg[10],disp_reg[11],
/*TODO*///			disp_reg[12],disp_reg[13],disp_reg[14],disp_reg[15]);
/*TODO*///			usrintf_showmessage(buf);
/*TODO*///		}
/*TODO*///	#endif
	} };
	
	public static WriteHandlerPtr namcos1_videocontrol_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		namcos1_controlram.write(offset, data);
		/* 0000-07ff work ram */
		if(offset <= 0x7ff)
			return;
		/* 0800-0fef sprite ram */
		if(offset <= 0x0fef)
		{
			namcos1_spriteram_w.handler(offset&0x7ff, data);
			return;
		}
		/* 0ff0-0fff display control ram */
		if(offset <= 0x0fff)
		{
			namcos1_displaycontrol_w.handler(offset&0x0f, data);
			return;
		}
		/* 1000-1fff control ram */
		namcos1_playfield_control_w.handler(offset&0xff, data);
	} };
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
	static void draw_background( osd_bitmap bitmap, int layer )
	{
		UBytePtr vid = new UBytePtr(playfields[layer].base);
		int width	= playfields[layer].width;
		int height	= playfields[layer].height;
		int color	= objects[layer].color;
		int scrollx = playfields[layer].scroll_x;
		int scrolly = playfields[layer].scroll_y;
		int sx,sy;
		int offs_x,offs_y;
		int ox,xx;
		int max_x = Machine.visible_area.max_x;
		int max_y = Machine.visible_area.max_y;
		int code;
	
		scrollx -= scrolloffsX[layer];
		scrolly -= scrolloffsY[layer];
	
		if (flipscreen != 0) {
			scrollx = -scrollx;
			scrolly = -scrolly;
		}
	
		if (scrollx < 0) scrollx = width - (-scrollx) % width;
		else scrollx %= width;
		if (scrolly < 0) scrolly = height - (-scrolly) % height;
		else scrolly %= height;
	
		width/=8;
		height/=8;
		sx	= (scrollx%8);
		offs_x	= width - (scrollx/8);
		sy	= (scrolly%8);
		offs_y	= height - (scrolly/8);
		if(sx>0)
		{
			sx-=8;
			offs_x--;
		}
		if(sy>0)
		{
			sy-=8;
			offs_y--;
		}
	
		/* draw for visible area */
		offs_x *= 2;
		width  *= 2;
		offs_y *= width;
		height = height * width;
		for ( ;sy <= max_y; offs_y+=width,sy+=8)
		{
			offs_y %= height;
			for( ox=offs_x,xx=sx; xx <= max_x; ox+=2,xx+=8 )
			{
				ox %= width;
				code = vid.read(offs_y+ox+1) + ( ( vid.read(offs_y+ox) & 0x3f ) << 8 );
				if(char_state.read(code)!=(char) CHAR_BLANK)
				{
					drawgfx( bitmap,Machine.gfx[1],
							code,color,
							flipscreen, flipscreen,
							flipscreen!=0 ? max_x -7 -xx : xx,
							flipscreen!=0 ? max_y -7 -sy : sy,
							Machine.visible_area,
							(char_state.read(code)==(char)CHAR_FULL) ? TRANSPARENCY_NONE : TRANSPARENCY_PEN,
							char_state.read(code));
				}
			}
		}
	}
	
	static void draw_foreground( osd_bitmap bitmap, int layer )
	{
		int offs;
		UBytePtr vid = new UBytePtr(playfields[layer].base);
		int color = objects[layer].color;
		int max_x = Machine.visible_area.max_x;
		int max_y = Machine.visible_area.max_y;
	
		for ( offs = 0; offs < 36*28*2; offs += 2 )
		{
			int sx,sy,code;
	
			code = vid.read(offs+1) + ( ( vid.read(offs+0) & 0x3f ) << 8 );
			if(char_state.read(code)!=(char)CHAR_BLANK)
			{
				sx = ((offs/2) % 36)*8;
				sy = ((offs/2) / 36)*8;
				if (flipscreen != 0)
				{
					sx = max_x -7 - sx;
					sy = max_y -7 - sy;
				}
	
				drawgfx( bitmap,Machine.gfx[1],
						code,color,
						flipscreen, flipscreen,
						sx,sy,
						Machine.visible_area,
						(char_state.read(code)==(char)CHAR_FULL) ? TRANSPARENCY_NONE : TRANSPARENCY_PEN,
						char_state.read(code));
			}
		}
	}
/*TODO*///	#endif
	
	/* tilemap callback */
	static UBytePtr info_vram=new UBytePtr();
	static int info_color;
	
	static WriteHandlerPtr background_get_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
                int tile_index = (row*64+col)*2;
                int code = info_vram.read(tile_index+1)+((info_vram.read(tile_index)&0x3f)<<8);
                SET_TILE_INFO(1,code,info_color);
                tile_info.mask_data = mask_ptr[code];
            }
        };
        
	static WriteHandlerPtr foreground_get_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
		int tile_index = (row*36+col)*2;
                int code = info_vram.read(tile_index+1)+((info_vram.read(tile_index)&0x3f)<<8);
                SET_TILE_INFO(1,code,info_color);
                tile_info.mask_data = mask_ptr[code];
            }
        };
	
	static void update_playfield( int layer )
	{
		tilemap tilemap = playfields[layer]._tilemap;
	
		/* for background , set scroll position */
		if( layer < 4 )
		{
			int scrollx = -playfields[layer].scroll_x + scrolloffsX[layer];
			int scrolly = -playfields[layer].scroll_y + scrolloffsY[layer];
			if (flipscreen != 0) {
				scrollx = -scrollx;
				scrolly = -scrolly;
			}
			/* set scroll */
			tilemap_set_scrollx(tilemap,0,scrollx);
			tilemap_set_scrolly(tilemap,0,scrolly);
		}
		info_vram  = playfields[layer].base;
		info_color = objects[layer].color;
		tilemap_update( tilemap );
	}
	
	/* tilemap draw handler */
	static gfx_objectHandlerPtr ns1_draw_tilemap = new gfx_objectHandlerPtr() {
            @Override
            public void handler(osd_bitmap bitmap, gfx_object object) {
                int layer = object.code;
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
/*TODO*///	#endif
		tilemap_draw( bitmap , playfields[layer]._tilemap , 0 );
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		else
		{
			if( layer < 4 )
				draw_background(bitmap,layer);
			else
				draw_foreground(bitmap,layer);
		}
/*TODO*///	#endif
            }
        };
	
	
	public static VhStartPtr namcos1_vh_start = new VhStartPtr() { public int handler() 
	{
		int i;
		gfx_object default_object;
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		/* tilemap used flag select */
		if(Machine.scrbitmap.depth==16)
			 /* tilemap system is not supported 16bit yet */
			namcos1_tilemap_used = 0;
		else
			/* selected by game option switch */
			namcos1_tilemap_used = namcos1_tilemap_need;
/*TODO*///	#endif
	
		/* set table for sprite color == 0x7f */
		for(i=0;i<=15;i++)
			gfx_drawmode_table[i] = DRAWMODE_SHADOW;
	
		/* set static memory points */
		namcos1_paletteram = new UBytePtr(memory_region(REGION_USER2));
		namcos1_controlram = new UBytePtr(memory_region(REGION_USER2), 0x8000);
	
		/* allocate videoram */
		namcos1_videoram = new UBytePtr(0x8000);
		if(namcos1_videoram==null)
		{
			return 1;
		}
		memset(namcos1_videoram,0,0x8000);
	
		/* initialize object manager */
		//memset(&default_object,0,sizeof(struct gfx_object));
                default_object = new gfx_object();
		default_object.transparency = TRANSPARENCY_PEN;
		default_object.transparet_color = 15;
		default_object.gfx = Machine.gfx[2];
		objectlist = gfxobj_create(MAX_PLAYFIELDS+MAX_SPRITES,8,default_object);
		if(objectlist == null)
		{
			namcos1_videoram = null;
			return 1;
		}
		objects = objectlist.objects;
	
		/* setup tilemap parameter to objects */
		for(i=0;i<MAX_PLAYFIELDS;i++)
		{
			/* set user draw handler */
			objects[i].special_handler = ns1_draw_tilemap;
			objects[i].gfx = null;
			objects[i].code = i;
			objects[i].visible = 0;
			objects[i].color = i;
		}
	
		/* initialize playfields */
		for (i = 0; i < MAX_PLAYFIELDS; i++)
		{
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			if (namcos1_tilemap_used != 0)
			{
/*TODO*///	#endif
			if ( i < 4 ) {
				playfields[i].base = new UBytePtr(namcos1_videoram, i<<13);
				playfields[i]._tilemap =
					tilemap_create( background_get_info,TILEMAP_BITMASK
								,8,8
								,64,i==3 ? 32 : 64);
			} else {
				playfields[i].base = new UBytePtr(namcos1_videoram, FG_OFFSET+0x10+( ( i - 4 ) * 0x800 ));
				playfields[i]._tilemap =
					tilemap_create( foreground_get_info,TILEMAP_BITMASK
								,8,8
								,36,28);
			}
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			}
			else
			{
				if ( i < 4 ) {
					playfields[i].base = new UBytePtr(namcos1_videoram, i<<13);
					playfields[i].width  = 64*8;
					playfields[i].height = ( i == 3 ) ? 32*8 : 64*8;
				} else {
					playfields[i].base = new UBytePtr(namcos1_videoram, FG_OFFSET+0x10+( ( i - 4 ) * 0x800 ));
					playfields[i].width  = 36*8;
					playfields[i].height = 28*8;
				}
			}
/*TODO*///	#endif
			playfields[i].scroll_x = 0;
			playfields[i].scroll_y = 0;
		}
		namcos1_set_flipscreen(0);
	
		/* initialize sprites and display controller */
		for(i=0;i<0x7ef;i++)
			namcos1_spriteram_w.handler(i,0);
		for(i=0;i<0xf;i++)
			namcos1_displaycontrol_w.handler(i,0);
		for(i=0;i<0xff;i++)
			namcos1_playfield_control_w.handler(i,0);
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
		{
/*TODO*///	#endif
		/* build tilemap mask data from gfx data of mask */
		/* because this driver use ORIENTATION_ROTATE_90 */
		/* mask data can't made by ROM image             */
		{
			GfxElement mask = Machine.gfx[0];
			int total  = mask.total_elements;
			int width  = mask.width;
			int height = mask.height;
			int line,x,c;
	
			mask_ptr = new UBytePtr[total];
/*TODO*///                        for (int _i=0 ; _i<total ; _i++)
/*TODO*///                            mask_ptr[_i]=new UBytePtr();
			if(mask_ptr == null)
			{
				namcos1_videoram=null;
				return 1;
			}
			mask_data = new UBytePtr(total * 8);
			if(mask_data == null)
			{
				namcos1_videoram=null;
				mask_ptr=null;
				return 1;
			}
	
			for(c=0;c<total;c++)
			{
				UBytePtr src_mask = new UBytePtr(mask_data, c*8);
				for(line=0;line<height;line++)
				{
					UBytePtr maskbm = new UBytePtr(get_gfx_pointer(mask,c,line));
					src_mask.write(line, 0);
					for (x=0;x<width;x++)
					{
						src_mask.write(line, src_mask.read(line)| maskbm.read(x)<<(7-x));
					}
				}
				mask_ptr[c] = new UBytePtr(src_mask);
				if(mask.pen_usage != null)
				{
                                        System.out.println("Check this man!");
					switch(mask.pen_usage[c])
					{
					case 0x01: mask_ptr[c].write( TILEMAP_BITMASK_TRANSPARENT ); break; /* blank */
					case 0x02: mask_ptr[c].write( TILEMAP_BITMASK_OPAQUE ); break; /* full */
					}
				}
			}
		}
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		}
		else /* namcos1_tilemap_used */
		{
	
		/* build char mask status table */
		{
			GfxElement mask = Machine.gfx[0];
			GfxElement pens = Machine.gfx[1];
			int total  = mask.total_elements;
			int width  = mask.width;
			int height = mask.height;
			int line,x,c;
	
			char_state = new UBytePtr( total );
			if(char_state == null)
			{
				namcos1_videoram=null;
				return 1;
			}
	
			for(c=0;c<total;c++)
			{
				int ordata = 0;
				int anddata = 0xff;
				for(line=0;line<height;line++)
				{
					UBytePtr maskbm = new UBytePtr(get_gfx_pointer(mask,c,line));
					for (x=0;x<width;x++)
					{
						ordata	|= maskbm.read(x);
						anddata &= maskbm.read(x);
					}
				}
				if(ordata==0)  char_state.write(c, CHAR_BLANK);
				else if (anddata != 0) char_state.write(c, CHAR_FULL);
				else
				{
					/* search non used pen */
					int[] penmap=new int[256];
					int trans_pen;
					memset(penmap,0,256);
					for(line=0;line<height;line++)
					{
						UBytePtr pensbm = new UBytePtr(get_gfx_pointer(pens,c,line));
						for (x=0;x<width;x++)
							penmap[pensbm.read(x)] = 1;
					}
					for(trans_pen=2;trans_pen<256;trans_pen++)
					{
						if(penmap[trans_pen]==0) break;
					}
					char_state.write(c, trans_pen); /* transparency color */
					/* fill transparency color */
					for(line=0;line<height;line++)
					{
						UBytePtr maskbm = new UBytePtr(get_gfx_pointer(mask,c,line));
						UBytePtr pensbm = new UBytePtr(get_gfx_pointer(pens,c,line));
						for (x=0;x<width;x++)
						{
							if(maskbm.read(x)==0) pensbm.write(x, trans_pen);
						}
					}
				}
			}
		}
	
		} /* namcos1_tilemap_used */
/*TODO*///	#endif
	
		for (i = 0;i < TILECOLORS;i++)
		{
			palette_shadow_table[Machine.pens[i+SPRITECOLORS]] = Machine.pens[i+SPRITECOLORS+TILECOLORS];
		}
	
		return 0;
	} };
	
	public static VhStopPtr namcos1_vh_stop = new VhStopPtr() { public void handler() 
	{
		namcos1_videoram=null;
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
		{
/*TODO*///	#endif
		mask_ptr=null;
		mask_data=null;
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		}
		else
			char_state=null;
/*TODO*///	#endif
	} };
	
	public static void namcos1_set_optimize( int optimize )
	{
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		namcos1_tilemap_need = optimize;
/*TODO*///	#endif
	}
	
	public static VhUpdatePtr namcos1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
		gfx_object object;
		char[] palette_map=new char[MAX_SPRITES+1];
		UBytePtr remapped=new UBytePtr();
	
		/* update all tilemaps */
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
		{
/*TODO*///	#endif
		for(i=0;i<MAX_PLAYFIELDS;i++)
			update_playfield(i);
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		}
/*TODO*///	#endif
		/* object list (sprite) update */
		gfxobj_update();
		/* palette resource marking */
		palette_init_used_colors();
/*TODO*///		memset(palette_map, 0, sizeof(palette_map));
                System.out.println("THIS needs to be implemented!!!!");
/*TODO*///		for(object=objectlist.first_object ; object!=0 ; object=object.next)
/*TODO*///		{
/*TODO*///			if (object.visible != 0)
/*TODO*///			{
/*TODO*///				int color = object.color;
/*TODO*///				if(object.gfx != null)
/*TODO*///				{	/* sprite object */
/*TODO*///					if (sprite_palette_state[color] != 0)
/*TODO*///					{
/*TODO*///						if (color != 0x7f) namcos1_palette_refresh(16*color, 16*color, 15);
/*TODO*///						sprite_palette_state[color] = 0;
/*TODO*///					}
/*TODO*///	
/*TODO*///					palette_map[color] |= Machine.gfx[2].pen_usage[object.code];
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{	/* playfield object */
/*TODO*///					if (tilemap_palette_state[color] != 0)
/*TODO*///					{
/*TODO*///						namcos1_palette_refresh(128*16+256*color, 128*16+256*playfields[color].color, 256);
/*TODO*////*TODO*///	#if NAMCOS1_DIRECT_DRAW
/*TODO*///						if(namcos1_tilemap_used==0)
/*TODO*///						{
/*TODO*///							/* mark used flag */
/*TODO*///							memset(&palette_used_colors[color*256+128*16],PALETTE_COLOR_VISIBLE,256);
/*TODO*///						}
/*TODO*////*TODO*///	#endif
/*TODO*///						tilemap_palette_state[color] = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
	
		for (i = 0; i < MAX_SPRITES; i++)
		{
			int usage = palette_map[i], j;
			if (usage != 0)
			{
				for (j = 0; j < 15; j++)
					if ((usage & (1 << j)) != 0)
						palette_used_colors.write(i * 16 + j, palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
			}
		}
		/* background color */
		palette_used_colors.write(BACKGROUNDCOLOR, palette_used_colors.read(BACKGROUNDCOLOR) | PALETTE_COLOR_VISIBLE);
	
		if ( ( remapped = palette_recalc() ) != null )
		{
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
			if (namcos1_tilemap_used != 0)
/*TODO*///	#endif
			for (i = 0;i < MAX_PLAYFIELDS;i++)
			{
				int j;
				UBytePtr remapped_layer = new UBytePtr(remapped, 128*16+256*i);
				for (j = 0;j < 256;j++)
				{
					if (remapped_layer.read(j) != 0)
					{
						tilemap_mark_all_pixels_dirty(playfields[i]._tilemap);
						break;
					}
				}
			}
		}
	
/*TODO*///	#if NAMCOS1_DIRECT_DRAW
		if (namcos1_tilemap_used != 0)
/*TODO*///	#endif
		tilemap_render(ALL_TILEMAPS);
		/* background color */
		fillbitmap(bitmap,Machine.pens[BACKGROUNDCOLOR],Machine.visible_area);
		/* draw objects (tilemaps and sprites) */
		gfxobj_draw(objectlist);
	} };
}
