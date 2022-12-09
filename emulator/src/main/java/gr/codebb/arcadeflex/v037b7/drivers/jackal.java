/*
 * ported to v0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.jackal.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.machine.jackal.*;
import static arcadeflex.v036.mame.inputH.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2151;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;

public class jackal {

    public static ReadHandlerPtr rotary_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (1 << (readinputport(6) * 8 / 256)) ^ 0xff;
        }
    };

    public static ReadHandlerPtr rotary_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (1 << (readinputport(7) * 8 / 256)) ^ 0xff;
        }
    };

    static char intenable;

    public static WriteHandlerPtr jackal_interrupt_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            intenable = (char) data;
        }
    };

    public static InterruptPtr jackal_interrupt = new InterruptPtr() {
        public int handler() {
            if ((intenable & 0x02) != 0) {
                return nmi_interrupt.handler();
            }
            if ((intenable & 0x08) != 0) {
                return M6809_INT_IRQ;
            }
            if ((intenable & 0x10) != 0) {
                return M6809_INT_FIRQ;
            }
            return ignore_interrupt.handler();
        }
    };

    static MemoryReadAddress jackal_readmem[]
            = {
                new MemoryReadAddress(0x0010, 0x0010, input_port_0_r),
                new MemoryReadAddress(0x0011, 0x0011, input_port_1_r),
                new MemoryReadAddress(0x0012, 0x0012, input_port_2_r),
                new MemoryReadAddress(0x0013, 0x0013, input_port_3_r),
                new MemoryReadAddress(0x0014, 0x0014, rotary_0_r),
                new MemoryReadAddress(0x0015, 0x0015, rotary_1_r),
                new MemoryReadAddress(0x0018, 0x0018, input_port_4_r),
                new MemoryReadAddress(0x0020, 0x005f, jackal_zram_r), /* MAIN   Z RAM,SUB    Z RAM */
                new MemoryReadAddress(0x0060, 0x1fff, jackal_commonram_r), /* M COMMON RAM,S COMMON RAM */
                new MemoryReadAddress(0x2000, 0x2fff, jackal_voram_r), /* MAIN V O RAM,SUB  V O RAM */
                new MemoryReadAddress(0x3000, 0x3fff, jackal_spriteram_r), /* MAIN V O RAM,SUB  V O RAM */
                new MemoryReadAddress(0x4000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress jackal_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0003, MWA_RAM, jackal_videoctrl), /* scroll + other things */
                new MemoryWriteAddress(0x0004, 0x0004, jackal_interrupt_enable_w),
                new MemoryWriteAddress(0x0019, 0x0019, MWA_NOP), /* possibly watchdog reset */
                new MemoryWriteAddress(0x001c, 0x001c, jackal_rambank_w),
                new MemoryWriteAddress(0x0020, 0x005f, jackal_zram_w),
                new MemoryWriteAddress(0x0060, 0x1fff, jackal_commonram_w),
                new MemoryWriteAddress(0x2000, 0x2fff, jackal_voram_w),
                new MemoryWriteAddress(0x3000, 0x3fff, jackal_spriteram_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress jackal_sound_readmem[]
            = {
                new MemoryReadAddress(0x2001, 0x2001, YM2151_status_port_0_r),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM), /* COLOR RAM (Self test only check 0x4000-0x423f */
                new MemoryReadAddress(0x6000, 0x605f, MRA_RAM), /* SOUND RAM (Self test check 0x6000-605f, 0x7c00-0x7fff */
                new MemoryReadAddress(0x6060, 0x7fff, jackal_commonram1_r), /* COMMON RAM */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress jackal_sound_writemem[]
            = {
                new MemoryWriteAddress(0x2000, 0x2000, YM2151_register_port_0_w),
                new MemoryWriteAddress(0x2001, 0x2001, YM2151_data_port_0_w),
                new MemoryWriteAddress(0x4000, 0x43ff, paletteram_xBBBBBGGGGGRRRRR_w, paletteram),
                new MemoryWriteAddress(0x6000, 0x605f, MWA_RAM),
                new MemoryWriteAddress(0x6060, 0x7fff, jackal_commonram1_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_jackal = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW1 */

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
            PORT_DIPSETTING(0x00, "Invalid");

            PORT_START(); 	/* IN1 */
            /* note that button 3 for player 1 and 2 are exchanged */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30000 150000");
            PORT_DIPSETTING(0x10, "50000 200000");
            PORT_DIPSETTING(0x08, "30000");
            PORT_DIPSETTING(0x00, "50000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW3 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, "Sound Mode");
            PORT_DIPSETTING(0x04, "Mono");
            PORT_DIPSETTING(0x00, "Stereo");
            PORT_DIPNAME(0x08, 0x00, "Sound Adj");
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));

            /* the rotary controls work in topgunbl only */
            PORT_START(); 	/* player 1 8-way rotary control - converted in rotary_0_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL, 25, 10, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0);

            PORT_START(); 	/* player 2 8-way rotary control - converted in rotary_1_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_PLAYER2, 25, 10, 0, 0, KEYCODE_N, KEYCODE_M, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            4096, /* 4096 characters */
            8, /* 8 bits per pixel (!) */
            new int[]{0, 1, 2, 3, 0x20000 * 8 + 0, 0x20000 * 8 + 1, 0x20000 * 8 + 2, 0x20000 * 8 + 3},
            new int[]{0 * 4, 1 * 4, 0x40000 * 8 + 0 * 4, 0x40000 * 8 + 1 * 4, 2 * 4, 3 * 4, 0x40000 * 8 + 2 * 4, 0x40000 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 0x40000 * 8 + 0 * 4, 0x40000 * 8 + 1 * 4, 2 * 4, 3 * 4, 0x40000 * 8 + 2 * 4, 0x40000 * 8 + 3 * 4,
                16 * 8 + 0 * 4, 16 * 8 + 1 * 4, 16 * 8 + 0x40000 * 8 + 0 * 4, 16 * 8 + 0x40000 * 8 + 1 * 4, 16 * 8 + 2 * 4, 16 * 8 + 3 * 4, 16 * 8 + 0x40000 * 8 + 2 * 4, 16 * 8 + 0x40000 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxLayout spritelayout8 = new GfxLayout(
            8, 8, /* 8*8 characters */
            4096, /* 4096 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 0x40000 * 8 + 0 * 4, 0x40000 * 8 + 1 * 4, 2 * 4, 3 * 4, 0x40000 * 8 + 2 * 4, 0x40000 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout topgunbl_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            4096, /* 4096 characters */
            8, /* 8 bits per pixel (!) */
            new int[]{0, 1, 2, 3, 0x20000 * 8 + 0, 0x20000 * 8 + 1, 0x20000 * 8 + 2, 0x20000 * 8 + 3},
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout topgunbl_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4,
                32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout topgunbl_spritelayout8 = new GfxLayout(
            8, 8, /* 8*8 characters */
            4096, /* 4096 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxDecodeInfo jackal_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 256, 1), /* colors 256-511 */
                new GfxDecodeInfo(REGION_GFX1, 0x10000, spritelayout, 512, 16), /* colors   0- 15 with lookup */
                new GfxDecodeInfo(REGION_GFX1, 0x30000, spritelayout, 512 + 16 * 16, 16), /* colors  16- 31 with lookup */
                new GfxDecodeInfo(REGION_GFX1, 0x10000, spritelayout8, 512, 16), /* to handle 8x8 sprites */
                new GfxDecodeInfo(REGION_GFX1, 0x30000, spritelayout8, 512 + 16 * 16, 16), /* to handle 8x8 sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo topgunbl_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, topgunbl_charlayout, 256, 1), /* colors 256-511 */
                new GfxDecodeInfo(REGION_GFX1, 0x40000, topgunbl_spritelayout, 512, 16), /* colors   0- 15 with lookup */
                new GfxDecodeInfo(REGION_GFX1, 0x60000, topgunbl_spritelayout, 512 + 16 * 16, 16), /* colors  16- 31 with lookup */
                new GfxDecodeInfo(REGION_GFX1, 0x40000, topgunbl_spritelayout8, 512, 16), /* to handle 8x8 sprites */
                new GfxDecodeInfo(REGION_GFX1, 0x60000, topgunbl_spritelayout8, 512 + 16 * 16, 16), /* to handle 8x8 sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2151interface ym2151_interface = new YM2151interface(
            1,
            3580000,
            new int[]{YM3012_VOL(50, MIXER_PAN_LEFT, 50, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_jackal = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* 2 MHz???? */
                        jackal_readmem, jackal_writemem, null, null,
                        jackal_interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* 2 MHz???? */
                        jackal_sound_readmem, jackal_sound_writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10, /* 10 CPU slices per frame - seems enough to keep the CPUs in sync */
            jackal_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 2 * 8, 30 * 8 - 1),
            jackal_gfxdecodeinfo,
            512, 512 + 16 * 16 + 16 * 16,
            jackal_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            jackal_vh_start,
            jackal_vh_stop,
            jackal_vh_screenrefresh,
            /* sound hardware */
            SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                )
            }
    );

    /* identical but different gfxdecode */
    static MachineDriver machine_driver_topgunbl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* 2 MHz???? */
                        jackal_readmem, jackal_writemem, null, null,
                        jackal_interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* 2 MHz???? */
                        jackal_sound_readmem, jackal_sound_writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10, /* 10 CPU slices per frame - seems enough to keep the CPUs in sync */
            jackal_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 2 * 8, 30 * 8 - 1),
            topgunbl_gfxdecodeinfo,
            512, 512,
            jackal_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            jackal_vh_start,
            jackal_vh_stop,
            jackal_vh_screenrefresh,
            /* sound hardware */
            SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
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
    static RomLoadPtr rom_jackal = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* Banked 64k for 1st CPU */

            ROM_LOAD("j-v02.rom", 0x04000, 0x8000, 0x0b7e0584);
            ROM_CONTINUE(0x14000, 0x8000);
            ROM_LOAD("j-v03.rom", 0x0c000, 0x4000, 0x3e0dfb83);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for 2nd cpu (Graphics & Sound)*/

            ROM_LOAD("631t01.bin", 0x8000, 0x8000, 0xb189af6a);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("631t04.bin", 0x00000, 0x20000, 0x457f42f0);
            ROM_LOAD("631t06.bin", 0x20000, 0x20000, 0x2d10e56e);
            ROM_LOAD("631t05.bin", 0x40000, 0x20000, 0x732b3fc1);
            ROM_LOAD("631t07.bin", 0x60000, 0x20000, 0x4961c397);

            ROM_REGION(0x0200, REGION_PROMS);/* color lookup tables */

            ROM_LOAD("631r08.bpr", 0x0000, 0x0100, 0x7553a172);
            ROM_LOAD("631r09.bpr", 0x0100, 0x0100, 0xa74dd86c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_topgunr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* Banked 64k for 1st CPU */

            ROM_LOAD("tgnr15d.bin", 0x04000, 0x8000, 0xf7e28426);
            ROM_CONTINUE(0x14000, 0x8000);
            ROM_LOAD("tgnr16d.bin", 0x0c000, 0x4000, 0xc086844e);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for 2nd cpu (Graphics & Sound)*/

            ROM_LOAD("631t01.bin", 0x8000, 0x8000, 0xb189af6a);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tgnr7h.bin", 0x00000, 0x20000, 0x50122a12);
            ROM_LOAD("tgnr12h.bin", 0x20000, 0x20000, 0x37dbbdb0);
            ROM_LOAD("tgnr8h.bin", 0x40000, 0x20000, 0x6943b1a4);
            ROM_LOAD("tgnr13h.bin", 0x60000, 0x20000, 0x22effcc8);

            ROM_REGION(0x0200, REGION_PROMS);/* color lookup tables */

            ROM_LOAD("631r08.bpr", 0x0000, 0x0100, 0x7553a172);
            ROM_LOAD("631r09.bpr", 0x0100, 0x0100, 0xa74dd86c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_jackalj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* Banked 64k for 1st CPU */

            ROM_LOAD("631t02.bin", 0x04000, 0x8000, 0x14db6b1a);
            ROM_CONTINUE(0x14000, 0x8000);
            ROM_LOAD("631t03.bin", 0x0c000, 0x4000, 0xfd5f9624);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for 2nd cpu (Graphics & Sound)*/

            ROM_LOAD("631t01.bin", 0x8000, 0x8000, 0xb189af6a);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("631t04.bin", 0x00000, 0x20000, 0x457f42f0);
            ROM_LOAD("631t06.bin", 0x20000, 0x20000, 0x2d10e56e);
            ROM_LOAD("631t05.bin", 0x40000, 0x20000, 0x732b3fc1);
            ROM_LOAD("631t07.bin", 0x60000, 0x20000, 0x4961c397);

            ROM_REGION(0x0200, REGION_PROMS);/* color lookup tables */

            ROM_LOAD("631r08.bpr", 0x0000, 0x0100, 0x7553a172);
            ROM_LOAD("631r09.bpr", 0x0100, 0x0100, 0xa74dd86c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_topgunbl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* Banked 64k for 1st CPU */

            ROM_LOAD("t-3.c5", 0x04000, 0x8000, 0x7826ad38);
            ROM_LOAD("t-4.c4", 0x14000, 0x8000, 0x976c8431);
            ROM_LOAD("t-2.c6", 0x0c000, 0x4000, 0xd53172e5);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for 2nd cpu (Graphics & Sound)*/

            ROM_LOAD("t-1.c14", 0x8000, 0x8000, 0x54aa2d29);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("t-17.n12", 0x00000, 0x08000, 0xe8875110);
            ROM_LOAD("t-18.n13", 0x08000, 0x08000, 0xcf14471d);
            ROM_LOAD("t-19.n14", 0x10000, 0x08000, 0x46ee5dd2);
            ROM_LOAD("t-20.n15", 0x18000, 0x08000, 0x3f472344);
            ROM_LOAD("t-13.n8", 0x20000, 0x08000, 0x5d669abb);
            ROM_LOAD("t-14.n9", 0x28000, 0x08000, 0xf349369b);
            ROM_LOAD("t-15.n10", 0x30000, 0x08000, 0x7c5a91dd);
            ROM_LOAD("t-16.n11", 0x38000, 0x08000, 0x5ec46d8e);
            ROM_LOAD("t-6.n1", 0x40000, 0x08000, 0x539cc48c);
            ROM_LOAD("t-5.m1", 0x48000, 0x08000, 0x2dd9a5e9);
            ROM_LOAD("t-7.n2", 0x50000, 0x08000, 0x0ecd31b1);
            ROM_LOAD("t-8.n3", 0x58000, 0x08000, 0xf946ada7);
            ROM_LOAD("t-9.n4", 0x60000, 0x08000, 0x8269caca);
            ROM_LOAD("t-10.n5", 0x68000, 0x08000, 0x25393e4f);
            ROM_LOAD("t-11.n6", 0x70000, 0x08000, 0x7895c22d);
            ROM_LOAD("t-12.n7", 0x78000, 0x08000, 0x15606dfc);

            ROM_REGION(0x0200, REGION_PROMS);/* color lookup tables */

            ROM_LOAD("631r08.bpr", 0x0000, 0x0100, 0x7553a172);
            ROM_LOAD("631r09.bpr", 0x0100, 0x0100, 0xa74dd86c);
            ROM_END();
        }
    };

    public static GameDriver driver_jackal = new GameDriver("1986", "jackal", "jackal.java", rom_jackal, null, machine_driver_jackal, input_ports_jackal, null, ROT90, "Konami", "Jackal (World)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
    public static GameDriver driver_topgunr = new GameDriver("1986", "topgunr", "jackal.java", rom_topgunr, driver_jackal, machine_driver_jackal, input_ports_jackal, null, ROT90, "Konami", "Top Gunner (US)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
    public static GameDriver driver_jackalj = new GameDriver("1986", "jackalj", "jackal.java", rom_jackalj, driver_jackal, machine_driver_jackal, input_ports_jackal, null, ROT90, "Konami", "Tokushu Butai Jackal (Japan)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
    public static GameDriver driver_topgunbl = new GameDriver("1987", "topgunbl", "jackal.java", rom_topgunbl, driver_jackal, machine_driver_topgunbl, input_ports_jackal, null, ROT90, "bootleg", "Top Gunner (bootleg)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
}
