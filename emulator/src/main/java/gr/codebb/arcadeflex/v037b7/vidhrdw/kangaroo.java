/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class kangaroo {

    public static UBytePtr kangaroo_video_control = new UBytePtr();
    public static UBytePtr kangaroo_bank_select = new UBytePtr();
    public static UBytePtr kangaroo_blitter = new UBytePtr();
    public static UBytePtr kangaroo_scroll = new UBytePtr();

    static int screen_flipped;
    static osd_bitmap tmpbitmap2;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Kangaroo doesn't have color PROMs, the playfield data is directly
     * converted into colors: 1 bit per gun, therefore only 8 possible colors,
     * but there is also a global mask register which controls intensities of
     * the three guns, separately for foreground and background. The fourth bit
     * in the video RAM data disables this mask, making the color display at
     * full intensity regardless of the mask value. Actually the mask doesn't
     * directly control intensity. The guns are rapidly turned on and off at a
     * subpixel rate, relying on the monitor to blend the colors into a more or
     * less uniform half intensity color.
     *
     * We use three groups of 8 pens: the first is fixed and contains the 8
     * possible colors; the other two are dynamically modified when the mask
     * register is written to, one is for the background, the other for sprites.
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr kangaroo_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                palette[p_inc++] = ((char) (((i & 4) >> 2) * 0xff));
                palette[p_inc++] = ((char) (((i & 2) >> 1) * 0xff));
                palette[p_inc++] = ((char) (((i & 1) >> 0) * 0xff));
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
    public static VhStartPtr kangaroo_vh_start = new VhStartPtr() {
        public int handler() {
            if ((tmpbitmap = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                return 1;
            }

            if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                bitmap_free(tmpbitmap);
                return 1;
            }

            if ((videoram = new UBytePtr(Machine.drv.screen_width * Machine.drv.screen_height)) == null) {
                bitmap_free(tmpbitmap);
                bitmap_free(tmpbitmap2);
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr kangaroo_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap2);
            bitmap_free(tmpbitmap);
            videoram = null;
        }
    };

    public static WriteHandlerPtr kangaroo_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* A & B bitmap control latch (A=playfield B=motion)
			  bit 5 FLIP A
			  bit 4 FLIP B
			  bit 3 EN A
			  bit 2 EN B
			  bit 1 PRI A
			  bit 0 PRI B */

            if ((kangaroo_video_control.read() & 0x30) != (data & 0x30)) {
                screen_flipped = 1;
            }

            kangaroo_video_control.write(data);
        }
    };

    public static WriteHandlerPtr kangaroo_bank_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* this is a VERY crude way to handle the banked ROMs - but it's */
 /* correct enough to pass the self test */
            if ((data & 0x05) != 0) {
                cpu_setbank(1, new UBytePtr(RAM, 0x10000));
            } else {
                cpu_setbank(1, new UBytePtr(RAM, 0x12000));
            }

            kangaroo_bank_select.write(data);
        }
    };

    public static WriteHandlerPtr kangaroo_color_mask_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;

            /* color mask for A plane */
            for (i = 0; i < 8; i++) {
                int r, g, b;

                r = ((i & 4) >> 2) * ((data & 0x20) != 0 ? 0xff : 0x7f);
                g = ((i & 2) >> 1) * ((data & 0x10) != 0 ? 0xff : 0x7f);
                b = ((i & 1) >> 0) * ((data & 0x08) != 0 ? 0xff : 0x7f);

                palette_change_color(8 + i, r, g, b);
            }

            /* color mask for B plane */
            for (i = 0; i < 8; i++) {
                int r, g, b;

                r = ((i & 4) >> 2) * ((data & 0x04) != 0 ? 0xff : 0x7f);
                g = ((i & 2) >> 1) * ((data & 0x02) != 0 ? 0xff : 0x7f);
                b = ((i & 1) >> 0) * ((data & 0x01) != 0 ? 0xff : 0x7f);

                palette_change_color(16 + i, r, g, b);
            }
        }
    };

    public static WriteHandlerPtr kangaroo_blitter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            kangaroo_blitter.write(offset, data);

            if (offset == 5) /* trigger DMA */ {
                int src, dest;
                int x, y, xb, yb, old_bank_select, new_bank_select;

                src = kangaroo_blitter.read(0) + 256 * kangaroo_blitter.read(1);
                dest = kangaroo_blitter.read(2) + 256 * kangaroo_blitter.read(3);

                xb = kangaroo_blitter.read(5);
                yb = kangaroo_blitter.read(4);

                old_bank_select = new_bank_select = kangaroo_bank_select.read();

                if ((new_bank_select & 0x0c) != 0) {
                    new_bank_select |= 0x0c;
                }
                if ((new_bank_select & 0x03) != 0) {
                    new_bank_select |= 0x03;
                }
                kangaroo_bank_select_w.handler(0, new_bank_select & 0x05);

                for (x = 0; x <= xb; x++) {
                    for (y = 0; y <= yb; y++) {
                        cpu_writemem16(dest++, cpu_readmem16(src++));
                    }

                    dest = dest - (yb + 1) + 256;
                }

                src = kangaroo_blitter.read(0) + 256 * kangaroo_blitter.read(1);
                dest = kangaroo_blitter.read(2) + 256 * kangaroo_blitter.read(3);

                kangaroo_bank_select_w.handler(0, new_bank_select & 0x0a);

                for (x = 0; x <= xb; x++) {
                    for (y = 0; y <= yb; y++) {
                        cpu_writemem16(dest++, cpu_readmem16(src++));
                    }

                    dest = dest - (yb + 1) + 256;
                }

                kangaroo_bank_select_w.handler(0, old_bank_select);
            }
        }
    };

    public static void kangaroo_plot_pixel(osd_bitmap bitmap, int x, int y, int col, int color_base, int flip) {
        if (flip != 0) {
            x = bitmap.width - 1 - x;
            y = bitmap.height - 1 - y;
        }

        plot_pixel.handler(bitmap, x, y, Machine.pens[((col & 0x08) != 0 ? 0 : color_base) + (col & 0x07)]);
    }

    public static void kangaroo_redraw_4pixels(int x, int y) {
        int offs, flipA, flipB;

        offs = y * 256 + x;

        flipA = kangaroo_video_control.read() & 0x20;
        flipB = kangaroo_video_control.read() & 0x10;

        kangaroo_plot_pixel(tmpbitmap, x, y, videoram.read(offs) & 0x0f, 8, flipA);
        kangaroo_plot_pixel(tmpbitmap, x + 1, y, videoram.read(offs + 1) & 0x0f, 8, flipA);
        kangaroo_plot_pixel(tmpbitmap, x + 2, y, videoram.read(offs + 2) & 0x0f, 8, flipA);
        kangaroo_plot_pixel(tmpbitmap, x + 3, y, videoram.read(offs + 3) & 0x0f, 8, flipA);
        kangaroo_plot_pixel(tmpbitmap2, x, y, videoram.read(offs) >> 4, 16, flipB);
        kangaroo_plot_pixel(tmpbitmap2, x + 1, y, videoram.read(offs + 1) >> 4, 16, flipB);
        kangaroo_plot_pixel(tmpbitmap2, x + 2, y, videoram.read(offs + 2) >> 4, 16, flipB);
        kangaroo_plot_pixel(tmpbitmap2, x + 3, y, videoram.read(offs + 3) >> 4, 16, flipB);
    }

    public static WriteHandlerPtr kangaroo_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int a_Z_R, a_G_B, b_Z_R, b_G_B;
            int sx, sy, offs;

            a_Z_R = kangaroo_bank_select.read() & 0x01;
            a_G_B = kangaroo_bank_select.read() & 0x02;
            b_Z_R = kangaroo_bank_select.read() & 0x04;
            b_G_B = kangaroo_bank_select.read() & 0x08;

            sx = (offset / 256) * 4;
            sy = offset % 256;
            offs = sy * 256 + sx;

            if (a_G_B != 0) {
                videoram.write(offs, (videoram.read(offs) & 0xfc) | ((data & 0x10) >> 3) | ((data & 0x01) >> 0));
                videoram.write(offs + 1, (videoram.read(offs + 1) & 0xfc) | ((data & 0x20) >> 4) | ((data & 0x02) >> 1));
                videoram.write(offs + 2, (videoram.read(offs + 2) & 0xfc) | ((data & 0x40) >> 5) | ((data & 0x04) >> 2));
                videoram.write(offs + 3, (videoram.read(offs + 3) & 0xfc) | ((data & 0x80) >> 6) | ((data & 0x08) >> 3));
            }

            if (a_Z_R != 0) {
                videoram.write(offs, (videoram.read(offs) & 0xf3) | ((data & 0x10) >> 1) | ((data & 0x01) << 2));
                videoram.write(offs + 1, (videoram.read(offs + 1) & 0xf3) | ((data & 0x20) >> 2) | ((data & 0x02) << 1));
                videoram.write(offs + 2, (videoram.read(offs + 2) & 0xf3) | ((data & 0x40) >> 3) | ((data & 0x04) >> 0));
                videoram.write(offs + 3, (videoram.read(offs + 3) & 0xf3) | ((data & 0x80) >> 4) | ((data & 0x08) >> 1));
            }

            if (b_G_B != 0) {
                videoram.write(offs, (videoram.read(offs) & 0xcf) | ((data & 0x10) << 1) | ((data & 0x01) << 4));
                videoram.write(offs + 1, (videoram.read(offs + 1) & 0xcf) | ((data & 0x20) >> 0) | ((data & 0x02) << 3));
                videoram.write(offs + 2, (videoram.read(offs + 2) & 0xcf) | ((data & 0x40) >> 1) | ((data & 0x04) << 2));
                videoram.write(offs + 3, (videoram.read(offs + 3) & 0xcf) | ((data & 0x80) >> 2) | ((data & 0x08) << 1));
            }

            if (b_Z_R != 0) {
                videoram.write(offs, (videoram.read(offs) & 0x3f) | ((data & 0x10) << 3) | ((data & 0x01) << 6));
                videoram.write(offs + 1, (videoram.read(offs + 1) & 0x3f) | ((data & 0x20) << 2) | ((data & 0x02) << 5));
                videoram.write(offs + 2, (videoram.read(offs + 2) & 0x3f) | ((data & 0x40) << 1) | ((data & 0x04) << 4));
                videoram.write(offs + 3, (videoram.read(offs + 3) & 0x3f) | ((data & 0x80) << 0) | ((data & 0x08) << 3));
            }

            kangaroo_redraw_4pixels(sx, sy);
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr kangaroo_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int scrollx, scrolly;

            if (palette_recalc() != null || screen_flipped != 0) {
                int x, y;

                /* redraw bitmap */
                for (x = 0; x < 256; x += 4) {
                    for (y = 0; y < 256; y++) {
                        kangaroo_redraw_4pixels(x, y);
                    }
                }

                screen_flipped = 0;
            }

            scrollx = kangaroo_scroll.read(1);
            scrolly = kangaroo_scroll.read(0);

            if ((kangaroo_bank_select.read() & 0x01) != 0) {
                /* Plane B is primary */
                copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_COLOR, 8);
            } else {
                /* Plane A is primary */
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
                copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_COLOR, 16);
            }
        }
    };
}
