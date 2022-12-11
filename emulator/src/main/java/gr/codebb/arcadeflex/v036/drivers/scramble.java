/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.sndhrdw.scramble.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.sndhrdw.frogger.*;
import static arcadeflex.v036.vidhrdw.galaxian.*;
import static gr.codebb.arcadeflex.v036.machine.scramble.*;
import static arcadeflex.v036.drivers.cclimber.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.drivers.frogger.*;
import static arcadeflex.v036.drivers.amidar.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_readmem16;
import static gr.codebb.arcadeflex.v037b7.mame.memory.cpu_writemem16;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;

public class scramble {

    public static ReadHandlerPtr ckongs_input_port_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(1) & 0xfc) | ((readinputport(2) & 0x06) >> 1);
        }
    };

    public static ReadHandlerPtr ckongs_input_port_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(2) & 0xf9) | ((readinputport(1) & 0x03) << 1);
        }
    };

    public static WriteHandlerPtr scramble_coin_counter_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(1, data);
        }
    };

    public static WriteHandlerPtr scramble_coin_counter_3_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(2, data);
        }
    };

    static MemoryReadAddress scramble_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x4c00, 0x4fff, videoram_r), /* mirror address */
                new MemoryReadAddress(0x5000, 0x507f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x7000, 0x7000, watchdog_reset_r),
                new MemoryReadAddress(0x7800, 0x7800, watchdog_reset_r),
                new MemoryReadAddress(0x8100, 0x8100, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x8101, 0x8101, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x8102, 0x8102, input_port_2_r), /* IN2 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress ckongs_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x6bff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0x7000, 0x7000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x7001, 0x7001, ckongs_input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x7002, 0x7002, ckongs_input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM), /* Video RAM */
                new MemoryReadAddress(0x9800, 0x987f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0xb000, 0xb000, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    /* Extra ROM and protection locations */
    static MemoryReadAddress mariner_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x4c00, 0x4fff, videoram_r), /* mirror address */
                new MemoryReadAddress(0x5000, 0x507f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x5800, 0x67ff, MRA_ROM),
                new MemoryReadAddress(0x7000, 0x7000, watchdog_reset_r),
                new MemoryReadAddress(0x8100, 0x8100, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x8101, 0x8101, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x8102, 0x8102, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x9008, 0x9008, mariner_protection_2_r),
                new MemoryReadAddress(0xb401, 0xb401, mariner_protection_1_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress mars_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x4c00, 0x4fff, videoram_r), /* mirror address */
                new MemoryReadAddress(0x5000, 0x507f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x7000, 0x7000, watchdog_reset_r),
                new MemoryReadAddress(0x8100, 0x8100, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x8102, 0x8102, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x8108, 0x8108, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x8208, 0x8208, input_port_3_r), /* IN3 */
                new MemoryReadAddress(0xa000, 0xafff, MRA_ROM), /* Sinbad 7 */
                new MemoryReadAddress(0xc100, 0xc100, input_port_0_r), /* IN0 - Sinbad 7 */
                new MemoryReadAddress(0xc102, 0xc102, input_port_1_r), /* IN1 - Sinbad 7 */
                new MemoryReadAddress(0xc108, 0xc108, input_port_2_r), /* IN2 - Sinbad 7 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress hotshock_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4bff, MRA_RAM), /* RAM and Video RAM */
                new MemoryReadAddress(0x4c00, 0x4fff, videoram_r), /* mirror address */
                new MemoryReadAddress(0x5000, 0x507f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x8000, 0x8000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x8001, 0x8001, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x8002, 0x8002, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x8003, 0x8003, input_port_3_r), /* IN3 */
                new MemoryReadAddress(-1) /* end of table */};
    public static ReadHandlerPtr hunchbks_mirror_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cpu_readmem16(0x1000 + offset);
        }
    };
    public static WriteHandlerPtr hunchbks_mirror_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_writemem16(0x1000 + offset, data);
        }
    };

    static MemoryReadAddress hunchbks_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x2fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x6fff, MRA_ROM),
                new MemoryReadAddress(0x1c00, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x1800, 0x1bff, videoram_r),
                new MemoryReadAddress(0x1400, 0x147f, MRA_RAM), /* screen attributes, sprites, bullets */
                new MemoryReadAddress(0x1500, 0x1500, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x1501, 0x1501, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x1502, 0x1502, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x1680, 0x1680, watchdog_reset_r),
                new MemoryReadAddress(0x3000, 0x3fff, hunchbks_mirror_r),
                new MemoryReadAddress(0x5000, 0x5fff, hunchbks_mirror_r),
                new MemoryReadAddress(0x7000, 0x7fff, hunchbks_mirror_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress scramble_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x4800, 0x4bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x5000, 0x503f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x5040, 0x505f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x6801, 0x6801, interrupt_enable_w),
                new MemoryWriteAddress(0x6802, 0x6802, coin_counter_w),
                new MemoryWriteAddress(0x6803, 0x6803, scramble_background_w),
                new MemoryWriteAddress(0x6804, 0x6804, galaxian_stars_w),
                new MemoryWriteAddress(0x6806, 0x6806, galaxian_flipx_w),
                new MemoryWriteAddress(0x6807, 0x6807, galaxian_flipy_w),
                new MemoryWriteAddress(0x8200, 0x8200, soundlatch_w),
                new MemoryWriteAddress(0x8201, 0x8201, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress triplep_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x4800, 0x4bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4c00, 0x4fff, videoram_w), /* mirror address */
                new MemoryWriteAddress(0x5000, 0x503f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x5040, 0x505f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x6801, 0x6801, interrupt_enable_w),
                new MemoryWriteAddress(0x6802, 0x6802, coin_counter_w),
                new MemoryWriteAddress(0x6803, 0x6803, MWA_NOP), /* ??? (it's NOT a background enable) */
                new MemoryWriteAddress(0x6804, 0x6804, galaxian_stars_w),
                new MemoryWriteAddress(0x6806, 0x6806, galaxian_flipx_w),
                new MemoryWriteAddress(0x6807, 0x6807, galaxian_flipy_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress ckongs_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x6000, 0x6bff, MWA_RAM),
                new MemoryWriteAddress(0x7800, 0x7800, soundlatch_w),
                new MemoryWriteAddress(0x7801, 0x7801, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9800, 0x983f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x9840, 0x985f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9860, 0x987f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0xa801, 0xa801, interrupt_enable_w),
                new MemoryWriteAddress(0xa802, 0xa802, coin_counter_w),
                new MemoryWriteAddress(0xa804, 0xa804, galaxian_stars_w),
                new MemoryWriteAddress(0xa806, 0xa806, galaxian_flipx_w),
                new MemoryWriteAddress(0xa807, 0xa807, galaxian_flipy_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress mars_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x4800, 0x4bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x5000, 0x503f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x5040, 0x505f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x5080, 0x50ff, MWA_NOP), /* unused */
                new MemoryWriteAddress(0x6800, 0x6800, scramble_coin_counter_2_w),
                new MemoryWriteAddress(0x6801, 0x6801, galaxian_stars_w),
                new MemoryWriteAddress(0x6802, 0x6802, interrupt_enable_w),
                new MemoryWriteAddress(0x6808, 0x6808, coin_counter_w),
                new MemoryWriteAddress(0x6809, 0x6809, galaxian_flipx_w),
                new MemoryWriteAddress(0x680b, 0x680b, galaxian_flipy_w),
                new MemoryWriteAddress(0x810a, 0x810a, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x8200, 0x8200, soundlatch_w),
                new MemoryWriteAddress(0x8202, 0x8202, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(0x820a, 0x820a, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xa000, 0xafff, MWA_ROM), /* Sinbad 7 */
                new MemoryWriteAddress(0xc10a, 0xc10a, MWA_NOP), /* ??? - Sinbad 7 */
                new MemoryWriteAddress(0xc20a, 0xc20a, MWA_NOP), /* ??? - Sinbad 7 */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress hotshock_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0x4800, 0x4bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x5000, 0x503f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x5040, 0x505f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x5060, 0x507f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x6000, 0x6000, scramble_coin_counter_3_w),
                new MemoryWriteAddress(0x6002, 0x6002, scramble_coin_counter_2_w),
                new MemoryWriteAddress(0x6004, 0x6004, hotshock_flipscreen_w),
                new MemoryWriteAddress(0x6005, 0x6005, coin_counter_w),
                new MemoryWriteAddress(0x6006, 0x6006, pisces_gfxbank_w),
                new MemoryWriteAddress(0x6801, 0x6801, interrupt_enable_w),
                new MemoryWriteAddress(0x7000, 0x7000, watchdog_reset_w),
                new MemoryWriteAddress(0x8000, 0x8000, soundlatch_w),
                new MemoryWriteAddress(0x9000, 0x9000, hotshock_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress hunchbks_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x2fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x4fff, MWA_ROM),
                new MemoryWriteAddress(0x6000, 0x6fff, MWA_ROM),
                new MemoryWriteAddress(0x1800, 0x1bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1c00, 0x1fff, MWA_RAM),
                new MemoryWriteAddress(0x1400, 0x143f, galaxian_attributes_w, galaxian_attributesram),
                new MemoryWriteAddress(0x1440, 0x145f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x1460, 0x147f, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size),
                new MemoryWriteAddress(0x1606, 0x1606, galaxian_flipx_w),
                new MemoryWriteAddress(0x1607, 0x1607, galaxian_flipy_w),
                new MemoryWriteAddress(0x1210, 0x1210, soundlatch_w),
                new MemoryWriteAddress(0x1211, 0x1211, scramble_sh_irqtrigger_w),
                new MemoryWriteAddress(0x3000, 0x3fff, hunchbks_mirror_w),
                new MemoryWriteAddress(0x5000, 0x5fff, hunchbks_mirror_w),
                new MemoryWriteAddress(0x7000, 0x7fff, hunchbks_mirror_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort triplep_readport[]
            = {
                new IOReadPort(0x01, 0x01, AY8910_read_port_0_r),
                new IOReadPort(0x02, 0x02, mariner_pip),
                new IOReadPort(0x03, 0x03, mariner_pap),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort triplep_writeport[]
            = {
                new IOWritePort(0x01, 0x01, AY8910_control_port_0_w),
                new IOWritePort(0x00, 0x00, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress scramble_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress scramble_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9fff, scramble_filter_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress froggers_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x17ff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress froggers_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x17ff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                //new MemoryWriteAddress( 0x6000, 0x6fff, scramble_filter_w ),  /* There is probably a filter here,	 */
                /* but it can't possibly be the same */
                new MemoryWriteAddress(-1) /* end of table */ /* as the one in Scramble. One 8910 only */};

    static IOReadPort scramble_sound_readport[]
            = {
                new IOReadPort(0x20, 0x20, AY8910_read_port_1_r),
                new IOReadPort(0x80, 0x80, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort scramble_sound_writeport[]
            = {
                new IOWritePort(0x10, 0x10, AY8910_control_port_1_w),
                new IOWritePort(0x20, 0x20, AY8910_write_port_1_w),
                new IOWritePort(0x40, 0x40, AY8910_control_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort froggers_sound_readport[]
            = {
                new IOReadPort(0x40, 0x40, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort froggers_sound_writeport[]
            = {
                new IOWritePort(0x40, 0x40, AY8910_write_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_control_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort hotshock_sound_readport[]
            = {
                new IOReadPort(0x20, 0x20, AY8910_read_port_1_r),
                new IOReadPort(0x40, 0x40, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort hotshock_sound_writeport[]
            = {
                new IOWritePort(0x10, 0x10, AY8910_control_port_1_w),
                new IOWritePort(0x20, 0x20, AY8910_write_port_1_w),
                new IOWritePort(0x40, 0x40, AY8910_write_port_0_w),
                new IOWritePort(0x80, 0x80, AY8910_control_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_scramble = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1  B 2/1  C 1/1");
            PORT_DIPSETTING(0x02, "A 1/2  B 1/1  C 1/2");
            PORT_DIPSETTING(0x04, "A 1/3  B 3/1  C 1/3");
            PORT_DIPSETTING(0x06, "A 1/4  B 4/1  C 1/4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_atlantis = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x0e, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 1/3  B 2/1");
            PORT_DIPSETTING(0x00, "A 1/6  B 1/1");
            PORT_DIPSETTING(0x04, "A 1/99 B 1/99");
            /* all the other combos give 99 credits */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /* same as scramble, dip switches are different */
    static InputPortHandlerPtr input_ports_theend = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

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
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_froggers = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 1P shoot2 - unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 1P shoot1 - unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x02, "7");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 2P shoot2 - unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 2P shoot1 - unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2/1 B 2/1 C 2/1");
            PORT_DIPSETTING(0x04, "A 2/1 B 1/3 C 2/1");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/1 C 1/1");
            PORT_DIPSETTING(0x06, "A 1/1 B 1/6 C 1/1");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_amidars = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 1P shoot2 - unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "A 1/1 B 1/6");
            PORT_DIPSETTING(0x02, "A 2/1 B 1/3");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_triplep = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 1/2 B 1/1 C 1/2");
            PORT_DIPSETTING(0x04, "A 1/3 B 3/1 C 1/3");
            PORT_DIPSETTING(0x00, "A 1/1 B 2/1 C 1/1");
            PORT_DIPSETTING(0x06, "A 1/4 B 4/1 C 1/4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_SERVICE(0x20, IP_ACTIVE_HIGH);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BITX(0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_ckongs = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN1 */
            /* the coinage dip switch is spread across bits 0/1 of port 1 and bit 3 of port 2. */
            /* To handle that, we swap bits 0/1 of port 1 and bits 1/2 of port 2 - this is handled */
            /* by ckongs_input_port_N_r() */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();       /* IN2 */
            /* the coinage dip switch is spread across bits 0/1 of port 1 and bit 3 of port 2. */
            /* To handle that, we swap bits 0/1 of port 1 and bits 1/2 of port 2 - this is handled */
            /* by ckongs_input_port_N_r() */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x0e, 0x0e, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_mars = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY | IPF_COCKTAIL); /* this also control cocktail mode */

            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "3");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "255", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);

            PORT_START(); 	/* IN3 */

            PORT_BIT(0x1f, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY | IPF_COCKTAIL);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_devilfsh = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x01, "15000");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN3 - unused */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_newsin7 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, " A 1C/1C  B 2C/1C");
            PORT_DIPSETTING(0x01, " A 1C/3C  B 3C/1C");
            PORT_DIPSETTING(0x02, " A 1C/2C  B 1C/1C");
            PORT_DIPSETTING(0x00, " A 1C/4C  B 4C/1C");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x02, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));  /* difficulty? */

            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x08, "5");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN3 - unused */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_hotshock = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0xc0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* pressing this disables the coins */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_DIPNAME(0x0f, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0b, DEF_STR("2C_4C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("2C_6C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0x0f, DEF_STR("2C_8C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_8C"));
            PORT_DIPNAME(0xf0, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xb0, DEF_STR("2C_4C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("2C_6C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0xf0, DEF_STR("2C_8C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_8C"));

            PORT_START(); 	/* IN3 */

            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x04, 0x04, "Language");
            PORT_DIPSETTING(0x04, "English");
            PORT_DIPSETTING(0x00, "Italian");
            PORT_DIPNAME(0x18, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "75000");
            PORT_DIPSETTING(0x08, "150000");
            PORT_DIPSETTING(0x10, "200000");
            PORT_DIPSETTING(0x18, "None");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_hunchbks = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2/1 B 1/3");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/5");
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x04, "40000");
            PORT_DIPSETTING(0x06, "80000");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* protection check? */

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
    static GfxLayout mariner_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 512 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout mariner_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 128 * 16 * 16}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );
    static GfxLayout devilfsh_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 2 * 256 * 8 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout devilfsh_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 2 * 64 * 16 * 16}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );
    static GfxLayout newsin7_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            3, /* 3 bits per pixel */
            new int[]{0, 2 * 256 * 8 * 8, 2 * 2 * 256 * 8 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout newsin7_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            3, /* 3 bits per pixel */
            new int[]{0, 2 * 64 * 16 * 16, 2 * 2 * 64 * 16 * 16}, /* the bitplanes are separated */
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
            new int[]{0},
            new int[]{3, 0, 0, 0, 0, 0, 0}, /* I "know" that this bit of the */
            new int[]{0}, /* graphics ROMs is 1 */
            0 /* no use */
    );
    static GfxLayout theend_bulletlayout = new GfxLayout(
            /* there is no gfx ROM for this one, it is generated by the hardware */
            7, 1, /* 4*1 line, I think - 7*1 to position it correctly */
            1, /* just one */
            1, /* 1 bit per pixel */
            new int[]{0},
            new int[]{2, 2, 2, 2, 0, 0, 0}, /* I "know" that this bit of the */
            new int[]{0}, /* graphics ROMs is 1 */
            0 /* no use */
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

    static GfxDecodeInfo scramble_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo theend_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, theend_bulletlayout, 8 * 4, 2),
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo mariner_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, mariner_charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, mariner_spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo devilfsh_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, devilfsh_charlayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, devilfsh_spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0x0000, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo newsin7_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, newsin7_charlayout, 0, 4),
                new GfxDecodeInfo(REGION_GFX1, 0x0800, newsin7_spritelayout, 0, 4),
                new GfxDecodeInfo(REGION_GFX1, 0x0000, bulletlayout, 8 * 4, 1), /* 1 color code instead of 2, so all */
                /* shots will be yellow */
                new GfxDecodeInfo(0, 0x0000, backgroundlayout, 8 * 4 + 2 * 2, 1), /* this will be dynamically created */
                new GfxDecodeInfo(-1) /* end of array */};
    static AY8910interface scramble_ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            14318000 / 8, /* 1.78975 MHz */
            new int[]{MIXERG(30, MIXER_GAIN_2x, MIXER_PAN_CENTER), MIXERG(30, MIXER_GAIN_2x, MIXER_PAN_CENTER)},
            new ReadHandlerPtr[]{soundlatch_r, null},
            new ReadHandlerPtr[]{scramble_portB_r, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static AY8910interface froggers_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            14318000 / 8, /* 1.78975 MHz */
            new int[]{MIXERG(80, MIXER_GAIN_2x, MIXER_PAN_CENTER)},
            new ReadHandlerPtr[]{soundlatch_r},
            new ReadHandlerPtr[]{frogger_portB_r},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static AY8910interface triplep_ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            14318000 / 8, /* 1.78975 MHz */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    public static MachineDriver machine_driver_scramble = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        scramble_readmem, scramble_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            scramble_gfxdecodeinfo,
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
                        scramble_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_theend = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        scramble_readmem, scramble_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            theend_gfxdecodeinfo,
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
                        scramble_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_froggers = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        scramble_readmem, scramble_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        froggers_sound_readmem, froggers_sound_writemem, froggers_sound_readport, froggers_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            scramble_gfxdecodeinfo,
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
                        froggers_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_mars = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mars_readmem, mars_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            scramble_gfxdecodeinfo,
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
                        scramble_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_devilfsh = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mars_readmem, mars_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            devilfsh_gfxdecodeinfo,
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
                        scramble_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_newsin7 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mars_readmem, mars_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            newsin7_gfxdecodeinfo,
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
                        scramble_ay8910_interface
                )
            }
    );
    static MachineDriver machine_driver_ckongs = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        ckongs_readmem, ckongs_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            mariner_gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            ckongs_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        scramble_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_hotshock = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        hotshock_readmem, hotshock_writemem, null, null,
                        scramble_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, hotshock_sound_readport, hotshock_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            mariner_gfxdecodeinfo,
            32 + 64 + 1, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 1 for background */
            galaxian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            pisces_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        scramble_ay8910_interface
                )
            }
    );

    /* Triple Punch and Mariner are different - only one CPU, one 8910 */
    static MachineDriver machine_driver_triplep = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        scramble_readmem, triplep_writemem, triplep_readport, triplep_writeport,
                        scramble_vh_interrupt, 1
                )
            },
            60, 2500,/* ? */ /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            scramble_gfxdecodeinfo,
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
                        triplep_ay8910_interface
                )
            }
    );

    static MachineDriver machine_driver_mariner = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz */
                        mariner_readmem, triplep_writemem, triplep_readport, triplep_writeport,
                        mariner_vh_interrupt, 1
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            mariner_gfxdecodeinfo,
            32 + 64 + 10, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 10 for background */
            mariner_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            mariner_vh_start,
            generic_vh_stop,
            galaxian_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        triplep_ay8910_interface
                )
            }
    );

    /**
     * *******************************************************
     */
    /* hunchbks is *very* different, as it uses an S2650 CPU */
    /*  epoxied in a plastic case labelled Century Playpack   */
    /**
     * *******************************************************
     */
    static MachineDriver machine_driver_hunchbks = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_S2650,
                        18432000 / 6,
                        hunchbks_readmem, hunchbks_writemem, null, null,
                        hunchbks_vh_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 MHz */
                        scramble_sound_readmem, scramble_sound_writemem, scramble_sound_readport, scramble_sound_writeport,
                        ignore_interrupt, 1
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            scramble_gfxdecodeinfo,
            32 + 64 + 10, 8 * 4 + 2 * 2 + 128 * 1, /* 32 for the characters, 64 for the stars, 10 for background */
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
                        scramble_ay8910_interface
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
    static RomLoadHandlerPtr rom_scramble = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2d.k", 0x0000, 0x0800, 0xea35ccaa);
            ROM_LOAD("2e.k", 0x0800, 0x0800, 0xe7bba1b3);
            ROM_LOAD("2f.k", 0x1000, 0x0800, 0x12d7fc3e);
            ROM_LOAD("2h.k", 0x1800, 0x0800, 0xb59360eb);
            ROM_LOAD("2j.k", 0x2000, 0x0800, 0x4919a91c);
            ROM_LOAD("2l.k", 0x2800, 0x0800, 0x26a4547b);
            ROM_LOAD("2m.k", 0x3000, 0x0800, 0x0bb49470);
            ROM_LOAD("2p.k", 0x3800, 0x0800, 0x6a5740e5);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xbcd297f0);
            ROM_LOAD("5d", 0x0800, 0x0800, 0xde7912da);
            ROM_LOAD("5e", 0x1000, 0x0800, 0xba2fa933);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f.k", 0x0000, 0x0800, 0x4708845b);
            ROM_LOAD("5h.k", 0x0800, 0x0800, 0x11fd2887);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_scrambls = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2d", 0x0000, 0x0800, 0xb89207a1);
            ROM_LOAD("2e", 0x0800, 0x0800, 0xe9b4b9eb);
            ROM_LOAD("2f", 0x1000, 0x0800, 0xa1f14f4c);
            ROM_LOAD("2h", 0x1800, 0x0800, 0x591bc0d9);
            ROM_LOAD("2j", 0x2000, 0x0800, 0x22f11b6b);
            ROM_LOAD("2l", 0x2800, 0x0800, 0x705ffe49);
            ROM_LOAD("2m", 0x3000, 0x0800, 0xea26c35c);
            ROM_LOAD("2p", 0x3800, 0x0800, 0x94d8f5e3);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xbcd297f0);
            ROM_LOAD("5d", 0x0800, 0x0800, 0xde7912da);
            ROM_LOAD("5e", 0x1000, 0x0800, 0xba2fa933);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x0000, 0x0800, 0x5f30311a);
            ROM_LOAD("5h", 0x0800, 0x0800, 0x516e029e);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_atlantis = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c", 0x0000, 0x0800, 0x0e485b9a);
            ROM_LOAD("2e", 0x0800, 0x0800, 0xc1640513);
            ROM_LOAD("2f", 0x1000, 0x0800, 0xeec265ee);
            ROM_LOAD("2h", 0x1800, 0x0800, 0xa5d2e442);
            ROM_LOAD("2j", 0x2000, 0x0800, 0x45f7cf34);
            ROM_LOAD("2l", 0x2800, 0x0800, 0xf335b96b);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xbcd297f0);
            ROM_LOAD("5d", 0x0800, 0x0800, 0xde7912da);
            ROM_LOAD("5e", 0x1000, 0x0800, 0xba2fa933);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f", 0x0000, 0x0800, 0x57f9c6b9);
            ROM_LOAD("5h", 0x0800, 0x0800, 0xe989f325);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_atlants2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("rom1", 0x0000, 0x0800, 0xad348089);
            ROM_LOAD("rom2", 0x0800, 0x0800, 0xcaa705d1);
            ROM_LOAD("rom3", 0x1000, 0x0800, 0xe420641d);
            ROM_LOAD("rom4", 0x1800, 0x0800, 0x04792d90);
            ROM_LOAD("rom5", 0x2000, 0x0800, 0x6eaf510d);
            ROM_LOAD("rom6", 0x2800, 0x0800, 0xb297bd4b);
            ROM_LOAD("rom7", 0x3000, 0x0800, 0xa50bf8d5);
            ROM_LOAD("rom8", 0x3800, 0x0800, 0xd2c5c984);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("5c", 0x0000, 0x0800, 0xbcd297f0);
            ROM_LOAD("5d", 0x0800, 0x0800, 0xde7912da);
            ROM_LOAD("5e", 0x1000, 0x0800, 0xba2fa933);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rom9", 0x0000, 0x0800, 0x55cd5acd);
            ROM_LOAD("rom10", 0x0800, 0x0800, 0x72e773b8);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_theend = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ic13_1t.bin", 0x0000, 0x0800, 0x93e555ba);
            ROM_LOAD("ic14_2t.bin", 0x0800, 0x0800, 0x2de7ad27);
            ROM_LOAD("ic15_3t.bin", 0x1000, 0x0800, 0x035f750b);
            ROM_LOAD("ic16_4t.bin", 0x1800, 0x0800, 0x61286b5c);
            ROM_LOAD("ic17_5t.bin", 0x2000, 0x0800, 0x434a8f68);
            ROM_LOAD("ic18_6t.bin", 0x2800, 0x0800, 0xdc4cc786);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ic56_1.bin", 0x0000, 0x0800, 0x7a141f29);
            ROM_LOAD("ic55_2.bin", 0x0800, 0x0800, 0x218497c1);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic30_2c.bin", 0x0000, 0x0800, 0x68ccf7bf);
            ROM_LOAD("ic31_1c.bin", 0x0800, 0x0800, 0x4a48c999);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("6331-1j.86", 0x0000, 0x0020, 0x24652bc4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_theends = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ic13", 0x0000, 0x0800, 0x90e5ab14);
            ROM_LOAD("ic14", 0x0800, 0x0800, 0x950f0a07);
            ROM_LOAD("ic15", 0x1000, 0x0800, 0x6786bcf5);
            ROM_LOAD("ic16", 0x1800, 0x0800, 0x380a0017);
            ROM_LOAD("ic17", 0x2000, 0x0800, 0xaf067b7f);
            ROM_LOAD("ic18", 0x2800, 0x0800, 0xa0411b93);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ic56", 0x0000, 0x0800, 0x3b2c2f70);
            ROM_LOAD("ic55", 0x0800, 0x0800, 0xe0429e50);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic30", 0x0000, 0x0800, 0x527fd384);
            ROM_LOAD("ic31", 0x0800, 0x0800, 0xaf6d09b6);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("6331-1j.86", 0x0000, 0x0020, 0x24652bc4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_froggers = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("vid_d2.bin", 0x0000, 0x0800, 0xc103066e);
            ROM_LOAD("vid_e2.bin", 0x0800, 0x0800, 0xf08bc094);
            ROM_LOAD("vid_f2.bin", 0x1000, 0x0800, 0x637a2ff8);
            ROM_LOAD("vid_h2.bin", 0x1800, 0x0800, 0x04c027a5);
            ROM_LOAD("vid_j2.bin", 0x2000, 0x0800, 0xfbdfbe74);
            ROM_LOAD("vid_l2.bin", 0x2800, 0x0800, 0x8a4389e1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("frogger.608", 0x0000, 0x0800, 0xe8ab0256);
            ROM_LOAD("frogger.609", 0x0800, 0x0800, 0x7380a48f);
            ROM_LOAD("frogger.610", 0x1000, 0x0800, 0x31d7eb27);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("epr-1036.1k", 0x0000, 0x0800, 0x658745f8);
            ROM_LOAD("frogger.607", 0x0800, 0x0800, 0x05f7d883);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("vid_e6.bin", 0x0000, 0x0020, 0x0b878b54);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_amidars = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("am2d", 0x0000, 0x0800, 0x24b79547);
            ROM_LOAD("am2e", 0x0800, 0x0800, 0x4c64161e);
            ROM_LOAD("am2f", 0x1000, 0x0800, 0xb3987a72);
            ROM_LOAD("am2h", 0x1800, 0x0800, 0x29873461);
            ROM_LOAD("am2j", 0x2000, 0x0800, 0x0fdd54d8);
            ROM_LOAD("am2l", 0x2800, 0x0800, 0x5382f7ed);
            ROM_LOAD("am2m", 0x3000, 0x0800, 0x1d7109e9);
            ROM_LOAD("am2p", 0x3800, 0x0800, 0xc9163ac6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("amidarus.5c", 0x0000, 0x1000, 0x8ca7b750);
            ROM_LOAD("amidarus.5d", 0x1000, 0x1000, 0x9b5bdc0a);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2716.a6", 0x0000, 0x0800, 0x2082ad0a);  /* Same graphics ROMs as Amigo */

            ROM_LOAD("2716.a5", 0x0800, 0x0800, 0x3029f94f);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("amidar.clr", 0x0000, 0x0020, 0xf940dcc3);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_triplep = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("triplep.2g", 0x0000, 0x1000, 0xc583a93d);
            ROM_LOAD("triplep.2h", 0x1000, 0x1000, 0xc03ddc49);
            ROM_LOAD("triplep.2k", 0x2000, 0x1000, 0xe83ca6b5);
            ROM_LOAD("triplep.2l", 0x3000, 0x1000, 0x982cc3b9);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("triplep.5f", 0x0000, 0x0800, 0xd51cbd6f);
            ROM_LOAD("triplep.5h", 0x0800, 0x0800, 0xf21c0059);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("tripprom.6e", 0x0000, 0x0020, 0x624f75df);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_knockout = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("knockout.2h", 0x0000, 0x1000, 0xeaaa848e);
            ROM_LOAD("knockout.2k", 0x1000, 0x1000, 0xbc26d2c0);
            ROM_LOAD("knockout.2l", 0x2000, 0x1000, 0x02025c10);
            ROM_LOAD("knockout.2m", 0x3000, 0x1000, 0xe9abc42b);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("triplep.5f", 0x0000, 0x0800, 0xd51cbd6f);
            ROM_LOAD("triplep.5h", 0x0800, 0x0800, 0xf21c0059);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("tripprom.6e", 0x0000, 0x0020, 0x624f75df);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_mariner = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for main CPU */

            ROM_LOAD("tp1", 0x0000, 0x1000, 0xdac1dfd0);
            ROM_LOAD("tm2", 0x1000, 0x1000, 0xefe7ca28);
            ROM_LOAD("tm3", 0x2000, 0x1000, 0x027881a6);
            ROM_LOAD("tm4", 0x3000, 0x1000, 0xa0fde7dc);
            ROM_LOAD("tm5", 0x6000, 0x0800, 0xd7ebcb8e);
            ROM_CONTINUE(0x5800, 0x0800);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tm8", 0x0000, 0x1000, 0x70ae611f);
            ROM_LOAD("tm9", 0x1000, 0x1000, 0x8e4e999e);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("tm.t4", 0x0000, 0x0020, 0xca42b6dd);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ckongs = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("vid_2c.bin", 0x0000, 0x1000, 0x49a8c234);
            ROM_LOAD("vid_2e.bin", 0x1000, 0x1000, 0xf1b667f1);
            ROM_LOAD("vid_2f.bin", 0x2000, 0x1000, 0xb194b75d);
            ROM_LOAD("vid_2h.bin", 0x3000, 0x1000, 0x2052ba8a);
            ROM_LOAD("vid_2j.bin", 0x4000, 0x1000, 0xb377afd0);
            ROM_LOAD("vid_2l.bin", 0x5000, 0x1000, 0xfe65e691);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("turt_snd.5c", 0x0000, 0x1000, 0xf0c30f9a);
            ROM_LOAD("snd_5d.bin", 0x1000, 0x1000, 0x892c9547);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("vid_5f.bin", 0x0000, 0x1000, 0x7866d2cb);
            ROM_LOAD("vid_5h.bin", 0x1000, 0x1000, 0x7311a101);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("vid_6e.bin", 0x0000, 0x0020, 0x5039af97);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_mars = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("u26.3", 0x0000, 0x0800, 0x2f88892c);
            ROM_LOAD("u56.4", 0x0800, 0x0800, 0x9e6bcbf7);
            ROM_LOAD("u69.5", 0x1000, 0x0800, 0xdf496e6e);
            ROM_LOAD("u98.6", 0x1800, 0x0800, 0x75f274bb);
            ROM_LOAD("u114.7", 0x2000, 0x0800, 0x497fd8d0);
            ROM_LOAD("u133.8", 0x2800, 0x0800, 0x3d4cd59f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("u39.9", 0x0000, 0x0800, 0xbb5968b9);
            ROM_LOAD("u51.10", 0x0800, 0x0800, 0x75fd7720);
            ROM_LOAD("u78.11", 0x1000, 0x0800, 0x72a492da);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("u72.1", 0x0000, 0x0800, 0x279789d0);
            ROM_LOAD("u101.2", 0x0800, 0x0800, 0xc5dc627f);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_devilfsh = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("u26.1", 0x0000, 0x0800, 0xec047d71);
            ROM_LOAD("u56.2", 0x0800, 0x0800, 0x0138ade9);
            ROM_LOAD("u69.3", 0x1000, 0x0800, 0x5dd0b3fc);
            ROM_LOAD("u98.4", 0x1800, 0x0800, 0xded0b745);
            ROM_LOAD("u114.5", 0x2000, 0x0800, 0x5fd40176);
            ROM_LOAD("u133.6", 0x2800, 0x0800, 0x03538336);
            ROM_LOAD("u143.7", 0x3000, 0x0800, 0x64676081);
            ROM_LOAD("u163.8", 0x3800, 0x0800, 0xbc3d6770);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("u39.9", 0x0000, 0x0800, 0x09987e2e);
            ROM_LOAD("u51.10", 0x0800, 0x0800, 0x1e2b1471);
            ROM_LOAD("u78.11", 0x1000, 0x0800, 0x45279aaa);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("u72.12", 0x0000, 0x1000, 0x5406508e);
            ROM_LOAD("u101.13", 0x1000, 0x1000, 0x8c4018b6);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_newsin7 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("newsin.1", 0x0000, 0x1000, 0xe6c23fe0);
            ROM_LOAD("newsin.2", 0x1000, 0x1000, 0x3d477b5f);
            ROM_LOAD("newsin.3", 0x2000, 0x1000, 0x7dfa9af0);
            ROM_LOAD("newsin.4", 0x3000, 0x1000, 0xd1b0ba19);
            ROM_LOAD("newsin.5", 0xa000, 0x1000, 0x06275d59);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("newsin.13", 0x0000, 0x0800, 0xd88489a2);
            ROM_LOAD("newsin.12", 0x0800, 0x0800, 0xb154a7af);
            ROM_LOAD("newsin.11", 0x1000, 0x0800, 0x7ade709b);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("newsin.7", 0x2000, 0x1000, 0x6bc5d64f);
            ROM_LOAD("newsin.8", 0x1000, 0x1000, 0x0c5b895a);
            ROM_LOAD("newsin.9", 0x0000, 0x1000, 0x6b87adff);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("newsin.6", 0x0000, 0x0020, 0x5cf2cd8d);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_hotshock = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("hotshock.l10", 0x0000, 0x1000, 0x401078f7);
            ROM_LOAD("hotshock.l9", 0x1000, 0x1000, 0xaf76c237);
            ROM_LOAD("hotshock.l8", 0x2000, 0x1000, 0x30486031);
            ROM_LOAD("hotshock.l7", 0x3000, 0x1000, 0x5bde9312);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("hotshock.b3", 0x0000, 0x1000, 0x0092f0e2);
            ROM_LOAD("hotshock.b4", 0x1000, 0x1000, 0xc2135a44);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hotshock.h4", 0x0000, 0x1000, 0x60bdaea9);
            ROM_LOAD("hotshock.h5", 0x1000, 0x1000, 0x4ef17453);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("82s123.6e", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_hunchbks = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2c_hb01.bin", 0x0000, 0x0800, 0x8bebd834);
            ROM_LOAD("2e_hb02.bin", 0x0800, 0x0800, 0x07de4229);
            ROM_LOAD("2f_hb03.bin", 0x2000, 0x0800, 0xb75a0dfc);
            ROM_LOAD("2h_hb04.bin", 0x2800, 0x0800, 0xf3206264);
            ROM_LOAD("2j_hb05.bin", 0x4000, 0x0800, 0x1bb78728);
            ROM_LOAD("2l_hb06.bin", 0x4800, 0x0800, 0xf25ed680);
            ROM_LOAD("2m_hb07.bin", 0x6000, 0x0800, 0xc72e0e17);
            ROM_LOAD("2p_hb08.bin", 0x6800, 0x0800, 0x412087b0);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("11d_snd.bin", 0x0000, 0x0800, 0x88226086);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5f_hb09.bin", 0x0000, 0x0800, 0xdb489c3d);
            ROM_LOAD("5h_hb10.bin", 0x0800, 0x0800, 0x3977650e);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("6e_prom.bin", 0x0000, 0x0020, 0x4e3caeab);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_scrambls = new InitDriverHandlerPtr() {
        public void handler() {
            install_mem_read_handler(0, 0x8102, 0x8102, scramble_input_port_2_r);
            install_mem_read_handler(0, 0x8202, 0x8202, scramble_protection_r);
        }
    };

    public static InitDriverHandlerPtr init_scramble = new InitDriverHandlerPtr() {
        public void handler() {
            install_mem_read_handler(0, 0x8202, 0x8202, scramblk_protection_r);
        }
    };

    public static InitDriverHandlerPtr init_hotshock = new InitDriverHandlerPtr() {
        public void handler() {
            /* protection??? The game jumps into never-neverland here. I think
             it just expects a RET there */
            memory_region(REGION_CPU1).write(0x2ef9, 0xc9);
        }
    };

    public static InitDriverHandlerPtr init_froggers = new InitDriverHandlerPtr() {
        public void handler() {
            int A;
            UBytePtr RAM;

            /* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
            RAM = memory_region(REGION_CPU2);
            for (A = 0; A < 0x0800; A++) {
                RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
            }
        }
    };

    public static InitDriverHandlerPtr init_mars = new InitDriverHandlerPtr() {
        public void handler() {
            int i;
            UBytePtr RAM;

            /* Address lines are scrambled on the main CPU:
	
             A0 . A2
             A1 . A0
             A2 . A3
             A3 . A1 */
            RAM = memory_region(REGION_CPU1);
            for (i = 0; i < 0x10000; i += 16) {
                int j;
                char[] swapbuffer = new char[16];

                for (j = 0; j < 16; j++) {
                    swapbuffer[j] = RAM.read(i + ((j & 1) << 2) + ((j & 2) >> 1) + ((j & 4) << 1) + ((j & 8) >> 2));
                }

                //memcpy(RAM[i], swapbuffer, 16);
                for (j = 0; j < 16; j++) {
                    RAM.write(i, swapbuffer[j]);
                }
            }
        }
    };

    public static GameDriver driver_scramble = new GameDriver("1981", "scramble", "scramble.java", rom_scramble, null, machine_driver_scramble, input_ports_scramble, init_scramble, ROT90, "Konami", "Scramble");
    public static GameDriver driver_scrambls = new GameDriver("1981", "scrambls", "scramble.java", rom_scrambls, driver_scramble, machine_driver_scramble, input_ports_scramble, init_scrambls, ROT90, "[Konami] (Stern license)", "Scramble (Stern)");
    public static GameDriver driver_atlantis = new GameDriver("1981", "atlantis", "scramble.java", rom_atlantis, null, machine_driver_scramble, input_ports_atlantis, null, ROT90, "Comsoft", "Battle of Atlantis (set 1)");
    public static GameDriver driver_atlants2 = new GameDriver("1981", "atlants2", "scramble.java", rom_atlants2, driver_atlantis, machine_driver_scramble, input_ports_atlantis, null, ROT90, "Comsoft", "Battle of Atlantis (set 2)");
    public static GameDriver driver_theend = new GameDriver("1980", "theend", "scramble.java", rom_theend, null, machine_driver_theend, input_ports_theend, null, ROT90, "Konami", "The End");
    public static GameDriver driver_theends = new GameDriver("1980", "theends", "scramble.java", rom_theends, driver_theend, machine_driver_theend, input_ports_theend, null, ROT90, "[Konami] (Stern license)", "The End (Stern)");
    public static GameDriver driver_froggers = new GameDriver("1981", "froggers", "scramble.java", rom_froggers, driver_frogger, machine_driver_froggers, input_ports_froggers, init_froggers, ROT90, "bootleg", "Frog");
    public static GameDriver driver_amidars = new GameDriver("1982", "amidars", "scramble.java", rom_amidars, driver_amidar, machine_driver_scramble, input_ports_amidars, null, ROT90, "Konami", "Amidar (Scramble hardware)");
    public static GameDriver driver_triplep = new GameDriver("1982", "triplep", "scramble.java", rom_triplep, null, machine_driver_triplep, input_ports_triplep, null, ROT90, "KKI", "Triple Punch");
    public static GameDriver driver_knockout = new GameDriver("1982", "knockout", "scramble.java", rom_knockout, driver_triplep, machine_driver_triplep, input_ports_triplep, null, ROT90, "KKK", "Knock Out !!");
    public static GameDriver driver_mariner = new GameDriver("1981", "mariner", "scramble.java", rom_mariner, null, machine_driver_mariner, input_ports_scramble, null, ROT90, "Amenip", "Mariner");
    public static GameDriver driver_ckongs = new GameDriver("1981", "ckongs", "scramble.java", rom_ckongs, driver_ckong, machine_driver_ckongs, input_ports_ckongs, null, ROT90, "bootleg", "Crazy Kong (Scramble hardware)");
    public static GameDriver driver_mars = new GameDriver("1981", "mars", "scramble.java", rom_mars, null, machine_driver_mars, input_ports_mars, init_mars, ROT90, "Artic", "Mars");
    public static GameDriver driver_devilfsh = new GameDriver("1982", "devilfsh", "scramble.java", rom_devilfsh, null, machine_driver_devilfsh, input_ports_devilfsh, init_mars, ROT90, "Artic", "Devil Fish");
    public static GameDriver driver_newsin7 = new GameDriver("1983", "newsin7", "scramble.java", rom_newsin7, null, machine_driver_newsin7, input_ports_newsin7, init_mars, ROT90, "ATW USA, Inc.", "New Sinbad 7", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_hotshock = new GameDriver("1982", "hotshock", "scramble.java", rom_hotshock, null, machine_driver_hotshock, input_ports_hotshock, init_hotshock, ROT90, "E.G. Felaco", "Hot Shocker");
    /*TODO*///	public static GameDriver driver_hunchbks    = new GameDriver("1983"	,"hunchbks"	,"scramble.java"	,rom_hunchbks,driver_hunchbkd	,machine_driver_hunchbks	,input_ports_hunchbks	,null	,ROT90	,	"Century", "Hunchback (Scramble conversion)" );
}
