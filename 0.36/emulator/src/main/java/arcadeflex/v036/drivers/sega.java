/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 24/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.sega.*;
import static arcadeflex.v036.machine.segar.*;
//mame imports
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.sega.*;
//sound imports
import static arcadeflex.v036.sound.samplesH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.vector.*;
import static arcadeflex.v036.vidhrdw.sega.*;

public class sega {

    /* Video hardware prototypes */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc800, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xefff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xdfff, MRA_RAM), /* sound ram */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xffff, sega_wr, sega_mem),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_RAM, vectorram, vectorram_size), /* handled by the above, */
                /* here only to initialize the pointer */
                new MemoryWriteAddress(-1)
            };

    static IOReadPort spacfury_readport[]
            = {
                new IOReadPort(0x3f, 0x3f, sega_sh_r),
                new IOReadPort(0xbe, 0xbe, sega_mult_r),
                new IOReadPort(0xf8, 0xfb, sega_read_ports),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort spacfury_writeport[]
            = {
                new IOWritePort(0x38, 0x38, sega_sh_speech_w),
                new IOWritePort(0x3e, 0x3e, spacfury1_sh_w),
                new IOWritePort(0x3f, 0x3f, spacfury2_sh_w),
                new IOWritePort(0xbd, 0xbd, sega_mult1_w),
                new IOWritePort(0xbe, 0xbe, sega_mult2_w),
                new IOWritePort(0xf9, 0xf9, coin_counter_w), /* 0x80 = enable, 0x00 = disable */
                new IOWritePort(-1) /* end of table */};

    static IOReadPort zektor_readport[]
            = {
                new IOReadPort(0x3f, 0x3f, sega_sh_r),
                new IOReadPort(0xbe, 0xbe, sega_mult_r),
                new IOReadPort(0xf8, 0xfb, sega_read_ports),
                new IOReadPort(0xfc, 0xfc, sega_IN4_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort zektor_writeport[]
            = {
                new IOWritePort(0x38, 0x38, sega_sh_speech_w),
                new IOWritePort(0x3e, 0x3e, zektor1_sh_w),
                new IOWritePort(0x3f, 0x3f, zektor2_sh_w),
                new IOWritePort(0xbd, 0xbd, sega_mult1_w),
                new IOWritePort(0xbe, 0xbe, sega_mult2_w),
                new IOWritePort(0xf8, 0xf8, sega_switch_w),
                new IOWritePort(0xf9, 0xf9, coin_counter_w), /* 0x80 = enable, 0x00 = disable */
                new IOWritePort(-1) /* end of table */};

    static IOWritePort tacscan_writeport[]
            = {
                new IOWritePort(0x3f, 0x3f, tacscan_sh_w),
                new IOWritePort(0xbd, 0xbd, sega_mult1_w),
                new IOWritePort(0xbe, 0xbe, sega_mult2_w),
                new IOWritePort(0xf8, 0xf8, sega_switch_w),
                new IOWritePort(0xf9, 0xf9, coin_counter_w), /* 0x80 = enable, 0x00 = disable */
                new IOWritePort(-1) /* end of table */};

    static IOReadPort elim2_readport[]
            = {
                new IOReadPort(0x3f, 0x3f, sega_sh_r),
                new IOReadPort(0xbe, 0xbe, sega_mult_r),
                new IOReadPort(0xf8, 0xfb, sega_read_ports),
                new IOReadPort(0xfc, 0xfc, input_port_4_r),
                new IOReadPort(-1) /* end of table */};

    static IOReadPort elim4_readport[]
            = {
                new IOReadPort(0x3f, 0x3f, sega_sh_r),
                new IOReadPort(0xbe, 0xbe, sega_mult_r),
                new IOReadPort(0xf8, 0xfb, sega_read_ports),
                new IOReadPort(0xfc, 0xfc, elim4_IN4_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort elim_writeport[]
            = {
                new IOWritePort(0x3e, 0x3e, elim1_sh_w),
                new IOWritePort(0x3f, 0x3f, elim2_sh_w),
                new IOWritePort(0xbd, 0xbd, sega_mult1_w),
                new IOWritePort(0xbe, 0xbe, sega_mult2_w),
                new IOWritePort(0xf8, 0xf8, sega_switch_w),
                new IOWritePort(0xf9, 0xf9, coin_counter_w), /* 0x80 = enable, 0x00 = disable */
                new IOWritePort(-1) /* end of table */};

    static IOWritePort startrek_writeport[]
            = {
                new IOWritePort(0x38, 0x38, sega_sh_speech_w),
                new IOWritePort(0x3f, 0x3f, startrek_sh_w),
                new IOWritePort(0xbd, 0xbd, sega_mult1_w),
                new IOWritePort(0xbe, 0xbe, sega_mult2_w),
                new IOWritePort(0xf8, 0xf8, sega_switch_w),
                new IOWritePort(0xf9, 0xf9, coin_counter_w), /* 0x80 = enable, 0x00 = disable */
                new IOWritePort(-1) /* end of table */};

    /**
     * ***********************************************************************
     * Input Ports
     * ***********************************************************************
     */

    /* This fake input port is used for DIP Switch 2
	   for all games except Eliminato 4 players */
    static InputPortHandlerPtr input_ports_spacfury = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
 /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - FAKE - lazy way to move the self-test fake input port to 5 */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x01, "30000");
            PORT_DIPSETTING(0x03, "40000");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Very Hard");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x30, "5");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0d, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x0b, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0f, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x07, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xf0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xd0, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0xb0, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xf0, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x70, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_zektor = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
 /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - port 0xfc - read in machine/sega.c */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x03, "10000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPSETTING(0x02, "30000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Very Hard");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x30, "5");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0d, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x0b, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0f, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x07, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xf0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xd0, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0xb0, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xf0, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x70, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));

            PORT_START();
            /* IN8 - FAKE port for the dial */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 100, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_startrek = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
 /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - port 0xfc - read in machine/sega.c */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON4);
            PORT_BIT(0xc0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x01, "30000");
            PORT_DIPSETTING(0x03, "40000");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Medium");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Tournament");
            PORT_DIPNAME(0x30, 0x30, "Photon Torpedoes");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x10, "3");
            PORT_DIPSETTING(0x30, "4");
            PORT_DIPNAME(0x40, 0x00, "Demo Sounds?");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0d, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x0b, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0f, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x07, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xf0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xd0, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0xb0, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xf0, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x70, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));

            PORT_START();
            /* IN8 - dummy port for the dial */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 100, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_tacscan = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
 /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - port 0xfc - read in machine/sega.c */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x03, "10000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPSETTING(0x02, "30000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Very Hard");
            PORT_DIPNAME(0x30, 0x30, "Number of Ships");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "6");
            PORT_DIPSETTING(0x30, "8");
            PORT_DIPNAME(0x40, 0x00, "Demo Sounds?");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0d, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x0b, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0f, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x07, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xf0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xd0, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0xb0, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xf0, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x70, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));

            PORT_START();
            /* IN8 - FAKE port for the dial */
            PORT_ANALOG(0xff, 0x00, IPT_DIAL | IPF_CENTER, 100, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_elim2 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
 /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - port 0xfc - read in machine/sega.c */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0xf8, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x03, "None");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Very Hard");
            PORT_DIPNAME(0x30, 0x20, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            /* 0x30 gives 5 Lives */
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x09, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x05, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0d, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x03, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0x0b, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0f, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x07, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0x0a, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xf0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x90, "2 Coins/1 Credit 5/3 6/4");
            PORT_DIPSETTING(0x50, "2 Coins/1 Credit 4/3");
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xd0, "1 Coin/1 Credit 5/6");
            PORT_DIPSETTING(0x30, "1 Coin/1 Credit 4/5");
            PORT_DIPSETTING(0xb0, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xf0, "1 Coin/2 Credits 4/9");
            PORT_DIPSETTING(0x70, "1 Coin/2 Credits 5/11");
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_elim4 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 - port 0xf8 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            /* The next bit is referred to as the Service switch in the self test - it just adds a credit */
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN1, 3);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN3, 3);

            PORT_START();
            /* IN1 - port 0xf9 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 - port 0xfa */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN3 - port 0xfb */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - port 0xfc - read in machine/sega.c */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER4);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER4);

            PORT_START();
            /* IN5 - FAKE */
 /* This fake input port is used to get the status of the F2 key, */
 /* and activate the test mode, which is triggered by a NMI */
            PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);

            PORT_START();
            /* FAKE */
 /* This fake input port is used for DIP Switch 1 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x01, "10000");
            PORT_DIPSETTING(0x02, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x03, "None");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x0c, "Very Hard");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            /* 0x30 gives 5 Lives */
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* That is the coinage port in all the other games */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN8 - FAKE - port 0xfc - read in machine/sega.c */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN1, 3);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_HIGH, IPT_COIN2, 3);
            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_HIGH, IPT_COIN3, 3);
            PORT_BIT_IMPULSE(0x08, IP_ACTIVE_HIGH, IPT_COIN4, 3);
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    /**
     * *************************************************************************
     * Security Chips
     * *************************************************************************
     */
    public static InitDriverHandlerPtr init_spacfury = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0064 security chip */
            sega_security(64);
        }
    };

    public static InitDriverHandlerPtr init_zektor = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0082 security chip */
            sega_security(82);
        }
    };

    public static InitDriverHandlerPtr init_elim2 = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0070 security chip */
            sega_security(70);
        }
    };

    public static InitDriverHandlerPtr init_elim4 = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0076 security chip */
            sega_security(76);
        }
    };

    public static InitDriverHandlerPtr init_startrek = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0064 security chip */
            sega_security(64);
        }
    };

    public static InitDriverHandlerPtr init_tacscan = new InitDriverHandlerPtr() {
        public void handler() {
            /* This game uses the 315-0076 security chip */
            sega_security(76);
        }
    };

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static String spacfury_sample_names[]
            = {
                "*spacfury",
                /* Speech samples */
                "sf01.wav",
                "sf02.wav",
                "sf03.wav",
                "sf04.wav",
                "sf05.wav",
                "sf06.wav",
                "sf07.wav",
                "sf08.wav",
                "sf09.wav",
                "sf0a.wav",
                "sf0b.wav",
                "sf0c.wav",
                "sf0d.wav",
                "sf0e.wav",
                "sf0f.wav",
                "sf10.wav",
                "sf11.wav",
                "sf12.wav",
                "sf13.wav",
                "sf14.wav",
                "sf15.wav",
                /* Sound samples */
                "sfury1.wav",
                "sfury2.wav",
                "sfury3.wav",
                "sfury4.wav",
                "sfury5.wav",
                "sfury6.wav",
                "sfury7.wav",
                "sfury8.wav",
                "sfury9.wav",
                "sfury10.wav",
                null /* end of array */};

    static Samplesinterface spacfury_samples_interface = new Samplesinterface(
            9, /* 9 channels */
            25, /* volume */
            spacfury_sample_names
    );

    static CustomSound_interface sega_custom_interface = new CustomSound_interface(
            sega_sh_start,
            null,
            sega_sh_update
    );

    static MachineDriver machine_driver_spacfury = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, spacfury_readport, spacfury_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(512, 1536, 552, 1464),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        spacfury_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        sega_custom_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static String zektor_sample_names[]
            = {
                "*zektor",
                "zk01.wav", /* 1 */
                "zk02.wav",
                "zk03.wav",
                "zk04.wav",
                "zk05.wav",
                "zk06.wav",
                "zk07.wav",
                "zk08.wav",
                "zk09.wav",
                "zk0a.wav",
                "zk0b.wav",
                "zk0c.wav",
                "zk0d.wav",
                "zk0e.wav",
                "zk0f.wav",
                "zk10.wav",
                "zk11.wav",
                "zk12.wav",
                "zk13.wav",
                "elim1.wav", /* 19 fireball */
                "elim2.wav", /* 20 bounce */
                "elim3.wav", /* 21 Skitter */
                "elim4.wav", /* 22 Eliminator */
                "elim5.wav", /* 23 Electron */
                "elim6.wav", /* 24 fire */
                "elim7.wav", /* 25 thrust */
                "elim8.wav", /* 26 Electron */
                "elim9.wav", /* 27 small explosion */
                "elim10.wav", /* 28 med explosion */
                "elim11.wav", /* 29 big explosion */
                /* Missing Zizzer */
                /* Missing City fly by */
                /* Missing Rotation Rings */
                null /* end of array */};

    static Samplesinterface zektor_samples_interface = new Samplesinterface(
            12, /* only speech for now */
            25, /* volume */
            zektor_sample_names
    );

    static MachineDriver machine_driver_zektor = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, zektor_readport, zektor_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(512, 1536, 624, 1432),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        zektor_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        sega_custom_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static String tacscan_sample_names[]
            = {
                "*tacscan",
                /* Player ship thrust sounds */
                "01.wav",
                "02.wav",
                "03.wav",
                "plaser.wav",
                "pexpl.wav",
                "pship.wav",
                "tunnelh.wav",
                "sthrust.wav",
                "slaser.wav",
                "sexpl.wav",
                "eshot.wav",
                "eexpl.wav",
                "tunnelw.wav",
                "flight1.wav",
                "flight2.wav",
                "flight3.wav",
                "flight4.wav",
                "flight5.wav",
                "formatn.wav",
                "warp.wav",
                "credit.wav",
                "1up.wav",
                null /* end of array */};

    static Samplesinterface tacscan_samples_interface = new Samplesinterface(
            12, /* 12 channels */
            25, /* volume */
            tacscan_sample_names
    );

    static CustomSound_interface tacscan_custom_interface = new CustomSound_interface(
            tacscan_sh_start,
            null,
            tacscan_sh_update
    );

    static MachineDriver machine_driver_tacscan = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, zektor_readport, tacscan_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(496, 1552, 592, 1456),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        tacscan_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        tacscan_custom_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */

    /* Eliminator sound samples (all versions) */
    static String elim_sample_names[]
            = {
                "*elim2",
                "elim1.wav",
                "elim2.wav",
                "elim3.wav",
                "elim4.wav",
                "elim5.wav",
                "elim6.wav",
                "elim7.wav",
                "elim8.wav",
                "elim9.wav",
                "elim10.wav",
                "elim11.wav",
                "elim12.wav",
                null /* end of array */};

    static Samplesinterface elim2_samples_interface = new Samplesinterface(
            8, /* 8 channels */
            25, /* volume */
            elim_sample_names
    );

    static MachineDriver machine_driver_elim2 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, elim2_readport, elim_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(512, 1536, 600, 1440),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        elim2_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        sega_custom_interface
                )
            }
    );

    static MachineDriver machine_driver_elim4 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, elim4_readport, elim_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(512, 1536, 600, 1440),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        elim2_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        sega_custom_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static String startrek_sample_names[]
            = {
                "*startrek",
                /* Speech samples */
                "st01.wav",
                "st02.wav",
                "st03.wav",
                "st04.wav",
                "st05.wav",
                "st06.wav",
                "st07.wav",
                "st08.wav",
                "st09.wav",
                "st0a.wav",
                "st0b.wav",
                "st0c.wav",
                "st0d.wav",
                "st0e.wav",
                "st0f.wav",
                "st10.wav",
                "st11.wav",
                "st12.wav",
                "st13.wav",
                "st14.wav",
                "st15.wav",
                "st16.wav",
                "st17.wav",
                /* Sound samples */
                "trek1.wav",
                "trek2.wav",
                "trek3.wav",
                "trek4.wav",
                "trek5.wav",
                "trek6.wav",
                "trek7.wav",
                "trek8.wav",
                "trek9.wav",
                "trek10.wav",
                "trek11.wav",
                "trek12.wav",
                "trek13.wav",
                "trek14.wav",
                "trek15.wav",
                "trek16.wav",
                "trek17.wav",
                "trek18.wav",
                "trek19.wav",
                "trek20.wav",
                "trek21.wav",
                "trek22.wav",
                "trek23.wav",
                "trek24.wav",
                "trek25.wav",
                "trek26.wav",
                "trek27.wav",
                "trek28.wav",
                null /* end of array */};

    static Samplesinterface startrek_samples_interface = new Samplesinterface(
            10, /* 10 channels */
            25, /* volume */
            startrek_sample_names
    );

    static MachineDriver machine_driver_startrek = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3867120, /* 3.86712 MHz */
                        readmem, writemem, zektor_readport, startrek_writeport,
                        null, 0, /* no vblank interrupt */
                        sega_interrupt, 40 /* 40 Hz */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            400, 300, new rectangle(512, 1536, 616, 1464),
            null,
            256, 256,
            sega_init_colors,
            VIDEO_TYPE_VECTOR,
            null,
            sega_vh_start,
            sega_vh_stop,
            sega_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        startrek_samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually plays the samples */
                        sega_custom_interface
                )
            }
    );

    static RomLoadHandlerPtr rom_spacfury = new RomLoadHandlerPtr() {
        public void handler() {
            /* Revision C */
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("969c.u25", 0x0000, 0x0800, 0x411207f2);
            ROM_LOAD("960c.u1", 0x0800, 0x0800, 0xd071ab7e);
            ROM_LOAD("961c.u2", 0x1000, 0x0800, 0xaebc7b97);
            ROM_LOAD("962c.u3", 0x1800, 0x0800, 0xdbbba35e);
            ROM_LOAD("963c.u4", 0x2000, 0x0800, 0xd9e9eadc);
            ROM_LOAD("964c.u5", 0x2800, 0x0800, 0x7ed947b6);
            ROM_LOAD("965c.u6", 0x3000, 0x0800, 0xd2443a22);
            ROM_LOAD("966c.u7", 0x3800, 0x0800, 0x1985ccfc);
            ROM_LOAD("967c.u8", 0x4000, 0x0800, 0x330f0751);
            ROM_LOAD("968c.u9", 0x4800, 0x0800, 0x8366eadb);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacfura = new RomLoadHandlerPtr() {
        public void handler() {
            /* Revision A */
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("969a.u25", 0x0000, 0x0800, 0x896a615c);
            ROM_LOAD("960a.u1", 0x0800, 0x0800, 0xe1ea7964);
            ROM_LOAD("961a.u2", 0x1000, 0x0800, 0xcdb04233);
            ROM_LOAD("962a.u3", 0x1800, 0x0800, 0x5f03e632);
            ROM_LOAD("963a.u4", 0x2000, 0x0800, 0x45a77b44);
            ROM_LOAD("964a.u5", 0x2800, 0x0800, 0xba008f8b);
            ROM_LOAD("965a.u6", 0x3000, 0x0800, 0x78677d31);
            ROM_LOAD("966a.u7", 0x3800, 0x0800, 0xa8a51105);
            ROM_LOAD("967a.u8", 0x4000, 0x0800, 0xd60f667d);
            ROM_LOAD("968a.u9", 0x4800, 0x0800, 0xaea85b6a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_zektor = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1611.cpu", 0x0000, 0x0800, 0x6245aa23);
            ROM_LOAD("1586.rom", 0x0800, 0x0800, 0xefeb4fb5);
            ROM_LOAD("1587.rom", 0x1000, 0x0800, 0xdaa6c25c);
            ROM_LOAD("1588.rom", 0x1800, 0x0800, 0x62b67dde);
            ROM_LOAD("1589.rom", 0x2000, 0x0800, 0xc2db0ba4);
            ROM_LOAD("1590.rom", 0x2800, 0x0800, 0x4d948414);
            ROM_LOAD("1591.rom", 0x3000, 0x0800, 0xb0556a6c);
            ROM_LOAD("1592.rom", 0x3800, 0x0800, 0x750ecadf);
            ROM_LOAD("1593.rom", 0x4000, 0x0800, 0x34f8850f);
            ROM_LOAD("1594.rom", 0x4800, 0x0800, 0x52b22ab2);
            ROM_LOAD("1595.rom", 0x5000, 0x0800, 0xa704d142);
            ROM_LOAD("1596.rom", 0x5800, 0x0800, 0x6975e33d);
            ROM_LOAD("1597.rom", 0x6000, 0x0800, 0xd48ab5c2);
            ROM_LOAD("1598.rom", 0x6800, 0x0800, 0xab54a94c);
            ROM_LOAD("1599.rom", 0x7000, 0x0800, 0xc9d4f3a5);
            ROM_LOAD("1600.rom", 0x7800, 0x0800, 0x893b7dbc);
            ROM_LOAD("1601.rom", 0x8000, 0x0800, 0x867bdf4f);
            ROM_LOAD("1602.rom", 0x8800, 0x0800, 0xbd447623);
            ROM_LOAD("1603.rom", 0x9000, 0x0800, 0x9f8f10e8);
            ROM_LOAD("1604.rom", 0x9800, 0x0800, 0xad2f0f6c);
            ROM_LOAD("1605.rom", 0xa000, 0x0800, 0xe27d7144);
            ROM_LOAD("1606.rom", 0xa800, 0x0800, 0x7965f636);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_tacscan = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1711a", 0x0000, 0x0800, 0x0da13158);
            ROM_LOAD("1670c", 0x0800, 0x0800, 0x98de6fd5);
            ROM_LOAD("1671a", 0x1000, 0x0800, 0xdc400074);
            ROM_LOAD("1672a", 0x1800, 0x0800, 0x2caf6f7e);
            ROM_LOAD("1673a", 0x2000, 0x0800, 0x1495ce3d);
            ROM_LOAD("1674a", 0x2800, 0x0800, 0xab7fc5d9);
            ROM_LOAD("1675a", 0x3000, 0x0800, 0xcf5e5016);
            ROM_LOAD("1676a", 0x3800, 0x0800, 0xb61a3ab3);
            ROM_LOAD("1677a", 0x4000, 0x0800, 0xbc0273b1);
            ROM_LOAD("1678b", 0x4800, 0x0800, 0x7894da98);
            ROM_LOAD("1679a", 0x5000, 0x0800, 0xdb865654);
            ROM_LOAD("1680a", 0x5800, 0x0800, 0x2c2454de);
            ROM_LOAD("1681a", 0x6000, 0x0800, 0x77028885);
            ROM_LOAD("1682a", 0x6800, 0x0800, 0xbabe5cf1);
            ROM_LOAD("1683a", 0x7000, 0x0800, 0x1b98b618);
            ROM_LOAD("1684a", 0x7800, 0x0800, 0xcb3ded3b);
            ROM_LOAD("1685a", 0x8000, 0x0800, 0x43016a79);
            ROM_LOAD("1686a", 0x8800, 0x0800, 0xa4397772);
            ROM_LOAD("1687a", 0x9000, 0x0800, 0x002f3bc4);
            ROM_LOAD("1688a", 0x9800, 0x0800, 0x0326d87a);
            ROM_LOAD("1709a", 0xa000, 0x0800, 0xf35ed1ec);
            ROM_LOAD("1710a", 0xa800, 0x0800, 0x6203be22);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_elim2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("cpu_u25.969", 0x0000, 0x0800, 0x411207f2);
            ROM_LOAD("1333", 0x0800, 0x0800, 0xfd2a2916);
            ROM_LOAD("1334", 0x1000, 0x0800, 0x79eb5548);
            ROM_LOAD("1335", 0x1800, 0x0800, 0x3944972e);
            ROM_LOAD("1336", 0x2000, 0x0800, 0x852f7b4d);
            ROM_LOAD("1337", 0x2800, 0x0800, 0xcf932b08);
            ROM_LOAD("1338", 0x3000, 0x0800, 0x99a3f3c9);
            ROM_LOAD("1339", 0x3800, 0x0800, 0xd35f0fa3);
            ROM_LOAD("1340", 0x4000, 0x0800, 0x8fd4da21);
            ROM_LOAD("1341", 0x4800, 0x0800, 0x629c9a28);
            ROM_LOAD("1342", 0x5000, 0x0800, 0x643df651);
            ROM_LOAD("1343", 0x5800, 0x0800, 0xd29d70d2);
            ROM_LOAD("1344", 0x6000, 0x0800, 0xc5e153a3);
            ROM_LOAD("1345", 0x6800, 0x0800, 0x40597a92);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_elim2a = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("cpu_u25.969", 0x0000, 0x0800, 0x411207f2);
            ROM_LOAD("1158", 0x0800, 0x0800, 0xa40ac3a5);
            ROM_LOAD("1159", 0x1000, 0x0800, 0xff100604);
            ROM_LOAD("1160a", 0x1800, 0x0800, 0xebfe33bd);
            ROM_LOAD("1161a", 0x2000, 0x0800, 0x03d41db3);
            ROM_LOAD("1162a", 0x2800, 0x0800, 0xf2c7ece3);
            ROM_LOAD("1163a", 0x3000, 0x0800, 0x1fc58b00);
            ROM_LOAD("1164a", 0x3800, 0x0800, 0xf37480d1);
            ROM_LOAD("1165a", 0x4000, 0x0800, 0x328819f8);
            ROM_LOAD("1166a", 0x4800, 0x0800, 0x1b8e8380);
            ROM_LOAD("1167a", 0x5000, 0x0800, 0x16aa3156);
            ROM_LOAD("1168a", 0x5800, 0x0800, 0x3c7c893a);
            ROM_LOAD("1169a", 0x6000, 0x0800, 0x5cee23b1);
            ROM_LOAD("1170a", 0x6800, 0x0800, 0x8cdacd35);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_elim4 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1390_cpu.u25", 0x0000, 0x0800, 0x97010c3e);
            ROM_LOAD("1347", 0x0800, 0x0800, 0x657d7320);
            ROM_LOAD("1348", 0x1000, 0x0800, 0xb15fe578);
            ROM_LOAD("1349", 0x1800, 0x0800, 0x0702b586);
            ROM_LOAD("1350", 0x2000, 0x0800, 0x4168dd3b);
            ROM_LOAD("1351", 0x2800, 0x0800, 0xc950f24c);
            ROM_LOAD("1352", 0x3000, 0x0800, 0xdc8c91cc);
            ROM_LOAD("1353", 0x3800, 0x0800, 0x11eda631);
            ROM_LOAD("1354", 0x4000, 0x0800, 0xb9dd6e7a);
            ROM_LOAD("1355", 0x4800, 0x0800, 0xc92c7237);
            ROM_LOAD("1356", 0x5000, 0x0800, 0x889b98e3);
            ROM_LOAD("1357", 0x5800, 0x0800, 0xd79248a5);
            ROM_LOAD("1358", 0x6000, 0x0800, 0xc5dabc77);
            ROM_LOAD("1359", 0x6800, 0x0800, 0x24c8e5d8);
            ROM_LOAD("1360", 0x7000, 0x0800, 0x96d48238);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_startrek = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("cpu1873", 0x0000, 0x0800, 0xbe46f5d9);
            ROM_LOAD("1848", 0x0800, 0x0800, 0x65e3baf3);
            ROM_LOAD("1849", 0x1000, 0x0800, 0x8169fd3d);
            ROM_LOAD("1850", 0x1800, 0x0800, 0x78fd68dc);
            ROM_LOAD("1851", 0x2000, 0x0800, 0x3f55ab86);
            ROM_LOAD("1852", 0x2800, 0x0800, 0x2542ecfb);
            ROM_LOAD("1853", 0x3000, 0x0800, 0x75c2526a);
            ROM_LOAD("1854", 0x3800, 0x0800, 0x096d75d0);
            ROM_LOAD("1855", 0x4000, 0x0800, 0xbc7b9a12);
            ROM_LOAD("1856", 0x4800, 0x0800, 0xed9fe2fb);
            ROM_LOAD("1857", 0x5000, 0x0800, 0x28699d45);
            ROM_LOAD("1858", 0x5800, 0x0800, 0x3a7593cb);
            ROM_LOAD("1859", 0x6000, 0x0800, 0x5b11886b);
            ROM_LOAD("1860", 0x6800, 0x0800, 0x62eb96e6);
            ROM_LOAD("1861", 0x7000, 0x0800, 0x99852d1d);
            ROM_LOAD("1862", 0x7800, 0x0800, 0x76ce27b2);
            ROM_LOAD("1863", 0x8000, 0x0800, 0xdd92d187);
            ROM_LOAD("1864", 0x8800, 0x0800, 0xe37d3a1e);
            ROM_LOAD("1865", 0x9000, 0x0800, 0xb2ec8125);
            ROM_LOAD("1866", 0x9800, 0x0800, 0x6f188354);
            ROM_LOAD("1867", 0xa000, 0x0800, 0xb0a3eae8);
            ROM_LOAD("1868", 0xa800, 0x0800, 0x8b4e2e07);
            ROM_LOAD("1869", 0xb000, 0x0800, 0xe5663070);
            ROM_LOAD("1870", 0xb800, 0x0800, 0x4340616d);

            // I'm not sure where these roms are supposed to go, but they are speech
            // related (from what I've read), so I just took a wild guess here,
            // until their location is determined and speech is emulated, plus, it
            // helps make sure everyone has them for the future... MRH
            ROM_LOAD("1670", 0xc000, 0x0800, 0xb779884b);
            ROM_LOAD("1871", 0xc800, 0x1000, 0x03713920);
            ROM_LOAD("1872", 0xd800, 0x1000, 0xebb5c3a9);
            ROM_END();
        }
    };

    public static GameDriver driver_spacfury = new GameDriver("1981", "spacfury", "sega.java", rom_spacfury, null, machine_driver_spacfury, input_ports_spacfury, init_spacfury, ROT0, "Sega", "Space Fury (revision C)");
    public static GameDriver driver_spacfura = new GameDriver("1981", "spacfura", "sega.java", rom_spacfura, driver_spacfury, machine_driver_spacfury, input_ports_spacfury, init_spacfury, ROT0, "Sega", "Space Fury (revision A)");
    public static GameDriver driver_zektor = new GameDriver("1982", "zektor", "sega.java", rom_zektor, null, machine_driver_zektor, input_ports_zektor, init_zektor, ROT0, "Sega", "Zektor");
    public static GameDriver driver_tacscan = new GameDriver("1982", "tacscan", "sega.java", rom_tacscan, null, machine_driver_tacscan, input_ports_tacscan, init_tacscan, ROT270, "Sega", "Tac/Scan");
    public static GameDriver driver_elim2 = new GameDriver("1981", "elim2", "sega.java", rom_elim2, null, machine_driver_elim2, input_ports_elim2, init_elim2, ROT0, "Gremlin", "Eliminator (2 Players, set 1)");
    public static GameDriver driver_elim2a = new GameDriver("1981", "elim2a", "sega.java", rom_elim2a, driver_elim2, machine_driver_elim2, input_ports_elim2, init_elim2, ROT0, "Gremlin", "Eliminator (2 Players, set 2)");
    public static GameDriver driver_elim4 = new GameDriver("1981", "elim4", "sega.java", rom_elim4, driver_elim2, machine_driver_elim4, input_ports_elim4, init_elim4, ROT0, "Gremlin", "Eliminator (4 Players)");
    public static GameDriver driver_startrek = new GameDriver("1982", "startrek", "sega.java", rom_startrek, null, machine_driver_startrek, input_ports_startrek, init_startrek, ROT0, "Sega", "Star Trek");
}
