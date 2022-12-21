/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

  Written by Manuel Abadia

Double Dribble Video Hardware explained:
----------------------------------------

Double Dribble has 4 layers:

2000-27ff	foreground layer 1
2800-2fff	foreground layer 2 (unused?)
6000-67ff	background layer 1
6800-6fff	background layer 2

Foreground layers use set 1 of char ROMS.
Background layers use set 2 of char ROMS.

Set 1 has 4096 tiles, so we have 16 banks of 256 tiles.
Set 2 has 8192 tiles, so we have 32 banks of 256 tiles.

Each layer is divided in two areas:

start..start+0x3ff			attributes
start+0x400..start+0x7ff	tile # to draw

attributes:
	bits 0..3: 	color?
	bit 4: 		flip x (0 = no / 1 = yes)
	bit 5:		flip y (0 = no / 1 = yes) / bit 2 of tile bank #
	bit 6:		bit 0 of tile bank #
	bit 7:		bit 1 of tile bank #

Double Dribble has 2 video registers to select bits 3 & 4 of tile bank #

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char* ddrible_fg_videoram;
unsigned char* ddrible_bg_videoram;
unsigned char *ddrible_spriteram_1;
unsigned char *ddrible_spriteram_2;

static struct tilemap *fg_tilemap,*bg_tilemap;



void ddrible_vh_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
	#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])


	/* build the lookup table for sprites. Palette is dynamic. */
	for (i = 0;i < TOTAL_COLORS(3);i++)
		COLOR(3,i) = (*(color_prom++) & 0x0f);
}



/*******************************************************************

	Double Dribble Video registers

0x0000:			y coordinate to start drawing the screen

0x0003:
	bit 0:		unused?
	bit 1:		bit 3 of tile bank #
	bit 2:		unused?
	bit 3:		???
	bits 4..7:	unused?

0x0801: background x scroll register

0x0803:
	bit 0:		bit 3 of tile bank #
	bit 1:		bit 4 of tile bank #
	bit 2:		unused?
	bit 3:		???
	bits 4..7:	unused?

*******************************************************************/

static int bankfg = 0;			/* bank # (foreground tiles) */
static int bankbg = 0;			/* bank # (background tiles) */

void ddrible_bank_select_w( int offset, int data )
{
	if ((data & 0x02) != bankfg)
	{
		bankfg = (data & 0x02);	/* char bank selection for set 1 */
		tilemap_mark_all_tiles_dirty( fg_tilemap );
	}
}

void ddrible_bank_select_w_2( int offset, int data )
{
	if (((data & 0x03) << 1) != bankbg)
	{
		bankbg = (data & 0x03) << 1;	/* char bank selection for set 2 */
		tilemap_mark_all_tiles_dirty( bg_tilemap );
	}
}


/***************************************************************************

	Callbacks for the TileMap code

***************************************************************************/

static void get_fg_tile_info( int col, int row )
{
	int tile_index;

	if (col >= 32)
	{
		col -= 32;
		tile_index = 0x800 + row*32 + col;
	}
	else
		tile_index = row*32 + col;

	{
		unsigned char attr = ddrible_fg_videoram[tile_index];
		int bank = ((attr & 0xc0) >> 6) + 4*(((attr & 0x20) >> 5) | bankfg);
		int num = ddrible_fg_videoram[tile_index + 0x400] + 256*bank;
		SET_TILE_INFO(0,num,0);
		tile_info.flags = TILE_FLIPYX((attr & 0x30) >> 4);
	}
}

static void get_bg_tile_info( int col, int row )
{
	int tile_index;

	if (col >= 32)
	{
		col -= 32;
		tile_index = 0x800 + row*32 + col;
	}
	else
		tile_index = row*32 + col;

	{
		unsigned char attr = ddrible_bg_videoram[tile_index];
		int bank = ((attr & 0xc0) >> 6) + 4*(((attr & 0x20) >> 5) | bankbg);
		int num = ddrible_bg_videoram[tile_index + 0x400] + 256*bank;
		SET_TILE_INFO(1,num,0);
		tile_info.flags = TILE_FLIPYX((attr & 0x30) >> 4);
	}
}

/***************************************************************************

	Start the video hardware emulation.

***************************************************************************/

int ddrible_vh_start ( void )
{
	fg_tilemap = tilemap_create( get_fg_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32 );
	bg_tilemap = tilemap_create( get_bg_tile_info, TILEMAP_OPAQUE, 8, 8, 64, 32 );

	if (fg_tilemap && bg_tilemap)
	{
		fg_tilemap->transparent_pen = 0;

		return 0;
	}

	return 1;
}

/***************************************************************************

	Memory handlers

***************************************************************************/

void ddrible_fg_videoram_w(int offset,int data)
{
	if (ddrible_fg_videoram[offset] != data)
	{
		ddrible_fg_videoram[offset] = data;
		if (offset & 0x800)
			tilemap_mark_tile_dirty(fg_tilemap,offset%32 + 32,(offset&0x3ff)/32);
		else
			tilemap_mark_tile_dirty(fg_tilemap,offset%32,(offset&0x3ff)/32);
	}
}

void ddrible_bg_videoram_w(int offset,int data)
{
	if (ddrible_bg_videoram[offset] != data)
	{
		ddrible_bg_videoram[offset] = data;
		if (offset & 0x800)
			tilemap_mark_tile_dirty(bg_tilemap,offset%32 + 32,(offset&0x3ff)/32);
		else
			tilemap_mark_tile_dirty(bg_tilemap,offset%32,(offset&0x3ff)/32);
	}
}


void ddrible_fg_scrollx_w( int offset,int data )
{
	static int fg_scrollx;

	if (offset)
		fg_scrollx = (fg_scrollx & 0x0ff) | ((data & 0x01) << 8);
	else
		fg_scrollx = (fg_scrollx & 0x100) | data;

	tilemap_set_scrollx(fg_tilemap,0,fg_scrollx);
}

void ddrible_fg_scrolly_w( int offset,int data )
{
	tilemap_set_scrolly(fg_tilemap,0,data);
}

void ddrible_bg_scrollx_w( int offset,int data )
{
	static int bg_scrollx;

	if (offset)
		bg_scrollx = (bg_scrollx & 0x0ff) | ((data & 0x01) << 8);
	else
		bg_scrollx = (bg_scrollx & 0x100) | data;

	tilemap_set_scrollx(bg_tilemap,0,bg_scrollx);
}

void ddrible_bg_scrolly_w( int offset,int data )
{
	tilemap_set_scrolly(bg_tilemap,0,data);
}



/***************************************************************************

	Double Dribble sprites

Each sprite has 5 bytes:
byte #0:	sprite number
byte #1:
	bits 0..2:	sprite bank #
	bits 3..7:	sprite color?
byte #2:	y position
byte #3:	x position
byte #4:	attributes
	bit 0:		enable
	bit 1:		???
	bit 2:		unused?
	bit 3:		unused?
	bit 4:		2x both ways
	bit 5:		flip x
	bit 6:		unused?
	bit 7:		unused?

***************************************************************************/

static void draw_sprites( struct osd_bitmap *bitmap, unsigned char* source, int lenght, int gfxset )
{
	struct GfxElement *gfx = Machine->gfx[gfxset];
	const unsigned char *finish = source + lenght;

	while( source < finish )
	{
		int sprite_number = source[0];		/* sprite number */
		int sprite_bank = source[1] & 0x07;	/* sprite bank */
		int sx = source[3];					/* vertical position */
		int sy = source[2];					/* horizontal position */
		int attr = source[4];				/* attributes */
		int xflip = attr & 0x20;			/* flip x */
		int color = (source[1] & 0xf0) >> 4;	/* color */
		if (sy < 16) attr |= 0x01;			/* clip sprites */


		if (!(attr & 0x01))	/* sprite enable */
		{
			sprite_number += sprite_bank*256;

			if (!(attr & 0x10))	/* normal sprite */
				drawgfx( bitmap, gfx, sprite_number, color, xflip, 0,
						sx,	sy,	0, TRANSPARENCY_PEN, 0 );
			else	/* 2x both ways */
			{
				if (!(attr & 0x20))
				{
					drawgfx( bitmap, gfx, sprite_number, color, xflip, 0,
							sx, sy,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number+1, color, xflip, 0,
							sx+16, sy,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number+2, color, xflip, 0,
							sx, sy+16,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number+3, color, xflip, 0,
							sx+16, sy+16,	0, TRANSPARENCY_PEN, 0 );
				}
				else	/* 2x both ways flipped */
				{
					drawgfx( bitmap, gfx, sprite_number+1, color, xflip, 0,
							sx, sy,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number, color, xflip, 0,
							sx+16, sy,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number+3, color, xflip, 0,
							sx, sy+16,	0, TRANSPARENCY_PEN, 0 );
					drawgfx( bitmap, gfx, sprite_number+2, color, xflip, 0,
							sx+16, sy+16,	0, TRANSPARENCY_PEN, 0 );
				}
			}
		}
		source += 5;
	}
}

/***************************************************************************

	Display Refresh

***************************************************************************/

void ddrible_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh )
{
	tilemap_update( ALL_TILEMAPS );
	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	tilemap_render( ALL_TILEMAPS );

	tilemap_draw(bitmap,bg_tilemap,0);
	draw_sprites(bitmap,ddrible_spriteram_1,  0x7d,2);	/* sprites set 1 */
	draw_sprites(bitmap,ddrible_spriteram_2, 0x140,3);	/* sprites set 2 */
	tilemap_draw(bitmap,fg_tilemap,0);
}
