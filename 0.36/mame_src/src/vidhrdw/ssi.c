#include "driver.h"
#include "vidhrdw/generic.h"
#include "cpu/m68000/m68000.h"




int ssi_videoram_r(int offset)
{
	return READ_WORD(&videoram[offset]);
}

void ssi_videoram_w(int offset,int data)
{
	COMBINE_WORD_MEM(&videoram[offset],data);
}

int ssi_vh_start(void)
{
	return 0;
}

void ssi_vh_stop (void)
{
}

void ssi_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int i,x,y,offs,code,color,spritecont,flipx,flipy;
	int xcurrent,ycurrent;


	/* update the palette usage */
	{
		unsigned short palette_map[256];

		memset (palette_map, 0, sizeof (palette_map));

		color = 0;

		for (offs = 0;offs < 0x3400;offs += 16)
		{
			spritecont = (READ_WORD(&videoram[offs+8]) & 0xff00) >> 8;

			if ((spritecont & 0x04) == 0)
			{
				color = READ_WORD(&videoram[offs+8]) & 0x00ff;
			}

			code = READ_WORD(&videoram[offs]) & 0x1fff;

			palette_map[color] |= Machine->gfx[0]->pen_usage[code];
		}

		for (i = 0;i < 256;i++)
		{
			int usage = palette_map[i];
			int j;

			if (usage)
			{
				palette_used_colors[i * 16 + 0] = PALETTE_COLOR_TRANSPARENT;
				for (j = 1; j < 16; j++)
					if (palette_map[i] & (1 << j))
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_USED;
					else
						palette_used_colors[i * 16 + j] = PALETTE_COLOR_UNUSED;
			}
			else
				memset(&palette_used_colors[i * 16],PALETTE_COLOR_UNUSED,16);
		}

		palette_recalc ();
	}

	osd_clearbitmap(bitmap);
	x = 0;
	y = 0;
	xcurrent = 0;
	ycurrent = 0;
	color = 0;

	for (offs = 0;offs < 0x3400;offs += 16)
	{
        spritecont = (READ_WORD(&videoram[offs+8]) & 0xff00) >> 8;

		flipx = spritecont & 0x01;
		flipy = spritecont & 0x02;

		if ((spritecont & 0x04) == 0)
		{
			x = READ_WORD(&videoram[offs+4]) & 0x0fff;
			if (x >= 0x800) x -= 0x1000;
			xcurrent = x;

			y = READ_WORD(&videoram[offs+6]) & 0x0fff;
			if (y >= 0x800) y -= 0x1000;
			ycurrent = y;

			color = READ_WORD(&videoram[offs+8]) & 0x00ff;
		}
		else
		{
			if ((spritecont & 0x10) == 0)
				y = ycurrent;
			else if ((spritecont & 0x20) != 0)
				y += 16;

			if ((spritecont & 0x40) == 0)
				x = xcurrent;
			else if ((spritecont & 0x80) != 0)
				x += 16;
		}

		code = READ_WORD(&videoram[offs]) & 0x1fff;

		drawgfx(bitmap,Machine->gfx[0],
				code,
				color,
				flipx,flipy,
				x,y,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
	}
}
