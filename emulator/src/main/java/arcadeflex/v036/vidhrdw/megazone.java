/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_new_bitmap;

public class megazone {

    public static UBytePtr megazone_scrollx = new UBytePtr();
    public static UBytePtr megazone_scrolly = new UBytePtr();
    static int flipscreen;

    public static UBytePtr megazone_videoram2 = new UBytePtr();
    public static UBytePtr megazone_colorram2 = new UBytePtr();
    public static int[] megazone_videoram2_size = new int[1];

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Megazone has one 32x8 palette PROM and two 256x4 lookup table PROMs (one
     * for characters, one for sprites). The palette PROM is connected to the
     * RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr megazone_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
 /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }
        }
    };

    public static WriteHandlerPtr megazone_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static VhStartHandlerPtr megazone_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            dirtybuffer = null;
            tmpbitmap = null;

            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            if ((tmpbitmap = osd_new_bitmap(256, 256, Machine.scrbitmap.depth)) == null) {
                dirtybuffer = null;
                return 1;
            }

            return 0;
        }
    };

    public static VhStopHandlerPtr megazone_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);

            dirtybuffer = null;
            tmpbitmap = null;
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
    public static VhUpdateHandlerPtr megazone_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int x, y;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = colorram.read(offs) & (1 << 6);
                    flipy = colorram.read(offs) & (1 << 5);
                    //			if (flipscreen != 0)
                    //			{
                    //				sx = 31 - sx;
                    //				sy = 31 - sy;
                    //				flipx = !flipx;
                    //				flipy = !flipy;
                    //			}

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            ((int) videoram.read(offs)) + (((colorram.read(offs) & (1 << 7)) != 0 ? 256 : 0)),
                            (colorram.read(offs) & 0x0f) + 0x10,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx = -megazone_scrolly.read() + 4 * 8;
                int scrolly = -megazone_scrollx.read();

                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            {
                for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                    int sx, sy, flipx, flipy;

                    sx = spriteram.read(offs + 3) + 4 * 8;
                    sy = 255 - ((spriteram.read(offs + 1) + 16) & 0xff);

                    flipx = ~spriteram.read(offs + 0) & 0x40;
                    flipy = spriteram.read(offs + 0) & 0x80;

                    //			if (flipscreen != 0)
                    //			{
                    //				sx = 240 - sx;
                    //				sy = 240 - sy;
                    //				flipx = !flipx;
                    //				flipy = !flipy;
                    //			}
                    drawgfx(bitmap, Machine.gfx[1],
                            spriteram.read(offs + 2),
                            spriteram.read(offs + 0) & 0x0f,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);
                }
            }

            for (y = 0; y < 32; y++) {
                offs = y * 32;
                for (x = 0; x < 6; x++) {
                    int sx, sy, flipx, flipy;

                    sx = x;
                    sy = y;

                    flipx = megazone_colorram2.read(offs) & (1 << 6);
                    flipy = megazone_colorram2.read(offs) & (1 << 5);
                    drawgfx(bitmap, Machine.gfx[0],
                            ((int) megazone_videoram2.read(offs)) + (((megazone_colorram2.read(offs) & (1 << 7)) != 0 ? 256 : 0)),
                            (megazone_colorram2.read(offs) & 0x0f) + 0x10,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                    offs++;
                }
            }
        }
    };
}
