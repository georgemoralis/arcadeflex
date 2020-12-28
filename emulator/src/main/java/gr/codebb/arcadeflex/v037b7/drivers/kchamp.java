/*
 * ported to 0.37b7
 * ported to v0.36
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910.*;
import static gr.codebb.arcadeflex.v037b7.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.kchamp.*;
import static gr.codebb.arcadeflex.v037b7.sound.dacH.*;
import static gr.codebb.arcadeflex.v037b7.sound.dac.*;
import static gr.codebb.arcadeflex.v037b7.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.memory_set_opcode_base;


public class kchamp {

    static int nmi_enable = 0;
    static int sound_nmi_enable = 0;

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xcfff, MRA_RAM),
                new MemoryReadAddress(0xd000, 0xd3ff, videoram_r),
                new MemoryReadAddress(0xd400, 0xd7ff, colorram_r),
                new MemoryReadAddress(0xd800, 0xd8ff, spriteram_r),
                new MemoryReadAddress(0xd900, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcfff, MWA_RAM),
                new MemoryWriteAddress(0xd000, 0xd3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xd400, 0xd7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xd800, 0xd8ff, spriteram_w, spriteram, spriteram_size),
                new MemoryWriteAddress(0xd900, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x6000, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1)
            };

    public static WriteHandlerPtr control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            nmi_enable = data & 1;
        }
    };

    public static WriteHandlerPtr sound_reset_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((data & 1) == 0) {
                cpu_set_reset_line(1, PULSE_LINE);
            }
        }
    };

    public static WriteHandlerPtr sound_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            MSM5205_reset_w.handler(0, (data & 1) == 0 ? 1 : 0);
            sound_nmi_enable = ((data >> 1) & 1);
        }
    };

    public static WriteHandlerPtr sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_cause_interrupt(1, 0xff);
        }
    };

    static int msm_data = 0;
    static int msm_play_lo_nibble = 1;

    public static WriteHandlerPtr sound_msm_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            msm_data = data;
            msm_play_lo_nibble = 1;
        }
    };

    static IOReadPort readport[]
            = {
                new IOReadPort(0x00, 0x00, input_port_0_r), /* Player 1 controls - ACTIVE LOW */
                new IOReadPort(0x40, 0x40, input_port_1_r), /* Player 2 controls - ACTIVE LOW */
                new IOReadPort(0x80, 0x80, input_port_2_r), /* Coins  Start - ACTIVE LOW */
                new IOReadPort(0xC0, 0xC0, input_port_3_r), /* Dipswitch */
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x00, MWA_NOP),
                new IOWritePort(0x01, 0x01, control_w),
                new IOWritePort(0x02, 0x02, sound_reset_w),
                new IOWritePort(0x40, 0x40, sound_command_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x01, 0x01, soundlatch_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_write_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_control_port_0_w),
                new IOWritePort(0x02, 0x02, AY8910_write_port_1_w),
                new IOWritePort(0x03, 0x03, AY8910_control_port_1_w),
                new IOWritePort(0x04, 0x04, sound_msm_w),
                new IOWritePort(0x05, 0x05, sound_control_w),
                new IOWritePort(-1) /* end of table */};

    /**
     * ******************
     * 1 Player Version * ******************
     */
    static MemoryReadAddress kc_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xc000, 0xdfff, MRA_RAM),
                new MemoryReadAddress(0xe000, 0xe3ff, videoram_r),
                new MemoryReadAddress(0xe400, 0xe7ff, colorram_r),
                new MemoryReadAddress(0xea00, 0xeaff, spriteram_r),
                new MemoryReadAddress(0xeb00, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress kc_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xdfff, MWA_RAM),
                new MemoryWriteAddress(0xe000, 0xe3ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0xe400, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xea00, 0xeaff, spriteram_w, spriteram, spriteram_size),
                new MemoryWriteAddress(0xeb00, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress kc_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0xdfff, MRA_ROM),
                new MemoryReadAddress(0xe000, 0xe2ff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress kc_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xdfff, MWA_ROM),
                new MemoryWriteAddress(0xe000, 0xe2ff, MWA_RAM),
                new MemoryWriteAddress(-1)
            };

    public static ReadHandlerPtr sound_reset_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            cpu_set_reset_line(1, PULSE_LINE);
            return 0;
        }
    };

    public static WriteHandlerPtr kc_sound_control_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (offset == 0) {
                sound_nmi_enable = ((data >> 7) & 1);
            }
            //	else
            //		DAC_set_volume(0,( data == 1 ) ? 255 : 0,0);
        }
    };

    static IOReadPort kc_readport[]
            = {
                new IOReadPort(0x90, 0x90, input_port_0_r), /* Player 1 controls - ACTIVE LOW */
                new IOReadPort(0x98, 0x98, input_port_1_r), /* Player 2 controls - ACTIVE LOW */
                new IOReadPort(0xa0, 0xa0, input_port_2_r), /* Coins  Start - ACTIVE LOW */
                new IOReadPort(0x80, 0x80, input_port_3_r), /* Dipswitch */
                new IOReadPort(0xa8, 0xa8, sound_reset_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort kc_writeport[]
            = {
                new IOWritePort(0x80, 0x80, MWA_NOP),
                new IOWritePort(0x81, 0x81, control_w),
                new IOWritePort(0xa8, 0xa8, sound_command_w),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort kc_sound_readport[]
            = {
                new IOReadPort(0x06, 0x06, soundlatch_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort kc_sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, AY8910_write_port_0_w),
                new IOWritePort(0x01, 0x01, AY8910_control_port_0_w),
                new IOWritePort(0x02, 0x02, AY8910_write_port_1_w),
                new IOWritePort(0x03, 0x03, AY8910_control_port_1_w),
                new IOWritePort(0x04, 0x04, DAC_0_data_w),
                new IOWritePort(0x05, 0x05, kc_sound_control_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_kchampvs = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_4WAY);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2 | IPF_4WAY);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x30, 0x10, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x30, "Easy");
            PORT_DIPSETTING(0x20, "Medium");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ******************
     * 1 Player Version * ******************
     */
    static InputPortPtr input_ports_kchamp = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_4WAY);

            PORT_START();
            /* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP | IPF_PLAYER2 | IPF_4WAY);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN | IPF_PLAYER2 | IPF_4WAY);

            PORT_START();
            /* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNUSED);

            PORT_START();
            /* DSW0 */

            PORT_DIPNAME(0x03, 0x03, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x10, 0x10, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Hard");
            PORT_DIPSETTING(0x10, "Normal");
            PORT_DIPNAME(0x20, 0x20, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x20, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x40, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x80, DEF_STR("Cocktail"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout tilelayout = new GfxLayout(
            8, 8, /* tile size */
            256 * 8, /* number of tiles */
            2, /* bits per pixel */
            new int[]{0x4000 * 8, 0}, /* plane offsets */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7}, /* x offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* y offsets */
            8 * 8 /* offset to next tile */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* tile size */
            512, /* number of tiles */
            2, /* bits per pixel */
            new int[]{0xC000 * 8, 0}, /* plane offsets */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                0x2000 * 8 + 0, 0x2000 * 8 + 1, 0x2000 * 8 + 2, 0x2000 * 8 + 3,
                0x2000 * 8 + 4, 0x2000 * 8 + 5, 0x2000 * 8 + 6, 0x2000 * 8 + 7}, /* x offsets */
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8}, /* y offsets */
            16 * 8 /* ofset to next tile */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, tilelayout, 32 * 4, 32),
                new GfxDecodeInfo(REGION_GFX2, 0x08000, spritelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x04000, spritelayout, 0, 16),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, spritelayout, 0, 16),
                new GfxDecodeInfo(-1)
            };

    public static InterruptPtr kc_interrupt = new InterruptPtr() {
        public int handler() {

            if (nmi_enable != 0) {
                return Z80_NMI_INT;
            }

            return Z80_IGNORE_INT;
        }
    };
    static int counter = 0;
    public static vclk_interruptPtr msmint = new vclk_interruptPtr() {
        public void handler(int data) {

            if (msm_play_lo_nibble != 0) {
                MSM5205_data_w.handler(0, msm_data & 0x0f);
            } else {
                MSM5205_data_w.handler(0, (msm_data >> 4) & 0x0f);
            }

            msm_play_lo_nibble = msm_play_lo_nibble ^ 1;

            if ((counter ^= 1) == 0) {
                if (sound_nmi_enable != 0) {
                    cpu_cause_interrupt(1, Z80_NMI_INT);
                }
            }
        }
    };

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 12 Mhz / 8 = 1.5 Mhz */
            new int[]{30, 30}, // Modified by T.Nogi 1999/11/08
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, null}
    );

    static MSM5205interface msm_interface = new MSM5205interface(
            1, /* 1 chip */
            375000, /* 12Mhz / 16 / 2 */
            new vclk_interruptPtr[]{msmint}, /* interrupt function */
            new int[]{MSM5205_S96_4B}, /* 1 / 96 = 3906.25Hz playback */
            new int[]{100}
    );

    /**
     * ******************
     * 1 Player Version * ******************
     */
    public static InterruptPtr sound_int = new InterruptPtr() {
        public int handler() {
            if (sound_nmi_enable != 0) {
                return Z80_NMI_INT;
            }

            return Z80_IGNORE_INT;
        }
    };

    static DACinterface dac_interface = new DACinterface(
            1,
            new int[]{50}
    );

    static MachineDriver machine_driver_kchampvs = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3000000, /* 12Mhz / 4 = 3.0 Mhz */
                        readmem, writemem, readport, writeport,
                        kc_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 12Mhz / 4 = 3.0 Mhz */
                        sound_readmem, sound_writemem, sound_readport, sound_writeport,
                        ignore_interrupt, 0
                /* irq's triggered from main cpu */
                /* nmi's from msm5205 */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* Interleaving forced by interrupts */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, /* number of colors */
            256, /* color table length */
            kchamp_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            kchampvs_vh_start,
            generic_vh_stop,
            kchamp_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm_interface
                )
            }
    );

    /**
     * ******************
     * 1 Player Version * ******************
     */
    static MachineDriver machine_driver_kchamp = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3000000, /* 12Mhz / 4 = 3.0 Mhz */
                        kc_readmem, kc_writemem, kc_readport, kc_writeport,
                        kc_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000, /* 12Mhz / 4 = 3.0 Mhz */
                        kc_sound_readmem, kc_sound_writemem, kc_sound_readport, kc_sound_writeport,
                        ignore_interrupt, 0,
                        sound_int, 125 /* Hz */
                /* irq's triggered from main cpu */
                /* nmi's from 125 Hz clock */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION,
            1, /* Interleaving forced by interrupts */
            null, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, /* number of colors */
            256, /* color table length */
            kchamp_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            kchamp1p_vh_start,
            generic_vh_stop,
            kchamp_vh_screenrefresh,
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
            }
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_kchamp = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("b014.bin", 0x0000, 0x2000, 0x0000d1a0);
            ROM_LOAD("b015.bin", 0x2000, 0x2000, 0x03fae67e);
            ROM_LOAD("b016.bin", 0x4000, 0x2000, 0x3b6e1d08);
            ROM_LOAD("b017.bin", 0x6000, 0x2000, 0xc1848d1a);
            ROM_LOAD("b018.bin", 0x8000, 0x2000, 0xb824abc7);
            ROM_LOAD("b019.bin", 0xa000, 0x2000, 0x3b487a46);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */ /* 64k for code */


            ROM_LOAD("b026.bin", 0x0000, 0x2000, 0x999ed2c7);
            ROM_LOAD("b025.bin", 0x2000, 0x2000, 0x33171e07);/* adpcm */

            ROM_LOAD("b024.bin", 0x4000, 0x2000, 0x910b48b9);/* adpcm */

            ROM_LOAD("b023.bin", 0x6000, 0x2000, 0x47f66aac);
            ROM_LOAD("b022.bin", 0x8000, 0x2000, 0x5928e749);
            ROM_LOAD("b021.bin", 0xa000, 0x2000, 0xca17e3ba);
            ROM_LOAD("b020.bin", 0xc000, 0x2000, 0xada4f2cd);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b000.bin", 0x00000, 0x2000, 0xa4fa98a1);
            /* plane0 */ /* tiles */


            ROM_LOAD("b001.bin", 0x04000, 0x2000, 0xfea09f7c);
            /* plane1 */ /* tiles */


            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("b013.bin", 0x00000, 0x2000, 0xeaad4168);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("b004.bin", 0x02000, 0x2000, 0x10a47e2d);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("b012.bin", 0x04000, 0x2000, 0xb4842ea9);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("b003.bin", 0x06000, 0x2000, 0x8cd166a5);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("b011.bin", 0x08000, 0x2000, 0x4cbd3aa3);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("b002.bin", 0x0a000, 0x2000, 0x6be342a6);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("b007.bin", 0x0c000, 0x2000, 0xcb91d16b);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("b010.bin", 0x0e000, 0x2000, 0x489c9c04);
            /* bot, plane1 */ /* sprites */


            ROM_LOAD("b006.bin", 0x10000, 0x2000, 0x7346db8a);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("b009.bin", 0x12000, 0x2000, 0xb78714fc);
            /* bot, plane1 */ /* sprites */


            ROM_LOAD("b005.bin", 0x14000, 0x2000, 0xb2557102);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("b008.bin", 0x16000, 0x2000, 0xc85aba0e);
            /* bot, plane1 */ /* sprites */


            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("br27", 0x0000, 0x0100, 0xf683c54a);/* red */

            ROM_LOAD("br26", 0x0100, 0x0100, 0x3ddbb6c4);/* green */

            ROM_LOAD("br25", 0x0200, 0x0100, 0xba4a5651);/* blue */

            ROM_END();
        }
    };

    static RomLoadPtr rom_karatedo = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("be14", 0x0000, 0x2000, 0x44e60aa0);
            ROM_LOAD("be15", 0x2000, 0x2000, 0xa65e3793);
            ROM_LOAD("be16", 0x4000, 0x2000, 0x151d8872);
            ROM_LOAD("be17", 0x6000, 0x2000, 0x8f393b6a);
            ROM_LOAD("be18", 0x8000, 0x2000, 0xa09046ad);
            ROM_LOAD("be19", 0xa000, 0x2000, 0x0cdc4da9);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */ /* 64k for code */


            ROM_LOAD("be26", 0x0000, 0x2000, 0x999ab0a3);
            ROM_LOAD("be25", 0x2000, 0x2000, 0x253bf0da);/* adpcm */

            ROM_LOAD("be24", 0x4000, 0x2000, 0xe2c188af);/* adpcm */

            ROM_LOAD("be23", 0x6000, 0x2000, 0x25262de1);
            ROM_LOAD("be22", 0x8000, 0x2000, 0x38055c48);
            ROM_LOAD("be21", 0xa000, 0x2000, 0x5f0efbe7);
            ROM_LOAD("be20", 0xc000, 0x2000, 0xcbe8a533);

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("be00", 0x00000, 0x2000, 0xcec020f2);
            /* plane0 */ /* tiles */


            ROM_LOAD("be01", 0x04000, 0x2000, 0xcd96271c);
            /* plane1 */ /* tiles */


            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("be13", 0x00000, 0x2000, 0xfb358707);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("be04", 0x02000, 0x2000, 0x48372bf8);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("b012.bin", 0x04000, 0x2000, 0xb4842ea9);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("b003.bin", 0x06000, 0x2000, 0x8cd166a5);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("b011.bin", 0x08000, 0x2000, 0x4cbd3aa3);
            /* top, plane0 */ /* sprites */


            ROM_LOAD("b002.bin", 0x0a000, 0x2000, 0x6be342a6);
            /* bot, plane0 */ /* sprites */


            ROM_LOAD("be07", 0x0c000, 0x2000, 0x40f2b6fb);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("be10", 0x0e000, 0x2000, 0x325c0a97);
            /* bot, plane1 */ /* sprites */


            ROM_LOAD("b006.bin", 0x10000, 0x2000, 0x7346db8a);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("b009.bin", 0x12000, 0x2000, 0xb78714fc);
            /* bot, plane1 */ /* sprites */


            ROM_LOAD("b005.bin", 0x14000, 0x2000, 0xb2557102);
            /* top, plane1 */ /* sprites */


            ROM_LOAD("b008.bin", 0x16000, 0x2000, 0xc85aba0e);
            /* bot, plane1 */ /* sprites */


            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("br27", 0x0000, 0x0100, 0xf683c54a);/* red */

            ROM_LOAD("br26", 0x0100, 0x0100, 0x3ddbb6c4);/* green */

            ROM_LOAD("br25", 0x0200, 0x0100, 0xba4a5651);/* blue */

            ROM_END();
        }
    };

    static RomLoadPtr rom_kchampvs = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);/* 64k for code + 64k for decrypted opcodes */

            ROM_LOAD("bs24", 0x0000, 0x2000, 0x829da69b);
            ROM_LOAD("bs23", 0x2000, 0x2000, 0x091f810e);
            ROM_LOAD("bs22", 0x4000, 0x2000, 0xd4df2a52);
            ROM_LOAD("bs21", 0x6000, 0x2000, 0x3d4ef0da);
            ROM_LOAD("bs20", 0x8000, 0x2000, 0x623a467b);
            ROM_LOAD("bs19", 0xa000, 0x2000, 0x43e196c4);
            ROM_CONTINUE(0xe000, 0x2000);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */ /* 64k for code */


            ROM_LOAD("bs18", 0x0000, 0x2000, 0xeaa646eb);
            ROM_LOAD("bs17", 0x2000, 0x2000, 0xd71031ad);/* adpcm */

            ROM_LOAD("bs16", 0x4000, 0x2000, 0x6f811c43);/* adpcm */

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bs12", 0x00000, 0x2000, 0x4c574ecd);
            ROM_LOAD("bs13", 0x02000, 0x2000, 0x750b66af);
            ROM_LOAD("bs14", 0x04000, 0x2000, 0x9ad6227c);
            ROM_LOAD("bs15", 0x06000, 0x2000, 0x3b6d5de5);

            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bs00", 0x00000, 0x2000, 0x51eda56c);
            ROM_LOAD("bs06", 0x02000, 0x2000, 0x593264cf);
            ROM_LOAD("b012.bin", 0x04000, 0x2000, 0xb4842ea9);
            /* bs01 */

            ROM_LOAD("b003.bin", 0x06000, 0x2000, 0x8cd166a5);
            /* bs07 */

            ROM_LOAD("b011.bin", 0x08000, 0x2000, 0x4cbd3aa3);
            /* bs02 */

            ROM_LOAD("b002.bin", 0x0a000, 0x2000, 0x6be342a6);
            /* bs08 */

            ROM_LOAD("bs03", 0x0c000, 0x2000, 0x8dcd271a);
            ROM_LOAD("bs09", 0x0e000, 0x2000, 0x4ee1dba7);
            ROM_LOAD("b006.bin", 0x10000, 0x2000, 0x7346db8a);
            /* bs04 */

            ROM_LOAD("b009.bin", 0x12000, 0x2000, 0xb78714fc);
            /* bs10 */

            ROM_LOAD("b005.bin", 0x14000, 0x2000, 0xb2557102);
            /* bs05 */

            ROM_LOAD("b008.bin", 0x16000, 0x2000, 0xc85aba0e);
            /* bs11 */

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("br27", 0x0000, 0x0100, 0xf683c54a);/* red */

            ROM_LOAD("br26", 0x0100, 0x0100, 0x3ddbb6c4);/* green */

            ROM_LOAD("br25", 0x0200, 0x0100, 0xba4a5651);/* blue */

            ROM_END();
        }
    };

    static RomLoadPtr rom_karatevs = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(2 * 0x10000, REGION_CPU1);/* 64k for code + 64k for decrypted opcodes */

            ROM_LOAD("br24", 0x0000, 0x2000, 0xea9cda49);
            ROM_LOAD("br23", 0x2000, 0x2000, 0x46074489);
            ROM_LOAD("br22", 0x4000, 0x2000, 0x294f67ba);
            ROM_LOAD("br21", 0x6000, 0x2000, 0x934ea874);
            ROM_LOAD("br20", 0x8000, 0x2000, 0x97d7816a);
            ROM_LOAD("br19", 0xa000, 0x2000, 0xdd2239d2);
            ROM_CONTINUE(0xe000, 0x2000);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound CPU */ /* 64k for code */


            ROM_LOAD("br18", 0x0000, 0x2000, 0x00ccb8ea);
            ROM_LOAD("bs17", 0x2000, 0x2000, 0xd71031ad);/* adpcm */

            ROM_LOAD("br16", 0x4000, 0x2000, 0x2512d961);/* adpcm */

            ROM_REGION(0x08000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("br12", 0x00000, 0x2000, 0x9ed6f00d);
            ROM_LOAD("bs13", 0x02000, 0x2000, 0x750b66af);
            ROM_LOAD("br14", 0x04000, 0x2000, 0xfc399229);
            ROM_LOAD("bs15", 0x06000, 0x2000, 0x3b6d5de5);

            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("br00", 0x00000, 0x2000, 0xc46a8b88);
            ROM_LOAD("br06", 0x02000, 0x2000, 0xcf8982ff);
            ROM_LOAD("b012.bin", 0x04000, 0x2000, 0xb4842ea9);
            /* bs01 */

            ROM_LOAD("b003.bin", 0x06000, 0x2000, 0x8cd166a5);
            /* bs07 */

            ROM_LOAD("b011.bin", 0x08000, 0x2000, 0x4cbd3aa3);
            /* bs02 */

            ROM_LOAD("b002.bin", 0x0a000, 0x2000, 0x6be342a6);
            /* bs08 */

            ROM_LOAD("br03", 0x0c000, 0x2000, 0xbde8a52b);
            ROM_LOAD("br09", 0x0e000, 0x2000, 0xe9a5f945);
            ROM_LOAD("b006.bin", 0x10000, 0x2000, 0x7346db8a);
            /* bs04 */

            ROM_LOAD("b009.bin", 0x12000, 0x2000, 0xb78714fc);
            /* bs10 */

            ROM_LOAD("b005.bin", 0x14000, 0x2000, 0xb2557102);
            /* bs05 */

            ROM_LOAD("b008.bin", 0x16000, 0x2000, 0xc85aba0e);
            /* bs11 */

            ROM_REGION(0x0300, REGION_PROMS);
            ROM_LOAD("br27", 0x0000, 0x0100, 0xf683c54a);/* red */

            ROM_LOAD("br26", 0x0100, 0x0100, 0x3ddbb6c4);/* green */

            ROM_LOAD("br25", 0x0200, 0x0100, 0xba4a5651);/* blue */

            ROM_END();
        }
    };

    public static InitDriverPtr init_kchampvs = new InitDriverPtr() {
        public void handler() {
            UBytePtr rom = memory_region(REGION_CPU1);
            int diff = memory_region_length(REGION_CPU1) / 2;
            int A;

            memory_set_opcode_base(0, new UBytePtr(rom, diff));

            for (A = 0; A < 0x10000; A++) {
                rom.write(A + diff, (rom.read(A) & 0x55) | ((rom.read(A) & 0x88) >> 2) | ((rom.read(A) & 0x22) << 2));
            }

            /*
             Note that the first 4 opcodes that the program
             executes aren't encrypted for some obscure reason.
             The address for the 2nd opcode (a jump) is encrypted too.
             It's not clear what the 3rd and 4th opcode are supposed to do,
             they just write to a RAM location. This write might be what
             turns the encryption on, but this doesn't explain the
             encrypted address for the jump.
             */
            rom.write(0 + diff, rom.read(0));
            /* this is a jump */

            A = rom.read(1) + 256 * rom.read(2);
            rom.write(A + diff, rom.read(A));
            /* fix opcode on first jump address (again, a jump) */

            rom.write(A + 1, rom.read(A + 1) ^ 0xee);
            /* fix address of the second jump */

            A = rom.read(A + 1) + 256 * rom.read(A + 2);
            rom.write(A + diff, rom.read(A));
            /* fix third opcode (ld a,$xx) */

            A += 2;
            rom.write(A + diff, rom.read(A));
            /* fix fourth opcode (ld ($xxxx),a */
 /* and from here on, opcodes are encrypted */

        }
    };

    public static GameDriver driver_kchamp = new GameDriver("1984", "kchamp", "kchamp.java", rom_kchamp, null, machine_driver_kchamp, input_ports_kchamp, null, ROT90, "Data East USA", "Karate Champ (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_karatedo = new GameDriver("1984", "karatedo", "kchamp.java", rom_karatedo, driver_kchamp, machine_driver_kchamp, input_ports_kchamp, null, ROT90, "Data East Corporation", "Karate Dou (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_kchampvs = new GameDriver("1984", "kchampvs", "kchamp.java", rom_kchampvs, driver_kchamp, machine_driver_kchampvs, input_ports_kchampvs, init_kchampvs, ROT90, "Data East USA", "Karate Champ (US VS version)", GAME_NO_COCKTAIL);
    public static GameDriver driver_karatevs = new GameDriver("1984", "karatevs", "kchamp.java", rom_karatevs, driver_kchamp, machine_driver_kchampvs, input_ports_kchampvs, init_kchampvs, ROT90, "Data East Corporation", "Taisen Karate Dou (Japan VS version)", GAME_NO_COCKTAIL);
}
