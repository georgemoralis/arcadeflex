/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;

public class bagman {

    public static UBytePtr bagman_video_enable = new UBytePtr();
    public static int[] flipscreen = new int[2];

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Bagman has two 32 bytes palette PROMs, connected to the RGB output this
     * way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr bagman_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr bagman_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 1) != flipscreen[offset]) {
                flipscreen[offset] = data & 1;
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
    public static VhUpdatePtr bagman_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (bagman_video_enable.read() == 0) {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);

                return;
            }

            /* for every character in the Video RAM, check if it has been modified */
            /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;
                    int bank;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    if (flipscreen[0] != 0) {
                        sx = 31 - sx;
                    }
                    sy = offs / 32;
                    if (flipscreen[1] != 0) {
                        sy = 31 - sy;
                    }

                    /* Pickin' doesn't have the second char bank */
                    bank = 0;
                    if (Machine.gfx[2] != null && (colorram.read(offs) & 0x10) != 0) {
                        bank = 2;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[bank],
                            videoram.read(offs) + 8 * (colorram.read(offs) & 0x20),
                            colorram.read(offs) & 0x0f,
                            flipscreen[0], flipscreen[1],
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = 240 - spriteram.read(offs + 2);
                flipx = spriteram.read(offs) & 0x40;
                flipy = spriteram.read(offs) & 0x80;
                if (flipscreen[0] != 0) {
                    sx = 240 - sx + 1;	/* compensate misplacement */

                    flipx = NOT(flipx);
                }
                if (flipscreen[1] != 0) {
                    sy = 240 - sy;
                    flipy = NOT(flipy);
                }

                if (spriteram.read(offs + 2) != 0 && spriteram.read(offs + 3) != 0) {
                    drawgfx(bitmap, Machine.gfx[1],
                            (spriteram.read(offs) & 0x3f) + 2 * (spriteram.read(offs + 1) & 0x20),
                            spriteram.read(offs + 1) & 0x1f,
                            flipx, flipy,
                            sx, sy + 1, /* compensate misplacement */
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
