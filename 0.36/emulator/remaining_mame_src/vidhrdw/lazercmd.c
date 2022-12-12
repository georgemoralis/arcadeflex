/*************************************************************/
/*                                                           */
/* Lazer Command video handler                               */
/*                                                           */
/*************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "vidhrdw/lazercmd.h"

extern  int marker_x, marker_y;

static  int overlay = 0;

static  int video_inverted = 0;

/* scale a markers vertical position */
/* the following table shows how the markers */
/* vertical position worked in hardware  */
/*  marker_y  lines    marker_y  lines   */
/*     0      0 + 1       8      10 + 11 */
/*     1      2 + 3       9      12 + 13 */
/*     2      4 + 5      10      14 + 15 */
/*     3      6 + 7      11      16 + 17 */
/*     4      8 + 9      12      18 + 19 */
static  int vert_scale(int data)
{
	return ((data & 0x07)<<1) + ((data & 0xf8)>>3) * VERT_CHR;
}

/* return the (overlay) color for coord x, y */
static  int x_y_color(int x, int y)
{
int color = 2;
    if (overlay)
	{
			/* left mustard yellow, right jade green */
			color  = (x < 16*HORZ_CHR) ? 0 : 1;
			/* but swapped in first and last lines */
			if ((y < 1*VERT_CHR) || (y > 22*VERT_CHR-1))
			color ^= 1;
	}
	if (video_inverted)
	{
		color += 3;
	}
	return color;
}

/* mark the character occupied by the marker dirty */
void    lazercmd_marker_dirty(int marker)
{
int x, y;
	{
		x = marker_x - 1;             /* normal video lags marker by 1 pixel */
		y = vert_scale(marker_y) - VERT_CHR; /* first line used as scratch pad */
    }
	if (x < 0 || x >= HORZ_RES * HORZ_CHR)
		return;
	if (y < 0 || y >= VERT_RES * VERT_CHR)
        return;
	/* mark all occupied character positions dirty */
    dirtybuffer[(y+0)/VERT_CHR * HORZ_RES + (x+0)/HORZ_CHR] = 1;
	dirtybuffer[(y+3)/VERT_CHR * HORZ_RES + (x+0)/HORZ_CHR] = 1;
	dirtybuffer[(y+0)/VERT_CHR * HORZ_RES + (x+3)/HORZ_CHR] = 1;
	dirtybuffer[(y+3)/VERT_CHR * HORZ_RES + (x+3)/HORZ_CHR] = 1;
}

/* plot a bitmap marker */
/* hardware has 2 marker sizes 2x2 and 4x2 selected by jumper */
/* meadows lanes normaly use 2x2 pixels and lazer command uses either */
static  void plot_pattern(int x, int y)
{
int xbit, ybit, size;
    size = 2;
	if (input_port_2_r(0) & 0x40)
    {
		size = 4;
    }
	for (ybit = 0; ybit < 2; ybit++)
	{
	    if (y+ybit < 0 || y+ybit >= VERT_RES * VERT_CHR)
		    return;
	    for (xbit = 0; xbit < size; xbit++)
		{
			if (x+xbit < 0 || x+xbit >= HORZ_RES * HORZ_CHR)
				continue;
				plot_pixel(tmpbitmap, x+xbit, y+ybit, Machine->pens[x_y_color(x+xbit,y+ybit)+6]);
		}
	}
}

void lazercmd_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
int     i;

        if (overlay != (input_port_2_r(0) & 0x80))
        {
                overlay = input_port_2_r(0) & 0x80;
                memset(dirtybuffer, 1, videoram_size);
        }

        if (video_inverted != (input_port_2_r(0) & 0x20))
        {
                video_inverted = input_port_2_r(0) & 0x20;
                memset(dirtybuffer, 1, videoram_size);
        }

        /* The first row of characters are invisible */
		for (i = 0; i < (VERT_RES - 1) * HORZ_RES; i++)
        {
                if (dirtybuffer[i])
                {
				int 	x, y;

                        dirtybuffer[i] = 0;

                        x = i % HORZ_RES;
                        y = i / HORZ_RES;

                        x *= HORZ_CHR;
                        y *= VERT_CHR;

                        drawgfx(tmpbitmap,
                                Machine->gfx[0],
								videoram[i],
								x_y_color(x,y),
                                0,0, x,y,
                                &Machine->drv->visible_area,
                                TRANSPARENCY_NONE,0);
                }
        }
		{
		int x, y;
			x = marker_x - 1;             /* normal video lags marker by 1 pixel */
			y = vert_scale(marker_y) - VERT_CHR; /* first line used as scratch pad */
			plot_pattern(x,y);
        }

		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
}

