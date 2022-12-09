/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;

public class seicross {

    public static UBytePtr seicross_row_scroll = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Seicross has two 32x8 palette PROMs, connected to the RGB output this
     * way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr seicross_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
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
        }
    };

    public static WriteHandlerPtr seicross_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (colorram.read(offset) != data) {
                /* bit 5 of the address is not used for color memory. There is just */
 /* 512k of memory; every two consecutive rows share the same memory */
 /* region. */
                offset &= 0xffdf;

                dirtybuffer[offset] = 1;
                dirtybuffer[offset + 0x20] = 1;

                colorram.write(offset, data);
                colorram.write(offset + 0x20, data);
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
    public static VhUpdateHandlerPtr seicross_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, x;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x10) << 4),
                            colorram.read(offs) & 0x0f,
                            colorram.read(offs) & 0x40, colorram.read(offs) & 0x80,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                for (offs = 0; offs < 32; offs++) {
                    scroll[offs] = -seicross_row_scroll.read(offs);
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw sprites */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                x = spriteram.read(offs + 3);
                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram.read(offs) & 0x3f) + ((spriteram.read(offs + 1) & 0x10) << 2) + 128,
                        spriteram.read(offs + 1) & 0x0f,
                        spriteram.read(offs) & 0x40, spriteram.read(offs) & 0x80,
                        x, 240 - spriteram.read(offs + 2),
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                if (x > 0xf0) {
                    drawgfx(bitmap, Machine.gfx[1],
                            (spriteram.read(offs) & 0x3f) + ((spriteram.read(offs + 1) & 0x10) << 2) + 128,
                            spriteram.read(offs + 1) & 0x0f,
                            spriteram.read(offs) & 0x40, spriteram.read(offs) & 0x80,
                            x - 256, 240 - spriteram.read(offs + 2),
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            for (offs = spriteram_2_size[0] - 4; offs >= 0; offs -= 4) {
                x = spriteram_2.read(offs + 3);
                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram_2.read(offs) & 0x3f) + ((spriteram_2.read(offs + 1) & 0x10) << 2),
                        spriteram_2.read(offs + 1) & 0x0f,
                        spriteram_2.read(offs) & 0x40, spriteram_2.read(offs) & 0x80,
                        x, 240 - spriteram_2.read(offs + 2),
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                if (x > 0xf0) {
                    drawgfx(bitmap, Machine.gfx[1],
                            (spriteram_2.read(offs) & 0x3f) + ((spriteram_2.read(offs + 1) & 0x10) << 2),
                            spriteram_2.read(offs + 1) & 0x0f,
                            spriteram_2.read(offs) & 0x40, spriteram_2.read(offs) & 0x80,
                            x - 256, 240 - spriteram_2.read(offs + 2),
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
