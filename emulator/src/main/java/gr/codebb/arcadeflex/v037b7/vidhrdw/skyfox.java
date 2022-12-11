/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.expressions.*;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static arcadeflex.v036.mame.commonH.REGION_GFX2;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.platform.video.osd_clearbitmap;
import static arcadeflex.v036.vidhrdw.generic.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
public class skyfox {

    /* Variables only used here: */
    static /*unsigned*/ char[] vreg = new char[8];

    /* Variables that driver has access to: */
    public static int skyfox_bg_pos, skyfox_bg_ctrl;

    /**
     * *************************************************************************
     *
     * Memory Handlers
     *
     **************************************************************************
     */
    public static ReadHandlerPtr skyfox_vregs_r = new ReadHandlerPtr() {
        public int handler(int offset) // for debug
        {
            return vreg[offset] & 0xFF;
        }
    };

    public static WriteHandlerPtr skyfox_vregs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            vreg[offset] = (char) (data & 0xFF);

            switch (offset) {
                case 0:
                    skyfox_bg_ctrl = data;
                    break;
                case 1:
                    soundlatch_w.handler(0, data);
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * There are three 256x4 palette PROMs (one per gun). I don't know the exact
     * values of the resistors between the RAM and the RGB output. I assumed
     * these values (the same as Commando)
     *
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr skyfox_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < 256; i++) {
                int bit0, bit1, bit2, bit3;

                /* red component */
                bit0 = (color_prom.read(256 * 0) >> 0) & 0x01;
                bit1 = (color_prom.read(256 * 0) >> 1) & 0x01;
                bit2 = (color_prom.read(256 * 0) >> 2) & 0x01;
                bit3 = (color_prom.read(256 * 0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                /* green component */
                bit0 = (color_prom.read(256 * 1) >> 0) & 0x01;
                bit1 = (color_prom.read(256 * 1) >> 1) & 0x01;
                bit2 = (color_prom.read(256 * 1) >> 2) & 0x01;
                bit3 = (color_prom.read(256 * 1) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                /* blue component */
                bit0 = (color_prom.read(256 * 2) >> 0) & 0x01;
                bit1 = (color_prom.read(256 * 2) >> 1) & 0x01;
                bit2 = (color_prom.read(256 * 2) >> 2) & 0x01;
                bit3 = (color_prom.read(256 * 2) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }

            /* Grey scale for the background??? */
            for (i = 0; i < 256; i++) {
                palette[p_inc++] = ((char) (i));
                palette[p_inc++] = ((char) (i));
                palette[p_inc++] = ((char) (i));
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Sprites Drawing
     *
     * Offset:	Value:
     *
     * 03	Code: selects one of the 32x32 tiles in the ROMs. (Tiles $80-ff are
     * bankswitched to cover $180 tiles)
     *
     * 02	Code + Attr
     *
     * 7654 ----	Code (low 4 bits) 8x8 sprites use bits 7654	(since there are 16
     * 8x8 tiles in the 32x32 one) 16x16 sprites use bits --54 (since there are
     * 4 16x16 tiles in the 32x32 one) 32x32 sprites use no bits	(since the
     * 32x32 tile is already selected)
     *
     * 7--- 3---	Size 1--- 1--- : 32x32 sprites 0--- 1--- : 16x16 sprites 8x8
     * sprites otherwise
     *
     * ---- -2--	Flip Y ---- --1-	Flip X ---- ---0	X Low Bit
     *
     * 00	Y
     *
     * 01	X (High 8 Bits)
     *
     **************************************************************************
     */
    public static void skyfox_draw_sprites(osd_bitmap bitmap) {
        int offs;

        int width = Machine.drv.screen_width;
        int height = Machine.drv.screen_height;

        /* The 32x32 tiles in the 80-ff range are bankswitched */
        int shift = (skyfox_bg_ctrl & 0x80) != 0 ? (4 - 1) : 4;

        for (offs = 0; offs < spriteram_size[0]; offs += 4) {
            int xstart, ystart, xend, yend;
            int xinc, yinc, dx, dy;
            int low_code, high_code, n;

            int y = spriteram.read(offs + 0);
            int x = spriteram.read(offs + 1);
            int code = spriteram.read(offs + 2) + spriteram.read(offs + 3) * 256;
            int flipx = code & 0x2;
            int flipy = code & 0x4;

            x = x * 2 + (code & 1);	// add the least significant bit

            high_code = ((code >> 4) & 0x7f0)
                    + ((code & 0x8000) >> shift);

            switch (code & 0x88) {
                case 0x88:
                    n = 4;
                    low_code = 0;
                    break;
                case 0x08:
                    n = 2;
                    low_code = ((code & 0x20) != 0 ? 8 : 0) + ((code & 0x10) != 0 ? 2 : 0);
                    break;
                default:
                    n = 1;
                    low_code = (code >> 4) & 0xf;
            }

            if ((skyfox_bg_ctrl & 1) != 0) // flipscreen
            {
                x = width - x - (n - 1) * 8;
                y = height - y - (n - 1) * 8;
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            if (flipx != 0) {
                xstart = n - 1;
                xend = -1;
                xinc = -1;
            } else {
                xstart = 0;
                xend = n;
                xinc = +1;
            }

            if (flipy != 0) {
                ystart = n - 1;
                yend = -1;
                yinc = -1;
            } else {
                ystart = 0;
                yend = n;
                yinc = +1;
            }

            code = low_code + high_code;

            for (dy = ystart; dy != yend; dy += yinc) {
                for (dx = xstart; dx != xend; dx += xinc) {
                    drawgfx(bitmap, Machine.gfx[0],
                            code++,
                            0,
                            flipx, flipy,
                            x + (dx * 8), y + (dy * 8),
                            Machine.visible_area, TRANSPARENCY_PEN, 0xff);

                }

                if (n == 2) {
                    code += 2;
                }
            }
        }

    }

    /**
     * *************************************************************************
     *
     * Background Rendering
     *
     **************************************************************************
     */
    public static void skyfox_draw_background(osd_bitmap bitmap) {
        UBytePtr RAM = memory_region(REGION_GFX2);
        int x, y, i;

        /* The foreground stars (sprites) move at twice this speed when
		   the bg scroll rate [e.g. (skyfox_bg_reg >> 1) & 7] is 4 */
        int pos = (skyfox_bg_pos >> 4) & (512 * 2 - 1);

        for (i = 0; i < 0x1000; i++) {
            int pen, offs, j;

            offs = (i * 2 + ((skyfox_bg_ctrl >> 4) & 0x3) * 0x2000) % 0x8000;

            pen = RAM.read(offs);
            x = RAM.read(offs + 1) * 2 + (i & 1) + pos + ((i & 8) != 0 ? 512 : 0);
            y = ((i / 8) / 2) * 8 + (i % 8);

            if ((skyfox_bg_ctrl & 1) != 0) // flipscreen
            {
                x = 512 * 2 - (x % (512 * 2));
                y = 256 - (y % 256);
            }

            for (j = 0; j <= ((pen & 0x80) != 0 ? 0 : 3); j++) {
                plot_pixel.handler(bitmap,
                        ((j & 1) + x) % 512,
                        (((j / 2) & 1) + y) % 256,
                        Machine.pens[256 + (pen & 0x7f)]);
            }
        }
    }

    /**
     * *************************************************************************
     *
     *
     * Screen Drawing
     *
     *
     **************************************************************************
     */
    public static VhUpdateHandlerPtr skyfox_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int layers_ctrl = -1;

            osd_clearbitmap(bitmap);	// the bg is black
            if ((layers_ctrl & 1) != 0) {
                skyfox_draw_background(bitmap);
            }
            if ((layers_ctrl & 2) != 0) {
                skyfox_draw_sprites(bitmap);
            }
        }
    };
}
