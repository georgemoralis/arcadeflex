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
import static gr.codebb.arcadeflex.v037b7.vidhrdw.nova2001.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static arcadeflex.v036.mame.sndintrfH.*;

public class nova2001 {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xa000, 0xb7ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc000, AY8910_read_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, AY8910_read_port_1_r),
                new MemoryReadAddress(0xc004, 0xc004, watchdog_reset_r),
                new MemoryReadAddress(0xc006, 0xc006, input_port_0_r),
                new MemoryReadAddress(0xc007, 0xc007, input_port_1_r),
                new MemoryReadAddress(0xc00e, 0xc00e, input_port_2_r),
                new MemoryReadAddress(0xe000, 0xe7ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xa000, 0xa3ff, MWA_RAM, nova2001_videoram, nova2001_videoram_size),
                new MemoryWriteAddress(0xa400, 0xa7ff, MWA_RAM, nova2001_colorram),
                new MemoryWriteAddress(0xa800, 0xabff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xac00, 0xafff, colorram_w, colorram),
                new MemoryWriteAddress(0xb000, 0xb7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xbfff, 0xbfff, nova2001_flipscreen_w),
                new MemoryWriteAddress(0xc000, 0xc000, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xc001, 0xc001, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xc002, 0xc002, AY8910_control_port_0_w),
                new MemoryWriteAddress(0xc003, 0xc003, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_nova2001 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* player 2 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START();
            /* dsw0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPNAME(0x04, 0x04, "1st Bonus Life");
            PORT_DIPSETTING(0x04, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPNAME(0x18, 0x18, "Extra Bonus Life");
            PORT_DIPSETTING(0x18, "60000");
            PORT_DIPSETTING(0x10, "70000");
            PORT_DIPSETTING(0x08, "90000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* dsw1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x03, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x01, "Hardest");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, "High Score Names");
            PORT_DIPSETTING(0x00, "3 Letters");
            PORT_DIPSETTING(0x08, "8 Letters");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 8192 * 8 + 0, 8192 * 8 + 4, 8, 12, 8192 * 8 + 8, 8192 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7},
            8 * 16
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 characters */
            64, /* 64 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 8192 * 8 + 0, 8192 * 8 + 4, 8, 12, 8192 * 8 + 8, 8192 * 8 + 12,
                16 * 8 + 0, 16 * 8 + 4, 16 * 8 + 8192 * 8 + 0, 16 * 8 + 8192 * 8 + 4, 16 * 8 + 8, 16 * 8 + 12, 16 * 8 + 8192 * 8 + 8, 16 * 8 + 8192 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7,
                32 * 8 + 16 * 0, 32 * 8 + 16 * 1, 32 * 8 + 16 * 2, 32 * 8 + 16 * 3, 32 * 8 + 16 * 4, 32 * 8 + 16 * 5, 32 * 8 + 16 * 6, 32 * 8 + 16 * 7},
            8 * 64
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, charlayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX1, 0x1000, spritelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x1000, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            6000000 / 3, /* 2 MHz */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, input_port_3_r},
            new ReadHandlerPtr[]{null, input_port_4_r},
            new WriteHandlerPtr[]{nova2001_scroll_x_w, null}, /* writes are connected to pf scroll */
            new WriteHandlerPtr[]{nova2001_scroll_y_w, null}
    );

    static MachineDriver machine_driver_nova2001 = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3000000, /* 3 MHz */
                        readmem, writemem, null, null,
                        interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 32 * 16,
            nova2001_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            nova2001_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static RomLoadPtr rom_nova2001 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("1.6c", 0x0000, 0x2000, 0x368cffc0);
            ROM_LOAD("2.6d", 0x2000, 0x2000, 0xbc4e442b);
            ROM_LOAD("3.6f", 0x4000, 0x2000, 0xb2849038);
            ROM_LOAD("4.6g", 0x6000, 0x1000, 0x6b5bb12d);
            ROM_RELOAD(0x7000, 0x1000);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5.12s", 0x0000, 0x2000, 0x54198941);
            ROM_LOAD("6.12p", 0x2000, 0x2000, 0xcbd90dca);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("7.12n", 0x0000, 0x2000, 0x9ebd8806);
            ROM_LOAD("8.12l", 0x2000, 0x2000, 0xd1b18389);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("nova2001.clr", 0x0000, 0x0020, 0xa2fac5cd);
            ROM_END();
        }
    };

    static RomLoadPtr rom_nov2001u = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("nova2001.1", 0x0000, 0x2000, 0xb79461bd);
            ROM_LOAD("nova2001.2", 0x2000, 0x2000, 0xfab87144);
            ROM_LOAD("3.6f", 0x4000, 0x2000, 0xb2849038);
            ROM_LOAD("4.6g", 0x6000, 0x1000, 0x6b5bb12d);
            ROM_RELOAD(0x7000, 0x1000);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nova2001.5", 0x0000, 0x2000, 0x8ea576e8);
            ROM_LOAD("nova2001.6", 0x2000, 0x2000, 0x0c61656c);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("7.12n", 0x0000, 0x2000, 0x9ebd8806);
            ROM_LOAD("8.12l", 0x2000, 0x2000, 0xd1b18389);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("nova2001.clr", 0x0000, 0x0020, 0xa2fac5cd);
            ROM_END();
        }
    };

    public static GameDriver driver_nova2001 = new GameDriver("1983", "nova2001", "nova2001.java", rom_nova2001, null, machine_driver_nova2001, input_ports_nova2001, null, ROT0, "UPL", "Nova 2001 (Japan)");
    public static GameDriver driver_nov2001u = new GameDriver("1983", "nov2001u", "nova2001.java", rom_nov2001u, driver_nova2001, machine_driver_nova2001, input_ports_nova2001, null, ROT0, "UPL (Universal license)", "Nova 2001 (US)");
}
