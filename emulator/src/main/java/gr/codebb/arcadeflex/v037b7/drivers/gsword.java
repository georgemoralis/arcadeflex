/*
 * ported to v0.37b7
 * ported to v0.36
 * 
 */
package gr.codebb.arcadeflex.v037b7.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.inptportH.*;
import static gr.codebb.arcadeflex.v037b7.vidhrdw.gsword.*;
import static arcadeflex.v036.sound.ay8910.*;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.machine.tait8741H.*;
import static gr.codebb.arcadeflex.v036.machine.tait8741.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.sound.MSM5205.*;
import static arcadeflex.v036.sound.MSM5205H.*;
import static arcadeflex.v036.cpu.z80.z80H.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.*;

public class gsword {

    static int coins;
    static int fake8910_0, fake8910_1;
    static int gsword_nmi_step, gsword_nmi_count;

    static int gsword_coins_in() {
        /* emulate 8741 coin slot */
        if ((readinputport(4) & 0xc0) != 0) {
            logerror("Coin In\n");
            
            return 0x80;
        }
        logerror("NO Coin\n");
        return 0x00;
    }

    public static ReadHandlerPtr gsword_8741_2_r = new ReadHandlerPtr() {
        public int handler(int num) {
            switch (num) {
                case 0x01: /* start button , coins */

                    return readinputport(0);
                case 0x02: /* Player 1 Controller */

                    return readinputport(1);
                case 0x04: /* Player 2 Controller */

                    return readinputport(3);
                default:
                    logerror("8741-2 unknown read %d PC=%04x\n", num, cpu_get_pc());
            }
            /* unknown */
            return 0;
        }
    };

    public static ReadHandlerPtr gsword_8741_3_r = new ReadHandlerPtr() {
        public int handler(int num) {
            switch (num) {
                case 0x01: /* start button  */

                    return readinputport(2);
                case 0x02: /* Player 1 Controller? */

                    return readinputport(1);
                case 0x04: /* Player 2 Controller? */

                    return readinputport(3);
            }
            /* unknown */
            logerror("8741-3 unknown read %d PC=%04x\n", num, cpu_get_pc());
            return 0;
        }
    };

    static TAITO8741interface gsword_8741interface = new TAITO8741interface(
            4, /* 4 chips */
            new int[]{TAITO8741_MASTER, TAITO8741_SLAVE, TAITO8741_PORT, TAITO8741_PORT}, /* program mode */
            new int[]{1, 0, 0, 0}, /* serial port connection */
            new ReadHandlerPtr[]{input_port_7_r, input_port_6_r, gsword_8741_2_r, gsword_8741_3_r} /* port handler */
    );

    public static InitMachinePtr machine_init = new InitMachinePtr() {
        public void handler() {
            UBytePtr ROM2 = memory_region(REGION_CPU2);

            ROM2.write(0x1da, 0xc3); /* patch for rom self check */

            ROM2.write(0x726, 0);    /* patch for sound protection or time out function */

            ROM2.write(0x727, 0);

            TAITO8741_start(gsword_8741interface);
        }
    };

    public static InitDriverPtr init_gsword = new InitDriverPtr() {
        public void handler() {
            int i;

            //for(i=0;i<4;i++) TAITO8741_reset(i);//shadow uneccesary it resets with machine_init
            coins = 0;
            gsword_nmi_count = 0;
            gsword_nmi_step = 0;
        }
    };

    public static InterruptPtr gsword_snd_interrupt = new InterruptPtr() {
        public int handler() {
            if ((gsword_nmi_count += gsword_nmi_step) >= 4) {
                gsword_nmi_count = 0;
                return Z80_NMI_INT;
            }
            return Z80_IGNORE_INT;
        }
    };

    public static WriteHandlerPtr gsword_nmi_set_w  = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            switch (data) {
                case 0x02:
                    /* needed to disable NMI for memory check */
                    gsword_nmi_step = 0;
                    gsword_nmi_count = 0;
                    break;
                case 0x0d:
                case 0x0f:
                    gsword_nmi_step = 4;
                    break;
                case 0xfe:
                case 0xff:
                    gsword_nmi_step = 4;
                    break;
            }
            /* bit1= nmi disable , for ram check */
            logerror("NMI controll %02x\n", data);
        }
    };

    public static WriteHandlerPtr gsword_AY8910_control_port_0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            AY8910_control_port_0_w.handler(offset, data);
            fake8910_0 = data;
        }
    };
    public static WriteHandlerPtr gsword_AY8910_control_port_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            AY8910_control_port_1_w.handler(offset, data);
            fake8910_1 = data;
        }
    };

    public static ReadHandlerPtr gsword_fake_0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return fake8910_0 + 1;
        }
    };
    public static ReadHandlerPtr gsword_fake_1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return fake8910_1 + 1;
        }
    };

    public static WriteHandlerPtr gsword_adpcm_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            MSM5205_data_w.handler(0, data & 0x0f); /* bit 0..3 */

            MSM5205_reset_w.handler(0, (data >> 5) & 1); /* bit 5    */

            MSM5205_vclk_w.handler(0, (data >> 4) & 1);  /* bit 4    */

        }
    };

    public static WriteHandlerPtr adpcm_soundcommand_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_set_nmi_line(2, PULSE_LINE);
        }
    };

    static MemoryReadAddress gsword_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x8fff, MRA_ROM),
                new MemoryReadAddress(0x9000, 0x9fff, MRA_RAM),
                new MemoryReadAddress(0xb000, 0xb7ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress gsword_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x8fff, MWA_ROM),
                new MemoryWriteAddress(0x9000, 0x9fff, MWA_RAM),
                new MemoryWriteAddress(0xa380, 0xa3ff, MWA_RAM, gs_spritetile_ram),
                new MemoryWriteAddress(0xa780, 0xa7ff, MWA_RAM, gs_spritexy_ram, gs_spritexy_size),
                new MemoryWriteAddress(0xa980, 0xa980, gs_charbank_w),
                new MemoryWriteAddress(0xaa80, 0xaa80, gs_videoctrl_w), /* flip screen, char palette bank */
                new MemoryWriteAddress(0xab00, 0xab00, MWA_RAM, gs_scrolly_ram),
                new MemoryWriteAddress(0xab80, 0xabff, MWA_RAM, gs_spriteattrib_ram),
                new MemoryWriteAddress(0xb000, 0xb7ff, gs_videoram_w, gs_videoram, gs_videoram_size),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(0x6000, 0x6000, adpcm_soundcommand_w),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress readmem_cpu3[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0xa000, 0xa000, soundlatch_r),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress writemem_cpu3[]
            = {
                new MemoryWriteAddress(0x0000, 0x5fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x8000, gsword_adpcm_data_w),
                new MemoryWriteAddress(-1)
            };

    static IOReadPort readport[]
            = {
                new IOReadPort(0x7e, 0x7f, TAITO8741_0_r),
                new IOReadPort(-1)
            };

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x7e, 0x7f, TAITO8741_0_w),
                new IOWritePort(-1)
            };

    static IOReadPort readport_cpu2[]
            = {
                new IOReadPort(0x00, 0x01, TAITO8741_2_r),
                new IOReadPort(0x20, 0x21, TAITO8741_3_r),
                new IOReadPort(0x40, 0x41, TAITO8741_1_r),
                new IOReadPort(0x60, 0x60, gsword_fake_0_r),
                new IOReadPort(0x61, 0x61, AY8910_read_port_0_r),
                new IOReadPort(0x80, 0x80, gsword_fake_1_r),
                new IOReadPort(0x81, 0x81, AY8910_read_port_1_r),
                new IOReadPort(0xe0, 0xe0, IORP_NOP), /* ?? */
                new IOReadPort(-1)
            };

    static IOWritePort writeport_cpu2[]
            = {
                new IOWritePort(0x00, 0x01, TAITO8741_2_w),
                new IOWritePort(0x20, 0x21, TAITO8741_3_w),
                new IOWritePort(0x40, 0x41, TAITO8741_1_w),
                new IOWritePort(0x60, 0x60, gsword_AY8910_control_port_0_w),
                new IOWritePort(0x61, 0x61, AY8910_write_port_0_w),
                new IOWritePort(0x80, 0x80, gsword_AY8910_control_port_1_w),
                new IOWritePort(0x81, 0x81, AY8910_write_port_1_w),
                new IOWritePort(0xa0, 0xa0, IOWP_NOP), /* ?? */
                new IOWritePort(0xe0, 0xe0, IOWP_NOP), /* watch dog ?*/
                new IOWritePort(-1)
            };

    static InputPortPtr input_ports_gsword = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 (8741-2 port1?) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_START(); 	/* IN1 (8741-2 port2?) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_START(); 	/* IN2 (8741-3 port1?) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_START(); 	/* IN3  (8741-3 port2?) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_START(); 	/* IN4 (coins) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN1, 1);

            PORT_START(); 	/* DSW0 */
            /* NOTE: Switches 0 & 1, 6,7,8 not used 	 */
            /*	 Coins configurations were handled 	 */
            /*	 via external hardware & not via program */

            PORT_DIPNAME(0x1c, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x1c, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x18, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x14, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x0c, DEF_STR("1C_5C"));

            PORT_START();       /* DSW1 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x04, "Stage 1 Difficulty");
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x04, "Normal");
            PORT_DIPSETTING(0x08, "Hard");
            PORT_DIPSETTING(0x0c, "Hardest");
            PORT_DIPNAME(0x10, 0x10, "Stage 2 Difficulty");
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPNAME(0x20, 0x20, "Stage 3 Difficulty");
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "Free Game Round");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();       /* DSW2 */

            PORT_DIPNAME(0x01, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x30, 0x00, "Stage Begins");
            PORT_DIPSETTING(0x00, "Fencing");
            PORT_DIPSETTING(0x10, "Kendo");
            PORT_DIPSETTING(0x20, "Rome");
            PORT_DIPSETTING(0x30, "Kendo");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static GfxLayout gsword_text = new GfxLayout(
            8, 8, /* 8x8 characters */
            1024, /* 1024 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            16 * 8 /* every char takes 16 bytes */
    );

    static GfxLayout gsword_sprites1 = new GfxLayout(
            16, 16, /* 16x16 sprites */
            64 * 2, /* 128 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8},
            64 * 8 /* every sprite takes 64 bytes */
    );

    static GfxLayout gsword_sprites2 = new GfxLayout(
            32, 32, /* 32x32 sprites */
            64, /* 64 sprites */
            2, /* 2 bits per pixel */
            new int[]{0, 4}, /* the two bitplanes for 4 pixels are packed into one byte */
            new int[]{0, 1, 2, 3, 8 * 8 + 0, 8 * 8 + 1, 8 * 8 + 2, 8 * 8 + 3,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 24 * 8 + 0, 24 * 8 + 1, 24 * 8 + 2, 24 * 8 + 3,
                64 * 8 + 0, 64 * 8 + 1, 64 * 8 + 2, 64 * 8 + 3, 72 * 8 + 0, 72 * 8 + 1, 72 * 8 + 2, 72 * 8 + 3,
                80 * 8 + 0, 80 * 8 + 1, 80 * 8 + 2, 80 * 8 + 3, 88 * 8 + 0, 88 * 8 + 1, 88 * 8 + 2, 88 * 8 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                32 * 8, 33 * 8, 34 * 8, 35 * 8, 36 * 8, 37 * 8, 38 * 8, 39 * 8,
                128 * 8, 129 * 8, 130 * 8, 131 * 8, 132 * 8, 133 * 8, 134 * 8, 135 * 8,
                160 * 8, 161 * 8, 162 * 8, 163 * 8, 164 * 8, 165 * 8, 166 * 8, 167 * 8},
            64 * 8 * 4 /* every sprite takes (64*8=16x6)*4) bytes */
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, gsword_text, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0, gsword_sprites1, 64 * 4, 64),
                new GfxDecodeInfo(REGION_GFX3, 0, gsword_sprites2, 64 * 4, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static AY8910interface ay8910_interface = new AY8910interface(
            2, /* 2 chips */
            1500000, /* 1.5 MHZ */
            new int[]{30, 30},
            new ReadHandlerPtr[]{null, null},
            new ReadHandlerPtr[]{null, null},
            new WriteHandlerPtr[]{null, gsword_nmi_set_w}, /* portA write */
            new WriteHandlerPtr[]{null, null}
    );

    static MSM5205interface msm5205_interface = new MSM5205interface(
            1, /* 1 chip             */
            384000, /* 384KHz verified!   */
            new vclk_interruptPtr[]{null}, /* interrupt function */
            new int[]{MSM5205_SEX_4B}, /* vclk input mode    */
            new int[]{60}
    );

    static MachineDriver machine_driver_gsword = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        3000000,
                        gsword_readmem, gsword_writemem,
                        readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3000000,
                        readmem_cpu2, writemem_cpu2,
                        readport_cpu2, writeport_cpu2,
                        gsword_snd_interrupt, 4
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        3000000,
                        readmem_cpu3, writemem_cpu3,
                        null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            200, /* Allow time for 2nd cpu to interleave*/
            machine_init,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            256, 64 * 4 + 64 * 4,
            gsword_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            gsword_vh_start,
            gsword_vh_stop,
            gsword_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_AY8910,
                        ay8910_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm5205_interface
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
    static RomLoadPtr rom_gsword = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64K for main CPU */

            ROM_LOAD("gs1", 0x0000, 0x2000, 0x565c4d9e);
            ROM_LOAD("gs2", 0x2000, 0x2000, 0xd772accf);
            ROM_LOAD("gs3", 0x4000, 0x2000, 0x2cee1871);
            ROM_LOAD("gs4", 0x6000, 0x2000, 0xca9d206d);
            ROM_LOAD("gs5", 0x8000, 0x1000, 0x2a892326);

            ROM_REGION(0x10000, REGION_CPU2);/* 64K for 2nd CPU */

            ROM_LOAD("gs15", 0x0000, 0x2000, 0x1aa4690e);
            ROM_LOAD("gs16", 0x2000, 0x2000, 0x10accc10);

            ROM_REGION(0x10000, REGION_CPU3);/* 64K for 3nd z80 */

            ROM_LOAD("gs12", 0x0000, 0x2000, 0xa6589068);
            ROM_LOAD("gs13", 0x2000, 0x2000, 0x4ee79796);
            ROM_LOAD("gs14", 0x4000, 0x2000, 0x455364b6);

            ROM_REGION(0x4000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gs10", 0x0000, 0x2000, 0x517c571b);/* tiles */

            ROM_LOAD("gs11", 0x2000, 0x2000, 0x7a1d8a3a);

            ROM_REGION(0x2000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gs6", 0x0000, 0x2000, 0x1b0a3cb7);/* sprites */

            ROM_REGION(0x4000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("gs7", 0x0000, 0x2000, 0xef5f28c6);
            ROM_LOAD("gs8", 0x2000, 0x2000, 0x46824b30);

            ROM_REGION(0x0360, REGION_PROMS);
            ROM_LOAD("ac0-1.bpr", 0x0000, 0x0100, 0x5c4b2adc);/* palette low bits */

            ROM_LOAD("ac0-2.bpr", 0x0100, 0x0100, 0x966bda66);/* palette high bits */

            ROM_LOAD("ac0-3.bpr", 0x0200, 0x0100, 0xdae13f77);/* sprite lookup table */

            ROM_LOAD("003", 0x0300, 0x0020, 0x43a548b8);/* address decoder? not used */

            ROM_LOAD("004", 0x0320, 0x0020, 0x43a548b8);/* address decoder? not used */

            ROM_LOAD("005", 0x0340, 0x0020, 0xe8d6dec0);/* address decoder? not used */

            ROM_END();
        }
    };

    public static GameDriver driver_gsword = new GameDriver("1984", "gsword", "gsword.java", rom_gsword, null, machine_driver_gsword, input_ports_gsword, init_gsword, ROT0, "Taito Corporation", "Great Swordsman", GAME_IMPERFECT_COLORS);
}
