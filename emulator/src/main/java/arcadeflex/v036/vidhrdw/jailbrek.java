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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_new_bitmap;

public class jailbrek {

    public static UBytePtr jailbrek_scroll_x = new UBytePtr();

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr jailbrek_vh_convert_color_prom = new VhConvertColorPromPtr() {
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

                bit0 = (color_prom.read(0) >> 4) & 0x01;
                bit1 = (color_prom.read(0) >> 5) & 0x01;
                bit2 = (color_prom.read(0) >> 6) & 0x01;
                bit3 = (color_prom.read(0) >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }

            color_prom.inc(Machine.drv.total_colors);

            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() + 0x10);
            }

            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (color_prom.readinc());
            }
        }
    };
    public static VhStartPtr jailbrek_vh_start = new VhStartPtr() {
        public int handler() {

            if ((dirtybuffer = new char[videoram_size[0]]) == null) {
                return 1;
            }
            memset(dirtybuffer, 1, videoram_size[0]);

            if ((tmpbitmap = osd_new_bitmap(Machine.drv.screen_width * 2, Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer = null;
                return 1;
            }

            return 0;
        }
    };
    public static VhStopPtr jailbrek_vh_stop = new VhStopPtr() {
        public void handler() {

            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
        }
    };

    static void drawsprites(osd_bitmap bitmap) {
        int i;

        for (i = 0; i < spriteram_size[0]; i += 4) {
            int tile, color, sx, sy, flipx, flipy;

            /* attributes = ?tyxcccc */
            sx = spriteram.read(i + 2) - ((spriteram.read(i + 1) & 0x80) << 1);
            sy = spriteram.read(i + 3);
            tile = spriteram.read(i) + ((spriteram.read(i + 1) & 0x40) << 2);
            flipx = spriteram.read(i + 1) & 0x10;
            flipy = spriteram.read(i + 1) & 0x20;
            color = spriteram.read(i + 1) & 0x0f;

            drawgfx(bitmap, Machine.gfx[1],
                    tile, color,
                    flipx, flipy,
                    sx, sy,
                    Machine.drv.visible_area, TRANSPARENCY_COLOR, 0);
        }
    }

    public static VhUpdatePtr jailbrek_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i;

            if (full_refresh != 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            for (i = 0; i < videoram_size[0]; i++) {
                if (dirtybuffer[i] != 0) {
                    int sx, sy, code;

                    dirtybuffer[i] = 0;

                    sx = (i % 64);
                    sy = (i / 64);

                    code = videoram.read(i) + ((colorram.read(i) & 0xc0) << 2);

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            code,
                            colorram.read(i) & 0x0f,
                            0, 0,
                            sx * 8, sy * 8,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            {
                int[] scrollx = new int[32];

                for (i = 0; i < 32; i++) {
                    scrollx[i] = -((jailbrek_scroll_x.read(i + 32) << 8) + jailbrek_scroll_x.read(i));
                }

                copyscrollbitmap(bitmap, tmpbitmap, 32, scrollx, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            drawsprites(bitmap);
        }
    };
}
