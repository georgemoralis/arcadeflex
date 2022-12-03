#include "driver.h"
#include "vidhrdw/generic.h"

unsigned char *footchmp_chargen_ram, *footchmp_text_ram;
unsigned char *footchmp_layer0_ram, *footchmp_layer1_ram;
unsigned char *footchmp_layer2_ram, *footchmp_layer3_ram;

static unsigned char *char_dirty;
static struct tilemap *text_layer, *layer0_map, *layer1_map, *layer2_map, *layer3_map;

static int spritebank[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };

/***************************************************************************

	Layers

***************************************************************************/

/* Callbacks */
#define LAYER_CALLBACK( layer_name ) \
	static void layer_name##_map_info( int col, int row ) { \
		int tileoffs = ( row * 32 + col ) * 4; \
		int data0 = READ_WORD( &footchmp_##layer_name##_ram[tileoffs] ); \
		int data1 = READ_WORD( &footchmp_##layer_name##_ram[tileoffs+2] ); \
		tile_info.flags = TILE_FLIPYX( ( data0 & 0xc000 ) >> 14 ); \
		SET_TILE_INFO( 0, data1 & 0x1fff, data0 & 0xff ); \
	}

LAYER_CALLBACK( layer0 )
LAYER_CALLBACK( layer1 )
LAYER_CALLBACK( layer2 )
LAYER_CALLBACK( layer3 )

static void text_layer_info( int col, int row ) {

	int tileoffs = ( row * 64 + col ) * 2;
	int data = READ_WORD( &footchmp_text_ram[tileoffs] );

	tile_info.flags = TILE_FLIPYX( ( data & 0xc000 ) >> 14 );

	/* set code and color */
	SET_TILE_INFO( 2, data & 0xff, ( data & 0x3f00 ) >> 8 );
}

#define LAYER_READ( layer_name ) \
	int footchmp_##layer_name##ram_r( int offset ) { \
		return READ_WORD( &footchmp_##layer_name##_ram[offset] ); \
	}

#define LAYER_WRITE( layer_name ) \
	void footchmp_##layer_name##ram_w( int offset, int data ) { \
		int oldword = READ_WORD (&footchmp_##layer_name##_ram[offset]); \
		int newword = COMBINE_WORD (oldword,data); \
		if (oldword != newword) \
		{ \
			WRITE_WORD (&footchmp_##layer_name##_ram[offset],newword); \
			offset /= 4; \
			tilemap_mark_tile_dirty( layer_name##_map, offset % 32, offset / 32 ); \
		} \
	}

LAYER_READ( layer0 )
LAYER_READ( layer1 )
LAYER_READ( layer2 )
LAYER_READ( layer3 )
LAYER_WRITE( layer0 )
LAYER_WRITE( layer1 )
LAYER_WRITE( layer2 )
LAYER_WRITE( layer3 )

/***************************************************************************

	Character Generator + Text Layer

***************************************************************************/

int footchmp_textram_r( int offset ) {
	return READ_WORD( &footchmp_text_ram[offset] );
}

void footchmp_textram_w( int offset, int data ) {
	int oldword = READ_WORD (&footchmp_text_ram[offset]);
	int newword = COMBINE_WORD (oldword,data);

	if (oldword != newword)
	{
		WRITE_WORD (&footchmp_text_ram[offset],newword);
		offset /= 2;
		tilemap_mark_tile_dirty( text_layer, offset % 64, offset / 64 );
	}
}

/* we have to straighten out the 16-bit word into bytes for gfxdecode() to work */
int footchmp_chargen_r( int offset ) {
	int res;

	res = READ_WORD (&footchmp_chargen_ram[offset]);

	#ifdef LSB_FIRST
	res = ((res & 0x00ff) << 8) | ((res & 0xff00) >> 8);
	#endif

	return res;
}

void footchmp_chargen_w( int offset,int data ) {
	int oldword = READ_WORD (&footchmp_chargen_ram[offset]);
	int newword;


	#ifdef LSB_FIRST
	data = ((data & 0x00ff00ff) << 8) | ((data & 0xff00ff00) >> 8);
	#endif

	newword = COMBINE_WORD (oldword,data);
	if (oldword != newword)
	{
		WRITE_WORD (&footchmp_chargen_ram[offset],newword);
		char_dirty[offset/32] = 1;
	}
}

/***************************************************************************

	Sprites

***************************************************************************/

int footchmp_spriteram_r( int offset ) {
	return READ_WORD( &spriteram[offset] );
}

void footchmp_spriteram_w( int offset, int data ) {
	COMBINE_WORD_MEM( &spriteram[offset], data );
}

void footchmp_spritebank_w( int offset, int data ) {
	if (errorlog) fprintf (errorlog, "PC = %06x: bank %d, new value: %04x\n", cpu_get_pc(), offset >> 1, ( data & 0x0f ) << 10);
//	if ( ( offset >> 1 ) < 2 ) return;
//	if ( data == 0 ) data = ( ( offset >> 1 ) * 0x400 ) >> 10;
	spritebank[offset >> 1] = data & 0x0f;
}

#define CONVERT_SPRITE_CODE												\
{																		\
	int bank;															\
																		\
	bank = (code & 0x1800) >> 11;										\
	switch (bank)														\
	{																	\
		case 0: code = spritebank[2] * 0x800 + (code & 0x7ff); break;	\
		case 1: code = spritebank[3] * 0x800 + (code & 0x7ff); break;	\
		case 2: code = spritebank[4] * 0x400 + (code & 0x7ff); break;	\
		case 3: code = spritebank[6] * 0x800 + (code & 0x7ff); break;	\
	}																	\
}																		\


static void footchmp_mark_sprite_colors( void ) {
	unsigned short palette_map[256];
	int code, color, offs, i;

	memset (palette_map, 0, sizeof (palette_map));

	for ( offs = 0x30; offs < 0x8000; offs += 16 ) {
		code = READ_WORD(&spriteram[offs]);
		CONVERT_SPRITE_CODE;
		color = READ_WORD(&spriteram[offs+8]) & 0x00ff;

		palette_map[color] |= Machine->gfx[1]->pen_usage[code];
	}

	/* now build the final table */
	for ( i = 0; i < 256; i++ )
	{
		int usage = palette_map[i], j;
		if (usage)
		{
			for (j = 1; j < 16; j++)
				if (usage & (1 << j))
					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
		}
	}
}

static void footchmp_drawsprites( struct osd_bitmap *bitmap ) {
	/*

		from vidhrdw/taitof2.c

		Sprite format:
		0000: 000xxxxxxxxxxxxx: tile code (0x0000 - 0x1fff)
		0002: unused?
		0004: 0000xxxxxxxxxxxx: x-coordinate absolute (-0x1ff to 0x01ff)
		      0x00000000000000: don't compensate for scrolling ??
		0006: 0000xxxxxxxxxxxx: y-coordinate absolute (-0x1ff to 0x01ff)
		0008: 00000000xxxxxxxx: color (0x00 - 0xff)
		      000000xx00000000: flipx & flipy
		      00000x0000000000: if clear, latch x & y coordinates, tile & color ?
		      0000x00000000000: if set, next sprite entry is part of sequence
		      000x000000000000: if clear, use latched y coordinate, else use current y
		      00x0000000000000: if set, y += 16
		      0x00000000000000: if clear, use latched x coordinate, else use current x
		      x000000000000000: if set, x += 16
		000a - 000f : unused?

		Additionally, the first 3 sprite entries are configuration info instead of
		actual sprites:

		offset 0x24: sprite x offset
		offset 0x26: sprite y offset
	*/
	int x,y,offs,code,color,spritecont,flipx,flipy;
	int xcurrent,ycurrent;
	int scroll1x, scroll1y;
	int scrollx=0, scrolly=0;
	int curx,cury;

	scroll1x = READ_WORD(&spriteram[0x24]);
	scroll1y = READ_WORD(&spriteram[0x26]);

	x = y = 0;
	xcurrent = ycurrent = 0;
	color = 0;

	for (offs = 0x30;offs < 0x8000;offs += 16)
	{
        spritecont = (READ_WORD(&spriteram[offs+8]) & 0xff00) >> 8;

		if ((spritecont & 0xf4) == 0)
		{
			x = READ_WORD(&spriteram[offs+4]);
			if (x & 0x4000)
			{
				scrollx = 0;
				scrolly = 0;
			}
			else
			{
				scrollx = scroll1x;
				scrolly = scroll1y;
			}
			x &= 0x1ff;
			y = READ_WORD(&spriteram[offs+6]) & 0x01ff;
			color = READ_WORD(&spriteram[offs+8]) & 0x00ff;

			xcurrent = x;
			ycurrent = y;
		}
		else
		{
			if ((spritecont & 0x10) == 0)
				y = ycurrent;
			else if ((spritecont & 0x20) != 0)
				y += 16;

			if ((spritecont & 0x40) == 0)
				x = xcurrent;
			else if ((spritecont & 0x80) != 0)
				x += 16;
		}

		code = READ_WORD(&spriteram[offs]);
		if (!code) continue;

		CONVERT_SPRITE_CODE;

		flipx = spritecont & 0x01;
		flipy = spritecont & 0x02;

		// AJP (fixes sprites off right side of screen)
		curx = (x + scrollx) & 0x1ff;
		if (curx>0x140) curx -= 0x200;
		cury = (y + scrolly) & 0x1ff;
		if (cury>0x100) cury -= 0x200;

		drawgfx (bitmap,Machine->gfx[1],
				code,
				color,
				flipx,flipy,
				curx,cury,
				0,TRANSPARENCY_PEN,0);
	}
}

/***************************************************************************

	Video Control (scroll,zoom)

***************************************************************************/

void footchmp_scroll_w( int offset, int data ) {

	/* scroll values are adjusted at PC $8BA */

	switch( offset ) {
		case 0x00:
			data = ~( data - 33 ); /* adjust x scroll */
			tilemap_set_scrollx( layer0_map, 0, data );
		break;

		case 0x02:
			data = ~( data - 29 ); /* adjust x scroll */
			tilemap_set_scrollx( layer1_map, 0, data );
		break;

		case 0x04:
			data = ~( data - 25 ); /* adjust x scroll */
			tilemap_set_scrollx( layer2_map, 0, data );
		break;

		case 0x06:
			data = ~( data - 21 ); /* adjust x scroll */
			tilemap_set_scrollx( layer3_map, 0, data );
		break;

		case 0x08:
			data += 8; /* adjust y scroll */
			tilemap_set_scrolly( layer0_map, 0, data );
		break;

		case 0x0a:
			data += 8; /* adjust y scroll */
			tilemap_set_scrolly( layer1_map, 0, data );
		break;

		case 0x0c:
			data += 8; /* adjust y scroll */
			tilemap_set_scrolly( layer2_map, 0, data );
		break;

		case 0x0e:
			data += 8; /* adjust y scroll */
			tilemap_set_scrolly( layer3_map, 0, data );
		break;

		case 0x10: /* layer 0 zoom */
		case 0x12: /* layer 1 zoom */
		case 0x14: /* layer 2 zoom */
		case 0x16: /* layer 3 zoom */
		break;

		case 0x18:
			data = ~( data - 35 ); /* adjust x scroll */
			tilemap_set_scrollx( text_layer, 0, data );
		break;

		case 0x1a:
			data = ~( data - 9 ); /* adjust y scroll (inverted for this layer) */
			tilemap_set_scrolly( text_layer, 0, data );
		break;
	}
}

/***************************************************************************

	MAME functions

***************************************************************************/

void footchmp_vh_screenrefresh( struct osd_bitmap *bitmap, int full_refresh ) {
	int		i, inval = 1;

	/* Update character generator */
	for ( i = 0; i < 256; i++ ) {
		if ( char_dirty[i] ) {
			decodechar (Machine->gfx[2],i,footchmp_chargen_ram, Machine->drv->gfxdecodeinfo[2].gfxlayout);
			char_dirty[i] = 0;
			/* We must invalidate all the layer's tiles (do it only once per dirty scan) */
			if ( inval ) {
				tilemap_mark_all_tiles_dirty(text_layer);
				inval = 0;
			}
		}
	}

	tilemap_update( ALL_TILEMAPS );

	palette_init_used_colors();

	footchmp_mark_sprite_colors();

	if (palette_recalc())
		tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);

	tilemap_render(ALL_TILEMAPS);

	tilemap_draw(bitmap, layer0_map, 0 );
	tilemap_draw(bitmap, layer1_map, 0 );
	tilemap_draw(bitmap, layer2_map, 0 );
	tilemap_draw(bitmap, layer3_map, 0 );
	footchmp_drawsprites( bitmap );
	tilemap_draw(bitmap, text_layer, 0 );
}

int footchmp_vh_start( void )
{
	char_dirty = malloc( 256 );

	if ( char_dirty == 0 )
		return 1;

	memset( char_dirty, 1, 256 );

	text_layer = tilemap_create( text_layer_info, TILEMAP_TRANSPARENT,  8,  8, 64, 64 );
	layer3_map = tilemap_create( layer3_map_info, TILEMAP_TRANSPARENT, 16, 16, 32, 32 );
	layer2_map = tilemap_create( layer2_map_info, TILEMAP_TRANSPARENT, 16, 16, 32, 32 );
	layer1_map = tilemap_create( layer1_map_info, TILEMAP_TRANSPARENT, 16, 16, 32, 32 );
	layer0_map = tilemap_create( layer0_map_info, TILEMAP_OPAQUE,      16, 16, 32, 32 );

	if ( !text_layer || !layer3_map || !layer2_map || !layer1_map || !layer0_map )
	{
		free( char_dirty );
		return 1;
	}

	text_layer->transparent_pen = 0;
	layer3_map->transparent_pen = 0;
	layer2_map->transparent_pen = 0;
	layer1_map->transparent_pen = 0;

	return 0;
}

void footchmp_vh_stop( void ) {
	free( char_dirty );
}
