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
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;

public class kaneko16
{
	
	/* Variables only used here: */
	
	public static tilemap bg_tilemap, fg_tilemap;
	public static osd_bitmap kaneko16_bg15_bitmap;
	public static int flipsprites;
	

	
	/* Variables that driver has access to: */
	
	   public static UBytePtr kaneko16_bgram;
              public static UBytePtr kaneko16_fgram=new UBytePtr();
	   public static UBytePtr kaneko16_layers1_regs=new UBytePtr();
           public static UBytePtr kaneko16_layers2_regs=new UBytePtr();
        public static UBytePtr kaneko16_screen_regs=new UBytePtr();
	public static UBytePtr kaneko16_bg15_select=new UBytePtr();
        public static UBytePtr kaneko16_bg15_reg=new UBytePtr();
	public static int kaneko16_spritetype;
	
	/* Variables defined in drivers: */
	
	
	/***************************************************************************
	
									Palette RAM
	
	***************************************************************************/
	
	public static WriteHandlerPtr kaneko16_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/*	byte 0    	byte 1		*/
		/*	xGGG GGRR   RRRB BBBB	*/
		/*	x432 1043 	2104 3210	*/
	
		int newword, r,g,b;
	
		COMBINE_WORD_MEM(paletteram,offset, data);
	
		newword = paletteram.READ_WORD(offset);
		r = (newword >>  5) & 0x1f;
		g = (newword >> 10) & 0x1f;
		b = (newword >>  0) & 0x1f;
	
		palette_change_color( offset/2,	 (r * 0xFF) / 0x1F,
										 (g * 0xFF) / 0x1F,
										 (b * 0xFF) / 0x1F	 );
	} };
	
	public static WriteHandlerPtr gtmr_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset < 0x10000)	kaneko16_paletteram_w.handler(offset, data);
		else					COMBINE_WORD_MEM(paletteram,offset, data);
	} };
	
	
	
	
	/***************************************************************************
	
								Video Registers
	
	***************************************************************************/
	
	/*	[gtmr]
	
	Initial self test:
	600000:4BC0 94C0 4C40 94C0-0404 0002 0000 0000
	680000:4BC0 94C0 4C40 94C0-1C1C 0002 0000 0000
	
	700000:0040 0000 0001 0180-0000 0000 0000 0000
	700010:0040 0000 0040 0000-0040 0000 2840 1E00
	
	Race start:
	600000:DC00 7D00 DC80 7D00-0404 0002 0000 0000
	680000:DC00 7D00 DC80 7D00-1C1C 0002 0000 0000
	
	700000:0040 0000 0001 0180-0000 0000 0000 0000
	700010:0040 0000 0040 0000-0040 0000 2840 1E00
	
	*/
	
	public static ReadHandlerPtr kaneko16_screen_regs_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return kaneko16_screen_regs.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr kaneko16_screen_regs_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int new_data;
	
		COMBINE_WORD_MEM(kaneko16_screen_regs,offset,data);
		new_data  = kaneko16_screen_regs.READ_WORD(offset);
	
		switch (offset)
		{
			case 0x00:	flipsprites = new_data & 3;	break;
		}
	
		if (errorlog != null) fprintf(errorlog, "CPU #0 PC %06X : Warning, screen reg %04X <- %04X\n",cpu_get_pc(),offset,data);
	} };
	
	
	
	/*	[gtmr]
	
		car select screen scroll values:
		Flipscreen off:
			$6x0000: $72c0 ; $fbc0 ; 7340 ; 0
			$72c0/$40 = $1cb = $200-$35	/	$7340/$40 = $1cd = $1cb+2
	
			$fbc0/$40 = -$11
	
		Flipscreen on:
			$6x0000: $5d00 ; $3780 ; $5c80 ; $3bc0
			$5d00/$40 = $174 = $200-$8c	/	$5c80/$40 = $172 = $174-2
	
			$3780/$40 = $de	/	$3bc0/$40 = $ef
	
	*/
	
	public static WriteHandlerPtr kaneko16_layers1_regs_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(kaneko16_layers1_regs,offset,data);
	} };
	
	public static WriteHandlerPtr kaneko16_layers2_regs_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(kaneko16_layers2_regs,offset,data);
	} };
	
	
	/* Select the high color background image (out of 32 in the ROMs) */
	public static ReadHandlerPtr kaneko16_bg15_select_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return kaneko16_bg15_select.READ_WORD(0);
	} };
	public static WriteHandlerPtr kaneko16_bg15_select_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(kaneko16_bg15_select,0,data);
	} };
	
	/* ? */
	public static ReadHandlerPtr kaneko16_bg15_reg_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return kaneko16_bg15_reg.READ_WORD(0);
	} };
	public static WriteHandlerPtr kaneko16_bg15_reg_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(kaneko16_bg15_reg,0,data);
	} };
	
	
	
	/***************************************************************************
	
							Callbacks for the TileMap code
	
								  [ Tiles Format ]
	
	Offset:
	
	0000.w			fedc ba-- ---- ----		unused?
					---- --98 ---- ----		Priority
					---- ---- 7654 32--		Color
					---- ---- ---- --1-		Flip X
					---- ---- ---- ---0		Flip Y
	
	0002.w									Code
	
	***************************************************************************/
	
	
	/* Background */
	
	public static final int BG_GFX =(0);
	public static final int BG_NX  =(0x20);
	public static final int BG_NY  =(0x20);
	
	public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = col + row * BG_NX;
		int code_hi = kaneko16_bgram.READ_WORD(tile_index*4 + 0);
		int code_lo = kaneko16_bgram.READ_WORD(tile_index*4 + 2);
		SET_TILE_INFO(BG_GFX, code_lo,(code_hi >> 2) & 0x3f);
		tile_info.flags 	=(char)	TILE_FLIPXY( code_hi & 3 );
	} };
	
	static WriteHandlerPtr kaneko16_bgram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	int old_data, new_data;
	
		old_data  = kaneko16_bgram.READ_WORD(offset);
		COMBINE_WORD_MEM(kaneko16_bgram,offset,data);
		new_data  = kaneko16_bgram.READ_WORD(offset);
	
		if (old_data != new_data)
			tilemap_mark_tile_dirty(bg_tilemap,(offset/4) % BG_NX,(offset/4) / BG_NX);
	} };
	
	
	
	
	
	/* Foreground */
	
	public static final int FG_GFX =(0);
	public static final int FG_NX  =(0x20);
	public static final int FG_NY  =(0x20);
	
	public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() { public void handler(int col, int row)
	{
		int tile_index = col + row * FG_NX;
		int code_hi = kaneko16_fgram.READ_WORD(tile_index*4 + 0);
		int code_lo = kaneko16_fgram.READ_WORD(tile_index*4 + 2);
		SET_TILE_INFO(FG_GFX, code_lo,(code_hi >> 2) & 0x3f);
		tile_info.flags 	=	(char)TILE_FLIPXY( code_hi & 3 );
		tile_info.priority	=	(char)((code_hi >> 8) & 3);
	} };
	
	public static WriteHandlerPtr kaneko16_fgram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	int old_data, new_data;
	
		old_data  = kaneko16_fgram.READ_WORD(offset);
		COMBINE_WORD_MEM(kaneko16_fgram,offset,data);
		new_data  = kaneko16_fgram.READ_WORD(offset);
	
		if (old_data != new_data)
			tilemap_mark_tile_dirty(fg_tilemap,(offset/4) % FG_NX,(offset/4) / FG_NX);
	} };
	
	
	
	public static WriteHandlerPtr kaneko16_layers1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset < 0x1000)	kaneko16_fgram_w.handler(offset,data);
		else
		{
			if (offset < 0x2000)	kaneko16_bgram_w.handler((offset-0x1000),data);
			else
			{
				COMBINE_WORD_MEM(kaneko16_fgram,offset,data);
			}
		}
	} };
	
	
	
	
	
	
	
	
	public static VhStartPtr kaneko16_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_bg_tile_info,
									TILEMAP_TRANSPARENT, /* to handle the optional hi-color bg */
									16,16,
									BG_NX,BG_NY );
	
		fg_tilemap = tilemap_create(get_fg_tile_info,
									TILEMAP_TRANSPARENT,
									16,16,
									FG_NX,FG_NY );
	
		if (bg_tilemap!=null && fg_tilemap!=null)
		{
	/*
	gtmr background:
			flipscreen off: write (x)-$33
			[x=fetch point (e.g. scroll *left* with incresing x)]
	
			flipscreen on:  write (x+320)+$33
			[x=fetch point (e.g. scroll *right* with incresing x)]
	
			W = 320+$33+$33 = $1a6 = 422
	
	berlwall background:
	6940 off	1a5 << 6
	5680 on		15a << 6
	*/
			int xdim = Machine.drv.screen_width;
			int ydim = Machine.drv.screen_height;
			int dx, dy;
	
	//		dx   = (422 - xdim) / 2;
			switch (xdim)
			{
				case 320:	dx = 0x33;	dy = 0;		break;
				case 256:	dx = 0x5b;	dy = -8;	break;
	
				default:	dx = dy = 0;
			}
	
			tilemap_set_scrolldx( bg_tilemap, -dx,		xdim + dx -1        );
			tilemap_set_scrolldx( fg_tilemap, -(dx+2),	xdim + (dx + 2) - 1 );
	
			tilemap_set_scrolldy( bg_tilemap, -dy,		ydim + dy -1 );
			tilemap_set_scrolldy( fg_tilemap, -dy,		ydim + dy -1);
	
			bg_tilemap.transparent_pen = 0;
			fg_tilemap.transparent_pen = 0;
			return 0;
		}
		else
			return 1;
	} };
	
	
	
	
	/* Berlwall has an additional hi-color background */
	public static VhConvertColorPromPtr berlwall_init_palette = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
                int palette_ptr=0;
		palette_ptr +=(2048 * 3);	/* first 2048 colors are dynamic */
	
		/* initialize 555 RGB lookup */
		for (i = 0; i < 32768; i++)
		{
			int r,g,b;
	
			r = (i >>  5) & 0x1f;
			g = (i >> 10) & 0x1f;
			b = (i >>  0) & 0x1f;
	
                        palette[palette_ptr++]=(char)((r << 3) | (r >> 2));
			palette[palette_ptr++]=(char)((g << 3) | (g >> 2));
			palette[palette_ptr++]=(char)((b << 3) | (b >> 2));
                        
		}
	}};
	
	public static VhStartPtr berlwall_vh_start = new VhStartPtr() { public int handler() 
	{
		int sx, x,y;
		UBytePtr RAM	=	memory_region(REGION_GFX3);
	
		/* Render the hi-color static backgrounds held in the ROMs */
	
		if ((kaneko16_bg15_bitmap = osd_new_bitmap(256 * 32, 256 * 1, 16)) == null)
			return 1;
	
	/*
		8aba is used as background color
		8aba/2 = 455d = 10001 01010 11101 = $11 $0a $1d
	*/
	
		for (sx = 0 ; sx < 32 ; sx++)	// horizontal screens
		 for (x = 0 ; x < 256 ; x++)	// horizontal pixels
		  for (y = 0 ; y < 256 ; y++)	// vertical pixels
		  {
				int addr  = sx * (256 * 256) + x + y * 256;
	
				int color = ( RAM.read(addr * 2 + 0) * 256 + RAM.read(addr * 2 + 1) ) >> 1;
	//				color ^= (0x8aba/2);
	
				plot_pixel.handler(kaneko16_bg15_bitmap,
							sx * 256 + x, y,
							Machine.pens[2048 + color] );
		  }
	
		return kaneko16_vh_start.handler();
	} };
	
	public static VhStopPtr berlwall_vh_stop = new VhStopPtr() { public void handler() 
	{
		if (kaneko16_bg15_bitmap != null)
			osd_free_bitmap(kaneko16_bg15_bitmap);
	
		kaneko16_bg15_bitmap = null;	// multisession safety
	} };
	
	
	/***************************************************************************
	
									Sprites Drawing
	
	Offset:			Format:						Value:
	
	0000.w			Attribute (type 0, older games: shogwarr, berlwall)
	
						f--- ---- ---- ----		Multisprite: Use Latched Code + 1
						-e-- ---- ---- ----		Multisprite: Use Latched Color (And Flip?)
						--d- ---- ---- ----		Multisprite: Use Latched X,Y As Offsets
						---c ba-- ---- ----
						---- --98 ---- ----		Priority?
						---- ---- 7654 32--		Color
						---- ---- ---- --1-		X Flip
						---- ---- ---- ---0		Y Flip
	
					Attribute (type 1: gtmr, gtmr2)
	
						f--- ---- ---- ----		Multisprite: Use Latched Code + 1
						-e-- ---- ---- ----		Multisprite: Use Latched Color (And Flip?)
						--d- ---- ---- ----		Multisprite: Use Latched X,Y As Offsets
						---c ba-- ---- ----		unused?
						---- --9- ---- ----		X Flip
						---- ---8 ---- ----		Y Flip
						---- ---- 76-- ----		Priority
						---- ---- --54 3210		Color
	
	0002.w										Code
	0004.w										X Position << 6
	0006.w										Y Position << 6
	
	Note:
		type 2 sprites (berlwall) are like type 0 but the data is held
		in the last 8 bytes of every 16.
	
	
	***************************************************************************/
	
	
	/* Map the attribute word to that of the type 1 sprite hardware */
	
	
	/* Mark the pens of visible sprites */
	
	public static void kaneko16_mark_sprites_colors()
	{
		int offs,inc;
	
		int xmin = Machine.drv.visible_area.min_x - (16 - 1);
		int xmax = Machine.drv.visible_area.max_x;
		int ymin = Machine.drv.visible_area.min_y - (16 - 1);
		int ymax = Machine.drv.visible_area.max_y;
	
		int nmax				=	Machine.gfx[0].total_elements;
		int color_granularity	=	Machine.gfx[0].color_granularity;
		int color_codes_start	=	Machine.drv.gfxdecodeinfo[0].color_codes_start;
		int total_color_codes	=	Machine.drv.gfxdecodeinfo[0].total_color_codes;
	
		int sx = 0;
		int sy = 0;
		int scode = 0;
		int scolor = 0;
	
		switch (kaneko16_spritetype)
		{
			case 2:		offs = 8; inc = 16;	break;
			default:	offs = 0; inc = 8;	break;
		}
	
		for ( ;  offs < spriteram_size[0] ; offs += inc)
		{
			int	attr	=	spriteram.READ_WORD(offs + 0);
			int	code	=	spriteram.READ_WORD(offs + 2) % nmax;
			int	x		=	spriteram.READ_WORD(offs + 4);
			int	y		=	spriteram.READ_WORD(offs + 6);
	
			/* Map the attribute word to that of the type 1 sprite hardware */
			if (kaneko16_spritetype != 1)	/* shogwarr, berlwall */ 
		{ 
			attr =	((attr & 0xfc00)     ) | 
					((attr & 0x03fc) >> 2) | 
					((attr & 0x0003) << 8) ; 
		}
	
			if ((x & 0x8000) != 0)	x -= 0x10000;
			if ((y & 0x8000) != 0)	y -= 0x10000;
	
			x /= 0x40;		y /= 0x40;
	
			if ((attr & 0x8000) != 0)
                        {
                            scode++;
                        }
			else					
                        {
                            scode = code;
                        }
	
			if ((attr & 0x4000)==0)	scolor = attr % total_color_codes;
	
			if ((attr & 0x2000) != 0)		{ sx += x;	sy += y; }
			else					{ sx  = x;	sy  = y; }
	
			/* Visibility check. No need to account for sprites flipping */
			if ((sx < xmin) || (sx > xmax))	continue;
			if ((sy < ymin) || (sy > ymax))	continue;
	
			memset(palette_used_colors,color_granularity * scolor + color_codes_start + 1,PALETTE_COLOR_USED,color_granularity - 1);
                        /*for(int i=0; i< color_granularity - 1; i++)
                        {
                            palette_used_colors.write(color_granularity * scolor + color_codes_start + 1+i,PALETTE_COLOR_USED);
                        }*/
		}
	
	}
	
	
	
	/* Draw the sprites */
	
	public static void kaneko16_draw_sprites(osd_bitmap bitmap, int priority)
	{
		int offs,inc;
	
		int max_x	=	Machine.drv.screen_width  - 16;
		int max_y	=	Machine.drv.screen_height - 16;
	
		int sx = 0;
		int sy = 0;
		int scode = 0;
		int sattr = 0;
		int sflipx = 0;
		int sflipy = 0;
	
		priority = ( priority & 3 ) << 6;
	
		switch (kaneko16_spritetype)
		{
			case 2:		offs = 8; inc = 16;	break;
			default:	offs = 0; inc = 8;	break;
		}
	
		for ( ;  offs < spriteram_size[0] ; offs += inc)
		{
			int	attr	=	spriteram.READ_WORD(offs + 0);
			int	code	=	spriteram.READ_WORD(offs + 2);
			int	x		=	spriteram.READ_WORD(offs + 4);
			int	y		=	spriteram.READ_WORD(offs + 6);
	
			/* Map the attribute word to that of the type 1 sprite hardware */
			if (kaneko16_spritetype != 1)	/* shogwarr, berlwall */ 
		{ 
			attr =	((attr & 0xfc00)     ) | 
					((attr & 0x03fc) >> 2) | 
					((attr & 0x0003) << 8) ; 
		}
	
			if ((x & 0x8000) != 0)	x -= 0x10000;
			if ((y & 0x8000) != 0)	y -= 0x10000;
	
			x /= 0x40;		y /= 0x40;
	
			if ((attr & 0x8000) != 0)		scode++;
			else					scode = code;
	
			if ((attr & 0x4000)==0)
			{
				sattr  = attr;
				sflipx = attr & 0x200;	sflipy = attr & 0x100;
			}
	
			if ((attr & 0x2000) != 0)		{ sx += x;	sy += y; }
			else					{ sx  = x;	sy  = y; }
	
			if ((sattr & 0xc0) != priority)	continue;
	
	
			if ((flipsprites & 2) != 0) { sx = max_x - sx;		sflipx = NOT(sflipx); }
			if ((flipsprites & 1) != 0) { sy = max_y - sy;		sflipy = NOT(sflipy); }
	
			drawgfx(bitmap,Machine.gfx[1],
					scode,
					sattr,
					sflipx, sflipy,
					sx,sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
	
			/* let's get back to normal to support multi sprites */
			if ((flipsprites & 2) != 0) { sx = max_x - sx;		sflipx = NOT(sflipx); }
			if ((flipsprites & 1) != 0) { sy = max_y - sy;		sflipy = NOT(sflipy); }
		}
	
	}
	
	
	
	
	
	
	/***************************************************************************
	
									Screen Drawing
	
	***************************************************************************/
	
	public static VhUpdatePtr kaneko16_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int flag;
		int layers_ctrl = -1;
		int layers_flip = kaneko16_layers1_regs.READ_WORD(0x08);
	
		tilemap_set_flip(fg_tilemap,((layers_flip & 0x0001)!=0 ? TILEMAP_FLIPY : 0) |
									((layers_flip & 0x0002)!=0 ? TILEMAP_FLIPX : 0) );
	
		tilemap_set_flip(bg_tilemap,((layers_flip & 0x0100)!=0 ? TILEMAP_FLIPY : 0) |
									((layers_flip & 0x0200)!=0 ? TILEMAP_FLIPX : 0) );
	
		tilemap_set_scrollx(fg_tilemap, 0, kaneko16_layers1_regs.READ_WORD(0x00) >> 6 );
		tilemap_set_scrolly(fg_tilemap, 0, kaneko16_layers1_regs.READ_WORD(0x02) >> 6 );
	
		tilemap_set_scrollx(bg_tilemap, 0, kaneko16_layers1_regs.READ_WORD(0x04) >> 6 );
		tilemap_set_scrolly(bg_tilemap, 0, kaneko16_layers1_regs.READ_WORD(0x06) >> 6 );
	
		
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
	
		kaneko16_mark_sprites_colors();
	
		if (palette_recalc()!=null)	tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		flag = TILEMAP_IGNORE_TRANSPARENCY;
		if (kaneko16_bg15_bitmap != null)
		{
	/*
		firstscreen	?				(hw: b8,00/06/7/8/9(c8). 202872 = 0880)
		press start	?				(hw: 80-d0,0a. 202872 = 0880)
		teaching	?				(hw: e0,1f. 202872 = 0880 )
		hiscores	!rom5,scr1($9)	(hw: b0,1f. 202872 = )
		lev1-1		rom6,scr2($12)	(hw: cc,0e. 202872 = 0880)
		lev2-1		?				(hw: a7,01. 202872 = 0880)
		lev2-2		rom6,scr1($11)	(hw: b0,0f. 202872 = 0880)
		lev2-4		rom6,scr0($10)	(hw: b2,10. 202872 = 0880)
		lev2-6?		rom5,scr7($f)	(hw: c0,11. 202872 = 0880)
		lev4-2		rom5,scr6($e)	(hw: d3,12. 202872 = 0880)
		redcross	?				(hw: d0,0a. 202872 = )
	*/
			int select	=	kaneko16_bg15_select.READ_WORD(0);
	//		int reg		=	READ_WORD(&kaneko16_bg15_reg[0]);
			int flip	=	select & 0x20;
			int sx, sy;
	
			if (flip != 0)	select ^= 0x1f;
	
			sx		=	(select & 0x1f) * 256;
			sy		=	0;
	
			copybitmap(
				bitmap, kaneko16_bg15_bitmap,
				flip, flip,
				-sx, -sy,
				Machine.drv.visible_area, TRANSPARENCY_NONE,0 );
	
			flag = 0;
		}
	
		if ((layers_ctrl & 0x01) != 0)	tilemap_draw(bitmap, bg_tilemap, flag);
		else					osd_clearbitmap(Machine.scrbitmap);
	
		if ((layers_ctrl & 0x02) != 0)	tilemap_draw(bitmap, fg_tilemap, 0);
		if ((layers_ctrl & 0x08) != 0)	kaneko16_draw_sprites(bitmap,0);
	
		if ((layers_ctrl & 0x04) != 0)	tilemap_draw(bitmap, fg_tilemap, 1);
		if ((layers_ctrl & 0x08) != 0)	kaneko16_draw_sprites(bitmap,1);
	
		if ((layers_ctrl & 0x10) != 0)	tilemap_draw(bitmap, fg_tilemap, 2);
		if ((layers_ctrl & 0x08) != 0)	kaneko16_draw_sprites(bitmap,2);
	
		if ((layers_ctrl & 0x20) != 0)	tilemap_draw(bitmap, fg_tilemap, 3);
		if ((layers_ctrl & 0x08) != 0)	kaneko16_draw_sprites(bitmap,3);
	} };
}
