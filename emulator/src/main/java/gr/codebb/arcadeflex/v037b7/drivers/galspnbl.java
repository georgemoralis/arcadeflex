/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_OKIM6295;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM3812;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.cpu.z80.z80H.Z80_NMI_INT;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import gr.codebb.arcadeflex.v037b7.sound._3812intfH.YM3812interface;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.OKIM6295_data_0_w;
import static gr.codebb.arcadeflex.v037b7.sound.okim6295.OKIM6295_status_0_r;
import gr.codebb.arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.galspnbl.*;

public class galspnbl {

    public static WriteHandlerPtr soundcommand_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 0x00ff0000) == 0) {
                soundlatch_w.handler(offset, data & 0xff);
                cpu_cause_interrupt(1, Z80_NMI_INT);
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x3fffff, MRA_ROM),
                new MemoryReadAddress(0x700000, 0x703fff, MRA_BANK1), /* galspnbl */
                new MemoryReadAddress(0x708000, 0x70ffff, MRA_BANK2), /* galspnbl */
                new MemoryReadAddress(0x800000, 0x803fff, MRA_BANK3), /* hotpinbl */
                new MemoryReadAddress(0x808000, 0x80ffff, MRA_BANK4), /* hotpinbl */
                new MemoryReadAddress(0x880000, 0x880fff, MRA_BANK5),
                new MemoryReadAddress(0x900000, 0x900fff, MRA_BANK6),
                new MemoryReadAddress(0x904000, 0x904fff, MRA_BANK7),
                new MemoryReadAddress(0x980000, 0x9bffff, galspnbl_bgvideoram_r),
                new MemoryReadAddress(0xa80000, 0xa80001, input_port_0_r),
                new MemoryReadAddress(0xa80010, 0xa80011, input_port_1_r),
                new MemoryReadAddress(0xa80020, 0xa80021, input_port_2_r),
                new MemoryReadAddress(0xa80030, 0xa80031, input_port_3_r),
                new MemoryReadAddress(0xa80040, 0xa80041, input_port_4_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x3fffff, MWA_ROM),
                new MemoryWriteAddress(0x700000, 0x703fff, MWA_BANK1), /* galspnbl work RAM */
                new MemoryWriteAddress(0x708000, 0x70ffff, MWA_BANK2), /* galspnbl work RAM, bitmaps are decompressed here */
                new MemoryWriteAddress(0x800000, 0x803fff, MWA_BANK3), /* hotpinbl work RAM */
                new MemoryWriteAddress(0x808000, 0x80ffff, MWA_BANK4), /* hotpinbl work RAM, bitmaps are decompressed here */
                new MemoryWriteAddress(0x880000, 0x880fff, MWA_BANK5, spriteram, spriteram_size),
                new MemoryWriteAddress(0x8ff400, 0x8fffff, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x900000, 0x900fff, MWA_BANK6, colorram),
                new MemoryWriteAddress(0x901000, 0x903fff, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x904000, 0x904fff, MWA_BANK7, videoram),
                new MemoryWriteAddress(0x905000, 0x907fff, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x980000, 0x9bffff, galspnbl_bgvideoram_w, galspnbl_bgvideoram),
                new MemoryWriteAddress(0xa00000, 0xa00fff, MWA_NOP), /* more palette ? */
                new MemoryWriteAddress(0xa01000, 0xa017ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram),
                new MemoryWriteAddress(0xa01800, 0xa027ff, MWA_NOP), /* more palette ? */
                new MemoryWriteAddress(0xa80010, 0xa80011, soundcommand_w),
                new MemoryWriteAddress(0xa80020, 0xa80021, MWA_NOP), /* could be watchdog, but causes resets when picture is shown */
                new MemoryWriteAddress(0xa80030, 0xa80031, MWA_NOP), /* irq ack? */
                new MemoryWriteAddress(0xa80050, 0xa80051, galspnbl_scroll_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xefff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf800, 0xf800, OKIM6295_status_0_r),
                new MemoryReadAddress(0xfc00, 0xfc00, MRA_NOP), /* irq ack ?? */
                new MemoryReadAddress(0xfc20, 0xfc20, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xefff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, OKIM6295_data_0_w),
                new MemoryWriteAddress(0xf810, 0xf810, YM3812_control_port_0_w),
                new MemoryWriteAddress(0xf811, 0xf811, YM3812_write_port_0_w),
                new MemoryWriteAddress(0xfc00, 0xfc00, MWA_NOP), /* irq ack ?? */
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_hotpinbl = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "Slide Show");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_galspnbl = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "Slide Show");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout tilelayout = new GfxLayout(
            16, 8,
            RGN_FRAC(1, 2),
            4,
            new int[]{0, 1, 2, 3},
            new int[]{0 * 4, 1 * 4, RGN_FRAC(1, 2) + 0 * 4, RGN_FRAC(1, 2) + 1 * 4, 2 * 4, 3 * 4, RGN_FRAC(1, 2) + 2 * 4, RGN_FRAC(1, 2) + 3 * 4,
                16 * 8 + 0 * 4, 16 * 8 + 1 * 4, 16 * 8 + RGN_FRAC(1, 2) + 0 * 4, 16 * 8 + RGN_FRAC(1, 2) + 1 * 4, 16 * 8 + 2 * 4, 16 * 8 + 3 * 4, 16 * 8 + RGN_FRAC(1, 2) + 2 * 4, 16 * 8 + RGN_FRAC(1, 2) + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            32 * 8
    );

    static GfxLayout spritelayout = new GfxLayout(
            8, 8,
            RGN_FRAC(1, 2),
            4,
            new int[]{0, 1, 2, 3},
            new int[]{0, 4, RGN_FRAC(1, 2) + 0, RGN_FRAC(1, 2) + 4, 8 + 0, 8 + 4, 8 + RGN_FRAC(1, 2) + 0, 8 + RGN_FRAC(1, 2) + 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tilelayout, 512, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            cpu_set_irq_line(1, 0, linestate);
        }
    };

    static YM3812interface ym3812_interface = new YM3812interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz ? */
            new int[]{100}, /* volume */
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static OKIM6295interface okim6295_interface = new OKIM6295interface(
            1, /* 1 chip */
            new int[]{8000}, /* 8000Hz frequency? */
            new int[]{REGION_SOUND1}, /* memory region */
            new int[]{100}
    );

    static MachineDriver machine_driver_hotpinbl = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* 10 MHz ??? */
                        readmem, writemem, null, null,
                        m68_level3_irq, 1 /* also has vector for 6, but it does nothing */
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        4000000, /* 4 MHz ??? */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 1 /* IRQ is caused by the YM3812 */
                /* NMI is caused by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            512, 256, new rectangle(0, 512 - 1, 16, 240 - 1),
            gfxdecodeinfo,
            1024 + 32768, 1024,
            galspnbl_init_palette,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_PIXEL_ASPECT_RATIO_1_2,
            null,
            generic_bitmapped_vh_start,
            generic_bitmapped_vh_stop,
            galspnbl_vh_screenrefresh,
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
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_galspnbl = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x400000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("7.rom", 0x000000, 0x80000, 0xce0189bf);
            ROM_LOAD_ODD("3.rom", 0x000000, 0x80000, 0x9b0a8744);
            ROM_LOAD_EVEN("8.rom", 0x100000, 0x80000, 0xeee2f087);
            ROM_LOAD_ODD("4.rom", 0x100000, 0x80000, 0x56298489);
            ROM_LOAD_EVEN("9.rom", 0x200000, 0x80000, 0xd9e4964c);
            ROM_LOAD_ODD("5.rom", 0x200000, 0x80000, 0xa5e71ee4);
            ROM_LOAD_EVEN("10.rom", 0x300000, 0x80000, 0x3a20e1e5);
            ROM_LOAD_ODD("6.rom", 0x300000, 0x80000, 0x94927d20);

            ROM_REGION(0x10000, REGION_CPU2);/* Z80 code */
            ROM_LOAD("2.rom", 0x0000, 0x10000, 0xfae688a7);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("17.rom", 0x00000, 0x40000, 0x7d435701);
            ROM_LOAD("18.rom", 0x40000, 0x40000, 0x136adaac);

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("15.rom", 0x00000, 0x20000, 0x4beb840d);
            ROM_LOAD("16.rom", 0x20000, 0x20000, 0x93d3c610);

            ROM_REGION(0x40000, REGION_SOUND1);/* OKIM6295 samples */
            ROM_LOAD("1.rom", 0x00000, 0x40000, 0x93c06d3d);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_hotpinbl = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x400000, REGION_CPU1);/* 68000 code */
            ROM_LOAD_EVEN("hp_07.bin", 0x000000, 0x80000, 0x978cc13e);
            ROM_LOAD_ODD("hp_03.bin", 0x000000, 0x80000, 0x68388726);
            ROM_LOAD_EVEN("hp_08.bin", 0x100000, 0x80000, 0xbd16be12);
            ROM_LOAD_ODD("hp_04.bin", 0x100000, 0x80000, 0x655b0cf0);
            ROM_LOAD_EVEN("hp_09.bin", 0x200000, 0x80000, 0xa6368624);
            ROM_LOAD_ODD("hp_05.bin", 0x200000, 0x80000, 0x48efd028);
            ROM_LOAD_EVEN("hp_10.bin", 0x300000, 0x80000, 0xa5c63e34);
            ROM_LOAD_ODD("hp_06.bin", 0x300000, 0x80000, 0x513eda91);

            ROM_REGION(0x10000, REGION_CPU2);/* Z80 code */
            ROM_LOAD("hp_02.bin", 0x0000, 0x10000, 0x82698269);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hp_13.bin", 0x00000, 0x40000, 0xd53b64b9);
            ROM_LOAD("hp_14.bin", 0x40000, 0x40000, 0x2fe3fcee);

            ROM_REGION(0x40000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("hp_11.bin", 0x00000, 0x20000, 0xdeecd7f1);
            ROM_LOAD("hp_12.bin", 0x20000, 0x20000, 0x5fd603c2);

            ROM_REGION(0x40000, REGION_SOUND1);/* OKIM6295 samples */
            ROM_LOAD("hp_01.bin", 0x00000, 0x40000, 0x93c06d3d);
            ROM_END();
        }
    };

    public static GameDriver driver_hotpinbl = new GameDriver("1995", "hotpinbl", "galspnbl.java", rom_hotpinbl, null, machine_driver_hotpinbl, input_ports_hotpinbl, null, ROT90_16BIT, "Comad & New Japan System", "Hot Pinball", GAME_NO_COCKTAIL);
    public static GameDriver driver_galspnbl = new GameDriver("1996", "galspnbl", "galspnbl.java", rom_galspnbl, null, machine_driver_hotpinbl, input_ports_galspnbl, null, ROT90_16BIT, "Comad", "Gals Pinball", GAME_NO_COCKTAIL);
}
