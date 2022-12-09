/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;

public class ssi {

    public static ReadHandlerPtr ssi_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return videoram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr ssi_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(videoram, offset, data);
        }
    };

    public static VhStartPtr ssi_vh_start = new VhStartPtr() {
        public int handler() {
            return 0;
        }
    };

    public static VhStopPtr ssi_vh_stop = new VhStopPtr() {
        public void handler() {
        }
    };

    public static VhUpdatePtr ssi_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i, x, y, offs, code, color, spritecont, flipx, flipy;
            int xcurrent, ycurrent;

            /* update the palette usage */
            {
                char[] palette_map = new char[256];

                memset(palette_map, 0, palette_map.length);

                color = 0;

                for (offs = 0; offs < 0x3400; offs += 16) {
                    spritecont = (videoram.READ_WORD(offs + 8) & 0xff00) >> 8;

                    if ((spritecont & 0x04) == 0) {
                        color = videoram.READ_WORD(offs + 8) & 0x00ff;
                    }

                    code = videoram.READ_WORD(offs) & 0x1fff;

                    palette_map[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (i = 0; i < 256; i++) {
                    int usage = palette_map[i];
                    int j;

                    if (usage != 0) {
                        palette_used_colors.write(i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
                        for (j = 1; j < 16; j++) {
                            if ((palette_map[i] & (1 << j)) != 0) {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_USED);
                            } else {
                                palette_used_colors.write(i * 16 + j, PALETTE_COLOR_UNUSED);
                            }
                        }
                    } else {
                        memset(palette_used_colors, i * 16, PALETTE_COLOR_UNUSED, 16);
                    }
                }

                palette_recalc();
            }

            osd_clearbitmap(bitmap);
            x = 0;
            y = 0;
            xcurrent = 0;
            ycurrent = 0;
            color = 0;

            for (offs = 0; offs < 0x3400; offs += 16) {
                spritecont = (videoram.READ_WORD(offs + 8) & 0xff00) >> 8;

                flipx = spritecont & 0x01;
                flipy = spritecont & 0x02;

                if ((spritecont & 0x04) == 0) {
                    x = videoram.READ_WORD(offs + 4) & 0x0fff;
                    if (x >= 0x800) {
                        x -= 0x1000;
                    }
                    xcurrent = x;

                    y = videoram.READ_WORD(offs + 6) & 0x0fff;
                    if (y >= 0x800) {
                        y -= 0x1000;
                    }
                    ycurrent = y;

                    color = videoram.READ_WORD(offs + 8) & 0x00ff;
                } else {
                    if ((spritecont & 0x10) == 0) {
                        y = ycurrent;
                    } else if ((spritecont & 0x20) != 0) {
                        y += 16;
                    }

                    if ((spritecont & 0x40) == 0) {
                        x = xcurrent;
                    } else if ((spritecont & 0x80) != 0) {
                        x += 16;
                    }
                }

                code = videoram.READ_WORD(offs) & 0x1fff;

                drawgfx(bitmap, Machine.gfx[0],
                        code,
                        color,
                        flipx, flipy,
                        x, y,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }
        }
    };
}
