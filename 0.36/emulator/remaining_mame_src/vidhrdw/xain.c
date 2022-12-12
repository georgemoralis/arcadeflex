/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *xain_charram, *xain_bgram0, *xain_bgram1;

static struct tilemap *char_tilemap, *bgram0_tilemap, *bgram1_tilemap;
static int flipscreen;


/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void get_bgram0_tile_info( int col, int row )
{
	int addr = (col & 0xf)|((col & 0x10)<<4)|((row & 0xf)<<4)|((row & 0x10)<<5);
	int attr = xain_bgram0[addr | 0x400];
	SET_TILE_INFO(2,xain_bgram0[addr] | ((attr & 7) << 8),(attr & 0x70) >> 4);
	tile_info.flags = (attr & 0x80) ? TILE_FLIPX : 0;
}

static void get_bgram1_tile_info( int col, int row )
{
	int addr = (col & 0xf)|((col & 0x10)<<4)|((row & 0xf)<<4)|((row & 0x10)<<5);
	int attr = xain_bgram1[addr | 0x400];
	SET_TILE_INFO(1,xain_bgram1[addr] | ((attr & 7) << 8),(attr & 0x70) >> 4);
	tile_info.flags = (attr & 0x80) ? TILE_FLIPX : 0;
}

static void get_char_tile_info( int col, int row )
{
	int addr = col + row*32;
	int attr = xain_charram[addr | 0x400];
	SET_TILE_INFO(0,xain_charram[addr] | ((attr & 3) << 8),(attr & 0xe0) >> 5);
}


/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

int xain_vh_start(void)
{
	bgram0_tilemap = tilemap_create(
			get_bgram0_tile_info,
			TILEMAP_OPAQUE,
			16,16,
			32,32);
	bgram1_tilemap = tilemap_create(
			get_bgram1_tile_info,
			TILEMAP_TRANSPARENT,
			16,16,
			32,32);
	char_tilemap = tilemap_create(
			get_char_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			32,32);

	if (!bgram0_tilemap || !bgram1_tilemap || !char_tilemap)
		return 1;

	bgram1_tilemap->transparent_pen = 0;
	char_tilemap->transparent_pen = 0;

	return 0;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

void xain_bgram0_w(int offset, int data)
{
	if (xain_bgram0[offset] != data)
	{
		xain_bgram0[offset] = data;
		tilemap_mark_tile_dirty (bgram0_tilemap,
				 ((offset>>4)&0x10)|(offset&0xf),
				 ((offset>>5)&0x10)|((offset>>4)&0xf));
	}
}

void xain_bgram1_w(int offset, int data)
{
	if (xain_bgram1[offset] != data)
	{
		xain_bgram1[offset] = data;
		tilemap_mark_tile_dirty (bgram1_tilemap,
				 ((offset>>4)&0x10)|(offset&0xf),
				 ((offset>>5)&0x10)|((offset>>4)&0xf));
	}
}

void xain_charram_w(int offset, int data)
{
	if (xain_charram[offset] != data)
	{
		xain_charram[offset] = data;
		tilemap_mark_tile_dirty (char_tilemap, offset & 0x1f, (offset & 0x3e0) >> 5);
	}
}

void xain_scrollxP0_w(int offset,int data)
{
	static unsigned char xain_scrollxP0[2];

	xain_scrollxP0[offset] = data;
	tilemap_set_scrollx(bgram0_tilemap, 0, xain_scrollxP0[0]|(xain_scrollxP0[1]<<8));
}

void xain_scrollyP0_w(int offset,int data)
{
	static unsigned char xain_scrollyP0[2];

	xain_scrollyP0[offset] = data;
	tilemap_set_scrolly(bgram0_tilemap, 0, xain_scrollyP0[0]|(xain_scrollyP0[1]<<8));
}

void xain_scrollxP1_w(int offset,int data)
{
	static unsigned char xain_scrollxP1[2];

	xain_scrollxP1[offset] = data;
	tilemap_set_scrollx(bgram1_tilemap, 0, xain_scrollxP1[0]|(xain_scrollxP1[1]<<8));
}

void xain_scrollyP1_w(int offset,int data)
{
	static unsigned char xain_scrollyP1[2];

	xain_scrollyP1[offset] = data;
	tilemap_set_scrolly(bgram1_tilemap, 0, xain_scrollyP1[0]|(xain_scrollyP1[1]<<8));
}


void xain_flipscreen_w(int offset,int data)
{
	flipscreen = data & 1;
	tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
}


/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;

	for (offs = 0; offs < spriteram_size;offs += 4)
	{
		int sx,sy,flipx;
		int attr = spriteram[offs+1];
		int numtile = spriteram[offs+2] | ((attr & 7) << 8);
		int color = (attr & 0x38) >> 3;

		sx = 239 - spriteram[offs+3];
		if (sx <= -7) sx += 256;
		sy = 240 - spriteram[offs];
		if (sy <= -7) sy += 256;
		flipx = attr & 0x40;
		if (flipscreen)
		{
			sx = 239 - sx;
			sy = 240 - sy;
			flipx = !flipx;
		}

		if (attr & 0x80)	/* double height */
		{
			drawgfx(bitmap,Machine->gfx[3],
					numtile,
					color,
					flipx,flipscreen,
					sx-1,flipscreen?sy+16:sy-16,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
			drawgfx(bitmap,Machine->gfx[3],
					numtile+1,
					color,
					flipx,flipscreen,
					sx-1,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
		else
		{
			drawgfx(bitmap,Machine->gfx[3],
					numtile,
					color,
					flipx,flipscreen,
					sx,sy,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
		}
	}
}

void xain_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	palette_init_used_colors();
	memset(palette_used_colors+128,PALETTE_COLOR_USED,128);	/* sprites */
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap,bgram0_tilemap,0);
	tilemap_draw(bitmap,bgram1_tilemap,0);
	draw_sprites(bitmap);
	tilemap_draw(bitmap,char_tilemap,0);
}
