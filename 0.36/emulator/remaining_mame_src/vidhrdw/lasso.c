/* vidhrdw/lasso.c */

#include "driver.h"
#include "vidhrdw/generic.h"

extern unsigned char *lasso_vram; /* 0x2000 bytes for a 256x256x1 bitmap */

void lasso_vh_convert_color_prom(
	unsigned char *palette, unsigned short *colortable,const unsigned char *color_prom)
{
	int i;
	for( i=0; i<0x40; i++ ){
		int data = color_prom[i];
		/* rrrgggbb - probably wrong */
		*palette++ = ((data>>0)&0x3)*(0xff/0x3); // red
		*palette++ = ((data>>2)&0x7)*(0xff/0x7); // green
		*palette++ = ((data>>5)&0x7)*(0xff/0x7); // blue
		colortable[i] = i;
	}

	/*	allocate one extra entry for background color;
	**	this is probably set at runtime
	*/
	colortable[0x40] = 0x40;
	*palette++ = 0x60;
	*palette++ = 0xef;
	*palette++ = 0x60;
}

static void draw_sprites( struct osd_bitmap *bitmap ){
    const struct GfxElement *gfx = Machine->gfx[1];
    struct rectangle clip = Machine->drv->visible_area;
    const unsigned char *finish = spriteram;
	const unsigned char *source = spriteram + 0x80 - 4;
	while( source>=finish ){
		int color = source[2];
		int tile_number = source[1];
		int sy = source[0];
		int sx = source[3];

		int flipy = (tile_number&0x80);
		int flipx = (tile_number&0x40);

        drawgfx( bitmap,gfx,
            tile_number&0x3f,
            color,
            flipx, flipy,
            sx,(240-sy),
            &clip,TRANSPARENCY_PEN,0);

        source-=4;
    }
}

static void draw_background( struct osd_bitmap *bitmap ){
    const struct GfxElement *gfx = Machine->gfx[0];
	const unsigned char *source = videoram;
	int x,y;

	fillbitmap( bitmap, Machine->pens[0x40], NULL );

	for( y=0; y<256; y+=8 ){
		for( x=0; x<256; x+=8 ){
			drawgfx( bitmap,gfx,
				source[0],
				source[0x400], /* color */
				0,0, /* no flip */
				x,y,
				NULL, /* clip */
				TRANSPARENCY_PEN,0);

			source++;
		}
	}
}

static void draw_lasso( struct osd_bitmap *bitmap ){
	const unsigned char *source = lasso_vram;
	int x,y,bit;
	int pen = Machine->pens[0];
	for( y=0; y<256; y++ ){
		for( x=0; x<256; x+=8 ){
			int data = *source++;
			for( bit=0; bit<8; bit++ ){
				if( (data<<bit)&0x80 ){
					plot_pixel( bitmap, x+bit, y, pen );
				}
			}
		}
	}
}

void lasso_screenrefresh( struct osd_bitmap *bitmap, int fullrefresh ){
	palette_recalc();
	draw_background( bitmap );
	draw_lasso( bitmap );
	draw_sprites( bitmap );
}
