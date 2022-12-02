#include "driver.h"
#include "vidhrdw/konamiic.h"


#define TOTAL_CHARS 0x1000
#define TOTAL_SPRITES 0x4000

unsigned char *gradius3_gfxram;
int gradius3_priority;
static int layer_colorbase[3],sprite_colorbase;
static int dirtygfx;
static unsigned char *dirtychar;



/***************************************************************************

  Callbacks for the K052109

***************************************************************************/

static void gradius3_tile_callback(int layer,int bank,int *code,int *color)
{
	/* (color & 0x02) is flip y handled internally by the 052109 */
	*code |= ((*color & 0x01) << 8) | ((*color & 0x1c) << 7);
	*color = layer_colorbase[layer] + ((*color & 0xe0) >> 5);
}



/***************************************************************************

  Callbacks for the K051960

***************************************************************************/

static void gradius3_sprite_callback(int *code,int *color,int *priority)
{
	*code |= (*color & 0x01) << 13;
	*priority = ((*color & 0x60) >> 5);
	*color = sprite_colorbase + ((*color & 0x1e) >> 1);
}



/***************************************************************************

  Start the video hardware emulation.

***************************************************************************/

int gradius3_vh_start(void)
{
	int i;
	static struct GfxLayout spritelayout =
	{
		8,8,
		TOTAL_SPRITES,
		4,
		{ 0, 1, 2, 3 },
		{ 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4,
				32*8+2*4, 32*8+3*4, 32*8+0*4, 32*8+1*4, 32*8+6*4, 32*8+7*4, 32*8+4*4, 32*8+5*4 },
		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
				64*8+0*32, 64*8+1*32, 64*8+2*32, 64*8+3*32, 64*8+4*32, 64*8+5*32, 64*8+6*32, 64*8+7*32 },
		128*8
	};


	layer_colorbase[0] = 0;
	layer_colorbase[1] = 32;
	layer_colorbase[2] = 48;
	sprite_colorbase = 16;
	if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,gradius3_tile_callback))
		return 1;
	if (K051960_vh_start(REGION_GFX2,REVERSE_PLANE_ORDER,gradius3_sprite_callback))
	{
		K052109_vh_stop();
		return 1;
	}

	/* re-decode the sprites because the ROMs are corrected to the custom IC differently
	   from how they are connected to the CPU. */
	for (i = 0;i < TOTAL_SPRITES;i++)
		decodechar(Machine->gfx[1],i,memory_region(REGION_GFX2),&spritelayout);

	if (!(dirtychar = malloc(TOTAL_CHARS)))
	{
		K052109_vh_stop();
		K051960_vh_stop();
		return 1;
	}

	memset(dirtychar,1,TOTAL_CHARS);

	return 0;
}

void gradius3_vh_stop(void)
{
	K052109_vh_stop();
	K051960_vh_stop();
}



/***************************************************************************

  Memory handlers

***************************************************************************/

int gradius3_gfxrom_r(int offset)
{
	unsigned char *gfxdata = memory_region(REGION_GFX2);

	return (gfxdata[offset+1] << 8) | gfxdata[offset];
}

int gradius3_gfxram_r(int offset)
{
	return READ_WORD(&gradius3_gfxram[offset]);
}

void gradius3_gfxram_w(int offset,int data)
{
	int oldword = READ_WORD(&gradius3_gfxram[offset]);
	int newword = COMBINE_WORD(oldword,data);

	if (oldword != newword)
	{
		dirtygfx = 1;
		dirtychar[offset / 32] = 1;
		WRITE_WORD(&gradius3_gfxram[offset],newword);
	}
}



/***************************************************************************

  Display refresh

***************************************************************************/

void gradius3_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	static struct GfxLayout charlayout =
	{
		8,8,
		TOTAL_CHARS,
		4,
		{ 0, 1, 2, 3 },
#ifdef LSB_FIRST
		{ 2*4, 3*4, 0*4, 1*4, 6*4, 7*4, 4*4, 5*4 },
#else
		{ 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4 },
#endif
		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32 },
		32*8
	};

	/* TODO: this kludge enforces the char banks. For some reason, they don't work otherwise. */
	K052109_w(0x1d80,0x10);
	K052109_w(0x1f00,0x32);

	if (dirtygfx)
	{
		int i;

		dirtygfx = 0;

		for (i = 0;i < TOTAL_CHARS;i++)
		{
			if (dirtychar[i])
			{
				dirtychar[i] = 0;
				decodechar(Machine->gfx[0],i,gradius3_gfxram,&charlayout);
			}
		}

		tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
	}

	K052109_tilemap_update();

	palette_init_used_colors();
	K051960_mark_sprites_colors();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	if (gradius3_priority == 0)
	{
//		K051960_sprites_draw(bitmap,3,3);	/* are these used? */
		K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY);
		K051960_sprites_draw(bitmap,0,0);
		K051960_sprites_draw(bitmap,2,2);	/* are these used? */
		K052109_tilemap_draw(bitmap,2,0);
		K051960_sprites_draw(bitmap,1,1);
		K052109_tilemap_draw(bitmap,0,0);
	}
	else
	{
//		K051960_sprites_draw(bitmap,3,3);	/* are these used? */
		K052109_tilemap_draw(bitmap,0,TILEMAP_IGNORE_TRANSPARENCY);
		K051960_sprites_draw(bitmap,0,0);
		K052109_tilemap_draw(bitmap,1,0);
		K051960_sprites_draw(bitmap,1,1);
		K052109_tilemap_draw(bitmap,2,0);
		K051960_sprites_draw(bitmap,2,2);	/* are these used? */
	}
}
