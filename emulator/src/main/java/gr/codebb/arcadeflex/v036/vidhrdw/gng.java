/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class gng {

    public static UBytePtr gng_fgvideoram = new UBytePtr();
    public static UBytePtr gng_fgcolorram = new UBytePtr();
    public static UBytePtr gng_bgvideoram = new UBytePtr();
    public static UBytePtr gng_bgcolorram = new UBytePtr();
    static tilemap bg_tilemap, fg_tilemap;
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = row * 32 + col;
            char attr = gng_fgcolorram.read(tile_index);
            SET_TILE_INFO(0, gng_fgvideoram.read(tile_index) + ((attr & 0xc0) << 2), attr & 0x0f);
            tile_info.flags = (char) TILE_FLIPYX((attr & 0x30) >> 4);
        }
    };

    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = col * 32 + row;
            char attr = gng_bgcolorram.read(tile_index);
            SET_TILE_INFO(1, gng_bgvideoram.read(tile_index) + ((attr & 0xc0) << 2), attr & 0x07);
            tile_info.flags = (char) (TILE_FLIPYX((attr & 0x30) >> 4) | TILE_SPLIT((attr & 0x08) >> 3));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr gng_vh_start = new VhStartPtr() {
        public int handler() {
            fg_tilemap = tilemap_create(
                    get_fg_tile_info,
                    TILEMAP_TRANSPARENT,
                    8, 8, /* tile width, tile height */
                    32, 32 /* number of columns, number of rows */
            );

            bg_tilemap = tilemap_create(
                    get_bg_tile_info,
                    TILEMAP_SPLIT,
                    16, 16,
                    32, 32
            );

            if (fg_tilemap != null && bg_tilemap != null) {
                fg_tilemap.transparent_pen = 3;

                bg_tilemap.transmask[0] = 0xff; /* split type 0 is totally transparent in front half */

                bg_tilemap.transmask[1] = 0x01; /* split type 1 has pen 1 transparent in front half */

                return 0;
            }

            return 1;
        }
    };

    public static WriteHandlerPtr gng_fgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gng_fgvideoram.read(offset) != data) {
                gng_fgvideoram.write(offset, data);
                tilemap_mark_tile_dirty(fg_tilemap, offset % 32, offset / 32);
            }
        }
    };

    public static WriteHandlerPtr gng_fgcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gng_fgcolorram.read(offset) != data) {
                gng_fgcolorram.write(offset, data);
                tilemap_mark_tile_dirty(fg_tilemap, offset % 32, offset / 32);
            }
        }
    };

    public static WriteHandlerPtr gng_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gng_bgvideoram.read(offset) != data) {
                gng_bgvideoram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, offset / 32, offset % 32);
            }
        }
    };

    public static WriteHandlerPtr gng_bgcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gng_bgcolorram.read(offset) != data) {
                gng_bgcolorram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, offset / 32, offset % 32);
            }
        }
    };

    static char[] scrollx = new char[2];
    public static WriteHandlerPtr gng_bgscrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            scrollx[offset] = (char) data;
            tilemap_set_scrollx(bg_tilemap, 0, scrollx[0] + 256 * scrollx[1]);
        }
    };
    static char[] scrolly = new char[2];
    public static WriteHandlerPtr gng_bgscrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            scrolly[offset] = (char) data;
            tilemap_set_scrolly(bg_tilemap, 0, scrolly[0] + 256 * scrolly[1]);
        }
    };

    public static WriteHandlerPtr gng_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flipscreen = ~data & 1;
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
    static void draw_sprites(osd_bitmap bitmap) {
        GfxElement gfx = Machine.gfx[2];
        rectangle clip = Machine.drv.visible_area;
        int offs;
        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            char attributes = (char) (spriteram.read(offs + 1) & 0xFF);
            int sx = spriteram.read(offs + 3) - 0x100 * (attributes & 0x01);
            int sy = spriteram.read(offs + 2);
            int flipx = attributes & 0x04;
            int flipy = attributes & 0x08;

            if (flipscreen != 0) {
                sx = 240 - sx;
                sy = 240 - sy;
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            drawgfx(bitmap, gfx,
                    spriteram.read(offs) + ((attributes << 2) & 0x300),
                    (attributes >> 4) & 3,
                    flipx, flipy,
                    sx, sy,
                    clip, TRANSPARENCY_PEN, 15);
        }
    }

    public static VhUpdatePtr gng_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_update(ALL_TILEMAPS);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, bg_tilemap, TILEMAP_BACK);
            draw_sprites(bitmap);
            tilemap_draw(bitmap, bg_tilemap, TILEMAP_FRONT);
            tilemap_draw(bitmap, fg_tilemap, 0);
        }
    };
}
