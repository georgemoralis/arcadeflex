/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.docastle.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.sn76496H.*;
import static arcadeflex.v036.sound.sn76496.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.docastle.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;

public class docastle {

    static MemoryReadAddress docastle_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x97ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa008, docastle_shared0_r),
                new MemoryReadAddress(0xb800, 0xbbff, videoram_r), /* mirror of video ram */
                new MemoryReadAddress(0xbc00, 0xbfff, colorram_r), /* mirror of color ram */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress docastle_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x97ff, MWA_RAM),
                new MemoryWriteAddress(0x9800, 0x99ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xa000, 0xa008, docastle_shared1_w),
                new MemoryWriteAddress(0xa800, 0xa800, watchdog_reset_w),
                new MemoryWriteAddress(0xb000, 0xb3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xb400, 0xb7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe000, docastle_nmitrigger),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress dorunrun_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x37ff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x9fff, MRA_ROM),
                new MemoryReadAddress(0xa000, 0xa008, docastle_shared0_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress dorunrun_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x37ff, MWA_RAM),
                new MemoryWriteAddress(0x3800, 0x39ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x4000, 0x9fff, MWA_ROM),
                new MemoryWriteAddress(0xa000, 0xa008, docastle_shared1_w),
                new MemoryWriteAddress(0xb000, 0xb3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xb400, 0xb7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xb800, 0xb800, docastle_nmitrigger),
                new MemoryWriteAddress(0xa800, 0xa800, watchdog_reset_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress docastle_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa008, docastle_shared1_r),
                new MemoryReadAddress(0xc003, 0xc003, input_port_0_r),
                new MemoryReadAddress(0xc083, 0xc083, input_port_0_r),
                new MemoryReadAddress(0xc005, 0xc005, input_port_1_r),
                new MemoryReadAddress(0xc085, 0xc085, input_port_1_r),
                new MemoryReadAddress(0xc007, 0xc007, input_port_2_r),
                new MemoryReadAddress(0xc087, 0xc087, input_port_2_r),
                new MemoryReadAddress(0xc002, 0xc002, input_port_3_r),
                new MemoryReadAddress(0xc082, 0xc082, input_port_3_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_4_r),
                new MemoryReadAddress(0xc081, 0xc081, input_port_4_r),
                new MemoryReadAddress(0xc004, 0xc004, docastle_flipscreen_off_r),
                new MemoryReadAddress(0xc084, 0xc084, docastle_flipscreen_on_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress docastle_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa008, docastle_shared0_w),
                new MemoryWriteAddress(0xe000, 0xe000, SN76496_0_w),
                new MemoryWriteAddress(0xe400, 0xe400, SN76496_1_w),
                new MemoryWriteAddress(0xe800, 0xe800, SN76496_2_w),
                new MemoryWriteAddress(0xec00, 0xec00, SN76496_3_w),
                new MemoryWriteAddress(0xc004, 0xc004, docastle_flipscreen_off_w),
                new MemoryWriteAddress(0xc084, 0xc084, docastle_flipscreen_on_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress dorunrun_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xc003, 0xc003, input_port_0_r),
                new MemoryReadAddress(0xc083, 0xc083, input_port_0_r),
                new MemoryReadAddress(0xc005, 0xc005, input_port_1_r),
                new MemoryReadAddress(0xc085, 0xc085, input_port_1_r),
                new MemoryReadAddress(0xc007, 0xc007, input_port_2_r),
                new MemoryReadAddress(0xc087, 0xc087, input_port_2_r),
                new MemoryReadAddress(0xc002, 0xc002, input_port_3_r),
                new MemoryReadAddress(0xc082, 0xc082, input_port_3_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_4_r),
                new MemoryReadAddress(0xc081, 0xc081, input_port_4_r),
                new MemoryReadAddress(0xc004, 0xc004, docastle_flipscreen_off_r),
                new MemoryReadAddress(0xc084, 0xc084, docastle_flipscreen_on_r),
                new MemoryReadAddress(0xe000, 0xe008, docastle_shared1_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress dorunrun_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa000, 0xa000, SN76496_0_w),
                new MemoryWriteAddress(0xa400, 0xa400, SN76496_1_w),
                new MemoryWriteAddress(0xa800, 0xa800, SN76496_2_w),
                new MemoryWriteAddress(0xac00, 0xac00, SN76496_3_w),
                new MemoryWriteAddress(0xc004, 0xc004, docastle_flipscreen_off_w),
                new MemoryWriteAddress(0xc084, 0xc084, docastle_flipscreen_on_w),
                new MemoryWriteAddress(0xe000, 0xe008, docastle_shared0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /* Coinage used for all games */
    static InputPortHandlerPtr input_ports_docastle = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);/* reported as 2 Player Fire */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as 2 Player Jump */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as test */
 /* coin input must be active for 32 frames to be consistently recognized */

            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_COIN3, 32);
            PORT_DIPNAME(0x08, 0x08, "Freeze");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            /* flip screen? doesn't work */

            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Extra");
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0x40, "5");

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x01, 0x02, 0x03, 0x04, 0x05 all give 1 Coin/1 Credit */
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x60, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x70, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x10, 0x20, 0x30, 0x40, 0x50 all give 1 Coin/1 Credit */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_dorunrun = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* Reported as Test */
 /* coin input must be active for 32 frames to be consistently recognized */

            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_COIN3, 32);
            PORT_DIPNAME(0x08, 0x08, "Freeze");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Extra");
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x40, 0x40, "Special");
            PORT_DIPSETTING(0x40, "Given");
            PORT_DIPSETTING(0x00, "Not Given");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x00, "5");

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x01, 0x02, 0x03, 0x04, 0x05 all give 1 Coin/1 Credit */
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x60, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x70, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x10, 0x20, 0x30, 0x40, 0x50 all give 1 Coin/1 Credit */
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_dowild = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);/* reported as 2 Player Fire */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as 2 Player Jump */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as test */
 /* coin input must be active for 32 frames to be consistently recognized */

            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_COIN3, 32);
            PORT_DIPNAME(0x08, 0x08, "Freeze");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Extra");
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));

            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x40, 0x40, "Special");
            PORT_DIPSETTING(0x40, "Given");
            PORT_DIPSETTING(0x00, "Not Given");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x00, "5");

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x01, 0x02, 0x03, 0x04, 0x05 all give 1 Coin/1 Credit */
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x60, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x70, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x10, 0x20, 0x30, 0x40, 0x50 all give 1 Coin/1 Credit */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_jjack = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);/* reported as 2 Player Fire */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as 2 Player Jump */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as test */
 /* coin input must be active for 32 frames to be consistently recognized */

            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_COIN3, 32);
            PORT_DIPNAME(0x08, 0x08, "Freeze");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* reported as not used */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, "Difficulty?");
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Extra?");
            PORT_DIPSETTING(0x10, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0x40, "5");

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x01, 0x02, 0x03, 0x04, 0x05 all give 1 Coin/1 Credit */
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x60, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x70, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x10, 0x20, 0x30, 0x40, 0x50 all give 1 Coin/1 Credit */

            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_kickridr = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* Reported as Test */
 /* coin input must be active for 32 frames to be consistently recognized */

            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_COIN3, 32);
            PORT_DIPNAME(0x08, 0x08, "Freeze");
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, "Difficulty?");
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "DSW4");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x40, 0x40, "DSW2");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "DSW1");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x06, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x01, 0x02, 0x03, 0x04, 0x05 all give 1 Coin/1 Credit */
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x60, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0xa0, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x70, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x90, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* 0x10, 0x20, 0x30, 0x40, 0x50 all give 1 Coin/1 Credit */
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 8, 12, 16, 20, 24, 28},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 8, 12, 16, 20, 24, 28,
                32, 36, 40, 44, 48, 52, 56, 60},
            new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 64 * 16, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static SN76496interface sn76496_interface = new SN76496interface(
            4, /* 4 chips */
            new int[]{4000000, 4000000, 4000000, 4000000}, /* 4 Mhz? */
            new int[]{25, 25, 25, 25}
    );

    static MachineDriver machine_driver_docastle = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz */
                        docastle_readmem, docastle_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz */
                        docastle_readmem2, docastle_writemem2, null, null,
                        interrupt, 8
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when communication takes place */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            258, 96 * 16,
            docastle_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            docastle_vh_start,
            docastle_vh_stop,
            docastle_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
                )
            }
    );

    static MachineDriver machine_driver_dorunrun = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz */
                        dorunrun_readmem, dorunrun_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz */
                        dorunrun_readmem2, dorunrun_writemem2, null, null,
                        interrupt, 8
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when communication takes place */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            258, 96 * 16,
            dorunrun_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            docastle_vh_start,
            docastle_vh_stop,
            docastle_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
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
    static RomLoadHandlerPtr rom_docastle = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("01p_a1.bin", 0x0000, 0x2000, 0x17c6fc24);
            ROM_LOAD("01n_a2.bin", 0x2000, 0x2000, 0x1d2fc7f4);
            ROM_LOAD("01l_a3.bin", 0x4000, 0x2000, 0x71a70ba9);
            ROM_LOAD("01k_a4.bin", 0x6000, 0x2000, 0x479a745e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("07n_a0.bin", 0x0000, 0x4000, 0xf23b5cdb);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("03a_a5.bin", 0x0000, 0x4000, 0x0636b8f4);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("04m_a6.bin", 0x0000, 0x2000, 0x3bbc9b26);
            ROM_LOAD("04l_a7.bin", 0x2000, 0x2000, 0x3dfaa9d1);
            ROM_LOAD("04j_a8.bin", 0x4000, 0x2000, 0x9afb16e9);
            ROM_LOAD("04h_a9.bin", 0x6000, 0x2000, 0xaf24bce0);

            ROM_REGION(0x0400, REGION_PROMS);
            ROM_LOAD("09c.bin", 0x0000, 0x0200, 0x066f52bc);/* color prom */

            ROM_LOAD("01d.bin", 0x0200, 0x0200, 0x2747ca77);/* ??? */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_docastl2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("a1", 0x0000, 0x2000, 0x0d81fafc);
            ROM_LOAD("a2", 0x2000, 0x2000, 0xa13dc4ac);
            ROM_LOAD("a3", 0x4000, 0x2000, 0xa1f04ffb);
            ROM_LOAD("a4", 0x6000, 0x2000, 0x1fb14aa6);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a10", 0x0000, 0x4000, 0x45f7f69b);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("03a_a5.bin", 0x0000, 0x4000, 0x0636b8f4);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("04m_a6.bin", 0x0000, 0x2000, 0x3bbc9b26);
            ROM_LOAD("04l_a7.bin", 0x2000, 0x2000, 0x3dfaa9d1);
            ROM_LOAD("04j_a8.bin", 0x4000, 0x2000, 0x9afb16e9);
            ROM_LOAD("04h_a9.bin", 0x6000, 0x2000, 0xaf24bce0);

            ROM_REGION(0x0400, REGION_PROMS);
            ROM_LOAD("09c.bin", 0x0000, 0x0200, 0x066f52bc);/* color prom */

            ROM_LOAD("01d.bin", 0x0200, 0x0200, 0x2747ca77);/* ??? */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_douni = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("dorev1.bin", 0x0000, 0x2000, 0x1e2cbb3c);
            ROM_LOAD("dorev2.bin", 0x2000, 0x2000, 0x18418f83);
            ROM_LOAD("dorev3.bin", 0x4000, 0x2000, 0x7b9e2061);
            ROM_LOAD("dorev4.bin", 0x6000, 0x2000, 0xe013954d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("dorev10.bin", 0x0000, 0x4000, 0x4b1925e3);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("03a_a5.bin", 0x0000, 0x4000, 0x0636b8f4);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("dorev6.bin", 0x0000, 0x2000, 0x9e335bf8);
            ROM_LOAD("dorev7.bin", 0x2000, 0x2000, 0xf5d5701d);
            ROM_LOAD("dorev8.bin", 0x4000, 0x2000, 0x7143ca68);
            ROM_LOAD("dorev9.bin", 0x6000, 0x2000, 0x893fc004);

            ROM_REGION(0x0400, REGION_PROMS);
            ROM_LOAD("dorevc9.bin", 0x0000, 0x0200, 0x96624ebe);/* color prom */

            ROM_LOAD("01d.bin", 0x0200, 0x0200, 0x2747ca77);/* ??? */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dorunruc = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("rev-0-1.p1", 0x0000, 0x2000, 0x49906ebd);
            ROM_LOAD("rev-0-2.n1", 0x2000, 0x2000, 0xdbe3e7db);
            ROM_LOAD("rev-0-3.l1", 0x4000, 0x2000, 0xe9b8181a);
            ROM_LOAD("rev-0-4.k1", 0x6000, 0x2000, 0xa63d0b89);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("rev-0-2.n7", 0x0000, 0x4000, 0x6dac2fa3);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rev-0-5.a3", 0x0000, 0x4000, 0xe20795b7);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2764.m4", 0x0000, 0x2000, 0x4bb231a0);
            ROM_LOAD("2764.l4", 0x2000, 0x2000, 0x0c08508a);
            ROM_LOAD("2764.j4", 0x4000, 0x2000, 0x79287039);
            ROM_LOAD("2764.h4", 0x6000, 0x2000, 0x523aa999);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("dorunrun.clr", 0x0000, 0x0100, 0xd5bab5d5);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dorunrun = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("2764.p1", 0x0000, 0x2000, 0x95c86f8e);
            ROM_LOAD("2764.l1", 0x4000, 0x2000, 0xe9a65ba7);
            ROM_LOAD("2764.k1", 0x6000, 0x2000, 0xb1195d3d);
            ROM_LOAD("2764.n1", 0x8000, 0x2000, 0x6a8160d1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("27128.p7", 0x0000, 0x4000, 0x8b06d461);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("27128.a3", 0x0000, 0x4000, 0x4be96dcf);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2764.m4", 0x0000, 0x2000, 0x4bb231a0);
            ROM_LOAD("2764.l4", 0x2000, 0x2000, 0x0c08508a);
            ROM_LOAD("2764.j4", 0x4000, 0x2000, 0x79287039);
            ROM_LOAD("2764.h4", 0x6000, 0x2000, 0x523aa999);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("dorunrun.clr", 0x0000, 0x0100, 0xd5bab5d5);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dorunru2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("p1", 0x0000, 0x2000, 0x12a99365);
            ROM_LOAD("l1", 0x4000, 0x2000, 0x38609287);
            ROM_LOAD("k1", 0x6000, 0x2000, 0x099aaf54);
            ROM_LOAD("n1", 0x8000, 0x2000, 0x4f8fcbae);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("27128.p7", 0x0000, 0x4000, 0x8b06d461);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("27128.a3", 0x0000, 0x4000, 0x4be96dcf);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("2764.m4", 0x0000, 0x2000, 0x4bb231a0);
            ROM_LOAD("2764.l4", 0x2000, 0x2000, 0x0c08508a);
            ROM_LOAD("2764.j4", 0x4000, 0x2000, 0x79287039);
            ROM_LOAD("2764.h4", 0x6000, 0x2000, 0x523aa999);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("dorunrun.clr", 0x0000, 0x0100, 0xd5bab5d5);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spiero = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("sp1.bin", 0x0000, 0x2000, 0x08d23e38);
            ROM_LOAD("sp3.bin", 0x4000, 0x2000, 0xfaa0c18c);
            ROM_LOAD("sp4.bin", 0x6000, 0x2000, 0x639b4e5d);
            ROM_LOAD("sp2.bin", 0x8000, 0x2000, 0x3a29ccb0);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("27128.p7", 0x0000, 0x4000, 0x8b06d461);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sp5.bin", 0x0000, 0x4000, 0x1b704bb0);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sp6.bin", 0x0000, 0x2000, 0x00f893a7);
            ROM_LOAD("sp7.bin", 0x2000, 0x2000, 0x173e5c6a);
            ROM_LOAD("sp8.bin", 0x4000, 0x2000, 0x2e66525a);
            ROM_LOAD("sp9.bin", 0x6000, 0x2000, 0x9c571525);

            ROM_REGION(0x0400, REGION_PROMS);
            ROM_LOAD("bprom1.bin", 0x0000, 0x0200, 0xfc1b66ff);/* color prom */

            ROM_LOAD("bprom2.bin", 0x0200, 0x0200, 0x2747ca77);/* ??? */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dowild = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("w1", 0x0000, 0x2000, 0x097de78b);
            ROM_LOAD("w3", 0x4000, 0x2000, 0xfc6a1cbb);
            ROM_LOAD("w4", 0x6000, 0x2000, 0x8aac1d30);
            ROM_LOAD("w2", 0x8000, 0x2000, 0x0914ab69);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("w10", 0x0000, 0x4000, 0xd1f37fba);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("w5", 0x0000, 0x4000, 0xb294b151);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("w6", 0x0000, 0x2000, 0x57e0208b);
            ROM_LOAD("w7", 0x2000, 0x2000, 0x5001a6f7);
            ROM_LOAD("w8", 0x4000, 0x2000, 0xec503251);
            ROM_LOAD("w9", 0x6000, 0x2000, 0xaf7bd7eb);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("dowild.clr", 0x0000, 0x0100, 0xa703dea5);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_jjack = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("j1.bin", 0x0000, 0x2000, 0x87f29bd2);
            ROM_LOAD("j3.bin", 0x4000, 0x2000, 0x35b0517e);
            ROM_LOAD("j4.bin", 0x6000, 0x2000, 0x35bb316a);
            ROM_LOAD("j2.bin", 0x8000, 0x2000, 0xdec52e80);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("j0.bin", 0x0000, 0x4000, 0xab042f04);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j5.bin", 0x0000, 0x4000, 0x75038ff9);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j6.bin", 0x0000, 0x2000, 0x5937bd7b);
            ROM_LOAD("j7.bin", 0x2000, 0x2000, 0xcf8ae8e7);
            ROM_LOAD("j8.bin", 0x4000, 0x2000, 0x84f6fc8c);
            ROM_LOAD("j9.bin", 0x6000, 0x2000, 0x3f9bb09f);

            ROM_REGION(0x0400, REGION_PROMS);
            ROM_LOAD("bprom1.bin", 0x0000, 0x0200, 0x2f0955f2);/* color prom */

            ROM_LOAD("bprom2.bin", 0x0200, 0x0200, 0x2747ca77);/* ??? */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_kickridr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("k1", 0x0000, 0x2000, 0xdfdd1ab4);
            ROM_LOAD("k3", 0x4000, 0x2000, 0x412244da);
            ROM_LOAD("k4", 0x6000, 0x2000, 0xa67dd2ec);
            ROM_LOAD("k2", 0x8000, 0x2000, 0xe193fb5c);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("k10", 0x0000, 0x4000, 0x6843dbc0);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("k5", 0x0000, 0x4000, 0x3f7d7e49);

            ROM_REGION(0x8000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("k6", 0x0000, 0x2000, 0x94252ed3);
            ROM_LOAD("k7", 0x2000, 0x2000, 0x7ef2420e);
            ROM_LOAD("k8", 0x4000, 0x2000, 0x29bed201);
            ROM_LOAD("k9", 0x6000, 0x2000, 0x847584d3);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("kickridr.clr", 0x0000, 0x0100, 0x73ec281c);
            ROM_END();
        }
    };

    public static GameDriver driver_docastle = new GameDriver("1983", "docastle", "docastle.java", rom_docastle, null, machine_driver_docastle, input_ports_docastle, null, ROT270, "Universal", "Mr. Do's Castle (set 1)");
    public static GameDriver driver_docastl2 = new GameDriver("1983", "docastl2", "docastle.java", rom_docastl2, driver_docastle, machine_driver_docastle, input_ports_docastle, null, ROT270, "Universal", "Mr. Do's Castle (set 2)");
    public static GameDriver driver_douni = new GameDriver("1983", "douni", "docastle.java", rom_douni, driver_docastle, machine_driver_docastle, input_ports_docastle, null, ROT270, "Universal", "Mr. Do vs. Unicorns");
    public static GameDriver driver_dorunrun = new GameDriver("1984", "dorunrun", "docastle.java", rom_dorunrun, null, machine_driver_dorunrun, input_ports_dorunrun, null, ROT0, "Universal", "Do! Run Run (set 1)");
    public static GameDriver driver_dorunru2 = new GameDriver("1984", "dorunru2", "docastle.java", rom_dorunru2, driver_dorunrun, machine_driver_dorunrun, input_ports_dorunrun, null, ROT0, "Universal", "Do! Run Run (set 2)");
    public static GameDriver driver_dorunruc = new GameDriver("1984", "dorunruc", "docastle.java", rom_dorunruc, driver_dorunrun, machine_driver_docastle, input_ports_dorunrun, null, ROT0, "Universal", "Do! Run Run (Do's Castle hardware)");
    public static GameDriver driver_spiero = new GameDriver("1987", "spiero", "docastle.java", rom_spiero, driver_dorunrun, machine_driver_dorunrun, input_ports_dorunrun, null, ROT0, "Universal", "Super Pierrot (Japan)");
    public static GameDriver driver_dowild = new GameDriver("1984", "dowild", "docastle.java", rom_dowild, null, machine_driver_dorunrun, input_ports_dowild, null, ROT0, "Universal", "Mr. Do's Wild Ride");
    public static GameDriver driver_jjack = new GameDriver("1984", "jjack", "docastle.java", rom_jjack, null, machine_driver_dorunrun, input_ports_jjack, null, ROT270, "Universal", "Jumping Jack");
    public static GameDriver driver_kickridr = new GameDriver("1984", "kickridr", "docastle.java", rom_kickridr, null, machine_driver_dorunrun, input_ports_kickridr, null, ROT0, "Universal", "Kick Rider");
}
