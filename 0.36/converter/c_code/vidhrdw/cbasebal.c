#include "driver.h"
#include "vidhrdw/generic.h"


static unsigned char *cbasebal_textram,*cbasebal_scrollram;
static struct tilemap *fg_tilemap,*bg_tilemap;
static int tilebank,spritebank;
static int text_on,bg_on,obj_on;
static int flipscreen;



/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void get_bg_tile_info(int col,int row)
{
	int tile_index = 2*(64*row+col);
	unsigned char attr = cbasebal_scrollram[tile_index+1];
	SET_TILE_INFO(1,cbasebal_scrollram[tile_index] + ((attr & 0x07) << 8) + 0x800 * tilebank,
			(attr & 0xf0) >> 4)
	tile_info.flags = (attr & 0x08) ? TILE_FLIPX : 0;
}

static void get_fg_tile_info(int col,int row)
{
	int tile_index = 64*row+col;
	unsigned char attr = cbasebal_textram[tile_index+0x800];
	SET_TILE_INFO(0,cbasebal_textram[tile_index] + ((attr & 0xf0) << 4),attr & 0x07)
	tile_info.flags = (attr & 0x08) ? TILE_FLIPX : 0;
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

void cbasebal_vh_stop(void)
{
	free(cbasebal_textram);
	cbasebal_textram = 0;
	free(cbasebal_scrollram);
	cbasebal_scrollram = 0;
}

int cbasebal_vh_start(void)
{
	int i;


	cbasebal_textram = malloc(0x1000);
	cbasebal_scrollram = malloc(0x1000);

	bg_tilemap = tilemap_create(
		get_bg_tile_info,
		TILEMAP_OPAQUE,
		16,16,
		64,32
	);

	fg_tilemap = tilemap_create(
		get_fg_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		64,32
	);

	if (!cbasebal_textram || !cbasebal_scrollram || !bg_tilemap || !fg_tilemap)
	{
		cbasebal_vh_stop();
		return 1;
	}

	fg_tilemap->transparent_pen = 3;

#define COLORTABLE_START(gfxn,color_code) Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + \
				color_code * Machine->gfx[gfxn]->color_granularity
#define GFX_COLOR_CODES(gfxn) Machine->gfx[gfxn]->total_colors
#define GFX_ELEM_COLORS(gfxn) Machine->gfx[gfxn]->color_granularity

	palette_init_used_colors();
	/* chars */
	for (i = 0;i < GFX_COLOR_CODES(0);i++)
	{
		memset(&palette_used_colors[COLORTABLE_START(0,i)],
				PALETTE_COLOR_USED,
				GFX_ELEM_COLORS(0)-1);
	}
	/* bg tiles */
	for (i = 0;i < GFX_COLOR_CODES(1);i++)
	{
		memset(&palette_used_colors[COLORTABLE_START(1,i)],
				PALETTE_COLOR_USED,
				GFX_ELEM_COLORS(1));
	}
	/* sprites */
	for (i = 0;i < GFX_COLOR_CODES(2);i++)
	{
		memset(&palette_used_colors[COLORTABLE_START(2,i)],
				PALETTE_COLOR_VISIBLE,
				GFX_ELEM_COLORS(2)-1);
	}

	return 0;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

void cbasebal_textram_w(int offset,int data)
{
	if (cbasebal_textram[offset] != data)
	{
		cbasebal_textram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,(offset&0x7ff)%64,(offset&0x7ff)/64);
	}
}

int cbasebal_textram_r(int offset)
{
	return cbasebal_textram[offset];
}

void cbasebal_scrollram_w(int offset,int data)
{
	if (cbasebal_scrollram[offset] != data)
	{
		cbasebal_scrollram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,(offset/2)%64,(offset/2)/64);
	}
}

int cbasebal_scrollram_r(int offset)
{
	return cbasebal_scrollram[offset];
}

void cbasebal_gfxctrl_w(int offset,int data)
{
	/* bit 0 is unknown - toggles continuously */

	/* bit 1 is flip screen */
	flipscreen = data & 0x02;
	tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	/* bit 2 is unknown - unused? */

	/* bit 3 is tile bank */
	if (tilebank != ((data & 0x08) >> 3))
	{
		tilebank = (data & 0x08) >> 3;
		tilemap_mark_all_tiles_dirty(bg_tilemap);
	}

	/* bit 4 is sprite bank */
	spritebank = (data & 0x10) >> 4;

	/* bits 5 is text enable */
	text_on = ~data & 0x20;

	/* bits 6-7 are bg/sprite enable (don't know which is which) */
	bg_on = ~data & 0x40;
	obj_on = ~data & 0x80;

	/* other bits unknown, but used */
}

void cbasebal_scrollx_w(int offset,int data)
{
	static unsigned char scroll[2];

	scroll[offset] = data;
	tilemap_set_scrollx(bg_tilemap,0,scroll[0] + 256 * scroll[1]);
}

void cbasebal_scrolly_w(int offset,int data)
{
	static unsigned char scroll[2];

	scroll[offset] = data;
	tilemap_set_scrolly(bg_tilemap,0,scroll[0] + 256 * scroll[1]);
}



/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap)
{
	int offs,sx,sy;

	/* the last entry is not a sprite, we skip it otherwise spang shows a bubble */
	/* moving diagonally across the screen */
	for (offs = spriteram_size-8;offs >= 0;offs -= 4)
	{
		int code = spriteram[offs];
		int attr = spriteram[offs+1];
		int color = attr & 0x07;
		int flipx = attr & 0x08;
		sx = spriteram[offs+3] + ((attr & 0x10) << 4);
		sy = ((spriteram[offs+2] + 8) & 0xff) - 8;
		code += (attr & 0xe0) << 3;
		code += spritebank * 0x800;

		if (flipscreen)
		{
			sx = 496 - sx;
			sy = 240 - sy;
			flipx = !flipx;
		}

		drawgfx(bitmap,Machine->gfx[2],
				code,
				color,
				flipx,flipscreen,
				sx,sy,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,15);
	}
}

void cbasebal_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	if (bg_on)
		tilemap_draw(bitmap,bg_tilemap,0);
	else
		fillbitmap(bitmap,Machine->pens[768],&Machine->drv->visible_area);

	if (obj_on)
		draw_sprites(bitmap);

	if (text_on)
		tilemap_draw(bitmap,fg_tilemap,0);
}
