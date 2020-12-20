/*
 * ported to v0.36
 * using automatic conversion tool v0.05 + manual fixes
 *
 *
 *
 */
package vidhrdw;

import static platform.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static platform.libc_old.*;
import static platform.ptrlib.*;

public class ladybug {

    static int flipscreen;

    public static VhConvertColorPromPtr ladybug_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            for (i = 0; i < 32; i++) {
                int bit1, bit2;

                bit1 = (~color_prom.read(i) >> 0) & 0x01;
                bit2 = (~color_prom.read(i) >> 5) & 0x01;
                palette[3 * i].set((char) (0x47 * bit1 + 0x97 * bit2));
                bit1 = (~color_prom.read(i) >> 2) & 0x01;
                bit2 = (~color_prom.read(i) >> 6) & 0x01;
                palette[3 * i + 1].set((char) (0x47 * bit1 + 0x97 * bit2));
                bit1 = (~color_prom.read(i) >> 4) & 0x01;
                bit2 = (~color_prom.read(i) >> 7) & 0x01;
                palette[3 * i + 2].set((char) (0x47 * bit1 + 0x97 * bit2));
            }

            /* characters */
            for (i = 0; i < 8; i++) {
                colortable[4 * i] = 0;
                colortable[4 * i + 1] = (char) (i + 0x08);
                colortable[4 * i + 2] = (char) (+0x10);
                colortable[4 * i + 3] = (char) (i + 0x18);
            }

            /* sprites */
            for (i = 0; i < 4 * 8; i++) {
                int bit0, bit1, bit2, bit3;

                /* low 4 bits are for sprite n */
                bit0 = (color_prom.read(i + 32) >> 3) & 0x01;
                bit1 = (color_prom.read(i + 32) >> 2) & 0x01;
                bit2 = (color_prom.read(i + 32) >> 1) & 0x01;
                bit3 = (color_prom.read(i + 32) >> 0) & 0x01;
                colortable[i + 4 * 8] = (char) (1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3);

                /* high 4 bits are for sprite n + 8 */
                bit0 = (color_prom.read(i + 32) >> 7) & 0x01;
                bit1 = (color_prom.read(i + 32) >> 6) & 0x01;
                bit2 = (color_prom.read(i + 32) >> 5) & 0x01;
                bit3 = (color_prom.read(i + 32) >> 4) & 0x01;
                colortable[i + 4 * 16] = (char) (1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3);
            }
        }
    };

    public static WriteHandlerPtr ladybug_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
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
    public static VhUpdatePtr ladybug_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i, offs;

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

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 32 * (colorram.read(offs) & 8),
                            colorram.read(offs),
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];
                int sx, sy;

                for (offs = 0; offs < 32; offs++) {
                    sx = offs % 4;
                    sy = offs / 4;

                    if (flipscreen != 0) {
                        scroll[31 - offs] = -videoram.read(32 * sx + sy);
                    } else {
                        scroll[offs] = -videoram.read(32 * sx + sy);
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 32, scroll, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
            /* order, to have the correct priorities. */
            /* sprites in the columns 15, 1 and 0 are outside of the visible area */
            for (offs = spriteram_size[0] - 2 * 0x40; offs >= 2 * 0x40; offs -= 0x40) {
                i = 0;
                while (i < 0x40 && spriteram.read(offs + i) != 0) {
                    i += 4;
                }

                while (i > 0) {
                    i -= 4;

                    if ((spriteram.read(offs + i) & 0x80) != 0) {
                        if ((spriteram.read(offs + i) & 0x40) != 0) /* 16x16 */ {
                            drawgfx(bitmap, Machine.gfx[1],
                                    (spriteram.read(offs + i + 1) >> 2) + 4 * (spriteram.read(offs + i + 2) & 0x10),
                                    spriteram.read(offs + i + 2) & 0x0f,
                                    spriteram.read(offs + i) & 0x20, spriteram.read(offs + i) & 0x10,
                                    spriteram.read(offs + i + 3),
                                    offs / 4 - 8 + (spriteram.read(offs + i) & 0x0f),
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        } else /* 8x8 */ {
                            drawgfx(bitmap, Machine.gfx[2],
                                    spriteram.read(offs + i + 1) + 4 * (spriteram.read(offs + i + 2) & 0x10),
                                    spriteram.read(offs + i + 2) & 0x0f,
                                    spriteram.read(offs + i) & 0x20, spriteram.read(offs + i) & 0x10,
                                    spriteram.read(offs + i + 3),
                                    offs / 4 + (spriteram.read(offs + i) & 0x0f),
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    }
                }
            }
        }
    };
}
