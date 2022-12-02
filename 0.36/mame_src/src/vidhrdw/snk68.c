/***************************************************************************

	SNK 68000 video routines

Notes:
	Search & Rescue uses Y flip on sprites only.
	Street Smart uses X flip on sprites only.

	Seems to be controlled in same byte as flipscreen.

	Emulation by Bryan McPhail, mish@tendril.co.uk

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

static int sprite_flip,flipscreen;
static struct tilemap *fix_tilemap;

/******************************************************************************/

void pow_flipscreen_w(int offset,int data)
{
	flipscreen=data&0x8;
	sprite_flip=data&0x4;
}

void pow_paletteram_w(int offset,int data)
{
	int oldword = READ_WORD (&paletteram[offset]);
	int newword = COMBINE_WORD (oldword, data);
	int r,g,b;

	WRITE_WORD (&paletteram[offset], newword);

	r = ((newword >> 7) & 0x1e) | ((newword >> 14) & 0x01);
	g = ((newword >> 3) & 0x1e) | ((newword >> 13) & 0x01) ;
	b = ((newword << 1) & 0x1e) | ((newword >> 12) & 0x01) ;

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(offset / 2,r,g,b);
}

/******************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap, int j,int pos)
{
	int offs,mx,my,color,tile,fx,fy,i;

	for (offs = pos; offs < pos+0x800; offs += 0x80 )
	{
		mx=(READ_WORD(&spriteram[offs+4+(4*j)])&0xff)<<4;
		my=READ_WORD(&spriteram[offs+6+(4*j)]);
		mx=mx+(my>>12);
		mx=((mx+16)&0x1ff)-16;

		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x100;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;

		for (i=0; i<0x80; i+=4) {
			color=READ_WORD(&spriteram[offs+i+(0x1000*j)+0x1000])&0x7f;

			if (color) {
				tile=READ_WORD(&spriteram[offs+2+i+(0x1000*j)+0x1000]);
				fy=tile&0x8000;
				fx=tile&0x4000;
				tile&=0x3fff;

				drawgfx(bitmap,Machine->gfx[1],
					tile,
					color,
					fx,fy,
					mx,my,
					0,TRANSPARENCY_PEN,0);
			}

			my+=16;
			if (my > 0x100) my-=0x200;
		}
	}
}

/******************************************************************************/

void pow_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,color,i;
	int colmask[0x80],code,pal_base;

	/* Update fix chars */
	tilemap_update(fix_tilemap);

	/* Build the dynamic palette */
	palette_init_used_colors();

	/* Tiles */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 128;color++) colmask[color] = 0;
	for (offs = 0x1000;offs <0x4000;offs += 4 )
	{
		code = READ_WORD(&spriteram[offs+2])&0x3fff;
		color= READ_WORD(&spriteram[offs])&0x7f;
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
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	tilemap_render(ALL_TILEMAPS);

	/* This appears to be correct priority */
	draw_sprites(bitmap,1,0x000);
	draw_sprites(bitmap,1,0x800);
	draw_sprites(bitmap,0,0x000);
	draw_sprites(bitmap,2,0x000);
	draw_sprites(bitmap,2,0x800);
	draw_sprites(bitmap,0,0x800);

	tilemap_draw(bitmap,fix_tilemap,0);
}

/******************************************************************************/

static void get_pow_tile_info( int col, int row )
{
	int offs=(row*4) + (col*128);
	int tile=READ_WORD(&videoram[offs])&0xff;
	int color=READ_WORD(&videoram[offs+2]);

	tile=((color&0xf000)>>4) | tile;
	color&=0xf;

	SET_TILE_INFO(0,tile,color)
}

static void get_sar_tile_info( int col, int row )
{
	int offs=(row*4) + (col*128);
	int tile=READ_WORD(&videoram[offs]);
	int color=tile >> 12;

	tile=tile&0xfff;

	SET_TILE_INFO(0,tile,color)
}

void pow_video_w(int offset,int data)
{
	COMBINE_WORD_MEM(&videoram[offset],data);
	tilemap_mark_tile_dirty(fix_tilemap,offset/128,(offset%128)/4);
}

/******************************************************************************/

static void draw_sprites2(struct osd_bitmap *bitmap, int j, int z, int pos)
{
	int offs,mx,my,color,tile,fx,fy,i;

	for (offs = pos; offs < pos+0x800 ; offs += 0x80 )
	{
		mx=READ_WORD(&spriteram[offs+j]);
		my=READ_WORD(&spriteram[offs+j+2]);

		mx=mx<<4;
		mx=mx|((my>>12)&0xf);
		my=my&0x1ff;

		mx=(mx+0x100)&0x1ff;
		my=(my+0x100)&0x1ff;
		mx-=0x100;
		my-=0x100;
		my=0x200 - my;
		my-=0x200;

		for (i=0; i<0x80; i+=4) {
			color=READ_WORD(&spriteram[offs+i+z])&0x7f;
			if (color) {
				tile=READ_WORD(&spriteram[offs+2+i+z]);
				if (sprite_flip) {
					fx=0;
					fy=tile&0x8000;
				} else {
					fy=0;
					fx=tile&0x8000;
				}

				tile&=0x7fff;
				if (tile>0x5fff) break;

				drawgfx(bitmap,Machine->gfx[1],
					tile,
					color,
					fx,fy,
					mx,my,
					0,TRANSPARENCY_PEN,0);
			}
			my+=16;
			if (my > 0x100) my-=0x200;
		}
	}
}

/******************************************************************************/

void searchar_vh_screenrefresh(struct osd_bitmap *bitmap, int full_refresh)
{
	int offs,color,i;
	int colmask[0x80],code,pal_base;

	/* Update fix chars */
	tilemap_update(fix_tilemap);

	/* Build the dynamic palette */
	palette_init_used_colors();

	/* Tiles */
	pal_base = Machine->drv->gfxdecodeinfo[1].color_codes_start;
	for (color = 0;color < 128;color++) colmask[color] = 0;
	for (offs = 0x1000;offs <0x4000;offs += 4 )
	{
		code = READ_WORD(&spriteram[offs+2])&0x7fff;
		color= READ_WORD(&spriteram[offs])&0x7f;
		if (code>0x5fff) code=0;
		if (color)
			colmask[color] |= Machine->gfx[1]->pen_usage[code];
	}
	/* Palette 0 is "don't display" */
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
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);

	tilemap_render(ALL_TILEMAPS);

	/* This appears to be correct priority */
	draw_sprites2(bitmap,8,0x2000,0x000);
	draw_sprites2(bitmap,8,0x2000,0x800);
	draw_sprites2(bitmap,12,0x3000,0x000);
	draw_sprites2(bitmap,12,0x3000,0x800);
	draw_sprites2(bitmap,4,0x1000,0x000);
	draw_sprites2(bitmap,4,0x1000,0x800);

	tilemap_draw(bitmap,fix_tilemap,0);
}

/******************************************************************************/

int pow_vh_start(void)
{
	/* Tilemaps are different between games */
	if (!strcmp(Machine->gamedrv->name,"pow")
		 || !strcmp(Machine->gamedrv->name,"powj")
		 || !strcmp(Machine->gamedrv->name,"streetsm"))
		fix_tilemap=tilemap_create(
			get_pow_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			32,32
		);
	else
		fix_tilemap=tilemap_create(
			get_sar_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			32,32
		);

	fix_tilemap->transparent_pen = 0;
	tilemap_set_scroll_rows(fix_tilemap,0);
	tilemap_set_scroll_cols(fix_tilemap,0);

	return 0;
}
