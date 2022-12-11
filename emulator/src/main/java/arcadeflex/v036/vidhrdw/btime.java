/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
import static common.libc.expressions.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;

public class btime {

    public static UBytePtr lnc_charbank = new UBytePtr();
    public static UBytePtr bnj_backgroundram = new UBytePtr();
    public static UBytePtr zoar_scrollram = new UBytePtr();
    public static UBytePtr deco_charram = new UBytePtr();
    public static int[] bnj_backgroundram_size = new int[1];

    static int[] sprite_dirty = new int[256];
    static int[] char_dirty = new int[1024];

    static int flipscreen = 0;
    static int btime_palette = 0;
    static /*unsigned*/ char bnj_scroll1 = 0;
    static /*unsigned*/ char bnj_scroll2 = 0;
    static /*unsigned*/ char[] dirtybuffer2 = null;
    static osd_bitmap background_bitmap;
    static int lnc_sound_interrupt_enabled = 0;

    /**
     * *************************************************************************
     *
     * Burger Time doesn't have a color PROM. It uses RAM to dynamically create
     * the palette. The palette RAM is connected to the RGB output this way:
     *
     * bit 7 -- 15 kohm resistor -- BLUE (inverted) -- 33 kohm resistor -- BLUE
     * (inverted) -- 15 kohm resistor -- GREEN (inverted) -- 33 kohm resistor --
     * GREEN (inverted) -- 47 kohm resistor -- GREEN (inverted) -- 15 kohm
     * resistor -- RED (inverted) -- 33 kohm resistor -- RED (inverted) bit 0 --
     * 47 kohm resistor -- RED (inverted)
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr btime_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            /* Burger Time doesn't have a color PROM, but Hamburge has. */
 /* This function is also used by Eggs. */
            if (color_prom == null) {
                return;
            }
            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * The PROM is connected to the RGB output this way:
     *
     * bit 7 -- 47 kohm resistor -- RED -- 33 kohm resistor -- RED -- 15 kohm
     * resistor -- RED -- 47 kohm resistor -- GREEN -- 33 kohm resistor -- GREEN
     * -- 15 kohm resistor -- GREEN -- 33 kohm resistor -- BLUE bit 0 -- 15 kohm
     * resistor -- BLUE
     *
     **************************************************************************
     */
    public static VhConvertColorPromHandlerPtr lnc_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            int p_inc = 0;
            for (i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 7) & 0x01;
                bit1 = (color_prom.read() >> 6) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* green component */
                bit0 = (color_prom.read() >> 4) & 0x01;
                bit1 = (color_prom.read() >> 3) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
                /* blue component */
                bit0 = 0;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 0) & 0x01;
                palette[p_inc++] = (char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);

                color_prom.inc();
            }
        }
    };

    public static InitMachineHandlerPtr lnc_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            lnc_charbank.write(1);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr bnj_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[bnj_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, bnj_backgroundram_size[0]);

            /* the background area is twice as wide as the screen */
            if ((background_bitmap = osd_create_bitmap(2 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            bnj_scroll1 = 0;
            bnj_scroll2 = 0;

            return 0;
        }
    };

    public static VhStartHandlerPtr btime_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            bnj_scroll1 = 0;
            bnj_scroll2 = 0;

            return generic_vh_start.handler();
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopHandlerPtr bnj_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(background_bitmap);
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr btime_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* RGB output is inverted */
            paletteram_BBGGGRRR_w.handler(offset, ~data);
        }
    };

    public static WriteHandlerPtr lnc_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videoram.read(offset) != data || colorram.read(offset) != lnc_charbank.read()) {
                videoram.write(offset, data);
                colorram.write(offset, lnc_charbank.read());

                dirtybuffer[offset] = 1;
            }
        }
    };

    public static ReadHandlerPtr btime_mirrorvideoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int x, y;

            /* swap x and y coordinates */
            x = offset / 32;
            y = offset % 32;
            offset = 32 * y + x;

            return videoram_r.handler(offset);
        }
    };

    public static ReadHandlerPtr btime_mirrorcolorram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int x, y;

            /* swap x and y coordinates */
            x = offset / 32;
            y = offset % 32;
            offset = 32 * y + x;

            return colorram_r.handler(offset);
        }
    };

    public static WriteHandlerPtr btime_mirrorvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int x, y;

            /* swap x and y coordinates */
            x = offset / 32;
            y = offset % 32;
            offset = 32 * y + x;

            videoram_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr lnc_mirrorvideoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int x, y;

            /* swap x and y coordinates */
            x = offset / 32;
            y = offset % 32;
            offset = 32 * y + x;

            lnc_videoram_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr btime_mirrorcolorram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int x, y;

            /* swap x and y coordinates */
            x = offset / 32;
            y = offset % 32;
            offset = 32 * y + x;

            colorram_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr deco_charram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (deco_charram.read(offset) == data) {
                return;
            }

            deco_charram.write(offset, data);

            offset &= 0x1fff;

            /* dirty sprite */
            sprite_dirty[offset >> 5] = 1;

            /* diry char */
            char_dirty[offset >> 3] = 1;
        }
    };

    public static WriteHandlerPtr bnj_background_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (bnj_backgroundram.read(offset) != data) {
                dirtybuffer2[offset] = 1;

                bnj_backgroundram.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr bnj_scroll1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            // Dirty screen if background is being turned off
            if (bnj_scroll1 != 0 && data == 0) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            bnj_scroll1 = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr bnj_scroll2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            bnj_scroll2 = (char) (data & 0xFF);
        }
    };
    static int video_control_zoar = -1;
    public static WriteHandlerPtr zoar_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            // Zoar video control
            //
            // Bit 0-2 = Unknown (always 0). Marked as MCOL on schematics
            // Bit 3-4 = Palette
            // Bit 7   = Flip Screen

            if (video_control_zoar != data) {
                memset(dirtybuffer, 1, videoram_size[0]);

                video_control_zoar = data;

                flipscreen = data & 0x80;
                btime_palette = (data & 0x30) >> 3;
            }
        }
    };
    static int video_control_btime = -1;
    public static WriteHandlerPtr btime_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            // Btime video control
            //
            // Bit 0   = Flip screen
            // Bit 1-7 = Unknown

            if (video_control_btime != data) {
                memset(dirtybuffer, 1, videoram_size[0]);

                // This is used by Bump'n'Jump
                if (dirtybuffer2 != null) {
                    memset(dirtybuffer2, 1, bnj_backgroundram_size[0]);
                }

                video_control_btime = data;

                flipscreen = data & 0x01;
            }
        }
    };

    public static WriteHandlerPtr bnj_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Bnj/Lnc works a little differently than the btime/eggs (apparently). */
 /* According to the information at: */
 /* http://www.davesclassics.com/arcade/Switch_Settings/BumpNJump.sw */
 /* SW8 is used for cocktail video selection (as opposed to controls), */
 /* but bit 7 of the input port is used for vblank input. */
 /* My guess is that this switch open circuits some connection to */
 /* the monitor hardware. */
 /* For now we just check 0x40 in DSW1, and ignore the write if we */
 /* are in upright controls mode. */

            if ((input_port_3_r.handler(0) & 0x40) != 0) /* cocktail mode */ {
                btime_video_control_w.handler(offset, data);
            }
        }
    };

    public static WriteHandlerPtr lnc_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            // I have a feeling that this only works by coincidence. I couldn't
            // figure out how NMI's are disabled by the sound processor
            lnc_sound_interrupt_enabled = data & 0x08;

            bnj_video_control_w.handler(offset, data & 0x01);
        }
    };
    static int video_control_disco = -1;
    public static WriteHandlerPtr disco_video_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (video_control_disco != data) {
                memset(dirtybuffer, 1, videoram_size[0]);

                video_control_disco = data;

                btime_palette = (data >> 2) & 0x03;

                if ((input_port_3_r.handler(0) & 0x40) == 0) /* cocktail mode */ {
                    flipscreen = data & 0x01;
                }
            }
        }
    };

    public static InterruptHandlerPtr lnc_sound_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if (lnc_sound_interrupt_enabled != 0) {
                return nmi_interrupt.handler();
            } else {
                return ignore_interrupt.handler();
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
    static void drawchars(osd_bitmap bitmap, int transparency, int color, int priority) {
        int offs;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. If the background is on, */
 /* draw characters as sprites */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            int sx, sy, code;

            if (dirtybuffer[offs] == 0 && (bitmap == tmpbitmap)) {
                continue;
            }

            dirtybuffer[offs] = 0;

            code = videoram.read(offs) + 256 * (colorram.read(offs) & 3);

            /* check priority */
            if ((priority != -1) && (priority != ((code >> 7) & 0x01))) {
                continue;
            }

            sx = 31 - (offs / 32);
            sy = offs % 32;

            if (flipscreen != 0) {
                sx = 31 - sx;
                sy = 31 - sy;
            }

            drawgfx(bitmap, Machine.gfx[0],
                    code,
                    color,
                    flipscreen, flipscreen,
                    8 * sx, 8 * sy,
                    Machine.drv.visible_area, transparency, 0);
        }
    }

    static void drawsprites(osd_bitmap bitmap, int color,
            int sprite_y_adjust, int sprite_y_adjust_flipscreen,
            UBytePtr sprite_ram, int interleave) {
        int i, offs;

        /* Draw the sprites */
        for (i = 0, offs = 0; i < 8; i++, offs += 4 * interleave) {
            int sx, sy, flipx, flipy;

            if ((sprite_ram.read(offs + 0) & 0x01) == 0) {
                continue;
            }

            sx = 240 - sprite_ram.read(offs + 3 * interleave);
            sy = 240 - sprite_ram.read(offs + 2 * interleave);

            flipx = sprite_ram.read(offs + 0) & 0x04;
            flipy = sprite_ram.read(offs + 0) & 0x02;

            if (flipscreen != 0) {
                sx = 240 - sx;
                sy = 240 - sy + sprite_y_adjust_flipscreen;

                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            sy -= sprite_y_adjust;

            drawgfx(bitmap, Machine.gfx[1],
                    sprite_ram.read(offs + interleave),
                    color,
                    flipx, flipy,
                    sx, sy,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

            sy += (flipscreen != 0 ? -256 : 256);

            // Wrap around
            drawgfx(bitmap, Machine.gfx[1],
                    sprite_ram.read(offs + interleave),
                    color,
                    flipx, flipy,
                    sx, sy,
                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
        }
    }

    static void drawbackground(osd_bitmap bitmap, UBytePtr tilemap) {
        int i, offs;

        int scroll = -(bnj_scroll2 | ((bnj_scroll1 & 0x03) << 8));

        // One extra iteration for wrap around
        for (i = 0; i < 5; i++, scroll += 256) {
            int tileoffset = tilemap.read(i & 3) * 0x100;

            // Skip if this title is completely off the screen
            if (scroll > 256) {
                break;
            }
            if (scroll < -256) {
                continue;
            }

            for (offs = 0; offs < 0x100; offs++) {
                int sx, sy;

                sx = 240 - (16 * (offs / 16) + scroll);
                sy = 16 * (offs % 16);

                if (flipscreen != 0) {
                    sx = 240 - sx;
                    sy = 240 - sy;
                }

                drawgfx(bitmap, Machine.gfx[2],
                        memory_region(REGION_GFX3).read(tileoffset + offs),
                        btime_palette,
                        flipscreen, flipscreen,
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);
            }
        }
    }

    static void decode_modified(UBytePtr sprite_ram, int interleave) {
        int i, offs;

        /* decode dirty characters */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            int code;

            code = videoram.read(offs) + 256 * (colorram.read(offs) & 3);

            switch (char_dirty[code]) {
                case 1:
                    decodechar(Machine.gfx[0], code, new UBytePtr(deco_charram.memory, deco_charram.offset), Machine.drv.gfxdecodeinfo[0].gfxlayout);
                    char_dirty[code] = 2;
                /* fall through */
                case 2:
                    dirtybuffer[offs] = 1;
                    break;
                default:
                    break;
            }
        }

        for (i = 0; i < /*sizeof(char_dirty);*/ 1024; i++) {
            if (char_dirty[i] == 2) {
                char_dirty[i] = 0;
            }
        }

        /* decode dirty sprites */
        for (i = 0, offs = 0; i < 8; i++, offs += 4 * interleave) {
            int code;

            code = sprite_ram.read(offs + interleave);
            if (sprite_dirty[code] != 0) {
                sprite_dirty[code] = 0;

                decodechar(Machine.gfx[1], code, new UBytePtr(deco_charram.memory, deco_charram.offset), Machine.drv.gfxdecodeinfo[1].gfxlayout);
            }
        }
    }

    static /*unsigned*/ char[] btime_tilemap = new char[4];
    public static VhUpdateHandlerPtr btime_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            if ((bnj_scroll1 & 0x10) != 0) {
                int i, start;

                // Generate tile map
                if (flipscreen != 0) {
                    start = 0;
                } else {
                    start = 1;
                }

                for (i = 0; i < 4; i++) {
                    btime_tilemap[i] = (char) (start | (bnj_scroll1 & 0x04));
                    start = (++start & 0x03);
                }

                drawbackground(bitmap, new UBytePtr(btime_tilemap));

                drawchars(bitmap, TRANSPARENCY_PEN, 0, -1);
            } else {
                drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

                /* copy the temporary bitmap to the screen */
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            drawsprites(bitmap, 0, 1, 0, videoram, 0x20);
        }
    };

    public static VhUpdateHandlerPtr eggs_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
        }
    };

    public static VhUpdateHandlerPtr lnc_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            drawsprites(bitmap, 0, 1, 2, videoram, 0x20);
        }
    };

    public static VhUpdateHandlerPtr zoar_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            if ((bnj_scroll1 & 0x04) != 0) {
                drawbackground(bitmap, zoar_scrollram);

                drawchars(bitmap, TRANSPARENCY_PEN, btime_palette + 1, -1);
            } else {
                drawchars(tmpbitmap, TRANSPARENCY_NONE, btime_palette + 1, -1);

                /* copy the temporary bitmap to the screen */
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* The order is important for correct priorities */
            drawsprites(bitmap, btime_palette + 1, 1, 2, new UBytePtr(videoram, 0x1f), 0x20);
            drawsprites(bitmap, btime_palette + 1, 1, 2, videoram, 0x20);
        }
    };

    public static VhUpdateHandlerPtr bnj_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer2, 1, bnj_backgroundram_size[0]);
            }

            /*
             *  For each character in the background RAM, check if it has been
             *  modified since last time and update it accordingly.
             */
            if (bnj_scroll1 != 0) {
                int scroll, offs;

                for (offs = bnj_backgroundram_size[0] - 1; offs >= 0; offs--) {
                    int sx, sy;

                    if (dirtybuffer2[offs] == 0) {
                        continue;
                    }

                    dirtybuffer2[offs] = 0;

                    sx = 16 * ((offs < 0x100) ? ((offs % 0x80) / 8) : ((offs % 0x80) / 8) + 16);
                    sy = 16 * (((offs % 0x100) < 0x80) ? offs % 8 : (offs % 8) + 8);
                    sx = 496 - sx;

                    if (flipscreen != 0) {
                        sx = 496 - sx;
                        sy = 240 - sy;
                    }

                    drawgfx(background_bitmap, Machine.gfx[2],
                            (bnj_backgroundram.read(offs) >> 4) + ((offs & 0x80) >> 3) + 32,
                            0,
                            flipscreen, flipscreen,
                            sx, sy,
                            null, TRANSPARENCY_NONE, 0);
                }

                /* copy the background bitmap to the screen */
                scroll = (bnj_scroll1 & 0x02) * 128 + 511 - bnj_scroll2;
                if (flipscreen == 0) {
                    scroll = 767 - scroll;
                }
                copyscrollbitmap(bitmap, background_bitmap, 1, new int[]{scroll}, 0, null, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

                /* copy the low priority characters followed by the sprites
                 then the high priority characters */
                drawchars(bitmap, TRANSPARENCY_PEN, 0, 1);
                drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
                drawchars(bitmap, TRANSPARENCY_PEN, 0, 0);
            } else {
                drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

                /* copy the temporary bitmap to the screen */
                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

                drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
            }
        }
    };

    public static VhUpdateHandlerPtr cookrace_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /*
             *  For each character in the background RAM, check if it has been
             *  modified since last time and update it accordingly.
             */
            for (offs = bnj_backgroundram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = 31 - (offs / 32);
                sy = offs % 32;

                if (flipscreen != 0) {
                    sx = 31 - sx;
                    sy = 31 - sy;
                }

                drawgfx(bitmap, Machine.gfx[2],
                        bnj_backgroundram.read(offs),
                        0,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        null, TRANSPARENCY_NONE, 0);
            }

            drawchars(bitmap, TRANSPARENCY_PEN, 0, -1);

            drawsprites(bitmap, 0, 1, 0, videoram, 0x20);
        }
    };

    public static VhUpdateHandlerPtr disco_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            decode_modified(spriteram, 1);

            drawchars(tmpbitmap, TRANSPARENCY_NONE, btime_palette, -1);

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            drawsprites(bitmap, btime_palette, 0, 0, spriteram, 1);
        }
    };

    public static VhUpdateHandlerPtr decocass_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            decode_modified(videoram, 0x20);

            drawchars(tmpbitmap, TRANSPARENCY_NONE, 0, -1);

            /* copy the temporary bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);

            drawsprites(bitmap, 0, 0, 0, videoram, 0x20);
        }
    };
}
