/*
 * ported to v0.36
 * using automatic conversion tool v0.09
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.adpcmH.*;
import static arcadeflex.v036.sound.adpcm.*;
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.lwings.*;
import static gr.codebb.arcadeflex.v036.machine.lwings.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class lwings {

    public static WriteHandlerPtr trojan_sound_cmd_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            if (data != 0xff && (data & 0x08) != 0) {
                /*
                 I assume that Trojan's ADPCM output is directly derived
                 from the sound code. I can't find an output port that
                 does this on either the sound board or main board.
                 */
                ADPCM_trigger.handler(0, data & 0x07);
            }

        }
    };

    public static WriteHandlerPtr avengers_protection_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
        }
    };
    static int hack;
    public static ReadHandlerPtr avengers_protection_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* the protection reads are used for background palette among other things */

            hack = hack & 0xf;
            return hack++;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM), /* CODE */
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1), /* CODE */
                new MemoryReadAddress(0xc000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf808, 0xf808, input_port_0_r),
                new MemoryReadAddress(0xf809, 0xf809, input_port_1_r),
                new MemoryReadAddress(0xf80a, 0xf80a, input_port_2_r),
                new MemoryReadAddress(0xf80b, 0xf80b, input_port_3_r),
                new MemoryReadAddress(0xf80c, 0xf80c, input_port_4_r),
                new MemoryReadAddress(0xf80d, 0xf80d, avengers_protection_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xddff, MWA_RAM),
                new MemoryWriteAddress(0xde00, 0xdfff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe000, 0xe3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xe400, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xebff, lwings_background_w, lwings_backgroundram, lwings_backgroundram_size),
                new MemoryWriteAddress(0xec00, 0xefff, lwings_backgroundattrib_w, lwings_backgroundattribram),
                new MemoryWriteAddress(0xf000, 0xf3ff, paletteram_RRRRGGGGBBBBxxxx_split2_w, paletteram_2),
                new MemoryWriteAddress(0xf400, 0xf7ff, paletteram_RRRRGGGGBBBBxxxx_split1_w, paletteram),
                new MemoryWriteAddress(0xf800, 0xf801, MWA_RAM, trojan_scrollx),
                new MemoryWriteAddress(0xf802, 0xf803, MWA_RAM, trojan_scrolly),
                new MemoryWriteAddress(0xf804, 0xf804, MWA_RAM, trojan_bk_scrollx),
                new MemoryWriteAddress(0xf805, 0xf805, MWA_RAM, trojan_bk_scrolly),
                new MemoryWriteAddress(0xf808, 0xf809, MWA_RAM, lwings_scrolly),
                new MemoryWriteAddress(0xf80a, 0xf80b, MWA_RAM, lwings_scrollx),
                new MemoryWriteAddress(0xf80c, 0xf80c, soundlatch_w),
                new MemoryWriteAddress(0xf80d, 0xf80d, watchdog_reset_w),
                new MemoryWriteAddress(0xf80e, 0xf80e, lwings_bankswitch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /* TROJAN - intercept sound command write for ADPCM samples */
    static MemoryWriteAddress trojan_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xddff, MWA_RAM),
                new MemoryWriteAddress(0xde00, 0xdf7f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xdf80, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xe400, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xebff, lwings_background_w, lwings_backgroundram, lwings_backgroundram_size),
                new MemoryWriteAddress(0xec00, 0xefff, lwings_backgroundattrib_w, lwings_backgroundattribram),
                new MemoryWriteAddress(0xf000, 0xf3ff, paletteram_RRRRGGGGBBBBxxxx_split2_w, paletteram_2),
                new MemoryWriteAddress(0xf400, 0xf7ff, paletteram_RRRRGGGGBBBBxxxx_split1_w, paletteram),
                new MemoryWriteAddress(0xf800, 0xf801, MWA_RAM, trojan_scrollx),
                new MemoryWriteAddress(0xf802, 0xf803, MWA_RAM, trojan_scrolly),
                new MemoryWriteAddress(0xf804, 0xf804, MWA_RAM, trojan_bk_scrollx),
                new MemoryWriteAddress(0xf805, 0xf805, MWA_RAM, trojan_bk_scrolly),
                new MemoryWriteAddress(0xf809, 0xf809, avengers_protection_w),
                new MemoryWriteAddress(0xf80c, 0xf80c, trojan_sound_cmd_w),
                new MemoryWriteAddress(0xf80d, 0xf80d, watchdog_reset_w),
                new MemoryWriteAddress(0xf80e, 0xf80e, lwings_bankswitch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xc800, soundlatch_r),
                new MemoryReadAddress(0xe006, 0xe006, MRA_RAM), /* Avengers - ADPCM status?? */
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0xe001, 0xe001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xe002, 0xe002, YM2203_control_port_1_w),
                new MemoryWriteAddress(0xe003, 0xe003, YM2203_write_port_1_w),
                new MemoryWriteAddress(0xe006, 0xe006, MWA_RAM), /* Avengers - ADPCM output??? */
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_sectionz = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* DSW0 */

            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x08, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x01, 0x01, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x01, DEF_STR("Yes"));
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x06, "Normal");
            PORT_DIPSETTING(0x04, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x38, "20000 50000");
            PORT_DIPSETTING(0x18, "20000 60000");
            PORT_DIPSETTING(0x28, "20000 70000");
            PORT_DIPSETTING(0x08, "30000 60000");
            PORT_DIPSETTING(0x30, "30000 70000");
            PORT_DIPSETTING(0x10, "30000 80000");
            PORT_DIPSETTING(0x20, "40000 100000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, "Upright One Player");
            PORT_DIPSETTING(0x40, "Upright Two Players");
            /*      PORT_DIPSETTING(    0x80, "???" );      probably unused */
            PORT_DIPSETTING(0xc0, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_lwings = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, "Unknown 1/2");
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x08, "5");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_4C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_3C"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPNAME(0x06, 0x06, "Difficulty?");
            PORT_DIPSETTING(0x02, "Easy?");
            PORT_DIPSETTING(0x06, "Normal?");
            PORT_DIPSETTING(0x04, "Difficult?");
            PORT_DIPSETTING(0x00, "Very Difficult?");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x10, DEF_STR("Yes"));
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xe0, "20000 50000");
            PORT_DIPSETTING(0x60, "20000 60000");
            PORT_DIPSETTING(0xa0, "20000 70000");
            PORT_DIPSETTING(0x20, "30000 60000");
            PORT_DIPSETTING(0xc0, "30000 70000");
            PORT_DIPSETTING(0x40, "30000 80000");
            PORT_DIPSETTING(0x80, "40000 100000");
            PORT_DIPSETTING(0x00, "None");
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_trojan = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, "Upright 1 Player");
            PORT_DIPSETTING(0x02, "Upright 2 Players");
            PORT_DIPSETTING(0x03, DEF_STR("Cocktail"));
            /* 0x01 same as 0x02 or 0x03 */
            PORT_DIPNAME(0x1c, 0x1c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x10, "20000 60000");
            PORT_DIPSETTING(0x0c, "20000 70000");
            PORT_DIPSETTING(0x08, "20000 80000");
            PORT_DIPSETTING(0x1c, "30000 60000");
            PORT_DIPSETTING(0x18, "30000 70000");
            PORT_DIPSETTING(0x14, "30000 80000");
            PORT_DIPSETTING(0x04, "40000 80000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xe0, 0xe0, "Starting Level");
            PORT_DIPSETTING(0xe0, "1");
            PORT_DIPSETTING(0xc0, "2");
            PORT_DIPSETTING(0xa0, "3");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0x60, "5");
            PORT_DIPSETTING(0x40, "6");
            /* 0x00 and 0x20 start at level 6 */

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x40, 0x40, "Flip Screen?");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));
            INPUT_PORTS_END();
        }
    };

    /* Trojan with level selection - starting level dip switches not used */
    static InputPortHandlerPtr input_ports_trojanls = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, "Upright 1 Player");
            PORT_DIPSETTING(0x02, "Upright 2 Players");
            PORT_DIPSETTING(0x03, DEF_STR("Cocktail"));
            /* 0x01 same as 0x02 or 0x03 */
            PORT_DIPNAME(0x1c, 0x1c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x10, "20000 60000");
            PORT_DIPSETTING(0x0c, "20000 70000");
            PORT_DIPSETTING(0x08, "20000 80000");
            PORT_DIPSETTING(0x1c, "30000 60000");
            PORT_DIPSETTING(0x18, "30000 70000");
            PORT_DIPSETTING(0x14, "30000 80000");
            PORT_DIPSETTING(0x04, "40000 80000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x40, 0x40, "Flip Screen?");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 tiles */
            2048, /* 2048 tiles */
            4, /* 4 bits per pixel */
            new int[]{0x30000 * 8, 0x20000 * 8, 0x10000 * 8, 0x00000 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0x10000 * 8 + 4, 0x10000 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 33 * 8 + 0, 33 * 8 + 1, 33 * 8 + 2, 33 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxLayout spritelayout_trojan = new GfxLayout/* LWings, with more sprites */(
                    16, 16, /* 16*16 sprites */
                    2048, /* 2048 sprites */
                    4, /* 4 bits per pixel */
                    new int[]{0x20000 * 8 + 4, 0x20000 * 8 + 0, 4, 0},
                    new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                        32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 33 * 8 + 0, 33 * 8 + 1, 33 * 8 + 2, 33 * 8 + 3},
                    new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                        8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
                    64 * 8 /* every sprite takes 64 consecutive bytes */
            );

    static GfxLayout bktilelayout_trojan = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0x08000 * 8 + 0, 0x08000 * 8 + 4, 0, 4},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3, 33 * 8 + 0, 33 * 8 + 1, 33 * 8 + 2, 33 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 512, 16), /* colors 512-575 */
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 0, 8), /* colors   0-127 */
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 384, 8), /* colors 384-511 */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo gfxdecodeinfo_trojan[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 768, 16), /* colors 768-831 */
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 256, 8), /* colors 256-383 */
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout_trojan, 640, 8), /* colors 640-767 */
                new GfxDecodeInfo(REGION_GFX4, 0, bktilelayout_trojan, 0, 8), /* colors   0-127 */
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz (?) */
            new int[]{YM2203_VOL(10, 20), YM2203_VOL(10, 20)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_lwings = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz (?) */
                        readmem, writemem, null, null,
                        lwings_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3 Mhz (?) */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            lwings_vh_start,
            lwings_vh_stop,
            lwings_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
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
    static RomLoadHandlerPtr rom_lwings = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("6c_lw01.bin", 0x00000, 0x8000, 0xb55a7f60);
            ROM_LOAD("7c_lw02.bin", 0x10000, 0x8000, 0xa5efbb1b);
            ROM_LOAD("9c_lw03.bin", 0x18000, 0x8000, 0xec5cc201);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("11e_lw04.bin", 0x0000, 0x8000, 0xa20337a2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9h_lw05.bin", 0x00000, 0x4000, 0x091d923c);/* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3e_lw14.bin", 0x00000, 0x8000, 0x5436392c);/* tiles */

            ROM_LOAD("1e_lw08.bin", 0x08000, 0x8000, 0xb491bbbb);
            ROM_LOAD("3d_lw13.bin", 0x10000, 0x8000, 0xfdd1908a);
            ROM_LOAD("1d_lw07.bin", 0x18000, 0x8000, 0x5c73d406);
            ROM_LOAD("3b_lw12.bin", 0x20000, 0x8000, 0x32e17b3c);
            ROM_LOAD("1b_lw06.bin", 0x28000, 0x8000, 0x52e533c1);
            ROM_LOAD("3f_lw15.bin", 0x30000, 0x8000, 0x99e134ba);
            ROM_LOAD("1f_lw09.bin", 0x38000, 0x8000, 0xc8f28777);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3j_lw17.bin", 0x00000, 0x8000, 0x5ed1bc9b);
            /* sprites */

            ROM_LOAD("1j_lw11.bin", 0x08000, 0x8000, 0x2a0790d6);
            ROM_LOAD("3h_lw16.bin", 0x10000, 0x8000, 0xe8834006);
            ROM_LOAD("1h_lw10.bin", 0x18000, 0x8000, 0xb693f5a5);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_lwings2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("u13-l", 0x00000, 0x8000, 0x3069c01c);
            ROM_LOAD("u14-k", 0x10000, 0x8000, 0x5d91c828);
            ROM_LOAD("9c_lw03.bin", 0x18000, 0x8000, 0xec5cc201);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("11e_lw04.bin", 0x0000, 0x8000, 0xa20337a2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9h_lw05.bin", 0x00000, 0x4000, 0x091d923c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b_03e.rom", 0x00000, 0x8000, 0x176e3027);
            /* tiles */

            ROM_LOAD("b_01e.rom", 0x08000, 0x8000, 0xf5d25623);
            ROM_LOAD("b_03d.rom", 0x10000, 0x8000, 0x001caa35);
            ROM_LOAD("b_01d.rom", 0x18000, 0x8000, 0x0ba008c3);
            ROM_LOAD("b_03b.rom", 0x20000, 0x8000, 0x4f8182e9);
            ROM_LOAD("b_01b.rom", 0x28000, 0x8000, 0xf1617374);
            ROM_LOAD("b_03f.rom", 0x30000, 0x8000, 0x9b374dcc);
            ROM_LOAD("b_01f.rom", 0x38000, 0x8000, 0x23654e0a);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b_03j.rom", 0x00000, 0x8000, 0x8f3c763a);
            /* sprites */

            ROM_LOAD("b_01j.rom", 0x08000, 0x8000, 0x7cc90a1d);
            ROM_LOAD("b_03h.rom", 0x10000, 0x8000, 0x7d58f532);
            ROM_LOAD("b_01h.rom", 0x18000, 0x8000, 0x3e396eda);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_lwingsjp = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("a_06c.rom", 0x00000, 0x8000, 0x2068a738);
            ROM_LOAD("a_07c.rom", 0x10000, 0x8000, 0xd6a2edc4);
            ROM_LOAD("9c_lw03.bin", 0x18000, 0x8000, 0xec5cc201);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("11e_lw04.bin", 0x0000, 0x8000, 0xa20337a2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9h_lw05.bin", 0x00000, 0x4000, 0x091d923c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b_03e.rom", 0x00000, 0x8000, 0x176e3027);
            /* tiles */

            ROM_LOAD("b_01e.rom", 0x08000, 0x8000, 0xf5d25623);
            ROM_LOAD("b_03d.rom", 0x10000, 0x8000, 0x001caa35);
            ROM_LOAD("b_01d.rom", 0x18000, 0x8000, 0x0ba008c3);
            ROM_LOAD("b_03b.rom", 0x20000, 0x8000, 0x4f8182e9);
            ROM_LOAD("b_01b.rom", 0x28000, 0x8000, 0xf1617374);
            ROM_LOAD("b_03f.rom", 0x30000, 0x8000, 0x9b374dcc);
            ROM_LOAD("b_01f.rom", 0x38000, 0x8000, 0x23654e0a);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b_03j.rom", 0x00000, 0x8000, 0x8f3c763a);
            /* sprites */

            ROM_LOAD("b_01j.rom", 0x08000, 0x8000, 0x7cc90a1d);
            ROM_LOAD("b_03h.rom", 0x10000, 0x8000, 0x7d58f532);
            ROM_LOAD("b_01h.rom", 0x18000, 0x8000, 0x3e396eda);
            ROM_END();
        }
    };

    /**
     * *************************************************************
     *
     * Section Z =========
     *
     * Exactly the same hardware as legendary wings, apart from the graphics
     * orientation.
     *
     **************************************************************
     */
    static RomLoadHandlerPtr rom_sectionz = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("6c_sz01.bin", 0x00000, 0x8000, 0x69585125);
            ROM_LOAD("7c_sz02.bin", 0x10000, 0x8000, 0x22f161b8);
            ROM_LOAD("9c_sz03.bin", 0x18000, 0x8000, 0x4c7111ed);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("11e_sz04.bin", 0x0000, 0x8000, 0xa6073566);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9h_sz05.bin", 0x00000, 0x4000, 0x3173ba2e);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3e_sz14.bin", 0x00000, 0x8000, 0x63782e30);
            /* tiles */

            ROM_LOAD("1e_sz08.bin", 0x08000, 0x8000, 0xd57d9f13);
            ROM_LOAD("3d_sz13.bin", 0x10000, 0x8000, 0x1b3d4d7f);
            ROM_LOAD("1d_sz07.bin", 0x18000, 0x8000, 0xf5b3a29f);
            ROM_LOAD("3b_sz12.bin", 0x20000, 0x8000, 0x11d47dfd);
            ROM_LOAD("1b_sz06.bin", 0x28000, 0x8000, 0xdf703b68);
            ROM_LOAD("3f_sz15.bin", 0x30000, 0x8000, 0x36bb9bf7);
            ROM_LOAD("1f_sz09.bin", 0x38000, 0x8000, 0xda8f06c9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3j_sz17.bin", 0x00000, 0x8000, 0x8df7b24a);
            /* sprites */

            ROM_LOAD("1j_sz11.bin", 0x08000, 0x8000, 0x685d4c54);
            ROM_LOAD("3h_sz16.bin", 0x10000, 0x8000, 0x500ff2bb);
            ROM_LOAD("1h_sz10.bin", 0x18000, 0x8000, 0x00b3d244);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sctionza = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("sz-01a.bin", 0x00000, 0x8000, 0x98df49fd);
            ROM_LOAD("7c_sz02.bin", 0x10000, 0x8000, 0x22f161b8);
            ROM_LOAD("sz-03j.bin", 0x18000, 0x8000, 0x94547abf);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("11e_sz04.bin", 0x0000, 0x8000, 0xa6073566);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("9h_sz05.bin", 0x00000, 0x4000, 0x3173ba2e);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3e_sz14.bin", 0x00000, 0x8000, 0x63782e30);
            /* tiles */

            ROM_LOAD("1e_sz08.bin", 0x08000, 0x8000, 0xd57d9f13);
            ROM_LOAD("3d_sz13.bin", 0x10000, 0x8000, 0x1b3d4d7f);
            ROM_LOAD("1d_sz07.bin", 0x18000, 0x8000, 0xf5b3a29f);
            ROM_LOAD("3b_sz12.bin", 0x20000, 0x8000, 0x11d47dfd);
            ROM_LOAD("1b_sz06.bin", 0x28000, 0x8000, 0xdf703b68);
            ROM_LOAD("3f_sz15.bin", 0x30000, 0x8000, 0x36bb9bf7);
            ROM_LOAD("1f_sz09.bin", 0x38000, 0x8000, 0xda8f06c9);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("3j_sz17.bin", 0x00000, 0x8000, 0x8df7b24a);
            /* sprites */

            ROM_LOAD("1j_sz11.bin", 0x08000, 0x8000, 0x685d4c54);
            ROM_LOAD("3h_sz16.bin", 0x10000, 0x8000, 0x500ff2bb);
            ROM_LOAD("1h_sz10.bin", 0x18000, 0x8000, 0x00b3d244);
            ROM_END();
        }
    };

    /**
     * *************************************************************
     *
     * Trojan ======
     *
     * Similar to Legendary Wings apart from: 1) More sprites 2) 3rd Z80 (ADPCM)
     * 3) Different palette layout 4) Third Background tile layer
     *
     **************************************************************
     */
    /*
     ADPCM is driven by Z80 continuously outputting to a port. The following
     table is lifted from the code.
	
     Sample 5 doesn't play properly.
     */
    static ADPCMsample trojan_samples[]
            = {
                new ADPCMsample(0x00, 0x00a7, (0x0aa9 - 0x00a7) * 2),
                new ADPCMsample(0x01, 0x0aa9, (0x12ab - 0x0aa9) * 2),
                new ADPCMsample(0x02, 0x12ab, (0x17ad - 0x12ab) * 2),
                new ADPCMsample(0x03, 0x17ad, (0x22af - 0x17ad) * 2),
                new ADPCMsample(0x04, 0x22af, (0x2db1 - 0x22af) * 2),
                new ADPCMsample(0x05, 0x2db1, (0x310a - 0x2db1) * 2),
                new ADPCMsample(0x06, 0x310a, (0x3cb3 - 0x310a) * 2)
            };

    public static ADPCM_initPtr trojan_adpcm_init = new ADPCM_initPtr() {
        public void handler(ADPCMinterface i, ADPCMsample[] s, int max) {

            //memcpy(sample_list,trojan_samples,sizeof(trojan_samples));
            System.arraycopy(trojan_samples, 0, s, 0, trojan_samples.length);
        }
    };

    static ADPCMinterface trojan_adpcm_interface = new ADPCMinterface(
            1, /* 1 channel */
            4000, /* 4000Hz playback */
            REGION_CPU3, /* memory region */
            trojan_adpcm_init, /* init function */
            new int[]{255}
    );

    static MachineDriver machine_driver_trojan = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz (?) */
                        readmem, trojan_writemem, null, null,
                        lwings_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3 Mhz (?) */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4
                )
            },
            60, 2500, /* frames per second, vblank duration */
            /* hand tuned to get rid of sprite lag */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo_trojan,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK,
            null,
            trojan_vh_start,
            trojan_vh_stop,
            trojan_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        trojan_adpcm_interface
                )
            }
    );

    static RomLoadHandlerPtr rom_trojan = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("t4", 0x00000, 0x8000, 0xc1bbeb4e);
            ROM_LOAD("t6", 0x10000, 0x8000, 0xd49592ef);
            ROM_LOAD("tb05.bin", 0x18000, 0x8000, 0x9273b264);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("tb02.bin", 0x0000, 0x8000, 0x21154797);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for ADPCM CPU (CPU not emulated) */

            ROM_LOAD("tb01.bin", 0x0000, 0x4000, 0x1c0f91b2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb03.bin", 0x00000, 0x4000, 0x581a2b4c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb13.bin", 0x00000, 0x8000, 0x285a052b);
            /* tiles */

            ROM_LOAD("tb09.bin", 0x08000, 0x8000, 0xaeb693f7);
            ROM_LOAD("tb12.bin", 0x10000, 0x8000, 0xdfb0fe5c);
            ROM_LOAD("tb08.bin", 0x18000, 0x8000, 0xd3a4c9d1);
            ROM_LOAD("tb11.bin", 0x20000, 0x8000, 0x00f0f4fd);
            ROM_LOAD("tb07.bin", 0x28000, 0x8000, 0xdff2ee02);
            ROM_LOAD("tb14.bin", 0x30000, 0x8000, 0x14bfac18);
            ROM_LOAD("tb10.bin", 0x38000, 0x8000, 0x71ba8a6d);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb18.bin", 0x00000, 0x8000, 0x862c4713);
            /* sprites */

            ROM_LOAD("tb16.bin", 0x08000, 0x8000, 0xd86f8cbd);
            ROM_LOAD("tb17.bin", 0x10000, 0x8000, 0x12a73b3f);
            ROM_LOAD("tb15.bin", 0x18000, 0x8000, 0xbb1a2769);
            ROM_LOAD("tb22.bin", 0x20000, 0x8000, 0x39daafd4);
            ROM_LOAD("tb20.bin", 0x28000, 0x8000, 0x94615d2a);
            ROM_LOAD("tb21.bin", 0x30000, 0x8000, 0x66c642bd);
            ROM_LOAD("tb19.bin", 0x38000, 0x8000, 0x81d5ab36);

            ROM_REGION(0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb25.bin", 0x00000, 0x8000, 0x6e38c6fa);
            /* Bk Tiles */

            ROM_LOAD("tb24.bin", 0x08000, 0x8000, 0x14fc6cf2);

            ROM_REGION(0x08000, REGION_GFX5);
            ROM_LOAD("tb23.bin", 0x00000, 0x08000, 0xeda13c0e);
            /* Tile Map */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_trojanr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("tb04.bin", 0x00000, 0x8000, 0x92670f27);
            ROM_LOAD("tb06.bin", 0x10000, 0x8000, 0xa4951173);
            ROM_LOAD("tb05.bin", 0x18000, 0x8000, 0x9273b264);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("tb02.bin", 0x0000, 0x8000, 0x21154797);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for ADPCM CPU (CPU not emulated) */

            ROM_LOAD("tb01.bin", 0x0000, 0x4000, 0x1c0f91b2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb03.bin", 0x00000, 0x4000, 0x581a2b4c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb13.bin", 0x00000, 0x8000, 0x285a052b);
            /* tiles */

            ROM_LOAD("tb09.bin", 0x08000, 0x8000, 0xaeb693f7);
            ROM_LOAD("tb12.bin", 0x10000, 0x8000, 0xdfb0fe5c);
            ROM_LOAD("tb08.bin", 0x18000, 0x8000, 0xd3a4c9d1);
            ROM_LOAD("tb11.bin", 0x20000, 0x8000, 0x00f0f4fd);
            ROM_LOAD("tb07.bin", 0x28000, 0x8000, 0xdff2ee02);
            ROM_LOAD("tb14.bin", 0x30000, 0x8000, 0x14bfac18);
            ROM_LOAD("tb10.bin", 0x38000, 0x8000, 0x71ba8a6d);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb18.bin", 0x00000, 0x8000, 0x862c4713);
            /* sprites */

            ROM_LOAD("tb16.bin", 0x08000, 0x8000, 0xd86f8cbd);
            ROM_LOAD("tb17.bin", 0x10000, 0x8000, 0x12a73b3f);
            ROM_LOAD("tb15.bin", 0x18000, 0x8000, 0xbb1a2769);
            ROM_LOAD("tb22.bin", 0x20000, 0x8000, 0x39daafd4);
            ROM_LOAD("tb20.bin", 0x28000, 0x8000, 0x94615d2a);
            ROM_LOAD("tb21.bin", 0x30000, 0x8000, 0x66c642bd);
            ROM_LOAD("tb19.bin", 0x38000, 0x8000, 0x81d5ab36);

            ROM_REGION(0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb25.bin", 0x00000, 0x8000, 0x6e38c6fa);
            /* Bk Tiles */

            ROM_LOAD("tb24.bin", 0x08000, 0x8000, 0x14fc6cf2);

            ROM_REGION(0x08000, REGION_GFX5);
            ROM_LOAD("tb23.bin", 0x00000, 0x08000, 0xeda13c0e);
            /* Tile Map */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_trojanj = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("troj-04.rom", 0x00000, 0x8000, 0x0b5a7f49);
            ROM_LOAD("troj-06.rom", 0x10000, 0x8000, 0xdee6ed92);
            ROM_LOAD("tb05.bin", 0x18000, 0x8000, 0x9273b264);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("tb02.bin", 0x0000, 0x8000, 0x21154797);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for ADPCM CPU (CPU not emulated) */

            ROM_LOAD("tb01.bin", 0x0000, 0x4000, 0x1c0f91b2);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb03.bin", 0x00000, 0x4000, 0x581a2b4c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb13.bin", 0x00000, 0x8000, 0x285a052b);
            /* tiles */

            ROM_LOAD("tb09.bin", 0x08000, 0x8000, 0xaeb693f7);
            ROM_LOAD("tb12.bin", 0x10000, 0x8000, 0xdfb0fe5c);
            ROM_LOAD("tb08.bin", 0x18000, 0x8000, 0xd3a4c9d1);
            ROM_LOAD("tb11.bin", 0x20000, 0x8000, 0x00f0f4fd);
            ROM_LOAD("tb07.bin", 0x28000, 0x8000, 0xdff2ee02);
            ROM_LOAD("tb14.bin", 0x30000, 0x8000, 0x14bfac18);
            ROM_LOAD("tb10.bin", 0x38000, 0x8000, 0x71ba8a6d);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb18.bin", 0x00000, 0x8000, 0x862c4713);
            /* sprites */

            ROM_LOAD("tb16.bin", 0x08000, 0x8000, 0xd86f8cbd);
            ROM_LOAD("tb17.bin", 0x10000, 0x8000, 0x12a73b3f);
            ROM_LOAD("tb15.bin", 0x18000, 0x8000, 0xbb1a2769);
            ROM_LOAD("tb22.bin", 0x20000, 0x8000, 0x39daafd4);
            ROM_LOAD("tb20.bin", 0x28000, 0x8000, 0x94615d2a);
            ROM_LOAD("tb21.bin", 0x30000, 0x8000, 0x66c642bd);
            ROM_LOAD("tb19.bin", 0x38000, 0x8000, 0x81d5ab36);

            ROM_REGION(0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tb25.bin", 0x00000, 0x8000, 0x6e38c6fa);
            /* Bk Tiles */

            ROM_LOAD("tb24.bin", 0x08000, 0x8000, 0x14fc6cf2);

            ROM_REGION(0x08000, REGION_GFX5);
            ROM_LOAD("tb23.bin", 0x00000, 0x08000, 0xeda13c0e);
            /* Tile Map */

            ROM_END();
        }
    };

    /**
     * *************************************************************************
     *
     * Avengers - Doesn't work properly due to copy protection
     *
     **************************************************************************
     */
    /*
     E2 00 E4 03 E6 0C E8 10 EA 19 EC 25 EE 38 F0 3B F2 3E F4 49 F4 s
     */
    static ADPCMsample avengers_samples[]
            = {
                new ADPCMsample(0x00, 0x00e2, (0x03e4 - 0x00e2) * 2),
                new ADPCMsample(0x01, 0x03e4, (0x0ce6 - 0x03e4) * 2),
                new ADPCMsample(0x02, 0x0ce6, (0x10e8 - 0x0ce6) * 2),
                new ADPCMsample(0x03, 0x10e8, (0x19ea - 0x10e8) * 2),
                new ADPCMsample(0x04, 0x19ea, (0x25ec - 0x19ea) * 2),
                new ADPCMsample(0x05, 0x25ec, (0x38ee - 0x25ec) * 2),
                new ADPCMsample(0x06, 0x38ee, (0x3bf0 - 0x38ee) * 2),
                new ADPCMsample(0x07, 0x3bf0, (0x3ef2 - 0x3bf0) * 2),
                new ADPCMsample(0x08, 0x3ef2, (0x49f4 - 0x3ef2) * 2)
            };

    public static ADPCM_initPtr avengers_adpcm_init = new ADPCM_initPtr() {
        public void handler(ADPCMinterface i, ADPCMsample[] s, int max) {

            //memcpy(sample_list,avengers_samples,sizeof(avengers_samples));
            System.arraycopy(avengers_samples, 0, s, 0, avengers_samples.length);
        }
    };

    static ADPCMinterface avengers_adpcm_interface = new ADPCMinterface(1, /* 1 channel */
            4000, /* 4000Hz playback */
            REGION_CPU3, /* memory region */
            avengers_adpcm_init, /* init function */
            new int[]{255}
    );

    /*
     machine driver is exactly the same as trojan apart from
     a new custom interrupt handler and slightly different videohardware
     */
    static MachineDriver machine_driver_avengers = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 Mhz (?) */
                        readmem, trojan_writemem, null, null,
                        avengers_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3 Mhz (?) */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4
                )
            },
            60, 2500, /* frames per second, vblank duration */
            /* hand tuned to get rid of sprite lag */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo_trojan,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_AFTER_VBLANK,
            null,
            avengers_vh_start,
            trojan_vh_stop,
            trojan_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_ADPCM,
                        avengers_adpcm_interface
                )
            }
    );

    static InputPortHandlerPtr input_ports_avengers = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* probably unused */

            PORT_START();
            /* DSWB */

            PORT_DIPNAME(0x01, 0x01, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x01, DEF_STR("Yes"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x0c, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x30, 0x30, "Bonus");
            PORT_DIPSETTING(0x30, "20k 60k");
            PORT_DIPSETTING(0x10, "20k 70k");
            PORT_DIPSETTING(0x20, "20k 80k");
            PORT_DIPSETTING(0x00, "30k 80k");
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0x80, "5");
            PORT_DIPSETTING(0x00, "6");

            PORT_START();
            /* DSWA */

            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x1c, 0x1c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x1c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x14, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_6C"));
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x60, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));
            INPUT_PORTS_END();
        }
    };

    static RomLoadHandlerPtr rom_avengers = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("04.10n", 0x00000, 0x8000, 0xa94aadcc);
            ROM_LOAD("06.13n", 0x10000, 0x8000, 0x39cd80bd);
            ROM_LOAD("05.12n", 0x18000, 0x8000, 0x06b1cec9);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("02.15h", 0x0000, 0x8000, 0x107a2e17);/* ?? */

            ROM_REGION(0x10000, REGION_CPU3);
            /* ADPCM CPU (not emulated) */

            ROM_LOAD("01.6d", 0x0000, 0x8000, 0xc1e5d258);/* adpcm player - "Talker" ROM */

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("03.8k", 0x00000, 0x4000, 0x4a297a5c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("13.6b", 0x00000, 0x8000, 0x9b5ff305);/* plane 1 */

            ROM_LOAD("09.6a", 0x08000, 0x8000, 0x08323355);
            ROM_LOAD("12.4b", 0x10000, 0x8000, 0x6d5261ba);/* plane 2 */

            ROM_LOAD("08.4a", 0x18000, 0x8000, 0xa13d9f54);
            ROM_LOAD("11.3b", 0x20000, 0x8000, 0xa2911d8b);/* plane 3 */

            ROM_LOAD("07.3a", 0x28000, 0x8000, 0xcde78d32);
            ROM_LOAD("14.8b", 0x30000, 0x8000, 0x44ac2671);/* plane 4 */

            ROM_LOAD("10.8a", 0x38000, 0x8000, 0xb1a717cb);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("18.7l", 0x00000, 0x8000, 0x3c876a17);/* planes 0,1 */

            ROM_LOAD("16.3l", 0x08000, 0x8000, 0x4b1ff3ac);
            ROM_LOAD("17.5l", 0x10000, 0x8000, 0x4eb543ef);
            ROM_LOAD("15.2l", 0x18000, 0x8000, 0x8041de7f);
            ROM_LOAD("22.7n", 0x20000, 0x8000, 0xbdaa8b22);/* planes 2,3 */

            ROM_LOAD("20.3n", 0x28000, 0x8000, 0x566e3059);
            ROM_LOAD("21.5n", 0x30000, 0x8000, 0x301059aa);
            ROM_LOAD("19.2n", 0x38000, 0x8000, 0xa00485ec);

            ROM_REGION(0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* bg tiles */

            ROM_LOAD("25.15n", 0x00000, 0x8000, 0x230d9e30);/* planes 0,1 */

            ROM_LOAD("24.13n", 0x08000, 0x8000, 0xa6354024);/* planes 2,3 */

            ROM_REGION(0x08000, REGION_GFX5);
            ROM_LOAD("23.9n", 0x0000, 0x08000, 0xc0a93ef6);
            /* Tile Map */

            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_avenger2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);
            /* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("avg4.bin", 0x00000, 0x8000, 0x0fea7ac5);
            ROM_LOAD("avg6.bin", 0x10000, 0x8000, 0x491a712c);
            ROM_LOAD("avg5.bin", 0x18000, 0x8000, 0x9a214b42);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */

            ROM_LOAD("02.15h", 0x0000, 0x8000, 0x107a2e17);/* MISSING from this set */

            ROM_REGION(0x10000, REGION_CPU3);
            /* ADPCM CPU (not emulated) */

            ROM_LOAD("01.6d", 0x0000, 0x8000, 0xc1e5d258);/* adpcm player - "Talker" ROM */

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("03.8k", 0x00000, 0x4000, 0x4a297a5c);
            /* characters */

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("13.6b", 0x00000, 0x8000, 0x9b5ff305);/* plane 1 */

            ROM_LOAD("09.6a", 0x08000, 0x8000, 0x08323355);
            ROM_LOAD("12.4b", 0x10000, 0x8000, 0x6d5261ba);/* plane 2 */

            ROM_LOAD("08.4a", 0x18000, 0x8000, 0xa13d9f54);
            ROM_LOAD("11.3b", 0x20000, 0x8000, 0xa2911d8b);/* plane 3 */

            ROM_LOAD("07.3a", 0x28000, 0x8000, 0xcde78d32);
            ROM_LOAD("14.8b", 0x30000, 0x8000, 0x44ac2671);/* plane 4 */

            ROM_LOAD("10.8a", 0x38000, 0x8000, 0xb1a717cb);

            ROM_REGION(0x40000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("18.7l", 0x00000, 0x8000, 0x3c876a17);/* planes 0,1 */

            ROM_LOAD("16.3l", 0x08000, 0x8000, 0x4b1ff3ac);
            ROM_LOAD("17.5l", 0x10000, 0x8000, 0x4eb543ef);
            ROM_LOAD("15.2l", 0x18000, 0x8000, 0x8041de7f);
            ROM_LOAD("22.7n", 0x20000, 0x8000, 0xbdaa8b22);/* planes 2,3 */

            ROM_LOAD("20.3n", 0x28000, 0x8000, 0x566e3059);
            ROM_LOAD("21.5n", 0x30000, 0x8000, 0x301059aa);
            ROM_LOAD("19.2n", 0x38000, 0x8000, 0xa00485ec);

            ROM_REGION(0x10000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("25.15n", 0x00000, 0x8000, 0x230d9e30);/* planes 0,1 */

            ROM_LOAD("24.13n", 0x08000, 0x8000, 0xa6354024);/* planes 2,3 */

            ROM_REGION(0x08000, REGION_GFX5);
            ROM_LOAD("23.9n", 0x0000, 0x08000, 0xc0a93ef6);
            /* Tile Map */

            ROM_END();
        }
    };

    public static GameDriver driver_lwings = new GameDriver("1986", "lwings", "lwings.java", rom_lwings, null, machine_driver_lwings, input_ports_lwings, null, ROT270, "Capcom", "Legendary Wings (US set 1)");
    public static GameDriver driver_lwings2 = new GameDriver("1986", "lwings2", "lwings.java", rom_lwings2, driver_lwings, machine_driver_lwings, input_ports_lwings, null, ROT270, "Capcom", "Legendary Wings (US set 2)");
    public static GameDriver driver_lwingsjp = new GameDriver("1986", "lwingsjp", "lwings.java", rom_lwingsjp, driver_lwings, machine_driver_lwings, input_ports_lwings, null, ROT270, "Capcom", "Ales no Tsubasa (Japan)");
    public static GameDriver driver_sectionz = new GameDriver("1985", "sectionz", "lwings.java", rom_sectionz, null, machine_driver_lwings, input_ports_sectionz, null, ROT0, "Capcom", "Section Z (set 1)");
    public static GameDriver driver_sctionza = new GameDriver("1985", "sctionza", "lwings.java", rom_sctionza, driver_sectionz, machine_driver_lwings, input_ports_sectionz, null, ROT0, "Capcom", "Section Z (set 2)");
    public static GameDriver driver_trojan = new GameDriver("1986", "trojan", "lwings.java", rom_trojan, null, machine_driver_trojan, input_ports_trojanls, null, ROT0, "Capcom", "Trojan (US)");
    public static GameDriver driver_trojanr = new GameDriver("1986", "trojanr", "lwings.java", rom_trojanr, driver_trojan, machine_driver_trojan, input_ports_trojan, null, ROT0, "Capcom (Romstar license)", "Trojan (Romstar)");
    public static GameDriver driver_trojanj = new GameDriver("1986", "trojanj", "lwings.java", rom_trojanj, driver_trojan, machine_driver_trojan, input_ports_trojan, null, ROT0, "Capcom", "Tatakai no Banka (Japan)");
    public static GameDriver driver_avengers = new GameDriver("1987", "avengers", "lwings.java", rom_avengers, null, machine_driver_avengers, input_ports_avengers, null, ROT270, "Capcom", "Avengers (set 1)", GAME_NOT_WORKING | GAME_WRONG_COLORS);
    public static GameDriver driver_avenger2 = new GameDriver("1987", "avenger2", "lwings.java", rom_avenger2, driver_avengers, machine_driver_avengers, input_ports_avengers, null, ROT270, "Capcom", "Avengers (set 2)", GAME_NOT_WORKING | GAME_WRONG_COLORS);
}
