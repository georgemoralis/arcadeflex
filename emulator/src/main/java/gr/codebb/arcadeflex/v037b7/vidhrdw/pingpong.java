/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class pingpong {

    /* This is strange; it's unlikely that the sprites actually have a hardware */
 /* clipping region, but I haven't found another way to have them masked by */
 /* the characters at the top and bottom of the screen. */
    static rectangle spritevisiblearea = new rectangle(
            0 * 8, 32 * 8 - 1,
            4 * 8, 29 * 8 - 1
    );

    /**
     * *************************************************************************
     * <p>
     * Convert the color PROMs into a more useable format.
     * <p>
     * Ping Pong has a 32 bytes palette PROM and two 256 bytes color lookup
     * table PROMs (one for sprites, one for characters). I don't know for sure
     * how the palette PROM is connected to the RGB output, but it's probably
     * the usual:
     * <p>
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     * <p>
     * *************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr pingpong_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
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

            /* color_prom now points to the beginning of the char lookup table */
 /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                int code;
                int bit0, bit1, bit2, bit3;

                /* the bits of the color code are in reverse order - 0123 instead of 3210 */
                code = (color_prom.readinc()) & 0x0f;
                bit0 = (code >> 0) & 1;
                bit1 = (code >> 1) & 1;
                bit2 = (code >> 2) & 1;
                bit3 = (code >> 3) & 1;
                code = (bit0 << 3) | (bit1 << 2) | (bit2 << 1) | (bit3 << 0);
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) code;
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }
        }
    };

    /**
     * *************************************************************************
     * <p>
     * Draw the game screen in the given osd_bitmap.
     * <p>
     * *************************************************************************
     */
    public static VhUpdatePtr pingpong_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy, tchar, color;

                    sx = offs % 32;
                    sy = offs / 32;

                    dirtybuffer[offs] = 0;

                    flipx = colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    color = colorram.read(offs) & 0x1F;
                    tchar = (videoram.read(offs) + ((colorram.read(offs) & 0x20) << 3));

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            tchar,
                            color,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy, color, schar;

                sx = spriteram.read(offs + 3);
                sy = 241 - spriteram.read(offs + 1);

                flipx = spriteram.read(offs) & 0x40;
                flipy = spriteram.read(offs) & 0x80;
                color = spriteram.read(offs) & 0x1F;
                schar = spriteram.read(offs + 2) & 0x7F;

                drawgfx(bitmap, Machine.gfx[1],
                        schar,
                        color,
                        flipx, flipy,
                        sx, sy,
                        spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }
        }
    };
}
