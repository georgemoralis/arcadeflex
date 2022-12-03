#include "driver.h"
#include "vidhrdw/generic.h"

extern unsigned char *pandoras_sharedram;
static struct tilemap *layer0;

/***********************************************************************

  Convert the color PROMs into a more useable format.

  Pandora's Palace has one 32x8 palette PROM and two 256x4 lookup table
  PROMs (one for characters, one for sprites).
  The palette PROM is connected to the RGB output this way:

  bit 7 -- 220 ohm resistor  -- BLUE
        -- 470 ohm resistor  -- BLUE
        -- 220 ohm resistor  -- GREEN
        -- 470 ohm resistor  -- GREEN
        -- 1  kohm resistor  -- GREEN
        -- 220 ohm resistor  -- RED
        -- 470 ohm resistor  -- RED
  bit 0 -- 1  kohm resistor  -- RED

***************************************************************************/
void pandoras_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])

	for (i = 0; i < Machine->drv->total_colors; i++)
	{
		int bit0, bit1, bit2;

		/* red component */
		bit0 = (*color_prom >> 0) & 0x01;
		bit1 = (*color_prom >> 1) & 0x01;
		bit2 = (*color_prom >> 2) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

		/* green component */
		bit0 = (*color_prom >> 3) & 0x01;
		bit1 = (*color_prom >> 4) & 0x01;
		bit2 = (*color_prom >> 5) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

		/* blue component */
		bit0 = 0;
		bit1 = (*color_prom >> 6) & 0x01;
		bit2 = (*color_prom >> 7) & 0x01;
		*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;

		color_prom++;
	}

	/* color_prom now points to the beginning of the lookup table */

	/* sprites */
	for (i = 0;i < TOTAL_COLORS(1);i++)
		COLOR(1,i) = *(color_prom++) & 0x0f;

	/* characters */
	for (i = 0;i < TOTAL_COLORS(0);i++)
		COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
}

/***************************************************************************

  Callbacks for the TileMap code

***************************************************************************/

static void get_tile_info0(int col,int row)
{
	int tile_index = 32*row+col;
	unsigned char attr = colorram[tile_index];
	SET_TILE_INFO(0,videoram[tile_index] + ((attr & 0x10) << 4),attr & 0x0f);
	tile_info.flags = TILE_FLIPYX((attr & 0xc0) >> 6);
	tile_info.priority = (attr & 0x20) >> 5;
}

/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int pandoras_vh_start(void)
{
	layer0 = tilemap_create(get_tile_info0, TILEMAP_OPAQUE, 8, 8, 32, 32);

	if(layer0)
		return 0;
	else
		return 1;
}

/***************************************************************************

  Memory Handlers

***************************************************************************/

int pandoras_vram_r(int offset){
	return videoram[offset];
}

int pandoras_cram_r(int offset){
	return colorram[offset];
}

void pandoras_vram_w(int offset,int data){
	if (videoram[offset] != data){
		tilemap_mark_tile_dirty(layer0, offset%32, offset/32);
		videoram[offset] = data;
	}
}

void pandoras_cram_w(int offset,int data){
	if (colorram[offset] != data){
		tilemap_mark_tile_dirty(layer0, offset%32, offset/32);
		colorram[offset] = data;
	}
}

void pandoras_scrolly_w(int offset,int data)
{
	tilemap_set_scrolly(layer0,0,data);
}

/***************************************************************************

  Screen Refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap, unsigned char* sr){
	int offs;

	for (offs = 0; offs < 0x100; offs += 4){
		int sx,sy,flipx,flipy;

		sy = sr[offs] - 1;
		sx = 240 - sr[offs + 1];
		flipx = sr[offs + 3] & 0x40;
		flipy = sr[offs + 3] & 0x80;

		drawgfx(bitmap,Machine->gfx[1],
			sr[offs + 2],
			sr[offs + 3] & 0x0f,
			flipx,flipy,
			sx,sy,
			&Machine->drv->visible_area,TRANSPARENCY_COLOR,0);
	}
}

void pandoras_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_update( layer0 );
	tilemap_render( layer0 );

	tilemap_draw( bitmap, layer0, 1 );
	draw_sprites( bitmap, &pandoras_sharedram[0x800] );
	tilemap_draw( bitmap, layer0, 0 );
}
