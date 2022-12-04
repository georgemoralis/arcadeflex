/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.expressions.NOT;

public class airbustr {

    static tilemap bg_tilemap, fg_tilemap;

    /* Variables that drivers has access to */
    public static UBytePtr airbustr_bgram = new UBytePtr();
    public static UBytePtr airbustr_fgram = new UBytePtr();

    /* Variables defined in drivers */
    //extern unsigned char *spriteram;
    public static int flipscreen;

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_fg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = row * 32 + col;
            char attr = airbustr_fgram.read(0x400 + tile_index);
            SET_TILE_INFO(0,
                    airbustr_fgram.read(tile_index) + ((attr & 0x0f) << 8),
                    (attr >> 4) + 0);
        }
    };

    public static WriteHandlerPtr get_bg_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = row * 32 + col;
            char attr = airbustr_bgram.read(0x400 + tile_index);
            SET_TILE_INFO(0,
                    airbustr_bgram.read(tile_index) + ((attr & 0x0f) << 8),
                    (attr >> 4) + 16);
        }
    };

    public static VhStartPtr airbustr_vh_start = new VhStartPtr() {
        public int handler() {
            fg_tilemap = tilemap_create(get_fg_tile_info,
                    TILEMAP_TRANSPARENT,
                    16, 16,
                    32, 32);

            bg_tilemap = tilemap_create(get_bg_tile_info,
                    TILEMAP_OPAQUE,
                    16, 16,
                    32, 32);

            if (fg_tilemap != null && bg_tilemap != null) {
                fg_tilemap.transparent_pen = 0;

                return 0;
            } else {
                return 1;
            }
        }
    };

    public static WriteHandlerPtr airbustr_fgram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (airbustr_fgram.read(offset) != data) {
                airbustr_fgram.write(offset, data);
                /* offset % 0x400 will take care of both the tile code and color */
                tilemap_mark_tile_dirty(fg_tilemap, (offset % 0x400) % 32, (offset % 0x400) / 32);
            }
        }
    };

    public static WriteHandlerPtr airbustr_bgram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (airbustr_bgram.read(offset) != data) {
                airbustr_bgram.write(offset, data);
                /* offset % 0x400 will take care of both the tile code and color */
                tilemap_mark_tile_dirty(bg_tilemap, (offset % 0x400) % 32, (offset % 0x400) / 32);
            }
        }
    };

    static int bg_scrollx, bg_scrolly, fg_scrollx, fg_scrolly, highbits;
    public static WriteHandlerPtr airbustr_scrollregs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int xoffs, yoffs;

            if (flipscreen != 0) {
                xoffs = -0x06a;
                yoffs = -0x1ff;
            } else {
                xoffs = -0x094;
                yoffs = -0x100;
            }

            switch (offset) // offset 0 <. port 4
            {
                case 0x00:
                    fg_scrolly = data;
                    break;	// low 8 bits
                case 0x02:
                    fg_scrollx = data;
                    break;
                case 0x04:
                    bg_scrolly = data;
                    break;
                case 0x06:
                    bg_scrollx = data;
                    break;
                case 0x08:
                    highbits = ~data;
                    break;	// complemented high bits

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "CPU #2 - port %02X written with %02X - PC = %04X\n", offset, data, cpu_get_pc());
                    }
            }

            tilemap_set_scrollx(bg_tilemap, 0, ((highbits << 6) & 0x100) + bg_scrollx + xoffs);
            tilemap_set_scrolly(bg_tilemap, 0, ((highbits << 5) & 0x100) + bg_scrolly + yoffs);
            tilemap_set_scrollx(fg_tilemap, 0, ((highbits << 8) & 0x100) + fg_scrollx + xoffs);
            tilemap_set_scrolly(fg_tilemap, 0, ((highbits << 7) & 0x100) + fg_scrolly + yoffs);
        }
    };

    static void draw_sprites(osd_bitmap bitmap) {
        int i, offs;

        /* Let's draw the sprites */
        for (i = 0; i < 2; i++) {
            UBytePtr ram = new UBytePtr(spriteram, i * 0x800);//unsigned char *ram = &spriteram[i * 0x800];
            int sx = 0;
            int sy = 0;

            for (offs = 0; offs < 0x100; offs++) {
                int attr = ram.read(offs + 0x300);
                int x = ram.read(offs + 0x400) - ((attr << 8) & 0x100);
                int y = ram.read(offs + 0x500) - ((attr << 7) & 0x100);

                int gfx = ram.read(offs + 0x700);
                int code = ram.read(offs + 0x600) + ((gfx & 0x1f) << 8);
                int flipx = gfx & 0x80;
                int flipy = gfx & 0x40;

                /* multi sprite */
                if ((attr & 0x04) != 0) {
                    sx += x;
                    sy += y;
                } else {
                    sx = x;
                    sy = y;
                }

                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[1],
                        code,
                        attr >> 4,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

                /* let's get back to normal to support multi sprites */
                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                }

            }
        }

    }

    public static VhUpdatePtr airbustr_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            UBytePtr ram;
            int i, offs;

            tilemap_update(ALL_TILEMAPS);

            /* Palette Stuff */
            palette_init_used_colors();

            /* Sprites */
            for (i = 0; i < 2; i++) {
                ram = new UBytePtr(spriteram, i * 0x800 + 0x300);	// color code
                for (offs = 0; offs < 0x100; offs++) {
                    int color = 256 * 2 + (ram.read(offs) & 0xf0);
                    //memset(&palette_used_colors[color+1],PALETTE_COLOR_USED,16-1);
                    for (int k = 0; k < 16 - 1; k++) {
                        palette_used_colors.write(k + color + 1, PALETTE_COLOR_USED);
                    }
                }
            }

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, bg_tilemap, 0);
            tilemap_draw(bitmap, fg_tilemap, 0);
            draw_sprites(bitmap);
        }
    };
}
