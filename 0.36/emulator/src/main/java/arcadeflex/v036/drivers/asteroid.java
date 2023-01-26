/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 26/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.asteroid.*;
import static arcadeflex.v036.machine.atari_vg.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.osdependH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.asteroid.*;
import static arcadeflex.v036.sndhrdw.llander.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.avgdvg.*;
import static arcadeflex.v036.vidhrdw.vector.*;
import static arcadeflex.v036.vidhrdw.llander.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class asteroid {

    /* Lunar Lander mirrors page 0 and page 1. */
    static UBytePtr llander_zeropage = new UBytePtr();

    public static ReadHandlerPtr llander_zeropage_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return llander_zeropage.read(offset & 0xff);
        }
    };

    public static WriteHandlerPtr llander_zeropage_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            llander_zeropage.write(offset & 0xff, data);
        }
    };

    static MemoryReadAddress asteroid_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2007, asteroid_IN0_r), /* IN0 */
                new MemoryReadAddress(0x2400, 0x2407, asteroid_IN1_r), /* IN1 */
                new MemoryReadAddress(0x2800, 0x2803, asteroid_DSW1_r), /* DSW1 */
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x5000, 0x57ff, MRA_ROM), /* vector rom */
                new MemoryReadAddress(0x6800, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress asteroib_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2000, asteroib_IN0_r), /* IN0 */
                new MemoryReadAddress(0x2003, 0x2003, input_port_3_r), /* hyperspace */
                new MemoryReadAddress(0x2400, 0x2407, asteroid_IN1_r), /* IN1 */
                new MemoryReadAddress(0x2800, 0x2803, asteroid_DSW1_r), /* DSW1 */
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x5000, 0x57ff, MRA_ROM), /* vector rom */
                new MemoryReadAddress(0x6800, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress asteroid_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x3000, 0x3000, avgdvg_go),
                new MemoryWriteAddress(0x3200, 0x3200, asteroid_bank_switch_w),
                new MemoryWriteAddress(0x3400, 0x3400, watchdog_reset_w),
                new MemoryWriteAddress(0x3600, 0x3600, asteroid_explode_w),
                new MemoryWriteAddress(0x3a00, 0x3a00, asteroid_thump_w),
                new MemoryWriteAddress(0x3c00, 0x3c05, asteroid_sounds_w),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x5000, 0x57ff, MWA_ROM), /* vector rom */
                new MemoryWriteAddress(0x6800, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress astdelux_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2007, asteroid_IN0_r), /* IN0 */
                new MemoryReadAddress(0x2400, 0x2407, asteroid_IN1_r), /* IN1 */
                new MemoryReadAddress(0x2800, 0x2803, asteroid_DSW1_r), /* DSW1 */
                new MemoryReadAddress(0x2c00, 0x2c0f, pokey1_r),
                new MemoryReadAddress(0x2c40, 0x2c7f, atari_vg_earom_r),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x4800, 0x57ff, MRA_ROM), /* vector rom */
                new MemoryReadAddress(0x6000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress astdelux_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x2405, 0x2405, astdelux_sounds_w), /* thrust sound */
                new MemoryWriteAddress(0x2c00, 0x2c0f, pokey1_w),
                new MemoryWriteAddress(0x3000, 0x3000, avgdvg_go),
                new MemoryWriteAddress(0x3200, 0x323f, atari_vg_earom_w),
                new MemoryWriteAddress(0x3400, 0x3400, watchdog_reset_w),
                new MemoryWriteAddress(0x3600, 0x3600, asteroid_explode_w),
                new MemoryWriteAddress(0x3a00, 0x3a00, atari_vg_earom_ctrl),
                /*	new MemoryWriteAddress( 0x3c00, 0x3c03, astdelux_led_w ),*/ /* P1 LED, P2 LED, unknown, thrust? */
                new MemoryWriteAddress(0x3c00, 0x3c03, MWA_NOP), /* P1 LED, P2 LED, unknown, thrust? */
                new MemoryWriteAddress(0x3c04, 0x3c04, astdelux_bank_switch_w),
                new MemoryWriteAddress(0x3c05, 0x3c07, coin_counter_w),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x4800, 0x57ff, MWA_ROM), /* vector rom */
                new MemoryWriteAddress(0x6000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress llander_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x01ff, llander_zeropage_r),
                new MemoryReadAddress(0x2000, 0x2000, llander_IN0_r), /* IN0 */
                new MemoryReadAddress(0x2400, 0x2407, asteroid_IN1_r), /* IN1 */
                new MemoryReadAddress(0x2800, 0x2803, asteroid_DSW1_r), /* DSW1 */
                new MemoryReadAddress(0x2c00, 0x2c00, input_port_3_r), /* IN3 */
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x4800, 0x5fff, MRA_ROM), /* vector rom */
                new MemoryReadAddress(0x6000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress llander_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x01ff, llander_zeropage_w, llander_zeropage),
                new MemoryWriteAddress(0x3000, 0x3000, avgdvg_go),
                new MemoryWriteAddress(0x3200, 0x3200, llander_led_w),
                new MemoryWriteAddress(0x3400, 0x3400, watchdog_reset_w),
                new MemoryWriteAddress(0x3c00, 0x3c00, llander_sounds_w),
                new MemoryWriteAddress(0x3e00, 0x3e00, llander_snd_reset_w),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x4800, 0x5fff, MWA_ROM), /* vector rom */
                new MemoryWriteAddress(0x6000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_asteroid = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Bit 2 and 3 are handled in the machine dependent part. */
 /* Bit 2 is the 3 KHz source and Bit 3 the VG_HALT bit    */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "German");
            PORT_DIPSETTING(0x02, "French");
            PORT_DIPSETTING(0x03, "Spanish");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Coinage"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_asteroib = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);/* resets */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* resets */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* Bit 7 is VG_HALT, handled in the machine dependant part */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "German");
            PORT_DIPSETTING(0x02, "French");
            PORT_DIPSETTING(0x03, "Spanish");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Coinage"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_START();
            /* hyperspace */
            PORT_BIT(0x7f, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON3);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_astdelux = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Bit 2 and 3 are handled in the machine dependent part. */
 /* Bit 2 is the 3 KHz source and Bit 3 the VG_HALT bit    */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);

            PORT_START();
            /* DSW 1 */
            PORT_DIPNAME(0x03, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "German");
            PORT_DIPSETTING(0x02, "French");
            PORT_DIPSETTING(0x03, "Spanish");
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2-4");
            PORT_DIPSETTING(0x04, "3-5");
            PORT_DIPSETTING(0x08, "4-6");
            PORT_DIPSETTING(0x0c, "5-7");
            PORT_DIPNAME(0x10, 0x00, "Minimum plays");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x10, "2");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x40, "12000");
            PORT_DIPSETTING(0x80, "15000");
            PORT_DIPSETTING(0xc0, "None");

            PORT_START();
            /* DSW 2 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x03, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x0c, "Right Coin");
            PORT_DIPSETTING(0x00, "*6");
            PORT_DIPSETTING(0x04, "*5");
            PORT_DIPSETTING(0x08, "*4");
            PORT_DIPSETTING(0x0c, "*1");
            PORT_DIPNAME(0x10, 0x10, "Center Coin");
            PORT_DIPSETTING(0x00, "*2");
            PORT_DIPSETTING(0x10, "*1");
            PORT_DIPNAME(0xe0, 0x80, "Bonus Coins");
            PORT_DIPSETTING(0x60, "1 each 5");
            PORT_DIPSETTING(0x80, "2 each 4");
            PORT_DIPSETTING(0xa0, "1 each 4");
            PORT_DIPSETTING(0xc0, "1 each 2");
            PORT_DIPSETTING(0xe0, "None");
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_llander = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
 /* Bit 0 is VG_HALT, handled in the machine dependant part */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_SERVICE(0x02, IP_ACTIVE_LOW);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            /* Of the rest, Bit 6 is the 3KHz source. 3,4 and 5 are unknown */
            PORT_BIT(0x78, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BITX(0x10, IP_ACTIVE_HIGH, IPT_START2, "Select Game", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1, "Abort", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x01, "Right Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x01, "*4");
            PORT_DIPSETTING(0x02, "*5");
            PORT_DIPSETTING(0x03, "*6");
            PORT_DIPNAME(0x0c, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x04, "French");
            PORT_DIPSETTING(0x08, "Spanish");
            PORT_DIPSETTING(0x0c, "German");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x20, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xd0, 0x80, "Fuel units");
            PORT_DIPSETTING(0x00, "450");
            PORT_DIPSETTING(0x40, "600");
            PORT_DIPSETTING(0x80, "750");
            PORT_DIPSETTING(0xc0, "900");
            PORT_DIPSETTING(0x10, "1100");
            PORT_DIPSETTING(0x50, "1300");
            PORT_DIPSETTING(0x90, "1550");
            PORT_DIPSETTING(0xd0, "1800");

            /* The next one is a potentiometer */
            PORT_START();
            /* IN3 */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE | IPF_REVERSE, 100, 10, 0, 255, KEYCODE_UP, KEYCODE_DOWN, JOYCODE_1_UP, JOYCODE_1_DOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_llander1 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
 /* Bit 0 is VG_HALT, handled in the machine dependant part */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_SERVICE(0x02, IP_ACTIVE_LOW);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);
            /* Of the rest, Bit 6 is the 3KHz source. 3,4 and 5 are unknown */
            PORT_BIT(0x78, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BITX(0x10, IP_ACTIVE_HIGH, IPT_START2, "Select Game", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BITX(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1, "Abort", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x01, "Right Coin");
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x01, "*4");
            PORT_DIPSETTING(0x02, "*5");
            PORT_DIPSETTING(0x03, "*6");
            PORT_DIPNAME(0x0c, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x04, "French");
            PORT_DIPSETTING(0x08, "Spanish");
            PORT_DIPSETTING(0x0c, "German");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x10, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xc0, 0x80, "Fuel units");
            PORT_DIPSETTING(0x00, "450");
            PORT_DIPSETTING(0x40, "600");
            PORT_DIPSETTING(0x80, "750");
            PORT_DIPSETTING(0xc0, "900");

            /* The next one is a potentiometer */
            PORT_START();
            /* IN3 */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE | IPF_REVERSE, 100, 10, 0, 255, KEYCODE_UP, KEYCODE_DOWN, JOYCODE_1_UP, JOYCODE_1_DOWN);
            INPUT_PORTS_END();
        }
    };

    static void asteroid1_hisave() {
        Object f;
        UBytePtr RAM = memory_region(REGION_CPU1);

        if ((f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_HIGHSCORE, 1)) != null) {
            osd_fwrite(f, new UBytePtr(RAM, 0x001c), 2 * 10 + 3 * 11);
            osd_fclose(f);
        }
    }

    static void asteroid_hisave() {
        Object f;
        UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));

        if ((f = osd_fopen(Machine.gamedrv.name, null, OSD_FILETYPE_HIGHSCORE, 1)) != null) {
            osd_fwrite(f, new UBytePtr(RAM, 0x001d), 2 * 10 + 3 * 11);
            osd_fclose(f);
        }
    }

    /* Asteroids Deluxe now uses the earom routines
	 * However, we keep the highscore location, just in case
	 *		osd_fwrite(f,&RAM[0x0023],3*10+3*11);
     */
    static CustomSound_interface asteroid_custom_interface = new CustomSound_interface(
            asteroid_sh_start,
            asteroid_sh_stop,
            asteroid_sh_update
    );

    static MachineDriver machine_driver_asteroid = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        asteroid_readmem, asteroid_writemem, null, null,
                        asteroid_interrupt, 4 /* 250 Hz */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            asteroid_init_machine,
            /* video hardware */
            400, 300, new rectangle(0, 1040, 70, 950),
            null,
            256, 256,
            avg_init_palette_white,
            VIDEO_TYPE_VECTOR,
            null,
            dvg_start,
            dvg_stop,
            dvg_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        asteroid_custom_interface
                )
            }
    );

    static MachineDriver machine_driver_asteroib = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        asteroib_readmem, asteroid_writemem, null, null,
                        asteroid_interrupt, 4 /* 250 Hz */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            asteroid_init_machine,
            /* video hardware */
            400, 300, new rectangle(0, 1040, 70, 950),
            null,
            256, 256,
            avg_init_palette_white,
            VIDEO_TYPE_VECTOR,
            null,
            dvg_start,
            dvg_stop,
            dvg_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        asteroid_custom_interface
                )
            }
    );

    static POKEYinterface pokey_interface = new POKEYinterface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz??? */
            new int[]{100},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            /* The allpot handler */
            new ReadHandlerPtr[]{input_port_3_r}
    );

    static CustomSound_interface astdelux_custom_interface = new CustomSound_interface(
            astdelux_sh_start,
            astdelux_sh_stop,
            astdelux_sh_update
    );

    static MachineDriver machine_driver_astdelux = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        astdelux_readmem, astdelux_writemem, null, null,
                        asteroid_interrupt, 4 /* 250 Hz */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 1040, 70, 950),
            null,
            256, 256,
            avg_init_palette_astdelux,
            VIDEO_TYPE_VECTOR,
            null,
            dvg_start,
            dvg_stop,
            dvg_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        pokey_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM,
                        astdelux_custom_interface
                )
            },
            atari_vg_earom_handler
    );

    static CustomSound_interface llander_custom_interface = new CustomSound_interface(
            llander_sh_start,
            llander_sh_stop,
            llander_sh_update
    );

    static MachineDriver machine_driver_llander = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        llander_readmem, llander_writemem, null, null,
                        llander_interrupt, 6 /* 250 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 1050, 0, 900),
            null,
            256, 256,
            llander_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            llander_start,
            llander_stop,
            llander_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        llander_custom_interface
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
    static RomLoadHandlerPtr rom_asteroid = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("035145.02", 0x6800, 0x0800, 0x0cc75459);
            ROM_LOAD("035144.02", 0x7000, 0x0800, 0x096ed35c);
            ROM_LOAD("035143.02", 0x7800, 0x0800, 0x312caa02);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("035127.02", 0x5000, 0x0800, 0x8b71fd9e);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_asteroi1 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("035145.01", 0x6800, 0x0800, 0xe9bfda64);
            ROM_LOAD("035144.01", 0x7000, 0x0800, 0xe53c28a9);
            ROM_LOAD("035143.01", 0x7800, 0x0800, 0x7d4e3d05);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("035127.01", 0x5000, 0x0800, 0x99699366);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_asteroib = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("035145ll.bin", 0x6800, 0x0800, 0x605fc0f2);
            ROM_LOAD("035144ll.bin", 0x7000, 0x0800, 0xe106de77);
            ROM_LOAD("035143ll.bin", 0x7800, 0x0800, 0x6b1d8594);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("035127.02", 0x5000, 0x0800, 0x8b71fd9e);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_astdelux = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("036430.02", 0x6000, 0x0800, 0xa4d7a525);
            ROM_LOAD("036431.02", 0x6800, 0x0800, 0xd4004aae);
            ROM_LOAD("036432.02", 0x7000, 0x0800, 0x6d720c41);
            ROM_LOAD("036433.03", 0x7800, 0x0800, 0x0dcc0be6);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("036800.02", 0x4800, 0x0800, 0xbb8cabe1);
            ROM_LOAD("036799.01", 0x5000, 0x0800, 0x7d511572);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_astdelu1 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("036430.01", 0x6000, 0x0800, 0x8f5dabc6);
            ROM_LOAD("036431.01", 0x6800, 0x0800, 0x157a8516);
            ROM_LOAD("036432.01", 0x7000, 0x0800, 0xfdea913c);
            ROM_LOAD("036433.02", 0x7800, 0x0800, 0xd8db74e3);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("036800.01", 0x4800, 0x0800, 0x3b597407);
            ROM_LOAD("036799.01", 0x5000, 0x0800, 0x7d511572);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_llander = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("034572.02", 0x6000, 0x0800, 0xb8763eea);
            ROM_LOAD("034571.02", 0x6800, 0x0800, 0x77da4b2f);
            ROM_LOAD("034570.01", 0x7000, 0x0800, 0x2724e591);
            ROM_LOAD("034569.02", 0x7800, 0x0800, 0x72837a4e);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("034599.01", 0x4800, 0x0800, 0x355a9371);
            ROM_LOAD("034598.01", 0x5000, 0x0800, 0x9c4ffa68);
            /* This _should_ be the rom for international versions. */
 /* Unfortunately, is it not currently available. */
            ROM_LOAD("034597.01", 0x5800, 0x0800, 0x00000000);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_llander1 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("034572.01", 0x6000, 0x0800, 0x2aff3140);
            ROM_LOAD("034571.01", 0x6800, 0x0800, 0x493e24b7);
            ROM_LOAD("034570.01", 0x7000, 0x0800, 0x2724e591);
            ROM_LOAD("034569.01", 0x7800, 0x0800, 0xb11a7d01);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Vector ROM */
            ROM_LOAD("034599.01", 0x4800, 0x0800, 0x355a9371);
            ROM_LOAD("034598.01", 0x5000, 0x0800, 0x9c4ffa68);
            /* This _should_ be the rom for international versions. */
 /* Unfortunately, is it not currently available. */
            ROM_LOAD("034597.01", 0x5800, 0x0800, 0x00000000);
            ROM_END();
        }
    };

    public static GameDriver driver_asteroid = new GameDriver("1979", "asteroid", "asteroid.java", rom_asteroid, null, machine_driver_asteroid, input_ports_asteroid, null, ROT0, "Atari", "Asteroids (rev 2)");
    public static GameDriver driver_asteroi1 = new GameDriver("1979", "asteroi1", "asteroid.java", rom_asteroi1, driver_asteroid, machine_driver_asteroid, input_ports_asteroid, null, ROT0, "Atari", "Asteroids (rev 1)");
    public static GameDriver driver_asteroib = new GameDriver("1979", "asteroib", "asteroid.java", rom_asteroib, driver_asteroid, machine_driver_asteroib, input_ports_asteroib, null, ROT0, "bootleg", "Asteroids (bootleg on Lunar Lander hardware)");
    public static GameDriver driver_astdelux = new GameDriver("1980", "astdelux", "asteroid.java", rom_astdelux, null, machine_driver_astdelux, input_ports_astdelux, null, ROT0, "Atari", "Asteroids Deluxe (rev 2)");
    public static GameDriver driver_astdelu1 = new GameDriver("1980", "astdelu1", "asteroid.java", rom_astdelu1, driver_astdelux, machine_driver_astdelux, input_ports_astdelux, null, ROT0, "Atari", "Asteroids Deluxe (rev 1)");
    public static GameDriver driver_llander = new GameDriver("1979", "llander", "asteroid.java", rom_llander, null, machine_driver_llander, input_ports_llander, null, ROT0, "Atari", "Lunar Lander (rev 2)");
    public static GameDriver driver_llander1 = new GameDriver("1979", "llander1", "asteroid.java", rom_llander1, driver_llander, machine_driver_llander, input_ports_llander1, null, ROT0, "Atari", "Lunar Lander (rev 1)");
}
