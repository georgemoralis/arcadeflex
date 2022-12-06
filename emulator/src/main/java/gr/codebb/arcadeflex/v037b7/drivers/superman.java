/**
 * ported to v0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.vidhrdw.superman.*;
import static gr.codebb.arcadeflex.v037b7.sndhrdw.rastan.*;
import static gr.codebb.arcadeflex.v036.machine.cchip.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2610;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;
import static gr.codebb.arcadeflex.v037b7.sound._2610intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2610intfH.*;

public class superman {

    public static UBytePtr ram = new UBytePtr();/* for high score save */

    public static ReadHandlerPtr superman_input_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            switch (offset) {
                case 0x00:
                    return readinputport(0);
                case 0x02:
                    return readinputport(1);
                case 0x04:
                    return readinputport(2);
                case 0x06:
                    return readinputport(3);
                default:
                    logerror("superman_input_r offset: %04x\n", offset);
                    return 0xff;
            }
        }
    };

    public static WriteHandlerPtr taito68k_sound_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU2);

            int banknum = (data - 1) & 3;

            cpu_setbank(2, new UBytePtr(RAM, 0x10000 + (banknum * 0x4000)));
        }
    };

    static MemoryReadAddress superman_readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
                new MemoryReadAddress(0x500000, 0x50000f, superman_input_r), /* DSW A/B */
                new MemoryReadAddress(0x800000, 0x800003, rastan_sound_r),
                new MemoryReadAddress(0x900000, 0x900fff, cchip1_r),
                new MemoryReadAddress(0xb00000, 0xb00fff, paletteram_word_r),
                new MemoryReadAddress(0xd00000, 0xd007ff, supes_attribram_r),
                new MemoryReadAddress(0xe00000, 0xe03fff, supes_videoram_r),
                new MemoryReadAddress(0xf00000, 0xf03fff, MRA_BANK1), /* Main RAM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress superman_writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
                new MemoryWriteAddress(0x800000, 0x800003, rastan_sound_w),
                new MemoryWriteAddress(0x900000, 0x900fff, cchip1_w),
                new MemoryWriteAddress(0xb00000, 0xb00fff, paletteram_xRRRRRGGGGGBBBBB_word_w, paletteram),
                new MemoryWriteAddress(0xd00000, 0xd007ff, supes_attribram_w, supes_attribram, supes_attribram_size),
                new MemoryWriteAddress(0xe00000, 0xe03fff, supes_videoram_w, supes_videoram, supes_videoram_size),
                new MemoryWriteAddress(0xf00000, 0xf03fff, MWA_BANK1, ram), /* Main RAM */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK2),
                new MemoryReadAddress(0xc000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe000, YM2610_status_port_0_A_r),
                new MemoryReadAddress(0xe001, 0xe001, YM2610_read_port_0_r),
                new MemoryReadAddress(0xe002, 0xe002, YM2610_status_port_0_B_r),
                new MemoryReadAddress(0xe200, 0xe200, MRA_NOP),
                new MemoryReadAddress(0xe201, 0xe201, rastan_a001_r),
                new MemoryReadAddress(0xea00, 0xea00, MRA_NOP),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe000, YM2610_control_port_0_A_w),
                new MemoryWriteAddress(0xe001, 0xe001, YM2610_data_port_0_A_w),
                new MemoryWriteAddress(0xe002, 0xe002, YM2610_control_port_0_B_w),
                new MemoryWriteAddress(0xe003, 0xe003, YM2610_data_port_0_B_w),
                new MemoryWriteAddress(0xe200, 0xe200, rastan_a000_w),
                new MemoryWriteAddress(0xe201, 0xe201, rastan_a001_w),
                new MemoryWriteAddress(0xe400, 0xe403, MWA_NOP), /* pan */
                new MemoryWriteAddress(0xee00, 0xee00, MWA_NOP), /* ? */
                new MemoryWriteAddress(0xf000, 0xf000, MWA_NOP), /* ? */
                new MemoryWriteAddress(0xf200, 0xf200, taito68k_sound_bankswitch_w), /* bankswitch? */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_superman = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW A */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unused"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW B */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW c */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0c, "50k and every 150k");
            PORT_DIPSETTING(0x04, "Bonus 2??");
            PORT_DIPSETTING(0x08, "Bonus 3??");
            PORT_DIPSETTING(0x00, "Bonus 4??");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW D */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unused"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unused"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    public static final int NUM_TILES = 16384;
    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            NUM_TILES, /* 16384 of them */
            4, /* 4 bits per pixel */
            new int[]{64 * 8 * NUM_TILES + 8, 64 * 8 * NUM_TILES + 0, 8, 0},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                8 * 16, 8 * 16 + 1, 8 * 16 + 2, 8 * 16 + 3, 8 * 16 + 4, 8 * 16 + 5, 8 * 16 + 6, 8 * 16 + 7},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo superman_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x000000, tilelayout, 0, 256), /* sprites  playfield */
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the YM2610 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_irq_line(1, 0, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    static YM2610interface ym2610_interface = new YM2610interface(
            1, /* 1 chip */
            8000000, /* 8 MHz ?????? */
            new int[]{30},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteYmHandlerPtr[]{irqhandler},
            new int[]{REGION_SOUND1},
            new int[]{REGION_SOUND1},
            new int[]{YM3012_VOL(60, MIXER_PAN_LEFT, 60, MIXER_PAN_RIGHT)}
    );
    static MachineDriver machine_driver_superman = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        8000000, /* 8 MHz? */
                        superman_readmem, superman_writemem, null, null,
                        m68_level6_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz ??? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* IRQs are triggered by the YM2610 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1,
            cchip1_init_machine,
            /* video hardware */
            48 * 8, 32 * 8, new rectangle(2 * 8, 46 * 8 - 1, 2 * 8, 32 * 8 - 1),
            superman_gfxdecodeinfo,
            4096, 4096,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            superman_vh_start,
            superman_vh_stop,
            superman_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,//SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2610,
                        ym2610_interface
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
    static RomLoadPtr rom_superman = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x80000, REGION_CPU1);
            /* 512k for 68000 code */

            ROM_LOAD_EVEN("a10_09.bin", 0x00000, 0x20000, 0x640f1d58);
            ROM_LOAD_ODD("a05_07.bin", 0x00000, 0x20000, 0xfddb9953);
            ROM_LOAD_EVEN("a08_08.bin", 0x40000, 0x20000, 0x79fc028e);
            ROM_LOAD_ODD("a03_13.bin", 0x40000, 0x20000, 0x9f446a44);

            ROM_REGION(0x1c000, REGION_CPU2);
            /* 64k for Z80 code */

            ROM_LOAD("d18_10.bin", 0x00000, 0x4000, 0x6efe79e8);
            ROM_CONTINUE(0x10000, 0xc000);/* banked stuff */

            ROM_REGION(0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("f01_14.bin", 0x000000, 0x80000, 0x89368c3e);/* Plane 0, 1 */

            ROM_LOAD("h01_15.bin", 0x080000, 0x80000, 0x910cc4f9);
            ROM_LOAD("j01_16.bin", 0x100000, 0x80000, 0x3622ed2f);/* Plane 2, 3 */

            ROM_LOAD("k01_17.bin", 0x180000, 0x80000, 0xc34f27e0);

            ROM_REGION(0x80000, REGION_SOUND1);/* adpcm samples */

            ROM_LOAD("e18_01.bin", 0x00000, 0x80000, 0x3cf99786);
            ROM_END();
        }
    };

    public static GameDriver driver_superman = new GameDriver("1988", "superman", "superman.java", rom_superman, null, machine_driver_superman, input_ports_superman, null, ROT0, "Taito Corporation", "Superman", GAME_NO_COCKTAIL);
}
