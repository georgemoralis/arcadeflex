/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
/**
 * Changelog
 * =========
 * 04/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.i8039.i8039H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sndhrdw imports
import static arcadeflex.v036.sndhrdw.spacefb.*;
//sound imports
import static arcadeflex.v036.sound.dac.*;
import static arcadeflex.v036.sound.dacH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.spacefb.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//to be organized

public class spacefb {

    public static InterruptHandlerPtr spacefb_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if (cpu_getiloops() != 0) {
                return (0x00cf);
                /* RST 08h */
            } else {
                return (0x00d7);
                /* RST 10h */
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r), /* IN 0 */
                new IOReadPort(0x01, 0x01, input_port_1_r), /* IN 1 */
                new IOReadPort(0x02, 0x02, input_port_2_r), /* Coin - Start */
                new IOReadPort(0x03, 0x03, input_port_3_r), /* DSW0 */
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, spacefb_video_control_w),
                new IOWritePort(0x01, 0x01, spacefb_port_1_w),
                new IOWritePort(0x02, 0x02, spacefb_port_2_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0x03ff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport_sound[]
            = {
                new IOReadPort(I8039_p2, I8039_p2, spacefb_sh_getp2),
                new IOReadPort(I8039_t0, I8039_t0, spacefb_sh_gett0),
                new IOReadPort(I8039_t1, I8039_t1, spacefb_sh_gett1),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport_sound[]
            = {
                new IOWritePort(I8039_p1, I8039_p1, DAC_data_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_spacefb = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* Coin - Start */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Test ? */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x10, "8000");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x20, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0xc0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /* Same as Space Firebird, except for the difficulty switch */
    static InputPortHandlerPtr input_ports_spacedem = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* Coin - Start */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Test ? */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN1);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x10, "8000");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x20, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_BIT(0xc0, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout spritelayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    /*
	 * The bullests are stored in a 256x4bit PROM but the .bin file is
	 * 256*8bit
     */

    static GfxLayout bulletlayout = new GfxLayout(
            4, 4, /* 4*4 characters */
            64, /* 64 characters */
            1, /* 1 bits per pixel */
            new int[]{0},
            new int[]{4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8},
            4 * 8 /* every char takes 4 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(REGION_GFX2, 0, bulletlayout, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{100}
    );

    static MachineDriver machine_driver_spacefb = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz? */
                        readmem, writemem, readport, writeport,
                        spacefb_interrupt, 2 /* two int's per frame */
                ),
                new MachineCPU(
                        CPU_I8035 | CPU_AUDIO_CPU,
                        6000000 / 15,
                        readmem_sound, writemem_sound, readport_sound, writeport_sound,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            3,
            null,
            /* video hardware */
            /* there is no real character graphics, only 8*8 and 4*4 sprites */
            264, 256, new rectangle(0, 263, 16, 247),
            gfxdecodeinfo,
            32, 32,
            spacefb_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            generic_vh_start,
            generic_vh_stop,
            spacefb_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
                )
            }
    );

    static RomLoadHandlerPtr rom_spacefb = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("5e.cpu", 0x0000, 0x0800, 0x2d406678);
            /* Code */
            ROM_LOAD("5f.cpu", 0x0800, 0x0800, 0x89f0c34a);
            ROM_LOAD("5h.cpu", 0x1000, 0x0800, 0xc4bcac3e);
            ROM_LOAD("5i.cpu", 0x1800, 0x0800, 0x61c00a65);
            ROM_LOAD("5j.cpu", 0x2000, 0x0800, 0x598420b9);
            ROM_LOAD("5k.cpu", 0x2800, 0x0800, 0x1713300c);
            ROM_LOAD("5m.cpu", 0x3000, 0x0800, 0x6286f534);
            ROM_LOAD("5n.cpu", 0x3800, 0x0800, 0x1c9f91ee);

            ROM_REGION(0x1000, REGION_CPU2);/* sound */
            ROM_LOAD("ic20.snd", 0x0000, 0x0400, 0x1c8670b3);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5k.vid", 0x0000, 0x0800, 0x236e1ff7);
            ROM_LOAD("6k.vid", 0x0800, 0x0800, 0xbf901a4e);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4i.vid", 0x0000, 0x0100, 0x528e8533);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mb7051.3n", 0x0000, 0x0020, 0x465d07af);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacefbg = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("tst-c.5e", 0x0000, 0x0800, 0x07949110);
            /* Code */
            ROM_LOAD("tst-c.5f", 0x0800, 0x0800, 0xce591929);
            ROM_LOAD("tst-c.5h", 0x1000, 0x0800, 0x55d34ea5);
            ROM_LOAD("tst-c.5i", 0x1800, 0x0800, 0xa11e2881);
            ROM_LOAD("tst-c.5j", 0x2000, 0x0800, 0xa6aff352);
            ROM_LOAD("tst-c.5k", 0x2800, 0x0800, 0xf4213603);
            ROM_LOAD("5m.cpu", 0x3000, 0x0800, 0x6286f534);
            ROM_LOAD("5n.cpu", 0x3800, 0x0800, 0x1c9f91ee);

            ROM_REGION(0x1000, REGION_CPU2);/* sound */
            ROM_LOAD("ic20.snd", 0x0000, 0x0400, 0x1c8670b3);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("tst-v.5k", 0x0000, 0x0800, 0xbacc780d);
            ROM_LOAD("tst-v.6k", 0x0800, 0x0800, 0x1645ff26);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4i.vid", 0x0000, 0x0100, 0x528e8533);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mb7051.3n", 0x0000, 0x0020, 0x465d07af);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacebrd = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sb5e.cpu", 0x0000, 0x0800, 0x232d66b8);
            /* Code */
            ROM_LOAD("sb5f.cpu", 0x0800, 0x0800, 0x99504327);
            ROM_LOAD("sb5h.cpu", 0x1000, 0x0800, 0x49a26fe5);
            ROM_LOAD("sb5i.cpu", 0x1800, 0x0800, 0xc23025da);
            ROM_LOAD("sb5j.cpu", 0x2000, 0x0800, 0x5e97baf0);
            ROM_LOAD("5k.cpu", 0x2800, 0x0800, 0x1713300c);
            ROM_LOAD("sb5m.cpu", 0x3000, 0x0800, 0x4cbe92fc);
            ROM_LOAD("sb5n.cpu", 0x3800, 0x0800, 0x1a798fbf);

            ROM_REGION(0x1000, REGION_CPU2);/* sound */
            ROM_LOAD("ic20.snd", 0x0000, 0x0400, 0x1c8670b3);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5k.vid", 0x0000, 0x0800, 0x236e1ff7);
            ROM_LOAD("6k.vid", 0x0800, 0x0800, 0xbf901a4e);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4i.vid", 0x0000, 0x0100, 0x528e8533);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("spcbird.clr", 0x0000, 0x0020, 0x25c79518);
            ROM_END();
        }
    };

    /* only a few bytes are different between this and spacebrd above */
    static RomLoadHandlerPtr rom_spacefbb = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("fc51", 0x0000, 0x0800, 0x5657bd2f);
            /* Code */
            ROM_LOAD("fc52", 0x0800, 0x0800, 0x303b0294);
            ROM_LOAD("sb5h.cpu", 0x1000, 0x0800, 0x49a26fe5);
            ROM_LOAD("sb5i.cpu", 0x1800, 0x0800, 0xc23025da);
            ROM_LOAD("fc55", 0x2000, 0x0800, 0x946bee5d);
            ROM_LOAD("5k.cpu", 0x2800, 0x0800, 0x1713300c);
            ROM_LOAD("sb5m.cpu", 0x3000, 0x0800, 0x4cbe92fc);
            ROM_LOAD("sb5n.cpu", 0x3800, 0x0800, 0x1a798fbf);

            ROM_REGION(0x1000, REGION_CPU2);/* sound */
            ROM_LOAD("fb.snd", 0x0000, 0x0400, 0xf7a59492);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("fc59", 0x0000, 0x0800, 0xa00ad16c);
            ROM_LOAD("6k.vid", 0x0800, 0x0800, 0xbf901a4e);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4i.vid", 0x0000, 0x0100, 0x528e8533);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mb7051.3n", 0x0000, 0x0020, 0x465d07af);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_spacedem = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sd5e.cpu", 0x0000, 0x0800, 0xbe4b9cbb);
            /* Code */
            ROM_LOAD("sd5f.cpu", 0x0800, 0x0800, 0x0814f964);
            ROM_LOAD("sd5h.cpu", 0x1000, 0x0800, 0xebfff682);
            ROM_LOAD("sd5i.cpu", 0x1800, 0x0800, 0xdd7e1378);
            ROM_LOAD("sd5j.cpu", 0x2000, 0x0800, 0x98334fda);
            ROM_LOAD("sd5k.cpu", 0x2800, 0x0800, 0xba4933b2);
            ROM_LOAD("sd5m.cpu", 0x3000, 0x0800, 0x14d3c656);
            ROM_LOAD("sd5n.cpu", 0x3800, 0x0800, 0x7e0e41b0);

            ROM_REGION(0x1000, REGION_CPU2);/* sound */
            ROM_LOAD("ic20.snd", 0x0000, 0x0400, 0x00000000);
            /* This ROM wasn't in the set. Using Space Firebird's */

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sd5k.vid", 0x0000, 0x0800, 0x55758e4d);
            ROM_LOAD("sd6k.vid", 0x0800, 0x0800, 0x3fcbb20c);

            ROM_REGION(0x0100, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("4i.vid", 0x0000, 0x0100, 0x00000000);
            /* This ROM wasn't in the set. Using Space Firebird's */

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("mb7051.3n", 0x0000, 0x0020, 0x00000000);
            /* This ROM wasn't in the set. Using Space Firebird's */
            ROM_END();
        }
    };

    public static GameDriver driver_spacefb = new GameDriver("1980", "spacefb", "spacefb.java", rom_spacefb, null, machine_driver_spacefb, input_ports_spacefb, null, ROT90, "Nintendo", "Space Firebird (Nintendo)", GAME_IMPERFECT_COLORS | GAME_IMPERFECT_SOUND);
    public static GameDriver driver_spacefbg = new GameDriver("1980", "spacefbg", "spacefb.java", rom_spacefbg, driver_spacefb, machine_driver_spacefb, input_ports_spacefb, null, ROT90, "Gremlin", "Space Firebird (Gremlin)", GAME_IMPERFECT_COLORS | GAME_IMPERFECT_SOUND);
    public static GameDriver driver_spacebrd = new GameDriver("1980", "spacebrd", "spacefb.java", rom_spacebrd, driver_spacefb, machine_driver_spacefb, input_ports_spacefb, null, ROT90, "bootleg", "Space Bird (bootleg)", GAME_IMPERFECT_COLORS | GAME_IMPERFECT_SOUND);
    public static GameDriver driver_spacefbb = new GameDriver("1980", "spacefbb", "spacefb.java", rom_spacefbb, driver_spacefb, machine_driver_spacefb, input_ports_spacefb, null, ROT90, "bootleg", "Space Firebird (bootleg)", GAME_IMPERFECT_COLORS | GAME_IMPERFECT_SOUND);
    public static GameDriver driver_spacedem = new GameDriver("1980", "spacedem", "spacefb.java", rom_spacedem, driver_spacefb, machine_driver_spacefb, input_ports_spacedem, null, ROT90, "Nintendo / Fortrek", "Space Demon", GAME_IMPERFECT_COLORS | GAME_IMPERFECT_SOUND);
}
