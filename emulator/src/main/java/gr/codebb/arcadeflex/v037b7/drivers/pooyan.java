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
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.timeplt.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.pooyan.*;

public class pooyan {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM), /* color and video RAM */
                new MemoryReadAddress(0xa000, 0xa000, input_port_4_r), /* DSW2 */
                new MemoryReadAddress(0xa080, 0xa080, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xa0a0, 0xa0a0, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xa0c0, 0xa0c0, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0xa0e0, 0xa0e0, input_port_3_r), /* DSW1 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, colorram_w, colorram),
                new MemoryWriteAddress(0x8400, 0x87ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8800, 0x8fff, MWA_RAM),
                new MemoryWriteAddress(0x9010, 0x903f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9410, 0x943f, MWA_RAM, spriteram_2),
                new MemoryWriteAddress(0xa000, 0xa000, MWA_NOP), /* watchdog reset? */
                new MemoryWriteAddress(0xa100, 0xa100, soundlatch_w),
                new MemoryWriteAddress(0xa180, 0xa180, interrupt_enable_w),
                new MemoryWriteAddress(0xa181, 0xa181, timeplt_sh_irqtrigger_w),
                new MemoryWriteAddress(0xa187, 0xa187, pooyan_flipscreen_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_pooyan = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, "Attract Mode - No Play");
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "50000 80000");
            PORT_DIPSETTING(0x00, "30000 70000");
            PORT_DIPNAME(0x70, 0x70, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x70, "Easiest");
            PORT_DIPSETTING(0x60, "Easier");
            PORT_DIPSETTING(0x50, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x30, "Medium");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            4, /* 4 bits per pixel */
            new int[]{0x1000 * 8 + 4, 0x1000 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            4, /* 4 bits per pixel */
            new int[]{0x1000 * 8 + 4, 0x1000 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static MachineDriver machine_driver_pooyan = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 Mhz (?) */
                        readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 8, /* 1.789772727 MHz */
                        timeplt_sound_readmem, timeplt_sound_writemem, null, null,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 16 * 16 + 16 * 16,
            pooyan_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            pooyan_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        timeplt_ay8910_interface
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
    static RomLoadPtr rom_pooyan = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1.4a", 0x0000, 0x2000, 0xbb319c63);
            ROM_LOAD("2.5a", 0x2000, 0x2000, 0xa1463d98);
            ROM_LOAD("3.6a", 0x4000, 0x2000, 0xfe1a9e08);
            ROM_LOAD("4.7a", 0x6000, 0x2000, 0x9e0f9bcc);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("xx.7a", 0x0000, 0x1000, 0xfbe2b368);
            ROM_LOAD("xx.8a", 0x1000, 0x1000, 0xe1795b3d);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("8.10g", 0x0000, 0x1000, 0x931b29eb);
            ROM_LOAD("7.9g", 0x1000, 0x1000, 0xbbe6d6e4);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("6.9a", 0x0000, 0x1000, 0xb2d8c121);
            ROM_LOAD("5.8a", 0x1000, 0x1000, 0x1097c2b6);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("pooyan.pr1", 0x0000, 0x0020, 0xa06a6d0e);/* palette */
            ROM_LOAD("pooyan.pr2", 0x0020, 0x0100, 0x82748c0b);/* sprites */
            ROM_LOAD("pooyan.pr3", 0x0120, 0x0100, 0x8cd4cd60);/* characters */
            ROM_END();
        }
    };

    static RomLoadPtr rom_pooyans = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic22_a4.cpu", 0x0000, 0x2000, 0x916ae7d7);
            ROM_LOAD("ic23_a5.cpu", 0x2000, 0x2000, 0x8fe38c61);
            ROM_LOAD("ic24_a6.cpu", 0x4000, 0x2000, 0x2660218a);
            ROM_LOAD("ic25_a7.cpu", 0x6000, 0x2000, 0x3d2a10ad);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("xx.7a", 0x0000, 0x1000, 0xfbe2b368);
            ROM_LOAD("xx.8a", 0x1000, 0x1000, 0xe1795b3d);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic13_g10.cpu", 0x0000, 0x1000, 0x7433aea9);
            ROM_LOAD("ic14_g9.cpu", 0x1000, 0x1000, 0x87c1789e);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("6.9a", 0x0000, 0x1000, 0xb2d8c121);
            ROM_LOAD("5.8a", 0x1000, 0x1000, 0x1097c2b6);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("pooyan.pr1", 0x0000, 0x0020, 0xa06a6d0e);/* palette */
            ROM_LOAD("pooyan.pr2", 0x0020, 0x0100, 0x82748c0b);/* sprites */
            ROM_LOAD("pooyan.pr3", 0x0120, 0x0100, 0x8cd4cd60);/* characters */
            ROM_END();
        }
    };

    static RomLoadPtr rom_pootan = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("poo_ic22.bin", 0x0000, 0x2000, 0x41b23a24);
            ROM_LOAD("poo_ic23.bin", 0x2000, 0x2000, 0xc9d94661);
            ROM_LOAD("3.6a", 0x4000, 0x2000, 0xfe1a9e08);
            ROM_LOAD("poo_ic25.bin", 0x6000, 0x2000, 0x8ae459ef);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("xx.7a", 0x0000, 0x1000, 0xfbe2b368);
            ROM_LOAD("xx.8a", 0x1000, 0x1000, 0xe1795b3d);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("poo_ic13.bin", 0x0000, 0x1000, 0x0be802e4);
            ROM_LOAD("poo_ic14.bin", 0x1000, 0x1000, 0xcba29096);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("6.9a", 0x0000, 0x1000, 0xb2d8c121);
            ROM_LOAD("5.8a", 0x1000, 0x1000, 0x1097c2b6);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("pooyan.pr1", 0x0000, 0x0020, 0xa06a6d0e);/* palette */
            ROM_LOAD("pooyan.pr2", 0x0020, 0x0100, 0x82748c0b);/* sprites */
            ROM_LOAD("pooyan.pr3", 0x0120, 0x0100, 0x8cd4cd60);/* characters */
            ROM_END();
        }
    };

    public static GameDriver driver_pooyan = new GameDriver("1982", "pooyan", "pooyan.java", rom_pooyan, null, machine_driver_pooyan, input_ports_pooyan, null, ROT270, "Konami", "Pooyan");
    public static GameDriver driver_pooyans = new GameDriver("1982", "pooyans", "pooyan.java", rom_pooyans, driver_pooyan, machine_driver_pooyan, input_ports_pooyan, null, ROT270, "[Konami] (Stern license)", "Pooyan (Stern)");
    public static GameDriver driver_pootan = new GameDriver("1982", "pootan", "pooyan.java", rom_pootan, driver_pooyan, machine_driver_pooyan, input_ports_pooyan, null, ROT270, "bootleg", "Pootan");
}
