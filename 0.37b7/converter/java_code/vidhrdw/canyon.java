/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class canyon
{
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr canyon_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	    int offs;
	
	    /* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (full_refresh || dirtybuffer[offs])
			{
				int charcode;
				int sx,sy;
	
				dirtybuffer[offs]=0;
	
				charcode = videoram.read(offs)& 0x3F;
	
				sx = 8 * (offs % 32);
				sy = 8 * (offs / 32);
				drawgfx(tmpbitmap,Machine.gfx[0],
						charcode, (videoram.read(offs)& 0x80)>>7,
						0,0,sx,sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		for (offs=0; offs<2; offs++)
		{
			int sx, sy;
			int pic;
			int flipx;
	
			sx = 27*8 - spriteram.read(offs*2+1);
			sy = 30*8 - spriteram.read(offs*2+8);
			pic = spriteram.read(offs*2+9);
			if ((pic & 0x80) != 0)
				flipx=0;
			else
				flipx=1;
	
	        drawgfx(bitmap,Machine.gfx[1],
	                (pic & 0x18) >> 3, offs,
					flipx,0,sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
		/* Draw the bombs as 2x2 squares */
		for (offs=2; offs<4; offs++)
		{
			int sx, sy;
	
			sx = 31*8 - spriteram.read(offs*2+1);
			sy = 31*8 - spriteram.read(offs*2+8);
	
	        drawgfx(bitmap,Machine.gfx[2],
	                0, offs,
					0,0,sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
