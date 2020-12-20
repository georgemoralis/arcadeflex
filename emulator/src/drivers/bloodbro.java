/**
 * ************************************************************************
 * Blood Bros, West Story. TAD Corporation 1990/Datsu 1991 68000 + Z80 + YM3931
 * + YM3812
 *
 * driver by Carlos A. Lozano Baides
 *
 * TODO:
 *
 * (*) Global - Sprites priorities/clip. - Fix timing?? Foreground layer
 * flickers during the game!!!! - hiscore.
 *
 * (*) Blood Bros
 *
 * (*) West Story - Sprites problems. (decode problems?)
 *
 * 27.10.99: Added sound - Bryan McPhail.
 *
 *************************************************************************
 */

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 */
package drivers;

import static platform.ptrlib.*;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.cpuintrfH.*;
import static mame.inputportH.*;
import static mame.mame.*;
import static platform.libc_old.*;
import static platform.libc.*;
import static mame.sndintrf.*;
import static cpu.konami.konamiH.*;
import static cpu.konami.konami.*;
import static cpu.z80.z80H.*;
import static mame.common.*;
import static mame.commonH.*;
import static mame.palette.*;
import static mame.memory.*;
import mame.sndintrfH.MachineSound;
import static mame.sndintrfH.SOUND_OKIM6295;
import static mame.sndintrfH.SOUND_YM3812;
import static sndhrdw.seibu.*;
import static sound._3812intf.*;
import static sound._3812intfH.*;
import static sound.okim6295.*;
import static sound.okim6295H.*;
import static vidhrdw.bloodbro.*;


public class bloodbro {

    /**
     * ************************************************************************
     */
    public static ReadHandlerPtr bloodbro_r_read = new ReadHandlerPtr() {
        public int handler(int offset) {
            //if( errorlog ) fprintf( errorlog, "INPUT e000[%x] \n", offset);
            switch (offset) {
                case 0x0: /* DIPSW 1&2 */

                    return readinputport(4) + readinputport(5) * 256;
                case 0x2: /* IN1-IN2 */

                    return readinputport(0) + readinputport(1) * 256;
                case 0x4: /* ???-??? */

                    return readinputport(2) + readinputport(3) * 256;
                default:
                    return (0xffff);
            }
        }
    };

    public static WriteHandlerPtr bloodbro_sound_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Slightly different interface in this game */
            if (offset == 0) {
                seibu_soundlatch_w.handler(0, data & 0xff); /* Convert 16 bit write to 8 bit */
            } else if (offset == 2) {
                seibu_soundlatch_w.handler(2, data & 0xff); /* Convert 16 bit write to 8 bit */
            } else if (offset != 2) {
                seibu_soundlatch_w.handler(offset, data & 0xff);
            }
        }
    };

    public static ReadHandlerPtr bloodbro_sound_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0x0060; /* Always return sound cpu ready */

        }
    };

    /**
     * ** Blood Bros Memory Map  ******************************************
     */
    static MemoryReadAddress readmem_cpu[] = {
        new MemoryReadAddress(0x00000, 0x7ffff, MRA_ROM),
        new MemoryReadAddress(0x80000, 0x8afff, MRA_RAM),
        new MemoryReadAddress(0x8b000, 0x8bfff, MRA_RAM),
        new MemoryReadAddress(0x8c000, 0x8c3ff, bloodbro_background_r),
        new MemoryReadAddress(0x8c400, 0x8cfff, MRA_RAM),
        new MemoryReadAddress(0x8d000, 0x8d3ff, bloodbro_foreground_r),
        new MemoryReadAddress(0x8d400, 0x8d7ff, MRA_RAM),
        new MemoryReadAddress(0x8d800, 0x8dfff, MRA_RAM),
        new MemoryReadAddress(0x8e000, 0x8e7ff, MRA_RAM),
        new MemoryReadAddress(0x8e800, 0x8f7ff, paletteram_word_r),
        new MemoryReadAddress(0x8f800, 0x8ffff, MRA_RAM),
        new MemoryReadAddress(0xa0000, 0xa001f, bloodbro_sound_r),
        new MemoryReadAddress(0xc0000, 0xc007f, MRA_BANK3),
        new MemoryReadAddress(0xe0000, 0xe000f, bloodbro_r_read),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress writemem_cpu[] = {
        new MemoryWriteAddress(0x00000, 0x7ffff, MWA_ROM),
        new MemoryWriteAddress(0x80000, 0x8afff, MWA_RAM),
        new MemoryWriteAddress(0x8b000, 0x8bfff, MWA_RAM, spriteram),
        new MemoryWriteAddress(0x8c000, 0x8c3ff, bloodbro_background_w, videoram), /* Background RAM */
        new MemoryWriteAddress(0x8c400, 0x8cfff, MWA_RAM),
        new MemoryWriteAddress(0x8d000, 0x8d3ff, bloodbro_foreground_w, bloodbro_videoram2), /* Foreground RAM */
        new MemoryWriteAddress(0x8d400, 0x8d7ff, MWA_RAM),
        new MemoryWriteAddress(0x8d800, 0x8dfff, MWA_RAM, textlayoutram),
        new MemoryWriteAddress(0x8e000, 0x8e7ff, MWA_RAM),
        new MemoryWriteAddress(0x8e800, 0x8f7ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram),
        new MemoryWriteAddress(0x8f800, 0x8ffff, MWA_RAM),
        new MemoryWriteAddress(0xa0000, 0xa001f, bloodbro_sound_w, seibu_shared_sound_ram),
        new MemoryWriteAddress(0xc0000, 0xc007f, MWA_BANK3, bloodbro_scroll),
        new MemoryWriteAddress(0xc0080, 0xc0081, MWA_NOP), /* IRQ Ack VBL? */
        new MemoryWriteAddress(0xc00c0, 0xc00c1, MWA_NOP), /* watchdog? */
        //new MemoryWriteAddress( 0xc0100, 0xc0100, MWA_NOP ), /* ?? Written 1 time */
        new MemoryWriteAddress(-1)
    };

    /**
     * ** West Story Memory Map *******************************************
     */
    static MemoryReadAddress weststry_readmem_cpu[] = {
        new MemoryReadAddress(0x000000, 0x07ffff, MRA_ROM),
        new MemoryReadAddress(0x080000, 0x08afff, MRA_RAM),
        new MemoryReadAddress(0x08b000, 0x08bfff, MRA_RAM),
        new MemoryReadAddress(0x08c000, 0x08c3ff, bloodbro_background_r),
        new MemoryReadAddress(0x08c400, 0x08cfff, MRA_RAM),
        new MemoryReadAddress(0x08d000, 0x08d3ff, bloodbro_foreground_r),
        new MemoryReadAddress(0x08d400, 0x08dfff, MRA_RAM),
        new MemoryReadAddress(0x08d800, 0x08dfff, MRA_RAM),
        new MemoryReadAddress(0x08e000, 0x08ffff, MRA_RAM),
        new MemoryReadAddress(0x0c1000, 0x0c100f, bloodbro_r_read),
        new MemoryReadAddress(0x0c1010, 0x0c17ff, MRA_BANK3),
        new MemoryReadAddress(0x128000, 0x1287ff, paletteram_word_r),
        new MemoryReadAddress(0x120000, 0x128fff, MRA_BANK2),
        new MemoryReadAddress(-1)
    };

    static MemoryWriteAddress weststry_writemem_cpu[] = {
        new MemoryWriteAddress(0x000000, 0x07ffff, MWA_ROM),
        new MemoryWriteAddress(0x080000, 0x08afff, MWA_RAM),
        new MemoryWriteAddress(0x08b000, 0x08bfff, MWA_RAM, spriteram),
        new MemoryWriteAddress(0x08c000, 0x08c3ff, bloodbro_background_w, videoram), /* Background RAM */
        new MemoryWriteAddress(0x08c400, 0x08cfff, MWA_RAM),
        new MemoryWriteAddress(0x08d000, 0x08d3ff, bloodbro_foreground_w, bloodbro_videoram2), /* Foreground RAM */
        new MemoryWriteAddress(0x08d400, 0x08d7ff, MWA_RAM),
        new MemoryWriteAddress(0x08d800, 0x08dfff, MWA_RAM, textlayoutram),
        new MemoryWriteAddress(0x08e000, 0x08ffff, MWA_RAM),
        new MemoryWriteAddress(0x0c1010, 0x0c17ff, MWA_BANK3),
        new MemoryWriteAddress(0x128000, 0x1287ff, paletteram_xxxxBBBBGGGGRRRR_word_w, paletteram),
        new MemoryWriteAddress(0x120000, 0x128fff, MWA_BANK2),
        new MemoryWriteAddress(-1)
    };

    /**
     * ***************************************************************************
     */
    static MemoryReadAddress sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
                new MemoryReadAddress(0x2000, 0x27ff, MRA_RAM),
                new MemoryReadAddress(0x4008, 0x4008, YM3812_status_port_0_r),
                new MemoryReadAddress(0x4010, 0x4012, seibu_soundlatch_r),
                new MemoryReadAddress(0x4013, 0x4013, MRA_NOP), /* No coin port in this game */
                new MemoryReadAddress(0x6000, 0x6000, OKIM6295_status_0_r),
                new MemoryReadAddress(0x8000, 0xffff, MRA_BANK1),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x1fff, MWA_ROM),
                new MemoryWriteAddress(0x2000, 0x27ff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0x4000, seibu_soundclear_w),
                new MemoryWriteAddress(0x4002, 0x4002, seibu_rst10_ack),
                new MemoryWriteAddress(0x4003, 0x4003, seibu_rst18_ack),
                new MemoryWriteAddress(0x4007, 0x4007, seibu_bank_w),
                new MemoryWriteAddress(0x4008, 0x4008, YM3812_control_port_0_w),
                new MemoryWriteAddress(0x4009, 0x4009, YM3812_write_port_0_w),
                new MemoryWriteAddress(0x4018, 0x401f, seibu_main_data_w),
                new MemoryWriteAddress(0x6000, 0x6000, OKIM6295_data_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    /**
     * ***************************************************************************
     */
    static InputPortPtr input_ports_bloodbro = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x0e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x0e, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();  /* DSW1 */

            PORT_DIPNAME(0x01, 0x00, "Coin Mode");
            PORT_DIPSETTING(0x00, "Normal");
            PORT_DIPSETTING(0x01, DEF_STR("Free_Play"));
            PORT_DIPNAME(0x06, 0x06, DEF_STR("Coin_A"));
            PORT_DIPSETTING(0x00, DEF_STR("5C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("3C_1C"));
            PORT_DIPSETTING(0x04, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x06, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Coin_B"));
            PORT_DIPSETTING(0x18, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x10, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x08, DEF_STR("1C_5C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_6C"));
            PORT_DIPNAME(0x20, 0x20, "Starting Coin");
            PORT_DIPSETTING(0x20, "Normal");
            PORT_DIPSETTING(0x00, "x2");
            PORT_DIPNAME(0x40, 0x40, "Unused 1");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x80, "Unused 2");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            PORT_DIPNAME(0x03, 0x03, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "1");
            PORT_DIPSETTING(0x02, "2");
            PORT_DIPSETTING(0x03, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPNAME(0x0c, 0x0c, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x0c, "300K 500K");
            PORT_DIPSETTING(0x08, "500K 500K");
            PORT_DIPSETTING(0x04, "500K");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x30, 0x30, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x20, "Easy");
            PORT_DIPSETTING(0x30, "Normal");
            PORT_DIPSETTING(0x10, "Hard");
            PORT_DIPSETTING(0x00, "Very Hard");
            PORT_DIPNAME(0x40, 0x40, "Allow Continue");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x40, DEF_STR("Yes"));
            PORT_DIPNAME(0x80, 0x80, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    /**
     * ** West Story Input Ports ******************************************
     */
    static InputPortPtr input_ports_weststry = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN */

            PORT_BIT(0xff, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    /**
     * ** Blood Bros gfx decode *******************************************
     */
    static GfxLayout textlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            4096, /* 4096 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 4, 0x10000 * 8, 0x10000 * 8 + 4},
            new int[]{3, 2, 1, 0, 8 + 3, 8 + 2, 8 + 1, 8 + 0},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout backlayout = new GfxLayout(
            16, 16, /* 16*16 sprites  */
            4096, /* 4096 sprites */
            4, /* 4 bits per pixel */
            new int[]{8, 12, 0, 4},
            new int[]{3, 2, 1, 0, 16 + 3, 16 + 2, 16 + 1, 16 + 0,
                3 + 32 * 16, 2 + 32 * 16, 1 + 32 * 16, 0 + 32 * 16, 16 + 3 + 32 * 16, 16 + 2 + 32 * 16, 16 + 1 + 32 * 16, 16 + 0 + 32 * 16},
            new int[]{0 * 16, 2 * 16, 4 * 16, 6 * 16, 8 * 16, 10 * 16, 12 * 16, 14 * 16,
                16 * 16, 18 * 16, 20 * 16, 22 * 16, 24 * 16, 26 * 16, 28 * 16, 30 * 16},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxLayout spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites  */
            8192, /* 8192 sprites */
            4, /* 4 bits per pixel */
            new int[]{8, 12, 0, 4},
            new int[]{3, 2, 1, 0, 16 + 3, 16 + 2, 16 + 1, 16 + 0,
                3 + 32 * 16, 2 + 32 * 16, 1 + 32 * 16, 0 + 32 * 16, 16 + 3 + 32 * 16, 16 + 2 + 32 * 16, 16 + 1 + 32 * 16, 16 + 0 + 32 * 16},
            new int[]{0 * 16, 2 * 16, 4 * 16, 6 * 16, 8 * 16, 10 * 16, 12 * 16, 14 * 16,
                16 * 16, 18 * 16, 20 * 16, 22 * 16, 24 * 16, 26 * 16, 28 * 16, 30 * 16},
            128 * 8 /* every sprite takes 128 consecutive bytes */
    );

    static GfxDecodeInfo bloodbro_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, textlayout, 0x70 * 16, 0x10), /* Text */
                new GfxDecodeInfo(REGION_GFX2, 0x00000, backlayout, 0x40 * 16, 0x10), /* Background */
                new GfxDecodeInfo(REGION_GFX2, 0x80000, backlayout, 0x50 * 16, 0x10), /* Foreground */
                new GfxDecodeInfo(REGION_GFX3, 0x00000, spritelayout, 0x00 * 16, 0x10), /* Sprites */
                new GfxDecodeInfo(-1)
            };

    /**
     * ** West Story gfx decode ********************************************
     */
    static GfxLayout weststry_textlayout = new GfxLayout(
            8, 8, /* 8*8 sprites */
            4096, /* 4096 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 0x8000 * 8, 2 * 0x8000 * 8, 3 * 0x8000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every sprite takes 8 consecutive bytes */
    );

    static GfxLayout weststry_backlayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            4096, /* 4096 sprites */
            4, /* 4 bits per pixel */
            new int[]{0 * 0x20000 * 8, 1 * 0x20000 * 8, 2 * 0x20000 * 8, 3 * 0x20000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout weststry_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            8192, /* 8192 sprites */
            4, /* 4 bits per pixel */
            new int[]{0 * 0x40000 * 8, 1 * 0x40000 * 8, 2 * 0x40000 * 8, 3 * 0x40000 * 8},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxDecodeInfo weststry_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0x00000, weststry_textlayout, 16 * 16, 0x10),
                new GfxDecodeInfo(REGION_GFX2, 0x00000, weststry_backlayout, 48 * 16, 0x10),
                new GfxDecodeInfo(REGION_GFX2, 0x80000, weststry_backlayout, 32 * 16, 0x10),
                new GfxDecodeInfo(REGION_GFX3, 0x00000, weststry_spritelayout, 0 * 16, 0x10),
                new GfxDecodeInfo(-1)
            };

    /**
     * ** Blood Bros Interrupt & Driver Machine  ***************************
     */
    /* Parameters: YM3812 frequency, Oki frequency, Oki memory region */
    //SEIBU_SOUND_SYSTEM_YM3812_HARDWARE(14318180/4,8000,REGION_SOUND1);
    static YM3812interface ym3812_interface = new YM3812interface(
            1,
            14318180 / 4,
            new int[]{50},
            new WriteYmHandlerPtr[]{seibu_ym3812_irqhandler}
    );

    static OKIM6295interface okim6295_interface = new OKIM6295interface(
            1,
            new int[]{8000},
            new int[]{REGION_SOUND1},
            new int[]{40}
    );
    static MachineDriver machine_driver_bloodbro = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* 10 Mhz */
                        readmem_cpu, writemem_cpu, null, null,
                        m68_level4_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 4,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1, /* CPU slices per frame */
            seibu_sound_init_1, /* init machine */
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1),
            bloodbro_gfxdecodeinfo,
            2048, 2048,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            bloodbro_vh_start,
            bloodbro_vh_stop,
            bloodbro_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    static MachineDriver machine_driver_weststry = new MachineDriver(
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M68000,
                        10000000, /* 10 Mhz */
                        weststry_readmem_cpu, weststry_writemem_cpu, null, null,
                        m68_level6_irq, 1
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        14318180 / 4,
                        sound_readmem, sound_writemem, null, null,
                        ignore_interrupt, 0
                )
            },
            60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
            1, /* CPU slices per frame */
            seibu_sound_init_1, /* init machine */
            /* video hardware */
            256, 256, new rectangle(0, 255, 16, 239),
            weststry_gfxdecodeinfo,
            1024, 1024,
            null,
            VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
            null,
            bloodbro_vh_start,
            bloodbro_vh_stop,
            weststry_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM3812,
                        ym3812_interface
                ),
                new MachineSound(
                        SOUND_OKIM6295,
                        okim6295_interface
                )
            }
    );

    static RomLoadPtr rom_bloodbro = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x90000, REGION_CPU1);
            ROM_LOAD_ODD("bb_02.bin", 0x00000, 0x20000, 0xc0fdc3e4);
            ROM_LOAD_EVEN("bb_01.bin", 0x00000, 0x20000, 0x2d7e0fdf);
            ROM_LOAD_ODD("bb_04.bin", 0x40000, 0x20000, 0xfd951c2c);
            ROM_LOAD_EVEN("bb_03.bin", 0x40000, 0x20000, 0x18d3c460);

            ROM_REGION(0x18000, REGION_CPU2);
            ROM_LOAD("bb_07.bin", 0x000000, 0x08000, 0x411b94e8);
            ROM_CONTINUE(0x010000, 0x08000);

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bb_05.bin", 0x00000, 0x10000, 0x04ba6d19);/* characters */

            ROM_LOAD("bb_06.bin", 0x10000, 0x10000, 0x7092e35b);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bloodb.bk", 0x00000, 0x100000, 0x1aa87ee6);/* Background+Foreground */

            ROM_REGION(0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("bloodb.obj", 0x00000, 0x100000, 0xd27c3952);/* sprites */

            ROM_REGION(0x20000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("bb_08.bin", 0x00000, 0x20000, 0xdeb1b975);
            ROM_END();
        }
    };

    static RomLoadPtr rom_weststry = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x90000, REGION_CPU1);/* 64k for cpu code */

            ROM_LOAD_ODD("ws13.bin", 0x00000, 0x20000, 0x158e302a);
            ROM_LOAD_EVEN("ws15.bin", 0x00000, 0x20000, 0x672e9027);
            ROM_LOAD_ODD("bb_04.bin", 0x40000, 0x20000, 0xfd951c2c);
            ROM_LOAD_EVEN("bb_03.bin", 0x40000, 0x20000, 0x18d3c460);

            ROM_REGION(0x18000, REGION_CPU2);/* 64k for sound cpu code */

            ROM_LOAD("ws17.bin", 0x000000, 0x08000, 0xe00a8f09);
            ROM_CONTINUE(0x010000, 0x08000);

            ROM_REGION(0x20000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ws09.bin", 0x00000, 0x08000, 0xf05b2b3e);/* characters */

            ROM_CONTINUE(0x00000, 0x8000);
            ROM_LOAD("ws11.bin", 0x08000, 0x08000, 0x2b10e3d2);
            ROM_CONTINUE(0x08000, 0x8000);
            ROM_LOAD("ws10.bin", 0x10000, 0x08000, 0xefdf7c82);
            ROM_CONTINUE(0x10000, 0x8000);
            ROM_LOAD("ws12.bin", 0x18000, 0x08000, 0xaf993578);
            ROM_CONTINUE(0x18000, 0x8000);

            ROM_REGION(0x100000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ws05.bin", 0x00000, 0x20000, 0x007c8dc0);/* Background */

            ROM_LOAD("ws07.bin", 0x20000, 0x20000, 0x0f0c8d9a);
            ROM_LOAD("ws06.bin", 0x40000, 0x20000, 0x459d075e);
            ROM_LOAD("ws08.bin", 0x60000, 0x20000, 0x4d6783b3);
            ROM_LOAD("ws01.bin", 0x80000, 0x20000, 0x32bda4bc);/* Foreground */

            ROM_LOAD("ws03.bin", 0xa0000, 0x20000, 0x046b51f8);
            ROM_LOAD("ws02.bin", 0xc0000, 0x20000, 0xed9d682e);
            ROM_LOAD("ws04.bin", 0xe0000, 0x20000, 0x75f082e5);

            ROM_REGION(0x100000, REGION_GFX3 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ws25.bin", 0x00000, 0x20000, 0x8092e8e9);/* sprites */

            ROM_LOAD("ws26.bin", 0x20000, 0x20000, 0xf6a1f42c);
            ROM_LOAD("ws23.bin", 0x40000, 0x20000, 0x43d58e24);
            ROM_LOAD("ws24.bin", 0x60000, 0x20000, 0x20a867ea);
            ROM_LOAD("ws21.bin", 0x80000, 0x20000, 0xe23d7296);
            ROM_LOAD("ws22.bin", 0xa0000, 0x20000, 0x7150a060);
            ROM_LOAD("ws19.bin", 0xc0000, 0x20000, 0xc5dd0a96);
            ROM_LOAD("ws20.bin", 0xe0000, 0x20000, 0xf1245c16);

            ROM_REGION(0x20000, REGION_SOUND1);/* ADPCM samples */

            ROM_LOAD("bb_08.bin", 0x00000, 0x20000, 0xdeb1b975);
            ROM_END();
        }
    };

    static void gfx_untangle() {
        UBytePtr gfx = memory_region(REGION_GFX3);
        int i;
        for (i = 0; i < 0x100000; i++) {
            gfx.write(i, ~gfx.read(i));
        }
    }

    /**
     * ************************************************************************
     */
    public static InitDriverPtr init_bloodbro = new InitDriverPtr() {
        public void handler() {
            install_seibu_sound_speedup(1);
        }
    };

    public static InitDriverPtr init_weststry = new InitDriverPtr() {
        public void handler() {
            install_seibu_sound_speedup(1);
            gfx_untangle();
        }
    };

    /**
     * ************************************************************************
     */
    public static GameDriver driver_bloodbro = new GameDriver("1990", "bloodbro", "bloodbro.java", rom_bloodbro, null, machine_driver_bloodbro, input_ports_bloodbro, init_bloodbro, ROT0, "Tad", "Blood Bros.");
    public static GameDriver driver_weststry = new GameDriver("1990", "weststry", "bloodbro.java", rom_weststry, driver_bloodbro, machine_driver_weststry, input_ports_weststry, init_weststry, ROT0, "bootleg", "West Story");
}
