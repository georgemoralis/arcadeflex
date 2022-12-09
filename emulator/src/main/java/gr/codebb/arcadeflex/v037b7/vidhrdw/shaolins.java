/*
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memset;
import static common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class shaolins {

    public static UBytePtr shaolins_scroll = new UBytePtr();
    static int palettebank;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr shaolins_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
                    /* preserve transparency */
                    if ((color_prom.read() & 0x0f) == 0) {
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + j * TOTAL_COLORS(1) / 8] = (char) 0;
                    } else {
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + j * TOTAL_COLORS(1) / 8] = (char) ((color_prom.read() & 0x0f) + 32 * j);
                    }
                }

                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr shaolins_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palettebank != (data & 7)) {
                palettebank = data & 7;
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
    public static VhUpdatePtr shaolins_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int sx, sy;
            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = 0; offs < videoram_size[0]; offs++) {
                if (dirtybuffer[offs] != 0) {
                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x40) << 2),
                            (colorram.read(offs) & 0x0f) + 16 * palettebank,
                            0, colorram.read(offs) & 0x20,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];
                int i;

                for (i = 0; i < 4; i++) {
                    scroll[i] = 0;
                }
                for (i = 4; i < 32; i++) {
                    scroll[i] = -shaolins_scroll.read() - 1;
                }
                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) /* max 24 sprites */ {
                if (spriteram.read(offs) != 0 && spriteram.read(offs + 6) != 0) /* stop rogue sprites on high score screen */ {
                    drawgfx(bitmap, Machine.gfx[1],
                            spriteram.read(offs + 8),
                            (spriteram.read(offs + 9) & 0x0f) + 16 * palettebank,
                            NOT(spriteram.read(offs + 9) & 0x40), (spriteram.read(offs + 9) & 0x80),
                            240 - spriteram.read(offs + 6), 248 - spriteram.read(offs + 4),
                            Machine.visible_area, TRANSPARENCY_COLOR, 0);
                    /* transparency_color, otherwise sprites in test mode are not visible */
                }
            }
        }
    };
}
