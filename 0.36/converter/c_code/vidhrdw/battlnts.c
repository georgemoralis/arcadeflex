#include "driver.h"
#include "vidhrdw/generic.h"
#include "vidhrdw/konamiic.h"

static int spritebank;

static int layer_colorbase[2];

/***************************************************************************

  Callback for the K007342

***************************************************************************/

static void tile_callback(int layer, int bank, int *code, int *color)
{
	*code |= ((*color & 0x0f) << 9) | ((*color & 0x40) << 2);
	*color = layer_colorbase[layer];
}

/***************************************************************************

  Callback for the K007420

***************************************************************************/

static void sprite_callback(int *code,int *color)
{
	*code |= ((*color & 0xc0) << 2) | spritebank;
	*code = (*code << 2) | ((*color & 0x30) >> 4);
	*color = 0;
}

void battlnts_spritebank_w(int offset,int data)
{
	spritebank = 1024 * (data & 1);
}

/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int battlnts_vh_start(void)
{
	layer_colorbase[0] = 0;
	layer_colorbase[1] = 0;

	if (K007342_vh_start(0,tile_callback))
	{
		/* Battlantis use this as Work RAM */
		K007342_tilemap_set_enable(1, 0);
		return 1;
	}

	if (K007420_vh_start(1,sprite_callback))
	{
		K007420_vh_stop();
		return 1;
	}

	return 0;
}

void battlnts_vh_stop(void)
{
	K007342_vh_stop();
	K007420_vh_stop();
}

/***************************************************************************

  Screen Refresh

***************************************************************************/

void battlnts_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh){

	K007342_tilemap_update();

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render( ALL_TILEMAPS );

	K007342_tilemap_draw( bitmap, 0, TILEMAP_IGNORE_TRANSPARENCY );
	K007420_sprites_draw( bitmap );
	K007342_tilemap_draw( bitmap, 0, 1 | TILEMAP_IGNORE_TRANSPARENCY );
}
