/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
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
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class renegade {

    public static UBytePtr renegade_textram = new UBytePtr();
    static int renegade_scrollx;
    static tilemap bg_tilemap;
    static tilemap fg_tilemap;
    static int flipscreen;

    public static WriteHandlerPtr renegade_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videoram.read(offset) != data) {
                videoram.write(offset, data);
                offset = offset % (64 * 16);
                tilemap_mark_tile_dirty(bg_tilemap, offset % 64, offset / 64);
            }
        }
    };

    public static WriteHandlerPtr renegade_textram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (renegade_textram.read(offset) != data) {
                renegade_textram.write(offset, data);
                offset = offset % (32 * 32);
                tilemap_mark_tile_dirty(fg_tilemap, offset % 32, offset / 32);
            }
        }
    };

    public static WriteHandlerPtr renegade_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flipscreen = data == 0 ? 1 : 0; //!data
            tilemap_set_flip(ALL_TILEMAPS, flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
        }
    };

    public static WriteHandlerPtr renegade_scroll0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            renegade_scrollx = (renegade_scrollx & 0xff00) | data;
        }
    };

    public static WriteHandlerPtr renegade_scroll1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            renegade_scrollx = (renegade_scrollx & 0xFF) | (data << 8);
        }
    };

    public static WriteHandlerPtr get_bg_tilemap_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            UBytePtr source = new UBytePtr(videoram, col + row * 64);
            char attributes = source.read(0x400);
            /* CCC??BBB */
            SET_TILE_INFO(
                    1 + (attributes & 0x7), /* bank */
                    source.read(0), /* tile_number */
                    attributes >> 5 /* color */
            );
        }
    };

    public static WriteHandlerPtr get_fg_tilemap_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            UBytePtr source = new UBytePtr(renegade_textram, col + row * 32);
            char attributes = source.read(0x400);
            SET_TILE_INFO(
                    0,
                    (attributes & 3) * 256 + source.read(0), /* tile_number */
                    attributes >> 6
            );
        }
    };

    public static VhStartHandlerPtr renegade_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(
                    get_bg_tilemap_info,
                    TILEMAP_OPAQUE,
                    16, 16, /* tile width, tile height */
                    64, 16 /* num_cols, num_rows */
            );
            fg_tilemap = tilemap_create(
                    get_fg_tilemap_info,
                    TILEMAP_TRANSPARENT,
                    8, 8, /* tile width, tile height */
                    32, 32 /* num_cols, num_rows */
            );
            if (bg_tilemap != null && fg_tilemap != null) {
                fg_tilemap.transparent_pen = 0;
                tilemap_set_scrolldx(bg_tilemap, 256, 0);

                tilemap_set_scrolldy(fg_tilemap, 0, 16);
                tilemap_set_scrolldy(bg_tilemap, 0, 16);
                return 0;
            }
            return 1;
        }
    };

    static void draw_sprites(osd_bitmap bitmap) {
        rectangle clip = new rectangle(Machine.drv.visible_area);

        UBytePtr source = new UBytePtr(spriteram, 0);
        int finish = source.offset + 96 * 4;//UBytePtr finish = new UBytePtr(source,96*4);

        while (source.offset < finish) {
            int sy = 240 - source.read(0);
            if (sy >= 16) {
                int attributes = source.read(1);
                /* SFCCBBBB */
                int sx = source.read(3);
                int sprite_number = source.read(2);
                int sprite_bank = 9 + (attributes & 0xF);
                int color = (attributes >> 4) & 0x3;
                int xflip = attributes & 0x40;

                if (sx > 248) {
                    sx -= 256;
                }

                if ((attributes & 0x80) != 0) {
                    /* big sprite */
                    drawgfx(bitmap, Machine.gfx[sprite_bank],
                            sprite_number + 1,
                            color,
                            xflip, 0,
                            sx, sy + 16,
                            clip, TRANSPARENCY_PEN, 0);
                } else {
                    sy += 16;
                }
                drawgfx(bitmap, Machine.gfx[sprite_bank],
                        sprite_number,
                        color,
                        xflip, 0,
                        sx, sy,
                        clip, TRANSPARENCY_PEN, 0);
            }
            source.inc(4);
        }
    }
    public static VhUpdateHandlerPtr renegade_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(bg_tilemap, 0, renegade_scrollx);
            tilemap_set_scrolly(bg_tilemap, 0, 0);
            tilemap_set_scrolly(fg_tilemap, 0, 0);

            tilemap_update(ALL_TILEMAPS);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);
            tilemap_draw(bitmap, bg_tilemap, 0);
            draw_sprites(bitmap);
            tilemap_draw(bitmap, fg_tilemap, 0);
        }
    };
}
