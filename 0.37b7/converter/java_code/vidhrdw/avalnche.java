/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class avalnche
{
	
	
	public static WriteHandlerPtr avalnche_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram.write(offset,data);

		if (offset >= 0x200)
		{
			int x,y,i;
	
			x = 8 * (offset % 32);
			y = offset / 32;
	
			for (i = 0;i < 8;i++)
				plot_pixel(Machine.scrbitmap,x+7-i,y,Machine.pens[(data >> i) & 1]);
		}
	} };
	
	public static VhUpdatePtr avalnche_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		if (full_refresh != 0)
		{
			int offs;
	
	
			for (offs = 0;offs < videoram_size[0]; offs++)
				avalnche_videoram_w(offs,videoram.read(offs));
		}
	} };
}
