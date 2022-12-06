/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstdlib.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.galpanic.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static common.libc.cstring.*;

public class galpanic {

    public static InterruptPtr galpanic_interrupt = new InterruptPtr() {
        public int handler() {
            /* IRQ 3 drives the game, IRQ 5 updates the palette */
            if (cpu_getiloops() != 0) {
                return 5;
            } else {
                return 3;
            }
        }
    };

    public static WriteHandlerPtr galpanic_6295_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xff000000) == 0) {
                UBytePtr RAM = memory_region(REGION_SOUND1);
                memcpy(RAM, 0x30000, RAM, 0x40000 + ((data >> 8) & 0x0f) * 0x10000, 0x10000);
            }
        }
    };

    public static WriteHandlerPtr oki_msb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0xff000000) == 0) {
                OKIM6295_data_0_w.handler(offset, (data >> 8) & 0xff);
            }
        }
    };

    public static ReadHandlerPtr oki_msb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return OKIM6295_status_0_r.handler(offset) << 8;
        }
    };

    static MemoryReadAddress galpanic_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x3fffff, MRA_ROM),
                new MemoryReadAddress(0x400000, 0x400001, OKIM6295_status_0_r),
                new MemoryReadAddress(0x500000, 0x51ffff, galpanic_fgvideoram_r),
                new MemoryReadAddress(0x520000, 0x53ffff, galpanic_bgvideoram_r),
                new MemoryReadAddress(0x600000, 0x6007ff, galpanic_paletteram_r),
                new MemoryReadAddress(0x700000, 0x7047ff, galpanic_spriteram_r),
                new MemoryReadAddress(0x800000, 0x800001, input_port_0_r),
                new MemoryReadAddress(0x800002, 0x800003, input_port_1_r),
                new MemoryReadAddress(0x800004, 0x800005, input_port_2_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress galpanic_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x3fffff, MWA_ROM),
                new MemoryWriteAddress(0x400000, 0x400001, OKIM6295_data_0_w),
                new MemoryWriteAddress(0x500000, 0x51ffff, galpanic_fgvideoram_w, galpanic_fgvideoram, galpanic_fgvideoram_size),
                new MemoryWriteAddress(0x520000, 0x53ffff, galpanic_bgvideoram_w, galpanic_bgvideoram), /* + work RAM */
                new MemoryWriteAddress(0x600000, 0x6007ff, galpanic_paletteram_w, paletteram), /* 1024 colors, but only 512 seem to be used */
                new MemoryWriteAddress(0x700000, 0x7047ff, galpanic_spriteram_w, spriteram, spriteram_size),
                new MemoryWriteAddress(0x900000, 0x900001, galpanic_6295_bankswitch_w),
                new MemoryWriteAddress(0xa00000, 0xa00001, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xb00000, 0xb00001, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xc00000, 0xc00001, MWA_NOP), /* ??? */
                new MemoryWriteAddress(-1) /* end of table */};

    public static ReadHandlerPtr kludge = new ReadHandlerPtr() {
        public int handler(int offset) {
            return rand() & 0x0700;
        }
    };

    static MemoryReadAddress comad_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x4fffff, MRA_ROM),
                new MemoryReadAddress(0x500000, 0x51ffff, galpanic_fgvideoram_r),
                new MemoryReadAddress(0x520000, 0x53ffff, galpanic_bgvideoram_r),
                new MemoryReadAddress(0x600000, 0x6007ff, galpanic_paletteram_r),
                new MemoryReadAddress(0x700000, 0x700fff, galpanic_spriteram_r),
                new MemoryReadAddress(0x800000, 0x800001, input_port_0_r),
                new MemoryReadAddress(0x800002, 0x800003, input_port_1_r),
                new MemoryReadAddress(0x800004, 0x800005, input_port_2_r),
                //	new MemoryReadAddress( 0x800006, 0x800007,  ),	??
                new MemoryReadAddress(0x80000a, 0x80000b, kludge), /* bits 8-a = timer? palette update code waits for them to be 111 */
                new MemoryReadAddress(0x80000c, 0x80000d, kludge), /* missw96 bits 8-a = timer? palette update code waits for them to be 111 */
                new MemoryReadAddress(0xc00000, 0xc0ffff, MRA_BANK1), /* missw96 */
                new MemoryReadAddress(0xc80000, 0xc8ffff, MRA_BANK2), /* fantasia, newfant */
                new MemoryReadAddress(0xf00000, 0xf00001, oki_msb_r), /* fantasia, missw96 */
                new MemoryReadAddress(0xf80000, 0xf80001, oki_msb_r), /* newfant */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress comad_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x4fffff, MWA_ROM),
                new MemoryWriteAddress(0x500000, 0x51ffff, galpanic_fgvideoram_w, galpanic_fgvideoram, galpanic_fgvideoram_size),
                new MemoryWriteAddress(0x520000, 0x53ffff, galpanic_bgvideoram_w, galpanic_bgvideoram), /* + work RAM */
                new MemoryWriteAddress(0x600000, 0x6007ff, galpanic_paletteram_w, paletteram), /* 1024 colors, but only 512 seem to be used */
                new MemoryWriteAddress(0x700000, 0x700fff, galpanic_spriteram_w, spriteram, spriteram_size),
                new MemoryWriteAddress(0x900000, 0x900001, galpanic_6295_bankswitch_w), /* not sure */
                new MemoryWriteAddress(0xc00000, 0xc0ffff, MWA_BANK1), /* missw96 */
                new MemoryWriteAddress(0xc80000, 0xc8ffff, MWA_BANK2), /* fantasia, newfant */
                new MemoryWriteAddress(0xf00000, 0xf00001, oki_msb_w), /* fantasia, missw96 */
                new MemoryWriteAddress(0xf80000, 0xf80001, oki_msb_w), /* newfant */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_galpanic = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_DIPNAME(0x0001, 0x0001, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0001, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0002, 0x0002, DEF_STR("Unknown"));
            /* flip screen? */
            PORT_DIPSETTING(0x0002, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_SERVICE(0x0004, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            /* might affect coinage according to manual, */
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            /* but settings below don't match */
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0030, 0x0030, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0000, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0010, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0020, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0030, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x00c0, 0x00c0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00c0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0080, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0040, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0000, DEF_STR("1C_6C"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER1);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);// BUTTON2 | IPF_PLAYER1 ) used in test mode
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0003, 0x0003, "Difficulty?");
            PORT_DIPSETTING(0x0002, "Easy?");
            PORT_DIPSETTING(0x0003, "Normal?");
            PORT_DIPSETTING(0x0001, "Hard?");
            PORT_DIPSETTING(0x0000, "Hardest?");
            PORT_DIPNAME(0x0004, 0x0004, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0004, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0030, 0x0030, DEF_STR("Lives"));
            PORT_DIPSETTING(0x0010, "2");
            PORT_DIPSETTING(0x0030, "3");
            PORT_DIPSETTING(0x0020, "4");
            PORT_DIPSETTING(0x0000, "5");
            PORT_DIPNAME(0x0040, 0x0040, DEF_STR("Unknown"));
            /* manual says demo sounds but has no effect */
            PORT_DIPSETTING(0x0040, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0080, 0x0080, "Test Mode");
            PORT_DIPSETTING(0x0080, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);//BUTTON2 | IPF_PLAYER2 )
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x00ff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_newfant = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_DIPNAME(0x0001, 0x0001, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0001, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0002, 0x0002, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0002, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0004, 0x0004, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0004, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0010, 0x0010, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0010, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0020, 0x0020, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0020, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0040, 0x0040, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0040, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0080, 0x0080, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0080, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0001, 0x0001, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0001, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0002, 0x0002, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0002, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_SERVICE(0x0004, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0030, 0x0030, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0020, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0030, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0010, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0000, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x00c0, 0x00c0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0080, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00c0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0040, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0000, DEF_STR("1C_6C"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x00ff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_SERVICE1);/* in service mode, but doesn't add credits */
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_missw96 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_DIPNAME(0x0001, 0x0001, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0001, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0002, 0x0002, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0002, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0004, 0x0004, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0004, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0010, 0x0010, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0010, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0020, 0x0020, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0020, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0040, 0x0040, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0040, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0080, 0x0080, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0080, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x0001, 0x0001, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0001, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0002, 0x0002, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0002, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0004, 0x0004, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0004, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0008, 0x0008, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x0008, DEF_STR("Off"));
            PORT_DIPSETTING(0x0000, DEF_STR("On"));
            PORT_DIPNAME(0x0030, 0x0030, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0020, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0030, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0010, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0000, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x00c0, 0x00c0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0080, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00c0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0040, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0000, DEF_STR("1C_6C"));
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x00ff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0100, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0200, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x0400, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x0800, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x1000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x2000, IP_ACTIVE_LOW, IPT_TILT);/* MAME crashes when pressed */
            PORT_BIT(0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout spritelayout = new GfxLayout(
            16, 16,
            RGN_FRAC(1, 1),
            4,
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                64 * 4, 65 * 4, 66 * 4, 67 * 4, 68 * 4, 69 * 4, 70 * 4, 71 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 256, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static OKIM6295interface okim6295_interface = new OKIM6295interface(
            1, /* 1 chip */
            new int[]{12000}, /* 12000Hz frequency */
            new int[]{REGION_SOUND1}, /* memory region */
            new int[]{100}
    );

    static MachineDriver machine_driver_galpanic = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        8000000,
                        galpanic_readmem, galpanic_writemem, null, null,
                        galpanic_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, new rectangle(0, 256 - 1, 0, 224 - 1),
            gfxdecodeinfo,
            1024 + 32768, 1024,
            galpanic_init_palette,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_bitmapped_vh_start,
            generic_bitmapped_vh_stop,
            galpanic_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );
    static MachineDriver machine_driver_comad = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000,
                        comad_readmem, comad_writemem, null, null,
                        galpanic_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, new rectangle(0, 256 - 1, 0, 224 - 1),
            gfxdecodeinfo,
            1024 + 32768, 1024,
            galpanic_init_palette,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_bitmapped_vh_start,
            generic_bitmapped_vh_stop,
            comad_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
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
    static RomLoadPtr rom_galpanic = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x400000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("pm110.4m2", 0x000000, 0x080000, 0xae6b17a8);
            ROM_LOAD_ODD("pm109.4m1", 0x000000, 0x080000, 0xb85d792d);
            /* The above two ROMs contain valid 68000 code, but the game doesn't */
 /* work. I think there might be a protection (addressed at e00000). */
 /* The two following ROMs replace the code with a working version. */
            ROM_LOAD_EVEN("pm112.6", 0x000000, 0x20000, 0x7b972b58);
            ROM_LOAD_ODD("pm111.5", 0x000000, 0x20000, 0x4eb7298d);
            ROM_LOAD_ODD("pm004e.8", 0x100000, 0x80000, 0xd3af52bc);
            ROM_LOAD_EVEN("pm005e.7", 0x100000, 0x80000, 0xd7ec650c);
            ROM_LOAD_ODD("pm000e.15", 0x200000, 0x80000, 0x5d220f3f);
            ROM_LOAD_EVEN("pm001e.14", 0x200000, 0x80000, 0x90433eb1);
            ROM_LOAD_ODD("pm002e.17", 0x300000, 0x80000, 0x713ee898);
            ROM_LOAD_EVEN("pm003e.16", 0x300000, 0x80000, 0x6bb060fd);

            ROM_REGION(0x100000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites */
            ROM_LOAD("pm006e.67", 0x000000, 0x100000, 0x57aec037);

            ROM_REGION(0x140000, REGION_SOUND1);/* OKIM6295 samples */
 /* 00000-2ffff is fixed, 30000-3ffff is bank switched from all the ROMs */
            ROM_LOAD("pm008e.l", 0x00000, 0x80000, 0xd9379ba8);
            ROM_RELOAD(0x40000, 0x80000);
            ROM_LOAD("pm007e.u", 0xc0000, 0x80000, 0xc7ed7950);
            ROM_END();
        }
    };

    static RomLoadPtr rom_fantasia = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x500000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("prog2_16.rom", 0x000000, 0x80000, 0xe27c6c57);
            ROM_LOAD_ODD("prog1_13.rom", 0x000000, 0x80000, 0x68d27413);
            ROM_LOAD_EVEN("iscr6_09.rom", 0x100000, 0x80000, 0x2a588393);
            ROM_LOAD_ODD("iscr5_05.rom", 0x100000, 0x80000, 0x6160e0f0);
            ROM_LOAD_EVEN("iscr4_08.rom", 0x200000, 0x80000, 0xf776b743);
            ROM_LOAD_ODD("iscr3_04.rom", 0x200000, 0x80000, 0x5df0dff2);
            ROM_LOAD_EVEN("iscr2_07.rom", 0x300000, 0x80000, 0x5707d861);
            ROM_LOAD_ODD("iscr1_03.rom", 0x300000, 0x80000, 0x36cb811a);
            ROM_LOAD_EVEN("imag2_10.rom", 0x400000, 0x80000, 0x1f14a395);
            ROM_LOAD_ODD("imag1_06.rom", 0x400000, 0x80000, 0xfaf870e4);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites */
            ROM_LOAD("obj1_17.rom", 0x00000, 0x80000, 0xaadb6eb7);

            ROM_REGION(0x140000, REGION_SOUND1);/* OKIM6295 samples */
 /* 00000-2ffff is fixed, 30000-3ffff is bank switched from all the ROMs */
            ROM_LOAD("mus-1_01.rom", 0x00000, 0x80000, 0x22955efb);
            ROM_RELOAD(0x40000, 0x80000);
            ROM_LOAD("mus-2_02.rom", 0xc0000, 0x80000, 0x4cd4d6c3);

            ROM_REGION(0x200000, REGION_USER1);/* unknown */
            ROM_LOAD_EVEN("gscr2_15.rom", 0x000000, 0x80000, 0x46666768);
            ROM_LOAD_ODD("gscr1_12.rom", 0x000000, 0x80000, 0x4bd25be6);
            ROM_LOAD_EVEN("gscr4_14.rom", 0x100000, 0x80000, 0x4e7e6ed4);
            ROM_LOAD_ODD("gscr3_11.rom", 0x100000, 0x80000, 0x6d00a4c5);
            ROM_END();
        }
    };

    static RomLoadPtr rom_newfant = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x500000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("prog2_12.rom", 0x000000, 0x80000, 0xde43a457);
            ROM_LOAD_ODD("prog1_07.rom", 0x000000, 0x80000, 0x370b45be);
            ROM_LOAD_EVEN("iscr2_10.rom", 0x100000, 0x80000, 0x4f2da2eb);
            ROM_LOAD_ODD("iscr1_05.rom", 0x100000, 0x80000, 0x63c6894f);
            ROM_LOAD_EVEN("iscr4_09.rom", 0x200000, 0x80000, 0x725741ec);
            ROM_LOAD_ODD("iscr3_04.rom", 0x200000, 0x80000, 0x51d6b362);
            ROM_LOAD_EVEN("iscr6_08.rom", 0x300000, 0x80000, 0x178b2ef3);
            ROM_LOAD_ODD("iscr5_03.rom", 0x300000, 0x80000, 0xd2b5c5fa);
            ROM_LOAD_EVEN("iscr8_11.rom", 0x400000, 0x80000, 0xf4148528);
            ROM_LOAD_ODD("iscr7_06.rom", 0x400000, 0x80000, 0x2dee0c31);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites */
            ROM_LOAD("obj1_13.rom", 0x00000, 0x80000, 0xe6d1bc71);

            ROM_REGION(0x140000, REGION_SOUND1);/* OKIM6295 samples */
 /* 00000-2ffff is fixed, 30000-3ffff is bank switched from all the ROMs */
            ROM_LOAD("musc1_01.rom", 0x00000, 0x80000, 0x10347fce);
            ROM_RELOAD(0x40000, 0x80000);
            ROM_LOAD("musc2_02.rom", 0xc0000, 0x80000, 0xb9646a8c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_missw96 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x400000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("mw96_10.bin", 0x000000, 0x80000, 0xb1309bb1);
            ROM_LOAD_ODD("mw96_06.bin", 0x000000, 0x80000, 0xa5892bb3);
            ROM_LOAD_EVEN("mw96_09.bin", 0x100000, 0x80000, 0x7032dfdf);
            ROM_LOAD_ODD("mw96_05.bin", 0x100000, 0x80000, 0x91de5ab5);
            ROM_LOAD_EVEN("mw96_08.bin", 0x200000, 0x80000, 0xb8e66fb5);
            ROM_LOAD_ODD("mw96_04.bin", 0x200000, 0x80000, 0xe77a04f8);
            ROM_LOAD_EVEN("mw96_07.bin", 0x300000, 0x80000, 0x26112ed3);
            ROM_LOAD_ODD("mw96_03.bin", 0x300000, 0x80000, 0xe9374a46);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites */
            ROM_LOAD("mw96_11.bin", 0x00000, 0x80000, 0x3983152f);

            ROM_REGION(0x140000, REGION_SOUND1);/* OKIM6295 samples */
 /* 00000-2ffff is fixed, 30000-3ffff is bank switched from all the ROMs */
            ROM_LOAD("mw96_01.bin", 0x00000, 0x80000, 0xe78a659e);
            ROM_RELOAD(0x40000, 0x80000);
            ROM_LOAD("mw96_02.bin", 0xc0000, 0x80000, 0x60fa0c00);
            ROM_END();
        }
    };

    public static GameDriver driver_galpanic = new GameDriver("1990", "galpanic", "galpanic.java", rom_galpanic, null, machine_driver_galpanic, input_ports_galpanic, null, ROT90_16BIT, "Kaneko", "Gals Panic", GAME_NO_COCKTAIL);
    public static GameDriver driver_fantasia = new GameDriver("1994", "fantasia", "galpanic.java", rom_fantasia, null, machine_driver_comad, input_ports_newfant, null, ROT90_16BIT, "Comad & New Japan System", "Fantasia", GAME_NO_COCKTAIL);
    public static GameDriver driver_newfant = new GameDriver("1995", "newfant", "galpanic.java", rom_newfant, null, machine_driver_comad, input_ports_newfant, null, ROT90_16BIT, "Comad & New Japan System", "New Fantasia", GAME_NO_COCKTAIL);
    public static GameDriver driver_missw96 = new GameDriver("1996", "missw96", "galpanic.java", rom_missw96, null, machine_driver_comad, input_ports_missw96, null, ROT0_16BIT, "Comad", "Miss World '96 Nude", GAME_NO_COCKTAIL);
}
