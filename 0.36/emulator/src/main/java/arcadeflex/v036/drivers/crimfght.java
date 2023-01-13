/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 13/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.konami.konami.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static arcadeflex.v036.sound.k007232.*;
import static arcadeflex.v036.sound.k007232H.*;
import static arcadeflex.v036.sound.mixerH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.crimfght.*;
import static arcadeflex.v036.vidhrdw.konamiic.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;

public class crimfght {

    public static konami_cpu_setlines_callbackPtr crimfght_banking = new konami_cpu_setlines_callbackPtr() {
        public void handler(int lines) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int offs = 0;

            /* bit 5 = select work RAM or palette */
            if ((lines & 0x20) != 0) {
                cpu_setbankhandler_r(1, paletteram_r);
                /* palette */
                cpu_setbankhandler_w(1, paletteram_xBBBBBGGGGGRRRRR_swap_w);
                /* palette */
            } else {
                cpu_setbankhandler_r(1, mrh_ram);
                /* RAM */
                cpu_setbankhandler_w(1, mwh_ram);
                /* RAM */
            }

            /* bit 6 = enable char ROM reading through the video RAM */
            K052109_set_RMRD_line((lines & 0x40) != 0 ? ASSERT_LINE : CLEAR_LINE);

            offs = 0x10000 + ((lines & 0x0f) * 0x2000);
            cpu_setbank(2, new UBytePtr(RAM, offs));
        }
    };

    public static InitMachineHandlerPtr crimfght_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            UBytePtr RAM = memory_region(REGION_CPU1);

            konami_cpu_setlines_callback = crimfght_banking;

            /* init the default bank */
            cpu_setbank(2, new UBytePtr(RAM, 0x10000));
        }
    };

    public static WriteHandlerPtr crimfght_coin_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);
        }
    };

    public static WriteHandlerPtr crimfght_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, 0xff);
        }
    };

    public static WriteHandlerPtr crimfght_snd_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_SOUND1);
            /* b1: bank for channel A */
 /* b0: bank for channel B */

            int bank_A = 0x20000 * ((data >> 1) & 0x01);
            int bank_B = 0x20000 * ((data) & 0x01);

            K007232_bankswitch(0, new UBytePtr(RAM, bank_A), new UBytePtr(RAM, bank_B));
        }
    };

    /**
     * *****************************************
     */
    public static ReadHandlerPtr speedup_r = new ReadHandlerPtr() {
        public int handler(int offs) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            int data = (RAM.read(0x0414) << 8) | RAM.read(0x0415);

            if (data < memory_region_length(REGION_CPU1)) {
                data = (RAM.read(data) << 8) | RAM.read(data + 1);

                if (data == 0xffff) {
                    cpu_spinuntil_int();
                }
            }

            return RAM.read(0x0414);
        }
    };

    static MemoryReadAddress crimfght_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_BANK1), /* banked RAM */
                new MemoryReadAddress(0x0414, 0x0414, speedup_r),
                new MemoryReadAddress(0x0400, 0x1fff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0x3f80, 0x3f80, input_port_7_r), /* Coinsw */
                new MemoryReadAddress(0x3f81, 0x3f81, input_port_3_r), /* 1P controls */
                new MemoryReadAddress(0x3f82, 0x3f82, input_port_4_r), /* 2P controls */
                new MemoryReadAddress(0x3f83, 0x3f83, input_port_1_r), /* DSW #2 */
                new MemoryReadAddress(0x3f84, 0x3f84, input_port_2_r), /* DSW #3 */
                new MemoryReadAddress(0x3f85, 0x3f85, input_port_5_r), /* 3P controls */
                new MemoryReadAddress(0x3f86, 0x3f86, input_port_6_r), /* 4P controls */
                new MemoryReadAddress(0x3f87, 0x3f87, input_port_0_r), /* DSW #1 */
                new MemoryReadAddress(0x3f88, 0x3f88, watchdog_reset_r), /* watchdog reset */
                new MemoryReadAddress(0x2000, 0x5fff, K052109_051960_r), /* video RAM + sprite RAM */
                new MemoryReadAddress(0x6000, 0x7fff, MRA_BANK2), /* banked ROM */
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM), /* ROM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress crimfght_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_BANK1), /* banked RAM */
                new MemoryWriteAddress(0x0400, 0x1fff, MWA_RAM), /* RAM */
                new MemoryWriteAddress(0x3f88, 0x3f88, crimfght_coin_w), /* coin counters */
                new MemoryWriteAddress(0x3f8c, 0x3f8c, crimfght_sh_irqtrigger_w), /* cause interrupt on audio CPU? */
                new MemoryWriteAddress(0x2000, 0x5fff, K052109_051960_w), /* video RAM + sprite RAM */
                new MemoryWriteAddress(0x6000, 0x7fff, MWA_ROM), /* banked ROM */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM), /* ROM */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress crimfght_readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM), /* ROM 821l01.h4 */
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0xa001, 0xa001, YM2151_status_port_0_r), /* YM2151 */
                new MemoryReadAddress(0xc000, 0xc000, soundlatch_r), /* soundlatch_r */
                new MemoryReadAddress(0xe000, 0xe00d, K007232_read_port_0_r), /* 007232 registers */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress crimfght_writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM), /* ROM 821l01.h4 */
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM), /* RAM */
                new MemoryWriteAddress(0xa000, 0xa000, YM2151_register_port_0_w), /* YM2151 */
                new MemoryWriteAddress(0xa001, 0xa001, YM2151_data_port_0_w), /* YM2151 */
                new MemoryWriteAddress(0xe000, 0xe00d, K007232_write_port_0_w), /* 007232 registers */
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * Input Ports
     *
     **************************************************************************
     */
    static InputPortHandlerPtr input_ports_crimfght = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW #1 */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "1 Coin/99 Credits");
            /*	PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x20, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x50, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "3C_2C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "4C_3C") );
		PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "3C_4C") );
		PORT_DIPSETTING(    0x70, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x60, DEF_STR( "2C_5C") );
		PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") );
		PORT_DIPSETTING(    0x90, DEF_STR( "1C_7C") );
		PORT_DIPSETTING(    0x00, "Invalid" );*/

            PORT_START();
            /* DSW #2 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
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
            /* DSW #3 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* PLAYER 1 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PLAYER 2 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PLAYER 3 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PLAYER 4 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* COINSW */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_SERVICE2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_SERVICE3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_SERVICE4);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_crimfgtj = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW #1 */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            //	PORT_DIPSETTING(    0x00, "Invalid" );

            PORT_START();
            /* DSW #2 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
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
            /* DSW #3 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* PLAYER 1 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* PLAYER 2 INPUTS */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* PLAYER 3 INPUTS */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* PLAYER 4 INPUTS */
            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* COINSW */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /**
     * *************************************************************************
     *
     * Machine Driver
     *
     **************************************************************************
     */
    static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz */
            new int[]{YM3012_VOL(50, MIXER_PAN_LEFT, 50, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null},
            new WriteHandlerPtr[]{crimfght_snd_bankswitch_w}
    );
    public static portwritehandlerPtr volume_callback = new portwritehandlerPtr() {
        public void handler(int v) {
            K007232_set_volume(0, 0, (v & 0x0f) * 0x11, 0);
            K007232_set_volume(0, 1, 0, (v >> 4) * 0x11);
        }
    };

    static K007232_interface k007232_interface = new K007232_interface(
            1, /* number of chips */
            new int[]{REGION_SOUND1}, /* memory regions */
            new int[]{K007232_VOL(20, MIXER_PAN_CENTER, 20, MIXER_PAN_CENTER)}, /* volume */
            new portwritehandlerPtr[]{volume_callback} /* external port callback */
    );

    static MachineDriver machine_driver_crimfght = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_KONAMI,
                        3000000, /* ? */
                        crimfght_readmem, crimfght_writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545,
                        crimfght_readmem_sound, crimfght_writemem_sound, null, null,
                        ignore_interrupt, 0 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            crimfght_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(13 * 8, (64 - 13) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            512, 512,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            crimfght_vh_start,
            crimfght_vh_stop,
            crimfght_vh_screenrefresh,
            /* sound hardware */
            SOUND_SUPPORTS_STEREO, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K007232,
                        k007232_interface
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
    static RomLoadHandlerPtr rom_crimfght = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x28000, REGION_CPU1);/* code + banked roms */
            ROM_LOAD("821l02.f24", 0x10000, 0x18000, 0x588e7da6);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */
            ROM_LOAD("821l01.h4", 0x0000, 0x8000, 0x0faca89e);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k06.k13", 0x000000, 0x040000, 0xa1eadb24);/* characters */
            ROM_LOAD("821k07.k19", 0x040000, 0x040000, 0x060019fa);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k04.k2", 0x000000, 0x080000, 0x00e0291b);/* sprites */
            ROM_LOAD("821k05.k8", 0x080000, 0x080000, 0xe09ea05d);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("821a08.i15", 0x0000, 0x0100, 0x7da55800);/* priority encoder (not used) */

            ROM_REGION(0x40000, REGION_SOUND1);/* data for the 007232 */
            ROM_LOAD("821k03.e5", 0x00000, 0x40000, 0xfef8505a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_crimfgtj = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x28000, REGION_CPU1);/* code + banked roms */
            ROM_LOAD("821p02.bin", 0x10000, 0x18000, 0xf33fa2e1);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */
            ROM_LOAD("821l01.h4", 0x0000, 0x8000, 0x0faca89e);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k06.k13", 0x000000, 0x040000, 0xa1eadb24);/* characters */
            ROM_LOAD("821k07.k19", 0x040000, 0x040000, 0x060019fa);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k04.k2", 0x000000, 0x080000, 0x00e0291b);/* sprites */
            ROM_LOAD("821k05.k8", 0x080000, 0x080000, 0xe09ea05d);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("821a08.i15", 0x0000, 0x0100, 0x7da55800);/* priority encoder (not used) */

            ROM_REGION(0x40000, REGION_SOUND1);/* data for the 007232 */
            ROM_LOAD("821k03.e5", 0x00000, 0x40000, 0xfef8505a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_crimfgt2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x28000, REGION_CPU1);/* code + banked roms */
            ROM_LOAD("crimefb.r02", 0x10000, 0x18000, 0x4ecdd923);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */
            ROM_LOAD("821l01.h4", 0x0000, 0x8000, 0x0faca89e);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k06.k13", 0x000000, 0x040000, 0xa1eadb24);/* characters */
            ROM_LOAD("821k07.k19", 0x040000, 0x040000, 0x060019fa);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */
            ROM_LOAD("821k04.k2", 0x000000, 0x080000, 0x00e0291b);/* sprites */
            ROM_LOAD("821k05.k8", 0x080000, 0x080000, 0xe09ea05d);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("821a08.i15", 0x0000, 0x0100, 0x7da55800);/* priority encoder (not used) */

            ROM_REGION(0x40000, REGION_SOUND1);/* data for the 007232 */
            ROM_LOAD("821k03.e5", 0x00000, 0x40000, 0xfef8505a);
            ROM_END();
        }
    };

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    public static InitDriverHandlerPtr init_crimfght = new InitDriverHandlerPtr() {
        public void handler() {
            konami_rom_deinterleave_2(REGION_GFX1);
            konami_rom_deinterleave_2(REGION_GFX2);
        }
    };

    public static GameDriver driver_crimfght = new GameDriver("1989", "crimfght", "crimfght.java", rom_crimfght, null, machine_driver_crimfght, input_ports_crimfght, init_crimfght, ROT0, "Konami", "Crime Fighters (US 4 players)");
    public static GameDriver driver_crimfgt2 = new GameDriver("1989", "crimfgt2", "crimfght.java", rom_crimfgt2, driver_crimfght, machine_driver_crimfght, input_ports_crimfgtj, init_crimfght, ROT0, "Konami", "Crime Fighters (World 2 Players)");
    public static GameDriver driver_crimfgtj = new GameDriver("1989", "crimfgtj", "crimfght.java", rom_crimfgtj, driver_crimfght, machine_driver_crimfght, input_ports_crimfgtj, init_crimfght, ROT0, "Konami", "Crime Fighters (Japan 2 Players)");
}
