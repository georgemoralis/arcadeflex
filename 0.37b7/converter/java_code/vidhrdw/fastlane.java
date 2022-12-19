/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class fastlane
{
	
	UBytePtr fastlane_k007121_regs,*fastlane_videoram1,*fastlane_videoram2;
	static struct tilemap *layer0, *layer1;
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoPtr get_tile_info0 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = fastlane_videoram1[tile_index];
		int code = fastlane_videoram1[tile_index + 0x400];
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[0][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(0,code+bank*256,1);
	} };
	
	public static GetTileInfoPtr get_tile_info1 = new GetTileInfoPtr() { public void handler(int tile_index) 
	{
		int attr = fastlane_videoram2[tile_index];
		int code = fastlane_videoram2[tile_index + 0x400];
		int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
		int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
		int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
		int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
		int bank = ((attr & 0x80) >> 7) |
				((attr >> (bit0+2)) & 0x02) |
				((attr >> (bit1+1)) & 0x04) |
				((attr >> (bit2  )) & 0x08) |
				((attr >> (bit3-1)) & 0x10) |
				((K007121_ctrlram[0][0x03] & 0x01) << 5);
		int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;
	
		bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);
	
		SET_TILE_INFO(0,code+bank*256,0);
	} };
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr fastlane_vh_start = new VhStartPtr() { public int handler() 
	{
		layer0 = tilemap_create(get_tile_info0,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
		layer1 = tilemap_create(get_tile_info1,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
	
		tilemap_set_scroll_rows( layer0, 32 );
	
		if (!layer0 || !layer1)
			return 1;
	
		{
			struct rectangle clip = Machine.visible_area;
			clip.min_x += 40;
			tilemap_set_clip(layer0,&clip);
	
			clip.max_x = 39;
			clip.min_x = 0;
			tilemap_set_clip(layer1,&clip);
	
			return 0;
		}
	} };
	
	/***************************************************************************
	
	  Memory Handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr fastlane_vram1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (fastlane_videoram1[offset] != data)
		{
			tilemap_mark_tile_dirty(layer0,offset & 0x3ff);
			fastlane_videoram1[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr fastlane_vram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (fastlane_videoram2[offset] != data)
		{
			tilemap_mark_tile_dirty(layer1,offset & 0x3ff);
			fastlane_videoram2[offset] = data;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Screen Refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr fastlane_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i, xoffs;
	
		/* set scroll registers */
		xoffs = K007121_ctrlram[0][0x00];
		for( i=0; i<32; i++ ){
			tilemap_set_scrollx(layer0, i, fastlane_k007121_regs[0x20 + i] + xoffs - 40);
		}
		tilemap_set_scrolly( layer0, 0, K007121_ctrlram[0][0x02] );
	
		tilemap_update( ALL_TILEMAPS );
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
		tilemap_render( ALL_TILEMAPS );
	
		tilemap_draw(bitmap,layer0,0);
		K007121_sprites_draw(0,bitmap,spriteram,0,40,0,-1);
		tilemap_draw(bitmap,layer1,0);
	} };
}
