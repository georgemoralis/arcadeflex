/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
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
import static vidhrdw.system1H.*;
import static arcadeflex.libc_old.*;
import static mame.palette.*;
import static mame.paletteH.*;
import static arcadeflex.video.*;
import static drivers.system1.*;
import static mame.memory.*;
import static mame.memoryH.*;
import static mame.common.*;
import static mame.commonH.*;


public class system1
{
	
	public static CharPtr system1_scroll_y=new CharPtr();
	public static CharPtr system1_scroll_x=new CharPtr();
	public static CharPtr system1_videoram=new CharPtr();
	public static CharPtr system1_backgroundram=new CharPtr();
	public static CharPtr system1_sprites_collisionram=new CharPtr();
	public static CharPtr system1_background_collisionram=new CharPtr();
	public static CharPtr system1_scrollx_ram=new CharPtr();
	public static int[] 	system1_videoram_size=new int[1];
	public static int[] 	system1_backgroundram_size=new int[1];
	
	public static char[] bg_ram;
	public static char[] bg_dirtybuffer;
	public static char[] tx_dirtybuffer;
	public static char[] SpritesCollisionTable;
	static int  background_scrollx=0,background_scrolly=0;
	static char bg_bank=0,bg_bank_latch=0;
	
	static int[] scrollx_row=new int[32];
	static osd_bitmap bitmap1;
	static osd_bitmap bitmap2;
	
	static int  system1_pixel_mode = 0,system1_background_memory,system1_video_mode=0;
	
	static char[] palette_lookup=new char[256*3];
	
	
	
	
	public static VhConvertColorPromPtr system1_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
	
		//palette = palette_lookup; // todo at the bottom
                int p_inc=0;
		if (color_prom != null)
		{
			for (i = 0;i < 256;i++)
			{
				int bit0,bit1,bit2,bit3;
	
				bit0 = (color_prom.read(0*256) >> 0) & 0x01;
				bit1 = (color_prom.read(0*256) >> 1) & 0x01;
				bit2 = (color_prom.read(0*256) >> 2) & 0x01;
				bit3 = (color_prom.read(0*256) >> 3) & 0x01;
				palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
				bit0 = (color_prom.read(1*256) >> 0) & 0x01;
				bit1 = (color_prom.read(1*256) >> 1) & 0x01;
				bit2 = (color_prom.read(1*256) >> 2) & 0x01;
				bit3 = (color_prom.read(1*256) >> 3) & 0x01;
				palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
				bit0 = (color_prom.read(2*256) >> 0) & 0x01;
				bit1 = (color_prom.read(2*256) >> 1) & 0x01;
				bit2 = (color_prom.read(2*256) >> 2) & 0x01;
				bit3 = (color_prom.read(2*256) >> 3) & 0x01;
				palette[p_inc++].set((char)(0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
				color_prom.inc();
			}
		}
		else
		{
			for (i = 0;i < 256;i++)
			{
				int val;
	
				/* red component */
				val = (i >> 0) & 0x07;
				palette[p_inc++].set((char)((val << 5) | (val << 2) | (val >> 1)));
				/* green component */
				val = (i >> 3) & 0x07;
				palette[p_inc++].set((char)((val << 5) | (val << 2) | (val >> 1)));
				/* blue component */
				val = (i >> 5) & 0x06;
				if (val != 0) val++;
				palette[p_inc++].set((char)((val << 5) | (val << 2) | (val >> 1)));
			}
		}
                //(shadow) copy palette to palette_lookup table
                for(int k=0; k<palette_lookup.length; k++)
                {
                    palette_lookup[k]=palette[k].read();
                }
	} };
	
	public static WriteHandlerPtr system1_paletteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		//unsigned char *palette = palette_lookup + data * 3;
                CharPtr palette = new CharPtr(palette_lookup,data*3);
		int r,g,b;
	
	
		paletteram.write(offset,data);
	
		r = palette.readinc();//*palette++;
		g = palette.readinc();//*palette++;
		b = palette.readinc();//*palette++;
	
		palette_change_color(offset,r,g,b);
	} };
	
	
	
	public static VhStartPtr system1_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((SpritesCollisionTable = new char[256*256]) == null)
			return 1;
		memset(SpritesCollisionTable,255,256*256);
	
		if ((bg_dirtybuffer = new char[1024]) == null)
		{
			SpritesCollisionTable=null;
			return 1;
		}
		memset(bg_dirtybuffer,1,1024);
		if ((tx_dirtybuffer = new char[1024]) == null)
		{
			bg_dirtybuffer=null;
			SpritesCollisionTable=null;
			return 1;
		}
		memset(tx_dirtybuffer,1,1024);
		if ((bg_ram = new char[0x4000]) == null)			/* Allocate 16k for background banked ram */
		{
			bg_dirtybuffer=null;
			tx_dirtybuffer=null;
			SpritesCollisionTable=null;
			return 1;
		}
		memset(bg_ram,0,0x4000);
		if ((bitmap1 = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == null)
		{
			bg_ram=null;
			bg_dirtybuffer=null;
			tx_dirtybuffer=null;
			SpritesCollisionTable=null;
			return 1;
		}
		if ((bitmap2 = osd_create_bitmap(Machine.drv.screen_width,Machine.drv.screen_height)) == null)
		{
			osd_free_bitmap(bitmap1);
			bg_ram=null;
			bg_dirtybuffer=null;
			tx_dirtybuffer=null;
			SpritesCollisionTable=null;
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr system1_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(bitmap2);
		osd_free_bitmap(bitmap1);
		bg_ram=null;
		bg_dirtybuffer=null;
		tx_dirtybuffer=null;
		SpritesCollisionTable=null;
	} };
	
	public static WriteHandlerPtr system1_videomode_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
            if (errorlog!=null && ((data & 0xef)!=0)) fprintf(errorlog,"videomode = %02x\n",data);
	
		/* bit 0 is coin counter */
	
		/* bit 3 is ??? */
	
		/* bit 4 is screen blank */
		system1_video_mode = data;
	} };
	
	public static ReadHandlerPtr system1_videomode_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return system1_video_mode;
	} };
	
	public static void system1_define_sprite_pixelmode(int Mode)
	{
		system1_pixel_mode = Mode;
	}
	
	public static void system1_define_background_memory(int Mode)
	{
		system1_background_memory = Mode;
	}
	
	public static ReadHandlerPtr GetSpriteBottomY = new ReadHandlerPtr() { public int handler(int spr_number)
	{
		return  spriteram.read(0x10 * spr_number + SPR_Y_BOTTOM);
	} };
	
	
	static void Pixel(osd_bitmap bitmap,int x,int y,int spr_number,int color)
	{
		int xr,yr,spr_y1,spr_y2;
		int SprOnScreen;
	
	
		if (x < Machine.drv.visible_area.min_x ||
			x > Machine.drv.visible_area.max_x ||
			y < Machine.drv.visible_area.min_y ||
			y > Machine.drv.visible_area.max_y)
			return;
	
		if (SpritesCollisionTable[256*y+x] == 255)
		{
			SpritesCollisionTable[256*y+x] = (char)spr_number;
			plot_pixel.handler(bitmap, x, y, color);
		}
		else
		{
			SprOnScreen=SpritesCollisionTable[256*y+x];
			system1_sprites_collisionram.write(SprOnScreen + 32 * spr_number,0xff);
			if (system1_pixel_mode==system1_SPRITE_PIXEL_MODE1)
			{
				spr_y1 = GetSpriteBottomY.handler(spr_number);
				spr_y2 = GetSpriteBottomY.handler(SprOnScreen);
				if (spr_y1 >= spr_y2)
				{
					plot_pixel.handler(bitmap, x, y, color);
					SpritesCollisionTable[256*y+x]=(char)spr_number;
				}
			}
			else
			{
				plot_pixel.handler(bitmap, x, y, color);
				SpritesCollisionTable[256*y+x]=(char)spr_number;
			}
		}
	
		xr = ((x - background_scrollx) & 0xff) / 8;
		yr = ((y - background_scrolly) & 0xff) / 8;
	
		/* TODO: bits 5 and 6 of backgroundram are also used (e.g. Pitfall2, Mr. Viking) */
		/* what's the difference? Bit 7 is used in Choplifter/WBML for extra char bank */
		/* selection, but it is also set in Pitfall2 */
	
		if (system1_background_memory == system1_BACKGROUND_MEMORY_SINGLE)
		{
			if ((system1_backgroundram.read(2 * (32 * yr + xr) + 1) & 0x10)!=0)
				system1_background_collisionram.write(0x20 + spr_number,0xff);
		}
		else
		{
			/* TODO: I should handle the paged background memory here. */
			/* maybe collision detection is not used by the paged games */
			/* (wbml and tokisens), though tokisens doesn't play very well */
			/* (you can't seem to fit in gaps where you should fit) */
		}
	
		/* TODO: collision should probably be checked with the foreground as well */
		/* (TeddyBoy Blues, head of the tiger in girl bonus round) */
	}
	
	public static WriteHandlerPtr system1_background_collisionram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* to do the RAM check, Mister Viking writes 0xff and immediately */
		/* reads it back, expecting bit 0 to be NOT set. */
		system1_background_collisionram.write(offset,0x7e);
	} };
	
	public static WriteHandlerPtr system1_sprites_collisionram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* to do the RAM check, Mister Viking write 0xff and immediately */
		/* reads it back, expecting bit 0 to be NOT set. */
		/* Up'n Down expects to find 0x7e at f800 before doing the whole */
		/* collision test */
		system1_sprites_collisionram.write(offset,0x7e);
	} };
	
	
	
	
	static void RenderSprite(osd_bitmap bitmap,int spr_number)
	{
		int SprX,SprY,Col,Row,Height,src;
		int bank;
		CharPtr SprReg =new CharPtr();//unsigned char *SprReg;
		CharPtr SprPalette=new CharPtr();//unsigned short *SprPalette;
		short skip;	/* bytes to skip before drawing each row (can be negative) */
	
	
		SprReg.set(spriteram, 0x10 * spr_number);//SprReg		= spriteram + 0x10 * spr_number;
	
		src = SprReg.read(SPR_GFXOFS_LO) + (SprReg.read(SPR_GFXOFS_HI) << 8);
		bank = 0x8000 * (((SprReg.read(SPR_X_HI) & 0x80) >> 7) + ((SprReg.read(SPR_X_HI) & 0x40) >> 5));
		bank &= (memory_region_length(REGION_GFX2)-1);	/* limit to the range of available ROMs */
		skip = (short)(SprReg.read(SPR_SKIP_LO) + (SprReg.read(SPR_SKIP_HI) << 8));
	
		Height		= SprReg.read(SPR_Y_BOTTOM) - SprReg.read(SPR_Y_TOP);
		SprPalette.set(Machine.remapped_colortable,0x10 * spr_number);//SprPalette	= Machine.remapped_colortable + 0x10 * spr_number;
		SprX = SprReg.read(SPR_X_LO) + ((SprReg.read(SPR_X_HI) & 0x01) << 8);
		SprX /= 2;	/* the hardware has sub-pixel placement, it seems */
		if (Machine.gamedrv == driver_wbml || Machine.gamedrv.clone_of == driver_wbml)
			SprX += 7;
		SprY = SprReg.read(SPR_Y_TOP) + 1;
	
		for (Row = 0;Row < Height;Row++)
		{
			src += skip;
	
			Col = 0;
			while (Col < 256)	/* this is only a safety check, */
								/* drawing is stopped by color == 15 */
			{
				int color1,color2;
	
				if ((src & 0x8000) != 0)	/* flip x */
				{
					int offs,data;
	
					offs = ((src - Col / 2) & 0x7fff) + bank;
	
					/* memory region #2 contains the packed sprite data */
					data = memory_region(REGION_GFX2).read(offs);
					color1 = data & 0x0f;
					color2 = data >> 4;
				}
				else
				{
					int offs,data;
	
					offs = ((src + Col / 2) & 0x7fff) + bank;
	
					/* memory region #2 contains the packed sprite data */
					data = memory_region(REGION_GFX2).read(offs);
					color1 = data >> 4;
					color2 = data & 0x0f;
				}
	
				if (color1 == 15) break;
				if (color1 != 0)
					Pixel(bitmap,SprX+Col,SprY+Row,spr_number,SprPalette.read(color1));
	
				Col++;
	
				if (color2 == 15) break;
				if (color2 != 0)
					Pixel(bitmap,SprX+Col,SprY+Row,spr_number,SprPalette.read(color2));
	
				Col++;
			}
		}
	}
	
	
	static void DrawSprites(osd_bitmap bitmap)
	{
		int spr_number,SprBottom,SprTop;
		CharPtr SprReg=new CharPtr();//unsigned char *SprReg;
	
	
		memset(SpritesCollisionTable,255,256*256);
	
		for (spr_number = 0;spr_number < 32;spr_number++)
		{
			SprReg.set(spriteram,0x10 * spr_number);//SprReg 		= spriteram + 0x10 * spr_number;
			SprTop		= SprReg.read(SPR_Y_TOP);
			SprBottom	= SprReg.read(SPR_Y_BOTTOM);
			if (SprBottom!=0 && (SprBottom-SprTop > 0))
				RenderSprite(bitmap,spr_number);
		}
	}
	
	
	
	public static void system1_compute_palette ()
	{
		//unsigned char bg_usage[64], tx_usage[64], sp_usage[32];
                char bg_usage[] = new char[64];
                char tx_usage[] = new char[64];
                char sp_usage[] = new char[64];
		int i;
	
		//memset (bg_usage, 0, sizeof (bg_usage));
		//memset (tx_usage, 0, sizeof (tx_usage));
		//memset (sp_usage, 0, sizeof (sp_usage));
	
		for (i = 0; i<system1_backgroundram_size[0]; i+=2)
		{
			int code = (system1_backgroundram.read(i) + (system1_backgroundram.read(i+1) << 8)) & 0x7FF;
			int palette = code >> 5;
			bg_usage[palette & 0x3f] = 1;
		}
	
		for (i = 0; i<system1_videoram_size[0]; i+=2)
		{
			int code = (system1_videoram.read(i) + (system1_videoram.read(i+1) << 8)) & 0x7FF;
	
			if (code != 0)
			{
				int palette = code>>5;
				tx_usage[palette & 0x3f] = 1;
			}
		}
	
		for (i=0; i<32; i++)
		{
			CharPtr reg = new CharPtr();//unsigned char *reg;
			int top, bottom;
	
			reg.set(spriteram,0x10 * i);//reg 	= spriteram + 0x10 * i;
			top		= reg.read(SPR_Y_TOP);
			bottom	= reg.read(SPR_Y_BOTTOM);
			if (bottom!=0 && (bottom - top > 0))
				sp_usage[i] = 1;
		}
	
		for (i = 0; i < 64; i++)
		{
			if (bg_usage[i]!=0)
				memset (palette_used_colors,1024 + i * 8, PALETTE_COLOR_USED, 8);
			else
				memset (palette_used_colors,1024 + i * 8, PALETTE_COLOR_UNUSED, 8);
	
			palette_used_colors.write(512 + i * 8,PALETTE_COLOR_TRANSPARENT);
			if (tx_usage[i]!=0)
				memset (palette_used_colors,512 + i * 8 + 1, PALETTE_COLOR_USED, 7);
			else
				memset (palette_used_colors,512 + i * 8 + 1, PALETTE_COLOR_UNUSED, 7);
		}
	
		for (i = 0; i < 32; i++)
		{
			palette_used_colors.write(0 + i * 16,PALETTE_COLOR_TRANSPARENT);
			if (sp_usage[i]!=0)
				memset (palette_used_colors,0 + i * 16 + 1, PALETTE_COLOR_USED, 15);
			else
				memset (palette_used_colors,0 + i * 16 + 1, PALETTE_COLOR_UNUSED, 15);
		}
	
		if (palette_recalc ()!=null)
		{
			memset(bg_dirtybuffer,1,1024);
			memset(tx_dirtybuffer,1,1024);
		}
	}
	
	
	public static WriteHandlerPtr system1_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		system1_videoram.write(offset,data);
		tx_dirtybuffer[offset>>1] = 1;
	} };
	
	public static WriteHandlerPtr system1_backgroundram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		system1_backgroundram.write(offset,data);
		bg_dirtybuffer[offset>>1] = 1;
	} };
	
	
	static int system1_draw_fg(osd_bitmap bitmap,int priority)
	{
		int sx,sy,offs;
		int drawn = 0;
	
	
		priority <<= 3;
	
		for (offs = 0;offs < system1_videoram_size[0];offs += 2)
		{
			if ((system1_videoram.read(offs+1) & 0x08) == priority)
			{
				int code,color;
	
	
				code = (system1_videoram.read(offs) | (system1_videoram.read(offs+1) << 8));
				code = ((code >> 4) & 0x800) | (code & 0x7ff);	/* Heavy Metal only */
				color = ((code >> 5) & 0x3f);
				sx = (offs/2) % 32;
				sy = (offs/2) / 32;
	
				if ((Machine.gfx[0].pen_usage[code] & ~1)!=0)
				{
					drawn = 1;
	
					drawgfx(bitmap,Machine.gfx[0],
							code,
							color,
							0,0,
							8*sx,8*sy,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	
		return drawn;
	}
	
	static void system1_draw_bg(osd_bitmap bitmap,int priority)
	{
		int sx,sy,offs;
	
	
		background_scrollx = ((system1_scroll_x.read(0) >> 1) + ((system1_scroll_x.read(1) & 1) << 7) + 14) & 0xff;
		background_scrolly = (-system1_scroll_y.read()) & 0xff;
	
		if (priority == -1)
		{
			/* optimized far background */
	
			/* for every character in the background video RAM, check if it has
			 * been modified since last time and update it accordingly.
			 */
	
			for (offs = 0;offs < system1_backgroundram_size[0];offs += 2)
			{
				if (bg_dirtybuffer[offs / 2]!=0)
				{
					int code,color;
	
	
					bg_dirtybuffer[offs / 2] = 0;
	
					code = (system1_backgroundram.read(offs) | (system1_backgroundram.read(offs+1) << 8));
					code = ((code >> 4) & 0x800) | (code & 0x7ff);	/* Heavy Metal only */
					color = ((code >> 5) & 0x3f) + 0x40;
					sx = (offs/2) % 32;
					sy = (offs/2) / 32;
	
					drawgfx(bitmap1,Machine.gfx[0],
							code,
							color,
							0,0,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
				}
			}
	
			/* copy the temporary bitmap to the screen */
			copyscrollbitmap(bitmap,bitmap1,1,new int[] {background_scrollx},1,new int[] {background_scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
		else
		{
			priority <<= 3;
	
			for (offs = 0;offs < system1_backgroundram_size[0];offs += 2)
			{
				if ((system1_backgroundram.read(offs+1) & 0x08) == priority)
				{
					int code,color;
	
	
					code = (system1_backgroundram.read(offs) | (system1_backgroundram.read(offs+1) << 8));
					code = ((code >> 4) & 0x800) | (code & 0x7ff);	/* Heavy Metal only */
					color = ((code >> 5) & 0x3f) + 0x40;
					sx = (offs/2) % 32;
					sy = (offs/2) / 32;
	
					sx = 8*sx + background_scrollx;
					sy = 8*sy + background_scrolly;
	
					drawgfx(bitmap,Machine.gfx[0],
							code,
							color,
							0,0,
							sx,sy,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0);
					if (sx > 248)
						drawgfx(bitmap,Machine.gfx[0],
								code,
								color,
								0,0,
								sx-256,sy,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0);
					if (sy > 248)
					{
						drawgfx(bitmap,Machine.gfx[0],
								code,
								color,
								0,0,
								sx,sy-256,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0);
						if (sx > 248)
							drawgfx(bitmap,Machine.gfx[0],
									code,
									color,
									0,0,
									sx-256,sy-256,
									Machine.drv.visible_area,TRANSPARENCY_PEN,0);
					}
				}
			}
		}
	}
	
	public static VhUpdatePtr system1_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int drawn;
	
	
		system1_compute_palette();
	
		system1_draw_bg(bitmap,-1);
		drawn = system1_draw_fg(bitmap,0);
		/* redraw low priority bg tiles if necessary */
		if (drawn != 0) system1_draw_bg(bitmap,0);
		DrawSprites(bitmap);
		system1_draw_bg(bitmap,1);
		system1_draw_fg(bitmap,1);
	
		/* even if screen is off, sprites must still be drawn to update the collision table */
		if ((system1_video_mode & 0x10) != 0)  /* screen off */
			fillbitmap(bitmap,palette_transparent_color,Machine.drv.visible_area);
	} };
	

	public static WriteHandlerPtr choplifter_scroll_x_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		system1_scrollx_ram.write(offset,data);
	
		scrollx_row[offset/2] = (system1_scrollx_ram.read(offset & ~1) >> 1) + ((system1_scrollx_ram.read(offset | 1) & 1) << 7);
	} };
	
	public static void chplft_draw_bg(osd_bitmap bitmap, int priority)
	{
		int sx,sy,offs;
		int choplifter_scroll_x_on = (system1_scrollx_ram.read(0) == 0xE5 && system1_scrollx_ram.read(1) == 0xFF) ? 0:1;
	
	
		if (priority == -1)
		{
			/* optimized far background */
	
			/* for every character in the background video RAM, check if it has
			 * been modified since last time and update it accordingly.
			 */
	
			for (offs = 0;offs < system1_backgroundram_size[0];offs += 2)
			{
				if (bg_dirtybuffer[offs / 2]!=0)
				{
					int code,color;
	
	
					bg_dirtybuffer[offs / 2] = 0;
	
					code = (system1_backgroundram.read(offs) | (system1_backgroundram.read(offs+1) << 8));
					code = ((code >> 4) & 0x800) | (code & 0x7ff);	/* Heavy Metal only */
					color = ((code >> 5) & 0x3f) + 0x40;
					sx = (offs/2) % 32;
					sy = (offs/2) / 32;
	
					drawgfx(bitmap1,Machine.gfx[0],
							code,
							color,
							0,0,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
				}
			}
	
			/* copy the temporary bitmap to the screen */
			if (choplifter_scroll_x_on != 0)
				copyscrollbitmap(bitmap,bitmap1,32,scrollx_row,0,null,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			else
				copybitmap(bitmap,bitmap1,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_NONE,0);
		}
	
		else
		{
			priority <<= 3;
	
			for (offs = 0;offs < system1_backgroundram_size[0];offs += 2)
			{
				if ((system1_backgroundram.read(offs+1) & 0x08) == priority)
				{
					int code,color;
	
	
					code = (system1_backgroundram.read(offs) | (system1_backgroundram.read(offs+1) << 8));
					code = ((code >> 4) & 0x800) | (code & 0x7ff);	/* Heavy Metal only */
					color = ((code >> 5) & 0x3f) + 0x40;
					sx = 8*((offs/2) % 32);
					sy = (offs/2) / 32;
	
					if (choplifter_scroll_x_on != 0)
						sx = (sx + scrollx_row[sy]) & 0xff;
					sy = 8*sy;
	
					drawgfx(bitmap,Machine.gfx[0],
							code,
							color,
							0,0,
							sx,sy,
							Machine.drv.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	public static VhUpdatePtr choplifter_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int drawn;
	
	
		system1_compute_palette();
	
		chplft_draw_bg(bitmap,-1);
		drawn = system1_draw_fg(bitmap,0);
		/* redraw low priority bg tiles if necessary */
		if (drawn != 0) chplft_draw_bg(bitmap,0);
		DrawSprites(bitmap);
		chplft_draw_bg(bitmap,1);
		system1_draw_fg(bitmap,1);
	
		/* even if screen is off, sprites must still be drawn to update the collision table */
		if ((system1_video_mode & 0x10) != 0)  /* screen off */
			fillbitmap(bitmap,palette_transparent_color,Machine.drv.visible_area);
	
	} };
	
	
	
	public static ReadHandlerPtr wbml_bg_bankselect_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bg_bank_latch;
	} };
	
	public static WriteHandlerPtr wbml_bg_bankselect_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		bg_bank_latch = (char)data;
		bg_bank = (char)((data >> 1) & 0x03);	/* Select 4 banks of 4k, bit 2,1 */
	} };
	
	public static ReadHandlerPtr wbml_paged_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return bg_ram[0x1000*bg_bank + offset];
	} };
	
	public static WriteHandlerPtr wbml_paged_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		bg_ram[0x1000*bg_bank + offset] = (char)data;
	} };
	
	public static void wbml_backgroundrefresh(osd_bitmap bitmap, int trasp)
	{
		int page;
	
	
		int xscroll = (bg_ram[0x7c0] >> 1) + ((bg_ram[0x7c1] & 1) << 7) - 256 + 5;
		int yscroll = -bg_ram[0x7ba];
	
		for (page=0; page < 4; page++)
		{
			//const unsigned char *source = bg_ram + (bg_ram[0x0740 + page*2] & 0x07)*0x800;
                        UBytePtr source = new UBytePtr(bg_ram , (bg_ram[0x0740 + page*2] & 0x07)*0x800);
			int startx = (page&1)*256+xscroll;
			int starty = (page>>1)*256+yscroll;
			int row,col;
	
	
			for( row=0; row<32*8; row+=8 )
			{
				for( col=0; col<32*8; col+=8 )
				{
					int code,priority;
					int x = (startx+col) & 0x1ff;
					int y = (starty+row) & 0x1ff;
					if (x > 256) x -= 512;
					if (y > 224) y -= 512;
	
	
					if (x > -8 && y > -8)
					{
						code = source.read(0) + (source.read(1) << 8);
						priority = code & 0x800;
						code = ((code >> 4) & 0x800) | (code & 0x7ff);
	
						if (trasp==0)
							drawgfx(bitmap,Machine.gfx[0],
									code,
									((code >> 5) & 0x3f) + 64,
									0,0,
									x,y,
									Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
						else if (priority != 0)
							drawgfx(bitmap,Machine.gfx[0],
									code,
									((code >> 5) & 0x3f) + 64,
									0,0,
									x,y,
									Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
					}
	
					source.inc(2);//source+=2;
				}
			}
		} /* next page */
	}
	
	public static void wbml_textrefresh(osd_bitmap bitmap)
	{
		int offs;
	
	
		for (offs = 0;offs < 0x700;offs += 2)
		{
			int sx,sy,code;
	
	
			sx = (offs/2) % 32;
			sy = (offs/2) / 32;
			code = bg_ram[offs] | (bg_ram[offs+1] << 8);
			code = ((code >> 4) & 0x800) | (code & 0x7ff);
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					(code >> 5) & 0x3f,
					0,0,
					8*sx,8*sy,
					Machine.drv.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	
	public static VhUpdatePtr wbml_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		palette_recalc();
		/* no need to check the return code since we redraw everything each frame */
	
		wbml_backgroundrefresh(bitmap,0);
		DrawSprites(bitmap);
		wbml_backgroundrefresh(bitmap,1);
		wbml_textrefresh(bitmap);
	
		/* even if screen is off, sprites must still be drawn to update the collision table */
		if ((system1_video_mode & 0x10) != 0)  /* screen off */
			fillbitmap(bitmap,palette_transparent_color,Machine.drv.visible_area);
	} };
}
