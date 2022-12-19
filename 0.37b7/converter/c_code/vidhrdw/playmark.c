#include "driver.h"
#include "vidhrdw/generic.h"



unsigned char *bigtwin_bgvideoram;
size_t bigtwin_bgvideoram_size;
unsigned char *wbeachvl_videoram1,*wbeachvl_videoram2,*wbeachvl_videoram3;
static struct osd_bitmap *bgbitmap;
static int bgscrollx,bgscrolly;
static struct tilemap *tx_tilemap,*fg_tilemap,*bg_tilemap;



/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void bigtwin_get_tx_tile_info(int tile_index)
{
	UINT16 code = READ_WORD(&wbeachvl_videoram1[4*tile_index]);
	UINT16 color = READ_WORD(&wbeachvl_videoram1[4*tile_index+2]);
	SET_TILE_INFO(2,code,color)
}

static void bigtwin_get_fg_tile_info(int tile_index)
{
	UINT16 code = READ_WORD(&wbeachvl_videoram2[4*tile_index]);
	UINT16 color = READ_WORD(&wbeachvl_videoram2[4*tile_index+2]);
	SET_TILE_INFO(1,code,color)
}


static void wbeachvl_get_tx_tile_info(int tile_index)
{
	UINT16 code = READ_WORD(&wbeachvl_videoram1[4*tile_index]);
	UINT16 color = READ_WORD(&wbeachvl_videoram1[4*tile_index+2]);
	SET_TILE_INFO(2,code,color / 4)
}

static void wbeachvl_get_fg_tile_info(int tile_index)
{
	UINT16 code = READ_WORD(&wbeachvl_videoram2[4*tile_index]);
	UINT16 color = READ_WORD(&wbeachvl_videoram2[4*tile_index+2]);
	SET_TILE_INFO(1,code & 0x7fff,color / 4 + 8)
	tile_info.flags = (code & 0x8000) ? TILE_FLIPX : 0;
}

static void wbeachvl_get_bg_tile_info(int tile_index)
{
	UINT16 code = READ_WORD(&wbeachvl_videoram3[4*tile_index]);
	UINT16 color = READ_WORD(&wbeachvl_videoram3[4*tile_index+2]);
	SET_TILE_INFO(1,code & 0x7fff,color / 4)
	tile_info.flags = (code & 0x8000) ? TILE_FLIPX : 0;
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

void bigtwin_vh_stop(void)
{
	bitmap_free(bgbitmap);
	bgbitmap = 0;
}

int bigtwin_vh_start(void)
{
	bgbitmap = bitmap_alloc(512,512);

	tx_tilemap = tilemap_create(bigtwin_get_tx_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,64,32);
	fg_tilemap = tilemap_create(bigtwin_get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,16);

	if (!tx_tilemap || !fg_tilemap || !bgbitmap)
	{
		bigtwin_vh_stop();
		return 1;
	}

	tx_tilemap->transparent_pen = 0;
	fg_tilemap->transparent_pen = 0;

	return 0;
}


int wbeachvl_vh_start(void)
{
	tx_tilemap = tilemap_create(wbeachvl_get_tx_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,64,32);
	fg_tilemap = tilemap_create(wbeachvl_get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,64,32);
	bg_tilemap = tilemap_create(wbeachvl_get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,     16,16,64,32);

	if (!tx_tilemap || !fg_tilemap || !bg_tilemap)
		return 1;

	tx_tilemap->transparent_pen = 0;
	fg_tilemap->transparent_pen = 0;

	return 0;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

READ_HANDLER( wbeachvl_txvideoram_r )
{
	return READ_WORD(&wbeachvl_videoram1[offset]);
}

WRITE_HANDLER( wbeachvl_txvideoram_w )
{
	int oldword = READ_WORD(&wbeachvl_videoram1[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		WRITE_WORD(&wbeachvl_videoram1[offset],newword);
		tilemap_mark_tile_dirty(tx_tilemap,offset / 4);
	}
}

READ_HANDLER( wbeachvl_fgvideoram_r )
{
	return READ_WORD(&wbeachvl_videoram2[offset]);
}

WRITE_HANDLER( wbeachvl_fgvideoram_w )
{
	int oldword = READ_WORD(&wbeachvl_videoram2[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		WRITE_WORD(&wbeachvl_videoram2[offset],newword);
		tilemap_mark_tile_dirty(fg_tilemap,offset / 4);
	}
}

READ_HANDLER( wbeachvl_bgvideoram_r )
{
	return READ_WORD(&wbeachvl_videoram3[offset]);
}

WRITE_HANDLER( wbeachvl_bgvideoram_w )
{
	int oldword = READ_WORD(&wbeachvl_videoram3[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		WRITE_WORD(&wbeachvl_videoram3[offset],newword);
		tilemap_mark_tile_dirty(bg_tilemap,offset / 4);
	}
}


WRITE_HANDLER( bigtwin_paletteram_w )
{
	int r,g,b,val;


	COMBINE_WORD_MEM(&paletteram[offset],data);

	val = READ_WORD(&paletteram[offset]);
	r = (val >> 11) & 0x1e;
	g = (val >>  7) & 0x1e;
	b = (val >>  3) & 0x1e;

	r |= ((val & 0x08) >> 3);
	g |= ((val & 0x04) >> 2);
	b |= ((val & 0x02) >> 1);

	r = (r << 3) | (r >> 2);
	g = (g << 3) | (g >> 2);
	b = (b << 3) | (b >> 2);

	palette_change_color(offset / 2,r,g,b);
}

WRITE_HANDLER( bigtwin_bgvideoram_w )
{
	int sx,sy,color;


	COMBINE_WORD_MEM(&bigtwin_bgvideoram[offset],data);

	sx = (offset/2) % 512;
	sy = (offset/2) / 512;

	color = READ_WORD(&bigtwin_bgvideoram[offset]) & 0xff;

	plot_pixel(bgbitmap,sx,sy,Machine->pens[256 + color]);
}


WRITE_HANDLER( bigtwin_scroll_w )
{
	static UINT8 scroll[12];


	COMBINE_WORD_MEM(&scroll[offset],data);
	data = READ_WORD(&scroll[offset]);

	switch (offset)
	{
		case 0x00: tilemap_set_scrollx(tx_tilemap,0,data+2); break;
		case 0x02: tilemap_set_scrolly(tx_tilemap,0,data);   break;
		case 0x04: bgscrollx = -(data+4);                    break;
		case 0x06: bgscrolly = (-data) & 0x1ff;              break;
		case 0x08: tilemap_set_scrollx(fg_tilemap,0,data+6); break;
		case 0x0a: tilemap_set_scrolly(fg_tilemap,0,data);   break;
	}
}

WRITE_HANDLER( wbeachvl_scroll_w )
{
	static UINT8 scroll[12];


	COMBINE_WORD_MEM(&scroll[offset],data);
	data = READ_WORD(&scroll[offset]);

	switch (offset)
	{
		case 0x00: tilemap_set_scrollx(tx_tilemap,0,data+2); break;
		case 0x02: tilemap_set_scrolly(tx_tilemap,0,data);   break;
		case 0x04: tilemap_set_scrollx(fg_tilemap,0,data+4); break;
		case 0x06: tilemap_set_scrolly(fg_tilemap,0,data);   break;
		case 0x08: tilemap_set_scrollx(bg_tilemap,0,data+6); break;
		case 0x0a: tilemap_set_scrolly(bg_tilemap,0,data);   break;
	}
}



/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap,int codeshift)
{
	int offs;
	int height = Machine->gfx[0]->height;
	int colordiv = Machine->gfx[0]->color_granularity / 16;

	for (offs = 8;offs < spriteram_size;offs += 8)
	{
		int sx,sy,code,color,flipx;

		sy = READ_WORD(&spriteram[offs+6-8]);	/* -8? what the... ??? */
		if (sy == 0x2000) return;	/* end of list marker */

		flipx = sy & 0x4000;
		sx = (READ_WORD(&spriteram[offs+2]) & 0x01ff) - 16-7;
		sy = (256-8-height - sy) & 0xff;
		code = READ_WORD(&spriteram[offs+4]) >> codeshift;
		color = (READ_WORD(&spriteram[offs+2]) & 0xfe00) >> 9;

		drawgfx(bitmap,Machine->gfx[0],
				code,
				color/colordiv,
				flipx,0,
				sx,sy,
				&Machine->visible_area,TRANSPARENCY_PEN,0);
	}
}


void bigtwin_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	palette_used_colors[256] = PALETTE_COLOR_TRANSPARENT;	/* keep the background black */
	if (palette_recalc())
	{
		int offs;

		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		for (offs = 0;offs < bigtwin_bgvideoram_size;offs += 2)
			bigtwin_bgvideoram_w(offs,READ_WORD(&bigtwin_bgvideoram[offs]));
	}

	tilemap_render(ALL_TILEMAPS);

	copyscrollbitmap(bitmap,bgbitmap,1,&bgscrollx,1,&bgscrolly,&Machine->visible_area,TRANSPARENCY_NONE,0);
	tilemap_draw(bitmap,fg_tilemap,0);
	draw_sprites(bitmap,4);
	tilemap_draw(bitmap,tx_tilemap,0);
}

void wbeachvl_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap,bg_tilemap,0);
	tilemap_draw(bitmap,fg_tilemap,0);
	draw_sprites(bitmap,0);
	tilemap_draw(bitmap,tx_tilemap,0);
}
