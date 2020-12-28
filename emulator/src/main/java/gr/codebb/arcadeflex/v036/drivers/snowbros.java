/**
 * *************************************************************************
 *
 * Snow Brothers
 *
 * driver by Mike Coates
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.snowbros.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.generic.*;

public class snowbros {

    static UBytePtr ram = new UBytePtr();

    public static InterruptPtr snowbros_interrupt = new InterruptPtr() {
        public int handler() {
            return cpu_getiloops() + 2;	/* IRQs 4, 3, and 2 */

        }
    };
    public static ReadHandlerPtr snowbros_input_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int ans = 0xff;

            switch (offset) {
                case 0:
                    ans = (input_port_0_r.handler(offset) << 8) + (input_port_3_r.handler(offset));
                    break;
                case 2:
                    ans = (input_port_1_r.handler(offset) << 8) + (input_port_4_r.handler(offset));
                    break;
                case 4:
                    ans = input_port_2_r.handler(offset) << 8;
                    break;
            }

            return ans;
        }
    };

    /* Sound Routines */
    public static ReadHandlerPtr snowbros_68000_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int ret;

            /* If the sound CPU is running, read the YM3812 status, otherwise
             just make it pass the test */
            if (Machine.sample_rate != 0) {
                ret = soundlatch_r.handler(offset);
            } else {
                ret = 3;
            }

            return ret;
        }
    };

    public static WriteHandlerPtr snowbros_68000_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(offset, data);
            cpu_cause_interrupt(1, Z80_NMI_INT);
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x000000, 0x03ffff, MRA_ROM),
                new MemoryReadAddress(0x100000, 0x103fff, MRA_BANK1),
                new MemoryReadAddress(0x300000, 0x300001, snowbros_68000_sound_r),
                new MemoryReadAddress(0x500000, 0x500005, snowbros_input_r),
                new MemoryReadAddress(0x600000, 0x6001ff, paletteram_word_r),
                new MemoryReadAddress(0x700000, 0x701dff, snowbros_spriteram_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x000000, 0x03ffff, MWA_ROM),
                new MemoryWriteAddress(0x100000, 0x103fff, MWA_BANK1, ram),
                new MemoryWriteAddress(0x200000, 0x200001, watchdog_reset_w),
                new MemoryWriteAddress(0x300000, 0x300001, snowbros_68000_sound_w),
                //	new MemoryWriteAddress( 0x400000, 0x400001, snowbros_interrupt_enable_w ),
                new MemoryWriteAddress(0x600000, 0x6001ff, paletteram_xBBBBBGGGGGRRRRR_word_w, paletteram),
                new MemoryWriteAddress(0x700000, 0x701dff, snowbros_spriteram_w, snowbros_spriteram, videoram_size),
                new MemoryWriteAddress(0x800000, 0x800001, MWA_NOP), /* IRQ 4 acknowledge? */
                new MemoryWriteAddress(0x900000, 0x900001, MWA_NOP), /* IRQ 3 acknowledge? */
                new MemoryWriteAddress(0xa00000, 0xa00001, MWA_NOP), /* IRQ 2 acknowledge? */
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x02, 0x02, YM3812_status_port_0_r),
                new IOReadPort(0x04, 0x04, soundlatch_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x02, 0x02, YM3812_control_port_0_w),
                new IOWritePort(0x03, 0x03, YM3812_write_port_0_w),
                new IOWritePort(0x04, 0x04, soundlatch_w), /* goes back to the main CPU, checked during boot */
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_snowbros = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* 500001 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Must be low or game stops! */
            /* probably VBlank */

            PORT_START(); 	/* 500003 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START(); 	/* 500005 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_TILT);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* DSW 1 */

            PORT_DIPNAME(0x01, 0x01, "Country (Affects Coinage)");
            PORT_DIPSETTING(0x01, "America");
            PORT_DIPSETTING(0x00, "Europe");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_SERVICE(0x04, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            /* Better to implement a coin mode 1-2 stuff later */
            PORT_DIPNAME(0x30, 0x30, "Coin A America/Europe");
            PORT_DIPSETTING(0x10, "2C/1C 3C/1C");
            PORT_DIPSETTING(0x30, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x00, "2C/3C 4C/1C");
            PORT_DIPSETTING(0x20, "1C/2C 2C/1C");
            PORT_DIPNAME(0xc0, 0xc0, "Coin B America/Europe");
            PORT_DIPSETTING(0x40, "2C/1C 1C/4C");
            PORT_DIPSETTING(0xc0, "1C/1C 1C/2C");
            PORT_DIPSETTING(0x00, "2C/3C 1C/6C");
            PORT_DIPSETTING(0x80, "1C/2C 1C/3C");

            PORT_START();  /* DSW 2 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x02, "Easy");
            PORT_DIPSETTING(0x03, "Normal");
            PORT_DIPSETTING(0x01, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "100k and every 200k ");
            PORT_DIPSETTING(0x0c, "100k Only");
            PORT_DIPSETTING(0x08, "200k Only");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Lives"));
            PORT_DIPSETTING(0x20, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPSETTING(0x30, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_BITX(0x40, 0x40, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout tilelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            4096, /* 4096 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3},
            new int[]{
                0, 4, 8, 12, 16, 20, 24, 28,
                8 * 32 + 0, 8 * 32 + 4, 8 * 32 + 8, 8 * 32 + 12, 8 * 32 + 16, 8 * 32 + 20, 8 * 32 + 24, 8 * 32 + 28,},
            new int[]{
                0 * 32, 1 * 32, 2 * 32, 3 * 32, 4 * 32, 5 * 32, 6 * 32, 7 * 32,
                16 * 32, 17 * 32, 18 * 32, 19 * 32, 20 * 32, 21 * 32, 22 * 32, 23 * 32
            },
            128 * 8
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, tilelayout, 0, 16),
                new GfxDecodeInfo(-1) /* end of array */};

    /* handler called by the 3812 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            cpu_set_irq_line(1, 0, linestate);
        }
    };

    static YM3812interface ym3812_interface = new YM3812interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz ? (hand tuned) */
            new int[]{100}, /* volume */
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static MachineDriver machine_driver_snowbros = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        8000000, /* 8 Mhz ????? */
                        readmem, writemem, null, null,
                        snowbros_interrupt, 3
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3600000, /* 3.6 Mhz ??? */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 0 /* IRQs are caused by the YM3812 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 256,
            null,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY | VIDEO_MODIFIES_PALETTE,
            null,
            generic_vh_start,
            generic_vh_stop,
            snowbros_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
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
    static RomLoadPtr rom_snowbros = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("sn6.bin", 0x00000, 0x20000, 0x4899ddcf);
            ROM_LOAD_ODD("sn5.bin", 0x00000, 0x20000, 0xad310d3f);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for z80 sound code */

            ROM_LOAD("snowbros.4", 0x0000, 0x8000, 0xe6eab4e4);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ch0", 0x00000, 0x20000, 0x36d84dfe);
            ROM_LOAD("ch1", 0x20000, 0x20000, 0x76347256);
            ROM_LOAD("ch2", 0x40000, 0x20000, 0xfdaa634c);
            ROM_LOAD("ch3", 0x60000, 0x20000, 0x34024aef);
            ROM_END();
        }
    };

    static RomLoadPtr rom_snowbroa = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("snowbros.3a", 0x00000, 0x20000, 0x10cb37e1);
            ROM_LOAD_ODD("snowbros.2a", 0x00000, 0x20000, 0xab91cc1e);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for z80 sound code */

            ROM_LOAD("snowbros.4", 0x0000, 0x8000, 0xe6eab4e4);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ch0", 0x00000, 0x20000, 0x36d84dfe);
            ROM_LOAD("ch1", 0x20000, 0x20000, 0x76347256);
            ROM_LOAD("ch2", 0x40000, 0x20000, 0xfdaa634c);
            ROM_LOAD("ch3", 0x60000, 0x20000, 0x34024aef);
            ROM_END();
        }
    };

    static RomLoadPtr rom_snowbrob = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("sbros3-a", 0x00000, 0x20000, 0x301627d6);
            ROM_LOAD_ODD("sbros2-a", 0x00000, 0x20000, 0xf6689f41);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for z80 sound code */

            ROM_LOAD("snowbros.4", 0x0000, 0x8000, 0xe6eab4e4);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ch0", 0x00000, 0x20000, 0x36d84dfe);
            ROM_LOAD("ch1", 0x20000, 0x20000, 0x76347256);
            ROM_LOAD("ch2", 0x40000, 0x20000, 0xfdaa634c);
            ROM_LOAD("ch3", 0x60000, 0x20000, 0x34024aef);
            ROM_END();
        }
    };

    static RomLoadPtr rom_snowbroj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x40000, REGION_CPU1);/* 6*64k for 68000 code */

            ROM_LOAD_EVEN("snowbros.3", 0x00000, 0x20000, 0x3f504f9e);
            ROM_LOAD_ODD("snowbros.2", 0x00000, 0x20000, 0x854b02bc);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for z80 sound code */

            ROM_LOAD("snowbros.4", 0x0000, 0x8000, 0xe6eab4e4);

            ROM_REGION(0x80000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            /* The gfx ROM (snowbros.1) was bad, I'm using the ones from the other sets. */
            ROM_LOAD("ch0", 0x00000, 0x20000, 0x36d84dfe);
            ROM_LOAD("ch1", 0x20000, 0x20000, 0x76347256);
            ROM_LOAD("ch2", 0x40000, 0x20000, 0xfdaa634c);
            ROM_LOAD("ch3", 0x60000, 0x20000, 0x34024aef);
            ROM_END();
        }
    };

    public static GameDriver driver_snowbros = new GameDriver("1990", "snowbros", "snowbros.java", rom_snowbros, null, machine_driver_snowbros, input_ports_snowbros, null, ROT0, "Toaplan (Romstar license)", "Snow Bros. - Nick  Tom (set 1)");
    public static GameDriver driver_snowbroa = new GameDriver("1990", "snowbroa", "snowbros.java", rom_snowbroa, driver_snowbros, machine_driver_snowbros, input_ports_snowbros, null, ROT0, "Toaplan (Romstar license)", "Snow Bros. - Nick  Tom (set 2)");
    public static GameDriver driver_snowbrob = new GameDriver("1990", "snowbrob", "snowbros.java", rom_snowbrob, driver_snowbros, machine_driver_snowbros, input_ports_snowbros, null, ROT0, "Toaplan (Romstar license)", "Snow Bros. - Nick  Tom (set 3)");
    public static GameDriver driver_snowbroj = new GameDriver("1990", "snowbroj", "snowbros.java", rom_snowbroj, driver_snowbros, machine_driver_snowbros, input_ports_snowbros, null, ROT0, "Toaplan (Romstar license)", "Snow Bros. - Nick  Tom (Japan)");
}
