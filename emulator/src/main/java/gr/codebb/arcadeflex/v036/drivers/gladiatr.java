/*
 Issues:
 - YM2203 mixing problems (loss of bass notes)
 - YM2203 some sound effects just don't sound correct
 - Audio Filter Switch not hooked up (might solve YM2203 mixing issue)
 - Ports 60,61,80,81 not fully understood yet...
 - CPU speed may not be accurate
 - some sprites linger on later stages (perhaps a sprite enable bit?)
 - Flipscreen not implemented
 - Scrolling issues in Test mode!
 - The four 8741 ROMs are available but not used.
 */
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package gr.codebb.arcadeflex.v036.drivers;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.inputport.*;
import static gr.codebb.arcadeflex.v037b7.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.gladiatr.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intf.*;
import static gr.codebb.arcadeflex.v037b7.sound._2203intfH.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v036.machine.tait8741H.*;
import static gr.codebb.arcadeflex.v036.machine.tait8741.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrfH.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205.*;
import static gr.codebb.arcadeflex.v036.sound.MSM5205H.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.v036.platform.fileio.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;

public class gladiatr {

    /*Rom bankswitching*/
    static int banka;

    /*Rom bankswitching*/
    public static WriteHandlerPtr gladiatr_bankswitch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int[] bank1 = {0x10000, 0x12000};
            int[] bank2 = {0x14000, 0x18000};
            UBytePtr RAM = memory_region(REGION_CPU1);
            banka = data;
            cpu_setbank(1, new UBytePtr(RAM, bank1[(data & 0x03)]));
            cpu_setbank(2, new UBytePtr(RAM, bank2[(data & 0x03)]));
        }
    };

    public static ReadHandlerPtr gladiatr_bankswitch_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return banka;
        }
    };

    public static ReadHandlerPtr gladiator_dsw1_r = new ReadHandlerPtr() {
        public int handler(int num) {
            int orig = readinputport(0); /* DSW1 */
            /*Reverse all bits for Input Port 0*/
            /*ie..Bit order is: 0,1,2,3,4,5,6,7*/

            return ((orig & 0x01) << 7) | ((orig & 0x02) << 5)
                    | ((orig & 0x04) << 3) | ((orig & 0x08) << 1)
                    | ((orig & 0x10) >> 1) | ((orig & 0x20) >> 3)
                    | ((orig & 0x40) >> 5) | ((orig & 0x80) >> 7);
        }
    };

    public static ReadHandlerPtr gladiator_dsw2_r = new ReadHandlerPtr() {
        public int handler(int num) {
            int orig = readinputport(1); /* DSW2 */
            /*Bits 2-7 are reversed for Input Port 1*/
            /*ie..Bit order is: 2,3,4,5,6,7,1,0*/

            return (orig & 0x01) | (orig & 0x02)
                    | ((orig & 0x04) << 5) | ((orig & 0x08) << 3)
                    | ((orig & 0x10) << 1) | ((orig & 0x20) >> 1)
                    | ((orig & 0x40) >> 3) | ((orig & 0x80) >> 5);
        }
    };

    public static ReadHandlerPtr gladiator_controll_r = new ReadHandlerPtr() {
        public int handler(int num) {
            int coins = 0;

            if ((readinputport(4) & 0xc0) != 0) {
                coins = 0x80;
            }
            switch (num) {
                case 0x01: /* start button , coins */

                    return readinputport(3) | coins;
                case 0x02: /* Player 1 Controller , coins */

                    return readinputport(5) | coins;
                case 0x04: /* Player 2 Controller , coins */

                    return readinputport(6) | coins;
            }
            /* unknown */
            return 0;
        }
    };

    public static ReadHandlerPtr gladiator_button3_r = new ReadHandlerPtr() {
        public int handler(int num) {
            switch (num) {
                case 0x01: /* button 3 */

                    return readinputport(7);
            }
            /* unknown */
            return 0;
        }
    };

    static TAITO8741interface gsword_8741interface = new TAITO8741interface(
            4, /* 4 chips */
            new int[]{TAITO8741_MASTER, TAITO8741_SLAVE, TAITO8741_PORT, TAITO8741_PORT},/* program mode */
            new int[]{1, 0, 0, 0}, /* serial port connection */
            new ReadHandlerPtr[]{gladiator_dsw1_r, gladiator_dsw2_r, gladiator_button3_r, gladiator_controll_r} /* port handler */
    );

    public static InitMachinePtr gladiator_machine_init = new InitMachinePtr() {
        public void handler() {
            TAITO8741_start(gsword_8741interface);
            /* 6809 bank memory set */
            {
                UBytePtr RAM = memory_region(REGION_CPU3);
                cpu_setbank(3, new UBytePtr(RAM, 0x10000));
                cpu_setbank(4, new UBytePtr(RAM, 0x18000));
                cpu_setbank(5, new UBytePtr(RAM, 0x20000));
            }
        }
    };

    //#if 1
	/* !!!!! patch to IRQ timming for 2nd CPU !!!!! */
    public static WriteHandlerPtr gladiatr_irq_patch_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(1, Z80_INT_REQ);
        }
    };
	//#endif

    /* YM2203 port A handler (input) */
    public static ReadHandlerPtr gladiator_dsw3_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return input_port_2_r.handler(offset) ^ 0xff;
        }
    };
    /* YM2203 port B handler (output) */
    public static WriteHandlerPtr gladiator_int_controll_w = new WriteHandlerPtr() {
        public void handler(int offer, int data) {
            /* bit 7   : SSRST = sound reset ? */
            /* bit 6-1 : N.C.                  */
            /* bit 0   : ??                    */
        }
    };
    /* YM2203 IRQ */
    public static WriteYmHandlerPtr gladiator_ym_irq = new WriteYmHandlerPtr() {
        public void handler(int irq) {
            /* NMI IRQ is not used by gladiator sound program */
            cpu_set_nmi_line(1, irq != 0 ? ASSERT_LINE : CLEAR_LINE);
            /* cpu_cause_interrupt(1,Z80_NMI_INT); */

        }
    };

    /*Sound Functions*/
    public static WriteHandlerPtr glad_adpcm_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            UBytePtr RAM = memory_region(REGION_CPU3);
            /* bit6 = bank offset */
            int bankoffset = (data & 0x40) != 0 ? 0x4000 : 0;
            cpu_setbank(3, new UBytePtr(RAM, 0x10000 + bankoffset));
            cpu_setbank(4, new UBytePtr(RAM, 0x18000 + bankoffset));
            cpu_setbank(5, new UBytePtr(RAM, 0x20000 + bankoffset));

            MSM5205_data_w.handler(0, data);         /* bit0..3  */

            MSM5205_reset_w.handler(0, (data >> 5) & 1); /* bit 5    */

            MSM5205_vclk_w.handler(0, (data >> 4) & 1); /* bit4     */

        }
    };

    public static WriteHandlerPtr glad_cpu_sound_command_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            soundlatch_w.handler(0, data);
            cpu_set_nmi_line(2, ASSERT_LINE);
        }
    };

    public static ReadHandlerPtr glad_cpu_sound_command_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            cpu_set_nmi_line(2, CLEAR_LINE);
            return soundlatch_r.handler(0);
        }
    };

    static UBytePtr nvram = new UBytePtr();
    static int[] nvram_size = new int[1];

    public static nvramPtr nvram_handler = new nvramPtr() {
        public void handler(Object file, int read_or_write) {
            /*if (read_or_write!=0)
             {
             //osd_fwrite(file,nvram,nvram_size[0]);
             }
             else
             {
             if (file!=null)
             {
             //osd_fread(file,nvram,nvram_size[0]);
             }
             else
             {
             memset(nvram,0,nvram_size[0]);
             }
             }*/
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x5fff, MRA_ROM),
                new MemoryReadAddress(0x6000, 0x7fff, MRA_BANK1),
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK2),
                new MemoryReadAddress(0xc000, 0xcbff, MRA_RAM),
                new MemoryReadAddress(0xcc00, 0xcfff, gladiatr_video_registers_r),
                new MemoryReadAddress(0xd000, 0xffff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xcbff, MWA_RAM, spriteram),
                new MemoryWriteAddress(0xcc00, 0xcfff, gladiatr_video_registers_w),
                new MemoryWriteAddress(0xd000, 0xd1ff, gladiatr_paletteram_rg_w, paletteram),
                new MemoryWriteAddress(0xd200, 0xd3ff, MWA_RAM),
                new MemoryWriteAddress(0xd400, 0xd5ff, gladiatr_paletteram_b_w, paletteram_2),
                new MemoryWriteAddress(0xd600, 0xd7ff, MWA_RAM),
                new MemoryWriteAddress(0xd800, 0xdfff, videoram_w, videoram),
                new MemoryWriteAddress(0xe000, 0xe7ff, colorram_w, colorram),
                new MemoryWriteAddress(0xe800, 0xefff, MWA_RAM, gladiator_text),
                new MemoryWriteAddress(0xf000, 0xf3ff, MWA_RAM, nvram, nvram_size), /* battery backed RAM */
                new MemoryWriteAddress(0xf400, 0xffff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress readmem_cpu2[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x83ff, MRA_RAM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem_cpu2[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x83ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x2000, 0x2fff, glad_cpu_sound_command_r),
                new MemoryReadAddress(0x4000, 0x7fff, MRA_BANK3), /* BANKED ROM */
                new MemoryReadAddress(0x8000, 0xbfff, MRA_BANK4), /* BANKED ROM */
                new MemoryReadAddress(0xc000, 0xffff, MRA_BANK5), /* BANKED ROM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x1000, 0x1fff, glad_adpcm_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort readport[]
            = {
                new IOReadPort(0x02, 0x02, gladiatr_bankswitch_r),
                new IOReadPort(0x9e, 0x9f, TAITO8741_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x01, 0x01, gladiatr_spritebank_w),
                new IOWritePort(0x02, 0x02, gladiatr_bankswitch_w),
                new IOWritePort(0x04, 0x04, gladiatr_irq_patch_w), /* !!! patch to 2nd CPU IRQ !!! */
                new IOWritePort(0x9e, 0x9f, TAITO8741_0_w),
                new IOWritePort(0xbf, 0xbf, IORP_NOP),
                new IOWritePort(-1) /* end of table */};

    static IOReadPort readport_cpu2[]
            = {
                new IOReadPort(0x00, 0x00, YM2203_status_port_0_r),
                new IOReadPort(0x01, 0x01, YM2203_read_port_0_r),
                new IOReadPort(0x20, 0x21, TAITO8741_1_r),
                new IOReadPort(0x40, 0x40, IOWP_NOP),
                new IOReadPort(0x60, 0x61, TAITO8741_2_r),
                new IOReadPort(0x80, 0x81, TAITO8741_3_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort writeport_cpu2[]
            = {
                new IOWritePort(0x00, 0x00, YM2203_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM2203_write_port_0_w),
                new IOWritePort(0x20, 0x21, TAITO8741_1_w),
                new IOWritePort(0x60, 0x61, TAITO8741_2_w),
                new IOWritePort(0x80, 0x81, TAITO8741_3_w),
                /*	new IOWritePort( 0x40, 0x40, glad_sh_irq_clr ), */
                new IOWritePort(0xe0, 0xe0, glad_cpu_sound_command_w),
                new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_gladiatr = new InputPortPtr() {
        public void handler() {
            PORT_START(); 		/* DSW1 (8741-0 parallel port)*/

            PORT_DIPNAME(0x03, 0x01, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x01, "Medium");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x04, 0x04, "After 4 Stages");
            PORT_DIPSETTING(0x04, "Continues");
            PORT_DIPSETTING(0x00, "Ends");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Bonus_Life"));   /*NOTE: Actual manual has these settings reversed(typo?)! */

            PORT_DIPSETTING(0x00, "Only at 100000");
            PORT_DIPSETTING(0x08, "Every 100000");
            PORT_DIPNAME(0x30, 0x20, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x10, "2");
            PORT_DIPSETTING(0x20, "3");
            PORT_DIPSETTING(0x30, "4");
            PORT_DIPNAME(0x40, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x40, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();       /* DSW2  (8741-1 parallel port) - Dips 6 Unused */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x01, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x03, DEF_STR("1C_5C"));
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x0c, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x08, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("2C_1C"));
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x40, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x40, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();       /* DSW3 (YM2203 port B) - Dips 5,6,7 Unused */

            PORT_BITX(0x01, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Invulnerability", IP_KEY_NONE, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x00, "Memory Backup");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x02, "Clear");
            PORT_DIPNAME(0x0c, 0x00, "Starting Stage");
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x04, "2");
            PORT_DIPSETTING(0x08, "3");
            PORT_DIPSETTING(0x0c, "4");
            PORT_SERVICE(0x80, IP_ACTIVE_HIGH);

            PORT_START(); 	/* IN0 (8741-3 parallel port 1) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* COINS */

            PORT_START(); 	/* COINS (8741-3 parallel port bit7) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT_IMPULSE(0x40, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT_IMPULSE(0x80, IP_ACTIVE_HIGH, IPT_COIN2, 1);

            PORT_START(); 	/* IN1 (8741-3 parallel port 2) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* COINS */

            PORT_START(); 	/* IN2 (8741-3 parallel port 4) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* COINS */

            PORT_START(); 	/* IN3 (8741-2 parallel port 1) */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /**
     * ****************************************************************
     */
    static GfxLayout gladiator_text_layout = new GfxLayout/* gfxset 0 */(
                    8, 8, /* 8*8 tiles */
                    1024, /* number of tiles */
                    1, /* bits per pixel */
                    new int[]{0}, /* plane offsets */
                    new int[]{0, 1, 2, 3, 4, 5, 6, 7}, /* x offsets */
                    new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8}, /* y offsets */
                    64 /* offset to next tile */
            );

    /**
     * ****************************************************************
     */
    static GfxLayout gladiator_tile0 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4, 0x08000 * 8, 0x08000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile1 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0, 0x0A000 * 8, 0x0A000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile2 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4 + 0x2000 * 8, 0x10000 * 8, 0x10000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile3 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0 + 0x2000 * 8, 0x12000 * 8, 0x12000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile4 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4 + 0x4000 * 8, 0x0C000 * 8, 0x0C000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile5 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0 + 0x4000 * 8, 0x0E000 * 8, 0x0E000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile6 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4 + 0x6000 * 8, 0x14000 * 8, 0x14000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tile7 = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0 + 0x6000 * 8, 0x16000 * 8, 0x16000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tileA = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4 + 0x2000 * 8, 0x0A000 * 8, 0x0A000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tileB = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0, 0x10000 * 8, 0x10000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tileC = new GfxLayout(
            8, 8, 512, 3,
            new int[]{4 + 0x6000 * 8, 0x0E000 * 8, 0x0E000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );
    static GfxLayout gladiator_tileD = new GfxLayout(
            8, 8, 512, 3,
            new int[]{0 + 0x4000 * 8, 0x14000 * 8, 0x14000 * 8 + 4},
            new int[]{0, 1, 2, 3, 64 + 0, 64 + 1, 64 + 2, 64 + 3},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            128
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                /* monochrome text layer */
                new GfxDecodeInfo(REGION_GFX1, 0x00000, gladiator_text_layout, 512, 1),
                /* background tiles */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile0, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile1, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile2, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile3, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile4, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile5, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile6, 0, 64),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, gladiator_tile7, 0, 64),
                /* sprites */
                new GfxDecodeInfo(REGION_GFX3, 0x00000, gladiator_tile0, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, gladiator_tileB, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, gladiator_tileA, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, gladiator_tile3, 0, 64), /* "GLAD..." */
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tile0, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tileB, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tileA, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tile3, 0, 64), /* ...DIATOR */
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tile4, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tileD, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tileC, 0, 64),
                new GfxDecodeInfo(REGION_GFX3, 0x18000, gladiator_tile7, 0, 64),
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz? */
            new int[]{YM2203_VOL(25, 25)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{gladiator_dsw3_r}, /* port B read */
            new WriteHandlerPtr[]{gladiator_int_controll_w}, /* port A write */
            new WriteHandlerPtr[]{null},
            new WriteYmHandlerPtr[]{gladiator_ym_irq} /* NMI request for 2nd cpu */
    );

    static MSM5205interface msm5205_interface = new MSM5205interface(
            1, /* 1 chip             */
            455000, /* 455KHz ??          */
            new vclk_interruptPtr[]{null}, /* interrupt function */
            new int[]{MSM5205_SEX_4B}, /* vclk input mode    */
            new int[]{60}
    );

    static MachineDriver machine_driver_gladiatr = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        6000000, /* 6 MHz? */
                        readmem, writemem, readport, writeport,
                        interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        3000000, /* 3 MHz? */
                        readmem_cpu2, writemem_cpu2, readport_cpu2, writeport_cpu2,
                        ignore_interrupt, 1
                ),
                new MachineCPU(
                        CPU_M6809 | CPU_AUDIO_CPU,
                        750000, /* 750 kHz (hand tuned) */
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0 /* NMIs are generated by the main CPU */
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* fps, vblank duration */
            10, /* interleaving */
            gladiator_machine_init,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0, 255, 0 + 16, 255 - 16),
            gfxdecodeinfo,
            512 + 2, 512 + 2,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            gladiatr_vh_start,
            gladiatr_vh_stop,
            gladiatr_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                ),
                new MachineSound(
                        SOUND_MSM5205,
                        msm5205_interface
                )
            },
            nvram_handler
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_gladiatr = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);
            ROM_LOAD("qb0-5", 0x00000, 0x4000, 0x25b19efb);
            ROM_LOAD("qb0-4", 0x04000, 0x2000, 0x347ec794);
            ROM_LOAD("qb0-1", 0x10000, 0x4000, 0x040c9839);
            ROM_LOAD("qc0-3", 0x14000, 0x8000, 0x8d182326);

            ROM_REGION(0x10000, REGION_CPU2);/* Code for the 2nd CPU */

            ROM_LOAD("qb0-17", 0x0000, 0x4000, 0xe78be010);

            ROM_REGION(0x28000, REGION_CPU3); /* 6809 Code & ADPCM data */

            ROM_LOAD("qb0-20", 0x10000, 0x8000, 0x15916eda);
            ROM_LOAD("qb0-19", 0x18000, 0x8000, 0x79caa7ed);
            ROM_LOAD("qb0-18", 0x20000, 0x8000, 0xe9591260);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("qc0-15", 0x00000, 0x2000, 0xa7efa340);/* (monochrome) */

            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("qb0-12", 0x00000, 0x8000, 0x0585d9ac);/* plane 3 */

            ROM_LOAD("qb0-13", 0x08000, 0x8000, 0xa6bb797b);/* planes 1,2 */

            ROM_LOAD("qb0-14", 0x10000, 0x8000, 0x85b71211);/* planes 1,2 */

            ROM_REGION(0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("qc1-6", 0x00000, 0x4000, 0x651e6e44);/* plane 3 */

            ROM_LOAD("qc0-8", 0x08000, 0x4000, 0x1c7ffdad);/* planes 1,2 */

            ROM_LOAD("qc1-9", 0x10000, 0x4000, 0x01043e03);/* planes 1,2 */

            ROM_LOAD("qc2-7", 0x18000, 0x8000, 0xc992c4f7);/* plane 3 */

            ROM_LOAD("qc1-10", 0x20000, 0x8000, 0x364cdb58);/* planes 1,2 */

            ROM_LOAD("qc2-11", 0x28000, 0x8000, 0xc9fecfff);/* planes 1,2 */

            ROM_END();
        }
    };

    static RomLoadPtr rom_ogonsiro = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x1c000, REGION_CPU1);
            ROM_LOAD("qb0-5", 0x00000, 0x4000, 0x25b19efb);
            ROM_LOAD("qb0-4", 0x04000, 0x2000, 0x347ec794);
            ROM_LOAD("qb0-1", 0x10000, 0x4000, 0x040c9839);
            ROM_LOAD("qb0_3", 0x14000, 0x8000, 0xd6a342e7);

            ROM_REGION(0x10000, REGION_CPU2);/* Code for the 2nd CPU */

            ROM_LOAD("qb0-17", 0x0000, 0x4000, 0xe78be010);

            ROM_REGION(0x28000, REGION_CPU3); /* 6809 Code & ADPCM data */

            ROM_LOAD("qb0-20", 0x10000, 0x8000, 0x15916eda);
            ROM_LOAD("qb0-19", 0x18000, 0x8000, 0x79caa7ed);
            ROM_LOAD("qb0-18", 0x20000, 0x8000, 0xe9591260);

            ROM_REGION(0x02000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("qb0_15", 0x00000, 0x2000, 0x5e1332b8);/* (monochrome) */

            ROM_REGION(0x18000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* tiles */

            ROM_LOAD("qb0-12", 0x00000, 0x8000, 0x0585d9ac);/* plane 3 */

            ROM_LOAD("qb0-13", 0x08000, 0x8000, 0xa6bb797b);/* planes 1,2 */

            ROM_LOAD("qb0-14", 0x10000, 0x8000, 0x85b71211);/* planes 1,2 */

            ROM_REGION(0x30000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("qb0_6", 0x00000, 0x4000, 0x1a2bc769);/* plane 3 */

            ROM_LOAD("qc0-8", 0x08000, 0x4000, 0x1c7ffdad);/* planes 1,2 */

            ROM_LOAD("qb0_9", 0x10000, 0x4000, 0x38f5152d);/* planes 1,2 */

            ROM_LOAD("qb0_7", 0x18000, 0x8000, 0x4b677bd9);/* plane 3 */

            ROM_LOAD("qb0_10", 0x20000, 0x8000, 0x87ab6cc4);/* planes 1,2 */

            ROM_LOAD("qb0_11", 0x28000, 0x8000, 0x25eaa4ff);/* planes 1,2 */

            ROM_END();
        }
    };

    public static GameDriver driver_gladiatr = new GameDriver("1986", "gladiatr", "gladiatr.java", rom_gladiatr, null, machine_driver_gladiatr, input_ports_gladiatr, null, ROT0, "Taito America Corporation", "Gladiator (US)", GAME_NO_COCKTAIL);
    public static GameDriver driver_ogonsiro = new GameDriver("1986", "ogonsiro", "gladiatr.java", rom_ogonsiro, driver_gladiatr, machine_driver_gladiatr, input_ports_gladiatr, null, ROT0, "Taito Corporation", "Ohgon no Siro (Japan)", GAME_NO_COCKTAIL);
}
