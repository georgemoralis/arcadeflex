/**
 * *************************************************************************
 *
 * vidhrdw.c
 *
 * Functions to emulate the video hardware of the machine.
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;

public class rockola {

    public static UBytePtr rockola_videoram2 = new UBytePtr();
    public static UBytePtr rockola_characterram = new UBytePtr();
    public static UBytePtr rockola_scrollx = new UBytePtr();
    public static UBytePtr rockola_scrolly = new UBytePtr();
    static char[] dirtycharacter = new char[256];
    static int flipscreen;
    static int charbank;
    static int backcolor;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Zarzon has a different PROM layout from the others.
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr rockola_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(0) >> 4) & 0x01;
                bit2 = (color_prom.read(0) >> 5) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(0) >> 6) & 0x01;
                bit2 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            backcolor = 0;
            /* background color can be changed by the game */

            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (i);
            }

            for (i = 0; i < TOTAL_COLORS(1); i++) {
                if (i % 4 == 0) {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((4 * backcolor + 0x20));
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((i + 0x20));
                }
            }
        }
    };

    public static VhConvertColorPromPtr satansat_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(0) >> 4) & 0x01;
                bit2 = (color_prom.read(0) >> 5) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(0) >> 6) & 0x01;
                bit2 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            backcolor = 0;
            /* background color can be changed by the game */

            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((4 * (i % 4) + (i / 4)));
            }

            for (i = 0; i < TOTAL_COLORS(1); i++) {
                if (i % 4 == 0) {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((backcolor + 0x10));
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((4 * (i % 4) + (i / 4) + 0x10));
                }
            }
        }
    };

    public static WriteHandlerPtr rockola_characterram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (rockola_characterram.read(offset) != data) {
                dirtycharacter[(offset / 8) & 0xff] = 1;
                rockola_characterram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr rockola_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0-2 select background color */
            if (backcolor != (data & 7)) {
                int i;

                backcolor = data & 7;

                for (i = 0; i < 32; i += 4) {
                    Machine.gfx[1].colortable.write(i, Machine.pens[4 * backcolor + 0x20]);
                }

                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bit 3 selects char bank */
            if (charbank != ((~data & 0x08) >> 3)) {
                charbank = (~data & 0x08) >> 3;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bit 7 flips screen */
            if (flipscreen != (data & 0x80)) {
                flipscreen = data & 0x80;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr satansat_b002_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 flips screen */
            if (flipscreen != (data & 0x01)) {
                flipscreen = data & 0x01;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* bit 1 enables interrups */
 /* it controls only IRQs, not NMIs. Here I am affecting both, which */
 /* is wrong. */
            interrupt_enable_w.handler(0, data & 0x02);

            /* other bits unused */
        }
    };

    public static WriteHandlerPtr satansat_backcolor_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0-1 select background color. Other bits unused. */
            if (backcolor != (data & 3)) {
                int i;

                backcolor = data & 3;

                for (i = 0; i < 16; i += 4) {
                    Machine.gfx[1].colortable.write(i, Machine.pens[backcolor + 0x10]);
                }

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
    public static VhUpdatePtr rockola_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        /* Pioner Balloon has a visible area different from all the others */
                        if (Machine.drv.visible_area.max_y == 28 * 8 - 1) {
                            sy = 27 - sy;
                        } else {
                            sy = 31 - sy;
                        }
                    }

                    drawgfx(tmpbitmap, Machine.gfx[1],
                            videoram.read(offs) + 256 * charbank,
                            (colorram.read(offs) & 0x38) >> 3,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the background graphics */
            {
                int scrollx, scrolly;

                scrollx = -rockola_scrolly.read();
                scrolly = -rockola_scrollx.read();
                if (flipscreen != 0) {
                    scrollx = -scrollx;
                    /* Pioner Balloon has a visible area different from all the others */
                    if (Machine.drv.visible_area.max_y == 28 * 8 - 1) {
                        scrolly = -scrolly - 32;
                    } else {
                        scrolly = -scrolly;
                    }
                }
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int charcode;
                int sx, sy;

                charcode = rockola_videoram2.read(offs);

                /* decode modified characters */
                if (dirtycharacter[charcode] != 0) {
                    decodechar(Machine.gfx[0], charcode, rockola_characterram,
                            Machine.drv.gfxdecodeinfo[0].gfxlayout);
                    dirtycharacter[charcode] = 0;
                }

                sx = offs % 32;
                sy = offs / 32;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    /* Pioner Balloon has a visible area different from all the others */
                    if (Machine.drv.visible_area.max_y == 28 * 8 - 1) {
                        sy = 27 - sy;
                    } else {
                        sy = 31 - sy;
                    }
                }

                drawgfx(bitmap, Machine.gfx[0],
                        charcode,
                        colorram.read(offs) & 0x07,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };

    /* Zarzon's background doesn't scroll, and the color code selection is different. */
    public static VhUpdatePtr satansat_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 27 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[1],
                            videoram.read(offs),
                            (colorram.read(offs) & 0x0c) >> 2,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int charcode;
                int sx, sy;

                charcode = rockola_videoram2.read(offs);

                /* decode modified characters */
                if (dirtycharacter[charcode] != 0) {
                    decodechar(Machine.gfx[0], charcode, rockola_characterram,
                            Machine.drv.gfxdecodeinfo[0].gfxlayout);
                    dirtycharacter[charcode] = 0;
                }

                sx = offs % 32;
                sy = offs / 32;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 27 - sy;
                }

                drawgfx(bitmap, Machine.gfx[0],
                        charcode,
                        colorram.read(offs) & 0x03,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

            }
        }
    };
}
