/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

//#define DEBUG_SPRITES

#ifdef DEBUG_SPRITES
#include <stdio.h>
FILE	*sprite_log;
#endif


static int char_palette,sprite_palette;
static int char_bank;


/***************************************************************************

  Convert the color PROMs into a more useable format.

  The palette PROM is connected to the RGB output this way:

  bit 7 -- 220 ohm resistor  -- BLUE
        -- 470 ohm resistor  -- BLUE
        -- 220 ohm resistor  -- GREEN
        -- 470 ohm resistor  -- GREEN
        -- 1  kohm resistor  -- GREEN
        -- 220 ohm resistor  -- RED
        -- 470 ohm resistor  -- RED
  bit 0 -- 1  kohm resistor  -- RED

***************************************************************************/
void exerion_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])


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

	/* color_prom now points to the beginning of the char lookup table */

	/* fg chars */
	for (i = 0;i < TOTAL_COLORS(0);i++)
	{
		COLOR(0,i) = (color_prom[(i / 64) * 64 + ((i / 4) & 0x0f) + (i % 4) * 16] & 0x0f) + 0x10;
	}
	color_prom += 256;

	/* color_prom now points to the beginning of the sprite lookup table */

	/* sprites */
	for (i = 0;i < TOTAL_COLORS(2);i++)
	{
		COLOR(2,i) = (color_prom[(i / 64) * 64 + ((i / 4) & 0x0f) + (i % 4) * 16] & 0x0f) + 0x10;
	}
	color_prom += 256;

	/* bg chars (this is not the full story... there are four layers mixed */
	/* using another PROM */
	for (i = 0;i < TOTAL_COLORS(1);i++)
	{
		COLOR(1,i) = *(color_prom++) & 0x0f;
	}
}



int exerion_vh_start (void)
{
#ifdef DEBUG_SPRITES
	sprite_log = fopen ("sprite.log","w");
#endif
	return generic_vh_start();
}

void exerion_vh_stop (void)
{
#ifdef DEBUG_SPRITES
	fclose (sprite_log);
#endif
	generic_vh_stop();
}


void exerion_videoreg_w (int offset,int data)
{
	/* bit 0 = flip screen and joystick input multiplexor */

	/* bits 1-2 char lookup table bank */
	char_palette = (data & 0x06) >> 1;

	/* bits 3 char bank */
	char_bank = (data & 0x08) >> 3;

	/* bits 4-5 unused */

	/* bits 6-7 sprite lookup table bank */
	sprite_palette = (data & 0xc0) >> 6;
}


/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void exerion_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int sx,sy,offs,i;


	fillbitmap(bitmap,Machine->pens[0],&Machine->drv->visible_area);

#ifdef DEBUG_SPRITES
	if (sprite_log)
	{
		int i;

		for (i = 0; i < spriteram_size; i+= 4)
		{
			if (spriteram[i+2] == 0x02)
			{
				fprintf (sprite_log, "%02x %02x %02x %02x\n",spriteram[i], spriteram[i+1], spriteram[i+2], spriteram[i+3]);
			}
		}
	}
#endif

	/* draw sprites */
	for (i=0; i < spriteram_size; i+=4)
	{
		int x, y, s, s2;
		int xflip, yflip, wide;
		int doubled;
		int color;

		x = spriteram[i+3]*2 + 64 + 8;	/* ??? */
		y = 255 - spriteram[i+1];
		s = spriteram[i+2];
		/* decode the sprite number */
		s = s2 = ((s & 0x07) << 5) | ((s & 0xf0) >> 4) | ((s & 0x08) << 1);
		xflip = spriteram[i] & 0x80;
		yflip = spriteram[i] & 0x40;
		wide = spriteram[i] & 0x08;
		doubled = spriteram[i] & 0x10;

		color = ((spriteram[i] & 0x06) >> 1) + ((spriteram[i+2] & 0x80) >> 5) +
				(spriteram[i+2] & 0x08) + sprite_palette * 16;

		if (wide)
		{
			if (yflip)
				s++;
			else
				s2++;

			if (doubled)
			{
				drawgfx(bitmap,Machine->gfx[3],
						s2,
						color,
						xflip,yflip,
						x,y+32,
						0, TRANSPARENCY_PEN,0);
			}
			else
			{
				drawgfx(bitmap,Machine->gfx[2],
						s2,
						color,
						xflip,yflip,
						x,y+16,
						0, TRANSPARENCY_PEN,0);
			}
		}

		if (doubled)
		{
			drawgfx(bitmap,Machine->gfx[3],
					s,
					color,
					xflip,yflip,
					x,y,
					0, TRANSPARENCY_PEN,0);
		}
		else
		{
			drawgfx(bitmap,Machine->gfx[2],
					s,
					color,
					xflip,yflip,
					x,y,
					0, TRANSPARENCY_PEN,0);
		}

		if (doubled) i += 4;
	}


	for (offs = videoram_size - 1;offs >= 0;offs--)
	{
		sx = offs % 64;
		sy = offs / 64;

		drawgfx(bitmap,Machine->gfx[0],
			videoram[offs] + 256 * char_bank,
			((videoram[offs] & 0xf0) >> 4) + char_palette * 16,
			0,0,
			8*sx,8*sy,
			0,TRANSPARENCY_PEN,0);
	}
}
