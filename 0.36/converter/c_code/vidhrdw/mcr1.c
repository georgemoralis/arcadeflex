/***************************************************************************

  vidhrdw/mcr1.c

  Functions to emulate the video hardware of an mcr 1 style machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


INT16 mcr1_spriteoffset;


/*************************************
 *
 *	Generic MCR1 redraw
 *
 *************************************/

void mcr1_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs;

	/* mark everything dirty on a full refresh */
	if (palette_recalc() || full_refresh)
		memset(dirtybuffer, 1, videoram_size);

	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1; offs >= 0; offs--)
	{
		if (dirtybuffer[offs])
		{
			int mx = offs % 32;
			int my = offs / 32;

			int code = videoram[offs];

			drawgfx(bitmap, Machine->gfx[0], code, 0, 0, 0,
					16 * mx, 16 * my, &Machine->drv->visible_area, TRANSPARENCY_NONE, 0);

			dirtybuffer[offs] = 0;
		}
	}

	/* draw the sprites */
	for (offs = 0; offs < spriteram_size; offs += 4)
	{
		int code, x, y, xcount, ycount, hflip, vflip, sx, sy;

		/* skip if nothing to draw */
		if (spriteram[offs] == 0)
			continue;

		/* extract the parameters */
		code = spriteram[offs + 1] & 0x3f;
		hflip = spriteram[offs + 1] & 0x40;
		vflip = spriteram[offs + 1] & 0x80;
		x = (spriteram[offs + 2] + mcr1_spriteoffset) * 2;
		y = (241 - spriteram[offs]) * 2;

		/* draw to the bitmap */
		drawgfx(bitmap, Machine->gfx[1], code, 0, hflip, vflip,
				x, y, &Machine->drv->visible_area, TRANSPARENCY_PEN, 0);

		/* mark tiles underneath as dirty */
		sx = x / 16;
		sy = y / 16;
		xcount = (x & 15) ? 3 : 2;
		ycount = (y & 15) ? 3 : 2;

		for (y = sy; y < sy + ycount; y++)
			for (x = sx; x < sx + xcount; x++)
				if (x >= 0 && x < 32 && y >= 0 && y < 30)
					dirtybuffer[32 * y + x] = 1;
	}
}
