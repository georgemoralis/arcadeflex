/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 31/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//machine imports
import static arcadeflex.v036.machine.jrpacman.*;
//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;
//sound imports
import static arcadeflex.v036.sound.namcoH.*;
import static arcadeflex.v036.sound.namco.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.jrpacman.*;
//common imports
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class jrpacman {

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x4fff, MRA_RAM), /* including video and color RAM */
                new MemoryReadAddress(0x5000, 0x503f, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x5040, 0x507f, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x5080, 0x50bf, input_port_2_r), /* DSW1 */
                new MemoryReadAddress(0x8000, 0xdfff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x47ff, jrpacman_videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x4800, 0x4fef, MWA_RAM),
                new MemoryWriteAddress(0x4ff0, 0x4fff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x5000, 0x5000, interrupt_enable_w),
                new MemoryWriteAddress(0x5001, 0x5001, pengo_sound_enable_w),
                new MemoryWriteAddress(0x5003, 0x5003, jrpacman_flipscreen_w),
                new MemoryWriteAddress(0x5040, 0x505f, pengo_sound_w, namco_soundregs),
                new MemoryWriteAddress(0x5060, 0x506f, MWA_RAM, spriteram_2),
                new MemoryWriteAddress(0x5070, 0x5070, jrpacman_palettebank_w, jrpacman_palettebank),
                new MemoryWriteAddress(0x5071, 0x5071, jrpacman_colortablebank_w, jrpacman_colortablebank),
                new MemoryWriteAddress(0x5073, 0x5073, MWA_RAM, jrpacman_bgpriority),
                new MemoryWriteAddress(0x5074, 0x5074, jrpacman_charbank_w, jrpacman_charbank),
                new MemoryWriteAddress(0x5075, 0x5075, MWA_RAM, jrpacman_spritebank),
                new MemoryWriteAddress(0x5080, 0x5080, MWA_RAM, jrpacman_scroll),
                new MemoryWriteAddress(0x50c0, 0x50c0, MWA_NOP),
                new MemoryWriteAddress(0x8000, 0xdfff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0, 0, interrupt_vector_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_jrpacman = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BITX(0x10, 0x10, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN3);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x80, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x01, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x03, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x0c, 0x08, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x0c, "5");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "10000");
            PORT_DIPSETTING(0x10, "15000");
            PORT_DIPSETTING(0x20, "20000");
            PORT_DIPSETTING(0x30, "30000");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* FAKE */
 /* This fake input port is used to get the status of the fire button */
 /* and activate the speedup cheat if it is. */

            PORT_BITX(0x01, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Speedup Cheat", KEYCODE_LCONTROL, JOYCODE_1_BUTTON1);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8,
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 0, 1, 2, 3}, /* bits are packed in groups of four */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{8 * 8, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3, 16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3,
                24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3, 0, 1, 2, 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 128),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 0, 128),
                new GfxDecodeInfo(-1) /* end of array */};

    static namco_interface namco_interface = new namco_interface(
            3072000 / 32, /* sample rate */
            3, /* number of voices */
            100, /* playback volume */
            REGION_SOUND1 /* memory region */
    );

    static MachineDriver machine_driver_jrpacman = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 Mhz */
                        readmem, writemem, null, writeport,
                        jrpacman_interrupt, 1
                )
            },
            60, 2500, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            jrpacman_init_machine,
            /* video hardware */
            36 * 8, 28 * 8, new rectangle(0 * 8, 36 * 8 - 1, 0 * 8, 28 * 8 - 1),
            gfxdecodeinfo,
            32, 128 * 4,
            jrpacman_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            jrpacman_vh_start,
            jrpacman_vh_stop,
            jrpacman_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_NAMCO,
                        namco_interface
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
    static RomLoadHandlerPtr rom_jrpacman = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("jrp8d.bin", 0x0000, 0x2000, 0xe3fa972e);
            ROM_LOAD("jrp8e.bin", 0x2000, 0x2000, 0xec889e94);
            ROM_LOAD("jrp8h.bin", 0x8000, 0x2000, 0x35f1fc6e);
            ROM_LOAD("jrp8j.bin", 0xa000, 0x2000, 0x9737099e);
            ROM_LOAD("jrp8k.bin", 0xc000, 0x2000, 0x5252dd97);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("jrp2c.bin", 0x0000, 0x2000, 0x0527ff9b);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("jrp2e.bin", 0x0000, 0x2000, 0x73477193);

            ROM_REGION(0x0140, REGION_PROMS);
            ROM_LOAD("jrpacman.9e", 0x0000, 0x0020, 0x90012b3f);/* palette low bits */

            ROM_LOAD("jrpacman.9f", 0x0020, 0x0020, 0x8300178e);/* palette high bits */

            ROM_LOAD("jrpacman.9p", 0x0040, 0x0100, 0x9f6ea9d8);/* color lookup table */

            ROM_REGION(0x0100, REGION_SOUND1);/* sound prom */
 /* I don't know if this is correct. I'm using the Pac Man one. */

            ROM_LOAD("pacman.spr", 0x0000, 0x0100, BADCRC(0xa9cc86bf));
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_jrpacman = new InitDriverHandlerPtr() {
        public void handler() {
            /* The encryption PALs garble bits 0, 2 and 7 of the ROMs. The encryption */
 /* scheme is complex (basically it's a state machine) and can only be */
 /* faithfully emulated at run time. To avoid the performance hit that would */
 /* cause, here we have a table of the values which must be XORed with */
 /* each memory region to obtain the decrypted bytes. */
 /* Decryption table provided by David Caldwell (david@indigita.com) */
 /* For an accurate reproduction of the encryption, see jrcrypt.c */
 /*struct {
             int count;
             int value;
             } */
            int table[][]
                    = {
                        {0x00C1, 0x00}, {0x0002, 0x80}, {0x0004, 0x00}, {0x0006, 0x80},
                        {0x0003, 0x00}, {0x0002, 0x80}, {0x0009, 0x00}, {0x0004, 0x80},
                        {0x9968, 0x00}, {0x0001, 0x80}, {0x0002, 0x00}, {0x0001, 0x80},
                        {0x0009, 0x00}, {0x0002, 0x80}, {0x0009, 0x00}, {0x0001, 0x80},
                        {0x00AF, 0x00}, {0x000E, 0x04}, {0x0002, 0x00}, {0x0004, 0x04},
                        {0x001E, 0x00}, {0x0001, 0x80}, {0x0002, 0x00}, {0x0001, 0x80},
                        {0x0002, 0x00}, {0x0002, 0x80}, {0x0009, 0x00}, {0x0002, 0x80},
                        {0x0009, 0x00}, {0x0002, 0x80}, {0x0083, 0x00}, {0x0001, 0x04},
                        {0x0001, 0x01}, {0x0001, 0x00}, {0x0002, 0x05}, {0x0001, 0x00},
                        {0x0003, 0x04}, {0x0003, 0x01}, {0x0002, 0x00}, {0x0001, 0x04},
                        {0x0003, 0x01}, {0x0003, 0x00}, {0x0003, 0x04}, {0x0001, 0x01},
                        {0x002E, 0x00}, {0x0078, 0x01}, {0x0001, 0x04}, {0x0001, 0x05},
                        {0x0001, 0x00}, {0x0001, 0x01}, {0x0001, 0x04}, {0x0002, 0x00},
                        {0x0001, 0x01}, {0x0001, 0x04}, {0x0002, 0x00}, {0x0001, 0x01},
                        {0x0001, 0x04}, {0x0002, 0x00}, {0x0001, 0x01}, {0x0001, 0x04},
                        {0x0001, 0x05}, {0x0001, 0x00}, {0x0001, 0x01}, {0x0001, 0x04},
                        {0x0002, 0x00}, {0x0001, 0x01}, {0x0001, 0x04}, {0x0002, 0x00},
                        {0x0001, 0x01}, {0x0001, 0x04}, {0x0001, 0x05}, {0x0001, 0x00},
                        {0x01B0, 0x01}, {0x0001, 0x00}, {0x0002, 0x01}, {0x00AD, 0x00},
                        {0x0031, 0x01}, {0x005C, 0x00}, {0x0005, 0x01}, {0x604E, 0x00},
                        {0, 0}
                    };
            int i, j, A;
            UBytePtr RAM = memory_region(REGION_CPU1);

            A = 0;
            i = 0;
            while (table[i][0] != 0) {
                for (j = 0; j < table[i][0]; j++) {
                    RAM.write(A, RAM.read(A) ^ table[i][1]);
                    A++;
                }
                i++;
            }
        }
    };

    public static GameDriver driver_jrpacman = new GameDriver("1983", "jrpacman", "jrpacman.java", rom_jrpacman, null, machine_driver_jrpacman, input_ports_jrpacman, init_jrpacman, ROT90, "Bally Midway", "Jr. Pac-Man");
}
