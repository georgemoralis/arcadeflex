/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.slapfght.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.machine.slapfght.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;

public class slapfght {

    /* Driver structure definition */
    static MemoryReadAddress tigerh_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xc80f, slapfight_dpram_r),
                new MemoryReadAddress(0xc810, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd7ff, MRA_RAM),
                new MemoryReadAddress(0xd800, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xc80f, slapfight_dpram_r),
                new MemoryReadAddress(0xc810, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd7ff, MRA_RAM),
                new MemoryReadAddress(0xd800, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe7ff, MRA_RAM), /* LE 151098 */
                new MemoryReadAddress(0xe803, 0xe803, getstar_e803_r), /* LE 151098 */
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xc80f, slapfight_dpram_w, slapfight_dpram, slapfight_dpram_size),
                new MemoryWriteAddress(0xc810, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd7ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd800, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe800, 0xe800, MWA_RAM, slapfight_scrollx_lo),
                new MemoryWriteAddress(0xe801, 0xe801, MWA_RAM, slapfight_scrollx_hi),
                new MemoryWriteAddress(0xe802, 0xe802, MWA_RAM, slapfight_scrolly),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM, slapfight_videoram, slapfight_videoram_size),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM, slapfight_colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress slapbtuk_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xc80f, slapfight_dpram_w, slapfight_dpram, slapfight_dpram_size),
                new MemoryWriteAddress(0xc810, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd7ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd800, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe800, 0xe800, MWA_RAM, slapfight_scrollx_hi),
                new MemoryWriteAddress(0xe802, 0xe802, MWA_RAM, slapfight_scrolly),
                new MemoryWriteAddress(0xe803, 0xe803, MWA_RAM, slapfight_scrollx_lo),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM, slapfight_videoram, slapfight_videoram_size),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM, slapfight_colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, slapfight_port_00_r), /* status register */
                new IOReadPort(-1) /* end of table */};

    static IOWritePort tigerh_writeport[]
            = {
                new IOWritePort(0x00, 0x00, slapfight_port_00_w),
                new IOWritePort(0x01, 0x01, slapfight_port_01_w),
                new IOWritePort(0x06, 0x06, slapfight_port_06_w),
                new IOWritePort(0x07, 0x07, slapfight_port_07_w),
                new IOWritePort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, slapfight_port_00_w),
                new IOWritePort(0x01, 0x01, slapfight_port_01_w),
                //	new IOWritePort( 0x04, 0x04, getstar_port_04_w   ),
                new IOWritePort(0x06, 0x06, slapfight_port_06_w),
                new IOWritePort(0x07, 0x07, slapfight_port_07_w),
                new IOWritePort(0x08, 0x08, slapfight_port_08_w), /* select bank 0 */
                new IOWritePort(0x09, 0x09, slapfight_port_09_w), /* select bank 1 */
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0xa081, 0xa081, AY8910_read_port_0_r),
                new MemoryReadAddress(0xa091, 0xa091, AY8910_read_port_1_r),
                new MemoryReadAddress(0xc800, 0xc80f, slapfight_dpram_r),
                new MemoryReadAddress(0xc810, 0xcfff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0xa080, 0xa080, AY8910_control_port_0_w),
                new MemoryWriteAddress(0xa082, 0xa082, AY8910_write_port_0_w),
                new MemoryWriteAddress(0xa090, 0xa090, AY8910_control_port_1_w),
                new MemoryWriteAddress(0xa092, 0xa092, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xa0e0, 0xa0e0, getstar_sh_intenable_w), /* LE 151098 (maybe a0f0 also)*/
                new MemoryWriteAddress(0xc800, 0xc80f, slapfight_dpram_w),
                new MemoryWriteAddress(0xc810, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_tigerh = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();   /* DSW1 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("3C_1C"));
            //	PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x10, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Dipswitch Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Player Speed");
            PORT_DIPSETTING(0x80, "Normal");
            PORT_DIPSETTING(0x00, "Fast");

            PORT_START();   /* DSW2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x10, "20000 80000");
            PORT_DIPSETTING(0x00, "50000 120000");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_slapfigh = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();   /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_BITX(0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Screen Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();   /* DSW2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x02, 0x02, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Dipswitch Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x30, "30000 100000");
            PORT_DIPSETTING(0x10, "50000 200000");
            PORT_DIPSETTING(0x20, "50000");
            PORT_DIPSETTING(0x00, "100000");
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x40, "Easy");
            PORT_DIPSETTING(0xc0, "Medium");
            PORT_DIPSETTING(0x80, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_getstar = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();   /* DSW1 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("3C_1C"));
            //	PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x10, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Dipswitch Test", KEYCODE_F2, IP_JOY_NONE);
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();   /* DSW2 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x10, "30000 100000");
            PORT_DIPSETTING(0x00, "50000 150000");
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
            2, /* 2 bits per pixel */
            new int[]{0, 1024 * 8 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout tigerh_tilelayout = new GfxLayout(
            8, 8, /* 8*8 tiles */
            2048, /* 2048 tiles */
            4, /* 4 bits per pixel */
            new int[]{0, 2048 * 8 * 8, 2 * 2048 * 8 * 8, 3 * 2048 * 8 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every tile takes 8 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            8, 8, /* 8*8 tiles */
            4096, /* 4096 tiles */
            4, /* 4 bits per pixel */
            new int[]{0, 4096 * 8 * 8, 2 * 4096 * 8 * 8, 3 * 4096 * 8 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every tile takes 8 consecutive bytes */
    );

    static GfxLayout tigerh_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 512 * 32 * 8, 2 * 512 * 32 * 8, 3 * 512 * 32 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            32 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1024 * 32 * 8, 2 * 1024 * 32 * 8, 3 * 1024 * 32 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            32 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo tigerh_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, tigerh_tilelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, tigerh_spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{25, 25},
            new ReadHandlerPtr[]{input_port_0_r, input_port_2_r},
            new ReadHandlerPtr[]{input_port_1_r, input_port_3_r},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_tigerh = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        tigerh_readmem, writemem, readport, tigerh_writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        sound_readmem, sound_writemem, null, null,
                        nmi_interrupt, 6 /* ??? */
                )
            },
            60, /* fps - frames per second */
            //	DEFAULT_REAL_60HZ_VBLANK_DURATION,
            5000, /* wrong, but fixes graphics glitches */
            10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
            slapfight_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(1 * 8, 36 * 8 - 1, 2 * 8, 32 * 8 - 1),
            tigerh_gfxdecodeinfo,
            256, 256,
            slapfight_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            slapfight_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_slapfigh = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        sound_readmem, sound_writemem, null, null,
                        getstar_interrupt/*nmi_interrupt*/, 3, /* p'tit Seb 980926 this way it sound much better ! */
                        null, 0 /* I think music is not so far from correct speed */
                /*			ignore_interrupt, 0,
                 slapfight_sound_interrupt, 27306667 */
                )
            },
            60, /* fps - frames per second */
            //	DEFAULT_REAL_60HZ_VBLANK_DURATION,
            5000, /* wrong, but fixes graphics glitches */
            10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
            slapfight_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(1 * 8, 36 * 8 - 1, 2 * 8, 32 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            slapfight_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            slapfight_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /* identical to slapfigh_ but writemem has different scroll registers */
    static MachineDriver machine_driver_slapbtuk = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        readmem, slapbtuk_writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000,
                        sound_readmem, sound_writemem, null, null,
                        getstar_interrupt/*nmi_interrupt*/, 3, /* p'tit Seb 980926 this way it sound much better ! */
                        null, 0 /* I think music is not so far from correct speed */
                /*			ignore_interrupt, 0,
                 slapfight_sound_interrupt, 27306667 */
                )
            },
            60, /* fps - frames per second */
            //	DEFAULT_REAL_60HZ_VBLANK_DURATION,
            5000, /* wrong, but fixes graphics glitches */
            10, /* 10 CPU slices per frame - enough for the sound CPU to read all commands */
            slapfight_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(1 * 8, 36 * 8 - 1, 2 * 8, 32 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            slapfight_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            slapfight_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static RomLoadPtr rom_tigerh = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("0.4", 0x00000, 0x4000, 0x4be73246);
            ROM_LOAD("1.4", 0x04000, 0x4000, 0xaad04867);
            ROM_LOAD("2.4", 0x08000, 0x4000, 0x4843f15c);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("a47_03.bin", 0x0000, 0x2000, 0xd105260f);

            ROM_REGION(0x0800, REGION_CPU3);/* 8k for the 68705 (missing!) */

            ROM_LOAD("a47_14.mcu", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_05.bin", 0x00000, 0x2000, 0xc5325b49); /* Chars */

            ROM_LOAD("a47_04.bin", 0x02000, 0x2000, 0xcd59628e);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_09.bin", 0x00000, 0x4000, 0x31fae8a8); /* Tiles */

            ROM_LOAD("a47_08.bin", 0x04000, 0x4000, 0xe539af2b);
            ROM_LOAD("a47_07.bin", 0x08000, 0x4000, 0x02fdd429);
            ROM_LOAD("a47_06.bin", 0x0c000, 0x4000, 0x11fbcc8c);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_13.bin", 0x00000, 0x4000, 0x739a7e7e); /* Sprites */

            ROM_LOAD("a47_12.bin", 0x04000, 0x4000, 0xc064ecdb);
            ROM_LOAD("a47_11.bin", 0x08000, 0x4000, 0x744fae9b);
            ROM_LOAD("a47_10.bin", 0x0c000, 0x4000, 0xe1cf844e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.12q", 0x0000, 0x0100, 0x2c69350d);
            ROM_LOAD("82s129.12m", 0x0100, 0x0100, 0x7142e972);
            ROM_LOAD("82s129.12n", 0x0200, 0x0100, 0x25f273f2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tigerh2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("b0.5", 0x00000, 0x4000, 0x6ae7e13c);
            ROM_LOAD("a47_01.bin", 0x04000, 0x4000, 0x65df2152);
            ROM_LOAD("a47_02.bin", 0x08000, 0x4000, 0x633d324b);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("a47_03.bin", 0x0000, 0x2000, 0xd105260f);

            ROM_REGION(0x0800, REGION_CPU3);/* 8k for the 68705 (missing!) */

            ROM_LOAD("a47_14.mcu", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_05.bin", 0x00000, 0x2000, 0xc5325b49); /* Chars */

            ROM_LOAD("a47_04.bin", 0x02000, 0x2000, 0xcd59628e);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_09.bin", 0x00000, 0x4000, 0x31fae8a8); /* Tiles */

            ROM_LOAD("a47_08.bin", 0x04000, 0x4000, 0xe539af2b);
            ROM_LOAD("a47_07.bin", 0x08000, 0x4000, 0x02fdd429);
            ROM_LOAD("a47_06.bin", 0x0c000, 0x4000, 0x11fbcc8c);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_13.bin", 0x00000, 0x4000, 0x739a7e7e); /* Sprites */

            ROM_LOAD("a47_12.bin", 0x04000, 0x4000, 0xc064ecdb);
            ROM_LOAD("a47_11.bin", 0x08000, 0x4000, 0x744fae9b);
            ROM_LOAD("a47_10.bin", 0x0c000, 0x4000, 0xe1cf844e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.12q", 0x0000, 0x0100, 0x2c69350d);
            ROM_LOAD("82s129.12m", 0x0100, 0x0100, 0x7142e972);
            ROM_LOAD("82s129.12n", 0x0200, 0x0100, 0x25f273f2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tigerhj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("a47_00.bin", 0x00000, 0x4000, 0xcbdbe3cc);
            ROM_LOAD("a47_01.bin", 0x04000, 0x4000, 0x65df2152);
            ROM_LOAD("a47_02.bin", 0x08000, 0x4000, 0x633d324b);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("a47_03.bin", 0x0000, 0x2000, 0xd105260f);

            ROM_REGION(0x0800, REGION_CPU3);/* 8k for the 68705 (missing!) */

            ROM_LOAD("a47_14.mcu", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_05.bin", 0x00000, 0x2000, 0xc5325b49); /* Chars */

            ROM_LOAD("a47_04.bin", 0x02000, 0x2000, 0xcd59628e);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_09.bin", 0x00000, 0x4000, 0x31fae8a8); /* Tiles */

            ROM_LOAD("a47_08.bin", 0x04000, 0x4000, 0xe539af2b);
            ROM_LOAD("a47_07.bin", 0x08000, 0x4000, 0x02fdd429);
            ROM_LOAD("a47_06.bin", 0x0c000, 0x4000, 0x11fbcc8c);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_13.bin", 0x00000, 0x4000, 0x739a7e7e); /* Sprites */

            ROM_LOAD("a47_12.bin", 0x04000, 0x4000, 0xc064ecdb);
            ROM_LOAD("a47_11.bin", 0x08000, 0x4000, 0x744fae9b);
            ROM_LOAD("a47_10.bin", 0x0c000, 0x4000, 0xe1cf844e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.12q", 0x0000, 0x0100, 0x2c69350d);
            ROM_LOAD("82s129.12m", 0x0100, 0x0100, 0x7142e972);
            ROM_LOAD("82s129.12n", 0x0200, 0x0100, 0x25f273f2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tigerhb1 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("14", 0x00000, 0x4000, 0xca59dd73);
            ROM_LOAD("13", 0x04000, 0x4000, 0x38bd54db);
            ROM_LOAD("a47_02.bin", 0x08000, 0x4000, 0x633d324b);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("a47_03.bin", 0x0000, 0x2000, 0xd105260f);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_05.bin", 0x00000, 0x2000, 0xc5325b49); /* Chars */

            ROM_LOAD("a47_04.bin", 0x02000, 0x2000, 0xcd59628e);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_09.bin", 0x00000, 0x4000, 0x31fae8a8); /* Tiles */

            ROM_LOAD("a47_08.bin", 0x04000, 0x4000, 0xe539af2b);
            ROM_LOAD("a47_07.bin", 0x08000, 0x4000, 0x02fdd429);
            ROM_LOAD("a47_06.bin", 0x0c000, 0x4000, 0x11fbcc8c);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_13.bin", 0x00000, 0x4000, 0x739a7e7e); /* Sprites */

            ROM_LOAD("a47_12.bin", 0x04000, 0x4000, 0xc064ecdb);
            ROM_LOAD("a47_11.bin", 0x08000, 0x4000, 0x744fae9b);
            ROM_LOAD("a47_10.bin", 0x0c000, 0x4000, 0xe1cf844e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.12q", 0x0000, 0x0100, 0x2c69350d);
            ROM_LOAD("82s129.12m", 0x0100, 0x0100, 0x7142e972);
            ROM_LOAD("82s129.12n", 0x0200, 0x0100, 0x25f273f2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tigerhb2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("rom00_09.bin", 0x00000, 0x4000, 0xef738c68);
            ROM_LOAD("a47_01.bin", 0x04000, 0x4000, 0x65df2152);
            ROM_LOAD("rom02_07.bin", 0x08000, 0x4000, 0x36e250b9);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("a47_03.bin", 0x0000, 0x2000, 0xd105260f);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_05.bin", 0x00000, 0x2000, 0xc5325b49); /* Chars */

            ROM_LOAD("a47_04.bin", 0x02000, 0x2000, 0xcd59628e);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_09.bin", 0x00000, 0x4000, 0x31fae8a8); /* Tiles */

            ROM_LOAD("a47_08.bin", 0x04000, 0x4000, 0xe539af2b);
            ROM_LOAD("a47_07.bin", 0x08000, 0x4000, 0x02fdd429);
            ROM_LOAD("a47_06.bin", 0x0c000, 0x4000, 0x11fbcc8c);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a47_13.bin", 0x00000, 0x4000, 0x739a7e7e); /* Sprites */

            ROM_LOAD("a47_12.bin", 0x04000, 0x4000, 0xc064ecdb);
            ROM_LOAD("a47_11.bin", 0x08000, 0x4000, 0x744fae9b);
            ROM_LOAD("a47_10.bin", 0x0c000, 0x4000, 0xe1cf844e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.12q", 0x0000, 0x0100, 0x2c69350d);
            ROM_LOAD("82s129.12m", 0x0100, 0x0100, 0x7142e972);
            ROM_LOAD("82s129.12n", 0x0200, 0x0100, 0x25f273f2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_slapfigh = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);
            ROM_LOAD("sf_r19.bin", 0x00000, 0x8000, 0x674c0e0f);
            ROM_LOAD("sf_rh.bin", 0x10000, 0x8000, 0x3c42e4a7);/* banked at 8000 */

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("sf_r05.bin", 0x0000, 0x2000, 0x87f4705a);

            ROM_REGION(0x0800, REGION_CPU3);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r11.bin", 0x00000, 0x2000, 0x2ac7b943); /* Chars */

            ROM_LOAD("sf_r10.bin", 0x02000, 0x2000, 0x33cadc93);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r06.bin", 0x00000, 0x8000, 0xb6358305); /* Tiles */

            ROM_LOAD("sf_r09.bin", 0x08000, 0x8000, 0xe92d9d60);
            ROM_LOAD("sf_r08.bin", 0x10000, 0x8000, 0x5faeeea3);
            ROM_LOAD("sf_r07.bin", 0x18000, 0x8000, 0x974e2ea9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r03.bin", 0x00000, 0x8000, 0x8545d397); /* Sprites */

            ROM_LOAD("sf_r01.bin", 0x08000, 0x8000, 0xb1b7b925);
            ROM_LOAD("sf_r04.bin", 0x10000, 0x8000, 0x422d946b);
            ROM_LOAD("sf_r02.bin", 0x18000, 0x8000, 0x587113ae);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("sf_col21.bin", 0x0000, 0x0100, 0xa0efaf99);
            ROM_LOAD("sf_col20.bin", 0x0100, 0x0100, 0xa56d57e5);
            ROM_LOAD("sf_col19.bin", 0x0200, 0x0100, 0x5cbf9fbf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_slapbtjp = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);
            ROM_LOAD("sf_r19jb.bin", 0x00000, 0x8000, 0x9a7ac8b3);
            ROM_LOAD("sf_rh.bin", 0x10000, 0x8000, 0x3c42e4a7);/* banked at 8000 */

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("sf_r05.bin", 0x0000, 0x2000, 0x87f4705a);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r11.bin", 0x00000, 0x2000, 0x2ac7b943); /* Chars */

            ROM_LOAD("sf_r10.bin", 0x02000, 0x2000, 0x33cadc93);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r06.bin", 0x00000, 0x8000, 0xb6358305); /* Tiles */

            ROM_LOAD("sf_r09.bin", 0x08000, 0x8000, 0xe92d9d60);
            ROM_LOAD("sf_r08.bin", 0x10000, 0x8000, 0x5faeeea3);
            ROM_LOAD("sf_r07.bin", 0x18000, 0x8000, 0x974e2ea9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r03.bin", 0x00000, 0x8000, 0x8545d397); /* Sprites */

            ROM_LOAD("sf_r01.bin", 0x08000, 0x8000, 0xb1b7b925);
            ROM_LOAD("sf_r04.bin", 0x10000, 0x8000, 0x422d946b);
            ROM_LOAD("sf_r02.bin", 0x18000, 0x8000, 0x587113ae);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("sf_col21.bin", 0x0000, 0x0100, 0xa0efaf99);
            ROM_LOAD("sf_col20.bin", 0x0100, 0x0100, 0xa56d57e5);
            ROM_LOAD("sf_col19.bin", 0x0200, 0x0100, 0x5cbf9fbf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_slapbtuk = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);
            ROM_LOAD("sf_r19eb.bin", 0x00000, 0x4000, 0x2efe47af);
            ROM_LOAD("sf_r20eb.bin", 0x04000, 0x4000, 0xf42c7951);
            ROM_LOAD("sf_rh.bin", 0x10000, 0x8000, 0x3c42e4a7);/* banked at 8000 */

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("sf_r05.bin", 0x0000, 0x2000, 0x87f4705a);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r11.bin", 0x00000, 0x2000, 0x2ac7b943); /* Chars */

            ROM_LOAD("sf_r10.bin", 0x02000, 0x2000, 0x33cadc93);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r06.bin", 0x00000, 0x8000, 0xb6358305); /* Tiles */

            ROM_LOAD("sf_r09.bin", 0x08000, 0x8000, 0xe92d9d60);
            ROM_LOAD("sf_r08.bin", 0x10000, 0x8000, 0x5faeeea3);
            ROM_LOAD("sf_r07.bin", 0x18000, 0x8000, 0x974e2ea9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r03.bin", 0x00000, 0x8000, 0x8545d397); /* Sprites */

            ROM_LOAD("sf_r01.bin", 0x08000, 0x8000, 0xb1b7b925);
            ROM_LOAD("sf_r04.bin", 0x10000, 0x8000, 0x422d946b);
            ROM_LOAD("sf_r02.bin", 0x18000, 0x8000, 0x587113ae);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("sf_col21.bin", 0x0000, 0x0100, 0xa0efaf99);
            ROM_LOAD("sf_col20.bin", 0x0100, 0x0100, 0xa56d57e5);
            ROM_LOAD("sf_col19.bin", 0x0200, 0x0100, 0x5cbf9fbf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_alcon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);
            ROM_LOAD("00", 0x00000, 0x8000, 0x2ba82d60);
            ROM_LOAD("01", 0x10000, 0x8000, 0x18bb2f12);/* banked at 8000 */

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for the audio CPU */

            ROM_LOAD("sf_r05.bin", 0x0000, 0x2000, 0x87f4705a);

            ROM_REGION(0x0800, REGION_CPU3);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("04", 0x00000, 0x2000, 0x31003483); /* Chars */

            ROM_LOAD("03", 0x02000, 0x2000, 0x404152c0);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r06.bin", 0x00000, 0x8000, 0xb6358305); /* Tiles */

            ROM_LOAD("sf_r09.bin", 0x08000, 0x8000, 0xe92d9d60);
            ROM_LOAD("sf_r08.bin", 0x10000, 0x8000, 0x5faeeea3);
            ROM_LOAD("sf_r07.bin", 0x18000, 0x8000, 0x974e2ea9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sf_r03.bin", 0x00000, 0x8000, 0x8545d397); /* Sprites */

            ROM_LOAD("sf_r01.bin", 0x08000, 0x8000, 0xb1b7b925);
            ROM_LOAD("sf_r04.bin", 0x10000, 0x8000, 0x422d946b);
            ROM_LOAD("sf_r02.bin", 0x18000, 0x8000, 0x587113ae);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("sf_col21.bin", 0x0000, 0x0100, 0xa0efaf99);
            ROM_LOAD("sf_col20.bin", 0x0100, 0x0100, 0xa56d57e5);
            ROM_LOAD("sf_col19.bin", 0x0200, 0x0100, 0x5cbf9fbf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_getstar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);	/* Region 0 - main cpu code */

            ROM_LOAD("rom0", 0x00000, 0x4000, 0x6a8bdc6c);
            ROM_LOAD("rom1", 0x04000, 0x4000, 0xebe8db3c);
            ROM_LOAD("rom2", 0x10000, 0x8000, 0x343e8415);

            ROM_REGION(0x10000, REGION_CPU2);	/* Region 3 - sound cpu code */

            ROM_LOAD("a68-03", 0x0000, 0x2000, 0x18daa44c);

            ROM_REGION(0x0800, REGION_CPU3);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_05-1", 0x00000, 0x2000, 0x06f60107); /* Chars */

            ROM_LOAD("a68_04-1", 0x02000, 0x2000, 0x1fc8f277);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_09", 0x00000, 0x8000, 0xa293cc2e); /* Tiles */

            ROM_LOAD("a68_08", 0x08000, 0x8000, 0x37662375);
            ROM_LOAD("a68_07", 0x10000, 0x8000, 0xcf1a964c);
            ROM_LOAD("a68_06", 0x18000, 0x8000, 0x05f9eb9a);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68-13", 0x00000, 0x8000, 0x643fb282); /* Sprites */

            ROM_LOAD("a68-12", 0x08000, 0x8000, 0x11f74e32);
            ROM_LOAD("a68-11", 0x10000, 0x8000, 0xf24158cf);
            ROM_LOAD("a68-10", 0x18000, 0x8000, 0x83161ed0);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("rom21", 0x0000, 0x0100, 0xd6360b4d);
            ROM_LOAD("rom20", 0x0100, 0x0100, 0x4ca01887);
            ROM_LOAD("rom19", 0x0200, 0x0100, 0x513224f0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_getstarj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);	/* Region 0 - main cpu code */

            ROM_LOAD("a68_00.bin", 0x00000, 0x4000, 0xad1a0143);
            ROM_LOAD("a68_01.bin", 0x04000, 0x4000, 0x3426eb7c);
            ROM_LOAD("a68_02.bin", 0x10000, 0x8000, 0x3567da17);

            ROM_REGION(0x10000, REGION_CPU2);	/* Region 3 - sound cpu code */

            ROM_LOAD("a68-03", 0x00000, 0x2000, 0x18daa44c);

            ROM_REGION(0x0800, REGION_CPU3);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x00000000);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_05.bin", 0x00000, 0x2000, 0xe3d409e7); /* Chars */

            ROM_LOAD("a68_04.bin", 0x02000, 0x2000, 0x6e5ac9d4);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_09", 0x00000, 0x8000, 0xa293cc2e); /* Tiles */

            ROM_LOAD("a68_08", 0x08000, 0x8000, 0x37662375);
            ROM_LOAD("a68_07", 0x10000, 0x8000, 0xcf1a964c);
            ROM_LOAD("a68_06", 0x18000, 0x8000, 0x05f9eb9a);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68-13", 0x00000, 0x8000, 0x643fb282); /* Sprites */

            ROM_LOAD("a68-12", 0x08000, 0x8000, 0x11f74e32);
            ROM_LOAD("a68-11", 0x10000, 0x8000, 0xf24158cf);
            ROM_LOAD("a68-10", 0x18000, 0x8000, 0x83161ed0);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("rom21", 0x0000, 0x0100, 0xd6360b4d);
            ROM_LOAD("rom20", 0x0100, 0x0100, 0x4ca01887);
            ROM_LOAD("rom19", 0x0200, 0x0100, 0x513224f0);
            ROM_END();
        }
    };

    static RomLoadPtr rom_getstarb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);	/* Region 0 - main cpu code */

            ROM_LOAD("gs_14.rom", 0x00000, 0x4000, 0x1a57a920);
            ROM_LOAD("gs_13.rom", 0x04000, 0x4000, 0x805f8e77);
            ROM_LOAD("a68_02.bin", 0x10000, 0x8000, 0x3567da17);

            ROM_REGION(0x10000, REGION_CPU2);	/* Region 3 - sound cpu code */

            ROM_LOAD("a68-03", 0x0000, 0x2000, 0x18daa44c);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_05.bin", 0x00000, 0x2000, 0xe3d409e7); /* Chars */

            ROM_LOAD("a68_04.bin", 0x02000, 0x2000, 0x6e5ac9d4);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68_09", 0x00000, 0x8000, 0xa293cc2e); /* Tiles */

            ROM_LOAD("a68_08", 0x08000, 0x8000, 0x37662375);
            ROM_LOAD("a68_07", 0x10000, 0x8000, 0xcf1a964c);
            ROM_LOAD("a68_06", 0x18000, 0x8000, 0x05f9eb9a);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* Region 1 - temporary for gfx */

            ROM_LOAD("a68-13", 0x00000, 0x8000, 0x643fb282); /* Sprites */

            ROM_LOAD("a68-12", 0x08000, 0x8000, 0x11f74e32);
            ROM_LOAD("a68-11", 0x10000, 0x8000, 0xf24158cf);
            ROM_LOAD("a68-10", 0x18000, 0x8000, 0x83161ed0);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("rom21", 0x0000, 0x0100, 0xd6360b4d);
            ROM_LOAD("rom20", 0x0100, 0x0100, 0x4ca01887);
            ROM_LOAD("rom19", 0x0200, 0x0100, 0x513224f0);
            ROM_END();
        }
    };

    public static GameDriver driver_tigerh = new GameDriver("1985", "tigerh", "slapfght.java", rom_tigerh, null, machine_driver_tigerh, input_ports_tigerh, null, ROT270, "Taito", "Tiger Heli (set 1)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tigerh2 = new GameDriver("1985", "tigerh2", "slapfght.java", rom_tigerh2, driver_tigerh, machine_driver_tigerh, input_ports_tigerh, null, ROT270, "Taito", "Tiger Heli (set 2)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tigerhj = new GameDriver("1985", "tigerhj", "slapfght.java", rom_tigerhj, driver_tigerh, machine_driver_tigerh, input_ports_tigerh, null, ROT270, "Taito", "Tiger Heli (Japan)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tigerhb1 = new GameDriver("1985", "tigerhb1", "slapfght.java", rom_tigerhb1, driver_tigerh, machine_driver_tigerh, input_ports_tigerh, null, ROT270, "bootleg", "Tiger Heli (bootleg 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_tigerhb2 = new GameDriver("1985", "tigerhb2", "slapfght.java", rom_tigerhb2, driver_tigerh, machine_driver_tigerh, input_ports_tigerh, null, ROT270, "bootleg", "Tiger Heli (bootleg 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_slapfigh = new GameDriver("1986", "slapfigh", "slapfght.java", rom_slapfigh, null, machine_driver_slapfigh, input_ports_slapfigh, null, ROT270, "Taito", "Slap Fight", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_slapbtjp = new GameDriver("1986", "slapbtjp", "slapfght.java", rom_slapbtjp, driver_slapfigh, machine_driver_slapfigh, input_ports_slapfigh, null, ROT270, "bootleg", "Slap Fight (Japan bootleg)", GAME_NO_COCKTAIL);
    public static GameDriver driver_slapbtuk = new GameDriver("1986", "slapbtuk", "slapfght.java", rom_slapbtuk, driver_slapfigh, machine_driver_slapbtuk, input_ports_slapfigh, null, ROT270, "bootleg", "Slap Fight (English bootleg)", GAME_NO_COCKTAIL);
    public static GameDriver driver_alcon = new GameDriver("1986", "alcon", "slapfght.java", rom_alcon, driver_slapfigh, machine_driver_slapfigh, input_ports_slapfigh, null, ROT270, "<unknown>", "Alcon", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_getstar = new GameDriver("1986", "getstar", "slapfght.java", rom_getstar, null, machine_driver_slapfigh, input_ports_getstar, null, ROT0, "Taito", "Guardian", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_getstarj = new GameDriver("1986", "getstarj", "slapfght.java", rom_getstarj, driver_getstar, machine_driver_slapfigh, input_ports_getstar, null, ROT0, "Taito", "Get Star (Japan)", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_getstarb = new GameDriver("1986", "getstarb", "slapfght.java", rom_getstarb, driver_getstar, machine_driver_slapfigh, input_ports_getstar, null, ROT0, "bootleg", "Get Star (bootleg)", GAME_NO_COCKTAIL);
}
