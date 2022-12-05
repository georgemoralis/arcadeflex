/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//common imports
import static common.libc.cstring.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class vulgus {

    public static UBytePtr vulgus_bgvideoram = new UBytePtr();
    public static UBytePtr vulgus_bgcolorram = new UBytePtr();
    public static int[] vulgus_bgvideoram_size = new int[1];
    public static UBytePtr vulgus_scrolllow = new UBytePtr();
    public static UBytePtr vulgus_scrollhigh = new UBytePtr();
    public static UBytePtr vulgus_palette_bank = new UBytePtr();
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;

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
    public static VhConvertColorPromPtr vulgus_vh_convert_color_prom = new VhConvertColorPromPtr() {
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
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 32-47 (?) */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) + 32);
            }

            /* sprites use colors 16-31 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) ((color_prom.readinc()) + 16);
            }

            /* background tiles use colors 0-15, 64-79, 128-143, 192-207 in four banks */
            for (i = 0; i < TOTAL_COLORS(1) / 4; i++) {

                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = color_prom.read();
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 32 * 8] = (char) ((color_prom.read()) + 64);
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 2 * 32 * 8] = (char) ((color_prom.read()) + 128);
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 3 * 32 * 8] = (char) ((color_prom.read()) + 192);
                color_prom.inc();
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr vulgus_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[vulgus_bgvideoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, vulgus_bgvideoram_size[0]);

            /* the background area is twice as tall and twice as large as the screen */
            if ((tmpbitmap2 = osd_create_bitmap(2 * Machine.drv.screen_width, 2 * Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
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
    public static VhStopPtr vulgus_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr vulgus_bgvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (vulgus_bgvideoram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                vulgus_bgvideoram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr vulgus_bgcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (vulgus_bgcolorram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                vulgus_bgcolorram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr vulgus_palette_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (vulgus_palette_bank.read() != data) {
                memset(dirtybuffer2, 1, vulgus_bgvideoram_size[0]);
                vulgus_palette_bank.write(data);
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
    public static VhUpdatePtr vulgus_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int scrollx, scrolly;

            scrolly = -(vulgus_scrolllow.read(0) + 256 * vulgus_scrollhigh.read(0));
            scrollx = -(vulgus_scrolllow.read(1) + 256 * vulgus_scrollhigh.read(1));

            for (offs = vulgus_bgvideoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                if (dirtybuffer2[offs] != 0) {
                    //			int minx,maxx,miny,maxy;

                    sx = (offs % 32);
                    sy = (offs / 32);

                    /* between level Vulgus changes the palette bank every frame. Redrawing */
 /* the whole background every time would slow the game to a crawl, so here */
 /* we check and redraw only the visible tiles */
 /*
                     minx = (sx + scrollx) & 0x1ff;
                     maxx = (sx + 15 + scrollx) & 0x1ff;
                     if (minx > maxx) minx = maxx - 15;
                     miny = (sy + scrolly) & 0x1ff;
                     maxy = (sy + 15 + scrolly) & 0x1ff;
                     if (miny > maxy) miny = maxy - 15;
	
                     if (minx + 15 >= Machine.drv.visible_area.min_x &&
                     maxx - 15 <= Machine.drv.visible_area.max_x &&
                     miny + 15 >= Machine.drv.visible_area.min_y &&
                     maxy - 15 <= Machine.drv.visible_area.max_y)
                     */
                    {
                        dirtybuffer2[offs] = 0;

                        drawgfx(tmpbitmap2, Machine.gfx[1],
                                vulgus_bgvideoram.read(offs) + 2 * (vulgus_bgcolorram.read(offs) & 0x80),
                                (vulgus_bgcolorram.read(offs) & 0x1f) + 32 * vulgus_palette_bank.read(),
                                vulgus_bgcolorram.read(offs) & 0x20, vulgus_bgcolorram.read(offs) & 0x40,
                                16 * sy, 16 * sx,
                                null, TRANSPARENCY_NONE, 0);
                    }
                }
            }

            /* copy the background graphics */
            copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int code, i, col, sx, sy;

                code = spriteram.read(offs);
                col = spriteram.read(offs + 1) & 0x0f;
                sx = spriteram.read(offs + 3);
                sy = spriteram.read(offs + 2);

                i = (spriteram.read(offs + 1) & 0xc0) >> 6;
                if (i == 2) {
                    i = 3;
                }

                do {
                    drawgfx(bitmap, Machine.gfx[2],
                            code + i,
                            col,
                            0, 0,
                            sx, sy + 16 * i,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);

                    /* draw again with wraparound */
                    drawgfx(bitmap, Machine.gfx[2],
                            code + i,
                            col,
                            0, 0,
                            sx, sy + 16 * i - 256,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                    i--;
                } while (i >= 0);
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = 8 * (offs % 32);
                sy = 8 * (offs / 32);

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 2 * (colorram.read(offs) & 0x80),
                        colorram.read(offs) & 0x3f,
                        0, 0,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 47);
            }
        }
    };
}
