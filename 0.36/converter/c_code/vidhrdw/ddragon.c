/***************************************************************************

  Video Hardware for Double Dragon (bootleg) & Double Dragon II

***************************************************************************/

#include "driver.h"
#include "vidhrdw/generic.h"
#include <stdio.h>

unsigned char *dd_videoram;
int dd_scrollx_hi, dd_scrolly_hi;
unsigned char *dd_scrollx_lo;
unsigned char *dd_scrolly_lo;
unsigned char *dd_spriteram;
int dd2_video;



int dd_vh_start( void )
{
	dirtybuffer = malloc( 0x400 );
	if( dirtybuffer )
	{
		memset(dirtybuffer,1, 0x400);

		tmpbitmap = osd_new_bitmap(
				Machine->drv->screen_width*2,
				Machine->drv->screen_height*2,
				Machine->scrbitmap->depth );

		if( tmpbitmap ) return 0;

		free( dirtybuffer );
	}

	return 1;
}



void dd_vh_stop( void )
{
	osd_free_bitmap( tmpbitmap );
	free( dirtybuffer );
}


void dd_background_w( int offset, int val )
{
	if( dd_videoram[offset] != val ){
		dd_videoram[offset] = val;
		dirtybuffer[offset/2] = 1;
	}
}


static void dd_draw_background( struct osd_bitmap *bitmap )
{
	const struct GfxElement *gfx = Machine->gfx[2];

	int scrollx = -dd_scrollx_hi - ( dd_scrollx_lo[0] );
	int scrolly = -dd_scrolly_hi - ( dd_scrolly_lo[0] );

	int offset;

	for( offset = 0; offset<0x400; offset++ ){
		int attributes = dd_videoram[offset*2];
		int color = ( attributes >> 3 ) & 0x7;
		if( dirtybuffer[offset] ){
			int tile_number = dd_videoram[offset*2+1] + ((attributes&7)<<8);
			int xflip = attributes & 0x40;
			int yflip = attributes & 0x80;
			int sx = 16*(((offset>>8)&1)*16 + (offset&0xff)%16);
			int sy = 16*(((offset>>9)&1)*16 + (offset&0xff)/16);

			/* CALB ????
                          if( sx<0 || sx>=512 || sy<0 || sy>=512 ) ExitToShell();*/

			drawgfx(tmpbitmap,gfx,
				tile_number,
				color,
				xflip,yflip,
				sx,sy,
				0,TRANSPARENCY_NONE,0);

			dirtybuffer[offset] = 0;
		}
	}

	copyscrollbitmap(bitmap,tmpbitmap,
			1,&scrollx,1,&scrolly,
			&Machine->drv->visible_area,
			TRANSPARENCY_NONE,0);
}

#define DRAW_SPRITE( order, sx, sy ) drawgfx( bitmap, gfx, \
					(which+order),color,flipx,flipy,sx,sy, \
					clip,TRANSPARENCY_PEN,0);

static void dd_draw_sprites( struct osd_bitmap *bitmap )
{
	const struct rectangle *clip = &Machine->drv->visible_area;
	const struct GfxElement *gfx = Machine->gfx[1];

	unsigned char *src = &( dd_spriteram[0x800] );
	int i;

	for( i = 0; i < ( 64 * 5 ); i += 5 ) {
		int attr = src[i+1];
		if ( attr & 0x80 ) { /* visible */
			int sx = 240 - src[i+4] + ( ( attr & 2 ) << 7 );
			int sy = 240 - src[i+0] + ( ( attr & 1 ) << 8 );
			int size = ( attr & 0x30 ) >> 4;
			int flipx = ( attr & 8 );
			int flipy = ( attr & 4 );

			int which;
			int color;

			if ( dd2_video ) {
				color = ( src[i+2] >> 5 );
				which = src[i+3] + ( ( src[i+2] & 0x1f ) << 8 );
			} else {
				color = ( src[i+2] >> 4 ) & 0x07;
				which = src[i+3] + ( ( src[i+2] & 0x0f ) << 8 );
			}

			switch ( size ) {
				case 0: /* normal */
				DRAW_SPRITE( 0, sx, sy );
				break;

				case 1: /* double y */
				DRAW_SPRITE( 0, sx, sy - 16 );
				DRAW_SPRITE( 1, sx, sy );
				break;

				case 2: /* double x */
				DRAW_SPRITE( 0, sx - 16, sy );
				DRAW_SPRITE( 2, sx, sy );
				break;

				case 3:
				DRAW_SPRITE( 0, sx - 16, sy - 16 );
				DRAW_SPRITE( 1, sx - 16, sy );
				DRAW_SPRITE( 2, sx, sy - 16 );
				DRAW_SPRITE( 3, sx, sy );
				break;
			}
		}
	}
}

#undef DRAW_SPRITE

static void dd_draw_foreground( struct osd_bitmap *bitmap )
{
	const struct GfxElement *gfx = Machine->gfx[0];
	unsigned char *source = videoram;

	int sx,sy;

	for( sy=0; sy<256; sy+=8 ){
		for( sx=0; sx<256; sx+=8 ){
			int attributes = source[0];
			int tile_number = source[1] + 256*( attributes & 7 );
			int color = ( attributes >> 5 ) & 0x7;

			if ( tile_number ) {
				drawgfx( bitmap,gfx, tile_number,
				color,
				0,0, /* no flip */
				sx,sy,
				0, /* no need to clip */
				TRANSPARENCY_PEN,0);
			}
			source += 2;
		}
	}
}



void dd_vh_screenrefresh(struct osd_bitmap *bitmap,int full_refresh)
{
	if (palette_recalc())
 		memset(dirtybuffer,1, 0x400);

	dd_draw_background( bitmap );
	dd_draw_sprites( bitmap );
	dd_draw_foreground( bitmap );
}
