/***************************************************************************

  vidhrdw/zerozone.c

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.videoram_size;

public class zerozone
{
	
	public static UBytePtr zerozone_videoram=new UBytePtr();
	
	public static UBytePtr video_dirty=new UBytePtr();
	
	
	
	public static ReadHandlerPtr zerozone_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return zerozone_videoram.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr zerozone_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = zerozone_videoram.READ_WORD (offset);
		int newword = COMBINE_WORD (oldword, data);
	
		if (oldword != newword)
		{
			zerozone_videoram.WRITE_WORD (offset, newword);
			video_dirty.write(offset / 2, 1);
		}
	} };
	
	
	
	/* free the palette dirty array */
	public static VhStopPtr zerozone_vh_stop = new VhStopPtr() { public void handler() 
	{
		video_dirty = null;
	} };
	
	/* claim a palette dirty array */
	public static VhStartPtr zerozone_vh_start = new VhStartPtr() { public int handler() 
	{
		video_dirty = new UBytePtr(videoram_size[0]/2);
	
		if (video_dirty==null)
		{
			zerozone_vh_stop.handler();
			return 1;
		}
	
		memset(video_dirty,1,videoram_size[0]/2);
	
		return 0;
	} };
	
	static void zerozone_update_palette ()
	{
		int[] palette_map=new int[16]; /* range of color table is 0-15 */
		int i;
	
		memset (palette_map, 0, palette_map.length);
	
		/* Find colors used in the background tile plane */
		for (i = 0; i < videoram_size[0]; i += 2)
		{
			int tile, color;
	
			tile = zerozone_videoram.READ_WORD (i) & 0xfff;
			color = (zerozone_videoram.READ_WORD (i) & 0xf000) >> 12;
	
			palette_map[color] |= Machine.gfx[0].pen_usage[tile];
		}
	
		/* Now tell the palette system about those colors */
		for (i = 0;i < 16;i++)
		{
			int usage = palette_map[i];
			int j;
	
			if (usage != 0)
			{
				palette_used_colors.write(i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
				for (j = 1; j < 16; j++)
					if ((palette_map[i] & (1 << j)) != 0)
						palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
					else
						palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
			}
			else
				memset(new UBytePtr(palette_used_colors, i * 16),PALETTE_COLOR_UNUSED,16);
		}
	
		if (palette_recalc() != null)
			memset(video_dirty,1,videoram_size[0]/2);
	
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
	
			tile = zerozone_videoram.READ_WORD (offs) & 0xfff;
			color = (zerozone_videoram.READ_WORD (offs) & 0xf000) >> 12;
	
			if (video_dirty.read(offs/2) != 0)
			{
				int sx,sy;
	
	
				video_dirty.write(offs/2, 0);
	
				sx = (offs/2) / 32;
				sy = (offs/2) % 32;
	
				drawgfx(bitmap,Machine.gfx[0],
					tile,
					color,
					0,0,
					8*sx,8*sy,
					null,TRANSPARENCY_NONE,0);
			}
		}
	} };
}
