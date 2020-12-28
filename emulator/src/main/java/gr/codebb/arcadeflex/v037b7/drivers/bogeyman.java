/**
 * ported to v0.37b7
 * ported to v0.36
 *
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.bogeyman.*;

public class bogeyman {

    public static ReadHandlerPtr bogeyman_videoram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return bogeyman_videoram.read(offset);
        }
    };

    /**
     * ***************************************************************************
     */
    /* Sound section is copied from Mysterious Stones driver by Nicola, Mike, Brad */
    static int psg_latch;

    public static WriteHandlerPtr bogeyman_8910_latch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            psg_latch = data;
        }
    };
    static int lastbog;
    public static WriteHandlerPtr bogeyman_8910_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 5 goes to 8910 #0 BDIR pin  */
            if ((lastbog & 0x20) == 0x20 && (data & 0x20) == 0x00) {
                /* bit 4 goes to the 8910 #0 BC1 pin */
                if ((lastbog & 0x10) != 0) {
                    AY8910_control_port_0_w.handler(0, psg_latch);
                } else {
                    AY8910_write_port_0_w.handler(0, psg_latch);
                }
            }
            /* bit 7 goes to 8910 #1 BDIR pin  */
            if ((lastbog & 0x80) == 0x80 && (data & 0x80) == 0x00) {
                /* bit 6 goes to the 8910 #1 BC1 pin */
                if ((lastbog & 0x40) != 0) {
                    AY8910_control_port_1_w.handler(0, psg_latch);
                } else {
                    AY8910_write_port_1_w.handler(0, psg_latch);
                }
            }

            lastbog = data;
        }
    };

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress bogeyman_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x17ff, MRA_RAM),
                new MemoryReadAddress(0x1800, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x21ff, bogeyman_videoram_r),
                new MemoryReadAddress(0x2800, 0x2bff, MRA_RAM),
                new MemoryReadAddress(0x3800, 0x3800, input_port_0_r), /* Player 1 */
                new MemoryReadAddress(0x3801, 0x3801, input_port_1_r), /* Player 2 + VBL */
                new MemoryReadAddress(0x3802, 0x3802, input_port_2_r), /* Dip 1 */
                new MemoryReadAddress(0x3803, 0x3803, input_port_3_r), /* Dip 2 + Coins */
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress bogeyman_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x17ff, MWA_RAM),
                new MemoryWriteAddress(0x1800, 0x1fff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2000, 0x21ff, bogeyman_videoram_w, bogeyman_videoram),
                new MemoryWriteAddress(0x2800, 0x2bff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x300f, bogeyman_paletteram_w, paletteram),
                new MemoryWriteAddress(0x3800, 0x3800, bogeyman_8910_control_w),
                new MemoryWriteAddress(0x3801, 0x3801, bogeyman_8910_latch_w),
                new MemoryWriteAddress(0x3803, 0x3803, MWA_NOP), /* ?? */
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    static InputPortPtr input_ports_bogeyman = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x20, DEF_STR("Cocktail"));
            PORT_SERVICE(0x40, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x80, 0x80, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));

            PORT_START();
            /* DSW2 */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x02, "50000");
            PORT_DIPSETTING(0x00, "none");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x0c, "Easy");		// Normal
            PORT_DIPSETTING(0x08, "Medium");		//   |
            PORT_DIPSETTING(0x04, "Hard");		//   |
            PORT_DIPSETTING(0x00, "Hardest");		//  HARD
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN3);
            INPUT_PORTS_END();
        }
    };

    /**
     * ***************************************************************************
     */
    static GfxLayout charlayout1 = new GfxLayout(
            8, 8, /* 8*8 chars */
            512,
            3,
            new int[]{0x8000 * 8 + 4, 0, 4},
            new int[]{0x2000 * 8 + 3, 0x2000 * 8 + 2, 0x2000 * 8 + 1, 0x2000 * 8 + 0, 3, 2, 1, 0,},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout charlayout2 = new GfxLayout(
            8, 8, /* 8*8 chars */
            512,
            3,
            new int[]{0x8000 * 8, 0 + 0x1000 * 8, 4 + 0x1000 * 8, 0},
            new int[]{0x2000 * 8 + 3, 0x2000 * 8 + 2, 0x2000 * 8 + 1, 0x2000 * 8 + 0, 3, 2, 1, 0,},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout tiles1a = new GfxLayout(
            16, 16,
            0x80,
            3,
            new int[]{0x8000 * 8 + 4, 0, 4},
            new int[]{1024 * 8 * 8 + 3, 1024 * 8 * 8 + 2, 1024 * 8 * 8 + 1, 1024 * 8 * 8 + 0, 3, 2, 1, 0,
                1024 * 8 * 8 + 3 + 64, 1024 * 8 * 8 + 2 + 64, 1024 * 8 * 8 + 1 + 64, 1024 * 8 * 8 + 0 + 64, 3 + 64, 2 + 64, 1 + 64, 0 + 64},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                0 * 8 + 16 * 8, 1 * 8 + 16 * 8, 2 * 8 + 16 * 8, 3 * 8 + 16 * 8, 4 * 8 + 16 * 8, 5 * 8 + 16 * 8, 6 * 8 + 16 * 8, 7 * 8 + 16 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout tiles1b = new GfxLayout(
            16, 16,
            0x80,
            3,
            new int[]{0x8000 * 8 + 0, 0 + 0x1000 * 8 + 0, 4 + 0x1000 * 8},
            new int[]{1024 * 8 * 8 + 3, 1024 * 8 * 8 + 2, 1024 * 8 * 8 + 1, 1024 * 8 * 8 + 0, 3, 2, 1, 0,
                1024 * 8 * 8 + 3 + 64, 1024 * 8 * 8 + 2 + 64, 1024 * 8 * 8 + 1 + 64, 1024 * 8 * 8 + 0 + 64, 3 + 64, 2 + 64, 1 + 64, 0 + 64},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                0 * 8 + 16 * 8, 1 * 8 + 16 * 8, 2 * 8 + 16 * 8, 3 * 8 + 16 * 8, 4 * 8 + 16 * 8, 5 * 8 + 16 * 8, 6 * 8 + 16 * 8, 7 * 8 + 16 * 8},
            32 * 8 /* every tile takes 32 consecutive bytes */
    );

    static GfxLayout sprites = new GfxLayout(
            16, 16,
            0x200,
            3,
            new int[]{0x8000 * 8, 0x4000 * 8, 0},
            new int[]{16 * 8, 1 + (16 * 8), 2 + (16 * 8), 3 + (16 * 8), 4 + (16 * 8), 5 + (16 * 8), 6 + (16 * 8), 7 + (16 * 8),
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8, 8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            16 * 16
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout1, 16, 32),
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout2, 16, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, sprites, 0, 2),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tiles1a, 16 + 128, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tiles1b, 16 + 128, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x04000, tiles1a, 16 + 128, 8),
                new GfxDecodeInfo(REGION_GFX3, 0x04000, tiles1b, 16 + 128, 8),
                /* colors 16+192 to 16+255 are currently unassigned */
                new GfxDecodeInfo(-1) /* end of array */};

    /**
     * ***************************************************************************
     */
    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{30, 30},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    /**
     * ***************************************************************************
     */
    static MachineDriver machine_driver_bogeyman = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        2000000, /* 12 MHz clock on board */
                        bogeyman_readmem, bogeyman_writemem, null, null,
                        interrupt, 16 /* Controls sound */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            16 + 256, 16 + 256,
            bogeyman_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_MODIFIES_PALETTE,
            null,
            bogeyman_vh_start,
            bogeyman_vh_stop,
            bogeyman_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                )
            }
    );

    /**
     * ***************************************************************************
     */
    static RomLoadPtr rom_bogeyman = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x58000, REGION_CPU1);
            ROM_LOAD("j20.c14", 0x04000, 0x04000, 0xea90d637);
            ROM_LOAD("j10.c15", 0x08000, 0x04000, 0x0a8f218d);
            ROM_LOAD("j00.c17", 0x0c000, 0x04000, 0x5d486de9);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j70.h15", 0x00000, 0x04000, 0xfdc787bf);/* Characters */

            ROM_LOAD("j60.c17", 0x08000, 0x01000, 0xcc03ceb2);
            ROM_CONTINUE(0x0a000, 0x01000);

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j30.c9", 0x00000, 0x04000, 0x41af81c0);/* Sprites */

            ROM_LOAD("j40.c7", 0x04000, 0x04000, 0x8b438421);
            ROM_LOAD("j50.c5", 0x08000, 0x04000, 0xb507157f);

            ROM_REGION(0x10000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("j90.h12", 0x00000, 0x04000, 0x46b2d4d0);/* Tiles */

            ROM_LOAD("j80.h13", 0x04000, 0x04000, 0x77ebd0a4);
            ROM_LOAD("ja0.h10", 0x08000, 0x01000, 0xf2aa05ed);
            ROM_CONTINUE(0x0a000, 0x01000);
            ROM_CONTINUE(0x0c000, 0x01000);
            ROM_CONTINUE(0x0e000, 0x01000);

            ROM_REGION(0x0200, REGION_PROMS);
            ROM_LOAD("82s129.5k", 0x0000, 0x0100, 0x4a7c5367);/* Colour prom 1 */

            ROM_LOAD("82s129.6k", 0x0100, 0x0100, 0xb6127713);/* Colour prom 2 */

            ROM_END();
        }
    };

    /**
     * ***************************************************************************
     */
    public static GameDriver driver_bogeyman = new GameDriver("1985", "bogeyman", "bogeyman.java", rom_bogeyman, null, machine_driver_bogeyman, input_ports_bogeyman, null, ROT0, "Technos Japan", "Bogey Manor", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
}
