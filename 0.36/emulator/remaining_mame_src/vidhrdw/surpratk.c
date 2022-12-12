#include "driver.h"
#include "vidhrdw/konamiic.h"

static int layer_colorbase[3],sprite_colorbase,bg_colorbase;

/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void tile_callback(int layer,int bank,int *code,int *color)
{
	tile_info.flags = (*color & 0x80) ? TILE_FLIPX : 0;
	*code |= ((*color & 0x03) << 8) | ((*color & 0x10) << 6) | ((*color & 0x0c) << 9) | (bank << 13);
	*color = layer_colorbase[layer] + ((*color & 0x60) >> 5);
}

/***************************************************************************

  Callbacks for the K053245

***************************************************************************/

static void sprite_callback(int *code,int *color,int *priority)
{
	*priority = 0x20 | ((*color & 0x60) >> 2);
	*color = sprite_colorbase + (*color & 0x1f);
}


/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int surpratk_vh_start( void )
{
	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
	{
		return 1;
	}
	if (K053245_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
	{
		K052109_vh_stop();
		return 1;
	}

	return 0;
}

void surpratk_vh_stop( void )
{
	K052109_vh_stop();
	K053245_vh_stop();
}

/* useful function to sort the three tile layers by priority order */
static void sortlayers(int *layer,int *pri)
{
#define SWAP(a,b) \
	if (pri[a] < pri[b]) \
	{ \
		int t; \
		t = pri[a]; pri[a] = pri[b]; pri[b] = t; \
		t = layer[a]; layer[a] = layer[b]; layer[b] = t; \
	}

	SWAP(0,1)
	SWAP(0,2)
	SWAP(1,2)
}

void surpratk_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int pri[3],layer[3];


	bg_colorbase       = K053251_get_palette_index(K053251_CI0);
	sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
	layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
	layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
	layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);

	K052109_tilemap_update();

	palette_init_used_colors();
	K053245_mark_sprites_colors();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	layer[0] = 0;
	pri[0] = K053251_get_priority(K053251_CI2);
	layer[1] = 1;
	pri[1] = K053251_get_priority(K053251_CI4);
	layer[2] = 2;
	pri[2] = K053251_get_priority(K053251_CI3);

	sortlayers(layer,pri);

	fillbitmap(bitmap,Machine->pens[16 * bg_colorbase],&Machine->drv->visible_area);
	K053245_sprites_draw(bitmap,pri[0]+1,0x3f);
	K052109_tilemap_draw(bitmap,layer[0],0);
	K053245_sprites_draw(bitmap,pri[1]+1,pri[0]);
	K052109_tilemap_draw(bitmap,layer[1],0);
	K053245_sprites_draw(bitmap,pri[2]+1,pri[1]);
	K052109_tilemap_draw(bitmap,layer[2],0);
	K053245_sprites_draw(bitmap,0,pri[2]);
}
