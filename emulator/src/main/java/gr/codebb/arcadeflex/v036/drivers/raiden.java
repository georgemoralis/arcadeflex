
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.raiden.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.*;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295H.*;

import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.seibu.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;

public class raiden {

    public static UBytePtr raiden_shared_ram = new UBytePtr();

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr raiden_shared_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return raiden_shared_ram.read(offset);
        }
    };
    public static WriteHandlerPtr raiden_shared_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            raiden_shared_ram.write(offset, data);
        }
    };
    static int latch = 0;
    public static ReadHandlerPtr raiden_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {

            int erg, orig, coin = readinputport(4);
            orig = seibu_shared_sound_ram.read(offset);

            /* Small kludge to allows coins with sound off */
            if (coin == 0) {
                latch = 0;
            }
            if (offset == 4 && (Machine.sample_rate == 0) && coin != 0 && latch == 0) {
                latch = 1;
                return coin;
            }

            switch (offset) {/* misusing $d006 as a latch...but it works !*/

                case 0x04: {
                    erg = seibu_shared_sound_ram.read(6);
                    seibu_shared_sound_ram.write(6, 0);
                    break;
                } /* just 1 time */

                case 0x06: {
                    erg = 0xa0;
                    break;
                }
                case 0x0a: {
                    erg = 0;
                    break;
                }
                default:
                    erg = seibu_shared_sound_ram.read(offset);
            }
            return erg;
        }
    };

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x00000, 0x07fff, MRA_RAM),
                new MemoryReadAddress(0x0a000, 0x0afff, raiden_shared_r),
                new MemoryReadAddress(0x0b000, 0x0b000, input_port_0_r),
                new MemoryReadAddress(0x0b001, 0x0b001, input_port_1_r),
                new MemoryReadAddress(0x0b002, 0x0b002, input_port_2_r),
                new MemoryReadAddress(0x0b003, 0x0b003, input_port_3_r),
                new MemoryReadAddress(0x0d000, 0x0d00f, raiden_sound_r),
                new MemoryReadAddress(0xa0000, 0xfffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x00000, 0x06fff, MWA_RAM),
                new MemoryWriteAddress(0x07000, 0x07fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x0a000, 0x0afff, raiden_shared_w, raiden_shared_ram),
                new MemoryWriteAddress(0x0b000, 0x0b007, raiden_control_w),
                new MemoryWriteAddress(0x0c000, 0x0c7ff, raiden_text_w, videoram),
                new MemoryWriteAddress(0x0d000, 0x0d00f, seibu_soundlatch_w, seibu_shared_sound_ram),
                new MemoryWriteAddress(0x0d060, 0x0d067, MWA_RAM, raiden_scroll_ram),
                new MemoryWriteAddress(0xa0000, 0xfffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sub_readmem[]
            = {
                new MemoryReadAddress(0x00000, 0x01fff, MRA_RAM),
                new MemoryReadAddress(0x02000, 0x027ff, raiden_background_r),
                new MemoryReadAddress(0x02800, 0x02fff, raiden_foreground_r),
                new MemoryReadAddress(0x03000, 0x03fff, paletteram_r),
                new MemoryReadAddress(0x04000, 0x04fff, raiden_shared_r),
                new MemoryReadAddress(0xc0000, 0xfffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sub_writemem[]
            = {
                new MemoryWriteAddress(0x00000, 0x01fff, MWA_RAM),
                new MemoryWriteAddress(0x02000, 0x027ff, raiden_background_w, raiden_back_data),
                new MemoryWriteAddress(0x02800, 0x02fff, raiden_foreground_w, raiden_fore_data),
                new MemoryWriteAddress(0x03000, 0x03fff, paletteram_xxxxBBBBGGGGRRRR_w, paletteram),
                new MemoryWriteAddress(0x04000, 0x04fff, raiden_shared_w),
                new MemoryWriteAddress(0x07ffe, 0x0afff, MWA_NOP),
                new MemoryWriteAddress(0xc0000, 0xfffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *********************** Alternate board set ***********************
     */
    static MemoryReadAddress alt_readmem[]
            = {
                new MemoryReadAddress(0x00000, 0x07fff, MRA_RAM),
                new MemoryReadAddress(0x08000, 0x08fff, raiden_shared_r),
                new MemoryReadAddress(0x0a000, 0x0a00f, raiden_sound_r),
                new MemoryReadAddress(0x0e000, 0x0e000, input_port_0_r),
                new MemoryReadAddress(0x0e001, 0x0e001, input_port_1_r),
                new MemoryReadAddress(0x0e002, 0x0e002, input_port_2_r),
                new MemoryReadAddress(0x0e003, 0x0e003, input_port_3_r),
                new MemoryReadAddress(0xa0000, 0xfffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress alt_writemem[]
            = {
                new MemoryWriteAddress(0x00000, 0x06fff, MWA_RAM),
                new MemoryWriteAddress(0x07000, 0x07fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x08000, 0x08fff, raiden_shared_w, raiden_shared_ram),
                new MemoryWriteAddress(0x0a000, 0x0a00f, seibu_soundlatch_w, seibu_shared_sound_ram),
                new MemoryWriteAddress(0x0b000, 0x0b007, raiden_control_w),
                new MemoryWriteAddress(0x0c000, 0x0c7ff, raidena_text_w, videoram),
                new MemoryWriteAddress(0x0f000, 0x0f035, MWA_RAM, raiden_scroll_ram),
                new MemoryWriteAddress(0xa0000, 0xfffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x4008, 0x4008, YM3812_status_port_0_r),
                new MemoryReadAddress(0x4010, 0x4012, seibu_soundlatch_r),
                new MemoryReadAddress(0x4013, 0x4013, input_port_4_r), /* Coin port */
                new MemoryReadAddress(0x6000, 0x6000, OKIM6295_status_0_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_BANK1),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x4000, seibu_soundclear_w),
                new MemoryWriteAddress(0x4002, 0x4002, seibu_rst10_ack),
                new MemoryWriteAddress(0x4003, 0x4003, seibu_rst18_ack),
                new MemoryWriteAddress(0x4007, 0x4007, seibu_bank_w),
                new MemoryWriteAddress(0x4008, 0x4008, YM3812_control_port_0_w),
                new MemoryWriteAddress(0x4009, 0x4009, YM3812_write_port_0_w),
                new MemoryWriteAddress(0x4018, 0x401f, seibu_main_data_w),
                new MemoryWriteAddress(0x6000, 0x6000, OKIM6295_data_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    static InputPortPtr input_ports_raiden = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START(); 	/* Dip switch A */

            PORT_DIPNAME(0x01, 0x01, "Coin Mode");
            PORT_DIPSETTING(0x01, "A");
            PORT_DIPSETTING(0x00, "B");
            /* Coin Mode A */
            PORT_DIPNAME(0x1e, 0x1e, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x14, DEF_STR("6C_1C"));
            PORT_DIPSETTING(0x16, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x1a, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("8C_3C"));
            PORT_DIPSETTING(0x1c, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("5C_3C"));
            PORT_DIPSETTING(0x06, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x1e, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x12, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            /* Coin Mode B */
            /*	PORT_DIPNAME( 0x06, 0x06, DEF_STR( "Coin_A") );
             PORT_DIPSETTING(    0x00, "5C/1C or Free if Coin B too" );
             PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
             PORT_DIPSETTING(    0x04, DEF_STR( "2C_1C") );
             PORT_DIPSETTING(    0x06, DEF_STR( "1C_1C") );
             PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Coin_B") );
             PORT_DIPSETTING(    0x18, DEF_STR( "1C_2C") );
             PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
             PORT_DIPSETTING(    0x08, DEF_STR( "1C_5C") );
             PORT_DIPSETTING(    0x00, "1C/6C or Free if Coin A too" );*/
            PORT_DIPNAME(0x20, 0x20, "Credits to Start");
            PORT_DIPSETTING(0x20, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unused"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* Dip switch B */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x02, "1");
            PORT_DIPSETTING(0x01, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "80000 300000");
            PORT_DIPSETTING(0x0c, "150000 400000");
            PORT_DIPSETTING(0x04, "300000 1000000");
            PORT_DIPSETTING(0x00, "1000000 5000000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x40, 0x40, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x40, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START(); 	/* Coins */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            INPUT_PORTS_END();
        }
    };

    /**
     * ***************************************************************************
     */
    static GfxLayout raiden_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 512 characters */
            4, /* 4 bits per pixel */
            new int[]{4, 0, (0x08000 * 8) + 4, 0x08000 * 8},
            new int[]{0, 1, 2, 3, 8, 9, 10, 11},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            128
    );

    static GfxLayout raiden_spritelayout = new GfxLayout(
            16, 16, /* 16*16 tiles */
            4096, /* 2048*4 tiles */
            4, /* 4 bits per pixel */
            new int[]{12, 8, 4, 0},
            new int[]{
                0, 1, 2, 3, 16, 17, 18, 19,
                512 + 0, 512 + 1, 512 + 2, 512 + 3,
                512 + 8 + 8, 512 + 9 + 8, 512 + 10 + 8, 512 + 11 + 8,},
            new int[]{
                0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32,},
            1024
    );

    static GfxDecodeInfo raiden_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, raiden_charlayout, 768, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, raiden_spritelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, raiden_spritelayout, 256, 16),
                new GfxDecodeInfo(REGION_GFX4, 0, raiden_spritelayout, 512, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ***************************************************************************
     */
    /* Parameters: YM3812 frequency, Oki frequency, Oki memory region */
    static YM3812interface ym3812_interface = new YM3812interface(
            1,
            14318180 / 4,
            new int[]{50},
            new WriteYmHandlerPtr[]{seibu_ym3812_irqhandler}
    );

    static OKIM6295interface okim6295_interface = new OKIM6295interface(
            1,
            new int[]{8000},
            new int[]{REGION_SOUND1},
            new int[]{40}
    );

    public static InterruptPtr raiden_interrupt = new InterruptPtr() {
        public int handler() {
            return 0xc8 / 4;	/* VBL */

        }
    };
    public static VhEofCallbackPtr raiden_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            buffer_spriteram_w.handler(0, 0);/* Could be a memory location instead */

        }
    };

    static MachineDriver machine_driver_raiden = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_V30, /* NEC V30 CPU */
                        19000000, /* 20MHz is correct, but glitched!? */
                        readmem, writemem, null, null,
                        raiden_interrupt, 1
                ),
                new MachineCPU(
                        CPU_V30, /* NEC V30 CPU */
                        19000000, /* 20MHz is correct, but glitched!? */
                        sub_readmem, sub_writemem, null, null,
                        raiden_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 4,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION * 2, /* frames per second, vblank duration */
            70, /* CPU interleave  */
            seibu_sound_init_2,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            raiden_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_BUFFERS_SPRITERAM,
            raiden_eof_callback,
            raiden_vh_start,
            null,
            raiden_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    static MachineDriver machine_driver_raidena = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_V30, /* NEC V30 CPU */
                        19000000, /* 20MHz is correct, but glitched!? */
                        alt_readmem, alt_writemem, null, null,
                        raiden_interrupt, 1
                ),
                new MachineCPU(
                        CPU_V30, /* NEC V30 CPU */
                        19000000, /* 20MHz is correct, but glitched!? */
                        sub_readmem, sub_writemem, null, null,
                        raiden_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 4,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION * 2, /* frames per second, vblank duration */
            60, /* CPU interleave  */
            seibu_sound_init_2,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            raiden_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_BUFFERS_SPRITERAM,
            raiden_eof_callback,
            raiden_vh_start,
            null,
            raiden_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    /**
     * ************************************************************************
     */
    static RomLoadPtr rom_raiden = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x100000, REGION_CPU1);/* v30 main cpu */

            ROM_LOAD_ODD("rai1.bin", 0x0a0000, 0x10000, 0xa4b12785);
            ROM_LOAD_EVEN("rai2.bin", 0x0a0000, 0x10000, 0x17640bd5);
            ROM_LOAD_ODD("rai3.bin", 0x0c0000, 0x20000, 0x9d735bf5);
            ROM_LOAD_EVEN("rai4.bin", 0x0c0000, 0x20000, 0x8d184b99);

            ROM_REGION(0x100000, REGION_CPU2);/* v30 sub cpu */

            ROM_LOAD_ODD("rai5.bin", 0x0c0000, 0x20000, 0x7aca6d61);
            ROM_LOAD_EVEN("rai6a.bin", 0x0c0000, 0x20000, 0xe3d35cc2);

            ROM_REGION(0x18000, REGION_CPU3);/* 64k code for sound Z80 */

            ROM_LOAD("rai6.bin", 0x000000, 0x08000, 0x723a483b);
            ROM_CONTINUE(0x010000, 0x08000);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rai9.bin", 0x00000, 0x08000, 0x1922b25e);/* chars */

            ROM_LOAD("rai10.bin", 0x08000, 0x08000, 0x5f90786a);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0919.bin", 0x00000, 0x80000, 0xda151f0b);/* tiles */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0920.bin", 0x00000, 0x80000, 0xac1f57ac);/* tiles */

            ROM_REGION(0x090000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu165.bin", 0x00000, 0x80000, 0x946d7bde);/* sprites */

            ROM_REGION(0x10000, REGION_SOUND1); /* ADPCM samples */

            ROM_LOAD("rai7.bin", 0x00000, 0x10000, 0x8f927822);
            ROM_END();
        }
    };

    static RomLoadPtr rom_raidena = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x100000, REGION_CPU1);/* v30 main cpu */

            ROM_LOAD_ODD("rai1.bin", 0x0a0000, 0x10000, 0xa4b12785);
            ROM_LOAD_EVEN("rai2.bin", 0x0a0000, 0x10000, 0x17640bd5);
            ROM_LOAD_ODD("raiden03.rom", 0x0c0000, 0x20000, 0xf6af09d0);
            ROM_LOAD_EVEN("raiden04.rom", 0x0c0000, 0x20000, 0x6bdfd416);

            ROM_REGION(0x100000, REGION_CPU2);/* v30 sub cpu */

            ROM_LOAD_ODD("raiden05.rom", 0x0c0000, 0x20000, 0xed03562e);
            ROM_LOAD_EVEN("raiden06.rom", 0x0c0000, 0x20000, 0xa19d5b5d);

            ROM_REGION(0x18000, REGION_CPU3);/* 64k code for sound Z80 */

            ROM_LOAD("raiden08.rom", 0x000000, 0x08000, 0x731adb43);
            ROM_CONTINUE(0x010000, 0x08000);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rai9.bin", 0x00000, 0x08000, 0x1922b25e);/* chars */

            ROM_LOAD("rai10.bin", 0x08000, 0x08000, 0x5f90786a);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0919.bin", 0x00000, 0x80000, 0xda151f0b);/* tiles */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0920.bin", 0x00000, 0x80000, 0xac1f57ac);/* tiles */

            ROM_REGION(0x090000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu165.bin", 0x00000, 0x80000, 0x946d7bde);/* sprites */

            ROM_REGION(0x10000, REGION_SOUND1); /* ADPCM samples */

            ROM_LOAD("rai7.bin", 0x00000, 0x10000, 0x8f927822);
            ROM_END();
        }
    };

    static RomLoadPtr rom_raidenk = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x100000, REGION_CPU1);/* v30 main cpu */

            ROM_LOAD_ODD("rai1.bin", 0x0a0000, 0x10000, 0xa4b12785);
            ROM_LOAD_EVEN("rai2.bin", 0x0a0000, 0x10000, 0x17640bd5);
            ROM_LOAD_ODD("raiden03.rom", 0x0c0000, 0x20000, 0xf6af09d0);
            ROM_LOAD_EVEN("1i", 0x0c0000, 0x20000, 0xfddf24da);

            ROM_REGION(0x100000, REGION_CPU2);/* v30 sub cpu */

            ROM_LOAD_ODD("raiden05.rom", 0x0c0000, 0x20000, 0xed03562e);
            ROM_LOAD_EVEN("raiden06.rom", 0x0c0000, 0x20000, 0xa19d5b5d);

            ROM_REGION(0x18000, REGION_CPU3);/* 64k code for sound Z80 */

            ROM_LOAD("8b", 0x000000, 0x08000, 0x99ee7505);
            ROM_CONTINUE(0x010000, 0x08000);

            ROM_REGION(0x010000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("rai9.bin", 0x00000, 0x08000, 0x1922b25e);/* chars */

            ROM_LOAD("rai10.bin", 0x08000, 0x08000, 0x5f90786a);

            ROM_REGION(0x080000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0919.bin", 0x00000, 0x80000, 0xda151f0b);/* tiles */

            ROM_REGION(0x080000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu0920.bin", 0x00000, 0x80000, 0xac1f57ac);/* tiles */

            ROM_REGION(0x090000, REGION_GFX4 | REGIONFLAG_DISPOSE);
            ROM_LOAD("raiu165.bin", 0x00000, 0x80000, 0x946d7bde);/* sprites */

            ROM_REGION(0x10000, REGION_SOUND1); /* ADPCM samples */

            ROM_LOAD("rai7.bin", 0x00000, 0x10000, 0x8f927822);
            ROM_END();
        }
    };

    /**
     * ************************************************************************
     */
    /* Spin the sub-cpu if it is waiting on the master cpu */
    public static ReadHandlerPtr sub_cpu_spin = new ReadHandlerPtr() {
        public int handler(int offset) {
            int pc = cpu_get_pc();
            int ret = raiden_shared_ram.read(0x8);

            if (offset == 1) {
                return raiden_shared_ram.read(0x9);
            }

            if (pc == 0xfcde6 && ret != 0x40) {
                cpu_spin();
            }

            return ret;
        }
    };

    public static ReadHandlerPtr sub_cpu_spina = new ReadHandlerPtr() {
        public int handler(int offset) {
            int pc = cpu_get_pc();
            int ret = raiden_shared_ram.read(0x8);

            if (offset == 1) {
                return raiden_shared_ram.read(0x9);
            }

            if (pc == 0xfcde8 && ret != 0x40) {
                cpu_spin();
            }

            return ret;
        }
    };

    public static InitDriverPtr init_raiden = new InitDriverPtr() {
        public void handler() {
            install_mem_read_handler(1, 0x4008, 0x4009, sub_cpu_spin);
            install_seibu_sound_speedup(2);
        }
    };

    static void memory_patcha() {
        install_mem_read_handler(1, 0x4008, 0x4009, sub_cpu_spina);
        install_seibu_sound_speedup(2);
    }

    /* This is based on code by Niclas Karlsson Mate, who figured out the
     encryption method! The technique is a combination of a XOR table plus
     bit-swapping */
    static void common_decrypt() {
        UBytePtr RAM = memory_region(REGION_CPU1);
        int i, a;

        int xor_table[][] = {
            {0xF1, 0xF9, 0xF5, 0xFD, 0xF1, 0xF1, 0x3D, 0x3D, /* rom 3 */
                0x73, 0xFB, 0x77, 0xFF, 0x73, 0xF3, 0x3F, 0x3F},
            {0xDF, 0xFF, 0xFF, 0xFF, 0xDB, 0xFF, 0xFB, 0xFF, /* rom 4 */
                0xFF, 0xFF, 0xFF, 0xFF, 0xFB, 0xFF, 0xFB, 0xFF},
            {0x7F, 0x7F, 0xBB, 0x77, 0x77, 0x77, 0xBE, 0xF6, /* rom 5 */
                0x7F, 0x7F, 0xBB, 0x77, 0x77, 0x77, 0xBE, 0xF6},
            {0xFF, 0xFF, 0xFD, 0xFD, 0xFD, 0xFD, 0xEF, 0xEF, /* rom 6 */
                0xFF, 0xFF, 0xFD, 0xFD, 0xFD, 0xFD, 0xEF, 0xEF}
        };

        /* Rom 3 - main cpu even bytes */
        for (i = 0xc0000; i < 0x100000; i += 2) {
            a = RAM.read(i);
            a ^= xor_table[0][(i / 2) & 0x0f];
            a ^= 0xff;
            a = (a & 0x31) | ((a << 1) & 0x04) | ((a >> 5) & 0x02)
                    | ((a << 4) & 0x40) | ((a << 4) & 0x80) | ((a >> 4) & 0x08);
            RAM.write(i, a);
        }

        /* Rom 4 - main cpu odd bytes */
        for (i = 0xc0001; i < 0x100000; i += 2) {
            a = RAM.read(i);
            a ^= xor_table[1][(i / 2) & 0x0f];
            a ^= 0xff;
            a = (a & 0xdb) | ((a >> 3) & 0x04) | ((a << 3) & 0x20);
            RAM.write(i, a);
        }

        RAM = memory_region(REGION_CPU2);

        /* Rom 5 - sub cpu even bytes */
        for (i = 0xc0000; i < 0x100000; i += 2) {
            a = RAM.read(i);
            a ^= xor_table[2][(i / 2) & 0x0f];
            a ^= 0xff;
            a = (a & 0x32) | ((a >> 1) & 0x04) | ((a >> 4) & 0x08)
                    | ((a << 5) & 0x80) | ((a >> 6) & 0x01) | ((a << 6) & 0x40);
            RAM.write(i, a);
        }

        /* Rom 6 - sub cpu odd bytes */
        for (i = 0xc0001; i < 0x100000; i += 2) {
            a = RAM.read(i);
            a ^= xor_table[3][(i / 2) & 0x0f];
            a ^= 0xff;
            a = (a & 0xed) | ((a >> 3) & 0x02) | ((a << 3) & 0x10);
            RAM.write(i, a);
        }
    }

    public static InitDriverPtr init_raidenk = new InitDriverPtr() {
        public void handler() {
            memory_patcha();
            common_decrypt();
        }
    };

    public static InitDriverPtr init_raidena = new InitDriverPtr() {
        public void handler() {
            memory_patcha();
            common_decrypt();
            seibu_sound_decrypt();
        }
    };

    /**
     * ************************************************************************
     */
    public static GameDriver driver_raiden = new GameDriver("1990", "raiden", "raiden.java", rom_raiden, null, machine_driver_raiden, input_ports_raiden, init_raiden, ROT270, "Seibu Kaihatsu", "Raiden");
    public static GameDriver driver_raidena = new GameDriver("1990", "raidena", "raiden.java", rom_raidena, driver_raiden, machine_driver_raidena, input_ports_raiden, init_raidena, ROT270, "Seibu Kaihatsu", "Raiden (Alternate Hardware)", GAME_NO_SOUND);
    public static GameDriver driver_raidenk = new GameDriver("1990", "raidenk", "raiden.java", rom_raidenk, driver_raiden, machine_driver_raidena, input_ports_raiden, init_raidenk, ROT270, "Seibu Kaihatsu (IBL Corporation license)", "Raiden (Korea)");
}
