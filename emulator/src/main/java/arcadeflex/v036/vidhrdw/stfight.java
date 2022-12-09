/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//common imports
import static common.libc.expressions.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class stfight {

    // Real stuff
    public static UBytePtr stfight_text_char_ram = new UBytePtr();
    public static UBytePtr stfight_text_attr_ram = new UBytePtr();
    public static UBytePtr stfight_vh_latch_ram = new UBytePtr();
    public static UBytePtr stfight_sprite_ram = new UBytePtr();

    static tilemap fg_tilemap;
    static tilemap bg_tilemap;
    static int stfight_flipscreen = 0;
    static int stfight_sprite_base = 0;
    static int stfight_fg_x = 0;
    static int stfight_fg_y = 0;
    static int stfight_bg_x = 0;
    static int stfight_bg_y = 0;
    static int stfight_fg_x_tile = 0;   // not strictly necessary
    static int stfight_fg_y_tile = 0;   // but a small peformance
    static int stfight_bg_x_tile = 0;   // optimsation
    static int stfight_bg_y_tile = 0;

    /*
     Graphics ROM Format
     ===================
	
     Each tile is 8x8 pixels
     Each composite tile is 2x2 tiles, 16x16 pixels
     Each screen is 32x32 composite tiles, 64x64 tiles, 256x256 pixels
     Each layer is a 4-plane bitmap 8x16 screens, 2048x4096 pixels
	
     There are 4x256=1024 composite tiles defined for each layer
	
     Each layer is mapped using 2 bytes/composite tile
     - one byte for the tile
     - one byte for the tile bank, attribute
     - b7,b5     tile bank (0-3)
	
     Each pixel is 4 bits = 16 colours.
	
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr stfight_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            /* unique color for transparency */
            palette[256 * 3 + 0] = (char) 0x04;
            palette[256 * 3 + 1] = (char) 0x04;
            palette[256 * 3 + 2] = (char) 0x04;

            /* text uses colors 192-207 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                if ((color_prom.read() & 0x0f) == 0x0f) {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (256);
                    /* transparent */
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.read() & 0x0f) + 0xc0));
                }
                color_prom.inc();
            }
            color_prom.inc(256 - TOTAL_COLORS(0));
            /* rest of the PROM is unused */

 /* fg uses colors 64-127 */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (((color_prom.read(256) & 0x0f) + 16 * (color_prom.read(0) & 0x03) + 0x40));
                color_prom.inc();
            }
            color_prom.inc(256);

            /* bg uses colors 0-63 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (((color_prom.read(256) & 0x0f) + 16 * (color_prom.read(0) & 0x03) + 0x00));
                color_prom.inc();
            }
            color_prom.inc(256);

            /* sprites use colors 128-191 */
            for (i = 0; i < TOTAL_COLORS(4); i++) {
                colortable[Machine.drv.gfxdecodeinfo[4].color_codes_start + i] = (char) (((color_prom.read(256) & 0x0f) + 16 * (color_prom.read(0) & 0x03) + 0x80));
                color_prom.inc();
            }
            color_prom.inc(256);
        }
    };

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            UBytePtr fgMap = memory_region(REGION_GFX5);

            int tile_index;
            int attr;
            int tile_base;

            row = (stfight_fg_y_tile + row) & 0xff;
            col = (stfight_fg_x_tile + col) & 0x7f;

            tile_index = ((row & 0xf0) << 7)
                    + ((col & 0xf0) << 4)
                    + ((row & 0x0f) << 4)
                    + (col & 0x0f);

            attr = fgMap.read(0x8000 + tile_index);
            tile_base = ((attr & 0x80) << 2) | ((attr & 0x20) << 3);

            SET_TILE_INFO(1,
                    tile_base + fgMap.read(tile_index),
                    attr & 0x07);
        }
    };

    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            UBytePtr bgMap = memory_region(REGION_GFX6);

            int tile_index;
            int attr;
            int tile_bank;
            int tile_base;

            row = (stfight_bg_y_tile + row) & 0xff;
            col = (stfight_bg_x_tile + col) & 0x7f;

            // Convert the interleaved row to a linear virtual row
            row = ((row & 0x80) >> 3)
                    | ((row & 0x60) << 2)
                    | ((row & 0x10) << 1)
                    | (row & 0x0f);

            tile_index = ((col & 0x01) << 12)
                    + ((row & 0x1f0) << 6)
                    + ((col & 0xf0) << 3)
                    + ((row & 0x0f) << 3)
                    + ((col & 0x0f) >> 1);

            attr = bgMap.read(0x8000 + tile_index);
            tile_bank = (attr & 0x20) >> 5;
            tile_base = (attr & 0x80) << 1;

            SET_TILE_INFO(2 + tile_bank,
                    tile_base + bgMap.read(tile_index),
                    attr & 0x07);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr stfight_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            fg_tilemap = tilemap_create(
                    get_fg_tile_info,
                    TILEMAP_TRANSPARENT,
                    16, 16,
                    16 + 1, 16
            );

            bg_tilemap = tilemap_create(
                    get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    16, 16,
                    16 + 1, 16
            );

            if (fg_tilemap != null && bg_tilemap != null) {
                fg_tilemap.transparent_pen = 0x0F;

                return (0);
            }

            return (1);
        }
    };

    /**
     * *************************************************************************
     *
     * Memory handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr stfight_text_char_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (stfight_text_char_ram.read(offset) != data) {
                stfight_text_char_ram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr stfight_text_attr_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (stfight_text_attr_ram.read(offset) != data) {
                stfight_text_attr_ram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr stfight_sprite_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            stfight_sprite_base = ((data & 0x04) << 7)
                    | ((data & 0x01) << 8);
        }
    };

    public static WriteHandlerPtr stfight_vh_latch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            stfight_vh_latch_ram.write(offset, data);

            switch (offset) {
                case 0x01:
                    stfight_fg_x = (data << 8) | stfight_vh_latch_ram.read(0);
                    break;

                case 0x03:
                    stfight_fg_y = (data << 8) | stfight_vh_latch_ram.read(2);
                    break;

                case 0x05:
                    stfight_bg_x = (data << 8) | stfight_vh_latch_ram.read(4);
                    break;

                case 0x08:
                    stfight_bg_y = (data << 8) | stfight_vh_latch_ram.read(6);
                    break;

                case 0x07:
                    tilemap_set_enable(bg_tilemap, (data & 0x20) != 0 ? 1 : 0);
                    tilemap_set_enable(fg_tilemap, (data & 0x10) != 0 ? 1 : 0);
                    stfight_flipscreen = (data & 0x01);
                    tilemap_set_flip(ALL_TILEMAPS,
                            (stfight_flipscreen != 0
                                    ? (TILEMAP_FLIPY | TILEMAP_FLIPX)
                                    : 0));
                    break;
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Display refresh
     *
     **************************************************************************
     */
    static void draw_sprites(osd_bitmap bitmap, int priority) {
        int offs, sx, sy;

        for (offs = 4096 - 32; offs >= 0; offs -= 32) {
            int code;
            int attr = stfight_sprite_ram.read(offs + 1);
            int flipx = (((attr & 0x10) != 0 ? 1 : 0)
                    ^ stfight_flipscreen);
            int flipy = (stfight_flipscreen);
            int color = attr & 0x0f;
            int pri = (attr & 0x20) >> 5;

            if (pri != priority) {
                continue;
            }

            sy = stfight_sprite_ram.read(offs + 2);
            sx = stfight_sprite_ram.read(offs + 3);

            // non-active sprites have zero y coordinate value
            if (sy > 0) {
                // sprites which wrap onto/off the screen have
                // a sign extension bit in the sprite attribute
                if (sx >= 0xf0) {
                    if ((((attr & 0x80) != 0 ? 1 : 0)
                            ^ stfight_flipscreen) != 0) {
                        sx -= 0x100;
                    }
                }

                if (stfight_flipscreen != 0) {
                    sy = 256 - 16 - sy;
                    sx = 256 - 16 - sx;
                }

                code = stfight_sprite_base + stfight_sprite_ram.read(offs);

                drawgfx(bitmap, Machine.gfx[4],
                        code,
                        color,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0x0f);
            }
        }
    }

    public static VhUpdateHandlerPtr stfight_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            // Only dirty layer if we scroll to next tile
            if ((stfight_bg_x >> 4) != stfight_bg_x_tile
                    || (stfight_bg_y >> 4) != stfight_bg_y_tile) {
                tilemap_mark_all_tiles_dirty(bg_tilemap);
                stfight_bg_x_tile = stfight_bg_x >> 4;
                stfight_bg_y_tile = stfight_bg_y >> 4;
            }

            // Only dirty layer if we scroll to next tile
            if ((stfight_fg_x >> 4) != stfight_fg_x_tile
                    || (stfight_fg_y >> 4) != stfight_fg_y_tile) {
                tilemap_mark_all_tiles_dirty(fg_tilemap);
                stfight_fg_x_tile = stfight_fg_x >> 4;
                stfight_fg_y_tile = stfight_fg_y >> 4;
            }

            tilemap_set_scrollx(bg_tilemap, 0, stfight_bg_x & 0x0f);
            tilemap_set_scrolly(bg_tilemap, 0, stfight_bg_y & 0x0f);

            tilemap_set_scrollx(fg_tilemap, 0, stfight_fg_x & 0x0f);
            tilemap_set_scrolly(fg_tilemap, 0, stfight_fg_y & 0x0f);

            tilemap_update(fg_tilemap);
            tilemap_update(bg_tilemap);

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(fg_tilemap);
            tilemap_render(bg_tilemap);

            /*
             *  Now draw everything
             */
            if (bg_tilemap.enable != 0) {
                tilemap_draw(bitmap, bg_tilemap, 0);
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            }

            /* Draw sprites which appear behind the foreground layer */
            if ((stfight_vh_latch_ram.read(0x07) & 0x40) != 0) {
                draw_sprites(bitmap, 1);
            }

            tilemap_draw(bitmap, fg_tilemap, 0);

            /* Draw sprites which appear on top */
            if ((stfight_vh_latch_ram.read(0x07) & 0x40) != 0) {
                draw_sprites(bitmap, 0);
            }

            if ((stfight_vh_latch_ram.read(0x07) & 0x80) != 0) {
                for (offs = 0x400 - 1; offs >= 0; offs--) {
                    int sx, sy, flipx, flipy;
                    /*unsigned*/ char attr = stfight_text_attr_ram.read(offs);

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = attr & 0x20;
                    flipy = attr & 0x40;
                    if (stfight_flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(bitmap, Machine.gfx[0],
                            stfight_text_char_ram.read(offs) + 2 * (attr & 0x80),
                            attr & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_COLOR, 256);
                }
            }
        }
    };
}
