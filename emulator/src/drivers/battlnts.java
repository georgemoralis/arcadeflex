/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package drivers;

import static platform.ptrlib.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static mame.cpuintrf.*;
import static mame.inputportH.*;
import static mame.sndintrf.*;
import static cpu.m6809.m6809H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.sndintrfH.*;
import static vidhrdw.konamiic.*;
import static vidhrdw.konami.K007342.*;
import static vidhrdw.konami.K007420.*;
import static vidhrdw.battlnts.*;
import static sound._3812intf.*;
import static sound._3812intfH.*;

public class battlnts {

    public static InterruptPtr battlnts_interrupt = new InterruptPtr() {
        public int handler() {
            if (K007342_is_INT_enabled() != 0) {
                return HD6309_INT_IRQ;
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr battlnts_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(1, 0xff);
        }
    };

    public static WriteHandlerPtr battlnts_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int bankaddress;

            /* bits 6 & 7 = bank number */
            bankaddress = 0x10000 + ((data & 0xc0) >> 6) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* bits 4 & 5 = coin counters */
            coin_counter_w.handler(0, data & 0x10);
            coin_counter_w.handler(1, data & 0x20);

            /* other bits unknown */
        }
    };

    static MemoryReadAddress battlnts_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, K007342_r), /* Color RAM + Video RAM */
                new MemoryReadAddress(0x2000, 0x21ff, K007420_r), /* Sprite RAM */
                new MemoryReadAddress(0x2200, 0x23ff, K007342_scroll_r), /* Scroll RAM */
                new MemoryReadAddress(0x2400, 0x24ff, paletteram_r), /* Palette */
                new MemoryReadAddress(0x2e00, 0x2e00, input_port_0_r), /* DIPSW #1 */
                new MemoryReadAddress(0x2e01, 0x2e01, input_port_4_r), /* 2P controls */
                new MemoryReadAddress(0x2e02, 0x2e02, input_port_3_r), /* 1P controls */
                new MemoryReadAddress(0x2e03, 0x2e03, input_port_2_r), /* coinsw, testsw, startsw */
                new MemoryReadAddress(0x2e04, 0x2e04, input_port_1_r), /* DISPW #2 */
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK1), /* banked ROM */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM), /* ROM 777e02.bin */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress battlnts_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, K007342_w), /* Color RAM + Video RAM */
                new MemoryWriteAddress(0x2000, 0x21ff, K007420_w), /* Sprite RAM */
                new MemoryWriteAddress(0x2200, 0x23ff, K007342_scroll_w), /* Scroll RAM */
                new MemoryWriteAddress(0x2400, 0x24ff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram),/* palette */
                new MemoryWriteAddress(0x2600, 0x2607, K007342_vreg_w), /* Video Registers */
                new MemoryWriteAddress(0x2e08, 0x2e08, battlnts_bankswitch_w), /* bankswitch control */
                new MemoryWriteAddress(0x2e0c, 0x2e0c, battlnts_spritebank_w), /* sprite bank select */
                new MemoryWriteAddress(0x2e10, 0x2e10, watchdog_reset_w), /* watchdog reset */
                new MemoryWriteAddress(0x2e14, 0x2e14, soundlatch_w), /* sound code # */
                new MemoryWriteAddress(0x2e18, 0x2e18, battlnts_sh_irqtrigger_w),/* cause interrupt on audio CPU */
                new MemoryWriteAddress(0x4000, 0x7fff, MWA_ROM), /* banked ROM */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM), /* ROM 777e02.bin */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress battlnts_readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM), /* ROM 777c01.rom */
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0xa000, 0xa000, YM3812_status_port_0_r), /* YM3812 (chip 1) */
                new MemoryReadAddress(0xc000, 0xc000, YM3812_status_port_1_r), /* YM3812 (chip 2) */
                new MemoryReadAddress(0xe000, 0xe000, soundlatch_r), /* soundlatch_r */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress battlnts_writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM), /* ROM 777c01.rom */
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM), /* RAM */
                new MemoryWriteAddress(0xa000, 0xa000, YM3812_control_port_0_w), /* YM3812 (chip 1) */
                new MemoryWriteAddress(0xa001, 0xa001, YM3812_write_port_0_w), /* YM3812 (chip 1) */
                new MemoryWriteAddress(0xc000, 0xc000, YM3812_control_port_1_w), /* YM3812 (chip 2) */
                new MemoryWriteAddress(0xc001, 0xc001, YM3812_write_port_1_w), /* YM3812 (chip 2) */
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * Input Ports
     *
     **************************************************************************
     */
    static InputPortPtr input_ports_battlnts = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW #1 */

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
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START(); 	/* DSW #2 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30k and every 70k");
            PORT_DIPSETTING(0x10, "40k and every 80k");
            PORT_DIPSETTING(0x08, "40k");
            PORT_DIPSETTING(0x00, "50k");
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* COINSW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, "Upright Controls");
            PORT_DIPSETTING(0x40, "Single");
            PORT_DIPSETTING(0x00, "Dual");
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START(); 	/* PLAYER 1 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x80, 0x80, "Continue limit");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x00, "5");

            PORT_START(); 	/* PLAYER 2 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_thehustj = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* DSW #1 */

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
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START(); 	/* DSW #2 */

            PORT_DIPNAME(0x03, 0x02, "Balls");
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* COINSW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START(); 	/* PLAYER 1 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* PLAYER 2 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8 x 8 characters */
            0x40000 / 32, /* 8192 characters */
            4, /* 4bpp */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every character takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            8, 8, /* 8*8 sprites */
            0x40000 / 32, /* 8192 sprites */
            4, /* 4 bpp */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 1), /* colors  0-15 */
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 4 * 16, 1), /* colors 64-79 */
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * *************************************************************************
     *
     * Machine Driver
     *
     **************************************************************************
     */
    static YM3812interface ym3812_interface = new YM3812interface(
            2, /* 2 chips */
            3000000, /* ? */
            new int[]{50, 50},
            new WriteYmHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_battlnts = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309,
                        3000000, /* ? */
                        battlnts_readmem, battlnts_writemem, null, null,
                        battlnts_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545, /* ? */
                        battlnts_readmem_sound, battlnts_writemem_sound, null, null,
                        ignore_interrupt, 0 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            128, 128,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            battlnts_vh_start,
            battlnts_vh_stop,
            battlnts_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                )
            }
    );

    /**
     * *************************************************************************
     *
     * Game ROMs
     *
     **************************************************************************
     */
    static RomLoadPtr rom_battlnts = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("g02.7e", 0x08000, 0x08000, 0xdbd8e17e);/* fixed ROM */

            ROM_LOAD("g03.8e", 0x10000, 0x10000, 0x7bd44fef);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("777c01.bin", 0x00000, 0x08000, 0xc21206e9);

            ROM_REGION(0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("777c04.bin", 0x00000, 0x40000, 0x45d92347);/* tiles */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("777c05.bin", 0x00000, 0x40000, 0xaeee778c);/* sprites */

            ROM_END();
        }
    };

    static RomLoadPtr rom_battlntj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("777e02.bin", 0x08000, 0x08000, 0xd631cfcb);/* fixed ROM */

            ROM_LOAD("777e03.bin", 0x10000, 0x10000, 0x5ef1f4ef);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("777c01.bin", 0x00000, 0x08000, 0xc21206e9);

            ROM_REGION(0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("777c04.bin", 0x00000, 0x40000, 0x45d92347);/* tiles */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("777c05.bin", 0x00000, 0x40000, 0xaeee778c);/* sprites */

            ROM_END();
        }
    };

    static RomLoadPtr rom_thehustl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("765-m02.7e", 0x08000, 0x08000, 0x934807b9);/* fixed ROM */

            ROM_LOAD("765-j03.8e", 0x10000, 0x10000, 0xa13fd751);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("765-j01.10a", 0x00000, 0x08000, 0x77ae753e);

            ROM_REGION(0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("765-e04.13a", 0x00000, 0x40000, 0x08c2b72e);/* tiles */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("765-e05.13e", 0x00000, 0x40000, 0xef044655);/* sprites */

            ROM_END();
        }
    };

    static RomLoadPtr rom_thehustj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("765-j02.7e", 0x08000, 0x08000, 0x2ac14c75);/* fixed ROM */

            ROM_LOAD("765-j03.8e", 0x10000, 0x10000, 0xa13fd751);/* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("765-j01.10a", 0x00000, 0x08000, 0x77ae753e);

            ROM_REGION(0x40000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("765-e04.13a", 0x00000, 0x40000, 0x08c2b72e);/* tiles */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("765-e05.13e", 0x00000, 0x40000, 0xef044655);/* sprites */

            ROM_END();
        }
    };

    public static GameDriver driver_battlnts = new GameDriver("1987", "battlnts", "battlnts.java", rom_battlnts, null, machine_driver_battlnts, input_ports_battlnts, null, ROT90, "Konami", "Battlantis");
    public static GameDriver driver_battlntj = new GameDriver("1987", "battlntj", "battlnts.java", rom_battlntj, driver_battlnts, machine_driver_battlnts, input_ports_battlnts, null, ROT90, "Konami", "Battlantis (Japan)");
    public static GameDriver driver_thehustl = new GameDriver("1987", "thehustl", "battlnts.java", rom_thehustl, null, machine_driver_battlnts, input_ports_thehustj, null, ROT90, "Konami", "The Hustler (Japan version M)");
    public static GameDriver driver_thehustj = new GameDriver("1987", "thehustj", "battlnts.java", rom_thehustj, driver_thehustl, machine_driver_battlnts, input_ports_thehustj, null, ROT90, "Konami", "The Hustler (Japan version J)");
}
