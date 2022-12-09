/*
 * ported to 0.37b7
 *
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
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;

public class ironhors {

    public static UBytePtr ironhors_scroll = new UBytePtr();
    public static int palettebank, charbank, spriterambank;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Iron Horse has three 256x4 palette PROMs (one per gun) and two 256x4
     * lookup table PROMs (one for characters, one for sprites). I don't know
     * for sure how the palette PROMs are connected to the RGB output, but it's
     * probably the usual:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr ironhors_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

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

    public static WriteHandlerPtr ironhors_charbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (charbank != (data & 1)) {
                charbank = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            spriterambank = data & 8;

            /* other bits unknown */
        }
    };

    public static WriteHandlerPtr ironhors_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palettebank != (data & 7)) {
                palettebank = data & 7;
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
    public static VhUpdateHandlerPtr ironhors_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, i;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = 8 * (offs % 32);
                    sy = 8 * (offs / 32);

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 16 * (colorram.read(offs) & 0x20) + 4 * (colorram.read(offs) & 0x40) + 1024 * charbank,
                            (colorram.read(offs) & 0x0f) + 16 * palettebank,
                            colorram.read(offs) & 0x10, colorram.read(offs) & 0x20,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                for (i = 0; i < 32; i++) {
                    scroll[i] = -(ironhors_scroll.read(i));
                }

                copyscrollbitmap(bitmap, tmpbitmap, 32, scroll, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            {
                UBytePtr sr;

                if (spriterambank != 0) {
                    sr = spriteram;
                } else {
                    sr = spriteram_2;
                }

                for (offs = 0; offs < spriteram_size[0]; offs += 5) {
                    if (sr.read(offs + 2) != 0) {
                        int sx, sy, flipx, flipy, code, color;

                        sx = sr.read(offs + 3);
                        sy = sr.read(offs + 2);
                        flipx = sr.read(offs + 4) & 0x20;
                        flipy = sr.read(offs + 4) & 0x40;
                        code = (sr.read(offs) << 2) + ((sr.read(offs + 1) & 0x01) << 10) + ((sr.read(offs + 1) & 0x0c) >> 2);
                        color = ((sr.read(offs + 1) & 0xf0) >> 4) + 16 * palettebank;

                        switch (sr.read(offs + 4) & 0x0c) {
                            case 0x00:
                                /* 16x16 */

                                drawgfx(bitmap, Machine.gfx[1],
                                        code / 4,
                                        color,
                                        flipx, flipy,
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                                break;

                            case 0x04: /* 16x8 */ {
                                drawgfx(bitmap, Machine.gfx[2],
                                        code & ~1,
                                        color,
                                        flipx, flipy,
                                        flipx != 0 ? sx + 8 : sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                                drawgfx(bitmap, Machine.gfx[2],
                                        code | 1,
                                        color,
                                        flipx, flipy,
                                        flipx != 0 ? sx : sx + 8, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                            }
                            break;

                            case 0x08: /* 8x16 */ {
                                drawgfx(bitmap, Machine.gfx[2],
                                        code & ~2,
                                        color,
                                        flipx, flipy,
                                        sx, flipy != 0 ? sy + 8 : sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                                drawgfx(bitmap, Machine.gfx[2],
                                        code | 2,
                                        color,
                                        flipx, flipy,
                                        sx, flipy != 0 ? sy : sy + 8,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                            }
                            break;

                            case 0x0c: /* 8x8 */ {
                                drawgfx(bitmap, Machine.gfx[2],
                                        code,
                                        color,
                                        flipx, flipy,
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                            }
                            break;
                        }
                    }
                }
            }
        }
    };
}
