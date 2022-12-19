/***************************************************************************

	Star Fire video system

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"


/* from the main driver */
extern UINT8 *starfire_videoram;
extern UINT8 *starfire_colorram;

/* local allocated storage */
static UINT8 *scanline_dirty;

static UINT8 starfire_vidctrl;
static UINT8 starfire_vidctrl1;
static UINT8 starfire_color;



/*************************************
 *
 *	Initialize the video system
 *
 *************************************/

int starfire_vh_start(void)
{
	/* make a temporary bitmap */
	tmpbitmap = bitmap_alloc(Machine->drv->screen_width, Machine->drv->screen_height);
	if (!tmpbitmap)
		return 1;

	/* make a dirty array */
	scanline_dirty = malloc(256);
	if (!scanline_dirty)
	{
		bitmap_free(tmpbitmap);
		return 1;
	}

	/* reset videoram */
	memset(starfire_videoram, 0, 0x2000);
	memset(starfire_colorram, 0, 0x2000);
	memset(scanline_dirty, 1, 256);

	return 0;
}



/*************************************
 *
 *	Tear down the video system
 *
 *************************************/

void starfire_vh_stop(void)
{
	bitmap_free(tmpbitmap);
	free(scanline_dirty);
}



/*************************************
 *
 *	Video control writes
 *
 *************************************/

WRITE_HANDLER( starfire_vidctrl_w )
{
    starfire_vidctrl = data;
}

WRITE_HANDLER( starfire_vidctrl1_w )
{
    starfire_vidctrl1 = data;
}



/*************************************
 *
 *	Color RAM read/writes
 *
 *************************************/

WRITE_HANDLER( starfire_colorram_w )
{
	/* handle writes to the pseudo-color RAM */
	if ((offset & 0xe0) == 0)
	{
		int palette_index = (offset & 0x1f) | ((offset & 0x200) >> 4);
		int r = ((data << 1) & 0x06) | ((offset >> 8) & 0x01);
		int b = (data >> 2) & 0x07;
		int g = (data >> 5) & 0x07;

		/* set RAM regardless */
		starfire_colorram[offset & ~0x100] = data;
		starfire_colorram[offset |  0x100] = data;

		/* don't modify the palette unless the TRANS bit is set */
		starfire_color = data & 0x1f;
		if (!(starfire_vidctrl1 & 0x40))
			return;

		/* modify the pen */
		r = (r << 5) | (r << 2) | (r >> 1);
		b = (b << 5) | (b << 2) | (b >> 1);
		g = (g << 5) | (g << 2) | (g >> 1);
		palette_change_color(palette_index, r, g, b);
	}

	/* handle writes to the rest of color RAM */
	else
	{
		/* set RAM based on CDRM */
		starfire_colorram[offset] = (starfire_vidctrl1 & 0x80) ? starfire_color : (data & 0x1f);
		scanline_dirty[offset & 0xff] = 1;
		starfire_color = data & 0x1f;
	}
}

READ_HANDLER( starfire_colorram_r )
{
	return starfire_colorram[offset];
}



/*************************************
 *
 *	Video RAM read/writes
 *
 *************************************/

WRITE_HANDLER( starfire_videoram_w )
{
	int sh, lr, dm, ds, mask, d0, dalu;
	int offset1 = offset & 0x1fff;
	int offset2 = (offset + 0x100) & 0x1fff;

	/* PROT */
	if (!(offset & 0xe0) && !(starfire_vidctrl1 & 0x20))
		return;

	/* selector 6A */
	if (offset & 0x2000)
	{
		sh = (starfire_vidctrl >> 1) & 0x07;
		lr = starfire_vidctrl & 0x01;
	}
	else
	{
		sh = (starfire_vidctrl >> 5) & 0x07;
		lr = (starfire_vidctrl >> 4) & 0x01;
	}

	/* mirror bits 5B/5C/5D/5E */
	dm = data;
	if (lr)
		dm = ((dm & 0x01) << 7) | ((dm & 0x02) << 5) | ((dm & 0x04) << 3) | ((dm & 0x08) << 1) |
		     ((dm & 0x10) >> 1) | ((dm & 0x20) >> 3) | ((dm & 0x40) >> 5) | ((dm & 0x80) >> 7);

	/* shifters 6D/6E */
	ds = (dm << 8) >> sh;
	mask = 0xff00 >> sh;

	/* ROLL */
	if ((offset & 0x1f00) == 0x1f00)
	{
		if (starfire_vidctrl1 & 0x10)
			mask &= 0x00ff;
		else
			mask &= 0xff00;
	}

	/* ALU 8B/8D */
	d0 = (starfire_videoram[offset1] << 8) | starfire_videoram[offset2];
	dalu = d0 & ~mask;
	d0 &= mask;
	ds &= mask;
	switch (~starfire_vidctrl1 & 15)
	{
		case 0:		dalu |= ds ^ mask;				break;
		case 1:		dalu |= (ds | d0) ^ mask;		break;
		case 2:		dalu |= (ds ^ mask) & d0;		break;
		case 3:		dalu |= 0;						break;
		case 4:		dalu |= (ds & d0) ^ mask;		break;
		case 5:		dalu |= d0 ^ mask;				break;
		case 6:		dalu |= ds ^ d0;				break;
		case 7:		dalu |= ds & (d0 ^ mask);		break;
		case 8:		dalu |= (ds ^ mask) | d0;		break;
		case 9:		dalu |= (ds ^ d0) ^ mask;		break;
		case 10:	dalu |= d0;						break;
		case 11:	dalu |= ds & d0;				break;
		case 12:	dalu |= mask;					break;
		case 13:	dalu |= ds | (d0 ^ mask);		break;
		case 14:	dalu |= ds | d0;				break;
		case 15:	dalu |= ds;						break;
	}

	/* final output */
	starfire_videoram[offset1] = dalu >> 8;
	starfire_videoram[offset2] = dalu;
	scanline_dirty[offset1 & 0xff] = 1;

	/* color output */
	if (!(offset & 0x2000) && !(starfire_vidctrl1 & 0x80))
	{
		if (mask & 0xff00)
			starfire_colorram[offset1] = starfire_color;
		if (mask & 0x00ff)
			starfire_colorram[offset2] = starfire_color;
	}
}

READ_HANDLER( starfire_videoram_r )
{
	int sh, mask, d0;
	int offset1 = offset & 0x1fff;
	int offset2 = (offset + 0x100) & 0x1fff;

	/* selector 6A */
	if (offset & 0x2000)
		sh = (starfire_vidctrl >> 1) & 0x07;
	else
		sh = (starfire_vidctrl >> 5) & 0x07;

	/* shifters 6D/6E */
	mask = 0xff00 >> sh;

	/* ROLL */
	if ((offset & 0x1f00) == 0x1f00)
	{
		if (starfire_vidctrl1 & 0x10)
			mask &= 0x00ff;
		else
			mask &= 0xff00;
	}

	/* munge the results */
	d0 = (starfire_videoram[offset1] & (mask >> 8)) | (starfire_videoram[offset2] & mask);
	d0 = (d0 << sh) | (d0 >> (8 - sh));
	return d0 & 0xff;
}



/*************************************
 *
 *	Periodic screen refresh callback
 *
 *************************************/

void starfire_video_update(int scanline, int count)
{
	UINT8 *pix = &starfire_videoram[scanline];
	UINT8 *col = &starfire_colorram[scanline];
	int x, y;

	/* update any dirty scanlines in this range */
	for (x = 0; x < 256; x += 8)
	{
		for (y = 0; y < count; y++)
			if (scanline_dirty[scanline + y])
			{
				int data = pix[y];
				int color = col[y];

				plot_pixel(tmpbitmap, x + 0, scanline + y, color | ((data >> 2) & 0x20));
				plot_pixel(tmpbitmap, x + 1, scanline + y, color | ((data >> 1) & 0x20));
				plot_pixel(tmpbitmap, x + 2, scanline + y, color | ((data >> 0) & 0x20));
				plot_pixel(tmpbitmap, x + 3, scanline + y, color | ((data << 1) & 0x20));
				plot_pixel(tmpbitmap, x + 4, scanline + y, color | ((data << 2) & 0x20));
				plot_pixel(tmpbitmap, x + 5, scanline + y, color | ((data << 3) & 0x20));
				plot_pixel(tmpbitmap, x + 6, scanline + y, color | ((data << 4) & 0x20));
				plot_pixel(tmpbitmap, x + 7, scanline + y, color | ((data << 5) & 0x20));
			}

		pix += 256;
		col += 256;
	}

	/* mark them not dirty anymore */
	for (y = 0; y < count; y++)
		scanline_dirty[scanline + y] = 0;
}



/*************************************
 *
 *	Standard screen refresh callback
 *
 *************************************/

void starfire_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	/* update the palette */
	palette_recalc();

	/* copy the bitmap, remapping the colors */
	copybitmap_remap(bitmap, tmpbitmap, 0, 0, 0, 0, &Machine->visible_area, TRANSPARENCY_NONE, 0);
}


