/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 07/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.phozon.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstdlib.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class phozon {

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * The palette PROMs are connected to the RGB output this way:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr phozon_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
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

 /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }
            /* sprites */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }
        }
    };

    public static VhStartHandlerPtr phozon_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            /* set up spriteram area */
            spriteram_size[0] = 0x80;
            spriteram = new UBytePtr(phozon_spriteram, 0x780);
            spriteram_2 = new UBytePtr(phozon_spriteram, 0x780 + 0x800);
            spriteram_3 = new UBytePtr(phozon_spriteram, 0x780 + 0x800 + 0x800);

            return generic_vh_start.handler();
        }
    };

    public static VhStopHandlerPtr phozon_vh_stop = new VhStopHandlerPtr() {
        public void handler() {

            generic_vh_stop.handler();
        }
    };

    public static void phozon_draw_sprite(osd_bitmap dest, int code, int color,
            int flipx, int flipy, int sx, int sy) {
        drawgfx(dest, Machine.gfx[2], code, color, flipx, flipy, sx, sy, Machine.drv.visible_area,
                TRANSPARENCY_PEN, 0);
    }

    public static void phozon_draw_sprite8(osd_bitmap dest, int code, int color,
            int flipx, int flipy, int sx, int sy) {
        drawgfx(dest, Machine.gfx[3], code, color, flipx, flipy, sx, sy, Machine.drv.visible_area,
                TRANSPARENCY_PEN, 0);
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
    public static VhUpdateHandlerPtr phozon_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, mx, my;

                    dirtybuffer[offs] = 0;

                    /* Even if Phozon screen is 28x36, the memory layout is 32x32. We therefore
                     have to convert the memory coordinates into screen coordinates.
                     Note that 32*32 = 1024, while 28*36 = 1008: therefore 16 bytes of Video RAM
                     don't map to a screen position. We don't check that here, however: range
                     checking is performed by drawgfx(). */
                    mx = offs % 32;
                    my = offs / 32;

                    if (my <= 1) {
                        /* bottom screen characters */

                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        /* top screen characters */

                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        /* middle screen characters */

                        sx = mx + 2;
                        sy = my - 2;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[(colorram.read(offs) & 0x80) != 0 ? 1 : 0],
                            videoram.read(offs),
                            colorram.read(offs) & 0x3f,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                /* is it on? */
                if ((spriteram_3.read(offs + 1) & 2) == 0) {
                    int sprite = spriteram.read(offs);
                    int color = spriteram.read(offs + 1);
                    int x = (spriteram_2.read(offs + 1) - 69) + 0x100 * (spriteram_3.read(offs + 1) & 1);
                    int y = (Machine.drv.screen_height) - spriteram_2.read(offs) - 8;
                    int flipx = spriteram_3.read(offs) & 1;
                    int flipy = spriteram_3.read(offs) & 2;

                    switch (spriteram_3.read(offs) & 0x3c) {
                        case 0x00:
                            /* 16x16 */

                            phozon_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y);
                            break;

                        case 0x14:
                            /* 8x8 */

                            sprite = (sprite << 2) | ((spriteram_3.read(offs) & 0xc0) >> 6);
                            phozon_draw_sprite8(bitmap, sprite, color, flipx, flipy, x, y + 8);
                            break;

                        case 0x04:
                            /* 8x16 */

                            sprite = (sprite << 2) | ((spriteram_3.read(offs) & 0xc0) >> 6);
                            if (flipy == 0) {
                                phozon_draw_sprite8(bitmap, 2 + sprite, color, flipx, flipy, x, y + 8);
                                phozon_draw_sprite8(bitmap, sprite, color, flipx, flipy, x, y);
                            } else {
                                phozon_draw_sprite8(bitmap, 2 + sprite, color, flipx, flipy, x, y);
                                phozon_draw_sprite8(bitmap, sprite, color, flipx, flipy, x, y + 8);
                            }
                            break;

                        case 0x24:
                            /* 8x32 */

                            sprite = (sprite << 2) | ((spriteram_3.read(offs) & 0xc0) >> 6);
                            if (flipy == 0) {
                                phozon_draw_sprite8(bitmap, 10 + sprite, color, flipx, flipy, x, y + 8);
                                phozon_draw_sprite8(bitmap, 8 + sprite, color, flipx, flipy, x, y);
                                phozon_draw_sprite8(bitmap, 2 + sprite, color, flipx, flipy, x, y - 8);
                                phozon_draw_sprite8(bitmap, sprite, color, flipx, flipy, x, y - 16);
                            } else {
                                phozon_draw_sprite8(bitmap, 10 + sprite, color, flipx, flipy, x, y - 16);
                                phozon_draw_sprite8(bitmap, 8 + sprite, color, flipx, flipy, x, y - 8);
                                phozon_draw_sprite8(bitmap, 2 + sprite, color, flipx, flipy, x, y);
                                phozon_draw_sprite8(bitmap, sprite, color, flipx, flipy, x, y + 8);
                            }
                            break;

                        default:
                            phozon_draw_sprite(bitmap, Math.abs(rand()), color, flipx, flipy, x, y);
                            break;
                    }
                }
            }

            /* redraw high priority chars */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if ((colorram.read(offs) & 0x40) != 0) {
                    int sx, sy, mx, my;

                    /* Even if Phozon screen is 28x36, the memory layout is 32x32. We therefore
                     have to convert the memory coordinates into screen coordinates.
                     Note that 32*32 = 1024, while 28*36 = 1008: therefore 16 bytes of Video RAM
                     don't map to a screen position. We don't check that here, however: range
                     checking is performed by drawgfx(). */
                    mx = offs % 32;
                    my = offs / 32;

                    if (my <= 1) {
                        /* bottom screen characters */

                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        /* top screen characters */

                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        /* middle screen characters */

                        sx = mx + 2;
                        sy = my - 2;
                    }

                    drawgfx(bitmap, Machine.gfx[(colorram.read(offs) & 0x80) != 0 ? 1 : 0],
                            videoram.read(offs),
                            colorram.read(offs) & 0x3f,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
