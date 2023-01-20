/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
/**
 * Changelog
 * =========
 * 20/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.drivers;

//cpu imports
import static arcadeflex.v036.cpu.konami.konami.*;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
//mame imports
import static arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.cpuintrf.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrfH.*;
//sound imports
import static arcadeflex.v036.sound.upd7759.*;
import static arcadeflex.v036.sound.upd7759H.*;
import static arcadeflex.v036.sound._2151intfH.*;
import static arcadeflex.v036.sound._2151intf.*;
import static arcadeflex.v036.sound.mixerH.*;
//vidhrdw imports
import static arcadeflex.v036.vidhrdw._88games.*;
import static arcadeflex.v036.vidhrdw.konamiic.*;
//TODO
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class _88games {

    public static konami_cpu_setlines_callbackPtr k88games_banking = new konami_cpu_setlines_callbackPtr() {
        public void handler(int lines) {
            UBytePtr RAM = memory_region(REGION_CPU1);
            int offs;

            if (errorlog != null) {
                fprintf(errorlog, "%04x: bank select %02x\n", cpu_get_pc(), lines);
            }

            /* bits 0-2 select ROM bank for 0000-1fff */
 /* bit 3: when 1, palette RAM at 1000-1fff */
 /* bit 4: when 0, 051316 RAM at 3800-3fff; when 1, work RAM at 2000-3fff (NVRAM 3370-37ff) */
            offs = 0x10000 + (lines & 0x07) * 0x2000;

            for (int i = 0; i < 0x1000; i++) {
                RAM.write(RAM.offset + i, RAM.read(offs + i));//memcpy(RAM,&RAM[offs],0x1000);
            }
            if ((lines & 0x08) != 0) {
                if (paletteram.read() != RAM.read(0x1000)) {
                    int base = paletteram.offset;
                    for (int i = 0; i < 0x1000; i++) {
                        RAM.write(i + 0x1000, paletteram.read(i));//memcpy(&RAM[0x1000],paletteram,0x1000);
                    }
                    paletteram = new UBytePtr(RAM, 0x1000);//paletteram = &RAM[0x1000];
                }
            } else {
                if (paletteram.read() != RAM.read(0x20000)) {
                    int base = paletteram.offset;
                    for (int i = 0; i < 0x1000; i++) {
                        RAM.write(i + 0x20000, paletteram.read(base + i));//memcpy(&RAM[0x20000],paletteram,0x1000);
                    }
                    paletteram = new UBytePtr(RAM, 0x20000);//paletteram = &RAM[0x20000];
                }
                for (int i = 0; i < 0x1000; i++) {
                    RAM.write(i + 0x1000, RAM.read(i + offs + 0x1000));//memcpy(&RAM[0x1000],&RAM[offs+0x1000],0x1000);
                }
            }
            videobank = lines & 0x10;

            /* bit 5 = enable char ROM reading through the video RAM */
            K052109_set_RMRD_line((lines & 0x20) != 0 ? ASSERT_LINE : CLEAR_LINE);

            /* bit 6 is unknown, 1 most of the time */
 /* bit 7 controls layer priority */
            k88games_priority = lines & 0x80;
        }
    };

    public static InitMachineHandlerPtr k88games_init_machine = new InitMachineHandlerPtr() {
        public void handler() {
            konami_cpu_setlines_callback = k88games_banking;
            paletteram = new UBytePtr(memory_region(REGION_CPU1), 0x20000);
        }
    };
    static UBytePtr ram = new UBytePtr();
    static int videobank;
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
                    //memset(nvram,0,nvram_size);
                    for (int i = 0; i < nvram_size[0]; i++) {
                        nvram.write(i, 0);
                    }
                }
            }
        }
    };

    public static InterruptHandlerPtr k88games_interrupt = new InterruptHandlerPtr() {
        public int handler() {
            if (K052109_is_IRQ_enabled() != 0) {
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    static int zoomreadroms;

    public static ReadHandlerPtr bankedram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if (videobank != 0) {
                return ram.read(offset);
            } else {
                if (zoomreadroms != 0) {
                    return K051316_rom_0_r.handler(offset);
                } else {
                    return K051316_0_r.handler(offset);
                }
            }
        }
    };

    public static WriteHandlerPtr bankedram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (videobank != 0) {
                ram.write(offset, data);
            } else {
                K051316_0_w.handler(offset, data);
            }
        }
    };

    public static WriteHandlerPtr k88games_5f84_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bits 0/1 coin counters */
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);

            /* bit 2 enables ROM reading from the 051316 */
 /* also 5fce == 2 read roms, == 3 read ram */
            zoomreadroms = data & 0x04;

            /*if (data & 0xf8)
		{
			char buf[40];
			sprintf(buf,"5f84 = %02x",data);
			usrintf_showmessage(buf);
		}*/
        }
    };

    public static WriteHandlerPtr k88games_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(1, 0xff);
        }
    };

    /* handle fake button for speed cheat */
    static int cheat = 0;
    static int bits[] = {0xee, 0xff, 0xbb, 0xaa};
    public static ReadHandlerPtr cheat_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = readinputport(1);

            if ((readinputport(0) & 0x08) == 0) {
                res |= 0x55;
                res &= bits[cheat];
                cheat = (++cheat) % 4;
            }
            return res;
        }
    };

    static int speech_chip;
    static int invalid_code;
    static int total_samples[] = {0x39, 0x15};

    public static WriteHandlerPtr speech_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int reset = ((data >> 1) & 1);
            int start = (~data) & 1;

            speech_chip = (data & 4) != 0 ? 1 : 0;

            UPD7759_reset_w.handler(speech_chip, reset);

            if (invalid_code == 0) {
                UPD7759_start_w.handler(speech_chip, start);
            }
        }
    };

    public static WriteHandlerPtr speech_msg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UPD7759_message_w.handler(speech_chip, data);
            invalid_code = (data == total_samples[speech_chip]) ? 1 : 0;
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM), /* banked ROM + palette RAM */
                new MemoryReadAddress(0x2000, 0x37ff, MRA_RAM),
                new MemoryReadAddress(0x3800, 0x3fff, bankedram_r),
                new MemoryReadAddress(0x5f94, 0x5f94, input_port_0_r),
                //	new MemoryReadAddress( 0x5f95, 0x5f95, input_port_1_r ),
                new MemoryReadAddress(0x5f95, 0x5f95, cheat_r), /* P1 IO and handle fake button for cheating */
                new MemoryReadAddress(0x5f96, 0x5f96, input_port_2_r),
                new MemoryReadAddress(0x5f97, 0x5f97, input_port_3_r),
                new MemoryReadAddress(0x5f9b, 0x5f9b, input_port_4_r),
                new MemoryReadAddress(0x4000, 0x7fff, K052109_051960_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0fff, MWA_RAM), /* banked ROM */
                new MemoryWriteAddress(0x1000, 0x1fff, paletteram_xBBBBBGGGGGRRRRR_swap_w, paletteram), /* banked ROM + palette RAM */
                new MemoryWriteAddress(0x2000, 0x2fff, MWA_RAM),
                new MemoryWriteAddress(0x3000, 0x37ff, MWA_RAM, nvram, nvram_size),
                new MemoryWriteAddress(0x3800, 0x3fff, bankedram_w, ram),
                new MemoryWriteAddress(0x5f84, 0x5f84, k88games_5f84_w),
                new MemoryWriteAddress(0x5f88, 0x5f88, watchdog_reset_w),
                new MemoryWriteAddress(0x5f8c, 0x5f8c, soundlatch_w),
                new MemoryWriteAddress(0x5f90, 0x5f90, k88games_sh_irqtrigger_w),
                new MemoryWriteAddress(0x5fc0, 0x5fcf, K051316_ctrl_0_w),
                new MemoryWriteAddress(0x4000, 0x7fff, K052109_051960_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x87ff, MRA_RAM),
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(0xc001, 0xc001, YM2151_status_port_0_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x87ff, MWA_RAM),
                new MemoryWriteAddress(0x9000, 0x9000, speech_msg_w),
                new MemoryWriteAddress(0xc000, 0xc000, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xc001, 0xc001, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xe000, 0xe000, speech_control_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * Input Ports
     *
     **************************************************************************
     */
    static InputPortHandlerPtr input_ports_88games = new InputPortHandlerPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            //	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
            /* Fake button to press buttons 1 and 3 impossibly fast. Handle via konami_IN1_r */
            PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_CHEAT | IPF_PLAYER1, "Run Like Hell Cheat", IP_KEY_DEFAULT, IP_JOY_DEFAULT);
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x20, "World Records");
            PORT_DIPSETTING(0x20, "Don't Erase");
            PORT_DIPSETTING(0x00, "Erase on Reset");
            PORT_SERVICE(0x40, IP_ACTIVE_LOW);
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START3);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_START4);

            PORT_START();
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
            //	PORT_DIPSETTING(    0x00, "Disabled" );

            PORT_START();
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x01, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x10, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz */
            new int[]{YM3012_VOL(75, MIXER_PAN_LEFT, 75, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static UPD7759_interface upd7759_interface = new UPD7759_interface(
            2, /* number of chips */
            UPD7759_STANDARD_CLOCK,
            new int[]{30, 30}, /* volume */
            new int[]{REGION_SOUND1, REGION_SOUND2}, /* memory region */
            UPD7759_STANDALONE_MODE, /* chip mode */
            new irqcallbackPtr[]{null}
    );

    static MachineDriver machine_driver_88games = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_KONAMI,
                        3000000, /* ? */
                        readmem, writemem, null, null,
                        k88games_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            k88games_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(13 * 8, (64 - 13) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            k88games_vh_start,
            k88games_vh_stop,
            k88games_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_UPD7759,
                        upd7759_interface
                )
            },
            nvram_handler
    );

    /**
     * *************************************************************************
     *
     * Game ROMs
     *
     **************************************************************************
     */
    static RomLoadHandlerPtr rom_88games = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x21000, REGION_CPU1);/* code + banked roms + space for banked ram */
            ROM_LOAD("861m01.k18", 0x08000, 0x08000, 0x4a4e2959);
            ROM_LOAD("861m02.k16", 0x10000, 0x10000, 0xe19f15f6);

            ROM_REGION(0x10000, REGION_CPU2);/* Z80 code */
            ROM_LOAD("861d01.d9", 0x00000, 0x08000, 0x0ff1dec0);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a08.a", 0x000000, 0x10000, 0x77a00dd6);/* characters */
            ROM_LOAD_GFX_ODD("861a08.c", 0x000000, 0x10000, 0xb422edfc);
            ROM_LOAD_GFX_EVEN("861a08.b", 0x020000, 0x10000, 0x28a8304f);
            ROM_LOAD_GFX_ODD("861a08.d", 0x020000, 0x10000, 0xe01a3802);
            ROM_LOAD_GFX_EVEN("861a09.a", 0x040000, 0x10000, 0xdf8917b6);
            ROM_LOAD_GFX_ODD("861a09.c", 0x040000, 0x10000, 0xf577b88f);
            ROM_LOAD_GFX_EVEN("861a09.b", 0x060000, 0x10000, 0x4917158d);
            ROM_LOAD_GFX_ODD("861a09.d", 0x060000, 0x10000, 0x2bb3282c);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a05.a", 0x000000, 0x10000, 0xcedc19d0);/* sprites */
            ROM_LOAD_GFX_ODD("861a05.e", 0x000000, 0x10000, 0x725af3fc);
            ROM_LOAD_GFX_EVEN("861a05.b", 0x020000, 0x10000, 0xdb2a8808);
            ROM_LOAD_GFX_ODD("861a05.f", 0x020000, 0x10000, 0x32d830ca);
            ROM_LOAD_GFX_EVEN("861a05.c", 0x040000, 0x10000, 0xcf03c449);
            ROM_LOAD_GFX_ODD("861a05.g", 0x040000, 0x10000, 0xfd51c4ea);
            ROM_LOAD_GFX_EVEN("861a05.d", 0x060000, 0x10000, 0x97d78c77);
            ROM_LOAD_GFX_ODD("861a05.h", 0x060000, 0x10000, 0x60d0c8a5);
            ROM_LOAD_GFX_EVEN("861a06.a", 0x080000, 0x10000, 0x85e2e30e);
            ROM_LOAD_GFX_ODD("861a06.e", 0x080000, 0x10000, 0x6f96651c);
            ROM_LOAD_GFX_EVEN("861a06.b", 0x0a0000, 0x10000, 0xce17eaf0);
            ROM_LOAD_GFX_ODD("861a06.f", 0x0a0000, 0x10000, 0x88310bf3);
            ROM_LOAD_GFX_EVEN("861a06.c", 0x0c0000, 0x10000, 0xa568b34e);
            ROM_LOAD_GFX_ODD("861a06.g", 0x0c0000, 0x10000, 0x4a55beb3);
            ROM_LOAD_GFX_EVEN("861a06.d", 0x0e0000, 0x10000, 0xbc70ab39);
            ROM_LOAD_GFX_ODD("861a06.h", 0x0e0000, 0x10000, 0xd906b79b);

            ROM_REGION(0x040000, REGION_GFX3);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD("861a04.a", 0x000000, 0x10000, 0x092a8b15);/* zoom/rotate */
            ROM_LOAD("861a04.b", 0x010000, 0x10000, 0x75744b56);
            ROM_LOAD("861a04.c", 0x020000, 0x10000, 0xa00021c5);
            ROM_LOAD("861a04.d", 0x030000, 0x10000, 0xd208304c);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("861.g3", 0x0000, 0x0100, 0x429785db);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples for UPD7759 #0 */
            ROM_LOAD("861a07.a", 0x000000, 0x10000, 0x5d035d69);
            ROM_LOAD("861a07.b", 0x010000, 0x10000, 0x6337dd91);

            ROM_REGION(0x20000, REGION_SOUND2);/* samples for UPD7759 #1 */
            ROM_LOAD("861a07.c", 0x000000, 0x10000, 0x5067a38b);
            ROM_LOAD("861a07.d", 0x010000, 0x10000, 0x86731451);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_konami88 = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x21000, REGION_CPU1);/* code + banked roms + space for banked ram */
            ROM_LOAD("861.e03", 0x08000, 0x08000, 0x55979bd9);
            ROM_LOAD("861.e02", 0x10000, 0x10000, 0x5b7e98a6);

            ROM_REGION(0x10000, REGION_CPU2);/* Z80 code */
            ROM_LOAD("861d01.d9", 0x00000, 0x08000, 0x0ff1dec0);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a08.a", 0x000000, 0x10000, 0x77a00dd6);/* characters */
            ROM_LOAD_GFX_ODD("861a08.c", 0x000000, 0x10000, 0xb422edfc);
            ROM_LOAD_GFX_EVEN("861a08.b", 0x020000, 0x10000, 0x28a8304f);
            ROM_LOAD_GFX_ODD("861a08.d", 0x020000, 0x10000, 0xe01a3802);
            ROM_LOAD_GFX_EVEN("861a09.a", 0x040000, 0x10000, 0xdf8917b6);
            ROM_LOAD_GFX_ODD("861a09.c", 0x040000, 0x10000, 0xf577b88f);
            ROM_LOAD_GFX_EVEN("861a09.b", 0x060000, 0x10000, 0x4917158d);
            ROM_LOAD_GFX_ODD("861a09.d", 0x060000, 0x10000, 0x2bb3282c);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a05.a", 0x000000, 0x10000, 0xcedc19d0);/* sprites */
            ROM_LOAD_GFX_ODD("861a05.e", 0x000000, 0x10000, 0x725af3fc);
            ROM_LOAD_GFX_EVEN("861a05.b", 0x020000, 0x10000, 0xdb2a8808);
            ROM_LOAD_GFX_ODD("861a05.f", 0x020000, 0x10000, 0x32d830ca);
            ROM_LOAD_GFX_EVEN("861a05.c", 0x040000, 0x10000, 0xcf03c449);
            ROM_LOAD_GFX_ODD("861a05.g", 0x040000, 0x10000, 0xfd51c4ea);
            ROM_LOAD_GFX_EVEN("861a05.d", 0x060000, 0x10000, 0x97d78c77);
            ROM_LOAD_GFX_ODD("861a05.h", 0x060000, 0x10000, 0x60d0c8a5);
            ROM_LOAD_GFX_EVEN("861a06.a", 0x080000, 0x10000, 0x85e2e30e);
            ROM_LOAD_GFX_ODD("861a06.e", 0x080000, 0x10000, 0x6f96651c);
            ROM_LOAD_GFX_EVEN("861a06.b", 0x0a0000, 0x10000, 0xce17eaf0);
            ROM_LOAD_GFX_ODD("861a06.f", 0x0a0000, 0x10000, 0x88310bf3);
            ROM_LOAD_GFX_EVEN("861a06.c", 0x0c0000, 0x10000, 0xa568b34e);
            ROM_LOAD_GFX_ODD("861a06.g", 0x0c0000, 0x10000, 0x4a55beb3);
            ROM_LOAD_GFX_EVEN("861a06.d", 0x0e0000, 0x10000, 0xbc70ab39);
            ROM_LOAD_GFX_ODD("861a06.h", 0x0e0000, 0x10000, 0xd906b79b);

            ROM_REGION(0x040000, REGION_GFX3);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD("861a04.a", 0x000000, 0x10000, 0x092a8b15);/* zoom/rotate */
            ROM_LOAD("861a04.b", 0x010000, 0x10000, 0x75744b56);
            ROM_LOAD("861a04.c", 0x020000, 0x10000, 0xa00021c5);
            ROM_LOAD("861a04.d", 0x030000, 0x10000, 0xd208304c);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("861.g3", 0x0000, 0x0100, 0x429785db);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples for UPD7759 #0 */
            ROM_LOAD("861a07.a", 0x000000, 0x10000, 0x5d035d69);
            ROM_LOAD("861a07.b", 0x010000, 0x10000, 0x6337dd91);

            ROM_REGION(0x20000, REGION_SOUND2);/* samples for UPD7759 #1 */
            ROM_LOAD("861a07.c", 0x000000, 0x10000, 0x5067a38b);
            ROM_LOAD("861a07.d", 0x010000, 0x10000, 0x86731451);
            ROM_END();
        }
    };

    static RomLoadHandlerPtr rom_hypsptsp = new RomLoadHandlerPtr() {
        public void handler() {
            ROM_REGION(0x21000, REGION_CPU1);/* code + banked roms + space for banked ram */
            ROM_LOAD("861f03.k18", 0x08000, 0x08000, 0x8c61aebd);
            ROM_LOAD("861f02.k16", 0x10000, 0x10000, 0xd2460c28);

            ROM_REGION(0x10000, REGION_CPU2);/* Z80 code */
            ROM_LOAD("861d01.d9", 0x00000, 0x08000, 0x0ff1dec0);

            ROM_REGION(0x080000, REGION_GFX1);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a08.a", 0x000000, 0x10000, 0x77a00dd6);/* characters */
            ROM_LOAD_GFX_ODD("861a08.c", 0x000000, 0x10000, 0xb422edfc);
            ROM_LOAD_GFX_EVEN("861a08.b", 0x020000, 0x10000, 0x28a8304f);
            ROM_LOAD_GFX_ODD("861a08.d", 0x020000, 0x10000, 0xe01a3802);
            ROM_LOAD_GFX_EVEN("861a09.a", 0x040000, 0x10000, 0xdf8917b6);
            ROM_LOAD_GFX_ODD("861a09.c", 0x040000, 0x10000, 0xf577b88f);
            ROM_LOAD_GFX_EVEN("861a09.b", 0x060000, 0x10000, 0x4917158d);
            ROM_LOAD_GFX_ODD("861a09.d", 0x060000, 0x10000, 0x2bb3282c);

            ROM_REGION(0x100000, REGION_GFX2);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD_GFX_EVEN("861a05.a", 0x000000, 0x10000, 0xcedc19d0);/* sprites */
            ROM_LOAD_GFX_ODD("861a05.e", 0x000000, 0x10000, 0x725af3fc);
            ROM_LOAD_GFX_EVEN("861a05.b", 0x020000, 0x10000, 0xdb2a8808);
            ROM_LOAD_GFX_ODD("861a05.f", 0x020000, 0x10000, 0x32d830ca);
            ROM_LOAD_GFX_EVEN("861a05.c", 0x040000, 0x10000, 0xcf03c449);
            ROM_LOAD_GFX_ODD("861a05.g", 0x040000, 0x10000, 0xfd51c4ea);
            ROM_LOAD_GFX_EVEN("861a05.d", 0x060000, 0x10000, 0x97d78c77);
            ROM_LOAD_GFX_ODD("861a05.h", 0x060000, 0x10000, 0x60d0c8a5);
            ROM_LOAD_GFX_EVEN("861a06.a", 0x080000, 0x10000, 0x85e2e30e);
            ROM_LOAD_GFX_ODD("861a06.e", 0x080000, 0x10000, 0x6f96651c);
            ROM_LOAD_GFX_EVEN("861a06.b", 0x0a0000, 0x10000, 0xce17eaf0);
            ROM_LOAD_GFX_ODD("861a06.f", 0x0a0000, 0x10000, 0x88310bf3);
            ROM_LOAD_GFX_EVEN("861a06.c", 0x0c0000, 0x10000, 0xa568b34e);
            ROM_LOAD_GFX_ODD("861a06.g", 0x0c0000, 0x10000, 0x4a55beb3);
            ROM_LOAD_GFX_EVEN("861a06.d", 0x0e0000, 0x10000, 0xbc70ab39);
            ROM_LOAD_GFX_ODD("861a06.h", 0x0e0000, 0x10000, 0xd906b79b);

            ROM_REGION(0x040000, REGION_GFX3);/* graphics ( dont dispose as the program can read them ) */
            ROM_LOAD("861a04.a", 0x000000, 0x10000, 0x092a8b15);/* zoom/rotate */
            ROM_LOAD("861a04.b", 0x010000, 0x10000, 0x75744b56);
            ROM_LOAD("861a04.c", 0x020000, 0x10000, 0xa00021c5);
            ROM_LOAD("861a04.d", 0x030000, 0x10000, 0xd208304c);

            ROM_REGION(0x0100, REGION_PROMS);
            ROM_LOAD("861.g3", 0x0000, 0x0100, 0x429785db);/* priority encoder (not used) */

            ROM_REGION(0x20000, REGION_SOUND1);/* samples for UPD7759 #0 */
            ROM_LOAD("861a07.a", 0x000000, 0x10000, 0x5d035d69);
            ROM_LOAD("861a07.b", 0x010000, 0x10000, 0x6337dd91);

            ROM_REGION(0x20000, REGION_SOUND2);/* samples for UPD7759 #1 */
            ROM_LOAD("861a07.c", 0x000000, 0x10000, 0x5067a38b);
            ROM_LOAD("861a07.d", 0x010000, 0x10000, 0x86731451);
            ROM_END();
        }
    };

    public static InitDriverHandlerPtr init_88games = new InitDriverHandlerPtr() {
        public void handler() {
            konami_rom_deinterleave_2(REGION_GFX1);
            konami_rom_deinterleave_2(REGION_GFX2);
        }
    };

    public static GameDriver driver_88games = new GameDriver("1988", "88games", "_88games.java", rom_88games, null, machine_driver_88games, input_ports_88games, init_88games, ROT0, "Konami", "'88 Games");
    public static GameDriver driver_konami88 = new GameDriver("1988", "konami88", "_88games.java", rom_konami88, driver_88games, machine_driver_88games, input_ports_88games, init_88games, ROT0, "Konami", "Konami '88");
    public static GameDriver driver_hypsptsp = new GameDriver("1988", "hypsptsp", "_88games.java", rom_hypsptsp, driver_88games, machine_driver_88games, input_ports_88games, init_88games, ROT0, "Konami", "Hyper Sports Special (Japan)");
}
