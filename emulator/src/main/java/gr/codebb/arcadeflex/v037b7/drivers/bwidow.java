/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static common.libc.cstring.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_POKEY;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.system1H.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.system1.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.memcpy;
import static gr.codebb.arcadeflex.v037b7.machine.atari_vg.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memory_set_opcode_base;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v036.vidhrdw.avgdvg.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v037b7.drivers.bzone.bzone_IN0_r;
import static gr.codebb.arcadeflex.v037b7.machine.atari_vg.atari_vg_earom_ctrl_w;
import static gr.codebb.arcadeflex.v037b7.machine.mathbox.*;
import static gr.codebb.arcadeflex.v037b7.machine.slapstic.*;

public class bwidow {

    public static final int IN_LEFT = (1 << 0);
    public static final int IN_RIGHT = (1 << 1);
    public static final int IN_FIRE = (1 << 2);
    public static final int IN_SHIELD = (1 << 3);
    public static final int IN_THRUST = (1 << 4);
    public static final int IN_P1 = (1 << 5);
    public static final int IN_P2 = (1 << 6);

    /*
	
	These 7 memory locations are used to read the 2 players' controls as well
	as sharing some dipswitch info in the lower 4 bits pertaining to coins/credits
	Typically, only the high 2 bits are read.
	
     */
    public static ReadHandlerPtr spacduel_IN3_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            int res;
            int res1;
            int res2;

            res1 = readinputport(3);
            res2 = readinputport(4);
            res = 0x00;

            switch (offset & 0x07) {
                case 0:
                    if ((res1 & IN_SHIELD) != 0) {
                        res |= 0x80;
                    }
                    if ((res1 & IN_FIRE) != 0) {
                        res |= 0x40;
                    }
                    break;
                case 1:
                    /* Player 2 */
                    if ((res2 & IN_SHIELD) != 0) {
                        res |= 0x80;
                    }
                    if ((res2 & IN_FIRE) != 0) {
                        res |= 0x40;
                    }
                    break;
                case 2:
                    if ((res1 & IN_LEFT) != 0) {
                        res |= 0x80;
                    }
                    if ((res1 & IN_RIGHT) != 0) {
                        res |= 0x40;
                    }
                    break;
                case 3:
                    /* Player 2 */
                    if ((res2 & IN_LEFT) != 0) {
                        res |= 0x80;
                    }
                    if ((res2 & IN_RIGHT) != 0) {
                        res |= 0x40;
                    }
                    break;
                case 4:
                    if ((res1 & IN_THRUST) != 0) {
                        res |= 0x80;
                    }
                    if ((res1 & IN_P1) != 0) {
                        res |= 0x40;
                    }
                    break;
                case 5:
                    /* Player 2 */
                    if ((res2 & IN_THRUST) != 0) {
                        res |= 0x80;
                    }
                    break;
                case 6:
                    if ((res1 & IN_P2) != 0) {
                        res |= 0x80;
                    }
                    break;
                case 7:
                    res = (0x00 /* upright */ | (0 & 0x40));
                    break;
            }
            return res;
        }
    };

    static int lastdata;
    public static WriteHandlerPtr bwidow_misc_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*
			0x10 = p1 led
			0x20 = p2 led
			0x01 = coin counter 1
			0x02 = coin counter 2
             */

            if (data == lastdata) {
                return;
            }
/*TODO*///            set_led_status(0, ~data & 0x10);
/*TODO*///            set_led_status(1, ~data & 0x20);
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);
            lastdata = data;
        }
    };

    static MemoryReadAddress bwidow_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x600f, pokey1_r),
                new MemoryReadAddress(0x6800, 0x680f, pokey2_r),
                new MemoryReadAddress(0x7000, 0x7000, atari_vg_earom_r),
                new MemoryReadAddress(0x7800, 0x7800, bzone_IN0_r), /* IN0 */
                new MemoryReadAddress(0x8000, 0x8000, input_port_3_r), /* IN1 */
                new MemoryReadAddress(0x8800, 0x8800, input_port_4_r), /* IN1 */
                new MemoryReadAddress(0x9000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bwidow_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x2800, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x6000, 0x67ff, pokey1_w),
                new MemoryWriteAddress(0x6800, 0x6fff, pokey2_w),
                new MemoryWriteAddress(0x8800, 0x8800, bwidow_misc_w), /* coin counters, leds */
                new MemoryWriteAddress(0x8840, 0x8840, avgdvg_go_w),
                new MemoryWriteAddress(0x8880, 0x8880, avgdvg_reset_w),
                new MemoryWriteAddress(0x88c0, 0x88c0, MWA_NOP), /* interrupt acknowledge */
                new MemoryWriteAddress(0x8900, 0x8900, atari_vg_earom_ctrl_w),
                new MemoryWriteAddress(0x8940, 0x897f, atari_vg_earom_w),
                new MemoryWriteAddress(0x8980, 0x89ed, MWA_NOP), /* watchdog clear */
                new MemoryWriteAddress(0x9000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress spacduel_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0800, bzone_IN0_r), /* IN0 */
                new MemoryReadAddress(0x0900, 0x0907, spacduel_IN3_r), /* IN1 */
                new MemoryReadAddress(0x0a00, 0x0a00, atari_vg_earom_r),
                new MemoryReadAddress(0x1000, 0x100f, pokey1_r),
                new MemoryReadAddress(0x1400, 0x140f, pokey2_r),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x2800, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x8fff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress spacduel_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0905, 0x0906, MWA_NOP), /* ignore? */
                //	new MemoryWriteAddress( 0x0c00, 0x0c00, coin_counter_w ), /* coin out */
                new MemoryWriteAddress(0x0c80, 0x0c80, avgdvg_go_w),
                new MemoryWriteAddress(0x0d00, 0x0d00, MWA_NOP), /* watchdog clear */
                new MemoryWriteAddress(0x0d80, 0x0d80, avgdvg_reset_w),
                new MemoryWriteAddress(0x0e00, 0x0e00, MWA_NOP), /* interrupt acknowledge */
                new MemoryWriteAddress(0x0e80, 0x0e80, atari_vg_earom_ctrl_w),
                new MemoryWriteAddress(0x0f00, 0x0f3f, atari_vg_earom_w),
                new MemoryWriteAddress(0x1000, 0x13ff, pokey1_w),
                new MemoryWriteAddress(0x1400, 0x17ff, pokey2_w),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x2800, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x8fff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_bwidow = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
            /* bit 6 is the VG HALT bit. We set it to "low" */
 /* per default (busy vector processor). */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* bit 7 is tied to a 3kHz clock */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x04, "*4");
            PORT_DIPSETTING(0x08, "*5");
            PORT_DIPSETTING(0x0c, "*6");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x10, "*2");
            PORT_DIPNAME(0xe0, 0x00, "Bonus Coins");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "3 credits/2 coins");
            PORT_DIPSETTING(0x40, "5 credits/4 coins");
            PORT_DIPSETTING(0x60, "6 credits/4 coins");
            PORT_DIPSETTING(0x80, "6 credits/6 coins");
            PORT_DIPSETTING(0xa0, "4 credits/3 coins");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x01, "Max Start");
            PORT_DIPSETTING(0x00, "Lev 13");
            PORT_DIPSETTING(0x01, "Lev 21");
            PORT_DIPSETTING(0x02, "Lev 37");
            PORT_DIPSETTING(0x03, "Lev 53");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x08, "5");
            PORT_DIPSETTING(0x0c, "6");
            PORT_DIPNAME(0x30, 0x10, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x10, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x30, "Demo");
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20000");
            PORT_DIPSETTING(0x40, "30000");
            PORT_DIPSETTING(0x80, "40000");
            PORT_DIPSETTING(0xc0, "None");

            PORT_START();
            /* IN3 - Movement joystick */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - Firing joystick */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_gravitar = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
            /* bit 6 is the VG HALT bit. We set it to "low" */
 /* per default (busy vector processor). */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* bit 7 is tied to a 3kHz clock */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_BIT(0x03, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x08, "5");
            PORT_DIPSETTING(0x0c, "6");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x40, "20000");
            PORT_DIPSETTING(0x80, "30000");
            PORT_DIPSETTING(0xc0, "None");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x04, "*4");
            PORT_DIPSETTING(0x08, "*5");
            PORT_DIPSETTING(0x0c, "*6");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x10, "*2");
            PORT_DIPNAME(0xe0, 0x00, "Bonus Coins");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "3 credits/2 coins");
            PORT_DIPSETTING(0x40, "5 credits/4 coins");
            PORT_DIPSETTING(0x60, "6 credits/4 coins");
            PORT_DIPSETTING(0x80, "6 credits/6 coins");
            PORT_DIPSETTING(0xa0, "4 credits/3 coins");

            PORT_START();
            /* IN3 - Player 1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN4 - Player 2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_spacduel = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x0c, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_SERVICE, "Diagnostic Step", KEYCODE_F1, IP_JOY_NONE);
            /* bit 6 is the VG HALT bit. We set it to "low" */
 /* per default (busy vector processor). */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* bit 7 is tied to a 3kHz clock */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPSETTING(0x02, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x0c, "Medium");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPNAME(0x30, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x10, "German");
            PORT_DIPSETTING(0x20, "French");
            PORT_DIPSETTING(0x30, "Spanish");
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0xc0, "8000");
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x40, "15000");
            PORT_DIPSETTING(0x80, "None");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x04, "*4");
            PORT_DIPSETTING(0x08, "*5");
            PORT_DIPSETTING(0x0c, "*6");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, "*1");
            PORT_DIPSETTING(0x10, "*2");
            PORT_DIPNAME(0xe0, 0x00, "Bonus Coins");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "3 credits/2 coins");
            PORT_DIPSETTING(0xa0, "4 credits/3 coins");
            PORT_DIPSETTING(0x40, "5 credits/4 coins");
            PORT_DIPSETTING(0x60, "6 credits/4 coins");
            PORT_DIPSETTING(0x80, "6 credits/6 coins");

            /* See machine/spacduel.c for more info on these 2 ports */
            PORT_START();
            /* IN3 - Player 1 - spread over 8 memory locations */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN4 - Player 2 - spread over 8 memory locations */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static POKEYinterface pokey_interface = new POKEYinterface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz??? */
            new int[]{50, 50},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            /* The allpot handler */
            new ReadHandlerPtr[]{input_port_1_r, input_port_2_r}
    );

    static MachineDriver machine_driver_bwidow = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        bwidow_readmem, bwidow_writemem, null, null,
                        interrupt, 4 /* 4.1ms */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 480, 0, 440),
            null,
            256, 0,
            avg_init_palette_multi,
            VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
            null,
            avg_start,
            avg_stop,
            vector_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        pokey_interface
                )
            }
    );

    static MachineDriver machine_driver_gravitar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        bwidow_readmem, bwidow_writemem, null, null,
                        interrupt, 4 /* 4.1ms */
                )
            },
            60, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 420, 0, 400),
            null,
            256, 0,
            avg_init_palette_multi,
            VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
            null,
            avg_start,
            avg_stop,
            vector_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        pokey_interface
                )
            },
            atari_vg_earom_handler
    );

    static MachineDriver machine_driver_spacduel = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        spacduel_readmem, spacduel_writemem, null, null,
                        interrupt, 4 /* 5.4ms */
                )
            },
            45, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 540, 0, 400),
            null,
            256, 0,
            avg_init_palette_multi,
            VIDEO_TYPE_VECTOR | VIDEO_SUPPORTS_DIRTY,
            null,
            avg_start,
            avg_stop,
            vector_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        pokey_interface
                )
            },
            atari_vg_earom_handler
    );

    /**
     * *************************************************************************
     * Game driver(s)
     **************************************************************************
     */
    static RomLoadPtr rom_bwidow = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
 /* Vector ROM */
            ROM_LOAD("136017.107", 0x2800, 0x0800, 0x97f6000c);
            ROM_LOAD("136017.108", 0x3000, 0x1000, 0x3da354ed);
            ROM_LOAD("136017.109", 0x4000, 0x1000, 0x2fc4ce79);
            ROM_LOAD("136017.110", 0x5000, 0x1000, 0x0dd52987);
            /* Program ROM */
            ROM_LOAD("136017.101", 0x9000, 0x1000, 0xfe3febb7);
            ROM_LOAD("136017.102", 0xa000, 0x1000, 0x10ad0376);
            ROM_LOAD("136017.103", 0xb000, 0x1000, 0x8a1430ee);
            ROM_LOAD("136017.104", 0xc000, 0x1000, 0x44f9943f);
            ROM_LOAD("136017.105", 0xd000, 0x1000, 0xa046a2e2);
            ROM_LOAD("136017.106", 0xe000, 0x1000, 0x4dc28b22);
            ROM_RELOAD(0xf000, 0x1000);/* for reset/interrupt vectors */
            ROM_END();
        }
    };

    static RomLoadPtr rom_gravitar = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
 /* Vector ROM */
            ROM_LOAD("136010.210", 0x2800, 0x0800, 0xdebcb243);
            ROM_LOAD("136010.207", 0x3000, 0x1000, 0x4135629a);
            ROM_LOAD("136010.208", 0x4000, 0x1000, 0x358f25d9);
            ROM_LOAD("136010.309", 0x5000, 0x1000, 0x4ac78df4);
            /* Program ROM */
            ROM_LOAD("136010.301", 0x9000, 0x1000, 0xa2a55013);
            ROM_LOAD("136010.302", 0xa000, 0x1000, 0xd3700b3c);
            ROM_LOAD("136010.303", 0xb000, 0x1000, 0x8e12e3e0);
            ROM_LOAD("136010.304", 0xc000, 0x1000, 0x467ad5da);
            ROM_LOAD("136010.305", 0xd000, 0x1000, 0x840603af);
            ROM_LOAD("136010.306", 0xe000, 0x1000, 0x3f3805ad);
            ROM_RELOAD(0xf000, 0x1000);/* for reset/interrupt vectors */
            ROM_END();
        }
    };

    static RomLoadPtr rom_gravitr2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
 /* Vector ROM */
            ROM_LOAD("136010.210", 0x2800, 0x0800, 0xdebcb243);
            ROM_LOAD("136010.207", 0x3000, 0x1000, 0x4135629a);
            ROM_LOAD("136010.208", 0x4000, 0x1000, 0x358f25d9);
            ROM_LOAD("136010.209", 0x5000, 0x1000, 0x37034287);
            /* Program ROM */
            ROM_LOAD("136010.201", 0x9000, 0x1000, 0x167315e4);
            ROM_LOAD("136010.202", 0xa000, 0x1000, 0xaaa9e62c);
            ROM_LOAD("136010.203", 0xb000, 0x1000, 0xae437253);
            ROM_LOAD("136010.204", 0xc000, 0x1000, 0x5d6bc29e);
            ROM_LOAD("136010.205", 0xd000, 0x1000, 0x0db1ff34);
            ROM_LOAD("136010.206", 0xe000, 0x1000, 0x4521ca48);
            ROM_RELOAD(0xf000, 0x1000);/* for reset/interrupt vectors */
            ROM_END();
        }
    };

    static RomLoadPtr rom_spacduel = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
 /* Vector ROM */
            ROM_LOAD("136006.106", 0x2800, 0x0800, 0x691122fe);
            ROM_LOAD("136006.107", 0x3000, 0x1000, 0xd8dd0461);
            /* Program ROM */
            ROM_LOAD("136006.201", 0x4000, 0x1000, 0xf4037b6e);
            ROM_LOAD("136006.102", 0x5000, 0x1000, 0x4c451e8a);
            ROM_LOAD("136006.103", 0x6000, 0x1000, 0xee72da63);
            ROM_LOAD("136006.104", 0x7000, 0x1000, 0xe41b38a3);
            ROM_LOAD("136006.105", 0x8000, 0x1000, 0x5652710f);
            ROM_RELOAD(0x9000, 0x1000);
            ROM_RELOAD(0xa000, 0x1000);
            ROM_RELOAD(0xb000, 0x1000);
            ROM_RELOAD(0xc000, 0x1000);
            ROM_RELOAD(0xd000, 0x1000);
            ROM_RELOAD(0xe000, 0x1000);
            ROM_RELOAD(0xf000, 0x1000);/* for reset/interrupt vectors */
            ROM_END();
        }
    };

    public static GameDriver driver_bwidow = new GameDriver("1982", "bwidow", "bwidow.java", rom_bwidow, null, machine_driver_bwidow, input_ports_bwidow, null, ROT0, "Atari", "Black Widow");
    public static GameDriver driver_gravitar = new GameDriver("1982", "gravitar", "bwidow.java", rom_gravitar, null, machine_driver_gravitar, input_ports_gravitar, null, ROT0, "Atari", "Gravitar (version 3)");
    public static GameDriver driver_gravitr2 = new GameDriver("1982", "gravitr2", "bwidow.java", rom_gravitr2, driver_gravitar, machine_driver_gravitar, input_ports_gravitar, null, ROT0, "Atari", "Gravitar (version 2)");
    public static GameDriver driver_spacduel = new GameDriver("1980", "spacduel", "bwidow.java", rom_spacduel, null, machine_driver_spacduel, input_ports_spacduel, null, ROT0, "Atari", "Space Duel");
}
