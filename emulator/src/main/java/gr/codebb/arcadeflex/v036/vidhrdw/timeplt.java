/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class timeplt {

    public static UBytePtr timeplt_videoram = new UBytePtr();
    public static UBytePtr timeplt_colorram = new UBytePtr();
    static tilemap bg_tilemap;
    static int flipscreen;
    static int sprite_multiplex_hack;

    public static InitDriverPtr init_timeplt = new InitDriverPtr() {
        public void handler() {
            sprite_multiplex_hack = 1;
        }
    };
    public static InitDriverPtr init_psurge = new InitDriverPtr() {
        public void handler() {
            sprite_multiplex_hack = 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Time Pilot has two 32x8 palette PROMs and two 256x4 lookup table PROMs
     * (one for characters, one for sprites). The palette PROMs are connected to
     * the RGB output this way:
     *
     * bit 7 -- 390 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 560 ohm
     * resistor -- BLUE -- 820 ohm resistor -- BLUE -- 1.2kohm resistor -- BLUE
     * -- 390 ohm resistor -- GREEN -- 470 ohm resistor -- GREEN bit 0 -- 560
     * ohm resistor -- GREEN
     *
     * bit 7 -- 820 ohm resistor -- GREEN -- 1.2kohm resistor -- GREEN -- 390
     * ohm resistor -- RED -- 470 ohm resistor -- RED -- 560 ohm resistor -- RED
     * -- 820 ohm resistor -- RED -- 1.2kohm resistor -- RED bit 0 -- not
     * connected
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr timeplt_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3, bit4;

                bit0 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 4) & 0x01;
                bit4 = (color_prom.read(Machine.drv.total_colors) >> 5) & 0x01;
                palette[p_inc++]=(char) (0x19 * bit0 + 0x24 * bit1 + 0x35 * bit2 + 0x40 * bit3 + 0x4d * bit4);
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 6) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 7) & 0x01;
                bit2 = (color_prom.read(0) >> 0) & 0x01;
                bit3 = (color_prom.read(0) >> 1) & 0x01;
                bit4 = (color_prom.read(0) >> 2) & 0x01;
                palette[p_inc++]=(char) (0x19 * bit0 + 0x24 * bit1 + 0x35 * bit2 + 0x40 * bit3 + 0x4d * bit4);
                bit0 = (color_prom.read(0) >> 3) & 0x01;
                bit1 = (color_prom.read(0) >> 4) & 0x01;
                bit2 = (color_prom.read(0) >> 5) & 0x01;
                bit3 = (color_prom.read(0) >> 6) & 0x01;
                bit4 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++]=(char) (0x19 * bit0 + 0x24 * bit1 + 0x35 * bit2 + 0x40 * bit3 + 0x4d * bit4);

                color_prom.inc();
            }

            color_prom.inc(Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

            /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (color_prom.readinc() & 0x0f);
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc() & 0x0f) + 0x10);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 32 * row + col;
            char attr = timeplt_colorram.read(tile_index);
            SET_TILE_INFO(0, timeplt_videoram.read(tile_index) + ((attr & 0x20) << 3), attr & 0x1f);
            tile_info.flags = (char) (TILE_FLIPYX((attr & 0xc0) >> 6));
            tile_info.priority = (char) ((attr & 0x10) >> 4);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr timeplt_vh_start = new VhStartPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(
                    get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    8, 8,
                    32, 32
            );

            if (bg_tilemap != null) {
                return 0;
            }

            return 1;
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr timeplt_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (timeplt_videoram.read(offset) != data) {
                timeplt_videoram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, offset % 32, offset / 32);
            }
        }
    };

    public static WriteHandlerPtr timeplt_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (timeplt_colorram.read(offset) != data) {
                timeplt_colorram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, offset % 32, offset / 32);
            }
        }
    };

    public static WriteHandlerPtr timeplt_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            flipscreen = data & 1;
            tilemap_set_flip(ALL_TILEMAPS, flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
        }
    };

    /* Return the current video scan line */
    public static ReadHandlerPtr timeplt_scanline_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_scalebyfcount.handler(256);
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    static void draw_sprites(osd_bitmap bitmap) {
        GfxElement gfx = Machine.gfx[1];
        rectangle clip = Machine.drv.visible_area;
        int offs;

        for (offs = spriteram_size[0] - 2; offs >= 0; offs -= 2) {
            int code, color, sx, sy, flipx, flipy;

            code = spriteram.read(offs + 1);
            color = spriteram_2.read(offs) & 0x3f;
            sx = 240 - spriteram.read(offs);
            sy = spriteram_2.read(offs + 1) - 1;
            flipx = spriteram_2.read(offs) & 0x40;
            flipy = NOT(spriteram_2.read(offs) & 0x80);

            drawgfx(bitmap, gfx,
                    code,
                    color,
                    flipx, flipy,
                    sx, sy,
                    clip, TRANSPARENCY_PEN, 0);

            if (sprite_multiplex_hack != 0) {
                if (sy < 240) {
                    /* clouds are drawn twice, offset by 128 pixels horizontally and vertically */
                    /* this is done by the program, multiplexing the sprites; we don't emulate */
                    /* that, we just reproduce the behaviour. */
                    if (offs <= 2 * 2 || offs >= 19 * 2) {
                        drawgfx(bitmap, gfx,
                                code,
                                color,
                                flipx, flipy,
                                (sx + 128) & 0xff, (sy + 128) & 0xff,
                                clip, TRANSPARENCY_PEN, 0);
                    }
                }
            }
        }
    }

    public static VhUpdatePtr timeplt_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_update(ALL_TILEMAPS);

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, bg_tilemap, 0);
            draw_sprites(bitmap);
            tilemap_draw(bitmap, bg_tilemap, 1);
        }
    };
}
