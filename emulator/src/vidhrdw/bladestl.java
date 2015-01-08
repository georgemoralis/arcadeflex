/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package vidhrdw;
import static arcadeflex.ptrlib.*;
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
import static vidhrdw.konami.K007342.*;
import static vidhrdw.konami.K007420.*;
import static mame.sndintrf.*;
import static mame.cpuintrf.*;
import static mame.inputport.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.common.*;
public class bladestl
{
	
	static int[] layer_colorbase=new int[2];
	public static int bladestl_spritebank;
	
        static int TOTAL_COLORS(int gfxn) 
        {
            return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
        }
	public static VhConvertColorPromPtr bladestl_vh_convert_color_prom = new VhConvertColorPromPtr() { public void handler(UByte []palette, char []colortable, UBytePtr color_prom) 
	{
		int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		//#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		/* build the lookup table for sprites. Palette is dynamic. */
		for (i = 0;i < TOTAL_COLORS(1);i++)
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char)(0x20 + ((color_prom.readinc()) & 0x0f));
	} };
	
	/***************************************************************************
	
	  Callback for the K007342
	
	***************************************************************************/
	
	public static K007342_callbackProcPtr tile_callback = new K007342_callbackProcPtr() { public void handler(int tilemap,int bank,int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0x0f) << 8) | ((color[0] & 0x40) << 6);
		color[0] = layer_colorbase[layer];
	}};
	
	/***************************************************************************
	
	  Callback for the K007420
	
	***************************************************************************/
	public static K007420_callbackProcPtr sprite_callback = new K007420_callbackProcPtr() { public void handler(int[] code,int[] color)
        {
		code[0] |= ((color[0] & 0xc0) << 2) + bladestl_spritebank;
		code[0] = (code[0] << 2) | ((color[0] & 0x30) >> 4);
		color[0] = 0 + (color[0] & 0x0f);
	}};
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartPtr bladestl_vh_start = new VhStartPtr() { public int handler() 
	{
		layer_colorbase[0] = 0;
		layer_colorbase[1] = 1;
	
		if (K007342_vh_start(0,tile_callback)!=0)
		{
			return 1;
		}
	
		if (K007420_vh_start(1,sprite_callback)!=0)
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
	
		if (palette_recalc()!=null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render( ALL_TILEMAPS );
	
		K007342_tilemap_draw( bitmap, 1, TILEMAP_IGNORE_TRANSPARENCY );
		K007420_sprites_draw( bitmap );
		K007342_tilemap_draw( bitmap, 1, 1 | TILEMAP_IGNORE_TRANSPARENCY );
		K007342_tilemap_draw( bitmap, 0, 0 );
		K007342_tilemap_draw( bitmap, 0, 1 );
	} };
}
