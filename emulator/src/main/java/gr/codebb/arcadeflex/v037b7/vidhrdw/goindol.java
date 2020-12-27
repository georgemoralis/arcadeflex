/**
 * ported to v0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;

public class goindol {

    public static UBytePtr goindol_bg_videoram = new UBytePtr();
    public static UBytePtr goindol_fg_videoram = new UBytePtr();
    public static UBytePtr goindol_spriteram1 = new UBytePtr();
    public static UBytePtr goindol_spriteram2 = new UBytePtr();
    public static UBytePtr goindol_fg_scrollx = new UBytePtr();
    public static UBytePtr goindol_fg_scrolly = new UBytePtr();
    static osd_bitmap bitmap_bg;
    static osd_bitmap bitmap_fg;
    static char[] fg_dirtybuffer;
    static char[] bg_dirtybuffer;

    public static int[] goindol_fg_videoram_size = new int[1];
    public static int[] goindol_bg_videoram_size = new int[1];
    public static int[] goindol_spriteram_size = new int[1];
    public static int goindol_char_bank;

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
    public static VhConvertColorPromPtr goindol_vh_convert_color_prom = new VhConvertColorPromPtr() {
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

            /* characters */
            for (i = 0; i < 256; i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) i;
            }
        }
    };

    public static WriteHandlerPtr goindol_fg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (goindol_fg_videoram.read(offset) != data) {
                fg_dirtybuffer[offset >> 1] = 1;
                goindol_fg_videoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr goindol_bg_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (goindol_bg_videoram.read(offset) != data) {
                bg_dirtybuffer[offset >> 1] = 1;
                goindol_bg_videoram.write(offset, data);
            }
        }
    };

    public static VhStartPtr goindol_vh_start = new VhStartPtr() {
        public int handler() {
            if ((fg_dirtybuffer = new char[32 * 32]) == null) {
                return 1;
            }
            if ((bg_dirtybuffer = new char[32 * 32]) == null) {
                bg_dirtybuffer = null;
                return 1;
            }
            if ((bitmap_fg = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                fg_dirtybuffer = null;
                bg_dirtybuffer = null;
                return 1;
            }
            if ((bitmap_bg = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                bitmap_free(bitmap_fg);
                fg_dirtybuffer = null;
                bg_dirtybuffer = null;
                return 1;
            }
            memset(fg_dirtybuffer, 1, 32 * 32);
            memset(bg_dirtybuffer, 1, 32 * 32);
            return 0;
        }
    };

    public static VhStopPtr goindol_vh_stop = new VhStopPtr() {
        public void handler() {
            fg_dirtybuffer = null;
            bg_dirtybuffer = null;
            bitmap_free(bitmap_fg);
            bitmap_free(bitmap_bg);
        }
    };

    static void goindol_draw_background(osd_bitmap bitmap) {
        int x, y, offs;
        int sx, sy, tile, palette, lo, hi;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (x = 0; x < 32; x++) {
            for (y = 0; y < 32; y++) {
                offs = y * 64 + (x * 2);
                if (bg_dirtybuffer[offs >> 1] != 0) {
                    sx = x << 3;
                    sy = y << 3;

                    bg_dirtybuffer[offs >> 1] = 0;

                    hi = goindol_bg_videoram.read(offs);
                    lo = goindol_bg_videoram.read(offs + 1);
                    tile = ((hi & 0x7) << 8) | lo;
                    palette = hi >> 3;
                    drawgfx(bitmap, Machine.gfx[1],
                            tile,
                            palette,
                            0, 0,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }
        }
    }

    static void goindol_draw_foreground(osd_bitmap bitmap) {
        int x, y, offs;
        int sx, sy, tile, palette, lo, hi;

        for (x = 0; x < 32; x++) {
            for (y = 0; y < 32; y++) {
                offs = y * 64 + (x * 2);
                if (fg_dirtybuffer[offs >> 1] != 0) {
                    sx = x << 3;
                    sy = y << 3;

                    fg_dirtybuffer[offs >> 1] = 0;

                    hi = goindol_fg_videoram.read(offs);
                    lo = goindol_fg_videoram.read(offs + 1);
                    tile = ((hi & 0x7) << 8) | lo;
                    palette = hi >> 3;
                    drawgfx(bitmap, Machine.gfx[0],
                            tile + (goindol_char_bank << 7),
                            palette,
                            0, 0,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }
        }

    }

    static void goindol_draw_sprites(osd_bitmap bitmap, int gfxbank, UBytePtr sprite_ram) {
        int offs, sx, sy, tile, palette;

        for (offs = 0; offs < goindol_spriteram_size[0]; offs += 4) {
            sx = sprite_ram.read(offs);
            sy = 240 - sprite_ram.read(offs + 1);

            if ((sprite_ram.read(offs + 1) >> 3) != 0 && (sx < 248)) {
                tile = ((sprite_ram.read(offs + 3)) + ((sprite_ram.read(offs + 2) & 7) << 8));
                tile += tile;
                palette = sprite_ram.read(offs + 2) >> 3;

                drawgfx(bitmap, Machine.gfx[gfxbank],
                        tile,
                        palette,
                        0, 0,
                        sx, sy,
                        Machine.visible_area,
                        TRANSPARENCY_PEN, 0);
                drawgfx(bitmap, Machine.gfx[gfxbank],
                        tile + 1,
                        palette,
                        0, 0,
                        sx, sy + 8,
                        Machine.visible_area,
                        TRANSPARENCY_PEN, 0);
            }
        }
    }

    public static VhUpdatePtr goindol_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int fg_scrollx, fg_scrolly;

            fg_scrollx = -goindol_fg_scrollx.read();
            fg_scrolly = -goindol_fg_scrolly.read();

            goindol_draw_background(bitmap_bg);
            goindol_draw_foreground(bitmap_fg);
            copybitmap(bitmap, bitmap_bg, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
            copyscrollbitmap(bitmap, bitmap_fg, 1, new int[]{fg_scrolly}, 1, new int[]{fg_scrollx}, Machine.visible_area, TRANSPARENCY_COLOR, 0);
            goindol_draw_sprites(bitmap, 1, goindol_spriteram1);
            goindol_draw_sprites(bitmap, 0, goindol_spriteram2);
        }
    };
}
