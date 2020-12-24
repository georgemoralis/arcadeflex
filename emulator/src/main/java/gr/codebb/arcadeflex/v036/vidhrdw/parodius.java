/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class parodius
{
	
	static int[] layer_colorbase=new int[3];
        static int sprite_colorbase,bg_colorbase;
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0x03) << 8) | ((color[0] & 0x10) << 6) | ((color[0] & 0x0c) << 9) | (bank << 13);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
	}};
	
	/***************************************************************************
	
	  Callbacks for the K053245
	
	***************************************************************************/
	
	public static K053245_callbackProcPtr sprite_callback = new K053245_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority)
        {
		priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
		color[0] = sprite_colorbase + (color[0] & 0x1f);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr parodius_vh_start = new VhStartPtr() { public int handler() 
	{
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback)!=0)
		{
			return 1;
		}
		if (K053245_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback)!=0)
		{
			K052109_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr parodius_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K053245_vh_stop();
	} };
	
	public static VhUpdatePtr parodius_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int[] pri=new int[3];
                int[] layer=new int[3];
	
	
		bg_colorbase       = K053251_get_palette_index(K053251_CI0);
		sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
		layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
		layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
		layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);
	
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K053245_mark_sprites_colors();
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		layer[0] = 0;
		pri[0] = K053251_get_priority(K053251_CI2);
		layer[1] = 1;
		pri[1] = K053251_get_priority(K053251_CI4);
		layer[2] = 2;
		pri[2] = K053251_get_priority(K053251_CI3);
	
		//sortlayers(layer,pri);
                if (pri[0] < pri[1]) 
		{ 
			int t; 
			t = pri[0]; pri[0] = pri[1]; pri[1] = t; 
			t = layer[0]; layer[0] = layer[1]; layer[1] = t; 
		}
                if (pri[0] < pri[2]) 
		{ 
			int t; 
			t = pri[0]; pri[0] = pri[2]; pri[2] = t; 
			t = layer[0]; layer[0] = layer[2]; layer[2] = t; 
		}
                if (pri[1] < pri[2]) 
		{ 
			int t; 
			t = pri[1]; pri[1] = pri[2]; pri[2] = t; 
			t = layer[1]; layer[1] = layer[2]; layer[2] = t; 
		}	
		fillbitmap(bitmap,Machine.pens[16 * bg_colorbase],Machine.drv.visible_area);
		K053245_sprites_draw(bitmap,pri[0]+1,0x3f);
		K052109_tilemap_draw(bitmap,layer[0],0);
		K053245_sprites_draw(bitmap,pri[1]+1,pri[0]);
		K052109_tilemap_draw(bitmap,layer[1],0);
		K053245_sprites_draw(bitmap,pri[2]+1,pri[1]);
		K052109_tilemap_draw(bitmap,layer[2],0);
		K053245_sprites_draw(bitmap,0,pri[2]);
	} };
}
