
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static vidhrdw.digdug.*;
import static mame.sndintrfH.*;
import static sound.namcoH.*;
import static sound.namco.*;
import static mame.inputportH.*;
import static machine.digdug.*;
import static mame.cpuintrf.*;

public class digdug {

    static MemoryReadAddress readmem_cpu1[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x7000, 0x700f, digdug_customio_data_r),
                new MemoryReadAddress(0x7100, 0x7100, digdug_customio_r),
                new MemoryReadAddress(0x8000, 0x9fff, digdug_sharedram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x9fff, digdug_sharedram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu3[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x9fff, digdug_sharedram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu1[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x6820, 0x6820, digdug_interrupt_enable_1_w),
                new MemoryWriteAddress(0x6821, 0x6821, digdug_interrupt_enable_2_w),
                new MemoryWriteAddress(0x6822, 0x6822, digdug_interrupt_enable_3_w),
                new MemoryWriteAddress(0x6823, 0x6823, digdug_halt_w),
                new MemoryWriteAddress(0xa007, 0xa007, digdug_flipscreen_w),
                new MemoryWriteAddress(0x6825, 0x6827, MWA_NOP),
                new MemoryWriteAddress(0x6830, 0x6830, watchdog_reset_w),
                new MemoryWriteAddress(0x7000, 0x700f, digdug_customio_data_w),
                new MemoryWriteAddress(0x7100, 0x7100, digdug_customio_w),
                new MemoryWriteAddress(0x8000, 0x9fff, digdug_sharedram_w, digdug_sharedram),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM, videoram, videoram_size), /* dirtybuffer[] handling is not needed because */
                new MemoryWriteAddress(0x8400, 0x87ff, MWA_RAM), /* characters are redrawn every frame */
                new MemoryWriteAddress(0x8b80, 0x8bff, MWA_RAM, spriteram, spriteram_size), /* these three are here just to initialize */
                new MemoryWriteAddress(0x9380, 0x93ff, MWA_RAM, spriteram_2), /* the pointers. The actual writes are */
                new MemoryWriteAddress(0x9b80, 0x9bff, MWA_RAM, spriteram_3), /* handled by digdug_sharedram_w() */
                new MemoryWriteAddress(0xa000, 0xa00f, digdug_vh_latch_w, digdug_vlatches),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x6821, 0x6821, digdug_interrupt_enable_2_w),
                new MemoryWriteAddress(0x6830, 0x6830, watchdog_reset_w),
                new MemoryWriteAddress(0x8000, 0x9fff, digdug_sharedram_w),
                new MemoryWriteAddress(0xa000, 0xa00f, digdug_vh_latch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu3[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(0x6800, 0x681f, pengo_sound_w, namco_soundregs),
                new MemoryWriteAddress(0x6822, 0x6822, digdug_interrupt_enable_3_w),
                new MemoryWriteAddress(0x8000, 0x9fff, digdug_sharedram_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /* input from the outside world */
    static InputPortPtr input_ports_digdug = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x07, 0x01, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_7C"));
            /* TODO: bonus scores are different for 5 lives */
            PORT_DIPNAME(0x38, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x20, "10k 40k 40k");
            PORT_DIPSETTING(0x10, "10k 50k 50k");
            PORT_DIPSETTING(0x30, "20k 60k 60k");
            PORT_DIPSETTING(0x08, "20k 70k 70k");
            PORT_DIPSETTING(0x28, "10k 40k");
            PORT_DIPSETTING(0x18, "20k 60k");
            PORT_DIPSETTING(0x38, "10k");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0xc0, "5");

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x20, 0x20, "Freeze");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x08, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x04, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");

            PORT_START(); 	/* FAKE */
            /* The player inputs are not memory mapped, they are handled by an I/O chip. */
            /* These fake input ports are read by digdug_customio_data_r() */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* FAKE */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* FAKE */

            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_LOW, IPT_COIN1, 1);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_LOW, IPT_COIN2, 1);
            PORT_BIT(0x0c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_START1, 1);
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_START2, 1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout1 = new GfxLayout(
            8, 8, /* 8*8 characters */
            128, /* 128 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* one bitplane */
            new int[]{7, 6, 5, 4, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout charlayout2 = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* characters are rotated 90 degrees */
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3,
                24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout1, 0, 8),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 8 * 2, 64),
                new GfxDecodeInfo(REGION_GFX3, 0, charlayout2, 64 * 4 + 8 * 2, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static namco_interface namco_interface = new namco_interface(
            3072000 / 32, /* sample rate */
            3, /* number of voices */
            100, /* playback volume */
            REGION_SOUND1 /* memory region */
    );

    static MachineDriver machine_driver_digdug = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu1, writemem_cpu1, null, null,
                        digdug_interrupt_1, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu2, writemem_cpu2, null, null,
                        digdug_interrupt_2, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu3, writemem_cpu3, null, null,
                        digdug_interrupt_3, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            100, /* 100 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            digdig_init_machine,
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 8 * 2 + 64 * 4 + 64 * 4,
            digdug_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            digdug_vh_start,
            digdug_vh_stop,
            digdug_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_NAMCO,
                        namco_interface
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
    static RomLoadPtr rom_digdug = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU  */

            ROM_LOAD("136007.101", 0x0000, 0x1000, 0xb9198079);
            ROM_LOAD("136007.102", 0x1000, 0x1000, 0xb2acbe49);
            ROM_LOAD("136007.103", 0x2000, 0x1000, 0xd6407b49);
            ROM_LOAD("dd1.4b", 0x3000, 0x1000, 0xf4cebc16);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("dd1.5b", 0x0000, 0x1000, 0x370ef9b4);
            ROM_LOAD("dd1.6b", 0x1000, 0x1000, 0x361eeb71);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU  */

            ROM_LOAD("136007.107", 0x0000, 0x1000, 0xa41bce72);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dd1.9", 0x0000, 0x0800, 0xf14a6fe1);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.116", 0x0000, 0x1000, 0xe22957c8);
            ROM_LOAD("dd1.14", 0x1000, 0x1000, 0x2829ec99);
            ROM_LOAD("136007.118", 0x2000, 0x1000, 0x458499e9);
            ROM_LOAD("136007.119", 0x3000, 0x1000, 0xc58252a0);

            ROM_REGION(0x1000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dd1.11", 0x0000, 0x1000, 0x7b383983);

            ROM_REGION(0x1000, REGION_GFX4);/* 4k for the playfield graphics */

            ROM_LOAD("dd1.10b", 0x0000, 0x1000, 0x2cf399c2);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("digdug.5n", 0x0000, 0x0020, 0x4cb9da99);
            ROM_LOAD("digdug.1c", 0x0020, 0x0100, 0x00c7c419);
            ROM_LOAD("digdug.2n", 0x0120, 0x0100, 0xe9b3e08e);

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */

            ROM_LOAD("digdug.spr", 0x0000, 0x0100, 0x7a2815b4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_digdugb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU  */

            ROM_LOAD("dd1a.1", 0x0000, 0x1000, 0xa80ec984);
            ROM_LOAD("dd1a.2", 0x1000, 0x1000, 0x559f00bd);
            ROM_LOAD("dd1a.3", 0x2000, 0x1000, 0x8cbc6fe1);
            ROM_LOAD("dd1a.4", 0x3000, 0x1000, 0xd066f830);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("dd1a.5", 0x0000, 0x1000, 0x6687933b);
            ROM_LOAD("dd1a.6", 0x1000, 0x1000, 0x843d857f);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU  */

            ROM_LOAD("136007.107", 0x0000, 0x1000, 0xa41bce72);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dd1.9", 0x0000, 0x0800, 0xf14a6fe1);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.116", 0x0000, 0x1000, 0xe22957c8);
            ROM_LOAD("dd1.14", 0x1000, 0x1000, 0x2829ec99);
            ROM_LOAD("136007.118", 0x2000, 0x1000, 0x458499e9);
            ROM_LOAD("136007.119", 0x3000, 0x1000, 0xc58252a0);

            ROM_REGION(0x1000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dd1.11", 0x0000, 0x1000, 0x7b383983);

            ROM_REGION(0x1000, REGION_GFX4);/* 4k for the playfield graphics */

            ROM_LOAD("dd1.10b", 0x0000, 0x1000, 0x2cf399c2);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("digdug.5n", 0x0000, 0x0020, 0x4cb9da99);
            ROM_LOAD("digdug.1c", 0x0020, 0x0100, 0x00c7c419);
            ROM_LOAD("digdug.2n", 0x0120, 0x0100, 0xe9b3e08e);

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */

            ROM_LOAD("digdug.spr", 0x0000, 0x0100, 0x7a2815b4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_digdugat = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU  */

            ROM_LOAD("136007.101", 0x0000, 0x1000, 0xb9198079);
            ROM_LOAD("136007.102", 0x1000, 0x1000, 0xb2acbe49);
            ROM_LOAD("136007.103", 0x2000, 0x1000, 0xd6407b49);
            ROM_LOAD("136007.104", 0x3000, 0x1000, 0xb3ad42c3);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("136007.105", 0x0000, 0x1000, 0x0a2aef4a);
            ROM_LOAD("136007.106", 0x1000, 0x1000, 0xa2876d6e);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU  */

            ROM_LOAD("136007.107", 0x0000, 0x1000, 0xa41bce72);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.108", 0x0000, 0x0800, 0x3d24a3af);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.116", 0x0000, 0x1000, 0xe22957c8);
            ROM_LOAD("136007.117", 0x1000, 0x1000, 0xa3bbfd85);
            ROM_LOAD("136007.118", 0x2000, 0x1000, 0x458499e9);
            ROM_LOAD("136007.119", 0x3000, 0x1000, 0xc58252a0);

            ROM_REGION(0x1000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.115", 0x0000, 0x1000, 0x754539be);

            ROM_REGION(0x1000, REGION_GFX4);/* 4k for the playfield graphics */

            ROM_LOAD("136007.114", 0x0000, 0x1000, 0xd6822397);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("digdug.5n", 0x0000, 0x0020, 0x4cb9da99);
            ROM_LOAD("digdug.1c", 0x0020, 0x0100, 0x00c7c419);
            ROM_LOAD("digdug.2n", 0x0120, 0x0100, 0xe9b3e08e);

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */

            ROM_LOAD("digdug.spr", 0x0000, 0x0100, 0x7a2815b4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_dzigzag = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code for the first CPU  */

            ROM_LOAD("136007.101", 0x0000, 0x1000, 0xb9198079);
            ROM_LOAD("136007.102", 0x1000, 0x1000, 0xb2acbe49);
            ROM_LOAD("136007.103", 0x2000, 0x1000, 0xd6407b49);
            ROM_LOAD("zigzag4", 0x3000, 0x1000, 0xda20d2f6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("zigzag5", 0x0000, 0x2000, 0xf803c748);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU  */

            ROM_LOAD("136007.107", 0x0000, 0x1000, 0xa41bce72);

            ROM_REGION(0x10000, REGION_CPU4);/* 64k for a Z80 which emulates the custom I/O chip (not used) */

            ROM_LOAD("zigzag7", 0x0000, 0x1000, 0x24c3510c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("zigzag8", 0x0000, 0x0800, 0x86120541);

            ROM_REGION(0x4000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136007.116", 0x0000, 0x1000, 0xe22957c8);
            ROM_LOAD("zigzag12", 0x1000, 0x1000, 0x386a0956);
            ROM_LOAD("zigzag13", 0x2000, 0x1000, 0x69f6e395);
            ROM_LOAD("136007.119", 0x3000, 0x1000, 0xc58252a0);

            ROM_REGION(0x1000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dd1.11", 0x0000, 0x1000, 0x7b383983);

            ROM_REGION(0x1000, REGION_GFX4);/* 4k for the playfield graphics */

            ROM_LOAD("dd1.10b", 0x0000, 0x1000, 0x2cf399c2);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("digdug.5n", 0x0000, 0x0020, 0x4cb9da99);
            ROM_LOAD("digdug.1c", 0x0020, 0x0100, 0x00c7c419);
            ROM_LOAD("digdug.2n", 0x0120, 0x0100, 0xe9b3e08e);

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */

            ROM_LOAD("digdug.spr", 0x0000, 0x0100, 0x7a2815b4);
            ROM_END();
        }
    };

    public static GameDriver driver_digdug = new GameDriver("1982", "digdug", "digdug.java", rom_digdug, null, machine_driver_digdug, input_ports_digdug, null, ROT90, "Namco", "Dig Dug (set 1)");
    public static GameDriver driver_digdugb = new GameDriver("1982", "digdugb", "digdug.java", rom_digdugb, driver_digdug, machine_driver_digdug, input_ports_digdug, null, ROT90, "Namco", "Dig Dug (set 2)");
    public static GameDriver driver_digdugat = new GameDriver("1982", "digdugat", "digdug.java", rom_digdugat, driver_digdug, machine_driver_digdug, input_ports_digdug, null, ROT90, "[Namco] (Atari license)", "Dig Dug (Atari)");
    public static GameDriver driver_dzigzag = new GameDriver("1982", "dzigzag", "digdug.java", rom_dzigzag, driver_digdug, machine_driver_digdug, input_ports_digdug, null, ROT90, "bootleg", "Zig Zag (Dig Dug hardware)");
}
