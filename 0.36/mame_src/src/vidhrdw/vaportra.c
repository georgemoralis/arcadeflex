/***************************************************************************

   Vapour Trail Video emulation - Bryan McPhail, mish@tendril.co.uk

****************************************************************************

	2 Data East 55 chips for playfields (same as Dark Seal, etc)
	1 Data East MXC-06 chip for sprites (same as Bad Dudes, etc)

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *vaportra_pf1_data,*vaportra_pf2_data,*vaportra_pf3_data,*vaportra_pf4_data;

static unsigned char vaportra_control_0[16];
static unsigned char vaportra_control_1[16];
static unsigned char vaportra_control_2[4];

static struct tilemap *vaportra_pf1_tilemap,*vaportra_pf2_tilemap,*vaportra_pf3_tilemap,*vaportra_pf4_tilemap;
static unsigned char *gfx_base;
static int gfx_bank,flipscreen;

static unsigned char *vaportra_spriteram;

/******************************************************************************/

void vaportra_update_sprites(int offset, int data)
{
	memcpy(vaportra_spriteram,spriteram,0x800);
}

static void update_24bitcol(int offset)
{
	int r,g,b;

	r = (READ_WORD(&paletteram[offset]) >> 0) & 0xff;
	g = (READ_WORD(&paletteram[offset]) >> 8) & 0xff;
	b = (READ_WORD(&paletteram_2[offset]) >> 0) & 0xff;

	palette_change_color(offset / 2,r,g,b);
}

void vaportra_palette_24bit_rg(int offset,int data)
{
	COMBINE_WORD_MEM(&paletteram[offset],data);
	update_24bitcol(offset);
}

void vaportra_palette_24bit_b(int offset,int data)
{
	COMBINE_WORD_MEM(&paletteram_2[offset],data);
	update_24bitcol(offset);
}

/******************************************************************************/

static void vaportra_update_palette(void)
{
	int offs,color,i,pal_base;
	int colmask[16];

	palette_init_used_colors();

	/* Sprites */
	pal_base = Machine->drv->gfxdecodeinfo[4].color_codes_start;
	for (color = 0;color < 16;color++) colmask[color] = 0;
	for (offs = 0;offs < 0x800;offs += 8)
	{
		int x,y,sprite,multi;

		y = READ_WORD(&vaportra_spriteram[offs]);
		if ((y&0x8000) == 0) continue;

		sprite = READ_WORD (&vaportra_spriteram[offs+2]) & 0x1fff;

		x = READ_WORD(&vaportra_spriteram[offs+4]);
		color = (x >> 12) &0xf;

		x = x & 0x01ff;
		if (x >= 256) x -= 512;
		x = 240 - x;
		if (x>256) continue; /* Speedup */

		multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */
		sprite &= ~multi;

		while (multi >= 0)
		{
			colmask[color] |= Machine->gfx[4]->pen_usage[sprite + multi];
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

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
}

static void vaportra_drawsprites(struct osd_bitmap *bitmap, int pri)
{
	int offs,priority_value;

	priority_value=READ_WORD(&vaportra_control_2[2]);

	for (offs = 0;offs < 0x800;offs += 8)
	{
		int x,y,sprite,colour,multi,fx,fy,inc,flash,mult;

		y = READ_WORD(&vaportra_spriteram[offs]);
		if ((y&0x8000) == 0) continue;

		sprite = READ_WORD (&vaportra_spriteram[offs+2]) & 0x1fff;
		x = READ_WORD(&vaportra_spriteram[offs+4]);
		colour = (x >> 12) &0xf;
		if (pri && (colour>=priority_value)) continue;
		if (!pri && !(colour>=priority_value)) continue;

		flash=x&0x800;
		if (flash && (cpu_getcurrentframe() & 1)) continue;

		fx = y & 0x2000;
		fy = y & 0x4000;
		multi = (1 << ((y & 0x1800) >> 11)) - 1;	/* 1x, 2x, 4x, 8x height */

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
			drawgfx(bitmap,Machine->gfx[4],
					sprite - multi * inc,
					colour,
					fx,fy,
					x,y + mult * multi,
					&Machine->drv->visible_area,TRANSPARENCY_PEN,0);

			multi--;
		}
	}
}

/* Function for all 16x16 1024x1024 layers */
static void get_back_tile_info( int col, int row )
{
	int offs,tile,color;

	if (col>31 && row>31) offs=0x1800 + (col-32)*2 + (row-32) *64; /* Bottom right */
	else if (row>31) offs=0x1000 + col*2 + (row-32) *64; /* Bottom left */
	else if (col>31) offs=0x800 + (col-32)*2 + row *64; /* Top right */
	else offs=col*2 + row *64; /* Top left */

	tile=READ_WORD(&gfx_base[offs]);
	color=tile >> 12;
	tile=tile&0xfff;

	SET_TILE_INFO(gfx_bank,tile,color)
}

/* 8x8 top layer */
static void get_fore_tile_info( int col, int row )
{
	int offs=(col*2) + (row*128);
	int tile=READ_WORD(&vaportra_pf1_data[offs]);
	int color=tile >> 12;

	tile=tile&0xfff;

	SET_TILE_INFO(0,tile,color)
}

/******************************************************************************/

void vaportra_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	int pri=READ_WORD(&vaportra_control_2[0]);
	static int last_pri=0;

	/* Update flipscreen */
	if (READ_WORD(&vaportra_control_1[0])&0x80)
		flipscreen=0;
	else
		flipscreen=1;

	tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	/* Update scroll registers */
	tilemap_set_scrollx( vaportra_pf1_tilemap,0, READ_WORD(&vaportra_control_1[2]) );
	tilemap_set_scrolly( vaportra_pf1_tilemap,0, READ_WORD(&vaportra_control_1[4]) );
	tilemap_set_scrollx( vaportra_pf2_tilemap,0, READ_WORD(&vaportra_control_0[2]) );
	tilemap_set_scrolly( vaportra_pf2_tilemap,0, READ_WORD(&vaportra_control_0[4]) );
	tilemap_set_scrollx( vaportra_pf3_tilemap,0, READ_WORD(&vaportra_control_1[6]) );
	tilemap_set_scrolly( vaportra_pf3_tilemap,0, READ_WORD(&vaportra_control_1[8]) );
	tilemap_set_scrollx( vaportra_pf4_tilemap,0, READ_WORD(&vaportra_control_0[6]) );
	tilemap_set_scrolly( vaportra_pf4_tilemap,0, READ_WORD(&vaportra_control_0[8]) );

	pri&=0x3;
	if (pri!=last_pri)
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	last_pri=pri;

	/* Update playfields */
	switch (pri) {
		case 0:
		case 2:
			vaportra_pf4_tilemap->type=TILEMAP_OPAQUE;
			vaportra_pf3_tilemap->type=TILEMAP_TRANSPARENT;
			vaportra_pf2_tilemap->type=TILEMAP_TRANSPARENT;
			break;
		case 1:
		case 3:
			vaportra_pf2_tilemap->type=TILEMAP_OPAQUE;
			vaportra_pf3_tilemap->type=TILEMAP_TRANSPARENT;
			vaportra_pf4_tilemap->type=TILEMAP_TRANSPARENT;
			break;
	}

	gfx_bank=1;
	gfx_base=vaportra_pf2_data;
	tilemap_update(vaportra_pf2_tilemap);

	gfx_bank=2;
	gfx_base=vaportra_pf3_data;
	tilemap_update(vaportra_pf3_tilemap);

	gfx_bank=3;
	gfx_base=vaportra_pf4_data;
	tilemap_update(vaportra_pf4_tilemap);

	tilemap_update(vaportra_pf1_tilemap);
	vaportra_update_palette();

	/* Draw playfields */
	tilemap_render(ALL_TILEMAPS);

	if (pri==0) {
		tilemap_draw(bitmap,vaportra_pf4_tilemap,0);
		tilemap_draw(bitmap,vaportra_pf2_tilemap,0);
		vaportra_drawsprites(bitmap,0);
		tilemap_draw(bitmap,vaportra_pf3_tilemap,0);
	}
	else if (pri==1) {
		tilemap_draw(bitmap,vaportra_pf2_tilemap,0);
		tilemap_draw(bitmap,vaportra_pf4_tilemap,0);
		vaportra_drawsprites(bitmap,0);
		tilemap_draw(bitmap,vaportra_pf3_tilemap,0);
	}
	else if (pri==2) {
		tilemap_draw(bitmap,vaportra_pf4_tilemap,0);
		tilemap_draw(bitmap,vaportra_pf3_tilemap,0);
		vaportra_drawsprites(bitmap,0);
		tilemap_draw(bitmap,vaportra_pf2_tilemap,0);
	}
	else {
		tilemap_draw(bitmap,vaportra_pf2_tilemap,0);
		tilemap_draw(bitmap,vaportra_pf3_tilemap,0);
		vaportra_drawsprites(bitmap,0);
		tilemap_draw(bitmap,vaportra_pf4_tilemap,0);
	}

	vaportra_drawsprites(bitmap,1);
	tilemap_draw(bitmap,vaportra_pf1_tilemap,0);
}

/******************************************************************************/

int vaportra_pf1_data_r(int offset)
{
	return READ_WORD(&vaportra_pf1_data[offset]);
}

int vaportra_pf2_data_r(int offset)
{
	return READ_WORD(&vaportra_pf2_data[offset]);
}

int vaportra_pf3_data_r(int offset)
{
	return READ_WORD(&vaportra_pf3_data[offset]);
}

int vaportra_pf4_data_r(int offset)
{
	return READ_WORD(&vaportra_pf4_data[offset]);
}

void vaportra_pf1_data_w(int offset,int data)
{
	COMBINE_WORD_MEM(&vaportra_pf1_data[offset],data);
	tilemap_mark_tile_dirty(vaportra_pf1_tilemap,(offset%128)/2,offset/128);
}

void vaportra_pf2_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&vaportra_pf2_data[offset],data);
	if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
	dx+=(offset%64)/2; dy+=(offset/64);
	tilemap_mark_tile_dirty(vaportra_pf2_tilemap,dx,dy);
}

void vaportra_pf3_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&vaportra_pf3_data[offset],data);
	if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
	dx+=(offset%64)/2; dy+=(offset/64);
	tilemap_mark_tile_dirty(vaportra_pf3_tilemap,dx,dy);
}

void vaportra_pf4_data_w(int offset,int data)
{
	int dx=0,dy=0;

	COMBINE_WORD_MEM(&vaportra_pf4_data[offset],data);
	if (offset>0x7ff) {offset-=0x800;dx=32; dy=0;}
	dx+=(offset%64)/2; dy+=(offset/64);
	tilemap_mark_tile_dirty(vaportra_pf4_tilemap,dx,dy);
}

void vaportra_control_0_w(int offset,int data)
{
	COMBINE_WORD_MEM(&vaportra_control_0[offset],data);
}

void vaportra_control_1_w(int offset,int data)
{
	COMBINE_WORD_MEM(&vaportra_control_1[offset],data);
}

void vaportra_control_2_w(int offset,int data)
{
	COMBINE_WORD_MEM(&vaportra_control_2[offset],data);
}

/******************************************************************************/

void vaportra_vh_stop (void)
{
	free(vaportra_spriteram);
}

int vaportra_vh_start(void)
{
	vaportra_pf2_tilemap = tilemap_create(
		get_back_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		64,32 /* 1024 by 512 */
	);

	vaportra_pf3_tilemap = tilemap_create(
		get_back_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		64,32
	);

	vaportra_pf4_tilemap = tilemap_create(
		get_back_tile_info,
		TILEMAP_TRANSPARENT,
		16,16,
		64,32
	);

	vaportra_pf1_tilemap = tilemap_create(
		get_fore_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,
		64,64
	);

	vaportra_pf1_tilemap->transparent_pen = 0;
	vaportra_pf2_tilemap->transparent_pen = 0;
	vaportra_pf3_tilemap->transparent_pen = 0;
	vaportra_pf4_tilemap->transparent_pen = 0;

	vaportra_spriteram = malloc(0x800);

	return 0;
}

/******************************************************************************/
