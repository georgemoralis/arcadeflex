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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K053247.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;

public class xmen {

    static int[] layer_colorbase = new int[3];
    static int sprite_colorbase, bg_colorbase;

    /**
     * *************************************************************************
     *
     * Callbacks for the K052109
     *
     **************************************************************************
     */
    public static K052109_callbackProcPtr xmen_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            /* (color & 0x02) is flip y handled internally by the 052109 */
            if (layer == 0) {
                color[0] = layer_colorbase[layer] + ((color[0] & 0xf0) >> 4);
            } else {
                color[0] = layer_colorbase[layer] + ((color[0] & 0x7c) >> 2);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the K053247
     *
     **************************************************************************
     */
    public static K053247_callbackProcPtr xmen_sprite_callback = new K053247_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            priority[0] = (color[0] & 0x00e0) >> 4;	/* ??????? */

            color[0] = sprite_colorbase + (color[0] & 0x001f);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr xmen_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, xmen_tile_callback) != 0) {
                return 1;
            }
            if (K053247_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, xmen_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }
            return 0;
        }
    };

    public static VhStopHandlerPtr xmen_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            K052109_vh_stop();
            K053247_vh_stop();
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
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
    public static VhUpdateHandlerPtr xmen_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI4);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI3);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI0);
            layer_colorbase[2] = K053251_get_palette_index(K053251_CI2);

            K052109_tilemap_update();

            palette_init_used_colors();
            K053247_mark_sprites_colors();
            palette_used_colors.write(16 * bg_colorbase + 1, palette_used_colors.read(16 * bg_colorbase + 1) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI3);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI0);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI2);

            //sortlayers(layer,pri);
            if (pri[0] < pri[1]) {
                int t;
                t = pri[0];
                pri[0] = pri[1];
                pri[1] = t;
                t = layer[0];
                layer[0] = layer[1];
                layer[1] = t;
            }
            if (pri[0] < pri[2]) {
                int t;
                t = pri[0];
                pri[0] = pri[2];
                pri[2] = t;
                t = layer[0];
                layer[0] = layer[2];
                layer[2] = t;
            }
            if (pri[1] < pri[2]) {
                int t;
                t = pri[1];
                pri[1] = pri[2];
                pri[2] = t;
                t = layer[1];
                layer[1] = layer[2];
                layer[2] = t;
            }

            /* note the '+1' in the background color!!! */
            fillbitmap(bitmap, Machine.pens[16 * bg_colorbase + 1], Machine.drv.visible_area);
            K053247_sprites_draw(bitmap, pri[0] + 1, 0x3f);
            K052109_tilemap_draw(bitmap, layer[0], 0);
            K053247_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K053247_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K053247_sprites_draw(bitmap, 0, pri[2]);
        }
    };
}
