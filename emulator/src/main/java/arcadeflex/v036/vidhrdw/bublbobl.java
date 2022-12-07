/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 */
package arcadeflex.v036.vidhrdw;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class bublbobl {

    public static UBytePtr bublbobl_objectram = new UBytePtr();
    public static int[] bublbobl_objectram_size = new int[1];

    public static VhConvertColorPromPtr bublbobl_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            /* no color PROMs here, only RAM, but the gfx data is inverted so we */
 /* cannot use the default lookup table */
            for (i = 0; i < Machine.drv.color_table_len; i++) {
                colortable[i] = (char) (i ^ 0x0f);
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
    public static VhUpdatePtr bublbobl_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int sx, sy, xc, yc;
            int gfx_num, gfx_attr, gfx_offs;

            palette_recalc();
            /* no need to check the return code since we redraw everything each frame */

 /* Bubble Bobble doesn't have a real video RAM. All graphics (characters */
 /* and sprites) are stored in the same memory region, and information on */
 /* the background character columns is stored inthe area dd00-dd3f */
 /* This clears & redraws the entire screen each pass */
            fillbitmap(bitmap, Machine.gfx[0].colortable.read(0), Machine.drv.visible_area);

            sx = 0;
            for (offs = 0; offs < bublbobl_objectram_size[0]; offs += 4) {
                int height;

                /* skip empty sprites */
 /* this is dword aligned so the UINT32 * cast shouldn't give problems */
 /* on any architecture */
                if (bublbobl_objectram.READ_DWORD(offs) == 0) {
                    continue;//if (*(UINT32 *)(&bublbobl_objectram[offs]) == 0)
                }

                gfx_num = bublbobl_objectram.read(offs + 1);
                gfx_attr = bublbobl_objectram.read(offs + 3);

                if ((gfx_num & 0x80) == 0) /* 16x16 sprites */ {
                    gfx_offs = ((gfx_num & 0x1f) * 0x80) + ((gfx_num & 0x60) >> 1) + 12;
                    height = 2;
                } else /* tilemaps (each sprite is a 16x256 column) */ {
                    gfx_offs = ((gfx_num & 0x3f) * 0x80);
                    height = 32;
                }

                if ((gfx_num & 0xc0) == 0xc0) /* next column */ {
                    sx += 16;
                } else {
                    sx = bublbobl_objectram.read(offs + 2);
                    if ((gfx_attr & 0x40) != 0) {
                        sx -= 256;
                    }
                }
                sy = 256 - height * 8 - (bublbobl_objectram.read(offs + 0));

                for (xc = 0; xc < 2; xc++) {
                    for (yc = 0; yc < height; yc++) {
                        int goffs, code, color, flipx, flipy, x, y;

                        goffs = gfx_offs + xc * 0x40 + yc * 0x02;
                        code = videoram.read(goffs) + 256 * (videoram.read(goffs + 1) & 0x03) + 1024 * (gfx_attr & 0x0f);
                        color = (videoram.read(goffs + 1) & 0x3c) >> 2;
                        flipx = videoram.read(goffs + 1) & 0x40;
                        flipy = videoram.read(goffs + 1) & 0x80;
                        x = sx + xc * 8;
                        y = (sy + yc * 8) & 0xff;

                        drawgfx(bitmap, Machine.gfx[0],
                                code,
                                color,
                                flipx, flipy,
                                x, y,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }
        }
    };
}
