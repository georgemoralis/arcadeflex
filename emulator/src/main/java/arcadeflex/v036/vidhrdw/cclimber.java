/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
 *
 */
package arcadeflex.v036.vidhrdw;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class cclimber {

    static final int BIGSPRITE_WIDTH = 128;
    static final int BIGSPRITE_HEIGHT = 128;
    public static UBytePtr cclimber_bsvideoram = new UBytePtr();
    public static int[] cclimber_bsvideoram_size = new int[1];
    public static UBytePtr cclimber_bigspriteram = new UBytePtr();
    public static UBytePtr cclimber_column_scroll = new UBytePtr();
    static char bsdirtybuffer[];
    static osd_bitmap bsbitmap;
    static int[] flipscreen = new int[2];
    static int palettebank;
    static int sidepanel_enabled;
    static int bgpen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Crazy Climber has three 32x8 palette PROMs. The palette PROMs are
     * connected to the RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    public static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr cclimber_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + (offs)])

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

            /* character and sprite lookup table */
 /* they use colors 0-63 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                /* pen 0 always uses color 0 (background in River Patrol and Silver Land) */
                if ((i % 4) == 0) {
                    //COLOR(0,i) = 0;
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = 0;
                } else {
                    // COLOR(0,i) = i;
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;
                }
            }

            /* big sprite lookup table */
 /* it uses colors 64-95 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                if (i % 4 == 0) {
                    //COLOR(2,i) = 0;
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = 0;
                } else {
                    //COLOR(2,i) = i + 64;
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (i + 64);
                }
            }

            bgpen = 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Swimmer has two 256x4 char/sprite palette PROMs and one 32x8 big sprite
     * palette PROM. The palette PROMs are connected to the RGB output this way:
     * (the 500 and 250 ohm resistors are made of 1 kohm resistors in parallel)
     *
     * bit 3 -- 250 ohm resistor -- BLUE -- 500 ohm resistor -- BLUE -- 250 ohm
     * resistor -- GREEN bit 0 -- 500 ohm resistor -- GREEN bit 3 -- 1 kohm
     * resistor -- GREEN -- 250 ohm resistor -- RED -- 500 ohm resistor -- RED
     * bit 0 -- 1 kohm resistor -- RED
     *
     * bit 7 -- 250 ohm resistor -- BLUE -- 500 ohm resistor -- BLUE -- 250 ohm
     * resistor -- GREEN -- 500 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 250 ohm resistor -- RED -- 500 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     * Additionally, the background color of the score panel is determined by
     * these resistors:
     *
     * /--- tri-state -- 470 -- BLUE +5V -- 1kohm ------- tri-state -- 390 --
     * GREEN \--- tri-state -- 1000 -- RED
     *
     **************************************************************************
     */
    public static final int BGPEN = (256 + 32);
    public static final int SIDEPEN = (256 + 32 + 1);

    public static VhConvertColorPromPtr swimmer_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + (offs)])

            int p_inc = 0;
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(i) >> 0) & 0x01;
                bit1 = (color_prom.read(i) >> 1) & 0x01;
                bit2 = (color_prom.read(i) >> 2) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);
                /* green component */
                bit0 = (color_prom.read(i) >> 3) & 0x01;
                bit1 = (color_prom.read(i + 256) >> 0) & 0x01;
                bit2 = (color_prom.read(i + 256) >> 1) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(i + 256) >> 2) & 0x01;
                bit2 = (color_prom.read(i + 256) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);

                /* side panel */
                if ((i % 8) != 0) {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;//COLOR(0,i) = i;
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i + 256] = (char) i;//COLOR(0,i+256) = i;
                } else {
                    /* background */
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) BGPEN;//COLOR(0,i) = BGPEN;
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i + 256] = (char) SIDEPEN;//COLOR(0,i+256) = SIDEPEN;
                }
            }

            color_prom.inc(2 * 256);//color_prom += 2 * 256;

            /* big sprite */
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(i) >> 0) & 0x01;
                bit1 = (color_prom.read(i) >> 1) & 0x01;
                bit2 = (color_prom.read(i) >> 2) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);
                /* green component */
                bit0 = (color_prom.read(i) >> 3) & 0x01;
                bit1 = (color_prom.read(i) >> 4) & 0x01;
                bit2 = (color_prom.read(i) >> 5) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(i) >> 6) & 0x01;
                bit2 = (color_prom.read(i) >> 7) & 0x01;
                palette[p_inc++] = (char) (0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2);

                if (i % 8 == 0) {
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) BGPEN;//COLOR(2,i) = BGPEN;  /* enforce transparency */
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (i + 256);//COLOR(2,i) = i+256;
                }
            }

            /* background */
            palette[p_inc++] = (char) (0);
            palette[p_inc++] = (char) (0);
            palette[p_inc++] = (char) (0);
            /* side panel background color */
            palette[p_inc++] = (char) (0x24);
            palette[p_inc++] = (char) (0x5d);
            palette[p_inc++] = (char) (0x4e);

            palette_transparent_color = BGPEN;
            /* background color */

            bgpen = BGPEN;
        }
    };

    /**
     * *************************************************************************
     *
     * Swimmer can directly set the background color. The latch is connected to
     * the RGB output this way: (the 500 and 250 ohm resistors are made of 1
     * kohm resistors in parallel)
     *
     * bit 7 -- 250 ohm resistor -- RED -- 500 ohm resistor -- RED -- 250 ohm
     * resistor -- GREEN -- 500 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 250 ohm resistor -- BLUE -- 500 ohm resistor -- BLUE bit 0 -- 1
     * kohm resistor -- BLUE
     *
     **************************************************************************
     */
    public static WriteHandlerPtr swimmer_bgcolor_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bit0, bit1, bit2;
            int r, g, b;

            /* red component */
            bit0 = 0;
            bit1 = (data >> 6) & 0x01;
            bit2 = (data >> 7) & 0x01;
            r = 0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2;

            /* green component */
            bit0 = (data >> 3) & 0x01;
            bit1 = (data >> 4) & 0x01;
            bit2 = (data >> 5) & 0x01;
            g = 0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2;

            /* blue component */
            bit0 = (data >> 0) & 0x01;
            bit1 = (data >> 1) & 0x01;
            bit2 = (data >> 2) & 0x01;
            b = 0x20 * bit0 + 0x40 * bit1 + 0x80 * bit2;

            palette_change_color(BGPEN, r, g, b);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr cclimber_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((bsdirtybuffer = new char[cclimber_bsvideoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(bsdirtybuffer, 1, cclimber_bsvideoram_size[0]);

            if ((bsbitmap = osd_create_bitmap(BIGSPRITE_WIDTH, BIGSPRITE_HEIGHT)) == null) {
                bsdirtybuffer = null;
                generic_vh_stop.handler();
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
    public static VhStopPtr cclimber_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(bsbitmap);
            bsdirtybuffer = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr cclimber_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen[offset] != (data & 1)) {
                flipscreen[offset] = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr cclimber_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (colorram.read(offset) != data) {
                /* bit 5 of the address is not used for color memory. There is just */
 /* 512 bytes of memory; every two consecutive rows share the same memory */
 /* region. */
                offset &= 0xffdf;

                dirtybuffer[offset] = 1;
                dirtybuffer[offset + 0x20] = 1;

                colorram.write(offset, data);
                colorram.write(offset + 0x20, data);
            }
        }
    };

    public static WriteHandlerPtr cclimber_bigsprite_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (cclimber_bsvideoram.read(offset) != data) {
                bsdirtybuffer[offset] = 1;

                cclimber_bsvideoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr swimmer_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palettebank != (data & 1)) {
                palettebank = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr swimmer_sidepanel_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data != sidepanel_enabled) {
                sidepanel_enabled = data;

                /* We only need to dirty the side panel, but this location is not */
 /* written to very often, so we just dirty the whole screen */
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
    static void drawbigsprite(osd_bitmap bitmap) {
        int sx, sy, flipx, flipy;

        sx = 136 - cclimber_bigspriteram.read(3);
        sy = 128 - cclimber_bigspriteram.read(2);
        flipx = cclimber_bigspriteram.read(1) & 0x10;
        flipy = cclimber_bigspriteram.read(1) & 0x20;
        if (flipscreen[1] != 0) /* only the Y direction has to be flipped */ {
            sy = 128 - sy;
            flipy = NOT(flipy);
        }

        /* we have to draw if four times for wraparound */
        sx &= 0xff;
        sy &= 0xff;
        copybitmap(bitmap, bsbitmap,
                flipx, flipy,
                sx, sy,
                Machine.drv.visible_area, TRANSPARENCY_COLOR, bgpen);
        copybitmap(bitmap, bsbitmap,
                flipx, flipy,
                sx - 256, sy,
                Machine.drv.visible_area, TRANSPARENCY_COLOR, bgpen);
        copybitmap(bitmap, bsbitmap,
                flipx, flipy,
                sx - 256, sy - 256,
                Machine.drv.visible_area, TRANSPARENCY_COLOR, bgpen);
        copybitmap(bitmap, bsbitmap,
                flipx, flipy,
                sx, sy - 256,
                Machine.drv.visible_area, TRANSPARENCY_COLOR, bgpen);
    }

    static int lastcol_cc;
    public static VhUpdatePtr cclimber_vh_screenrefresh = new VhUpdatePtr() {
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
                    /* vertical flipping flips two adjacent characters */
                    if (flipy != 0) {
                        sy ^= 1;
                    }

                    if (flipscreen[0] != 0) {
                        sx = 31 - sx;
                        flipx = NOT(flipx);
                    }
                    if (flipscreen[1] != 0) {
                        sy = 31 - sy;
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[(colorram.read(offs) & 0x10) != 0 ? 1 : 0],
                            videoram.read(offs) + 8 * (colorram.read(offs) & 0x20),
                            colorram.read(offs) & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                if (flipscreen[0] != 0) {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[offs] = -cclimber_column_scroll.read(31 - offs);
                        if (flipscreen[1] != 0) {
                            scroll[offs] = -scroll[offs];
                        }
                    }
                } else {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[offs] = -cclimber_column_scroll.read(offs);
                        if (flipscreen[1] != 0) {
                            scroll[offs] = -scroll[offs];
                        }
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* update the "big sprite" */
            {
                int newcol;

                newcol = cclimber_bigspriteram.read(1) & 0x07;

                for (offs = cclimber_bsvideoram_size[0] - 1; offs >= 0; offs--) {
                    int sx, sy;

                    if (bsdirtybuffer[offs] != 0 || newcol != lastcol_cc) {
                        bsdirtybuffer[offs] = 0;

                        sx = offs % 16;
                        sy = offs / 16;

                        drawgfx(bsbitmap, Machine.gfx[2],
                                cclimber_bsvideoram.read(offs), newcol,
                                0, 0,
                                8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    }

                }

                lastcol_cc = newcol;
            }

            if ((cclimber_bigspriteram.read(0) & 1) != 0) /* draw the "big sprite" below sprites */ {
                drawbigsprite(bitmap);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = 240 - spriteram.read(offs + 2);
                flipx = spriteram.read(offs) & 0x40;
                flipy = spriteram.read(offs) & 0x80;
                if (flipscreen[0] != 0) {
                    sx = 240 - sx;
                    flipx = NOT(flipx);
                }
                if (flipscreen[1] != 0) {
                    sy = 240 - sy;
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[(spriteram.read(offs + 1) & 0x10) != 0 ? 4 : 3],
                        (spriteram.read(offs) & 0x3f) + 2 * (spriteram.read(offs + 1) & 0x20),
                        spriteram.read(offs + 1) & 0x0f,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            if ((cclimber_bigspriteram.read(0) & 1) == 0) /* draw the "big sprite" over sprites */ {
                drawbigsprite(bitmap);
            }
        }
    };

    static int lastcol_swimmer;
    public static VhUpdatePtr swimmer_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(bsdirtybuffer, 1, cclimber_bsvideoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy, color;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    /* vertical flipping flips two adjacent characters */
                    if (flipy != 0) {
                        sy ^= 1;
                    }

                    if (flipscreen[0] != 0) {
                        sx = 31 - sx;
                        flipx = NOT(flipx);
                    }
                    if (flipscreen[1] != 0) {
                        sy = 31 - sy;
                        flipy = NOT(flipy);
                    }

                    color = (colorram.read(offs) & 0x0f) + 0x10 * palettebank;
                    if (sx >= 24 && sidepanel_enabled != 0) {
                        color += 32;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x10) << 4),
                            color,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                if (flipscreen[1] != 0) {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[offs] = cclimber_column_scroll.read(31 - offs);
                    }
                } else {
                    for (offs = 0; offs < 32; offs++) {
                        scroll[offs] = -cclimber_column_scroll.read(offs);
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* update the "big sprite" */
            {
                int newcol;

                newcol = cclimber_bigspriteram.read(1) & 0x03;

                for (offs = cclimber_bsvideoram_size[0] - 1; offs >= 0; offs--) {
                    int sx, sy;

                    if (bsdirtybuffer[offs] != 0 || newcol != lastcol_swimmer) {
                        bsdirtybuffer[offs] = 0;

                        sx = offs % 16;
                        sy = offs / 16;

                        drawgfx(bsbitmap, Machine.gfx[2],
                                cclimber_bsvideoram.read(offs) + ((cclimber_bigspriteram.read(1) & 0x08) << 5),
                                newcol,
                                0, 0,
                                8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    }

                }

                lastcol_swimmer = newcol;
            }

            if ((cclimber_bigspriteram.read(0) & 1) != 0) /* draw the "big sprite" below sprites */ {
                drawbigsprite(bitmap);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = 240 - spriteram.read(offs + 2);
                flipx = spriteram.read(offs) & 0x40;
                flipy = spriteram.read(offs) & 0x80;
                if (flipscreen[0] != 0) {
                    sx = 240 - sx;
                    flipx = NOT(flipx);
                }
                if (flipscreen[1] != 0) {
                    sy = 240 - sy;
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram.read(offs) & 0x3f) | (spriteram.read(offs + 1) & 0x10) << 2,
                        (spriteram.read(offs + 1) & 0x0f) + 0x10 * palettebank,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            if ((cclimber_bigspriteram.read(0) & 1) == 0) /* draw the "big sprite" over sprites */ {
                drawbigsprite(bitmap);
            }
        }
    };
}
