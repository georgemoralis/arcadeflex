/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 21/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.astrof.*;
//vidhrw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.astrof.*;

public class astrof {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x5fff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r),
                new MemoryReadAddress(0xa001, 0xa001, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xa003, 0xa003, tomahawk_protection_r), // Only on Tomahawk
                new MemoryReadAddress(0xd000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress astrof_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x5fff, astrof_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8003, 0x8003, MWA_RAM, astrof_color),
                new MemoryWriteAddress(0x8004, 0x8004, astrof_video_control1_w),
                new MemoryWriteAddress(0x8005, 0x8005, astrof_video_control2_w),
                new MemoryWriteAddress(0x8006, 0x8006, astrof_sample1_w),
                new MemoryWriteAddress(0x8007, 0x8007, astrof_sample2_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress tomahawk_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x5fff, tomahawk_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8003, 0x8003, MWA_RAM, astrof_color),
                new MemoryWriteAddress(0x8004, 0x8004, astrof_video_control1_w),
                new MemoryWriteAddress(0x8005, 0x8005, tomahawk_video_control2_w),
                new MemoryWriteAddress(0x8006, 0x8006, MWA_NOP), // Sound triggers
                new MemoryWriteAddress(0x8007, 0x8007, MWA_RAM, tomahawk_protection),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * These games don't have VBlank interrupts. Interrupts are still used by
     * the game: but they are related to coin slots.
     *
     **************************************************************************
     */
    public static InterruptHandlerPtr astrof_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if ((readinputport(2) & 1) != 0) /* Coin */ {
                return nmi_interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    static InputPortHandlerPtr input_ports_astrof = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            /* Player 1 Controls */
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            /* Player 2 Controls */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");

            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            /* 0x0c gives 2 Coins/1 Credit */

            PORT_DIPNAME(0x30, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "3000");
            PORT_DIPSETTING(0x10, "5000");
            PORT_DIPSETTING(0x20, "7000");
            PORT_DIPSETTING(0x30, "None");

            PORT_DIPNAME(0x40, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x40, "Hard");

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START();
            /* FAKE */
 /* The coin slots are not memory mapped. Coin insertion causes a NMI. */
 /* This fake input port is used by the interrupt */
 /* handler to be notified of coin insertions. We use IMPULSE to */
 /* trigger exactly one interrupt, without having to check when the */
 /* user releases the key. */
 /* The cabinet selector is not memory mapped, but just disables the */
 /* screen flip logic */

            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_tomahawk = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");

            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            /* 0x0c gives 2 Coins/1 Credit */

            PORT_DIPNAME(0x30, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x10, "7000");
            PORT_DIPSETTING(0x20, "10000");
            PORT_DIPSETTING(0x30, "None");

            PORT_DIPNAME(0x40, 0x00, DEF_STR("Difficulty"));
            /* Only on Tomahawk 777 */

            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x40, "Hard");

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START();
            /* FAKE */
 /* The coin slots are not memory mapped. Coin insertion causes a NMI. */
 /* This fake input port is used by the interrupt */
 /* handler to be notified of coin insertions. We use IMPULSE to */
 /* trigger exactly one interrupt, without having to check when the */
 /* user releases the key. */
 /* The cabinet selector is not memory mapped, but just disables the */
 /* screen flip logic */

            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static MachineDriver machine_driver_astrof = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        10595000 / 16, /* 0.66 Mhz */
                        readmem, astrof_writemem, null, null,
                        astrof_interrupt, 1
                )
            },
            60, 3400, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, /* screen_width, screen_height */
            new rectangle(8, 256 - 1 - 8, 8, 256 - 1 - 8), /* struct rectangle visible_area */
            null, /* no gfxdecodeinfo - bitmapped display */
            16, 0,
            astrof_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            astrof_vh_start,
            astrof_vh_stop,
            astrof_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        astrof_samples_interface
                )
            }
    );
    static MachineDriver machine_driver_tomahawk = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        10595000 / 16, /* 0.66 Mhz */
                        readmem, tomahawk_writemem, null, null,
                        astrof_interrupt, 1
                )
            },
            60, 3400, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256, 256, /* screen_width, screen_height */
            new rectangle(8, 256 - 1 - 8, 8, 256 - 1 - 8), /* struct rectangle visible_area */
            null, /* no gfxdecodeinfo - bitmapped display */
            32, 0,
            astrof_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            astrof_vh_start,
            astrof_vh_stop,
            astrof_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SAMPLES,
                        tomahawk_samples_interface
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
    static RomLoadHandlerPtr rom_astrof = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("afii.6", 0xd000, 0x0800, 0xd6cd13a4);
            ROM_LOAD("afii.5", 0xd800, 0x0800, 0x6fd3c4df);
            ROM_LOAD("afii.4", 0xe000, 0x0800, 0x9612dae3);
            ROM_LOAD("afii.3", 0xe800, 0x0800, 0x5a0fef42);
            ROM_LOAD("afii.2", 0xf000, 0x0800, 0x69f8a4fc);
            ROM_LOAD("afii.1", 0xf800, 0x0800, 0x322c09d2);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("astrf.clr", 0x0000, 0x0020, 0x61329fd1);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_astrof2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("kei2", 0xd000, 0x0400, 0x9f0bd355);
            ROM_LOAD("keii", 0xd400, 0x0400, 0x71f229f0);
            ROM_LOAD("kei0", 0xd800, 0x0400, 0x88114f7c);
            ROM_LOAD("af579.08", 0xdc00, 0x0400, 0x9793c124);
            ROM_LOAD("ke8", 0xe000, 0x0400, 0x08e44b12);
            ROM_LOAD("ke7", 0xe400, 0x0400, 0x8a42d62c);
            ROM_LOAD("ke6", 0xe800, 0x0400, 0x3e9aa743);
            ROM_LOAD("ke5", 0xec00, 0x0400, 0x712a4557);
            ROM_LOAD("ke4", 0xf000, 0x0400, 0xad06f306);
            ROM_LOAD("ke3", 0xf400, 0x0400, 0x680b91b4);
            ROM_LOAD("ke2", 0xf800, 0x0400, 0x2c4cab1a);
            ROM_LOAD("af583.00", 0xfc00, 0x0400, 0xf699dda3);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("astrf.clr", 0x0000, 0x0020, 0x61329fd1);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_astrof3 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("kei2", 0xd000, 0x0400, 0x9f0bd355);
            ROM_LOAD("keii", 0xd400, 0x0400, 0x71f229f0);
            ROM_LOAD("kei0", 0xd800, 0x0400, 0x88114f7c);
            ROM_LOAD("ke9", 0xdc00, 0x0400, 0x29cbaea6);
            ROM_LOAD("ke8", 0xe000, 0x0400, 0x08e44b12);
            ROM_LOAD("ke7", 0xe400, 0x0400, 0x8a42d62c);
            ROM_LOAD("ke6", 0xe800, 0x0400, 0x3e9aa743);
            ROM_LOAD("ke5", 0xec00, 0x0400, 0x712a4557);
            ROM_LOAD("ke4", 0xf000, 0x0400, 0xad06f306);
            ROM_LOAD("ke3", 0xf400, 0x0400, 0x680b91b4);
            ROM_LOAD("ke2", 0xf800, 0x0400, 0x2c4cab1a);
            ROM_LOAD("kei", 0xfc00, 0x0400, 0xfce4718d);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("astrf.clr", 0x0000, 0x0020, 0x61329fd1);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_tomahawk = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("l8-1", 0xdc00, 0x0400, 0x7c911661);
            ROM_LOAD("l7-1", 0xe000, 0x0400, 0xadeffb69);
            ROM_LOAD("l6-1", 0xe400, 0x0400, 0x9116e59d);
            ROM_LOAD("l5-1", 0xe800, 0x0400, 0x01e4c7c4);
            ROM_LOAD("l4-1", 0xec00, 0x0400, 0xd9f69cb0);
            ROM_LOAD("l3-1", 0xf000, 0x0400, 0x7ce7183f);
            ROM_LOAD("l2-1", 0xf400, 0x0400, 0x43fea29d);
            ROM_LOAD("l1-1", 0xf800, 0x0400, 0xf2096ba9);
            ROM_LOAD("l0-1", 0xfc00, 0x0400, 0x42edbc28);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("t777.clr", 0x0000, 0x0020, 0xd6a528fd);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_tomahaw5 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("thawk.l8", 0xdc00, 0x0400, 0xb01dab4b);
            ROM_LOAD("thawk.l7", 0xe000, 0x0400, 0x3a6549e8);
            ROM_LOAD("thawk.l6", 0xe400, 0x0400, 0x863e47f7);
            ROM_LOAD("thawk.l5", 0xe800, 0x0400, 0xde0183bc);
            ROM_LOAD("thawk.l4", 0xec00, 0x0400, 0x11e9c7ea);
            ROM_LOAD("thawk.l3", 0xf000, 0x0400, 0xec44d388);
            ROM_LOAD("thawk.l2", 0xf400, 0x0400, 0xdc0a0f54);
            ROM_LOAD("thawk.l1", 0xf800, 0x0400, 0x1d9dab9c);
            ROM_LOAD("thawk.l0", 0xfc00, 0x0400, 0xd21a1eba);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("t777.clr", 0x0000, 0x0020, 0xd6a528fd);
            ROM_END();
        }
    };

    public static GameDriver driver_astrof = new GameDriver("1980", "astrof", "astrof.java", rom_astrof, null, machine_driver_astrof, input_ports_astrof, null, ROT90, "Data East", "Astro Fighter (set 1)");
    public static GameDriver driver_astrof2 = new GameDriver("1980", "astrof2", "astrof.java", rom_astrof2, driver_astrof, machine_driver_astrof, input_ports_astrof, null, ROT90, "Data East", "Astro Fighter (set 2)");
    public static GameDriver driver_astrof3 = new GameDriver("1980", "astrof3", "astrof.java", rom_astrof3, driver_astrof, machine_driver_astrof, input_ports_astrof, null, ROT90, "Data East", "Astro Fighter (set 3)");
    public static GameDriver driver_tomahawk = new GameDriver("1980", "tomahawk", "astrof.java", rom_tomahawk, null, machine_driver_tomahawk, input_ports_tomahawk, null, ROT90, "Data East", "Tomahawk 777 (Revision 1)");
    public static GameDriver driver_tomahaw5 = new GameDriver("1980", "tomahaw5", "astrof.java", rom_tomahaw5, driver_tomahawk, machine_driver_tomahawk, input_ports_tomahawk, null, ROT90, "Data East", "Tomahawk 777 (Revision 5)");
}
