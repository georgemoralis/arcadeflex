/**
 * *************************************************************************
 *
 * vidhrdw/shuuz.c
 *
 * Functions to emulate the video hardware of the machine.
 *
 ****************************************************************************
 *
 * Playfield encoding ------------------ 1 16-bit word is used
 *
 * Word 1: Bits 13-15 = palette Bits 0-12 = image number
 *
 *
 * Motion Object encoding ---------------------- 4 16-bit words are used
 *
 * Word 1: Bits 0-7 = link to the next motion object
 *
 * Word 2: Bits 0-11 = image index
 *
 * Word 3: Bits 7-15 = horizontal position Bits 0-3 = motion object palette
 *
 * Word 4: Bits 7-15 = vertical position Bits 4-6 = horizontal size of the
 * object, in tiles Bit 3 = horizontal flip Bits 0-2 = vertical size of the
 * object, in tiles
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

import static gr.codebb.arcadeflex.v036.platform.libc_old.memset;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sizeof;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.machine.atarigen.*;
import static gr.codebb.arcadeflex.v036.machine.atarigenH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.paletteH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class shuuz {

    public static final int XCHARS = 42;
    public static final int YCHARS = 30;

    public static final int XDIM = (XCHARS * 8);
    public static final int YDIM = (YCHARS * 8);

    /**
     * ***********************************
     *
     * Constants
     *
     ************************************
     */
    public static final int OVERRENDER_STANDARD = 0;
    public static final int OVERRENDER_PRIORITY = 1;

    /**
     * ***********************************
     *
     * Structures
     *
     ************************************
     */
    /*TODO*///    struct pf_overrender_data
/*TODO*///
/*TODO*///    {
/*TODO*///        struct osd_bitmap *bitmap;
/*TODO*///        int type, color;
/*TODO*///    }
/*TODO*///    ;
    /**
     * ***********************************
     *
     * Video system start
     *
     ************************************
     */
    public static atarigen_mo_desc mo_desc = new atarigen_mo_desc(
            256, /* maximum number of MO's */
            8, /* number of bytes per MO entry */
            2, /* number of bytes between MO words */
            0, /* ignore an entry if this word == 0xffff */
            0, 0, 0xff, /* link = (data[linkword] >> linkshift) & linkmask */
            0 /* render in reverse link order */
    );
    public static atarigen_pf_desc pf_desc = new atarigen_pf_desc(
            8, 8, /* width/height of each tile */
            64, 64, /* number of tiles in each direction */
            1 /* non-scrolling */
    );
    public static VhStartPtr shuuz_vh_start = new VhStartPtr() {
        public int handler() {

            /* initialize the playfield */
            if (atarigen_pf_init(pf_desc) != 0) {
                return 1;
            }

            /* initialize the motion objects */
            if (atarigen_mo_init(mo_desc) != 0) {
                atarigen_pf_free();
                return 1;
            }

            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Video system shutdown
     *
     ************************************
     */
    public static VhStopPtr shuuz_vh_stop = new VhStopPtr() {
        public void handler() {
            atarigen_pf_free();
            atarigen_mo_free();
        }
    };

    /**
     * ***********************************
     *
     * Playfield RAM write handler
     *
     ************************************
     */
    public static WriteHandlerPtr shuuz_playfieldram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = atarigen_playfieldram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);

            /* update the data if different */
            if (oldword != newword) {
                atarigen_playfieldram.WRITE_WORD(offset, newword);
                atarigen_pf_dirty[(offset & 0x1fff) / 2] = 1;
            }

            /* handle the latch, but only write the upper byte */
            if (offset < 0x2000 && atarigen_video_control_state.latch1 != -1) {
                shuuz_playfieldram_w.handler(offset + 0x2000, atarigen_video_control_state.latch1 | 0x00ff0000);
            }
        }
    };

    /**
     * ***********************************
     *
     * Periodic scanline updater
     *
     ************************************
     */
    public static atarigen_scanline_callbackPtr shuuz_scanline_update = new atarigen_scanline_callbackPtr() {
        public void handler(int scanline) {
            /* update the playfield */
            if (scanline == 0) {
                atarigen_video_control_update(new UBytePtr(atarigen_playfieldram, 0x1f00));
            }

            /* update the MOs from the SLIP table */
            atarigen_mo_update_slip_512(atarigen_spriteram, atarigen_video_control_state.sprite_yscroll, scanline, new UBytePtr(atarigen_playfieldram, 0x1f80));
        }
    };

    /**
     * ***********************************
     *
     * Main refresh
     *
     ************************************
     */
    public static VhUpdatePtr shuuz_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* remap if necessary */
            if (update_palette() != null) {
                memset(atarigen_pf_dirty, 1, atarigen_playfieldram_size[0] / 4);
            }

            /* update playfield */
            /*TODO*///            atarigen_pf_process(pf_render_callback, bitmap,  & Machine.drv.visible_area);

            /* render the motion objects */
            /*TODO*///            atarigen_mo_process(mo_render_callback, bitmap);

            /* update onscreen messages */
            /*TODO*///            atarigen_update_messages();
        }
    };

    /**
     * ***********************************
     *
     * Palette management
     *
     ************************************
     */
    static UBytePtr update_palette() {
        char[] mo_map = new char[16];
        char[] pf_map = new char[16];
        int i, j;

        /* reset color tracking */
        memset(mo_map, 0, sizeof(mo_map));
        memset(pf_map, 0, sizeof(pf_map));
        palette_init_used_colors();

        /* update color usage for the playfield */
        atarigen_pf_process(pf_color_callback, pf_map, Machine.drv.visible_area);

        /* update color usage for the mo's */
        /*TODO*///        atarigen_mo_process(mo_color_callback, mo_map);

        /* rebuild the playfield palette */
        for (i = 0; i < 16; i++) {
            char used = pf_map[i];
            if (used != 0) {
                for (j = 0; j < 16; j++) {
                    if ((used & (1 << j)) != 0) {
                        palette_used_colors.write(0x100 + i * 16 + j, PALETTE_COLOR_USED);
                    }
                }
            }
        }

        /* rebuild the motion object palette */
        for (i = 0; i < 16; i++) {
            char used = mo_map[i];
            if (used != 0) {
                palette_used_colors.write(0x000 + i * 16 + 0, PALETTE_COLOR_TRANSPARENT);
                for (j = 1; j < 16; j++) {
                    if ((used & (1 << j)) != 0) {
                        palette_used_colors.write(0x000 + i * 16 + j, PALETTE_COLOR_USED);
                    }
                }
            }
        }

        /* special case color 15 of motion object palette 15 */
        palette_used_colors.write(0x000 + 15 * 16 + 15, PALETTE_COLOR_TRANSPARENT);

        return palette_recalc();
    }

    /**
     * ***********************************
     *
     * Playfield palette
     *
     ************************************
     */
    public static atarigen_pf_callbackPtr pf_color_callback = new atarigen_pf_callbackPtr() {

        public void handler(rectangle tiles, rectangle clip, atarigen_pf_state state, Object param) {
            int[] usage = Machine.gfx[0].pen_usage;
            char[] colormap = (char[]) param;
            int x, y;

            /* standard loop over tiles */
            for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63) {
                for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63) {
                    int offs = x * 64 + y;
                    int data1 = atarigen_playfieldram.READ_WORD(offs * 2);
                    int data2 = atarigen_playfieldram.READ_WORD(offs * 2 + 0x2000);
                    int code = data1 & 0x3fff;
                    int color = (data2 >> 8) & 15;

                    /* mark the colors used by this tile */
                    colormap[color] |= usage[code];
                }
            }
        }
    };

    /**
     * ***********************************
     *
     * Playfield rendering
     *
     ************************************
     */
    /*TODO*///    static void pf_render_callback(
/*TODO*///            
/*TODO*///    const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param
/*TODO*///
/*TODO*///    )
/*TODO*///	{
/*TODO*///		const
/*TODO*///        struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///        struct osd_bitmap *bitmap = param;
/*TODO*///        int x, y;
/*TODO*///
/*TODO*///        /* standard loop over tiles */
/*TODO*///        for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63) {
/*TODO*///            for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63) {
/*TODO*///                int offs = x * 64 + y;

    /* update only if dirty */
    /*TODO*///                if (atarigen_pf_dirty[offs]) {
/*TODO*///                    int data1 = READ_WORD( & atarigen_playfieldram[offs * 2]);
/*TODO*///                    int data2 = READ_WORD( & atarigen_playfieldram[offs * 2 + 0x2000]);
/*TODO*///                    int color = (data2 >> 8) & 15;
/*TODO*///                    int hflip = data1 & 0x8000;
/*TODO*///                    int code = data1 & 0x3fff;
/*TODO*///
/*TODO*///                    drawgfx(atarigen_pf_bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, 0, TRANSPARENCY_NONE, 0);
/*TODO*///                    atarigen_pf_dirty[offs] = 0;

    /*TODO*///                }
/*TODO*///            }
/*TODO*///        }

    /* then blast the result */
    /*TODO*///        copybitmap(bitmap, atarigen_pf_bitmap, 0, 0, 0, 0, clip, TRANSPARENCY_NONE, 0);
/*TODO*///    }
    /**
     * ***********************************
     *
     * Playfield overrendering
     *
     ************************************
     */
    /*TODO*///    static void pf_overrender_callback(
    /*TODO*///    const struct rectangle *clip, const struct rectangle *tiles, const struct atarigen_pf_state *state, void *param
/*TODO*///
/*TODO*///    )
/*TODO*///	{
/*TODO*///		const
/*TODO*///        struct pf_overrender_data *overrender_data = param;
/*TODO*///        const
/*TODO*///        struct GfxElement *gfx = Machine.gfx[0];
/*TODO*///        struct osd_bitmap *bitmap = overrender_data.bitmap;
/*TODO*///        int x, y;
/*TODO*///
        /* standard loop over tiles */
    /*TODO*///        for (x = tiles.min_x; x != tiles.max_x; x = (x + 1) & 63) {
/*TODO*///            for (y = tiles.min_y; y != tiles.max_y; y = (y + 1) & 63) {
/*TODO*///                int offs = x * 64 + y;
/*TODO*///                int data2 = READ_WORD( & atarigen_playfieldram[offs * 2 + 0x2000]);
/*TODO*///                int color = (data2 >> 8) & 15;
/*TODO*///
                /* overdraw if the color is 15 */
    /*TODO*///                if (((color & 8) && color >= overrender_data.color) || overrender_data.type == OVERRENDER_PRIORITY) {
/*TODO*///                    int data1 = READ_WORD( & atarigen_playfieldram[offs * 2]);
/*TODO*///                    int hflip = data1 & 0x8000;
/*TODO*///                    int code = data1 & 0x3fff;
/*TODO*///
/*TODO*///                    drawgfx(bitmap, gfx, code, color, hflip, 0, 8 * x, 8 * y, clip, TRANSPARENCY_NONE, 0);

    /*TODO*///                }
/*TODO*///            }
/*TODO*///        }
/*TODO*///    }
    /**
     * ***********************************
     *
     * Motion object palette
     *
     ************************************
     */
    /*TODO*///    static void mo_color_callback(
/*TODO*///            
/*TODO*///    const UINT16 *data, const struct rectangle *clip, void *param
/*TODO*///
/*TODO*///    )
/*TODO*///	{
/*TODO*///		constunsigned int *usage = Machine.gfx[1].pen_usage;
/*TODO*///        UINT16 * colormap = param;
/*TODO*///        int code = data[1] & 0x7fff;
/*TODO*///        int color = data[2] & 0x000f;
/*TODO*///        int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///        int vsize = (data[3] & 7) + 1;
/*TODO*///        int tiles = hsize * vsize;
/*TODO*///        UINT16 temp = 0;
/*TODO*///        int i;

    /*TODO*///        for (i = 0; i < tiles; i++) {
/*TODO*///            temp |= usage[code++];
/*TODO*///        }
/*TODO*///        colormap[color] |= temp;
/*TODO*///    }
    /**
     * ***********************************
     *
     * Motion object rendering
     *
     ************************************
     */
    /*TODO*///    static void mo_render_callback(
    /*TODO*///    const UINT16 *data, const struct rectangle *clip, void *param
/*TODO*///
/*TODO*///    )
/*TODO*///	{
/*TODO*///		const
/*TODO*///        struct GfxElement *gfx = Machine.gfx[1];
/*TODO*///        struct pf_overrender_data overrender_data;
/*TODO*///        struct osd_bitmap *bitmap = param;
/*TODO*///        struct rectangle pf_clip;

    /* extract data from the various words */
    /*TODO*///        int hflip = data[1] & 0x8000;
/*TODO*///        int code = data[1] & 0x7fff;
/*TODO*///        int xpos = (data[2] >> 7) - atarigen_video_control_state.sprite_xscroll;
/*TODO*///        int color = data[2] & 0x000f;
/*TODO*///        int ypos = -(data[3] >> 7) - atarigen_video_control_state.sprite_yscroll;
/*TODO*///        int hsize = ((data[3] >> 4) & 7) + 1;
/*TODO*///        int vsize = (data[3] & 7) + 1;

    /* adjust for height */
    /*TODO*///        ypos -= vsize * 8;

    /* adjust the final coordinates */
    /*TODO*///        xpos &= 0x1ff;
/*TODO*///        ypos &= 0x1ff;
/*TODO*///        if (xpos >= XDIM) {
/*TODO*///            xpos -= 0x200;
/*TODO*///        }
/*TODO*///        if (ypos >= YDIM) {
/*TODO*///            ypos -= 0x200;
/*TODO*///        }

    /* determine the bounding box */
    /*TODO*///        atarigen_mo_compute_clip_8x8(pf_clip, xpos, ypos, hsize, vsize, clip);

    /* draw the motion object */
    /*TODO*///        atarigen_mo_draw_8x8(bitmap, gfx, code, color, hflip, 0, xpos, ypos, hsize, vsize, clip, TRANSPARENCY_PEN, 0);

    /* standard priority case? */
    /*TODO*///        if (color != 15) {
            /* overrender the playfield */
    /*TODO*///            overrender_data.bitmap = bitmap;
/*TODO*///            overrender_data.type = OVERRENDER_STANDARD;
/*TODO*///            overrender_data.color = color;
/*TODO*///            atarigen_pf_process(pf_overrender_callback,  & overrender_data,  & pf_clip);
/*TODO*///        } /* high priority case? */ else {
            /* overrender the playfield */
    /*TODO*///            overrender_data.bitmap = atarigen_pf_overrender_bitmap;
/*TODO*///            overrender_data.type = OVERRENDER_PRIORITY;
/*TODO*///            overrender_data.color = color;
/*TODO*///            atarigen_pf_process(pf_overrender_callback,  & overrender_data,  & pf_clip);
/*TODO*///
            /* finally, copy this chunk to the real bitmap */
    /*TODO*///            copybitmap(bitmap, atarigen_pf_overrender_bitmap, 0, 0, 0, 0,  & pf_clip, TRANSPARENCY_THROUGH, palette_transparent_pen);
/*TODO*///        }
/*TODO*///    }
}
