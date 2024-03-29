/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 08/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class trackfld {

    public static UBytePtr trackfld_scroll = new UBytePtr();
    public static UBytePtr trackfld_scroll2 = new UBytePtr();
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Track 'n Field has one 32x8 palette PROM and two 256x4 lookup table PROMs
     * (one for characters, one for sprites). The palette PROM is connected to
     * the RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr trackfld_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
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
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
 /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr trackfld_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            /* TracknField has a virtual screen twice as large as the visible screen */
            if ((tmpbitmap = osd_create_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer = null;
                return 1;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopHandlerPtr trackfld_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
        }
    };

    public static WriteHandlerPtr trackfld_flipscreen_w = new WriteHandlerPtr() {
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
    public static VhUpdateHandlerPtr trackfld_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 64;
                    sy = offs / 64;
                    flipx = colorram.read(offs) & 0x10;
                    flipy = colorram.read(offs) & 0x20;
                    if (flipscreen != 0) {
                        sx = 63 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0),
                            colorram.read(offs) & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                if (flipscreen != 0) {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[31 - offs] = 256 - (trackfld_scroll.read(offs) + 256 * (trackfld_scroll2.read(offs) & 1));
                    }
                } else {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[offs] = -(trackfld_scroll.read(offs) + 256 * (trackfld_scroll2.read(offs) & 1));
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 32, scroll, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 2; offs >= 0; offs -= 2) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs) - 1;
                sy = 240 - spriteram_2.read(offs + 1);
                flipx = ~spriteram_2.read(offs) & 0x40;
                flipy = spriteram_2.read(offs) & 0x80;
                if (flipscreen != 0) {
                    sy = 240 - sy;
                    flipy = NOT(flipy);
                }

                /* Note that this adjustement must be done AFTER handling flipscreen, thus */
 /* proving that this is a hardware related "feature" */
                sy += 1;

                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs + 1),
                        spriteram_2.read(offs) & 0x0f,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);

                /* redraw with wraparound */
                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs + 1),
                        spriteram_2.read(offs) & 0x0f,
                        flipx, flipy,
                        sx - 256, sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);
            }
        }
    };
}
