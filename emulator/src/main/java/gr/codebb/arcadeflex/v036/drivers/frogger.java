/*
 * ported to v0.36
 * using automatic conversion tool v0.08 + manual fixes
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.frogger.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910.*;
import static gr.codebb.arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.frogger.*;

public class frogger {

    public static WriteHandlerPtr frogger_counterb_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coin_counter_w.handler(1, data);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x8800, 0x8800, watchdog_reset_r),
                new MemoryReadAddress(0xa800, 0xabff, MRA_RAM), /* video RAM */
                new MemoryReadAddress(0xb000, 0xb05f, MRA_RAM), /* screen attributes, sprites */
                new MemoryReadAddress(0xe000, 0xe000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xe002, 0xe002, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xe004, 0xe004, input_port_2_r), /* IN2 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0xa800, 0xabff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xb000, 0xb03f, frogger_attributes_w, frogger_attributesram),
                new MemoryWriteAddress(0xb040, 0xb05f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xb808, 0xb808, interrupt_enable_w),
                new MemoryWriteAddress(0xb80c, 0xb80c, frogger_flipscreen_w),
                new MemoryWriteAddress(0xb818, 0xb818, coin_counter_w),
                new MemoryWriteAddress(0xb81c, 0xb81c, frogger_counterb_w),
                new MemoryWriteAddress(0xd000, 0xd000, soundlatch_w),
                new MemoryWriteAddress(0xd002, 0xd002, frogger_sh_irqtrigger_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress froggrmc_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM), /* video RAM */
                new MemoryReadAddress(0x9800, 0x985f, MRA_RAM), /* screen attributes, sprites */
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xa800, 0xa800, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xb000, 0xb000, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0xb800, 0xb800, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress froggrmc_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9800, 0x983f, frogger_attributes_w, frogger_attributesram),
                new MemoryWriteAddress(0x9840, 0x985f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xa800, 0xa800, soundlatch_w),
                new MemoryWriteAddress(0xb000, 0xb000, interrupt_enable_w),
                new MemoryWriteAddress(0xb001, 0xb001, frogger2_sh_irqtrigger_w),
                new MemoryWriteAddress(0xb006, 0xb006, frogger_flipscreen_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x17ff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x17ff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x40, 0x40, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x80, 0x80, AY8910_control_port_0_w),
                new IOWritePort(0x40, 0x40, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_frogger = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 1P shoot2 - unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 1P shoot1 - unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START(); 	/* IN1 */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x02, "7");
            PORT_BITX(0, 0x03, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 2P shoot2 - unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* 2P shoot1 - unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2/1 B 2/1 C 2/1");
            PORT_DIPSETTING(0x04, "A 2/1 B 1/3 C 2/1");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/1 C 1/1");
            PORT_DIPSETTING(0x06, "A 1/1 B 1/6 C 1/1");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_froggrmc = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_COIN3);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_DIPNAME(0xc0, 0xc0, DEF_STR("Lives"));
            PORT_DIPSETTING(0xc0, "3");
            PORT_DIPSETTING(0x80, "5");
            PORT_DIPSETTING(0x40, "7");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "256", IP_KEY_NONE, IP_JOY_NONE);

            PORT_START(); 	/* IN2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x02, "A 2/1 B 2/1 C 2/1");
            PORT_DIPSETTING(0x04, "A 2/1 B 1/3 C 2/1");
            PORT_DIPSETTING(0x06, "A 1/1 B 1/1 C 1/1");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/6 C 1/1");
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{256 * 8 * 8, 0}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{64 * 16 * 16, 0}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    public static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            14318000 / 8, /* 1.78975 Mhz */
            new int[]{(MIXERG(80, MIXER_GAIN_2x, MIXER_PAN_CENTER))},
            new ReadHandlerPtr[]{soundlatch_r},
            new ReadHandlerPtr[]{frogger_portB_r},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_frogger = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 64,
            frogger_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            frogger_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),}
    );

    static MachineDriver machine_driver_froggrmc = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        froggrmc_readmem, froggrmc_writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318000 / 8, /* 1.78975 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 64,
            frogger_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            frogger2_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),}
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_frogger = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("frogger.26", 0x0000, 0x1000, 0x597696d6);
            ROM_LOAD("frogger.27", 0x1000, 0x1000, 0xb6e6fcc3);
            ROM_LOAD("frsm3.7", 0x2000, 0x1000, 0xaca22ae0);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("frogger.608", 0x0000, 0x0800, 0xe8ab0256);
            ROM_LOAD("frogger.609", 0x0800, 0x0800, 0x7380a48f);
            ROM_LOAD("frogger.610", 0x1000, 0x0800, 0x31d7eb27);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("frogger.606", 0x0000, 0x0800, 0xf524ee30);
            ROM_LOAD("frogger.607", 0x0800, 0x0800, 0x05f7d883);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pr-91.6l", 0x0000, 0x0020, 0x413703bf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_frogseg1 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("frogger.26", 0x0000, 0x1000, 0x597696d6);
            ROM_LOAD("frogger.27", 0x1000, 0x1000, 0xb6e6fcc3);
            ROM_LOAD("frogger.34", 0x2000, 0x1000, 0xed866bab);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("frogger.608", 0x0000, 0x0800, 0xe8ab0256);
            ROM_LOAD("frogger.609", 0x0800, 0x0800, 0x7380a48f);
            ROM_LOAD("frogger.610", 0x1000, 0x0800, 0x31d7eb27);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("frogger.606", 0x0000, 0x0800, 0xf524ee30);
            ROM_LOAD("frogger.607", 0x0800, 0x0800, 0x05f7d883);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pr-91.6l", 0x0000, 0x0020, 0x413703bf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_frogseg2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("frogger.ic5", 0x0000, 0x1000, 0xefab0c79);
            ROM_LOAD("frogger.ic6", 0x1000, 0x1000, 0xaeca9c13);
            ROM_LOAD("frogger.ic7", 0x2000, 0x1000, 0xdd251066);
            ROM_LOAD("frogger.ic8", 0x3000, 0x1000, 0xbf293a02);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("frogger.608", 0x0000, 0x0800, 0xe8ab0256);
            ROM_LOAD("frogger.609", 0x0800, 0x0800, 0x7380a48f);
            ROM_LOAD("frogger.610", 0x1000, 0x0800, 0x31d7eb27);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("frogger.606", 0x0000, 0x0800, 0xf524ee30);
            ROM_LOAD("frogger.607", 0x0800, 0x0800, 0x05f7d883);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pr-91.6l", 0x0000, 0x0020, 0x413703bf);
            ROM_END();
        }
    };

    static RomLoadPtr rom_froggrmc = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("epr-1031.15", 0x0000, 0x1000, 0x4b7c8d11);
            ROM_LOAD("epr-1032.16", 0x1000, 0x1000, 0xac00b9d9);
            ROM_LOAD("epr-1033.33", 0x2000, 0x1000, 0xbc1d6fbc);
            ROM_LOAD("epr-1034.34", 0x3000, 0x1000, 0x9efe7399);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("epr-1082.42", 0x0000, 0x1000, 0x802843c2);
            ROM_LOAD("epr-1035.43", 0x1000, 0x0800, 0x14e74148);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("epr-1036.1k", 0x0000, 0x0800, 0x658745f8);
            ROM_LOAD("frogger.607", 0x0800, 0x0800, 0x05f7d883);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("pr-91.6l", 0x0000, 0x0020, 0x413703bf);
            ROM_END();
        }
    };

    public static InitDriverPtr init_frogger = new InitDriverPtr() {
        public void handler() {
            int A;
            UBytePtr RAM;

            /* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
            RAM = memory_region(REGION_CPU2);
            for (A = 0; A < 0x0800; A++) {
                RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
            }

            /* likewise, the first gfx ROM has data lines D0 and D1 swapped. Decode it. */
            RAM = memory_region(REGION_GFX1);
            for (A = 0; A < 0x0800; A++) {
                RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
            }
        }
    };

    public static InitDriverPtr init_froggrmc = new InitDriverPtr() {
        public void handler() {
            int A;
            UBytePtr RAM;

            /* the first ROM of the second CPU has data lines D0 and D1 swapped. Decode it. */
            RAM = memory_region(REGION_CPU2);
            for (A = 0; A < 0x1000; A++) {
                RAM.write(A, (RAM.read(A) & 0xfc) | ((RAM.read(A) & 1) << 1) | ((RAM.read(A) & 2) >> 1));
            }
        }
    };

    public static GameDriver driver_frogger = new GameDriver("1981", "frogger", "frogger.java", rom_frogger, null, machine_driver_frogger, input_ports_frogger, init_frogger, ROT90, "Konami", "Frogger");
    public static GameDriver driver_frogseg1 = new GameDriver("1981", "frogseg1", "frogger.java", rom_frogseg1, driver_frogger, machine_driver_frogger, input_ports_frogger, init_frogger, ROT90, "[Konami] (Sega license)", "Frogger (Sega set 1)");
    public static GameDriver driver_frogseg2 = new GameDriver("1981", "frogseg2", "frogger.java", rom_frogseg2, driver_frogger, machine_driver_frogger, input_ports_frogger, init_frogger, ROT90, "[Konami] (Sega license)", "Frogger (Sega set 2)");

    /* this version runs on modified Moon Cresta hardware */
    public static GameDriver driver_froggrmc = new GameDriver("1981", "froggrmc", "frogger.java", rom_froggrmc, driver_frogger, machine_driver_froggrmc, input_ports_froggrmc, init_froggrmc, ROT90, "bootleg?", "Frogger (modified Moon Cresta hardware)");
}
