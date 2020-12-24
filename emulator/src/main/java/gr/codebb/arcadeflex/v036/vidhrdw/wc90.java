/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import java.util.ArrayList;
import java.util.Arrays;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class wc90 {

    public static UBytePtr wc90_shared = new UBytePtr();
    public static UBytePtr wc90_tile_colorram = new UBytePtr();
    public static UBytePtr wc90_tile_videoram = new UBytePtr();
    public static UBytePtr wc90_tile_colorram2 = new UBytePtr();
    public static UBytePtr wc90_tile_videoram2 = new UBytePtr();
    public static UBytePtr wc90_scroll0xlo = new UBytePtr();
    public static UBytePtr wc90_scroll0xhi = new UBytePtr();
    public static UBytePtr wc90_scroll1xlo = new UBytePtr();
    public static UBytePtr wc90_scroll1xhi = new UBytePtr();
    public static UBytePtr wc90_scroll2xlo = new UBytePtr();
    public static UBytePtr wc90_scroll2xhi = new UBytePtr();
    public static UBytePtr wc90_scroll0ylo = new UBytePtr();
    public static UBytePtr wc90_scroll0yhi = new UBytePtr();
    public static UBytePtr wc90_scroll1ylo = new UBytePtr();
    public static UBytePtr wc90_scroll1yhi = new UBytePtr();
    public static UBytePtr wc90_scroll2ylo = new UBytePtr();
    public static UBytePtr wc90_scroll2yhi = new UBytePtr();

    public static int[] wc90_tile_videoram_size = new int[1];
    public static int[] wc90_tile_videoram_size2 = new int[1];

    static int[] last_tile1 = {-1};
    static int[] last_tile2 = {-1};

    static char[] dirtybuffer1 = null;
    static char[] dirtybuffer2 = null;
    static osd_bitmap tmpbitmap1 = null;
    static osd_bitmap tmpbitmap2 = null;

    public static VhStartPtr wc90_vh_start = new VhStartPtr() {
        public int handler() {

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer1 = new char[wc90_tile_videoram_size[0]]) == null) {
                return 1;
            }

            memset(dirtybuffer1, 1, wc90_tile_videoram_size[0]);

            if ((tmpbitmap1 = osd_new_bitmap(4 * Machine.drv.screen_width, 2 * Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer1 = null;
                generic_vh_stop.handler();
                return 1;
            }

            if ((dirtybuffer2 = new char[wc90_tile_videoram_size2[0]]) == null) {
                osd_free_bitmap(tmpbitmap1);
                dirtybuffer1 = null;
                generic_vh_stop.handler();
                return 1;
            }

            memset(dirtybuffer2, 1, wc90_tile_videoram_size2[0]);

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

            last_tile1[0] = wc90_tile_videoram_size[0];
            last_tile2[0] = wc90_tile_videoram_size2[0];

            return 0;
        }
    };

    public static VhStopPtr wc90_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer1 = null;
            dirtybuffer2 = null;
            osd_free_bitmap(tmpbitmap1);
            osd_free_bitmap(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static ReadHandlerPtr wc90_tile_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90_tile_videoram.read(offset);
        }
    };

    public static WriteHandlerPtr wc90_tile_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int v) {
            if (wc90_tile_videoram.read(offset) != v) {
                dirtybuffer1[offset] = 1;
                wc90_tile_videoram.write(offset, v);
                if (offset > last_tile1[0]) {
                    last_tile1[0] = offset;
                }
            }
        }
    };

    public static ReadHandlerPtr wc90_tile_colorram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90_tile_colorram.read(offset);
        }
    };

    public static WriteHandlerPtr wc90_tile_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int v) {
            if (wc90_tile_colorram.read(offset) != v) {
                dirtybuffer1[offset] = 1;
                wc90_tile_colorram.write(offset, v);
                if (offset > last_tile1[0]) {
                    last_tile1[0] = offset;
                }
            }
        }
    };

    public static ReadHandlerPtr wc90_tile_videoram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90_tile_videoram2.read(offset);
        }
    };

    public static WriteHandlerPtr wc90_tile_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int v) {
            if (wc90_tile_videoram2.read(offset) != v) {
                dirtybuffer2[offset] = 1;
                wc90_tile_videoram2.write(offset, v);
                if (offset > last_tile2[0]) {
                    last_tile2[0] = offset;
                }
            }
        }
    };

    public static ReadHandlerPtr wc90_tile_colorram2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90_tile_colorram2.read(offset);
        }
    };

    public static WriteHandlerPtr wc90_tile_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int v) {
            if (wc90_tile_colorram2.read(offset) != v) {
                dirtybuffer2[offset] = 1;
                wc90_tile_colorram2.write(offset, v);
                if (offset > last_tile2[0]) {
                    last_tile2[0] = offset;
                }
            }
        }
    };

    public static ReadHandlerPtr wc90_shared_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return wc90_shared.read(offset);
        }
    };

    public static WriteHandlerPtr wc90_shared_w = new WriteHandlerPtr() {
        public void handler(int offset, int v) {
            wc90_shared.write(offset, v);
        }
    };

    public static VhUpdatePtr wc90_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, i;
            int scrollx, scrolly;

            /* compute palette usage */
            {
                char[] palette_map = new char[4 * 16];
                int tile, cram;

			//memset (palette_map, 0, sizeof (palette_map));
                for (offs = wc90_tile_videoram_size2[0] - 1; offs >= 0; offs--) {
                    cram = wc90_tile_colorram2.read(offs);
                    tile = wc90_tile_videoram2.read(offs) + 256 * ((cram & 3) + ((cram >> 1) & 4));
                    palette_map[3 * 16 + (cram >> 4)] |= Machine.gfx[2].pen_usage[tile];
                }
                for (offs = wc90_tile_videoram_size[0] - 1; offs >= 0; offs--) {
                    cram = wc90_tile_colorram.read(offs);
                    tile = wc90_tile_videoram.read(offs) + 256 * ((cram & 3) + ((cram >> 1) & 4));
                    palette_map[2 * 16 + (cram >> 4)] |= Machine.gfx[1].pen_usage[tile];
                }
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    cram = colorram.read(offs);
                    tile = videoram.read(offs) + ((cram & 0x07) << 8);
                    palette_map[1 * 16 + (cram >> 4)] |= Machine.gfx[0].pen_usage[tile];
                }
                for (offs = 0; offs < spriteram_size[0]; offs += 16) {
                    int bank = spriteram.read(offs + 0);

                    if ((bank & 4) != 0) { /* visible */

                        int flags = spriteram.read(offs + 4);
                        palette_map[0 * 16 + (flags >> 4)] |= 0xfffe;
                    }
                }

                /* expand the results */
                for (i = 0; i < 4 * 16; i++) {
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
				//	memset (&palette_used_colors[i * 16 + 0], PALETTE_COLOR_UNUSED, 16);
                        //System.out.println("TODO");
                        for (j = 1; j < 16; j++) {
                            palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
                        }
                    }
                }

                if (palette_recalc() != null) {
                    memset(dirtybuffer, 1, videoram_size[0]);
                    memset(dirtybuffer1, 1, wc90_tile_videoram_size[0]);
                    memset(dirtybuffer2, 1, wc90_tile_videoram_size2[0]);
                }
            }

            /* commented out -- if we copyscrollbitmap below with TRANSPARENCY_NONE, we shouldn't waste our
             time here:
             wc90_draw_sprites( bitmap, 3 );
             */
            for (offs = last_tile2[0] - 1; offs >= 0; offs--) {
                int sx, sy, tile;

                if (dirtybuffer2[offs] != 0) {

                    dirtybuffer2[offs] = 0;

                    sx = (offs % 64);
                    sy = (offs / 64);

                    tile = wc90_tile_videoram2.read(offs)
                            + 256 * ((wc90_tile_colorram2.read(offs) & 3) + ((wc90_tile_colorram2.read(offs) >> 1) & 4));

                    drawgfx(tmpbitmap2, Machine.gfx[2],
                            tile,
                            wc90_tile_colorram2.read(offs) >> 4,
                            0, 0,
                            sx * 16, sy * 16,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            last_tile2[0] = -1;

            scrollx = -wc90_scroll2xlo.read(0) - 256 * (wc90_scroll2xhi.read(0) & 3);
            scrolly = -wc90_scroll2ylo.read(0) - 256 * (wc90_scroll2yhi.read(0) & 1);

            copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            wc90_draw_sprites(bitmap, 2);

            for (offs = last_tile1[0] - 1; offs >= 0; offs--) {
                int sx, sy, tile;
                if (dirtybuffer1[offs] != 0) {

                    dirtybuffer1[offs] = 0;

                    sx = (offs % 64);
                    sy = (offs / 64);

                    tile = wc90_tile_videoram.read(offs)
                            + 256 * ((wc90_tile_colorram.read(offs) & 3) + ((wc90_tile_colorram.read(offs) >> 1) & 4));

                    drawgfx(tmpbitmap1, Machine.gfx[1],
                            tile,
                            wc90_tile_colorram.read(offs) >> 4,
                            0, 0,
                            sx * 16, sy * 16,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            last_tile1[0] = -1;

            scrollx = -wc90_scroll1xlo.read(0) - 256 * (wc90_scroll1xhi.read(0) & 3);
            scrolly = -wc90_scroll1ylo.read(0) - 256 * (wc90_scroll1yhi.read(0) & 1);

            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            wc90_draw_sprites(bitmap, 1);

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

            scrollx = -wc90_scroll0xlo.read(0) - 256 * (wc90_scroll0xhi.read(0) & 1);
            scrolly = -wc90_scroll0ylo.read(0) - 256 * (wc90_scroll0yhi.read(0) & 1);

            copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            wc90_draw_sprites(bitmap, 0);
        }
    };

    public static abstract interface drawsprites_proc_procPtr {

        public abstract void handler(osd_bitmap bitmap, int code,
                int sx, int sy, int bank, int flags);
    }

    public static void WC90_DRAW_SPRITE(int code, int sx, int sy, int flags, int bank, osd_bitmap bitmap) {
        drawgfx(bitmap, Machine.gfx[3], code, flags >> 4,
                bank & 1, bank & 2, sx, sy, Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
    }

    static char pos32x32[] = {0, 1, 2, 3};
    static char pos32x32x[] = {1, 0, 3, 2};
    static char pos32x32y[] = {2, 3, 0, 1};
    static char pos32x32xy[] = {3, 2, 1, 0};

    static char pos32x64[] = {0, 1, 2, 3, 4, 5, 6, 7};
    static char pos32x64x[] = {5, 4, 7, 6, 1, 0, 3, 2};
    static char pos32x64y[] = {2, 3, 0, 1, 6, 7, 4, 5};
    static char pos32x64xy[] = {7, 6, 5, 4, 3, 2, 1, 0};

    static char pos64x32[] = {0, 1, 2, 3, 4, 5, 6, 7};
    static char pos64x32x[] = {1, 0, 3, 2, 5, 4, 7, 6};
    static char pos64x32y[] = {6, 7, 4, 5, 2, 3, 0, 1};
    static char pos64x32xy[] = {7, 6, 5, 4, 3, 2, 1, 0};

    static char pos64x64[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    static char pos64x64x[] = {5, 4, 7, 6, 1, 0, 3, 2, 13, 12, 15, 14, 9, 8, 11, 10};
    static char pos64x64y[] = {10, 11, 8, 9, 14, 15, 12, 13, 2, 3, 0, 1, 6, 7, 4, 5};
    static char pos64x64xy[] = {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

    static ArrayList<char[]> p32x32 = new ArrayList<>(Arrays.asList(pos32x32, pos32x32x, pos32x32y, pos32x32xy));

    static ArrayList<char[]> p32x64 = new ArrayList<>(Arrays.asList(pos32x64, pos32x64x, pos32x64y, pos32x64xy));

    static ArrayList<char[]> p64x32 = new ArrayList<>(Arrays.asList(pos64x32, pos64x32x, pos64x32y, pos64x32xy));

    static ArrayList<char[]> p64x64 = new ArrayList<>(Arrays.asList(pos64x64, pos64x64x, pos64x64y, pos64x64xy));

    public static drawsprites_proc_procPtr drawsprite_16x16 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {

            WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
        }
    };
    public static drawsprites_proc_procPtr drawsprite_16x32 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {
            if ((bank & 2) != 0) {
                WC90_DRAW_SPRITE(code + 1, sx, sy + 16, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
            } else {
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx, sy + 16, flags, bank, bitmap);
            }
        }
    };
    public static drawsprites_proc_procPtr drawsprite_16x64 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {

            if ((bank & 2) != 0) {
                WC90_DRAW_SPRITE(code + 3, sx, sy + 48, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 2, sx, sy + 32, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx, sy + 16, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
            } else {
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx, sy + 16, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 2, sx, sy + 32, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 3, sx, sy + 48, flags, bank, bitmap);
            }
        }
    };
    public static drawsprites_proc_procPtr drawsprite_32x16 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {
            if ((bank & 1) != 0) {
                WC90_DRAW_SPRITE(code + 1, sx + 16, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
            } else {
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx + 16, sy, flags, bank, bitmap);
            }
        }
    };

    public static drawsprites_proc_procPtr drawsprite_32x32 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {
            char[] p = p32x32.get(bank & 3);

            WC90_DRAW_SPRITE(code + p[0], sx, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[1], sx + 16, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[2], sx, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[3], sx + 16, sy + 16, flags, bank, bitmap);
        }
    };

    public static drawsprites_proc_procPtr drawsprite_32x64 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {

            char[] p = p32x64.get(bank & 3);

            WC90_DRAW_SPRITE(code + p[0], sx, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[1], sx + 16, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[2], sx, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[3], sx + 16, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[4], sx, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[5], sx + 16, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[6], sx, sy + 48, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[7], sx + 16, sy + 48, flags, bank, bitmap);
        }
    };
    public static drawsprites_proc_procPtr drawsprite_64x16 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {
            if ((bank & 1) != 0) {
                WC90_DRAW_SPRITE(code + 3, sx + 48, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 2, sx + 32, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx + 16, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
            } else {
                WC90_DRAW_SPRITE(code, sx, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 1, sx + 16, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 2, sx + 32, sy, flags, bank, bitmap);
                WC90_DRAW_SPRITE(code + 3, sx + 48, sy, flags, bank, bitmap);
            }
        }
    };
    public static drawsprites_proc_procPtr drawsprite_64x32 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {

            char[] p = p64x32.get(bank & 3);

            WC90_DRAW_SPRITE(code + p[0], sx, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[1], sx + 16, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[2], sx, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[3], sx + 16, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[4], sx + 32, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[5], sx + 48, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[6], sx + 32, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[7], sx + 48, sy + 16, flags, bank, bitmap);
        }
    };

    public static drawsprites_proc_procPtr drawsprite_64x64 = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {

            char[] p = p64x64.get(bank & 3);

            WC90_DRAW_SPRITE(code + p[0], sx, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[1], sx + 16, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[2], sx, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[3], sx + 16, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[4], sx + 32, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[5], sx + 48, sy, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[6], sx + 32, sy + 16, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[7], sx + 48, sy + 16, flags, bank, bitmap);

            WC90_DRAW_SPRITE(code + p[8], sx, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[9], sx + 16, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[10], sx, sy + 48, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[11], sx + 16, sy + 48, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[12], sx + 32, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[13], sx + 48, sy + 32, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[14], sx + 32, sy + 48, flags, bank, bitmap);
            WC90_DRAW_SPRITE(code + p[15], sx + 48, sy + 48, flags, bank, bitmap);
        }
    };

    public static drawsprites_proc_procPtr drawsprite_invalid = new drawsprites_proc_procPtr() {
        public void handler(osd_bitmap bitmap, int code, int sx, int sy, int bank, int flags) {
            if (errorlog != null) {
                fprintf(errorlog, "8 pixel sprite size not supported\n");
            }
        }
    };

    public static drawsprites_proc_procPtr drawsprites_proc[]
            = {
                drawsprite_invalid, /* 0000 = 08x08 */
                drawsprite_invalid, /* 0001 = 16x08 */
                drawsprite_invalid, /* 0010 = 32x08 */
                drawsprite_invalid, /* 0011 = 64x08 */
                drawsprite_invalid, /* 0100 = 08x16 */
                drawsprite_16x16, /* 0101 = 16x16 */
                drawsprite_32x16, /* 0110 = 32x16 */
                drawsprite_64x16, /* 0111 = 64x16 */
                drawsprite_invalid, /* 1000 = 08x32 */
                drawsprite_16x32, /* 1001 = 16x32 */
                drawsprite_32x32, /* 1010 = 32x32 */
                drawsprite_64x32, /* 1011 = 64x32 */
                drawsprite_invalid, /* 1100 = 08x64 */
                drawsprite_16x64, /* 1101 = 16x64 */
                drawsprite_32x64, /* 1110 = 32x64 */
                drawsprite_64x64 /* 1111 = 64x64 */};
	//static drawsprites_procdef drawsprites_proc[16] = {
    //	

    static void wc90_draw_sprites(osd_bitmap bitmap, int priority) {
        int offs, sx, sy, flags, which;

        /* draw all visible sprites of specified priority */
        for (offs = 0; offs < spriteram_size[0]; offs += 16) {
            int bank = spriteram.read(offs + 0);

            if ((bank >> 4) == priority) {

                if ((bank & 4) != 0) { /* visible */

                    which = (spriteram.read(offs + 2) >> 2) + (spriteram.read(offs + 3) << 6);

                    sx = spriteram.read(offs + 8) + ((spriteram.read(offs + 9) & 1) << 8);
                    sy = spriteram.read(offs + 6) + ((spriteram.read(offs + 7) & 1) << 8);

                    flags = spriteram.read(offs + 4);
                    drawsprites_proc[flags & 0x0f].handler(bitmap, which, sx, sy, bank, flags);
                    //( *( drawsprites_proc[ flags & 0x0f ] ) )( bitmap, which, sx, sy, bank, flags );
                }
            }
        }
    }
}
