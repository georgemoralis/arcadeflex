/*
**	Video Driver for Taito Samurai (1985)
*/

#include "driver.h"
#include "vidhrdw/generic.h"

/*
** prototypes
*/
void tsamurai_bgcolor_w( int offset, int data );
void tsamurai_flipscreen_w( int offset, int data );
void tsamurai_textbank_w( int offset, int data );
void tsamurai_scrolly_w( int offset, int data );
void tsamurai_scrollx_w( int offset, int data );

void tsamurai_bg_videoram_w( int offset, int data );
void tsamurai_fg_videoram_w( int offset, int data );

void tsamurai_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom);
void tsamurai_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh );
int tsamurai_vh_start( void );

/*
** variables
*/
unsigned char *tsamurai_videoram;
static int flipscreen;
static int bgcolor;
static int textbank;

static struct tilemap *background, *foreground;

/*
** video register write handlers
*/

void tsamurai_scrolly_w( int offset, int data ){
	tilemap_set_scrolly( background, 0, data );
}

void tsamurai_scrollx_w( int offset, int data ){
	tilemap_set_scrollx( background, 0, data );
}

void tsamurai_bgcolor_w( int offset, int data ){
	bgcolor = data;
}

void tsamurai_flipscreen_w( int offset, int data ){
	if( flipscreen!=data ){
		flipscreen = data;
		tilemap_set_flip( ALL_TILEMAPS, flipscreen?(TILEMAP_FLIPX | TILEMAP_FLIPY):0 );
	}
}

void tsamurai_textbank_w( int offset, int data ){
	if( textbank!=data ){
		textbank = data;
		tilemap_mark_all_tiles_dirty( foreground );
	}
}

/*
**	videoram memory-write handlers
*/

void tsamurai_bg_videoram_w( int offset, int data ){
	if( tsamurai_videoram[offset]!=data ){
		tsamurai_videoram[offset]=data;
		offset = offset/2;
		tilemap_mark_tile_dirty( background, offset%32, offset/32 );
	}
}
void tsamurai_fg_videoram_w( int offset, int data ){
	if( videoram[offset]!=data ){
		videoram[offset]=data;
		tilemap_mark_tile_dirty( foreground, offset%32, offset/32 );
	}
}
void tsamurai_fg_colorram_w( int offset, int data ){
	if( colorram[offset]!=data ){
		int i;
		colorram[offset]=data;
		if (offset & 1)
		{
			for (i = 0;i < 32;i++)
				tilemap_mark_tile_dirty( foreground, offset/2, i );
		}
	}
}

/*
** tilemap manager callbacks
*/

static void get_bg_tile_info( int col, int row ){
	int tile_index = (row*32+col)*2;
	unsigned char attributes = tsamurai_videoram[tile_index|1];
	int color = (attributes&0x1f);
	SET_TILE_INFO(0,tsamurai_videoram[tile_index]+4*(attributes&0xc0),color )
}

static void get_fg_tile_info( int col, int row ){
	int tile_number = videoram[row*32+col];
	if( textbank&1 ) tile_number += 256;
	SET_TILE_INFO(1,tile_number,colorram[(col*2)+1] & 0x1f )
}

/*
** color prom decoding
*/

void tsamurai_convert_color_prom(unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;

	for (i = 0;i < Machine->drv->total_colors;i++)
	{
		int bit0,bit1,bit2,bit3;


		/* red component */
		bit0 = (color_prom[0] >> 0) & 0x01;
		bit1 = (color_prom[0] >> 1) & 0x01;
		bit2 = (color_prom[0] >> 2) & 0x01;
		bit3 = (color_prom[0] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		/* green component */
		bit0 = (color_prom[Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
		/* blue component */
		bit0 = (color_prom[2*Machine->drv->total_colors] >> 0) & 0x01;
		bit1 = (color_prom[2*Machine->drv->total_colors] >> 1) & 0x01;
		bit2 = (color_prom[2*Machine->drv->total_colors] >> 2) & 0x01;
		bit3 = (color_prom[2*Machine->drv->total_colors] >> 3) & 0x01;
		*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;

		color_prom++;
	}
}

/*
** video driver initialization
*/

int tsamurai_vh_start( void ){
	background = tilemap_create( get_bg_tile_info, TILEMAP_TRANSPARENT, 8, 8, 32, 32 );
	foreground = tilemap_create( get_fg_tile_info, TILEMAP_TRANSPARENT, 8, 8, 32, 32 );
	if( background && foreground ){
		background->transparent_pen = 0;
		foreground->transparent_pen = 0;
		return 0;
	}
	return 1;
}

static void draw_sprites( struct osd_bitmap *bitmap ){
	struct GfxElement *gfx = Machine->gfx[2];
	const struct rectangle *clip = &Machine->drv->visible_area;
	const unsigned char *source = spriteram+32*4-4;
	const unsigned char *finish = spriteram; /* ? */
	static int flicker;
	flicker = 1-flicker;

	while( source>=finish ){
		int attributes = source[2]; /* bit 0x10 is usually, but not always set */

		int sx = source[3] - 16;
		int sy = 240-source[0];
		int sprite_number = source[1];
		int color = attributes&0x1f;
		//color = 0x2d - color; nunchakun fix?
		if( sy<-16 ) sy += 256;

		if( flipscreen ){
			drawgfx( bitmap,gfx,
				sprite_number&0x7f,
				color,
				1,(sprite_number&0x80)?0:1,
				256-32-sx,256-32-sy,
				clip,TRANSPARENCY_PEN,0 );
		}
		else {
			drawgfx( bitmap,gfx,
				sprite_number&0x7f,
				color,
				0,sprite_number&0x80,
				sx,sy,
				clip,TRANSPARENCY_PEN,0 );
		}

		source -= 4;
	}
}

void tsamurai_vh_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh ){
	tilemap_update( ALL_TILEMAPS );
	tilemap_render( ALL_TILEMAPS );

	/*
		This following isn't particularly efficient.  We'd be better off to
		dynamically change every 8th palette to the background color, so we
		could draw the background as an opaque tilemap.

		Note that the background color register isn't well understood
		(screenshots would be helpful)
	*/
	fillbitmap( bitmap, Machine->pens[bgcolor], 0 );
	tilemap_draw( bitmap, background, 0 );

	draw_sprites( bitmap );

	tilemap_draw( bitmap, foreground, 0 );
}
