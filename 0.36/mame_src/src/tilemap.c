/* tilemap.c

New:
- the files tmapmvis.c,tmapdrao.c, and tmapdraw.c are no longer needed
- crude 16BPP support.  We currently do pen marking just as with 8BPP graphic


To Do:
- warnings (in debug mode, at least) for tilemap_mark_dirty using bad row/col
- virtualization for huge tilemaps
- inform palette manager about cached, offscreen tiles

Optimizations/Features TBA:
-	precompute spans per row (to speed up the low level code)
-	support for unusual tile sizes (8x12, 8x10)
-	screenwise scrolling
-	only update "scrolled" flag when a tilemap scrolls a full tile or more
-	better dirty marking (per row, per tilemap)
-	dirtygrid for non-scrolling games
-	profile average tiles changed per last N frames

	Usage Notes:

	When the videoram for a tile changes, call tilemap_mark_tile_dirty
	with the appropriate tile_index.

	In the video driver, follow these steps:

	1)	set each tilemap's scroll registers

	2)	call tilemap_update for each tilemap.

	3)	call palette_init_used_colors.
		mark the colors used by sprites.
		call palette recalc.  If the palette manager has compressed the palette,
			call tilemap_mark_all_pixels_dirty for each tilemap.

	4)	call tilemap_render for each tilemap.

	5)	call tilemap_draw to draw the tilemaps to the screen, from back to front
*/

#ifndef DECLARE

#include "driver.h"
#include "tilemap.h"

static UINT8 flip_bit_table[0x100]; /* horizontal flip for 8 pixels */
static struct tilemap *first_tilemap; /* resource tracking */
static int screen_width, screen_height;
struct tile_info tile_info;

enum {
	TILE_TRANSPARENT,
	TILE_MASKED,
	TILE_OPAQUE
};

/* the following parameters are constant across tilemap_draw calls */
static struct {
	int clip_left, clip_top, clip_right, clip_bottom;
	int source_width, source_height;

	int dest_line_offset,source_line_offset,mask_line_offset;
	int dest_row_offset,source_row_offset,mask_row_offset;
	struct osd_bitmap *screen, *pixmap, *bitmask;

	UINT8 **mask_data_row;
	char **priority_data_row, **visible_row;
	UINT8 priority;
} blit;

#define MASKROWBYTES(W) (((W)+7)/8)

static void memcpybitmask8( UINT8 *dest, const UINT8 *source, const UINT8 *bitmask, int count ){
	for(;;){
		UINT8 data = *bitmask++;
		if( data&0x80 ) dest[0] = source[0];
		if( data&0x40 ) dest[1] = source[1];
		if( data&0x20 ) dest[2] = source[2];
		if( data&0x10 ) dest[3] = source[3];
		if( data&0x08 ) dest[4] = source[4];
		if( data&0x04 ) dest[5] = source[5];
		if( data&0x02 ) dest[6] = source[6];
		if( data&0x01 ) dest[7] = source[7];
		if( --count == 0 ) break;
		source+=8;
		dest+=8;
	}
}
static void memcpybitmask16( UINT16 *dest, const UINT16 *source, const UINT8 *bitmask, int count ){
	for(;;){
		UINT16 data = *bitmask++;
		if( data&0x80 ) dest[0] = source[0];
		if( data&0x40 ) dest[1] = source[1];
		if( data&0x20 ) dest[2] = source[2];
		if( data&0x10 ) dest[3] = source[3];
		if( data&0x08 ) dest[4] = source[4];
		if( data&0x04 ) dest[5] = source[5];
		if( data&0x02 ) dest[6] = source[6];
		if( data&0x01 ) dest[7] = source[7];
		if( --count == 0 ) break;
		source+=8;
		dest+=8;
	}
}

/***********************************************************************************/

#define TILE_WIDTH	8
#define TILE_HEIGHT	8
#define DATA_TYPE UINT8
#define memcpybitmask memcpybitmask8
#define DECLARE(function,args,body) static void function##8x8x8BPP args body
#include "tilemap.c"

#define TILE_WIDTH	16
#define TILE_HEIGHT	16
#define DATA_TYPE UINT8
#define memcpybitmask memcpybitmask8
#define DECLARE(function,args,body) static void function##16x16x8BPP args body
#include "tilemap.c"

#define TILE_WIDTH	32
#define TILE_HEIGHT	32
#define DATA_TYPE UINT8
#define memcpybitmask memcpybitmask8
#define DECLARE(function,args,body) static void function##32x32x8BPP args body
#include "tilemap.c"

#define TILE_WIDTH	8
#define TILE_HEIGHT	8
#define DATA_TYPE UINT16
#define memcpybitmask memcpybitmask16
#define DECLARE(function,args,body) static void function##8x8x16BPP args body
#include "tilemap.c"

#define TILE_WIDTH	16
#define TILE_HEIGHT	16
#define DATA_TYPE UINT16
#define memcpybitmask memcpybitmask16
#define DECLARE(function,args,body) static void function##16x16x16BPP args body
#include "tilemap.c"

#define TILE_WIDTH	32
#define TILE_HEIGHT	32
#define DATA_TYPE UINT16
#define memcpybitmask memcpybitmask16
#define DECLARE(function,args,body) static void function##32x32x16BPP args body
#include "tilemap.c"

/*********************************************************************************/

#define SWAP(X,Y) {int temp=X; X=Y; Y=temp; }

void tilemap_set_enable( struct tilemap *tilemap, int enable ){
	tilemap->enable = enable;
}

void tilemap_set_flip( struct tilemap *tilemap, int attributes ){
	if( tilemap==ALL_TILEMAPS ){
		tilemap = first_tilemap;
		while( tilemap ){
			tilemap_set_flip( tilemap, attributes );
			tilemap = tilemap->next;
		}
	}
	else if( tilemap->attributes!=attributes ){
		tilemap->attributes = attributes;

		tilemap->orientation = Machine->orientation;

		if( attributes&TILEMAP_FLIPY ){
			tilemap->orientation ^= ORIENTATION_FLIP_Y;
			tilemap->scrolly_delta = tilemap->dy_if_flipped;
		}
		else {
			tilemap->scrolly_delta = tilemap->dy;
		}

		if( attributes&TILEMAP_FLIPX ){
			tilemap->orientation ^= ORIENTATION_FLIP_X;
			tilemap->scrollx_delta = tilemap->dx_if_flipped;
		}
		else {
			tilemap->scrollx_delta = tilemap->dx;
		}

		tilemap_mark_all_tiles_dirty( tilemap );
	}
}

static struct osd_bitmap *create_tmpbitmap( int width, int height ){
	if( Machine->orientation&ORIENTATION_SWAP_XY ) SWAP(width,height);
	return osd_new_bitmap( width,height, Machine->scrbitmap->depth );
}

static struct osd_bitmap *create_bitmask( int width, int height ){
	if( Machine->orientation&ORIENTATION_SWAP_XY ) SWAP(width,height);
	return osd_new_bitmap( width,height, 8 );
}

void tilemap_set_clip( struct tilemap *tilemap, const struct rectangle *clip ){
	int left,top,right,bottom;

	if (clip){
		left = clip->min_x;
		top = clip->min_y;
		right = clip->max_x+1;
		bottom = clip->max_y+1;

		if( tilemap->orientation & ORIENTATION_SWAP_XY ){
			SWAP(left,top)
			SWAP(right,bottom)
		}
		if( tilemap->orientation & ORIENTATION_FLIP_X ){
			SWAP(left,right)
			left = screen_width-left;
			right = screen_width-right;
		}
		if( tilemap->orientation & ORIENTATION_FLIP_Y ){
			SWAP(top,bottom)
			top = screen_height-top;
			bottom = screen_height-bottom;
		}
	}
	else
	{
		left = 0;
		top = 0;
		right = tilemap->width;
		bottom = tilemap->height;
	}

	tilemap->clip_left = left;
	tilemap->clip_right = right;
	tilemap->clip_top = top;
	tilemap->clip_bottom = bottom;
	if( errorlog ) fprintf( errorlog, "clip: %d,%d,%d,%d\n", left,top,right,bottom );
}

void tilemap_init( void ){
	int value, data, bit;
	for( value=0; value<0x100; value++ ){
		data = 0;
		for( bit=0; bit<8; bit++ ) if( (value>>bit)&1 ) data |= 0x80>>bit;
		flip_bit_table[value] = data;
	}
	screen_width = Machine->scrbitmap->width;
	screen_height = Machine->scrbitmap->height;
	first_tilemap = 0;
}

/***********************************************************************************/

static void dispose_tile_info( struct tilemap *tilemap ){
	free( tilemap->pendata );
	free( tilemap->maskdata );
	free( tilemap->paldata );
	free( tilemap->pen_usage );
	free( tilemap->priority );
	free( tilemap->visible );
	free( tilemap->dirty_vram );
	free( tilemap->dirty_pixels );
	free( tilemap->flags );
	free( tilemap->priority_row );
	free( tilemap->visible_row );
}

static int create_tile_info( struct tilemap *tilemap ){
	int num_tiles = tilemap->num_tiles;
	int num_cols = tilemap->num_cols;
	int num_rows = tilemap->num_rows;

	tilemap->pendata = malloc( sizeof( UINT8 *)*num_tiles );
	tilemap->maskdata = malloc( sizeof( UINT8 *)*num_tiles ); /* needed only for TILEMAP_BITMASK */
	tilemap->paldata = malloc( sizeof( unsigned short *)*num_tiles );
	tilemap->pen_usage = malloc( sizeof( unsigned int )*num_tiles );
	tilemap->priority = malloc( num_tiles );
	tilemap->visible = malloc( num_tiles );
	tilemap->dirty_vram = malloc( num_tiles );
	tilemap->dirty_pixels = malloc( num_tiles );
	tilemap->flags = malloc( num_tiles );
	tilemap->rowscroll = (int *)calloc(tilemap->height,sizeof(int));
	tilemap->colscroll = (int *)calloc(tilemap->width,sizeof(int));

	tilemap->priority_row = malloc( sizeof(char *)*num_rows );
	tilemap->visible_row = malloc( sizeof(char *)*num_rows );

	if( tilemap->pendata &&
		tilemap->maskdata &&
		tilemap->paldata && tilemap->pen_usage &&
		tilemap->priority && tilemap->visible &&
		tilemap->dirty_vram && tilemap->dirty_pixels &&
		tilemap->flags &&
		tilemap->rowscroll && tilemap->colscroll &&
		tilemap->priority_row && tilemap->visible_row )
	{
		int tile_index,row;

		for( row=0; row<num_rows; row++ ){
			tilemap->priority_row[row] = tilemap->priority+num_cols*row;
			tilemap->visible_row[row] = tilemap->visible+num_cols*row;
		}

		for( tile_index=0; tile_index<num_tiles; tile_index++ ){
			tilemap->paldata[tile_index] = 0;
		}

		memset( tilemap->priority, 0, num_tiles );
		memset( tilemap->visible, 0, num_tiles );
		memset( tilemap->dirty_vram, 1, num_tiles );
		memset( tilemap->dirty_pixels, 1, num_tiles );

		return 1; /* done */
	}
	dispose_tile_info( tilemap );
	return 0; /* error */
}

void tilemap_set_scroll_cols( struct tilemap *tilemap, int n ){
	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
		if (tilemap->scroll_rows != n){
			tilemap->scroll_rows = n;
			tilemap->scrolled = 1;
		}
	}
	else {
		if (tilemap->scroll_cols != n){
			tilemap->scroll_cols = n;
			tilemap->scrolled = 1;
		}
	}
}

void tilemap_set_scroll_rows( struct tilemap *tilemap, int n ){
	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
		if (tilemap->scroll_cols != n){
			tilemap->scroll_cols = n;
			tilemap->scrolled = 1;
		}
	}
	else
	{
		if (tilemap->scroll_rows != n){
			tilemap->scroll_rows = n;
			tilemap->scrolled = 1;
		}
	}
}

static int create_pixmap( struct tilemap *tilemap ){
	tilemap->pixmap = create_tmpbitmap( tilemap->width, tilemap->height );
	if( tilemap->pixmap ){
		tilemap->pixmap_line_offset = tilemap->pixmap->line[1] - tilemap->pixmap->line[0];
		return 1; /* done */
	}
	return 0; /* error */
}

static void dispose_pixmap( struct tilemap *tilemap ){
	osd_free_bitmap( tilemap->pixmap );
	free( tilemap->colscroll );
	free( tilemap->rowscroll );
}

static UINT8 **new_mask_data_table( UINT8 *mask_data, int num_cols, int num_rows ){
	UINT8 **mask_data_row = malloc(num_rows * sizeof(UINT8 *));
	if( mask_data_row ){
		int row;
		for( row = 0; row<num_rows; row++ ) mask_data_row[row] = mask_data + num_cols*row;
	}
	return mask_data_row;
}

static int create_fg_mask( struct tilemap *tilemap ){
	//if( tilemap->type == TILEMAP_OPAQUE ) return 1;

	tilemap->fg_mask_data = malloc( tilemap->num_tiles );
	if( tilemap->fg_mask_data ){
		tilemap->fg_mask_data_row = new_mask_data_table( tilemap->fg_mask_data, tilemap->num_cols, tilemap->num_rows );
		if( tilemap->fg_mask_data_row ){
			tilemap->fg_mask = create_bitmask( MASKROWBYTES(tilemap->width), tilemap->height );
			if( tilemap->fg_mask ){
				tilemap->fg_mask_line_offset = tilemap->fg_mask->line[1] - tilemap->fg_mask->line[0];
				return 1; /* done */
			}
			free( tilemap->fg_mask_data_row );
		}
		free( tilemap->fg_mask_data );
	}
	return 0; /* error */
}

static int create_bg_mask( struct tilemap *tilemap ){
	if( (tilemap->type & TILEMAP_SPLIT)==0 ) return 1;

	tilemap->bg_mask_data = malloc( tilemap->num_tiles );
	if( tilemap->bg_mask_data ){
		tilemap->bg_mask_data_row = new_mask_data_table( tilemap->bg_mask_data, tilemap->num_cols, tilemap->num_rows );
		if( tilemap->bg_mask_data_row ){
			tilemap->bg_mask = create_bitmask( MASKROWBYTES(tilemap->width), tilemap->height );
			if( tilemap->bg_mask ){
				tilemap->bg_mask_line_offset = tilemap->bg_mask->line[1] - tilemap->bg_mask->line[0];
				return 1; /* done */
			}
			free( tilemap->bg_mask_data_row );
		}
		free( tilemap->bg_mask_data );
	}
	return 0; /* error */
}

static void dispose_fg_mask( struct tilemap *tilemap ){
	free( tilemap->fg_mask_data_row );
	free( tilemap->fg_mask_data );
	osd_free_bitmap( tilemap->fg_mask );
}

static void dispose_bg_mask( struct tilemap *tilemap ){
	if( tilemap->type & TILEMAP_SPLIT ){
		osd_free_bitmap( tilemap->bg_mask );
		free( tilemap->bg_mask_data_row );
		free( tilemap->bg_mask_data );
	}
}

/***********************************************************************************/

struct tilemap *tilemap_create(
		void (*tile_get_info)( int col, int row ),
		int type,
		int tile_width, int tile_height,
		int num_cols, int num_rows ){

	struct tilemap *tilemap = (struct tilemap *)calloc( 1,sizeof( struct tilemap ) );
	if( tilemap ){
		memset( tilemap, 0, sizeof( struct tilemap ) );

		tilemap->orientation = Machine->orientation;
		if( tilemap->orientation & ORIENTATION_SWAP_XY ){
			SWAP( tile_width, tile_height )
			SWAP( num_cols,num_rows )
		}

		if( errorlog ){
			fprintf( errorlog, "cached tilemap info:\n" );
			fprintf( errorlog, "tilewidth,tileheight:%d,%d\n",tile_width,tile_height );
			fprintf( errorlog, "cols,rows:%d,%d\n",num_cols,num_rows );
		}

		tilemap->tile_get_info = tile_get_info;
		tilemap->enable = 1;
		tilemap_set_clip( tilemap, &Machine->drv->visible_area );

		if( Machine->scrbitmap->depth==16 ){
			if( tile_width==8 && tile_height==8 ){
				tilemap->mark_visible = mark_visible8x8x16BPP;
				tilemap->draw = draw8x8x16BPP;
				tilemap->draw_opaque = draw_opaque8x8x16BPP;
			}
			else if( tile_width==16 && tile_height==16 ){
				tilemap->mark_visible = mark_visible16x16x16BPP;
				tilemap->draw = draw16x16x16BPP;
				tilemap->draw_opaque = draw_opaque16x16x16BPP;
			}
			else if( tile_width==32 && tile_height==32 ){
				tilemap->mark_visible = mark_visible32x32x16BPP;
				tilemap->draw = draw32x32x16BPP;
				tilemap->draw_opaque = draw_opaque32x32x16BPP;
			}
		}
		else {
			if( tile_width==8 && tile_height==8 ){
				tilemap->mark_visible = mark_visible8x8x8BPP;
				tilemap->draw = draw8x8x8BPP;
				tilemap->draw_opaque = draw_opaque8x8x8BPP;
			}
			else if( tile_width==16 && tile_height==16 ){
				tilemap->mark_visible = mark_visible16x16x8BPP;
				tilemap->draw = draw16x16x8BPP;
				tilemap->draw_opaque = draw_opaque16x16x8BPP;
			}
			else if( tile_width==32 && tile_height==32 ){
				tilemap->mark_visible = mark_visible32x32x8BPP;
				tilemap->draw = draw32x32x8BPP;
				tilemap->draw_opaque = draw_opaque32x32x8BPP;
			}
		}

		if( tilemap->mark_visible && tilemap->draw ){
			tilemap->type = type;

			tilemap->tile_width = tile_width;
			tilemap->tile_height = tile_height;
			tilemap->width = tile_width*num_cols;
			tilemap->height = tile_height*num_rows;

			tilemap->num_rows = num_rows;
			tilemap->num_cols = num_cols;
			tilemap->num_tiles = num_cols*num_rows;

			tilemap->scroll_rows = 1;
			tilemap->scroll_cols = 1;
			tilemap->scrolled = 1;

			tilemap->transparent_pen = -1; /* default (this is supplied by video driver) */

			if( create_pixmap( tilemap ) ){
				if( create_fg_mask( tilemap ) ){
					if( create_bg_mask( tilemap ) ){
						if( create_tile_info( tilemap ) ){
							tilemap->next = first_tilemap;
							first_tilemap = tilemap;
							return tilemap;
						}
						dispose_bg_mask( tilemap );
					}
					dispose_fg_mask( tilemap );
				}
				dispose_pixmap( tilemap );
			}
		}
		free( tilemap );
	}
	return 0; /* error */
}

void tilemap_dispose( struct tilemap *tilemap ){
	if( tilemap==first_tilemap ){
		first_tilemap = tilemap->next;
	}
	else {
		struct tilemap *prev = first_tilemap;
		while( prev->next != tilemap ) prev = prev->next;
		prev->next =tilemap->next;
	}

	dispose_tile_info( tilemap );
	dispose_bg_mask( tilemap );
	dispose_fg_mask( tilemap );
	dispose_pixmap( tilemap );
	free( tilemap );
}

void tilemap_close( void ){
	while( first_tilemap ){
		struct tilemap *next = first_tilemap->next;
		tilemap_dispose( first_tilemap );
		first_tilemap = next;
	}
}

/***********************************************************************************/

void tilemap_mark_tile_dirty( struct tilemap *tilemap, int col, int row ){
	/* convert logical coordinates to cached coordinates */
	if( tilemap->orientation & ORIENTATION_SWAP_XY ) SWAP(col,row)
	if( tilemap->orientation & ORIENTATION_FLIP_X ) col = tilemap->num_cols-1-col;
	if( tilemap->orientation & ORIENTATION_FLIP_Y ) row = tilemap->num_rows-1-row;

//	tilemap->dirty_vram_row[row][col] = 1;
	tilemap->dirty_vram[row*tilemap->num_cols + col] = 1;
}

void tilemap_mark_all_tiles_dirty( struct tilemap *tilemap ){
	if( tilemap==ALL_TILEMAPS ){
		tilemap = first_tilemap;
		while( tilemap ){
			tilemap_mark_all_tiles_dirty( tilemap );
			tilemap = tilemap->next;
		}
	}
	else {
		memset( tilemap->dirty_vram, 1, tilemap->num_tiles );
	}
}

void tilemap_mark_all_pixels_dirty( struct tilemap *tilemap ){
	if( tilemap==ALL_TILEMAPS ){
		tilemap = first_tilemap;
		while( tilemap ){
			tilemap_mark_all_pixels_dirty( tilemap );
			tilemap = tilemap->next;
		}
	}
	else {
		/* let's invalidate all offscreen tiles, decreasing the refcounts */
		int tile_index;
		int num_pens = tilemap->tile_width*tilemap->tile_height; /* precalc - needed for >4bpp pen management handling */
		for( tile_index=0; tile_index<tilemap->num_tiles; tile_index++ ){
			if( !tilemap->visible[tile_index] ){
				unsigned short *the_color = tilemap->paldata[tile_index];
				if( the_color ){
					unsigned int old_pen_usage = tilemap->pen_usage[tile_index];
					if( old_pen_usage ){
						palette_decrease_usage_count( the_color-Machine->remapped_colortable, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
					}
					else {
						palette_decrease_usage_countx( the_color-Machine->remapped_colortable, num_pens, tilemap->pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
					}
					tilemap->paldata[tile_index] = NULL;
				}
				tilemap->dirty_vram[tile_index] = 1;
			}
		}
		memset( tilemap->dirty_pixels, 1, tilemap->num_tiles );
	}
}

/***********************************************************************************/

static void draw_tile(
		struct osd_bitmap *pixmap,
		int col, int row, int tile_width, int tile_height,
		const UINT8 *pendata, const unsigned short *paldata,
		UINT8 flags )
{
	int x, sx = tile_width*col;
	int sy,y1,y2,dy;

	if( Machine->scrbitmap->depth==16 ){
		if( flags&TILE_FLIPY ){
			y1 = tile_height*row+tile_height-1;
			y2 = y1-tile_height;
	 		dy = -1;
	 	}
	 	else {
			y1 = tile_height*row;
			y2 = y1+tile_height;
	 		dy = 1;
	 	}

		if( flags&TILE_FLIPX ){
			tile_width--;
			for( sy=y1; sy!=y2; sy+=dy ){
				UINT16 *dest  = sx + (UINT16 *)pixmap->line[sy];
				for( x=tile_width; x>=0; x-- ) dest[x] = paldata[*pendata++];
			}
		}
		else {
			for( sy=y1; sy!=y2; sy+=dy ){
				UINT16 *dest  = sx + (UINT16 *)pixmap->line[sy];
				for( x=0; x<tile_width; x++ ) dest[x] = paldata[*pendata++];
			}
		}
	}
	else {
		if( flags&TILE_FLIPY ){
			y1 = tile_height*row+tile_height-1;
			y2 = y1-tile_height;
	 		dy = -1;
	 	}
	 	else {
			y1 = tile_height*row;
			y2 = y1+tile_height;
	 		dy = 1;
	 	}

		if( flags&TILE_FLIPX ){
			tile_width--;
			for( sy=y1; sy!=y2; sy+=dy ){
				UINT8 *dest  = sx + (UINT8 *)pixmap->line[sy];
				for( x=tile_width; x>=0; x-- ) dest[x] = paldata[*pendata++];
			}
		}
		else {
			for( sy=y1; sy!=y2; sy+=dy ){
				UINT8 *dest  = sx + (UINT8 *)pixmap->line[sy];
				for( x=0; x<tile_width; x++ ) dest[x] = paldata[*pendata++];
			}
		}
	}
}

static void draw_mask(
		struct osd_bitmap *mask,
		int col, int row, int tile_width, int tile_height,
		const UINT8 *pendata, unsigned int transmask,
		UINT8 flags )
{
	int x,bit,sx = tile_width*col;
	int sy,y1,y2,dy;

	if( flags&TILE_FLIPY ){
		y1 = tile_height*row+tile_height-1;
		y2 = y1-tile_height;
 		dy = -1;
 	}
 	else {
		y1 = tile_height*row;
		y2 = y1+tile_height;
 		dy = 1;
 	}

	if( flags&TILE_FLIPX ){
		tile_width--;
		for( sy=y1; sy!=y2; sy+=dy ){
			UINT8 *mask_dest  = mask->line[sy]+sx/8;
			for( x=tile_width/8; x>=0; x-- ){
				UINT8 data = 0;
				for( bit=0; bit<8; bit++ ){
					UINT8 p = *pendata++;
					data = (data>>1)|(((1<<p)&transmask)?0x00:0x80);
				}
				mask_dest[x] = data;
			}
		}
	}
	else {
		for( sy=y1; sy!=y2; sy+=dy ){
			UINT8 *mask_dest  = mask->line[sy]+sx/8;
			for( x=0; x<tile_width/8; x++ ){
				UINT8 data = 0;
				for( bit=0; bit<8; bit++ ){
					UINT8 p = *pendata++;
					data = (data<<1)|(((1<<p)&transmask)?0x00:0x01);
				}
				mask_dest[x] = data;
			}
		}
	}
}

/***********************************************************************************/

static int draw_bitmask(
		struct osd_bitmap *mask,
		int col, int row, int tile_width, int tile_height,
		const UINT8 *maskdata,
		UINT8 flags )
{
	int is_opaque = 1, is_transparent = 1;

	int x,sx = tile_width*col;
	int sy,y1,y2,dy;

	if(maskdata==TILEMAP_BITMASK_TRANSPARENT)  return TILE_TRANSPARENT;
	if(maskdata==TILEMAP_BITMAK_OPAQUE) return TILE_OPAQUE;

	if( flags&TILE_FLIPY ){
		y1 = tile_height*row+tile_height-1;
		y2 = y1-tile_height;
 		dy = -1;
 	}
 	else {
		y1 = tile_height*row;
		y2 = y1+tile_height;
 		dy = 1;
 	}

	if( flags&TILE_FLIPX ){
		tile_width--;
		for( sy=y1; sy!=y2; sy+=dy ){
			UINT8 *mask_dest  = mask->line[sy]+sx/8;
			for( x=tile_width/8; x>=0; x-- ){
				UINT8 data = flip_bit_table[*maskdata++];
				if( data!=0x00 ) is_transparent = 0;
				if( data!=0xff ) is_opaque = 0;
				mask_dest[x] = data;
			}
		}
	}
	else {
		for( sy=y1; sy!=y2; sy+=dy ){
			UINT8 *mask_dest  = mask->line[sy]+sx/8;
			for( x=0; x<tile_width/8; x++ ){
				UINT8 data = *maskdata++;
				if( data!=0x00 ) is_transparent = 0;
				if( data!=0xff ) is_opaque = 0;
				mask_dest[x] = data;
			}
		}
	}

	if( is_transparent ) return TILE_TRANSPARENT;
	if( is_opaque ) return TILE_OPAQUE;
	return TILE_MASKED;
}

void tilemap_render( struct tilemap *tilemap ){
	if( tilemap==ALL_TILEMAPS ){
		tilemap = first_tilemap;
		while( tilemap ){
			tilemap_render( tilemap );
			tilemap = tilemap->next;
		}
	}
	else if( tilemap->enable ){
		int type = tilemap->type;
		int transparent_pen = tilemap->transparent_pen;
		unsigned int *transmask = tilemap->transmask;

		int tile_width = tilemap->tile_width;
		int tile_height = tilemap->tile_height;

		char *dirty_pixels = tilemap->dirty_pixels;
		char *visible = tilemap->visible;
		volatile int tile_index = 0; // LBO - CWPro4 bug workaround
		int row,col;

		for( row=0; row<tilemap->num_rows; row++ ){
			for( col=0; col<tilemap->num_cols; col++ ){
				if( dirty_pixels[tile_index] && visible[tile_index] ){
					unsigned int pen_usage = tilemap->pen_usage[tile_index];
					const UINT8 *pendata = tilemap->pendata[tile_index];
					UINT8 flags = tilemap->flags[tile_index];

					draw_tile(
						tilemap->pixmap,
						col, row, tile_width, tile_height,
						pendata,
						tilemap->paldata[tile_index],
						flags );
					if( type & TILEMAP_BITMASK ){
						tilemap->fg_mask_data_row[row][col] =
							draw_bitmask( tilemap->fg_mask,
								col, row, tile_width, tile_height,
								tilemap->maskdata[tile_index], flags );
					}
					else if( type & TILEMAP_SPLIT ){
						int pen_mask = (transparent_pen<0)?0:(1<<transparent_pen);

						if( flags&TILE_IGNORE_TRANSPARENCY ){
							tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
							tilemap->bg_mask_data_row[row][col] = TILE_OPAQUE;
						}
						else if( pen_mask == pen_usage ){ /* totally transparent */
							tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
							tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
						}
						else {
							unsigned int fg_transmask = transmask[(flags>>2)&3];
							unsigned int bg_transmask = (~fg_transmask)|pen_mask;
							if( (pen_usage & fg_transmask)==0 ){ /* foreground totally opaque */
								tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
								tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
							}
							else if( (pen_usage & bg_transmask)==0 ){ /* background totally opaque */
								tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
								tilemap->bg_mask_data_row[row][col] = TILE_OPAQUE;
							}
							else if( (pen_usage & ~bg_transmask)==0 ){ /* background transparent */
								draw_mask( tilemap->fg_mask,
									col, row, tile_width, tile_height,
									pendata, fg_transmask, flags );
								tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
								tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
							}
							else if( (pen_usage & ~fg_transmask)==0 ){ /* foreground transparent */
								draw_mask( tilemap->bg_mask,
									col, row, tile_width, tile_height,
									pendata, bg_transmask, flags );
								tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
								tilemap->bg_mask_data_row[row][col] = TILE_MASKED;
							}
							else { /* split tile - opacity in both foreground and background */
								draw_mask( tilemap->fg_mask,
									col, row, tile_width, tile_height,
									pendata, fg_transmask, flags );
								draw_mask( tilemap->bg_mask,
									col, row, tile_width, tile_height,
									pendata, bg_transmask, flags );
								tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
								tilemap->bg_mask_data_row[row][col] = TILE_MASKED;
							}
						}
				 	}
				 	else if( type==TILEMAP_TRANSPARENT ){
				 		unsigned int fg_transmask = 1 << transparent_pen;
				 	 	if( flags&TILE_IGNORE_TRANSPARENCY ) fg_transmask = 0;

						if( pen_usage == fg_transmask ){
							tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
						}
						else if( pen_usage & fg_transmask ){
							draw_mask( tilemap->fg_mask,
								col, row, tile_width, tile_height,
								pendata, fg_transmask, flags );
							tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
						}
						else {
							tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
						}
					}
					else {
						tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
				 	}

					dirty_pixels[tile_index] = 0;
				}
				tile_index++;
			} /* next col */
		} /* next row */
	}
}

/***********************************************************************************/

void tilemap_draw( struct osd_bitmap *dest, struct tilemap *tilemap, int priority ){
	int xpos,ypos;

	if( tilemap->enable ){
		void (*draw)( int, int );

		int rows = tilemap->scroll_rows;
		const int *rowscroll = tilemap->rowscroll;
		int cols = tilemap->scroll_cols;
		const int *colscroll = tilemap->colscroll;

		int left = tilemap->clip_left;
		int right = tilemap->clip_right;
		int top = tilemap->clip_top;
		int bottom = tilemap->clip_bottom;

		int tile_height = tilemap->tile_height;

		blit.screen = dest;
		blit.dest_line_offset = dest->line[1] - dest->line[0];

		blit.pixmap = tilemap->pixmap;
		blit.source_line_offset = tilemap->pixmap_line_offset;

		if( tilemap->type==TILEMAP_OPAQUE || (priority&TILEMAP_IGNORE_TRANSPARENCY) ){
			draw = tilemap->draw_opaque;
		}
		else {
			draw = tilemap->draw;

			if( priority&TILEMAP_BACK ){
				blit.bitmask = tilemap->bg_mask;
				blit.mask_line_offset = tilemap->bg_mask_line_offset;
				blit.mask_data_row = tilemap->bg_mask_data_row;
			}
			else {
				blit.bitmask = tilemap->fg_mask;
				blit.mask_line_offset = tilemap->fg_mask_line_offset;
				blit.mask_data_row = tilemap->fg_mask_data_row;
			}

			blit.mask_row_offset = tile_height*blit.mask_line_offset;
		}

		if( dest->depth==16 ){
			blit.dest_line_offset /= 2;
			blit.source_line_offset /= 2;
		}

		blit.source_row_offset = tile_height*blit.source_line_offset;
		blit.dest_row_offset = tile_height*blit.dest_line_offset;

		blit.priority_data_row = tilemap->priority_row;
		blit.source_width = tilemap->width;
		blit.source_height = tilemap->height;
		blit.priority = priority&0xf;

		if( rows == 0 && cols == 0 ){ /* no scrolling */
	 		blit.clip_left = left;
	 		blit.clip_top = top;
	 		blit.clip_right = right;
	 		blit.clip_bottom = bottom;

			draw( 0,0 );
		}
		else if( rows == 0 ){ /* scrolling columns */
			int col = 0;
			int colwidth = blit.source_width / cols;

			blit.clip_top = top;
			blit.clip_bottom = bottom;

			while( col < cols ){
				int cons = 1;
				int scrolly = colscroll[col];

	 			/* count consecutive columns scrolled by the same amount */
				if( scrolly != TILE_LINE_DISABLED ){
					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;

					if (scrolly < 0){
						scrolly = blit.source_height - (-scrolly) % blit.source_height;
					}
					else {
						scrolly %= blit.source_height;
					}

					blit.clip_left = col * colwidth;
					if( blit.clip_left < left ) blit.clip_left = left;
					blit.clip_right = (col + cons) * colwidth;
					if( blit.clip_right > right ) blit.clip_right = right;

					for(
						ypos = scrolly - blit.source_height;
						ypos < blit.clip_bottom;
						ypos += blit.source_height )
					{
						draw( 0,ypos );
					}
				}
				col += cons;
			}
		}
		else if( cols == 0 ){ /* scrolling rows */
			int row = 0;
			int rowheight = blit.source_height / rows;

			blit.clip_left = left;
			blit.clip_right = right;

			while( row < rows ){
				int cons = 1;
				int scrollx = rowscroll[row];

				/* count consecutive rows scrolled by the same amount */
				if( scrollx != TILE_LINE_DISABLED ){
					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;

					if( scrollx < 0 ){
						scrollx = blit.source_width - (-scrollx) % blit.source_width;
					}
					else {
						scrollx %= blit.source_width;
					}

					blit.clip_top = row * rowheight;
					if (blit.clip_top < top) blit.clip_top = top;
					blit.clip_bottom = (row + cons) * rowheight;
					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

					for(
						xpos = scrollx - blit.source_width;
						xpos<blit.clip_right;
						xpos += blit.source_width
					){
						draw( xpos,0 );
					}
				}
				row += cons;
			}
		}
		else if( rows == 1 && cols == 1 ){ /* XY scrolling playfield */
			int scrollx = rowscroll[0];
			int scrolly = colscroll[0];

			if( scrollx < 0 ){
				scrollx = blit.source_width - (-scrollx) % blit.source_width;
			}
			else {
				scrollx = scrollx % blit.source_width;
			}

			if( scrolly < 0 ){
				scrolly = blit.source_height - (-scrolly) % blit.source_height;
			}
			else {
				scrolly = scrolly % blit.source_height;
			}

	 		blit.clip_left = left;
	 		blit.clip_top = top;
	 		blit.clip_right = right;
	 		blit.clip_bottom = bottom;

			for(
				ypos = scrolly - blit.source_height;
				ypos < blit.clip_bottom;
				ypos += blit.source_height
			){
				for(
					xpos = scrollx - blit.source_width;
					xpos < blit.clip_right;
					xpos += blit.source_width
				){
					draw( xpos,ypos );
				}
			}
		}
		else if( rows == 1 ){ /* scrolling columns + horizontal scroll */
			int col = 0;
			int colwidth = blit.source_width / cols;
			int scrollx = rowscroll[0];

			if( scrollx < 0 ){
				scrollx = blit.source_width - (-scrollx) % blit.source_width;
			}
			else {
				scrollx = scrollx % blit.source_width;
			}

			blit.clip_top = top;
			blit.clip_bottom = bottom;

			while( col < cols ){
				int cons = 1;
				int scrolly = colscroll[col];

	 			/* count consecutive columns scrolled by the same amount */
				if( scrolly != TILE_LINE_DISABLED ){
					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;

					if( scrolly < 0 ){
						scrolly = blit.source_height - (-scrolly) % blit.source_height;
					}
					else {
						scrolly %= blit.source_height;
					}

					blit.clip_left = col * colwidth + scrollx;
					if (blit.clip_left < left) blit.clip_left = left;
					blit.clip_right = (col + cons) * colwidth + scrollx;
					if (blit.clip_right > right) blit.clip_right = right;

					for(
						ypos = scrolly - blit.source_height;
						ypos < blit.clip_bottom;
						ypos += blit.source_height
					){
						draw( scrollx,ypos );
					}

					blit.clip_left = col * colwidth + scrollx - blit.source_width;
					if (blit.clip_left < left) blit.clip_left = left;
					blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
					if (blit.clip_right > right) blit.clip_right = right;

					for(
						ypos = scrolly - blit.source_height;
						ypos < blit.clip_bottom;
						ypos += blit.source_height
					){
						draw( scrollx - blit.source_width,ypos );
					}
				}
				col += cons;
			}
		}
		else if( cols == 1 ){ /* scrolling rows + vertical scroll */
			int row = 0;
			int rowheight = blit.source_height / rows;
			int scrolly = colscroll[0];

			if( scrolly < 0 ){
				scrolly = blit.source_height - (-scrolly) % blit.source_height;
			}
			else {
				scrolly = scrolly % blit.source_height;
			}

			blit.clip_left = left;
			blit.clip_right = right;

			while( row < rows ){
				int cons = 1;
				int scrollx = rowscroll[row];

				/* count consecutive rows scrolled by the same amount */

				if( scrollx != TILE_LINE_DISABLED ){
					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;

					if( scrollx < 0){
						scrollx = blit.source_width - (-scrollx) % blit.source_width;
					}
					else {
						scrollx %= blit.source_width;
					}

					blit.clip_top = row * rowheight + scrolly;
					if (blit.clip_top < top) blit.clip_top = top;
					blit.clip_bottom = (row + cons) * rowheight + scrolly;
					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

					for(
						xpos = scrollx - blit.source_width;
						xpos < blit.clip_right;
						xpos += blit.source_width
					){
						draw( xpos,scrolly );
					}

					blit.clip_top = row * rowheight + scrolly - blit.source_height;
					if (blit.clip_top < top) blit.clip_top = top;
					blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

					for(
						xpos = scrollx - blit.source_width;
						xpos < blit.clip_right;
						xpos += blit.source_width
					){
						draw( xpos,scrolly - blit.source_height );
					}
				}
				row += cons;
			}
		}
	}
}

void tilemap_update( struct tilemap *tilemap ){
	if( tilemap==ALL_TILEMAPS ){
		tilemap = first_tilemap;
		while( tilemap ){
			tilemap_update( tilemap );
			tilemap = tilemap->next;
		}
	}
	else if( tilemap->enable ){
		if( tilemap->scrolled ){
			void (*mark_visible)( int, int ) = tilemap->mark_visible;

			int rows = tilemap->scroll_rows;
			const int *rowscroll = tilemap->rowscroll;
			int cols = tilemap->scroll_cols;
			const int *colscroll = tilemap->colscroll;

			int left = tilemap->clip_left;
			int right = tilemap->clip_right;
			int top = tilemap->clip_top;
			int bottom = tilemap->clip_bottom;

			blit.source_width = tilemap->width;
			blit.source_height = tilemap->height;
			blit.visible_row = tilemap->visible_row;

			memset( tilemap->visible, 0, tilemap->num_tiles );

			if( rows == 0 && cols == 0 ){ /* no scrolling */
		 		blit.clip_left = left;
		 		blit.clip_top = top;
		 		blit.clip_right = right;
		 		blit.clip_bottom = bottom;

				mark_visible( 0,0 );
			}
			else if( rows == 0 ){ /* scrolling columns */
				int col,colwidth;

				colwidth = blit.source_width / cols;

				blit.clip_top = top;
				blit.clip_bottom = bottom;

				col = 0;
				while( col < cols ){
					int cons,scroll;

		 			/* count consecutive columns scrolled by the same amount */
					scroll = colscroll[col];
					cons = 1;
					if(scroll != TILE_LINE_DISABLED)
					{
						while( col + cons < cols &&	colscroll[col + cons] == scroll ) cons++;

						if (scroll < 0) scroll = blit.source_height - (-scroll) % blit.source_height;
						else scroll %= blit.source_height;

						blit.clip_left = col * colwidth;
						if (blit.clip_left < left) blit.clip_left = left;
						blit.clip_right = (col + cons) * colwidth;
						if (blit.clip_right > right) blit.clip_right = right;

						mark_visible( 0,scroll );
						mark_visible( 0,scroll - blit.source_height );
					}
					col += cons;
				}
			}
			else if( cols == 0 ){ /* scrolling rows */
				int row,rowheight;

				rowheight = blit.source_height / rows;

				blit.clip_left = left;
				blit.clip_right = right;

				row = 0;
				while( row < rows ){
					int cons,scroll;

					/* count consecutive rows scrolled by the same amount */
					scroll = rowscroll[row];
					cons = 1;
					if(scroll != TILE_LINE_DISABLED)
					{
						while( row + cons < rows &&	rowscroll[row + cons] == scroll ) cons++;

						if (scroll < 0) scroll = blit.source_width - (-scroll) % blit.source_width;
						else scroll %= blit.source_width;

						blit.clip_top = row * rowheight;
						if (blit.clip_top < top) blit.clip_top = top;
						blit.clip_bottom = (row + cons) * rowheight;
						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

						mark_visible( scroll,0 );
						mark_visible( scroll - blit.source_width,0 );
					}
					row += cons;
				}
			}
			else if( rows == 1 && cols == 1 ){ /* XY scrolling playfield */
				int scrollx,scrolly;

				if (rowscroll[0] < 0) scrollx = blit.source_width - (-rowscroll[0]) % blit.source_width;
				else scrollx = rowscroll[0] % blit.source_width;

				if (colscroll[0] < 0) scrolly = blit.source_height - (-colscroll[0]) % blit.source_height;
				else scrolly = colscroll[0] % blit.source_height;

		 		blit.clip_left = left;
		 		blit.clip_top = top;
		 		blit.clip_right = right;
		 		blit.clip_bottom = bottom;

				mark_visible( scrollx,scrolly );
				mark_visible( scrollx,scrolly - blit.source_height );
				mark_visible( scrollx - blit.source_width,scrolly );
				mark_visible( scrollx - blit.source_width,scrolly - blit.source_height );
			}
			else if( rows == 1 ){ /* scrolling columns + horizontal scroll */
				int col,colwidth;
				int scrollx;

				if (rowscroll[0] < 0) scrollx = blit.source_width - (-rowscroll[0]) % blit.source_width;
				else scrollx = rowscroll[0] % blit.source_width;

				colwidth = blit.source_width / cols;

				blit.clip_top = top;
				blit.clip_bottom = bottom;

				col = 0;
				while( col < cols ){
					int cons,scroll;

		 			/* count consecutive columns scrolled by the same amount */
					scroll = colscroll[col];
					cons = 1;
					if(scroll != TILE_LINE_DISABLED)
					{
						while( col + cons < cols &&	colscroll[col + cons] == scroll ) cons++;

						if (scroll < 0) scroll = blit.source_height - (-scroll) % blit.source_height;
						else scroll %= blit.source_height;

						blit.clip_left = col * colwidth + scrollx;
						if (blit.clip_left < left) blit.clip_left = left;
						blit.clip_right = (col + cons) * colwidth + scrollx;
						if (blit.clip_right > right) blit.clip_right = right;

						mark_visible( scrollx,scroll );
						mark_visible( scrollx,scroll - blit.source_height );

						blit.clip_left = col * colwidth + scrollx - blit.source_width;
						if (blit.clip_left < left) blit.clip_left = left;
						blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
						if (blit.clip_right > right) blit.clip_right = right;

						mark_visible( scrollx - blit.source_width,scroll );
						mark_visible( scrollx - blit.source_width,scroll - blit.source_height );
					}
					col += cons;
				}
			}
			else if( cols == 1 ){ /* scrolling rows + vertical scroll */
				int row,rowheight;
				int scrolly;

				if (colscroll[0] < 0) scrolly = blit.source_height - (-colscroll[0]) % blit.source_height;
				else scrolly = colscroll[0] % blit.source_height;

				rowheight = blit.source_height / rows;

				blit.clip_left = left;
				blit.clip_right = right;

				row = 0;
				while( row < rows ){
					int cons,scroll;

					/* count consecutive rows scrolled by the same amount */
					scroll = rowscroll[row];
					cons = 1;
					if(scroll != TILE_LINE_DISABLED)
					{
						while (row + cons < rows &&	rowscroll[row + cons] == scroll) cons++;

						if (scroll < 0) scroll = blit.source_width - (-scroll) % blit.source_width;
						else scroll %= blit.source_width;

						blit.clip_top = row * rowheight + scrolly;
						if (blit.clip_top < top) blit.clip_top = top;
						blit.clip_bottom = (row + cons) * rowheight + scrolly;
						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

						mark_visible( scroll,scrolly );
						mark_visible( scroll - blit.source_width,scrolly );

						blit.clip_top = row * rowheight + scrolly - blit.source_height;
						if (blit.clip_top < top) blit.clip_top = top;
						blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;

						mark_visible( scroll,scrolly - blit.source_height );
						mark_visible( scroll - blit.source_width,scrolly - blit.source_height );
					}
					row += cons;
				}
			}

			tilemap->scrolled = 0;
		}

		{
			int num_pens = tilemap->tile_width*tilemap->tile_height; /* precalc - needed for >4bpp pen management handling */

			int tile_index;
			char *visible = tilemap->visible;
			char *dirty_vram = tilemap->dirty_vram;
			char *dirty_pixels = tilemap->dirty_pixels;

			UINT8 **pendata = tilemap->pendata;
			UINT8 **maskdata = tilemap->maskdata;
			unsigned short **paldata = tilemap->paldata;
			unsigned int *pen_usage = tilemap->pen_usage;

			int tile_flip = 0;
			if( tilemap->attributes&TILEMAP_FLIPX ) tile_flip |= TILE_FLIPX;
			if( tilemap->attributes&TILEMAP_FLIPY ) tile_flip |= TILE_FLIPY;
#ifndef PREROTATE_GFX
			if( Machine->orientation & ORIENTATION_SWAP_XY )
			{
				if( Machine->orientation & ORIENTATION_FLIP_X ) tile_flip ^= TILE_FLIPY;
				if( Machine->orientation & ORIENTATION_FLIP_Y ) tile_flip ^= TILE_FLIPX;
			}
			else
			{
				if( Machine->orientation & ORIENTATION_FLIP_X ) tile_flip ^= TILE_FLIPX;
				if( Machine->orientation & ORIENTATION_FLIP_Y ) tile_flip ^= TILE_FLIPY;
			}
#endif

			tile_info.flags = 0;
			tile_info.priority = 0;

			for( tile_index=0; tile_index<tilemap->num_tiles; tile_index++ ){
				if( visible[tile_index] && dirty_vram[tile_index] ){
					int row = tile_index/tilemap->num_cols;
					int col = tile_index%tilemap->num_cols;
					int flags;

					if( tilemap->orientation & ORIENTATION_FLIP_Y ) row = tilemap->num_rows-1-row;
					if( tilemap->orientation & ORIENTATION_FLIP_X ) col = tilemap->num_cols-1-col;
					if( tilemap->orientation & ORIENTATION_SWAP_XY ) SWAP(col,row)

					{
						unsigned short *the_color = paldata[tile_index];
						if( the_color ){
							unsigned int old_pen_usage = pen_usage[tile_index];
							if( old_pen_usage ){
								palette_decrease_usage_count( the_color-Machine->remapped_colortable, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
							}
							else {
								palette_decrease_usage_countx( the_color-Machine->remapped_colortable, num_pens, pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
							}
						}
					}
					tilemap->tile_get_info( col, row );

					flags = tile_info.flags ^ tile_flip;
					if( tilemap->orientation & ORIENTATION_SWAP_XY ){
						flags =
							(flags&0xfc) |
							((flags&1)<<1) | ((flags&2)>>1);
					}

					pen_usage[tile_index] = tile_info.pen_usage;
					pendata[tile_index] = tile_info.pen_data;
					paldata[tile_index] = tile_info.pal_data;
					maskdata[tile_index] = tile_info.mask_data; // needed for TILEMAP_BITMASK
					tilemap->flags[tile_index] = flags;
					tilemap->priority[tile_index] = tile_info.priority;


					if( tile_info.pen_usage ){
						palette_increase_usage_count( tile_info.pal_data-Machine->remapped_colortable, tile_info.pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
					}
					else {
						palette_increase_usage_countx( tile_info.pal_data-Machine->remapped_colortable, num_pens, tile_info.pen_data, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
					}

					dirty_pixels[tile_index] = 1;
					dirty_vram[tile_index] = 0;
				}
			}
		}
	}
}

void tilemap_set_scrollx( struct tilemap *tilemap, int which, int value ){
	value = tilemap->scrollx_delta-value;

	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->height-value;
		if( tilemap->colscroll[which]!=value ){
			tilemap->scrolled = 1;
			tilemap->colscroll[which] = value;
		}
	}
	else {
		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->width-value;
		if( tilemap->rowscroll[which]!=value ){
			tilemap->scrolled = 1;
			tilemap->rowscroll[which] = value;
		}
	}
}
void tilemap_set_scrolly( struct tilemap *tilemap, int which, int value ){
	value = tilemap->scrolly_delta - value;

	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->width-value;
		if( tilemap->rowscroll[which]!=value ){
			tilemap->scrolled = 1;
			tilemap->rowscroll[which] = value;
		}
	}
	else {
		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->height-value;
		if( tilemap->colscroll[which]!=value ){
			tilemap->scrolled = 1;
			tilemap->colscroll[which] = value;
		}
	}
}

void tilemap_set_scrolldx( struct tilemap *tilemap, int dx, int dx_if_flipped ){
	tilemap->dx = dx;
	tilemap->dx_if_flipped = dx_if_flipped;
	tilemap->scrollx_delta = ( tilemap->attributes & TILEMAP_FLIPX )?dx_if_flipped:dx;
}

void tilemap_set_scrolldy( struct tilemap *tilemap, int dy, int dy_if_flipped ){
	tilemap->dy = dy;
	tilemap->dy_if_flipped = dy_if_flipped;
	tilemap->scrolly_delta = ( tilemap->attributes & TILEMAP_FLIPY )?dy_if_flipped:dy;
}

#else // DECLARE
/*
	The following procedure body is #included several times by
	tilemap.c to implement a suite of tilemap_draw subroutines.

	The constants TILE_WIDTH and TILE_HEIGHT are different in
	each instance of this code, allowing arithmetic shifts to
	be used by the compiler instead of multiplies/divides.

	This routine should be fairly optimal, for C code, though of
	course there is room for improvement.

	It renders pixels one row at a time, skipping over runs of totally
	transparent tiles, and calling custom blitters to handle runs of
	masked/totally opaque tiles.
*/

DECLARE( draw, (int xpos, int ypos),
{
	int x1 = xpos;
	int y1 = ypos;
	int x2 = xpos+blit.source_width;
	int y2 = ypos+blit.source_height;

	/* clip source coordinates */
	if( x1<blit.clip_left ) x1 = blit.clip_left;
	if( x2>blit.clip_right ) x2 = blit.clip_right;
	if( y1<blit.clip_top ) y1 = blit.clip_top;
	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;

	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
		UINT8 priority = blit.priority;

		DATA_TYPE *dest_baseaddr;
		DATA_TYPE *dest_next;
		const DATA_TYPE *source_baseaddr;
		const DATA_TYPE *source_next;
		const UINT8 *mask_baseaddr;
		const UINT8 *mask_next;

		int c1;
		int c2; /* leftmost and rightmost visible columns in source tilemap */
		int y; /* current screen line to render */
		int y_next;

		dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];

		/* convert screen coordinates to source tilemap coordinates */
		x1 -= xpos;
		y1 -= ypos;
		x2 -= xpos;
		y2 -= ypos;

		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
		mask_baseaddr = blit.bitmask->line[y1];

		c1 = x1/TILE_WIDTH; /* round down */
		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */

		y = y1;
		y_next = TILE_HEIGHT*(y1/TILE_HEIGHT) + TILE_HEIGHT;
		if( y_next>y2 ) y_next = y2;

		{
			int dy = y_next-y;
			dest_next = dest_baseaddr + dy*blit.dest_line_offset;
			source_next = source_baseaddr + dy*blit.source_line_offset;
			mask_next = mask_baseaddr + dy*blit.mask_line_offset;
		}

		for(;;){
			int row = y/TILE_HEIGHT;
			UINT8 *mask_data = blit.mask_data_row[row];
			char *priority_data = blit.priority_data_row[row];

			UINT8 tile_type;
			UINT8 prev_tile_type = TILE_TRANSPARENT;

			int x_start = x1;
			int x_end;

			int column;
			for( column=c1; column<=c2; column++ ){
				if( column==c2 || priority_data[column]!=priority )
					tile_type = TILE_TRANSPARENT;
				else
					tile_type = mask_data[column];

				if( tile_type!=prev_tile_type ){
					x_end = column*TILE_WIDTH;
					if( x_end<x1 ) x_end = x1;
					if( x_end>x2 ) x_end = x2;

					if( prev_tile_type != TILE_TRANSPARENT ){
						if( prev_tile_type == TILE_MASKED ){
							int count = (x_end+7)/8 - x_start/8;
							const UINT8 *mask0 = mask_baseaddr + x_start/8;
							const DATA_TYPE *source0 = source_baseaddr + (x_start&0xfff8);
							DATA_TYPE *dest0 = dest_baseaddr + (x_start&0xfff8);
							int i = y;
							for(;;){
								memcpybitmask( dest0, source0, mask0, count );
								if( ++i == y_next ) break;

								dest0 += blit.dest_line_offset;
								source0 += blit.source_line_offset;
								mask0 += blit.mask_line_offset;
							}
						}
						else { /* TILE_OPAQUE */
							int num_pixels = x_end - x_start;
							DATA_TYPE *dest0 = dest_baseaddr+x_start;
							const DATA_TYPE *source0 = source_baseaddr+x_start;
							int i = y;
							for(;;){
								memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
								if( ++i == y_next ) break;

								dest0 += blit.dest_line_offset;
								source0 += blit.source_line_offset;
							}
						}
					}
					x_start = x_end;
				}

				prev_tile_type = tile_type;
			}

			if( y_next==y2 ) break; /* we are done! */

			dest_baseaddr = dest_next;
			source_baseaddr = source_next;
			mask_baseaddr = mask_next;

			y = y_next;
			y_next += TILE_HEIGHT;

			if( y_next>=y2 ){
				y_next = y2;
			}
			else {
				dest_next += blit.dest_row_offset;
				source_next += blit.source_row_offset;
				mask_next += blit.mask_row_offset;
			}
		} /* process next row */
	} /* not totally clipped */
})

DECLARE( draw_opaque, (int xpos, int ypos),
{
	int x1 = xpos;
	int y1 = ypos;
	int x2 = xpos+blit.source_width;
	int y2 = ypos+blit.source_height;

	/* clip source coordinates */
	if( x1<blit.clip_left ) x1 = blit.clip_left;
	if( x2>blit.clip_right ) x2 = blit.clip_right;
	if( y1<blit.clip_top ) y1 = blit.clip_top;
	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;

	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
		UINT8 priority = blit.priority;

		DATA_TYPE *dest_baseaddr;
		DATA_TYPE *dest_next;
		const DATA_TYPE *source_baseaddr;
		const DATA_TYPE *source_next;

		int c1;
		int c2; /* leftmost and rightmost visible columns in source tilemap */
		int y; /* current screen line to render */
		int y_next;

		dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];

		/* convert screen coordinates to source tilemap coordinates */
		x1 -= xpos;
		y1 -= ypos;
		x2 -= xpos;
		y2 -= ypos;

		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];

		c1 = x1/TILE_WIDTH; /* round down */
		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */

		y = y1;
		y_next = TILE_HEIGHT*(y1/TILE_HEIGHT) + TILE_HEIGHT;
		if( y_next>y2 ) y_next = y2;

		{
			int dy = y_next-y;
			dest_next = dest_baseaddr + dy*blit.dest_line_offset;
			source_next = source_baseaddr + dy*blit.source_line_offset;
		}

		for(;;){
			int row = y/TILE_HEIGHT;
			char *priority_data = blit.priority_data_row[row];

			UINT8 tile_type;
			UINT8 prev_tile_type = TILE_TRANSPARENT;

			int x_start = x1;
			int x_end;

			int column;
			for( column=c1; column<=c2; column++ ){
				if( column==c2 || priority_data[column]!=priority )
					tile_type = TILE_TRANSPARENT;
				else
					tile_type = TILE_OPAQUE;

				if( tile_type!=prev_tile_type ){
					x_end = column*TILE_WIDTH;
					if( x_end<x1 ) x_end = x1;
					if( x_end>x2 ) x_end = x2;

					if( prev_tile_type != TILE_TRANSPARENT ){
						/* TILE_OPAQUE */
						int num_pixels = x_end - x_start;
						DATA_TYPE *dest0 = dest_baseaddr+x_start;
						const DATA_TYPE *source0 = source_baseaddr+x_start;
						int i = y;
						for(;;){
							memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
							if( ++i == y_next ) break;

							dest0 += blit.dest_line_offset;
							source0 += blit.source_line_offset;
						}
					}
					x_start = x_end;
				}

				prev_tile_type = tile_type;
			}

			if( y_next==y2 ) break; /* we are done! */

			dest_baseaddr = dest_next;
			source_baseaddr = source_next;

			y = y_next;
			y_next += TILE_HEIGHT;

			if( y_next>=y2 ){
				y_next = y2;
			}
			else {
				dest_next += blit.dest_row_offset;
				source_next += blit.source_row_offset;
			}
		} /* process next row */
	} /* not totally clipped */
})

DECLARE( mark_visible, (int xpos, int ypos),
{
	int x1 = xpos;
	int y1 = ypos;
	int x2 = xpos+blit.source_width;
	int y2 = ypos+blit.source_height;

	/* clip source coordinates */
	if( x1<blit.clip_left ) x1 = blit.clip_left;
	if( x2>blit.clip_right ) x2 = blit.clip_right;
	if( y1<blit.clip_top ) y1 = blit.clip_top;
	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;

	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
		int c1;
		int c2; /* leftmost and rightmost visible columns in source tilemap */
		int r1;
		int r2;
		char **visible_row;
		int span;
		int row;

		/* convert screen coordinates to source tilemap coordinates */
		x1 -= xpos;
		y1 -= ypos;
		x2 -= xpos;
		y2 -= ypos;

		r1 = y1/TILE_HEIGHT;
		r2 = (y2+TILE_HEIGHT-1)/TILE_HEIGHT;

		c1 = x1/TILE_WIDTH; /* round down */
		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */
		visible_row = blit.visible_row;
		span = c2-c1;

		for( row=r1; row<r2; row++ ){
			memset( visible_row[row]+c1, 1, span );
		}
	}
})

#undef TILE_WIDTH
#undef TILE_HEIGHT
#undef DATA_TYPE
#undef memcpybitmask
#undef DECLARE

#endif /* DECLARE */
