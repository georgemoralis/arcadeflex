/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.vidhrdw.generic.*;

public class exprraid {

    public static UBytePtr exprraid_bgcontrol = new UBytePtr();

    public static VhConvertColorPromPtr exprraid_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }
        }
    };

    static void drawbg(osd_bitmap bitmap, int priority) {
        UBytePtr map1 = new UBytePtr(memory_region(REGION_GFX4), 0x0000);
        UBytePtr map2 = new UBytePtr(memory_region(REGION_GFX4), 0x4000);
        int offs, scrolly, scrollx1, scrollx2;

        scrolly = exprraid_bgcontrol.read(4);
        /* TODO: bgcontrol[7] seems related to the y scroll as well, but I'm not sure how */
        scrollx1 = exprraid_bgcontrol.read(5);
        scrollx2 = exprraid_bgcontrol.read(6);

        for (offs = 0x100 - 1; offs >= 0; offs--) {
            int sx, sy, quadrant, base, bank;

            sx = 16 * (offs % 16);
            sy = 16 * (offs / 16) - scrolly;

            quadrant = 0;
            if (sy <= -8) {
                quadrant += 2;
                sy += 256;
                sx -= scrollx2;
            } else {
                sx -= scrollx1;
            }

            if (sx <= -8) {
                quadrant++;
                sx += 256;
            }

            base = (exprraid_bgcontrol.read(quadrant) & 0x3f) * 0x100;

            if (priority == 0 || (map2.read(offs + base) & 0x80) != 0) {
                bank = 2 * (map2.read(offs + base) & 0x03) + ((map1.read(offs + base) & 0x80) >> 7);

                drawgfx(bitmap, Machine.gfx[2 + bank],
                        map1.read(offs + base) & 0x7f,
                        (map2.read(offs + base) & 0x18) >> 3,
                        (map2.read(offs + base) & 0x04), 0,
                        sx, sy,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            }
        }
    }

    public static VhUpdatePtr exprraid_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* draw the background */
            drawbg(bitmap, 0);

            /* draw the sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                int sx, sy, code, color, flipx;

                code = spriteram.read(offs + 3) + ((spriteram.read(offs + 1) & 0xe0) << 3);

                sx = ((248 - spriteram.read(offs + 2)) & 0xff) - 8;
                sy = spriteram.read(offs);
                color = (spriteram.read(offs + 1) & 0x03) + ((spriteram.read(offs + 1) & 0x08) >> 1);
                flipx = (spriteram.read(offs + 1) & 0x04);

                drawgfx(bitmap, Machine.gfx[1],
                        code,
                        color,
                        flipx, 0,
                        sx, sy,
                        null, TRANSPARENCY_PEN, 0);

                if ((spriteram.read(offs + 1) & 0x10) != 0) {
                    /* double height */
                    drawgfx(bitmap, Machine.gfx[1],
                            code + 1,
                            color,
                            flipx, 0,
                            sx, sy + 16,
                            null, TRANSPARENCY_PEN, 0);
                }
            }

            /* redraw the tiles which have priority over the sprites */
            drawbg(bitmap, 1);

            /* draw the foreground */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = offs % 32;
                sy = offs / 32;

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + ((colorram.read(offs) & 7) << 8),
                        (colorram.read(offs) & 0x10) >> 4,
                        0, 0,
                        8 * sx, 8 * sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
