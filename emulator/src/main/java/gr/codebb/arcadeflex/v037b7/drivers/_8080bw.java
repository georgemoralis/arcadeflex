/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw._8080bw.*;

import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.cpu.i8039.i8039H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.machine._8080bw.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw._8080bw.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static common.libc.cstring.memcpy;

public class _8080bw {

    static char invaders_palette[]
            = {
                0x00, 0x00, 0x00, /* BLACK */
                0xff, 0xff, 0xff, /* WHITE */};

    public static VhConvertColorPromHandlerPtr init_palette = new VhConvertColorPromHandlerPtr() {
        public void handler(char[] game_palette, char[] game_colortable, UBytePtr color_prom) {
            memcpy(game_palette, invaders_palette, invaders_palette.length);
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Space Invaders"                             */
 /*                                                     */
    /**
     * ****************************************************
     */
    static MemoryReadAddress invaders_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x3fff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress invaders_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x2400, 0x3fff, invaders_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort invaders_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, invaders_shift_data_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport_0_3[]
            = {
                new IOWritePort(0x00, 0x00, invaders_shift_amount_w),
                new IOWritePort(0x03, 0x03, invaders_shift_data_w),
                new IOWritePort(-1) /* end of table */};

    static IOWritePort writeport_1_2[]
            = {
                new IOWritePort(0x01, 0x01, invaders_shift_amount_w),
                new IOWritePort(0x02, 0x02, invaders_shift_data_w),
                new IOWritePort(-1) /* end of table */};

    static IOWritePort writeport_2_3[]
            = {
                new IOWritePort(0x02, 0x02, invaders_shift_amount_w),
                new IOWritePort(0x03, 0x03, invaders_shift_data_w),
                new IOWritePort(-1) /* end of table */};

    static IOWritePort writeport_2_4[]
            = {
                new IOWritePort(0x02, 0x02, invaders_shift_amount_w),
                new IOWritePort(0x04, 0x04, invaders_shift_data_w),
                new IOWritePort(-1) /* end of table */};

    static IOWritePort writeport_4_3[]
            = {
                new IOWritePort(0x03, 0x03, invaders_shift_data_w),
                new IOWritePort(0x04, 0x04, invaders_shift_amount_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_invaders = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* must be ACTIVE_HIGH Super Invaders */

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_invaders = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_invaders,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        invaders_samples_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_SN76477,
/*TODO*///                        invaders_sn76477_interface
                )
            }
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Space Invaders TV Version (Taito)                   */
 /*                                                     */
 /*LT 24-12-1998                                        */
    /**
     * ****************************************************
     */
    /* same as Invaders with a test mode switch */
    static InputPortPtr input_ports_sitv = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* TEST MODE */
            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Space Invaders Part II"                     */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_invadpt2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* otherwise high score entry ends right away */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, "High Score Preset Mode");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /* same as regular invaders, but with a color board added */
    static MachineDriver machine_driver_invadpt2 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_invaders,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        invaders_samples_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_SN76477,
/*TODO*///                        invaders_sn76477_interface
                )
            }
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* ?????? "Super Earth Invasion"                       */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_earthinv = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPNAME(0x02, 0x02, "Pence Coinage");
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN2);/* Pence Coin */
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            /* Not bonus */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "2C/1C 50p/3C (+ Bonus Life)");
            PORT_DIPSETTING(0x80, "1C/1C 50p/5C");

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* ?????? "Space Attack II"                            */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_spaceatt = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));

            PORT_START();
            /* Dummy port for cocktail mode (not used) */
            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Zenitone Microsec "Invaders Revenge"                */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_invrvnge = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Space Invaders II Cocktail"                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_invad2ct = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            /* dip 8 */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* tied to pull-down */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* tied to pull-down */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* labelled reset but tied to pull-up */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* tied to pull-down */
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */

            PORT_START();
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            /* dips 4 & 3 */
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            /* tied to pull-up */
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            /* dip 2 */
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Bonus_Life"));
            /* dip 1 */
            PORT_DIPSETTING(0x80, "1500");
            PORT_DIPSETTING(0x00, "2000");

            PORT_START();
            /* Dummy port for cocktail mode (not used) */
            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_invad2ct = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        1996800, /* 19.968MHz / 10 */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_invad2ct,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        invad2ct_samples_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_SN76477,
/*TODO*///                        invad2ct_sn76477_interface
                )
            }
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Space Laser"                                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_spclaser = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "1 Coin/1 or 2 Players");
            PORT_DIPSETTING(0x80, "1 Coin/1 Player  2 Coins/2 Players");

            PORT_START();
            /* Dummy port for cocktail mode (not used) */
            PORT_BIT(0xff, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Space War Part 3                                    */
 /*                                                     */
 /* Added 21/11/1999 By LT                              */
 /* Thanks to Peter Fyfe for machine info               */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_spacewr3 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Galaxy Wars"                                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_galxwars = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x08, "5000");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Lunar Rescue"                                */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_lrescue = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Universal "Cosmic Monsters"                         */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_cosmicmo = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Nichibutsu "Rolling Crash"                          */
 /*                                                     */
    /**
     * ****************************************************
     */
    static MemoryReadAddress rollingc_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x3fff, MRA_RAM),
                //  new MemoryReadAddress( 0x2000, 0x2002, MRA_RAM ),
                //  new MemoryReadAddress( 0x2003, 0x2003, hack ),
                new MemoryReadAddress(0x4000, 0x5fff, MRA_ROM),
//                new MemoryReadAddress(0xa400, 0xbfff, schaser_colorram_r),
                new MemoryReadAddress(0xe400, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress rollingc_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x2400, 0x3fff, invaders_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0xa400, 0xbfff, schaser_colorram_w, colorram),
                new MemoryWriteAddress(0xe400, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_rollingc = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);/* Game Select */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);/* Game Select */
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_rollingc = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        rollingc_readmem, rollingc_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ******************************************************
     */
    /*                                                       */
 /* Nintendo "Sheriff"                                    */
 /*                                                       */
 /* The only difference between Sheriff and Bandido,      */
 /* beside the copyright notice is the adjustable coinage */
 /* in Bandido.											 */
 /*                                                       */
    /**
     * ******************************************************
     */
    static MemoryReadAddress sheriff_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x27ff, MRA_ROM),
                new MemoryReadAddress(0x4200, 0x7fff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sheriff_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x27ff, MWA_ROM),
                new MemoryWriteAddress(0x4200, 0x5dff, invaders_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x5e00, 0x7fff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sheriff_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, invaders_shift_data_r),
                new IOReadPort(0x04, 0x04, input_port_3_r),
                new IOReadPort(-1) /* end of table */};

    static MemoryReadAddress sheriff_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress sheriff_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sheriff_sound_readport[]
            = {
                new IOReadPort(I8039_p1, I8039_p1, sheriff_sh_p1_r),
                new IOReadPort(I8039_p2, I8039_p2, sheriff_sh_p2_r),
                new IOReadPort(I8039_t0, I8039_t0, sheriff_sh_t0_r),
                new IOReadPort(I8039_t1, I8039_t1, sheriff_sh_t1_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sheriff_sound_writeport[]
            = {
                new IOWritePort(I8039_p2, I8039_p2, sheriff_sh_p2_w),
                new IOWritePort(-1) /* end of table */};

    /* All of the controls/dips for cocktail mode are as per the schematic */
 /* BUT a coffee table version was never manufactured and support was   */
 /* probably never completed.                                           */
 /* e.g. cocktail players button will give 6 credits!                   */
    static InputPortPtr input_ports_sheriff = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 00 Main Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);

            PORT_START();
            /* 01 Player 2 Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* 02 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Marked for   */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Expansion    */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* on Schematic */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* 04 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_bandido = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 00 Main Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY);

            PORT_START();
            /* 01 Player 2 Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* 02 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Marked for   */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* Expansion    */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* on Schematic */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* 04 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_sheriff = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        20160000 / 8, /* 2.52 MHz */
                        sheriff_readmem, sheriff_writemem, sheriff_readport, writeport_2_3,
                        invaders_interrupt, 2 /* two interrupts per frame */
                ),
                new MachineCPU(
                        CPU_I8035 | CPU_AUDIO_CPU,
                        6000000 / 15, /* ??? */
                        sheriff_sound_readmem, sheriff_sound_writemem,
                        sheriff_sound_readport, sheriff_sound_writeport,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_sheriff,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        sheriff_dac_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_SN76477,
/*TODO*///                        sheriff_sn76477_interface
                )
            }
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Space Encounters"                           */
 /*                                                     */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort spcenctr_readport[]
            = {
                new IOReadPort(0x00, 0x00, spcenctr_port_0_r), /* These 2 ports use Gray's binary encoding */
                new IOReadPort(0x01, 0x01, spcenctr_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort spcenctr_writeport[]
            = {
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_spcenctr = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x3f, 0x1f, IPT_AD_STICK_X | IPF_REVERSE, 10, 10, 0, 0x3f);/* 6 bit horiz encoder - Gray's binary */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);
            /* fire */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */
            PORT_ANALOG(0x3f, 0x1f, IPT_AD_STICK_Y, 10, 10, 0, 0x3f);/* 6 bit vert encoder - Gray's binary */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "2000 4000 8000");
            PORT_DIPSETTING(0x01, "3000 6000 12000");
            PORT_DIPSETTING(0x02, "4000 8000 16000");
            PORT_DIPSETTING(0x03, "5000 10000 20000");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x30, 0x00, "Bonus/Test Mode");
            PORT_DIPSETTING(0x00, "Bonus On");
            PORT_DIPSETTING(0x30, "Bonus Off");
            PORT_DIPSETTING(0x20, "Cross Hatch");
            PORT_DIPSETTING(0x10, "Test Mode");
            PORT_DIPNAME(0xc0, 0x00, "Time");
            PORT_DIPSETTING(0x00, "45");
            PORT_DIPSETTING(0x40, "60");
            PORT_DIPSETTING(0x80, "75");
            PORT_DIPSETTING(0xc0, "90");
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_spcenctr = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, spcenctr_readport, spcenctr_writeport,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Gun Fight"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort gunfight_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, boothill_shift_data_r),
                new IOReadPort(-1) /* end of table */};

    static InputPortPtr input_ports_gunfight = new InputPortPtr() {
        public void handler() {
            /* Gun position uses bits 4-6, handled using fake paddles */
            PORT_START();
            /* IN0 - Player 2 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            /* Move Man */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            /* Fire */

            PORT_START();
            /* IN1 - Player 1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            /* Move Man */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1);
            /* Fire */

 /*#ifdef NOTDEF
		PORT_START();       /* IN2 Dips & Coins */
 /*	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START1 );
		PORT_DIPNAME( 0x0C, 0x00, "Plays" );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x04, "2" );
		PORT_DIPSETTING(    0x08, "3" );
		PORT_DIPSETTING(    0x0C, "4" );
		PORT_DIPNAME( 0x30, 0x00, "Time" );/* These are correct */
 /*	PORT_DIPSETTING(    0x00, "60" );
		PORT_DIPSETTING(    0x10, "70" );
		PORT_DIPSETTING(    0x20, "80" );
		PORT_DIPSETTING(    0x30, "90" );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, "1 Coin/1 Player" );
		PORT_DIPSETTING(    0x40, "1 Coin/2 Players" );
		PORT_DIPSETTING(    0x80, "1 Coin/3 Players" );
		PORT_DIPSETTING(    0xc0, "1 Coin/4 Players" );
	#endif*/
            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "1 Coin");
            PORT_DIPSETTING(0x01, "2 Coins");
            PORT_DIPSETTING(0x02, "3 Coins");
            PORT_DIPSETTING(0x03, "4 Coins");
            PORT_DIPNAME(0x0C, 0x00, "Plays");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x0C, "4");
            PORT_DIPNAME(0x30, 0x00, "Time");/* These are correct */
            PORT_DIPSETTING(0x00, "60");
            PORT_DIPSETTING(0x10, "70");
            PORT_DIPSETTING(0x20, "80");
            PORT_DIPSETTING(0x30, "90");
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);

            PORT_START();
            /* Player 2 Gun */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE | IPF_PLAYER2, 50, 10, 1, 255, KEYCODE_H, KEYCODE_Y, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();
            /* Player 1 Gun */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE, 50, 10, 1, 255, KEYCODE_Z, KEYCODE_A, IP_JOY_NONE, IP_JOY_NONE);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_gunfight = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, gunfight_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            init_machine_gunfight,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "M-4"                                        */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_m4 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);/* left trigger */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);/* left reload */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);/* right trigger */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);/* right reload */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, "Time");
            PORT_DIPSETTING(0x00, "60");
            PORT_DIPSETTING(0x04, "70");
            PORT_DIPSETTING(0x08, "80");
            PORT_DIPSETTING(0x0C, "90");
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_m4 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, gunfight_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Boot Hill"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_boothill = new InputPortPtr() {
        public void handler() {
            /* Gun position uses bits 4-6, handled using fake paddles */
            PORT_START();
            /* IN0 - Player 2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            /* Move Man */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);/* Fire */

            PORT_START();
            /* IN1 - Player 1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);/* Move Man */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);/* Fire */

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            //	PORT_DIPSETTING(    0x03, DEF_STR( "1C_2C") );
            PORT_DIPNAME(0x0c, 0x00, "Time");
            PORT_DIPSETTING(0x00, "64");
            PORT_DIPSETTING(0x04, "74");
            PORT_DIPSETTING(0x08, "84");
            PORT_DIPSETTING(0x0C, "94");
            PORT_SERVICE(0x10, IP_ACTIVE_HIGH);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* Player 2 Gun */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE | IPF_PLAYER2, 50, 10, 1, 255, IP_KEY_NONE, IP_KEY_NONE, IP_JOY_NONE, IP_JOY_NONE);

            PORT_START();
            /* Player 1 Gun */
            PORT_ANALOGX(0xff, 0x00, IPT_PADDLE, 50, 10, 1, 255, KEYCODE_Z, KEYCODE_A, IP_JOY_NONE, IP_JOY_NONE);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_boothill = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, gunfight_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            init_machine_boothill,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        boothill_samples_interface
                )
            }
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Space Chaser"                                */
 /*                                                     */
    /**
     * ****************************************************
     */
    static MemoryReadAddress schaser_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x3fff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x5fff, MRA_ROM),
//                new MemoryReadAddress(0xc400, 0xdfff, schaser_colorram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress schaser_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x2400, 0x3fff, invaders_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0xc400, 0xdfff, schaser_colorram_w, colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_schaser = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_DIPNAME(0x40, 0x00, "Number of Controllers");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x40, "2");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_schaser = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        1996800, /* 19.968MHz / 10 */
                        schaser_readmem, schaser_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_schaser,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
/*TODO*///            new MachineSound[]{
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_SN76477,
/*TODO*///                        schaser_sn76477_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_DAC,
/*TODO*///                        schaser_dac_interface
/*TODO*///                ),
/*TODO*///                new MachineSound(
/*TODO*///                        SOUND_CUSTOM,
/*TODO*///                        schaser_custom_interface
/*TODO*///                )
/*TODO*///            }
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Space Chaser" (CV version)                   */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_schasrcv = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Clowns"                                     */
 /*                                                     */
    /**
     * ****************************************************
     */
    /*
	 * Clowns (EPROM version)
     */
    static InputPortPtr input_ports_clowns = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0xff, 0x7f, IPT_PADDLE, 100, 10, 0x01, 0xfe);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, "Balloon Resets");
            PORT_DIPSETTING(0x00, "Each row");
            PORT_DIPSETTING(0x10, "All rows");
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x20, "4000");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_clowns = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Guided Missile"                             */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_gmissile = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, "Time");
            PORT_DIPSETTING(0x00, "60");
            PORT_DIPSETTING(0x08, "70");
            PORT_DIPSETTING(0x04, "80");
            PORT_DIPSETTING(0x0c, "90");
            PORT_DIPNAME(0x30, 0x00, "Extra Play");
            PORT_DIPSETTING(0x00, "500");
            PORT_DIPSETTING(0x20, "700");
            PORT_DIPSETTING(0x10, "1000");
            PORT_DIPSETTING(0x30, "None");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "280 ZZZAP"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_280zzzap = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x0f, 0x00, IPT_PEDAL, 100, 64, 0x00, 0x0f);/* accelerator */
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_TOGGLE);
            /* shift */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN1 - Steering Wheel */
            PORT_ANALOG(0xff, 0x7f, IPT_PADDLE | IPF_REVERSE, 100, 10, 0x01, 0xfe);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x00, "Time");
            PORT_DIPSETTING(0x0c, "60");
            PORT_DIPSETTING(0x00, "80");
            PORT_DIPSETTING(0x08, "99");
            PORT_DIPSETTING(0x04, "Test Mode");
            PORT_DIPNAME(0x30, 0x00, "Extended Time");
            PORT_DIPSETTING(0x00, "Score >= 2.5");
            PORT_DIPSETTING(0x10, "Score >= 2");
            PORT_DIPSETTING(0x20, "None");
            /* 0x30 same as 0x20 */
            PORT_DIPNAME(0xc0, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "German");
            PORT_DIPSETTING(0x80, "French");
            PORT_DIPSETTING(0xc0, "Spanish");
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_280zzzap = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_4_3,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Lupin III"                                   */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_lupin3 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* selects color mode (dynamic vs. static) */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* something has to do with sound */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x08, 0x00, "Bags to Collect");
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x00, "8");
            PORT_DIPNAME(0x10, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x10, "Japanese");
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BITX(0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_lupin3 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        schaser_readmem, schaser_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Nintendo "Heli Fire"                                */
 /*                                                     */
    /**
     * ****************************************************
     */
    static MemoryReadAddress helifire_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x27ff, MRA_ROM),
                new MemoryReadAddress(0x4200, 0x7fff, MRA_RAM),
                new MemoryReadAddress(0xc200, 0xddff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress helifire_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x27ff, MWA_ROM),
                new MemoryWriteAddress(0x4200, 0x5dff, invaders_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x5e00, 0x7fff, MWA_RAM),
//                new MemoryWriteAddress(0xc200, 0xddff, helifire_colorram_w, colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_helifire = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 00 Main Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* 01 Player 2 Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* Start and Coin Buttons */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNUSED);/* Marked for   */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);/* Expansion    */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);/* on Schematic */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0c, "5000");
            PORT_DIPSETTING(0x04, "6000");
            PORT_DIPSETTING(0x08, "8000");
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Nintendo "Space Fever (Color)"                      */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_spacefev = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* 00 Main Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* 01 Player 2 Controls */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BITX(0x08, 0x00, 0, "Start Game A", KEYCODE_Q, IP_JOY_NONE);
            PORT_BITX(0x10, 0x00, 0, "Start Game B", KEYCODE_W, IP_JOY_NONE);
            PORT_BITX(0x20, 0x00, 0, "Start Game C", KEYCODE_E, IP_JOY_NONE);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);/* If on low the game doesn't start */

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            //	PORT_DIPNAME( 0xfc, 0x00, DEF_STR( "Unknown") );
            //	PORT_DIPSETTING(    0x00, DEF_STR( "On") );
            //	PORT_DIPSETTING(    0xfc, DEF_STR( "Off") );
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Polaris"                                     */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_polaris = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT);
            PORT_DIPNAME(0x80, 0x00, "High Score Preset Mode");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_polaris = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        1996800, /* 19.968MHz / 10 */
                        schaser_readmem, schaser_writemem, invaders_readport, writeport_0_3,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_polaris,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Laguna Racer"                               */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_lagunar = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x0f, 0x00, IPT_PEDAL, 100, 64, 0x00, 0x0f);/* accelerator */
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_TOGGLE);
            /* shift */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN1 - Steering Wheel */
            PORT_ANALOG(0xff, 0x7f, IPT_PADDLE | IPF_REVERSE, 100, 10, 0x01, 0xfe);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, "Time");
            PORT_DIPSETTING(0x00, "45");
            PORT_DIPSETTING(0x04, "60");
            PORT_DIPSETTING(0x08, "75");
            PORT_DIPSETTING(0x0c, "90");
            PORT_DIPNAME(0x30, 0x00, "Extended Time");
            PORT_DIPSETTING(0x00, "350");
            PORT_DIPSETTING(0x10, "400");
            PORT_DIPSETTING(0x20, "450");
            PORT_DIPSETTING(0x30, "500");
            PORT_DIPNAME(0xc0, 0x00, "Test Modes");
            PORT_DIPSETTING(0x00, "Play Mode");
            PORT_DIPSETTING(0x40, "RAM/ROM");
            PORT_DIPSETTING(0x80, "Steering");
            PORT_DIPSETTING(0xc0, "No Extended Play");
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Phantom II"                                 */
 /*                                                     */
 /* To Do : little fluffy clouds                        */
 /*         you still see them sometimes in the desert  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_phantom2 = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x06, 0x06, "Time");
            PORT_DIPSETTING(0x00, "45sec 20sec 20");
            PORT_DIPSETTING(0x02, "60sec 25sec 25");
            PORT_DIPSETTING(0x04, "75sec 30sec 30");
            PORT_DIPSETTING(0x06, "90sec 35sec 35");
            PORT_SERVICE(0x20, IP_ACTIVE_LOW);

            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Dog Patch"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_dogpatch = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_ANALOG(0x38, 0x1f, IPT_AD_STICK_X | IPF_PLAYER2, 25, 10, 0x05, 0x48);
            /* 6 bit horiz encoder - Gray's binary? */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2);

            PORT_START();
            /* IN1 */
            PORT_ANALOG(0x3f, 0x1f, IPT_AD_STICK_X, 25, 10, 0x01, 0x3e);/* 6 bit horiz encoder - Gray's binary? */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x03, 0x00, "# Cans");
            PORT_DIPSETTING(0x03, "10");
            PORT_DIPSETTING(0x02, "15");
            PORT_DIPSETTING(0x01, "20");
            PORT_DIPSETTING(0x00, "25");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x10, 0x00, "Extended Play");
            PORT_DIPSETTING(0x10, "3 extra cans");
            PORT_DIPSETTING(0x00, "5 extra cans");
            PORT_SERVICE(0x20, IP_ACTIVE_LOW);
            PORT_DIPNAME(0xc0, 0x00, "Extended Play");
            PORT_DIPSETTING(0xc0, "150 Pts");
            PORT_DIPSETTING(0x80, "175 Pts");
            PORT_DIPSETTING(0x40, "225 Pts");
            PORT_DIPSETTING(0x00, "275 Pts");
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "4 Player Bowling"                           */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort bowler_readport[]
            = {
                new IOReadPort(0x01, 0x01, invaders_shift_data_comp_r),
                new IOReadPort(0x02, 0x02, input_port_0_r), /* dip switch */
                new IOReadPort(0x04, 0x04, input_port_1_r), /* coins / switches */
                new IOReadPort(0x05, 0x05, input_port_2_r), /* ball vert */
                new IOReadPort(0x06, 0x06, input_port_3_r), /* ball horz */
                new IOReadPort(-1) /* end of table */};

    static InputPortPtr input_ports_bowler = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN2 */
            PORT_DIPNAME(0x03, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "French");
            PORT_DIPSETTING(0x02, "German");
            /*PORT_DIPSETTING(    0x03, "German" );/
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );/* effects button 1 */
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START();
            /* IN4 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* IN5 */
            PORT_ANALOG(0xff, 0, IPT_TRACKBALL_Y | IPF_REVERSE, 10, 10, 0, 0);

            PORT_START();
            /* IN6 */
            PORT_ANALOG(0xff, 0, IPT_TRACKBALL_X, 10, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_bowler = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz */
                        invaders_readmem, invaders_writemem, bowler_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Shuffleboard"                               */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort shuffle_readport[]
            = {
                new IOReadPort(0x01, 0x01, invaders_shift_data_r),
                new IOReadPort(0x02, 0x02, input_port_0_r), /* dip switch */
                new IOReadPort(0x04, 0x04, input_port_1_r), /* coins / switches */
                new IOReadPort(0x05, 0x05, input_port_2_r), /* ball vert */
                new IOReadPort(0x06, 0x06, input_port_3_r), /* ball horz */
                new IOReadPort(-1) /* end of table */};

    static InputPortPtr input_ports_shuffle = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x01, "French");
            PORT_DIPSETTING(0x02, "German");
            /*PORT_DIPSETTING(    0x03, "German" );/
		PORT_DIPNAME( 0x0c, 0x04, "Points to Win" );
		PORT_DIPSETTING(    0x00, "25" );
		PORT_DIPSETTING(    0x04, "35" );
		PORT_DIPSETTING(    0x08, "40" );
		PORT_DIPSETTING(    0x0c, "50" );
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x30, "2 Coins/1 Player  4 Coins/2 Players" );
		PORT_DIPSETTING(    0x20, "2 Coins/1 or 2 Players" );
		PORT_DIPSETTING(    0x10, "1 Coin/1 Player  2 Coins/2 Players" );
		PORT_DIPSETTING(    0x00, "1 Coin/1 or 2 Players" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );	/* time limit? */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START2);
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON1, "Game Select", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN2 */
            PORT_ANALOG(0xff, 0, IPT_TRACKBALL_Y, 10, 10, 0, 0);

            PORT_START();
            /* IN3 */
            PORT_ANALOG(0xff, 0, IPT_TRACKBALL_X | IPF_REVERSE, 10, 10, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_shuffle = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz */
                        invaders_readmem, invaders_writemem, shuffle_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Sea Wolf"                                   */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort seawolf_readport[]
            = {
                new IOReadPort(0x00, 0x00, invaders_shift_data_rev_r),
                new IOReadPort(0x01, 0x01, input_port_0_r),
                new IOReadPort(0x02, 0x02, input_port_1_r),
                new IOReadPort(0x03, 0x03, invaders_shift_data_r),
                new IOReadPort(-1) /* end of table */};

    static InputPortPtr input_ports_seawolf = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x1f, 0x01, IPT_PADDLE, 20, 5, 0, 0x1f);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_DIPNAME(0xc0, 0x00, "Time");
            PORT_DIPSETTING(0x00, "61");
            PORT_DIPSETTING(0x40, "71");
            PORT_DIPSETTING(0x80, "81");
            PORT_DIPSETTING(0xc0, "91");

            PORT_START();
            /* IN1 Dips & Coins */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START1);
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_TILT);// Reset High Scores
            PORT_DIPNAME(0xe0, 0x20, "Extended Play");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "2000");
            PORT_DIPSETTING(0x40, "3000");
            PORT_DIPSETTING(0x60, "4000");
            PORT_DIPSETTING(0x80, "5000");
            PORT_DIPSETTING(0xa0, "6000");
            PORT_DIPSETTING(0xc0, "7000");
            PORT_DIPSETTING(0xe0, "Test Mode");
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_seawolf = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, seawolf_readport, writeport_4_3,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            init_machine_seawolf,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Blue Shark"                                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_blueshrk = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x7f, 0x45, IPT_PADDLE, 100, 10, 0xf, 0x7f);

            PORT_START();
            /* IN1 Dips & Coins */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x60, 0x20, "Replay");
            PORT_DIPSETTING(0x20, "14000");
            PORT_DIPSETTING(0x40, "18000");
            PORT_DIPSETTING(0x60, "22000");
            PORT_DIPSETTING(0x00, "None");
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_blueshrk = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000,
                        invaders_readmem, invaders_writemem, seawolf_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Desert Gun"                                 */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_desertgu = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_ANALOG(0x7f, 0x45, IPT_AD_STICK_X, 70, 10, 0xf, 0x7f);

            PORT_START();
            PORT_DIPNAME(0x03, 0x00, "Time");
            PORT_DIPSETTING(0x00, "40");
            PORT_DIPSETTING(0x01, "50");
            PORT_DIPSETTING(0x02, "60");
            PORT_DIPSETTING(0x03, "70");
            PORT_DIPNAME(0x0c, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x04, "German");
            PORT_DIPSETTING(0x08, "French");
            PORT_DIPSETTING(0x0c, "Norwegian?");
            PORT_DIPNAME(0x30, 0x00, "Extended Play");
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x10, "7000");
            PORT_DIPSETTING(0x20, "9000");
            PORT_DIPSETTING(0x30, "Test Mode");
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* IN2 */
            PORT_ANALOG(0x7f, 0x45, IPT_AD_STICK_Y, 70, 10, 0xf, 0x7f);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_desertgu = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000,
                        invaders_readmem, invaders_writemem, seawolf_readport, writeport_1_2,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_desertgu,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Extra Innings"                              */
 /*                                                     */
    /**
     * ****************************************************
     */
    /*
	 * The cocktail version has independent bat, pitch, and field controls
	 * while the upright version ties the pairs of inputs together through
	 * jumpers in the wiring harness.
     */
    static InputPortPtr input_ports_einnings = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            /* home bat */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);/* home fielders left */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);/* home fielders right */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            /* home pitch left */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);/* home pitch right */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            /* home pitch slow */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            /* home pitch fast */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            /* vistor bat */
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);/* vistor fielders left */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);/* visitor fielders right */
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);/* visitor pitch left */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);/* visitor pitch right */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            /* visitor pitch slow */
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            /* visitor pitch fast */
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "2C/1 In (1 or 2 Players)");
            PORT_DIPSETTING(0x03, "2C/1 In 4C/3 In (1 or 2 Pls)");
            PORT_DIPSETTING(0x00, "1 Coin/1 Inning (1 or 2 Pls)");
            PORT_DIPSETTING(0x01, "1C/1 In 2C/3 In (1 or 2 Pls)");
            PORT_DIPSETTING(0x04, "1C/1Pl 2C/2Pl 4C/3Inn");
            PORT_DIPSETTING(0x05, "2C/1Pl 4C/2Pl 8C/3Inn");
            /* 0x06 and 0x07 same as 0x00 */
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x40, IP_ACTIVE_LOW);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Amazing Maze"                               */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_maze = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START();
            /* DSW0 - Never read (?) */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Tornado Baseball"                           */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_tornbase = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_DIPNAME(0x78, 0x40, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x18, "4 Coins/1 Inning 32/9");
            PORT_DIPSETTING(0x10, "3 Coins/1 Inning 24/9");
            PORT_DIPSETTING(0x38, "4 Coins/2 Innings 16/9");
            PORT_DIPSETTING(0x08, "2 Coins/1 Inning 16/9");
            PORT_DIPSETTING(0x30, "3 Coins/2 Innings 12/9");
            PORT_DIPSETTING(0x28, "2 Coins/2 Innings 8/9");
            PORT_DIPSETTING(0x00, "1 Coin/1 Inning 8/9");
            PORT_DIPSETTING(0x58, "4 Coins/4 Innings 8/9");
            PORT_DIPSETTING(0x50, "3 Coins/4 Innings 6/9");
            PORT_DIPSETTING(0x48, "2 Coins/4 Innings 4/9");
            PORT_DIPSETTING(0x20, "1 Coin/2 Innings 4/9");
            PORT_DIPSETTING(0x40, "1 Coin/4 Innings 2/9");
            PORT_DIPSETTING(0x78, "4 Coins/9 Innings");
            PORT_DIPSETTING(0x70, "3 Coins/9 Innings");
            PORT_DIPSETTING(0x68, "2 Coins/9 Innings");
            PORT_DIPSETTING(0x60, "1 Coin/9 Innings");
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_tornbase = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Midway "Checkmate"                                  */
 /*                                                     */
    /**
     * ****************************************************
     */
    static IOReadPort checkmat_readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, input_port_3_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort checkmat_writeport[]
            = {
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_checkmat = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0  */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);

            PORT_START();
            /* IN1  */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_PLAYER4);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER4);

            PORT_START();
            /* IN2 Dips & Coins */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, "1 Coin/1 or 2 Playera");
            PORT_DIPSETTING(0x01, "1 Coin/1 to 4 Players");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, "Rounds");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x04, "3");
            PORT_DIPSETTING(0x08, "4");
            PORT_DIPSETTING(0x0c, "5");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x00, "Language?");
            PORT_DIPSETTING(0x00, "English?");
            PORT_DIPSETTING(0x20, "German?");
            PORT_DIPSETTING(0x40, "French?");
            PORT_DIPSETTING(0x60, "Spanish?");
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START();
            /* IN3  */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START3);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START4);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_checkmat = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, checkmat_readport, checkmat_writeport,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            32768 + 2, 0, /* leave extra colors for the overlay */
            init_palette,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Ozma Wars"                                   */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_ozmawars = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, "Energy");
            PORT_DIPSETTING(0x00, "15000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPSETTING(0x02, "25000");
            PORT_DIPSETTING(0x03, "35000");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_spaceph = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x00, "Fuel");
            PORT_DIPSETTING(0x03, "35000");
            PORT_DIPSETTING(0x02, "25000");
            PORT_DIPSETTING(0x01, "20000");
            PORT_DIPSETTING(0x00, "15000");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, "Bonus Fuel");
            PORT_DIPSETTING(0x08, "10000");
            PORT_DIPSETTING(0x00, "15000");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Fire */
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Left */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Right */
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_2C"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Emag "Super Invaders"                               */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_sinvemag = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Jatre Specter (Taito?)                              */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_jspecter = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            /* Note: There must have been a toggle switch on the outside of the unit.
		   The difficulty can be set by the player */
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x00, "Hard");

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "1000");
            PORT_DIPSETTING(0x00, "1500");
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************
     */
    /*                                                     */
 /* Taito "Balloon Bomber"                              */
 /*                                                     */
    /**
     * ****************************************************
     */
    static InputPortPtr input_ports_ballbomb = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_ballbomb = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_8080,
                        2000000, /* 2 MHz? */
                        invaders_readmem, invaders_writemem, invaders_readport, writeport_2_4,
                        invaders_interrupt, 2 /* two interrupts per frame */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            init_machine_ballbomb,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            null, /* no gfxdecodeinfo - bitmapped display */
            8, 0,
            invadpt2_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            invaders_vh_start,
            invaders_vh_stop,
            invaders_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    static InputPortPtr input_ports_spceking = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, "High Score Preset Mode");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_DIPNAME(0x80, 0x00, "Coin Info");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* Dummy port for cocktail mode */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static RomLoadHandlerPtr rom_invaders = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invaders.h", 0x0000, 0x0800, 0x734f5ad8);
            ROM_LOAD("invaders.g", 0x0800, 0x0800, 0x6bfaca4a);
            ROM_LOAD("invaders.f", 0x1000, 0x0800, 0x0ccead96);
            ROM_LOAD("invaders.e", 0x1800, 0x0800, 0x14e538b0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_earthinv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("earthinv.h", 0x0000, 0x0800, 0x58a750c8);
            ROM_LOAD("earthinv.g", 0x0800, 0x0800, 0xb91742f1);
            ROM_LOAD("earthinv.f", 0x1000, 0x0800, 0x4acbbc60);
            ROM_LOAD("earthinv.e", 0x1800, 0x0800, 0xdf397b12);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spaceatt = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("spaceatt.h", 0x0000, 0x0800, 0xa31d0756);
            ROM_LOAD("spaceatt.g", 0x0800, 0x0800, 0xf41241f7);
            ROM_LOAD("spaceatt.f", 0x1000, 0x0800, 0x4c060223);
            ROM_LOAD("spaceatt.e", 0x1800, 0x0800, 0x7cf6f604);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sinvzen = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("1.bin", 0x0000, 0x0400, 0x9b0da779);
            ROM_LOAD("2.bin", 0x0400, 0x0400, 0x9858ccab);
            ROM_LOAD("3.bin", 0x0800, 0x0400, 0xa1cc38b5);
            ROM_LOAD("4.bin", 0x0c00, 0x0400, 0x1f2db7a8);
            ROM_LOAD("5.bin", 0x1000, 0x0400, 0x9b505fcd);
            ROM_LOAD("6.bin", 0x1400, 0x0400, 0xde0ca0ae);
            ROM_LOAD("7.bin", 0x1800, 0x0400, 0x25a296f6);
            ROM_LOAD("8.bin", 0x1c00, 0x0400, 0xf4bc4a98);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sinvemag = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sv0h.bin", 0x0000, 0x0400, 0x86bb8cb6);
            ROM_LOAD("emag_si.b", 0x0400, 0x0400, 0xfebe6d1a);
            ROM_LOAD("emag_si.c", 0x0800, 0x0400, 0xaafb24f7);
            ROM_LOAD("emag_si.d", 0x1400, 0x0400, 0x68c4b9da);
            ROM_LOAD("emag_si.e", 0x1800, 0x0400, 0xc4e80586);
            ROM_LOAD("emag_si.f", 0x1c00, 0x0400, 0x077f5ef2);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_alieninv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("1h.bin", 0x0000, 0x0800, 0xc46df7f4);
            ROM_LOAD("1g.bin", 0x0800, 0x0800, 0x4b1112d6);
            ROM_LOAD("1f.bin", 0x1000, 0x0800, 0Xadca18a5);
            ROM_LOAD("1e.bin", 0x1800, 0x0800, 0x0449CB52);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sitv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("tv0h.s1", 0x0000, 0x0800, 0xfef18aad);
            ROM_LOAD("tv02.rp1", 0x0800, 0x0800, 0x3c759a90);
            ROM_LOAD("tv03.n1", 0x1000, 0x0800, 0x0ad3657f);
            ROM_LOAD("tv04.m1", 0x1800, 0x0800, 0xcd2c67f6);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sicv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("cv17.bin", 0x0000, 0x0800, 0x3dfbe9e6);
            ROM_LOAD("cv18.bin", 0x0800, 0x0800, 0xbc3c82bf);
            ROM_LOAD("cv19.bin", 0x1000, 0x0800, 0xd202b41c);
            ROM_LOAD("cv20.bin", 0x1800, 0x0800, 0xc74ee7b6);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 */
            ROM_LOAD("cv01_1.bin", 0x0000, 0x0400, 0xaac24f34);
            ROM_LOAD("cv02_2.bin", 0x0400, 0x0400, 0x2bdf83a0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sisv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sv0h.bin", 0x0000, 0x0400, 0x86bb8cb6);
            ROM_LOAD("sv02.bin", 0x0400, 0x0400, 0x0e159534);
            ROM_LOAD("invaders.g", 0x0800, 0x0800, 0x6bfaca4a);
            ROM_LOAD("invaders.f", 0x1000, 0x0800, 0x0ccead96);
            ROM_LOAD("tv04.m1", 0x1800, 0x0800, 0xcd2c67f6);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 */
            ROM_LOAD("cv01_1.bin", 0x0000, 0x0400, 0xaac24f34);
            ROM_LOAD("cv02_2.bin", 0x0400, 0x0400, 0x2bdf83a0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sisv2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sv0h.bin", 0x0000, 0x0400, 0x86bb8cb6);
            ROM_LOAD("emag_si.b", 0x0400, 0x0400, 0xfebe6d1a);
            ROM_LOAD("sv12", 0x0800, 0x0400, 0xa08e7202);
            ROM_LOAD("invaders.f", 0x1000, 0x0800, 0x0ccead96);
            ROM_LOAD("sv13", 0x1800, 0x0400, 0xa9011634);
            ROM_LOAD("sv14", 0x1c00, 0x0400, 0x58730370);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 */
            ROM_LOAD("cv01_1.bin", 0x0000, 0x0400, 0xaac24f34);
            ROM_LOAD("cv02_2.bin", 0x0400, 0x0400, 0x2bdf83a0);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spceking = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invaders.h", 0x0000, 0x0800, 0x734f5ad8);
            ROM_LOAD("spcekng2", 0x0800, 0x0800, 0x96dcdd42);
            ROM_LOAD("spcekng3", 0x1000, 0x0800, 0x95fc96ad);
            ROM_LOAD("spcekng4", 0x1800, 0x0800, 0x54170ada);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spcewars = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sanritsu.1", 0x0000, 0x0400, 0xca331679);
            ROM_LOAD("sanritsu.2", 0x0400, 0x0400, 0x48dc791c);
            ROM_LOAD("ic35.bin", 0x0800, 0x0800, 0x40c2d55b);
            ROM_LOAD("sanritsu.5", 0x1000, 0x0400, 0x77475431);
            ROM_LOAD("sanritsu.6", 0x1400, 0x0400, 0x392ef82c);
            ROM_LOAD("sanritsu.7", 0x1800, 0x0400, 0xb3a93df8);
            ROM_LOAD("sanritsu.8", 0x1c00, 0x0400, 0x64fdc3e1);
            ROM_LOAD("sanritsu.9", 0x4000, 0x0400, 0xb2f29601);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacewr3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("ic36.bin", 0x0000, 0x0800, 0x9e30f88a);
            ROM_LOAD("ic35.bin", 0x0800, 0x0800, 0x40c2d55b);
            ROM_LOAD("ic34.bin", 0x1000, 0x0800, 0xb435f021);
            ROM_LOAD("ic33.bin", 0x1800, 0x0800, 0xcbdc6fe8);
            ROM_LOAD("ic32.bin", 0x4000, 0x0800, 0x1e5a753c);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invaderl = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("c01", 0x0000, 0x0400, 0x499f253a);
            ROM_LOAD("c02", 0x0400, 0x0400, 0x2d0b2e1f);
            ROM_LOAD("c03", 0x0800, 0x0400, 0x03033dc2);
            ROM_LOAD("c07", 0x1000, 0x0400, 0x5a7bbf1f);
            ROM_LOAD("c04", 0x1400, 0x0400, 0x455b1fa7);
            ROM_LOAD("c05", 0x1800, 0x0400, 0x40cbef75);
            ROM_LOAD("sv06.bin", 0x1c00, 0x0400, 0x2c68e0b4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_jspecter = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("3305.u6", 0x0000, 0x1000, 0xab211a4f);
            ROM_LOAD("3306.u7", 0x1400, 0x1000, 0x0df142a7);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invadpt2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("pv.01", 0x0000, 0x0800, 0x7288a511);
            ROM_LOAD("pv.02", 0x0800, 0x0800, 0x097dd8d5);
            ROM_LOAD("pv.03", 0x1000, 0x0800, 0x1766337e);
            ROM_LOAD("pv.04", 0x1800, 0x0800, 0x8f0e62e0);
            ROM_LOAD("pv.05", 0x4000, 0x0800, 0x19b505e9);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 */
            ROM_LOAD("pv06_1.bin", 0x0000, 0x0400, 0xa732810b);
            ROM_LOAD("pv07_2.bin", 0x0400, 0x0400, 0x2c5b91cb);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invaddlx = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invdelux.h", 0x0000, 0x0800, 0xe690818f);
            ROM_LOAD("invdelux.g", 0x0800, 0x0800, 0x4268c12d);
            ROM_LOAD("invdelux.f", 0x1000, 0x0800, 0xf4aa1880);
            ROM_LOAD("invdelux.e", 0x1800, 0x0800, 0x408849c1);
            ROM_LOAD("invdelux.d", 0x4000, 0x0800, 0xe8d5afcd);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_moonbase = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("pv.01", 0x0000, 0x0800, 0x7288a511);
            ROM_LOAD("pv.02", 0x0800, 0x0800, 0x097dd8d5);
            ROM_LOAD("ze3-5.bin", 0x1000, 0x0400, 0x2b105ed3);
            ROM_LOAD("ze3-6.bin", 0x1400, 0x0400, 0xcb3d6dcb);
            ROM_LOAD("ze3-7.bin", 0x1800, 0x0400, 0x774b52c9);
            ROM_LOAD("ze3-8.bin", 0x1c00, 0x0400, 0xe88ea83b);
            ROM_LOAD("ze3-9.bin", 0x4000, 0x0400, 0x2dd5adfa);
            ROM_LOAD("ze3-10.bin", 0x4400, 0x0400, 0x1e7c22a4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invad2ct = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invad2ct.h", 0x0000, 0x0800, 0x51d02a71);
            ROM_LOAD("invad2ct.g", 0x0800, 0x0800, 0x533ac770);
            ROM_LOAD("invad2ct.f", 0x1000, 0x0800, 0xd1799f39);
            ROM_LOAD("invad2ct.e", 0x1800, 0x0800, 0x291c1418);
            ROM_LOAD("invad2ct.b", 0x5000, 0x0800, 0x8d9a07c4);
            ROM_LOAD("invad2ct.a", 0x5800, 0x0800, 0xefdabb03);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invrvnge = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invrvnge.h", 0x0000, 0x0800, 0xaca41bbb);
            ROM_LOAD("invrvnge.g", 0x0800, 0x0800, 0xcfe89dad);
            ROM_LOAD("invrvnge.f", 0x1000, 0x0800, 0xe350de2c);
            ROM_LOAD("invrvnge.e", 0x1800, 0x0800, 0x1ec8dfc8);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_invrvnga = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("5m.bin", 0x0000, 0x0800, 0xb145cb71);
            ROM_LOAD("5n.bin", 0x0800, 0x0800, 0x660e8af3);
            ROM_LOAD("5p.bin", 0x1000, 0x0800, 0x6ec5a9ad);
            ROM_LOAD("5r.bin", 0x1800, 0x0800, 0x74516811);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spclaser = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("la01", 0x0000, 0x0800, 0xbedc0078);
            ROM_LOAD("spcewarl.2", 0x0800, 0x0800, 0x43bc65c5);
            ROM_LOAD("la03", 0x1000, 0x0800, 0x1083e9cc);
            ROM_LOAD("la04", 0x1800, 0x0800, 0x5116b234);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_laser = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("1.u36", 0x0000, 0x0800, 0xb44e2c41);
            ROM_LOAD("2.u35", 0x0800, 0x0800, 0x9876f331);
            ROM_LOAD("3.u34", 0x1000, 0x0800, 0xed79000b);
            ROM_LOAD("4.u33", 0x1800, 0x0800, 0x10a160a1);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spcewarl = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("spcewarl.1", 0x0000, 0x0800, 0x1fcd34d2);
            ROM_LOAD("spcewarl.2", 0x0800, 0x0800, 0x43bc65c5);
            ROM_LOAD("spcewarl.3", 0x1000, 0x0800, 0x7820df3a);
            ROM_LOAD("spcewarl.4", 0x1800, 0x0800, 0xadc05b8d);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_galxwars = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("galxwars.0", 0x0000, 0x0400, 0x608bfe7f);
            ROM_LOAD("galxwars.1", 0x0400, 0x0400, 0xa810b258);
            ROM_LOAD("galxwars.2", 0x0800, 0x0400, 0x74f31781);
            ROM_LOAD("galxwars.3", 0x0c00, 0x0400, 0xc88f886c);
            ROM_LOAD("galxwars.4", 0x4000, 0x0400, 0xae4fe8fb);
            ROM_LOAD("galxwars.5", 0x4400, 0x0400, 0x37708a35);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_starw = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("roma", 0x0000, 0x0400, 0x60e8993c);
            ROM_LOAD("romb", 0x0400, 0x0400, 0xb8060773);
            ROM_LOAD("romc", 0x0800, 0x0400, 0x307ce6b8);
            ROM_LOAD("romd", 0x1400, 0x0400, 0x2b0d0a88);
            ROM_LOAD("rome", 0x1800, 0x0400, 0x5b1c3ad0);
            ROM_LOAD("romf", 0x1c00, 0x0400, 0xc8e42d3d);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_lrescue = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("lrescue.1", 0x0000, 0x0800, 0x2bbc4778);
            ROM_LOAD("lrescue.2", 0x0800, 0x0800, 0x49e79706);
            ROM_LOAD("lrescue.3", 0x1000, 0x0800, 0x1ac969be);
            ROM_LOAD("lrescue.4", 0x1800, 0x0800, 0x782fee3c);
            ROM_LOAD("lrescue.5", 0x4000, 0x0800, 0x58fde8bc);
            ROM_LOAD("lrescue.6", 0x4800, 0x0800, 0xbfb0f65d);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color map */
            ROM_LOAD("7643-1.cpu", 0x0000, 0x0400, 0x8b2e38de);
            ROM_RELOAD(0x0400, 0x0400);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_grescue = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("lrescue.1", 0x0000, 0x0800, 0x2bbc4778);
            ROM_LOAD("lrescue.2", 0x0800, 0x0800, 0x49e79706);
            ROM_LOAD("lrescue.3", 0x1000, 0x0800, 0x1ac969be);
            ROM_LOAD("grescue.4", 0x1800, 0x0800, 0xca412991);
            ROM_LOAD("grescue.5", 0x4000, 0x0800, 0xa419a4d6);
            ROM_LOAD("lrescue.6", 0x4800, 0x0800, 0xbfb0f65d);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color map */
            ROM_LOAD("7643-1.cpu", 0x0000, 0x0400, 0x8b2e38de);
            ROM_RELOAD(0x0400, 0x0400);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_desterth = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("36_h.bin", 0x0000, 0x0800, 0xf86923e5);
            ROM_LOAD("35_g.bin", 0x0800, 0x0800, 0x797f440d);
            ROM_LOAD("34_f.bin", 0x1000, 0x0800, 0x993d0846);
            ROM_LOAD("33_e.bin", 0x1800, 0x0800, 0x8d155fc5);
            ROM_LOAD("32_d.bin", 0x4000, 0x0800, 0x3f531b6f);
            ROM_LOAD("31_c.bin", 0x4800, 0x0800, 0xab019c30);
            ROM_LOAD("42_b.bin", 0x5000, 0x0800, 0xed9dbac6);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color map */
            ROM_LOAD("7643-1.cpu", 0x0000, 0x0400, 0x8b2e38de);
            ROM_RELOAD(0x0400, 0x0400);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_cosmicmo = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("cosmicmo.1", 0x0000, 0x0400, 0xd6e4e5da);
            ROM_LOAD("cosmicmo.2", 0x0400, 0x0400, 0x8f7988e6);
            ROM_LOAD("cosmicmo.3", 0x0800, 0x0400, 0x2d2e9dc8);
            ROM_LOAD("cosmicmo.4", 0x0c00, 0x0400, 0x26cae456);
            ROM_LOAD("cosmicmo.5", 0x4000, 0x0400, 0xb13f228e);
            ROM_LOAD("cosmicmo.6", 0x4400, 0x0400, 0x4ae1b9c4);
            ROM_LOAD("cosmicmo.7", 0x4800, 0x0400, 0x6a13b15b);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_superinv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("00", 0x0000, 0x0400, 0x7a9b4485);
            ROM_LOAD("01", 0x0400, 0x0400, 0x7c86620d);
            ROM_LOAD("02", 0x0800, 0x0400, 0xccaf38f6);
            ROM_LOAD("03", 0x1400, 0x0400, 0x8ec9eae2);
            ROM_LOAD("04", 0x1800, 0x0400, 0x68719b30);
            ROM_LOAD("05", 0x1c00, 0x0400, 0x8abe2466);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_rollingc = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("rc01.bin", 0x0000, 0x0400, 0x66fa50bf);
            ROM_LOAD("rc02.bin", 0x0400, 0x0400, 0x61c06ae4);
            ROM_LOAD("rc03.bin", 0x0800, 0x0400, 0x77e39fa0);
            ROM_LOAD("rc04.bin", 0x0c00, 0x0400, 0x3fdfd0f3);
            ROM_LOAD("rc05.bin", 0x1000, 0x0400, 0xc26a8f5b);
            ROM_LOAD("rc06.bin", 0x1400, 0x0400, 0x0b98dbe5);
            ROM_LOAD("rc07.bin", 0x1800, 0x0400, 0x6242145c);
            ROM_LOAD("rc08.bin", 0x1c00, 0x0400, 0xd23c2ef1);
            ROM_LOAD("rc09.bin", 0x4000, 0x0800, 0x2e2c5b95);
            ROM_LOAD("rc10.bin", 0x4800, 0x0800, 0xef94c502);
            ROM_LOAD("rc11.bin", 0x5000, 0x0800, 0xa3164b18);
            ROM_LOAD("rc12.bin", 0x5800, 0x0800, 0x2052f6d9);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_boothill = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("romh.cpu", 0x0000, 0x0800, 0x1615d077);
            ROM_LOAD("romg.cpu", 0x0800, 0x0800, 0x65a90420);
            ROM_LOAD("romf.cpu", 0x1000, 0x0800, 0x3fdafd79);
            ROM_LOAD("rome.cpu", 0x1800, 0x0800, 0x374529f4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_schaser = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("rt13.bin", 0x0000, 0x0400, 0x0dfbde68);
            ROM_LOAD("rt14.bin", 0x0400, 0x0400, 0x5a508a25);
            ROM_LOAD("rt15.bin", 0x0800, 0x0400, 0x2ac43a93);
            ROM_LOAD("rt16.bin", 0x0c00, 0x0400, 0xf5583afc);
            ROM_LOAD("rt17.bin", 0x1000, 0x0400, 0x51cf1155);
            ROM_LOAD("rt18.bin", 0x1400, 0x0400, 0x3f0fc73a);
            ROM_LOAD("rt19.bin", 0x1800, 0x0400, 0xb66ea369);
            ROM_LOAD("rt20.bin", 0x1c00, 0x0400, 0xe3a7466a);
            ROM_LOAD("rt21.bin", 0x4000, 0x0400, 0xb368ac98);
            ROM_LOAD("rt22.bin", 0x4400, 0x0400, 0x6e060dfb);

            ROM_REGION(0x0400, REGION_PROMS);
            /* background color map (missing) */
            ROM_LOAD("schaser.prm", 0x0000, 0x0400, 0x00000000);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_schasrcv = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("1", 0x0000, 0x0400, 0xbec2b16b);
            ROM_LOAD("2", 0x0400, 0x0400, 0x9d25e608);
            ROM_LOAD("3", 0x0800, 0x0400, 0x113d0635);
            ROM_LOAD("4", 0x0c00, 0x0400, 0xf3a43c8d);
            ROM_LOAD("5", 0x1000, 0x0400, 0x47c84f23);
            ROM_LOAD("6", 0x1400, 0x0400, 0x02ff2199);
            ROM_LOAD("7", 0x1800, 0x0400, 0x87d06b88);
            ROM_LOAD("8", 0x1c00, 0x0400, 0x6dfaad08);
            ROM_LOAD("9", 0x4000, 0x0400, 0x3d1a2ae3);
            ROM_LOAD("10", 0x4400, 0x0400, 0x037edb99);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 (not used, but they were on the board) */
            ROM_LOAD("cv01", 0x0000, 0x0400, 0x037e16ac);
            ROM_LOAD("cv02", 0x0400, 0x0400, 0x8263da38);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spcenctr = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("4m33.cpu", 0x0000, 0x0800, 0x7458b2db);
            ROM_LOAD("4m32.cpu", 0x0800, 0x0800, 0x1b873788);
            ROM_LOAD("4m31.cpu", 0x1000, 0x0800, 0xd4319c91);
            ROM_LOAD("4m30.cpu", 0x1800, 0x0800, 0x9b9a1a45);
            ROM_LOAD("4m29.cpu", 0x4000, 0x0800, 0x294d52ce);
            ROM_LOAD("4m28.cpu", 0x4800, 0x0800, 0xce44c923);
            ROM_LOAD("4m27.cpu", 0x5000, 0x0800, 0x098070ab);
            ROM_LOAD("4m26.cpu", 0x5800, 0x0800, 0x7f1d1f44);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_clowns = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("h2.cpu", 0x0000, 0x0400, 0xff4432eb);
            ROM_LOAD("g2.cpu", 0x0400, 0x0400, 0x676c934b);
            ROM_LOAD("f2.cpu", 0x0800, 0x0400, 0x00757962);
            ROM_LOAD("e2.cpu", 0x0c00, 0x0400, 0x9e506a36);
            ROM_LOAD("d2.cpu", 0x1000, 0x0400, 0xd61b5b47);
            ROM_LOAD("c2.cpu", 0x1400, 0x0400, 0x154d129a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_gmissile = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("gm_623.h", 0x0000, 0x0800, 0xa3ebb792);
            ROM_LOAD("gm_623.g", 0x0800, 0x0800, 0xa5e740bb);
            ROM_LOAD("gm_623.f", 0x1000, 0x0800, 0xda381025);
            ROM_LOAD("gm_623.e", 0x1800, 0x0800, 0xf350146b);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_seawolf = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sw0041.h", 0x0000, 0x0400, 0x8f597323);
            ROM_LOAD("sw0042.g", 0x0400, 0x0400, 0xdb980974);
            ROM_LOAD("sw0043.f", 0x0800, 0x0400, 0xe6ffa008);
            ROM_LOAD("sw0044.e", 0x0c00, 0x0400, 0xc3557d6a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_gunfight = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("7609h.bin", 0x0000, 0x0400, 0x0b117d73);
            ROM_LOAD("7609g.bin", 0x0400, 0x0400, 0x57bc3159);
            ROM_LOAD("7609f.bin", 0x0800, 0x0400, 0x8049a6bd);
            ROM_LOAD("7609e.bin", 0x0c00, 0x0400, 0x773264e2);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_280zzzap = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("zzzaph", 0x0000, 0x0400, 0x1fa86e1c);
            ROM_LOAD("zzzapg", 0x0400, 0x0400, 0x9639bc6b);
            ROM_LOAD("zzzapf", 0x0800, 0x0400, 0xadc6ede1);
            ROM_LOAD("zzzape", 0x0c00, 0x0400, 0x472493d6);
            ROM_LOAD("zzzapd", 0x1000, 0x0400, 0x4c240ee1);
            ROM_LOAD("zzzapc", 0x1400, 0x0400, 0x6e85aeaf);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_lupin3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("lp12.bin", 0x0000, 0x0800, 0x68a7f47a);
            ROM_LOAD("lp13.bin", 0x0800, 0x0800, 0xcae9a17b);
            ROM_LOAD("lp14.bin", 0x1000, 0x0800, 0x3553b9e4);
            ROM_LOAD("lp15.bin", 0x1800, 0x0800, 0xacbeef64);
            ROM_LOAD("lp16.bin", 0x4000, 0x0800, 0x19fcdc54);
            ROM_LOAD("lp17.bin", 0x4800, 0x0800, 0x66289ab2);
            ROM_LOAD("lp18.bin", 0x5000, 0x0800, 0x2f07b4ba);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_polaris = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("ps-01", 0x0000, 0x0800, 0xc04ce5a9);
            ROM_LOAD("ps-09", 0x0800, 0x0800, 0x9a5c8cb2);
            ROM_LOAD("ps-08", 0x1000, 0x0800, 0x8680d7ea);
            ROM_LOAD("ps-04", 0x1800, 0x0800, 0x65694948);
            ROM_LOAD("ps-05", 0x4000, 0x0800, 0x772e31f3);
            ROM_LOAD("ps-10", 0x4800, 0x0800, 0x3df77bac);

            ROM_REGION(0x0400, REGION_PROMS);
            /* background color map */
            ROM_LOAD("ps07", 0x0000, 0x0400, 0x164aa05d);

            ROM_REGION(0x0100, REGION_USER1);
            /* cloud graphics */
            ROM_LOAD("mb7052.2c", 0x0000, 0x0100, 0x2953253b);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_polarisa = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("ps01-1", 0x0000, 0x0800, 0x7d41007c);
            ROM_LOAD("ps-09", 0x0800, 0x0800, 0x9a5c8cb2);
            ROM_LOAD("ps03-1", 0x1000, 0x0800, 0x21f32415);
            ROM_LOAD("ps-04", 0x1800, 0x0800, 0x65694948);
            ROM_LOAD("ps-05", 0x4000, 0x0800, 0x772e31f3);
            ROM_LOAD("ps-10", 0x4800, 0x0800, 0x3df77bac);
            ROM_LOAD("ps26", 0x5000, 0x0800, 0x9d5c3d50);

            ROM_REGION(0x0400, REGION_PROMS);
            /* background color map */
            ROM_LOAD("ps07", 0x0000, 0x0400, 0x164aa05d);

            ROM_REGION(0x0100, REGION_USER1);
            /* cloud graphics */
            ROM_LOAD("mb7052.2c", 0x0000, 0x0100, 0x2953253b);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_lagunar = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("lagunar.h", 0x0000, 0x0800, 0x0cd5a280);
            ROM_LOAD("lagunar.g", 0x0800, 0x0800, 0x824cd6f5);
            ROM_LOAD("lagunar.f", 0x1000, 0x0800, 0x62692ca7);
            ROM_LOAD("lagunar.e", 0x1800, 0x0800, 0x20e098ed);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_m4 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("m4.h", 0x0000, 0x0800, 0x9ee2a0b5);
            ROM_LOAD("m4.g", 0x0800, 0x0800, 0x0e84b9cb);
            ROM_LOAD("m4.f", 0x1000, 0x0800, 0x9ded9956);
            ROM_LOAD("m4.e", 0x1800, 0x0800, 0xb6983238);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_phantom2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("phantom2.h", 0x0000, 0x0800, 0x0e3c2439);
            ROM_LOAD("phantom2.g", 0x0800, 0x0800, 0xe8df3e52);
            ROM_LOAD("phantom2.f", 0x1000, 0x0800, 0x30e83c6d);
            ROM_LOAD("phantom2.e", 0x1800, 0x0800, 0x8c641cac);

            ROM_REGION(0x0800, REGION_PROMS);
            /* cloud graphics */
            ROM_LOAD("p2clouds", 0x0000, 0x0800, 0xdcdd2927);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dogpatch = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("dogpatch.h", 0x0000, 0x0800, 0x74ebdf4d);
            ROM_LOAD("dogpatch.g", 0x0800, 0x0800, 0xac246f70);
            ROM_LOAD("dogpatch.f", 0x1000, 0x0800, 0xa975b011);
            ROM_LOAD("dogpatch.e", 0x1800, 0x0800, 0xc12b1f60);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_bowler = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("h.cpu", 0x0000, 0x0800, 0x74c29b93);
            ROM_LOAD("g.cpu", 0x0800, 0x0800, 0xca26d8b4);
            ROM_LOAD("f.cpu", 0x1000, 0x0800, 0xba8a0bfa);
            ROM_LOAD("e.cpu", 0x1800, 0x0800, 0x4da65a40);
            ROM_LOAD("d.cpu", 0x4000, 0x0800, 0xe7dbc9d9);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_shuffle = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("shuffle.h", 0x0000, 0x0800, 0x0d422a18);
            ROM_LOAD("shuffle.g", 0x0800, 0x0800, 0x7db7fcf9);
            ROM_LOAD("shuffle.f", 0x1000, 0x0800, 0xcd04d848);
            ROM_LOAD("shuffle.e", 0x1800, 0x0800, 0x2c118357);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_blueshrk = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("blueshrk.h", 0x0000, 0x0800, 0x4ff94187);
            ROM_LOAD("blueshrk.g", 0x0800, 0x0800, 0xe49368fd);
            ROM_LOAD("blueshrk.f", 0x1000, 0x0800, 0x86cca79d);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_einnings = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("ei.h", 0x0000, 0x0800, 0xeff9c7af);
            ROM_LOAD("ei.g", 0x0800, 0x0800, 0x5d1e66cb);
            ROM_LOAD("ei.f", 0x1000, 0x0800, 0xed96785d);
            ROM_LOAD("ei.e", 0x1800, 0x0800, 0xad096a5d);
            ROM_LOAD("ei.b", 0x5000, 0x0800, 0x56b407d4);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_dplay = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("dplay619.h", 0x0000, 0x0800, 0x6680669b);
            ROM_LOAD("dplay619.g", 0x0800, 0x0800, 0x0eec7e01);
            ROM_LOAD("dplay619.f", 0x1000, 0x0800, 0x3af4b719);
            ROM_LOAD("dplay619.e", 0x1800, 0x0800, 0x65cab4fc);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_maze = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("invaders.h", 0x0000, 0x0800, 0xf2860cff);
            ROM_LOAD("invaders.g", 0x0800, 0x0800, 0x65fad839);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_tornbase = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("tb.h", 0x0000, 0x0800, 0x653f4797);
            ROM_LOAD("tb.g", 0x0800, 0x0800, BADCRC(0x33468006));
            /* this ROM fails the test */
            ROM_LOAD("tb.f", 0x1000, 0x0800, 0x215e070c);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_checkmat = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("checkmat.h", 0x0000, 0x0400, 0x3481a6d1);
            ROM_LOAD("checkmat.g", 0x0400, 0x0400, 0xdf5fa551);
            ROM_LOAD("checkmat.f", 0x0800, 0x0400, 0x25586406);
            ROM_LOAD("checkmat.e", 0x0c00, 0x0400, 0x59330d84);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_desertgu = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("desertgu.h", 0x0000, 0x0800, 0xc0030d7c);
            ROM_LOAD("desertgu.g", 0x0800, 0x0800, 0x1ddde10b);
            ROM_LOAD("desertgu.f", 0x1000, 0x0800, 0x808e46f1);
            ROM_LOAD("desertgu.e", 0x1800, 0x0800, 0xac64dc62);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ozmawars = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("mw01", 0x0000, 0x0800, 0x31f4397d);
            ROM_LOAD("mw02", 0x0800, 0x0800, 0xd8e77c62);
            ROM_LOAD("mw03", 0x1000, 0x0800, 0x3bfa418f);
            ROM_LOAD("mw04", 0x1800, 0x0800, 0xe190ce6c);
            ROM_LOAD("mw05", 0x4000, 0x0800, 0x3bc7d4c7);
            ROM_LOAD("mw06", 0x4800, 0x0800, 0x99ca2eae);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_solfight = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("solfight.m", 0x0000, 0x0800, 0xa4f2814e);
            ROM_LOAD("solfight.n", 0x0800, 0x0800, 0x5657ec07);
            ROM_LOAD("solfight.p", 0x1000, 0x0800, 0xef9ce96d);
            ROM_LOAD("solfight.r", 0x1800, 0x0800, 0x4f1ef540);
            ROM_LOAD("mw05", 0x4000, 0x0800, 0x3bc7d4c7);
            ROM_LOAD("solfight.t", 0x4800, 0x0800, 0x3b6fb206);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spaceph = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("sv01.bin", 0x0000, 0x0400, 0xde84771d);
            ROM_LOAD("sv02.bin", 0x0400, 0x0400, 0x957fc661);
            ROM_LOAD("sv03.bin", 0x0800, 0x0400, 0xdbda38b9);
            ROM_LOAD("sv04.bin", 0x0c00, 0x0400, 0xf51544a5);
            ROM_LOAD("sv05.bin", 0x1000, 0x0400, 0x98d02683);
            ROM_LOAD("sv06.bin", 0x1400, 0x0400, 0x4ec390fd);
            ROM_LOAD("sv07.bin", 0x1800, 0x0400, 0x170862fd);
            ROM_LOAD("sv08.bin", 0x1c00, 0x0400, 0x511b12cf);
            ROM_LOAD("sv09.bin", 0x4000, 0x0400, 0xaf1cd1af);
            ROM_LOAD("sv10.bin", 0x4400, 0x0400, 0x31b7692e);
            ROM_LOAD("sv11.bin", 0x4800, 0x0400, 0x50257351);
            ROM_LOAD("sv12.bin", 0x4c00, 0x0400, 0xa2a3366a);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ballbomb = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("tn01", 0x0000, 0x0800, 0x551585b5);
            ROM_LOAD("tn02", 0x0800, 0x0800, 0x7e1f734f);
            ROM_LOAD("tn03", 0x1000, 0x0800, 0xd93e20bc);
            ROM_LOAD("tn04", 0x1800, 0x0800, 0xd0689a22);
            ROM_LOAD("tn05-1", 0x4000, 0x0800, 0x5d5e94f1);

            ROM_REGION(0x0800, REGION_PROMS);
            /* color maps player 1/player 2 */
            ROM_LOAD("tn06", 0x0000, 0x0400, 0x7ec554c4);
            ROM_LOAD("tn07", 0x0400, 0x0400, 0xdeb0ac82);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_yosakdon = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("yd1.bin", 0x0000, 0x0400, 0x607899c9);
            ROM_LOAD("yd2.bin", 0x0400, 0x0400, 0x78336df4);
            ROM_LOAD("yd3.bin", 0x0800, 0x0400, 0xc5af6d52);
            ROM_LOAD("yd4.bin", 0x0c00, 0x0400, 0xdca8064f);
            ROM_LOAD("yd5.bin", 0x1400, 0x0400, 0x38804ff1);
            ROM_LOAD("yd6.bin", 0x1800, 0x0400, 0x988d2362);
            ROM_LOAD("yd7.bin", 0x1c00, 0x0400, 0x2744e68b);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sheriff = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("f1", 0x0000, 0x0400, 0xe79df6e8);
            ROM_LOAD("f2", 0x0400, 0x0400, 0xda67721a);
            ROM_LOAD("g1", 0x0800, 0x0400, 0x3fb7888e);
            ROM_LOAD("g2", 0x0c00, 0x0400, 0x585fcfee);
            ROM_LOAD("h1", 0x1000, 0x0400, 0xe59eab52);
            ROM_LOAD("h2", 0x1400, 0x0400, 0x79e69a6a);
            ROM_LOAD("i1", 0x1800, 0x0400, 0xdda7d1e8);
            ROM_LOAD("i2", 0x1c00, 0x0400, 0x5c5f3f86);
            ROM_LOAD("j1", 0x2000, 0x0400, 0x0aa8b79a);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("basnd.u2", 0x0000, 0x0400, 0x75731745);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_bandido = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("baf1-3", 0x0000, 0x0400, 0xaec94829);
            ROM_LOAD("f2", 0x0400, 0x0400, 0xda67721a);
            ROM_LOAD("g1", 0x0800, 0x0400, 0x3fb7888e);
            ROM_LOAD("g2", 0x0c00, 0x0400, 0x585fcfee);
            ROM_LOAD("bah1-1", 0x1000, 0x0400, 0x5cb63677);
            ROM_LOAD("h2", 0x1400, 0x0400, 0x79e69a6a);
            ROM_LOAD("i1", 0x1800, 0x0400, 0xdda7d1e8);
            ROM_LOAD("i2", 0x1c00, 0x0400, 0x5c5f3f86);
            ROM_LOAD("j1", 0x2000, 0x0400, 0x0aa8b79a);
            ROM_LOAD("baj2-2", 0x2400, 0x0400, 0xa10b848a);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("basnd.u2", 0x0000, 0x0400, 0x75731745);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_helifire = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("tub.f1b", 0x0000, 0x0400, 0x032f89ca);
            ROM_LOAD("tub.f2b", 0x0400, 0x0400, 0x2774e70f);
            ROM_LOAD("tub.g1b", 0x0800, 0x0400, 0xb5ad6e8a);
            ROM_LOAD("tub.g2b", 0x0c00, 0x0400, 0x5e015bf4);
            ROM_LOAD("tub.h1b", 0x1000, 0x0400, 0x23bb4e5a);
            ROM_LOAD("tub.h2b", 0x1400, 0x0400, 0x358227c6);
            ROM_LOAD("tub.i1b", 0x1800, 0x0400, 0x0c679f44);
            ROM_LOAD("tub.i2b", 0x1c00, 0x0400, 0xd8b7a398);
            ROM_LOAD("tub.j1b", 0x2000, 0x0400, 0x98ef24db);
            ROM_LOAD("tub.j2b", 0x2400, 0x0400, 0x5e2b5877);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("tub.snd", 0x0000, 0x0400, 0x9d77a31f);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_helifira = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("f1a.bin", 0x0000, 0x0400, 0x92c9d6c1);
            ROM_LOAD("f2a.bin", 0x0400, 0x0400, 0xa264dde8);
            ROM_LOAD("tub.g1b", 0x0800, 0x0400, 0xb5ad6e8a);
            ROM_LOAD("g2a.bin", 0x0c00, 0x0400, 0xa987ebcd);
            ROM_LOAD("h1a.bin", 0x1000, 0x0400, 0x25abcaf0);
            ROM_LOAD("tub.h2b", 0x1400, 0x0400, 0x358227c6);
            ROM_LOAD("tub.i1b", 0x1800, 0x0400, 0x0c679f44);
            ROM_LOAD("i2a.bin", 0x1c00, 0x0400, 0x296610fd);
            ROM_LOAD("tub.j1b", 0x2000, 0x0400, 0x98ef24db);
            ROM_LOAD("tub.j2b", 0x2400, 0x0400, 0x5e2b5877);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("tub.snd", 0x0000, 0x0400, 0x9d77a31f);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacefev = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("tsf.f1", 0x0000, 0x0400, 0x35f295bd);
            ROM_LOAD("tsf.f2", 0x0400, 0x0400, 0x0c633f4c);
            ROM_LOAD("tsf.g1", 0x0800, 0x0400, 0xf3d851cb);
            ROM_LOAD("tsf.g2", 0x0c00, 0x0400, 0x1faef63a);
            ROM_LOAD("tsf.h1", 0x1000, 0x0400, 0xb365389d);
            ROM_LOAD("tsf.h2", 0x1400, 0x0400, 0xa36c61c9);
            ROM_LOAD("tsf.i1", 0x1800, 0x0400, 0xd4f3b50d);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("basnd.u2", 0x0000, 0x0400, 0x75731745);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sfeverbw = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("spacefev.f1", 0x0000, 0x0400, 0xb8887351);
            ROM_LOAD("spacefev.f2", 0x0400, 0x0400, 0xcda933a7);
            ROM_LOAD("spacefev.g1", 0x0800, 0x0400, 0xde17578a);
            ROM_LOAD("spacefev.g2", 0x0c00, 0x0400, 0xf1a90948);
            ROM_LOAD("spacefev.h1", 0x1000, 0x0400, 0xeefb4273);
            ROM_LOAD("spacefev.h2", 0x1400, 0x0400, 0xe91703e8);
            ROM_LOAD("spacefev.i1", 0x1800, 0x0400, 0x41e18df9);
            ROM_LOAD("spacefev.i2", 0x1c00, 0x0400, 0xeff9f82d);

            ROM_REGION(0x1000, REGION_CPU2);/* Sound 8035 + 76477 Sound Generator */
            ROM_LOAD("basnd.u2", 0x0000, 0x0400, 0x75731745);
            ROM_END();
        }
    };

    /* Midway games */
 /* board #            rom       parent    machine   inp       init (overlay/color hardware setup) */
 /* 596 */ public static GameDriver driver_seawolf = new GameDriver("1976", "seawolf", "_8080bw.java", rom_seawolf, null, machine_driver_seawolf, input_ports_seawolf, init_seawolf, ROT0, "Midway", "Sea Wolf", GAME_NO_SOUND);
    /* 597 */ public static GameDriver driver_gunfight = new GameDriver("1975", "gunfight", "_8080bw.java", rom_gunfight, null, machine_driver_gunfight, input_ports_gunfight, init_8080bw, ROT0, "Midway", "Gun Fight", GAME_NO_SOUND);
    /* 605 */ public static GameDriver driver_tornbase = new GameDriver("1976", "tornbase", "_8080bw.java", rom_tornbase, null, machine_driver_tornbase, input_ports_tornbase, init_8080bw, ROT0, "Midway", "Tornado Baseball", GAME_NO_SOUND);
    /* 610 */ public static GameDriver driver_280zzzap = new GameDriver("1976", "280zzzap", "_8080bw.java", rom_280zzzap, null, machine_driver_280zzzap, input_ports_280zzzap, init_8080bw, ROT0, "Midway", "Datsun 280 Zzzap", GAME_NO_SOUND);
    /* 611 */ public static GameDriver driver_maze = new GameDriver("1976", "maze", "_8080bw.java", rom_maze, null, machine_driver_tornbase, input_ports_maze, init_8080bw, ROT0, "Midway", "Amazing Maze", GAME_NO_SOUND);
//    /* 612 */ public static GameDriver driver_boothill = new GameDriver("1977", "boothill", "_8080bw.java", rom_boothill, null, machine_driver_boothill, input_ports_boothill, init_boothill, ROT0, "Midway", "Boot Hill");
    /* 615 */ public static GameDriver driver_checkmat = new GameDriver("1977", "checkmat", "_8080bw.java", rom_checkmat, null, machine_driver_checkmat, input_ports_checkmat, init_8080bw, ROT0, "Midway", "Checkmate", GAME_NO_SOUND);
    /* 618 */ public static GameDriver driver_desertgu = new GameDriver("1977", "desertgu", "_8080bw.java", rom_desertgu, null, machine_driver_desertgu, input_ports_desertgu, init_desertgu, ROT0, "Midway", "Desert Gun", GAME_NO_SOUND);
    /* 622 */ public static GameDriver driver_lagunar = new GameDriver("1977", "lagunar", "_8080bw.java", rom_lagunar, null, machine_driver_280zzzap, input_ports_lagunar, init_8080bw, ROT90, "Midway", "Laguna Racer", GAME_NO_SOUND);
    /* 623 */ public static GameDriver driver_gmissile = new GameDriver("1977", "gmissile", "_8080bw.java", rom_gmissile, null, machine_driver_m4, input_ports_gmissile, init_8080bw, ROT0, "Midway", "Guided Missile", GAME_NO_SOUND);
    /* 626 */ public static GameDriver driver_m4 = new GameDriver("1977", "m4", "_8080bw.java", rom_m4, null, machine_driver_m4, input_ports_m4, init_8080bw, ROT0, "Midway", "M-4", GAME_NO_SOUND);
    /* 630 */ public static GameDriver driver_clowns = new GameDriver("1978", "clowns", "_8080bw.java", rom_clowns, null, machine_driver_clowns, input_ports_clowns, init_8080bw, ROT0, "Midway", "Clowns", GAME_NO_SOUND);

    /* 640    																			"Midway", "Space Walk" */
 /* 642 */ public static GameDriver driver_einnings = new GameDriver("1978", "einnings", "_8080bw.java", rom_einnings, null, machine_driver_m4, input_ports_einnings, init_8080bw, ROT0, "Midway", "Extra Innings", GAME_NO_SOUND);
    /* 619 */ public static GameDriver driver_dplay = new GameDriver("1977", "dplay", "_8080bw.java", rom_dplay, driver_einnings, machine_driver_m4, input_ports_einnings, init_8080bw, ROT0, "Midway", "Double Play", GAME_NO_SOUND);
    /* 643 */ public static GameDriver driver_shuffle = new GameDriver("1978", "shuffle", "_8080bw.java", rom_shuffle, null, machine_driver_shuffle, input_ports_shuffle, init_8080bw, ROT90, "Midway", "Shuffleboard", GAME_NO_SOUND);
    /* 644 */ public static GameDriver driver_dogpatch = new GameDriver("1977", "dogpatch", "_8080bw.java", rom_dogpatch, null, machine_driver_clowns, input_ports_dogpatch, init_8080bw, ROT0, "Midway", "Dog Patch", GAME_NO_SOUND);
//    /* 645 */ public static GameDriver driver_spcenctr = new GameDriver("1980", "spcenctr", "_8080bw.java", rom_spcenctr, null, machine_driver_spcenctr, input_ports_spcenctr, init_spcenctr, ROT0_16BIT, "Midway", "Space Encounters", GAME_NO_SOUND);
//    /* 652 */ public static GameDriver driver_phantom2 = new GameDriver("1979", "phantom2", "_8080bw.java", rom_phantom2, null, machine_driver_m4, input_ports_phantom2, init_phantom2, ROT0, "Midway", "Phantom II", GAME_NO_SOUND);
    /* 730 */ public static GameDriver driver_bowler = new GameDriver("1978", "bowler", "_8080bw.java", rom_bowler, null, machine_driver_bowler, input_ports_bowler, init_8080bw, ROT90, "Midway", "4 Player Bowling", GAME_NO_SOUND);
    /* 739 */ public static GameDriver driver_invaders = new GameDriver("1978", "invaders", "_8080bw.java", rom_invaders, null, machine_driver_invaders, input_ports_invaders, init_invaders, ROT270, "Midway", "Space Invaders");
    /* 742 */ public static GameDriver driver_blueshrk = new GameDriver("1978", "blueshrk", "_8080bw.java", rom_blueshrk, null, machine_driver_blueshrk, input_ports_blueshrk, init_blueshrk, ROT0, "Midway", "Blue Shark", GAME_NO_SOUND);
    /* 851 */ public static GameDriver driver_invad2ct = new GameDriver("1980", "invad2ct", "_8080bw.java", rom_invad2ct, null, machine_driver_invad2ct, input_ports_invad2ct, init_invad2ct, ROT90, "Midway", "Space Invaders II (Midway, cocktail)");
    /* 870    																			"Midway", "Space Invaders Deluxe (cocktail) "*/

 /* Taito games */
    public static GameDriver driver_sitv = new GameDriver("1978", "sitv", "_8080bw.java", rom_sitv, driver_invaders, machine_driver_invaders, input_ports_sitv, init_invaders, ROT270, "Taito", "Space Invaders (TV Version)");
    public static GameDriver driver_sicv = new GameDriver("1979", "sicv", "_8080bw.java", rom_sicv, driver_invaders, machine_driver_invadpt2, input_ports_invaders, init_invadpt2, ROT270, "Taito", "Space Invaders (CV Version)");
    public static GameDriver driver_sisv = new GameDriver("1978", "sisv", "_8080bw.java", rom_sisv, driver_invaders, machine_driver_invadpt2, input_ports_invaders, init_invadpt2, ROT270, "Taito", "Space Invaders (SV Version)");
    public static GameDriver driver_sisv2 = new GameDriver("1978", "sisv2", "_8080bw.java", rom_sisv2, driver_invaders, machine_driver_invadpt2, input_ports_invaders, init_invadpt2, ROT270, "Taito", "Space Invaders (SV Version 2)");
    public static GameDriver driver_galxwars = new GameDriver("1979", "galxwars", "_8080bw.java", rom_galxwars, null, machine_driver_invaders, input_ports_galxwars, init_invaders, ROT270, "Taito", "Galaxy Wars");
    public static GameDriver driver_starw = new GameDriver("1979", "starw", "_8080bw.java", rom_starw, driver_galxwars, machine_driver_invaders, input_ports_galxwars, init_invaders, ROT270, "bootleg", "Star Wars");
    public static GameDriver driver_lrescue = new GameDriver("1979", "lrescue", "_8080bw.java", rom_lrescue, null, machine_driver_invadpt2, input_ports_lrescue, init_invadpt2, ROT270, "Taito", "Lunar Rescue");
    public static GameDriver driver_grescue = new GameDriver("1979", "grescue", "_8080bw.java", rom_grescue, driver_lrescue, machine_driver_invadpt2, input_ports_lrescue, init_invadpt2, ROT270, "Taito (Universal license?)", "Galaxy Rescue");
    public static GameDriver driver_desterth = new GameDriver("1979", "desterth", "_8080bw.java", rom_desterth, driver_lrescue, machine_driver_invadpt2, input_ports_invrvnge, init_invadpt2, ROT270, "bootleg", "Destination Earth");
    public static GameDriver driver_invadpt2 = new GameDriver("1980", "invadpt2", "_8080bw.java", rom_invadpt2, null, machine_driver_invadpt2, input_ports_invadpt2, init_invadpt2, ROT270, "Taito", "Space Invaders Part II (Taito)");
    public static GameDriver driver_schaser = new GameDriver("1980", "schaser", "_8080bw.java", rom_schaser, null, machine_driver_schaser, input_ports_schaser, init_schaser, ROT270, "Taito", "Space Chaser", GAME_IMPERFECT_SOUND | GAME_IMPERFECT_COLORS);
    public static GameDriver driver_schasrcv = new GameDriver("1979", "schasrcv", "_8080bw.java", rom_schasrcv, driver_schaser, machine_driver_lupin3, input_ports_schasrcv, init_schaser, ROT270, "Taito", "Space Chaser (CV version)", GAME_NO_SOUND | GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
//    public static GameDriver driver_lupin3 = new GameDriver("1980", "lupin3", "_8080bw.java", rom_lupin3, null, machine_driver_lupin3, input_ports_lupin3, init_lupin3, ROT270, "Taito", "Lupin III", GAME_NO_SOUND | GAME_NO_COCKTAIL);
//    public static GameDriver driver_polaris = new GameDriver("1980", "polaris", "_8080bw.java", rom_polaris, null, machine_driver_polaris, input_ports_polaris, init_polaris, ROT270, "Taito", "Polaris (set 1)", GAME_NO_SOUND);
//    public static GameDriver driver_polarisa = new GameDriver("1980", "polarisa", "_8080bw.java", rom_polarisa, driver_polaris, machine_driver_polaris, input_ports_polaris, init_polaris, ROT270, "Taito", "Polaris (set 2)", GAME_NO_SOUND);
    public static GameDriver driver_ballbomb = new GameDriver("1980", "ballbomb", "_8080bw.java", rom_ballbomb, null, machine_driver_ballbomb, input_ports_ballbomb, init_invadpt2, ROT270, "Taito", "Balloon Bomber");

    /* Nintendo games */
    public static GameDriver driver_sheriff = new GameDriver("1980", "sheriff", "_8080bw.java", rom_sheriff, null, machine_driver_sheriff, input_ports_sheriff, init_8080bw, ROT270, "Nintendo", "Sheriff", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_bandido = new GameDriver("1980", "bandido", "_8080bw.java", rom_bandido, driver_sheriff, machine_driver_sheriff, input_ports_bandido, init_8080bw, ROT270, "Exidy", "Bandido", GAME_IMPERFECT_SOUND);
 //   public static GameDriver driver_helifire = new GameDriver("1980", "helifire", "_8080bw.java", rom_helifire, null, machine_driver_helifire, input_ports_helifire, init_helifire, ROT270, "Nintendo", "HeliFire (revision B)", GAME_NO_SOUND);
 //   public static GameDriver driver_helifira = new GameDriver("1980", "helifira", "_8080bw.java", rom_helifira, driver_helifire, machine_driver_helifire, input_ports_helifire, init_helifire, ROT270, "Nintendo", "HeliFire (revision A)", GAME_NO_SOUND);
    public static GameDriver driver_spacefev = new GameDriver("1980", "spacefev", "_8080bw.java", rom_spacefev, null, machine_driver_sheriff, input_ports_spacefev, init_8080bw, ROT270, "Nintendo", "Space Fever (color)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_sfeverbw = new GameDriver("1980", "sfeverbw", "_8080bw.java", rom_sfeverbw, null, machine_driver_sheriff, input_ports_spacefev, init_8080bw, ROT270, "Nintendo", "Space Fever (black and white)", GAME_IMPERFECT_SOUND);

    /* Misc. manufacturers */
    public static GameDriver driver_earthinv = new GameDriver("1980", "earthinv", "_8080bw.java", rom_earthinv, driver_invaders, machine_driver_invaders, input_ports_earthinv, init_invaders, ROT270, "bootleg", "Super Earth Invasion");
    public static GameDriver driver_spaceatt = new GameDriver("1980", "spaceatt", "_8080bw.java", rom_spaceatt, driver_invaders, machine_driver_invaders, input_ports_spaceatt, init_invaders, ROT270, "Zenitone-Microsec Ltd", "Space Attack II");
    public static GameDriver driver_sinvzen = new GameDriver("????", "sinvzen", "_8080bw.java", rom_sinvzen, driver_invaders, machine_driver_invaders, input_ports_spaceatt, init_invaders, ROT270, "Zenitone-Microsec Ltd", "Super Invaders (Zenitone-Microsec)");
    public static GameDriver driver_sinvemag = new GameDriver("????", "sinvemag", "_8080bw.java", rom_sinvemag, driver_invaders, machine_driver_invaders, input_ports_sinvemag, init_invaders, ROT270, "bootleg", "Super Invaders (EMAG)");
    public static GameDriver driver_alieninv = new GameDriver("????", "alieninv", "_8080bw.java", rom_alieninv, driver_invaders, machine_driver_invaders, input_ports_earthinv, init_invaders, ROT270, "bootleg", "Alien Invasion Part II");
    public static GameDriver driver_spceking = new GameDriver("1978", "spceking", "_8080bw.java", rom_spceking, driver_invaders, machine_driver_invaders, input_ports_spceking, init_invaders, ROT270, "Leijac (Konami)", "Space King");
    public static GameDriver driver_spcewars = new GameDriver("1978", "spcewars", "_8080bw.java", rom_spcewars, driver_invaders, machine_driver_invaders, input_ports_invadpt2, init_invaders, ROT270, "Sanritsu", "Space War (Sanritsu)");
    public static GameDriver driver_spacewr3 = new GameDriver("1978", "spacewr3", "_8080bw.java", rom_spacewr3, driver_invaders, machine_driver_invaders, input_ports_spacewr3, init_invaders, ROT270, "bootleg", "Space War Part 3");
    public static GameDriver driver_invaderl = new GameDriver("1978", "invaderl", "_8080bw.java", rom_invaderl, driver_invaders, machine_driver_invaders, input_ports_invaders, init_invaders, ROT270, "bootleg", "Space Invaders (Logitec)");
    public static GameDriver driver_jspecter = new GameDriver("1979", "jspecter", "_8080bw.java", rom_jspecter, driver_invaders, machine_driver_invaders, input_ports_jspecter, init_invaders, ROT270, "Jatre", "Jatre Specter");
    public static GameDriver driver_cosmicmo = new GameDriver("1979", "cosmicmo", "_8080bw.java", rom_cosmicmo, driver_invaders, machine_driver_invaders, input_ports_cosmicmo, init_invaders, ROT270, "Universal", "Cosmic Monsters");
    public static GameDriver driver_superinv = new GameDriver("????", "superinv", "_8080bw.java", rom_superinv, driver_invaders, machine_driver_invaders, input_ports_invaders, init_invaders, ROT270, "bootleg", "Super Invaders");
   // public static GameDriver driver_moonbase = new GameDriver("????", "moonbase", "_8080bw.java", rom_moonbase, driver_invadpt2, machine_driver_invaders, input_ports_invadpt2, init_invaddlx, ROT270, "Nichibutsu", "Moon Base");
    public static GameDriver driver_invrvnge = new GameDriver("????", "invrvnge", "_8080bw.java", rom_invrvnge, null, machine_driver_tornbase, input_ports_invrvnge, init_invrvnge, ROT270, "Zenitone Microsec", "Invader's Revenge", GAME_NO_SOUND);
    public static GameDriver driver_invrvnga = new GameDriver("????", "invrvnga", "_8080bw.java", rom_invrvnga, driver_invrvnge, machine_driver_tornbase, input_ports_invrvnge, init_invrvnge, ROT270, "Zenitone Microsec (Dutchford license)", "Invader's Revenge (Dutchford)", GAME_NO_SOUND);
   // public static GameDriver driver_spclaser = new GameDriver("1980", "spclaser", "_8080bw.java", rom_spclaser, null, machine_driver_invaders, input_ports_spclaser, init_invaddlx, ROT270, "Game Plan, Inc. (Taito)", "Space Laser");
   // public static GameDriver driver_laser = new GameDriver("1980", "laser", "_8080bw.java", rom_laser, driver_spclaser, machine_driver_invaders, input_ports_spclaser, init_invaddlx, ROT270, "<unknown>", "Laser");
   // public static GameDriver driver_spcewarl = new GameDriver("1979", "spcewarl", "_8080bw.java", rom_spcewarl, driver_spclaser, machine_driver_invaders, input_ports_spclaser, init_invaddlx, ROT270, "Leijac (Konami)", "Space War (Leijac)");
    public static GameDriver driver_rollingc = new GameDriver("1979", "rollingc", "_8080bw.java", rom_rollingc, null, machine_driver_rollingc, input_ports_rollingc, init_rollingc, ROT270, "Nichibutsu", "Rolling Crash / Moon Base", GAME_NO_SOUND);
    public static GameDriver driver_ozmawars = new GameDriver("1979", "ozmawars", "_8080bw.java", rom_ozmawars, null, machine_driver_invaders, input_ports_ozmawars, init_8080bw, ROT270, "SNK", "Ozma Wars");
    public static GameDriver driver_solfight = new GameDriver("1979", "solfight", "_8080bw.java", rom_solfight, driver_ozmawars, machine_driver_invaders, input_ports_ozmawars, init_8080bw, ROT270, "bootleg", "Solar Fight");
    public static GameDriver driver_spaceph = new GameDriver("1979", "spaceph", "_8080bw.java", rom_spaceph, driver_ozmawars, machine_driver_invaders, input_ports_spaceph, init_8080bw, ROT270, "Zilec Games", "Space Phantoms");
    public static GameDriver driver_yosakdon = new GameDriver("1979", "yosakdon", "_8080bw.java", rom_yosakdon, null, machine_driver_tornbase, input_ports_lrescue, init_8080bw, ROT270, "bootleg", "Yosaku To Donbee (bootleg)", GAME_NO_SOUND);
   // /* 852 */ public static GameDriver driver_invaddlx = new GameDriver("1980", "invaddlx", "_8080bw.java", rom_invaddlx, driver_invadpt2, machine_driver_invaders, input_ports_invadpt2, init_invaddlx, ROT270, "Midway", "Space Invaders Deluxe");

}
