/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 24/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
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

public class tecmo {

    public static UBytePtr tecmo_videoram = new UBytePtr();
    public static UBytePtr tecmo_colorram = new UBytePtr();
    public static UBytePtr tecmo_videoram2 = new UBytePtr();
    public static UBytePtr tecmo_colorram2 = new UBytePtr();
    public static UBytePtr tecmo_scroll = new UBytePtr();
    public static int[] tecmo_videoram2_size = new int[1];

    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;
    static osd_bitmap tmpbitmap3;

    static int video_type = 0;
    /*
     video_type is used to distinguish Rygar, Silkworm and Gemini Wing.
     This is needed because there is a difference in the tile and sprite indexing.
     */

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr tecmo_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            /* the background area is twice as wide as the screen */
            if ((tmpbitmap2 = osd_new_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }
            if ((tmpbitmap3 = osd_new_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                osd_free_bitmap(tmpbitmap2);
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            /* 0x100 is the background color */
            palette_transparent_color = 0x100;

            return 0;
        }
    };

    public static VhStartHandlerPtr rygar_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            video_type = 0;
            return tecmo_vh_start.handler();
        }
    };

    public static VhStartHandlerPtr silkworm_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            video_type = 1;
            return tecmo_vh_start.handler();
        }
    };

    public static VhStartHandlerPtr gemini_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            video_type = 2;
            return tecmo_vh_start.handler();
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopHandlerPtr tecmo_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap3);
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr tecmo_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (tecmo_videoram.read(offset) != data) {
                dirtybuffer2[offset] = 1;
                tecmo_videoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr tecmo_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (tecmo_colorram.read(offset) != data) {
                dirtybuffer2[offset] = 1;
                tecmo_colorram.write(offset, data);
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
    public static void tecmo_draw_sprites(osd_bitmap bitmap, int priority) {
        int offs;

        /* draw all visible sprites of specified priority */
        for (offs = 0; offs < spriteram_size[0]; offs += 8) {
            int flags = spriteram.read(offs + 3);

            if ((flags >> 6) == priority) {
                int bank = spriteram.read(offs + 0);
                if ((bank & 4) != 0) {
                    /* visible */

                    int which = spriteram.read(offs + 1);
                    int code;
                    int size = (spriteram.read(offs + 2) & 3);
                    /* 0 = 8x8 1 = 16x16 2 = 32x32 3 = 64x64 */
                    if (size == 3) {
                        continue;
                        /* not used by these games */
                    }

                    if (video_type != 0) {
                        code = (which) + ((bank & 0xf8) << 5);
                        /* silkworm */
                    } else {
                        code = (which) + ((bank & 0xf0) << 4);
                        /* rygar */
                    }

                    if (size == 1) {
                        code >>= 2;
                    } else if (size == 2) {
                        code >>= 4;
                    }

                    drawgfx(bitmap, Machine.gfx[size + 1],
                            code,
                            flags & 0xf, /* color */
                            bank & 1, /* flipx */
                            bank & 2, /* flipy */
                            spriteram.read(offs + 5) - ((flags & 0x10) << 4), /* sx */
                            spriteram.read(offs + 4) - ((flags & 0x20) << 3), /* sy */
                            Machine.drv.visible_area,
                            priority == 3 ? TRANSPARENCY_THROUGH : TRANSPARENCY_PEN,
                            priority == 3 ? palette_transparent_pen : 0);
                }
            }
        }
    }

    public static VhUpdateHandlerPtr tecmo_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            palette_init_used_colors();

            {
                int color, code, i;
                int[] colmask = new int[16];
                int pal_base;

                pal_base = Machine.drv.gfxdecodeinfo[5].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    if (video_type == 2) /* Gemini Wing */ {
                        code = videoram.read(offs) + 16 * (colorram.read(offs) & 0x70);
                        color = colorram.read(offs) & 0x0f;
                    } else {
                        code = videoram.read(offs) + 256 * (colorram.read(offs) & 0x07);
                        color = colorram.read(offs) >> 4;
                    }

                    colmask[color] |= Machine.gfx[5].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[4].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    if (video_type == 2) /* Gemini Wing */ {
                        code = tecmo_videoram.read(offs) + 16 * (tecmo_colorram.read(offs) & 0x70);
                        color = tecmo_colorram.read(offs) & 0x0f;
                    } else {
                        code = tecmo_videoram.read(offs) + 256 * (tecmo_colorram.read(offs) & 0x07);
                        color = tecmo_colorram.read(offs) >> 4;
                    }

                    colmask[color] |= Machine.gfx[4].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = 0; offs < spriteram_size[0]; offs += 8) {
                    int flags = spriteram.read(offs + 3);
                    int bank = spriteram.read(offs + 0);
                    if ((bank & 4) != 0) {
                        /* visible */

                        int which = spriteram.read(offs + 1);
                        int size = (spriteram.read(offs + 2) & 3);
                        /* 0 = 8x8 1 = 16x16 2 = 32x32 3 = 64x64 */
                        if (size == 3) {
                            continue;
                            /* not used by these games */
                        }

                        if (video_type != 0) {
                            code = (which) + ((bank & 0xf8) << 5);
                            /* silkworm */
                        } else {
                            code = (which) + ((bank & 0xf0) << 4);
                            /* rygar */
                        }

                        if (size == 1) {
                            code >>= 2;
                        } else if (size == 2) {
                            code >>= 4;
                        }

                        color = flags & 0xf;

                        colmask[color] |= Machine.gfx[size + 1].pen_usage[code];
                    }
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = tecmo_videoram2_size[0] - 1; offs >= 0; offs--) {
                    code = tecmo_videoram2.read(offs) + ((tecmo_colorram2.read(offs) & 0x03) << 8);
                    color = tecmo_colorram2.read(offs) >> 4;
                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }
            }

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
            }

            /* draw the background. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int code, color, sx, sy;

                    if (video_type == 2) /* Gemini Wing */ {
                        code = videoram.read(offs) + 16 * (colorram.read(offs) & 0x70);
                        color = colorram.read(offs) & 0x0f;
                    } else {
                        code = videoram.read(offs) + 256 * (colorram.read(offs) & 0x07);
                        color = colorram.read(offs) >> 4;
                    }
                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap2, Machine.gfx[5],
                            code,
                            color,
                            0, 0,
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);

                    dirtybuffer[offs] = 0;
                }

                if (dirtybuffer2[offs] != 0) {
                    int code, color, sx, sy;

                    if (video_type == 2) /* Gemini Wing */ {
                        code = tecmo_videoram.read(offs) + 16 * (tecmo_colorram.read(offs) & 0x70);
                        color = tecmo_colorram.read(offs) & 0x0f;
                    } else {
                        code = tecmo_videoram.read(offs) + 256 * (tecmo_colorram.read(offs) & 0x07);
                        color = tecmo_colorram.read(offs) >> 4;
                    }
                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap3, Machine.gfx[4],
                            code,
                            color,
                            0, 0,
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
                dirtybuffer2[offs] = 0;
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                /* draw background tiles */
                scrollx = -tecmo_scroll.read(3) - 256 * (tecmo_scroll.read(4) & 1) - 48;
                scrolly = -tecmo_scroll.read(5);

                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly},
                        Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                /* sprites will be drawn with TRANSPARENCY_THROUGH and appear behind the background */
                tecmo_draw_sprites(bitmap, 3);
                /* this should never draw anything, but just in case... */

                tecmo_draw_sprites(bitmap, 2);

                /* draw foreground tiles */
                scrollx = -tecmo_scroll.read(0) - 256 * (tecmo_scroll.read(1) & 1) - 48;
                scrolly = -tecmo_scroll.read(2);
                copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly},
                        Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
            }

            tecmo_draw_sprites(bitmap, 1);

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = tecmo_videoram2_size[0] - 1; offs >= 0; offs--) {
                int sx = offs % 32;
                int sy = offs / 32;

                drawgfx(bitmap, Machine.gfx[0],
                        tecmo_videoram2.read(offs) + ((tecmo_colorram2.read(offs) & 0x03) << 8),
                        tecmo_colorram2.read(offs) >> 4,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            tecmo_draw_sprites(bitmap, 0);
        }
    };
}
