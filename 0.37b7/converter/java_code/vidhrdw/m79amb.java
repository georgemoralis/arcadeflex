/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class m79amb
{
	
	
	
	/* palette colors (see drivers/8080bw.c) */
	static final int BLACK = 0, WHITE = 1;
	
	
	static unsigned char mask = 0;
	
	public static WriteHandlerPtr ramtek_mask_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mask = data;
	} };
	
	public static WriteHandlerPtr ramtek_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data = data & ~mask;
	
		if (videoram.read(offset)!= data)
		{
			int i,x,y;
	
			videoram.write(offset,data);

			y = offset / 32;
			x = 8 * (offset % 32);
	
			for (i = 0; i < 8; i++)
			{
				plot_pixel2(Machine.scrbitmap, tmpbitmap, x, y, Machine.pens[(data & 0x80) ? WHITE : BLACK]);
	
				x++;
				data <<= 1;
			}
		}
	} };
}
