
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.ccastles.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.common.libc.cstring.*;
public class ccastles {

    static UBytePtr nvram = new UBytePtr();
    static int[] nvram_size = new int[1];

    public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                osd_fwrite(file, nvram, nvram_size[0]);
            } else {
                if (file != null) {
                    osd_fread(file, nvram, nvram_size[0]);
                } else {
                    memset(nvram, 0, nvram_size[0]);
                }
            }
        }
    };

    public static WriteHandlerPtr ccastles_led_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //osd_led_w(offset,~data);
        }
    };

    public static WriteHandlerPtr ccastles_coin_counter_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* this is not working, haven't investigated why */
            coin_counter_w.handler(offset ^ 1, ~data);
        }
    };

    public static WriteHandlerPtr ccastles_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            if (data != 0) {
                cpu_setbank(1, new UBytePtr(RAM, 0x10000));
            } else {
                cpu_setbank(1, new UBytePtr(RAM, 0xa000));
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0002, 0x0002, ccastles_bitmode_r),
                new MemoryReadAddress(0x0000, 0x8fff, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x90ff, MRA_RAM),
                new MemoryReadAddress(0x9400, 0x9400, input_port_2_r), /* trackball y - player 1 */
                new MemoryReadAddress(0x9402, 0x9402, input_port_2_r), /* trackball y - player 2 */
                new MemoryReadAddress(0x9500, 0x9500, input_port_2_r), /* trackball y - player 1 mirror */
                new MemoryReadAddress(0x9401, 0x9401, input_port_3_r), /* trackball x - player 1 */
                new MemoryReadAddress(0x9403, 0x9403, input_port_3_r), /* trackball x - player 2 */
                new MemoryReadAddress(0x9501, 0x9501, input_port_3_r), /* trackball x - player 1 mirror */
                new MemoryReadAddress(0x9600, 0x9600, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x9800, 0x980f, pokey1_r), /* Random # generator on a Pokey */
                new MemoryReadAddress(0x9a00, 0x9a0f, pokey2_r), /* Random #, IN1 */
                new MemoryReadAddress(0xa000, 0xdfff, MRA_BANK1),
                new MemoryReadAddress(0xe000, 0xffff, MRA_ROM), /* ROMs/interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0001, MWA_RAM, ccastles_screen_addr),
                new MemoryWriteAddress(0x0002, 0x0002, ccastles_bitmode_w),
                new MemoryWriteAddress(0x0003, 0x0bff, MWA_RAM),
                new MemoryWriteAddress(0x0c00, 0x7fff, MWA_RAM, videoram),
                new MemoryWriteAddress(0x8000, 0x8dff, MWA_RAM),
                new MemoryWriteAddress(0x8e00, 0x8eff, MWA_RAM, spriteram_2, spriteram_size),
                new MemoryWriteAddress(0x8f00, 0x8fff, MWA_RAM, spriteram),
                new MemoryWriteAddress(0x9000, 0x90ff, MWA_RAM, nvram, nvram_size),
                new MemoryWriteAddress(0x9800, 0x980f, pokey1_w),
                new MemoryWriteAddress(0x9a00, 0x9a0f, pokey2_w),
                new MemoryWriteAddress(0x9c80, 0x9c80, MWA_RAM, ccastles_scrollx),
                new MemoryWriteAddress(0x9d00, 0x9d00, MWA_RAM, ccastles_scrolly),
                new MemoryWriteAddress(0x9d80, 0x9d80, MWA_NOP),
                new MemoryWriteAddress(0x9e00, 0x9e00, watchdog_reset_w),
                new MemoryWriteAddress(0x9e80, 0x9e81, ccastles_led_w),
                new MemoryWriteAddress(0x9e85, 0x9e86, ccastles_coin_counter_w),
                new MemoryWriteAddress(0x9e87, 0x9e87, ccastles_bankswitch_w),
                new MemoryWriteAddress(0x9f00, 0x9f01, MWA_RAM, ccastles_screen_inc_enable),
                new MemoryWriteAddress(0x9f02, 0x9f03, MWA_RAM, ccastles_screen_inc),
                new MemoryWriteAddress(0x9f04, 0x9f04, ccastles_flipscreen_w),
                new MemoryWriteAddress(0x9f05, 0x9f06, MWA_RAM),
                new MemoryWriteAddress(0x9f07, 0x9f07, MWA_RAM, ccastles_sprite_bank),
                new MemoryWriteAddress(0x9f80, 0x9fbf, ccastles_paletteram_w),
                new MemoryWriteAddress(0xa000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_ccastles = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_TILT);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_VBLANK);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1);			/* 1p Jump, non-cocktail start1 */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);/* 2p Jump, non-cocktail start2 */

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x07, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);			/* cocktail only */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);			/* cocktail only */

            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_BIT(0xc0, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_ANALOG(0xff, 0x7f, IPT_TRACKBALL_Y | IPF_REVERSE, 10, 30, 0, 0);

            PORT_START(); 	/* IN3 */

            PORT_ANALOG(0xff, 0x7f, IPT_TRACKBALL_X, 10, 30, 0, 0);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout ccastles_spritelayout = new GfxLayout(
            8, 16, /* 8*16 sprites */
            256, /* 256 sprites */
            4, /* 4 bits per pixel (the most significant bit is always 0) */
            new int[]{0x2000 * 8 + 0, 0x2000 * 8 + 4, 0, 4}, /* the three bitplanes are separated */
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x0000, ccastles_spritelayout, 0, 1),
                new GfxDecodeInfo(-1) /* end of array */};

    static POKEYinterface pokey_interface = new POKEYinterface(
            2, /* 2 chips */
            1250000, /* 1.25 MHz??? */
            new int[]{50, 50},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            /* The allpot handler */
            new ReadHandlerPtr[]{null, input_port_1_r}
    );

    static MachineDriver machine_driver_ccastles = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 Mhz */
                        readmem, writemem, null, null,
                        interrupt, 4
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            256, 232, new rectangle(0, 255, 0, 231),
            gfxdecodeinfo,
            32, 32,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            ccastles_vh_start,
            ccastles_vh_stop,
            ccastles_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_POKEY,
                        pokey_interface
                )
            },
            nvram_handler
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_ccastles = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ccastles.303", 0x0a000, 0x2000, 0x10e39fce);
            ROM_LOAD("ccastles.304", 0x0c000, 0x2000, 0x74510f72);
            ROM_LOAD("ccastles.305", 0x0e000, 0x2000, 0x9418cf8a);
            ROM_LOAD("ccastles.102", 0x10000, 0x2000, 0xf6ccfbd4);/* Bank switched ROMs */

            ROM_LOAD("ccastles.101", 0x12000, 0x2000, 0xe2e17236);/* containing level data. */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ccastles.107", 0x0000, 0x2000, 0x39960b7d);
            ROM_LOAD("ccastles.106", 0x2000, 0x2000, 0x9d1d89fc);
            ROM_END();
        }
    };

    static RomLoadPtr rom_ccastle2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x14000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("ccastles.203", 0x0a000, 0x2000, 0x348a96f0);
            ROM_LOAD("ccastles.204", 0x0c000, 0x2000, 0xd48d8c1f);
            ROM_LOAD("ccastles.205", 0x0e000, 0x2000, 0x0e4883cc);
            ROM_LOAD("ccastles.102", 0x10000, 0x2000, 0xf6ccfbd4);/* Bank switched ROMs */

            ROM_LOAD("ccastles.101", 0x12000, 0x2000, 0xe2e17236);/* containing level data. */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ccastles.107", 0x0000, 0x2000, 0x39960b7d);
            ROM_LOAD("ccastles.106", 0x2000, 0x2000, 0x9d1d89fc);
            ROM_END();
        }
    };

    public static GameDriver driver_ccastles = new GameDriver("1983", "ccastles", "ccastles.java", rom_ccastles, null, machine_driver_ccastles, input_ports_ccastles, null, ROT0, "Atari", "Crystal Castles (set 1)");
    public static GameDriver driver_ccastle2 = new GameDriver("1983", "ccastle2", "ccastles.java", rom_ccastle2, driver_ccastles, machine_driver_ccastles, input_ports_ccastles, null, ROT0, "Atari", "Crystal Castles (set 2)");
}
