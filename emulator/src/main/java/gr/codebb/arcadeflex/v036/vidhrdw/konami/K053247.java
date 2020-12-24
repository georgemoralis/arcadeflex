package gr.codebb.arcadeflex.v036.vidhrdw.konami;

import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.mame.mameH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.paletteH.PALETTE_COLOR_VISIBLE;

public class K053247 {

    //K053247_callback interface
    public static abstract interface K053247_callbackProcPtr {

        public abstract void handler(int[] code, int[] color, int[] priority);
    }

    public static int K053247_memory_region;
    public static GfxElement K053247_gfx;
    public static K053247_callbackProcPtr K053247_callback;
    public static int K053246_OBJCHA_line;
    public static int K053246_romoffset;
    public static int K053247_flipscreenX, K053247_flipscreenY;
    public static int K053247_spriteoffsX, K053247_spriteoffsY;
    public static UBytePtr K053247_ram;
    public static int K053247_irq_enabled;

    public static int K053247_vh_start(int gfx_memory_region, int plane0, int plane1, int plane2, int plane3, K053247_callbackProcPtr callback) {
        int gfx_index;
        GfxLayout spritelayout = new GfxLayout(
                16, 16,
                0, /* filled in later */
                4,
                new int[]{0, 0, 0, 0}, /* filled in later */
                new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4,
                    10 * 4, 11 * 4, 8 * 4, 9 * 4, 14 * 4, 15 * 4, 12 * 4, 13 * 4},
                new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                    8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
                128 * 8
        );


        /* find first empty slot to decode gfx */
        for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++) {
            if (Machine.gfx[gfx_index] == null) {
                break;
            }
        }
        if (gfx_index == MAX_GFX_ELEMENTS) {
            return 1;
        }

        /* tweak the structure for the number of tiles we have */
        spritelayout.total = memory_region_length(gfx_memory_region) / 128;
        spritelayout.planeoffset[0] = plane0;
        spritelayout.planeoffset[1] = plane1;
        spritelayout.planeoffset[2] = plane2;
        spritelayout.planeoffset[3] = plane3;

        /* decode the graphics */
        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region), spritelayout);
        if (Machine.gfx[gfx_index] == null) {
            return 1;
        }

        /* set the color information */
        Machine.gfx[gfx_index].colortable = new UShortArray(Machine.remapped_colortable);
        Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

        K053247_memory_region = gfx_memory_region;
        K053247_gfx = Machine.gfx[gfx_index];
        K053247_callback = callback;
        K053246_OBJCHA_line = CLEAR_LINE;
        K053247_ram = new UBytePtr(0x1000);

        memset(K053247_ram, 0, 0x1000);

        return 0;
    }

    public static void K053247_vh_stop() {
        K053247_ram = null;
    }
    public static ReadHandlerPtr K053247_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int read = K053247_ram.READ_WORD(offset);
            if (konamiicclog != null) {
                fprintf(konamiicclog, "K053247_word_r offset=%d return=%d\n", offset, read);
            }
            return read;
        }
    };
    public static WriteHandlerPtr K053247_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(K053247_ram, offset, data);
        }
    };
    public static ReadHandlerPtr K053247_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int shift = ((offset & 1) ^ 1) << 3;
            int read = (K053247_ram.READ_WORD(offset & ~1) >>> shift) & 0xff;  //unsigned shift?
            if (konamiicclog != null) {
                fprintf(konamiicclog, "K053247_r offset=%d shift=%d return=%d\n", offset, shift, read);
            }
            return read;
        }
    };
    public static WriteHandlerPtr K053247_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int shift = ((offset & 1) ^ 1) << 3;
            offset &= ~1;
            COMBINE_WORD_MEM(K053247_ram, offset, (0xff000000 >>> shift) | ((data & 0xff) << shift));//unsigned shift
        }
    };

    public static ReadHandlerPtr K053246_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (K053246_OBJCHA_line == ASSERT_LINE) {
                int addr;

                addr = 2 * K053246_romoffset + ((offset & 1) ^ 1);
                addr &= memory_region_length(K053247_memory_region) - 1;

                return memory_region(K053247_memory_region).read(addr);
            } else {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: read from unknown 053244 address %x\n", cpu_get_pc(), offset);
                }
                return 0;
            }
        }
    };

    public static WriteHandlerPtr K053246_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0x00) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0x00ff) | (data << 8);
            } else if (offset == 0x01) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0xff00) | data;
            } else if (offset == 0x02) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0x00ff) | (data << 8);
            } else if (offset == 0x03) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0xff00) | data;
            } else if (offset == 0x05) {
                /* bit 0/1 = flip screen */
                K053247_flipscreenX = data & 0x01;
                K053247_flipscreenY = data & 0x02;

                /* bit 2 = unknown */

                /* bit 4 = interrupt enable */
                K053247_irq_enabled = data & 0x10;

                /* bit 5 = unknown */
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: write %02x to 053246 address 5\n", cpu_get_pc(), data);
                }
            } else if (offset >= 0x04 && offset < 0x08) /* only 4,6,7 - 5 is handled above */ {
                offset = 8 * (((offset & 0x03) ^ 0x01) - 1);
                K053246_romoffset = (K053246_romoffset & ~(0xff << offset)) | (data << offset);
                return;
            } else if (errorlog != null) {
                fprintf(errorlog, "%04x: write %02x to unknown 053246 address %x\n", cpu_get_pc(), data, offset);
            }
        }
    };
    public static ReadHandlerPtr K053246_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            return K053246_r.handler(offset + 1) | (K053246_r.handler(offset) << 8);
        }
    };
    public static WriteHandlerPtr K053246_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if ((data & 0xff000000) == 0) {
                K053246_w.handler(offset, (data >>> 8) & 0xff);
            }
            if ((data & 0x00ff0000) == 0) {
                K053246_w.handler(offset + 1, data & 0xff);
            }
        }
    };

    public static void K053246_set_OBJCHA_line(int state) {
        K053246_OBJCHA_line = state;
    }

    /*
     * Sprite Format
     * ------------------
     *
     * Word | Bit(s)           | Use
     * -----+-fedcba9876543210-+----------------
     *   0  | x--------------- | active (show this sprite)
     *   0  | -x-------------- | maintain aspect ratio (when set, zoom y acts on both axis)
     *   0  | --x------------- | flip y
     *   0  | ---x------------ | flip x
     *   0  | ----xxxx-------- | sprite size (see below)
     *   0  | ---------xxxxxxx | priority order
     *   1  | xxxxxxxxxxxxxxxx | sprite code
     *   2  | ------xxxxxxxxxx | y position
     *   3  | ------xxxxxxxxxx | x position
     *   4  | xxxxxxxxxxxxxxxx | zoom y (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
     *   5  | xxxxxxxxxxxxxxxx | zoom x (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
     *   6  | x--------------- | mirror y (top half is drawn as mirror image of the bottom)
     *   6  | -x-------------- | mirror x (right half is drawn as mirror image of the left)
     *   6  | -----x---------- | shadow
     *   6  | xxxxxxxxxxxxxxxx | "color", but depends on external connections
     *   7  | ---------------- |
     *
     * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
     * The rest of the sprite remains normal.
     */
    static int offsetkludge;

    public static void K053247_sprites_draw(osd_bitmap bitmap, int min_priority, int max_priority) {
        int NUM_SPRITES = 256;
        int offs, pri_code;
        int[] sortedlist = new int[NUM_SPRITES];

        for (offs = 0; offs < NUM_SPRITES; offs++) {
            sortedlist[offs] = -1;
        }

        /* prebuild a sorted table */
        for (offs = 0; offs < 0x1000; offs += 16) {
//		if (READ_WORD(&K053247_ram[offs]) & 0x8000)
            sortedlist[K053247_ram.READ_WORD(offs) & 0x00ff] = offs;
        }

        for (pri_code = NUM_SPRITES - 1; pri_code >= 0; pri_code--) {
            int ox, oy, size, w, h, x, y, xa, ya, flipx, flipy, mirrorx, mirrory, zoomx, zoomy;
            int color[] = new int[1];
            int code[] = new int[1];
            int pri[] = new int[1];
            /* sprites can be grouped up to 8x8. The draw order is
             0  1  4  5 16 17 20 21
             2  3  6  7 18 19 22 23
             8  9 12 13 24 25 28 29
             10 11 14 15 26 27 30 31
             32 33 36 37 48 49 52 53
             34 35 38 39 50 51 54 55
             40 41 44 45 56 57 60 61
             42 43 46 47 58 59 62 63
             */
            int xoffset[] = {0, 1, 4, 5, 16, 17, 20, 21};
            int yoffset[] = {0, 2, 8, 10, 32, 34, 40, 42};

            offs = sortedlist[pri_code];
            if (offs == -1) {
                continue;
            }

            if ((K053247_ram.READ_WORD(offs) & 0x8000) == 0) {
                continue;
            }

            code[0] = K053247_ram.READ_WORD(offs + 0x02);
            color[0] = K053247_ram.READ_WORD(offs + 0x0c);
            pri[0] = 0;

            K053247_callback.handler(code, color, pri);

            if (pri[0] < min_priority || pri[0] > max_priority) {
                continue;
            }

            size = (K053247_ram.READ_WORD(offs) & 0x0f00) >> 8;

            w = 1 << (size & 0x03);
            h = 1 << ((size >> 2) & 0x03);

            /* the sprite can start at any point in the 8x8 grid. We have to */
            /* adjust the offsets to draw it correctly. Simpsons does this all the time. */
            xa = 0;
            ya = 0;
            if ((code[0] & 0x01) != 0) {
                xa += 1;
            }
            if ((code[0] & 0x02) != 0) {
                ya += 1;
            }
            if ((code[0] & 0x04) != 0) {
                xa += 2;
            }
            if ((code[0] & 0x08) != 0) {
                ya += 2;
            }
            if ((code[0] & 0x10) != 0) {
                xa += 4;
            }
            if ((code[0] & 0x20) != 0) {
                ya += 4;
            }
            code[0] &= ~0x3f;


            /* zoom control:
             0x40 = normal scale
             <0x40 enlarge (0x20 = double size)
             >0x40 reduce (0x80 = half size)
             */
            zoomy = K053247_ram.READ_WORD(offs + 0x08);
            if (zoomy > 0x2000) {
                continue;
            }
            if (zoomy != 0) {
                zoomy = (0x400000 + zoomy / 2) / zoomy;
            } else {
                zoomy = 2 * 0x400000;
            }
            if ((K053247_ram.READ_WORD(offs) & 0x4000) == 0) {
                zoomx = K053247_ram.READ_WORD(offs + 0x0a);
                if (zoomx > 0x2000) {
                    continue;
                }
                if (zoomx != 0) {
                    zoomx = (0x400000 + zoomx / 2) / zoomx;
                } else {
                    zoomx = 2 * 0x400000;
                }
            } else {
                zoomx = zoomy;
            }

            ox = K053247_ram.READ_WORD(offs + 0x06);
            oy = K053247_ram.READ_WORD(offs + 0x04);

            /* TODO: it is not known how the global Y offset works */
            switch (K053247_spriteoffsY) {
                case 0x0261:	/* simpsons */

                case 0x0262:	/* simpsons (dreamland) */

                case 0x0263:	/* simpsons (dreamland) */

                case 0x0264:	/* simpsons (dreamland) */

                case 0x0265:	/* simpsons (dreamland) */

                case 0x006d:	/* simpsons flip (dreamland) */

                case 0x006e:	/* simpsons flip (dreamland) */

                case 0x006f:	/* simpsons flip (dreamland) */

                case 0x0070:	/* simpsons flip (dreamland) */

                case 0x0071:	/* simpsons flip */

                    offsetkludge = 0x017;
                    break;
                case 0x02f7:	/* vendetta (level 4 boss) */

                case 0x02f8:	/* vendetta (level 4 boss) */

                case 0x02f9:	/* vendetta (level 4 boss) */

                case 0x02fa:	/* vendetta */

                case 0x02fb:	/* vendetta (fat guy jumping) */

                case 0x02fc:	/* vendetta (fat guy jumping) */

                case 0x02fd:	/* vendetta (fat guy jumping) */

                case 0x02fe:	/* vendetta (fat guy jumping) */

                case 0x02ff:	/* vendetta (fat guy jumping) */

                case 0x03f7:	/* vendetta flip (level 4 boss) */

                case 0x03f8:	/* vendetta flip (level 4 boss) */

                case 0x03f9:	/* vendetta flip (level 4 boss) */

                case 0x03fa:	/* vendetta flip */

                case 0x03fb:	/* vendetta flip (fat guy jumping) */

                case 0x03fc:	/* vendetta flip (fat guy jumping) */

                case 0x03fd:	/* vendetta flip (fat guy jumping) */

                case 0x03fe:	/* vendetta flip (fat guy jumping) */

                case 0x03ff:	/* vendetta flip (fat guy jumping) */

                    offsetkludge = 0x006;
                    break;
                case 0x0292:	/* xmen */

                case 0x0072:	/* xmen flip */

                    offsetkludge = -0x002;
                    break;
                default:
                    offsetkludge = 0;
                    //usrintf_showmessage("unknown spriteoffsY %04x",K053247_spriteoffsY);
                    break;
            }

            flipx = K053247_ram.READ_WORD(offs) & 0x1000;
            flipy = K053247_ram.READ_WORD(offs) & 0x2000;
            mirrorx = K053247_ram.READ_WORD(offs + 0x0c) & 0x4000;
            mirrory = K053247_ram.READ_WORD(offs + 0x0c) & 0x8000;

            if (K053247_flipscreenX != 0) {
                ox = -ox;
                if (mirrorx == 0) {
                    flipx = NOT(flipx);
                }
            }
            if (K053247_flipscreenY != 0) {
                oy = -oy;
                if (mirrory == 0) {
                    flipy = NOT(flipy);
                }
            }

            ox = (ox + 0x35 - K053247_spriteoffsX) & 0x3ff;
            if (ox >= 768) {
                ox -= 1024;
            }
            oy = (-(oy + K053247_spriteoffsY + offsetkludge)) & 0x3ff;
            if (oy >= 640) {
                oy -= 1024;
            }

            /* the coordinates given are for the *center* of the sprite */
            ox -= (zoomx * w) >> 13;
            oy -= (zoomy * h) >> 13;

            for (y = 0; y < h; y++) {
                int sx, sy, zw, zh;

                sy = oy + ((zoomy * y + (1 << 11)) >> 12);
                zh = (oy + ((zoomy * (y + 1) + (1 << 11)) >> 12)) - sy;

                for (x = 0; x < w; x++) {
                    int c, fx, fy;

                    sx = ox + ((zoomx * x + (1 << 11)) >> 12);
                    zw = (ox + ((zoomx * (x + 1) + (1 << 11)) >> 12)) - sx;
                    c = code[0];
                    if (mirrorx != 0) {
                        if ((flipx == 0) ^ (2 * x < w)) {
                            /* mirror left/right */
                            c += xoffset[(w - 1 - x + xa) & 7];
                            fx = 1;
                        } else {
                            c += xoffset[(x + xa) & 7];
                            fx = 0;
                        }
                    } else {
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x + xa) & 7];
                        } else {
                            c += xoffset[(x + xa) & 7];
                        }
                        fx = flipx;
                    }
                    if (mirrory != 0) {
                        if ((flipy == 0) ^ (2 * y >= h)) {
                            /* mirror top/bottom */
                            c += yoffset[(h - 1 - y + ya) & 7];
                            fy = 1;
                        } else {
                            c += yoffset[(y + ya) & 7];
                            fy = 0;
                        }
                    } else {
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y + ya) & 7];
                        } else {
                            c += yoffset[(y + ya) & 7];
                        }
                        fy = flipy;
                    }

                    if (zoomx == 0x10000 && zoomy == 0x10000) {
                        /* hack to simulate shadow */
                        if ((K053247_ram.READ_WORD(offs + 0x0c) & 0x0400) != 0) {
                            int o = K053247_gfx.colortable.read(16 * color[0] + 15);
                            K053247_gfx.colortable.write(16 * color[0] + 15, palette_transparent_pen);
                            drawgfx(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, fy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PENS, (cpu_getcurrentframe() & 1) != 0 ? 0x8001 : 0x0001);
                            K053247_gfx.colortable.write(16 * color[0] + 15, o);
                        } else {
                            drawgfx(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, fy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    } else {
                        drawgfxzoom(bitmap, K053247_gfx,
                                c,
                                color[0],
                                fx, fy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                (zw << 16) / 16, (zh << 16) / 16);
                    }
                    if (mirrory != 0 && h == 1) /* Simpsons shadows */ {
                        if (zoomx == 0x10000 && zoomy == 0x10000) {
                            /* hack to simulate shadow */
                            if ((K053247_ram.READ_WORD(offs + 0x0c) & 0x0400) != 0) {
                                int o = K053247_gfx.colortable.read(16 * color[0] + 15);
                                K053247_gfx.colortable.write(16 * color[0] + 15, palette_transparent_pen);
                                drawgfx(bitmap, K053247_gfx,
                                        c,
                                        color[0],
                                        fx, NOT(fy),
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PENS, (cpu_getcurrentframe() & 1) != 0 ? 0x8001 : 0x0001);
                                K053247_gfx.colortable.write(16 * color[0] + 15, o);
                            } else {
                                drawgfx(bitmap, K053247_gfx,
                                        c,
                                        color[0],
                                        fx, NOT(fy),
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                            }
                        } else {
                            drawgfxzoom(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, NOT(fy),
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                    (zw << 16) / 16, (zh << 16) / 16);
                        }
                    }
                }
            }
        }
    }

    public static void K053247_mark_sprites_colors() {
        int offs, i;

        /*unsigned short*/ int[] palette_map = new int[512];

        //memset (palette_map, 0, sizeof (palette_map));

        /* sprites */
        for (offs = 0x1000 - 16; offs >= 0; offs -= 16) {
            if ((K053247_ram.READ_WORD(offs) & 0x8000) != 0) {
                int[] code = new int[1];
                int[] color = new int[1];
                int[] pri = new int[1];

                code[0] = K053247_ram.READ_WORD(offs + 0x02);
                color[0] = K053247_ram.READ_WORD(offs + 0x0c);
                pri[0] = 0;
                K053247_callback.handler(code, color, pri);
                palette_map[color[0]] |= 0xffff;
            }
        }

        /* now build the final table */
        for (i = 0; i < 512; i++) {
            int usage = palette_map[i], j;
            if (usage != 0) {
                for (j = 1; j < 16; j++) {
                    if ((usage & (1 << j)) != 0) {
                        palette_used_colors.write(i * 16 + j, palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
                    }
                }
            }
        }
    }

    public static int K053247_is_IRQ_enabled() {
        return K053247_irq_enabled;
    }
}
