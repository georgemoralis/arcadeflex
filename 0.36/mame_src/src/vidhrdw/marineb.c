/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


unsigned char *marineb_column_scroll;
int marineb_active_low_flipscreen;
static int palbank;


static int flipscreen_x;
static int flipscreen_y;


void marineb_palbank0_w(int offset, int data)
{
	if ((palbank & 1) != (data & 1))
	{
		palbank = (palbank & ~1) | (data & 1);
		memset(dirtybuffer, 1, videoram_size);
	}
}

void marineb_palbank1_w(int offset, int data)
{
	data <<= 1;
	if ((palbank & 2) != (data & 2))
	{
		palbank = (palbank & ~2) | (data & 2);
		memset(dirtybuffer, 1, videoram_size);
	}
}

void marineb_flipscreen_x_w(int offset, int data)
{
	if (flipscreen_x != (data ^ marineb_active_low_flipscreen))
	{
		flipscreen_x = data ^ marineb_active_low_flipscreen;
		memset(dirtybuffer, 1, videoram_size);
	}
}

void marineb_flipscreen_y_w(int offset, int data)
{
	if (flipscreen_y != (data ^ marineb_active_low_flipscreen))
	{
		flipscreen_y = data ^ marineb_active_low_flipscreen;
		memset(dirtybuffer, 1, videoram_size);
	}
}


/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
static void draw_chars(struct osd_bitmap *_tmpbitmap, struct osd_bitmap *bitmap,
                       int scroll_cols)
{
	int offs;


	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		if (dirtybuffer[offs])
		{
			int sx,sy,flipx,flipy;


			dirtybuffer[offs] = 0;

			sx = offs % 32;
			sy = offs / 32;

			flipx = colorram[offs] & 0x20;
			flipy = colorram[offs] & 0x10;

			if (flipscreen_y)
			{
				sy = 31 - sy;
				flipy = !flipy;
			}

			if (flipscreen_x)
			{
				sx = 31 - sx;
				flipx = !flipx;
			}

			drawgfx(_tmpbitmap,Machine->gfx[0],
					videoram[offs] | ((colorram[offs] & 0xc0) << 2),
					(colorram[offs] & 0x0f) + 16 * palbank,
					flipx,flipy,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}


	/* copy the temporary bitmap to the screen */
	{
		int scroll[32];


		if (flipscreen_y)
		{
			for (offs = 0;offs < 32 - scroll_cols;offs++)
				scroll[offs] = 0;

			for (;offs < 32;offs++)
				scroll[offs] = marineb_column_scroll[0];
		}
		else
		{
			for (offs = 0;offs < scroll_cols;offs++)
				scroll[offs] = -marineb_column_scroll[0];

			for (;offs < 32;offs++)
				scroll[offs] = 0;
		}
		copyscrollbitmap(bitmap,tmpbitmap,0,0,32,scroll,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
	}
}


void marineb_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	draw_chars(tmpbitmap, bitmap, 24);


	/* draw the sprites */
	for (offs = 0x0f; offs >= 0; offs--)
	{
		int gfx,sx,sy,code,col,flipx,flipy,offs2;


		if ((offs == 0) || (offs == 2))  continue;  /* no sprites here */


		if (offs < 8)
		{
			offs2 = 0x0018 + offs;
		}
		else
		{
			offs2 = 0x03d8 - 8 + offs;
		}


		code  = videoram[offs2];
		sx    = videoram[offs2 + 0x20];
		sy    = colorram[offs2];
		col   = (colorram[offs2 + 0x20] & 0x0f) + 16 * palbank;
		flipx =   code & 0x02;
		flipy = !(code & 0x01);

		if (offs < 4)
		{
			/* big sprite */
			gfx = 2;
			code = (code >> 4) | ((code & 0x0c) << 2);
		}
		else
		{
			/* small sprite */
			gfx = 1;
			code >>= 2;
		}

		if (!flipscreen_y)
		{
			sy = 256 - Machine->gfx[gfx]->width - sy;
			flipy = !flipy;
		}

		if (flipscreen_x)
		{
			sx++;
		}

		drawgfx(bitmap,Machine->gfx[gfx],
				code,
				col,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}


void changes_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs,sx,sy,code,col,flipx,flipy;


	draw_chars(tmpbitmap, bitmap, 26);


	/* draw the small sprites */
	for (offs = 0x05; offs >= 0; offs--)
	{
		int offs2;


		offs2 = 0x001a + offs;

		code  = videoram[offs2];
		sx    = videoram[offs2 + 0x20];
		sy    = colorram[offs2];
		col   = (colorram[offs2 + 0x20] & 0x0f) + 16 * palbank;
		flipx =   code & 0x02;
		flipy = !(code & 0x01);

		if (!flipscreen_y)
		{
			sy = 256 - Machine->gfx[1]->width - sy;
			flipy = !flipy;
		}

		if (flipscreen_x)
		{
			sx++;
		}

		drawgfx(bitmap,Machine->gfx[1],
				code >> 2,
				col,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}

	/* draw the big sprite */

	code  = videoram[0x3df];
	sx    = videoram[0x3ff];
	sy    = colorram[0x3df];
	col   = colorram[0x3ff];
	flipx =   code & 0x02;
	flipy = !(code & 0x01);

	if (!flipscreen_y)
	{
		sy = 256 - Machine->gfx[2]->width - sy;
		flipy = !flipy;
	}

	if (flipscreen_x)
	{
		sx++;
	}

	code >>= 4;

	drawgfx(bitmap,Machine->gfx[2],
			code,
			col,
			flipx,flipy,
			sx,sy,
			&Machine->drv->visible_area,TRANSPARENCY_PEN,0);

	/* draw again for wrap around */

	drawgfx(bitmap,Machine->gfx[2],
			code,
			col,
			flipx,flipy,
			sx-256,sy,
			&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
}


void springer_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	draw_chars(tmpbitmap, bitmap, 0);


	/* draw the sprites */
	for (offs = 0x0f; offs >= 0; offs--)
	{
		int gfx,sx,sy,code,col,flipx,flipy,offs2;


		if ((offs == 0) || (offs == 2))  continue;  /* no sprites here */


		offs2 = 0x0010 + offs;


		code  = videoram[offs2];
		sx    = 240 - videoram[offs2 + 0x20];
		sy    = colorram[offs2];
		col   = (colorram[offs2 + 0x20] & 0x0f) + 16 * palbank;
		flipx = !(code & 0x02);
		flipy = !(code & 0x01);

		if (offs < 4)
		{
			/* big sprite */
			sx -= 0x10;
			gfx = 2;
			code = (code >> 4) | ((code & 0x0c) << 2);
		}
		else
		{
			/* small sprite */
			gfx = 1;
			code >>= 2;
		}

		if (!flipscreen_y)
		{
			sy = 256 - Machine->gfx[gfx]->width - sy;
			flipy = !flipy;
		}

		if (!flipscreen_x)
		{
			sx--;
		}

		drawgfx(bitmap,Machine->gfx[gfx],
				code,
				col,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}


void hoccer_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	draw_chars(tmpbitmap, bitmap, 0);


	/* draw the sprites */
	for (offs = 0x07; offs >= 0; offs--)
	{
		int sx,sy,code,col,flipx,flipy,offs2;


		offs2 = 0x0018 + offs;


		code  = spriteram[offs2];
		sx    = spriteram[offs2 + 0x20];
		sy    = colorram[offs2];
		col   = colorram[offs2 + 0x20];
		flipx =   code & 0x02;
		flipy = !(code & 0x01);

		if (!flipscreen_y)
		{
			sy = 256 - Machine->gfx[1]->width - sy;
			flipy = !flipy;
		}

		if (flipscreen_x)
		{
			sx = 256 - Machine->gfx[1]->width - sx;
			flipx = !flipx;
		}

		drawgfx(bitmap,Machine->gfx[1],
				code >> 2,
				col,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}


void hopprobo_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	draw_chars(tmpbitmap, bitmap, 0);


	/* draw the sprites */
	for (offs = 0x0f; offs >= 0; offs--)
	{
		int gfx,sx,sy,code,col,flipx,flipy,offs2;


		if ((offs == 0) || (offs == 2))  continue;  /* no sprites here */


		offs2 = 0x0010 + offs;


		code  = videoram[offs2];
		sx    = videoram[offs2 + 0x20];
		sy    = colorram[offs2];
		col   = (colorram[offs2 + 0x20] & 0x0f) + 16 * palbank;
		flipx =   code & 0x02;
		flipy = !(code & 0x01);

		if (offs < 4)
		{
			/* big sprite */
			gfx = 2;
			code = (code >> 4) | ((code & 0x0c) << 2);
		}
		else
		{
			/* small sprite */
			gfx = 1;
			code >>= 2;
		}

		if (!flipscreen_y)
		{
			sy = 256 - Machine->gfx[gfx]->width - sy;
			flipy = !flipy;
		}

		if (!flipscreen_x)
		{
			sx--;
		}

		drawgfx(bitmap,Machine->gfx[gfx],
				code,
				col,
				flipx,flipy,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}
