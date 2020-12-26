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
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound._3812intf.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.machine.twincobr.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.twincobr.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.crtc6845.*;
import static gr.codebb.arcadeflex.v037b7.mame.memory.install_mem_read_handler;
import static gr.codebb.arcadeflex.v037b7.mame.memory.mrh_bank1;
import static gr.codebb.arcadeflex.v037b7.mame.memory.mrh_bank2;
import static gr.codebb.arcadeflex.v037b7.mame.memory.mrh_bank3;

public class wardner {

    static int wardner_membank = 0;

    public static InterruptPtr wardner_interrupt = new InterruptPtr() {
        public int handler() {
            if (twincobr_intenable!=0) {
                twincobr_intenable = 0;
                return interrupt.handler();
            } else {
                return ignore_interrupt.handler();
            }
        }
    };

    public static WriteHandlerPtr CRTC_add_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            crtc6845_address_w.handler(offset, data);
        }
    };

    public static WriteHandlerPtr CRTC_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            crtc6845_register_w.handler(0, data);
            twincobr_display_on = 1;
        }
    };

    public static ReadHandlerPtr wardner_sprite_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return spriteram.read(offset);
        }
    };

    public static WriteHandlerPtr wardner_sprite_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            spriteram.write(offset, data);
        }
    };

    public static WriteHandlerPtr wardner_ramrom_banksw = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (wardner_membank != data) {
                int bankaddress = 0;

                UBytePtr RAM = memory_region(REGION_CPU1);

                wardner_membank = data;

                if (data != 0) {
                    install_mem_read_handler(0, 0x8000, 0xffff, mrh_bank1);
                    switch (data) {
                        case 2:
                            bankaddress = 0x10000;
                            break;
                        case 3:
                            bankaddress = 0x18000;
                            break;
                        case 4:
                            bankaddress = 0x20000;
                            break;
                        case 5:
                            bankaddress = 0x28000;
                            break;
                        case 7:
                            bankaddress = 0x38000;
                            break;
                        case 1:
                            bankaddress = 0x08000;
                            break; /* not used */

                        case 6:
                            bankaddress = 0x30000;
                            break; /* not used */

                        default:
                            bankaddress = 0x00000;
                            break; /* not used */

                    }
                    cpu_setbank(1, new UBytePtr(RAM, bankaddress));
                } else {
                    cpu_setbank(1, new UBytePtr(RAM, 0x0000));
                    install_mem_read_handler(0, 0x8000, 0x8fff, wardner_sprite_r);
                    install_mem_read_handler(0, 0xa000, 0xadff, paletteram_r);
                    install_mem_read_handler(0, 0xae00, 0xafff, mrh_bank2);
                    install_mem_read_handler(0, 0xc000, 0xc7ff, mrh_bank3);
                }
            }
        }
    };

    static MemoryReadAddress readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x6fff, MRA_ROM), /* Main CPU ROM code */
                new MemoryReadAddress(0x7000, 0x7fff, wardner_mainram_r), /* Main RAM */
                new MemoryReadAddress(0x8000, 0x8fff, wardner_sprite_r), /* Sprite RAM data */
                new MemoryReadAddress(0x9000, 0x9fff, MRA_ROM), /* Banked ROM */
                new MemoryReadAddress(0xa000, 0xadff, paletteram_r), /* Palette RAM */
                new MemoryReadAddress(0xae00, 0xafff, MRA_BANK2), /* Unused Palette RAM */
                new MemoryReadAddress(0xb000, 0xbfff, MRA_ROM), /* Banked ROM */
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_BANK3), /* Shared RAM with Sound CPU RAM */
                new MemoryReadAddress(0xc800, 0xffff, MRA_ROM), /* Banked ROM */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x6fff, MWA_ROM),
                new MemoryWriteAddress(0x7000, 0x7fff, wardner_mainram_w, wardner_mainram),
                new MemoryWriteAddress(0x8000, 0x8fff, wardner_sprite_w, spriteram, spriteram_size),
                new MemoryWriteAddress(0x9000, 0x9fff, MWA_ROM),
                new MemoryWriteAddress(0xa000, 0xadff, paletteram_xBBBBBGGGGGRRRRR_w, paletteram),
                new MemoryWriteAddress(0xae00, 0xafff, MWA_BANK2),
                new MemoryWriteAddress(0xb000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_BANK3),
                new MemoryWriteAddress(0xc800, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
                new MemoryReadAddress(0x8000, 0x807f, MRA_BANK4),
                new MemoryReadAddress(0xc000, 0xc7ff, MRA_BANK3),
                new MemoryReadAddress(0xc800, 0xcfff, MRA_BANK5),
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
                new MemoryWriteAddress(0x8000, 0x807f, MWA_BANK4),
                new MemoryWriteAddress(0xc000, 0xc7ff, MWA_BANK3),
                new MemoryWriteAddress(0xc800, 0xcfff, MWA_BANK5),
                new MemoryWriteAddress(-1)
            };

    static MemoryReadAddress DSP_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x0bff, MRA_ROM), /* 0x600 words */
                new MemoryReadAddress(0x8000, 0x811f, MRA_RAM), /* The real DSP has this at address 0 */
                /* View this at 4000h in the debugger */
                new MemoryReadAddress(-1)
            };

    static MemoryWriteAddress DSP_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x0bff, MWA_ROM), /* 0x600 words */
                new MemoryWriteAddress(0x8000, 0x811f, MWA_RAM), /* The real DSP has this at address 0 */
                /* View this at 4000h in the debugger */
                new MemoryWriteAddress(-1)
            };

    static IOReadPort readport[]
            = {
                new IOReadPort(0x50, 0x50, input_port_3_r), /* DSW A */
                new IOReadPort(0x52, 0x52, input_port_4_r), /* DSW B */
                new IOReadPort(0x54, 0x54, input_port_1_r), /* Player 1 */
                new IOReadPort(0x56, 0x56, input_port_2_r), /* Player 2 */
                new IOReadPort(0x58, 0x58, input_port_0_r), /* V-Blank/Coin/Start */
                new IOReadPort(0x60, 0x65, wardner_videoram_r), /* data from video layer RAM */
                new IOReadPort(-1)
            };

    static IOWritePort writeport[]
            = {
                new IOWritePort(0x00, 0x02, twincobr_crtc_w),
                new IOWritePort(0x10, 0x13, wardner_txscroll_w), /* scroll text layer */
                new IOWritePort(0x14, 0x15, wardner_txlayer_w), /* offset in text video RAM */
                new IOWritePort(0x20, 0x23, wardner_bgscroll_w), /* scroll bg layer */
                new IOWritePort(0x24, 0x25, wardner_bglayer_w), /* offset in bg video RAM */
                new IOWritePort(0x30, 0x33, wardner_fgscroll_w), /* scroll fg layer */
                new IOWritePort(0x34, 0x35, wardner_fglayer_w), /* offset in fg video RAM */
                new IOWritePort(0x40, 0x43, twincobr_exscroll_w), /* scroll extra layer (not used) */
                new IOWritePort(0x60, 0x65, wardner_videoram_w), /* data for video layer RAM */
                new IOWritePort(0x5a, 0x5a, fshark_coin_dsp_w), /* Machine system control */
                new IOWritePort(0x5c, 0x5c, twincobr_7800c_w), /* Machine system control */
                new IOWritePort(0x70, 0x70, wardner_ramrom_banksw), /* ROM bank select */
                new IOWritePort(-1)
            };

    static IOReadPort sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, YM3812_status_port_0_r),
                new IOReadPort(-1)
            };

    static IOWritePort sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM3812_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM3812_write_port_0_w),
                new IOWritePort(-1)
            };

    static IOReadPort DSP_readport[]
            = {
                new IOReadPort(0x01, 0x01, twincobr_dsp_in),
                new IOReadPort(-1)
            };
    static IOWritePort DSP_writeport[]
            = {
                new IOWritePort(0x00, 0x03, twincobr_dsp_out),
                new IOWritePort(-1)
            };

    /**
     * ***************************************************************************
     * Input Port definitions
     *
     * There is a test mode for button/switch tests. To enter Test mode, set the
     * Cross Hatch Pattern DSW to on, restart and then press player 1 start
     * button when in the cross-hatch screen.
	****************************************************************************
     */
    static InputPortPtr input_ports_wardner = new InputPortPtr() {
        public void handler() {
            PORT_START(); 				/* test button doesnt seem to do anything ? */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);	/* Service button */

            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNUSED);/* Test button */

            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);/* V-Blank */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1);/* Shot D */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2);/* Shot D */

            PORT_START(); 		/* DSW A */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, "Cross Hatch Pattern");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x30, DEF_STR("4C_1C"));
            PORT_DIPSETTING(0x20, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x10, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x80, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0xc0, DEF_STR("1C_6C"));

            PORT_START(); 		/* DSW B */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 & 80000");
            PORT_DIPSETTING(0x04, "50000 & 100000");
            PORT_DIPSETTING(0x08, "30000");
            PORT_DIPSETTING(0x0c, "50000");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "1");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pyros = new InputPortPtr() {
        public void handler() {
            PORT_START(); 				/* test button doesnt seem to do anything ? */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);	/* Service button */

            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNUSED);/* Test button */

            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);/* V-Blank */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1);/* Shot D */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2);/* Shot D */

            PORT_START(); 		/* DSW A */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, "Cross Hatch Pattern");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));

            PORT_START(); 		/* DSW B */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 & 80000");
            PORT_DIPSETTING(0x04, "50000 & 100000");
            PORT_DIPSETTING(0x08, "50000");
            PORT_DIPSETTING(0x0c, "100000");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "1");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            PORT_DIPNAME(0x40, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x40, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_wardnerj = new InputPortPtr() {
        public void handler() {
            PORT_START(); 				/* test button doesnt seem to do anything ? */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN3);	/* Service button */

            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_TILT);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNUSED);/* Test button */

            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_VBLANK);/* V-Blank */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1);/* Shot D */

            PORT_START();  				/* Player 1 button 3 skips video RAM tests */

            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2);/* Fire */

            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2);/* Jump */

            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2);/* Shot C */

            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2);/* Shot D */

            PORT_START(); 		/* DSW A */

            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x02, DEF_STR("On"));
            PORT_DIPNAME(0x04, 0x00, "Cross Hatch Pattern");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x20, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x30, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_2C"));
            PORT_DIPNAME(0xc0, 0x00, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x80, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0xc0, DEF_STR("2C_3C"));
            PORT_DIPSETTING(0x40, DEF_STR("1C_2C"));

            PORT_START(); 		/* DSW B */

            PORT_DIPNAME(0x03, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x01, "Easy");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x02, "Hard");
            PORT_DIPSETTING(0x03, "Hardest");
            PORT_DIPNAME(0x0c, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "30000 & 80000");
            PORT_DIPSETTING(0x04, "50000 & 100000");
            PORT_DIPSETTING(0x08, "30000");
            PORT_DIPSETTING(0x0c, "50000");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x30, "1");
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            INPUT_PORTS_END();
        }
    };

    static GfxLayout charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            3, /* 3 bits per pixel */
            new int[]{0 * 2048 * 8 * 8, 1 * 2048 * 8 * 8, 2 * 2048 * 8 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxLayout tilelayout = new GfxLayout(
            8, 8, /* 8*8 tiles */
            4096, /* 4096 tiles */
            4, /* 4 bits per pixel */
            new int[]{0 * 4096 * 8 * 8, 1 * 4096 * 8 * 8, 2 * 4096 * 8 * 8, 3 * 4096 * 8 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every tile takes 8 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            2048, /* 2048 sprites */
            4, /* 4 bits per pixel */
            new int[]{0 * 2048 * 32 * 8, 1 * 2048 * 32 * 8, 2 * 2048 * 32 * 8, 3 * 2048 * 32 * 8}, /* the bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                8 * 16, 9 * 16, 10 * 16, 11 * 16, 12 * 16, 13 * 16, 14 * 16, 15 * 16},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    /* handler called by the 3812 emulator when the internal timers cause an IRQ */
    public static WriteYmHandlerPtr irqhandler = new WriteYmHandlerPtr() {
        public void handler(int linestate) {
            cpu_set_irq_line(1, 0, linestate);
        }
    };

    static YM3812interface ym3812_interface = new YM3812interface(
            1, /* 1 chip */
            24000000 / 7, /* 3.43 MHz ??? */
            new int[]{100}, /* volume */
            new WriteYmHandlerPtr[]{irqhandler}
    );

    static GfxDecodeInfo gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, charlayout, 1536, 32), /* colors 1536-1791 */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, tilelayout, 1280, 16), /* colors 1280-1535 */
                new GfxDecodeInfo(REGION_GFX3, 0x00000, tilelayout, 1024, 16), /* colors 1024-1079 */
                new GfxDecodeInfo(REGION_GFX4, 0x00000, spritelayout, 0, 64), /* colors    0-1023 */
                new GfxDecodeInfo(-1) /* end of array */};

    static MachineDriver machine_driver_wardner = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80,
                        24000000 / 4, /* 6 MHz ??? - Real board crystal is 24Mhz */
                        readmem, writemem,
                        readport,
                        writeport,
                        wardner_interrupt, 1
                ),
                new MachineCPU(
                        CPU_Z80,
                        24000000 / 7, /* 3.43 MHz ??? */
                        sound_readmem, sound_writemem,
                        sound_readport, sound_writeport,
                        ignore_interrupt, 0 /* IRQs are caused by the YM3812 */
                ),
                new MachineCPU(
                        CPU_TMS320C10,
                        24000000 / 7, /* 3.43 MHz ??? */
                        DSP_readmem, DSP_writemem,
                        DSP_readport, DSP_writeport,
                        ignore_interrupt, 0 /* IRQs are caused by Z80(0) */
                ),},
            56, DEFAULT_REAL_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            100, /* 100 CPU slices per frame */
            wardner_reset,
            /* video hardware */
            64 * 8, 32 * 8, new rectangle(0 * 8, 40 * 8 - 1, 0 * 8, 30 * 8 - 1),
            gfxdecodeinfo,
            1792, 1792,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_BUFFERS_SPRITERAM,
            twincobr_eof_callback,
            twincobr_vh_start,
            twincobr_vh_stop,
            twincobr_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),}
    );

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_wardner = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);/* Banked Main Z80 code */

            ROM_LOAD("wardner.17", 0x00000, 0x08000, 0xc5dd56fd);/* Main Z80 code */

            ROM_LOAD("b25-18.rom", 0x18000, 0x10000, 0x9aab8ee2);/* OBJ ROMs */

            ROM_LOAD("b25-19.rom", 0x28000, 0x10000, 0x95b68813);
            ROM_LOAD("wardner.20", 0x40000, 0x08000, 0x347f411b);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */

            ROM_LOAD("b25-16.rom", 0x00000, 0x08000, 0xe5202ff8);

            ROM_REGION(0x10000, REGION_CPU3);/* Co-Processor TMS320C10 MCU code */

            //LSB_FIRST

            ROM_LOAD_NIB_HIGH( "82s137.1d",  0x1000, 0x0400, 0xcc5b3f53 ); /* msb */
            ROM_LOAD_NIB_LOW ( "82s137.1e",  0x1000, 0x0400, 0x47351d55 );
            ROM_LOAD_NIB_HIGH( "82s131.3b",  0x1400, 0x0200, 0x9dfffaff );
            ROM_LOAD_NIB_LOW ( "82s131.3a",  0x1400, 0x0200, 0x712bad47 );
            ROM_LOAD_NIB_HIGH( "82s137.3d",  0x1800, 0x0400, 0x70b537b9 ); /* lsb */
            ROM_LOAD_NIB_LOW ( "82s137.3e",  0x1800, 0x0400, 0x6edb2de8 );
            ROM_LOAD_NIB_HIGH( "82s131.2a",  0x1c00, 0x0200, 0xac843ca6 );
            ROM_LOAD_NIB_LOW ( "82s131.1a",  0x1c00, 0x0200, 0x50452ff8 );

            ROM_REGION(0x0c000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* chars */

            ROM_LOAD("wardner.07", 0x00000, 0x04000, 0x1392b60d);
            ROM_LOAD("wardner.06", 0x04000, 0x04000, 0x0ed848da);
            ROM_LOAD("wardner.05", 0x08000, 0x04000, 0x79792c86);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* fg tiles */

            ROM_LOAD("b25-12.rom", 0x00000, 0x08000, 0x15d08848);
            ROM_LOAD("b25-15.rom", 0x08000, 0x08000, 0xcdd2d408);
            ROM_LOAD("b25-14.rom", 0x10000, 0x08000, 0x5a2aef4f);
            ROM_LOAD("b25-13.rom", 0x18000, 0x08000, 0xbe21db2b);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* bg tiles */

            ROM_LOAD("b25-08.rom", 0x00000, 0x08000, 0x883ccaa3);
            ROM_LOAD("b25-11.rom", 0x08000, 0x08000, 0xd6ebd510);
            ROM_LOAD("b25-10.rom", 0x10000, 0x08000, 0xb9a61e81);
            ROM_LOAD("b25-09.rom", 0x18000, 0x08000, 0x585411b7);

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("b25-01.rom", 0x00000, 0x10000, 0x42ec01fb);
            ROM_LOAD("b25-02.rom", 0x10000, 0x10000, 0x6c0130b7);
            ROM_LOAD("b25-03.rom", 0x20000, 0x10000, 0xb923db99);
            ROM_LOAD("b25-04.rom", 0x30000, 0x10000, 0x8059573c);

            ROM_REGION(0x260, REGION_PROMS);	/* nibble bproms, lo/hi order to be determined */

            ROM_LOAD("82s129.b19", 0x000, 0x100, 0x24e7d62f);/* sprite priority control ?? */

            ROM_LOAD("82s129.b18", 0x100, 0x100, 0xa50cef09);/* sprite priority control ?? */

            ROM_LOAD("82s123.b21", 0x200, 0x020, 0xf72482db);/* sprite control ?? */

            ROM_LOAD("82s123.c6", 0x220, 0x020, 0xbc88cced);/* sprite attribute (flip/position) ?? */

            ROM_LOAD("82s123.f1", 0x240, 0x020, 0x4fb5df2a);/* tile to sprite priority ?? */

            ROM_END();
        }
    };

    static RomLoadPtr rom_pyros = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);/* Banked Z80 code */

            ROM_LOAD("b25-29.rom", 0x00000, 0x08000, 0xb568294d);/* Main Z80 code */

            ROM_LOAD("b25-18.rom", 0x18000, 0x10000, 0x9aab8ee2);/* OBJ ROMs */

            ROM_LOAD("b25-19.rom", 0x28000, 0x10000, 0x95b68813);
            ROM_LOAD("b25-30.rom", 0x40000, 0x08000, 0x5056c799);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */

            ROM_LOAD("b25-16.rom", 0x00000, 0x08000, 0xe5202ff8);

            ROM_REGION(0x10000, REGION_CPU3);/* Co-Processor TMS320C10 MCU code */

            //LSB_FIRST

            ROM_LOAD_NIB_HIGH( "82s137.1d",  0x1000, 0x0400, 0xcc5b3f53 ); /* msb */
            ROM_LOAD_NIB_LOW ( "82s137.1e",  0x1000, 0x0400, 0x47351d55 );
            ROM_LOAD_NIB_HIGH( "82s131.3b",  0x1400, 0x0200, 0x9dfffaff );
            ROM_LOAD_NIB_LOW ( "82s131.3a",  0x1400, 0x0200, 0x712bad47 );
            ROM_LOAD_NIB_HIGH( "82s137.3d",  0x1800, 0x0400, 0x70b537b9 );/* lsb */
            ROM_LOAD_NIB_LOW ( "82s137.3e",  0x1800, 0x0400, 0x6edb2de8 );
            ROM_LOAD_NIB_HIGH( "82s131.2a",  0x1c00, 0x0200, 0xac843ca6 );
            ROM_LOAD_NIB_LOW ( "82s131.1a",  0x1c00, 0x0200, 0x50452ff8 );

            ROM_REGION(0x0c000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* chars */

            ROM_LOAD("b25-35.rom", 0x00000, 0x04000, 0xfec6f0c0);
            ROM_LOAD("b25-34.rom", 0x04000, 0x04000, 0x02505dad);
            ROM_LOAD("b25-33.rom", 0x08000, 0x04000, 0x9a55fcb9);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* fg tiles */

            ROM_LOAD("b25-12.rom", 0x00000, 0x08000, 0x15d08848);
            ROM_LOAD("b25-15.rom", 0x08000, 0x08000, 0xcdd2d408);
            ROM_LOAD("b25-14.rom", 0x10000, 0x08000, 0x5a2aef4f);
            ROM_LOAD("b25-13.rom", 0x18000, 0x08000, 0xbe21db2b);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* bg tiles */

            ROM_LOAD("b25-08.rom", 0x00000, 0x08000, 0x883ccaa3);
            ROM_LOAD("b25-11.rom", 0x08000, 0x08000, 0xd6ebd510);
            ROM_LOAD("b25-10.rom", 0x10000, 0x08000, 0xb9a61e81);
            ROM_LOAD("b25-09.rom", 0x18000, 0x08000, 0x585411b7);

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("b25-01.rom", 0x00000, 0x10000, 0x42ec01fb);
            ROM_LOAD("b25-02.rom", 0x10000, 0x10000, 0x6c0130b7);
            ROM_LOAD("b25-03.rom", 0x20000, 0x10000, 0xb923db99);
            ROM_LOAD("b25-04.rom", 0x30000, 0x10000, 0x8059573c);

            ROM_REGION(0x260, REGION_PROMS);	/* nibble bproms, lo/hi order to be determined */

            ROM_LOAD("82s129.b19", 0x000, 0x100, 0x24e7d62f);/* sprite priority control ?? */

            ROM_LOAD("82s129.b18", 0x100, 0x100, 0xa50cef09);/* sprite priority control ?? */

            ROM_LOAD("82s123.b21", 0x200, 0x020, 0xf72482db);/* sprite control ?? */

            ROM_LOAD("82s123.c6", 0x220, 0x020, 0xbc88cced);/* sprite attribute (flip/position) ?? */

            ROM_LOAD("82s123.f1", 0x240, 0x020, 0x4fb5df2a);/* tile to sprite priority ?? */

            ROM_END();
        }
    };

    static RomLoadPtr rom_wardnerj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x48000, REGION_CPU1);/* Banked Z80 code */

            ROM_LOAD("b25-17.bin", 0x00000, 0x08000, 0x4164dca9);/* Main Z80 code */

            ROM_LOAD("b25-18.rom", 0x18000, 0x10000, 0x9aab8ee2);/* OBJ ROMs */

            ROM_LOAD("b25-19.rom", 0x28000, 0x10000, 0x95b68813);
            ROM_LOAD("b25-20.bin", 0x40000, 0x08000, 0x1113ad38);

            ROM_REGION(0x10000, REGION_CPU2);/* Sound Z80 code */

            ROM_LOAD("b25-16.rom", 0x00000, 0x08000, 0xe5202ff8);

            ROM_REGION(0x10000, REGION_CPU3);/* Co-Processor TMS320C10 MCU code */

            //LSB_FIRST

            ROM_LOAD_NIB_HIGH( "82s137.1d",  0x1000, 0x0400, 0xcc5b3f53 ); /* msb */
            ROM_LOAD_NIB_LOW ( "82s137.1e",  0x1000, 0x0400, 0x47351d55 );
            ROM_LOAD_NIB_HIGH( "82s131.3b",  0x1400, 0x0200, 0x9dfffaff );
            ROM_LOAD_NIB_LOW ( "82s131.3a",  0x1400, 0x0200, 0x712bad47 );
            ROM_LOAD_NIB_HIGH( "82s137.3d",  0x1800, 0x0400, 0x70b537b9 );/* lsb */
            ROM_LOAD_NIB_LOW ( "82s137.3e",  0x1800, 0x0400, 0x6edb2de8 );
            ROM_LOAD_NIB_HIGH( "82s131.2a",  0x1c00, 0x0200, 0xac843ca6 );
            ROM_LOAD_NIB_LOW ( "82s131.1a",  0x1c00, 0x0200, 0x50452ff8 );

            ROM_REGION(0x0c000, REGION_GFX1 | REGIONFLAG_DISPOSE);/* chars */

            ROM_LOAD("b25-07.bin", 0x00000, 0x04000, 0x50e329e0);
            ROM_LOAD("b25-06.bin", 0x04000, 0x04000, 0x3bfeb6ae);
            ROM_LOAD("b25-05.bin", 0x08000, 0x04000, 0xbe36a53e);

            ROM_REGION(0x20000, REGION_GFX2 | REGIONFLAG_DISPOSE);/* fg tiles */

            ROM_LOAD("b25-12.rom", 0x00000, 0x08000, 0x15d08848);
            ROM_LOAD("b25-15.rom", 0x08000, 0x08000, 0xcdd2d408);
            ROM_LOAD("b25-14.rom", 0x10000, 0x08000, 0x5a2aef4f);
            ROM_LOAD("b25-13.rom", 0x18000, 0x08000, 0xbe21db2b);

            ROM_REGION(0x20000, REGION_GFX3 | REGIONFLAG_DISPOSE);/* bg tiles */

            ROM_LOAD("b25-08.rom", 0x00000, 0x08000, 0x883ccaa3);
            ROM_LOAD("b25-11.rom", 0x08000, 0x08000, 0xd6ebd510);
            ROM_LOAD("b25-10.rom", 0x10000, 0x08000, 0xb9a61e81);
            ROM_LOAD("b25-09.rom", 0x18000, 0x08000, 0x585411b7);

            ROM_REGION(0x40000, REGION_GFX4 | REGIONFLAG_DISPOSE);/* sprites */

            ROM_LOAD("b25-01.rom", 0x00000, 0x10000, 0x42ec01fb);
            ROM_LOAD("b25-02.rom", 0x10000, 0x10000, 0x6c0130b7);
            ROM_LOAD("b25-03.rom", 0x20000, 0x10000, 0xb923db99);
            ROM_LOAD("b25-04.rom", 0x30000, 0x10000, 0x8059573c);

            ROM_REGION(0x260, REGION_PROMS);	/* nibble bproms, lo/hi order to be determined */

            ROM_LOAD("82s129.b19", 0x000, 0x100, 0x24e7d62f);/* sprite priority control ?? */

            ROM_LOAD("82s129.b18", 0x100, 0x100, 0xa50cef09);/* sprite priority control ?? */

            ROM_LOAD("82s123.b21", 0x200, 0x020, 0xf72482db);/* sprite control ?? */

            ROM_LOAD("82s123.c6", 0x220, 0x020, 0xbc88cced);/* sprite attribute (flip/position) ?? */

            ROM_LOAD("82s123.f1", 0x240, 0x020, 0x4fb5df2a);/* tile to sprite priority ?? */

            ROM_END();
        }
    };

    public static InitDriverPtr init_wardner = new InitDriverPtr() {
        public void handler() {
            int A;
            /*unsigned*/ char datamsb;
            /*unsigned*/ char datalsb;

            UBytePtr DSP_ROMS = memory_region(REGION_CPU3);

            /* The ROM loader fixes the nibble images. Here we fix the byte ordering. */
            for (A = 0; A < 0x0600; A++) {
                datamsb = DSP_ROMS.read(0x1000 + A);
                datalsb = DSP_ROMS.read(0x1800 + A);
                DSP_ROMS.write((A * 2), datamsb & 0xFF);
                DSP_ROMS.write((A * 2) + 1, datalsb & 0xFF);

                DSP_ROMS.write(0x1000 + A, 00);
                DSP_ROMS.write(0x1800 + A, 00);
            }
        }
    };

    public static GameDriver driver_wardner = new GameDriver("1987", "wardner", "wardner.java", rom_wardner, null, machine_driver_wardner, input_ports_wardner, init_wardner, ROT0, "[Toaplan] Taito Corporation Japan", "Wardner (World)");
    public static GameDriver driver_pyros = new GameDriver("1987", "pyros", "wardner.java", rom_pyros, driver_wardner, machine_driver_wardner, input_ports_pyros, init_wardner, ROT0, "[Toaplan] Taito America Corporation", "Pyros (US)");
    public static GameDriver driver_wardnerj = new GameDriver("1987", "wardnerj", "wardner.java", rom_wardnerj, driver_wardner, machine_driver_wardner, input_ports_wardnerj, init_wardner, ROT0, "[Toaplan] Taito Corporation", "Wardna no Mori (Japan)");

}
