/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.galaxian.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.vidhrdw.zodiack.*;
import static arcadeflex.v036.machine.espial.*;

public class zodiack {

    public static int percuss_hardware;

    public static InitMachineHandlerPtr zodiack_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            percuss_hardware = 0;
            espial_init_machine.handler();
        }
    };

    public static InitMachineHandlerPtr percuss_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            percuss_hardware = 1;
            espial_init_machine.handler();
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x4fff, MRA_ROM),
                new MemoryReadAddress(0x5800, 0x5fff, MRA_RAM),
                new MemoryReadAddress(0x6081, 0x6081, input_port_0_r),
                new MemoryReadAddress(0x6082, 0x6082, input_port_1_r),
                new MemoryReadAddress(0x6083, 0x6083, input_port_2_r),
                new MemoryReadAddress(0x6084, 0x6084, input_port_3_r),
                new MemoryReadAddress(0x6090, 0x6090, soundlatch_r),
                new MemoryReadAddress(0x7000, 0x7000, MRA_NOP), /* ??? */
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa3ff, MRA_RAM),
                new MemoryReadAddress(0xb000, 0xb3ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xcfff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x4fff, MWA_ROM),
                new MemoryWriteAddress(0x5800, 0x5fff, MWA_RAM),
                new MemoryWriteAddress(0x6081, 0x6081, zodiac_control_w),
                new MemoryWriteAddress(0x6090, 0x6090, zodiac_master_soundlatch_w),
                new MemoryWriteAddress(0x7000, 0x7000, watchdog_reset_w),
                new MemoryWriteAddress(0x7100, 0x7100, zodiac_master_interrupt_enable_w),
                new MemoryWriteAddress(0x7200, 0x7200, zodiac_flipscreen_w),
                new MemoryWriteAddress(0x9000, 0x903f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x9040, 0x905f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9060, 0x907f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x9080, 0x93ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xb000, 0xb3ff, MWA_RAM, zodiack_videoram2),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x23ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x4000, interrupt_enable_w),
                new MemoryWriteAddress(0x6000, 0x6000, soundlatch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_zodiac = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */ /* never read in this game */


            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x1c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x14, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x18, "2 Coins/1 Credit  3 Coins/2 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x1c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000 50000");
            PORT_DIPSETTING(0x20, "40000 70000");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_dogfight = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x05, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x07, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x38, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x38, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x28, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            /* most likely unused */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            /* most likely unused */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            /* most likely unused */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            /* most likely unused */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BITX(0x10, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20k, 50k, then every 50k");
            PORT_DIPSETTING(0x20, "40k, 70k, then every 70k");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, "Freeze");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_moguchan = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */

            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x1c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x14, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x18, "2 Coins/1 Credit  3 Coins/2 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x1c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000 50000");
            PORT_DIPSETTING(0x20, "40000 70000");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            /* these are read, but are they */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            /* ever used? */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_percuss = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000 100000");
            PORT_DIPSETTING(0x04, "20000 200000");
            PORT_DIPSETTING(0x08, "40000 100000");
            PORT_DIPSETTING(0x0c, "40000 200000");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x10, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x02, "6");
            PORT_DIPSETTING(0x03, "7");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            256, /* 256 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* single bitplane */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout charlayout_2 = new GfxLayout(
            8, 8, /* 8*8 chars */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 512 * 8 * 8}, /* The bitplanes are seperate */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 128 * 32 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout bulletlayout = new GfxLayout(
            /* there is no gfx ROM for this one, it is generated by the hardware */
            7, 1, /* it's just 1 pixel, but we use 7*1 to position it correctly */
            1, /* just one */
            1, /* 1 bit per pixel */
            new int[]{10 * 8 * 8}, /* point to letter "A" */
            new int[]{3, 7, 7, 7, 7, 7, 7}, /* I "know" that this bit of the */
            new int[]{1 * 8}, /* graphics ROMs is 1 */
            0 /* no use */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 8 * 4, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, bulletlayout, 8 * 4 + 8 * 2, 1),
                new GfxDecodeInfo(REGION_GFX1, 0x1000, charlayout_2, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1789750, /* 1.78975 MHz? */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_zodiack = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4.00 MHz??? */
                        readmem, writemem, null, null,
                        zodiac_master_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80,
                        14318000 / 8, /* 1.78975 Mhz??? */
                        sound_readmem, sound_writemem, null, sound_writeport,
                        nmi_interrupt, 8 /* IRQs are triggered by the main CPU */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            zodiack_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            49, 4 * 8 + 2 * 8 + 2 * 1,
            zodiack_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            zodiack_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_percuss = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4.00 MHz??? */
                        readmem, writemem, null, null,
                        zodiac_master_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80,
                        14318000 / 8, /* 1.78975 Mhz??? */
                        sound_readmem, sound_writemem, null, sound_writeport,
                        nmi_interrupt, 8 /* IRQs are triggered by the main CPU */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            percuss_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            49, 4 * 8 + 2 * 8 + 2 * 1,
            zodiack_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            zodiack_vh_screenrefresh,
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
    static RomLoadHandlerPtr rom_zodiack = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("ovg30c.2", 0x0000, 0x2000, 0xa2125e99);
            ROM_LOAD("ovg30c.3", 0x2000, 0x2000, 0xaee2b77f);
            ROM_LOAD("ovg30c.6", 0x4000, 0x0800, 0x1debb278);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ovg20c.1", 0x0000, 0x1000, 0x2d3c3baf);

            ROM_REGION(0x2800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ovg40c.7", 0x0000, 0x0800, 0xed9d3be7);
            ROM_LOAD("orca40c.8", 0x0800, 0x1000, 0x88269c94);
            ROM_LOAD("orca40c.9", 0x1800, 0x1000, 0xa3bd40c9);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("ovg40c.2a", 0x0000, 0x0020, 0x703821b8);
            ROM_LOAD("ovg40c.2b", 0x0020, 0x0020, 0x21f77ec7);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dogfight = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("df-2", 0x0000, 0x2000, 0xad24b28b);
            ROM_LOAD("df-3", 0x2000, 0x2000, 0xcd172707);
            ROM_LOAD("df-5", 0x4000, 0x1000, 0x874dc6bf);
            ROM_LOAD("df-4", 0xc000, 0x1000, 0xd8aa3d6d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("df-1", 0x0000, 0x1000, 0xdcbb1c5b);

            ROM_REGION(0x2800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("df-6", 0x0000, 0x0800, 0x3059b515);
            ROM_LOAD("df-7", 0x0800, 0x1000, 0xffe05fee);
            ROM_LOAD("df-8", 0x1800, 0x1000, 0x2cb51793);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("1.bpr", 0x0000, 0x0020, 0x69a35aa5);
            ROM_LOAD("2.bpr", 0x0020, 0x0020, 0x596ae457);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_moguchan = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("2.5r", 0x0000, 0x1000, 0x85d0cb7e);
            ROM_LOAD("4.5m", 0x1000, 0x1000, 0x359ef951);
            ROM_LOAD("3.5np", 0x2000, 0x1000, 0xc8776f77);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("1.7hj", 0x0000, 0x1000, 0x1a88d35f);

            ROM_REGION(0x2800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5.4r", 0x0000, 0x0800, 0x1b7febd8);
            ROM_LOAD("6.7p", 0x0800, 0x1000, 0xc8060ffe);
            ROM_LOAD("7.7m", 0x1800, 0x1000, 0xbfca00f4);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("moguchan.2a", 0x0000, 0x0020, 0xe83daab3);
            ROM_LOAD("moguchan.2b", 0x0020, 0x0020, 0x9abfdf40);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_percuss = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("percuss.1", 0x0000, 0x1000, 0xff0364f7);
            ROM_LOAD("percuss.3", 0x1000, 0x1000, 0x7f646c59);
            ROM_LOAD("percuss.2", 0x2000, 0x1000, 0x6bf72dd2);
            ROM_LOAD("percuss.4", 0x3000, 0x1000, 0xfb1b15ba);
            ROM_LOAD("percuss.5", 0x4000, 0x1000, 0x8e5a9692);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("percuss.8", 0x0000, 0x0800, 0xd63f56f3);
            ROM_LOAD("percuss.9", 0x0800, 0x0800, 0xe08fef2f);

            ROM_REGION(0x2800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("percuss.10", 0x0000, 0x0800, 0x797598aa);
            ROM_LOAD("percuss.6", 0x0800, 0x1000, 0x5285a580);
            ROM_LOAD("percuss.7", 0x1800, 0x1000, 0x8fc4175d);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("percus2a.prm", 0x0000, 0x0020, 0xe2ee9637);
            ROM_LOAD("percus2b.prm", 0x0020, 0x0020, 0xe561b029);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_bounty = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
 /* first 256 bytes are missing due to protection - they are replaced */
 /* with stub code that draws the Orca logo on screen */
            ROM_LOAD("1.4f", 0x0000, 0x1000, BADCRC(0xb3776ecb));
            ROM_LOAD("3.4k", 0x1000, 0x1000, 0xfa3086c3);
            ROM_LOAD("2.4h", 0x2000, 0x1000, 0x52ab5314);
            ROM_LOAD("4.4m", 0x3000, 0x1000, 0x5c9d3f07);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("7.4n", 0x0000, 0x1000, 0x45e369b8);
            ROM_LOAD("8.4r", 0x1000, 0x1000, 0x4f52c87d);

            ROM_REGION(0x2800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9.4r", 0x0000, 0x0800, 0x4b4acde5);
            ROM_LOAD("5.7m", 0x0800, 0x1000, 0xa5ce2a24);
            ROM_LOAD("6.7p", 0x1800, 0x1000, 0x43183301);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("mb7051.2a", 0x0000, 0x0020, 0x0de11a46);
            ROM_LOAD("mb7051.2b", 0x0020, 0x0020, 0x465e31d4);
            ROM_END();
        }
    };

    public static GameDriver driver_zodiack = new GameDriver("1983", "zodiack", "zodiack.java", rom_zodiack, null, machine_driver_zodiack, input_ports_zodiac, null, ROT270, "Orca (Esco Trading Co, Inc)", "Zodiack", GAME_IMPERFECT_COLORS);/* bullet color needs to be verified */
    public static GameDriver driver_dogfight = new GameDriver("1983", "dogfight", "zodiack.java", rom_dogfight, null, machine_driver_zodiack, input_ports_dogfight, null, ROT270, "[Orca] Thunderbolt", "Dog Fight", GAME_IMPERFECT_COLORS);/* bullet color needs to be verified */
    public static GameDriver driver_moguchan = new GameDriver("1982", "moguchan", "zodiack.java", rom_moguchan, null, machine_driver_zodiack, input_ports_moguchan, null, ROT270, "Orca (Eastern Commerce Inc. license) (bootleg?)", /* this is in the ROM at $0b5c */ "Moguchan", GAME_WRONG_COLORS);
    public static GameDriver driver_percuss = new GameDriver("1981", "percuss", "zodiack.java", rom_percuss, null, machine_driver_percuss, input_ports_percuss, null, ROT270, "Orca", "The Percussor");
    public static GameDriver driver_bounty = new GameDriver("1982", "bounty", "zodiack.java", rom_bounty, null, machine_driver_zodiack, input_ports_percuss, null, ROT0, "Orca", "The Bounty", GAME_NOT_WORKING);

}
