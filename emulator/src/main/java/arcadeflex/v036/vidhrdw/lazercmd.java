/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//drivers import
import static arcadeflex.v036.drivers.lazercmd.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.lazercmdH.*;
//TODO
import static gr.codebb.arcadeflex.common.libc.cstring.memset;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.copybitmap;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.drawgfx;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.plot_pixel;
import gr.codebb.arcadeflex.v036.mame.driverH.ReadHandlerPtr;
import gr.codebb.arcadeflex.v036.mame.driverH.VhUpdatePtr;
import gr.codebb.arcadeflex.v036.mame.driverH.WriteHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.TRANSPARENCY_NONE;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.input_port_2_r;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.dirtybuffer;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.tmpbitmap;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.videoram;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.videoram_size;

public class lazercmd {

    static int overlay = 0;

    static int video_inverted = 0;

    public static ReadHandlerPtr vert_scale = new ReadHandlerPtr() {
        public int handler(int data) {
            return ((data & 0x07) << 1) + ((data & 0xf8) >> 3) * VERT_CHR;
        }
    };

    /* return the (overlay) color for coord x, y */
    static int x_y_color(int x, int y) {
        int color = 2;
        if (overlay != 0) {
            /* left mustard yellow, right jade green */
            color = (x < 16 * HORZ_CHR) ? 0 : 1;
            /* but swapped in first and last lines */
            if ((y < 1 * VERT_CHR) || (y > 22 * VERT_CHR - 1)) {
                color ^= 1;
            }
        }
        if (video_inverted != 0) {
            color += 3;
        }
        return color;
    }

    /* mark the character occupied by the marker dirty */
    public static void lazercmd_marker_dirty(int marker) {
        int x, y;
        {
            x = marker_x - 1;
            /* normal video lags marker by 1 pixel */
            y = vert_scale.handler(marker_y) - VERT_CHR;
            /* first line used as scratch pad */
        }
        if (x < 0 || x >= HORZ_RES * HORZ_CHR) {
            return;
        }
        if (y < 0 || y >= VERT_RES * VERT_CHR) {
            return;
        }
        /* mark all occupied character positions dirty */
        dirtybuffer[(y + 0) / VERT_CHR * HORZ_RES + (x + 0) / HORZ_CHR] = 1;
        dirtybuffer[(y + 3) / VERT_CHR * HORZ_RES + (x + 0) / HORZ_CHR] = 1;
        dirtybuffer[(y + 0) / VERT_CHR * HORZ_RES + (x + 3) / HORZ_CHR] = 1;
        dirtybuffer[(y + 3) / VERT_CHR * HORZ_RES + (x + 3) / HORZ_CHR] = 1;
    }

    /* plot a bitmap marker */
 /* hardware has 2 marker sizes 2x2 and 4x2 selected by jumper */
 /* meadows lanes normaly use 2x2 pixels and lazer command uses either */
    public static WriteHandlerPtr plot_pattern = new WriteHandlerPtr() {
        public void handler(int x, int y) {
            int xbit, ybit, size;
            size = 2;
            if ((input_port_2_r.handler(0) & 0x40) != 0) {
                size = 4;
            }
            for (ybit = 0; ybit < 2; ybit++) {
                if (y + ybit < 0 || y + ybit >= VERT_RES * VERT_CHR) {
                    return;
                }
                for (xbit = 0; xbit < size; xbit++) {
                    if (x + xbit < 0 || x + xbit >= HORZ_RES * HORZ_CHR) {
                        continue;
                    }
                    plot_pixel.handler(tmpbitmap, x + xbit, y + ybit, Machine.pens[x_y_color(x + xbit, y + ybit) + 6]);
                }
            }
        }
    };

    public static VhUpdatePtr lazercmd_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i;

            if (overlay != (input_port_2_r.handler(0) & 0x80)) {
                overlay = input_port_2_r.handler(0) & 0x80;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            if (video_inverted != (input_port_2_r.handler(0) & 0x20)) {
                video_inverted = input_port_2_r.handler(0) & 0x20;
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* The first row of characters are invisible */
            for (i = 0; i < (VERT_RES - 1) * HORZ_RES; i++) {
                if (dirtybuffer[i] != 0) {
                    int x, y;

                    dirtybuffer[i] = 0;

                    x = i % HORZ_RES;
                    y = i / HORZ_RES;

                    x *= HORZ_CHR;
                    y *= VERT_CHR;

                    drawgfx(tmpbitmap,
                            Machine.gfx[0],
                            videoram.read(i),
                            x_y_color(x, y),
                            0, 0, x, y,
                            Machine.drv.visible_area,
                            TRANSPARENCY_NONE, 0);
                }
            }
            {
                int x, y;
                x = marker_x - 1;
                /* normal video lags marker by 1 pixel */
                y = vert_scale.handler(marker_y) - VERT_CHR;
                /* first line used as scratch pad */
                plot_pattern.handler(x, y);
            }

            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
        }
    };

}
