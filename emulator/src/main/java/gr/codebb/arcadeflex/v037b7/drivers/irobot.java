/*
 * ported to v0.37b7
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.cpu.m6809.m6809H.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_r;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.soundlatch_w;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_K007232;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2151;
import static gr.codebb.arcadeflex.v036.sound.pokey.*;
import static gr.codebb.arcadeflex.v037b7.machine.irobot.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.irobot.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.pokeyH.*;

public class irobot {

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
                    memset(nvram, 0, nvram_size[0]);
                }
            }
        }
    };

    public static WriteHandlerPtr irobot_nvram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nvram.write(offset, data & 0x0f);
        }
    };

    public static WriteHandlerPtr irobot_clearirq_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_set_irq_line(0, M6809_IRQ_LINE, CLEAR_LINE);
        }
    };

    public static WriteHandlerPtr irobot_clearfirq_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_set_irq_line(0, M6809_FIRQ_LINE, CLEAR_LINE);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, MRA_RAM),
                new MemoryReadAddress(0x0800, 0x0fff, MRA_BANK2),
                new MemoryReadAddress(0x1000, 0x103f, input_port_0_r),
                new MemoryReadAddress(0x1040, 0x1040, input_port_1_r),
                new MemoryReadAddress(0x1080, 0x1080, irobot_status_r),
                new MemoryReadAddress(0x10c0, 0x10c0, input_port_3_r),
                new MemoryReadAddress(0x1200, 0x12ff, MRA_RAM),
                new MemoryReadAddress(0x1300, 0x13ff, irobot_control_r),
                new MemoryReadAddress(0x1400, 0x143f, quad_pokey_r),
                new MemoryReadAddress(0x1c00, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2000, 0x3fff, irobot_sharedmem_r),
                new MemoryReadAddress(0x4000, 0x5fff, MRA_BANK1),
                new MemoryReadAddress(0x6000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, MWA_RAM),
                new MemoryWriteAddress(0x0800, 0x0fff, MWA_BANK2),
                new MemoryWriteAddress(0x1100, 0x1100, irobot_clearirq_w),
                new MemoryWriteAddress(0x1140, 0x1140, irobot_statwr_w),
                new MemoryWriteAddress(0x1180, 0x1180, irobot_out0_w),
                new MemoryWriteAddress(0x11c0, 0x11c0, irobot_rom_banksel_w),
                new MemoryWriteAddress(0x1200, 0x12ff, irobot_nvram_w, nvram, nvram_size),
                new MemoryWriteAddress(0x1400, 0x143f, quad_pokey_w),
                new MemoryWriteAddress(0x1800, 0x18ff, irobot_paletteram_w),
                new MemoryWriteAddress(0x1900, 0x19ff, MWA_RAM), /* Watchdog reset */
                new MemoryWriteAddress(0x1a00, 0x1a00, irobot_clearfirq_w),
                new MemoryWriteAddress(0x1b00, 0x1bff, irobot_control_w),
                new MemoryWriteAddress(0x1c00, 0x1fff, MWA_RAM, videoram, videoram_size),
                new MemoryWriteAddress(0x2000, 0x3fff, irobot_sharedmem_w),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_irobot = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_SERVICE(0x10, IP_ACTIVE_LOW);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START1);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* MB DONE */
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* EXT DONE */
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x03, 0x00, "Coins Per Credit");
            PORT_DIPSETTING(0x00, "1 Coin 1 Credit");
            PORT_DIPSETTING(0x01, "2 Coins 1 Credit");
            PORT_DIPSETTING(0x02, "3 Coins 1 Credit");
            PORT_DIPSETTING(0x03, "4 Coins 1 Credit");
            PORT_DIPNAME(0x0c, 0x00, "Right Coin");
            PORT_DIPSETTING(0x00, "1 Coin for 1 Coin Unit");
            PORT_DIPSETTING(0x04, "1 Coin for 4 Coin Units");
            PORT_DIPSETTING(0x08, "1 Coin for 5 Coin Units");
            PORT_DIPSETTING(0x0c, "1 Coin for 6 Coin Units");
            PORT_DIPNAME(0x10, 0x00, "Left Coin");
            PORT_DIPSETTING(0x00, "1 Coin for 1 Coin Unit");
            PORT_DIPSETTING(0x10, "1 Coin for 2 Coin Units");
            PORT_DIPNAME(0xe0, 0x00, "Bonus Adder");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPSETTING(0x20, "1 Credit for 2 Coin Units");
            PORT_DIPSETTING(0xa0, "1 Credit for 3 Coin Units");
            PORT_DIPSETTING(0x40, "1 Credit for 4 Coin Units");
            PORT_DIPSETTING(0x80, "1 Credit for 5 Coin Units");
            PORT_DIPSETTING(0x60, "2 Credits for 4 Coin Units");
            PORT_DIPSETTING(0xe0, DEF_STR("Free_Play"));

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x01, 0x01, "Language");
            PORT_DIPSETTING(0x01, "English");
            PORT_DIPSETTING(0x00, "German");
            PORT_DIPNAME(0x02, 0x02, "Min Game Time");
            PORT_DIPSETTING(0x00, "90 Sec");
            PORT_DIPSETTING(0x02, "3 Lives");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "None");
            PORT_DIPSETTING(0x0c, "20000");
            PORT_DIPSETTING(0x00, "30000");
            PORT_DIPSETTING(0x04, "50000");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x00, "4");
            PORT_DIPSETTING(0x10, "5");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x40, "Medium");
            PORT_DIPNAME(0x80, 0x80, "Demo Mode");
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* IN4 */
            PORT_ANALOG(0xff, 0x80, IPT_AD_STICK_Y | IPF_CENTER, 70, 50, 95, 159);

            PORT_START();
            /* IN5 */
            PORT_ANALOG(0xff, 0x80, IPT_AD_STICK_X | IPF_REVERSE | IPF_CENTER, 50, 50, 95, 159);

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            64, /* 64 characters */
            1, /* 1 bit per pixel */
            new int[]{0}, /* the bitplanes are packed in one nibble */
            new int[]{4, 5, 6, 7, 12, 13, 14, 15},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 64, 16),
                new GfxDecodeInfo(-1)
            };

    static POKEYinterface pokey_interface = new POKEYinterface(
            4, /* 4 chips */
            1250000, /* 1.25 MHz??? */
            new int[]{25, 25, 25, 25},
            /* The 8 pot handlers */
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            new ReadHandlerPtr[]{null, null, null, null},
            /* The allpot handler */
            new ReadHandlerPtr[]{input_port_4_r, null, null, null}
    );

    static MachineDriver machine_driver_irobot = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        1500000, /* 1.5 Mhz */
                        readmem, writemem, null, null,
                        ignore_interrupt, 0 /* interrupt handled by scanline callbacks */
                ),},
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1,
            irobot_init_machine,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 29 * 8 - 1),
            gfxdecodeinfo,
            64 + 32, 64 + 32, /* 64 for polygons, 32 for text */
            irobot_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            irobot_vh_start,
            irobot_vh_stop,
            irobot_vh_screenrefresh,
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
     * Game driver(s)
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_irobot = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x20000, REGION_CPU1);/* 64k for code + 48K Banked ROM*/
            ROM_LOAD("136029.208", 0x06000, 0x2000, 0xb4d0be59);
            ROM_LOAD("136029.209", 0x08000, 0x4000, 0xf6be3cd0);
            ROM_LOAD("136029.210", 0x0c000, 0x4000, 0xc0eb2133);
            ROM_LOAD("136029.405", 0x10000, 0x4000, 0x9163efe4);
            ROM_LOAD("136029.206", 0x14000, 0x4000, 0xe114a526);
            ROM_LOAD("136029.207", 0x18000, 0x4000, 0xb4556cb0);

            ROM_REGION(0x14000, REGION_CPU2);
            /* mathbox region */
            ROM_LOAD_ODD("ir103.bin", 0x0000, 0x2000, 0x0c83296d);/* ROM data from 0000-bfff */
            ROM_LOAD_EVEN("ir104.bin", 0x0000, 0x2000, 0x0a6cdcca);
            ROM_LOAD_ODD("ir101.bin", 0x4000, 0x4000, 0x62a38c08);
            ROM_LOAD_EVEN("ir102.bin", 0x4000, 0x4000, 0x9d588f22);
            ROM_LOAD("ir111.bin", 0xc000, 0x0400, 0x9fbc9bf3);/* program ROMs from c000-f3ff */
            ROM_LOAD("ir112.bin", 0xc400, 0x0400, 0xb2713214);
            ROM_LOAD("ir113.bin", 0xc800, 0x0400, 0x7875930a);
            ROM_LOAD("ir114.bin", 0xcc00, 0x0400, 0x51d29666);
            ROM_LOAD("ir115.bin", 0xd000, 0x0400, 0x00f9b304);
            ROM_LOAD("ir116.bin", 0xd400, 0x0400, 0x326aba54);
            ROM_LOAD("ir117.bin", 0xd800, 0x0400, 0x98efe8d0);
            ROM_LOAD("ir118.bin", 0xdc00, 0x0400, 0x4a6aa7f9);
            ROM_LOAD("ir119.bin", 0xe000, 0x0400, 0xa5a13ad8);
            ROM_LOAD("ir120.bin", 0xe400, 0x0400, 0x2a083465);
            ROM_LOAD("ir121.bin", 0xe800, 0x0400, 0xadebcb99);
            ROM_LOAD("ir122.bin", 0xec00, 0x0400, 0xda7b6f79);
            ROM_LOAD("ir123.bin", 0xf000, 0x0400, 0x39fff18f);
            /* RAM data from 10000-11fff */
 /* COMRAM from   12000-13fff */

            ROM_REGION(0x800, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("136029.124", 0x0000, 0x0800, 0x848948b6);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("ir125.bin", 0x0000, 0x0020, 0x446335ba);
            ROM_END();
        }
    };

    /*  Colorprom from John's driver. ? */
 /*  ROM_LOAD( "136029.125",    0x0000, 0x0020, 0xc05abf82 );*/
    public static GameDriver driver_irobot = new GameDriver("1983", "irobot", "irobot.java", rom_irobot, null, machine_driver_irobot, input_ports_irobot, init_irobot, ROT0, "Atari", "I, Robot");
}
