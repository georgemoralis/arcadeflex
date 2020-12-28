/**
 * ported to 0.37b7
 * ported to 0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.superqix.*;

public class superqix {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xe000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xe000, 0xe0ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe100, 0xe7ff, MWA_RAM),
                new MemoryWriteAddress(0xe800, 0xebff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xec00, 0xefff, colorram_w, colorram),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x0000, 0x00ff, paletteram_r),
                new IOReadPort(0x0401, 0x0401, AY8910_read_port_0_r),
                new IOReadPort(0x0405, 0x0405, AY8910_read_port_1_r),
                new IOReadPort(0x0418, 0x0418, input_port_4_r),
                new IOReadPort(0x0800, 0x77ff, superqix_bitmapram_r),
                new IOReadPort(0x8800, 0xf7ff, superqix_bitmapram2_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x0000, 0x00ff, paletteram_BBGGRRII_w),
                new IOWritePort(0x0402, 0x0402, AY8910_write_port_0_w),
                new IOWritePort(0x0403, 0x0403, AY8910_control_port_0_w),
                new IOWritePort(0x0406, 0x0406, AY8910_write_port_1_w),
                new IOWritePort(0x0407, 0x0407, AY8910_control_port_1_w),
                new IOWritePort(0x0410, 0x0410, superqix_0410_w), /* ROM bank, NMI enable, tile bank */
                new IOWritePort(0x0800, 0x77ff, superqix_bitmapram_w),
                new IOWritePort(0x8800, 0xf7ff, superqix_bitmapram2_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_superqix = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_VBLANK);/* ??? */
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BITX(0x80, 0x00, IPT_DIPSWITCH_NAME, "Freeze???", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x10, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x10, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x20, 0x20, "Freeze");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "20000 50000");
            PORT_DIPSETTING(0x0c, "30000 100000");
            PORT_DIPSETTING(0x04, "50000 100000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0xc0, 0xc0, "Fill Area");
            PORT_DIPSETTING(0x80, "70%");
            PORT_DIPSETTING(0xc0, "75%");
            PORT_DIPSETTING(0x40, "80%");
            PORT_DIPSETTING(0x00, "85%");

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every sprites takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 0, 16), /* Chars */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, charlayout, 0, 16), /* Background tiles */
                new GfxDecodeInfo(REGION_GFX2, 0x08000, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x10000, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x18000, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, spritelayout, 0, 16), /* Sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static int loop = 0;
    public static InterruptPtr sqix_interrupt = new InterruptPtr() {
        public int handler() {
            loop++;

            if (loop > 2) {
                if (loop == 6) {
                    loop = 0;
                }
                return nmi_interrupt.handler();
            } else {
                return 0;
            }
        }
    };
    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 1 chip */
            1500000,
            new int[]{25, 25},
            new ReadHandlerPtr[]{input_port_0_r, input_port_3_r},
            new ReadHandlerPtr[]{input_port_1_r, input_port_2_r},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );
    static MachineDriver machine_driver_superqix = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80 | CPU_16BIT_PORT,
                        //			10000000,	/* 10 Mhz ? */
                        6000000, /* 6 Mhz ? */
                        readmem, writemem, readport, writeport,
                        //			nmi_interrupt,3	/* ??? */
                        sqix_interrupt, 6 /* ??? */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            superqix_vh_start,
            superqix_vh_stop,
            superqix_vh_screenrefresh,
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
     *************************************************************************
     */
    static RomLoadPtr rom_superqix = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sq01.97", 0x00000, 0x08000, 0x0888b7de);
            ROM_LOAD("sq02.96", 0x10000, 0x10000, 0x9c23cb64);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq04.2", 0x00000, 0x08000, 0xf815ef45);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq03.3", 0x00000, 0x10000, 0x6e8b6a67);
            ROM_LOAD("sq06.14", 0x10000, 0x10000, 0x38154517);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq05.1", 0x00000, 0x10000, 0xdf326540);

            ROM_REGION(0x1000, REGION_USER1);/* Unknown (protection related?) */
            ROM_LOAD("sq07.108", 0x00000, 0x1000, 0x071a598c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_sqixbl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("cpu.2", 0x00000, 0x08000, 0x682e28e3);
            ROM_LOAD("sq02.96", 0x10000, 0x10000, 0x9c23cb64);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq04.2", 0x00000, 0x08000, 0xf815ef45);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq03.3", 0x00000, 0x10000, 0x6e8b6a67);
            ROM_LOAD("sq06.14", 0x10000, 0x10000, 0x38154517);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sq05.1", 0x00000, 0x10000, 0xdf326540);
            ROM_END();
        }
    };

    public static GameDriver driver_superqix = new GameDriver("1987", "superqix", "superqix.java", rom_superqix, null, machine_driver_superqix, input_ports_superqix, null, ROT90, "Taito", "Super Qix", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_sqixbl = new GameDriver("1987", "sqixbl", "superqix.java", rom_sqixbl, driver_superqix, machine_driver_superqix, input_ports_superqix, null, ROT90, "bootleg", "Super Qix (bootleg)", GAME_NO_COCKTAIL);
}
