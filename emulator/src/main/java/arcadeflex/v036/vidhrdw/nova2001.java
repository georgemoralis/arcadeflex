/*
 * ported to v0.36
 *
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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;

public class nova2001 {

    public static UBytePtr nova2001_videoram = new UBytePtr();
    public static UBytePtr nova2001_colorram = new UBytePtr();
    public static int[] nova2001_videoram_size = new int[1];

    static int nova2001_xscroll;
    static int nova2001_yscroll;
    static int flipscreen;

    public static VhConvertColorPromPtr nova2001_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i, j;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int intensity;

                intensity = (color_prom.read() >> 0) & 0x03;
                /* red component */
                palette[p_inc++] = ((char) ((((color_prom.read() >> 0) & 0x0c) | intensity) * 0x11));
                /* green component */
                palette[p_inc++] = ((char) ((((color_prom.read() >> 2) & 0x0c) | intensity) * 0x11));
                /* blue component */
                palette[p_inc++] = ((char) ((((color_prom.read() >> 4) & 0x0c) | intensity) * 0x11));

                color_prom.inc();
            }

            /* Color #1 is used for palette animation.          */
 /* To handle this, color entries 0-15 are based on  */
 /* the primary 16 colors, while color entries 16-31 */
 /* are based on the secondary set.                  */
 /* The only difference among 0-15 and 16-31 is that */
 /* color #1 changes each time */
            for (i = 0; i < 16; i++) {
                for (j = 0; j < 16; j++) {
                    if (j == 1) {
                        colortable[16 * i + 1] = (char) i;
                        colortable[16 * i + 16 * 16 + 1] = (char) (i + 16);
                    } else {
                        colortable[16 * i + j] = (char) j;
                        colortable[16 * i + 16 * 16 + j] = (char) (j + 16);
                    }
                }
            }
        }
    };

    public static WriteHandlerPtr nova2001_scroll_x_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nova2001_xscroll = data;
        }
    };

    public static WriteHandlerPtr nova2001_scroll_y_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nova2001_yscroll = data;
        }
    };

    public static WriteHandlerPtr nova2001_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((~data & 0x01) != flipscreen) {
                flipscreen = ~data & 0x01;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    /**
     * *************************************************************************
     * <p>
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     * <p>
     * *************************************************************************
     */
    public static VhUpdatePtr nova2001_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[1],
                            videoram.read(offs),
                            colorram.read(offs) & 0x0f,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            {
                int scrollx, scrolly;

                if (flipscreen != 0) {
                    scrollx = nova2001_xscroll;
                    scrolly = nova2001_yscroll;
                } else {
                    scrollx = -nova2001_xscroll;
                    scrolly = -nova2001_yscroll;
                }

                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Next, draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 32) {
                if ((spriteram.read(offs + 0) & 0x40) != 0) {
                    int sx, sy, flipx, flipy;

                    sx = spriteram.read(offs + 1);
                    sy = spriteram.read(offs + 2);
                    flipx = spriteram.read(offs + 3) & 0x10;
                    flipy = spriteram.read(offs + 3) & 0x20;
                    if (flipscreen != 0) {
                        sx = 240 - sx;
                        sy = 240 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(bitmap, Machine.gfx[2 + ((spriteram.read(offs + 0) & 0x80) >> 7)],
                            spriteram.read(offs + 0) & 0x3f,
                            spriteram.read(offs + 3) & 0x0f,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            /* Finally, draw the foreground text */
            for (offs = nova2001_videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = offs % 32;
                sy = offs / 32;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                }

                drawgfx(bitmap, Machine.gfx[0],
                        nova2001_videoram.read(offs),
                        nova2001_colorram.read(offs) & 0x0f,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
