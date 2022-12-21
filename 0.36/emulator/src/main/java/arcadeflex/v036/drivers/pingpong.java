/**
 * ported to 0.36
 */
/**
 * Changelog
 * =========
 * 21/12/2022 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
//sound imports
import static arcadeflex.v036.sound.sn76496H.*;
import static arcadeflex.v036.sound.sn76496.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.pingpong.*;
//TODO
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class pingpong {

    static UBytePtr intenable = new UBytePtr();

    public static InterruptHandlerPtr pingpong_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                if ((intenable.read() & 0x04) != 0) {
                    return interrupt.handler();
                }
            } else if ((cpu_getiloops() % 2) != 0) {
                if ((intenable.read() & 0x08) != 0) {
                    return nmi_interrupt.handler();
                }
            }

            return ignore_interrupt.handler();
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7FFF, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87FF, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x97FF, MRA_RAM),
                new MemoryReadAddress(0xA800, 0xA800, input_port_0_r),
                new MemoryReadAddress(0xA880, 0xA880, input_port_1_r),
                new MemoryReadAddress(0xA900, 0xA900, input_port_2_r),
                new MemoryReadAddress(0xA980, 0xA980, input_port_3_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7FFF, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83FF, colorram_w, colorram),
                new MemoryWriteAddress(0x8400, 0x87FF, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9000, 0x9002, MWA_RAM),
                new MemoryWriteAddress(0x9003, 0x9052, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9053, 0x97FF, MWA_RAM),
                new MemoryWriteAddress(0xA000, 0xA000, MWA_RAM, intenable), /* bit 2 = irq enable, bit 3 = nmi enable */
                /* bit 0/1 = coin counters */
                /* other bits unknown */
                new MemoryWriteAddress(0xA200, 0xA200, MWA_NOP), /* SN76496 data latch */
                new MemoryWriteAddress(0xA400, 0xA400, SN76496_0_w), /* trigger read */
                new MemoryWriteAddress(0xA600, 0xA600, watchdog_reset_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_pingpong = new InputPortHandlerPtr() {
        public void handler() {

            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_BUTTON1);

            PORT_START();
            /* DSW1 */

            PORT_DIPNAME(0x0F, 0x0F, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x04, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x0A, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0x0F, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0C, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x0E, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x06, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0B, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0D, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x09, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));
            PORT_DIPNAME(0xF0, 0xF0, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x40, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0xA0, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x80, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xF0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xC0, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0xE0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x70, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xB0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x30, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xD0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x50, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW2 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x06, "Easy");
            PORT_DIPSETTING(0x02, "Normal");
            PORT_DIPSETTING(0x04, "Difficult");
            PORT_DIPSETTING(0x00, "Very Difficult");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{4, 0}, /* the bitplanes are packed in one nibble */
            new int[]{3, 2, 1, 0, 8 * 8 + 3, 8 * 8 + 2, 8 * 8 + 1, 8 * 8 + 0}, /* x bit */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* y bit */
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            128, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{4, 0}, /* the bitplanes are packed in one nibble */
            new int[]{12 * 16 + 3, 12 * 16 + 2, 12 * 16 + 1, 12 * 16 + 0,
                8 * 16 + 3, 8 * 16 + 2, 8 * 16 + 1, 8 * 16 + 0,
                4 * 16 + 3, 4 * 16 + 2, 4 * 16 + 1, 4 * 16 + 0,
                3, 2, 1, 0}, /* x bit */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8}, /* y bit */
            64 * 8 /* every char takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 64 * 4, 64),
                new GfxDecodeInfo(-1) /* end of array */};
    static SN76496interface sn76496_interface = new SN76496interface(
            1, /* 1 chip */
            new int[]{18432000 / 8}, /* 2.304 MHz */
            new int[]{100}
    );

    static MachineDriver machine_driver_pingpong = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        18432000 / 6, /* 3.072 MHz (probably) */
                        readmem, writemem, null, null,
                        pingpong_interrupt, 16 /* 1 IRQ + 8 NMI */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1,
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 64 * 4 + 64 * 4,
            pingpong_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            pingpong_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
                ),}
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_pingpong = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("pp_e04.rom", 0x0000, 0x4000, 0x18552f8f);
            ROM_LOAD("pp_e03.rom", 0x4000, 0x4000, 0xae5f01e8);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("pp_e01.rom", 0x0000, 0x2000, 0xd1d6f090);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("pp_e02.rom", 0x0000, 0x2000, 0x33c687e0);

            ROM_REGION(0x0220, REGION_PROMS);
            ROM_LOAD("pingpong.3j", 0x0000, 0x0020, 0x3e04f06e);/* palette (this might be bad) */

            ROM_LOAD("pingpong.11j", 0x0020, 0x0100, 0x09d96b08);/* sprites */

            ROM_LOAD("pingpong.5h", 0x0120, 0x0100, 0x8456046a);/* characters */

            ROM_END();
        }
    };

    public static GameDriver driver_pingpong = new GameDriver("1985", "pingpong", "pingpong.java", rom_pingpong, null, machine_driver_pingpong, input_ports_pingpong, null, ROT0, "Konami", "Ping Pong");
}
