/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.expressions.*;
//TODO
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class galivan {

    static /*unsigned*/ char[] scrollx = new char[2];
    static char[] scrolly = new char[2];

    /* Layers has only bits 5-6 active.
	   6 selects background off/on
	   5 is unknown (active only on title screen,
	     not for scores or push start nor game)
     */
    static int flipscreen, layers;

    static tilemap bg_tilemap, char_tilemap;

    static UBytePtr spritepalettebank;
    static int ninjemak_dispdisable;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr galivan_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup tables */

 /* characters use colors 0-127 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;
            }

            /* I think that */
 /* background tiles use colors 192-255 in four banks */
 /* the bottom two bits of the color code select the palette bank for */
 /* pens 0-7; the top two bits for pens 8-15. */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                if ((i & 8) != 0) {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (192 + (i & 0x0f) + ((i & 0xc0) >> 2));
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (192 + (i & 0x0f) + ((i & 0x30) >> 0));
                }
            }

            /* sprites use colors 128-191 in four banks */
 /* The lookup table tells which colors to pick from the selected bank */
 /* the bank is selected by another PROM and depends on the top 7 bits of */
 /* the sprite code. The PROM selects the bank *separately* for pens 0-7 and */
 /* 8-15 (like for tiles). */
            for (i = 0; i < TOTAL_COLORS(2) / 16; i++) {
                int j;

                for (j = 0; j < 16; j++) {
                    if ((i & 8) != 0) {
                        colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i + j * (TOTAL_COLORS(2) / 16)] = (char) (128 + ((j & 0x0c) << 2) + (color_prom.read() & 0x0f));
                        //COLOR(2,i + j * (TOTAL_COLORS(2)/16)) = 128 + ((j & 0x0c) << 2) + (*color_prom & 0x0f);
                    } else {
                        colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i + j * (TOTAL_COLORS(2) / 16)] = (char) (128 + ((j & 0x03) << 4) + (color_prom.read() & 0x0f));
                        //COLOR(2,i + j * (TOTAL_COLORS(2)/16)) = 128 + ((j & 0x03) << 4) + (*color_prom & 0x0f);
                    }
                }

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the sprite palette bank table */
            spritepalettebank = new UBytePtr(color_prom);
            /* we'll need it at run time */
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
            UBytePtr BGROM = memory_region(REGION_GFX4);
            int addr = 128 * row + col;
            int attr = BGROM.read(addr + 0x4000);
            int code = BGROM.read(addr) | ((attr & 0x03) << 8);
            SET_TILE_INFO(1, code, (attr & 0x78) >> 3);
            /* seems correct */
        }
    };

    public static WriteHandlerPtr get_char_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int addr = 32 * col + row;
            int attr = colorram.read(addr);
            int code = videoram.read(addr) | ((attr & 0x01) << 8);
            SET_TILE_INFO(0, code, (attr & 0xe0) >> 5);
            /* not sure */
            tile_info.priority = (attr & 8) != 0 ? (char) 0 : (char) 1;
            /* wrong */
        }
    };

    public static WriteHandlerPtr ninjemak_get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            UBytePtr BGROM = memory_region(REGION_GFX4);
            int addr = 32 * col + row;
            int attr = BGROM.read(addr + 0x4000);
            int code = BGROM.read(addr) | ((attr & 0x03) << 8);
            SET_TILE_INFO(1, code, ((attr & 0x60) >> 3) | ((attr & 0x0c) >> 2));
            /* seems correct */
        }
    };

    public static WriteHandlerPtr ninjemak_get_char_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int addr = 32 * col + row;
            int attr = colorram.read(addr);
            int code = videoram.read(addr) | ((attr & 0x03) << 8);
            SET_TILE_INFO(0, code, (attr & 0x1c) >> 2);
            /* seems correct ? */
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr galivan_vh_start = new VhStartPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    16, 16,
                    128, 128);
            char_tilemap = tilemap_create(get_char_tile_info,
                    TILEMAP_TRANSPARENT,
                    8, 8,
                    32, 32);

            if (bg_tilemap == null || char_tilemap == null) {
                return 1;
            }

            char_tilemap.transparent_pen = 15;

            return 0;
        }
    };

    public static VhStartPtr ninjemak_vh_start = new VhStartPtr() {
        public int handler() {
            bg_tilemap = tilemap_create(ninjemak_get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    16, 16,
                    512, 32);
            char_tilemap = tilemap_create(ninjemak_get_char_tile_info,
                    TILEMAP_TRANSPARENT,
                    8, 8,
                    32, 32);

            if (bg_tilemap == null || char_tilemap == null) {
                return 1;
            }

            char_tilemap.transparent_pen = 15;

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
    public static WriteHandlerPtr galivan_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videoram.read(offset) != data) {
                videoram.write(offset, data);
                tilemap_mark_tile_dirty(char_tilemap, offset / 32, offset % 32);
            }
        }
    };

    public static WriteHandlerPtr galivan_colorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (colorram.read(offset) != data) {
                colorram.write(offset, data);
                tilemap_mark_tile_dirty(char_tilemap, offset / 32, offset % 32);
            }
        }
    };

    /* Written through port 40 */
    public static WriteHandlerPtr galivan_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bit 2 flip screen */
            flipscreen = data & 0x04;
            tilemap_set_flip(bg_tilemap, flipscreen != 0 ? TILEMAP_FLIPX | TILEMAP_FLIPY : 0);
            tilemap_set_flip(char_tilemap, flipscreen != 0 ? TILEMAP_FLIPX | TILEMAP_FLIPY : 0);

            /* bit 7 selects one of two ROM banks for c000-dfff */
            {
                int bank = (data & 0x80) >> 7;
                UBytePtr RAM = memory_region(REGION_CPU1);

                cpu_setbank(1, new UBytePtr(RAM, 0x10000 + 0x2000 * bank));
            }

            /*	if (errorlog != 0) fprintf(errorlog,"Address: %04X - port 40 = %02x\n",cpu_get_pc(),data); */
        }
    };

    public static WriteHandlerPtr ninjemak_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bit 2 flip screen */
            flipscreen = data & 0x04;
            tilemap_set_flip(bg_tilemap, flipscreen != 0 ? TILEMAP_FLIPX | TILEMAP_FLIPY : 0);
            tilemap_set_flip(char_tilemap, flipscreen != 0 ? TILEMAP_FLIPX | TILEMAP_FLIPY : 0);

            /* bit 3 text bank flag ??? */
            if ((data & 0x08) != 0) {
                /* This is a temporary condition specification. */

                int offs;

                if (errorlog != null) {
                    fprintf(errorlog, "%04x: write %02x to port 80\n", cpu_get_pc(), data);
                }

                for (offs = 0; offs < videoram_size[0]; offs++) {
                    galivan_videoram_w.handler(offs, 0x20);
                }
                for (offs = 0; offs < videoram_size[0]; offs++) {
                    galivan_colorram_w.handler(offs, 0x03);
                }
            }

            /* bit 4 background disable flag */
            ninjemak_dispdisable = data & 0x10;

            /* bit 5 sprite flag ??? */
 /* bit 6, 7 ROM bank select */
            {
                int bank = (data & 0xc0) >> 6;
                UBytePtr RAM = memory_region(REGION_CPU1);

                cpu_setbank(1, new UBytePtr(RAM, 0x10000 + 0x2000 * bank));
            }
        }
    };

    /* Written through port 41-42 */
    static int up = 0;
    public static WriteHandlerPtr galivan_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (offset == 1) {
                if ((data & 0x80) != 0) {
                    up = 1;
                } else if (up != 0) {
                    layers = data & 0x60;
                    up = 0;
                }
            }
            scrollx[offset] = (char) (data & 0xFF);
        }
    };

    /* Written through port 43-44 */
    public static WriteHandlerPtr galivan_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scrolly[offset] = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr ninjemak_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scrollx[offset] = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr ninjemak_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scrolly[offset] = (char) (data & 0xFF);
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

        /* draw the sprites */
        for (offs = 0; offs < spriteram_size[0]; offs += 4) {
            int code;
            int attr = spriteram.read(offs + 2);
            int color = (attr & 0x3c) >> 2;
            int flipx = attr & 0x40;
            int flipy = attr & 0x80;
            int sx, sy;

            sx = (spriteram.read(offs + 3) - 0x80) + 256 * (attr & 0x01);
            sy = 240 - spriteram.read(offs);
            if (flipscreen != 0) {
                sx = 240 - sx;
                sy = 240 - sy;
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            //		code = spriteram[offs+1] + ((attr & 0x02) << 7);
            code = spriteram.read(offs + 1) + ((attr & 0x06) << 7);	// for ninjemak, not sure ?

            drawgfx(bitmap, Machine.gfx[2],
                    code,
                    color + 16 * (spritepalettebank.read(code >> 2) & 0x0f),
                    flipx, flipy,
                    sx, sy,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
        }
    }

    public static VhUpdatePtr galivan_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(bg_tilemap, 0, scrollx[0] + 256 * (scrollx[1] & 0x07));
            tilemap_set_scrolly(bg_tilemap, 0, scrolly[0] + 256 * (scrolly[1] & 0x07));

            tilemap_update(ALL_TILEMAPS);
            tilemap_render(ALL_TILEMAPS);

            if ((layers & 0x40) != 0) {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            } else {
                tilemap_draw(bitmap, bg_tilemap, 0);
            }

            tilemap_draw(bitmap, char_tilemap, 0);

            draw_sprites(bitmap);

            tilemap_draw(bitmap, char_tilemap, 1);
        }
    };
    public static VhUpdatePtr ninjemak_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* (scrollx[1] & 0x40) does something */
            tilemap_set_scrollx(bg_tilemap, 0, scrollx[0] + 256 * (scrollx[1] & 0x1f));
            tilemap_set_scrolly(bg_tilemap, 0, scrolly[0] + 256 * (scrolly[1] & 0xff));

            tilemap_update(ALL_TILEMAPS);
            tilemap_render(ALL_TILEMAPS);

            if (ninjemak_dispdisable != 0) {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            } else {
                tilemap_draw(bitmap, bg_tilemap, 0);
            }

            draw_sprites(bitmap);

            tilemap_draw(bitmap, char_tilemap, 0);
        }
    };
}
