/**
 * *************************************************************************
 *
 * Prehistoric Isle video routines
 *
 * Emulation by Bryan McPhail, mish@tendril.co.uk
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class prehisle {

    static osd_bitmap pf1_bitmap, pf2_bitmap;
    public static UBytePtr prehisle_video = new UBytePtr();
    static int[] vid_control = new int[32];
    //static UBytePtr vid_control=new UBytePtr(32);
    static int dirty_back, dirty_front;

    /**
     * ***************************************************************************
     */
    static int old_base = 0xfffff, old_front = 0xfffff;
    public static VhUpdatePtr prehisle_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, mx, my, color, tile, i;
            int[] colmask = new int[0x80];
            int code, pal_base, tile_base;
            int scrollx, scrolly;
            UBytePtr tilemap = memory_region(REGION_GFX5);

            /* Build the dynamic palette */
            palette_init_used_colors();

            /* Text layer */
            pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;
            for (color = 0; color < 16; color++) {
                colmask[color] = 0;
            }
            for (offs = 0; offs < 0x800; offs += 2) {
                code = videoram.READ_WORD(offs);
                color = code >> 12;
                if (code == 0xff20) {
                    continue;
                }
                if (Machine.gfx[0].pen_usage.length >= (code & 0xFFF) - 1)//custom fix (shadow)
                {
                    colmask[color] |= Machine.gfx[0].pen_usage[code & 0xfff];
                }
            }
            for (color = 0; color < 16; color++) {
                for (i = 0; i < 15; i++) {
                    if ((colmask[color] & (1 << i)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                    }
                }
            }

            /* Tiles - bottom layer */
            pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
            for (offs = 0; offs < 256; offs++) {
                palette_used_colors.write(pal_base + offs, PALETTE_COLOR_USED);
            }

            /* Tiles - top layer */
            pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
            for (color = 0; color < 16; color++) {
                colmask[color] = 0;
            }
            for (offs = 0x0000; offs < 0x4000; offs += 2) {
                code = prehisle_video.READ_WORD(offs);
                color = code >> 12;
                colmask[color] |= Machine.gfx[2].pen_usage[code & 0x7ff];
            }
            for (color = 0; color < 16; color++) {
                for (i = 0; i < 15; i++) {
                    if ((colmask[color] & (1 << i)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                    }
                }

                ///* kludge */
                palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_TRANSPARENT);
                palette_change_color(pal_base + 16 * color + 15, 0, 0, 0);

            }

            /* Sprites */
            pal_base = Machine.drv.gfxdecodeinfo[3].color_codes_start;
            for (color = 0; color < 16; color++) {
                colmask[color] = 0;
            }
            for (offs = 0; offs < 0x400; offs += 8) {
                code = spriteram.READ_WORD(offs + 4) & 0x1fff;
                color = spriteram.READ_WORD(offs + 6) >> 12;
                if (code > 0x13ff) {
                    code = 0x13ff;
                }
                colmask[color] |= Machine.gfx[3].pen_usage[code];
            }
            for (color = 0; color < 16; color++) {
                for (i = 0; i < 15; i++) {
                    if ((colmask[color] & (1 << i)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                    }
                }
            }

            if (palette_recalc() != null) {
                dirty_back = 1;
                dirty_front = 1;
            }

            /* Calculate tilebase for background, 64 bytes per column */
            tile_base = (((vid_control[7] << 8 | vid_control[6]) & 0x3ff0) >> 4) * 64;
            if (old_base != tile_base) {
                dirty_back = 1; /* Redraw */
            }
            old_base = tile_base;

            /* Back layer, taken from tilemap rom */
            if (dirty_back != 0) {
                tile_base &= 0xfffe; /* Safety */

                dirty_back = 0;

                for (mx = 0; mx < 17; mx++) {
                    for (my = 0; my < 32; my++) {
                        tile = tilemap.read(1 + tile_base) + (tilemap.read(tile_base) << 8);
                        color = tile >> 12;
                        drawgfx(pf1_bitmap, Machine.gfx[1],
                                (tile & 0x7ff) | 0x800,
                                color,
                                tile & 0x800, 0,
                                16 * mx, 16 * my,
                                null, TRANSPARENCY_NONE, 0);
                        tile_base += 2;
                        if (tile_base == 0x10000) {
                            tile_base = 0; /* Wraparound */
                        }
                    }
                }
            }

            scrollx = -((vid_control[7] << 8 | vid_control[6]) & 0xf);
            scrolly = -(vid_control[5] << 8 | vid_control[4]);
            copyscrollbitmap(bitmap, pf1_bitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Calculate tilebase for background, 64 bytes per column */
            tile_base = (((vid_control[3] << 8 | vid_control[2]) & 0xff0) >> 4) * 64;
            if (old_front != tile_base) {
                dirty_front = 1; /* Redraw */
            }
            old_front = tile_base;

            /* Back layer, taken from tilemap rom */
            if (dirty_front != 0) {
                tile_base &= 0x3ffe; /* Safety */

                dirty_front = 0;

                for (mx = 0; mx < 17; mx++) {
                    for (my = 0; my < 32; my++) {
                        tile = prehisle_video.READ_WORD(tile_base);
                        color = tile >> 12;
                        drawgfx(pf2_bitmap, Machine.gfx[2],
                                tile & 0x7ff,
                                color,
                                0, tile & 0x800,
                                16 * mx, 16 * my,
                                null, TRANSPARENCY_NONE, 0);
                        tile_base += 2;
                        if (tile_base == 0x4000) {
                            tile_base = 0; /* Wraparound */
                        }
                    }
                }
            }

            scrollx = -((vid_control[3] << 8 | vid_control[2]) & 0xf);
            scrolly = -(vid_control[1] << 8 | vid_control[0]);
            copyscrollbitmap(bitmap, pf2_bitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            /* Sprites */
            for (offs = 0; offs < 0x800; offs += 8) {
                int x, y, sprite, colour, fx, fy;

                y = spriteram.READ_WORD(offs + 0);
                if (y > 254) {
                    continue; /* Speedup */
                }
                x = spriteram.READ_WORD(offs + 2);
                if ((x & 0x200) != 0) {
                    x = -(0xff - (x & 0xff));
                }
                if (x > 256) {
                    continue; /* Speedup */
                }

                sprite = spriteram.READ_WORD(offs + 4);
                colour = spriteram.READ_WORD(offs + 6) >> 12;

                fy = sprite & 0x8000;
                fx = sprite & 0x4000;

                sprite = sprite & 0x1fff;

                if (sprite > 0x13ff) {
                    sprite = 0x13ff;
                }

                drawgfx(bitmap, Machine.gfx[3],
                        sprite,
                        colour, fx, fy, x, y,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
            }

            /* Text layer */
            mx = -1;
            my = 0;
            for (offs = 0x000; offs < 0x800; offs += 2) {
                mx++;
                if (mx == 32) {
                    mx = 0;
                    my++;
                }
                tile = videoram.READ_WORD(offs);
                color = tile >> 12;
                if ((tile & 0xff) != 0x20) {
                    drawgfx(bitmap, Machine.gfx[0],
                            tile & 0xfff,
                            color,
                            0, 0,
                            8 * mx, 8 * my,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }
        }
    };

    /**
     * ***************************************************************************
     */
    public static VhStartPtr prehisle_vh_start = new VhStartPtr() {
        public int handler() {
            pf1_bitmap = osd_create_bitmap(256 + 16, 512);
            pf2_bitmap = osd_create_bitmap(256 + 16, 512);
            return 0;
        }
    };

    public static VhStopPtr prehisle_vh_stop = new VhStopPtr() {
        public void handler() {
            pf1_bitmap = null;
            pf2_bitmap = null;
        }
    };

    public static WriteHandlerPtr prehisle_video_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = prehisle_video.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                prehisle_video.WRITE_WORD(offset, newword);
                dirty_front = 1; /* Redraw */

            }
        }
    };

    public static ReadHandlerPtr prehisle_video_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return prehisle_video.READ_WORD(offset);
        }
    };

    static int controls_invert;

    public static ReadHandlerPtr prehisle_control_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0x10: /* Player 2 */

                    return readinputport(1);

                case 0x20: /* Coins, tilt, service */

                    return readinputport(2);

                case 0x40: /* Player 1 */

                    return readinputport(0) ^ controls_invert;

                case 0x42: /* Dips */

                    return readinputport(3);

                case 0x44: /* Dips + VBL */

                    return readinputport(4);

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "%06x: read unknown control %02x\n", cpu_get_pc(), offset);
                    }
                    return 0;
            }
        }
    };

    public static WriteHandlerPtr prehisle_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    vid_control[0] = (data & 0xFFFF);
                    vid_control[1] = (data >> 16);
                    break;
                case 0x10:
                    vid_control[2] = (data & 0xFFFF);
                    vid_control[3] = (data >> 16);
                    break;

                case 0x20:
                    vid_control[4] = (data & 0xFFFF);
                    vid_control[5] = (data >> 16);
                    break;

                case 0x30:
                    vid_control[6] = (data & 0xFFFF);
                    vid_control[7] = (data >> 16);
                    break;

                case 0x46:
                    controls_invert = data != 0 ? 0xff : 0x00;
                    break;

                case 0x50:
                    vid_control[8] = (data & 0xFFFF);
                    vid_control[9] = (data >> 16);
                    break;

                case 0x52:
                    vid_control[10] = (data & 0xFFFF);
                    vid_control[11] = (data >> 16);
                    break;
                case 0x60:
                    vid_control[12] = (data & 0xFFFF);
                    vid_control[13] = (data >> 16);
                    break;

                default:
                    if (errorlog != null) {
                        fprintf(errorlog, "%06x: write unknown control %02x\n", cpu_get_pc(), offset);
                    }
                    break;
            }
        }
    };
}
