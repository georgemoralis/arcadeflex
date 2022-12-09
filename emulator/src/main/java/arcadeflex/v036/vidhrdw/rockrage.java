/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K007342.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K007420.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class rockrage {

    static int[] layer_colorbase = new int[2];
    static int rockrage_vreg;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr rockrage_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            /* build the lookup table for sprites. Palette is dynamic. */
            for (i = 0; i < TOTAL_COLORS(0) / 2; i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (0x00 + (color_prom.read(i) & 0x0f));
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + (TOTAL_COLORS(0) / 2) + i] = (char) (0x10 + (color_prom.read(0x100 + i) & 0x0f));
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Callback for the K007342
     *
     **************************************************************************
     */
    public static K007342_callbackProcPtr tile_callback = new K007342_callbackProcPtr() {
        public void handler(int tilemap, int bank, int[] code, int[] color) {
            if (layer == 1) {
                code[0] |= ((color[0] & 0x40) << 2) | ((bank & 0x01) << 9);
            } else {
                code[0] |= ((color[0] & 0x40) << 2) | ((bank & 0x03) << 10) | ((rockrage_vreg & 0x04) << 7) | ((rockrage_vreg & 0x08) << 9);
            }
            color[0] = layer_colorbase[layer] + (color[0] & 0x0f);
        }
    };

    /**
     * *************************************************************************
     *
     * Callback for the K007420
     *
     **************************************************************************
     */
    public static K007420_callbackProcPtr sprite_callback = new K007420_callbackProcPtr() {
        public void handler(int[] code, int[] color) {
            code[0] |= ((color[0] & 0x40) << 2) | ((color[0] & 0x80) << 1) * (rockrage_vreg << 1);
            code[0] = (code[0] << 2) | ((color[0] & 0x30) >> 4);
            color[0] = 0;
        }
    };

    public static WriteHandlerPtr rockrage_vreg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 4-7: unused */
 /* bit 3: bit 4 of bank # (layer 0) */
 /* bit 2: bit 1 of bank # (layer 0) */
 /* bits 0-1: sprite bank select */

            if ((data & 0x0c) != (rockrage_vreg & 0x0c)) {
                tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
            }

            rockrage_vreg = data;
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr rockrage_vh_start = new VhStartPtr() {
        public int handler() {
            layer_colorbase[0] = 0x00;
            layer_colorbase[1] = 0x10;

            if (K007342_vh_start(0, tile_callback) != 0) {
                return 1;
            }

            if (K007420_vh_start(1, sprite_callback) != 0) {
                K007420_vh_stop();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopPtr rockrage_vh_stop = new VhStopPtr() {
        public void handler() {
            K007342_vh_stop();
            K007420_vh_stop();
        }
    };

    /**
     * *************************************************************************
     *
     * Screen Refresh
     *
     **************************************************************************
     */
    public static VhUpdatePtr rockrage_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            K007342_tilemap_update();

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            K007342_tilemap_draw(bitmap, 0, TILEMAP_IGNORE_TRANSPARENCY);
            K007420_sprites_draw(bitmap);
            K007342_tilemap_draw(bitmap, 0, 1 | TILEMAP_IGNORE_TRANSPARENCY);
            K007342_tilemap_draw(bitmap, 1, 0);
            K007342_tilemap_draw(bitmap, 1, 1);
        }
    };
}
