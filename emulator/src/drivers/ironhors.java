/*
 * ported to v0.36
 * using automatic conversion tool v0.09
 */
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.cpuintrf.*;
import static mame.inputportH.*;
import static mame.sndintrf.*;
import static cpu.m6809.m6809H.*;
import static vidhrdw.ironhors.*;
import static platform.ptrlib.*;
import static mame.sndintrfH.*;
import static sound._2203intf.*;
import static sound._2203intfH.*;

public class ironhors {

    public static UBytePtr ironhors_interrupt_enable = new UBytePtr();

    public static InterruptPtr ironhors_interrupt = new InterruptPtr() {
        public int handler() {
            if (cpu_getiloops() == 0) {
                if ((ironhors_interrupt_enable.read() & 4) != 0) {
                    return M6809_INT_FIRQ;
                }
            } else if ((cpu_getiloops() % 2) != 0) {
                if ((ironhors_interrupt_enable.read() & 1) != 0) {
                    return nmi_interrupt.handler();
                }
            }
            return ignore_interrupt.handler();
        }
    };

    public static WriteHandlerPtr ironhors_sh_irqtrigger_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            cpu_cause_interrupt(1, 0xff);
        }
    };

    static MemoryReadAddress ironhors_readmem[]
            = {
                new MemoryReadAddress(0x0020, 0x003f, MRA_RAM),
                new MemoryReadAddress(0x0b03, 0x0b03, input_port_0_r), /* coins + selftest */
                new MemoryReadAddress(0x0b02, 0x0b02, input_port_1_r), /* player 1 controls */
                new MemoryReadAddress(0x0b01, 0x0b01, input_port_2_r), /* player 2 controls */
                new MemoryReadAddress(0x0a00, 0x0a00, input_port_3_r), /* Dipswitch settings 0 */
                new MemoryReadAddress(0x0b00, 0x0b00, input_port_4_r), /* Dipswitch settings 1 */
                new MemoryReadAddress(0x0900, 0x0900, input_port_5_r), /* Dipswitch settings 2 */
                new MemoryReadAddress(0x2000, 0x2fff, MRA_RAM),
                new MemoryReadAddress(0x3000, 0x3fff, MRA_RAM),
                new MemoryReadAddress(0x4000, 0xffff, MRA_ROM),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress ironhors_writemem[]
            = {
                new MemoryWriteAddress(0x0003, 0x0003, ironhors_charbank_w),
                new MemoryWriteAddress(0x0004, 0x0004, MWA_RAM, ironhors_interrupt_enable),
                new MemoryWriteAddress(0x0020, 0x003f, MWA_RAM, ironhors_scroll),
                new MemoryWriteAddress(0x0800, 0x0800, soundlatch_w),
                new MemoryWriteAddress(0x0900, 0x0900, ironhors_sh_irqtrigger_w), /* cause interrupt on audio CPU */
                new MemoryWriteAddress(0x0a00, 0x0a00, ironhors_palettebank_w),
                new MemoryWriteAddress(0x0b00, 0x0b00, watchdog_reset_w),
                new MemoryWriteAddress(0x2000, 0x23ff, colorram_w, colorram),
                new MemoryWriteAddress(0x2400, 0x27ff, videoram_w, videoram, videoram_size),
                new MemoryWriteAddress(0x2800, 0x2fff, MWA_RAM),
                new MemoryWriteAddress(0x3000, 0x30ff, MWA_RAM, spriteram_2),
                new MemoryWriteAddress(0x3100, 0x37ff, MWA_RAM),
                new MemoryWriteAddress(0x3800, 0x38ff, MWA_RAM, spriteram, spriteram_size),
                new MemoryWriteAddress(0x3900, 0x3fff, MWA_RAM),
                new MemoryWriteAddress(0x4000, 0xffff, MWA_ROM),
                new MemoryWriteAddress(-1) /* end of table */};

    static MemoryReadAddress ironhors_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(0x8000, 0x8000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress ironhors_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(-1) /* end of table */};

    static IOReadPort ironhors_sound_readport[]
            = {
                new IOReadPort(0x00, 0x00, YM2203_status_port_0_r),
                new IOReadPort(-1) /* end of table */};

    static IOWritePort ironhors_sound_writeport[]
            = {
                new IOWritePort(0x00, 0x00, YM2203_control_port_0_w),
                new IOWritePort(0x01, 0x01, YM2203_write_port_0_w),
                new IOWritePort(-1) /* end of table */};

    static MemoryReadAddress farwest_sound_readmem[]
            = {
                new MemoryReadAddress(0x0000, 0x3fff, MRA_ROM),
                new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
                new MemoryReadAddress(0x8000, 0x8000, soundlatch_r),
                new MemoryReadAddress(-1) /* end of table */};
    static MemoryWriteAddress farwest_sound_writemem[]
            = {
                new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
                new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
                new MemoryWriteAddress(0x8000, 0x8000, YM2203_control_port_0_w),
                new MemoryWriteAddress(0x8001, 0x8001, YM2203_write_port_0_w),
                new MemoryWriteAddress(-1) /* end of table */};

    static InputPortPtr input_ports_ironhors = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */
            /* note that button 3 for player 1 and 2 are exchanged */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30000 70000");
            PORT_DIPSETTING(0x10, "40000 80000");
            PORT_DIPSETTING(0x08, "40000");
            PORT_DIPSETTING(0x00, "50000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW1 */

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
            /* 	PORT_DIPSETTING(    0x00, "Invalid" );*/

            PORT_START();
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Controls");
            PORT_DIPSETTING(0x02, "Single");
            PORT_DIPSETTING(0x00, "Dual");
            PORT_DIPNAME(0x04, 0x04, "Button Layout");
            PORT_DIPSETTING(0x04, "Power Atk Squat");
            PORT_DIPSETTING(0x00, "Squat Atk Power");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unused"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static InputPortPtr input_ports_dairesya = new InputPortPtr() {
        public void handler() {
            PORT_START(); 	/* IN0 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_COIN2);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_COIN3);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_START1);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_START2);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN1 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* IN2 */

            PORT_BIT(0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_4WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL);
            PORT_BIT(0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL);
            PORT_BIT(0x80, IP_ACTIVE_LOW, IPT_UNKNOWN);

            PORT_START(); 	/* DSW0 */

            PORT_DIPNAME(0x03, 0x02, DEF_STR("Lives"));
            PORT_DIPSETTING(0x03, "2");
            PORT_DIPSETTING(0x02, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPSETTING(0x00, "7");
            PORT_DIPNAME(0x04, 0x00, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x00, DEF_STR("Upright"));
            PORT_DIPSETTING(0x04, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x18, 0x18, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x18, "30000 70000");
            PORT_DIPSETTING(0x10, "40000 80000");
            PORT_DIPSETTING(0x08, "40000");
            PORT_DIPSETTING(0x00, "50000");
            PORT_DIPNAME(0x60, 0x60, DEF_STR("Difficulty"));
            PORT_DIPSETTING(0x60, "Easy");
            PORT_DIPSETTING(0x40, "Normal");
            PORT_DIPSETTING(0x20, "Hard");
            PORT_DIPSETTING(0x00, "Hardest");
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x80, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));

            PORT_START(); 	/* DSW1 */

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
            /* 	PORT_DIPSETTING(    0x00, "Invalid" );*/

            PORT_START();
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x01, DEF_STR("On"));
            PORT_DIPNAME(0x02, 0x02, "Controls");
            PORT_DIPSETTING(0x02, "Single");
            PORT_DIPSETTING(0x00, "Dual");
            PORT_DIPNAME(0x04, 0x04, "Button Layout");
            PORT_DIPSETTING(0x04, "Power Atk Squat");
            PORT_DIPSETTING(0x00, "Squat Atk Power");
            PORT_DIPNAME(0x08, 0x08, DEF_STR("Unused"));
            PORT_DIPSETTING(0x08, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_BIT(0xf0, IP_ACTIVE_LOW, IPT_UNKNOWN);
            INPUT_PORTS_END();
        }
    };

    static GfxLayout ironhors_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 0x8000 * 8 + 0 * 4, 0x8000 * 8 + 1 * 4, 2 * 4, 3 * 4, 0x8000 * 8 + 2 * 4, 0x8000 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16},
            16 * 8 /* every char takes 16 consecutive bytes */
    );

    static GfxLayout ironhors_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 1, 2, 3}, /* the four bitplanes are packed in one nibble */
            new int[]{0 * 4, 1 * 4, 0x8000 * 8 + 0 * 4, 0x8000 * 8 + 1 * 4, 2 * 4, 3 * 4, 0x8000 * 8 + 2 * 4, 0x8000 * 8 + 3 * 4,
                16 * 8 + 0 * 4, 16 * 8 + 1 * 4, 16 * 8 + 0x8000 * 8 + 0 * 4, 16 * 8 + 0x8000 * 8 + 1 * 4, 16 * 8 + 2 * 4, 16 * 8 + 3 * 4, 16 * 8 + 0x8000 * 8 + 2 * 4, 16 * 8 + 0x8000 * 8 + 3 * 4},
            new int[]{0 * 16, 1 * 16, 2 * 16, 3 * 16, 4 * 16, 5 * 16, 6 * 16, 7 * 16,
                16 * 16, 17 * 16, 18 * 16, 19 * 16, 20 * 16, 21 * 16, 22 * 16, 23 * 16},
            64 * 8 /* every sprite takes 64 consecutive bytes */
    );

    static GfxDecodeInfo ironhors_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, ironhors_charlayout, 0, 16 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, ironhors_spritelayout, 16 * 8 * 16, 16 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, ironhors_charlayout, 16 * 8 * 16, 16 * 8), /* to handle 8x8 sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static GfxLayout farwest_charlayout = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 2, 4, 6}, /* the four bitplanes are packed in one byte */
            new int[]{3 * 8 + 1, 3 * 8 + 0, 0 * 8 + 1, 0 * 8 + 0, 1 * 8 + 1, 1 * 8 + 0, 2 * 8 + 1, 2 * 8 + 0},
            new int[]{0 * 4 * 8, 1 * 4 * 8, 2 * 4 * 8, 3 * 4 * 8, 4 * 4 * 8, 5 * 4 * 8, 6 * 4 * 8, 7 * 4 * 8},
            32 * 8 /* every char takes 32 consecutive bytes */
    );

    static GfxLayout farwest_spritelayout = new GfxLayout(
            16, 16, /* 16*16 sprites */
            512, /* 512 sprites */
            4, /* 4 bits per pixel */
            new int[]{0, 512 * 32 * 8, 2 * 512 * 32 * 8, 3 * 512 * 32 * 8}, /* the four bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7,
                16 * 8 + 0, 16 * 8 + 1, 16 * 8 + 2, 16 * 8 + 3, 16 * 8 + 4, 16 * 8 + 5, 16 * 8 + 6, 16 * 8 + 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8,
                8 * 8, 9 * 8, 10 * 8, 11 * 8, 12 * 8, 13 * 8, 14 * 8, 15 * 8},
            32 * 8 /* every sprite takes 32 consecutive bytes */
    );

    static GfxLayout farwest_spritelayout2 = new GfxLayout(
            8, 8, /* 8*8 characters */
            2048, /* 2048 characters */
            4, /* 4 bits per pixel */
            new int[]{0, 2048 * 8 * 8, 2 * 2048 * 8 * 8, 3 * 2048 * 8 * 8}, /* the four bitplanes are separated */
            new int[]{0, 1, 2, 3, 4, 5, 6, 7},
            new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
            8 * 8 /* every char takes 8 consecutive bytes */
    );

    static GfxDecodeInfo farwest_gfxdecodeinfo[]
            = {
                new GfxDecodeInfo(REGION_GFX1, 0, farwest_charlayout, 0, 16 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, farwest_spritelayout, 16 * 8 * 16, 16 * 8),
                new GfxDecodeInfo(REGION_GFX2, 0, farwest_spritelayout2, 16 * 8 * 16, 16 * 8), /* to handle 8x8 sprites */
                new GfxDecodeInfo(-1) /* end of array */};

    static YM2203interface ym2203_interface = new YM2203interface(
            1, /* 2 chips */
            18432000 / 6, /* 3.07 MHz?  mod by Shingo Suzuki 1999/10/15 */
            new int[]{YM2203_VOL(40, 40)},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{null}
    );

    static MachineDriver machine_driver_ironhors = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        18432000 / 6, /* 3.07MHz? mod by Shingo Suzuki 1999/10/15 */
                        ironhors_readmem, ironhors_writemem, null, null,
                        ironhors_interrupt, 8
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        18432000 / 6, /* 3.07MHz? mod by Shingo Suzuki 1999/10/15 */
                        ironhors_sound_readmem, ironhors_sound_writemem, ironhors_sound_readport, ironhors_sound_writeport,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            30, DEFAULT_30HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 2 * 8, 30 * 8 - 1),
            ironhors_gfxdecodeinfo,
            256, 16 * 8 * 16 + 16 * 8 * 16,
            ironhors_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            ironhors_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
                )
            }
    );

    static MachineDriver machine_driver_farwest = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_M6809,
                        2000000, /* ? */
                        ironhors_readmem, ironhors_writemem, null, null,
                        ironhors_interrupt, 8
                ),
                new MachineCPU(
                        CPU_Z80 | CPU_AUDIO_CPU,
                        18432000 / 6, /* 3.07MHz? mod by Shingo Suzuki 1999/10/15 */
                        farwest_sound_readmem, farwest_sound_writemem, null, null,
                        ignore_interrupt, 1 /* interrupts are triggered by the main CPU */
                )
            },
            30, DEFAULT_30HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* 1 CPU slice per frame - interleaving is forced when a sound command is written */
            null,
            /* video hardware */
            32 * 8, 32 * 8, new rectangle(1 * 8, 31 * 8 - 1, 2 * 8, 30 * 8 - 1),
            farwest_gfxdecodeinfo,
            256, 16 * 8 * 16 + 16 * 8 * 16,
            ironhors_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            generic_vh_start,
            generic_vh_stop,
            ironhors_vh_screenrefresh,
            /* sound hardware */
            0, 0, 0, 0,
            new MachineSound[]{
                new MachineSound(
                        SOUND_YM2203,
                        ym2203_interface
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
    static RomLoadPtr rom_ironhors = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("13c_h03.bin", 0x4000, 0x8000, 0x24539af1);
            ROM_LOAD("12c_h02.bin", 0xc000, 0x4000, 0xfab07f86);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio cpu */

            ROM_LOAD("10c_h01.bin", 0x0000, 0x4000, 0x2b17930f);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("09f_h07.bin", 0x00000, 0x8000, 0xc761ec73);
            ROM_LOAD("06f_h04.bin", 0x08000, 0x8000, 0xc1486f61);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("08f_h06.bin", 0x00000, 0x8000, 0xf21d8c93);
            ROM_LOAD("07f_h05.bin", 0x08000, 0x8000, 0x60107859);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("03f_h08.bin", 0x0000, 0x0100, 0x9f6ddf83);/* palette red */

            ROM_LOAD("04f_h09.bin", 0x0100, 0x0100, 0xe6773825);/* palette green */

            ROM_LOAD("05f_h10.bin", 0x0200, 0x0100, 0x30a57860);/* palette blue */

            ROM_LOAD("10f_h12.bin", 0x0300, 0x0100, 0x5eb33e73);/* character lookup table */

            ROM_LOAD("10f_h11.bin", 0x0400, 0x0100, 0xa63e37d8);/* sprite lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_dairesya = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */

            ROM_LOAD("560-k03.13c", 0x4000, 0x8000, 0x2ac6103b);
            ROM_LOAD("560-k02.12c", 0xc000, 0x4000, 0x07bc13a9);

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio cpu */

            ROM_LOAD("560-j01.10c", 0x0000, 0x4000, 0xa203b223);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("560-k07.9f", 0x00000, 0x8000, 0xc8a1b840);
            ROM_LOAD("560-k04.6f", 0x08000, 0x8000, 0xc883d856);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("560-j06.8f", 0x00000, 0x8000, 0xa6e8248d);
            ROM_LOAD("560-j05.7f", 0x08000, 0x8000, 0xf75893d4);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("03f_h08.bin", 0x0000, 0x0100, 0x9f6ddf83);/* palette red */

            ROM_LOAD("04f_h09.bin", 0x0100, 0x0100, 0xe6773825);/* palette green */

            ROM_LOAD("05f_h10.bin", 0x0200, 0x0100, 0x30a57860);/* palette blue */

            ROM_LOAD("10f_h12.bin", 0x0300, 0x0100, 0x5eb33e73);/* character lookup table */

            ROM_LOAD("10f_h11.bin", 0x0400, 0x0100, 0xa63e37d8);/* sprite lookup table */

            ROM_END();
        }
    };

    static RomLoadPtr rom_farwest = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x12000, REGION_CPU1);/* 64k for code + 8k for extra ROM */

            ROM_LOAD("ironhors.008", 0x04000, 0x4000, 0xb1c8246c);
            ROM_LOAD("ironhors.009", 0x08000, 0x8000, 0xea34ecfc);
            ROM_LOAD("ironhors.007", 0x10000, 0x2000, 0x471182b7);/* don't know what this is for */

            ROM_REGION(0x10000, REGION_CPU2);    /* 64k for audio cpu */

            ROM_LOAD("ironhors.010", 0x0000, 0x4000, 0xa28231a6);

            ROM_REGION(0x10000, REGION_GFX1 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ironhors.005", 0x00000, 0x8000, 0xf77e5b83);
            ROM_LOAD("ironhors.006", 0x08000, 0x8000, 0x7bbc0b51);

            ROM_REGION(0x10000, REGION_GFX2 | REGIONFLAG_DISPOSE);
            ROM_LOAD("ironhors.001", 0x00000, 0x4000, 0xa8fc21d3);
            ROM_LOAD("ironhors.002", 0x04000, 0x4000, 0x9c1e5593);
            ROM_LOAD("ironhors.003", 0x08000, 0x4000, 0x3a0bf799);
            ROM_LOAD("ironhors.004", 0x0c000, 0x4000, 0x1fab18a3);

            ROM_REGION(0x0500, REGION_PROMS);
            ROM_LOAD("ironcol.003", 0x0000, 0x0100, 0x3e3fca11);/* palette red */

            ROM_LOAD("ironcol.001", 0x0100, 0x0100, 0xdfb13014);/* palette green */

            ROM_LOAD("ironcol.002", 0x0200, 0x0100, 0x77c88430);/* palette blue */

            ROM_LOAD("10f_h12.bin", 0x0300, 0x0100, 0x5eb33e73);/* character lookup table */

            ROM_LOAD("ironcol.005", 0x0400, 0x0100, 0x15077b9c);/* sprite lookup table */

            ROM_END();
        }
    };

    public static GameDriver driver_ironhors = new GameDriver("1986", "ironhors", "ironhors.java", rom_ironhors, null, machine_driver_ironhors, input_ports_ironhors, null, ROT0, "Konami", "Iron Horse", GAME_NO_COCKTAIL);
    public static GameDriver driver_dairesya = new GameDriver("1986", "dairesya", "ironhors.java", rom_dairesya, driver_ironhors, machine_driver_ironhors, input_ports_dairesya, null, ROT0, "[Konami] (Kawakusu license)", "Dai Ressya Goutou (Japan)", GAME_NO_COCKTAIL);
    public static GameDriver driver_farwest = new GameDriver("1986", "farwest", "ironhors.java", rom_farwest, driver_ironhors, machine_driver_farwest, input_ports_ironhors, null, ROT0, "bootleg?", "Far West", GAME_NOT_WORKING | GAME_NO_COCKTAIL);
}
