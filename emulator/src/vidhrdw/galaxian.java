package vidhrdw;

import static arcadeflex.libc.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static vidhrdw.generic.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static drivers.scramble.*;
import static arcadeflex.ptrlib.*;

public class galaxian {

    static rectangle _spritevisiblearea = new rectangle(
            2 * 8 + 1, 32 * 8 - 1,
            2 * 8, 30 * 8 - 1
    );
    static rectangle _spritevisibleareaflipx = new rectangle(
            0 * 8, 30 * 8 - 2,
            2 * 8, 30 * 8 - 1
    );

    static rectangle spritevisiblearea;
    static rectangle spritevisibleareaflipx;

    static final int MAX_STARS = 250;
    static final int STARS_COLOR_BASE = 32;

    public static UBytePtr galaxian_attributesram = new UBytePtr();
    public static UBytePtr galaxian_bulletsram = new UBytePtr();

    public static int[] galaxian_bulletsram_size = new int[1];
    static int stars_on, stars_blink;
    static int stars_type;        /* 0 = Galaxian stars 1 = Scramble stars */
    /*  2 = Rescue stars (same as Scramble, but only half screen) */
    /*  3 = Mariner stars (same as Galaxian, but some parts are blanked */

    static int stars_scroll;
    static int color_mask;

    static class star {

        public star() {
        }
        ;
        public int x, y, code;
    };
    static star stars[] = new star[MAX_STARS];

    static {
        for (int k = 0; k < MAX_STARS; k++) {
            stars[k] = new star();
        }
    }
    static int total_stars;

    public static abstract interface modify_charcodePtr {

        public abstract void handler(int[] charcode, int offs);
    }

    public static abstract interface modify_spritecodePtr {

        public abstract void handler(int[] spritecode, int[] flipx, int[] flipy, int offs);
    }

    public static modify_spritecodePtr modify_spritecode;
    public static modify_charcodePtr modify_charcode;

    /*TODO*///static int mooncrst_gfxextend;
    static int pisces_gfxbank;
    /*TODO*///static int jumpbug_gfxbank[5];
    static int[] flipscreen = new int[2];

    static int background_on;
    static char[] backcolor = new char[256];
    /*TODO*///
    /*TODO*///static void mooncrgx_gfxextend_w      (int offset,int data);
    /*TODO*///
    /*TODO*///static void mooncrst_modify_charcode  (int *charcode,int offs);
    /*TODO*///static void  moonqsr_modify_charcode  (int *charcode,int offs);
    /*TODO*///static void   pisces_modify_charcode  (int *charcode,int offs);
    /*TODO*///static void  mariner_modify_charcode  (int *charcode,int offs);
    /*TODO*///static void  jumpbug_modify_charcode  (int *charcode,int offs);
    /*TODO*///
    /*TODO*///static void mooncrst_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///static void  moonqsr_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///static void   ckongs_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///static void  calipso_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///static void   pisces_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///static void  jumpbug_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs);
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///
    /*TODO*///  Convert the color PROMs into a more useable format.
    /*TODO*///
    /*TODO*///  Moon Cresta has one 32 bytes palette PROM, connected to the RGB output
    /*TODO*///  this way:
    /*TODO*///
    /*TODO*///  bit 7 -- 220 ohm resistor  -- BLUE
    /*TODO*///        -- 470 ohm resistor  -- BLUE
    /*TODO*///        -- 220 ohm resistor  -- GREEN
    /*TODO*///        -- 470 ohm resistor  -- GREEN
    /*TODO*///        -- 1  kohm resistor  -- GREEN
    /*TODO*///        -- 220 ohm resistor  -- RED
    /*TODO*///        -- 470 ohm resistor  -- RED
    /*TODO*///  bit 0 -- 1  kohm resistor  -- RED
    /*TODO*///
    /*TODO*///  The output of the background star generator is connected this way:
    /*TODO*///
    /*TODO*///  bit 5 -- 100 ohm resistor  -- BLUE
    /*TODO*///        -- 150 ohm resistor  -- BLUE
    /*TODO*///        -- 100 ohm resistor  -- GREEN
    /*TODO*///        -- 150 ohm resistor  -- GREEN
    /*TODO*///        -- 100 ohm resistor  -- RED
    /*TODO*///  bit 0 -- 150 ohm resistor  -- RED
    /*TODO*///
    /*TODO*///  The blue background in Scramble and other games goes through a 390 ohm
    /*TODO*///  resistor.
    /*TODO*///
    /*TODO*///  The RGB outputs have a 470 ohm pull-down each.
    /*TODO*///
    /*TODO*///***************************************************************************/

    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }
    public static VhConvertColorPromPtr galaxian_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;
		//#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
            //#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])

            color_mask = (Machine.gfx[0].color_granularity == 4) ? 7 : 3;

            /* first, the character/sprite palette */
            int p_inc = 0;
            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                /* red component */
                bit0 = (color_prom.read() >> 0) & 0x01;
                bit1 = (color_prom.read() >> 1) & 0x01;
                bit2 = (color_prom.read() >> 2) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* green component */
                bit0 = (color_prom.read() >> 3) & 0x01;
                bit1 = (color_prom.read() >> 4) & 0x01;
                bit2 = (color_prom.read() >> 5) & 0x01;
                palette[p_inc++].set((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                /* blue component */
                bit0 = (color_prom.read() >> 6) & 0x01;
                bit1 = (color_prom.read() >> 7) & 0x01;
                palette[p_inc++].set((char) (0x4f * bit0 + 0xa8 * bit1));

                color_prom.inc();
            }

            /* now the stars */
            for (i = 0; i < 64; i++) {
                int bits;
                int map[] = {0x00, 0x88, 0xcc, 0xff};

                bits = (i >> 0) & 0x03;
                palette[p_inc++].set((char) (map[bits]));
                bits = (i >> 2) & 0x03;
                palette[p_inc++].set((char) (map[bits]));
                bits = (i >> 4) & 0x03;
                palette[p_inc++].set((char) (map[bits]));
            }

            /* characters and sprites use the same palette */
            for (i = 0; i < TOTAL_COLORS(0); i++) {
                /* 00 is always mapped to pen 0 */
                if ((i & (Machine.gfx[0].color_granularity - 1)) == 0) {
                    colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = 0;
                }
            }

            /* bullets can be either white or yellow */
            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 0] = 0;
            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 1] = 0x0f + STARS_COLOR_BASE;	/* yellow */

            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 2] = 0;
            colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + 3] = 0x3f + STARS_COLOR_BASE;	/* white */

            /* default blue background */
            palette[p_inc++].set((char) (0));
            palette[p_inc++].set((char) (0));
            palette[p_inc++].set((char) (0x55));

            for (i = 0; i < TOTAL_COLORS(3); i++) {
                colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char) (96 + (i % (Machine.drv.total_colors - 96)));
            }
        }
    };
    public static VhConvertColorPromPtr minefld_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
            /* Graduated Blue */
            for (i = 0; i < 64; i++) {
                palette[96 * 3 + i * 3 + 0].set((char) (0));
                palette[96 * 3 + i * 3 + 1].set((char) (i * 2));
                palette[96 * 3 + i * 3 + 2].set((char) (i * 4));
            }

            /* Graduated Brown */
            for (i = 0; i < 64; i++) {
                palette[160 * 3 + i * 3 + 0].set((char) (i * 3));
                palette[160 * 3 + i * 3 + 1].set((char) (i * 1.5));
                palette[160 * 3 + i * 3 + 2].set((char) (i));
            }
        }
    };

    public static VhConvertColorPromPtr rescue_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
            /* Graduated Blue */
            for (i = 0; i < 64; i++) {
                palette[96 * 3 + i * 3 + 0].set((char) 0);
                palette[96 * 3 + i * 3 + 1].set((char) (i * 2));
                palette[96 * 3 + i * 3 + 2].set((char) (i * 4));
            }
        }
    };

    public static VhConvertColorPromPtr stratgyx_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
            /* blue and dark brown */
            palette[96 * 3 + 0].set((char) 0);
            palette[96 * 3 + 1].set((char) 0);
            palette[96 * 3 + 2].set((char) 0x55);

            palette[97 * 3 + 0].set((char) 0x40);
            palette[97 * 3 + 1].set((char) 0x20);
            palette[97 * 3 + 2].set((char) 0x0);
        }
    };
    public static VhConvertColorPromPtr mariner_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(UByte[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            galaxian_vh_convert_color_prom.handler(palette, colortable, color_prom);

            /* set up background colors */
            /* nine shades of blue */
            palette[96 * 3 + 0].set((char) 0);
            palette[96 * 3 + 1].set((char) 0);
            palette[96 * 3 + 2].set((char) 0);

            for (i = 1; i < 10; i++) {
                palette[96 * 3 + i * 3 + 0].set((char) 0);
                palette[96 * 3 + i * 3 + 1].set((char) 0);
                palette[96 * 3 + i * 3 + 2].set((char) (0xea - 0x15 * (i - 1)));
            }
        }
    };

    static void decode_background() {
        int i, j, k;
        char[] tile = new char[32 * 8 * 8];

        for (i = 0; i < 32; i++) {
            for (j = 0; j < 8; j++) {
                for (k = 0; k < 8; k++) {
                    tile[i * 64 + j * 8 + k] = backcolor[i * 8 + j];
                }
            }

            decodechar(Machine.gfx[3], i, new UBytePtr(tile), Machine.drv.gfxdecodeinfo[3].gfxlayout);
        }
    }

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    static int common_vh_start() {
        /*TODO*///	extern struct GameDriver driver_newsin7;
        int generator;
        int x, y;

        modify_charcode = null;
        modify_spritecode = null;
        /*TODO*///
    /*TODO*///	mooncrst_gfxextend = 0;
        stars_on = 0;
        flipscreen[0] = 0;
        flipscreen[1] = 0;

        if (generic_vh_start.handler() != 0) {
            return 1;
        }

        /* Default alternate background - Solid Blue */
        for (x = 0; x < 256; x++) {
            backcolor[x] = 0;
        }
        background_on = 0;

        decode_background();

        /* precalculate the star background */
        total_stars = 0;
        generator = 0;

        for (y = 255; y >= 0; y--) {
            for (x = 511; x >= 0; x--) {
                int bit1, bit2;

                generator <<= 1;
                bit1 = (~generator >> 17) & 1;
                bit2 = (generator >> 5) & 1;

                if ((bit1 ^ bit2) != 0) {
                    generator |= 1;
                }

                if ((((~generator >> 16) & 1) != 0) && (generator & 0xff) == 0xff) {
                    int color;

                    color = (~(generator >> 8)) & 0x3f;
                    if (color != 0 && total_stars < MAX_STARS) {
                        stars[total_stars].x = x;
                        stars[total_stars].y = y;
                        stars[total_stars].code = color;

                        total_stars++;
                    }
                }
            }
        }

        /* all the games except New Sinbad 7 clip the sprites at the top of the screen,
         New Sinbad 7 does it at the bottom */
        if (Machine.gamedrv == driver_newsin7) {
            spritevisiblearea = _spritevisibleareaflipx;
            spritevisibleareaflipx = _spritevisiblearea;
        } else {
            spritevisiblearea = _spritevisiblearea;
            spritevisibleareaflipx = _spritevisibleareaflipx;
        }

        return 0;
    }
    public static VhStartPtr galaxian_vh_start = new VhStartPtr() {
        public int handler() {
            stars_type = 0;
            return common_vh_start();
        }
    };
    /*TODO*///int mooncrst_vh_start(void)
    /*TODO*///{
    /*TODO*///	int ret = galaxian_vh_start();
    /*TODO*///
    /*TODO*///	modify_charcode   = mooncrst_modify_charcode;
    /*TODO*///	modify_spritecode = mooncrst_modify_spritecode;
    /*TODO*///	return ret;
    /*TODO*///}
    /*TODO*///
    /*TODO*///int mooncrgx_vh_start(void)
    /*TODO*///{
    /*TODO*///	install_mem_write_handler(0, 0x6000, 0x6002, mooncrgx_gfxextend_w);
    /*TODO*///	return mooncrst_vh_start();
    /*TODO*///}
    /*TODO*///
    /*TODO*///int moonqsr_vh_start(void)
    /*TODO*///{
    /*TODO*///	int ret = galaxian_vh_start();
    /*TODO*///
    /*TODO*///	modify_charcode   = moonqsr_modify_charcode;
    /*TODO*///	modify_spritecode = moonqsr_modify_spritecode;
    /*TODO*///	return ret;
    /*TODO*///}
    /*TODO*///
    public static VhStartPtr pisces_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = galaxian_vh_start.handler();

            modify_charcode = pisces_modify_charcode;
            modify_spritecode = pisces_modify_spritecode;
            return ret;
        }
    };
    public static VhStartPtr scramble_vh_start = new VhStartPtr() {
        public int handler() {
            stars_type = 1;
            return common_vh_start();
        }
    };

    public static VhStartPtr rescue_vh_start = new VhStartPtr() {
        public int handler() {
            int x;

            int ret = common_vh_start();

            stars_type = 2;

            /* Setup background color array (blue sky, blue sea, black bottom line) */
            for (x = 0; x < 64; x++) {
                backcolor[x * 2 + 0] = (char) x;
                backcolor[x * 2 + 1] = (char) x;
            }

            for (x = 0; x < 60; x++) {
                backcolor[128 + x * 2 + 0] = (char) (x + 4);
                backcolor[128 + x * 2 + 1] = (char) (x + 4);
            }

            for (x = 248; x < 256; x++) {
                backcolor[x] = 0;
            }

            decode_background();

            return ret;
        }
    };

    public static VhStartPtr minefld_vh_start = new VhStartPtr() {
        public int handler() {
            int x;

            int ret = common_vh_start();

            stars_type = 2;

            /* Setup background color array (blue sky, brown ground, black bottom line) */
            for (x = 0; x < 64; x++) {
                backcolor[x * 2 + 0] = (char) x;
                backcolor[x * 2 + 1] = (char) x;
            }

            for (x = 0; x < 60; x++) {
                backcolor[128 + x * 2 + 0] = (char) (x + 64);
                backcolor[128 + x * 2 + 1] = (char) (x + 64);
            }

            for (x = 248; x < 256; x++) {
                backcolor[x] = 0;
            }

            decode_background();

            return ret;
        }
    };

    public static VhStartPtr stratgyx_vh_start = new VhStartPtr() {
        public int handler() {
            int x;

            int ret = common_vh_start();

            stars_type = -1;

            /* Setup background color array (blue left side, brown ground */
            for (x = 0; x < 48; x++) {
                backcolor[x] = 0;
            }

            for (x = 48; x < 256; x++) {
                backcolor[x] = 1;
            }

            decode_background();

            return ret;
        }
    };
    public static VhStartPtr ckongs_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = common_vh_start();

            stars_type = 1;
            modify_spritecode = ckongs_modify_spritecode;
            return ret;
        }
    };
    public static VhStartPtr calipso_vh_start = new VhStartPtr() {
        public int handler() {
            int ret = common_vh_start();

            stars_type = 1;
            modify_spritecode = calipso_modify_spritecode;
            return ret;
        }
    };
    public static VhStartPtr mariner_vh_start = new VhStartPtr() {
        public int handler() {
            int x;

            int ret = common_vh_start();

            stars_type = 3;
            modify_charcode = mariner_modify_charcode;

            /* Setup background color array (blue water) */
            for (x = 0; x < 63; x++) {
                backcolor[x] = 0;
            }
            for (x = 63; x < 71; x++) {
                backcolor[x] = 1;
            }
            for (x = 71; x < 79; x++) {
                backcolor[x] = 2;
            }
            for (x = 79; x < 87; x++) {
                backcolor[x] = 3;
            }
            for (x = 87; x < 95; x++) {
                backcolor[x] = 4;
            }
            for (x = 95; x < 111; x++) {
                backcolor[x] = 5;
            }
            for (x = 111; x < 135; x++) {
                backcolor[x] = 6;
            }
            for (x = 135; x < 167; x++) {
                backcolor[x] = 7;
            }
            for (x = 167; x < 207; x++) {
                backcolor[x] = 8;
            }
            for (x = 207; x < 247; x++) {
                backcolor[x] = 9;
            }
            for (x = 247; x < 256; x++) {
                backcolor[x] = 0;
            }

            decode_background();

            /* The background is always on */
            background_on = 1;

            return ret;
        }
    };
    /*TODO*///
    /*TODO*///int jumpbug_vh_start(void)
    /*TODO*///{
    /*TODO*///	int ret = galaxian_vh_start();
    /*TODO*///
    /*TODO*///	modify_charcode   = jumpbug_modify_charcode;
    /*TODO*///	modify_spritecode = jumpbug_modify_spritecode;
    /*TODO*///	return ret;
    /*TODO*///}
    /*TODO*///
    /*TODO*///int zigzag_vh_start(void)
    /*TODO*///{
    /*TODO*///	int ret = galaxian_vh_start();
    /*TODO*///
    /*TODO*///	/* no bullets RAM */
    /*TODO*///	galaxian_bulletsram_size = 0;
    /*TODO*///	return ret;
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///
    public static WriteHandlerPtr galaxian_flipx_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen[0] != (data & 1)) {
                flipscreen[0] = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };
    public static WriteHandlerPtr galaxian_flipy_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen[1] != (data & 1)) {
                flipscreen[1] = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr hotshock_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            galaxian_flipx_w.handler(offset, data);
            galaxian_flipy_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr galaxian_attributes_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (((offset & 1) != 0) && galaxian_attributesram.read(offset) != data) {
                int i;

                for (i = offset / 2; i < videoram_size[0]; i += 32) {
                    dirtybuffer[i] = 1;
                }
            }

            galaxian_attributesram.write(offset, data);
        }
    };
    public static WriteHandlerPtr scramble_background_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (background_on != data) {
                background_on = data;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    public static WriteHandlerPtr galaxian_stars_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            stars_on = (data & 1);
            stars_scroll = 0;
        }
    };

    /*TODO*///void mooncrst_gfxextend_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	if (data) mooncrst_gfxextend |= (1 << offset);
    /*TODO*///	else mooncrst_gfxextend &= ~(1 << offset);
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*///static void mooncrgx_gfxextend_w(int offset,int data)
    /*TODO*///{
    /*TODO*///  /* for the Moon Cresta bootleg on Galaxian H/W the gfx_extend is
    /*TODO*///     located at 0x6000-0x6002.  Also, 0x6000 and 0x6001 are reversed. */
    /*TODO*///     if(offset == 1)
    /*TODO*///       offset = 0;
    /*TODO*///     else if(offset == 0)
    /*TODO*///       offset = 1;    /* switch 0x6000 and 0x6001 */
    /*TODO*///	mooncrst_gfxextend_w(offset, data);
    /*TODO*///}
    /*TODO*///
    public static WriteHandlerPtr pisces_gfxbank_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (pisces_gfxbank != (data & 1)) {
                pisces_gfxbank = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };
    /*TODO*///void jumpbug_gfxbank_w(int offset,int data)
    /*TODO*///{
    /*TODO*///	if (jumpbug_gfxbank[offset] != data)
    /*TODO*///	{
    /*TODO*///		jumpbug_gfxbank[offset] = data;
    /*TODO*///		memset(dirtybuffer,1,videoram_size);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///

    public static void plot_star(osd_bitmap bitmap, int x, int y, int code) {
        int backcol, pixel;

        backcol = backcolor[x];

        if (flipscreen[0] != 0) {
            x = 255 - x;
        }
        if (flipscreen[1] != 0) {
            y = 255 - y;
        }

        pixel = read_pixel.handler(bitmap, x, y);

        if ((pixel == Machine.pens[0])
                || (pixel == Machine.pens[96 + backcol])) {
            plot_pixel.handler(bitmap, x, y, Machine.pens[STARS_COLOR_BASE + code]);
        }
    }
    /*TODO*///
    /*TODO*///
    /*TODO*////* Character banking routines */
    /*TODO*///static void mooncrst_modify_charcode(int *charcode,int offs)
    /*TODO*///{
    /*TODO*///	if ((mooncrst_gfxextend & 4) && (*charcode & 0xc0) == 0x80)
    /*TODO*///	{
    /*TODO*///		*charcode = (*charcode & 0x3f) | (mooncrst_gfxextend << 6);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void moonqsr_modify_charcode(int *charcode,int offs)
    /*TODO*///{
    /*TODO*///	if (galaxian_attributesram[2 * (offs % 32) + 1] & 0x20)
    /*TODO*///	{
    /*TODO*///		*charcode += 256;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///    mooncrst_modify_charcode(charcode,offs);
    /*TODO*///}
    /*TODO*///
    public static modify_charcodePtr pisces_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            if (pisces_gfxbank != 0) {
                charcode[0] += 256;
            }
        }
    };
    public static modify_charcodePtr mariner_modify_charcode = new modify_charcodePtr() {
        public void handler(int[] charcode, int offs) {
            /* I don't really know if this is correct, but I don't see
             any other obvious way to switch character banks. */
            if (((offs & 0x1f) <= 4)
                    || ((offs & 0x1f) >= 30)) {
                charcode[0] += 256;
            }
        }
    };
    /*TODO*///
    /*TODO*///static void jumpbug_modify_charcode(int *charcode,int offs)
    /*TODO*///{
    /*TODO*///	if (((*charcode & 0xc0) == 0x80) &&
    /*TODO*///		 (jumpbug_gfxbank[2] & 1) != 0)
    /*TODO*///	{
    /*TODO*///		*charcode += 128 + (( jumpbug_gfxbank[0] & 1) << 6) +
    /*TODO*///				           (( jumpbug_gfxbank[1] & 1) << 7) +
    /*TODO*///						   ((~jumpbug_gfxbank[4] & 1) << 8);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///
    /*TODO*////* Sprite banking routines */
    /*TODO*///static void mooncrst_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs)
    /*TODO*///{
    /*TODO*///	if ((mooncrst_gfxextend & 4) && (*spritecode & 0x30) == 0x20)
    /*TODO*///	{
    /*TODO*///		*spritecode = (*spritecode & 0x0f) | (mooncrst_gfxextend << 4);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*///static void moonqsr_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs)
    /*TODO*///{
    /*TODO*///	if (spriteram[offs + 2] & 0x20)
    /*TODO*///	{
    /*TODO*///		*spritecode += 64;
    /*TODO*///	}
    /*TODO*///
    /*TODO*///    mooncrst_modify_spritecode(spritecode, flipx, flipy, offs);
    /*TODO*///}
    /*TODO*///
    public static modify_spritecodePtr ckongs_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if ((spriteram.read(offs + 2) & 0x10) != 0) {
                spritecode[0] += 64;//*spritecode += 64;
            }
        }
    };

    public static modify_spritecodePtr calipso_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            /* No flips */
            spritecode[0] = spriteram.read(offs + 1);
            flipx[0] = 0;
            flipy[0] = 0;
        }
    };
    public static modify_spritecodePtr pisces_modify_spritecode = new modify_spritecodePtr() {
        public void handler(int[] spritecode, int[] flipx, int[] flipy, int offs) {
            if (pisces_gfxbank != 0) {
                spritecode[0] += 64;
            }

        }
    };

    /*TODO*///static void jumpbug_modify_spritecode(int *spritecode,int *flipx,int *flipy,int offs)
    /*TODO*///{
    /*TODO*///	if (((*spritecode & 0x30) == 0x20) &&
    /*TODO*///		 (jumpbug_gfxbank[2] & 1) != 0)
    /*TODO*///	{
    /*TODO*///		*spritecode += 32 + (( jumpbug_gfxbank[0] & 1) << 4) +
    /*TODO*///		                    (( jumpbug_gfxbank[1] & 1) << 5) +
    /*TODO*///		                    ((~jumpbug_gfxbank[4] & 1) << 6);
    /*TODO*///	}
    /*TODO*///}
    /*TODO*///
    /*TODO*////***************************************************************************
    /*TODO*///
    /*TODO*///  Draw the game screen in the given osd_bitmap.
    /*TODO*///  Do NOT call osd_update_display() from this function, it will be called by
    /*TODO*///  the main emulation engine.
    /*TODO*///
    /*TODO*///***************************************************************************/
    public static VhUpdatePtr galaxian_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            int i, offs;

            /* for every character in the Video RAM, check if it has been modified */
            /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, background_charcode;
                    int[] charcode = new int[1];

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;

                    background_charcode = sx;

                    charcode[0] = videoram.read(offs);

                    if (flipscreen[0] != 0) {
                        sx = 31 - sx;
                    }
                    if (flipscreen[1] != 0) {
                        sy = 31 - sy;
                    }

                    if (modify_charcode != null) {
                        modify_charcode.handler(charcode, offs);
                    }

                    if (background_on != 0) {
                        /* Draw background */

                        drawgfx(tmpbitmap, Machine.gfx[3],
                                background_charcode,
                                0,
                                flipscreen[0], flipscreen[1],
                                8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    }

                    drawgfx(tmpbitmap, Machine.gfx[0],
                            charcode[0],
                            galaxian_attributesram.read(2 * (offs % 32) + 1) & color_mask,
                            flipscreen[0], flipscreen[1],
                            8 * sx, 8 * sy,
                            null, background_on != 0 ? TRANSPARENCY_COLOR : TRANSPARENCY_NONE, 0);
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int[] scroll = new int[32];

                if (flipscreen[0] != 0) {
                    for (i = 0; i < 32; i++) {
                        scroll[31 - i] = -galaxian_attributesram.read(2 * i);
                        if (flipscreen[1] != 0) {
                            scroll[31 - i] = -scroll[31 - i];
                        }
                    }
                } else {
                    for (i = 0; i < 32; i++) {
                        scroll[i] = -galaxian_attributesram.read(2 * i);
                        if (flipscreen[1] != 0) {
                            scroll[i] = -scroll[i];
                        }
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.drv.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw the bullets */
            for (offs = 0; offs < galaxian_bulletsram_size[0]; offs += 4) {
                int x, y;
                int color;

                if (offs == 7 * 4) {
                    color = 0;	/* yellow */
                } else {
                    color = 1;	/* white */
                }

                x = 255 - galaxian_bulletsram.read(offs + 3) - Machine.drv.gfxdecodeinfo[2].gfxlayout.width;
                y = 255 - galaxian_bulletsram.read(offs + 1);
                if (flipscreen[1] != 0) {
                    y = 255 - y;
                }

                drawgfx(bitmap, Machine.gfx[2],
                        0, /* this is just a line, generated by the hardware */
                        color,
                        0, 0,
                        x, y,
                        Machine.drv.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* Draw the sprites */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                int sx, sy;
                int[] flipx = new int[1];
                int[] flipy = new int[1];
                int[] spritecode = new int[1];

                sx = (spriteram.read(offs + 3) + 1) & 0xff; /* This is definately correct in Mariner. Look at
                 the 'gate' moving up/down. It stops at the
                 right spots */

                sy = 240 - spriteram.read(offs);
                flipx[0] = spriteram.read(offs + 1) & 0x40;
                flipy[0] = spriteram.read(offs + 1) & 0x80;
                spritecode[0] = spriteram.read(offs + 1) & 0x3f;

                if (modify_spritecode != null) {
                    //System.out.println("before:" + spritecode[0] + " " + flipx[0] + " " + flipy[0] + " " +offs);
                    modify_spritecode.handler(spritecode, flipx, flipy, offs);
                    //System.out.println("aadter:" + spritecode[0] + " " + flipx[0] + " " + flipy[0] + " " +offs);
                }

                if (flipscreen[0] != 0) {
                    sx = 240 - sx;	/* I checked a bunch of games including Scramble
                     (# of pixels the ship is from the top of the mountain),
                     Mariner and Checkman. This is correct for them */

                    flipx[0] = NOT(flipx[0]);
                }
                if (flipscreen[1] != 0) {
                    sy = 240 - sy;
                    flipy[0] = NOT(flipy[0]);
                }

                /* In Amidar, */
                /* Sprites #0, #1 and #2 need to be offset one pixel to be correctly */
                /* centered on the ladders in Turtles (we move them down, but since this */
                /* is a rotated game, we actually move them left). */
                /* Note that the adjustment must be done AFTER handling flipscreen, thus */
                /* proving that this is a hardware related "feature" */
                /* This is not Amidar, it is Galaxian/Scramble/hundreds of clones, and I'm */
                /* not sure it should be the same. A good game to test alignment is Armored Car */
                /*		if (offs <= 2*4) sy++;*/
                drawgfx(bitmap, Machine.gfx[1],
                        spritecode[0],
                        spriteram.read(offs + 2) & color_mask,
                        flipx[0], flipy[0],
                        sx, sy,
                        flipscreen[0] != 0 ? spritevisibleareaflipx : spritevisiblearea, TRANSPARENCY_PEN, 0);
            }

            /* draw the stars */
            if (stars_on != 0) {

                switch (stars_type) {
                    case -1: /* no stars */

                        break;

                    case 0:	/* Galaxian stars */

                    case 3:	/* Mariner stars */

                        for (offs = 0; offs < total_stars; offs++) {
                            int x, y;

                            x = ((stars[offs].x + stars_scroll) % 512) / 2;
                            y = (stars[offs].y + (stars_scroll + stars[offs].x) / 512) % 256;

                            if (y >= Machine.drv.visible_area.min_y
                                    && y <= Machine.drv.visible_area.max_y) {
                                /* No stars below row (column) 64, between rows 176 and 215 or
                                 between 224 and 247 */
                                if ((stars_type == 3)
                                        && ((x < 64)
                                        || ((x >= 176) && (x < 216))
                                        || ((x >= 224) && (x < 248)))) {
                                    continue;
                                }

                                if (((y & 1) ^ ((x >> 4) & 1)) != 0) {
                                    plot_star(bitmap, x, y, stars[offs].code);
                                }
                            }
                        }
                        break;

                    case 1:	/* Scramble stars */

                    case 2:	/* Rescue stars */

                        for (offs = 0; offs < total_stars; offs++) {
                            int x, y;

                            x = stars[offs].x / 2;
                            y = stars[offs].y;

                            if (y >= Machine.drv.visible_area.min_y
                                    && y <= Machine.drv.visible_area.max_y) {
                                if ((stars_type != 2 || x < 128)
                                        && /* draw only half screen in Rescue */ (((y & 1) ^ ((x >> 4) & 1)) != 0)) {
                                    /* Determine when to skip plotting */
                                    switch (stars_blink) {
                                        case 0:
                                            if ((stars[offs].code & 1) == 0) {
                                                continue;
                                            }
                                            break;
                                        case 1:
                                            if ((stars[offs].code & 4) == 0) {
                                                continue;
                                            }
                                            break;
                                        case 2:
                                            if ((stars[offs].x & 4) == 0) {
                                                continue;
                                            }
                                            break;
                                        case 3:
                                            /* Always plot */
                                            break;
                                    }
                                    plot_star(bitmap, x, y, stars[offs].code);
                                }
                            }
                        }
                        break;
                }
            }
        }
    };
    public static InterruptPtr galaxian_vh_interrupt = new InterruptPtr() {
        public int handler() {
            stars_scroll++;
            return nmi_interrupt.handler();
        }
    };
    static int blink_count_scramble;
    public static InterruptPtr scramble_vh_interrupt = new InterruptPtr() {
        public int handler() {

            blink_count_scramble++;
            if (blink_count_scramble >= 45) {
                blink_count_scramble = 0;
                stars_blink = (stars_blink + 1) & 3;
            }

            return nmi_interrupt.handler();
        }
    };
    public static InterruptPtr mariner_vh_interrupt = new InterruptPtr() {
        public int handler() {

            stars_scroll--;

            return nmi_interrupt.handler();
        }
    };
    public static InterruptPtr devilfsg_vh_interrupt = new InterruptPtr() {
        public int handler() {
            stars_scroll++;

            return interrupt.handler();
        }
    };
    public static InterruptPtr hunchbks_vh_interrupt = new InterruptPtr() {
        public int handler() {
            cpu_irq_line_vector_w(0, 0, 0x03);
            cpu_set_irq_line(0, 0, PULSE_LINE);

            return ignore_interrupt.handler();
        }
    };

}
