/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;

public class spy {

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
            tile_info.flags = (color[0] & 0x20) != 0 ? (char) TILE_FLIPX : 0;
            code[0] |= ((color[0] & 0x03) << 8) | ((color[0] & 0x10) << 6) | ((color[0] & 0x0c) << 9)
                    | (bank << 13);
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
            /* bit 4 = priority over layer A (0 = have priority) */
            /* bit 5 = priority over layer B (1 = have priority) */
            priority[0] = (color[0] & 0x30) >> 4;
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
    public static VhStartPtr spy_vh_start = new VhStartPtr() {
        public int handler() {
            layer_colorbase[0] = 48;
            layer_colorbase[1] = 0;
            layer_colorbase[2] = 16;
            sprite_colorbase = 32;
            if (K052109_vh_start(REGION_GFX1, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, tile_callback) != 0) {
                return 1;
            }
            if (K051960_vh_start(REGION_GFX2, 0, 1, 2, 3/*NORMAL_PLANE_ORDER*/, sprite_callback) != 0) {
                K052109_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopPtr spy_vh_stop = new VhStopPtr() {
        public void handler() {
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
    public static VhUpdatePtr spy_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K052109_tilemap_update();

            palette_init_used_colors();
            K051960_mark_sprites_colors();
            palette_used_colors.write(16 * layer_colorbase[0], palette_used_colors.read(16 * layer_colorbase[0]) | PALETTE_COLOR_VISIBLE);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, Machine.pens[16 * layer_colorbase[0]], Machine.drv.visible_area);
            K051960_sprites_draw(bitmap, 1, 1);	/* are these used? */

            K052109_tilemap_draw(bitmap, 1, 0);
            K051960_sprites_draw(bitmap, 0, 0);
            K052109_tilemap_draw(bitmap, 2, 0);
            K051960_sprites_draw(bitmap, 3, 3);	/* are these used? They are supposed to have */
            /* priority over layer B but not layer A. */

            K051960_sprites_draw(bitmap, 2, 2);
            K052109_tilemap_draw(bitmap, 0, 0);
        }
    };
}
