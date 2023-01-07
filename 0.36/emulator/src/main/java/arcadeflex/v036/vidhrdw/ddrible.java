/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 07/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class ddrible {

    public static UBytePtr ddrible_fg_videoram = new UBytePtr();
    public static UBytePtr ddrible_bg_videoram = new UBytePtr();
    public static UBytePtr ddrible_spriteram_1 = new UBytePtr();
    public static UBytePtr ddrible_spriteram_2 = new UBytePtr();

    static tilemap fg_tilemap, bg_tilemap;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr ddrible_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            /* build the lookup table for sprites. Palette is dynamic. */
            for (i = 0; i < TOTAL_COLORS(3); i++) {
                colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }
        }
    };

    /**
     * *****************************************************************
     *
     * Double Dribble Video registers
     *
     * 0x0000:	y coordinate to start drawing the screen
     *
     * 0x0003: bit 0:	unused? bit 1:	bit 3 of tile bank # bit 2:	unused? bit 3:
     * ??? bits 4..7:	unused?
     *
     * 0x0801: background x scroll register
     *
     * 0x0803: bit 0:	bit 3 of tile bank # bit 1:	bit 4 of tile bank # bit 2:
     * unused? bit 3:	??? bits 4..7:	unused?
     *
     ******************************************************************
     */
    static int bankfg = 0;
    /* bank # (foreground tiles) */
    static int bankbg = 0;
    /* bank # (background tiles) */

    public static WriteHandlerPtr ddrible_bank_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x02) != bankfg) {
                bankfg = (data & 0x02);
                /* char bank selection for set 1 */
                tilemap_mark_all_tiles_dirty(fg_tilemap);
            }
        }
    };

    public static WriteHandlerPtr ddrible_bank_select_w_2 = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (((data & 0x03) << 1) != bankbg) {
                bankbg = (data & 0x03) << 1;
                /* char bank selection for set 2 */
                tilemap_mark_all_tiles_dirty(bg_tilemap);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index;

            if (col >= 32) {
                col -= 32;
                tile_index = 0x800 + row * 32 + col;
            } else {
                tile_index = row * 32 + col;
            }

            {
                char attr = (char) (ddrible_fg_videoram.read(tile_index) & 0xFF);
                int bank = ((attr & 0xc0) >> 6) + 4 * (((attr & 0x20) >> 5) | bankfg);
                int num = ddrible_fg_videoram.read(tile_index + 0x400) + 256 * bank;
                SET_TILE_INFO(0, num, 0);
                tile_info.flags = (char) TILE_FLIPYX((attr & 0x30) >> 4);
            }
        }
    };

    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index;

            if (col >= 32) {
                col -= 32;
                tile_index = 0x800 + row * 32 + col;
            } else {
                tile_index = row * 32 + col;
            }

            {
                char attr = (char) (ddrible_bg_videoram.read(tile_index) & 0xFF);
                int bank = ((attr & 0xc0) >> 6) + 4 * (((attr & 0x20) >> 5) | bankbg);
                int num = ddrible_bg_videoram.read(tile_index + 0x400) + 256 * bank;
                SET_TILE_INFO(1, num, 0);
                tile_info.flags = (char) TILE_FLIPYX((attr & 0x30) >> 4);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr ddrible_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            fg_tilemap = tilemap_create(get_fg_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
            bg_tilemap = tilemap_create(get_bg_tile_info, TILEMAP_OPAQUE, 8, 8, 64, 32);

            if (fg_tilemap != null && bg_tilemap != null) {
                fg_tilemap.transparent_pen = 0;

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
    public static WriteHandlerPtr ddrible_fg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (ddrible_fg_videoram.read(offset) != data) {
                ddrible_fg_videoram.write(offset, data);
                if ((offset & 0x800) != 0) {
                    tilemap_mark_tile_dirty(fg_tilemap, offset % 32 + 32, (offset & 0x3ff) / 32);
                } else {
                    tilemap_mark_tile_dirty(fg_tilemap, offset % 32, (offset & 0x3ff) / 32);
                }
            }
        }
    };

    public static WriteHandlerPtr ddrible_bg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (ddrible_bg_videoram.read(offset) != data) {
                ddrible_bg_videoram.write(offset, data);
                if ((offset & 0x800) != 0) {
                    tilemap_mark_tile_dirty(bg_tilemap, offset % 32 + 32, (offset & 0x3ff) / 32);
                } else {
                    tilemap_mark_tile_dirty(bg_tilemap, offset % 32, (offset & 0x3ff) / 32);
                }
            }
        }
    };

    static int fg_scrollx;
    public static WriteHandlerPtr ddrible_fg_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (offset != 0) {
                fg_scrollx = (fg_scrollx & 0x0ff) | ((data & 0x01) << 8);
            } else {
                fg_scrollx = (fg_scrollx & 0x100) | data;
            }

            tilemap_set_scrollx(fg_tilemap, 0, fg_scrollx);
        }
    };

    public static WriteHandlerPtr ddrible_fg_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tilemap_set_scrolly(fg_tilemap, 0, data);
        }
    };
    static int bg_scrollx;

    public static WriteHandlerPtr ddrible_bg_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (offset != 0) {
                bg_scrollx = (bg_scrollx & 0x0ff) | ((data & 0x01) << 8);
            } else {
                bg_scrollx = (bg_scrollx & 0x100) | data;
            }

            tilemap_set_scrollx(bg_tilemap, 0, bg_scrollx);
        }
    };

    public static WriteHandlerPtr ddrible_bg_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tilemap_set_scrolly(bg_tilemap, 0, data);
        }
    };

    /**
     * *************************************************************************
     *
     * Double Dribble sprites
     *
     * Each sprite has 5 bytes: byte #0:	sprite number byte #1: bits 0..2:
     * sprite bank # bits 3..7:	sprite color? byte #2:	y position byte #3:	x
     * position byte #4:	attributes bit 0:	enable bit 1:	??? bit 2:	unused? bit
     * 3:	unused? bit 4:	2x both ways bit 5:	flip x bit 6:	unused? bit 7:
     * unused?
     *
     **************************************************************************
     */
    static void draw_sprites(osd_bitmap bitmap, UBytePtr source, int lenght, int gfxset) {
        GfxElement gfx = Machine.gfx[gfxset];
        int finish = source.offset + lenght;//const unsigned char *finish = source + lenght;

        while (source.offset < finish)//( source < finish )
        {
            int sprite_number = source.read(0);
            /* sprite number */
            int sprite_bank = source.read(1) & 0x07;
            /* sprite bank */
            int sx = source.read(3);
            /* vertical position */
            int sy = source.read(2);
            /* horizontal position */
            int attr = source.read(4);
            /* attributes */
            int xflip = attr & 0x20;
            /* flip x */
            int color = (source.read(1) & 0xf0) >> 4;
            /* color */
            if (sy < 16) {
                attr |= 0x01;
                /* clip sprites */
            }

            if ((attr & 0x01) == 0) /* sprite enable */ {
                sprite_number += sprite_bank * 256;

                if ((attr & 0x10) == 0) /* normal sprite */ {
                    drawgfx(bitmap, gfx, sprite_number, color, xflip, 0,
                            sx, sy, null, TRANSPARENCY_PEN, 0);
                } else /* 2x both ways */ {
                    if ((attr & 0x20) == 0) {
                        drawgfx(bitmap, gfx, sprite_number, color, xflip, 0,
                                sx, sy, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number + 1, color, xflip, 0,
                                sx + 16, sy, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number + 2, color, xflip, 0,
                                sx, sy + 16, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number + 3, color, xflip, 0,
                                sx + 16, sy + 16, null, TRANSPARENCY_PEN, 0);
                    } else /* 2x both ways flipped */ {
                        drawgfx(bitmap, gfx, sprite_number + 1, color, xflip, 0,
                                sx, sy, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number, color, xflip, 0,
                                sx + 16, sy, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number + 3, color, xflip, 0,
                                sx, sy + 16, null, TRANSPARENCY_PEN, 0);
                        drawgfx(bitmap, gfx, sprite_number + 2, color, xflip, 0,
                                sx + 16, sy + 16, null, TRANSPARENCY_PEN, 0);
                    }
                }
            }
            source.inc(5);
        }
    }

    /**
     * *************************************************************************
     *
     * Display Refresh
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr ddrible_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_update(ALL_TILEMAPS);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, bg_tilemap, 0);
            draw_sprites(bitmap, new UBytePtr(ddrible_spriteram_1, 0), 0x7d, 2);
            /* sprites set 1 */
            draw_sprites(bitmap, new UBytePtr(ddrible_spriteram_2, 0), 0x140, 3);
            /* sprites set 2 */
            tilemap_draw(bitmap, fg_tilemap, 0);
        }
    };
}
