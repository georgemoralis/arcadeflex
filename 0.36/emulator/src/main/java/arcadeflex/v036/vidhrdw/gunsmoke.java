/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 22/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class gunsmoke {

    public static UBytePtr gunsmoke_bg_scrolly = new UBytePtr();
    public static UBytePtr gunsmoke_bg_scrollx = new UBytePtr();
    static int chon, objon, bgon;
    static int sprite3bank;
    static int flipscreen;

    static osd_bitmap bgbitmap;
    static char[][][] bgmap = new char[9][9][2];

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr gunsmoke_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = (char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 64-79 */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) ((color_prom.readinc()) + 64);
            }
            color_prom.inc(128);
            /* skip the bottom half of the PROM - not used */

 /* background tiles use colors 0-63 */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (color_prom.read(0) + 16 * (color_prom.read(256) & 0x03));
                color_prom.inc();
            }
            color_prom.inc(TOTAL_COLORS(1));

            /* sprites use colors 128-255 */
            for (i = 0; i < TOTAL_COLORS(2); i++) {
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (color_prom.read(0) + 16 * (color_prom.read(256) & 0x07) + 128);
                color_prom.inc();
            }
            color_prom.inc(TOTAL_COLORS(2));
        }
    };

    public static VhStartHandlerPtr gunsmoke_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if ((bgbitmap = osd_create_bitmap(9 * 32, 9 * 32)) == null) {
                return 1;
            }

            if (generic_vh_start.handler() == 1) {
                osd_free_bitmap(bgbitmap);
                return 1;
            }

            //memset (bgmap, 0xff, sizeof (bgmap));
            for (int i = 0; i < bgmap.length; i++) {
                for (int k = 0; k < bgmap[i].length; k++) {
                    for (int z = 0; z < bgmap[i][k].length; z++) {
                        bgmap[i][k][z] = 0xff;
                    }
                }
            }
            return 0;
        }
    };

    public static VhStopHandlerPtr gunsmoke_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(bgbitmap);
        }
    };

    public static WriteHandlerPtr gunsmoke_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* bits 0 and 1 are for coin counters? - we ignore them */
 /* bits 2 and 3 select the ROM bank */
            bankaddress = 0x10000 + (data & 0x0c) * 0x1000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* bit 5 resets the sound CPU? - we ignore it */
 /* bit 6 flips screen */
            if (flipscreen != (data & 0x40)) {
                flipscreen = data & 0x40;
                //		memset(dirtybuffer,1,c1942_backgroundram_size);
            }

            /* bit 7 enables characters? */
            chon = data & 0x80;
        }
    };

    public static WriteHandlerPtr gunsmoke_d806_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0-2 select the sprite 3 bank */
            sprite3bank = data & 0x07;

            /* bit 4 enables bg 1? */
            bgon = data & 0x10;

            /* bit 5 enables sprites? */
            objon = data & 0x20;
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
    public static VhUpdateHandlerPtr gunsmoke_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;
            int bg_scrolly, bg_scrollx;
            UBytePtr p = memory_region(REGION_GFX4);
            int top, left, xscroll, yscroll;

            /* TODO: support flipscreen */
            if (bgon != 0) {
                bg_scrolly = gunsmoke_bg_scrolly.read(0) + 256 * gunsmoke_bg_scrolly.read(1);
                bg_scrollx = gunsmoke_bg_scrollx.read(0);
                offs = 16 * ((bg_scrolly >> 5) + 8) + 2 * (bg_scrollx >> 5);
                if ((bg_scrollx & 0x80) != 0) {
                    offs -= 0x10;
                }

                top = 8 - (bg_scrolly >> 5) % 9;
                left = (bg_scrollx >> 5) % 9;

                bg_scrolly &= 0x1f;
                bg_scrollx &= 0x1f;

                for (sy = 0; sy < 9; sy++) {
                    int ty = (sy + top) % 9;
                    offs &= 0x7fff;
                    /* Enforce limits (for top of scroll) */

                    for (sx = 0; sx < 9; sx++) {
                        int tile, attr, offset;
                        int tx = (sx + left) % 9;
                        //unsigned char *map = &bgmap[ty][tx][0];
                        UBytePtr map = new UBytePtr(bgmap[ty][tx], 0);
                        offset = offs + (sx * 2);

                        tile = p.read(offset);
                        attr = p.read(offset + 1);

                        if (tile != map.read(0) || attr != map.read(1)) {
                            map.write(0, tile);
                            map.write(1, attr);
                            tile += 256 * (attr & 0x01);
                            drawgfx(bgbitmap, Machine.gfx[1],
                                    tile,
                                    (attr & 0x3c) >> 2,
                                    attr & 0x40, attr & 0x80,
                                    (8 - ty) * 32, tx * 32,
                                    null,
                                    TRANSPARENCY_NONE, 0);
                        }
                        //map += 2;
                    }
                    offs -= 0x10;
                }

                xscroll = (top * 32 - bg_scrolly);
                yscroll = -(left * 32 + bg_scrollx);
                copyscrollbitmap(bitmap, bgbitmap,
                        1, new int[]{xscroll},
                        1, new int[]{yscroll},
                        Machine.drv.visible_area,
                        TRANSPARENCY_NONE, 0);
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            }

            if (objon != 0) {
                /* Draw the sprites. */
                for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                    int bank, flipx, flipy;

                    bank = (spriteram.read(offs + 1) & 0xc0) >> 6;
                    if (bank == 3) {
                        bank += sprite3bank;
                    }

                    sx = spriteram.read(offs + 3) - ((spriteram.read(offs + 1) & 0x20) << 3);
                    sy = spriteram.read(offs + 2);
                    flipx = 0;
                    flipy = spriteram.read(offs + 1) & 0x10;
                    if (flipscreen != 0) {
                        sx = 240 - sx;
                        sy = 240 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs) + 256 * bank,
                            spriteram.read(offs + 1) & 0x0f,
                            flipx, flipy,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            if (chon != 0) {
                /* draw the frontmost playfield. They are characters, but draw them as sprites */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0xc0) << 2),
                            colorram.read(offs) & 0x1f,
                            NOT(flipscreen), NOT(flipscreen),
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_COLOR, 79);
                }
            }
        }
    };
}
