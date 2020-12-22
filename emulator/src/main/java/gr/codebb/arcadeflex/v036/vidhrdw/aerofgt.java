/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;

public class aerofgt {

    public static UBytePtr aerofgt_rasterram = new UBytePtr();
    public static UBytePtr aerofgt_bg1videoram = new UBytePtr();
    public static UBytePtr aerofgt_bg2videoram = new UBytePtr();
    public static int[] aerofgt_bg1videoram_size = new int[1];
    public static int[] aerofgt_bg2videoram_size = new int[1];

    static UBytePtr gfxbank = new UBytePtr(8);
    static UBytePtr bg1scrolly = new UBytePtr(2);
    static UBytePtr bg2scrollx = new UBytePtr(2);
    static UBytePtr bg2scrolly = new UBytePtr(2);

    static osd_bitmap tmpbitmap2;
    static char[] dirtybuffer2;

    static int charpalettebank, spritepalettebank;
    static int bg2_chardisplacement;

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    static int common_vh_start(int width, int height) {
        if ((dirtybuffer = new char[aerofgt_bg1videoram_size[0] / 2]) == null) {
            return 1;
        }
        memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);

        if ((tmpbitmap = osd_new_bitmap(width, height, Machine.scrbitmap.depth)) == null) {
            dirtybuffer = null;
            return 1;
        }

        if ((dirtybuffer2 = new char[aerofgt_bg2videoram_size[0] / 2]) == null) {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            return 1;
        }
        memset(dirtybuffer2, 1, aerofgt_bg2videoram_size[0] / 2);

        if ((tmpbitmap2 = osd_new_bitmap(width, height, Machine.scrbitmap.depth)) == null) {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            dirtybuffer2 = null;
            return 1;
        }

        charpalettebank = 0;
        spritepalettebank = 0;

        return 0;
    }

    public static VhStartPtr pspikes_vh_start = new VhStartPtr() {
        public int handler() {
            bg2_chardisplacement = 0;
            aerofgt_bg2videoram_size[0] = 0;	/* no bg2 in this game */

            return common_vh_start(512, 256);
        }
    };

    public static VhStartPtr turbofrc_vh_start = new VhStartPtr() {
        public int handler() {
            bg2_chardisplacement = 0x9c;
            return common_vh_start(512, 512);
        }
    };

    public static VhStartPtr aerofgt_vh_start = new VhStartPtr() {
        public int handler() {
            bg2_chardisplacement = 0;
            return common_vh_start(512, 512);
        }
    };

    public static VhStartPtr aerofgtb_vh_start = new VhStartPtr() {
        public int handler() {
            bg2_chardisplacement = 0x4000;
            return common_vh_start(512, 512);
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr aerofgt_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(tmpbitmap);
            dirtybuffer2 = null;
            osd_free_bitmap(tmpbitmap2);
        }
    };

    public static ReadHandlerPtr aerofgt_rasterram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return aerofgt_rasterram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr aerofgt_rasterram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(aerofgt_rasterram, offset, data);
        }
    };

    public static ReadHandlerPtr aerofgt_spriteram_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram_2.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr aerofgt_spriteram_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(spriteram_2, offset, data);
        }
    };

    public static ReadHandlerPtr aerofgt_bg1videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return aerofgt_bg1videoram.READ_WORD(offset);
        }
    };

    public static ReadHandlerPtr aerofgt_bg2videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return aerofgt_bg2videoram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr aerofgt_bg1videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = aerofgt_bg1videoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                aerofgt_bg1videoram.WRITE_WORD(offset, newword);
                dirtybuffer[offset / 2] = 1;
            }
        }
    };

    public static WriteHandlerPtr aerofgt_bg2videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = aerofgt_bg2videoram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            if (oldword != newword) {
                aerofgt_bg2videoram.WRITE_WORD(offset, newword);
                dirtybuffer2[offset / 2] = 1;
            }
        }
    };

    public static WriteHandlerPtr pspikes_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* there are actually two banks in pspikes, instead of four like */
            /* in the other games. The character code is cccBnnnnnnnnnnnn instead */
            /* of cccBBnnnnnnnnnnn. Here I convert the data to four banks so I */
            /* can use the same common routines as the other games. */
            gfxbank.write(0, (char) ((2 * ((data & 0xf0) >> 4)) & 0xFF));	/* guess */

            gfxbank.write(1, (char) ((2 * ((data & 0xf0) >> 4) + 1) & 0xFF));	/* guess */

            gfxbank.write(2, (char) ((2 * (data & 0x0f)) & 0xFF));
            gfxbank.write(3, (char) ((2 * (data & 0x0f) + 1) & 0xFF));
        }
    };
    static UBytePtr oldw = new UBytePtr(4);
    public static WriteHandlerPtr turbofrc_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int oldword = oldw.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);
            if (oldword != newword) {
                oldw.WRITE_WORD(offset, newword);

                gfxbank.write(2 * offset + 0, (char) (((newword >> 0) & 0x0f) & 0xFF));
                gfxbank.write(2 * offset + 1, (char) (((newword >> 4) & 0x0f) & 0xFF));
                gfxbank.write(2 * offset + 2, (char) (((newword >> 8) & 0x0f) & 0xFF));
                gfxbank.write(2 * offset + 3, (char) (((newword >> 12) & 0x0f) & 0xFF));
                if (offset < 2) {
                    memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);
                } else {
                    memset(dirtybuffer2, 1, aerofgt_bg2videoram_size[0] / 2);
                }
            }
        }
    };

    public static WriteHandlerPtr aerofgt_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = gfxbank.READ_WORD(offset);
            int newword;

            /* straighten out the 16-bit word into bytes for conveniency */
            //#ifdef LSB_FIRST
            data = ((data & 0x00ff00ff) << 8) | ((data & 0xff00ff00) >> 8);
            //#endif

            newword = COMBINE_WORD(oldword, data);
            if (oldword != newword) {
                gfxbank.WRITE_WORD(offset, newword);
                if (offset < 4) {
                    memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);
                } else {
                    memset(dirtybuffer2, 1, aerofgt_bg2videoram_size[0] / 2);
                }
            }
        }
    };

    public static WriteHandlerPtr aerofgt_bg1scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(bg1scrolly, 0, data);
        }
    };

    public static WriteHandlerPtr turbofrc_bg2scrollx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(bg2scrollx, 0, data);
        }
    };

    public static WriteHandlerPtr aerofgt_bg2scrolly_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(bg2scrolly, 0, data);
        }
    };

    public static WriteHandlerPtr pspikes_palette_bank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spritepalettebank = data & 0x03;
            charpalettebank = (data & 0x1c) >> 2;
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
    static void bg_dopalette() {
        int offs;
        int color, code, i;
        int[] colmask = new int[32];
        int pal_base;

        pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;

        for (color = 0; color < 32; color++) {
            colmask[color] = 0;
        }

        for (offs = aerofgt_bg1videoram_size[0] - 2; offs >= 0; offs -= 2) {
            code = aerofgt_bg1videoram.READ_WORD(offs);
            color = ((code & 0xe000) >> 13) + 8 * charpalettebank;
            code = (code & 0x07ff) + (gfxbank.read(0 + ((code & 0x1800) >> 11)) << 11);
            colmask[color] |= Machine.gfx[0].pen_usage[code];
        }

        for (offs = aerofgt_bg2videoram_size[0] - 2; offs >= 0; offs -= 2) {
            code = aerofgt_bg2videoram.READ_WORD(offs);
            color = ((code & 0xe000) >> 13) + 16;
            code = bg2_chardisplacement + (code & 0x07ff) + (gfxbank.read(4 + ((code & 0x1800) >> 11)) << 11);
            colmask[color] |= Machine.gfx[0].pen_usage[code];
        }

        for (color = 0; color < 32; color++) {
            for (i = 0; i < 16; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
            /* bg1 uses colors 0-7 and is not transparent */
            /* bg2 uses colors 16-23 and is transparent */
            if (color >= 16 && (colmask[color] & (1 << 15)) != 0) {
                palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_TRANSPARENT);
            }
        }
    }

    static void aerofgt_spr_dopalette() {
        int offs;
        int color, i;
        int[] colmask = new int[32];
        int pal_base;

        for (color = 0; color < 32; color++) {
            colmask[color] = 0;
        }

        offs = 0;
        while (offs < 0x0800 && (spriteram_2.READ_WORD(offs) & 0x8000) == 0) {
            int attr_start, map_start;

            attr_start = 8 * (spriteram_2.READ_WORD(offs) & 0x03ff);

            color = (spriteram_2.READ_WORD(attr_start + 4) & 0x0f00) >> 8;
            map_start = 2 * (spriteram_2.READ_WORD(attr_start + 6) & 0x3fff);
            if (map_start >= 0x4000) {
                color += 16;
            }

            colmask[color] |= 0xffff;

            offs += 2;
        }

        pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;
        for (color = 0; color < 16; color++) {
            for (i = 0; i < 15; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
        }
        pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
        for (color = 0; color < 16; color++) {
            for (i = 0; i < 15; i++) {
                if ((colmask[color + 16] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
        }
    }

    static void turbofrc_spr_dopalette() {
        int color, i;
        int[] colmask = new int[16];
        int pal_base;
        int attr_start, base, first;

        for (color = 0; color < 16; color++) {
            colmask[color] = 0;
        }

        pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;

        base = 0;
        first = 8 * spriteram_2.READ_WORD(0x3fc + base);
        for (attr_start = first + base; attr_start < base + 0x0400 - 8; attr_start += 8) {
            color = (spriteram_2.READ_WORD(attr_start + 4) & 0x000f) + 16 * spritepalettebank;
            colmask[color] |= 0xffff;
        }

        for (color = 0; color < 16; color++) {
            for (i = 0; i < 15; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
        }

        if (spriteram_2_size[0] > 0x400) /* turbofrc, not pspikes */ {
            for (color = 0; color < 16; color++) {
                colmask[color] = 0;
            }

            pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;

            base = 0x0400;
            first = 8 * spriteram_2.READ_WORD(0x3fc + base);
            for (attr_start = first + base; attr_start < base + 0x0400 - 8; attr_start += 8) {
                color = (spriteram_2.READ_WORD(attr_start + 4) & 0x000f) + 16 * spritepalettebank;
                colmask[color] |= 0xffff;
            }

            for (color = 0; color < 16; color++) {
                for (i = 0; i < 15; i++) {
                    if ((colmask[color] & (1 << i)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                    }
                }
            }
        }
    }

    static void aerofgt_drawsprites(osd_bitmap bitmap, int priority) {
        int offs;

        priority <<= 12;

        offs = 0;
        while (offs < 0x0800 && (spriteram_2.READ_WORD(offs) & 0x8000) == 0) {
            int attr_start;

            attr_start = 8 * (spriteram_2.READ_WORD(offs) & 0x03ff);

            /* is the way I handle priority correct? Or should I just check bit 13? */
            if ((spriteram_2.READ_WORD(attr_start + 4) & 0x3000) == priority) {
                int map_start;
                int ox, oy, x, y, xsize, ysize, zoomx, zoomy, flipx, flipy, color;
                /* table hand made by looking at the ship explosion in attract mode */
                /* it's almost a logarithmic scale but not exactly */
                int zoomtable[] = {0, 7, 14, 20, 25, 30, 34, 38, 42, 46, 49, 52, 54, 57, 59, 61};

                ox = spriteram_2.READ_WORD(attr_start + 2) & 0x01ff;
                xsize = (spriteram_2.READ_WORD(attr_start + 2) & 0x0e00) >> 9;
                zoomx = (spriteram_2.READ_WORD(attr_start + 2) & 0xf000) >> 12;
                oy = spriteram_2.READ_WORD(attr_start + 0) & 0x01ff;
                ysize = (spriteram_2.READ_WORD(attr_start + 0) & 0x0e00) >> 9;
                zoomy = (spriteram_2.READ_WORD(attr_start + 0) & 0xf000) >> 12;
                flipx = spriteram_2.READ_WORD(attr_start + 4) & 0x4000;
                flipy = spriteram_2.READ_WORD(attr_start + 4) & 0x8000;
                color = (spriteram_2.READ_WORD(attr_start + 4) & 0x0f00) >> 8;
                map_start = 2 * (spriteram_2.READ_WORD(attr_start + 6) & 0x3fff);

                zoomx = 16 - zoomtable[zoomx] / 8;
                zoomy = 16 - zoomtable[zoomy] / 8;

                for (y = 0; y <= ysize; y++) {
                    int sx, sy;

                    if (flipy != 0) {
                        sy = ((oy + zoomy * (ysize - y) + 16) & 0x1ff) - 16;
                    } else {
                        sy = ((oy + zoomy * y + 16) & 0x1ff) - 16;
                    }

                    for (x = 0; x <= xsize; x++) {
                        int code;

                        if (flipx != 0) {
                            sx = ((ox + zoomx * (xsize - x) + 16) & 0x1ff) - 16;
                        } else {
                            sx = ((ox + zoomx * x + 16) & 0x1ff) - 16;
                        }

                        code = spriteram.READ_WORD(map_start) & 0x1fff;
                        if (zoomx == 16 && zoomy == 16) {
                            drawgfx(bitmap, Machine.gfx[map_start >= 0x4000 ? 2 : 1],
                                    code,
                                    color,
                                    flipx, flipy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                        } else {
                            drawgfxzoom(bitmap, Machine.gfx[map_start >= 0x4000 ? 2 : 1],
                                    code,
                                    color,
                                    flipx, flipy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 15,
                                    0x1000 * zoomx, 0x1000 * zoomy);
                        }
                        map_start += 2;
                    }
                }
            }

            offs += 2;
        }
    }

    static void turbofrc_drawsprites(osd_bitmap bitmap, int priority) {
        int attr_start, base, first;

        base = (priority & 1) * 0x0400;
        first = 8 * spriteram_2.READ_WORD(0x3fc + base);
        priority = (priority & 2) << 3;

        for (attr_start = first + base; attr_start < base + 0x0400 - 8; attr_start += 8) {
            if ((spriteram_2.READ_WORD(attr_start + 4) & 0x0010) == priority) {
                int map_start;
                int ox, oy, x, y, xsize, ysize, zoomx, zoomy, flipx, flipy, color;
                /* table hand made by looking at the ship explosion in attract mode */
                /* it's almost a logarithmic scale but not exactly */
                int zoomtable[] = {0, 7, 14, 20, 25, 30, 34, 38, 42, 46, 49, 52, 54, 57, 59, 61};

                ox = spriteram_2.READ_WORD(attr_start + 2) & 0x01ff;
                xsize = (spriteram_2.READ_WORD(attr_start + 4) & 0x0700) >> 8;
                zoomx = (spriteram_2.READ_WORD(attr_start + 2) & 0xf000) >> 12;
                oy = spriteram_2.READ_WORD(attr_start + 0) & 0x01ff;
                ysize = (spriteram_2.READ_WORD(attr_start + 4) & 0x7000) >> 12;
                zoomy = (spriteram_2.READ_WORD(attr_start + 0) & 0xf000) >> 12;
                flipx = spriteram_2.READ_WORD(attr_start + 4) & 0x0800;
                flipy = spriteram_2.READ_WORD(attr_start + 4) & 0x8000;
                color = (spriteram_2.READ_WORD(attr_start + 4) & 0x000f) + 16 * spritepalettebank;
                map_start = 2 * (spriteram_2.READ_WORD(attr_start + 6) & 0x1fff);
                if (attr_start >= 0x0400) {
                    map_start |= 0x4000;
                }

                zoomx = 16 - zoomtable[zoomx] / 8;
                zoomy = 16 - zoomtable[zoomy] / 8;

                for (y = 0; y <= ysize; y++) {
                    int sx, sy;

                    if (flipy != 0) {
                        sy = ((oy + zoomy * (ysize - y) + 16) & 0x1ff) - 16;
                    } else {
                        sy = ((oy + zoomy * y + 16) & 0x1ff) - 16;
                    }

                    for (x = 0; x <= xsize; x++) {
                        int code;

                        if (flipx != 0) {
                            sx = ((ox + zoomx * (xsize - x) + 16) & 0x1ff) - 16;
                        } else {
                            sx = ((ox + zoomx * x + 16) & 0x1ff) - 16;
                        }

                        code = spriteram.READ_WORD(map_start) & 0x3fff;
                        if (zoomx == 16 && zoomy == 16) {
                            drawgfx(bitmap, Machine.gfx[map_start >= 0x4000 ? 2 : 1],
                                    code,
                                    color,
                                    flipx, flipy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                        } else {
                            drawgfxzoom(bitmap, Machine.gfx[map_start >= 0x4000 ? 2 : 1],
                                    code,
                                    color,
                                    flipx, flipy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 15,
                                    0x1000 * zoomx, 0x1000 * zoomy);
                        }
                        map_start += 2;
                    }

                    if (xsize == 2) {
                        map_start += 2;
                    }
                    if (xsize == 4) {
                        map_start += 6;
                    }
                    if (xsize == 5) {
                        map_start += 4;
                    }
                    if (xsize == 6) {
                        map_start += 2;
                    }
                }
            }
        }
    }
    public static VhUpdatePtr pspikes_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            palette_init_used_colors();
            bg_dopalette();
            turbofrc_spr_dopalette();
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);
                //		memset(dirtybuffer2,1,aerofgt_bg2videoram_size / 2);
            }

            for (offs = aerofgt_bg1videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs / 2] != 0) {
                    int sx, sy, code;

                    dirtybuffer[offs / 2] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    code = aerofgt_bg1videoram.READ_WORD(offs);
                    drawgfx(tmpbitmap, Machine.gfx[0],
                            (code & 0x07ff) + (gfxbank.read(0 + ((code & 0x1800) >> 11)) << 11),
                            ((code & 0xe000) >> 13) + 8 * charpalettebank,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scrollx = new int[256];
                int scrolly;

                scrolly = -bg1scrolly.READ_WORD(0);
                for (offs = 0; offs < 256; offs++) {
                    scrollx[(offs - scrolly) & 0x0ff] = -aerofgt_rasterram.READ_WORD(2 * offs);
                }
                copyscrollbitmap(bitmap, tmpbitmap, 256, scrollx, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            turbofrc_drawsprites(bitmap, 0);
            //	turbofrc_drawsprites(bitmap,1);

            turbofrc_drawsprites(bitmap, 2);
            //	turbofrc_drawsprites(bitmap,3);
        }
    };
    static int drawbg2 = 1;
    public static VhUpdatePtr turbofrc_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (keyboard_pressed_memory(KEYCODE_SPACE) != 0) {
                drawbg2 = NOT(drawbg2);
            }

            palette_init_used_colors();
            bg_dopalette();
            turbofrc_spr_dopalette();
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);
                memset(dirtybuffer2, 1, aerofgt_bg2videoram_size[0] / 2);
            }

            for (offs = aerofgt_bg1videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs / 2] != 0) {
                    int sx, sy, code;

                    dirtybuffer[offs / 2] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    code = aerofgt_bg1videoram.READ_WORD(offs);
                    drawgfx(tmpbitmap, Machine.gfx[0],
                            (code & 0x07ff) + (gfxbank.read(0 + ((code & 0x1800) >> 11)) << 11),
                            (code & 0xe000) >> 13,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            for (offs = aerofgt_bg2videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer2[offs / 2] != 0) {
                    int sx, sy, code;

                    dirtybuffer2[offs / 2] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    code = aerofgt_bg2videoram.READ_WORD(offs);

                    drawgfx(tmpbitmap2, Machine.gfx[0],
                            bg2_chardisplacement + (code & 0x07ff) + (gfxbank.read(4 + ((code & 0x1800) >> 11)) << 11),
                            ((code & 0xe000) >> 13) + 16,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scrollx = new int[512];
                int scrolly;

                scrolly = -bg1scrolly.READ_WORD(0) - 2;
                for (offs = 0; offs < 256; offs++) //			scrollx[(offs - scrolly) & 0x1ff] = -READ_WORD(&aerofgt_rasterram[2*offs])+11;
                {
                    scrollx[(offs - scrolly) & 0x1ff] = -aerofgt_rasterram.READ_WORD(0xe) + 11;
                }
                copyscrollbitmap(bitmap, tmpbitmap, 512, scrollx, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            turbofrc_drawsprites(bitmap, 0);
            turbofrc_drawsprites(bitmap, 1);

            {
                int scrollx, scrolly;

                scrollx = -bg2scrollx.READ_WORD(0) + 7;
                scrolly = -bg2scrolly.READ_WORD(0) - 2;
                if (drawbg2 != 0) {
                    copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
                }
            }

            turbofrc_drawsprites(bitmap, 2);
            turbofrc_drawsprites(bitmap, 3);
        }
    };
    public static VhUpdatePtr aerofgt_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            palette_init_used_colors();
            bg_dopalette();
            aerofgt_spr_dopalette();
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, aerofgt_bg1videoram_size[0] / 2);
                memset(dirtybuffer2, 1, aerofgt_bg2videoram_size[0] / 2);
            }

            for (offs = aerofgt_bg1videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs / 2] != 0) {
                    int sx, sy, code;

                    dirtybuffer[offs / 2] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    code = aerofgt_bg1videoram.READ_WORD(offs);
                    drawgfx(tmpbitmap, Machine.gfx[0],
                            (code & 0x07ff) + (gfxbank.read(0 + ((code & 0x1800) >> 11)) << 11),
                            (code & 0xe000) >> 13,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            for (offs = aerofgt_bg2videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer2[offs / 2] != 0) {
                    int sx, sy, code;

                    dirtybuffer2[offs / 2] = 0;

                    sx = (offs / 2) % 64;
                    sy = (offs / 2) / 64;

                    code = aerofgt_bg2videoram.READ_WORD(offs);
                    drawgfx(tmpbitmap2, Machine.gfx[0],
                            bg2_chardisplacement + (code & 0x07ff) + (gfxbank.read(4 + ((code & 0x1800) >> 11)) << 11),
                            ((code & 0xe000) >> 13) + 16,
                            0, 0,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scrollx, scrolly;

                scrollx = -aerofgt_rasterram.READ_WORD(0x0000) + 18;
                scrolly = -bg1scrolly.READ_WORD(0);
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            aerofgt_drawsprites(bitmap, 0);
            aerofgt_drawsprites(bitmap, 1);

            {
                int scrollx, scrolly;

                scrollx = -aerofgt_rasterram.READ_WORD(0x0400) + 20;
                scrolly = -bg2scrolly.READ_WORD(0);
                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
            }

            aerofgt_drawsprites(bitmap, 2);
            aerofgt_drawsprites(bitmap, 3);
        }
    };
}
