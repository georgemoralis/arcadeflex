/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

extern unsigned char *grobda_spriteram;
static int flipscreen;


/***************************************************************************

  Convert the color PROMs into a more useable format.

  Grodba has one 32x8 palette PROM and two 256x4 color lookup table PROMs
  (one for characters, one for sprites).
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
void grobda_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;

	for (i = 0;i < 32;i++)
	{
		int bit0,bit1,bit2;

		bit0 = (color_prom[i] >> 0) & 0x01;
		bit1 = (color_prom[i] >> 1) & 0x01;
		bit2 = (color_prom[i] >> 2) & 0x01;
		palette[3*i] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		bit0 = (color_prom[i] >> 3) & 0x01;
		bit1 = (color_prom[i] >> 4) & 0x01;
		bit2 = (color_prom[i] >> 5) & 0x01;
		palette[3*i + 1] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		bit0 = 0;
		bit1 = (color_prom[i] >> 6) & 0x01;
		bit2 = (color_prom[i] >> 7) & 0x01;
		palette[3*i + 2] = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	}
	/* characters */
	for (i = 0; i < 256; i++)
		colortable[i] = (0x1f - (color_prom[i + 32] & 0x0f));
	/* sprites */
	for (i = 256; i < 512; i++)
		colortable[i] = (color_prom[i + 32] & 0x0f);
}



int grobda_vh_start( void )
{
	/* set up spriteram area */
	spriteram_size = 0x80;
	spriteram = &grobda_spriteram[0x780];
	spriteram_2 = &grobda_spriteram[0x780+0x800];
	spriteram_3 = &grobda_spriteram[0x780+0x800+0x800];

	return generic_vh_start();
}

void grobda_vh_stop( void )
{
	generic_vh_stop();
}


/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/

void grobda_draw_sprite(struct osd_bitmap *dest,unsigned int code,unsigned int color,
	int flipx,int flipy,int sx,int sy)
{
	drawgfx(dest,Machine->gfx[1],code,color,flipx,flipy,sx,sy,&Machine->drv->visible_area,
		TRANSPARENCY_PEN,0);
}

void grobda_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
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
				if (mx < 2 || mx >= 30) continue; /* not visible */
				sx = my + 34;
				sy = mx - 2;
			}
			else if (my >= 30)
			{
				if (mx < 2 || mx >= 30) continue; /* not visible */
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


	/* Draw the sprites. */
	for (offs = 0;offs < spriteram_size;offs += 2)
	{
		/* is it on? */
		if ((spriteram_3[offs+1] & 2) == 0)
		{
			int sprite = spriteram[offs];
			int color = spriteram[offs+1];
			int x = (spriteram_2[offs+1]-40) + 0x100*(spriteram_3[offs+1] & 1);
			int y = 28*8-spriteram_2[offs];
			int flipx = spriteram_3[offs] & 1;
			int flipy = spriteram_3[offs] & 2;

			switch (spriteram_3[offs] & 0x0c)
			{
				case 0:		/* normal size */
					grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
					break;

				case 4:		/* 2x horizontal */
					sprite &= ~1;
					if (!flipx)
					{
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x+16,y);
					}
					else
					{
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x+16,y);
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x,y);
					}
					break;

				case 8:		/* 2x vertical */
					sprite &= ~2;
					if (!flipy)
					{
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y-16);
					}
					else
					{
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y-16);
					}
					break;

				case 12:		/* 2x both ways */
					sprite &= ~3;
					if (!flipx && !flipy)
					{
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x+16,y);
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y-16);
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x+16,y-16);
					}
					else if (flipx && flipy)
					{
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x+16,y);
						grobda_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x,y-16);
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x+16,y-16);
					}
					else if (flipy)
					{
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x+16,y);
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x,y-16);
						grobda_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x+16,y-16);
					}
					else /* flipx */
					{
						grobda_draw_sprite(bitmap,3+sprite,color,flipx,flipy,x,y);
						grobda_draw_sprite(bitmap,2+sprite,color,flipx,flipy,x+16,y);
						grobda_draw_sprite(bitmap,1+sprite,color,flipx,flipy,x,y-16);
						grobda_draw_sprite(bitmap,sprite,color,flipx,flipy,x+16,y-16);
					}
					break;
			}
		}
	}
}
