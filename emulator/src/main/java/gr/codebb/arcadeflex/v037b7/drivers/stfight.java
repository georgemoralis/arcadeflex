/**
 * ported to 0.37b7
 * ported to 0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v036.vidhrdw.stfight.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.machine.stfight.*;

public class stfight {

	// vidhrdw
    static MemoryReadAddress readmem_cpu1[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1), /* sf02.bin */
                new MemoryReadAddress(0xc000, 0xc1ff, MRA_RAM), /* palette ram */
                new MemoryReadAddress(0xc200, 0xc200, input_port_0_r), /* IN1 */
                new MemoryReadAddress(0xc201, 0xc201, input_port_1_r), /* IN2 */
                new MemoryReadAddress(0xc202, 0xc202, input_port_2_r), /* IN3 */
                new MemoryReadAddress(0xc203, 0xc204, stfight_dsw_r), /* DS0,1 */
                new MemoryReadAddress(0xc205, 0xc205, stfight_coin_r), /* coin mech */
                new MemoryReadAddress(0xd000, 0xd7ff, MRA_RAM), /* video */
                new MemoryReadAddress(0xe000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu1[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0xbfff, MWA_BANK1), /* sf02.bin */
                new MemoryWriteAddress(0xc000, 0xc0ff, paletteram_xxxxBBBBRRRRGGGG_split1_w, paletteram),
                new MemoryWriteAddress(0xc100, 0xc1ff, paletteram_xxxxBBBBRRRRGGGG_split2_w, paletteram_2),
                new MemoryWriteAddress(0xc500, 0xc500, stfight_fm_w), /* play fm sound */
                new MemoryWriteAddress(0xc600, 0xc600, stfight_adpcm_control_w), /* voice control */
                new MemoryWriteAddress(0xc700, 0xc700, stfight_coin_w), /* coin mech */
                new MemoryWriteAddress(0xc804, 0xc806, MWA_NOP), /* TBD */
                new MemoryWriteAddress(0xc807, 0xc807, stfight_sprite_bank_w),
                new MemoryWriteAddress(0xd000, 0xd3ff, stfight_text_char_w, stfight_text_char_ram),
                new MemoryWriteAddress(0xd400, 0xd7ff, stfight_text_attr_w, stfight_text_attr_ram),
                new MemoryWriteAddress(0xd800, 0xd808, stfight_vh_latch_w, stfight_vh_latch_ram),
                new MemoryWriteAddress(0xe000, 0xefff, MWA_RAM),
                new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM, stfight_sprite_ram),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xc001, 0xc001, YM2203_read_port_0_r),
                new MemoryReadAddress(0xc801, 0xc801, YM2203_read_port_1_r),
                new MemoryReadAddress(0xf000, 0xf000, stfight_fm_r),
                new MemoryReadAddress(0xf800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0xc001, 0xc001, YM2203_write_port_0_w),
                new MemoryWriteAddress(0xc800, 0xc800, YM2203_control_port_1_w),
                new MemoryWriteAddress(0xc801, 0xc801, YM2203_write_port_1_w),
                new MemoryWriteAddress(0xe800, 0xe800, stfight_e800_w),
                new MemoryWriteAddress(0xf800, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_stfight = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* PLAYER 1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* PLAYER 2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* START BUTTONS */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0xe7, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x07, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x07, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x18, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_SERVICE(0x20, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x40, 0x40, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x40, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, "Bullet Colour");
            PORT_DIPSETTING(0x80, "Red");
            PORT_DIPSETTING(0x00, "Blue");

            PORT_START(); 	/* DSW1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x01, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x06, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x02, "Normal");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPSETTING(0x06, "Hardest");
            PORT_DIPNAME(0x18, 0x10, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x10, "3");
            PORT_DIPSETTING(0x18, "4");
            PORT_DIPNAME(0x60, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000 30000");
            PORT_DIPSETTING(0x20, "20000 40000");
            PORT_DIPSETTING(0x40, "30000 60000");
            PORT_DIPSETTING(0x60, "40000 80000");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* COIN MECH */

            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_LOW, IPT_COIN1, 2);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_LOW, IPT_COIN2, 2);
            INPUT_PORTS_END();
        }
    };

    /* text-layer characters */
    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 pixels */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{4, 0},
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            8 * 16 /* every char takes 16 consecutive bytes */
    );

    /* foreground tiles */
    static GfxLayout fglayout = new GfxLayout(
            16, 16, /* 16*16 pixels */
            1024, /* 1024 tiles */
            4, /* 4 bits per pixel */
            new int[]{64 * 1024 * 8 + 0, 64 * 1024 * 8 + 4, 0, 4},
            new int[]{0, 1, 2, 3,
                8, 9, 10, 11,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3,
                32 * 8 + 8, 32 * 8 + 9, 32 * 8 + 10, 32 * 8 + 11},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8,
                8 * 8, 10 * 8, 12 * 8, 14 * 8,
                16 * 8, 18 * 8, 20 * 8, 22 * 8,
                24 * 8, 26 * 8, 28 * 8, 30 * 8},
            64 * 8 /* every char takes 64 consecutive bytes */
    );

    /*
     *      The background tiles are interleaved in banks of 2
     *      - so we need to create two separate layout structs
     *        to handle them properly with tilemaps
     */
    /* background tiles */
    static GfxLayout bglayout = new GfxLayout(
            16, 16, /* 16*16 pixels */
            512, /* 512 tiles */
            4, /* 4 bits per pixel */
            new int[]{64 * 1024 * 8 + 4, 64 * 1024 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3,
                8, 9, 10, 11,
                64 * 8 + 0, 64 * 8 + 1, 64 * 8 + 2, 64 * 8 + 3,
                64 * 8 + 8, 64 * 8 + 9, 64 * 8 + 10, 64 * 8 + 11},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8,
                8 * 8, 10 * 8, 12 * 8, 14 * 8,
                16 * 8, 18 * 8, 20 * 8, 22 * 8,
                24 * 8, 26 * 8, 28 * 8, 30 * 8},
            128 * 8 /* every tile takes 64/128 consecutive bytes */
    );

    /* sprites */
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 pixels */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{64 * 1024 * 8 + 0, 64 * 1024 * 8 + 4, 0, 4},
            new int[]{0, 1, 2, 3,
                8, 9, 10, 11,
                32 * 8 + 0, 32 * 8 + 1, 32 * 8 + 2, 32 * 8 + 3,
                32 * 8 + 8, 32 * 8 + 9, 32 * 8 + 10, 32 * 8 + 11},
            new int[]{0 * 8, 2 * 8, 4 * 8, 6 * 8,
                8 * 8, 10 * 8, 12 * 8, 14 * 8,
                16 * 8, 18 * 8, 20 * 8, 22 * 8,
                24 * 8, 26 * 8, 28 * 8, 30 * 8},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x0000, fglayout, 16 * 4, 16),
                new GfxDecodeInfo(REGION_GFX3, 0x0000, bglayout, 16 * 4 + 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX3, 0x0020, bglayout, 16 * 4 + 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX4, 0x0000, spritelayout, 16 * 4 + 16 * 16 + 16 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz */
            new int[]{YM2203_VOL(10, 15), YM2203_VOL(10, 15)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MSM5205interface msm5205_interface = new MSM5205interface(
            1, /* 1 chip            */
            384000, /* 384KHz             */
            new vclk_interruptPtr[]{stfight_adpcm_int}, /* interrupt function */
            new int[]{MSM5205_S48_4B}, /* 8KHz               */
            new int[]{50}
    );

    static MachineDriver machine_driver_stfight = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3000000, /* 3 Mhz */
                        readmem_cpu1, writemem_cpu1, null, null,
                        stfight_vb_interrupt, 1,
                        stfight_interrupt_1, 30
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 3 Mhz */
                        readmem_cpu2, writemem_cpu2, null, null,
                        null, 0,
                        stfight_interrupt_2, 120
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            10,
            stfight_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256 + 1, 16 * 4 + 16 * 16 + 16 * 16 + 16 * 16,
            stfight_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            stfight_vh_start,
            null,
            stfight_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm5205_interface
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
    static RomLoadPtr rom_empcity = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x18000, REGION_CPU1);/* 96k for code + 96k for decrypted opcodes */

            ROM_LOAD("ec_01.rom", 0x00000, 0x8000, 0xfe01d9b1);
            ROM_LOAD("ec_02.rom", 0x10000, 0x8000, 0xb3cf1ef7);/* bank switched */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("ec_04.rom", 0x0000, 0x8000, 0xaa3e7d1e);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* character data */

            ROM_LOAD("sf17.bin", 0x0000, 0x2000, 0x1b3706b5);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* foreground tile pixel data */

            ROM_LOAD("sf07.bin", 0x10000, 0x8000, 0x2c6caa5f);
            ROM_LOAD("sf08.bin", 0x18000, 0x8000, 0xe11ded31);
            ROM_LOAD("sf05.bin", 0x00000, 0x8000, 0x0c099a31);
            ROM_LOAD("sf06.bin", 0x08000, 0x8000, 0x3cc77c31);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* background tile pixel data */

            ROM_LOAD("sf13.bin", 0x10000, 0x8000, 0x0ae48dd3);
            ROM_LOAD("sf14.bin", 0x18000, 0x8000, 0xdebf5d76);
            ROM_LOAD("sf11.bin", 0x00000, 0x8000, 0x8261ecfe);
            ROM_LOAD("sf12.bin", 0x08000, 0x8000, 0x71137301);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprite data */

            ROM_LOAD("sf20.bin", 0x10000, 0x8000, 0x8299f247);
            ROM_LOAD("sf21.bin", 0x18000, 0x8000, 0xb57dc037);
            ROM_LOAD("sf18.bin", 0x00000, 0x8000, 0x68acd627);
            ROM_LOAD("sf19.bin", 0x08000, 0x8000, 0x5170a057);

            ROM_REGION(0x10000, REGION_GFX5);/* foreground map data */

            ROM_LOAD("sf09.bin", 0x00000, 0x8000, 0x8ceaf4fe);
            ROM_LOAD("sf10.bin", 0x08000, 0x8000, 0x5a1a227a);

            ROM_REGION(0x10000, REGION_GFX6);/* background map data */

            ROM_LOAD("sf15.bin", 0x00000, 0x8000, 0x27a310bc);
            ROM_LOAD("sf16.bin", 0x08000, 0x8000, 0x3d19ce18);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("82s129.006", 0x0000, 0x0100, 0xf9424b5b);/* text lookup table */

            ROM_LOAD("82s129.002", 0x0100, 0x0100, 0xc883d49b);/* fg lookup table */

            ROM_LOAD("82s129.003", 0x0200, 0x0100, 0xaf81882a);
            ROM_LOAD("82s129.004", 0x0300, 0x0100, 0x1831ce7c);/* bg lookup table */

            ROM_LOAD("82s129.005", 0x0400, 0x0100, 0x96cb6293);
            ROM_LOAD("82s129.052", 0x0500, 0x0100, 0x3d915ffc);/* sprite lookup table */

            ROM_LOAD("82s129.066", 0x0600, 0x0100, 0x51e8832f);
            ROM_LOAD("82s129.015", 0x0700, 0x0100, 0x0eaf5158);/* timing? (not used) */

            ROM_REGION(0x08000, REGION_SOUND1);/* adpcm voice data */

            ROM_LOAD("sf04.bin", 0x00000, 0x8000, 0x1b8d0c07);
            ROM_END();
        }
    };

    static RomLoadPtr rom_empcityj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x18000, REGION_CPU1);/* 96k for code + 96k for decrypted opcodes */

            ROM_LOAD("1.bin", 0x00000, 0x8000, 0x8162331c);
            ROM_LOAD("2.bin", 0x10000, 0x8000, 0x960edea6);/* bank switched */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("ec_04.rom", 0x0000, 0x8000, 0xaa3e7d1e);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* character data */

            ROM_LOAD("sf17.bin", 0x0000, 0x2000, 0x1b3706b5);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* foreground tile pixel data */

            ROM_LOAD("sf07.bin", 0x10000, 0x8000, 0x2c6caa5f);
            ROM_LOAD("sf08.bin", 0x18000, 0x8000, 0xe11ded31);
            ROM_LOAD("sf05.bin", 0x00000, 0x8000, 0x0c099a31);
            ROM_LOAD("sf06.bin", 0x08000, 0x8000, 0x3cc77c31);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* background tile pixel data */

            ROM_LOAD("sf13.bin", 0x10000, 0x8000, 0x0ae48dd3);
            ROM_LOAD("sf14.bin", 0x18000, 0x8000, 0xdebf5d76);
            ROM_LOAD("sf11.bin", 0x00000, 0x8000, 0x8261ecfe);
            ROM_LOAD("sf12.bin", 0x08000, 0x8000, 0x71137301);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprite data */

            ROM_LOAD("sf20.bin", 0x10000, 0x8000, 0x8299f247);
            ROM_LOAD("sf21.bin", 0x18000, 0x8000, 0xb57dc037);
            ROM_LOAD("sf18.bin", 0x00000, 0x8000, 0x68acd627);
            ROM_LOAD("sf19.bin", 0x08000, 0x8000, 0x5170a057);

            ROM_REGION(0x10000, REGION_GFX5);/* foreground map data */

            ROM_LOAD("sf09.bin", 0x00000, 0x8000, 0x8ceaf4fe);
            ROM_LOAD("sf10.bin", 0x08000, 0x8000, 0x5a1a227a);

            ROM_REGION(0x10000, REGION_GFX6);/* background map data */

            ROM_LOAD("sf15.bin", 0x00000, 0x8000, 0x27a310bc);
            ROM_LOAD("sf16.bin", 0x08000, 0x8000, 0x3d19ce18);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("82s129.006", 0x0000, 0x0100, 0xf9424b5b);/* text lookup table */

            ROM_LOAD("82s129.002", 0x0100, 0x0100, 0xc883d49b);/* fg lookup table */

            ROM_LOAD("82s129.003", 0x0200, 0x0100, 0xaf81882a);
            ROM_LOAD("82s129.004", 0x0300, 0x0100, 0x1831ce7c);/* bg lookup table */

            ROM_LOAD("82s129.005", 0x0400, 0x0100, 0x96cb6293);
            ROM_LOAD("82s129.052", 0x0500, 0x0100, 0x3d915ffc);/* sprite lookup table */

            ROM_LOAD("82s129.066", 0x0600, 0x0100, 0x51e8832f);
            ROM_LOAD("82s129.015", 0x0700, 0x0100, 0x0eaf5158);/* timing? (not used) */

            ROM_REGION(0x08000, REGION_SOUND1);/* adpcm voice data */

            ROM_LOAD("sf04.bin", 0x00000, 0x8000, 0x1b8d0c07);
            ROM_END();
        }
    };

    static RomLoadPtr rom_stfight = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x18000, REGION_CPU1);/* 96k for code + 96k for decrypted opcodes */

            ROM_LOAD("a-1.4q", 0x00000, 0x8000, 0xff83f316);
            ROM_LOAD("sf02.bin", 0x10000, 0x8000, 0xe626ce9e);/* bank switched */

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the second CPU */

            ROM_LOAD("sf03.bin", 0x0000, 0x8000, 0x6a8cb7a6);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* character data */

            ROM_LOAD("sf17.bin", 0x0000, 0x2000, 0x1b3706b5);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* foreground tile pixel data */

            ROM_LOAD("sf07.bin", 0x10000, 0x8000, 0x2c6caa5f);
            ROM_LOAD("sf08.bin", 0x18000, 0x8000, 0xe11ded31);
            ROM_LOAD("sf05.bin", 0x00000, 0x8000, 0x0c099a31);
            ROM_LOAD("sf06.bin", 0x08000, 0x8000, 0x3cc77c31);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* background tile pixel data */

            ROM_LOAD("sf13.bin", 0x10000, 0x8000, 0x0ae48dd3);
            ROM_LOAD("sf14.bin", 0x18000, 0x8000, 0xdebf5d76);
            ROM_LOAD("sf11.bin", 0x00000, 0x8000, 0x8261ecfe);
            ROM_LOAD("sf12.bin", 0x08000, 0x8000, 0x71137301);

            ROM_REGION(0x20000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprite data */

            ROM_LOAD("sf20.bin", 0x10000, 0x8000, 0x8299f247);
            ROM_LOAD("sf21.bin", 0x18000, 0x8000, 0xb57dc037);
            ROM_LOAD("sf18.bin", 0x00000, 0x8000, 0x68acd627);
            ROM_LOAD("sf19.bin", 0x08000, 0x8000, 0x5170a057);

            ROM_REGION(0x10000, REGION_GFX5);/* foreground map data */

            ROM_LOAD("sf09.bin", 0x00000, 0x8000, 0x8ceaf4fe);
            ROM_LOAD("sf10.bin", 0x08000, 0x8000, 0x5a1a227a);

            ROM_REGION(0x10000, REGION_GFX6);/* background map data */

            ROM_LOAD("sf15.bin", 0x00000, 0x8000, 0x27a310bc);
            ROM_LOAD("sf16.bin", 0x08000, 0x8000, 0x3d19ce18);

            ROM_REGION(0x0800, REGION_PROMS);
            ROM_LOAD("82s129.006", 0x0000, 0x0100, 0xf9424b5b);/* text lookup table */

            ROM_LOAD("82s129.002", 0x0100, 0x0100, 0xc883d49b);/* fg lookup table */

            ROM_LOAD("82s129.003", 0x0200, 0x0100, 0xaf81882a);
            ROM_LOAD("82s129.004", 0x0300, 0x0100, 0x1831ce7c);/* bg lookup table */

            ROM_LOAD("82s129.005", 0x0400, 0x0100, 0x96cb6293);
            ROM_LOAD("82s129.052", 0x0500, 0x0100, 0x3d915ffc);/* sprite lookup table */

            ROM_LOAD("82s129.066", 0x0600, 0x0100, 0x51e8832f);
            ROM_LOAD("82s129.015", 0x0700, 0x0100, 0x0eaf5158);/* timing? (not used) */

            ROM_REGION(0x08000, REGION_SOUND1);/* adpcm voice data */

            ROM_LOAD("sf04.bin", 0x00000, 0x8000, 0x1b8d0c07);
            ROM_END();
        }
    };

    public static GameDriver driver_empcity = new GameDriver("1986", "empcity", "stfight.java", rom_empcity, null, machine_driver_stfight, input_ports_stfight, init_empcity, ROT0, "Seibu Kaihatsu", "Empire City: 1931 (bootleg?)");
    public static GameDriver driver_empcityj = new GameDriver("1986", "empcityj", "stfight.java", rom_empcityj, driver_empcity, machine_driver_stfight, input_ports_stfight, init_stfight, ROT0, "[Seibu Kaihatsu] (Taito license)", "Empire City: 1931 (Japan)");
    public static GameDriver driver_stfight = new GameDriver("1986", "stfight", "stfight.java", rom_stfight, driver_empcity, machine_driver_stfight, input_ports_stfight, init_stfight, ROT0, "Seibu Kaihatsu", "Street Fight (Germany)");
}
