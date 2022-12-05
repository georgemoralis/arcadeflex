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
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.blueprnt.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;

public class blueprnt {

    static int dipsw;

    public static WriteHandlerPtr dipsw_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            dipsw = data;
        }
    };

    public static ReadHandlerPtr blueprnt_sh_dipsw_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return dipsw;
        }
    };

    public static WriteHandlerPtr blueprnt_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };
    static int lastval;
    public static WriteHandlerPtr blueprnt_coin_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (lastval == data) {
                return;
            }
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);
            lastval = data;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM),
                new MemoryReadAddress(0x9400, 0x97ff, videoram_r), /* mirror address, I THINK */
                new MemoryReadAddress(0xa000, 0xa01f, MRA_RAM),
                new MemoryReadAddress(0xb000, 0xb0ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc000, input_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_1_r),
                new MemoryReadAddress(0xc003, 0xc003, blueprnt_sh_dipsw_r),
                new MemoryReadAddress(0xe000, 0xe000, watchdog_reset_r),
                new MemoryReadAddress(0xf000, 0xf3ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xa000, 0xa01f, MWA_RAM, blueprnt_scrollram),
                new MemoryWriteAddress(0xb000, 0xb0ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xc000, 0xc000, blueprnt_coin_w),
                new MemoryWriteAddress(0xd000, 0xd000, blueprnt_sound_command_w),
                new MemoryWriteAddress(0xe000, 0xe000, blueprnt_flipscreen_w), /* + gfx bank */
                new MemoryWriteAddress(0xf000, 0xf3ff, colorram_w, colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x2fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(0x6002, 0x6002, AY8910_read_port_0_r),
                new MemoryReadAddress(0x8002, 0x8002, AY8910_read_port_1_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x2fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(0x6000, 0x6000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x6001, 0x6001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x8000, 0x8000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0x8001, 0x8001, AY8910_write_port_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_blueprnt = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000");
            PORT_DIPSETTING(0x02, "30000");
            PORT_DIPSETTING(0x04, "40000");
            PORT_DIPSETTING(0x06, "50000");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, "Maze Monster");
            PORT_DIPSETTING(0x00, "2nd Maze");
            PORT_DIPSETTING(0x10, "3rd Maze");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x10, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x30, "Hardest");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_saturn = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0x80, "5");
            PORT_DIPSETTING(0xc0, "6");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2/1 B 1/3");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/6");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{512 * 8 * 8, 0}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            8, 16, /* 8*16 sprites */
            256, /* 256 sprites */
            3, /* 3 bits per pixel */
            new int[]{2 * 128 * 16 * 16, 128 * 16 * 16, 0}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 8 /* every sprite takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 128),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 128 * 4, 1),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            10000000 / 8, /* 1.25 MHz (4H) */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, input_port_2_r},
            new ReadHandlerPtr[]{soundlatch_r, input_port_3_r},
            new WriteHandlerPtr[]{dipsw_w, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_blueprnt = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        10000000 / 4, /* 2.5 MHz (2H) */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80, /* can't use CPU_AUDIO_CPU because this CPU reads the dip switches */
                        10000000 / 4, /* 2.5 MHz (2H) */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4 /* IRQs connected to 32V */
                /* NMIs are caused by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            16, 128 * 4 + 8,
            blueprnt_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            blueprnt_vh_screenrefresh,
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
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_blueprnt = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1m", 0x0000, 0x1000, 0xb20069a6);
            ROM_LOAD("1n", 0x1000, 0x1000, 0x4a30302e);
            ROM_LOAD("1p", 0x2000, 0x1000, 0x6866ca07);
            ROM_LOAD("1r", 0x3000, 0x1000, 0x5d3cfac3);
            ROM_LOAD("1s", 0x4000, 0x1000, 0xa556cac4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("3u", 0x0000, 0x1000, 0xfd38777a);
            ROM_LOAD("3v", 0x2000, 0x1000, 0x33d5bf5b);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("c3", 0x0000, 0x1000, 0xac2a61bc);
            ROM_LOAD("d3", 0x1000, 0x1000, 0x81fe85d7);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("d17", 0x0000, 0x1000, 0xa73b6483);
            ROM_LOAD("d18", 0x1000, 0x1000, 0x7d622550);
            ROM_LOAD("d20", 0x2000, 0x1000, 0x2fcb4f26);
            ROM_END();
        }
    };

    static RomLoadPtr rom_blueprnj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("bp_01.bin", 0x0000, 0x1000, 0x2e746693);
            ROM_LOAD("bp_02.bin", 0x1000, 0x1000, 0xa0eb0b8e);
            ROM_LOAD("bp_03.bin", 0x2000, 0x1000, 0xc34981bb);
            ROM_LOAD("bp_04.bin", 0x3000, 0x1000, 0x525e77b5);
            ROM_LOAD("bp_05.bin", 0x4000, 0x1000, 0x431a015f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("3u", 0x0000, 0x1000, 0xfd38777a);
            ROM_LOAD("3v", 0x2000, 0x1000, 0x33d5bf5b);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bp_09.bin", 0x0000, 0x0800, 0x43718c34);
            ROM_LOAD("bp_08.bin", 0x1000, 0x0800, 0xd3ce077d);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bp_10.bin", 0x0000, 0x1000, 0x83da108f);
            ROM_LOAD("bp_11.bin", 0x1000, 0x1000, 0xb440f32f);
            ROM_LOAD("bp_12.bin", 0x2000, 0x1000, 0x23026765);
            ROM_END();
        }
    };

    static RomLoadPtr rom_saturn = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("r1", 0x0000, 0x1000, 0x18a6d68e);
            ROM_LOAD("r2", 0x1000, 0x1000, 0xa7dd2665);
            ROM_LOAD("r3", 0x2000, 0x1000, 0xb9cfa791);
            ROM_LOAD("r4", 0x3000, 0x1000, 0xc5a997e7);
            ROM_LOAD("r5", 0x4000, 0x1000, 0x43444d00);
            ROM_LOAD("r6", 0x5000, 0x1000, 0x4d4821f6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("r7", 0x0000, 0x1000, 0xdd43e02f);
            ROM_LOAD("r8", 0x2000, 0x1000, 0x7f9d0877);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("r10", 0x0000, 0x1000, 0x35987d61);
            ROM_LOAD("r9", 0x1000, 0x1000, 0xca6a7fda);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("r11", 0x0000, 0x1000, 0x6e4e6e5d);
            ROM_LOAD("r12", 0x1000, 0x1000, 0x46fc049e);
            ROM_LOAD("r13", 0x2000, 0x1000, 0x8b3e8c32);
            ROM_END();
        }
    };

    public static GameDriver driver_blueprnt = new GameDriver("1982", "blueprnt", "blueprnt.java", rom_blueprnt, null, machine_driver_blueprnt, input_ports_blueprnt, null, ROT270, "[Zilec] Bally Midway", "Blue Print (Midway)");
    public static GameDriver driver_blueprnj = new GameDriver("1982", "blueprnj", "blueprnt.java", rom_blueprnj, driver_blueprnt, machine_driver_blueprnt, input_ports_blueprnt, null, ROT270, "[Zilec] Jaleco", "Blue Print (Jaleco)");
    public static GameDriver driver_saturn = new GameDriver("1983", "saturn", "blueprnt.java", rom_saturn, null, machine_driver_blueprnt, input_ports_saturn, null, ROT270, "[Zilec] Jaleco", "Saturn");
}
