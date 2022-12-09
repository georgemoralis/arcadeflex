/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.drivers;

//machine imports
import static arcadeflex.v036.machine.galaga.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.samplesH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.galaga.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.sound.namcoH.*;
import static gr.codebb.arcadeflex.v036.sound.namco.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;

public class galaga {

    static MemoryReadAddress readmem_cpu1[]
            = {
                new MemoryReadAddress(0x8000, 0x9fff, galaga_sharedram_r),
                new MemoryReadAddress(0x6800, 0x6807, galaga_dsw_r),
                new MemoryReadAddress(0x7000, 0x700f, galaga_customio_data_r),
                new MemoryReadAddress(0x7100, 0x7100, galaga_customio_r),
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x8000, 0x9fff, galaga_sharedram_r),
                new MemoryReadAddress(0x6800, 0x6807, galaga_dsw_r),
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu3[]
            = {
                new MemoryReadAddress(0x8000, 0x9fff, galaga_sharedram_r),
                new MemoryReadAddress(0x6800, 0x6807, galaga_dsw_r),
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu1[]
            = {
                new MemoryWriteAddress(0x8000, 0x9fff, galaga_sharedram_w, galaga_sharedram),
                new MemoryWriteAddress(0x6830, 0x6830, MWA_NOP),
                new MemoryWriteAddress(0x7000, 0x700f, galaga_customio_data_w),
                new MemoryWriteAddress(0x7100, 0x7100, galaga_customio_w),
                new MemoryWriteAddress(0xa000, 0xa005, MWA_RAM, galaga_starcontrol),
                new MemoryWriteAddress(0x6820, 0x6820, galaga_interrupt_enable_1_w),
                new MemoryWriteAddress(0x6822, 0x6822, galaga_interrupt_enable_3_w),
                new MemoryWriteAddress(0x6823, 0x6823, galaga_halt_w),
                new MemoryWriteAddress(0xa007, 0xa007, galaga_flipscreen_w),
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8b80, 0x8bff, MWA_RAM, spriteram, spriteram_size), /* these three are here just to initialize */
                new MemoryWriteAddress(0x9380, 0x93ff, MWA_RAM, spriteram_2), /* the pointers. The actual writes are */
                new MemoryWriteAddress(0x9b80, 0x9bff, MWA_RAM, spriteram_3), /* handled by galaga_sharedram_w() */
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM, videoram, videoram_size), /* dirtybuffer[] handling is not needed because */
                new MemoryWriteAddress(0x8400, 0x87ff, MWA_RAM, colorram), /* characters are redrawn every frame */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x8000, 0x9fff, galaga_sharedram_w),
                new MemoryWriteAddress(0x6821, 0x6821, galaga_interrupt_enable_2_w),
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu3[]
            = {
                new MemoryWriteAddress(0x8000, 0x9fff, galaga_sharedram_w),
                new MemoryWriteAddress(0x6800, 0x681f, pengo_sound_w, namco_soundregs),
                new MemoryWriteAddress(0x6822, 0x6822, galaga_interrupt_enable_3_w),
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_galaga = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* TODO: bonus scores are different for 5 lives */
            PORT_DIPNAME(0x38, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x20, "20K 60K 60K");
            PORT_DIPSETTING(0x18, "20K 60K");
            PORT_DIPSETTING(0x10, "20K 70K 70K");
            PORT_DIPSETTING(0x30, "20K 80K 80K");
            PORT_DIPSETTING(0x38, "30K 80K");
            PORT_DIPSETTING(0x08, "30K 100K 100K");
            PORT_DIPSETTING(0x28, "30K 120K 120K");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0xc0, "5");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, "2 Credits Game");
            PORT_DIPSETTING(0x00, "1 Player");
            PORT_DIPSETTING(0x01, "2 Players");
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x06, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x04, "Hardest");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Freeze");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* FAKE */
 /* The player inputs are not memory mapped, they are handled by an I/O chip. */
 /* These fake input ports are read by galaga_customio_data_r() */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* FAKE */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* FAKE */
 /* the button here is used to trigger the sound in the test screen */
            PORT_BITX(0x03, IP_ACTIVE_LOW, IPT_BUTTON1, null, IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_START1, 1);
            PORT_BIT_IMPULSE(0x08, IP_ACTIVE_LOW, IPT_START2, 1);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_COIN1, 1);
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN3, 1);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    /* same as galaga, dip switches are slightly different */
    static InputPortPtr input_ports_galaganm = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x07, 0x07, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            /* TODO: bonus scores are different for 5 lives */
            PORT_DIPNAME(0x38, 0x10, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x20, "20K 60K 60K");
            PORT_DIPSETTING(0x18, "20K 60K");
            PORT_DIPSETTING(0x10, "20K 70K 70K");
            PORT_DIPSETTING(0x30, "20K 80K 80K");
            PORT_DIPSETTING(0x38, "30K 80K");
            PORT_DIPSETTING(0x08, "30K 100K 100K");
            PORT_DIPSETTING(0x28, "30K 120K 120K");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0xc0, 0x80, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x80, "3");
            PORT_DIPSETTING(0x40, "4");
            PORT_DIPSETTING(0xc0, "5");

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x03, "Easy");
            PORT_DIPSETTING(0x00, "Medium");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x02, "Hardest");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, "Freeze");
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x20, 0x20, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* FAKE */
 /* The player inputs are not memory mapped, they are handled by an I/O chip. */
 /* These fake input ports are read by galaga_customio_data_r() */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* FAKE */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, 1);
            PORT_BITX(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL, null, IP_KEY_PREVIOUS, IP_JOY_PREVIOUS);
            PORT_BIT(0xc0, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* FAKE */
 /* the button here is used to trigger the sound in the test screen */
            PORT_BITX(0x03, IP_ACTIVE_LOW, IPT_BUTTON1, null, IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_BIT_IMPULSE(0x04, IP_ACTIVE_LOW, IPT_START1, 1);
            PORT_BIT_IMPULSE(0x08, IP_ACTIVE_LOW, IPT_START2, 1);
            PORT_BIT_IMPULSE(0x10, IP_ACTIVE_LOW, IPT_COIN1, 1);
            PORT_BIT_IMPULSE(0x20, IP_ACTIVE_LOW, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN3, 1);
            PORT_SERVICE(0x80, IP_ACTIVE_LOW);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            128, /* 128 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* characters are rotated 90 degrees */
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3,
                24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 32),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 32 * 4, 32),
                new GfxDecodeInfo(-1) /* end of array */};

    static namco_interface namco_interface = new namco_interface(
            3072000 / 32, /* sample rate */
            3, /* number of voices */
            100, /* playback volume */
            REGION_SOUND1 /* memory region */
    );

    static String galaga_sample_names[]
            = {
                "*galaga",
                "bang.wav",
                null /* end of array */};

    static Samplesinterface samples_interface = new Samplesinterface(
            1, /* one channel */
            80, /* volume */
            galaga_sample_names
    );

    static MachineDriver machine_driver_galaga = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu1, writemem_cpu1, null, null,
                        galaga_interrupt_1, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu2, writemem_cpu2, null, null,
                        galaga_interrupt_2, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3125000, /* 3.125 Mhz */
                        readmem_cpu3, writemem_cpu3, null, null,
                        galaga_interrupt_3, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            99, /* 99 CPU slices per frame - with 100, galagab2 hangs on coin insertion */
            galaga_init_machine,
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32 + 64, 64 * 4, /* 32 for the characters, 64 for the stars */
            galaga_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            galaga_vh_start,
            generic_vh_stop,
            galaga_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_NAMCO,
                        namco_interface
                ),
                new MachineSound(
                        SOUND_SAMPLES,
                        samples_interface
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
    static RomLoadPtr rom_galaga = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("04m_g01.bin", 0x0000, 0x1000, 0xa3a0f743);
            ROM_LOAD("04k_g02.bin", 0x1000, 0x1000, 0x43bb0d5c);
            ROM_LOAD("04j_g03.bin", 0x2000, 0x1000, 0x753ce503);
            ROM_LOAD("04h_g04.bin", 0x3000, 0x1000, 0x83874442);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("04e_g05.bin", 0x0000, 0x1000, 0x3102fccd);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("04d_g06.bin", 0x0000, 0x1000, 0x8995088d);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07m_g08.bin", 0x0000, 0x1000, 0x58b2f47c);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galagamw = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("3200a.bin", 0x0000, 0x1000, 0x3ef0b053);
            ROM_LOAD("3300b.bin", 0x1000, 0x1000, 0x1b280831);
            ROM_LOAD("3400c.bin", 0x2000, 0x1000, 0x16233d33);
            ROM_LOAD("3500d.bin", 0x3000, 0x1000, 0x0aaf5c23);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("3600e.bin", 0x0000, 0x1000, 0xbc556e76);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("3700g.bin", 0x0000, 0x1000, 0xb07f0aa4);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07m_g08.bin", 0x0000, 0x1000, 0x58b2f47c);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galagads = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("3200a.bin", 0x0000, 0x1000, 0x3ef0b053);
            ROM_LOAD("3300b.bin", 0x1000, 0x1000, 0x1b280831);
            ROM_LOAD("3400c.bin", 0x2000, 0x1000, 0x16233d33);
            ROM_LOAD("3500d.bin", 0x3000, 0x1000, 0x0aaf5c23);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("3600fast.bin", 0x0000, 0x1000, 0x23d586e5);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("3700g.bin", 0x0000, 0x1000, 0xb07f0aa4);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07m_g08.bin", 0x0000, 0x1000, 0x58b2f47c);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_gallag = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("04m_g01.bin", 0x0000, 0x1000, 0xa3a0f743);
            ROM_LOAD("gallag.2", 0x1000, 0x1000, 0x5eda60a7);
            ROM_LOAD("04j_g03.bin", 0x2000, 0x1000, 0x753ce503);
            ROM_LOAD("04h_g04.bin", 0x3000, 0x1000, 0x83874442);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("04e_g05.bin", 0x0000, 0x1000, 0x3102fccd);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("04d_g06.bin", 0x0000, 0x1000, 0x8995088d);

            ROM_REGION(0x10000, REGION_CPU4);/* 64k for a Z80 which emulates the custom I/O chip (not used) */
            ROM_LOAD("gallag.6", 0x0000, 0x1000, 0x001b70bc);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gallag.8", 0x0000, 0x1000, 0x169a98a4);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galagab2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("g1", 0x0000, 0x1000, 0xab036c9f);
            ROM_LOAD("g2", 0x1000, 0x1000, 0xd9232240);
            ROM_LOAD("04j_g03.bin", 0x2000, 0x1000, 0x753ce503);
            ROM_LOAD("g4", 0x3000, 0x1000, 0x499fcc76);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("04e_g05.bin", 0x0000, 0x1000, 0x3102fccd);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("04d_g06.bin", 0x0000, 0x1000, 0x8995088d);

            ROM_REGION(0x10000, REGION_CPU4);/* 64k for a Z80 which emulates the custom I/O chip (not used) */
            ROM_LOAD("10h_g07.bin", 0x0000, 0x1000, 0x035e300c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gallag.8", 0x0000, 0x1000, 0x169a98a4);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_galaga84 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("g1", 0x0000, 0x1000, 0xab036c9f);
            ROM_LOAD("gal84_u2", 0x1000, 0x1000, 0x4d832a30);
            ROM_LOAD("04j_g03.bin", 0x2000, 0x1000, 0x753ce503);
            ROM_LOAD("g4", 0x3000, 0x1000, 0x499fcc76);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("gal84_u5", 0x0000, 0x1000, 0xbb5caae3);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("04d_g06.bin", 0x0000, 0x1000, 0x8995088d);

            ROM_REGION(0x10000, REGION_CPU4);/* 64k for a Z80 which emulates the custom I/O chip (not used) */
            ROM_LOAD("10h_g07.bin", 0x0000, 0x1000, 0x035e300c);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07m_g08.bin", 0x0000, 0x1000, 0x58b2f47c);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gal84u4d", 0x0000, 0x1000, 0x22e339d5);
            ROM_LOAD("gal84u4e", 0x1000, 0x1000, 0x60dcf940);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    static RomLoadPtr rom_nebulbee = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64k for code for the first CPU  */
            ROM_LOAD("nebulbee.01", 0x0000, 0x1000, 0xf405f2c4);
            ROM_LOAD("nebulbee.02", 0x1000, 0x1000, 0x31022b60);
            ROM_LOAD("04j_g03.bin", 0x2000, 0x1000, 0x753ce503);
            ROM_LOAD("nebulbee.04", 0x3000, 0x1000, 0xd76788a5);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the second CPU */
            ROM_LOAD("04e_g05.bin", 0x0000, 0x1000, 0x3102fccd);

            ROM_REGION(0x10000, REGION_CPU3);
            /* 64k for the third CPU  */
            ROM_LOAD("04d_g06.bin", 0x0000, 0x1000, 0x8995088d);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07m_g08.bin", 0x0000, 0x1000, 0x58b2f47c);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("07e_g10.bin", 0x0000, 0x1000, 0xad447c80);
            ROM_LOAD("07h_g09.bin", 0x1000, 0x1000, 0xdd6f1afc);

            ROM_REGION(0x0320, REGION_PROMS);
            ROM_LOAD("5n.bin", 0x0000, 0x0020, 0x54603c6b);/* palette */
            ROM_LOAD("2n.bin", 0x0020, 0x0100, 0xa547d33b);/* char lookup table */
            ROM_LOAD("1c.bin", 0x0120, 0x0100, 0xb6f585fb);/* sprite lookup table */
            ROM_LOAD("5c.bin", 0x0220, 0x0100, 0x8bd565f6);/* unknown */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
            ROM_LOAD("1d.bin", 0x0000, 0x0100, 0x86d92b24);
            ROM_END();
        }
    };

    public static GameDriver driver_galaga = new GameDriver("1981", "galaga", "galaga.java", rom_galaga, null, machine_driver_galaga, input_ports_galaganm, null, ROT90, "Namco", "Galaga (Namco)");
    public static GameDriver driver_galagamw = new GameDriver("1981", "galagamw", "galaga.java", rom_galagamw, driver_galaga, machine_driver_galaga, input_ports_galaga, null, ROT90, "[Namco] (Midway license)", "Galaga (Midway)");
    public static GameDriver driver_galagads = new GameDriver("1981", "galagads", "galaga.java", rom_galagads, driver_galaga, machine_driver_galaga, input_ports_galaga, null, ROT90, "hack", "Galaga (fast shoot)");
    public static GameDriver driver_gallag = new GameDriver("1982", "gallag", "galaga.java", rom_gallag, driver_galaga, machine_driver_galaga, input_ports_galaganm, null, ROT90, "bootleg", "Gallag");
    public static GameDriver driver_galagab2 = new GameDriver("1981", "galagab2", "galaga.java", rom_galagab2, driver_galaga, machine_driver_galaga, input_ports_galaganm, null, ROT90, "bootleg", "Galaga (bootleg)");
    public static GameDriver driver_galaga84 = new GameDriver("1984", "galaga84", "galaga.java", rom_galaga84, driver_galaga, machine_driver_galaga, input_ports_galaganm, null, ROT90, "hack", "Galaga '84");
    public static GameDriver driver_nebulbee = new GameDriver("1984", "nebulbee", "galaga.java", rom_nebulbee, driver_galaga, machine_driver_galaga, input_ports_galaganm, null, ROT90, "hack", "Nebulous Bee");
}
