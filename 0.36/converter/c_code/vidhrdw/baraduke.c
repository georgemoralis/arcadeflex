#include "driver.h"
#include "vidhrdw/generic.h"
#include "tilemap.h"

unsigned char *baraduke_textram, *baraduke_videoram;

static struct tilemap *tilemap[2];	/* backgrounds */
static int xscroll[2], yscroll[2];	/* scroll registers */

/***************************************************************************

	Convert the color PROMs into a more useable format.

	The palette PROMs are connected to the RGB output this way:

	bit 3	-- 220 ohm resistor  -- RED/GREEN/BLUE
			-- 470 ohm resistor  -- RED/GREEN/BLUE
			-- 1  kohm resistor  -- RED/GREEN/BLUE
	bit 0	-- 2.2kohm resistor  -- RED/GREEN/BLUE

***************************************************************************/

void baraduke_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	int bit0,bit1,bit2,bit3;

	for (i = 0; i < 2048; i++){
		/* red component */
		bit0 = (color_prom[2048] >> 0) & 0x01;
		bit1 = (color_prom[2048] >> 1) & 0x01;
		bit2 = (color_prom[2048] >> 2) & 0x01;
		bit3 = (color_prom[2048] >> 3) & 0x01;
		*(palette++) = 0x0e*bit0 + 0x1f*bit1 + 0x43*bit2 + 0x8f*bit3;

		/* green component */
		bit0 = (color_prom[0] >> 0) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*(palette++) = 0x0e*bit0 + 0x1f*bit1 + 0x43*bit2 + 0x8f*bit3;

		/* blue component */
		bit0 = (color_prom[0] >> 4) & 0x01;
		bit1 = (color_prom[0] >> 5) & 0x01;
		bit2 = (color_prom[0] >> 6) & 0x01;
		bit3 = (color_prom[0] >> 7) & 0x01;
		*(palette++) = 0x0e*bit0 + 0x1f*bit1 + 0x43*bit2 + 0x8f*bit3;

		color_prom++;
	}
}

/***************************************************************************

	Callbacks for the TileMap code

***************************************************************************/

static void get_tile_info0(int col,int row)
{
	int tile_index = 2*(64*row + col);
	unsigned char attr = baraduke_videoram[tile_index + 1];
	unsigned char code = baraduke_videoram[tile_index];

	SET_TILE_INFO(1 + ((attr & 0x02) >> 1), code | ((attr & 0x01) << 8), attr);
}

static void get_tile_info1(int col,int row)
{
	int tile_index = 2*(64*row + col);
	unsigned char attr = baraduke_videoram[0x1000 + tile_index + 1];
	unsigned char code = baraduke_videoram[0x1000 + tile_index];

	SET_TILE_INFO(3 + ((attr & 0x02) >> 1), code | ((attr & 0x01) << 8), attr);
}

/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int baraduke_vh_start( void )
{
	tilemap[0] = tilemap_create(get_tile_info0, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
	tilemap[1] = tilemap_create(get_tile_info1, TILEMAP_TRANSPARENT, 8, 8, 64, 32);

	if (tilemap[0] && tilemap[1]){
		tilemap[0]->transparent_pen = 7;
		tilemap[1]->transparent_pen = 7;

		return 0;
	}
	return 1;
}

/***************************************************************************

	Memory handlers

***************************************************************************/

int baraduke_videoram_r(int offset)
{
	return baraduke_videoram[offset];
}

void baraduke_videoram_w(int offset,int data)
{
	if (baraduke_videoram[offset] != data){
		baraduke_videoram[offset] = data;
		tilemap_mark_tile_dirty(tilemap[offset/0x1000],(offset/2)%64,((offset%0x1000)/2)/64);
	}
}

static void scroll_w(int layer,int offset,int data)
{
	int xdisp[2] = { 26, 24 };

	switch (offset){

		case 0:	/* high scroll x */
			xscroll[layer] = (xscroll[layer] & 0xff) | (data << 8);
			break;
		case 1:	/* low scroll x */
			xscroll[layer] = (xscroll[layer] & 0xff00) | data;
			break;
		case 2:	/* scroll y */
			yscroll[layer] = data;
			break;
	}

	tilemap_set_scrollx(tilemap[layer], 0, xscroll[layer] + xdisp[layer]);
	tilemap_set_scrolly(tilemap[layer], 0, yscroll[layer] + 25);
}

void baraduke_scroll0_w(int offset,int data)
{
	scroll_w(0, offset, data);
}
void baraduke_scroll1_w(int offset,int data)
{
	scroll_w(1, offset, data);
}

/***************************************************************************

	Display Refresh

***************************************************************************/

static void draw_sprites(struct osd_bitmap *bitmap, int priority)
{
	const struct rectangle *clip = &Machine->drv->visible_area;

	const unsigned char *source = &spriteram[0];
	const unsigned char *finish = &spriteram[0x0800-16];/* the last is NOT a sprite */

	int sprite_xoffs = spriteram[0x07f5] - 256 * (spriteram[0x07f4] & 1) + 16;
	int sprite_yoffs = spriteram[0x07f7] - 256 * (spriteram[0x07f6] & 1);

	while( source<finish )
	{
/*
	source[4]	S-FT ---P
	source[5]	TTTT TTTT
	source[6]   CCCC CCCX
	source[7]	XXXX XXXX
	source[8]	---T -S-F
	source[9]   YYYY YYYY
*/
		{
			unsigned char attrs = source[4];
			unsigned char attr2 = source[8];
			unsigned char color = source[6];
			int sx = source[7] + (color & 0x01)*256; /* need adjust for left clip */
			int sy = -source[9];
			int flipx = attrs & 0x20;
			int flipy = attr2 & 0x01;
			int tall = (attr2 & 0x04) ? 1 : 0;
			int wide = (attrs & 0x80) ? 1 : 0;
			int pri = attrs & 0x01;
			int sprite_number = (source[5] & 0xff)*4;
			int row,col;

			if (pri == priority)
			{
				if ((attrs & 0x10) && !wide) sprite_number += 1;
				if ((attr2 & 0x10) && !tall) sprite_number += 2;
				color = color >> 1;

				if( sx > 512 - 32 ) sx -= 512;

				if( flipx && !wide ) sx -= 16;
				if( !tall ) sy += 16;
				if( !tall && (attr2 & 0x10) && flipy ) sy -= 16;

				sx += sprite_xoffs;
				sy -= sprite_yoffs;

				for( row=0; row<=tall; row++ )
				{
					for( col=0; col<=wide; col++ )
					{
						drawgfx( bitmap, Machine->gfx[5],
								sprite_number+2*row+col,
								color,
								flipx,flipy,
								-87 + (sx+16*(flipx ? 1-col : col)),
								209 + (sy+16*(flipy ? 1-row : row)),
								clip,
								TRANSPARENCY_PEN, 0x0f );
					}
				}
			}
		}
		source+=16;
	}
}

static void mark_textlayer_colors(void)
{
	int i, offs;
	unsigned short palette_map[512];

	memset (palette_map, 0, sizeof (palette_map));

	for (offs = 0; offs < 0x400; offs++){
		palette_map[(baraduke_textram[offs+0x400] << 2) & 0x1ff] |= 0xffff;
	}

	/* now build the final table */
	for (i = 0; i < 512; i++){
		int usage = palette_map[i], j;
		if (usage){
			for (j = 0; j < 4; j++)
				if (usage & (1 << j))
					palette_used_colors[i * 4 + j] |= PALETTE_COLOR_VISIBLE;
		}
	}
}

static void mark_sprites_colors(void)
{
	int i;
	const unsigned char *source = &spriteram[0];
	const unsigned char *finish = &spriteram[0x0800-16];/* the last is NOT a sprite */

	unsigned short palette_map[128];

	memset (palette_map, 0, sizeof (palette_map));

	while( source<finish ){
		palette_map[source[6] >> 1] |= 0xffff;
		source+=16;
	}

	/* now build the final table */
	for (i = 0; i < 128; i++){
		int usage = palette_map[i], j;
		if (usage){
			for (j = 0; j < 16; j++)
				if (usage & (1 << j))
					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
		}
	}
}


void baraduke_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	tilemap_update(ALL_TILEMAPS);

	palette_init_used_colors();
	mark_textlayer_colors();
	mark_sprites_colors();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap,tilemap[1],TILEMAP_IGNORE_TRANSPARENCY);
	draw_sprites(bitmap,0);
	tilemap_draw(bitmap,tilemap[0],0);
	draw_sprites(bitmap,1);

	for (offs = 0x400 - 1; offs > 0; offs--){
		int mx,my,sx,sy;

        mx = offs % 32;
		my = offs / 32;

		if (my < 2)	{
			if (mx < 2 || mx >= 30) continue; /* not visible */
			sx = my + 34; sy = mx - 2;
		}
		else if (my >= 30){
			if (mx < 2 || mx >= 30) continue; /* not visible */
			sx = my - 30; sy = mx - 2;
		}
		else{
			sx = mx + 2; sy = my - 2;
		}
		drawgfx(bitmap,Machine->gfx[0],	baraduke_textram[offs],
				(baraduke_textram[offs+0x400] << 2) & 0x1ff,
				0,0,sx*8,sy*8,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,3);
	}
}

void metrocrs_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	tilemap_update(ALL_TILEMAPS);

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap,tilemap[0],TILEMAP_IGNORE_TRANSPARENCY);
	draw_sprites(bitmap,0);
	tilemap_draw(bitmap,tilemap[1],0);
	draw_sprites(bitmap,1);
	for (offs = 0x400 - 1; offs > 0; offs--){
		int mx,my,sx,sy;

        mx = offs % 32;
		my = offs / 32;

		if (my < 2)	{
			if (mx < 2 || mx >= 30) continue; /* not visible */
			sx = my + 34; sy = mx - 2;
		}
		else if (my >= 30){
			if (mx < 2 || mx >= 30) continue; /* not visible */
			sx = my - 30; sy = mx - 2;
		}
		else{
			sx = mx + 2; sy = my - 2;
		}
		drawgfx(bitmap,Machine->gfx[0],	baraduke_textram[offs],
				(baraduke_textram[offs+0x400] << 2) & 0x1ff,
				0,0,sx*8,sy*8,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,3);
	}
}
