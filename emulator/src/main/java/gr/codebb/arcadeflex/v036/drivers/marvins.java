/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.marvins.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.namcoH.*;
import static gr.codebb.arcadeflex.v036.sound.namco.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;

public class marvins {

    public static final String CREDITS = "Phil Stroffolino\nTim Lindquist\nCarlos A. Lozano";

    /**
     * *************************************************************************
     **
     **	CPUA and CPUB communicate through shared RAM. *
	**************************************************************************
     */
    /**
     * *************************************************************************
     **
     ** Video Driver *
	**************************************************************************
     */
    /**
     * *************************************************************************
     **
     **	Sound System * *	The sound CPU is a slave, with communication. * *
     * Sound Chips: PSGX2 + "Wave Generater" * *	The Custom Wave Generator is
     * controlled by 6 bytes * *	The first pair of registers (0x8002, 0x8003)
     * appear to define frequency *	as a fraction: RAM[0x8003]/RAM[0x8002]. * *
     * (0x8004, 0x8005, 0x8006, 0x8007) are currently unmapped. Probably they *
     * control the shape of the wave being played. * *	snkwave_interface is
     * currently implemented with the "namco" sound component. *
	**************************************************************************
     */
    static int sound_cpu_busy_bit;
    static int sound_cpu_ready;
    static int sound_command;
    static int sound_fetched;

    static namco_interface snkwave_interface = new namco_interface(
            23920, /* ? (wave generator has a 8Mhz clock near it) */
            1, /* number of voices */
            8, /* playback volume */
            -1 /* memory region */
    );

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* number of chips */
            2000000, /* 2 MHz */
            new int[]{35, 35},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static void init_sound(int busy_bit) {
        sound_cpu_busy_bit = busy_bit;
        sound_cpu_ready = 1;
        sound_command = 0x00;
        sound_fetched = 1;
    }

    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (sound_fetched == 0) {
                if (errorlog != null) {
                    fprintf(errorlog, "missed sound command: %02x\n", sound_command);
                }
            }

            sound_fetched = 0;
            sound_command = data;
            sound_cpu_ready = 0;
            cpu_cause_interrupt(2, Z80_IRQ_INT);
        }
    };

    public static ReadHandlerPtr sound_command_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            sound_fetched = 1;
            return sound_command;
        }
    };

    public static ReadHandlerPtr sound_ack_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            sound_cpu_ready = 1;
            return 0xff;
        }
    };

    static MemoryReadAddress readmem_sound[] = {
        new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
        new MemoryReadAddress(0x4000, 0x4000, sound_command_r),
        new MemoryReadAddress(0xa000, 0xa000, sound_ack_r),
        new MemoryReadAddress(0xe000, 0xe7ff, MRA_RAM),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress writemem_sound[] = {
        new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM, namco_wavedata), /* silly hack - this shouldn't be here */
        new MemoryWriteAddress(0x8000, 0x8000, AY8910_control_port_0_w),
        new MemoryWriteAddress(0x8001, 0x8001, AY8910_write_port_0_w),
        new MemoryWriteAddress(0x8002, 0x8007, snkwave_w),
        new MemoryWriteAddress(0x8008, 0x8008, AY8910_control_port_1_w),
        new MemoryWriteAddress(0x8009, 0x8009, AY8910_write_port_1_w),
        new MemoryWriteAddress(0xe000, 0xe7ff, MWA_RAM),
        new MemoryWriteAddress(-1)
    };

    /* this input port has one of its bits mapped to sound CPU status */
    public static ReadHandlerPtr marvins_port_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int result = input_port_0_r.handler(0);
            if (sound_cpu_ready == 0) {
                result |= sound_cpu_busy_bit;
            }
            return result;
        }
    };

    /**
     * *************************************************************************
     **
     **	Game Specific Initialization * *	madcrash_vreg defines an offset for
     * the video registers which is *	different in Mad Crasher and Vanguard II.
     * * *	init_sound defines the location of the polled sound CPU busy bit, *
     * which also varies across games. *
	**************************************************************************
     */
    public static int madcrash_vreg;

    /**
     * *************************************************************************
     **
     **	Interrupt Handling * *	CPUA can trigger an interrupt on CPUB, and CPUB
     * can trigger an interrupt *	on CPUA. Each CPU must re-enable interrupts on
     * itself. *
	**************************************************************************
     */
    public static final int SNK_NMI_ENABLE = 1;
    public static final int SNK_NMI_PENDING = 2;
    static int CPUA_latch = 0;
    static int CPUB_latch = 0;

    public static WriteHandlerPtr CPUA_int_enable = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((CPUA_latch & SNK_NMI_PENDING) != 0) {
                cpu_cause_interrupt(0, Z80_NMI_INT);
                CPUA_latch = 0;
            } else {
                CPUA_latch |= SNK_NMI_ENABLE;
            }
        }
    };

    public static ReadHandlerPtr CPUA_int_trigger = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((CPUA_latch & SNK_NMI_ENABLE) != 0) {
                cpu_cause_interrupt(0, Z80_NMI_INT);
                CPUA_latch = 0;
            } else {
                CPUA_latch |= SNK_NMI_PENDING;
            }
            return 0xff;
        }
    };

    public static WriteHandlerPtr CPUB_int_enable = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((CPUB_latch & SNK_NMI_PENDING) != 0) {
                cpu_cause_interrupt(1, Z80_NMI_INT);
                CPUB_latch = 0;
            } else {
                CPUB_latch |= SNK_NMI_ENABLE;
            }
        }
    };

    public static ReadHandlerPtr CPUB_int_trigger = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((CPUB_latch & SNK_NMI_ENABLE) != 0) {
                cpu_cause_interrupt(1, Z80_NMI_INT);
                CPUB_latch = 0;
            } else {
                CPUB_latch |= SNK_NMI_PENDING;
            }
            return 0xff;
        }
    };

    /**
     * *************************************************************************
     **
     **	Memory Maps for CPUA, CPUB * *	Shared RAM is shuffled in Mad
     * Crasher/Vanguard II compared to *	Marvin's Maze. * *	A few ports are
     * mapped differently for each game. *
	**************************************************************************
     */
    static MemoryReadAddress readmem_CPUA[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x8000, marvins_port_0_r), /* coin input, start, sound CPU status */
                new MemoryReadAddress(0x8100, 0x8100, input_port_1_r), /* player #1 controls */
                new MemoryReadAddress(0x8200, 0x8200, input_port_2_r), /* player #2 controls */
                new MemoryReadAddress(0x8400, 0x8400, input_port_3_r), /* dipswitch#1 */
                new MemoryReadAddress(0x8500, 0x8500, input_port_4_r), /* dipswitch#2 */
                new MemoryReadAddress(0x8700, 0x8700, CPUB_int_trigger),
                new MemoryReadAddress(0xc000, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress writemem_CPUA[]
            = {
                new MemoryWriteAddress(0x6000, 0x6000, marvins_palette_bank_w), // Marvin's Maze only
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8300, 0x8300, sound_command_w),
                new MemoryWriteAddress(0x8600, 0x8600, MWA_RAM),
                new MemoryWriteAddress(0x86f1, 0x86f1, MWA_RAM),
                new MemoryWriteAddress(0x8700, 0x8700, CPUA_int_enable),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM, spriteram),
                new MemoryWriteAddress(0xd000, 0xd7ff, marvins_background_ram_w, videoram),
                new MemoryWriteAddress(0xd800, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe7ff, marvins_foreground_ram_w),
                new MemoryWriteAddress(0xe800, 0xefff, MWA_RAM),
                new MemoryWriteAddress(0xf000, 0xf3ff, marvins_text_ram_w),
                new MemoryWriteAddress(0xf400, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress marvins_readmem_CPUB[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8700, 0x8700, CPUA_int_trigger),
                new MemoryReadAddress(0xc000, 0xcfff, marvins_spriteram_r),
                new MemoryReadAddress(0xd000, 0xffff, marvins_background_ram_r),
                new MemoryReadAddress(0xe000, 0xffff, marvins_foreground_ram_r),
                new MemoryReadAddress(0xf000, 0xffff, marvins_text_ram_r),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress marvins_writemem_CPUB[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8700, 0x8700, CPUB_int_enable),
                new MemoryWriteAddress(0xc000, 0xcfff, marvins_spriteram_w),
                new MemoryWriteAddress(0xd000, 0xffff, marvins_background_ram_w),
                new MemoryWriteAddress(0xe000, 0xffff, marvins_foreground_ram_w),
                new MemoryWriteAddress(0xf000, 0xffff, marvins_text_ram_w),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress madcrash_readmem_CPUB[]
            = {
                new MemoryReadAddress(0x0000, 0x9fff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xcfff, marvins_foreground_ram_r),
                new MemoryReadAddress(0xd000, 0xdfff, marvins_text_ram_r),
                new MemoryReadAddress(0xe000, 0xefff, marvins_spriteram_r),
                new MemoryReadAddress(0xf000, 0xffff, marvins_background_ram_r),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress madcrash_writemem_CPUB[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8700, 0x8700, CPUB_int_enable), /* Vangaurd II */
                new MemoryWriteAddress(0x8000, 0x9fff, MWA_ROM), /* extra ROM for Mad Crasher */
                new MemoryWriteAddress(0xa000, 0xa000, CPUB_int_enable), /* Mad Crasher */
                new MemoryWriteAddress(0xc000, 0xcfff, marvins_foreground_ram_w),
                new MemoryWriteAddress(0xd000, 0xdfff, marvins_text_ram_w),
                new MemoryWriteAddress(0xe000, 0xefff, marvins_spriteram_w),
                new MemoryWriteAddress(0xf000, 0xffff, marvins_background_ram_w),
                new MemoryWriteAddress(-1)
            };

    static InputPortPtr input_ports_marvins = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* sound CPU status */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* player#1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();  /* player#2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x01, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x03, "5");
            PORT_BITX(0x04, 0x04, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_JOY_NONE, IP_KEY_NONE);
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x38, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x38, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x28, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x20, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Freeze");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW2 (unverified) */

            PORT_DIPNAME(0x07, 0x07, "1st Bonus Life");
            PORT_DIPSETTING(0x07, "10000");
            PORT_DIPSETTING(0x06, "20000");
            PORT_DIPSETTING(0x05, "30000");
            PORT_DIPSETTING(0x04, "40000");
            PORT_DIPSETTING(0x03, "50000");
            PORT_DIPSETTING(0x02, "60000");
            PORT_DIPSETTING(0x01, "70000");
            PORT_DIPSETTING(0x00, "80000");
            PORT_DIPNAME(0x18, 0x18, "2nd Bonus Life");
            PORT_DIPSETTING(0x10, "1st bonus*2");
            PORT_DIPSETTING(0x08, "1st bonus*3");
            PORT_DIPSETTING(0x00, "1st bonus*4");
            //	PORT_DIPSETTING(    0x18, "Unused" );
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_vangrd2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* sound CPU status */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* player#1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();  /* player#2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Unknown");// difficulty?
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "Unknown");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0xc0, "5");

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Freeze");
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x04, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x08, "Language");
            PORT_DIPSETTING(0x08, "English");
            PORT_DIPSETTING(0x00, "Japanese");
            PORT_DIPNAME(0x10, 0x10, "Unknown");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Infinite Lives", IP_JOY_NONE, IP_KEY_NONE);
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_madcrash = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* sound CPU status */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();  /* player#1 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();  /* player#2 controls */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x02, "Unknown");
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Lives"));
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x38, 0x38, DEF_STR("Coinage"));
            //	PORT_DIPSETTING(    0x08, DEF_STR( "5C_1C") );
            PORT_DIPSETTING(0x10, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x38, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x28, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xc0, 0xc0, "Bonus?");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0xc0, "4");

            PORT_START(); 	/* DSW2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x04, "Easy");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x18, 0x10, "Game mode");
            PORT_DIPSETTING(0x18, "Demo Sounds Off");
            PORT_DIPSETTING(0x10, "Demo Sounds On");
            PORT_BITX(0, 0x08, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite Lives", IP_JOY_NONE, IP_KEY_NONE);
            PORT_DIPSETTING(0x00, "Freeze");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * *************************************************************************
     **
     **	Graphics Layout *
	**************************************************************************
     */
    static GfxLayout sprite_layout = new GfxLayout(
            16, 16,
            0x100,
            3,
            new int[]{0, 0x2000 * 8, 0x4000 * 8},
            new int[]{
                7, 6, 5, 4, 3, 2, 1, 0,
                15, 14, 13, 12, 11, 10, 9, 8
            },
            new int[]{
                0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16
            },
            256
    );

    static GfxLayout tile_layout = new GfxLayout(
            8, 8,
            0x100,
            4,
            new int[]{0, 1, 2, 3},
            new int[]{4, 0, 12, 8, 20, 16, 28, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            256
    );

    static GfxDecodeInfo marvins_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tile_layout, 0x080, 8), /* text layer */
                new GfxDecodeInfo(REGION_GFX2, 0, tile_layout, 0x110, 1), /* background */
                new GfxDecodeInfo(REGION_GFX3, 0, tile_layout, 0x100, 1), /* foreground */
                new GfxDecodeInfo(REGION_GFX4, 0, sprite_layout, 0x000, 16), /* sprites */
                new GfxDecodeInfo(-1)
            };

    /**
     * *************************************************************************
     **
     **	Machine Driver *
	**************************************************************************
     */
    static MachineDriver machine_driver_marvins = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 Mhz */
                        readmem_CPUA, writemem_CPUA, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 Mhz */
                        marvins_readmem_CPUB, marvins_writemem_CPUB, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4.0 Mhz */
                        readmem_sound, writemem_sound, null, null,
                        nmi_interrupt, 4 /* seems to be correct */
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            100, /* CPU slices per frame */
            null, /* init_machine */
            /* video hardware */
            256 + 32, 224, new rectangle(0, 255 + 32, 0, 223),
            marvins_gfxdecodeinfo,
            (16 + 2) * 16, (16 + 2) * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            marvins_vh_start,
            null,
            marvins_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_NAMCO,
                        snkwave_interface
                )
            }
    );

    static MachineDriver machine_driver_madcrash = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 Mhz */
                        readmem_CPUA, writemem_CPUA, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3360000, /* 3.336 Mhz */
                        madcrash_readmem_CPUB, madcrash_writemem_CPUB, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4.0 Mhz */
                        readmem_sound, writemem_sound, null, null,
                        nmi_interrupt, 4 /* wrong? */
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            100, /* CPU slices per frame */
            null, /* init_machine */
            /* video hardware */
            256 + 32, 224, new rectangle(0, 255 + 32, 0, 223),
            marvins_gfxdecodeinfo,
            (16 + 2) * 16, (16 + 2) * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            marvins_vh_start,
            null,
            madcrash_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_NAMCO,
                        snkwave_interface
                )
            }
    );

    /**
     * *************************************************************************
     **
     **	ROM Loading * *	note: *	Mad Crasher doesn't pass its internal checksum
     * *	Also, some of the background graphics look to be incorrect. *
	**************************************************************************
     */
    static RomLoadPtr rom_marvins = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for CPUA code */

            ROM_LOAD("pa1", 0x0000, 0x2000, 0x0008d791);
            ROM_LOAD("pa2", 0x2000, 0x2000, 0x9457003c);
            ROM_LOAD("pa3", 0x4000, 0x2000, 0x54c33ecb);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for CPUB code */

            ROM_LOAD("pb1", 0x0000, 0x2000, 0x3b6941a5);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */

            ROM_LOAD("m1", 0x0000, 0x2000, 0x2314c696);
            ROM_LOAD("m2", 0x2000, 0x2000, 0x74ba5799);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("s1", 0x0000, 0x2000, 0x327f70f3);/* characters */

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b1", 0x0000, 0x2000, 0xe528bc60);/* background tiles */

            ROM_REGION(0x2000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b2", 0x0000, 0x2000, 0xe528bc60);/* foreground tiles */

            ROM_REGION(0x6000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("f3", 0x0000, 0x2000, 0xe55c9b83);/* sprites */

            ROM_LOAD("f2", 0x2000, 0x2000, 0x8fc2b081);
            ROM_LOAD("f1", 0x4000, 0x2000, 0x0bd6b4e5);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("marvmaze.j1", 0x000, 0x400, 0x92f5b06d);
            ROM_LOAD("marvmaze.j2", 0x400, 0x400, 0xd2b25665);
            ROM_LOAD("marvmaze.j3", 0x800, 0x400, 0xdf9e6005);
            ROM_END();
        }
    };

    static RomLoadPtr rom_madcrash = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for CPUA code */

            ROM_LOAD("p8", 0x0000, 0x2000, 0xecb2fdc9);
            ROM_LOAD("p9", 0x2000, 0x2000, 0x0a87df26);
            ROM_LOAD("p10", 0x4000, 0x2000, 0x6eb8a87c);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for CPUB code */

            ROM_LOAD("p4", 0x0000, 0x2000, 0x5664d699);
            ROM_LOAD("p5", 0x2000, 0x2000, 0xdea2865a);
            ROM_LOAD("p6", 0x4000, 0x2000, 0xe25a9b9c);
            ROM_LOAD("p7", 0x6000, 0x2000, 0x55b14a36);
            ROM_LOAD("p3", 0x8000, 0x2000, 0xe3c8c2cb);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */

            ROM_LOAD("p1", 0x0000, 0x2000, 0x2dcd036d);
            ROM_LOAD("p2", 0x2000, 0x2000, 0xcc30ae8b);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p13", 0x0000, 0x2000, 0x48c4ade0);/* characters */

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p11", 0x0000, 0x2000, 0x67174956);/* background tiles */

            ROM_REGION(0x2000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p12", 0x0000, 0x2000, 0x085094c1);/* foreground tiles */

            ROM_REGION(0x6000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p14", 0x0000, 0x2000, 0x07e807bc);/* sprites */

            ROM_LOAD("p15", 0x2000, 0x2000, 0xa74149d4);
            ROM_LOAD("p16", 0x4000, 0x2000, 0x6153611a);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("m3-prom.j3", 0x000, 0x400, 0xd19e8a91);
            ROM_LOAD("m2-prom.j4", 0x400, 0x400, 0x9fc325af);
            ROM_LOAD("m1-prom.j5", 0x800, 0x400, 0x07678443);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vangrd2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            ROM_LOAD("p1.9a", 0x0000, 0x2000, 0xbc9eeca5);
            ROM_LOAD("p3.11a", 0x2000, 0x2000, 0x3970f69d);
            ROM_LOAD("p2.12a", 0x4000, 0x2000, 0x58b08b58);
            ROM_LOAD("p4.14a", 0x6000, 0x2000, 0xa95f11ea);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("p5.4a", 0x0000, 0x2000, 0xe4dfd0ba);
            ROM_LOAD("p6.6a", 0x2000, 0x2000, 0x894ff00d);
            ROM_LOAD("p7.7a", 0x4000, 0x2000, 0x40b4d069);

            ROM_REGION(0x10000, REGION_CPU3);/* 64k for sound code */

            ROM_LOAD("p8.6a", 0x0000, 0x2000, 0xa3daa438);
            ROM_LOAD("p9.8a", 0x2000, 0x2000, 0x9345101a);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p15.1e", 0x0000, 0x2000, 0x85718a41);/* characters */

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p13.1a", 0x0000, 0x2000, 0x912f22c6);/* background tiles */

            ROM_REGION(0x2000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p9", 0x0000, 0x2000, 0x7aa0b684);/* foreground tiles */

            ROM_REGION(0x6000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("p12.1kl", 0x0000, 0x2000, 0x8658ea6c);/* sprites */

            ROM_LOAD("p11.3kl", 0x2000, 0x2000, 0x620cd4ec);
            ROM_LOAD("p10.4kl", 0x4000, 0x2000, 0x5bfc04c0);

            ROM_REGION(0x0c00, REGION_PROMS);
            ROM_LOAD("mb7054.3j", 0x000, 0x400, 0x506f659a);
            ROM_LOAD("mb7054.4j", 0x400, 0x400, 0x222133ce);
            ROM_LOAD("mb7054.5j", 0x800, 0x400, 0x2e21a79b);
            ROM_END();
        }
    };

    /**
     * ****************************************************************************************
     */
    public static InitDriverPtr init_marvins = new InitDriverPtr() {
        public void handler() {
            init_sound(0x40);
        }
    };

    public static InitDriverPtr init_madcrash = new InitDriverPtr() {
        public void handler() {
            /*
             The following lines patch out the ROM test (which fails - probably
             because of bit rot, so the rest of the test mode (what little there
             is) can be explored.
	
             unsigned char *mem = memory_region(REGION_CPU1);
             mem[0x3a5d] = 0; mem[0x3a5e] = 0; mem[0x3a5f] = 0;
             */
            init_sound(0x20);
            madcrash_vreg = 0x00;
        }
    };

    public static InitDriverPtr init_vangrd2 = new InitDriverPtr() {
        public void handler() {
            init_sound(0x20);
            madcrash_vreg = 0xf1;
        }
    };

    public static GameDriver driver_marvins = new GameDriver("1983", "marvins", "marvins.java", rom_marvins, null, machine_driver_marvins, input_ports_marvins, init_marvins, ROT270, "SNK", "Marvin's Maze");
    public static GameDriver driver_madcrash = new GameDriver("1984", "madcrash", "marvins.java", rom_madcrash, null, machine_driver_madcrash, input_ports_madcrash, init_madcrash, ROT0, "SNK", "Mad Crasher", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_vangrd2 = new GameDriver("1984", "vangrd2", "marvins.java", rom_vangrd2, null, machine_driver_madcrash, input_ports_vangrd2, init_vangrd2, ROT270, "SNK", "Vanguard II");
}
