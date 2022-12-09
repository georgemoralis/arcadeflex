/***************************************************************************

  vidhrdw/mcr3.c

	Functions to emulate the video hardware of an mcr3-style machine.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v036.vidhrdw;

import static gr.codebb.arcadeflex.common.SubArrays.*;
import static common.libc.cstring.*;
import static common.libc.cstdio.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.IntSubArray;
import static gr.codebb.arcadeflex.v036.platform.video.osd_get_pen;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_GFX2;
import static gr.codebb.arcadeflex.v036.mame.commonH.REGION_GFX3;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.mame.spriteC.*;
import static gr.codebb.arcadeflex.v036.mame.spriteH.*;
import static gr.codebb.arcadeflex.v037b7.machine.mcr.*;
import static arcadeflex.v036.vidhrdw.generic.*;
        
public class mcr3
{
/* These are used to align Discs of Tron with the backdrop */
    public static final int DOTRON_X_START = 144;
    public static final int DOTRON_Y_START = 40;
    public static final int DOTRON_HORIZON = 138;

    /**
     * ***********************************
     *
     * Global variables
     *
     ************************************
     */
    /* Spy Hunter hardware extras */
    public static int/*UINT8*/ u8_spyhunt_sprite_color_mask;
    public static short spyhunt_scrollx, spyhunt_scrolly;
    public static short spyhunt_scroll_offset;
    public static int/*UINT8*/ u8_spyhunt_draw_lamps;
    public static int[]/*UINT8*/ u8_spyhunt_lamp = new int[8];

    public static UBytePtr spyhunt_alpharam = new UBytePtr();
    public static int[] spyhunt_alpharam_size = new int[1];

    /**
     * ***********************************
     *
     * Local variables
     *
     ************************************
     */
    /* Spy Hunter-specific scrolling background */
    static osd_bitmap spyhunt_backbitmap;

    /* Discs of Tron artwork globals */
    public static int[][]/*UINT8*/ u8_dotron_palettes = new int[3][3 * 256];
    public static int/*UINT8*/ u8_light_status;

    public static int/*UINT8*/ u8_last_cocktail_flip;

    /**
     * ***********************************
     *
     * Palette RAM writes
     *
     ************************************
     */
    public static WriteHandlerPtr mcr3_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int r, g, b;

            paletteram.write(offset, data);
            offset &= 0x7f;

            /* high bit of red comes from low bit of address */
            r = ((offset & 1) << 2) + (data >> 6);
            g = (data >> 0) & 7;
            b = (data >> 3) & 7;

            /* up to 8 bits */
            r = (r << 5) | (r << 2) | (r >> 1);
            g = (g << 5) | (g << 2) | (g >> 1);
            b = (b << 5) | (b << 2) | (b >> 1);

            palette_change_color(offset / 2, r, g, b);
        }
    };

    /**
     * ***********************************
     *
     * Video RAM writes
     *
     ************************************
     */
    public static WriteHandlerPtr mcr3_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videoram.read(offset) != data) {
                dirtybuffer[offset & ~1] = 1;
                videoram.write(offset, data);
            }
        }
    };

    /**
     * ***********************************
     *
     * Background update
     *
     ************************************
     */
    static void mcr3_update_background(osd_bitmap bitmap, int color_xor) {
        int offs;

        /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
        for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
            if (dirtybuffer[offs] != 0) {
                int mx = (offs / 2) % 32;
                int my = (offs / 2) / 32;
                int attr = videoram.read(offs + 1);
                int color = ((attr & 0x30) >> 4) ^ color_xor;
                int code = videoram.read(offs) + 256 * (attr & 0x03);

                if (mcr_cocktail_flip == 0) {
                    drawgfx(bitmap, Machine.gfx[0], code, color, attr & 0x04, attr & 0x08,
                            16 * mx, 16 * my, Machine.visible_area, TRANSPARENCY_NONE, 0);
                } else {
                    drawgfx(bitmap, Machine.gfx[0], code, color, (attr & 0x04)!=0?0:1, (attr & 0x08)!=0?0:1,
                            16 * (31 - mx), 16 * (29 - my), Machine.visible_area, TRANSPARENCY_NONE, 0);
                }

                dirtybuffer[offs] = 0;
            }
        }
    }

    /**
     * ***********************************
     *
     * Sprite update
     *
     ************************************
     */
    public static void mcr3_update_sprites(osd_bitmap bitmap, int color_mask, int code_xor, int dx, int dy) {
        int offs;

        /* loop over sprite RAM */
        for (offs = 0; offs < spriteram_size[0]; offs += 4) {
            int code, color, flipx, flipy, sx, sy, flags;

            /* skip if zero */
            if (spriteram.read(offs) == 0) {
                continue;
            }

            /* extract the bits of information */
            flags = spriteram.read(offs + 1);
            code = spriteram.read(offs + 2) + 256 * ((flags >> 3) & 0x01);
            color = ~flags & color_mask;
            flipx = flags & 0x10;
            flipy = flags & 0x20;
            sx = (spriteram.read(offs + 3) - 3) * 2;
            sy = (241 - spriteram.read(offs)) * 2;

            code ^= code_xor;

            sx += dx;
            sy += dy;

            /* draw the sprite */
            if (mcr_cocktail_flip == 0) {
                drawgfx(bitmap, Machine.gfx[1], code, color, flipx, flipy, sx, sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            } else {
                drawgfx(bitmap, Machine.gfx[1], code, color, (flipx)!=0?0:1, (flipy)!=0?0:1, 480 - sx, 452 - sy,
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* sprites use color 0 for background pen and 8 for the 'under tile' pen.
				The color 8 is used to cover over other sprites. */
            if ((Machine.gfx[1].pen_usage[code] & 0x0100) != 0) {
                rectangle clip = new rectangle();

                clip.min_x = sx;
                clip.max_x = sx + 31;
                clip.min_y = sy;
                clip.max_y = sy + 31;

                copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, clip, TRANSPARENCY_THROUGH, Machine.pens[8 + color * 16]);
            }
        }
    }

    /**
     * ***********************************
     *
     * Generic MCR3 redraw
     *
     ************************************
     */
    public static VhUpdatePtr mcr3_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* mark everything dirty on a cocktail flip change */
            if (palette_recalc() != null || u8_last_cocktail_flip != mcr_cocktail_flip) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }
            u8_last_cocktail_flip = mcr_cocktail_flip;

            /* redraw the background */
            mcr3_update_background(tmpbitmap, 0);

            /* copy it to the destination */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            mcr3_update_sprites(bitmap, 0x03, 0, 0, 0);
        }
    };

    /**
     * ***********************************
     *
     * MCR monoboard-specific redraw
     *
     ************************************
     */
    public static VhUpdatePtr mcrmono_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* redraw the background */
            mcr3_update_background(tmpbitmap, 3);

            /* copy it to the destination */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            mcr3_update_sprites(bitmap, 0x03, 0, 0, 0);
        }
    };

    /**
     * ***********************************
     *
     * Spy Hunter-specific color PROM decoder
     *
     ************************************
     */
    public static VhConvertColorPromPtr spyhunt_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            /* add some colors for the alpha RAM */
            palette[(8 * 16) * 3 + 0] = 0;
            palette[(8 * 16) * 3 + 1] = 0;
            palette[(8 * 16) * 3 + 2] = 0;
            palette[(8 * 16 + 1) * 3 + 0] = 0;
            palette[(8 * 16 + 1) * 3 + 1] = 255;
            palette[(8 * 16 + 1) * 3 + 2] = 0;
            palette[(8 * 16 + 2) * 3 + 0] = 0;
            palette[(8 * 16 + 2) * 3 + 1] = 0;
            palette[(8 * 16 + 2) * 3 + 2] = 255;
            palette[(8 * 16 + 3) * 3 + 0] = 255;
            palette[(8 * 16 + 3) * 3 + 1] = 255;
            palette[(8 * 16 + 3) * 3 + 2] = 255;

            /* put them into the color table */
            colortable[8 * 16 + 0] = 8 * 16;
            colortable[8 * 16 + 1] = 8 * 16 + 1;
            colortable[8 * 16 + 2] = 8 * 16 + 2;
            colortable[8 * 16 + 3] = 8 * 16 + 3;
        }
    };

    /**
     * ***********************************
     *
     * Spy Hunter-specific video startup
     *
     ************************************
     */
    public static VhStartPtr spyhunt_vh_start = new VhStartPtr() {
        public int handler() {
            /* allocate our own dirty buffer */
            dirtybuffer = new char[videoram_size[0]];

            memset(dirtybuffer, 1, videoram_size[0]);

            /* allocate a bitmap for the background */
            spyhunt_backbitmap = bitmap_alloc(64 * 64, 32 * 32);
            if (spyhunt_backbitmap == null) {
                dirtybuffer = null;
                return 1;
            }

            /* reset the scrolling */
            spyhunt_scrollx = spyhunt_scrolly = 0;

            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Spy Hunter-specific video shutdown
     *
     ************************************
     */
    public static VhStopPtr spyhunt_vh_stop = new VhStopPtr() {
        public void handler() {
            /* free the buffers */
            bitmap_free(spyhunt_backbitmap);
            dirtybuffer = null;
        }
    };

    /**
     * ***********************************
     *
     * Spy Hunter-specific redraw
     *
     ************************************
     */
    static rectangle spyhunt_clip = new rectangle(0, 30 * 16 - 1, 0, 30 * 16 - 1);
    public static VhUpdatePtr spyhunt_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, scrollx, scrolly;

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int code = videoram.read(offs);
                    int vflip = code & 0x40;
                    int mx = (offs >> 4) & 0x3f;
                    int my = (offs & 0x0f) | ((offs >> 6) & 0x10);

                    code = (code & 0x3f) | ((code & 0x80) >> 1);

                    drawgfx(spyhunt_backbitmap, Machine.gfx[0], code, 0, 0, vflip,
                            64 * mx, 32 * my, null, TRANSPARENCY_NONE, 0);

                    dirtybuffer[offs] = 0;
                }
            }

            /* copy it to the destination */
            scrollx = -spyhunt_scrollx * 2 + spyhunt_scroll_offset;
            scrolly = -spyhunt_scrolly * 2;
            copyscrollbitmap(bitmap, spyhunt_backbitmap, 1, new int[]{scrollx}, 1, new int[]{scrolly}, spyhunt_clip, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            mcr3_update_sprites(bitmap, u8_spyhunt_sprite_color_mask, 0x80, -12, 0);

            /* render any characters on top */
            for (offs = spyhunt_alpharam_size[0] - 1; offs >= 0; offs--) {
                int ch = spyhunt_alpharam.read(offs);
                if (ch != 0) {
                    int mx = offs / 32;
                    int my = offs % 32;

                    drawgfx(bitmap, Machine.gfx[2], ch, 0, 0, 0,
                            16 * mx - 16, 16 * my, spyhunt_clip, TRANSPARENCY_PEN, 0);
                }
            }

            /* lamp indicators */
            if (u8_spyhunt_draw_lamps != 0) {
                String buffer;

                buffer = sprintf("%s  %s  %s  %s  %s",
                        u8_spyhunt_lamp[0] != 0 ? "OIL" : "   ",
                        u8_spyhunt_lamp[1] != 0 ? "MISSILE" : "       ",
                        u8_spyhunt_lamp[2] != 0 ? "VAN" : "   ",
                        u8_spyhunt_lamp[3] != 0 ? "SMOKE" : "     ",
                        u8_spyhunt_lamp[4] != 0 ? "GUNS" : "    ");
                for (offs = 0; offs < 30; offs++) {
                    drawgfx(bitmap, Machine.gfx[2], buffer.charAt(offs), 0, 0, 0,
                            30 * 16, (29 - offs) * 16, Machine.visible_area, TRANSPARENCY_NONE, 0);
                }
            }
        }
    };

    /**
     * ***********************************
     *
     * Discs of Tron-specific video startup
     *
     ************************************
     */
    public static VhStartPtr dotron_vh_start = new VhStartPtr() {
        public int handler() {
            int i, x, y;

            /* do generic initialization to start */
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            /*TODO*///		backdrop_load("dotron.png", 64, Machine.drv.total_colors-64);
            /* if we got it, compute palettes */
 /*TODO*///		if (artwork_backdrop != 0)
/*TODO*///		{
/*TODO*///			/* from the horizon upwards, use the second palette */
/*TODO*///			for (y = 0; y < DOTRON_HORIZON; y++)
/*TODO*///				for (x = 0; x < artwork_backdrop.artwork.width; x++)
/*TODO*///				{
/*TODO*///					int newpixel = read_pixel(artwork_backdrop.orig_artwork, x, y) + 95;
/*TODO*///					plot_pixel(artwork_backdrop.orig_artwork, x, y, newpixel);
/*TODO*///				}
/*TODO*///	
/*TODO*///			/* create palettes with different levels of brightness */
/*TODO*///			memcpy(dotron_palettes[0], artwork_backdrop.orig_palette, 3 * artwork_backdrop.num_pens_used);
/*TODO*///			for (i = 0; i < artwork_backdrop.num_pens_used; i++)
/*TODO*///			{
/*TODO*///				/* only boost red and blue */
/*TODO*///				dotron_palettes[1][i * 3 + 0] = MIN(artwork_backdrop.orig_palette[i * 3] * 2, 255);
/*TODO*///				dotron_palettes[1][i * 3 + 1] = artwork_backdrop.orig_palette[i * 3 + 1];
/*TODO*///				dotron_palettes[1][i * 3 + 2] = MIN(artwork_backdrop.orig_palette[i * 3 + 2] * 2, 255);
/*TODO*///				dotron_palettes[2][i * 3 + 0] = MIN(artwork_backdrop.orig_palette[i * 3] * 3, 255);
/*TODO*///				dotron_palettes[2][i * 3 + 1] = artwork_backdrop.orig_palette[i * 3 + 1];
/*TODO*///				dotron_palettes[2][i * 3 + 2] = MIN(artwork_backdrop.orig_palette[i * 3 + 2] * 3, 255);
/*TODO*///			}
/*TODO*///	
/*TODO*///			logerror("Backdrop loaded.\n");
/*TODO*///		}
            return 0;
        }
    };

    /**
     * ***********************************
     *
     * Discs of Tron light management
     *
     ************************************
     */
    public static void dotron_change_light(int light) {
        u8_light_status = light & 0xFF;
    }

    static void dotron_change_palette(int which) {
        /*TODO*///		UINT8 *new_palette;
/*TODO*///		int i, offset;

        /* get the palette indices */
 /*TODO*///		offset = artwork_backdrop.start_pen + 95;
/*TODO*///		new_palette = dotron_palettes[which];
        /* update the palette entries */
 /*TODO*///		for (i = 0; i < artwork_backdrop.num_pens_used; i++)
/*TODO*///			palette_change_color(i + offset, new_palette[i * 3], new_palette[i * 3 + 1], new_palette[i * 3 + 2]);
    }

    /**
     * ***********************************
     *
     * Discs of Tron-specific redraw
     *
     ************************************
     */
    public static VhUpdatePtr dotron_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            rectangle sclip = new rectangle();
            int offs;

            /* handle background lights */
 /*TODO*///		if (artwork_backdrop != NULL)
/*TODO*///		{
/*TODO*///			int light = light_status & 1;
/*TODO*///			if ((light_status & 2) && (cpu_getcurrentframe() & 1)) light++;	/* strobe */
/*TODO*///			dotron_change_palette(light);
/*TODO*///		}
            if (full_refresh != 0 || palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
            }

            /* Screen clip, because our backdrop is a different resolution than the game */
            sclip.min_x = DOTRON_X_START + 0;
            sclip.max_x = DOTRON_X_START + 32 * 16 - 1;
            sclip.min_y = DOTRON_Y_START + 0;
            sclip.max_y = DOTRON_Y_START + 30 * 16 - 1;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer[offs] != 0) {
                    int attr = videoram.read(offs + 1);
                    int code = videoram.read(offs) + 256 * (attr & 0x03);
                    int color = (attr & 0x30) >> 4;
                    int mx = ((offs / 2) % 32) * 16;
                    int my = ((offs / 2) / 32) * 16;

                    /* center for the backdrop */
                    mx += DOTRON_X_START;
                    my += DOTRON_Y_START;

                    drawgfx(tmpbitmap, Machine.gfx[0], code, color, attr & 0x04, attr & 0x08,
                            mx, my, sclip, TRANSPARENCY_NONE, 0);

                    dirtybuffer[offs] = 0;
                }
            }

            /* copy the resulting bitmap to the screen */
            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, null, TRANSPARENCY_NONE, 0);

            /* draw the sprites */
            mcr3_update_sprites(bitmap, 0x03, 0, DOTRON_X_START, DOTRON_Y_START);
        }
    };
	
}
