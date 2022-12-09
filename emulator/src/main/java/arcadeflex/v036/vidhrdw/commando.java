/*
 * ported to v0.36
 * using automatic conversion tool v0.08
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
import static common.libc.expressions.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class commando {

    public static UBytePtr commando_bgvideoram = new UBytePtr();
    public static UBytePtr commando_bgcolorram = new UBytePtr();
    public static int[] commando_bgvideoram_size = new int[1];
    public static UBytePtr commando_scrollx = new UBytePtr();
    public static UBytePtr commando_scrolly = new UBytePtr();
    public static char[] dirtybuffer2;
    public static osd_bitmap tmpbitmap2;
    public static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Commando has three 256x4 palette PROMs (one per gun), connected to the
     * RGB output this way:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr commando_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(i) >> 0) & 0x01;
                bit1 = (color_prom.read(i) >> 1) & 0x01;
                bit2 = (color_prom.read(i) >> 2) & 0x01;
                bit3 = (color_prom.read(i) >> 3) & 0x01;
                palette[3 * i] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(i + Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(i + Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(i + Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(i + Machine.drv.total_colors) >> 3) & 0x01;
                palette[3 * i + 1] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(i + 2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[3 * i + 2] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
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
    public static VhStartPtr commando_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[commando_bgvideoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, commando_bgvideoram_size[0]);

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            /* the background area is twice as tall and twice as large as the screen */
            if ((tmpbitmap2 = osd_create_bitmap(2 * Machine.drv.screen_width, 2 * Machine.drv.screen_height)) == null) {
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
    public static VhStopPtr commando_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr commando_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (commando_bgvideoram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                commando_bgvideoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr commando_bgcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (commando_bgcolorram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                commando_bgcolorram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr commando_spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*if (errorlog && data != spriteram[offset] && offset % 4 == 2)
             fprintf(errorlog,"%04x: sprite %d X offset (old = %d new = %d) scanline %d\n",
             cpu_get_pc(),offset/4,spriteram[offset],data,255 - (cpu_getfcount() * 256 / cpu_getfperiod()));
             if (errorlog && data != spriteram[offset] && offset % 4 == 3)
             fprintf(errorlog,"%04x: sprite %d Y offset (old = %d new = %d) scanline %d\n",
             cpu_get_pc(),offset/4,spriteram[offset],data,255 - (cpu_getfcount() * 256 / cpu_getfperiod()));
             if (errorlog && data != spriteram[offset] && offset % 4 == 0)
             fprintf(errorlog,"%04x: sprite %d code (old = %d new = %d) scanline %d\n",
             cpu_get_pc(),offset/4,spriteram[offset],data,255 - (cpu_getfcount() * 256 / cpu_getfperiod()));
             */
            spriteram.write(offset, data);
        }
    };

    public static WriteHandlerPtr commando_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are for coin counters - we ignore them */

 /* bit 4 resets the sound CPU - we ignore it */
 /* bit 7 flips screen */
            if (flipscreen != (~data & 0x80)) {
                flipscreen = ~data & 0x80;
                memset(dirtybuffer2, 1, commando_bgvideoram_size[0]);
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
    public static VhUpdatePtr commando_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            for (offs = commando_bgvideoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer2[offs] != 0) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer2[offs] = 0;

                    sx = offs / 32;
                    sy = offs % 32;
                    flipx = commando_bgcolorram.read(offs) & 0x10;
                    flipy = commando_bgcolorram.read(offs) & 0x20;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            commando_bgvideoram.read(offs) + 4 * (commando_bgcolorram.read(offs) & 0xc0),
                            commando_bgcolorram.read(offs) & 0x0f,
                            flipx, flipy,
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the background graphics */
            {
                int scrollx, scrolly;

                scrollx = -(commando_scrolly.read(0) + 256 * commando_scrolly.read(1));
                scrolly = -(commando_scrollx.read(0) + 256 * commando_scrollx.read(1));
                if (flipscreen != 0) {
                    scrollx = 256 - scrollx;
                    scrolly = 256 - scrolly;
                }

                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy, bank;

                /* bit 1 of [offs+1] is not used */
                sx = spriteram.read(offs + 3) - 0x100 * (spriteram.read(offs + 1) & 0x01);
                sy = spriteram.read(offs + 2);
                flipx = spriteram.read(offs + 1) & 0x04;
                flipy = spriteram.read(offs + 1) & 0x08;
                bank = (spriteram.read(offs + 1) & 0xc0) >> 6;

                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                if (bank < 3) {
                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs) + 256 * bank,
                            (spriteram.read(offs + 1) & 0x30) >> 4,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy, flipx, flipy;

                sx = offs % 32;
                sy = offs / 32;
                flipx = colorram.read(offs) & 0x10;
                flipy = colorram.read(offs) & 0x20;

                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0),
                        colorram.read(offs) & 0x0f,
                        flipx, flipy,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };
}
