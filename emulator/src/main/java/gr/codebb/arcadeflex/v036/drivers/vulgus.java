/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.drivers._1942.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vulgus.*;

public class vulgus {

    public static WriteHandlerPtr vulgus_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0-1 coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bit 7   flip screen
	
             in vulgus this is active LO, in vulgusj this is active HI !!! */
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x9fff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc000, input_port_0_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_1_r),
                new MemoryReadAddress(0xc002, 0xc002, input_port_2_r),
                new MemoryReadAddress(0xc003, 0xc003, input_port_3_r),
                new MemoryReadAddress(0xc004, 0xc004, input_port_4_r),
                new MemoryReadAddress(0xd000, 0xefff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x9fff, MWA_ROM),
                new MemoryWriteAddress(0xc800, 0xc800, soundlatch_w),
                new MemoryWriteAddress(0xc802, 0xc803, MWA_RAM, vulgus_scrolllow),
                new MemoryWriteAddress(0xc804, 0xc804, vulgus_control_w),
                new MemoryWriteAddress(0xc805, 0xc805, vulgus_palette_bank_w, vulgus_palette_bank),
                new MemoryWriteAddress(0xc902, 0xc903, MWA_RAM, vulgus_scrollhigh),
                new MemoryWriteAddress(0xcc00, 0xcc7f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xd800, 0xdbff, vulgus_bgvideoram_w, vulgus_bgvideoram, vulgus_bgvideoram_size),
                new MemoryWriteAddress(0xdc00, 0xdfff, vulgus_bgcolorram_w, vulgus_bgcolorram),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x8000, 0x8000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x8001, 0x8001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xc000, 0xc000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xc001, 0xc001, AY8910_write_port_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_vulgus = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);   /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x00, "5");
            /* these are the settings for the second coin input, but it seems that the */
            /* game only supports one */
            PORT_DIPNAME(0x1c, 0x1c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x10, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x1c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x14, DEF_STR("1C_3C"));
            /*	PORT_DIPSETTING(    0x00, "Invalid" );disables both coins */
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x80, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_START();       /* DSW1 */
            /* not sure about difficulty
             Code perform a read and (& 0x03). NDMix*/

            PORT_DIPNAME(0x03, 0x03, "Difficulty?");
            PORT_DIPSETTING(0x02, "Easy?");
            PORT_DIPSETTING(0x03, "Normal?");
            PORT_DIPSETTING(0x01, "Hard?");
            PORT_DIPSETTING(0x00, "Hardest?");
            PORT_DIPNAME(0x04, 0x04, "Demo Music");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x70, 0x70, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x30, "10000 50000");
            PORT_DIPSETTING(0x50, "10000 60000");
            PORT_DIPSETTING(0x10, "10000 70000");
            PORT_DIPSETTING(0x70, "20000 60000");
            PORT_DIPSETTING(0x60, "20000 70000");
            PORT_DIPSETTING(0x20, "20000 80000");
            PORT_DIPSETTING(0x40, "30000 70000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
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
    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 tiles */
            512, /* 512 tiles */
            3, /* 3 bits per pixel */
            new int[]{0, 512 * 32 * 8, 2 * 512 * 32 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{256 * 64 * 8 + 4, 256 * 64 * 8, 4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 33 * 8 + 0, 33 * 8 + 1, 33 * 8 + 2, 33 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 64 * 4 + 16 * 16, 32 * 4),
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 64 * 4, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_vulgus = new MachineDriver(
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
                        interrupt, 8
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 64 * 4 + 16 * 16 + 4 * 32 * 8,
            vulgus_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            vulgus_vh_start,
            vulgus_vh_stop,
            vulgus_vh_screenrefresh,
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
    static RomLoadPtr rom_vulgus = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("v2", 0x0000, 0x2000, 0x3e18ff62);
            ROM_LOAD("v3", 0x2000, 0x2000, 0xb4650d82);
            ROM_LOAD("v4", 0x4000, 0x2000, 0x5b26355c);
            ROM_LOAD("v5", 0x6000, 0x2000, 0x4ca7f10e);
            ROM_LOAD("1-8n.bin", 0x8000, 0x2000, 0x6ca5ca41);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1-11c.bin", 0x0000, 0x2000, 0x3bd2acf4);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("1-3d.bin", 0x00000, 0x2000, 0x8bc5d7a5);/* characters */

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2a.bin", 0x00000, 0x2000, 0xe10aaca1);/* tiles */

            ROM_LOAD("2-3a.bin", 0x02000, 0x2000, 0x8da520da);
            ROM_LOAD("2-4a.bin", 0x04000, 0x2000, 0x206a13f1);
            ROM_LOAD("2-5a.bin", 0x06000, 0x2000, 0xb6d81984);
            ROM_LOAD("2-6a.bin", 0x08000, 0x2000, 0x5a26b38f);
            ROM_LOAD("2-7a.bin", 0x0a000, 0x2000, 0x1e1ca773);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2n.bin", 0x00000, 0x2000, 0x6db1b10d);/* sprites */

            ROM_LOAD("2-3n.bin", 0x02000, 0x2000, 0x5d8c34ec);
            ROM_LOAD("2-4n.bin", 0x04000, 0x2000, 0x0071a2e3);
            ROM_LOAD("2-5n.bin", 0x06000, 0x2000, 0x4023a1ec);

            ROM_REGION(0x0600, REGION_PROMS);
            ROM_LOAD("e8.bin", 0x0000, 0x0100, 0x06a83606);/* red component */

            ROM_LOAD("e9.bin", 0x0100, 0x0100, 0xbeacf13c);/* green component */

            ROM_LOAD("e10.bin", 0x0200, 0x0100, 0xde1fb621);/* blue component */

            ROM_LOAD("d1.bin", 0x0300, 0x0100, 0x7179080d);/* char lookup table */

            ROM_LOAD("j2.bin", 0x0400, 0x0100, 0xd0842029);/* sprite lookup table */

            ROM_LOAD("c9.bin", 0x0500, 0x0100, 0x7a1f0bd6);/* tile lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_vulgus2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("vulgus.002", 0x0000, 0x2000, 0xe49d6c5d);
            ROM_LOAD("vulgus.003", 0x2000, 0x2000, 0x51acef76);
            ROM_LOAD("vulgus.004", 0x4000, 0x2000, 0x489e7f60);
            ROM_LOAD("vulgus.005", 0x6000, 0x2000, 0xde3a24a8);
            ROM_LOAD("1-8n.bin", 0x8000, 0x2000, 0x6ca5ca41);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1-11c.bin", 0x0000, 0x2000, 0x3bd2acf4);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("1-3d.bin", 0x00000, 0x2000, 0x8bc5d7a5);/* characters */

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2a.bin", 0x00000, 0x2000, 0xe10aaca1);/* tiles */

            ROM_LOAD("2-3a.bin", 0x02000, 0x2000, 0x8da520da);
            ROM_LOAD("2-4a.bin", 0x04000, 0x2000, 0x206a13f1);
            ROM_LOAD("2-5a.bin", 0x06000, 0x2000, 0xb6d81984);
            ROM_LOAD("2-6a.bin", 0x08000, 0x2000, 0x5a26b38f);
            ROM_LOAD("2-7a.bin", 0x0a000, 0x2000, 0x1e1ca773);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2n.bin", 0x00000, 0x2000, 0x6db1b10d);/* sprites */

            ROM_LOAD("2-3n.bin", 0x02000, 0x2000, 0x5d8c34ec);
            ROM_LOAD("2-4n.bin", 0x04000, 0x2000, 0x0071a2e3);
            ROM_LOAD("2-5n.bin", 0x06000, 0x2000, 0x4023a1ec);

            ROM_REGION(0x0600, REGION_PROMS);
            ROM_LOAD("e8.bin", 0x0000, 0x0100, 0x06a83606);/* red component */

            ROM_LOAD("e9.bin", 0x0100, 0x0100, 0xbeacf13c);/* green component */

            ROM_LOAD("e10.bin", 0x0200, 0x0100, 0xde1fb621);/* blue component */

            ROM_LOAD("d1.bin", 0x0300, 0x0100, 0x7179080d);/* char lookup table */

            ROM_LOAD("j2.bin", 0x0400, 0x0100, 0xd0842029);/* sprite lookup table */

            ROM_LOAD("c9.bin", 0x0500, 0x0100, 0x7a1f0bd6);/* tile lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_vulgusj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("1-4n.bin", 0x0000, 0x2000, 0xfe5a5ca5);
            ROM_LOAD("1-5n.bin", 0x2000, 0x2000, 0x847e437f);
            ROM_LOAD("1-6n.bin", 0x4000, 0x2000, 0x4666c436);
            ROM_LOAD("1-7n.bin", 0x6000, 0x2000, 0xff2097f9);
            ROM_LOAD("1-8n.bin", 0x8000, 0x2000, 0x6ca5ca41);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1-11c.bin", 0x0000, 0x2000, 0x3bd2acf4);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("1-3d.bin", 0x00000, 0x2000, 0x8bc5d7a5);/* characters */

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2a.bin", 0x00000, 0x2000, 0xe10aaca1);/* tiles */

            ROM_LOAD("2-3a.bin", 0x02000, 0x2000, 0x8da520da);
            ROM_LOAD("2-4a.bin", 0x04000, 0x2000, 0x206a13f1);
            ROM_LOAD("2-5a.bin", 0x06000, 0x2000, 0xb6d81984);
            ROM_LOAD("2-6a.bin", 0x08000, 0x2000, 0x5a26b38f);
            ROM_LOAD("2-7a.bin", 0x0a000, 0x2000, 0x1e1ca773);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2-2n.bin", 0x00000, 0x2000, 0x6db1b10d);/* sprites */

            ROM_LOAD("2-3n.bin", 0x02000, 0x2000, 0x5d8c34ec);
            ROM_LOAD("2-4n.bin", 0x04000, 0x2000, 0x0071a2e3);
            ROM_LOAD("2-5n.bin", 0x06000, 0x2000, 0x4023a1ec);

            ROM_REGION(0x0600, REGION_PROMS);
            ROM_LOAD("e8.bin", 0x0000, 0x0100, 0x06a83606);/* red component */

            ROM_LOAD("e9.bin", 0x0100, 0x0100, 0xbeacf13c);/* green component */

            ROM_LOAD("e10.bin", 0x0200, 0x0100, 0xde1fb621);/* blue component */

            ROM_LOAD("d1.bin", 0x0300, 0x0100, 0x7179080d);/* char lookup table */

            ROM_LOAD("j2.bin", 0x0400, 0x0100, 0xd0842029);/* sprite lookup table */

            ROM_LOAD("c9.bin", 0x0500, 0x0100, 0x7a1f0bd6);/* tile lookup table */

            ROM_END();
        }
    };

    public static GameDriver driver_vulgus = new GameDriver("1984", "vulgus", "vulgus.java", rom_vulgus, null, machine_driver_vulgus, input_ports_vulgus, null, ROT270, "Capcom", "Vulgus (set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_vulgus2 = new GameDriver("1984", "vulgus2", "vulgus.java", rom_vulgus2, driver_vulgus, machine_driver_vulgus, input_ports_vulgus, null, ROT270, "Capcom", "Vulgus (set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_vulgusj = new GameDriver("1984", "vulgusj", "vulgus.java", rom_vulgusj, driver_vulgus, machine_driver_vulgus, input_ports_vulgus, null, ROT270, "Capcom", "Vulgus (Japan?)", GAME_NO_COCKTAIL);
}
