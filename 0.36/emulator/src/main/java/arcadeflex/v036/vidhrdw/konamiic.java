/*
 * ported to v0.36
 */
package arcadeflex.v036.vidhrdw;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.mameH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.paletteH.*;
//common imports
import static common.libc.expressions.*;
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import gr.codebb.arcadeflex.common.PtrLib.UShortPtr;
import gr.codebb.arcadeflex.common.SubArrays.UShortArray;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.COMBINE_WORD_MEM;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_transparent_pen;
import static gr.codebb.arcadeflex.v037b7.mame.palette.palette_used_colors;

public class konamiic {

    /**
     * Macros from konamiic.h and specific arcadeflex ones
     */
    public static final int K053251_CI0 = 0;
    public static final int K053251_CI1 = 1;
    public static final int K053251_CI2 = 2;
    public static final int K053251_CI3 = 3;
    public static final int K053251_CI4 = 4;

    /*TODO*///#define VERBOSE 0
    /*
     This recursive function doesn't use additional memory
     (it could be easily converted into an iterative one).
     It's called shuffle because it mimics the shuffling of a deck of cards.
     */
    static void shuffle(UShortPtr buf, int len) {
        int i;
        char t;

        if (len == 2) {
            return;
        }

        if ((len % 4) != 0) {
            throw new UnsupportedOperationException("Error in shuffle konamicc");
        }
        /* must not happen */

        len /= 2;

        for (i = 0; i < len / 2; i++) {
            t = buf.read(len / 2 + i);
            buf.write(len / 2 + i, buf.read(len + i));
            buf.write(len + i, t);
        }

        shuffle(buf, len);
        shuffle(new UShortPtr(buf, len * 2), len);//len*2 ??
    }


    /* helper function to join two 16-bit ROMs and form a 32-bit data stream */
    public static void konami_rom_deinterleave_2(int mem_region) {
        shuffle(new UShortPtr(memory_region(mem_region).memory, memory_region(mem_region).offset), memory_region_length(mem_region) / 2);
    }

    /* helper function to join four 16-bit ROMs and form a 64-bit data stream */
    public static void konami_rom_deinterleave_4(int mem_region) {
        konami_rom_deinterleave_2(mem_region);
        konami_rom_deinterleave_2(mem_region);
    }

    public static final int MAX_K007121 = 2;
    public static char[][] K007121_ctrlram = new char[MAX_K007121][];
    public static int[] K007121_flipscreen = new int[MAX_K007121];

    static {
        for (int i = 0; i < MAX_K007121; i++) {
            K007121_ctrlram[i] = new char[8];
        }
    }

    public static void K007121_ctrl_w(int chip, int offset, int data) {
        switch (offset) {
            case 6:
                /* palette bank change */
                if ((K007121_ctrlram[chip][offset] & 0x30) != (data & 0x30)) {
                    tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                }
                break;
            case 7:
                K007121_flipscreen[chip] = data & 0x08;
                break;
        }

        K007121_ctrlram[chip][offset] = (char) (data & 0xff);

    }
    public static WriteHandlerPtr K007121_ctrl_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007121_ctrl_w(0, offset, data & 0xFF);
        }
    };

    public static WriteHandlerPtr K007121_ctrl_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007121_ctrl_w(1, offset, data & 0xFF);
        }
    };

    public static void K007121_sprites_draw(int chip, osd_bitmap bitmap,
            UBytePtr source, int base_color, int global_x_offset, int bank_base) {
        GfxElement gfx = Machine.gfx[chip];
        int flip_screen = K007121_flipscreen[chip];
        int i, num, inc, trans;
        int[] offs = new int[5];
        int is_flakatck = K007121_ctrlram[chip][0x06] & 0x04;
        /* WRONG!!!! */

 /*       if(konamiicclog!=null) fprintf( konamiicclog,"%02x-%02x-%02x-%02x-%02x-%02x-%02x-%02x  %02x-%02x-%02x-%02x-%02x-%02x-%02x-%02x\n",
         (int)K007121_ctrlram[0][0x00],(int)K007121_ctrlram[0][0x01],(int)K007121_ctrlram[0][0x02],(int)K007121_ctrlram[0][0x03],(int)K007121_ctrlram[0][0x04],(int)K007121_ctrlram[0][0x05],(int)K007121_ctrlram[0][0x06],(int)K007121_ctrlram[0][0x07],
         (int)K007121_ctrlram[1][0x00],(int)K007121_ctrlram[1][0x01],(int)K007121_ctrlram[1][0x02],(int)K007121_ctrlram[1][0x03],(int)K007121_ctrlram[1][0x04],(int)K007121_ctrlram[1][0x05],(int)K007121_ctrlram[1][0x06],(int)K007121_ctrlram[1][0x07]);
         */
        if (is_flakatck != 0) {
            num = 0x40;
            inc = -0x20;
            source.inc(0x3f * 0x20);
            offs[0] = 0x0e;
            offs[1] = 0x0f;
            offs[2] = 0x06;
            offs[3] = 0x04;
            offs[4] = 0x08;
            /* Flak Attack doesn't use a lookup PROM, it maps the color code directly */
 /* to a palette entry */
            trans = TRANSPARENCY_PEN;
        } else /* all others */ {
            num = (K007121_ctrlram[chip][0x03] & 0x40) != 0 ? 0x80 : 0x40;
            /* WRONG!!! (needed by combasc)  */

            inc = 5;
            offs[0] = 0x00;
            offs[1] = 0x01;
            offs[2] = 0x02;
            offs[3] = 0x03;
            offs[4] = 0x04;
            trans = TRANSPARENCY_COLOR;
        }

        for (i = 0; i < num; i++) {
            int number = source.read(offs[0]);
            /* sprite number */

            int sprite_bank = source.read(offs[1]) & 0x0f;
            /* sprite bank */

            int sx = source.read(offs[3]);
            /* vertical position */

            int sy = source.read(offs[2]);
            /* horizontal position */

            int attr = source.read(offs[4]);
            /* attributes */

            int xflip = source.read(offs[4]) & 0x10;
            /* flip x */

            int yflip = source.read(offs[4]) & 0x20;
            /* flip y */

            int color = base_color + ((source.read(offs[1]) & 0xf0) >> 4);
            int width, height;
            int x_offset[] = {0x0, 0x1, 0x4, 0x5};
            int y_offset[] = {0x0, 0x2, 0x8, 0xa};
            int x, y, ex, ey;

            if ((attr & 0x01) != 0) {
                sx -= 256;
            }
            if (sy >= 240) {
                sy -= 256;
            }

            number += ((sprite_bank & 0x3) << 8) + ((attr & 0xc0) << 4);
            number = number << 2;
            number += (sprite_bank >> 2) & 3;
            /*if (konamiicclog != null) {
                fprintf(konamiicclog, "number=%d,sprite_bank=%d,sx=%d,sy=%d,attr=%d,xflip=%d,yflip=%d,color=%d\n", number, sprite_bank, sx, sy, attr, xflip, yflip, color);
            }*/
            if (is_flakatck == 0 || source.read(0x00) != 0) /* Flak Attack needs this */ {
                number += bank_base;

                switch (attr & 0xe) {
                    case 0x06:
                        width = height = 1;
                        break;
                    case 0x04:
                        width = 1;
                        height = 2;
                        number &= (~2);
                        break;
                    case 0x02:
                        width = 2;
                        height = 1;
                        number &= (~1);
                        break;
                    case 0x00:
                        width = height = 2;
                        number &= (~3);
                        break;
                    case 0x08:
                        width = height = 4;
                        number &= (~3);
                        break;
                    default:
                        width = 1;
                        height = 1;
                    //					if (errorlog) fprintf(errorlog,"Unknown sprite size %02x\n",attr&0xe);
                    //					usrintf_showmessage("Unknown sprite size %02x\n",attr&0xe);
                }

                for (y = 0; y < height; y++) {
                    for (x = 0; x < width; x++) {
                        ex = xflip != 0 ? (width - 1 - x) : x;
                        ey = yflip != 0 ? (height - 1 - y) : y;

                        if (flip_screen != 0) {
                            drawgfx(bitmap, gfx,
                                    number + x_offset[ex] + y_offset[ey],
                                    color,
                                    NOT(xflip), NOT(yflip),
                                    248 - (sx + x * 8), 248 - (sy + y * 8),
                                    Machine.drv.visible_area, trans, 0);
                        } else {
                            drawgfx(bitmap, gfx,
                                    number + x_offset[ex] + y_offset[ey],
                                    color,
                                    xflip, yflip,
                                    global_x_offset + sx + x * 8, sy + y * 8,
                                    Machine.drv.visible_area, trans, 0);
                        }
                    }
                }
            }

            source.inc(inc);
        }
    }

    /*TODO*///
/*TODO*///void K007121_mark_sprites_colors(int chip,
/*TODO*///		const unsigned char *source,int base_color,int bank_base)
/*TODO*///{
/*TODO*///	int i,num,inc,offs[5];
/*TODO*///	int is_flakatck = K007121_ctrlram[chip][0x06] & 0x04;	/* WRONG!!!! */
/*TODO*///
/*TODO*///	unsigned short palette_map[512];
/*TODO*///
/*TODO*///	if (is_flakatck)
/*TODO*///	{
/*TODO*///		num = 0x40;
/*TODO*///		inc = -0x20;
/*TODO*///		source += 0x3f*0x20;
/*TODO*///		offs[0] = 0x0e;
/*TODO*///		offs[1] = 0x0f;
/*TODO*///		offs[2] = 0x06;
/*TODO*///		offs[3] = 0x04;
/*TODO*///		offs[4] = 0x08;
/*TODO*///	}
/*TODO*///	else	/* all others */
/*TODO*///	{
/*TODO*///		num = (K007121_ctrlram[chip][0x03] & 0x40) ? 0x80 : 0x40;
/*TODO*///		inc = 5;
/*TODO*///		offs[0] = 0x00;
/*TODO*///		offs[1] = 0x01;
/*TODO*///		offs[2] = 0x02;
/*TODO*///		offs[3] = 0x03;
/*TODO*///		offs[4] = 0x04;
/*TODO*///	}
/*TODO*///
/*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*///
/*TODO*///	/* sprites */
/*TODO*///	for (i = 0;i < num;i++)
/*TODO*///	{
/*TODO*///		int color;
/*TODO*///
/*TODO*///		color = base_color + ((source[offs[1]] & 0xf0) >> 4);
/*TODO*///		palette_map[color] |= 0xffff;
/*TODO*///
/*TODO*///		source += inc;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now build the final table */
/*TODO*///	for (i = 0; i < 512; i++)
/*TODO*///	{
/*TODO*///		int usage = palette_map[i], j;
/*TODO*///		if (usage)
/*TODO*///		{
/*TODO*///			for (j = 0; j < 16; j++)
/*TODO*///				if (usage & (1 << j))
/*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
    //K007342_callback interface
    public static abstract interface K007342_callbackProcPtr {

        public abstract void handler(int tilemap, int bank, int[] code, int[] color);
    }

    static UBytePtr K007342_ram, K007342_scroll_ram;
    static int K007342_gfxnum;
    static int K007342_int_enabled;
    static int K007342_flipscreen;
    static int[] K007342_scrollx = new int[2];
    static int[] K007342_scrolly = new int[2];
    static UBytePtr K007342_videoram_0, K007342_colorram_0;
    static UBytePtr K007342_videoram_1, K007342_colorram_1;
    static int[] K007342_regs = new int[8];
    static K007342_callbackProcPtr K007342_callback;
    static tilemap[] K007342_tilemap = new tilemap[2];

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    public static UBytePtr colorram, videoram1, videoram2;
    public static int layer;

    public static void tilemap_0_preupdate() {
        colorram = K007342_colorram_0;
        videoram1 = K007342_videoram_0;
        layer = 0;
    }

    public static void tilemap_1_preupdate() {
        colorram = K007342_colorram_1;
        videoram1 = K007342_videoram_1;
        layer = 1;
    }

    public static WriteHandlerPtr K007342_get_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int tile_index;
            int color[] = new int[1];
            int code[] = new int[1];

            if (col >= 32) {
                col -= 32;
                tile_index = 0x400 + row * 32 + col;
            } else {
                tile_index = row * 32 + col;
            }

            color[0] = colorram.read(tile_index);
            code[0] = videoram1.read(tile_index);

            tile_info.flags = (char) (TILE_FLIPYX((color[0] & 0x30) >> 4) & 0xFF);
            tile_info.priority = (char) (((color[0] & 0x80) >> 7) & 0xFF);

            K007342_callback.handler(layer, K007342_regs[1], code, color);

            SET_TILE_INFO(K007342_gfxnum, code[0], color[0]);
        }
    };

    public static int K007342_vh_start(int gfx_index, K007342_callbackProcPtr callback) {
        K007342_gfxnum = gfx_index;
        K007342_callback = callback;

        K007342_tilemap[0] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
        K007342_tilemap[1] = tilemap_create(K007342_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);

        K007342_ram = new UBytePtr(0x2000);
        K007342_scroll_ram = new UBytePtr(0x0200);

        if (K007342_ram == null || K007342_scroll_ram == null || K007342_tilemap[0] == null || K007342_tilemap[1] == null) {
            K007342_vh_stop();
            return 1;
        }

        memset(K007342_ram, 0, 0x2000);

        K007342_colorram_0 = new UBytePtr(K007342_ram, 0x0000);
        K007342_colorram_1 = new UBytePtr(K007342_ram, 0x1000);
        K007342_videoram_0 = new UBytePtr(K007342_ram, 0x0800);
        K007342_videoram_1 = new UBytePtr(K007342_ram, 0x1800);

        K007342_tilemap[0].transparent_pen = 0;
        K007342_tilemap[1].transparent_pen = 0;

        return 0;
    }

    public static void K007342_vh_stop() {
        K007342_ram = null;
        K007342_scroll_ram = null;
    }
    public static ReadHandlerPtr K007342_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007342_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007342_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset < 0x1000) {
                /* layer 0 */

                if (K007342_ram.read(offset) != data) {
                    if ((offset & 0x400) != 0) {
                        tilemap_mark_tile_dirty(K007342_tilemap[0], offset % 32 + 32, (offset & 0x3ff) / 32);
                    } else {
                        tilemap_mark_tile_dirty(K007342_tilemap[0], offset % 32, (offset & 0x3ff) / 32);
                    }
                    K007342_ram.write(offset, data);
                }
            } else {
                /* layer 1 */

                if (K007342_ram.read(offset) != data) {
                    if ((offset & 0x400) != 0) {
                        tilemap_mark_tile_dirty(K007342_tilemap[1], offset % 32 + 32, (offset & 0x3ff) / 32);
                    } else {
                        tilemap_mark_tile_dirty(K007342_tilemap[1], offset % 32, (offset & 0x3ff) / 32);
                    }
                    K007342_ram.write(offset, data);
                }
            }
        }
    };

    public static ReadHandlerPtr K007342_scroll_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007342_scroll_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007342_scroll_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007342_scroll_ram.write(offset, data);
        }
    };

    public static WriteHandlerPtr K007342_vreg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0x00:
                    /* bit 1: INT control */
                    K007342_int_enabled = data & 0x02;
                    K007342_flipscreen = data & 0x10;
                    tilemap_set_flip(K007342_tilemap[0], K007342_flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    tilemap_set_flip(K007342_tilemap[1], K007342_flipscreen != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    break;
                case 0x01:
                    /* used for banking in Rock'n'Rage */

                    if (data != K007342_regs[1]) {
                        tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                    }
                case 0x02:
                    K007342_scrollx[0] = (K007342_scrollx[0] & 0xff) | ((data & 0x01) << 8);
                    K007342_scrollx[1] = (K007342_scrollx[1] & 0xff) | ((data & 0x02) << 7);
                    break;
                case 0x03:
                    /* scroll x (register 0) */

                    K007342_scrollx[0] = (K007342_scrollx[0] & 0x100) | data;
                    break;
                case 0x04:
                    /* scroll y (register 0) */

                    K007342_scrolly[0] = data;
                    break;
                case 0x05:
                    /* scroll x (register 1) */

                    K007342_scrollx[1] = (K007342_scrollx[1] & 0x100) | data;
                    break;
                case 0x06:
                    /* scroll y (register 1) */

                    K007342_scrolly[1] = data;
                case 0x07:
                    /* unused */

                    break;
            }
            K007342_regs[offset] = data;
        }
    };

    public static void K007342_tilemap_update() {
        int offs;

        /* update scroll */
        switch (K007342_regs[2] & 0x1c) {
            case 0x00:
            case 0x08:
                /* unknown, blades of steel shootout between periods */

                tilemap_set_scroll_rows(K007342_tilemap[0], 1);
                tilemap_set_scroll_cols(K007342_tilemap[0], 1);
                tilemap_set_scrollx(K007342_tilemap[0], 0, K007342_scrollx[0]);
                tilemap_set_scrolly(K007342_tilemap[0], 0, K007342_scrolly[0]);
                break;

            case 0x0c:
                /* 32 columns */

                tilemap_set_scroll_rows(K007342_tilemap[0], 1);
                tilemap_set_scroll_cols(K007342_tilemap[0], 512);
                tilemap_set_scrollx(K007342_tilemap[0], 0, K007342_scrollx[0]);
                for (offs = 0; offs < 256; offs++) {
                    tilemap_set_scrolly(K007342_tilemap[0], (offs + K007342_scrollx[0]) & 0x1ff,
                            K007342_scroll_ram.read(2 * (offs / 8)) + 256 * K007342_scroll_ram.read(2 * (offs / 8) + 1));
                }
                break;

            case 0x14:
                /* 256 rows */

                tilemap_set_scroll_rows(K007342_tilemap[0], 256);
                tilemap_set_scroll_cols(K007342_tilemap[0], 1);
                tilemap_set_scrolly(K007342_tilemap[0], 0, K007342_scrolly[0]);
                for (offs = 0; offs < 256; offs++) {
                    tilemap_set_scrollx(K007342_tilemap[0], (offs + K007342_scrolly[0]) & 0xff,
                            K007342_scroll_ram.read(2 * offs) + 256 * K007342_scroll_ram.read(2 * offs + 1));
                }
                break;

            default:
                break;
        }

        tilemap_set_scrollx(K007342_tilemap[1], 0, K007342_scrollx[1]);
        tilemap_set_scrolly(K007342_tilemap[1], 0, K007342_scrolly[1]);

        /* update all layers */
        tilemap_0_preupdate();
        tilemap_update(K007342_tilemap[0]);
        tilemap_1_preupdate();
        tilemap_update(K007342_tilemap[1]);

    }

    public static WriteHandlerPtr K007342_tilemap_set_enable = new WriteHandlerPtr() {
        public void handler(int tilemap, int enable) {
            tilemap_set_enable(K007342_tilemap[tilemap], enable);
        }
    };

    public static void K007342_tilemap_draw(osd_bitmap bitmap, int num, int flags) {
        tilemap_draw(bitmap, K007342_tilemap[num], flags);
    }

    public static int K007342_is_INT_enabled() {
        return K007342_int_enabled;
    }
    //K007420_callback interface

    public static abstract interface K007420_callbackProcPtr {

        public abstract void handler(int[] code, int[] color);
    }
    static GfxElement K007420_gfx;
    static K007420_callbackProcPtr K007420_callback;
    static UBytePtr K007420_ram;

    public static int K007420_vh_start(int gfxnum, K007420_callbackProcPtr callback) {
        K007420_gfx = Machine.gfx[gfxnum];
        K007420_callback = callback;
        K007420_ram = new UBytePtr(0x200);
        if (K007420_ram == null) {
            return 1;
        }

        memset(K007420_ram, 0, 0x200);

        return 0;
    }

    public static void K007420_vh_stop() {
        K007420_ram = null;
    }

    public static ReadHandlerPtr K007420_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K007420_ram.read(offset);
        }
    };

    public static WriteHandlerPtr K007420_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K007420_ram.write(offset, data);
        }
    };

    /*
     * Sprite Format
     * ------------------
     *
     * Byte | Bit(s)   | Use
     * -----+-76543210-+----------------
     *   0  | xxxxxxxx | y position
     *   1  | xxxxxxxx | sprite code (low 8 bits)
     *   2  | xxxxxxxx | depends on external conections. Usually banking
     *   3  | xxxxxxxx | x position (low 8 bits)
     *   4  | x------- | x position (high bit)
     *   4  | -xxx---- | sprite size 000=16x16 001=8x16 010=16x8 011=8x8 100=32x32
     *   4  | ----x--- | flip y
     *   4  | -----x-- | flip x
     *   4  | ------xx | zoom (bits 8 & 9)
     *   5  | xxxxxxxx | zoom (low 8 bits)  0x080 = normal, < 0x80 enlarge, > 0x80 reduce
     *   6  | xxxxxxxx | unused
     *   7  | xxxxxxxx | unused
     */
    public static void K007420_sprites_draw(osd_bitmap bitmap) {
        int K007420_SPRITERAM_SIZE = 0x200;
        int offs;

        for (offs = K007420_SPRITERAM_SIZE - 8; offs >= 0; offs -= 8) {
            int ox, oy, flipx, flipy, zoom, w, h, x, y;
            int[] code = new int[1];
            int[] color = new int[1];
            int xoffset[] = {0, 1, 4, 5};
            int yoffset[] = {0, 2, 8, 10};

            code[0] = K007420_ram.read(offs + 1);
            color[0] = K007420_ram.read(offs + 2);
            ox = K007420_ram.read(offs + 3) - ((K007420_ram.read(offs + 4) & 0x80) << 1);
            oy = 256 - K007420_ram.read(offs + 0);
            flipx = K007420_ram.read(offs + 4) & 0x04;
            flipy = K007420_ram.read(offs + 4) & 0x08;

            K007420_callback.handler(code, color);

            /* kludge for rock'n'rage */
            if ((K007420_ram.read(offs + 4) == 0x40) && (K007420_ram.read(offs + 1) == 0xff)
                    && (K007420_ram.read(offs + 2) == 0x00) && (K007420_ram.read(offs + 5) == 0xf0)) {
                continue;
            }

            /* 0x080 = normal scale, 0x040 = double size, 0x100 half size */
            zoom = K007420_ram.read(offs + 5) | ((K007420_ram.read(offs + 4) & 0x03) << 8);
            if (zoom == 0) {
                continue;
            }
            zoom = 0x10000 * 128 / zoom;

            switch (K007420_ram.read(offs + 4) & 0x70) {
                case 0x30:
                    w = h = 1;
                    break;
                case 0x20:
                    w = 2;
                    h = 1;
                    code[0] &= (~1);
                    break;
                case 0x10:
                    w = 1;
                    h = 2;
                    code[0] &= (~2);
                    break;
                case 0x00:
                    w = h = 2;
                    code[0] &= (~3);
                    break;
                case 0x40:
                    w = h = 4;
                    code[0] &= (~3);
                    break;
                default:
                    w = 1;
                    h = 1;
                //if (errorlog) fprintf(errorlog,"Unknown sprite size %02x\n",(K007420_ram[offs+4] & 0x70)>>4);
            }

            if (K007342_flipscreen != 0) {
                ox = 256 - ox - ((zoom * w + (1 << 12)) >> 13);
                oy = 256 - oy - ((zoom * h + (1 << 12)) >> 13);
                flipx = NOT(flipx);
                flipy = NOT(flipy);
            }

            if (zoom == 0x10000) {
                int sx, sy;

                for (y = 0; y < h; y++) {
                    sy = oy + 8 * y;

                    for (x = 0; x < w; x++) {
                        int c = code[0];

                        sx = ox + 8 * x;
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x)];
                        } else {
                            c += xoffset[x];
                        }
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y)];
                        } else {
                            c += yoffset[y];
                        }

                        drawgfx(bitmap, K007420_gfx,
                                c,
                                color[0],
                                flipx, flipy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0);

                        if ((K007342_regs[2] & 0x80) != 0) {
                            drawgfx(bitmap, K007420_gfx,
                                    c,
                                    color[0],
                                    flipx, flipy,
                                    sx, sy - 256,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    }
                }
            } else {
                int sx, sy, zw, zh;
                for (y = 0; y < h; y++) {
                    sy = oy + ((zoom * y + (1 << 12)) >> 13);
                    zh = (oy + ((zoom * (y + 1) + (1 << 12)) >> 13)) - sy;

                    for (x = 0; x < w; x++) {
                        int c = code[0];

                        sx = ox + ((zoom * x + (1 << 12)) >> 13);
                        zw = (ox + ((zoom * (x + 1) + (1 << 12)) >> 13)) - sx;
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x)];
                        } else {
                            c += xoffset[x];
                        }
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y)];
                        } else {
                            c += yoffset[y];
                        }

                        drawgfxzoom(bitmap, K007420_gfx,
                                c,
                                color[0],
                                flipx, flipy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                (zw << 16) / 8, (zh << 16) / 8);

                        if ((K007342_regs[2] & 0x80) != 0) {
                            drawgfxzoom(bitmap, K007420_gfx,
                                    c,
                                    color[0],
                                    flipx, flipy,
                                    sx, sy - 256,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                    (zw << 16) / 8, (zh << 16) / 8);
                        }
                    }
                }
            }
        }
    }

    //K052109_callback interface
    public static abstract interface K052109_callbackProcPtr {

        public abstract void handler(int layer, int bank, int[] code, int[] color);
    }
    static int K052109_memory_region;
    static int K052109_gfxnum;
    static K052109_callbackProcPtr K052109_callback;
    static UBytePtr K052109_ram;
    static UBytePtr K052109_videoram_F, K052109_videoram2_F, K052109_colorram_F;
    static UBytePtr K052109_videoram_A, K052109_videoram2_A, K052109_colorram_A;
    static UBytePtr K052109_videoram_B, K052109_videoram2_B, K052109_colorram_B;
    static /*unsigned*/ char[] K052109_charrombank = new char[4];
    static int has_extra_video_ram;
    static int K052109_RMRD_line;
    static int K052109_tileflip_enable;
    static int K052109_irq_enabled;
    static /*unsigned*/ char K052109_romsubbank, K052109_scrollctrl;
    static tilemap[] K052109_tilemap = new tilemap[3];

    /**
     * *************************************************************************
     *
     * Callbacks for the TileMap code
     *
     **************************************************************************
     */
    static void tilemap0_preupdate() {
        colorram = K052109_colorram_F;
        videoram1 = K052109_videoram_F;
        videoram2 = K052109_videoram2_F;
        layer = 0;
    }

    static void tilemap1_preupdate() {
        colorram = K052109_colorram_A;
        videoram1 = K052109_videoram_A;
        videoram2 = K052109_videoram2_A;
        layer = 1;
    }

    static void tilemap2_preupdate() {
        colorram = K052109_colorram_B;
        videoram1 = K052109_videoram_B;
        videoram2 = K052109_videoram2_B;
        layer = 2;
    }

    public static WriteHandlerPtr K052109_get_tile_info = new WriteHandlerPtr() {
        public void handler(int col, int row) {
            int flipy = 0;
            int tile_index = 64 * row + col;
            int[] code = new int[1];
            int[] color = new int[1];
            code[0] = videoram1.read(tile_index) + 256 * videoram2.read(tile_index);
            color[0] = colorram.read(tile_index);
            int bank = K052109_charrombank[(color[0] & 0x0c) >> 2] & 0xFF;
            if (has_extra_video_ram != 0) {
                bank = (color[0] & 0x0c) >> 2;
                /* kludge for X-Men */

            }
            color[0] = (color[0] & 0xf3) | ((bank & 0x03) << 2);
            bank >>= 2;

            flipy = color[0] & 0x02;

            tile_info.flags = 0;

            K052109_callback.handler(layer, bank, code, color);///(*K052109_callback)(layer,bank,&code,&color);

            SET_TILE_INFO(K052109_gfxnum, code[0], color[0]);

            /* if the callback set flip X but it is not enabled, turn it off */
            if ((K052109_tileflip_enable & 1) == 0) {
                tile_info.flags &= ~TILE_FLIPX;
            }

            /* if flip Y is enabled and the attribute but is set, turn it on */
            if (flipy != 0 && (K052109_tileflip_enable & 2) != 0) {
                tile_info.flags |= TILE_FLIPY;
            }
        }
    };

    public static int K052109_vh_start(int gfx_memory_region, int plane0, int plane1, int plane2, int plane3, K052109_callbackProcPtr callback) {
        int gfx_index;
        GfxLayout charlayout = new GfxLayout(
                8, 8,
                0, /* filled in later */
                4,
                new int[]{0, 0, 0, 0}, /* filled in later */
                new int[]{0, 1, 2, 3, 4, 5, 6, 7},
                new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
                32 * 8
        );


        /* find first empty slot to decode gfx */
        for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++) {
            if (Machine.gfx[gfx_index] == null) {
                break;
            }
        }
        if (gfx_index == MAX_GFX_ELEMENTS) {
            return 1;
        }

        /* tweak the structure for the number of tiles we have */
        charlayout.total = memory_region_length(gfx_memory_region) / 32;
        charlayout.planeoffset[0] = plane3 * 8;
        charlayout.planeoffset[1] = plane2 * 8;
        charlayout.planeoffset[2] = plane1 * 8;
        charlayout.planeoffset[3] = plane0 * 8;

        /* decode the graphics */
        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region), charlayout);
        if (Machine.gfx[gfx_index] == null) {
            return 1;
        }

        /* set the color information */
        Machine.gfx[gfx_index].colortable = new UShortArray(Machine.remapped_colortable);
        Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

        K052109_memory_region = gfx_memory_region;
        K052109_gfxnum = gfx_index;
        K052109_callback = callback;
        K052109_RMRD_line = CLEAR_LINE;

        has_extra_video_ram = 0;

        K052109_tilemap[0] = tilemap_create(K052109_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
        K052109_tilemap[1] = tilemap_create(K052109_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);
        K052109_tilemap[2] = tilemap_create(K052109_get_tile_info, TILEMAP_TRANSPARENT, 8, 8, 64, 32);

        K052109_ram = new UBytePtr(0x6000);

        if (K052109_ram == null || K052109_tilemap[0] == null || K052109_tilemap[1] == null || K052109_tilemap[2] == null) {
            K052109_vh_stop();
            return 1;
        }

        for (int i = 0; i < 0x6000; i++) {
            K052109_ram.write(i, 0);//memset(K052109_ram,0,0x6000);
        }
        K052109_colorram_F = new UBytePtr(K052109_ram, 0x0000);
        K052109_colorram_A = new UBytePtr(K052109_ram, 0x0800);
        K052109_colorram_B = new UBytePtr(K052109_ram, 0x1000);
        K052109_videoram_F = new UBytePtr(K052109_ram, 0x2000);
        K052109_videoram_A = new UBytePtr(K052109_ram, 0x2800);
        K052109_videoram_B = new UBytePtr(K052109_ram, 0x3000);
        K052109_videoram2_F = new UBytePtr(K052109_ram, 0x4000);
        K052109_videoram2_A = new UBytePtr(K052109_ram, 0x4800);
        K052109_videoram2_B = new UBytePtr(K052109_ram, 0x5000);

        K052109_tilemap[0].transparent_pen = 0;
        K052109_tilemap[1].transparent_pen = 0;
        K052109_tilemap[2].transparent_pen = 0;

        return 0;
    }

    public static void K052109_vh_stop() {
        K052109_ram = null;
    }
    public static ReadHandlerPtr K052109_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (K052109_RMRD_line == CLEAR_LINE) {
                if ((offset & 0x1fff) >= 0x1800) {
                    if (offset >= 0x180c && offset < 0x1834) {
                        /* A y scroll */                    } else if (offset >= 0x1a00 && offset < 0x1c00) {
                        /* A x scroll */                    } else if (offset == 0x1d00) {
                        /* read for bitwise operations before writing */                    } else if (offset >= 0x380c && offset < 0x3834) {
                        /* B y scroll */                    } else if (offset >= 0x3a00 && offset < 0x3c00) {
                        /* B x scroll */                    } else if (errorlog != null) {
                        fprintf(errorlog, "%04x: read from unknown 052109 address %04x\n", cpu_get_pc(), offset);
                    }
                }

                return K052109_ram.read(offset);
            } else /* Punk Shot and TMNT read from 0000-1fff, Aliens from 2000-3fff */ {
                int[] code = new int[1];
                int[] color = new int[1];
                code[0] = (offset & 0x1fff) >> 5;
                color[0] = K052109_romsubbank;
                int bank = (K052109_charrombank[(color[0] & 0x0c) >> 2] >> 2) & 0xFF;
                /* discard low bits (TMNT) */

                int addr;

                if (has_extra_video_ram != 0) {
                    code[0] |= color[0] << 8;
                    /* kludge for X-Men */

                } else {
                    K052109_callback.handler(0, bank, code, color);//(*K052109_callback)(0,bank,&code,&color);
                }
                addr = (code[0] << 5) + (offset & 0x1f);
                addr &= memory_region_length(K052109_memory_region) - 1;

                return memory_region(K052109_memory_region).read(addr);
            }
        }
    };
    public static WriteHandlerPtr K052109_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 0x1fff) < 0x1800) /* tilemap RAM */ {
                if (K052109_ram.read(offset) != data) {
                    if (offset >= 0x4000) {
                        has_extra_video_ram = 1;
                        /* kludge for X-Men */

                    }
                    K052109_ram.write(offset, data);
                    tilemap_mark_tile_dirty(K052109_tilemap[(offset & 0x1fff) / 0x800], offset % 64, (offset % 0x800) / 64);
                }
            } else /* control registers */ {
                K052109_ram.write(offset, data);

                if (offset >= 0x180c && offset < 0x1834) {
                    /* A y scroll */                } else if (offset >= 0x1a00 && offset < 0x1c00) {
                    /* A x scroll */                } else if (offset == 0x1c80) {
                    if (K052109_scrollctrl != data) {
                        if (errorlog != null) {
                            fprintf(errorlog, "%04x: rowscrollcontrol = %02x\n", cpu_get_pc(), data);
                        }
                        K052109_scrollctrl = (char) (data & 0xFF);
                    }
                } else if (offset == 0x1d00) {
                    //#if VERBOSE
                    //if (errorlog) fprintf(errorlog,"%04x: 052109 register 1d00 = %02x\n",cpu_get_pc(),data);
                    //#endif
                    /* bit 2 = irq enable */
 /* the custom chip can also generate NMI and FIRQ, for use with a 6809 */
                    K052109_irq_enabled = data & 0x04;
                } else if (offset == 0x1d80) {
                    int dirty = 0;

                    if (K052109_charrombank[0] != (data & 0x0f)) {
                        dirty |= 1;
                    }
                    if (K052109_charrombank[1] != ((data >> 4) & 0x0f)) {
                        dirty |= 2;
                    }
                    if (dirty != 0) {
                        int i;

                        K052109_charrombank[0] = (char) ((data & 0x0f) & 0xFF);
                        K052109_charrombank[1] = (char) (((data >> 4) & 0x0f) & 0xFF);

                        for (i = 0; i < 0x1800; i++) {
                            int bank = (K052109_ram.read(i) & 0x0c) >> 2;
                            if ((bank == 0 && (dirty & 1) != 0) || (bank == 1 && (dirty & 2) != 0)) {
                                tilemap_mark_tile_dirty(K052109_tilemap[(i & 0x1fff) / 0x800], i % 64, (i % 0x800) / 64);
                            }
                        }
                    }
                } else if (offset == 0x1e00) {
                    if (errorlog != null) {
                        fprintf(errorlog, "%04x: 052109 register 1e00 = %02x\n", cpu_get_pc(), data);
                    }
                    K052109_romsubbank = (char) (data & 0xFF);
                } else if (offset == 0x1e80) {
                    if (errorlog != null && (data & 0xfe) != 0) {
                        fprintf(errorlog, "%04x: 052109 register 1e80 = %02x\n", cpu_get_pc(), data);
                    }
                    tilemap_set_flip(K052109_tilemap[0], (data & 1) != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    tilemap_set_flip(K052109_tilemap[1], (data & 1) != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    tilemap_set_flip(K052109_tilemap[2], (data & 1) != 0 ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
                    if (K052109_tileflip_enable != ((data & 0x06) >> 1)) {
                        K052109_tileflip_enable = ((data & 0x06) >> 1);

                        tilemap_mark_all_tiles_dirty(K052109_tilemap[0]);
                        tilemap_mark_all_tiles_dirty(K052109_tilemap[1]);
                        tilemap_mark_all_tiles_dirty(K052109_tilemap[2]);
                    }
                } else if (offset == 0x1f00) {
                    int dirty = 0;

                    if (K052109_charrombank[2] != (data & 0x0f)) {
                        dirty |= 1;
                    }
                    if (K052109_charrombank[3] != ((data >> 4) & 0x0f)) {
                        dirty |= 2;
                    }
                    if (dirty != 0) {
                        int i;

                        K052109_charrombank[2] = (char) ((data & 0x0f) & 0xFF);
                        K052109_charrombank[3] = (char) (((data >> 4) & 0x0f) & 0xFF);

                        for (i = 0; i < 0x1800; i++) {
                            int bank = (K052109_ram.read(i) & 0x0c) >> 2;
                            if ((bank == 2 && (dirty & 1) != 0) || (bank == 3 && (dirty & 2) != 0)) {
                                tilemap_mark_tile_dirty(K052109_tilemap[(i & 0x1fff) / 0x800], i % 64, (i % 0x800) / 64);
                            }
                        }
                    }
                } else if (offset >= 0x380c && offset < 0x3834) {
                    /* B y scroll */                } else if (offset >= 0x3a00 && offset < 0x3c00) {
                    /* B x scroll */                } else if (errorlog != null) {
                    fprintf(errorlog, "%04x: write %02x to unknown 052109 address %04x\n", cpu_get_pc(), data, offset);
                }
            }
        }
    };

    public static void K052109_set_RMRD_line(int state) {
        K052109_RMRD_line = state;
    }

    public static void K052109_tilemap_update() {

        if ((K052109_scrollctrl & 0x03) == 0x02) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x1a00);

            tilemap_set_scroll_rows(K052109_tilemap[1], 256);
            tilemap_set_scroll_cols(K052109_tilemap[1], 1);
            yscroll = K052109_ram.read(0x180c);
            tilemap_set_scrolly(K052109_tilemap[1], 0, yscroll);
            for (offs = 0; offs < 256; offs++) {
                xscroll = scrollram.read(2 * (offs & 0xfff8) + 0) + 256 * scrollram.read(2 * (offs & 0xfff8) + 1);
                xscroll -= 6;
                tilemap_set_scrollx(K052109_tilemap[1], (offs + yscroll) & 0xff, xscroll);
            }
        } else if ((K052109_scrollctrl & 0x03) == 0x03) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x1a00);

            tilemap_set_scroll_rows(K052109_tilemap[1], 256);
            tilemap_set_scroll_cols(K052109_tilemap[1], 1);
            yscroll = K052109_ram.read(0x180c);
            tilemap_set_scrolly(K052109_tilemap[1], 0, yscroll);
            for (offs = 0; offs < 256; offs++) {
                xscroll = scrollram.read(2 * offs + 0) + 256 * scrollram.read(2 * offs + 1);
                xscroll -= 6;
                tilemap_set_scrollx(K052109_tilemap[1], (offs + yscroll) & 0xff, xscroll);
            }
        } else if ((K052109_scrollctrl & 0x04) == 0x04) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x1800);

            tilemap_set_scroll_rows(K052109_tilemap[1], 1);
            tilemap_set_scroll_cols(K052109_tilemap[1], 512);
            xscroll = K052109_ram.read(0x1a00) + 256 * K052109_ram.read(0x1a01);
            xscroll -= 6;
            tilemap_set_scrollx(K052109_tilemap[1], 0, xscroll);
            for (offs = 0; offs < 512; offs++) {
                yscroll = scrollram.read(offs / 8);
                tilemap_set_scrolly(K052109_tilemap[1], (offs + xscroll) & 0x1ff, yscroll);
            }
        } else {
            int xscroll, yscroll;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x1a00);

            tilemap_set_scroll_rows(K052109_tilemap[1], 1);
            tilemap_set_scroll_cols(K052109_tilemap[1], 1);
            xscroll = scrollram.read(0) + 256 * scrollram.read(1);
            xscroll -= 6;
            yscroll = K052109_ram.read(0x180c);
            tilemap_set_scrollx(K052109_tilemap[1], 0, xscroll);
            tilemap_set_scrolly(K052109_tilemap[1], 0, yscroll);
        }

        if ((K052109_scrollctrl & 0x18) == 0x10) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x3a00);

            tilemap_set_scroll_rows(K052109_tilemap[2], 256);
            tilemap_set_scroll_cols(K052109_tilemap[2], 1);
            yscroll = K052109_ram.read(0x380c);
            tilemap_set_scrolly(K052109_tilemap[2], 0, yscroll);
            for (offs = 0; offs < 256; offs++) {
                xscroll = scrollram.read(2 * (offs & 0xfff8) + 0) + 256 * scrollram.read(2 * (offs & 0xfff8) + 1);
                xscroll -= 6;
                tilemap_set_scrollx(K052109_tilemap[2], (offs + yscroll) & 0xff, xscroll);
            }
        } else if ((K052109_scrollctrl & 0x18) == 0x18) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x3a00);

            tilemap_set_scroll_rows(K052109_tilemap[2], 256);
            tilemap_set_scroll_cols(K052109_tilemap[2], 1);
            yscroll = K052109_ram.read(0x380c);
            tilemap_set_scrolly(K052109_tilemap[2], 0, yscroll);
            for (offs = 0; offs < 256; offs++) {
                xscroll = scrollram.read(2 * offs + 0) + 256 * scrollram.read(2 * offs + 1);
                xscroll -= 6;
                tilemap_set_scrollx(K052109_tilemap[2], (offs + yscroll) & 0xff, xscroll);
            }
        } else if ((K052109_scrollctrl & 0x20) == 0x20) {
            int xscroll, yscroll, offs;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x3800);

            tilemap_set_scroll_rows(K052109_tilemap[2], 1);
            tilemap_set_scroll_cols(K052109_tilemap[2], 512);
            xscroll = K052109_ram.read(0x3a00) + 256 * K052109_ram.read(0x3a01);
            xscroll -= 6;
            tilemap_set_scrollx(K052109_tilemap[2], 0, xscroll);
            for (offs = 0; offs < 512; offs++) {
                yscroll = scrollram.read(offs / 8);
                tilemap_set_scrolly(K052109_tilemap[2], (offs + xscroll) & 0x1ff, yscroll);
            }
        } else {
            int xscroll, yscroll;
            UBytePtr scrollram = new UBytePtr(K052109_ram, 0x3a00);

            tilemap_set_scroll_rows(K052109_tilemap[2], 1);
            tilemap_set_scroll_cols(K052109_tilemap[2], 1);
            xscroll = scrollram.read(0) + 256 * scrollram.read(1);
            xscroll -= 6;
            yscroll = K052109_ram.read(0x380c);
            tilemap_set_scrollx(K052109_tilemap[2], 0, xscroll);
            tilemap_set_scrolly(K052109_tilemap[2], 0, yscroll);
        }

        tilemap0_preupdate();
        tilemap_update(K052109_tilemap[0]);
        tilemap1_preupdate();
        tilemap_update(K052109_tilemap[1]);
        tilemap2_preupdate();
        tilemap_update(K052109_tilemap[2]);

        /*#ifdef MAME_DEBUG
         if ((K052109_scrollctrl & 0x03) == 0x01 ||
         (K052109_scrollctrl & 0x18) == 0x08 ||
         ((K052109_scrollctrl & 0x04) && (K052109_scrollctrl & 0x03)) ||
         ((K052109_scrollctrl & 0x20) && (K052109_scrollctrl & 0x18)) ||
         (K052109_scrollctrl & 0xc0) != 0)
         usrintf_showmessage("scrollcontrol = %02x",K052109_scrollctrl);
         #endif*/
    }

    public static void K052109_tilemap_draw(osd_bitmap bitmap, int num, int flags) {
        tilemap_draw(bitmap, K052109_tilemap[num], flags);
    }

    public static int K052109_is_IRQ_enabled() {
        return K052109_irq_enabled;
    }

    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int K051960_memory_region;
/*TODO*///static struct GfxElement *K051960_gfx;
/*TODO*///static void (*K051960_callback)(int *code,int *color,int *priority);
/*TODO*///static int K051960_romoffset;
/*TODO*///static int K051960_spriteflip,K051960_readroms;
/*TODO*///static unsigned char K051960_spriterombank[3];
/*TODO*///static unsigned char *K051960_ram;
/*TODO*///static int K051960_irq_enabled, K051960_nmi_enabled;
/*TODO*///
/*TODO*///
/*TODO*///int K051960_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*///		void (*callback)(int *code,int *color,int *priority))
/*TODO*///{
/*TODO*///	int gfx_index;
/*TODO*///	static struct GfxLayout spritelayout =
/*TODO*///	{
/*TODO*///		16,16,
/*TODO*///		0,				/* filled in later */
/*TODO*///		4,
/*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7,
/*TODO*///				8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
/*TODO*///		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
/*TODO*///				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
/*TODO*///		128*8
/*TODO*///	};
/*TODO*///
/*TODO*///
/*TODO*///	/* find first empty slot to decode gfx */
/*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*///			break;
/*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*///	spritelayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*///	spritelayout.planeoffset[0] = plane0 * 8;
/*TODO*///	spritelayout.planeoffset[1] = plane1 * 8;
/*TODO*///	spritelayout.planeoffset[2] = plane2 * 8;
/*TODO*///	spritelayout.planeoffset[3] = plane3 * 8;
/*TODO*///
/*TODO*///	/* decode the graphics */
/*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&spritelayout);
/*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* set the color information */
/*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*///
/*TODO*///	K051960_memory_region = gfx_memory_region;
/*TODO*///	K051960_gfx = Machine->gfx[gfx_index];
/*TODO*///	K051960_callback = callback;
/*TODO*///	K051960_ram = malloc(0x400);
/*TODO*///	if (!K051960_ram) return 1;
/*TODO*///
/*TODO*///	memset(K051960_ram,0,0x400);
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void K051960_vh_stop(void)
/*TODO*///{
/*TODO*///	free(K051960_ram);
/*TODO*///	K051960_ram = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static int K051960_fetchromdata(int byte)
/*TODO*///{
/*TODO*///	int code,color,pri,off1,addr;
/*TODO*///
/*TODO*///
/*TODO*///	addr = K051960_romoffset + (K051960_spriterombank[0] << 8) +
/*TODO*///			((K051960_spriterombank[1] & 0x03) << 16);
/*TODO*///	code = (addr & 0x3ffe0) >> 5;
/*TODO*///	off1 = addr & 0x1f;
/*TODO*///	color = ((K051960_spriterombank[1] & 0xfc) >> 2) + ((K051960_spriterombank[2] & 0x03) << 6);
/*TODO*///	pri = 0;
/*TODO*///	(*K051960_callback)(&code,&color,&pri);
/*TODO*///
/*TODO*///	addr = (code << 7) | (off1 << 2) | byte;
/*TODO*///	addr &= memory_region_length(K051960_memory_region)-1;
/*TODO*///
/*TODO*///#if 0
/*TODO*///	usrintf_showmessage("%04x: addr %06x",cpu_get_pc(),addr);
/*TODO*///#endif
/*TODO*///
/*TODO*///	return memory_region(K051960_memory_region)[addr];
/*TODO*///}
/*TODO*///
/*TODO*///int K051960_r(int offset)
/*TODO*///{
/*TODO*///	if (K051960_readroms)
/*TODO*///	{
/*TODO*///		/* the 051960 remembers the last address read and uses it when reading the sprite ROMs */
/*TODO*///		K051960_romoffset = (offset & 0x3fc) >> 2;
/*TODO*///		return K051960_fetchromdata(offset & 3);	/* only 88 Games reads the ROMs from here */
/*TODO*///	}
/*TODO*///	else
/*TODO*///		return K051960_ram[offset];
/*TODO*///}
/*TODO*///
/*TODO*///void K051960_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051960_ram[offset] = data;
/*TODO*///}
/*TODO*///
/*TODO*///int K051960_word_r(int offset)
/*TODO*///{
/*TODO*///	return K051960_r(offset + 1) | (K051960_r(offset) << 8);
/*TODO*///}
/*TODO*///
/*TODO*///void K051960_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*///		K051960_w(offset,(data >> 8) & 0xff);
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///		K051960_w(offset + 1,data & 0xff);
/*TODO*///}
/*TODO*///
/*TODO*///int K051937_r(int offset)
/*TODO*///{
/*TODO*///	if (K051960_readroms && offset >= 4 && offset < 8)
/*TODO*///	{
/*TODO*///		return K051960_fetchromdata(offset & 3);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (offset == 0)
/*TODO*///		{
/*TODO*///			static int counter;
/*TODO*///
/*TODO*///			/* some games need bit 0 to pulse */
/*TODO*///			return (counter++) & 1;
/*TODO*///		}
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: read unknown 051937 address %x\n",cpu_get_pc(),offset);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void K051937_w(int offset,int data)
/*TODO*///{
/*TODO*///	if (offset == 0)
/*TODO*///	{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///if (data & 0xc6)
/*TODO*///	usrintf_showmessage("051937 reg 00 = %02x",data);
/*TODO*///#endif
/*TODO*///		/* bit 0 is IRQ enable */
/*TODO*///		K051960_irq_enabled = (data & 0x01);
/*TODO*///
/*TODO*///		/* bit 1: probably FIRQ enable */
/*TODO*///
/*TODO*///		/* bit 2 is NMI enable */
/*TODO*///		K051960_nmi_enabled = (data & 0x04);
/*TODO*///
/*TODO*///		/* bit 3 = flip screen */
/*TODO*///		K051960_spriteflip = data & 0x08;
/*TODO*///
/*TODO*///		/* bit 4 used by Devastators and TMNT, unknown */
/*TODO*///
/*TODO*///		/* bit 5 = enable gfx ROM reading */
/*TODO*///		K051960_readroms = data & 0x20;
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 051937 address %x\n",cpu_get_pc(),data,offset);
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	else if (offset >= 2 && offset < 5)
/*TODO*///	{
/*TODO*///		K051960_spriterombank[offset - 2] = data;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///#if 0
/*TODO*///	usrintf_showmessage("%04x: write %02x to 051937 address %x",cpu_get_pc(),data,offset);
/*TODO*///#endif
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 051937 address %x\n",cpu_get_pc(),data,offset);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int K051937_word_r(int offset)
/*TODO*///{
/*TODO*///	return K051937_r(offset + 1) | (K051937_r(offset) << 8);
/*TODO*///}
/*TODO*///
/*TODO*///void K051937_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	if ((data & 0xff000000) == 0)
/*TODO*///		K051937_w(offset,(data >> 8) & 0xff);
/*TODO*///	if ((data & 0x00ff0000) == 0)
/*TODO*///		K051937_w(offset + 1,data & 0xff);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*
/*TODO*/// * Sprite Format
/*TODO*/// * ------------------
/*TODO*/// *
/*TODO*/// * Byte | Bit(s)   | Use
/*TODO*/// * -----+-76543210-+----------------
/*TODO*/// *   0  | x------- | active (show this sprite)
/*TODO*/// *   0  | -xxxxxxx | priority order
/*TODO*/// *   1  | xxx----- | sprite size (see below)
/*TODO*/// *   1  | ---xxxxx | sprite code (high 5 bits)
/*TODO*/// *   2  | xxxxxxxx | sprite code (low 8 bits)
/*TODO*/// *   3  | xxxxxxxx | "color", but depends on external connections (see below)
/*TODO*/// *   4  | xxxxxx-- | zoom y (0 = normal, >0 = shrink)
/*TODO*/// *   4  | ------x- | flip y
/*TODO*/// *   4  | -------x | y position (high bit)
/*TODO*/// *   5  | xxxxxxxx | y position (low 8 bits)
/*TODO*/// *   6  | xxxxxx-- | zoom x (0 = normal, >0 = shrink)
/*TODO*/// *   6  | ------x- | flip x
/*TODO*/// *   6  | -------x | x position (high bit)
/*TODO*/// *   7  | xxxxxxxx | x position (low 8 bits)
/*TODO*/// *
/*TODO*/// * Example of "color" field for Punk Shot:
/*TODO*/// *   3  | x------- | shadow
/*TODO*/// *   3  | -xx----- | priority
/*TODO*/// *   3  | ---x---- | use second gfx ROM bank
/*TODO*/// *   3  | ----xxxx | color code
/*TODO*/// *
/*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*/// * The rest of the sprite remains normal.
/*TODO*/// * Note that Aliens also uses the shadow bit to select the second sprite bank.
/*TODO*/// */
/*TODO*///
/*TODO*///void K051960_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*///{
/*TODO*///#define NUM_SPRITES 128
/*TODO*///	int offs,pri_code;
/*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*///
/*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*///		sortedlist[offs] = -1;
/*TODO*///
/*TODO*///	/* prebuild a sorted table */
/*TODO*///	for (offs = 0;offs < 0x400;offs += 8)
/*TODO*///	{
/*TODO*///		if (K051960_ram[offs] & 0x80)
/*TODO*///			sortedlist[K051960_ram[offs] & 0x7f] = offs;
/*TODO*///	}
/*TODO*///
/*TODO*///	for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
/*TODO*///	{
/*TODO*///		int ox,oy,code,color,pri,size,w,h,x,y,flipx,flipy,zoomx,zoomy;
/*TODO*///		/* sprites can be grouped up to 8x8. The draw order is
/*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*///			10 11 14 15 26 27 30 31
/*TODO*///			32 33 36 37 48 49 52 53
/*TODO*///			34 35 38 39 50 51 54 55
/*TODO*///			40 41 44 45 56 57 60 61
/*TODO*///			42 43 46 47 58 59 62 63
/*TODO*///		*/
/*TODO*///		static int xoffset[8] = { 0, 1, 4, 5, 16, 17, 20, 21 };
/*TODO*///		static int yoffset[8] = { 0, 2, 8, 10, 32, 34, 40, 42 };
/*TODO*///		static int width[8] =  { 1, 2, 1, 2, 4, 2, 4, 8 };
/*TODO*///		static int height[8] = { 1, 1, 2, 2, 2, 4, 4, 8 };
/*TODO*///
/*TODO*///
/*TODO*///		offs = sortedlist[pri_code];
/*TODO*///		if (offs == -1) continue;
/*TODO*///
/*TODO*///		code = K051960_ram[offs+2] + ((K051960_ram[offs+1] & 0x1f) << 8);
/*TODO*///		color = K051960_ram[offs+3] & 0xff;
/*TODO*///		pri = 0;
/*TODO*///
/*TODO*///		(*K051960_callback)(&code,&color,&pri);
/*TODO*///
/*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*///
/*TODO*///		size = (K051960_ram[offs+1] & 0xe0) >> 5;
/*TODO*///		w = width[size];
/*TODO*///		h = height[size];
/*TODO*///
/*TODO*///		if (w >= 2) code &= ~0x01;
/*TODO*///		if (h >= 2) code &= ~0x02;
/*TODO*///		if (w >= 4) code &= ~0x04;
/*TODO*///		if (h >= 4) code &= ~0x08;
/*TODO*///		if (w >= 8) code &= ~0x10;
/*TODO*///		if (h >= 8) code &= ~0x20;
/*TODO*///
/*TODO*///		ox = (256 * K051960_ram[offs+6] + K051960_ram[offs+7]) & 0x01ff;
/*TODO*///		oy = 256 - ((256 * K051960_ram[offs+4] + K051960_ram[offs+5]) & 0x01ff);
/*TODO*///		flipx = K051960_ram[offs+6] & 0x02;
/*TODO*///		flipy = K051960_ram[offs+4] & 0x02;
/*TODO*///		zoomx = (K051960_ram[offs+6] & 0xfc) >> 2;
/*TODO*///		zoomy = (K051960_ram[offs+4] & 0xfc) >> 2;
/*TODO*///		zoomx = 0x10000 / 128 * (128 - zoomx);
/*TODO*///		zoomy = 0x10000 / 128 * (128 - zoomy);
/*TODO*///
/*TODO*///		if (K051960_spriteflip)
/*TODO*///		{
/*TODO*///			ox = 512 - (zoomx * w >> 12) - ox;
/*TODO*///			oy = 256 - (zoomy * h >> 12) - oy;
/*TODO*///			flipx = !flipx;
/*TODO*///			flipy = !flipy;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*///		{
/*TODO*///			int sx,sy;
/*TODO*///
/*TODO*///			for (y = 0;y < h;y++)
/*TODO*///			{
/*TODO*///				sy = oy + 16 * y;
/*TODO*///
/*TODO*///				for (x = 0;x < w;x++)
/*TODO*///				{
/*TODO*///					int c = code;
/*TODO*///
/*TODO*///					sx = ox + 16 * x;
/*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*///					else c += xoffset[x];
/*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*///					else c += yoffset[y];
/*TODO*///
/*TODO*///					/* hack to simulate shadow */
/*TODO*///					if (K051960_ram[offs+3] & 0x80)
/*TODO*///					{
/*TODO*///						int o = K051960_gfx->colortable[16*color+15];
/*TODO*///						K051960_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*///						drawgfx(bitmap,K051960_gfx,
/*TODO*///								c,
/*TODO*///								color,
/*TODO*///								flipx,flipy,
/*TODO*///								sx & 0x1ff,sy,
/*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*///						K051960_gfx->colortable[16*color+15] = o;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						drawgfx(bitmap,K051960_gfx,
/*TODO*///								c,
/*TODO*///								color,
/*TODO*///								flipx,flipy,
/*TODO*///								sx & 0x1ff,sy,
/*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			int sx,sy,zw,zh;
/*TODO*///
/*TODO*///			for (y = 0;y < h;y++)
/*TODO*///			{
/*TODO*///				sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*///				zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*///
/*TODO*///				for (x = 0;x < w;x++)
/*TODO*///				{
/*TODO*///					int c = code;
/*TODO*///
/*TODO*///					sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*///					zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*///					if (flipx) c += xoffset[(w-1-x)];
/*TODO*///					else c += xoffset[x];
/*TODO*///					if (flipy) c += yoffset[(h-1-y)];
/*TODO*///					else c += yoffset[y];
/*TODO*///
/*TODO*///					drawgfxzoom(bitmap,K051960_gfx,
/*TODO*///							c,
/*TODO*///							color,
/*TODO*///							flipx,flipy,
/*TODO*///							sx & 0x1ff,sy,
/*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///#if 0
/*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*///{
/*TODO*///	FILE *fp;
/*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(K051960_ram, 0x400, 1, fp);
/*TODO*///		usrintf_showmessage("saved");
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif
/*TODO*///#undef NUM_SPRITES
/*TODO*///}
/*TODO*///
/*TODO*///void K051960_mark_sprites_colors(void)
/*TODO*///{
/*TODO*///	int offs,i;
/*TODO*///
/*TODO*///	unsigned short palette_map[512];
/*TODO*///
/*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*///
/*TODO*///	/* sprites */
/*TODO*///	for (offs = 0x400-8;offs >= 0;offs -= 8)
/*TODO*///	{
/*TODO*///		if (K051960_ram[offs] & 0x80)
/*TODO*///		{
/*TODO*///			int code,color,pri;
/*TODO*///
/*TODO*///			code = K051960_ram[offs+2] + ((K051960_ram[offs+1] & 0x1f) << 8);
/*TODO*///			color = (K051960_ram[offs+3] & 0xff);
/*TODO*///			pri = 0;
/*TODO*///			(*K051960_callback)(&code,&color,&pri);
/*TODO*///			palette_map[color] |= 0xffff;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now build the final table */
/*TODO*///	for (i = 0; i < 512; i++)
/*TODO*///	{
/*TODO*///		int usage = palette_map[i], j;
/*TODO*///		if (usage)
/*TODO*///		{
/*TODO*///			for (j = 1; j < 16; j++)
/*TODO*///				if (usage & (1 << j))
/*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int K051960_is_IRQ_enabled(void)
/*TODO*///{
/*TODO*///	return K051960_irq_enabled;
/*TODO*///}
/*TODO*///
/*TODO*///int K051960_is_NMI_enabled(void)
/*TODO*///{
/*TODO*///	return K051960_nmi_enabled;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int K052109_051960_r(int offset)
/*TODO*///{
/*TODO*///	if (K052109_RMRD_line == CLEAR_LINE)
/*TODO*///	{
/*TODO*///		if (offset >= 0x3800 && offset < 0x3808)
/*TODO*///			return K051937_r(offset - 0x3800);
/*TODO*///		else if (offset < 0x3c00)
/*TODO*///			return K052109_r(offset);
/*TODO*///		else
/*TODO*///			return K051960_r(offset - 0x3c00);
/*TODO*///	}
/*TODO*///	else return K052109_r(offset);
/*TODO*///}
/*TODO*///
/*TODO*///void K052109_051960_w(int offset,int data)
/*TODO*///{
/*TODO*///	if (offset >= 0x3800 && offset < 0x3808)
/*TODO*///		K051937_w(offset - 0x3800,data);
/*TODO*///	else if (offset < 0x3c00)
/*TODO*///		K052109_w(offset,data);
/*TODO*///	else
/*TODO*///		K051960_w(offset - 0x3c00,data);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int K053245_memory_region=2;
/*TODO*///static struct GfxElement *K053245_gfx;
/*TODO*///static void (*K053245_callback)(int *code,int *color,int *priority);
/*TODO*///static int K053244_romoffset,K053244_rombank;
/*TODO*///static int K053244_readroms;
/*TODO*///static int K053245_flipscreenX,K053245_flipscreenY;
/*TODO*///static int K053245_spriteoffsX,K053245_spriteoffsY;
/*TODO*///static unsigned char *K053245_ram;
/*TODO*///
/*TODO*///int K053245_vh_start(int gfx_memory_region,int plane0,int plane1,int plane2,int plane3,
/*TODO*///		void (*callback)(int *code,int *color,int *priority))
/*TODO*///{
/*TODO*///	int gfx_index;
/*TODO*///	static struct GfxLayout spritelayout =
/*TODO*///	{
/*TODO*///		16,16,
/*TODO*///		0,				/* filled in later */
/*TODO*///		4,
/*TODO*///		{ 0, 0, 0, 0 },	/* filled in later */
/*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7,
/*TODO*///				8*32+0, 8*32+1, 8*32+2, 8*32+3, 8*32+4, 8*32+5, 8*32+6, 8*32+7 },
/*TODO*///		{ 0*32, 1*32, 2*32, 3*32, 4*32, 5*32, 6*32, 7*32,
/*TODO*///				16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32 },
/*TODO*///		128*8
/*TODO*///	};
/*TODO*///
/*TODO*///
/*TODO*///	/* find first empty slot to decode gfx */
/*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*///			break;
/*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* tweak the structure for the number of tiles we have */
/*TODO*///	spritelayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*///	spritelayout.planeoffset[0] = plane3 * 8;
/*TODO*///	spritelayout.planeoffset[1] = plane2 * 8;
/*TODO*///	spritelayout.planeoffset[2] = plane1 * 8;
/*TODO*///	spritelayout.planeoffset[3] = plane0 * 8;
/*TODO*///
/*TODO*///	/* decode the graphics */
/*TODO*///	Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&spritelayout);
/*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* set the color information */
/*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / 16;
/*TODO*///
/*TODO*///	K053245_memory_region = gfx_memory_region;
/*TODO*///	K053245_gfx = Machine->gfx[gfx_index];
/*TODO*///	K053245_callback = callback;
/*TODO*///	K053244_rombank = 0;
/*TODO*///	K053245_ram = malloc(0x800);
/*TODO*///	if (!K053245_ram) return 1;
/*TODO*///
/*TODO*///	memset(K053245_ram,0,0x800);
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void K053245_vh_stop(void)
/*TODO*///{
/*TODO*///	free(K053245_ram);
/*TODO*///	K053245_ram = 0;
/*TODO*///}
/*TODO*///
/*TODO*///int K053245_word_r(int offset)
/*TODO*///{
/*TODO*///	return READ_WORD(&K053245_ram[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///void K053245_word_w(int offset,int data)
/*TODO*///{
/*TODO*///	COMBINE_WORD_MEM(&K053245_ram[offset],data);
/*TODO*///}
/*TODO*///
/*TODO*///int K053245_r(int offset)
/*TODO*///{
/*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*///	return (READ_WORD(&K053245_ram[offset & ~1]) >> shift) & 0xff;
/*TODO*///}
/*TODO*///
/*TODO*///void K053245_w(int offset,int data)
/*TODO*///{
/*TODO*///	int shift = ((offset & 1) ^ 1) << 3;
/*TODO*///	offset &= ~1;
/*TODO*///	COMBINE_WORD_MEM(&K053245_ram[offset],(0xff000000 >> shift) | ((data & 0xff) << shift));
/*TODO*///}
/*TODO*///
/*TODO*///int K053244_r(int offset)
/*TODO*///{
/*TODO*///	if (K053244_readroms && offset >= 0x0c && offset < 0x10)
/*TODO*///	{
/*TODO*///		int addr;
/*TODO*///
/*TODO*///
/*TODO*///		addr = 0x200000 * K053244_rombank + 4 * (K053244_romoffset & 0x7ffff) + ((offset & 3) ^ 1);
/*TODO*///		addr &= memory_region_length(K053245_memory_region)-1;
/*TODO*///
/*TODO*///#if 0
/*TODO*///	usrintf_showmessage("%04x: offset %02x addr %06x",cpu_get_pc(),offset&3,addr);
/*TODO*///#endif
/*TODO*///
/*TODO*///		return memory_region(K053245_memory_region)[addr];
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: read from unknown 053244 address %x\n",cpu_get_pc(),offset);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void K053244_w(int offset,int data)
/*TODO*///{
/*TODO*///	if (offset == 0x00)
/*TODO*///		K053245_spriteoffsX = (K053245_spriteoffsX & 0x00ff) | (data << 8);
/*TODO*///	else if (offset == 0x01)
/*TODO*///		K053245_spriteoffsX = (K053245_spriteoffsX & 0xff00) | data;
/*TODO*///	else if (offset == 0x02)
/*TODO*///		K053245_spriteoffsY = (K053245_spriteoffsY & 0x00ff) | (data << 8);
/*TODO*///	else if (offset == 0x03)
/*TODO*///		K053245_spriteoffsY = (K053245_spriteoffsY & 0xff00) | data;
/*TODO*///	else if (offset == 0x05)
/*TODO*///	{
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///if (data & 0xc8)
/*TODO*///	usrintf_showmessage("053244 reg 05 = %02x",data);
/*TODO*///#endif
/*TODO*///		/* bit 0/1 = flip screen */
/*TODO*///		K053245_flipscreenX = data & 0x01;
/*TODO*///		K053245_flipscreenY = data & 0x02;
/*TODO*///
/*TODO*///		/* bit 2 = unknown, Parodius uses it */
/*TODO*///
/*TODO*///		/* bit 4 = enable gfx ROM reading */
/*TODO*///		K053244_readroms = data & 0x10;
/*TODO*///
/*TODO*///		/* bit 5 = unknown, Rollergames uses it */
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 053244 address 5\n",cpu_get_pc(),data);
/*TODO*///#endif
/*TODO*///	}
/*TODO*///	else if (offset >= 0x08 && offset < 0x0c)
/*TODO*///	{
/*TODO*///		offset = 8*((offset & 0x03) ^ 0x01);
/*TODO*///		K053244_romoffset = (K053244_romoffset & ~(0xff << offset)) | (data << offset);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	else
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to unknown 053244 address %x\n",cpu_get_pc(),data,offset);
/*TODO*///}
/*TODO*///
/*TODO*///void K053244_bankselect(int bank)   /* used by TMNT2 for ROM testing */
/*TODO*///{
/*TODO*///	K053244_rombank = bank;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Sprite Format
/*TODO*/// * ------------------
/*TODO*/// *
/*TODO*/// * Word | Bit(s)           | Use
/*TODO*/// * -----+-fedcba9876543210-+----------------
/*TODO*/// *   0  | x--------------- | active (show this sprite)
/*TODO*/// *   0  | -x-------------- | maintain aspect ratio (when set, zoom y acts on both axis)
/*TODO*/// *   0  | --x------------- | flip y
/*TODO*/// *   0  | ---x------------ | flip x
/*TODO*/// *   0  | ----xxxx-------- | sprite size (see below)
/*TODO*/// *   0  | ---------xxxxxxx | priority order
/*TODO*/// *   1  | --xxxxxxxxxxxxxx | sprite code. We use an additional bit in TMNT2, but this is
/*TODO*/// *                           probably not accurate (protection related so we can't verify)
/*TODO*/// *   2  | ------xxxxxxxxxx | y position
/*TODO*/// *   3  | ------xxxxxxxxxx | x position
/*TODO*/// *   4  | xxxxxxxxxxxxxxxx | zoom y (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*/// *   5  | xxxxxxxxxxxxxxxx | zoom x (0x40 = normal, <0x40 = enlarge, >0x40 = reduce)
/*TODO*/// *   6  | ------x--------- | mirror y (top half is drawn as mirror image of the bottom)
/*TODO*/// *   6  | -------x-------- | mirror x (right half is drawn as mirror image of the left)
/*TODO*/// *   6  | --------x------- | shadow
/*TODO*/// *   6  | ---------xxxxxxx | "color", but depends on external connections
/*TODO*/// *   7  | ---------------- |
/*TODO*/// *
/*TODO*/// * shadow enables transparent shadows. Note that it applies to pen 0x0f ONLY.
/*TODO*/// * The rest of the sprite remains normal.
/*TODO*/// */
/*TODO*///
/*TODO*///void K053245_sprites_draw(struct osd_bitmap *bitmap,int min_priority,int max_priority)
/*TODO*///{
/*TODO*///#define NUM_SPRITES 128
/*TODO*///	int offs,pri_code;
/*TODO*///	int sortedlist[NUM_SPRITES];
/*TODO*///
/*TODO*///	for (offs = 0;offs < NUM_SPRITES;offs++)
/*TODO*///		sortedlist[offs] = -1;
/*TODO*///
/*TODO*///	/* prebuild a sorted table */
/*TODO*///	for (offs = 0;offs < 0x800;offs += 16)
/*TODO*///	{
/*TODO*///		if (READ_WORD(&K053245_ram[offs]) & 0x8000)
/*TODO*///			sortedlist[READ_WORD(&K053245_ram[offs]) & 0x007f] = offs;
/*TODO*///	}
/*TODO*///
/*TODO*///	for (pri_code = 0;pri_code < NUM_SPRITES;pri_code++)
/*TODO*///	{
/*TODO*///		int ox,oy,color,code,size,w,h,x,y,flipx,flipy,mirrorx,mirrory,zoomx,zoomy,pri;
/*TODO*///
/*TODO*///
/*TODO*///		offs = sortedlist[pri_code];
/*TODO*///		if (offs == -1) continue;
/*TODO*///
/*TODO*///		/* the following changes the sprite draw order from
/*TODO*///			 0  1  4  5 16 17 20 21
/*TODO*///			 2  3  6  7 18 19 22 23
/*TODO*///			 8  9 12 13 24 25 28 29
/*TODO*///			10 11 14 15 26 27 30 31
/*TODO*///			32 33 36 37 48 49 52 53
/*TODO*///			34 35 38 39 50 51 54 55
/*TODO*///			40 41 44 45 56 57 60 61
/*TODO*///			42 43 46 47 58 59 62 63
/*TODO*///
/*TODO*///			to
/*TODO*///
/*TODO*///			 0  1  2  3  4  5  6  7
/*TODO*///			 8  9 10 11 12 13 14 15
/*TODO*///			16 17 18 19 20 21 22 23
/*TODO*///			24 25 26 27 28 29 30 31
/*TODO*///			32 33 34 35 36 37 38 39
/*TODO*///			40 41 42 43 44 45 46 47
/*TODO*///			48 49 50 51 52 53 54 55
/*TODO*///			56 57 58 59 60 61 62 63
/*TODO*///		*/
/*TODO*///
/*TODO*///		/* NOTE: from the schematics, it looks like the top 2 bits should be ignored */
/*TODO*///		/* (there are not output pins for them), and probably taken from the "color" */
/*TODO*///		/* field to do bank switching. However this applies only to TMNT2, with its */
/*TODO*///		/* protection mcu creating the sprite table, so we don't know where to fetch */
/*TODO*///		/* the bits from. */
/*TODO*///		code = READ_WORD(&K053245_ram[offs+0x02]);
/*TODO*///		code = ((code & 0xffe1) + ((code & 0x0010) >> 2) + ((code & 0x0008) << 1)
/*TODO*///				 + ((code & 0x0004) >> 1) + ((code & 0x0002) << 2));
/*TODO*///		color = READ_WORD(&K053245_ram[offs+0x0c]) & 0x00ff;
/*TODO*///		pri = 0;
/*TODO*///
/*TODO*///		(*K053245_callback)(&code,&color,&pri);
/*TODO*///
/*TODO*///		if (pri < min_priority || pri > max_priority) continue;
/*TODO*///
/*TODO*///		size = (READ_WORD(&K053245_ram[offs]) & 0x0f00) >> 8;
/*TODO*///
/*TODO*///		w = 1 << (size & 0x03);
/*TODO*///		h = 1 << ((size >> 2) & 0x03);
/*TODO*///
/*TODO*///		/* zoom control:
/*TODO*///		   0x40 = normal scale
/*TODO*///		  <0x40 enlarge (0x20 = double size)
/*TODO*///		  >0x40 reduce (0x80 = half size)
/*TODO*///		*/
/*TODO*///		zoomy = READ_WORD(&K053245_ram[offs+0x08]);
/*TODO*///		if (zoomy > 0x2000) continue;
/*TODO*///		if (zoomy) zoomy = (0x400000+zoomy/2) / zoomy;
/*TODO*///		else zoomy = 2 * 0x400000;
/*TODO*///		if ((READ_WORD(&K053245_ram[offs]) & 0x4000) == 0)
/*TODO*///		{
/*TODO*///			zoomx = READ_WORD(&K053245_ram[offs+0x0a]);
/*TODO*///			if (zoomx > 0x2000) continue;
/*TODO*///			if (zoomx) zoomx = (0x400000+zoomx/2) / zoomx;
/*TODO*/////			else zoomx = 2 * 0x400000;
/*TODO*///else zoomx = zoomy; /* workaround for TMNT2 */
/*TODO*///		}
/*TODO*///		else zoomx = zoomy;
/*TODO*///
/*TODO*///		ox = READ_WORD(&K053245_ram[offs+0x06]) + K053245_spriteoffsX;
/*TODO*///		oy = READ_WORD(&K053245_ram[offs+0x04]);
/*TODO*///
/*TODO*///		flipx = READ_WORD(&K053245_ram[offs]) & 0x1000;
/*TODO*///		flipy = READ_WORD(&K053245_ram[offs]) & 0x2000;
/*TODO*///		mirrorx = READ_WORD(&K053245_ram[offs+0x0c]) & 0x0100;
/*TODO*///		mirrory = READ_WORD(&K053245_ram[offs+0x0c]) & 0x0200;
/*TODO*///
/*TODO*///		if (K053245_flipscreenX)
/*TODO*///		{
/*TODO*///			ox = 512 - ox;
/*TODO*///			if (!mirrorx) flipx = !flipx;
/*TODO*///		}
/*TODO*///		if (K053245_flipscreenY)
/*TODO*///		{
/*TODO*///			oy = -oy;
/*TODO*///			if (!mirrory) flipy = !flipy;
/*TODO*///		}
/*TODO*///
/*TODO*///		ox = (ox + 0x5d) & 0x3ff;
/*TODO*///		if (ox >= 768) ox -= 1024;
/*TODO*///		oy = (-(oy + K053245_spriteoffsY + 0x07)) & 0x3ff;
/*TODO*///		if (oy >= 640) oy -= 1024;
/*TODO*///
/*TODO*///		/* the coordinates given are for the *center* of the sprite */
/*TODO*///		ox -= (zoomx * w) >> 13;
/*TODO*///		oy -= (zoomy * h) >> 13;
/*TODO*///
/*TODO*///		for (y = 0;y < h;y++)
/*TODO*///		{
/*TODO*///			int sx,sy,zw,zh;
/*TODO*///
/*TODO*///			sy = oy + ((zoomy * y + (1<<11)) >> 12);
/*TODO*///			zh = (oy + ((zoomy * (y+1) + (1<<11)) >> 12)) - sy;
/*TODO*///
/*TODO*///			for (x = 0;x < w;x++)
/*TODO*///			{
/*TODO*///				int c,fx,fy;
/*TODO*///
/*TODO*///				sx = ox + ((zoomx * x + (1<<11)) >> 12);
/*TODO*///				zw = (ox + ((zoomx * (x+1) + (1<<11)) >> 12)) - sx;
/*TODO*///				c = code;
/*TODO*///				if (mirrorx)
/*TODO*///				{
/*TODO*///					if ((flipx == 0) ^ (2*x < w))
/*TODO*///					{
/*TODO*///						/* mirror left/right */
/*TODO*///						c += (w-x-1);
/*TODO*///						fx = 1;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						c += x;
/*TODO*///						fx = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (flipx) c += w-1-x;
/*TODO*///					else c += x;
/*TODO*///					fx = flipx;
/*TODO*///				}
/*TODO*///				if (mirrory)
/*TODO*///				{
/*TODO*///					if ((flipy == 0) ^ (2*y >= h))
/*TODO*///					{
/*TODO*///						/* mirror top/bottom */
/*TODO*///						c += 8*(h-y-1);
/*TODO*///						fy = 1;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						c += 8*y;
/*TODO*///						fy = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if (flipy) c += 8*(h-1-y);
/*TODO*///					else c += 8*y;
/*TODO*///					fy = flipy;
/*TODO*///				}
/*TODO*///
/*TODO*///				/* the sprite can start at any point in the 8x8 grid, but it must stay */
/*TODO*///				/* in a 64 entries window, wrapping around at the edges. The animation */
/*TODO*///				/* at the end of the saloon level in SUnset Riders breaks otherwise. */
/*TODO*///				c = (c & 0x3f) | (code & ~0x3f);
/*TODO*///
/*TODO*///				if (zoomx == 0x10000 && zoomy == 0x10000)
/*TODO*///				{
/*TODO*///					/* hack to simulate shadow */
/*TODO*///					if (READ_WORD(&K053245_ram[offs+0x0c]) & 0x0080)
/*TODO*///					{
/*TODO*///						int o = K053245_gfx->colortable[16*color+15];
/*TODO*///						K053245_gfx->colortable[16*color+15] = palette_transparent_pen;
/*TODO*///						drawgfx(bitmap,K053245_gfx,
/*TODO*///								c,
/*TODO*///								color,
/*TODO*///								fx,fy,
/*TODO*///								sx,sy,
/*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PENS,(cpu_getcurrentframe() & 1) ? 0x8001 : 0x0001);
/*TODO*///						K053245_gfx->colortable[16*color+15] = o;
/*TODO*///					}
/*TODO*///					else
/*TODO*///						drawgfx(bitmap,K053245_gfx,
/*TODO*///								c,
/*TODO*///								color,
/*TODO*///								fx,fy,
/*TODO*///								sx,sy,
/*TODO*///								&Machine->drv->visible_area,TRANSPARENCY_PEN,0);
/*TODO*///				}
/*TODO*///				else
/*TODO*///					drawgfxzoom(bitmap,K053245_gfx,
/*TODO*///							c,
/*TODO*///							color,
/*TODO*///							fx,fy,
/*TODO*///							sx,sy,
/*TODO*///							&Machine->drv->visible_area,TRANSPARENCY_PEN,0,
/*TODO*///							(zw << 16) / 16,(zh << 16) / 16);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///#if 0
/*TODO*///if (keyboard_pressed(KEYCODE_D))
/*TODO*///{
/*TODO*///	FILE *fp;
/*TODO*///	fp=fopen("SPRITE.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(K053245_ram, 0x800, 1, fp);
/*TODO*///		usrintf_showmessage("saved");
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///}
/*TODO*///#endif
/*TODO*///#undef NUM_SPRITES
/*TODO*///}
/*TODO*///
/*TODO*///void K053245_mark_sprites_colors(void)
/*TODO*///{
/*TODO*///	int offs,i;
/*TODO*///
/*TODO*///	unsigned short palette_map[512];
/*TODO*///
/*TODO*///	memset (palette_map, 0, sizeof (palette_map));
/*TODO*///
/*TODO*///	/* sprites */
/*TODO*///	for (offs = 0x800-16;offs >= 0;offs -= 16)
/*TODO*///	{
/*TODO*///		if (READ_WORD(&K053245_ram[offs]) & 0x8000)
/*TODO*///		{
/*TODO*///			int code,color,pri;
/*TODO*///
/*TODO*///			code = READ_WORD(&K053245_ram[offs+0x02]);
/*TODO*///			code = ((code & 0xffe1) + ((code & 0x0010) >> 2) + ((code & 0x0008) << 1)
/*TODO*///					 + ((code & 0x0004) >> 1) + ((code & 0x0002) << 2));
/*TODO*///			color = READ_WORD(&K053245_ram[offs+0x0c]) & 0x00ff;
/*TODO*///			pri = 0;
/*TODO*///			(*K053245_callback)(&code,&color,&pri);
/*TODO*///			palette_map[color] |= 0xffff;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* now build the final table */
/*TODO*///	for (i = 0; i < 512; i++)
/*TODO*///	{
/*TODO*///		int usage = palette_map[i], j;
/*TODO*///		if (usage)
/*TODO*///		{
/*TODO*///			for (j = 1; j < 16; j++)
/*TODO*///				if (usage & (1 << j))
/*TODO*///					palette_used_colors[i * 16 + j] |= PALETTE_COLOR_VISIBLE;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
//K053247_callback interface
    public static abstract interface K053247_callbackProcPtr {

        public abstract void handler(int[] code, int[] color, int[] priority);
    }

    public static int K053247_memory_region;
    public static GfxElement K053247_gfx;
    public static K053247_callbackProcPtr K053247_callback;
    public static int K053246_OBJCHA_line;
    public static int K053246_romoffset;
    public static int K053247_flipscreenX, K053247_flipscreenY;
    public static int K053247_spriteoffsX, K053247_spriteoffsY;
    public static UBytePtr K053247_ram;
    public static int K053247_irq_enabled;

    public static int K053247_vh_start(int gfx_memory_region, int plane0, int plane1, int plane2, int plane3, K053247_callbackProcPtr callback) {
        int gfx_index;
        GfxLayout spritelayout = new GfxLayout(
                16, 16,
                0, /* filled in later */
                4,
                new int[]{0, 0, 0, 0}, /* filled in later */
                new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4,
                    10 * 4, 11 * 4, 8 * 4, 9 * 4, 14 * 4, 15 * 4, 12 * 4, 13 * 4},
                new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                    8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
                128 * 8
        );


        /* find first empty slot to decode gfx */
        for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++) {
            if (Machine.gfx[gfx_index] == null) {
                break;
            }
        }
        if (gfx_index == MAX_GFX_ELEMENTS) {
            return 1;
        }

        /* tweak the structure for the number of tiles we have */
        spritelayout.total = memory_region_length(gfx_memory_region) / 128;
        spritelayout.planeoffset[0] = plane0;
        spritelayout.planeoffset[1] = plane1;
        spritelayout.planeoffset[2] = plane2;
        spritelayout.planeoffset[3] = plane3;

        /* decode the graphics */
        Machine.gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region), spritelayout);
        if (Machine.gfx[gfx_index] == null) {
            return 1;
        }

        /* set the color information */
        Machine.gfx[gfx_index].colortable = new UShortArray(Machine.remapped_colortable);
        Machine.gfx[gfx_index].total_colors = Machine.drv.color_table_len / 16;

        K053247_memory_region = gfx_memory_region;
        K053247_gfx = Machine.gfx[gfx_index];
        K053247_callback = callback;
        K053246_OBJCHA_line = CLEAR_LINE;
        K053247_ram = new UBytePtr(0x1000);

        memset(K053247_ram, 0, 0x1000);

        return 0;
    }

    public static void K053247_vh_stop() {
        K053247_ram = null;
    }
    public static ReadHandlerPtr K053247_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int read = K053247_ram.READ_WORD(offset);
            /*if (konamiicclog != null) {
                fprintf(konamiicclog, "K053247_word_r offset=%d return=%d\n", offset, read);
            }*/
            return read;
        }
    };
    public static WriteHandlerPtr K053247_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            COMBINE_WORD_MEM(K053247_ram, offset, data);
        }
    };
    public static ReadHandlerPtr K053247_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int shift = ((offset & 1) ^ 1) << 3;
            int read = (K053247_ram.READ_WORD(offset & ~1) >>> shift) & 0xff;  //unsigned shift?
            /*if (konamiicclog != null) {
                fprintf(konamiicclog, "K053247_r offset=%d shift=%d return=%d\n", offset, shift, read);
            }*/
            return read;
        }
    };
    public static WriteHandlerPtr K053247_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int shift = ((offset & 1) ^ 1) << 3;
            offset &= ~1;
            COMBINE_WORD_MEM(K053247_ram, offset, (0xff000000 >>> shift) | ((data & 0xff) << shift));//unsigned shift
        }
    };

    public static ReadHandlerPtr K053246_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (K053246_OBJCHA_line == ASSERT_LINE) {
                int addr;

                addr = 2 * K053246_romoffset + ((offset & 1) ^ 1);
                addr &= memory_region_length(K053247_memory_region) - 1;

                return memory_region(K053247_memory_region).read(addr);
            } else {
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: read from unknown 053244 address %x\n", cpu_get_pc(), offset);
                }
                return 0;
            }
        }
    };

    public static WriteHandlerPtr K053246_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0x00) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0x00ff) | (data << 8);
            } else if (offset == 0x01) {
                K053247_spriteoffsX = (K053247_spriteoffsX & 0xff00) | data;
            } else if (offset == 0x02) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0x00ff) | (data << 8);
            } else if (offset == 0x03) {
                K053247_spriteoffsY = (K053247_spriteoffsY & 0xff00) | data;
            } else if (offset == 0x05) {
                /* bit 0/1 = flip screen */
                K053247_flipscreenX = data & 0x01;
                K053247_flipscreenY = data & 0x02;

                /* bit 2 = unknown */

 /* bit 4 = interrupt enable */
                K053247_irq_enabled = data & 0x10;

                /* bit 5 = unknown */
                if (errorlog != null) {
                    fprintf(errorlog, "%04x: write %02x to 053246 address 5\n", cpu_get_pc(), data);
                }
            } else if (offset >= 0x04 && offset < 0x08) /* only 4,6,7 - 5 is handled above */ {
                offset = 8 * (((offset & 0x03) ^ 0x01) - 1);
                K053246_romoffset = (K053246_romoffset & ~(0xff << offset)) | (data << offset);
                return;
            } else if (errorlog != null) {
                fprintf(errorlog, "%04x: write %02x to unknown 053246 address %x\n", cpu_get_pc(), data, offset);
            }
        }
    };
    public static ReadHandlerPtr K053246_word_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            return K053246_r.handler(offset + 1) | (K053246_r.handler(offset) << 8);
        }
    };
    public static WriteHandlerPtr K053246_word_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if ((data & 0xff000000) == 0) {
                K053246_w.handler(offset, (data >>> 8) & 0xff);
            }
            if ((data & 0x00ff0000) == 0) {
                K053246_w.handler(offset + 1, data & 0xff);
            }
        }
    };

    public static void K053246_set_OBJCHA_line(int state) {
        K053246_OBJCHA_line = state;
    }

    static int offsetkludge;

    public static void K053247_sprites_draw(osd_bitmap bitmap, int min_priority, int max_priority) {
        int NUM_SPRITES = 256;
        int offs, pri_code;
        int[] sortedlist = new int[NUM_SPRITES];

        for (offs = 0; offs < NUM_SPRITES; offs++) {
            sortedlist[offs] = -1;
        }

        /* prebuild a sorted table */
        for (offs = 0; offs < 0x1000; offs += 16) {
//		if (READ_WORD(&K053247_ram[offs]) & 0x8000)
            sortedlist[K053247_ram.READ_WORD(offs) & 0x00ff] = offs;
        }

        for (pri_code = NUM_SPRITES - 1; pri_code >= 0; pri_code--) {
            int ox, oy, size, w, h, x, y, xa, ya, flipx, flipy, mirrorx, mirrory, zoomx, zoomy;
            int color[] = new int[1];
            int code[] = new int[1];
            int pri[] = new int[1];
            /* sprites can be grouped up to 8x8. The draw order is
             0  1  4  5 16 17 20 21
             2  3  6  7 18 19 22 23
             8  9 12 13 24 25 28 29
             10 11 14 15 26 27 30 31
             32 33 36 37 48 49 52 53
             34 35 38 39 50 51 54 55
             40 41 44 45 56 57 60 61
             42 43 46 47 58 59 62 63
             */
            int xoffset[] = {0, 1, 4, 5, 16, 17, 20, 21};
            int yoffset[] = {0, 2, 8, 10, 32, 34, 40, 42};

            offs = sortedlist[pri_code];
            if (offs == -1) {
                continue;
            }

            if ((K053247_ram.READ_WORD(offs) & 0x8000) == 0) {
                continue;
            }

            code[0] = K053247_ram.READ_WORD(offs + 0x02);
            color[0] = K053247_ram.READ_WORD(offs + 0x0c);
            pri[0] = 0;

            K053247_callback.handler(code, color, pri);

            if (pri[0] < min_priority || pri[0] > max_priority) {
                continue;
            }

            size = (K053247_ram.READ_WORD(offs) & 0x0f00) >> 8;

            w = 1 << (size & 0x03);
            h = 1 << ((size >> 2) & 0x03);

            /* the sprite can start at any point in the 8x8 grid. We have to */
 /* adjust the offsets to draw it correctly. Simpsons does this all the time. */
            xa = 0;
            ya = 0;
            if ((code[0] & 0x01) != 0) {
                xa += 1;
            }
            if ((code[0] & 0x02) != 0) {
                ya += 1;
            }
            if ((code[0] & 0x04) != 0) {
                xa += 2;
            }
            if ((code[0] & 0x08) != 0) {
                ya += 2;
            }
            if ((code[0] & 0x10) != 0) {
                xa += 4;
            }
            if ((code[0] & 0x20) != 0) {
                ya += 4;
            }
            code[0] &= ~0x3f;


            /* zoom control:
             0x40 = normal scale
             <0x40 enlarge (0x20 = double size)
             >0x40 reduce (0x80 = half size)
             */
            zoomy = K053247_ram.READ_WORD(offs + 0x08);
            if (zoomy > 0x2000) {
                continue;
            }
            if (zoomy != 0) {
                zoomy = (0x400000 + zoomy / 2) / zoomy;
            } else {
                zoomy = 2 * 0x400000;
            }
            if ((K053247_ram.READ_WORD(offs) & 0x4000) == 0) {
                zoomx = K053247_ram.READ_WORD(offs + 0x0a);
                if (zoomx > 0x2000) {
                    continue;
                }
                if (zoomx != 0) {
                    zoomx = (0x400000 + zoomx / 2) / zoomx;
                } else {
                    zoomx = 2 * 0x400000;
                }
            } else {
                zoomx = zoomy;
            }

            ox = K053247_ram.READ_WORD(offs + 0x06);
            oy = K053247_ram.READ_WORD(offs + 0x04);

            /* TODO: it is not known how the global Y offset works */
            switch (K053247_spriteoffsY) {
                case 0x0261:
                /* simpsons */

                case 0x0262:
                /* simpsons (dreamland) */

                case 0x0263:
                /* simpsons (dreamland) */

                case 0x0264:
                /* simpsons (dreamland) */

                case 0x0265:
                /* simpsons (dreamland) */

                case 0x006d:
                /* simpsons flip (dreamland) */

                case 0x006e:
                /* simpsons flip (dreamland) */

                case 0x006f:
                /* simpsons flip (dreamland) */

                case 0x0070:
                /* simpsons flip (dreamland) */

                case 0x0071:
                    /* simpsons flip */

                    offsetkludge = 0x017;
                    break;
                case 0x02f7:
                /* vendetta (level 4 boss) */

                case 0x02f8:
                /* vendetta (level 4 boss) */

                case 0x02f9:
                /* vendetta (level 4 boss) */

                case 0x02fa:
                /* vendetta */

                case 0x02fb:
                /* vendetta (fat guy jumping) */

                case 0x02fc:
                /* vendetta (fat guy jumping) */

                case 0x02fd:
                /* vendetta (fat guy jumping) */

                case 0x02fe:
                /* vendetta (fat guy jumping) */

                case 0x02ff:
                /* vendetta (fat guy jumping) */

                case 0x03f7:
                /* vendetta flip (level 4 boss) */

                case 0x03f8:
                /* vendetta flip (level 4 boss) */

                case 0x03f9:
                /* vendetta flip (level 4 boss) */

                case 0x03fa:
                /* vendetta flip */

                case 0x03fb:
                /* vendetta flip (fat guy jumping) */

                case 0x03fc:
                /* vendetta flip (fat guy jumping) */

                case 0x03fd:
                /* vendetta flip (fat guy jumping) */

                case 0x03fe:
                /* vendetta flip (fat guy jumping) */

                case 0x03ff:
                    /* vendetta flip (fat guy jumping) */

                    offsetkludge = 0x006;
                    break;
                case 0x0292:
                /* xmen */

                case 0x0072:
                    /* xmen flip */

                    offsetkludge = -0x002;
                    break;
                default:
                    offsetkludge = 0;
                    //usrintf_showmessage("unknown spriteoffsY %04x",K053247_spriteoffsY);
                    break;
            }

            flipx = K053247_ram.READ_WORD(offs) & 0x1000;
            flipy = K053247_ram.READ_WORD(offs) & 0x2000;
            mirrorx = K053247_ram.READ_WORD(offs + 0x0c) & 0x4000;
            mirrory = K053247_ram.READ_WORD(offs + 0x0c) & 0x8000;

            if (K053247_flipscreenX != 0) {
                ox = -ox;
                if (mirrorx == 0) {
                    flipx = NOT(flipx);
                }
            }
            if (K053247_flipscreenY != 0) {
                oy = -oy;
                if (mirrory == 0) {
                    flipy = NOT(flipy);
                }
            }

            ox = (ox + 0x35 - K053247_spriteoffsX) & 0x3ff;
            if (ox >= 768) {
                ox -= 1024;
            }
            oy = (-(oy + K053247_spriteoffsY + offsetkludge)) & 0x3ff;
            if (oy >= 640) {
                oy -= 1024;
            }

            /* the coordinates given are for the *center* of the sprite */
            ox -= (zoomx * w) >> 13;
            oy -= (zoomy * h) >> 13;

            for (y = 0; y < h; y++) {
                int sx, sy, zw, zh;

                sy = oy + ((zoomy * y + (1 << 11)) >> 12);
                zh = (oy + ((zoomy * (y + 1) + (1 << 11)) >> 12)) - sy;

                for (x = 0; x < w; x++) {
                    int c, fx, fy;

                    sx = ox + ((zoomx * x + (1 << 11)) >> 12);
                    zw = (ox + ((zoomx * (x + 1) + (1 << 11)) >> 12)) - sx;
                    c = code[0];
                    if (mirrorx != 0) {
                        if ((flipx == 0) ^ (2 * x < w)) {
                            /* mirror left/right */
                            c += xoffset[(w - 1 - x + xa) & 7];
                            fx = 1;
                        } else {
                            c += xoffset[(x + xa) & 7];
                            fx = 0;
                        }
                    } else {
                        if (flipx != 0) {
                            c += xoffset[(w - 1 - x + xa) & 7];
                        } else {
                            c += xoffset[(x + xa) & 7];
                        }
                        fx = flipx;
                    }
                    if (mirrory != 0) {
                        if ((flipy == 0) ^ (2 * y >= h)) {
                            /* mirror top/bottom */
                            c += yoffset[(h - 1 - y + ya) & 7];
                            fy = 1;
                        } else {
                            c += yoffset[(y + ya) & 7];
                            fy = 0;
                        }
                    } else {
                        if (flipy != 0) {
                            c += yoffset[(h - 1 - y + ya) & 7];
                        } else {
                            c += yoffset[(y + ya) & 7];
                        }
                        fy = flipy;
                    }

                    if (zoomx == 0x10000 && zoomy == 0x10000) {
                        /* hack to simulate shadow */
                        if ((K053247_ram.READ_WORD(offs + 0x0c) & 0x0400) != 0) {
                            int o = K053247_gfx.colortable.read(16 * color[0] + 15);
                            K053247_gfx.colortable.write(16 * color[0] + 15, palette_transparent_pen);
                            drawgfx(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, fy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PENS, (cpu_getcurrentframe() & 1) != 0 ? 0x8001 : 0x0001);
                            K053247_gfx.colortable.write(16 * color[0] + 15, o);
                        } else {
                            drawgfx(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, fy,
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                        }
                    } else {
                        drawgfxzoom(bitmap, K053247_gfx,
                                c,
                                color[0],
                                fx, fy,
                                sx, sy,
                                Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                (zw << 16) / 16, (zh << 16) / 16);
                    }
                    if (mirrory != 0 && h == 1) /* Simpsons shadows */ {
                        if (zoomx == 0x10000 && zoomy == 0x10000) {
                            /* hack to simulate shadow */
                            if ((K053247_ram.READ_WORD(offs + 0x0c) & 0x0400) != 0) {
                                int o = K053247_gfx.colortable.read(16 * color[0] + 15);
                                K053247_gfx.colortable.write(16 * color[0] + 15, palette_transparent_pen);
                                drawgfx(bitmap, K053247_gfx,
                                        c,
                                        color[0],
                                        fx, NOT(fy),
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PENS, (cpu_getcurrentframe() & 1) != 0 ? 0x8001 : 0x0001);
                                K053247_gfx.colortable.write(16 * color[0] + 15, o);
                            } else {
                                drawgfx(bitmap, K053247_gfx,
                                        c,
                                        color[0],
                                        fx, NOT(fy),
                                        sx, sy,
                                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
                            }
                        } else {
                            drawgfxzoom(bitmap, K053247_gfx,
                                    c,
                                    color[0],
                                    fx, NOT(fy),
                                    sx, sy,
                                    Machine.drv.visible_area, TRANSPARENCY_PEN, 0,
                                    (zw << 16) / 16, (zh << 16) / 16);
                        }
                    }
                }
            }
        }
    }

    public static void K053247_mark_sprites_colors() {
        int offs, i;

        /*unsigned short*/ int[] palette_map = new int[512];

        //memset (palette_map, 0, sizeof (palette_map));

        /* sprites */
        for (offs = 0x1000 - 16; offs >= 0; offs -= 16) {
            if ((K053247_ram.READ_WORD(offs) & 0x8000) != 0) {
                int[] code = new int[1];
                int[] color = new int[1];
                int[] pri = new int[1];

                code[0] = K053247_ram.READ_WORD(offs + 0x02);
                color[0] = K053247_ram.READ_WORD(offs + 0x0c);
                pri[0] = 0;
                K053247_callback.handler(code, color, pri);
                palette_map[color[0]] |= 0xffff;
            }
        }

        /* now build the final table */
        for (i = 0; i < 512; i++) {
            int usage = palette_map[i], j;
            if (usage != 0) {
                for (j = 1; j < 16; j++) {
                    if ((usage & (1 << j)) != 0) {
                        palette_used_colors.write(i * 16 + j, palette_used_colors.read(i * 16 + j) | PALETTE_COLOR_VISIBLE);
                    }
                }
            }
        }
    }

    public static int K053247_is_IRQ_enabled() {
        return K053247_irq_enabled;
    }

    /*TODO*///
/*TODO*///
/*TODO*///#define MAX_K051316 3
/*TODO*///
/*TODO*///static int K051316_memory_region[MAX_K051316];
/*TODO*///static int K051316_gfxnum[MAX_K051316];
/*TODO*///static int K051316_wraparound[MAX_K051316];
/*TODO*///static int K051316_offset[MAX_K051316][2];
/*TODO*///static int K051316_bpp[MAX_K051316];
/*TODO*///static void (*K051316_callback[MAX_K051316])(int *code,int *color);
/*TODO*///static unsigned char *K051316_ram[MAX_K051316];
/*TODO*///static unsigned char K051316_ctrlram[MAX_K051316][16];
/*TODO*///static struct tilemap *K051316_tilemap[MAX_K051316];
/*TODO*///static int K051316_chip_selected;
/*TODO*///
/*TODO*///void K051316_vh_stop(int chip);
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Callbacks for the TileMap code
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///static void K051316_preupdate(int chip)
/*TODO*///{
/*TODO*///	K051316_chip_selected = chip;
/*TODO*///}
/*TODO*///
/*TODO*///static void K051316_get_tile_info(int col,int row)
/*TODO*///{
/*TODO*///	int tile_index = 32*row+col;
/*TODO*///	int code = K051316_ram[K051316_chip_selected][tile_index];
/*TODO*///	int color = K051316_ram[K051316_chip_selected][tile_index + 0x400];
/*TODO*///
/*TODO*///	(*K051316_callback[K051316_chip_selected])(&code,&color);
/*TODO*///
/*TODO*///	SET_TILE_INFO(K051316_gfxnum[K051316_chip_selected],code,color);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int K051316_vh_start(int chip, int gfx_memory_region,int bpp,
/*TODO*///		void (*callback)(int *code,int *color))
/*TODO*///{
/*TODO*///	int gfx_index;
/*TODO*///
/*TODO*///
/*TODO*///	/* find first empty slot to decode gfx */
/*TODO*///	for (gfx_index = 0; gfx_index < MAX_GFX_ELEMENTS; gfx_index++)
/*TODO*///		if (Machine->gfx[gfx_index] == 0)
/*TODO*///			break;
/*TODO*///	if (gfx_index == MAX_GFX_ELEMENTS)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (bpp == 4)
/*TODO*///	{
/*TODO*///		static struct GfxLayout charlayout =
/*TODO*///		{
/*TODO*///			16,16,
/*TODO*///			0,				/* filled in later */
/*TODO*///			4,
/*TODO*///			{ 0, 1, 2, 3 },
/*TODO*///			{ 0*4, 1*4, 2*4, 3*4, 4*4, 5*4, 6*4, 7*4,
/*TODO*///					8*4, 9*4, 10*4, 11*4, 12*4, 13*4, 14*4, 15*4 },
/*TODO*///			{ 0*64, 1*64, 2*64, 3*64, 4*64, 5*64, 6*64, 7*64,
/*TODO*///					8*64, 9*64, 10*64, 11*64, 12*64, 13*64, 14*64, 15*64 },
/*TODO*///			128*8
/*TODO*///		};
/*TODO*///
/*TODO*///
/*TODO*///		/* tweak the structure for the number of tiles we have */
/*TODO*///		charlayout.total = memory_region_length(gfx_memory_region) / 128;
/*TODO*///
/*TODO*///		/* decode the graphics */
/*TODO*///		Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&charlayout);
/*TODO*///	}
/*TODO*///	else if (bpp == 7)
/*TODO*///	{
/*TODO*///		static struct GfxLayout charlayout =
/*TODO*///		{
/*TODO*///			16,16,
/*TODO*///			0,				/* filled in later */
/*TODO*///			7,
/*TODO*///			{ 1, 2, 3, 4, 5, 6, 7 },
/*TODO*///			{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
/*TODO*///					8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
/*TODO*///			{ 0*128, 1*128, 2*128, 3*128, 4*128, 5*128, 6*128, 7*128,
/*TODO*///					8*128, 9*128, 10*128, 11*128, 12*128, 13*128, 14*128, 15*128 },
/*TODO*///			256*8
/*TODO*///		};
/*TODO*///
/*TODO*///
/*TODO*///		/* tweak the structure for the number of tiles we have */
/*TODO*///		charlayout.total = memory_region_length(gfx_memory_region) / 256;
/*TODO*///
/*TODO*///		/* decode the graphics */
/*TODO*///		Machine->gfx[gfx_index] = decodegfx(memory_region(gfx_memory_region),&charlayout);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///if (errorlog) fprintf(errorlog,"K051316_vh_start supports only 4 or 7 bpp\n");
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!Machine->gfx[gfx_index])
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	/* set the color information */
/*TODO*///	Machine->gfx[gfx_index]->colortable = Machine->remapped_colortable;
/*TODO*///	Machine->gfx[gfx_index]->total_colors = Machine->drv->color_table_len / (1 << bpp);
/*TODO*///
/*TODO*///	K051316_memory_region[chip] = gfx_memory_region;
/*TODO*///	K051316_gfxnum[chip] = gfx_index;
/*TODO*///	K051316_bpp[chip] = bpp;
/*TODO*///	K051316_callback[chip] = callback;
/*TODO*///
/*TODO*///	K051316_tilemap[chip] = tilemap_create(K051316_get_tile_info,TILEMAP_OPAQUE,16,16,32,32);
/*TODO*///
/*TODO*///	K051316_ram[chip] = malloc(0x800);
/*TODO*///
/*TODO*///	if (!K051316_ram[chip] || !K051316_tilemap[chip])
/*TODO*///	{
/*TODO*///		K051316_vh_stop(chip);
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	tilemap_set_clip(K051316_tilemap[chip],0);
/*TODO*///
/*TODO*///	K051316_wraparound[chip] = 0;	/* default = no wraparound */
/*TODO*///	K051316_offset[chip][0] = K051316_offset[chip][1] = 0;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_vh_start_0(int gfx_memory_region,int bpp,
/*TODO*///		void (*callback)(int *code,int *color))
/*TODO*///{
/*TODO*///	return K051316_vh_start(0,gfx_memory_region,bpp,callback);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_vh_start_1(int gfx_memory_region,int bpp,
/*TODO*///		void (*callback)(int *code,int *color))
/*TODO*///{
/*TODO*///	return K051316_vh_start(1,gfx_memory_region,bpp,callback);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_vh_start_2(int gfx_memory_region,int bpp,
/*TODO*///		void (*callback)(int *code,int *color))
/*TODO*///{
/*TODO*///	return K051316_vh_start(2,gfx_memory_region,bpp,callback);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void K051316_vh_stop(int chip)
/*TODO*///{
/*TODO*///	free(K051316_ram[chip]);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_vh_stop_0(void)
/*TODO*///{
/*TODO*///	K051316_vh_stop(0);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_vh_stop_1(void)
/*TODO*///{
/*TODO*///	K051316_vh_stop(1);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_vh_stop_2(void)
/*TODO*///{
/*TODO*///	K051316_vh_stop(2);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_r(int chip, int offset)
/*TODO*///{
/*TODO*///	return K051316_ram[chip][offset];
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_0_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_r(0, offset);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_1_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_r(1, offset);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_2_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_r(2, offset);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void K051316_w(int chip,int offset,int data)
/*TODO*///{
/*TODO*///	if (K051316_ram[chip][offset] != data)
/*TODO*///	{
/*TODO*///		K051316_ram[chip][offset] = data;
/*TODO*///		tilemap_mark_tile_dirty(K051316_tilemap[chip],offset%32,(offset%0x400)/32);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_0_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_w(0,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_1_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_w(1,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_2_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_w(2,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int K051316_rom_r(int chip, int offset)
/*TODO*///{
/*TODO*///	if ((K051316_ctrlram[chip][0x0e] & 0x01) == 0)
/*TODO*///	{
/*TODO*///		int addr;
/*TODO*///
/*TODO*///		addr = offset + (K051316_ctrlram[chip][0x0c] << 11) + (K051316_ctrlram[chip][0x0d] << 19);
/*TODO*///		if (K051316_bpp[chip] <= 4) addr /= 2;
/*TODO*///		addr &= memory_region_length(K051316_memory_region[chip])-1;
/*TODO*///
/*TODO*///#if 0
/*TODO*///	usrintf_showmessage("%04x: offset %04x addr %04x",cpu_get_pc(),offset,addr);
/*TODO*///#endif
/*TODO*///
/*TODO*///		return memory_region(K051316_memory_region[chip])[addr];
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 051316 ROM offset %04x but reg 0x0c bit 0 not clear\n",cpu_get_pc(),offset);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_rom_0_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_rom_r(0,offset);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_rom_1_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_rom_r(1,offset);
/*TODO*///}
/*TODO*///
/*TODO*///int K051316_rom_2_r(int offset)
/*TODO*///{
/*TODO*///	return K051316_rom_r(2,offset);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void K051316_ctrl_w(int chip,int offset,int data)
/*TODO*///{
/*TODO*///	K051316_ctrlram[chip][offset] = data;
/*TODO*///if (errorlog && offset >= 0x0c) fprintf(errorlog,"%04x: write %02x to 051316 reg %x\n",cpu_get_pc(),data,offset);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_ctrl_0_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_ctrl_w(0,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_ctrl_1_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_ctrl_w(1,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_ctrl_2_w(int offset,int data)
/*TODO*///{
/*TODO*///	K051316_ctrl_w(2,offset,data);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_wraparound_enable(int chip, int status)
/*TODO*///{
/*TODO*///	K051316_wraparound[chip] = status;
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_set_offset(int chip, int xoffs, int yoffs)
/*TODO*///{
/*TODO*///	K051316_offset[chip][0] = xoffs;
/*TODO*///	K051316_offset[chip][1] = yoffs;
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_tilemap_update(int chip)
/*TODO*///{
/*TODO*///	K051316_preupdate(chip);
/*TODO*///	tilemap_update(K051316_tilemap[chip]);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_tilemap_update_0(void)
/*TODO*///{
/*TODO*///	K051316_tilemap_update(0);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_tilemap_update_1(void)
/*TODO*///{
/*TODO*///	K051316_tilemap_update(1);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_tilemap_update_2(void)
/*TODO*///{
/*TODO*///	K051316_tilemap_update(2);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* Note: rotation support doesn't handle asymmetrical visible areas. This doesn't */
/*TODO*////* matter because in the Konami games the visible area is always symmetrical. */
/*TODO*///void K051316_zoom_draw(int chip, struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	UINT32 startx,starty,cx,cy;
/*TODO*///	int incxx,incxy,incyx,incyy;
/*TODO*///	int x,sx,sy,ex,ey;
/*TODO*///	struct osd_bitmap *srcbitmap = K051316_tilemap[chip]->pixmap;
/*TODO*///
/*TODO*///	startx = 256 * ((INT16)(256 * K051316_ctrlram[chip][0x00] + K051316_ctrlram[chip][0x01]));
/*TODO*///	incxx  =        (INT16)(256 * K051316_ctrlram[chip][0x02] + K051316_ctrlram[chip][0x03]);
/*TODO*///	incyx  =        (INT16)(256 * K051316_ctrlram[chip][0x04] + K051316_ctrlram[chip][0x05]);
/*TODO*///	starty = 256 * ((INT16)(256 * K051316_ctrlram[chip][0x06] + K051316_ctrlram[chip][0x07]));
/*TODO*///	incxy  =        (INT16)(256 * K051316_ctrlram[chip][0x08] + K051316_ctrlram[chip][0x09]);
/*TODO*///	incyy  =        (INT16)(256 * K051316_ctrlram[chip][0x0a] + K051316_ctrlram[chip][0x0b]);
/*TODO*///
/*TODO*///	startx += (Machine->drv->visible_area.min_y - (16 + K051316_offset[chip][1])) * incyx;
/*TODO*///	starty += (Machine->drv->visible_area.min_y - (16 + K051316_offset[chip][1])) * incyy;
/*TODO*///
/*TODO*///	startx += (Machine->drv->visible_area.min_x - (89 + K051316_offset[chip][0])) * incxx;
/*TODO*///	starty += (Machine->drv->visible_area.min_x - (89 + K051316_offset[chip][0])) * incxy;
/*TODO*///
/*TODO*///	sx = Machine->drv->visible_area.min_x;
/*TODO*///	sy = Machine->drv->visible_area.min_y;
/*TODO*///	ex = Machine->drv->visible_area.max_x;
/*TODO*///	ey = Machine->drv->visible_area.max_y;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int t;
/*TODO*///
/*TODO*///		t = startx; startx = starty; starty = t;
/*TODO*///		t = sx; sx = sy; sy = t;
/*TODO*///		t = ex; ex = ey; ey = t;
/*TODO*///		t = incxx; incxx = incyy; incyy = t;
/*TODO*///		t = incxy; incxy = incyx; incyx = t;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_X)
/*TODO*///	{
/*TODO*///		int w = ex - sx;
/*TODO*///
/*TODO*///		incxy = -incxy;
/*TODO*///		incyx = -incyx;
/*TODO*///		startx = 0xfffff - startx;
/*TODO*///		startx -= incxx * w;
/*TODO*///		starty -= incxy * w;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_FLIP_Y)
/*TODO*///	{
/*TODO*///		int h = ey - sy;
/*TODO*///
/*TODO*///		incxy = -incxy;
/*TODO*///		incyx = -incyx;
/*TODO*///		starty = 0xfffff - starty;
/*TODO*///		startx -= incyx * h;
/*TODO*///		starty -= incyy * h;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (bitmap->depth == 8)
/*TODO*///	{
/*TODO*///		unsigned char *dest;
/*TODO*///
/*TODO*///		if (incxy == 0 && incyx == 0 && !K051316_wraparound[chip])
/*TODO*///		{
/*TODO*///			/* optimized loop for the not rotated case */
/*TODO*///
/*TODO*///			if (incxx == 0x800)
/*TODO*///			{
/*TODO*///				/* optimized loop for the not zoomed case */
/*TODO*///
/*TODO*///				/* startx is unsigned */
/*TODO*///				startx = ((INT32)startx) >> 11;
/*TODO*///
/*TODO*///				if (startx >= 512)
/*TODO*///				{
/*TODO*///					sx += -startx;
/*TODO*///					startx = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (sx <= ex)
/*TODO*///				{
/*TODO*///					while (sy <= ey)
/*TODO*///					{
/*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							x = sx;
/*TODO*///							cx = startx;
/*TODO*///							cy = starty >> 11;
/*TODO*///							dest = &bitmap->line[sy][sx];
/*TODO*///							while (x <= ex && cx < 512)
/*TODO*///							{
/*TODO*///								int c = srcbitmap->line[cy][cx];
/*TODO*///
/*TODO*///								if (c != palette_transparent_pen)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx++;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						starty += incyy;
/*TODO*///						sy++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				while ((startx & 0xfff00000) != 0 && sx <= ex)
/*TODO*///				{
/*TODO*///					startx += incxx;
/*TODO*///					sx++;
/*TODO*///				}
/*TODO*///
/*TODO*///				if ((startx & 0xfff00000) == 0)
/*TODO*///				{
/*TODO*///					while (sy <= ey)
/*TODO*///					{
/*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							x = sx;
/*TODO*///							cx = startx;
/*TODO*///							cy = starty >> 11;
/*TODO*///							dest = &bitmap->line[sy][sx];
/*TODO*///							while (x <= ex && (cx & 0xfff00000) == 0)
/*TODO*///							{
/*TODO*///								int c = srcbitmap->line[cy][cx >> 11];
/*TODO*///
/*TODO*///								if (c != palette_transparent_pen)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx += incxx;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						starty += incyy;
/*TODO*///						sy++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (K051316_wraparound[chip])
/*TODO*///			{
/*TODO*///				/* plot with wraparound */
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					x = sx;
/*TODO*///					cx = startx;
/*TODO*///					cy = starty;
/*TODO*///					dest = &bitmap->line[sy][sx];
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						int c = srcbitmap->line[(cy >> 11) & 0x1ff][(cx >> 11) & 0x1ff];
/*TODO*///
/*TODO*///						if (c != palette_transparent_pen)
/*TODO*///							*dest = c;
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///					startx += incyx;
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					x = sx;
/*TODO*///					cx = startx;
/*TODO*///					cy = starty;
/*TODO*///					dest = &bitmap->line[sy][sx];
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						if ((cx & 0xfff00000) == 0 && (cy & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							int c = srcbitmap->line[cy >> 11][cx >> 11];
/*TODO*///
/*TODO*///							if (c != palette_transparent_pen)
/*TODO*///								*dest = c;
/*TODO*///						}
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///					startx += incyx;
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* 16-bit case */
/*TODO*///
/*TODO*///		unsigned short *dest;
/*TODO*///
/*TODO*///		if (incxy == 0 && incyx == 0 && !K051316_wraparound[chip])
/*TODO*///		{
/*TODO*///			/* optimized loop for the not rotated case */
/*TODO*///
/*TODO*///			if (incxx == 0x800)
/*TODO*///			{
/*TODO*///				/* optimized loop for the not zoomed case */
/*TODO*///
/*TODO*///				/* startx is unsigned */
/*TODO*///				startx = ((INT32)startx) >> 11;
/*TODO*///
/*TODO*///				if (startx >= 512)
/*TODO*///				{
/*TODO*///					sx += -startx;
/*TODO*///					startx = 0;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (sx <= ex)
/*TODO*///				{
/*TODO*///					while (sy <= ey)
/*TODO*///					{
/*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							x = sx;
/*TODO*///							cx = startx;
/*TODO*///							cy = starty >> 11;
/*TODO*///							dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*///							while (x <= ex && cx < 512)
/*TODO*///							{
/*TODO*///								int c = ((unsigned short *)srcbitmap->line[cy])[cx];
/*TODO*///
/*TODO*///								if (c != palette_transparent_pen)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx++;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						starty += incyy;
/*TODO*///						sy++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				while ((startx & 0xfff00000) != 0 && sx <= ex)
/*TODO*///				{
/*TODO*///					startx += incxx;
/*TODO*///					sx++;
/*TODO*///				}
/*TODO*///
/*TODO*///				if ((startx & 0xfff00000) == 0)
/*TODO*///				{
/*TODO*///					while (sy <= ey)
/*TODO*///					{
/*TODO*///						if ((starty & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							x = sx;
/*TODO*///							cx = startx;
/*TODO*///							cy = starty >> 11;
/*TODO*///							dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*///							while (x <= ex && (cx & 0xfff00000) == 0)
/*TODO*///							{
/*TODO*///								int c = ((unsigned short *)srcbitmap->line[cy])[cx >> 11];
/*TODO*///
/*TODO*///								if (c != palette_transparent_pen)
/*TODO*///									*dest = c;
/*TODO*///
/*TODO*///								cx += incxx;
/*TODO*///								x++;
/*TODO*///								dest++;
/*TODO*///							}
/*TODO*///						}
/*TODO*///						starty += incyy;
/*TODO*///						sy++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (K051316_wraparound[chip])
/*TODO*///			{
/*TODO*///				/* plot with wraparound */
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					x = sx;
/*TODO*///					cx = startx;
/*TODO*///					cy = starty;
/*TODO*///					dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						int c = ((unsigned short *)srcbitmap->line[(cy >> 11) & 0x1ff])[(cx >> 11) & 0x1ff];
/*TODO*///
/*TODO*///						if (c != palette_transparent_pen)
/*TODO*///							*dest = c;
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///					startx += incyx;
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				while (sy <= ey)
/*TODO*///				{
/*TODO*///					x = sx;
/*TODO*///					cx = startx;
/*TODO*///					cy = starty;
/*TODO*///					dest = &((unsigned short *)bitmap->line[sy])[sx];
/*TODO*///					while (x <= ex)
/*TODO*///					{
/*TODO*///						if ((cx & 0xfff00000) == 0 && (cy & 0xfff00000) == 0)
/*TODO*///						{
/*TODO*///							int c = ((unsigned short *)srcbitmap->line[cy >> 11])[cx >> 11];
/*TODO*///
/*TODO*///							if (c != palette_transparent_pen)
/*TODO*///								*dest = c;
/*TODO*///						}
/*TODO*///
/*TODO*///						cx += incxx;
/*TODO*///						cy += incxy;
/*TODO*///						x++;
/*TODO*///						dest++;
/*TODO*///					}
/*TODO*///					startx += incyx;
/*TODO*///					starty += incyy;
/*TODO*///					sy++;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///#if 0
/*TODO*///	usrintf_showmessage("%02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x %02x%02x%02x%02x",
/*TODO*///			K051316_ctrlram[chip][0x00],
/*TODO*///			K051316_ctrlram[chip][0x01],
/*TODO*///			K051316_ctrlram[chip][0x02],
/*TODO*///			K051316_ctrlram[chip][0x03],
/*TODO*///			K051316_ctrlram[chip][0x04],
/*TODO*///			K051316_ctrlram[chip][0x05],
/*TODO*///			K051316_ctrlram[chip][0x06],
/*TODO*///			K051316_ctrlram[chip][0x07],
/*TODO*///			K051316_ctrlram[chip][0x08],
/*TODO*///			K051316_ctrlram[chip][0x09],
/*TODO*///			K051316_ctrlram[chip][0x0a],
/*TODO*///			K051316_ctrlram[chip][0x0b],
/*TODO*///			K051316_ctrlram[chip][0x0c],	/* bank for ROM testing */
/*TODO*///			K051316_ctrlram[chip][0x0d],
/*TODO*///			K051316_ctrlram[chip][0x0e],	/* 0 = test ROMs */
/*TODO*///			K051316_ctrlram[chip][0x0f]);
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_zoom_draw_0(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	K051316_zoom_draw(0, bitmap);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_zoom_draw_1(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	K051316_zoom_draw(1, bitmap);
/*TODO*///}
/*TODO*///
/*TODO*///void K051316_zoom_draw_2(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	K051316_zoom_draw(2, bitmap);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    static /*unsigned*/ char[] K053251_ram = new char[16];
    static int[] K053251_palette_index = new int[5];

    public static WriteHandlerPtr K053251_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            data &= 0x3f;

            if (K053251_ram[offset] != data) {
                K053251_ram[offset] = (char) (data & 0xFF);
                if (offset == 9) {
                    /* palette base index */
                    K053251_palette_index[0] = 32 * ((data >> 0) & 0x03);
                    K053251_palette_index[1] = 32 * ((data >> 2) & 0x03);
                    K053251_palette_index[2] = 32 * ((data >> 4) & 0x03);
                    tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                } else if (offset == 10) {
                    /* palette base index */
                    K053251_palette_index[3] = 16 * ((data >> 0) & 0x07);
                    K053251_palette_index[4] = 16 * ((data >> 3) & 0x07);
                    tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
                }
            }
        }
    };

    static int K053251_get_priority(int ci) {
        return K053251_ram[ci];
    }

    static int K053251_get_palette_index(int ci) {
        return K053251_palette_index[ci];
    }
    /*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static unsigned char K054000_ram[0x20];
/*TODO*///
/*TODO*///static void collision_w( int offs, int data )
/*TODO*///{
/*TODO*///}
/*TODO*///
/*TODO*///void K054000_w(int offset,int data)
/*TODO*///{
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 054000 address %02x\n",cpu_get_pc(),data,offset);
/*TODO*///#endif
/*TODO*///
/*TODO*///	K054000_ram[offset] = data;
/*TODO*///}
/*TODO*///
/*TODO*///int K054000_r(int offset)
/*TODO*///{
/*TODO*///	int Acx,Acy,Aax,Aay;
/*TODO*///	int Bcx,Bcy,Bax,Bay;
/*TODO*///
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 054000 address %02x\n",cpu_get_pc(),offset);
/*TODO*///#endif
/*TODO*///
/*TODO*///	if (offset != 0x18) return 0;
/*TODO*///
/*TODO*///
/*TODO*///	Acx = (K054000_ram[0x01] << 16) | (K054000_ram[0x02] << 8) | K054000_ram[0x03];
/*TODO*///	Acy = (K054000_ram[0x09] << 16) | (K054000_ram[0x0a] << 8) | K054000_ram[0x0b];
/*TODO*////* TODO: this is a hack to make thndrx2 pass the startup check. It is certainly wrong. */
/*TODO*///if (K054000_ram[0x04] == 0xff) Acx+=3;
/*TODO*///if (K054000_ram[0x0c] == 0xff) Acy+=3;
/*TODO*///	Aax = K054000_ram[0x06] + 1;
/*TODO*///	Aay = K054000_ram[0x07] + 1;
/*TODO*///
/*TODO*///	Bcx = (K054000_ram[0x15] << 16) | (K054000_ram[0x16] << 8) | K054000_ram[0x17];
/*TODO*///	Bcy = (K054000_ram[0x11] << 16) | (K054000_ram[0x12] << 8) | K054000_ram[0x13];
/*TODO*///	Bax = K054000_ram[0x0e] + 1;
/*TODO*///	Bay = K054000_ram[0x0f] + 1;
/*TODO*///
/*TODO*///	if (Acx + Aax < Bcx - Bax)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (Bcx + Bax < Acx - Aax)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (Acy + Aay < Bcy - Bay)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (Bcy + Bay < Acy - Aay)
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static unsigned char K051733_ram[0x20];
/*TODO*///
/*TODO*///void K051733_w(int offset,int data)
/*TODO*///{
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: write %02x to 051733 address %02x\n",cpu_get_pc(),data,offset);
/*TODO*///#endif
/*TODO*///
/*TODO*///	K051733_ram[offset] = data;
/*TODO*///}
/*TODO*///
/*TODO*///int K051733_r(int offset)
/*TODO*///{
/*TODO*///	int op1 = (K051733_ram[0x00] << 8) | K051733_ram[0x01];
/*TODO*///	int op2 = (K051733_ram[0x02] << 8) | K051733_ram[0x03];
/*TODO*///
/*TODO*///	int rad = (K051733_ram[0x06] << 8) | K051733_ram[0x07];
/*TODO*///	int yobj1c = (K051733_ram[0x08] << 8) | K051733_ram[0x09];
/*TODO*///	int xobj1c = (K051733_ram[0x0a] << 8) | K051733_ram[0x0b];
/*TODO*///	int yobj2c = (K051733_ram[0x0c] << 8) | K051733_ram[0x0d];
/*TODO*///	int xobj2c = (K051733_ram[0x0e] << 8) | K051733_ram[0x0f];
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog) fprintf(errorlog,"%04x: read 051733 address %02x\n",cpu_get_pc(),offset);
/*TODO*///#endif
/*TODO*///
/*TODO*///	switch(offset){
/*TODO*///		case 0x00:
/*TODO*///			if (op2) return	((op1/op2) >> 8);
/*TODO*///			else return 0xff;
/*TODO*///		case 0x01:
/*TODO*///			if (op2) return	op1/op2;
/*TODO*///			else return 0xff;
/*TODO*///
/*TODO*///		/* this is completely unverified */
/*TODO*///		case 0x02:
/*TODO*///			if (op2) return	((op1%op2) >> 8);
/*TODO*///			else return 0xff;
/*TODO*///		case 0x03:
/*TODO*///			if (op2) return	op1%op2;
/*TODO*///			else return 0xff;
/*TODO*///
/*TODO*///		case 0x07:{
/*TODO*///			if (xobj1c + rad < xobj2c - rad)
/*TODO*///				return 0x80;
/*TODO*///
/*TODO*///			if (xobj2c + rad < xobj1c - rad)
/*TODO*///				return 0x80;
/*TODO*///
/*TODO*///			if (yobj1c + rad < yobj2c - rad)
/*TODO*///				return 0x80;
/*TODO*///
/*TODO*///			if (yobj2c + rad < yobj1c - rad)
/*TODO*///				return 0x80;
/*TODO*///
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		default:
/*TODO*///			return K051733_ram[offset];
/*TODO*///	}
/*TODO*///}
/*TODO*///    
}
