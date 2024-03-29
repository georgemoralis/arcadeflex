/*
 * ported to v0.36
 *
 */
/**
 * Changelog
 * =========
 * 18/12/2022 - shadow - This file should be complete for 0.36 version
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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;

public class pooyan {

    static int flipscreen;

    /**
     * *************************************************************************
     * <p>
     * Convert the color PROMs into a more useable format.
     * <p>
     * Pooyan has one 32x8 palette PROM and two 256x4 lookup table PROMs (one
     * for characters, one for sprites). The palette PROM is connected to the
     * RGB output this way:
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

    public static VhConvertColorPromHandlerPtr pooyan_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

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
                //COLOR(1,i) = *(color_prom++) & 0x0f;
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                //COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc() & 0x0f) + 0x10);
            }
        }
    };

    public static WriteHandlerPtr pooyan_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
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
    public static VhUpdateHandlerPtr pooyan_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy;

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
                            videoram.read(offs) + 8 * (colorram.read(offs) & 0x20),
                            colorram.read(offs) & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                /* TRANSPARENCY_COLOR is needed for the scores */
                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs + 1),
                        spriteram_2.read(offs) & 0x0f,
                        spriteram_2.read(offs) & 0x40, ~spriteram_2.read(offs) & 0x80,
                        240 - spriteram.read(offs), spriteram_2.read(offs + 1),
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);
            }
        }
    };
}
