package gr.codebb.arcadeflex.v036.vidhrdw;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v037b7.drivers.neogeo.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static common.libc.cstring.*;

public class neogeo {

    static UBytePtr vidram;
    static UBytePtr neogeo_paletteram;
    /* pointer to 1 of the 2 palette banks */

    static UBytePtr pal_bank1;
    /* 0x100*16 2 byte palette entries */

    static UBytePtr pal_bank2;
    /* 0x100*16 2 byte palette entries */

    static int palno, modulo, where, high_tile, vhigh_tile, vvhigh_tile;
    static int no_of_tiles;
    static int palette_swap_pending, fix_bank;
    /*TODO*///
/*TODO*///extern unsigned char *neogeo_ram;
/*TODO*///extern unsigned int neogeo_frame_counter;
/*TODO*///extern int neogeo_game_fix;
/*TODO*///
/*TODO*///void NeoMVSDrawGfx(unsigned char **line,const struct GfxElement *gfx,
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		int zx,int zy,const struct rectangle *clip);
/*TODO*///
    static char[] dda_x_skip = new char[16];
    static char[] dda_y_skip = new char[17];
    static char full_y_skip[] = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    /*TODO*///
/*TODO*///
/*TODO*////******************************************************************************/
/*TODO*///
    public static VhStopHandlerPtr neogeo_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            /*TODO*///   	if (pal_bank1) free (pal_bank1);
/*TODO*///	if (pal_bank2) free (pal_bank2);
/*TODO*///	if (vidram) free (vidram);
/*TODO*///	if (neogeo_ram) free (neogeo_ram);
/*TODO*///
/*TODO*///	pal_bank1=pal_bank2=vidram=neogeo_ram=0;
        }
    };
    public static VhStartHandlerPtr common_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            pal_bank1 = null;
            pal_bank2 = null;
            vidram = null;

            pal_bank1 = new UBytePtr(0x2000);
            if (pal_bank1 == null) {
                neogeo_vh_stop.handler();
                return 1;
            }

            pal_bank2 = new UBytePtr(0x2000);
            if (pal_bank2 == null) {
                neogeo_vh_stop.handler();
                return 1;
            }

            vidram = new UBytePtr(0x20000);
            /* 0x20000 bytes even though only 0x10c00 is used */

            if (vidram == null) {
                neogeo_vh_stop.handler();
                return 1;
            }
            memset(vidram, 0, 0x20000);

            neogeo_paletteram = pal_bank1;
            palno = 0;
            modulo = 1;
            where = 0;
            fix_bank = 0;
            palette_swap_pending = 0;

            return 0;
        }
    };

    static void decodetile(int tileno) {
        /*unsigned*/ char[] swap = new char[128];
        IntPtr gfxdata;
        int x, y;
        /*unsigned*/ int pen;

        gfxdata = new IntPtr(memory_region(REGION_GFX2), 128 * tileno);

        memcpy(swap, gfxdata, 128);

        for (y = 0; y < 16; y++) {
            int/*UINT32*/ dw;

            dw = 0;
            for (x = 0; x < 8; x++) {
                pen = ((swap[64 + 4 * y + 3] >> x) & 1) << 3;
                pen |= ((swap[64 + 4 * y + 1] >> x) & 1) << 2;
                pen |= ((swap[64 + 4 * y + 2] >> x) & 1) << 1;
                pen |= (swap[64 + 4 * y] >> x) & 1;
                dw |= pen << 4 * (7 - x);
                Machine.gfx[2].pen_usage[tileno] |= (1 << pen);
            }
            //*(gfxdata++) = dw;
            gfxdata.write(dw);
            gfxdata.inc();

            dw = 0;
            for (x = 0; x < 8; x++) {
                pen = ((swap[4 * y + 3] >> x) & 1) << 3;
                pen |= ((swap[4 * y + 1] >> x) & 1) << 2;
                pen |= ((swap[4 * y + 2] >> x) & 1) << 1;
                pen |= (swap[4 * y] >> x) & 1;
                dw |= pen << 4 * (7 - x);
                Machine.gfx[2].pen_usage[tileno] |= (1 << pen);
            }
            //*(gfxdata++) = dw;
            gfxdata.write(dw);
            gfxdata.inc();
        }
    }
    public static VhStartHandlerPtr neogeo_mvs_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            no_of_tiles = memory_region_length(REGION_GFX2) / 128;
            if (no_of_tiles > 0x10000) {
                high_tile = 1;
            } else {
                high_tile = 0;
            }
            if (no_of_tiles > 0x20000) {
                vhigh_tile = 1;
            } else {
                vhigh_tile = 0;
            }
            if (no_of_tiles > 0x40000) {
                vvhigh_tile = 1;
            } else {
                vvhigh_tile = 0;
            }
            Machine.gfx[2].total_elements = no_of_tiles;
            if (Machine.gfx[2].pen_usage != null) {
                Machine.gfx[2].pen_usage = null;
            }
            Machine.gfx[2].pen_usage = new int[no_of_tiles * 4];
            memset(Machine.gfx[2].pen_usage, 0, no_of_tiles * 4);

            /* tiles are not decoded yet. They will be decoded later as they are used. */
 /* pen_usage is used as a marker of decoded tiles: if it is 0, then the tile */
 /* hasn't been decoded yet. */
            return common_vh_start.handler();
        }
    };

    /**
     * ***************************************************************************
     */
    static void swap_palettes() {
        int i, newword;

        for (i = 0; i < 0x2000; i += 2) {
            int r, g, b;

            newword = neogeo_paletteram.READ_WORD(i);

            r = ((newword >> 7) & 0x1e) | ((newword >> 14) & 0x01);
            g = ((newword >> 3) & 0x1e) | ((newword >> 13) & 0x01);
            b = ((newword << 1) & 0x1e) | ((newword >> 12) & 0x01);

            r = (r << 3) | (r >> 2);
            g = (g << 3) | (g >> 2);
            b = (b << 3) | (b >> 2);

            palette_change_color(i / 2, r, g, b);
        }

        palette_swap_pending = 0;
    }

    public static WriteHandlerPtr neogeo_setpalbank0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palno != 0) {
                palno = 0;
                neogeo_paletteram = pal_bank1;
                palette_swap_pending = 1;
            }
        }
    };
    public static WriteHandlerPtr neogeo_setpalbank1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (palno != 1) {
                palno = 1;
                neogeo_paletteram = pal_bank2;
                palette_swap_pending = 1;
            }
        }
    };
    public static ReadHandlerPtr neogeo_paletteram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return neogeo_paletteram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr neogeo_paletteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int oldword = neogeo_paletteram.READ_WORD(offset);
            int newword = COMBINE_WORD(oldword, data);
            int r, g, b;

            neogeo_paletteram.WRITE_WORD(offset, newword);

            r = ((newword >> 7) & 0x1e) | ((newword >> 14) & 0x01);
            g = ((newword >> 3) & 0x1e) | ((newword >> 13) & 0x01);
            b = ((newword << 1) & 0x1e) | ((newword >> 12) & 0x01);

            r = (r << 3) | (r >> 2);
            g = (g << 3) | (g >> 2);
            b = (b << 3) | (b >> 2);

            palette_change_color(offset / 2, r, g, b);
        }
    };

    /**
     * ***************************************************************************
     */
    static UBytePtr neogeo_palette(rectangle clip) {
        int color, code, pal_base, y, my = 0, count, offs, i;
        int[] colmask = new int[256];
        int[] pen_usage;
        /* Save some struct derefs */

        int sx = 0, sy = 0, oy = 0, zx = 1, rzy = 1;
        int tileno, tileatr, t1, t2, t3;
        char fullmode = 0;
        int ddax = 0, dday = 0, rzx = 15, yskip = 0;

        if (Machine.scrbitmap.depth == 16) {
            return palette_recalc();
        }

        palette_init_used_colors();

        /* character foreground */
        pen_usage = Machine.gfx[fix_bank].pen_usage;
        pal_base = Machine.drv.gfxdecodeinfo[fix_bank].color_codes_start;
        for (color = 0; color < 16; color++) {
            colmask[color] = 0;
        }
        for (offs = 0xe000; offs < 0xea00; offs += 2) {
            code = vidram.READ_WORD(offs);
            color = code >> 12;
            colmask[color] |= pen_usage[code & 0xfff];
        }
        for (color = 0; color < 16; color++) {
            for (i = 1; i < 16; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_VISIBLE);
                }
            }
        }

        /* Tiles */
        pen_usage = Machine.gfx[2].pen_usage;
        pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;
        for (color = 0; color < 256; color++) {
            colmask[color] = 0;
        }
        for (count = 0; count < 0x300; count += 2) {
            t3 = vidram.READ_WORD(0x10000 + count);
            t1 = vidram.READ_WORD(0x10400 + count);
            t2 = vidram.READ_WORD(0x10800 + count);

            /* If this bit is set this new column is placed next to last one */
            if ((t1 & 0x40) != 0) {
                sx += rzx;
                if (sx >= 0x1F0) {
                    sx -= 0x200;
                }

                /* Get new zoom for this column */
                zx = (t3 >> 8) & 0x0f;
                sy = oy;
            } else {
                /* nope it is a new block */
 /* Sprite scaling */

                zx = (t3 >> 8) & 0x0f;
                rzy = t3 & 0xff;

                sx = (t2 >> 7);
                if (sx >= 0x1F0) {
                    sx -= 0x200;
                }

                /* Number of tiles in this strip */
                my = t1 & 0x3f;
                if (my == 0x20) {
                    fullmode = 1;
                } else if (my >= 0x21) {
                    fullmode = 2;
                    /* most games use 0x21, but */
                } /* Alpha Mission II uses 0x3f */ else {
                    fullmode = 0;
                }

                sy = 0x200 - (t1 >> 7);
                if (clip.max_y - clip.min_y > 8
                        || /* kludge to improve the ssideki games */ clip.min_y == Machine.drv.visible_area.min_y) {
                    if (sy > 0x110) {
                        sy -= 0x200;
                    }
                    if (fullmode == 2 || (fullmode == 1 && rzy == 0xff)) {
                        while (sy < 0) {
                            sy += 2 * (rzy + 1);
                        }
                    }
                }
                oy = sy;

                if (rzy < 0xff && my < 0x10 && my != 0) {
                    my = (my * 256) / (rzy + 1);
                    if (my > 0x10) {
                        my = 0x10;
                    }
                }
                if (my > 0x20) {
                    my = 0x20;
                }

                ddax = 0;
                /* =16; NS990110 neodrift fix */ /* setup x zoom */


            }

            /* No point doing anything if tile strip is 0 */
            if (my == 0) {
                continue;
            }

            /* Process x zoom */
            if (zx != 15) {
                rzx = 0;
                for (i = 0; i < 16; i++) {
                    ddax -= zx + 1;
                    if (ddax <= 0) {
                        ddax += 15 + 1;
                        rzx++;
                    }
                }
            } else {
                rzx = 16;
            }

            if (sx >= 320) {
                continue;
            }

            /* Setup y zoom */
            if (rzy == 255) {
                yskip = 16;
            } else {
                dday = 0;
                /* =256; NS990105 mslug fix */
            }

            offs = count << 6;

            /* my holds the number of tiles in each vertical multisprite block */
            for (y = 0; y < my; y++) {
                tileno = vidram.READ_WORD(offs);
                offs += 2;
                tileatr = vidram.READ_WORD(offs);
                offs += 2;

                if ((high_tile != 0 && (tileatr & 0x10) != 0)) {
                    tileno += 0x10000;
                }
                if ((vhigh_tile != 0 && (tileatr & 0x20) != 0)) {
                    tileno += 0x20000;
                }
                if ((vvhigh_tile != 0 && (tileatr & 0x40) != 0)) {
                    tileno += 0x40000;
                }

                if ((tileatr & 0x8) != 0) {
                    tileno = (tileno & ~7) + ((tileno + neogeo_frame_counter) & 7);
                } else if ((tileatr & 0x4) != 0) {
                    tileno = (tileno & ~3) + ((tileno + neogeo_frame_counter) & 3);
                }

                if (fullmode == 2 || (fullmode == 1 && rzy == 0xff)) {
                    if (sy >= 248) {
                        sy -= 2 * (rzy + 1);
                    }
                } else if (fullmode == 1) {
                    if (y == 0x10) {
                        sy -= 2 * (rzy + 1);
                    }
                } else if (sy > 0x110) {
                    sy -= 0x200;
                    /* NS990105 mslug2 fix */
                }

                if (rzy != 255) {
                    yskip = 0;
                    for (i = 0; i < 16; i++) {
                        dday -= rzy + 1;
                        if (dday <= 0) {
                            dday += 256;
                            yskip++;
                        }
                    }
                }

                if (sy + yskip - 1 >= clip.min_y && sy <= clip.max_y) {
                    tileatr = tileatr >> 8;
                    tileno %= no_of_tiles;
                    if (pen_usage[tileno] == 0) /* decode tile if it hasn't been yet */ {
                        decodetile(tileno);
                    }
                    colmask[tileatr] |= pen_usage[tileno];
                }

                sy += yskip;

            }
            /* for y */

        }
        /* for count */

        for (color = 0; color < 256; color++) {
            for (i = 1; i < 16; i++) {
                if ((colmask[color] & (1 << i)) != 0) {
                    palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_VISIBLE);
                }
            }
        }

        palette_used_colors.write(4095, PALETTE_COLOR_VISIBLE);

        return palette_recalc();
    }

    /**
     * ***************************************************************************
     */
    public static WriteHandlerPtr vidram_offset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            where = data;
        }
    };
    public static ReadHandlerPtr vidram_data_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return vidram.READ_WORD(where << 1);
        }
    };
    public static WriteHandlerPtr vidram_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            vidram.WRITE_WORD(where << 1, data);
            where = (where + modulo) & 0xffff;
        }
    };

    /* Modulo can become negative , Puzzle Bobble Super Sidekicks and a lot */
 /* of other games use this */
    public static WriteHandlerPtr vidram_modulo_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            modulo = data;
        }
    };

    public static ReadHandlerPtr vidram_modulo_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return modulo;
        }
    };
    /* Two routines to enable videoram to be read in debugger */
    public static ReadHandlerPtr mish_vid_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return vidram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr mish_vid_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(vidram, offset, data);
        }
    };

    public static WriteHandlerPtr neo_board_fix_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            fix_bank = 1;
        }
    };

    public static WriteHandlerPtr neo_game_fix_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            fix_bank = 0;
        }
    };

    /**
     * ***************************************************************************
     */

    public static void NeoMVSDrawGfx(UBytePtr[] line, GfxElement gfx, /* AJP */
            /*unsigned*/ int code,/*unsigned*/ int color, int flipx, int flipy, int sx, int sy,
            int zx, int zy, rectangle clip) {
        int /*ox,*/ oy, ey, y, dy;
        UBytePtr bm;
        int col;
        int l;
        /* Line skipping counter */

        int mydword;

        IntPtr fspr = new IntPtr(memory_region(REGION_GFX2));//UINT32 *fspr = (UINT32 *)memory_region(REGION_GFX2);

        char[] l_y_skip;
        /*TODO*///
/*TODO*///
        /* Mish/AJP - Most clipping is done in main loop */
        oy = sy;
        ey = sy + zy - 1;
        /* Clip for size of zoomed object */

        if (sy < clip.min_y) {
            sy = clip.min_y;
        }
        if (ey >= clip.max_y) {
            ey = clip.max_y;
        }
        if (sx <= -16) {
            return;
        }

        /* Safety feature */
        code = code % no_of_tiles;

        if (gfx.pen_usage[code] == 0) /* decode tile if it hasn't been yet */ {
            decodetile(code);
        }

        /* Check for total transparency, no need to draw */
        if ((gfx.pen_usage[code] & ~1) == 0) {
            return;
        }

        if (zy == 16) {
            l_y_skip = full_y_skip;
        } else {
            l_y_skip = dda_y_skip;
        }

        if (flipy!=0) /* Y flip */ {
            dy = -2;
            fspr.inc((code + 1) * 32 - 2 - (sy - oy) * 2);
        } else /* normal */ {
            dy = 2;
            fspr.inc(code * 32 + (sy - oy) * 2);
        }
        UShortArray paldata;/* ASG 980209 */
        paldata = new UShortArray(gfx.colortable, gfx.color_granularity * color);
        		if (flipx!=0)	/* X flip */
		{
			l=0;
			if(zx==16)
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = new UBytePtr(line[y],sx);

					fspr.inc(l_y_skip[l]*dy);

					mydword = fspr.read(4);//fspr.read(1);
					col = (mydword>> 0)&0xf; if (col!=0) bm.write( 0, paldata.read(col));
					col = (mydword>> 4)&0xf; if (col!=0) bm.write( 1, paldata.read(col));
					col = (mydword>> 8)&0xf; if (col!=0) bm.write( 2, paldata.read(col));
					col = (mydword>>12)&0xf; if (col!=0) bm.write( 3, paldata.read(col));
					col = (mydword>>16)&0xf; if (col!=0) bm.write( 4, paldata.read(col));
					col = (mydword>>20)&0xf; if (col!=0) bm.write( 5, paldata.read(col));
					col = (mydword>>24)&0xf; if (col!=0) bm.write( 6, paldata.read(col));
					col = (mydword>>28)&0xf; if (col!=0) bm.write( 7, paldata.read(col));

					mydword = fspr.read(0);
					col = (mydword>> 0)&0xf; if (col!=0) bm.write( 8, paldata.read(col));
					col = (mydword>> 4)&0xf; if (col!=0) bm.write( 9, paldata.read(col));
					col = (mydword>> 8)&0xf; if (col!=0) bm.write(10, paldata.read(col));
					col = (mydword>>12)&0xf; if (col!=0) bm.write(11, paldata.read(col));
					col = (mydword>>16)&0xf; if (col!=0) bm.write(12, paldata.read(col));
					col = (mydword>>20)&0xf; if (col!=0) bm.write(13, paldata.read(col));
					col = (mydword>>24)&0xf; if (col!=0) bm.write(14, paldata.read(col));
					col = (mydword>>28)&0xf; if (col!=0) bm.write(15, paldata.read(col));

					l++;
				}
			}
			else
			{
				for (y = sy;y <= ey;y++)
				{
					bm  = new UBytePtr(line[y],sx);
					fspr.inc(l_y_skip[l]*dy);

					mydword = fspr.read(4);//fspr.read(1);
					if (dda_x_skip[ 0]!=0) { col = (mydword>> 0)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 1]!=0) { col = (mydword>> 4)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 2]!=0) { col = (mydword>> 8)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 3]!=0) { col = (mydword>>12)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 4]!=0) { col = (mydword>>16)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 5]!=0) { col = (mydword>>20)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 6]!=0) { col = (mydword>>24)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 7]!=0) { col = (mydword>>28)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }

					mydword = fspr.read(0);
					if (dda_x_skip[ 8]!=0) { col = (mydword>> 0)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 9]!=0) { col = (mydword>> 4)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[10]!=0) { col = (mydword>> 8)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[11]!=0) { col = (mydword>>12)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[12]!=0) { col = (mydword>>16)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[13]!=0) { col = (mydword>>20)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[14]!=0) { col = (mydword>>24)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[15]!=0) { col = (mydword>>28)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }

					l++;
				}
			}
		}
		else		/* normal */
		{
	  		l=0;
			if(zx==16)
			{
				for (y = sy ;y <= ey;y++)
				{
					bm  = new UBytePtr(line[y] , sx);
					fspr.inc(l_y_skip[l]*dy);

					mydword = fspr.read(0);
					col = (mydword>>28)&0xf; if (col!=0) bm.write( 0, paldata.read(col));
					col = (mydword>>24)&0xf; if (col!=0) bm.write( 1, paldata.read(col));
					col = (mydword>>20)&0xf; if (col!=0) bm.write( 2, paldata.read(col));
					col = (mydword>>16)&0xf; if (col!=0) bm.write( 3, paldata.read(col));
					col = (mydword>>12)&0xf; if (col!=0) bm.write( 4, paldata.read(col));
					col = (mydword>> 8)&0xf; if (col!=0) bm.write( 5, paldata.read(col));
					col = (mydword>> 4)&0xf; if (col!=0) bm.write( 6, paldata.read(col));
					col = (mydword>> 0)&0xf; if (col!=0) bm.write( 7, paldata.read(col));

					mydword = fspr.read(4);//fspr.read(1);
					col = (mydword>>28)&0xf; if (col!=0) bm.write( 8, paldata.read(col));
					col = (mydword>>24)&0xf; if (col!=0) bm.write( 9, paldata.read(col));
					col = (mydword>>20)&0xf; if (col!=0) bm.write(10, paldata.read(col));
					col = (mydword>>16)&0xf; if (col!=0) bm.write(11, paldata.read(col));
					col = (mydword>>12)&0xf; if (col!=0) bm.write(12, paldata.read(col));
					col = (mydword>> 8)&0xf; if (col!=0) bm.write(13, paldata.read(col));
					col = (mydword>> 4)&0xf; if (col!=0) bm.write(14, paldata.read(col));
					col = (mydword>> 0)&0xf; if (col!=0) bm.write(15, paldata.read(col));

					l++;
				}
			}
			else
			{
				for (y = sy ;y <= ey;y++)
				{
					bm  = new UBytePtr(line[y] , sx);
					fspr.inc(l_y_skip[l]*dy);

					mydword = fspr.read(0);
					if (dda_x_skip[ 0]!=0) { col = (mydword>>28)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 1]!=0) { col = (mydword>>24)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 2]!=0) { col = (mydword>>20)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 3]!=0) { col = (mydword>>16)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 4]!=0) { col = (mydword>>12)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 5]!=0) { col = (mydword>> 8)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 6]!=0) { col = (mydword>> 4)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 7]!=0) { col = (mydword>> 0)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }

					mydword = fspr.read(4);//fspr.read(1);
					if (dda_x_skip[ 8]!=0) { col = (mydword>>28)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[ 9]!=0) { col = (mydword>>24)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[10]!=0) { col = (mydword>>20)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[11]!=0) { col = (mydword>>16)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[12]!=0) { col = (mydword>>12)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[13]!=0) { col = (mydword>> 8)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[14]!=0) { col = (mydword>> 4)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }
					if (dda_x_skip[15]!=0) { col = (mydword>> 0)&0xf; if (col!=0) bm.write(paldata.read(col)); bm.inc(); }

					l++;
				}
			}
		}
    }

    /*TODO*///
/*TODO*///void NeoMVSDrawGfx16(unsigned char **line,const struct GfxElement *gfx, /* AJP */
/*TODO*///		unsigned int code,unsigned int color,int flipx,int flipy,int sx,int sy,
/*TODO*///		int zx,int zy,const struct rectangle *clip)
/*TODO*///{
/*TODO*///	int /*ox,*/oy,ey,y,dy;
/*TODO*///	unsigned short *bm;
/*TODO*///	int col;
/*TODO*///	int l; /* Line skipping counter */
/*TODO*///
/*TODO*///	int mydword;
/*TODO*///
/*TODO*///	UINT32 *fspr = (UINT32 *)memory_region(REGION_GFX2);
/*TODO*///
/*TODO*///	char *l_y_skip;
/*TODO*///
/*TODO*///
/*TODO*///	/* Mish/AJP - Most clipping is done in main loop */
/*TODO*///	oy = sy;
/*TODO*///  	ey = sy + zy -1; 	/* Clip for size of zoomed object */
/*TODO*///
/*TODO*///	if (sy < clip->min_y) sy = clip->min_y;
/*TODO*///	if (ey >= clip->max_y) ey = clip->max_y;
/*TODO*///	if (sx <= -16) return;
/*TODO*///
/*TODO*///	/* Safety feature */
/*TODO*///	code=code%no_of_tiles;
/*TODO*///
/*TODO*///	if (gfx->pen_usage[code] == 0)	/* decode tile if it hasn't been yet */
/*TODO*///		decodetile(code);
/*TODO*///
/*TODO*///	/* Check for total transparency, no need to draw */
/*TODO*///	if ((gfx->pen_usage[code] & ~1) == 0)
/*TODO*///		return;
/*TODO*///
/*TODO*///   	if(zy==16)
/*TODO*///		 l_y_skip=full_y_skip;
/*TODO*///	else
/*TODO*///		 l_y_skip=dda_y_skip;
/*TODO*///
/*TODO*///	if (flipy)	/* Y flip */
/*TODO*///	{
/*TODO*///		dy = -2;
/*TODO*///		fspr+=(code+1)*32 - 2 - (sy-oy)*2;
/*TODO*///	}
/*TODO*///	else		/* normal */
/*TODO*///	{
/*TODO*///		dy = 2;
/*TODO*///		fspr+=code*32 + (sy-oy)*2;
/*TODO*///	}
/*TODO*///
/*TODO*///	{
/*TODO*///		const unsigned short *paldata;	/* ASG 980209 */
/*TODO*///		paldata = &gfx->colortable[gfx->color_granularity * color];
/*TODO*///		if (flipx)	/* X flip */
/*TODO*///		{
/*TODO*///			l=0;
/*TODO*///			if(zx==16)
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = ((unsigned short *)line[y])+sx;
/*TODO*///
/*TODO*///					fspr+=l_y_skip[l]*dy;
/*TODO*///
/*TODO*///					mydword = fspr[1];
/*TODO*///					col = (mydword>> 0)&0xf; if (col) bm[ 0] = paldata[col];
/*TODO*///					col = (mydword>> 4)&0xf; if (col) bm[ 1] = paldata[col];
/*TODO*///					col = (mydword>> 8)&0xf; if (col) bm[ 2] = paldata[col];
/*TODO*///					col = (mydword>>12)&0xf; if (col) bm[ 3] = paldata[col];
/*TODO*///					col = (mydword>>16)&0xf; if (col) bm[ 4] = paldata[col];
/*TODO*///					col = (mydword>>20)&0xf; if (col) bm[ 5] = paldata[col];
/*TODO*///					col = (mydword>>24)&0xf; if (col) bm[ 6] = paldata[col];
/*TODO*///					col = (mydword>>28)&0xf; if (col) bm[ 7] = paldata[col];
/*TODO*///
/*TODO*///					mydword = fspr[0];
/*TODO*///					col = (mydword>> 0)&0xf; if (col) bm[ 8] = paldata[col];
/*TODO*///					col = (mydword>> 4)&0xf; if (col) bm[ 9] = paldata[col];
/*TODO*///					col = (mydword>> 8)&0xf; if (col) bm[10] = paldata[col];
/*TODO*///					col = (mydword>>12)&0xf; if (col) bm[11] = paldata[col];
/*TODO*///					col = (mydword>>16)&0xf; if (col) bm[12] = paldata[col];
/*TODO*///					col = (mydword>>20)&0xf; if (col) bm[13] = paldata[col];
/*TODO*///					col = (mydword>>24)&0xf; if (col) bm[14] = paldata[col];
/*TODO*///					col = (mydword>>28)&0xf; if (col) bm[15] = paldata[col];
/*TODO*///
/*TODO*///					l++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				for (y = sy;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = ((unsigned short *)line[y])+sx;
/*TODO*///					fspr+=l_y_skip[l]*dy;
/*TODO*///
/*TODO*///					mydword = fspr[1];
/*TODO*///					if (dda_x_skip[ 0]) { col = (mydword>> 0)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 1]) { col = (mydword>> 4)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 2]) { col = (mydword>> 8)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 3]) { col = (mydword>>12)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 4]) { col = (mydword>>16)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 5]) { col = (mydword>>20)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 6]) { col = (mydword>>24)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 7]) { col = (mydword>>28)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///
/*TODO*///					mydword = fspr[0];
/*TODO*///					if (dda_x_skip[ 8]) { col = (mydword>> 0)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 9]) { col = (mydword>> 4)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[10]) { col = (mydword>> 8)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[11]) { col = (mydword>>12)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[12]) { col = (mydword>>16)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[13]) { col = (mydword>>20)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[14]) { col = (mydword>>24)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[15]) { col = (mydword>>28)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///
/*TODO*///					l++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else		/* normal */
/*TODO*///		{
/*TODO*///	  		l=0;
/*TODO*///			if(zx==16)
/*TODO*///			{
/*TODO*///				for (y = sy ;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = ((unsigned short *)line[y]) + sx;
/*TODO*///					fspr+=l_y_skip[l]*dy;
/*TODO*///
/*TODO*///					mydword = fspr[0];
/*TODO*///					col = (mydword>>28)&0xf; if (col) bm[ 0] = paldata[col];
/*TODO*///					col = (mydword>>24)&0xf; if (col) bm[ 1] = paldata[col];
/*TODO*///					col = (mydword>>20)&0xf; if (col) bm[ 2] = paldata[col];
/*TODO*///					col = (mydword>>16)&0xf; if (col) bm[ 3] = paldata[col];
/*TODO*///					col = (mydword>>12)&0xf; if (col) bm[ 4] = paldata[col];
/*TODO*///					col = (mydword>> 8)&0xf; if (col) bm[ 5] = paldata[col];
/*TODO*///					col = (mydword>> 4)&0xf; if (col) bm[ 6] = paldata[col];
/*TODO*///					col = (mydword>> 0)&0xf; if (col) bm[ 7] = paldata[col];
/*TODO*///
/*TODO*///					mydword = fspr[1];
/*TODO*///					col = (mydword>>28)&0xf; if (col) bm[ 8] = paldata[col];
/*TODO*///					col = (mydword>>24)&0xf; if (col) bm[ 9] = paldata[col];
/*TODO*///					col = (mydword>>20)&0xf; if (col) bm[10] = paldata[col];
/*TODO*///					col = (mydword>>16)&0xf; if (col) bm[11] = paldata[col];
/*TODO*///					col = (mydword>>12)&0xf; if (col) bm[12] = paldata[col];
/*TODO*///					col = (mydword>> 8)&0xf; if (col) bm[13] = paldata[col];
/*TODO*///					col = (mydword>> 4)&0xf; if (col) bm[14] = paldata[col];
/*TODO*///					col = (mydword>> 0)&0xf; if (col) bm[15] = paldata[col];
/*TODO*///
/*TODO*///					l++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				for (y = sy ;y <= ey;y++)
/*TODO*///				{
/*TODO*///					bm  = ((unsigned short *)line[y]) + sx;
/*TODO*///					fspr+=l_y_skip[l]*dy;
/*TODO*///
/*TODO*///					mydword = fspr[0];
/*TODO*///					if (dda_x_skip[ 0]) { col = (mydword>>28)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 1]) { col = (mydword>>24)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 2]) { col = (mydword>>20)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 3]) { col = (mydword>>16)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 4]) { col = (mydword>>12)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 5]) { col = (mydword>> 8)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 6]) { col = (mydword>> 4)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 7]) { col = (mydword>> 0)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///
/*TODO*///					mydword = fspr[1];
/*TODO*///					if (dda_x_skip[ 8]) { col = (mydword>>28)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[ 9]) { col = (mydword>>24)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[10]) { col = (mydword>>20)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[11]) { col = (mydword>>16)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[12]) { col = (mydword>>12)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[13]) { col = (mydword>> 8)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[14]) { col = (mydword>> 4)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///					if (dda_x_skip[15]) { col = (mydword>> 0)&0xf; if (col) *bm = paldata[col]; bm++; }
/*TODO*///
/*TODO*///					l++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////******************************************************************************/
/*TODO*///

    static void screenrefresh(osd_bitmap bitmap, rectangle clip) {
        int sx = 0, sy = 0, oy = 0, my = 0, zx = 1, rzy = 1;
        int offs, i, count, y, x;
        int tileno, tileatr, t1, t2, t3;
        char fullmode = 0;
        int ddax = 0, dday = 0, rzx = 15, yskip = 0;
        UBytePtr[] line = bitmap.line;
        int[] pen_usage;
        GfxElement gfx = Machine.gfx[2];
        /* Save constant struct dereference */


        if (clip.max_y - clip.min_y > 8
                || /* kludge to speed up raster effects */ clip.min_y == Machine.drv.visible_area.min_y) {
            /* Palette swap occured after last frame but before this one */
            if (palette_swap_pending != 0) {
                swap_palettes();
            }

            /* Do compressed palette stuff */
            neogeo_palette(clip);
            /* no need to check the return code since we redraw everything each frame */
        }
        fillbitmap(bitmap, Machine.pens[4095], clip);

        /* Draw sprites */
        for (count = 0; count < 0x300; count += 2) {
            t3 = vidram.READ_WORD(0x10000 + count);
            t1 = vidram.READ_WORD(0x10400 + count);
            t2 = vidram.READ_WORD(0x10800 + count);

            /* If this bit is set this new column is placed next to last one */
            if ((t1 & 0x40) != 0) {
                sx += rzx;
                if (sx >= 0x1F0) {
                    sx -= 0x200;
                }

                /* Get new zoom for this column */
                zx = (t3 >> 8) & 0x0f;
                sy = oy;
            } else {
                /* nope it is a new block */
 /* Sprite scaling */
                zx = (t3 >> 8) & 0x0f;
                rzy = t3 & 0xff;

                sx = (t2 >> 7);
                if (sx >= 0x1F0) {
                    sx -= 0x200;
                }

                /* Number of tiles in this strip */
                my = t1 & 0x3f;
                if (my == 0x20) {
                    fullmode = 1;
                } else if (my >= 0x21) {
                    fullmode = 2;	/* most games use 0x21, but */
                } /* Alpha Mission II uses 0x3f */ else {
                    fullmode = 0;
                }

                sy = 0x200 - (t1 >> 7);
                if (clip.max_y - clip.min_y > 8
                        || /* kludge to improve the ssideki games */ clip.min_y == Machine.drv.visible_area.min_y) {
                    if (sy > 0x110) {
                        sy -= 0x200;
                    }
                    if (fullmode == 2 || (fullmode == 1 && rzy == 0xff)) {
                        while (sy < 0) {
                            sy += 2 * (rzy + 1);
                        }
                    }
                }
                oy = sy;

                if (rzy < 0xff && my < 0x10 && my != 0) {
                    my = (my * 256) / (rzy + 1);
                    if (my > 0x10) {
                        my = 0x10;
                    }
                }
                if (my > 0x20) {
                    my = 0x20;
                }

                ddax = 0;
                /* =16; NS990110 neodrift fix */ /* setup x zoom */
            }

            /* No point doing anything if tile strip is 0 */
            if (my == 0) {
                continue;
            }

            /* Process x zoom */
            if (zx != 15) {
                rzx = 0;
                for (i = 0; i < 16; i++) {
                    ddax -= zx + 1;
                    if (ddax <= 0) {
                        ddax += 15 + 1;
                        dda_x_skip[i] = 1;
                        rzx++;
                    } else {
                        dda_x_skip[i] = 0;
                    }
                }
            } else {
                rzx = 16;
            }

            if (sx >= 320) {
                continue;
            }

            /* Setup y zoom */
            if (rzy == 255) {
                yskip = 16;
            } else {
                dday = 0;	/* =256; NS990105 mslug fix */
            }

            offs = count << 6;

            /* my holds the number of tiles in each vertical multisprite block */
            for (y = 0; y < my; y++) {
                tileno = vidram.READ_WORD(offs);
                offs += 2;
                tileatr = vidram.READ_WORD(offs);
                offs += 2;

                if (high_tile != 0 && (tileatr & 0x10) != 0) {
                    tileno += 0x10000;
                }
                if (vhigh_tile != 0 && (tileatr & 0x20) != 0) {
                    tileno += 0x20000;
                }
                if (vvhigh_tile != 0 && (tileatr & 0x40) != 0) {
                    tileno += 0x40000;
                }

                if ((tileatr & 0x8) != 0) {
                    tileno = (tileno & ~7) + ((tileno + neogeo_frame_counter) & 7);
                } else if ((tileatr & 0x4) != 0) {
                    tileno = (tileno & ~3) + ((tileno + neogeo_frame_counter) & 3);
                }

                if (fullmode == 2 || (fullmode == 1 && rzy == 0xff)) {
                    if (sy >= 248) {
                        sy -= 2 * (rzy + 1);
                    }
                } else if (fullmode == 1) {
                    if (y == 0x10) {
                        sy -= 2 * (rzy + 1);
                    }
                } else if (sy > 0x110) {
                    sy -= 0x200;	/* NS990105 mslug2 fix */
                }

                if (rzy != 255) {
                    yskip = 0;
                    dda_y_skip[0] = 0;
                    for (i = 0; i < 16; i++) {
                        dda_y_skip[i + 1] = 0;
                        dday -= rzy + 1;
                        if (dday <= 0) {
                            dday += 256;
                            yskip++;
                            dda_y_skip[yskip]++;
                        } else {
                            dda_y_skip[yskip]++;
                        }
                    }
                }

                if (sy + 15 >= clip.min_y && sy <= clip.max_y) {
                    if (Machine.scrbitmap.depth == 16) {
                        throw new UnsupportedOperationException("Unsupported");
                        /*TODO*///					NeoMVSDrawGfx16(line,
/*TODO*///						gfx,
/*TODO*///						tileno,
/*TODO*///						tileatr >> 8,
/*TODO*///						tileatr & 0x01,tileatr & 0x02,
/*TODO*///						sx,sy,rzx,yskip,
/*TODO*///						clip
/*TODO*///					);
                    } else {
                        NeoMVSDrawGfx(line,
                                gfx,
                                tileno,
                                tileatr >> 8,
                                tileatr & 0x01, tileatr & 0x02,
                                sx, sy, rzx, yskip,
                                clip
                        );
                    }
                }

                sy += yskip;
            }
            /* for y */
        }
        /* for count */



 /* Save some struct de-refs */
        gfx = Machine.gfx[fix_bank];
        pen_usage = gfx.pen_usage;

        /* Character foreground */
        for (y = clip.min_y / 8; y <= clip.max_y / 8; y++) {
            for (x = 0; x < 40; x++) {

                int byte1 = vidram.READ_WORD(0xE000 + 2 * (y + 32 * x));
                int byte2 = byte1 >> 12;
                byte1 = byte1 & 0xfff;

                if ((pen_usage[byte1] & ~1) == 0) {
                    continue;
                }

                drawgfx(bitmap, gfx,
                        byte1,
                        byte2,
                        0, 0,
                        x * 8, y * 8,
                        clip, TRANSPARENCY_PEN, 0);
            }
        }

    }

    public static VhUpdateHandlerPtr neogeo_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            screenrefresh(bitmap, Machine.drv.visible_area);

        }
    };

    static int next_update_first_line;

    public static void neogeo_vh_raster_partial_refresh(osd_bitmap bitmap, int current_line) {
        rectangle clip = new rectangle();

        if (current_line < next_update_first_line) {
            next_update_first_line = 0;
        }

        clip.min_x = Machine.drv.visible_area.min_x;
        clip.max_x = Machine.drv.visible_area.max_x;
        clip.min_y = next_update_first_line;
        clip.max_y = current_line;
        if (clip.min_y < Machine.drv.visible_area.min_y) {
            clip.min_y = Machine.drv.visible_area.min_y;
        }
        if (clip.max_y > Machine.drv.visible_area.max_y) {
            clip.max_y = Machine.drv.visible_area.max_y;
        }

        if (clip.max_y >= clip.min_y) {
//if (errorlog) fprintf(errorlog,"refresh %d-%d\n",clip.min_y,clip.max_y);
            screenrefresh(bitmap, clip);
        }

        next_update_first_line = current_line + 1;
    }
    public static VhUpdateHandlerPtr neogeo_vh_raster_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /* Palette swap occured after last frame but before this one */
            if (palette_swap_pending != 0) {
                swap_palettes();
            }
            palette_recalc();
            /* no need to check the return code since we redraw everything each frame */
        }
    };
}
