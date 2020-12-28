/***************************************************************************

  vidhrdw.c

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;

public class nemesis
{
	
	public static UBytePtr nemesis_videoram1=new UBytePtr();
	public static UBytePtr nemesis_videoram2=new UBytePtr();
	
	public static UBytePtr nemesis_characterram=new UBytePtr();
	public static UBytePtr nemesis_characterram_gfx=new UBytePtr();
	public static int[] nemesis_characterram_size=new int[1];
	public static UBytePtr nemesis_xscroll1=new UBytePtr();
        public static UBytePtr nemesis_xscroll2=new UBytePtr();
        public static UBytePtr nemesis_yscroll=new UBytePtr();
	public static UBytePtr nemesis_yscroll1=new UBytePtr();
        public static UBytePtr nemesis_yscroll2=new UBytePtr();
	
	static osd_bitmap tmpbitmap2;
	static osd_bitmap tmpbitmap3;
	static osd_bitmap tmpbitmap4;
	
	static char[] video1_dirty;	/* 0x800 chars - foreground */
	static char[] video2_dirty;	/* 0x800 chars - background */
	static char[] char_dirty;	/* 2048 chars */
	static char[] sprite_dirty;	/* 512 sprites */
	static char[] sprite3216_dirty;	/* 256 sprites */
	static char[] sprite816_dirty;	/* 1024 sprites */
	static char[] sprite1632_dirty;	/* 256 sprites */
	static char[] sprite3232_dirty;	/* 128 sprites */
	static char[] sprite168_dirty;	/* 1024 sprites */
	static char[] sprite6464_dirty;	/* 32 sprites */
	
	public static WriteHandlerPtr nemesis_palette_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int r,g,b,bit1,bit2,bit3,bit4,bit5;
	
		COMBINE_WORD_MEM(paletteram,offset,data);
		data = paletteram.READ_WORD(offset);
	
		/* Mish, 30/11/99 - Schematics show the resistor values are:
			300 Ohms
			620 Ohms
			1200 Ohms
			2400 Ohms
			4700 Ohms
	
			So the correct weights per bit are 8, 17, 33, 67, 130
		*/

	
		bit1=(data >>  0)&1;
		bit2=(data >>  1)&1;
		bit3=(data >>  2)&1;
		bit4=(data >>  3)&1;
		bit5=(data >>  4)&1;
		r = 8 * bit1 + 17 * bit2 + 33 * bit3 + 67 * bit4 + 130 * bit5;
		r = (int)(Math.pow (r/255.0, 2)*255);
		bit1=(data >>  5)&1;
		bit2=(data >>  6)&1;
		bit3=(data >>  7)&1;
		bit4=(data >>  8)&1;
		bit5=(data >>  9)&1;
		g = 8 * bit1 + 17 * bit2 + 33 * bit3 + 67 * bit4 + 130 * bit5;
		g = (int)(Math.pow (g/255.0, 2)*255);
		bit1=(data >>  10)&1;
		bit2=(data >>  11)&1;
		bit3=(data >>  12)&1;
		bit4=(data >>  13)&1;
		bit5=(data >>  14)&1;
		b = 8 * bit1 + 17 * bit2 + 33 * bit3 + 67 * bit4 + 130 * bit5;
		b = (int)(Math.pow (b/255.0, 2)*255);
	
		palette_change_color(offset / 2,r,g,b);
	} };
	
	public static WriteHandlerPtr salamander_palette_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int r,g,b;
	
		COMBINE_WORD_MEM(paletteram,offset,data);
		if ((offset%4)!=0) offset-=2;
	
		data = ((paletteram.READ_WORD(offset) << 8) | paletteram.READ_WORD(offset+2))&0xffff;
	
		r = (data >>  0) & 0x1f;
		g = (data >>  5) & 0x1f;
		b = (data >> 10) & 0x1f;
	
		r = (r << 3) | (r >> 2);
		g = (g << 3) | (g >> 2);
		b = (b << 3) | (b >> 2);
	
		palette_change_color(offset / 4,r,g,b);
	} };
	
	public static ReadHandlerPtr nemesis_videoram1_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return nemesis_videoram1.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr nemesis_videoram1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(nemesis_videoram1,offset,data);
		if (offset < 0x1000)
			video1_dirty[offset / 2] = 1;
		else
			video2_dirty[(offset - 0x1000) / 2] = 1;
	} };
	
	public static ReadHandlerPtr nemesis_videoram2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return nemesis_videoram2.READ_WORD(offset);
	} };
	
	public static WriteHandlerPtr nemesis_videoram2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		COMBINE_WORD_MEM(nemesis_videoram2,offset,data);
		if (offset < 0x1000)
			video1_dirty[offset / 2] = 1;
		else
			video2_dirty[(offset - 0x1000) / 2] = 1;
	} };
	
	
	public static ReadHandlerPtr gx400_xscroll1_r = new ReadHandlerPtr() { public int handler(int offset){ return nemesis_xscroll1.READ_WORD(offset);} };
	public static ReadHandlerPtr gx400_xscroll2_r = new ReadHandlerPtr() { public int handler(int offset){ return nemesis_xscroll2.READ_WORD(offset);} };
	public static ReadHandlerPtr gx400_yscroll_r = new ReadHandlerPtr() { public int handler(int offset){ return nemesis_yscroll.READ_WORD(offset);} };
	
	public static WriteHandlerPtr gx400_xscroll1_w = new WriteHandlerPtr() { public void handler(int offset, int data){ COMBINE_WORD_MEM(nemesis_xscroll1,offset,data);} };
	public static WriteHandlerPtr gx400_xscroll2_w = new WriteHandlerPtr() { public void handler(int offset, int data){ COMBINE_WORD_MEM(nemesis_xscroll2,offset,data);} };
	public static WriteHandlerPtr gx400_yscroll_w = new WriteHandlerPtr() { public void handler(int offset, int data){ COMBINE_WORD_MEM(nemesis_yscroll,offset,data);} };
	
	
	/* we have to straighten out the 16-bit word into bytes for gfxdecode() to work */
	public static ReadHandlerPtr nemesis_characterram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		int res;
	
		res = nemesis_characterram_gfx.READ_WORD(offset);
	
		//#ifdef LSB_FIRST
		res = ((res & 0x00ff) << 8) | ((res & 0xff00) >> 8);
		//#endif
	
		return res;
	} };
	
	public static WriteHandlerPtr nemesis_characterram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		int oldword = nemesis_characterram_gfx.READ_WORD(offset);
		int newword;
	
		COMBINE_WORD_MEM(nemesis_characterram,offset,data);	/* this is need so that twinbee can run code in the
																	character RAM */
	
		//#ifdef LSB_FIRST
		data = ((data & 0x00ff00ff) << 8) | ((data & 0xff00ff00) >> 8);
		//#endif
	
		newword = COMBINE_WORD(oldword,data);
		if (oldword != newword)
		{
			nemesis_characterram_gfx.WRITE_WORD(offset,newword);
	
			char_dirty[offset / 32] = 1;
			sprite_dirty[offset / 128] = 1;
			sprite3216_dirty[offset / 256] = 1;
			sprite1632_dirty[offset / 256] = 1;
			sprite3232_dirty[offset / 512] = 1;
			sprite168_dirty[offset / 64] = 1;
			sprite816_dirty[offset / 64] = 1;
			sprite6464_dirty[offset / 2048] = 1;
		}
	} };
	
	
	
	/* free the palette dirty array */
	public static VhStopPtr nemesis_vh_stop = new VhStopPtr() { public void handler() 
	{
		osd_free_bitmap(tmpbitmap);
		osd_free_bitmap(tmpbitmap2);
		osd_free_bitmap(tmpbitmap3);
		osd_free_bitmap(tmpbitmap4);
		tmpbitmap=null;
		char_dirty=null;
		sprite_dirty=null;
		sprite3216_dirty=null;
		sprite1632_dirty=null;
		sprite3232_dirty=null;
		sprite168_dirty=null;
		sprite816_dirty=null;
		sprite6464_dirty=null;
		char_dirty = null;
		video1_dirty=null;
		video2_dirty=null;
		nemesis_characterram_gfx=null;
	} };
	
	/* claim a palette dirty array */
	public static VhStartPtr nemesis_vh_start = new VhStartPtr() { public int handler() 
	{
		if ((tmpbitmap = osd_new_bitmap(2 * Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
	
		if ((tmpbitmap2 = osd_new_bitmap(2 * Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
	
		if ((tmpbitmap3 = osd_new_bitmap(2 * Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
	
		if ((tmpbitmap4 = osd_new_bitmap(2 * Machine.drv.screen_width,Machine.drv.screen_height,Machine.scrbitmap.depth)) == null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
	
		char_dirty = new char[2048];
		if (char_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(char_dirty,1,2048);
	
		sprite_dirty = new char[512];
		if (sprite_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite_dirty,1,512);
	
		sprite3216_dirty = new char[256];
		if (sprite3216_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite3216_dirty,1,256);
	
		sprite1632_dirty = new char[256];
		if (sprite1632_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite1632_dirty,1,256);
	
		sprite3232_dirty = new char[128];
		if (sprite3232_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite3232_dirty,1,128);
	
		sprite168_dirty = new char[1024];
		if (sprite168_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite168_dirty,1,1024);
	
		sprite816_dirty = new char[1024];
		if (sprite816_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite816_dirty,1,32);
	
		sprite6464_dirty = new char[32];
		if (sprite6464_dirty==null) {
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(sprite6464_dirty,1,32);
	
		video1_dirty = new char[0x800];
		video2_dirty = new char[0x800];
		if (video1_dirty==null || video2_dirty==null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(video1_dirty,1,0x800);
		memset(video2_dirty,1,0x800);
	
		nemesis_characterram_gfx = new UBytePtr(nemesis_characterram_size[0]);
		if(nemesis_characterram_gfx==null)
		{
			nemesis_vh_stop.handler();
			return 1;
		}
		memset(nemesis_characterram_gfx,0,nemesis_characterram_size[0]);
	
		return 0;
	} };
	
	
	/* This is a bit slow, but it works. I'll speed it up later */
	static void nemesis_drawgfx_zoomup(osd_bitmap dest,GfxElement gfx,
			 int code, int color,int flipx,int flipy,int sx,int sy,
			 rectangle clip,int transparency,int transparent_color,int scale)
	{
		int ex,ey,y,start,dy;
		UBytePtr sd;
		UBytePtr bm;
		int col;
		rectangle myclip=new rectangle();
	
		int dda_x=0;
		int dda_y=0;
		int ex_count;
		int ey_count;
		int real_x;
		int ysize;
		int xsize;
		UShortArray paldata;	/* ASG 980209 */
		int transmask;
	
		if (gfx==null) return;
	
		code %= gfx.total_elements;
		color %= gfx.total_colors;
	
		transmask = 1 << transparent_color;
	
		if ((gfx.pen_usage[code] & ~transmask) == 0)
			/* character is totally transparent, no need to draw */
			return;
	
	
		if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
		{
			int temp;
	
			temp = sx;
			sx = sy;
			sy = temp;
	
			temp = flipx;
			flipx = flipy;
			flipy = temp;
	
			if (clip != null)
			{
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_x;
				myclip.min_x = clip.min_y;
				myclip.min_y = temp;
				temp = clip.max_x;
				myclip.max_x = clip.max_y;
				myclip.max_y = temp;
				clip = myclip;
			}
		}
		if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
		{
			sx = dest.width - gfx.width - sx;
	
			if (clip != null)
			{
				int temp;
	
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_x;
				myclip.min_x = dest.width-1 - clip.max_x;
				myclip.max_x = dest.width-1 - temp;
				myclip.min_y = clip.min_y;
				myclip.max_y = clip.max_y;
				clip = myclip;
			}
		}
		if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
		{
			sy = dest.height - gfx.height - sy;
	
			if (clip != null)
			{
				int temp;
	
				myclip.min_x = clip.min_x;
				myclip.max_x = clip.max_x;
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_y;
				myclip.min_y = dest.height-1 - clip.max_y;
				myclip.max_y = dest.height-1 - temp;
				clip = myclip;
			}
		}
	
	
		/* check bounds */
		xsize=gfx.width;
		ysize=gfx.height;
		/* Clipping currently done in code loop */
		ex = sx + xsize -1;
		ey = sy + ysize -1;
	/*	if (ex >= dest.width) ex = dest.width-1;
		if (clip && ex > clip.max_x) ex = clip.max_x;
		if (sx > ex) return;
		if (ey >= dest.height) tey = dest.height-1;
		if (clip && ey > clip.max_y) ey = clip.max_y;
		if (sy > ey) return;
	*/
		/* start = code * gfx.height; */
		if (flipy != 0)	/* Y flip */
		{
			start = code * gfx.height + gfx.height-1;
			dy = -1;
		}
		else		/* normal */
		{
			start = code * gfx.height;
			dy = 1;
		}
	
	
	
		paldata = new UShortArray(gfx.colortable,gfx.color_granularity * color);
	
		if (flipx != 0)	/* X flip */
		{
			if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
				y=sy + ysize -1;
			else
				y=sy;
			dda_y=0x80;
			ey_count=sy;
			do
			{
				if(y>=clip.min_y && y<=clip.max_y)
				{
					if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
					{
						bm  = new UBytePtr(dest.line[y],sx + xsize -1);
						real_x=sx + xsize -1;
					} else {
						bm  = new UBytePtr(dest.line[y],sx);
						real_x=sx;
					}
					sd = new UBytePtr(gfx.gfxdata , start * gfx.line_modulo + xsize -1);
					dda_x=0x80;
					ex_count=sx;
					col = sd.read();
					do
					{
						if ((real_x<=clip.max_x) && (real_x>=clip.min_x))
							if (col != transparent_color) bm.write(paldata.read(col));
						if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
						{
							bm.offset--;
							real_x--;
						} else {
							bm.offset++;
							real_x++;
						}
						dda_x-=scale;
						if(dda_x<=0)
						{
							dda_x+=0x80;
							sd.offset--;
							ex_count++;
							col = sd.read();
						}
					} while(ex_count <= ex);
				}
				if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
					y--;
				else
					y++;
				dda_y-=scale;
				if(dda_y<=0)
				{
					dda_y+=0x80;
					start+=dy;
					ey_count++;
				}
	
			} while(ey_count <= ey);
		}
		else		/* normal */
		{
			if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
				y=sy + ysize -1;
			else
				y=sy;
			dda_y=0x80;
			ey_count=sy;
			do
			{
				if(y>=clip.min_y && y<=clip.max_y)
				{
					if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
					{
						bm  = new UBytePtr(dest.line[y],sx + xsize -1);
						real_x=sx + xsize -1;
					} else {
						bm  = new UBytePtr(dest.line[y],sx);
						real_x=sx;
					}
					sd = new UBytePtr(gfx.gfxdata, start * gfx.line_modulo);
					dda_x=0x80;
					ex_count=sx;
					col = sd.read();
					do
					{
						if ((real_x<=clip.max_x) && (real_x>=clip.min_x))
							if (col != transparent_color) bm.write(paldata.read(col));
						if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
						{
							bm.offset--;
							real_x--;
						} else {
							bm.offset++;
							real_x++;
						}
						dda_x-=scale;
						if(dda_x<=0)
						{
							dda_x+=0x80;
							sd.offset++;
							ex_count++;
							col = sd.read();
						}
					} while(ex_count <= ex);
				}
				if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
					y--;
				else
					y++;
				dda_y-=scale;
				if(dda_y<=0)
				{
					dda_y+=0x80;
					start+=dy;
					ey_count++;
				}
	
			} while(ey_count <= ey);
		}
	}
	
	/* This is a bit slow, but it works. I'll speed it up later */
	static void nemesis_drawgfx_zoomdown(osd_bitmap dest,GfxElement gfx,
			 int code, int color,int flipx,int flipy,int sx,int sy,
			rectangle clip,int transparency,int transparent_color,int scale)
	{
		int ex,ey,y,start,dy;
		UBytePtr sd;
		UBytePtr bm;
		int col;
		rectangle myclip=new rectangle();
	
		int dda_x=0;
		int dda_y=0;
		int ex_count;
		int ey_count;
		int real_x;
		int ysize;
		int xsize;
		int transmask;
		UShortArray paldata;	/* ASG 980209 */
	
		if (gfx==null) return;
	
		code %= gfx.total_elements;
		color %= gfx.total_colors;
	
		transmask = 1 << transparent_color;
		if ((gfx.pen_usage[code] & ~transmask) == 0)
			/* character is totally transparent, no need to draw */
			return;
	
	
		if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
		{
			int temp;
	
			temp = sx;
			sx = sy;
			sy = temp;
	
			temp = flipx;
			flipx = flipy;
			flipy = temp;
	
			if (clip != null)
			{
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_x;
				myclip.min_x = clip.min_y;
				myclip.min_y = temp;
				temp = clip.max_x;
				myclip.max_x = clip.max_y;
				myclip.max_y = temp;
				clip = myclip;
			}
		}
		if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
		{
			sx = dest.width - gfx.width - sx;
	
			if (clip != null)
			{
				int temp;
	
	
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_x;
				myclip.min_x = dest.width-1 - clip.max_x;
				myclip.max_x = dest.width-1 - temp;
				myclip.min_y = clip.min_y;
				myclip.max_y = clip.max_y;
				clip = myclip;
			}
		}
		if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
		{
			sy = dest.height - gfx.height - sy;
	
			if (clip != null)
			{
				int temp;
	
				myclip.min_x = clip.min_x;
				myclip.max_x = clip.max_x;
				/* clip and myclip might be the same, so we need a temporary storage */
				temp = clip.min_y;
				myclip.min_y = dest.height-1 - clip.max_y;
				myclip.max_y = dest.height-1 - temp;
				clip = myclip;
			}
		}
	
	
		/* check bounds */
		xsize=gfx.width;
		ysize=gfx.height;
		ex = sx + xsize -1;
		if (ex >= dest.width) ex = dest.width-1;
		if (clip!=null && ex > clip.max_x) ex = clip.max_x;
		if (sx > ex) return;
		ey = sy + ysize -1;
		if (ey >= dest.height) ey = dest.height-1;
		if (clip!=null && ey > clip.max_y) ey = clip.max_y;
		if (sy > ey) return;
	
		/* start = code * gfx.height; */
		if (flipy != 0)	/* Y flip */
		{
			start = code * gfx.height + gfx.height-1;
			dy = -1;
		}
		else		/* normal */
		{
			start = code * gfx.height;
			dy = 1;
		}
	
	
	
		paldata = new UShortArray(gfx.colortable,gfx.color_granularity * color);
	
		if (flipx != 0)	/* X flip */
		{
			if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
				y=sy + ysize -1;
			else
				y=sy;
			dda_y=0-scale/2;
			for(ey_count=0;ey_count<ysize;ey_count++)
			{
				if(dda_y<0) dda_y+=0x80;
				if(dda_y>=0)
				{
					dda_y-=scale;
					if(y>=clip.min_y && y<=clip.max_y)
					{
						if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
						{
							bm  = new UBytePtr(dest.line[y],sx + xsize -1);
							real_x=sx + xsize -1;
						} else {
							bm  = new UBytePtr(dest.line[y],sx);
							real_x=sx;
						}
						sd = new UBytePtr(gfx.gfxdata , start * gfx.line_modulo + xsize -1);
						dda_x=0-scale/2;
						for(ex_count=0;ex_count<xsize;ex_count++)
						{
							if(dda_x<0) dda_x+=0x80;
							if(dda_x>=0)
							{
								dda_x-=scale;
								if ((real_x<=clip.max_x) && (real_x>=clip.min_x))
								{
									col = sd.read();
									if (col != transparent_color) bm.write(paldata.read(col));
								}
								if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
								{
									bm.offset--;
									real_x--;
								} else {
									bm.offset++;
									real_x++;
								}
							}
							sd.offset--;
						}
					}
					if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
						y--;
					else
						y++;
				}
				start+=dy;
			}
	
		}
		else		/* normal */
		{
			if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
				y=sy + ysize -1;
			else
				y=sy;
			dda_y=0-scale/2;
			for(ey_count=0;ey_count<ysize;ey_count++)
			{
				if(dda_y<0) dda_y+=0x80;
				if(dda_y>=0)
				{
					dda_y-=scale;
					if(y>=clip.min_y && y<=clip.max_y)
					{
						if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
						{
							bm  = new UBytePtr(dest.line[y],sx + xsize -1);
							real_x=sx + xsize -1;
						} else {
							bm  = new UBytePtr(dest.line[y],sx);
							real_x=sx;
						}
						sd = new UBytePtr(gfx.gfxdata , start * gfx.line_modulo);
						dda_x=0-scale/2;
						for(ex_count=0;ex_count<xsize;ex_count++)
						{
							if(dda_x<0) dda_x+=0x80;
							if(dda_x>=0)
							{
								dda_x-=scale;
								if ((real_x<=clip.max_x) && (real_x>=clip.min_x))
								{
									col = sd.read();
									if (col != transparent_color) bm.write(paldata.read(col));
								}
								if ((Machine.orientation & ORIENTATION_FLIP_X)!=0)
								{
									bm.offset--;
									real_x--;
								} else {
									bm.offset++;
									real_x++;
								}
							}
							sd.offset++;
						}
					}
					if ((Machine.orientation & ORIENTATION_FLIP_Y)!=0)
						y--;
					else
						y++;
				}
				start+=dy;
			}
	
		}
	}
	
	
	static void draw_sprites(osd_bitmap bitmap)
	{
		/*
		 *	16 bytes per sprite, in memory from 56000-56fff
		 *
		 *	byte	0 :	relative priority.
		 *	byte	2 :	size (?) value #E0 means not used., bit 0x01 is flipx
		                0xc0 is upper 2 bits of zoom.
						0x38 is size.
		 * 	byte	4 :	zoom = 0xff
		 *	byte	6 :	low bits sprite code.
		 *	byte	8 :	color + hi bits sprite code., bit 0x20 is flipy bit. bit 0x01 is high bit of X pos.
		 *	byte	A :	X position.
		 *	byte	C :	Y position.
		 * 	byte	E :	not used.
		 */
	
		int adress;	/* start of sprite in spriteram */
		int sx;	/* sprite X-pos */
		int sy;	/* sprite Y-pos */
		int code;	/* start of sprite in obj RAM */
		int color;	/* color of the sprite */
		int flipx,flipy;
		int zoom;
		int char_type;
		int priority;
		int size;
	
		for (priority=0;priority<256;priority++)
		{
			for (adress = 0;adress < spriteram_size[0];adress += 16)
			{
				if(spriteram.READ_WORD(adress)!=priority) continue;
	
				code = spriteram.READ_WORD(adress+6) + ((spriteram.READ_WORD(adress+8) & 0xc0) << 2);
				zoom=spriteram.READ_WORD(adress+4)&0xff;
				if (zoom != 0xFF || code!=0)
				{
					size=spriteram.READ_WORD(adress+2);
					zoom+=(size&0xc0)<<2;
	
					sx = spriteram.READ_WORD(adress+10)&0xff;
					sy = spriteram.READ_WORD(adress+12)&0xff;
					if((spriteram.READ_WORD(adress+8)&1)!=0) sx-=0x100;	/* fixes left side clip */
					color = (spriteram.READ_WORD(adress+8) & 0x1e) >> 1;
					flipx = spriteram.READ_WORD(adress+2) & 0x01;
					flipy = spriteram.READ_WORD(adress+8) & 0x20;
	
					switch(size&0x38)
					{
						case 0x00:	/* sprite 32x32*/
							char_type=4;
							code/=8;
							break;
						case 0x08:	/* sprite 16x32 */
							char_type=5;
							code/=4;
							break;
						case 0x10:	/* sprite 32x16 */
							char_type=2;
							code/=4;
							break;
						case 0x18:		/* sprite 64x64 */
							char_type=7;
							code/=32;
							break;
						case 0x20:	/* char 8x8 */
							char_type=0;
							code*=2;
							break;
						case 0x28:		/* sprite 16x8 */
							char_type=6;
							break;
						case 0x30:	/* sprite 8x16 */
							char_type=3;
							break;
						case 0x38:
						default:	/* sprite 16x16 */
							char_type=1;
							code/=2;
							break;
					}
	
					/*  0x80 == no zoom */
					if(zoom==0x80)
					{
						drawgfx(bitmap,Machine.gfx[char_type],
								code,
								color,
								flipx,flipy,
								sx,sy,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0);
					}
					else if(zoom>=0x80)
					{
						nemesis_drawgfx_zoomdown(bitmap,Machine.gfx[char_type],
								code,
								color,
								flipx,flipy,
								sx,sy,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0,zoom);
					}
					else if(zoom>=0x10)
					{
						nemesis_drawgfx_zoomup(bitmap,Machine.gfx[char_type],
								code,
								color,
								flipx,flipy,
								sx,sy,
								Machine.drv.visible_area,TRANSPARENCY_PEN,0,zoom);
					}
				} /* if sprite */
			} /* for loop */
		} /* priority */
	}
	
	/******************************************************************************/
	
	static void setup_palette()
	{
		int color,code,i;
		int[] colmask=new int[0x80];
		int pal_base,offs;
	
		palette_init_used_colors();
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 0x80;color++) colmask[color] = 0;
		for (offs = 0x1000 - 2;offs >= 0;offs -= 2)
		{
			code = nemesis_videoram1.READ_WORD(offs + 0x1000) & 0x7ff;
			if (char_dirty[code] == 1)
			{
				decodechar(Machine.gfx[0],code,nemesis_characterram_gfx,
						Machine.drv.gfxdecodeinfo[0].gfxlayout);
				char_dirty[code] = 2;
			}
			color = nemesis_videoram2.READ_WORD(offs + 0x1000) & 0x7f;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 0x80;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
	
		pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
		for (color = 0;color < 0x80;color++) colmask[color] = 0;
		for (offs = 0;offs < spriteram_size[0];offs += 16)
		{
			int char_type;
			int zoom=spriteram.READ_WORD(offs+4);
			code = spriteram.READ_WORD(offs+6) + ((spriteram.READ_WORD(offs+8) & 0xc0) << 2);
			if (zoom != 0xFF || code!=0)
			{
				int size=spriteram.READ_WORD(offs+2);
				switch(size&0x38)
				{
					case 0x00:
						/* sprite 32x32*/
						char_type=4;
						code/=8;
						if (sprite3232_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite3232_dirty[code] = 0;
						}
						break;
					case 0x08:
						/* sprite 16x32 */
						char_type=5;
						code/=4;
						if (sprite1632_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite1632_dirty[code] = 0;
	
						}
						break;
					case 0x10:
						/* sprite 32x16 */
						char_type=2;
						code/=4;
						if (sprite3216_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite3216_dirty[code] = 0;
						}
						break;
					case 0x18:
						/* sprite 64x64 */
						char_type=7;
						code/=32;
						if (sprite6464_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite6464_dirty[code] = 0;
						}
						break;
					case 0x20:
						/* char 8x8 */
						char_type=0;
						code*=2;
						if (char_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
							Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							char_dirty[code] = 0;
						}
						break;
					case 0x28:
						/* sprite 16x8 */
						char_type=6;
						if (sprite168_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite168_dirty[code] = 0;
						}
						break;
					case 0x30:
						/* sprite 8x16 */
						char_type=3;
						if (sprite816_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite816_dirty[code] = 0;
						}
						break;
					default:
						if (errorlog != null) fprintf(errorlog,"UN-SUPPORTED SPRITE SIZE %-4x\n",size&0x38);
					case 0x38:
						/* sprite 16x16 */
						char_type=1;
						code/=2;
						if (sprite_dirty[code] == 1)
						{
							decodechar(Machine.gfx[char_type],code,nemesis_characterram_gfx,
									Machine.drv.gfxdecodeinfo[char_type].gfxlayout);
							sprite_dirty[code] = 2;
	
						}
						break;
				}
	
				color = (spriteram.READ_WORD(offs+8) & 0x1e) >> 1;
				colmask[color] |= Machine.gfx[char_type].pen_usage[code];
			}
		}
	
	
		for (color = 0;color < 0x80;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i,PALETTE_COLOR_USED);
			}
		}
	
	
		pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
		for (color = 0;color < 0x80;color++) colmask[color] = 0;
		for (offs = 0x1000 - 2;offs >= 0;offs -= 2)
		{
			code = nemesis_videoram1.READ_WORD(offs) & 0x7ff;
			if (char_dirty[code] == 1)
			{
				decodechar(Machine.gfx[0],code,nemesis_characterram_gfx,
						Machine.drv.gfxdecodeinfo[0].gfxlayout);
				char_dirty[code] = 2;
			}
			color = nemesis_videoram2.READ_WORD(offs) & 0x7f;
			colmask[color] |= Machine.gfx[0].pen_usage[code];
		}
	
		for (color = 0;color < 0x80;color++)
		{
			if ((colmask[color] & (1 << 0))!=0)
				palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
			for (i = 1;i < 16;i++)
			{
				if ((colmask[color] & (1 << i))!=0)
					palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
			}
		}
	
		if (palette_recalc()!=null)
		{
			memset(video1_dirty,1,0x800);
			memset(video2_dirty,1,0x800);
		}
	}
	
	static void setup_backgrounds()
	{
		int offs;
	
		/* Do the foreground first */
		for (offs = 0x1000 - 2;offs >= 0;offs -= 2)
		{
			int code,color;
	
	
			code = nemesis_videoram1.READ_WORD(offs + 0x1000) & 0x7ff;
	
			if (video2_dirty[offs/2]!=0 || char_dirty[code]!=0)
			{
				int sx,sy,flipx,flipy;
	
				color = nemesis_videoram2.READ_WORD(offs + 0x1000) & 0x7f;
	
				video2_dirty[offs/2] = 0;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
				flipx = nemesis_videoram2.READ_WORD(offs + 0x1000) & 0x80;
				flipy =  nemesis_videoram1.READ_WORD(offs + 0x1000) & 0x800;
	
				if(nemesis_videoram1.READ_WORD(offs + 0x1000)!=0 || nemesis_videoram2.READ_WORD(offs + 0x1000)!=0)
				{
					if ((nemesis_videoram1.READ_WORD(offs + 0x1000) & 0x1000)!=0)		//screen priority
					{
						rectangle clip=new rectangle();
	
						drawgfx(tmpbitmap3,Machine.gfx[0],
							code,
							color,
							flipx,flipy,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
	
						clip.min_x=8*sx;
						clip.max_x=8*sx+7;
						clip.min_y=8*sy;
						clip.max_y=8*sy+7;
						fillbitmap(tmpbitmap,palette_transparent_pen,clip);
					} else {
						rectangle clip=new rectangle();
	
						drawgfx(tmpbitmap,Machine.gfx[0],
							code,
							color,
							flipx,flipy,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
	
						clip.min_x=8*sx;
						clip.max_x=8*sx+7;
						clip.min_y=8*sy;
						clip.max_y=8*sy+7;
						fillbitmap(tmpbitmap3,palette_transparent_pen,clip);
					}
				} else {
					rectangle clip=new rectangle();
					clip.min_x=8*sx;
					clip.max_x=8*sx+7;
					clip.min_y=8*sy;
					clip.max_y=8*sy+7;
					fillbitmap(tmpbitmap,palette_transparent_pen,clip);
					fillbitmap(tmpbitmap3,palette_transparent_pen,clip);
				}
			}
		}
	
		/* Background */
		for (offs = 0x1000 - 2;offs >= 0;offs -= 2)
		{
			int code,color;
	
	
			code = nemesis_videoram1.READ_WORD(offs) & 0x7ff;
	
			if (video1_dirty[offs/2]!=0 || char_dirty[code]!=0)
			{
				int sx,sy,flipx,flipy;
	
				video1_dirty[offs/2] = 0;
	
				color = nemesis_videoram2.READ_WORD(offs) & 0x7f;
	
				sx = (offs/2) % 64;
				sy = (offs/2) / 64;
				flipx = nemesis_videoram2.READ_WORD(offs) & 0x80;
				flipy = nemesis_videoram1.READ_WORD(offs) & 0x800;
	
				if(nemesis_videoram1.READ_WORD(offs)!=0 || nemesis_videoram2.READ_WORD(offs)!=0)
				{
					if ((nemesis_videoram1.READ_WORD(offs) & 0x1000)!=0)		//screen priority
					{
						rectangle clip=new rectangle();
	
						drawgfx(tmpbitmap4,Machine.gfx[0],
							code,
							color,
							flipx,flipy,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
	
						clip.min_x=8*sx;
						clip.max_x=8*sx+7;
						clip.min_y=8*sy;
						clip.max_y=8*sy+7;
						fillbitmap(tmpbitmap2,palette_transparent_pen,clip);
					} else {
						rectangle clip=new rectangle();
	
						drawgfx(tmpbitmap2,Machine.gfx[0],
							code,
							color,
							flipx,flipy,
							8*sx,8*sy,
							null,TRANSPARENCY_NONE,0);
	
						clip.min_x=8*sx;
						clip.max_x=8*sx+7;
						clip.min_y=8*sy;
						clip.max_y=8*sy+7;
						fillbitmap(tmpbitmap4,palette_transparent_pen,clip);
					}
				} else {
					rectangle clip=new rectangle();
					clip.min_x=8*sx;
					clip.max_x=8*sx+7;
					clip.min_y=8*sy;
					clip.max_y=8*sy+7;
					fillbitmap(tmpbitmap2,palette_transparent_pen,clip);
					fillbitmap(tmpbitmap4,palette_transparent_pen,clip);
				}
			}
		}
	}
	
	/******************************************************************************/
	
	public static VhUpdatePtr nemesis_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int xscroll[]=new int[256];
                int xscroll2[]=new int[256];
                int yscroll;
	
		setup_palette();
	
		/* Render backgrounds */
		setup_backgrounds();
	
		/* screen flash */
		fillbitmap(bitmap,Machine.pens[paletteram.READ_WORD(0x00) & 0x7ff],Machine.drv.visible_area);
	
		/* Copy the background bitmap */
		yscroll = -(nemesis_yscroll.READ_WORD(0x300) & 0xff);	/* used on nemesis level 2 */
		for (offs = 0;offs < 256;offs++)
		{
			xscroll2[offs] = -((nemesis_xscroll2.READ_WORD(2 * offs) & 0xff) +
					((nemesis_xscroll2.READ_WORD(0x200 + 2 * offs) & 1) << 8));
		}
		copyscrollbitmap(bitmap,tmpbitmap,256,xscroll2,1,new int[]{yscroll},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		/* Do the foreground */
		for (offs = 0;offs < 256;offs++)
		{
			xscroll[offs] = -((nemesis_xscroll1.READ_WORD(2 * offs) & 0xff) +
					((nemesis_xscroll1.READ_WORD(0x200 + 2 * offs) & 1) << 8));
		}
		copyscrollbitmap(bitmap,tmpbitmap2,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		draw_sprites(bitmap);
	
		copyscrollbitmap(bitmap,tmpbitmap3,256,xscroll2,1,new int[]{yscroll},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		copyscrollbitmap(bitmap,tmpbitmap4,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		for (offs = 0; offs < 2048; offs++)
		{
			if (char_dirty[offs] == 2)
				char_dirty[offs] = 0;
		}
	} };
	
	public static VhUpdatePtr twinbee_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		int xscroll[] =new int[256];
                int xscroll2[]=new int[256];
                int yscroll;
	
		setup_palette();
	
		/* Render backgrounds */
		setup_backgrounds();
	
		/* Copy the background bitmap */
		yscroll = -(nemesis_yscroll.READ_WORD(0x300) & 0xff);	/* used on nemesis level 2 */
		for (offs = 0;offs < 256;offs++)
		{
			xscroll2[offs] = -((nemesis_xscroll2.READ_WORD(2 * offs) & 0xff) +
					((nemesis_xscroll2.READ_WORD(0x200 + 2 * offs) & 1) << 8));
		}
		copyscrollbitmap(bitmap,tmpbitmap,256,xscroll2,1,new int[]{yscroll},Machine.drv.visible_area,TRANSPARENCY_NONE,0);
	
		/* Do the foreground */
		for (offs = 0;offs < 256;offs++)
		{
			xscroll[offs] = -((nemesis_xscroll1.READ_WORD(2 * offs) & 0xff) +
					((nemesis_xscroll1.READ_WORD(0x200 + 2 * offs) & 1) << 8));
		}
		copyscrollbitmap(bitmap,tmpbitmap2,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
			Machine.orientation ^= ORIENTATION_FLIP_X;
		else
			Machine.orientation ^= ORIENTATION_FLIP_Y;
	
		draw_sprites(bitmap);
	
		if ((Machine.orientation & ORIENTATION_SWAP_XY)!=0)
			Machine.orientation ^= ORIENTATION_FLIP_X;
		else
			Machine.orientation ^= ORIENTATION_FLIP_Y;
	
		copyscrollbitmap(bitmap,tmpbitmap3,256,xscroll2,1,new int[]{yscroll},Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		copyscrollbitmap(bitmap,tmpbitmap4,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
		for (offs = 0; offs < 2048; offs++)
		{
			if (char_dirty[offs] == 2)
				char_dirty[offs] = 0;
		}
	} };
	
	public static VhUpdatePtr salamand_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs,l;
		int[] xscroll=new int[256];
                int[] yscroll=new int[256];
                int[] xscroll2=new int[256];
                int[] yscroll2=new int[256];
		int culumn_scroll = 0;
	
		setup_palette();
	
		/* Render backgrounds */
		setup_backgrounds();
	
		/* screen flash */
		fillbitmap(bitmap,Machine.pens[paletteram.READ_WORD(0x00) & 0x7ff],Machine.drv.visible_area);
	
		/* Kludge - check if we need row or column scroll */
		if (nemesis_yscroll.READ_WORD(0x780)!=0 || nemesis_yscroll.READ_WORD(0x790)!=0) {
			/* Column scroll */
			culumn_scroll = 1;
			l=0;
			for (offs = 0x800-2;offs >= 0x780; offs-=2)
			{
				yscroll[l] = yscroll[l+64] = -nemesis_yscroll.READ_WORD(offs);
				l++;
			}
			copyscrollbitmap(bitmap,tmpbitmap2,0,null,128,yscroll,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		} else { /* Rowscroll */
			for (offs = 0;offs < 256;offs++)
			{
				xscroll[offs] = -((nemesis_xscroll1.READ_WORD(2 * offs) & 0xff) +
						((nemesis_xscroll1.READ_WORD(0x200 + 2 * offs) & 1) << 8));
			}
			copyscrollbitmap(bitmap,tmpbitmap2,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	
		/* Copy the foreground bitmap */
		if (nemesis_yscroll.READ_WORD(0x700)!=0 || nemesis_yscroll.READ_WORD(0x710)!=0) {
			/* Column scroll */
			l=0;
			for (offs = 0x780-2;offs >= 0x700; offs-=2)
			{
				yscroll2[l] = yscroll2[l+64] = -nemesis_yscroll.READ_WORD(offs);
				l++;
			}
			copyscrollbitmap(bitmap,tmpbitmap,0,null,128,yscroll2,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
			draw_sprites(bitmap);
	
			if (culumn_scroll != 0)
				copyscrollbitmap(bitmap,tmpbitmap4,0,null,128,yscroll,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
			else
				copyscrollbitmap(bitmap,tmpbitmap4,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
			copyscrollbitmap(bitmap,tmpbitmap3,0,null,128,yscroll2,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		} else { /* Rowscroll */
			for (offs = 0;offs < 256;offs++)
			{
				xscroll2[offs] = -((nemesis_xscroll2.READ_WORD(2 * offs) & 0xff) +
						((nemesis_xscroll2.READ_WORD(0x200 + 2 * offs) & 1) << 8));
			}
			copyscrollbitmap(bitmap,tmpbitmap,256,xscroll2,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
			draw_sprites(bitmap);
	
			if (culumn_scroll != 0)
				copyscrollbitmap(bitmap,tmpbitmap4,0,null,128,yscroll,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
			else
				copyscrollbitmap(bitmap,tmpbitmap4,256,xscroll,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	
			copyscrollbitmap(bitmap,tmpbitmap3,256,xscroll2,0,null,Machine.drv.visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
		}
	
		for (offs = 0; offs < 2048; offs++)
		{
			if (char_dirty[offs] == 2)
				char_dirty[offs] = 0;
		}
	} };
	
}
