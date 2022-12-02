/***************************************************************************

  gryzor: vidhrdw.c

***************************************************************************/

#include "driver.h"
#include "vidhrdw/konamiic.h"
#include "vidhrdw/generic.h"

//static int spriteram_offset;
static unsigned char *private_spriteram_2,*private_spriteram;

unsigned char *contra_fg_vram,*contra_fg_cram;
unsigned char *contra_text_vram,*contra_text_cram;
unsigned char *contra_bg_vram,*contra_bg_cram;

static struct tilemap *bg_tilemap, *fg_tilemap, *text_tilemap;

/***************************************************************************
**
**	Contra has palette RAM, but it also has four lookup table PROMs
**
**	0	sprites #0
**	1	tiles   #0
**	2	sprites #1
**	3	tiles   #1
**
***************************************************************************/

void contra_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i,chip,pal,clut;

	for (chip = 0;chip < 2;chip++)
	{
		for (pal = 0;pal < 8;pal++)
		{
			clut = (pal & 1) + 2 * chip;
			for (i = 0;i < 256;i++)
			{
				if ((pal & 1) == 0)	/* sprites */
				{
					if (color_prom[256 * clut + i] == 0)
						*(colortable++) = 0;
					else
						*(colortable++) = 16 * pal + color_prom[256 * clut + i];
				}
				else
					*(colortable++) = 16 * pal + color_prom[256 * clut + i];
			}
		}
	}
}

/***************************************************************************
**
**	Tilemap Manager Callbacks
**
***************************************************************************/

static void get_fg_tile_info( int col, int row )
{
	int offs = row*32 + col;
	int attr = contra_fg_cram[offs];
	int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
	int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
	int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
	int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
	int bank = ((attr & 0x80) >> 7) |
			((attr >> (bit0+2)) & 0x02) |
			((attr >> (bit1+1)) & 0x04) |
			((attr >> (bit2  )) & 0x08) |
			((attr >> (bit3-1)) & 0x10) |
			((K007121_ctrlram[0][0x03] & 0x01) << 5);
	int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;

	bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);

	SET_TILE_INFO(0, contra_fg_vram[offs]+bank*256, ((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7) )
}

static void get_bg_tile_info( int col, int row )
{
	int offs = row*32 + col;
	int attr = contra_bg_cram[offs];
	int bit0 = (K007121_ctrlram[1][0x05] >> 0) & 0x03;
	int bit1 = (K007121_ctrlram[1][0x05] >> 2) & 0x03;
	int bit2 = (K007121_ctrlram[1][0x05] >> 4) & 0x03;
	int bit3 = (K007121_ctrlram[1][0x05] >> 6) & 0x03;
	int bank = ((attr & 0x80) >> 7) |
			((attr >> (bit0+2)) & 0x02) |
			((attr >> (bit1+1)) & 0x04) |
			((attr >> (bit2  )) & 0x08) |
			((attr >> (bit3-1)) & 0x10) |
			((K007121_ctrlram[1][0x03] & 0x01) << 5);
	int mask = (K007121_ctrlram[1][0x04] & 0xf0) >> 4;

	bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);

	SET_TILE_INFO(1, contra_bg_vram[offs]+bank*256, ((K007121_ctrlram[1][6]&0x30)*2+16)+(attr&7) )
}

static void get_text_tile_info( int col, int row )
{
	int offs = row*32 + col;
	int attr = contra_text_cram[offs];
	int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
	int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
	int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
	int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
	int bank = ((attr & 0x80) >> 7) |
			((attr >> (bit0+2)) & 0x02) |
			((attr >> (bit1+1)) & 0x04) |
			((attr >> (bit2  )) & 0x08) |
			((attr >> (bit3-1)) & 0x10);
	SET_TILE_INFO(0,contra_text_vram[offs]+bank*256, ((K007121_ctrlram[0][6]&0x30)*2+16)+(attr&7) )
}

/***************************************************************************
**
**	Memory Write Handlers
**
***************************************************************************/

void contra_fg_vram_w(int offset,int data){
	if (contra_fg_vram[offset] != data){
		tilemap_mark_tile_dirty( fg_tilemap, offset%32, offset/32 );
		contra_fg_vram[offset] = data;
	}
}

void contra_fg_cram_w(int offset,int data){
	if (contra_fg_cram[offset] != data){
		tilemap_mark_tile_dirty( fg_tilemap, offset%32, offset/32 );
		contra_fg_cram[offset] = data;
	}
}

void contra_bg_vram_w(int offset,int data){
	if (contra_bg_vram[offset] != data){
		tilemap_mark_tile_dirty( bg_tilemap, offset%32, offset/32 );
		contra_bg_vram[offset] = data;
	}
}

void contra_bg_cram_w(int offset,int data){
	if (contra_bg_cram[offset] != data){
		tilemap_mark_tile_dirty( bg_tilemap, offset%32, offset/32 );
		contra_bg_cram[offset] = data;
	}
}

void contra_text_vram_w(int offset,int data){
	if (contra_text_vram[offset] != data){
		tilemap_mark_tile_dirty( text_tilemap, offset%32, offset/32 );
		contra_text_vram[offset] = data;
	}
}

void contra_text_cram_w(int offset,int data){
	if (contra_text_cram[offset] != data){
		tilemap_mark_tile_dirty( text_tilemap, offset%32, offset/32 );
		contra_text_cram[offset] = data;
	}
}

void contra_K007121_ctrl_0_w( int offset, int data )
{
	if (offset == 3)
	{
		if ((data&0x8)==0)
			memcpy(private_spriteram,spriteram+0x800,0x800);
		else
			memcpy(private_spriteram,spriteram,0x800);
	}
	if (offset == 6)
	{
		if (K007121_ctrlram[0][6] != data)
			tilemap_mark_all_tiles_dirty( fg_tilemap );
	}
	if (offset == 7)
		tilemap_set_flip(fg_tilemap,(data & 0x08) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	K007121_ctrl_0_w(offset,data);
}

void contra_K007121_ctrl_1_w( int offset, int data )
{
	if (offset == 3)
	{
		if ((data&0x8)==0)
			memcpy(private_spriteram_2,spriteram+0x2800,0x800);
		else
			memcpy(private_spriteram_2,spriteram+0x2000,0x800);
	}
	if (offset == 6)
	{
		if (K007121_ctrlram[1][6] != data )
			tilemap_mark_all_tiles_dirty( bg_tilemap );
	}
	if (offset == 7)
		tilemap_set_flip(bg_tilemap,(data & 0x08) ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);

	K007121_ctrl_1_w(offset,data);
}

/***************************************************************************
**
**	Video Driver Initialization
**
***************************************************************************/

int contra_vh_start(void){
	bg_tilemap = tilemap_create(
		get_bg_tile_info,
		TILEMAP_OPAQUE,
		8,8,	/* tile width, tile height */
		32,32	/* number of columns, number of rows */
	);

	fg_tilemap = tilemap_create(
		get_fg_tile_info,
		TILEMAP_TRANSPARENT,
		8,8,	/* tile width, tile height */
		32,32	/* number of columns, number of rows */
	);

	text_tilemap = tilemap_create(
		get_text_tile_info,
		TILEMAP_OPAQUE,
		8,8,	/* tile width, tile height */
		32,32	/* number of columns, number of rows */
	);

	private_spriteram=malloc (0x800);
	private_spriteram_2=malloc (0x800);

	if( bg_tilemap && fg_tilemap && text_tilemap ){
		struct rectangle clip = Machine->drv->visible_area;
		clip.min_x += 40;
		tilemap_set_clip( bg_tilemap, &clip );
		tilemap_set_clip( fg_tilemap, &clip );

		clip.max_x = 39;
		clip.min_x = 0;
		tilemap_set_clip( text_tilemap, &clip );

		fg_tilemap->transparent_pen = 0;

		return 0;
	}

	return 1;
}

void contra_vh_stop(void)
{
	free(private_spriteram);
	free(private_spriteram_2);
}

static void draw_sprites( struct osd_bitmap *bitmap, int bank )
{
	const unsigned char *source;
	int base_color = (K007121_ctrlram[bank][6]&0x30)*2;

	if (bank==0) source=private_spriteram;
	else source=private_spriteram_2;

	K007121_sprites_draw(bank,bitmap,source,base_color,40,0);
}

void contra_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	tilemap_set_scrollx( fg_tilemap,0, K007121_ctrlram[0][0x00] - 40 );
	tilemap_set_scrolly( fg_tilemap,0, K007121_ctrlram[0][0x02] );
	tilemap_set_scrollx( bg_tilemap,0, K007121_ctrlram[1][0x00] - 40 );
	tilemap_set_scrolly( bg_tilemap,0, K007121_ctrlram[1][0x02] );

	tilemap_update( ALL_TILEMAPS );
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	tilemap_render( ALL_TILEMAPS );

	tilemap_draw( bitmap, bg_tilemap, 0 );
	tilemap_draw( bitmap, fg_tilemap, 0 );
	draw_sprites( bitmap, 0 );
	draw_sprites( bitmap, 1 );
	tilemap_draw( bitmap, text_tilemap, 0 );
}
