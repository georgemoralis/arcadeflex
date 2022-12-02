
#include "driver.h"
#include "vidhrdw/generic.h"

static struct tilemap *background_layer,*foreground_layer,*text_layer;
unsigned char *raiden_back_data,*raiden_fore_data,*raiden_scroll_ram;

static int flipscreen,ALTERNATE;

/******************************************************************************/

int raiden_background_r(int offset)
{
	return raiden_back_data[offset];
}

int raiden_foreground_r(int offset)
{
	return raiden_fore_data[offset];
}

void raiden_background_w(int offset,int data)
{
	raiden_back_data[offset]=data;
	tilemap_mark_tile_dirty( background_layer,(offset/2)/32,(offset/2)%32 );
}

void raiden_foreground_w(int offset,int data)
{
	raiden_fore_data[offset]=data;
	tilemap_mark_tile_dirty( foreground_layer,(offset/2)/32,(offset/2)%32 );
}

void raiden_text_w(int offset,int data)
{
	videoram[offset]=data;
	tilemap_mark_tile_dirty( text_layer,(offset/2)/32,(offset/2)%32 );
}

void raidena_text_w(int offset,int data)
{
	videoram[offset]=data;
	tilemap_mark_tile_dirty( text_layer,(offset/2)%32,(offset/2)/32 );
}

static void get_back_tile_info( int col, int row )
{
	int offs=(row*2) + (col*64);
	int tile=raiden_back_data[offs]+(raiden_back_data[offs+1]<<8);
	int color=tile >> 12;

	tile=tile&0xfff;

	SET_TILE_INFO(1,tile,color)
}

static void get_fore_tile_info( int col, int row )
{
	int offs=(row*2) + (col*64);
	int tile=raiden_fore_data[offs]+(raiden_fore_data[offs+1]<<8);
	int color=tile >> 12;

	tile=tile&0xfff;

	SET_TILE_INFO(2,tile,color)
}

static void get_text_tile_info( int col, int row )
{
	int offs=(row*2) + (col*64);
	int tile=videoram[offs]+((videoram[offs+1]&0xc0)<<2);
	int color=videoram[offs+1]&0xf;

	SET_TILE_INFO(0,tile,color)
}

static void get_text_alt_tile_info( int col, int row )
{
	int offs=(col*2) + (row*64);
	int tile=videoram[offs]+((videoram[offs+1]&0xc0)<<2);
	int color=videoram[offs+1]&0xf;

	SET_TILE_INFO(0,tile,color)
}

int raiden_vh_start(void)
{
	background_layer = tilemap_create(
		get_back_tile_info,
		0,
		16,16,
		32,32
	);

	foreground_layer = tilemap_create(
		get_fore_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		32,32
	);

	/* Weird - Raiden (Alternate) has different char format! */
	if (!strcmp(Machine->gamedrv->name,"raiden"))
		ALTERNATE=0;
	else
		ALTERNATE=1;

	/* Weird - Raiden (Alternate) has different char format! */
	if (!ALTERNATE)
		text_layer = tilemap_create(
			get_text_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			32,32
		);
	else
		text_layer = tilemap_create(
			get_text_alt_tile_info,
			TILEMAP_TRANSPARENT,
			8,8,
			32,32
		);

	tilemap_set_scroll_rows(background_layer,1);
	tilemap_set_scroll_cols(background_layer,1);
	tilemap_set_scroll_rows(foreground_layer,1);
	tilemap_set_scroll_cols(foreground_layer,1);
	tilemap_set_scroll_rows(text_layer,0);
	tilemap_set_scroll_cols(text_layer,0);

	foreground_layer->transparent_pen = 15;
	text_layer->transparent_pen = 15;

	return 0;
}

void raiden_control_w(int offset, int data)
{
	/* All other bits unknown - could be playfield enables */

	/* Flipscreen */
	if (offset==6) {
		flipscreen=data&0x2;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	}
}

static void draw_sprites(struct osd_bitmap *bitmap,int pri_mask)
{
	int offs,fx,fy,x,y,color,sprite;

	for (offs = 0x1000-8;offs >= 0;offs -= 8)
	{
		/* Don't draw empty sprite table entries */
		if (buffered_spriteram[offs+7]!=0xf) continue;
		if (buffered_spriteram[offs+0]==0xf0f) continue;
		if (!(pri_mask&buffered_spriteram[offs+5])) continue;

		fx= buffered_spriteram[offs+1]&0x20;
		fy= buffered_spriteram[offs+1]&0x40;
		y = buffered_spriteram[offs+0];
		x = buffered_spriteram[offs+4];

		if (buffered_spriteram[offs+5]&1) x=0-(0x100-x);

		color = buffered_spriteram[offs+1]&0xf;
		sprite = buffered_spriteram[offs+2]+(buffered_spriteram[offs+3]<<8);
		sprite &= 0x0fff;

		if (flipscreen) {
			x=240-x;
			y=240-y;
			if (fx) fx=0; else fx=1;
			if (fy) fy=0; else fy=1;
		}

		drawgfx(bitmap,Machine->gfx[3],
				sprite,
				color,fx,fy,x,y,
				&Machine->drv->visible_area,TRANSPARENCY_PEN,15);
	}
}

void raiden_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int color,offs,sprite;
	int colmask[16],i,pal_base;

	/* Setup the tilemaps, alternate version has different scroll positions */
	if (!ALTERNATE) {
		tilemap_set_scrollx( background_layer,0, ((raiden_scroll_ram[1]<<8)+raiden_scroll_ram[0]) );
		tilemap_set_scrolly( background_layer,0, ((raiden_scroll_ram[3]<<8)+raiden_scroll_ram[2]) );
		tilemap_set_scrollx( foreground_layer,0, ((raiden_scroll_ram[5]<<8)+raiden_scroll_ram[4]) );
		tilemap_set_scrolly( foreground_layer,0, ((raiden_scroll_ram[7]<<8)+raiden_scroll_ram[6]) );
	}
	else {
		tilemap_set_scrolly( background_layer,0, ((raiden_scroll_ram[0x02]&0x30)<<4)+((raiden_scroll_ram[0x04]&0x7f)<<1)+((raiden_scroll_ram[0x04]&0x80)>>7) );
		tilemap_set_scrollx( background_layer,0, ((raiden_scroll_ram[0x12]&0x30)<<4)+((raiden_scroll_ram[0x14]&0x7f)<<1)+((raiden_scroll_ram[0x14]&0x80)>>7) );
		tilemap_set_scrolly( foreground_layer,0, ((raiden_scroll_ram[0x22]&0x30)<<4)+((raiden_scroll_ram[0x24]&0x7f)<<1)+((raiden_scroll_ram[0x24]&0x80)>>7) );
		tilemap_set_scrollx( foreground_layer,0, ((raiden_scroll_ram[0x32]&0x30)<<4)+((raiden_scroll_ram[0x34]&0x7f)<<1)+((raiden_scroll_ram[0x34]&0x80)>>7) );
	}

	tilemap_update(ALL_TILEMAPS);

	/* Build the dynamic palette */
	palette_init_used_colors();

	/* Sprites */
	pal_base = Machine->drv->gfxdecodeinfo[3].color_codes_start;
	for (color = 0;color < 16;color++) colmask[color] = 0;
	for (offs = 0;offs <0x1000;offs += 8)
	{
		color = buffered_spriteram[offs+1]&0xf;
		sprite = buffered_spriteram[offs+2]+(buffered_spriteram[offs+3]<<8);
		sprite &= 0x0fff;
		colmask[color] |= Machine->gfx[3]->pen_usage[sprite];
	}
	for (color = 0;color < 16;color++)
	{
		for (i = 0;i < 15;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);
	tilemap_draw(bitmap,background_layer,0);

	/* Draw sprites underneath foreground */
	draw_sprites(bitmap,0x40);
	tilemap_draw(bitmap,foreground_layer,0);

	/* Rest of sprites */
	draw_sprites(bitmap,0x80);

	/* Text layer */
	tilemap_draw(bitmap,text_layer,0);
}


