/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 26/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.z80.z80H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.snk.*;
//common imports
import static common.libc.cstring.memset;
//to be organized
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.copyscrollbitmap;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.drawgfx;
import static gr.codebb.arcadeflex.v036.platform.video.osd_free_bitmap;
import static gr.codebb.arcadeflex.v036.platform.video.osd_new_bitmap;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.YM3526_control_port_0_w;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.YM3526_status_port_0_r;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.YM3526_write_port_0_w;
import gr.codebb.arcadeflex.v037b7.sound._3812intfH.YM3526interface;

public class hal21 {

    static int scrollx_base;
    /* this is the only difference in video hardware found so far */

    public static VhStartHandlerPtr common_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            dirtybuffer = new char[64 * 64];
            if (dirtybuffer != null) {
                tmpbitmap = osd_new_bitmap(512, 512, Machine.scrbitmap.depth);
                if (tmpbitmap != null) {
                    memset(dirtybuffer, 1, 64 * 64);
                    return 0;
                }
                dirtybuffer = null;
            }
            return 1;
        }
    };

    public static VhStartHandlerPtr aso_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            scrollx_base = -16;
            return common_vh_start.handler();
        }
    };

    public static VhStartHandlerPtr hal21_vh_start = new VhStartHandlerPtr() {
        public int handler() {
            scrollx_base = 240;
            return common_vh_start.handler();
        }
    };

    public static VhStopHandlerPtr aso_vh_stop = new VhStopHandlerPtr() {
        public void handler() {
            osd_free_bitmap(tmpbitmap);
            dirtybuffer = null;
        }
    };
    public static VhConvertColorPromHandlerPtr aso_vh_convert_color_prom = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {

            int i;
            int num_colors = 1024;
            /* palette format is RRRG GGBB B??? the three unknown bits are used but */
 /* I'm not sure how, I'm currently using them as least significant bit but */
 /* that's most likely wrong. */
            int p_inc = 0;
            for (i = 0; i < num_colors; i++) {
                int bit0 = 0, bit1, bit2, bit3;

                colortable[i] = (char) i;

                bit0 = (color_prom.read(2 * num_colors) >> 2) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                bit0 = (color_prom.read(2 * num_colors) >> 1) & 0x01;
                bit1 = (color_prom.read(num_colors) >> 2) & 0x01;
                bit2 = (color_prom.read(num_colors) >> 3) & 0x01;
                bit3 = (color_prom.read(0) >> 0) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                bit0 = (color_prom.read(2 * num_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * num_colors) >> 3) & 0x01;
                bit2 = (color_prom.read(num_colors) >> 0) & 0x01;
                bit3 = (color_prom.read(num_colors) >> 1) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }
        }
    };
    static int old_bank, old_color;

    static void aso_draw_background(
            osd_bitmap bitmap,
            int scrollx, int scrolly,
            int bank, int color,
            GfxElement gfx) {
        rectangle clip = new rectangle(Machine.drv.visible_area);
        int offs;

        if (color != old_color || bank != old_bank) {
            memset(dirtybuffer, 1, 64 * 64);
            old_bank = bank;
            old_color = color;
        }

        for (offs = 0; offs < 64 * 64; offs++) {
            if (dirtybuffer[offs] != 0) {
                int tile_number = videoram.read(offs) + bank * 256;
                int sy = (offs % 64) * 8;
                int sx = (offs / 64) * 8;

                drawgfx(tmpbitmap, gfx,
                        tile_number,
                        color,
                        0, 0, /* no flip */
                        sx, sy,
                        null, TRANSPARENCY_NONE, 0);

                dirtybuffer[offs] = 0;
            }
        }

        copyscrollbitmap(bitmap, tmpbitmap,
                1, new int[]{scrollx}, 1, new int[]{scrolly},
                clip,
                TRANSPARENCY_NONE, 0);
    }

    static void aso_draw_sprites(
            osd_bitmap bitmap,
            int xscroll, int yscroll,
            GfxElement gfx
    ) {
        UBytePtr source = new UBytePtr(spriteram);
        int finish = source.offset + 60 * 4;

        rectangle clip = new rectangle(Machine.drv.visible_area);

        while (source.offset < finish) {
            int attributes = source.read(3);
            /* YBBX.CCCC */
            int tile_number = source.read(1);
            int sy = source.read(0) + ((attributes & 0x10) != 0 ? 256 : 0) - yscroll;
            int sx = source.read(2) + ((attributes & 0x80) != 0 ? 256 : 0) - xscroll;
            int color = attributes & 0xf;

            if ((attributes & 0x20) == 0) {
                tile_number += 512;
            }
            if ((attributes & 0x40) != 0) {
                tile_number += 256;
            }

            drawgfx(bitmap, gfx,
                    tile_number,
                    color,
                    0, 0,
                    (256 - sx) & 0x1ff, sy & 0x1ff,
                    clip, TRANSPARENCY_PEN, 7);

            source.inc(4);
        }
    }

    static int[] hal21_vreg = new int[6];

    public static WriteHandlerPtr hal21_vreg0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[0] = data;
        }
    };
    public static WriteHandlerPtr hal21_vreg1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[1] = data;
        }
    };
    public static WriteHandlerPtr hal21_vreg2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[2] = data;
        }
    };
    public static WriteHandlerPtr hal21_vreg3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[3] = data;
        }
    };
    public static WriteHandlerPtr hal21_vreg4_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[4] = data;
        }
    };
    public static WriteHandlerPtr hal21_vreg5_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            hal21_vreg[5] = data;
        }
    };

    public static VhUpdateHandlerPtr aso_vh_screenrefresh = new VhUpdateHandlerPtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            UBytePtr ram = memory_region(REGION_CPU1);
            int attributes = hal21_vreg[1];
            {
                char bg_attrs = (char) (hal21_vreg[0] & 0xFF);
                int scrolly = -8 + hal21_vreg[4] + ((attributes & 0x10) != 0 ? 256 : 0);
                int scrollx = scrollx_base + hal21_vreg[5] + ((attributes & 0x02) != 0 ? 0 : 256);

                aso_draw_background(bitmap, -scrollx, -scrolly,
                        bg_attrs >> 4, /* tile bank */
                        bg_attrs & 0xf, /* color bank */
                        Machine.gfx[1]
                );
            }

            {
                int scrollx = 0x1e + hal21_vreg[3] + ((attributes & 0x01) != 0 ? 256 : 0);
                int scrolly = -8 + 0x11 + hal21_vreg[2] + ((attributes & 0x08) != 0 ? 256 : 0);
                aso_draw_sprites(bitmap, scrollx, scrolly, Machine.gfx[2]);
            }

            {
                int bank = (attributes & 0x40) != 0 ? 1 : 0;
                tnk3_draw_text(bitmap, bank, new UBytePtr(ram, 0xf800));
                tnk3_draw_status(bitmap, bank, new UBytePtr(ram, 0xfc00));
            }
            /*
		{
			int i;
			for( i=0; i<6; i++ ){
				int data = hal21_vreg[i];
				drawgfx( bitmap, Machine.uifont,
					"0123456789abcdef"[data>>4],0,0,0,
					0,i*16,
					&Machine.visible_area,
					TRANSPARENCY_NONE,0 );
				drawgfx( bitmap, Machine.uifont,
					"0123456789abcdef"[data&0xf],0,0,0,
					8,i*16,
					&Machine.visible_area,
					TRANSPARENCY_NONE,0 );
			}
		}
             */
        }
    };
    static InputPortHandlerPtr input_ports_hal21 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* sound CPU status */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* P1 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* P2 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            /* unused */
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            /* ? */
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xc0, "20000 60000");
            PORT_DIPSETTING(0x80, "40000 90000");
            PORT_DIPSETTING(0x40, "50000 120000");
            PORT_DIPSETTING(0x00, "None");

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x01, 0x01, "Bonus Type");
            PORT_DIPSETTING(0x00, "Every Bonus Set");
            PORT_DIPSETTING(0x01, "Second Bonus Set");
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x06, "4");
            PORT_DIPNAME(0x18, 0x18, "Special");
            PORT_DIPSETTING(0x18, "Normal");
            PORT_DIPSETTING(0x10, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, "Infinite Lives");
            PORT_DIPSETTING(0x00, "Freeze");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen")); // 0x20 . fe65
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown")); // unused
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            /* ? */
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ***********************************************************************
     */
    static InputPortHandlerPtr input_ports_aso = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_VBLANK);
            /* ? */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, "Allow Continue");
            PORT_DIPSETTING(0x01, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x28, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xc0, "50k 100k");
            PORT_DIPSETTING(0x80, "60k 120k");
            PORT_DIPSETTING(0x40, "100k 200k");
            PORT_DIPSETTING(0x00, "None");

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, "Bonus Occurrence");
            PORT_DIPSETTING(0x01, "1st & every 2nd");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x06, "Easy");
            PORT_DIPSETTING(0x04, "Normal");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x10, 0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Cheat of some kind", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0xc0, "Start Area");
            PORT_DIPSETTING(0xc0, "1");
            PORT_DIPSETTING(0x80, "2");
            PORT_DIPSETTING(0x40, "3");
            PORT_DIPSETTING(0x00, "4");
            INPUT_PORTS_END();
        }
    };

    /**
     * ***********************************************************************
     */
    static GfxLayout char256 = new GfxLayout(
            8, 8,
            0x100,
            4,
            new int[]{0, 1, 2, 3},
            new int[]{4, 0, 12, 8, 20, 16, 28, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            256
    );

    static GfxLayout char1024 = new GfxLayout(
            8, 8,
            0x400,
            4,
            new int[]{0, 1, 2, 3},
            new int[]{4, 0, 12, 8, 20, 16, 28, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            256
    );

    static GfxLayout sprite1024 = new GfxLayout(
            16, 16,
            0x400,
            3,
            new int[]{2 * 1024 * 256, 1 * 1024 * 256, 0 * 1024 * 256},
            new int[]{
                7, 6, 5, 4, 3, 2, 1, 0,
                15, 14, 13, 12, 11, 10, 9, 8
            },
            new int[]{
                0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16
            },
            256
    );

    static GfxDecodeInfo aso_gfxdecodeinfo[]
            = {
                /* colors 512-1023 are currently unused, I think they are a second bank */
                new GfxDecodeInfo(REGION_GFX1, 0, char256, 128 * 3, 8), /* colors 384..511 */
                new GfxDecodeInfo(REGION_GFX2, 0, char1024, 128 * 1, 16), /* colors 128..383 */
                new GfxDecodeInfo(REGION_GFX3, 0, sprite1024, 128 * 0, 16), /* colors   0..127 */
                new GfxDecodeInfo(-1)
            };

    /**
     * ***********************************************************************
     */
    public static final int SNK_NMI_ENABLE = 1;
    public static final int SNK_NMI_PENDING = 2;

    static int snk_soundcommand = 0;
    static UBytePtr shared_ram = new UBytePtr();
    static UBytePtr shared_auxram = new UBytePtr();

    public static ReadHandlerPtr shared_auxram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return shared_auxram.read(offset);
        }
    };
    public static WriteHandlerPtr shared_auxram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            shared_auxram.write(offset, data);
        }
    };

    public static ReadHandlerPtr shared_ram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return shared_ram.read(offset);
        }
    };
    public static WriteHandlerPtr shared_ram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            shared_ram.write(offset, data);
        }
    };

    static int CPUA_latch = 0;
    static int CPUB_latch = 0;

    public static WriteHandlerPtr CPUA_int_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((CPUA_latch & SNK_NMI_PENDING) != 0) {
                cpu_cause_interrupt(0, Z80_NMI_INT);
                CPUA_latch = 0;
            } else {
                CPUA_latch |= SNK_NMI_ENABLE;
            }
        }
    };

    public static ReadHandlerPtr CPUA_int_trigger_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((CPUA_latch & SNK_NMI_ENABLE) != 0) {
                cpu_cause_interrupt(0, Z80_NMI_INT);
                CPUA_latch = 0;
            } else {
                CPUA_latch |= SNK_NMI_PENDING;
            }
            return 0xff;
        }
    };

    public static WriteHandlerPtr CPUB_int_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((CPUB_latch & SNK_NMI_PENDING) != 0) {
                cpu_cause_interrupt(1, Z80_NMI_INT);
                CPUB_latch = 0;
            } else {
                CPUB_latch |= SNK_NMI_ENABLE;
            }
        }
    };

    public static ReadHandlerPtr CPUB_int_trigger_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((CPUB_latch & SNK_NMI_ENABLE) != 0) {
                cpu_cause_interrupt(1, Z80_NMI_INT);
                CPUB_latch = 0;
            } else {
                CPUB_latch |= SNK_NMI_PENDING;
            }
            return 0xff;
        }
    };

    public static WriteHandlerPtr snk_soundcommand_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            snk_soundcommand = data;
            cpu_cause_interrupt(2, Z80_IRQ_INT);
            //	cpu_cause_interrupt(2, 0xff); old ASO
        }
    };

    public static ReadHandlerPtr snk_soundcommand_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int val = snk_soundcommand;
            snk_soundcommand = 0;
            return val;
        }
    };

    /**
     * ***********************************************************************
     */
    static YM3526interface ym3526_interface = new YM3526interface(
            1, /* number of chips */
            4000000, /* 4 MHz? (hand tuned) */
            new int[]{50} /* (not supported) */
    );

    static MemoryReadAddress aso_readmem_sound[] = {
        new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
        new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
        new MemoryReadAddress(0xd000, 0xd000, snk_soundcommand_r),
        new MemoryReadAddress(0xf000, 0xf000, YM3526_status_port_0_r),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress aso_writemem_sound[] = {
        new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
        new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
        new MemoryWriteAddress(0xf000, 0xf000, YM3526_control_port_0_w), /* YM3526 #1 control port? */
        new MemoryWriteAddress(0xf001, 0xf001, YM3526_write_port_0_w), /* YM3526 #1 write port?  */
        new MemoryWriteAddress(-1)
    };

    /**
     * ***********************************************************************
     */
    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* number of chips */
            2000000, /* 2 MHz */
            new int[]{35, 35},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MemoryReadAddress hal21_readmem_sound[] = {
        new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
        new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
        new MemoryReadAddress(0xa000, 0xa000, snk_soundcommand_r),
        //	new MemoryReadAddress( 0xc000, 0xc000, ack ),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress hal21_writemem_sound[] = {
        new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
        new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
        new MemoryWriteAddress(0xe000, 0xe000, AY8910_control_port_0_w),
        new MemoryWriteAddress(0xe001, 0xe001, AY8910_write_port_0_w),
        new MemoryWriteAddress(0xe008, 0xe008, AY8910_control_port_1_w),
        new MemoryWriteAddress(0xe009, 0xe009, AY8910_write_port_1_w),
        new MemoryWriteAddress(-1)
    };

    /**
     * ************************** ASO/Alpha Mission ************************
     */
    static MemoryReadAddress aso_readmem_cpuA[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc000, input_port_0_r), /* coin, start */
                new MemoryReadAddress(0xc100, 0xc100, input_port_1_r), /* P1 */
                new MemoryReadAddress(0xc200, 0xc200, input_port_2_r), /* P2 */
                new MemoryReadAddress(0xc500, 0xc500, input_port_3_r), /* DSW1 */
                new MemoryReadAddress(0xc600, 0xc600, input_port_4_r), /* DSW2 */
                new MemoryReadAddress(0xc700, 0xc700, CPUB_int_trigger_r),
                new MemoryReadAddress(0xd000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress aso_writemem_cpuA[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc400, 0xc400, snk_soundcommand_w),
                new MemoryWriteAddress(0xc700, 0xc700, CPUA_int_enable_w),
                new MemoryWriteAddress(0xc800, 0xc800, hal21_vreg1_w),
                new MemoryWriteAddress(0xc900, 0xc900, hal21_vreg2_w),
                new MemoryWriteAddress(0xca00, 0xca00, hal21_vreg3_w),
                new MemoryWriteAddress(0xcb00, 0xcb00, hal21_vreg4_w),
                new MemoryWriteAddress(0xcc00, 0xcc00, hal21_vreg5_w),
                new MemoryWriteAddress(0xcf00, 0xcf00, hal21_vreg0_w),
                new MemoryWriteAddress(0xd800, 0xdfff, MWA_RAM, shared_auxram),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM, spriteram),
                new MemoryWriteAddress(0xe800, 0xf7ff, videoram_w, videoram),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM, shared_ram),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress aso_readmem_cpuB[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc000, CPUA_int_trigger_r),
                new MemoryReadAddress(0xc800, 0xe7ff, shared_auxram_r),
                new MemoryReadAddress(0xe800, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xffff, shared_ram_r),
                new MemoryReadAddress(-1)
            };
    static MemoryWriteAddress aso_writemem_cpuB[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc000, CPUB_int_enable_w),
                new MemoryWriteAddress(0xc800, 0xd7ff, shared_auxram_w),
                new MemoryWriteAddress(0xd800, 0xe7ff, videoram_w),
                new MemoryWriteAddress(0xe800, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xffff, shared_ram_w),
                new MemoryWriteAddress(-1)
            };

    /**
     * ************************** HAL21 ************************
     */
    static MemoryReadAddress hal21_readmem_CPUA[] = {
        new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
        new MemoryReadAddress(0xc000, 0xc000, input_port_0_r), /* coin, start */
        new MemoryReadAddress(0xc100, 0xc100, input_port_1_r), /* P1 */
        new MemoryReadAddress(0xc200, 0xc200, input_port_2_r), /* P2 */
        new MemoryReadAddress(0xc400, 0xc400, input_port_3_r), /* DSW1 */
        new MemoryReadAddress(0xc500, 0xc500, input_port_4_r), /* DSW2 */
        new MemoryReadAddress(0xc700, 0xc700, CPUB_int_trigger_r),
        new MemoryReadAddress(0xe000, 0xefff, MRA_RAM),
        new MemoryReadAddress(0xf000, 0xffff, MRA_RAM),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress hal21_writemem_CPUA[] = {
        new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
        new MemoryWriteAddress(0xc300, 0xc300, snk_soundcommand_w),
        new MemoryWriteAddress(0xc600, 0xc600, hal21_vreg0_w),
        new MemoryWriteAddress(0xc700, 0xc700, CPUA_int_enable_w),
        new MemoryWriteAddress(0xd300, 0xd300, hal21_vreg1_w),
        new MemoryWriteAddress(0xd400, 0xd400, hal21_vreg2_w),
        new MemoryWriteAddress(0xd500, 0xd500, hal21_vreg3_w),
        new MemoryWriteAddress(0xd600, 0xd600, hal21_vreg4_w),
        new MemoryWriteAddress(0xd700, 0xd700, hal21_vreg5_w),
        new MemoryWriteAddress(0xe000, 0xefff, MWA_RAM, spriteram),
        new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM, shared_ram),
        new MemoryWriteAddress(-1)
    };

    public static ReadHandlerPtr hal21_spriteram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram.read(offset);
        }
    };
    public static WriteHandlerPtr hal21_spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram.write(offset, data);
        }
    };

    static MemoryReadAddress hal21_readmem_CPUB[] = {
        new MemoryReadAddress(0x0000, 0x9fff, MRA_ROM),
        new MemoryReadAddress(0xc000, 0xcfff, hal21_spriteram_r),
        new MemoryReadAddress(0xd000, 0xdfff, MRA_RAM), /* background */
        new MemoryReadAddress(0xe000, 0xefff, shared_ram_r),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress hal21_writemem_CPUB[] = {
        new MemoryWriteAddress(0x0000, 0x9fff, MWA_ROM),
        new MemoryWriteAddress(0xa000, 0xa000, CPUB_int_enable_w),
        new MemoryWriteAddress(0xc000, 0xcfff, hal21_spriteram_w),
        new MemoryWriteAddress(0xd000, 0xdfff, videoram_w, videoram),
        new MemoryWriteAddress(0xe000, 0xefff, shared_ram_w),
        new MemoryWriteAddress(-1)
    };

    /**
     * ***********************************************************************
     */
    static MachineDriver machine_driver_aso = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* ? */
                        aso_readmem_cpuA, aso_writemem_cpuA, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* ? */
                        aso_readmem_cpuB, aso_writemem_cpuB, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz (?) */
                        aso_readmem_sound, aso_writemem_sound, null, null,
                        interrupt, 1
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            100, /* CPU slices per frame */
            null, /* init machine */
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 1 * 8, 28 * 8 - 1),
            aso_gfxdecodeinfo,
            1024, 1024,
            aso_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            aso_vh_start,
            aso_vh_stop,
            aso_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                )
            }
    );

    static MachineDriver machine_driver_hal21 = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 MHz? */
                        hal21_readmem_CPUA, hal21_writemem_CPUA, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 MHz? */
                        hal21_readmem_CPUB, hal21_writemem_CPUB, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz (?) */
                        hal21_readmem_sound, hal21_writemem_sound, null, null,
                        interrupt, 1
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            100, /* CPU slices per frame */
            null, /* init_machine */
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 1 * 8, 28 * 8 - 1),
            aso_gfxdecodeinfo,
            1024, 1024,
            aso_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            hal21_vh_start,
            aso_vh_stop,
            aso_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /**
     * ***********************************************************************
     */
    static RomLoadHandlerPtr rom_hal21 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for CPUA code */
            ROM_LOAD("hal21p1.bin", 0x0000, 0x2000, 0x9d193830);
            ROM_LOAD("hal21p2.bin", 0x2000, 0x2000, 0xc1f00350);
            ROM_LOAD("hal21p3.bin", 0x4000, 0x2000, 0x881d22a6);
            ROM_LOAD("hal21p4.bin", 0x6000, 0x2000, 0xce692534);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for CPUB code */
            ROM_LOAD("hal21p5.bin", 0x0000, 0x2000, 0x3ce0684a);
            ROM_LOAD("hal21p6.bin", 0x2000, 0x2000, 0x878ef798);
            ROM_LOAD("hal21p7.bin", 0x4000, 0x2000, 0x72ebbe95);
            ROM_LOAD("hal21p8.bin", 0x6000, 0x2000, 0x17e22ad3);
            ROM_LOAD("hal21p9.bin", 0x8000, 0x2000, 0xb146f891);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */
            ROM_LOAD("hal21p10.bin", 0x0000, 0x4000, 0x916f7ba0);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hal21p12.bin", 0x0000, 0x2000, 0x9839a7cd);/* char */

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* background tiles */
            ROM_LOAD("hal21p11.bin", 0x0000, 0x4000, 0x24abc57e);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* 16x16 sprites */
            ROM_LOAD("hal21p13.bin", 0x00000, 0x4000, 0x052b4f4f);
            ROM_RELOAD(0x04000, 0x4000);
            ROM_LOAD("hal21p14.bin", 0x08000, 0x4000, 0xda0cb670);
            ROM_RELOAD(0x0c000, 0x4000);
            ROM_LOAD("hal21p15.bin", 0x10000, 0x4000, 0x5c5ea945);
            ROM_RELOAD(0x14000, 0x4000);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("hal21_1.prm", 0x000, 0x400, 0x195768fc);
            ROM_LOAD("hal21_2.prm", 0x400, 0x400, 0xc5d84225);
            ROM_LOAD("hal21_3.prm", 0x800, 0x400, 0x605afff8);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_hal21j = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for CPUA code */
            ROM_LOAD("hal21p1.bin", 0x0000, 0x2000, 0x9d193830);
            ROM_LOAD("hal21p2.bin", 0x2000, 0x2000, 0xc1f00350);
            ROM_LOAD("hal21p3.bin", 0x4000, 0x2000, 0x881d22a6);
            ROM_LOAD("hal21p4.bin", 0x6000, 0x2000, 0xce692534);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for CPUB code */
            ROM_LOAD("hal21p5.bin", 0x0000, 0x2000, 0x3ce0684a);
            ROM_LOAD("hal21p6.bin", 0x2000, 0x2000, 0x878ef798);
            ROM_LOAD("hal21p7.bin", 0x4000, 0x2000, 0x72ebbe95);
            ROM_LOAD("hal21p8.bin", 0x6000, 0x2000, 0x17e22ad3);
            ROM_LOAD("hal21p9.bin", 0x8000, 0x2000, 0xb146f891);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */
            ROM_LOAD("hal21-10.bin", 0x0000, 0x4000, 0xa182b3f0);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hal21p12.bin", 0x0000, 0x2000, 0x9839a7cd);/* char */

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* background tiles */
            ROM_LOAD("hal21p11.bin", 0x0000, 0x4000, 0x24abc57e);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* 16x16 sprites */
            ROM_LOAD("hal21p13.bin", 0x00000, 0x4000, 0x052b4f4f);
            ROM_RELOAD(0x04000, 0x4000);
            ROM_LOAD("hal21p14.bin", 0x08000, 0x4000, 0xda0cb670);
            ROM_RELOAD(0x0c000, 0x4000);
            ROM_LOAD("hal21p15.bin", 0x10000, 0x4000, 0x5c5ea945);
            ROM_RELOAD(0x14000, 0x4000);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("hal21_1.prm", 0x000, 0x400, 0x195768fc);
            ROM_LOAD("hal21_2.prm", 0x400, 0x400, 0xc5d84225);
            ROM_LOAD("hal21_3.prm", 0x800, 0x400, 0x605afff8);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_aso = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for cpuA code */
            ROM_LOAD("aso.1", 0x0000, 0x8000, 0x3fc9d5e4);
            ROM_LOAD("aso.3", 0x8000, 0x4000, 0x39a666d2);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for cpuB code */
            ROM_LOAD("aso.4", 0x0000, 0x8000, 0x2429792b);
            ROM_LOAD("aso.6", 0x8000, 0x4000, 0xc0bfdf1f);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */
            ROM_LOAD("aso.7", 0x0000, 0x8000, 0x49258162);
            /* YM3526 */
            ROM_LOAD("aso.9", 0x8000, 0x4000, 0xaef5a4f4);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */
            ROM_LOAD("aso.14", 0x0000, 0x2000, 0x8baa2253);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* background tiles */
            ROM_LOAD("aso.10", 0x0000, 0x8000, 0x00dff996);

            ROM_REGION(0x18000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* 16x16 sprites */
            ROM_LOAD("aso.11", 0x00000, 0x8000, 0x7feac86c);
            ROM_LOAD("aso.12", 0x08000, 0x8000, 0x6895990b);
            ROM_LOAD("aso.13", 0x10000, 0x8000, 0x87a81ce1);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("up02_f12.rom", 0x000, 0x00400, 0x5b0a0059);
            ROM_LOAD("up02_f13.rom", 0x400, 0x00400, 0x37e28dd8);
            ROM_LOAD("up02_f14.rom", 0x800, 0x00400, 0xc3fd1dd3);
            ROM_END();
        }
    };

    public static GameDriver driver_aso = new GameDriver("1985", "aso", "hal21.java", rom_aso, null, machine_driver_aso, input_ports_aso, null, ROT270_16BIT, "SNK", "ASO - Armored Scrum Object", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_hal21 = new GameDriver("1985", "hal21", "hal21.java", rom_hal21, null, machine_driver_hal21, input_ports_hal21, null, ROT270_16BIT, "SNK", "HAL21", GAME_NO_SOUND | GAME_WRONG_COLORS);
    public static GameDriver driver_hal21j = new GameDriver("1985", "hal21j", "hal21.java", rom_hal21j, driver_hal21, machine_driver_hal21, input_ports_hal21, null, ROT270_16BIT, "SNK", "HAL21 (Japan)", GAME_NO_SOUND | GAME_WRONG_COLORS);
}
