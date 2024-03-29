/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 15/01/2023 - shadow - This file should be complete for 0.36 version
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
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class vigilant {

    static rectangle topvisiblearea = new rectangle(
            16 * 8, 48 * 8 - 1,
            0 * 8, 6 * 8 - 1
    );
    static rectangle bottomvisiblearea = new rectangle(
            16 * 8, 48 * 8 - 1,
            6 * 8, 32 * 8 - 1
    );

    public static UBytePtr vigilant_paletteram = new UBytePtr();
    public static UBytePtr vigilant_sprite_paletteram = new UBytePtr();

    static int horiz_scroll_low = 0;
    static int horiz_scroll_high = 0;
    static int rear_horiz_scroll_low = 0;
    static int rear_horiz_scroll_high = 0;
    static int rear_color = 0;
    static int rear_disable = 1;

    static int rear_refresh = 1;

    static osd_bitmap bg_bitmap;

    public static VhStartHandlerPtr vigilant_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            generic_vh_start.handler();

            if ((bg_bitmap = osd_create_bitmap(512 * 3, 256)) == null) {
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr vigilant_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(bg_bitmap);
            generic_vh_stop.handler();
        }
    };

    /**
     * *************************************************************************
     * update_background
     *
     * There are three background ROMs, each one contains a 512x256 picture.
     * Redraw them if the palette changes.
     * ************************************************************************
     */
    static void update_background() {
        int row, col, page;
        int charcode;

        charcode = 0;

        /* There are only three background ROMs */
        for (page = 0; page < 3; page++) {
            for (row = 0; row < 256; row++) {
                for (col = 0; col < 512; col += 32) {
                    drawgfx(bg_bitmap,
                            Machine.gfx[2],
                            charcode,
                            row < 128 ? 0 : 1,
                            0, 0,
                            512 * page + col, row,
                            null, TRANSPARENCY_NONE, 0);
                    charcode++;
                }
            }
        }
    }

    /**
     * *************************************************************************
     * vigilant_paletteram_w
     *
     * There are two palette chips, each one is labelled "KNA91H014". One is
     * used for the sprites, one is used for the two background layers.
     *
     * The chip has three enables (!CS, !E0, !E1), R/W pins, A0-A7 as input, 'L'
     * and 'H' inputs, and D0-D4 as input. 'L' and 'H' are used to bank into
     * Red, Green, and Blue memory. There are only 5 bits of memory for each
     * byte, and a total of 256*3 bytes memory per chip.
     *
     * There are additionally two sets of D0-D7 inputs per chip labelled 'A' and
     * 'B'. There is also an 'S' pin to select between the two input sets. These
     * are used to index a color triplet of RGB. The triplet is read from RAM,
     * and output to R0-R4, G0-G4, and B0-B4.
     * ************************************************************************
     */
    public static WriteHandlerPtr vigilant_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bank, r, g, b;

            paletteram.write(offset, data);

            bank = offset & 0x400;
            offset &= 0xff;

            r = (paletteram.read(bank + offset + 0x000) << 3) & 0xFF;
            g = (paletteram.read(bank + offset + 0x100) << 3) & 0xFF;
            b = (paletteram.read(bank + offset + 0x200) << 3) & 0xFF;

            palette_change_color((bank >> 2) + offset, r, g, b);
        }
    };

    /**
     * *************************************************************************
     * vigilant_horiz_scroll_w
     *
     * horiz_scroll_low = HSPL, an 8-bit register horiz_scroll_high = HSPH, a
     * 1-bit register
     * ************************************************************************
     */
    public static WriteHandlerPtr vigilant_horiz_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                horiz_scroll_low = data;
            } else {
                horiz_scroll_high = (data & 0x01) * 256;
            }
        }
    };

    /**
     * *************************************************************************
     * vigilant_rear_horiz_scroll_w
     *
     * rear_horiz_scroll_low = RHSPL, an 8-bit register rear_horiz_scroll_high =
     * RHSPH, an 8-bit register but only 3 bits are saved
     * *************************************************************************
     */
    public static WriteHandlerPtr vigilant_rear_horiz_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                rear_horiz_scroll_low = data;
            } else {
                rear_horiz_scroll_high = (data & 0x07) * 256;
            }
        }
    };

    /**
     * *************************************************************************
     * vigilant_rear_color_w
     *
     * This is an 8-bit register labelled RCOD. D6 is hooked to !ROME
     * (rear_disable) D3 = RCC2 (rear color bit 2) D2 = RCC1 (rear color bit 1)
     * D0 = RCC0 (rear color bit 0)
     *
     * I know it looks odd, but D1, D4, D5, and D7 are empty.
     *
     * What makes this extremely odd is that RCC is supposed to hook up to the
     * palette. However, the top four bits of the palette inputs are labelled:
     * "RCC3", "RCC2", "V256E", "RCC0". Methinks there's a typo.
     * ************************************************************************
     */
    public static WriteHandlerPtr vigilant_rear_color_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            rear_disable = data & 0x40;
            rear_color = (data & 0x0d);
        }
    };

    /**
     * *************************************************************************
     * draw_foreground
     *
     * ???
     * ************************************************************************
     */
    static void draw_foreground(osd_bitmap bitmap, int priority, int opaque) {
        int offs;
        int scroll = -(horiz_scroll_low + horiz_scroll_high);

        for (offs = 0; offs < videoram_size[0]; offs += 2) {
            int sy = 8 * ((offs / 2) / 64);
            int sx = 8 * ((offs / 2) % 64);
            int attributes = videoram.read(offs + 1);
            int color = attributes & 0x0F;
            int tile_number = videoram.read(offs) | ((attributes & 0xF0) << 4);

            if (priority != 0) /* foreground */ {
                if ((color & 0x0c) == 0x0c) /* mask sprites */ {
                    if (sy >= 48) {
                        sx = (sx + scroll) & 0x1ff;

                        if (sx > 16 * 8 - 8 && sx < 48 * 8) {
                            drawgfx(bitmap, Machine.gfx[0],
                                    tile_number,
                                    color,
                                    0, 0,
                                    sx, sy,
                                    bottomvisiblearea, TRANSPARENCY_PENS, 0x00ff);
                        }
                    }
                }
            } else /* background */ {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            tile_number,
                            color,
                            0, 0,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }
        }

        if (priority == 0) {
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, topvisiblearea, TRANSPARENCY_NONE, 0);
            copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll}, 0, null, bottomvisiblearea,
                    opaque != 0 ? TRANSPARENCY_NONE : TRANSPARENCY_PEN, palette_transparent_pen);
        }
    }

    /**
     * *************************************************************************
     * draw_background
     *
     * ???
     * ************************************************************************
     */
    static void draw_background(osd_bitmap bitmap) {
        int scrollx = 0x17a + 16 * 8 - (rear_horiz_scroll_low + rear_horiz_scroll_high);

        if (rear_refresh != 0) {
            update_background();
            rear_refresh = 0;
        }

        copyscrollbitmap(bitmap, bg_bitmap, 1, new int[]{scrollx}, 0, null, bottomvisiblearea, TRANSPARENCY_NONE, 0);
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    static void draw_sprites(osd_bitmap bitmap, rectangle clip) {
        int offs;

        for (offs = 0; offs < spriteram_size[0]; offs += 8) {
            int code, color, sx, sy, flipx, flipy, h, y;

            code = spriteram.read(offs + 4) | ((spriteram.read(offs + 5) & 0x0f) << 8);
            color = spriteram.read(offs + 0) & 0x0f;
            sx = (spriteram.read(offs + 6) | ((spriteram.read(offs + 7) & 0x01) << 8));
            sy = 256 + 128 - (spriteram.read(offs + 2) | ((spriteram.read(offs + 3) & 0x01) << 8));
            flipx = spriteram.read(offs + 5) & 0x40;
            flipy = spriteram.read(offs + 5) & 0x80;
            h = 1 << ((spriteram.read(offs + 5) & 0x30) >> 4);
            sy -= 16 * h;

            for (y = 0; y < h; y++) {
                int c = code;

                if (flipy != 0) {
                    c += h - 1 - y;
                } else {
                    c += y;
                }

                drawgfx(bitmap, Machine.gfx[1],
                        c,
                        color,
                        flipx, flipy,
                        sx, sy + 16 * y,
                        clip, TRANSPARENCY_PEN, 0);
            }
        }
    }

    public static VhUpdateHandlerPtr vigilant_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i;

            if (rear_disable != 0) /* opaque foreground */ {
                for (i = 0; i < 8; i++) {
                    palette_used_colors.write(256 + 16 * i, PALETTE_COLOR_USED);
                }
            } else {
                for (i = 0; i < 8; i++) {
                    palette_used_colors.write(256 + 16 * i, PALETTE_COLOR_TRANSPARENT);
                }
            }

            /* copy the background palette */
            for (i = 0; i < 16; i++) {
                int r, g, b;

                r = (paletteram.read(0x400 + 16 * rear_color + i) << 3) & 0xFF;
                g = (paletteram.read(0x500 + 16 * rear_color + i) << 3) & 0xFF;
                b = (paletteram.read(0x600 + 16 * rear_color + i) << 3) & 0xFF;

                palette_change_color(512 + i, r, g, b);

                r = (paletteram.read(0x400 + 16 * rear_color + 32 + i) << 3) & 0xFF;
                g = (paletteram.read(0x500 + 16 * rear_color + 32 + i) << 3) & 0xFF;
                b = (paletteram.read(0x600 + 16 * rear_color + 32 + i) << 3) & 0xFF;

                palette_change_color(512 + 16 + i, r, g, b);
            }

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                rear_refresh = 1;
            }

            if (rear_disable != 0) /* opaque foreground */ {
                draw_foreground(bitmap, 0, 1);
                draw_sprites(bitmap, bottomvisiblearea);
                draw_foreground(bitmap, 1, 1);
            } else {
                draw_background(bitmap);
                draw_foreground(bitmap, 0, 0);
                draw_sprites(bitmap, bottomvisiblearea);
                draw_foreground(bitmap, 1, 0); // priority tiles
            }
        }
    };

    public static VhUpdateHandlerPtr kikcubic_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            for (offs = 0; offs < videoram_size[0]; offs += 2) {
                int sy = 8 * ((offs / 2) / 64);
                int sx = 8 * ((offs / 2) % 64);
                int attributes = videoram.read(offs + 1);
                int color = (attributes & 0xF0) >> 4;
                int tile_number = videoram.read(offs) | ((attributes & 0x0F) << 8);

                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    dirtybuffer[offs] = dirtybuffer[offs + 1] = 0;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            tile_number,
                            color,
                            0, 0,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            draw_sprites(bitmap, Machine.drv.visible_area);
        }
    };
}
