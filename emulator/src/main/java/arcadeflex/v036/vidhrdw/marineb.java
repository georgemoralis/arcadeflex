/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class marineb {

    public static UBytePtr marineb_column_scroll = new UBytePtr();
    public static int marineb_active_low_flipscreen;
    public static int palbank;

    static int flipscreen_x;
    static int flipscreen_y;

    public static WriteHandlerPtr marineb_palbank0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((palbank & 1) != (data & 1)) {
                palbank = (palbank & ~1) | (data & 1);
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr marineb_palbank1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data <<= 1;
            if ((palbank & 2) != (data & 2)) {
                palbank = (palbank & ~2) | (data & 2);
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr marineb_flipscreen_x_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen_x != (data ^ marineb_active_low_flipscreen)) {
                flipscreen_x = data ^ marineb_active_low_flipscreen;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr marineb_flipscreen_y_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen_y != (data ^ marineb_active_low_flipscreen)) {
                flipscreen_y = data ^ marineb_active_low_flipscreen;
                memset(dirtybuffer, 1, videoram_size[0]);
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
    static void draw_chars(osd_bitmap _tmpbitmap, osd_bitmap bitmap,
            int scroll_cols) {
        int offs;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            if (dirtybuffer[offs] != 0) {
                int sx, sy, flipx, flipy;

                dirtybuffer[offs] = 0;

                sx = offs % 32;
                sy = offs / 32;

                flipx = colorram.read(offs) & 0x20;
                flipy = colorram.read(offs) & 0x10;

                if (flipscreen_y != 0) {
                    sy = 31 - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x != 0) {
                    sx = 31 - sx;
                    flipx = NOT(flipx);
                }

                drawgfx(_tmpbitmap, Machine.gfx[0],
                        videoram.read(offs) | ((colorram.read(offs) & 0xc0) << 2),
                        (colorram.read(offs) & 0x0f) + 16 * palbank,
                        flipx, flipy,
                        8 * sx, 8 * sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }

        /* copy the temporary bitmap to the screen */
        {
            int[] scroll = new int[32];

            if (flipscreen_y != 0) {
                for (offs = 0; offs < 32 - scroll_cols; offs++) {
                    scroll[offs] = 0;
                }

                for (; offs < 32; offs++) {
                    scroll[offs] = marineb_column_scroll.read(0);
                }
            } else {
                for (offs = 0; offs < scroll_cols; offs++) {
                    scroll[offs] = -marineb_column_scroll.read(0);
                }

                for (; offs < 32; offs++) {
                    scroll[offs] = 0;
                }
            }
            copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
        }
    }

    public static VhUpdatePtr marineb_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            draw_chars(tmpbitmap, bitmap, 24);

            /* draw the sprites */
            for (offs = 0x0f; offs >= 0; offs--) {
                int gfx, sx, sy, code, col, flipx, flipy, offs2;

                if ((offs == 0) || (offs == 2)) {
                    continue;
                    /* no sprites here */
                }

                if (offs < 8) {
                    offs2 = 0x0018 + offs;
                } else {
                    offs2 = 0x03d8 - 8 + offs;
                }

                code = videoram.read(offs2);
                sx = videoram.read(offs2 + 0x20);
                sy = colorram.read(offs2);
                col = (colorram.read(offs2 + 0x20) & 0x0f) + 16 * palbank;
                flipx = code & 0x02;
                flipy = NOT(code & 0x01);

                if (offs < 4) {
                    /* big sprite */
                    gfx = 2;
                    code = (code >> 4) | ((code & 0x0c) << 2);
                } else {
                    /* small sprite */
                    gfx = 1;
                    code >>= 2;
                }

                if (flipscreen_y == 0) {
                    sy = 256 - Machine.gfx[gfx].width - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x != 0) {
                    sx++;
                }

                drawgfx(bitmap, Machine.gfx[gfx],
                        code,
                        col,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    public static VhUpdatePtr changes_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy, code, col, flipx, flipy;

            draw_chars(tmpbitmap, bitmap, 26);

            /* draw the small sprites */
            for (offs = 0x05; offs >= 0; offs--) {
                int offs2;

                offs2 = 0x001a + offs;

                code = videoram.read(offs2);
                sx = videoram.read(offs2 + 0x20);
                sy = colorram.read(offs2);
                col = (colorram.read(offs2 + 0x20) & 0x0f) + 16 * palbank;
                flipx = code & 0x02;
                flipy = NOT((code & 0x01));

                if (flipscreen_y == 0) {
                    sy = 256 - Machine.gfx[1].width - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x != 0) {
                    sx++;
                }

                drawgfx(bitmap, Machine.gfx[1],
                        code >> 2,
                        col,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* draw the big sprite */
            code = videoram.read(0x3df);
            sx = videoram.read(0x3ff);
            sy = colorram.read(0x3df);
            col = colorram.read(0x3ff);
            flipx = code & 0x02;
            flipy = NOT(code & 0x01);

            if (flipscreen_y == 0) {
                sy = 256 - Machine.gfx[2].width - sy;
                flipy = NOT(flipy);
            }

            if (flipscreen_x != 0) {
                sx++;
            }

            code >>= 4;

            drawgfx(bitmap, Machine.gfx[2],
                    code,
                    col,
                    flipx, flipy,
                    sx, sy,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

            /* draw again for wrap around */
            drawgfx(bitmap, Machine.gfx[2],
                    code,
                    col,
                    flipx, flipy,
                    sx - 256, sy,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
        }
    };

    public static VhUpdatePtr springer_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            draw_chars(tmpbitmap, bitmap, 0);

            /* draw the sprites */
            for (offs = 0x0f; offs >= 0; offs--) {
                int gfx, sx, sy, code, col, flipx, flipy, offs2;

                if ((offs == 0) || (offs == 2)) {
                    continue;
                    /* no sprites here */
                }

                offs2 = 0x0010 + offs;

                code = videoram.read(offs2);
                sx = 240 - videoram.read(offs2 + 0x20);
                sy = colorram.read(offs2);
                col = (colorram.read(offs2 + 0x20) & 0x0f) + 16 * palbank;
                flipx = NOT(code & 0x02);
                flipy = NOT(code & 0x01);

                if (offs < 4) {
                    /* big sprite */
                    sx -= 0x10;
                    gfx = 2;
                    code = (code >> 4) | ((code & 0x0c) << 2);
                } else {
                    /* small sprite */
                    gfx = 1;
                    code >>= 2;
                }

                if (flipscreen_y == 0) {
                    sy = 256 - Machine.gfx[gfx].width - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x == 0) {
                    sx--;
                }

                drawgfx(bitmap, Machine.gfx[gfx],
                        code,
                        col,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    public static VhUpdatePtr hoccer_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            draw_chars(tmpbitmap, bitmap, 0);

            /* draw the sprites */
            for (offs = 0x07; offs >= 0; offs--) {
                int sx, sy, code, col, flipx, flipy, offs2;

                offs2 = 0x0018 + offs;

                code = spriteram.read(offs2);
                sx = spriteram.read(offs2 + 0x20);
                sy = colorram.read(offs2);
                col = colorram.read(offs2 + 0x20);
                flipx = code & 0x02;
                flipy = NOT(code & 0x01);

                if (flipscreen_y == 0) {
                    sy = 256 - Machine.gfx[1].width - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x != 0) {
                    sx = 256 - Machine.gfx[1].width - sx;
                    flipx = NOT(flipx);
                }

                drawgfx(bitmap, Machine.gfx[1],
                        code >> 2,
                        col,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    public static VhUpdatePtr hopprobo_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            draw_chars(tmpbitmap, bitmap, 0);

            /* draw the sprites */
            for (offs = 0x0f; offs >= 0; offs--) {
                int gfx, sx, sy, code, col, flipx, flipy, offs2;

                if ((offs == 0) || (offs == 2)) {
                    continue;
                    /* no sprites here */
                }

                offs2 = 0x0010 + offs;

                code = videoram.read(offs2);
                sx = videoram.read(offs2 + 0x20);
                sy = colorram.read(offs2);
                col = (colorram.read(offs2 + 0x20) & 0x0f) + 16 * palbank;
                flipx = code & 0x02;
                flipy = NOT(code & 0x01);

                if (offs < 4) {
                    /* big sprite */
                    gfx = 2;
                    code = (code >> 4) | ((code & 0x0c) << 2);
                } else {
                    /* small sprite */
                    gfx = 1;
                    code >>= 2;
                }

                if (flipscreen_y == 0) {
                    sy = 256 - Machine.gfx[gfx].width - sy;
                    flipy = NOT(flipy);
                }

                if (flipscreen_x == 0) {
                    sx--;
                }

                drawgfx(bitmap, Machine.gfx[gfx],
                        code,
                        col,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
