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
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.vidhrdw.wc90.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;

public class wc90 {

    public static WriteHandlerPtr wc90_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + ((data & 0xf8) << 8);
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };
    public static WriteHandlerPtr wc90_bankswitch1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU2);

            bankaddress = 0x10000 + ((data & 0xf8) << 8);
            cpu_setbank(2, new UBytePtr(RAM, bankaddress));
        }
    };

    public static WriteHandlerPtr wc90_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(2, Z80_NMI_INT);
        }
    };

    static MemoryReadAddress wc90_readmem1[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x9fff, MRA_RAM), /* Main RAM */
                new MemoryReadAddress(0xa000, 0xa7ff, wc90_tile_colorram_r), /* bg 1 color ram */
                new MemoryReadAddress(0xa800, 0xafff, wc90_tile_videoram_r), /* bg 1 tile ram */
                new MemoryReadAddress(0xb000, 0xbfff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc7ff, wc90_tile_colorram2_r), /* bg 2 color ram */
                new MemoryReadAddress(0xc800, 0xcfff, wc90_tile_videoram2_r), /* bg 2 tile ram */
                new MemoryReadAddress(0xd000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe7ff, colorram_r), /* fg color ram */
                new MemoryReadAddress(0xe800, 0xefff, videoram_r), /* fg tile ram */
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_BANK1),
                new MemoryReadAddress(0xf800, 0xfbff, wc90_shared_r),
                new MemoryReadAddress(0xfc00, 0xfc00, input_port_0_r), /* Stick 1 */
                new MemoryReadAddress(0xfc02, 0xfc02, input_port_1_r), /* Stick 2 */
                new MemoryReadAddress(0xfc05, 0xfc05, input_port_4_r), /* Start  Coin */
                new MemoryReadAddress(0xfc06, 0xfc06, input_port_2_r), /* DIP Switch A */
                new MemoryReadAddress(0xfc07, 0xfc07, input_port_3_r), /* DIP Switch B */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress wc90_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd7ff, MRA_RAM),
                new MemoryReadAddress(0xd800, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe7ff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_BANK2),
                new MemoryReadAddress(0xf800, 0xfbff, wc90_shared_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress wc90_writemem1[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x9fff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa7ff, wc90_tile_colorram_w, wc90_tile_colorram),
                new MemoryWriteAddress(0xa800, 0xafff, wc90_tile_videoram_w, wc90_tile_videoram, wc90_tile_videoram_size),
                new MemoryWriteAddress(0xb000, 0xbfff, MWA_RAM),
                new MemoryWriteAddress(0xc000, 0xc7ff, wc90_tile_colorram2_w, wc90_tile_colorram2),
                new MemoryWriteAddress(0xc800, 0xcfff, wc90_tile_videoram2_w, wc90_tile_videoram2, wc90_tile_videoram_size2),
                new MemoryWriteAddress(0xd000, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xefff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xfbff, wc90_shared_w, wc90_shared),
                new MemoryWriteAddress(0xfc02, 0xfc02, MWA_RAM, wc90_scroll0ylo),
                new MemoryWriteAddress(0xfc03, 0xfc03, MWA_RAM, wc90_scroll0yhi),
                new MemoryWriteAddress(0xfc06, 0xfc06, MWA_RAM, wc90_scroll0xlo),
                new MemoryWriteAddress(0xfc07, 0xfc07, MWA_RAM, wc90_scroll0xhi),
                new MemoryWriteAddress(0xfc22, 0xfc22, MWA_RAM, wc90_scroll1ylo),
                new MemoryWriteAddress(0xfc23, 0xfc23, MWA_RAM, wc90_scroll1yhi),
                new MemoryWriteAddress(0xfc26, 0xfc26, MWA_RAM, wc90_scroll1xlo),
                new MemoryWriteAddress(0xfc27, 0xfc27, MWA_RAM, wc90_scroll1xhi),
                new MemoryWriteAddress(0xfc42, 0xfc42, MWA_RAM, wc90_scroll2ylo),
                new MemoryWriteAddress(0xfc43, 0xfc43, MWA_RAM, wc90_scroll2yhi),
                new MemoryWriteAddress(0xfc46, 0xfc46, MWA_RAM, wc90_scroll2xlo),
                new MemoryWriteAddress(0xfc47, 0xfc47, MWA_RAM, wc90_scroll2xhi),
                new MemoryWriteAddress(0xfcc0, 0xfcc0, wc90_sound_command_w),
                new MemoryWriteAddress(0xfcd0, 0xfcd0, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xfce0, 0xfce0, wc90_bankswitch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress wc90_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xd800, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe7ff, paletteram_xxxxBBBBRRRRGGGG_swap_w, paletteram),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xfbff, wc90_shared_w),
                new MemoryWriteAddress(0xfc00, 0xfc00, wc90_bankswitch1_w),
                new MemoryWriteAddress(0xfc01, 0xfc01, MWA_NOP), /* ??? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xf800, YM2203_status_port_0_r),
                new MemoryReadAddress(0xf801, 0xf801, YM2203_read_port_0_r),
                new MemoryReadAddress(0xf802, 0xf802, YM2203_status_port_1_r),
                new MemoryReadAddress(0xf803, 0xf803, YM2203_read_port_1_r),
                new MemoryReadAddress(0xfc00, 0xfc00, MRA_NOP), /* ??? adpcm ??? */
                new MemoryReadAddress(0xfc10, 0xfc10, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, YM2203_control_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xf802, 0xf802, YM2203_control_port_1_w),
                new MemoryWriteAddress(0xf803, 0xf803, YM2203_write_port_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_wc90 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 bit 0-5 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 bit 0-5 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSWA */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "10 Coins/1 Credit");
            PORT_DIPSETTING(0x08, DEF_STR("9C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("8C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("7C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x30, "Easy");
            PORT_DIPSETTING(0x10, "Normal");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, "Countdown Speed");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x00, "Fast");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSWB */

            PORT_DIPNAME(0x03, 0x03, "1 Player Game Time");
            PORT_DIPSETTING(0x01, "1:00");
            PORT_DIPSETTING(0x02, "1:30");
            PORT_DIPSETTING(0x03, "2:00");
            PORT_DIPSETTING(0x00, "2:30");
            PORT_DIPNAME(0x1c, 0x1c, "2 Players Game Time");
            PORT_DIPSETTING(0x0c, "1:00");
            PORT_DIPSETTING(0x14, "1:30");
            PORT_DIPSETTING(0x04, "2:00");
            PORT_DIPSETTING(0x18, "2:30");
            PORT_DIPSETTING(0x1c, "3:00");
            PORT_DIPSETTING(0x08, "3:30");
            PORT_DIPSETTING(0x10, "4:00");
            PORT_DIPSETTING(0x00, "5:00");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x80, "Japanese");

            PORT_START();
            /* IN2 bit 0-3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 1024 characters */
            4, /* 8 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            2048, /* 2048 tiles */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            4096, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 1024 * 256 * 8 + 0 * 4, 1024 * 256 * 8 + 1 * 4, 2 * 4, 3 * 4, 1024 * 256 * 8 + 2 * 4, 1024 * 256 * 8 + 3 * 4,
                16 * 8 + 0 * 4, 16 * 8 + 1 * 4, 1024 * 256 * 8 + 16 * 8 + 0 * 4, 1024 * 256 * 8 + 16 * 8 + 1 * 4, 16 * 8 + 2 * 4, 16 * 8 + 3 * 4, 1024 * 256 * 8 + 16 * 8 + 2 * 4, 1024 * 256 * 8 + 16 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16},
            64 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 1 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout, 2 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tilelayout, 3 * 16 * 16, 16 * 16),
                new GfxDecodeInfo(REGION_GFX4, 0x00000, spritelayout, 0 * 16 * 16, 16 * 16), // sprites
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the 2203 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_irq_line(2, 0, irq != 0 ? ASSERT_LINE : CLEAR_LINE);

        }
    };
    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            6000000, /* 6 MHz ????? seems awfully fast, I don't even know if the */
            /*  YM2203 can go at that speed */
            new int[]{YM2203_VOL(25, 25), YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static MachineDriver machine_driver_wc90 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6.0 Mhz ??? */
                        wc90_readmem1, wc90_writemem1, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6.0 Mhz ??? */
                        wc90_readmem2, wc90_writemem2, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ???? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the YM2203 */
                /* NMIs are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            4 * 16 * 16, 4 * 16 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            wc90_vh_start,
            wc90_vh_stop,
            wc90_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                )
            }
    );

    static RomLoadPtr rom_wc90 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 128k for code */

            ROM_LOAD("ic87_01.bin", 0x00000, 0x08000, 0x4a1affbc);/* c000-ffff is not used */

            ROM_LOAD("ic95_02.bin", 0x10000, 0x10000, 0x847d439c);/* banked at f000-f7ff */

            ROM_REGION(0x20000, REGION_CPU2);/* 96k for code */ /* Second CPU */


            ROM_LOAD("ic67_04.bin", 0x00000, 0x10000, 0xdc6eaf00);/* c000-ffff is not used */

            ROM_LOAD("ic56_03.bin", 0x10000, 0x10000, 0x1ac02b3b);/* banked at f000-f7ff */

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the audio CPU */

            ROM_LOAD("ic54_05.bin", 0x00000, 0x10000, 0x27c348b3);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic85_07v.bin", 0x00000, 0x10000, 0xc5219426);/* characters */

            ROM_REGION(0x040000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic86_08v.bin", 0x00000, 0x20000, 0x8fa1a1ff);/* tiles #1 */

            ROM_LOAD("ic90_09v.bin", 0x20000, 0x20000, 0x99f8841c);/* tiles #2 */

            ROM_REGION(0x040000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic87_10v.bin", 0x00000, 0x20000, 0x8232093d);/* tiles #3 */

            ROM_LOAD("ic91_11v.bin", 0x20000, 0x20000, 0x188d3789);/* tiles #4 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic50_12v.bin", 0x00000, 0x20000, 0xda1fe922);/* sprites  */

            ROM_LOAD("ic54_13v.bin", 0x20000, 0x20000, 0x9ad03c2c);/* sprites  */

            ROM_LOAD("ic60_14v.bin", 0x40000, 0x20000, 0x499dfb1b);/* sprites  */

            ROM_LOAD("ic65_15v.bin", 0x60000, 0x20000, 0xd8ea5c81);/* sprites  */

            ROM_REGION(0x20000, REGION_SOUND1);/* 64k for ADPCM samples */

            ROM_LOAD("ic82_06.bin", 0x00000, 0x20000, 0x2fd692ed);
            ROM_END();
        }
    };

    public static GameDriver driver_wc90 = new GameDriver("1989", "wc90", "wc90.java", rom_wc90, null, machine_driver_wc90, input_ports_wc90, null, ROT0, "Tecmo", "World Cup 90", GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL);
}
