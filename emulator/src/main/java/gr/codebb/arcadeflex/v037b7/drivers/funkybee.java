/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.funkybee.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;

public class funkybee {

    public static ReadHandlerPtr funkybee_input_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            watchdog_reset_r.handler(0);
            return input_port_0_r.handler(offset);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x4fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf000, MRA_NOP), /* IRQ Ack */
                new MemoryReadAddress(0xf800, 0xf800, funkybee_input_port_0_r),
                new MemoryReadAddress(0xf801, 0xf801, input_port_1_r),
                new MemoryReadAddress(0xf802, 0xf802, input_port_2_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x4fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xbfff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xc000, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe000, MWA_RAM, funkyb_row_scroll),
                new MemoryWriteAddress(0xe802, 0xe803, coin_counter_w),
                new MemoryWriteAddress(0xe805, 0xe805, funkybee_gfx_bank_w),
                new MemoryWriteAddress(0xf800, 0xf800, watchdog_reset_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x02, 0x02, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_funkybee = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, "Freeze");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 		/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();       /* DSW */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_skylancr = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, "Freeze");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 		/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xe0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();       /* DSW */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x40, "20000 50000");
            PORT_DIPSETTING(0x00, "40000 70000");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            8, 32, /* 8*32 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8,
                48 * 8, 49 * 8, 50 * 8, 51 * 8, 52 * 8, 53 * 8, 54 * 8, 55 * 8},
            4 * 16 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX2, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 16, 4),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16, 4),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{50},
            new ReadHandlerPtr[]{input_port_3_r},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_funkybee = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 Mhz */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU game */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(12, 32 * 8 - 1 - 12, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 32,
            funkybee_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            funkybee_vh_screenrefresh,
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
    static RomLoadHandlerPtr rom_funkybee = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("funkybee.1", 0x0000, 0x1000, 0x3372cb33);
            ROM_LOAD("funkybee.3", 0x1000, 0x1000, 0x7bf7c62f);
            ROM_LOAD("funkybee.2", 0x2000, 0x1000, 0x8cc0fe8e);
            ROM_LOAD("funkybee.4", 0x3000, 0x1000, 0x1e1aac26);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("funkybee.5", 0x0000, 0x2000, 0x86126655);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("funkybee.6", 0x0000, 0x2000, 0x5fffd323);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("funkybee.clr", 0x0000, 0x0020, 0xe2cf5fe2);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_skylancr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("1.5a", 0x0000, 0x2000, 0x82d55824);
            ROM_LOAD("2.5c", 0x2000, 0x2000, 0xdff3a682);
            ROM_LOAD("3.5d", 0x4000, 0x1000, 0x7c006ee6);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4.6a", 0x0000, 0x2000, 0x0f8ede07);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5.6b", 0x0000, 0x2000, 0x24cec070);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("18s030.1a", 0x0000, 0x0020, 0xe645bacb);
            ROM_END();
        }
    };

    public static GameDriver driver_funkybee = new GameDriver("1982", "funkybee", "funkybee.java", rom_funkybee, null, machine_driver_funkybee, input_ports_funkybee, null, ROT90, "Orca Corporation", "Funky Bee", GAME_NO_COCKTAIL);
    public static GameDriver driver_skylancr = new GameDriver("1983", "skylancr", "funkybee.java", rom_skylancr, null, machine_driver_funkybee, input_ports_skylancr, null, ROT90, "Orca (Esco Trading Co license)", "Sky Lancer", GAME_NO_COCKTAIL);
}
