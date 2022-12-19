/***************************************************************************

  vmissile.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class missile
{
	
	UBytePtr missile_videoram;
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VhStartPtr missile_vh_start = new VhStartPtr() { public int handler() 
	{
	
		/* force video ram to be $0000-$FFFF even though only $1900-$FFFF is used */
		if ((missile_videoram = malloc (256 * 256)) == 0)
			return 1;
	
		memset (missile_videoram, 0, 256 * 256);
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Stop the video hardware emulation.
	
	***************************************************************************/
	public static VhStopPtr missile_vh_stop = new VhStopPtr() { public void handler() 
	{
		free (missile_videoram);
	} };
	
	/********************************************************************************************/
	public static ReadHandlerPtr missile_video_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (missile_videoram[offset] & 0xe0);
	} };
	
	/********************************************************************************************/
	static void missile_blit_w (offs_t offset)
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
		if (flip_screen != 0)
		{
			y = Machine.scrbitmap.height - 1 - y;
		}
	
		color = (missile_videoram[offset] >> 5);
	
		if (bottom != 0) color &= 0x06;
	
		plot_pixel(Machine.scrbitmap, x, y, Machine.pens[color]);
	}
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* $0640 - $4fff */
		int wbyte, wbit;
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		if (offset < 0xf800)
		{
			missile_videoram[offset] = data;
			missile_blit_w (offset);
		}
		else
		{
			missile_videoram[offset] = (missile_videoram[offset] & 0x20) | data;
			missile_blit_w (offset);
			wbyte = ((offset - 0xf800) >> 2) & 0xfffe;
			wbit = (offset - 0xf800) % 8;
			if ((data & 0x20) != 0)
				RAM[0x401 + wbyte] |= (1 << wbit);
			else
				RAM[0x401 + wbyte] &= ((1 << wbit) ^ 0xff);
		}
	} };
	
	public static WriteHandlerPtr missile_video2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* $5000 - $ffff */
		offset += 0x5000;
		missile_video_w (offset, data);
	} };
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_mult_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*
			$1900 - $3fff
	
			2-bit color writes in 4-byte blocks.
			The 2 color bits are in x000x000.
	
			Note that the address range is smaller because 1 byte covers 4 screen pixels.
		*/
	
		data = (data & 0x80) + ((data & 8) << 3);
		offset = offset << 2;
	
		/* If this is the bottom 8 lines of the screen, set the 3rd color bit */
		if (offset >= 0xf800) data |= 0x20;
	
		missile_videoram[offset]     = data;
		missile_videoram[offset + 1] = data;
		missile_videoram[offset + 2] = data;
		missile_videoram[offset + 3] = data;
	
		missile_blit_w (offset);
		missile_blit_w (offset + 1);
		missile_blit_w (offset + 2);
		missile_blit_w (offset + 3);
	} };
	
	
	/********************************************************************************************/
	public static WriteHandlerPtr missile_video_3rd_bit_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
		UBytePtr RAM = memory_region(REGION_CPU1);
		offset = offset + 0x400;
	
		/* This is needed to make the scrolling text work properly */
		RAM[offset] = data;
	
		offset = ((offset - 0x401) << 2) + 0xf800;
		for (i=0; i<8; i++)
		{
			if (data & (1 << i))
				missile_videoram[offset + i] |= 0x20;
			else
				missile_videoram[offset + i] &= 0xc0;
			missile_blit_w (offset + i);
		}
	} };
	
	
	/********************************************************************************************/
	public static VhUpdatePtr missile_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (palette_recalc() || full_refresh)
		{
			int offs;
	
			for (offs = 0x1900; offs <= 0xffff; offs++)
				missile_blit_w (offs);
		}
	} };
}
