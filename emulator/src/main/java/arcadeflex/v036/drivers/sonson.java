
/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package arcadeflex.v036.drivers;

//mame imports
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.sonson.*;
import static arcadeflex.v036.vidhrdw.generic.*;
//TODO
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;

public class sonson {

    static int last;
    public static WriteHandlerPtr sonson_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            if (last == 0 && data == 1) {
                /* setting bit null low then high triggers IRQ on the sound CPU */
                cpu_cause_interrupt(1, M6809_INT_FIRQ);
            }

            last = data;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x17ff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(0x3002, 0x3002, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x3003, 0x3003, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x3004, 0x3004, input_port_2_r), /* IN2 */
                new MemoryReadAddress(0x3005, 0x3005, input_port_3_r), /* DSW0 */
                new MemoryReadAddress(0x3006, 0x3006, input_port_4_r), /* DSW1 */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x13ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1400, 0x17ff, colorram_w, colorram),
                new MemoryWriteAddress(0x2020, 0x207f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3000, 0x3000, MWA_RAM, sonson_scrollx),
                new MemoryWriteAddress(0x3008, 0x3008, MWA_NOP),
                new MemoryWriteAddress(0x3010, 0x3010, soundlatch_w),
                new MemoryWriteAddress(0x3018, 0x3018, MWA_NOP),
                new MemoryWriteAddress(0x3019, 0x3019, sonson_sh_irqtrigger_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(0xe000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2000, AY8910_control_port_0_w),
                new MemoryWriteAddress(0x2001, 0x2001, AY8910_write_port_0_w),
                new MemoryWriteAddress(0x4000, 0x4000, AY8910_control_port_1_w),
                new MemoryWriteAddress(0x4001, 0x4001, AY8910_write_port_1_w),
                new MemoryWriteAddress(0xe000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_sonson = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coinage"));
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
            PORT_DIPNAME(0x10, 0x10, "Coinage affects");
            PORT_DIPSETTING(0x10, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("Coin_B"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x40, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x80, 0x80, "unknown");/* maybe flip screen */

            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x02, "4");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, "2 Players Game");
            PORT_DIPSETTING(0x04, "1 Credit");
            PORT_DIPSETTING(0x00, "2 Credits");
            PORT_DIPNAME(0x18, 0x08, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "20000 80000 100000");
            PORT_DIPSETTING(0x00, "30000 90000 120000");
            PORT_DIPSETTING(0x18, "20000");
            PORT_DIPSETTING(0x10, "30000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x80, "Freeze");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            2, /* 2 bits per pixel */
            new int[]{1024 * 8 * 8, 0}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7}, /* pretty straightforward layout */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            3, /* 3 bits per pixel */
            new int[]{2 * 2048 * 8 * 8, 2048 * 8 * 8, 0}, /* the two bitplanes are separated */
            new int[]{8 * 16 + 7, 8 * 16 + 6, 8 * 16 + 5, 8 * 16 + 4, 8 * 16 + 3, 8 * 16 + 2, 8 * 16 + 1, 8 * 16 + 0, /* pretty straightforward layout */
                7, 6, 5, 4, 3, 2, 1, 0},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 64 * 4, 32),
                new GfxDecodeInfo(-1) /* end of array */};
    /*static struct AY8910interface ay8910_interface =
     {
     2,	/* 2 chips */
 /*	1500000,	/* 1.5 MHz ? */
 /*	{ 30, 30 },
     { 0 },
     { 0 },
     { 0 },
     { 0 }
     };*/
    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHz ???? */
            new int[]{30, 30},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MachineDriver machine_driver_sonson = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* 2 Mhz (?) */
                        readmem, writemem, null, null,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        2000000, /* 2 Mhz (?) */
                        sound_readmem, sound_writemem, null, null,
                        interrupt, 4 /* FIRQs are triggered by the main CPU */
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 1 * 8, 31 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4 + 32 * 8,
            sonson_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            sonson_vh_screenrefresh,
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
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_sonson = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code + 3*16k for the banked ROMs images */

            ROM_LOAD("ss.01e", 0x4000, 0x4000, 0xcd40cc54);
            ROM_LOAD("ss.02e", 0x8000, 0x4000, 0xc3476527);
            ROM_LOAD("ss.03e", 0xc000, 0x4000, 0x1fd0e729);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the audio CPU */

            ROM_LOAD("ss3.v12", 0xe000, 0x2000, 0x1135c48a);

            ROM_REGION(0x04000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ss5.v12", 0x00000, 0x2000, 0x990890b1);/* characters */

            ROM_LOAD("ss6.v12", 0x02000, 0x2000, 0x9388ff82);

            ROM_REGION(0x0c000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ss7.v12", 0x00000, 0x4000, 0x32b14b8e);/* sprites */

            ROM_LOAD("ss8.v12", 0x04000, 0x4000, 0x9f59014e);
            ROM_LOAD("ss9.v12", 0x08000, 0x4000, 0xe240345a);

            ROM_REGION(0x0240, REGION_PROMS);
            ROM_LOAD("ss12.bin", 0x0000, 0x0020, 0xc8eaf234);/* red/green component */

            ROM_LOAD("ss13.bin", 0x0020, 0x0020, 0x0e434add);/* blue component */

            ROM_LOAD("ssb2.bin", 0x0040, 0x0100, 0x6ce8ac39);/* character lookup table */

            ROM_LOAD("ssb.bin", 0x0140, 0x0100, 0xd4f7bfb5);/* sprite lookup table */

            ROM_END();
        }
    };

    public static GameDriver driver_sonson = new GameDriver("1984", "sonson", "sonson.java", rom_sonson, null, machine_driver_sonson, input_ports_sonson, null, ROT0, "Capcom", "Son Son");
}
