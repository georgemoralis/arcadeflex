#include "driver.h"
#include "vidhrdw/generic.h"
#include "vidhrdw/konamiic.h"

static int bg_colorbase,sprite_colorbase,layer_colorbase[3];
unsigned char *simpsons_xtraram;



/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void tile_callback(int layer,int bank,int *code,int *color)
{
	*code |= ((*color & 0x3f) << 8) | (bank << 14);
	*color = layer_colorbase[layer] + ((*color & 0xc0) >> 6);
}


/***************************************************************************

  Callbacks for the K053247

***************************************************************************/

static void sprite_callback(int *code,int *color,int *priority)
{
	*priority = (*color & 0x0f80) >> 6;	/* ??????? */
	*color = sprite_colorbase + (*color & 0x001f);
}


/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int simpsons_vh_start( void )
{
	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
		return 1;
	if (K053247_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
	{
		K052109_vh_stop();
		return 1;
	}

	return 0;
}

void simpsons_vh_stop( void )
{
	K052109_vh_stop();
	K053247_vh_stop();
}



/***************************************************************************

  Display refresh

***************************************************************************/

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

void simpsons_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int pri[3],layer[3];


	bg_colorbase       = K053251_get_palette_index(K053251_CI0);
	sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
	layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
	layer_colorbase[1] = K053251_get_palette_index(K053251_CI3);
	layer_colorbase[2] = K053251_get_palette_index(K053251_CI4);

	K052109_tilemap_update();

	palette_init_used_colors();
	K053247_mark_sprites_colors();
	palette_used_colors[16 * bg_colorbase] |= PALETTE_COLOR_VISIBLE;
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	layer[0] = 0;
	pri[0] = K053251_get_priority(K053251_CI2);
	layer[1] = 1;
	pri[1] = K053251_get_priority(K053251_CI3);
	layer[2] = 2;
	pri[2] = K053251_get_priority(K053251_CI4);

	sortlayers(layer,pri);

	fillbitmap(bitmap,Machine->pens[16 * bg_colorbase],&Machine->drv->visible_area);
	K053247_sprites_draw(bitmap,pri[0]+1,0x3f);
	K052109_tilemap_draw(bitmap,layer[0],0);
	K053247_sprites_draw(bitmap,pri[1]+1,pri[0]);
	K052109_tilemap_draw(bitmap,layer[1],0);
	K053247_sprites_draw(bitmap,pri[2]+1,pri[1]);
	K052109_tilemap_draw(bitmap,layer[2],0);
	K053247_sprites_draw(bitmap,0,pri[2]);
}

/***************************************************************************

  Extra video banking

***************************************************************************/

static int simpsons_K052109_r(int offset)
{
	return K052109_r(offset + 0x2000);
}

static void simpsons_K052109_w(int offset,int data)
{
	K052109_w(offset + 0x2000,data);
}

static int simpsons_K053247_r(int offset)
{
	if (offset < 0x1000) return K053247_r(offset);
	else return simpsons_xtraram[offset - 0x1000];
}

static void simpsons_K053247_w(int offset,int data)
{
	if (offset < 0x1000) K053247_w(offset,data);
	else simpsons_xtraram[offset - 0x1000] = data;
}

void simpsons_video_banking(int bank)
{
	if (bank & 1)
	{
		cpu_setbankhandler_r(3,paletteram_r);
		cpu_setbankhandler_w(3,paletteram_xBBBBBGGGGGRRRRR_swap_w);
	}
	else
	{
		cpu_setbankhandler_r(3,K052109_r);
		cpu_setbankhandler_w(3,K052109_w);
	}

	if (bank & 2)
	{
		cpu_setbankhandler_r(4,simpsons_K053247_r);
		cpu_setbankhandler_w(4,simpsons_K053247_w);
	}
	else
	{
		cpu_setbankhandler_r(4,simpsons_K052109_r);
		cpu_setbankhandler_w(4,simpsons_K052109_w);
	}
}
