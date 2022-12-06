/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_init_used_colors;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_used_colors;
import static arcadeflex.v036.mame.paletteH.PALETTE_COLOR_USED;

public class macross {

    public static UBytePtr macross_workram = new UBytePtr();
    public static UBytePtr macross_spriteram;
    public static UBytePtr macross_txvideoram = new UBytePtr();
    public static UBytePtr macross_videocontrol = new UBytePtr();
    public static int[] macross_txvideoram_size = new int[1];

    static char[] dirtybuffer;
    static osd_bitmap tmpbitmap;
    static int flipscreen = 0;

    public static VhStartPtr macross_vh_start = new VhStartPtr() {
        public int handler() {
            dirtybuffer = new char[macross_txvideoram_size[0] / 2];
            tmpbitmap = bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);

            if (dirtybuffer == null || tmpbitmap == null) {
                if (tmpbitmap != null) {
                    bitmap_free(tmpbitmap);
                }
                if (dirtybuffer != null) {
                    dirtybuffer = null;
                }
                return 1;
            }

            macross_spriteram = new UBytePtr(macross_workram, 0x8000);

            return 0;
        }
    };

    public static VhStopPtr macross_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(tmpbitmap);
            dirtybuffer = null;
            tmpbitmap = null;
        }
    };

    public static ReadHandlerPtr macross_txvideoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return macross_txvideoram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr macross_txvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = macross_txvideoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                macross_txvideoram.WRITE_WORD(offset, newword);
                dirtybuffer[offset / 2] = 1;
            }
        }
    };

    public static VhUpdatePtr macross_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            palette_init_used_colors();

            for (offs = (macross_txvideoram_size[0] / 2) - 1; offs >= 0; offs--) {
                int color = (macross_txvideoram.READ_WORD(offs * 2) >> 12);
                memset(palette_used_colors, 512 + 16 * color, PALETTE_COLOR_USED, 16);
            }

            for (offs = 0; offs < 256 * 16; offs += 16) {
                if (macross_spriteram.READ_WORD(offs) != 0) {
                    memset(palette_used_colors, 256 + 16 * macross_spriteram.READ_WORD(offs + 14), PALETTE_COLOR_USED, 16);
                }
            }

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, macross_txvideoram_size[0] / 2);
            }

            for (offs = (macross_txvideoram_size[0] / 2) - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx = offs / 32;
                    int sy = offs % 32;

                    int tilecode = macross_txvideoram.READ_WORD(offs * 2);

                    if (flipscreen != 0) {
                        sx = 47 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            tilecode & 0xfff,
                            tilecode >> 12,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);

                    dirtybuffer[offs] = 0;
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            for (offs = 0; offs < 256 * 16; offs += 16) {
                if (macross_spriteram.READ_WORD(offs) != 0) {
                    int sx = (macross_spriteram.READ_WORD(offs + 8) & 0x1ff);
                    int sy = (macross_spriteram.READ_WORD(offs + 12) & 0x1ff);
                    int tilecode = macross_spriteram.READ_WORD(offs + 6);
                    int xx = (macross_spriteram.READ_WORD(offs + 2) & 0x0f) + 1;
                    int yy = (macross_spriteram.READ_WORD(offs + 2) >> 4) + 1;
                    int width = xx;
                    int delta = 16;
                    int startx = sx;

                    if (flipscreen != 0) {
                        sx = 367 - sx;
                        sy = 239 - sy;
                        delta = -16;
                        startx = sx;
                    }

                    do {
                        do {
                            drawgfx(bitmap, Machine.gfx[2],
                                    tilecode & 0x3fff,
                                    macross_spriteram.READ_WORD(offs + 14),
                                    flipscreen, flipscreen,
                                    sx & 0x1ff, sy & 0x1ff,
                                    Machine.visible_area, TRANSPARENCY_PEN, 15);

                            tilecode++;
                            sx += delta;
                        } while (--xx != 0);

                        sy += delta;
                        sx = startx;
                        xx = width;
                    } while (--yy != 0);
                }
            }
        }
    };
}
