/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.drivers.toaplan2.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.cpu_set_reset_line;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.PULSE_LINE;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.sound._3812intf.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;

public class toaplan2 {

    public static final int TOAPLAN2_BG_VRAM_SIZE = 0x1000;
    /* Background RAM size (in bytes) */
    public static final int TOAPLAN2_FG_VRAM_SIZE = 0x1000;
    /* Foreground RAM size (in bytes) */
    public static final int TOAPLAN2_TOP_VRAM_SIZE = 0x1000;
    /* Top Layer  RAM size (in bytes) */
    public static final int TOAPLAN2_SPRITERAM_SIZE = 0x0800;
    /* Sprite	  RAM size (in bytes) */

    public static final int TOAPLAN2_SPRITE_FLIPX = 0x1000;
    /* Sprite flip flags (for screen flip) */
    public static final int TOAPLAN2_SPRITE_FLIPY = 0x2000;

    public static final int CPU_2_NONE = 0x00;
    public static final int CPU_2_Z80 = 0x5a;
    public static final int CPU_2_HD647180 = 0xa5;
    public static final int CPU_2_Zx80 = 0xff;

    static UBytePtr[] bgvideoram = new UBytePtr[2];
    static UBytePtr[] fgvideoram = new UBytePtr[2];
    static UBytePtr[] topvideoram = new UBytePtr[2];
    static UBytePtr[] spriteram_now = new UBytePtr[2];
    /* Sprites to draw this frame */
    static UBytePtr[] spriteram_next = new UBytePtr[2];
    /* Sprites to draw next frame */
    static UBytePtr[] spriteram_new = new UBytePtr[2];
    /* Sprites to add to next frame */
    static int toaplan2_unk_vram;
    /* Video RAM tested but not used (for Teki Paki)*/

    static int[] toaplan2_scroll_reg = new int[2];
    static int[] toaplan2_voffs = new int[2];
    static int[] bg_offs = new int[2];
    static int[] fg_offs = new int[2];
    static int[] top_offs = new int[2];
    static int[] sprite_offs = new int[2];
    static int[] bg_scrollx = new int[2];
    static int[] bg_scrolly = new int[2];
    static int[] fg_scrollx = new int[2];
    static int[] fg_scrolly = new int[2];
    static int[] top_scrollx = new int[2];
    static int[] top_scrolly = new int[2];
    static int[] sprite_scrollx = new int[2];
    static int[] sprite_scrolly = new int[2];

    static int[] display_sp = {1, 1};

    static int[][] sprite_priority = new int[2][16];
    static int[] bg_flip = {0, 0};
    static int[] fg_flip = {0, 0};
    static int[] top_flip = {0, 0};
    static int[] sprite_flip = {0, 0};

    static tilemap[] top_tilemap = new tilemap[2];
    static tilemap[] fg_tilemap = new tilemap[2];
    static tilemap[] bg_tilemap = new tilemap[2];

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static WriteHandlerPtr get_top0_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(topvideoram[0]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(0, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    public static WriteHandlerPtr get_fg0_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(fgvideoram[0]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(0, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    public static WriteHandlerPtr get_bg0_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(bgvideoram[0]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(0, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    public static WriteHandlerPtr get_top1_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(topvideoram[1]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(2, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    public static WriteHandlerPtr get_fg1_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(fgvideoram[1]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(2, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    public static WriteHandlerPtr get_bg1_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int color, tile_number, attrib, offset;
            UShortPtr source = new UShortPtr(bgvideoram[1]);

            offset = ((row * 64) + (col * 2)) & 0x7ff;

            attrib = source.read(offset);
            tile_number = source.read(offset + 1);
            color = attrib & 0x7f;
            SET_TILE_INFO(2, tile_number, color);
            tile_info.priority = (char) ((attrib & 0x0f00) >> 8);
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    static void toaplan2_vh_stop(int controller) {
        bgvideoram[controller] = null;
        fgvideoram[controller] = null;
        topvideoram[controller] = null;
        spriteram_now[controller] = null;
        spriteram_next[controller] = null;
        spriteram_new[controller] = null;
    }
    public static VhStopPtr toaplan2_0_vh_stop = new VhStopPtr() {
        public void handler() {
            toaplan2_vh_stop(0);
        }
    };
    public static VhStopPtr toaplan2_1_vh_stop = new VhStopPtr() {
        public void handler() {
            toaplan2_vh_stop(1);
            toaplan2_vh_stop(0);
        }
    };

    static int create_tilemaps_0() {
        top_tilemap[0] = tilemap_create(
                get_top0_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        fg_tilemap[0] = tilemap_create(
                get_fg0_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        bg_tilemap[0] = tilemap_create(
                get_bg0_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        if (top_tilemap[0] != null && fg_tilemap[0] != null && bg_tilemap[0] != null) {
            top_tilemap[0].transparent_pen = 0;
            fg_tilemap[0].transparent_pen = 0;
            bg_tilemap[0].transparent_pen = 0;
            return 0;
        }
        return 1;
    }

    static int create_tilemaps_1() {
        top_tilemap[1] = tilemap_create(
                get_top1_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        fg_tilemap[1] = tilemap_create(
                get_fg1_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        bg_tilemap[1] = tilemap_create(
                get_bg1_tile_info,
                TILEMAP_TRANSPARENT,
                16, 16,
                32, 32
        );

        if (top_tilemap[1] != null && fg_tilemap[1] != null && bg_tilemap[1] != null) {
            top_tilemap[1].transparent_pen = 0;
            fg_tilemap[1].transparent_pen = 0;
            bg_tilemap[1].transparent_pen = 0;
            return 0;
        }
        return 1;
    }
    static int error_level = 0;
    public static ReadHandlerPtr toaplan2_vh_start = new ReadHandlerPtr() {
        public int handler(int controller) {
            if ((spriteram_new[controller] = new UBytePtr(TOAPLAN2_SPRITERAM_SIZE)) == null) {
                return 1;
            }
            memset(spriteram_new[controller], 0, TOAPLAN2_SPRITERAM_SIZE);

            if ((spriteram_next[controller] = new UBytePtr(TOAPLAN2_SPRITERAM_SIZE)) == null) {
                spriteram_new[controller] = null;
                return 1;
            }
            memset(spriteram_next[controller], 0, TOAPLAN2_SPRITERAM_SIZE);

            if ((spriteram_now[controller] = new UBytePtr(TOAPLAN2_SPRITERAM_SIZE)) == null) {
                spriteram_next[controller] = null;
                spriteram_new[controller] = null;
                return 1;
            }
            memset(spriteram_now[controller], 0, TOAPLAN2_SPRITERAM_SIZE);

            if ((topvideoram[controller] = new UBytePtr(TOAPLAN2_TOP_VRAM_SIZE)) == null) {
                spriteram_now[controller] = null;
                spriteram_next[controller] = null;
                spriteram_new[controller] = null;
                return 1;
            }
            memset(topvideoram[controller], 0, TOAPLAN2_TOP_VRAM_SIZE);

            if ((fgvideoram[controller] = new UBytePtr(TOAPLAN2_FG_VRAM_SIZE)) == null) {
                topvideoram[controller] = null;
                spriteram_now[controller] = null;
                spriteram_next[controller] = null;
                spriteram_new[controller] = null;
                return 1;
            }
            memset(fgvideoram[controller], 0, TOAPLAN2_FG_VRAM_SIZE);

            if ((bgvideoram[controller] = new UBytePtr(TOAPLAN2_BG_VRAM_SIZE)) == null) {
                fgvideoram[controller] = null;
                topvideoram[controller] = null;
                spriteram_now[controller] = null;
                spriteram_next[controller] = null;
                spriteram_new[controller] = null;
                return 1;
            }
            memset(bgvideoram[controller], 0, TOAPLAN2_BG_VRAM_SIZE);

            if (controller == 0) {
                error_level |= create_tilemaps_0();
            }
            if (controller == 1) {
                error_level |= create_tilemaps_1();
            }
            return error_level;
        }
    };
    public static VhStartPtr toaplan2_0_vh_start = new VhStartPtr() {
        public int handler() {
            return toaplan2_vh_start.handler(0);
        }
    };
    public static VhStartPtr toaplan2_1_vh_start = new VhStartPtr() {
        public int handler() {
            int error_level = 0;
            error_level |= toaplan2_vh_start.handler(0);
            error_level |= toaplan2_vh_start.handler(1);
            return error_level;
        }
    };

    /**
     * *************************************************************************
     *
     * Video I/O port hardware.
     *
     **************************************************************************
     */
    static void toaplan2_voffs_w(int offset, int data, int controller) {
        toaplan2_voffs[controller] = data;

        /* Layers are seperated by ranges in the offset */
        switch (data & 0xfc00) {
            case 0x0400:
            case 0x0000:
                bg_offs[controller] = (data & 0x7ff) * 2;
                break;
            case 0x0c00:
            case 0x0800:
                fg_offs[controller] = (data & 0x7ff) * 2;
                break;
            case 0x1400:
            case 0x1000:
                top_offs[controller] = (data & 0x7ff) * 2;
                break;
            case 0x1800:
                sprite_offs[controller] = (data & 0x3ff) * 2;
                break;
            default:
                if (errorlog != null) {
                    fprintf(errorlog, "Hmmm, unknown video controller %01x layer being selected (%08x)\n", controller, data);
                }
                data &= 0x1800;
                if ((data & 0x1800) == 0x0000) {
                    bg_offs[controller] = (data & 0x7ff) * 2;
                }
                if ((data & 0x1800) == 0x0800) {
                    fg_offs[controller] = (data & 0x7ff) * 2;
                }
                if ((data & 0x1800) == 0x1000) {
                    top_offs[controller] = (data & 0x7ff) * 2;
                }
                if ((data & 0x1800) == 0x1800) {
                    sprite_offs[controller] = (data & 0x3ff) * 2;
                }
                break;
        }
    }
    public static WriteHandlerPtr toaplan2_0_voffs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_voffs_w(offset, data, 0);
        }
    };
    public static WriteHandlerPtr toaplan2_1_voffs_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_voffs_w(offset, data, 1);
        }
    };
    static int video_data = 0;

    static int toaplan2_videoram_r(int offset, int controller) {

        int videoram_offset;

        switch (toaplan2_voffs[controller] & 0xfc00) {
            case 0x0400:
            case 0x0000:
                videoram_offset = bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE - 1);
                video_data = bgvideoram[controller].READ_WORD(videoram_offset);
                bg_offs[controller] += 2;
                if (bg_offs[controller] > TOAPLAN2_BG_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Reading %04x from out of range BG Layer address (%08x)  Video controller %01x  !!!\n", video_data, bg_offs[controller], controller);
                    }
                }
                break;
            case 0x0c00:
            case 0x0800:
                videoram_offset = fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE - 1);
                video_data = fgvideoram[controller].READ_WORD(videoram_offset);
                fg_offs[controller] += 2;
                if (fg_offs[controller] > TOAPLAN2_FG_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Reading %04x from out of range FG Layer address (%08x)  Video controller %01x  !!!\n", video_data, fg_offs[controller], controller);
                    }
                }
                break;
            case 0x1400:
            case 0x1000:
                videoram_offset = top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE - 1);
                video_data = topvideoram[controller].READ_WORD(videoram_offset);
                top_offs[controller] += 2;
                if (top_offs[controller] > TOAPLAN2_TOP_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Reading %04x from out of range TOP Layer address (%08x)  Video controller %01x  !!!\n", video_data, top_offs[controller], controller);
                    }
                }
                break;
            case 0x1800:
                videoram_offset = sprite_offs[controller] & (TOAPLAN2_SPRITERAM_SIZE - 1);
                video_data = spriteram_new[controller].READ_WORD(videoram_offset);
                sprite_offs[controller] += 2;
                if (sprite_offs[controller] > TOAPLAN2_SPRITERAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Reading %04x from out of range Sprite address (%08x)  Video controller %01x  !!!\n", video_data, sprite_offs[controller], controller);
                    }
                }
                break;
            default:
                video_data = toaplan2_unk_vram;
                if (errorlog != null) {
                    fprintf(errorlog, "Hmmm, reading %04x from unknown video layer (%08x)  Video controller %01x  !!!\n", video_data, toaplan2_voffs[controller], controller);
                }
                break;
        }
        return video_data;
    }
    public static ReadHandlerPtr toaplan2_0_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan2_videoram_r(offset, 0);
        }
    };
    public static ReadHandlerPtr toaplan2_1_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return toaplan2_videoram_r(offset, 1);
        }
    };

    public static void toaplan2_videoram_w(int offset, int data, int controller) {
        int oldword = 0;
        int videoram_offset;
        int dirty_cell;

        switch (toaplan2_voffs[controller] & 0xfc00) {
            case 0x0400:
            case 0x0000:
                videoram_offset = bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE - 1);
                oldword = bgvideoram[controller].READ_WORD(videoram_offset);
                if (data != oldword) {
                    bgvideoram[controller].WRITE_WORD(videoram_offset, data);
                    dirty_cell = (bg_offs[controller] & (TOAPLAN2_BG_VRAM_SIZE - 3)) / 2;
                    tilemap_mark_tile_dirty(bg_tilemap[controller], (dirty_cell % 64) / 2, dirty_cell / 64);
                }
                bg_offs[controller] += 2;
                if (bg_offs[controller] > TOAPLAN2_BG_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Writing %04x to out of range BG Layer address (%08x)  Video controller %01x  !!!\n", data, bg_offs[controller], controller);
                    }
                }
                break;
            case 0x0c00:
            case 0x0800:
                videoram_offset = fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE - 1);
                oldword = fgvideoram[controller].READ_WORD(videoram_offset);
                if (data != oldword) {
                    fgvideoram[controller].WRITE_WORD(videoram_offset, data);
                    dirty_cell = (fg_offs[controller] & (TOAPLAN2_FG_VRAM_SIZE - 3)) / 2;
                    tilemap_mark_tile_dirty(fg_tilemap[controller], (dirty_cell % 64) / 2, dirty_cell / 64);
                }
                fg_offs[controller] += 2;
                if (fg_offs[controller] > TOAPLAN2_FG_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Writing %04x to out of range FG Layer address (%08x)  Video controller %01x  !!!\n", data, fg_offs[controller], controller);
                    }
                }
                break;
            case 0x1400:
            case 0x1000:
                videoram_offset = top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE - 1);
                oldword = topvideoram[controller].READ_WORD(videoram_offset);
                if (data != oldword) {
                    topvideoram[controller].WRITE_WORD(videoram_offset, data);
                    dirty_cell = (top_offs[controller] & (TOAPLAN2_TOP_VRAM_SIZE - 3)) / 2;
                    tilemap_mark_tile_dirty(top_tilemap[controller], (dirty_cell % 64) / 2, dirty_cell / 64);
                }
                top_offs[controller] += 2;
                if (top_offs[controller] > TOAPLAN2_TOP_VRAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Writing %04x to out of range TOP Layer address (%08x)  Video controller %01x  !!!\n", data, top_offs[controller], controller);
                    }
                }
                break;
            case 0x1800:
                videoram_offset = sprite_offs[controller] & (TOAPLAN2_SPRITERAM_SIZE - 1);
                spriteram_new[controller].WRITE_WORD(videoram_offset, data);
                sprite_offs[controller] += 2;
                if (sprite_offs[controller] > TOAPLAN2_SPRITERAM_SIZE) {
                    if (errorlog != null) {
                        fprintf(errorlog, "Writing %04x to out of range Sprite address (%08x)  Video controller %01x  !!!\n", data, sprite_offs[controller], controller);
                    }
                }
                break;
            default:
                toaplan2_unk_vram = data;
                if (errorlog != null) {
                    fprintf(errorlog, "Hmmm, writing %04x to unknown video layer (%08x)  Video controller %01x  \n", toaplan2_unk_vram, toaplan2_voffs[controller], controller);
                }
                break;
        }
    }
    public static WriteHandlerPtr toaplan2_0_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_videoram_w(offset, data, 0);
        }
    };
    public static WriteHandlerPtr toaplan2_1_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_videoram_w(offset, data, 1);
        }
    };

    public static void toaplan2_scroll_reg_select_w(int offset, int data, int controller) {
        toaplan2_scroll_reg[controller] = data;
        if ((toaplan2_scroll_reg[controller] & 0xffffff70) != 0) {
            if (errorlog != null) {
                fprintf(errorlog, "Hmmm, unknown video control register selected (%08x)  Video controller %01x  \n", toaplan2_scroll_reg[controller], controller);
            }
        }
    }
    public static WriteHandlerPtr toaplan2_0_scroll_reg_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_scroll_reg_select_w(offset, data, 0);
        }
    };
    public static WriteHandlerPtr toaplan2_1_scroll_reg_select_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_scroll_reg_select_w(offset, data, 1);
        }
    };

    static void toaplan2_scroll_reg_data_w(int offset, int data, int controller) {
        /**
         * *********************************************************************
         */
        /**
         * *** X and Y layer flips can be set independantly, so emulate it *****
         */
        /**
         * *********************************************************************
         */

        //	int vid_controllers = 1;
        switch (toaplan2_scroll_reg[controller]) {
            case 0x00:
                bg_scrollx[controller] = data - 0x1d6;
                /* 1D6h */
                bg_flip[controller] &= (~TILEMAP_FLIPX);
                tilemap_set_flip(bg_tilemap[controller], bg_flip[controller]);
                tilemap_set_scrollx(bg_tilemap[controller], 0, bg_scrollx[controller]);
                break;
            case 0x01:
                bg_scrolly[controller] = data - 0x1ef;
                /* 1EFh */
                bg_flip[controller] &= (~TILEMAP_FLIPY);
                tilemap_set_flip(bg_tilemap[controller], bg_flip[controller]);
                tilemap_set_scrolly(bg_tilemap[controller], 0, bg_scrolly[controller]);
                break;
            case 0x02:
                fg_scrollx[controller] = data - 0x1d8;
                /* 1D0h */
                fg_flip[controller] &= (~TILEMAP_FLIPX);
                tilemap_set_flip(fg_tilemap[controller], fg_flip[controller]);
                tilemap_set_scrollx(fg_tilemap[controller], 0, fg_scrollx[controller]);
                break;
            case 0x03:
                fg_scrolly[controller] = data - 0x1ef;
                /* 1EFh */
                fg_flip[controller] &= (~TILEMAP_FLIPY);
                tilemap_set_flip(fg_tilemap[controller], fg_flip[controller]);
                tilemap_set_scrolly(fg_tilemap[controller], 0, fg_scrolly[controller]);
                break;
            case 0x04:
                top_scrollx[controller] = data - 0x1da;
                /* 1DAh */
                top_flip[controller] &= (~TILEMAP_FLIPX);
                tilemap_set_flip(top_tilemap[controller], top_flip[controller]);
                tilemap_set_scrollx(top_tilemap[controller], 0, top_scrollx[controller]);
                break;
            case 0x05:
                top_scrolly[controller] = data - 0x1ef;
                /* 1EFh */
                top_flip[controller] &= (~TILEMAP_FLIPY);
                tilemap_set_flip(top_tilemap[controller], top_flip[controller]);
                tilemap_set_scrolly(top_tilemap[controller], 0, top_scrolly[controller]);
                break;
            case 0x06:
                sprite_scrollx[controller] = data - 0x1cc;
                /* 1D4h */
                if ((sprite_scrollx[controller] & 0x80000000) != 0) {
                    sprite_scrollx[controller] |= 0xfffffe00;
                } else {
                    sprite_scrollx[controller] &= 0x1ff;
                }
                sprite_flip[controller] &= (~TOAPLAN2_SPRITE_FLIPX);
                break;
            case 0x07:
                sprite_scrolly[controller] = data - 0x1ef;
                /* 1F7h */
                if ((sprite_scrolly[controller] & 0x80000000) != 0) {
                    sprite_scrolly[controller] |= 0xfffffe00;
                } else {
                    sprite_scrolly[controller] &= 0x1ff;
                }
                sprite_flip[controller] &= (~TOAPLAN2_SPRITE_FLIPY);
                break;
            case 0x0f:
                break;
            case 0x80:
                bg_scrollx[controller] = data - 0x229;
                /* 169h */
                bg_flip[controller] |= TILEMAP_FLIPX;
                tilemap_set_flip(bg_tilemap[controller], bg_flip[controller]);
                tilemap_set_scrollx(bg_tilemap[controller], 0, bg_scrollx[controller]);
                break;
            case 0x81:
                bg_scrolly[controller] = data - 0x210;
                /* 100h */
                bg_flip[controller] |= TILEMAP_FLIPY;
                tilemap_set_flip(bg_tilemap[controller], bg_flip[controller]);
                tilemap_set_scrolly(bg_tilemap[controller], 0, bg_scrolly[controller]);
                break;
            case 0x82:
                fg_scrollx[controller] = data - 0x227;
                /* 15Fh */
                fg_flip[controller] |= TILEMAP_FLIPX;
                tilemap_set_flip(fg_tilemap[controller], fg_flip[controller]);
                tilemap_set_scrollx(fg_tilemap[controller], 0, fg_scrollx[controller]);
                break;
            case 0x83:
                fg_scrolly[controller] = data - 0x210;
                /* 100h */
                fg_flip[controller] |= TILEMAP_FLIPY;
                tilemap_set_flip(fg_tilemap[controller], fg_flip[controller]);
                tilemap_set_scrolly(fg_tilemap[controller], 0, fg_scrolly[controller]);
                break;
            case 0x84:
                top_scrollx[controller] = data - 0x225;
                /* 165h */
                top_flip[controller] |= TILEMAP_FLIPX;
                tilemap_set_flip(top_tilemap[controller], top_flip[controller]);
                tilemap_set_scrollx(top_tilemap[controller], 0, top_scrollx[controller]);
                break;
            case 0x85:
                top_scrolly[controller] = data - 0x210;
                /* 100h */
                top_flip[controller] |= TILEMAP_FLIPY;
                tilemap_set_flip(top_tilemap[controller], top_flip[controller]);
                tilemap_set_scrolly(top_tilemap[controller], 0, top_scrolly[controller]);
                break;
            case 0x86:
                sprite_scrollx[controller] = data - 0x17b;
                /* 17Bh */
                if ((sprite_scrollx[controller] & 0x80000000) != 0) {
                    sprite_scrollx[controller] |= 0xfffffe00;
                } else {
                    sprite_scrollx[controller] &= 0x1ff;
                }
                sprite_flip[controller] |= TOAPLAN2_SPRITE_FLIPX;
                break;
            case 0x87:
                sprite_scrolly[controller] = data - 0x108;
                /* 108h */
                if ((sprite_scrolly[controller] & 0x80000000) != 0) {
                    sprite_scrolly[controller] |= 0xfffffe00;
                } else {
                    sprite_scrolly[controller] &= 0x1ff;
                }
                sprite_flip[controller] |= TOAPLAN2_SPRITE_FLIPY;
                break;
            case 0x8f:
                break;

            case 0x0e:
                /**
                 * ***** Initialise video controller register ? ******
                 */
                if ((toaplan2_sub_cpu == CPU_2_Z80) && (data == 3)) {
                    /* HACK! When tilted, sound CPU needs to be reset. */
                    cpu_set_reset_line(1, PULSE_LINE);
                    YM3812_sh_reset();
                }
            default:
                if (errorlog != null) {
                    fprintf(errorlog, "Hmmm, writing %08x to unknown video control register (%08x)  Video controller %01x  !!!\n", data, toaplan2_scroll_reg[controller], controller);
                }
                break;
        }

    }
    public static WriteHandlerPtr toaplan2_0_scroll_reg_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_scroll_reg_data_w(offset, data, 0);
        }
    };
    public static WriteHandlerPtr toaplan2_1_scroll_reg_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            toaplan2_scroll_reg_data_w(offset, data, 1);
        }
    };

    /**
     * *************************************************************************
     * Sprite Handlers
	**************************************************************************
     */
    static void mark_sprite_colors(int controller) {
        int offs, attrib, sprite, color, i, pal_base;
        int sprite_sizex, sprite_sizey, temp_x, temp_y;
        int[] colmask = new int[64];

        UShortPtr source = new UShortPtr(spriteram_now[controller]);

        pal_base = Machine.drv.gfxdecodeinfo[((controller * 2) + 1)].color_codes_start;

        for (i = 0; i < 64; i++) {
            colmask[i] = 0;
        }

        for (offs = 0; offs < (TOAPLAN2_SPRITERAM_SIZE / 2); offs += 4) {
            attrib = source.read(offs);
            sprite = source.read(offs + 1) | ((attrib & 3) << 16);
            sprite %= Machine.gfx[((controller * 2) + 1)].total_elements;
            if ((attrib & 0x8000) != 0) {
                /* While we're here, mark all priorities used */
                sprite_priority[controller][((attrib & 0x0f00) >> 8)] = display_sp[controller];

                color = (attrib >> 2) & 0x3f;
                sprite_sizex = (source.read(offs + 2) & 0x0f) + 1;
                sprite_sizey = (source.read(offs + 3) & 0x0f) + 1;

                for (temp_y = 0; temp_y < sprite_sizey; temp_y++) {
                    for (temp_x = 0; temp_x < sprite_sizex; temp_x++) {
                        colmask[color] |= Machine.gfx[((controller * 2) + 1)].pen_usage[sprite];
                        sprite++;
                    }
                }
            }
        }

        for (color = 0; color < 64; color++) {
            if ((color == 0) && (colmask[0] & 1) != 0) {
                palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
            }
            for (i = 1; i < 16; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                }
            }
        }
    }

    static void draw_sprites(osd_bitmap bitmap, int controller, int priority_to_display) {
        GfxElement gfx = Machine.gfx[((controller * 2) + 1)];
        rectangle clip = Machine.drv.visible_area;

        UShortPtr source = new UShortPtr(spriteram_now[controller]);

        int offs;
        for (offs = 0; offs < (TOAPLAN2_SPRITERAM_SIZE / 2); offs += 4) {
            int attrib, sprite, color, priority, flipx, flipy, sx, sy;
            int sprite_sizex, sprite_sizey, temp_x, temp_y, sx_base, sy_base;

            attrib = source.read(offs);
            priority = (attrib & 0x0f00) >> 8;

            if ((priority == priority_to_display) && (attrib & 0x8000) != 0) {
                sprite = ((attrib & 3) << 16) | source.read(offs + 1);
                /* 18 bit */
                color = (attrib >> 2) & 0x3f;

                /**
                 * **** find out sprite size *****
                 */
                sprite_sizex = ((source.read(offs + 2) & 0x0f) + 1) * 8;
                sprite_sizey = ((source.read(offs + 3) & 0x0f) + 1) * 8;

                /**
                 * **** find position to display sprite *****
                 */
                sx_base = (source.read(offs + 2) >> 7) - sprite_scrollx[controller];
                sy_base = (source.read(offs + 3) >> 7) - sprite_scrolly[controller];

                flipx = attrib & TOAPLAN2_SPRITE_FLIPX;
                flipy = attrib & TOAPLAN2_SPRITE_FLIPY;

                if (flipx != 0) {
                    sx_base -= 7;
                    /**
                     * **** wrap around sprite position *****
                     */
                    if (sx_base >= 0x1c0) {
                        sx_base -= 0x200;
                    }
                } else if (sx_base >= 0x180) {
                    sx_base -= 0x200;
                }

                if (flipy != 0) {
                    sy_base -= 7;
                    if (sy_base >= 0x1c0) {
                        sy_base -= 0x200;
                    }
                } else if (sy_base >= 0x180) {
                    sy_base -= 0x200;
                }

                /**
                 * **** flip the sprite layer in any active X or Y flip *****
                 */
                if (sprite_flip[controller] != 0) {
                    if ((sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPX) != 0) {
                        sx_base = 320 - sx_base;
                    }
                    if ((sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPY) != 0) {
                        sy_base = 240 - sy_base;
                    }
                }

                /**
                 * **** cancel flip, if it and sprite layer flip are active *****
                 */
                flipx = (flipx ^ (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPX));
                flipy = (flipy ^ (sprite_flip[controller] & TOAPLAN2_SPRITE_FLIPY));

                for (temp_y = 0; temp_y < sprite_sizey; temp_y += 8) {
                    if (flipy != 0) {
                        sy = sy_base - temp_y;
                    } else {
                        sy = sy_base + temp_y;
                    }
                    for (temp_x = 0; temp_x < sprite_sizex; temp_x += 8) {
                        if (flipx != 0) {
                            sx = sx_base - temp_x;
                        } else {
                            sx = sx_base + temp_x;
                        }

                        drawgfx(bitmap, gfx, sprite,
                                color,
                                flipx, flipy,
                                sx, sy,
                                clip, TRANSPARENCY_PEN, 0);

                        sprite++;
                    }
                }
            }
        }
    }

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap.
     *
     **************************************************************************
     */
    public static VhUpdatePtr toaplan2_0_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int priority;

            for (priority = 0; priority < 16; priority++) {
                sprite_priority[0][priority] = 0;		/* Clear priorities used list */
            }

            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            mark_sprite_colors(0);
            /* Also mark priorities used */

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, palette_transparent_pen, Machine.drv.visible_area);

            for (priority = 0; priority < 16; priority++) {
                tilemap_draw(bitmap, bg_tilemap[0], priority);
                tilemap_draw(bitmap, fg_tilemap[0], priority);
                tilemap_draw(bitmap, top_tilemap[0], priority);
                if (sprite_priority[0][priority] != 0) {
                    draw_sprites(bitmap, 0, priority);
                }
            }
        }
    };
    public static VhUpdatePtr toaplan2_1_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int priority;

            for (priority = 0; priority < 16; priority++) {
                sprite_priority[0][priority] = 0;
                /* Clear priorities used list */
                sprite_priority[1][priority] = 0;
                /* Clear priorities used list */
            }

            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            mark_sprite_colors(0);
            /* Also mark priorities used */
            mark_sprite_colors(1);
            /* Also mark priorities used */

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, palette_transparent_pen, Machine.drv.visible_area);

            for (priority = 0; priority < 16; priority++) {
                tilemap_draw(bitmap, bg_tilemap[1], priority);
                tilemap_draw(bitmap, fg_tilemap[1], priority);
                tilemap_draw(bitmap, top_tilemap[1], priority);
                if (sprite_priority[1][priority] != 0) {
                    draw_sprites(bitmap, 1, priority);
                }
            }
            for (priority = 0; priority < 16; priority++) {
                tilemap_draw(bitmap, bg_tilemap[0], priority);
                tilemap_draw(bitmap, fg_tilemap[0], priority);
                tilemap_draw(bitmap, top_tilemap[0], priority);
                if (sprite_priority[0][priority] != 0) {
                    draw_sprites(bitmap, 0, priority);
                }
            }
        }
    };
    public static VhUpdatePtr batsugun_1_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int priority;

            for (priority = 0; priority < 16; priority++) {
                sprite_priority[0][priority] = 0;
                /* Clear priorities used list */
                sprite_priority[1][priority] = 0;
                /* Clear priorities used list */
            }

            tilemap_update(ALL_TILEMAPS);

            palette_init_used_colors();
            mark_sprite_colors(0);
            /* Also mark priorities used */
            mark_sprite_colors(1);
            /* Also mark priorities used */

            if (palette_recalc() != null) {
                tilemap_mark_all_pixels_dirty(ALL_TILEMAPS);
            }

            tilemap_render(ALL_TILEMAPS);

            fillbitmap(bitmap, palette_transparent_pen, Machine.drv.visible_area);

            for (priority = 0; priority < 16; priority++) {
                tilemap_draw(bitmap, bg_tilemap[1], priority);
                tilemap_draw(bitmap, bg_tilemap[0], priority);
                tilemap_draw(bitmap, fg_tilemap[1], priority);
                tilemap_draw(bitmap, top_tilemap[1], priority);
                if (sprite_priority[1][priority] != 0) {
                    draw_sprites(bitmap, 1, priority);
                }
            }
            for (priority = 0; priority < 16; priority++) {
                tilemap_draw(bitmap, fg_tilemap[0], priority);
                tilemap_draw(bitmap, top_tilemap[0], priority);
                if (sprite_priority[0][priority] != 0) {
                    draw_sprites(bitmap, 0, priority);
                }
            }
        }
    };

    public static VhEofCallbackPtr toaplan2_0_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            /**
             * Shift sprite RAM buffers *** Used to fix sprite lag *
             */
            memcpy(spriteram_now[0], spriteram_next[0], TOAPLAN2_SPRITERAM_SIZE);
            memcpy(spriteram_next[0], spriteram_new[0], TOAPLAN2_SPRITERAM_SIZE);
        }
    };
    public static VhEofCallbackPtr toaplan2_1_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            /**
             * Shift sprite RAM buffers *** Used to fix sprite lag *
             */
            memcpy(spriteram_now[0], spriteram_next[0], TOAPLAN2_SPRITERAM_SIZE);
            memcpy(spriteram_next[0], spriteram_new[0], TOAPLAN2_SPRITERAM_SIZE);
            memcpy(spriteram_now[1], spriteram_next[1], TOAPLAN2_SPRITERAM_SIZE);
            memcpy(spriteram_next[1], spriteram_new[1], TOAPLAN2_SPRITERAM_SIZE);
        }
    };

}
