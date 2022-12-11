/**
 * *************************************************************************
 *
 * POW - Prisoners Of War (US)	A7008	SNK 1988 POW - Prisoners Of War (Japan)
 * A7008	SNK 1988 SAR - Search And Rescue	(World)	A8007	SNK 1989 SAR - Search
 * And Rescue	(US)	A8007	SNK 1989 Street Smart (US version 1)	A8007	SNK 1989
 * Street Smart (US version 2)	A7008	SNK 1989 Street Smart (Japan version 1)
 * A8007	SNK 1989 Ikari III	- The Rescue (US)	A7007	SNK 1989
 *
 * For some strange reason version 2 of Street Smart runs on Pow hardware!
 *
 * Emulation by Bryan McPhail, mish@tendril.co.uk
 *
 * To Do:
 *
 * - Fix Ikari III intro. Priority issue.
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.vidhrdw.snk68.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3526intf.*;
import static arcadeflex.v036.sound.upd7759.*;
import static arcadeflex.v036.sound.upd7759H.*;
import static arcadeflex.v036.mame.sndintrfH.*;

public class snk68 {

    /**
     * ***************************************************************************
     */
    public static ReadHandlerPtr sound_cpu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0x0100;
        }
    };

    public static ReadHandlerPtr pow_video_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return videoram.READ_WORD(offset);
        }
    };

    public static WriteHandlerPtr pow_spriteram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* DWORD aligned bytes should be $ff */
            if ((offset & 0x02) != 0) {
                spriteram.WRITE_WORD(offset, data);
            } else {
                spriteram.WRITE_WORD(offset, data | 0xff00);
            }
        }
    };

    public static ReadHandlerPtr pow_spriteram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram.READ_WORD(offset);
        }
    };

    public static ReadHandlerPtr control_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(0) + (readinputport(1) << 8));
        }
    };

    public static ReadHandlerPtr control_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(2);
        }
    };

    public static ReadHandlerPtr dip_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(3) << 8);
        }
    };

    public static ReadHandlerPtr dip_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (readinputport(4) << 8);
        }
    };

    public static ReadHandlerPtr rotary_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ((~(1 << (readinputport(5) * 12 / 256))) << 8) & 0xff00;
        }
    };

    public static ReadHandlerPtr rotary_2_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ((~(1 << (readinputport(6) * 12 / 256))) << 8) & 0xff00;
        }
    };

    public static ReadHandlerPtr rotary_lsb_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (((~(1 << (readinputport(6) * 12 / 256))) << 4) & 0xf000)
                    + (((~(1 << (readinputport(5) * 12 / 256)))) & 0x0f00);
        }
    };

    static int invert_controls;

    public static ReadHandlerPtr protcontrols_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return readinputport(offset / 2) ^ invert_controls;
        }
    };

    public static WriteHandlerPtr protection_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* top byte is used, meaning unknown */
            /* bottom byte is protection in ikari 3 and streetsm */
            if ((data & 0x00ff0000) == 0) {
                invert_controls = ((data & 0xff) == 0x07) ? 0xff : 0x00;
            }
        }
    };

    public static WriteHandlerPtr sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, (data >> 8) & 0xff);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    /**
     * ****************************************************************************
     */
    static MemoryReadAddress pow_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x040000, 0x043fff, MRA_BANK1),
                new MemoryReadAddress(0x080000, 0x080001, control_1_r),
                new MemoryReadAddress(0x0c0000, 0x0c0001, control_2_r),
                new MemoryReadAddress(0x0e0000, 0x0e0001, MRA_NOP), /* Watchdog or IRQ ack */
                new MemoryReadAddress(0x0e8000, 0x0e8001, MRA_NOP), /* Watchdog or IRQ ack */
                new MemoryReadAddress(0x0f0000, 0x0f0001, dip_1_r),
                new MemoryReadAddress(0x0f0008, 0x0f0009, dip_2_r),
                new MemoryReadAddress(0x100000, 0x100fff, pow_video_r),
                new MemoryReadAddress(0x200000, 0x207fff, pow_spriteram_r),
                new MemoryReadAddress(0x400000, 0x400fff, paletteram_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress pow_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x040000, 0x043fff, MWA_BANK1),
                new MemoryWriteAddress(0x080000, 0x080001, sound_w),
                new MemoryWriteAddress(0x0c0000, 0x0c0001, pow_flipscreen_w),
                new MemoryWriteAddress(0x0f0008, 0x0f0009, MWA_NOP),
                new MemoryWriteAddress(0x100000, 0x100fff, pow_video_w, videoram),
                new MemoryWriteAddress(0x200000, 0x207fff, pow_spriteram_w, spriteram),
                new MemoryWriteAddress(0x400000, 0x400fff, pow_paletteram_w, paletteram),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress searchar_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x040000, 0x043fff, MRA_BANK1),
                new MemoryReadAddress(0x080000, 0x080005, protcontrols_r), /* Player 1  2 */
                new MemoryReadAddress(0x0c0000, 0x0c0001, rotary_1_r), /* Player 1 rotary */
                new MemoryReadAddress(0x0c8000, 0x0c8001, rotary_2_r), /* Player 2 rotary */
                new MemoryReadAddress(0x0d0000, 0x0d0001, rotary_lsb_r), /* Extra rotary bits */
                new MemoryReadAddress(0x0e0000, 0x0e0001, MRA_NOP), /* Watchdog or IRQ ack */
                new MemoryReadAddress(0x0e8000, 0x0e8001, MRA_NOP), /* Watchdog or IRQ ack */
                new MemoryReadAddress(0x0f0000, 0x0f0001, dip_1_r),
                new MemoryReadAddress(0x0f0008, 0x0f0009, dip_2_r),
                new MemoryReadAddress(0x0f8000, 0x0f8001, sound_cpu_r),
                new MemoryReadAddress(0x100000, 0x107fff, pow_spriteram_r),
                new MemoryReadAddress(0x200000, 0x200fff, pow_video_r),
                new MemoryReadAddress(0x300000, 0x33ffff, MRA_BANK8), /* Extra code bank */
                new MemoryReadAddress(0x400000, 0x400fff, paletteram_word_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress searchar_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x040000, 0x043fff, MWA_BANK1),
                new MemoryWriteAddress(0x080000, 0x080001, sound_w),
                new MemoryWriteAddress(0x080006, 0x080007, protection_w), /* top byte unknown, bottom is protection in ikari3 and streetsm */
                new MemoryWriteAddress(0x0c0000, 0x0c0001, pow_flipscreen_w),
                new MemoryWriteAddress(0x0f0000, 0x0f0001, MWA_NOP),
                new MemoryWriteAddress(0x100000, 0x107fff, pow_spriteram_w, spriteram),
                new MemoryWriteAddress(0x200000, 0x200fff, pow_video_w, videoram),
                new MemoryWriteAddress(0x201000, 0x201fff, pow_video_w), /* Mirror used by Ikari 3 */
                new MemoryWriteAddress(0x400000, 0x400fff, pow_paletteram_w, paletteram),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xefff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xf800, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xefff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    public static WriteHandlerPtr D7759_write_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UPD7759_reset_w.handler(0, 0);
            UPD7759_message_w.handler(offset, data);
            UPD7759_start_w.handler(0, 0);
        }
    };

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, YM3812_status_port_0_r),
                new IOReadPort(-1)
            };

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM3812_control_port_0_w),
                new IOWritePort(0x20, 0x20, YM3812_write_port_0_w),
                new IOWritePort(0x40, 0x40, D7759_write_port_0_w),
                new IOWritePort(0x80, 0x80, MWA_NOP), /* IRQ ack? */
                new IOWritePort(-1)
            };

    /**
     * ***************************************************************************
     */
    static InputPortHandlerPtr input_ports_pow = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switch bank 1, all active high */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0c, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x10, "3");
            PORT_DIPNAME(0x20, 0x00, "Bonus Occurence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x20, "1st & every 2nd");
            PORT_DIPNAME(0x40, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "Japanese");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Dip switch bank 2, all active high */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20k 50k");
            PORT_DIPSETTING(0x08, "40k 100k");
            PORT_DIPSETTING(0x04, "60k 150k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");
            INPUT_PORTS_END();
        }
    };

    /* Identical to pow, but the Language dip switch has no effect */
    static InputPortHandlerPtr input_ports_powj = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switch bank 1, all active high */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x0c, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x10, "3");
            PORT_DIPNAME(0x20, 0x00, "Bonus Occurence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x20, "1st & every 2nd");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Dip switch bank 2, all active high */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20k 50k");
            PORT_DIPSETTING(0x08, "40k 100k");
            PORT_DIPSETTING(0x04, "60k 150k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_searchar = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* coin */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switches (Active high) */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x0c, "5");
            PORT_DIPNAME(0x30, 0x00, "Coin A & B");
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x30, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x40, 0x00, "Bonus Occurrence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x40, "1st & every 2nd");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* Dip switches (Active high) */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "50k 200k");
            PORT_DIPSETTING(0x08, "70k 270k");
            PORT_DIPSETTING(0x04, "90k 350k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");

            PORT_START(); 	/* player 1 12-way rotary control - converted in controls_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE, 25, 10, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0);

            PORT_START(); 	/* player 2 12-way rotary control - converted in controls_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE | IPF_PLAYER2, 25, 10, 0, 0, KEYCODE_N, KEYCODE_M, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_streetsm = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* coin */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switches (Active high) */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x03, "4");
            PORT_DIPNAME(0x0c, 0x00, "Coin A & B");
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, "Bonus Occurrence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x20, "1st & every 2nd");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* Dip switches (Active high) */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "200k 400k");
            PORT_DIPSETTING(0x08, "400k 600k");
            PORT_DIPSETTING(0x04, "600k 800k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");

            PORT_START(); 	/* player 1 12-way rotary control - not used in this game */

            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* player 2 12-way rotary control - not used in this game */

            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    /* Same as streetsm, but Coinage is different */
    static InputPortHandlerPtr input_ports_streetsj = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* coin */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switches (Active high) */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x03, "4");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x0c, "A 4/1 B 1/4");
            PORT_DIPSETTING(0x04, "A 3/1 B 1/3");
            PORT_DIPSETTING(0x08, "A 2/1 B 1/2");
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, "Bonus Occurrence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x20, "1st & every 2nd");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* Dip switches (Active high) */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "200k 400k");
            PORT_DIPSETTING(0x08, "400k 600k");
            PORT_DIPSETTING(0x04, "600k 800k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");

            PORT_START(); 	/* player 1 12-way rotary control - not used in this game */

            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* player 2 12-way rotary control - not used in this game */

            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_ikari3 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START(); 	/* Player 1 controls, maybe all are active_high? */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* Player 2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* coin */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_SERVICE1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_SERVICE);/* same as the service mode dsw */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* Dip switches (Active high) */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x0c, 0x00, "Coin A & B");
            PORT_DIPSETTING(0x08, "First 2 Coins/1 Credit then 1/1");
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, "First 1 Coin/2 Credits then 1/1");
            PORT_DIPSETTING(0x0c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, "Bonus Occurrence");
            PORT_DIPSETTING(0x00, "1st & 2nd only");
            PORT_DIPSETTING(0x20, "1st & every 2nd");
            PORT_DIPNAME(0x40, 0x00, "Blood");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();  /* Dip switches (Active high) */

            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x02, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20k 50k");
            PORT_DIPSETTING(0x08, "40k 100k");
            PORT_DIPSETTING(0x04, "60k 150k");
            PORT_DIPSETTING(0x0c, "None");
            PORT_DIPNAME(0x30, 0x00, "Game Mode");
            PORT_DIPSETTING(0x20, "Demo Sounds Off");
            PORT_DIPSETTING(0x00, "Demo Sounds On");
            PORT_DIPSETTING(0x30, "Freeze");
            PORT_BITX(0, 0x10, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x80, "Normal");
            PORT_DIPSETTING(0x40, "Hard");
            PORT_DIPSETTING(0xc0, "Hardest");

            PORT_START(); 	/* player 1 12-way rotary control - converted in controls_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE, 25, 10, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0);

            PORT_START(); 	/* player 2 12-way rotary control - converted in controls_r() */

            PORT_ANALOGX(0xff, 0x00, IPT_DIAL | IPF_REVERSE | IPF_PLAYER2, 25, 10, 0, 0, KEYCODE_N, KEYCODE_M, 0, 0);
            INPUT_PORTS_END();
        }
    };

    /**
     * ***************************************************************************
     */
    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 chars */
            2048,
            4, /* 4 bits per pixel  */
            new int[]{0, 4, 0x8000 * 8, (0x8000 * 8) + 4},
            new int[]{8 * 8 + 3, 8 * 8 + 2, 8 * 8 + 1, 8 * 8 + 0, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout pow_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            4096 * 4,
            4, /* 4 bits per pixel */
            new int[]{0, 0x80000 * 8, 0x100000 * 8, 0x180000 * 8},
            new int[]{16 * 8 + 7, 16 * 8 + 6, 16 * 8 + 5, 16 * 8 + 4, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                7, 6, 5, 4, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            8 * 32 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout searchar_spritelayout = new GfxLayout(
            16, 16,
            0x6000,
            4,
            new int[]{0, 8, 0x180000 * 8, 0x180000 * 8 + 8},
            new int[]{32 * 8 + 7, 32 * 8 + 6, 32 * 8 + 5, 32 * 8 + 4, 32 * 8 + 3, 32 * 8 + 2, 32 * 8 + 1, 32 * 8 + 0,
                7, 6, 5, 4, 3, 2, 1, 0
            },
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16
            },
            64 * 8
    );

    static GfxLayout ikari3_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            4096 * 5,
            4, /* 4 bits per pixel */
            new int[]{0x140000 * 8, 0, 0xa0000 * 8, 0x1e0000 * 8},
            new int[]{16 * 8 + 7, 16 * 8 + 6, 16 * 8 + 5, 16 * 8 + 4, 16 * 8 + 3, 16 * 8 + 2, 16 * 8 + 1, 16 * 8 + 0,
                7, 6, 5, 4, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            8 * 32 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo pow_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 128),
                new GfxDecodeInfo(REGION_GFX2, 0, pow_spritelayout, 0, 128),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo searchar_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, searchar_spritelayout, 0, 128),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo ikari3_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, ikari3_spritelayout, 0, 128),
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ***************************************************************************
     */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_irq_line(1, 0, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    static YM3812interface ym3812_interface = new YM3812interface(
            1, /* 1 chip */
            4000000, /* 4 MHz - accurate for POW, should be accurate for others */
            new int[]{50},
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static UPD7759_interface upd7759_interface = new UPD7759_interface(
            1, /* number of chips */
            UPD7759_STANDARD_CLOCK,
            new int[]{50}, /* volume */
            new int[]{REGION_SOUND1}, /* memory region */
            UPD7759_STANDALONE_MODE, /* chip mode */
            new irqcallbackPtr[]{null}
    );

    /**
     * ***************************************************************************
     */
    static MachineDriver machine_driver_ikari3 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* Accurate */
                        searchar_readmem, searchar_writemem, null, null,
                        m68_level1_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* Accurate */
                        sound_readmem, sound_writemem,
                        sound_readport, sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            ikari3_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            pow_vh_start,
            null,
            searchar_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );

    static MachineDriver machine_driver_pow = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* Accurate */
                        pow_readmem, pow_writemem, null, null,
                        m68_level1_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* Accurate */
                        sound_readmem, sound_writemem,
                        sound_readport, sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            pow_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            pow_vh_start,
            null,
            pow_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );

    static MachineDriver machine_driver_searchar = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        12000000,
                        searchar_readmem, searchar_writemem, null, null,
                        m68_level1_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000,
                        sound_readmem, sound_writemem,
                        sound_readport, sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            searchar_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            pow_vh_start,
            null,
            searchar_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );

    static MachineDriver machine_driver_streetsm = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* Accurate */
                        pow_readmem, pow_writemem, null, null,
                        m68_level1_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* Accurate */
                        sound_readmem, sound_writemem,
                        sound_readport, sound_writeport,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            searchar_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            pow_vh_start,
            null,
            searchar_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            }
    );

    /**
     * ***************************************************************************
     */
    static RomLoadHandlerPtr rom_pow = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("dg1", 0x000000, 0x20000, 0x8e71a8af);
            ROM_LOAD_ODD("dg2", 0x000000, 0x20000, 0x4287affc);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("dg8", 0x000000, 0x10000, 0xd1d61da3);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dg9", 0x000000, 0x08000, 0xdf864a08);
            ROM_LOAD("dg10", 0x008000, 0x08000, 0x9e470d53);

            ROM_REGION(0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("snk880.11a", 0x000000, 0x20000, 0xe70fd906);
            ROM_LOAD("snk880.12a", 0x020000, 0x20000, 0x628b1aed);
            ROM_LOAD("snk880.13a", 0x040000, 0x20000, 0x19dc8868);
            ROM_LOAD("snk880.14a", 0x060000, 0x20000, 0x47cd498b);
            ROM_LOAD("snk880.15a", 0x080000, 0x20000, 0x7a90e957);
            ROM_LOAD("snk880.16a", 0x0a0000, 0x20000, 0xe40a6c13);
            ROM_LOAD("snk880.17a", 0x0c0000, 0x20000, 0xc7931cc2);
            ROM_LOAD("snk880.18a", 0x0e0000, 0x20000, 0xeed72232);
            ROM_LOAD("snk880.19a", 0x100000, 0x20000, 0x1775b8dd);
            ROM_LOAD("snk880.20a", 0x120000, 0x20000, 0xf8e752ec);
            ROM_LOAD("snk880.21a", 0x140000, 0x20000, 0x27e9fffe);
            ROM_LOAD("snk880.22a", 0x160000, 0x20000, 0xaa9c00d8);
            ROM_LOAD("snk880.23a", 0x180000, 0x20000, 0xadb6ad68);
            ROM_LOAD("snk880.24a", 0x1a0000, 0x20000, 0xdd41865a);
            ROM_LOAD("snk880.25a", 0x1c0000, 0x20000, 0x055759ad);
            ROM_LOAD("snk880.26a", 0x1e0000, 0x20000, 0x9bc261c5);

            ROM_REGION(0x10000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("dg7", 0x000000, 0x10000, 0xaba9a9d3);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_powj = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("1-2", 0x000000, 0x20000, 0x2f17bfb0);
            ROM_LOAD_ODD("2-2", 0x000000, 0x20000, 0xbaa32354);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("dg8", 0x000000, 0x10000, 0xd1d61da3);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("dg9", 0x000000, 0x08000, 0xdf864a08);
            ROM_LOAD("dg10", 0x008000, 0x08000, 0x9e470d53);

            ROM_REGION(0x200000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("snk880.11a", 0x000000, 0x20000, 0xe70fd906);
            ROM_LOAD("snk880.12a", 0x020000, 0x20000, 0x628b1aed);
            ROM_LOAD("snk880.13a", 0x040000, 0x20000, 0x19dc8868);
            ROM_LOAD("snk880.14a", 0x060000, 0x20000, 0x47cd498b);
            ROM_LOAD("snk880.15a", 0x080000, 0x20000, 0x7a90e957);
            ROM_LOAD("snk880.16a", 0x0a0000, 0x20000, 0xe40a6c13);
            ROM_LOAD("snk880.17a", 0x0c0000, 0x20000, 0xc7931cc2);
            ROM_LOAD("snk880.18a", 0x0e0000, 0x20000, 0xeed72232);
            ROM_LOAD("snk880.19a", 0x100000, 0x20000, 0x1775b8dd);
            ROM_LOAD("snk880.20a", 0x120000, 0x20000, 0xf8e752ec);
            ROM_LOAD("snk880.21a", 0x140000, 0x20000, 0x27e9fffe);
            ROM_LOAD("snk880.22a", 0x160000, 0x20000, 0xaa9c00d8);
            ROM_LOAD("snk880.23a", 0x180000, 0x20000, 0xadb6ad68);
            ROM_LOAD("snk880.24a", 0x1a0000, 0x20000, 0xdd41865a);
            ROM_LOAD("snk880.25a", 0x1c0000, 0x20000, 0x055759ad);
            ROM_LOAD("snk880.26a", 0x1e0000, 0x20000, 0x9bc261c5);

            ROM_REGION(0x10000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("dg7", 0x000000, 0x10000, 0xaba9a9d3);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_searchar = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("bhw.2", 0x000000, 0x20000, 0xe1430138);
            ROM_LOAD_ODD("bhw.3", 0x000000, 0x20000, 0xee1f9374);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("bh.5", 0x000000, 0x10000, 0x53e2fa76);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("bh.7", 0x000000, 0x08000, 0xb0f1b049);
            ROM_LOAD("bh.8", 0x008000, 0x08000, 0x174ddba7);

            ROM_REGION(0x300000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("bh.c1", 0x000000, 0x80000, 0x1fb8f0ae);
            ROM_LOAD("bh.c3", 0x080000, 0x80000, 0xfd8bc407);
            ROM_LOAD("bh.c5", 0x100000, 0x80000, 0x1d30acc3);
            ROM_LOAD("bh.c2", 0x180000, 0x80000, 0x7c803767);
            ROM_LOAD("bh.c4", 0x200000, 0x80000, 0xeede7c43);
            ROM_LOAD("bh.c6", 0x280000, 0x80000, 0x9f785cd9);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("bh.v1", 0x000000, 0x20000, 0x07a6114b);

            ROM_REGION(0x40000, REGION_USER1);/* Extra code bank */

            ROM_LOAD_EVEN("bhw.1", 0x000000, 0x20000, 0x62b60066);
            ROM_LOAD_ODD("bhw.4", 0x000000, 0x20000, 0x16d8525c);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sercharu = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("bh.2", 0x000000, 0x20000, 0xc852e2e2);
            ROM_LOAD_ODD("bh.3", 0x000000, 0x20000, 0xbc04a4a1);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("bh.5", 0x000000, 0x10000, 0x53e2fa76);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("bh.7", 0x000000, 0x08000, 0xb0f1b049);
            ROM_LOAD("bh.8", 0x008000, 0x08000, 0x174ddba7);

            ROM_REGION(0x300000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("bh.c1", 0x000000, 0x80000, 0x1fb8f0ae);
            ROM_LOAD("bh.c3", 0x080000, 0x80000, 0xfd8bc407);
            ROM_LOAD("bh.c5", 0x100000, 0x80000, 0x1d30acc3);
            ROM_LOAD("bh.c2", 0x180000, 0x80000, 0x7c803767);
            ROM_LOAD("bh.c4", 0x200000, 0x80000, 0xeede7c43);
            ROM_LOAD("bh.c6", 0x280000, 0x80000, 0x9f785cd9);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("bh.v1", 0x000000, 0x20000, 0x07a6114b);

            ROM_REGION(0x40000, REGION_USER1);/* Extra code bank */

            ROM_LOAD_EVEN("bh.1", 0x000000, 0x20000, 0xba9ca70b);
            ROM_LOAD_ODD("bh.4", 0x000000, 0x20000, 0xeabc5ddf);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_streetsm = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("s2-1ver2.14h", 0x00000, 0x20000, 0x655f4773);
            ROM_LOAD_ODD("s2-2ver2.14k", 0x00000, 0x20000, 0xefae4823);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("s2-5.16c", 0x000000, 0x10000, 0xca4b171e);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("s2-9.25l", 0x000000, 0x08000, 0x09b6ac67);
            ROM_LOAD("s2-10.25m", 0x008000, 0x08000, 0x89e4ee6f);

            ROM_REGION(0x300000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("stsmart.900", 0x000000, 0x80000, 0xa8279a7e);
            ROM_LOAD("stsmart.902", 0x080000, 0x80000, 0x2f021aa1);
            ROM_LOAD("stsmart.904", 0x100000, 0x80000, 0x167346f7);
            ROM_LOAD("stsmart.901", 0x180000, 0x80000, 0xc305af12);
            ROM_LOAD("stsmart.903", 0x200000, 0x80000, 0x73c16d35);
            ROM_LOAD("stsmart.905", 0x280000, 0x80000, 0xa5beb4e2);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("s2-6.18d", 0x000000, 0x20000, 0x47db1605);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_streets1 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("s2-1ver1.9c", 0x00000, 0x20000, 0xb59354c5);
            ROM_LOAD_ODD("s2-2ver1.10c", 0x00000, 0x20000, 0xe448b68b);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("s2-5.16c", 0x000000, 0x10000, 0xca4b171e);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("s2-7.15l", 0x000000, 0x08000, 0x22bedfe5);
            ROM_LOAD("s2-8.15m", 0x008000, 0x08000, 0x6a1c70ab);

            ROM_REGION(0x300000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("stsmart.900", 0x000000, 0x80000, 0xa8279a7e);
            ROM_LOAD("stsmart.902", 0x080000, 0x80000, 0x2f021aa1);
            ROM_LOAD("stsmart.904", 0x100000, 0x80000, 0x167346f7);
            ROM_LOAD("stsmart.901", 0x180000, 0x80000, 0xc305af12);
            ROM_LOAD("stsmart.903", 0x200000, 0x80000, 0x73c16d35);
            ROM_LOAD("stsmart.905", 0x280000, 0x80000, 0xa5beb4e2);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("s2-6.18d", 0x000000, 0x20000, 0x47db1605);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_streetsj = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("s2v1j_01.bin", 0x00000, 0x20000, 0xf031413c);
            ROM_LOAD_ODD("s2v1j_02.bin", 0x00000, 0x20000, 0xe403a40b);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("s2-5.16c", 0x000000, 0x10000, 0xca4b171e);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("s2-7.15l", 0x000000, 0x08000, 0x22bedfe5);
            ROM_LOAD("s2-8.15m", 0x008000, 0x08000, 0x6a1c70ab);

            ROM_REGION(0x300000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("stsmart.900", 0x000000, 0x80000, 0xa8279a7e);
            ROM_LOAD("stsmart.902", 0x080000, 0x80000, 0x2f021aa1);
            ROM_LOAD("stsmart.904", 0x100000, 0x80000, 0x167346f7);
            ROM_LOAD("stsmart.901", 0x180000, 0x80000, 0xc305af12);
            ROM_LOAD("stsmart.903", 0x200000, 0x80000, 0x73c16d35);
            ROM_LOAD("stsmart.905", 0x280000, 0x80000, 0xa5beb4e2);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("s2-6.18d", 0x000000, 0x20000, 0x47db1605);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ikari3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);
            ROM_LOAD_EVEN("ik3-2.bin", 0x000000, 0x20000, 0xa7b34dcd);
            ROM_LOAD_ODD("ik3-3.bin", 0x000000, 0x20000, 0x50f2b83d);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */

            ROM_LOAD("ik3-5.bin", 0x000000, 0x10000, 0xce6706fc);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* characters */

            ROM_LOAD("ik3-7.bin", 0x000000, 0x08000, 0x0b4804df);
            ROM_LOAD("ik3-8.bin", 0x008000, 0x08000, 0x10ab4e50);

            ROM_REGION(0x280000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("ik3-13.bin", 0x000000, 0x20000, 0x9a56bd32);
            ROM_LOAD("ik3-12.bin", 0x020000, 0x20000, 0x0ce6a10a);
            ROM_LOAD("ik3-11.bin", 0x040000, 0x20000, 0xe4e2be43);
            ROM_LOAD("ik3-10.bin", 0x060000, 0x20000, 0xac222372);
            ROM_LOAD("ik3-9.bin", 0x080000, 0x20000, 0xc33971c2);
            ROM_LOAD("ik3-14.bin", 0x0a0000, 0x20000, 0x453bea77);
            ROM_LOAD("ik3-15.bin", 0x0c0000, 0x20000, 0x781a81fc);
            ROM_LOAD("ik3-16.bin", 0x0e0000, 0x20000, 0x80ba400b);
            ROM_LOAD("ik3-17.bin", 0x100000, 0x20000, 0x0cc3ce4a);
            ROM_LOAD("ik3-18.bin", 0x120000, 0x20000, 0xba106245);
            ROM_LOAD("ik3-23.bin", 0x140000, 0x20000, 0xd0fd5c77);
            ROM_LOAD("ik3-22.bin", 0x160000, 0x20000, 0x4878d883);
            ROM_LOAD("ik3-21.bin", 0x180000, 0x20000, 0x50d0fbf0);
            ROM_LOAD("ik3-20.bin", 0x1a0000, 0x20000, 0x9a851efc);
            ROM_LOAD("ik3-19.bin", 0x1c0000, 0x20000, 0x4ebdba89);
            ROM_LOAD("ik3-24.bin", 0x1e0000, 0x20000, 0xe9b26d68);
            ROM_LOAD("ik3-25.bin", 0x200000, 0x20000, 0x073b03f1);
            ROM_LOAD("ik3-26.bin", 0x220000, 0x20000, 0x9c613561);
            ROM_LOAD("ik3-27.bin", 0x240000, 0x20000, 0x16dd227e);
            ROM_LOAD("ik3-28.bin", 0x260000, 0x20000, 0x711715ae);

            ROM_REGION(0x20000, REGION_SOUND1);/* UPD7759 samples */

            ROM_LOAD("ik3-6.bin", 0x000000, 0x20000, 0x59d256a4);

            ROM_REGION(0x40000, REGION_USER1);/* Extra code bank */

            ROM_LOAD_EVEN("ik3-1.bin", 0x000000, 0x10000, 0x47e4d256);
            ROM_LOAD_ODD("ik3-4.bin", 0x000000, 0x10000, 0xa43af6b5);
            ROM_END();
        }
    };

    /**
     * ***************************************************************************
     */
    public static InitDriverHandlerPtr init_searchar = new InitDriverHandlerPtr() {
        public void handler() {
            cpu_setbank(8, memory_region(REGION_USER1));
        }
    };

    /**
     * ***************************************************************************
     */
    public static GameDriver driver_pow = new GameDriver("1988", "pow", "snk68.java", rom_pow, null, machine_driver_pow, input_ports_pow, null, ROT0, "SNK", "P.O.W. - Prisoners of War (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_powj = new GameDriver("1988", "powj", "snk68.java", rom_powj, driver_pow, machine_driver_pow, input_ports_powj, null, ROT0, "SNK", "Datsugoku - Prisoners of War (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_searchar = new GameDriver("1989", "searchar", "snk68.java", rom_searchar, null, machine_driver_searchar, input_ports_searchar, init_searchar, ROT90, "SNK", "SAR - Search And Rescue (World)", GAME_NO_COCKTAIL);
    public static GameDriver driver_sercharu = new GameDriver("1989", "sercharu", "snk68.java", rom_sercharu, driver_searchar, machine_driver_searchar, input_ports_searchar, init_searchar, ROT90, "SNK", "SAR - Search And Rescue (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_streetsm = new GameDriver("1989", "streetsm", "snk68.java", rom_streetsm, null, machine_driver_streetsm, input_ports_streetsm, null, ROT0, "SNK", "Street Smart (US version 2)", GAME_NO_COCKTAIL);
    public static GameDriver driver_streets1 = new GameDriver("1989", "streets1", "snk68.java", rom_streets1, driver_streetsm, machine_driver_searchar, input_ports_streetsm, null, ROT0, "SNK", "Street Smart (US version 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_streetsj = new GameDriver("1989", "streetsj", "snk68.java", rom_streetsj, driver_streetsm, machine_driver_searchar, input_ports_streetsj, null, ROT0, "SNK", "Street Smart (Japan version 1)", GAME_NO_COCKTAIL);
    public static GameDriver driver_ikari3 = new GameDriver("1989", "ikari3", "snk68.java", rom_ikari3, null, machine_driver_ikari3, input_ports_ikari3, init_searchar, ROT0, "SNK", "Ikari III - The Rescue", GAME_NO_COCKTAIL);
}
