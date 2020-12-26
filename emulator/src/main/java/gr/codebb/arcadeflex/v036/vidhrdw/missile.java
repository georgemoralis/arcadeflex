/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class missile
{
	
	public static UBytePtr missile_videoram=new UBytePtr(256 * 256);
	public static int missile_flipscreen;
	static int screen_flipped;
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr missile_vh_start = new VhStartPtr() { public int handler() 
	{
	
		/* force video ram to be $0000-$FFFF even though only $1900-$FFFF is used */
		/*if ((missile_videoram = malloc (256 * 256)) == 0)
			return 1;
	
		memset (missile_videoram, 0, 256 * 256);*/
		missile_flipscreen = 0x40;
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr missile_vh_stop = new VhStopPtr() { public void handler() 
	{
		missile_videoram=null;
	} };
	
	/********************************************************************************************/
	public static ReadHandlerPtr missile_video_r = new ReadHandlerPtr() { public int handler(int address)
	{
		return (missile_videoram.read(address) & 0xe0);
	} };
	
	/********************************************************************************************/
	/* This routine is called when the flipscreen bit changes. It forces a redraw of the entire bitmap. */
	public static void missile_flip_screen ()
	{
		screen_flipped = 1;
	}
	
	/********************************************************************************************/
	public static void missile_blit_w (int offset)
	{
		int x, y;
		int bottom;
		int color;
	
		/* The top 25 lines ($0000 . $18ff) aren't used or drawn */
		y = (offset >> 8) - 25;
		x = offset & 0xff;
		if( y < 231 - 32)
			bottom = 1;
		else
			bottom = 0;
	
		/* cocktail mode */
		if (missile_flipscreen != 0)
		{
			y = Machine.scrbitmap.height - 1 - y;
		}
	
		color = (missile_videoram.read(offset) >> 5);
	
		if (bottom != 0) color &= 0x06;
	
		plot_pixel.handler(Machine.scrbitmap, x, y, Machine.pens[color]);
	}
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		/* $0640 - $4fff */
		int wbyte, wbit;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		if (address < 0xf800)
		{
			missile_videoram.write(address,data);
			missile_blit_w (address);
		}
		else
		{
			missile_videoram.write(address,(missile_videoram.read(address) & 0x20) | data);
			missile_blit_w (address);
			wbyte = ((address - 0xf800) >> 2) & 0xfffe;
			wbit = (address - 0xf800) % 8;
			if ((data & 0x20) != 0)
				RAM.write(0x401 + wbyte,RAM.read(0x401 + wbyte) | (1 << wbit));
			else
				RAM.write(0x401 + wbyte,RAM.read(0x401 + wbyte)  & ((1 << wbit) ^ 0xff));
		}
	} };
	
	public static WriteHandlerPtr missile_video2_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* $5000 - $ffff */
		offset += 0x5000;
		missile_video_w.handler(offset, data);
	} };
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_mult_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		/*
			$1900 - $3fff
	
			2-bit color writes in 4-byte blocks.
			The 2 color bits are in x000x000.
	
			Note that the address range is smaller because 1 byte covers 4 screen pixels.
		*/
	
		data = (data & 0x80) + ((data & 8) << 3);
		address = address << 2;
	
		/* If this is the bottom 8 lines of the screen, set the 3rd color bit */
		if (address >= 0xf800) data |= 0x20;
	
		missile_videoram.write(address,data);
		missile_videoram.write(address + 1,data);
		missile_videoram.write(address + 2,data);
		missile_videoram.write(address + 3,data);
	
		missile_blit_w (address);
		missile_blit_w (address + 1);
		missile_blit_w (address + 2);
		missile_blit_w (address + 3);
	} };
	
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_3rd_bit_w = new WriteHandlerPtr() { public void handler(int address, int data)
	{
		int i;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		address += 0x400;
		/* This is needed to make the scrolling text work properly */
		RAM.write(address,data);
	
		address = ((address - 0x401) << 2) + 0xf800;
		for (i=0; i<8; i++)
		{
			if ((data & (1 << i))!=0)
				missile_videoram.write(address + i,missile_videoram.read(address + i) | 0x20);
			else
				missile_videoram.write(address + i,missile_videoram.read(address + i) & 0xc0);
			missile_blit_w (address + i);
		}
	} };
	
	
	/********************************************************************************************/
	public static VhUpdatePtr missile_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int address;
	
		if (palette_recalc()!=null || full_refresh!=0 || screen_flipped!=0)
		{
			for (address = 0x1900; address <= 0xffff; address++)
				missile_blit_w (address);
	
			screen_flipped = 0;
		}
	} };
}
