/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class macross
{
	
	UBytePtr macross_workram;
	UBytePtr macross_spriteram;
	UBytePtr macross_txvideoram;
	UBytePtr macross_videocontrol;
	size_t macross_txvideoram_size;
	
	static UBytePtr  dirtybuffer;
	static struct osd_bitmap *tmpbitmap;
	static int flipscreen = 0;
	
	
	public static VhStartPtr macross_vh_start = new VhStartPtr() { public int handler() 
	{
		dirtybuffer = malloc(macross_txvideoram_size/2);
		tmpbitmap = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height);
	
		if (!dirtybuffer || !tmpbitmap)
		{
			if (tmpbitmap != 0) bitmap_free(tmpbitmap);
			if (dirtybuffer != 0) free(dirtybuffer);
			return 1;
		}
	
		macross_spriteram = macross_workram + 0x8000;
	
		return 0;
	} };
	
	public static VhStopPtr macross_vh_stop = new VhStopPtr() { public void handler() 
	{
		bitmap_free(tmpbitmap);
		free(dirtybuffer);
	
		dirtybuffer = 0;
		tmpbitmap = 0;
	} };
	
	
	public static ReadHandlerPtr macross_txvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&macross_txvideoram[offset]);
	} };
	
	public static WriteHandlerPtr macross_txvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&macross_txvideoram[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			WRITE_WORD(&macross_txvideoram[offset],newword);
			dirtybuffer[offset/2] = 1;
		}
	} };
	
	
	
	public static VhUpdatePtr macross_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
		palette_init_used_colors();
	
		for (offs = (macross_txvideoram_size/2)-1; offs >= 0; offs--)
		{
			int color = (READ_WORD(&macross_txvideoram[offs*2]) >> 12);
			memset(&palette_used_colors[512 + 16 * color],PALETTE_COLOR_USED,16);
		}
	
		for (offs = 0; offs < 256*16; offs += 16)
		{
			if (READ_WORD(&macross_spriteram[offs]) != 0)
				memset(&palette_used_colors[256 + 16*READ_WORD(&macross_spriteram[offs+14])],PALETTE_COLOR_USED,16);
		}
	
		if (palette_recalc())
		{
			memset(dirtybuffer, 1, macross_txvideoram_size/2);
		}
	
		for (offs = (macross_txvideoram_size/2)-1; offs >= 0; offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx = offs / 32;
				int sy = offs % 32;
	
				int tilecode = READ_WORD(&macross_txvideoram[offs*2]);
	
				if (flipscreen != 0)
				{
					sx = 47-sx;
					sy = 31-sy;
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						tilecode & 0xfff,
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
			if (READ_WORD(&macross_spriteram[offs]) != 0)
			{
				int sx = (READ_WORD(&macross_spriteram[offs+8]) & 0x1ff);
				int sy = (READ_WORD(&macross_spriteram[offs+12]) & 0x1ff);
				int tilecode = READ_WORD(&macross_spriteram[offs+6]);
				int xx = (READ_WORD(&macross_spriteram[offs+2]) & 0x0f) + 1;
				int yy = (READ_WORD(&macross_spriteram[offs+2]) >> 4) + 1;
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
								tilecode & 0x3fff,
								READ_WORD(&macross_spriteram[offs+14]),
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
