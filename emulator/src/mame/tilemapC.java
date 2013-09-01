package mame;

import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.driverH.*;
import static mame.drawgfxH.*;
import mame.tilemapH.tilemap;
import static arcadeflex.video.*;
import static mame.osdependH.*;

public class tilemapC {
    /*TODO*///#ifndef DECLARE
    /*TODO*///
    /*TODO*///#include "driver.h"
    /*TODO*///#include "tilemap.h"
    /*TODO*///
    static char[] flip_bit_table=new char[0x100]; /* horizontal flip for 8 pixels */
    static tilemap first_tilemap; /* resource tracking */
    static int screen_width, screen_height;
    /*TODO*///struct tile_info tile_info;
    /*TODO*///
    /*TODO*///enum {
    /*TODO*///	TILE_TRANSPARENT,
    /*TODO*///	TILE_MASKED,
    /*TODO*///	TILE_OPAQUE
    /*TODO*///};
    /*TODO*///
    /*TODO*////* the following parameters are constant across tilemap_draw calls */
    /*TODO*///static struct {
    /*TODO*///	int clip_left, clip_top, clip_right, clip_bottom;
    /*TODO*///	int source_width, source_height;
    /*TODO*///
    /*TODO*///	int dest_line_offset,source_line_offset,mask_line_offset;
    /*TODO*///	int dest_row_offset,source_row_offset,mask_row_offset;
    /*TODO*///	struct osd_bitmap *screen, *pixmap, *bitmask;
    /*TODO*///
    /*TODO*///	UINT8 **mask_data_row;
    /*TODO*///	char **priority_data_row, **visible_row;
    /*TODO*///	UINT8 priority;
    /*TODO*///} blit;
    /*TODO*///

    public static int MASKROWBYTES(int W) { return (W + 7) / 8; }
    /*TODO*///
    /*TODO*///static void memcpybitmask8( UINT8 *dest, const UINT8 *source, const UINT8 *bitmask, int count ){
    /*TODO*///	for(;;){
    /*TODO*///		UINT8 data = *bitmask++;
    /*TODO*///		if( data&0x80 ) dest[0] = source[0];
    /*TODO*///		if( data&0x40 ) dest[1] = source[1];
    /*TODO*///		if( data&0x20 ) dest[2] = source[2];
    /*TODO*///		if( data&0x10 ) dest[3] = source[3];
    /*TODO*///		if( data&0x08 ) dest[4] = source[4];
    /*TODO*///		if( data&0x04 ) dest[5] = source[5];
    /*TODO*///		if( data&0x02 ) dest[6] = source[6];
    /*TODO*///		if( data&0x01 ) dest[7] = source[7];
    /*TODO*///		if( --count == 0 ) break;
    /*TODO*///		source+=8;
    /*TODO*///		dest+=8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///static void memcpybitmask16( UINT16 *dest, const UINT16 *source, const UINT8 *bitmask, int count ){
    /*TODO*///	for(;;){
    /*TODO*///		UINT16 data = *bitmask++;
    /*TODO*///		if( data&0x80 ) dest[0] = source[0];
    /*TODO*///		if( data&0x40 ) dest[1] = source[1];
    /*TODO*///		if( data&0x20 ) dest[2] = source[2];
    /*TODO*///		if( data&0x10 ) dest[3] = source[3];
    /*TODO*///		if( data&0x08 ) dest[4] = source[4];
    /*TODO*///		if( data&0x04 ) dest[5] = source[5];
    /*TODO*///		if( data&0x02 ) dest[6] = source[6];
    /*TODO*///		if( data&0x01 ) dest[7] = source[7];
    /*TODO*///		if( --count == 0 ) break;
    /*TODO*///		source+=8;
    /*TODO*///		dest+=8;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	8
    /*TODO*///#define TILE_HEIGHT	8
    /*TODO*///#define DATA_TYPE UINT8
    /*TODO*///#define memcpybitmask memcpybitmask8
    /*TODO*///#define DECLARE(function,args,body) static void function##8x8x8BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	16
    /*TODO*///#define TILE_HEIGHT	16
    /*TODO*///#define DATA_TYPE UINT8
    /*TODO*///#define memcpybitmask memcpybitmask8
    /*TODO*///#define DECLARE(function,args,body) static void function##16x16x8BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	32
    /*TODO*///#define TILE_HEIGHT	32
    /*TODO*///#define DATA_TYPE UINT8
    /*TODO*///#define memcpybitmask memcpybitmask8
    /*TODO*///#define DECLARE(function,args,body) static void function##32x32x8BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	8
    /*TODO*///#define TILE_HEIGHT	8
    /*TODO*///#define DATA_TYPE UINT16
    /*TODO*///#define memcpybitmask memcpybitmask16
    /*TODO*///#define DECLARE(function,args,body) static void function##8x8x16BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	16
    /*TODO*///#define TILE_HEIGHT	16
    /*TODO*///#define DATA_TYPE UINT16
    /*TODO*///#define memcpybitmask memcpybitmask16
    /*TODO*///#define DECLARE(function,args,body) static void function##16x16x16BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*///#define TILE_WIDTH	32
    /*TODO*///#define TILE_HEIGHT	32
    /*TODO*///#define DATA_TYPE UINT16
    /*TODO*///#define memcpybitmask memcpybitmask16
    /*TODO*///#define DECLARE(function,args,body) static void function##32x32x16BPP args body
    /*TODO*///#include "tilemap.c"
    /*TODO*///
    /*TODO*////*********************************************************************************/
    /*TODO*///
    /*TODO*///#define SWAP(X,Y) {int temp=X; X=Y; Y=temp; }
    /*TODO*///
    /*TODO*///void tilemap_set_enable( struct tilemap *tilemap, int enable ){
    /*TODO*///	tilemap->enable = enable;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_set_flip( struct tilemap *tilemap, int attributes ){
    /*TODO*///	if( tilemap==ALL_TILEMAPS ){
    /*TODO*///		tilemap = first_tilemap;
    /*TODO*///		while( tilemap ){
    /*TODO*///			tilemap_set_flip( tilemap, attributes );
    /*TODO*///			tilemap = tilemap->next;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else if( tilemap->attributes!=attributes ){
    /*TODO*///		tilemap->attributes = attributes;
    /*TODO*///
    /*TODO*///		tilemap->orientation = Machine->orientation;
    /*TODO*///
    /*TODO*///		if( attributes&TILEMAP_FLIPY ){
    /*TODO*///			tilemap->orientation ^= ORIENTATION_FLIP_Y;
    /*TODO*///			tilemap->scrolly_delta = tilemap->dy_if_flipped;
    /*TODO*///		}
    /*TODO*///		else {
    /*TODO*///			tilemap->scrolly_delta = tilemap->dy;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if( attributes&TILEMAP_FLIPX ){
    /*TODO*///			tilemap->orientation ^= ORIENTATION_FLIP_X;
    /*TODO*///			tilemap->scrollx_delta = tilemap->dx_if_flipped;
    /*TODO*///		}
    /*TODO*///		else {
    /*TODO*///			tilemap->scrollx_delta = tilemap->dx;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		tilemap_mark_all_tiles_dirty( tilemap );
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    static osd_bitmap create_tmpbitmap( int width, int height ){
    	if(( Machine.orientation&ORIENTATION_SWAP_XY )!=0)
        {
            //SWAP(width,height);
            int temp=width; width=height; height=temp;
        }
    	return osd_new_bitmap( width,height, Machine.scrbitmap.depth );
    }
    
    static osd_bitmap create_bitmask( int width, int height )
    {
    	if(( Machine.orientation&ORIENTATION_SWAP_XY )!=0)
        {
            //SWAP(width,height);
            int temp=width; width=height; height=temp;
        }
    	return osd_new_bitmap( width,height, 8 );
    }
    
    public static void tilemap_set_clip(tilemap _tilemap, rectangle clip ){
    	int left,top,right,bottom;
    
    	if (clip!=null){
    		left = clip.min_x;
    		top = clip.min_y;
    		right = clip.max_x+1;
    		bottom = clip.max_y+1;
    
    		if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0){  
                        int temp=left; left=top; top=temp;                   
    			//SWAP(left,top)
                        int temp2=right; right=bottom; bottom=temp2;
    			//SWAP(right,bottom)
    		}
    		if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0){
    			//SWAP(left,right)
                        int temp=left; left=right; right=temp;
    			left = screen_width-left;
    			right = screen_width-right;
    		}
    		if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0){
    			//SWAP(top,bottom)
                        int temp=top; top=bottom; bottom=temp;
    			top = screen_height-top;
    			bottom = screen_height-bottom;
    		}
    	}
    	else
    	{
    		left = 0;
    		top = 0;
    		right = _tilemap.width;
    		bottom = _tilemap.height;
    	}
    
    	_tilemap.clip_left = left;
    	_tilemap.clip_right = right;
    	_tilemap.clip_top = top;
    	_tilemap.clip_bottom = bottom;
    	if( errorlog!=null ) fprintf( errorlog, "clip: %d,%d,%d,%d\n", left,top,right,bottom );   
    }
    
    public static void tilemap_init()
    {
                
    	int value, data, bit;
    	for( value=0; value<0x100; value++ ){
    		data = 0;
    		for( bit=0; bit<8; bit++ ) if( ((value>>bit)&1 )!=0) data |= 0x80>>bit;
    		flip_bit_table[value] = (char)(data & 0xFF);//unsigned byte value
    	}
    	screen_width = Machine.scrbitmap.width;
    	screen_height = Machine.scrbitmap.height;
    	first_tilemap = null;
    }
    
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///static void dispose_tile_info( struct tilemap *tilemap ){
    /*TODO*///	free( tilemap->pendata );
    /*TODO*///	free( tilemap->maskdata );
    /*TODO*///	free( tilemap->paldata );
    /*TODO*///	free( tilemap->pen_usage );
    /*TODO*///	free( tilemap->priority );
    /*TODO*///	free( tilemap->visible );
    /*TODO*///	free( tilemap->dirty_vram );
    /*TODO*///	free( tilemap->dirty_pixels );
    /*TODO*///	free( tilemap->flags );
    /*TODO*///	free( tilemap->priority_row );
    /*TODO*///	free( tilemap->visible_row );
    /*TODO*///}
    /*TODO*///
    /*TODO*///static int create_tile_info( struct tilemap *tilemap ){
    /*TODO*///	int num_tiles = tilemap->num_tiles;
    /*TODO*///	int num_cols = tilemap->num_cols;
    /*TODO*///	int num_rows = tilemap->num_rows;
    /*TODO*///
    /*TODO*///	tilemap->pendata = malloc( sizeof( UINT8 *)*num_tiles );
    /*TODO*///	tilemap->maskdata = malloc( sizeof( UINT8 *)*num_tiles ); /* needed only for TILEMAP_BITMASK */
    /*TODO*///	tilemap->paldata = malloc( sizeof( unsigned short *)*num_tiles );
    /*TODO*///	tilemap->pen_usage = malloc( sizeof( unsigned int )*num_tiles );
    /*TODO*///	tilemap->priority = malloc( num_tiles );
    /*TODO*///	tilemap->visible = malloc( num_tiles );
    /*TODO*///	tilemap->dirty_vram = malloc( num_tiles );
    /*TODO*///	tilemap->dirty_pixels = malloc( num_tiles );
    /*TODO*///	tilemap->flags = malloc( num_tiles );
    /*TODO*///	tilemap->rowscroll = (int *)calloc(tilemap->height,sizeof(int));
    /*TODO*///	tilemap->colscroll = (int *)calloc(tilemap->width,sizeof(int));
    /*TODO*///
    /*TODO*///	tilemap->priority_row = malloc( sizeof(char *)*num_rows );
    /*TODO*///	tilemap->visible_row = malloc( sizeof(char *)*num_rows );
    /*TODO*///
    /*TODO*///	if( tilemap->pendata &&
    /*TODO*///		tilemap->maskdata &&
    /*TODO*///		tilemap->paldata && tilemap->pen_usage &&
    /*TODO*///		tilemap->priority && tilemap->visible &&
    /*TODO*///		tilemap->dirty_vram && tilemap->dirty_pixels &&
    /*TODO*///		tilemap->flags &&
    /*TODO*///		tilemap->rowscroll && tilemap->colscroll &&
    /*TODO*///		tilemap->priority_row && tilemap->visible_row )
    /*TODO*///	{
    /*TODO*///		int tile_index,row;
    /*TODO*///
    /*TODO*///		for( row=0; row<num_rows; row++ ){
    /*TODO*///			tilemap->priority_row[row] = tilemap->priority+num_cols*row;
    /*TODO*///			tilemap->visible_row[row] = tilemap->visible+num_cols*row;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		for( tile_index=0; tile_index<num_tiles; tile_index++ ){
    /*TODO*///			tilemap->paldata[tile_index] = 0;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		memset( tilemap->priority, 0, num_tiles );
    /*TODO*///		memset( tilemap->visible, 0, num_tiles );
    /*TODO*///		memset( tilemap->dirty_vram, 1, num_tiles );
    /*TODO*///		memset( tilemap->dirty_pixels, 1, num_tiles );
    /*TODO*///
    /*TODO*///		return 1; /* done */
    /*TODO*///	}
    /*TODO*///	dispose_tile_info( tilemap );
    /*TODO*///	return 0; /* error */
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_set_scroll_cols( struct tilemap *tilemap, int n ){
    /*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
    /*TODO*///		if (tilemap->scroll_rows != n){
    /*TODO*///			tilemap->scroll_rows = n;
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		if (tilemap->scroll_cols != n){
    /*TODO*///			tilemap->scroll_cols = n;
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    public static void tilemap_set_scroll_rows( tilemap _tilemap, int n )
    {
    	if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0)
        {
    		if (_tilemap.scroll_cols != n){
    			_tilemap.scroll_cols = n;
    			_tilemap.scrolled = 1;
    		}
    	}
    	else
    	{
    		if (_tilemap.scroll_rows != n){
    			_tilemap.scroll_rows = n;
    			_tilemap.scrolled = 1;
    		}
    	}
    }
    
    public static int create_pixmap( tilemap _tilemap ){
    	_tilemap.pixmap = create_tmpbitmap( _tilemap.width, _tilemap.height );
    	if( _tilemap.pixmap!=null ){
    		_tilemap.pixmap_line_offset = _tilemap.pixmap.line[1].read() - _tilemap.pixmap.line[0].read();
    		return 1; /* done */
    	}
    	return 0; /* error */
    }
    /*TODO*///
    /*TODO*///static void dispose_pixmap( struct tilemap *tilemap ){
    /*TODO*///	osd_free_bitmap( tilemap->pixmap );
    /*TODO*///	free( tilemap->colscroll );
    /*TODO*///	free( tilemap->rowscroll );
    /*TODO*///}
    /*TODO*///
    /*TODO*///static UINT8 **new_mask_data_table( UINT8 *mask_data, int num_cols, int num_rows ){
    /*TODO*///	UINT8 **mask_data_row = malloc(num_rows * sizeof(UINT8 *));
    /*TODO*///	if( mask_data_row ){
    /*TODO*///		int row;
    /*TODO*///		for( row = 0; row<num_rows; row++ ) mask_data_row[row] = mask_data + num_cols*row;
    /*TODO*///	}
    /*TODO*///	return mask_data_row;
    /*TODO*///}
    /*TODO*///
    static UBytePtr[] new_mask_data_table(char[] mask_data, int num_cols, int num_rows)
    {
            UBytePtr[] mask_data_row = new UBytePtr[num_rows];
            if (mask_data_row != null)
            {
                for (int row = 0; row < num_rows; row++) 
                    mask_data_row[row] = new UBytePtr(mask_data, num_cols * row);
            }
            return mask_data_row;
    }

    public static int create_fg_mask(tilemap _tilemap ){
    	//if( tilemap->type == TILEMAP_OPAQUE ) return 1;
    
    	_tilemap.fg_mask_data =new char[_tilemap.num_tiles];
    	if( _tilemap.fg_mask_data!=null )
        {
    		_tilemap.fg_mask_data_row = new_mask_data_table( _tilemap.fg_mask_data, _tilemap.num_cols, _tilemap.num_rows );
    		if( _tilemap.fg_mask_data_row!=null ){
    			_tilemap.fg_mask = create_bitmask( MASKROWBYTES(_tilemap.width), _tilemap.height );
    			if( _tilemap.fg_mask!=null ){
    				_tilemap.fg_mask_line_offset = _tilemap.fg_mask.line[1].read() - _tilemap.fg_mask.line[0].read();
    				return 1; /* done */
    			}
    			 _tilemap.fg_mask_data_row=null;
    		}
    		 _tilemap.fg_mask_data=null;
    	}
    	return 0; /* error */
    }
    /*TODO*///
    /*TODO*///static int create_bg_mask( struct tilemap *tilemap ){
    /*TODO*///	if( (tilemap->type & TILEMAP_SPLIT)==0 ) return 1;
    /*TODO*///
    /*TODO*///	tilemap->bg_mask_data = malloc( tilemap->num_tiles );
    /*TODO*///	if( tilemap->bg_mask_data ){
    /*TODO*///		tilemap->bg_mask_data_row = new_mask_data_table( tilemap->bg_mask_data, tilemap->num_cols, tilemap->num_rows );
    /*TODO*///		if( tilemap->bg_mask_data_row ){
    /*TODO*///			tilemap->bg_mask = create_bitmask( MASKROWBYTES(tilemap->width), tilemap->height );
    /*TODO*///			if( tilemap->bg_mask ){
    /*TODO*///				tilemap->bg_mask_line_offset = tilemap->bg_mask->line[1] - tilemap->bg_mask->line[0];
    /*TODO*///				return 1; /* done */
    /*TODO*///			}
    /*TODO*///			free( tilemap->bg_mask_data_row );
    /*TODO*///		}
    /*TODO*///		free( tilemap->bg_mask_data );
    /*TODO*///	}
    /*TODO*///	return 0; /* error */
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void dispose_fg_mask( struct tilemap *tilemap ){
    /*TODO*///	free( tilemap->fg_mask_data_row );
    /*TODO*///	free( tilemap->fg_mask_data );
    /*TODO*///	osd_free_bitmap( tilemap->fg_mask );
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void dispose_bg_mask( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap->type & TILEMAP_SPLIT ){
    /*TODO*///		osd_free_bitmap( tilemap->bg_mask );
    /*TODO*///		free( tilemap->bg_mask_data_row );
    /*TODO*///		free( tilemap->bg_mask_data );
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    public static tilemap tilemap_create(WriteHandlerPtr tile_get_info,
    		/*void (*tile_get_info)( int col, int row ),*/
    		int type,
    		int tile_width, int tile_height,
    		int num_cols, int num_rows )
    {
    	
        tilemap _tilemap = new tilemap();//struct tilemap *tilemap = (struct tilemap *)calloc( 1,sizeof( struct tilemap ) );
    	if( _tilemap!=null )
        {
    		//memset( tilemap, 0, sizeof( struct tilemap ) );
    
    		_tilemap.orientation = Machine.orientation;
    		if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0)
                {
    			//SWAP( tile_width, tile_height )
                        int temp=tile_width; tile_width=tile_height; tile_height=temp;
    			//SWAP( num_cols,num_rows )
                        int temp2 = num_cols; num_cols=num_rows; num_rows=temp2;
    		}
    
    		if( errorlog!=null ){
    			fprintf( errorlog, "cached tilemap info:\n" );
    			fprintf( errorlog, "tilewidth,tileheight:%d,%d\n",tile_width,tile_height );
    			fprintf( errorlog, "cols,rows:%d,%d\n",num_cols,num_rows );
    		}
    
    		_tilemap.tile_get_info = tile_get_info;
    		_tilemap.enable = 1;
    		tilemap_set_clip( _tilemap, Machine.drv.visible_area );
    
    		if( Machine.scrbitmap.depth==16 ){
                    throw new UnsupportedOperationException("tilemap_init() 16BIT unimplemented");
                
    /*TODO*///			if( tile_width==8 && tile_height==8 ){
    /*TODO*///				tilemap->mark_visible = mark_visible8x8x16BPP;
    /*TODO*///				tilemap->draw = draw8x8x16BPP;
    /*TODO*///				tilemap->draw_opaque = draw_opaque8x8x16BPP;
    /*TODO*///			}
    /*TODO*///			else if( tile_width==16 && tile_height==16 ){
    /*TODO*///				tilemap->mark_visible = mark_visible16x16x16BPP;
    /*TODO*///				tilemap->draw = draw16x16x16BPP;
    /*TODO*///				tilemap->draw_opaque = draw_opaque16x16x16BPP;
    /*TODO*///			}
    /*TODO*///			else if( tile_width==32 && tile_height==32 ){
    /*TODO*///				tilemap->mark_visible = mark_visible32x32x16BPP;
    /*TODO*///				tilemap->draw = draw32x32x16BPP;
    /*TODO*///				tilemap->draw_opaque = draw_opaque32x32x16BPP;
    /*TODO*///			}
    		}
    		else {
    			if( tile_width==8 && tile_height==8 ){
       			    _tilemap.mark_visible = mark_visible8x8x8BPP;
    		            _tilemap.draw = draw8x8x8BPP;
    		            _tilemap.draw_opaque = draw_opaque8x8x8BPP;
    			}
    			else if( tile_width==16 && tile_height==16 ){
                            throw new UnsupportedOperationException("tilemap_init() tile_width==16 && tile_height==16 unimplemented");
    /*TODO*///				tilemap->mark_visible = mark_visible16x16x8BPP;
    /*TODO*///				tilemap->draw = draw16x16x8BPP;
    /*TODO*///				tilemap->draw_opaque = draw_opaque16x16x8BPP;
    			}
    			else if( tile_width==32 && tile_height==32 ){
                            throw new UnsupportedOperationException("tilemap_init() tile_width==32 && tile_height==32 unimplemented");
    /*TODO*///				tilemap->mark_visible = mark_visible32x32x8BPP;
    /*TODO*///				tilemap->draw = draw32x32x8BPP;
    /*TODO*///				tilemap->draw_opaque = draw_opaque32x32x8BPP;
    			}
    		}
    
    		if( _tilemap.mark_visible!=null && _tilemap.draw!=null ){
    			_tilemap.type = type;
    
    			_tilemap.tile_width = tile_width;
    			_tilemap.tile_height = tile_height;
    			_tilemap.width = tile_width*num_cols;
    			_tilemap.height = tile_height*num_rows;
    
    			_tilemap.num_rows = num_rows;
    			_tilemap.num_cols = num_cols;
    			_tilemap.num_tiles = num_cols*num_rows;
    
    			_tilemap.scroll_rows = 1;
    			_tilemap.scroll_cols = 1;
    			_tilemap.scrolled = 1;
    
    			_tilemap.transparent_pen = -1; /* default (this is supplied by video driver) */
    
    			if( create_pixmap( _tilemap )!=0 ){
    				if( create_fg_mask( _tilemap )!=0 ){
    /*TODO*///					if( create_bg_mask( tilemap ) ){
    /*TODO*///						if( create_tile_info( tilemap ) ){
    /*TODO*///							tilemap->next = first_tilemap;
    /*TODO*///							first_tilemap = tilemap;
    /*TODO*///							return tilemap;
    /*TODO*///						}
    /*TODO*///						dispose_bg_mask( tilemap );
    /*TODO*///					}
    /*TODO*///					dispose_fg_mask( tilemap );
    				}
    /*TODO*///				dispose_pixmap( tilemap );
    			}
    		}
    		_tilemap=null;
    	}
    	return null; /* error */
    }
    /*TODO*///
    /*TODO*///void tilemap_dispose( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap==first_tilemap ){
    /*TODO*///		first_tilemap = tilemap->next;
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		struct tilemap *prev = first_tilemap;
    /*TODO*///		while( prev->next != tilemap ) prev = prev->next;
    /*TODO*///		prev->next =tilemap->next;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	dispose_tile_info( tilemap );
    /*TODO*///	dispose_bg_mask( tilemap );
    /*TODO*///	dispose_fg_mask( tilemap );
    /*TODO*///	dispose_pixmap( tilemap );
    /*TODO*///	free( tilemap );
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_close( void ){
    /*TODO*///	while( first_tilemap ){
    /*TODO*///		struct tilemap *next = first_tilemap->next;
    /*TODO*///		tilemap_dispose( first_tilemap );
    /*TODO*///		first_tilemap = next;
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///void tilemap_mark_tile_dirty( struct tilemap *tilemap, int col, int row ){
    /*TODO*///	/* convert logical coordinates to cached coordinates */
    /*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY ) SWAP(col,row)
    /*TODO*///	if( tilemap->orientation & ORIENTATION_FLIP_X ) col = tilemap->num_cols-1-col;
    /*TODO*///	if( tilemap->orientation & ORIENTATION_FLIP_Y ) row = tilemap->num_rows-1-row;
    /*TODO*///
    /*TODO*/////	tilemap->dirty_vram_row[row][col] = 1;
    /*TODO*///	tilemap->dirty_vram[row*tilemap->num_cols + col] = 1;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_mark_all_tiles_dirty( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap==ALL_TILEMAPS ){
    /*TODO*///		tilemap = first_tilemap;
    /*TODO*///		while( tilemap ){
    /*TODO*///			tilemap_mark_all_tiles_dirty( tilemap );
    /*TODO*///			tilemap = tilemap->next;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		memset( tilemap->dirty_vram, 1, tilemap->num_tiles );
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_mark_all_pixels_dirty( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap==ALL_TILEMAPS ){
    /*TODO*///		tilemap = first_tilemap;
    /*TODO*///		while( tilemap ){
    /*TODO*///			tilemap_mark_all_pixels_dirty( tilemap );
    /*TODO*///			tilemap = tilemap->next;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		/* let's invalidate all offscreen tiles, decreasing the refcounts */
    /*TODO*///		int tile_index;
    /*TODO*///		int num_pens = tilemap->tile_width*tilemap->tile_height; /* precalc - needed for >4bpp pen management handling */
    /*TODO*///		for( tile_index=0; tile_index<tilemap->num_tiles; tile_index++ ){
    /*TODO*///			if( !tilemap->visible[tile_index] ){
    /*TODO*///				unsigned short *the_color = tilemap->paldata[tile_index];
    /*TODO*///				if( the_color ){
    /*TODO*///					unsigned int old_pen_usage = tilemap->pen_usage[tile_index];
    /*TODO*///					if( old_pen_usage ){
    /*TODO*///						palette_decrease_usage_count( the_color-Machine->remapped_colortable, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						palette_decrease_usage_countx( the_color-Machine->remapped_colortable, num_pens, tilemap->pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///					}
    /*TODO*///					tilemap->paldata[tile_index] = NULL;
    /*TODO*///				}
    /*TODO*///				tilemap->dirty_vram[tile_index] = 1;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		memset( tilemap->dirty_pixels, 1, tilemap->num_tiles );
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///static void draw_tile(
    /*TODO*///		struct osd_bitmap *pixmap,
    /*TODO*///		int col, int row, int tile_width, int tile_height,
    /*TODO*///		const UINT8 *pendata, const unsigned short *paldata,
    /*TODO*///		UINT8 flags )
    /*TODO*///{
    /*TODO*///	int x, sx = tile_width*col;
    /*TODO*///	int sy,y1,y2,dy;
    /*TODO*///
    /*TODO*///	if( Machine->scrbitmap->depth==16 ){
    /*TODO*///		if( flags&TILE_FLIPY ){
    /*TODO*///			y1 = tile_height*row+tile_height-1;
    /*TODO*///			y2 = y1-tile_height;
    /*TODO*///	 		dy = -1;
    /*TODO*///	 	}
    /*TODO*///	 	else {
    /*TODO*///			y1 = tile_height*row;
    /*TODO*///			y2 = y1+tile_height;
    /*TODO*///	 		dy = 1;
    /*TODO*///	 	}
    /*TODO*///
    /*TODO*///		if( flags&TILE_FLIPX ){
    /*TODO*///			tile_width--;
    /*TODO*///			for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///				UINT16 *dest  = sx + (UINT16 *)pixmap->line[sy];
    /*TODO*///				for( x=tile_width; x>=0; x-- ) dest[x] = paldata[*pendata++];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else {
    /*TODO*///			for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///				UINT16 *dest  = sx + (UINT16 *)pixmap->line[sy];
    /*TODO*///				for( x=0; x<tile_width; x++ ) dest[x] = paldata[*pendata++];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		if( flags&TILE_FLIPY ){
    /*TODO*///			y1 = tile_height*row+tile_height-1;
    /*TODO*///			y2 = y1-tile_height;
    /*TODO*///	 		dy = -1;
    /*TODO*///	 	}
    /*TODO*///	 	else {
    /*TODO*///			y1 = tile_height*row;
    /*TODO*///			y2 = y1+tile_height;
    /*TODO*///	 		dy = 1;
    /*TODO*///	 	}
    /*TODO*///
    /*TODO*///		if( flags&TILE_FLIPX ){
    /*TODO*///			tile_width--;
    /*TODO*///			for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///				UINT8 *dest  = sx + (UINT8 *)pixmap->line[sy];
    /*TODO*///				for( x=tile_width; x>=0; x-- ) dest[x] = paldata[*pendata++];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else {
    /*TODO*///			for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///				UINT8 *dest  = sx + (UINT8 *)pixmap->line[sy];
    /*TODO*///				for( x=0; x<tile_width; x++ ) dest[x] = paldata[*pendata++];
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void draw_mask(
    /*TODO*///		struct osd_bitmap *mask,
    /*TODO*///		int col, int row, int tile_width, int tile_height,
    /*TODO*///		const UINT8 *pendata, unsigned int transmask,
    /*TODO*///		UINT8 flags )
    /*TODO*///{
    /*TODO*///	int x,bit,sx = tile_width*col;
    /*TODO*///	int sy,y1,y2,dy;
    /*TODO*///
    /*TODO*///	if( flags&TILE_FLIPY ){
    /*TODO*///		y1 = tile_height*row+tile_height-1;
    /*TODO*///		y2 = y1-tile_height;
    /*TODO*/// 		dy = -1;
    /*TODO*/// 	}
    /*TODO*/// 	else {
    /*TODO*///		y1 = tile_height*row;
    /*TODO*///		y2 = y1+tile_height;
    /*TODO*/// 		dy = 1;
    /*TODO*/// 	}
    /*TODO*///
    /*TODO*///	if( flags&TILE_FLIPX ){
    /*TODO*///		tile_width--;
    /*TODO*///		for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///			UINT8 *mask_dest  = mask->line[sy]+sx/8;
    /*TODO*///			for( x=tile_width/8; x>=0; x-- ){
    /*TODO*///				UINT8 data = 0;
    /*TODO*///				for( bit=0; bit<8; bit++ ){
    /*TODO*///					UINT8 p = *pendata++;
    /*TODO*///					data = (data>>1)|(((1<<p)&transmask)?0x00:0x80);
    /*TODO*///				}
    /*TODO*///				mask_dest[x] = data;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///			UINT8 *mask_dest  = mask->line[sy]+sx/8;
    /*TODO*///			for( x=0; x<tile_width/8; x++ ){
    /*TODO*///				UINT8 data = 0;
    /*TODO*///				for( bit=0; bit<8; bit++ ){
    /*TODO*///					UINT8 p = *pendata++;
    /*TODO*///					data = (data<<1)|(((1<<p)&transmask)?0x00:0x01);
    /*TODO*///				}
    /*TODO*///				mask_dest[x] = data;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///static int draw_bitmask(
    /*TODO*///		struct osd_bitmap *mask,
    /*TODO*///		int col, int row, int tile_width, int tile_height,
    /*TODO*///		const UINT8 *maskdata,
    /*TODO*///		UINT8 flags )
    /*TODO*///{
    /*TODO*///	int is_opaque = 1, is_transparent = 1;
    /*TODO*///
    /*TODO*///	int x,sx = tile_width*col;
    /*TODO*///	int sy,y1,y2,dy;
    /*TODO*///
    /*TODO*///	if(maskdata==TILEMAP_BITMASK_TRANSPARENT)  return TILE_TRANSPARENT;
    /*TODO*///	if(maskdata==TILEMAP_BITMAK_OPAQUE) return TILE_OPAQUE;
    /*TODO*///
    /*TODO*///	if( flags&TILE_FLIPY ){
    /*TODO*///		y1 = tile_height*row+tile_height-1;
    /*TODO*///		y2 = y1-tile_height;
    /*TODO*/// 		dy = -1;
    /*TODO*/// 	}
    /*TODO*/// 	else {
    /*TODO*///		y1 = tile_height*row;
    /*TODO*///		y2 = y1+tile_height;
    /*TODO*/// 		dy = 1;
    /*TODO*/// 	}
    /*TODO*///
    /*TODO*///	if( flags&TILE_FLIPX ){
    /*TODO*///		tile_width--;
    /*TODO*///		for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///			UINT8 *mask_dest  = mask->line[sy]+sx/8;
    /*TODO*///			for( x=tile_width/8; x>=0; x-- ){
    /*TODO*///				UINT8 data = flip_bit_table[*maskdata++]; //flip_bit_table unsigned
    /*TODO*///				if( data!=0x00 ) is_transparent = 0;
    /*TODO*///				if( data!=0xff ) is_opaque = 0;
    /*TODO*///				mask_dest[x] = data;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		for( sy=y1; sy!=y2; sy+=dy ){
    /*TODO*///			UINT8 *mask_dest  = mask->line[sy]+sx/8;
    /*TODO*///			for( x=0; x<tile_width/8; x++ ){
    /*TODO*///				UINT8 data = *maskdata++;
    /*TODO*///				if( data!=0x00 ) is_transparent = 0;
    /*TODO*///				if( data!=0xff ) is_opaque = 0;
    /*TODO*///				mask_dest[x] = data;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///
    /*TODO*///	if( is_transparent ) return TILE_TRANSPARENT;
    /*TODO*///	if( is_opaque ) return TILE_OPAQUE;
    /*TODO*///	return TILE_MASKED;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_render( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap==ALL_TILEMAPS ){
    /*TODO*///		tilemap = first_tilemap;
    /*TODO*///		while( tilemap ){
    /*TODO*///			tilemap_render( tilemap );
    /*TODO*///			tilemap = tilemap->next;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else if( tilemap->enable ){
    /*TODO*///		int type = tilemap->type;
    /*TODO*///		int transparent_pen = tilemap->transparent_pen;
    /*TODO*///		unsigned int *transmask = tilemap->transmask;
    /*TODO*///
    /*TODO*///		int tile_width = tilemap->tile_width;
    /*TODO*///		int tile_height = tilemap->tile_height;
    /*TODO*///
    /*TODO*///		char *dirty_pixels = tilemap->dirty_pixels;
    /*TODO*///		char *visible = tilemap->visible;
    /*TODO*///		volatile int tile_index = 0; // LBO - CWPro4 bug workaround
    /*TODO*///		int row,col;
    /*TODO*///
    /*TODO*///		for( row=0; row<tilemap->num_rows; row++ ){
    /*TODO*///			for( col=0; col<tilemap->num_cols; col++ ){
    /*TODO*///				if( dirty_pixels[tile_index] && visible[tile_index] ){
    /*TODO*///					unsigned int pen_usage = tilemap->pen_usage[tile_index];
    /*TODO*///					const UINT8 *pendata = tilemap->pendata[tile_index];
    /*TODO*///					UINT8 flags = tilemap->flags[tile_index];
    /*TODO*///
    /*TODO*///					draw_tile(
    /*TODO*///						tilemap->pixmap,
    /*TODO*///						col, row, tile_width, tile_height,
    /*TODO*///						pendata,
    /*TODO*///						tilemap->paldata[tile_index],
    /*TODO*///						flags );
    /*TODO*///					if( type & TILEMAP_BITMASK ){
    /*TODO*///						tilemap->fg_mask_data_row[row][col] =
    /*TODO*///							draw_bitmask( tilemap->fg_mask,
    /*TODO*///								col, row, tile_width, tile_height,
    /*TODO*///								tilemap->maskdata[tile_index], flags );
    /*TODO*///					}
    /*TODO*///					else if( type & TILEMAP_SPLIT ){
    /*TODO*///						int pen_mask = (transparent_pen<0)?0:(1<<transparent_pen);
    /*TODO*///
    /*TODO*///						if( flags&TILE_IGNORE_TRANSPARENCY ){
    /*TODO*///							tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///							tilemap->bg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///						}
    /*TODO*///						else if( pen_mask == pen_usage ){ /* totally transparent */
    /*TODO*///							tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///							tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///						}
    /*TODO*///						else {
    /*TODO*///							unsigned int fg_transmask = transmask[(flags>>2)&3];
    /*TODO*///							unsigned int bg_transmask = (~fg_transmask)|pen_mask;
    /*TODO*///							if( (pen_usage & fg_transmask)==0 ){ /* foreground totally opaque */
    /*TODO*///								tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///								tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///							}
    /*TODO*///							else if( (pen_usage & bg_transmask)==0 ){ /* background totally opaque */
    /*TODO*///								tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///								tilemap->bg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///							}
    /*TODO*///							else if( (pen_usage & ~bg_transmask)==0 ){ /* background transparent */
    /*TODO*///								draw_mask( tilemap->fg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, fg_transmask, flags );
    /*TODO*///								tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///								tilemap->bg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///							}
    /*TODO*///							else if( (pen_usage & ~fg_transmask)==0 ){ /* foreground transparent */
    /*TODO*///								draw_mask( tilemap->bg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, bg_transmask, flags );
    /*TODO*///								tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///								tilemap->bg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///							}
    /*TODO*///							else { /* split tile - opacity in both foreground and background */
    /*TODO*///								draw_mask( tilemap->fg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, fg_transmask, flags );
    /*TODO*///								draw_mask( tilemap->bg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, bg_transmask, flags );
    /*TODO*///								tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///								tilemap->bg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///				 	}
    /*TODO*///				 	else if( type==TILEMAP_TRANSPARENT ){
    /*TODO*///				 		unsigned int fg_transmask = 1 << transparent_pen;
    /*TODO*///				 	 	if( flags&TILE_IGNORE_TRANSPARENCY ) fg_transmask = 0;
    /*TODO*///
    /*TODO*///						if( pen_usage == fg_transmask ){
    /*TODO*///							tilemap->fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///						}
    /*TODO*///						else if( pen_usage & fg_transmask ){
    /*TODO*///							draw_mask( tilemap->fg_mask,
    /*TODO*///								col, row, tile_width, tile_height,
    /*TODO*///								pendata, fg_transmask, flags );
    /*TODO*///							tilemap->fg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///						}
    /*TODO*///						else {
    /*TODO*///							tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						tilemap->fg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///				 	}
    /*TODO*///
    /*TODO*///					dirty_pixels[tile_index] = 0;
    /*TODO*///				}
    /*TODO*///				tile_index++;
    /*TODO*///			} /* next col */
    /*TODO*///		} /* next row */
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***********************************************************************************/
    /*TODO*///
    /*TODO*///void tilemap_draw( struct osd_bitmap *dest, struct tilemap *tilemap, int priority ){
    /*TODO*///	int xpos,ypos;
    /*TODO*///
    /*TODO*///	if( tilemap->enable ){
    /*TODO*///		void (*draw)( int, int );
    /*TODO*///
    /*TODO*///		int rows = tilemap->scroll_rows;
    /*TODO*///		const int *rowscroll = tilemap->rowscroll;
    /*TODO*///		int cols = tilemap->scroll_cols;
    /*TODO*///		const int *colscroll = tilemap->colscroll;
    /*TODO*///
    /*TODO*///		int left = tilemap->clip_left;
    /*TODO*///		int right = tilemap->clip_right;
    /*TODO*///		int top = tilemap->clip_top;
    /*TODO*///		int bottom = tilemap->clip_bottom;
    /*TODO*///
    /*TODO*///		int tile_height = tilemap->tile_height;
    /*TODO*///
    /*TODO*///		blit.screen = dest;
    /*TODO*///		blit.dest_line_offset = dest->line[1] - dest->line[0];
    /*TODO*///
    /*TODO*///		blit.pixmap = tilemap->pixmap;
    /*TODO*///		blit.source_line_offset = tilemap->pixmap_line_offset;
    /*TODO*///
    /*TODO*///		if( tilemap->type==TILEMAP_OPAQUE || (priority&TILEMAP_IGNORE_TRANSPARENCY) ){
    /*TODO*///			draw = tilemap->draw_opaque;
    /*TODO*///		}
    /*TODO*///		else {
    /*TODO*///			draw = tilemap->draw;
    /*TODO*///
    /*TODO*///			if( priority&TILEMAP_BACK ){
    /*TODO*///				blit.bitmask = tilemap->bg_mask;
    /*TODO*///				blit.mask_line_offset = tilemap->bg_mask_line_offset;
    /*TODO*///				blit.mask_data_row = tilemap->bg_mask_data_row;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				blit.bitmask = tilemap->fg_mask;
    /*TODO*///				blit.mask_line_offset = tilemap->fg_mask_line_offset;
    /*TODO*///				blit.mask_data_row = tilemap->fg_mask_data_row;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			blit.mask_row_offset = tile_height*blit.mask_line_offset;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		if( dest->depth==16 ){
    /*TODO*///			blit.dest_line_offset /= 2;
    /*TODO*///			blit.source_line_offset /= 2;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		blit.source_row_offset = tile_height*blit.source_line_offset;
    /*TODO*///		blit.dest_row_offset = tile_height*blit.dest_line_offset;
    /*TODO*///
    /*TODO*///		blit.priority_data_row = tilemap->priority_row;
    /*TODO*///		blit.source_width = tilemap->width;
    /*TODO*///		blit.source_height = tilemap->height;
    /*TODO*///		blit.priority = priority&0xf;
    /*TODO*///
    /*TODO*///		if( rows == 0 && cols == 0 ){ /* no scrolling */
    /*TODO*///	 		blit.clip_left = left;
    /*TODO*///	 		blit.clip_top = top;
    /*TODO*///	 		blit.clip_right = right;
    /*TODO*///	 		blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///			draw( 0,0 );
    /*TODO*///		}
    /*TODO*///		else if( rows == 0 ){ /* scrolling columns */
    /*TODO*///			int col = 0;
    /*TODO*///			int colwidth = blit.source_width / cols;
    /*TODO*///
    /*TODO*///			blit.clip_top = top;
    /*TODO*///			blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///			while( col < cols ){
    /*TODO*///				int cons = 1;
    /*TODO*///				int scrolly = colscroll[col];
    /*TODO*///
    /*TODO*///	 			/* count consecutive columns scrolled by the same amount */
    /*TODO*///				if( scrolly != TILE_LINE_DISABLED ){
    /*TODO*///					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;
    /*TODO*///
    /*TODO*///					if (scrolly < 0){
    /*TODO*///						scrolly = blit.source_height - (-scrolly) % blit.source_height;
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						scrolly %= blit.source_height;
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_left = col * colwidth;
    /*TODO*///					if( blit.clip_left < left ) blit.clip_left = left;
    /*TODO*///					blit.clip_right = (col + cons) * colwidth;
    /*TODO*///					if( blit.clip_right > right ) blit.clip_right = right;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						ypos = scrolly - blit.source_height;
    /*TODO*///						ypos < blit.clip_bottom;
    /*TODO*///						ypos += blit.source_height )
    /*TODO*///					{
    /*TODO*///						draw( 0,ypos );
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///				col += cons;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else if( cols == 0 ){ /* scrolling rows */
    /*TODO*///			int row = 0;
    /*TODO*///			int rowheight = blit.source_height / rows;
    /*TODO*///
    /*TODO*///			blit.clip_left = left;
    /*TODO*///			blit.clip_right = right;
    /*TODO*///
    /*TODO*///			while( row < rows ){
    /*TODO*///				int cons = 1;
    /*TODO*///				int scrollx = rowscroll[row];
    /*TODO*///
    /*TODO*///				/* count consecutive rows scrolled by the same amount */
    /*TODO*///				if( scrollx != TILE_LINE_DISABLED ){
    /*TODO*///					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;
    /*TODO*///
    /*TODO*///					if( scrollx < 0 ){
    /*TODO*///						scrollx = blit.source_width - (-scrollx) % blit.source_width;
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						scrollx %= blit.source_width;
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_top = row * rowheight;
    /*TODO*///					if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///					blit.clip_bottom = (row + cons) * rowheight;
    /*TODO*///					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						xpos = scrollx - blit.source_width;
    /*TODO*///						xpos<blit.clip_right;
    /*TODO*///						xpos += blit.source_width
    /*TODO*///					){
    /*TODO*///						draw( xpos,0 );
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///				row += cons;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else if( rows == 1 && cols == 1 ){ /* XY scrolling playfield */
    /*TODO*///			int scrollx = rowscroll[0];
    /*TODO*///			int scrolly = colscroll[0];
    /*TODO*///
    /*TODO*///			if( scrollx < 0 ){
    /*TODO*///				scrollx = blit.source_width - (-scrollx) % blit.source_width;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				scrollx = scrollx % blit.source_width;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if( scrolly < 0 ){
    /*TODO*///				scrolly = blit.source_height - (-scrolly) % blit.source_height;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				scrolly = scrolly % blit.source_height;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///	 		blit.clip_left = left;
    /*TODO*///	 		blit.clip_top = top;
    /*TODO*///	 		blit.clip_right = right;
    /*TODO*///	 		blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///			for(
    /*TODO*///				ypos = scrolly - blit.source_height;
    /*TODO*///				ypos < blit.clip_bottom;
    /*TODO*///				ypos += blit.source_height
    /*TODO*///			){
    /*TODO*///				for(
    /*TODO*///					xpos = scrollx - blit.source_width;
    /*TODO*///					xpos < blit.clip_right;
    /*TODO*///					xpos += blit.source_width
    /*TODO*///				){
    /*TODO*///					draw( xpos,ypos );
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else if( rows == 1 ){ /* scrolling columns + horizontal scroll */
    /*TODO*///			int col = 0;
    /*TODO*///			int colwidth = blit.source_width / cols;
    /*TODO*///			int scrollx = rowscroll[0];
    /*TODO*///
    /*TODO*///			if( scrollx < 0 ){
    /*TODO*///				scrollx = blit.source_width - (-scrollx) % blit.source_width;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				scrollx = scrollx % blit.source_width;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			blit.clip_top = top;
    /*TODO*///			blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///			while( col < cols ){
    /*TODO*///				int cons = 1;
    /*TODO*///				int scrolly = colscroll[col];
    /*TODO*///
    /*TODO*///	 			/* count consecutive columns scrolled by the same amount */
    /*TODO*///				if( scrolly != TILE_LINE_DISABLED ){
    /*TODO*///					while( col + cons < cols &&	colscroll[col + cons] == scrolly ) cons++;
    /*TODO*///
    /*TODO*///					if( scrolly < 0 ){
    /*TODO*///						scrolly = blit.source_height - (-scrolly) % blit.source_height;
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						scrolly %= blit.source_height;
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_left = col * colwidth + scrollx;
    /*TODO*///					if (blit.clip_left < left) blit.clip_left = left;
    /*TODO*///					blit.clip_right = (col + cons) * colwidth + scrollx;
    /*TODO*///					if (blit.clip_right > right) blit.clip_right = right;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						ypos = scrolly - blit.source_height;
    /*TODO*///						ypos < blit.clip_bottom;
    /*TODO*///						ypos += blit.source_height
    /*TODO*///					){
    /*TODO*///						draw( scrollx,ypos );
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_left = col * colwidth + scrollx - blit.source_width;
    /*TODO*///					if (blit.clip_left < left) blit.clip_left = left;
    /*TODO*///					blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
    /*TODO*///					if (blit.clip_right > right) blit.clip_right = right;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						ypos = scrolly - blit.source_height;
    /*TODO*///						ypos < blit.clip_bottom;
    /*TODO*///						ypos += blit.source_height
    /*TODO*///					){
    /*TODO*///						draw( scrollx - blit.source_width,ypos );
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///				col += cons;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///		else if( cols == 1 ){ /* scrolling rows + vertical scroll */
    /*TODO*///			int row = 0;
    /*TODO*///			int rowheight = blit.source_height / rows;
    /*TODO*///			int scrolly = colscroll[0];
    /*TODO*///
    /*TODO*///			if( scrolly < 0 ){
    /*TODO*///				scrolly = blit.source_height - (-scrolly) % blit.source_height;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				scrolly = scrolly % blit.source_height;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			blit.clip_left = left;
    /*TODO*///			blit.clip_right = right;
    /*TODO*///
    /*TODO*///			while( row < rows ){
    /*TODO*///				int cons = 1;
    /*TODO*///				int scrollx = rowscroll[row];
    /*TODO*///
    /*TODO*///				/* count consecutive rows scrolled by the same amount */
    /*TODO*///
    /*TODO*///				if( scrollx != TILE_LINE_DISABLED ){
    /*TODO*///					while( row + cons < rows &&	rowscroll[row + cons] == scrollx ) cons++;
    /*TODO*///
    /*TODO*///					if( scrollx < 0){
    /*TODO*///						scrollx = blit.source_width - (-scrollx) % blit.source_width;
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						scrollx %= blit.source_width;
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_top = row * rowheight + scrolly;
    /*TODO*///					if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///					blit.clip_bottom = (row + cons) * rowheight + scrolly;
    /*TODO*///					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						xpos = scrollx - blit.source_width;
    /*TODO*///						xpos < blit.clip_right;
    /*TODO*///						xpos += blit.source_width
    /*TODO*///					){
    /*TODO*///						draw( xpos,scrolly );
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					blit.clip_top = row * rowheight + scrolly - blit.source_height;
    /*TODO*///					if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///					blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
    /*TODO*///					if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///					for(
    /*TODO*///						xpos = scrollx - blit.source_width;
    /*TODO*///						xpos < blit.clip_right;
    /*TODO*///						xpos += blit.source_width
    /*TODO*///					){
    /*TODO*///						draw( xpos,scrolly - blit.source_height );
    /*TODO*///					}
    /*TODO*///				}
    /*TODO*///				row += cons;
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_update( struct tilemap *tilemap ){
    /*TODO*///	if( tilemap==ALL_TILEMAPS ){
    /*TODO*///		tilemap = first_tilemap;
    /*TODO*///		while( tilemap ){
    /*TODO*///			tilemap_update( tilemap );
    /*TODO*///			tilemap = tilemap->next;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else if( tilemap->enable ){
    /*TODO*///		if( tilemap->scrolled ){
    /*TODO*///			void (*mark_visible)( int, int ) = tilemap->mark_visible;
    /*TODO*///
    /*TODO*///			int rows = tilemap->scroll_rows;
    /*TODO*///			const int *rowscroll = tilemap->rowscroll;
    /*TODO*///			int cols = tilemap->scroll_cols;
    /*TODO*///			const int *colscroll = tilemap->colscroll;
    /*TODO*///
    /*TODO*///			int left = tilemap->clip_left;
    /*TODO*///			int right = tilemap->clip_right;
    /*TODO*///			int top = tilemap->clip_top;
    /*TODO*///			int bottom = tilemap->clip_bottom;
    /*TODO*///
    /*TODO*///			blit.source_width = tilemap->width;
    /*TODO*///			blit.source_height = tilemap->height;
    /*TODO*///			blit.visible_row = tilemap->visible_row;
    /*TODO*///
    /*TODO*///			memset( tilemap->visible, 0, tilemap->num_tiles );
    /*TODO*///
    /*TODO*///			if( rows == 0 && cols == 0 ){ /* no scrolling */
    /*TODO*///		 		blit.clip_left = left;
    /*TODO*///		 		blit.clip_top = top;
    /*TODO*///		 		blit.clip_right = right;
    /*TODO*///		 		blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///				mark_visible( 0,0 );
    /*TODO*///			}
    /*TODO*///			else if( rows == 0 ){ /* scrolling columns */
    /*TODO*///				int col,colwidth;
    /*TODO*///
    /*TODO*///				colwidth = blit.source_width / cols;
    /*TODO*///
    /*TODO*///				blit.clip_top = top;
    /*TODO*///				blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///				col = 0;
    /*TODO*///				while( col < cols ){
    /*TODO*///					int cons,scroll;
    /*TODO*///
    /*TODO*///		 			/* count consecutive columns scrolled by the same amount */
    /*TODO*///					scroll = colscroll[col];
    /*TODO*///					cons = 1;
    /*TODO*///					if(scroll != TILE_LINE_DISABLED)
    /*TODO*///					{
    /*TODO*///						while( col + cons < cols &&	colscroll[col + cons] == scroll ) cons++;
    /*TODO*///
    /*TODO*///						if (scroll < 0) scroll = blit.source_height - (-scroll) % blit.source_height;
    /*TODO*///						else scroll %= blit.source_height;
    /*TODO*///
    /*TODO*///						blit.clip_left = col * colwidth;
    /*TODO*///						if (blit.clip_left < left) blit.clip_left = left;
    /*TODO*///						blit.clip_right = (col + cons) * colwidth;
    /*TODO*///						if (blit.clip_right > right) blit.clip_right = right;
    /*TODO*///
    /*TODO*///						mark_visible( 0,scroll );
    /*TODO*///						mark_visible( 0,scroll - blit.source_height );
    /*TODO*///					}
    /*TODO*///					col += cons;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else if( cols == 0 ){ /* scrolling rows */
    /*TODO*///				int row,rowheight;
    /*TODO*///
    /*TODO*///				rowheight = blit.source_height / rows;
    /*TODO*///
    /*TODO*///				blit.clip_left = left;
    /*TODO*///				blit.clip_right = right;
    /*TODO*///
    /*TODO*///				row = 0;
    /*TODO*///				while( row < rows ){
    /*TODO*///					int cons,scroll;
    /*TODO*///
    /*TODO*///					/* count consecutive rows scrolled by the same amount */
    /*TODO*///					scroll = rowscroll[row];
    /*TODO*///					cons = 1;
    /*TODO*///					if(scroll != TILE_LINE_DISABLED)
    /*TODO*///					{
    /*TODO*///						while( row + cons < rows &&	rowscroll[row + cons] == scroll ) cons++;
    /*TODO*///
    /*TODO*///						if (scroll < 0) scroll = blit.source_width - (-scroll) % blit.source_width;
    /*TODO*///						else scroll %= blit.source_width;
    /*TODO*///
    /*TODO*///						blit.clip_top = row * rowheight;
    /*TODO*///						if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///						blit.clip_bottom = (row + cons) * rowheight;
    /*TODO*///						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///						mark_visible( scroll,0 );
    /*TODO*///						mark_visible( scroll - blit.source_width,0 );
    /*TODO*///					}
    /*TODO*///					row += cons;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else if( rows == 1 && cols == 1 ){ /* XY scrolling playfield */
    /*TODO*///				int scrollx,scrolly;
    /*TODO*///
    /*TODO*///				if (rowscroll[0] < 0) scrollx = blit.source_width - (-rowscroll[0]) % blit.source_width;
    /*TODO*///				else scrollx = rowscroll[0] % blit.source_width;
    /*TODO*///
    /*TODO*///				if (colscroll[0] < 0) scrolly = blit.source_height - (-colscroll[0]) % blit.source_height;
    /*TODO*///				else scrolly = colscroll[0] % blit.source_height;
    /*TODO*///
    /*TODO*///		 		blit.clip_left = left;
    /*TODO*///		 		blit.clip_top = top;
    /*TODO*///		 		blit.clip_right = right;
    /*TODO*///		 		blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///				mark_visible( scrollx,scrolly );
    /*TODO*///				mark_visible( scrollx,scrolly - blit.source_height );
    /*TODO*///				mark_visible( scrollx - blit.source_width,scrolly );
    /*TODO*///				mark_visible( scrollx - blit.source_width,scrolly - blit.source_height );
    /*TODO*///			}
    /*TODO*///			else if( rows == 1 ){ /* scrolling columns + horizontal scroll */
    /*TODO*///				int col,colwidth;
    /*TODO*///				int scrollx;
    /*TODO*///
    /*TODO*///				if (rowscroll[0] < 0) scrollx = blit.source_width - (-rowscroll[0]) % blit.source_width;
    /*TODO*///				else scrollx = rowscroll[0] % blit.source_width;
    /*TODO*///
    /*TODO*///				colwidth = blit.source_width / cols;
    /*TODO*///
    /*TODO*///				blit.clip_top = top;
    /*TODO*///				blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///				col = 0;
    /*TODO*///				while( col < cols ){
    /*TODO*///					int cons,scroll;
    /*TODO*///
    /*TODO*///		 			/* count consecutive columns scrolled by the same amount */
    /*TODO*///					scroll = colscroll[col];
    /*TODO*///					cons = 1;
    /*TODO*///					if(scroll != TILE_LINE_DISABLED)
    /*TODO*///					{
    /*TODO*///						while( col + cons < cols &&	colscroll[col + cons] == scroll ) cons++;
    /*TODO*///
    /*TODO*///						if (scroll < 0) scroll = blit.source_height - (-scroll) % blit.source_height;
    /*TODO*///						else scroll %= blit.source_height;
    /*TODO*///
    /*TODO*///						blit.clip_left = col * colwidth + scrollx;
    /*TODO*///						if (blit.clip_left < left) blit.clip_left = left;
    /*TODO*///						blit.clip_right = (col + cons) * colwidth + scrollx;
    /*TODO*///						if (blit.clip_right > right) blit.clip_right = right;
    /*TODO*///
    /*TODO*///						mark_visible( scrollx,scroll );
    /*TODO*///						mark_visible( scrollx,scroll - blit.source_height );
    /*TODO*///
    /*TODO*///						blit.clip_left = col * colwidth + scrollx - blit.source_width;
    /*TODO*///						if (blit.clip_left < left) blit.clip_left = left;
    /*TODO*///						blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
    /*TODO*///						if (blit.clip_right > right) blit.clip_right = right;
    /*TODO*///
    /*TODO*///						mark_visible( scrollx - blit.source_width,scroll );
    /*TODO*///						mark_visible( scrollx - blit.source_width,scroll - blit.source_height );
    /*TODO*///					}
    /*TODO*///					col += cons;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///			else if( cols == 1 ){ /* scrolling rows + vertical scroll */
    /*TODO*///				int row,rowheight;
    /*TODO*///				int scrolly;
    /*TODO*///
    /*TODO*///				if (colscroll[0] < 0) scrolly = blit.source_height - (-colscroll[0]) % blit.source_height;
    /*TODO*///				else scrolly = colscroll[0] % blit.source_height;
    /*TODO*///
    /*TODO*///				rowheight = blit.source_height / rows;
    /*TODO*///
    /*TODO*///				blit.clip_left = left;
    /*TODO*///				blit.clip_right = right;
    /*TODO*///
    /*TODO*///				row = 0;
    /*TODO*///				while( row < rows ){
    /*TODO*///					int cons,scroll;
    /*TODO*///
    /*TODO*///					/* count consecutive rows scrolled by the same amount */
    /*TODO*///					scroll = rowscroll[row];
    /*TODO*///					cons = 1;
    /*TODO*///					if(scroll != TILE_LINE_DISABLED)
    /*TODO*///					{
    /*TODO*///						while (row + cons < rows &&	rowscroll[row + cons] == scroll) cons++;
    /*TODO*///
    /*TODO*///						if (scroll < 0) scroll = blit.source_width - (-scroll) % blit.source_width;
    /*TODO*///						else scroll %= blit.source_width;
    /*TODO*///
    /*TODO*///						blit.clip_top = row * rowheight + scrolly;
    /*TODO*///						if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///						blit.clip_bottom = (row + cons) * rowheight + scrolly;
    /*TODO*///						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///						mark_visible( scroll,scrolly );
    /*TODO*///						mark_visible( scroll - blit.source_width,scrolly );
    /*TODO*///
    /*TODO*///						blit.clip_top = row * rowheight + scrolly - blit.source_height;
    /*TODO*///						if (blit.clip_top < top) blit.clip_top = top;
    /*TODO*///						blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
    /*TODO*///						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///						mark_visible( scroll,scrolly - blit.source_height );
    /*TODO*///						mark_visible( scroll - blit.source_width,scrolly - blit.source_height );
    /*TODO*///					}
    /*TODO*///					row += cons;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			tilemap->scrolled = 0;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		{
    /*TODO*///			int num_pens = tilemap->tile_width*tilemap->tile_height; /* precalc - needed for >4bpp pen management handling */
    /*TODO*///
    /*TODO*///			int tile_index;
    /*TODO*///			char *visible = tilemap->visible;
    /*TODO*///			char *dirty_vram = tilemap->dirty_vram;
    /*TODO*///			char *dirty_pixels = tilemap->dirty_pixels;
    /*TODO*///
    /*TODO*///			UINT8 **pendata = tilemap->pendata;
    /*TODO*///			UINT8 **maskdata = tilemap->maskdata;
    /*TODO*///			unsigned short **paldata = tilemap->paldata;
    /*TODO*///			unsigned int *pen_usage = tilemap->pen_usage;
    /*TODO*///
    /*TODO*///			int tile_flip = 0;
    /*TODO*///			if( tilemap->attributes&TILEMAP_FLIPX ) tile_flip |= TILE_FLIPX;
    /*TODO*///			if( tilemap->attributes&TILEMAP_FLIPY ) tile_flip |= TILE_FLIPY;
    /*TODO*///
    /*TODO*///			if( Machine->orientation & ORIENTATION_SWAP_XY )
    /*TODO*///			{
    /*TODO*///				if( Machine->orientation & ORIENTATION_FLIP_X ) tile_flip ^= TILE_FLIPY;
    /*TODO*///				if( Machine->orientation & ORIENTATION_FLIP_Y ) tile_flip ^= TILE_FLIPX;
    /*TODO*///			}
    /*TODO*///			else
    /*TODO*///			{
    /*TODO*///				if( Machine->orientation & ORIENTATION_FLIP_X ) tile_flip ^= TILE_FLIPX;
    /*TODO*///				if( Machine->orientation & ORIENTATION_FLIP_Y ) tile_flip ^= TILE_FLIPY;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///
    /*TODO*///			tile_info.flags = 0;
    /*TODO*///			tile_info.priority = 0;
    /*TODO*///
    /*TODO*///			for( tile_index=0; tile_index<tilemap->num_tiles; tile_index++ ){
    /*TODO*///				if( visible[tile_index] && dirty_vram[tile_index] ){
    /*TODO*///					int row = tile_index/tilemap->num_cols;
    /*TODO*///					int col = tile_index%tilemap->num_cols;
    /*TODO*///					int flags;
    /*TODO*///
    /*TODO*///					if( tilemap->orientation & ORIENTATION_FLIP_Y ) row = tilemap->num_rows-1-row;
    /*TODO*///					if( tilemap->orientation & ORIENTATION_FLIP_X ) col = tilemap->num_cols-1-col;
    /*TODO*///					if( tilemap->orientation & ORIENTATION_SWAP_XY ) SWAP(col,row)
    /*TODO*///
    /*TODO*///					{
    /*TODO*///						unsigned short *the_color = paldata[tile_index];
    /*TODO*///						if( the_color ){
    /*TODO*///							unsigned int old_pen_usage = pen_usage[tile_index];
    /*TODO*///							if( old_pen_usage ){
    /*TODO*///								palette_decrease_usage_count( the_color-Machine->remapped_colortable, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///							}
    /*TODO*///							else {
    /*TODO*///								palette_decrease_usage_countx( the_color-Machine->remapped_colortable, num_pens, pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///					tilemap->tile_get_info( col, row );
    /*TODO*///
    /*TODO*///					flags = tile_info.flags ^ tile_flip;
    /*TODO*///					if( tilemap->orientation & ORIENTATION_SWAP_XY ){
    /*TODO*///						flags =
    /*TODO*///							(flags&0xfc) |
    /*TODO*///							((flags&1)<<1) | ((flags&2)>>1);
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					pen_usage[tile_index] = tile_info.pen_usage;
    /*TODO*///					pendata[tile_index] = tile_info.pen_data;
    /*TODO*///					paldata[tile_index] = tile_info.pal_data;
    /*TODO*///					maskdata[tile_index] = tile_info.mask_data; // needed for TILEMAP_BITMASK
    /*TODO*///					tilemap->flags[tile_index] = flags;
    /*TODO*///					tilemap->priority[tile_index] = tile_info.priority;
    /*TODO*///
    /*TODO*///
    /*TODO*///					if( tile_info.pen_usage ){
    /*TODO*///						palette_increase_usage_count( tile_info.pal_data-Machine->remapped_colortable, tile_info.pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///					}
    /*TODO*///					else {
    /*TODO*///						palette_increase_usage_countx( tile_info.pal_data-Machine->remapped_colortable, num_pens, tile_info.pen_data, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    /*TODO*///					}
    /*TODO*///
    /*TODO*///					dirty_pixels[tile_index] = 1;
    /*TODO*///					dirty_vram[tile_index] = 0;
    /*TODO*///				}
    /*TODO*///			}
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_set_scrollx( struct tilemap *tilemap, int which, int value ){
    /*TODO*///	value = tilemap->scrollx_delta-value;
    /*TODO*///
    /*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->height-value;
    /*TODO*///		if( tilemap->colscroll[which]!=value ){
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///			tilemap->colscroll[which] = value;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->width-value;
    /*TODO*///		if( tilemap->rowscroll[which]!=value ){
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///			tilemap->rowscroll[which] = value;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///void tilemap_set_scrolly( struct tilemap *tilemap, int which, int value ){
    /*TODO*///	value = tilemap->scrolly_delta - value;
    /*TODO*///
    /*TODO*///	if( tilemap->orientation & ORIENTATION_SWAP_XY ){
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) which = tilemap->scroll_rows-1 - which;
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) value = screen_width-tilemap->width-value;
    /*TODO*///		if( tilemap->rowscroll[which]!=value ){
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///			tilemap->rowscroll[which] = value;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///	else {
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_X ) which = tilemap->scroll_cols-1 - which;
    /*TODO*///		if( tilemap->orientation & ORIENTATION_FLIP_Y ) value = screen_height-tilemap->height-value;
    /*TODO*///		if( tilemap->colscroll[which]!=value ){
    /*TODO*///			tilemap->scrolled = 1;
    /*TODO*///			tilemap->colscroll[which] = value;
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_set_scrolldx( struct tilemap *tilemap, int dx, int dx_if_flipped ){
    /*TODO*///	tilemap->dx = dx;
    /*TODO*///	tilemap->dx_if_flipped = dx_if_flipped;
    /*TODO*///	tilemap->scrollx_delta = ( tilemap->attributes & TILEMAP_FLIPX )?dx_if_flipped:dx;
    /*TODO*///}
    /*TODO*///
    /*TODO*///void tilemap_set_scrolldy( struct tilemap *tilemap, int dy, int dy_if_flipped ){
    /*TODO*///	tilemap->dy = dy;
    /*TODO*///	tilemap->dy_if_flipped = dy_if_flipped;
    /*TODO*///	tilemap->scrolly_delta = ( tilemap->attributes & TILEMAP_FLIPY )?dy_if_flipped:dy;
    /*TODO*///}
    /*TODO*///
    /*TODO*///#else // DECLARE
    /*TODO*////*
    /*TODO*///	The following procedure body is #included several times by
    /*TODO*///	tilemap.c to implement a suite of tilemap_draw subroutines.
    /*TODO*///
    /*TODO*///	The constants TILE_WIDTH and TILE_HEIGHT are different in
    /*TODO*///	each instance of this code, allowing arithmetic shifts to
    /*TODO*///	be used by the compiler instead of multiplies/divides.
    /*TODO*///
    /*TODO*///	This routine should be fairly optimal, for C code, though of
    /*TODO*///	course there is room for improvement.
    /*TODO*///
    /*TODO*///	It renders pixels one row at a time, skipping over runs of totally
    /*TODO*///	transparent tiles, and calling custom blitters to handle runs of
    /*TODO*///	masked/totally opaque tiles.
    /*TODO*///*/
    /*TODO*///
    public static WriteHandlerPtr draw8x8x8BPP = new WriteHandlerPtr() { public void handler(int xpos, int ypos)
    {
        throw new UnsupportedOperationException("tilemap draw unimplemented");
    }};
    /*TODO*///DECLARE( draw, (int xpos, int ypos),
    /*TODO*///{
    /*TODO*///	int x1 = xpos;
    /*TODO*///	int y1 = ypos;
    /*TODO*///	int x2 = xpos+blit.source_width;
    /*TODO*///	int y2 = ypos+blit.source_height;
    /*TODO*///
    /*TODO*///	/* clip source coordinates */
    /*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
    /*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
    /*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
    /*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
    /*TODO*///
    /*TODO*///	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
    /*TODO*///		UINT8 priority = blit.priority;
    /*TODO*///
    /*TODO*///		DATA_TYPE *dest_baseaddr;
    /*TODO*///		DATA_TYPE *dest_next;
    /*TODO*///		const DATA_TYPE *source_baseaddr;
    /*TODO*///		const DATA_TYPE *source_next;
    /*TODO*///		const UINT8 *mask_baseaddr;
    /*TODO*///		const UINT8 *mask_next;
    /*TODO*///
    /*TODO*///		int c1;
    /*TODO*///		int c2; /* leftmost and rightmost visible columns in source tilemap */
    /*TODO*///		int y; /* current screen line to render */
    /*TODO*///		int y_next;
    /*TODO*///
    /*TODO*///		dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];
    /*TODO*///
    /*TODO*///		/* convert screen coordinates to source tilemap coordinates */
    /*TODO*///		x1 -= xpos;
    /*TODO*///		y1 -= ypos;
    /*TODO*///		x2 -= xpos;
    /*TODO*///		y2 -= ypos;
    /*TODO*///
    /*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
    /*TODO*///		mask_baseaddr = blit.bitmask->line[y1];
    /*TODO*///
    /*TODO*///		c1 = x1/TILE_WIDTH; /* round down */
    /*TODO*///		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */
    /*TODO*///
    /*TODO*///		y = y1;
    /*TODO*///		y_next = TILE_HEIGHT*(y1/TILE_HEIGHT) + TILE_HEIGHT;
    /*TODO*///		if( y_next>y2 ) y_next = y2;
    /*TODO*///
    /*TODO*///		{
    /*TODO*///			int dy = y_next-y;
    /*TODO*///			dest_next = dest_baseaddr + dy*blit.dest_line_offset;
    /*TODO*///			source_next = source_baseaddr + dy*blit.source_line_offset;
    /*TODO*///			mask_next = mask_baseaddr + dy*blit.mask_line_offset;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		for(;;){
    /*TODO*///			int row = y/TILE_HEIGHT;
    /*TODO*///			UINT8 *mask_data = blit.mask_data_row[row];
    /*TODO*///			char *priority_data = blit.priority_data_row[row];
    /*TODO*///
    /*TODO*///			UINT8 tile_type;
    /*TODO*///			UINT8 prev_tile_type = TILE_TRANSPARENT;
    /*TODO*///
    /*TODO*///			int x_start = x1;
    /*TODO*///			int x_end;
    /*TODO*///
    /*TODO*///			int column;
    /*TODO*///			for( column=c1; column<=c2; column++ ){
    /*TODO*///				if( column==c2 || priority_data[column]!=priority )
    /*TODO*///					tile_type = TILE_TRANSPARENT;
    /*TODO*///				else
    /*TODO*///					tile_type = mask_data[column];
    /*TODO*///
    /*TODO*///				if( tile_type!=prev_tile_type ){
    /*TODO*///					x_end = column*TILE_WIDTH;
    /*TODO*///					if( x_end<x1 ) x_end = x1;
    /*TODO*///					if( x_end>x2 ) x_end = x2;
    /*TODO*///
    /*TODO*///					if( prev_tile_type != TILE_TRANSPARENT ){
    /*TODO*///						if( prev_tile_type == TILE_MASKED ){
    /*TODO*///							int count = (x_end+7)/8 - x_start/8;
    /*TODO*///							const UINT8 *mask0 = mask_baseaddr + x_start/8;
    /*TODO*///							const DATA_TYPE *source0 = source_baseaddr + (x_start&0xfff8);
    /*TODO*///							DATA_TYPE *dest0 = dest_baseaddr + (x_start&0xfff8);
    /*TODO*///							int i = y;
    /*TODO*///							for(;;){
    /*TODO*///								memcpybitmask( dest0, source0, mask0, count );
    /*TODO*///								if( ++i == y_next ) break;
    /*TODO*///
    /*TODO*///								dest0 += blit.dest_line_offset;
    /*TODO*///								source0 += blit.source_line_offset;
    /*TODO*///								mask0 += blit.mask_line_offset;
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///						else { /* TILE_OPAQUE */
    /*TODO*///							int num_pixels = x_end - x_start;
    /*TODO*///							DATA_TYPE *dest0 = dest_baseaddr+x_start;
    /*TODO*///							const DATA_TYPE *source0 = source_baseaddr+x_start;
    /*TODO*///							int i = y;
    /*TODO*///							for(;;){
    /*TODO*///								memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
    /*TODO*///								if( ++i == y_next ) break;
    /*TODO*///
    /*TODO*///								dest0 += blit.dest_line_offset;
    /*TODO*///								source0 += blit.source_line_offset;
    /*TODO*///							}
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///					x_start = x_end;
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				prev_tile_type = tile_type;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if( y_next==y2 ) break; /* we are done! */
    /*TODO*///
    /*TODO*///			dest_baseaddr = dest_next;
    /*TODO*///			source_baseaddr = source_next;
    /*TODO*///			mask_baseaddr = mask_next;
    /*TODO*///
    /*TODO*///			y = y_next;
    /*TODO*///			y_next += TILE_HEIGHT;
    /*TODO*///
    /*TODO*///			if( y_next>=y2 ){
    /*TODO*///				y_next = y2;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				dest_next += blit.dest_row_offset;
    /*TODO*///				source_next += blit.source_row_offset;
    /*TODO*///				mask_next += blit.mask_row_offset;
    /*TODO*///			}
    /*TODO*///		} /* process next row */
    /*TODO*///	} /* not totally clipped */
    /*TODO*///})
    /*TODO*///
    public static WriteHandlerPtr draw_opaque8x8x8BPP = new WriteHandlerPtr() { public void handler(int xpos, int ypos)
    {
        throw new UnsupportedOperationException("tilemap draw_opaque unimplemented");
    }};
    /*TODO*///DECLARE( draw_opaque, (int xpos, int ypos),
    /*TODO*///{
    /*TODO*///	int x1 = xpos;
    /*TODO*///	int y1 = ypos;
    /*TODO*///	int x2 = xpos+blit.source_width;
    /*TODO*///	int y2 = ypos+blit.source_height;
    /*TODO*///
    /*TODO*///	/* clip source coordinates */
    /*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
    /*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
    /*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
    /*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
    /*TODO*///
    /*TODO*///	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
    /*TODO*///		UINT8 priority = blit.priority;
    /*TODO*///
    /*TODO*///		DATA_TYPE *dest_baseaddr;
    /*TODO*///		DATA_TYPE *dest_next;
    /*TODO*///		const DATA_TYPE *source_baseaddr;
    /*TODO*///		const DATA_TYPE *source_next;
    /*TODO*///
    /*TODO*///		int c1;
    /*TODO*///		int c2; /* leftmost and rightmost visible columns in source tilemap */
    /*TODO*///		int y; /* current screen line to render */
    /*TODO*///		int y_next;
    /*TODO*///
    /*TODO*///		dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];
    /*TODO*///
    /*TODO*///		/* convert screen coordinates to source tilemap coordinates */
    /*TODO*///		x1 -= xpos;
    /*TODO*///		y1 -= ypos;
    /*TODO*///		x2 -= xpos;
    /*TODO*///		y2 -= ypos;
    /*TODO*///
    /*TODO*///		source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
    /*TODO*///
    /*TODO*///		c1 = x1/TILE_WIDTH; /* round down */
    /*TODO*///		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */
    /*TODO*///
    /*TODO*///		y = y1;
    /*TODO*///		y_next = TILE_HEIGHT*(y1/TILE_HEIGHT) + TILE_HEIGHT;
    /*TODO*///		if( y_next>y2 ) y_next = y2;
    /*TODO*///
    /*TODO*///		{
    /*TODO*///			int dy = y_next-y;
    /*TODO*///			dest_next = dest_baseaddr + dy*blit.dest_line_offset;
    /*TODO*///			source_next = source_baseaddr + dy*blit.source_line_offset;
    /*TODO*///		}
    /*TODO*///
    /*TODO*///		for(;;){
    /*TODO*///			int row = y/TILE_HEIGHT;
    /*TODO*///			char *priority_data = blit.priority_data_row[row];
    /*TODO*///
    /*TODO*///			UINT8 tile_type;
    /*TODO*///			UINT8 prev_tile_type = TILE_TRANSPARENT;
    /*TODO*///
    /*TODO*///			int x_start = x1;
    /*TODO*///			int x_end;
    /*TODO*///
    /*TODO*///			int column;
    /*TODO*///			for( column=c1; column<=c2; column++ ){
    /*TODO*///				if( column==c2 || priority_data[column]!=priority )
    /*TODO*///					tile_type = TILE_TRANSPARENT;
    /*TODO*///				else
    /*TODO*///					tile_type = TILE_OPAQUE;
    /*TODO*///
    /*TODO*///				if( tile_type!=prev_tile_type ){
    /*TODO*///					x_end = column*TILE_WIDTH;
    /*TODO*///					if( x_end<x1 ) x_end = x1;
    /*TODO*///					if( x_end>x2 ) x_end = x2;
    /*TODO*///
    /*TODO*///					if( prev_tile_type != TILE_TRANSPARENT ){
    /*TODO*///						/* TILE_OPAQUE */
    /*TODO*///						int num_pixels = x_end - x_start;
    /*TODO*///						DATA_TYPE *dest0 = dest_baseaddr+x_start;
    /*TODO*///						const DATA_TYPE *source0 = source_baseaddr+x_start;
    /*TODO*///						int i = y;
    /*TODO*///						for(;;){
    /*TODO*///							memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
    /*TODO*///							if( ++i == y_next ) break;
    /*TODO*///
    /*TODO*///							dest0 += blit.dest_line_offset;
    /*TODO*///							source0 += blit.source_line_offset;
    /*TODO*///						}
    /*TODO*///					}
    /*TODO*///					x_start = x_end;
    /*TODO*///				}
    /*TODO*///
    /*TODO*///				prev_tile_type = tile_type;
    /*TODO*///			}
    /*TODO*///
    /*TODO*///			if( y_next==y2 ) break; /* we are done! */
    /*TODO*///
    /*TODO*///			dest_baseaddr = dest_next;
    /*TODO*///			source_baseaddr = source_next;
    /*TODO*///
    /*TODO*///			y = y_next;
    /*TODO*///			y_next += TILE_HEIGHT;
    /*TODO*///
    /*TODO*///			if( y_next>=y2 ){
    /*TODO*///				y_next = y2;
    /*TODO*///			}
    /*TODO*///			else {
    /*TODO*///				dest_next += blit.dest_row_offset;
    /*TODO*///				source_next += blit.source_row_offset;
    /*TODO*///			}
    /*TODO*///		} /* process next row */
    /*TODO*///	} /* not totally clipped */
    /*TODO*///})
    /*TODO*///
    public static WriteHandlerPtr mark_visible8x8x8BPP = new WriteHandlerPtr() { public void handler(int xpos, int ypos)
    {
        throw new UnsupportedOperationException("tilemap mark_visible unimplemented");
    }};

    /*TODO*///DECLARE( mark_visible, (int xpos, int ypos),
    /*TODO*///{
    /*TODO*///	int x1 = xpos;
    /*TODO*///	int y1 = ypos;
    /*TODO*///	int x2 = xpos+blit.source_width;
    /*TODO*///	int y2 = ypos+blit.source_height;
    /*TODO*///
    /*TODO*///	/* clip source coordinates */
    /*TODO*///	if( x1<blit.clip_left ) x1 = blit.clip_left;
    /*TODO*///	if( x2>blit.clip_right ) x2 = blit.clip_right;
    /*TODO*///	if( y1<blit.clip_top ) y1 = blit.clip_top;
    /*TODO*///	if( y2>blit.clip_bottom ) y2 = blit.clip_bottom;
    /*TODO*///
    /*TODO*///	if( x1<x2 && y1<y2 ){ /* do nothing if totally clipped */
    /*TODO*///		int c1;
    /*TODO*///		int c2; /* leftmost and rightmost visible columns in source tilemap */
    /*TODO*///		int r1;
    /*TODO*///		int r2;
    /*TODO*///		char **visible_row;
    /*TODO*///		int span;
    /*TODO*///		int row;
    /*TODO*///
    /*TODO*///		/* convert screen coordinates to source tilemap coordinates */
    /*TODO*///		x1 -= xpos;
    /*TODO*///		y1 -= ypos;
    /*TODO*///		x2 -= xpos;
    /*TODO*///		y2 -= ypos;
    /*TODO*///
    /*TODO*///		r1 = y1/TILE_HEIGHT;
    /*TODO*///		r2 = (y2+TILE_HEIGHT-1)/TILE_HEIGHT;
    /*TODO*///
    /*TODO*///		c1 = x1/TILE_WIDTH; /* round down */
    /*TODO*///		c2 = (x2+TILE_WIDTH-1)/TILE_WIDTH; /* round up */
    /*TODO*///		visible_row = blit.visible_row;
    /*TODO*///		span = c2-c1;
    /*TODO*///
    /*TODO*///		for( row=r1; row<r2; row++ ){
    /*TODO*///			memset( visible_row[row]+c1, 1, span );
    /*TODO*///		}
    /*TODO*///	}
    /*TODO*///})
    /*TODO*///
    /*TODO*///#undef TILE_WIDTH
    /*TODO*///#undef TILE_HEIGHT
    /*TODO*///#undef DATA_TYPE
    /*TODO*///#undef memcpybitmask
    /*TODO*///#undef DECLARE
    /*TODO*///
    /*TODO*///#endif /* DECLARE */
    /*TODO*///    
}
