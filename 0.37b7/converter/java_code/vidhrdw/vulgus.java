/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class vulgus
{
	
	UBytePtr vulgus_fgvideoram,*vulgus_bgvideoram;
	UBytePtr vulgus_scroll_low,*vulgus_scroll_high;
	
	static data_t vulgus_palette_bank;
	static struct tilemap *fg_tilemap, *bg_tilemap;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	
	public static VhConvertColorPromPtr vulgus_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the lookup table */
	
	
		/* characters use colors 32-47 (?) */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = *(color_prom++) + 32;
	
		/* sprites use colors 16-31 */
		for (i = 0;i < TOTAL_COLORS(2);i++)
		{
			COLOR(2,i) = *(color_prom++) + 16;
		}
	
		/* background tiles use colors 0-15, 64-79, 128-143, 192-207 in four banks */
		for (i = 0;i < TOTAL_COLORS(1)/4;i++)
		{
			COLOR(1,i) = *color_prom;
			COLOR(1,i+32*8) = *color_prom + 64;
			COLOR(1,i+2*32*8) = *color_prom + 128;
			COLOR(1,i+3*32*8) = *color_prom + 192;
			color_prom++;
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vulgus_fgvideoram[tile_index];
		color = vulgus_fgvideoram[tile_index + 0x400];
		SET_TILE_INFO(0, code + ((color & 0x80) << 1), color & 0x3f);
	} };
	
	public static GetTileInfoPtr get_bg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vulgus_bgvideoram[tile_index];
		color = vulgus_bgvideoram[tile_index + 0x400];
		SET_TILE_INFO(1, code + ((color & 0x80) << 1), (color & 0x1f) + (0x20 * vulgus_palette_bank));
		tile_info.flags = TILE_FLIPYX((color & 0x60) >> 5);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr vulgus_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT_COLOR, 8, 8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE           ,16,16,32,32);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 47;
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr vulgus_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vulgus_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr vulgus_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vulgus_bgvideoram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
	} };
	
	
	public static WriteHandlerPtr vulgus_c804_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* bits 0 and 1 are coin counters */
		coin_counter_w.handler(0, data & 0x01);
		coin_counter_w.handler(1, data & 0x02);
	
		/* bit 7 flips screen */
		flip_screen_w(offset, data & 0x80);
	} };
	
	
	public static WriteHandlerPtr vulgus_palette_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (vulgus_palette_bank != data)
			tilemap_mark_all_tiles_dirty(bg_tilemap);
	
		vulgus_palette_bank = data;
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int code,i,col,sx,sy,dir;
	
	
			code = spriteram.read(offs);
			col = spriteram.read(offs + 1)& 0x0f;
			sx = spriteram.read(offs + 3);
			sy = spriteram.read(offs + 2);
			dir = 1;
			if (flip_screen != 0)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				dir = -1;
			}
	
			i = (spriteram.read(offs + 1)& 0xc0) >> 6;
			if (i == 2) i = 3;
	
			do
			{
				drawgfx(bitmap,Machine.gfx[2],
						code + i,
						col,
						flip_screen,flip_screen,
						sx, sy + 16 * i * dir,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
	
				/* draw again with wraparound */
				drawgfx(bitmap,Machine.gfx[2],
						code + i,
						col,
						flip_screen,flip_screen,
						sx, sy + 16 * i * dir -  dir * 256,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
				i--;
			} while (i >= 0);
		}
	}
	
	public static VhUpdatePtr vulgus_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_set_scrollx(bg_tilemap, 0, vulgus_scroll_low[1] + 256 * vulgus_scroll_high[1]);
		tilemap_set_scrolly(bg_tilemap, 0, vulgus_scroll_low[0] + 256 * vulgus_scroll_high[0]);
	
		tilemap_update(ALL_TILEMAPS);
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg_tilemap,0);
		draw_sprites(bitmap);
		tilemap_draw(bitmap,fg_tilemap,0);
	} };
}