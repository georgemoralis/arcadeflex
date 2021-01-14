 /*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.vidhrdw;

import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import gr.codebb.arcadeflex.v036.mame.osdependH.osd_bitmap;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;

public class segar {

    public static UBytePtr segar_characterram = new UBytePtr();
    public static UBytePtr segar_characterram2 = new UBytePtr();
    public static UBytePtr segar_mem_colortable = new UBytePtr();
    public static UBytePtr segar_mem_bcolortable = new UBytePtr();

    public static class SEGAR_VID_STRUCT {

        /*unsigned*/ char[] dirtychar = new char[256];		// graphics defined in RAM, mark when changed

        /*unsigned*/ char[] colorRAM = new char[0x40];		// stored in a 93419 (vid board)
        /*unsigned*/ char[] bcolorRAM = new char[0x40];		// stored in a 93419 (background board)
        /*unsigned*/ char color_write_enable;	// write-enable the 93419 (vid board)
        /*unsigned*/ char flip;					// cocktail flip mode (vid board)
        /*unsigned*/ char bflip;				// cocktail flip mode (background board)

        /*unsigned*/ char refresh;				// refresh the screen
        /*unsigned*/ char brefresh;				// refresh the background
        /*unsigned*/ char char_refresh;			// refresh the character graphics

        /*unsigned*/ char has_bcolorRAM;		// do we have background color RAM?
        /*unsigned*/ char background_enable;	// draw the background?
        /*unsigned*/ int back_scene;
        /*unsigned*/ int back_charset;


        // used for Pig Newton
        /*unsigned*/ int bcolor_offset;


        // used for Space Odyssey
        /*unsigned*/ char backfill;
        /*unsigned*/ char fill_background;
        /*unsigned*/ int backshift;
        osd_bitmap horizbackbitmap;
        osd_bitmap vertbackbitmap;
    }

    static SEGAR_VID_STRUCT sv = new SEGAR_VID_STRUCT();

    /**
     * *************************************************************************
     *
     * The Sega raster games don't have a color PROM. Instead, it has a color
     * RAM that can be filled with bytes of the form BBGGGRRR. We'll still build
     * up an initial palette, and set our colortable to point to a different
     * color for each entry in the colortable, which we'll adjust later using
     * palette_change_color.
     *
     **************************************************************************
     */
    static /*unsigned*/ char color_scale[] = {0x00, 0x40, 0x80, 0xC0};
    public static VhConvertColorPromPtr segar_init_colors = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int p_ptr = 0;
            int i;

            /* Our first color needs to be black (transparent) */
            palette[p_ptr++] = 0;
            palette[p_ptr++] = 0;
            palette[p_ptr++] = 0;

            /* Space Odyssey uses a static palette for the background, so
		   our choice of colors isn't exactly arbitrary.  S.O. uses a
		   6-bit color setup, so we make sure that every 0x40 colors
		   gets a nice 6-bit palette.
	
	       (All of the other G80 games overwrite the default colors on startup)
             */
            for (i = 0; i < (Machine.drv.total_colors - 1); i++) {
                palette[p_ptr++] = color_scale[((i & 0x30) >> 4)];
                palette[p_ptr++] = color_scale[((i & 0x0C) >> 2)];
                palette[p_ptr++] = color_scale[((i & 0x03) << 0)];
            }

            for (i = 0; i < Machine.drv.total_colors; i++) {
                colortable[i] = (char) i;
            }

        }
    };

    /**
     * *************************************************************************
     * The two bit planes are separated in memory. If either bit plane changes,
     * mark the character as modified.
     * *************************************************************************
     */
    public static WriteHandlerPtr segar_characterram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.dirtychar[offset / 8] = 1;

            segar_characterram.write(offset, data);
        }
    };

    public static WriteHandlerPtr segar_characterram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.dirtychar[offset / 8] = 1;

            segar_characterram2.write(offset, data);
        }
    };

    /**
     * *************************************************************************
     * The video port is not entirely understood. D0 = FLIP D1 = Color Write
     * Enable (vid board) D2 = ??? (we seem to need a char_refresh when this is
     * set) D3 = ??? (looks to be unused on the schems) D4-D7 = unused?
     * *************************************************************************
     */
    public static WriteHandlerPtr segar_video_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            logerror("VPort = %02X\n", data);

            if ((data & 0x01) != sv.flip) {
                sv.flip = (char) (data & 0x01);
                sv.refresh = 1;
            }

            if ((data & 0x02) != 0) {
                sv.color_write_enable = 1;
            } else {
                sv.color_write_enable = 0;
            }

            if ((data & 0x04) != 0) {
                sv.char_refresh = 1;
            }
        }
    };

    /**
     * *************************************************************************
     * If a color changes, refresh the entire screen because it's possible that
     * the color change affected the transparency (switched either to or from
     * black)
     * *************************************************************************
     */
    static /*unsigned*/ char red[] = {0x00, 0x24, 0x49, 0x6D, 0x92, 0xB6, 0xDB, 0xFF};
    static /*unsigned*/ char grn[] = {0x00, 0x24, 0x49, 0x6D, 0x92, 0xB6, 0xDB, 0xFF};
    static /*unsigned*/ char blu[] = {0x00, 0x55, 0xAA, 0xFF};
    public static WriteHandlerPtr segar_colortable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (sv.color_write_enable != 0) {
                int r, g, b;

                b = blu[(data & 0xC0) >> 6];
                g = grn[(data & 0x38) >> 3];
                r = red[(data & 0x07)];

                palette_change_color(offset + 1, r, g, b);

                if (data == 0) {
                    Machine.gfx[0].colortable.write(offset, Machine.pens[0]);
                } else {
                    Machine.gfx[0].colortable.write(offset, Machine.pens[offset + 1]);
                }

                // refresh the screen if the color switched to or from black
                if (sv.colorRAM[offset] != data) {
                    if ((sv.colorRAM[offset] == 0) || (data == 0)) {
                        sv.refresh = 1;
                    }
                }

                sv.colorRAM[offset] = (char) (data & 0xFF);
            } else {
                logerror("color %02X:%02X (write=%d)\n", offset, data, sv.color_write_enable);
                segar_mem_colortable.write(offset, data);
            }
        }
    };
    static /*unsigned*/ char _red[] = {0x00, 0x24, 0x49, 0x6D, 0x92, 0xB6, 0xDB, 0xFF};
    static /*unsigned*/ char _grn[] = {0x00, 0x24, 0x49, 0x6D, 0x92, 0xB6, 0xDB, 0xFF};
    static /*unsigned*/ char _blu[] = {0x00, 0x55, 0xAA, 0xFF};
    public static WriteHandlerPtr segar_bcolortable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            int r, g, b;

            if (sv.has_bcolorRAM != 0) {
                sv.bcolorRAM[offset] = (char) (data & 0xFF);

                b = _blu[(data & 0xC0) >> 6];
                g = _grn[(data & 0x38) >> 3];
                r = _red[(data & 0x07)];

                palette_change_color(offset + 0x40 + 1, r, g, b);
            }

            // Needed to pass the self-tests
            segar_mem_bcolortable.write(offset, data);
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
    public static VhStartPtr segar_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            // Init our vid struct, everything defaults to 0
            //memset( & sv, 0, sizeof(SEGAR_VID_STRUCT));
            return 0;
        }
    };

    /**
     * *************************************************************************
     * This is the refresh code that is common across all the G80 games. This
     * corresponds to the VIDEO I board.
     * *************************************************************************
     */
    static void segar_common_screenrefresh(osd_bitmap bitmap, int sprite_transparency, int copy_transparency) {
        int offs;
        int charcode;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
            if ((sv.char_refresh != 0) && (sv.dirtychar[videoram.read(offs)]) != 0) {
                dirtybuffer[offs] = 1;
            }

            /* Redraw every character if our palette or scene changed */
            if ((dirtybuffer[offs] != 0) || sv.refresh != 0) {
                int sx, sy;

                sx = 8 * (offs % 32);
                sy = 8 * (offs / 32);

                if (sv.flip != 0) {
                    sx = 31 * 8 - sx;
                    sy = 27 * 8 - sy;
                }

                charcode = videoram.read(offs);

                /* decode modified characters */
                if (sv.dirtychar[charcode] == 1) {
                    decodechar(Machine.gfx[0], charcode, segar_characterram,
                            Machine.drv.gfxdecodeinfo[0].gfxlayout);
                    sv.dirtychar[charcode] = 2;
                }

                drawgfx(tmpbitmap, Machine.gfx[0],
                        charcode, charcode >> 4,
                        sv.flip, sv.flip, sx, sy,
                        Machine.visible_area, sprite_transparency, 0);

                dirtybuffer[offs] = 0;

            }
        }

        for (offs = 0; offs < 256; offs++) {
            if (sv.dirtychar[offs] == 2) {
                sv.dirtychar[offs] = 0;
            }
        }

        /* copy the character mapped graphics */
        copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, copy_transparency, Machine.pens[0]);

        sv.char_refresh = 0;
        sv.refresh = 0;
    }

    /**
     * *************************************************************************
     * "Standard" refresh for games without special background boards.
     * *************************************************************************
     */
    public static VhUpdatePtr segar_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null || full_refresh != 0) {
                sv.refresh = 1;
            }

            segar_common_screenrefresh(bitmap, TRANSPARENCY_NONE, TRANSPARENCY_NONE);
        }
    };

    /**
     * *************************************************************************
     * ---------------------------------------------------------------------------
     * Space Odyssey Functions
     * ---------------------------------------------------------------------------
     * *************************************************************************
     */
    /**
     * *************************************************************************
     *
     * Create two background bitmaps for Space Odyssey - one for the horizontal
     * scrolls that's 4 times wider than the screen, and one for the vertical
     * scrolls that's 4 times taller than the screen.
     *
     **************************************************************************
     */
    public static VhStartPtr spaceod_vh_start = new VhStartPtr() {
        public int handler() {
            if (segar_vh_start.handler() != 0) {
                return 1;
            }

            if ((sv.horizbackbitmap = bitmap_alloc(4 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                generic_vh_stop.handler();
                return 1;
            }

            if ((sv.vertbackbitmap = bitmap_alloc(Machine.drv.screen_width, 4 * Machine.drv.screen_height)) == null) {
                bitmap_free(sv.horizbackbitmap);
                generic_vh_stop.handler();
                return 1;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Get rid of the Space Odyssey background bitmaps.
     *
     **************************************************************************
     */
    public static VhStopPtr spaceod_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(sv.horizbackbitmap);
            bitmap_free(sv.vertbackbitmap);
            generic_vh_stop.handler();
        }
    };

    /**
     * *************************************************************************
     * This port controls which background to draw for Space Odyssey.	The
     * temp_scene and temp_charset are analogous to control lines used to select
     * the background. If the background changed, refresh the screen.
     * *************************************************************************
     */
    public static WriteHandlerPtr spaceod_back_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*unsigned*/ int temp_scene, temp_charset;

            temp_scene = (data & 0xC0) >> 6;
            temp_charset = (data & 0x04) >> 2;

            if (temp_scene != sv.back_scene) {
                sv.back_scene = temp_scene;
                sv.brefresh = 1;
            }
            if (temp_charset != sv.back_charset) {
                sv.back_charset = temp_charset;
                sv.brefresh = 1;
            }

            /* Our cocktail flip-the-screen bit. */
            if ((data & 0x01) != sv.bflip) {
                sv.bflip = (char) (data & 0x01);
                sv.brefresh = 1;
            }

            sv.background_enable = 1;
            sv.fill_background = 0;
        }
    };

    /**
     * *************************************************************************
     * This port controls the Space Odyssey background scrolling.	Each write to
     * this port scrolls the background by one bit. Faster speeds are achieved
     * by the program writing more often to this port. Oddly enough, the value
     * sent to this port also seems to indicate the speed, but the value itself
     * is never checked.
     * *************************************************************************
     */
    public static WriteHandlerPtr spaceod_backshift_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.backshift = (sv.backshift + 1) % 0x400;
            sv.background_enable = 1;
            sv.fill_background = 0;
        }
    };

    /**
     * *************************************************************************
     * This port resets the Space Odyssey background to the "top". This is only
     * really important for the Black Hole level, since the only way the program
     * can line up the background's Black Hole with knowing when to spin the
     * ship is to force the background to restart every time you die.
     * *************************************************************************
     */
    public static WriteHandlerPtr spaceod_backshift_clear_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.backshift = 0;
            sv.background_enable = 1;
            sv.fill_background = 0;
        }
    };

    /**
     * *************************************************************************
     * Space Odyssey also lets you fill the background with a specific color.
     * *************************************************************************
     */
    public static WriteHandlerPtr spaceod_backfill_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.backfill = (char) (data + 0x40 + 1);
            sv.fill_background = 1;
        }
    };

    /**
     * *************************************************************************
     **************************************************************************
     */
    public static WriteHandlerPtr spaceod_nobackfill_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sv.backfill = 0;
            sv.fill_background = 0;
        }
    };

    /**
     * *************************************************************************
     * Special refresh for Space Odyssey, this code refreshes the static
     * background.
     * *************************************************************************
     */
    public static VhUpdatePtr spaceod_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int charcode;
            int sprite_transparency;
            int vert_scene;

            UBytePtr back_charmap = memory_region(REGION_USER1);

            if (palette_recalc() != null || full_refresh != 0) {
                sv.refresh = 1;
            }

            // scenes 0,1 are horiz.  scenes 2,3 are vert.
            vert_scene = (sv.back_scene & 0x02)!=0?0:1;

            sprite_transparency = TRANSPARENCY_PEN;

            /* If the background picture changed, draw the new one in temp storage */
            if (sv.brefresh != 0) {
                sv.brefresh = 0;

                for (offs = 0x1000 - 1; offs >= 0; offs--) {
                    int sx, sy;

                    /* Use Vertical Back Scene */
                    if (vert_scene != 0) {
                        sx = 8 * (offs % 32);
                        sy = 8 * (offs / 32);

                        if (sv.bflip != 0) {
                            sx = 31 * 8 - sx;
                            sy = 127 * 8 - sy;
                        }
                    } /* Use Horizontal Back Scene */ else {
                        sx = (8 * (offs % 32)) + (256 * (offs >> 10));
                        sy = 8 * ((offs & 0x3FF) / 32);

                        if (sv.bflip != 0) {
                            sx = 127 * 8 - sx;
                            sy = 31 * 8 - sy;
                            /* is this right? */
                        }
                    }

                    charcode = back_charmap.read((sv.back_scene * 0x1000) + offs);

                    if (vert_scene != 0) {
                        drawgfx(sv.vertbackbitmap, Machine.gfx[1 + sv.back_charset],
                                charcode, 0,
                                sv.bflip, sv.bflip, sx, sy,
                                null, TRANSPARENCY_NONE, 0);
                    } else {
                        drawgfx(sv.horizbackbitmap, Machine.gfx[1 + sv.back_charset],
                                charcode, 0,
                                sv.bflip, sv.bflip, sx, sy,
                                null, TRANSPARENCY_NONE, 0);
                    }
                }
            }

            /* Copy the scrolling background */
            {
                int scrollx, scrolly;

                if (vert_scene != 0) {
                    if (sv.bflip != 0) {
                        scrolly = sv.backshift;
                    } else {
                        scrolly = -sv.backshift;
                    }

                    copyscrollbitmap(bitmap, sv.vertbackbitmap, 0, null, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
                } else {
                    if (sv.bflip != 0) {
                        scrollx = sv.backshift;
                    } else {
                        scrollx = -sv.backshift;
                    }

                    scrolly = -32;

                    copyscrollbitmap(bitmap, sv.horizbackbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }

            if (sv.fill_background == 1) {
                fillbitmap(bitmap, Machine.pens[sv.backfill], Machine.visible_area);
            }

            /* Refresh the "standard" graphics */
            segar_common_screenrefresh(bitmap, TRANSPARENCY_NONE, TRANSPARENCY_PEN);
        }
    };

    /**
     * *************************************************************************
     * ---------------------------------------------------------------------------
     * Monster Bash Functions
     * ---------------------------------------------------------------------------
     * *************************************************************************
     */
    public static VhStartPtr monsterb_vh_start = new VhStartPtr() {
        public int handler() {
            if (segar_vh_start.handler() != 0) {
                return 1;
            }

            sv.has_bcolorRAM = 1;
            return 0;
        }
    };

    /**
     * *************************************************************************
     * This port controls which background to draw for Monster Bash. The
     * tempscene and tempoffset are analogous to control lines used to bank
     * switch the background ROMs. If the background changed, refresh the
     * screen.
     * *************************************************************************
     */
    public static WriteHandlerPtr monsterb_back_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*unsigned*/ int temp_scene, temp_charset;

            temp_scene = 0x400 * ((data & 0x70) >> 4);
            temp_charset = data & 0x03;

            if (sv.back_scene != temp_scene) {
                sv.back_scene = temp_scene;
                sv.refresh = 1;
            }
            if (sv.back_charset != temp_charset) {
                sv.back_charset = temp_charset;
                sv.refresh = 1;
            }

            /* This bit turns the background off and on. */
            if ((data & 0x80) != 0 && (sv.background_enable == 0)) {
                sv.background_enable = 1;
                sv.refresh = 1;
            } else if (((data & 0x80) == 0) && (sv.background_enable == 1)) {
                sv.background_enable = 0;
                sv.refresh = 1;
            }
        }
    };

    /**
     * *************************************************************************
     * Special refresh for Monster Bash, this code refreshes the static
     * background.
     * *************************************************************************
     */
    public static VhUpdatePtr monsterb_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int charcode;
            int sprite_transparency;

            UBytePtr back_charmap = memory_region(REGION_USER1);

            if (palette_recalc() != null || full_refresh != 0) {
                sv.refresh = 1;
            }

            sprite_transparency = TRANSPARENCY_NONE;

            /* If the background is turned on, refresh it first. */
            if (sv.background_enable != 0) {
                /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    if ((sv.char_refresh) != 0 && (sv.dirtychar[videoram.read(offs)]) != 0) {
                        dirtybuffer[offs] = 1;
                    }

                    /* Redraw every background character if our palette or scene changed */
                    if ((dirtybuffer[offs] != 0) || sv.refresh != 0) {
                        int sx, sy;

                        sx = 8 * (offs % 32);
                        sy = 8 * (offs / 32);

                        if (sv.flip != 0) {
                            sx = 31 * 8 - sx;
                            sy = 27 * 8 - sy;
                        }

                        charcode = back_charmap.read(offs + sv.back_scene);

                        drawgfx(tmpbitmap, Machine.gfx[1 + sv.back_charset],
                                charcode, ((charcode & 0xF0) >> 4),
                                sv.flip, sv.flip, sx, sy,
                                Machine.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }
                sprite_transparency = TRANSPARENCY_PEN;
            }

            /* Refresh the "standard" graphics */
            segar_common_screenrefresh(bitmap, sprite_transparency, TRANSPARENCY_NONE);
        }
    };

    /**
     * *************************************************************************
     * ---------------------------------------------------------------------------
     * Pig Newton Functions
     * ---------------------------------------------------------------------------
     * *************************************************************************
     */
    /**
     * *************************************************************************
     * This port seems to control the background colors for Pig Newton.
     * *************************************************************************
     */
    public static WriteHandlerPtr pignewt_back_color_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                sv.bcolor_offset = data;
            } else {
                segar_bcolortable_w.handler(sv.bcolor_offset, data);
            }
        }
    };

    /**
     * *************************************************************************
     * These ports control which background to draw for Pig Newton. They might
     * also control other video aspects, since without schematics the usage of
     * many of the data lines is indeterminate. Segar_back_scene and
     * segar_backoffset are analogous to registers used to control
     * bank-switching of the background "videorom" ROMs and the background
     * graphics ROMs, respectively. If the background changed, refresh the
     * screen.
     * *************************************************************************
     */
    public static WriteHandlerPtr pignewt_back_ports_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*unsigned*/ int tempscene;

            logerror("Port %02X:%02X\n", offset + 0xb8, data);

            /* These are all guesses.  There are some bits still being ignored! */
            switch (offset) {
                case 1:
                    /* Bit D7 turns the background off and on? */
                    if ((data & 0x80) != 0 && (sv.background_enable == 0)) {
                        sv.background_enable = 1;
                        sv.refresh = 1;
                    } else if (((data & 0x80) == 0) && (sv.background_enable == 1)) {
                        sv.background_enable = 0;
                        sv.refresh = 1;
                    }
                    /* Bits D0-D1 help select the background? */
                    tempscene = (sv.back_scene & 0x0C) | (data & 0x03);
                    if (sv.back_scene != tempscene) {
                        sv.back_scene = tempscene;
                        sv.refresh = 1;
                    }
                    break;
                case 3:
                    /* Bits D0-D1 help select the background? */
                    tempscene = ((data << 2) & 0x0C) | (sv.back_scene & 0x03);
                    if (sv.back_scene != tempscene) {
                        sv.back_scene = tempscene;
                        sv.refresh = 1;
                    }
                    break;
                case 4:
                    if (sv.back_charset != (data & 0x03)) {
                        sv.back_charset = data & 0x03;
                        sv.refresh = 1;
                    }
                    break;
            }
        }
    };

    /**
     * *************************************************************************
     * ---------------------------------------------------------------------------
     * Sinbad Mystery Functions
     * ---------------------------------------------------------------------------
     * *************************************************************************
     */
    /**
     * *************************************************************************
     * Controls the background image
     * *************************************************************************
     */
    public static WriteHandlerPtr sindbadm_back_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*unsigned*/ int tempscene;

            /* Bit D7 turns the background off and on? */
            if ((data & 0x80) != 0 && (sv.background_enable == 0)) {
                sv.background_enable = 1;
                sv.refresh = 1;
            } else if (((data & 0x80) == 0) && (sv.background_enable == 1)) {
                sv.background_enable = 0;
                sv.refresh = 1;
            }
            /* Bits D2-D6 select the background? */
            tempscene = (data >> 2) & 0x1F;
            if (sv.back_scene != tempscene) {
                sv.back_scene = tempscene;
                sv.refresh = 1;
            }
            /* Bits D0-D1 select the background char set? */
            if (sv.back_charset != (data & 0x03)) {
                sv.back_charset = data & 0x03;
                sv.refresh = 1;
            }
        }
    };

    /**
     * *************************************************************************
     * Special refresh for Sinbad Mystery, this code refreshes the static
     * background.
     * *************************************************************************
     */
    public static VhUpdatePtr sindbadm_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;
            int charcode;
            int sprite_transparency;
            /*unsigned*/ long backoffs;
            /*unsigned*/ long back_scene;

            UBytePtr back_charmap = memory_region(REGION_USER1);

            if (palette_recalc() != null || full_refresh != 0) {
                sv.refresh = 1;
            }

            sprite_transparency = TRANSPARENCY_NONE;

            /* If the background is turned on, refresh it first. */
            if (sv.background_enable != 0) {
                /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    if ((sv.char_refresh != 0) && (sv.dirtychar[videoram.read(offs)]) != 0) {
                        dirtybuffer[offs] = 1;
                    }

                    /* Redraw every background character if our palette or scene changed */
                    if ((dirtybuffer[offs] != 0) || sv.refresh != 0) {
                        int sx, sy;

                        sx = 8 * (offs % 32);
                        sy = 8 * (offs / 32);

                        if (sv.flip != 0) {
                            sx = 31 * 8 - sx;
                            sy = 27 * 8 - sy;
                        }

                        // NOTE: Pig Newton has 16 backgrounds, Sinbad Mystery has 32
                        back_scene = (sv.back_scene & 0x1C) << 10;

                        backoffs = (offs & 0x01F) + ((offs & 0x3E0) << 2) + ((sv.back_scene & 0x03) << 5);

                        charcode = back_charmap.read((int) (backoffs + back_scene));

                        drawgfx(tmpbitmap, Machine.gfx[1 + sv.back_charset],
                                charcode, ((charcode & 0xF0) >> 4),
                                sv.flip, sv.flip, sx, sy,
                                Machine.visible_area, TRANSPARENCY_NONE, 0);
                    }
                }
                sprite_transparency = TRANSPARENCY_PEN;
            }

            /* Refresh the "standard" graphics */
            segar_common_screenrefresh(bitmap, sprite_transparency, TRANSPARENCY_NONE);

        }
    };

}
