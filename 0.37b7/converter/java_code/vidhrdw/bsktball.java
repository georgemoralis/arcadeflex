/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class bsktball
{
	
	UBytePtr bsktball_motion;
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr bsktball_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
	    int offs,motion;
	
	    /* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
	                if (dirtybuffer[offs])
	                {
	                        int charcode;
	                        int sx,sy;
							int flipx;
	                        int color;
	
	                        dirtybuffer[offs]=0;
	
	                        charcode = videoram.read(offs);
	
	                        color = (charcode & 0x40) >> 6;
							flipx = (charcode & 0x80) >> 7;
	
	                        charcode = ((charcode & 0x0F) << 2) | ((charcode & 0x30) >> 4);
	
	                        sx = 8 * (offs % 32);
	                        sy = 8 * (offs / 32);
	                        drawgfx(tmpbitmap,Machine.gfx[0],
	                                charcode, color,
									flipx,0,sx,sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
	                }
		}
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
	
		for (motion=0;motion<16;motion++)
		{
			int pic,sx,sy,color,flipx;
	
			pic = bsktball_motion[motion*4];
			sy = 28*8 - bsktball_motion[motion*4 + 1];
			sx = bsktball_motion[motion*4 + 2];
			color = bsktball_motion[motion*4 + 3];
	
			flipx = (pic & 0x80) >> 7;
			pic = (pic & 0x3F);
	        color = (color & 0x3F);
	
	        drawgfx(bitmap,Machine.gfx[1],
	                pic, color,
					flipx,0,sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
	
}
