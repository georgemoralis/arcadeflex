/**
 * *************************************************************************
 *
 * Vendetta (GX081) (c) 1991 Konami
 *
 * Preliminary driver by: Ernesto Corvi someone@secureshell.com
 *
 * Notes: - collision detection is handled by a protection chip. Its emulation
 * might not be 100% accurate.
 *
 **************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import arcadeflex.v036.generic.funcPtr.TimerCallbackHandlerPtr;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptport.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.inptportH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import arcadeflex.v036.mame.sndintrfH.MachineSound;
import static arcadeflex.v036.mame.sndintrfH.SOUND_YM2151;
import static gr.codebb.arcadeflex.v036.vidhrdw.konamiic.*;
import static gr.codebb.arcadeflex.v036.sound._2151intf.*;
import static gr.codebb.arcadeflex.v036.sound._2151intfH.*;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K053247.*;
import static arcadeflex.v036.mame.timer.*;
import static arcadeflex.v036.mame.timerH.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.vendetta.*;
import static gr.codebb.arcadeflex.v036.cpu.konami.konamiH.*;
import static gr.codebb.arcadeflex.v036.machine.eeprom.*;
import static gr.codebb.arcadeflex.v036.machine.eepromH.*;
import static gr.codebb.arcadeflex.v036.cpu.konami.konami.*;
import static arcadeflex.v036.mame.sndintrfH.SOUND_K053260;
import static gr.codebb.arcadeflex.v036.vidhrdw.konami.K054000.*;
import static gr.codebb.arcadeflex.v036.sound.k053260.*;
import static gr.codebb.arcadeflex.v036.sound.k053260H.*;

public class vendetta {

    public static konami_cpu_setlines_callbackPtr vendetta_banking = new konami_cpu_setlines_callbackPtr() {
        public void handler(int lines) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            if (lines >= 0x1c) {
                if (errorlog != null) {
                    fprintf(errorlog, "PC = %04x : Unknown bank selected %02x\n", cpu_get_pc(), lines);
                }
            } else {
                cpu_setbank(1, new UBytePtr(RAM, 0x10000 + (lines * 0x2000)));
            }
        }
    };

    public static InitMachinePtr vendetta_init_machine = new InitMachinePtr() {
        public void handler() {
            konami_cpu_setlines_callback = vendetta_banking;

            paletteram = new UBytePtr(memory_region(REGION_CPU1), 0x48000);
            irq_enabled = 0;

            /* init banks */
            cpu_setbank(1, new UBytePtr(memory_region(REGION_CPU1), 0x10000));
            vendetta_video_banking(0);
        }
    };
    /**
     * *************************************************************************
     *
     * EEPROM
     *
     **************************************************************************
     */

    static int init_eeprom_count;

    static EEPROM_interface eeprom_interface = new EEPROM_interface(
            7, /* address bits */
            8, /* data bits */
            "011000", /*  read command */
            "011100", /* write command */
            null, /* erase command */
            "0100000000000",/* lock command */
            "0100110000000" /* unlock command */
    );

    public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            if (read_or_write != 0) {
                EEPROM_save(file);
            } else {
                EEPROM_init(eeprom_interface);

                if (file != null) {
                    init_eeprom_count = 0;
                    EEPROM_load(file);
                } else {
                    init_eeprom_count = 1000;
                }
            }
        }
    };

    public static ReadHandlerPtr vendetta_eeprom_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int res;

            res = EEPROM_read_bit();

            res |= 0x02;//konami_eeprom_ack() << 5; /* add the ack */

            res |= readinputport(3) & 0x0c;
            /* test switch */

            if (init_eeprom_count != 0) {
                init_eeprom_count--;
                res &= 0xfb;
            }
            return res;
        }
    };

    static int irq_enabled;

    public static WriteHandlerPtr vendetta_eeprom_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 0 - VOC0 - Video banking related */
 /* bit 1 - VOC1 - Video banking related */
 /* bit 2 - MSCHNG - Mono Sound select (Amp) */
 /* bit 3 - EEPCS - Eeprom CS */
 /* bit 4 - EEPCLK - Eeprom CLK */
 /* bit 5 - EEPDI - Eeprom data */
 /* bit 6 - IRQ enable */
 /* bit 7 - Unused */

            if (data == 0xff) /* this is a bug in the eeprom write code */ {
                return;
            }

            /* EEPROM */
            EEPROM_write_bit(data & 0x20);
            EEPROM_set_clock_line((data & 0x10) != 0 ? ASSERT_LINE : CLEAR_LINE);
            EEPROM_set_cs_line((data & 0x08) != 0 ? CLEAR_LINE : ASSERT_LINE);

            irq_enabled = (data >> 6) & 1;

            vendetta_video_banking(data & 1);
        }
    };

    /**
     * *****************************************
     */
    public static ReadHandlerPtr vendetta_K052109_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return K052109_r.handler(offset + 0x2000);
        }
    };
    public static WriteHandlerPtr vendetta_K052109_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            K052109_w.handler(offset + 0x2000, data);
        }
    };

    static void vendetta_video_banking(int select) {
        if ((select & 1) != 0) {
            cpu_setbankhandler_r(2, paletteram_r);
            cpu_setbankhandler_w(2, paletteram_xBBBBBGGGGGRRRRR_swap_w);
            cpu_setbankhandler_r(3, K053247_r);
            cpu_setbankhandler_w(3, K053247_w);
        } else {
            cpu_setbankhandler_r(2, vendetta_K052109_r);
            cpu_setbankhandler_w(2, vendetta_K052109_w);
            cpu_setbankhandler_r(3, K052109_r);
            cpu_setbankhandler_w(3, K052109_w);
        }
    }

    public static WriteHandlerPtr vendetta_5fe0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            //char baf[40];
            //sprintf(baf,"5fe0 = %02x",data);
            //usrintf_showmessage(baf);

            /* bit 0,1 coin counters */
            coin_counter_w.handler(0, data & 0x01);
            coin_counter_w.handler(1, data & 0x02);

            /* bit 2 = BRAMBK ?? */
 /* bit 3 = enable char ROM reading through the video RAM */
            K052109_set_RMRD_line((data & 0x08) != 0 ? ASSERT_LINE : CLEAR_LINE);

            /* bit 4 = INIT ?? */
 /* bit 5 = enable sprite ROM reading */
            K053246_set_OBJCHA_line((data & 0x20) != 0 ? ASSERT_LINE : CLEAR_LINE);
        }
    };

    public static ReadHandlerPtr speedup_r = new ReadHandlerPtr() {
        public int handler(int offs) {
            UBytePtr RAM = memory_region(REGION_CPU1);

            int data = (RAM.read(0x28d2) << 8) | RAM.read(0x28d3);

            if (data < memory_region_length(REGION_CPU1)) {
                data = (RAM.read(data) << 8) | RAM.read(data + 1);

                if (data == 0xffff) {
                    cpu_spinuntil_int();
                }
            }

            return RAM.read(0x28d2);
        }
    };
    public static TimerCallbackHandlerPtr z80_nmi_callback = new TimerCallbackHandlerPtr() {
        public void handler(int param) {
            cpu_set_nmi_line(1, ASSERT_LINE);
        }
    };

    public static WriteHandlerPtr z80_arm_nmi = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_set_nmi_line(1, CLEAR_LINE);

            timer_set(TIME_IN_USEC(50), 0, z80_nmi_callback);
        }
    };

    public static WriteHandlerPtr z80_irq_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(1, 0xff);
        }
    };
    public static ReadHandlerPtr vendetta_sound_interrupt_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            cpu_cause_interrupt(1, 0xff);
            return 0x00;
        }
    };
    static int res = 0x00;
    public static ReadHandlerPtr vendetta_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* If the sound CPU is running, read the status, otherwise
             just make it pass the test */
            if (Machine.sample_rate != 0) {
                return K053260_ReadReg.handler(2 + offset);
            } else {

                res = ((res + 1) & 0x07);
                return offset != 0 ? res : 0x00;
            }
        }
    };

    /**
     * *****************************************
     */
    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_BANK1),
                new MemoryReadAddress(0x28d2, 0x28d2, speedup_r),
                new MemoryReadAddress(0x2000, 0x3fff, MRA_RAM),
                new MemoryReadAddress(0x5f80, 0x5f9f, K054000_r),
                new MemoryReadAddress(0x5fc0, 0x5fc0, input_port_0_r),
                new MemoryReadAddress(0x5fc1, 0x5fc1, input_port_1_r),
                new MemoryReadAddress(0x5fd0, 0x5fd0, vendetta_eeprom_r), /* vblank, service */
                new MemoryReadAddress(0x5fd1, 0x5fd1, input_port_2_r),
                new MemoryReadAddress(0x5fe4, 0x5fe4, vendetta_sound_interrupt_r),
                new MemoryReadAddress(0x5fe6, 0x5fe7, vendetta_sound_r),
                new MemoryReadAddress(0x5fe8, 0x5fe9, K053246_r),
                new MemoryReadAddress(0x5fea, 0x5fea, watchdog_reset_r),
                new MemoryReadAddress(0x4000, 0x4fff, MRA_BANK3),
                new MemoryReadAddress(0x6000, 0x6fff, MRA_BANK2),
                new MemoryReadAddress(0x4000, 0x7fff, K052109_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x3fff, MWA_RAM),
                new MemoryWriteAddress(0x5f80, 0x5f9f, K054000_w),
                new MemoryWriteAddress(0x5fa0, 0x5faf, K053251_w),
                new MemoryWriteAddress(0x5fb0, 0x5fb7, K053246_w),
                new MemoryWriteAddress(0x5fe0, 0x5fe0, vendetta_5fe0_w),
                new MemoryWriteAddress(0x5fe2, 0x5fe2, vendetta_eeprom_w),
                new MemoryWriteAddress(0x5fe4, 0x5fe4, z80_irq_w),
                new MemoryWriteAddress(0x5fe6, 0x5fe7, K053260_WriteReg),
                new MemoryWriteAddress(0x4000, 0x4fff, MWA_BANK3),
                new MemoryWriteAddress(0x6000, 0x6fff, MWA_BANK2),
                new MemoryWriteAddress(0x4000, 0x7fff, K052109_w),
                new MemoryWriteAddress(0x8000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_sound[]
            = {
                new MemoryReadAddress(0x0000, 0xefff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xf7ff, MRA_RAM),
                new MemoryReadAddress(0xf801, 0xf801, YM2151_status_port_0_r),
                new MemoryReadAddress(0xfc00, 0xfc2f, K053260_ReadReg),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_sound[]
            = {
                new MemoryWriteAddress(0x0000, 0xefff, MWA_ROM),
                new MemoryWriteAddress(0xf000, 0xf7ff, MWA_RAM),
                new MemoryWriteAddress(0xf800, 0xf800, YM2151_register_port_0_w),
                new MemoryWriteAddress(0xf801, 0xf801, YM2151_data_port_0_w),
                new MemoryWriteAddress(0xfa00, 0xfa00, z80_arm_nmi),
                new MemoryWriteAddress(0xfc00, 0xfc2f, K053260_WriteReg),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * *************************************************************************
     *
     * Input Ports
     *
     **************************************************************************
     */
    static InputPortPtr input_ports_vendetta = new InputPortPtr() {
        public void handler() {
            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN1);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_COIN2);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM data */

            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_UNKNOWN);/* EEPROM ready */

            PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR("Service_Mode"), KEYCODE_F2, IP_JOY_NONE);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_VBLANK);/* not really vblank, object related. Its timed, otherwise sprites flicker */

            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNUSED);
            INPUT_PORTS_END();
        }
    };

    /**
     * *************************************************************************
     *
     * Machine Driver
     *
     **************************************************************************
     */
    static YM2151interface ym2151_interface = new YM2151interface(
            1, /* 1 chip */
            3579545, /* 3.579545 MHz */
            new int[]{YM3012_VOL(35, MIXER_PAN_LEFT, 35, MIXER_PAN_RIGHT)},
            new WriteYmHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static K053260_interface k053260_interface = new K053260_interface(
            3579545,
            REGION_SOUND1, /* memory region */
            new int[]{MIXER(75, MIXER_PAN_LEFT), MIXER(75, MIXER_PAN_RIGHT)},
            null
    );
    public static InterruptPtr vendetta_irq = new InterruptPtr() {
        public int handler() {
            if (irq_enabled != 0) {
                return KONAMI_INT_IRQ;
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    static MachineDriver machine_driver_vendetta = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_KONAMI,
                        3000000, /* ? */
                        readmem, writemem, null, null,
                        vendetta_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3579545,
                        readmem_sound, writemem_sound, null, null,
                        ignore_interrupt, 0 /* interrupts are triggered by the main CPU */
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            vendetta_init_machine,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(13 * 8, (64 - 13) * 8 - 1, 2 * 8, 30 * 8 - 1),
            null, /* gfx decoded by konamiic.c */
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            vendetta_vh_start,
            vendetta_vh_stop,
            vendetta_vh_screenrefresh,
            /* sound hardware */
            SOUND_SUPPORTS_STEREO,0,0,0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2151,
                        ym2151_interface
                ),
                new MachineSound(
                        SOUND_K053260,
                        k053260_interface
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
    static RomLoadPtr rom_vendetta = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x49000, REGION_CPU1);/* code + banked roms + banked ram */

            ROM_LOAD("081u01", 0x10000, 0x38000, 0xb4d9ade5);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("081b02", 0x000000, 0x10000, 0x4c604d9b);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a09", 0x000000, 0x080000, 0xb4c777a9);/* characters */

            ROM_LOAD("081a08", 0x080000, 0x080000, 0x272ac8d9);/* characters */

            ROM_REGION(0x400000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a04", 0x000000, 0x100000, 0x464b9aa4);/* sprites */

            ROM_LOAD("081a05", 0x100000, 0x100000, 0x4e173759);/* sprites */

            ROM_LOAD("081a06", 0x200000, 0x100000, 0xe9fe6d80);/* sprites */

            ROM_LOAD("081a07", 0x300000, 0x100000, 0x8a22b29a);/* sprites */

            ROM_REGION(0x100000, REGION_SOUND1);/* 053260 samples */

            ROM_LOAD("081a03", 0x000000, 0x100000, 0x14b6baea);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vendett2 = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x49000, REGION_CPU1);/* code + banked roms + banked ram */

            ROM_LOAD("081d01", 0x10000, 0x38000, 0x335da495);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("081b02", 0x000000, 0x10000, 0x4c604d9b);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a09", 0x000000, 0x080000, 0xb4c777a9);/* characters */

            ROM_LOAD("081a08", 0x080000, 0x080000, 0x272ac8d9);/* characters */

            ROM_REGION(0x400000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a04", 0x000000, 0x100000, 0x464b9aa4);/* sprites */

            ROM_LOAD("081a05", 0x100000, 0x100000, 0x4e173759);/* sprites */

            ROM_LOAD("081a06", 0x200000, 0x100000, 0xe9fe6d80);/* sprites */

            ROM_LOAD("081a07", 0x300000, 0x100000, 0x8a22b29a);/* sprites */

            ROM_REGION(0x100000, REGION_SOUND1);/* 053260 samples */

            ROM_LOAD("081a03", 0x000000, 0x100000, 0x14b6baea);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vendettj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x49000, REGION_CPU1);/* code + banked roms + banked ram */

            ROM_LOAD("081p01", 0x10000, 0x38000, 0x5fe30242);
            ROM_CONTINUE(0x08000, 0x08000);

            ROM_REGION(0x10000, REGION_CPU2);/* 64k for the sound CPU */

            ROM_LOAD("081b02", 0x000000, 0x10000, 0x4c604d9b);

            ROM_REGION(0x100000, REGION_GFX1);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a09", 0x000000, 0x080000, 0xb4c777a9);/* characters */

            ROM_LOAD("081a08", 0x080000, 0x080000, 0x272ac8d9);/* characters */

            ROM_REGION(0x400000, REGION_GFX2);/* graphics ( don't dispose as the program can read them ) */

            ROM_LOAD("081a04", 0x000000, 0x100000, 0x464b9aa4);/* sprites */

            ROM_LOAD("081a05", 0x100000, 0x100000, 0x4e173759);/* sprites */

            ROM_LOAD("081a06", 0x200000, 0x100000, 0xe9fe6d80);/* sprites */

            ROM_LOAD("081a07", 0x300000, 0x100000, 0x8a22b29a);/* sprites */

            ROM_REGION(0x100000, REGION_SOUND1);/* 053260 samples */

            ROM_LOAD("081a03", 0x000000, 0x100000, 0x14b6baea);
            ROM_END();
        }
    };

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    public static InitDriverPtr init_vendetta = new InitDriverPtr() {
        public void handler() {
            konami_rom_deinterleave_2(REGION_GFX1);
            konami_rom_deinterleave_4(REGION_GFX2);
        }
    };

    public static GameDriver driver_vendetta = new GameDriver("1991", "vendetta", "vendetta.java", rom_vendetta, null, machine_driver_vendetta, input_ports_vendetta, init_vendetta, ROT0, "Konami", "Vendetta (Asia set 1)");
    public static GameDriver driver_vendett2 = new GameDriver("1991", "vendett2", "vendetta.java", rom_vendett2, driver_vendetta, machine_driver_vendetta, input_ports_vendetta, init_vendetta, ROT0, "Konami", "Vendetta (Asia set 2)");
    public static GameDriver driver_vendettj = new GameDriver("1991", "vendettj", "vendetta.java", rom_vendettj, driver_vendetta, machine_driver_vendetta, input_ports_vendetta, init_vendetta, ROT0, "Konami", "Crime Fighters 2 (Japan)");
}
