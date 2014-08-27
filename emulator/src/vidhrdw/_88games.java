/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;
import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.memoryH.*;
import static vidhrdw.konamiic.*;
import static mame.sndintrf.*;
import static mame.cpuintrf.*;
import static mame.inputport.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.paletteH.*;
import static mame.common.*;

public class _88games
{
	
	
	public static int k88games_priority;
	
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
		priority[0] = (color[0] & 0x20) >> 5;	/* ??? */
		color[0] = sprite_colorbase + (color[0] & 0x0f);
	}};
	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	public static K051316_callbackProcPtr zoom_callback = new K051316_callbackProcPtr() { public void handler(int[] code,int[] color)
        {
		tile_info.flags = (color[0] & 0x40)!=0 ? (char)TILE_FLIPX : 0;
		code[0] |= ((color[0] & 0x07) << 8);
		color[0] = zoom_colorbase + ((color[0] & 0x38) >> 3) + ((color[0] & 0x80) >> 4);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr k88games_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 64;
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
	
	public static VhStopPtr k88games_vh_stop = new VhStopPtr() { public void handler() 
	{
		K052109_vh_stop();
		K051960_vh_stop();
		K051316_vh_stop_0();
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr k88games_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
	
		K052109_tilemap_update();
		K051316_tilemap_update_0();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
		/* set back pen for the zoom layer */
		for (i = 0;i < 16;i++)
			palette_used_colors.write((zoom_colorbase + i) * 16, PALETTE_COLOR_TRANSPARENT);
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		if (k88games_priority != 0)
		{
			K052109_tilemap_draw(bitmap,0,TILEMAP_IGNORE_TRANSPARENCY);
			K051960_sprites_draw(bitmap,1,1);
			K052109_tilemap_draw(bitmap,2,0);
			K052109_tilemap_draw(bitmap,1,0);
			K051960_sprites_draw(bitmap,0,0);
			K051316_zoom_draw_0(bitmap);
		}
		else
		{
			K052109_tilemap_draw(bitmap,2,TILEMAP_IGNORE_TRANSPARENCY);
			K051316_zoom_draw_0(bitmap);
			K051960_sprites_draw(bitmap,0,0);
			K052109_tilemap_draw(bitmap,1,0);
			K051960_sprites_draw(bitmap,1,1);
			K052109_tilemap_draw(bitmap,0,0);
		}
	} };
}
