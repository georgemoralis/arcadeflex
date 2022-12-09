/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.mystston.*;
//TODO
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class mystston {

    static int coin_myst;
    public static InterruptHandlerPtr mystston_interrupt = new InterruptHandlerPtr() {
        public int handler() {

            if ((readinputport(0) & 0xc0) != 0xc0) {
                if (coin_myst == 0) {
                    coin_myst = 1;
                    return nmi_interrupt.handler();
                }
            } else {
                coin_myst = 0;
            }

            return interrupt.handler();
        }
    };

    static int psg_latch;

    public static WriteHandlerPtr mystston_8910_latch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            psg_latch = data;
        }
    };
    static int last;
    public static WriteHandlerPtr mystston_8910_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 5 goes to 8910 #0 BDIR pin  */
            if ((last & 0x20) == 0x20 && (data & 0x20) == 0x00) {
                /* bit 4 goes to the 8910 #0 BC1 pin */
                if ((last & 0x10) != 0) {
                    AY8910_control_port_0_w.handler(0, psg_latch);
                } else {
                    AY8910_write_port_0_w.handler(0, psg_latch);
                }
            }
            /* bit 7 goes to 8910 #1 BDIR pin  */
            if ((last & 0x80) == 0x80 && (data & 0x80) == 0x00) {
                /* bit 6 goes to the 8910 #1 BC1 pin */
                if ((last & 0x40) != 0) {
                    AY8910_control_port_1_w.handler(0, psg_latch);
                } else {
                    AY8910_write_port_1_w.handler(0, psg_latch);
                }
            }

            last = data;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x077f, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0fff, MRA_RAM), /* work RAM? */
                new MemoryReadAddress(0x1000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x2000, input_port_0_r),
                new MemoryReadAddress(0x2010, 0x2010, input_port_1_r),
                new MemoryReadAddress(0x2020, 0x2020, input_port_2_r),
                new MemoryReadAddress(0x2030, 0x2030, input_port_3_r),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x077f, MWA_RAM),
                new MemoryWriteAddress(0x0780, 0x07df, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x0800, 0x0fff, MWA_RAM), /* work RAM? */
                new MemoryWriteAddress(0x1000, 0x13ff, MWA_RAM, mystston_videoram2, mystston_videoram2_size),
                new MemoryWriteAddress(0x1400, 0x17ff, MWA_RAM, mystston_colorram2),
                new MemoryWriteAddress(0x1800, 0x19ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1a00, 0x1bff, colorram_w, colorram),
                new MemoryWriteAddress(0x1c00, 0x1fff, MWA_RAM), /* work RAM? This gets copied to videoram */
                new MemoryWriteAddress(0x2000, 0x2000, mystston_2000_w), /* flip screen  coin counters */
                new MemoryWriteAddress(0x2010, 0x2010, watchdog_reset_w), /* or IRQ acknowledge maybe? */
                new MemoryWriteAddress(0x2020, 0x2020, MWA_RAM, mystston_scroll),
                new MemoryWriteAddress(0x2030, 0x2030, mystston_8910_latch_w),
                new MemoryWriteAddress(0x2040, 0x2040, mystston_8910_control_w),
                new MemoryWriteAddress(0x2060, 0x2077, paletteram_BBGGGRRR_w, paletteram),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_mystston = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_LOW, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_LOW, IPT_COIN1, 1);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Demo_Sounds"));
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

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_3C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x40, DEF_STR("Cocktail"));
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            3, /* 3 bits per pixel */
            new int[]{2 * 2048 * 8 * 8, 2048 * 8 * 8, 0}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            3, /* 3 bits per pixel */
            new int[]{2 * 512 * 16 * 16, 512 * 16 * 16, 0}, /* the bitplanes are separated */
            new int[]{16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 16 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 tiles */
            512, /* 512 tiles */
            3, /* 3 bits per pixel */
            new int[]{2 * 512 * 16 * 16, 512 * 16 * 16, 0}, /* the bitplanes are separated */
            new int[]{16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7,
                0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every tile takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 3 * 8, 4),
                new GfxDecodeInfo(REGION_GFX2, 0, tilelayout, 2 * 8, 1),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 2),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ? */
            new int[]{30, 30},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_mystston = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1500000, /* 1.5 MHz ???? */
                        readmem, writemem, null, null,
                        mystston_interrupt, 16 /* ? controls music tempo */
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            24 + 32, 24 + 32,
            mystston_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            mystston_vh_start,
            mystston_vh_stop,
            mystston_vh_screenrefresh,
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
     * *************************************************************************
     *
     * Mysterious Stones driver
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_mystston = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ms0", 0x4000, 0x2000, 0x6dacc05f);
            ROM_LOAD("ms1", 0x6000, 0x2000, 0xa3546df7);
            ROM_LOAD("ms2", 0x8000, 0x2000, 0x43bc6182);
            ROM_LOAD("ms3", 0xa000, 0x2000, 0x9322222b);
            ROM_LOAD("ms4", 0xc000, 0x2000, 0x47cefe9b);
            ROM_LOAD("ms5", 0xe000, 0x2000, 0xb37ae12b);

            ROM_REGION(0x0c000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ms6", 0x00000, 0x2000, 0x85c83806);
            ROM_LOAD("ms9", 0x02000, 0x2000, 0xb146c6ab);
            ROM_LOAD("ms7", 0x04000, 0x2000, 0xd025f84d);
            ROM_LOAD("ms10", 0x06000, 0x2000, 0xd85015b5);
            ROM_LOAD("ms8", 0x08000, 0x2000, 0x53765d89);
            ROM_LOAD("ms11", 0x0a000, 0x2000, 0x919ee527);

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ms12", 0x00000, 0x2000, 0x72d8331d);
            ROM_LOAD("ms13", 0x02000, 0x2000, 0x845a1f9b);
            ROM_LOAD("ms14", 0x04000, 0x2000, 0x822874b0);
            ROM_LOAD("ms15", 0x06000, 0x2000, 0x4594e53c);
            ROM_LOAD("ms16", 0x08000, 0x2000, 0x2f470b0f);
            ROM_LOAD("ms17", 0x0a000, 0x2000, 0x38966d1b);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("ic61", 0x0000, 0x0020, 0xe802d6cf);
            ROM_END();
        }
    };

    public static GameDriver driver_mystston = new GameDriver("1984", "mystston", "mystston.java", rom_mystston, null, machine_driver_mystston, input_ports_mystston, null, ROT270, "Technos", "Mysterious Stones");
}
