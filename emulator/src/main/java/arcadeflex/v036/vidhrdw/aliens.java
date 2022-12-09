/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;

import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class aliens {

    static int[] layer_colorbase = new int[3];
    public static int sprite_colorbase;

    /**
     * *************************************************************************
     *
     * Callbacks for the K052109
     *
     **************************************************************************
     */
    public static K052109_callbackProcPtr tile_callback = new K052109_callbackProcPtr() {
        public void handler(int layer, int bank, int[] code, int[] color) {
            code[0] |= ((color[0] & 0x3f) << 8) | (bank << 14);
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
    public static K051960_callbackProcPtr sprite_callback = new K051960_callbackProcPtr() {
        public void handler(int[] code, int[] color, int[] priority) {
            /* Weird priority scheme. Why use three bits when two would suffice? */
 /* The PROM allows for mixed priorities, where sprites would have */
 /* priority over text but not on one or both of the other two planes. */
 /* Luckily, this isn't used by the game. */
            switch (color[0] & 0x70) {
                case 0x10:
                    priority[0] = 0;
                    break;
                case 0x00:
                    priority[0] = 1;
                    break;
                case 0x40:
                    priority[0] = 2;
                    break;
                case 0x20:
                    priority[0] = 3;
                    break;
                /*   0x60 == 0x20 */
 /*   0x50 priority over F and A, but not over B */
 /*   0x30 priority over F, but not over A and B */
 /*   0x70 == 0x30 */
            }
            code[0] |= (color[0] & 0x80) << 6;
            color[0] = sprite_colorbase + (color[0] & 0x0f);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr aliens_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            paletteram = new UBytePtr(0x400);
            if (paletteram == null) {
                return 1;
            }

            layer_colorbase[0] = 0;
            layer_colorbase[1] = 4;
            layer_colorbase[2] = 8;
            sprite_colorbase = 16;
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tile_callback) != 0) {
                paletteram = null;
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, sprite_callback) != 0) {
                paletteram = null;
                K052109_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr aliens_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            paletteram = null;
            K052109_vh_stop();
            K051960_vh_stop();
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr aliens_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            palette_used_colors.write(layer_colorbase[1] * 16, palette_used_colors.read(layer_colorbase[1] * 16) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, Machine.pens[layer_colorbase[1] * 16], Machine.drv.visible_area);
            K051960_sprites_draw(bitmap, 3, 3);
            K052109_tilemap_draw(bitmap, 1, 0);
            K051960_sprites_draw(bitmap, 2, 2);
            K052109_tilemap_draw(bitmap, 2, 0);
            K051960_sprites_draw(bitmap, 1, 1);
            K052109_tilemap_draw(bitmap, 0, 0);
            K051960_sprites_draw(bitmap, 0, 0);
        }
    };
}
