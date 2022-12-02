/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

void subs_invert1_w(int offset, int data)
{
	if ((offset & 0x01) == 1)
	{
		palette_change_color(0, 0x00, 0x00, 0x00);
		palette_change_color(1, 0xFF, 0xFF, 0xFF);
	}
	else
	{
		palette_change_color(1, 0x00, 0x00, 0x00);
		palette_change_color(0, 0xFF, 0xFF, 0xFF);
	}
}

void subs_invert2_w(int offset, int data)
{
	if ((offset & 0x01) == 1)
	{
		palette_change_color(2, 0x00, 0x00, 0x00);
		palette_change_color(3, 0xFF, 0xFF, 0xFF);
	}
	else
	{
		palette_change_color(3, 0x00, 0x00, 0x00);
		palette_change_color(2, 0xFF, 0xFF, 0xFF);
	}
}


/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void subs_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	if (palette_recalc())
		memset(dirtybuffer,1,videoram_size);

	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		if (full_refresh || dirtybuffer[offs])
		{
			int charcode;
			int sx,sy;
			int left_enable,right_enable;
			int left_sonar_window,right_sonar_window;

			left_sonar_window = 0;
			right_sonar_window = 0;

			dirtybuffer[offs]=0;

			charcode = videoram[offs];

			/* Which monitor is this for? */
			right_enable = charcode & 0x40;
			left_enable = charcode & 0x80;

			sx = 8 * (offs % 32);
			sy = 8 * (offs / 32);

			/* Special hardware logic for sonar windows */
			if ((sy >= (128+64)) && (sx < 32))
				left_sonar_window = 1;
			else if ((sy >= (128+64)) && (sx >= (128+64+32)))
				right_sonar_window = 1;
			else
				charcode = charcode & 0x3F;

			/* Draw the left screen */
			if ((left_enable || left_sonar_window) && (!right_sonar_window))
			{
				drawgfx(tmpbitmap,Machine->gfx[0],
						charcode, 1,
						0,0,sx,sy,
						&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}
			else
			{
				drawgfx(tmpbitmap,Machine->gfx[0],
						0, 1,
						0,0,sx,sy,
						&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}

			/* Draw the right screen */
			if ((right_enable || right_sonar_window) && (!left_sonar_window))
			{
				drawgfx(tmpbitmap,Machine->gfx[0],
						charcode, 0,
						0,0,sx+256,sy,
						&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}
			else
			{
				drawgfx(tmpbitmap,Machine->gfx[0],
						0, 0,
						0,0,sx+256,sy,
						&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
			}
		}
	}

	/* copy the character mapped graphics */
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	/* draw the motion objects */
	for (offs = 0; offs < 4; offs++)
	{
		int sx,sy;
		int charcode;
		int prom_set;
		int sub_enable;

		sx = spriteram[0x00 + (offs * 2)] - 16;
		sy = spriteram[0x08 + (offs * 2)] - 16;
		charcode = spriteram[0x09 + (offs * 2)];
		sub_enable = spriteram[0x01 + (offs * 2)];

		prom_set = charcode & 0x01;
		charcode = (charcode >> 3) & 0x1F;

		/* Left screen - special check for drawing right screen's sub */
		if ((offs!=0) || (sub_enable))
		{
			drawgfx(bitmap,Machine->gfx[1],
					charcode + 32 * prom_set,
					0,
					0,0,sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}

		/* Right screen - special check for drawing left screen's sub */
		if ((offs!=1) || (sub_enable))
		{
			drawgfx(bitmap,Machine->gfx[1],
					charcode + 32 * prom_set,
					0,
					0,0,sx + 256,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
	}
}
