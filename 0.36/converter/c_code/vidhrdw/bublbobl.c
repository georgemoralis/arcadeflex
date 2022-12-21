/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
#include "driver.h"
#include "vidhrdw/generic.h"
#include "ctype.h"



unsigned char *bublbobl_objectram;
int bublbobl_objectram_size;



void bublbobl_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;


	/* no color PROMs here, only RAM, but the gfx data is inverted so we */
	/* cannot use the default lookup table */
	for (i = 0;i < Machine->drv->color_table_len;i++)
		colortable[i] = i ^ 0x0f;
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void bublbobl_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	int sx,sy,xc,yc;
	int gfx_num,gfx_attr,gfx_offs;


	palette_recalc();
	/* no need to check the return code since we redraw everything each frame */


	/* Bubble Bobble doesn't have a real video RAM. All graphics (characters */
	/* and sprites) are stored in the same memory region, and information on */
	/* the background character columns is stored inthe area dd00-dd3f */

	/* This clears & redraws the entire screen each pass */
	fillbitmap(bitmap,Machine->gfx[0]->colortable[0],&Machine->drv->visible_area);

	sx = 0;
	for (offs = 0;offs < bublbobl_objectram_size;offs += 4)
    {
		int height;


		/* skip empty sprites */
		/* this is dword aligned so the UINT32 * cast shouldn't give problems */
		/* on any architecture */
		if (*(UINT32 *)(&bublbobl_objectram[offs]) == 0)
			continue;

		gfx_num = bublbobl_objectram[offs + 1];
		gfx_attr = bublbobl_objectram[offs + 3];

		if ((gfx_num & 0x80) == 0)	/* 16x16 sprites */
		{
			gfx_offs = ((gfx_num & 0x1f) * 0x80) + ((gfx_num & 0x60) >> 1) + 12;
			height = 2;
		}
		else	/* tilemaps (each sprite is a 16x256 column) */
		{
			gfx_offs = ((gfx_num & 0x3f) * 0x80);
			height = 32;
		}

		if ((gfx_num & 0xc0) == 0xc0)	/* next column */
			sx += 16;
		else
		{
			sx = bublbobl_objectram[offs + 2];
			if (gfx_attr & 0x40) sx -= 256;
		}
		sy = 256 - height*8 - (bublbobl_objectram[offs + 0]);

		for (xc = 0;xc < 2;xc++)
		{
			for (yc = 0;yc < height;yc++)
			{
				int goffs,code,color,flipx,flipy,x,y;

				goffs = gfx_offs + xc * 0x40 + yc * 0x02;
				code = videoram[goffs] + 256 * (videoram[goffs + 1] & 0x03) + 1024 * (gfx_attr & 0x0f);
				color = (videoram[goffs + 1] & 0x3c) >> 2;
				flipx = videoram[goffs + 1] & 0x40;
				flipy = videoram[goffs + 1] & 0x80;
				x = sx + xc * 8;
				y = (sy + yc * 8) & 0xff;

				drawgfx(bitmap,Machine->gfx[0],
						code,
						color,
						flipx,flipy,
						x,y,
						&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
}
