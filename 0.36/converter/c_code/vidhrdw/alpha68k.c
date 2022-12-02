/***************************************************************************

   Alpha 68k video emulation - Bryan McPhail, mish@tendril.co.uk

****************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

static int bank_base;

/******************************************************************************/

void alpha68k_paletteram_w(int offset,int data)
{
	int oldword = READ_WORD (&paletteram[offset]);
	int newword = COMBINE_WORD (oldword, data);
	int r,g,b;

	WRITE_WORD (&paletteram[offset], newword);

	r = ((newword >> 7) & 0x1e) | ((newword >> 14) & 0x01);
	g = ((newword >> 3) & 0x1e) | ((newword >> 13) & 0x01);
	b = ((newword << 1) & 0x1e) | ((newword >> 12) & 0x01);

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(offset / 2,r,g,b);
}

/******************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap, int j, int pos)
{
	int offs,mx,my,color,tile,fx,fy,i;

	for (offs = pos; offs < pos+0x800; offs += 0x80 )
	{
		mx=READ_WORD(&spriteram[offs+4+(4*j)])<<1;
		my=READ_WORD(&spriteram[offs+6+(4*j)]);
		if (my&0x8000) mx++;

		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x100;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;

		for (i=0; i<0x80; i+=4) {
			tile=READ_WORD(&spriteram[offs+2+i+(0x1000*j)+0x1000]);
			color=READ_WORD(&spriteram[offs+i+(0x1000*j)+0x1000])&0x7f;

			fy=tile&0x8000;
			fx=tile&0x4000;
			tile&=0x3fff;

			if (color)
				drawgfx(bitmap,Machine->gfx[1],
					tile,
					color,
					fx,fy,
					mx,my,
					0,TRANSPARENCY_PEN,0);

			my+=16;
			if (my > 0x100) my-=0x200;
		}
	}
}

/******************************************************************************/

void alpha68k_II_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,mx,my,color,tile,i;
	int colmask[0x80],code,pal_base;

	/* Build the dynamic palette */
	memset(palette_used_colors,PALETTE_COLOR_UNUSED,2048 * sizeof(unsigned char));

	/* Text layer */
	pal_base = Machine->drv->gfxdecodeinfo[0].color_codes_start;
	for (color = 0;color < 128;color++) colmask[color] = 0;
	for (offs = 0;offs <0x1000;offs += 4)
	{
		color = READ_WORD(&videoram[offs+2]) &0xf;
		tile = READ_WORD(&videoram[offs])&0xff;
		tile = tile | ((bank_base)<<8);
		colmask[color] |= Machine->gfx[0]->pen_usage[tile];
	}
	for (color = 0;color < 16;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	/* Tiles */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 128;color++) colmask[color] = 0;
	for (offs = 0x1000;offs <0x4000;offs += 4 )
	{
		color= READ_WORD(&spriteram[offs])&0x7f;
		if (!color) continue;
		code = READ_WORD(&spriteram[offs+2])&0x3fff;
		colmask[color] |= Machine->gfx[1]->pen_usage[code];
	}

	for (color = 1;color < 128;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	palette_transparent_color=2047;
	palette_used_colors[2047] = PALETTE_COLOR_USED;
	palette_recalc();
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	/* This appears to be correct priority */
	draw_sprites(bitmap,1,0x000);
	draw_sprites(bitmap,1,0x800);
	draw_sprites(bitmap,0,0x000);
	draw_sprites(bitmap,0,0x800);
	draw_sprites(bitmap,2,0x000);
	draw_sprites(bitmap,2,0x800);

	/* Text layer */
	my = -1;
	mx = 0;
	for (offs = 0x000; offs < 0x1000;offs += 4)
	{
		my++;
		if (my == 32)
		{
			my = 0;
			mx++;
		}
		tile  = READ_WORD(&videoram[offs])&0xff;
		color = READ_WORD(&videoram[offs+2]) &0xf;
		tile = tile | (bank_base<<8);
		drawgfx(bitmap,Machine->gfx[0],
				tile,
				color,
				0,0,
				8*mx,8*my,
				0,TRANSPARENCY_PEN,0);

	}
}

/******************************************************************************/

/*
	Video banking:

	Write to these locations in this order for correct bank:

	20 28 30 for Bank 0
	60 28 30 for Bank 1
	20 68 30 etc
	60 68 30
	20 28 70
	60 28 70
	20 68 70
	60 68 70 for Bank 7

	Actual data values written don't matter!

*/

void alpha68k_II_video_bank_w(int offset, int data)
{
	static int buffer_28,buffer_60,buffer_68;

	switch (offset) {
		case 0x20: /* Reset */
			bank_base=buffer_28=buffer_60=buffer_68=0;
			return;
		case 0x28:
			buffer_28=1;
			return;
		case 0x30:
			if (buffer_68) {if (buffer_60) bank_base=3; else bank_base=2; }
			if (buffer_28) {if (buffer_60) bank_base=1; else bank_base=0; }
			return;
		case 0x60:
			bank_base=buffer_28=buffer_68=0;
			buffer_60=1;
			return;
		case 0x68:
			buffer_68=1;
			return;
		case 0x70:
			if (buffer_68) {if (buffer_60) bank_base=7; else bank_base=6; }
			if (buffer_28) {if (buffer_60) bank_base=5; else bank_base=4; }
			return;
		case 0x10: /* Graphics flags?  Not related to fix chars anyway */
		case 0x18:
		case 0x50:
		case 0x58:
			return;
	}

	if (errorlog) fprintf(errorlog,"%04x \n",offset);
}

/******************************************************************************/

void alpha68k_V_video_control_w(int offset, int data)
{
	switch (offset) {
		case 0x10: /* Graphics flags?  Not related to fix chars anyway */
		case 0x18:
		case 0x50:
		case 0x58:
			return;
	}
}

static void draw_sprites_V(struct osd_bitmap *bitmap, int j, int s, int e, int fx_mask, int fy_mask, int sprite_mask)
{
	int offs,mx,my,color,tile,fx,fy,i;

	for (offs = s; offs < e; offs += 0x80 )
	{
		mx=READ_WORD(&spriteram[offs+4+(4*j)])<<1;
		my=READ_WORD(&spriteram[offs+6+(4*j)]);
		if (my&0x8000) mx++;

		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x100;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;

		for (i=0; i<0x80; i+=4) {
			tile=READ_WORD(&spriteram[offs+2+i+(0x1000*j)+0x1000]);
			color=READ_WORD(&spriteram[offs+i+(0x1000*j)+0x1000])&0xff;

			fx=tile&fx_mask;
			fy=tile&fy_mask;
			tile=tile&sprite_mask;
			if (tile>0x4fff) continue;

			if (color)
				drawgfx(bitmap,Machine->gfx[1],
					tile,
					color,
					fx,fy,
					mx,my,
					0,TRANSPARENCY_PEN,0);

			my+=16;
			if (my > 0x100) my-=0x200;
		}
	}
}

void alpha68k_V_video_bank_w(int offset, int data)
{
	bank_base=data&0xf;
}

void alpha68k_V_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,mx,my,color,tile,i;
	int colmask[256],code,pal_base;

	/* Build the dynamic palette */
	memset(palette_used_colors,PALETTE_COLOR_UNUSED,4096 * sizeof(unsigned char));

	/* Text layer */
	pal_base = Machine->drv->gfxdecodeinfo[0].color_codes_start;
	for (color = 0;color < 16;color++) colmask[color] = 0;
	for (offs = 0;offs <0x1000;offs += 4)
	{
		color = READ_WORD(&videoram[offs+2])&0xf;
		tile = READ_WORD(&videoram[offs])&0xff;
        tile = tile | ((bank_base)<<8);
		colmask[color] |= Machine->gfx[0]->pen_usage[tile];
	}
	for (color = 0;color < 16;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	/* Tiles */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 256;color++) colmask[color] = 0;
	for (offs = 0x1000;offs <0x4000;offs += 4 )
	{
		color= READ_WORD(&spriteram[offs])&0xff;
		if (!color) continue;
		code = READ_WORD(&spriteram[offs+2])&0x7fff;
		colmask[color] |= Machine->gfx[1]->pen_usage[code];
	}

	for (color = 1;color < 256;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	palette_transparent_color=4095;
	palette_used_colors[4095] = PALETTE_COLOR_USED;
	palette_recalc();
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	/* This appears to be correct priority */
	if (!strcmp(Machine->gamedrv->name,"skyadvnt"))
	{
		draw_sprites_V(bitmap,0,0x0f80,0x1000,0,0x8000,0x7fff);
		draw_sprites_V(bitmap,1,0x0000,0x1000,0,0x8000,0x7fff);
		draw_sprites_V(bitmap,2,0x0000,0x1000,0,0x8000,0x7fff);
		draw_sprites_V(bitmap,0,0x0000,0x0f80,0,0x8000,0x7fff);
	}
	else	/* gangwars */
	{
		draw_sprites_V(bitmap,0,0x0f80,0x1000,0x8000,0,0x7fff);
		draw_sprites_V(bitmap,1,0x0000,0x1000,0x8000,0,0x7fff);
		draw_sprites_V(bitmap,2,0x0000,0x1000,0x8000,0,0x7fff);
		draw_sprites_V(bitmap,0,0x0000,0x0f80,0x8000,0,0x7fff);
	}

	/* Text layer */
	my = -1;
	mx = 0;
	for (offs = 0x000; offs < 0x1000;offs += 4)
	{
		my++;
		if (my == 32)
		{
			my = 0;
			mx++;
		}
		tile  = READ_WORD(&videoram[offs])&0xff;
		color = READ_WORD(&videoram[offs+2]);
		tile = tile | ((bank_base)<<8);
		drawgfx(bitmap,Machine->gfx[0],
				tile,
				color&0xf,
				0,0,
				8*mx,8*my,
				0,TRANSPARENCY_PEN,0);

	}
}

void alpha68k_V_sb_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,mx,my,color,tile,i;
	int colmask[256],code,pal_base;

	/* Build the dynamic palette */
	memset(palette_used_colors,PALETTE_COLOR_UNUSED,4096 * sizeof(unsigned char));

	/* Text layer */
	pal_base = Machine->drv->gfxdecodeinfo[0].color_codes_start;
	for (color = 0;color < 16;color++) colmask[color] = 0;
	for (offs = 0;offs <0x1000;offs += 4)
	{
		color = READ_WORD(&videoram[offs+2])&0xf;
		tile = READ_WORD(&videoram[offs])&0xff;
        tile = tile | ((bank_base)<<8);
		colmask[color] |= Machine->gfx[0]->pen_usage[tile];
	}
	for (color = 0;color < 16;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	/* Tiles */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 256;color++) colmask[color] = 0;
	for (offs = 0x1000;offs <0x4000;offs += 4 )
	{
		color= READ_WORD(&spriteram[offs])&0xff;
		if (!color) continue;
		code = READ_WORD(&spriteram[offs+2])&0x7fff;
		colmask[color] |= Machine->gfx[1]->pen_usage[code];
	}

	for (color = 1;color < 256;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	palette_transparent_color=4095;
	palette_used_colors[4095] = PALETTE_COLOR_USED;
	palette_recalc();
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	/* This appears to be correct priority */
	draw_sprites_V(bitmap,0,0x0f80,0x1000,0x4000,0x8000,0x3fff);
	draw_sprites_V(bitmap,1,0x0000,0x1000,0x4000,0x8000,0x3fff);
	draw_sprites_V(bitmap,2,0x0000,0x1000,0x4000,0x8000,0x3fff);
	draw_sprites_V(bitmap,0,0x0000,0x0f80,0x4000,0x8000,0x3fff);

	/* Text layer */
	my = -1;
	mx = 0;
	for (offs = 0x000; offs < 0x1000;offs += 4)
	{
		my++;
		if (my == 32)
		{
			my = 0;
			mx++;
		}
		tile  = READ_WORD(&videoram[offs]);
		color = READ_WORD(&videoram[offs+2]);
		tile = tile | ((bank_base)<<8);
		drawgfx(bitmap,Machine->gfx[0],
				tile,
				color&0xf,
				0,0,
				8*mx,8*my,
				0,TRANSPARENCY_PEN,0);
	}
}

void alpha68k_V_16bit_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,mx,my,color,tile;

	palette_transparent_color=4095;
	palette_recalc();
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	/* This appears to be correct priority */
	draw_sprites_V(bitmap,0,0x0f80,0x1000,0x8000,0,0x7fff);
	draw_sprites_V(bitmap,1,0x0000,0x1000,0x8000,0,0x7fff);
	draw_sprites_V(bitmap,2,0x0000,0x1000,0x8000,0,0x7fff);
	draw_sprites_V(bitmap,0,0x0000,0x0f80,0x8000,0,0x7fff);

	/* Text layer */
	my = -1;
	mx = 0;
	for (offs = 0x000; offs < 0x1000;offs += 4)
	{
		my++;
		if (my == 32)
		{
			my = 0;
			mx++;
		}
		tile  = READ_WORD(&videoram[offs]);
		color = READ_WORD(&videoram[offs+2]);
		tile = tile | ((bank_base)<<8);
		drawgfx(bitmap,Machine->gfx[0],
				tile,
				color&0xf,
				0,0,
				8*mx,8*my,
				0,TRANSPARENCY_PEN,0);
	}
}

/******************************************************************************/

void alpha68k_I_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i,bit0,bit1,bit2,bit3;

	for( i=0; i<256; i++ ){

		colortable[i] = i;
		bit0 = (color_prom[0] >> 0) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		bit0 = (color_prom[0x100] >> 0) & 0x01;
		bit1 = (color_prom[0x100] >> 1) & 0x01;
		bit2 = (color_prom[0x100] >> 2) & 0x01;
		bit3 = (color_prom[0x100] >> 3) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		bit0 = (color_prom[0x200] >> 0) & 0x01;
		bit1 = (color_prom[0x200] >> 1) & 0x01;
		bit2 = (color_prom[0x200] >> 2) & 0x01;
		bit3 = (color_prom[0x200] >> 3) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

/*
		bit0 = (color_prom[0x200] >> 2) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		bit0 = (color_prom[0x200] >> 1) & 0x01;
		bit1 = (color_prom[0x100] >> 2) & 0x01;
		bit2 = (color_prom[0x100] >> 3) & 0x01;
		bit3 = (color_prom[0] >> 0) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		bit0 = (color_prom[0x200] >> 0) & 0x01;
		bit1 = (color_prom[0x200] >> 3) & 0x01;
		bit2 = (color_prom[0x100] >> 0) & 0x01;
		bit3 = (color_prom[0x100] >> 1) & 0x01;
		*palette++ = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
*/


		color_prom++;
	}
}

static void draw_sprites2(struct osd_bitmap *bitmap, int c,int d)
{
	int offs,mx,my,color,tile,i;

	for (offs = 0x0000; offs < 0x800; offs += 0x40 )
	{
		mx=READ_WORD(&spriteram[offs+c]);

		my=mx>>8;
		mx=mx&0xff;

		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x110;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;

		for (i=0; i<0x40; i+=2) {
			tile=READ_WORD(&spriteram[offs+d+i]);
			color=1;
			tile&=0x3fff;

			if (tile && tile!=0x3000 && tile!=0x26)
				drawgfx(bitmap,Machine->gfx[0],
					tile,
					color,
					0,0,
					mx,my,
					0,TRANSPARENCY_PEN,0);

			my+=8;
			if (my > 0x100) my-=0x200;
		}
	}
}

void alpha68k_I_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
//	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	/* This appears to be correct priority */
draw_sprites2(bitmap,6,0x1800);
draw_sprites2(bitmap,4,0x1000);
draw_sprites2(bitmap,2,0x800);
//
}

/******************************************************************************/

static void draw_sprites3(struct osd_bitmap *bitmap, int c,int d)
{
	int offs,mx,my,color,tile,i,bank,fx,fy;

	for (offs = 0x0000; offs < 0x800; offs += 0x40 )
	{
		mx=READ_WORD(&spriteram[offs+c]);

		my=mx>>8;
		mx=mx&0xff;

/*		if (my&0x80)


		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x100;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;
*/
		for (i=0; i<0x40; i+=2) {
			tile=READ_WORD(&spriteram[offs+d+i]);
			color=0;
			fy=tile&0x1000;
			fx=0;
			tile&=0xfff;

			if (tile<0x400) bank=0;
			else if (tile<0x800) bank=1;
			else if (tile<0xc00) bank=2;
			else bank=3;

//check the exclusions
			if (tile && tile!=0x3000 && tile!=0x26)
				drawgfx(bitmap,Machine->gfx[bank],
					tile&0x3ff,
					color,
					fx,fy,
					mx,my,
					0,TRANSPARENCY_PEN,0);

			my+=8;
			if (my > 0x100) my-=0x200;
		}
	}
}

void kyros_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	fillbitmap(bitmap,1,&Machine->drv->visible_area);

	/* This appears to be correct priority */

draw_sprites3(bitmap,4,0x1000);
draw_sprites3(bitmap,6,0x1800);
draw_sprites3(bitmap,2,0x800);
}
