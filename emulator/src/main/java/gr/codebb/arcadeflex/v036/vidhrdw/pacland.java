/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;



public class pacland
{
	
	
	static osd_bitmap tmpbitmap2;
        static osd_bitmap tmpbitmap3;
	static int scroll0,scroll1;
	static int palette_bank;
	static UBytePtr pacland_color_prom;
	
	static rectangle spritevisiblearea = new rectangle
	(
		3*8, 39*8-1,
		5*8, 29*8-1
        );
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Pacland has one 1024x8 and one 1024x4 palette PROM; and three 1024x8 lookup
	  table PROMs (sprites, bg tiles, fg tiles).
	  The palette has 1024 colors, but it is bank switched (4 banks) and only 256
	  colors are visible at a time. So, instead of creating a static palette, we
	  modify it when the bank switching takes place.
	  The color PROMs are connected to the RGB output this way:
	
	  bit 7 -- 220 ohm resistor  -- GREEN
			-- 470 ohm resistor  -- GREEN
			-- 1  kohm resistor  -- GREEN
	        -- 2.2kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
			-- 470 ohm resistor  -- RED
			-- 1  kohm resistor  -- RED
	  bit 0 -- 2.2kohm resistor  -- RED
	
	  bit 3 -- 220 ohm resistor  -- BLUE
			-- 470 ohm resistor  -- BLUE
			-- 1  kohm resistor  -- BLUE
	  bit 0 -- 2.2kohm resistor  -- BLUE
	
	***************************************************************************/
        static int TOTAL_COLORS(int gfxn) {
            return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
        }
	public static VhConvertColorPromPtr pacland_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		pacland_color_prom = new UBytePtr(color_prom);	/* we'll need this later */
		/* skip the palette data, it will be initialized later */
		color_prom.inc(2 * 1024);
		/* color_prom now points to the beginning of the lookup table */
	
		/* Sprites */
		for (i = 0;i < TOTAL_COLORS(2)/3;i++)
		{
			colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] =  (char)(color_prom.readinc());
	
			/* color 0x7f is special, it makes the foreground tiles it overlaps */
			/* transparent (used in round 19) */
			if (colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] == 0x7f)
                        {
                            int offset=i + 2*TOTAL_COLORS(2)/3;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start +offset] = colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start +i];
                        }
                        else
                        {
                            int offset = i + 2*TOTAL_COLORS(2)/3;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start +offset] = 0xff;
                        }
	
			/* transparent colors are 0x7f and 0xff - map all to 0xff */
			if (colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] == 0x7f) colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = 0xff;
	
			/* high priority colors which appear over the foreground even when */
			/* the foreground has priority over sprites */
			if (colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i]  >= 0xf0)   
                        {
                            int offset= i + TOTAL_COLORS(2)/3;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start+offset] = colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i];
                        }
                        else 
                        {
                            int offset = i + TOTAL_COLORS(2)/3;
                            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + offset] = 0xff;
                        }
		}
	
		/* Foreground */
		for (i = 0;i < TOTAL_COLORS(0);i++)
		{
			colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i]  = (char)(color_prom.readinc());
			/* transparent colors are 0x7f and 0xff - map all to 0xff */
			if (colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] == 0x7f) colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = 0xff;
		}
	
		/* Background */
		for (i = 0;i < TOTAL_COLORS(1);i++)
		{
			colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char)(color_prom.readinc());
		}
	
		/* Intialize transparency */
		if (palette_used_colors != null)
		{
			//memset (palette_used_colors, PALETTE_COLOR_USED, Machine.drv.total_colors);
                        for(int k=0; k<Machine.drv.total_colors; k++)
                            palette_used_colors.write(i,PALETTE_COLOR_USED);
			palette_used_colors.write(0xff,PALETTE_COLOR_TRANSPARENT);
		}
	} };
	
	
	
	public static VhStartPtr pacland_vh_start = new VhStartPtr() { public int handler() 
	{
		if ( ( dirtybuffer = new char[videoram_size[0]+1] ) == null)//shadow hack! added +1 to dirtybuffer else it overflows?
			return 1;
		memset (dirtybuffer, 1, videoram_size[0]);
	
		if ( ( tmpbitmap = osd_create_bitmap( 64*8, 32*8 ) ) == null )
		{
			dirtybuffer=null;
			return 1;
		}
	
		if ( ( tmpbitmap2 = osd_create_bitmap( 64*8, 32*8 ) ) == null )
		{
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			return 1;
		}
	
		if ( ( tmpbitmap3 = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height) ) == null )
		{
			osd_free_bitmap(tmpbitmap2);
			osd_free_bitmap(tmpbitmap);
			dirtybuffer=null;
			return 1;
		}
	
		palette_bank = -1;
	
		return 0;
	} };
	
	public static VhStopPtr pacland_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap3);
		osd_free_bitmap(tmpbitmap2);
		osd_free_bitmap(tmpbitmap);
		dirtybuffer=null;
	} };
	
	
	
	
	public static WriteHandlerPtr pacland_scroll0_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll0 = data + 256 * offset;
	} };
	
	public static WriteHandlerPtr pacland_scroll1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		scroll1 = data + 256 * offset;
	} };
	
	
	
	public static WriteHandlerPtr pacland_bankswitch_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int bankaddress;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		bankaddress = 0x10000 + ((data & 0x07) << 13);
		cpu_setbank(1,new UBytePtr(RAM,bankaddress));
	
	//	pbc = data & 0x20;
	
		if (palette_bank != ((data & 0x18) >> 3))
		{
			int i;
			UBytePtr color_prom;
	
			palette_bank = (data & 0x18) >> 3;
			color_prom = new UBytePtr(pacland_color_prom,256 * palette_bank);
	
			for (i = 0;i < 256;i++)
			{
				int bit0,bit1,bit2,bit3;
				int r,g,b;
	
				bit0 = (color_prom.read(0) >> 0) & 0x01;
				bit1 = (color_prom.read(0) >> 1) & 0x01;
				bit2 = (color_prom.read(0) >> 2) & 0x01;
				bit3 = (color_prom.read(0) >> 3) & 0x01;
				r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
				bit0 = (color_prom.read(0) >> 4) & 0x01;
				bit1 = (color_prom.read(0) >> 5) & 0x01;
				bit2 = (color_prom.read(0) >> 6) & 0x01;
				bit3 = (color_prom.read(0) >> 7) & 0x01;
				g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
				bit0 = (color_prom.read(1024) >> 0) & 0x01;
				bit1 = (color_prom.read(1024) >> 1) & 0x01;
				bit2 = (color_prom.read(1024) >> 2) & 0x01;
				bit3 = (color_prom.read(1024) >> 3) & 0x01;
				b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
				color_prom.inc();
	
				palette_change_color(i,r,g,b);
			}
		}
		palette_change_color(0x7f,8,8,8);	/* make color 0x7f unique so we can use it for transparency */
	} };
	
	
	
	static void DRAW_SPRITE( osd_bitmap bitmap,int gfx,int color,int flipx,int flipy,int code, int sx, int sy ) 
			{ drawgfx( bitmap, Machine.gfx[ 2+gfx ], code, color, flipx, flipy, sx, sy, 
			spritevisiblearea, TRANSPARENCY_COLOR,0xff); }
	
	static void pacland_draw_sprites( osd_bitmap bitmap,int priority)
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size[0];offs += 2)
		{
			int sprite = spriteram.read(offs);
			int gfx = ( spriteram_3.read(offs) >> 7 ) & 1;
			int color = ( spriteram.read(offs+1) & 0x3f ) + 64 * priority;
			int x = (spriteram_2.read(offs+1)) + 0x100*(spriteram_3.read(offs+1) & 1) - 48;
			int y = 256 - spriteram_2.read(offs) - 23;
			int flipy = spriteram_3.read(offs) & 2;
			int flipx = spriteram_3.read(offs) & 1;
	
			switch ( spriteram_3.read(offs) & 0x0c )
			{
				case 0:		/* normal size */
					DRAW_SPRITE(  bitmap,gfx,color,flipx,flipy,sprite, x, y );
				break;
	
				case 4:		/* 2x horizontal */
					sprite &= ~1;
					if (flipx==0)
					{
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x+16, y );
					} else {
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x+16, y );
					}
				break;
	
				case 8:		/* 2x vertical */
					sprite &= ~2;
					if (flipy==0)
					{
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x, y );
					} else {
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x, y );
					}
				break;
	
				case 12:		/* 2x both ways */
					sprite &= ~3;
					if ( flipy==0 && flipx==0 )
					{
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x+16, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,3+sprite, x+16, y );
					} else if ( flipy!=0 && flipx!=0 ) {
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,3+sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x+16, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x+16, y );
					} else if (flipx != 0) {
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x+16, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,3+sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x+16, y );
					} else /* flipy */ {
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,2+sprite, x, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,3+sprite, x+16, y-16 );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,sprite, x, y );
						DRAW_SPRITE( bitmap,gfx,color,flipx,flipy,1+sprite, x+16, y );
					}
					break;
			}
		}
	}
	
	
	
	public static VhUpdatePtr pacland_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int sx,sy, code, flipx, flipy, color;
	
	
		/* recalc the palette if necessary */
		if (palette_recalc ()!=null)
			memset (dirtybuffer, 1, videoram_size[0]);
	
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for ( offs = videoram_size[0] / 2; offs < videoram_size[0]; offs += 2 )
		{
			if ( dirtybuffer[offs]!=0 || dirtybuffer[offs+1]!=0 )
			{
				dirtybuffer[offs] = dirtybuffer[offs+1] = 0;
	
				sx = ( ( ( offs - ( videoram_size[0] / 2 ) ) % 128 ) / 2 );
				sy = ( ( ( offs - ( videoram_size[0] / 2 ) ) / 128 ) );
	
				flipx = videoram.read(offs+1) & 0x40;
				flipy = videoram.read(offs+1) & 0x80;
	
				code = videoram.read(offs) + ((videoram.read(offs+1) & 0x01) << 8);
				color = ((videoram.read(offs+1) & 0x3e) >> 1) + ((code & 0x1c0) >> 1);
	
				drawgfx(tmpbitmap,Machine.gfx[1],
						code,
						color,
						flipx,flipy,
						sx*8,sy*8,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy scrolled contents */
		{
			int i;
                        int[] scroll=new int[32];
	
			/* x position is adjusted to make the end of level door border aligned */
			for ( i = 0; i < 32; i++ )
			{
				if ( i < 5 || i > 28 )
					scroll[i] = 2;
				else
					scroll[i] = -scroll1+2;
			}
	
			copyscrollbitmap( bitmap, tmpbitmap, 32, scroll, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0 );
		}
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for ( offs = 0; offs < videoram_size[0] / 2; offs += 2 )
		{
			if ( dirtybuffer[offs]!=0 || dirtybuffer[offs+1]!=0 )
			{
				dirtybuffer[offs] = dirtybuffer[offs+1] = 0;
	
				sx = ( ( offs % 128 ) / 2 );
				sy = ( ( offs / 128 ) );
	
				flipx = videoram.read(offs+1) & 0x40;
				flipy = videoram.read(offs+1) & 0x80;
	
				code = videoram.read(offs) + ((videoram.read(offs+1) & 0x01) << 8);
				color = ((videoram.read(offs+1) & 0x1e) >> 1) + ((code & 0x1e0) >> 1);
	
				drawgfx(tmpbitmap2,Machine.gfx[0],
						code,
						color,
						flipx,flipy,
						sx*8,sy*8,
						null,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy scrolled contents */
		fillbitmap(tmpbitmap3,Machine.pens[0x7f],Machine.drv.visible_area);
		{
			int i;
                        int[] scroll=new int[32];
	
			for ( i = 0; i < 32; i++ )
			{
				if ( i < 5 || i > 28 )
					scroll[i] = 0;
				else
					scroll[i] = -scroll0;
			}
	
			copyscrollbitmap( tmpbitmap3, tmpbitmap2, 32, scroll, 0, null, Machine.drv.visible_area, TRANSPARENCY_COLOR, 0xff );
		}
		pacland_draw_sprites(tmpbitmap3,2);
		copybitmap(bitmap,tmpbitmap3,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_COLOR,0x7f);
	
		pacland_draw_sprites(bitmap,0);
	
		/* redraw the tiles which have priority over the sprites */
		fillbitmap(tmpbitmap3,Machine.pens[0x7f],Machine.drv.visible_area);
		for ( offs = 0; offs < videoram_size[0] / 2; offs += 2 )
		{
			if ((videoram.read(offs+1) & 0x20)!=0)
			{
				int scroll;
	
	
				sx = ( ( offs % 128 ) / 2 );
				sy = ( ( offs / 128 ) );
	
				if ( sy < 5 || sy > 28 )
					scroll = 0;
				else
					scroll = -scroll0;
	
				if (sx*8 + scroll < -8) scroll += 512;
	
				flipx = videoram.read(offs+1) & 0x40;
				flipy = videoram.read(offs+1) & 0x80;
	
				code = videoram.read(offs) + ((videoram.read(offs+1) & 0x01) << 8);
				color = ((videoram.read(offs+1) & 0x1e) >> 1) + ((code & 0x1e0) >> 1);
	
				drawgfx(tmpbitmap3,Machine.gfx[0],
						code,
						color,
						flipx,flipy,
						sx*8 + scroll,sy*8,
						Machine.drv.visible_area,TRANSPARENCY_COLOR,0xff);
			}
		}
		pacland_draw_sprites(tmpbitmap3,2);
		copybitmap(bitmap,tmpbitmap3,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_COLOR,0x7f);
	
		pacland_draw_sprites(bitmap,1);
	} };
}
