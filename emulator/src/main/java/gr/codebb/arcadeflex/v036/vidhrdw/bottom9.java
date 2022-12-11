/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;


public class bottom9
{
	
	
	
	public static int bottom9_video_enable;
	
	static int[] layer_colorbase=new int[3];
        static int sprite_colorbase,zoom_colorbase;
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color)
        {
		code[0] |= (color[0] & 0x3f) << 8;
		color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	public static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority)
        {
		/* bit 4 = priority over zoom (0 = have priority) */
		/* bit 5 = priority over B (1 = have priority) */
		priority[0] = (color[0] & 0x30) >> 4;
		color[0] = sprite_colorbase + (color[0] & 0x0f);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	public static K051316_callbackProcPtr zoom_callback = new K051316_callbackProcPtr() { public void handler(int[] code,int[] color)
        {
		tile_info.flags = (color[0] & 0x40)!=0 ? (char)TILE_FLIPX : 0;
		code[0] |= ((color[0] & 0x03) << 8);
		color[0] = zoom_colorbase + ((color[0] & 0x3c) >> 2);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartHandlerPtr bottom9_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;	/* not used */
		layer_colorbase[1] = 0;
		layer_colorbase[2] = 16;
		sprite_colorbase = 32;
		zoom_colorbase = 48;
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback)!=0)
		{
			return 1;
		}
		if (K051960_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback)!=0)
		{
			K052109_vh_stop();
			return 1;
		}
		if (K051316_vh_start_0(REGION_GFX3,4,zoom_callback)!=0)
		{
			K052109_vh_stop();
			K051960_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopHandlerPtr bottom9_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
		K051316_vh_stop_0();
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdateHandlerPtr bottom9_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		K052109_tilemap_update();
		K051316_tilemap_update_0();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		/* set back pen for the zoom layer */
		for (i = 0;i < 16;i++)
			palette_used_colors.write((zoom_colorbase + i) * 16,PALETTE_COLOR_TRANSPARENT);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		/* note: FIX layer is not used */
		fillbitmap(bitmap,Machine.pens[layer_colorbase[1]],Machine.drv.visible_area);
	//	if (bottom9_video_enable != 0)
		{
			K051960_sprites_draw(bitmap,1,1);
			K051316_zoom_draw_0(bitmap);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,2,0);
			/* note that priority 3 is opposite to the basic layer priority! */
			/* (it IS used, but hopefully has no effect) */
			K051960_sprites_draw(bitmap,2,3);
			K052109_tilemap_draw(bitmap,1,0);
		}
	} };
}
