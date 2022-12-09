/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memset;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_alloc;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_free;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_mark_dirty;
import static arcadeflex.v036.vidhrdw.generic.*;

public class popeye {

    public static UBytePtr popeye_videoram = new UBytePtr();
    public static int[] popeye_videoram_size = new int[1];
    public static UBytePtr popeye_background_pos = new UBytePtr();
    public static UBytePtr popeye_palette_bank = new UBytePtr();
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Popeye has four color PROMS: - 32x8 char palette - 32x8 background
     * palette - two 256x4 sprite palette
     *
     * The char and sprite PROMs are connected to the RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE (inverted) -- 470 ohm resistor -- BLUE
     * (inverted) -- 220 ohm resistor -- GREEN (inverted) -- 470 ohm resistor --
     * GREEN (inverted) -- 1 kohm resistor -- GREEN (inverted) -- 220 ohm
     * resistor -- RED (inverted) -- 470 ohm resistor -- RED (inverted) bit 0 --
     * 1 kohm resistor -- RED (inverted)
     *
     * The background PROM is connected to the RGB output this way:
     *
     * bit 7 -- 470 ohm resistor -- BLUE (inverted) -- 680 ohm resistor -- BLUE
     * (inverted) -- 470 ohm resistor -- GREEN (inverted) -- 680 ohm resistor --
     * GREEN (inverted) -- 1.2kohm resistor -- GREEN (inverted) -- 470 ohm
     * resistor -- RED (inverted) -- 680 ohm resistor -- RED (inverted) bit 0 --
     * 1.2kohm resistor -- RED (inverted)
     *
     * The bootleg is the same, but the outputs are not inverted.
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr popeye_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            /* background - darker than the others */
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));

                color_prom.inc();
            }

            /* characters */
            for (i = 0; i < 16; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            color_prom.inc(16);
            /* skip unused part of the PROM */

 /* sprites */
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(256) >> 0) & 0x01;
                bit2 = (color_prom.read(256) >> 1) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(256) >> 2) & 0x01;
                bit2 = (color_prom.read(256) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* palette entries 0-31 are directly used by the background */
            int c_ptr = 0;
            for (i = 0; i < 16; i++) /* characters */ {
                colortable[c_ptr++] = 0;
                /* since chars are transparent, the PROM only */
 /* stores the non transparent color */
                colortable[c_ptr++] = (char) (i + 32);
            }
            for (i = 0; i < 256; i++) /* sprites */ {
                colortable[c_ptr++] = (char) (i + 32 + 16);
            }
        }
    };

    public static VhConvertColorPromPtr popeyebl_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            /* background - darker than the others */
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x1c * bit0 + 0x31 * bit1 + 0x47 * bit2));

                color_prom.inc();
            }

            /* characters */
            for (i = 0; i < 16; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            color_prom.inc(16);
            /* skip unused part of the PROM */

 /* sprites */
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(256) >> 0) & 0x01;
                bit2 = (color_prom.read(256) >> 1) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(256) >> 2) & 0x01;
                bit2 = (color_prom.read(256) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* palette entries 0-31 are directly used by the background */
            int c_ptr = 0;
            for (i = 0; i < 16; i++) /* characters */ {
                colortable[c_ptr++] = 0;
                /* since chars are transparent, the PROM only */
 /* stores the non transparent color */
                colortable[c_ptr++] = (char) (i + 32);
            }
            for (i = 0; i < 256; i++) /* sprites */ {
                colortable[c_ptr++] = (char) (i + 32 + 16);
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
    public static VhStartPtr popeye_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[popeye_videoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, popeye_videoram_size[0]);

            if ((tmpbitmap2 = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
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
    public static VhStopPtr popeye_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr popeye_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x80) != 0) /* write to the upper nibble */ {
                if ((popeye_videoram.read(offset) & 0xf0) != ((data << 4) & 0xf0)) {
                    dirtybuffer2[offset] = 1;

                    popeye_videoram.write(offset, (popeye_videoram.read(offset) & 0x0f) | ((data << 4) & 0xf0));
                }
            } else /* write to the lower nibble */ {
                if ((popeye_videoram.read(offset) & 0x0f) != (data & 0x0f)) {
                    dirtybuffer2[offset] = 1;

                    popeye_videoram.write(offset, (popeye_videoram.read(offset) & 0xf0) | (data & 0x0f));
                }
            }
        }
    };

    public static WriteHandlerPtr popeye_palettebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x08) != (popeye_palette_bank.read() & 0x08)) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, popeye_videoram_size[0]);
            }

            popeye_palette_bank.write(data);
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
    static int lastpos[] = {-1, -1};

    public static VhUpdatePtr popeye_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = popeye_videoram_size[0] - 1 - 128; offs >= 128; offs--) {
                if (dirtybuffer2[offs] != 0) {
                    int sx, sy, x, y, colour;

                    dirtybuffer2[offs] = 0;

                    sx = 8 * (offs % 64);
                    sy = 8 * (offs / 64) - 16;

                    if (sx >= Machine.visible_area.min_x
                            && sx + 7 <= Machine.visible_area.max_x
                            && sy >= Machine.visible_area.min_y
                            && sy + 7 <= Machine.visible_area.max_y) {
                        /* this is slow, but the background doesn't change during game */

                        colour = Machine.pens[(popeye_videoram.read(offs) & 0x0f) + 2 * (popeye_palette_bank.read() & 0x08)];
                        for (y = 0; y < 4; y++) {
                            for (x = 0; x < 8; x++) {
                                plot_pixel.handler(tmpbitmap2, sx + x, sy + y, colour);
                            }
                        }

                        colour = Machine.pens[(popeye_videoram.read(offs) >> 4) + 2 * (popeye_palette_bank.read() & 0x08)];
                        for (y = 4; y < 8; y++) {
                            for (x = 0; x < 8; x++) {
                                plot_pixel.handler(tmpbitmap2, sx + x, sy + y, colour);
                            }
                        }
                    }
                }
            }

            {

                if (popeye_background_pos.read(0) != lastpos[0]
                        || popeye_background_pos.read(1) != lastpos[1]) {
                    /* mark the whole screen dirty if we're scrolling */
                    osd_mark_dirty(Machine.visible_area.min_x, Machine.visible_area.min_y,
                            Machine.visible_area.max_x, Machine.visible_area.max_y, 0);
                    lastpos[0] = popeye_background_pos.read(0);
                    lastpos[1] = popeye_background_pos.read(1);
                }
            }

            if (popeye_background_pos.read(0) == 0) /* no background */ {
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    if (dirtybuffer[offs] != 0) {
                        int sx, sy;

                        dirtybuffer[offs] = 0;

                        sx = 16 * (offs % 32);
                        sy = 16 * (offs / 32) - 16;

                        drawgfx(tmpbitmap, Machine.gfx[0],
                                videoram.read(offs), colorram.read(offs),
                                0, 0, sx, sy,
                                Machine.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }

                /* copy the frontmost playfield (should be in front of sprites, but never mind) */
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
            } else {
                /* copy the background graphics */

                int x = 400 - 2 * popeye_background_pos.read(0);
                int y = 2 * (256 - popeye_background_pos.read(1));

                copybitmap(bitmap, tmpbitmap2, 0, 0,
                        x,
                        y,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int code, color;

                /*
			 * offs+3:
			 * bit 7 ?
			 * bit 6 ?
			 * bit 5 ?
			 * bit 4 MSB of sprite code
			 * bit 3 vertical flip
			 * bit 2 sprite bank
			 * bit 1 \ color (with bit 2 as well)
			 * bit 0 /
                 */
                code = (spriteram.read(offs + 2) & 0x7f) + ((spriteram.read(offs + 3) & 0x10) << 3)
                        + ((spriteram.read(offs + 3) & 0x04) << 6);
                color = (spriteram.read(offs + 3) & 0x07) + 8 * (popeye_palette_bank.read() & 0x07);

                if (spriteram.read(offs) != 0) {
                    drawgfx(bitmap, Machine.gfx[1],
                            code ^ 0x1ff,
                            color,
                            spriteram.read(offs + 2) & 0x80, spriteram.read(offs + 3) & 0x08,
                            /* sprite placement IS correct - the squares on level 1 leave one pixel */
                            /* of the house background uncovered */
                            2 * (spriteram.read(offs)) - 7, 2 * (256 - spriteram.read(offs + 1)) - 16,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            if (popeye_background_pos.read(0) != 0) /* background is present */ {
                /* draw the frontmost playfield. They are characters, but draw them as sprites */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    int sx, sy;

                    sx = 16 * (offs % 32);
                    sy = 16 * (offs / 32) - 16;

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs), colorram.read(offs),
                            0, 0, sx, sy,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };
}
