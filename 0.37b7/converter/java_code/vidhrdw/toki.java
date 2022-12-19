/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class toki
{
	
	#define SPRITE_Y		0
	#define SPRITE_TILE		2
	#define SPRITE_FLIP_X		2
	#define SPRITE_PAL_BANK		4
	#define SPRITE_X		6
	
	#define XBG1SCROLL_ADJUST(x) (-(x)+0x103)
	#define XBG2SCROLL_ADJUST(x) (-(x)+0x101)
	#define YBGSCROLL_ADJUST(x) (-(x)-1)
	
	UBytePtr toki_foreground_videoram;
	UBytePtr toki_background1_videoram;
	UBytePtr toki_background2_videoram;
	UBytePtr toki_sprites_dataram;
	UBytePtr toki_scrollram;
	signed char toki_linescroll[256];
	
	size_t toki_foreground_videoram_size;
	size_t toki_background1_videoram_size;
	size_t toki_background2_videoram_size;
	size_t toki_sprites_dataram_size;
	
	static UBytePtr frg_dirtybuffer;		/* foreground */
	static UBytePtr bg1_dirtybuffer;		/* background 1 */
	static UBytePtr bg2_dirtybuffer;		/* background 2 */
	
	static struct osd_bitmap *bitmap_frg;		/* foreground bitmap */
	static struct osd_bitmap *bitmap_bg1;		/* background bitmap 1 */
	static struct osd_bitmap *bitmap_bg2;		/* background bitmap 2 */
	
	static int bg1_scrollx, bg1_scrolly;
	static int bg2_scrollx, bg2_scrolly;
	
	
	
	/*************************************
	 *
	 *		Start/Stop
	 *
	 *************************************/
	
	public static VhStartPtr toki_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((frg_dirtybuffer = malloc (toki_foreground_videoram_size / 2)) == 0)
		{
			return 1;
		}
		if ((bg1_dirtybuffer = malloc (toki_background1_videoram_size / 2)) == 0)
		{
			free (frg_dirtybuffer);
			return 1;
		}
		if ((bg2_dirtybuffer = malloc (toki_background2_videoram_size / 2)) == 0)
		{
			free (bg1_dirtybuffer);
			free (frg_dirtybuffer);
			return 1;
		}
	
		/* foreground bitmap */
		if ((bitmap_frg = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height)) == 0)
		{
			free (bg1_dirtybuffer);
			free (bg2_dirtybuffer);
			free (frg_dirtybuffer);
			return 1;
		}
	
		/* background1 bitmap */
		if ((bitmap_bg1 = bitmap_alloc(Machine.drv.screen_width*2,Machine.drv.screen_height*2)) == 0)
		{
			free (bg1_dirtybuffer);
			free (bg2_dirtybuffer);
			free (frg_dirtybuffer);
			bitmap_free (bitmap_frg);
			return 1;
		}
	
		/* background2 bitmap */
		if ((bitmap_bg2 = bitmap_alloc(Machine.drv.screen_width*2,Machine.drv.screen_height*2)) == 0)
		{
			free (bg1_dirtybuffer);
			free (bg2_dirtybuffer);
			free (frg_dirtybuffer);
			bitmap_free (bitmap_bg1);
			bitmap_free (bitmap_frg);
			return 1;
		}
		memset (frg_dirtybuffer,1,toki_foreground_videoram_size / 2);
		memset (bg2_dirtybuffer,1,toki_background1_videoram_size / 2);
		memset (bg1_dirtybuffer,1,toki_background2_videoram_size / 2);
		return 0;
	
	} };
	
	public static VhStopPtr toki_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free (bitmap_frg);
		bitmap_free (bitmap_bg1);
		bitmap_free (bitmap_bg2);
		free (bg1_dirtybuffer);
		free (bg2_dirtybuffer);
		free (frg_dirtybuffer);
	} };
	
	
	
	/*************************************
	 *
	 *		Foreground RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_foreground_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	   int oldword = READ_WORD (&toki_foreground_videoram[offset]);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			WRITE_WORD (&toki_foreground_videoram[offset], data);
			frg_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_foreground_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD (&toki_foreground_videoram[offset]);
	} };
	
	
	
	/*************************************
	 *
	 *		Background 1 RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_background1_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	   int oldword = READ_WORD (&toki_background1_videoram[offset]);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			WRITE_WORD (&toki_background1_videoram[offset], data);
			bg1_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_background1_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD (&toki_background1_videoram[offset]);
	} };
	
	
	
	/*************************************
	 *
	 *		Background 2 RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr toki_background2_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	   int oldword = READ_WORD (&toki_background2_videoram[offset]);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			WRITE_WORD (&toki_background2_videoram[offset], data);
			bg2_dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr toki_background2_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD (&toki_background2_videoram[offset]);
	} };
	
	
	
	/*************************************
	 *
	 *		Sprite rendering
	 *
	 *************************************/
	
	void toki_render_sprites (struct osd_bitmap *bitmap)
	{
		int SprX,SprY,SprTile,SprFlipX,SprPalette,offs;
		UBytePtr SprRegs;
	
		/* Draw the sprites. 256 sprites in total */
	
		for (offs = 0;offs < toki_sprites_dataram_size;offs += 8)
		{
			SprRegs = &toki_sprites_dataram[offs];
	
			if (READ_WORD (&SprRegs[SPRITE_Y])==0xf100) break;
			if (READ_WORD (&SprRegs[SPRITE_PAL_BANK]))
			{
	
				SprX = READ_WORD (&SprRegs[SPRITE_X]) & 0x1ff;
				if (SprX > 256)
					SprX -= 512;
	
				SprY = READ_WORD (&SprRegs[SPRITE_Y]) & 0x1ff;
				if (SprY > 256)
				  SprY = (512-SprY)+240;
				else
		       		  SprY = 240-SprY;
	
				SprFlipX   = READ_WORD (&SprRegs[SPRITE_FLIP_X]) & 0x4000;
				SprTile    = READ_WORD (&SprRegs[SPRITE_TILE]) & 0x1fff;
				SprPalette = READ_WORD (&SprRegs[SPRITE_PAL_BANK])>>12;
	
				drawgfx (bitmap,Machine.gfx[1],
						SprTile,
						SprPalette,
						SprFlipX,0,
						SprX,SprY-1,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
			}
		}
	}
	
	
	
	/*************************************
	 *
	 *		Background rendering
	 *
	 *************************************/
	
	void toki_draw_background1 (struct osd_bitmap *bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_background1_videoram_size / 2;offs++)
		{
			if (bg1_dirtybuffer[offs])
			{
				code = READ_WORD (&toki_background1_videoram[offs*2]);
				palette = code>>12;
				sx = (offs  % 32) << 4;
				sy = (offs >>  5) << 4;
				bg1_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[2],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	void toki_draw_background2 (struct osd_bitmap *bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_background2_videoram_size / 2;offs++)
		{
			if (bg2_dirtybuffer[offs])
			{
				code = READ_WORD (&toki_background2_videoram[offs*2]);
				palette = code>>12;
				sx = (offs  % 32) << 4;
				sy = (offs >>  5) << 4;
				bg2_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[3],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	}
	
	
	void toki_draw_foreground (struct osd_bitmap *bitmap)
	{
		int sx,sy,code,palette,offs;
	
		for (offs = 0;offs < toki_foreground_videoram_size / 2;offs++)
		{
			if (frg_dirtybuffer[offs])
			{
				code = READ_WORD (&toki_foreground_videoram[offs*2]);
				palette = code>>12;
	
				sx = (offs % 32) << 3;
				sy = (offs >> 5) << 3;
				frg_dirtybuffer[offs] = 0;
				drawgfx (bitmap,Machine.gfx[0],
						code & 0xfff,
						palette,
						0,0,sx,sy,
						0,TRANSPARENCY_NONE,0);
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
	
		bg1_scrolly = YBGSCROLL_ADJUST  (READ_WORD (&toki_scrollram[0]));
		bg1_scrollx = XBG1SCROLL_ADJUST (READ_WORD (&toki_scrollram[2]));
		bg2_scrolly = YBGSCROLL_ADJUST  (READ_WORD (&toki_scrollram[4]));
		bg2_scrollx = XBG2SCROLL_ADJUST (READ_WORD (&toki_scrollram[6]));
	
		/* Palette mapping first */
		{
			unsigned short palette_map[16*4];
			int code, palette, offs;
	
			memset (palette_map, 0, sizeof (palette_map));
	
			for (offs = 0; offs < toki_foreground_videoram_size / 2; offs++)
			{
				/* foreground */
				code = READ_WORD (&toki_foreground_videoram[offs * 2]);
				palette = code >> 12;
				palette_map[16 + palette] |= Machine.gfx[0].pen_usage[code & 0xfff];
				/* background 1 */
				code = READ_WORD (&toki_background1_videoram[offs * 2]);
				palette = code >> 12;
				palette_map[32 + palette] |= Machine.gfx[2].pen_usage[code & 0xfff];
				/* background 2 */
				code = READ_WORD (&toki_background2_videoram[offs * 2]);
				palette = code >> 12;
				palette_map[48 + palette] |= Machine.gfx[3].pen_usage[code & 0xfff];
			}
	
			/* sprites */
			for (offs = 0;offs < toki_sprites_dataram_size;offs += 8)
			{
				UBytePtr data = &toki_sprites_dataram[offs];
	
				if (READ_WORD (&data[SPRITE_Y]) == 0xf100)
					break;
				palette = READ_WORD (&data[SPRITE_PAL_BANK]);
				if (palette != 0)
				{
					code = READ_WORD (&data[SPRITE_TILE]) & 0x1fff;
					palette_map[0 + (palette >> 12)] |= Machine.gfx[1].pen_usage[code];
				}
			}
	
			/* expand it */
			for (palette = 0; palette < 16 * 4; palette++)
			{
				unsigned short usage = palette_map[palette];
	
				if (usage != 0)
				{
					int i;
	
					for (i = 0; i < 15; i++)
						if (usage & (1 << i))
							palette_used_colors[palette * 16 + i] = PALETTE_COLOR_USED;
						else
							palette_used_colors[palette * 16 + i] = PALETTE_COLOR_UNUSED;
					palette_used_colors[palette * 16 + 15] = PALETTE_COLOR_TRANSPARENT;
				}
				else
					memset (&palette_used_colors[palette * 16 + 0], PALETTE_COLOR_UNUSED, 16);
			}
	
			/* recompute */
			if (palette_recalc ())
			{
				memset (frg_dirtybuffer, 1, toki_foreground_videoram_size / 2);
				memset (bg1_dirtybuffer, 1, toki_background1_videoram_size / 2);
				memset (bg2_dirtybuffer, 1, toki_background2_videoram_size / 2);
			}
		}
	
	
		title_on = (READ_WORD (&toki_foreground_videoram[0x710])==0x44) ? 1:0;
	
	 	toki_draw_foreground (bitmap_frg);
		toki_draw_background1 (bitmap_bg1);
	 	toki_draw_background2 (bitmap_bg2);
	
		if (title_on != 0)
		{
			int i,scrollx[512];
	
			for (i = 0;i < 256;i++)
				scrollx[i] = bg2_scrollx - toki_linescroll[i];
	
			copyscrollbitmap (bitmap,bitmap_bg1,1,&bg1_scrollx,1,&bg1_scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
			if (bg2_scrollx!=-32768)
				copyscrollbitmap (bitmap,bitmap_bg2,512,scrollx,1,&bg2_scrolly,&Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		} else
		{
			copyscrollbitmap (bitmap,bitmap_bg2,1,&bg2_scrollx,1,&bg2_scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
			copyscrollbitmap (bitmap,bitmap_bg1,1,&bg1_scrollx,1,&bg1_scrolly,&Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	
		toki_render_sprites (bitmap);
	   	copybitmap (bitmap,bitmap_frg,0,0,0,0,&Machine.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	} };
	
	
	
	static int lastline,lastdata;
	
	public static WriteHandlerPtr toki_linescroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 2)
		{
			int currline;
	
			currline = cpu_getscanline();
	
			if (currline < lastline)
			{
				while (lastline < 256)
					toki_linescroll[lastline++] = lastdata;
				lastline = 0;
			}
			while (lastline < currline)
				toki_linescroll[lastline++] = lastdata;
	
			lastdata = data & 0x7f;
		}
		else
		{
			/* this is the sign, it is either 0x00 or 0xff */
			if (data != 0) lastdata |= 0x80;
		}
	} };
	
	public static InterruptPtr toki_interrupt = new InterruptPtr() { public int handler() 
	{
		while (lastline < 256)
			toki_linescroll[lastline++] = lastdata;
		lastline = 0;
		return 1;  /*Interrupt vector 1*/
	} };
}
