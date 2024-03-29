/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
 */
/**
 * Changelog
 * =========
 * 24/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class mrdo {

    public static UBytePtr mrdo_bgvideoram = new UBytePtr();
    public static UBytePtr mrdo_fgvideoram = new UBytePtr();
    static tilemap bg_tilemap, fg_tilemap;
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Mr. Do! has two 32 bytes palette PROM and a 32 bytes sprite color lookup
     * table PROM. The palette PROMs are connected to the RGB output this way:
     *
     * U2: bit 7 -- unused -- unused -- 100 ohm resistor -- BLUE -- 75 ohm
     * resistor -- BLUE -- 100 ohm resistor -- GREEN -- 75 ohm resistor -- GREEN
     * -- 100 ohm resistor -- RED bit 0 -- 75 ohm resistor -- RED
     *
     * T2: bit 7 -- unused -- unused -- 150 ohm resistor -- BLUE -- 120 ohm
     * resistor -- BLUE -- 150 ohm resistor -- GREEN -- 120 ohm resistor --
     * GREEN -- 150 ohm resistor -- RED bit 0 -- 120 ohm resistor -- RED
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr mrdo_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            int p_inc = 0;
            for (i = 0; i < 256; i++) {
                int a1, a2;
                int bit0, bit1, bit2, bit3;

                a1 = ((i >> 3) & 0x1c) + (i & 0x03) + 32;
                a2 = ((i >> 0) & 0x1c) + (i & 0x03);

                bit0 = (color_prom.read(a1) >> 1) & 0x01;
                bit1 = (color_prom.read(a1) >> 0) & 0x01;
                bit2 = (color_prom.read(a2) >> 1) & 0x01;
                bit3 = (color_prom.read(a2) >> 0) & 0x01;
                palette[p_inc++] = (char) (0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3);
                bit0 = (color_prom.read(a1) >> 3) & 0x01;
                bit1 = (color_prom.read(a1) >> 2) & 0x01;
                bit2 = (color_prom.read(a2) >> 3) & 0x01;
                bit3 = (color_prom.read(a2) >> 2) & 0x01;
                palette[p_inc++] = (char) (0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3);
                bit0 = (color_prom.read(a1) >> 5) & 0x01;
                bit1 = (color_prom.read(a1) >> 4) & 0x01;
                bit2 = (color_prom.read(a2) >> 5) & 0x01;
                bit3 = (color_prom.read(a2) >> 4) & 0x01;
                palette[p_inc++] = (char) (0x2c * bit0 + 0x37 * bit1 + 0x43 * bit2 + 0x59 * bit3);
            }

            color_prom.inc(64);

            /* sprites */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                int bits;

                if (i < 32) {
                    bits = color_prom.read(i) & 0x0f;
                    /* low 4 bits are for sprite color n */
                } else {
                    bits = color_prom.read(i & 0x1f) >> 4;
                    /* high 4 bits are for sprite color n + 8 */
                }

                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (bits + ((bits & 0x0c) << 3));
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
            char attr = mrdo_bgvideoram.read(tile_index);
            SET_TILE_INFO(1, mrdo_bgvideoram.read(tile_index + 0x400) + ((attr & 0x80) << 1), attr & 0x3f);
            tile_info.flags = (char) (TILE_SPLIT((attr & 0x40) >> 6));
        }
    };

    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 32 * row + col;
            char attr = mrdo_fgvideoram.read(tile_index);
            SET_TILE_INFO(0, mrdo_fgvideoram.read(tile_index + 0x400) + ((attr & 0x80) << 1), attr & 0x3f);
            tile_info.flags = (char) (TILE_SPLIT((attr & 0x40) >> 6));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr mrdo_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(get_bg_tile_info, TILEMAP_SPLIT, 8, 8, 32, 32);
            fg_tilemap = tilemap_create(get_fg_tile_info, TILEMAP_SPLIT, 8, 8, 32, 32);

            if (bg_tilemap == null || fg_tilemap == null) {
                return 1;
            }

            bg_tilemap.transmask[0] = 0x01;
            /* split type 0 has pen 1 transparent in front half */

            bg_tilemap.transmask[1] = 0x00;
            /* split type 1 is totally opaque in front half */

            fg_tilemap.transmask[0] = 0x01;
            /* split type 0 has pen 1 transparent in front half */

            fg_tilemap.transmask[1] = 0x00;
            /* split type 1 is totally opaque in front half */

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr mrdo_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (mrdo_bgvideoram.read(offset) != data) {
                mrdo_bgvideoram.write(offset, data);
                tilemap_mark_tile_dirty(bg_tilemap, offset % 32, (offset & 0x3ff) / 32);
            }
        }
    };

    public static WriteHandlerPtr mrdo_fgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (mrdo_fgvideoram.read(offset) != data) {
                mrdo_fgvideoram.write(offset, data);
                tilemap_mark_tile_dirty(fg_tilemap, offset % 32, (offset & 0x3ff) / 32);
            }
        }
    };

    public static WriteHandlerPtr mrdo_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tilemap_set_scrollx(bg_tilemap, 0, data);
        }
    };

    public static WriteHandlerPtr mrdo_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tilemap_set_scrolly(bg_tilemap, 0, data);
        }
    };

    public static WriteHandlerPtr mrdo_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 1-3 control the playfield priority, but they are not used by */
 /* Mr. Do! so we don't emulate them */

            flipscreen = data & 0x01;
            tilemap_set_flip(ALL_TILEMAPS, flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
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
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            if (spriteram.read(offs + 1) != 0) {
                drawgfx(bitmap, Machine.gfx[2],
                        spriteram.read(offs), spriteram.read(offs + 2) & 0x0f,
                        spriteram.read(offs + 2) & 0x10, spriteram.read(offs + 2) & 0x20,
                        spriteram.read(offs + 3), 256 - spriteram.read(offs + 1),
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    }

    public static VhUpdateHandlerPtr mrdo_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_update(ALL_TILEMAPS);

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            tilemap_draw(bitmap, bg_tilemap, TILEMAP_FRONT);
            tilemap_draw(bitmap, fg_tilemap, TILEMAP_FRONT);
            draw_sprites(bitmap);
        }
    };
}
