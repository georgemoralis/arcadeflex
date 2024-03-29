/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  (c) 12/2/1998 Lee Taylor

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class skychut
{
	
	
	
	static int flipscreen;
	
	
	public static WriteHandlerPtr skychut_vh_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	/*	if (flipscreen != (data & 0x8f))
		{
			flipscreen = (data & 0x8f);
			memset(dirtybuffer,1,videoram_size[0]);
		}
	*/
	} };
	
	
	public static WriteHandlerPtr skychut_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			dirtybuffer[offset] = 1;
	
			colorram.write(offset,data);
	}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr skychut_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		if (full_refresh != 0)
			memset (dirtybuffer, 1, videoram_size[0]);
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				drawgfx(bitmap,Machine.gfx[0],
						videoram.read(offs),
						 colorram.read(offs),
						flipscreen,flipscreen,
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	} };
	
	UINT8* iremm15_chargen;
	
	static void iremm15_drawgfx(struct osd_bitmap *bitmap, int ch,
								INT16 color, INT16 back, int x, int y)
	{
		UINT8 mask;
		int i;
	
		for (i=0; i<8; i++, x++) {
			mask=iremm15_chargen[ch*8+i];
			plot_pixel(bitmap,x,y+7,mask&0x80?color:back);
			plot_pixel(bitmap,x,y+6,mask&0x40?color:back);
			plot_pixel(bitmap,x,y+5,mask&0x20?color:back);
			plot_pixel(bitmap,x,y+4,mask&0x10?color:back);
			plot_pixel(bitmap,x,y+3,mask&8?color:back);
			plot_pixel(bitmap,x,y+2,mask&4?color:back);
			plot_pixel(bitmap,x,y+1,mask&2?color:back);
			plot_pixel(bitmap,x,y,mask&1?color:back);
		}
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr iremm15_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
		if (full_refresh != 0)
			memset (dirtybuffer, 1, videoram_size[0]);
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				iremm15_drawgfx(bitmap,
								videoram.read(offs),
								Machine.pens[colorram.read(offs)],
								Machine.pens[7], // space beam not color 0
								8*sx,8*sy);
				osd_mark_dirty (sx*8, sy*8, sx*8+7, sy*8+7, 0);
			}
		}
	
	} };
	
}
