/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.*;
import static common.libc.expressions.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class mrjong {

    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs. (from vidhrdw/penco.c)
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr mrjong_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn, offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + (offs)])
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            color_prom.inc(0x10);
            /* color_prom now points to the beginning of the lookup table */

 /* character lookup table */
 /* sprites use the same color lookup table as characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Display control parameter.
     *
     **************************************************************************
     */
    public static WriteHandlerPtr mrjong_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = (data & 1);
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap.
     *
     **************************************************************************
     */
    public static VhUpdatePtr mrjong_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* Draw the tiles. */
            for (offs = (videoram_size[0] - 1); offs > 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int tile;
                    int color;
                    int sx, sy;
                    int flipx, flipy;

                    dirtybuffer[offs] = 0;

                    tile = videoram.read(offs) | ((colorram.read(offs) & 0x20) << 3);
                    flipx = (colorram.read(offs) & 0x40) >> 6;
                    flipy = (colorram.read(offs) & 0x80) >> 7;
                    color = colorram.read(offs) & 0x1f;

                    sx = 31 - (offs % 32);
                    sy = 31 - (offs / 32);

                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            tile,
                            color,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = (spriteram_size[0] - 4); offs >= 0; offs -= 4) {
                int sprt;
                int color;
                int sx, sy;
                int flipx, flipy;

                sprt = (((spriteram.read(offs + 1) >> 2) & 0x3f) | ((spriteram.read(offs + 3) & 0x20) << 1));
                flipx = (spriteram.read(offs + 1) & 0x01) >> 0;
                flipy = (spriteram.read(offs + 1) & 0x02) >> 1;
                color = (spriteram.read(offs + 3) & 0x1f);

                sx = 224 - spriteram.read(offs + 2);
                sy = spriteram.read(offs + 0);
                if (flipscreen != 0) {
                    sx = 208 - sx;
                    sy = 240 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[1],
                        sprt,
                        color,
                        flipx, flipy,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
