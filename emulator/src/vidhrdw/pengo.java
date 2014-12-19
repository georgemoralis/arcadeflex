/*
 * ported to v0.36
 * using automatic conversion tool v0.04 + manual conversion
 * converted at : 29-05-2013 18:29:30
 *
 *  THIS FILE IS 100% PORTED!!!
 *
 */
package vidhrdw;

import static arcadeflex.libc_old.*;
import static arcadeflex.libc.*;
import static mame.driverH.*;
import static vidhrdw.generic.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static arcadeflex.ptrlib.*;

public class pengo {

    public static int gfx_bank;
    public static int flipscreen;
    public static int xoffsethack;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    static rectangle spritevisiblearea = new rectangle(
            2 * 8, 34 * 8 - 1,
            0 * 8, 28 * 8 - 1
    );

    public static VhConvertColorPromPtr pacman_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            //UBytePtr palette= new UBytePtr(palette_table);
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            color_prom.inc(0x10);//color_prom += 0x10;

            /* color_prom now points to the beginning of the lookup table */
            /* character lookup table */
            /* sprites use the same color lookup table as characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }
        }
    };

    public static VhConvertColorPromPtr pengo_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
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
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
            /* character lookup table */
            /* sprites use the same color lookup table as characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }

            color_prom.inc(0x80); //color_prom += 0x80;

            /* second bank character lookup table */
            /* sprites use the same color lookup table as characters */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                if (color_prom.read() != 0) {
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) ((color_prom.read() & 0x0f) + 0x10);/* second palette bank */

                } else {
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = 0;/* preserve transparency */

                }
                color_prom.inc();
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
    public static VhStartPtr pengo_vh_start = new VhStartPtr() {
        public int handler() {
            gfx_bank = 0;
            xoffsethack = 0;

            return generic_vh_start.handler();
        }
    };

    public static VhStartPtr pacman_vh_start = new VhStartPtr() {
        public int handler() {
            gfx_bank = 0;
            /* In the Pac Man based games (NOT Pengo) the first two sprites must be offset */
            /* one pixel to the left to get a more correct placement */
            xoffsethack = 1;

            return generic_vh_start.handler();
        }
    };

    public static WriteHandlerPtr pengo_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* the Pengo hardware can set independently the palette bank, color lookup */
            /* table, and chars/sprites. However the game always set them together (and */
            /* the only place where this is used is the intro screen) so I don't bother */
            /* emulating the whole thing. */
            if (gfx_bank != (data & 1)) {
                gfx_bank = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr pengo_flipscreen_w = new WriteHandlerPtr() {
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
    public static VhUpdatePtr pengo_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            for (offs = videoram_size[0] - 1; offs > 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int mx, my, sx, sy;

                    dirtybuffer[offs] = 0;
                    mx = offs % 32;
                    my = offs / 32;

                    if (my < 2) {
                        if (mx < 2 || mx >= 30) {
                            continue; /* not visible */
                        }
                        sx = my + 34;
                        sy = mx - 2;
                    } else if (my >= 30) {
                        if (mx < 2 || mx >= 30) {
                            continue; /* not visible */
                        }
                        sx = my - 30;
                        sy = mx - 2;
                    } else {
                        sx = mx + 2;
                        sy = my - 2;
                    }

                    if (flipscreen != 0) {
                        sx = 35 - sx;
                        sy = 27 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[gfx_bank * 2],
                            videoram.read(offs),
                            (int) (colorram.read(offs) & 0x1f),
                            flipscreen, flipscreen,
                            sx * 8, sy * 8,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. Note that it is important to draw them exactly in this */
            /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 2; offs > 2 * 2; offs -= 2) {
                int sx, sy;

                sx = 272 - spriteram_2.read(offs + 1);
                sy = spriteram_2.read(offs) - 31;

                drawgfx(bitmap, Machine.gfx[gfx_bank * 2 + 1],
                        spriteram.read(offs) >> 2,
                        spriteram.read(offs + 1) & 0x1f,
                        spriteram.read(offs) & 1, spriteram.read(offs) & 2,
                        sx, sy,
                        spritevisiblearea, TRANSPARENCY_COLOR, 0);

                /* also plot the sprite with wraparound (tunnel in Crush Roller) */
                drawgfx(bitmap, Machine.gfx[gfx_bank * 2 + 1],
                        spriteram.read(offs) >> 2,
                        spriteram.read(offs + 1) & 0x1f,
                        spriteram.read(offs) & 1, spriteram.read(offs) & 2,
                        sx - 256, sy,
                        spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }
            /* In the Pac Man based games (NOT Pengo) the first two sprites must be offset */
            /* one pixel to the left to get a more correct placement */
            for (offs = 2 * 2; offs >= 0; offs -= 2) {
                int sx, sy;

                sx = 272 - spriteram_2.read(offs + 1);
                sy = spriteram_2.read(offs) - 31;

                drawgfx(bitmap, Machine.gfx[gfx_bank * 2 + 1],
                        spriteram.read(offs) >> 2,
                        spriteram.read(offs + 1) & 0x1f,
                        spriteram.read(offs) & 1, spriteram.read(offs) & 2,
                        sx, sy + xoffsethack,
                        spritevisiblearea, TRANSPARENCY_COLOR, 0);

                /* also plot the sprite with wraparound (tunnel in Crush Roller) */
                drawgfx(bitmap, Machine.gfx[gfx_bank * 2 + 1],
                        spriteram.read(offs) >> 2,
                        spriteram.read(offs + 1) & 0x1f,
                        spriteram.read(offs) & 2, spriteram.read(offs) & 1,
                        sx - 256, sy + xoffsethack,
                        spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }
        }
    };
}
