/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;

import static platform.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static platform.ptrlib.*;
import static mame.commonH.*;
import static vidhrdw.konamiic.*;
import static vidhrdw.konami.K053247.*;
import static mame.memory.*;
import static mame.palette.*;
import static mame.paletteH.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;


public class simpsons
{
	
	static int bg_colorbase,sprite_colorbase;
        static int[] layer_colorbase=new int[3];
	public static UBytePtr simpsons_xtraram;
	
	
	
	/***************************************************************************
	
	  Callbacks for the K052109
	
	***************************************************************************/
	public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() { public void handler(int layer,int bank,int[] code,int[] color)
        {

		code[0] |= ((color[0] & 0x3f) << 8) | (bank << 14);
		color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K053247
	
	***************************************************************************/
	public static K053247_callbackProcPtr sprite_callback = new K053247_callbackProcPtr() { public void handler(int[] code,int[] color,int[] priority)
        {
		priority[0] = (color[0] & 0x0f80) >> 6;	/* ??????? */
		color[0] = sprite_colorbase + (color[0] & 0x001f);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr simpsons_vh_start = new VhStartPtr() { public int handler() 
	{
		if (K052109_vh_start(REGION_GFX1,0,1,2,3/*NORMAL_PLANE_ORDER*/,tile_callback)!=0)
			return 1;
		if (K053247_vh_start(REGION_GFX2,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback)!=0)
		{
			K052109_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr simpsons_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K053247_vh_stop();
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	/* useful function to sort the three tile layers by priority order */
	/*static void sortlayers(int *layer,int *pri)
	{
	#define SWAP(a,b) \
		if (pri[a] < pri[b]) \
		{ \
			int t; \
			t = pri[a]; pri[a] = pri[b]; pri[b] = t; \
			t = layer[a]; layer[a] = layer[b]; layer[b] = t; \
		}
	
		SWAP(0,1)
		SWAP(0,2)
		SWAP(1,2)
	}*/
	
	public static VhUpdatePtr simpsons_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int[] pri=new int[3];
                int[] layer=new int[3];
	
	
		bg_colorbase       = K053251_get_palette_index(K053251_CI0);
		sprite_colorbase   = K053251_get_palette_index(K053251_CI1);
		layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
		layer_colorbase[1] = K053251_get_palette_index(K053251_CI3);
		layer_colorbase[2] = K053251_get_palette_index(K053251_CI4);
	
		K052109_tilemap_update();
	
		palette_init_used_colors();
		K053247_mark_sprites_colors();
		palette_used_colors.write(16 * bg_colorbase,  palette_used_colors.read(16 * bg_colorbase) |PALETTE_COLOR_VISIBLE);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		layer[0] = 0;
		pri[0] = K053251_get_priority(K053251_CI2);
		layer[1] = 1;
		pri[1] = K053251_get_priority(K053251_CI3);
		layer[2] = 2;
		pri[2] = K053251_get_priority(K053251_CI4);
	
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
		K053247_sprites_draw(bitmap,pri[0]+1,0x3f);
		K052109_tilemap_draw(bitmap,layer[0],0);
		K053247_sprites_draw(bitmap,pri[1]+1,pri[0]);
		K052109_tilemap_draw(bitmap,layer[1],0);
		K053247_sprites_draw(bitmap,pri[2]+1,pri[1]);
		K052109_tilemap_draw(bitmap,layer[2],0);
		K053247_sprites_draw(bitmap,0,pri[2]);
	} };
	
	/***************************************************************************
	
	  Extra video banking
	
	***************************************************************************/
	
	public static ReadHandlerPtr simpsons_K052109_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		return K052109_r.handler(offset + 0x2000);
	} };
	
	public static WriteHandlerPtr simpsons_K052109_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		K052109_w.handler(offset + 0x2000,data);
	} };
	
	public static ReadHandlerPtr simpsons_K053247_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if (offset < 0x1000) return K053247_r.handler(offset);
		else return simpsons_xtraram.read(offset - 0x1000);
	} };
	
	public static WriteHandlerPtr simpsons_K053247_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		if (offset < 0x1000) K053247_w.handler(offset,data);
		else simpsons_xtraram.write(offset - 0x1000,data);
	} };
	
	public static void simpsons_video_banking(int bank)
	{
		if ((bank & 1) != 0)
		{
			cpu_setbankhandler_r(3,paletteram_r);
			cpu_setbankhandler_w(3,paletteram_xBBBBBGGGGGRRRRR_swap_w);
		}
		else
		{
			cpu_setbankhandler_r(3,K052109_r);
			cpu_setbankhandler_w(3,K052109_w);
		}
	
		if ((bank & 2) != 0)
		{
			cpu_setbankhandler_r(4,simpsons_K053247_r);
			cpu_setbankhandler_w(4,simpsons_K053247_w);
		}
		else
		{
			cpu_setbankhandler_r(4,simpsons_K052109_r);
			cpu_setbankhandler_w(4,simpsons_K052109_w);
		}
	}
}
