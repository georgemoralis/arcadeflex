/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
#include "driver.h"
#include "vidhrdw/generic.h"
#include "ctype.h"



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void flstory_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	if (palette_recalc())
		memset(dirtybuffer,1,videoram_size);

	for (offs = videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer[offs] || dirtybuffer[offs+1])
		{
			int sx,sy;


			dirtybuffer[offs] = 0;
			dirtybuffer[offs+1] = 0;

			sx = 31 - (offs/2)%32;
			sy = 31 - (offs/2)/32;

			drawgfx(tmpbitmap,Machine->gfx[0],
					videoram[offs] + ((videoram[offs + 1] & 0xc0) << 2),
					0,
					~videoram[offs + 1] & 8,0,
					8*sx,8*sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
		}
	}

	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	for (offs = 0;offs < spriteram_size;offs += 4)
	{
		int code,sx,sy,flipx,flipy;


		code = spriteram[offs+2] + ((spriteram[offs+1] & 0x20) << 3);
		sx = 240 - spriteram[offs+3];
		sy = spriteram[offs+0];
		flipx = ~spriteram[offs+1]&0x40;
		flipy = ~spriteram[offs+1]&0x80;

		drawgfx(bitmap,Machine->gfx[1],
				code,
				0,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		/* wrap around */
		if (sx < 0)
			drawgfx(bitmap,Machine->gfx[1],
					code,
					0,
					flipx,flipy,
					sx+256,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}
