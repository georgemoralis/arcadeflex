/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


void rastan_vh_stop (void);

int rastan_videoram_size;
unsigned char *rastan_videoram1;
unsigned char *rastan_videoram3;
unsigned char *rastan_spriteram;
unsigned char *rastan_scrollx;
unsigned char *rastan_scrolly;

static unsigned char *rastan_dirty1;
static unsigned char *rastan_dirty3;

static unsigned char spritepalettebank;

static struct osd_bitmap *tmpbitmap1;
static struct osd_bitmap *tmpbitmap3;



int rastan_vh_start (void)
{
	/* Allocate a video RAM */
	rastan_dirty1 = malloc ( rastan_videoram_size/4);
	if (!rastan_dirty1)
	{
		rastan_vh_stop();
		return 1;
	}
	memset(rastan_dirty1,1,rastan_videoram_size / 4);

	rastan_dirty3 = malloc ( rastan_videoram_size/4);
	if (!rastan_dirty3)
	{
		rastan_vh_stop();
		return 1;
	}
	memset(rastan_dirty3,1,rastan_videoram_size / 4);

	/* Allocate temporary bitmaps */
 	if ((tmpbitmap1 = osd_create_bitmap(512,512)) == 0)
	{
		rastan_vh_stop ();
		return 1;
	}
	if ((tmpbitmap3 = osd_create_bitmap(512,512)) == 0)
	{
		rastan_vh_stop ();
		return 1;
	}

	return 0;
}



void rastan_vh_stop (void)
{
	/* Free temporary bitmaps */
	if (tmpbitmap3)
		osd_free_bitmap (tmpbitmap3);
	tmpbitmap3 = 0;
	if (tmpbitmap1)
		osd_free_bitmap (tmpbitmap1);
	tmpbitmap1 = 0;

	/* Free video RAM */
	if (rastan_dirty1)
	        free (rastan_dirty1);
	if (rastan_dirty3)
	        free (rastan_dirty3);
	rastan_dirty1 = rastan_dirty3 = 0;
}



/*
 *   scroll write handlers
 */

void rastan_scrollY_w (int offset, int data)
{
	COMBINE_WORD_MEM (&rastan_scrolly[offset], data);
}

void rastan_scrollX_w (int offset, int data)
{
	COMBINE_WORD_MEM (&rastan_scrollx[offset], data);
}



void rastan_videoram1_w (int offset, int data)
{
	int oldword = READ_WORD(&rastan_videoram1[offset]);
	int newword = COMBINE_WORD(oldword,data);


	if (oldword != newword)
	{
		WRITE_WORD(&rastan_videoram1[offset],newword);
		rastan_dirty1[offset / 4] = 1;
	}
}
int rastan_videoram1_r (int offset)
{
   return READ_WORD (&rastan_videoram1[offset]);
}

void rastan_videoram3_w (int offset, int data)
{
	int oldword = READ_WORD(&rastan_videoram3[offset]);
	int newword = COMBINE_WORD(oldword,data);


	if (oldword != newword)
	{
		WRITE_WORD(&rastan_videoram3[offset],newword);
		rastan_dirty3[offset / 4] = 1;
	}
}
int rastan_videoram3_r (int offset)
{
   return READ_WORD (&rastan_videoram3[offset]);
}



void rastan_videocontrol_w (int offset, int data)
{
	if (offset == 0)
	{
		/* bits 2 and 3 are the coin counters */

		/* bits 5-7 look like the sprite palette bank */
		spritepalettebank = (data & 0xe0) >> 5;

		/* other bits unknown */
	}
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
void rastan_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	int scrollx,scrolly;


palette_init_used_colors();

{
	int color,code,i;
	int colmask[128];
	int pal_base;


	pal_base = 0;

	for (color = 0;color < 128;color++) colmask[color] = 0;

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		code = READ_WORD(&rastan_videoram1[offs + 2]) & 0x3fff;
		color = READ_WORD(&rastan_videoram1[offs]) & 0x7f;

		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		code = READ_WORD(&rastan_videoram3[offs + 2]) & 0x3fff;
		color = READ_WORD(&rastan_videoram3[offs]) & 0x7f;

		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (offs = 0x800-8; offs >= 0; offs -= 8)
	{
		code = READ_WORD (&rastan_spriteram[offs+4]);

		if (code)
		{
			int data1;

			data1 = READ_WORD (&rastan_spriteram[offs]);

			color = (data1 & 0x0f) + 0x10 * spritepalettebank;
			colmask[color] |= Machine->gfx[1]->pen_usage[code];
		}
	}

	for (color = 0;color < 128;color++)
	{
		if (colmask[color] & (1 << 0))
			palette_used_colors[pal_base + 16 * color] = PALETTE_COLOR_TRANSPARENT;
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}


	if (palette_recalc())
	{
		memset(rastan_dirty1,1,rastan_videoram_size / 4);
		memset(rastan_dirty3,1,rastan_videoram_size / 4);
	}
}


	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty1[offs/4])
		{
			int sx,sy;
			int data1,data2;


			rastan_dirty1[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram1[offs]);
			data2 = READ_WORD(&rastan_videoram1[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

			drawgfx(tmpbitmap1, Machine->gfx[0],
					data2 & 0x3fff,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty3[offs/4])
		{
			int sx,sy;
			int data1,data2;


			rastan_dirty3[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram3[offs]);
			data2 = READ_WORD(&rastan_videoram3[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

			drawgfx(tmpbitmap3, Machine->gfx[0],
					data2 & 0x3fff,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	scrollx = READ_WORD(&rastan_scrollx[0]) - 16;
	scrolly = READ_WORD(&rastan_scrolly[0]);
	copyscrollbitmap(bitmap,tmpbitmap1,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	scrollx = READ_WORD(&rastan_scrollx[2]) - 16;
	scrolly = READ_WORD(&rastan_scrolly[2]);
	copyscrollbitmap(bitmap,tmpbitmap3,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);


	/* Draw the sprites. 256 sprites in total */
	for (offs = 0x800-8; offs >= 0; offs -= 8)
	{
		int num = READ_WORD (&rastan_spriteram[offs+4]);


		if (num)
		{
			int sx,sy,col,data1;

			sx = READ_WORD(&rastan_spriteram[offs+6]) & 0x1ff;
			if (sx > 400) sx = sx - 512;
			sy = READ_WORD(&rastan_spriteram[offs+2]) & 0x1ff;
			if (sy > 400) sy = sy - 512;

			data1 = READ_WORD (&rastan_spriteram[offs]);

			col = (data1 & 0x0f) + 0x10 * spritepalettebank;

			drawgfx(bitmap,Machine->gfx[1],
					num,
					col,
					data1 & 0x4000, data1 & 0x8000,
					sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
	}
}

void rainbow_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	int scrollx,scrolly;


palette_init_used_colors();

/* TODO: we are using the same table for background and foreground tiles, but this */
/* causes the sky to be black instead of blue. */
{
	int color,code,i;
	int colmask[128];
	int pal_base;


	pal_base = 0;

	for (color = 0;color < 128;color++)
	{
		colmask[color] = 0;
    }

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		code = READ_WORD(&rastan_videoram1[offs + 2]) & 0x3FFF;
		color = READ_WORD(&rastan_videoram1[offs]) & 0x7f;

		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		code = READ_WORD(&rastan_videoram3[offs + 2]) & 0x3fff;
		color = READ_WORD(&rastan_videoram3[offs]) & 0x7f;

		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (offs = 0x800-8; offs >= 0; offs -= 8)
	{
		code = READ_WORD (&rastan_spriteram[offs+4]);

		if (code)
		{
			int data1;

			data1 = READ_WORD (&rastan_spriteram[offs]);

			color = (data1 + 0x10) & 0x7f;

            if(code < 4096)
				colmask[color] |= Machine->gfx[1]->pen_usage[code];
            else
				colmask[color] |= Machine->gfx[2]->pen_usage[code-4096];
		}
	}

	for (color = 0;color < 128;color++)
	{
		if (colmask[color] & (1 << 0))
			palette_used_colors[pal_base + 16 * color] = PALETTE_COLOR_USED;

		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

    /* Make one transparent colour */

    palette_used_colors[pal_base] = PALETTE_COLOR_TRANSPARENT;

	if (palette_recalc())
	{
		memset(rastan_dirty1,1,rastan_videoram_size / 4);
		memset(rastan_dirty3,1,rastan_videoram_size / 4);
	}
}


	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty1[offs/4])
		{
			int sx,sy;
			int data1,data2;


			rastan_dirty1[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram1[offs]);
			data2 = READ_WORD(&rastan_videoram1[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

			drawgfx(tmpbitmap1, Machine->gfx[0],
					data2 & 0x3fff,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty3[offs/4])
		{
			int sx,sy;
			int data1,data2;


			rastan_dirty3[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram3[offs]);
			data2 = READ_WORD(&rastan_videoram3[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

            /* Colour as Transparent */

			drawgfx(tmpbitmap3, Machine->gfx[0],
					0,
					0,
					0, 0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);

            /* Draw over with correct Transparency */

			drawgfx(tmpbitmap3, Machine->gfx[0],
					data2 & 0x3fff,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_PEN,0);
		}
	}

	scrollx = READ_WORD(&rastan_scrollx[0]) - 16;
	scrolly = READ_WORD(&rastan_scrolly[0]);
	copyscrollbitmap(bitmap,tmpbitmap1,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	/* Draw the sprites. 256 sprites in total */
	for (offs = 0x800-8; offs >= 0; offs -= 8)
	{
		int num = READ_WORD (&rastan_spriteram[offs+4]);

		if (num)
		{
			int sx,sy,col,data1;

			sx = READ_WORD(&rastan_spriteram[offs+6]) & 0x1ff;
			if (sx > 400) sx = sx - 512;
			sy = READ_WORD(&rastan_spriteram[offs+2]) & 0x1ff;
			if (sy > 400) sy = sy - 512;

			data1 = READ_WORD (&rastan_spriteram[offs]);

			col = (data1 + 0x10) & 0x7f;

            if(num < 4096)
			    drawgfx(bitmap,Machine->gfx[1],
					    num,
					    col,
					    data1 & 0x4000, data1 & 0x8000,
					    sx,sy,
					    &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
            else
			    drawgfx(bitmap,Machine->gfx[2],
					    num-4096,
					    col,
					    data1 & 0x4000, data1 & 0x8000,
					    sx,sy,
					    &Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
	}

	scrollx = READ_WORD(&rastan_scrollx[2]) - 16;
	scrolly = READ_WORD(&rastan_scrolly[2]);
	copyscrollbitmap(bitmap,tmpbitmap3,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);


}


/* Jumping uses different sprite controller   */
/* than rainbow island. - values are remapped */
/* at address 0x2EA in the code. Apart from   */
/* physical layout, the main change is that   */
/* the Y settings are active low              */

void jumping_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
	int scrollx,scrolly;

    palette_init_used_colors();

    /* TODO: we are using the same table for background and foreground tiles, but this */
    /* causes the sky to be black instead of blue. */
    {
	    int color,code,i;
	    int colmask[128];
    	int pal_base;

	    pal_base = 0;

	    for (color = 0;color < 128;color++) colmask[color] = 0;

	    for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	    {
		    code = READ_WORD(&rastan_videoram1[offs + 2]) & 0x3FFF;
		    color = READ_WORD(&rastan_videoram1[offs]) & 0x7f;

		    colmask[color] |= Machine->gfx[0]->pen_usage[code];
	    }

	    for (offs = 0x800-8; offs >= 0; offs -= 8)
	    {
		    code = READ_WORD (&rastan_spriteram[offs]);

		    if (code < Machine->gfx[1]->total_elements)
		    {
			    int data1;

			    data1 = READ_WORD (&rastan_spriteram[offs+8]);

			    color = (data1 + 0x10) & 0x7f;
			    colmask[color] |= Machine->gfx[1]->pen_usage[code];
		    }
	    }

	    for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	    {
		    code = READ_WORD(&rastan_videoram3[offs + 2]) & 0x3FFF;
		    color = READ_WORD(&rastan_videoram3[offs]) & 0x7f;

		    colmask[color] |= Machine->gfx[0]->pen_usage[code];
	    }


	    for (color = 0;color < 128;color++)
	    {
		    if (colmask[color] & (1 << 15))
			    palette_used_colors[pal_base + 16 * color + 15] = PALETTE_COLOR_USED;

		    for (i = 0;i < 15;i++)
		    {
			    if (colmask[color] & (1 << i))
				    palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		    }
	    }

        /* Make one transparent colour */

        palette_used_colors[pal_base + 15] = PALETTE_COLOR_TRANSPARENT;

	    if (palette_recalc())
	    {
		    memset(rastan_dirty1,1,rastan_videoram_size / 4);
		    memset(rastan_dirty3,1,rastan_videoram_size / 4);
	    }

    }

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty1[offs/4])
		{
			int sx,sy;
			int data1,data2;

			rastan_dirty1[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram1[offs]);
			data2 = READ_WORD(&rastan_videoram1[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

			drawgfx(tmpbitmap1, Machine->gfx[0],
					data2,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	for (offs = rastan_videoram_size - 4;offs >= 0;offs -= 4)
	{
		if (rastan_dirty3[offs/4])
		{
			int sx,sy;
			int data1,data2;


			rastan_dirty3[offs/4] = 0;

			data1 = READ_WORD(&rastan_videoram3[offs]);
			data2 = READ_WORD(&rastan_videoram3[offs + 2]);

			sx = (offs/4) % 64;
			sy = (offs/4) / 64;

            /* Colour as Transparent */

			drawgfx(tmpbitmap3, Machine->gfx[0],
					0,
					0,
					0, 0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);

            /* Draw over with correct Transparency */

			drawgfx(tmpbitmap3, Machine->gfx[0],
					data2,
					data1 & 0x7f,
					data1 & 0x4000, data1 & 0x8000,
					8*sx,8*sy,
					0,TRANSPARENCY_PEN,15);
		}
	}

	scrollx = READ_WORD(&rastan_scrollx[0]) - 16;
	scrolly = -READ_WORD(&rastan_scrolly[0]);
	copyscrollbitmap(bitmap,tmpbitmap1,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);

	/* Draw the sprites. 128 sprites in total */

	for (offs = 0x07F0; offs >= 0; offs -= 16)
	{
		int num = READ_WORD (&rastan_spriteram[offs]);

		if (num)
		{
			int  sx,col,data1;
            int sy;

			sy = ((READ_WORD(&rastan_spriteram[offs+2]) - 0xFFF1) ^ 0xFFFF) & 0x1FF;
  			if (sy > 400) sy = sy - 512;
			sx = (READ_WORD(&rastan_spriteram[offs+4]) - 0x38) & 0x1ff;
			if (sx > 400) sx = sx - 512;

			data1 = READ_WORD(&rastan_spriteram[offs+6]);
			col   = (READ_WORD(&rastan_spriteram[offs+8]) + 0x10) & 0x7F;

			drawgfx(bitmap,Machine->gfx[1],
					num,
					col,
					data1 & 0x40, data1 & 0x80,
					sx,sy+1,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,15);
		}
	}

	scrollx = - 16;
	scrolly = 0;
  	copyscrollbitmap(bitmap,tmpbitmap3,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
}
