/*
 * ported to v0.37b16
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b16.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static common.libc.cstring.*;

public class namcos1
{
	
	public static UBytePtr get_gfx_pointer(GfxElement gfxelement, int c, int line){ return (new UBytePtr(gfxelement.gfxdata, (c*gfxelement.height+line) * gfxelement.line_modulo)); }
	
	public static final int SPRITECOLORS = 204;
	public static final int TILECOLORS = 1536;
	public static int BACKGROUNDCOLOR = (SPRITECOLORS+2*TILECOLORS);
	
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
	public static UBytePtr namcos1_videoram;
	/*
	  paletteram map (s1ram  0x0000-0x7fff)
	  0000-17ff : palette page0 : sprite
	  2000-37ff : palette page1 : playfield
	  4000-57ff : palette page2 : playfield (shadow)
	  6000-7fff : work ram ?
	*/
	public static UBytePtr namcos1_paletteram;
	/*
	  controlram map (s1ram 0x8000-0x9fff)
	  0000-07ff : work ram
	  0800-0fef : sprite ram	: 0x10 * 127
	  0ff0-0fff : display control register
	  1000-1fff : playfield control register
	*/
	public static UBytePtr namcos1_controlram;
	
	public static int FG_OFFSET = 0x7000;
	
	public static final int MAX_PLAYFIELDS = 6;
	public static int MAX_SPRITES    = 127;
	
	static tilemap[] tilemap = new tilemap[MAX_PLAYFIELDS];
	
	public static class playfield {
		int 	color;
	};
	
	public static playfield[] playfields = new playfield[MAX_PLAYFIELDS];
	
	/* playfields maskdata for tilemap */
	static UBytePtr mask_ptr;
	static UBytePtr mask_data;
	
	/* palette dirty information */
	static int[] sprite_palette_state = new int[MAX_SPRITES+1];
	static int[] tilemap_palette_state = new int[MAX_PLAYFIELDS];
	
	/* per game scroll adjustment */
	static int[] scrolloffsX=new int[4];
	static int[] scrolloffsY=new int[4];
	
	static int sprite_fixed_sx;
	static int sprite_fixed_sy;
	static int flipscreen;
	
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
		tilemap_set_flip(ALL_TILEMAPS,flipscreen!=0 ? TILEMAP_FLIPX|TILEMAP_FLIPY : 0);
	}
	
	
	static int[] namcos1_playfield_control=new int[0x100];
	
	public static WriteHandlerPtr namcos1_playfield_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		namcos1_playfield_control[offset] = data;
	
		/* 0-15 : scrolling */
		if ( offset < 16 )
		{
		}
		/* 16-21 : priority */
		else if ( offset < 22 )
		{
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
			if(offset < FG_OFFSET)
			{	/* background 0-3 */
				int layer = offset/0x2000;
				int num = (offset &= 0x1fff)/2;
				tilemap_mark_tile_dirty(tilemap[layer],num,/**/0);
			}
			else
			{	/* foreground 4-5 */
				int layer = (offset&0x800)!=0 ? 5 : 4;
				int num = ((offset&0x7ff)-0x10)/2;
				if (num >= 0 && num < 0x3f0)
					tilemap_mark_tile_dirty(tilemap[layer],num,/**/0);
			}
		}
	} };
	
	public static ReadHandlerPtr namcos1_paletteram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return namcos1_paletteram.read(offset);
	} };
	
	public static WriteHandlerPtr namcos1_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
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
	} };
	
	static void namcos1_palette_refresh(int start,int offset,int num)
	{
		int color;
	
		offset = (offset/0x800)*0x2000 + (offset&0x7ff);
	
		for (color = start; color < start + num; color++)
		{
			int r,g,b;
			r = namcos1_paletteram.read(offset);
			g = namcos1_paletteram.read(offset + 0x0800);
			b = namcos1_paletteram.read(offset + 0x1000);
			palette_change_color(color,r,g,b);
	
			if (offset >= 0x2000)
			{
				r = namcos1_paletteram.read(offset + 0x2000);
				g = namcos1_paletteram.read(offset + 0x2800);
				b = namcos1_paletteram.read(offset + 0x3000);
				palette_change_color(color+TILECOLORS,r,g,b);
			}
			offset++;
		}
	}
	
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
		namcos1_controlram.write(offset, (char) data);
	
		/* 0000-07ff work ram */
	
		/* 0800-0fef sprite ram */
	
		/* 0ff0-0fff display control ram */
		if(offset >= 0xff0 && offset <= 0x0fff)
		{
			namcos1_displaycontrol_w.handler(offset&0x0f, data);
			return;
		}
		/* 1000-1fff control ram */
		else if (offset >= 0x1000)
			namcos1_playfield_control_w.handler(offset&0xff, data);
	} };
	
	/* tilemap callback */
	static UBytePtr info_vram;
	static int info_color;
	
	static WriteHandlerPtr background_get_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
                int tile_index = (row*64+col)*2;
                int code = info_vram.read(tile_index+1)+((info_vram.read(tile_index)&0x3f)<<8);
                SET_TILE_INFO(1,code,info_color);
                tile_info.mask_data = new UBytePtr(mask_ptr, code);                		
            }
        };
	
	static WriteHandlerPtr foreground_get_info = new WriteHandlerPtr() {
            @Override
            public void handler(int col, int row) {
                int tile_index = (row*36+col)*2;
                int code = info_vram.read(tile_index+1)+((info_vram.read(tile_index)&0x3f)<<8);
                SET_TILE_INFO(1,code,info_color);
                tile_info.mask_data = new UBytePtr(mask_ptr, code);
            }
        };
	
	static void update_playfield( int layer )
	{
		/* for background , set scroll position */
		if( layer < 4 )
		{
			int scrollx = -(namcos1_playfield_control[layer*4+1] + 256*namcos1_playfield_control[layer*4+0]) + scrolloffsX[layer];
			int scrolly = -(namcos1_playfield_control[layer*4+3] + 256*namcos1_playfield_control[layer*4+2]) + scrolloffsY[layer];
	
			if (flipscreen != 0) {
				scrollx = -scrollx;
				scrolly = -scrolly;
			}
			/* set scroll */
			tilemap_set_scrollx(tilemap[layer],0,scrollx);
			tilemap_set_scrolly(tilemap[layer],0,scrolly);
	
			info_vram = new UBytePtr(namcos1_videoram, layer<<13);
		}
		else
			info_vram = new UBytePtr(namcos1_videoram, FG_OFFSET+0x10+( ( layer - 4 ) * 0x800 ));
	
		info_color = layer;
		tilemap_update(tilemap[layer]);
	}
	
	
	public static VhStartHandlerPtr namcos1_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		int i;
	
                for (int _i=0 ; _i<MAX_PLAYFIELDS ; _i++)
                    playfields[_i] = new playfield();
	
		/* set table for sprite color == 0x7f */
		for(i=0;i<=15;i++)
			gfx_drawmode_table[i] = DRAWMODE_SHADOW;
	
		/* set static memory points */
		namcos1_paletteram = memory_region(REGION_USER2);
		namcos1_controlram = new UBytePtr(memory_region(REGION_USER2), 0x8000);
	
		/* allocate videoram */
		namcos1_videoram = new UBytePtr(0x8000);
	
		/* initialize playfields */
		//tilemap[0] = tilemap_create(background_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,64/*,64*/);
                tilemap[0] = tilemap_create( background_get_info,TILEMAP_BITMASK,8,8,64,64);
		//tilemap[1] = tilemap_create(background_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,64,64);
                tilemap[1] = tilemap_create( background_get_info,TILEMAP_BITMASK,8,8,64,64);
		//tilemap[2] = tilemap_create(background_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,64,64);
                tilemap[2] = tilemap_create( background_get_info,TILEMAP_BITMASK,8,8,64,64);
		//tilemap[3] = tilemap_create(background_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,64,32);
                tilemap[3] = tilemap_create( background_get_info,TILEMAP_BITMASK,8,8,64,32);
		//tilemap[4] = tilemap_create(foreground_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,36,28);
                tilemap[4] = tilemap_create( foreground_get_info,TILEMAP_BITMASK,8,8,36,28);
		//tilemap[5] = tilemap_create(foreground_get_info,tilemap_scan_rows,TILEMAP_BITMASK,8,8,36,28);
                tilemap[5] = tilemap_create( foreground_get_info,TILEMAP_BITMASK,8,8,36,28);
	
		if (tilemap[0]==null || tilemap[1]==null || tilemap[2]==null || tilemap[3]==null || tilemap[4]==null || tilemap[5]==null
				|| namcos1_videoram==null)
			return 1;
	
		memset(namcos1_videoram,0,0x8000);
	
		namcos1_set_flipscreen(0);
	
		/* initialize sprites and display controller */
		for(i=0;i<0xf;i++)
			namcos1_displaycontrol_w.handler(i,0);
		for(i=0;i<0xff;i++)
			namcos1_playfield_control_w.handler(i,0);
	
		/* build tilemap mask data from gfx data of mask */
		/* because this driver use ORIENTATION_ROTATE_90 */
		/* mask data can't made by ROM image             */
		{
			GfxElement mask = Machine.gfx[0];
			int total  = mask.total_elements;
			int width  = mask.width;
			int height = mask.height;
			int line,x,c;
	
			mask_ptr = new UBytePtr(total);
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
					UBytePtr maskbm = get_gfx_pointer(mask,c,line);
					src_mask.write(line, 0);
					for (x=0;x<width;x++)
					{
						src_mask.write(line, src_mask.read(line) | maskbm.read(x)<<(7-x));
					}
				}
				mask_ptr.write(c, src_mask.read());
				if(mask.pen_usage != null)
				{
					switch(mask.pen_usage[c])
					{
					case 0x01: mask_ptr.write(c, TILEMAP_BITMASK_TRANSPARENT); break; /* blank */
					case 0x02: mask_ptr.write(c, TILEMAP_BITMASK_OPAQUE); break; /* full */
					}
				}
			}
		}
	
		for (i = 0;i < TILECOLORS;i++)
		{
			palette_shadow_table[Machine.pens[i+SPRITECOLORS]] = Machine.pens[i+SPRITECOLORS+TILECOLORS];
		}
	
		return 0;
	} };
	
	public static VhStopHandlerPtr namcos1_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		namcos1_videoram=null;
	
		mask_ptr=null;
		mask_data=null;
	} };
	
	
	
	static void draw_sprites(osd_bitmap bitmap,int priority)
	{
		int offs;
		UBytePtr namcos1_spriteram = new UBytePtr(namcos1_controlram, 0x0800);
	
	
		/* the last 0x10 bytes are control registers, not a sprite */
		for (offs = 0;offs < 0x800-0x10;offs += 0x10)
		{
			int sprite_sizemap[] = {16,8,32,4};
			int sx,sy,code,color,flipx,flipy;
			int width,height,left,top;
			rectangle rect=new rectangle();
	
	
			if (((namcos1_spriteram.read(offs + 8)>>5)&7) != priority) continue;
	
			width =  sprite_sizemap[(namcos1_spriteram.read(offs + 4)>>6)&3];
			height = sprite_sizemap[(namcos1_spriteram.read(offs + 8)>>1)&3];
			flipx = ((namcos1_spriteram.read(offs + 4)>>5)&1) ^ flipscreen;
			flipy = (namcos1_spriteram.read(offs + 8)&1) ^ flipscreen;
			left = (namcos1_spriteram.read(offs + 4)&0x18) & (~(width-1));
			top =  (namcos1_spriteram.read(offs + 8)&0x18) & (~(height-1));
			code = (namcos1_spriteram.read(offs + 4)&7)*256 + namcos1_spriteram.read(offs + 5);
			color = namcos1_spriteram.read(offs + 6)>>1;
	
/*TODO*///	#if 1
/*TODO*///			if (color == 0x7f && (Machine.gamedrv.flags & GAME_REQUIRES_16BIT)==0)
/*TODO*///				usrintf_showmessage("This driver requires GAME_REQUIRES_16BIT flag");
/*TODO*///	#endif
	
			/* sx */
			sx = (namcos1_spriteram.read(offs + 6)&1)*256 + namcos1_spriteram.read(offs + 7);
			sx += sprite_fixed_sx;
	
			if (flipscreen != 0) sx = 210 - sx - width;
	
			if( sx > 480  ) sx -= 512;
			if( sx < -32  ) sx += 512;
			if( sx < -224 ) sx += 512;
	
			/* sy */
			sy = sprite_fixed_sy - namcos1_spriteram.read(offs + 9);
	
			if (flipscreen != 0) sy = 222 - sy;
			else sy = sy - height;
	
			if( sy > 224 ) sy -= 256;
			if( sy < -32 ) sy += 256;
	
			rect.min_x=sx;
			rect.max_x=sx+(width-1);
			rect.min_y=sy;
			rect.max_y=sy+(height-1);
	
			if (flipx != 0) sx -= 32-width-left;
			else sx -= left;
			if (flipy != 0) sy -= 32-height-top;
			else sy -= top;
	
			drawgfx(bitmap,Machine.gfx[2],
					code,
					color,
					flipx,flipy,
					sx,sy,
					rect,color == 0x7f ? TRANSPARENCY_PEN_TABLE : TRANSPARENCY_PEN,0x0f);
		}
	}
	
	static void mark_sprites_colors()
	{
		int offs,i;
		UBytePtr namcos1_spriteram = new UBytePtr(namcos1_controlram, 0x0800);
	
		int[] palette_map=new int[128];
	
/*TODO*///		memset (palette_map, 0, sizeof (palette_map));
	
		/* the last 0x10 bytes are control registers, not a sprite */
		for (offs = 0;offs < 0x800-0x10;offs += 0x10)
		{
			int color;
	
			color = namcos1_spriteram.read(offs + 6)>>1;
			palette_map[color] |= 0xffff;
		}
	
		/* now build the final table */
		for (i = 0; i < 128; i++)
		{
			int usage = palette_map[i], j;
			if (usage != 0)
			{
				for (j = 0; j < 15; j++)
					if ((usage & (1 << j)) != 0)
						palette_used_colors.write(i * 16 + j, palette_used_colors.read(i * 16 +j) | PALETTE_COLOR_VISIBLE);
			}
		}
	}
	
	public static VhUpdateHandlerPtr namcos1_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i,priority;
	
	
		/* update all tilemaps */
		for(i=0;i<MAX_PLAYFIELDS;i++)
			update_playfield(i);
	
		for (i = 0;i < 128;i++)
		{	/* sprite object */
			if (sprite_palette_state[i] != 0)
			{
				namcos1_palette_refresh(16*i, 16*i, 15);
				sprite_palette_state[i] = 0;
			}
		}
	
		for (i = 0;i < MAX_PLAYFIELDS;i++)
		{	/* playfield object */
			if (tilemap_palette_state[i] != 0)
			{
				namcos1_palette_refresh(128*16+256*i, 128*16+256*playfields[i].color, 256);
				tilemap_palette_state[i] = 0;
			}
		}
	
	
		/* palette resource marking */
		palette_init_used_colors();
	
		mark_sprites_colors();
	
		/* background color */
		palette_used_colors.write(BACKGROUNDCOLOR, palette_used_colors.read(BACKGROUNDCOLOR) | PALETTE_COLOR_VISIBLE);
	
		palette_recalc();
	
		/* background color */
		fillbitmap(bitmap,Machine.pens[BACKGROUNDCOLOR],Machine.visible_area);
	
		for (priority = 0;priority <= 7;priority++)
		{
			/* bit 0-2 priority */
			/* bit 3   disable	*/
			if (namcos1_playfield_control[16] == priority) tilemap_draw(bitmap,tilemap[0],0/*,0*/);
			if (namcos1_playfield_control[17] == priority) tilemap_draw(bitmap,tilemap[1],0/*,0*/);
			if (namcos1_playfield_control[18] == priority) tilemap_draw(bitmap,tilemap[2],0/*,0*/);
			if (namcos1_playfield_control[19] == priority) tilemap_draw(bitmap,tilemap[3],0/*,0*/);
			if (namcos1_playfield_control[20] == priority) tilemap_draw(bitmap,tilemap[4],0/*,0*/);
			if (namcos1_playfield_control[21] == priority) tilemap_draw(bitmap,tilemap[5],0/*,0*/);
	
			draw_sprites(bitmap,priority);
		}
	} };
}
