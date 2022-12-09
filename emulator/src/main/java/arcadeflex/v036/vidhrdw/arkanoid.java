/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual changes
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
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.machine.arkanoid.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class arkanoid {

    static int[] flipscreen = new int[2];
    static int gfxbank, palettebank;

    public static VhConvertColorPromHandlerPtr arkanoid_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
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
        }
    };

    public static WriteHandlerPtr arkanoid_d008_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 flip X and Y, I don't know which is which */
            if (flipscreen[0] != (data & 0x01)) {
                flipscreen[0] = data & 0x01;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
            if (flipscreen[1] != (data & 0x02)) {
                flipscreen[1] = data & 0x02;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bit 2 selects the input paddle */
            arkanoid_paddle_select = data & 0x04;

            /* bit 3 is coin lockout (but not the service coin) */
            coin_lockout_w.handler(0, NOT(data & 0x08));
            coin_lockout_w.handler(1, NOT(data & 0x08));

            /* bit 4 is unknown */
 /* bits 5 and 6 control gfx bank and palette bank. They are used together */
 /* so I don't know which is which. */
            if (gfxbank != ((data & 0x20) >> 5)) {
                gfxbank = (data & 0x20) >> 5;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
            if (palettebank != ((data & 0x40) >> 6)) {
                palettebank = (data & 0x40) >> 6;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bit 7 is unknown */
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
    public static VhUpdateHandlerPtr arkanoid_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                int offs2;

                offs2 = offs / 2;
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy, code;

                    dirtybuffer[offs] = 0;
                    dirtybuffer[offs + 1] = 0;

                    sx = offs2 % 32;
                    sy = offs2 / 32;

                    if (flipscreen[0] != 0) {
                        sx = 31 - sx;
                    }
                    if (flipscreen[1] != 0) {
                        sy = 31 - sy;
                    }

                    code = videoram.read(offs + 1) + ((videoram.read(offs) & 0x07) << 8) + 2048 * gfxbank;
                    drawgfx(tmpbitmap, Machine.gfx[0],
                            code,
                            ((videoram.read(offs) & 0xf8) >> 3) + 32 * palettebank,
                            flipscreen[0], flipscreen[1],
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int sx, sy, code;

                sx = spriteram.read(offs);
                sy = 248 - spriteram.read(offs + 1);
                if (flipscreen[0] != 0) {
                    sx = 248 - sx;
                }
                if (flipscreen[1] != 0) {
                    sy = 248 - sy;
                }

                code = spriteram.read(offs + 3) + ((spriteram.read(offs + 2) & 0x03) << 8) + 1024 * gfxbank;
                drawgfx(bitmap, Machine.gfx[0],
                        2 * code,
                        ((spriteram.read(offs + 2) & 0xf8) >> 3) + 32 * palettebank,
                        flipscreen[0], flipscreen[1],
                        sx, sy + (flipscreen[1] != 0 ? 8 : -8),
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                drawgfx(bitmap, Machine.gfx[0],
                        2 * code + 1,
                        ((spriteram.read(offs + 2) & 0xf8) >> 3) + 32 * palettebank,
                        flipscreen[0], flipscreen[1],
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
