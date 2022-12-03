/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


static int flipscreen;


/***************************************************************************

  Convert the color PROMs into a more useable format.

***************************************************************************/

void wiping_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + (offs)])


	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		int bit0,bit1,bit2;


		/* red component */
		bit0 = (*color_prom >> 0) & 0x01;
		bit1 = (*color_prom >> 1) & 0x01;
		bit2 = (*color_prom >> 2) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		/* green component */
		bit0 = (*color_prom >> 3) & 0x01;
		bit1 = (*color_prom >> 4) & 0x01;
		bit2 = (*color_prom >> 5) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		/* blue component */
		bit0 = 0;
		bit1 = (*color_prom >> 6) & 0x01;
		bit2 = (*color_prom >> 7) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

		color_prom++;
	}

	/* color_prom now points to the beginning of the lookup table */

	/* chars use colors 0-15 */
	for (i = 0;i < TOTAL_COLORS(0);i++)
		COLOR(0,i ^ 3) = *(color_prom++) & 0x0f;

	/* sprites use colors 16-31 */
	for (i = 0;i < TOTAL_COLORS(1);i++)
		COLOR(1,i ^ 3) = (*(color_prom++) & 0x0f) + 0x10;
}



void wiping_flipscreen_w(int offset,int data)
{
	if (flipscreen != (data & 1))
	{
		flipscreen = (data & 1);
		memset(dirtybuffer,1,videoram_size);
	}
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void wiping_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	for (offs = videoram_size - 1; offs > 0; offs--)
	{
		if (dirtybuffer[offs])
		{
			int mx,my,sx,sy;

			dirtybuffer[offs] = 0;

	        mx = offs % 32;
			my = offs / 32;

			if (my < 2)
			{
				sx = my + 34;
				sy = mx - 2;
			}
			else if (my >= 30)
			{
				sx = my - 30;
				sy = mx - 2;
			}
			else
			{
				sx = mx + 2;
				sy = my - 2;
			}

			if (flipscreen)
			{
				sx = 35 - sx;
				sy = 27 - sy;
			}

			drawgfx(tmpbitmap,Machine->gfx[0],
					videoram[offs],
					colorram[offs] & 0x3f,
					flipscreen,flipscreen,
					sx*8,sy*8,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
        	}
	}
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	/* Note, we're counting up on purpose ! */
	/* This way the vacuum cleaner is always on top */
	for (offs = 0x0; offs < 128; offs += 2) {
		int sx,sy,flipx,flipy,otherbank;

		sx = spriteram[offs+0x100+1] + ((spriteram[offs+0x81] & 0x01) << 8) - 40;
		sy = 224 - spriteram[offs+0x100];

		otherbank = spriteram[offs+0x80] & 0x01;

		flipy = spriteram[offs] & 0x40;
		flipx = spriteram[offs] & 0x80;

		if (flipscreen)
		{
			sy = 208 - sy;
			flipx = !flipx;
			flipy = !flipy;
		}

		drawgfx(bitmap,Machine->gfx[1],
			(spriteram[offs] & 0x3f) + 64 * otherbank,
			spriteram[offs+1] & 0x3f,
			flipx,flipy,
			sx,sy,
			&Machine->drv->visible_area,TRANSPARENCY_COLOR,0x1f);
	}

	/* redraw high priority chars */
	for (offs = videoram_size - 1; offs > 0; offs--)
	{
		if (colorram[offs] & 0x80)
		{
			int mx,my,sx,sy;

	        mx = offs % 32;
			my = offs / 32;

			if (my < 2)
			{
				sx = my + 34;
				sy = mx - 2;
			}
			else if (my >= 30)
			{
				sx = my - 30;
				sy = mx - 2;
			}
			else
			{
				sx = mx + 2;
				sy = my - 2;
			}

			if (flipscreen)
			{
				sx = 35 - sx;
				sy = 27 - sy;
			}

			drawgfx(bitmap,Machine->gfx[0],
					videoram[offs],
					colorram[offs] & 0x3f,
					flipscreen,flipscreen,
					sx*8,sy*8,
					&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
        	}
	}


#if 0
{
	int i,j;
	extern unsigned char *wiping_soundregs;

	for (i = 0;i < 8;i++)
	{
		for (j = 0;j < 8;j++)
		{
			char buf[40];
			sprintf(buf,"%01x",wiping_soundregs[i*8+j]&0xf);
			ui_text(buf,j*10,i*8);
		}
	}

	for (i = 0;i < 8;i++)
	{
		for (j = 0;j < 8;j++)
		{
			char buf[40];
			sprintf(buf,"%01x",wiping_soundregs[0x2000+i*8+j]>>4);
			ui_text(buf,j*10,80+i*8);
		}
	}
}
#endif
}
