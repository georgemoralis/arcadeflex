/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 03/02/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.konamiic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class parodius {

    static int[] layer_colorbase = new int[3];
    static int sprite_colorbase, bg_colorbase;

    /**
     * *************************************************************************
     *
     * Callbacks for the K052109
     *
     **************************************************************************
     */
    public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            code[0] |= ((color[0] & 0x03) << 8) | ((color[0] & 0x10) << 6) | ((color[0] & 0x0c) << 9) | (bank << 13);
            color[0] = layer_colorbase[layer] + ((color[0] & 0xe0) >> 5);
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the K053245
     *
     **************************************************************************
     */
    public static K053245_callbackProcPtr sprite_callback = new K053245_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            priority[0] = 0x20 | ((color[0] & 0x60) >> 2);
            color[0] = sprite_colorbase + (color[0] & 0x1f);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr parodius_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tile_callback) != 0) {
                return 1;
            }
            if (K053245_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr parodius_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            K052109_vh_stop();
            K053245_vh_stop();
        }
    };

    public static VhUpdateHandlerPtr parodius_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int[] pri = new int[3];
            int[] layer = new int[3];

            bg_colorbase = K053251_get_palette_index(K053251_CI0);
            sprite_colorbase = K053251_get_palette_index(K053251_CI1);
            layer_colorbase[0] = K053251_get_palette_index(K053251_CI2);
            layer_colorbase[1] = K053251_get_palette_index(K053251_CI4);
            layer_colorbase[2] = K053251_get_palette_index(K053251_CI3);

            K052109_tilemap_update();

            palette_init_used_colors();
            K053245_mark_sprites_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            layer[0] = 0;
            pri[0] = K053251_get_priority(K053251_CI2);
            layer[1] = 1;
            pri[1] = K053251_get_priority(K053251_CI4);
            layer[2] = 2;
            pri[2] = K053251_get_priority(K053251_CI3);

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
            fillbitmap(bitmap, Machine.pens[16 * bg_colorbase], Machine.drv.visible_area);
            K053245_sprites_draw(bitmap, pri[0] + 1, 0x3f);
            K052109_tilemap_draw(bitmap, layer[0], 0);
            K053245_sprites_draw(bitmap, pri[1] + 1, pri[0]);
            K052109_tilemap_draw(bitmap, layer[1], 0);
            K053245_sprites_draw(bitmap, pri[2] + 1, pri[1]);
            K052109_tilemap_draw(bitmap, layer[2], 0);
            K053245_sprites_draw(bitmap, 0, pri[2]);
        }
    };
}
