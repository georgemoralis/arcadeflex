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
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.vidhrdw.nova2001.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.vidhrdw.pkunwar.*;

public class pkunwar {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0xa001, 0xa001, AY8910_read_port_0_r),
                new MemoryReadAddress(0xa003, 0xa003, AY8910_read_port_1_r),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x8800, 0x8bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8c00, 0x8fff, colorram_w, colorram),
                new MemoryWriteAddress(0xa000, 0xa000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0xa001, 0xa001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xa002, 0xa002, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xa003, 0xa003, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, pkunwar_flipscreen_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_pkunwar = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_SERVICE(0x40, IP_ACTIVE_LOW);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x30, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, 2048 * 16 * 8, 2048 * 16 * 8 + 4, 2 * 4, 3 * 4, 2048 * 16 * 8 + 8, 2048 * 16 * 8 + 12},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8, 8 * 8, 10 * 8, 12 * 8, 14 * 8},
            16 * 8
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, 512 * 64 * 8, 512 * 64 * 8 + 4, 2 * 4, 3 * 4, 512 * 64 * 8 + 8, 512 * 64 * 8 + 12,
                0 * 4 + 16 * 8, 1 * 4 + 16 * 8, 512 * 64 * 8 + 16 * 8, 512 * 64 * 8 + 4 + 16 * 8, 2 * 4 + 16 * 8, 3 * 4 + 16 * 8, 512 * 64 * 8 + 8 + 16 * 8, 512 * 64 * 8 + 12 + 16 * 8},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8, 8 * 8, 10 * 8, 12 * 8, 14 * 8,
                0 * 8 + 32 * 8, 2 * 8 + 32 * 8, 4 * 8 + 32 * 8, 6 * 8 + 32 * 8, 8 * 8 + 32 * 8, 10 * 8 + 32 * 8, 12 * 8 + 32 * 8, 14 * 8 + 32 * 8},
            64 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            3072000 / 2, // lax 11/03/1999  (1250000 . 1536000 ???)
            new int[]{25, 25},
            new ReadHandlerPtr[]{input_port_0_r, input_port_2_r},
            new ReadHandlerPtr[]{input_port_1_r, input_port_3_r},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_pkunwar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000,
                        readmem, writemem, null, writeport,
                        interrupt, 1
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 32 * 16,
            nova2001_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            pkunwar_vh_screenrefresh,
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
    static RomLoadPtr rom_pkunwar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("pkwar.01r", 0x0000, 0x4000, 0xce2d2c7b);
            ROM_LOAD("pkwar.02r", 0x4000, 0x4000, 0xabc1f661);
            ROM_LOAD("pkwar.03r", 0xe000, 0x2000, 0x56faebea);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("pkwar.01y", 0x0000, 0x2000, 0x428d3b92);
            ROM_CONTINUE(0x8000, 0x2000);
            ROM_LOAD("pkwar.02y", 0x2000, 0x2000, 0xce1da7bc);
            ROM_CONTINUE(0xa000, 0x2000);
            ROM_LOAD("pkwar.03y", 0x4000, 0x2000, 0x63204400);
            ROM_CONTINUE(0xc000, 0x2000);
            ROM_LOAD("pkwar.04y", 0x6000, 0x2000, 0x061dfca8);
            ROM_CONTINUE(0xe000, 0x2000);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pkwar.col", 0x0000, 0x0020, 0xaf0fc5e2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_pkunwarj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("pgunwar.6", 0x0000, 0x4000, 0x357f3ef3);
            ROM_LOAD("pgunwar.5", 0x4000, 0x4000, 0x0092e49e);
            ROM_LOAD("pkwar.03r", 0xe000, 0x2000, 0x56faebea);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("pkwar.01y", 0x0000, 0x2000, 0x428d3b92);
            ROM_CONTINUE(0x8000, 0x2000);
            ROM_LOAD("pkwar.02y", 0x2000, 0x2000, 0xce1da7bc);
            ROM_CONTINUE(0xa000, 0x2000);
            ROM_LOAD("pgunwar.2", 0x4000, 0x2000, 0xa2a43443);
            ROM_CONTINUE(0xc000, 0x2000);
            ROM_LOAD("pkwar.04y", 0x6000, 0x2000, 0x061dfca8);
            ROM_CONTINUE(0xe000, 0x2000);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pkwar.col", 0x0000, 0x0020, 0xaf0fc5e2);
            ROM_END();
        }
    };

    public static GameDriver driver_pkunwar = new GameDriver("1985", "pkunwar", "pkunwar.java", rom_pkunwar, null, machine_driver_pkunwar, input_ports_pkunwar, null, ROT0, "UPL", "Penguin-Kun Wars (US)");
    public static GameDriver driver_pkunwarj = new GameDriver("1985", "pkunwarj", "pkunwar.java", rom_pkunwarj, driver_pkunwar, machine_driver_pkunwar, input_ports_pkunwar, null, ROT0, "UPL", "Penguin-Kun Wars (Japan)");
}
