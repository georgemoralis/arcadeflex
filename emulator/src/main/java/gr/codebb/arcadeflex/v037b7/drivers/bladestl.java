/*
 * ported to v0.37b7
 * ported to v0.36
 * 
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import gr.codebb.arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_UPD7759;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.SOUND_YM2203;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K007342.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K007420.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.bladestl.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.sound.upd7759.*;
import static gr.codebb.arcadeflex.v036.sound.upd7759H.*;

public class bladestl {

    public static InterruptPtr bladestl_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                if (K007342_is_INT_enabled() != 0) {
                    return HD6309_INT_FIRQ;
                }
            } else if ((cpu_getiloops() % 2) != 0) {
                return nmi_interrupt.handler();
            }
            return ignore_interrupt.handler();
        }
    };
    static int[] last = new int[4];
    public static ReadHandlerPtr trackball_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            int curr, delta;

            curr = readinputport(5 + offset);
            delta = (curr - last[offset]) & 0xff;
            last[offset] = curr;
            return (delta & 0x80) | (curr >> 1);
        }
    };

    public static WriteHandlerPtr bladestl_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int bankaddress;

            /* bits 0 & 1 = coin counters */
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);

            /* bits 2 & 3 = lamps */
            //osd_led_w(1,(data & 0x08) >> 3);
            //set_led_status(1,data & 0x08);
            /* bit 4 = relay (???) */
 /* bits 5-6 = bank number */
            bankaddress = 0x10000 + ((data & 0x60) >> 5) * 0x2000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* bit 7 = select sprite bank */
            bladestl_spritebank = (data & 0x80) << 3;

        }
    };

    public static WriteHandlerPtr bladestl_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, M6809_INT_IRQ);
            //if (errorlog) fprintf(errorlog,"(sound) write %02x\n", data);
        }
    };

    public static WriteHandlerPtr bladestl_port_A_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0-4 = uPD7759 sample number (chip 0) */
            //UPD7759_message_w( 0, data);
            //if (data)
            //	if (errorlog) fprintf(errorlog,"%04x: (port A) write %02x\n",cpu_get_pc(), data);
        }
    };

    public static WriteHandlerPtr bladestl_port_B_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* unknown */
            //if (data)
            //	if (errorlog) fprintf(errorlog,"%04x: (port B) write %02x\n",cpu_get_pc(), data);
        }
    };

    public static WriteHandlerPtr bladestl_speech_ctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* not understood yet */
            //if (data)
            //	if (errorlog) fprintf(errorlog,"%04x: (speech_ctrl) write %02x\n",cpu_get_pc(), data);
        }
    };

    static MemoryReadAddress bladestl_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, K007342_r), /* Color RAM + Video RAM */
                new MemoryReadAddress(0x2000, 0x21ff, K007420_r), /* Sprite RAM */
                new MemoryReadAddress(0x2200, 0x23ff, K007342_scroll_r), /* Scroll RAM */
                new MemoryReadAddress(0x2400, 0x245f, paletteram_r), /* Palette */
                new MemoryReadAddress(0x2e01, 0x2e01, input_port_3_r), /* 1P controls */
                new MemoryReadAddress(0x2e02, 0x2e02, input_port_4_r), /* 2P controls */
                new MemoryReadAddress(0x2e03, 0x2e03, input_port_1_r), /* DISPW #2 */
                new MemoryReadAddress(0x2e40, 0x2e40, input_port_0_r), /* DIPSW #1 */
                new MemoryReadAddress(0x2e00, 0x2e00, input_port_2_r), /* DIPSW #3, coinsw, startsw */
                new MemoryReadAddress(0x2f00, 0x2f03, trackball_r), /* Trackballs */
                new MemoryReadAddress(0x2f80, 0x2f9f, K051733_r), /* Protection: 051733 */
                new MemoryReadAddress(0x4000, 0x5fff, MRA_RAM), /* Work RAM */
                new MemoryReadAddress(0x6000, 0x7fff, MRA_BANK1), /* banked ROM */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM), /* ROM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bladestl_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, K007342_w), /* Color RAM + Video RAM */
                new MemoryWriteAddress(0x2000, 0x21ff, K007420_w), /* Sprite RAM */
                new MemoryWriteAddress(0x2200, 0x23ff, K007342_scroll_w), /* Scroll RAM */
                new MemoryWriteAddress(0x2400, 0x245f, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram),/* palette */
                new MemoryWriteAddress(0x2600, 0x2607, K007342_vreg_w), /* Video Registers */
                new MemoryWriteAddress(0x2e80, 0x2e80, bladestl_sh_irqtrigger_w),/* cause interrupt on audio CPU */
                new MemoryWriteAddress(0x2ec0, 0x2ec0, watchdog_reset_w), /* watchdog reset */
                new MemoryWriteAddress(0x2f40, 0x2f40, bladestl_bankswitch_w), /* bankswitch control */
                new MemoryWriteAddress(0x2f80, 0x2f9f, K051733_w), /* Protection: 051733 */
                new MemoryWriteAddress(0x2fc0, 0x2fc0, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x4000, 0x5fff, MWA_RAM), /* Work RAM */
                new MemoryWriteAddress(0x6000, 0x7fff, MWA_RAM), /* banked ROM */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM), /* ROM */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress bladestl_readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0x1000, 0x1000, YM2203_status_port_0_r), /* YM2203 */
                new MemoryReadAddress(0x1001, 0x1001, YM2203_read_port_0_r), /* YM2203 */
                new MemoryReadAddress(0x4000, 0x4000, UPD7759_busy_r), /* UPD7759? */
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r), /* soundlatch_r */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM), /* ROM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bladestl_writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM), /* RAM */
                new MemoryWriteAddress(0x1000, 0x1000, YM2203_control_port_0_w),/* YM2203 */
                new MemoryWriteAddress(0x1001, 0x1001, YM2203_write_port_0_w), /* YM2203 */
                new MemoryWriteAddress(0x3000, 0x3000, bladestl_speech_ctrl_w), /* UPD7759 */
                new MemoryWriteAddress(0x5000, 0x5000, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM), /* ROM */
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * Input Ports
     *
     **************************************************************************
     */
    static InputPortPtr input_ports_bladestl = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW #1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));

            PORT_START();
            /* DSW #2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, "Bonus time set");
            PORT_DIPSETTING(0x18, "30 secs");
            PORT_DIPSETTING(0x10, "20 secs");
            PORT_DIPSETTING(0x08, "15 secs");
            PORT_DIPSETTING(0x00, "10 secs");
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* COINSW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* PLAYER 1 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_DIPNAME(0x80, 0x80, "Period time set");
            PORT_DIPSETTING(0x80, "4");
            PORT_DIPSETTING(0x00, "7");

            PORT_START();
            /* PLAYER 2 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            /* Trackball 1P */
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER1, 100, 63, 0, 0);
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 63, 0, 0);

            /* Trackball 2P */
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER2, 100, 63, 0, 0);
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 63, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_bladstle = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW #1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_5C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));

            PORT_START();
            /* DSW #2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very difficult");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* COINSW */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* PLAYER 1 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PLAYER 2 INPUTS */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            /* Trackball 1P */
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER1, 100, 63, 0, 0);
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER1, 100, 63, 0, 0);

            /* Trackball 2P */
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_Y | IPF_REVERSE | IPF_PLAYER2, 100, 63, 0, 0);
            PORT_START();
            PORT_ANALOG(0xff, 0x00, IPT_TRACKBALL_X | IPF_PLAYER2, 100, 63, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8 x 8 characters */
            0x40000 / 32, /* 8192 characters */
            4, /* 4bpp */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{2 * 4, 3 * 4, 0 * 4, 1 * 4, 6 * 4, 7 * 4, 4 * 4, 5 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every character takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            8, 8, /* 8*8 sprites */
            0x40000 / 32, /* 8192 sprites */
            4, /* 4 bpp */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x000000, charlayout, 0, 2), /* colors 00..31 */
                new GfxDecodeInfo(REGION_GFX1, 0x040000, spritelayout, 32, 16), /* colors 32..47 but using lookup table */
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * *************************************************************************
     *
     * Machine Driver
     *
     **************************************************************************
     */
    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz? */
            new int[]{YM2203_VOL(45, 45)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{bladestl_port_A_w}, /* uPD7759 (chip 0) */
            new WriteHandlerPtr[]{bladestl_port_B_w} /* uPD7759 (chip 1)??? */
    );

    static UPD7759_interface upd7759_interface = new UPD7759_interface(
            2, /* number of chips */
            UPD7759_STANDARD_CLOCK,
            new int[]{60, 50}, /* volume */
            new int[]{REGION_SOUND1, REGION_SOUND2}, /* memory regions */
            UPD7759_STANDALONE_MODE,
            new irqcallbackPtr[]{null, null}
    );
    static MachineDriver machine_driver_bladestl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_HD6309,
                        3000000, /* 24MHz/8 (?) */
                        bladestl_readmem, bladestl_writemem, null, null,
                        bladestl_interrupt, 2 /* (1 IRQ + 1 NMI) */
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        2000000, /* ? */
                        bladestl_readmem_sound, bladestl_writemem_sound, null, null,
                        ignore_interrupt, 0 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            10,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            48, 48 + 16 * 16,
            bladestl_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            bladestl_vh_start,
            bladestl_vh_stop,
            bladestl_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )

            }
    );

    /**
     * *************************************************************************
     *
     * Game ROMs
     *
     **************************************************************************
     */
    static RomLoadPtr rom_bladestl = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("797t01.bin", 0x10000, 0x08000, 0x89d7185d);/* fixed ROM */

            ROM_CONTINUE(0x08000, 0x08000);
            /* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("797c02", 0x08000, 0x08000, 0x65a331ea);

            ROM_REGION(0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("797a05", 0x000000, 0x40000, 0x5491ba28);/* tiles */

            ROM_LOAD("797a06", 0x040000, 0x40000, 0xd055f5cc);/* sprites */

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("797a07", 0x0000, 0x0100, 0x7aecad4e);/* sprites lookup table */

            ROM_REGION(0x80000, REGION_SOUND1);/* uPD7759 data (chip 1) */

            ROM_LOAD("797a03", 0x00000, 0x80000, 0x9ee1a542);

            ROM_REGION(0x40000, REGION_SOUND2);/* uPD7759 data (chip 2) */

            ROM_LOAD("797a04", 0x000000, 0x40000, 0x9ac8ea4e);
            ROM_END();
        }
    };

    static RomLoadPtr rom_bladstle = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x18000, REGION_CPU1);/* code + banked roms */

            ROM_LOAD("797e01", 0x10000, 0x08000, 0xf8472e95);/* fixed ROM */

            ROM_CONTINUE(0x08000, 0x08000);
            /* banked ROM */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("797c02", 0x08000, 0x08000, 0x65a331ea);

            ROM_REGION(0x080000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("797a05", 0x000000, 0x40000, 0x5491ba28);/* tiles */

            ROM_LOAD("797a06", 0x040000, 0x40000, 0xd055f5cc);/* sprites */

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("797a07", 0x0000, 0x0100, 0x7aecad4e);/* sprites lookup table */

            ROM_REGION(0x80000, REGION_SOUND1);/* uPD7759 data (chip 1) */

            ROM_LOAD("797a03", 0x00000, 0x80000, 0x9ee1a542);

            ROM_REGION(0x40000, REGION_SOUND2);/* uPD7759 data (chip 2) */

            ROM_LOAD("797a04", 0x000000, 0x40000, 0x9ac8ea4e);
            ROM_END();
        }
    };

    public static GameDriver driver_bladestl = new GameDriver("1987", "bladestl", "bladestl.java", rom_bladestl, null, machine_driver_bladestl, input_ports_bladestl, null, ROT90, "Konami", "Blades of Steel (version T)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_bladstle = new GameDriver("1987", "bladstle", "bladestl.java", rom_bladstle, driver_bladestl, machine_driver_bladestl, input_ports_bladstle, null, ROT90, "Konami", "Blades of Steel (version E)", GAME_IMPERFECT_SOUND);
}
