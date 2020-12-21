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

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;

public class exedexes {

    public static UBytePtr exedexes_bg_scroll = new UBytePtr();

    public static UBytePtr exedexes_nbg_yscroll = new UBytePtr();
    public static UBytePtr exedexes_nbg_xscroll = new UBytePtr();

    static int TileMap(int offs) {
        return memory_region(REGION_GFX5).read(offs);
    }

    static int BackTileMap(int offs) {
        return (memory_region(REGION_GFX5).read(offs + 0x4000));
    }

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
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;
                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                /* green component */
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                /* blue component */
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++].set((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

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

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr exedexes_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;

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
                            BackTileMap(tile) & 0x3f,
                            BackTileMap(tile + 8 * 8),
                            BackTileMap(tile) & 0x40, BackTileMap(tile) & 0x80,
                            sy * 32 - (yo & 0x1F), sx * 32,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* front layer */
            for (sy = 0; sy <= 16; sy++) {
                for (sx = 0; sx < 16; sx++) {
                    int xo, yo, tile;

                    xo = ((exedexes_nbg_xscroll.read(1)) << 8) + exedexes_nbg_xscroll.read(0) + sx * 16;
                    yo = ((exedexes_nbg_yscroll.read(1)) << 8) + exedexes_nbg_yscroll.read(0) + sy * 16;

                    tile = ((yo & 0xf0) >> 4) + (xo & 0xF0) + (yo & 0x700) + ((xo & 0x700) << 3);

                    drawgfx(bitmap, Machine.gfx[2],
                            TileMap(tile),
                            0,
                            0, 0,
                            sy * 16 - (yo & 0xF), sx * 16 - (xo & 0xF),
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                drawgfx(bitmap, Machine.gfx[3],
                        spriteram.read(offs),
                        spriteram.read(offs + 1) & 0x0f,
                        spriteram.read(offs + 1) & 0x10, spriteram.read(offs + 1) & 0x20,
                        spriteram.read(offs + 3) - 0x10 * (spriteram.read(offs + 1) & 0x80), spriteram.read(offs + 2),
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                sx = offs % 32;
                sy = offs / 32;

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 2 * (colorram.read(offs) & 0x80),
                        colorram.read(offs) & 0x3f,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 207);
            }
        }
    };
}
