/***************************************************************************

  Mad Motor video emulation - Bryan McPhail, mish@tendril.co.uk

  Notes:  Playfield 3 can change size between 512x1024 and 2048x256

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *madmotor_pf1_rowscroll;
unsigned char *madmotor_pf1_data,*madmotor_pf2_data,*madmotor_pf3_data;

static unsigned char madmotor_pf1_control[32];
static unsigned char madmotor_pf2_control[32];
static unsigned char madmotor_pf3_control[32];

static int flipscreen;
static struct tilemap *madmotor_pf1_tilemap,*madmotor_pf2_tilemap,*madmotor_pf3_tilemap,*madmotor_pf3a_tilemap;

/******************************************************************************/

static void madmotor_mark_sprite_colours(void)
{
	int offs,color,i,pal_base;
	int colmask[16];

	palette_init_used_colors();

	/* Sprites */
	pal_base = Machine->drv->gfxdecodeinfo[3].color_codes_start;
	for (color = 0;color < 16;color++) colmask[color] = 0;
	for (offs = 0;offs < 0x800;offs += 8)
	{
		int x,y,sprite,multi;

		y = READ_WORD(&spriteram[offs]);
		if ((y&0x8000) == 0) continue;

		x = READ_WORD(&spriteram[offs+4]);
		color = (x & 0xf000) >> 12;

		multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
											/* multi = 0   1   3   7 */

		x = x & 0x01ff;
		if (x >= 256) x -= 512;
		x = 240 - x;
		if (x>256) continue; /* Speedup + save colours */

		sprite = READ_WORD (&spriteram[offs+2]) & 0x1fff;
		sprite &= ~multi;

		while (multi >= 0)
		{
			colmask[color] |= Machine->gfx[3]->pen_usage[sprite + multi];

			multi--;
		}
	}

	for (color = 0;color < 16;color++)
	{
		for (i = 1;i < 16;i++)
		{
			if (colmask[color] & (1 << i))
				palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
		}
	}
}

/* 512 by 512 playfield, 8 by 8 tiles */
static void get_pf1_tile_info( int col, int row )
{
	int offs,tile,color;

	if (col>31 && row>31) offs=0x1800 + (col-32)*2 + (row-32) *64; /* Bottom right */
	else if (col>31) offs=0x1000 + (col-32)*2 + row *64; /* Top right */
	else if (row>31) offs=0x800 + col*2 + (row-32) *64; /* Bottom left */
	else offs=col*2 + row *64; /* Top left */

	tile=READ_WORD(&madmotor_pf1_data[offs]);
	color=tile >> 12;
	tile=tile&0xfff;

	SET_TILE_INFO(0,tile,color)
}

/* 512 by 512 playfield, 16 by 16 tiles */
static void get_pf2_tile_info( int col, int row )
{
	int offs,tile,color;

	if (col>15 && row>15) offs=0x600 + (col-16)*2 + (row-16) * 32; /* Bottom right */
	else if (col>15) offs=0x400 + (col-16)*2 + row *32; /* Top right */
	else if (row>15) offs=0x200 + col*2 + (row-16) *32; /* Bottom left */
	else offs=col*2 + row *32; /* Top left */

	tile=READ_WORD(&madmotor_pf2_data[offs]);
	color=tile >> 12;
	tile=tile&0xfff;

	SET_TILE_INFO(1,tile,color)
}

/* 512 by 1024 playfield, 16 by 16 tiles */
static void get_pf3_tile_info( int col, int row )
{
	int offs,tile,color;

	if (col>15 && row>47) offs=0xe00 + (col-16)*2 + (row-48) *32;
	else if (col>15 && row>31) offs=0xc00 + (col-16)*2 + (row-32) *32;
	else if (col>15 && row>15) offs=0xa00 + (col-16)*2 + (row-16) *32;
	else if (col>15) offs=0x800 + (col-16)*2 + row *32;
	else if (row>47) offs=0x600 + col*2 + (row-48) *32;
	else if (row>31) offs=0x400 + col*2 + (row-32) *32;
	else if (row>15) offs=0x200 + col*2 + (row-16) *32;
	else offs=col*2 + row *32; /* Top left */

	tile=READ_WORD(&madmotor_pf3_data[offs]);
	color=tile >> 12;
	tile=tile&0xfff;

	SET_TILE_INFO(2,tile,color)
}

/* 2048 by 256 playfield, 16 by 16 tiles */
static void get_pf3a_tile_info( int col, int row )
{
	int offs,tile,color;

	if (col>111) offs=0xe00 + (col-112)*2 + row *32;
	else if (col>95) offs=0xc00 + (col-96)*2 + row *32;
	else if (col>79) offs=0xa00 + (col-80)*2 + row *32;
	else if (col>63) offs=0x800 + (col-64)*2 + row *32;
	else if (col>47) offs=0x600 + (col-48)*2 + row *32;
	else if (col>31) offs=0x400 + (col-32)*2 + row *32;
	else if (col>15) offs=0x200 + (col-16)*2 + row *32;
	else offs=col*2 + row *32; /* Top left */

	tile=READ_WORD(&madmotor_pf3_data[offs]);
	color=tile >> 12;
	tile=tile&0xfff;

	SET_TILE_INFO(2,tile,color)
}

static void dec0_drawsprites(struct osd_bitmap *bitmap,int pri_mask,int pri_val)
{
	int offs;

	for (offs = 0;offs < 0x800;offs += 8)
	{
		int x,y,sprite,colour,multi,fx,fy,inc,flash,mult;

		y = READ_WORD(&spriteram[offs]);
		if ((y&0x8000) == 0) continue;

		x = READ_WORD(&spriteram[offs+4]);
		colour = x >> 12;
		if ((colour & pri_mask) != pri_val) continue;

		flash=x&0x800;
		if (flash && (cpu_getcurrentframe() & 1)) continue;

		fx = y & 0x2000;
		fy = y & 0x4000;
		multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
											/* multi = 0   1   3   7 */

		sprite = READ_WORD (&spriteram[offs+2]) & 0x1fff;

		x = x & 0x01ff;
		y = y & 0x01ff;
		if (x >= 256) x -= 512;
		if (y >= 256) y -= 512;
		x = 240 - x;
		y = 240 - y;

		if (x>256) continue; /* Speedup */

		sprite &= ~multi;
		if (fy)
			inc = -1;
		else
		{
			sprite += multi;
			inc = 1;
		}

		if (flipscreen) {
			y=240-y;
			x=240-x;
			if (fx) fx=0; else fx=1;
			if (fy) fy=0; else fy=1;
			mult=16;
		}
		else mult=-16;

		while (multi >= 0)
		{
			drawgfx(bitmap,Machine->gfx[3],
					sprite - multi * inc,
					colour,
					fx,fy,
					x,y + mult * multi,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);

			multi--;
		}
	}
}

/******************************************************************************/

void madmotor_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int offs;

	/* Update flipscreen */
	if (READ_WORD(&madmotor_pf1_control[0])&0x80)
		flipscreen=1;
	else
		flipscreen=0;
	tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	/* Setup scroll registers */
	for (offs = 0;offs < 512;offs++)
		tilemap_set_scrollx( madmotor_pf1_tilemap,offs, READ_WORD(&madmotor_pf1_control[0x10]) + READ_WORD(&madmotor_pf1_rowscroll[0x400+2*offs]) );
	tilemap_set_scrolly( madmotor_pf1_tilemap,0, READ_WORD(&madmotor_pf1_control[0x12]) );
	tilemap_set_scrollx( madmotor_pf2_tilemap,0, READ_WORD(&madmotor_pf2_control[0x10]) );
	tilemap_set_scrolly( madmotor_pf2_tilemap,0, READ_WORD(&madmotor_pf2_control[0x12]) );
	tilemap_set_scrollx( madmotor_pf3_tilemap,0, READ_WORD(&madmotor_pf3_control[0x10]) );
	tilemap_set_scrolly( madmotor_pf3_tilemap,0, READ_WORD(&madmotor_pf3_control[0x12]) );
	tilemap_set_scrollx( madmotor_pf3a_tilemap,0, READ_WORD(&madmotor_pf3_control[0x10]) );
	tilemap_set_scrolly( madmotor_pf3a_tilemap,0, READ_WORD(&madmotor_pf3_control[0x12]) );

	tilemap_update(madmotor_pf1_tilemap);
	tilemap_update(madmotor_pf2_tilemap);
	tilemap_update(madmotor_pf3_tilemap);
	tilemap_update(madmotor_pf3a_tilemap);

	madmotor_mark_sprite_colours();
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	/* Draw playfields & sprites */
	tilemap_render(ALL_TILEMAPS);
	if (READ_WORD(&madmotor_pf3_control[0x6])==2)
		tilemap_draw(bitmap,madmotor_pf3_tilemap,0);
	else
		tilemap_draw(bitmap,madmotor_pf3a_tilemap,0);
	tilemap_draw(bitmap,madmotor_pf2_tilemap,0);
	dec0_drawsprites(bitmap,0x00,0x00);
	tilemap_draw(bitmap,madmotor_pf1_tilemap,0);
}

/******************************************************************************/

int madmotor_pf1_data_r(int offset)
{
	return READ_WORD(&madmotor_pf1_data[offset]);
}

int madmotor_pf2_data_r(int offset)
{
	return READ_WORD(&madmotor_pf2_data[offset]);
}

int madmotor_pf3_data_r(int offset)
{
	return READ_WORD(&madmotor_pf3_data[offset]);
}

void madmotor_pf1_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&madmotor_pf1_data[offset],data);
	if (offset>0x17ff) {offset-=0x1800;dx=32; dy=32;}
	else if (offset>0xfff) {offset-=0x1000;dx=32; dy=0;}
	else if (offset>0x7ff) {offset-=0x800;dx=0; dy=32;}
	dx+=(offset%64)/2; dy+=(offset/64);
	tilemap_mark_tile_dirty(madmotor_pf1_tilemap,dx,dy);
}

void madmotor_pf2_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&madmotor_pf2_data[offset],data);
	if (offset>0x5ff) {offset-=0x600;dx=16; dy=16;}
	else if (offset>0x3ff) {offset-=0x400;dx=16; dy=0;}
	else if (offset>0x1ff) {offset-=0x200;dx=0; dy=16;}
	dx+=(offset%32)/2; dy+=(offset/32);
	tilemap_mark_tile_dirty(madmotor_pf2_tilemap,dx,dy);
}

void madmotor_pf3_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&madmotor_pf3_data[offset],data);

	/* Mark the dirty position on the 512 x 1024 version */
	if (offset>0xdff) {offset-=0xe00;dx=16; dy=48;}
	else if (offset>0xbff) {offset-=0xc00;dx=16; dy=32;}
	else if (offset>0x9ff) {offset-=0xa00;dx=16; dy=16;}
	else if (offset>0x7ff) {offset-=0x800;dx=16; dy=0; }
	else if (offset>0x5ff) {offset-=0x600;dx=0;  dy=48;}
	else if (offset>0x3ff) {offset-=0x400;dx=0;  dy=32;}
	else if (offset>0x1ff) {offset-=0x200;dx=0;  dy=16;}
	dx+=(offset%32)/2; dy+=(offset/32);
	tilemap_mark_tile_dirty(madmotor_pf3_tilemap,dx,dy);

	/* Mark the dirty position on the 2048 x 256 version */
	dx=0; dy=0;
	if (offset>0xdff) {offset-=0xe00;dx=112; }
	else if (offset>0xbff) {offset-=0xc00;dx=96; }
	else if (offset>0x9ff) {offset-=0xa00;dx=80; }
	else if (offset>0x7ff) {offset-=0x800;dx=64; }
	else if (offset>0x5ff) {offset-=0x600;dx=48; }
	else if (offset>0x3ff) {offset-=0x400;dx=32; }
	else if (offset>0x1ff) {offset-=0x200;dx=16; }
	dx+=(offset%32)/2; dy+=(offset/32);
	tilemap_mark_tile_dirty(madmotor_pf3_tilemap,dx,dy);
}

void madmotor_pf1_control_w(int offset,int data)
{
	COMBINE_WORD_MEM(&madmotor_pf1_control[offset],data);
}

void madmotor_pf2_control_w(int offset,int data)
{
	COMBINE_WORD_MEM(&madmotor_pf2_control[offset],data);
}

void madmotor_pf3_control_w(int offset,int data)
{
	COMBINE_WORD_MEM(&madmotor_pf3_control[offset],data);
}

int madmotor_pf1_rowscroll_r(int offset)
{
	return READ_WORD(&madmotor_pf1_rowscroll[offset]);
}

void madmotor_pf1_rowscroll_w(int offset,int data)
{
	COMBINE_WORD_MEM(&madmotor_pf1_rowscroll[offset],data);
}

/******************************************************************************/

int madmotor_vh_start(void)
{
	madmotor_pf1_tilemap = tilemap_create(
		get_pf1_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		64,64
	);

	madmotor_pf2_tilemap = tilemap_create(
		get_pf2_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		32,32
	);

	madmotor_pf3_tilemap = tilemap_create(
		get_pf3_tile_info,
		0,
		16,16,
		32,64
	);

	madmotor_pf3a_tilemap = tilemap_create(
		get_pf3a_tile_info,
		0,
		16,16,
		128,16
	);

	madmotor_pf1_tilemap->transparent_pen = 0;
	madmotor_pf2_tilemap->transparent_pen = 0;
	tilemap_set_scroll_rows(madmotor_pf1_tilemap,512);
	tilemap_set_scroll_cols(madmotor_pf1_tilemap,1);
	tilemap_set_scroll_rows(madmotor_pf2_tilemap,1);
	tilemap_set_scroll_cols(madmotor_pf2_tilemap,1);
	tilemap_set_scroll_rows(madmotor_pf3_tilemap,1);
	tilemap_set_scroll_cols(madmotor_pf3_tilemap,1);
	tilemap_set_scroll_rows(madmotor_pf3a_tilemap,1);
	tilemap_set_scroll_cols(madmotor_pf3a_tilemap,1);

	return 0;
}

/******************************************************************************/
