/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class yard {

    public static UBytePtr yard_scroll_x_low = new UBytePtr();
    public static UBytePtr yard_scroll_x_high = new UBytePtr();
    public static UBytePtr yard_scroll_y_low = new UBytePtr();
    public static UBytePtr yard_score_panel_disabled = new UBytePtr();
    static int flipscreen;
    static osd_bitmap scroll_panel_bitmap;

    public static final int SCROLL_PANEL_WIDTH = (14 * 4);

    public static final int RADAR_PALETTE_BASE = (256 + 16);

    static rectangle panelvisiblearea = new rectangle(
            26 * 8, 32 * 8 - 1,
            1 * 8, 31 * 8 - 1
    );

    static rectangle panelvisibleareaflip = new rectangle(
            0 * 8, 6 * 8 - 1,
            1 * 8, 31 * 8 - 1
    );

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * 10 Yard Fight has two 256x4 character palette PROMs, one 32x8 sprite
     * palette PROM, one 256x4 sprite color lookup table PROM, and two 256x4
     * radar palette PROMs.
     *
     * I don't know for sure how the palette PROMs are connected to the RGB
     * output, but it's probably something like this; note that RED and BLUE are
     * swapped wrt the usual configuration.
     *
     * bit 7 -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE bit 0 -- 1
     * kohm resistor -- BLUE
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr yard_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            /* character palette */
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = 0;
                bit1 = (color_prom.read(256) >> 2) & 0x01;
                bit2 = (color_prom.read(256) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(256) >> 0) & 0x01;
                bit2 = (color_prom.read(256) >> 1) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            color_prom.inc(256);
            /* color_prom now points to the beginning of the sprite palette */

 /* sprite palette */
            for (i = 0; i < 16; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            color_prom.inc(16);
            /* color_prom now points to the beginning of the sprite lookup table */

 /* sprite lookup table */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (256 + ((color_prom.readinc()) & 0x0f));
            }

            /* color_prom now points to the beginning of the radar palette */
 /* radar palette */
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = 0;
                bit1 = (color_prom.read(256) >> 2) & 0x01;
                bit2 = (color_prom.read(256) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(256) >> 0) & 0x01;
                bit2 = (color_prom.read(256) >> 1) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
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
    public static VhStartPtr yard_vh_start = new VhStartPtr() {
        public int handler() {
            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            if ((tmpbitmap = osd_create_bitmap(Machine.drv.screen_width * 2, Machine.drv.screen_height)) == null) {
                dirtybuffer = null;
                return 1;
            }

            if ((scroll_panel_bitmap = osd_create_bitmap(SCROLL_PANEL_WIDTH, Machine.drv.screen_height)) == null) {
                dirtybuffer = null;
                osd_free_bitmap(tmpbitmap);
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
    public static VhStopPtr yard_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            osd_free_bitmap(scroll_panel_bitmap);
        }
    };

    public static WriteHandlerPtr yard_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* screen flip is handled both by software and hardware */
            data ^= ~readinputport(4) & 1;

            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            coin_counter_w.handler(0, data & 0x02);
            coin_counter_w.handler(1, data & 0x20);
        }
    };

    public static WriteHandlerPtr yard_scroll_panel_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int sx, sy, i;

            sx = (offset % 16);
            sy = (offset / 16);

            if (sx < 1 || sx > 14) {
                return;
            }

            sx = 4 * (sx - 1);

            for (i = 0; i < 4; i++) {
                int col;

                col = (data >> i) & 0x11;
                col = ((col >> 3) | col) & 3;

                plot_pixel.handler(scroll_panel_bitmap, sx + i, sy, Machine.pens[RADAR_PALETTE_BASE + (sy & 0xfc) + col]);
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
    public static VhUpdatePtr yard_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0 || dirtybuffer[offs + 1] != 0) {
                    int sx, sy, flipx;

                    dirtybuffer[offs] = 0;
                    dirtybuffer[offs + 1] = 0;

                    sx = (offs / 2) % 32;
                    sy = (offs / 2) / 32;
                    flipx = videoram.read(offs + 1) & 0x20;

                    if (sy >= 32) {
                        sy -= 32;
                        sx += 32;
                    }

                    if (flipscreen != 0) {
                        sx = 63 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((videoram.read(offs + 1) & 0xc0) << 2),
                            videoram.read(offs + 1) & 0x1f,
                            flipx, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scroll_x, scroll_y;

                scroll_x = (yard_scroll_x_high.read() * 0x100) + yard_scroll_x_low.read();

                if (flipscreen != 0) {
                    scroll_x += 256;
                    scroll_y = yard_scroll_y_low.read();
                } else {
                    scroll_x = -scroll_x;
                    scroll_y = -yard_scroll_y_low.read();
                }

                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll_x}, 1, new int[]{scroll_y}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int code1, code2, bank, sx, sy1, sy2, flipx, flipy;

                bank = (spriteram.read(offs + 1) & 0x20) >> 5;
                code1 = spriteram.read(offs + 2) & 0xbf;
                sx = spriteram.read(offs + 3);
                sy1 = 241 - spriteram.read(offs);
                flipx = spriteram.read(offs + 1) & 0x40;
                flipy = spriteram.read(offs + 1) & 0x80;

                if (flipy != 0) {
                    code2 = code1;
                    code1 += 0x40;
                } else {
                    code2 = code1 + 0x40;
                }

                if (flipscreen != 0) {
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                    sx = 240 - sx;
                    sy2 = 224 - sy1;
                    sy1 = sy2 + 0x10;
                } else {
                    sy2 = sy1 + 0x10;
                }

                drawgfx(bitmap, Machine.gfx[1],
                        code1 + 256 * bank,
                        spriteram.read(offs + 1) & 0x1f,
                        flipx, flipy,
                        sx, sy1,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 256);

                drawgfx(bitmap, Machine.gfx[1],
                        code2 + 256 * bank,
                        spriteram.read(offs + 1) & 0x1f,
                        flipx, flipy,
                        sx, sy2,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 256);
            }

            /* draw the static bitmapped area to screen */
            if (yard_score_panel_disabled.read() == 0) {
                int xpos;

                xpos = flipscreen != 0 ? Machine.drv.visible_area.min_x - 8
                        : Machine.drv.visible_area.max_x + 1 - SCROLL_PANEL_WIDTH;

                copybitmap(bitmap, scroll_panel_bitmap, flipscreen, flipscreen,
                        xpos, 0,
                        flipscreen != 0 ? panelvisibleareaflip : panelvisiblearea, TRANSPARENCY_NONE, 0);
            }
        }
    };
}
