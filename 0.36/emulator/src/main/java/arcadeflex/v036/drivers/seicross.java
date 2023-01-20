/*
 * ported to v0.36
 */
/**
 * Changelog
 * =========
 * 20/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static arcadeflex.v036.sound.dac.*;
import static arcadeflex.v036.sound.dacH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.vidhrdw.seicross.*;
//common imports
import static common.libc.cstring.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fread;
import static gr.codebb.arcadeflex.v036.platform.fileio.osd_fwrite;

public class seicross {

    static UBytePtr nvram = new UBytePtr();
    static int[] nvram_size = new int[1];

    public static nvramHandlerPtr nvram_handler = new nvramHandlerPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                osd_fwrite(file, nvram, nvram_size[0]);
            } else {
                if (file != null) {
                    osd_fread(file, nvram, nvram_size[0]);
                } else {
                    /* fill in the default values */
                    memset(nvram, 0, nvram_size[0]);
                    nvram.write(0x0d, 1);
                    nvram.write(0x0f, 1);
                    nvram.write(0x11, 1);
                    nvram.write(0x13, 1);
                    nvram.write(0x15, 1);
                    nvram.write(0x19, 1);
                    nvram.write(0x17, 3);
                }
            }
        }
    };

    public static InitMachineHandlerPtr friskyt_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            /* start with the protection mcu halted */
            cpu_set_halt_line(1, ASSERT_LINE);
        }
    };

    static int portb;

    public static ReadHandlerPtr friskyt_portB_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (portb & 0x9f) | (readinputport(6) & 0x60);
        }
    };

    public static WriteHandlerPtr friskyt_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //logerror("PC %04x: 8910 port B = %02x\n",cpu_get_pc(),data);
            /* bit 0 is IRQ enable */
            interrupt_enable_w.handler(0, data & 1);

            /* bit 1 flips screen */
 /* bit 2 resets the microcontroller */
            if (((portb & 4) == 0) && (data & 4) != 0) {
                /* reset and start the protection mcu */
                cpu_set_reset_line(1, PULSE_LINE);
                cpu_set_halt_line(1, CLEAR_LINE);
            }

            /* other bits unknown */
            portb = data;
        }
    };

    static UBytePtr sharedram = new UBytePtr();

    public static ReadHandlerPtr sharedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return sharedram.read(offset);
        }
    };

    public static WriteHandlerPtr sharedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            sharedram.write(offset, data);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x77ff, MRA_ROM),
                new MemoryReadAddress(0x7800, 0x7fff, sharedram_r),
                new MemoryReadAddress(0x8820, 0x887f, MRA_RAM),
                new MemoryReadAddress(0x9000, 0x93ff, MRA_RAM), /* video RAM */
                new MemoryReadAddress(0x9800, 0x981f, MRA_RAM),
                new MemoryReadAddress(0x9c00, 0x9fff, MRA_RAM), /* color RAM */
                new MemoryReadAddress(0xa000, 0xa000, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xa800, 0xa800, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xb000, 0xb000, input_port_2_r), /* test */
                new MemoryReadAddress(0xb800, 0xb800, watchdog_reset_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x77ff, MWA_ROM),
                new MemoryWriteAddress(0x7800, 0x7fff, sharedram_w, sharedram),
                new MemoryWriteAddress(0x8820, 0x887f, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9000, 0x93ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x9800, 0x981f, MWA_RAM, seicross_row_scroll),
                new MemoryWriteAddress(0x9880, 0x989f, MWA_RAM, spriteram_2, spriteram_2_size),
                new MemoryWriteAddress(0x9c00, 0x9fff, seicross_colorram_w, colorram),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x04, 0x04, AY8910_read_port_0_r),
                new IOReadPort(0x0c, 0x0c, AY8910_read_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_control_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_write_port_0_w),
                new IOWritePort(0x08, 0x08, AY8910_control_port_0_w),
                new IOWritePort(0x09, 0x09, AY8910_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress mcu_nvram_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x1000, 0x10ff, MRA_RAM),
                new MemoryReadAddress(0x8000, 0xf7ff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, sharedram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryReadAddress mcu_no_nvram_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x007f, MRA_RAM),
                new MemoryReadAddress(0x1003, 0x1003, input_port_3_r), /* DSW1 */
                new MemoryReadAddress(0x1005, 0x1005, input_port_4_r), /* DSW2 */
                new MemoryReadAddress(0x1006, 0x1006, input_port_5_r), /* DSW3 */
                new MemoryReadAddress(0x8000, 0xf7ff, MRA_ROM),
                new MemoryReadAddress(0xf800, 0xffff, sharedram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress mcu_nvram_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x1000, 0x10ff, MWA_RAM, nvram, nvram_size),
                new MemoryWriteAddress(0x2000, 0x2000, DAC_data_w),
                new MemoryWriteAddress(0x8000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xffff, sharedram_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryWriteAddress mcu_no_nvram_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x007f, MWA_RAM),
                new MemoryWriteAddress(0x2000, 0x2000, DAC_data_w),
                new MemoryWriteAddress(0x8000, 0xf7ff, MWA_ROM),
                new MemoryWriteAddress(0xf800, 0xffff, sharedram_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_friskyt = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_SERVICE(0x20, IP_ACTIVE_HIGH);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0x80, 0x00, "Counter Check");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* Test */
            PORT_DIPNAME(0x01, 0x00, "Test Mode");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, "Connection Error");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0xfc, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_radrad = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* Test */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0xfc, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x06, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x06, "5");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x07, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x05, "5");
            PORT_DIPSETTING(0x06, "6");
            PORT_DIPSETTING(0x07, "7");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* DSW3 */
            PORT_DIPNAME(0x0f, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x09, DEF_STR("2C_2C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x0a, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x0b, DEF_STR("2C_4C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x0c, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0x0d, DEF_STR("2C_6C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0e, DEF_STR("2C_7C"));
            PORT_DIPSETTING(0x0f, DEF_STR("2C_8C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x05, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x07, DEF_STR("1C_8C"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    static InputPortHandlerPtr input_ports_seicross = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);

            PORT_START();
            /* Test */
            PORT_SERVICE(0x01, IP_ACTIVE_HIGH);
            PORT_DIPNAME(0x02, 0x00, "Connection Error");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_BIT(0xfc, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* probably unused */

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, "0");
            PORT_DIPSETTING(0x04, "1");
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x0c, "3");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x08, "2");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x04, "4");
            PORT_DIPSETTING(0x0c, "5");
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* DSW3 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x03, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_6C"));
            PORT_BIT(0xf0, IP_ACTIVE_HIGH, IPT_UNUSED);

            PORT_START();
            /* Debug */
            PORT_BIT(0x1f, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_DIPNAME(0x20, 0x20, "Debug Mode");
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BITX(0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes are packed in one byte */
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            256, /* 256 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes are packed in one byte */
            new int[]{0, 1, 2, 3, 8 + 0, 8 + 1, 8 + 2, 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 17 * 8 + 0, 17 * 8 + 1, 17 * 8 + 2, 17 * 8 + 3},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX1, 0, spritelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1536000, /* 1.536 MHz ?? */
            new int[]{25},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{friskyt_portB_r},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{friskyt_portB_w}
    );

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{25}
    );

    static MachineDriver machine_driver_nvram = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 MHz? */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_NSC8105,
                        6000000 / 4, /* ??? */
                        mcu_nvram_readmem, mcu_nvram_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            20, /* 20 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            friskyt_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            64, 64,
            seicross_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            seicross_vh_screenrefresh,
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
            },
            nvram_handler
    );
    static MachineDriver machine_driver_no_nvram = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3072000, /* 3.072 MHz? */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_NSC8105,
                        6000000 / 4, /* ??? */
                        mcu_no_nvram_readmem, mcu_no_nvram_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            20, /* 20 CPU slices per frame - an high value to ensure proper */
            /* synchronization of the CPUs */
            friskyt_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            64, 64,
            seicross_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            seicross_vh_screenrefresh,
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
            },
            null
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_friskyt = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ftom.01", 0x0000, 0x1000, 0xbce5d486);
            ROM_LOAD("ftom.02", 0x1000, 0x1000, 0x63157d6e);
            ROM_LOAD("ftom.03", 0x2000, 0x1000, 0xc8d9ef2c);
            ROM_LOAD("ftom.04", 0x3000, 0x1000, 0x23a01aac);
            ROM_LOAD("ftom.05", 0x4000, 0x1000, 0xbfaf702a);
            ROM_LOAD("ftom.06", 0x5000, 0x1000, 0xbce70b9c);
            ROM_LOAD("ftom.07", 0x6000, 0x1000, 0xb2ef303a);
            ROM_LOAD("ft8_8.rom", 0x7000, 0x0800, 0x10461a24);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the protection mcu */
 /* filled in later */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ftom.11", 0x0000, 0x1000, 0x1ec6ff65);
            ROM_LOAD("ftom.12", 0x1000, 0x1000, 0x3b8f40b5);
            ROM_LOAD("ftom.09", 0x2000, 0x1000, 0x60642f25);
            ROM_LOAD("ftom.10", 0x3000, 0x1000, 0x07b9dcfc);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("ft.9c", 0x0000, 0x0020, 0x0032167e);
            ROM_LOAD("ft.9b", 0x0020, 0x0020, 0x6b364e69);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_radrad = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("1.3a", 0x0000, 0x1000, 0xb1e958ca);
            ROM_LOAD("2.3b", 0x1000, 0x1000, 0x30ba76b3);
            ROM_LOAD("3.3c", 0x2000, 0x1000, 0x1c9f397b);
            ROM_LOAD("4.3d", 0x3000, 0x1000, 0x453966a3);
            ROM_LOAD("5.3e", 0x4000, 0x1000, 0xc337c4bd);
            ROM_LOAD("6.3f", 0x5000, 0x1000, 0x06e15b59);
            ROM_LOAD("7.3g", 0x6000, 0x1000, 0x02b1f9c9);
            ROM_LOAD("8.3h", 0x7000, 0x0800, 0x911c90e8);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the protection mcu */
 /* filled in later */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("11.l7", 0x0000, 0x1000, 0x4ace7afb);
            ROM_LOAD("12.n7", 0x1000, 0x1000, 0xb19b8473);
            ROM_LOAD("9.j7", 0x2000, 0x1000, 0x229939a3);
            ROM_LOAD("10.j7", 0x3000, 0x1000, 0x79237913);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("clr.9c", 0x0000, 0x0020, 0xc9d88422);
            ROM_LOAD("clr.9b", 0x0020, 0x0020, 0xee81af16);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_seicross = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("smc1", 0x0000, 0x1000, 0xf6c3aeca);
            ROM_LOAD("smc2", 0x1000, 0x1000, 0x0ec6c218);
            ROM_LOAD("smc3", 0x2000, 0x1000, 0xceb3c8f4);
            ROM_LOAD("smc4", 0x3000, 0x1000, 0x3112af59);
            ROM_LOAD("smc5", 0x4000, 0x1000, 0xb494a993);
            ROM_LOAD("smc6", 0x5000, 0x1000, 0x09d5b9da);
            ROM_LOAD("smc7", 0x6000, 0x1000, 0x13052b03);
            ROM_LOAD("smc8", 0x7000, 0x0800, 0x2093461d);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the protection mcu */
 /* filled in later */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sz11.7k", 0x0000, 0x1000, 0xfbd9b91d);
            ROM_LOAD("smcd", 0x1000, 0x1000, 0xc3c953c4);
            ROM_LOAD("sz9.7j", 0x2000, 0x1000, 0x4819f0cd);
            ROM_LOAD("sz10.7h", 0x3000, 0x1000, 0x4c268778);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("sz73.10c", 0x0000, 0x0020, 0x4d218a3c);
            ROM_LOAD("sz74.10b", 0x0020, 0x0020, 0xc550531c);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_sectrzon = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sz1.3a", 0x0000, 0x1000, 0xf0a45cb4);
            ROM_LOAD("sz2.3c", 0x1000, 0x1000, 0xfea68ddb);
            ROM_LOAD("sz3.3d", 0x2000, 0x1000, 0xbaad4294);
            ROM_LOAD("sz4.3e", 0x3000, 0x1000, 0x75f2ca75);
            ROM_LOAD("sz5.3fg", 0x4000, 0x1000, 0xdc14f2c8);
            ROM_LOAD("sz6.3h", 0x5000, 0x1000, 0x397a38c5);
            ROM_LOAD("sz7.3i", 0x6000, 0x1000, 0x7b34dc1c);
            ROM_LOAD("sz8.3j", 0x7000, 0x0800, 0x9933526a);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the protection mcu */
 /* filled in later */

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sz11.7k", 0x0000, 0x1000, 0xfbd9b91d);
            ROM_LOAD("sz12.7m", 0x1000, 0x1000, 0x2bdef9ad);
            ROM_LOAD("sz9.7j", 0x2000, 0x1000, 0x4819f0cd);
            ROM_LOAD("sz10.7h", 0x3000, 0x1000, 0x4c268778);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("sz73.10c", 0x0000, 0x0020, 0x4d218a3c);
            ROM_LOAD("sz74.10b", 0x0020, 0x0020, 0xc550531c);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_friskyt = new InitDriverHandlerPtr() {
        public void handler() {
            int A;
            UBytePtr src;
            UBytePtr dest;

            /* the protection mcu shares the main program ROMs and RAM with the main CPU. */
 /* copy over the ROMs */
            src = memory_region(REGION_CPU1);
            dest = memory_region(REGION_CPU2);
            for (A = 0; A < 0x8000; A++) {
                dest.write(A + 0x8000, src.read(A));
            }
        }
    };

    public static GameDriver driver_friskyt = new GameDriver("1981", "friskyt", "seicross.java", rom_friskyt, null, machine_driver_nvram, input_ports_friskyt, init_friskyt, ROT0, "Nichibutsu", "Frisky Tom", GAME_NO_COCKTAIL);
    public static GameDriver driver_radrad = new GameDriver("1982", "radrad", "seicross.java", rom_radrad, null, machine_driver_no_nvram, input_ports_radrad, init_friskyt, ROT0, "Nichibutsu USA", "Radical Radial", GAME_NO_COCKTAIL);
    public static GameDriver driver_seicross = new GameDriver("1984", "seicross", "seicross.java", rom_seicross, null, machine_driver_no_nvram, input_ports_seicross, init_friskyt, ROT90, "Nichibutsu + Alice", "Seicross", GAME_NO_COCKTAIL);
    public static GameDriver driver_sectrzon = new GameDriver("1984", "sectrzon", "seicross.java", rom_sectrzon, driver_seicross, machine_driver_no_nvram, input_ports_seicross, init_friskyt, ROT90, "Nichibutsu + Alice", "Sector Zone", GAME_NO_COCKTAIL);
}
