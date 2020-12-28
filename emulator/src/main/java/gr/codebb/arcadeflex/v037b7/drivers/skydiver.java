/**
 * ported to 0.37b7
 * ported to 0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.skydiver.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;

public class skydiver {

    /* vidhrdw/skydiver.c */
    static int skydiver_nmion;

    public static ReadHandlerPtr skydiver_input_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = input_port_0_r.handler(0);

            switch (offset) {
                case 0:
                    return ((data & 0x03) << 6);
                case 1:
                    return ((data & 0x0C) << 4);
                case 2:
                    return ((data & 0x30) << 2);
                case 3:
                    return ((data & 0xC0) << 0);
                default:
                    return 0;
            }
        }
    };

    public static ReadHandlerPtr skydiver_input_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = input_port_1_r.handler(0);

            switch (offset) {
                case 0:
                    return ((data & 0x03) << 6);
                case 1:
                    return ((data & 0x0C) << 4);
                case 2:
                    return ((data & 0x30) << 2);
                case 3:
                    return ((data & 0xC0) << 0);
                default:
                    return 0;
            }
        }
    };

    public static ReadHandlerPtr skydiver_input_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = input_port_2_r.handler(0);

            switch (offset) {
                case 0:
                    return ((data & 0x03) << 6);
                case 1:
                    return ((data & 0x0C) << 4);
                case 2:
                    return ((data & 0x30) << 2);
                case 3:
                    return ((data & 0xC0) << 0);
                default:
                    return 0;
            }
        }
    };

    public static ReadHandlerPtr skydiver_input_3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = input_port_3_r.handler(0);

            switch (offset) {
                case 0:
                    return ((data & 0x03) << 6);
                case 1:
                    return ((data & 0x0C) << 4);
                case 2:
                    return ((data & 0x30) << 2);
                case 3:
                    return ((data & 0xC0) << 0);
                default:
                    return 0;
            }
        }
    };

    public static WriteHandlerPtr skydiver_nmion_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //	logerror("nmi_on: %02x:%02x\n", offset, data);
            skydiver_nmion = offset;
        }
    };

    public static InterruptPtr skydiver_interrupt = new InterruptPtr() {
        public int handler() {
            if (skydiver_nmion != 0) {
                return nmi_interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x00ff, MRA_RAM),
                new MemoryReadAddress(0x0400, 0x077f, MRA_RAM),
                //  new MemoryReadAddress( 0x780, 0x7ff, MRA_RAM ),
                new MemoryReadAddress(0x1800, 0x1803, skydiver_input_0_r),
                new MemoryReadAddress(0x1804, 0x1807, skydiver_input_1_r),
                new MemoryReadAddress(0x1808, 0x180b, skydiver_input_2_r),
                new MemoryReadAddress(0x1810, 0x1811, skydiver_input_3_r),
                new MemoryReadAddress(0x2000, 0x2000, watchdog_reset_r),
                new MemoryReadAddress(0x2800, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x7800, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x00ff, MWA_RAM),
                new MemoryWriteAddress(0x0010, 0x001f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x0400, 0x077f, videoram_w, videoram, videoram_size),
                // new MemoryWriteAddress( 0x0780, 0x07ff, MWA_RAM ),
                new MemoryWriteAddress(0x0800, 0x0803, skydiver_sk_lamps_w),
                // new MemoryWriteAddress( 0x0804, 0x0807, skydiver_start_lamps_w ),
                new MemoryWriteAddress(0x0808, 0x080b, skydiver_yd_lamps_w),
                // new MemoryWriteAddress( 0x080c, 0x080d, skydiver_sound_enable_w ),
                // new MemoryWriteAddress( 0x1000, 0x1001, skydiver_jump1_lamps_w ),
                // new MemoryWriteAddress( 0x1002, 0x1003, skydiver_coin_lockout_w ),
                // new MemoryWriteAddress( 0x1006, 0x1007, skydiver_jump2_lamps_w ),
                // new MemoryWriteAddress( 0x1008, 0x100b, skydiver_whistle_w ),
                new MemoryWriteAddress(0x100c, 0x100d, skydiver_nmion_w),
                new MemoryWriteAddress(0x100e, 0x100f, skydiver_width_w),
                new MemoryWriteAddress(0x2002, 0x2009, skydiver_iver_lamps_w),
                // new MemoryWriteAddress( 0x200a, 0x200d, skydiver_oct_w ),
                // new MemoryWriteAddress( 0x200e, 0x200f, skydiver_noise_reset_w ),
                new MemoryWriteAddress(0x2800, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_skydiver = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* fake port, gets mapped to Sky Diver ports */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);/* Jump 1 */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);/* Chute 1 */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);/* Jump 2 */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);/* Chute 2 */

            PORT_START();
            /* fake port, gets mapped to Sky Diver ports */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN, "(D);OPT SW NEXT TEST", KEYCODE_D, IP_JOY_NONE);
            PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN, "(F);OPT SW", KEYCODE_F, IP_JOY_NONE);
            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN, "(E);OPT SW", KEYCODE_E, IP_JOY_NONE);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN, "(H);OPT SW DIAGNOSTICS", KEYCODE_H, IP_JOY_NONE);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN1, 1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN2, 1);

            PORT_START();
            /* fake port, gets mapped to Sky Diver ports */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x08, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x20, 0x00, "Extended Play");
            PORT_DIPSETTING(0x20, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0xc0, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "French");
            PORT_DIPSETTING(0x80, "Spanish");
            PORT_DIPSETTING(0xc0, "German");

            PORT_START();
            /* fake port, gets mapped to Sky Diver ports */

            PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_SERVICE | IPF_TOGGLE, "Self Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_VBLANK);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0xF8, IP_ACTIVE_HIGH, IPT_UNUSED);

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            64, /* 64 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{7, 6, 5, 4, 15, 14, 13, 12},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            8 * 16 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout motion_layout = new GfxLayout(
            16, 16, /* 16*16 characters */
            32, /* 32 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{4, 5, 6, 7, 4 + 0x400 * 8, 5 + 0x400 * 8, 6 + 0x400 * 8, 7 + 0x400 * 8,
                12, 13, 14, 15, 12 + 0x400 * 8, 13 + 0x400 * 8, 14 + 0x400 * 8, 15 + 0x400 * 8},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            8 * 32 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout wide_motion_layout = new GfxLayout(
            32, 16, /* 32*16 characters */
            32, /* 32 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{4, 4, 5, 5, 6, 6, 7, 7,
                4 + 0x400 * 8, 4 + 0x400 * 8, 5 + 0x400 * 8, 5 + 0x400 * 8,
                6 + 0x400 * 8, 6 + 0x400 * 8, 7 + 0x400 * 8, 7 + 0x400 * 8,
                12, 12, 13, 13, 14, 14, 15, 15,
                12 + 0x400 * 8, 12 + 0x400 * 8, 13 + 0x400 * 8, 13 + 0x400 * 8,
                14 + 0x400 * 8, 14 + 0x400 * 8, 15 + 0x400 * 8, 15 + 0x400 * 8},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            8 * 32 /* every char takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 6),
                new GfxDecodeInfo(REGION_GFX2, 0, motion_layout, 0, 6),
                new GfxDecodeInfo(REGION_GFX2, 0, wide_motion_layout, 0, 6),
                new GfxDecodeInfo(-1) /* end of array */};

    static char palette[]
            = {
                0x00, 0x00, 0x00, /* BLACK */
                0xbf, 0xbf, 0xff, /* LT BLUE */
                0x7f, 0x7f, 0xff, /* BLUE */};

    static char colortable[]
            = {
                0x02, 0x01,
                0x02, 0x00,
                0x01, 0x02,
                0x00, 0x02,
                0x00, 0x00, /* used only to draw the SKYDIVER LEDs */
                0x00, 0x01, /* used only to draw the SKYDIVER LEDs */};
    public static VhConvertColorPromPtr init_palette = new VhConvertColorPromPtr() {
        public void handler(char[] game_palette, char[] game_colortable, UBytePtr color_prom) {
            //memcpy(game_palette,palette,sizeof(palette));
            //memcpy(game_colortable,colortable,sizeof(colortable));
            for (int i = 0; i < palette.length; i++) {
                game_palette[i]=(char) palette[i];
            }
            for (int i = 0; i < colortable.length; i++) {
                game_colortable[i] = colortable[i];
            }
        }
    };

    static MachineDriver machine_driver_skydiver = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6800,
                        3000000 / 4, /* ???? */
                        readmem, writemem, null, null,
                        skydiver_interrupt, 8
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 29 * 8 - 1),
            gfxdecodeinfo,
            palette.length / 3, colortable.length,
            init_palette,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            skydiver_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, null
    );

    /**
     * *************************************************************************
     *
     * Game ROMs
     *
     **************************************************************************
     */
    static RomLoadPtr rom_skydiver = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("33167-02.f1", 0x2800, 0x0800, 0x25a5c976);
            ROM_LOAD("33164-02.e1", 0x3000, 0x0800, 0xa348ac39);
            ROM_LOAD("33165-02.d1", 0x3800, 0x0800, 0xa1fc5504);
            ROM_LOAD("33166-02.c1", 0x7800, 0x0800, 0x3d26da2b);
            ROM_RELOAD(0xF800, 0x0800);

            ROM_REGION(0x0400, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("33163-01.h5", 0x0000, 0x0400, 0x5b9bb7c2);

            ROM_REGION(0x0800, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("33176-01.l5", 0x0000, 0x0400, 0x6b082a01);
            ROM_LOAD("33177-01.k5", 0x0400, 0x0400, 0xf5541af0);
            ROM_END();
        }
    };

    public static GameDriver driver_skydiver = new GameDriver("1978", "skydiver", "skydiver.java", rom_skydiver, null, machine_driver_skydiver, input_ports_skydiver, null, ROT0, "Atari", "Sky Diver", GAME_NO_SOUND);
}
