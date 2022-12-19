/***************************************************************************

	Renegade Video Hardware

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class renegade
{
	
	UBytePtr renegade_textram;
	int renegade_scrollx;
	static struct tilemap *bg_tilemap;
	static struct tilemap *fg_tilemap;
	static int flipscreen;
	
	public static WriteHandlerPtr renegade_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( videoram.read(offset)!=data )
		{
			videoram.write(offset,data);
		offset = offset%(64*16);
			tilemap_mark_tile_dirty(bg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr renegade_textram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if( renegade_textram[offset]!=data )
		{
			renegade_textram[offset] = data;
			offset = offset%(32*32);
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr renegade_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flipscreen = !data;
		tilemap_set_flip( ALL_TILEMAPS, flipscreen?(TILEMAP_FLIPY|TILEMAP_FLIPX):0);
	} };
	
	public static WriteHandlerPtr renegade_scroll0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		renegade_scrollx = (renegade_scrollx&0xff00)|data;
	} };
	
	public static WriteHandlerPtr renegade_scroll1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		renegade_scrollx = (renegade_scrollx&0xFF)|(data<<8);
	} };
	
	public static GetTileInfoPtr get_bg_tilemap_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		const UINT8 *source = &videoram.read(tile_index);
		UINT8 attributes = source[0x400]; /* CCC??BBB */
		SET_TILE_INFO(
			1+(attributes&0x7), /* bank */
			source[0],  /* tile_number */
			attributes>>5 /* color */
		)
	} };
	
	public static GetTileInfoPtr get_fg_tilemap_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		const UINT8 *source = &renegade_textram[tile_index];
		UINT8 attributes = source[0x400];
		SET_TILE_INFO(
			0,
			(attributes&3)*256 + source[0], /* tile_number */
			attributes>>6
		)
	} };
	
	public static VhStartPtr renegade_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_bg_tilemap_info,tilemap_scan_rows,TILEMAP_OPAQUE,   16,16,64,16);
		fg_tilemap = tilemap_create(get_fg_tilemap_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!bg_tilemap || !fg_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
		tilemap_set_scrolldx( bg_tilemap, 256, 0 );
	
		tilemap_set_scrolldy( fg_tilemap, 0, 16 );
		tilemap_set_scrolldy( bg_tilemap, 0, 16 );
		return 0;
	} };
	
	static void draw_sprites( struct osd_bitmap *bitmap )
	{
		const struct rectangle *clip = &Machine.visible_area;
	
		UBytePtr source = spriteram;
		UBytePtr finish = source+96*4;
	
		while( source<finish )
		{
			int sy = 240-source[0];
			if( sy>=16 )
			{
			    int attributes = source[1]; /* SFCCBBBB */
			    int sx = source[3];
			    int sprite_number = source[2];
			    int sprite_bank = 9 + (attributes&0xF);
			    int color = (attributes>>4)&0x3;
			    int xflip = attributes&0x40;
	
			    if( sx>248 ) sx -= 256;
	
			    if ((attributes & 0x80) != 0){ /* big sprite */
			        drawgfx(bitmap,Machine.gfx[sprite_bank],
			            sprite_number+1,
			            color,
			            xflip,0,
			            sx,sy+16,
			            clip,TRANSPARENCY_PEN,0);
			    }
			    else
				{
			        sy += 16;
			    }
			    drawgfx(bitmap,Machine.gfx[sprite_bank],
			        sprite_number,
			        color,
			        xflip,0,
			        sx,sy,
			        clip,TRANSPARENCY_PEN,0);
			}
			source+=4;
		}
	}
	
	public static VhUpdatePtr renegade_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_scrollx( bg_tilemap, 0, renegade_scrollx );
		tilemap_set_scrolly( bg_tilemap, 0, 0 );
		tilemap_set_scrolly( fg_tilemap, 0, 0 );
	
		tilemap_update( ALL_TILEMAPS );
		if (palette_recalc())  tilemap_mark_all_pixels_dirty( ALL_TILEMAPS );
		tilemap_render( ALL_TILEMAPS );
		tilemap_draw( bitmap,bg_tilemap,0 );
		draw_sprites( bitmap );
		tilemap_draw( bitmap,fg_tilemap,0 );
	} };
}
