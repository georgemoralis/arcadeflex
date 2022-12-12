#include "driver.h"
#include "vidhrdw/konamiic.h"


int scontra_priority;
static int layer_colorbase[3],sprite_colorbase;

/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void tile_callback(int layer,int bank,int *code,int *color)
{
	*code |= ((*color & 0x1f) << 8) | (bank << 13);
	*color = layer_colorbase[layer] + ((*color & 0xe0) >> 5);
}


/***************************************************************************

  Callbacks for the K051960

***************************************************************************/

static void sprite_callback(int *code,int *color,int *priority)
{
	*priority = (*color & 0x30) >> 4;
	*color = sprite_colorbase + (*color & 0x0f);
}



/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int scontra_vh_start(void)
{
	layer_colorbase[0] = 48;
	layer_colorbase[1] = 0;
	layer_colorbase[2] = 16;
	sprite_colorbase = 32;

	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
		return 1;
	if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
	{
		K052109_vh_stop();
		return 1;
	}

	return 0;
}

void scontra_vh_stop(void)
{
	K052109_vh_stop();
	K051960_vh_stop();
}


void scontra_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	K052109_tilemap_update();

	palette_init_used_colors();
	K051960_mark_sprites_colors();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	/* Sprite priority 1 means appear behind background, used only to mask sprites */
	/* in the foreground */
	/* Sprite priority 3 means not draw (not used) */
	/* The background color is always from layer 1 - but it's always black anyway */
	if (scontra_priority)
	{
//		fillbitmap(bitmap,Machine->pens[16 * layer_colorbase[1]],&Machine->drv->visible_area);
//		K051960_sprites_draw(bitmap,1,1);
		K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
		K051960_sprites_draw(bitmap,2,2);
		K052109_tilemap_draw(bitmap,1,0);
		K051960_sprites_draw(bitmap,0,0);
		K052109_tilemap_draw(bitmap,0,0);
	}
	else
	{
//		fillbitmap(bitmap,Machine->pens[16 * layer_colorbase[1]],&Machine->drv->visible_area);
//		K051960_sprites_draw(bitmap,1,1);
		K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY);
		K051960_sprites_draw(bitmap,2,2);
		K052109_tilemap_draw(bitmap,2,0);
		K051960_sprites_draw(bitmap,0,0);
		K052109_tilemap_draw(bitmap,0,0);
	}
}
