/*
 * ported to v0.36
 *
 *  THIS FILE IS 100% PORTED!!!
 *
 */
package vidhrdw;

/**
 *
 * @author shadow
 */
import static arcadeflex.ptrlib.*;
import static arcadeflex.libc.*;
import static mame.driverH.*;
import static vidhrdw.generic.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.drawgfx.*;

public class minivadr {

    /**
     * *****************************************************************
     *
     * Palette Setting.
     *
     ******************************************************************
     */
    static char minivadr_palette[]
            = {
                0x00, 0x00, 0x00, /* black */
                0xff, 0xff, 0xff /* white */};

    public static VhConvertColorPromPtr minivadr_init_palette = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {

            //memcpy(game_palette, minivadr_palette, sizeof(minivadr_palette));
            for (int i = 0; i < minivadr_palette.length; i++) {
                palette[i].set(minivadr_palette[i]);
            }
        }
    };

    /**
     * *****************************************************************
     *
     * Draw Pixel.
     *
     ******************************************************************
     */
    public static WriteHandlerPtr minivadr_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;
            int x, y;
            int color;

            videoram.write(offset, data);

            x = (offset % 32) * 8;
            y = (offset / 32);

            if (x >= Machine.drv.visible_area.min_x
                    && x <= Machine.drv.visible_area.max_x
                    && y >= Machine.drv.visible_area.min_y
                    && y <= Machine.drv.visible_area.max_y) {
                for (i = 0; i < 8; i++) {
                    color = Machine.pens[((data >> i) & 0x01)];

                    plot_pixel.handler(Machine.scrbitmap, x + (7 - i), y, color);
                }
            }
        }
    };

    public static VhUpdatePtr minivadr_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (full_refresh != 0) {
                int offs;

                /* redraw bitmap */
                for (offs = 0; offs < videoram_size[0]; offs++) {
                    minivadr_videoram_w.handler(offs, videoram.read(offs));
                }
            }
        }
    };

}
