/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.cop01.*;
//TODO
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;

public class cop01 {

    public static WriteHandlerPtr cop01_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, 0xff);
        }
    };
    static int pulse;
    public static final int TIMER_RATE = 12000;
    /* total guess */
    public static ReadHandlerPtr cop01_sound_command_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = (soundlatch_r.handler(offset) & 0x7f) << 1;

            /* bit 0 seems to be a timer */
            if (((cpu_gettotalcycles() / TIMER_RATE) & 1) != 0) {
                if (pulse == 0) {
                    res |= 1;
                }
                pulse = 1;
            } else {
                pulse = 0;
            }

            return res;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe0ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd7ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd800, 0xdfff, colorram_w, colorram),
                new MemoryWriteAddress(0xe000, 0xe0ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xf000, 0xf3ff, MWA_RAM, cop01_videoram, cop01_videoram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x01, 0x01, input_port_1_r),
                new IOReadPort(0x02, 0x02, input_port_2_r),
                new IOReadPort(0x03, 0x03, input_port_3_r),
                new IOReadPort(0x04, 0x04, input_port_4_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x40, 0x40, cop01_gfxbank_w),
                new IOWritePort(0x41, 0x42, cop01_scrollx_w),
                new IOWritePort(0x44, 0x44, cop01_sound_command_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(-1)
            };

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x06, 0x06, cop01_sound_command_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_write_port_0_w),
                new IOWritePort(0x02, 0x02, AY8910_control_port_1_w),
                new IOWritePort(0x03, 0x03, AY8910_write_port_1_w),
                new IOWritePort(0x04, 0x04, AY8910_control_port_2_w),
                new IOWritePort(0x05, 0x05, AY8910_write_port_2_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_cop01 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* TEST, COIN, START */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_SERVICE(0x20, IP_ACTIVE_LOW);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x01, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Lives"));
            PORT_DIPSETTING(0x0c, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x08, "5");
            PORT_DIPSETTING(0x00, "6");
            PORT_DIPNAME(0x10, 0x10, "1st Bonus Life");
            PORT_DIPSETTING(0x10, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPNAME(0x60, 0x60, "2nd Bonus Life");
            PORT_DIPSETTING(0x60, "30000");
            PORT_DIPSETTING(0x20, "50000");
            PORT_DIPSETTING(0x40, "100000");
            PORT_DIPSETTING(0x00, "150000");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{4, 0, 12, 8, 20, 16, 28, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{4, 0, 12, 8, 20, 16, 28, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* plane offset */
            new int[]{4, 0, 4 + 512 * 64 * 8, 0 + 512 * 64 * 8, 12, 8, 12 + 512 * 64 * 8, 8 + 512 * 64 * 8,
                20, 16, 20 + 512 * 64 * 8, 16 + 512 * 64 * 8, 28, 24, 28 + 512 * 64 * 8, 24 + 512 * 64 * 8},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32},
            64 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* ?? */
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 16 * 16, 4),
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 16 * 16 + 4 * 16, 16),
                new GfxDecodeInfo(-1)
            };

    static AY8910interface ay8910_interface = new AY8910interface(
            3, /* 3 chips */
            1500000, /* 1.5 MHz?????? */
            new int[]{25, 25, 25},
            new ReadHandlerPtr[]{null, null, null},
            new ReadHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null}
    );

    static MachineDriver machine_driver_cop01 = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3500000, /* 3.5 MHz (?) */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3.0 MHz (?) */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 0 /* IRQs are caused by the main CPU */
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 16 * 16 + 4 * 16 + 16 * 16,
            cop01_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            cop01_vh_start,
            cop01_vh_stop,
            cop01_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
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
    static RomLoadHandlerPtr rom_cop01 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("cop01.2b", 0x0000, 0x4000, 0x5c2734ab);
            ROM_LOAD("cop02.4b", 0x4000, 0x4000, 0x9c7336ef);
            ROM_LOAD("cop03.5b", 0x8000, 0x4000, 0x2566c8bf);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for code */
            ROM_LOAD("cop15.17b", 0x0000, 0x4000, 0x6a5f08fa);
            ROM_LOAD("cop16.18b", 0x4000, 0x4000, 0x56bf6946);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop14.15g", 0x00000, 0x2000, 0x066d1c55);/* chars */

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop04.15c", 0x00000, 0x4000, 0x622d32e6);/* tiles */
            ROM_LOAD("cop05.16c", 0x04000, 0x4000, 0xc6ac5a35);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop10.3e", 0x00000, 0x2000, 0x444cb19d);/* sprites */
            ROM_LOAD("cop11.5e", 0x02000, 0x2000, 0x9078bc04);
            ROM_LOAD("cop12.6e", 0x04000, 0x2000, 0x257a6706);
            ROM_LOAD("cop13.8e", 0x06000, 0x2000, 0x07c4ea66);
            ROM_LOAD("cop06.3g", 0x08000, 0x2000, 0xf1c1f4a5);
            ROM_LOAD("cop07.5g", 0x0a000, 0x2000, 0x11db7b72);
            ROM_LOAD("cop08.6g", 0x0c000, 0x2000, 0xa63ddda6);
            ROM_LOAD("cop09.8g", 0x0e000, 0x2000, 0x855a2ec3);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("copproma.13d", 0x0000, 0x0100, 0x97f68a7a);/* red */
            ROM_LOAD("coppromb.14d", 0x0100, 0x0100, 0x39a40b4c);/* green */
            ROM_LOAD("coppromc.15d", 0x0200, 0x0100, 0x8181748b);/* blue */
            ROM_LOAD("coppromd.19d", 0x0300, 0x0100, 0x6a63dbb8);/* lookup table? (not implemented) */
            ROM_LOAD("copprome.2e", 0x0400, 0x0100, 0x214392fa);/* sprite lookup table */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_cop01a = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code */
            ROM_LOAD("cop01alt.001", 0x0000, 0x4000, 0xa13ee0d3);
            ROM_LOAD("cop01alt.002", 0x4000, 0x4000, 0x20bad28e);
            ROM_LOAD("cop01alt.003", 0x8000, 0x4000, 0xa7e10b79);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for code */
            ROM_LOAD("cop01alt.015", 0x0000, 0x4000, 0x95be9270);
            ROM_LOAD("cop01alt.016", 0x4000, 0x4000, 0xc20bf649);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop01alt.014", 0x00000, 0x2000, 0xedd8a474);/* chars */

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop04.15c", 0x00000, 0x4000, 0x622d32e6);/* tiles */
            ROM_LOAD("cop05.16c", 0x04000, 0x4000, 0xc6ac5a35);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("cop01alt.010", 0x00000, 0x2000, 0x94aee9d6);/* sprites */
            ROM_LOAD("cop11.5e", 0x02000, 0x2000, 0x9078bc04);
            ROM_LOAD("cop12.6e", 0x04000, 0x2000, 0x257a6706);
            ROM_LOAD("cop13.8e", 0x06000, 0x2000, 0x07c4ea66);
            ROM_LOAD("cop01alt.006", 0x08000, 0x2000, 0xcac7dac8);
            ROM_LOAD("cop07.5g", 0x0a000, 0x2000, 0x11db7b72);
            ROM_LOAD("cop08.6g", 0x0c000, 0x2000, 0xa63ddda6);
            ROM_LOAD("cop09.8g", 0x0e000, 0x2000, 0x855a2ec3);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("copproma.13d", 0x0000, 0x0100, 0x97f68a7a);/* red */
            ROM_LOAD("coppromb.14d", 0x0100, 0x0100, 0x39a40b4c);/* green */
            ROM_LOAD("coppromc.15d", 0x0200, 0x0100, 0x8181748b);/* blue */
            ROM_LOAD("coppromd.19d", 0x0300, 0x0100, 0x6a63dbb8);/* lookup table? (not implemented) */
            ROM_LOAD("copprome.2e", 0x0400, 0x0100, 0x214392fa);/* sprite lookup table */
            ROM_END();
        }
    };

    public static GameDriver driver_cop01 = new GameDriver("1985", "cop01", "cop01.java", rom_cop01, null, machine_driver_cop01, input_ports_cop01, null, ROT0, "Nichibutsu", "Cop 01 (set 1)", GAME_IMPERFECT_COLORS);
    public static GameDriver driver_cop01a = new GameDriver("1985", "cop01a", "cop01.java", rom_cop01a, driver_cop01, machine_driver_cop01, input_ports_cop01, null, ROT0, "Nichibutsu", "Cop 01 (set 2)", GAME_IMPERFECT_COLORS);
}
