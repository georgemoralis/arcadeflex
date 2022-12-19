/***************************************************************************

  vidhrdw/zerozone.c

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class zerozone
{
	
	UBytePtr zerozone_videoram;
	
	extern size_t videoram_size;
	static UBytePtr video_dirty;
	
	
	
	public static ReadHandlerPtr zerozone_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return READ_WORD(&zerozone_videoram[offset]);
	} };
	
	public static WriteHandlerPtr zerozone_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD (&zerozone_videoram[offset]);
		int newword = COMBINE_WORD (oldword, data);
	
		if (oldword != newword)
		{
			WRITE_WORD (&zerozone_videoram[offset],newword);
			video_dirty[offset / 2] = 1;
		}
	} };
	
	
	
	/* free the palette dirty array */
	public static VhStopPtr zerozone_vh_stop = new VhStopPtr() { public void handler() 
	{
		free (video_dirty);
	} };
	
	/* claim a palette dirty array */
	public static VhStartPtr zerozone_vh_start = new VhStartPtr() { public int handler() 
	{
		video_dirty = malloc (videoram_size[0]/2);
	
		if (!video_dirty)
		{
			zerozone_vh_stop();
			return 1;
		}
	
		memset(video_dirty,1,videoram_size[0]/2);
	
		return 0;
	} };
	
	void zerozone_update_palette (void)
	{
		unsigned short palette_map[16]; /* range of color table is 0-15 */
		int i;
	
		memset (palette_map, 0, sizeof (palette_map));
	
		/* Find colors used in the background tile plane */
		for (i = 0; i < videoram_size; i += 2)
		{
			int tile, color;
	
			tile = READ_WORD (&zerozone_videoram[i]) & 0xfff;
			color = (READ_WORD (&zerozone_videoram[i]) & 0xf000) >> 12;
	
			palette_map[color] |= Machine.gfx[0].pen_usage[tile];
		}
	
		/* Now tell the palette system about those colors */
		for (i = 0;i < 16;i++)
		{
			int usage = palette_map[i];
			int j;
	
			if (usage != 0)
			{
				palette_used_colors[i * 16 + 0] = PALETTE_COLOR_TRANSPARENT;
				for (j = 1; j < 16; j++)
					if (palette_map[i] & (1 << j))
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
					else
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_UNUSED;
			}
			else
				memset(&palette_used_colors[i * 16],PALETTE_COLOR_UNUSED,16);
		}
	
		if (palette_recalc ())
			memset(video_dirty,1,videoram_size/2);
	
	}
	
	public static VhUpdatePtr zerozone_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
		zerozone_update_palette ();
	
		if (full_refresh != 0)
			memset(video_dirty,1,videoram_size[0]/2);
	
		/* Do the background first */
		for (offs = 0;offs < videoram_size[0];offs += 2)
		{
			int tile, color;
	
			tile = READ_WORD (&zerozone_videoram[offs]) & 0xfff;
			color = (READ_WORD (&zerozone_videoram[offs]) & 0xf000) >> 12;
	
			if (video_dirty[offs/2])
			{
				int sx,sy;
	
	
				video_dirty[offs/2] = 0;
	
				sx = (offs/2) / 32;
				sy = (offs/2) % 32;
	
				drawgfx(bitmap,Machine.gfx[0],
					tile,
					color,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
			}
		}
	} };
}
