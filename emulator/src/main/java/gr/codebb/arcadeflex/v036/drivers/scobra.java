
/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.scramble.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.frogger.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.galaxian.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;

public class scobra {

    public static InitMachinePtr scobra_init_machine = new InitMachinePtr() {
        public void handler() {
            /* we must start with NMI interrupts disabled, otherwise some games */
            /* (e.g. Lost Tomb, Rescue) will not pass the startup test. */
            interrupt_enable_w.handler(0, 0);
        }
    };
    public static ReadHandlerPtr moonwar2_IN0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int sign;
            int delta;

            delta = readinputport(3);

            sign = (delta & 0x80) >> 3;
            delta &= 0x0f;

            return ((readinputport(0) & 0xe0) | delta | sign);
        }
    };

    public static WriteHandlerPtr stratgyx_coin_counter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Bit 1 selects coin counter */
            coin_counter_w.handler(offset >> 1, data);
        }
    };

    static MemoryReadAddress type1_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x9000, 0x907f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x9800, 0x9800, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x9801, 0x9801, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x9802, 0x9802, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0xb000, 0xb000, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress type1_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8800, 0x8bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8c00, 0x8fff, MWA_NOP),
                new MemoryWriteAddress(0x9000, 0x903f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x9040, 0x905f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9060, 0x907f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x9080, 0x90ff, MWA_NOP),
                new MemoryWriteAddress(0xa000, 0xa000, soundlatch_w),
                new MemoryWriteAddress(0xa001, 0xa001, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(0xa801, 0xa801, interrupt_enable_w),
                new MemoryWriteAddress(0xa802, 0xa802, coin_counter_w),
                new MemoryWriteAddress(0xa803, 0xa803, scramble_background_w),
                new MemoryWriteAddress(0xa804, 0xa804, galaxian_stars_w),
                new MemoryWriteAddress(0xa806, 0xa806, galaxian_flipx_w),
                new MemoryWriteAddress(0xa807, 0xa807, galaxian_flipy_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress type2_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x9800, 0x9800, watchdog_reset_r),
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xa004, 0xa004, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xa008, 0xa008, input_port_2_r), /* IN2 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress type2_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8800, 0x883f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x8840, 0x885f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x8860, 0x887f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x8880, 0x88ff, MWA_NOP),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xa800, 0xa800, soundlatch_w),
                new MemoryWriteAddress(0xa804, 0xa804, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(0xb000, 0xb000, galaxian_stars_w),
                new MemoryWriteAddress(0xb002, 0xb002, scramble_background_w),
                new MemoryWriteAddress(0xb004, 0xb004, interrupt_enable_w),
                new MemoryWriteAddress(0xb006, 0xb008, stratgyx_coin_counter_w),
                new MemoryWriteAddress(0xb00c, 0xb00c, galaxian_flipy_w),
                new MemoryWriteAddress(0xb00e, 0xb00e, galaxian_flipx_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress hustler_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x9000, 0x907f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0xb800, 0xb800, watchdog_reset_r),
                new MemoryReadAddress(0xd000, 0xd000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xd008, 0xd008, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xd010, 0xd010, input_port_2_r), /* IN2 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress hustler_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8800, 0x8bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9000, 0x903f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x9040, 0x905f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9060, 0x907f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0xa802, 0xa802, galaxian_flipx_w),
                new MemoryWriteAddress(0xa804, 0xa804, interrupt_enable_w),
                new MemoryWriteAddress(0xa806, 0xa806, galaxian_flipy_w),
                new MemoryWriteAddress(0xa80e, 0xa80e, MWA_NOP), /* coin counters */
                new MemoryWriteAddress(0xe000, 0xe000, soundlatch_w),
                new MemoryWriteAddress(0xe008, 0xe008, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress hustlerb_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x9000, 0x907f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0xb000, 0xb000, watchdog_reset_r),
                new MemoryReadAddress(0xc100, 0xc100, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xc101, 0xc101, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xc102, 0xc102, input_port_2_r), /* IN2 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress hustlerb_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x8800, 0x8bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9000, 0x903f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x9040, 0x905f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9060, 0x907f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0xa801, 0xa801, interrupt_enable_w),
                new MemoryWriteAddress(0xa802, 0xa802, MWA_NOP), /* coin counters */
                new MemoryWriteAddress(0xa806, 0xa806, galaxian_flipy_w),
                new MemoryWriteAddress(0xa807, 0xa807, galaxian_flipx_w),
                new MemoryWriteAddress(0xc200, 0xc200, soundlatch_w),
                new MemoryWriteAddress(0xc201, 0xc201, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9fff, scramble_filter_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress hustler_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress hustler_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x20, 0x20, AY8910_read_port_0_r),
                new IOReadPort(0x80, 0x80, AY8910_read_port_1_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x10, 0x10, AY8910_control_port_0_w),
                new IOWritePort(0x20, 0x20, AY8910_write_port_0_w),
                new IOWritePort(0x40, 0x40, AY8910_control_port_1_w),
                new IOWritePort(0x80, 0x80, AY8910_write_port_1_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort hustler_sound_readport[]
            = {
                new IOReadPort(0x40, 0x40, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort hustler_sound_writeport[]
            = {
                new IOWritePort(0x40, 0x40, AY8910_write_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_control_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort hustlerb_sound_readport[]
            = {
                new IOReadPort(0x80, 0x80, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort hustlerb_sound_writeport[]
            = {
                new IOWritePort(0x40, 0x40, AY8910_control_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_scobra = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x01, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x01, DEF_STR("Yes"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "1 Coin/99 Credits");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /* identical to scobra apart from the number of lives */
    static InputPortPtr input_ports_scobrak = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x01, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x01, DEF_STR("Yes"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "1 Coin/99 Credits");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_stratgyx = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x81, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "99", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* IN2 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "1 Coin/99 Credits");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_armorcar = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPSETTING(0x04, "Coin A 1/3 Coin B 3/1");
            PORT_DIPSETTING(0x06, "Coin A 1/4 Coin B 4/1");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_moonwar2 = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x1f, IP_ACTIVE_LOW, IPT_UNKNOWN);/* the spinner */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();       /* IN2 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN3 - dummy port for the dial */

            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 25, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_monwar2a = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x1f, IP_ACTIVE_LOW, IPT_UNKNOWN);/* the spinner */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();       /* IN2 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN3 - dummy port for the dial */

            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 25, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_spdcoin = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x01, 0x00, "Freeze");  /* Dip Sw #2 */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown")); /* Dip Sw #1 */

            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));    /* Dip Sw #5 */

            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));    /* Dip Sw #4 */

            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Lives"));    /* Dip Sw #3 */

            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();       /* IN3 - dummy port for the dial */

            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 25, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_darkplnt = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON3);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPSETTING(0x04, "Coin A 1/3 Coin B 3/1");
            PORT_DIPSETTING(0x06, "Coin A 1/4 Coin B 4/1");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_tazmania = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPSETTING(0x04, "Coin A 1/3 Coin B 3/1");
            PORT_DIPSETTING(0x06, "Coin A 1/4 Coin B 4/1");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /* Cocktail mode is N/A */
    static InputPortPtr input_ports_calipso = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x08, 0x08, "Cabinet (Not Supported)");
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /* Cocktail mode not working due to bug */
    static InputPortPtr input_ports_anteater = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPSETTING(0x04, "Coin A 1/3 Coin B 3/1");
            PORT_DIPSETTING(0x06, "Coin A 1/4 Coin B 4/1");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x08, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_rescue = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_DIPNAME(0x02, 0x02, "Starting Level");
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x00, "3");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPSETTING(0x04, "Coin A 1/3 Coin B 3/1");
            PORT_DIPSETTING(0x06, "Coin A 1/4 Coin B 4/1");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_minefld = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_DIPNAME(0x02, 0x02, "Starting Level");
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x00, "3");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "Coin A 1/2 Coin B 2/1");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /* Cocktail mode is N/A */
    static InputPortPtr input_ports_losttomb = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "A 1/2 B 2/1");
            PORT_DIPSETTING(0x04, "A 1/3 B 3/1");
            PORT_DIPSETTING(0x06, "A 1/4 B 4/1");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /* Cocktail mode is N/A */
    static InputPortPtr input_ports_superbon = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "A 1/2 B 2/1");
            PORT_DIPSETTING(0x04, "A 1/3 B 3/1");
            PORT_DIPSETTING(0x06, "A 1/4 B 4/1");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_hustler = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();       /* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x01, "2");
            PORT_BITX(0x02, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 64 * 16 * 16}, /* the two bitplanes are separated */
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
            new int[]{17 * 8 * 8}, /* point to letter "A" */
            new int[]{3, 0, 0, 0, 0, 0, 0}, /* I "know" that this bit of the */
            new int[]{1 * 8}, /* graphics ROMs is 1 */
            0 /* no use */
    );
    static GfxLayout armorcar_bulletlayout = new GfxLayout(
            /* there is no gfx ROM for this one, it is generated by the hardware */
            7, 1, /* 4*1 line, I think - 7*1 to position it correctly */
            1, /* just one */
            1, /* 1 bit per pixel */
            new int[]{0},
            new int[]{2, 2, 2, 2, 0, 0, 0}, /* I "know" that this bit of the */
            new int[]{8}, /* graphics ROMs is 1 */
            0 /* no use */
    );

    static GfxLayout calipso_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 1024 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout calipso_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 16 * 16}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );
    static GfxLayout backgroundlayout = new GfxLayout(
            /* there is no gfx ROM for this one, it is generated by the hardware */
            8, 8,
            32, /* one for each column */
            7, /* 128 colors max */
            new int[]{1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8 * 8, 1 * 8 * 8, 2 * 8 * 8, 3 * 8 * 8, 4 * 8 * 8, 5 * 8 * 8, 6 * 8 * 8, 7 * 8 * 8},
            new int[]{0, 8, 16, 24, 32, 40, 48, 56},
            8 * 8 * 8 /* each character takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo armorcar_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, armorcar_bulletlayout, 8 * 4, 2),
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo calipso_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, calipso_charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, calipso_spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            14318000 / 8, /* 1.78975 Mhz */
            /* Ant Eater clips if the volume is set higher than this */
            new int[]{MIXERG(16, MIXER_GAIN_2x, MIXER_PAN_CENTER), MIXERG(16, MIXER_GAIN_2x, MIXER_PAN_CENTER)},
            new ReadHandlerPtr[]{null, soundlatch_r},
            new ReadHandlerPtr[]{null, scramble_portB_r},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static AY8910interface hustler_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            14318000 / 8, /* 1.78975 Mhz */
            new int[]{80},
            new ReadHandlerPtr[]{soundlatch_r},
            new ReadHandlerPtr[]{frogger_portB_r},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_type1 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type1_readmem, type1_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /* same as the above, the only difference is in gfxdecodeinfo to have long bullets */
    static MachineDriver machine_driver_armorcar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type1_readmem, type1_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            armorcar_gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /* Rescue, Minefield and Strategy X have extra colours, and custom video initialise */
    /* routines to set up the graduated colour backgound they use */
    static MachineDriver machine_driver_rescue = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type1_readmem, type1_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 64, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 64 for background */
            rescue_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            rescue_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_minefld = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type1_readmem, type1_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 128, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 128 for background */
            minefld_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            minefld_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_stratgyx = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type2_readmem, type2_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 2, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 2 for background */
            stratgyx_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            stratgyx_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_type2 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type2_readmem, type2_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_hustler = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        hustler_readmem, hustler_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        hustler_sound_readmem, hustler_sound_writemem, hustler_sound_readport, hustler_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        hustler_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_hustlerb = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        hustlerb_readmem, hustlerb_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, hustlerb_sound_readport, hustlerb_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            scramble_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        hustler_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_calipso = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        type1_readmem, type1_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            scobra_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            calipso_gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            calipso_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
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
    static RomLoadPtr rom_scobra = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x1000, 0xa0744b3f);
            ROM_LOAD("2e", 0x1000, 0x1000, 0x8e7245cd);
            ROM_LOAD("2f", 0x2000, 0x1000, 0x47a4e6fb);
            ROM_LOAD("2h", 0x3000, 0x1000, 0x7244f21c);
            ROM_LOAD("2j", 0x4000, 0x1000, 0xe1f8a801);
            ROM_LOAD("2l", 0x5000, 0x1000, 0xd52affde);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xd4346959);
            ROM_LOAD("5d", 0x0800, 0x0800, 0xcc025d95);
            ROM_LOAD("5e", 0x1000, 0x0800, 0x1628c53f);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x0000, 0x0800, 0x64d113b4);
            ROM_LOAD("5h", 0x0800, 0x0800, 0xa96316d3);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x9b87f90d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_scobras = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("scobra2c.bin", 0x0000, 0x1000, 0xe15ade38);
            ROM_LOAD("scobra2e.bin", 0x1000, 0x1000, 0xa270e44d);
            ROM_LOAD("scobra2f.bin", 0x2000, 0x1000, 0xbdd70346);
            ROM_LOAD("scobra2h.bin", 0x3000, 0x1000, 0xdca5ec31);
            ROM_LOAD("scobra2j.bin", 0x4000, 0x1000, 0x0d8f6b6e);
            ROM_LOAD("scobra2l.bin", 0x5000, 0x1000, 0x6f80f3a9);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("snd_5c.bin", 0x0000, 0x0800, 0xdeeb0dd3);
            ROM_LOAD("snd_5d.bin", 0x0800, 0x0800, 0x872c1a74);
            ROM_LOAD("snd_5e.bin", 0x1000, 0x0800, 0xccd7a110);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x0000, 0x0800, 0x64d113b4);
            ROM_LOAD("5h", 0x0800, 0x0800, 0xa96316d3);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x9b87f90d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_scobrab = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("vid_2c.bin", 0x0000, 0x0800, 0xaeddf391);
            ROM_LOAD("vid_2e.bin", 0x0800, 0x0800, 0x72b57eb7);
            ROM_LOAD("scobra2e.bin", 0x1000, 0x1000, 0xa270e44d);
            ROM_LOAD("scobra2f.bin", 0x2000, 0x1000, 0xbdd70346);
            ROM_LOAD("scobra2h.bin", 0x3000, 0x1000, 0xdca5ec31);
            ROM_LOAD("scobra2j.bin", 0x4000, 0x1000, 0x0d8f6b6e);
            ROM_LOAD("scobra2l.bin", 0x5000, 0x1000, 0x6f80f3a9);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("snd_5c.bin", 0x0000, 0x0800, 0xdeeb0dd3);
            ROM_LOAD("snd_5d.bin", 0x0800, 0x0800, 0x872c1a74);
            ROM_LOAD("snd_5e.bin", 0x1000, 0x0800, 0xccd7a110);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x0000, 0x0800, 0x64d113b4);
            ROM_LOAD("5h", 0x0800, 0x0800, 0xa96316d3);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x9b87f90d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_stratgyx = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c_1.bin", 0x0000, 0x1000, 0xeec01237);
            ROM_LOAD("2e_2.bin", 0x1000, 0x1000, 0x926cb2d5);
            ROM_LOAD("2f_3.bin", 0x2000, 0x1000, 0x849e2504);
            ROM_LOAD("2h_4.bin", 0x3000, 0x1000, 0x8a64069b);
            ROM_LOAD("2j_5.bin", 0x4000, 0x1000, 0x78b9b898);
            ROM_LOAD("2l_6.bin", 0x5000, 0x1000, 0x20bae414);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound code */

            ROM_LOAD("s1.bin", 0x0000, 0x1000, 0x713a5db8);
            ROM_LOAD("s2.bin", 0x1000, 0x1000, 0x46079411);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f_c2.bin", 0x0000, 0x0800, 0x7121b679);
            ROM_LOAD("5h_c1.bin", 0x0800, 0x0800, 0xd105ad91);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("strategy.6e", 0x0000, 0x0020, 0x51a629e1);
            ROM_END();
        }
    };

    static RomLoadPtr rom_stratgys = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c.cpu", 0x0000, 0x1000, 0xf2aaaf2b);
            ROM_LOAD("2e.cpu", 0x1000, 0x1000, 0x5873fdc8);
            ROM_LOAD("2f.cpu", 0x2000, 0x1000, 0x532d604f);
            ROM_LOAD("2h.cpu", 0x3000, 0x1000, 0x82b1d95e);
            ROM_LOAD("2j.cpu", 0x4000, 0x1000, 0x66e84cde);
            ROM_LOAD("2l.cpu", 0x5000, 0x1000, 0x62b032d0);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound code */

            ROM_LOAD("s1.bin", 0x0000, 0x1000, 0x713a5db8);
            ROM_LOAD("s2.bin", 0x1000, 0x1000, 0x46079411);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f.cpu", 0x0000, 0x0800, 0xf4aa5ddd);
            ROM_LOAD("5h.cpu", 0x0800, 0x0800, 0x548e4635);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("strategy.6e", 0x0000, 0x0020, 0x51a629e1);
            ROM_END();
        }
    };

    static RomLoadPtr rom_armorcar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("cpu.2c", 0x0000, 0x1000, 0x0d7bfdfb);
            ROM_LOAD("cpu.2e", 0x1000, 0x1000, 0x76463213);
            ROM_LOAD("cpu.2f", 0x2000, 0x1000, 0x2cc6d5f0);
            ROM_LOAD("cpu.2h", 0x3000, 0x1000, 0x61278dbb);
            ROM_LOAD("cpu.2j", 0x4000, 0x1000, 0xfb158d8c);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sound.5c", 0x0000, 0x0800, 0x54ee7753);
            ROM_LOAD("sound.5d", 0x0800, 0x0800, 0x5218fec0);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cpu.5f", 0x0000, 0x0800, 0x8a3da4d1);
            ROM_LOAD("cpu.5h", 0x0800, 0x0800, 0x85bdb113);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x9b87f90d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_armorca2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x1000, 0xe393bd2f);
            ROM_LOAD("2e", 0x1000, 0x1000, 0xb7d443af);
            ROM_LOAD("2g", 0x2000, 0x1000, 0xe67380a4);
            ROM_LOAD("2h", 0x3000, 0x1000, 0x72af7b37);
            ROM_LOAD("2j", 0x4000, 0x1000, 0xe6b0dd7f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("sound.5c", 0x0000, 0x0800, 0x54ee7753);
            ROM_LOAD("sound.5d", 0x0800, 0x0800, 0x5218fec0);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cpu.5f", 0x0000, 0x0800, 0x8a3da4d1);
            ROM_LOAD("cpu.5h", 0x0800, 0x0800, 0x85bdb113);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x9b87f90d);
            ROM_END();
        }
    };

    static RomLoadPtr rom_moonwar2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("mw2.2c", 0x0000, 0x1000, 0x7c11b4d9);
            ROM_LOAD("mw2.2e", 0x1000, 0x1000, 0x1b6362be);
            ROM_LOAD("mw2.2f", 0x2000, 0x1000, 0x4fd8ba4b);
            ROM_LOAD("mw2.2h", 0x3000, 0x1000, 0x56879f0d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("mw2.5c", 0x0000, 0x0800, 0xc26231eb);
            ROM_LOAD("mw2.5d", 0x0800, 0x0800, 0xbb48a646);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mw2.5f", 0x0000, 0x0800, 0xc5fa1aa0);
            ROM_LOAD("mw2.5h", 0x0800, 0x0800, 0xa6ccc652);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mw2.clr", 0x0000, 0x0020, 0x99614c6c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_monwar2a = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x1000, 0xbc20b734);
            ROM_LOAD("2e", 0x1000, 0x1000, 0xdb6ffec2);
            ROM_LOAD("2f", 0x2000, 0x1000, 0x378931b8);
            ROM_LOAD("2h", 0x3000, 0x1000, 0x031dbc2c);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("mw2.5c", 0x0000, 0x0800, 0xc26231eb);
            ROM_LOAD("mw2.5d", 0x0800, 0x0800, 0xbb48a646);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mw2.5f", 0x0000, 0x0800, 0xc5fa1aa0);
            ROM_LOAD("mw2.5h", 0x0800, 0x0800, 0xa6ccc652);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mw2.clr", 0x0000, 0x0020, 0x99614c6c);
            ROM_END();
        }
    };

    static RomLoadPtr rom_spdcoin = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("spdcoin.2c", 0x0000, 0x1000, 0x65cf1e49);
            ROM_LOAD("spdcoin.2e", 0x1000, 0x1000, 0x1ee59232);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("spdcoin.5c", 0x0000, 0x0800, 0xb4cf64b7);
            ROM_LOAD("spdcoin.5d", 0x0800, 0x0800, 0x92304df0);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("spdcoin.5f", 0x0000, 0x0800, 0xdd5f1dbc);
            ROM_LOAD("spdcoin.5h", 0x0800, 0x0800, 0xab1fe81b);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("spdcoin.clr", 0x0000, 0x0020, 0x1a2ccc56);
            ROM_END();
        }
    };

    static RomLoadPtr rom_darkplnt = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("drkplt2c.dat", 0x0000, 0x1000, 0x5a0ca559);
            ROM_LOAD("drkplt2e.dat", 0x1000, 0x1000, 0x52e2117d);
            ROM_LOAD("drkplt2g.dat", 0x2000, 0x1000, 0x4093219c);
            ROM_LOAD("drkplt2j.dat", 0x3000, 0x1000, 0xb974c78d);
            ROM_LOAD("drkplt2k.dat", 0x4000, 0x1000, 0x71a37385);
            ROM_LOAD("drkplt2l.dat", 0x5000, 0x1000, 0x5ad25154);
            ROM_LOAD("drkplt2m.dat", 0x6000, 0x1000, 0x8d2f0122);
            ROM_LOAD("drkplt2p.dat", 0x7000, 0x1000, 0x2d66253b);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c.snd", 0x0000, 0x1000, 0x672b9454);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("drkplt5f.dat", 0x0000, 0x0800, 0x2af0ee66);
            ROM_LOAD("drkplt5h.dat", 0x0800, 0x0800, 0x66ef3225);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("6e.cpu", 0x0000, 0x0020, 0x86b6e124);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tazmania = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c.cpu", 0x0000, 0x1000, 0x932c5a06);
            ROM_LOAD("2e.cpu", 0x1000, 0x1000, 0xef17ce65);
            ROM_LOAD("2f.cpu", 0x2000, 0x1000, 0x43c7c39d);
            ROM_LOAD("2h.cpu", 0x3000, 0x1000, 0xbe829694);
            ROM_LOAD("2j.cpu", 0x4000, 0x1000, 0x6e197271);
            ROM_LOAD("2k.cpu", 0x5000, 0x1000, 0xa1eb453b);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("rom0.snd", 0x0000, 0x0800, 0xb8d741f1);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f.cpu", 0x0000, 0x0800, 0x2c5b612b);
            ROM_LOAD("5h.cpu", 0x0800, 0x0800, 0x3f5ff3ac);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("colr6f.cpu", 0x0000, 0x0020, 0xfce333c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tazmani2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2ck.cpu", 0x0000, 0x1000, 0xbf0492bf);
            ROM_LOAD("2ek.cpu", 0x1000, 0x1000, 0x6636c4d0);
            ROM_LOAD("2fk.cpu", 0x2000, 0x1000, 0xce59a57b);
            ROM_LOAD("2hk.cpu", 0x3000, 0x1000, 0x8bda3380);
            ROM_LOAD("2jk.cpu", 0x4000, 0x1000, 0xa4095e35);
            ROM_LOAD("2kk.cpu", 0x5000, 0x1000, 0xf308ca36);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("rom0.snd", 0x0000, 0x0800, 0xb8d741f1);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f.cpu", 0x0000, 0x0800, 0x2c5b612b);
            ROM_LOAD("5h.cpu", 0x0800, 0x0800, 0x3f5ff3ac);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("colr6f.cpu", 0x0000, 0x0020, 0xfce333c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_calipso = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("calipso.2c", 0x0000, 0x1000, 0x0fcb703c);
            ROM_LOAD("calipso.2e", 0x1000, 0x1000, 0xc6622f14);
            ROM_LOAD("calipso.2f", 0x2000, 0x1000, 0x7bacbaba);
            ROM_LOAD("calipso.2h", 0x3000, 0x1000, 0xa3a8111b);
            ROM_LOAD("calipso.2j", 0x4000, 0x1000, 0xfcbd7b9e);
            ROM_LOAD("calipso.2l", 0x5000, 0x1000, 0xf7630cab);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for sound code */

            ROM_LOAD("calipso.5c", 0x0000, 0x0800, 0x9cbc65ab);
            ROM_LOAD("calipso.5d", 0x0800, 0x0800, 0xa225ee3b);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("calipso.5f", 0x0000, 0x2000, 0xfd4252e9);
            ROM_LOAD("calipso.5h", 0x2000, 0x2000, 0x1663a73a);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("calipso.clr", 0x0000, 0x0020, 0x01165832);
            ROM_END();
        }
    };

    static RomLoadPtr rom_anteater = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ra1-2c", 0x0000, 0x1000, 0x58bc9393);
            ROM_LOAD("ra1-2e", 0x1000, 0x1000, 0x574fc6f6);
            ROM_LOAD("ra1-2f", 0x2000, 0x1000, 0x2f7c1fe5);
            ROM_LOAD("ra1-2h", 0x3000, 0x1000, 0xae8a5da3);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ra4-5c", 0x0000, 0x0800, 0x87300b4f);
            ROM_LOAD("ra4-5d", 0x0800, 0x0800, 0xaf4e5ffe);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ra6-5f", 0x1000, 0x0800, 0x4c3f8a08);/* we load the roms at 0x1000-0x1fff, they */

            ROM_LOAD("ra6-5h", 0x1800, 0x0800, 0xb30c7c9f);/* will be decrypted at 0x0000-0x0fff */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("colr6f.cpu", 0x0000, 0x0020, 0xfce333c7);
            ROM_END();
        }
    };

    static RomLoadPtr rom_rescue = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("rb15acpu.bin", 0x0000, 0x1000, 0xd7e654ba);
            ROM_LOAD("rb15bcpu.bin", 0x1000, 0x1000, 0xa93ea158);
            ROM_LOAD("rb15ccpu.bin", 0x2000, 0x1000, 0x058cd3d0);
            ROM_LOAD("rb15dcpu.bin", 0x3000, 0x1000, 0xd6505742);
            ROM_LOAD("rb15ecpu.bin", 0x4000, 0x1000, 0x604df3a4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("rb15csnd.bin", 0x0000, 0x0800, 0x8b24bf17);
            ROM_LOAD("rb15dsnd.bin", 0x0800, 0x0800, 0xd96e4fb3);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rb15fcpu.bin", 0x1000, 0x0800, 0x4489d20c);/* we load the roms at 0x1000-0x1fff, they */

            ROM_LOAD("rb15hcpu.bin", 0x1800, 0x0800, 0x5512c547);/* will be decrypted at 0x0000-0x0fff */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("rescue.clr", 0x0000, 0x0020, 0x40c6bcbd);
            ROM_END();
        }
    };

    static RomLoadPtr rom_minefld = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ma22c", 0x0000, 0x1000, 0x1367a035);
            ROM_LOAD("ma22e", 0x1000, 0x1000, 0x68946d21);
            ROM_LOAD("ma22f", 0x2000, 0x1000, 0x7663aee5);
            ROM_LOAD("ma22h", 0x3000, 0x1000, 0x9787475d);
            ROM_LOAD("ma22j", 0x4000, 0x1000, 0x2ceceb54);
            ROM_LOAD("ma22l", 0x5000, 0x1000, 0x85138fc9);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ma15c", 0x0000, 0x0800, 0x8bef736b);
            ROM_LOAD("ma15d", 0x0800, 0x0800, 0xf67b3f97);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ma15f", 0x1000, 0x0800, 0x9f703006);/* we load the roms at 0x1000-0x1fff, they */

            ROM_LOAD("ma15h", 0x1800, 0x0800, 0xed0dccb1);/* will be decrypted at 0x0000-0x0fff */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("minefld.clr", 0x0000, 0x0020, 0x1877368e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_losttomb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x1000, 0xd6176d2c);
            ROM_LOAD("2e", 0x1000, 0x1000, 0xa5f55f4a);
            ROM_LOAD("2f", 0x2000, 0x1000, 0x0169fa3c);
            ROM_LOAD("2h-easy", 0x3000, 0x1000, 0x054481b6);
            ROM_LOAD("2j", 0x4000, 0x1000, 0x249ee040);
            ROM_LOAD("2l", 0x5000, 0x1000, 0xc7d2e608);
            ROM_LOAD("2m", 0x6000, 0x1000, 0xbc4bc5b1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xb899be2a);
            ROM_LOAD("5d", 0x0800, 0x0800, 0x6907af31);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x1000, 0x0800, 0x61f137e7);/* we load the roms at 0x1000-0x1fff, they */

            ROM_LOAD("5h", 0x1800, 0x0800, 0x5581de5f);/* will be decrypted at 0x0000-0x0fff */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("ltprom", 0x0000, 0x0020, 0x1108b816);
            ROM_END();
        }
    };

    static RomLoadPtr rom_losttmbh = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x1000, 0xd6176d2c);
            ROM_LOAD("2e", 0x1000, 0x1000, 0xa5f55f4a);
            ROM_LOAD("2f", 0x2000, 0x1000, 0x0169fa3c);
            ROM_LOAD("lthard", 0x3000, 0x1000, 0xe32cbf0e);
            ROM_LOAD("2j", 0x4000, 0x1000, 0x249ee040);
            ROM_LOAD("2l", 0x5000, 0x1000, 0xc7d2e608);
            ROM_LOAD("2m", 0x6000, 0x1000, 0xbc4bc5b1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xb899be2a);
            ROM_LOAD("5d", 0x0800, 0x0800, 0x6907af31);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x1000, 0x0800, 0x61f137e7);/* we load the roms at 0x1000-0x1fff, they */

            ROM_LOAD("5h", 0x1800, 0x0800, 0x5581de5f);/* will be decrypted at 0x0000-0x0fff */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("ltprom", 0x0000, 0x0020, 0x1108b816);
            ROM_END();
        }
    };

    static RomLoadPtr rom_superbon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2d.cpu", 0x0000, 0x1000, 0x60c0ba18);
            ROM_LOAD("2e.cpu", 0x1000, 0x1000, 0xddcf44bf);
            ROM_LOAD("2f.cpu", 0x2000, 0x1000, 0xbb66c2d5);
            ROM_LOAD("2h.cpu", 0x3000, 0x1000, 0x74f4f04d);
            ROM_LOAD("2j.cpu", 0x4000, 0x1000, 0x78effb08);
            ROM_LOAD("2l.cpu", 0x5000, 0x1000, 0xe9dcecbd);
            ROM_LOAD("2m.cpu", 0x6000, 0x1000, 0x3ed0337e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xb899be2a);
            ROM_LOAD("5d.snd", 0x0800, 0x0800, 0x80640a04);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f.cpu", 0x0000, 0x0800, 0x5b9d4686);
            ROM_LOAD("5h.cpu", 0x0800, 0x0800, 0x58c29927);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("superbon.clr", 0x0000, 0x0020, 0x00000000);
            ROM_END();
        }
    };

    static RomLoadPtr rom_hustler = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("hustler.1", 0x0000, 0x1000, 0x94479a3e);
            ROM_LOAD("hustler.2", 0x1000, 0x1000, 0x3cc67bcc);
            ROM_LOAD("hustler.3", 0x2000, 0x1000, 0x9422226a);
            /* 3000-3fff space for diagnostics ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("hustler.6", 0x0000, 0x0800, 0x7a946544);
            ROM_LOAD("hustler.7", 0x0800, 0x0800, 0x3db57351);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hustler.5f", 0x0000, 0x0800, 0x0bdfad0e);
            ROM_LOAD("hustler.5h", 0x0800, 0x0800, 0x8e062177);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("hustler.clr", 0x0000, 0x0020, 0xaa1f7f5e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_billiard = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("a", 0x0000, 0x1000, 0xb7eb50c0);
            ROM_LOAD("b", 0x1000, 0x1000, 0x988fe1c5);
            ROM_LOAD("c", 0x2000, 0x1000, 0x7b8de793);
            /* 3000-3fff space for diagnostics ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("hustler.6", 0x0000, 0x0800, 0x7a946544);
            ROM_LOAD("hustler.7", 0x0800, 0x0800, 0x3db57351);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hustler.5f", 0x0000, 0x0800, 0x0bdfad0e);
            ROM_LOAD("hustler.5h", 0x0800, 0x0800, 0x8e062177);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("hustler.clr", 0x0000, 0x0020, 0xaa1f7f5e);
            ROM_END();
        }
    };

    /* this is identical to billiard, but with a different memory map */
    static RomLoadPtr rom_hustlerb = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("hustler.2c", 0x0000, 0x1000, 0x3a1ac6a9);
            ROM_LOAD("hustler.2f", 0x1000, 0x1000, 0xdc6752ec);
            ROM_LOAD("hustler.2j", 0x2000, 0x1000, 0x27c1e0f8);
            /* 3000-3fff space for diagnostics ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("hustler.11d", 0x0000, 0x0800, 0xb559bfde);
            ROM_LOAD("hustler.10d", 0x0800, 0x0800, 0x6ef96cfb);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hustler.5f", 0x0000, 0x0800, 0x0bdfad0e);
            ROM_LOAD("hustler.5h", 0x0800, 0x0800, 0x8e062177);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("hustler.clr", 0x0000, 0x0020, 0xaa1f7f5e);
            ROM_END();
        }
    };

    public static InitDriverPtr init_moonwar2 = new InitDriverPtr() {
        public void handler() {
            /* Install special handler for the spinner */
            install_mem_read_handler(0, 0x9800, 0x9800, moonwar2_IN0_r);
        }
    };

    static int bit(int i, int n) {
        return ((i >> n) & 1);
    }

    public static InitDriverPtr init_anteater = new InitDriverPtr() {
        public void handler() {
            /*
             *   Code To Decode Lost Tomb by Mirko Buffoni
             *   Optimizations done by Fabio Buffoni
             */
            int i, j;
            UBytePtr RAM;

            /* The gfx ROMs are scrambled. Decode them. They have been loaded at 0x1000, */
            /* we write them at 0x0000. */
            RAM = memory_region(REGION_GFX1);

            for (i = 0; i < 0x1000; i++) {
                j = i & 0x9bf;
                j |= (bit(i, 4) ^ bit(i, 9) ^ (bit(i, 2) & bit(i, 10))) << 6;
                j |= (bit(i, 2) ^ bit(i, 10)) << 9;
                j |= (bit(i, 0) ^ bit(i, 6) ^ 1) << 10;
                RAM.write(i, RAM.read(j + 0x1000));
            }
        }
    };

    public static InitDriverPtr init_rescue = new InitDriverPtr() {
        public void handler() {
            /*
             *   Code To Decode Lost Tomb by Mirko Buffoni
             *   Optimizations done by Fabio Buffoni
             */
            int i, j;
            UBytePtr RAM;

            /* The gfx ROMs are scrambled. Decode them. They have been loaded at 0x1000, */
            /* we write them at 0x0000. */
            RAM = memory_region(REGION_GFX1);

            for (i = 0; i < 0x1000; i++) {
                j = i & 0xa7f;
                j |= (bit(i, 3) ^ bit(i, 10)) << 7;
                j |= (bit(i, 1) ^ bit(i, 7)) << 8;
                j |= (bit(i, 0) ^ bit(i, 8)) << 10;
                RAM.write(i, RAM.read(j + 0x1000));
            }
        }
    };

    public static InitDriverPtr init_minefld = new InitDriverPtr() {
        public void handler() {
            /*
             *   Code To Decode Minefield by Mike Balfour and Nicola Salmoria
             */
            int i, j;
            UBytePtr RAM;

            /* The gfx ROMs are scrambled. Decode them. They have been loaded at 0x1000, */
            /* we write them at 0x0000. */
            RAM = memory_region(REGION_GFX1);

            for (i = 0; i < 0x1000; i++) {
                j = i & 0xd5f;
                j |= (bit(i, 3) ^ bit(i, 7)) << 5;
                j |= (bit(i, 2) ^ bit(i, 9) ^ (bit(i, 0) & bit(i, 5))
                        ^ (bit(i, 3) & bit(i, 7) & (bit(i, 0) ^ bit(i, 5)))) << 7;
                j |= (bit(i, 0) ^ bit(i, 5) ^ (bit(i, 3) & bit(i, 7))) << 9;
                RAM.write(i, RAM.read(j + 0x1000));
            }
        }
    };

    public static InitDriverPtr init_losttomb = new InitDriverPtr() {
        public void handler() {
            /*
             *   Code To Decode Lost Tomb by Mirko Buffoni
             *   Optimizations done by Fabio Buffoni
             */
            int i, j;
            UBytePtr RAM;

            /* The gfx ROMs are scrambled. Decode them. They have been loaded at 0x1000, */
            /* we write them at 0x0000. */
            RAM = memory_region(REGION_GFX1);

            for (i = 0; i < 0x1000; i++) {
                j = i & 0xa7f;
                j |= ((bit(i, 1) & bit(i, 8)) | ((1 ^ bit(i, 1)) & (bit(i, 10)))) << 7;
                j |= (bit(i, 7) ^ (bit(i, 1) & (bit(i, 7) ^ bit(i, 10)))) << 8;
                j |= ((bit(i, 1) & bit(i, 7)) | ((1 ^ bit(i, 1)) & (bit(i, 8)))) << 10;
                RAM.write(i, RAM.read(j + 0x1000));
            }
        }
    };

    public static InitDriverPtr init_superbon = new InitDriverPtr() {
        public void handler() {
            /*
             *   Code rom deryption worked out by hand by Chris Hardy.
             */
            int i;
            UBytePtr RAM;

            RAM = memory_region(REGION_CPU1);

            for (i = 0; i < 0x1000; i++) {
                /* Code is encrypted depending on bit 7 and 9 of the address */
                switch (i & 0x280) {
                    case 0x000:
                        RAM.write(i, RAM.read(i) ^ 0x92);
                        break;
                    case 0x080:
                        RAM.write(i, RAM.read(i) ^ 0x82);
                        break;
                    case 0x200:
                        RAM.write(i, RAM.read(i) ^ 0x12);
                        break;
                    case 0x280:
                        RAM.write(i, RAM.read(i) ^ 0x10);
                        break;
                }
            }
        }
    };

    public static InitDriverPtr init_hustler = new InitDriverPtr() {
        public void handler() {
            int A;

            for (A = 0; A < 0x4000; A++) {
                char xormask;
                int[] bits = new int[8];
                int i;
                UBytePtr RAM = memory_region(REGION_CPU1);

                for (i = 0; i < 8; i++) {
                    bits[i] = (A >> i) & 1;
                }

                xormask = 0xff;
                if ((bits[0] ^ bits[1]) != 0) {
                    xormask ^= 0x01;
                }
                if ((bits[3] ^ bits[6]) != 0) {
                    xormask ^= 0x02;
                }
                if ((bits[4] ^ bits[5]) != 0) {
                    xormask ^= 0x04;
                }
                if ((bits[0] ^ bits[2]) != 0) {
                    xormask ^= 0x08;
                }
                if ((bits[2] ^ bits[3]) != 0) {
                    xormask ^= 0x10;
                }
                if ((bits[1] ^ bits[5]) != 0) {
                    xormask ^= 0x20;
                }
                if ((bits[0] ^ bits[7]) != 0) {
                    xormask ^= 0x40;
                }
                if ((bits[4] ^ bits[6]) != 0) {
                    xormask ^= 0x80;
                }

                RAM.write(A, RAM.read(A) ^ xormask);
            }

            /* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
            {
                UBytePtr RAM = memory_region(REGION_CPU2);

                for (A = 0; A < 0x0800; A++) {
                    RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
                }
            }
        }
    };

    public static InitDriverPtr init_billiard = new InitDriverPtr() {
        public void handler() {
            int A;

            for (A = 0; A < 0x4000; A++) {
                char xormask;
                int[] bits = new int[8];
                int i;
                UBytePtr RAM = memory_region(REGION_CPU1);

                for (i = 0; i < 8; i++) {
                    bits[i] = (A >> i) & 1;
                }

                xormask = 0x55;
                if ((bits[2] ^ (bits[3] & bits[6])) != 0) {
                    xormask ^= 0x01;
                }
                if ((bits[4] ^ (bits[5] & bits[7])) != 0) {
                    xormask ^= 0x02;
                }
                if ((bits[0] ^ (bits[7] & NOT(bits[3]))) != 0) {
                    xormask ^= 0x04;
                }
                if ((bits[3] ^ (NOT(bits[0]) & bits[2])) != 0) {
                    xormask ^= 0x08;
                }
                if ((bits[5] ^ (NOT(bits[4]) & bits[1])) != 0) {
                    xormask ^= 0x10;
                }
                if ((bits[6] ^ (NOT(bits[2]) & NOT(bits[5]))) != 0) {
                    xormask ^= 0x20;
                }
                if ((bits[1] ^ (NOT(bits[6]) & NOT(bits[4]))) != 0) {
                    xormask ^= 0x40;
                }
                if ((bits[7] ^ (NOT(bits[1]) & bits[0])) != 0) {
                    xormask ^= 0x80;
                }

                RAM.write(A, RAM.read(A) ^ xormask);

                for (i = 0; i < 8; i++) {
                    bits[i] = (RAM.read(A) >> i) & 1;
                }

                RAM.write(A,
                        (bits[7] << 0)
                        + (bits[0] << 1)
                        + (bits[3] << 2)
                        + (bits[4] << 3)
                        + (bits[5] << 4)
                        + (bits[2] << 5)
                        + (bits[1] << 6)
                        + (bits[6] << 7));
            }

            /* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
            {
                UBytePtr RAM = memory_region(REGION_CPU2);

                for (A = 0; A < 0x0800; A++) {
                    RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
                }
            }
        }
    };

    public static GameDriver driver_scobra = new GameDriver("1981", "scobra", "scobra.java", rom_scobra, null, machine_driver_type1, input_ports_scobrak, null, ROT90, "Konami", "Super Cobra");
    public static GameDriver driver_scobras = new GameDriver("1981", "scobras", "scobra.java", rom_scobras, driver_scobra, machine_driver_type1, input_ports_scobra, null, ROT90, "[Konami] (Stern license)", "Super Cobra (Stern)");
    public static GameDriver driver_scobrab = new GameDriver("1981", "scobrab", "scobra.java", rom_scobrab, driver_scobra, machine_driver_type1, input_ports_scobra, null, ROT90, "bootleg", "Super Cobra (bootleg)");
    public static GameDriver driver_stratgyx = new GameDriver("1981", "stratgyx", "scobra.java", rom_stratgyx, null, machine_driver_stratgyx, input_ports_stratgyx, null, ROT0, "Konami", "Strategy X");
    public static GameDriver driver_stratgys = new GameDriver("1981", "stratgys", "scobra.java", rom_stratgys, driver_stratgyx, machine_driver_stratgyx, input_ports_stratgyx, null, ROT0, "[Konami] (Stern license)", "Strategy X (Stern)");
    public static GameDriver driver_armorcar = new GameDriver("1981", "armorcar", "scobra.java", rom_armorcar, null, machine_driver_armorcar, input_ports_armorcar, null, ROT90, "Stern", "Armored Car (set 1)");
    public static GameDriver driver_armorca2 = new GameDriver("1981", "armorca2", "scobra.java", rom_armorca2, driver_armorcar, machine_driver_armorcar, input_ports_armorcar, null, ROT90, "Stern", "Armored Car (set 2)");
    public static GameDriver driver_moonwar2 = new GameDriver("1981", "moonwar2", "scobra.java", rom_moonwar2, null, machine_driver_type1, input_ports_moonwar2, init_moonwar2, ROT90, "Stern", "Moon War II (set 1)");
    public static GameDriver driver_monwar2a = new GameDriver("1981", "monwar2a", "scobra.java", rom_monwar2a, driver_moonwar2, machine_driver_type1, input_ports_monwar2a, init_moonwar2, ROT90, "Stern", "Moon War II (set 2)");
    public static GameDriver driver_spdcoin = new GameDriver("1984", "spdcoin", "scobra.java", rom_spdcoin, null, machine_driver_type1, input_ports_spdcoin, null, ROT90, "Stern", "Speed Coin (prototype)");
    public static GameDriver driver_darkplnt = new GameDriver("1982", "darkplnt", "scobra.java", rom_darkplnt, null, machine_driver_type2, input_ports_darkplnt, null, ROT180, "Stern", "Dark Planet", GAME_NOT_WORKING);
    public static GameDriver driver_tazmania = new GameDriver("1982", "tazmania", "scobra.java", rom_tazmania, null, machine_driver_type1, input_ports_tazmania, null, ROT90, "Stern", "Tazz-Mania (Scramble hardware)");
    public static GameDriver driver_tazmani2 = new GameDriver("1982", "tazmani2", "scobra.java", rom_tazmani2, driver_tazmania, machine_driver_type2, input_ports_tazmania, null, ROT90, "Stern", "Tazz-Mania (Strategy X hardware)");
    public static GameDriver driver_calipso = new GameDriver("1982", "calipso", "scobra.java", rom_calipso, null, machine_driver_calipso, input_ports_calipso, null, ROT90, "[Stern] (Tago license)", "Calipso");
    public static GameDriver driver_anteater = new GameDriver("1982", "anteater", "scobra.java", rom_anteater, null, machine_driver_type1, input_ports_anteater, init_anteater, ROT90, "[Stern] (Tago license)", "Anteater");
    public static GameDriver driver_rescue = new GameDriver("1982", "rescue", "scobra.java", rom_rescue, null, machine_driver_rescue, input_ports_rescue, init_rescue, ROT90, "Stern", "Rescue");
    public static GameDriver driver_minefld = new GameDriver("1983", "minefld", "scobra.java", rom_minefld, null, machine_driver_minefld, input_ports_minefld, init_minefld, ROT90, "Stern", "Minefield");
    public static GameDriver driver_losttomb = new GameDriver("1982", "losttomb", "scobra.java", rom_losttomb, null, machine_driver_type1, input_ports_losttomb, init_losttomb, ROT90, "Stern", "Lost Tomb (easy)");
    public static GameDriver driver_losttmbh = new GameDriver("1982", "losttmbh", "scobra.java", rom_losttmbh, driver_losttomb, machine_driver_type1, input_ports_losttomb, init_losttomb, ROT90, "Stern", "Lost Tomb (hard)");
    public static GameDriver driver_superbon = new GameDriver("1982", "superbon", "scobra.java", rom_superbon, null, machine_driver_type1, input_ports_superbon, init_superbon, ROT90, "bootleg", "Super Bond");
    public static GameDriver driver_hustler = new GameDriver("1981", "hustler", "scobra.java", rom_hustler, null, machine_driver_hustler, input_ports_hustler, init_hustler, ROT90, "Konami", "Video Hustler");
    public static GameDriver driver_billiard = new GameDriver("1981", "billiard", "scobra.java", rom_billiard, driver_hustler, machine_driver_hustler, input_ports_hustler, init_billiard, ROT90, "bootleg", "The Billiards");
    public static GameDriver driver_hustlerb = new GameDriver("1981", "hustlerb", "scobra.java", rom_hustlerb, driver_hustler, machine_driver_hustlerb, input_ports_hustler, null, ROT90, "bootleg", "Video Hustler (bootleg)");
}
