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
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.mnight.*;
import static gr.codebb.arcadeflex.v036.machine.lwings.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.adpcmH.*;
import static gr.codebb.arcadeflex.v036.sound.adpcm.*;
import static gr.codebb.arcadeflex.v036.sound._2203intf.*;
import static gr.codebb.arcadeflex.v036.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

public class mnight {

    static int mnight_bank_latch = 255, main_cpu_num;

    public static InitMachinePtr mnight_init_machine = new InitMachinePtr() {
        public void handler() {
            main_cpu_num = 0;
        }
    };

    public static InterruptPtr mnight_interrupt = new InterruptPtr() {
        public int handler() {
            return 0x00d7;	/* RST 10h */

        }
    };

    public static ReadHandlerPtr mnight_bankselect_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return mnight_bank_latch;
        }
    };

    public static WriteHandlerPtr mnight_bankselect_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1),main_cpu_num);
            int bankaddress;

            if (data != mnight_bank_latch) {
                mnight_bank_latch = data;

                bankaddress = 0x10000 + ((data & 0x7) * 0x4000);
                cpu_setbank(1, new UBytePtr(RAM, bankaddress));	 /* Select 8 banks of 16k */

            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xf800, input_port_2_r),
                new MemoryReadAddress(0xf801, 0xf801, input_port_0_r),
                new MemoryReadAddress(0xf802, 0xf802, input_port_1_r),
                new MemoryReadAddress(0xf803, 0xf803, input_port_3_r),
                new MemoryReadAddress(0xf804, 0xf804, input_port_4_r),
                new MemoryReadAddress(0xfa00, 0xfa00, MRA_RAM),
                new MemoryReadAddress(0xfa01, 0xfa01, MRA_RAM),
                new MemoryReadAddress(0xfa02, 0xfa02, mnight_bankselect_r),
                new MemoryReadAddress(0xfa03, 0xfa03, MRA_RAM),
                new MemoryReadAddress(0xfa08, 0xfa09, MRA_RAM),
                new MemoryReadAddress(0xfa0a, 0xfa0b, MRA_RAM),
                new MemoryReadAddress(0xfa0c, 0xfa0c, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xd9ff, MWA_RAM),
                new MemoryWriteAddress(0xda00, 0xdfff, MWA_RAM, mnight_spriteram, mnight_spriteram_size),
                new MemoryWriteAddress(0xe000, 0xe7ff, mnight_bgvideoram_w, mnight_background_videoram, mnight_backgroundram_size), // VFY
                new MemoryWriteAddress(0xe800, 0xefff, mnight_fgvideoram_w, mnight_foreground_videoram, mnight_foregroundram_size), //VFY
                new MemoryWriteAddress(0xf000, 0xf5ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xf600, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xfa00, 0xfa00, soundlatch_w),
                new MemoryWriteAddress(0xfa01, 0xfa01, MWA_RAM), // unknown but used
                new MemoryWriteAddress(0xfa02, 0xfa02, mnight_bankselect_w),
                new MemoryWriteAddress(0xfa03, 0xfa03, mnight_sprite_overdraw_w, mnight_spoverdraw_ram),
                new MemoryWriteAddress(0xfa08, 0xfa09, MWA_RAM, mnight_scrollx_ram),
                new MemoryWriteAddress(0xfa0a, 0xfa0b, MWA_RAM, mnight_scrolly_ram),
                new MemoryWriteAddress(0xfa0c, 0xfa0c, mnight_background_enable_w, mnight_bgenable_ram),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress snd_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe000, soundlatch_r),
                new MemoryReadAddress(0xefee, 0xefee, MRA_NOP),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress snd_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xeff5, 0xeff6, MWA_NOP), /* SAMPLE FREQUENCY ??? */
                new MemoryWriteAddress(0xefee, 0xefee, MWA_NOP), /* CHIP COMMAND ?? */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort snd_writeport[]
            = {
                new IOWritePort(0x0000, 0x0000, YM2203_control_port_0_w),
                new IOWritePort(0x0001, 0x0001, YM2203_write_port_0_w),
                new IOWritePort(0x0080, 0x0080, YM2203_control_port_1_w),
                new IOWritePort(0x0081, 0x0081, YM2203_write_port_1_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_mnight = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE);/* keep pressed during boot to enter service mode */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();  /* DSW0 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x02, "30k and every 50k");
            PORT_DIPSETTING(0x00, "50k and every 80k");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Normal");
            PORT_DIPSETTING(0x00, "Difficult");
            PORT_DIPNAME(0x08, 0x08, "Free Game");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x10, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0x80, "2");
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0x00, "5");

            PORT_START();  /* DSW1 */

            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_arkarea = new InputPortPtr() {
        public void handler() {
            PORT_START();  /* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE);/* keep pressed during boot to enter service mode */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();  /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x40, "50000 and every 50000");
            PORT_DIPSETTING(0x00, "100000 and every 100000");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x00, "4");

            PORT_START();  /* DSW1 */

            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 16384 * 8 + 0, 16384 * 8 + 4, 8, 12, 16384 * 8 + 8, 16384 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7},
            8 * 16
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 characters */
            1536, /* 1536 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 0x18000 * 8 + 0, 0x18000 * 8 + 4, 8, 12, 0x18000 * 8 + 8, 0x18000 * 8 + 12,
                16 * 8 + 0, 16 * 8 + 4, 16 * 8 + 0x18000 * 8 + 0, 16 * 8 + 0x18000 * 8 + 4, 16 * 8 + 8, 16 * 8 + 12, 16 * 8 + 0x18000 * 8 + 8, 16 * 8 + 0x18000 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7,
                32 * 8 + 16 * 0, 32 * 8 + 16 * 1, 32 * 8 + 16 * 2, 32 * 8 + 16 * 3, 32 * 8 + 16 * 4, 32 * 8 + 16 * 5, 32 * 8 + 16 * 6, 32 * 8 + 16 * 7},
            8 * 64
    );

    static GfxLayout bigspritelayout = new GfxLayout(
            32, 32, /* 32*32 characters */
            384, /* 384 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 0x18000 * 8 + 0, 0x18000 * 8 + 4, 8, 12, 0x18000 * 8 + 8, 0x18000 * 8 + 12,
                16 * 8 + 0, 16 * 8 + 4, 16 * 8 + 0x18000 * 8 + 0, 16 * 8 + 0x18000 * 8 + 4, 16 * 8 + 8, 16 * 8 + 12, 16 * 8 + 0x18000 * 8 + 8, 16 * 8 + 0x18000 * 8 + 12,
                64 * 8 + 0, 64 * 8 + 4, 64 * 8 + 0x18000 * 8 + 0, 64 * 8 + 0x18000 * 8 + 4, 64 * 8 + 8, 64 * 8 + 12, 64 * 8 + 0x18000 * 8 + 8, 64 * 8 + 0x18000 * 8 + 12,
                64 * 8 + 16 * 8 + 0, 64 * 8 + 16 * 8 + 4, 64 * 8 + 16 * 8 + 0x18000 * 8 + 0, 64 * 8 + 16 * 8 + 0x18000 * 8 + 4,
                64 * 8 + 16 * 8 + 8, 64 * 8 + 16 * 8 + 12, 64 * 8 + 16 * 8 + 0x18000 * 8 + 8, 64 * 8 + 16 * 8 + 0x18000 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7,
                32 * 8 + 16 * 0, 32 * 8 + 16 * 1, 32 * 8 + 16 * 2, 32 * 8 + 16 * 3, 32 * 8 + 16 * 4, 32 * 8 + 16 * 5, 32 * 8 + 16 * 6, 32 * 8 + 16 * 7,
                128 * 8 + 16 * 0, 128 * 8 + 16 * 1, 128 * 8 + 16 * 2, 128 * 8 + 16 * 3,
                128 * 8 + 16 * 4, 128 * 8 + 16 * 5, 128 * 8 + 16 * 6, 128 * 8 + 16 * 7,
                128 * 8 + 32 * 8 + 16 * 0, 128 * 8 + 32 * 8 + 16 * 1, 128 * 8 + 32 * 8 + 16 * 2, 128 * 8 + 32 * 8 + 16 * 3,
                128 * 8 + 32 * 8 + 16 * 4, 128 * 8 + 32 * 8 + 16 * 5, 128 * 8 + 32 * 8 + 16 * 6, 128 * 8 + 32 * 8 + 16 * 7},
            8 * 64 * 4
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0 * 16, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, bigspritelayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, charlayout, 32 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            12000000 / 8, // lax 11/03/1999  (1250000 . 1500000 ???)
            new int[]{YM2203_VOL(25, 25), YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_mnight = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 12000000/2 ??? */
                        readmem, writemem, null, null, /* very sensitive to these settings */
                        mnight_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 12000000/3 ??? */
                        snd_readmem, snd_writemem,
                        null, snd_writeport,
                        interrupt, 2
                ),},
            60, 10000, /* frames per second, vblank duration */
            10, /* single CPU, no need for interleaving */
            mnight_init_machine,
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            48 * 16, 48 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            mnight_vh_start,
            mnight_vh_stop,
            mnight_vh_screenrefresh,
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                )
            }
    );

    static RomLoadPtr rom_mnight = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("mn6-j19.bin", 0x00000, 0x8000, 0x56678d14);
            ROM_LOAD("mn5-j17.bin", 0x10000, 0x8000, 0x2a73f88e);
            ROM_LOAD("mn4-j16.bin", 0x18000, 0x8000, 0xc5e42bb4);
            ROM_LOAD("mn3-j14.bin", 0x20000, 0x8000, 0xdf6a4f7a);
            ROM_LOAD("mn2-j12.bin", 0x28000, 0x8000, 0x9c391d1b);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("mn1-j7.bin", 0x00000, 0x10000, 0xa0782a31);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mn11-b20.bin", 0x00000, 0x4000, 0x4d37e0f4);  // background tiles
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_LOAD("mn12-b22.bin", 0x08000, 0x4000, 0xb22cbbd3);
            ROM_CONTINUE(0x20000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x24000, 0x4000);
            ROM_LOAD("mn13-b23.bin", 0x10000, 0x4000, 0x65714070);
            ROM_CONTINUE(0x28000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_CONTINUE(0x2c000, 0x4000);

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mn7-e11.bin", 0x00000, 0x4000, 0x4883059c);  // sprites tiles
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_LOAD("mn8-e12.bin", 0x08000, 0x4000, 0x2b91445);
            ROM_CONTINUE(0x20000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x24000, 0x4000);
            ROM_LOAD("mn9-e14.bin", 0x10000, 0x4000, 0x9f08d160);
            ROM_CONTINUE(0x28000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_CONTINUE(0x2c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mn10-b10.bin", 0x00000, 0x2000, 0x37b8221f);// foreground tiles OK
            ROM_CONTINUE(0x04000, 0x2000);
            ROM_CONTINUE(0x02000, 0x2000);
            ROM_CONTINUE(0x06000, 0x2000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_arkarea = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("arkarea.008", 0x00000, 0x8000, 0x1ce1b5b9);
            ROM_LOAD("arkarea.009", 0x10000, 0x8000, 0xdb1c81d1);
            ROM_LOAD("arkarea.010", 0x18000, 0x8000, 0x5a460dae);
            ROM_LOAD("arkarea.011", 0x20000, 0x8000, 0x63f022c9);
            ROM_LOAD("arkarea.012", 0x28000, 0x8000, 0x3c4c65d5);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("arkarea.013", 0x00000, 0x8000, 0x2d409d58);

            ROM_REGION(0x30000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("arkarea.003", 0x00000, 0x4000, 0x6f45a308);  // background tiles
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_LOAD("arkarea.002", 0x08000, 0x4000, 0x051d3482);
            ROM_CONTINUE(0x20000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x24000, 0x4000);
            ROM_LOAD("arkarea.001", 0x10000, 0x4000, 0x09d11ab7);
            ROM_CONTINUE(0x28000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_CONTINUE(0x2c000, 0x4000);

            ROM_REGION(0x30000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("arkarea.007", 0x00000, 0x4000, 0xd5684a27);  // sprites tiles
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);
            ROM_LOAD("arkarea.006", 0x08000, 0x4000, 0x2c0567d6);
            ROM_CONTINUE(0x20000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x24000, 0x4000);
            ROM_LOAD("arkarea.005", 0x10000, 0x4000, 0x9886004d);
            ROM_CONTINUE(0x28000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_CONTINUE(0x2c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("arkarea.004", 0x00000, 0x2000, 0x69e36af2);// foreground tiles OK
            ROM_CONTINUE(0x04000, 0x2000);
            ROM_CONTINUE(0x02000, 0x2000);
            ROM_CONTINUE(0x06000, 0x2000);
            ROM_END();
        }
    };

    public static GameDriver driver_mnight = new GameDriver("1987", "mnight", "mnight.java", rom_mnight, null, machine_driver_mnight, input_ports_mnight, null, ROT0, "UPL (Kawakus license)", "Mutant Night");
    public static GameDriver driver_arkarea = new GameDriver("1988", "arkarea", "mnight.java", rom_arkarea, null, machine_driver_mnight, input_ports_arkarea, null, ROT0, "UPL", "Ark Area");
}
