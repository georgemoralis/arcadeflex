/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"



/* palette colors (see drivers/8080bw.c) */
enum { BLACK, WHITE };


static unsigned char mask = 0;

void ramtek_mask_w(int offset, int data)
{
	mask = data;
}

void ramtek_videoram_w (int offset,int data)
{
	data = data & ~mask;

	if (videoram[offset] != data)
	{
		int i,x,y;

		videoram[offset] = data;

		y = offset / 32;
		x = 8 * (offset % 32);

		for (i = 0; i < 8; i++)
		{
			plot_pixel2(Machine->scrbitmap, tmpbitmap, x, y, Machine->pens[(data & 0x80) ? WHITE : BLACK]);

			x++;
			data <<= 1;
		}
	}
}
