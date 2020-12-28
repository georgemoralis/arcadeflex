/*
 * ported to 0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;

public class slapfght {

    public static UBytePtr slapfight_videoram = new UBytePtr();
    public static UBytePtr slapfight_colorram = new UBytePtr();
    public static int[] slapfight_videoram_size = new int[1];
    public static UBytePtr slapfight_scrollx_lo = new UBytePtr();
    public static UBytePtr slapfight_scrollx_hi = new UBytePtr();
    public static UBytePtr slapfight_scrolly = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Slapfight has three 256x4 palette PROMs (one per gun) all colours for all
     * outputs are mapped to the palette directly.
     *
     * The palette PROMs are connected to the RGB output this way:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr slapfight_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
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
    public static VhUpdatePtr slapfight_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 64;
                    sy = offs / 64;

                    drawgfx(tmpbitmap, Machine.gfx[1],
                            videoram.read(offs) + ((colorram.read(offs) & 0x0f) << 8),
                            (colorram.read(offs) & 0xf0) >> 4,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                scrollx = -(slapfight_scrollx_lo.read() + 256 * slapfight_scrollx_hi.read());
                scrolly = -slapfight_scrolly.read() + 1;
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                drawgfx(bitmap, Machine.gfx[2],
                        spriteram.read(offs) + ((spriteram.read(offs + 2) & 0xc0) << 2),
                        (spriteram.read(offs + 2) & 0x1e) >> 1,
                        0, 0,
                        /* Mysterious fudge factor sprite offset */
                        (spriteram.read(offs + 1) + ((spriteram.read(offs + 2) & 0x01) << 8)) - 13, spriteram.read(offs + 3),
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = slapfight_videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = offs % 64;
                sy = offs / 64;

                drawgfx(bitmap, Machine.gfx[0],
                        slapfight_videoram.read(offs) + ((slapfight_colorram.read(offs) & 0x03) << 8),
                        (slapfight_colorram.read(offs) & 0xfc) >> 2,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
