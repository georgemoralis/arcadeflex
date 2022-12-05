/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//mame imports
import static arcadeflex.v036.mame.artwork.*;
import static arcadeflex.v036.mame.artworkH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_PROMS;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.copybitmap;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.plot_pixel;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.plot_pixel2;
import gr.codebb.arcadeflex.v036.mame.driverH.InitDriverPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhConvertColorPromPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhStartPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhStopPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhUpdatePtr;
import gr.codebb.arcadeflex.v036.mame.driverH.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.TRANSPARENCY_NONE;
import gr.codebb.arcadeflex.v037b7.mame.drawgfxH.rectangle;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_0_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_1_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_2_r;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_3_r;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;

public class _8080bw {

    static int use_tmpbitmap;
    static int flipscreen;
    static int screen_red;
    static int screen_red_enabled;/* 1 for games that can turn the screen red */
    static int redraw_screen;
    static int color_map_select;

    static artwork_element[] init_overlay;
    static struct_artwork overlay;

    static WriteHandlerPtr videoram_w_p;
    static VhUpdatePtr vh_screenrefresh_p;

    static abstract interface plot_pixel_Ptr {

        public abstract void handler(int x, int y, int col);
    }
    static plot_pixel_Ptr plot_pixel_p;

    /* smoothed pure colors, overlays are not so contrasted */
    static char[] BLACK = {0x00, 0x00, 0x00};
    static char[] RED = {0xff, 0x20, 0x20};
    static char[] GREEN = {0x20, 0xff, 0x20};
    static char[] YELLOW = {0xff, 0xff, 0x20};
    static char[] WHITE = {0xff, 0xff, 0xff};
    static char[] CYAN = {0x20, 0xff, 0xff};
    static char[] PURPLE = {0xff, 0x20, 0xff};

    static char[] ORANGE = {0xff, 0x90, 0x20};
    static char[] YELLOW_GREEN = {0x90, 0xff, 0x20};
    static char[] GREEN_CYAN = {0x20, 0xff, 0x90};

    static artwork_element invaders_overlay[]
            = {
                new artwork_element(new rectangle(0, 255, 0, 255), WHITE, (char) 0xff),
                new artwork_element(new rectangle(16, 71, 0, 255), GREEN, (char) 0xff),
                new artwork_element(new rectangle(0, 15, 16, 133), GREEN, (char) 0xff),
                new artwork_element(new rectangle(192, 223, 0, 255), RED, (char) 0xff),
                new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
            };

    static artwork_element invdpt2m_overlay[]
            = {
                new artwork_element(new rectangle(0, 255, 0, 255), WHITE, (char) 0xff),
                new artwork_element(new rectangle(16, 71, 0, 255), GREEN, (char) 0xff),
                new artwork_element(new rectangle(0, 15, 16, 133), GREEN, (char) 0xff),
                new artwork_element(new rectangle(72, 191, 0, 255), YELLOW, (char) 0xff),
                new artwork_element(new rectangle(192, 223, 0, 255), RED, (char) 0xff),
                new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
            };

    static artwork_element invrvnge_overlay[]
            = {
                new artwork_element(new rectangle(0, 255, 0, 255), WHITE, (char) 0xff),
                new artwork_element(new rectangle(0, 71, 0, 255), GREEN, (char) 0xff),
                new artwork_element(new rectangle(192, 223, 0, 255), RED, (char) 0xff),
                new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
            };

    static artwork_element invad2ct_overlay[]
            = {
                new artwork_element(new rectangle(0, 24, 0, 255), YELLOW, (char) 0xff),
                new artwork_element(new rectangle(25, 47, 0, 255), YELLOW_GREEN, (char) 0xff),
                new artwork_element(new rectangle(48, 70, 0, 255), GREEN_CYAN, (char) 0xff),
                new artwork_element(new rectangle(71, 116, 0, 255), CYAN, (char) 0xff),
                new artwork_element(new rectangle(117, 139, 0, 255), GREEN_CYAN, (char) 0xff),
                new artwork_element(new rectangle(140, 162, 0, 255), GREEN, (char) 0xff),
                new artwork_element(new rectangle(163, 185, 0, 255), YELLOW_GREEN, (char) 0xff),
                new artwork_element(new rectangle(186, 208, 0, 255), YELLOW, (char) 0xff),
                new artwork_element(new rectangle(209, 231, 0, 255), ORANGE, (char) 0xff),
                new artwork_element(new rectangle(232, 255, 0, 255), RED, (char) 0xff),
                new artwork_element(new rectangle(-1, -1, -1, -1), new char[]{0, 0, 0}, (char) 0)
            };

    public static InitDriverPtr init_8080bw = new InitDriverPtr() {
        public void handler() {
            videoram_w_p = bw_videoram_w;
            vh_screenrefresh_p = vh_screenrefresh;
            use_tmpbitmap = 0;
            screen_red_enabled = 0;
            init_overlay = null;
            color_map_select = 0;
            flipscreen = 0;
        }
    };

    public static InitDriverPtr init_invaders = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            init_overlay = invaders_overlay;
        }
    };

    public static InitDriverPtr init_invdpt2m = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            init_overlay = invdpt2m_overlay;
        }
    };

    public static InitDriverPtr init_invrvnge = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            init_overlay = invrvnge_overlay;
        }
    };

    public static InitDriverPtr init_invad2ct = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            init_overlay = invad2ct_overlay;
        }
    };

    public static InitDriverPtr init_schaser = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            videoram_w_p = schaser_videoram_w;
        }
    };

    public static InitDriverPtr init_rollingc = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            videoram_w_p = rollingc_videoram_w;
        }
    };

    public static InitDriverPtr init_invadpt2 = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            videoram_w_p = invadpt2_videoram_w;
            screen_red_enabled = 1;
        }
    };

    public static InitDriverPtr init_seawolf = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            vh_screenrefresh_p = seawolf_vh_screenrefresh;
            use_tmpbitmap = 1;
        }
    };

    public static InitDriverPtr init_blueshrk = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            vh_screenrefresh_p = blueshrk_vh_screenrefresh;
            use_tmpbitmap = 1;
        }
    };

    public static InitDriverPtr init_desertgu = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            vh_screenrefresh_p = desertgu_vh_screenrefresh;
            use_tmpbitmap = 1;
        }
    };

    public static InitDriverPtr init_astinvad = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            videoram_w_p = astinvad_videoram_w;
            screen_red_enabled = 1;
        }
    };

    public static InitDriverPtr init_spaceint = new InitDriverPtr() {
        public void handler() {
            init_8080bw.handler();
            videoram_w_p = spaceint_videoram_w;
        }
    };

    public static VhStartPtr invaders_vh_start = new VhStartPtr() {
        public int handler() {
            /* create overlay if one of was specified in init_X */
            if (init_overlay != null) {
                if ((overlay = artwork_create(init_overlay, 2, Machine.drv.total_colors - 2)) == null) {
                    return 1;
                }

                use_tmpbitmap = 1;
            }

            if (use_tmpbitmap != 0 && (generic_bitmapped_vh_start.handler() != 0)) {
                return 1;
            }

            plot_pixel_p = use_tmpbitmap != 0 ? plot_pixel_8080_tmpbitmap : plot_pixel_8080;

            return 0;
        }
    };

    public static VhStopPtr invaders_vh_stop = new VhStopPtr() {
        public void handler() {
            if (overlay != null) {
                artwork_free(overlay);
                overlay = null;
            }

            if (use_tmpbitmap != 0) {
                generic_bitmapped_vh_stop.handler();
            }
        }
    };

    public static void invaders_flipscreen_w(int data) {
        if (data != color_map_select) {
            color_map_select = data;
            redraw_screen = 1;
        }

        if ((input_port_3_r.handler(0) & 0x01) != 0) {
            if (data != flipscreen) {
                flipscreen = data;
                redraw_screen = 1;
            }
        }
    }

    public static void invaders_screen_red_w(int data) {
        if (screen_red_enabled != 0 && (data != screen_red)) {
            screen_red = data;
            redraw_screen = 1;
        }
    }

    public static plot_pixel_Ptr plot_pixel_8080 = new plot_pixel_Ptr() {
        public void handler(int x, int y, int col) {
            if (flipscreen != 0) {
                x = 255 - x;
                y = 223 - y;
            }

            plot_pixel.handler(Machine.scrbitmap, x, y, Machine.pens[col]);
        }
    };

    public static plot_pixel_Ptr plot_pixel_8080_tmpbitmap = new plot_pixel_Ptr() {
        public void handler(int x, int y, int col) {
            if (flipscreen != 0) {
                x = 255 - x;
                y = 223 - y;
            }

            plot_pixel2(Machine.scrbitmap, tmpbitmap, x, y, Machine.pens[col]);
        }
    };

    public static WriteHandlerPtr invaders_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            videoram_w_p.handler(offset, data);
        }
    };

    public static WriteHandlerPtr bw_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, x, y;

            videoram.write(offset, data);

            y = offset / 32;
            x = 8 * (offset % 32);

            for (i = 0; i < 8; i++) {
                plot_pixel_p.handler(x, y, data & 0x01);

                x++;
                data >>= 1;
            }
        }
    };

    /* thr only difference between these is the background color */
    public static WriteHandlerPtr schaser_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, x, y, fore_color, back_color;

            videoram.write(offset, data);

            y = offset / 32;
            x = 8 * (offset % 32);

            back_color = 2;
            /* blue */
            fore_color = colorram.read(offset & 0x1f1f) & 0x07;

            for (i = 0; i < 8; i++) {
                if ((data & 0x01) != 0) {
                    plot_pixel_p.handler(x, y, fore_color);
                } else {
                    plot_pixel_p.handler(x, y, back_color);
                }

                x++;
                data >>= 1;
            }
        }
    };

    public static WriteHandlerPtr rollingc_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, x, y, fore_color, back_color;

            videoram.write(offset, data);

            y = offset / 32;
            x = 8 * (offset % 32);

            back_color = 0;
            /* black */
            fore_color = colorram.read(offset & 0x1f1f) & 0x07;

            for (i = 0; i < 8; i++) {
                if ((data & 0x01) != 0) {
                    plot_pixel_p.handler(x, y, fore_color);
                } else {
                    plot_pixel_p.handler(x, y, back_color);
                }

                x++;
                data >>= 1;
            }
        }
    };

    public static WriteHandlerPtr schaser_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;

            offset &= 0x1f1f;

            colorram.write(offset, data);

            /* redraw region with (possibly) changed color */
            for (i = 0; i < 8; i++, offset += 0x20) {
                videoram_w_p.handler(offset, videoram.read(offset));
            }
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
    public static VhUpdatePtr invaders_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            vh_screenrefresh_p.handler(bitmap, full_refresh);
        }
    };

    public static VhUpdatePtr vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null || redraw_screen != 0 || (full_refresh != 0 && use_tmpbitmap == 0)) {
                int offs;

                for (offs = 0; offs < videoram_size[0]; offs++) {
                    videoram_w_p.handler(offs, videoram.read(offs));
                }

                redraw_screen = 0;

                if (overlay != null) {
                    overlay_remap(overlay);
                }
            }

            if (full_refresh != 0 && use_tmpbitmap != 0) /* copy the character mapped graphics */ {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            if (overlay != null) {
                overlay_draw(bitmap, overlay);
            }
        }
    };

    static void draw_sight(osd_bitmap bitmap, int x_center, int y_center) {
        int x, y;

        if (x_center < 2) {
            x_center = 2;
        }
        if (x_center > 253) {
            x_center = 253;
        }

        if (y_center < 2) {
            y_center = 2;
        }
        if (y_center > 253) {
            y_center = 253;
        }

        for (y = y_center - 10; y < y_center + 11; y++) {
            if ((y >= 0) && (y < 256)) {
                plot_pixel.handler(bitmap, x_center, y, Machine.pens[1]);
            }
        }

        for (x = x_center - 20; x < x_center + 21; x++) {
            if ((x >= 0) && (x < 256)) {
                plot_pixel.handler(bitmap, x, y_center, Machine.pens[1]);
            }
        }
    }

    public static VhUpdatePtr seawolf_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* update the bitmap (and erase old cross) */

            vh_screenrefresh.handler(bitmap, 1);

            draw_sight(bitmap, ((input_port_0_r.handler(0) & 0x1f) * 8) + 4, 31);
        }
    };

    public static VhUpdatePtr blueshrk_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* update the bitmap (and erase old cross) */

            vh_screenrefresh.handler(bitmap, 1);

            draw_sight(bitmap, ((input_port_1_r.handler(0) & 0x7f) * 2) - 12, 31);
        }
    };

    public static VhUpdatePtr desertgu_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* update the bitmap (and erase old cross) */

            vh_screenrefresh.handler(bitmap, 1);

            draw_sight(bitmap,
                    ((input_port_1_r.handler(0) & 0x7f) * 2) - 30,
                    ((input_port_2_r.handler(0) & 0x7f) * 2) - 30);
        }
    };

    public static VhConvertColorPromPtr invadpt2_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                /* this bit arrangment is a little unusual but are confirmed by screen shots */

                palette[p_inc++] = (char) (0xff * ((i >> 0) & 1));
                palette[p_inc++] = (char) (0xff * ((i >> 2) & 1));
                palette[p_inc++] = (char) (0xff * ((i >> 1) & 1));
            }
        }
    };

    public static WriteHandlerPtr invadpt2_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, x, y;
            int col;

            videoram.write(offset, data);

            y = offset / 32;
            x = 8 * (offset % 32);

            /* 32 x 32 colormap */
            if (screen_red == 0) {
                col = memory_region(REGION_PROMS).read((color_map_select != 0 ? 0x400 : 0) + (((y + 32) / 8) * 32) + (x / 8)) & 7;
            } else {
                col = 1;
                /* red */
            }

            for (i = 0; i < 8; i++) {
                plot_pixel_p.handler(x, y, (data & 0x01) != 0 ? col : 0);

                x++;
                data >>= 1;
            }
        }
    };

    public static WriteHandlerPtr astinvad_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i, x, y;
            int col;

            videoram.write(offset, data);

            y = offset / 32;
            x = 8 * (offset % 32);

            if (screen_red == 0) {
                if (flipscreen != 0) {
                    col = memory_region(REGION_PROMS).read(((y + 32) / 8) * 32 + (x / 8)) >> 4;
                } else {
                    col = memory_region(REGION_PROMS).read((31 - y / 8) * 32 + (31 - x / 8)) & 0x0f;
                }
            } else {
                col = 1;
                /* red */
            }

            for (i = 0; i < 8; i++) {
                plot_pixel_p.handler(x, y, (data & 0x01) != 0 ? col : 0);

                x++;
                data >>= 1;
            }
        }
    };

    public static WriteHandlerPtr spaceint_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;
            int/*UINT8*/ x, y;

            videoram.write(offset, data);

            y = (8 * (offset / 256)) & 0xFF;
            x = (offset % 256) & 0xFF;

            for (i = 0; i < 8; i++) {
                int col = 0;

                if ((data & 0x01) != 0) {
                    /* this is wrong */
                    col = memory_region(REGION_PROMS).read((y / 16) + 16 * ((x + 16) / 32));
                }

                plot_pixel_p.handler(x, y, col);

                y = (y + 1) & 0xFF;//y++;
                data >>= 1;
            }
        }
    };
}
