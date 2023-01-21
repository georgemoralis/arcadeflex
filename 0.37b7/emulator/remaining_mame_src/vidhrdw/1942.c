/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"



unsigned char *c1942_fgvideoram;
unsigned char *c1942_bgvideoram;
unsigned char *c1942_spriteram;
size_t c1942_spriteram_size;

static data_t c1942_palette_bank;
static struct tilemap *fg_tilemap, *bg_tilemap;



/***************************************************************************

  Convert the color PROMs into a more useable format.

  1942 has three 256x4 palette PROMs (one per gun) and three 256x4 lookup
  table PROMs (one for characters, one for sprites, one for background tiles).
  The palette PROMs are connected to the RGB output this way:

  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
        -- 470 ohm resistor  -- RED/GREEN/BLUE
        -- 1  kohm resistor  -- RED/GREEN/BLUE
  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE

***************************************************************************/
void c1942_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])


	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		int bit0,bit1,bit2,bit3;


		/* red component */
		bit0 = (color_prom[0] >> 0) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		/* green component */
		bit0 = (color_prom[Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		/* blue component */
		bit0 = (color_prom[2*Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[2*Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[2*Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[2*Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		color_prom++;
	}

	color_prom += 2*Machine->drv->total_colors;
	/* color_prom now points to the beginning of the lookup table */


	/* characters use colors 128-143 */
	for (i = 0;i < TOTAL_COLORS(0);i++)
		COLOR(0,i) = *(color_prom++) + 128;

	/* background tiles use colors 0-63 in four banks */
	for (i = 0;i < TOTAL_COLORS(1)/4;i++)
	{
		COLOR(1,i) = *color_prom;
		COLOR(1,i+32*8) = *color_prom + 16;
		COLOR(1,i+2*32*8) = *color_prom + 32;
		COLOR(1,i+3*32*8) = *color_prom + 48;
		color_prom++;
	}

	/* sprites use colors 64-79 */
	for (i = 0;i < TOTAL_COLORS(2);i++)
		COLOR(2,i) = *(color_prom++) + 64;
}


/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void get_fg_tile_info(int tile_index)
{
	int code, color;

	code = c1942_fgvideoram[tile_index];
	color = c1942_fgvideoram[tile_index + 0x400];
	SET_TILE_INFO(0,code + ((color & 0x80) << 1),color & 0x3f)
}

static void get_bg_tile_info(int tile_index)
{
	int code, color;

	tile_index = (tile_index & 0x0f) | ((tile_index & 0x01f0) << 1);

	code = c1942_bgvideoram[tile_index];
	color = c1942_bgvideoram[tile_index + 0x10];
	SET_TILE_INFO(1,code + ((color & 0x80) << 1),(color & 0x1f) + (0x20 * c1942_palette_bank));
	tile_info.flags = TILE_FLIPYX((color & 0x60) >> 5);
}


/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/
int c1942_vh_start(void)
{
	fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,32,32);
	bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE,     16,16,32,16);

	if (!fg_tilemap || !bg_tilemap)
		return 1;

	fg_tilemap->transparent_pen = 0;

	return 0;
}


/***************************************************************************

  Memory handlers

***************************************************************************/

WRITE_HANDLER( c1942_fgvideoram_w )
{
	c1942_fgvideoram[offset] = data;
	tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
}

WRITE_HANDLER( c1942_bgvideoram_w )
{
	c1942_bgvideoram[offset] = data;
	tilemap_mark_tile_dirty(bg_tilemap,(offset & 0x0f) | ((offset >> 1) & 0x01f0));
}


WRITE_HANDLER( c1942_palette_bank_w )
{
	if (c1942_palette_bank != data)
		tilemap_mark_all_tiles_dirty(bg_tilemap);

	c1942_palette_bank = data;
}

WRITE_HANDLER( c1942_scroll_w )
{
	static unsigned char scroll[2];

	scroll[offset] = data;
	tilemap_set_scrollx(bg_tilemap,0,scroll[0] | (scroll[1] << 8));
}


WRITE_HANDLER( c1942_c804_w )
{
	/* bit 7: flip screen
       bit 4: cpu B reset
	   bit 0: coin counter */

	coin_counter_w(offset, data & 0x01);

	cpu_set_reset_line(1,(data & 0x10) ? ASSERT_LINE : CLEAR_LINE);

	flip_screen_w(offset, data & 0x80);
}


/***************************************************************************

  Display refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap)
{
	int offs;


	for (offs = c1942_spriteram_size - 4;offs >= 0;offs -= 4)
	{
		int i,code,col,sx,sy,dir;


		code = (c1942_spriteram[offs] & 0x7f) + 4*(c1942_spriteram[offs + 1] & 0x20)
				+ 2*(c1942_spriteram[offs] & 0x80);
		col = c1942_spriteram[offs + 1] & 0x0f;
		sx = c1942_spriteram[offs + 3] - 0x10 * (c1942_spriteram[offs + 1] & 0x10);
		sy = c1942_spriteram[offs + 2];
		dir = 1;
		if (flip_screen)
		{
			sx = 240 - sx;
			sy = 240 - sy;
			dir = -1;
		}

		/* handle double / quadruple height */
		i = (c1942_spriteram[offs + 1] & 0xc0) >> 6;
		if (i == 2) i = 3;

		do
		{
			drawgfx(bitmap,Machine->gfx[2],
					code + i,col,
					flip_screen,flip_screen,
					sx,sy + 16 * i * dir,
					&Machine->visible_area,TRANSPARENCY_PEN,15);

			i--;
		} while (i >= 0);
	}


}

void c1942_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update(ALL_TILEMAPS);
	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap,bg_tilemap,0);
	draw_sprites(bitmap);
	tilemap_draw(bitmap,fg_tilemap,0);
}
