/***************************************************************************

  Bionic Commando Video Hardware

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *bionicc_fgvideoram;
unsigned char *bionicc_bgvideoram;
unsigned char *bionicc_txvideoram;

static struct tilemap *tx_tilemap, *bg_tilemap, *fg_tilemap;
static int flipscreen;


/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static const unsigned short *videoram1,*videoram2;

static void bg_preupdate(void)
{
	videoram1 = (const unsigned short *)bionicc_bgvideoram;
}

static void get_bg_tile_info(int col,int row)
{
	int offset = 2*(row*64+col);
	int attr = videoram1[offset+1];
	SET_TILE_INFO(1,(videoram1[offset] & 0xff) + ((attr & 0x07) << 8),(attr & 0x18) >> 3);
	tile_info.flags = TILE_FLIPXY((attr & 0xc0) >> 6);
}

static void fg_preupdate(void)
{
	videoram1 = (const unsigned short *)bionicc_fgvideoram;
}

static void get_fg_tile_info( int col, int row )
{
	int offset = 2*(row*64 + col);
	int attr = videoram1[offset+1];
	SET_TILE_INFO(2,(videoram1[offset] & 0xff) + ((attr & 0x07) << 8),(attr & 0x18) >> 3);
	if ((attr & 0xc0) == 0xc0)
	{
		tile_info.priority = 2;
		tile_info.flags = 0;
	}
	else
	{
		tile_info.priority = (attr & 0x20) >> 5;
		tile_info.flags = TILE_FLIPXY((attr & 0xc0) >> 6);
	}
}

static void tx_preupdate(void)
{
	videoram1 = (const unsigned short *)bionicc_txvideoram;
	videoram2 = (const unsigned short *)&bionicc_txvideoram[0x800];
}

static void get_tx_tile_info( int col, int row )
{
	int offset = row*32+col;
	int attr = videoram2[offset];
	SET_TILE_INFO(0,(videoram1[offset] & 0xff) + ((attr & 0x00c0) << 2),attr & 0x3f);
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

int bionicc_vh_start(void)
{
	tx_tilemap = tilemap_create(
		get_tx_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		32,32
	);
	fg_tilemap = tilemap_create(
		get_fg_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		64,64
	);
	bg_tilemap = tilemap_create(
		get_bg_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		64,64
	);

	if (fg_tilemap && bg_tilemap && tx_tilemap)
	{
		tx_tilemap->transparent_pen = 3;
		fg_tilemap->transparent_pen = 15;
		bg_tilemap->transparent_pen = 15;

		return 0;
	}

	return 1;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

void bionicc_bgvideoram_w(int offset,int data)
{
	int oldword = READ_WORD(&bionicc_bgvideoram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		int tile_index = offset/4;
		WRITE_WORD(&bionicc_bgvideoram[offset],newword);
		tilemap_mark_tile_dirty(bg_tilemap,tile_index%64,tile_index/64);
	}
}

void bionicc_fgvideoram_w(int offset,int data)
{
	int oldword = READ_WORD(&bionicc_fgvideoram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		int tile_index = offset/4;
		WRITE_WORD(&bionicc_fgvideoram[offset],newword);
		tilemap_mark_tile_dirty(fg_tilemap,tile_index%64,tile_index/64);
	}
}

void bionicc_txvideoram_w(int offset,int data)
{
	int oldword = READ_WORD(&bionicc_txvideoram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		int tile_index = (offset&0x7ff)/2;
		WRITE_WORD(&bionicc_txvideoram[offset],newword);
		tilemap_mark_tile_dirty(tx_tilemap,tile_index%32,tile_index/32);
	}
}

int bionicc_bgvideoram_r(int offset)
{
	return READ_WORD(&bionicc_bgvideoram[offset]);
}

int bionicc_fgvideoram_r(int offset)
{
	return READ_WORD(&bionicc_fgvideoram[offset]);
}

int bionicc_txvideoram_r(int offset)
{
	return READ_WORD(&bionicc_txvideoram[offset]);
}

void bionicc_paletteram_w(int offset,int data)
{
	paletteram_RRRRGGGGBBBBIIII_word_w(offset,(data & 0xfff1) | ((data & 0x0007) << 1));
}

void bionicc_scroll_w(int offset,int data)
{
	switch( offset )
	{
		case 0:
			tilemap_set_scrollx(fg_tilemap,0,data);
			break;
		case 2:
			tilemap_set_scrolly(fg_tilemap,0,data);
			break;
		case 4:
			tilemap_set_scrollx(bg_tilemap,0,data);
			break;
		case 6:
			tilemap_set_scrolly(bg_tilemap,0,data);
			break;
	}
}

void bionicc_gfxctrl_w(int offset,int data)
{
	data >>= 8;

	flipscreen = data & 1;
	tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	tilemap_set_enable(bg_tilemap,data & 0x20);	/* guess */
	tilemap_set_enable(fg_tilemap,data & 0x10);	/* guess */

	coin_counter_w(0,data & 0x80);
	coin_counter_w(1,data & 0x40);
}



/***************************************************************************

  Display refresh

***************************************************************************/

static void bionicc_draw_sprites( struct osd_bitmap *bitmap )
{
	int offs;
	const struct GfxElement *gfx = Machine->gfx[3];
	const struct rectangle *clip = &Machine->drv->visible_area;

	for (offs = spriteram_size-8;offs >= 0;offs -= 8)
	{
		int tile_number = READ_WORD(&buffered_spriteram[offs])&0x7ff;
		if( tile_number!=0x7FF ){
			int attr = READ_WORD(&buffered_spriteram[offs+2]);
			int color = (attr&0x3C)>>2;
			int flipx = attr&0x02;
			int flipy = 0;
			int sx= (signed short)READ_WORD(&buffered_spriteram[offs+6]);
			int sy= (signed short)READ_WORD(&buffered_spriteram[offs+4]);
			if(sy>512-16) sy-=512;
			if (flipscreen)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}

			drawgfx( bitmap, gfx,
				tile_number,
				color,
				flipx,flipy,
				sx,sy,
				clip,TRANSPARENCY_PEN,15);
		}
	}
}

void mark_sprite_colors( void )
{
	int offs, code, color, i, pal_base;
	int colmask[16];

	pal_base = Machine->drv->gfxdecodeinfo[3].color_codes_start;
	for(i=0;i<16;i++) colmask[i] = 0;

	for (offs = 0; offs < 0x500;offs += 8)
	{

		code = READ_WORD(&buffered_spriteram[offs]) & 0x7ff;
		if( code != 0x7FF ) {
			color = (READ_WORD(&buffered_spriteram[offs+2]) & 0x3c) >> 2;
			colmask[color] |= Machine->gfx[3]->pen_usage[code];
		}
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

void bionicc_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	fg_preupdate(); tilemap_update(fg_tilemap);
	bg_preupdate(); tilemap_update(bg_tilemap);
	tx_preupdate(); tilemap_update(tx_tilemap);

	palette_init_used_colors();
	mark_sprite_colors();
	palette_used_colors[0] |= PALETTE_COLOR_VISIBLE;

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	fillbitmap(bitmap,Machine->pens[0],&Machine->drv->visible_area);
	tilemap_draw(bitmap,fg_tilemap,2);
	tilemap_draw(bitmap,bg_tilemap,0);
	tilemap_draw(bitmap,fg_tilemap,0);
	bionicc_draw_sprites(bitmap);
	tilemap_draw(bitmap,fg_tilemap,1);
	tilemap_draw(bitmap,tx_tilemap,0);
}

void bionicc_eof_callback(void)
{
	/* Mish: Spriteram is always 1 frame ahead, suggesting buffering.  I can't
		find a register to control this so I assume it happens automatically
		every frame at the end of vblank */
	buffer_spriteram_w(0,0);
}
