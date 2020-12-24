package gr.codebb.arcadeflex.v036.vidhrdw.konami;

/*
 used in battlnts driver. Seems to be fully functional
 */
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;

public class K007342 {

    //K007342_callback interface
    public static abstract interface K007342_callbackProcPtr {

        public abstract void handler(int tilemap, int bank, int[] code, int[] color);
    }

    static UBytePtr K007342_ram, K007342_scroll_ram;
    static int K007342_gfxnum;
    static int K007342_int_enabled;
    static int K007342_flipscreen;
    static int[] K007342_scrollx = new int[2];
    static int[] K007342_scrolly = new int[2];
    static UBytePtr K007342_videoram_0, K007342_colorram_0;
    static UBytePtr K007342_videoram_1, K007342_colorram_1;
    static int[] K007342_regs = new int[8];
    static K007342_callbackProcPtr K007342_callback;
    static tilemap[] K007342_tilemap = new tilemap[2];

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */

    /*
     data format:
     video RAM     xxxxxxxx    tile number (bits 0-7)
     color RAM     x-------    tiles with priority over the sprites
     color RAM     -x------    depends on external conections
     color RAM     --x-----    flip Y
     color RAM     ---x----    flip X
     color RAM     ----xxxx    depends on external connections (usually color and banking)
     */
    public static void tilemap_0_preupdate() {
        colorram = K007342_colorram_0;
        videoram1 = K007342_videoram_0;
        layer = 0;
    }

    public static void tilemap_1_preupdate() {
        colorram = K007342_colorram_1;
        videoram1 = K007342_videoram_1;
        layer = 1;
    }

    public static WriteHandlerPtr K007342_get_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index;
            int color[] = new int[1];
            int code[] = new int[1];

            if (col >= 32) {
                col -= 32;
                tile_index = 0x400 + row * 32 + col;
            } else {
                tile_index = row * 32 + col;
            }

            color[0] = colorram.read(tile_index);
            code[0] = videoram1.read(tile_index);

            tile_info.flags = (char) (TILE_FLIPYX((color[0] & 0x30) >> 4) & 0xFF);
            tile_info.priority = (char) (((color[0] & 0x80) >> 7) & 0xFF);

            K007342_callback.handler(layer, K007342_regs[1], code, color);

            SET_TILE_INFO(K007342_gfxnum, code[0], color[0]);
        }
    };

    public static int K007342_vh_start(int gfx_index, K007342_callbackProcPtr callback) {
        K007342_gfxnum = gfx_index;
        K007342_callback = callback;

        K007342_tilemap[0] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
        K007342_tilemap[1] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);

        K007342_ram = new UBytePtr(0x2000);
        K007342_scroll_ram = new UBytePtr(0x0200);

        if (K007342_ram == null || K007342_scroll_ram == null || K007342_tilemap[0] == null || K007342_tilemap[1] == null) {
            K007342_vh_stop();
            return 1;
        }

        memset(K007342_ram, 0, 0x2000);

        K007342_colorram_0 = new UBytePtr(K007342_ram, 0x0000);
        K007342_colorram_1 = new UBytePtr(K007342_ram, 0x1000);
        K007342_videoram_0 = new UBytePtr(K007342_ram, 0x0800);
        K007342_videoram_1 = new UBytePtr(K007342_ram, 0x1800);

        K007342_tilemap[0].transparent_pen = 0;
        K007342_tilemap[1].transparent_pen = 0;

        return 0;
    }

    public static void K007342_vh_stop() {
        K007342_ram = null;
        K007342_scroll_ram = null;
    }
    public static ReadHandlerPtr K007342_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007342_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007342_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset < 0x1000) {		/* layer 0 */

                if (K007342_ram.read(offset) != data) {
                    if ((offset & 0x400) != 0) {
                        tilemap_mark_tile_dirty(K007342_tilemap[0], offset % 32 + 32, (offset & 0x3ff) / 32);
                    } else {
                        tilemap_mark_tile_dirty(K007342_tilemap[0], offset % 32, (offset & 0x3ff) / 32);
                    }
                    K007342_ram.write(offset, data);
                }
            } else {						/* layer 1 */

                if (K007342_ram.read(offset) != data) {
                    if ((offset & 0x400) != 0) {
                        tilemap_mark_tile_dirty(K007342_tilemap[1], offset % 32 + 32, (offset & 0x3ff) / 32);
                    } else {
                        tilemap_mark_tile_dirty(K007342_tilemap[1], offset % 32, (offset & 0x3ff) / 32);
                    }
                    K007342_ram.write(offset, data);
                }
            }
        }
    };

    public static ReadHandlerPtr K007342_scroll_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007342_scroll_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007342_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007342_scroll_ram.write(offset, data);
        }
    };

    public static WriteHandlerPtr K007342_vreg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x00:
                    /* bit 1: INT control */
                    K007342_int_enabled = data & 0x02;
                    K007342_flipscreen = data & 0x10;
                    tilemap_set_flip(K007342_tilemap[0], K007342_flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    tilemap_set_flip(K007342_tilemap[1], K007342_flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    break;
                case 0x01:  /* used for banking in Rock'n'Rage */

                    if (data != K007342_regs[1]) {
                        tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                    }
                case 0x02:
                    K007342_scrollx[0] = (K007342_scrollx[0] & 0xff) | ((data & 0x01) << 8);
                    K007342_scrollx[1] = (K007342_scrollx[1] & 0xff) | ((data & 0x02) << 7);
                    break;
                case 0x03:  /* scroll x (register 0) */

                    K007342_scrollx[0] = (K007342_scrollx[0] & 0x100) | data;
                    break;
                case 0x04:  /* scroll y (register 0) */

                    K007342_scrolly[0] = data;
                    break;
                case 0x05:  /* scroll x (register 1) */

                    K007342_scrollx[1] = (K007342_scrollx[1] & 0x100) | data;
                    break;
                case 0x06:  /* scroll y (register 1) */

                    K007342_scrolly[1] = data;
                case 0x07:  /* unused */

                    break;
            }
            K007342_regs[offset] = data;
        }
    };

    public static void K007342_tilemap_update() {
        int offs;

        /* update scroll */
        switch (K007342_regs[2] & 0x1c) {
            case 0x00:
            case 0x08:	/* unknown, blades of steel shootout between periods */

                tilemap_set_scroll_rows(K007342_tilemap[0], 1);
                tilemap_set_scroll_cols(K007342_tilemap[0], 1);
                tilemap_set_scrollx(K007342_tilemap[0], 0, K007342_scrollx[0]);
                tilemap_set_scrolly(K007342_tilemap[0], 0, K007342_scrolly[0]);
                break;

            case 0x0c:	/* 32 columns */

                tilemap_set_scroll_rows(K007342_tilemap[0], 1);
                tilemap_set_scroll_cols(K007342_tilemap[0], 512);
                tilemap_set_scrollx(K007342_tilemap[0], 0, K007342_scrollx[0]);
                for (offs = 0; offs < 256; offs++) {
                    tilemap_set_scrolly(K007342_tilemap[0], (offs + K007342_scrollx[0]) & 0x1ff,
                            K007342_scroll_ram.read(2 * (offs / 8)) + 256 * K007342_scroll_ram.read(2 * (offs / 8) + 1));
                }
                break;

            case 0x14:	/* 256 rows */

                tilemap_set_scroll_rows(K007342_tilemap[0], 256);
                tilemap_set_scroll_cols(K007342_tilemap[0], 1);
                tilemap_set_scrolly(K007342_tilemap[0], 0, K007342_scrolly[0]);
                for (offs = 0; offs < 256; offs++) {
                    tilemap_set_scrollx(K007342_tilemap[0], (offs + K007342_scrolly[0]) & 0xff,
                            K007342_scroll_ram.read(2 * offs) + 256 * K007342_scroll_ram.read(2 * offs + 1));
                }
                break;

            default:
                break;
        }

        tilemap_set_scrollx(K007342_tilemap[1], 0, K007342_scrollx[1]);
        tilemap_set_scrolly(K007342_tilemap[1], 0, K007342_scrolly[1]);

        /* update all layers */
        tilemap_0_preupdate();
        tilemap_update(K007342_tilemap[0]);
        tilemap_1_preupdate();
        tilemap_update(K007342_tilemap[1]);

    }

    public static WriteHandlerPtr K007342_tilemap_set_enable = new WriteHandlerPtr() {
        public void handler(int tilemap, int enable) {
            tilemap_set_enable(K007342_tilemap[tilemap], enable);
        }
    };

    public static void K007342_tilemap_draw(osd_bitmap bitmap, int num, int flags) {
        tilemap_draw(bitmap, K007342_tilemap[num], flags);
    }

    public static int K007342_is_INT_enabled() {
        return K007342_int_enabled;
    }
}
