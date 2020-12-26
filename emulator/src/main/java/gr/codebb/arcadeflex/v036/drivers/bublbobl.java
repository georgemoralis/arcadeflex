/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.bublbobl.*;
import static gr.codebb.arcadeflex.v036.machine.bublbobl.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrfH.*;

public class bublbobl {

    static MemoryReadAddress bublbobl_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xf7ff, bublbobl_sharedram1_r),
                new MemoryReadAddress(0xf800, 0xf9ff, paletteram_r),
                new MemoryReadAddress(0xfc00, 0xfcff, bublbobl_sharedram2_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bublbobl_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xdcff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0xdd00, 0xdfff, MWA_RAM, bublbobl_objectram, bublbobl_objectram_size),
                new MemoryWriteAddress(0xe000, 0xf7ff, bublbobl_sharedram1_w, bublbobl_sharedram1),
                new MemoryWriteAddress(0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xfa00, 0xfa00, bublbobl_sound_command_w),
                new MemoryWriteAddress(0xfa80, 0xfa80, watchdog_reset_w), /* not sure - could go to the 68705 */
                new MemoryWriteAddress(0xfb40, 0xfb40, bublbobl_bankswitch_w),
                new MemoryWriteAddress(0xfc00, 0xfcff, bublbobl_sharedram2_w, bublbobl_sharedram2),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress m68705_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0000, bublbobl_68705_portA_r),
                new MemoryReadAddress(0x0001, 0x0001, bublbobl_68705_portB_r),
                new MemoryReadAddress(0x0002, 0x0002, input_port_0_r), /* COIN */
                new MemoryReadAddress(0x0010, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x0080, 0x07ff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress m68705_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0000, bublbobl_68705_portA_w),
                new MemoryWriteAddress(0x0001, 0x0001, bublbobl_68705_portB_w),
                new MemoryWriteAddress(0x0004, 0x0004, bublbobl_68705_ddrA_w),
                new MemoryWriteAddress(0x0005, 0x0005, bublbobl_68705_ddrB_w),
                new MemoryWriteAddress(0x0010, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x0080, 0x07ff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress boblbobl_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xf7ff, bublbobl_sharedram1_r),
                new MemoryReadAddress(0xf800, 0xf9ff, paletteram_r),
                new MemoryReadAddress(0xfc00, 0xfcff, bublbobl_sharedram2_r),
                new MemoryReadAddress(0xff00, 0xff00, input_port_0_r),
                new MemoryReadAddress(0xff01, 0xff01, input_port_1_r),
                new MemoryReadAddress(0xff02, 0xff02, input_port_2_r),
                new MemoryReadAddress(0xff03, 0xff03, input_port_3_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress boblbobl_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xdcff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0xdd00, 0xdfff, MWA_RAM, bublbobl_objectram, bublbobl_objectram_size),
                new MemoryWriteAddress(0xe000, 0xf7ff, bublbobl_sharedram1_w, bublbobl_sharedram1),
                new MemoryWriteAddress(0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xfa00, 0xfa00, bublbobl_sound_command_w),
                new MemoryWriteAddress(0xfa80, 0xfa80, MWA_NOP),
                new MemoryWriteAddress(0xfb40, 0xfb40, bublbobl_bankswitch_w),
                new MemoryWriteAddress(0xfc00, 0xfcff, bublbobl_sharedram2_w, bublbobl_sharedram2),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress bublbobl_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xe000, 0xf7ff, bublbobl_sharedram1_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bublbobl_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xe000, 0xf7ff, bublbobl_sharedram1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x9000, YM2203_status_port_0_r),
                new MemoryReadAddress(0x9001, 0x9001, YM2203_read_port_0_r),
                new MemoryReadAddress(0xa000, 0xa000, YM3526_status_port_0_r),
                new MemoryReadAddress(0xb000, 0xb000, soundlatch_r),
                new MemoryReadAddress(0xb001, 0xb001, MRA_NOP), /* ??? */
                new MemoryReadAddress(0xe000, 0xefff, MRA_ROM), /* space for diagnostic ROM? */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x8fff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0x9001, 0x9001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xa000, 0xa000, YM3526_control_port_0_w),
                new MemoryWriteAddress(0xa001, 0xa001, YM3526_write_port_0_w),
                new MemoryWriteAddress(0xb000, 0xb000, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xb001, 0xb001, bublbobl_sh_nmi_enable_w),
                new MemoryWriteAddress(0xb002, 0xb002, bublbobl_sh_nmi_disable_w),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_ROM), /* space for diagnostic ROM? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress tokio_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xf7ff, bublbobl_sharedram1_r),
                new MemoryReadAddress(0xf800, 0xf9ff, paletteram_r),
                new MemoryReadAddress(0xfa03, 0xfa03, input_port_0_r),
                new MemoryReadAddress(0xfa04, 0xfa04, input_port_1_r),
                new MemoryReadAddress(0xfa05, 0xfa05, input_port_2_r),
                new MemoryReadAddress(0xfa06, 0xfa06, input_port_3_r),
                new MemoryReadAddress(0xfa07, 0xfa07, input_port_4_r),
                new MemoryReadAddress(0xfe00, 0xfe00, tokio_fake_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress tokio_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xdcff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0xdd00, 0xdfff, MWA_RAM, bublbobl_objectram, bublbobl_objectram_size),
                new MemoryWriteAddress(0xe000, 0xf7ff, bublbobl_sharedram1_w, bublbobl_sharedram1),
                new MemoryWriteAddress(0xf800, 0xf9ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xfa00, 0xfa00, MWA_NOP),
                new MemoryWriteAddress(0xfa80, 0xfa80, tokio_bankswitch_w),
                new MemoryWriteAddress(0xfb00, 0xfb00, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0xfb80, 0xfb80, tokio_nmitrigger_w),
                new MemoryWriteAddress(0xfc00, 0xfc00, bublbobl_sound_command_w),
                new MemoryWriteAddress(0xfe00, 0xfe00, MWA_NOP), /* ??? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress tokio_readmem2[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x97ff, bublbobl_sharedram1_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress tokio_writemem2[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x97ff, bublbobl_sharedram1_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress tokio_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x9000, soundlatch_r),
                //	new MemoryReadAddress( 0x9800, 0x9800, MRA_NOP ),	/* ??? */
                new MemoryReadAddress(0xb000, 0xb000, YM2203_status_port_0_r),
                new MemoryReadAddress(0xb001, 0xb001, YM2203_read_port_0_r),
                new MemoryReadAddress(0xe000, 0xefff, MRA_ROM), /* space for diagnostic ROM? */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress tokio_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x8fff, MWA_RAM),
                //	new MemoryWriteAddress( 0x9000, 0x9000, MWA_NOP ),	/* ??? */
                new MemoryWriteAddress(0xa000, 0xa000, bublbobl_sh_nmi_disable_w),
                new MemoryWriteAddress(0xa800, 0xa800, bublbobl_sh_nmi_enable_w),
                new MemoryWriteAddress(0xb000, 0xb000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0xb001, 0xb001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_ROM), /* space for diagnostic ROM? */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_bublbobl = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, "Language");
            PORT_DIPSETTING(0x01, "Japanese");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "20000 80000");
            PORT_DIPSETTING(0x0c, "30000 100000");
            PORT_DIPSETTING(0x04, "40000 200000");
            PORT_DIPSETTING(0x00, "50000 250000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x10, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "5");
            PORT_DIPNAME(0xc0, 0xc0, "Spare");
            PORT_DIPSETTING(0x00, "A");
            PORT_DIPSETTING(0x40, "B");
            PORT_DIPSETTING(0x80, "C");
            PORT_DIPSETTING(0xc0, "D");

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_boblbobl = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "Japanese");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "20000 80000");
            PORT_DIPSETTING(0x0c, "30000 100000");
            PORT_DIPSETTING(0x04, "40000 200000");
            PORT_DIPSETTING(0x00, "50000 250000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x10, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "5");
            PORT_DIPNAME(0xc0, 0x00, "Monster Speed");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x80, "High");
            PORT_DIPSETTING(0xc0, "Very High");

            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);/* ?????*/

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_sboblbob = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, "Game");
            PORT_DIPSETTING(0x01, "Bobble Bobble");
            PORT_DIPSETTING(0x00, "Super Bobble Bobble");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "20000 80000");
            PORT_DIPSETTING(0x0c, "30000 100000");
            PORT_DIPSETTING(0x04, "40000 200000");
            PORT_DIPSETTING(0x00, "50000 250000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x10, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_BITX(0, 0x20, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "100", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, "Monster Speed");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x80, "High");
            PORT_DIPSETTING(0xc0, "Very High");

            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_TILT);/* ?????*/

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_tokio = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* DSW0 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, "Demo Sounds?");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x02, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x08, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0C, "100000 400000");
            PORT_DIPSETTING(0x08, "200000 400000");
            PORT_DIPSETTING(0x04, "300000 400000");
            PORT_DIPSETTING(0x00, "400000 400000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "99", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x80, "Japanese");

            PORT_START();       /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN3);/* service */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();       /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* the characters are 8x8 pixels */
            256 * 8 * 8, /* 256 chars per bank * 8 banks per ROM pair * 8 ROM pairs */
            4, /* 4 bits per pixel */
            new int[]{0, 4, 8 * 0x8000 * 8, 8 * 0x8000 * 8 + 4},
            new int[]{3, 2, 1, 0, 8 + 3, 8 + 2, 8 + 1, 8 + 0},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 bytes in two ROMs */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                /* read all graphics into one big graphics region */
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the 2203 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_irq_line(2, 0, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 1 chip */
            3000000, /* 3 MHz ??? (hand tuned) */
            new int[]{YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static YM3526interface ym3526_interface = new YM3526interface(
            1, /* 1 chip (no more supported) */
            3000000, /* 3 MHz ??? (hand tuned) */
            new int[]{255} /* (not supported) */
    );

    static YM2203interface tokio_ym2203_interface = new YM2203interface(
            1, /* 1 chip */
            3000000, /* 3 MHz ??? */
            new int[]{YM2203_VOL(100, 20)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static MachineDriver machine_driver_bublbobl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6 Mhz??? */
                        bublbobl_readmem, bublbobl_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the 68705 */
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6 Mhz??? */
                        bublbobl_readmem2, bublbobl_writemem2, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 Mhz ??? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* NMIs are triggered by the main CPU */
                /* IRQs are triggered by the YM2203 */
                ),
                new MachineCPU(
                        CPU_M68705,
                        4000000 / 2, /* xtal is 4MHz, I think it's divided by 2 internally */
                        m68705_readmem, m68705_writemem, null, null,
                        bublbobl_m68705_interrupt, 2 /* ??? should come from the same */
                /* clock which latches the INT pin on the second Z80 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            100, /* 100 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            null, /* init_machine() */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            bublbobl_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_vh_start,
            generic_vh_stop,
            bublbobl_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                )
            }
    );

    static MachineDriver machine_driver_boblbobl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6 Mhz??? */
                        boblbobl_readmem, boblbobl_writemem, null, null,
                        interrupt, 1 /* interrupt mode 1, unlike Bubble Bobble */
                ),
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6 Mhz??? */
                        bublbobl_readmem2, bublbobl_writemem2, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 Mhz ??? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* NMIs are triggered by the main CPU */
                /* IRQs are triggered by the YM2203 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            100, /* 100 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            null, /* init_machine() */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            bublbobl_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_vh_start,
            generic_vh_stop,
            bublbobl_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_YM3526,
                        ym3526_interface
                )
            }
    );

    static MachineDriver machine_driver_tokio = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{ /* MachineCPU */
                new MachineCPU( /* Main CPU */
                        CPU_Z80,
                        4000000, /* 4 Mhz??? */
                        tokio_readmem, tokio_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU( /* Video CPU */
                        CPU_Z80,
                        4000000, /* 4 Mhz??? */
                        tokio_readmem2, tokio_writemem2, null, null,
                        interrupt, 1
                ),
                new MachineCPU( /* Audio CPU */
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 Mhz ??? */
                        tokio_sound_readmem, tokio_sound_writemem, null, null,
                        ignore_interrupt, 0
                /* NMIs are triggered by the main CPU */
                /* IRQs are triggered by the YM2203 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames/second, vblank duration */
            100, /* 100 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            null, /* init_machine() */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            bublbobl_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            generic_vh_start,
            generic_vh_stop,
            bublbobl_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        tokio_ym2203_interface
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
    static RomLoadPtr rom_bublbobl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k+64k for the first CPU */

            ROM_LOAD("a78_06.bin", 0x00000, 0x8000, 0x32c8305b);
            ROM_LOAD("a78_05.bin", 0x08000, 0x4000, 0x53f4bc6e);/* banked at 8000-bfff. I must load */

            ROM_CONTINUE(0x10000, 0xc000);			/* bank 0 at 8000 because the code falls into */
            /* it from 7fff, so bank switching wouldn't work */

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a78_09.bin", 0x00000, 0x8000, 0x20358c22);   /* 1st plane */

            ROM_LOAD("a78_10.bin", 0x08000, 0x8000, 0x930168a9);
            ROM_LOAD("a78_11.bin", 0x10000, 0x8000, 0x9773e512);
            ROM_LOAD("a78_12.bin", 0x18000, 0x8000, 0xd045549b);
            ROM_LOAD("a78_13.bin", 0x20000, 0x8000, 0xd0af35c5);
            ROM_LOAD("a78_14.bin", 0x28000, 0x8000, 0x7b5369a8);
            /* 0x30000-0x3ffff empty */
            ROM_LOAD("a78_15.bin", 0x40000, 0x8000, 0x6b61a413);   /* 2nd plane */

            ROM_LOAD("a78_16.bin", 0x48000, 0x8000, 0xb5492d97);
            ROM_LOAD("a78_17.bin", 0x50000, 0x8000, 0xd69762d5);
            ROM_LOAD("a78_18.bin", 0x58000, 0x8000, 0x9f243b68);
            ROM_LOAD("a78_19.bin", 0x60000, 0x8000, 0x66e9438c);
            ROM_LOAD("a78_20.bin", 0x68000, 0x8000, 0x9ef863ad);
            /* 0x70000-0x7ffff empty */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a78_08.bin", 0x0000, 0x08000, 0xae11a07b);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU */

            ROM_LOAD("a78_07.bin", 0x0000, 0x08000, 0x4f9a26e8);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x78caa635);/* from a pirate board */

            ROM_END();
        }
    };

    static RomLoadPtr rom_bublbobr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k+64k for the first CPU */

            ROM_LOAD("25.cpu", 0x00000, 0x8000, 0x2d901c9d);
            ROM_LOAD("24.cpu", 0x08000, 0x4000, 0xb7afedc4);/* banked at 8000-bfff. I must load */

            ROM_CONTINUE(0x10000, 0xc000);			/* bank 0 at 8000 because the code falls into */
            /* it from 7fff, so bank switching wouldn't work */

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a78_09.bin", 0x00000, 0x8000, 0x20358c22);   /* 1st plane */

            ROM_LOAD("a78_10.bin", 0x08000, 0x8000, 0x930168a9);
            ROM_LOAD("a78_11.bin", 0x10000, 0x8000, 0x9773e512);
            ROM_LOAD("a78_12.bin", 0x18000, 0x8000, 0xd045549b);
            ROM_LOAD("a78_13.bin", 0x20000, 0x8000, 0xd0af35c5);
            ROM_LOAD("a78_14.bin", 0x28000, 0x8000, 0x7b5369a8);
            /* 0x30000-0x3ffff empty */
            ROM_LOAD("a78_15.bin", 0x40000, 0x8000, 0x6b61a413);   /* 2nd plane */

            ROM_LOAD("a78_16.bin", 0x48000, 0x8000, 0xb5492d97);
            ROM_LOAD("a78_17.bin", 0x50000, 0x8000, 0xd69762d5);
            ROM_LOAD("a78_18.bin", 0x58000, 0x8000, 0x9f243b68);
            ROM_LOAD("a78_19.bin", 0x60000, 0x8000, 0x66e9438c);
            ROM_LOAD("a78_20.bin", 0x68000, 0x8000, 0x9ef863ad);
            /* 0x70000-0x7ffff empty */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a78_08.bin", 0x0000, 0x08000, 0xae11a07b);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU */

            ROM_LOAD("a78_07.bin", 0x0000, 0x08000, 0x4f9a26e8);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x78caa635);/* from a pirate board */

            ROM_END();
        }
    };

    static RomLoadPtr rom_bubbobr1 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k+64k for the first CPU */

            ROM_LOAD("a78_06.bin", 0x00000, 0x8000, 0x32c8305b);
            ROM_LOAD("a78_21.bin", 0x08000, 0x4000, 0x2844033d);/* banked at 8000-bfff. I must load */

            ROM_CONTINUE(0x10000, 0xc000);			/* bank 0 at 8000 because the code falls into */
            /* it from 7fff, so bank switching wouldn't work */

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a78_09.bin", 0x00000, 0x8000, 0x20358c22);   /* 1st plane */

            ROM_LOAD("a78_10.bin", 0x08000, 0x8000, 0x930168a9);
            ROM_LOAD("a78_11.bin", 0x10000, 0x8000, 0x9773e512);
            ROM_LOAD("a78_12.bin", 0x18000, 0x8000, 0xd045549b);
            ROM_LOAD("a78_13.bin", 0x20000, 0x8000, 0xd0af35c5);
            ROM_LOAD("a78_14.bin", 0x28000, 0x8000, 0x7b5369a8);
            /* 0x30000-0x3ffff empty */
            ROM_LOAD("a78_15.bin", 0x40000, 0x8000, 0x6b61a413);   /* 2nd plane */

            ROM_LOAD("a78_16.bin", 0x48000, 0x8000, 0xb5492d97);
            ROM_LOAD("a78_17.bin", 0x50000, 0x8000, 0xd69762d5);
            ROM_LOAD("a78_18.bin", 0x58000, 0x8000, 0x9f243b68);
            ROM_LOAD("a78_19.bin", 0x60000, 0x8000, 0x66e9438c);
            ROM_LOAD("a78_20.bin", 0x68000, 0x8000, 0x9ef863ad);
            /* 0x70000-0x7ffff empty */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a78_08.bin", 0x0000, 0x08000, 0xae11a07b);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU */

            ROM_LOAD("a78_07.bin", 0x0000, 0x08000, 0x4f9a26e8);

            ROM_REGION(0x0800, REGION_CPU4);/* 2k for the microcontroller */

            ROM_LOAD("68705.bin", 0x0000, 0x0800, 0x78caa635);/* from a pirate board */

            ROM_END();
        }
    };

    static RomLoadPtr rom_boblbobl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k+64k for the first CPU */

            ROM_LOAD("bb3", 0x00000, 0x8000, 0x01f81936);
            ROM_LOAD("bb5", 0x08000, 0x4000, 0x13118eb1);/* banked at 8000-bfff. I must load */

            ROM_CONTINUE(0x10000, 0x4000);			/* bank 0 at 8000 because the code falls into */
            /* it from 7fff, so bank switching wouldn't work */

            ROM_LOAD("bb4", 0x14000, 0x8000, 0xafda99d8);/* banked at 8000-bfff */

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a78_09.bin", 0x00000, 0x8000, 0x20358c22);   /* 1st plane */

            ROM_LOAD("a78_10.bin", 0x08000, 0x8000, 0x930168a9);
            ROM_LOAD("a78_11.bin", 0x10000, 0x8000, 0x9773e512);
            ROM_LOAD("a78_12.bin", 0x18000, 0x8000, 0xd045549b);
            ROM_LOAD("a78_13.bin", 0x20000, 0x8000, 0xd0af35c5);
            ROM_LOAD("a78_14.bin", 0x28000, 0x8000, 0x7b5369a8);
            /* 0x30000-0x3ffff empty */
            ROM_LOAD("a78_15.bin", 0x40000, 0x8000, 0x6b61a413);   /* 2nd plane */

            ROM_LOAD("a78_16.bin", 0x48000, 0x8000, 0xb5492d97);
            ROM_LOAD("a78_17.bin", 0x50000, 0x8000, 0xd69762d5);
            ROM_LOAD("a78_18.bin", 0x58000, 0x8000, 0x9f243b68);
            ROM_LOAD("a78_19.bin", 0x60000, 0x8000, 0x66e9438c);
            ROM_LOAD("a78_20.bin", 0x68000, 0x8000, 0x9ef863ad);
            /* 0x70000-0x7ffff empty */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a78_08.bin", 0x0000, 0x08000, 0xae11a07b);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU */

            ROM_LOAD("a78_07.bin", 0x0000, 0x08000, 0x4f9a26e8);
            ROM_END();
        }
    };

    static RomLoadPtr rom_sboblbob = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);/* 64k+64k for the first CPU */

            ROM_LOAD("bbb-3.rom", 0x00000, 0x8000, 0xf304152a);
            ROM_LOAD("bb5", 0x08000, 0x4000, 0x13118eb1);/* banked at 8000-bfff. I must load */

            ROM_CONTINUE(0x10000, 0x4000);			/* bank 0 at 8000 because the code falls into */
            /* it from 7fff, so bank switching wouldn't work */

            ROM_LOAD("bbb-4.rom", 0x14000, 0x8000, 0x94c75591);/* banked at 8000-bfff */

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a78_09.bin", 0x00000, 0x8000, 0x20358c22);   /* 1st plane */

            ROM_LOAD("a78_10.bin", 0x08000, 0x8000, 0x930168a9);
            ROM_LOAD("a78_11.bin", 0x10000, 0x8000, 0x9773e512);
            ROM_LOAD("a78_12.bin", 0x18000, 0x8000, 0xd045549b);
            ROM_LOAD("a78_13.bin", 0x20000, 0x8000, 0xd0af35c5);
            ROM_LOAD("a78_14.bin", 0x28000, 0x8000, 0x7b5369a8);
            /* 0x30000-0x3ffff empty */
            ROM_LOAD("a78_15.bin", 0x40000, 0x8000, 0x6b61a413);   /* 2nd plane */

            ROM_LOAD("a78_16.bin", 0x48000, 0x8000, 0xb5492d97);
            ROM_LOAD("a78_17.bin", 0x50000, 0x8000, 0xd69762d5);
            ROM_LOAD("a78_18.bin", 0x58000, 0x8000, 0x9f243b68);
            ROM_LOAD("a78_19.bin", 0x60000, 0x8000, 0x66e9438c);
            ROM_LOAD("a78_20.bin", 0x68000, 0x8000, 0x9ef863ad);
            /* 0x70000-0x7ffff empty */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("a78_08.bin", 0x0000, 0x08000, 0xae11a07b);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for the third CPU */

            ROM_LOAD("a78_07.bin", 0x0000, 0x08000, 0x4f9a26e8);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tokio = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* main CPU */

            ROM_LOAD("a7127-1.256", 0x00000, 0x8000, 0x8c180896);
            /* ROMs banked at 8000-bfff */
            ROM_LOAD("a7128-1.256", 0x10000, 0x8000, 0x1b447527);
            ROM_LOAD("a7104.256", 0x18000, 0x8000, 0xa0a4ce0e);
            ROM_LOAD("a7105.256", 0x20000, 0x8000, 0x6da0b945);
            ROM_LOAD("a7106-1.256", 0x28000, 0x8000, 0x56927b3f);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a7108.256", 0x00000, 0x8000, 0x0439ab13);   /* 1st plane */

            ROM_LOAD("a7109.256", 0x08000, 0x8000, 0xedb3d2ff);
            ROM_LOAD("a7110.256", 0x10000, 0x8000, 0x69f0888c);
            ROM_LOAD("a7111.256", 0x18000, 0x8000, 0x4ae07c31);
            ROM_LOAD("a7112.256", 0x20000, 0x8000, 0x3f6bd706);
            ROM_LOAD("a7113.256", 0x28000, 0x8000, 0xf2c92aaa);
            ROM_LOAD("a7114.256", 0x30000, 0x8000, 0xc574b7b2);
            ROM_LOAD("a7115.256", 0x38000, 0x8000, 0x12d87e7f);
            ROM_LOAD("a7116.256", 0x40000, 0x8000, 0x0bce35b6);   /* 2nd plane */

            ROM_LOAD("a7117.256", 0x48000, 0x8000, 0xdeda6387);
            ROM_LOAD("a7118.256", 0x50000, 0x8000, 0x330cd9d7);
            ROM_LOAD("a7119.256", 0x58000, 0x8000, 0xfc4b29e0);
            ROM_LOAD("a7120.256", 0x60000, 0x8000, 0x65acb265);
            ROM_LOAD("a7121.256", 0x68000, 0x8000, 0x33cde9b2);
            ROM_LOAD("a7122.256", 0x70000, 0x8000, 0xfb98eac0);
            ROM_LOAD("a7123.256", 0x78000, 0x8000, 0x30bd46ad);

            ROM_REGION(0x10000, REGION_CPU2);/* video CPU */

            ROM_LOAD("a7101.256", 0x00000, 0x8000, 0x0867c707);

            ROM_REGION(0x10000, REGION_CPU3);/* audio CPU */

            ROM_LOAD("a7107.256", 0x0000, 0x08000, 0xf298cc7b);
            ROM_END();
        }
    };

    static RomLoadPtr rom_tokiob = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);/* main CPU */

            ROM_LOAD("2", 0x00000, 0x8000, 0xf583b1ef);
            /* ROMs banked at 8000-bfff */
            ROM_LOAD("3", 0x10000, 0x8000, 0x69dacf44);
            ROM_LOAD("a7104.256", 0x18000, 0x8000, 0xa0a4ce0e);
            ROM_LOAD("a7105.256", 0x20000, 0x8000, 0x6da0b945);
            ROM_LOAD("6", 0x28000, 0x8000, 0x1490e95b);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("a7108.256", 0x00000, 0x8000, 0x0439ab13);   /* 1st plane */

            ROM_LOAD("a7109.256", 0x08000, 0x8000, 0xedb3d2ff);
            ROM_LOAD("a7110.256", 0x10000, 0x8000, 0x69f0888c);
            ROM_LOAD("a7111.256", 0x18000, 0x8000, 0x4ae07c31);
            ROM_LOAD("a7112.256", 0x20000, 0x8000, 0x3f6bd706);
            ROM_LOAD("a7113.256", 0x28000, 0x8000, 0xf2c92aaa);
            ROM_LOAD("a7114.256", 0x30000, 0x8000, 0xc574b7b2);
            ROM_LOAD("a7115.256", 0x38000, 0x8000, 0x12d87e7f);
            ROM_LOAD("a7116.256", 0x40000, 0x8000, 0x0bce35b6);   /* 2nd plane */

            ROM_LOAD("a7117.256", 0x48000, 0x8000, 0xdeda6387);
            ROM_LOAD("a7118.256", 0x50000, 0x8000, 0x330cd9d7);
            ROM_LOAD("a7119.256", 0x58000, 0x8000, 0xfc4b29e0);
            ROM_LOAD("a7120.256", 0x60000, 0x8000, 0x65acb265);
            ROM_LOAD("a7121.256", 0x68000, 0x8000, 0x33cde9b2);
            ROM_LOAD("a7122.256", 0x70000, 0x8000, 0xfb98eac0);
            ROM_LOAD("a7123.256", 0x78000, 0x8000, 0x30bd46ad);

            ROM_REGION(0x10000, REGION_CPU2);/* video CPU */

            ROM_LOAD("a7101.256", 0x00000, 0x8000, 0x0867c707);

            ROM_REGION(0x10000, REGION_CPU3);/* audio CPU */

            ROM_LOAD("a7107.256", 0x0000, 0x08000, 0xf298cc7b);
            ROM_END();
        }
    };

    public static void MOD_PAGE(int page, int addr, int data) {
        memory_region(REGION_CPU1).write(page != 0 ? addr - 0x8000 + 0x10000 + 0x4000 * (page - 1) : addr, data);
    }

    public static InitDriverPtr init_boblbobl = new InitDriverPtr() {
        public void handler() {
            /* these shouldn't be necessary, surely - this is a bootleg ROM
             * with the protection removed - so what are all these JP's to
             * 0xa288 doing?  and why does the emulator fail the ROM checks?
             */

            MOD_PAGE(3, 0x9a71, 0x00);
            MOD_PAGE(3, 0x9a72, 0x00);
            MOD_PAGE(3, 0x9a73, 0x00);
            MOD_PAGE(3, 0xa4af, 0x00);
            MOD_PAGE(3, 0xa4b0, 0x00);
            MOD_PAGE(3, 0xa4b1, 0x00);
            MOD_PAGE(3, 0xa55d, 0x00);
            MOD_PAGE(3, 0xa55e, 0x00);
            MOD_PAGE(3, 0xa55f, 0x00);
            MOD_PAGE(3, 0xb561, 0x00);
            MOD_PAGE(3, 0xb562, 0x00);
            MOD_PAGE(3, 0xb563, 0x00);
        }
    };

    public static GameDriver driver_bublbobl = new GameDriver("1986", "bublbobl", "bublbobl.java", rom_bublbobl, null, machine_driver_bublbobl, input_ports_bublbobl, null, ROT0, "Taito Corporation", "Bubble Bobble", GAME_NO_COCKTAIL);
    public static GameDriver driver_bublbobr = new GameDriver("1986", "bublbobr", "bublbobl.java", rom_bublbobr, driver_bublbobl, machine_driver_bublbobl, input_ports_bublbobl, null, ROT0, "Taito America Corporation (Romstar license)", "Bubble Bobble (US set 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_bubbobr1 = new GameDriver("1986", "bubbobr1", "bublbobl.java", rom_bubbobr1, driver_bublbobl, machine_driver_bublbobl, input_ports_bublbobl, null, ROT0, "Taito America Corporation (Romstar license)", "Bubble Bobble (US set 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_boblbobl = new GameDriver("1986", "boblbobl", "bublbobl.java", rom_boblbobl, driver_bublbobl, machine_driver_boblbobl, input_ports_boblbobl, init_boblbobl, ROT0, "bootleg", "Bobble Bobble", GAME_NO_COCKTAIL);
    public static GameDriver driver_sboblbob = new GameDriver("1986", "sboblbob", "bublbobl.java", rom_sboblbob, driver_bublbobl, machine_driver_boblbobl, input_ports_sboblbob, null, ROT0, "bootleg", "Super Bobble Bobble", GAME_NO_COCKTAIL);
    public static GameDriver driver_tokio = new GameDriver("1986", "tokio", "bublbobl.java", rom_tokio, null, machine_driver_tokio, input_ports_tokio, null, ROT90, "Taito", "Tokio / Scramble Formation", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
    public static GameDriver driver_tokiob = new GameDriver("1986", "tokiob", "bublbobl.java", rom_tokiob, driver_tokio, machine_driver_tokio, input_ports_tokio, null, ROT90, "bootleg", "Tokio / Scramble Formation (bootleg)", GAME_NO_COCKTAIL);
}
