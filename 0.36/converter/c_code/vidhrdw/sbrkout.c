/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  CHANGES:
  MAB 05 MAR 99 - changed overlay support to use artwork functions
***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "artwork.h"

unsigned char *sbrkout_horiz_ram;
unsigned char *sbrkout_vert_ram;

static struct artwork *overlay;

/* The first entry defines the color with which the bitmap is filled initially */
/* The array is terminated with an entry with negative coordinates. */
/* At least two entries are needed. */
static const struct artwork_element sbrkout_ol[]={
	{{	0, 256,   0, 256}, 0xFF, 0xFF, 0xFF,   0xFF},	/* white */
	{{208, 248,   8, 218}, 0x00, 0x00, 0xFF,   0xFF},	/* blue */
	{{176, 208,   8, 218}, 0xFF, 0x80, 0x00,   0xFF},	/* orange */
	{{144, 176,   8, 218}, 0x00, 0xFF, 0x00,   0xFF},	/* green */
	{{ 96, 144,   8, 218}, 0xFF, 0xFF, 0x00,   0xFF},	/* yellow */
	{{ 16,	24,   8, 218}, 0x00, 0x00, 0xFF,   0xFF},	/* blue */
	{{-1,-1,-1,-1},0,0,0,0}
};


/***************************************************************************
***************************************************************************/

int sbrkout_vh_start(void)
{
	int start_pen = 2;	/* leave space for black and white */

	if (generic_vh_start()!=0)
		return 1;

	if ((overlay = artwork_create(sbrkout_ol, start_pen, Machine->drv->total_colors-start_pen))==NULL)
		return 1;

	return 0;
}

/***************************************************************************
***************************************************************************/

void sbrkout_vh_stop(void)
{
	if (overlay)
		artwork_free(overlay);

	generic_vh_stop();
}

/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void sbrkout_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	int ball;


	if (palette_recalc())
	{
		memset(dirtybuffer,1,videoram_size);
		overlay_remap(overlay);
	}

	/* for every character in the Video RAM, check if it has been modified */
	/* since last time and update it accordingly. */
	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		if (dirtybuffer[offs])
		{
			int charcode;
			int sx,sy;
			int color;

			dirtybuffer[offs]=0;

			charcode = videoram[offs] & 0x3F;

			sx = 8*(offs % 32);
			sy = 8*(offs / 32);

			/* Check the "draw" bit */
			color = ((videoram[offs] & 0x80)>>7);

			drawgfx(tmpbitmap,Machine->gfx[0],
					charcode, color,
					0,0,sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
		}
	}

	/* copy the character mapped graphics */
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	/* Draw each one of our three balls */
	for (ball=2;ball>=0;ball--)
	{
		int sx,sy;
		int picture;

		sx=31*8-sbrkout_horiz_ram[ball*2];
		sy=30*8-sbrkout_vert_ram[ball*2];
		picture=((sbrkout_vert_ram[ball*2+1] & 0x80) >> 7);

		drawgfx(bitmap,Machine->gfx[1],
				picture,1,
				0,0,sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}

	overlay_draw(bitmap,overlay);
}

