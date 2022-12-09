/**
 * ported to 0.37b7
 * ported to 0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.solomon.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class solomon {

    public static WriteHandlerPtr solomon_sh_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xcfff, MRA_RAM), /* RAM */
                new MemoryReadAddress(0xd000, 0xdfff, MRA_RAM), /* video + color + bg */
                new MemoryReadAddress(0xe000, 0xe07f, MRA_RAM), /* spriteram  */
                new MemoryReadAddress(0xe400, 0xe5ff, MRA_RAM), /* paletteram */
                new MemoryReadAddress(0xe600, 0xe600, input_port_0_r),
                new MemoryReadAddress(0xe601, 0xe601, input_port_1_r),
                new MemoryReadAddress(0xe602, 0xe602, input_port_2_r),
                new MemoryReadAddress(0xe604, 0xe604, input_port_3_r), /* DSW1 */
                new MemoryReadAddress(0xe605, 0xe605, input_port_4_r), /* DSW2 */
                new MemoryReadAddress(0xf000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd3ff, colorram_w, colorram),
                new MemoryWriteAddress(0xd400, 0xd7ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd800, 0xdbff, solomon_bgcolorram_w, solomon_bgcolorram),
                new MemoryWriteAddress(0xdc00, 0xdfff, solomon_bgvideoram_w, solomon_bgvideoram),
                new MemoryWriteAddress(0xe000, 0xe07f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0xe400, 0xe5ff, paletteram_xxxxBBBBGGGGRRRR_w, paletteram),
                new MemoryWriteAddress(0xe600, 0xe600, interrupt_enable_w),
                new MemoryWriteAddress(0xe604, 0xe604, solomon_flipscreen_w),
                new MemoryWriteAddress(0xe800, 0xe800, solomon_sh_command_w),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress solomon_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x47ff, MRA_RAM),
                new MemoryReadAddress(0x8000, 0x8000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress solomon_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, MWA_RAM),
                new MemoryWriteAddress(0xffff, 0xffff, MWA_NOP), /* watchdog? */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort solomon_sound_writeport[]
            = {
                new IOWritePort(0x10, 0x10, AY8910_control_port_0_w),
                new IOWritePort(0x11, 0x11, AY8910_write_port_0_w),
                new IOWritePort(0x20, 0x20, AY8910_control_port_1_w),
                new IOWritePort(0x21, 0x21, AY8910_write_port_1_w),
                new IOWritePort(0x30, 0x30, AY8910_control_port_2_w),
                new IOWritePort(0x31, 0x31, AY8910_write_port_2_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_solomon = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* COIN */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x02, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x0c, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x08, "4");
            PORT_DIPSETTING(0x04, "5");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_3C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_3C"));

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x01, 0x00, "Unknown DSW2 1");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, "Unknown DSW2 2");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, "Unknown DSW2 3");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, "Unknown DSW2 4");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, "Unknown DSW2 5");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0xe0, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30k 200k 500k");
            PORT_DIPSETTING(0x80, "100k 300k 800k");
            PORT_DIPSETTING(0x40, "30k 200k");
            PORT_DIPSETTING(0xc0, "100k 300k");
            PORT_DIPSETTING(0x20, "30k");
            PORT_DIPSETTING(0xa0, "100k");
            PORT_DIPSETTING(0x60, "200k");
            PORT_DIPSETTING(0xe0, "None");
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 8*8 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 512 * 32 * 8, 2 * 512 * 32 * 8, 3 * 512 * 32 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, /* pretty straightforward layout */
                8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 8 * 8 + 4, 8 * 8 + 5, 8 * 8 + 6, 8 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 8), /* colors   0-127 */
                new GfxDecodeInfo(REGION_GFX2, 0, charlayout, 128, 8), /* colors 128-255 */
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 0, 8), /* colors   0-127 */
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            3, /* 3 chips */
            1500000, /* 1.5 MHz?????? */
            new int[]{12, 12, 12},
            new ReadHandlerPtr[]{null, null, null},
            new ReadHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null},
            new WriteHandlerPtr[]{null, null, null}
    );

    static MachineDriver machine_driver_solomon = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4.0 Mhz (?????) */
                        readmem, writemem, null, null,
                        nmi_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3072000, /* 3.072 Mhz (?????) */
                        solomon_sound_readmem, solomon_sound_writemem, null, solomon_sound_writeport,
                        interrupt, 2 /* ??? */
                /* NMIs are caused by the main CPU */
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            solomon_vh_start,
            solomon_vh_stop,
            solomon_vh_screenrefresh,
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
    static RomLoadPtr rom_solomon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("slmn_06.bin", 0x00000, 0x4000, 0xe4d421ff);
            ROM_LOAD("slmn_07.bin", 0x08000, 0x4000, 0xd52d7e38);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_LOAD("slmn_08.bin", 0x0f000, 0x1000, 0xb924d162);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */
            ROM_LOAD("slmn_01.bin", 0x0000, 0x4000, 0xfa6e562e);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("slmn_12.bin", 0x00000, 0x08000, 0xaa26dfcb);/* characters */
            ROM_LOAD("slmn_11.bin", 0x08000, 0x08000, 0x6f94d2af);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("slmn_10.bin", 0x00000, 0x08000, 0x8310c2a1);
            ROM_LOAD("slmn_09.bin", 0x08000, 0x08000, 0xab7e6c42);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("slmn_02.bin", 0x00000, 0x04000, 0x80fa2be3);/* sprites */
            ROM_LOAD("slmn_03.bin", 0x04000, 0x04000, 0x236106b4);
            ROM_LOAD("slmn_04.bin", 0x08000, 0x04000, 0x088fe5d9);
            ROM_LOAD("slmn_05.bin", 0x0c000, 0x04000, 0x8366232a);
            ROM_END();
        }
    };

    public static GameDriver driver_solomon = new GameDriver("1986", "solomon", "solomon.java", rom_solomon, null, machine_driver_solomon, input_ports_solomon, null, ROT0, "Tecmo", "Solomon's Key (Japan)");
}
