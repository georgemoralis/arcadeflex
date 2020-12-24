/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;

public class gsword {

    public static int[] gs_videoram_size = new int[1];
    public static int[] gs_spritexy_size = new int[1];

    public static UBytePtr gs_videoram = new UBytePtr();
    public static UBytePtr gs_scrolly_ram = new UBytePtr();
    public static UBytePtr gs_spritexy_ram = new UBytePtr();
    public static UBytePtr gs_spritetile_ram = new UBytePtr();
    public static UBytePtr gs_spriteattrib_ram = new UBytePtr();

    static osd_bitmap bitmap_bg;
    static char[] dirtybuffer;
    static int charbank, charpalbank;
    static int flipscreen;

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr gsword_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            /* sprite lookup table is not original but it is almost 98% correct */

            int sprite_lookup_table[] = {0x00, 0x02, 0x05, 0x8C, 0x49, 0xDD, 0xB7, 0x06,
                0xD5, 0x7A, 0x85, 0x8D, 0x27, 0x1A, 0x03, 0x0F};
            int i;

		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 1;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 1;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 1;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 3) & 1;
                bit1 = (color_prom.read(0) >> 0) & 1;
                bit2 = (color_prom.read(0) >> 1) & 1;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read(0) >> 2) & 1;
                bit2 = (color_prom.read(0) >> 3) & 1;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            color_prom.inc(Machine.drv.total_colors);
            /* color_prom now points to the beginning of the sprite lookup table */

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (i);
            }

            /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (sprite_lookup_table[(color_prom.readinc())]);
            }
        }
    };

    public static VhStartPtr gsword_vh_start = new VhStartPtr() {
        public int handler() {
            if ((dirtybuffer = new char[gs_videoram_size[0]]) == null) {
                return 1;
            }
            if ((bitmap_bg = osd_create_bitmap(Machine.drv.screen_width, 2 * Machine.drv.screen_height)) == null) {
                dirtybuffer = null;
                return 1;
            }
            memset(dirtybuffer, 1, gs_videoram_size[0]);
            return 0;
        }
    };

    public static VhStopPtr gsword_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer = null;
            osd_free_bitmap(bitmap_bg);
        }
    };

    public static WriteHandlerPtr gs_charbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (charbank != data) {
                charbank = data;
                memset(dirtybuffer, 1, gs_videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr gs_videoctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*if ((data & 0x8f) != 0)
             {
             char baf[40];
             sprintf(baf,"videoctrl %02x",data);
             usrintf_showmessage(baf);
             }*/
            /* bits 5-6 are char palette bank */
            if (charpalbank != ((data & 0x60) >> 5)) {
                charpalbank = (data & 0x60) >> 5;
                memset(dirtybuffer, 1, gs_videoram_size[0]);
            }
            /* bit 4 is flip screen */
            if (flipscreen != (data & 0x10)) {
                flipscreen = data & 0x10;
                memset(dirtybuffer, 1, gs_videoram_size[0]);
            }

            /* bit 0 could be used but unknown */
            /* other bits unused */
        }
    };

    public static WriteHandlerPtr gs_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (gs_videoram.read(offset) != data) {
                dirtybuffer[offset] = 1;
                gs_videoram.write(offset, data);
            }
        }
    };

    public static void render_background(osd_bitmap bitmap) {
        int offs;

        /* for every character in the Video RAM, check if it has been modified */
        /* since last time and update it accordingly. */
        for (offs = 0; offs < gs_videoram_size[0]; offs++) {
            if (dirtybuffer[offs] != 0) {
                int sx, sy, tile, flipx, flipy;

                dirtybuffer[offs] = 0;

                sx = offs % 32;
                sy = offs / 32;
                flipx = 0;
                flipy = 0;

                if (flipscreen != 0) {
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                tile = gs_videoram.read(offs) + ((charbank & 0x03) << 8);

                drawgfx(bitmap_bg, Machine.gfx[0],
                        tile,
                        ((tile & 0x3c0) >> 6) + 16 * charpalbank,
                        flipx, flipy,
                        8 * sx, 8 * sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }
    }

    public static void render_sprites(osd_bitmap bitmap) {
        int offs;

        for (offs = 0; offs < gs_spritexy_size[0] - 1; offs += 2) {
            int sx, sy, flipx, flipy, spritebank, tile;

            if (gs_spritexy_ram.read(offs) != 0xf1) {
                spritebank = 0;
                tile = gs_spritetile_ram.read(offs);
                sy = 241 - gs_spritexy_ram.read(offs);
                sx = gs_spritexy_ram.read(offs + 1) - 56;
                flipx = gs_spriteattrib_ram.read(offs) & 0x02;
                flipy = gs_spriteattrib_ram.read(offs) & 0x01;

                // Adjust sprites that should be far far right!
                if (sx < 0) {
                    sx += 256;
                }

                // Adjuste for 32x32 tiles(#128-256)
                if (tile > 127) {
                    spritebank = 1;
                    tile -= 128;
                    sy -= 16;
                }
                if (flipscreen != 0) {
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }
                drawgfx(bitmap, Machine.gfx[1 + spritebank],
                        tile,
                        gs_spritetile_ram.read(offs + 1) & 0x3f,
                        flipx, flipy,
                        sx, sy,
                        Machine.drv.visible_area, TRANSPARENCY_COLOR, 15);
            }
        }
    }

    public static VhUpdatePtr gsword_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int scrollx = 0, scrolly = -(gs_scrolly_ram.read());

            render_background(bitmap_bg);
            copyscrollbitmap(bitmap, bitmap_bg, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            render_sprites(bitmap);
        }
    };

}
