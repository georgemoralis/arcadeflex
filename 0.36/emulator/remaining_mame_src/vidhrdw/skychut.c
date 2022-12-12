/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  (c) 12/2/1998 Lee Taylor

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"



static int flipscreen;


void skychut_vh_flipscreen_w (int offset,int data)
{
/*	if (flipscreen != (data & 0x8f))
	{
		flipscreen = (data & 0x8f);
		memset(dirtybuffer,1,videoram_size);
	}
*/
}


void skychut_colorram_w(int offset,int data)
{
	if (colorram[offset] != data)
	{
		dirtybuffer[offset] = 1;

		colorram[offset] = data;
	}
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void skychut_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	if (full_refresh)
		memset (dirtybuffer, 1, videoram_size);

	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		if (dirtybuffer[offs])
		{
			int sx,sy;


			dirtybuffer[offs] = 0;

			sx = offs % 32;
			sy = offs / 32;

			drawgfx(bitmap,Machine->gfx[0],
					videoram[offs],
					 colorram[offs],
					flipscreen,flipscreen,
					8*sx,8*sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);


		}
	}

}
