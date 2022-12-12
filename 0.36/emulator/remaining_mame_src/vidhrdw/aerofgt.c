#include "driver.h"
#include "vidhrdw/generic.h"


unsigned char *aerofgt_rasterram;
unsigned char *aerofgt_bg1videoram,*aerofgt_bg2videoram;
int aerofgt_bg1videoram_size,aerofgt_bg2videoram_size;

static unsigned char gfxbank[8];
static unsigned char bg1scrolly[2],bg2scrollx[2],bg2scrolly[2];

static struct osd_bitmap *tmpbitmap2;
static unsigned char *dirtybuffer2;

static int charpalettebank,spritepalettebank;
static int bg2_chardisplacement;



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/
static int common_vh_start(int width,int height)
{
	if ((dirtybuffer = malloc(aerofgt_bg1videoram_size / 2)) == 0)
	{
		return 1;
	}
	memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);

	if ((tmpbitmap = osd_new_bitmap(width,height,Machine->scrbitmap->depth)) == 0)
	{
		free(dirtybuffer);
		return 1;
	}

	if ((dirtybuffer2 = malloc(aerofgt_bg2videoram_size / 2)) == 0)
	{
		free(dirtybuffer);
		osd_free_bitmap(tmpbitmap);
		return 1;
	}
	memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);

	if ((tmpbitmap2 = osd_new_bitmap(width,height,Machine->scrbitmap->depth)) == 0)
	{
		free(dirtybuffer);
		osd_free_bitmap(tmpbitmap);
		free(dirtybuffer2);
		return 1;
	}

	charpalettebank = 0;
	spritepalettebank = 0;

	return 0;
}

int pspikes_vh_start(void)
{
	bg2_chardisplacement = 0;
	aerofgt_bg2videoram_size = 0;	/* no bg2 in this game */
	return common_vh_start(512,256);
}

int turbofrc_vh_start(void)
{
	bg2_chardisplacement = 0x9c;
	return common_vh_start(512,512);
}

int aerofgt_vh_start(void)
{
	bg2_chardisplacement = 0;
	return common_vh_start(512,512);
}

int aerofgtb_vh_start(void)
{
	bg2_chardisplacement = 0x4000;
	return common_vh_start(512,512);
}



/***************************************************************************

  Stop the video hardware emulation.

***************************************************************************/
void aerofgt_vh_stop(void)
{
	free(dirtybuffer);
	osd_free_bitmap(tmpbitmap);
	free(dirtybuffer2);
	osd_free_bitmap (tmpbitmap2);
}




int aerofgt_rasterram_r(int offset)
{
	return READ_WORD(&aerofgt_rasterram[offset]);
}

void aerofgt_rasterram_w(int offset,int data)
{
	COMBINE_WORD_MEM(&aerofgt_rasterram[offset],data);
}

int aerofgt_spriteram_2_r(int offset)
{
	return READ_WORD(&spriteram_2[offset]);
}

void aerofgt_spriteram_2_w(int offset,int data)
{
	COMBINE_WORD_MEM(&spriteram_2[offset],data);
}

int aerofgt_bg1videoram_r(int offset)
{
	return READ_WORD(&aerofgt_bg1videoram[offset]);
}

int aerofgt_bg2videoram_r(int offset)
{
	return READ_WORD(&aerofgt_bg2videoram[offset]);
}

void aerofgt_bg1videoram_w(int offset,int data)
{
	int oldword = READ_WORD(&aerofgt_bg1videoram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	if (oldword != newword)
	{
		WRITE_WORD(&aerofgt_bg1videoram[offset],newword);
		dirtybuffer[offset / 2] = 1;
	}
}

void aerofgt_bg2videoram_w(int offset,int data)
{
	int oldword = READ_WORD(&aerofgt_bg2videoram[offset]);
	int newword = COMBINE_WORD(oldword,data);


	if (oldword != newword)
	{
		WRITE_WORD(&aerofgt_bg2videoram[offset],newword);
		dirtybuffer2[offset / 2] = 1;
	}
}


void pspikes_gfxbank_w(int offset,int data)
{
	/* there are actually two banks in pspikes, instead of four like */
	/* in the other games. The character code is cccBnnnnnnnnnnnn instead */
	/* of cccBBnnnnnnnnnnn. Here I convert the data to four banks so I */
	/* can use the same common routines as the other games. */
	gfxbank[0] = 2*((data & 0xf0) >> 4);	/* guess */
	gfxbank[1] = 2*((data & 0xf0) >> 4) + 1;	/* guess */
	gfxbank[2] = 2*(data & 0x0f);
	gfxbank[3] = 2*(data & 0x0f) + 1;
}

void turbofrc_gfxbank_w(int offset,int data)
{
	static unsigned char old[4];
	int oldword = READ_WORD(&old[offset]);
	int newword;

	newword = COMBINE_WORD(oldword,data);
	if (oldword != newword)
	{
		WRITE_WORD(&old[offset],newword);

		gfxbank[2*offset + 0] = (newword >>  0) & 0x0f;
		gfxbank[2*offset + 1] = (newword >>  4) & 0x0f;
		gfxbank[2*offset + 2] = (newword >>  8) & 0x0f;
		gfxbank[2*offset + 3] = (newword >> 12) & 0x0f;
		if (offset < 2)
			memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);
		else
			memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
	}
}

void aerofgt_gfxbank_w(int offset,int data)
{
	int oldword = READ_WORD(&gfxbank[offset]);
	int newword;


	/* straighten out the 16-bit word into bytes for conveniency */
	#ifdef LSB_FIRST
	data = ((data & 0x00ff00ff) << 8) | ((data & 0xff00ff00) >> 8);
	#endif

	newword = COMBINE_WORD(oldword,data);
	if (oldword != newword)
	{
		WRITE_WORD(&gfxbank[offset],newword);
		if (offset < 4)
			memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);
		else
			memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
	}
}

void aerofgt_bg1scrolly_w(int offset,int data)
{
	COMBINE_WORD_MEM(bg1scrolly,data);
}

void turbofrc_bg2scrollx_w(int offset,int data)
{
	COMBINE_WORD_MEM(bg2scrollx,data);
}

void aerofgt_bg2scrolly_w(int offset,int data)
{
	COMBINE_WORD_MEM(bg2scrolly,data);
}

void pspikes_palette_bank_w(int offset,int data)
{
	spritepalettebank = data & 0x03;
	charpalettebank = (data & 0x1c) >> 2;
}



/***************************************************************************

  Draw the game screen in the given osd_bitmap.
  Do NOT call osd_update_display() from this function, it will be called by
  the main emulation engine.

***************************************************************************/
static void bg_dopalette(void)
{
	int offs;
	int color,code,i;
	int colmask[32];
	int pal_base;


	pal_base = Machine->drv->gfxdecodeinfo[0].color_codes_start;

	for (color = 0;color < 32;color++) colmask[color] = 0;

	for (offs = aerofgt_bg1videoram_size - 2;offs >= 0;offs -= 2)
	{
		code = READ_WORD(&aerofgt_bg1videoram[offs]);
		color = ((code & 0xe000) >> 13) + 8 * charpalettebank;
		code = (code & 0x07ff) + (gfxbank[0 + ((code & 0x1800) >> 11)] << 11);
		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (offs = aerofgt_bg2videoram_size - 2;offs >= 0;offs -= 2)
	{
		code = READ_WORD(&aerofgt_bg2videoram[offs]);
		color = ((code & 0xe000) >> 13) + 16;
		code = bg2_chardisplacement + (code & 0x07ff) + (gfxbank[4 + ((code & 0x1800) >> 11)] << 11),
		colmask[color] |= Machine->gfx[0]->pen_usage[code];
	}

	for (color = 0;color < 32;color++)
	{
		for (i = 0;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
		/* bg1 uses colors 0-7 and is not transparent */
		/* bg2 uses colors 16-23 and is transparent */
		if (color >= 16 && (colmask[color] & (1 << 15)))
			palette_used_colors[pal_base + 16 * color + 15] = PALETTE_COLOR_TRANSPARENT;
	}
}

static void aerofgt_spr_dopalette(void)
{
	int offs;
	int color,i;
	int colmask[32];
	int pal_base;


	for (color = 0;color < 32;color++) colmask[color] = 0;

	offs = 0;
	while (offs < 0x0800 && (READ_WORD(&spriteram_2[offs]) & 0x8000) == 0)
	{
		int attr_start,map_start;

		attr_start = 8 * (READ_WORD(&spriteram_2[offs]) & 0x03ff);

		color = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x0f00) >> 8;
		map_start = 2 * (READ_WORD(&spriteram_2[attr_start + 6]) & 0x3fff);
		if (map_start >= 0x4000) color += 16;

		colmask[color] |= 0xffff;

		offs += 2;
	}

	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 16;color++)
	{
		for (i = 0;i < 15;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}
	pal_base = Machine->drv->gfxdecodeinfo[2].color_codes_start;
	for (color = 0;color < 16;color++)
	{
		for (i = 0;i < 15;i++)
		{
			if (colmask[color+16] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}
}

static void turbofrc_spr_dopalette(void)
{
	int color,i;
	int colmask[16];
	int pal_base;
	int attr_start,base,first;


	for (color = 0;color < 16;color++) colmask[color] = 0;

	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;

	base = 0;
	first = 8*READ_WORD(&spriteram_2[0x3fc + base]);
	for (attr_start = first + base;attr_start < base + 0x0400-8;attr_start += 8)
	{
		color = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x000f) + 16 * spritepalettebank;
		colmask[color] |= 0xffff;
	}

	for (color = 0;color < 16;color++)
	{
		for (i = 0;i < 15;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}


	if (spriteram_2_size > 0x400)	/* turbofrc, not pspikes */
	{
		for (color = 0;color < 16;color++) colmask[color] = 0;

		pal_base = Machine->drv->gfxdecodeinfo[2].color_codes_start;

		base = 0x0400;
		first = 8*READ_WORD(&spriteram_2[0x3fc + base]);
		for (attr_start = first + base;attr_start < base + 0x0400-8;attr_start += 8)
		{
			color = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x000f) + 16 * spritepalettebank;
			colmask[color] |= 0xffff;
		}

		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 15;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	}
}


static void aerofgt_drawsprites(struct osd_bitmap *bitmap,int priority)
{
	int offs;


	priority <<= 12;

	offs = 0;
	while (offs < 0x0800 && (READ_WORD(&spriteram_2[offs]) & 0x8000) == 0)
	{
		int attr_start;

		attr_start = 8 * (READ_WORD(&spriteram_2[offs]) & 0x03ff);

		/* is the way I handle priority correct? Or should I just check bit 13? */
		if ((READ_WORD(&spriteram_2[attr_start + 4]) & 0x3000) == priority)
		{
			int map_start;
			int ox,oy,x,y,xsize,ysize,zoomx,zoomy,flipx,flipy,color;
			/* table hand made by looking at the ship explosion in attract mode */
			/* it's almost a logarithmic scale but not exactly */
			int zoomtable[16] = { 0,7,14,20,25,30,34,38,42,46,49,52,54,57,59,61 };

			ox = READ_WORD(&spriteram_2[attr_start + 2]) & 0x01ff;
			xsize = (READ_WORD(&spriteram_2[attr_start + 2]) & 0x0e00) >> 9;
			zoomx = (READ_WORD(&spriteram_2[attr_start + 2]) & 0xf000) >> 12;
			oy = READ_WORD(&spriteram_2[attr_start + 0]) & 0x01ff;
			ysize = (READ_WORD(&spriteram_2[attr_start + 0]) & 0x0e00) >> 9;
			zoomy = (READ_WORD(&spriteram_2[attr_start + 0]) & 0xf000) >> 12;
			flipx = READ_WORD(&spriteram_2[attr_start + 4]) & 0x4000;
			flipy = READ_WORD(&spriteram_2[attr_start + 4]) & 0x8000;
			color = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x0f00) >> 8;
			map_start = 2 * (READ_WORD(&spriteram_2[attr_start + 6]) & 0x3fff);

			zoomx = 16 - zoomtable[zoomx]/8;
			zoomy = 16 - zoomtable[zoomy]/8;

			for (y = 0;y <= ysize;y++)
			{
				int sx,sy;

				if (flipy) sy = ((oy + zoomy * (ysize - y) + 16) & 0x1ff) - 16;
				else sy = ((oy + zoomy * y + 16) & 0x1ff) - 16;

				for (x = 0;x <= xsize;x++)
				{
					int code;

					if (flipx) sx = ((ox + zoomx * (xsize - x) + 16) & 0x1ff) - 16;
					else sx = ((ox + zoomx * x + 16) & 0x1ff) - 16;

					code = READ_WORD(&spriteram[map_start]) & 0x1fff;
					if (zoomx == 16 && zoomy == 16)
						drawgfx(bitmap,Machine->gfx[map_start >= 0x4000 ? 2 : 1],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine->drv->visible_area,TRANSPARENCY_PEN,15);
					else
						drawgfxzoom(bitmap,Machine->gfx[map_start >= 0x4000 ? 2 : 1],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine->drv->visible_area,TRANSPARENCY_PEN,15,
								0x1000 * zoomx,0x1000 * zoomy);
					map_start += 2;
				}
			}
		}

		offs += 2;
	}
}

static void turbofrc_drawsprites(struct osd_bitmap *bitmap,int priority)
{
	int attr_start,base,first;


	base = (priority & 1) * 0x0400;
	first = 8*READ_WORD(&spriteram_2[0x3fc + base]);
	priority = (priority & 2) << 3;

	for (attr_start = first + base;attr_start < base + 0x0400-8;attr_start += 8)
	{
		if ((READ_WORD(&spriteram_2[attr_start + 4]) & 0x0010) == priority)
		{
			int map_start;
			int ox,oy,x,y,xsize,ysize,zoomx,zoomy,flipx,flipy,color;
			/* table hand made by looking at the ship explosion in attract mode */
			/* it's almost a logarithmic scale but not exactly */
			int zoomtable[16] = { 0,7,14,20,25,30,34,38,42,46,49,52,54,57,59,61 };

			ox = READ_WORD(&spriteram_2[attr_start + 2]) & 0x01ff;
			xsize = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x0700) >> 8;
			zoomx = (READ_WORD(&spriteram_2[attr_start + 2]) & 0xf000) >> 12;
			oy = READ_WORD(&spriteram_2[attr_start + 0]) & 0x01ff;
			ysize = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x7000) >> 12;
			zoomy = (READ_WORD(&spriteram_2[attr_start + 0]) & 0xf000) >> 12;
			flipx = READ_WORD(&spriteram_2[attr_start + 4]) & 0x0800;
			flipy = READ_WORD(&spriteram_2[attr_start + 4]) & 0x8000;
			color = (READ_WORD(&spriteram_2[attr_start + 4]) & 0x000f) + 16 * spritepalettebank;
			map_start = 2 * (READ_WORD(&spriteram_2[attr_start + 6]) & 0x1fff);
			if (attr_start >= 0x0400) map_start |= 0x4000;

			zoomx = 16 - zoomtable[zoomx]/8;
			zoomy = 16 - zoomtable[zoomy]/8;

			for (y = 0;y <= ysize;y++)
			{
				int sx,sy;

				if (flipy) sy = ((oy + zoomy * (ysize - y) + 16) & 0x1ff) - 16;
				else sy = ((oy + zoomy * y + 16) & 0x1ff) - 16;

				for (x = 0;x <= xsize;x++)
				{
					int code;

					if (flipx) sx = ((ox + zoomx * (xsize - x) + 16) & 0x1ff) - 16;
					else sx = ((ox + zoomx * x + 16) & 0x1ff) - 16;

					code = READ_WORD(&spriteram[map_start]) & 0x3fff;
					if (zoomx == 16 && zoomy == 16)
						drawgfx(bitmap,Machine->gfx[map_start >= 0x4000 ? 2 : 1],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine->drv->visible_area,TRANSPARENCY_PEN,15);
					else
						drawgfxzoom(bitmap,Machine->gfx[map_start >= 0x4000 ? 2 : 1],
								code,
								color,
								flipx,flipy,
								sx,sy,
								&Machine->drv->visible_area,TRANSPARENCY_PEN,15,
								0x1000 * zoomx,0x1000 * zoomy);
					map_start += 2;
				}

				if (xsize == 2) map_start += 2;
				if (xsize == 4) map_start += 6;
				if (xsize == 5) map_start += 4;
				if (xsize == 6) map_start += 2;
			}
		}
	}
}



void pspikes_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;


	palette_init_used_colors();
	bg_dopalette();
	turbofrc_spr_dopalette();
	if (palette_recalc())
	{
		memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);
//		memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
	}

	for (offs = aerofgt_bg1videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer[offs/2])
		{
			int sx,sy,code;


			dirtybuffer[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg1videoram[offs]);
			drawgfx(tmpbitmap,Machine->gfx[0],
					(code & 0x07ff) + (gfxbank[0 + ((code & 0x1800) >> 11)] << 11),
					((code & 0xe000) >> 13) + 8 * charpalettebank,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

#if 0
	for (offs = aerofgt_bg2videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer2[offs/2])
		{
			int sx,sy,code;


			dirtybuffer2[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg2videoram[offs]);

			drawgfx(tmpbitmap2,Machine->gfx[0],
					bg2_chardisplacement + (code & 0x07ff) + (gfxbank[4 + ((code & 0x1800) >> 11)] << 11),
					((code & 0xe000) >> 13) + 16,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}
#endif

	/* copy the temporary bitmap to the screen */
	{
		int scrollx[256],scrolly;

		scrolly = -READ_WORD(bg1scrolly);
		for (offs = 0;offs < 256;offs++)
			scrollx[(offs - scrolly) & 0x0ff] = -READ_WORD(&aerofgt_rasterram[2*offs]);
		copyscrollbitmap(bitmap,tmpbitmap,256,scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
	}

	turbofrc_drawsprites(bitmap,0);
//	turbofrc_drawsprites(bitmap,1);

#if 0
	{
		int scrollx,scrolly;

		scrollx = -READ_WORD(bg2scrollx)+7;
		scrolly = -READ_WORD(bg2scrolly)-2;
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	}
#endif

	turbofrc_drawsprites(bitmap,2);
//	turbofrc_drawsprites(bitmap,3);
}

void turbofrc_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;
static int drawbg2 = 1;

if (keyboard_pressed_memory(KEYCODE_SPACE))
	drawbg2 = !drawbg2;

	palette_init_used_colors();
	bg_dopalette();
	turbofrc_spr_dopalette();
	if (palette_recalc())
	{
		memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);
		memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
	}

	for (offs = aerofgt_bg1videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer[offs/2])
		{
			int sx,sy,code;


			dirtybuffer[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg1videoram[offs]);
			drawgfx(tmpbitmap,Machine->gfx[0],
					(code & 0x07ff) + (gfxbank[0 + ((code & 0x1800) >> 11)] << 11),
					(code & 0xe000) >> 13,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	for (offs = aerofgt_bg2videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer2[offs/2])
		{
			int sx,sy,code;


			dirtybuffer2[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg2videoram[offs]);

			drawgfx(tmpbitmap2,Machine->gfx[0],
					bg2_chardisplacement + (code & 0x07ff) + (gfxbank[4 + ((code & 0x1800) >> 11)] << 11),
					((code & 0xe000) >> 13) + 16,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}


	/* copy the temporary bitmap to the screen */
	{
		int scrollx[512],scrolly;

		scrolly = -READ_WORD(bg1scrolly)-2;
		for (offs = 0;offs < 256;offs++)
//			scrollx[(offs - scrolly) & 0x1ff] = -READ_WORD(&aerofgt_rasterram[2*offs])+11;
scrollx[(offs - scrolly) & 0x1ff] = -READ_WORD(&aerofgt_rasterram[0xe])+11;
		copyscrollbitmap(bitmap,tmpbitmap,512,scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
	}

	turbofrc_drawsprites(bitmap,0);
	turbofrc_drawsprites(bitmap,1);

	{
		int scrollx,scrolly;

		scrollx = -READ_WORD(bg2scrollx)+7;
		scrolly = -READ_WORD(bg2scrolly)-2;
if (drawbg2)
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	}

	turbofrc_drawsprites(bitmap,2);
	turbofrc_drawsprites(bitmap,3);
}

void aerofgt_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	palette_init_used_colors();
	bg_dopalette();
	aerofgt_spr_dopalette();
	if (palette_recalc())
	{
		memset(dirtybuffer,1,aerofgt_bg1videoram_size / 2);
		memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
	}

	for (offs = aerofgt_bg1videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer[offs/2])
		{
			int sx,sy,code;


			dirtybuffer[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg1videoram[offs]);
			drawgfx(tmpbitmap,Machine->gfx[0],
					(code & 0x07ff) + (gfxbank[0 + ((code & 0x1800) >> 11)] << 11),
					(code & 0xe000) >> 13,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}

	for (offs = aerofgt_bg2videoram_size - 2;offs >= 0;offs -= 2)
	{
		if (dirtybuffer2[offs/2])
		{
			int sx,sy,code;


			dirtybuffer2[offs/2] = 0;

			sx = (offs/2) % 64;
			sy = (offs/2) / 64;

			code = READ_WORD(&aerofgt_bg2videoram[offs]);
			drawgfx(tmpbitmap2,Machine->gfx[0],
					bg2_chardisplacement + (code & 0x07ff) + (gfxbank[4 + ((code & 0x1800) >> 11)] << 11),
					((code & 0xe000) >> 13) + 16,
					0,0,
					8*sx,8*sy,
					0,TRANSPARENCY_NONE,0);
		}
	}


	/* copy the temporary bitmap to the screen */
	{
		int scrollx,scrolly;

		scrollx = -READ_WORD(&aerofgt_rasterram[0x0000])+18;
		scrolly = -READ_WORD(bg1scrolly);
		copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_NONE,0);
	}

	aerofgt_drawsprites(bitmap,0);
	aerofgt_drawsprites(bitmap,1);

	{
		int scrollx,scrolly;

		scrollx = -READ_WORD(&aerofgt_rasterram[0x0400])+20;
		scrolly = -READ_WORD(bg2scrolly);
		copyscrollbitmap(bitmap,tmpbitmap2,1,&scrollx,1,&scrolly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
	}

	aerofgt_drawsprites(bitmap,2);
	aerofgt_drawsprites(bitmap,3);
}
