/*
**	Video Driver for Taito Samurai (1985)
*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class tsamurai
{
	
	/*
	** prototypes
	*/
	
	
	
	/*
	** variables
	*/
	UBytePtr tsamurai_videoram;
	static int bgcolor;
	static int textbank;
	
	static struct tilemap *background, *foreground;
	
	
	/*
	** color prom decoding
	*/
	
	void tsamurai_convert_color_prom(UBytePtr palette, unsigned short *colortable,const UBytePtr color_prom)
	{
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	}
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		unsigned char attributes = tsamurai_videoram[2*tile_index+1];
		int color = (attributes&0x1f);
		SET_TILE_INFO(0,tsamurai_videoram[2*tile_index]+4*(attributes&0xc0),color )
	} };
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile_number = videoram.read(tile_index);
		if ((textbank & 1) != 0) tile_number += 256;
		SET_TILE_INFO(1,tile_number,colorram.read(((tile_index&0x1f)*2)+1)& 0x1f )
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr tsamurai_vh_start = new VhStartPtr() { public int handler() 
	{
		background = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		foreground = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!background || !foreground)
			return 1;
	
		background.transparent_pen = 0;
		foreground.transparent_pen = 0;
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr tsamurai_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrolly( background, 0, data );
	} };
	
	public static WriteHandlerPtr tsamurai_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrollx( background, 0, data );
	} };
	
	public static WriteHandlerPtr tsamurai_bgcolor_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		bgcolor = data;
	} };
	
	public static WriteHandlerPtr tsamurai_textbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( textbank!=data )
		{
			textbank = data;
			tilemap_mark_all_tiles_dirty( foreground );
		}
	} };
	
	public static WriteHandlerPtr tsamurai_bg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( tsamurai_videoram[offset]!=data )
		{
			tsamurai_videoram[offset]=data;
			offset = offset/2;
			tilemap_mark_tile_dirty(background,offset);
		}
	} };
	public static WriteHandlerPtr tsamurai_fg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( videoram.read(offset)!=data )
		{
			videoram.write(offset,data);
		tilemap_mark_tile_dirty(foreground,offset);
		}
	} };
	public static WriteHandlerPtr tsamurai_fg_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( colorram.read(offset)!=data )
		{
			colorram.write(offset,data);
		if ((offset & 1) != 0)
			{
				int col = offset/2;
				int row;
				for (row = 0;row < 32;row++)
					tilemap_mark_tile_dirty(foreground,32*row+col);
			}
		}
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
		struct GfxElement *gfx = Machine.gfx[2];
		const struct rectangle *clip = &Machine.visible_area;
		const UBytePtr source = spriteram+32*4-4;
		const UBytePtr finish = spriteram; /* ? */
		static int flicker;
		flicker = 1-flicker;
	
		while( source>=finish )
		{
			int attributes = source[2]; /* bit 0x10 is usually, but not always set */
	
			int sx = source[3] - 16;
			int sy = 240-source[0];
			int sprite_number = source[1];
			int color = attributes&0x1f;
			//color = 0x2d - color; nunchakun fix?
			if( sy<-16 ) sy += 256;
	
			if (flip_screen != 0)
			{
				drawgfx( bitmap,gfx,
					sprite_number&0x7f,
					color,
					1,(sprite_number&0x80)?0:1,
					256-32-sx,256-32-sy,
					clip,TRANSPARENCY_PEN,0 );
			}
			else
			{
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
	
	public static VhUpdatePtr tsamurai_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update( ALL_TILEMAPS );
		tilemap_render( ALL_TILEMAPS );
	
		/*
			This following isn't particularly efficient.  We'd be better off to
			dynamically change every 8th palette to the background color, so we
			could draw the background as an opaque tilemap.
	
			Note that the background color register isn't well understood
			(screenshots would be helpful)
		*/
		fillbitmap( bitmap, Machine.pens[bgcolor], 0 );
		tilemap_draw( bitmap, background, 0 );
	
		draw_sprites( bitmap );
	
		tilemap_draw( bitmap, foreground, 0 );
	} };
}
