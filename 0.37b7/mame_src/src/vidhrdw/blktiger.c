#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *blktiger_txvideoram;

#define BGRAM_BANK_SIZE 0x1000
#define BGRAM_BANKS 4

static int blktiger_scroll_bank;
static unsigned char *scroll_ram;
static int screen_layout;
static int chon,objon,bgon;

static struct tilemap *tx_tilemap,*bg_tilemap8x4,*bg_tilemap4x8;


/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static UINT32 bg8x4_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
{
	/* logical (col,row) -> memory offset */
	return (col & 0x0f) + ((row & 0x0f) << 4) + ((col & 0x70) << 4) + ((row & 0x30) << 7);
}

static UINT32 bg4x8_scan(UINT32 col,UINT32 row,UINT32 num_cols,UINT32 num_rows)
{
	/* logical (col,row) -> memory offset */
	return (col & 0x0f) + ((row & 0x0f) << 4) + ((col & 0x30) << 4) + ((row & 0x70) << 6);
}

static void get_bg_tile_info(int tile_index)
{
	/* the tile priority table is a guess compiled by looking at the game. It
	   was not derived from a PROM so it could be wrong. */
	static int split_table[16] =
	{
		3,0,2,2,	/* the fourth could be 1 instead of 2 */
		0,1,0,0,
		0,0,0,0,
		0,0,0,0
	};
	unsigned char attr = scroll_ram[2*tile_index + 1];
	int color = (attr & 0x78) >> 3;
	SET_TILE_INFO(1,scroll_ram[2*tile_index] + ((attr & 0x07) << 8),color)
	tile_info.flags = TILE_SPLIT(split_table[color]);
	if (attr & 0x80) tile_info.flags |= TILE_FLIPX;
}

static void get_tx_tile_info(int tile_index)
{
	unsigned char attr = blktiger_txvideoram[tile_index + 0x400];
	SET_TILE_INFO(0,blktiger_txvideoram[tile_index] + ((attr & 0xe0) << 3),attr & 0x1f)
}


/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

void blktiger_vh_stop(void)
{
	free(scroll_ram);
	scroll_ram = NULL;
}

int blktiger_vh_start(void)
{
	scroll_ram = malloc(BGRAM_BANK_SIZE * BGRAM_BANKS);

	tx_tilemap = tilemap_create(get_tx_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	bg_tilemap8x4 = tilemap_create(get_bg_tile_info,bg8x4_scan,TILEMAP_TRANSPARENT | TILEMAP_SPLIT,16,16,128,64);
	bg_tilemap4x8 = tilemap_create(get_bg_tile_info,bg4x8_scan,TILEMAP_TRANSPARENT | TILEMAP_SPLIT,16,16,64,128);

	if (!scroll_ram || !tx_tilemap || !bg_tilemap8x4 || !bg_tilemap4x8)
	{
		blktiger_vh_stop();
		return 1;
	}

	tx_tilemap->transparent_pen = 3;
	bg_tilemap8x4->transparent_pen =
	bg_tilemap4x8->transparent_pen = 15;

	bg_tilemap8x4->transmask[0] =
	bg_tilemap4x8->transmask[0] = 0xffff;	/* split type 0 is totally transparent in front half */
	bg_tilemap8x4->transmask[1] =
	bg_tilemap4x8->transmask[1] = 0xfff0;	/* split type 1 has pens 4-15 transparent in front half */
	bg_tilemap8x4->transmask[2] =
	bg_tilemap4x8->transmask[2] = 0xff00;	/* split type 1 has pens 8-15 transparent in front half */
	bg_tilemap8x4->transmask[3] =
	bg_tilemap4x8->transmask[3] = 0xf000;	/* split type 1 has pens 12-15 transparent in front half */

	return 0;
}



/***************************************************************************

  Memory handlers

***************************************************************************/

WRITE_HANDLER( blktiger_txvideoram_w )
{
	if (blktiger_txvideoram[offset] != data)
	{
		blktiger_txvideoram[offset] = data;
		tilemap_mark_tile_dirty(tx_tilemap,offset & 0x3ff);
	}
}

READ_HANDLER( blktiger_bgvideoram_r )
{
	return scroll_ram[offset + blktiger_scroll_bank];
}

WRITE_HANDLER( blktiger_bgvideoram_w )
{
	offset += blktiger_scroll_bank;

	if (scroll_ram[offset] != data)
	{
		scroll_ram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap8x4,offset/2);
		tilemap_mark_tile_dirty(bg_tilemap4x8,offset/2);
	}
}

WRITE_HANDLER( blktiger_bgvideoram_bank_w )
{
	blktiger_scroll_bank = (data % BGRAM_BANKS) * BGRAM_BANK_SIZE;
}


WRITE_HANDLER( blktiger_scrolly_w )
{
	static unsigned char scroll[2];
	int scrolly;

	scroll[offset] = data;
	scrolly = scroll[0] | (scroll[1] << 8);
	tilemap_set_scrolly(bg_tilemap8x4,0,scrolly);
	tilemap_set_scrolly(bg_tilemap4x8,0,scrolly);
}

WRITE_HANDLER( blktiger_scrollx_w )
{
	static unsigned char scroll[2];
	int scrollx;

	scroll[offset] = data;
	scrollx = scroll[0] | (scroll[1] << 8);
	tilemap_set_scrollx(bg_tilemap8x4,0,scrollx);
	tilemap_set_scrollx(bg_tilemap4x8,0,scrollx);
}


WRITE_HANDLER( blktiger_video_control_w )
{
	/* bits 0 and 1 are coin counters */
	coin_counter_w(0,data & 1);
	coin_counter_w(1,data & 2);

	/* bit 5 resets the sound CPU */
	cpu_set_reset_line(1,(data & 0x20) ? ASSERT_LINE : CLEAR_LINE);

	/* bit 6 flips screen */
	flip_screen_w(0,data & 0x40);

	/* bit 7 enables characters? Just a guess */
	chon = ~data & 0x80;
}

WRITE_HANDLER( blktiger_video_enable_w )
{
	/* not sure which is which, but I think that bit 1 and 2 enable background and sprites */
	/* bit 1 enables bg ? */
	bgon = ~data & 0x02;

	/* bit 2 enables sprites ? */
	objon = ~data & 0x04;
}

WRITE_HANDLER( blktiger_screen_layout_w )
{
	screen_layout = data;
	tilemap_set_enable(bg_tilemap8x4, screen_layout);
	tilemap_set_enable(bg_tilemap4x8,!screen_layout);
}



/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;

	/* Draw the sprites. */
	for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
	{
		int attr = buffered_spriteram[offs+1];
		int sx = buffered_spriteram[offs + 3] - ((attr & 0x10) << 4);
		int sy = buffered_spriteram[offs + 2];
		int code = buffered_spriteram[offs] | ((attr & 0xe0) << 3);
		int color = attr & 0x07;
		int flipx = attr & 0x08;

		if (flip_screen)
		{
			sx = 240 - sx;
			sy = 240 - sy;
			flipx = !flipx;
		}

		drawgfx(bitmap,Machine->gfx[2],
				code,
				color,
				flipx,flip_screen,
				sx,sy,
				&Machine->visible_area,TRANSPARENCY_PEN,15);
	}
}

static void mark_sprites_colors(void)
{
	int offs;

	for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
	{
		int attr = buffered_spriteram[offs+1];
		int sx = buffered_spriteram[offs + 3] - ((attr & 0x10) << 4);
		int sy = buffered_spriteram[offs + 2];

		/* only count visible sprites */
		if (sx+15 >= Machine->visible_area.min_x &&
				sx <= Machine->visible_area.max_x &&
				sy+15 >= Machine->visible_area.min_y &&
				sy <= Machine->visible_area.max_y)
		{
			int i;

			int color = attr & 0x07;
			int code = buffered_spriteram[offs] | ((attr & 0xe0) << 3);

			for (i = 0;i < 15;i++)
			{
				if (Machine->gfx[2]->pen_usage[code] & (1 << i))
					palette_used_colors[512 + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	}
}

void blktiger_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);

	palette_init_used_colors();
	mark_sprites_colors();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	fillbitmap(bitmap,palette_transparent_pen,&Machine->visible_area);

	if (bgon)
		tilemap_draw(bitmap,screen_layout ? bg_tilemap8x4 : bg_tilemap4x8,TILEMAP_BACK);

	if (objon)
		draw_sprites(bitmap);

	if (bgon)
		tilemap_draw(bitmap,screen_layout ? bg_tilemap8x4 : bg_tilemap4x8,TILEMAP_FRONT);

	if (chon)
		tilemap_draw(bitmap,tx_tilemap,0);
}

void blktiger_eof_callback(void)
{
	buffer_spriteram_w(0,0);
}
