/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 25/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound._2203intf.*;
import static arcadeflex.v036.sound._2203intfH.*;
import static arcadeflex.v036.sound.samples.*;
import static arcadeflex.v036.sound.samplesH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.ninjakd2.*;
//to be organized
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v037b7.mame.palette.paletteram;
import static gr.codebb.arcadeflex.v037b7.mame.palette.paletteram_RRRRGGGGBBBBxxxx_swap_w;

public class ninjakd2 {

    static int ninjakd2_bank_latch = 255;

    public static ShStartHandlerPtr ninjakd2_init_samples = new ShStartHandlerPtr() {

        public int handler(MachineSound msound) {
            int i, n;
            UBytePtr source = memory_region(REGION_SOUND1);
            GameSamples samples;
            int sample_info[][] = {{0x0000, 0x0A00}, {0x0A00, 0x1D00}, {0x2700, 0x1700},
            {0x3E00, 0x1500}, {0x5300, 0x0B00}, {0x5E00, 0x0A00}, {0x6800, 0x0E00}, {0x7600, 0x1E00}, {0xF000, 0x0400}};

            //if ((Machine.samples = malloc(sizeof(struct GameSamples) + 9 * sizeof(struct GameSample *))) == null)
            //	return 1;
            Machine.samples = new GameSamples(9);

            //samples = Machine.samples;
            Machine.samples.total = 8;

            for (i = 0; i <= 8; i++) {
                //if ((samples.sample[i] = malloc(sizeof(struct GameSample) + (sample_info[i][1]))) == NULL)
                //	return 1;
                Machine.samples.sample[i] = new GameSample(sample_info[i][1]);

                Machine.samples.sample[i].length = sample_info[i][1];
                Machine.samples.sample[i].smpfreq = 16000;
                /* 16 kHz */
                Machine.samples.sample[i].resolution = 8;
                for (n = 0; n < sample_info[i][1]; n++) {
                    Machine.samples.sample[i].data[n] = (byte) (source.read(sample_info[i][0] + n) ^ 0x80);
                }
            }

            /*	The samples are now ready to be used.  They are a 8 bit, 16 kHz samples. 	 */
            return 0;
        }
    };

    public static InterruptHandlerPtr ninjakd2_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            return 0x00d7;
            /* RST 10h */
        }
    };

    public static ReadHandlerPtr ninjakd2_bankselect_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return ninjakd2_bank_latch;
        }
    };

    public static WriteHandlerPtr ninjakd2_bankselect_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int bankaddress;

            if (data != ninjakd2_bank_latch) {
                ninjakd2_bank_latch = data;

                bankaddress = 0x10000 + ((data & 0x7) * 0x4000);
                cpu_setbank(1, new UBytePtr(RAM, bankaddress));
                /* Select 8 banks of 16k */
            }
        }
    };

    public static WriteHandlerPtr ninjakd2_pcm_play_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;
            int sample_no[] = {0x00, 0x0A, 0x27, 0x3E, 0x53, 0x5E, 0x68, 0x76, 0xF0};

            for (i = 0; i < 9; i++) {
                if (sample_no[i] == data) {
                    break;
                }
            }

            if (i == 8) {
                sample_stop(0);
            } else {
                sample_start(0, i, 0);
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK1),
                new MemoryReadAddress(0xc000, 0xc000, input_port_2_r),
                new MemoryReadAddress(0xc001, 0xc001, input_port_0_r),
                new MemoryReadAddress(0xc002, 0xc002, input_port_1_r),
                new MemoryReadAddress(0xc003, 0xc003, input_port_3_r),
                new MemoryReadAddress(0xc004, 0xc004, input_port_4_r),
                new MemoryReadAddress(0xc200, 0xc200, MRA_RAM),
                new MemoryReadAddress(0xc201, 0xc201, MRA_RAM), // unknown but used
                new MemoryReadAddress(0xc202, 0xc202, ninjakd2_bankselect_r),
                new MemoryReadAddress(0xc203, 0xc203, MRA_RAM),
                new MemoryReadAddress(0xc208, 0xc209, MRA_RAM),
                new MemoryReadAddress(0xc20a, 0xc20b, MRA_RAM),
                new MemoryReadAddress(0xc20c, 0xc20c, MRA_RAM),
                new MemoryReadAddress(0xc800, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc200, 0xc200, soundlatch_w),
                new MemoryWriteAddress(0xc201, 0xc201, MWA_RAM), // unknown but used
                new MemoryWriteAddress(0xc202, 0xc202, ninjakd2_bankselect_w),
                new MemoryWriteAddress(0xc203, 0xc203, ninjakd2_sprite_overdraw_w, ninjakd2_spoverdraw_ram),
                new MemoryWriteAddress(0xc208, 0xc209, MWA_RAM, ninjakd2_scrollx_ram),
                new MemoryWriteAddress(0xc20a, 0xc20b, MWA_RAM, ninjakd2_scrolly_ram),
                new MemoryWriteAddress(0xc20c, 0xc20c, ninjakd2_background_enable_w, ninjakd2_bgenable_ram),
                new MemoryWriteAddress(0xc800, 0xcdff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xd000, 0xd7ff, ninjakd2_fgvideoram_w, ninjakd2_foreground_videoram, ninjakd2_foregroundram_size),
                new MemoryWriteAddress(0xd800, 0xdfff, ninjakd2_bgvideoram_w, ninjakd2_background_videoram, ninjakd2_backgroundram_size),
                new MemoryWriteAddress(0xe000, 0xf9ff, MWA_RAM),
                new MemoryWriteAddress(0xfa00, 0xffff, MWA_RAM, ninjakd2_spriteram, ninjakd2_spriteram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress snd_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe000, soundlatch_r),
                new MemoryReadAddress(0xefee, 0xefee, MRA_NOP),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress snd_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_RAM),
                new MemoryWriteAddress(0xf000, 0xf000, ninjakd2_pcm_play_w), /* PCM SAMPLE OFFSET*256 */
                new MemoryWriteAddress(0xeff5, 0xeff6, MWA_NOP), /* SAMPLE FREQUENCY ??? */
                new MemoryWriteAddress(0xefee, 0xefee, MWA_NOP), /* CHIP COMMAND ?? */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort snd_writeport[]
            = {
                new IOWritePort(0x0000, 0x0000, YM2203_control_port_0_w),
                new IOWritePort(0x0001, 0x0001, YM2203_write_port_0_w),
                new IOWritePort(0x0080, 0x0080, YM2203_control_port_1_w),
                new IOWritePort(0x0081, 0x0081, YM2203_write_port_1_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_ninjakd2 = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* player 2 controls */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_SERVICE);/* keep pressed during boot to enter service mode */
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* dsw0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "20000 50000");
            PORT_DIPSETTING(0x06, "30000 50000");
            PORT_DIPSETTING(0x02, "50000 100000");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x08, 0x08, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x08, DEF_STR("Yes"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Normal");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Lives"));
            PORT_DIPSETTING(0x40, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPNAME(0x80, 0x00, "Language");
            PORT_DIPSETTING(0x00, "English");
            PORT_DIPSETTING(0x80, "Japanese");

            PORT_START();
            /* dsw1 */
            PORT_SERVICE(0x01, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x02, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x00, "Credit Service");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPNAME(0xe0, 0xe0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 16384 * 8 + 0, 16384 * 8 + 4, 8, 12, 16384 * 8 + 8, 16384 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7},
            8 * 16
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 characters */
            1024, /* 1024 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0, 4, 65536 * 8 + 0, 65536 * 8 + 4, 8, 12, 65536 * 8 + 8, 65536 * 8 + 12,
                16 * 8 + 0, 16 * 8 + 4, 16 * 8 + 65536 * 8 + 0, 16 * 8 + 65536 * 8 + 4, 16 * 8 + 8, 16 * 8 + 12, 16 * 8 + 65536 * 8 + 8, 16 * 8 + 65536 * 8 + 12},
            new int[]{16 * 0, 16 * 1, 16 * 2, 16 * 3, 16 * 4, 16 * 5, 16 * 6, 16 * 7,
                32 * 8 + 16 * 0, 32 * 8 + 16 * 1, 32 * 8 + 16 * 2, 32 * 8 + 16 * 3, 32 * 8 + 16 * 4, 32 * 8 + 16 * 5, 32 * 8 + 16 * 6, 32 * 8 + 16 * 7},
            8 * 64
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0 * 16, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16 * 16, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, charlayout, 32 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static Samplesinterface samples_interface = new Samplesinterface(
            1, /* 1 channel */
            25, /* volume */
            null
    );

    static CustomSound_interface custom_interface = new CustomSound_interface(
            ninjakd2_init_samples,
            null,
            null
    );

    /* handler called by the 2203 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            cpu_set_irq_line(1, 0, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    static YM2203interface ym2203_interface = new YM2203interface(
            2, /* 2 chips */
            1500000, /* 12000000/8 MHz */
            new int[]{YM2203_VOL(25, 25), YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteYmHandlerPtr[]{irqhandler, null}
    );

    static MachineDriver machine_driver_ninjakd2 = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 12000000/2 ??? */
                        readmem, writemem, null, null, /* very sensitive to these settings */
                        ninjakd2_interrupt, 1
                )
            },
            60, 10000, /* frames per second, vblank duration */
            10, /* single CPU, no need for interleaving */
            null,
            32 * 8, 32 * 8,
            new rectangle(0 * 8, 32 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            48 * 16, 48 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            ninjakd2_vh_start,
            ninjakd2_vh_stop,
            ninjakd2_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                )
            }
    );

    static MachineDriver machine_driver_ninjak2a = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 12000000/2 ??? */
                        readmem, writemem, null, null, /* very sensitive to these settings */
                        ninjakd2_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 12000000/3 ??? */
                        snd_readmem, snd_writemem, null, snd_writeport,
                        ignore_interrupt, 0
                )
            },
            60, 10000, /* frames per second, vblank duration */
            10,
            null,
            32 * 8, 32 * 8,
            new rectangle(0 * 8, 32 * 8 - 1, 4 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            48 * 16, 48 * 16,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            ninjakd2_vh_start,
            ninjakd2_vh_stop,
            ninjakd2_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_SAMPLES,
                        samples_interface
                ),
                new MachineSound(
                        SOUND_CUSTOM, /* actually initializes the samples */
                        custom_interface
                )
            }
    );

    static RomLoadHandlerPtr rom_ninjakd2 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("nk2_01.rom", 0x00000, 0x8000, 0x3cdbb906);
            ROM_LOAD("nk2_02.rom", 0x10000, 0x8000, 0xb5ce9a1a);
            ROM_LOAD("nk2_03.rom", 0x18000, 0x8000, 0xad275654);
            ROM_LOAD("nk2_04.rom", 0x20000, 0x8000, 0xe7692a77);
            ROM_LOAD("nk2_05.rom", 0x28000, 0x8000, 0x5dac9426);

            ROM_REGION(0x10000, REGION_CPU2);
            ROM_LOAD("nk2_06.rom", 0x0000, 0x10000, 0xd3a18a79); // sound z80 code encrypted

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_11.rom", 0x00000, 0x4000, 0x41a714b3);/* background tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_10.rom", 0x08000, 0x4000, 0xc913c4ab);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_08.rom", 0x00000, 0x4000, 0x1b79c50a);/* sprites tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_07.rom", 0x08000, 0x4000, 0x0be5cd13);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_12.rom", 0x00000, 0x02000, 0xdb5657a9);/* foreground tiles */
            ROM_CONTINUE(0x04000, 0x02000);
            ROM_CONTINUE(0x02000, 0x02000);
            ROM_CONTINUE(0x06000, 0x02000);

            ROM_REGION(0x10000, REGION_SOUND1);
            ROM_LOAD("nk2_09.rom", 0x0000, 0x10000, 0xc1d2d170);/* raw pcm samples */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ninjak2a = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("nk2_01.bin", 0x00000, 0x8000, 0xe6adca65);
            ROM_LOAD("nk2_02.bin", 0x10000, 0x8000, 0xd9284bd1);
            ROM_LOAD("nk2_03.rom", 0x18000, 0x8000, 0xad275654);
            ROM_LOAD("nk2_04.rom", 0x20000, 0x8000, 0xe7692a77);
            ROM_LOAD("nk2_05.bin", 0x28000, 0x8000, 0x960725fb);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("nk2_06.bin", 0x10000, 0x8000, 0x7bfe6c9e);/* decrypted opcodes */
            ROM_CONTINUE(0x00000, 0x8000);
            /* decrypted data */

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_11.rom", 0x00000, 0x4000, 0x41a714b3);/* background tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_10.rom", 0x08000, 0x4000, 0xc913c4ab);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_08.rom", 0x00000, 0x4000, 0x1b79c50a);/* sprites tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_07.rom", 0x08000, 0x4000, 0x0be5cd13);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_12.rom", 0x00000, 0x02000, 0xdb5657a9);/* foreground tiles */
            ROM_CONTINUE(0x04000, 0x02000);
            ROM_CONTINUE(0x02000, 0x02000);
            ROM_CONTINUE(0x06000, 0x02000);

            ROM_REGION(0x10000, REGION_SOUND1);
            ROM_LOAD("nk2_09.rom", 0x0000, 0x10000, 0xc1d2d170);/* raw pcm samples */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_ninjak2b = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("1.3s", 0x00000, 0x8000, 0xcb4f4624);
            ROM_LOAD("2.3q", 0x10000, 0x8000, 0x0ad0c100);
            ROM_LOAD("nk2_03.rom", 0x18000, 0x8000, 0xad275654);
            ROM_LOAD("nk2_04.rom", 0x20000, 0x8000, 0xe7692a77);
            ROM_LOAD("nk2_05.rom", 0x28000, 0x8000, 0x5dac9426);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("nk2_06.bin", 0x10000, 0x8000, 0x7bfe6c9e);/* decrypted opcodes */
            ROM_CONTINUE(0x00000, 0x8000);
            /* decrypted data */

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_11.rom", 0x00000, 0x4000, 0x41a714b3);/* background tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_10.rom", 0x08000, 0x4000, 0xc913c4ab);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_08.rom", 0x00000, 0x4000, 0x1b79c50a);/* sprites tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_07.rom", 0x08000, 0x4000, 0x0be5cd13);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_12.rom", 0x00000, 0x02000, 0xdb5657a9);/* foreground tiles */
            ROM_CONTINUE(0x04000, 0x02000);
            ROM_CONTINUE(0x02000, 0x02000);
            ROM_CONTINUE(0x06000, 0x02000);

            ROM_REGION(0x10000, REGION_SOUND1);
            ROM_LOAD("nk2_09.rom", 0x0000, 0x10000, 0xc1d2d170);/* raw pcm samples */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_rdaction = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x30000, REGION_CPU1);
            ROM_LOAD("1.3u", 0x00000, 0x8000, 0x5c475611);
            ROM_LOAD("2.3s", 0x10000, 0x8000, 0xa1e23bd2);
            ROM_LOAD("nk2_03.rom", 0x18000, 0x8000, 0xad275654);
            ROM_LOAD("nk2_04.rom", 0x20000, 0x8000, 0xe7692a77);
            ROM_LOAD("nk2_05.bin", 0x28000, 0x8000, 0x960725fb);

            ROM_REGION(2 * 0x10000, REGION_CPU2);/* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("nk2_06.bin", 0x10000, 0x8000, 0x7bfe6c9e);/* decrypted opcodes */
            ROM_CONTINUE(0x00000, 0x8000);
            /* decrypted data */

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_11.rom", 0x00000, 0x4000, 0x41a714b3);/* background tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_10.rom", 0x08000, 0x4000, 0xc913c4ab);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("nk2_08.rom", 0x00000, 0x4000, 0x1b79c50a);/* sprites tiles */
            ROM_CONTINUE(0x10000, 0x4000);
            ROM_CONTINUE(0x04000, 0x4000);
            ROM_CONTINUE(0x14000, 0x4000);
            ROM_LOAD("nk2_07.rom", 0x08000, 0x4000, 0x0be5cd13);
            ROM_CONTINUE(0x18000, 0x4000);
            ROM_CONTINUE(0x0c000, 0x4000);
            ROM_CONTINUE(0x1c000, 0x4000);

            ROM_REGION(0x08000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("12.5n", 0x00000, 0x02000, 0x0936b365);/* foreground tiles */
            ROM_CONTINUE(0x04000, 0x02000);
            ROM_CONTINUE(0x02000, 0x02000);
            ROM_CONTINUE(0x06000, 0x02000);

            ROM_REGION(0x10000, REGION_SOUND1);
            ROM_LOAD("nk2_09.rom", 0x0000, 0x10000, 0xc1d2d170);/* raw pcm samples */
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_ninjak2a = new InitDriverHandlerPtr() {
        public void handler() {
            UBytePtr rom = memory_region(REGION_CPU2);
            int diff = memory_region_length(REGION_CPU2) / 2;

            memory_set_opcode_base(1, new UBytePtr(rom, diff));
        }
    };

    public static GameDriver driver_ninjakd2 = new GameDriver("1987", "ninjakd2", "ninjakd2.java", rom_ninjakd2, null, machine_driver_ninjakd2, input_ports_ninjakd2, null, ROT0, "UPL", "Ninja Kid II (set 1)", GAME_NO_SOUND);/* sound program is encrypted */
    public static GameDriver driver_ninjak2a = new GameDriver("1987", "ninjak2a", "ninjakd2.java", rom_ninjak2a, driver_ninjakd2, machine_driver_ninjak2a, input_ports_ninjakd2, init_ninjak2a, ROT0, "UPL", "Ninja Kid II (set 2)");
    public static GameDriver driver_ninjak2b = new GameDriver("1987", "ninjak2b", "ninjakd2.java", rom_ninjak2b, driver_ninjakd2, machine_driver_ninjak2a, input_ports_ninjakd2, init_ninjak2a, ROT0, "UPL", "Ninja Kid II (set 3)");
    public static GameDriver driver_rdaction = new GameDriver("1987", "rdaction", "ninjakd2.java", rom_rdaction, driver_ninjakd2, machine_driver_ninjak2a, input_ports_ninjakd2, init_ninjak2a, ROT0, "UPL (World Games license)", "Rad Action");
}
