/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.inputportH.*;
import static vidhrdw.galaxian.*;
import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mame.sndintrf.*;
import static arcadeflex.ptrlib.*;
import static mame.mame.*;
import static arcadeflex.libc_old.*;
import static mame.memory.*;
import static vidhrdw.fastfred.*;


public class fastfred {

    public static ReadHandlerPtr jumpcoas_custom_io_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (offset == 0x100) {
                return 0x63;
            }

            return 0x00;
        }
    };

	// This routine is a big hack, but the only way I can get the game working
    // without knowing anything about the way the protection chip works.
    // These values were derived based on disassembly of the code. Usually, it
    // was pretty obvious what the values should be. Of course, this will have
    // to change if a different ROM set ever surfaces.
    public static ReadHandlerPtr fastfred_custom_io_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (cpu_get_pc()) {
                case 0x03c0:
                    return 0x9d;
                case 0x03e6:
                    return 0x9f;
                case 0x0407:
                    return 0x00;
                case 0x0446:
                    return 0x94;
                case 0x049f:
                    return 0x01;
                case 0x04b1:
                    return 0x00;
                case 0x0dd2:
                    return 0x00;
                case 0x0de4:
                    return 0x20;
                case 0x122b:
                    return 0x10;
                case 0x123d:
                    return 0x00;
                case 0x1a83:
                    return 0x10;
                case 0x1a93:
                    return 0x00;
                case 0x1b26:
                    return 0x00;
                case 0x1b37:
                    return 0x80;
                case 0x2491:
                    return 0x10;
                case 0x24a2:
                    return 0x00;
                case 0x46ce:
                    return 0x20;
                case 0x46df:
                    return 0x00;
                case 0x7b18:
                    return 0x01;
                case 0x7b29:
                    return 0x00;
                case 0x7b47:
                    return 0x00;
                case 0x7b58:
                    return 0x20;
            }

            if (errorlog != null) {
                fprintf(errorlog, "Uncaught custom I/O read %04X at %04X\n", 0xc800 + offset, cpu_get_pc());
            }
            return 0x00;
        }
    };

    static MemoryReadAddress fastfred_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x9fff, MRA_NOP), // There is a bug in Fast Freddie that causes
                // these locations to be read. See 1b5a
                // One of the instructions should be ld de,
                // instead of ld hl,
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd3ff, MRA_RAM),
                new MemoryReadAddress(0xd800, 0xd8ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe000, input_port_0_r),
                new MemoryReadAddress(0xe800, 0xe800, input_port_1_r),
                new MemoryReadAddress(0xf000, 0xf000, input_port_2_r),
                new MemoryReadAddress(0xf800, 0xf800, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress fastfred_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xcfff, MWA_NOP),
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, videoram_w), // Mirrored for above
                new MemoryWriteAddress(0xd800, 0xd83f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0xd840, 0xd85f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xd860, 0xdbff, MWA_RAM), // Unused, but initialized
                new MemoryWriteAddress(0xe000, 0xe000, fastfred_background_color_w),
                new MemoryWriteAddress(0xf000, 0xf000, MWA_NOP), // Unused, but initialized
                new MemoryWriteAddress(0xf001, 0xf001, interrupt_enable_w),
                new MemoryWriteAddress(0xf002, 0xf003, fastfred_color_bank_select_w),
                new MemoryWriteAddress(0xf004, 0xf005, fastfred_character_bank_select_w),
                new MemoryWriteAddress(0xf006, 0xf006, fastfred_flipx_w),
                new MemoryWriteAddress(0xf007, 0xf007, fastfred_flipy_w),
                new MemoryWriteAddress(0xf116, 0xf116, fastfred_flipx_w),
                new MemoryWriteAddress(0xf117, 0xf117, fastfred_flipy_w),
                new MemoryWriteAddress(0xf800, 0xf800, soundlatch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress jumpcoas_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd3ff, MRA_RAM),
                new MemoryReadAddress(0xd800, 0xdbff, MRA_RAM),
                new MemoryReadAddress(0xe800, 0xe800, input_port_0_r),
                new MemoryReadAddress(0xe802, 0xe802, input_port_1_r),
                new MemoryReadAddress(0xe802, 0xe803, input_port_2_r),
                //new MemoryReadAddress( 0xf800, 0xf800, watchdog_reset_r ),  // Why doesn't this work???
                new MemoryReadAddress(0xf800, 0xf800, MRA_NOP),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress jumpcoas_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xc800, 0xcfff, MWA_NOP),
                new MemoryWriteAddress(0xd000, 0xd03f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0xd040, 0xd05f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xd060, 0xd3ff, MWA_NOP),
                new MemoryWriteAddress(0xd800, 0xdbff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xdc00, 0xdfff, videoram_w), /* mirror address, used in the name entry screen */
                new MemoryWriteAddress(0xe000, 0xe000, fastfred_background_color_w),
                new MemoryWriteAddress(0xf000, 0xf000, MWA_NOP), // Unused, but initialized
                new MemoryWriteAddress(0xf001, 0xf001, interrupt_enable_w),
                new MemoryWriteAddress(0xf002, 0xf003, fastfred_color_bank_select_w),
                new MemoryWriteAddress(0xf004, 0xf005, fastfred_character_bank_select_w),
                new MemoryWriteAddress(0xf006, 0xf006, fastfred_flipx_w),
                new MemoryWriteAddress(0xf007, 0xf007, fastfred_flipy_w),
                new MemoryWriteAddress(0xf116, 0xf116, fastfred_flipx_w),
                new MemoryWriteAddress(0xf117, 0xf117, fastfred_flipy_w),
                new MemoryWriteAddress(0xf800, 0xf800, AY8910_control_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, AY8910_write_port_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x23ff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x3000, 0x3000, interrupt_enable_w),
                new MemoryWriteAddress(0x4000, 0x4000, MWA_RAM), // Reset PSG's
                new MemoryWriteAddress(0x5000, 0x5000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x5001, 0x5001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x6000, 0x6000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0x6001, 0x6001, AY8910_write_port_1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_fastfred = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* DSW 1 */

            PORT_DIPNAME(0x0f, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, "A 2/1 B 2/1");
            PORT_DIPSETTING(0x02, "A 2/1 B 1/3");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/1");
            PORT_DIPSETTING(0x03, "A 1/1 B 1/2");
            PORT_DIPSETTING(0x04, "A 1/1 B 1/3");
            PORT_DIPSETTING(0x05, "A 1/1 B 1/4");
            PORT_DIPSETTING(0x06, "A 1/1 B 1/5");
            PORT_DIPSETTING(0x07, "A 1/1 B 1/6");
            PORT_DIPSETTING(0x08, "A 1/2 B 1/2");
            PORT_DIPSETTING(0x09, "A 1/2 B 1/4");
            PORT_DIPSETTING(0x0a, "A 1/2 B 1/5");
            PORT_DIPSETTING(0x0e, "A 1/2 B 1/6");
            PORT_DIPSETTING(0x0b, "A 1/2 B 1/10");
            PORT_DIPSETTING(0x0c, "A 1/2 B 1/11");
            PORT_DIPSETTING(0x0d, "A 1/2 B 1/12");
            PORT_DIPSETTING(0x0f, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPNAME(0x60, 0x20, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x20, "30000");
            PORT_DIPSETTING(0x40, "50000");
            PORT_DIPSETTING(0x60, "100000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_flyboy = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();       /* DSW 1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0c, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPSETTING(0x20, "7");
            PORT_BITX(0, 0x30, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x40, 0x00, "Invincibility");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_BITX(0, 0x40, IPT_DIPSWITCH_SETTING | IPF_CHEAT, DEF_STR("On"), IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_jumpcoas = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* DSW 0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x03, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0c, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPSETTING(0x20, "7");
            PORT_BITX(0, 0x30, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout fastfred_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            3, /* 3 bits per pixel */
            new int[]{0x4000 * 8, 0x2000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout jumpcoas_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            3, /* 3 bits per pixel */
            new int[]{0x2000 * 8, 0x1000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout fastfred_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x2000 * 8, 0x1000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout jumpcoas_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            3, /* 3 bits per pixel */
            new int[]{0x2000 * 8, 0x1000 * 8, 0}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo fastfred_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, fastfred_charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0, fastfred_spritelayout, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo jumpcoas_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, jumpcoas_charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, jumpcoas_spritelayout, 0, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    public static final int CLOCK = 18432000;  /* The crystal is 18.432Mhz */

    static AY8910interface fastfred_ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            CLOCK / 12, /* ? */
            new int[]{25, 25},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static AY8910interface jumpcoas_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            CLOCK / 12, /* ? */
            new int[]{25},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_fastfred = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        CLOCK / 6, /* 3.072 Mhz */
                        fastfred_readmem, fastfred_writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        CLOCK / 12, /* 1.536 Mhz */
                        sound_readmem, sound_writemem, null, null,
                        nmi_interrupt, 4
                )
            },
            60, CLOCK / 16 / 60, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            fastfred_gfxdecodeinfo,
            256, 32 * 8,
            fastfred_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_vh_start,
            generic_vh_stop,
            fastfred_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        fastfred_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_jumpcoas = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        CLOCK / 6, /* 3.072 Mhz */
                        jumpcoas_readmem, jumpcoas_writemem, null, null,
                        nmi_interrupt, 1
                )
            },
            60, CLOCK / 16 / 60, /* frames per second, vblank duration */
            1, /* Single CPU game */
            jumpcoas_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            jumpcoas_gfxdecodeinfo,
            256, 32 * 8,
            fastfred_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            fastfred_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        jumpcoas_ay8910_interface
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
    static RomLoadPtr rom_fastfred = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for main CPU */

            ROM_LOAD("ffr.01", 0x0000, 0x1000, 0x15032c13);
            ROM_LOAD("ffr.02", 0x1000, 0x1000, 0xf9642744);
            ROM_LOAD("ffr.03", 0x2000, 0x1000, 0xf0919727);
            ROM_LOAD("ffr.04", 0x3000, 0x1000, 0xc778751e);
            ROM_LOAD("ffr.05", 0x4000, 0x1000, 0xcd6e160a);
            ROM_LOAD("ffr.06", 0x5000, 0x1000, 0x67f7f9b3);
            ROM_LOAD("ffr.07", 0x6000, 0x1000, 0x2935c76a);
            ROM_LOAD("ffr.08", 0x7000, 0x1000, 0x0fb79e7b);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio CPU */

            ROM_LOAD("ffr.09", 0x0000, 0x1000, 0xa1ec8d7e);
            ROM_LOAD("ffr.10", 0x1000, 0x1000, 0x460ca837);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ffr.14", 0x0000, 0x1000, 0xe8a00e81);
            ROM_LOAD("ffr.17", 0x1000, 0x1000, 0x701e0f01);
            ROM_LOAD("ffr.15", 0x2000, 0x1000, 0xb49b053f);
            ROM_LOAD("ffr.18", 0x3000, 0x1000, 0x4b208c8b);
            ROM_LOAD("ffr.16", 0x4000, 0x1000, 0x8c686bc2);
            ROM_LOAD("ffr.19", 0x5000, 0x1000, 0x75b613f6);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ffr.11", 0x0000, 0x1000, 0x0e1316d4);
            ROM_LOAD("ffr.12", 0x1000, 0x1000, 0x94c06686);
            ROM_LOAD("ffr.13", 0x2000, 0x1000, 0x3fcfaa8e);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("flyboy.red", 0x0000, 0x0100, 0xb801e294);
            ROM_LOAD("flyboy.grn", 0x0100, 0x0100, 0x7da063d0);
            ROM_LOAD("flyboy.blu", 0x0200, 0x0100, 0x85c05c18);
            ROM_END();
        }
    };

    static RomLoadPtr rom_flyboy = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for main CPU */

            ROM_LOAD("flyboy01.cpu", 0x0000, 0x1000, 0xb05aa900);
            ROM_LOAD("flyboy02.cpu", 0x1000, 0x1000, 0x474867f5);
            ROM_LOAD("rom3.cpu", 0x2000, 0x1000, 0xd2f8f085);
            ROM_LOAD("rom4.cpu", 0x3000, 0x1000, 0x19e5e15c);
            ROM_LOAD("flyboy05.cpu", 0x4000, 0x1000, 0x207551f7);
            ROM_LOAD("rom6.cpu", 0x5000, 0x1000, 0xf5464c72);
            ROM_LOAD("rom7.cpu", 0x6000, 0x1000, 0x50a1baff);
            ROM_LOAD("rom8.cpu", 0x7000, 0x1000, 0xfe2ae95d);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio CPU */

            ROM_LOAD("rom9.cpu", 0x0000, 0x1000, 0x5d05d1a0);
            ROM_LOAD("rom10.cpu", 0x1000, 0x1000, 0x7a28005b);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom14.rom", 0x0000, 0x1000, 0xaeb07260);
            ROM_LOAD("rom17.rom", 0x1000, 0x1000, 0xa834325b);
            ROM_LOAD("rom15.rom", 0x2000, 0x1000, 0xc10c7ce2);
            ROM_LOAD("rom18.rom", 0x3000, 0x1000, 0x2f196c80);
            ROM_LOAD("rom16.rom", 0x4000, 0x1000, 0x719246b1);
            ROM_LOAD("rom19.rom", 0x5000, 0x1000, 0x00c1c5d2);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom11.rom", 0x0000, 0x1000, 0xee7ec342);
            ROM_LOAD("rom12.rom", 0x1000, 0x1000, 0x84d03124);
            ROM_LOAD("rom13.rom", 0x2000, 0x1000, 0xfcb33ff4);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("flyboy.red", 0x0000, 0x0100, 0xb801e294);
            ROM_LOAD("flyboy.grn", 0x0100, 0x0100, 0x7da063d0);
            ROM_LOAD("flyboy.blu", 0x0200, 0x0100, 0x85c05c18);
            ROM_END();
        }
    };

    static RomLoadPtr rom_flyboyb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for main CPU */

            ROM_LOAD("rom1.cpu", 0x0000, 0x1000, 0xe9e1f527);
            ROM_LOAD("rom2.cpu", 0x1000, 0x1000, 0x07fbe78c);
            ROM_LOAD("rom3.cpu", 0x2000, 0x1000, 0xd2f8f085);
            ROM_LOAD("rom4.cpu", 0x3000, 0x1000, 0x19e5e15c);
            ROM_LOAD("rom5.cpu", 0x4000, 0x1000, 0xd56872ea);
            ROM_LOAD("rom6.cpu", 0x5000, 0x1000, 0xf5464c72);
            ROM_LOAD("rom7.cpu", 0x6000, 0x1000, 0x50a1baff);
            ROM_LOAD("rom8.cpu", 0x7000, 0x1000, 0xfe2ae95d);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio CPU */

            ROM_LOAD("rom9.cpu", 0x0000, 0x1000, 0x5d05d1a0);
            ROM_LOAD("rom10.cpu", 0x1000, 0x1000, 0x7a28005b);

            ROM_REGION(0x6000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom14.rom", 0x0000, 0x1000, 0xaeb07260);
            ROM_LOAD("rom17.rom", 0x1000, 0x1000, 0xa834325b);
            ROM_LOAD("rom15.rom", 0x2000, 0x1000, 0xc10c7ce2);
            ROM_LOAD("rom18.rom", 0x3000, 0x1000, 0x2f196c80);
            ROM_LOAD("rom16.rom", 0x4000, 0x1000, 0x719246b1);
            ROM_LOAD("rom19.rom", 0x5000, 0x1000, 0x00c1c5d2);

            ROM_REGION(0x3000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom11.rom", 0x0000, 0x1000, 0xee7ec342);
            ROM_LOAD("rom12.rom", 0x1000, 0x1000, 0x84d03124);
            ROM_LOAD("rom13.rom", 0x2000, 0x1000, 0xfcb33ff4);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("flyboy.red", 0x0000, 0x0100, 0xb801e294);
            ROM_LOAD("flyboy.grn", 0x0100, 0x0100, 0x7da063d0);
            ROM_LOAD("flyboy.blu", 0x0200, 0x0100, 0x85c05c18);
            ROM_END();
        }
    };

    static RomLoadPtr rom_jumpcoas = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for code */

            ROM_LOAD("jumpcoas.001", 0x0000, 0x2000, 0x0778c953);
            ROM_LOAD("jumpcoas.002", 0x2000, 0x2000, 0x57f59ce1);
            ROM_LOAD("jumpcoas.003", 0x4000, 0x2000, 0xd9fc93be);
            ROM_LOAD("jumpcoas.004", 0x6000, 0x2000, 0xdc108fc1);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("jumpcoas.005", 0x0000, 0x1000, 0x2dce6b07);
            ROM_LOAD("jumpcoas.006", 0x1000, 0x1000, 0x0d24aa1b);
            ROM_LOAD("jumpcoas.007", 0x2000, 0x1000, 0x14c21e67);

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("jumpcoas.red", 0x0000, 0x0100, 0x13714880);
            ROM_LOAD("jumpcoas.gre", 0x0100, 0x0100, 0x05354848);
            ROM_LOAD("jumpcoas.blu", 0x0200, 0x0100, 0xf4662db7);
            ROM_END();
        }
    };

    public static InitDriverPtr init_fastfred = new InitDriverPtr() {
        public void handler() {
            install_mem_read_handler(0, 0xc800, 0xcfff, fastfred_custom_io_r);
        }
    };

    public static InitDriverPtr init_jumpcoas = new InitDriverPtr() {
        public void handler() {
            install_mem_read_handler(0, 0xc800, 0xcfff, jumpcoas_custom_io_r);
        }
    };

    public static GameDriver driver_flyboy = new GameDriver("1982", "flyboy", "fastred.java", rom_flyboy, null, machine_driver_fastfred, input_ports_flyboy, null, ROT90, "Kaneko", "Fly-Boy", GAME_NOT_WORKING);	/* protection */

    public static GameDriver driver_flyboyb = new GameDriver("1982", "flyboyb", "fastfred.java", rom_flyboyb, driver_flyboy, machine_driver_fastfred, input_ports_flyboy, null, ROT90, "Kaneko", "Fly-Boy (bootleg)");
    public static GameDriver driver_fastfred = new GameDriver("1982", "fastfred", "fastfred.java", rom_fastfred, driver_flyboy, machine_driver_fastfred, input_ports_fastfred, init_fastfred, ROT90, "Atari", "Fast Freddie");
    public static GameDriver driver_jumpcoas = new GameDriver("1983", "jumpcoas", "fastfred.java", rom_jumpcoas, null, machine_driver_jumpcoas, input_ports_jumpcoas, init_jumpcoas, ROT90, "Kaneko", "Jump Coaster");
}
