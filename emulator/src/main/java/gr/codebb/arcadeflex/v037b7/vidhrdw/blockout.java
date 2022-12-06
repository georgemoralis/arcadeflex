/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_alloc;

public class blockout
{
	
	
	
	public static UBytePtr blockout_videoram = new UBytePtr();
	public static UBytePtr blockout_frontvideoram = new UBytePtr();
	
	
	
	static void setcolor(int color,int rgb)
	{
		int bit0,bit1,bit2,bit3;
		int r,g,b;
	
	
		/* red component */
		bit0 = (rgb >> 0) & 0x01;
		bit1 = (rgb >> 1) & 0x01;
		bit2 = (rgb >> 2) & 0x01;
		bit3 = (rgb >> 3) & 0x01;
		r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		/* green component */
		bit0 = (rgb >> 4) & 0x01;
		bit1 = (rgb >> 5) & 0x01;
		bit2 = (rgb >> 6) & 0x01;
		bit3 = (rgb >> 7) & 0x01;
		g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		/* blue component */
		bit0 = (rgb >> 8) & 0x01;
		bit1 = (rgb >> 9) & 0x01;
		bit2 = (rgb >> 10) & 0x01;
		bit3 = (rgb >> 11) & 0x01;
		b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		palette_change_color(color,r,g,b);
	}
	
	public static WriteHandlerPtr blockout_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = paletteram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
	
		paletteram.WRITE_WORD(offset,newword);
	
		setcolor(offset / 2,newword);
	} };
	
	public static WriteHandlerPtr blockout_frontcolor_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		setcolor(512,data);
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr blockout_vh_start = new VhStartPtr() { public int handler() 
	{
		/* Allocate temporary bitmaps */
		if ((tmpbitmap = bitmap_alloc(Machine.drv.screen_width,Machine.drv.screen_height)) == null)
			return 1;
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr blockout_vh_stop = new VhStopPtr() { public void handler() 
	{
		tmpbitmap = null;
	} };
	
	
	
	static void updatepixels(int x,int y)
	{
		int front,back;
		int color;
	
	
		if (x < Machine.visible_area.min_x ||
				x > Machine.visible_area.max_x ||
				y < Machine.visible_area.min_y ||
				y > Machine.visible_area.max_y)
			return;
	
		front = blockout_videoram.READ_WORD(y*512+x);
		back = blockout_videoram.READ_WORD(0x20000 + y*512+x);
	
		if ((front>>8)!=0) color = front>>8;
		else color = (back>>8) + 256;
		plot_pixel.handler(tmpbitmap, x, y, Machine.pens[color]);
	
		if ((front & 0xff) != 0) color = front&0xff;
		else color = (back&0xff) + 256;
		plot_pixel.handler(tmpbitmap, x+1, y, Machine.pens[color]);
	}
	
	
	
	public static WriteHandlerPtr blockout_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = blockout_videoram.READ_WORD(offset);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			blockout_videoram.WRITE_WORD(offset,newword);
			updatepixels(offset % 512,(offset / 512) % 256);
		}
	} };
	
	public static ReadHandlerPtr blockout_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return blockout_videoram.READ_WORD(offset);
	} };
	
	
	
	public static WriteHandlerPtr blockout_frontvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(blockout_frontvideoram,offset,data);
	} };
	
	public static ReadHandlerPtr blockout_frontvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return blockout_frontvideoram.READ_WORD(offset);
	} };
	
	
	
	public static VhUpdatePtr blockout_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (palette_recalc() != null)
		{
			/* if we ran out of palette entries, rebuild the whole screen */
			int x,y;
	
	
			for (y = 0;y < 256;y++)
			{
				for (x = 0;x < 320;x+=2)
				{
					updatepixels(x,y);
				}
			}
		}
	
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.visible_area,TRANSPARENCY_NONE,0);
	
		{
			int x,y,color;
	
	
			color = Machine.pens[512];
	
			for (y = 0;y < 256;y++)
			{
				for (x = 0;x < 320;x+=8)
				{
					int d;
	
	
					d = blockout_frontvideoram.READ_WORD(y*128+(x/4));
	
					if (d != 0)
					{
						if ((d & 0x80) != 0) plot_pixel.handler(bitmap, x  , y, color);
						if ((d & 0x40) != 0) plot_pixel.handler(bitmap, x+1, y, color);
						if ((d & 0x20) != 0) plot_pixel.handler(bitmap, x+2, y, color);
						if ((d & 0x10) != 0) plot_pixel.handler(bitmap, x+3, y, color);
						if ((d & 0x08) != 0) plot_pixel.handler(bitmap, x+4, y, color);
						if ((d & 0x04) != 0) plot_pixel.handler(bitmap, x+5, y, color);
						if ((d & 0x02) != 0) plot_pixel.handler(bitmap, x+6, y, color);
						if ((d & 0x01) != 0) plot_pixel.handler(bitmap, x+7, y, color);
					}
				}
			}
		}
	} };
}
