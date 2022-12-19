/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class vastar
{
	
	
	
	UBytePtr vastar_bg1videoram;
	UBytePtr vastar_bg2videoram;
	UBytePtr vastar_fgvideoram;
	UBytePtr vastar_sprite_priority;
	UBytePtr vastar_bg1_scroll;
	UBytePtr vastar_bg2_scroll;
	
	static struct tilemap *fg_tilemap, *bg1_tilemap, *bg2_tilemap;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	
	public static VhConvertColorPromPtr vastar_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(char []palette, char []colortable, UBytePtr color_prom) 
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
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* green component */
			bit0 = (color_prom.read(Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			/* blue component */
			bit0 = (color_prom.read(2*Machine->drv->total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine->drv->total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine->drv->total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine->drv->total_colors)>> 3) & 0x01;
			*(palette++) =  0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			color_prom++;
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_fg_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_fgvideoram[tile_index + 0x800] | (vastar_fgvideoram[tile_index + 0x400] << 8);
		color = vastar_fgvideoram[tile_index];
		SET_TILE_INFO(0, code, color);
	} };
	
	public static GetTileInfoPtr get_bg1_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_bg1videoram[tile_index + 0x800] | (vastar_bg1videoram[tile_index] << 8);
		color = vastar_bg1videoram[tile_index + 0xc00];
		SET_TILE_INFO(4, code, color);
	} };
	
	public static GetTileInfoPtr get_bg2_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_bg2videoram[tile_index + 0x800] | (vastar_bg2videoram[tile_index] << 8);
		color = vastar_bg2videoram[tile_index + 0xc00];
		SET_TILE_INFO(3, code, color);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr vastar_vh_start = new VhStartPtr() { public int handler() 
	{
		fg_tilemap  = tilemap_create(get_fg_tile_info, tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg1_tilemap = tilemap_create(get_bg1_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg2_tilemap = tilemap_create(get_bg2_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!fg_tilemap || !bg1_tilemap || !bg2_tilemap)
			return 1;
	
		fg_tilemap.transparent_pen = 0;
		bg1_tilemap.transparent_pen = 0;
		bg2_tilemap.transparent_pen = 0;
	
		tilemap_set_scroll_cols(bg1_tilemap, 32);
		tilemap_set_scroll_cols(bg2_tilemap, 32);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr vastar_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vastar_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr vastar_bg1videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vastar_bg1videoram[offset] = data;
		tilemap_mark_tile_dirty(bg1_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr vastar_bg2videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vastar_bg2videoram[offset] = data;
		tilemap_mark_tile_dirty(bg2_tilemap,offset & 0x3ff);
	} };
	
	
	public static ReadHandlerPtr vastar_bg1videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vastar_bg1videoram[offset];
	} };
	
	public static ReadHandlerPtr vastar_bg2videoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return vastar_bg2videoram[offset];
	} };
	
	
	public static WriteHandlerPtr vastar_bg1_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vastar_bg1_scroll[offset] = data;
		tilemap_set_scrolly(bg1_tilemap,offset,data);
	} };
	
	public static WriteHandlerPtr vastar_bg2_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		vastar_bg2_scroll[offset] = data;
		tilemap_set_scrolly(bg2_tilemap,offset,data);
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct osd_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = 0; offs < spriteram_size; offs += 2)
		{
			int code, sx, sy, color, flipx, flipy;
	
	
			code = ((spriteram_3.read(offs)& 0xfc) >> 2) + ((spriteram_2.read(offs)& 0x01) << 6)
					+ ((offs & 0x20) << 2);
	
			sx = spriteram_3.read(offs + 1);
			sy = spriteram.read(offs);
			color = spriteram.read(offs + 1)& 0x3f;
			flipx = spriteram_3.read(offs)& 0x02;
			flipy = spriteram_3.read(offs)& 0x01;
	
			if (flip_screen != 0)
			{
				flipx = !flipx;
				flipy = !flipy;
			}
	
			if (spriteram_2.read(offs)& 0x08)	/* double width */
			{
				if (!flip_screen)
					sy = 224 - sy;
	
				drawgfx(bitmap,Machine.gfx[2],
						code/2,color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
				/* redraw with wraparound */
				drawgfx(bitmap,Machine.gfx[2],
						code/2,color,
						flipx,flipy,
						sx,sy+256,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
			else
			{
				if (!flip_screen)
					sy = 240 - sy;
	
				drawgfx(bitmap,Machine.gfx[1],
						code,color,
						flipx,flipy,
						sx,sy,
						&Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VhUpdatePtr vastar_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		tilemap_update(ALL_TILEMAPS);
		tilemap_render(ALL_TILEMAPS);
	
		switch (*vastar_sprite_priority)
		{
		case 0:
			tilemap_draw(bitmap, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY);
			draw_sprites(bitmap);
			tilemap_draw(bitmap, bg2_tilemap, 0);
			tilemap_draw(bitmap, fg_tilemap, 0);
			break;
	
		case 2:
			tilemap_draw(bitmap, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY);
			draw_sprites(bitmap);
			tilemap_draw(bitmap, bg1_tilemap, 0);
			tilemap_draw(bitmap, bg2_tilemap, 0);
			tilemap_draw(bitmap, fg_tilemap, 0);
			break;
	
		case 3:
			tilemap_draw(bitmap, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY);
			tilemap_draw(bitmap, bg2_tilemap, 0);
			tilemap_draw(bitmap, fg_tilemap, 0);
			draw_sprites(bitmap);
			break;
	
		default:
			logerror("Unimplemented priority %X\n", *vastar_sprite_priority);
			break;
		}
	} };
}
