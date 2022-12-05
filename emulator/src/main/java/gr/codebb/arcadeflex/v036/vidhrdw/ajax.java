/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;

public class ajax
{
	
	
	public/*unsigned char*/static int ajax_priority;
	static int[] layer_colorbase=new int[3];
        static int sprite_colorbase,zoom_colorbase;
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0x0f) << 8) | (bank << 12);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xf0) >> 4);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	public static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority)
        {
		/* priority bits:
		   4 over zoom (0 = have priority)
		   5 over B    (0 = have priority) - is this used?
		   6 over A    (1 = have priority)
		*/
		priority[0] = (color[0] & 0x70) >> 4;
		color[0] = sprite_colorbase + (color[0] & 0x0f);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	public static K051316_callbackProcPtr zoom_callback = new K051316_callbackProcPtr() { public void handler(int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0x07) << 8);
		color[0] = zoom_colorbase + ((color[0] & 0x08) >> 3);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr ajax_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 64;
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 32;
		sprite_colorbase = 16;
		zoom_colorbase = 6;	/* == 48 since it's 7-bit graphics */
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback)!=0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback)!=0)
		{
			K052109_vh_stop();
			return 1;
		}
		if (K051316_vh_start_0(REGION_GFX3,7,zoom_callback)!=0)
		{
			K052109_vh_stop();
			K051960_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr ajax_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
		K051316_vh_stop_0();
	} };
	
	
	
	/***************************************************************************
	
		Display Refresh
	
	***************************************************************************/
	public static VhUpdatePtr ajax_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K052109_tilemap_update();
		K051316_tilemap_update_0();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		/* set back pen for the zoom layer */
		palette_used_colors.write((zoom_colorbase + 0) * 128,PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write((zoom_colorbase + 1) * 128, PALETTE_COLOR_TRANSPARENT);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		/* sprite priority bits:
		   0 over zoom (0 = have priority)
		   1 over B    (0 = have priority)
		   2 over A    (1 = have priority)
		*/
		if (ajax_priority != 0)
		{
			/* basic layer order is B, zoom, A, F */
	
			/* pri = 2 have priority over zoom, not over A and B - is this used? */
			/* pri = 3 have priority over nothing - is this used? */
	//		K051960_sprites_draw(bitmap,2,3);
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
			/* pri = 1 have priority over B, not over zoom and A - is this used? */
	//		K051960_sprites_draw(bitmap,1,1);
			K051316_zoom_draw_0(bitmap);
			/* pri = 0 have priority over zoom and B, not over A */
			/* the game seems to just use pri 0. */
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,1,0);
			/* pri = 4 have priority over zoom, A and B */
			/* pri = 5 have priority over A and B, not over zoom - OPPOSITE TO BASIC ORDER! (stage 6 boss) */
			K051960_sprites_draw(bitmap,4,5);
			/* pri = 6 have priority over zoom and A, not over B - is this used? */
			/* pri = 7 have priority over A, not over zoom and B - is this used? */
	//		K051960_sprites_draw(bitmap,5,7);
			K052109_tilemap_draw(bitmap,0,0);
		}
		else
		{
			/* basic layer order is B, A, zoom, F */
	
			/* pri = 2 have priority over zoom, not over A and B - is this used? */
			/* pri = 3 have priority over nothing - is this used? */
	//		K051960_sprites_draw(bitmap,2,3);
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
			/* pri = 0 have priority over zoom and B, not over A - OPPOSITE TO BASIC ORDER! */
			/* pri = 1 have priority over B, not over zoom and A */
			/* the game seems to just use pri 0. */
			K051960_sprites_draw(bitmap,0,1);
			K052109_tilemap_draw(bitmap,1,0);
			K051316_zoom_draw_0(bitmap);
			/* pri = 4 have priority over zoom, A and B */
			K051960_sprites_draw(bitmap,4,4);
			/* pri = 5 have priority over A and B, not over zoom - is this used? */
			/* pri = 6 have priority over zoom and A, not over B - is this used? */
			/* pri = 7 have priority over A, not over zoom and B - is this used? */
	//		K051960_sprites_draw(bitmap,5,7);
			K052109_tilemap_draw(bitmap,0,0);
		}
	}};
}
