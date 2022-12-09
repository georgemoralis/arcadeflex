/*
 * modifications based on 0.58 for vlm5030
 * ported to v0.37b7
 *
 */
package gr.codebb.arcadeflex.v037b7.drivers;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.sound.sn76496H.*;
import static arcadeflex.v036.sound.sn76496.*;
import static arcadeflex.v036.vidhrdw.jailbrek.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.machine.konami.*;
import static arcadeflex.v058.sound.vlm5030.*;
import static arcadeflex.v058.sound.vlm5030H.*;

public class jailbrek {

    static UBytePtr interrupt_control = new UBytePtr();

    public static InitMachineHandlerPtr jailbrek_machine_init = new InitMachineHandlerPtr() {
        public void handler() {
            interrupt_control.write(0, 0);
        }
    };

    public static ReadHandlerPtr jailbrek_speech_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (VLM5030_BSY() != 0 ? 1 : 0);
        }
    };

    public static WriteHandlerPtr jailbrek_speech_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 could be latch direction like in yiear */
            VLM5030_ST((data >> 1) & 1);
            VLM5030_RST((data >> 2) & 1);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x07ff, colorram_r),
                new MemoryReadAddress(0x0800, 0x0fff, videoram_r),
                new MemoryReadAddress(0x1000, 0x10bf, MRA_RAM), /* sprites */
                new MemoryReadAddress(0x10c0, 0x14ff, MRA_RAM), /* ??? */
                new MemoryReadAddress(0x1500, 0x1fff, MRA_RAM), /* work ram */
                new MemoryReadAddress(0x2000, 0x203f, MRA_RAM), /* scroll registers */
                new MemoryReadAddress(0x3000, 0x307f, MRA_NOP), /* related to sprites? */
                new MemoryReadAddress(0x3100, 0x3100, input_port_1_r), /* DSW1 */
                new MemoryReadAddress(0x3200, 0x3200, MRA_NOP), /* ??? */
                new MemoryReadAddress(0x3300, 0x3300, input_port_2_r), /* coins, start */
                new MemoryReadAddress(0x3301, 0x3301, input_port_3_r), /* joy1 */
                new MemoryReadAddress(0x3302, 0x3302, input_port_4_r), /* joy2 */
                new MemoryReadAddress(0x3303, 0x3303, input_port_0_r), /* DSW0 */
                new MemoryReadAddress(0x6000, 0x6000, jailbrek_speech_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x07ff, colorram_w, colorram),
                new MemoryWriteAddress(0x0800, 0x0fff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x1000, 0x10bf, MWA_RAM, spriteram, spriteram_size), /* sprites */
                new MemoryWriteAddress(0x10c0, 0x14ff, MWA_RAM), /* ??? */
                new MemoryWriteAddress(0x1500, 0x1fff, MWA_RAM), /* work ram */
                new MemoryWriteAddress(0x2000, 0x203f, MWA_RAM, jailbrek_scroll_x), /* scroll registers */
                new MemoryWriteAddress(0x2043, 0x2043, MWA_NOP), /* ??? */
                new MemoryWriteAddress(0x2044, 0x2044, MWA_RAM, interrupt_control), /* irq, nmi enable, bit3 = cocktail mode? */
                new MemoryWriteAddress(0x3000, 0x307f, MWA_RAM), /* ??? */
                new MemoryWriteAddress(0x3100, 0x3100, SN76496_0_w), /* SN76496 data write */
                new MemoryWriteAddress(0x3200, 0x3200, MWA_NOP), /* mirror of the previous? */
                new MemoryWriteAddress(0x3300, 0x3300, watchdog_reset_w), /* watchdog */
                new MemoryWriteAddress(0x4000, 0x4000, jailbrek_speech_w), /* speech pins */
                new MemoryWriteAddress(0x5000, 0x5000, VLM5030_data_w), /* speech data */
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortHandlerPtr input_ports_jailbrek = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            /* DSW0  - $3303 */
            PORT_DIPNAME(0x0f, 0x0f, DEF_STR("Coin_A"));
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
            PORT_DIPNAME(0xf0, 0xf0, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x20, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x50, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x40, DEF_STR("3C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("4C_3C"));
            PORT_DIPSETTING(0xf0, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("3C_4C"));
            PORT_DIPSETTING(0x70, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0xe0, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x60, DEF_STR("2C_5C"));
            PORT_DIPSETTING(0xd0, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xb0, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0xa0, DEF_STR("1C_6C"));
            PORT_DIPSETTING(0x90, DEF_STR("1C_7C"));
            PORT_DIPSETTING(0x00, "Invalid?");

            PORT_START();
            /* DSW1  - $3100 */
            PORT_DIPNAME(0x03, 0x01, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x01, "3");
            PORT_DIPSETTING(0x00, "5");
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x08, "30000 70000");
            PORT_DIPSETTING(0x00, "40000 80000");
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* IN0 - $3300 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN1 - $3301 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* IN2 - $3302 */
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            1024, /* 1024 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 2 * 4, 3 * 4, 4 * 4, 5 * 4, 6 * 4, 7 * 4,
                32 * 8 + 0 * 4, 32 * 8 + 1 * 4, 32 * 8 + 2 * 4, 32 * 8 + 3 * 4, 32 * 8 + 4 * 4, 32 * 8 + 5 * 4, 32 * 8 + 6 * 4, 32 * 8 + 7 * 4},
            new int[]{0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, charlayout, 0, 16), /* characters */
                new GfxDecodeInfo(REGION_GFX2, 0, spritelayout, 16 * 16, 16), /* sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    public static InterruptHandlerPtr jb_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if ((interrupt_control.read(0) & 2) != 0) {
                return interrupt.handler();
            }

            return ignore_interrupt.handler();
        }
    };

    public static InterruptHandlerPtr jb_interrupt_nmi = new InterruptHandlerPtr() {
        public int handler() {
            if ((interrupt_control.read(0) & 1) != 0) {
                return nmi_interrupt.handler();
            }

            return ignore_interrupt.handler();
        }
    };

    static SN76496interface sn76496_interface = new SN76496interface(
            1, /* 1 chip */
            new int[]{1500000}, /*  1.5 MHz ? (hand tuned) */
            new int[]{100}
    );

    static VLM5030interface vlm5030_interface = new VLM5030interface(
            3580000, /* master clock */
            100, /* volume       */
            REGION_SOUND1, /* memory region of speech rom */
            0 /* memory size of speech rom */
    );

    static MachineDriver machine_driver_jailbrek = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        3000000, /* 3 Mhz ??? */
                        readmem, writemem, null, null,
                        jb_interrupt, 1,
                        jb_interrupt_nmi, 500 /* ? */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            jailbrek_machine_init,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            32, 512,
            jailbrek_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            jailbrek_vh_start,
            generic_vh_stop,
            jailbrek_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_SN76496,
                        sn76496_interface
                ),
                new MachineSound(
                        SOUND_VLM5030,
                        vlm5030_interface
                )
            }
    );

    /**
     * *************************************************************************
     * Game driver(s)
     * *************************************************************************
     */
    static RomLoadHandlerPtr rom_jailbrek = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);
            /* 64k for code + 64k for decrypted opcodes */
            ROM_LOAD("jailb11d.bin", 0x8000, 0x4000, 0xa0b88dfd);
            ROM_LOAD("jailb9d.bin", 0xc000, 0x4000, 0x444b7d8e);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("jailb4f.bin", 0x00000, 0x4000, 0xe3b7a226);/* characters */
            ROM_LOAD("jailb5f.bin", 0x04000, 0x4000, 0x504f0912);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("jailb3e.bin", 0x00000, 0x4000, 0x0d269524);/* sprites */
            ROM_LOAD("jailb4e.bin", 0x04000, 0x4000, 0x27d4f6f4);
            ROM_LOAD("jailb5e.bin", 0x08000, 0x4000, 0x717485cb);
            ROM_LOAD("jailb3f.bin", 0x0c000, 0x4000, 0xe933086f);

            ROM_REGION(0x0240, REGION_PROMS);
            ROM_LOAD("jailbbl.cl2", 0x0000, 0x0020, 0xf1909605);/* red & green */
            ROM_LOAD("jailbbl.cl1", 0x0020, 0x0020, 0xf70bb122);/* blue */
            ROM_LOAD("jailbbl.bp2", 0x0040, 0x0100, 0xd4fe5c97);/* char lookup */
            ROM_LOAD("jailbbl.bp1", 0x0140, 0x0100, 0x0266c7db);/* sprites lookup */

            ROM_REGION(0x4000, REGION_SOUND1);/* speech rom */
            ROM_LOAD("jailb8c.bin", 0x0000, 0x2000, 0xd91d15e3);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_jailbrek = new InitDriverHandlerPtr() {
        public void handler() {
            konami1_decode();
        }
    };

    public static GameDriver driver_jailbrek = new GameDriver("1986", "jailbrek", "jailbrek.java", rom_jailbrek, null, machine_driver_jailbrek, input_ports_jailbrek, init_jailbrek, ROT0, "Konami", "Jail Break", GAME_NO_COCKTAIL);
}
