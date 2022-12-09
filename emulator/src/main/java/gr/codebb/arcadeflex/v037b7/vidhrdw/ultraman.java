/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.driverH.*;
import arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v037b7.drivers.ultraman.ultraman_regs;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;

public class ultraman
{
	
	static final int SPRITEROM_MEM_REGION   = REGION_GFX1;
	static final int ZOOMROM0_MEM_REGION    = REGION_GFX2;
	static final int ZOOMROM1_MEM_REGION    = REGION_GFX3;
	static final int ZOOMROM2_MEM_REGION    = REGION_GFX4;
	
	static int sprite_colorbase;
        static int[] zoom_colorbase = new int[3];
	
	//extern UBytePtr  ultraman_regs;
	
	/***************************************************************************
	
	  Callbacks for the K051960
	
	***************************************************************************/
	
	static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color, int[] priority) {
		priority[0] = (color[0] & 0x80) >> 7;
		color[0] = sprite_colorbase + ((color[0] & 0x7e) >> 1);
/*TODO*///		shadow = 0;
            }
        };
	
	
	/***************************************************************************
	
	  Callbacks for the K051316
	
	***************************************************************************/
	
	static K051316_callbackProcPtr zoom_callback_0 = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
                int data = ultraman_regs.READ_WORD(0x18);
		code[0] |= ((color[0] & 0x07) << 8) | (data & 0x02) << 10;
		color[0] = zoom_colorbase[0] + ((color[0] & 0xf8) >> 3);
            }
        };
	
	static K051316_callbackProcPtr zoom_callback_1 = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
		int data = ultraman_regs.READ_WORD(0x18);
		code[0] |= ((color[0] & 0x07) << 8) | (data & 0x08) << 8;
		color[0] = zoom_colorbase[1] + ((color[0] & 0xf8) >> 3);
            }
        };
	
	static K051316_callbackProcPtr zoom_callback_2 = new K051316_callbackProcPtr() {
            @Override
            public void handler(int[] code, int[] color) {
		int data = ultraman_regs.READ_WORD(0x18);
		code[0] |= ((color[0] & 0x07) << 8) | (data & 0x20) << 6;
		color[0] = zoom_colorbase[2] + ((color[0] & 0xf8) >> 3);
            }
        };
	
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VhStartHandlerPtr ultraman_vh_start = new VhStartHandlerPtr() { public int handler() 
	{
		sprite_colorbase = 192;
		zoom_colorbase[0] = 0;
		zoom_colorbase[1] = 64;
		zoom_colorbase[2] = 128;
	
		if (K051960_vh_start(SPRITEROM_MEM_REGION,0,1,2,3/*NORMAL_PLANE_ORDER*/,sprite_callback) != 0)
		{
			return 1;
		}
	
		if (K051316_vh_start_0(ZOOMROM0_MEM_REGION,4,zoom_callback_0) != 0)
		{
			K051960_vh_stop();
			return 1;
		}
	
		if (K051316_vh_start_1(ZOOMROM1_MEM_REGION,4,zoom_callback_1) != 0)
		{
			K051960_vh_stop();
			K051316_vh_stop_0();
			return 1;
		}
	
		if (K051316_vh_start_2(ZOOMROM2_MEM_REGION,4,zoom_callback_2) != 0)
		{
			K051960_vh_stop();
			K051316_vh_stop_0();
			K051316_vh_stop_1();
			return 1;
		}
	
		K051316_set_offset(0, 8, 0);
		K051316_set_offset(1, 8, 0);
		K051316_set_offset(2, 8, 0);
	
		return 0;
	} };
	
	public static VhStopHandlerPtr ultraman_vh_stop = new VhStopHandlerPtr() { public void handler() 
	{
		K051960_vh_stop();
		K051316_vh_stop_0();
		K051316_vh_stop_1();
		K051316_vh_stop_2();
	} };
	
	
	
	/***************************************************************************
	
		Display Refresh
	
	***************************************************************************/
	
	public static VhUpdateHandlerPtr ultraman_vh_screenrefresh = new VhUpdateHandlerPtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		int i;
	
		K051316_tilemap_update_0();
		K051316_tilemap_update_1();
		K051316_tilemap_update_2();
	
		palette_init_used_colors();
		K051960_mark_sprites_colors();
	
		/* set transparent pens for the K051316 */
		for (i = 0;i < 64;i++){
			palette_used_colors.write((zoom_colorbase[0] + i) * 16, PALETTE_COLOR_TRANSPARENT);
			palette_used_colors.write((zoom_colorbase[1] + i) * 16, PALETTE_COLOR_TRANSPARENT);
		}
	
		if (palette_recalc() != null)
			tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
	
		tilemap_render(ALL_TILEMAPS);
	
		K051316_zoom_draw_2(bitmap);
		K051316_zoom_draw_1(bitmap);
		K051960_sprites_draw(bitmap,0,0);
		K051316_zoom_draw_0(bitmap);
		K051960_sprites_draw(bitmap,1,1);
	} };
}
