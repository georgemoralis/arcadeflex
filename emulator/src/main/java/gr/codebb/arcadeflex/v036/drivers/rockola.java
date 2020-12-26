/**
 * *************************************************************************
 *
 * Vanguard memory map (preliminary)
 *
 * 0000-03ff RAM
 * 0400-07ff Video RAM 1
 * 0800-0bff Video RAM 2
 * 0c00-0fff Color RAM (3 bits for video RAM 1 and 3 bits for video RAM 2)
 * 1000-1fff Character generator RAM
 * 4000-bfff ROM
 *
 * read:
 * 3104      IN0
 * 3105      IN1
 * 3106      DSW ??
 * 3107      IN2
 *
 * write
 * 3100      Sound Port null
 * 3101      Sound Port 1
 * 3103      bit 7 = flip screen
 * 3200      y scroll register
 * 3300      x scroll register
 *
 * Fantasy and Nibbler memory map (preliminary)
 *
 * 0000-03ff RAM
 * 0400-07ff Video RAM 1
 * 0800-0bff Video RAM 2
 * 0c00-0fff Color RAM (3 bits for video RAM 1 and 3 bits for video RAM 2)
 * 1000-1fff Character generator RAM
 * 3000-bfff ROM
 *
 * read:
 * 2104      IN0
 * 2105      IN1
 * 2106      DSW
 * 2107      IN2
 *
 * write
 * 2000-2001 To the HD46505S video controller
 * 2100      Sound Port null
 * 2101      Sound Port 1
 * 2103      bit 7 = flip screen
 * bit 4-6 = music 2
 * bit 3 = char bank selector
 * bit null-2 = background color
 * 2200      y scroll register
 * 2300      x scroll register
 *
 * Interrupts: VBlank causes an IRQ. Coin insertion causes a NMI.
 *
 * Pioneer Balloon memory map (preliminary)
 *
 * 0000-03ff RAM		   IC13 cpu
 * 0400-07ff Video RAM 1  IC67 video
 * 0800-0bff Video RAM 2  ???? video
 * 0c00-0fff Color RAM    IC68 (3 bits for VRAM 1 and 3 bits for VRAM 2)
 * 1000-1fff RAM		   ???? Character generator
 * 3000-3fff ROM 4/5	   IC12
 * 4000-4fff ROM 1 	   IC07
 * 5000-5fff ROM 2 	   IC08
 * 6000-6fff ROM 3 	   IC09
 * 7000-7fff ROM 4 	   IC10
 * 8000-8fff ROM 5 	   IC14
 * 9000-9fff ROM 6 	   IC15
 * read:
 * b104	  IN0
 * b105	  IN1
 * b106	  DSW
 * b107	  IN2
 *
 * write
 * b000	  Sound Port null
 * b001	  Sound Port 1
 * b100	  ????
 * b103	  bit 7 = flip screen
 * bit 4-6 = music 2
 * bit 3 = char bank selector
 * bit null-2 = background color
 * b106	  ????
 * b200	  y scroll register
 * b300	  x scroll register
 *
 * Interrupts: VBlank causes an IRQ. Coin insertion causes a NMI.
 *
 **************************************************************************
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
import static gr.codebb.arcadeflex.v036.mame.inputportH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.rockola.*;
import static gr.codebb.arcadeflex.v036.sndhrdw.rockola.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.generic.*;
import static gr.codebb.arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.vidhrdw.crtc6845.*;

public class rockola {

    static MemoryWriteAddress sasuke_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM, rockola_videoram2),
                new MemoryWriteAddress(0x0800, 0x0bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0c00, 0x0fff, colorram_w, colorram),
                new MemoryWriteAddress(0x1000, 0x1fff, rockola_characterram_w, rockola_characterram),
                new MemoryWriteAddress(0x4000, 0x97ff, MWA_ROM),
                new MemoryWriteAddress(0x3000, 0x3000, crtc6845_address_w),
                new MemoryWriteAddress(0x3001, 0x3001, crtc6845_register_w),
                new MemoryWriteAddress(0xb002, 0xb002, satansat_b002_w), /* flip screen  irq enable */
                new MemoryWriteAddress(0xb003, 0xb003, satansat_backcolor_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress satansat_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0x97ff, MRA_ROM),
                new MemoryReadAddress(0xb004, 0xb004, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xb005, 0xb005, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xb006, 0xb006, input_port_2_r), /* DSW */
                new MemoryReadAddress(0xb007, 0xb007, input_port_3_r), /* IN2 */
                new MemoryReadAddress(0xf800, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress satansat_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM, rockola_videoram2),
                new MemoryWriteAddress(0x0800, 0x0bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0c00, 0x0fff, colorram_w, colorram),
                new MemoryWriteAddress(0x1000, 0x1fff, rockola_characterram_w, rockola_characterram),
                new MemoryWriteAddress(0x4000, 0x97ff, MWA_ROM),
                new MemoryWriteAddress(0x3000, 0x3000, crtc6845_address_w),
                new MemoryWriteAddress(0x3001, 0x3001, crtc6845_register_w),
                new MemoryWriteAddress(0xb000, 0xb000, satansat_sound0_w),
                new MemoryWriteAddress(0xb001, 0xb001, satansat_sound1_w),
                new MemoryWriteAddress(0xb002, 0xb002, satansat_b002_w), /* flip screen  irq enable */
                new MemoryWriteAddress(0xb003, 0xb003, satansat_backcolor_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress vanguard_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x3104, 0x3104, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x3105, 0x3105, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x3106, 0x3106, input_port_2_r), /* DSW */
                new MemoryReadAddress(0x3107, 0x3107, input_port_3_r), /* IN2 */
                new MemoryReadAddress(0x4000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xf000, 0xffff, MRA_ROM), /* for the reset / interrupt vectors */
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress vanguard_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM, rockola_videoram2),
                new MemoryWriteAddress(0x0800, 0x0bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0c00, 0x0fff, colorram_w, colorram),
                new MemoryWriteAddress(0x1000, 0x1fff, rockola_characterram_w, rockola_characterram),
                new MemoryWriteAddress(0x3000, 0x3000, crtc6845_address_w),
                new MemoryWriteAddress(0x3001, 0x3001, crtc6845_register_w),
                new MemoryWriteAddress(0x3100, 0x3100, vanguard_sound0_w),
                new MemoryWriteAddress(0x3101, 0x3101, vanguard_sound1_w),
                //	new MemoryWriteAddress( 0x3102, 0x3102, ),	/* TODO: music channels #0 and #1 volume */
                new MemoryWriteAddress(0x3103, 0x3103, rockola_flipscreen_w),
                new MemoryWriteAddress(0x3200, 0x3200, MWA_RAM, rockola_scrolly),
                new MemoryWriteAddress(0x3300, 0x3300, MWA_RAM, rockola_scrollx),
                new MemoryWriteAddress(0x4000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress fantasy_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x2104, 0x2104, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0x2105, 0x2105, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0x2106, 0x2106, input_port_2_r), /* DSW */
                new MemoryReadAddress(0x2107, 0x2107, input_port_3_r), /* IN2 */
                new MemoryReadAddress(0x3000, 0xbfff, MRA_ROM),
                new MemoryReadAddress(0xfffa, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress fantasy_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM, rockola_videoram2),
                new MemoryWriteAddress(0x0800, 0x0bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0c00, 0x0fff, colorram_w, colorram),
                new MemoryWriteAddress(0x1000, 0x1fff, rockola_characterram_w, rockola_characterram),
                new MemoryWriteAddress(0x2000, 0x2000, crtc6845_address_w),
                new MemoryWriteAddress(0x2001, 0x2001, crtc6845_register_w),
                new MemoryWriteAddress(0x2100, 0x2100, fantasy_sound0_w),
                new MemoryWriteAddress(0x2101, 0x2101, fantasy_sound1_w),
                //	new MemoryWriteAddress( 0x2102, 0x2102, ),	/* TODO: music channels #0 and #1 volume */
                new MemoryWriteAddress(0x2103, 0x2103, fantasy_sound2_w), /* + flipscreen, gfx bank, bg color */
                new MemoryWriteAddress(0x2200, 0x2200, MWA_RAM, rockola_scrolly),
                new MemoryWriteAddress(0x2300, 0x2300, MWA_RAM, rockola_scrollx),
                new MemoryWriteAddress(0x3000, 0xbfff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress pballoon_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x1fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x9fff, MRA_ROM),
                new MemoryReadAddress(0xb104, 0xb104, input_port_0_r), /* IN0 */
                new MemoryReadAddress(0xb105, 0xb105, input_port_1_r), /* IN1 */
                new MemoryReadAddress(0xb106, 0xb106, input_port_2_r), /* DSW */
                new MemoryReadAddress(0xb107, 0xb107, input_port_3_r), /* IN2 */
                new MemoryReadAddress(0xfffa, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress pballoon_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x03ff, MWA_RAM),
                new MemoryWriteAddress(0x0400, 0x07ff, MWA_RAM, rockola_videoram2),
                new MemoryWriteAddress(0x0800, 0x0bff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x0c00, 0x0fff, colorram_w, colorram),
                new MemoryWriteAddress(0x1000, 0x1fff, rockola_characterram_w, rockola_characterram),
                new MemoryWriteAddress(0x3000, 0x9fff, MWA_ROM),
                new MemoryWriteAddress(0xb000, 0xb000, crtc6845_address_w),
                new MemoryWriteAddress(0xb001, 0xb001, crtc6845_register_w),
                new MemoryWriteAddress(0xb100, 0xb100, fantasy_sound0_w),
                new MemoryWriteAddress(0xb101, 0xb101, fantasy_sound1_w),
                //	new MemoryWriteAddress( 0xb102, 0xb102, ),	/* TODO: music channels #0 and #1 volume */
                new MemoryWriteAddress(0xb103, 0xb103, fantasy_sound2_w), /* + flipscreen, gfx bank, bg color */
                new MemoryWriteAddress(0xb200, 0xb200, MWA_RAM, rockola_scrolly),
                new MemoryWriteAddress(0xb300, 0xb300, MWA_RAM, rockola_scrollx),
                new MemoryWriteAddress(-1) /* end of table */};

    public static InterruptPtr satansat_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() != 0) {
                /* user asks to insert coin: generate a NMI interrupt. */
                if ((readinputport(3) & 1) != 0) {
                    return nmi_interrupt.handler();
                } else {
                    return ignore_interrupt.handler();
                }
            } else {
                return interrupt.handler();
                /* one IRQ per frame */
            }
        }
    };

    public static InterruptPtr rockola_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() != 0) {
                /* user asks to insert coin: generate a NMI interrupt. */
                if ((readinputport(3) & 3) != 0) {
                    return nmi_interrupt.handler();
                } else {
                    return ignore_interrupt.handler();
                }
            } else {
                return interrupt.handler();
                /* one IRQ per frame */
            }
        }
    };

    /* Derived from Zarzon. Might not reflect the actual hardware. */
    static InputPortPtr input_ports_sasuke = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START2);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x7C, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x02, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x04, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x08, DEF_STR("On"));
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            /* 0x30 gives 3 again */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "RAM Test");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x0e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);/* connected to a counter - random number generator? */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_satansat = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x7C, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START();
            /* DSW */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x0a, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x08, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPSETTING(0x02, DEF_STR("1C_2C"));
            /* 0x0a gives 2/1 again */
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x00, "5000");
            PORT_DIPSETTING(0x04, "10000");
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            /* 0x30 gives 3 again */
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, "RAM Test");
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x0e, IP_ACTIVE_LOW, IPT_UNUSED);
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);/* connected to a counter - random number generator? */
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_vanguard = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON4);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x4e, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x42, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x48, "1 Coin/2 Credits 2/5");
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x44, "1 Coin/3 Credits 2/7");
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x4c, "1 Coin/6 Credits 2/13");
            PORT_DIPSETTING(0x0c, DEF_STR("1C_7C"));
            /*
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x46, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4a, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4e, "1 Coin/1 Credit + Bonus" );
             */
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            /*	PORT_DIPSETTING(    0x30, "3" );*/
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_fantasy = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x4e, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x42, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x48, "1 Coin/2 Credits 2/5");
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x44, "1 Coin/3 Credits 2/7");
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x4c, "1 Coin/6 Credits 2/13");
            PORT_DIPSETTING(0x0c, DEF_STR("1C_7C"));
            /*
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x46, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4a, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4e, "1 Coin/1 Credit + Bonus" );
             */
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            /*	PORT_DIPSETTING(    0x30, "3" );*/
            PORT_DIPNAME(0x80, 0x00, "Allow Continue");
            PORT_DIPSETTING(0x80, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_pballoon = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x01, 0x01, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x01, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x4e, 0x02, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x42, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x02, "1 Coin/1 Credit 2/3");
            PORT_DIPSETTING(0x00, DEF_STR("1C_2C"));
            PORT_DIPSETTING(0x48, "1 Coin/2 Credits 2/5");
            PORT_DIPSETTING(0x08, DEF_STR("1C_3C"));
            PORT_DIPSETTING(0x44, "1 Coin/3 Credits 2/7");
            PORT_DIPSETTING(0x04, DEF_STR("1C_4C"));
            PORT_DIPSETTING(0x4c, "1 Coin/6 Credits 2/11");
            PORT_DIPSETTING(0x0c, DEF_STR("1C_7C"));
            /*
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0a, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x40, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x46, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4a, "1 Coin/1 Credit + Bonus" );
		PORT_DIPSETTING(    0x4e, "1 Coin/1 Credit + Bonus" );
             */
            PORT_DIPNAME(0x30, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x10, "4");
            PORT_DIPSETTING(0x20, "5");
            /*	PORT_DIPSETTING(    0x30, "3" );*/
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unknown"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_nibbler = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Slow down */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* debug command? */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* debug command */
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* debug command */
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Pause */
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* Unpause */
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* End game */
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);/* debug command */
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);

            PORT_START();
            /* DSW0 */
            PORT_DIPNAME(0x03, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "4");
            PORT_DIPSETTING(0x02, "5");
            PORT_DIPSETTING(0x03, "6");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x00, "Easy");
            PORT_DIPSETTING(0x04, "Hard");
            PORT_DIPNAME(0x08, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x08, DEF_STR("Cocktail"));
            PORT_BITX(0x10, 0x00, IPT_DIPSWITCH_NAME | IPF_CHEAT, "Rack Test", KEYCODE_F1, IP_JOY_NONE);
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Free_Play"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x40, DEF_STR("2C_1C"));
            PORT_DIPSETTING(0x00, DEF_STR("1C_1C"));
            PORT_DIPNAME(0x80, 0x00, "Bonus Every 2 Credits");
            PORT_DIPSETTING(0x00, DEF_STR("No"));
            PORT_DIPSETTING(0x80, DEF_STR("Yes"));

            PORT_START();
            /* IN2 */
            PORT_BIT_IMPULSE(0x01, IP_ACTIVE_HIGH, IPT_COIN2, 1);
            PORT_BIT_IMPULSE(0x02, IP_ACTIVE_HIGH, IPT_COIN1, 1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_START1);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout swapcharlayout256 = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{256 * 8 * 8, 0}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout charlayout256 = new GfxLayout(
            8, 8, /* 8*8 characters */
            256, /* 256 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 256 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );
    static GfxLayout charlayout512 = new GfxLayout(
            8, 8, /* 8*8 characters */
            512, /* 512 characters */
            2, /* 2 bits per pixel */
            new int[]{0, 512 * 8 * 8}, /* the two bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxDecodeInfo sasuke_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(0, 0x1000, swapcharlayout256, 0, 4), /* the game dynamically modifies this */
                new GfxDecodeInfo(REGION_GFX1, 0x0000, swapcharlayout256, 4 * 4, 4),
                new GfxDecodeInfo(-1)
            };

    static GfxDecodeInfo satansat_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(0, 0x1000, charlayout256, 0, 4), /* the game dynamically modifies this */
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout256, 4 * 4, 4),
                new GfxDecodeInfo(-1)
            };

    static GfxDecodeInfo vanguard_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(0, 0x1000, charlayout256, 0, 8), /* the game dynamically modifies this */
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout256, 8 * 4, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxDecodeInfo fantasy_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(0, 0x1000, charlayout256, 0, 8), /* the game dynamically modifies this */
                new GfxDecodeInfo(REGION_GFX1, 0x0000, charlayout512, 8 * 4, 8),
                new GfxDecodeInfo(-1) /* end of array */};

    static CustomSound_interface custom_interface = new CustomSound_interface(
            rockola_sh_start,
            null,
            rockola_sh_update
    );

    static MachineDriver machine_driver_sasuke = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        11289000 / 16, /* 700 kHz */
                        satansat_readmem, sasuke_writemem, null, null,
                        satansat_interrupt, 2
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            sasuke_gfxdecodeinfo,
            32, 4 * 4 + 4 * 4,
            satansat_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            generic_vh_start,
            generic_vh_stop,
            satansat_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            null
    );

    static MachineDriver machine_driver_satansat = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        11289000 / 16, /* 700 kHz */
                        satansat_readmem, satansat_writemem, null, null,
                        satansat_interrupt, 2
                ),},
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            satansat_gfxdecodeinfo,
            32, 4 * 4 + 4 * 4,
            satansat_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            generic_vh_start,
            generic_vh_stop,
            satansat_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_vanguard = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1000000, /* 1 MHz??? */
                        vanguard_readmem, vanguard_writemem, null, null,
                        rockola_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            vanguard_gfxdecodeinfo,
            64, 16 * 4,
            rockola_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            rockola_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    static MachineDriver machine_driver_fantasy = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1000000, /* 1 MHz??? */
                        fantasy_readmem, fantasy_writemem, null, null,
                        rockola_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 0 * 8, 28 * 8 - 1),
            fantasy_gfxdecodeinfo,
            64, 16 * 4,
            rockola_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            rockola_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
                )
            }
    );

    /* note that in this driver the visible area is different!!! */
    static MachineDriver machine_driver_pballoon = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6502,
                        1000000, /* 1 MHz??? */
                        pballoon_readmem, pballoon_writemem, null, null,
                        rockola_interrupt, 2
                )
            },
            60, DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(0 * 8, 32 * 8 - 1, 2 * 8, 30 * 8 - 1), /* different from the others! */
            fantasy_gfxdecodeinfo,
            64, 16 * 4,
            rockola_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            rockola_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_CUSTOM,
                        custom_interface
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
    static RomLoadPtr rom_sasuke = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sc1", 0x4000, 0x0800, 0x34cbbe03);
            ROM_LOAD("sc2", 0x4800, 0x0800, 0x38cc14f0);
            ROM_LOAD("sc3", 0x5000, 0x0800, 0x54c41285);
            ROM_LOAD("sc4", 0x5800, 0x0800, 0x23edafcf);
            ROM_LOAD("sc5", 0x6000, 0x0800, 0xca410e4f);
            ROM_LOAD("sc6", 0x6800, 0x0800, 0x80406afb);
            ROM_LOAD("sc7", 0x7000, 0x0800, 0x04d0f104);
            ROM_LOAD("sc8", 0x7800, 0x0800, 0x0219104b);
            ROM_RELOAD(0xf800, 0x0800);/* for the reset/interrupt vectors */
            ROM_LOAD("sc9", 0x8000, 0x0800, 0xd6ff889a);
            ROM_LOAD("sc10", 0x8800, 0x0800, 0x19df6b9a);
            ROM_LOAD("sc11", 0x9000, 0x0800, 0x24a0e121);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("mcs_c", 0x0000, 0x0800, 0xaff9743d);
            ROM_LOAD("mcs_d", 0x0800, 0x0800, 0x9c805120);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("sasuke.clr", 0x0000, 0x0020, 0xb70f34c1);

            /* no sound ROMs - the sound section is entirely analog */
            ROM_END();
        }
    };

    static RomLoadPtr rom_satansat = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ss1", 0x4000, 0x0800, 0x549dd13a);
            ROM_LOAD("ss2", 0x4800, 0x0800, 0x04972fa8);
            ROM_LOAD("ss3", 0x5000, 0x0800, 0x9caf9057);
            ROM_LOAD("ss4", 0x5800, 0x0800, 0xe1bdcfe1);
            ROM_LOAD("ss5", 0x6000, 0x0800, 0xd454de19);
            ROM_LOAD("ss6", 0x6800, 0x0800, 0x7fbd5d30);
            ROM_LOAD("zarz128.15", 0x7000, 0x0800, 0x93ea2df9);
            ROM_LOAD("zarz129.16", 0x7800, 0x0800, 0xe67ec873);
            ROM_RELOAD(0xf800, 0x0800);/* for the reset/interrupt vectors */
            ROM_LOAD("zarz130.22", 0x8000, 0x0800, 0x22c44650);
            ROM_LOAD("ss10", 0x8800, 0x0800, 0x8f1b313a);
            ROM_LOAD("ss11", 0x9000, 0x0800, 0xe74f98e0);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("zarz135.73", 0x0000, 0x0800, 0xe837c62b);
            ROM_LOAD("zarz136.75", 0x0800, 0x0800, 0x83f61623);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("zarz138.03", 0x0000, 0x0020, 0x5dd6933a);

            ROM_REGION(0x1000, REGION_SOUND1);
            /* sound data for Vanguard-style audio section */
            ROM_LOAD("ss12", 0x0000, 0x0800, 0xdee01f24);
            ROM_LOAD("zarz134.54", 0x0800, 0x0800, 0x580934d2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_zarzon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("zarz122.07", 0x4000, 0x0800, 0xbdfa67e2);
            ROM_LOAD("zarz123.08", 0x4800, 0x0800, 0xd034e61e);
            ROM_LOAD("zarz124.09", 0x5000, 0x0800, 0x296397ea);
            ROM_LOAD("zarz125.10", 0x5800, 0x0800, 0x26dc5e66);
            ROM_LOAD("zarz126.13", 0x6000, 0x0800, 0xcee18d7f);
            ROM_LOAD("zarz127.14", 0x6800, 0x0800, 0xbbd2cc0d);
            ROM_LOAD("zarz128.15", 0x7000, 0x0800, 0x93ea2df9);
            ROM_LOAD("zarz129.16", 0x7800, 0x0800, 0xe67ec873);
            ROM_RELOAD(0xf800, 0x0800);/* for the reset/interrupt vectors */
            ROM_LOAD("zarz130.22", 0x8000, 0x0800, 0x22c44650);
            ROM_LOAD("zarz131.23", 0x8800, 0x0800, 0x7be20678);
            ROM_LOAD("zarz132.24", 0x9000, 0x0800, 0x72b2cb76);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("zarz135.73", 0x0000, 0x0800, 0xe837c62b);
            ROM_LOAD("zarz136.75", 0x0800, 0x0800, 0x83f61623);

            ROM_REGION(0x0020, REGION_PROMS);
            ROM_LOAD("zarz138.03", 0x0000, 0x0020, 0x5dd6933a);

            ROM_REGION(0x1000, REGION_SOUND1);
            /* sound data for Vanguard-style audio section */
            ROM_LOAD("zarz133.53", 0x0000, 0x0800, 0xb253cf78);
            ROM_LOAD("zarz134.54", 0x0800, 0x0800, 0x580934d2);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vanguard = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sk4_ic07.bin", 0x4000, 0x1000, 0x6a29e354);
            ROM_LOAD("sk4_ic08.bin", 0x5000, 0x1000, 0x302bba54);
            ROM_LOAD("sk4_ic09.bin", 0x6000, 0x1000, 0x424755f6);
            ROM_LOAD("sk4_ic10.bin", 0x7000, 0x1000, 0x54603274);
            ROM_LOAD("sk4_ic13.bin", 0x8000, 0x1000, 0xfde157d0);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("sk4_ic14.bin", 0x9000, 0x1000, 0x0d5b47d0);
            ROM_LOAD("sk4_ic15.bin", 0xa000, 0x1000, 0x8549b8f8);
            ROM_LOAD("sk4_ic16.bin", 0xb000, 0x1000, 0x062e0be2);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sk5_ic50.bin", 0x0000, 0x0800, 0xe7d4315b);
            ROM_LOAD("sk5_ic51.bin", 0x0800, 0x0800, 0x96e87858);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("sk5_ic7.bin", 0x0000, 0x0020, 0xad782a73);/* foreground colors */
            ROM_LOAD("sk5_ic6.bin", 0x0020, 0x0020, 0x7dc9d450);/* background colors */

            ROM_REGION(0x1000, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("sk4_ic51.bin", 0x0000, 0x0800, 0xd2a64006);
            /* sound ROM 1 */
            ROM_LOAD("sk4_ic52.bin", 0x0800, 0x0800, 0xcc4a0b6f);
            /* sound ROM 2 */

            ROM_REGION(0x1800, REGION_SOUND2);/* space for the speech ROMs (not supported) */
            ROM_LOAD("sk6_ic07.bin", 0x0000, 0x0800, 0x2b7cbae9);
            ROM_LOAD("sk6_ic08.bin", 0x0800, 0x0800, 0x3b7e9d7c);
            ROM_LOAD("sk6_ic11.bin", 0x1000, 0x0800, 0xc36df041);
            ROM_END();
        }
    };

    static RomLoadPtr rom_vangrdce = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sk4_ic07.bin", 0x4000, 0x1000, 0x6a29e354);
            ROM_LOAD("sk4_ic08.bin", 0x5000, 0x1000, 0x302bba54);
            ROM_LOAD("sk4_ic09.bin", 0x6000, 0x1000, 0x424755f6);
            ROM_LOAD("4", 0x7000, 0x1000, 0x770f9714);
            ROM_LOAD("5", 0x8000, 0x1000, 0x3445cba6);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("sk4_ic14.bin", 0x9000, 0x1000, 0x0d5b47d0);
            ROM_LOAD("sk4_ic15.bin", 0xa000, 0x1000, 0x8549b8f8);
            ROM_LOAD("8", 0xb000, 0x1000, 0x4b825bc8);

            ROM_REGION(0x1000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sk5_ic50.bin", 0x0000, 0x0800, 0xe7d4315b);
            ROM_LOAD("sk5_ic51.bin", 0x0800, 0x0800, 0x96e87858);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("sk5_ic7.bin", 0x0000, 0x0020, 0xad782a73);/* foreground colors */
            ROM_LOAD("sk5_ic6.bin", 0x0020, 0x0020, 0x7dc9d450);/* background colors */

            ROM_REGION(0x1000, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("sk4_ic51.bin", 0x0000, 0x0800, 0xd2a64006);
            /* missing, using the SNK one */
            ROM_LOAD("sk4_ic52.bin", 0x0800, 0x0800, 0xcc4a0b6f);
            /* missing, using the SNK one */

            ROM_REGION(0x1800, REGION_SOUND2);/* space for the speech ROMs (not supported) */
            ROM_LOAD("sk6_ic07.bin", 0x0000, 0x0800, 0x2b7cbae9);
            ROM_LOAD("sk6_ic08.bin", 0x0800, 0x0800, 0x3b7e9d7c);
            ROM_LOAD("sk6_ic11.bin", 0x1000, 0x0800, 0xc36df041);
            ROM_END();
        }
    };

    static RomLoadPtr rom_fantasy = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic12.cpu", 0x3000, 0x1000, 0x22cb2249);
            ROM_LOAD("ic07.cpu", 0x4000, 0x1000, 0x0e2880b6);
            ROM_LOAD("ic08.cpu", 0x5000, 0x1000, 0x4c331317);
            ROM_LOAD("ic09.cpu", 0x6000, 0x1000, 0x6ac1dbfc);
            ROM_LOAD("ic10.cpu", 0x7000, 0x1000, 0xc796a406);
            ROM_LOAD("ic14.cpu", 0x8000, 0x1000, 0x6f1f0698);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("ic15.cpu", 0x9000, 0x1000, 0x5534d57e);
            ROM_LOAD("ic16.cpu", 0xa000, 0x1000, 0x6c2aeb6e);
            ROM_LOAD("ic17.cpu", 0xb000, 0x1000, 0xf6aa5de1);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("fs10ic50.bin", 0x0000, 0x1000, 0x86a801c3);
            ROM_LOAD("fs11ic51.bin", 0x1000, 0x1000, 0x9dfff71c);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("fantasy.ic7", 0x0000, 0x0020, 0x361a5e99);/* foreground colors */
            ROM_LOAD("fantasy.ic6", 0x0020, 0x0020, 0x33d974f7);/* background colors */

            ROM_REGION(0x1800, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("fs_b_51.bin", 0x0000, 0x0800, 0x48094ec5);
            ROM_LOAD("fs_a_52.bin", 0x0800, 0x0800, 0x1d0316e8);
            ROM_LOAD("fs_c_53.bin", 0x1000, 0x0800, 0x49fd4ae8);

            ROM_REGION(0x1800, REGION_SOUND2);/* space for the speech ROMs (not supported) */
            ROM_LOAD("fs_d_7.bin", 0x0000, 0x0800, 0xa7ef4cc6);
            ROM_LOAD("fs_e_8.bin", 0x0800, 0x0800, 0x19b8fb3e);
            ROM_LOAD("fs_f_11.bin", 0x1000, 0x0800, 0x3a352e1f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_fantasyj = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("fs5jic12.bin", 0x3000, 0x1000, 0xdd1eac89);
            ROM_LOAD("fs1jic7.bin", 0x4000, 0x1000, 0x7b8115ae);
            ROM_LOAD("fs2jic8.bin", 0x5000, 0x1000, 0x61531dd1);
            ROM_LOAD("fs3jic9.bin", 0x6000, 0x1000, 0x36a12617);
            ROM_LOAD("fs4jic10.bin", 0x7000, 0x1000, 0xdbf7c347);
            ROM_LOAD("fs6jic14.bin", 0x8000, 0x1000, 0xbf59a33a);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("fs7jic15.bin", 0x9000, 0x1000, 0xcc18428e);
            ROM_LOAD("fs8jic16.bin", 0xa000, 0x1000, 0xae5bf727);
            ROM_LOAD("fs9jic17.bin", 0xb000, 0x1000, 0xfa6903e2);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("fs10ic50.bin", 0x0000, 0x1000, 0x86a801c3);
            ROM_LOAD("fs11ic51.bin", 0x1000, 0x1000, 0x9dfff71c);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("prom-8.bpr", 0x0000, 0x0020, 0x1aa9285a);/* foreground colors */
            ROM_LOAD("prom-7.bpr", 0x0020, 0x0020, 0x7a6f7dc3);/* background colors */

            ROM_REGION(0x1800, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("fs_b_51.bin", 0x0000, 0x0800, 0x48094ec5);
            ROM_LOAD("fs_a_52.bin", 0x0800, 0x0800, 0x1d0316e8);
            ROM_LOAD("fs_c_53.bin", 0x1000, 0x0800, 0x49fd4ae8);

            ROM_REGION(0x1800, REGION_SOUND2);/* space for the speech ROMs (not supported) */
            ROM_LOAD("fs_d_7.bin", 0x0000, 0x0800, 0xa7ef4cc6);
            ROM_LOAD("fs_e_8.bin", 0x0800, 0x0800, 0x19b8fb3e);
            ROM_LOAD("fs_f_11.bin", 0x1000, 0x0800, 0x3a352e1f);
            ROM_END();
        }
    };

    static RomLoadPtr rom_pballoon = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("sk7_ic12.bin", 0x3000, 0x1000, 0xdfe2ae05);
            ROM_LOAD("sk7_ic07.bin", 0x4000, 0x1000, 0x736e67df);
            ROM_LOAD("sk7_ic08.bin", 0x5000, 0x1000, 0x7a2032b2);
            ROM_LOAD("sk7_ic09.bin", 0x6000, 0x1000, 0x2d63cf3a);
            ROM_LOAD("sk7_ic10.bin", 0x7000, 0x1000, 0x7b88cbd4);
            ROM_LOAD("sk7_ic14.bin", 0x8000, 0x1000, 0x6a8817a5);
            ROM_RELOAD(0xf000, 0x1000);
            /* for the reset and interrupt vectors */
            ROM_LOAD("sk7_ic15.bin", 0x9000, 0x1000, 0x1f78d814);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("sk8_ic50.bin", 0x0000, 0x1000, 0x560df07f);
            ROM_LOAD("sk8_ic51.bin", 0x1000, 0x1000, 0xd415de51);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("sk8_ic7.bin", 0x0000, 0x0020, 0xef6c82a0);/* foreground colors */
            ROM_LOAD("sk8_ic6.bin", 0x0020, 0x0020, 0xeabc6a00);/* background colors */

            ROM_REGION(0x1800, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("sk7_ic51.bin", 0x0000, 0x0800, 0x0345f8b7);
            ROM_LOAD("sk7_ic52.bin", 0x0800, 0x0800, 0x5d6d68ea);
            ROM_LOAD("sk7_ic53.bin", 0x1000, 0x0800, 0xa4c505cd);
            ROM_END();
        }
    };

    static RomLoadPtr rom_nibbler = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("g960-52.12", 0x3000, 0x1000, 0xac6a802b);
            ROM_LOAD("g960-48.07", 0x4000, 0x1000, 0x35971364);
            ROM_LOAD("g960-49.08", 0x5000, 0x1000, 0x6b33b806);
            ROM_LOAD("g960-50.09", 0x6000, 0x1000, 0x91a4f98d);
            ROM_LOAD("g960-51.10", 0x7000, 0x1000, 0xa151d934);
            ROM_LOAD("g960-53.14", 0x8000, 0x1000, 0x063f05cc);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("g960-54.15", 0x9000, 0x1000, 0x7205fb8d);
            ROM_LOAD("g960-55.16", 0xa000, 0x1000, 0x4bb39815);
            ROM_LOAD("g960-56.17", 0xb000, 0x1000, 0xed680f19);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("g960-57.50", 0x0000, 0x1000, 0x01d4d0c2);
            ROM_LOAD("g960-58.51", 0x1000, 0x1000, 0xfeff7faf);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("g70805.ic7", 0x0000, 0x0020, 0xa5709ff3);/* foreground colors */
            ROM_LOAD("g70804.ic6", 0x0020, 0x0020, 0xdacd592d);/* background colors */

            ROM_REGION(0x1800, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("g959-43.51", 0x0000, 0x0800, 0x0345f8b7);
            ROM_LOAD("g959-44.52", 0x0800, 0x0800, 0x87d67dee);
            ROM_LOAD("g959-45.53", 0x1000, 0x0800, 0x33189917);
            ROM_END();
        }
    };

    static RomLoadPtr rom_nibblera = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic12", 0x3000, 0x1000, 0x6dfa1be5);
            ROM_LOAD("ic07", 0x4000, 0x1000, 0x808e1a03);
            ROM_LOAD("ic08", 0x5000, 0x1000, 0x1571d4a2);
            ROM_LOAD("ic09", 0x6000, 0x1000, 0xa599df10);
            ROM_LOAD("ic10", 0x7000, 0x1000, 0xa6b5abe5);
            ROM_LOAD("ic14", 0x8000, 0x1000, 0x9f537185);
            ROM_RELOAD(0xf000, 0x1000);/* for the reset and interrupt vectors */
            ROM_LOAD("g960-54.15", 0x9000, 0x1000, 0x7205fb8d);
            ROM_LOAD("g960-55.16", 0xa000, 0x1000, 0x4bb39815);
            ROM_LOAD("g960-56.17", 0xb000, 0x1000, 0xed680f19);

            ROM_REGION(0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("g960-57.50", 0x0000, 0x1000, 0x01d4d0c2);
            ROM_LOAD("g960-58.51", 0x1000, 0x1000, 0xfeff7faf);

            ROM_REGION(0x0040, REGION_PROMS);
            ROM_LOAD("g70805.ic7", 0x0000, 0x0020, 0xa5709ff3);/* foreground colors */
            ROM_LOAD("g70804.ic6", 0x0020, 0x0020, 0xdacd592d);/* background colors */

            ROM_REGION(0x1800, REGION_SOUND1);/* sound ROMs */
            ROM_LOAD("g959-43.51", 0x0000, 0x0800, 0x0345f8b7);
            ROM_LOAD("g959-44.52", 0x0800, 0x0800, 0x87d67dee);
            ROM_LOAD("g959-45.53", 0x1000, 0x0800, 0x33189917);
            ROM_END();
        }
    };

    public static GameDriver driver_sasuke = new GameDriver("1980", "sasuke", "rockola.java", rom_sasuke, null, machine_driver_sasuke, input_ports_sasuke, null, ROT90, "SNK", "Sasuke vs. Commander", GAME_NO_SOUND);
    public static GameDriver driver_satansat = new GameDriver("1981", "satansat", "rockola.java", rom_satansat, null, machine_driver_satansat, input_ports_satansat, null, ROT90, "SNK", "Satan of Saturn", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_zarzon = new GameDriver("1981", "zarzon", "rockola.java", rom_zarzon, driver_satansat, machine_driver_satansat, input_ports_satansat, null, ROT90, "[SNK] (Taito America license)", "Zarzon", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_vanguard = new GameDriver("1981", "vanguard", "rockola.java", rom_vanguard, null, machine_driver_vanguard, input_ports_vanguard, null, ROT90, "SNK", "Vanguard (SNK)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_vangrdce = new GameDriver("1981", "vangrdce", "rockola.java", rom_vangrdce, driver_vanguard, machine_driver_vanguard, input_ports_vanguard, null, ROT90, "SNK (Centuri license)", "Vanguard (Centuri)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_fantasy = new GameDriver("1981", "fantasy", "rockola.java", rom_fantasy, null, machine_driver_fantasy, input_ports_fantasy, null, ROT90, "[SNK] (Rock-ola license)", "Fantasy (US)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_fantasyj = new GameDriver("1981", "fantasyj", "rockola.java", rom_fantasyj, driver_fantasy, machine_driver_fantasy, input_ports_fantasy, null, ROT90, "SNK", "Fantasy (Japan)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_pballoon = new GameDriver("1982", "pballoon", "rockola.java", rom_pballoon, null, machine_driver_pballoon, input_ports_pballoon, null, ROT90, "SNK", "Pioneer Balloon", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_nibbler = new GameDriver("1982", "nibbler", "rockola.java", rom_nibbler, null, machine_driver_fantasy, input_ports_nibbler, null, ROT90, "Rock-ola", "Nibbler (set 1)", GAME_IMPERFECT_SOUND);
    public static GameDriver driver_nibblera = new GameDriver("1982", "nibblera", "rockola.java", rom_nibblera, driver_nibbler, machine_driver_fantasy, input_ports_nibbler, null, ROT90, "Rock-ola", "Nibbler (set 2)", GAME_IMPERFECT_SOUND);
}
