/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 16/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.commonH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.konamiic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class mainevt {

    static int[] layer_colorbase = new int[3];
    static int sprite_colorbase;

    /**
     * *************************************************************************
     *
     * Callbacks for the K052109
     *
     **************************************************************************
     */
    public static K052109_callbackProcPtr mainevt_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            tile_info.flags = (color[0] & 0x02) != 0 ? (char) TILE_FLIPX : 0;

            /* priority relative to HALF priority sprites */
            if (layer == 2) {
                tile_info.priority = (char) ((color[0] & 0x20) >> 5);
            } else {
                tile_info.priority = 0;
            }

            code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x1c) << 7);
            color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
        }
    };

    public static K052109_callbackProcPtr dv_tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            /* (color & 0x02) is flip y handled internally by the 052109 */
            code[0] |= ((color[0] & 0x01) << 8) | ((color[0] & 0x3c) << 7);
            color[0] = layer_colorbase[layer] + ((color[0] & 0xc0) >> 6);
        }
    };
    /**
     * *************************************************************************
     *
     * Callbacks for the K051960
     *
     **************************************************************************
     */
    public static K051960_callbackProcPtr mainevt_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            /* bit 5 = priority over layer B (has precedence) */
 /* bit 6 = HALF priority over layer B (used for crowd when you get out of the ring) */
            if ((color[0] & 0x20) != 0) {
                priority[0] = 1;
            } else if ((color[0] & 0x40) != 0) {
                priority[0] = 2;
            }
            /* bit 7 is shadow, not used */

 /* kludge to fix ropes until sprite/sprite priority is supported correctly */
            if (code[0] == 0x3f8 || code[0] == 0x3f9) {
                priority[0] = 2;
            }

            color[0] = sprite_colorbase + (color[0] & 0x03);
        }
    };

    public static K051960_callbackProcPtr dv_sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            /* TODO: the priority/shadow handling (bits 5-7) seems to be quite complex (see PROM) */
            color[0] = sprite_colorbase + (color[0] & 0x07);
        }
    };

    /**
     * **************************************************************************
     */
    public static VhStartHandlerPtr mainevt_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            layer_colorbase[0] = 0;
            layer_colorbase[1] = 8;
            layer_colorbase[2] = 4;
            sprite_colorbase = 12;

            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, mainevt_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, mainevt_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStartHandlerPtr dv_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            layer_colorbase[0] = 0;
            layer_colorbase[1] = 0;
            layer_colorbase[2] = 4;
            sprite_colorbase = 8;

            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, dv_tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, dv_sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr mainevt_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            K052109_vh_stop();
            K051960_vh_stop();
        }
    };

    /**
     * **************************************************************************
     */
    public static VhUpdateHandlerPtr mainevt_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            K052109_tilemap_draw(bitmap, 1, TILEMAP_IGNORE_TRANSPARENCY);
            K051960_sprites_draw(bitmap, 0, 0);
            K052109_tilemap_draw(bitmap, 2, 1);
            /* low priority part of layer */
            K051960_sprites_draw(bitmap, 2, 2);
            K052109_tilemap_draw(bitmap, 2, 0);
            /* high priority part of layer */
            K051960_sprites_draw(bitmap, 1, 1);
            K052109_tilemap_draw(bitmap, 0, 0);
        }
    };

    public static VhUpdateHandlerPtr dv_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            K052109_tilemap_draw(bitmap, 1, TILEMAP_IGNORE_TRANSPARENCY);
            K052109_tilemap_draw(bitmap, 2, 0);
            K051960_sprites_draw(bitmap, 0, 0);
            K052109_tilemap_draw(bitmap, 0, 0);
        }
    };
}
