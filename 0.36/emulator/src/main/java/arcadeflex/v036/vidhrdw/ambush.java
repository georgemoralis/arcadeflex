/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 23/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.expressions.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.drawgfx;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.fillbitmap;

public class ambush {

    public static UBytePtr ambush_scrollram = new UBytePtr();
    public static UBytePtr ambush_colorbank = new UBytePtr();

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * I'm not sure about the resistor value, I'm using the Galaxian ones.
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr ambush_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            /* first, the char acter/sprite palette */
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
    static void draw_chars(osd_bitmap bitmap, int priority) {
        int offs, transparency;

        transparency = (priority == 0) ? TRANSPARENCY_NONE : TRANSPARENCY_PEN;

        for (offs = 0; offs < videoram_size[0]; offs++) {
            int code, sx, sy, col;
            int/*UINT8*/ u8_scroll;

            sy = (offs / 32);
            sx = (offs % 32);

            col = colorram.read(((sy & 0x1c) << 3) + sx);

            if ((col & 0x10) != priority) {
                continue;
            }

            u8_scroll = (~ambush_scrollram.read(sx)) & 0xFF;

            code = videoram.read(offs) | ((col & 0x60) << 3);

            if (flip_screen.read() != 0) {
                sx = 31 - sx;
                sy = 31 - sy;
                u8_scroll = (~u8_scroll - 1) & 0xFF;
            }

            drawgfx(bitmap, Machine.gfx[0],
                    code,
                    (col & 0x0f) | ((ambush_colorbank.read() & 0x03) << 4),
                    flip_screen.read(), flip_screen.read(),
                    8 * sx, (8 * sy + u8_scroll) & 0xff,
                    Machine.drv.visible_area, transparency, 0);
        }
    }

    public static VhUpdateHandlerPtr ambush_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);

            /* Draw the background priority characters */
            draw_chars(bitmap, 0x00);

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int code, col, sx, sy, flipx, flipy, gfx;

                sy = spriteram.read(offs + 0);
                sx = spriteram.read(offs + 3);

                if ((sy == 0)
                        || (sy == 0xff)
                        || ((sx < 0x40) && (spriteram.read(offs + 2) & 0x10) != 0)
                        || ((sx >= 0xc0) && ((spriteram.read(offs + 2) & 0x10)) == 0)) {
                    continue;
                    /* prevent wraparound */
                }

                code = (spriteram.read(offs + 1) & 0x3f) | ((spriteram.read(offs + 2) & 0x60) << 1);

                if ((spriteram.read(offs + 2) & 0x80) != 0) {
                    /* 16x16 sprites */
                    gfx = 1;

                    if (flip_screen.read() == 0) {
                        sy = 240 - sy;
                    } else {
                        sx = 240 - sx;
                    }
                } else {
                    /* 8x8 sprites */
                    gfx = 0;
                    code <<= 2;

                    if (flip_screen.read() == 0) {
                        sy = 248 - sy;
                    } else {
                        sx = 248 - sx;
                    }
                }

                col = spriteram.read(offs + 2) & 0x0f;
                flipx = spriteram.read(offs + 1) & 0x40;
                flipy = spriteram.read(offs + 1) & 0x80;

                if (flip_screen.read() != 0) {
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                drawgfx(bitmap, Machine.gfx[gfx],
                        code, col | ((ambush_colorbank.read() & 0x03) << 4),
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* Draw the foreground priority characters */
            draw_chars(bitmap, 0x10);
        }
    };
}
