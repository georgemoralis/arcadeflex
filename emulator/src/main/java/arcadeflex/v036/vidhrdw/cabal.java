/*
 * ported to v0.36
 */
package arcadeflex.v036.vidhrdw;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;

public class cabal {

    static void draw_background(osd_bitmap bitmap) {
        int offs;
        GfxElement gfx = Machine.gfx[1];

        for (offs = 0; offs < videoram_size[0]; offs += 2) {
            int offs2 = offs / 2;
            if (dirtybuffer[offs2] != 0) {
                int data = videoram.READ_WORD(offs);
                int numtile = (data & 0xfff);
                int color = (data & 0xf000) >> 12;
                int sx = (offs2 % 16) * 16;
                int sy = (offs2 / 16) * 16;

                dirtybuffer[offs2] = 0;

                drawgfx(tmpbitmap, gfx,
                        numtile,
                        color,
                        0, 0,
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }

        copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
    }

    static void draw_text(osd_bitmap bitmap) {
        int offs;
        rectangle clip = Machine.drv.visible_area;

        for (offs = 0; offs < 0x800; offs += 2) {
            /*unsigned short*/
            char data = (char) colorram.READ_WORD(offs);
            int tile_number = data & 0x3ff;

            if (tile_number != 0xd) {
                int color = data >> 10;
                int sx = 8 * ((offs >> 1) % 32);
                int sy = 8 * ((offs >> 1) / 32);

                drawgfx(bitmap, Machine.gfx[0],
                        tile_number,
                        color,
                        0, 0, /* no flip */
                        sx, sy,
                        clip, TRANSPARENCY_PEN, 0x3);
            }
        }
    }

    static void draw_sprites(osd_bitmap bitmap) {
        rectangle clip = Machine.drv.visible_area;
        GfxElement gfx = Machine.gfx[2];
        int offs;

        for (offs = spriteram_size[0] - 8; offs >= 0; offs -= 8) {
            int data0 = spriteram.READ_WORD(offs);
            int data1 = spriteram.READ_WORD(offs + 2);
            int data2 = spriteram.READ_WORD(offs + 4);
            //      int data3 = READ_WORD( &spriteram[offs+6] );

            /*
	            -------E YYYYYYYY
	            ----BBTT TTTTTTTT
	            -CCCCF-X XXXXXXXX
	            -------- --------
             */
            if ((data0 & 0x100) != 0) {
                int tile_number = data1 & 0xfff;
                int color = (data2 & 0x7800) >> 11;
                int sy = (data0 & 0xff);
                int sx = (data2 & 0x1ff);
                int hflip = (data2 & 0x0400);

                if (sx > 256) {
                    sx -= 512;
                }

                drawgfx(bitmap, gfx,
                        tile_number,
                        color,
                        hflip, 0,
                        sx, sy,
                        clip, TRANSPARENCY_PEN, 0xf);
            }
        }
    }
    public static VhUpdatePtr cabal_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0] / 2);
            }

            draw_background(bitmap);
            draw_sprites(bitmap);
            draw_text(bitmap);
        }
    };
}
