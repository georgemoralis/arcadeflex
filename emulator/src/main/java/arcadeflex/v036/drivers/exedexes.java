/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.drivers;

//drivers imports
import static arcadeflex.v036.drivers._1942.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.exedexes.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496H.*;

public class exedexes {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc000, input_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_1_r),
                new MemoryReadAddress(0xc002, 0xc002, input_port_2_r),
                new MemoryReadAddress(0xc003, 0xc003, input_port_3_r),
                new MemoryReadAddress(0xc004, 0xc004, input_port_4_r),
                new MemoryReadAddress(0xe000, 0xefff, MRA_RAM), /* Work RAM */
                new MemoryReadAddress(0xf000, 0xffff, MRA_RAM), /* Sprite RAM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc800, 0xc800, soundlatch_w),
                new MemoryWriteAddress(0xc806, 0xc806, MWA_NOP), /* Watchdog ?? */
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xd800, 0xd801, MWA_RAM, exedexes_nbg_yscroll),
                new MemoryWriteAddress(0xd802, 0xd803, MWA_RAM, exedexes_nbg_xscroll),
                new MemoryWriteAddress(0xd804, 0xd805, MWA_RAM, exedexes_bg_scroll),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_RAM),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x8000, 0x8000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x8001, 0x8001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x8002, 0x8002, SN76496_0_w),
                new MemoryWriteAddress(0x8003, 0x8003, SN76496_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_exedexes = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 8);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "1");
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x10, 0x10, "2 Players Game");
            PORT_DIPSETTING(0x00, "1 Credit");
            PORT_DIPSETTING(0x10, "2 Credits");
            PORT_DIPNAME(0x20, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x20, "Japanese");
            PORT_DIPNAME(0x40, 0x40, "Freeze");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x40, 0x40, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x40, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{0x4000 * 8 + 4, 0x4000 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 33 * 8 + 0, 33 * 8 + 1, 33 * 8 + 2, 33 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );
    static GfxLayout tilelayout = new GfxLayout(
            32, 32, /* 32*32 tiles */
            64, /* 64 tiles */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                64 * 8 + 0, 64 * 8 + 1, 64 * 8 + 2, 64 * 8 + 3, 65 * 8 + 0, 65 * 8 + 1, 65 * 8 + 2, 65 * 8 + 3,
                128 * 8 + 0, 128 * 8 + 1, 128 * 8 + 2, 128 * 8 + 3, 129 * 8 + 0, 129 * 8 + 1, 129 * 8 + 2, 129 * 8 + 3,
                192 * 8 + 0, 192 * 8 + 1, 192 * 8 + 2, 192 * 8 + 3, 193 * 8 + 0, 193 * 8 + 1, 193 * 8 + 2, 193 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16,
                24 * 16, 25 * 16, 26 * 16, 27 * 16, 28 * 16, 29 * 16, 30 * 16, 31 * 16},
            256 * 8 /* every tile takes 256 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 64 * 4, 64), /* 32x32 Tiles */
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 2 * 64 * 4, 16), /* 16x16 Tiles */
                new GfxDecodeInfo(REGION_GFX4, 0, spritelayout, 2 * 64 * 4 + 16 * 16, 16), /* Sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{15, 15},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static SN76496interface sn76496_interface = new SN76496interface(
            2, /* 2 chips */
            new int[]{3000000, 3000000}, /* 3 MHz????? */
            new int[]{30, 30}
    );

    static MachineDriver machine_driver_exedexes = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz (?) */
                        readmem, writemem, null, null,
                        c1942_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3 Mhz ??? */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 64 * 4 + 64 * 4 + 16 * 16 + 16 * 16,
            exedexes_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            exedexes_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
                )
            }
    );

    static RomLoadPtr rom_exedexes = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("11m_ee04.bin", 0x0000, 0x4000, 0x44140dbd);
            ROM_LOAD("10m_ee03.bin", 0x4000, 0x4000, 0xbf72cfba);
            ROM_LOAD("09m_ee02.bin", 0x8000, 0x4000, 0x7ad95e2f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("11e_ee01.bin", 0x00000, 0x4000, 0x73cdf3b2);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("05c_ee00.bin", 0x00000, 0x2000, 0xcadb75bd);/* Characters */

            ROM_REGION(0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("h01_ee08.bin", 0x00000, 0x4000, 0x96a65c1d);/* 32x32 tiles planes 0-1 */

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a03_ee06.bin", 0x00000, 0x4000, 0x6039bdd1);/* 16x16 tiles planes 0-1 */
            ROM_LOAD("a02_ee05.bin", 0x04000, 0x4000, 0xb32d8252);/* 16x16 tiles planes 2-3 */

            ROM_REGION(0x08000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j11_ee10.bin", 0x00000, 0x4000, 0xbc83e265);/* Sprites planes 0-1 */
            ROM_LOAD("j12_ee11.bin", 0x04000, 0x4000, 0x0e0f300d);/* Sprites planes 2-3 */

            ROM_REGION(0x6000, REGION_GFX5);/* background tilemaps */
            ROM_LOAD("c01_ee07.bin", 0x0000, 0x4000, 0x3625a68d);/* Front Tile Map */
            ROM_LOAD("h04_ee09.bin", 0x4000, 0x2000, 0x6057c907);/* Back Tile map */

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("02d_e-02.bin", 0x0000, 0x0100, 0x8d0d5935);/* red component */
            ROM_LOAD("03d_e-03.bin", 0x0100, 0x0100, 0xd3c17efc);/* green component */
            ROM_LOAD("04d_e-04.bin", 0x0200, 0x0100, 0x58ba964c);/* blue component */
            ROM_LOAD("06f_e-05.bin", 0x0300, 0x0100, 0x35a03579);/* char lookup table */
            ROM_LOAD("l04_e-10.bin", 0x0400, 0x0100, 0x1dfad87a);/* 32x32 tile lookup table */
            ROM_LOAD("c04_e-07.bin", 0x0500, 0x0100, 0x850064e0);/* 16x16 tile lookup table */
            ROM_LOAD("l09_e-11.bin", 0x0600, 0x0100, 0x2bb68710);/* sprite lookup table */
            ROM_LOAD("l10_e-12.bin", 0x0700, 0x0100, 0x173184ef);/* sprite palette bank */
            ROM_END();
        }
    };

    static RomLoadPtr rom_savgbees = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("ee04e.11m", 0x0000, 0x4000, 0xc0caf442);
            ROM_LOAD("ee03e.10m", 0x4000, 0x4000, 0x9cd70ae1);
            ROM_LOAD("ee02e.9m", 0x8000, 0x4000, 0xa04e6368);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("ee01e.11e", 0x00000, 0x4000, 0x93d3f952);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ee00e.5c", 0x00000, 0x2000, 0x5972f95f);/* Characters */

            ROM_REGION(0x04000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("h01_ee08.bin", 0x00000, 0x4000, 0x96a65c1d);/* 32x32 tiles planes 0-1 */

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a03_ee06.bin", 0x00000, 0x4000, 0x6039bdd1);/* 16x16 tiles planes 0-1 */
            ROM_LOAD("a02_ee05.bin", 0x04000, 0x4000, 0xb32d8252);/* 16x16 tiles planes 2-3 */

            ROM_REGION(0x08000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j11_ee10.bin", 0x00000, 0x4000, 0xbc83e265);/* Sprites planes 0-1 */
            ROM_LOAD("j12_ee11.bin", 0x04000, 0x4000, 0x0e0f300d);/* Sprites planes 2-3 */

            ROM_REGION(0x6000, REGION_GFX5);/* background tilemaps */
            ROM_LOAD("c01_ee07.bin", 0x0000, 0x4000, 0x3625a68d);/* Front Tile Map */
            ROM_LOAD("h04_ee09.bin", 0x4000, 0x2000, 0x6057c907);/* Back Tile map */

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("02d_e-02.bin", 0x0000, 0x0100, 0x8d0d5935);/* red component */
            ROM_LOAD("03d_e-03.bin", 0x0100, 0x0100, 0xd3c17efc);/* green component */
            ROM_LOAD("04d_e-04.bin", 0x0200, 0x0100, 0x58ba964c);/* blue component */
            ROM_LOAD("06f_e-05.bin", 0x0300, 0x0100, 0x35a03579);/* char lookup table */
            ROM_LOAD("l04_e-10.bin", 0x0400, 0x0100, 0x1dfad87a);/* 32x32 tile lookup table */
            ROM_LOAD("c04_e-07.bin", 0x0500, 0x0100, 0x850064e0);/* 16x16 tile lookup table */
            ROM_LOAD("l09_e-11.bin", 0x0600, 0x0100, 0x2bb68710);/* sprite lookup table */
            ROM_LOAD("l10_e-12.bin", 0x0700, 0x0100, 0x173184ef);/* sprite palette bank */
            ROM_END();
        }
    };

    public static GameDriver driver_exedexes = new GameDriver("1985", "exedexes", "exedexes.java", rom_exedexes, null, machine_driver_exedexes, input_ports_exedexes, null, ROT270, "Capcom", "Exed Exes", GAME_NO_COCKTAIL);
    public static GameDriver driver_savgbees = new GameDriver("1985", "savgbees", "exedexes.java", rom_savgbees, driver_exedexes, machine_driver_exedexes, input_ports_exedexes, null, ROT270, "Capcom (Memetron license)", "Savage Bees", GAME_NO_COCKTAIL);
}
