/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 */
package arcadeflex.v036.vidhrdw;

//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class blktiger {

    public static UBytePtr blktiger_backgroundram = new UBytePtr();
    static char blktiger_video_control;
    public static UBytePtr blktiger_screen_layout = new UBytePtr();
    public static int[] blktiger_backgroundram_size = new int[1];
    static int blktiger_scroll_bank;
    static int scroll_page_count = 4;
    static char[] blktiger_scrolly = new char[2];
    static char[] blktiger_scrollx = new char[2];
    public static char[] scroll_ram;
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;
    static osd_bitmap tmpbitmap3;
    static int screen_layout;
    static int chon, objon, bgon;

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr blktiger_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[blktiger_backgroundram_size[0] * scroll_page_count]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, blktiger_backgroundram_size[0] * scroll_page_count);

            if ((scroll_ram = new char[blktiger_backgroundram_size[0] * scroll_page_count]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(scroll_ram, 0, blktiger_backgroundram_size[0] * scroll_page_count);

            /* the background area is 8 x 4 */
            if ((tmpbitmap2 = osd_create_bitmap(8 * Machine.drv.screen_width,
                    scroll_page_count * Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            /* the alternative background area is 4 x 8 */
            if ((tmpbitmap3 = osd_create_bitmap(4 * Machine.drv.screen_width,
                    2 * scroll_page_count * Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                osd_free_bitmap(tmpbitmap2);
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr blktiger_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            scroll_ram = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr blktiger_background_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            offset += blktiger_scroll_bank;

            /* TODO: there's a bug lurking around. If I uncomment the following line, */
 /* the intro screen doesn't work anymore (complete black instead of city landscape) */
            //	if (scroll_ram[offset] != data)
            {
                dirtybuffer2[offset] = 1;
                scroll_ram[offset] = (char) data;
            }
        }
    };

    public static WriteHandlerPtr blktiger_scrollbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            blktiger_scroll_bank = (data & 0x03) * blktiger_backgroundram_size[0];
        }
    };

    public static ReadHandlerPtr blktiger_background_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            offset += blktiger_scroll_bank;
            return scroll_ram[offset];
        }
    };

    public static WriteHandlerPtr blktiger_scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            blktiger_scrolly[offset] = (char) data;
        }
    };

    public static WriteHandlerPtr blktiger_scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            blktiger_scrollx[offset] = (char) data;
        }
    };

    public static WriteHandlerPtr blktiger_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bit 5 resets the sound CPU - we ignore it */
 /* bit 6 flips screen */
            blktiger_video_control = (char) data;

            /* bit 7 enables characters? Just a guess */
            chon = ~data & 0x80;
        }
    };

    public static WriteHandlerPtr blktiger_video_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* not sure which is which, but I think that bit 1 and 2 enable background and sprites */
 /* bit 1 enables bg ? */
            bgon = ~data & 0x02;

            /* bit 2 enables sprites ? */
            objon = ~data & 0x04;
        }
    };

    public static WriteHandlerPtr blktiger_screen_layout_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            screen_layout = data;
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
    public static VhUpdatePtr blktiger_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;

            palette_init_used_colors();

            {
                int color, code, i;
                int[] colmask = new int[16];
                int pal_base;

                pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = blktiger_backgroundram_size[0] * scroll_page_count - 2; offs >= 0; offs -= 2) {
                    int attr;

                    attr = scroll_ram[offs + 1];
                    color = (attr & 0x78) >> 3;
                    code = scroll_ram[offs] + ((attr & 0x07) << 8);
                    colmask[color] |= Machine.gfx[1].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    for (i = 0; i < 15; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                    if ((colmask[color] & (1 << 15)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_TRANSPARENT);
                    }
                }
            }
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int colour, code, x;

                sy = spriteram.read(offs + 2);
                sx = spriteram.read(offs + 3) - ((spriteram.read(offs + 1) & 0x10) << 4);

                /* only count visible sprites */
                if (sx + 15 >= Machine.drv.visible_area.min_x
                        && sx <= Machine.drv.visible_area.max_x
                        && sy + 15 >= Machine.drv.visible_area.min_y
                        && sy <= Machine.drv.visible_area.max_y) {
                    colour = spriteram.read(offs + 1) & 0x07;
                    code = scroll_ram[offs];
                    code = spriteram.read(offs);
                    code += (((int) (spriteram.read(offs + 1) & 0xe0)) << 3);
                    for (x = 0; x < 15; x++) {
                        if ((Machine.gfx[2].pen_usage[code] & (1 << x)) != 0) {
                            palette_used_colors.write(512 + 16 * colour + x, PALETTE_COLOR_USED);
                        }
                    }
                }
            }
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int colour, code, x;

                colour = colorram.read(offs) & 0x1f;
                code = videoram.read(offs) + ((colorram.read(offs) & 0xe0) << 3);
                for (x = 0; x < 3; x++) {
                    if ((Machine.gfx[0].pen_usage[code] & (1 << x)) != 0) {
                        palette_used_colors.write(768 + 4 * colour + x, PALETTE_COLOR_USED);
                    }
                }
            }

            if (palette_recalc() != null) {
                memset(dirtybuffer2, 1, blktiger_backgroundram_size[0] * scroll_page_count);
            }

            if (bgon != 0) {
                /*
                 Draw the tiles.
	
                 This method may look unnecessarily complex. Only tiles that are
                 likely to be visible are drawn. The rest are kept dirty until they
                 become visible.
	
                 The reason for this is that on level 3, the palette changes a lot
                 if the whole virtual screen is checked and redrawn then the
                 game will slow down to a crawl.
                 */

                if (screen_layout != 0) {
                    /* 8x4 screen */
                    int offsetbase;
                    int scrollx, scrolly, y;
                    scrollx = ((blktiger_scrollx[0] >> 4) + 16 * blktiger_scrollx[1]);
                    scrolly = ((blktiger_scrolly[0] >> 4) + 16 * blktiger_scrolly[1]);

                    for (sy = 0; sy < 18; sy++) {
                        y = (scrolly + sy) & (16 * 4 - 1);
                        offsetbase = ((y & 0xf0) << 8) + 32 * (y & 0x0f);
                        for (sx = 0; sx < 18; sx++) {
                            int colour, attr, code, x;
                            x = (scrollx + sx) & (16 * 8 - 1);
                            offs = offsetbase + ((x & 0xf0) << 5) + 2 * (x & 0x0f);

                            if (dirtybuffer2[offs] != 0 || dirtybuffer2[offs + 1] != 0) {
                                attr = scroll_ram[offs + 1];
                                colour = (attr & 0x78) >> 3;
                                code = scroll_ram[offs];
                                code += 256 * (attr & 0x07);

                                dirtybuffer2[offs] = dirtybuffer2[offs + 1] = 0;

                                drawgfx(tmpbitmap2, Machine.gfx[1],
                                        code,
                                        colour,
                                        attr & 0x80, 0,
                                        x * 16, y * 16,
                                        null, TRANSPARENCY_NONE, 0);
                            }
                        }
                    }

                    /* copy the background graphics */
                    {
                        scrollx = -(blktiger_scrollx[0] + 256 * blktiger_scrollx[1]);
                        scrolly = -(blktiger_scrolly[0] + 256 * blktiger_scrolly[1]);
                        copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                    }
                } else {
                    /* 4x8 screen */
                    int offsetbase;
                    int scrollx, scrolly, y;
                    scrollx = ((blktiger_scrollx[0] >> 4) + 16 * blktiger_scrollx[1]);
                    scrolly = ((blktiger_scrolly[0] >> 4) + 16 * blktiger_scrolly[1]);

                    for (sy = 0; sy < 18; sy++) {
                        y = (scrolly + sy) & (16 * 8 - 1);
                        offsetbase = ((y & 0xf0) << 7) + 32 * (y & 0x0f);
                        for (sx = 0; sx < 18; sx++) {
                            int colour, attr, code, x;
                            x = (scrollx + sx) & (16 * 4 - 1);
                            offs = offsetbase + ((x & 0xf0) << 5) + 2 * (x & 0x0f);

                            if (dirtybuffer2[offs] != 0 || dirtybuffer2[offs + 1] != 0) {
                                attr = scroll_ram[offs + 1];
                                colour = (attr & 0x78) >> 3;

                                code = scroll_ram[offs];
                                code += 256 * (attr & 0x07);

                                dirtybuffer2[offs] = dirtybuffer2[offs + 1] = 0;

                                drawgfx(tmpbitmap3, Machine.gfx[1],
                                        code,
                                        colour,
                                        attr & 0x80, 0,
                                        x * 16, y * 16,
                                        null, TRANSPARENCY_NONE, 0);
                            }
                        }
                    }

                    /* copy the background graphics */
                    {
                        scrollx = -(blktiger_scrollx[0] + 256 * blktiger_scrollx[1]);
                        scrolly = -(blktiger_scrolly[0] + 256 * blktiger_scrolly[1]);
                        copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }
            } else {
                fillbitmap(bitmap, palette_transparent_pen, Machine.drv.visible_area);
            }

            if (objon != 0) {
                /* Draw the sprites. */
                for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                    /*	SPRITES
                     =====
                     Attribute
                     0x80 Code MSB
                     0x40 Code MSB
                     0x20 Code MSB
                     0x10 X MSB
                     0x08 X flip
                     0x04 Colour
                     0x02 Colour
                     0x01 Colour
                     */

                    int code, colour;

                    code = spriteram.read(offs);
                    code += (((int) (spriteram.read(offs + 1) & 0xe0)) << 3);
                    colour = spriteram.read(offs + 1) & 0x07;

                    sy = spriteram.read(offs + 2);
                    sx = spriteram.read(offs + 3) - ((spriteram.read(offs + 1) & 0x10) << 4);

                    drawgfx(bitmap, Machine.gfx[2],
                            code,
                            colour,
                            spriteram.read(offs + 1) & 0x08, 0,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }

            if (chon != 0) {
                /* draw the frontmost playfield. They are characters, but draw them as sprites */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    sx = offs % 32;
                    sy = offs / 32;

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0xe0) << 3),
                            colorram.read(offs) & 0x1f,
                            0, 0,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
                }
            }
        }
    };
}
