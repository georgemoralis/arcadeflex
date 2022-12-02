#include "driver.h"
#include "vidhrdw/konamiic.h"


static int layer_colorbase[3],sprite_colorbase;

/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void tile_callback(int layer,int bank,int *code,int *color)
{
	*code |= ((*color & 0x3f) << 8) | (bank << 14);
	*color = layer_colorbase[layer] + ((*color & 0xc0) >> 6);
}


/***************************************************************************

  Callbacks for the K051960

***************************************************************************/

static void sprite_callback(int *code,int *color,int *priority)
{
	/* Weird priority scheme. Why use three bits when two would suffice? */
	/* The PROM allows for mixed priorities, where sprites would have */
	/* priority over text but not on one or both of the other two planes. */
	/* Luckily, this isn't used by the game. */
	switch (*color & 0x70)
	{
		case 0x10: *priority = 0; break;
		case 0x00: *priority = 1; break;
		case 0x40: *priority = 2; break;
		case 0x20: *priority = 3; break;
		/*   0x60 == 0x20 */
		/*   0x50 priority over F and A, but not over B */
		/*   0x30 priority over F, but not over A and B */
		/*   0x70 == 0x30 */
	}
	*code |= (*color & 0x80) << 6;
	*color = sprite_colorbase + (*color & 0x0f);
}



/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int aliens_vh_start( void )
{
	paletteram = malloc(0x400);
	if (!paletteram) return 1;

	layer_colorbase[0] = 0;
	layer_colorbase[1] = 4;
	layer_colorbase[2] = 8;
	sprite_colorbase = 16;
	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
	{
		free(paletteram);
		return 1;
	}
	if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
	{
		free(paletteram);
		K052109_vh_stop();
		return 1;
	}

	return 0;
}

void aliens_vh_stop( void )
{
	free(paletteram);
	K052109_vh_stop();
	K051960_vh_stop();
}



/***************************************************************************

  Display refresh

***************************************************************************/

void aliens_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	K052109_tilemap_update();

	palette_init_used_colors();
	K051960_mark_sprites_colors();
	palette_used_colors[layer_colorbase[1] * 16] |= PALETTE_COLOR_VISIBLE;
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	fillbitmap(bitmap,Machine->pens[layer_colorbase[1] * 16],&Machine->drv->visible_area);
	K051960_sprites_draw(bitmap,3,3);
	K052109_tilemap_draw(bitmap,1,0);
	K051960_sprites_draw(bitmap,2,2);
	K052109_tilemap_draw(bitmap,2,0);
	K051960_sprites_draw(bitmap,1,1);
	K052109_tilemap_draw(bitmap,0,0);
	K051960_sprites_draw(bitmap,0,0);
}
