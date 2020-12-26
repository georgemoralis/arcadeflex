/**
 * *************************************************************************
 *
 * Toki
 *
 * driver by Jarek Parchanski
 *
 **************************************************************************
 */
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205.*;
import static gr.codebb.arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.toki.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;

public class toki {

    static UBytePtr ram = new UBytePtr();

    public static ReadHandlerPtr toki_read_ports = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0:
                    return input_port_3_r.handler(0) + (input_port_4_r.handler(0) << 8);
                case 2:
                    return input_port_1_r.handler(0) + (input_port_2_r.handler(0) << 8);
                case 4:
                    return input_port_0_r.handler(0);
                default:
                    return 0;
            }
        }
    };

    public static WriteHandlerPtr toki_soundcommand_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data & 0xff);
            cpu_cause_interrupt(1, 0xff);
        }
    };

    static int msm5205next;
    static int toggle = 0;

    public static vclk_interruptPtr toki_adpcm_int = new vclk_interruptPtr() {
        public void handler(int num) {
            {
                MSM5205_data_w.handler(0, msm5205next);
                msm5205next = msm5205next >> 4;

                toggle ^= 1;
                if (toggle != 0) {
                    cpu_cause_interrupt(1, Z80_NMI_INT);
                }
            }
        }
    };

    public static WriteHandlerPtr toki_adpcm_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU2);

            /* the code writes either 2 or 3 in the bottom two bits */
            bankaddress = 0x10000 + (data & 0x01) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            MSM5205_reset_w.handler(0, data & 0x08);
        }
    };

    public static WriteHandlerPtr toki_adpcm_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msm5205next = data;
        }
    };

    public static ReadHandlerPtr pip = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0xffff;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x05ffff, MRA_ROM),
                new MemoryReadAddress(0x060000, 0x06dfff, MRA_BANK2),
                new MemoryReadAddress(0x06e000, 0x06e7ff, paletteram_word_r),
                new MemoryReadAddress(0x06e800, 0x06efff, toki_background1_videoram_r),
                new MemoryReadAddress(0x06f000, 0x06f7ff, toki_background2_videoram_r),
                new MemoryReadAddress(0x06f800, 0x06ffff, toki_foreground_videoram_r),
                new MemoryReadAddress(0x072000, 0x072001, watchdog_reset_r), /* probably */
                new MemoryReadAddress(0x0c0000, 0x0c0005, toki_read_ports),
                new MemoryReadAddress(0x0c000e, 0x0c000f, pip), /* sound related, if we return 0 the code writes */
                /* the sound command quickly followed by 0 and the */
                /* sound CPU often misses the command. */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x05ffff, MWA_ROM),
                new MemoryWriteAddress(0x060000, 0x06dfff, MWA_BANK2, ram),
                new MemoryWriteAddress(0x06e000, 0x06e7ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x06e800, 0x06efff, toki_background1_videoram_w, toki_background1_videoram, toki_background1_videoram_size),
                new MemoryWriteAddress(0x06f000, 0x06f7ff, toki_background2_videoram_w, toki_background2_videoram, toki_background2_videoram_size),
                new MemoryWriteAddress(0x06f800, 0x06ffff, toki_foreground_videoram_w, toki_foreground_videoram, toki_foreground_videoram_size),
                new MemoryWriteAddress(0x071000, 0x071001, MWA_NOP), /* sprite related? seems another scroll register; */
                /* gets written the same value as 75000a (bg2 scrollx) */
                new MemoryWriteAddress(0x071804, 0x071807, MWA_NOP), /* sprite related; always 01be0100 */
                new MemoryWriteAddress(0x07180e, 0x071e45, MWA_BANK3, toki_sprites_dataram, toki_sprites_dataram_size),
                new MemoryWriteAddress(0x075000, 0x075001, toki_soundcommand_w),
                new MemoryWriteAddress(0x075004, 0x07500b, MWA_BANK4, toki_scrollram),
                new MemoryWriteAddress(0x0a002a, 0x0a002d, toki_linescroll_w), /* scroll register used to waggle the title screen */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xec00, 0xec00, YM3812_status_port_0_r),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xf800, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xe000, 0xe000, toki_adpcm_control_w), /* MSM5205 + ROM bank */
                new MemoryWriteAddress(0xe400, 0xe400, toki_adpcm_data_w),
                new MemoryWriteAddress(0xec00, 0xec00, YM3812_control_port_0_w),
                new MemoryWriteAddress(0xec01, 0xec01, YM3812_write_port_0_w),
                new MemoryWriteAddress(0xec08, 0xec08, YM3812_control_port_0_w), /* mirror address, it seems */
                new MemoryWriteAddress(0xec09, 0xec09, YM3812_write_port_0_w), /* mirror address, it seems */
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_toki = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x1F, 0x1F, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x15, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x17, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x19, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x1B, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("8C_3C"));
            PORT_DIPSETTING(0x1D, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("5C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x1F, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x13, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x11, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0F, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0D, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0B, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x1E, "A 1/1 B 1/2");
            PORT_DIPSETTING(0x14, "A 2/1 B 1/3");
            PORT_DIPSETTING(0x0A, "A 3/1 B 1/5");
            PORT_DIPSETTING(0x00, "A 5/1 B 1/6");
            PORT_DIPSETTING(0x01, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x20, 0x20, "Joysticks");
            PORT_DIPSETTING(0x20, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0C, 0x0C, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "50000 150000");
            PORT_DIPSETTING(0x00, "70000 140000 210000");
            PORT_DIPSETTING(0x0C, "70000");
            PORT_DIPSETTING(0x04, "100000 200000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Medium");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x40, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x40, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout toki_charlayout = new GfxLayout(
            8, 8,
            4096,
            4,
            new int[]{4096 * 16 * 8 + 0, 4096 * 16 * 8 + 4, 0, 4},
            new int[]{3, 2, 1, 0, 8 + 3, 8 + 2, 8 + 1, 8 + 0},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8
    );

    static GfxLayout toki_tilelayout = new GfxLayout(
            16, 16,
            4096,
            4,
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4},
            new int[]{3, 2, 1, 0, 16 + 3, 16 + 2, 16 + 1, 16 + 0,
                64 * 8 + 3, 64 * 8 + 2, 64 * 8 + 1, 64 * 8 + 0, 64 * 8 + 16 + 3, 64 * 8 + 16 + 2, 64 * 8 + 16 + 1, 64 * 8 + 16 + 0},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32},
            128 * 8
    );

    static GfxLayout toki_spritelayout = new GfxLayout(
            16, 16,
            8192,
            4,
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4},
            new int[]{3, 2, 1, 0, 16 + 3, 16 + 2, 16 + 1, 16 + 0,
                64 * 8 + 3, 64 * 8 + 2, 64 * 8 + 1, 64 * 8 + 0, 64 * 8 + 16 + 3, 64 * 8 + 16 + 2, 64 * 8 + 16 + 1, 64 * 8 + 16 + 0},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32},
            128 * 8
    );

    static GfxDecodeInfo toki_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, toki_charlayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, toki_spritelayout, 0 * 16, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, toki_tilelayout, 32 * 16, 16),
                new GfxDecodeInfo(REGION_GFX4, 0, toki_tilelayout, 48 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxLayout tokib_charlayout = new GfxLayout(
            8, 8, /* 8 by 8 */
            4096, /* 4096 characters */
            4, /* 4 bits per pixel */
            new int[]{4096 * 8 * 8 * 3, 4096 * 8 * 8 * 2, 4096 * 8 * 8 * 1, 4096 * 8 * 8 * 0}, /* planes */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7}, /* x bit */
            new int[]{0, 8, 16, 24, 32, 40, 48, 56}, /* y bit */
            8 * 8
    );

    static GfxLayout tokib_tilelayout = new GfxLayout(
            16, 16, /* 16 by 16 */
            4096, /* 4096 characters */
            4, /* 4 bits per pixel */
            new int[]{4096 * 16 * 16 * 3, 4096 * 16 * 16 * 2, 4096 * 16 * 16 * 1, 4096 * 16 * 16 * 0}, /* planes */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                0x8000 * 8 + 0, 0x8000 * 8 + 1, 0x8000 * 8 + 2, 0x8000 * 8 + 3, 0x8000 * 8 + 4,
                0x8000 * 8 + 5, 0x8000 * 8 + 6, 0x8000 * 8 + 7}, /* x bit */
            new int[]{
                0, 8, 16, 24, 32, 40, 48, 56,
                0x10000 * 8 + 0, 0x10000 * 8 + 8, 0x10000 * 8 + 16, 0x10000 * 8 + 24, 0x10000 * 8 + 32,
                0x10000 * 8 + 40, 0x10000 * 8 + 48, 0x10000 * 8 + 56}, /* y bit */
            8 * 8
    );

    static GfxLayout tokib_spriteslayout = new GfxLayout(
            16, 16, /* 16 by 16 */
            8192, /* 8192 sprites */
            4, /* 4 bits per pixel */
            new int[]{8192 * 16 * 16 * 3, 8192 * 16 * 16 * 2, 8192 * 16 * 16 * 1, 8192 * 16 * 16 * 0}, /* planes */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                128 + 0, 128 + 1, 128 + 2, 128 + 3, 128 + 4, 128 + 5, 128 + 6, 128 + 7}, /* x bit */
            new int[]{0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 80, 88, 96, 104, 112, 120}, /* y bit */
            16 * 16
    );

    static GfxDecodeInfo tokib_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tokib_charlayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, tokib_spriteslayout, 0 * 16, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, tokib_tilelayout, 32 * 16, 16),
                new GfxDecodeInfo(REGION_GFX4, 0, tokib_tilelayout, 48 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static YM3526interface ym3812_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            3600000, /* 3.600000 MHz ? (partially supported) */
            new int[]{60} /* (not supported) */
    );

    static MSM5205interface msm5205_interface = new MSM5205interface(
            1, /* 1 chip             */
            384000, /* 384KHz             */
            new vclk_interruptPtr[]{toki_adpcm_int},/* interrupt function */
            new int[]{MSM5205_S96_4B}, /* 4KHz               */
            new int[]{60}
    );

    static MachineDriver machine_driver_toki = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        16000000, /* with less than 14MHz there are slowdowns and the */
                        /* title screen doesn't wave correctly */
                        readmem, writemem,
                        null, null,
                        toki_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz (?) */
                        sound_readmem, sound_writemem,
                        null, null,
                        ignore_interrupt, 0 /* IRQs are caused by the main CPU?? */
                /* NMIs are caused by the ADPCM chip */
                ),},
            57, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8,
            new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 32 * 8 - 1),
            toki_gfxdecodeinfo,
            4 * 256, 4 * 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            toki_vh_start,
            toki_vh_stop,
            toki_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm5205_interface
                )
            }
    );

    static MachineDriver machine_driver_tokib = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        16000000, /* with less than 14MHz there are slowdowns and the */
                        /* title screen doesn't wave correctly */
                        readmem, writemem,
                        null, null,
                        toki_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz (?) */
                        sound_readmem, sound_writemem,
                        null, null,
                        ignore_interrupt, 0 /* IRQs are caused by the main CPU?? */
                /* NMIs are caused by the ADPCM chip */
                ),},
            57, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8,
            new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 32 * 8 - 1),
            tokib_gfxdecodeinfo,
            4 * 256, 4 * 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            toki_vh_start,
            toki_vh_stop,
            toki_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm5205_interface
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
    static RomLoadPtr rom_toki = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("tokijp.006", 0x00000, 0x20000, 0x03d726b1);
            ROM_LOAD_ODD("tokijp.004", 0x00000, 0x20000, 0x54a45e12);
            ROM_LOAD_EVEN("tokijp.005", 0x40000, 0x10000, 0xd6a82808);
            ROM_LOAD_ODD("tokijp.003", 0x40000, 0x10000, 0xa01a5b10);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */
            /* is this the Z80 code? maybe its encrypted */

            ROM_LOAD("tokijp.008", 0x00000, 0x2000, 0x6c87c4c5);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tokijp.001", 0x000000, 0x10000, 0x8aa964a2);/* chars */

            ROM_LOAD("tokijp.002", 0x010000, 0x10000, 0x86e87e48);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.ob1", 0x000000, 0x80000, 0xa27a80ba);/* sprites */

            ROM_LOAD("toki.ob2", 0x080000, 0x80000, 0xfa687718);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk1", 0x000000, 0x80000, 0xfdaa5f4b);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk2", 0x000000, 0x80000, 0xd86ac664);/* tiles 2 */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples */

            ROM_LOAD("tokijp.009", 0x00000, 0x20000, 0xae7a6b8b);

            ROM_REGION(0x10000, REGION_USER1);/* unknown */

            ROM_LOAD("tokijp.007", 0x00000, 0x10000, 0xa67969c4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_toki2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("tokijp.006", 0x00000, 0x20000, 0x03d726b1);
            ROM_LOAD_ODD("4c.10k", 0x00000, 0x20000, 0xb2c345c5);
            ROM_LOAD_EVEN("tokijp.005", 0x40000, 0x10000, 0xd6a82808);
            ROM_LOAD_ODD("tokijp.003", 0x40000, 0x10000, 0xa01a5b10);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */
            /* is this the Z80 code? maybe its encrypted */

            ROM_LOAD("tokijp.008", 0x00000, 0x2000, 0x6c87c4c5);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tokijp.001", 0x000000, 0x10000, 0x8aa964a2);/* chars */

            ROM_LOAD("tokijp.002", 0x010000, 0x10000, 0x86e87e48);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.ob1", 0x000000, 0x80000, 0xa27a80ba);/* sprites */

            ROM_LOAD("toki.ob2", 0x080000, 0x80000, 0xfa687718);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk1", 0x000000, 0x80000, 0xfdaa5f4b);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk2", 0x000000, 0x80000, 0xd86ac664);/* tiles 2 */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples */

            ROM_LOAD("tokijp.009", 0x00000, 0x20000, 0xae7a6b8b);

            ROM_REGION(0x10000, REGION_USER1);/* unknown */

            ROM_LOAD("tokijp.007", 0x00000, 0x10000, 0xa67969c4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_toki3 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("l10_6.bin", 0x00000, 0x20000, 0x94015d91);
            ROM_LOAD_ODD("k10_4e.bin", 0x00000, 0x20000, 0x531bd3ef);
            ROM_LOAD_EVEN("tokijp.005", 0x40000, 0x10000, 0xd6a82808);
            ROM_LOAD_ODD("tokijp.003", 0x40000, 0x10000, 0xa01a5b10);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */
            /* is this the Z80 code? maybe its encrypted */

            ROM_LOAD("tokijp.008", 0x00000, 0x2000, 0x6c87c4c5);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tokijp.001", 0x000000, 0x10000, 0x8aa964a2);/* chars */

            ROM_LOAD("tokijp.002", 0x010000, 0x10000, 0x86e87e48);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.ob1", 0x000000, 0x80000, 0xa27a80ba);/* sprites */

            ROM_LOAD("toki.ob2", 0x080000, 0x80000, 0xfa687718);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk1", 0x000000, 0x80000, 0xfdaa5f4b);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk2", 0x000000, 0x80000, 0xd86ac664);/* tiles 2 */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples */

            ROM_LOAD("tokijp.009", 0x00000, 0x20000, 0xae7a6b8b);

            ROM_REGION(0x10000, REGION_USER1);/* unknown */

            ROM_LOAD("tokijp.007", 0x00000, 0x10000, 0xa67969c4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tokiu = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("6b.10m", 0x00000, 0x20000, 0x3674d9fe);
            ROM_LOAD_ODD("14.10k", 0x00000, 0x20000, 0xbfdd48af);
            ROM_LOAD_EVEN("tokijp.005", 0x40000, 0x10000, 0xd6a82808);
            ROM_LOAD_ODD("tokijp.003", 0x40000, 0x10000, 0xa01a5b10);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for code */
            /* is this the Z80 code? maybe its encrypted */

            ROM_LOAD("tokijp.008", 0x00000, 0x2000, 0x6c87c4c5);

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tokijp.001", 0x000000, 0x10000, 0x8aa964a2);/* chars */

            ROM_LOAD("tokijp.002", 0x010000, 0x10000, 0x86e87e48);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.ob1", 0x000000, 0x80000, 0xa27a80ba);/* sprites */

            ROM_LOAD("toki.ob2", 0x080000, 0x80000, 0xfa687718);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk1", 0x000000, 0x80000, 0xfdaa5f4b);/* tiles 1 */

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.bk2", 0x000000, 0x80000, 0xd86ac664);/* tiles 2 */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples */

            ROM_LOAD("tokijp.009", 0x00000, 0x20000, 0xae7a6b8b);

            ROM_REGION(0x10000, REGION_USER1);/* unknown */

            ROM_LOAD("tokijp.007", 0x00000, 0x10000, 0xa67969c4);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tokib = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x60000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("toki.e3", 0x00000, 0x20000, 0xae9b3da4);
            ROM_LOAD_ODD("toki.e5", 0x00000, 0x20000, 0x66a5a1d6);
            ROM_LOAD_EVEN("tokijp.005", 0x40000, 0x10000, 0xd6a82808);
            ROM_LOAD_ODD("tokijp.003", 0x40000, 0x10000, 0xa01a5b10);

            ROM_REGION(0x18000, REGION_CPU2);/* 64k for code + 32k for banked data */

            ROM_LOAD("toki.e1", 0x00000, 0x8000, 0x2832ef75);
            ROM_CONTINUE(0x10000, 0x8000);/* banked at 8000-bfff */

            ROM_REGION(0x020000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.e21", 0x000000, 0x08000, 0xbb8cacbd);/* chars */

            ROM_LOAD("toki.e13", 0x008000, 0x08000, 0x052ad275);
            ROM_LOAD("toki.e22", 0x010000, 0x08000, 0x04dcdc21);
            ROM_LOAD("toki.e7", 0x018000, 0x08000, 0x70729106);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.e26", 0x000000, 0x20000, 0xa8ba71fc);/* sprites */

            ROM_LOAD("toki.e28", 0x020000, 0x20000, 0x29784948);
            ROM_LOAD("toki.e34", 0x040000, 0x20000, 0xe5f6e19b);
            ROM_LOAD("toki.e36", 0x060000, 0x20000, 0x96e8db8b);
            ROM_LOAD("toki.e30", 0x080000, 0x20000, 0x770d2b1b);
            ROM_LOAD("toki.e32", 0x0a0000, 0x20000, 0xc289d246);
            ROM_LOAD("toki.e38", 0x0c0000, 0x20000, 0x87f4e7fb);
            ROM_LOAD("toki.e40", 0x0e0000, 0x20000, 0x96e87350);

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.e23", 0x000000, 0x10000, 0xfeb13d35);/* tiles 1 */

            ROM_LOAD("toki.e24", 0x010000, 0x10000, 0x5b365637);
            ROM_LOAD("toki.e15", 0x020000, 0x10000, 0x617c32e6);
            ROM_LOAD("toki.e16", 0x030000, 0x10000, 0x2a11c0f0);
            ROM_LOAD("toki.e17", 0x040000, 0x10000, 0xfbc3d456);
            ROM_LOAD("toki.e18", 0x050000, 0x10000, 0x4c2a72e1);
            ROM_LOAD("toki.e8", 0x060000, 0x10000, 0x46a1b821);
            ROM_LOAD("toki.e9", 0x070000, 0x10000, 0x82ce27f6);

            ROM_REGION(0x080000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("toki.e25", 0x000000, 0x10000, 0x63026cad);/* tiles 2 */

            ROM_LOAD("toki.e20", 0x010000, 0x10000, 0xa7f2ce26);
            ROM_LOAD("toki.e11", 0x020000, 0x10000, 0x48989aa0);
            ROM_LOAD("toki.e12", 0x030000, 0x10000, 0xc2ad9342);
            ROM_LOAD("toki.e19", 0x040000, 0x10000, 0x6cd22b18);
            ROM_LOAD("toki.e14", 0x050000, 0x10000, 0x859e313a);
            ROM_LOAD("toki.e10", 0x060000, 0x10000, 0xe15c1d0f);
            ROM_LOAD("toki.e6", 0x070000, 0x10000, 0x6f4b878a);
            ROM_END();
        }
    };

    public static InitDriverPtr init_tokib = new InitDriverPtr() {
        public void handler() {

            UBytePtr temp = new UBytePtr(65536 * 2);
            int i, offs;

            /* invert the sprite data in the ROMs */
            for (i = 0; i < memory_region_length(REGION_GFX2); i++) {
                memory_region(REGION_GFX2).write(i, memory_region(REGION_GFX2).read(i) ^ 0xff);
            }

            /* merge background tile graphics together */
            if (temp != null) {
                for (offs = 0; offs < memory_region_length(REGION_GFX3); offs += 0x20000) {
                    UBytePtr base = new UBytePtr(memory_region(REGION_GFX3), offs);
                    memcpy(temp, base, 65536 * 2);
                    for (i = 0; i < 16; i++) {
                        memcpy(base, 0x00000 + i * 0x800, temp, 0x0000 + i * 0x2000, 0x800);
                        memcpy(base, 0x10000 + i * 0x800, temp, 0x0800 + i * 0x2000, 0x800);
                        memcpy(base, 0x08000 + i * 0x800, temp, 0x1000 + i * 0x2000, 0x800);
                        memcpy(base, 0x18000 + i * 0x800, temp, 0x1800 + i * 0x2000, 0x800);
                    }
                }
                for (offs = 0; offs < memory_region_length(REGION_GFX4); offs += 0x20000) {
                    UBytePtr base = new UBytePtr(memory_region(REGION_GFX4), offs);
                    memcpy(temp, base, 65536 * 2);
                    for (i = 0; i < 16; i++) {
                        memcpy(base, 0x00000 + i * 0x800, temp, 0x0000 + i * 0x2000, 0x800);
                        memcpy(base, 0x10000 + i * 0x800, temp, 0x0800 + i * 0x2000, 0x800);
                        memcpy(base, 0x08000 + i * 0x800, temp, 0x1000 + i * 0x2000, 0x800);
                        memcpy(base, 0x18000 + i * 0x800, temp, 0x1800 + i * 0x2000, 0x800);
                    }
                }
            }
        }
    };

    public static GameDriver driver_toki = new GameDriver("1989", "toki", "toki.java", rom_toki, null, machine_driver_toki, input_ports_toki, null, ROT0, "Tad", "Toki (set 1)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_toki2 = new GameDriver("1989", "toki2", "toki.java", rom_toki2, driver_toki, machine_driver_toki, input_ports_toki, null, ROT0, "Tad", "Toki (set 2)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_toki3 = new GameDriver("1989", "toki3", "toki.java", rom_toki3, driver_toki, machine_driver_toki, input_ports_toki, null, ROT0, "Tad", "Toki (set 3)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tokiu = new GameDriver("1989", "tokiu", "toki.java", rom_tokiu, driver_toki, machine_driver_toki, input_ports_toki, null, ROT0, "Tad (Fabtek license)", "Toki (US)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tokib = new GameDriver("1989", "tokib", "toki.java", rom_tokib, driver_toki, machine_driver_tokib, input_ports_toki, init_tokib, ROT0, "bootleg", "Toki (bootleg)", GAME_NO_COCKTAIL);
}
