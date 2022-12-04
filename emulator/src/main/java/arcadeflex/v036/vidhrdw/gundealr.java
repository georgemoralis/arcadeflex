/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 */
package arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class gundealr {

    public static UBytePtr gundealr_bg_videoram = new UBytePtr();
    public static UBytePtr gundealr_fg_videoram = new UBytePtr();

    static tilemap bg_tilemap, fg_tilemap;
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 2 * (32 * col + row);
            char attr = gundealr_bg_videoram.read(tile_index + 1);
            SET_TILE_INFO(0, gundealr_bg_videoram.read(tile_index) + ((attr & 0x07) << 8), (attr & 0xf0) >> 4);
        }
    };

    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 2 * ((row & 0x0f) + ((col & 0x3f) << 4) + ((row & 0x10) << 6));
            char attr = gundealr_fg_videoram.read(tile_index + 1);
            SET_TILE_INFO(1, gundealr_fg_videoram.read(tile_index) + ((attr & 0x03) << 8), (attr & 0xf0) >> 4);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr gundealr_vh_start = new VhStartPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(
                    get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    8, 8,
                    32, 32
            );
            fg_tilemap = tilemap_create(
                    get_fg_tile_info,
                    TILEMAP_TRANSPARENT,
                    16, 16,
                    64, 32
            );

            if (bg_tilemap != null && fg_tilemap != null) {
                fg_tilemap.transparent_pen = 15;

                return 0;
            }

            return 1;
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr gundealr_bg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gundealr_bg_videoram.read(offset) != data) {
                gundealr_bg_videoram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, (offset / 2) / 32, (offset / 2) % 32);
            }
        }
    };

    public static WriteHandlerPtr gundealr_fg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gundealr_fg_videoram.read(offset) != data) {
                int x, y;

                gundealr_fg_videoram.write(offset, data);

                offset /= 2;
                x = (offset & 0x3f0) >> 4;
                y = (offset & 0x00f) | ((offset & 0x400) >> 6);
                tilemap_mark_tile_dirty(fg_tilemap, x, y);
            }
        }
    };

    public static WriteHandlerPtr gundealr_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int r, g, b, val;

            paletteram.write(offset, data);

            val = paletteram.read(offset & ~1);
            r = (val >> 4) & 0x0f;
            g = (val >> 0) & 0x0f;

            val = paletteram.read(offset | 1);
            b = (val >> 4) & 0x0f;
            /* TODO: the bottom 4 bits are used as well, but I'm not sure about the meaning */

            r = 0x11 * r;
            g = 0x11 * g;
            b = 0x11 * b;

            palette_change_color(offset / 2, r, g, b);
        }
    };
    static char[] scroll = new char[4];
    public static WriteHandlerPtr gundealr_fg_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            scroll[offset] = (char) (data & 0xFF);
            tilemap_set_scrollx(fg_tilemap, 0, scroll[1] | ((scroll[0] & 0x03) << 8));
            tilemap_set_scrolly(fg_tilemap, 0, scroll[3] | ((scroll[2] & 0x03) << 8));
        }
    };
    static char[] scrolly = new char[4];
    public static WriteHandlerPtr yamyam_fg_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            scrolly[offset] = (char) (data & 0xFF);
            tilemap_set_scrollx(fg_tilemap, 0, scrolly[0] | ((scrolly[1] & 0x03) << 8));
            tilemap_set_scrolly(fg_tilemap, 0, scrolly[2] | ((scrolly[3] & 0x03) << 8));
        }
    };

    public static WriteHandlerPtr gundealr_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flipscreen = data;
            tilemap_set_flip(ALL_TILEMAPS, flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    public static VhUpdatePtr gundealr_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, bg_tilemap, 0);
            tilemap_draw(bitmap, fg_tilemap, 0);
        }
    };
}
