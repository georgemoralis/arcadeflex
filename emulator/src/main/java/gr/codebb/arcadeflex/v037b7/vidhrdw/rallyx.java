/**
 * ported to v0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memset;
import static common.libc.expressions.NOT;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_alloc;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_free;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class rallyx {

    public static UBytePtr rallyx_videoram2 = new UBytePtr();
    public static UBytePtr rallyx_colorram2 = new UBytePtr();
    public static UBytePtr rallyx_radarx = new UBytePtr();
    public static UBytePtr rallyx_radary = new UBytePtr();
    public static UBytePtr rallyx_radarattr = new UBytePtr();
    public static int[] rallyx_radarram_size = new int[1];
    public static UBytePtr rallyx_scrollx = new UBytePtr();
    public static UBytePtr rallyx_scrolly = new UBytePtr();
    static char dirtybuffer2[];
    /* keep track of modified portions of the screen */
 /* to speed up video refresh */

    static osd_bitmap tmpbitmap1;
    static int flipscreen;

    static rectangle spritevisiblearea = new rectangle(
            0 * 8, 28 * 8 - 1,
            0 * 8, 28 * 8 - 1
    );

    static rectangle spritevisibleareaflip = new rectangle(
            8 * 8, 36 * 8 - 1,
            0 * 8, 28 * 8 - 1
    );

    static rectangle radarvisiblearea = new rectangle(
            28 * 8, 36 * 8 - 1,
            0 * 8, 28 * 8 - 1
    );

    static rectangle radarvisibleareaflip = new rectangle(
            0 * 8, 8 * 8 - 1,
            0 * 8, 28 * 8 - 1
    );

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Rally X has one 32x8 palette PROM and one 256x4 color lookup table PROM.
     * The palette PROM is connected to the RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     * In Rally-X there is a 1 kohm pull-down on B only, in Locomotion the 1
     * kohm pull-down is an all three RGB outputs.
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr rallyx_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
 /* character lookup table */
 /* sprites use the same color lookup table as characters */
 /* characters use colors 0-15 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }

            /* radar dots lookup table */
 /* they use colors 16-19 */
            for (i = 0; i < 4; i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (16 + i);
            }

        }
    };

    public static VhConvertColorPromPtr locomotn_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = (char) (0x50 * bit0 + 0xab * bit1);

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
 /* character lookup table */
 /* sprites use the same color lookup table as characters */
 /* characters use colors 0-15 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }

            /* radar dots lookup table */
 /* they use colors 16-19 */
            for (i = 0; i < 4; i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (16 + i);
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
    public static VhStartPtr rallyx_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            if ((tmpbitmap1 = bitmap_alloc(32 * 8, 32 * 8)) == null) {
                dirtybuffer2 = null;
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
    public static VhStopPtr rallyx_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap1);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr rallyx_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (rallyx_videoram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                rallyx_videoram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr rallyx_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (rallyx_colorram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                rallyx_colorram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr rallyx_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
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
    public static VhUpdatePtr rallyx_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;
            int scrollx, scrolly;
            int displacement = 1;

            if (flipscreen != 0) {
                scrollx = (rallyx_scrollx.read() - displacement) + 32;
                scrolly = (rallyx_scrolly.read() + 16) - 32;
            } else {
                scrollx = -(rallyx_scrollx.read() - 3 * displacement);
                scrolly = -(rallyx_scrolly.read() + 16);
            }

            /* draw the below sprite priority characters */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if ((rallyx_colorram2.read(offs) & 0x20) != 0) {
                    continue;
                }

                if (dirtybuffer2[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = ~rallyx_colorram2.read(offs) & 0x40;
                    flipy = rallyx_colorram2.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            rallyx_videoram2.read(offs),
                            rallyx_colorram2.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* update radar */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = (offs % 32) ^ 4;
                    sy = offs / 32 - 2;
                    flipx = ~colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 7 - sx;
                        sy = 27 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs),
                            colorram.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            radarvisibleareaflip, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                sx = spriteram.read(offs + 1) + ((spriteram_2.read(offs + 1) & 0x80) << 1) - displacement;
                sy = 225 - spriteram_2.read(offs) - displacement;

                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram.read(offs) & 0xfc) >> 2,
                        spriteram_2.read(offs + 1) & 0x3f,
                        spriteram.read(offs) & 1, spriteram.read(offs) & 2,
                        sx, sy,
                        flipscreen != 0 ? spritevisibleareaflip : spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }

            /* draw the above sprite priority characters */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int flipx, flipy;

                if ((rallyx_colorram2.read(offs) & 0x20) == 0) {
                    continue;
                }

                sx = offs % 32;
                sy = offs / 32;
                flipx = ~rallyx_colorram2.read(offs) & 0x40;
                flipy = rallyx_colorram2.read(offs) & 0x80;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[0],
                        rallyx_videoram2.read(offs),
                        rallyx_colorram2.read(offs) & 0x3f,
                        flipx, flipy,
                        (8 * sx + scrollx) & 0xff, (8 * sy + scrolly) & 0xff,
                        null, TRANSPARENCY_NONE, 0);
                drawgfx(bitmap, Machine.gfx[0],
                        rallyx_videoram2.read(offs),
                        rallyx_colorram2.read(offs) & 0x3f,
                        flipx, flipy,
                        ((8 * sx + scrollx) & 0xff) - 256, (8 * sy + scrolly) & 0xff,
                        null, TRANSPARENCY_NONE, 0);
            }

            /* radar */
            if (flipscreen != 0) {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, radarvisibleareaflip, TRANSPARENCY_NONE, 0);
            } else {
                copybitmap(bitmap, tmpbitmap, 0, 0, 28 * 8, 0, radarvisiblearea, TRANSPARENCY_NONE, 0);
            }

            /* draw the cars on the radar */
            for (offs = 0; offs < rallyx_radarram_size[0]; offs++) {
                int x, y;

                x = rallyx_radarx.read(offs) + ((~rallyx_radarattr.read(offs) & 0x08) << 5);
                y = 237 - rallyx_radary.read(offs);
                if (flipscreen != 0) {
                    x -= 1;
                    y += 2;
                }

                drawgfx(bitmap, Machine.gfx[2],
                        ((rallyx_radarattr.read(offs) & 0x0e) >> 1) ^ 0x07,
                        0,
                        flipscreen, flipscreen,
                        x, y,
                        Machine.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };

    public static VhUpdatePtr jungler_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;
            int scrollx, scrolly;
            int displacement = 0;

            if (flipscreen != 0) {
                scrollx = (rallyx_scrollx.read() - displacement) + 32;
                scrolly = (rallyx_scrolly.read() + 16) - 32;
            } else {
                scrollx = -(rallyx_scrollx.read() - 3 * displacement);
                scrolly = -(rallyx_scrolly.read() + 16);
            }

            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer2[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = ~rallyx_colorram2.read(offs) & 0x40;
                    flipy = rallyx_colorram2.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            rallyx_videoram2.read(offs),
                            rallyx_colorram2.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* update radar */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = (offs % 32) ^ 4;
                    sy = offs / 32 - 2;
                    flipx = ~colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 7 - sx;
                        sy = 27 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs),
                            colorram.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            radarvisibleareaflip, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                sx = spriteram.read(offs + 1) + ((spriteram_2.read(offs + 1) & 0x80) << 1) - displacement;
                sy = 225 - spriteram_2.read(offs) - displacement;

                drawgfx(bitmap, Machine.gfx[1],
                        (spriteram.read(offs) & 0xfc) >> 2,
                        spriteram_2.read(offs + 1) & 0x3f,
                        spriteram.read(offs) & 1, spriteram.read(offs) & 2,
                        sx, sy,
                        flipscreen != 0 ? spritevisibleareaflip : spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }

            /* radar */
            if (flipscreen != 0) {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, radarvisibleareaflip, TRANSPARENCY_NONE, 0);
            } else {
                copybitmap(bitmap, tmpbitmap, 0, 0, 28 * 8, 0, radarvisiblearea, TRANSPARENCY_NONE, 0);
            }

            /* draw the cars on the radar */
            for (offs = 0; offs < rallyx_radarram_size[0]; offs++) {
                int x, y;

                x = rallyx_radarx.read(offs) + ((~rallyx_radarattr.read(offs) & 0x08) << 5) + 1;
                y = 238 - rallyx_radary.read(offs);

                drawgfx(bitmap, Machine.gfx[2],
                        (rallyx_radarattr.read(offs) & 0x07) ^ 0x07,
                        0,
                        flipscreen, flipscreen,
                        x, y,
                        Machine.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };

    public static VhUpdatePtr locomotn_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer2[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    /* not a mistake, one bit selects both  flips */
                    flipx = rallyx_colorram2.read(offs) & 0x80;
                    flipy = rallyx_colorram2.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            (rallyx_videoram2.read(offs) & 0x7f) + 2 * (rallyx_colorram2.read(offs) & 0x40) + 2 * (rallyx_videoram2.read(offs) & 0x80),
                            rallyx_colorram2.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* update radar */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = (offs % 32) ^ 4;
                    sy = offs / 32 - 2;
                    /* not a mistake, one bit selects both  flips */
                    flipx = colorram.read(offs) & 0x80;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 7 - sx;
                        sy = 27 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            (videoram.read(offs) & 0x7f) + 2 * (colorram.read(offs) & 0x40) + 2 * (videoram.read(offs) & 0x80),
                            colorram.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            radarvisibleareaflip, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                if (flipscreen != 0) {
                    scrollx = (rallyx_scrollx.read()) + 32;
                    scrolly = (rallyx_scrolly.read() + 16) - 32;
                } else {
                    scrollx = -(rallyx_scrollx.read());
                    scrolly = -(rallyx_scrolly.read() + 16);
                }

                copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* radar */
            if (flipscreen != 0) {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, radarvisibleareaflip, TRANSPARENCY_NONE, 0);
            } else {
                copybitmap(bitmap, tmpbitmap, 0, 0, 28 * 8, 0, radarvisiblearea, TRANSPARENCY_NONE, 0);
            }

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                sx = spriteram.read(offs + 1) - 1;
                sy = 224 - spriteram_2.read(offs);
                if (flipscreen != 0) {
                    sx += 32;
                }

                drawgfx(bitmap, Machine.gfx[1],
                        ((spriteram.read(offs) & 0x7c) >> 2) + 0x20 * (spriteram.read(offs) & 0x01) + ((spriteram.read(offs) & 0x80) >> 1),
                        spriteram_2.read(offs + 1) & 0x3f,
                        NOT(flipscreen), NOT(flipscreen),
                        sx, sy,
                        flipscreen != 0 ? spritevisibleareaflip : spritevisiblearea, TRANSPARENCY_COLOR, 0);
            }

            /* draw the cars on the radar */
            for (offs = 0; offs < rallyx_radarram_size[0]; offs++) {
                int x, y;

                /* it looks like the addresses used are
                 a000-a003  a004-a00f
                 8020-8023  8034-803f
                 8820-8823  8834-883f
                 so 8024-8033 and 8824-8833 are not used
                 */
                x = rallyx_radarx.read(offs) + ((~rallyx_radarattr.read(offs & 0x0f) & 0x08) << 5);
                if (flipscreen != 0) {
                    x += 32;
                }
                y = 237 - rallyx_radary.read(offs);

                drawgfx(bitmap, Machine.gfx[2],
                        (rallyx_radarattr.read(offs & 0x0f) & 0x07) ^ 0x07,
                        0,
                        flipscreen, flipscreen,
                        x, y,
                        //				&Machine.visible_area,TRANSPARENCY_PEN,3);
                        flipscreen != 0 ? spritevisibleareaflip : spritevisiblearea, TRANSPARENCY_PEN, 3);
            }
        }
    };

    public static VhUpdatePtr commsega_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer2[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    /* not a mistake, one bit selects both  flips */
                    flipx = rallyx_colorram2.read(offs) & 0x80;
                    flipy = rallyx_colorram2.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            (rallyx_videoram2.read(offs) & 0x7f) + 2 * (rallyx_colorram2.read(offs) & 0x40) + 2 * (rallyx_videoram2.read(offs) & 0x80),
                            rallyx_colorram2.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* update radar */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = (offs % 32) ^ 4;
                    sy = offs / 32 - 2;
                    /* not a mistake, one bit selects both  flips */
                    flipx = colorram.read(offs) & 0x80;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 7 - sx;
                        sy = 27 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            (videoram.read(offs) & 0x7f) + 2 * (colorram.read(offs) & 0x40) + 2 * (videoram.read(offs) & 0x80),
                            colorram.read(offs) & 0x3f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            radarvisibleareaflip, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                if (flipscreen != 0) {
                    scrollx = (rallyx_scrollx.read()) + 32;
                    scrolly = (rallyx_scrolly.read() + 16) - 32;
                } else {
                    scrollx = -(rallyx_scrollx.read());
                    scrolly = -(rallyx_scrolly.read() + 16);
                }

                copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* radar */
            if (flipscreen != 0) {
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, radarvisibleareaflip, TRANSPARENCY_NONE, 0);
            } else {
                copybitmap(bitmap, tmpbitmap, 0, 0, 28 * 8, 0, radarvisiblearea, TRANSPARENCY_NONE, 0);
            }

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 2) {
                int flipx, flipy;

                sx = spriteram.read(offs + 1) - 1;
                sy = 224 - spriteram_2.read(offs);
                if (flipscreen != 0) {
                    sx += 32;
                }
                flipx = ~spriteram.read(offs) & 1;
                flipy = ~spriteram.read(offs) & 2;
                if (flipscreen != 0) {
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                if ((spriteram.read(offs) & 0x01) != 0) /* ??? */ {
                    drawgfx(bitmap, Machine.gfx[1],
                            ((spriteram.read(offs) & 0x7c) >> 2) + 0x20 * (spriteram.read(offs) & 0x01) + ((spriteram.read(offs) & 0x80) >> 1),
                            spriteram_2.read(offs + 1) & 0x3f,
                            flipx, flipy,
                            sx, sy,
                            Machine.visible_area, TRANSPARENCY_COLOR, 0);
                }
            }

            /* draw the cars on the radar */
            for (offs = 0; offs < rallyx_radarram_size[0]; offs++) {
                int x, y;

                /* it looks like the addresses used are
                 a000-a003  a004-a00f
                 8020-8023  8034-803f
                 8820-8823  8834-883f
                 so 8024-8033 and 8824-8833 are not used
                 */
                x = rallyx_radarx.read(offs) + ((~rallyx_radarattr.read(offs & 0x0f) & 0x08) << 5);
                if (flipscreen != 0) {
                    x += 32;
                }
                y = 237 - rallyx_radary.read(offs);

                drawgfx(bitmap, Machine.gfx[2],
                        (rallyx_radarattr.read(offs & 0x0f) & 0x07) ^ 0x07,
                        0,
                        flipscreen, flipscreen,
                        x, y,
                        Machine.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };
}
