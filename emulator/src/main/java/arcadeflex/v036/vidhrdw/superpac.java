/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class superpac {

    public static VhConvertColorPromPtr superpac_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                bit0 = (color_prom.read(31 - i) >> 0) & 0x01;
                bit1 = (color_prom.read(31 - i) >> 1) & 0x01;
                bit2 = (color_prom.read(31 - i) >> 2) & 0x01;
                palette[3 * i] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                bit0 = (color_prom.read(31 - i) >> 3) & 0x01;
                bit1 = (color_prom.read(31 - i) >> 4) & 0x01;
                bit2 = (color_prom.read(31 - i) >> 5) & 0x01;
                palette[3 * i + 1] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                bit0 = 0;
                bit1 = (color_prom.read(31 - i) >> 6) & 0x01;
                bit2 = (color_prom.read(31 - i) >> 7) & 0x01;
                palette[3 * i + 2] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
            }

            /* characters */
            for (i = 0; i < 64 * 4; i++) {
                colortable[i] = (char) (color_prom.read(i + 32) & 0x0f);
            }
            /* sprites */
            for (i = 64 * 4; i < 128 * 4; i++) {
                colortable[i] = (char) (0x1f - (color_prom.read(i + 32) & 0x0f));
            }
        }
    };

    public static void superpac_draw_sprite(osd_bitmap dest,/*unsigned*/ int code,/*unsigned*/ int color,
            int flipx, int flipy, int sx, int sy) {
        drawgfx(dest, Machine.gfx[1], code, color, flipx, flipy, sx, sy, Machine.drv.visible_area,
                TRANSPARENCY_COLOR, 16);
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
    public static VhUpdatePtr superpac_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, mx, my;

                    dirtybuffer[offs] = 0;

                    /* Even if Super Pac-Man's screen is 28x36, the memory layout is 32x32. We therefore */
 /* have to convert the memory coordinates into screen coordinates. */
 /* Note that 32*32 = 1024, while 28*36 = 1008: therefore 16 bytes of Video RAM */
 /* don't map to a screen position. We don't check that here, however: range */
 /* checking is performed by drawgfx(). */
                    mx = offs % 32;
                    my = offs / 32;

                    if (my <= 1) {
                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        sx = mx + 2;
                        sy = my - 2;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs),
                            colorram.read(offs),
                            0, 0, 8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                /* is it on? */
                if ((spriteram_3.read(offs + 1) & 2) == 0) {
                    int sprite = spriteram.read(offs);
                    int color = spriteram.read(offs + 1);
                    int x = (spriteram_2.read(offs + 1) - 40) + 0x100 * (spriteram_3.read(offs + 1) & 1);
                    int y = 28 * 8 - spriteram_2.read(offs) + 1;
                    int flipx = spriteram_3.read(offs) & 1;
                    int flipy = spriteram_3.read(offs) & 2;

                    switch (spriteram_3.read(offs) & 0x0c) {
                        case 0:
                            /* normal size */

                            superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y);
                            break;

                        case 4:
                            /* 2x horizontal */

                            sprite &= ~1;
                            if (flipx == 0) {
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x + 16, y);
                            } else {
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x + 16, y);
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x, y);
                            }
                            break;

                        case 8:
                            /* 2x vertical */

                            sprite &= ~2;
                            if (flipy == 0) {
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y - 16);
                            } else {
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x, y - 16);
                            }
                            break;

                        case 12:
                            /* 2x both ways */

                            sprite &= ~3;
                            if (flipx == 0 && flipy == 0) {
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, 3 + sprite, color, flipx, flipy, x + 16, y);
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y - 16);
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x + 16, y - 16);
                            } else if (flipx != 0 && flipy != 0) {
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x + 16, y);
                                superpac_draw_sprite(bitmap, 3 + sprite, color, flipx, flipy, x, y - 16);
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x + 16, y - 16);
                            } else if (flipy != 0) {
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x + 16, y);
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x, y - 16);
                                superpac_draw_sprite(bitmap, 3 + sprite, color, flipx, flipy, x + 16, y - 16);
                            } else /* flipx */ {
                                superpac_draw_sprite(bitmap, 3 + sprite, color, flipx, flipy, x, y);
                                superpac_draw_sprite(bitmap, 2 + sprite, color, flipx, flipy, x + 16, y);
                                superpac_draw_sprite(bitmap, 1 + sprite, color, flipx, flipy, x, y - 16);
                                superpac_draw_sprite(bitmap, sprite, color, flipx, flipy, x + 16, y - 16);
                            }
                            break;
                    }
                }
            }
        }
    };
}
