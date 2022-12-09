/*
 * ported to v0.36
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;

public class gyruss {

    public static UBytePtr gyruss_spritebank = new UBytePtr();
    public static UBytePtr gyruss_6809_drawplanet = new UBytePtr();
    public static UBytePtr gyruss_6809_drawship = new UBytePtr();
    static int flipscreen;

    public static class Sprites {

        /*unsigned*/ char y;
        /*unsigned*/ char shape;
        /*unsigned*/ char attr;
        /*unsigned*/ char x;
    }

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Gyruss has one 32x8 palette PROM and two 256x4 lookup table PROMs (one
     * for characters, one for sprites). The palette PROM is connected to the
     * RGB output this way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromHandlerPtr gyruss_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the sprite lookup table */
 /* sprites */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) ((color_prom.readinc()) & 0x0f);
            }

            /* characters */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) + 0x10);
            }
        }
    };

    public static final int YTABLE_START = (0xe000);
    public static final int SINTABLE_START = (0xe400);
    public static final int COSTABLE_START = (0xe600);

    /* convert sprite coordinates from polar to cartesian */
    static int SprTrans(Sprites u) {

        int ro;
        int theta2;
        UBytePtr table;

        ro = memory_region(REGION_CPU4).read(YTABLE_START + u.y);
        theta2 = 2 * u.x;

        /* cosine table */
        table = new UBytePtr(memory_region(REGION_CPU4), COSTABLE_START);

        u.y = (char) ((table.read(theta2 + 1) * ro) >>> 8);
        if (u.y >= 0x80) {
            u.x = 0;
            return 0;
        }
        if (table.read(theta2) != 0) /* negative */ {
            if (u.y >= 0x78) /* avoid wraparound from top to bottom of screen */ {
                u.x = 0;
                return 0;
            }
            u.y = (char) -u.y;
        }

        /* sine table */
        table = new UBytePtr(memory_region(REGION_CPU4), SINTABLE_START);

        u.x = (char) ((table.read(theta2 + 1) * ro) >>> 8);
        if (u.x >= 0x80) {
            u.x = 0;
            return 0;
        }
        if (table.read(theta2) != 0) /* negative */ {
            u.x = (char) -u.x;
        }

        /* convert from logical coordinates to screen coordinates */
        if ((u.attr & 0x10) != 0) {
            u.y += 0x78;
        } else {
            u.y += 0x7C;
        }

        u.x += 0x78;

        return 1;
        /* queue this sprite */
    }

    /* Gyruss uses a semaphore system to queue sprites, and when critic
		   region is released, the 6809 processor writes queued sprites onto
		   screen visible area.
		   When a701 = 0 and a702 = 1 gyruss hardware queue sprites.
		   When a701 = 1 and a702 = 0 gyruss hardware draw sprites.
	
	           both Z80 e 6809 are interrupted at the same time by the
	           VBLANK interrupt.  If there is some work to do (example
	           A7FF is not 0 or FF), 6809 waits for Z80 to store a 1 in
	           A701 and draw currently queued sprites
     */
    public static Sprites SPR(int n, UBytePtr sr) {
        Sprites spr = new Sprites();
        spr.y = sr.read(4 * n);
        spr.shape = sr.read(4 * n + 1);
        spr.attr = sr.read(4 * n + 2);
        spr.x = sr.read(4 * n + 3);
        return spr;
    }

    public static void write(Sprites spr, int n, UBytePtr sr) {
        sr.write(4 * n, spr.y);
        sr.write(4 * n + 1, spr.shape);
        sr.write(4 * n + 2, spr.attr);
        sr.write(4 * n + 3, spr.x);
    }
    public static WriteHandlerPtr gyruss_queuereg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == 1) {
                int n;
                UBytePtr sr;

                /* Gyruss hardware stores alternatively sprites at position
	           0xa000 and 0xa200.  0xA700 tells which one is used.
                 */
                if (gyruss_spritebank.read() == 0) {
                    sr = spriteram;
                } else {
                    sr = spriteram_2;
                }

                /* #0-#3 - ship */
 /* #4-#23 */
                if (gyruss_6809_drawplanet.read() != 0) /* planet is on screen */ {
                    Sprites s = SPR(4, sr);
                    SprTrans(s);
                    write(s, 4, sr);
                    /* #4 - polar coordinates - ship */
                    Sprites s1 = SPR(5, sr);
                    s1.x = 0;
                    write(s1, 5, sr);
                    /* #5 - unused */

 /* #6-#23 - planet */
                } else {
                    for (n = 4; n < 24; n += 2) /* 10 double height sprites in polar coordinates - enemies flying */ {
                        Sprites s = SPR(n, sr);
                        SprTrans(s);
                        write(s, n, sr);
                        Sprites s1 = SPR(n + 1, sr);
                        s1.x = 0;
                        write(s1, n + 1, sr);
                    }
                }

                /* #24-#59 */
                for (n = 24; n < 60; n++) /* 36 sprites in polar coordinates - enemies at center of screen */ {
                    Sprites s = SPR(n, sr);
                    SprTrans(s);
                    write(s, n, sr);
                }

                /* #60-#63 - unused */
 /* #64-#77 */
                if (gyruss_6809_drawship.read() == 0) {
                    for (n = 64; n < 78; n++) /* 14 sprites in polar coordinates - bullets */ {
                        Sprites s = SPR(n, sr);
                        SprTrans(s);
                        write(s, n, sr);
                    }
                }
                /* else 14 sprites - player ship being formed */

 /* #78-#93 - stars */
                for (n = 78; n < 86; n++) {
                    if (SprTrans(SPR(n, sr)) != 0) {
                        /* make a mirror copy */
                        Sprites s1 = SPR(n + 8, sr);
                        s1.x = (char) (SPR(n, sr).y - 4);
                        s1.y = (char) (SPR(n, sr).x + 4);
                        write(s1, n + 8, sr);
                    } else {
                        Sprites s1 = SPR(n + 8, sr);
                        s1.x = 0;
                        write(s1, n + 8, sr);
                    }
                }
            }
        }
    };

    public static WriteHandlerPtr gyruss_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    /* Return the current video scan line */
    public static ReadHandlerPtr gyruss_scanline_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_scalebyfcount(256);
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
    public static VhUpdateHandlerPtr gyruss_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, flipx, flipy;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    flipx = colorram.read(offs) & 0x40;
                    flipy = colorram.read(offs) & 0x80;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                        flipy = NOT(flipy);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            videoram.read(offs) + 8 * (colorram.read(offs) & 0x20),
                            colorram.read(offs) & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the character mapped graphics */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            /*
		   offs+0 :  Ypos
		   offs+1 :  Sprite number
		   offs+2 :  Attribute in the form HF-VF-BK-DH-p3-p2-p1-p0
					 where  HF is horizontal flip
							VF is vertical flip
							BK is for bank select
							DH is for double height (if set sprite is 16*16, else is 16*8)
							px is palette weight
		   offs+3 :  Xpos
             */
 /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            {
                UBytePtr sr;

                if (gyruss_spritebank.read() == 0) {
                    sr = spriteram;
                } else {
                    sr = spriteram_2;
                }

                for (offs = spriteram_size[0] - 8; offs >= 0; offs -= 8) {
                    if ((sr.read(2 + offs) & 0x10) != 0) /* double height */ {
                        if (sr.read(offs + 0) != 0) {
                            drawgfx(bitmap, Machine.gfx[3],
                                    sr.read(offs + 1) / 2 + 4 * (sr.read(offs + 2) & 0x20),
                                    sr.read(offs + 2) & 0x0f,
                                    NOT(sr.read(offs + 2) & 0x40), sr.read(offs + 2) & 0x80,
                                    sr.read(offs + 0), 240 - sr.read(offs + 3) + 1,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    } else /* single height */ {
                        if (sr.read(offs + 0) != 0) {
                            drawgfx(bitmap, Machine.gfx[1 + (sr.read(offs + 1) & 1)],
                                    sr.read(offs + 1) / 2 + 4 * (sr.read(offs + 2) & 0x20),
                                    sr.read(offs + 2) & 0x0f,
                                    NOT(sr.read(offs + 2) & 0x40), sr.read(offs + 2) & 0x80,
                                    sr.read(offs + 0), 240 - sr.read(offs + 3) + 1,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }

                        if (sr.read(offs + 4) != 0) {
                            drawgfx(bitmap, Machine.gfx[1 + (sr.read(offs + 5) & 1)],
                                    sr.read(offs + 5) / 2 + 4 * (sr.read(offs + 6) & 0x20),
                                    sr.read(offs + 6) & 0x0f,
                                    NOT(sr.read(offs + 6) & 0x40), sr.read(offs + 6) & 0x80,
                                    sr.read(offs + 4), 240 - sr.read(offs + 7) + 1,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    }
                }
            }

            /* redraw the characters which have priority over sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy, flipx, flipy;

                sx = offs % 32;
                sy = offs / 32;
                flipx = colorram.read(offs) & 0x40;
                flipy = colorram.read(offs) & 0x80;
                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                    flipx = NOT(flipx);
                    flipy = NOT(flipy);
                }

                if ((colorram.read(offs) & 0x10) != 0) {
                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + 8 * (colorram.read(offs) & 0x20),
                            colorram.read(offs) & 0x0f,
                            flipx, flipy,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }
        }
    };
}
