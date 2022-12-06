/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.vidhrdw;
import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.expressions.NOT;

public class lwings {

    static int trojan_vh_type;
    public static UBytePtr lwings_backgroundram = new UBytePtr();
    public static UBytePtr lwings_backgroundattribram = new UBytePtr();
    public static int[] lwings_backgroundram_size = new int[1];
    public static UBytePtr lwings_scrolly = new UBytePtr();
    public static UBytePtr lwings_scrollx = new UBytePtr();

    public static UBytePtr trojan_scrolly = new UBytePtr();
    public static UBytePtr trojan_scrollx = new UBytePtr();
    public static UBytePtr trojan_bk_scrolly = new UBytePtr();
    public static UBytePtr trojan_bk_scrollx = new UBytePtr();

    static char[] dirtybuffer2;
    static char[] dirtybuffer4;
    static osd_bitmap tmpbitmap2;
    static osd_bitmap tmpbitmap3;

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
       // 	#define COLORTABLE_START(gfxn,color_code) Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + \
    //				color_code * Machine.gfx[gfxn].color_granularity
    //#define GFX_COLOR_CODES(gfxn) Machine.gfx[gfxn].total_colors
    //#define GFX_ELEM_COLORS(gfxn) Machine.gfx[gfxn].color_granularity
    static int COLORTABLE_START(int gfxn, int color_code) {
        return Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + color_code * Machine.gfx[gfxn].color_granularity;
    }

    static int GFX_COLOR_CODES(int gfxn) {
        return Machine.gfx[gfxn].total_colors;
    }

    static int GFX_ELEM_COLORS(int gfxn) {
        return Machine.gfx[gfxn].color_granularity;
    }
    public static VhStartPtr lwings_vh_start = new VhStartPtr() {
        public int handler() {
            int i;

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[lwings_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, lwings_backgroundram_size[0]);

            if ((dirtybuffer4 = new char[lwings_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer4, 1, lwings_backgroundram_size[0]);

            /* the background area is twice as tall as the screen */
            if ((tmpbitmap2 = osd_new_bitmap(2 * Machine.drv.screen_width,
                    2 * Machine.drv.screen_height, Machine.scrbitmap.depth)) == null) {
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;
            }

            palette_init_used_colors();
            /* chars */
            for (i = 0; i < GFX_COLOR_CODES(0); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(0,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(0));*/
                for (int x = 0; x < GFX_ELEM_COLORS(0); x++) {
                    palette_used_colors.write(COLORTABLE_START(0, i) + x, PALETTE_COLOR_USED);
                }
                palette_used_colors.write(COLORTABLE_START(0, i) + GFX_ELEM_COLORS(0) - 1, PALETTE_COLOR_TRANSPARENT);
            }
            /* bg tiles */
            for (i = 0; i < GFX_COLOR_CODES(1); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(1,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(1));*/
                for (int x = 0; x < GFX_ELEM_COLORS(1); x++) {
                    palette_used_colors.write(COLORTABLE_START(1, i) + x, PALETTE_COLOR_USED);
                }
            }
            /* sprites */
            for (i = 0; i < GFX_COLOR_CODES(2); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(2,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(2));*/
                for (int x = 0; x < GFX_ELEM_COLORS(2); x++) {
                    palette_used_colors.write(COLORTABLE_START(2, i) + x, PALETTE_COLOR_USED);
                }
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr lwings_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            dirtybuffer2 = null;
            dirtybuffer4 = null;
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr lwings_background_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (lwings_backgroundram.read(offset) != data) {
                lwings_backgroundram.write(offset, data);
                dirtybuffer2[offset] = 1;
            }
        }
    };

    public static WriteHandlerPtr lwings_backgroundattrib_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (lwings_backgroundattribram.read(offset) != data) {
                lwings_backgroundattribram.write(offset, data);
                dirtybuffer4[offset] = 1;
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
    public static VhUpdatePtr lwings_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            if (palette_recalc() != null) {
                memset(dirtybuffer2, 1, lwings_backgroundram_size[0]);
                memset(dirtybuffer4, 1, lwings_backgroundram_size[0]);
            }

            for (offs = lwings_backgroundram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy, colour;
                /*
                 Tiles
                 =====
                 0x80 Tile code MSB
                 0x40 Tile code MSB
                 0x20 Tile code MSB
                 0x10 X flip
                 0x08 Y flip
                 0x04 Colour
                 0x02 Colour
                 0x01 Colour
                 */

                colour = (lwings_backgroundattribram.read(offs) & 0x07);
                if (dirtybuffer2[offs] != 0 || dirtybuffer4[offs] != 0) {
                    int code;
                    dirtybuffer2[offs] = dirtybuffer4[offs] = 0;

                    sx = offs / 32;
                    sy = offs % 32;
                    code = lwings_backgroundram.read(offs);
                    code += ((((int) lwings_backgroundattribram.read(offs)) & 0xe0) << 3);

                    drawgfx(tmpbitmap2, Machine.gfx[1],
                            code,
                            colour,
                            (lwings_backgroundattribram.read(offs) & 0x08),
                            (lwings_backgroundattribram.read(offs) & 0x10),
                            16 * sx, 16 * sy,
                            null, TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the background graphics */
            {
                int scrollx, scrolly;

                scrolly = -(lwings_scrollx.read(0) + 256 * lwings_scrollx.read(1));
                scrollx = -(lwings_scrolly.read(0) + 256 * lwings_scrolly.read(1));

                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int code, sx, sy;

                /*
                 Sprites
                 =======
                 0x80 Sprite code MSB
                 0x40 Sprite code MSB
                 0x20 Colour
                 0x10 Colour
                 0x08 Colour
                 0x04 Y flip
                 0x02 X flip
                 0x01 X MSB
                 */
                sx = spriteram.read(offs + 3) - 0x100 * (spriteram.read(offs + 1) & 0x01);
                sy = spriteram.read(offs + 2);
                if (sx != 0 && sy != 0) {
                    code = spriteram.read(offs);
                    code += (spriteram.read(offs + 1) & 0xc0) << 2;

                    drawgfx(bitmap, Machine.gfx[2],
                            code,
                            (spriteram.read(offs + 1) & 0x38) >> 3,
                            spriteram.read(offs + 1) & 0x02, spriteram.read(offs + 1) & 0x04,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                sx = offs % 32;
                sy = offs / 32;

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0),
                        colorram.read(offs) & 0x0f,
                        colorram.read(offs) & 0x10, colorram.read(offs) & 0x20,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };

    /*
     TROJAN
     ======
	
     Differences:
	
     Tile attribute (no y flip, possible priority)
     Sprite attribute (more sprites)
     Extra scroll layer
	
     */
    public static VhStartPtr trojan_vh_start = new VhStartPtr() {
        public int handler() {
            int i;
            trojan_vh_type = 0;

            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((dirtybuffer2 = new char[lwings_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer2, 1, lwings_backgroundram_size[0]);

            if ((dirtybuffer4 = new char[lwings_backgroundram_size[0]]) == null) {
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer4, 1, lwings_backgroundram_size[0]);

            if ((tmpbitmap3 = osd_new_bitmap(16 * 0x12,
                    16 * 0x12, Machine.scrbitmap.depth)) == null) {
                dirtybuffer4 = null;
                dirtybuffer2 = null;
                generic_vh_stop.handler();
                return 1;

            }

            palette_init_used_colors();
            /* chars */
            for (i = 0; i < GFX_COLOR_CODES(0); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(0,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(0));*/
                for (int x = 0; x < GFX_ELEM_COLORS(0); x++) {
                    palette_used_colors.write(COLORTABLE_START(0, i) + x, PALETTE_COLOR_USED);
                }
                palette_used_colors.write(COLORTABLE_START(0, i) + GFX_ELEM_COLORS(0) - 1, PALETTE_COLOR_TRANSPARENT);
            }
            /* fg tiles */
            for (i = 0; i < GFX_COLOR_CODES(1); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(1,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(1));*/

                for (int x = 0; x < GFX_ELEM_COLORS(1); x++) {
                    palette_used_colors.write(COLORTABLE_START(1, i) + x, PALETTE_COLOR_USED);
                }
            }
            /* sprites */
            for (i = 0; i < GFX_COLOR_CODES(2); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(2,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(2));*/
                for (int x = 0; x < GFX_ELEM_COLORS(2); x++) {
                    palette_used_colors.write(COLORTABLE_START(2, i) + x, PALETTE_COLOR_USED);
                }
            }
            /* bg tiles */
            for (i = 0; i < GFX_COLOR_CODES(3); i++) {
                /*memset(&palette_used_colors[COLORTABLE_START(3,i)],
                 PALETTE_COLOR_USED,
                 GFX_ELEM_COLORS(3));*/
                for (int x = 0; x < GFX_ELEM_COLORS(3); x++) {
                    palette_used_colors.write(COLORTABLE_START(3, i) + x, PALETTE_COLOR_USED);
                }
            }

            return 0;
        }
    };
    public static VhStartPtr avengers_vh_start = new VhStartPtr() {
        public int handler() {
            int result = trojan_vh_start.handler();
            trojan_vh_type = 1;
            return result;
        }
    };

    public static VhStopPtr trojan_vh_stop = new VhStopPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap3);
            dirtybuffer4 = null;
            dirtybuffer2 = null;
            generic_vh_stop.handler();
        }
    };

    static void trojan_render_foreground(osd_bitmap bitmap, int scrollx, int scrolly, int priority) {
        int scrlx = -(scrollx & 0x0f);
        int scrly = -(scrolly & 0x0f);
        int sx, sy;
        int offsy = (scrolly >> 4) - 1;
        int offsx = (scrollx >> 4) * 32 - 32;

        int transp0, transp1;
        if (priority != 0) {
            transp0 = 0xFFFF;	/* draw nothing (all pens transparent) */

            transp1 = 0xF00F;	/* high priority half of tile */

        } else {
            transp0 = 1;		/* TRANSPARENCY_PEN, color 0 */

            transp1 = 0x0FF0;	/* low priority half of tile */

        }

        for (sx = 0; sx < 0x12; sx++) {
            offsx &= 0x03ff;
            for (sy = 0; sy < 0x12; sy++) {
                /*
                 Tiles
                 0x80 Tile code MSB
                 0x40 Tile code MSB
                 0x20 Tile code MSB
                 0x10 X flip
                 0x08 Priority ????
                 0x04 Colour
                 0x02 Colour
                 0x01 Colour
                 */
                int offset = offsx + ((sy + offsy) & 0x1f);
                int attribute = lwings_backgroundattribram.read(offset);
                drawgfx(bitmap, Machine.gfx[1],
                        lwings_backgroundram.read(offset) + ((attribute & 0xe0) << 3),
                        attribute & 0x07,
                        attribute & 0x10,
                        0,
                        16 * sx + scrlx - 16, 16 * sy + scrly - 16, Machine.drv.visible_area, TRANSPARENCY_PENS, (attribute & 0x08) != 0 ? transp1 : transp0);
            }
            offsx += 0x20;
        }
    }

    static void trojan_draw_sprites(osd_bitmap bitmap) {
        rectangle clip = Machine.drv.visible_area;
        int offs;

        for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
            int code = spriteram.read(offs);
            int attrib = spriteram.read(offs + 1);
            /*
             0x80 Sprite code MSB
             0x40 Sprite code MSB
             0x20 Sprite code MSB
             0x10 X flip
             0x08 colour
             0x04 colour
             0x02 colour
             0x01 X MSB
             */
            int sy = spriteram.read(offs + 2);
            int sx = spriteram.read(offs + 3) - 0x100 * (attrib & 0x01);
            if (sx != 0 && sy != 0) {
                int flipx = attrib & 0x10;
                int flipy = 1;

                if (trojan_vh_type != 0) { /* avengers */

                    flipy = NOT(flipx);
                    flipx = 0;
                }

                if ((attrib & 0x40) != 0) {
                    code += 256;
                }
                if ((attrib & 0x80) != 0) {
                    code += 256 * 4;
                }
                if ((attrib & 0x20) != 0) {
                    code += 256 * 2;
                }

                drawgfx(bitmap, Machine.gfx[2],
                        code,
                        (attrib & 0x0e) >> 1, /* color */
                        flipx, flipy,
                        sx, sy,
                        clip, TRANSPARENCY_PEN, 15);
            }
        }
    }
    static int oldoffsy = 0xffff;
    static int oldoffsx = 0xffff;
    public static VhUpdatePtr trojan_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy, scrollx, scrolly;
            int offsy, offsx;

            if (palette_recalc() != null) {
                memset(dirtybuffer2, 1, lwings_backgroundram_size[0]);
                memset(dirtybuffer4, 1, lwings_backgroundram_size[0]);
            }

            {
                scrollx = (trojan_bk_scrollx.read(0));
                scrolly = (trojan_bk_scrolly.read(0));

                offsy = 0x20 * scrolly;
                offsx = (scrollx >> 4);
                scrollx = -(scrollx & 0x0f);
                scrolly = 0; /* Y doesn't scroll ??? */

                if (oldoffsy != offsy || oldoffsx != offsx) {
                    UBytePtr p = memory_region(REGION_GFX5);
                    oldoffsx = offsx;
                    oldoffsy = offsy;

                    for (sy = 0; sy < 0x11; sy++) {
                        offsy &= 0x7fff;
                        for (sx = 0; sx < 0x11; sx++) {
                            int code, colour;
                            int offset = offsy + ((2 * (offsx + sx)) & 0x3f);
                            code = p.read(offset & 0xff);//*(p+offset);
                            colour = p.read((offset + 1) & 0xff);//*(p+offset+1);
                            drawgfx(tmpbitmap3, Machine.gfx[3],
                                    code + ((colour & 0x80) << 1),
                                    colour & 0x07,
                                    colour & 0x10,
                                    colour & 0x20,
                                    16 * sx, 16 * sy,
                                    null, TRANSPARENCY_NONE, 0);
                        }
                        offsy += 0x800;
                    }
                }
                copyscrollbitmap(bitmap, tmpbitmap3, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            scrollx = (trojan_scrollx.read(0) + 256 * trojan_scrollx.read(1));
            scrolly = (trojan_scrolly.read(0) + 256 * trojan_scrolly.read(1));

            trojan_render_foreground(bitmap, scrollx, scrolly, 0);

            trojan_draw_sprites(bitmap);
            trojan_render_foreground(bitmap, scrollx, scrolly, 1);

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                sx = offs % 32;
                sy = offs / 32;
                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0),
                        colorram.read(offs) & 0x0f,
                        colorram.read(offs) & 0x10, colorram.read(offs) & 0x20,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };
}
