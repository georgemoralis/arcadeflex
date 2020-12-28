/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class ttmahjng {

    public static UBytePtr ttmahjng_sharedram = new UBytePtr();
    public static UBytePtr ttmahjng_videoram1 = new UBytePtr();
    public static UBytePtr ttmahjng_videoram2 = new UBytePtr();
    public static int[] ttmahjng_videoram_size = new int[1];

    static osd_bitmap tmpbitmap1;
    static osd_bitmap tmpbitmap2;

    static int video_flip;
    static int video_color_select_1;
    static int video_color_select_2;
    static int video_remap_1;
    static int video_remap_2;
    static UBytePtr ttmahjng_color_prom;

    public static VhConvertColorPromPtr ttmahjng_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            ttmahjng_color_prom = color_prom;
            /* we'll need this later */
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr ttmahjng_vh_start = new VhStartPtr() {
        public int handler() {
            if ((tmpbitmap1 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                return 1;
            }

            if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {

                bitmap_free(tmpbitmap1);
                tmpbitmap1 = null;
                return 1;
            }

            video_flip = 0;
            video_color_select_1 = 0;
            video_color_select_2 = 0;
            video_remap_1 = 1;
            video_remap_2 = 1;

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
    public static VhStopPtr ttmahjng_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap1);
            bitmap_free(tmpbitmap2);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_out0_w
     * *************************************************************************
     */
    static int last_write_1 = 0;
    public static WriteHandlerPtr ttmahjng_out0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == last_write_1) {
                return;
            }

            video_color_select_1 = ((data & 0x1f) << 2);

            /* Bit 5 is the coin counter. */
            coin_counter_w.handler(0, data & 0x20);

            video_remap_1 = 1;
            last_write_1 = data;
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_out1_w
     * *************************************************************************
     */
    static int last_write_2 = 0;
    public static WriteHandlerPtr ttmahjng_out1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == last_write_2) {
                return;
            }

            video_color_select_2 = ((data & 0x1f) << 2);

            if (video_flip != ((data & 0x20) >> 5)) {
                video_flip = (data & 0x20) >> 5;
            }

            video_remap_2 = 1;
            last_write_2 = data;
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_sharedram_r
     * *************************************************************************
     */
    public static ReadHandlerPtr ttmahjng_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ttmahjng_sharedram.read(offset);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_sharedram_w
     * *************************************************************************
     */
    public static WriteHandlerPtr ttmahjng_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ttmahjng_sharedram.write(offset, data);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_videoram1_r
     * *************************************************************************
     */
    public static ReadHandlerPtr ttmahjng_videoram1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ttmahjng_videoram1.read(offset);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_videoram2_r
     * *************************************************************************
     */
    public static ReadHandlerPtr ttmahjng_videoram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ttmahjng_videoram1.read(offset);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_videoram1_w
     * *************************************************************************
     */
    public static WriteHandlerPtr ttmahjng_videoram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ttmahjng_videoram1.write(offset, data);

            common_videoram_w(offset, data, 0, tmpbitmap1);
        }
    };

    /**
     * *************************************************************************
     * ttmahjng_videoram2_w
     * *************************************************************************
     */
    public static WriteHandlerPtr ttmahjng_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            ttmahjng_videoram2.write(offset, data);

            common_videoram_w(offset, data, 4, tmpbitmap2);
        }
    };

    /**
     * *************************************************************************
     * common_videoram_w
     * *************************************************************************
     */
    static void common_videoram_w(int offset, int data, int coloroffset, osd_bitmap bitmap) {
        int x, y, color1, color2, color3, color4;

        x = ((offset & 0x3f) << 2);
        y = (offset & 0xffc0) >> 6;

        if (video_flip != 0) {
            x = 255 - x;
            y = 255 - y;
        }

        color4 = ((data & 0x80) >> 6) | ((data & 0x08) >> 3);
        color3 = ((data & 0x40) >> 5) | ((data & 0x04) >> 2);
        color2 = ((data & 0x20) >> 4) | ((data & 0x02) >> 1);
        color1 = ((data & 0x10) >> 3) | ((data & 0x01));

        if (video_flip != 0) {
            plot_pixel.handler(bitmap, x, y, Machine.pens[color1 | coloroffset]);
            plot_pixel.handler(bitmap, x - 1, y, Machine.pens[color2 | coloroffset]);
            plot_pixel.handler(bitmap, x - 2, y, Machine.pens[color3 | coloroffset]);
            plot_pixel.handler(bitmap, x - 3, y, Machine.pens[color4 | coloroffset]);
        } else {
            plot_pixel.handler(bitmap, x, y, Machine.pens[color1 | coloroffset]);
            plot_pixel.handler(bitmap, x + 1, y, Machine.pens[color2 | coloroffset]);
            plot_pixel.handler(bitmap, x + 2, y, Machine.pens[color3 | coloroffset]);
            plot_pixel.handler(bitmap, x + 3, y, Machine.pens[color4 | coloroffset]);
        }
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
    public static VhUpdatePtr ttmahjng_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (video_remap_1 != 0) {
                modify_pen(0, video_color_select_1 + 0);
                modify_pen(1, video_color_select_1 + 1);
                modify_pen(2, video_color_select_1 + 2);
                modify_pen(3, video_color_select_1 + 3);
            }

            if (video_remap_2 != 0) {
                modify_pen(4, video_color_select_2 + 0);
                modify_pen(5, video_color_select_2 + 1);
                modify_pen(6, video_color_select_2 + 2);
                modify_pen(7, video_color_select_2 + 3);
            }

            if (palette_recalc() != null || video_remap_1 != 0 || video_remap_2 != 0) {
                int offs;

                // redraw bitmaps
                for (offs = 0; offs < ttmahjng_videoram_size[0]; offs++) {
                    ttmahjng_videoram1_w.handler(offs, ttmahjng_videoram1.read(offs));
                    ttmahjng_videoram2_w.handler(offs, ttmahjng_videoram2.read(offs));
                }
            }

            video_remap_1 = 0;
            video_remap_2 = 0;

            copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
            copybitmap(bitmap, tmpbitmap1, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_COLOR, 0);
        }
    };

    /**
     * *************************************************************************
     * mofify_pen
     * *************************************************************************
     */
    static void modify_pen(int pen, int colorindex) {
        int r, g, b, color;

        color = ttmahjng_color_prom.read(colorindex);

        b = ((color & 1) != 0 ? 0xff : 0x00);
        g = ((color & 2) != 0 ? 0xff : 0x00);
        r = ((color & 4) != 0 ? 0xff : 0x00);

        palette_change_color(pen, r, g, b);
    }
}
