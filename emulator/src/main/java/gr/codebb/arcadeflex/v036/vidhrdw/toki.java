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
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;


public class toki
{
	
	public static int SPRITE_Y		=0;
	public static int SPRITE_TILE		=2;
	public static int SPRITE_FLIP_X		=2;
	public static int SPRITE_PAL_BANK		=4;
	public static int SPRITE_X		=6;
	
	public static int XBG1SCROLL_ADJUST(int x){ return (-(x)+0x103);}
	public static int XBG2SCROLL_ADJUST(int x){ return  (-(x)+0x101);}
	public static int YBGSCROLL_ADJUST(int x){ return  (-(x)-1);}
	
	public static UBytePtr toki_foreground_videoram= new UBytePtr();
	public static UBytePtr toki_background1_videoram= new UBytePtr();
	public static UBytePtr toki_background2_videoram= new UBytePtr();
	public static UBytePtr toki_sprites_dataram= new UBytePtr();
	public static UBytePtr toki_scrollram= new UBytePtr();
	/*signed char*/public static byte[] toki_linescroll= new byte[256];
	
	public static int[] toki_foreground_videoram_size=new int[1];
	public static int[] toki_background1_videoram_size=new int[1];
	public static int[] toki_background2_videoram_size=new int[1];
	public static int[] toki_sprites_dataram_size=new int[1];
	
	public static char[] frg_dirtybuffer;		/* foreground */
	public static char[] bg1_dirtybuffer;		/* background 1 */
	public static char[] bg2_dirtybuffer;		/* background 2 */
	
	public static osd_bitmap bitmap_frg;		/* foreground bitmap */
	public static osd_bitmap bitmap_bg1;		/* background bitmap 1 */
	public static osd_bitmap bitmap_bg2;		/* background bitmap 2 */
	
	public static int bg1_scrollx, bg1_scrolly;
	public static int bg2_scrollx, bg2_scrolly;
	
	
	
	/*************************************
	 *
	 *		Start/Stop
	 *
	 *************************************/
	
	public static VhStartPtr toki_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((frg_dirtybuffer = new char[toki_foreground_videoram_size[0] / 2]) == null)
		{
			return 1;
		}
		if ((bg1_dirtybuffer = new char[toki_background1_videoram_size[0] / 2]) == null)
		{
			frg_dirtybuffer=null;
			return 1;
		}
		if ((bg2_dirtybuffer = new char[toki_background2_videoram_size[0] / 2]) == null)
		{
			bg1_dirtybuffer=null;
			frg_dirtybuffer=null;
			return 1;
		}
	
		/* foreground bitmap */
		if ((bitmap_frg = osd_new_bitmap (Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			bg1_dirtybuffer=null;
			bg2_dirtybuffer=null;
			frg_dirtybuffer=null;
			return 1;
		}
	
		/* background1 bitmap */
		if ((bitmap_bg1 = osd_new_bitmap (Machine.drv.screen_width*2,Machine.drv.screen_height*2,Machine.scrbitmap.depth)) == null)
		{
			bg1_dirtybuffer=null;
			bg2_dirtybuffer=null;
			frg_dirtybuffer=null;
			osd_free_bitmap (bitmap_frg);
			return 1;
		}
	
		/* background2 bitmap */
		if ((bitmap_bg2 = osd_new_bitmap (Machine.drv.screen_width*2,Machine.drv.screen_height*2,Machine.scrbitmap.depth)) == null)
		{
			bg1_dirtybuffer=null;
			bg2_dirtybuffer=null;
			frg_dirtybuffer=null;
			osd_free_bitmap (bitmap_bg1);
			osd_free_bitmap (bitmap_frg);
			return 1;
		}
		memset (frg_dirtybuffer,1,toki_foreground_videoram_size[0] / 2);
		memset (bg2_dirtybuffer,1,toki_background1_videoram_size[0] / 2);
		memset (bg1_dirtybuffer,1,toki_background2_videoram_size[0] / 2);
		return 0;
	
	} };
	
	public static VhStopPtr toki_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap (bitmap_frg);
		osd_free_bitmap (bitmap_bg1);
		osd_free_bitmap (bitmap_bg2);
		bg1_dirtybuffer=null;
			bg2_dirtybuffer=null;
			frg_dirtybuffer=null;
	} };
	
	
	
	/*************************************
	 *
	 *		Foreground RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_foreground_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	   int oldword = toki_foreground_videoram.READ_WORD(offset);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			toki_foreground_videoram.WRITE_WORD(offset, data);
			frg_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_foreground_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return toki_foreground_videoram.READ_WORD(offset);
	} };
	
	
	
	/*************************************
	 *
	 *		Background 1 RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_background1_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	   int oldword = toki_background1_videoram.READ_WORD(offset);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			toki_background1_videoram.WRITE_WORD(offset, data);
			bg1_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_background1_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return toki_background1_videoram.READ_WORD(offset);
	} };
	
	
	
	/*************************************
	 *
	 *		Background 2 RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_background2_videoram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	   int oldword = toki_background2_videoram.READ_WORD(offset);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			toki_background2_videoram.WRITE_WORD(offset, data);
			bg2_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_background2_videoram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return toki_background2_videoram.READ_WORD(offset);
	} };
	
	
	
	/*************************************
	 *
	 *		Sprite rendering
	 *
	 *************************************/
	
	public static void toki_render_sprites (osd_bitmap bitmap)
	{
		int SprX,SprY,SprTile,SprFlipX,SprPalette,offs;
		UBytePtr SprRegs;
	
		/* Draw the sprites. 256 sprites in total */
	
		for (offs = 0;offs < toki_sprites_dataram_size[0];offs += 8)
		{
			SprRegs = new UBytePtr(toki_sprites_dataram,offs);
	
			if (SprRegs.READ_WORD(SPRITE_Y)==0xf100) break;
			if (SprRegs.READ_WORD(SPRITE_PAL_BANK)!=0)
			{
	
				SprX = SprRegs.READ_WORD(SPRITE_X) & 0x1ff;
				if (SprX > 256)
					SprX -= 512;
	
				SprY = SprRegs.READ_WORD(SPRITE_Y) & 0x1ff;
				if (SprY > 256)
				  SprY = (512-SprY)+240;
				else
		       		  SprY = 240-SprY;
	
				SprFlipX   = SprRegs.READ_WORD(SPRITE_FLIP_X) & 0x4000;
				SprTile    = SprRegs.READ_WORD(SPRITE_TILE) & 0x1fff;
				SprPalette = SprRegs.READ_WORD(SPRITE_PAL_BANK)>>12;
	
				drawgfx (bitmap,Machine.gfx[1],
						SprTile,
						SprPalette,
						SprFlipX,0,
						SprX,SprY-1,
						Machine.drv.visible_area,TRANSPARENCY_PEN,15);
			}
		}
	}
	
	
	
	/*************************************
	 *
	 *		Background rendering
	 *
	 *************************************/
	
	public static void toki_draw_background1 (osd_bitmap bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_background1_videoram_size[0] / 2;offs++)
		{
			if (bg1_dirtybuffer[offs]!=0)
			{
				code = toki_background1_videoram.READ_WORD(offs*2);
				palette = code>>12;
				sx = (offs  % 32) << 4;
				sy = (offs >>  5) << 4;
				bg1_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[2],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	public static void toki_draw_background2 (osd_bitmap bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_background2_videoram_size[0] / 2;offs++)
		{
			if (bg2_dirtybuffer[offs]!=0)
			{
				code = toki_background2_videoram.READ_WORD(offs*2);
				palette = code>>12;
				sx = (offs  % 32) << 4;
				sy = (offs >>  5) << 4;
				bg2_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[3],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	public static void toki_draw_foreground (osd_bitmap bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_foreground_videoram_size[0] / 2;offs++)
		{
			if (frg_dirtybuffer[offs]!=0)
			{
				code = toki_foreground_videoram.READ_WORD(offs*2);
				palette = code>>12;
	
				sx = (offs % 32) << 3;
				sy = (offs >> 5) << 3;
				frg_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[0],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						null,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	
	/*************************************
	 *
	 *		Master update function
	 *
	 *************************************/
	
	public static VhUpdatePtr toki_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int title_on; 			/* title on screen flag */
	
		bg1_scrolly = YBGSCROLL_ADJUST  (toki_scrollram.READ_WORD(0));
		bg1_scrollx = XBG1SCROLL_ADJUST (toki_scrollram.READ_WORD(2));
		bg2_scrolly = YBGSCROLL_ADJUST  (toki_scrollram.READ_WORD(4));
		bg2_scrollx = XBG2SCROLL_ADJUST (toki_scrollram.READ_WORD(6));
	
		/* Palette mapping first */
		{
			char[] palette_map=new char[16*4];
			int code, palette, offs;
	
			memset (palette_map, 0, sizeof (palette_map));
	
			for (offs = 0; offs < toki_foreground_videoram_size[0] / 2; offs++)
			{
				/* foreground */
				code = toki_foreground_videoram.READ_WORD(offs * 2);
				palette = code >> 12;
				palette_map[16 + palette] |= Machine.gfx[0].pen_usage[code & 0xfff];
				/* background 1 */
				code = toki_background1_videoram.READ_WORD(offs * 2);
				palette = code >> 12;
				palette_map[32 + palette] |= Machine.gfx[2].pen_usage[code & 0xfff];
				/* background 2 */
				code = toki_background2_videoram.READ_WORD(offs * 2);
				palette = code >> 12;
				palette_map[48 + palette] |= Machine.gfx[3].pen_usage[code & 0xfff];
			}
	
			/* sprites */
			for (offs = 0;offs < toki_sprites_dataram_size[0];offs += 8)
			{
				UBytePtr data = new UBytePtr(toki_sprites_dataram,offs);
	
				if (data.READ_WORD(SPRITE_Y) == 0xf100)
					break;
				palette = data.READ_WORD(SPRITE_PAL_BANK);
				if (palette != 0)
				{
					code = data.READ_WORD(SPRITE_TILE) & 0x1fff;
					palette_map[0 + (palette >> 12)] |= Machine.gfx[1].pen_usage[code];
				}
			}
	
			/* expand it */
			for (palette = 0; palette < 16 * 4; palette++)
			{
				char usage = palette_map[palette];
	
				if (usage != 0)
				{
					int i;
	
					for (i = 0; i < 15; i++)
						if ((usage & (1 << i))!=0)
							palette_used_colors.write(palette * 16 + i, PALETTE_COLOR_USED);
						else
							palette_used_colors.write(palette * 16 + i, PALETTE_COLOR_UNUSED);
					palette_used_colors.write(palette * 16 + 15, PALETTE_COLOR_TRANSPARENT);
				}
				else
                                {
					//memset (&palette_used_colors[palette * 16 + 0], PALETTE_COLOR_UNUSED, 16);
                                    for(int i=0; i<16; i++)
                                    {
                                        palette_used_colors.write(palette * 16 + 0 + i, PALETTE_COLOR_UNUSED);
                                    }
                                }
			}
	
			/* recompute */
			if (palette_recalc ()!=null)
			{
				memset (frg_dirtybuffer, 1, toki_foreground_videoram_size[0] / 2);
				memset (bg1_dirtybuffer, 1, toki_background1_videoram_size[0] / 2);
				memset (bg2_dirtybuffer, 1, toki_background2_videoram_size[0] / 2);
			}
		}
	
	
		title_on = (toki_foreground_videoram.READ_WORD(0x710)==0x44) ? 1:0;
	
	 	toki_draw_foreground (bitmap_frg);
		toki_draw_background1 (bitmap_bg1);
	 	toki_draw_background2 (bitmap_bg2);
	
		if (title_on != 0)
		{
			int i;
                        int[] scrollx=new int[512];
	
			for (i = 0;i < 256;i++)
				scrollx[i] = bg2_scrollx - toki_linescroll[i];
	
			copyscrollbitmap (bitmap,bitmap_bg1,1,new int[] {bg1_scrollx},1,new int[] {bg1_scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			if (bg2_scrollx!=-32768)
				copyscrollbitmap (bitmap,bitmap_bg2,512,scrollx,1,new int[]{bg2_scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		} else
		{
			copyscrollbitmap (bitmap,bitmap_bg2,1,new int[]{bg2_scrollx},1,new int[]{bg2_scrolly},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
			copyscrollbitmap (bitmap,bitmap_bg1,1,new int[]{bg1_scrollx},1,new int[]{bg1_scrolly},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	
		toki_render_sprites (bitmap);
	   	copybitmap (bitmap,bitmap_frg,0,0,0,0,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	} };
	
	
	
	static int lastline,lastdata;
	
	public static WriteHandlerPtr toki_linescroll_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset == 2)
		{
			int currline;
	
			currline = cpu_getscanline();
	
			if (currline < lastline)
			{
				while (lastline < 256)
					toki_linescroll[lastline++] = (byte)lastdata;
				lastline = 0;
			}
			while (lastline < currline)
				toki_linescroll[lastline++] = (byte)lastdata;
	
			lastdata = data & 0x7f;
		}
		else
		{
			/* this is the sign, it is either 0x00 or 0xff */
			if (data != 0) lastdata |= 0x80;
		}
	} };
	public static InterruptPtr toki_interrupt = new InterruptPtr() {
        public int handler() {

		while (lastline < 256)
			toki_linescroll[lastline++] = (byte)lastdata;
		lastline = 0;
		return 1;  /*Interrupt vector 1*/
	}};
}
