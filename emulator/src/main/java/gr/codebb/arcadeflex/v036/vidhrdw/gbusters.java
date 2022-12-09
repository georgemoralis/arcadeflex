/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;


import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class gbusters
{
	
	
	
	public static int gbusters_priority;
	 static int[] layer_colorbase=new int[3];
        public static int sprite_colorbase;
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color)
        {
		/* (color & 0x02) is flip y handled internally by the 052109 */
		code[0] |= ((color[0] & 0x0d) << 8) | ((color[0] & 0x10) << 5) | (bank << 12);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
	}};
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	public static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority)
        {
		priority[0] = (color[0] & 0x30) >> 4;
		color[0] = sprite_colorbase + (color[0] & 0x0f);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr gbusters_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 48;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 16;
		sprite_colorbase = 32;
	
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback)!=0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback)!=0)
		{
			K052109_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr gbusters_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
	} };
	
	
	public static VhUpdatePtr gbusters_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		/* sprite priority 3 = disable */
		if (gbusters_priority != 0)
		{
	//		K051960_sprites_draw(bitmap,1,1);	/* are these used? */
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
			K051960_sprites_draw(bitmap,2,2);
			K052109_tilemap_draw(bitmap,1,0);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,0,0);
		}
		else
		{
	//		K051960_sprites_draw(bitmap,1,1);	/* are these used? */
			K052109_tilemap_draw(bitmap,1,TILEMAP_IGNORE_TRANSPARENCY);
			K051960_sprites_draw(bitmap,2,2);
			K052109_tilemap_draw(bitmap,2,0);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,0,0);
		}
	} };
}
