/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
/**
 * Changelog
 * =========
 * 07/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.paletteH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.platform.video.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class sidearms {

    public static UBytePtr sidearms_bg_scrollx = new UBytePtr();
    public static UBytePtr sidearms_bg_scrolly = new UBytePtr();
    public static UBytePtr sidearms_bg2_scrollx = new UBytePtr();
    public static UBytePtr sidearms_bg2_scrolly = new UBytePtr();
    public static osd_bitmap tmpbitmap2;
    public static int flipscreen;
    public static int bgon, objon;

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartHandlerPtr sidearms_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            /* create a temporary bitmap slightly larger than the screen for the background */
            if ((tmpbitmap2 = osd_create_bitmap(48 * 8 + 32, Machine.drv.screen_height + 32)) == null) {
                generic_vh_stop.handler();
                return 1;
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
    public static VhStopHandlerPtr sidearms_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap2);
            generic_vh_stop.handler();
        }
    };

    public static WriteHandlerPtr sidearms_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0 and 1 are coin counters */
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);

            /* bit 4 probably resets the sound CPU */
 /* TODO: I don't know about the other bits (all used) */
 /* bit 7 flips screen */
            if (flipscreen != (data & 0x80)) {
                flipscreen = data & 0x80;
                /* TODO: support screen flip */
                //		memset(dirtybuffer,1,c1942_backgroundram_size);
            }
        }
    };

    public static WriteHandlerPtr sidearms_gfxctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            objon = data & 0x01;
            bgon = data & 0x02;
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
    static int lastoffs;
    public static VhUpdateHandlerPtr sidearms_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;
            int scrollx, scrolly;

            int dirtypalette = 0;

            palette_init_used_colors();

            {
                int color, code, i;
                int[] colmask = new int[64];
                int pal_base;
                UBytePtr p = memory_region(REGION_GFX4);

                pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;

                for (color = 0; color < 32; color++) {
                    colmask[color] = 0;
                }

                scrollx = sidearms_bg_scrollx.read(0) + 256 * sidearms_bg_scrollx.read(1) + 64;
                scrolly = sidearms_bg_scrolly.read(0) + 256 * sidearms_bg_scrolly.read(1);
                offs = 2 * (scrollx >> 5) + 0x100 * (scrolly >> 5);
                scrollx = -(scrollx & 0x1f);
                scrolly = -(scrolly & 0x1f);

                for (sy = 0; sy < 9; sy++) {
                    for (sx = 0; sx < 13; sx++) {
                        int offset;

                        offset = offs + 2 * sx;

                        /* swap bits 1-7 and 8-10 of the address to compensate for the */
 /* funny layout of the ROM data */
                        offset = (offset & 0xf801) | ((offset & 0x0700) >> 7) | ((offset & 0x00fe) << 3);

                        code = p.read(offset) + 256 * (p.read(offset + 1) & 0x01);
                        color = (p.read(offset + 1) & 0xf8) >> 3;
                        colmask[color] |= Machine.gfx[1].pen_usage[code];
                    }
                    offs += 0x100;
                }

                for (color = 0; color < 32; color++) {
                    if ((colmask[color] & (1 << 15)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 0; i < 15; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                    code = spriteram.read(offs) + 8 * (spriteram.read(offs + 1) & 0xe0);
                    color = spriteram.read(offs + 1) & 0x0f;
                    colmask[color] |= Machine.gfx[2].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 15)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color + 15, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 0; i < 15; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;

                for (color = 0; color < 64; color++) {
                    colmask[color] = 0;
                }

                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    code = videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0);
                    color = colorram.read(offs) & 0x3f;
                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (color = 0; color < 64; color++) {
                    if ((colmask[color] & (1 << 3)) != 0) {
                        palette_used_colors.write(pal_base + 4 * color + 3, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 0; i < 3; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 4 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }
            }

            if (palette_recalc() != null) {
                dirtypalette = 1;
            }

            /* There is a scrolling blinking star background behind the tile */
 /* background, but I have absolutely NO IDEA how to render it. */
 /* The scroll registers have a 64 pixels resolution. */
//	#if IHAVETHEBACKGROUND
//		{
//			int x,y;
//			for (x = 0;x < 48;x+=8)
//			{
//				for (y = 0;y < 32;y+=8)
//				{
//					drawgfx(tmpbitmap,Machine.gfx[0],
//							(y%8)*48+(x%8),
//							0,
//							0,0,
//							8*x,8*y,
//							0,TRANSPARENCY_NONE,0);
//				}
//			}
//		}
//	
            /* copy the temporary bitmap to the screen */
//		scrollx = -(*sidearms_bg2_scrollx & 0x3f);
//		scrolly = -(*sidearms_bg2_scrolly & 0x3f);
            //		copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_NONE,0);
//	#endif
            if (bgon != 0) {
                scrollx = sidearms_bg_scrollx.read(0) + 256 * sidearms_bg_scrollx.read(1) + 64;
                scrolly = sidearms_bg_scrolly.read(0) + 256 * sidearms_bg_scrolly.read(1);
                offs = 2 * (scrollx >> 5) + 0x100 * (scrolly >> 5);
                scrollx = -(scrollx & 0x1f);
                scrolly = -(scrolly & 0x1f);

                if (offs != lastoffs || dirtypalette != 0) {
                    UBytePtr p = memory_region(REGION_GFX4);

                    lastoffs = offs;

                    /* Draw the entire background scroll */
                    for (sy = 0; sy < 9; sy++) {
                        for (sx = 0; sx < 13; sx++) {
                            int offset;

                            offset = offs + 2 * sx;

                            /* swap bits 1-7 and 8-10 of the address to compensate for the */
 /* funny layout of the ROM data */
                            offset = (offset & 0xf801) | ((offset & 0x0700) >> 7) | ((offset & 0x00fe) << 3);

                            drawgfx(tmpbitmap2, Machine.gfx[1],
                                    p.read(offset) + 256 * (p.read(offset + 1) & 0x01),
                                    (p.read(offset + 1) & 0xf8) >> 3,
                                    p.read(offset + 1) & 0x02, p.read(offset + 1) & 0x04,
                                    32 * sx, 32 * sy,
                                    null, TRANSPARENCY_NONE, 0);
                        }
                        offs += 0x100;
                    }
                }

                scrollx += 64;
                //#if IHAVETHEBACKGROUND
                //copyscrollbitmap(bitmap,tmpbitmap2,1,&scrollx,1,&scrolly,&Machine.drv.visible_area,TRANSPARENCY_COLOR,1);
                //#else
                copyscrollbitmap(bitmap, tmpbitmap2, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
                //#endif
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.drv.visible_area);
            }

            /* Draw the sprites. */
            if (objon != 0) {
                for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                    sx = spriteram.read(offs + 3) + ((spriteram.read(offs + 1) & 0x10) << 4);
                    sy = spriteram.read(offs + 2);
                    if (flipscreen != 0) {
                        sx = 496 - sx;
                        sy = 240 - sy;
                    }

                    drawgfx(bitmap, Machine.gfx[2],
                            spriteram.read(offs) + 8 * (spriteram.read(offs + 1) & 0xe0),
                            spriteram.read(offs + 1) & 0x0f,
                            flipscreen, flipscreen,
                            sx, sy,
                            Machine.drv.visible_area, TRANSPARENCY_PEN, 15);
                }
            }

            /* draw the frontmost playfield. They are characters, but draw them as sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                sx = offs % 64;
                sy = offs / 64;

                if (flipscreen != 0) {
                    sx = 63 - sx;
                    sy = 31 - sy;
                }

                drawgfx(bitmap, Machine.gfx[0],
                        videoram.read(offs) + 4 * (colorram.read(offs) & 0xc0),
                        colorram.read(offs) & 0x3f,
                        flipscreen, flipscreen,
                        8 * sx, 8 * sy,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 3);
            }
        }
    };
}
