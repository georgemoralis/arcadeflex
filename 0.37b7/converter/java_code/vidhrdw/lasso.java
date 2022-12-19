/* vidhrdw/lasso.c */

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class lasso
{
	
	UBytePtr lasso_vram; /* 0x2000 bytes for a 256x256x1 bitmap */
	static int flipscreen,gfxbank;
	static struct tilemap *background;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	
	void lasso_vh_convert_color_prom(UBytePtr palette,unsigned short *colortable,const UBytePtr color_prom)
	{
		int i;
		for (i = 0;i < 0x40;i++)
		{
			int bit0,bit1,bit2;
	
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			*(palette++) = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (*color_prom >> 6) & 0x01;
			bit1 = (*color_prom >> 7) & 0x01;
			*(palette++) = 0x4f * bit0 + 0xa8 * bit1;
	
			color_prom++;
		}
	}
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile_number = videoram.read(tile_index);
		int attributes = videoram.read(tile_index+0x400);
		SET_TILE_INFO(gfxbank,tile_number,attributes&0xf)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr lasso_vh_start = new VhStartPtr() { public int handler() 
	{
		background = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
	
		if (!background)
			return 1;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr lasso_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( videoram.read(offset)!=data )
		{
			videoram.write(offset,data);
		tilemap_mark_tile_dirty( background, offset&0x3ff );
		}
	} };
	
	public static WriteHandlerPtr lasso_cocktail_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flipscreen = data & 0x01;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
		if (gfxbank != ((data & 0x04) >> 2))
		{
			gfxbank = (data & 0x04) >> 2;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr lasso_backcolor_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i,bit0,bit1,bit2,r,g,b;
	
	
		/* red component */
		bit0 = (data >> 0) & 0x01;
		bit1 = (data >> 1) & 0x01;
		bit2 = (data >> 2) & 0x01;
		r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		/* green component */
		bit0 = (data >> 3) & 0x01;
		bit1 = (data >> 4) & 0x01;
		bit2 = (data >> 5) & 0x01;
		g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
		/* blue component */
		bit0 = (data >> 6) & 0x01;
		bit1 = (data >> 7) & 0x01;
		b = 0x4f * bit0 + 0xa8 * bit1;
	
		for( i=0; i<0x40; i+=4 ) /* stuff into color#0 of each palette */
			palette_change_color( i,r,g,b );
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
	    const struct GfxElement *gfx = Machine.gfx[2+gfxbank];
	    struct rectangle clip = Machine.visible_area;
	    const UBytePtr finish = spriteram;
		const UBytePtr source = spriteram + 0x80 - 4;
		while( source>=finish )
		{
			int color = source[2];
			int tile_number = source[1];
			int sy = source[0];
			int sx = source[3];
			int flipy = (tile_number&0x80);
			int flipx = (tile_number&0x40);
			if (flipscreen != 0)
			{
				sx = 240-sx;
				flipx = !flipx;
				flipy = !flipy;
			}
			else
			{
				sy = 240-sy;
			}
	        drawgfx( bitmap,gfx,
	            tile_number&0x3f,
	            color,
	            flipx, flipy,
	            sx,sy,
	            &clip,TRANSPARENCY_PEN,0);
	
	        source-=4;
	    }
	}
	
	static void draw_lasso( struct osd_bitmap *bitmap )
	{
		const UBytePtr source = lasso_vram;
		int x,y;
		int pen = Machine.pens[0x3f];
		for( y=0; y<256; y++ )
		{
			for( x=0; x<256; x+=8 )
			{
				int data = *source++;
				if (data != 0)
				{
					int bit;
					if (flipscreen != 0)
					{
						for( bit=0; bit<8; bit++ )
						{
							if( (data<<bit)&0x80 )
								plot_pixel( bitmap, 255-(x+bit), 255-y, pen );
						}
					}
					else
					{
						for( bit=0; bit<8; bit++ )
						{
							if( (data<<bit)&0x80 )
								plot_pixel( bitmap, x+bit, y, pen );
						}
					}
				}
			}
		}
	}
	
	public static VhUpdatePtr lasso_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		tilemap_render(ALL_TILEMAPS);
		tilemap_draw(bitmap,background,0);
		draw_lasso(bitmap);
		draw_sprites(bitmap);
	} };
}
