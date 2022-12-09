/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
import static arcadeflex.v036.vidhrdw.copsnrob.*;
import static gr.codebb.arcadeflex.v036.machine.copsnrob.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class copsnrob {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x01ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x08ff, MRA_RAM),
                new MemoryReadAddress(0x0b00, 0x0bff, MRA_RAM),
                new MemoryReadAddress(0x0c00, 0x0fff, MRA_RAM),
                new MemoryReadAddress(0x1000, 0x1000, input_port_0_r),
                new MemoryReadAddress(0x1002, 0x100e, copsnrob_gun_position_r),
                new MemoryReadAddress(0x1012, 0x1012, input_port_3_r),
                new MemoryReadAddress(0x1016, 0x1016, input_port_1_r),
                new MemoryReadAddress(0x101a, 0x101a, input_port_2_r),
                new MemoryReadAddress(0x1200, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0xfff8, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x01ff, MWA_RAM),
                new MemoryWriteAddress(0x0500, 0x0503, MWA_RAM),
                new MemoryWriteAddress(0x0504, 0x0507, MWA_NOP), // ???
                new MemoryWriteAddress(0x0600, 0x0600, MWA_RAM, copsnrob_trucky),
                new MemoryWriteAddress(0x0700, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x08ff, MWA_RAM, copsnrob_bulletsram),
                new MemoryWriteAddress(0x0900, 0x0903, MWA_RAM, copsnrob_carimage),
                new MemoryWriteAddress(0x0a00, 0x0a03, MWA_RAM, copsnrob_cary),
                new MemoryWriteAddress(0x0b00, 0x0bff, MWA_RAM),
                new MemoryWriteAddress(0x0c00, 0x0fff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1000, 0x1003, MWA_NOP),
                new MemoryWriteAddress(0x1200, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0xfff8, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_copsnrob = new InputPortPtr() {
        public void handler() {
            PORT_START();       /* IN0 */

            PORT_BIT(0xFF, IP_ACTIVE_LOW, IPT_VBLANK);

            PORT_START();       /* IN1 */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();       /* IN2 */

            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();       /* DIP1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, "1 Coin/1 Player");
            PORT_DIPSETTING(0x02, "1 Coin/2 Players");
            PORT_DIPSETTING(0x01, "1 Coin/Game");
            PORT_DIPSETTING(0x00, "2 Coins/1 Player");
            PORT_DIPNAME(0x0C, 0x00, "Time Limit");
            PORT_DIPSETTING(0x0C, "1min");
            PORT_DIPSETTING(0x08, "1min 45sec");
            PORT_DIPSETTING(0x04, "2min 20sec");
            PORT_DIPSETTING(0x00, "3min");
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);

            /* These input ports are fake */
            PORT_START();       /* IN3 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER1 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER1 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);

            PORT_START();       /* IN4 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);

            PORT_START();       /* IN5 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER3 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER3 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);

            PORT_START();       /* IN6 */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_PLAYER4 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_PLAYER4 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            64, /* 64 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout carlayout = new GfxLayout(
            32, 32, /* 32*32 sprites */
            16, /* 16 sprites */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{7, 6, 5, 4, 3, 2, 1, 0,
                15, 14, 13, 12, 11, 10, 9, 8,
                23, 22, 21, 20, 19, 18, 17, 16,
                31, 30, 29, 28, 27, 26, 25, 24},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                8 * 32, 9 * 32, 10 * 32, 11 * 32, 12 * 32, 13 * 32, 14 * 32, 15 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32,
                24 * 32, 25 * 32, 26 * 32, 27 * 32, 28 * 32, 29 * 32, 30 * 32, 31 * 32},
            32 * 32 /* every sprite takes 128 consecutive bytes */
    );

    static GfxLayout trucklayout = new GfxLayout(
            16, 32, /* 16*32 sprites */
            2, /* 2 sprites */
            1, /* 1 bit per pixel */
            new int[]{0}, /* no separation in 1 bpp */
            new int[]{3 * 256 + 4, 3 * 256 + 5, 3 * 256 + 6, 3 * 256 + 7, 2 * 256 + 4, 2 * 256 + 5, 2 * 256 + 6, 2 * 256 + 7,
                1 * 256 + 4, 1 * 256 + 5, 1 * 256 + 6, 1 * 256 + 7, 0 * 256 + 4, 0 * 256 + 5, 0 * 256 + 6, 0 * 256 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8,
                16 * 8, 17 * 8, 18 * 8, 19 * 8, 20 * 8, 21 * 8, 22 * 8, 23 * 8,
                24 * 8, 25 * 8, 26 * 8, 27 * 8, 28 * 8, 29 * 8, 30 * 8, 31 * 8},
            32 * 32 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 3),
                new GfxDecodeInfo(REGION_GFX2, 0, carlayout, 0, 3),
                new GfxDecodeInfo(REGION_GFX3, 0, trucklayout, 0, 3),
                new GfxDecodeInfo(-1) /* end of array */};

    static char palette[]
            = {
                0x00, 0x00, 0x00, /* Black */
                0x40, 0x40, 0xc0, /* Blue */
                0xf0, 0xf0, 0x30, /* Yellow */
                0xbd, 0x9b, 0x13, /* Amber */};
    static short colortable[]
            = {
                0x00, 0x01,
                0x00, 0x02,
                0x00, 0x03
            };
    public static VhConvertColorPromPtr init_palette = new VhConvertColorPromPtr() {
        public void handler(char[] game_palette, char[] game_colortable, UBytePtr color_prom) {
            //memcpy(game_palette,palette,sizeof(palette));
            for (int i = 0; i < palette.length; i++) {
                game_palette[i]=palette[i];
            }
            //memcpy(game_colortable,colortable,sizeof(colortable));
            for (int i = 0; i < colortable.length; i++) {
                game_colortable[i] = (char) colortable[i];
            }
        }
    };

    static MachineDriver machine_driver_copsnrob = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        14318180 / 16, /* 894886.25 kHz */
                        readmem, writemem, null, null,
                        ignore_interrupt, 1
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            palette.length / 3, colortable.length,
            init_palette,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            copsnrob_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0, null
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_copsnrob = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);    /* 64k for code */

            ROM_LOAD("5777.l7", 0x1200, 0x0200, 0x2b62d627);
            ROM_LOAD("5776.k7", 0x1400, 0x0200, 0x7fb12a49);
            ROM_LOAD("5775.j7", 0x1600, 0x0200, 0x627dee63);
            ROM_LOAD("5774.h7", 0x1800, 0x0200, 0xdfbcb7f2);
            ROM_LOAD("5773.e7", 0x1a00, 0x0200, 0xff7c95f4);
            ROM_LOAD("5772.d7", 0x1c00, 0x0200, 0x8d26afdc);
            ROM_LOAD("5771.b7", 0x1e00, 0x0200, 0xd61758d6);
            ROM_RELOAD(0xfe00, 0x0200);// For 6502 vectors

            ROM_REGION(0x0200, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5782.m3", 0x0000, 0x0200, 0x82b86852);

            ROM_REGION(0x0800, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5778.p1", 0x0000, 0x0200, 0x78bff86a);
            ROM_LOAD("5779.m1", 0x0200, 0x0200, 0x8b1d0d83);
            ROM_LOAD("5780.l1", 0x0400, 0x0200, 0x6f4c6bab);
            ROM_LOAD("5781.j1", 0x0600, 0x0200, 0xc87f2f13);

            ROM_REGION(0x0100, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("5770.m2", 0x0000, 0x0100, 0xb00bbe77);

            ROM_REGION(0x0060, REGION_PROMS); /* misc. PROMs (timing?) */

            ROM_LOAD("5765.h8", 0x0000, 0x0020, 0x6cd58931);
            ROM_LOAD("5766.k8", 0x0020, 0x0020, 0xe63edf4f);
            ROM_LOAD("5767.j8", 0x0040, 0x0020, 0x381b5ae4);
            ROM_END();
        }
    };

    public static GameDriver driver_copsnrob = new GameDriver("1976", "copsnrob", "copsnrob.java", rom_copsnrob, null, machine_driver_copsnrob, input_ports_copsnrob, null, ROT0, "Atari", "Cops'n Robbers", GAME_NO_SOUND);
}
