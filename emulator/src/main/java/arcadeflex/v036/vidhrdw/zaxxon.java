/*
 * ported to v0.36
 * using automatic conversion tool v0.05 + manual work
 */
package arcadeflex.v036.vidhrdw;

//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class zaxxon {

    public static UBytePtr zaxxon_background_position = new UBytePtr();
    public static UBytePtr zaxxon_background_color_bank = new UBytePtr();
    public static UBytePtr zaxxon_background_enable = new UBytePtr();
    public static UBytePtr zaxxon_char_color_bank = new UBytePtr();
    public static UBytePtr color_codes = new UBytePtr();
    static osd_bitmap backgroundbitmap1, backgroundbitmap2;

    public static int zaxxon_vid_type;
    /* set by init_machine; 0 = zaxxon; 1 = congobongo */

    public static final int ZAXXON_VID = 0;
    public static final int CONGO_VID = 1;
    public static final int FUTSPY_VID = 2;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr zaxxon_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the character color codes */
            color_codes = new UBytePtr(color_prom);//color_codes = color_prom;
            color_codes.offset = color_prom.offset;

            /* all gfx elements use the same palette */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;
            }
            //COLOR(0,i) = i;
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    static void copy_pixel(osd_bitmap dst_bm, int dx, int dy, osd_bitmap src_bm, int sx, int sy) {
        plot_pixel.handler(dst_bm, dx, dy, read_pixel.handler(src_bm, sx, sy));
    }

    static void create_background(osd_bitmap dst_bm, osd_bitmap src_bm, int col) {
        int offs;
        int sx, sy;

        for (offs = 0; offs < 0x4000; offs++) {
            sy = 8 * (offs / 32);
            sx = 8 * (offs % 32);

            if ((Machine.orientation & ORIENTATION_SWAP_XY) == 0) /* leave screenful of black pixels at end */ {
                sy += 256;
            }

            drawgfx(src_bm, Machine.gfx[1],
                    memory_region(REGION_GFX4).read(offs) + 256 * (memory_region(REGION_GFX4).read(0x4000 + offs) & 3),
                    col + (memory_region(REGION_GFX4).read(0x4000 + offs) >> 4),
                    0, 0,
                    sx, sy,
                    null, TRANSPARENCY_NONE, 0);
        }

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            /* the background is stored as a rectangle, but is drawn by the hardware skewed: */
 /* go right two pixels, then up one pixel. Doing the conversion at run time would */
 /* be extremely expensive, so we do it now. To save memory, we squash the image */
 /* horizontally (doing line shifts at run time is much less expensive than doing */
 /* column shifts) */
            for (offs = -510; offs < 4096; offs += 2) {
                sx = (2302 - 510 / 2) - offs / 2;

                for (sy = 0; sy < 512; sy += 2) {
                    if (offs + sy >= 0 && offs + sy < 4096) {
                        copy_pixel(dst_bm, sx, 511 - sy, src_bm, sy / 2, 4095 - (offs + sy));
                        copy_pixel(dst_bm, sx, 511 - (sy + 1), src_bm, sy / 2, 4095 - (offs + sy + 1));
                    }
                }
            }
        }
    }

    public static VhStartPtr zaxxon_vh_start = new VhStartPtr() {
        public int handler() {
            osd_bitmap prebitmap;
            int width, height;

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            /* for speed, backgrounds are arranged differently if axis is swapped */
            if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                height = 512;
                width = 2303 + 32;
            } else {
                /* leave a screenful of black pixels at each end */
                height = 256 + 4096 + 256;
                width = 256;
            }

            /* large bitmap for the precalculated background */
            if ((backgroundbitmap1 = osd_create_bitmap(width, height)) == null) {
                zaxxon_vh_stop.handler();
                return 1;
            }

            if (zaxxon_vid_type == ZAXXON_VID || zaxxon_vid_type == FUTSPY_VID) {
                if ((backgroundbitmap2 = osd_create_bitmap(width, height)) == null) {
                    zaxxon_vh_stop.handler();
                    return 1;
                }
            }

            if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                /* create a temporary bitmap to prepare the background before converting it */
                if ((prebitmap = osd_create_bitmap(256, 4096)) == null) {
                    zaxxon_vh_stop.handler();
                    return 1;
                }
            } else {
                prebitmap = backgroundbitmap1;
            }

            /* prepare the background */
            create_background(backgroundbitmap1, prebitmap, 0);

            if (zaxxon_vid_type == ZAXXON_VID || zaxxon_vid_type == FUTSPY_VID) {
                if ((Machine.orientation & ORIENTATION_SWAP_XY) == 0) {
                    prebitmap = backgroundbitmap2;
                }

                /* prepare a second background with different colors, used in the death sequence */
                create_background(backgroundbitmap2, prebitmap, 16);
            }

            if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                osd_free_bitmap(prebitmap);
            }

            return 0;
        }
    };

    public static VhStartPtr razmataz_vh_start = new VhStartPtr() {
        public int handler() {
            int offs;

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            /* large bitmap for the precalculated background */
            if ((backgroundbitmap1 = osd_create_bitmap(256, 4096)) == null) {
                zaxxon_vh_stop.handler();
                return 1;
            }

            if ((backgroundbitmap2 = osd_create_bitmap(256, 4096)) == null) {
                zaxxon_vh_stop.handler();
                return 1;
            }

            /* prepare the background */
            for (offs = 0; offs < 0x4000; offs++) {
                int sx, sy;

                sy = 8 * (offs / 32);
                sx = 8 * (offs % 32);

                drawgfx(backgroundbitmap1, Machine.gfx[1],
                        memory_region(REGION_GFX4).read(offs) + 256 * (memory_region(REGION_GFX4).read(0x4000 + offs) & 3),
                        memory_region(REGION_GFX4).read(0x4000 + offs) >> 4,
                        0, 0,
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);

                drawgfx(backgroundbitmap2, Machine.gfx[1],
                        memory_region(REGION_GFX4).read(offs) + 256 * (memory_region(REGION_GFX4).read(0x4000 + offs) & 3),
                        16 + (memory_region(REGION_GFX4).read(0x4000 + offs) >> 4),
                        0, 0,
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);
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
    public static VhStopPtr zaxxon_vh_stop = new VhStopPtr() {
        public void handler() {
            if (backgroundbitmap1 != null) {
                osd_free_bitmap(backgroundbitmap1);
            }
            if (backgroundbitmap2 != null) {
                osd_free_bitmap(backgroundbitmap2);
            }
            generic_vh_stop.handler();
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
    static int sprpri[] = new int[0x100];

    /* this really should not be more
			                             * than 0x1e, but I did not want to check
			                             * for 0xff which is set when sprite is off
			                             * -V-
     */
    static void draw_sprites(osd_bitmap bitmap) {
        int offs;

        if (zaxxon_vid_type == CONGO_VID) {
            int i;

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
 /* Sprites actually start at 0xff * [0xc031], it seems to be static tho'*/
 /* The number of active sprites is stored at 0xc032 */
            for (offs = 0x1e * 0x20; offs >= 0x00; offs -= 0x20) {
                sprpri[spriteram.read(offs + 1)] = offs;
            }

            for (i = 0x1e; i >= 0; i--) {
                offs = sprpri[i];

                if (spriteram.read(offs + 2) != 0xff) {
                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs + 2 + 1) & 0x7f,
                            spriteram.read(offs + 2 + 2),
                            spriteram.read(offs + 2 + 2) & 0x80, spriteram.read(offs + 2 + 1) & 0x80,
                            ((spriteram.read(offs + 2 + 3) + 16) & 0xff) - 31, 255 - spriteram.read(offs + 2) - 15,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        } else if (zaxxon_vid_type == FUTSPY_VID) {
            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                if (spriteram.read(offs) != 0xff) {
                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs + 1) & 0x7f,
                            spriteram.read(offs + 2) & 0x3f,
                            spriteram.read(offs + 1) & 0x80, spriteram.read(offs + 1) & 0x80, /* ?? */
                            ((spriteram.read(offs + 3) + 16) & 0xff) - 32, 255 - spriteram.read(offs) - 16,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        } else {
            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                if (spriteram.read(offs) != 0xff) {
                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs + 1) & 0x3f,
                            spriteram.read(offs + 2) & 0x3f,
                            spriteram.read(offs + 1) & 0x40, spriteram.read(offs + 1) & 0x80,
                            ((spriteram.read(offs + 3) + 16) & 0xff) - 32, 255 - spriteram.read(offs) - 16,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    }

    public static VhUpdatePtr zaxxon_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* copy the background */
 /* TODO: there's a bug here which shows only in test mode. The background doesn't */
 /* cover the whole screen, so the image is not fully overwritten and part of the */
 /* character color test screen remains on screen when it is replaced by the background */
 /* color test. */
            if (zaxxon_background_enable.read() != 0) {
                int i, skew, scroll;
                rectangle clip = new rectangle();

                if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                    /* standard rotation - skew background horizontally */
                    if (zaxxon_vid_type == CONGO_VID) {
                        scroll = 1023 + 63 - (zaxxon_background_position.read(0) + 256 * zaxxon_background_position.read(1));
                    } else {
                        scroll = 2048 + 63 - (zaxxon_background_position.read(0) + 256 * (zaxxon_background_position.read(1) & 7));
                    }

                    skew = 128 - 512 + 2 * Machine.drv.visible_area.min_x;

                    clip.min_y = Machine.drv.visible_area.min_y;
                    clip.max_y = Machine.drv.visible_area.max_y;

                    for (i = Machine.drv.visible_area.min_x; i <= Machine.drv.visible_area.max_x; i++) {
                        clip.min_x = i;
                        clip.max_x = i;

                        if ((zaxxon_vid_type == ZAXXON_VID || zaxxon_vid_type == FUTSPY_VID)
                                && ((zaxxon_background_color_bank.read() & 1) != 0)) {
                            copybitmap(bitmap, backgroundbitmap2, 0, 0, -scroll, skew, clip, TRANSPARENCY_NONE, 0);
                        } else {
                            copybitmap(bitmap, backgroundbitmap1, 0, 0, -scroll, skew, clip, TRANSPARENCY_NONE, 0);
                        }

                        skew += 2;
                    }
                } else {
                    /* skew background up one pixel every 2 horizontal pixels */
                    if (zaxxon_vid_type == CONGO_VID) {
                        scroll = 2050 + 2 * (zaxxon_background_position.read(0) + 256 * zaxxon_background_position.read(1))
                                - backgroundbitmap1.height + 256;
                    } else {
                        scroll = 2 * (zaxxon_background_position.read(0) + 256 * (zaxxon_background_position.read(1) & 7))
                                - backgroundbitmap1.height + 256;
                    }

                    skew = 72 - (255 - Machine.drv.visible_area.max_y);

                    clip.min_x = Machine.drv.visible_area.min_x;
                    clip.max_x = Machine.drv.visible_area.max_x;

                    for (i = Machine.drv.visible_area.max_y; i >= Machine.drv.visible_area.min_y; i -= 2) {
                        clip.min_y = i - 1;
                        clip.max_y = i;

                        if ((zaxxon_vid_type == ZAXXON_VID || zaxxon_vid_type == FUTSPY_VID)
                                && ((zaxxon_background_color_bank.read() & 1) != 0)) {
                            copybitmap(bitmap, backgroundbitmap2, 0, 0, skew, scroll, clip, TRANSPARENCY_NONE, 0);
                        } else {
                            copybitmap(bitmap, backgroundbitmap1, 0, 0, skew, scroll, clip, TRANSPARENCY_NONE, 0);
                        }

                        skew--;
                    }
                }
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            }

            draw_sprites(bitmap);

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;
                int color;

                sy = offs / 32;
                sx = offs % 32;

                if (zaxxon_vid_type == CONGO_VID) {
                    color = colorram.read(offs);
                } else /* not sure about the color code calculation - char_color_bank is used only in test mode */ {
                    color = (color_codes.read(sx + 32 * (sy / 4)) & 0x0f) + 16 * (zaxxon_char_color_bank.read(0) & 1);
                }

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs),
                        color,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    public static VhUpdatePtr razmataz_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* copy the background */
            if (zaxxon_background_enable.read() != 0) {
                int scroll;

                scroll = 2 * (zaxxon_background_position.read(0) + 256 * (zaxxon_background_position.read(1) & 7));

                if ((zaxxon_background_color_bank.read() & 1) != 0) {
                    copyscrollbitmap(bitmap, backgroundbitmap2, 0, null, 1, new int[]{scroll}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                } else {
                    copyscrollbitmap(bitmap, backgroundbitmap1, 0, null, 1, new int[]{scroll}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            }

            draw_sprites(bitmap);

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;
                int code, color;

                sx = offs % 32;
                sy = offs / 32;

                code = videoram.read(offs);
                color = (color_codes.read(code) & 0x0f) + 16 * (zaxxon_char_color_bank.read() & 1);

                drawgfx(bitmap, Machine.gfx[0],
                        code,
                        color,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
