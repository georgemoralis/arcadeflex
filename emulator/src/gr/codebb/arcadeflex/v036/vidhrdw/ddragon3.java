/**
 * *************************************************************************
 *
 * Video Hardware for Double Dragon 3
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;

public class ddragon3 {

    public static UBytePtr ddragon3_bg_videoram = new UBytePtr();
    static int ddragon3_bg_scrollx;
    static int ddragon3_bg_scrolly;

    static int ddragon3_bg_tilebase;
    static int old_ddragon3_bg_tilebase;

    public static UBytePtr ddragon3_fg_videoram = new UBytePtr();
    static int ddragon3_fg_scrollx;
    static int ddragon3_fg_scrolly;
    public static char ddragon3_vreg;

    static tilemap background, foreground;

    /* scroll write function */
    public static WriteHandlerPtr ddragon3_scroll_write = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x0: /* Scroll X, BG1 */

                    ddragon3_fg_scrollx = data;
                    return;

                case 0x2: /* Scroll Y, BG1 */

                    ddragon3_fg_scrolly = data;
                    return;

                case 0x4: /* Scroll X, BG0 */

                    ddragon3_bg_scrollx = data;
                    return;

                case 0x6: /* Scroll Y, BG0 */

                    ddragon3_bg_scrolly = data;
                    return;

                case 0xc: /* BG Tile Base */

                    ddragon3_bg_tilebase = (COMBINE_WORD(ddragon3_bg_tilebase, data) & 0x1ff);
                    return;

                default:  /* Unknown */

                    if (errorlog != null) {
                        fprintf(errorlog, "OUTPUT c00[%02x] %02x \n", offset, data);
                    }
                    break;
            }
        }
    };

    /* background */
    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int index = col + row * 32 + 0;
            char data = (char) (ddragon3_bg_videoram.memory[ddragon3_bg_videoram.offset + 1 + index * 2] << 8 | ddragon3_bg_videoram.memory[ddragon3_bg_videoram.offset + index * 2]);
            SET_TILE_INFO(0, (data & 0xfff) | ((ddragon3_bg_tilebase & 1) << 12), ((data & 0xf000) >> 12) + 16);  // GFX,NUMBER,COLOR
        }
    };

    public static WriteHandlerPtr ddragon3_bg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = ddragon3_bg_videoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);
            if (oldword != newword) {
                ddragon3_bg_videoram.WRITE_WORD(offset, newword);
                offset = offset / 2;
                tilemap_mark_tile_dirty(background, offset % 32, offset / 32);
            }
        }
    };

    public static ReadHandlerPtr ddragon3_bg_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ddragon3_bg_videoram.READ_WORD(offset);
        }
    };

    /* foreground */
    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int index1 = col * 2 + row * 64 + 0;
            char data0 = (char) (ddragon3_fg_videoram.memory[ddragon3_fg_videoram.offset + 1 + index1 * 2] << 8 | ddragon3_fg_videoram.memory[ddragon3_fg_videoram.offset + index1 * 2]);
            int index2 = col * 2 + row * 64 + 1;
            char data1 = (char) (ddragon3_fg_videoram.memory[ddragon3_fg_videoram.offset + 1 + index2 * 2] << 8 | ddragon3_fg_videoram.memory[ddragon3_fg_videoram.offset + index2 * 2]);
            SET_TILE_INFO(0, data1 & 0x1fff, data0 & 0xf);  // GFX,NUMBER,COLOR
            tile_info.flags = (char) ((data0 & 0x40) >> 6);  // FLIPX
        }
    };

    public static WriteHandlerPtr ddragon3_fg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = ddragon3_fg_videoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);
            if (oldword != newword) {
                ddragon3_fg_videoram.WRITE_WORD(offset, newword);
                offset = offset / 4;
                tilemap_mark_tile_dirty(foreground, offset % 32, offset / 32);
            }
        }
    };

    public static ReadHandlerPtr ddragon3_fg_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ddragon3_fg_videoram.READ_WORD(offset);
        }
    };

    /* start & stop */
    public static VhStartPtr ddragon3_vh_start = new VhStartPtr() {
        public int handler() {
            ddragon3_bg_tilebase = 0;
            old_ddragon3_bg_tilebase = -1;

            background = tilemap_create(
                    get_bg_tile_info,
                    0,
                    16, 16, /* tile width, tile height */
                    32, 32 /* number of columns, number of rows */
            );

            foreground = tilemap_create(
                    get_fg_tile_info,
                    TILEMAP_TRANSPARENT,
                    16, 16, /* tile width, tile height */
                    32, 32 /* number of columns, number of rows */
            );

            if (background != null && foreground != null) {
                foreground.transparent_pen = 0;
                return 0;
            }
            return 1;
        }
    };

    /*
     * Sprite Format
     * ----------------------------------
     *
     * Word | Bit(s)           | Use
     * -----+-fedcba9876543210-+----------------
     *   0	| --------xxxxxxxx | ypos (signed)
     * -----+------------------+
     *   1	| --------xxx----- | height
     *   1  | -----------xx--- | yflip, xflip
     *   1  | -------------x-- | msb x
     *   1  | --------------x- | msb y?
     *   1  | ---------------x | enable
     * -----+------------------+
     *   2  | --------xxxxxxxx | tile number
     * -----+------------------+
     *   3  | --------xxxxxxxx | bank
     * -----+------------------+
     *   4  | ------------xxxx |color
     * -----+------------------+
     *   5  | --------xxxxxxxx | xpos
     * -----+------------------+
     *   6,7| unused
     */
    static void draw_sprites(osd_bitmap bitmap) {
        rectangle clip =  Machine.drv.visible_area;
        GfxElement gfx = Machine.gfx[1];
        UShortPtr source = new UShortPtr(spriteram);
        UShortPtr finish = new UShortPtr(source,0x800);

        while (source.offset < finish.offset) {
            char attributes = source.read(1);
            if ((attributes & 0x01) != 0) { /* enable */

                int flipx = attributes & 0x10;
                int flipy = attributes & 0x08;
                int height = (attributes >>> 5) & 0x7;

                int sy = source.read(0) & 0xff;
                int sx = source.read(5) & 0xff;
                char tile_number = (char)(source.read(2) & 0xff);
                char color = (char)(source.read(4) & 0xf);
                int bank = source.read(3) & 0xff;
                int i;

                if ((attributes & 0x04) != 0) {
                    sx |= 0x100;
                }
                if ((attributes & 0x02) != 0) {
                    sy = 239 + (0x100 - sy);
                } else {
                    sy = 240 - sy;
                }
                if (sx > 0x17f) {
                    sx = 0 - (0x200 - sx);
                }

                tile_number += (bank * 256);

                for (i = 0; i <= height; i++) {
                    int tile_index = tile_number + i;

                    drawgfx(bitmap, gfx,
                            tile_index,
                            color,
                            flipx, flipy,
                            sx, sy - i * 16,
                            clip, TRANSPARENCY_PEN, 0);
                }
            }
            source.offset += 16;//source.offset+=8; //change cause of use of ushortptr (shadow)
        }
    }

    static void mark_sprite_colors() {
        int offs, color, i, pal_base, sprite, multi, attr;
        int[] colmask = new int[16];
        int[] pen_usage; /* Save some struct derefs */

        /* Sprites */
        pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
        pen_usage = Machine.gfx[1].pen_usage;
        for (color = 0; color < 16; color++) {
            colmask[color] = 0;
        }
        for (offs = 0; offs < 0x1000; offs += 16) {
            attr = spriteram.READ_WORD(offs + 2);
            if ((attr & 1) == 0) {
                continue;
            }

            multi = (attr >> 5) & 0x7;
            sprite = spriteram.READ_WORD(offs + 4) & 0xff;
            sprite += ((spriteram.READ_WORD(offs + 6) & 0xff) << 8);
            color = spriteram.READ_WORD(offs + 8) & 0xf;

            while (multi >= 0) {
                colmask[color] |= pen_usage[sprite + multi];
                multi--;
            }
        }

        for (color = 0; color < 16; color++) {
            for (i = 1; i < 16; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
        }
    }

    public static VhUpdatePtr ddragon3_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (ddragon3_bg_tilebase != old_ddragon3_bg_tilebase) {
                old_ddragon3_bg_tilebase = ddragon3_bg_tilebase;
                tilemap_mark_all_tiles_dirty(background);
            }

            tilemap_set_scrolly(background, 0, ddragon3_bg_scrolly);
            tilemap_set_scrollx(background, 0, ddragon3_bg_scrollx);

            tilemap_set_scrolly(foreground, 0, ddragon3_fg_scrolly);
            tilemap_set_scrollx(foreground, 0, ddragon3_fg_scrollx);

            tilemap_update(background);
            tilemap_update(foreground);

            palette_init_used_colors();
            mark_sprite_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(background);
            tilemap_render(foreground);

            if ((ddragon3_vreg & 0x40) != 0) {
                tilemap_draw(bitmap, background, 0);
                tilemap_draw(bitmap, foreground, 0);
                draw_sprites(bitmap);
            } else {
                tilemap_draw(bitmap, background, 0);
                draw_sprites(bitmap);
                tilemap_draw(bitmap, foreground, 0);
            }
        }
    };

    public static VhUpdatePtr ctribe_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (ddragon3_bg_tilebase != old_ddragon3_bg_tilebase) {
                old_ddragon3_bg_tilebase = ddragon3_bg_tilebase;
                tilemap_mark_all_tiles_dirty(background);
            }

            tilemap_set_scrolly(background, 0, ddragon3_bg_scrolly);
            tilemap_set_scrollx(background, 0, ddragon3_bg_scrollx);
            tilemap_set_scrolly(foreground, 0, ddragon3_fg_scrolly);
            tilemap_set_scrollx(foreground, 0, ddragon3_fg_scrollx);

            tilemap_update(background);
            tilemap_update(foreground);

            palette_init_used_colors();
            mark_sprite_colors();
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(background);
            tilemap_render(foreground);

            tilemap_draw(bitmap, background, 0);
            tilemap_draw(bitmap, foreground, 0);
            draw_sprites(bitmap);
        }
    };
}
