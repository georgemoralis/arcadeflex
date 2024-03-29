
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class raiden
{
	
	static struct tilemap *bg_layer,*fg_layer,*tx_layer;
	UBytePtr raiden_back_data,*raiden_fore_data,*raiden_scroll_ram;
	
	static int flipscreen,ALTERNATE;
	
	/******************************************************************************/
	
	public static ReadHandlerPtr raiden_background_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return raiden_back_data[offset];
	} };
	
	public static ReadHandlerPtr raiden_foreground_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return raiden_fore_data[offset];
	} };
	
	public static WriteHandlerPtr raiden_background_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		raiden_back_data[offset]=data;
		tilemap_mark_tile_dirty( bg_layer,offset/2);
	} };
	
	public static WriteHandlerPtr raiden_foreground_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		raiden_fore_data[offset]=data;
		tilemap_mark_tile_dirty( fg_layer,offset/2);
	} };
	
	public static WriteHandlerPtr raiden_text_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram.write(offset,data);
	tilemap_mark_tile_dirty( tx_layer,offset/2);
	} };
	
	public static WriteHandlerPtr raidena_text_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram.write(offset,data);
	tilemap_mark_tile_dirty( tx_layer,offset/2);
	} };
	
	public static GetTileInfoPtr get_back_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile=raiden_back_data[2*tile_index]+(raiden_back_data[2*tile_index+1]<<8);
		int color=tile >> 12;
	
		tile=tile&0xfff;
	
		SET_TILE_INFO(1,tile,color)
	} };
	
	public static GetTileInfoPtr get_fore_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile=raiden_fore_data[2*tile_index]+(raiden_fore_data[2*tile_index+1]<<8);
		int color=tile >> 12;
	
		tile=tile&0xfff;
	
		SET_TILE_INFO(2,tile,color)
	} };
	
	public static GetTileInfoPtr get_text_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile=videoram.read(2*tile_index)+((videoram.read(2*tile_index+1)&0xc0)<<2);
		int color=videoram.read(2*tile_index+1)&0xf;
	
		SET_TILE_INFO(0,tile,color)
	} };
	
	public static GetTileInfoPtr get_text_alt_tile_info = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int tile=videoram.read(2*tile_index)+((videoram.read(2*tile_index+1)&0xc0)<<2);
		int color=videoram.read(2*tile_index+1)&0xf;
	
		SET_TILE_INFO(0,tile,color)
	} };
	
	public static VhStartPtr raiden_vh_start = new VhStartPtr() { public int handler() 
	{
		bg_layer = tilemap_create(get_back_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE,     16,16,32,32);
		fg_layer = tilemap_create(get_fore_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,16,16,32,32);
	
		/* Weird - Raiden (Alternate) has different char format! */
		if (!strcmp(Machine.gamedrv.name,"raiden"))
			ALTERNATE=0;
		else
			ALTERNATE=1;
	
		/* Weird - Raiden (Alternate) has different char format! */
		if (!ALTERNATE)
			tx_layer = tilemap_create(get_text_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,32,32);
		else
			tx_layer = tilemap_create(get_text_alt_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!bg_layer || !fg_layer || !tx_layer)
			return 1;
	
		fg_layer.transparent_pen = 15;
		tx_layer.transparent_pen = 15;
	
		return 0;
	} };
	
	public static WriteHandlerPtr raiden_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* All other bits unknown - could be playfield enables */
	
		/* Flipscreen */
		if (offset==6) {
			flipscreen=data&0x2;
			tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		}
	} };
	
	static void draw_sprites(struct osd_bitmap *bitmap,int pri_mask)
	{
		int offs,fx,fy,x,y,color,sprite;
	
		for (offs = 0x1000-8;offs >= 0;offs -= 8)
		{
			/* Don't draw empty sprite table entries */
			if (buffered_spriteram.read(offs+7)!=0xf) continue;
			if (buffered_spriteram[offs+0]==0xf0f) continue;
			if (!(pri_mask&buffered_spriteram.read(offs+5))) continue;
	
			fx= buffered_spriteram.read(offs+1)&0x20;
			fy= buffered_spriteram.read(offs+1)&0x40;
			y = buffered_spriteram.read(offs+0);
			x = buffered_spriteram.read(offs+4);
	
			if (buffered_spriteram.read(offs+5)&1) x=0-(0x100-x);
	
			color = buffered_spriteram.read(offs+1)&0xf;
			sprite = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			sprite &= 0x0fff;
	
			if (flipscreen != 0) {
				x=240-x;
				y=240-y;
				if (fx != 0) fx=0; else fx=1;
				if (fy != 0) fy=0; else fy=1;
			}
	
			drawgfx(bitmap,Machine.gfx[3],
					sprite,
					color,fx,fy,x,y,
					&Machine.visible_area,TRANSPARENCY_PEN,15);
		}
	}
	
	public static VhUpdatePtr raiden_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int color,offs,sprite;
		int colmask[16],i,pal_base;
	
		/* Setup the tilemaps, alternate version has different scroll positions */
		if (!ALTERNATE) {
			tilemap_set_scrollx( bg_layer,0, ((raiden_scroll_ram[1]<<8)+raiden_scroll_ram[0]) );
			tilemap_set_scrolly( bg_layer,0, ((raiden_scroll_ram[3]<<8)+raiden_scroll_ram[2]) );
			tilemap_set_scrollx( fg_layer,0, ((raiden_scroll_ram[5]<<8)+raiden_scroll_ram[4]) );
			tilemap_set_scrolly( fg_layer,0, ((raiden_scroll_ram[7]<<8)+raiden_scroll_ram[6]) );
		}
		else {
			tilemap_set_scrolly( bg_layer,0, ((raiden_scroll_ram[0x02]&0x30)<<4)+((raiden_scroll_ram[0x04]&0x7f)<<1)+((raiden_scroll_ram[0x04]&0x80)>>7) );
			tilemap_set_scrollx( bg_layer,0, ((raiden_scroll_ram[0x12]&0x30)<<4)+((raiden_scroll_ram[0x14]&0x7f)<<1)+((raiden_scroll_ram[0x14]&0x80)>>7) );
			tilemap_set_scrolly( fg_layer,0, ((raiden_scroll_ram[0x22]&0x30)<<4)+((raiden_scroll_ram[0x24]&0x7f)<<1)+((raiden_scroll_ram[0x24]&0x80)>>7) );
			tilemap_set_scrollx( fg_layer,0, ((raiden_scroll_ram[0x32]&0x30)<<4)+((raiden_scroll_ram[0x34]&0x7f)<<1)+((raiden_scroll_ram[0x34]&0x80)>>7) );
		}
	
		tilemap_update(ALL_TILEMAPS);
	
		/* Build the dynamic palette */
		palette_init_used_colors();
	
		/* Sprites */
		pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
		for (color = 0;color < 16;color++) colmask[color] = 0;
		for (offs = 0;offs <0x1000;offs += 8)
		{
			color = buffered_spriteram.read(offs+1)&0xf;
			sprite = buffered_spriteram.read(offs+2)+(buffered_spriteram.read(offs+3)<<8);
			sprite &= 0x0fff;
			colmask[color] |= Machine.gfx[3].pen_usage[sprite];
		}
		for (color = 0;color < 16;color++)
		{
			for (i = 0;i < 15;i++)
			{
				if (colmask[color] & (1 << i))
					palette_used_colors[pal_base + 16 * color + i] = PALETTE_COLOR_USED;
			}
		}
	
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
		tilemap_draw(bitmap,bg_layer,0);
	
		/* Draw sprites underneath foreground */
		draw_sprites(bitmap,0x40);
		tilemap_draw(bitmap,fg_layer,0);
	
		/* Rest of sprites */
		draw_sprites(bitmap,0x80);
	
		/* Text layer */
		tilemap_draw(bitmap,tx_layer,0);
	} };
	
	
}
