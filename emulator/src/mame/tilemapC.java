package mame;

import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.driverH.*;
import static mame.drawgfxH.*;
import mame.tilemapH.tilemap;
import static mame.tilemapH.*;
import static arcadeflex.video.*;
import static mame.osdependH.*;
import static mame.palette.*;
import static mame.paletteH.*;

public class tilemapC {
    /*TODO*///#ifndef DECLARE
    /*TODO*///
    /*TODO*///#include "driver.h"
    /*TODO*///#include "tilemap.h"
    /*TODO*///
    static char[] flip_bit_table=new char[0x100]; /* horizontal flip for 8 pixels */
    static tilemap first_tilemap; /* resource tracking */
    static int screen_width, screen_height;
    public static _tile_info tile_info = new _tile_info();
    /*TODO*///
    /*TODO*///enum {
    /*TODO*///	TILE_TRANSPARENT,
    /*TODO*///	TILE_MASKED,
    /*TODO*///	TILE_OPAQUE
    /*TODO*///};
    /*TODO*///
    public static final int TILE_TRANSPARENT=0;
    public static final int TILE_MASKED=1;
    public static final int TILE_OPAQUE=2;     
    /* the following parameters are constant across tilemap_draw calls */

    static class _blit
    {
       public int clip_left, clip_top, clip_right, clip_bottom;
       public int source_width, source_height;

       public int dest_line_offset, source_line_offset, mask_line_offset;
       public int dest_row_offset, source_row_offset, mask_row_offset;
       public osd_bitmap screen, pixmap, bitmask;

       public UBytePtr[] mask_data_row;
       public UBytePtr[] priority_data_row, visible_row;
       public char priority;//unsigned byte
    }
    static _blit blit = new _blit();


    public static int MASKROWBYTES(int W) { return (W + 7) / 8; }
    
    static void memcpybitmask8( UBytePtr dest, UBytePtr source, UBytePtr bitmask, int count ){
    	int bi = 0, di = 0, si = 0;
            for (; ; )
            {
                int  data = bitmask.read(bi++) & 0xFF;
                if ((data & 0x80) != 0) dest.write(di + 0,source.read(si + 0));
                if ((data & 0x40) != 0) dest.write(di + 1,source.read(si + 1));
                if ((data & 0x20) != 0) dest.write(di + 2,source.read(si + 2));
                if ((data & 0x10) != 0) dest.write(di + 3,source.read(si + 3));
                if ((data & 0x08) != 0) dest.write(di + 4,source.read(si + 4));
                if ((data & 0x04) != 0) dest.write(di + 5,source.read(si + 5));
                if ((data & 0x02) != 0) dest.write(di + 6,source.read(si + 6));
                if ((data & 0x01) != 0) dest.write(di + 7,source.read(si + 7));
                if (--count == 0) break;
                si += 8;
                di += 8;
            }
    }
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
    
    public static void tilemap_set_flip(tilemap _tilemap, int attributes ){
    	if( _tilemap==ALL_TILEMAPS ){
    		_tilemap = first_tilemap;
    		while( _tilemap!=null ){
    			tilemap_set_flip( _tilemap, attributes );
    			_tilemap = _tilemap.next;
    		}
    	}
    	else if( _tilemap.attributes!=attributes ){
    		_tilemap.attributes = attributes;
    
    		_tilemap.orientation = Machine.orientation;
    
    		if(( attributes&TILEMAP_FLIPY )!=0){
    			_tilemap.orientation ^= ORIENTATION_FLIP_Y;
    			_tilemap.scrolly_delta = _tilemap.dy_if_flipped;
    		}
    		else {
    			_tilemap.scrolly_delta = _tilemap.dy;
    		}
    
    		if(( attributes&TILEMAP_FLIPX )!=0){
    			_tilemap.orientation ^= ORIENTATION_FLIP_X;
    			_tilemap.scrollx_delta = _tilemap.dx_if_flipped;
    		}
    		else {
    			_tilemap.scrollx_delta = _tilemap.dx;
    		}
    
    		tilemap_mark_all_tiles_dirty( _tilemap );
    	}
    }
    
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
    
    /***********************************************************************************/
    
    public static void dispose_tile_info(tilemap _tilemap ){
    	_tilemap.pendata=null;
    	_tilemap.maskdata=null;
    	_tilemap.paldata=null;
    	_tilemap.pen_usage=null;
    	_tilemap.priority=null;
    	_tilemap.visible=null;
    	_tilemap.dirty_vram=null;
    	_tilemap.dirty_pixels=null;
    	_tilemap.flags=null;
    	_tilemap.priority_row=null;
    	_tilemap.visible_row=null;
    }
    
    public static int create_tile_info( tilemap _tilemap )
    {
    	int num_tiles = _tilemap.num_tiles;
    	int num_cols = _tilemap.num_cols;
    	int num_rows = _tilemap.num_rows;
    
    	_tilemap.pendata = new UBytePtr[num_tiles];//malloc( sizeof( UINT8 *)*num_tiles );
    	_tilemap.maskdata = new UBytePtr[num_tiles];//malloc( sizeof( UINT8 *)*num_tiles ); /* needed only for TILEMAP_BITMASK */
    	_tilemap.paldata = new CharPtr[num_tiles];//malloc( sizeof( unsigned short *)*num_tiles );
    	_tilemap.pen_usage = new int[num_tiles];//malloc( sizeof( unsigned int )*num_tiles );
    	_tilemap.priority = new char[num_tiles];
    	_tilemap.visible = new char[num_tiles];
    	_tilemap.dirty_vram = new char[num_tiles];
    	_tilemap.dirty_pixels = new char[num_tiles];
    	_tilemap.flags =new char[num_tiles];
    	_tilemap.rowscroll = new int[_tilemap.height];//(int *)calloc(_tilemap.height,sizeof(int));
    	_tilemap.colscroll = new int[_tilemap.width];//(int *)calloc(_tilemap.width,sizeof(int));
    
    	_tilemap.priority_row =  new UBytePtr[num_rows];//malloc( sizeof(char *)*num_rows );
    	_tilemap.visible_row = new UBytePtr[num_rows];//malloc( sizeof(char *)*num_rows );
    
    	if( _tilemap.pendata!=null &&
    		_tilemap.maskdata!=null &&
    		_tilemap.paldata!=null && _tilemap.pen_usage!=null &&
    		_tilemap.priority!=null && _tilemap.visible!=null &&
    		_tilemap.dirty_vram!=null && _tilemap.dirty_pixels!=null &&
    		_tilemap.flags!=null &&
    		_tilemap.rowscroll!=null && _tilemap.colscroll!=null &&
    		_tilemap.priority_row!=null && _tilemap.visible_row!=null )
    	{
    		int tile_index,row;
    
    		for( row=0; row<num_rows; row++ ){
    			_tilemap.priority_row[row] = new UBytePtr(_tilemap.priority,num_cols*row);
    			_tilemap.visible_row[row] = new UBytePtr(_tilemap.visible,num_cols*row);
    		}
    
    		for( tile_index=0; tile_index<num_tiles; tile_index++ ){
    			_tilemap.paldata[tile_index] = null;
    		}
    
    		memset( _tilemap.priority, 0, num_tiles );
    		memset( _tilemap.visible, 0, num_tiles );
    		memset( _tilemap.dirty_vram, 1, num_tiles );
    		memset( _tilemap.dirty_pixels, 1, num_tiles );
    
    		return 1; /* done */
    	}
    	dispose_tile_info( _tilemap );
    	return 0; /* error */
    }
    
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
    		_tilemap.pixmap_line_offset = _tilemap.pixmap.line[1].base - _tilemap.pixmap.line[0].base;
    		return 1; /* done */
    	}
    	return 0; /* error */
    }
    
    public static void dispose_pixmap(tilemap _tilemap ){
    	osd_free_bitmap( _tilemap.pixmap );
    	_tilemap.colscroll=null;
    	_tilemap.rowscroll=null;
    }
    
    
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
    				_tilemap.fg_mask_line_offset = _tilemap.fg_mask.line[1].base - _tilemap.fg_mask.line[0].base;
    				return 1; /* done */
    			}
    			 _tilemap.fg_mask_data_row=null;
    		}
    		 _tilemap.fg_mask_data=null;
    	}
    	return 0; /* error */
    }
    
    public static int create_bg_mask(tilemap _tilemap ){
    	if( (_tilemap.type & TILEMAP_SPLIT)==0 ) return 1;
    
    	_tilemap.bg_mask_data = new char [_tilemap.num_tiles];
    	if( _tilemap.bg_mask_data!=null ){
    		_tilemap.bg_mask_data_row = new_mask_data_table( _tilemap.bg_mask_data, _tilemap.num_cols, _tilemap.num_rows );
    		if( _tilemap.bg_mask_data_row!=null ){
    			_tilemap.bg_mask = create_bitmask( MASKROWBYTES(_tilemap.width), _tilemap.height );
    			if( _tilemap.bg_mask!=null ){
    				_tilemap.bg_mask_line_offset = _tilemap.bg_mask.line[1].base - _tilemap.bg_mask.line[0].base;
    				return 1; /* done */
    			}
    			 _tilemap.bg_mask_data_row =null;
    		}
    		_tilemap.bg_mask_data=null;
    	}
    	return 0; /* error */
    }
    
    public static void dispose_fg_mask( tilemap _tilemap ){
    	_tilemap.fg_mask_data_row=null;
    	_tilemap.fg_mask_data=null;
    	osd_free_bitmap( _tilemap.fg_mask );
    }
    
    public static void dispose_bg_mask( tilemap _tilemap ){
    	if(( _tilemap.type & TILEMAP_SPLIT )!=0){
    		osd_free_bitmap( _tilemap.bg_mask );
    		_tilemap.bg_mask_data_row=null;
    		_tilemap.bg_mask_data=null;
    	}
    }
    
    /***********************************************************************************/
    
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
    					if( create_bg_mask( _tilemap )!=0 ){
    						if( create_tile_info( _tilemap )!=0 ){
    							_tilemap.next = first_tilemap;
    							first_tilemap = _tilemap;
    							return _tilemap;
    						}
    						dispose_bg_mask( _tilemap );
    					}
    					dispose_fg_mask( _tilemap );
    				}
    				dispose_pixmap( _tilemap );
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
    /***********************************************************************************/
    
    public static void tilemap_mark_tile_dirty(tilemap _tilemap, int col, int row )
    {
    	/* convert logical coordinates to cached coordinates */
    	if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0) 
        {
            //SWAP(col,row)
            int temp =col;  col=row;  row=temp;
        }
    	if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) col = _tilemap.num_cols-1-col;
    	if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) row = _tilemap.num_rows-1-row;
    
    //	_tilemap.dirty_vram_row[row][col] = 1;
    	_tilemap.dirty_vram[row*_tilemap.num_cols + col] = 1;
    }
    
    public static void tilemap_mark_all_tiles_dirty(tilemap _tilemap ){
    	if( _tilemap==ALL_TILEMAPS ){
    		_tilemap = first_tilemap;
    		while( _tilemap!=null ){
    			tilemap_mark_all_tiles_dirty( _tilemap );
    			_tilemap = _tilemap.next;
    		}
    	}
    	else {
    		memset( _tilemap.dirty_vram, 1, _tilemap.num_tiles );
    	}
    }
    
    public static void tilemap_mark_all_pixels_dirty(tilemap _tilemap ){
    	if( _tilemap==ALL_TILEMAPS ){
    		_tilemap = first_tilemap;
    		while( _tilemap!=null ){
    			tilemap_mark_all_pixels_dirty( _tilemap );
    			_tilemap = _tilemap.next;
    		}
    	}
    	else {
    		/* let's invalidate all offscreen tiles, decreasing the refcounts */
    		int tile_index;
    		int num_pens = _tilemap.tile_width*_tilemap.tile_height; /* precalc - needed for >4bpp pen management handling */
    		for( tile_index=0; tile_index<_tilemap.num_tiles; tile_index++ ){
    			if( _tilemap.visible[tile_index]==0 ){
    				CharPtr the_color = _tilemap.paldata[tile_index];
    				if( the_color!=null ){
    					/*unsigned */int old_pen_usage = _tilemap.pen_usage[tile_index];
    					if( old_pen_usage!=0 ){
   /*TODO*/ 						palette_decrease_usage_count( the_color.base- Machine.remapped_colortable.length, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    					}
    					else {
  /*TODO*/  						palette_decrease_usage_countx( the_color.base-Machine.remapped_colortable.length, num_pens, _tilemap.pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    					}
    					_tilemap.paldata[tile_index] = null;
    				}
    				_tilemap.dirty_vram[tile_index] = 1;
    			}
    		}
    		memset( _tilemap.dirty_pixels, 1, _tilemap.num_tiles );
    	}
    }
    
    /***********************************************************************************/
    
    public static void draw_tile(
    		osd_bitmap pixmap,
    		int col, int row, int tile_width, int tile_height,
    		UBytePtr pendata, CharPtr paldata,
    		char flags )
    {
    	int x, sx = tile_width*col;
    	int sy,y1,y2,dy;
    
    	if( Machine.scrbitmap.depth==16 ){
            throw new UnsupportedOperationException("draw_tile in 16bit unimplemented");
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
    	}
    	else {
    		if(( flags&TILE_FLIPY )!=0){
    			y1 = tile_height*row+tile_height-1;
    			y2 = y1-tile_height;
    	 		dy = -1;
    	 	}
    	 	else {
    			y1 = tile_height*row;
    			y2 = y1+tile_height;
    	 		dy = 1;
    	 	}
    
    		if(( flags&TILE_FLIPX )!=0){
    			tile_width--;
    			for( sy=y1; sy!=y2; sy+=dy ){
                            UBytePtr dest = new UBytePtr(pixmap.line[sy], sx);
                            for (x = tile_width; x >= 0; x--)
                            { 
                                //dest[x] = paldata[*pendata++];
                                dest.write(x,paldata.read(pendata.readinc()));
                            }
    			}
    		}
    		else {
    			for( sy=y1; sy!=y2; sy+=dy ){
    				UBytePtr dest = new UBytePtr(pixmap.line[sy], sx);
    				for( x=0; x<tile_width; x++ ) 
                                {
                                    //dest[x] = paldata[*pendata++];
                                    dest.write(x,paldata.read(pendata.readinc()));
                                }
    			}
    		}
    	}
    }
    
    static void draw_mask(
    		osd_bitmap mask,
    		int col, int row, int tile_width, int tile_height,
    		UBytePtr pendata, int transmask,
    		char flags )
    {
    	int x,bit,sx = tile_width*col;
    	int sy,y1,y2,dy;
    
    	if(( flags&TILE_FLIPY )!=0){
    		y1 = tile_height*row+tile_height-1;
    		y2 = y1-tile_height;
     		dy = -1;
     	}
     	else {
    		y1 = tile_height*row;
    		y2 = y1+tile_height;
     		dy = 1;
     	}
    
    	if(( flags&TILE_FLIPX )!=0){
    		tile_width--;
    		for( sy=y1; sy!=y2; sy+=dy ){
    			//UINT8 *mask_dest  = mask->line[sy]+sx/8;
                        UBytePtr mask_dest = new UBytePtr(mask.line[sy], sx / 8);
    			for( x=tile_width/8; x>=0; x-- ){
    				/*UINT8*/char data = 0;
    				for( bit=0; bit<8; bit++ ){
    					char/*UINT8*/ p = pendata.readinc();
    					data = (char)((data>>1)|(((1<<p)&transmask)!=0?0x00:0x80));
    				}
    				mask_dest.write(x,(data & 0xFF));
    			}
    		}
    	}
    	else {
    		for( sy=y1; sy!=y2; sy+=dy ){
    			//UINT8 *mask_dest  = mask->line[sy]+sx/8;
                        UBytePtr mask_dest = new UBytePtr(mask.line[sy], sx / 8);
    			for( x=0; x<tile_width/8; x++ ){
    				/*UINT8*/char data = 0;
    				for( bit=0; bit<8; bit++ ){
    					char/*UINT8*/ p = pendata.readinc();
    					data = (char)((data<<1)|(((1<<p)&transmask)!=0?0x00:0x01));
    				}
    				mask_dest.write(x,(data & 0xFF));
    			}
    		}
    	}
    }
    
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
    
    public static void tilemap_render( tilemap _tilemap )
    {
    	if( _tilemap==ALL_TILEMAPS ){
    		_tilemap = first_tilemap;
    		while( _tilemap!=null ){
    			tilemap_render( _tilemap );
    			_tilemap = _tilemap.next;
    		}
    	}
    	else if( _tilemap.enable!=0 ){
    		int type = _tilemap.type;
    		int transparent_pen = _tilemap.transparent_pen;
    		int[] transmask = _tilemap.transmask;
    
    		int tile_width = _tilemap.tile_width;
    		int tile_height = _tilemap.tile_height;
    
    		char []dirty_pixels = _tilemap.dirty_pixels;
    		char []visible = _tilemap.visible;
    		int tile_index = 0; // LBO - CWPro4 bug workaround
    		int row,col;
    
    		for( row=0; row<_tilemap.num_rows; row++ ){
    			for( col=0; col<_tilemap.num_cols; col++ ){
    				if( dirty_pixels[tile_index]!=0 && visible[tile_index]!=0 ){
    					int pen_usage = _tilemap.pen_usage[tile_index];
    					UBytePtr pendata = _tilemap.pendata[tile_index];
    					char flags = _tilemap.flags[tile_index];
    
    					draw_tile(
    						_tilemap.pixmap,
    						col, row, tile_width, tile_height,
    						pendata,
    						_tilemap.paldata[tile_index],
    						flags );
    					if(( type & TILEMAP_BITMASK )!=0){
    /*TODO*///						_tilemap.fg_mask_data_row[row][col] =
    /*TODO*///							draw_bitmask( _tilemap.fg_mask,
    /*TODO*///								col, row, tile_width, tile_height,
    /*TODO*///								_tilemap.maskdata[tile_index], flags );
                                            throw new UnsupportedOperationException("tilemap rendered unimplemented");
    					}
    					else if(( type & TILEMAP_SPLIT )!=0){          
    						int pen_mask = (transparent_pen<0)?0:(1<<transparent_pen);
    
    						if(( flags&TILE_IGNORE_TRANSPARENCY )!=0){
    							_tilemap.fg_mask_data_row[row].write(col,TILE_OPAQUE);
    							_tilemap.bg_mask_data_row[row].write(col,TILE_OPAQUE);
    						}
    						else if( pen_mask == pen_usage ){ /* totally transparent */
    							_tilemap.fg_mask_data_row[row].write(col,TILE_TRANSPARENT);
    							_tilemap.bg_mask_data_row[row].write(col,TILE_TRANSPARENT);
    						}
    						else {
                                                  
    							int fg_transmask = transmask[(flags>>2)&3];
    							long bg_transmask = (long)(((~fg_transmask)|pen_mask) & 0xFFFFFFFFL);
    							if( (pen_usage & fg_transmask)==0 ){ /* foreground totally opaque */
    								_tilemap.fg_mask_data_row[row].write(col,TILE_OPAQUE);
    								_tilemap.bg_mask_data_row[row].write(col,TILE_TRANSPARENT);
    							}
    							else if( (pen_usage & bg_transmask)==0 ){ /* background totally opaque */
    								_tilemap.fg_mask_data_row[row].write(col,TILE_TRANSPARENT);
    								_tilemap.bg_mask_data_row[row].write(col,TILE_OPAQUE);
    							}
    							else if( (pen_usage & ~bg_transmask)==0 ){ /* background transparent */
                                                              throw new UnsupportedOperationException("tilemap rendered unimplemented");
    /*TODO*///								draw_mask( _tilemap.fg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, fg_transmask, flags );
    /*TODO*///								_tilemap.fg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///								_tilemap.bg_mask_data_row[row][col] = TILE_TRANSPARENT;
    							}
    							else if( (pen_usage & ~fg_transmask)==0 ){ /* foreground transparent */
                                                              throw new UnsupportedOperationException("tilemap rendered unimplemented");
    /*TODO*///								draw_mask( _tilemap.bg_mask,
    /*TODO*///									col, row, tile_width, tile_height,
    /*TODO*///									pendata, bg_transmask, flags );
    /*TODO*///								_tilemap.fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///								_tilemap.bg_mask_data_row[row][col] = TILE_MASKED;
    							}
    							else { /* split tile - opacity in both foreground and background */

    								draw_mask( _tilemap.fg_mask,
    									col, row, tile_width, tile_height,
    									pendata, fg_transmask, flags );
    								draw_mask( _tilemap.bg_mask,
    									col, row, tile_width, tile_height,
    									pendata, (int)bg_transmask, flags );
    								_tilemap.fg_mask_data_row[row].write(col,TILE_MASKED);
    								_tilemap.bg_mask_data_row[row].write(col,TILE_MASKED);
    							}
    						}
    				 	}
    				 	else if( type==TILEMAP_TRANSPARENT ){
                                            throw new UnsupportedOperationException("tilemap rendered unimplemented");
    /*TODO*///				 		unsigned int fg_transmask = 1 << transparent_pen;
    /*TODO*///				 	 	if( flags&TILE_IGNORE_TRANSPARENCY ) fg_transmask = 0;
    /*TODO*///
    /*TODO*///						if( pen_usage == fg_transmask ){
    /*TODO*///							_tilemap.fg_mask_data_row[row][col] = TILE_TRANSPARENT;
    /*TODO*///						}
    /*TODO*///						else if( pen_usage & fg_transmask ){
    /*TODO*///							draw_mask( _tilemap.fg_mask,
    /*TODO*///								col, row, tile_width, tile_height,
    /*TODO*///								pendata, fg_transmask, flags );
    /*TODO*///							_tilemap.fg_mask_data_row[row][col] = TILE_MASKED;
    /*TODO*///						}
    /*TODO*///						else {
    /*TODO*///							_tilemap.fg_mask_data_row[row][col] = TILE_OPAQUE;
    /*TODO*///						}
    					}
    					else {
    						_tilemap.fg_mask_data_row[row].write(col,TILE_OPAQUE);
    				 	}
    
    					dirty_pixels[tile_index] = 0;
    				}
    				tile_index++;
    			} /* next col */
    		} /* next row */
    	}
    }
    
    /***********************************************************************************/
 
    public static void tilemap_draw( osd_bitmap dest, tilemap _tilemap, int priority )
    {
    	int xpos,ypos;
    
    	if( _tilemap.enable!=0 ){
    		WriteHandlerPtr draw;
    
    		int rows = _tilemap.scroll_rows;
    		int []rowscroll = _tilemap.rowscroll;
    		int cols = _tilemap.scroll_cols;
    		int []colscroll = _tilemap.colscroll;
    
    		int left = _tilemap.clip_left;
    		int right = _tilemap.clip_right;
    		int top = _tilemap.clip_top;
    		int bottom = _tilemap.clip_bottom;
    
    		int tile_height = _tilemap.tile_height;
    
    		blit.screen = dest;
    		blit.dest_line_offset = dest.line[1].base - dest.line[0].base;
    
    		blit.pixmap = _tilemap.pixmap;
    		blit.source_line_offset = _tilemap.pixmap_line_offset;
    
    		if( _tilemap.type==TILEMAP_OPAQUE || ((priority&TILEMAP_IGNORE_TRANSPARENCY)!=0) ){
    			draw = _tilemap.draw_opaque;
    		}
    		else {
    			draw = _tilemap.draw;
    
    			if(( priority&TILEMAP_BACK )!=0){
    				blit.bitmask = _tilemap.bg_mask;
    				blit.mask_line_offset = _tilemap.bg_mask_line_offset;
    				blit.mask_data_row = _tilemap.bg_mask_data_row;
    			}
    			else {
    				blit.bitmask = _tilemap.fg_mask;
    				blit.mask_line_offset = _tilemap.fg_mask_line_offset;
    				blit.mask_data_row = _tilemap.fg_mask_data_row;
    			}
    
    			blit.mask_row_offset = tile_height*blit.mask_line_offset;
    		}
    
    		if( dest.depth==16 ){
    			blit.dest_line_offset /= 2;
    			blit.source_line_offset /= 2;
    		}
    
    		blit.source_row_offset = tile_height*blit.source_line_offset;
    		blit.dest_row_offset = tile_height*blit.dest_line_offset;
    
    		blit.priority_data_row = _tilemap.priority_row;
    		blit.source_width = _tilemap.width;
    		blit.source_height = _tilemap.height;
    		blit.priority = (char)(priority&0xf);
    
    		if( rows == 0 && cols == 0 ){ /* no scrolling */
                    throw new UnsupportedOperationException("tilemap draw unimplemented");
    /*TODO*///	 		blit.clip_left = left;
    /*TODO*///	 		blit.clip_top = top;
    /*TODO*///	 		blit.clip_right = right;
    /*TODO*///	 		blit.clip_bottom = bottom;
    /*TODO*///
    /*TODO*///			draw( 0,0 );
    		}
    		else if( rows == 0 ){ /* scrolling columns */
                    throw new UnsupportedOperationException("tilemap draw unimplemented");
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
    		}
    		else if( cols == 0 ){ /* scrolling rows */
                    throw new UnsupportedOperationException("tilemap draw unimplemented");
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
    					draw.handler(xpos,ypos );
    				}
    			}
    		}
    		else if( rows == 1 ){ /* scrolling columns + horizontal scroll */
                    throw new UnsupportedOperationException("tilemap draw unimplemented");
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
    						draw.handler(xpos,scrolly );
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
    						draw.handler(xpos,scrolly - blit.source_height );
    					}
    				}
    				row += cons;
    			}
    		}
    	}
    }
    
    public static void tilemap_update(tilemap _tilemap ){
    	if( _tilemap==ALL_TILEMAPS ){
    		_tilemap = first_tilemap;
    		while( _tilemap!=null ){
    			tilemap_update( _tilemap );
    			_tilemap = _tilemap.next;
    		}
    	}
    	else if(( _tilemap.enable )!=0)
        {
    		if(( _tilemap.scrolled )!=0)
                {                
			//void (*mark_visible)( int, int ) = _tilemap.mark_visible;
    
    			int rows = _tilemap.scroll_rows;
    			int[] rowscroll = _tilemap.rowscroll;
    			int cols = _tilemap.scroll_cols;
    			int[] colscroll = _tilemap.colscroll;
    
    			int left = _tilemap.clip_left;
    			int right = _tilemap.clip_right;
    			int top = _tilemap.clip_top;
    			int bottom = _tilemap.clip_bottom;
    
    			blit.source_width = _tilemap.width;
    			blit.source_height = _tilemap.height;
    			blit.visible_row = _tilemap.visible_row;
    
    			memset( _tilemap.visible, 0, _tilemap.num_tiles );
    
    			if( rows == 0 && cols == 0 ){ /* no scrolling */
    		 		blit.clip_left = left;
    		 		blit.clip_top = top;
    		 		blit.clip_right = right;
    		 		blit.clip_bottom = bottom;
    
    				_tilemap.mark_visible.handler(0,0 );
                           
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
    
    						_tilemap.mark_visible.handler(0,scroll );
    						_tilemap.mark_visible.handler( 0,scroll - blit.source_height );
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
    
    						_tilemap.mark_visible.handler( scroll,0 );
    						_tilemap.mark_visible.handler( scroll - blit.source_width,0 );
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
    
    				_tilemap.mark_visible.handler( scrollx,scrolly );
    				_tilemap.mark_visible.handler( scrollx,scrolly - blit.source_height );
    				_tilemap.mark_visible.handler( scrollx - blit.source_width,scrolly );
    				_tilemap.mark_visible.handler( scrollx - blit.source_width,scrolly - blit.source_height );
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
    
    						_tilemap.mark_visible.handler( scrollx,scroll );
    						_tilemap.mark_visible.handler( scrollx,scroll - blit.source_height );
    
    						blit.clip_left = col * colwidth + scrollx - blit.source_width;
    						if (blit.clip_left < left) blit.clip_left = left;
    						blit.clip_right = (col + cons) * colwidth + scrollx - blit.source_width;
    						if (blit.clip_right > right) blit.clip_right = right;
    
    						_tilemap.mark_visible.handler( scrollx - blit.source_width,scroll );
    						_tilemap.mark_visible.handler( scrollx - blit.source_width,scroll - blit.source_height );
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
    
    						_tilemap.mark_visible.handler( scroll,scrolly );
    						_tilemap.mark_visible.handler( scroll - blit.source_width,scrolly );
    
    						blit.clip_top = row * rowheight + scrolly - blit.source_height;
    						if (blit.clip_top < top) blit.clip_top = top;
    						blit.clip_bottom = (row + cons) * rowheight + scrolly - blit.source_height;
    						if (blit.clip_bottom > bottom) blit.clip_bottom = bottom;
    
    						_tilemap.mark_visible.handler(scroll,scrolly - blit.source_height );
    						_tilemap.mark_visible.handler(scroll - blit.source_width,scrolly - blit.source_height );
    					}
    					row += cons;
                                       
    				}
    			}
    
    			_tilemap.scrolled = 0;
    		}
    
    		{
    			int num_pens = _tilemap.tile_width*_tilemap.tile_height; /* precalc - needed for >4bpp pen management handling */
    
    			int tile_index;
    			char []visible = _tilemap.visible;
    			char []dirty_vram = _tilemap.dirty_vram;
    			char []dirty_pixels = _tilemap.dirty_pixels;
    
    			UBytePtr[] pendata = _tilemap.pendata;
    			UBytePtr[] maskdata = _tilemap.maskdata;
    			CharPtr[] paldata = _tilemap.paldata;
    			int []pen_usage = _tilemap.pen_usage;
    
    			int tile_flip = 0;
    			if(( _tilemap.attributes&TILEMAP_FLIPX )!=0) tile_flip |= TILE_FLIPX;
    			if(( _tilemap.attributes&TILEMAP_FLIPY )!=0) tile_flip |= TILE_FLIPY;
    
    			if(( Machine.orientation & ORIENTATION_SWAP_XY )!=0)
    			{
    				if(( Machine.orientation & ORIENTATION_FLIP_X )!=0) tile_flip ^= TILE_FLIPY;
    				if(( Machine.orientation & ORIENTATION_FLIP_Y )!=0) tile_flip ^= TILE_FLIPX;
    			}
    			else
    			{
    				if(( Machine.orientation & ORIENTATION_FLIP_X )!=0) tile_flip ^= TILE_FLIPX;
    				if(( Machine.orientation & ORIENTATION_FLIP_Y )!=0) tile_flip ^= TILE_FLIPY;
    			}
    
    
    			tile_info.flags = 0;
    			tile_info.priority = 0;
    
    			for( tile_index=0; tile_index<_tilemap.num_tiles; tile_index++ ){
    				if( visible[tile_index]!=0 && dirty_vram[tile_index]!=0 ){
    					int row = tile_index/_tilemap.num_cols;
    					int col = tile_index%_tilemap.num_cols;
    					int flags;
    
    					if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) row = _tilemap.num_rows-1-row;
    					if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) col = _tilemap.num_cols-1-col;
    					if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0)
                                        {
                                            //SWAP(col,row)
                                            int temp=col; col=row; row=temp;
                                        }
    
    					{
    						CharPtr the_color = paldata[tile_index];
    						if( the_color!=null ){
    							/*unsigned*/ int old_pen_usage = pen_usage[tile_index];
    							if( old_pen_usage!=0 ){
    		/*TODO RECHECK THIS*/				palette_decrease_usage_count( the_color.base-Machine.remapped_colortable.length, old_pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    							}
    							else {
    		/*TODO RECHECK THIS*/				palette_decrease_usage_countx( the_color.base-Machine.remapped_colortable.length, num_pens, pendata[tile_index], PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    							}
    						}
    					}
    					_tilemap.tile_get_info.handler(col, row );
    
    					flags = tile_info.flags ^ tile_flip;
    					if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0){
    						flags =
    							(flags&0xfc) |
    							((flags&1)<<1) | ((flags&2)>>1);
    					}
    
    					pen_usage[tile_index] = tile_info.pen_usage;
    					pendata[tile_index] = tile_info.pen_data;
    					paldata[tile_index] = tile_info.pal_data;
    					maskdata[tile_index] = tile_info.mask_data; // needed for TILEMAP_BITMASK
    					_tilemap.flags[tile_index] = (char)flags;
    					_tilemap.priority[tile_index] = tile_info.priority;
    
    
    					if( tile_info.pen_usage!=0 ){
  /*TODO RECHECK THIS*/  			palette_increase_usage_count( tile_info.pal_data.base-Machine.remapped_colortable.length, tile_info.pen_usage, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    					}
    					else {
 /*TODO RECHECK THIS*/   			palette_increase_usage_countx( tile_info.pal_data.base-Machine.remapped_colortable.length, num_pens, tile_info.pen_data, PALETTE_COLOR_VISIBLE|PALETTE_COLOR_CACHED );
    					}
    
    					dirty_pixels[tile_index] = 1;
    					dirty_vram[tile_index] = 0;
    				}
    			}
    		}
   	}
    }
   
    public static void tilemap_set_scrollx( tilemap _tilemap, int which, int value ){
    	value = _tilemap.scrollx_delta-value;
    
    	if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0){
    		if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) which = _tilemap.scroll_cols-1 - which;
    		if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) value = screen_height-_tilemap.height-value;
    		if( _tilemap.colscroll[which]!=value ){
    			_tilemap.scrolled = 1;
    			_tilemap.colscroll[which] = value;
    		}
    	}
    	else {
    		if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) which = _tilemap.scroll_rows-1 - which;
    		if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) value = screen_width-_tilemap.width-value;
    		if( _tilemap.rowscroll[which]!=value ){
    			_tilemap.scrolled = 1;
    			_tilemap.rowscroll[which] = value;
    		}
    	}
    }
    public static void tilemap_set_scrolly(tilemap _tilemap, int which, int value ){
    	value = _tilemap.scrolly_delta - value;
    
    	if(( _tilemap.orientation & ORIENTATION_SWAP_XY )!=0){
    		if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) which = _tilemap.scroll_rows-1 - which;
    		if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) value = screen_width-_tilemap.width-value;
    		if( _tilemap.rowscroll[which]!=value ){
    			_tilemap.scrolled = 1;
    			_tilemap.rowscroll[which] = value;
    		}
    	}
    	else {
    		if(( _tilemap.orientation & ORIENTATION_FLIP_X )!=0) which = _tilemap.scroll_cols-1 - which;
    		if(( _tilemap.orientation & ORIENTATION_FLIP_Y )!=0) value = screen_height-_tilemap.height-value;
    		if(_tilemap.colscroll[which]!=value ){
    			_tilemap.scrolled = 1;
    			_tilemap.colscroll[which] = value;
    		}
    	}
    }
    
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
    		/*UINT8*/char priority = blit.priority;
    
    		UBytePtr dest_baseaddr;
    		UBytePtr dest_next;
    		UBytePtr source_baseaddr;
    		UBytePtr source_next;
    		UBytePtr mask_baseaddr;
    		UBytePtr mask_next;
    
    		int c1;
    		int c2; /* leftmost and rightmost visible columns in source tilemap */
    		int y; /* current screen line to render */
    		int y_next;
    
    		dest_baseaddr = new UBytePtr(blit.screen.line[y1], xpos);//dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];
    
    		/* convert screen coordinates to source tilemap coordinates */
    		x1 -= xpos;
    		y1 -= ypos;
    		x2 -= xpos;
    		y2 -= ypos;
    
    		//source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
    		//mask_baseaddr = blit.bitmask->line[y1];
                source_baseaddr = blit.pixmap.line[y1];
                mask_baseaddr = blit.bitmask.line[y1];


    
    		c1 = x1/8; /* round down */
    		c2 = (x2+8-1)/8; /* round up */
    
    		y = y1;
    		y_next = 8*(y1/8) + 8;
    		if( y_next>y2 ) y_next = y2;
    
    		{
    			int dy = y_next-y;
    			/*dest_next = dest_baseaddr + dy*blit.dest_line_offset;
    			source_next = source_baseaddr + dy*blit.source_line_offset;
    			mask_next = mask_baseaddr + dy*blit.mask_line_offset;*/
                        dest_next = new UBytePtr(dest_baseaddr, dy * blit.dest_line_offset);
                        source_next = new UBytePtr(source_baseaddr, dy * blit.source_line_offset);
                        mask_next = new UBytePtr(mask_baseaddr, dy * blit.mask_line_offset);

    		}
    
    		for(;;){
    			int row = y/8;
    			UBytePtr mask_data = blit.mask_data_row[row];
    			UBytePtr priority_data = blit.priority_data_row[row];
    
    			char/*UINT8*/ tile_type;
    			char/*UINT8*/ prev_tile_type = TILE_TRANSPARENT;
    
    			int x_start = x1;
    			int x_end;
    
    			int column;
    			for( column=c1; column<=c2; column++ ){
    				if( column==c2 || priority_data.read(column)!=priority )
    					tile_type = TILE_TRANSPARENT;
    				else
    					tile_type = mask_data.read(column);
    
    				if( tile_type!=prev_tile_type ){
    					x_end = column*8;
    					if( x_end<x1 ) x_end = x1;
    					if( x_end>x2 ) x_end = x2;
    
    					if( prev_tile_type != TILE_TRANSPARENT ){
                                            
    						if( prev_tile_type == TILE_MASKED ){
//                                                    throw new UnsupportedOperationException("tilemap draw8x8x8BPP unimplemented");
    							int count = (x_end+7)/8 - x_start/8;
    							//const UINT8 *mask0 = mask_baseaddr + x_start/8;
    							//const DATA_TYPE *source0 = source_baseaddr + (x_start&0xfff8);
    							//DATA_TYPE *dest0 = dest_baseaddr + (x_start&0xfff8);
                                                        UBytePtr mask0 = new UBytePtr(mask_baseaddr,x_start/8);
                                                        UBytePtr source0 = new UBytePtr(source_baseaddr,(x_start&0xfff8));
                                                        UBytePtr dest0 = new UBytePtr(dest_baseaddr,(x_start&0xfff8));
    							int i = y;
    							for(;;){
    								memcpybitmask8( dest0, source0, mask0, count );
    								if( ++i == y_next ) break;
    
    								dest0.base +=blit.dest_line_offset;
    								source0.base +=blit.source_line_offset;
    								mask0.base +=blit.mask_line_offset;
    							}
    						}
    						else { /* TILE_OPAQUE */
                                                     
    							int num_pixels = x_end - x_start;
                                                        UBytePtr dest0 = new UBytePtr(dest_baseaddr, x_start);
                                                        UBytePtr source0 = new UBytePtr(source_baseaddr, x_start);
    							//DATA_TYPE *dest0 = dest_baseaddr+x_start;
    							//const DATA_TYPE *source0 = source_baseaddr+x_start;
    							int i = y;
    							for(;;){
                                                          
                                                            System.arraycopy(source0.memory, source0.base, dest0.memory, dest0.base, num_pixels);
    								//memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) );
    								if( ++i == y_next ) break;
    
    								dest0.base += blit.dest_line_offset;
    								source0.base +=blit.source_line_offset;
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
    			y_next += 8;
    
    			if( y_next>=y2 ){
    				y_next = y2;
    			}
    			else {
    				dest_next.base += blit.dest_row_offset;
    				source_next.base += blit.source_row_offset;
    				mask_next.base += blit.mask_row_offset;
    			}
    		} /* process next row */
    	} /* not totally clipped */
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
    		/*UINT8*/char priority = (char)(blit.priority & 0xFF);
    
    		UBytePtr dest_baseaddr;
    		UBytePtr dest_next;
    		UBytePtr source_baseaddr;
    		UBytePtr source_next;
    
    		int c1;
    		int c2; /* leftmost and rightmost visible columns in source tilemap */
    		int y; /* current screen line to render */
    		int y_next;
    
    		 dest_baseaddr = new UBytePtr(blit.screen.line[y1], xpos);//dest_baseaddr = xpos + (DATA_TYPE *)blit.screen->line[y1];
    
    		/* convert screen coordinates to source tilemap coordinates */
    		x1 -= xpos;
    		y1 -= ypos;
    		x2 -= xpos;
    		y2 -= ypos;
    
    		source_baseaddr = new UBytePtr(blit.pixmap.line[y1]);//source_baseaddr = (DATA_TYPE *)blit.pixmap->line[y1];
    
    		c1 = x1/8; /* round down */
    		c2 = (x2+8-1)/8; /* round up */
    
    		y = y1;
    		y_next = 8*(y1/8) + 8;
    		if( y_next>y2 ) y_next = y2;
    
    		{
                    int dy = y_next-y;
    		    //dest_next = dest_baseaddr + dy*blit.dest_line_offset;
    		    //source_next = source_baseaddr + dy*blit.source_line_offset;
                    dest_next = new UBytePtr(dest_baseaddr, dy * blit.dest_line_offset);
                    source_next = new UBytePtr(source_baseaddr, dy * blit.source_line_offset);
    		}
    
    		for(;;){
    			int row = y/8;
    			//char *priority_data = blit.priority_data_row[row];
                        UBytePtr priority_data = new UBytePtr(blit.priority_data_row[row]);

    			char/*UINT8*/ tile_type;
    			char/*UINT8*/ prev_tile_type = TILE_TRANSPARENT;
    
    			int x_start = x1;
    			int x_end;
    
    			int column;
    			for( column=c1; column<=c2; column++ ){
    				if( column==c2 || priority_data.read(column)!=priority )
    					tile_type = TILE_TRANSPARENT;
    				else
    					tile_type = TILE_OPAQUE;
    
    				if( tile_type!=prev_tile_type ){
    					x_end = column*8;
    					if( x_end<x1 ) x_end = x1;
    					if( x_end>x2 ) x_end = x2;
    
    					if( prev_tile_type != TILE_TRANSPARENT ){
    						/* TILE_OPAQUE */
    						int num_pixels = x_end - x_start;
    						//DATA_TYPE *dest0 = dest_baseaddr+x_start;
    						//const DATA_TYPE *source0 = source_baseaddr+x_start;
                                                UBytePtr dest0 = new UBytePtr(dest_baseaddr, x_start);
                                                UBytePtr source0 = new UBytePtr(source_baseaddr, x_start);

    						int i = y;
    						for(;;){
                                                    System.arraycopy(source0.memory, source0.base, dest0.memory, dest0.base, num_pixels);
                                                	//memcpy( dest0, source0, num_pixels*sizeof(DATA_TYPE) ); 							
    							if( ++i == y_next ) break;
    
    							dest0.base += blit.dest_line_offset;
    							source0.base += blit.source_line_offset;
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
    			y_next += 8;
    
    			if( y_next>=y2 ){
    				y_next = y2;
    			}
    			else {
    				dest_next.base += blit.dest_row_offset;
    				source_next.base += blit.source_row_offset;
    			}
    		} /* process next row */
    	} /* not totally clipped */
    }};
    public static WriteHandlerPtr mark_visible8x8x8BPP = new WriteHandlerPtr() { public void handler(int xpos, int ypos)
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
    		UBytePtr[] visible_row;
    		int span;
    		int row;
    
    		/* convert screen coordinates to source tilemap coordinates */
    		x1 -= xpos;
    		y1 -= ypos;
    		x2 -= xpos;
    		y2 -= ypos;
    
    		r1 = y1/8;
    		r2 = (y2+8-1)/8;
    
    		c1 = x1/8; /* round down */
    		c2 = (x2+8-1)/8; /* round up */
    		visible_row = blit.visible_row;
    		span = c2-c1;
    
    		for( row=r1; row<r2; row++ ){
                    for (int i = 0; i < span; i++)
    			visible_row[row].write(c1 + i, 1);//memset( visible_row[row]+c1, 1, span );
    		}
    	}
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
