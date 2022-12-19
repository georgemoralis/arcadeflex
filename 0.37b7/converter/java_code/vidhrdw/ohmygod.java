/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class ohmygod
{
	
	
	UBytePtr ohmygod_videoram;
	
	static int spritebank;
	static struct tilemap *bg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		UINT16 code = READ_WORD(&ohmygod_videoram[4*tile_index+2]);
		UINT16 attr = READ_WORD(&ohmygod_videoram[4*tile_index]);
		SET_TILE_INFO(0,code,(attr & 0x0f00) >> 8)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr ohmygod_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_tilemap = tilemap_create(get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64,64);
	
		if (!bg_tilemap)
			return 1;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr ohmygod_videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	   return READ_WORD(&ohmygod_videoram[offset]);
	} };
	
	public static WriteHandlerPtr ohmygod_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int oldword = READ_WORD(&ohmygod_videoram[offset]);
		int newword = COMBINE_WORD(oldword,data);
	
		if (oldword != newword)
		{
			WRITE_WORD(&ohmygod_videoram[offset],newword);
			tilemap_mark_tile_dirty(bg_tilemap,offset/4);
		}
	} };
	
	public static WriteHandlerPtr ohmygod_spritebank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		spritebank = data & 0x8000;
	} };
	
	public static WriteHandlerPtr ohmygod_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0) tilemap_set_scrollx(bg_tilemap,0,data - 0x81ec);
		else tilemap_set_scrolly(bg_tilemap,0,data - 0x81ef);
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 8)
		{
			int sx,sy,code,color,flipx;
			UBytePtr sr;
	
			sr = spritebank ? spriteram_2 : spriteram;
	
			code = READ_WORD(&sr[offs+6]) & 0x0fff;
			color = READ_WORD(&sr[offs+4]) & 0x000f;
			sx = READ_WORD(&sr[offs+0]) - 29;
			sy = READ_WORD(&sr[offs+2]);
			if (sy >= 32768) sy -= 65536;
			flipx = READ_WORD(&sr[offs+6]) & 0x8000;
	
			drawgfx(bitmap,Machine.gfx[1],
					code,
					color,
					flipx,0,
					sx,sy,
					&Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VhUpdatePtr ohmygod_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,0);
		draw_sprites(bitmap);
	} };
}
