/**
 * ported to 0.36
 */
/**
 * Changelog
 * =========
 * 24/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame improts
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.memoryH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.speedbal.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class speedbal {

    public static UBytePtr speedbal_foreground_videoram = new UBytePtr();
    public static UBytePtr speedbal_background_videoram = new UBytePtr();
    public static UBytePtr speedbal_sprites_dataram = new UBytePtr();

    public static int[] speedbal_foreground_videoram_size = new int[1];
    public static int[] speedbal_background_videoram_size = new int[1];
    public static int[] speedbal_sprites_dataram_size = new int[1];

    public static UBytePtr speedbal_sharedram = new UBytePtr();

    public static ReadHandlerPtr speedbal_sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            //  if (offset==0x0) speedbal_sharedram[offset]+=1;
            return speedbal_sharedram.read(offset);
        }
    };

    public static WriteHandlerPtr speedbal_sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            speedbal_sharedram.write(offset, data);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xdbff, MRA_ROM),
                new MemoryReadAddress(0xdc00, 0xdfff, speedbal_sharedram_r), // shared with SOUND
                new MemoryReadAddress(0xe000, 0xe1ff, speedbal_background_videoram_r),
                new MemoryReadAddress(0xe800, 0xefff, speedbal_foreground_videoram_r),
                new MemoryReadAddress(0xf000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xdbff, MWA_ROM),
                new MemoryWriteAddress(0xdc00, 0xdfff, speedbal_sharedram_w, speedbal_sharedram), // shared with SOUND
                new MemoryWriteAddress(0xe000, 0xe1ff, speedbal_background_videoram_w, speedbal_background_videoram, speedbal_background_videoram_size),
                new MemoryWriteAddress(0xe800, 0xefff, speedbal_foreground_videoram_w, speedbal_foreground_videoram, speedbal_foreground_videoram_size),
                new MemoryWriteAddress(0xf000, 0xf5ff, paletteram_RRRRGGGGBBBBxxxx_swap_w, paletteram),
                new MemoryWriteAddress(0xf600, 0xfeff, MWA_RAM),
                new MemoryWriteAddress(0xff00, 0xffff, MWA_RAM, speedbal_sprites_dataram, speedbal_sprites_dataram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0xdc00, 0xdfff, speedbal_sharedram_r), // shared with MAIN CPU
                new MemoryReadAddress(0xf000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0xdc00, 0xdfff, speedbal_sharedram_w), // shared with MAIN CPU
                new MemoryWriteAddress(0xf000, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r),
                new IOReadPort(0x10, 0x10, input_port_1_r),
                new IOReadPort(0x20, 0x20, input_port_2_r),
                new IOReadPort(0x30, 0x30, input_port_3_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, YM3812_status_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM3812_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM3812_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_speedbal = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW2 */

            PORT_DIPNAME(0x07, 0x07, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x06, "70000 200000 1M");
            PORT_DIPSETTING(0x07, "70000 200000");
            PORT_DIPSETTING(0x03, "100000 300000 1M");
            PORT_DIPSETTING(0x04, "100000 300000");
            PORT_DIPSETTING(0x01, "200000 1M");
            PORT_DIPSETTING(0x05, "200000");
            /*	PORT_DIPSETTING(    0x02, "200000" );*/
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x30, "Difficulty 1");
            PORT_DIPSETTING(0x30, "Very Easy");
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x10, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0xc0, 0xc0, "Difficulty 2");
            PORT_DIPSETTING(0xc0, "Very Easy");
            PORT_DIPSETTING(0x80, "Easy");
            PORT_DIPSETTING(0x40, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x20, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON4);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* actually 2 bits per pixel - two of the planes are empty */
            new int[]{1024 * 16 * 8 + 4, 1024 * 16 * 8 + 0, 4, 0},
            new int[]{8 + 3, 8 + 2, 8 + 1, 8 + 0, 3, 2, 1, 0},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16}, /* characters are rotated 90 degrees */
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 tiles */
            1024, /* 1024 tiles */
            4, /* 4 bits per pixel */
            new int[]{0, 2, 4, 6}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 8 + 0, 0 * 8 + 1, 7 * 8 + 0, 7 * 8 + 1, 6 * 8 + 0, 6 * 8 + 1, 5 * 8 + 0, 5 * 8 + 1,
                4 * 8 + 0, 4 * 8 + 1, 3 * 8 + 0, 3 * 8 + 1, 2 * 8 + 0, 2 * 8 + 1, 1 * 8 + 0, 1 * 8 + 1},
            new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 2, 4, 6}, /* the bitplanes are packed in one nibble */
            new int[]{7 * 8 + 1, 7 * 8 + 0, 6 * 8 + 1, 6 * 8 + 0, 5 * 8 + 1, 5 * 8 + 0, 4 * 8 + 1, 4 * 8 + 0,
                3 * 8 + 1, 3 * 8 + 0, 2 * 8 + 1, 2 * 8 + 0, 1 * 8 + 1, 1 * 8 + 0, 0 * 8 + 1, 0 * 8 + 0},
            new int[]{0 * 64, 1 * 64, 2 * 64, 3 * 64, 4 * 64, 5 * 64, 6 * 64, 7 * 64,
                8 * 64, 9 * 64, 10 * 64, 11 * 64, 12 * 64, 13 * 64, 14 * 64, 15 * 64},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 256, 16),
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 512, 16),
                new GfxDecodeInfo(REGION_GFX3, 0, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static YM3812interface ym3812_interface = new YM3812interface(
            1, /* 1 chip (no more supported) */
            3600000, /* 3.600000 MHz ? (partially supported) */
            new int[]{255} /* (not supported) */
    );

    static MachineDriver machine_driver_speedbal = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        4000000, /* 4 MHz ??? */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        2660000, /* 2.66 MHz ???  Maybe yes */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        interrupt, 8
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slices per frame */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            768, 768,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            speedbal_vh_start,
            speedbal_vh_stop,
            speedbal_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
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
    static RomLoadHandlerPtr rom_speedbal = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);
            /* 64K for code: main */

            ROM_LOAD("sb1.bin", 0x0000, 0x8000, 0x1c242e34);
            ROM_LOAD("sb3.bin", 0x8000, 0x8000, 0x7682326a);

            ROM_REGION(0x10000, REGION_CPU2);
            /* 64K for second CPU: sound */

            ROM_LOAD("sb2.bin", 0x0000, 0x8000, 0xe6a6d9b7);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sb10.bin", 0x00000, 0x08000, 0x36dea4bf);
            /* chars */

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sb9.bin", 0x00000, 0x08000, 0xb567e85e);
            /* bg tiles */

            ROM_LOAD("sb5.bin", 0x08000, 0x08000, 0xb0eae4ba);
            ROM_LOAD("sb8.bin", 0x10000, 0x08000, 0xd2bfbdb6);
            ROM_LOAD("sb4.bin", 0x18000, 0x08000, 0x1d23a130);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sb6.bin", 0x00000, 0x08000, 0x0e2506eb);
            /* sprites */

            ROM_LOAD("sb7.bin", 0x08000, 0x08000, 0x9f1b33d1);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_speedbal = new InitDriverHandlerPtr() {
        public void handler() {
            int i;

            /* invert the graphics bits on the sprites */
            for (i = 0; i < memory_region_length(REGION_GFX3); i++) {
                memory_region(REGION_GFX3).write(i, memory_region(REGION_GFX3).read(i) ^ 0xff);
            }
        }
    };

    public static GameDriver driver_speedbal = new GameDriver("1987", "speedbal", "speedbal.java", rom_speedbal, null, machine_driver_speedbal, input_ports_speedbal, init_speedbal, ROT270, "Tecfri", "Speed Ball");
}
