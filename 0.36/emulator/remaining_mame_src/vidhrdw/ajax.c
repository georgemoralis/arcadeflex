/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include "vidhrdw/konamiic.h"


unsigned char ajax_priority;
static int layer_colorbase[3],sprite_colorbase,zoom_colorbase;


/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void tile_callback(int layer,int bank,int *code,int *color)
{
	*code |= ((*color & 0x0f) << 8) | (bank << 12);
	*color = layer_colorbase[layer] + ((*color & 0xf0) >> 4);
}


/***************************************************************************

  Callbacks for the K051960

***************************************************************************/

static void sprite_callback(int *code,int *color,int *priority)
{
	/* priority bits:
	   4 over zoom (0 = have priority)
	   5 over B    (0 = have priority) - is this used?
	   6 over A    (1 = have priority)
	*/
	*priority = (*color & 0x70) >> 4;
	*color = sprite_colorbase + (*color & 0x0f);
}


/***************************************************************************

  Callbacks for the K051316

***************************************************************************/

static void zoom_callback(int *code,int *color)
{
	*code |= ((*color & 0x07) << 8);
	*color = zoom_colorbase + ((*color & 0x08) >> 3);
}


/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int ajax_vh_start( void )
{
	layer_colorbase[0] = 64;
	layer_colorbase[1] = 0;
	layer_colorbase[2] = 32;
	sprite_colorbase = 16;
	zoom_colorbase = 6;	/* == 48 since it's 7-bit graphics */
	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
		return 1;
	if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
	{
		K052109_vh_stop();
		return 1;
	}
	if (K051316_vh_start_0(REGION_GFX3,7,zoom_callback))
	{
		K052109_vh_stop();
		K051960_vh_stop();
		return 1;
	}

	return 0;
}

void ajax_vh_stop( void )
{
	K052109_vh_stop();
	K051960_vh_stop();
	K051316_vh_stop_0();
}



/***************************************************************************

	Display Refresh

***************************************************************************/

void ajax_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh )
{
	K052109_tilemap_update();
	K051316_tilemap_update_0();

	palette_init_used_colors();
	K051960_mark_sprites_colors();
	/* set back pen for the zoom layer */
	palette_used_colors[(zoom_colorbase + 0) * 128] = PALETTE_COLOR_TRANSPARENT;
	palette_used_colors[(zoom_colorbase + 1) * 128] = PALETTE_COLOR_TRANSPARENT;
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	/* sprite priority bits:
	   0 over zoom (0 = have priority)
	   1 over B    (0 = have priority)
	   2 over A    (1 = have priority)
	*/
	if (ajax_priority)
	{
		/* basic layer order is B, zoom, A, F */

		/* pri = 2 have priority over zoom, not over A and B - is this used? */
		/* pri = 3 have priority over nothing - is this used? */
//		K051960_sprites_draw(bitmap,2,3);
		K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
		/* pri = 1 have priority over B, not over zoom and A - is this used? */
//		K051960_sprites_draw(bitmap,1,1);
		K051316_zoom_draw_0(bitmap);
		/* pri = 0 have priority over zoom and B, not over A */
		/* the game seems to just use pri 0. */
		K051960_sprites_draw(bitmap,0,0);
		K052109_tilemap_draw(bitmap,1,0);
		/* pri = 4 have priority over zoom, A and B */
		/* pri = 5 have priority over A and B, not over zoom - OPPOSITE TO BASIC ORDER! (stage 6 boss) */
		K051960_sprites_draw(bitmap,4,5);
		/* pri = 6 have priority over zoom and A, not over B - is this used? */
		/* pri = 7 have priority over A, not over zoom and B - is this used? */
//		K051960_sprites_draw(bitmap,5,7);
		K052109_tilemap_draw(bitmap,0,0);
	}
	else
	{
		/* basic layer order is B, A, zoom, F */

		/* pri = 2 have priority over zoom, not over A and B - is this used? */
		/* pri = 3 have priority over nothing - is this used? */
//		K051960_sprites_draw(bitmap,2,3);
		K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
		/* pri = 0 have priority over zoom and B, not over A - OPPOSITE TO BASIC ORDER! */
		/* pri = 1 have priority over B, not over zoom and A */
		/* the game seems to just use pri 0. */
		K051960_sprites_draw(bitmap,0,1);
		K052109_tilemap_draw(bitmap,1,0);
		K051316_zoom_draw_0(bitmap);
		/* pri = 4 have priority over zoom, A and B */
		K051960_sprites_draw(bitmap,4,4);
		/* pri = 5 have priority over A and B, not over zoom - is this used? */
		/* pri = 6 have priority over zoom and A, not over B - is this used? */
		/* pri = 7 have priority over A, not over zoom and B - is this used? */
//		K051960_sprites_draw(bitmap,5,7);
		K052109_tilemap_draw(bitmap,0,0);
	}
}
