/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class qwakprot
{
	
	
	static struct rectangle spritevisiblearea =
	{
		1*8, 31*8-1,
		0*8, 30*8-1
	};
	
	
	/***************************************************************************
	  qwakprot_paletteram_w
	
	  This might seem a little odd, but it really seems as though the palette
	  is writing as GGGRRRBB.  This is just a guess, and has not been confirmed.
	***************************************************************************/
	public static WriteHandlerPtr qwakprot_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int bit0,bit1,bit2;
		int r,g,b;
	
	
		paletteram.write(offset,data);

		/* red component */
		bit0 = (~data >> 2) & 0x01;
		bit1 = (~data >> 3) & 0x01;
		bit2 = (~data >> 4) & 0x01;
		r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		/* green component */
		bit0 = (~data >> 5) & 0x01;
		bit1 = (~data >> 6) & 0x01;
		bit2 = (~data >> 7) & 0x01;
		g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		/* blue component */
		bit0 = 0;
		bit1 = (~data >> 0) & 0x01;
		bit2 = (~data >> 1) & 0x01;
		b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		palette_change_color(offset,r,g,b);
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given osd_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VhUpdatePtr qwakprot_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int offs;
	
		if (palette_recalc() || full_refresh)
			memset (dirtybuffer, 1, videoram_size[0]);
	
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy;
				int gfxset;
	
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
	
				gfxset = ((videoram.read(offs)& 0x80) >> 7);
				drawgfx(bitmap,Machine.gfx[gfxset],
						videoram.read(offs)& 0x7f,
						0,		/* color */
						0,0,	/* flipx, flipy */
						8*sx,8*sy,
						&Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
		/* Draw the sprites */
		for (offs = 0;offs < 0x10;offs++)
		{
			int spritenum;
			int flip;
			int x, y;
			int sx, sy;
	
			spritenum = spriteram.read(offs)& 0x7f;
	
			flip = (spriteram.read(offs)& 0x80);
			x = spriteram.read(offs + 0x20);
			y = 240 - spriteram.read(offs + 0x10);
	
			drawgfx(bitmap,Machine.gfx[2],
					spritenum,0,
					0,flip,
					x,y,
					&spritevisiblearea,TRANSPARENCY_PEN,0);
	
			/* mark tiles underneath as dirty */
			sx = x >> 3;
			sy = y >> 3;
	
			{
				int max_x = 1;
				int max_y = 2;
				int x2, y2;
	
				if ((x & 0x07) != 0) max_x ++;
				if ((y & 0x0f) != 0) max_y ++;
	
				for (y2 = sy; y2 < sy + max_y; y2 ++)
				{
					for (x2 = sx; x2 < sx + max_x; x2 ++)
					{
						if ((x2 < 32) && (y2 < 30) && (x2 >= 0) && (y2 >= 0))
							dirtybuffer[x2 + 32*y2] = 1;
					}
				}
			}
	
		}
	} };
}
