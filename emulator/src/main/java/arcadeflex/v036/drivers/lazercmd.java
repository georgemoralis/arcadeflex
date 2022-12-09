/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.s2650.s2650H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.lazercmd.*;
import static arcadeflex.v036.vidhrdw.lazercmdH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.mame.common.memory_region;
import static arcadeflex.v036.mame.commonH.REGIONFLAG_DISPOSE;
import static arcadeflex.v036.mame.commonH.REGION_CPU1;
import static arcadeflex.v036.mame.commonH.REGION_GFX1;
import static arcadeflex.v036.mame.commonH.ROM_END;
import static arcadeflex.v036.mame.commonH.ROM_LOAD;
import static arcadeflex.v036.mame.commonH.ROM_REGION;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.KEYCODE_X;
import static arcadeflex.v036.mame.inputH.KEYCODE_Z;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_DAC;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sizeof;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import gr.codebb.arcadeflex.v037b7.mame.memoryH.IOReadPort;
import gr.codebb.arcadeflex.v037b7.mame.memoryH.IOWritePort;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.MRA_RAM;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.MRA_ROM;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.MWA_ROM;
import gr.codebb.arcadeflex.v037b7.mame.memoryH.MemoryReadAddress;
import gr.codebb.arcadeflex.v037b7.mame.memoryH.MemoryWriteAddress;
import static arcadeflex.v036.sound.dac.DAC_data_w;
import arcadeflex.v036.sound.dacH.DACinterface;

public class lazercmd {

    public static int marker_x, marker_y;

    /**
     * ***********************************************************
     *
     * Statics
     *
     ************************************************************
     */
    static int timer_count = 0;

    /**
     * ***********************************************************
     * Interrupt for the cpu Fake something toggling the sense input line of the
     * S2650 The rate should be at about 1 Hz
     * ***********************************************************
     */
    static int sense_state = 0;
    public static InterruptHandlerPtr lazercmd_timer = new InterruptHandlerPtr() {
        public int handler() {
            if (++timer_count >= 64 * 128) {
                timer_count = 0;
                sense_state ^= 1;
                cpu_set_irq_line(0, 1, (sense_state) != 0 ? ASSERT_LINE : CLEAR_LINE);
            }
            return ignore_interrupt.handler();
        }
    };

    /**
     * ***********************************************************
     *
     * IO port read/write
     *
     ************************************************************
     */
    /* triggered by WRTC,r opcode */
    public static WriteHandlerPtr lazercmd_ctrl_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };

    /* triggered by REDC,r opcode */
    public static ReadHandlerPtr lazercmd_ctrl_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = 0;
            return data;
        }
    };

    /* triggered by WRTD,r opcode */
    public static WriteHandlerPtr lazercmd_data_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };

    /* triggered by REDD,r opcode */
    public static ReadHandlerPtr lazercmd_data_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data;
            data = input_port_2_r.handler(0) & 0x0f;
            return data;
        }
    };
    static int DAC_data = 0;

    public static WriteHandlerPtr lazercmd_hardware_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (offset) {
                case 0:
                    /* audio channels */
                    DAC_data = (data & 0x80) ^ ((data & 0x40) << 1) ^ ((data & 0x20) << 2) ^ ((data & 0x10) << 3);
                    if (DAC_data != 0) {
                        DAC_data_w.handler(0, 0xff);
                    } else {
                        DAC_data_w.handler(0, 0);
                    }
                    break;
                case 1:
                    /* marker Y position */
                    lazercmd_marker_dirty(0);
                    /* mark old position dirty */
                    marker_y = data;
                    break;
                case 2:
                    /* marker X position */
                    lazercmd_marker_dirty(0);
                    /* mark old position dirty */
                    marker_x = data;
                    break;
                case 3:
                    /* D4 clears coin detected and D0 toggles on attract mode */
                    break;
            }
        }
    };
    static int DAC_data1 = 0;
    public static WriteHandlerPtr medlanes_hardware_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            switch (offset) {
                case 0:
                    /* audio control */
 /* bits 4 and 5 are used to control a sound board */
 /* these could be used to control sound samples */
 /* at the moment they are routed through the dac */
                    DAC_data1 = ((data & 0x20) << 2) ^ ((data & 0x10) << 3);
                    if (DAC_data1 != 0) {
                        DAC_data_w.handler(0, 0xff);
                    } else {
                        DAC_data_w.handler(0, 0);
                    }
                    break;
                case 1:
                    /* marker Y position */
                    lazercmd_marker_dirty(0);
                    /* mark old position dirty */
                    marker_y = data;
                    break;
                case 2:
                    /* marker X position */
                    lazercmd_marker_dirty(0);
                    /* mark old position dirty */
                    marker_x = data;
                    break;
                case 3:
                    /* D4 clears coin detected and D0 toggles on attract mode */
                    break;
            }
        }
    };

    public static ReadHandlerPtr lazercmd_hardware_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int data = 0;

            switch (offset) {
                case 0:
                    /* player 1 joysticks */
                    data = input_port_0_r.handler(0);
                    break;
                case 1:
                    /* player 2 joysticks */
                    data = input_port_1_r.handler(0);
                    break;
                case 2:
                    /* player 1 + 2 buttons */
                    data = input_port_4_r.handler(0);
                    break;
                case 3:
                    /* coin slot + start buttons */
                    data = input_port_3_r.handler(0);
                    break;
                case 4:
                    /* vertical scan counter */
                    data = ((timer_count & 0x10) >> 1) | ((timer_count & 0x20) >> 3) | ((timer_count & 0x40) >> 5) | ((timer_count & 0x80) >> 7);
                    break;
                case 5:
                    /* vertical scan counter */
                    data = timer_count & 0x0f;
                    break;
                case 6:
                    /* 1f02 readback */
                    data = marker_x;
                    break;
                case 7:
                    /* 1f01 readback */
                    data = marker_y;
                    break;
            }
            return data;
        }
    };

    static MemoryWriteAddress lazercmd_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0bff, MWA_ROM),
                new MemoryWriteAddress(0x1c20, 0x1eff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1f00, 0x1f03, lazercmd_hardware_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress lazercmd_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0bff, MRA_ROM),
                new MemoryReadAddress(0x1c20, 0x1eff, MRA_RAM),
                new MemoryReadAddress(0x1f00, 0x1f03, lazercmd_hardware_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress medlanes_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0bff, MWA_ROM),
                new MemoryWriteAddress(0x1000, 0x1800, MWA_ROM),
                new MemoryWriteAddress(0x1c20, 0x1eff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1f00, 0x1f03, medlanes_hardware_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress medlanes_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0bff, MRA_ROM),
                new MemoryReadAddress(0x1000, 0x1800, MRA_ROM),
                new MemoryReadAddress(0x1c20, 0x1eff, MRA_RAM),
                new MemoryReadAddress(0x1f00, 0x1f03, lazercmd_hardware_r),
                new MemoryReadAddress(-1) /* end of table */};

    static IOWritePort lazercmd_writeport[]
            = {
                new IOWritePort(S2650_CTRL_PORT, S2650_CTRL_PORT, lazercmd_ctrl_port_w),
                new IOWritePort(S2650_DATA_PORT, S2650_DATA_PORT, lazercmd_data_port_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort lazercmd_readport[]
            = {
                new IOReadPort(S2650_CTRL_PORT, S2650_CTRL_PORT, lazercmd_ctrl_port_r),
                new IOReadPort(S2650_DATA_PORT, S2650_DATA_PORT, lazercmd_data_port_r),
                new IOReadPort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_lazercmd = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 player 1 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 player 2 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 dip switch */
            PORT_DIPNAME(0x03, 0x03, "Game Time");
            PORT_DIPSETTING(0x00, "60 seconds");
            PORT_DIPSETTING(0x01, "90 seconds");
            PORT_DIPSETTING(0x02, "120 seconds");
            PORT_DIPSETTING(0x03, "180 seconds");
            PORT_BIT(0x18, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x20, 0x20, "Video Invert");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, "Marker Size");
            PORT_DIPSETTING(0x00, "Small");
            PORT_DIPSETTING(0x40, "Large");
            PORT_DIPNAME(0x80, 0x80, "Color overlay");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN3 coinage & start */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_1C"));
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN4 player 1 + 2 buttons */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_medlanes = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 player 1 controls */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 player 1 controls */
            PORT_BITX(0x01, IP_ACTIVE_LOW, 0, "Hook Left", KEYCODE_Z, 0);
            PORT_BITX(0x02, IP_ACTIVE_LOW, 0, "Hook Right", KEYCODE_X, 0);
            PORT_BIT(0xfc, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 dip switch */
            PORT_DIPNAME(0x01, 0x01, "Game Timer");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Time");
            PORT_DIPSETTING(0x00, "3 seconds");
            PORT_DIPSETTING(0x02, "5 seconds");
            PORT_BIT(0x1C, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x20, 0x00, "Video Invert");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, "Marker Size");
            PORT_DIPSETTING(0x00, "Small");
            PORT_DIPSETTING(0x40, "Large");
            PORT_DIPNAME(0x80, 0x00, "Color overlay");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN3 coinage & start */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_1C"));
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0xf4, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_START();
            /* IN4 not used */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 10, /* 8*10 characters */
            4 * 64, /* 4 * 64 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no bitplanes */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8, 8 * 8, 9 * 8},
            10 * 8 /* every char takes 10 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 6),
                new GfxDecodeInfo(-1) /* end of array */};

    /* some colors for the frontend */
    static char palette[] = {
        /*  Red Green Blue */
        0x00, 0x00, 0x00, /* 0 black */
        0xb0, 0xb0, 0xb0, /* 1 white */
        0x20, 0xb0, 0x20, /* 2 jade green */
        0x02, 0x0b, 0x02, /* 3 very dark green */
        0xb0, 0x80, 0x20, /* 4 mustard yellow */
        0x0b, 0x08, 0x02, /* 5 very dark yellow */
        0x33, 0xff, 0x33, /* 6 bright jade */
        0xff, 0xcc, 0x33, /* 7 bright yellow */
        0xff, 0xff, 0xff, /* 8 bright white */
        0x33, 0xff, 0x33, /* 9 bright jade */
        0xff, 0xcc, 0x33, /* 10 bright yellow */
        0xff, 0xff, 0xff /* 11 bright white */};

    static final int BLACK = 0, WHITE = 1, JADE = 2, DARK_GREEN = 3, MUSTARD = 4, DARK_YELLOW = 5;

    static char colortable[] = {
        JADE, DARK_GREEN, /* 0  very dark green on jade green */
        MUSTARD, DARK_YELLOW, /* 1  very dark yellow on mustard yellow */
        WHITE, BLACK, /* 2  black on white */
        DARK_GREEN, JADE, /* 3  above inverted */
        DARK_YELLOW, MUSTARD, /* 4    "      " */
        BLACK, WHITE /* 5    "      " */};

    public static VhConvertColorPromHandlerPtr init_palette = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] game_palette, char[] game_colortable, UBytePtr color_prom) {
            memcpy(game_palette, palette, sizeof(palette));
            memcpy(game_colortable, colortable, sizeof(colortable));
        }
    };

    static DACinterface lazercmd_DAC_interface = new DACinterface(
            1,
            new int[]{255}
    );

    static MachineDriver machine_driver_lazercmd = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_S2650,
                        8064000 / 12 / 3, /* 672 kHz? */
                        /*          Main Clock is 8Mhz divided by 12
				but memory and IO access is only possible
				within the line and frame blanking period
				thus requiring an extra loading of approx 3-5 */
                        lazercmd_readmem, lazercmd_writemem, lazercmd_readport, lazercmd_writeport,
                        lazercmd_timer, 128 /* 7680 Hz */
                )
            },
            /* frames per second, vblank duration (arbitrary values!) */
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            HORZ_RES * HORZ_CHR, VERT_RES * VERT_CHR,
            new rectangle(
                    0 * HORZ_CHR, HORZ_RES * HORZ_CHR - 1,
                    0 * VERT_CHR, VERT_RES * VERT_CHR
                    - 1),
            gfxdecodeinfo,
            palette.length / 3, colortable.length,
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            generic_vh_start,
            generic_vh_stop,
            lazercmd_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        lazercmd_DAC_interface
                )
            }
    );

    static MachineDriver machine_driver_medlanes = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_S2650,
                        8064000 / 12 / 3, /* 672 kHz? */
                        /*          Main Clock is 8Mhz divided by 12
				but memory and IO access is only possible
				within the line and frame blanking period
				thus requiring an extra loading of approx 3-5 */
                        medlanes_readmem, medlanes_writemem, lazercmd_readport, lazercmd_writeport,
                        lazercmd_timer, 128 /* 7680 Hz */
                )
            },
            /* frames per second, vblank duration (arbitrary values!) */
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            HORZ_RES * HORZ_CHR, VERT_RES * VERT_CHR,
            new rectangle(
                    0 * HORZ_CHR, HORZ_RES * HORZ_CHR - 1,
                    0 * VERT_CHR, VERT_RES * VERT_CHR
                    - 1),
            gfxdecodeinfo,
            palette.length / 3, colortable.length,
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            generic_vh_start,
            generic_vh_stop,
            lazercmd_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        lazercmd_DAC_interface
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
    static RomLoadHandlerPtr rom_lazercmd = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x8000, REGION_CPU1);
            /* 32K cpu, 4K for ROM/RAM */
            ROM_LOAD("lc.e5", 0x0000, 0x0400, 0x56dc7a40);
            ROM_LOAD("lc.e6", 0x0400, 0x0400, 0xb1ef0aa2);
            ROM_LOAD("lc.e7", 0x0800, 0x0400, 0x8e6ffc97);
            ROM_LOAD("lc.f5", 0x1000, 0x0400, 0xfc5b38a4);
            ROM_LOAD("lc.f6", 0x1400, 0x0400, 0x26eaee21);
            ROM_LOAD("lc.f7", 0x1800, 0x0400, 0x9ec3534d);

            ROM_REGION(0x0c00, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("lc.b8", 0x0a00, 0x0200, 0x6d708edd);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_medlanes = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x8000, REGION_CPU1);
            /* 32K cpu, 4K for ROM/RAM */
            ROM_LOAD("medlanes.2a", 0x0000, 0x0400, 0x9c77566a);
            ROM_LOAD("medlanes.2b", 0x0400, 0x0400, 0x7841b1a9);
            ROM_LOAD("medlanes.2c", 0x0800, 0x0400, 0xa359b5b8);
            ROM_LOAD("medlanes.1a", 0x1000, 0x0400, 0x0d57c596);
            ROM_LOAD("medlanes.1b", 0x1400, 0x0400, 0x1d451630);
            ROM_LOAD("medlanes.3a", 0x4000, 0x0400, 0x22bc56a6);
            ROM_LOAD("medlanes.3b", 0x4400, 0x0400, 0x6616dbef);
            ROM_LOAD("medlanes.3c", 0x4800, 0x0400, 0xb3db0f3d);
            ROM_LOAD("medlanes.4a", 0x5000, 0x0400, 0x30d495e9);
            ROM_LOAD("medlanes.4b", 0x5400, 0x0400, 0xa4abb5db);

            ROM_REGION(0x0c00, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("medlanes.8b", 0x0a00, 0x0200, 0x44e5de8f);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_lazercmd = new InitDriverHandlerPtr() {
        public void handler() {
            int i, y;

            /**
             * ****************************************************************
             * The ROMs are 1K x 4 bit, so we have to mix them into 8 bit bytes.
             * The data is also inverted.
             * ****************************************************************
             */
            for (i = 0; i < 0x0c00; i++) {
                memory_region(REGION_CPU1).write(i + 0x0000,
                        ((memory_region(REGION_CPU1).read(i + 0x0000) << 4)
                        | (memory_region(REGION_CPU1).read(i + 0x1000) & 15)) ^ 0xff);
            }
            /**
             * ****************************************************************
             * To show the maze bit #6 and #7 of the video ram are used. Bit #7:
             * add a vertical line to the right of the character Bit #6: add a
             * horizontal line below the character The video logic generates 10
             * lines per character row, but the character generator only
             * contains 8 rows, so we expand the font to 8x10.
             * ****************************************************************
             */
            for (i = 0; i < 0x40; i++) {
                UBytePtr d = new UBytePtr(memory_region(REGION_GFX1), 0 * 64 * 10 + i * VERT_CHR);
                UBytePtr s = new UBytePtr(memory_region(REGION_GFX1), 4 * 64 * 10 + i * VERT_FNT);

                for (y = 0; y < VERT_CHR; y++) {
                    d.write(0 * 64 * 10, (y < VERT_FNT) ? s.readinc() : 0xff);
                    d.write(1 * 64 * 10, (y == VERT_CHR - 1) ? 0 : d.read());
                    d.write(2 * 64 * 10, d.read() & 0xfe);
                    d.write(3 * 64 * 10, (y == VERT_CHR - 1) ? 0 : d.read() & 0xfe);
                    d.inc();
                }
            }
        }
    };

    public static InitDriverHandlerPtr init_medlanes = new InitDriverHandlerPtr() {
        public void handler() {
            int i, y;

            /**
             * ****************************************************************
             * The ROMs are 1K x 4 bit, so we have to mix them into 8 bit bytes.
             * The data is also inverted.
             * ****************************************************************
             */
            for (i = 0; i < 0x4000; i++) {
                memory_region(REGION_CPU1).write(i + 0x0000,
                        ~((memory_region(REGION_CPU1).read(i + 0x0000) << 4)
                        | (memory_region(REGION_CPU1).read(i + 0x4000) & 15)));
            }
            /**
             * ****************************************************************
             * To show the maze bit #6 and #7 of the video ram are used. Bit #7:
             * add a vertical line to the right of the character Bit #6: add a
             * horizontal line below the character The video logic generates 10
             * lines per character row, but the character generator only
             * contains 8 rows, so we expand the font to 8x10.
             * ****************************************************************
             */
            for (i = 0; i < 0x40; i++) {
                UBytePtr d = new UBytePtr(memory_region(REGION_GFX1), 0 * 64 * 10 + i * VERT_CHR);
                UBytePtr s = new UBytePtr(memory_region(REGION_GFX1), 4 * 64 * 10 + i * VERT_FNT);

                for (y = 0; y < VERT_CHR; y++) {
                    d.write(0 * 64 * 10, (y < VERT_FNT) ? s.readinc() : 0xff);
                    d.write(1 * 64 * 10, (y == VERT_CHR - 1) ? 0 : d.read());
                    d.write(2 * 64 * 10, d.read() & 0xfe);
                    d.write(3 * 64 * 10, (y == VERT_CHR - 1) ? 0 : d.read() & 0xfe);
                    d.inc();
                }
            }
        }
    };

    public static GameDriver driver_lazercmd = new GameDriver("1976", "lazercmd", "lazercmd.java", rom_lazercmd, null, machine_driver_lazercmd, input_ports_lazercmd, init_lazercmd, ROT0, "Meadows Games, Inc.", "Lazer Command");
    public static GameDriver driver_medlanes = new GameDriver("1977", "medlanes", "lazercmd.java", rom_medlanes, null, machine_driver_medlanes, input_ports_medlanes, init_medlanes, ROT0, "Meadows Games, Inc.", "Meadows Lanes");
}
