/***************************************************************************

  vidhrdw/superman.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class superman
{
	
	size_t supes_videoram_size;
	size_t supes_attribram_size;
	
	UBytePtr supes_videoram;
	UBytePtr supes_attribram;
	//static UBytePtr dirtybuffer;		/* foreground */
	//static UBytePtr dirtybuffer2;		/* background */
	
	public static VhStartPtr superman_vh_start = new VhStartPtr() { public int handler() 
	{
		return 0;
	} };
	
	public static VhStopPtr superman_vh_stop = new VhStopPtr() { public void handler() 
	{
	} };
	
	/*************************************
	 *
	 *		Foreground RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr supes_attribram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	   int oldword = READ_WORD (&supes_attribram[offset]);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			WRITE_WORD (&supes_attribram[offset], data);
	//		dirtybuffer2[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr supes_attribram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD (&supes_attribram[offset]);
	} };
	
	
	
	/*************************************
	 *
	 *		Background RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr supes_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	   int oldword = READ_WORD (&supes_videoram[offset]);
	   int newword = COMBINE_WORD (oldword, data);
	
	   if (oldword != newword)
	   {
			WRITE_WORD (&supes_videoram[offset], data);
	//		dirtybuffer[offset/2] = 1;
	   }
	} };
	
	public static ReadHandlerPtr supes_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD (&supes_videoram[offset]);
	} };
	
	
	void superman_update_palette (void)
	{
		unsigned short palette_map[32]; /* range of color table is 0-31 */
		int i;
	
		memset (palette_map, 0, sizeof (palette_map));
	
		/* Find colors used in the background tile plane */
		for (i = 0; i < 0x400; i += 0x40)
		{
			int i2;
	
			for (i2 = i; i2 < (i + 0x40); i2 += 2)
			{
				int tile;
				int color;
	
				color = 0;
	
				tile = READ_WORD (&supes_videoram[0x800 + i2]) & 0x3fff;
				if (tile != 0)
					color = READ_WORD (&supes_videoram[0xc00 + i2]) >> 11;
	
				palette_map[color] |= Machine.gfx[0].pen_usage[tile];
	
			}
		}
	
		/* Find colors used in the sprite plane */
		for (i = 0x3fe; i >= 0; i -= 2)
		{
			int tile;
			int color;
	
			color = 0;
	
			tile = READ_WORD (&supes_videoram[i]) & 0x3fff;
			if (tile != 0)
				color = READ_WORD (&supes_videoram[0x400 + i]) >> 11;
	
			palette_map[color] |= Machine.gfx[0].pen_usage[tile];
		}
	
		/* Now tell the palette system about those colors */
		for (i = 0;i < 32;i++)
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
	
		palette_recalc ();
	
	}
	
	public static VhUpdatePtr superman_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		superman_update_palette ();
	
		osd_clearbitmap(bitmap);
	
		/* Refresh the background tile plane */
		for (i = 0; i < 0x400; i += 0x40)
		{
			int x1, y1;
			int i2;
	
			x1 = READ_WORD (&supes_attribram[0x408 + (i >> 1)]);
			y1 = READ_WORD (&supes_attribram[0x400 + (i >> 1)]);
	
			for (i2 = i; i2 < (i + 0x40); i2 += 2)
			{
				int tile;
	
				tile = READ_WORD (&supes_videoram[0x800 + i2]) & 0x3fff;
				if (tile != 0)
				{
					int x, y;
	
					x = (        x1 + ((i2 & 0x03) << 3))  & 0x1ff;
					y = ((265 - (y1 - ((i2 & 0x3c) << 2))) &  0xff);
	
	//				if ((x > 0) && (y > 0) && (x < 388) && (y < 272))
					{
						int flipx = READ_WORD (&supes_videoram[0x800 + i2]) & 0x4000;
						int flipy = READ_WORD (&supes_videoram[0x800 + i2]) & 0x8000;
						int color = READ_WORD (&supes_videoram[0xc00 + i2]) >> 11;
	
						/* Some tiles are transparent, e.g. the gate, so we use TRANSPARENCY_PEN */
						drawgfx(bitmap,Machine.gfx[0],
							tile,
							color,
							flipx,flipy,
							x,y,
							&Machine.visible_area,
							TRANSPARENCY_PEN,0);
					}
				}
			}
		}
	
		/* Refresh the sprite plane */
		for (i = 0x3fe; i >= 0; i -= 2)
		{
			int sprite;
	
			sprite = READ_WORD (&supes_videoram[i]) & 0x3fff;
			if (sprite != 0)
			{
				int x, y;
	
				x = (      READ_WORD (&supes_videoram [0x400 + i]))  & 0x1ff;
				y = (250 - READ_WORD (&supes_attribram[i        ]))  & 0xff;
	
	//			if ((x > 0) && (y > 0) && (x < 388) && (y < 272))
				{
					int flipy = READ_WORD (&supes_videoram[i]) & 0x4000;
					int flipx = READ_WORD (&supes_videoram[i]) & 0x8000;
					int color = READ_WORD (&supes_videoram[0x400 + i]) >> 11;
	
					drawgfx(bitmap,Machine.gfx[0],
						sprite,
						color,
						flipx,flipy,
						x,y,
						&Machine.visible_area,
						TRANSPARENCY_PEN,0);
				}
			}
		}
	} };
}
