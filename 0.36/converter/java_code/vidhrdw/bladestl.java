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

public class bladestl
{
	
	static int layer_colorbase[2];
	extern int bladestl_spritebank;
	
	public static VhConvertColorPromPtr bladestl_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		/* build the lookup table for sprites. Palette is dynamic. */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = 0x20 + (*(color_prom++) & 0x0f);
	} };
	
	/***************************************************************************
	
	  Callback for the K007342
	
	***************************************************************************/
	
	static void tile_callback(int layer, int bank, int *code, int *color)
	{
		*code |= ((*color & 0x0f) << 8) | ((*color & 0x40) << 6);
		*color = layer_colorbase[layer];
	}
	
	/***************************************************************************
	
	  Callback for the K007420
	
	***************************************************************************/
	
	static void sprite_callback(int *code,int *color)
	{
		*code |= ((*color & 0xc0) << 2) + bladestl_spritebank;
		*code = (*code << 2) | ((*color & 0x30) >> 4);
		*color = 0 + (*color & 0x0f);
	}
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr bladestl_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 1;
	
		if (K007342_vh_start(0,tile_callback))
		{
			return 1;
		}
	
		if (K007420_vh_start(1,sprite_callback))
		{
			K007420_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr bladestl_vh_stop = new VhStopPtr() { public void handler() 
	{
		K007342_vh_stop();
		K007420_vh_stop();
	} };
	
	/***************************************************************************
	
	  Screen Refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr bladestl_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		K007342_tilemap_update();
	
		if (palette_recalc())
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render( ALL_TILEMAPS );
	
		K007342_tilemap_draw( bitmap, 1, TILEMAP_IGNORE_TRANSPARENCY );
		K007420_sprites_draw( bitmap );
		K007342_tilemap_draw( bitmap, 1, 1 | TILEMAP_IGNORE_TRANSPARENCY );
		K007342_tilemap_draw( bitmap, 0, 0 );
		K007342_tilemap_draw( bitmap, 0, 1 );
	} };
}
