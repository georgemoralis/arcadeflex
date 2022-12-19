/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class bjtwin
{
	
	UBytePtr bjtwin_cmdram;
	UBytePtr bjtwin_workram;
	UBytePtr bjtwin_spriteram;
	UBytePtr bjtwin_txvideoram;
	UBytePtr bjtwin_videocontrol;
	size_t bjtwin_txvideoram_size;
	
	static UBytePtr  dirtybuffer;
	static struct osd_bitmap *tmpbitmap;
	static int flipscreen = 0;
	
	
	public static VhStartPtr bjtwin_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = malloc(bjtwin_txvideoram_size/2);
		tmpbitmap = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height);
	
		if (!dirtybuffer || !tmpbitmap)
		{
			if (tmpbitmap != 0) bitmap_free(tmpbitmap);
			if (dirtybuffer != 0) free(dirtybuffer);
			return 1;
		}
	
		bjtwin_spriteram = bjtwin_workram + 0x8000;
	
		return 0;
	} };
	
	public static VhStopPtr bjtwin_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(tmpbitmap);
		free(dirtybuffer);
	
		dirtybuffer = 0;
		tmpbitmap = 0;
	} };
	
	
	public static ReadHandlerPtr bjtwin_txvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&bjtwin_txvideoram[offset]);
	} };
	
	public static WriteHandlerPtr bjtwin_txvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&bjtwin_txvideoram[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			WRITE_WORD(&bjtwin_txvideoram[offset],newword);
			dirtybuffer[offset/2] = 1;
		}
	} };
	
	
	public static WriteHandlerPtr bjtwin_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r,g,b;
		int oldword = READ_WORD(&paletteram.read(offset));
		int newword = COMBINE_WORD(oldword,data);
	
	
		WRITE_WORD(&paletteram.read(offset),newword);
	
		r = ((newword >> 11) & 0x1e) | ((newword >> 3) & 0x01);
		g = ((newword >>  7) & 0x1e) | ((newword >> 2) & 0x01);
		b = ((newword >>  3) & 0x1e) | ((newword >> 1) & 0x01);
	
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(offset / 2,r,g,b);
	} };
	
	
	public static WriteHandlerPtr bjtwin_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 1) != flipscreen)
		{
			flipscreen = data & 1;
			memset(dirtybuffer, 1, bjtwin_txvideoram_size/2);
		}
	} };
	
	public static VhUpdatePtr bjtwin_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		static int oldbgstart = -1;
		int offs, bgstart;
	
		bgstart = 2048 * (READ_WORD(&bjtwin_videocontrol[0]) & 0x0f);
	
		palette_init_used_colors();
	
		for (offs = (bjtwin_txvideoram_size/2)-1; offs >= 0; offs--)
		{
			int color = (READ_WORD(&bjtwin_txvideoram[offs*2]) >> 12);
			memset(&palette_used_colors[16 * color],PALETTE_COLOR_USED,16);
		}
	
		for (offs = 0; offs < 256*16; offs += 16)
		{
			if (READ_WORD(&bjtwin_spriteram[offs]) != 0)
				memset(&palette_used_colors[256 + 16*READ_WORD(&bjtwin_spriteram[offs+14])],PALETTE_COLOR_USED,16);
		}
	
		if (palette_recalc() || (oldbgstart != bgstart))
		{
			oldbgstart = bgstart;
			memset(dirtybuffer, 1, bjtwin_txvideoram_size/2);
		}
	
		for (offs = (bjtwin_txvideoram_size/2)-1; offs >= 0; offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx = offs / 32;
				int sy = offs % 32;
	
				int tilecode = READ_WORD(&bjtwin_txvideoram[offs*2]);
				int bank = (tilecode & 0x800) ? 1 : 0;
	
				if (flipscreen != 0)
				{
					sx = 47-sx;
					sy = 31-sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[bank],
						(tilecode & 0x7ff) + ((bank) ? bgstart : 0),
						tilecode >> 12,
						flipscreen, flipscreen,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
	
				dirtybuffer[offs] = 0;
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		for (offs = 0; offs < 256*16; offs += 16)
		{
			if (READ_WORD(&bjtwin_spriteram[offs]) != 0)
			{
				int sx = (READ_WORD(&bjtwin_spriteram[offs+8]) & 0x1ff) + 64;
				int sy = (READ_WORD(&bjtwin_spriteram[offs+12]) & 0x1ff);
				int tilecode = READ_WORD(&bjtwin_spriteram[offs+6]);
				int xx = (READ_WORD(&bjtwin_spriteram[offs+2]) & 0x0f) + 1;
				int yy = (READ_WORD(&bjtwin_spriteram[offs+2]) >> 4) + 1;
				int width = xx;
				int delta = 16;
				int startx = sx;
	
				if (flipscreen != 0)
				{
					sx = 367 - sx;
					sy = 239 - sy;
					delta = -16;
					startx = sx;
				}
	
				do
				{
					do
					{
						drawgfx(bitmap,Machine.gfx[2],
								tilecode & 0x1fff,
								READ_WORD(&bjtwin_spriteram[offs+14]),
								flipscreen, flipscreen,
								sx & 0x1ff,sy & 0x1ff,
								&Machine.visible_area,TRANSPARENCY_PEN,15);
	
						tilecode++;
						sx += delta;
					} while (--xx);
	
					sy += delta;
					sx = startx;
					xx = width;
				} while (--yy);
			}
		}
	} };
}
