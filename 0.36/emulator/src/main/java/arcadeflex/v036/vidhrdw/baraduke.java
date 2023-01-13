/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 13/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.paletteH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class baraduke {

    public static UBytePtr baraduke_textram = new UBytePtr();
    public static UBytePtr baraduke_videoram = new UBytePtr();

    static tilemap[] _tilemap = new tilemap[2];
    /* backgrounds */
    static int[] xscroll = new int[2];
    static int[] yscroll = new int[2];
    /* scroll registers */

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
    public static VhConvertColorPromHandlerPtr baraduke_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int bit0, bit1, bit2, bit3;
            int p_inc = 0;
            for (i = 0; i < 2048; i++) {
                /* red component */
                bit0 = (color_prom.read(2048) >> 0) & 0x01;
                bit1 = (color_prom.read(2048) >> 1) & 0x01;
                bit2 = (color_prom.read(2048) >> 2) & 0x01;
                bit3 = (color_prom.read(2048) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                /* green component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                /* blue component */
                bit0 = (color_prom.read(0) >> 4) & 0x01;
                bit1 = (color_prom.read(0) >> 5) & 0x01;
                bit2 = (color_prom.read(0) >> 6) & 0x01;
                bit3 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
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
    public static WriteHandlerPtr get_tile_info0 = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 2 * (64 * row + col);
            /*unsigned */
            char attr = baraduke_videoram.read(tile_index + 1);
            /*unsigned */
            char code = baraduke_videoram.read(tile_index);

            SET_TILE_INFO(1 + ((attr & 0x02) >> 1), code | ((attr & 0x01) << 8), attr);
        }
    };

    public static WriteHandlerPtr get_tile_info1 = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 2 * (64 * row + col);
            /*unsigned */
            char attr = baraduke_videoram.read(0x1000 + tile_index + 1);
            /*unsigned */
            char code = baraduke_videoram.read(0x1000 + tile_index);

            SET_TILE_INFO(3 + ((attr & 0x02) >> 1), code | ((attr & 0x01) << 8), attr);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr baraduke_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            _tilemap[0] = tilemap_create(get_tile_info0, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
            _tilemap[1] = tilemap_create(get_tile_info1, TILEMAP_TRANSPARENT, 8, 8, 64, 32);

            if (_tilemap[0] != null && _tilemap[1] != null) {
                _tilemap[0].transparent_pen = 7;
                _tilemap[1].transparent_pen = 7;

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
    public static ReadHandlerPtr baraduke_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return baraduke_videoram.read(offset);
        }
    };

    public static WriteHandlerPtr baraduke_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (baraduke_videoram.read(offset) != data) {
                baraduke_videoram.write(offset, data);
                tilemap_mark_tile_dirty(_tilemap[offset / 0x1000], (offset / 2) % 64, ((offset % 0x1000) / 2) / 64);
            }
        }
    };

    static void scroll_w(int layer, int offset, int data) {
        int xdisp[] = {26, 24};

        switch (offset) {

            case 0:
                /* high scroll x */
                xscroll[layer] = (xscroll[layer] & 0xff) | (data << 8);
                break;
            case 1:
                /* low scroll x */
                xscroll[layer] = (xscroll[layer] & 0xff00) | data;
                break;
            case 2:
                /* scroll y */
                yscroll[layer] = data;
                break;
        }

        tilemap_set_scrollx(_tilemap[layer], 0, xscroll[layer] + xdisp[layer]);
        tilemap_set_scrolly(_tilemap[layer], 0, yscroll[layer] + 25);
    }

    public static WriteHandlerPtr baraduke_scroll0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_w(0, offset, data);
        }
    };
    public static WriteHandlerPtr baraduke_scroll1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_w(1, offset, data);
        }
    };

    /**
     * *************************************************************************
     *
     * Display Refresh
     *
     **************************************************************************
     */
    static void draw_sprites(osd_bitmap bitmap, int priority) {
        rectangle clip = new rectangle(Machine.drv.visible_area);

        UBytePtr source = new UBytePtr(spriteram, 0);
        UBytePtr finish = new UBytePtr(spriteram, 0x0800 - 16);/* the last is NOT a sprite */

        int sprite_xoffs = spriteram.read(0x07f5) - 256 * (spriteram.read(0x07f4) & 1) + 16;
        int sprite_yoffs = spriteram.read(0x07f7) - 256 * (spriteram.read(0x07f6) & 1);

        while (source.offset < finish.offset) {
            {
                int attrs = source.read(4);
                int attr2 = source.read(8);
                int color = source.read(6);
                int sx = source.read(7) + (color & 0x01) * 256;
                /* need adjust for left clip */
                int sy = -source.read(9);
                int flipx = attrs & 0x20;
                int flipy = attr2 & 0x01;
                int tall = (attr2 & 0x04) != 0 ? 1 : 0;
                int wide = (attrs & 0x80) != 0 ? 1 : 0;
                int pri = attrs & 0x01;
                int sprite_number = (source.read(5) & 0xff) * 4;
                int row, col;

                if (pri == priority) {
                    if ((attrs & 0x10) != 0 && wide == 0) {
                        sprite_number += 1;
                    }
                    if ((attr2 & 0x10) != 0 && tall == 0) {
                        sprite_number += 2;
                    }
                    color = color >> 1;

                    if (sx > 512 - 32) {
                        sx -= 512;
                    }

                    if (flipx != 0 && wide == 0) {
                        sx -= 16;
                    }
                    if (tall == 0) {
                        sy += 16;
                    }
                    if (tall == 0 && (attr2 & 0x10) != 0 && flipy != 0) {
                        sy -= 16;
                    }

                    sx += sprite_xoffs;
                    sy -= sprite_yoffs;

                    for (row = 0; row <= tall; row++) {
                        for (col = 0; col <= wide; col++) {
                            drawgfx(bitmap, Machine.gfx[5],
                                    sprite_number + 2 * row + col,
                                    color,
                                    flipx, flipy,
                                    -87 + (sx + 16 * (flipx != 0 ? 1 - col : col)),
                                    209 + (sy + 16 * (flipy != 0 ? 1 - row : row)),
                                    clip,
                                    TRANSPARENCY_PEN, 0x0f);
                        }
                    }
                }
            }
            source.inc(16);
        }
    }

    static void mark_textlayer_colors() {
        int i, offs;
        char[] palette_map = new char[512];

        memset(palette_map, 0, palette_map.length);

        for (offs = 0; offs < 0x400; offs++) {
            palette_map[(baraduke_textram.read(offs + 0x400) << 2) & 0x1ff] |= 0xffff;
        }

        /* now build the final table */
        for (i = 0; i < 512; i++) {
            int usage = palette_map[i], j;
            if (usage != 0) {
                for (j = 0; j < 4; j++) {
                    if ((usage & (1 << j)) != 0) {
                        palette_used_colors.write(i * 4 + j, palette_used_colors.read(i * 4 + j) | PALETTE_COLOR_VISIBLE);
                    }
                }
            }
        }
    }

    static void mark_sprites_colors() {
        int i;
        UBytePtr source = new UBytePtr(spriteram, 0);
        UBytePtr finish = new UBytePtr(spriteram, 0x0800 - 16);/* the last is NOT a sprite */

        char[] palette_map = new char[128];

        memset(palette_map, 0, palette_map.length);

        while (source.offset < finish.offset) {
            palette_map[source.read(6) >> 1] |= 0xffff;
            source.inc(16);
        }

        /* now build the final table */
        for (i = 0; i < 128; i++) {
            int usage = palette_map[i], j;
            if (usage != 0) {
                for (j = 0; j < 16; j++) {
                    if ((usage & (1 << j)) != 0) {
                        palette_used_colors.write(i * 16 + j, palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
                    }
                }
            }
        }
    }

    public static VhUpdateHandlerPtr baraduke_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            mark_textlayer_colors();
            mark_sprites_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, _tilemap[1], TILEMAP_IGNORE_TRANSPARENCY);
            draw_sprites(bitmap, 0);
            tilemap_draw(bitmap, _tilemap[0], 0);
            draw_sprites(bitmap, 1);

            for (offs = 0x400 - 1; offs > 0; offs--) {
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
                drawgfx(bitmap, Machine.gfx[0], baraduke_textram.read(offs),
                        (baraduke_textram.read(offs + 0x400) << 2) & 0x1ff,
                        0, 0, sx * 8, sy * 8,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };

    public static VhUpdateHandlerPtr metrocrs_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            tilemap_update(ALL_TILEMAPS);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, _tilemap[0], TILEMAP_IGNORE_TRANSPARENCY);
            draw_sprites(bitmap, 0);
            tilemap_draw(bitmap, _tilemap[1], 0);
            draw_sprites(bitmap, 1);
            for (offs = 0x400 - 1; offs > 0; offs--) {
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
                drawgfx(bitmap, Machine.gfx[0], baraduke_textram.read(offs),
                        (baraduke_textram.read(offs + 0x400) << 2) & 0x1ff,
                        0, 0, sx * 8, sy * 8,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };
}
