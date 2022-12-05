/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class exedexes {

    public static UBytePtr exedexes_bg_scroll = new UBytePtr();

    public static UBytePtr exedexes_nbg_yscroll = new UBytePtr();
    public static UBytePtr exedexes_nbg_xscroll = new UBytePtr();

    static int chon, objon, sc1on, sc2on;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Exed Exes has three 256x4 palette PROMs (one per gun), three 256x4 lookup
     * table PROMs (one for characters, one for sprites, one for background
     * tiles) and one 256x4 sprite palette bank selector PROM.
     *
     * The palette PROMs are connected to the RGB output this way:
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr exedexes_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

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
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 192-207 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) + 192);
            }

            /* 32x32 tiles use colors 0-15 */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((color_prom.readinc()));
            }

            /* 16x16 tiles use colors 64-79 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) ((color_prom.readinc()) + 64);
            }

            /* sprites use colors 128-191 in four banks */
            for (i = 0; i < TOTAL_COLORS(3); i++) {
                colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char) (color_prom.read(0) + 128 + 16 * color_prom.read(256));
                color_prom.inc();
            }
        }
    };

    public static WriteHandlerPtr exedexes_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are coin counters */
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);

            /* bit 7 is text enable */
            chon = data & 0x80;

            /* other bits seem to be unused */
        }
    };

    public static WriteHandlerPtr exedexes_gfxctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 4 is bg enable */
            sc2on = data & 0x10;

            /* bit 5 is fg enable */
            sc1on = data & 0x20;

            /* bit 6 is sprite enable */
            objon = data & 0x40;

            /* other bits seem to be unused */
        }
    };

    static void draw_sprites(osd_bitmap bitmap, int priority) {
        int offs;

        priority = priority != 0 ? 0x40 : 0x00;

        for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
            if ((buffered_spriteram.read(offs + 1) & 0x40) == priority) {
                int code, color, flipx, flipy, sx, sy;

                code = buffered_spriteram.read(offs);
                color = buffered_spriteram.read(offs + 1) & 0x0f;
                flipx = buffered_spriteram.read(offs + 1) & 0x10;
                flipy = buffered_spriteram.read(offs + 1) & 0x20;
                sx = buffered_spriteram.read(offs + 3) - ((buffered_spriteram.read(offs + 1) & 0x80) << 1);
                sy = buffered_spriteram.read(offs + 2);

                drawgfx(bitmap, Machine.gfx[3],
                        code,
                        color,
                        flipx, flipy,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    }

    public static VhUpdatePtr exedexes_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;

            if (sc2on != 0) {
                /* TODO: this is very slow, have to optimize it using a temporary bitmap */
 /* draw the background graphics */
 /* back layer */
                for (sy = 0; sy <= 8; sy++) {
                    for (sx = 0; sx < 8; sx++) {
                        int xo, yo, tile;

                        xo = sx * 32;
                        yo = ((exedexes_bg_scroll.read(1)) << 8) + exedexes_bg_scroll.read(0) + sy * 32;

                        tile = ((yo & 0xe0) >> 5) + ((xo & 0xe0) >> 2) + ((yo & 0x3f00) >> 1);

                        drawgfx(bitmap, Machine.gfx[1],
                                memory_region(REGION_GFX5).read(tile + 0x4000) & 0x3f,
                                memory_region(REGION_GFX5).read((tile + 0x4000) + 8 * 8),
                                memory_region(REGION_GFX5).read(tile + 0x4000) & 0x40,
                                memory_region(REGION_GFX5).read(tile + 0x4000) & 0x80,
                                sy * 32 - (yo & 0x1F), sx * 32,
                                Machine.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.visible_area);
            }

            if (objon != 0) {
                draw_sprites(bitmap, 1);
            }

            if (sc1on != 0) {
                /* front layer */
                for (sy = 0; sy <= 16; sy++) {
                    for (sx = 0; sx < 16; sx++) {
                        int xo, yo, tile;

                        xo = ((exedexes_nbg_xscroll.read(1)) << 8) + exedexes_nbg_xscroll.read(0) + sx * 16;
                        yo = ((exedexes_nbg_yscroll.read(1)) << 8) + exedexes_nbg_yscroll.read(0) + sy * 16;

                        tile = ((yo & 0xf0) >> 4) + (xo & 0xF0) + (yo & 0x700) + ((xo & 0x700) << 3);

                        drawgfx(bitmap, Machine.gfx[2],
                                memory_region(REGION_GFX5).read(tile),
                                0,
                                0, 0,
                                sy * 16 - (yo & 0xF), sx * 16 - (xo & 0xF),
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }

            if (objon != 0) {
                draw_sprites(bitmap, 0);
            }

            if (chon != 0) {
                /* draw the frontmost playfield. They are characters, but draw them as sprites */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + 2 * (colorram.read(offs) & 0x80),
                            colorram.read(offs) & 0x3f,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_COLOR, 207);
                }
            }
        }
    };

    public static VhEofCallbackPtr exedexes_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            buffer_spriteram_w.handler(0, 0);
        }
    };
}
