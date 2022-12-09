/**
 * ported to 0.37b7
 * ported to 0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.vidhrdw.wiz.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;

public class wiz {

    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (data == 0x90) {
                /* ??? */
            } else {
                soundlatch_w.handler(0, data);
                /* ??? */

            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd85f, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe85f, MRA_RAM),
                new MemoryReadAddress(0xf000, 0xf000, input_port_2_r), /* DSW0 */
                new MemoryReadAddress(0xf008, 0xf008, input_port_3_r), /* DSW1 */
                new MemoryReadAddress(0xf010, 0xf010, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xf018, 0xf018, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xf800, 0xf800, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xc801, coin_counter_w),
                new MemoryWriteAddress(0xd000, 0xd3ff, MWA_RAM, wiz_videoram2),
                new MemoryWriteAddress(0xd400, 0xd7ff, MWA_RAM, wiz_colorram2),
                new MemoryWriteAddress(0xd800, 0xd83f, MWA_RAM, wiz_attributesram2),
                new MemoryWriteAddress(0xd840, 0xd85f, MWA_RAM, spriteram_2, spriteram_size),
                new MemoryWriteAddress(0xe000, 0xe3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xe400, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xe83f, wiz_attributes_w, wiz_attributesram),
                new MemoryWriteAddress(0xe840, 0xe85f, MWA_RAM, spriteram),
                new MemoryWriteAddress(0xf000, 0xf000, MWA_RAM, wiz_sprite_bank),
                new MemoryWriteAddress(0xf001, 0xf001, interrupt_enable_w),
                new MemoryWriteAddress(0xf002, 0xf003, wiz_palettebank_w),
                new MemoryWriteAddress(0xf004, 0xf005, wiz_char_bank_select_w),
                new MemoryWriteAddress(0xf006, 0xf006, wiz_flipx_w),
                new MemoryWriteAddress(0xf007, 0xf007, wiz_flipy_w),
                new MemoryWriteAddress(0xf800, 0xf800, sound_command_w),
                new MemoryWriteAddress(0xf808, 0xf808, MWA_NOP), /* explosion sound trigger; analog? */
                new MemoryWriteAddress(0xf80a, 0xf80a, MWA_NOP), /* shoot sound trigger; analog? */
                new MemoryWriteAddress(0xf818, 0xf818, MWA_NOP),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x23ff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3000, soundlatch_r), /* Stinger/Scion */
                new MemoryReadAddress(0x7000, 0x7000, soundlatch_r), /* Wiz */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x3000, 0x3000, interrupt_enable_w), /* Stinger/Scion */
                new MemoryWriteAddress(0x4000, 0x4000, AY8910_control_port_2_w),
                new MemoryWriteAddress(0x4001, 0x4001, AY8910_write_port_2_w),
                new MemoryWriteAddress(0x5000, 0x5000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x5001, 0x5001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x6000, 0x6000, AY8910_control_port_1_w), /* Wiz only */
                new MemoryWriteAddress(0x6001, 0x6001, AY8910_write_port_1_w), /* Wiz only */
                new MemoryWriteAddress(0x7000, 0x7000, interrupt_enable_w), /* Wiz */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_wiz = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW 0 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x18, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* DSW 1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x06, "4");
            PORT_DIPNAME(0x18, 0x10, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "1");
            PORT_DIPSETTING(0x10, "3");
            PORT_DIPSETTING(0x18, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x60, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000 30000");
            PORT_DIPSETTING(0x20, "20000 40000");
            PORT_DIPSETTING(0x40, "30000 60000");
            PORT_DIPSETTING(0x60, "40000 80000");
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_stinger = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x18, 0x08, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x18, "5");
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xe0, "20000 50000");
            PORT_DIPSETTING(0xc0, "20000 60000");
            PORT_DIPSETTING(0xa0, "20000 70000");
            PORT_DIPSETTING(0x80, "20000 80000");
            PORT_DIPSETTING(0x60, "20000 90000");
            PORT_DIPSETTING(0x40, "30000 80000");
            PORT_DIPSETTING(0x20, "30000 90000");
            PORT_DIPSETTING(0x00, "None");

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));	  // Doesn't seem to be referenced
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));    // Doesn't seem to be referenced
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));    // Doesn't seem to be referenced
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x70, 0x70, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_8C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_scion = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x08, "4");
            PORT_DIPSETTING(0x0c, "5");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000 40000");
            PORT_DIPSETTING(0x20, "20000 60000");
            PORT_DIPSETTING(0x10, "20000 80000");
            PORT_DIPSETTING(0x30, "30000 90000");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));    // Doesn't seem to be referenced
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));    // Doesn't seem to be referenced
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x18, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x18, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            //PORT_DIPSETTING(    0x20, DEF_STR( "Off") );  /* This setting will screw up the game */
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            //PORT_DIPSETTING(    0x40, DEF_STR( "Off") );  /* This setting will screw up the game */
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            //PORT_DIPSETTING(    0x80, DEF_STR( "Off") );  /* This setting will screw up the game */
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8, 0x2000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8, 0x2000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    // I don't know how the 32 colors are used. I'm making the
    // forest and the title screen green/brown, and the ice and sky levels
    // white/blue. What we need is screenshots.
    static GfxDecodeInfo wiz_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x6000, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0800, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x6800, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, spritelayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, spritelayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x6000, spritelayout, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo stinger_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0800, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, spritelayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, spritelayout, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface wiz_ay8910_interface = new AY8910interface(
            3, /* 3 chips */
            14318000 / 8, /* ? */
            new int[]{10, 10, 10},
            new ReadHandlerPtr[]{null, null, null},
            new ReadHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null}
    );
    static AY8910interface stinger_ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            14318000 / 8, /* ? */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_wiz = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz ??? */
                        readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* ? */
                        sound_readmem, sound_writemem, null, null,
                        nmi_interrupt, 3 /* ??? */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            wiz_gfxdecodeinfo,
            256, 32 * 8,
            wiz_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            wiz_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        wiz_ay8910_interface
                ),}
    );
    static MachineDriver machine_driver_stinger = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz ??? */
                        readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* ? */
                        sound_readmem, sound_writemem, null, null,
                        nmi_interrupt, 3 /* ??? */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            stinger_gfxdecodeinfo,
            256, 32 * 8,
            wiz_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            stinger_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        stinger_ay8910_interface
                ),}
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_wiz = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("ic07_01.bin", 0x0000, 0x4000, 0xc05f2c78);
            ROM_LOAD("ic05_03.bin", 0x4000, 0x4000, 0x7978d879);
            ROM_LOAD("ic06_02.bin", 0x8000, 0x4000, 0x9c406ad2);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("ic57_10.bin", 0x0000, 0x2000, 0x8a7575bd);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("ic12_04.bin", 0x0000, 0x2000, 0x8969acdd);
            ROM_LOAD("ic13_05.bin", 0x2000, 0x2000, 0x2868e6a5);
            ROM_LOAD("ic14_06.bin", 0x4000, 0x2000, 0xb398e142);

            ROM_REGION(0xc000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("ic03_07.bin", 0x0000, 0x2000, 0x297c02fc);
            ROM_CONTINUE(0x6000, 0x2000);
            ROM_LOAD("ic02_08.bin", 0x2000, 0x2000, 0xede77d37);
            ROM_CONTINUE(0x8000, 0x2000);
            ROM_LOAD("ic01_09.bin", 0x4000, 0x2000, 0x4d86b041);
            ROM_CONTINUE(0xa000, 0x2000);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("ic23_3-1.bin", 0x0000, 0x0100, 0x2dd52fb2);/* palette red component */

            ROM_LOAD("ic23_3-2.bin", 0x0100, 0x0100, 0x8c2880c9);/* palette green component */

            ROM_LOAD("ic23_3-3.bin", 0x0200, 0x0100, 0xa488d761);/* palette blue component */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_wizt = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */

            ROM_LOAD("wiz1.bin", 0x0000, 0x4000, 0x5a6d3c60);
            ROM_LOAD("ic05_03.bin", 0x4000, 0x4000, 0x7978d879);
            ROM_LOAD("ic06_02.bin", 0x8000, 0x4000, 0x9c406ad2);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("ic57_10.bin", 0x0000, 0x2000, 0x8a7575bd);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("wiz4.bin", 0x0000, 0x2000, 0xe6c636b3);
            ROM_LOAD("wiz5.bin", 0x2000, 0x2000, 0x77986058);
            ROM_LOAD("wiz6.bin", 0x4000, 0x2000, 0xf6970b23);

            ROM_REGION(0xc000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("wiz7.bin", 0x0000, 0x2000, 0x601f2f3f);
            ROM_CONTINUE(0x6000, 0x2000);
            ROM_LOAD("wiz8.bin", 0x2000, 0x2000, 0xf5ab982d);
            ROM_CONTINUE(0x8000, 0x2000);
            ROM_LOAD("wiz9.bin", 0x4000, 0x2000, 0xf6c662e2);
            ROM_CONTINUE(0xa000, 0x2000);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("ic23_3-1.bin", 0x0000, 0x0100, 0x2dd52fb2);/* palette red component */

            ROM_LOAD("ic23_3-2.bin", 0x0100, 0x0100, 0x8c2880c9);/* palette green component */

            ROM_LOAD("ic23_3-3.bin", 0x0200, 0x0100, 0xa488d761);/* palette blue component */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_stinger = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);/* 64k for code + 64k for decrypted opcodes */

            ROM_LOAD("n1.bin", 0x0000, 0x2000, 0xf2d2790c);/* encrypted */

            ROM_LOAD("n2.bin", 0x2000, 0x2000, 0x8fd2d8d8);/* encrypted */

            ROM_LOAD("n3.bin", 0x4000, 0x2000, 0xf1794d36);/* encrypted */

            ROM_LOAD("n4.bin", 0x6000, 0x2000, 0x230ba682);/* encrypted */

            ROM_LOAD("n5.bin", 0x8000, 0x2000, 0xa03a01da);/* encrypted */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound cpu */

            ROM_LOAD("6.bin", 0x0000, 0x2000, 0x79757f0c);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("7.bin", 0x0000, 0x2000, 0x775489be);
            ROM_LOAD("8.bin", 0x2000, 0x2000, 0x43c61b3f);
            ROM_LOAD("9.bin", 0x4000, 0x2000, 0xc9ed8fc7);

            ROM_REGION(0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("10.bin", 0x0000, 0x2000, 0xf6721930);
            ROM_LOAD("11.bin", 0x2000, 0x2000, 0xa4404e63);
            ROM_LOAD("12.bin", 0x4000, 0x2000, 0xb60fa88c);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("stinger.a7", 0x0000, 0x0100, 0x52c06fc2);/* red component */

            ROM_LOAD("stinger.b7", 0x0100, 0x0100, 0x9985e575);/* green component */

            ROM_LOAD("stinger.a8", 0x0200, 0x0100, 0x76b57629);/* blue component */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_scion = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("sc1", 0x0000, 0x2000, 0x8dcad575);
            ROM_LOAD("sc2", 0x2000, 0x2000, 0xf608e0ba);
            ROM_LOAD("sc3", 0x4000, 0x2000, 0x915289b9);
            ROM_LOAD("4.9j", 0x6000, 0x2000, 0x0f40d002);
            ROM_LOAD("5.10j", 0x8000, 0x2000, 0xdc4923b7);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound cpu */

            ROM_LOAD("sc6", 0x0000, 0x2000, 0x09f5f9c1);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("7.10e", 0x0000, 0x2000, 0x223e0d2a);
            ROM_LOAD("8.12e", 0x2000, 0x2000, 0xd3e39b48);
            ROM_LOAD("9.15e", 0x4000, 0x2000, 0x630861b5);

            ROM_REGION(0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("10.10h", 0x0000, 0x2000, 0x0d2a0d1e);
            ROM_LOAD("11.12h", 0x2000, 0x2000, 0xdc6ef8ab);
            ROM_LOAD("12.15h", 0x4000, 0x2000, 0xc82c28bf);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.7a", 0x0000, 0x0100, 0x2f89d9ea);/* red component */

            ROM_LOAD("82s129.7b", 0x0100, 0x0100, 0xba151e6a);/* green component */

            ROM_LOAD("82s129.8a", 0x0200, 0x0100, 0xf681ce59);/* blue component */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_scionc = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("1.5j", 0x0000, 0x2000, 0x5aaf571e);
            ROM_LOAD("2.6j", 0x2000, 0x2000, 0xd5a66ac9);
            ROM_LOAD("3.8j", 0x4000, 0x2000, 0x6e616f28);
            ROM_LOAD("4.9j", 0x6000, 0x2000, 0x0f40d002);
            ROM_LOAD("5.10j", 0x8000, 0x2000, 0xdc4923b7);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound cpu */

            ROM_LOAD("6.9f", 0x0000, 0x2000, 0xa66a0ce6);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("7.10e", 0x0000, 0x2000, 0x223e0d2a);
            ROM_LOAD("8.12e", 0x2000, 0x2000, 0xd3e39b48);
            ROM_LOAD("9.15e", 0x4000, 0x2000, 0x630861b5);

            ROM_REGION(0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites/chars */

            ROM_LOAD("10.10h", 0x0000, 0x2000, 0x0d2a0d1e);
            ROM_LOAD("11.12h", 0x2000, 0x2000, 0xdc6ef8ab);
            ROM_LOAD("12.15h", 0x4000, 0x2000, 0xc82c28bf);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("82s129.7a", 0x0000, 0x0100, 0x2f89d9ea);/* red component */

            ROM_LOAD("82s129.7b", 0x0100, 0x0100, 0xba151e6a);/* green component */

            ROM_LOAD("82s129.8a", 0x0200, 0x0100, 0xf681ce59);/* blue component */

            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_stinger = new InitDriverHandlerPtr() {
        public void handler() {
            char xortable[][]
                    = {
                        {0xa0, 0x88, 0x88, 0xa0}, /* .........00.0... */
                        {0x88, 0x00, 0xa0, 0x28}, /* .........00.1... */
                        {0x80, 0xa8, 0x20, 0x08}, /* .........01.0... */
                        {0x28, 0x28, 0x88, 0x88} /* .........01.1... */};
            UBytePtr rom = memory_region(REGION_CPU1);
            int diff = memory_region_length(REGION_CPU1) / 2;
            int A;

            memory_set_opcode_base(0, new UBytePtr(rom, diff));

            for (A = 0x0000; A < 0x10000; A++) {
                int row, col;
                char src;

                if ((A & 0x2040) != 0) {
                    /* not encrypted */
                    rom.write(A + diff, rom.read(A));
                } else {
                    src = rom.read(A);

                    /* pick the translation table from bits 3 and 5 */
                    row = ((A >> 3) & 1) + (((A >> 5) & 1) << 1);

                    /* pick the offset in the table from bits 3 and 5 of the source data */
                    col = ((src >> 3) & 1) + (((src >> 5) & 1) << 1);
                    /* the bottom half of the translation table is the mirror image of the top */
                    if ((src & 0x80) != 0) {
                        col = 3 - col;
                    }

                    /* decode the opcodes */
                    rom.write(A + diff, src ^ xortable[row][col]);
                }
            }
        }
    };

    public static GameDriver driver_stinger = new GameDriver("1983", "stinger", "wiz.java", rom_stinger, null, machine_driver_stinger, input_ports_stinger, init_stinger, ROT90, "Seibu Denshi", "Stinger", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_scion = new GameDriver("1984", "scion", "wiz.java", rom_scion, null, machine_driver_stinger, input_ports_scion, null, ROT0, "Seibu Denshi", "Scion", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_scionc = new GameDriver("1984", "scionc", "wiz.java", rom_scionc, driver_scion, machine_driver_stinger, input_ports_scion, null, ROT0, "Seibu Denshi [Cinematronics license]", "Scion (Cinematronics)", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_wiz = new GameDriver("1985", "wiz", "wiz.java", rom_wiz, null, machine_driver_wiz, input_ports_wiz, null, ROT270, "Seibu Kaihatsu Inc.", "Wiz", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_wizt = new GameDriver("1985", "wizt", "wiz.java", rom_wizt, driver_wiz, machine_driver_wiz, input_ports_wiz, null, ROT270, "Taito Corp.", "Wiz (Taito)", GAME_IMPERFECT_COLORS);
}
