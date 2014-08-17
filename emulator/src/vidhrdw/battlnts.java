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
import static mame.common.*;

public class battlnts
{
	
	static int spritebank;
	
	static int[] layer_colorbase=new int[2];
	
	/***************************************************************************
	
	  Callback for the K007342
	
	***************************************************************************/
	public static K007342_callbackProcPtr tile_callback = new K007342_callbackProcPtr() { public void handler(int tilemap,int bank,int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0x0f) << 9) | ((color[0] & 0x40) << 2);
		color[0] = layer_colorbase[layer];
	}};
	
	/***************************************************************************
	
	  Callback for the K007420
	
	***************************************************************************/
	public static K007420_callbackProcPtr sprite_callback = new K007420_callbackProcPtr() { public void handler(int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0xc0) << 2) | spritebank;
		code[0] = (code[0] << 2) | ((color[0] & 0x30) >> 4);
		color[0] = 0;
	}};
	
	public static WriteHandlerPtr battlnts_spritebank_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		spritebank = 1024 * (data & 1);
	} };
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr battlnts_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 0;
	
		if (K007342_vh_start(0,tile_callback)!=0)
		{
			/* Battlantis use this as Work RAM */
			K007342_tilemap_set_enable.handler(1, 0);
			return 1;
		}
	
		if (K007420_vh_start(1,sprite_callback)!=0)
		{
			K007420_vh_stop();
			return 1;
		}
	
		return 0;
	} };
	
	public static VhStopPtr battlnts_vh_stop = new VhStopPtr() { public void handler() 
	{
		K007342_vh_stop();
		K007420_vh_stop();
	} };
	
	/***************************************************************************
	
	  Screen Refresh
	
	***************************************************************************/
	
	public static VhUpdatePtr battlnts_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) {
	
		K007342_tilemap_update();
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render( ALL_TILEMAPS );
	
		K007342_tilemap_draw( bitmap, 0, TILEMAP_IGNORE_TRANSPARENCY );
		K007420_sprites_draw( bitmap );
		K007342_tilemap_draw( bitmap, 0, 1 | TILEMAP_IGNORE_TRANSPARENCY );
	} };
}
