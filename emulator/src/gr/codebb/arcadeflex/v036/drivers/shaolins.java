/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.shaolins.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496H.*;
import static gr.codebb.arcadeflex.v036.sound.sn76496.*;


public class shaolins {

    public static UBytePtr shaolins_nmi_enable = new UBytePtr();

    public static InterruptPtr shaolins_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                return interrupt.handler();
            } else if ((cpu_getiloops() % 2) != 0) {
                if ((shaolins_nmi_enable.read() & 0x02) != 0) {
                    return nmi_interrupt.handler();
                }
            }

            return ignore_interrupt.handler();
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0500, 0x0500, input_port_3_r), /* Dipswitch settings */
                new MemoryReadAddress(0x0600, 0x0600, input_port_4_r), /* Dipswitch settings */
                new MemoryReadAddress(0x0700, 0x0700, input_port_0_r), /* coins + service */
                new MemoryReadAddress(0x0701, 0x0701, input_port_1_r), /* player 1 controls */
                new MemoryReadAddress(0x0702, 0x0702, input_port_2_r), /* player 2 controls */
                new MemoryReadAddress(0x0703, 0x0703, input_port_5_r), /* selftest */
                new MemoryReadAddress(0x2800, 0x2bff, MRA_RAM), /* RAM BANK 2 */
                new MemoryReadAddress(0x3000, 0x33ff, MRA_RAM), /* RAM BANK 1 */
                new MemoryReadAddress(0x3800, 0x3fff, MRA_RAM), /* video RAM */
                new MemoryReadAddress(0x4000, 0x5fff, MRA_ROM), /* Machine checks for extra rom */
                new MemoryReadAddress(0x6000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0000, MWA_RAM, shaolins_nmi_enable), /* bit 1 = nmi enable, bit 2 = ? */
                /* bit 3, bit 4 = coin counters */
                new MemoryWriteAddress(0x0100, 0x0100, watchdog_reset_w),
                new MemoryWriteAddress(0x0300, 0x0300, SN76496_0_w), /* trigger chip to read from latch. The program always */
                new MemoryWriteAddress(0x0400, 0x0400, SN76496_1_w), /* writes the same number as the latch, so we don't */
                /* bother emulating them. */
                new MemoryWriteAddress(0x0800, 0x0800, MWA_NOP), /* latch for 76496 #0 */
                new MemoryWriteAddress(0x1000, 0x1000, MWA_NOP), /* latch for 76496 #1 */
                new MemoryWriteAddress(0x1800, 0x1800, shaolins_palettebank_w),
                new MemoryWriteAddress(0x2000, 0x2000, MWA_RAM, shaolins_scroll),
                new MemoryWriteAddress(0x2800, 0x2bff, MWA_RAM), /* RAM BANK 2 */
                new MemoryWriteAddress(0x3000, 0x30ff, MWA_RAM), /* RAM BANK 1 */
                new MemoryWriteAddress(0x3100, 0x33ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3800, 0x3bff, colorram_w, colorram),
                new MemoryWriteAddress(0x3c00, 0x3fff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x6000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_shaolins = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30000 70000");
            PORT_DIPSETTING(0x10, "40000 80000");
            PORT_DIPSETTING(0x08, "40000");
            PORT_DIPSETTING(0x00, "50000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Unknown DSW2 2");
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, "Unknown DSW2 4");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Unknown DSW2 5");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "Unknown DSW2 6");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, "Unknown DSW2 7");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Unknown DSW2 8");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW2 */

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
            INPUT_PORTS_END();
        }
    };

    static GfxLayout shaolins_charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            512, /* 512 characters */
            4, /* 4 bits per pixel */
            new int[]{512 * 16 * 8 + 4, 512 * 16 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout shaolins_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{256 * 64 * 8 + 4, 256 * 64 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo shaolins_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, shaolins_charlayout, 0, 16 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, shaolins_spritelayout, 16 * 8 * 16, 16 * 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static SN76496interface sn76496_interface = new SN76496interface(
            2, /* 2 chips */
            new int[]{1536000, 3072000}, /* 3.072 Mhz???? */
            new int[]{100, 100}
    );

    static MachineDriver machine_driver_shaolins = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1250000, /* 1.25 Mhz */
                        readmem, writemem, null, null,
                        shaolins_interrupt, 16 /* 1 IRQ + 8 NMI */
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            shaolins_gfxdecodeinfo,
            256, 16 * 8 * 16 + 16 * 8 * 16,
            shaolins_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            shaolins_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
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
    static RomLoadPtr rom_kicker = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for code */

            ROM_LOAD("kikrd8.bin", 0x6000, 0x2000, 0x2598dfdd);
            ROM_LOAD("kikrd9.bin", 0x8000, 0x4000, 0x0cf0351a);
            ROM_LOAD("kikrd11.bin", 0xC000, 0x4000, 0x654037f8);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("kikra10.bin", 0x0000, 0x2000, 0x4d156afc);
            ROM_LOAD("kikra11.bin", 0x2000, 0x2000, 0xff6ca5df);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("kikrh14.bin", 0x0000, 0x4000, 0xb94e645b);
            ROM_LOAD("kikrh13.bin", 0x4000, 0x4000, 0x61bbf797);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("kicker.a12", 0x0000, 0x0100, 0xb09db4b4);/* palette red component */

            ROM_LOAD("kicker.a13", 0x0100, 0x0100, 0x270a2bf3);/* palette green component */

            ROM_LOAD("kicker.a14", 0x0200, 0x0100, 0x83e95ea8);/* palette blue component */

            ROM_LOAD("kicker.b8", 0x0300, 0x0100, 0xaa900724);/* character lookup table */

            ROM_LOAD("kicker.f16", 0x0400, 0x0100, 0x80009cf5);/* sprite lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_shaolins = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for code */

            ROM_LOAD("kikrd8.bin", 0x6000, 0x2000, 0x2598dfdd);
            ROM_LOAD("kikrd9.bin", 0x8000, 0x4000, 0x0cf0351a);
            ROM_LOAD("kikrd11.bin", 0xC000, 0x4000, 0x654037f8);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("shaolins.6", 0x0000, 0x2000, 0xff18a7ed);
            ROM_LOAD("shaolins.7", 0x2000, 0x2000, 0x5f53ae61);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("kikrh14.bin", 0x0000, 0x4000, 0xb94e645b);
            ROM_LOAD("kikrh13.bin", 0x4000, 0x4000, 0x61bbf797);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("kicker.a12", 0x0000, 0x0100, 0xb09db4b4);/* palette red component */

            ROM_LOAD("kicker.a13", 0x0100, 0x0100, 0x270a2bf3);/* palette green component */

            ROM_LOAD("kicker.a14", 0x0200, 0x0100, 0x83e95ea8);/* palette blue component */

            ROM_LOAD("kicker.b8", 0x0300, 0x0100, 0xaa900724);/* character lookup table */

            ROM_LOAD("kicker.f16", 0x0400, 0x0100, 0x80009cf5);/* sprite lookup table */

            ROM_END();
        }
    };

    public static GameDriver driver_kicker = new GameDriver("1985", "kicker", "shaolins.java", rom_kicker, null, machine_driver_shaolins, input_ports_shaolins, null, ROT90, "Konami", "Kicker");
    public static GameDriver driver_shaolins = new GameDriver("1985", "shaolins", "shaolins.java", rom_shaolins, driver_kicker, machine_driver_shaolins, input_ports_shaolins, null, ROT90, "Konami", "Shao-Lin's Road");
}
