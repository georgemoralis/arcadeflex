/*
 * ported to v0.36
 */
package arcadeflex.v036.vidhrdw;

//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;

public class tp84 {

    public static UBytePtr tp84_videoram2 = new UBytePtr();
    public static UBytePtr tp84_colorram2 = new UBytePtr();
    static osd_bitmap tmpbitmap2;
    static char[] dirtybuffer2;

    public static UBytePtr tp84_scrollx = new UBytePtr();
    public static UBytePtr tp84_scrolly = new UBytePtr();

    static int col0;

    static rectangle topvisiblearea = new rectangle(
            0 * 8, 2 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );
    static rectangle bottomvisiblearea = new rectangle(
            30 * 8, 32 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr tp84_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                /* red component */
                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));
                /* green component */
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));
                /* blue component */
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x42 * bit2 + 0x90 * bit3));

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 128-255 */
            for (i = 0; i < TOTAL_COLORS(0) / 8; i++) {
                int j;

                for (j = 0; j < 8; j++) {
                    //COLOR(0,i+256*j) = *color_prom + 128 + 16*j;
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i + 256 * j] = (char) (color_prom.read() + 128 + 16 * j);
                }
                color_prom.inc();
            }

            /* sprites use colors 0-127 */
            for (i = 0; i < TOTAL_COLORS(1) / 8; i++) {
                int j;

                for (j = 0; j < 8; j++) {
                    if (color_prom.read() != 0) {
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 256 * j] = (char) (color_prom.read() + 16 * j);

                    } else {
                        colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i + 256 * j] = (char) (0);
                    }
                    /* preserve transparency */

                }

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
    public static VhStartPtr tp84_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
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
    public static VhStopPtr tp84_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer2 = null;
            osd_free_bitmap(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr tp84_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (tp84_videoram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                tp84_videoram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr tp84_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (tp84_colorram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                tp84_colorram2.write(offset, data);
            }
        }
    };

    /**
     * ***
     * col0 is a register to index the color Proms ***
     */
    public static WriteHandlerPtr tp84_col0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (col0 != data) {
                col0 = data;

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
    public static VhUpdatePtr tp84_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int coloffset;

            coloffset = ((col0 & 0x18) << 1) + ((col0 & 0x07) << 6);

            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x30) << 4),
                            (colorram.read(offs) & 0x0f) + coloffset,
                            colorram.read(offs) & 0x40, colorram.read(offs) & 0x80,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }

                if (dirtybuffer2[offs] != 0) {
                    int sx, sy;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    /* Skip the middle of the screen, this ram seem to be used as normal ram. */
                    if (sx < 2 || sx >= 30) {
                        drawgfx(tmpbitmap2, Machine.gfx[0],
                                tp84_videoram2.read(offs) + ((tp84_colorram2.read(offs) & 0x30) << 4),
                                (tp84_colorram2.read(offs) & 0x0f) + coloffset,
                                tp84_colorram2.read(offs) & 0x40, tp84_colorram2.read(offs) & 0x80,
                                8 * sx, 8 * sy,
                                Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                scrollx = -tp84_scrollx.read();
                scrolly = -tp84_scrolly.read();

                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            coloffset = ((col0 & 0x07) << 4);
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy, flipx, flipy;

                sx = spriteram.read(offs + 0);
                sy = 240 - spriteram.read(offs + 3);
                flipx = NOT(spriteram.read(offs + 2) & 0x40);
                flipy = spriteram.read(offs + 2) & 0x80;

                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs + 1),
                        (spriteram.read(offs + 2) & 0x0f) + coloffset,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);
            }

            /* Copy the frontmost playfield. */
            copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, topvisiblearea, TRANSPARENCY_NONE, 0);
            copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, bottomvisiblearea, TRANSPARENCY_NONE, 0);
        }
    };
}
