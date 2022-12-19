/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class thunderx
{
	
	
	int scontra_priority;
	static int layer_colorbase[3],sprite_colorbase;
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		*code |= ((*color & 0x1f) << 8) | (bank << 13);
		*color = layer_colorbase[layer] + ((*color & 0xe0) >> 5);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	public static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority) 
	{
		/* Sprite priority 1 means appear behind background, used only to mask sprites */
		/* in the foreground */
		/* Sprite priority 3 means don't draw (not used) */
		switch (*color & 0x30)
		{
			case 0x00: *priority_mask = 0xf0; break;
			case 0x10: *priority_mask = 0xf0|0xcc|0xaa; break;
			case 0x20: *priority_mask = 0xf0|0xcc; break;
			case 0x30: *priority_mask = 0xffff; break;
		}
	
		*color = sprite_colorbase + (*color & 0x0f);
	} };
	
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr scontra_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 48;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 16;
		sprite_colorbase = 32;
	
		if (K052109_vh_start(REGION_GFX1,NORMAL_PLANE_ORDER,tile_callback))
			return 1;
		if (K051960_vh_start(REGION_GFX2,NORMAL_PLANE_ORDER,sprite_callback))
		{
			K052109_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr scontra_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr scontra_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		fillbitmap(priority_bitmap,0,NULL);
	
		/* The background color is always from layer 1 - but it's always black anyway */
	//	fillbitmap(bitmap,Machine.pens[16 * layer_colorbase[1]],&Machine.visible_area);
		if (scontra_priority != 0)
		{
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY|(1<<16));
			K052109_tilemap_draw(bitmap,1,2<<16);
		}
		else
		{
			K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY|(1<<16));
			K052109_tilemap_draw(bitmap,2,2<<16);
		}
		K052109_tilemap_draw(bitmap,0,4<<16);
	
		K051960_sprites_draw(bitmap,-1,-1);
	} };
}
