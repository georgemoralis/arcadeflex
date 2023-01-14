/*
 * ported to v0.36
 * 
 */
/**
 * Changelog
 * =========
 * 14/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.i8039.i8039H.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.konami.*;
//mame imports
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.sound.dac.*;
import static arcadeflex.v036.sound.dacH.*;
import static arcadeflex.v036.sound.streams.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.megazone.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;

public class megazone {

    static UBytePtr megazone_sharedram = new UBytePtr();
    static int i8039_irqenable;
    static int i8039_status;

    public static ReadHandlerPtr megazone_portA_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int clock, timer;

            /* main xtal 14.318MHz, divided by 8 to get the AY-3-8910 clock, further */
 /* divided by 1024 to get this timer */
 /* The base clock for the CPU and 8910 is NOT the same, so we have to */
 /* compensate. */
 /* (divide by (1024/2), and not 1024, because the CPU cycle counter is */
 /* incremented every other state change of the clock) */
            clock = cpu_gettotalcycles() * 7159 / 12288;
            /* = (14318/8)/(18432/6) */
            timer = (clock / (1024 / 2)) & 0x0f;

            /* low three bits come from the 8039 */
            return (timer << 4) | i8039_status;
        }
    };

    public static WriteHandlerPtr megazone_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;

            for (i = 0; i < 3; i++) {
                int C;

                C = 0;
                if ((data & 1) != 0) {
                    C += 10000;
                    /*  10000pF = 0.01uF */
                }
                if ((data & 2) != 0) {
                    C += 220000;
                    /* 220000pF = 0.22uF */
                }
                data >>= 2;
                set_RC_filter(i, 1000, 2200, 200, C);
            }
        }
    };

    public static WriteHandlerPtr megazone_videoram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (megazone_videoram2.read(offset) != data) {
                megazone_videoram2.write(offset, data);
            }
        }
    };

    public static WriteHandlerPtr megazone_colorram2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (megazone_colorram2.read(offset) != data) {
                megazone_colorram2.write(offset, data);
            }
        }
    };

    public static ReadHandlerPtr megazone_dip3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (0xff);
        }
    };

    public static ReadHandlerPtr megazone_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (megazone_sharedram.read(offset));
        }
    };

    public static WriteHandlerPtr megazone_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            megazone_sharedram.write(offset, data);
        }
    };

    public static WriteHandlerPtr megazone_i8039_irq_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (i8039_irqenable != 0) {
                cpu_cause_interrupt(2, I8039_EXT_INT);
            }
        }
    };

    public static WriteHandlerPtr i8039_irqen_and_status_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            i8039_irqenable = data & 0x80;
            i8039_status = (data & 0x70) >> 4;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x2000, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x33ff, MRA_RAM),
                new MemoryReadAddress(0x3800, 0x3fff, megazone_sharedram_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM), /* 4000.5FFF is a debug rom */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0007, 0x0007, interrupt_enable_w),
                new MemoryWriteAddress(0x0800, 0x0800, watchdog_reset_w),
                new MemoryWriteAddress(0x1800, 0x1800, MWA_RAM, megazone_scrollx),
                new MemoryWriteAddress(0x1000, 0x1000, MWA_RAM, megazone_scrolly),
                new MemoryWriteAddress(0x2000, 0x23ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x2400, 0x27ff, megazone_videoram2_w, megazone_videoram2, megazone_videoram2_size),
                new MemoryWriteAddress(0x2800, 0x2bff, colorram_w, colorram),
                new MemoryWriteAddress(0x2c00, 0x2fff, megazone_colorram2_w, megazone_colorram2),
                new MemoryWriteAddress(0x3000, 0x33ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3800, 0x3fff, megazone_sharedram_w, megazone_sharedram),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x6000, input_port_0_r), /* IO Coin */
                new MemoryReadAddress(0x6001, 0x6001, input_port_1_r), /* P1 IO */
                new MemoryReadAddress(0x6002, 0x6002, input_port_2_r), /* P2 IO */
                new MemoryReadAddress(0x6003, 0x6003, input_port_3_r), /* DIP 1 */
                new MemoryReadAddress(0x8000, 0x8000, input_port_4_r), /* DIP 2 */
                new MemoryReadAddress(0x8001, 0x8001, megazone_dip3_r), /* DIP 3 - Not used */
                new MemoryReadAddress(0xe000, 0xe7ff, megazone_sharedram_r), /* Shared with $3800.3fff of main CPU */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x2000, megazone_i8039_irq_w), /* START line. Interrupts 8039 */
                new MemoryWriteAddress(0x4000, 0x4000, soundlatch_w), /* CODE  line. Command Interrupts 8039 */
                new MemoryWriteAddress(0xa000, 0xa000, MWA_RAM), /* INTMAIN - Interrupts main CPU (unused) */
                new MemoryWriteAddress(0xc000, 0xc000, MWA_RAM), /* INT (Actually is NMI) enable/disable (unused)*/
                new MemoryWriteAddress(0xc001, 0xc001, watchdog_reset_w),
                new MemoryWriteAddress(0xe000, 0xe7ff, megazone_sharedram_w), /* Shared with $3800.3fff of main CPU */
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x00, 0x02, AY8910_read_port_0_r),
                new IOReadPort(-1)
            };

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x02, 0x02, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress i8039_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress i8039_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort i8039_readport[]
            = {
                new IOReadPort(0x00, 0xff, soundlatch_r),
                new IOReadPort(0x111, 0x111, IORP_NOP),
                new IOReadPort(-1)
            };

    static IOWritePort i8039_writeport[]
            = {
                new IOWritePort(I8039_p1, I8039_p1, DAC_data_w),
                new IOWritePort(I8039_p2, I8039_p2, i8039_irqen_and_status_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_megazone = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x02, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x05, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0f, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x07, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0b, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x0a, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_BITX(0, 0x00, IPT_DIPSWITCH_SETTING | IPF_CHEAT, "Infinite", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "20k 70k");
            PORT_DIPSETTING(0x10, "20k 80k");
            PORT_DIPSETTING(0x08, "30k 90k");
            PORT_DIPSETTING(0x00, "30k 100k");

            PORT_DIPNAME(0x60, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");

            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel */
            new int[]{0x4000 * 8 + 4, 0x4000 * 8 + 0, 4, 0},
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8,},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16 * 16, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            14318000 / 8, /* 1.78975 MHz */
            new int[]{30},
            new ReadHandlerPtr[]{megazone_portA_r},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{megazone_portB_w}
    );

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{50}
    );

    static MachineDriver machine_driver_megazone = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2048000, /* 2 MHz */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* Z80 Clock is derived from the H1 signal */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_I8039 | CPU_AUDIO_CPU,
                        14318000 / 2 / 15, /* 1/2 14MHz crystal */
                        i8039_readmem, i8039_writemem, i8039_readport, i8039_writeport,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            15, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            36 * 8, 32 * 8, new rectangle(0 * 8, 36 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 16 * 16 + 16 * 16,
            megazone_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            megazone_vh_start,
            megazone_vh_stop,
            megazone_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_DAC,
                        dac_interface
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
    static RomLoadHandlerPtr rom_megazone = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);
            /* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("319i07.bin", 0x6000, 0x2000, 0x94b22ea8);
            ROM_LOAD("319i06.bin", 0x8000, 0x2000, 0x0468b619);
            ROM_LOAD("319i05.bin", 0xa000, 0x2000, 0xac59000c);
            ROM_LOAD("319i04.bin", 0xc000, 0x2000, 0x1e968603);
            ROM_LOAD("319i03.bin", 0xe000, 0x2000, 0x0888b803);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */
            ROM_LOAD("319e02.bin", 0x0000, 0x2000, 0xd5d45edb);

            ROM_REGION(0x1000, REGION_CPU3);
            /* 4k for the 8039 DAC CPU */
            ROM_LOAD("319e01.bin", 0x0000, 0x1000, 0xed5725a0);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("319e12.bin", 0x0000, 0x2000, 0xe0fb7835);
            ROM_LOAD("319e13.bin", 0x2000, 0x2000, 0x3d8f3743);

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("319e11.bin", 0x0000, 0x2000, 0xf36f19c5);
            ROM_LOAD("319e09.bin", 0x2000, 0x2000, 0x5eaa7f3e);
            ROM_LOAD("319e10.bin", 0x4000, 0x2000, 0x7bb1aeee);
            ROM_LOAD("319e08.bin", 0x6000, 0x2000, 0x6add71b1);

            ROM_REGION(0x0260, REGION_PROMS);
            ROM_LOAD("319b18.a16", 0x0000, 0x020, 0x23cb02af);/* palette */
            ROM_LOAD("319b16.c6", 0x0020, 0x100, 0x5748e933);/* sprite lookup table */
            ROM_LOAD("319b17.a11", 0x0120, 0x100, 0x1fbfce73);/* character lookup table */
            ROM_LOAD("319b14.e7", 0x0220, 0x020, 0x55044268);/* timing (not used) */
            ROM_LOAD("319b15.e8", 0x0240, 0x020, 0x31fd7ab9);/* timing (not used) */
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_megaznik = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);
            /* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("ic59_cpu.bin", 0x6000, 0x2000, 0xf41922a0);
            ROM_LOAD("ic58_cpu.bin", 0x8000, 0x2000, 0x7fd7277b);
            ROM_LOAD("ic57_cpu.bin", 0xa000, 0x2000, 0xa4b33b51);
            ROM_LOAD("ic56_cpu.bin", 0xc000, 0x2000, 0x2aabcfbf);
            ROM_LOAD("ic55_cpu.bin", 0xe000, 0x2000, 0xb33a3c37);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64k for the audio CPU */
            ROM_LOAD("319e02.bin", 0x0000, 0x2000, 0xd5d45edb);

            ROM_REGION(0x1000, REGION_CPU3);
            /* 4k for the 8039 DAC CPU */
            ROM_LOAD("319e01.bin", 0x0000, 0x1000, 0xed5725a0);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic40_vid.bin", 0x0000, 0x2000, 0x07b8b24b);
            ROM_LOAD("319e13.bin", 0x2000, 0x2000, 0x3d8f3743);

            ROM_REGION(0x08000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ic15_vid.bin", 0x0000, 0x2000, 0x965a7ff6);
            ROM_LOAD("319e09.bin", 0x2000, 0x2000, 0x5eaa7f3e);
            ROM_LOAD("319e10.bin", 0x4000, 0x2000, 0x7bb1aeee);
            ROM_LOAD("319e08.bin", 0x6000, 0x2000, 0x6add71b1);

            ROM_REGION(0x0260, REGION_PROMS);
            ROM_LOAD("319b18.a16", 0x0000, 0x020, 0x23cb02af);/* palette */
            ROM_LOAD("319b16.c6", 0x0020, 0x100, 0x5748e933);/* sprite lookup table */
            ROM_LOAD("319b17.a11", 0x0120, 0x100, 0x1fbfce73);/* character lookup table */
            ROM_LOAD("319b14.e7", 0x0220, 0x020, 0x55044268);/* timing (not used) */
            ROM_LOAD("319b15.e8", 0x0240, 0x020, 0x31fd7ab9);/* timing (not used) */
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_megazone = new InitDriverHandlerPtr() {
        public void handler() {
            konami1_decode();
        }
    };

    public static GameDriver driver_megazone = new GameDriver("1983", "megazone", "megazone.java", rom_megazone, null, machine_driver_megazone, input_ports_megazone, init_megazone, ROT90, "Konami", "Mega Zone");
    public static GameDriver driver_megaznik = new GameDriver("1983", "megaznik", "megazone.java", rom_megaznik, driver_megazone, machine_driver_megazone, input_ports_megazone, init_megazone, ROT90, "Konami / Interlogic + Kosuka", "Mega Zone (Kosuka)");
}
