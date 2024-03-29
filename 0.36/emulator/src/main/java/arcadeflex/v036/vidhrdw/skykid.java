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
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;

public class skykid {

    public static UBytePtr skykid_textram = new UBytePtr();
    public static UBytePtr drgnbstr_videoram = new UBytePtr();

    static tilemap background;
    /* background */
    static int game;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * The palette PROMs are connected to the RGB output this way:
     *
     * bit 3	-- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0	-- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr skykid_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int bit0, bit1, bit2, bit3;
            int totcolors = Machine.drv.total_colors;
            int p_inc = 0;
            for (i = 0; i < totcolors; i++) {
                /* red component */
                bit0 = (color_prom.read(totcolors * 0) >> 0) & 0x01;
                bit1 = (color_prom.read(totcolors * 0) >> 1) & 0x01;
                bit2 = (color_prom.read(totcolors * 0) >> 2) & 0x01;
                bit3 = (color_prom.read(totcolors * 0) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                /* green component */
                bit0 = (color_prom.read(totcolors * 1) >> 0) & 0x01;
                bit1 = (color_prom.read(totcolors * 1) >> 1) & 0x01;
                bit2 = (color_prom.read(totcolors * 1) >> 2) & 0x01;
                bit3 = (color_prom.read(totcolors * 1) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                /* blue component */
                bit0 = (color_prom.read(totcolors * 2) >> 0) & 0x01;
                bit1 = (color_prom.read(totcolors * 2) >> 1) & 0x01;
                bit2 = (color_prom.read(totcolors * 2) >> 2) & 0x01;
                bit3 = (color_prom.read(totcolors * 2) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            /* text palette */
            int co_ptr = 0;
            for (i = 0; i < 64 * 4; i++) {
                colortable[co_ptr++] = (char) i;
            }

            color_prom.inc(2 * totcolors);
            /* color_prom now points to the beginning of the lookup table */

 /* tiles lookup table */
            for (i = 0; i < 128 * 4; i++) {
                colortable[co_ptr++] = (char) (color_prom.readinc());
            }

            /* sprites lookup table */
            for (i = 0; i < 64 * 8; i++) {
                colortable[co_ptr++] = (char) (color_prom.readinc());
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
    public static WriteHandlerPtr get_tile_info_bg = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = row * 64 + col;
            /*unsigned */
            char code = drgnbstr_videoram.read(tile_index);
            /*unsigned */
            char attr = drgnbstr_videoram.read(tile_index + 0x800);

            SET_TILE_INFO(1, code + 256 * (attr & 0x01), ((attr & 0x7e) >> 1) | ((attr & 0x01) << 6));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    static int skykid_drgnbstr_common_vh_init() {
        background = tilemap_create(get_tile_info_bg, TILEMAP_OPAQUE, 8, 8, 64, 32);

        if (background != null) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            spriteram = new UBytePtr(RAM, 0x4f80);
            spriteram_2 = new UBytePtr(RAM, 0x4f80 + 0x0800);
            spriteram_3 = new UBytePtr(RAM, 0x4f80 + 0x0800 + 0x0800);
            spriteram_size[0] = 0x80;

            return 0;
        }
        return 1;
    }

    public static VhStartHandlerPtr skykid_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            game = 0;
            return skykid_drgnbstr_common_vh_init();
        }
    };

    public static VhStartHandlerPtr drgnbstr_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            game = 1;
            return skykid_drgnbstr_common_vh_init();
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static ReadHandlerPtr skykid_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return drgnbstr_videoram.read(offset);
        }
    };

    public static WriteHandlerPtr skykid_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (drgnbstr_videoram.read(offset) != data) {
                drgnbstr_videoram.write(offset, data);
                tilemap_mark_tile_dirty(background, (offset & 0x7ff) % 64, (offset & 0x7ff) / 64);
            }
        }
    };

    public static WriteHandlerPtr skykid_scroll_x_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (game != 0) {
                tilemap_set_scrollx(background, 0, ((offset ^ 1) + 36) & 0x1ff);
            } else {
                tilemap_set_scrollx(background, 0, (189 - (offset ^ 1)) & 0x1ff);
            }
        }
    };

    public static WriteHandlerPtr skykid_scroll_y_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (game != 0) {
                tilemap_set_scrolly(background, 0, (offset + 25) & 0xff);
            } else {
                tilemap_set_scrolly(background, 0, (261 - offset) & 0xff);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Display Refresh
     *
     **************************************************************************
     */
    static void skykid_draw_sprites(osd_bitmap bitmap) {
        int offs;

        for (offs = 0; offs < spriteram_size[0]; offs += 2) {
            int number = spriteram.read(offs) | ((spriteram_3.read(offs) & 0x80) << 1);
            int color = (spriteram.read(offs + 1) & 0x3f);
            int sx = 256 - ((spriteram_2.read(offs + 1)) + 0x100 * (spriteram_3.read(offs + 1) & 0x01)) + 72;
            int sy = spriteram_2.read(offs) - 7;
            int flipy = spriteram_3.read(offs) & 0x02;
            int flipx = spriteram_3.read(offs) & 0x01;
            int width, height;

            if (number >= 128 * 3) {
                continue;
            }

            switch (spriteram_3.read(offs) & 0x0c) {
                case 0x0c:
                    /* 2x both ways */
                    width = height = 2;
                    number &= (~3);
                    break;
                case 0x08:
                    /* 2x vertical */
                    width = 1;
                    height = 2;
                    number &= (~2);
                    break;
                case 0x04:
                    /* 2x horizontal */
                    width = 2;
                    height = 1;
                    number &= (~1);
                    break;
                default:
                    /* normal sprite */
                    width = height = 1;
                    sx += 16;
                    break;
            }

            {
                int x_offset[] = {0x00, 0x01};
                int y_offset[] = {0x00, 0x02};
                int x, y, ex, ey;

                for (y = 0; y < height; y++) {
                    for (x = 0; x < width; x++) {
                        ex = flipx != 0 ? (width - 1 - x) : x;
                        ey = flipy != 0 ? (height - 1 - y) : y;

                        drawgfx(bitmap, Machine.gfx[2 + (number >> 7)],
                                (number) + x_offset[ex] + y_offset[ey],
                                color,
                                flipx, flipy,
                                sx + x * 16, sy + y * 16,
                                Machine.drv.visible_area,
                                TRANSPARENCY_COLOR, 255);
                    }
                }
            }
        }
    }

    static void drgnbstr_draw_sprites(osd_bitmap bitmap) {
        int offs;

        for (offs = 0; offs < spriteram_size[0]; offs += 2) {
            int number = spriteram.read(offs) | ((spriteram_3.read(offs) & 0x80) << 1);
            int color = (spriteram.read(offs + 1) & 0x3f);
            int sx = (spriteram_2.read(offs + 1)) + 0x100 * (spriteram_3.read(offs + 1) & 1) - 74;
            int sy = 256 - spriteram_2.read(offs) - 54;
            int flipy = spriteram_3.read(offs) & 0x02;
            int flipx = spriteram_3.read(offs) & 0x01;
            int width, height;

            if (number >= 128 * 3) {
                continue;
            }

            switch (spriteram_3.read(offs) & 0x0c) {
                case 0x0c:
                    /* 2x both ways */
                    width = height = 2;
                    number &= (~3);
                    break;
                case 0x08:
                    /* 2x vertical */
                    width = 1;
                    height = 2;
                    number &= (~2);
                    break;
                case 0x04:
                    /* 2x horizontal */
                    width = 2;
                    height = 1;
                    number &= (~1);
                    sy += 16;
                    break;
                default:
                    /* normal sprite */
                    width = height = 1;
                    sy += 16;
                    break;
            }

            {
                int x_offset[] = {0x00, 0x01};
                int y_offset[] = {0x00, 0x02};
                int x, y, ex, ey;

                for (y = 0; y < height; y++) {
                    for (x = 0; x < width; x++) {
                        ex = flipx != 0 ? (width - 1 - x) : x;
                        ey = flipy != 0 ? (height - 1 - y) : y;

                        drawgfx(bitmap, Machine.gfx[2 + (number >> 7)],
                                (number) + x_offset[ex] + y_offset[ey],
                                color,
                                flipx, flipy,
                                sx + x * 16, sy + y * 16,
                                Machine.drv.visible_area,
                                TRANSPARENCY_COLOR, 255);
                    }
                }
            }
        }
    }

    public static VhUpdateHandlerPtr skykid_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            tilemap_update(ALL_TILEMAPS);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, background, 0);
            skykid_draw_sprites(bitmap);

            for (offs = 0x400 - 1; offs > 0; offs--) {
                {
                    int mx, my, sx, sy;

                    mx = offs % 32;
                    my = offs / 32;

                    if (my < 2) {
                        if (mx < 2 || mx >= 30) {
                            continue;
                            /* not visible */
                        }
                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        if (mx < 2 || mx >= 30) {
                            continue;
                            /* not visible */
                        }
                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        sx = mx + 2;
                        sy = my - 2;
                    }
                    drawgfx(bitmap, Machine.gfx[0], skykid_textram.read(offs),
                            skykid_textram.read(offs + 0x400) & 0x3f,
                            0, 0, sx * 8, sy * 8,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };

    public static VhUpdateHandlerPtr drgnbstr_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            tilemap_update(ALL_TILEMAPS);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, background, 0);
            for (offs = 0x400 - 1; offs > 0; offs--) {
                {
                    int mx, my, sx, sy;

                    mx = offs % 32;
                    my = offs / 32;

                    if (my < 2) {
                        if (mx < 2 || mx >= 30) {
                            continue;
                            /* not visible */
                        }
                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        if (mx < 2 || mx >= 30) {
                            continue;
                            /* not visible */
                        }
                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        sx = mx + 2;
                        sy = my - 2;
                    }
                    drawgfx(bitmap, Machine.gfx[0], skykid_textram.read(offs),
                            skykid_textram.read(offs + 0x400) & 0x3f,
                            0, 0, sx * 8, sy * 8,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            drgnbstr_draw_sprites(bitmap);
        }
    };
}
