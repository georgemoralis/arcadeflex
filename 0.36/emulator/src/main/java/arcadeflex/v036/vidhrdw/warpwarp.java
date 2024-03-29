/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 15/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;

public class warpwarp {

    public static UBytePtr warpwarp_bulletsram = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Warp Warp doesn't use PROMs - the 8-bit code is directly converted into a
     * color.
     *
     * The color RAM is connected to the RGB output this way (I think -
     * schematics are fuzzy):
     *
     * bit 7 -- 300 ohm resistor -- BLUE -- 820 ohm resistor -- BLUE -- 300 ohm
     * resistor -- GREEN -- 820 ohm resistor -- GREEN -- 1.6kohm resistor --
     * GREEN -- 300 ohm resistor -- RED -- 820 ohm resistor -- RED bit 0 --
     * 1.6kohm resistor -- RED
     *
     * Moreover, the bullet is pure white, obtained with three 220 ohm
     * resistors.
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr warpwarp_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
            int p_ptr = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (i >> 0) & 0x01;
                bit1 = (i >> 1) & 0x01;
                bit2 = (i >> 2) & 0x01;
                palette[p_ptr++] = (char) (0x1f * bit0 + 0x3c * bit1 + 0xa4 * bit2);
                /* green component */
                bit0 = (i >> 3) & 0x01;
                bit1 = (i >> 4) & 0x01;
                bit2 = (i >> 5) & 0x01;
                palette[p_ptr++] = (char) (0x1f * bit0 + 0x3c * bit1 + 0xa4 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (i >> 6) & 0x01;
                bit2 = (i >> 7) & 0x01;
                palette[p_ptr++] = (char) (0x1f * bit0 + 0x3c * bit1 + 0xa4 * bit2);
            }

            for (i = 0; i < TOTAL_COLORS(0); i += 2) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (0);
                /* black background */
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i + 1] = (char) (i / 2);
                /* colored foreground */
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
    public static VhUpdateHandlerPtr warpwarp_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int mx, my, sx, sy;

                    mx = offs % 32;
                    my = offs / 32;

                    if (my == 0) {
                        sx = 33;
                        sy = mx;
                    } else if (my == 1) {
                        sx = 0;
                        sy = mx;
                    } else {
                        sx = mx + 1;
                        sy = my;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs),
                            colorram.read(offs),
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

                    dirtybuffer[offs] = 0;
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            if (warpwarp_bulletsram.read(0) > 1) {
                int x, y;

                x = 260 - warpwarp_bulletsram.read(0);
                y = 252 - warpwarp_bulletsram.read(1);
                if (x >= Machine.drv.visible_area.min_x && x + 3 <= Machine.drv.visible_area.max_x
                        && y >= Machine.drv.visible_area.min_y && y + 3 <= Machine.drv.visible_area.max_y) {
                    int colour;
                    int i, j;

                    colour = Machine.pens[0xf6];
                    /* white */

                    for (i = 0; i < 4; i++) {
                        for (j = 0; j < 4; j++) {
                            plot_pixel.handler(bitmap, x + j, y + i, colour);
                        }
                    }
                }
            }
        }
    };
}
