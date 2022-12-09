/*
 * ported to v0.36
 * 
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_new_bitmap;

public class ninjakd2 {

    /*#define COLORTABLE_START(gfxn,color)	Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + color * Machine.gfx[gfxn].color_granularity
	#define GFX_COLOR_CODES(gfxn) 		Machine.gfx[gfxn].total_colors
	#define GFX_ELEM_COLORS(gfxn) 		Machine.gfx[gfxn].color_granularity
     */
    public static UBytePtr ninjakd2_scrolly_ram = new UBytePtr();
    public static UBytePtr ninjakd2_scrollx_ram = new UBytePtr();
    public static UBytePtr ninjakd2_bgenable_ram = new UBytePtr();
    public static UBytePtr ninjakd2_spoverdraw_ram = new UBytePtr();
    public static UBytePtr ninjakd2_spriteram = new UBytePtr();
    public static int[] ninjakd2_spriteram_size = new int[1];
    public static UBytePtr ninjakd2_background_videoram = new UBytePtr();
    public static int[] ninjakd2_backgroundram_size = new int[1];
    public static UBytePtr ninjakd2_foreground_videoram = new UBytePtr();
    public static int[] ninjakd2_foregroundram_size = new int[1];

    static osd_bitmap bitmap_bg;
    static osd_bitmap bitmap_sp;

    static char[] bg_dirtybuffer;
    static int bg_enable = 1;
    static int sp_overdraw = 0;

    public static VhStartHandlerPtr ninjakd2_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            int i;

            if ((bg_dirtybuffer = new char[1024]) == null) {
                return 1;
            }
            if ((bitmap_bg = osd_new_bitmap(Machine.drv.screen_width * 2, Machine.drv.screen_height * 2, Machine.scrbitmap.depth)) == null) {
                bg_dirtybuffer = null;
                return 1;
            }
            if ((bitmap_sp = osd_new_bitmap(Machine.drv.screen_width, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                bg_dirtybuffer = null;
                bitmap_bg = null;
                return 1;
            }
            memset(bg_dirtybuffer, 1, 1024);

            /* chars, background tiles, sprites */
            memset(palette_used_colors, PALETTE_COLOR_USED, Machine.drv.total_colors);

            for (i = 0; i < Machine.gfx[1].total_colors; i++) {
                palette_used_colors.write((Machine.drv.gfxdecodeinfo[1].color_codes_start + i * Machine.gfx[1].color_granularity) + 15, PALETTE_COLOR_TRANSPARENT);
                palette_used_colors.write((Machine.drv.gfxdecodeinfo[2].color_codes_start + i * Machine.gfx[2].color_granularity) + 15, PALETTE_COLOR_TRANSPARENT);
            }
            return 0;
        }
    };

    public static VhStopHandlerPtr ninjakd2_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(bitmap_bg);
            osd_free_bitmap(bitmap_sp);
            bg_dirtybuffer = null;
        }
    };

    public static WriteHandlerPtr ninjakd2_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (ninjakd2_background_videoram.read(offset) != data) {
                bg_dirtybuffer[offset >> 1] = 1;
                ninjakd2_background_videoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr ninjakd2_fgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (ninjakd2_foreground_videoram.read(offset) != data) {
                ninjakd2_foreground_videoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr ninjakd2_background_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (bg_enable != data) {
                ninjakd2_bgenable_ram.write(offset, data);
                bg_enable = data;
                if (bg_enable != 0) {
                    memset(bg_dirtybuffer, 1, ninjakd2_backgroundram_size[0] / 2);
                } else {
                    fillbitmap(bitmap_bg, palette_transparent_pen, null);
                }
            }
        }
    };

    public static WriteHandlerPtr ninjakd2_sprite_overdraw_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (sp_overdraw != data) {
                ninjakd2_spoverdraw_ram.write(offset, data);
                fillbitmap(bitmap_sp, 15, Machine.drv.visible_area);
                sp_overdraw = data;
            }
        }
    };

    public static void ninjakd2_draw_foreground(osd_bitmap bitmap) {
        int offs;

        /* Draw the foreground text */
        for (offs = 0; offs < ninjakd2_foregroundram_size[0] / 2; offs++) {
            int sx, sy, tile, palette, flipx, flipy, lo, hi;

            if ((ninjakd2_foreground_videoram.read(offs * 2) | ninjakd2_foreground_videoram.read(offs * 2 + 1)) != 0) {
                sx = (offs % 32) << 3;
                sy = (offs >> 5) << 3;

                lo = ninjakd2_foreground_videoram.read(offs * 2);
                hi = ninjakd2_foreground_videoram.read(offs * 2 + 1);
                tile = ((hi & 0xc0) << 2) | lo;
                flipx = hi & 0x20;
                flipy = hi & 0x10;
                palette = hi & 0x0f;

                drawgfx(bitmap, Machine.gfx[2],
                        tile,
                        palette,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
            }

        }
    }

    public static void ninjakd2_draw_background(osd_bitmap bitmap) {
        int offs;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (offs = 0; offs < ninjakd2_backgroundram_size[0] / 2; offs++) {
            int sx, sy, tile, palette, flipx, flipy, lo, hi;

            if (bg_dirtybuffer[offs] != 0) {
                sx = (offs % 32) << 4;
                sy = (offs >> 5) << 4;

                bg_dirtybuffer[offs] = 0;

                lo = ninjakd2_background_videoram.read(offs * 2);
                hi = ninjakd2_background_videoram.read(offs * 2 + 1);
                tile = ((hi & 0xc0) << 2) | lo;
                flipx = hi & 0x20;
                flipy = hi & 0x10;
                palette = hi & 0x0f;
                drawgfx(bitmap, Machine.gfx[0],
                        tile,
                        palette,
                        flipx, flipy,
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);
            }

        }
    }

    public static void ninjakd2_draw_sprites(osd_bitmap bitmap) {
        int offs;

        /* Draw the sprites */
        for (offs = 11; offs < ninjakd2_spriteram_size[0]; offs += 16) {
            int sx, sy, tile, palette, flipx, flipy;

            if ((ninjakd2_spriteram.read(offs + 2) & 2) != 0) {
                sx = ninjakd2_spriteram.read(offs + 1);
                sy = ninjakd2_spriteram.read(offs);
                if ((ninjakd2_spriteram.read(offs + 2) & 1) != 0) {
                    sx -= 256;
                }
                tile = ninjakd2_spriteram.read(offs + 3) + ((ninjakd2_spriteram.read(offs + 2) & 0xc0) << 2);
                flipx = ninjakd2_spriteram.read(offs + 2) & 0x10;
                flipy = ninjakd2_spriteram.read(offs + 2) & 0x20;
                palette = ninjakd2_spriteram.read(offs + 4) & 0x0f;
                drawgfx(bitmap, Machine.gfx[1],
                        tile,
                        palette,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area,
                        TRANSPARENCY_PEN, 15);
            }
        }
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr ninjakd2_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int scrollx, scrolly;

            if (palette_recalc() != null) {
                memset(bg_dirtybuffer, 1, ninjakd2_backgroundram_size[0] / 2);
            }

            if (bg_enable != 0) {
                ninjakd2_draw_background(bitmap_bg);
            }

            scrollx = -((ninjakd2_scrollx_ram.read(0) + ninjakd2_scrollx_ram.read(1) * 256) & 0x1FF);
            scrolly = -((ninjakd2_scrolly_ram.read(0) + ninjakd2_scrolly_ram.read(1) * 256) & 0x1FF);

            if (sp_overdraw != 0) /* overdraw sprite mode */ {
                ninjakd2_draw_sprites(bitmap_sp);
                ninjakd2_draw_foreground(bitmap_sp);
                copyscrollbitmap(bitmap, bitmap_bg, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                copybitmap(bitmap, bitmap_sp, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
            } else /* normal sprite mode */ {
                copyscrollbitmap(bitmap, bitmap_bg, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                ninjakd2_draw_sprites(bitmap);
                ninjakd2_draw_foreground(bitmap);
            }

        }
    };
}
