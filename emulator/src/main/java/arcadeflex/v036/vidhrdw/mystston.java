/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class mystston {

    public static UBytePtr mystston_videoram2 = new UBytePtr();
    public static UBytePtr mystston_colorram2 = new UBytePtr();
    public static int[] mystston_videoram2_size = new int[1];
    public static UBytePtr mystston_scroll = new UBytePtr();
    static int textcolor;
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Mysterious Stones has both palette RAM and a PROM. The PROM is used for
     * text.
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr mystston_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;

            p_inc += 3 * 24;//palette += 3*24;	/* first 24 colors are RAM */

            for (i = 0; i < 32; i++) {
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
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr mystston_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            /* Mysterious Stones has a virtual screen twice as large as the visible screen */
            if ((tmpbitmap = osd_create_bitmap(Machine.drv.screen_width, 2 * Machine.drv.screen_height)) == null) {
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
    public static VhStopHandlerPtr mystston_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
        }
    };

    public static WriteHandlerPtr mystston_2000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are text color */
            textcolor = ((data & 0x01) << 1) | ((data & 0x02) >> 1);

            /* bits 4 and 5 are coin counters */
            coin_counter_w.handler(0, data & 0x10);
            coin_counter_w.handler(1, data & 0x20);

            /* bit 7 is screen flip */
            if (flipscreen != (data & 0x80)) {
                flipscreen = data & 0x80;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* other bits unused? */
            if (errorlog != null) {
                fprintf(errorlog, "PC %04x: 2000 = %02x\n", cpu_get_pc(), data);
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
    public static VhUpdateHandlerPtr mystston_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipy;

                    dirtybuffer[offs] = 0;

                    sx = 15 - offs / 32;
                    sy = offs % 32;
                    flipy = (sy >= 16) ? 1 : 0;
                    /* flip horizontally tiles on the right half of the bitmap */
                    if (flipscreen != 0) {
                        sx = 15 - sx;
                        sy = 31 - sy;
                        flipy = NOT(flipy);
                    }
                    drawgfx(tmpbitmap, Machine.gfx[1],
                            videoram.read(offs) + 256 * (colorram.read(offs) & 0x01),
                            0,
                            flipscreen, flipy,
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrolly;

                scrolly = -mystston_scroll.read();
                if (flipscreen != 0) {
                    scrolly = 256 - scrolly;
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                if ((spriteram.read(offs) & 0x01) != 0) {
                    int sx, sy, flipx, flipy;

                    sx = 240 - spriteram.read(offs + 3);
                    sy = (240 - spriteram.read(offs + 2)) & 0xff;
                    flipx = spriteram.read(offs) & 0x04;
                    flipy = spriteram.read(offs) & 0x02;
                    if (flipscreen != 0) {
                        sx = 240 - sx;
                        sy = 240 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs + 1) + ((spriteram.read(offs) & 0x10) << 4),
                            (spriteram.read(offs) & 0x08) >> 3,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = mystston_videoram2_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = 31 - offs / 32;
                sy = offs % 32;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                }

                drawgfx(bitmap, Machine.gfx[0],
                        mystston_videoram2.read(offs) + 256 * (mystston_colorram2.read(offs) & 0x07),
                        textcolor,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
