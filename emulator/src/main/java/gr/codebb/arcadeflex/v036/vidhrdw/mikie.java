/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;

public class mikie {

    static int palettebank, flipscreen;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr mikie_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++]=(char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the character lookup table */

            /* there are eight 32 colors palette banks; sprites use colors 0-15 and */
            /* characters 16-31 of each bank. */
            for (i = 0; i < TOTAL_COLORS(0) / 8; i++) {
                int j;

                for (j = 0; j < 8; j++) {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i + j * TOTAL_COLORS(0) / 8] = (char) ((color_prom.read() & 0x0f) + 32 * j + 16);
                }
                color_prom.inc();
            }

            for (i = 0; i < TOTAL_COLORS(1) / 8; i++) {
                int j;

                for (j = 0; j < 8; j++) {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + j * TOTAL_COLORS(1) / 8] = (char) ((color_prom.read() & 0x0f) + 32 * j);
                }
                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr mikie_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palettebank != (data & 7)) {
                palettebank = data & 7;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr mikie_flipscreen_w = new WriteHandlerPtr() {
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
    public static VhUpdatePtr mikie_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
            /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy, flipx, flipy;

                if (dirtybuffer[offs] != 0) {
                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x20) << 3),
                            (colorram.read(offs) & 0x0f) + 16 * palettebank,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = 244 - spriteram.read(offs + 1);
                flipx = ~spriteram.read(offs) & 0x10;
                flipy = spriteram.read(offs) & 0x20;
                if (flipscreen != 0) {
                    sy = 242 - sy;
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[(spriteram.read(offs + 2) & 0x40) != 0 ? 2 : 1],
                        (spriteram.read(offs + 2) & 0x3f) + ((spriteram.read(offs + 2) & 0x80) >> 1)
                        + ((spriteram.read(offs) & 0x40) << 1),
                        (spriteram.read(offs) & 0x0f) + 16 * palettebank,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
