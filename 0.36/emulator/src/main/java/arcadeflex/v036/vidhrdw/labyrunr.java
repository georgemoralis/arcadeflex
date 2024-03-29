/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 16/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.konamiic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class labyrunr {

    public static UBytePtr labyrunr_videoram1 = new UBytePtr();
    public static UBytePtr labyrunr_videoram2 = new UBytePtr();
    static tilemap layer0, layer1;

    public static VhConvertColorPromHandlerPtr labyrunr_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i, pal;
            int ptr = 0;
            for (pal = 0; pal < 8; pal++) {
                if ((pal & 1) != 0) /* chars, no lookup table */ {
                    for (i = 0; i < 256; i++) {
                        colortable[ptr++] = (char) (16 * pal + (i & 0x0f));
                    }
                } else /* sprites */ {
                    for (i = 0; i < 256; i++) {
                        if (color_prom.read(i) == 0) {
                            colortable[ptr++] = (char) (0);
                        } else {
                            colortable[ptr++] = (char) (16 * pal + color_prom.read(i));
                        }
                    }
                }
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
    public static WriteHandlerPtr get_tile_info0 = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 32 * row + col;
            int attr = labyrunr_videoram1.read(tile_index);
            int code = labyrunr_videoram1.read(tile_index + 0x400);
            int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
            int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
            int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
            int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
            int bank = ((attr & 0x80) >> 7)
                    | ((attr >> (bit0 + 2)) & 0x02)
                    | ((attr >> (bit1 + 1)) & 0x04)
                    | ((attr >> (bit2)) & 0x08)
                    | ((attr >> (bit3 - 1)) & 0x10)
                    | ((K007121_ctrlram[0][0x03] & 0x01) << 5);
            int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;

            bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);

            SET_TILE_INFO(0, code + bank * 256, ((K007121_ctrlram[0][6] & 0x30) * 2 + 16) + (attr & 7));
        }
    };

    public static WriteHandlerPtr get_tile_info1 = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index = 32 * row + col;
            int attr = labyrunr_videoram2.read(tile_index);
            int code = labyrunr_videoram2.read(tile_index + 0x400);
            int bit0 = (K007121_ctrlram[0][0x05] >> 0) & 0x03;
            int bit1 = (K007121_ctrlram[0][0x05] >> 2) & 0x03;
            int bit2 = (K007121_ctrlram[0][0x05] >> 4) & 0x03;
            int bit3 = (K007121_ctrlram[0][0x05] >> 6) & 0x03;
            int bank = ((attr & 0x80) >> 7)
                    | ((attr >> (bit0 + 2)) & 0x02)
                    | ((attr >> (bit1 + 1)) & 0x04)
                    | ((attr >> (bit2)) & 0x08)
                    | ((attr >> (bit3 - 1)) & 0x10)
                    | ((K007121_ctrlram[0][0x03] & 0x01) << 5);
            int mask = (K007121_ctrlram[0][0x04] & 0xf0) >> 4;

            bank = (bank & ~(mask << 1)) | ((K007121_ctrlram[0][0x04] & mask) << 1);

            SET_TILE_INFO(0, code + bank * 256, ((K007121_ctrlram[0][6] & 0x30) * 2 + 16) + (attr & 7));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr labyrunr_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            layer0 = tilemap_create(get_tile_info0, TILEMAP_OPAQUE, 8, 8, 32, 32);
            layer1 = tilemap_create(get_tile_info1, TILEMAP_OPAQUE, 8, 8, 5, 32);

            if (layer0 != null && layer1 != null) {
                rectangle clip = new rectangle(Machine.drv.visible_area);
                clip.min_x += 40;
                tilemap_set_clip(layer0, clip);

                clip.max_x = 39;
                clip.min_x = 0;
                tilemap_set_clip(layer1, clip);

                return 0;
            }
            return 1;
        }
    };

    /**
     * *************************************************************************
     *
     * Memory Handlers
     *
     **************************************************************************
     */
    public static WriteHandlerPtr labyrunr_vram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (labyrunr_videoram1.read(offset) != data) {
                tilemap_mark_tile_dirty(layer0, offset % 32, (offset & 0x3ff) / 32);
                labyrunr_videoram1.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr labyrunr_vram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (labyrunr_videoram2.read(offset) != data) {
                int col = offset % 32;
                if (col < 5) {
                    tilemap_mark_tile_dirty(layer1, col, (offset & 0x3ff) / 32);
                }
                labyrunr_videoram2.write(offset, data);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Screen Refresh
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr labyrunr_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            tilemap_set_scrollx(layer0, 0, K007121_ctrlram[0][0x00] - 40);
            tilemap_set_scrolly(layer0, 0, K007121_ctrlram[0][0x02]);

            tilemap_update(ALL_TILEMAPS);
            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }
            tilemap_render(ALL_TILEMAPS);

            tilemap_draw(bitmap, layer0, 0);
            K007121_sprites_draw(0, bitmap, spriteram, (K007121_ctrlram[0][6] & 0x30) * 2, 40, 0);
            tilemap_draw(bitmap, layer1, 0);
        }
    };
}
