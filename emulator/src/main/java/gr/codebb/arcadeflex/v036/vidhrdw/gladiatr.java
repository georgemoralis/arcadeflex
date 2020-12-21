/**
 * *************************************************************************
 * Video Hardware description for Taito Gladiator
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

import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;

public class gladiatr {

    static int video_attributes;
    static int base_scroll;
    static int background_scroll;
    static int sprite_bank;

    public static UBytePtr gladiatr_scroll = new UBytePtr();
    public static UBytePtr gladiator_text = new UBytePtr();

    static void update_color(int offset) {
        int r, g, b;

        r = (paletteram.read(offset) >> 0) & 0x0f;
        g = (paletteram.read(offset) >> 4) & 0x0f;
        b = (paletteram_2.read(offset) >> 0) & 0x0f;

        r = (r << 1) + ((paletteram_2.read(offset) >> 4) & 0x01);
        g = (g << 1) + ((paletteram_2.read(offset) >> 5) & 0x01);
        b = (b << 1) + ((paletteram_2.read(offset) >> 6) & 0x01);

        r = (r << 3) | (r >> 2);
        g = (g << 3) | (g >> 2);
        b = (b << 3) | (b >> 2);

        palette_change_color(offset, r, g, b);

        /* the text layer might use the other 512 entries in the palette RAM */
        /* (which are all set to 0x07ff = white). I don't know, so I just set */
        /* it to white. */
        palette_change_color(512, 0x00, 0x00, 0x00);
        palette_change_color(513, 0xff, 0xff, 0xff);
    }

    public static WriteHandlerPtr gladiatr_paletteram_rg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            paletteram.write(offset, data);
            update_color(offset);
        }
    };

    public static WriteHandlerPtr gladiatr_paletteram_b_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            paletteram_2.write(offset, data);
            update_color(offset);
        }
    };

    public static WriteHandlerPtr gladiatr_spritebank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sprite_bank = (data != 0) ? 4 : 2;
        }
    };

    public static ReadHandlerPtr gladiatr_video_registers_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0x080:
                    return video_attributes;
                case 0x100:
                    return base_scroll;
                case 0x300:
                    return background_scroll;
            }
            return 0;
        }
    };

    public static WriteHandlerPtr gladiatr_video_registers_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x000:
                    break;
                case 0x080:
                    video_attributes = data;
                    break;
                case 0x100:
                    base_scroll = data;
                    break;
                case 0x200:
                    break;
                case 0x300:
                    background_scroll = data;
                    break;
            }
        }
    };

    public static VhStartPtr gladiatr_vh_start = new VhStartPtr() {
        public int handler() {
            sprite_bank = 2;

            dirtybuffer = new char[64 * 32];
            if (dirtybuffer != null) {
                tmpbitmap = osd_new_bitmap(512, 256, Machine.scrbitmap.depth);
                if (tmpbitmap != null) {
                    memset(dirtybuffer, 1, 64 * 32);
                    return 0;
                }
                dirtybuffer = null;
            }
            return 1; /* error */

        }
    };

    public static VhStopPtr gladiatr_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap);
            dirtybuffer = null;
        }
    };

    static int tile_bank_select = 0;

    static void render_background(osd_bitmap bitmap) {
        int i;

        int scrollx = -background_scroll;

        if (base_scroll < 0xd0) {
            scrollx += 256 - (0xd0) - 64 - 32;

            if ((video_attributes & 0x04) != 0) {
                scrollx += 256;
            }
        } else {
            if ((video_attributes & 0x04) != 0) {
                scrollx += base_scroll;
            } else {
                scrollx += 256 - (0xd0) - 64 - 32;
            }
        }

        {
            int old_bank_select = tile_bank_select;
            if ((video_attributes & 0x10) != 0) {
                tile_bank_select = 256 * 8;
            } else {
                tile_bank_select = 0;
            }
            if (old_bank_select != tile_bank_select) {
                memset(dirtybuffer, 1, 64 * 32);
            }
        }

        for (i = 0; i < 64 * 32; i++) {
            if (dirtybuffer[i] != 0) {
                int sx = (i % 64) * 8;
                int sy = (i / 64) * 8;

                int attributes = colorram.read(i);
                int color = 0x1F - (attributes >> 3);
                int tile_number = videoram.read(i) + 256 * (attributes & 0x7) + tile_bank_select;

                drawgfx(tmpbitmap, Machine.gfx[1 + (tile_number / 512)],
                        tile_number % 512,
                        color,
                        0, 0, /* no flip */
                        sx, sy,
                        null, /* no need to clip */
                        TRANSPARENCY_NONE, 0);

                dirtybuffer[i] = 0;
            }
        }

        copyscrollbitmap(bitmap, tmpbitmap,
                1, new int[]{scrollx},
                0, null,
                Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
    }

    static void render_text(osd_bitmap bitmap) {
        rectangle clip = Machine.drv.visible_area;
        GfxElement gfx = Machine.gfx[0];

        int tile_bank_offset = (video_attributes & 3) * 256;

        UBytePtr source = new UBytePtr(gladiator_text);

        int sx, sy;

        int dx;

        if (base_scroll < 0xd0) { /* panning text */

            dx = 256 - (0xd0) - 64 - 32 - background_scroll;
            if ((video_attributes & 0x04) != 0) {
                dx += 256;
            }
        } else { /* fixed text */

            dx = 0;
            if ((video_attributes & 0x08) == 0) {
                source.inc(32); /* page 2 */
            }
        }

        for (sy = 0; sy < 256; sy += 8) {
            for (sx = 0; sx < 256; sx += 8) {
                drawgfx(bitmap, gfx,
                        tile_bank_offset + source.readinc(),
                        0, /* color */
                        0, 0, /* no flip */
                        sx + dx, sy,
                        clip, TRANSPARENCY_PEN, 0);
            }
            source.inc(32); /* skip to next row */

        }
    }

    static void draw_sprite(osd_bitmap bitmap, int tile_number, int color, int sx, int sy, int xflip, int yflip, int big) {
        rectangle clip = Machine.drv.visible_area;

        int tile_offset[][] = {
            {0x0, 0x1, 0x4, 0x5},
            {0x2, 0x3, 0x6, 0x7},
            {0x8, 0x9, 0xC, 0xD},
            {0xA, 0xB, 0xE, 0xF}
        };

        int x, y;

        int size = big != 0 ? 4 : 2;

        for (y = 0; y < size; y++) {
            for (x = 0; x < size; x++) {
                int ex = xflip != 0 ? (size - 1 - x) : x;
                int ey = yflip != 0 ? (size - 1 - y) : y;

                int t = tile_offset[ey][ex] + tile_number;

                drawgfx(bitmap, Machine.gfx[1 + 8 + ((t / 512) % 12)],
                        t % 512,
                        color,
                        xflip, yflip,
                        sx + x * 8, sy + y * 8,
                        clip, TRANSPARENCY_PEN, 0);
            }
        }
    }

    static void render_sprites(osd_bitmap bitmap) {
        UBytePtr source = new UBytePtr(spriteram);
        int finish = source.offset + 0x400;//unsigned char *finish = source+0x400;

        do {
            int attributes = source.read(0x800);
            int big = attributes & 0x10;
            int bank = (attributes & 0x1) + ((attributes & 2) != 0 ? sprite_bank : 0);
            int tile_number = (source.read(0) + 256 * bank) * 4;
            int sx = source.read(0x400 + 1) + 256 * (source.read(0x801) & 1);
            int sy = 240 - source.read(0x400) - (big != 0 ? 16 : 0);
            int xflip = attributes & 0x04;
            int yflip = attributes & 0x08;
            int color = 0x20 + (source.read(1) & 0x1F);

            if (((video_attributes & 0x04) != 0) && (base_scroll < 0xd0)) {
                sx += 256 - 64 + 8 - 0xD0 - 64 + 8;
            } else {
                sx += base_scroll - 0xD0 - 64 + 8;
            }

            draw_sprite(bitmap, tile_number, color, sx, sy, xflip, yflip, big);

            source.inc(2);
        } while (source.offset < finish);
    }

    public static VhUpdatePtr gladiatr_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if ((video_attributes & 0x20) != 0) /* screen refresh enable? */ {
                if (palette_recalc() != null) {
                    memset(dirtybuffer, 1, 64 * 32);
                }

                render_background(bitmap);
                render_sprites(bitmap);
                render_text(bitmap);
            }
        }
    };
}
