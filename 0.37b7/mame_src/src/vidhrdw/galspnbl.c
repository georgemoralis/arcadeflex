#include "driver.h"
#include "vidhrdw/generic.h"


unsigned char *galspnbl_bgvideoram;
static int screenscroll;



void galspnbl_init_palette(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
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



READ_HANDLER( galspnbl_bgvideoram_r )
{
	return READ_WORD(&galspnbl_bgvideoram[offset]);
}

WRITE_HANDLER( galspnbl_bgvideoram_w )
{
	int sx,sy,color;


	COMBINE_WORD_MEM(&galspnbl_bgvideoram[offset],data);

	sx = (offset/2) % 512;
	sy = (offset/2) / 512;

	color = READ_WORD(&galspnbl_bgvideoram[offset]);

	plot_pixel(tmpbitmap,sx,sy,Machine->pens[1024 + (color >> 1)]);
}

WRITE_HANDLER( galspnbl_scroll_w )
{
	if ((data & 0x00ff0000) == 0)
		screenscroll = 4-(data & 0xff);
}



/* sprite format (see also Ninja Gaiden):
 *
 *	word		bit					usage
 * --------+-fedcba9876543210-+----------------
 *    0    | ---------------x | flip x
 *         | --------------x- | flip y
 *         | -------------x-- | enable
 *         | ----------xx---- | priority?
 *         | ---------x------ | flicker?
 *    1    | xxxxxxxxxxxxxxxx | code
 *    2    | --------xxxx---- | color
 *         | --------------xx | size: 8x8, 16x16, 32x32, 64x64
 *    3    | xxxxxxxxxxxxxxxx | y position
 *    4    | xxxxxxxxxxxxxxxx | x position
 *    5,6,7|                  | unused
 */
static void draw_sprites(struct osd_bitmap *bitmap,int priority)
{
	int offs;
	const UINT8 layout[8][8] =
	{
		{0,1,4,5,16,17,20,21},
		{2,3,6,7,18,19,22,23},
		{8,9,12,13,24,25,28,29},
		{10,11,14,15,26,27,30,31},
		{32,33,36,37,48,49,52,53},
		{34,35,38,39,50,51,54,55},
		{40,41,44,45,56,57,60,61},
		{42,43,46,47,58,59,62,63}
	};

	for (offs = spriteram_size-16;offs >= 0;offs -= 16)
	{
		int sx,sy,code,color,size,attr,flipx,flipy;
		int col,row;

		attr = READ_WORD(&spriteram[offs]);
		if ((attr & 0x0004) && ((attr & 0x0040) == 0 || (cpu_getcurrentframe() & 1))
//				&& ((attr & 0x0030) >> 4) == priority)
				&& ((attr & 0x0020) >> 5) == priority)
		{
			code = READ_WORD(&spriteram[offs+2]);
			color = READ_WORD(&spriteram[offs+4]);
			size = 1 << (color & 0x0003); // 1,2,4,8
			color = (color & 0x00f0) >> 4;
			sx = READ_WORD(&spriteram[offs+8]) + screenscroll;
			sy = READ_WORD(&spriteram[offs+6]);
			flipx = attr & 0x0001;
			flipy = attr & 0x0002;

			for (row = 0;row < size;row++)
			{
				for (col = 0;col < size;col++)
				{
					int x = sx + 8*(flipx?(size-1-col):col);
					int y = sy + 8*(flipy?(size-1-row):row);
					drawgfx(bitmap,Machine->gfx[1],
						code + layout[row][col],
						color,
						flipx,flipy,
						x,y,
						&Machine->visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
}


void galspnbl_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	palette_recalc();

	/* copy the temporary bitmap to the screen */
	/* it's raw RGB, so it doesn't have to be recalculated even if palette_recalc() */
	/* returns true */
	copyscrollbitmap(bitmap,tmpbitmap,1,&screenscroll,0,0,&Machine->visible_area,TRANSPARENCY_NONE,0);

	draw_sprites(bitmap,0);

	for (offs = 0;offs < 0x1000;offs += 2)
	{
		int sx,sy,code,attr,color;

		code = READ_WORD(&videoram[offs]);
		attr = READ_WORD(&colorram[offs]);
		color = (attr & 0x00f0) >> 4;
		sx = (offs/2) % 64;
		sy = (offs/2) / 64;

		/* What is this? A priority/half transparency marker? */
		if (!(attr & 0x0008))
		{
			drawgfx(bitmap,Machine->gfx[0],
					code,
					color,
					0,0,
					16*sx + screenscroll,8*sy,
					&Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	}

	draw_sprites(bitmap,1);
}
