/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.vidhrdw.espial.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.cpu.z80.z80H.Z80_IRQ_INT;

public class espial {

    public static InitMachinePtr espial_init_machine = new InitMachinePtr() {
        public void handler() {
            /* we must start with NMI interrupts disabled */
            //interrupt_enable = 0;
            interrupt_enable_w.handler(0, 0);
        }
    };

    public static WriteHandlerPtr zodiac_master_interrupt_enable_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            interrupt_enable_w.handler(offset, data ^ 1);
        }
    };

    public static InterruptPtr zodiac_master_interrupt = new InterruptPtr() {
        public int handler() {
            return (cpu_getiloops() == 0) ? nmi_interrupt.handler() : interrupt.handler();
        }
    };

    public static WriteHandlerPtr zodiac_master_soundlatch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_IRQ_INT);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x4fff, MRA_ROM),
                new MemoryReadAddress(0x5800, 0x5fff, MRA_RAM),
                new MemoryReadAddress(0x7000, 0x7000, MRA_RAM), /* ?? */
                new MemoryReadAddress(0x8000, 0x803f, MRA_RAM),
                new MemoryReadAddress(0x8400, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x8c00, 0x903f, MRA_RAM),
                new MemoryReadAddress(0x9400, 0x97ff, MRA_RAM),
                new MemoryReadAddress(0x6081, 0x6081, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x6082, 0x6082, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x6083, 0x6083, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x6084, 0x6084, input_port_3_r), /* IN3 */
                new MemoryReadAddress(0x6090, 0x6090, soundlatch_r), /* the main CPU reads the command back from the slave */
                new MemoryReadAddress(0xc000, 0xcfff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x4fff, MWA_ROM),
                new MemoryWriteAddress(0x5800, 0x5fff, MWA_RAM),
                new MemoryWriteAddress(0x6090, 0x6090, zodiac_master_soundlatch_w),
                new MemoryWriteAddress(0x7000, 0x7000, watchdog_reset_w),
                new MemoryWriteAddress(0x7100, 0x7100, zodiac_master_interrupt_enable_w),
                new MemoryWriteAddress(0x8000, 0x801f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x8400, 0x87ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x8800, 0x880f, MWA_RAM, spriteram_3),
                new MemoryWriteAddress(0x8c00, 0x8fff, espial_attributeram_w, espial_attributeram),
                new MemoryWriteAddress(0x9000, 0x901f, MWA_RAM, spriteram_2),
                new MemoryWriteAddress(0x9020, 0x903f, MWA_RAM, espial_column_scroll),
                new MemoryWriteAddress(0x9400, 0x97ff, colorram_w, colorram),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x23ff, MRA_RAM),
                new MemoryReadAddress(0x6000, 0x6000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x23ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x4000, interrupt_enable_w),
                new MemoryWriteAddress(0x6000, 0x6000, soundlatch_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_espial = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_DIPNAME(0x01, 0x00, "Fire Buttons");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPNAME(0x02, 0x02, "CounterAttack");/* you can shoot bullets */
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x1c, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x14, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x1c, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "20k and every 70k");
            PORT_DIPSETTING(0x20, "50k and every 100k");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, "Test Mode");/* ??? */
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x80, "Test");

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);

            PORT_START();
            /* IN3 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            768, /* 768 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 128 * 32 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 0, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz?????? */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_espial = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 MHz */
                        readmem, writemem, null, null,
                        zodiac_master_interrupt, 2
                ),
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 2 MHz?????? */
                        sound_readmem, sound_writemem, null, sound_writeport,
                        nmi_interrupt, 4
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            espial_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            espial_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            espial_vh_screenrefresh,
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
    static RomLoadPtr rom_espial = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("espial.3", 0x0000, 0x2000, 0x10f1da30);
            ROM_LOAD("espial.4", 0x2000, 0x2000, 0xd2adbe39);
            ROM_LOAD("espial.6", 0x4000, 0x1000, 0xbaa60bc1);
            ROM_LOAD("espial.5", 0xc000, 0x1000, 0x6d7bbfc1);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("espial.1", 0x0000, 0x1000, 0x1e5ec20b);
            ROM_LOAD("espial.2", 0x1000, 0x1000, 0x3431bb97);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("espial.8", 0x0000, 0x2000, 0x2f43036f);
            ROM_LOAD("espial.7", 0x2000, 0x1000, 0xebfef046);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("espial.10", 0x0000, 0x1000, 0xde80fbc1);
            ROM_LOAD("espial.9", 0x1000, 0x1000, 0x48c258a0);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("espial.1f", 0x0000, 0x0100, 0xd12de557);/* palette low 4 bits */
            ROM_LOAD("espial.1h", 0x0100, 0x0100, 0x4c84fe70);/* palette high 4 bits */
            ROM_END();
        }
    };

    static RomLoadPtr rom_espiale = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("2764.3", 0x0000, 0x2000, 0x0973c8a4);
            ROM_LOAD("2764.4", 0x2000, 0x2000, 0x6034d7e5);
            ROM_LOAD("2732.6", 0x4000, 0x1000, 0x357025b4);
            ROM_LOAD("2732.5", 0xc000, 0x1000, 0xd03a2fc4);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("2732.1", 0x0000, 0x1000, 0xfc7729e9);
            ROM_LOAD("2732.2", 0x1000, 0x1000, 0xe4e256da);

            ROM_REGION(0x3000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("espial.8", 0x0000, 0x2000, 0x2f43036f);
            ROM_LOAD("espial.7", 0x2000, 0x1000, 0xebfef046);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("espial.10", 0x0000, 0x1000, 0xde80fbc1);
            ROM_LOAD("espial.9", 0x1000, 0x1000, 0x48c258a0);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("espial.1f", 0x0000, 0x0100, 0xd12de557);/* palette low 4 bits */
            ROM_LOAD("espial.1h", 0x0100, 0x0100, 0x4c84fe70);/* palette high 4 bits */
            ROM_END();
        }
    };

    public static GameDriver driver_espial = new GameDriver("1983", "espial", "espial.java", rom_espial, null, machine_driver_espial, input_ports_espial, null, ROT0, "[Orca] Thunderbolt", "Espial (US?)", GAME_NO_COCKTAIL);
    public static GameDriver driver_espiale = new GameDriver("1983", "espiale", "espial.java", rom_espiale, driver_espial, machine_driver_espial, input_ports_espial, null, ROT0, "[Orca] Thunderbolt", "Espial (Europe)", GAME_NO_COCKTAIL);
}
