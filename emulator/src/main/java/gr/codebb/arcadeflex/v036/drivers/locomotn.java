/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.rallyx.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.timeplt.*;

public class locomotn {

    public static WriteHandlerPtr coin_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(0, data & 1);
        }
    };
    public static WriteHandlerPtr coin_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(1, data & 1);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0x9800, 0x9fff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xa080, 0xa080, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xa100, 0xa100, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0xa180, 0xa180, input_port_3_r), /* DSW */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress jungler_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8400, 0x87ff, rallyx_videoram2_w, rallyx_videoram2),
                new MemoryWriteAddress(0x8800, 0x8bff, colorram_w, colorram),
                new MemoryWriteAddress(0x8c00, 0x8fff, rallyx_colorram2_w, rallyx_colorram2),
                new MemoryWriteAddress(0x9800, 0x9fff, MWA_RAM),
                new MemoryWriteAddress(0xa034, 0xa03f, MWA_RAM, rallyx_radarattr),
                new MemoryWriteAddress(0xa080, 0xa080, watchdog_reset_w),
                new MemoryWriteAddress(0xa100, 0xa100, soundlatch_w),
                new MemoryWriteAddress(0xa130, 0xa130, MWA_RAM, rallyx_scrollx),
                new MemoryWriteAddress(0xa140, 0xa140, MWA_RAM, rallyx_scrolly),
                new MemoryWriteAddress(0xa170, 0xa170, MWA_NOP), /* ????? */
                new MemoryWriteAddress(0xa180, 0xa180, timeplt_sh_irqtrigger_w),
                new MemoryWriteAddress(0xa181, 0xa181, interrupt_enable_w),
                //	new MemoryWriteAddress( 0xa182, 0xa182, MWA_NOP ),	sound mute
                new MemoryWriteAddress(0xa183, 0xa183, rallyx_flipscreen_w),
                new MemoryWriteAddress(0xa184, 0xa184, coin_1_w),
                new MemoryWriteAddress(0xa186, 0xa186, coin_2_w),
                //	new MemoryWriteAddress( 0xa187, 0xa187, MWA_NOP ),	stars enable
                new MemoryWriteAddress(0x8014, 0x801f, MWA_RAM, spriteram, spriteram_size), /* these are here just to initialize */
                new MemoryWriteAddress(0x8814, 0x881f, MWA_RAM, spriteram_2), /* the pointers. */
                new MemoryWriteAddress(0x8034, 0x803f, MWA_RAM, rallyx_radarx, rallyx_radarram_size), /* ditto */
                new MemoryWriteAddress(0x8834, 0x883f, MWA_RAM, rallyx_radary),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8400, 0x87ff, rallyx_videoram2_w, rallyx_videoram2),
                new MemoryWriteAddress(0x8800, 0x8bff, colorram_w, colorram),
                new MemoryWriteAddress(0x8c00, 0x8fff, rallyx_colorram2_w, rallyx_colorram2),
                new MemoryWriteAddress(0x9800, 0x9fff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa00f, MWA_RAM, rallyx_radarattr),
                new MemoryWriteAddress(0xa080, 0xa080, watchdog_reset_w),
                new MemoryWriteAddress(0xa100, 0xa100, soundlatch_w),
                new MemoryWriteAddress(0xa130, 0xa130, MWA_RAM, rallyx_scrollx),
                new MemoryWriteAddress(0xa140, 0xa140, MWA_RAM, rallyx_scrolly),
                new MemoryWriteAddress(0xa170, 0xa170, MWA_NOP), /* ????? */
                new MemoryWriteAddress(0xa180, 0xa180, timeplt_sh_irqtrigger_w),
                new MemoryWriteAddress(0xa181, 0xa181, interrupt_enable_w),
                //	new MemoryWriteAddress( 0xa182, 0xa182, MWA_NOP ),	sound mute
                new MemoryWriteAddress(0xa183, 0xa183, rallyx_flipscreen_w),
                new MemoryWriteAddress(0xa184, 0xa184, coin_1_w),
                new MemoryWriteAddress(0xa186, 0xa186, coin_2_w),
                //	new MemoryWriteAddress( 0xa187, 0xa187, MWA_NOP ),	stars enable
                new MemoryWriteAddress(0x8000, 0x801f, MWA_RAM, spriteram, spriteram_size), /* these are here just to initialize */
                new MemoryWriteAddress(0x8800, 0x881f, MWA_RAM, spriteram_2), /* the pointers. */
                new MemoryWriteAddress(0x8020, 0x803f, MWA_RAM, rallyx_radarx, rallyx_radarram_size), /* ditto */
                new MemoryWriteAddress(0x8820, 0x883f, MWA_RAM, rallyx_radary),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_locomotn = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Disabled");
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_jungler = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* DSW0 */

            PORT_BIT(0x7f, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_BITX(0x80, 0x80, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_commsega = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x1c, 0x1c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x14, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x1c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters (256 in Jungler) */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites (64 in Jungler) */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3,
                24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3, 16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxLayout dotlayout = new GfxLayout(
            4, 4, /* 4*4 characters */
            8, /* 8 characters */
            2, /* 2 bits per pixel */
            new int[]{6, 7},
            new int[]{3 * 8, 2 * 8, 1 * 8, 0 * 8},
            new int[]{3 * 32, 2 * 32, 1 * 32, 0 * 32},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, dotlayout, 64 * 4, 1),
                new GfxDecodeInfo(-1) /* end of array */};

    public static MachineDriver machine_driver_locomotn = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
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
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4 + 4,
            locomotn_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            rallyx_vh_start,
            rallyx_vh_stop,
            locomotn_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        timeplt_ay8910_interface
                )
            }
    );
    public static MachineDriver machine_driver_commsega = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
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
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4 + 4,
            locomotn_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            rallyx_vh_start,
            rallyx_vh_stop,
            commsega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        timeplt_ay8910_interface
                )
            }
    );
    public static MachineDriver machine_driver_jungler = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        readmem, jungler_writemem, null, null,
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
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4 + 4,
            locomotn_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            rallyx_vh_start,
            rallyx_vh_stop,
            jungler_vh_screenrefresh,
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
    static RomLoadPtr rom_locomotn = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("1a.cpu", 0x0000, 0x1000, 0xb43e689a);
            ROM_LOAD("2a.cpu", 0x1000, 0x1000, 0x529c823d);
            ROM_LOAD("3.cpu", 0x2000, 0x1000, 0xc9dbfbd1);
            ROM_LOAD("4.cpu", 0x3000, 0x1000, 0xcaf6431c);
            ROM_LOAD("5.cpu", 0x4000, 0x1000, 0x64cf8dd6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1b_s1.bin", 0x0000, 0x1000, 0xa1105714);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5l_c1.bin", 0x0000, 0x1000, 0x5732eda9);
            ROM_LOAD("c2.cpu", 0x1000, 0x1000, 0xc3035300);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("10g.bpr", 0x0000, 0x0100, 0x2ef89356);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("8b.bpr", 0x0000, 0x0020, 0x75b05da0);/* palette */

            ROM_LOAD("9d.bpr", 0x0020, 0x0100, 0xaa6cf063);/* loookup table */

            ROM_LOAD("7a.bpr", 0x0120, 0x0020, 0x48c8f094);/* video layout (not used) */

            ROM_LOAD("10a.bpr", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    static RomLoadPtr rom_gutangtn = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("3d_1.bin", 0x0000, 0x1000, 0xe9757395);
            ROM_LOAD("3e_2.bin", 0x1000, 0x1000, 0x11d21d2e);
            ROM_LOAD("3f_3.bin", 0x2000, 0x1000, 0x4d80f895);
            ROM_LOAD("3h_4.bin", 0x3000, 0x1000, 0xaa258ddf);
            ROM_LOAD("3j_5.bin", 0x4000, 0x1000, 0x52aec87e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1b_s1.bin", 0x0000, 0x1000, 0xa1105714);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5l_c1.bin", 0x0000, 0x1000, 0x5732eda9);
            ROM_LOAD("5m_c2.bin", 0x1000, 0x1000, 0x51c542fd);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("10g.bpr", 0x0000, 0x0100, 0x2ef89356);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("8b.bpr", 0x0000, 0x0020, 0x75b05da0);/* palette */

            ROM_LOAD("9d.bpr", 0x0020, 0x0100, 0xaa6cf063);/* loookup table */

            ROM_LOAD("7a.bpr", 0x0120, 0x0020, 0x48c8f094);/* video layout (not used) */

            ROM_LOAD("10a.bpr", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    static RomLoadPtr rom_cottong = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("c1", 0x0000, 0x1000, 0x2c256fe6);
            ROM_LOAD("c2", 0x1000, 0x1000, 0x1de5e6a0);
            ROM_LOAD("c3", 0x2000, 0x1000, 0x01f909fe);
            ROM_LOAD("c4", 0x3000, 0x1000, 0xa89eb3e3);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("c7", 0x0000, 0x1000, 0x3d83f6d3);
            ROM_LOAD("c8", 0x1000, 0x1000, 0x323e1937);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("c5", 0x0000, 0x1000, 0x992d079c);
            ROM_LOAD("c6", 0x1000, 0x1000, 0x0149ef46);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5.bpr", 0x0000, 0x0100, 0x21fb583f);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("2.bpr", 0x0000, 0x0020, 0x26f42e6f);/* palette */

            ROM_LOAD("3.bpr", 0x0020, 0x0100, 0x4aecc0c8);/* loookup table */

            ROM_LOAD("7a.bpr", 0x0120, 0x0020, 0x48c8f094);/* video layout (not used) */

            ROM_LOAD("10a.bpr", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    static RomLoadPtr rom_jungler = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("jungr1", 0x0000, 0x1000, 0x5bd6ad15);
            ROM_LOAD("jungr2", 0x1000, 0x1000, 0xdc99f1e3);
            ROM_LOAD("jungr3", 0x2000, 0x1000, 0x3dcc03da);
            ROM_LOAD("jungr4", 0x3000, 0x1000, 0xf92e9940);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1b", 0x0000, 0x1000, 0xf86999c3);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5k", 0x0000, 0x0800, 0x924262bf);
            ROM_LOAD("5m", 0x0800, 0x0800, 0x131a08ac);
            /* 1000-1fff empty for my convenience */

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("82s129.10g", 0x0000, 0x0100, 0x2ef89356);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("18s030.8b", 0x0000, 0x0020, 0x55a7e6d1);/* palette */

            ROM_LOAD("tbp24s10.9d", 0x0020, 0x0100, 0xd223f7b8);/* loookup table */

            ROM_LOAD("18s030.7a", 0x0120, 0x0020, 0x8f574815);/* video layout (not used) */

            ROM_LOAD("6331-1.10a", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    static RomLoadPtr rom_junglers = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("5c", 0x0000, 0x1000, 0xedd71b28);
            ROM_LOAD("5a", 0x1000, 0x1000, 0x61ea4d46);
            ROM_LOAD("4d", 0x2000, 0x1000, 0x557c7925);
            ROM_LOAD("4c", 0x3000, 0x1000, 0x51aac9a5);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1b", 0x0000, 0x1000, 0xf86999c3);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5k", 0x0000, 0x0800, 0x924262bf);
            ROM_LOAD("5m", 0x0800, 0x0800, 0x131a08ac);
            /* 1000-1fff empty for my convenience */

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("82s129.10g", 0x0000, 0x0100, 0x2ef89356);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("18s030.8b", 0x0000, 0x0020, 0x55a7e6d1);/* palette */

            ROM_LOAD("tbp24s10.9d", 0x0020, 0x0100, 0xd223f7b8);/* loookup table */

            ROM_LOAD("18s030.7a", 0x0120, 0x0020, 0x8f574815);/* video layout (not used) */

            ROM_LOAD("6331-1.10a", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    static RomLoadPtr rom_commsega = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("csega1", 0x0000, 0x1000, 0x92de3405);
            ROM_LOAD("csega2", 0x1000, 0x1000, 0xf14e2f9a);
            ROM_LOAD("csega3", 0x2000, 0x1000, 0x941dbf48);
            ROM_LOAD("csega4", 0x3000, 0x1000, 0xe0ac69b4);
            ROM_LOAD("csega5", 0x4000, 0x1000, 0xbc56ebd0);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("csega8", 0x0000, 0x1000, 0x588b4210);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("csega7", 0x0000, 0x1000, 0xe8e374f9);
            ROM_LOAD("csega6", 0x1000, 0x1000, 0xcf07fd5e);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gg3.bpr", 0x0000, 0x0100, 0xae7fd962);/* dots */

            ROM_REGION(0x0160, REGION_PROMS);
            ROM_LOAD("gg1.bpr", 0x0000, 0x0020, 0xf69e585a);/* palette */

            ROM_LOAD("gg2.bpr", 0x0020, 0x0100, 0x0b756e30);/* loookup table */

            ROM_LOAD("gg0.bpr", 0x0120, 0x0020, 0x48c8f094);/* video layout (not used) */

            ROM_LOAD("tt3.bpr", 0x0140, 0x0020, 0xb8861096);/* video timing (not used) */

            ROM_END();
        }
    };

    public static GameDriver driver_locomotn = new GameDriver("1982", "locomotn", "locomotn.java", rom_locomotn, null, machine_driver_locomotn, input_ports_locomotn, null, ROT90, "Konami (Centuri license)", "Loco-Motion");
    public static GameDriver driver_gutangtn = new GameDriver("1982", "gutangtn", "locomotn.java", rom_gutangtn, driver_locomotn, machine_driver_locomotn, input_ports_locomotn, null, ROT90, "Konami (Sega license)", "Guttang Gottong");
    public static GameDriver driver_cottong = new GameDriver("1982", "cottong", "locomotn.java", rom_cottong, driver_locomotn, machine_driver_locomotn, input_ports_locomotn, null, ROT90, "bootleg", "Cotocoto Cottong");
    public static GameDriver driver_jungler = new GameDriver("1981", "jungler", "locomotn.java", rom_jungler, null, machine_driver_jungler, input_ports_jungler, null, ROT90, "Konami", "Jungler");
    public static GameDriver driver_junglers = new GameDriver("1981", "junglers", "locomotn.java", rom_junglers, driver_jungler, machine_driver_jungler, input_ports_jungler, null, ROT90, "[Konami] (Stern license)", "Jungler (Stern)");
    public static GameDriver driver_commsega = new GameDriver("1983", "commsega", "locomotn.java", rom_commsega, null, machine_driver_commsega, input_ports_commsega, null, ROT90, "Sega", "Commando (Sega)");
}
