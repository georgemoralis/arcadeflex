/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.sndhrdw.phoenix.*;
import static arcadeflex.v036.sndhrdw.pleiads.*;
import static arcadeflex.v036.sound.tms36xxH.*;
import static arcadeflex.v036.vidhrdw.phoenix.*;

public class phoenix {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4fff, phoenix_paged_ram_r), /* 2 pages selected by Bit 0 of videoregister */
                new MemoryReadAddress(0x7000, 0x73ff, phoenix_input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x7800, 0x7bff, input_port_1_r), /* DSW */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress phoenix_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x4fff, phoenix_paged_ram_w), /* 2 pages selected by Bit 0 of the video register */
                new MemoryWriteAddress(0x5000, 0x53ff, phoenix_videoreg_w),
                new MemoryWriteAddress(0x5800, 0x5bff, phoenix_scroll_w), /* the game sometimes writes at mirror addresses */
                new MemoryWriteAddress(0x6000, 0x63ff, phoenix_sound_control_a_w),
                new MemoryWriteAddress(0x6800, 0x6bff, phoenix_sound_control_b_w),
                new MemoryWriteAddress(-1) /* end of table */};
    static MemoryWriteAddress pleiads_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x4fff, phoenix_paged_ram_w), /* 2 pages selected by Bit 0 of the video register */
                new MemoryWriteAddress(0x5000, 0x53ff, phoenix_videoreg_w),
                new MemoryWriteAddress(0x5800, 0x5bff, phoenix_scroll_w), /* the game sometimes writes at mirror addresses */
                new MemoryWriteAddress(0x6000, 0x63ff, pleiads_sound_control_a_w),
                new MemoryWriteAddress(0x6800, 0x6bff, pleiads_sound_control_b_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_phoenix = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_DIPSETTING(0x0c, "6000");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_phoenixa = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_DIPSETTING(0x0c, "6000");
            /* Coinage is backwards from phoenix (Amstar) */
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_phoenixt = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_DIPSETTING(0x0c, "6000");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_phoenix3 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_DIPSETTING(0x0c, "6000");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pleiads = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* Protection. See 0x0552 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x04, "4000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_DIPSETTING(0x0c, "6000");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{256 * 8 * 8, 0}, /* the two bitplanes are separated */
            new int[]{7, 6, 5, 4, 3, 2, 1, 0}, /* pretty straightforward layout */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, charlayout, 16 * 4, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static TMS36XXinterface phoenix_tms36xx_interface = new TMS36XXinterface(
            1,
            new int[]{50}, /* mixing levels */
            new int[]{MM6221AA}, /* TMS36xx subtype(s) */
            new int[]{372}, /* base frequency */
            new double[][]{{0.50, 0, 0, 1.05, 0, 0}}, /* decay times of voices */
            new double[]{0.21} /* tune speed (time between beats) */
    );

    static CustomSound_interface phoenix_custom_interface = new CustomSound_interface(
            phoenix_sh_start,
            phoenix_sh_stop,
            phoenix_sh_update
    );

    static TMS36XXinterface pleiads_tms36xx_interface = new TMS36XXinterface(
            1,
            new int[]{75}, /* mixing levels */
            new int[]{TMS3615}, /* TMS36xx subtype(s) */
            new int[]{247}, /* base frequencies (one octave below A) */
            /*
		 * Decay times of the voices; NOTE: it's unknown if
		 * the the TMS3615 mixes more than one voice internally.
		 * A wav taken from Pop Flamer sounds like there
		 * are at least no 'odd' harmonics (5 1/3' and 2 2/3')
             */
            new double[][]{{0.33, 0.33, 0, 0.33, 0, 0.33}}
    );

    static CustomSound_interface pleiads_custom_interface = new CustomSound_interface(
            pleiads_sh_start,
            pleiads_sh_stop,
            pleiads_sh_update
    );

    static MachineDriver machine_driver_phoenix = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        3072000, /* 3 MHz ? */
                        readmem, phoenix_writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 31 * 8 - 1, 0 * 8, 26 * 8 - 1),
            gfxdecodeinfo,
            256, 16 * 4 + 16 * 4,
            phoenix_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            phoenix_vh_start,
            phoenix_vh_stop,
            phoenix_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_TMS36XX,
                        phoenix_tms36xx_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM,
                        phoenix_custom_interface
                )
            }
    );
    static MachineDriver machine_driver_pleiads = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        3072000, /* 3 MHz ? */
                        readmem, pleiads_writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 31 * 8 - 1, 0 * 8, 26 * 8 - 1),
            gfxdecodeinfo,
            256, 16 * 4 + 16 * 4,
            phoenix_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            phoenix_vh_start,
            phoenix_vh_stop,
            phoenix_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_TMS36XX,
                        pleiads_tms36xx_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM,
                        pleiads_custom_interface
                )
            }
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_phoenix = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic45", 0x0000, 0x0800, 0x9f68086b);
            ROM_LOAD("ic46", 0x0800, 0x0800, 0x273a4a82);
            ROM_LOAD("ic47", 0x1000, 0x0800, 0x3d4284b9);
            ROM_LOAD("ic48", 0x1800, 0x0800, 0xcb5d9915);
            ROM_LOAD("ic49", 0x2000, 0x0800, 0xa105e4e7);
            ROM_LOAD("ic50", 0x2800, 0x0800, 0xac5e9ec1);
            ROM_LOAD("ic51", 0x3000, 0x0800, 0x2eab35b4);
            ROM_LOAD("ic52", 0x3800, 0x0800, 0xaff8e9c5);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23", 0x0000, 0x0800, 0x3c7e623f);
            ROM_LOAD("ic24", 0x0800, 0x0800, 0x59916d3b);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39", 0x0000, 0x0800, 0x53413e8f);
            ROM_LOAD("ic40", 0x0800, 0x0800, 0x0be2ba91);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("ic40_b.bin", 0x0000, 0x0100, 0x79350b25);
            /* palette low bits */
            ROM_LOAD("ic41_a.bin", 0x0100, 0x0100, 0xe176b768);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_phoenixa = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic45.k1", 0x0000, 0x0800, 0xc7a9b499);
            ROM_LOAD("ic46.k2", 0x0800, 0x0800, 0xd0e6ae1b);
            ROM_LOAD("ic47.k3", 0x1000, 0x0800, 0x64bf463a);
            ROM_LOAD("ic48.k4", 0x1800, 0x0800, 0x1b20fe62);
            ROM_LOAD("phoenixc.49", 0x2000, 0x0800, 0x1a1ce0d0);
            ROM_LOAD("ic50", 0x2800, 0x0800, 0xac5e9ec1);
            ROM_LOAD("ic51", 0x3000, 0x0800, 0x2eab35b4);
            ROM_LOAD("ic52", 0x3800, 0x0800, 0xaff8e9c5);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23", 0x0000, 0x0800, 0x3c7e623f);
            ROM_LOAD("ic24", 0x0800, 0x0800, 0x59916d3b);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("phoenixc.39", 0x0000, 0x0800, 0xbb0525ed);
            ROM_LOAD("phoenixc.40", 0x0800, 0x0800, 0x4178aa4f);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("ic40_b.bin", 0x0000, 0x0100, 0x79350b25);
            /* palette low bits */
            ROM_LOAD("ic41_a.bin", 0x0100, 0x0100, 0xe176b768);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_phoenixt = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("phoenix.45", 0x0000, 0x0800, 0x5b8c55a8);
            ROM_LOAD("phoenix.46", 0x0800, 0x0800, 0xdbc942fa);
            ROM_LOAD("phoenix.47", 0x1000, 0x0800, 0xcbbb8839);
            ROM_LOAD("phoenix.48", 0x1800, 0x0800, 0xcb65eff8);
            ROM_LOAD("phoenix.49", 0x2000, 0x0800, 0xc8a5d6d6);
            ROM_LOAD("ic50", 0x2800, 0x0800, 0xac5e9ec1);
            ROM_LOAD("ic51", 0x3000, 0x0800, 0x2eab35b4);
            ROM_LOAD("phoenix.52", 0x3800, 0x0800, 0xb9915263);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23", 0x0000, 0x0800, 0x3c7e623f);
            ROM_LOAD("ic24", 0x0800, 0x0800, 0x59916d3b);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39", 0x0000, 0x0800, 0x53413e8f);
            ROM_LOAD("ic40", 0x0800, 0x0800, 0x0be2ba91);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("ic40_b.bin", 0x0000, 0x0100, 0x79350b25);
            /* palette low bits */
            ROM_LOAD("ic41_a.bin", 0x0100, 0x0100, 0xe176b768);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_phoenix3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("phoenix3.45", 0x0000, 0x0800, 0xa362cda0);
            ROM_LOAD("phoenix3.46", 0x0800, 0x0800, 0x5748f486);
            ROM_LOAD("phoenix.47", 0x1000, 0x0800, 0xcbbb8839);
            ROM_LOAD("phoenix3.48", 0x1800, 0x0800, 0xb5d97a4d);
            ROM_LOAD("ic49", 0x2000, 0x0800, 0xa105e4e7);
            ROM_LOAD("ic50", 0x2800, 0x0800, 0xac5e9ec1);
            ROM_LOAD("ic51", 0x3000, 0x0800, 0x2eab35b4);
            ROM_LOAD("phoenix3.52", 0x3800, 0x0800, 0xd2c5c984);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23", 0x0000, 0x0800, 0x3c7e623f);
            ROM_LOAD("ic24", 0x0800, 0x0800, 0x59916d3b);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39", 0x0000, 0x0800, 0x53413e8f);
            ROM_LOAD("ic40", 0x0800, 0x0800, 0x0be2ba91);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("ic40_b.bin", 0x0000, 0x0100, 0x79350b25);
            /* palette low bits */
            ROM_LOAD("ic41_a.bin", 0x0100, 0x0100, 0xe176b768);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_phoenixc = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("phoenix.45", 0x0000, 0x0800, 0x5b8c55a8);
            ROM_LOAD("phoenix.46", 0x0800, 0x0800, 0xdbc942fa);
            ROM_LOAD("phoenix.47", 0x1000, 0x0800, 0xcbbb8839);
            ROM_LOAD("phoenixc.48", 0x1800, 0x0800, 0x5ae0b215);
            ROM_LOAD("phoenixc.49", 0x2000, 0x0800, 0x1a1ce0d0);
            ROM_LOAD("ic50", 0x2800, 0x0800, 0xac5e9ec1);
            ROM_LOAD("ic51", 0x3000, 0x0800, 0x2eab35b4);
            ROM_LOAD("phoenixc.52", 0x3800, 0x0800, 0x8424d7c4);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23", 0x0000, 0x0800, 0x3c7e623f);
            ROM_LOAD("ic24", 0x0800, 0x0800, 0x59916d3b);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("phoenixc.39", 0x0000, 0x0800, 0xbb0525ed);
            ROM_LOAD("phoenixc.40", 0x0800, 0x0800, 0x4178aa4f);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("ic40_b.bin", 0x0000, 0x0100, 0x79350b25);
            /* palette low bits */
            ROM_LOAD("ic41_a.bin", 0x0100, 0x0100, 0xe176b768);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_pleiads = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic47.r1", 0x0000, 0x0800, 0x960212c8);
            ROM_LOAD("ic48.r2", 0x0800, 0x0800, 0xb254217c);
            ROM_LOAD("ic47.bin", 0x1000, 0x0800, 0x87e700bb);/* IC 49 on real board */
            ROM_LOAD("ic48.bin", 0x1800, 0x0800, 0x2d5198d0);/* IC 50 on real board */
            ROM_LOAD("ic51.r5", 0x2000, 0x0800, 0x49c629bc);
            ROM_LOAD("ic50.bin", 0x2800, 0x0800, 0xf1a8a00d);/* IC 52 on real board */
            ROM_LOAD("ic53.r7", 0x3000, 0x0800, 0xb5f07fbc);
            ROM_LOAD("ic52.bin", 0x3800, 0x0800, 0xb1b5a8a6);/* IC 54 on real board */

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23.bin", 0x0000, 0x0800, 0x4e30f9e7);/* IC 45 on real board */
            ROM_LOAD("ic24.bin", 0x0800, 0x0800, 0x5188fc29);/* IC 44 on real board */

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39.bin", 0x0000, 0x0800, 0x85866607);/* IC 27 on real board */
            ROM_LOAD("ic40.bin", 0x0800, 0x0800, 0xa841d511);/* IC 26 on real board */

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("7611-5.26", 0x0000, 0x0100, 0x7a1bcb1e);
            /* palette low bits */
            ROM_LOAD("7611-5.33", 0x0100, 0x0100, 0xe38eeb83);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_pleiadbl = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic45.bin", 0x0000, 0x0800, 0x93fc2958);
            ROM_LOAD("ic46.bin", 0x0800, 0x0800, 0xe2b5b8cd);
            ROM_LOAD("ic47.bin", 0x1000, 0x0800, 0x87e700bb);
            ROM_LOAD("ic48.bin", 0x1800, 0x0800, 0x2d5198d0);
            ROM_LOAD("ic49.bin", 0x2000, 0x0800, 0x9dc73e63);
            ROM_LOAD("ic50.bin", 0x2800, 0x0800, 0xf1a8a00d);
            ROM_LOAD("ic51.bin", 0x3000, 0x0800, 0x6f56f317);
            ROM_LOAD("ic52.bin", 0x3800, 0x0800, 0xb1b5a8a6);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic23.bin", 0x0000, 0x0800, 0x4e30f9e7);
            ROM_LOAD("ic24.bin", 0x0800, 0x0800, 0x5188fc29);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39.bin", 0x0000, 0x0800, 0x85866607);
            ROM_LOAD("ic40.bin", 0x0800, 0x0800, 0xa841d511);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("7611-5.26", 0x0000, 0x0100, 0x7a1bcb1e);
            /* palette low bits */
            ROM_LOAD("7611-5.33", 0x0100, 0x0100, 0xe38eeb83);
            /* palette high bits */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_pleiadce = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("pleiades.47", 0x0000, 0x0800, 0x711e2ba0);
            ROM_LOAD("pleiades.48", 0x0800, 0x0800, 0x93a36943);
            ROM_LOAD("ic47.bin", 0x1000, 0x0800, 0x87e700bb);
            ROM_LOAD("pleiades.50", 0x1800, 0x0800, 0x5a9beba0);
            ROM_LOAD("pleiades.51", 0x2000, 0x0800, 0x1d828719);
            ROM_LOAD("ic50.bin", 0x2800, 0x0800, 0xf1a8a00d);
            ROM_LOAD("pleiades.53", 0x3000, 0x0800, 0x037b319c);
            ROM_LOAD("pleiades.54", 0x3800, 0x0800, 0xca264c7c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("pleiades.45", 0x0000, 0x0800, 0x8dbd3785);
            ROM_LOAD("pleiades.44", 0x0800, 0x0800, 0x0db3e436);

            ROM_REGION(0x1000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic39.bin", 0x0000, 0x0800, 0x85866607);
            ROM_LOAD("ic40.bin", 0x0800, 0x0800, 0xa841d511);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("7611-5.26", 0x0000, 0x0100, 0x7a1bcb1e);
            /* palette low bits */
            ROM_LOAD("7611-5.33", 0x0100, 0x0100, 0xe38eeb83);
            /* palette high bits */
            ROM_END();
        }
    };

    public static GameDriver driver_phoenix = new GameDriver("1980", "phoenix", "phoenix.java", rom_phoenix, null, machine_driver_phoenix, input_ports_phoenix, null, ROT90, "Amstar", "Phoenix (Amstar)", GAME_NO_COCKTAIL);
    public static GameDriver driver_phoenixa = new GameDriver("1980", "phoenixa", "phoenix.java", rom_phoenixa, driver_phoenix, machine_driver_phoenix, input_ports_phoenixa, null, ROT90, "Amstar (Centuri license)", "Phoenix (Centuri)", GAME_NO_COCKTAIL);
    public static GameDriver driver_phoenixt = new GameDriver("1980", "phoenixt", "phoenix.java", rom_phoenixt, driver_phoenix, machine_driver_phoenix, input_ports_phoenixt, null, ROT90, "Taito", "Phoenix (Taito)", GAME_NO_COCKTAIL);
    public static GameDriver driver_phoenix3 = new GameDriver("1980", "phoenix3", "phoenix.java", rom_phoenix3, driver_phoenix, machine_driver_phoenix, input_ports_phoenix3, null, ROT90, "bootleg", "Phoenix (T.P.N.)", GAME_NO_COCKTAIL);
    public static GameDriver driver_phoenixc = new GameDriver("1981", "phoenixc", "phoenix.java", rom_phoenixc, driver_phoenix, machine_driver_phoenix, input_ports_phoenixt, null, ROT90, "bootleg?", "Phoenix (IRECSA, G.G.I Corp)", GAME_NO_COCKTAIL);
    public static GameDriver driver_pleiads = new GameDriver("1981", "pleiads", "phoenix.java", rom_pleiads, null, machine_driver_pleiads, input_ports_pleiads, null, ROT90, "Tehkan", "Pleiads (Tehkan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_pleiadbl = new GameDriver("1981", "pleiadbl", "phoenix.java", rom_pleiadbl, driver_pleiads, machine_driver_pleiads, input_ports_pleiads, null, ROT90, "bootleg", "Pleiads (bootleg)", GAME_NO_COCKTAIL);
    public static GameDriver driver_pleiadce = new GameDriver("1981", "pleiadce", "phoenix.java", rom_pleiadce, driver_pleiads, machine_driver_pleiads, input_ports_pleiads, null, ROT90, "Tehkan (Centuri license)", "Pleiads (Centuri)", GAME_NO_COCKTAIL);

}
