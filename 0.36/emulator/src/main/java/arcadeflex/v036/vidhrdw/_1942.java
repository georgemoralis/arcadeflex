/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + Manual fixes
 *
 */
/**
 * Changelog
 * =========
 * 17/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class _1942 {

    public static int[] c1942_backgroundram_size = new int[1];
    public static UBytePtr c1942_backgroundram = new UBytePtr();
    public static UBytePtr c1942_scroll = new UBytePtr();
    public static UBytePtr c1942_palette_bank = new UBytePtr();
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;
    static int flipscreen;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr c1942_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                /* green component */
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                /* blue component */
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 128-143 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) + 128);
            }

            /* background tiles use colors 0-63 in four banks */
            for (i = 0; i < TOTAL_COLORS(1) / 4; i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = color_prom.read();
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 32 * 8] = (char) ((color_prom.read()) + 16);
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 2 * 32 * 8] = (char) ((color_prom.read()) + 32);
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 3 * 32 * 8] = (char) ((color_prom.read()) + 48);
                color_prom.inc();
            }

            /* sprites use colors 64-79 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) ((color_prom.readinc()) + 64);
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
    public static VhStartHandlerPtr c1942_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[c1942_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, c1942_backgroundram_size[0]);

            /* the background area is twice as wide as the screen (actually twice as tall, */
 /* because this is a vertical game) */
            if ((tmpbitmap2 = osd_create_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
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
    public static VhStopHandlerPtr c1942_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr c1942_background_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (c1942_backgroundram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                c1942_backgroundram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr c1942_palette_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (c1942_palette_bank.read() != data) {
                memset(dirtybuffer2, 1, c1942_backgroundram_size[0]);
                c1942_palette_bank.write(data);
            }
        }
    };

    public static WriteHandlerPtr c1942_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 0x80)) {
                flipscreen = data & 0x80;
                memset(dirtybuffer2, 1, c1942_backgroundram_size[0]);
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
    public static VhUpdateHandlerPtr c1942_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            for (offs = c1942_backgroundram_size[0] - 1; offs >= 0; offs--) {
                if ((offs & 0x10) == 0 && (dirtybuffer2[offs] != 0 || dirtybuffer2[offs + 16] != 0)) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer2[offs] = dirtybuffer2[offs + 16] = 0;

                    sx = offs / 32;
                    sy = offs % 32;
                    flipx = c1942_backgroundram.read(offs + 16) & 0x20;
                    flipy = c1942_backgroundram.read(offs + 16) & 0x40;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 15 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            c1942_backgroundram.read(offs) + 2 * (c1942_backgroundram.read(offs + 16) & 0x80),
                            (c1942_backgroundram.read(offs + 16) & 0x1f) + 32 * c1942_palette_bank.read(),
                            flipx, flipy,
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the background graphics */
            {
                int scroll;

                scroll = -(c1942_scroll.read(0) + 256 * c1942_scroll.read(1));
                if (flipscreen != 0) {
                    scroll = 256 - scroll;
                }

                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scroll}, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int i, code, col, sx, sy, dir;

                code = (spriteram.read(offs) & 0x7f) + 4 * (spriteram.read(offs + 1) & 0x20)
                        + 2 * (spriteram.read(offs) & 0x80);
                col = spriteram.read(offs + 1) & 0x0f;
                sx = spriteram.read(offs + 3) - 0x10 * (spriteram.read(offs + 1) & 0x10);
                sy = spriteram.read(offs + 2);
                dir = 1;
                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                    dir = -1;
                }

                /* handle double / quadruple height (actually width because this is a rotated game) */
                i = (spriteram.read(offs + 1) & 0xc0) >> 6;
                if (i == 2) {
                    i = 3;
                }

                do {
                    drawgfx(bitmap, Machine.gfx[2],
                            code + i, col,
                            flipscreen, flipscreen,
                            sx, sy + 16 * i * dir,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);

                    i--;
                } while (i >= 0);
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (videoram.read(offs) != 0x30) /* don't draw spaces */ {
                    int sx, sy;

                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + 2 * (colorram.read(offs) & 0x80),
                            colorram.read(offs) & 0x3f,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
