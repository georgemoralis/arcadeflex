/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;

public class ajax
{
	
	
	public static int ajax_priority;
	static int[] layer_colorbase=new int[3];
        static int sprite_colorbase,zoom_colorbase;
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color) 
	{
		code[0] |= ((color[0] & 0x0f) << 8) | (bank << 12);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xf0) >> 4);
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color, int[] priority) {
                /* priority bits:
		   4 over zoom (0 = have priority)
		   5 over B    (0 = have priority)
		   6 over A    (1 = have priority)
		   never over F
		*/
		priority[0] = 0xff00;							/* F = 8 */
		if (( color[0] & 0x10)!=0) priority[0] |= 0xf0f0;	/* Z = 4 */
		if ((~color[0] & 0x40)!=0) priority[0] |= 0xcccc;	/* A = 2 */
		if (( color[0] & 0x20)!=0) priority[0] |= 0xaaaa;	/* B = 1 */
		color[0] = sprite_colorbase + (color[0] & 0x0f);
            }
        };
        	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	static K051316_callbackProcPtr zoom_callback = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
                code[0] |= ((color[0] & 0x07) << 8);
		color[0] = zoom_colorbase + ((color[0] & 0x08) >> 3);
            }
        };
        	
	
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
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback) != 0)
			return 1;
		if (K051960_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback) != 0)
		{
			K052109_vh_stop();
			return 1;
		}
		if (K051316_vh_start_0(REGION_GFX3,7,zoom_callback) != 0)
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
		palette_used_colors.write((zoom_colorbase + 0) * 128, PALETTE_COLOR_TRANSPARENT);
		palette_used_colors.write((zoom_colorbase + 1) * 128, PALETTE_COLOR_TRANSPARENT);
		if (palette_recalc() != null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
/*TODO*///		fillbitmap(priority_bitmap,0,null);
	
		fillbitmap(bitmap,Machine.pens[0],Machine.visible_area);
		K052109_tilemap_draw(bitmap,2,1<<16);
		if (ajax_priority != 0)
		{
			/* basic layer order is B, zoom, A, F */
			K051316_zoom_draw_0(bitmap/*,4*/);
			K052109_tilemap_draw(bitmap,1,2<<16);
		}
		else
		{
			/* basic layer order is B, A, zoom, F */
			K052109_tilemap_draw(bitmap,1,2<<16);
			K051316_zoom_draw_0(bitmap/*,4*/);
		}
		K052109_tilemap_draw(bitmap,0,8<<16);
	
		K051960_sprites_draw(bitmap,-1,-1);
	} };
}
