/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;

public class appoooh {

    public static final int CHR1_OFST = 0x00; /* palette page of char set #1 */

    public static final int CHR2_OFST = 0x10;  /* palette page of char set #2 */

    public static UBytePtr appoooh_videoram2 = new UBytePtr();
    public static UBytePtr appoooh_colorram2 = new UBytePtr();
    public static UBytePtr appoooh_spriteram2 = new UBytePtr();
    static char[] dirtybuffer2;
    static osd_bitmap tmpbitmap2;

    static int scroll_x;
    static int flipscreen;
    static int priority;

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Palette information of appoooh is not known.
     *
     * The palette decoder of Bank Panic was used for this driver. Because these
     * hardware is similar.
     *
     **************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr appoooh_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
            //#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++]=(char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }

            /* color_prom now points to the beginning of the lookup table */
            /* charset #1 lookup table */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) | CHR1_OFST);
            }

            /* charset #2 lookup table */
            for (i = 0; i < TOTAL_COLORS(1); i++) {
                colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (((color_prom.readinc()) & 0x0f) | CHR2_OFST);
            }

            /* TODO: the driver currently uses only 16 of the 32 color codes. */
            /* 16-31 might be unused, but there might be a palette bank selector */
            /* to use them somewhere in the game. */
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr appoooh_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[videoram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, videoram_size[0]);

            if ((tmpbitmap2 = osd_create_bitmap(Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    public static WriteHandlerPtr appoooh_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_x = data;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr appoooh_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer2 = null;
            osd_free_bitmap(tmpbitmap2);
            generic_vh_stop.handler();

        }
    };

    public static WriteHandlerPtr appoooh_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (appoooh_videoram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                appoooh_videoram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr appoooh_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (appoooh_colorram2.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                appoooh_colorram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr appoooh_out_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 controls NMI */
            if ((data & 0x01) != 0) {
                interrupt_enable_w.handler(0, 1);
            } else {
                interrupt_enable_w.handler(0, 0);
            }

            /* bit 1 flip screen */
            if ((data & 0x02) != flipscreen) {
                flipscreen = data & 0x02;
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, videoram_size[0]);
            }

            /* bits 2-3 unknown */
            /* bits 4-5 are playfield/sprite priority */
            /* TODO: understand how this works, currently the only thing I do is draw */
            /* the front layer behind sprites when priority == 0, and invert the sprite */
            /* order when priority == 1 */
            priority = (data & 0x30) >> 4;

            /* bit 6 ROM bank select */
            {
                UBytePtr RAM = memory_region(REGION_CPU1);

                cpu_setbank(1, new UBytePtr(RAM, (data & 0x40) != 0 ? 0x10000 : 0x0a000));
            }

            /* bit 7 unknown (used) */
        }
    };

    public static void appoooh_draw_sprites(osd_bitmap dest_bmp,
            GfxElement gfx,
            UBytePtr sprite) {
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            int sy = 256 - 16 - sprite.read(offs + 0);
            int code = (sprite.read(offs + 1) >> 2) + ((sprite.read(offs + 2) >> 5) & 0x07) * 0x40;
            int color = sprite.read(offs + 2) & 0x0f;	/* TODO: bit 4 toggles continuously, what is it? */

            int sx = sprite.read(offs + 3);
            int flipx = sprite.read(offs + 1) & 0x01;

            if (sx >= 248) {
                sx -= 256;
            }

            if (flipscreen != 0) {
                sx = 239 - sx;
                sy = 255 - sy;
                flipx = NOT(flipx);
            }
            drawgfx(dest_bmp, gfx,
                    code,
                    color,
                    flipx, flipscreen,
                    sx, sy,
                    Machine.drv.visible_area,
                    TRANSPARENCY_PEN, 0);
        }
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr appoooh_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int scroll;

            /* for every character in the Video RAM, check if it has been modified */
            /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                /* char set #1 */
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, code, flipx;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    code = videoram.read(offs) + 256 * ((colorram.read(offs) >> 5) & 7);

                    flipx = colorram.read(offs) & 0x10;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                    }
                    drawgfx(tmpbitmap, Machine.gfx[0],
                            code,
                            colorram.read(offs) & 0x0f,
                            flipx, flipscreen,
                            8 * sx, 8 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
                /* char set #2 */
                if (dirtybuffer2[offs] != 0) {
                    int sx, sy, code, flipx;

                    dirtybuffer2[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    code = appoooh_videoram2.read(offs) + 256 * ((appoooh_colorram2.read(offs) >> 5) & 0x07);

                    flipx = appoooh_colorram2.read(offs) & 0x10;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        flipx = NOT(flipx);
                    }
                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            code,
                            appoooh_colorram2.read(offs) & 0x0f,
                            flipx, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            scroll = -scroll_x;
            scroll = 0;

            /* copy the temporary bitmaps to the screen */
            copybitmap(bitmap, tmpbitmap2, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            if (priority == 0) /* fg behind sprites */ {
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll}, 0, null, Machine.drv.visible_area, TRANSPARENCY_COLOR, CHR1_OFST);
            }

            /* draw sprites */
            if (priority == 1) {
                /* sprite set #1 */
                appoooh_draw_sprites(bitmap, Machine.gfx[2], spriteram);
                /* sprite set #2 */
                appoooh_draw_sprites(bitmap, Machine.gfx[3], appoooh_spriteram2);
            } else {
                /* sprite set #2 */
                appoooh_draw_sprites(bitmap, Machine.gfx[3], appoooh_spriteram2);
                /* sprite set #1 */
                appoooh_draw_sprites(bitmap, Machine.gfx[2], spriteram);
            }

            if (priority != 0) /* fg in front of sprites */ {
                copyscrollbitmap(bitmap, tmpbitmap, 1, new int[]{scroll}, 0, null, Machine.drv.visible_area, TRANSPARENCY_COLOR, CHR1_OFST);
            }
        }
    };
}
