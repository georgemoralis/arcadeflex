/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.common.libc.cstring.memset;
import static gr.codebb.arcadeflex.common.libc.expressions.*;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_alloc;
import static gr.codebb.arcadeflex.v036.mame.common.bitmap_free;
import static gr.codebb.arcadeflex.v036.mame.common.coin_counter_w;
import static gr.codebb.arcadeflex.v036.mame.common.coin_lockout_w;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_init_used_colors;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_recalc;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_transparent_pen;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_used_colors;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.PALETTE_COLOR_TRANSPARENT;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.PALETTE_COLOR_USED;

public class rastan {

    public static int[] rastan_videoram_size = new int[1];
    public static UBytePtr rastan_videoram1 = new UBytePtr();
    public static UBytePtr rastan_videoram3 = new UBytePtr();
    public static UBytePtr rastan_spriteram = new UBytePtr();
    public static UBytePtr rastan_scrollx = new UBytePtr();
    public static UBytePtr rastan_scrolly = new UBytePtr();

    static char[] rastan_dirty1;
    static char[] rastan_dirty3;

    static /*unsigned char*/ int spritepalettebank;
    static int flipscreen;

    static osd_bitmap tmpbitmap1;
    static osd_bitmap tmpbitmap3;

    public static VhStartPtr rastan_vh_start = new VhStartPtr() {
        public int handler() {
            /* Allocate a video RAM */
            rastan_dirty1 = new char[rastan_videoram_size[0] / 4];
            if (rastan_dirty1 == null) {
                rastan_vh_stop.handler();
                return 1;
            }
            memset(rastan_dirty1, 1, rastan_videoram_size[0] / 4);

            rastan_dirty3 = new char[rastan_videoram_size[0] / 4];
            if (rastan_dirty3 == null) {
                rastan_vh_stop.handler();
                return 1;
            }
            memset(rastan_dirty3, 1, rastan_videoram_size[0] / 4);

            /* Allocate temporary bitmaps */
            if ((tmpbitmap1 = bitmap_alloc(512, 512)) == null) {
                rastan_vh_stop.handler();
                return 1;
            }
            if ((tmpbitmap3 = bitmap_alloc(512, 512)) == null) {
                rastan_vh_stop.handler();
                return 1;
            }
            flipscreen = 0;
            /*maybe not needed*/
            return 0;
        }
    };

    public static VhStopPtr rastan_vh_stop = new VhStopPtr() {
        public void handler() {
            /* Free temporary bitmaps */
            if (tmpbitmap3 != null) {
                bitmap_free(tmpbitmap3);
            }
            tmpbitmap3 = null;
            if (tmpbitmap1 != null) {
                bitmap_free(tmpbitmap1);
            }
            tmpbitmap1 = null;

            /* Free video RAM */
            if (rastan_dirty1 != null) {
                rastan_dirty1 = null;
            }
            if (rastan_dirty3 != null) {
                rastan_dirty3 = null;
            }
        }
    };

    /*
     *   scroll write handlers
     */
    public static WriteHandlerPtr rastan_scrollY_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(rastan_scrolly, offset, data);
        }
    };

    public static WriteHandlerPtr rastan_scrollX_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(rastan_scrollx, offset, data);
        }
    };

    public static WriteHandlerPtr rastan_videoram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = rastan_videoram1.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                rastan_videoram1.WRITE_WORD(offset, newword);
                rastan_dirty1[offset / 4] = 1;
            }
        }
    };
    public static ReadHandlerPtr rastan_videoram1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return rastan_videoram1.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr rastan_videoram3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = rastan_videoram3.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                rastan_videoram3.WRITE_WORD(offset, newword);
                rastan_dirty3[offset / 4] = 1;
            }
        }
    };
    public static ReadHandlerPtr rastan_videoram3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return rastan_videoram3.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr rastan_videocontrol_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                /* bits 0 and 1 are coin lockout */
                coin_lockout_w.handler(1, ~data & 0x01);
                coin_lockout_w.handler(0, ~data & 0x02);

                /* bits 2 and 3 are the coin counters */
                coin_counter_w.handler(1, data & 0x04);
                coin_counter_w.handler(0, data & 0x08);

                /* bits 5-7 look like the sprite palette bank */
                spritepalettebank = (data & 0xe0) >> 5;

                /* other bits unknown */
            }
        }
    };

    public static WriteHandlerPtr rastan_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                /* flipscreen when bit 0 set */
                if (flipscreen != (data & 1)) {
                    flipscreen = data & 1;
                    memset(rastan_dirty1, 1, rastan_videoram_size[0] / 4);
                    memset(rastan_dirty3, 1, rastan_videoram_size[0] / 4);
                }
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
    public static VhUpdatePtr rastan_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int scrollx, scrolly;

            palette_init_used_colors();

            {
                int color, code, i;
                int[] colmask = new int[128];
                int pal_base;

                pal_base = 0;

                for (color = 0; color < 128; color++) {
                    colmask[color] = 0;
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram1.READ_WORD(offs + 2) & 0x3fff;
                    color = rastan_videoram1.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram3.READ_WORD(offs + 2) & 0x3fff;
                    color = rastan_videoram3.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (offs = 0x800 - 8; offs >= 0; offs -= 8) {
                    code = rastan_spriteram.READ_WORD(offs + 4) & 0x0fff;

                    if (code != 0) {
                        int data1;

                        data1 = rastan_spriteram.READ_WORD(offs);

                        color = (data1 & 0x0f) + 0x10 * spritepalettebank;
                        colmask[color] |= Machine.gfx[1].pen_usage[code];
                    }
                }

                for (color = 0; color < 128; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                if (palette_recalc() != null) {
                    memset(rastan_dirty1, 1, rastan_videoram_size[0] / 4);
                    memset(rastan_dirty3, 1, rastan_videoram_size[0] / 4);
                }
            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty1[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;
                    int flipx, flipy;

                    rastan_dirty1[offs / 4] = 0;

                    data1 = rastan_videoram1.READ_WORD(offs);
                    data2 = rastan_videoram1.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 63 - sx;
                        sy = 63 - sy;
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            data2 & 0x3fff,
                            data1 & 0x7f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty3[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;
                    int flipx, flipy;

                    rastan_dirty3[offs / 4] = 0;

                    data1 = rastan_videoram3.READ_WORD(offs);
                    data2 = rastan_videoram3.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 63 - sx;
                        sy = 63 - sy;
                    }

                    drawgfx(tmpbitmap3, Machine.gfx[0],
                            data2 & 0x3fff,
                            data1 & 0x7f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            scrollx = rastan_scrollx.READ_WORD(0) - 16;
            scrolly = rastan_scrolly.READ_WORD(0);
            if (flipscreen != 0) {
                scrollx = 320 - scrollx;
                scrolly = 240 - scrolly + 16;
            }
            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);

            scrollx = rastan_scrollx.READ_WORD(2) - 16;
            scrolly = rastan_scrolly.READ_WORD(2);
            if (flipscreen != 0) {
                scrollx = 320 - scrollx;
                scrolly = 240 - scrolly + 16;
            }
            copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

            /* Draw the sprites. 256 sprites in total */
            for (offs = 0x800 - 8; offs >= 0; offs -= 8) {
                int num = rastan_spriteram.READ_WORD(offs + 4);

                if (num != 0) {
                    int sx, sy, col, data1;
                    int flipx, flipy;

                    sx = rastan_spriteram.READ_WORD(offs + 6) & 0x1ff;
                    if (sx > 400) {
                        sx = sx - 512;
                    }
                    sy = rastan_spriteram.READ_WORD(offs + 2) & 0x1ff;
                    if (sy > 400) {
                        sy = sy - 512;
                    }

                    data1 = rastan_spriteram.READ_WORD(offs);

                    col = (data1 & 0x0f) + 0x10 * spritepalettebank;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 320 - sx - 16;
                        sy = 240 - sy;
                    }

                    drawgfx(bitmap, Machine.gfx[1],
                            num,
                            col,
                            flipx, flipy,
                            sx, sy,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }
        }
    };

    public static VhUpdatePtr rainbow_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int scrollx, scrolly;

            palette_init_used_colors();

            /* TODO: we are using the same table for background and foreground tiles, but this */
 /* causes the sky to be black instead of blue. */
            {
                int color, code, i;
                int[] colmask = new int[128];
                int pal_base;

                pal_base = 0;

                for (color = 0; color < 128; color++) {
                    colmask[color] = 0;
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram1.READ_WORD(offs + 2) & 0x3FFF;
                    color = rastan_videoram1.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram3.READ_WORD(offs + 2) & 0x3fff;
                    color = rastan_videoram3.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (offs = 0x800 - 8; offs >= 0; offs -= 8) {
                    code = rastan_spriteram.READ_WORD(offs + 4);

                    if (code != 0) {
                        int data1;

                        data1 = rastan_spriteram.READ_WORD(offs);

                        color = (data1 + 0x10) & 0x7f;

                        if (code < 4096) {
                            colmask[color] |= Machine.gfx[1].pen_usage[code];
                        } else {
                            colmask[color] |= Machine.gfx[2].pen_usage[code - 4096];
                        }
                    }
                }

                for (color = 0; color < 128; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_USED);
                    }

                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                /* Make one transparent colour */
                palette_used_colors.write(pal_base, PALETTE_COLOR_TRANSPARENT);

                if (palette_recalc() != null) {
                    memset(rastan_dirty1, 1, rastan_videoram_size[0] / 4);
                    memset(rastan_dirty3, 1, rastan_videoram_size[0] / 4);
                }
            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty1[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;
                    int flipx, flipy;

                    rastan_dirty1[offs / 4] = 0;

                    data1 = rastan_videoram1.READ_WORD(offs);
                    data2 = rastan_videoram1.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 63 - sx;
                        sy = 63 - sy;
                    }

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            data2 & 0x3fff,
                            data1 & 0x7f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty3[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;
                    int flipx, flipy;

                    rastan_dirty3[offs / 4] = 0;

                    data1 = rastan_videoram3.READ_WORD(offs);
                    data2 = rastan_videoram3.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 63 - sx;
                        sy = 63 - sy;
                    }
                    /* Colour as Transparent */

                    drawgfx(tmpbitmap3, Machine.gfx[0],
                            0,
                            0,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);

                    /* Draw over with correct Transparency */
                    drawgfx(tmpbitmap3, Machine.gfx[0],
                            data2 & 0x3fff,
                            data1 & 0x7f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_PEN, 0);
                }
            }

            scrollx = rastan_scrollx.READ_WORD(0) - 16;
            scrolly = rastan_scrolly.READ_WORD(0);
            if (flipscreen != 0) {
                scrollx = 320 - scrollx;
                scrolly = 240 - scrolly + 16;
            }
            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. 256 sprites in total */
            for (offs = 0x800 - 8; offs >= 0; offs -= 8) {
                int num = rastan_spriteram.READ_WORD(offs + 4);

                if (num != 0) {
                    int sx, sy, col, data1;
                    int flipx, flipy;

                    sx = rastan_spriteram.READ_WORD(offs + 6) & 0x1ff;
                    if (sx > 400) {
                        sx = sx - 512;
                    }
                    sy = rastan_spriteram.READ_WORD(offs + 2) & 0x1ff;
                    if (sy > 400) {
                        sy = sy - 512;
                    }

                    data1 = rastan_spriteram.READ_WORD(offs);

                    col = (data1 + 0x10) & 0x7f;

                    flipx = data1 & 0x4000;
                    flipy = data1 & 0x8000;

                    if (flipscreen != 0) {
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                        sx = 320 - sx - 16;
                        sy = 240 - sy;
                    }

                    if (num < 4096) {
                        drawgfx(bitmap, Machine.gfx[1],
                                num,
                                col,
                                flipx, flipy,
                                sx, sy,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    } else {
                        drawgfx(bitmap, Machine.gfx[2],
                                num - 4096,
                                col,
                                flipx, flipy,
                                sx, sy,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }

            scrollx = rastan_scrollx.READ_WORD(2) - 16;
            scrolly = rastan_scrolly.READ_WORD(2);
            if (flipscreen != 0) {
                scrollx = 320 - scrollx;
                scrolly = 240 - scrolly + 16;
            }
            copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);

        }
    };

    /* Jumping uses different sprite controller   */
 /* than rainbow island. - values are remapped */
 /* at address 0x2EA in the code. Apart from   */
 /* physical layout, the main change is that   */
 /* the Y settings are active low              */
    public static VhUpdatePtr jumping_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int scrollx, scrolly;

            palette_init_used_colors();

            /* TODO: we are using the same table for background and foreground tiles, but this */
 /* causes the sky to be black instead of blue. */
            {
                int color, code, i;
                int[] colmask = new int[128];
                int pal_base;

                pal_base = 0;

                for (color = 0; color < 128; color++) {
                    colmask[color] = 0;
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram1.READ_WORD(offs + 2) & 0x3FFF;
                    color = rastan_videoram1.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (offs = 0x800 - 8; offs >= 0; offs -= 8) {
                    code = rastan_spriteram.READ_WORD(offs);

                    if (code < Machine.gfx[1].total_elements) {
                        int data1;

                        data1 = rastan_spriteram.READ_WORD(offs + 8);

                        color = (data1 + 0x10) & 0x7f;
                        colmask[color] |= Machine.gfx[1].pen_usage[code];
                    }
                }

                for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                    code = rastan_videoram3.READ_WORD(offs + 2) & 0x3FFF;
                    color = rastan_videoram3.READ_WORD(offs) & 0x7f;

                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (color = 0; color < 128; color++) {
                    if ((colmask[color] & (1 << 15)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_USED);
                    }

                    for (i = 0; i < 15; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                /* Make one transparent colour */
                palette_used_colors.write(pal_base + 15, PALETTE_COLOR_TRANSPARENT);

                if (palette_recalc() != null) {
                    memset(rastan_dirty1, 1, rastan_videoram_size[0] / 4);
                    memset(rastan_dirty3, 1, rastan_videoram_size[0] / 4);
                }

            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty1[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;

                    rastan_dirty1[offs / 4] = 0;

                    data1 = rastan_videoram1.READ_WORD(offs);
                    data2 = rastan_videoram1.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    drawgfx(tmpbitmap1, Machine.gfx[0],
                            data2,
                            data1 & 0x7f,
                            data1 & 0x4000, data1 & 0x8000,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            for (offs = rastan_videoram_size[0] - 4; offs >= 0; offs -= 4) {
                if (rastan_dirty3[offs / 4] != 0) {
                    int sx, sy;
                    int data1, data2;

                    rastan_dirty3[offs / 4] = 0;

                    data1 = rastan_videoram3.READ_WORD(offs);
                    data2 = rastan_videoram3.READ_WORD(offs + 2);

                    sx = (offs / 4) % 64;
                    sy = (offs / 4) / 64;

                    /* Colour as Transparent */
                    drawgfx(tmpbitmap3, Machine.gfx[0],
                            0,
                            0,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);

                    /* Draw over with correct Transparency */
                    drawgfx(tmpbitmap3, Machine.gfx[0],
                            data2,
                            data1 & 0x7f,
                            data1 & 0x4000, data1 & 0x8000,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_PEN, 15);
                }
            }

            scrollx = rastan_scrollx.READ_WORD(0) - 16;
            scrolly = -rastan_scrolly.READ_WORD(0);
            copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /* Draw the sprites. 128 sprites in total */
            for (offs = 0x07F0; offs >= 0; offs -= 16) {
                int num = rastan_spriteram.READ_WORD(offs);

                if (num != 0) {
                    int sx, col, data1;
                    int sy;

                    sy = ((rastan_spriteram.READ_WORD(offs + 2) - 0xFFF1) ^ 0xFFFF) & 0x1FF;
                    if (sy > 400) {
                        sy = sy - 512;
                    }
                    sx = (rastan_spriteram.READ_WORD(offs + 4) - 0x38) & 0x1ff;
                    if (sx > 400) {
                        sx = sx - 512;
                    }

                    data1 = rastan_spriteram.READ_WORD(offs + 6);
                    col = (rastan_spriteram.READ_WORD(offs + 8) + 0x10) & 0x7F;

                    drawgfx(bitmap, Machine.gfx[1],
                            num,
                            col,
                            data1 & 0x40, data1 & 0x80,
                            sx, sy + 1,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }

            scrollx = - 16;
            scrolly = 0;
            copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
        }
    };
}
