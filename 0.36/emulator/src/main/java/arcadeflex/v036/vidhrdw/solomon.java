/**
 * ported to v0.36
 *
 */
/**
 * Changelog
 * =========
 * 21/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.paletteH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_transparent_pen;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_used_colors;

public class solomon {

    public static UBytePtr solomon_bgvideoram = new UBytePtr();
    public static UBytePtr solomon_bgcolorram = new UBytePtr();

    static osd_bitmap tmpbitmap2;
    static /*unsigned*/ char[] dirtybuffer2;
    static int flipscreen;

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr solomon_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            int i;

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                generic_vh_stop.handler();
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                osd_free_bitmap(tmpbitmap2);
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            /* leave everything at the default, but map all foreground 0 pens as transparent */
            for (i = 0; i < 8; i++) {
                palette_used_colors.write(16 * i, PALETTE_COLOR_TRANSPARENT);
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopHandlerPtr solomon_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr solomon_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (solomon_bgvideoram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                solomon_bgvideoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr solomon_bgcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (solomon_bgcolorram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                solomon_bgcolorram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr solomon_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
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
    public static VhUpdateHandlerPtr solomon_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* recalc the palette if necessary */
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
            }

            for (offs = 0; offs < videoram_size[0]; offs++) {
                if (dirtybuffer2[offs] != 0) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer2[offs] = 0;
                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = solomon_bgcolorram.read(offs) & 0x80;
                    flipy = solomon_bgcolorram.read(offs) & 0x08;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            solomon_bgvideoram.read(offs) + 256 * (solomon_bgcolorram.read(offs) & 0x07),
                            ((solomon_bgcolorram.read(offs) & 0x70) >> 4),
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the frontmost playfield */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;
                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 256 * (colorram.read(offs) & 0x07),
                            (colorram.read(offs) & 0x70) >> 4,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            /* draw sprites */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 3);
                sy = 241 - spriteram.read(offs + 2);
                flipx = spriteram.read(offs + 1) & 0x40;
                flipy = spriteram.read(offs + 1) & 0x80;
                if ((flipscreen & 1) != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[2],
                        spriteram.read(offs) + 16 * (spriteram.read(offs + 1) & 0x10),
                        (spriteram.read(offs + 1) & 0x0e) >> 1,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
