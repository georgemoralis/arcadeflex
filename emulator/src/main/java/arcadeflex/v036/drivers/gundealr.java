/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//sound imports
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.gundealr.*;
//TODO
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_write_handler;

public class gundealr {

    static int input_ports_hack;

    public static InterruptHandlerPtr yamyam_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                if (input_ports_hack != 0) {
                    UBytePtr RAM = memory_region(REGION_CPU1);
                    RAM.write(0xe004, readinputport(4));
                    /* COIN */

                    RAM.write(0xe005, readinputport(3));
                    /* IN1 */

                    RAM.write(0xe006, readinputport(2));
                    /* IN0 */

                }
                return 0xd7;
                /* RST 10h vblank */

            }
            if ((cpu_getiloops() & 1) == 1) {
                return 0xcf;
                /* RST 08h sound (hand tuned) */

            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr yamyam_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            bankaddress = 0x10000 + (data & 0x07) * 0x4000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));
        }
    };

    public static WriteHandlerPtr yamyam_protection_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            if (errorlog != null) {
                fprintf(errorlog, "e000 = %02x\n", RAM.read(0xe000));
            }
            RAM.write(0xe000, data);
            if (data == 0x03) {
                RAM.write(0xe001, 0x03);
            }
            if (data == 0x04) {
                RAM.write(0xe001, 0x04);
            }
            if (data == 0x05) {
                RAM.write(0xe001, 0x05);
            }
            if (data == 0x0a) {
                RAM.write(0xe001, 0x08);
            }
            if (data == 0x0d) {
                RAM.write(0xe001, 0x07);
            }

            if (data == 0x03) {
                RAM.write(0xe010, 0x3a);
                RAM.write(0xe011, 0x00);
                RAM.write(0xe012, 0xc0);
                RAM.write(0xe013, 0x47);
                RAM.write(0xe014, 0x3a);
                RAM.write(0xe015, 0x01);
                RAM.write(0xe016, 0xc0);
                RAM.write(0xe017, 0xc9);
            }
            if (data == 0x05) {
                RAM.write(0xe020, 0xc5);
                RAM.write(0xe021, 0x01);
                RAM.write(0xe022, 0x00);
                RAM.write(0xe023, 0x00);
                RAM.write(0xe024, 0x4f);
                RAM.write(0xe025, 0x09);
                RAM.write(0xe026, 0xc1);
                RAM.write(0xe027, 0xc9);

                RAM.write(0xe010, 0xcd);
                RAM.write(0xe011, 0x20);
                RAM.write(0xe012, 0xe0);
                RAM.write(0xe013, 0x7e);
                RAM.write(0xe014, 0xc9);
            }
        }
    };

    public static InitDriverHandlerPtr init_gundealr = new InitDriverHandlerPtr() {
        public void handler() {
            input_ports_hack = 0;
        }
    };

    public static InitDriverHandlerPtr init_yamyam = new InitDriverHandlerPtr() {
        public void handler() {
            input_ports_hack = 1;
            install_mem_write_handler(0, 0xe000, 0xe000, yamyam_protection_w);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xc000, input_port_0_r), /* DSW0 */
                new MemoryReadAddress(0xc001, 0xc001, input_port_1_r), /* DSW1 */
                new MemoryReadAddress(0xc004, 0xc004, input_port_2_r), /* COIN (Gun Dealer only) */
                new MemoryReadAddress(0xc005, 0xc005, input_port_3_r), /* IN1 (Gun Dealer only) */
                new MemoryReadAddress(0xc006, 0xc006, input_port_4_r), /* IN0 (Gun Dealer only) */
                new MemoryReadAddress(0xc400, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc010, 0xc013, yamyam_fg_scroll_w), /* Yam Yam only */
                new MemoryWriteAddress(0xc014, 0xc014, gundealr_flipscreen_w),
                new MemoryWriteAddress(0xc016, 0xc016, yamyam_bankswitch_w),
                new MemoryWriteAddress(0xc020, 0xc023, gundealr_fg_scroll_w), /* Gun Dealer only */
                new MemoryWriteAddress(0xc400, 0xc7ff, gundealr_paletteram_w, paletteram),
                new MemoryWriteAddress(0xc800, 0xcfff, gundealr_bg_videoram_w, gundealr_bg_videoram),
                new MemoryWriteAddress(0xd000, 0xdfff, gundealr_fg_videoram_w, gundealr_fg_videoram),
                new MemoryWriteAddress(0xe000, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x01, 0x01, YM2203_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM2203_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM2203_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_gundealr = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x40, "1");
            PORT_DIPSETTING(0x80, "2");
            PORT_DIPSETTING(0xc0, "3");

            PORT_START();
            /* COIN */

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

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_yamyam = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, "Difficulty?");
            PORT_DIPSETTING(0x00, "Easy?");
            PORT_DIPSETTING(0x04, "Medium?");
            PORT_DIPSETTING(0x08, "Hard?");
            PORT_DIPSETTING(0x0c, "Hardest?");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            /*	PORT_DIPSETTING(    0x03, DEF_STR( "1C_1C") ); */
 /*	PORT_DIPSETTING(    0x02, DEF_STR( "1C_1C") ); */
 /*	PORT_DIPSETTING(    0x01, DEF_STR( "1C_1C") ); */
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x38, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x38, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x28, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            /*	PORT_DIPSETTING(    0x18, DEF_STR( "1C_1C") ); */
 /*	PORT_DIPSETTING(    0x10, DEF_STR( "1C_1C") ); */
 /*	PORT_DIPSETTING(    0x08, DEF_STR( "1C_1C") ); */
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* COIN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_TILT);/* "TEST" */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                16 * 32 + 0 * 4, 16 * 32 + 1 * 4, 16 * 32 + 2 * 4, 16 * 32 + 3 * 4, 16 * 32 + 4 * 4, 16 * 32 + 5 * 4, 16 * 32 + 6 * 4, 16 * 32 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* colors 0-255 */
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 256, 16), /* colors 256-511 */
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 2 chips */
            1500000, /* 1.5 MHz ?????? */
            new int[]{YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_gundealr = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        8000000, /* 8 Mhz ??? */
                        readmem, writemem, readport, writeport,
                        yamyam_interrupt, 4 /* ? */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            512, 512,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            gundealr_vh_start,
            null,
            gundealr_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
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
    static RomLoadHandlerPtr rom_gundealr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* 64k for code + 128k for banks */

            ROM_LOAD("gundealr.1", 0x00000, 0x10000, 0x5797e830);
            ROM_RELOAD(0x10000, 0x10000);/* banked at 0x8000-0xbfff */

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gundealr.3", 0x00000, 0x10000, 0x01f99de2);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gundealr.2", 0x00000, 0x20000, 0x7874ec41);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_gundeala = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* 64k for code + 128k for banks */

            ROM_LOAD("gundeala.1", 0x00000, 0x10000, 0xd87e24f1);
            ROM_RELOAD(0x10000, 0x10000);/* banked at 0x8000-0xbfff */

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gundeala.3", 0x00000, 0x10000, 0x836cf1a3);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gundeala.2", 0x00000, 0x20000, 0x4b5fb53c);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_yamyam = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* 64k for code + 128k for banks */

            ROM_LOAD("b3.f10", 0x00000, 0x20000, 0x96ae9088);
            ROM_RELOAD(0x10000, 0x20000);/* banked at 0x8000-0xbfff */

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b2.d16", 0x00000, 0x10000, 0xcb4f84ee);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b1.a16", 0x00000, 0x20000, 0xb122828d);
            ROM_END();
        }
    };

    /* only gfx are different, code is the same */
    static RomLoadHandlerPtr rom_wiseguy = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* 64k for code + 128k for banks */

            ROM_LOAD("b3.f10", 0x00000, 0x20000, 0x96ae9088);
            ROM_RELOAD(0x10000, 0x20000);/* banked at 0x8000-0xbfff */

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("wguyb2.bin", 0x00000, 0x10000, 0x1c684c46);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b1.a16", 0x00000, 0x20000, 0xb122828d);
            ROM_END();
        }
    };

    public static GameDriver driver_gundealr = new GameDriver("1990", "gundealr", "gundealr.java", rom_gundealr, null, machine_driver_gundealr, input_ports_gundealr, init_gundealr, ROT270, "Dooyong", "Gun Dealer (set 1)");
    public static GameDriver driver_gundeala = new GameDriver("????", "gundeala", "gundealr.java", rom_gundeala, driver_gundealr, machine_driver_gundealr, input_ports_gundealr, init_gundealr, ROT270, "Dooyong", "Gun Dealer (set 2)");
    public static GameDriver driver_yamyam = new GameDriver("1990", "yamyam", "gundealr.java", rom_yamyam, null, machine_driver_gundealr, input_ports_yamyam, init_yamyam, ROT0, "Dooyong", "Yam! Yam!?");
    public static GameDriver driver_wiseguy = new GameDriver("1990", "wiseguy", "gundealr.java", rom_wiseguy, driver_yamyam, machine_driver_gundealr, input_ports_yamyam, init_yamyam, ROT0, "Dooyong", "Wise Guy");
}
