/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class lwings
{
	
	
	
	UBytePtr lwings_fgvideoram;
	UBytePtr lwings_bg1videoram;
	
	static int trojan_vh_type, bg2_image;
	static struct tilemap *fg_tilemap, *bg1_tilemap, *bg2_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	static UINT32 get_bg2_memory_offset( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		return (row * 0x800) | (col * 2);
	}
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = lwings_fgvideoram[tile_index];
		color = lwings_fgvideoram[tile_index + 0x400];
		SET_TILE_INFO(0, code + ((color & 0xc0) << 2), color & 0x0f);
		tile_info.flags = TILE_FLIPYX((color & 0x30) >> 4);
	} };
	
	public static GetTileInfoPtr lwings_get_bg1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = lwings_bg1videoram[tile_index];
		color = lwings_bg1videoram[tile_index + 0x400];
		SET_TILE_INFO(1, code + ((color & 0xe0) << 3), color & 0x07);
		tile_info.flags = TILE_FLIPYX((color & 0x18) >> 3);
	} };
	
	public static GetTileInfoPtr trojan_get_bg1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = lwings_bg1videoram[tile_index];
		color = lwings_bg1videoram[tile_index + 0x400];
		SET_TILE_INFO(1, code + ((color & 0xe0) << 3), color & 0x07);
		tile_info.flags = TILE_SPLIT((color & 0x08) >> 3);
		if ((color & 0x10) != 0)
			tile_info.flags |= TILE_FLIPX;
	} };
	
	public static GetTileInfoPtr get_bg2_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = memory_region(REGION_GFX5)[bg2_image * 0x20 + tile_index];
		color = memory_region(REGION_GFX5)[bg2_image * 0x20 + tile_index + 1];
		SET_TILE_INFO(3, code + ((color & 0x80) << 1), color & 0x07);
		tile_info.flags = TILE_FLIPYX((color & 0x30) >> 4);
	} };
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr lwings_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap  = tilemap_create(get_fg_tile_info,        tilemap_scan_rows,TILEMAP_TRANSPARENT, 8, 8,32,32);
		bg1_tilemap = tilemap_create(lwings_get_bg1_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE,     16,16,32,32);
	
		if (!fg_tilemap || !bg1_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 3;
	
		return 0;
	} };
	
	public static VhStartPtr trojan_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap  = tilemap_create(get_fg_tile_info,        tilemap_scan_rows,    TILEMAP_TRANSPARENT,                 8, 8,32,32);
		bg1_tilemap = tilemap_create(trojan_get_bg1_tile_info,tilemap_scan_cols,    TILEMAP_TRANSPARENT | TILEMAP_SPLIT,16,16,32,32);
		bg2_tilemap = tilemap_create(get_bg2_tile_info,       get_bg2_memory_offset,TILEMAP_OPAQUE,                     16,16,32,16);
	
		if (!fg_tilemap || !bg1_tilemap || !bg2_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 3;
		bg1_tilemap.transparent_pen = 0;
		bg1_tilemap.transmask[0] = 0xffff; /* split type 0 is totally transparent in front half */
		bg1_tilemap.transmask[1] = 0xf07f; /* split type 1 has pens 7-11 opaque in front half */
	
		trojan_vh_type = 0;
	
		return 0;
	} };
	
	public static VhStartPtr avengers_vh_start = new VhStartPtr() { public int handler() 
	{
		int result = trojan_vh_start();
	
		trojan_vh_type = 1;
	
		return result;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr lwings_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		lwings_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr lwings_bg1videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		lwings_bg1videoram[offset] = data;
		tilemap_mark_tile_dirty(bg1_tilemap,offset & 0x3ff);
	} };
	
	
	public static WriteHandlerPtr lwings_bg1_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrollx(bg1_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	public static WriteHandlerPtr lwings_bg1_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrolly(bg1_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	public static WriteHandlerPtr lwings_bg2_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrollx(bg2_tilemap,0,data);
	} };
	
	public static WriteHandlerPtr lwings_bg2_image_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (bg2_image != data)
		{
			bg2_image = data;
			tilemap_mark_all_tiles_dirty(bg2_tilemap);
		}
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	INLINE int is_sprite_on(int offs)
	{
		int sx,sy;
	
	
		sx = buffered_spriteram.read(offs + 3)- 0x100 * (buffered_spriteram.read(offs + 1)& 0x01);
		sy = buffered_spriteram.read(offs + 2);
	
		return sx && sy;
	}
	
	static void lwings_draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (is_sprite_on(offs))
			{
				int code,color,sx,sy,flipx,flipy;
	
	
				sx = buffered_spriteram.read(offs + 3)- 0x100 * (buffered_spriteram.read(offs + 1)& 0x01);
				sy = buffered_spriteram.read(offs + 2);
				code = buffered_spriteram.read(offs)| (buffered_spriteram.read(offs + 1)& 0xc0) << 2;
				color = (buffered_spriteram.read(offs + 1)& 0x38) >> 3;
				flipx = buffered_spriteram.read(offs + 1)& 0x02;
				flipy = buffered_spriteram.read(offs + 1)& 0x04;
	
				if (flip_screen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(bitmap,Machine.gfx[2],
						code,color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
			}
		}
	}
	
	static void trojan_draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (is_sprite_on(offs))
			{
				int code,color,sx,sy,flipx,flipy;
	
	
				sx = buffered_spriteram.read(offs + 3)- 0x100 * (buffered_spriteram.read(offs + 1)& 0x01);
				sy = buffered_spriteram.read(offs + 2);
				code = buffered_spriteram.read(offs)|
					   ((buffered_spriteram.read(offs + 1)& 0x20) << 4) |
					   ((buffered_spriteram.read(offs + 1)& 0x40) << 2) |
					   ((buffered_spriteram.read(offs + 1)& 0x80) << 3);
				color = (buffered_spriteram.read(offs + 1)& 0x0e) >> 1;
	
				if (trojan_vh_type != 0)
				{
					flipx = 0;										/* Avengers */
					flipy = ~buffered_spriteram.read(offs + 1)& 0x10;
				}
				else
				{
					flipx = buffered_spriteram.read(offs + 1)& 0x10;	/* Trojan */
					flipy = 1;
				}
	
				if (flip_screen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(bitmap,Machine.gfx[2],
						code,color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,15);
			}
		}
	}
	
	static void lwings_mark_sprite_colors(void)
	{
		int offs;
		UBytePtr sprite_colors;
	
	
		sprite_colors = palette_used_colors + Machine.drv.gfxdecodeinfo[2].color_codes_start;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (is_sprite_on(offs))
			{
				int color;
	
				color = (buffered_spriteram.read(offs + 1)& 0x38) >> 3;
				memset(sprite_colors + color * 16, PALETTE_COLOR_USED, 15);
			}
		}
	}
	
	static void trojan_mark_sprite_colors(void)
	{
		int offs;
		UBytePtr sprite_colors;
	
	
		sprite_colors = palette_used_colors + Machine.drv.gfxdecodeinfo[2].color_codes_start;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (is_sprite_on(offs))
			{
				int color;
	
				color = (buffered_spriteram.read(offs + 1)& 0x0e) >> 1;
				memset(sprite_colors + color * 16, PALETTE_COLOR_USED, 15);
			}
		}
	}
	
	public static VhUpdatePtr lwings_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		lwings_mark_sprite_colors();
	
		if (palette_recalc()) tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg1_tilemap,0);
		lwings_draw_sprites(bitmap);
		tilemap_draw(bitmap,fg_tilemap,0);
	} };
	
	public static VhUpdatePtr trojan_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
	
		palette_init_used_colors();
		trojan_mark_sprite_colors();
	
		if (palette_recalc()) tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		tilemap_draw(bitmap,bg2_tilemap,0);
		tilemap_draw(bitmap,bg1_tilemap,TILEMAP_BACK);
		trojan_draw_sprites(bitmap);
		tilemap_draw(bitmap,bg1_tilemap,TILEMAP_FRONT);
		tilemap_draw(bitmap,fg_tilemap,0);
	} };
	
	public static VhEofCallbackPtr lwings_eof_callback = new VhEofCallbackPtr() { public void handler() 
	{
		buffer_spriteram_w(0,0);
	} };
}
