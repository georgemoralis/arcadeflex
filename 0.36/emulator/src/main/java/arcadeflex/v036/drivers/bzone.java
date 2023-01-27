/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
/**
 * Changelog
 * =========
 * 27/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.mathbox.*;
import static arcadeflex.v036.machine.atari_vg.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inputH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.bzone.*;
import static arcadeflex.v036.sndhrdw.redbaron.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.avgdvg.*;
import static arcadeflex.v036.vidhrdw.vector.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import gr.codebb.arcadeflex.v036.sound.pokeyH.*;

public class bzone {

    public static int IN0_3KHZ = (1 << 7);
    public static int IN0_VG_HALT = (1 << 6);

    public static ReadHandlerPtr bzone_IN0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = readinputport(0);

            if ((cpu_gettotalcycles() & 0x100) != 0) {
                res |= IN0_3KHZ;
            } else {
                res &= ~IN0_3KHZ;
            }

            if (avgdvg_done() != 0) {
                res |= IN0_VG_HALT;
            } else {
                res &= ~IN0_VG_HALT;
            }

            return res;
        }
    };

    /* Translation table for one-joystick emulation */
    static int one_joy_trans[] = {
        0x00, 0x0A, 0x05, 0x00, 0x06, 0x02, 0x01, 0x00,
        0x09, 0x08, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00};

    public static ReadHandlerPtr bzone_IN3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res, res1;

            res = readinputport(3);
            res1 = readinputport(4);

            res |= one_joy_trans[res1 & 0x1f];

            return (res);
        }
    };

    public static InterruptHandlerPtr bzone_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if ((readinputport(0) & 0x10) != 0) {
                return nmi_interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static int rb_input_select;

    public static ReadHandlerPtr redbaron_joy_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (rb_input_select != 0) {
                return readinputport(5);
            } else {
                return readinputport(6);
            }
        }
    };

    static MemoryReadAddress bzone_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0800, bzone_IN0_r), /* IN0 */
                new MemoryReadAddress(0x0a00, 0x0a00, input_port_1_r), /* DSW1 */
                new MemoryReadAddress(0x0c00, 0x0c00, input_port_2_r), /* DSW2 */
                new MemoryReadAddress(0x1800, 0x1800, mb_status_r),
                new MemoryReadAddress(0x1810, 0x1810, mb_lo_r),
                new MemoryReadAddress(0x1818, 0x1818, mb_hi_r),
                new MemoryReadAddress(0x1820, 0x182f, pokey1_r),
                new MemoryReadAddress(0x2000, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x5000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bzone_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x1000, coin_counter_w),
                new MemoryWriteAddress(0x1200, 0x1200, avgdvg_go),
                new MemoryWriteAddress(0x1400, 0x1400, watchdog_reset_w),
                new MemoryWriteAddress(0x1600, 0x1600, avgdvg_reset),
                new MemoryWriteAddress(0x1820, 0x182f, pokey1_w),
                new MemoryWriteAddress(0x1840, 0x1840, bzone_sounds_w),
                new MemoryWriteAddress(0x1860, 0x187f, mb_go),
                new MemoryWriteAddress(0x2000, 0x2fff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x3000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x5000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_bzone = new InputPortHandlerPtr() {
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
 /* handled by bzone_IN0_r() */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* bit 7 is tied to a 3kHz clock */
 /* handled by bzone_IN0_r() */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x03, "5");
            PORT_DIPNAME(0x0c, 0x04, "Missile appears at");
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x04, "10000");
            PORT_DIPSETTING(0x08, "20000");
            PORT_DIPSETTING(0x0c, "30000");
            PORT_DIPNAME(0x30, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x10, "15k and 100k");
            PORT_DIPSETTING(0x20, "20k and 100k");
            PORT_DIPSETTING(0x30, "50k and 100k");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xc0, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x40, "German");
            PORT_DIPSETTING(0x80, "French");
            PORT_DIPSETTING(0xc0, "Spanish");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
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
            PORT_DIPSETTING(0x80, "6 credits/5 coins");

            PORT_START();
            /* IN3 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_DOWN | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICKRIGHT_UP | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_DOWN | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICKLEFT_UP | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* fake port for single joystick control */
 /* This fake port is handled via bzone_IN3_r */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_CHEAT);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_CHEAT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_CHEAT);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_CHEAT);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_CHEAT);
            INPUT_PORTS_END();
        }
    };

    static MemoryReadAddress redbaron_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0800, bzone_IN0_r), /* IN0 */
                new MemoryReadAddress(0x0a00, 0x0a00, input_port_1_r), /* DSW1 */
                new MemoryReadAddress(0x0c00, 0x0c00, input_port_2_r), /* DSW2 */
                new MemoryReadAddress(0x1800, 0x1800, mb_status_r),
                new MemoryReadAddress(0x1802, 0x1802, input_port_4_r), /* IN4 */
                new MemoryReadAddress(0x1804, 0x1804, mb_lo_r),
                new MemoryReadAddress(0x1806, 0x1806, mb_hi_r),
                new MemoryReadAddress(0x1810, 0x181f, pokey1_r),
                new MemoryReadAddress(0x1820, 0x185f, atari_vg_earom_r),
                new MemoryReadAddress(0x2000, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x5000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress redbaron_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x1000, MWA_NOP), /* coin out */
                new MemoryWriteAddress(0x1200, 0x1200, avgdvg_go),
                new MemoryWriteAddress(0x1400, 0x1400, MWA_NOP), /* watchdog clear */
                new MemoryWriteAddress(0x1600, 0x1600, avgdvg_reset),
                new MemoryWriteAddress(0x1808, 0x1808, redbaron_sounds_w), /* and select joystick pot also */
                new MemoryWriteAddress(0x180a, 0x180a, MWA_NOP), /* sound reset, yet todo */
                new MemoryWriteAddress(0x180c, 0x180c, atari_vg_earom_ctrl),
                new MemoryWriteAddress(0x1810, 0x181f, pokey1_w),
                new MemoryWriteAddress(0x1820, 0x185f, atari_vg_earom_w),
                new MemoryWriteAddress(0x1860, 0x187f, mb_go),
                new MemoryWriteAddress(0x2000, 0x2fff, MWA_RAM, vectorram, vectorram_size),
                new MemoryWriteAddress(0x3000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x5000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_redbaron = new InputPortHandlerPtr() {
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
 /* handled by bzone_IN0_r() */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            /* bit 7 is tied to a 3kHz clock */
 /* handled by bzone_IN0_r() */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW0 */
 /* See the table above if you are really interested */
            PORT_DIPNAME(0xff, 0xfd, DEF_STR("Coinage"));
            PORT_DIPSETTING(0xfd, "Normal");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x03, "Language");
            PORT_DIPSETTING(0x00, "German");
            PORT_DIPSETTING(0x01, "French");
            PORT_DIPSETTING(0x02, "Spanish");
            PORT_DIPSETTING(0x03, "English");
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0c, "2k 10k 30k");
            PORT_DIPSETTING(0x08, "4k 15k 40k");
            PORT_DIPSETTING(0x04, "6k 20k 50k");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x30, 0x20, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "2");
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x40, 0x40, "One Play Minimum");
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Self Adjust Diff");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            /* IN3 - the real machine reads either the X or Y axis from this port */
 /* Instead, we use the two fake 5 & 6 ports and bank-switch the proper */
 /* value based on the lsb of the byte written to the sound port */
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);

            PORT_START();
            /* IN4 - misc controls */
            PORT_BIT(0x3f, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1);

            /* These 2 are fake - they are bank-switched from reads to IN3 */
 /* Red Baron doesn't seem to use the full 0-255 range. */
            PORT_START();
            /* IN5 */
            PORT_ANALOG(0xff, 0x80, IPT_AD_STICK_X, 25, 10, 64, 192);

            PORT_START();
            /* IN6 */
            PORT_ANALOG(0xff, 0x80, IPT_AD_STICK_Y, 25, 10, 64, 192);
            INPUT_PORTS_END();
        }
    };

    static POKEYinterface bzone_pokey_interface = new POKEYinterface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz??? */
            new int[]{100},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            /* The allpot handler */
            new ReadHandlerPtr[]{bzone_IN3_r}
    );

    static CustomSound_interface bzone_custom_interface = new CustomSound_interface(
            bzone_sh_start,
            bzone_sh_stop,
            bzone_sh_update
    );

    static MachineDriver machine_driver_bzone = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        bzone_readmem, bzone_writemem, null, null,
                        bzone_interrupt, 6 /* 4.1ms */
                )
            },
            40, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 580, 0, 400),
            null,
            256, 256,
            avg_init_palette_bzone,
            VIDEO_TYPE_VECTOR,
            null,
            avg_start_bzone,
            avg_stop,
            avg_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        bzone_pokey_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM,
                        bzone_custom_interface
                )
            }
    );

    static POKEYinterface redbaron_pokey_interface = new POKEYinterface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz??? */
            new int[]{100},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            /* The allpot handler */
            new ReadHandlerPtr[]{redbaron_joy_r}
    );

    static CustomSound_interface redbaron_custom_interface = new CustomSound_interface(
            redbaron_sh_start,
            redbaron_sh_stop,
            redbaron_sh_update
    );

    static MachineDriver machine_driver_redbaron = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz */
                        redbaron_readmem, redbaron_writemem, null, null,
                        bzone_interrupt, 4 /* 5.4ms */
                )
            },
            45, 0, /* frames per second, vblank duration (vector game, so no vblank) */
            1,
            null,
            /* video hardware */
            400, 300, new rectangle(0, 520, 0, 400),
            null,
            256, 256,
            avg_init_palette_aqua,
            VIDEO_TYPE_VECTOR,
            null,
            avg_start_redbaron,
            avg_stop,
            avg_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        redbaron_pokey_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM,
                        redbaron_custom_interface
                )
            },
            atari_vg_earom_handler
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_bzone = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("036414.01", 0x5000, 0x0800, 0xefbc3fa0);
            ROM_LOAD("036413.01", 0x5800, 0x0800, 0x5d9d9111);
            ROM_LOAD("036412.01", 0x6000, 0x0800, 0xab55cbd2);
            ROM_LOAD("036411.01", 0x6800, 0x0800, 0xad281297);
            ROM_LOAD("036410.01", 0x7000, 0x0800, 0x0b7bfaa4);
            ROM_LOAD("036409.01", 0x7800, 0x0800, 0x1e14e919);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Mathbox ROMs */
            ROM_LOAD("036422.01", 0x3000, 0x0800, 0x7414177b);
            ROM_LOAD("036421.01", 0x3800, 0x0800, 0x8ea8f939);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_bzone2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("036414a.01", 0x5000, 0x0800, 0x13de36d5);
            ROM_LOAD("036413.01", 0x5800, 0x0800, 0x5d9d9111);
            ROM_LOAD("036412.01", 0x6000, 0x0800, 0xab55cbd2);
            ROM_LOAD("036411.01", 0x6800, 0x0800, 0xad281297);
            ROM_LOAD("036410.01", 0x7000, 0x0800, 0x0b7bfaa4);
            ROM_LOAD("036409.01", 0x7800, 0x0800, 0x1e14e919);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Mathbox ROMs */
            ROM_LOAD("036422.01", 0x3000, 0x0800, 0x7414177b);
            ROM_LOAD("036421.01", 0x3800, 0x0800, 0x8ea8f939);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_redbaron = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("037587.01", 0x4800, 0x0800, 0x60f23983);
            ROM_CONTINUE(0x5800, 0x0800);
            ROM_LOAD("037000.01e", 0x5000, 0x0800, 0x69bed808);
            ROM_LOAD("036998.01e", 0x6000, 0x0800, 0xd1104dd7);
            ROM_LOAD("036997.01e", 0x6800, 0x0800, 0x7434acb4);
            ROM_LOAD("036996.01e", 0x7000, 0x0800, 0xc0e7589e);
            ROM_LOAD("036995.01e", 0x7800, 0x0800, 0xad81d1da);
            ROM_RELOAD(0xf800, 0x0800);/* for reset/interrupt vectors */
 /* Mathbox ROMs */
            ROM_LOAD("037006.01e", 0x3000, 0x0800, 0x9fcffea0);
            ROM_LOAD("037007.01e", 0x3800, 0x0800, 0x60250ede);
            ROM_END();
        }
    };

    public static GameDriver driver_bzone = new GameDriver("1980", "bzone", "bzone.java", rom_bzone, null, machine_driver_bzone, input_ports_bzone, null, ROT0, "Atari", "Battle Zone (set 1)");
    public static GameDriver driver_bzone2 = new GameDriver("1980", "bzone2", "bzone.java", rom_bzone2, driver_bzone, machine_driver_bzone, input_ports_bzone, null, ROT0, "Atari", "Battle Zone (set 2)");
    public static GameDriver driver_redbaron = new GameDriver("1980", "redbaron", "bzone.java", rom_redbaron, null, machine_driver_redbaron, input_ports_redbaron, null, ROT0, "Atari", "Red Baron");
}
