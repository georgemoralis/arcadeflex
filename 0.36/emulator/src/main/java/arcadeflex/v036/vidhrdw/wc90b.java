/*
 * ported to v0.36
 *
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
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.mame.*;
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

public class wc90b {

    public static UBytePtr wc90b_shared = new UBytePtr();

    public static UBytePtr wc90b_tile_colorram = new UBytePtr();
    public static UBytePtr wc90b_tile_videoram = new UBytePtr();
    public static UBytePtr wc90b_tile_colorram2 = new UBytePtr();
    public static UBytePtr wc90b_tile_videoram2 = new UBytePtr();
    public static UBytePtr wc90b_scroll1xlo = new UBytePtr();
    public static UBytePtr wc90b_scroll1xhi = new UBytePtr();
    public static UBytePtr wc90b_scroll2xlo = new UBytePtr();
    public static UBytePtr wc90b_scroll2xhi = new UBytePtr();
    public static UBytePtr wc90b_scroll1ylo = new UBytePtr();
    public static UBytePtr wc90b_scroll1yhi = new UBytePtr();
    public static UBytePtr wc90b_scroll2ylo = new UBytePtr();
    public static UBytePtr wc90b_scroll2yhi = new UBytePtr();

    public static int[] wc90b_tile_videoram_size = new int[1];
    public static int[] wc90b_tile_videoram_size2 = new int[1];

    static char[] dirtybuffer1;
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap1;
    static osd_bitmap tmpbitmap2;

    public static VhStartHandlerPtr wc90b_vh_start = new VhStartHandlerPtr() {
        public int handler() {

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer1 = new char[wc90b_tile_videoram_size[0]]) == null) {
                return 1;
            }

            memset(dirtybuffer1, 1, wc90b_tile_videoram_size[0]);

            if ((tmpbitmap1 = osd_new_bitmap(4 * Machine.drv.screen_width, 2 * Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer1 = null;
                generic_vh_stop.handler();
                return 1;
            }

            if ((dirtybuffer2 = new char[wc90b_tile_videoram_size2[0]]) == null) {
                osd_free_bitmap(tmpbitmap1);
                dirtybuffer1 = null;
                generic_vh_stop.handler();
                return 1;
            }

            memset(dirtybuffer2, 1, wc90b_tile_videoram_size2[0]);

            if ((tmpbitmap2 = osd_new_bitmap(4 * Machine.drv.screen_width, 2 * Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                osd_free_bitmap(tmpbitmap1);
                dirtybuffer1 = null;
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            // Free the generic bitmap and allocate one twice as wide
            tmpbitmap = null;

            if ((tmpbitmap = osd_new_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                osd_free_bitmap(tmpbitmap1);
                osd_free_bitmap(tmpbitmap2);
                dirtybuffer = null;
                dirtybuffer1 = null;
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr wc90b_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            dirtybuffer1 = null;
            dirtybuffer2 = null;
            osd_free_bitmap(tmpbitmap1);
            osd_free_bitmap(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static ReadHandlerPtr wc90b_tile_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90b_tile_videoram.read(offset);
        }
    };

    public static WriteHandlerPtr wc90b_tile_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (wc90b_tile_videoram.read(offset) != data) {
                dirtybuffer1[offset] = 1;
                wc90b_tile_videoram.write(offset, data);
            }
        }
    };

    public static ReadHandlerPtr wc90b_tile_colorram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90b_tile_colorram.read(offset);
        }
    };

    public static WriteHandlerPtr wc90b_tile_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (wc90b_tile_colorram.read(offset) != data) {
                dirtybuffer1[offset] = 1;
                wc90b_tile_colorram.write(offset, data);
            }
        }
    };

    public static ReadHandlerPtr wc90b_tile_videoram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90b_tile_videoram2.read(offset);
        }
    };

    public static WriteHandlerPtr wc90b_tile_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (wc90b_tile_videoram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;
                wc90b_tile_videoram2.write(offset, data);
            }
        }
    };

    public static ReadHandlerPtr wc90b_tile_colorram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90b_tile_colorram2.read(offset);
        }
    };

    public static WriteHandlerPtr wc90b_tile_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (wc90b_tile_colorram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;
                wc90b_tile_colorram2.write(offset, data);
            }
        }
    };

    public static ReadHandlerPtr wc90b_shared_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90b_shared.read(offset);
        }
    };

    public static WriteHandlerPtr wc90b_shared_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            wc90b_shared.write(offset, data);
        }
    };

    static void wc90b_draw_sprites(osd_bitmap bitmap, int priority) {
        int offs;

        /* draw all visible sprites of specified priority */
        for (offs = spriteram_size[0] - 8; offs >= 0; offs -= 8) {

            if ((~(spriteram.read(offs + 3) >> 6) & 3) == priority) {

                if (spriteram.read(offs + 1) > 16) {
                    /* visible */
                    int code = (spriteram.read(offs + 3) & 0x3f) << 4;
                    int bank = spriteram.read(offs + 0);
                    int flags = spriteram.read(offs + 4);

                    code += (bank & 0xf0) >> 4;
                    code <<= 2;
                    code += (bank & 0x0f) >> 2;

                    drawgfx(bitmap, Machine.gfx[17], code,
                            flags >> 4, /* color */
                            bank & 1, /* flipx */
                            bank & 2, /* flipy */
                            spriteram.read(offs + 2), /* sx */
                            240 - spriteram.read(offs + 1), /* sy */
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }
        }
    }

    public static VhUpdateHandlerPtr wc90b_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, i;
            int scrollx, scrolly;

            /* compute palette usage */
            {
                char[] palette_map = new char[4 * 16];
                int tile, gfx, cram;

                memset(palette_map, 0, 4 * 16);

                for (offs = wc90b_tile_videoram_size2[0] - 1; offs >= 0; offs--) {
                    tile = wc90b_tile_videoram2.read(offs);
                    cram = wc90b_tile_colorram2.read(offs);
                    gfx = 9 + (cram & 3) + ((cram >> 1) & 4);
                    palette_map[3 * 16 + (cram >> 4)] |= Machine.gfx[gfx].pen_usage[tile];
                }
                for (offs = wc90b_tile_videoram_size[0] - 1; offs >= 0; offs--) {
                    tile = wc90b_tile_videoram.read(offs);
                    cram = wc90b_tile_colorram.read(offs);
                    gfx = 1 + (cram & 3) + ((cram >> 1) & 4);
                    palette_map[2 * 16 + (cram >> 4)] |= Machine.gfx[gfx].pen_usage[tile];
                }
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    cram = colorram.read(offs);
                    tile = videoram.read(offs) + ((cram & 0x07) << 8);
                    palette_map[1 * 16 + (cram >> 4)] |= Machine.gfx[0].pen_usage[tile];
                }
                for (offs = spriteram_size[0] - 8; offs >= 0; offs -= 8) {
                    if (spriteram.read(offs + 1) > 16) {
                        /* visible */
                        int flags = spriteram.read(offs + 4);
                        palette_map[0 * 16 + (flags >> 4)] |= 0xfffe;
                    }
                }

                /* expand the results */
                for (i = 0; i < 1 * 16; i++) {
                    int usage = palette_map[i], j;
                    if (usage != 0) {
                        palette_used_colors.write(i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
                        for (j = 1; j < 16; j++) {
                            if ((usage & (1 << j)) != 0) {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
                            } else {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
                            }
                        }
                    } else {
                        memset(palette_used_colors, i * 16 + 0, PALETTE_COLOR_UNUSED, 16);
                    }
                }
                for (i = 1 * 16; i < 4 * 16; i++) {
                    int usage = palette_map[i], j;
                    if (usage != 0) {
                        for (j = 0; j < 15; j++) {
                            if ((usage & (1 << j)) != 0) {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
                            } else {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
                            }
                        }
                        palette_used_colors.write(i * 16 + 15, PALETTE_COLOR_TRANSPARENT);
                    } else {
                        memset(palette_used_colors, i * 16 + 0, PALETTE_COLOR_UNUSED, 16);
                    }
                }

                if (palette_recalc() != null) {
                    memset(dirtybuffer, 1, videoram_size[0]);
                    memset(dirtybuffer1, 1, wc90b_tile_videoram_size[0]);
                    memset(dirtybuffer2, 1, wc90b_tile_videoram_size2[0]);
                }
            }

            /* commented out -- if we copyscrollbitmap below with TRANSPARENCY_NONE, we shouldn't waste our
	   time here:
		wc90b_draw_sprites( bitmap, 3 );
             */
            for (offs = wc90b_tile_videoram_size2[0] - 1; offs >= 0; offs--) {
                int sx, sy, tile, gfx;

                if (dirtybuffer2[offs] != 0) {

                    dirtybuffer2[offs] = 0;

                    sx = (offs % 64);
                    sy = (offs / 64);

                    tile = wc90b_tile_videoram2.read(offs);
                    gfx = 9 + (wc90b_tile_colorram2.read(offs) & 3) + ((wc90b_tile_colorram2.read(offs) >> 1) & 4);

                    drawgfx(tmpbitmap2, Machine.gfx[gfx],
                            tile,
                            wc90b_tile_colorram2.read(offs) >> 4,
                            0, 0,
                            sx * 16, sy * 16,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            scrollx = -wc90b_scroll2xlo.read(0) - 256 * (wc90b_scroll2xhi.read(0) & 3);
            scrolly = -wc90b_scroll2ylo.read(0) - 256 * (wc90b_scroll2yhi.read(0) & 1);

            copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            wc90b_draw_sprites(bitmap, 2);

            for (offs = wc90b_tile_videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy, tile, gfx;

                if (dirtybuffer1[offs] != 0) {

                    dirtybuffer1[offs] = 0;

                    sx = (offs % 64);
                    sy = (offs / 64);

                    tile = wc90b_tile_videoram.read(offs);
                    gfx = 1 + (wc90b_tile_colorram.read(offs) & 3) + ((wc90b_tile_colorram.read(offs) >> 1) & 4);

                    drawgfx(tmpbitmap1, Machine.gfx[gfx],
                            tile,
                            wc90b_tile_colorram.read(offs) >> 4,
                            0, 0,
                            sx * 16, sy * 16,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            scrollx = -wc90b_scroll1xlo.read(0) - 256 * (wc90b_scroll1xhi.read(0) & 3);
            scrolly = -wc90b_scroll1ylo.read(0) - 256 * (wc90b_scroll1yhi.read(0) & 1);

            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            wc90b_draw_sprites(bitmap, 1);

            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = (offs % 64);
                    sy = (offs / 64);

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x07) << 8),
                            colorram.read(offs) >> 4,
                            0, 0,
                            sx * 8, sy * 8,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            wc90b_draw_sprites(bitmap, 0);
        }
    };
}
