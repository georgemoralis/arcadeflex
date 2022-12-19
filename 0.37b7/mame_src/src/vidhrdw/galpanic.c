#include "driver.h"
#include "vidhrdw/generic.h"



unsigned char *galpanic_bgvideoram,*galpanic_fgvideoram;
size_t galpanic_fgvideoram_size;



void galpanic_init_palette(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;

	palette += 3*1024;	/* first 1024 colors are dynamic */

	/* initialize 555 RGB lookup */
	for (i = 0;i < 32768;i++)
	{
		int r,g,b;

		r = (i >>  5) & 0x1f;
		g = (i >> 10) & 0x1f;
		b = (i >>  0) & 0x1f;

		(*palette++) = (r << 3) | (r >> 2);
		(*palette++) = (g << 3) | (g >> 2);
		(*palette++) = (b << 3) | (b >> 2);
	}
}



READ_HANDLER( galpanic_bgvideoram_r )
{
	return READ_WORD(&galpanic_bgvideoram[offset]);
}

WRITE_HANDLER( galpanic_bgvideoram_w )
{
	int sx,sy,color;


	COMBINE_WORD_MEM(&galpanic_bgvideoram[offset],data);

	sy = (offset/2) / 256;
	sx = (offset/2) % 256;

	color = READ_WORD(&galpanic_bgvideoram[offset]);

	plot_pixel(tmpbitmap, sx, sy, Machine->pens[1024 + (color >> 1)]);
}

READ_HANDLER( galpanic_fgvideoram_r )
{
	return READ_WORD(&galpanic_fgvideoram[offset]);
}

WRITE_HANDLER( galpanic_fgvideoram_w )
{
	COMBINE_WORD_MEM(&galpanic_fgvideoram[offset],data);
}

READ_HANDLER( galpanic_paletteram_r )
{
	return READ_WORD(&paletteram[offset]);
}

WRITE_HANDLER( galpanic_paletteram_w )
{
	int r,g,b;
	int oldword = READ_WORD(&paletteram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	WRITE_WORD(&paletteram[offset],newword);

	r = (newword >>  6) & 0x1f;
	g = (newword >> 11) & 0x1f;
	b = (newword >>  1) & 0x1f;
	/* bit 0 seems to be a transparency flag for the front bitmap */

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(offset / 2,r,g,b);
}

READ_HANDLER( galpanic_spriteram_r )
{
	return READ_WORD(&spriteram[offset]);
}

WRITE_HANDLER( galpanic_spriteram_w )
{
	COMBINE_WORD_MEM(&spriteram[offset],data);
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/

static void galpanic_draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;
	int sx,sy;

	sx = sy = 0;
	for (offs = 0;offs < spriteram_size;offs += 0x10)
	{
		int x,y,code,color,flipx,flipy,attr1,attr2;

		attr1 = READ_WORD(&spriteram[offs + 6]);
		x = READ_WORD(&spriteram[offs + 8]) - ((attr1 & 0x01) << 8);
		y = READ_WORD(&spriteram[offs + 10]) + ((attr1 & 0x02) << 7);
		if (attr1 & 0x04)	/* multi sprite */
		{
			sx += x;
			sy += y;
		}
		else
		{
			sx = x;
			sy = y;
		}

		color = (attr1 & 0xf0) >> 4;

		/* bit 0 [offs + 0] is used but I don't know what for */

		attr2 = READ_WORD(&spriteram[offs + 14]);
		code = READ_WORD(&spriteram[offs + 12]) + ((attr2 & 0x1f) << 8);
		flipx = attr2 & 0x80;
		flipy = attr2 & 0x40;

		drawgfx(bitmap,Machine->gfx[0],
				code,
				color,
				flipx,flipy,
				sx,sy - 16,
				&Machine->visible_area,TRANSPARENCY_PEN,0);
	}
}

static void comad_draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;

	for (offs = 0;offs < spriteram_size;offs += 8)
	{
		int sx,sy,code,color,flipx,flipy;

		sx = READ_WORD(&spriteram[offs + 4]) >> 6;
		sy = READ_WORD(&spriteram[offs + 6]) >> 6;
		code = READ_WORD(&spriteram[offs + 2]);
		color = (READ_WORD(&spriteram[offs]) & 0x003c) >> 2;
		flipx = READ_WORD(&spriteram[offs]) & 0x0002;
		flipy = READ_WORD(&spriteram[offs]) & 0x0001;

		drawgfx(bitmap,Machine->gfx[0],
				code,
				color,
				flipx,flipy,
				sx,sy,
				&Machine->visible_area,TRANSPARENCY_PEN,0);
	}
}

static void draw_fgbitmap(struct osd_bitmap *bitmap)
{
	int offs;

	for (offs = 0;offs < galpanic_fgvideoram_size;offs+=2)
	{
		int sx,sy,color;

		sx = (offs/2) % 256;
		sy = (offs/2) / 256;
		color = READ_WORD(&galpanic_fgvideoram[offs]);
		if (color)
		{
			plot_pixel(bitmap, sx, sy, Machine->pens[color]);
		}
	}
}

void galpanic_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	palette_recalc();

	/* copy the temporary bitmap to the screen */
	/* it's raw RGB, so it doesn't have to be recalculated even if palette_recalc() */
	/* returns true */
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);

	draw_fgbitmap(bitmap);

	galpanic_draw_sprites(bitmap);
}

void comad_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	palette_recalc();

	/* copy the temporary bitmap to the screen */
	/* it's raw RGB, so it doesn't have to be recalculated even if palette_recalc() */
	/* returns true */
	copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);

	draw_fgbitmap(bitmap);

	comad_draw_sprites(bitmap);
}
